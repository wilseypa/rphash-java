package edu.uc.rphash;

import java.io.File;
import java.io.FileWriter;
//import java.io.FileInputStream;
import java.io.IOException;
//import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import edu.uc.rphash.Readers.RPHashObject;
import edu.uc.rphash.Readers.SimpleArrayReader;
import edu.uc.rphash.Readers.StreamObject;
import edu.uc.rphash.decoders.Dn;
import edu.uc.rphash.decoders.E8;
import edu.uc.rphash.decoders.Leech;
import edu.uc.rphash.decoders.MultiDecoder;
import edu.uc.rphash.decoders.PsdLSH;
import edu.uc.rphash.decoders.Spherical;
import edu.uc.rphash.tests.StatTests;
import edu.uc.rphash.tests.clusterers.Agglomerative3;
import edu.uc.rphash.tests.clusterers.Kmeans;
import edu.uc.rphash.tests.clusterers.StreamingKmeans;
import edu.uc.rphash.tests.kmeanspp.DoublePoint;
import edu.uc.rphash.tests.kmeanspp.KMeansPlusPlus;
import edu.uc.rphash.util.VectorUtil;

public class RPHash {

	static String[] clusteringmethods = { "simple", "streaming", "3stage",
			"multiproj", "consensus", "redux", "kmeans", "pkmeans",
			"kmeansplusplus", "streamingkmeans" };
	static String[] offlineclusteringmethods = { "singlelink",
			"completelink", "averagelink", "kmeans" };
	static String[] ops = { "numprojections", "innerdecodermultiplier",
			"numblur", "randomseed", "hashmod", "parallel", "streamduration",
			"raw", "decayrate", "dimparameter", "decodertype","offlineclusterer"
			 };
	static String[] decoders = { "dn", "e8", "multie8", "leech", "multileech",
			"sphere", "levypstable", "cauchypstable", "gaussianpstable" };

	public static void main(String[] args) throws NumberFormatException,
			IOException, InterruptedException {

		if (args.length < 3) {
			System.out.print("Usage: rphash InputFile k OutputFile [CLUSTERING_METHOD ...][OPTIONAL_ARG=value ...]\n");
			
			System.out.print("\tCLUSTERING_METHOD:\n");
			for (String s : clusteringmethods)
				System.out.print("\t\t"+s +"\n");
			
			System.out.print("\tOPTIONAL_ARG:\n");
			for (int i = 0;i<ops.length-2;i++)
			{	String s = ops[i];
				System.out.println("\t\t" + s);
			}
			System.out.print("\t\t"+ops[ops.length-2]+"\t:[");
			for (String s : decoders)
				System.out.print(s + " ,");
			System.out.print("]\n");

			System.out.print("\t\t"+ops[ops.length-1]+"\t:[");
			for (String s : offlineclusteringmethods)
				System.out.print(s + " ,");
			System.out.print("]\n");

			System.exit(0);
		}

		List<float[]> data = null;

		String filename = args[0];
		int k = Integer.parseInt(args[1]);
		String outputFile = args[2];

		boolean raw = false;

		if (args.length == 3) {
			data = VectorUtil.readFile(filename, raw);
			RPHashSimple clusterer = new RPHashSimple(data, k);
			VectorUtil.writeFile(new File(outputFile + "."
					+ clusterer.getClass().getName()),
					clusterer.getCentroids(), raw);
		}

		List<String> truncatedArgs = new ArrayList<String>();
		Map<String, String> taggedArgs = argsUI(args, truncatedArgs);

		if (!taggedArgs.containsKey("streamduration"))
			data = VectorUtil.readFile(filename, raw);

		List<Clusterer> runs;
		if (taggedArgs.containsKey("raw")) {
			raw = Boolean.getBoolean(taggedArgs.get("raw"));
			runs = runConfigs(truncatedArgs, taggedArgs, data, filename, true);
		} else {
			runs = runConfigs(truncatedArgs, taggedArgs, data, filename, false);
		}

		if (taggedArgs.containsKey("streamduration")) {
			runStream(runs, outputFile,
					Integer.parseInt(taggedArgs.get("streamduration")), k, raw);
			return;
		}
		// run remaining, read file into ram
		data = VectorUtil.readFile(filename, raw);
		runner(runs, outputFile, raw);

	}

	public static void runner(List<Clusterer> runitems, String outputFile,
			boolean raw) throws InterruptedException {
		for (Clusterer clu : runitems) {
			String[] ClusterHashName = clu.getClass().getName().split("\\.");
//			String[] DecoderHashName = clu.getParam().toString().split("\\.");
			System.out.print(ClusterHashName[ClusterHashName.length - 1] + " { "+clu.getParam().toString()
//					ClusterHashName[ClusterHashName.length - 1] + "{"
//					+ DecoderHashName[DecoderHashName.length - 2]
					+ "} processing time : ");
			
			Runtime rt = Runtime.getRuntime();

			long startmemory = rt.totalMemory() - rt.freeMemory();
			long startTime = System.nanoTime();
			List<float[]> cents = clu.getCentroids();
			float timed = (System.nanoTime() - startTime) / 1000000000f;
			long usedkB = ((rt.totalMemory() - rt.freeMemory())-startmemory) / 1024;
			
			RPHashObject reader = clu.getParam();

			double wcsse = StatTests.WCSSE(cents, reader.getData());

			System.out.println(timed + ", used(KB): "+usedkB +", wcsse: "+wcsse);
			try {
				FileWriter metricsfile =new FileWriter(new File("metrics_time_memkb_wcsse.csv"));
				metricsfile.write(timed+","+usedkB+","+wcsse+"\n");
				metricsfile.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			
			
			VectorUtil.writeFile(new File(outputFile + "."
					+ ClusterHashName[ClusterHashName.length - 1]),
					clu.getCentroids(), raw);
		}

	}

	/**
	 * Compute the average time to read a file
	 * 
	 * @param streamDuration
	 * @param f
	 *            - file name string
	 * @param testsize
	 * @return the number of milliseconds it takes on average to read
	 *         streamduration vectors
	 * @throws IOException
	 */
	public static long computeAverageReadTime(Integer streamDuration, String f,
			int testsize, boolean raw) throws IOException {
		StreamObject streamer = new StreamObject(f, 0, raw);
		int i = 0;

		ArrayList<float[]> vecsInThisRound = new ArrayList<float[]>();
		long startTime = System.nanoTime();
		while (streamer.hasNext() && i < testsize) {
			i++;
			float[] nxt = streamer.next();
			vecsInThisRound.add(nxt);
		}
		streamer.reset();
		return (System.nanoTime() - startTime);
	}

	public static void runStream(List<Clusterer> runitems, String outputFile,
			Integer streamDuration, int k, boolean raw) throws IOException,
			InterruptedException {

		Iterator<Clusterer> cluit = runitems.iterator();
		// needs work, just use for both to be more accurate
		long avgtimeToRead = 0;// computeAverageReadTime(streamDuration,f,streamDuration);
		Runtime rt = Runtime.getRuntime();

		while (cluit.hasNext()) {
			Clusterer clu = cluit.next();
			StreamObject streamer = (StreamObject) clu.getParam();
			if (clu instanceof StreamClusterer) {
				String[] ClusterHashName = clu.getClass().getName().split("\\.");
				System.out.print(ClusterHashName[ClusterHashName.length - 1] 
						+ " { "+clu.getParam().toString()
						+ "}"
						+ ",stream_duration:" + streamDuration
						+ "} \n cpu time \t wcsse \t\t\t mem(kb)\n");

				long startTime = System.nanoTime() + avgtimeToRead;
				int i = 1;
				ArrayList<float[]> vecsInThisRound = new ArrayList<float[]>();

				while (streamer.hasNext()) {

					i++;
					float[] nxt = streamer.next();
					vecsInThisRound.add(nxt);
					((StreamClusterer) clu).addVectorOnlineStep(nxt);

					if (i % streamDuration == 0 || !streamer.hasNext()) {
						List<float[]> cents = ((StreamClusterer) clu)
								.getCentroidsOfflineStep();
						
						long time = System.nanoTime() - startTime;
						double wcsse = StatTests.WCSSE(cents, vecsInThisRound);
						vecsInThisRound = new ArrayList<float[]>();
						rt.gc();
						Thread.sleep(10);
						rt.gc();

						long usedkB = (rt.totalMemory() - rt.freeMemory()) / 1024;
						System.out.println(time / 1000000000f + "\t" + wcsse
								+ "\t" + usedkB);
						VectorUtil.writeFile(new File(outputFile + "_round" + i
								+ "."
								+ ClusterHashName[ClusterHashName.length - 1]),
								cents, raw);
						startTime = System.nanoTime() + avgtimeToRead;
					}
				}
				streamer.reset();
				cluit.remove();
			}
		}
	}

	public static List<Clusterer> runConfigs(List<String> untaggedArgs,
			Map<String, String> taggedArgs, List<float[]> data, String f,
			boolean raw) throws IOException {

		List<Clusterer> runitems = new ArrayList<>();
		int i = 3;
		// List<float[]> data = TestUtil.readFile(new
		// File(untaggedArgs.get(0)));
		// float variance = StatTests.varianceSample(data, .01f);

		int k = Integer.parseInt(untaggedArgs.get(1));

		
		RPHashObject o = new SimpleArrayReader(data, k);
		StreamObject so = new StreamObject(f, k, raw);

		if (taggedArgs.containsKey("numprojections")) {
			so.setNumProjections(Integer.parseInt(taggedArgs
					.get("numprojections")));
			o.setNumProjections(Integer.parseInt(taggedArgs
					.get("numprojections")));
		}
		if (taggedArgs.containsKey("innerdecodermultiplier")) {
			o.setInnerDecoderMultiplier(Integer.parseInt(taggedArgs
					.get("innerdecodermultiplier")));
			so.setInnerDecoderMultiplier(Integer.parseInt(taggedArgs
					.get("innerdecodermultiplier")));
		}
		if (taggedArgs.containsKey("numblur")) {
			o.setNumBlur(Integer.parseInt(taggedArgs.get("numblur")));
			so.setNumBlur(Integer.parseInt(taggedArgs.get("numblur")));
		}
		if (taggedArgs.containsKey("randomseed")) {
			o.setRandomSeed(Long.parseLong(taggedArgs.get("randomseed")));
			so.setRandomSeed(Long.parseLong(taggedArgs.get("randomseed")));
		}
		if (taggedArgs.containsKey("hashmod")) {
			o.setHashMod(Long.parseLong(taggedArgs.get("hashmod")));
			so.setHashMod(Long.parseLong(taggedArgs.get("hashmod")));
		}
		if (taggedArgs.containsKey("decayrate")) {
			o.setDecayRate(Float.parseFloat(taggedArgs.get("decayrate")));
			so.setDecayRate(Float.parseFloat(taggedArgs.get("decayrate")));
		}
		if (taggedArgs.containsKey("parallel")) {
			o.setParallel(Boolean.parseBoolean(taggedArgs.get("parallel")));
			so.setParallel(Boolean.parseBoolean(taggedArgs.get("parallel")));
		}
		if (taggedArgs.containsKey("dimparameter")) {
			o.setDimparameter(Integer.parseInt(taggedArgs.get("dimparameter")));
			so.setDimparameter(Integer.parseInt(taggedArgs.get("dimparameter")));
		}
		if (taggedArgs.containsKey("decodertype")) {
			switch (taggedArgs.get("decodertype").toLowerCase()) {
			case "dn": {
				o.setDecoderType(new Dn(o.getDimparameter()));
				so.setDecoderType(new Dn(o.getDimparameter()));
				break;
			}
			case "e8": {
				o.setDecoderType(new E8(2f));
				so.setDecoderType(new E8(2f));
				break;
			}
			case "multie8": {
				o.setDecoderType(new MultiDecoder(
						o.getInnerDecoderMultiplier() * 8, new E8(2f)));
				so.setDecoderType(new MultiDecoder(so
						.getInnerDecoderMultiplier() * 8, new E8(2f)));
				break;
			}
			case "leech": {
				o.setDecoderType(new Leech(2f));
				so.setDecoderType(new Leech(2f));
				break;
			}
			case "multileech": {
				o.setDecoderType(new MultiDecoder(
						o.getInnerDecoderMultiplier() * 24, new Leech(2f)));
				so.setDecoderType(new MultiDecoder(so
						.getInnerDecoderMultiplier() * 24, new Leech(2f)));
				break;
			}
			case "levypstable": {
				o.setDecoderType(new PsdLSH(PsdLSH.LEVY, o.getDimparameter()));
				so.setDecoderType(new PsdLSH(PsdLSH.LEVY, o.getDimparameter()));
				break;
			}
			case "cauchypstable": {
				o.setDecoderType(new PsdLSH(PsdLSH.CAUCHY, o.getDimparameter()));
				so.setDecoderType(new PsdLSH(PsdLSH.CAUCHY, o.getDimparameter()));
				break;
			}
			case "gaussianpstable": {
				o.setDecoderType(new PsdLSH(PsdLSH.GAUSSIAN, o
						.getDimparameter()));
				so.setDecoderType(new PsdLSH(PsdLSH.GAUSSIAN, o
						.getDimparameter()));
				break;
			}
			case "sphere": {
				o.setDecoderType(new Spherical(o.getDimparameter(), 3, 2));
				so.setDecoderType(new Spherical(o.getDimparameter(), 3, 2));
				break;
			}
			default: {
				System.out.println(taggedArgs.get("decodertype")
						+ " decoder does not exist, using defaults");
			}
			}
		}

		if (taggedArgs.containsKey("offlineclusterer")) 
		{
			switch (taggedArgs.get("offlineclusterer").toLowerCase()) {
			case "singlelink": {

				o.setOfflineClusterer(new Agglomerative3(
						Agglomerative3.ClusteringType.SINGLE_LINKAGE));
				so.setOfflineClusterer(new Agglomerative3(
						Agglomerative3.ClusteringType.SINGLE_LINKAGE));
				break;
			}
			case "completelink": {

				o.setOfflineClusterer(new Agglomerative3(
						Agglomerative3.ClusteringType.COMPLETE_LINKAGE));
				so.setOfflineClusterer(new Agglomerative3(
						Agglomerative3.ClusteringType.COMPLETE_LINKAGE));
				break;
			}
			case "averagelink": {
				o.setOfflineClusterer(new Agglomerative3(
						Agglomerative3.ClusteringType.AVG_LINKAGE));
				so.setOfflineClusterer(new Agglomerative3(
						Agglomerative3.ClusteringType.AVG_LINKAGE));
				break;
			}
			case "kmeans": {
				o.setOfflineClusterer(new Kmeans());
				so.setOfflineClusterer(new Kmeans());
				break;
			}
			default: {
				System.out.println(taggedArgs.get("clustering type")
						+ "does not exist, using defaults");
			}
			}
		}

		while (i < untaggedArgs.size()) {
			switch (untaggedArgs.get(i).toLowerCase()) {
			case "simple":
				runitems.add(new RPHashSimple(o));
				break;
			case "streaming":{
				if(taggedArgs.containsKey("streamduration"))
					runitems.add(new RPHashStream(so));
				else
					runitems.add(new RPHashStream(o));
				break;
			}
			case "3stage":
				runitems.add(new RPHash3Stage(o));
				break;
			case "concensus":
				runitems.add(new RPHashConsensusRP(o));
				break;
			case "multiproj":
				runitems.add(new RPHashMultiProj(o));
				break;
			case "redux":
				runitems.add(new RPHashIterativeRedux(o));
				break;
			case "kmeans":
				runitems.add(new Kmeans(k, data));
				break;
			case "pkmeans":
				runitems.add(new Kmeans(k, data, o.getNumProjections()));
				break;
			case "kmeansplusplus":
				runitems.add(new KMeansPlusPlus<DoublePoint>(data, k));
				break;
			case "streamingkmeans":{
				if(taggedArgs.containsKey("streamduration"))
					runitems.add(new StreamingKmeans(so));
				else
					runitems.add(new StreamingKmeans(o));
				break;
			}
			default:
				System.out.println(untaggedArgs.get(i) + " does not exist");
				break;
			}
			i++;
		}
		return runitems;

	}

	/**
	 * Parse the cmd options, fill in non-default values
	 * 
	 * @param args
	 * @param mpsim
	 */
	public static Map<String, String> argsUI(String[] args,
			List<String> truncatedArgs) {

		Map<String, String> cmdMap = new HashMap<String, String>();
		for (String s : args) {
			String[] cmd = s.split("=");
			if (cmd.length > 1)
				cmdMap.put(cmd[0].toLowerCase(), cmd[1].toLowerCase());
			else
				truncatedArgs.add(s);
		}
		args = new String[truncatedArgs.size()];
		for (int i = 0; i < truncatedArgs.size(); i++)
			args[i] = truncatedArgs.get(i);
		return cmdMap;
	}

}

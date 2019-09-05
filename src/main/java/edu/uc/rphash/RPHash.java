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
import java.util.Random;




//import org.rosuda.JRI.Rengine;
import edu.uc.rphash.Readers.RPHashObject;
import edu.uc.rphash.Readers.SimpleArrayReader;
import edu.uc.rphash.Readers.StreamObject;
import edu.uc.rphash.decoders.DepthProbingLSH;
import edu.uc.rphash.decoders.Dn;
import edu.uc.rphash.decoders.E8;

import edu.uc.rphash.decoders.Leech;
import edu.uc.rphash.decoders.MultiDecoder;
import edu.uc.rphash.decoders.OriginDecoder;

import edu.uc.rphash.decoders.Spherical;
import edu.uc.rphash.projections.DBFriendlyProjection;
import edu.uc.rphash.projections.FJLTProjection;
import edu.uc.rphash.projections.GaussianProjection;
import edu.uc.rphash.projections.NoProjection;
import edu.uc.rphash.projections.SVDProjection;
import edu.uc.rphash.tests.StatTests;

import edu.uc.rphash.tests.clusterers.Agglomerative3;
import edu.uc.rphash.tests.clusterers.DummyClusterer;
import edu.uc.rphash.tests.clusterers.DBScan;
import edu.uc.rphash.tests.clusterers.KMeans2;
import edu.uc.rphash.tests.clusterers.KMeansPlusPlus;

import edu.uc.rphash.tests.clusterers.MultiKMPP;
import edu.uc.rphash.tests.clusterers.StreamingKmeans;
import edu.uc.rphash.tests.clusterers.StreamingKmeans2;
import edu.uc.rphash.util.VectorUtil;

//import edu.uc.rphash.tests.kmeanspp.KMeansPlusPlus;

public class RPHash {

	static String[] clusteringmethods = { "simple", "streaming", "multiproj", 
		"kmeans", "pkmeans","kmeansplusplus", "streamingkmeans", "adaptive","dummy" ,"twrp" , "twrpbisect", "twrpbest", "twrpmergetree" };
	
	static String[] offlineclusteringmethods = { "singlelink", "completelink",
		"averagelink", "kmeans", "adaptivemeanshift", "kmpp", "multikmpp" , "dbscan", "none" };
	
	static String[] projectionmethods = { "dbf", "fjlt", "rp", "svd", "noproj" };
	
	static String[] ops = { "numprojections", "innerdecodermultiplier",
		"numblur", "randomseed", "hashmod", "parallel", "streamduration",
		"raw", "decayrate", "dimparameter", "decodertype",
		"offlineclusterer", "runs", "normalize", "projection" };
	
	static String[] decoders = { "dn", "e8", "golay", "multie8", "leech",
			"multileech", "sphere", "levypstable", "cauchypstable",
			"gaussianpstable", "adaptive", "origin" };
	
	static String[] twrp_options = { "cutoff", "randomvector" };
	
	

	public static void main(String[] args) throws NumberFormatException,
			IOException, InterruptedException {

		
		if (args.length < 3) {
			System.out
					.print("Usage: rphash InputFile k OutputFile [CLUSTERING_METHOD ...][OPTIONAL_ARG=value ...]\n");

			System.out.print("\tCLUSTERING_METHOD:\n");
			for (String s : clusteringmethods)
				System.out.print("\t\t" + s + "\n");

			System.out.print("\tOPTIONAL_ARG:\n");
			for (int i = 0; i < ops.length - 2; i++) {
				String s = ops[i];
				System.out.println("\t\t" + s);
			}
			System.out.print("\t\t" + ops[ops.length - 2] + "\t:[");
			for (String s : decoders)
				System.out.print(s + " ,");
			System.out.print("]\n");

			System.out.print("\t\t" + ops[ops.length - 1] + "\t:[");
			for (String s : offlineclusteringmethods)
				System.out.print(s + " ,");
			System.out.print("]\n");

			System.out.print("\t Projection_methods" + "\t:[");
			for (String s : projectionmethods)
				System.out.print(s + " ,");
			System.out.print("]\n");

			System.out.print("\t twrp_options" + "\t:[");
			for (String s : twrp_options)
				System.out.print(s + " ,");
			System.out.print("]\n");
			
			
			System.exit(0);
		}

		List<String> unmatchedkeywords = new ArrayList<>();
		
		for (int i = 3; i < args.length; i++) {
			args[i] = args[i].toLowerCase();
			String arg = args[i];
			String keyword = arg.split("=")[0];// either just a keyword or
												// keyword=value
			boolean matched = false;
			for (String match : clusteringmethods)
				matched |= keyword.equals(match);
			for (String match : offlineclusteringmethods)
				matched |= keyword.equals(match);
			for (String match : ops)
				matched |= keyword.equals(match);
			for (String match : decoders)
				matched |= keyword.equals(match);
			for (String match : twrp_options)
				matched |= keyword.equals(match);
			
			if (!matched)
				unmatchedkeywords.add(keyword);
		}
		if (unmatchedkeywords.size() > 0) {
			System.out.println("ERROR Keyword(s) not found:");
			System.out.println("\t" + unmatchedkeywords);
			System.exit(0);
		}

		List<float[]> data = null;

		String filename = args[0];
		int k = Integer.parseInt(args[1]);
		String outputFile = args[2];

		
		List<String> truncatedArgs = new ArrayList<String>();
		Map<String, String> taggedArgs = argsUI(args, truncatedArgs);
		boolean raw = Boolean.parseBoolean(taggedArgs.get("raw"));
		int bestofruns = 1;

		// Rengine re = Rengine.getMainEngine();
		// if(re == null)
		// re = new Rengine(new String[] {"--no-save"}, false, null);
		// basic run configuration
		if (args.length == 3) {
			data = VectorUtil.readFile(filename, raw);
			RPHashSimple clusterer = new RPHashSimple(data, k);
			VectorUtil.writeCentroidsToFile(new File(outputFile + "."
					+ clusterer.getClass().getName()),
					clusterer.getCentroids(), raw);
		}

		List<Clusterer> runs;

		//non streaming data
		if (!taggedArgs.containsKey("streamduration")){
			data = VectorUtil.readFile(filename, raw);
		}
		
		if (taggedArgs.containsKey("runs")) {
			bestofruns = Integer.parseInt(taggedArgs.get("runs"));
		}

		StreamObject so = new StreamObject(filename, k, raw);
		
		RPHashObject o = new SimpleArrayReader(data, k);	
		
		runs = runConfigs(truncatedArgs, taggedArgs,o,so);
		
		if (taggedArgs.containsKey("streamduration")) {
			runStreamForSetOfClusterers(runs, outputFile,
					Integer.parseInt(taggedArgs.get("streamduration")), k, raw,
					bestofruns, filename,so);
		} else {
			// run remaining, read file into ram
			runner(runs, outputFile, raw, bestofruns, data);
		}

	}

	/**
	 * Run the cluster and find the best clustering
	 * 
	 * @param clu
	 * @param raw
	 * @param runs
	 * @return the minimum wcss centroid set
	 */
	public static List<Centroid> runclusterer(Clusterer clu, boolean raw,
			int runs, List<float[]> data) {

		// some clusterers can do multi runs in parallel
		if (clu.setMultiRun(runs)) {
			return clu.getCentroids();
		} 
		else {
			List<Centroid> mincents = clu.getCentroids();
			double minwcss = StatTests.WCSSECentroidsFloat(mincents, data);

			for (int i = 1; i < runs; i++) {
				clu.reset(new Random().nextInt());
				List<Centroid> tmpcents = clu.getCentroids();
				double tmpwcss = StatTests.WCSSECentroidsFloat(tmpcents, data);

				if (tmpwcss < minwcss) {
					mincents = tmpcents;
					minwcss = tmpwcss;
				}
				// System.out.println(tmpwcss+":"+minwcss);
			}
			return mincents;
		}

	}

	/**
	 * This function runs the set of run items as specified on the command line
	 * as an ordered list of cluster tasks
	 * 
	 * @param runitems
	 * @param outputFile
	 * @param raw
	 * @param runs
	 * @throws InterruptedException
	 */
	public static void runner(List<Clusterer> runitems, String outputFile,
			boolean raw, int runs, List<float[]> data)
			throws InterruptedException {
		for (Clusterer clu : runitems) {
			String[] ClusterHashName = clu.getClass().getName().split("\\.");
			// String[] DecoderHashName =
			// clu.getParam().toString().split("\\.");
			if (clu.getParam() != null)
				System.out.print(ClusterHashName[ClusterHashName.length - 1]
						+ " { " + clu.getParam().toString()
						// ClusterHashName[ClusterHashName.length - 1] + "{"
						// + DecoderHashName[DecoderHashName.length - 2]
						+ "} processing time : ");

			Runtime rt = Runtime.getRuntime();
			rt.gc();
			Thread.sleep(10);
			rt.gc();
			long startmemory = rt.totalMemory() - rt.freeMemory();
			long startTime = System.nanoTime();

			List<Centroid> cents = runclusterer(clu, raw, runs, data);

			float timed = (System.nanoTime() - startTime) / 1000000000f;
			rt.gc();
			Thread.sleep(10);
			rt.gc();
			long usedkB = ((rt.totalMemory() - rt.freeMemory()) - startmemory) / 1024;

			// RPHashObject reader = clu.getParam();

			double wcsse = StatTests.WCSSECentroidsFloat(cents, data);

			System.out.println(timed + ", used(KB): " + usedkB + ", wcsse: "
					+ wcsse);
			try {
				FileWriter metricsfile = new FileWriter(new File(
						"metrics_time_memkb_wcsse.csv"));
				metricsfile.write(timed + "," + usedkB + "," + wcsse + "\n");
				metricsfile.close();
			} catch (IOException e) {
				e.printStackTrace();
			}

			VectorUtil.writeCentroidsToFile(new File(outputFile + "."
					+ ClusterHashName[ClusterHashName.length - 1]),
					clu.getCentroids(), raw);
			
			if(clu instanceof RPHashSimple){
				VectorUtil.writeVectorFile(new File(outputFile + "."
						+ ClusterHashName[ClusterHashName.length - 1]+".labels"), ((RPHashSimple)clu).getLabels());
			}
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

	/**
	 * Run a subset of a stream for the available clusterers print the results
	 * NOTE this does not use the bestOfRuns parameter yet
	 * 
	 * @param outputFile
	 * @param streamDuration
	 * @param k
	 * @param raw
	 * @param clu
	 * @param avgtimeToRead
	 * @param rt
	 * @param streamer
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void runStreamForAClusterer(String outputFile,
			Integer streamDuration, int k, boolean raw, Clusterer clu,
			long avgtimeToRead, Runtime rt, StreamObject streamer/* , Rengine re */)
			throws IOException, InterruptedException {

		if (clu instanceof StreamClusterer) {
			String[] ClusterHashName = clu.getClass().getName().split("\\.");
			System.out
					.print(ClusterHashName[ClusterHashName.length - 1]
							+ " { "
							+ clu.getParam().toString()
							+ "}"
							+ ",stream_duration:"
							+ streamDuration
							+ "} \n cpu time \t wcsse \t\t\t mem(kb)\t\tcluster_count\n");

			rt.gc();
			Thread.sleep(1000);
			rt.gc();

			long usedkB = (rt.totalMemory() - rt.freeMemory());
			long startTime = System.nanoTime() + avgtimeToRead;
			int i = 1;
			//this will incur massive memory requrement and is for testing
			ArrayList<float[]> vecsInThisRound = new ArrayList<float[]>();
			int count = 0;
			while (streamer.hasNext()) {

				float[] nxt = streamer.next();
				
				if(streamDuration>0)vecsInThisRound.add(nxt);
				
				((StreamClusterer) clu).addVectorOnlineStep(nxt);
				//-1 for stream duration means do not stop
				if (streamDuration>0 && i % streamDuration == 0) {
					List<Centroid> cents = ((StreamClusterer) clu)
							.getCentroidsOfflineStep();

					long time = System.nanoTime() - startTime;
					double wcsse = StatTests.WCSSECentroidsFloat(cents,
							vecsInThisRound);
					count += vecsInThisRound.size();
					vecsInThisRound = new ArrayList<float[]>();

					rt.gc();
					Thread.sleep(10);
					rt.gc();

					System.out.println(time / 1000000000f + "\t" + wcsse
							+ "\t "
							+ ((rt.totalMemory() - rt.freeMemory()) - usedkB)
							/ 1024 + "\t\t\t" + cents.size());
					
					VectorUtil.writeCentroidsToFile(new File(outputFile
							+ "_round" + new Integer(count).toString() + "."
							+ ClusterHashName[ClusterHashName.length - 1]),
							cents, false);

					startTime = System.nanoTime() + avgtimeToRead;
				}
				i++;
			}
			if (!streamer.hasNext()) {
				((StreamClusterer) clu).shutdown();

			}
			
			long time = System.nanoTime() - startTime;
			streamer.reset();
			List<Centroid> cents = ((StreamClusterer) clu)
					.getCentroidsOfflineStep();
			
			double wcsse = StatTests.WCSSECentroidsFloat(cents,streamer);

			System.out.println( time / 1000000000f + "\t" + wcsse
					+ "\t "
					+ ((rt.totalMemory() - rt.freeMemory()) - usedkB)
					/ 1024 + "\t\t\t" + cents.size());
			
			VectorUtil.writeCentroidsToFile(new File(outputFile
					+ "_round" + new Integer(count).toString() + "."
					+ ClusterHashName[ClusterHashName.length - 1]),
					cents, false);


			
		}

	}

	/**
	 * Apply stream to set of clusterers, output results periodically according
	 * to cmd parameter StreamDuration
	 * 
	 * @param runitems
	 * @param outputFile
	 * @param streamDuration
	 * @param k
	 * @param raw
	 * @param bestofruns
	 * @throws IOException
	 * @throws InterruptedException
	 */
	public static void runStreamForSetOfClusterers(List<Clusterer> runitems,
			String outputFile, Integer streamDuration, int k, boolean raw,
			int bestofruns,String input, StreamObject so) throws IOException, InterruptedException {

		Iterator<Clusterer> cluit = runitems.iterator();
		// needs work, just use for both to be more accurate
		long avgtimeToRead = 0;// computeAverageReadTime(streamDuration,f,streamDuration);

		Runtime rt = Runtime.getRuntime();

		while (cluit.hasNext()) {
			Clusterer clu = cluit.next();
			// due to the memoryless nature of stream clusterers
			// all stream clusterers must run in multi clustering
			// in parallel
			clu.setMultiRun(bestofruns);
//TODO second creation
			//StreamObject streamer = new StreamObject(input, k, raw);//(StreamObject) clu.getParam();
			
			runStreamForAClusterer(outputFile, streamDuration, k, raw, clu,
					avgtimeToRead, rt, so);
			cluit.remove();
		}

	}

	public static List<Clusterer> runConfigs(List<String> untaggedArgs,
			Map<String, String> taggedArgs
			,RPHashObject o,StreamObject so) throws IOException {

		List<Clusterer> runitems = new ArrayList<>();
		int i = 3;
		// List<float[]> data = TestUtil.readFile(new
		// File(untaggedArgs.get(0)));
		// float variance = StatTests.varianceSample(data, .01f);

		int k = Integer.parseInt(untaggedArgs.get(1));
		
		o.setK(k);
		so.setK(k);
		
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

		if (taggedArgs.containsKey("normalize")) {
			o.setNormalize(Boolean.parseBoolean(taggedArgs.get("normalize")));
			so.setNormalize(Boolean.parseBoolean(taggedArgs.get("normalize")));
		}
		
		
		if (taggedArgs.containsKey("cutoff")) {
			o.setCutoff(Integer.parseInt(taggedArgs.get("cutoff")));
			so.setCutoff(Integer.parseInt(taggedArgs.get("cutoff")));
		}
		
		
		if (taggedArgs.containsKey("randomvector")) {
			o.setRandomVector(Boolean.parseBoolean(taggedArgs.get("randomvector")));
			so.setRandomVector(Boolean.parseBoolean(taggedArgs.get("randomvector")));
		}
		
		

		if (taggedArgs.containsKey("projection")) {
			switch (taggedArgs.get("projection")) {

			case "dbf": {
				o.setProjectionType(new DBFriendlyProjection());
				so.setProjectionType(new DBFriendlyProjection());
				break;
			}
			case "rp": {
				o.setProjectionType(new GaussianProjection());
				so.setProjectionType(new GaussianProjection());
				break;
			}
			case "svd": {
				o.setProjectionType(new SVDProjection(o.getRawData()));
				so.setProjectionType(new DBFriendlyProjection());
				break;
			}
			case "noproj": {
				o.setProjectionType(new NoProjection());
				so.setProjectionType(new NoProjection());
				break;
			}
			case "fjlt": {
				o.setProjectionType(new FJLTProjection(1_000_000_000));
				so.setProjectionType(new FJLTProjection(1_000_000_000));
				break;
			}

			default: {
				System.out.println("projection method does not exist");
				System.exit(2);
			}
			}
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
				o.setDecoderType(new Leech());
				so.setDecoderType(new Leech());
				break;
			}
			case "multileech": {
				o.setDecoderType(new MultiDecoder(
						o.getInnerDecoderMultiplier() * 24, new Leech()));
				so.setDecoderType(new MultiDecoder(so
						.getInnerDecoderMultiplier() * 24, new Leech()));
				break;
			}
			
				
			
			case "sphere": {// pad to ~32 bits
				// int ctsofsphere =
				// (int)(Math.log(o.getDimparameter()*2)/Math.log(2.0)) /2;
				o.setDecoderType(new Spherical(o.getDimparameter(), 4, 1));
				so.setDecoderType(new Spherical(o.getDimparameter(), 4, 1));
				// o.setDecoderType(new Spherical(o.getDimparameter(),
				// ctsofsphere, o.getNumBlur()));
				// so.setDecoderType(new Spherical(o.getDimparameter(),
				// ctsofsphere, o.getNumBlur()));
				break;
			}
			case "origin": {
				o.setDecoderType(new OriginDecoder(o.getDimparameter()));
				so.setDecoderType(new OriginDecoder(o.getDimparameter()));
				break;
			}
			case "adaptive": {
				o.setDecoderType(new DepthProbingLSH(o.getDimparameter()));
				so.setDecoderType(new DepthProbingLSH(o.getDimparameter()));
				break;
			}

			default: {
				System.out.println(taggedArgs.get("decodertype")
						+ " decoder does not exist, using defaults");
			}
			}
		}

		if (taggedArgs.containsKey("offlineclusterer")) {
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

				o.setOfflineClusterer(new KMeans2());
				so.setOfflineClusterer(new KMeans2());

				break;
			}
			case "kmeansplusplus":
				o.setOfflineClusterer(new KMeansPlusPlus());
				so.setOfflineClusterer(new KMeansPlusPlus());
				break;
			
			case "kmpp": {

				o.setOfflineClusterer(new KMeansPlusPlus());
				so.setOfflineClusterer(new KMeansPlusPlus());

				break;
			}

			
			case "multikmpp": {

				o.setOfflineClusterer(new MultiKMPP());
				so.setOfflineClusterer(new MultiKMPP());

				break;
			}
			
			
			case "dbscan": {

				o.setOfflineClusterer(new DBScan());
				so.setOfflineClusterer(new DBScan());

				break;
			}
			
			case "none": {

				o.setOfflineClusterer(null);
				so.setOfflineClusterer(null);

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
			case "streaming": {
				if (taggedArgs.containsKey("streamduration"))
					runitems.add(new RPHashStream(so));
				else
					runitems.add(new RPHashStream(o));
				break;
			}
			case "multiproj":
				runitems.add(new RPHashSimple(o));
				// runitems.add(new RPHashMultiProj(o));
				break;

			case "kmeans": {
				runitems.add(new KMeans2(k, o.getRawData()));
				break;
			}
			
			case "kmeansplusplus":
				runitems.add(new KMeansPlusPlus(o.getRawData(), k));
				break;

			case "streamingkmeans": {
				if (taggedArgs.containsKey("streamduration"))
					runitems.add(new StreamingKmeans2(so));
				else
					runitems.add(new StreamingKmeans2(o));
				break;
			}
			
			case "adaptive": {
				runitems.add(new RPHashAdaptive2Pass(o));
				break;
			}
			
			case "twrp": {
				runitems.add(new TWRPv2(o));
				break;
			}
			
			case "twrpmergetree": {
				runitems.add(new TWRPv3(o));
				break;
			}
			
			case "twrpbisect": {
				runitems.add(new TWRPv4(o));
				break;
			}
			
			case "twrpbest": {
				runitems.add(new TWRPv5_WCSS(o));
				break;
			}
			
		
			
			case "dummy": {
				runitems.add(new DummyClusterer(so));
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

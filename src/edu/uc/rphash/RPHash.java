package edu.uc.rphash;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.uc.rphash.Readers.RPHashObject;
import edu.uc.rphash.Readers.SimpleArrayReader;
import edu.uc.rphash.decoders.Dn;
import edu.uc.rphash.decoders.E8;
import edu.uc.rphash.decoders.Leech;
import edu.uc.rphash.decoders.MultiDecoder;
import edu.uc.rphash.decoders.PStableDistribution;
import edu.uc.rphash.decoders.Spherical;
import edu.uc.rphash.tests.Kmeans;
import edu.uc.rphash.tests.StatTests;
import edu.uc.rphash.tests.StreamingKmeans;
import edu.uc.rphash.tests.TestUtil;
import edu.uc.rphash.tests.kmeanspp.DoublePoint;
import edu.uc.rphash.tests.kmeanspp.KMeansPlusPlus;

public class RPHash {

	static String[] rphashes = { "simple", "streaming", "3stage", "multiProj", "consensus",
			"redux", "kmeans", "pkmeans","kmeansplusplus","streamingkmeans" };
	static String[] ops = { "NumProjections", "InnerDecoderMultiplier",
			"NumBlur", "RandomSeed", "Hashmod", "DecoderType" };
	static String[] decoders = { "Dn", "E8", "MultiE8", "Leech", "MultiLeech",
			"PStable", "Sphere" };

	public static void main(String[] args) {

		if (args.length < 3) {
			System.out.print("Usage: rphash InputFile k OutputFile [");
			for (String s : rphashes)
				System.out.print(s + " ,");
			System.out.print("] [arg=value...]\n \t Optional Args:\n");

			for (String s : ops)
				System.out.println("\t\t" + s);
			System.out.print("\t\t\t\t:[");
			for (String s : decoders)
				System.out.print(s + " ,");
			System.out.print("]\n");

			System.exit(0);
		}

		List<float[]> data = TestUtil.readFile(new File(args[0]));
		int k = Integer.parseInt(args[1]);
		String outputFile = args[2];
		if (args.length == 3) {
			RPHashSimple clusterer = new RPHashSimple(data, k);
			TestUtil.writeFile(new File(outputFile + "."
					+ clusterer.getClass().getName()), clusterer.getCentroids());
		}
		List<String> truncatedArgs = new ArrayList<String>();
		Map<String, String> taggedArgs = argsUI(args, truncatedArgs);
		List<Clusterer> runs = runConfigs(truncatedArgs, taggedArgs, data);
		runner(runs, outputFile);
	}

	public static void runner(List<Clusterer> runitems, String outputFile) {
		for (Clusterer clu : runitems) {
			
			String[] ClusterHashName = clu.getClass().getName().split("\\.");
			String[] DecoderHashName = clu.getParam().toString().split("\\.");
			System.out.print(ClusterHashName[ClusterHashName.length-1] + "{"
					+ DecoderHashName[DecoderHashName.length-1] + "} processing time : ");
			long startTime = System.nanoTime();
			clu.getCentroids();
			System.out.println((System.nanoTime() - startTime) / 1000000000f);
			TestUtil.writeFile(new File(outputFile + "."
					+ ClusterHashName[ClusterHashName.length-1]), clu.getCentroids());
		}

	}

	public static List<Clusterer> runConfigs(List<String> untaggedArgs,
			Map<String, String> taggedArgs, List<float[]> data) {
		List<Clusterer> runitems = new ArrayList<>();
		int i = 3;

		// List<float[]> data = TestUtil.readFile(new
		// File(untaggedArgs.get(0)));
		float variance = StatTests.varianceSample(data, .01f);

		int k = Integer.parseInt(untaggedArgs.get(1));
		RPHashObject o = new SimpleArrayReader(data, k);

		if (taggedArgs.containsKey("numprojections"))
			o.setNumProjections(Integer.parseInt(taggedArgs
					.get("numprojections")));
		if (taggedArgs.containsKey("innerdecodermultiplier"))
			o.setInnerDecoderMultiplier(Integer.parseInt(taggedArgs
					.get("innerdecodermultiplier")));
		if (taggedArgs.containsKey("numblur"))
			o.setNumBlur(Integer.parseInt(taggedArgs.get("numblur")));
		if (taggedArgs.containsKey("randomseed"))
			o.setRandomSeed(Long.parseLong(taggedArgs.get("randomseed")));
		if (taggedArgs.containsKey("hashmod"))
			o.setHashMod(Long.parseLong(taggedArgs.get("hashmod")));

		if (taggedArgs.containsKey("decodertype")) {
			switch (taggedArgs.get("decodertype").toLowerCase()) {
				case "dn":
					o.setDecoderType(new Dn(o.getInnerDecoderMultiplier()));
					break;
				case "e8":
					o.setDecoderType(new E8(variance));
					break;
				case "multie8":
					o.setDecoderType(new MultiDecoder(
							o.getInnerDecoderMultiplier()*8, new E8(variance)));
					break;
				case "leech":
					o.setDecoderType(new Leech(variance));
					break;
				case "multileech":
					o.setDecoderType(new MultiDecoder(
							o.getInnerDecoderMultiplier()*24, new Leech(variance)));
					break;
				case "pstable":
					o.setDecoderType(new PStableDistribution(variance));
					break;
				case "sphere": {
					o.setDecoderType(new Spherical(32,3,4));
					break;
				}
				default: {
					System.out.println(taggedArgs.get("decodertype")
							+ " decoder does not exist");
					o.setDecoderType(null);
				}
			}
		}

		while (i < untaggedArgs.size()) {
			switch (untaggedArgs.get(i).toLowerCase()) {
				case "simple":
					runitems.add(new RPHashSimple(o));
					break;
				case "streaming":
					runitems.add(new RPHashStream(o));
					break;
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
				case "streamingkmeans":
					runitems.add(new StreamingKmeans(data, k));
					break;
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

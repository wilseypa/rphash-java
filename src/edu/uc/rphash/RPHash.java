package edu.uc.rphash;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.uc.rphash.Readers.RPHashObject;
import edu.uc.rphash.Readers.SimpleArrayReader;
import edu.uc.rphash.decoders.E8;
import edu.uc.rphash.decoders.Leech;
import edu.uc.rphash.decoders.MultiDecoder;
import edu.uc.rphash.decoders.PStableDistribution;
import edu.uc.rphash.tests.Kmeans;
import edu.uc.rphash.tests.StatTests;
import edu.uc.rphash.tests.TestUtil;

public class RPHash {

	static String[] rphashes = { "simple", "3stage", "mproj", "mprobe", "redux" };
	static String[] ops = { "NumProjections", "InnerDecoderMultiplier", "NumBlur",
			"RandomSeed", "Hashmod", "DecoderType" };
	static String[] decoders = {"Dn","E8","MultiE8","Leech","MultiLeech","PStable","Sphere"};
	public static void main(String[] args) {

		if (args.length < 3) {
			System.out.print("Usage: rphash InputFile k OutputFile [");
			for(String s:rphashes)System.out.print(s+" ,");
			System.out.print("] [args]\nargs:\n");
			
			for(String s:ops)
				System.out.println("\t\t"+s);
			System.out.print("\t\t\t[");
			for(String s:decoders)System.out.print(s+" ,");
			System.out.print("]\n");
			
			System.exit(0);
		}

		List<float[]> data = TestUtil.readFile(new File(args[0]));
		int k = Integer.parseInt(args[1]);
		String outputFile = args[2];
		if (args.length == 3) {

			RPHashSimple clusterer = new RPHashSimple(data, k);
			TestUtil.writeFile(new File(outputFile), clusterer.getCentroids());
		}
		Map<String, String> taggedArgs = argsUI(args);
		List<Clusterer> runs = runConfigs(args, taggedArgs);
		runner(runs, outputFile);
	}

	public static void runner(List<Clusterer> runitems, String outputFile) {
		for (Clusterer clu : runitems) {
			System.out.print(Clusterer.class.getName() + " processing time : ");
			long startTime = System.nanoTime();
			clu.getCentroids();
			System.out.println((System.nanoTime() - startTime) / 1000000000f);
			TestUtil.writeFile(
					new File(outputFile + Clusterer.class.getName()),
					clu.getCentroids());
		}

	}

	public static List<Clusterer> runConfigs(String[] untaggedArgs,
			Map<String, String> taggedArgs) {
		List<Clusterer> runitems = new ArrayList<>();

		int i = 3;

		List<float[]> data = TestUtil.readFile(new File(untaggedArgs[0]));
		float variance = StatTests.varianceSample(data, .01f);
		
		int k = Integer.parseInt(untaggedArgs[1]);
		RPHashObject o = new SimpleArrayReader(data, k);


		if (taggedArgs.containsKey("NumProjections"))
			o.setNumProjections(Integer.parseInt(taggedArgs
					.get("NumProjections")));
		if (taggedArgs.containsKey("InnerDecoderMultiplier"))
			o.setInnerDecoderMultiplier(Integer.parseInt(taggedArgs
					.get("InnerDecoderMultiplier")));
		if (taggedArgs.containsKey("NumBlur"))
			o.setNumBlur(Integer.parseInt(taggedArgs.get("NumBlur")));
		if (taggedArgs.containsKey("RandomSeed"))
			o.setRandomSeed(Long.parseLong(taggedArgs.get("RandomSeed")));
		if (taggedArgs.containsKey("HashMod"))
			o.setHashMod(Long.parseLong(taggedArgs.get("HashMod")));
		if (taggedArgs.containsKey("DecoderType")) {
			switch (taggedArgs.get("DecoderType")) {
				case "E8":
					o.setDecoderType(new E8(variance));
				case "MultiE8":
					o.setDecoderType(new MultiDecoder(
							o.getInnerDecoderMultiplier(), new E8(variance)));
				case "Leech":
					o.setDecoderType(new Leech(variance));
				case "MultiLeech":
					o.setDecoderType(new MultiDecoder(
							o.getInnerDecoderMultiplier(), new Leech(variance)));
				case "PStable":
					o.setDecoderType(new PStableDistribution(variance));
				case "Sphere": {
					System.out.println(taggedArgs.get("DecoderType")
							+ " decoder does not exist yet");
					o.setDecoderType(null);
				}
				default: {
					System.out.println(taggedArgs.get("DecoderType")
							+ " decoder does not exist");
					o.setDecoderType(null);
				}
			}
		}

		while (i < untaggedArgs.length) {
			switch (untaggedArgs[i]) {
			case "simple":
				runitems.add(new RPHashSimple(o));
				break;
			case "3stage":
				runitems.add(new RPHash3Stage(o));
				break;
			case "multiRP":
				runitems.add(new RPHashConsensusRP(o));
				break;
			case "multiProj":
				runitems.add(new RPHashMultiProj(data, k));
				break;
			case "redux":
				runitems.add(new RPHashIterativeRedux(data, k));
				break;
			case "kmeans":
				runitems.add(new Kmeans(k, data));
				break;
			case "pkmeans":
				runitems.add(new Kmeans(k, data, o.getNumProjections()));
				break;
			default:
				System.out.println(untaggedArgs[i] + " does not exist");
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
	public static Map<String, String> argsUI(String[] args) {

		List<String> ret = new ArrayList<String>();
		Map<String, String> cmdMap = new HashMap<String, String>();
		for (String s : args) {
			String[] cmd = s.split("=");
			if (cmd.length > 1)
				cmdMap.put(cmd[0].toLowerCase(), cmd[1]);
			else
				ret.add(s);
		}

		args = (String[]) ret.toArray();
		return cmdMap;
	}

}

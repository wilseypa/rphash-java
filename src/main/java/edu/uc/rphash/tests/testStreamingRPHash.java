package edu.uc.rphash.tests;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import edu.uc.rphash.RPHashStream;
import edu.uc.rphash.StreamClusterer;
import edu.uc.rphash.tests.clusterers.StreamingKmeans;
import edu.uc.rphash.tests.generators.GenerateStreamData;
import edu.uc.rphash.util.VectorUtil;

public class testStreamingRPHash {
	public static void readFileData(String[] args) throws Exception {

		int interval = 1000;
		int k = 10;
		String filename = "/home/lee/Desktop/Dimension3204/data.mat";
		int processors = Runtime.getRuntime().availableProcessors();
		if (args.length > 1)
			filename = args[0];
		if (args.length > 2)
			k = Integer.parseInt(args[1]);
		if (args.length > 3)
			processors = Integer.parseInt(args[0]);

		Runtime rt = Runtime.getRuntime();
		List<float[]> data = VectorUtil.readFile(filename, false);

		RPHashStream rphit = new RPHashStream(data, k,processors!=1);

		// System.out.printf("Running Streaming RPHash on %d processors, d=%d,k=%d,n=%d\n",rphit.getProcessors(),d,k,interval);
		// StreamClusterer rphit = new StreamingKmeans(data, k);
		// System.out.printf("Running Streaming KMeans on %d processors, d=%d,k=%d\n",1,data.size(),k);

		System.out.printf("Vecs\tMem(KB)\tTime\tWCSSE\n");
		long timestart = System.nanoTime();

		timestart = System.nanoTime();
		rphit.addVectorOnlineStep(data.get(0));
		for (int i = 1; i < 20000; i++) {
			rphit.addVectorOnlineStep(data.get(i));

			if (i % interval == 0) {
				List<float[]> cents = rphit.getCentroidsOfflineStep();
				long time = System.nanoTime() - timestart;

				rt.gc();
				long usedkB = (rt.totalMemory() - rt.freeMemory()) / 1024;

				double wcsse = StatTests.WCSSE(cents, data);

				System.gc();
				System.out.printf("%d\t%d\t%.4f\t%.0f\n", i, usedkB,
						time / 1000000000f, wcsse);
				timestart = System.nanoTime();
			}
		}

	}

	public static void generateAndStream() {

		int k = 10;
		int d = 1000;
		float var = 1f;
		int interval = 10000;
		Runtime rt = Runtime.getRuntime();

		GenerateStreamData gen1 = new GenerateStreamData(k, d, var, 11331313);
		GenerateStreamData noise = new GenerateStreamData(1, d, var*10, 11331313);
		RPHashStream rphit = new RPHashStream(k, gen1,4);

		System.out.printf("Vecs\tMem(KB)\tTime\tWCSSE\tRealWCSSE\n");
		
		long timestart = System.nanoTime();
		for (int i = 0; i < 2500000;) {
			ArrayList<float[]> vecsAndNoiseInThisRound = new ArrayList<float[]>();
			ArrayList<float[]> justvecsInThisRound = new ArrayList<float[]>();
			
			for (int j = 1; j < interval && i < 2500000; i++, j++){
				float[] vec = gen1.generateNext();
				vecsAndNoiseInThisRound.add(vec);
				justvecsInThisRound.add(vec);
				vecsAndNoiseInThisRound.add(noise.generateNext());
			}

			timestart = System.nanoTime();
			for (float[] f : vecsAndNoiseInThisRound) {
				rphit.addVectorOnlineStep(f);
			}

			List<float[]> cents = rphit.getCentroidsOfflineStep();
			long time = System.nanoTime() - timestart;

			rt.gc();
			long usedkB = (rt.totalMemory() - rt.freeMemory()) / 1024;

			double wcsse = StatTests.WCSSE(cents, justvecsInThisRound);
			double realwcsse = StatTests.WCSSE(gen1.medoids, justvecsInThisRound);
			
//			double realwcsse = WCSSE(gen1.medoids, justvecsInThisRound);

			// recreate vectors at execution time to check average
			System.gc();
			System.out.printf("%d\t%d\t%.4f\t%.1f\t%.1f\n", i, usedkB,
					time / 1000000000f, wcsse, realwcsse);
		}
	}

	public static void streamingPushtest() {
		int k = 10;
		int d = 1000;
		float var = 1f;

		GenerateStreamData gen1 = new GenerateStreamData(k, d, var, 11331313);

		RPHashStream rphit = new RPHashStream(k,gen1);

		ArrayList<Integer> cts = new ArrayList<Integer>();
		for (int i = 0; i < 10000; i++) {
			long centroidCount = rphit.addVectorOnlineStep(gen1.generateNext());
//			if (centroidCount>1 ) {
//			cts.add((int) centroidCount);
//				List<Float> f = rphit.getTopIdSizes();
//				for (float ff : f)
//					System.out.print(ff/(float)i + ",");
//				System.out.print("]\n[");
//			}
		}
		//System.out.println(cts.toString());
	}

	public static void main(String[] args) throws Exception {
//		readFileData(args);
		generateAndStream();
//		streamingPushtest();
	}

}

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
		int k = 20;
		int d = 1000;
		float var = 1f;

		Runtime rt = Runtime.getRuntime();
		int processors = rt.availableProcessors();

		GenerateStreamData gen1 = new GenerateStreamData(k, d, var, 11331313);

		ArrayList<float[]> vecsInThisRound = new ArrayList<float[]>();
		int interval = 10000;

		RPHashStream srphash = new RPHashStream(k, gen1, processors);

		StreamClusterer skmeans = new StreamingKmeans(k, gen1);

		Random noiseDataRandomSrc = new Random();

		System.out.printf("Vecs\tMem(KB)\tTime\tWCSSE\tCentSSE\n");
		long timestart = System.nanoTime();
		for (int i = 0; i < 2500000; i++) {

//			if (i % 10 == 0) {
//				float[] noi = new float[d];
//				for (int j = 0; j < d; j++)
//					noi[j] = (noiseDataRandomSrc.nextFloat()) * 2.0f - 1.0f;
//				vecsInThisRound.add(noi);
//			}

			vecsInThisRound.add(gen1.generateNext());

			if (i % interval == interval - 1) {

				timestart = System.nanoTime();
				for (float[] f : vecsInThisRound) {
					srphash.addVectorOnlineStep(f);
				}

				List<float[]> cents = srphash.getCentroidsOfflineStep();

				long time = System.nanoTime() - timestart;

				rt.gc();
				long usedkB = (rt.totalMemory() - rt.freeMemory()) / 1024;

				List<float[]> aligned = VectorUtil.alignCentroids(cents,
						gen1.getMedoids());
				double wcsse = StatTests.WCSSE(cents, vecsInThisRound);
				double ssecent = StatTests.SSE(aligned, gen1);

				// recreate vectors at execution time to check average
				System.gc();
				System.out.printf("%d\t%d\t%.4f\t%.0f\t%.3f\t", i, usedkB,
						time / 1000000000f, wcsse, ssecent);

//				timestart = System.nanoTime();
//				for (float[] f : vecsInThisRound) {
//					skmeans.addVectorOnlineStep(f);
//				}
//
//				cents = skmeans.getCentroidsOfflineStep();
//				time = System.nanoTime() - timestart;
//
//				rt.gc();
//				usedkB = (rt.totalMemory() - rt.freeMemory()) / 1024;
//
//				aligned = VectorUtil.alignCentroids(cents, gen1.getMedoids());
//				wcsse = StatTests.WCSSE(cents, vecsInThisRound);
//				ssecent = StatTests.SSE(aligned, gen1);
//
//				System.gc();
//				System.out.printf("%d\t%d\t%.4f\t%.0f\t%.3f\t", i, usedkB,
//						time / 1000000000f, wcsse, ssecent);

				System.out.printf("\n");
				vecsInThisRound = new ArrayList<float[]>();
			}
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

package edu.uc.rphash.tests;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Consumer;

import edu.uc.rphash.RPHashAdaptive2Pass;
import edu.uc.rphash.RPHashAdaptive2PassParallel;
import edu.uc.rphash.RPHashSimple;
import edu.uc.rphash.RPHashSimpleParallel;
import edu.uc.rphash.RPHashStream;
import edu.uc.rphash.tests.generators.GenerateStreamData;

public class ScalabilityTest {

	public static long rphashstream(ArrayList<float[]> vecsAndNoiseInThisRound,
			int i, int k, GenerateStreamData gen1) {
		long timestart = System.nanoTime();
		RPHashStream rphit = new RPHashStream(k, gen1, i);
		rphit.getParam().setParallel(false);

		// usually we would want to call addvectoronline,
		// fork join pools may be more efficient when all
		// the data is available
		try {
			ForkJoinPool myPool = new ForkJoinPool(i);
			myPool.submit(
					() -> vecsAndNoiseInThisRound.parallelStream().forEach(
							new Consumer<float[]>() {

								@Override
								public void accept(float[] t) {
									rphit.addVectorOnlineStep(t);
								}

							})).get();

		} catch (InterruptedException | ExecutionException e) {
			e.printStackTrace();
		}
		return System.nanoTime() - timestart;

	}

	public static long rphashsimple(ArrayList<float[]> vecsAndNoiseInThisRound,
			int i, int k) {
		long timestart = System.nanoTime();
		RPHashSimpleParallel rphit = new RPHashSimpleParallel(vecsAndNoiseInThisRound, k, i);
		rphit.map();
		rphit.reduce();
		return System.nanoTime() - timestart;
	}

	public static long rphashadaptive(
			ArrayList<float[]> vecsAndNoiseInThisRound, int i, int k) {
		long timestart = System.nanoTime();
		RPHashAdaptive2PassParallel rphit = new RPHashAdaptive2PassParallel(
				vecsAndNoiseInThisRound, k, i);
		rphit.run();
		return System.nanoTime() - timestart;
	}

	public static void scalability(int n) {
		int k = 10;
		int d = 1000;
		float var = 1f;
		Runtime rt = Runtime.getRuntime();
		Random r = new Random();
		int NUM_Procs = rt.availableProcessors();

		GenerateStreamData gen1 = new GenerateStreamData(k, d, var, 11331313);
		GenerateStreamData noise = new GenerateStreamData(1, d, var * 10,
				11331313);
		ArrayList<float[]> vecsAndNoiseInThisRound = new ArrayList<float[]>();

		for (int j = 1; j < n; j++) {
			float[] vec = gen1.generateNext();
			vecsAndNoiseInThisRound.add(vec);
			if (r.nextInt(10) == 1)
				vecsAndNoiseInThisRound.add(noise.generateNext());
		}

		System.out.printf("Threads\tSimple\tStream\tAdaptive\n");

		long timesimple=0, timeadaptive=0, timestream=0;

		for (int i = 1; i <= NUM_Procs; i++) {

			try {
				System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism",String.valueOf(i));
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

				timesimple = rphashsimple(vecsAndNoiseInThisRound, i, k);

				timeadaptive = rphashadaptive(vecsAndNoiseInThisRound, i, k);

				timestream = rphashstream(vecsAndNoiseInThisRound, i, k, gen1);

				System.out.printf("%d\t%.6f\t%.6f\t%.6f\n", i,
						timesimple / 1000000000f, timestream / 1000000000f,
						timeadaptive / 1000000000f);

			} catch (Exception e) {
				System.out.println("Exception at Proc:" + String.valueOf(i));
				System.out.printf("%d\t%.6f\t%.6f\t%.6f\n", i,
						timesimple / 1000000000f, timestream / 1000000000f,
						timeadaptive / 1000000000f);

			}
		}
	}

	public static void main(String[] args) throws InterruptedException {
		ScalabilityTest.scalability(Integer.parseInt(args[0]));
	}
}

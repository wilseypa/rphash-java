package edu.uc.rphash.tests;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

import edu.uc.rphash.RPHashAdaptive2PassParallel;
import edu.uc.rphash.RPHashSimpleParallel;
import edu.uc.rphash.RPHashStream;
import edu.uc.rphash.concurrent.VectorLevelConcurrency;
import edu.uc.rphash.tests.generators.GenerateStreamData;

public class ScalabilityTest {

	public static long rphashstream(ArrayList<float[]> vecsAndNoiseInThisRound,
			int i, int k, GenerateStreamData gen1) {
		RPHashStream rphit = new RPHashStream(k, gen1, i);
		long timestart = System.nanoTime();
		//vecsAndNoiseInThisRound.parallelStream().map(vec->
		//		VectorLevelConcurrency.computeSequential(vec, rphit.lshfuncs.get(0), rphit.is.get(0), rphit.getParam()));
		for (float[] v : vecsAndNoiseInThisRound)
			rphit.addVectorOnlineStep(v);
		rphit.getCentroidsOfflineStep();

		return System.nanoTime() - timestart;
	}

	public static long rphashsimple(ArrayList<float[]> vecsAndNoiseInThisRound,
			int i, int k) {
		RPHashSimpleParallel rphit = new RPHashSimpleParallel(
				vecsAndNoiseInThisRound, k, i);

		long timestart = System.nanoTime();
		rphit.mapreduce1();
		rphit.mapreduce2();
		return System.nanoTime() - timestart;
	}

	public static long rphashadaptive(
			ArrayList<float[]> vecsAndNoiseInThisRound, int i, int k) {

		RPHashAdaptive2PassParallel rphit = new RPHashAdaptive2PassParallel(
				vecsAndNoiseInThisRound, k, i);

		long timestart = System.nanoTime();
		rphit.run();
		return System.nanoTime() - timestart;
	}

	public static void scalability(int n) {
		int k = 10;
		int d = 1000;
		float var = 1f;
		Runtime rt = Runtime.getRuntime();
		// Random r = new Random();
		int NUM_Procs = rt.availableProcessors();

		GenerateStreamData gen1 = new GenerateStreamData(k, d, var, 11331313);

		ArrayList<float[]> vecsAndNoiseInThisRound = new ArrayList<float[]>(n);

		// generate data in parallel
		vecsAndNoiseInThisRound = gen1.genParallel(n);
		
		System.out.println(vecsAndNoiseInThisRound.size());
		System.out.printf("Threads\tSimple\tStream\tAdaptive\n");

		long timesimple = 0, timeadaptive = 0, timestream = 0;

		for (int i = 1; i <= NUM_Procs; i++) {

			try {
				//mix up the order
				if(i%3==0){
					System.gc();
					Thread.sleep(1000);
					timesimple = rphashsimple(vecsAndNoiseInThisRound, i, k);
					System.gc();
					Thread.sleep(1000);
					timeadaptive = rphashadaptive(vecsAndNoiseInThisRound, i, k);
					System.gc();
					Thread.sleep(1000);
					timestream = rphashstream(vecsAndNoiseInThisRound, i, k, gen1);
				}
				
				if(i%3==1){
					System.gc();
					Thread.sleep(1000);
					timeadaptive = rphashadaptive(vecsAndNoiseInThisRound, i, k);
					System.gc();
					Thread.sleep(1000);
					timesimple = rphashsimple(vecsAndNoiseInThisRound, i, k);
					System.gc();
					Thread.sleep(1000);
					timestream = rphashstream(vecsAndNoiseInThisRound, i, k, gen1);
				}
				
				if(i%3==2){
					System.gc();
					Thread.sleep(1000);
					timesimple = rphashsimple(vecsAndNoiseInThisRound, i, k);
					System.gc();
					Thread.sleep(1000);
					timestream = rphashstream(vecsAndNoiseInThisRound, i, k, gen1);
					System.gc();
					Thread.sleep(1000);
					timeadaptive = rphashadaptive(vecsAndNoiseInThisRound, i, k);
				}
				
				System.out.printf("%d\t%.6f\t%.6f\t%.6f\n", i,
						timesimple / 1e9f, timestream / 1e9f,
						timeadaptive / 1e9f);

			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Exception at Proc:" + String.valueOf(i));
				System.out.printf("%d\t%.6f\t%.6f\t%.6f\n", i,
						timesimple / 1e9f, timestream / 1e9f,
						timeadaptive / 1e9f);

			}
		}
	}

	public static void main(String[] args) throws InterruptedException {
		ScalabilityTest.scalability(Integer.parseInt(args[0]));
		
	}
}

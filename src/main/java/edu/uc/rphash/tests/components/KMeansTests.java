package edu.uc.rphash.tests.components;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import edu.uc.rphash.Centroid;
import edu.uc.rphash.tests.StatTests;
import edu.uc.rphash.tests.clusterers.KMeans2;
import edu.uc.rphash.tests.clusterers.KMeans2NoWCSS;
import edu.uc.rphash.tests.generators.GenerateStreamData;
import edu.uc.rphash.util.VectorUtil;

public class KMeansTests {

	public static void main(String[] args) {
//		testkm2();
		wcsstrack();
	}

	private static void testmerger() {
		float[] x_1 = { 55.085215700553867f, 57.422460638012225f,
				57.572855373777195f, 56.483903938427062f, 58.427244931281827f };
		float[] var_1 = { 890.10853249058232f, 739.96201001558484f,
				825.3038578897914f, 875.91938138515559f, 830.95191282523501f };
		float cnt_1 = 4611;
		float[] x_2 = { 28.24013827f, 28.41069007f, 25.01421101f, 18.6368656f,
				33.49266143f };
		float[] var_2 = { 0, 0, 0, 0, 0 };
		float cnt_2 = 15;

		float[][] ret = Centroid.merge(cnt_1, x_1, var_1, cnt_2, x_2, var_2);
		VectorUtil.prettyPrint(ret[0]);
		System.out.println();
		VectorUtil.prettyPrint(ret[1]);
		System.out.println();
		VectorUtil.prettyPrint(ret[2]);
		System.out.println();

		for (int k = 0; k < 500; k++) {
			List<Centroid> cs = new ArrayList<Centroid>();
			List<double[]> csfull = new ArrayList<double[]>();

			Random r = new Random();
			int n = 200;
			int d = 8;
			for (int i = 0; i < n; i++) {
				double[] randvecd = new double[d];
				float[] randvecf = new float[d];

				for (int j = 0; j < d; j++) {
					randvecf[j] = (float) r.nextFloat()
							* (float) r.nextGaussian() + r.nextInt(20);
					randvecd[j] = randvecf[j];
				}

				int ct = new Random().nextInt(10) + 1;
				Centroid tmp = new Centroid(randvecf, 0);
				tmp.setCount(ct);
				cs.add(tmp);

				// add a bunch of times
				for (int j = 0; j < ct; j++)
					csfull.add(randvecd);
			}
			float wcsstot = 0.0f;
			float[] wcss = KMeans2.computemeanAndWCSS(cs)[2];
			for (int i = 0; i < wcss.length; i++)
				wcsstot += wcss[i];
			System.out.print(wcsstot + "\t");
			wcsstot = 0.0f;
			double[] wcss2 = StatTests.WCSS(csfull);
			for (int i = 0; i < wcss2.length; i++)
				wcsstot += wcss2[i];
			System.out.print(wcsstot + "\n");
		}

	}
	
	private static void wcsstrack() {
		for(int i = 2;i<100;i++){
			int k = 3;
			int d = i;
			float var = 1.1f;
	
			GenerateStreamData gen1 = new GenerateStreamData(k, d, var, 11331313);
			List<float[]> vecsAndNoiseInThisRound = new ArrayList<float[]>();
			for (int j = 0; j < 10000;j++) {
				vecsAndNoiseInThisRound.add(gen1.generateNext());
			}
			KMeans2NoWCSS kmwcss = new KMeans2NoWCSS();
			kmwcss.setRawData(vecsAndNoiseInThisRound);
			kmwcss.setK(k);
			kmwcss.setMultiRun(10);
			List<Centroid> cents = kmwcss.getCentroids();
			
			double wcsse = StatTests.WCSSECentroidsFloat(cents, vecsAndNoiseInThisRound);
			
			float wcsseacc = 0.0f;
			for(Centroid c : cents){
				for(float f: c.getWCSS())wcsseacc+=f;
			}
			
			System.out.printf("%f\t%f\n",wcsse,wcsseacc);
			
		}
	}
	

	private static void testkm2() {

		int k = 3;
		int d = 6;
		float var = 3.1f;
		int interval = 10000;
		Runtime rt = Runtime.getRuntime();

		GenerateStreamData gen1 = new GenerateStreamData(k, d, var, 11331313);
		GenerateStreamData noise = new GenerateStreamData(1, d, var * 10,
				11331313);
		KMeans2NoWCSS kmwcss = new KMeans2NoWCSS();
		KMeans2 km = new KMeans2();

		System.out.printf("KMWCSS\t\t\t\t\tKM\t\t\tReal\n");
		System.out
				.printf("Vecs\tMem(KB)\tTime\tWCSSE\t\tTime\tWCSSE\t\tWCSSE\n");

		long time, usedkB;
		List<Centroid> cents;
		double wcsse;

		long timestart = System.nanoTime();
		for (int i = 0; i < 2500000;) {
			ArrayList<float[]> vecsAndNoiseInThisRound = new ArrayList<float[]>();
			ArrayList<float[]> justvecsInThisRound = new ArrayList<float[]>();

			for (int j = 1; j < interval && i < 2500000; i++, j++) {
				float[] vec = gen1.generateNext();
				vecsAndNoiseInThisRound.add(vec);
				justvecsInThisRound.add(vec);
				vecsAndNoiseInThisRound.add(noise.generateNext());
			}

			System.out.printf("\n Real \n");
			VectorUtil.prettyPrint(gen1.medoids);
			
			timestart = System.nanoTime();
			kmwcss.setRawData(vecsAndNoiseInThisRound);
			kmwcss.setK(k);
			kmwcss.setMultiRun(10);
			cents = kmwcss.getCentroids();
			time = System.nanoTime() - timestart;
			rt.gc();
			usedkB = (rt.totalMemory() - rt.freeMemory()) / 1024;

			wcsse = StatTests.WCSSECentroidsFloat(cents, justvecsInThisRound);
			
			float wcsseacc = 0.0f;
			for(Centroid c : cents){
				for(float f: c.getWCSS())wcsseacc+=f;
			}
			
			System.out.printf("%f\t%f\n",wcsse,wcsseacc);
			System.out.printf("\n KMWCSS\n");
			prettyPrint(cents);
			
			System.out.printf("%d\t%d\t%.4f\t%.1f\t\t", i, usedkB,
					time / 1000000000f, wcsse);

			timestart = System.nanoTime();
			km.setRawData(vecsAndNoiseInThisRound);
			km.setK(k);
			km.setMultiRun(10);
			cents = km.getCentroids();
			time = System.nanoTime() - timestart;
			rt.gc();
			usedkB = (rt.totalMemory() - rt.freeMemory()) / 1024;

			wcsse = StatTests.WCSSECentroidsFloat(cents, justvecsInThisRound);

			System.out.printf("%.4f\t%.1f\t\t%.1f\n", time / 1000000000f,
					wcsse, StatTests.WCSSE(gen1.medoids, justvecsInThisRound));
			System.out.printf("\n KM\n");
			prettyPrint(cents);
			System.out.printf("\n");
		}
	}
	
	 static void prettyPrint(List<Centroid> cs){

			int n = cs.get(0).centroid.length;
			boolean curtailm = n > 10;
			if (curtailm) {
				for (int i = 0; i < 4; i++) {
					VectorUtil.prettyPrint(cs.get(i).centroid);
				}
				for (int j = 0; j < n / 2; j++)
					System.out.print("\t");
				System.out.print(" ...\n");
				for (int i = cs.size() - 4; i < cs.size(); i++) {
					VectorUtil.prettyPrint(cs.get(i).centroid);
				}
			} else {
				for (int i = 0; i < cs.size(); i++) {
					VectorUtil.prettyPrint(cs.get(i).centroid);
					System.out.print("\n");
				}
			}
		
	}

}

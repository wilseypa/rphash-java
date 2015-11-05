package edu.uc.rphash;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import java.util.concurrent.TimeUnit;

import edu.uc.rphash.Readers.RPHashObject;
import edu.uc.rphash.Readers.SimpleArrayReader;
import edu.uc.rphash.concurrent.VectorLevelConcurrency;
import edu.uc.rphash.decoders.Decoder;
import edu.uc.rphash.frequentItemSet.KHHCentroidCounter;
//import edu.uc.rphash.frequentItemSet.KHHCountMinSketch.Tuple;
import edu.uc.rphash.lsh.LSH;
import edu.uc.rphash.projections.DBFriendlyProjection;
import edu.uc.rphash.projections.Projector;
import edu.uc.rphash.standardhash.HashAlgorithm;
import edu.uc.rphash.standardhash.MurmurHash;
import edu.uc.rphash.tests.ClusterGenerator;
import edu.uc.rphash.tests.GenerateData;
import edu.uc.rphash.tests.GenerateStreamData;
import edu.uc.rphash.tests.Kmeans;
import edu.uc.rphash.tests.StatTests;
import edu.uc.rphash.tests.StreamingKmeans;
import edu.uc.rphash.tests.TestUtil;

public class RPHashStream implements StreamClusterer {
	private float variance;
	public KHHCentroidCounter is;
	private LSH[] lshfuncs;
	private StatTests vartracker;
	private List<float[]> centroids = null;
	private RPHashObject so;
	private boolean parallel = true;
	ExecutorService executor;

	@Override
	public synchronized long addVectorOnlineStep(final float[] vec) {

		if (parallel) {
			VectorLevelConcurrency r = new VectorLevelConcurrency(vec, so, lshfuncs,
					vartracker, variance, is);
			executor.execute(r);
			return is.count;
		}

		long hash[];
		Centroid c = new Centroid(vec);
		float tmpvar = vartracker.updateVarianceSample(vec);
		if (variance != tmpvar) {
			for (LSH lshfunc : lshfuncs)
				lshfunc.updateDecoderVariance(tmpvar);
			variance = tmpvar;
		}
		for (LSH lshfunc : lshfuncs) {
			hash = lshfunc.lshHashRadiusNo2Hash(vec, so.getNumBlur());
			for (long h : hash)
				c.addID(h);
		}
		is.add(c);
		return is.count;
	}

	public void init() {

		Random r = new Random(so.getRandomSeed());
		this.vartracker = new StatTests(.01);
		int projections = so.getNumProjections();
		int k = (int) (so.getk() * projections);

		// initialize our counter
		float decayrate = .001f;// bottom number is window size
		is = new KHHCentroidCounter(k, decayrate);

		// create LSH Device
		lshfuncs = new LSH[projections];
		Decoder dec = so.getDecoderType();
		HashAlgorithm hal = new MurmurHash(so.getHashmod());
		// create projection matrices add to LSH Device
		for (int i = 0; i < projections; i++) {
			Projector p = new DBFriendlyProjection(so.getdim(),
					dec.getDimensionality(), r.nextLong());
			List<float[]> noise = LSH.genNoiseTable(dec.getDimensionality(), 1,
					r, dec.getErrorRadius() / dec.getDimensionality());
			lshfuncs[i] = new LSH(dec, p, hal, noise);
		}

		if (parallel == true)
			executor = Executors.newFixedThreadPool(Runtime.getRuntime()
					.availableProcessors());

	}

	public RPHashStream(int k, ClusterGenerator c) {

		so = new SimpleArrayReader(k, c);
		init();
	}

	public RPHashStream(List<float[]> data, int k) {

		variance = StatTests.varianceSample(data, .01f);
		so = new SimpleArrayReader(data, k);
		init();
	}

	public RPHashStream(RPHashObject so) {
		this.so = so;
		init();
	}

	public List<float[]> getCentroids(RPHashObject so) {
		this.so = so;
		init();
		if (centroids == null)
			run();
		centroids = new ArrayList<float[]>();
		for (Centroid c : is.getTop())
			centroids.add(c.centroid());
		return new Kmeans(so.getk(), centroids, is.getCounts()).getCentroids();
	}

	@Override
	public List<float[]> getCentroids() {
		if (centroids == null) {
			init();
			run();
			centroids = new ArrayList<float[]>();
			for (Centroid cent : is.getTop())
				centroids.add(cent.centroid());
			centroids = new Kmeans(so.getk(), centroids, is.getCounts())
					.getCentroids();
		}
		return centroids;
	}

	ArrayList<Float> counts;

	public List<float[]> getCentroidsOfflineStep() {

		centroids = new ArrayList<float[]>();
		counts = new ArrayList<Float>();

		for (int i = 0; i < is.getTop().size(); i++) {
			centroids.add(is.getTop().get(i).centroid());
			counts.add(is.getCounts().get(i));
		}

		centroids = new Kmeans(so.getk(), centroids, counts).getCentroids();

		int count = (int) ((Collections.max(counts) + Collections.min(counts)) / 2);
		counts = new ArrayList<Float>();
		for (int i = 0; i < so.getk(); i++)
			counts.add((float) count);
		return centroids;
	}

	public void run() {
		// add to frequent itemset the hashed Decoded randomly projected vector
		Iterator<float[]> vecs = so.getVectorIterator();

		while (vecs.hasNext()) {
			if (parallel) {
				executor.execute(new VectorLevelConcurrency(vecs.next(), so,
						lshfuncs, vartracker, variance, is));
			} else {
				addVectorOnlineStep(vecs.next());
			}
		}

	}

	public List<Float> getTopIdSizes() {
		return is.getCounts();
	}

	public static void main(String[] args) throws Exception {

		int k = 10;
		int d = 5000;
		int n = 50000;
		float var = .75f;
		// for (float f = (float) d; f < 100000f; f *= 1.5f) {
		// for (int i = 0; i < 1; i++) {
		// GenerateData gen = new GenerateData(k, n / k, (int) f, var,
		// true, 1f);
		// // StreamingKmeans rphit = new StreamingKmeans(gen.data(), k);
		// RPHashStream rphit = new RPHashStream(gen.getData(), k);
		// long startTime = System.nanoTime();
		// rphit.getCentroids();
		// if (rphit.parallel) {
		// rphit.executor.shutdown();
		// try {
		// rphit.executor.awaitTermination(2, TimeUnit.MINUTES);
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }
		// }
		//
		// long duration = (System.nanoTime() - startTime);
		//
		// List<float[]> aligned = TestUtil.alignCentroids(
		// rphit.getCentroids(), gen.medoids());
		// System.out.println(f + ":" + StatTests.PR(aligned, gen) + ":"
		// + StatTests.WCSSE(gen.medoids(), gen.getData()) + ":"
		// + StatTests.WCSSE(aligned, gen.getData()) + ":"
		// + duration / 1000000000f);
		// System.gc();
		// }
		// }

		Runtime rt = Runtime.getRuntime();
		GenerateStreamData gen = new GenerateStreamData(k, d, var, 25l);
		StreamingKmeans rphit = new  StreamingKmeans(k,gen); //RPHashStream(k,	gen);

		ArrayList<float[]> vecsInThisRound = new ArrayList<float[]>();
		
		
		
		long gentime = System.nanoTime();
		int interval = 50000;
		
		for (int i = 0; i < interval; i++) {
			float[] f = gen.generateNext();
			vecsInThisRound.add(f);
			vecsInThisRound = new ArrayList<float[]>();
		}
		gentime = (System.nanoTime() - gentime);
		System.out.println("Average Vector Generation Time for Interval: "+gentime/ 1000000000f);
		
		System.out.printf("Vecs\tMem(KB)\tTime\tWCSSE\tCentSSE\n");
		long timestart = System.nanoTime();
		for (int i = 0; i < 8000000; i++) {
			
			float[] f = gen.generateNext();
			rphit.addVectorOnlineStep(f);
			vecsInThisRound.add(f);
			if (i % interval == interval - 1) {

				if (rphit.parallel) {
					rphit.executor.shutdown();
					try {
						rphit.executor.awaitTermination(2, TimeUnit.MINUTES);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					rphit.executor = Executors.newFixedThreadPool(Runtime
							.getRuntime().availableProcessors());
				}

				List<float[]> cents = rphit.getCentroidsOfflineStep();
				long time = System.nanoTime() - timestart;
				
				rt.gc();
				Thread.sleep(100);
				rt.gc();
				long usedkB = (rt.totalMemory() - rt.freeMemory()) / 1024;
				
				List<float[]> aligned = TestUtil.alignCentroids(cents,
						gen.getMedoids());
				double wcsse = StatTests.WCSSE(cents, vecsInThisRound);
				double ssecent = StatTests.SSE(aligned, gen);
				
				vecsInThisRound = new ArrayList<float[]>();
				System.out.printf("%d\t%d\t%.3f\t%.0f\t%.3f\n", i,
						usedkB, (time) / 1000000000f, wcsse, ssecent);

//				gentime = System.nanoTime();
//				for (int b = 0; b < interval; b++) {
//					f = gen.generateNext();
//					vecsInThisRound.add(f);
//					vecsInThisRound = new ArrayList<float[]>();
//				}
//				gentime = (System.nanoTime() - gentime);
//				System.out.println("Average Vector Generation Time for Interval: "+gentime/ 1000000000f);
//				vecsInThisRound = new ArrayList<float[]>();
				
				timestart = System.nanoTime();
			}
		}
	}

	@Override
	public RPHashObject getParam() {
		return so;
	}

}

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
	private final int processors;

	public int getProcessors() {
		return processors;
	}

	@Override
	public synchronized long addVectorOnlineStep(final float[] vec) {

		if (parallel) {
			VectorLevelConcurrency r = new VectorLevelConcurrency(vec, so,
					lshfuncs, vartracker, variance, is);
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
		this.vartracker = new StatTests(.01f);
		int projections = so.getNumProjections();
		int k = (int) (so.getk() * projections);

		// initialize our counter
		float decayrate = .001f;// bottom number is window size
		is = new KHHCentroidCounter(k);// , decayrate); //add back for decayed
										// counter

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

	}

	public RPHashStream(int k, ClusterGenerator c) {

		so = new SimpleArrayReader(k, c);
		if(parallel)this.processors = Runtime.getRuntime().availableProcessors();
		else this.processors = 1;
		executor = Executors.newFixedThreadPool(this.processors);

		init();
	}

	public RPHashStream(List<float[]> data, int k) {

		variance = StatTests.varianceSample(data, .01f);
		so = new SimpleArrayReader(data, k);
		
		if(parallel)this.processors = Runtime.getRuntime().availableProcessors();
		else this.processors = 1;
		executor = Executors.newFixedThreadPool(this.processors);
		
		init();
	}

	public RPHashStream(RPHashObject so) {
		this.so = so;
		
		if(parallel)this.processors = Runtime.getRuntime().availableProcessors();
		else this.processors = 1;
		executor = Executors.newFixedThreadPool(this.processors);
		
		init();
	}

	public RPHashStream(int k, GenerateStreamData c, int processors) {


		so = new SimpleArrayReader(k, c);
		if(parallel)this.processors = processors;
		else this.processors = 1;
		executor = Executors.newFixedThreadPool(this.processors);

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
		int d = 10000;
		float var = 1f;
		
		int processors = Runtime.getRuntime().availableProcessors();
		if(args.length>0) processors = Integer.parseInt(args[0]);
		

		Runtime rt = Runtime.getRuntime();
		GenerateStreamData gen = new GenerateStreamData(k, d, var, 25l);
		RPHashStream rphit = new RPHashStream(k, gen,processors); // StreamingKmeans(k,
														// gen);
		if(processors==1)rphit.parallel = false;

		ArrayList<float[]> vecsInThisRound = new ArrayList<float[]>();

		int interval = 50000;
		System.out.printf("Running Streaming RPHash on %d processors, d=%d,k=%d,n=%d,var=%.0f\n",rphit.getProcessors(),d,k,interval,var);
		System.out.printf("Vecs\tMem(KB)\tTime\tWCSSE\tCentSSE\n");
		long timestart = System.nanoTime();
		for (int i = 0; i < 8000000; i++) {

//			float[] f = gen.generateNext();
			//rphit.addVectorOnlineStep(f);
			vecsInThisRound.add(gen.generateNext());
			if (i % interval == interval - 1) {

//				gen.executor.shutdown();
//				gen.executor.awaitTermination(2, TimeUnit.MINUTES);
//				gen.executor = Executors.newFixedThreadPool(gen.processors);
				
				
				timestart = System.nanoTime();
				for(float[] f: vecsInThisRound)rphit.addVectorOnlineStep(f);
				
				if (rphit.parallel) {
					rphit.executor.shutdown();
					rphit.executor.awaitTermination(2, TimeUnit.MINUTES);
					rphit.executor = Executors.newFixedThreadPool(rphit.getProcessors());
				}

				
				List<float[]> cents = rphit.getCentroidsOfflineStep();
				long time = System.nanoTime() - timestart;

				rt.gc();
				long usedkB = (rt.totalMemory() - rt.freeMemory()) / 1024;

				List<float[]> aligned = TestUtil.alignCentroids(cents,
						gen.getMedoids());
				double wcsse = StatTests.WCSSE(cents, vecsInThisRound);
				double ssecent = StatTests.SSE(aligned, gen);

				
				vecsInThisRound = new ArrayList<float[]>();
				// recreate vectors at execution time to check average

				System.out.printf("%d\t%d\t%.4f\t%.0f\t%.3f\n", i,
						usedkB, time / 1000000000f, wcsse, ssecent);

				
			}
		}
	}

	@Override
	public RPHashObject getParam() {
		return so;
	}

}

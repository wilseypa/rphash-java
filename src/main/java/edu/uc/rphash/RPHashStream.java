package edu.uc.rphash;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
import edu.uc.rphash.tests.StatTests;
import edu.uc.rphash.tests.clusterers.Kmeans;
import edu.uc.rphash.tests.generators.ClusterGenerator;
import edu.uc.rphash.tests.generators.GenerateStreamData;

public class RPHashStream implements StreamClusterer {
	public KHHCentroidCounter is;
	private LSH[] lshfuncs;
	private StatTests vartracker;
	private List<float[]> centroids = null;
	private RPHashObject so;
//	public boolean parallel = true;
	ExecutorService executor;
	private final int processors;

	public int getProcessors() {
		return processors;
	}

	@Override
	public synchronized long addVectorOnlineStep(final float[] vec) {

		if (so.getParallel()) {
			VectorLevelConcurrency r = new VectorLevelConcurrency(vec,
					lshfuncs, vartracker, is,so);
			executor.execute(r);
			return is.count;
		}

		Centroid c = new Centroid(vec);
		for (LSH lshfunc : lshfuncs) {
			if (so.getNumBlur() != 1) {
				long[] hash = lshfunc
						.lshHashRadiusNo2Hash(vec, so.getNumBlur());
				for (long h : hash) {
					c.addID(h);
					is.addLong(h, 1);
				}
			} else {
				long hash = lshfunc.lshHash(vec);
				c.addID(hash);
				is.addLong(hash, 1);
			}
		}
		is.add(c);
		return is.count;
	}

	public void init() {
		System.out.println("init rphash machine");
		Random r = new Random(so.getRandomSeed());
		this.vartracker = new StatTests(.01f);
		int projections = so.getNumProjections();
		int k = (int) (so.getk() * Math.log(so.getk()));
		// initialize our counter
		float decayrate = so.getDecayRate();// 1f;// bottom number is window
											// size
		is = new KHHCentroidCounter(k, decayrate);// , decayrate); //add back
													// for decayed
		// create LSH Device
		lshfuncs = new LSH[projections];
		Decoder dec = so.getDecoderType();
		HashAlgorithm hal = new MurmurHash(so.getHashmod());
		// create projection matrices add to LSH Device
		for (int i = 0; i < projections; i++) {
			Projector p = new DBFriendlyProjection(so.getdim(),
					dec.getDimensionality(), r.nextLong());
			List<float[]> noise = LSH.genNoiseTable(dec.getDimensionality(),
					SimpleArrayReader.DEFAULT_NUM_BLUR, r, dec.getErrorRadius()
							/ dec.getDimensionality());
			lshfuncs[i] = new LSH(dec, p, hal, noise);
		}
	}

	public RPHashStream(int k, ClusterGenerator c) {
		so = new SimpleArrayReader(c, k);
		if (so.getParallel())
			this.processors = Runtime.getRuntime().availableProcessors();
		else
			this.processors = 1;
		executor = Executors.newFixedThreadPool(this.processors);

		init();
	}

	public RPHashStream(List<float[]> data, int k) {
		so = new SimpleArrayReader(data, k);
		if (so.getParallel())
			this.processors = Runtime.getRuntime().availableProcessors();
		else
			this.processors = 1;
		executor = Executors.newFixedThreadPool(this.processors);
		init();
	}
	
	public RPHashStream(List<float[]> data, int k, boolean parallel) {
		so = new SimpleArrayReader(data, k);
		so.setParallel(parallel);
		if (so.getParallel())
			this.processors = Runtime.getRuntime().availableProcessors();
		else
			this.processors = 1;
		executor = Executors.newFixedThreadPool(this.processors);
		init();
	}

	public RPHashStream(RPHashObject so) {
		this.so = so;
		if (so.getParallel())
			this.processors = Runtime.getRuntime().availableProcessors();
		else
			this.processors = 1;
		executor = Executors.newFixedThreadPool(this.processors);
		init();
	}

	public RPHashStream(int k, GenerateStreamData c, int processors) {
		so = new SimpleArrayReader(c, k);
		if (so.getParallel())
			this.processors = processors;
		else
			this.processors = 1;
		executor = Executors.newFixedThreadPool(this.processors);
		init();
	}

	@Override
	public List<float[]> getCentroids() {
		if (centroids == null) {
			init();
			run();
			getCentroidsOfflineStep();
		}
		return centroids;
	}

	public List<float[]> getCentroidsOfflineStep() {
		if (so.getParallel()) {
			executor.shutdown();
			try {
				executor.awaitTermination(10, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			executor = Executors.newFixedThreadPool(getProcessors());
		}

		centroids = new ArrayList<float[]>();
		List<Centroid> cents = is.getTop();
		List<Float> counts = is.getCounts();

		for (int i = 0; i < cents.size(); i++) {
			centroids.add(cents.get(i).centroid());
		}

		centroids = new Kmeans(so.getk(), centroids, counts).getCentroids();

		return centroids;
	}

	public void run() {
		// add to frequent itemset the hashed Decoded randomly projected vector
		Iterator<float[]> vecs = so.getVectorIterator();
		while (vecs.hasNext()) {
			if (so.getParallel()) {
				float[] vec = vecs.next();
				executor.execute(new VectorLevelConcurrency(vec, lshfuncs,
						vartracker, is,so));
			} else {
				addVectorOnlineStep(vecs.next());
			}
		}
	}

	public List<Float> getTopIdSizes() {
		return is.getCounts();
	}

	@Override
	public RPHashObject getParam() {
		return so;
	}

}

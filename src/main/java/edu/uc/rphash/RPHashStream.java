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
import edu.uc.rphash.tests.generators.ClusterGenerator;
import edu.uc.rphash.tests.generators.GenerateStreamData;

public class RPHashStream implements StreamClusterer {
	public KHHCentroidCounter is;
	private LSH[] lshfuncs;
//	private StatTests vartracker;
	private List<float[]> centroids = null;
	private RPHashObject so;
	ExecutorService executor;
	private final int processors;

	public int getProcessors() {
		return processors;
	}

	@Override
	public long addVectorOnlineStep(final float[] vec) {
		if (so.getParallel()) {
			VectorLevelConcurrency r = new VectorLevelConcurrency(vec,
					lshfuncs,  is,so);
			executor.execute(r);
			return is.count;
		}
		return VectorLevelConcurrency.computeSequential(vec, lshfuncs, is, so);
	}

	public void init() {
//		System.out.println("init rphash machine");
		Random r = new Random(so.getRandomSeed());
//		this.vartracker = new StatTests(.01f);
		
		int projections = so.getNumProjections();
		int k = (int) (so.getk()*Math.log(so.getk()));
		// initialize our counter
		float decayrate = so.getDecayRate();// 1f;// bottom number is window
											// size
		if(so.getDecayRate()==0.0){
			is = new KHHCentroidCounter(k);
		}else{
			is = new KHHCentroidCounter(k, decayrate);
		}
		// create LSH Device
		lshfuncs = new LSH[projections];
		Decoder dec = so.getDecoderType();
		HashAlgorithm hal = new MurmurHash(so.getHashmod());
		// create projection matrices add to LSH Device
		for (int i = 0; i < projections; i++) {
			Projector p = new DBFriendlyProjection(so.getdim(),
					dec.getDimensionality(), r.nextLong());
			List<float[]> noise = LSH.genNoiseTable(dec.getDimensionality(),
					so.getNumBlur(), r, dec.getErrorRadius()
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
		List<Integer> projIDs = new ArrayList<Integer>();
		List<Centroid> cents = is.getTop();
		List<Float> counts = is.getCounts();
		
		System.out.println(counts);
		
		int i =  0;
		//get rid of size one clusters that are there just because they were added to the list at the end
		for (; i < cents.size() ; i++) {
			if(counts.get(i)==1)break;
			projIDs.add(cents.get(i).projectionID);
			centroids.add(cents.get(i).centroid());
		}
		counts = counts.subList(0, i);
		Clusterer offlineclusterer = so.getOfflineClusterer();
		offlineclusterer.setWeights(counts);
		offlineclusterer.setData(centroids);
		offlineclusterer.setK(so.getk());
		centroids = offlineclusterer.getCentroids();
		
		while(centroids.size()<so.getk() && counts.size()>so.getk())centroids = offlineclusterer.getCentroids();
		if(counts.size()<so.getk())System.out.println("WARNING: Failed to partition dataset into K clusters");
		return centroids;
	}

	public void run() {
		// add to frequent itemset the hashed Decoded randomly projected vector
		Iterator<float[]> vecs = so.getVectorIterator();
		while (vecs.hasNext()) {
			if (so.getParallel()) {
				float[] vec = vecs.next();
				executor.execute(new VectorLevelConcurrency(vec, lshfuncs,is,so));
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

	@Override
	public void setWeights(List<Float> counts) {
	}

	@Override
	public void setData(List<float[]> centroids) {

	}

	@Override
	public void setK(int getk) {

	}

	@Override
	public void shutdown() {
		if (so.getParallel()) {
			executor.shutdown();
			try {
				executor.awaitTermination(10, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			executor = Executors.newFixedThreadPool(getProcessors());
		}
		
	}

}

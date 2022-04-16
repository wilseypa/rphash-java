package edu.uc.rphash;

import java.util.ArrayList;
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
//import edu.uc.rphash.projections.DBFriendlyProjection;
import edu.uc.rphash.projections.Projector;
import edu.uc.rphash.standardhash.HashAlgorithm;
import edu.uc.rphash.standardhash.MurmurHash;
import edu.uc.rphash.tests.StatTests;
import edu.uc.rphash.tests.clusterers.KMeansPlusPlus;
import edu.uc.rphash.tests.generators.ClusterGenerator;
import edu.uc.rphash.tests.generators.GenerateStreamData;

public class PRPHashStream implements StreamClusterer {
	public List<KHHCentroidCounter> is;
	public List<LSH[]> lshfuncs;
	private StatTests vartracker;
	private List<List<Centroid>> centroids = null;
	private List<Centroid> bestcentroids = null;
	private RPHashObject so;
	ExecutorService executor;
	private final int processors;
	private int concurrentRuns;

	boolean initialized=false;
	@Override
	public int getProcessors() {
		return processors;
	}

	@Override
	public long addVectorOnlineStep(final float[] vec) {
		if(!initialized){
			System.out.println("Not initialized!");
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		for(int i = 0;i<this.concurrentRuns;i++){
			//execute as future
			if (so.getParallel()) {
				executor.execute(new VectorLevelConcurrency(vec,lshfuncs.get(i),is.get(i),so));
			}//execute sequentially
			else
			{
				VectorLevelConcurrency.computeSequential(vec, lshfuncs.get(i), is.get(i), so);
			}
		}
		//there will always be at least 1 concurrent run
		return 1;//is.get(0).count;
	}

	public void init() {
//		System.out.println("init rphash machine");
		Random r = new Random(so.getRandomSeed());
//		this.vartracker = new StatTests(.01f);
		
		
		if(this.concurrentRuns<1)this.concurrentRuns=1;
		int projections = so.getNumProjections();
		int k = (int) (so.getk()*Math.log(so.getk()));
		
		// initialize our counter
//		float decayrate = so.getDecayRate();// 1f bottom number is window
		is = new ArrayList<>(concurrentRuns);
		lshfuncs = new ArrayList<LSH[]>(concurrentRuns);
		for(int i = 0;i<this.concurrentRuns;i++){
			
			if(so.getDecayRate()==0.0){
					is.add(new KHHCentroidCounter(k));
			}else{
					is.add(new KHHCentroidCounter(k));
			}
			
			// create LSH Device
			LSH[] lshfunc = new LSH[projections];
			Decoder dec = so.getDecoderType();
			dec.setCounter(is.get(i));
			HashAlgorithm hal = new MurmurHash(so.getHashmod());
			// create projection matrices add to LSH Device
				for (int projidx = 0; projidx < projections; projidx++) {
					Projector p = so.getProjectionType();
					p.setOrigDim(so.getdim());
					p.setProjectedDim(dec.getDimensionality());
					p.setRandomSeed(r.nextLong());
					p.init();
					
					List<float[]> noise = LSH.genNoiseTable(dec.getDimensionality(),
							so.getNumBlur(), r, dec.getErrorRadius()
									/ dec.getDimensionality());
					lshfunc[projidx] = new LSH(dec, p, hal, noise,so.getNormalize());
				}
			lshfuncs.add(lshfunc);
		}
		initialized = true;
	}

	public PRPHashStream(int k, ClusterGenerator c) {
		so = new SimpleArrayReader(c, k);
		if (so.getParallel())
			this.processors = Runtime.getRuntime().availableProcessors();
		else
			this.processors = 1;
		executor = Executors.newFixedThreadPool(this.processors );
		init();
	}

	public PRPHashStream(List<float[]> data, int k) {
		so = new SimpleArrayReader(data, k);
		if (so.getParallel())
			this.processors = Runtime.getRuntime().availableProcessors();
		else
			this.processors = 1;
		executor = Executors.newFixedThreadPool(this.processors );
		init();
	}

	public PRPHashStream(RPHashObject so) {
		this.so = so;
		if (so.getParallel())
			this.processors = Runtime.getRuntime().availableProcessors();
		else
			this.processors = 1;
		executor = Executors.newFixedThreadPool(this.processors );
		init();
	}

	public PRPHashStream(int k, GenerateStreamData c, int processors) {
		so = new SimpleArrayReader(c, k);
		if (so.getParallel())
			this.processors = processors;
		else
			this.processors = 1;
		executor = Executors.newFixedThreadPool(this.processors );
		init();
	}

	@Override
	public List<Centroid> getCentroids() {
		if (centroids == null) {
			init();
			run();
			getCentroidsOfflineStep();
		}
		return bestcentroids;
	}

	public List<Centroid> getCentroidsOfflineStep() {
		if (so.getParallel()) {
			executor.shutdown();
			try {
				executor.awaitTermination(10, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			executor = Executors.newFixedThreadPool(this.processors);
		}

		bestcentroids = new ArrayList<Centroid>();
//		List<Integer> projIDs = new ArrayList<Integer>();
//		List<Centroid> cents = is.getTop();
//		List<Float> counts = is.getCounts();
//		
		List<Centroid> cents = new ArrayList<Centroid>();
		int i =  0;
		//get rid of size one clusters that are there just because they were added to the list at the end
		for (; i < is.size() ; i++) {
//			if(is.get(i).count==1)break;
			cents.addAll(is.get(i).getTop());
		}
		
		;
//		counts = counts.subList(0, i);
		Clusterer offlineclusterer = new KMeansPlusPlus();
		offlineclusterer.setData(cents);
		offlineclusterer.setK(so.getk());
		cents = offlineclusterer.getCentroids();
		
		
		
//		while(centroids.size()<so.getk() && cents.size()>so.getk())cents = offlineclusterer.getCentroids();
//		if(cents.size()<so.getk())System.out.println("WARNING: Failed to partition dataset into K clusters");
		
		
		return cents;
	}

	public void run() {
//		// add to frequent itemset the hashed Decoded randomly projected vector
//		Iterator<float[]> vecs = so.getVectorIterator();
//		while (vecs.hasNext()) {
//			if (so.getParallel()) {
//				float[] vec = vecs.next();
//				executor.execute(new VectorLevelConcurrency(vec, lshfuncs,is,so));
//			} else {
//				addVectorOnlineStep(vecs.next());
//			}
//		}
	}

	public List<Float> getTopIdSizes() {
		return null;
//		return is.getCounts();
	}

	@Override
	public RPHashObject getParam() {
		return so;
	}

	@Override
	public void setWeights(List<Float> counts) {
		
	}

	@Override
	public void setRawData(List<float[]> data) 
	{
//		this.centroids = new ArrayList<Centroid>(data.size());
//		for(float[] f: data){
//			this.data.add(new Centroid(f,0));
//		}
	}
	
	@Override
	public void setData(List<Centroid> centroids) {
		ArrayList<float[]> data = new ArrayList<float[]>(centroids.size());
		for(Centroid c : centroids)data.add(c.centroid());
		setRawData(data);	
	}
	

	@Override
	public void setK(int getk) {

	}

	@Override
	public void shutdown() {
		if (so.getParallel()) {
			executor.shutdown();
			try {
//				System.out.println("Shutting Down");
				executor.awaitTermination(1200, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			executor = Executors.newFixedThreadPool(this.processors );
		}
	}
	
	@Override
	public void reset(int randomseed) {
		centroids = null;
		so.setRandomSeed(randomseed);
	}
	@Override
	public boolean setMultiRun(int runs) {
		return false;
	}

}

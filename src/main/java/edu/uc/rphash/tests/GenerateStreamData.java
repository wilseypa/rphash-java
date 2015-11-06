package edu.uc.rphash.tests;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class GenerateStreamData implements ClusterGenerator {

	protected int numClusters;
	protected int dimension;
	protected Random r;
	protected List<float[]> data;
	public List<float[]> medoids;
	protected List<float[]> variances;
	protected List<Integer> reps;
	protected float variance_scaler;
	protected boolean shuffle;
	protected float sparseness;
	protected boolean save;
	protected long size;
	protected float avgvariance;

	public GenerateStreamData(int numClusters, int dimension, float variance,
			boolean save) {
		this.r = new Random();
		this.numClusters = numClusters;
		this.dimension = dimension;
		this.shuffle = true;
		this.medoids = new ArrayList<float[]>();
		this.data = new ArrayList<float[]>();
		this.reps = new ArrayList<Integer>();
		this.avgvariance = variance;
		this.variance_scaler = (variance / (float) Math.sqrt(dimension));// normalize
		this.variances = new ArrayList<float[]>();															// dimension
		this.sparseness = 1.0f;
		this.generateMedoids();
		this.save = save;
	}

	public GenerateStreamData(int numClusters, int dimension, float variance, long randomseed) {
		this.r = new Random(randomseed);
		this.numClusters = numClusters;
		this.dimension = dimension;
		this.shuffle = true;
		this.medoids = new ArrayList<float[]>();
		
		this.data = new ArrayList<float[]>();
		this.reps = new ArrayList<Integer>();
		this.variances = new ArrayList<float[]>();
		this.variance_scaler = (variance / (float) Math.sqrt(dimension));// normalize
																		// dimension
		this.sparseness = 1.0f;
		this.save = false;
		this.generateMedoids();
	
	}
	public GenerateStreamData(int numClusters, int dimension, float variance) {
		this.r = new Random();
		this.numClusters = numClusters;
		this.dimension = dimension;
		this.shuffle = true;
		this.medoids = new ArrayList<float[]>();
		
		this.data = new ArrayList<float[]>();
		this.reps = new ArrayList<Integer>();
		this.variances = new ArrayList<float[]>();
		this.variance_scaler = (variance / (float) Math.sqrt(dimension));// normalize
																		// dimension
		this.sparseness = 1.0f;
		this.save = false;
		this.generateMedoids();

	}

	public void generateMedoids() {
		if (save) {
			reps = new ArrayList<>();
			data = new ArrayList<>();
		}
		size = 0;
		for (int i = 0; i < numClusters; i++) {
			// gen cluster center
			float[] medoid = new float[dimension];
			float[] variance = new float[dimension];

			for (int k = 0; k < dimension; k++) {
				if (r.nextInt() % (int) (1.0f / sparseness) == 0) {
					medoid[k] = r.nextFloat() * 2.0f - 1.0f;
					variance[k] = variance_scaler * (r.nextFloat());
				} else {
					medoid[k] = 0;
					variance[k] = 0;
				}

			}
			this.medoids.add(medoid);
			this.variances.add(variance);
		}

	}

	
	
	private class ParallelGen implements Runnable {

	
		float[] dat;
		float[] medoid;
		float[] variance;
		int end;
		int start;
		
		public ParallelGen(float[] dat,float[] medoid,float[] variance,int start, int end) {
			this.dat = dat;
			this.medoid = medoid;
			this.variance = variance;
			this.start = start;
			this.end = end;
		}

		@Override
		public void run() {
			for (int k = start; k < end; k++) {
				if (r.nextInt() % (int) (1.0f / sparseness) == 0)
					dat[k] = medoid[k] + (float) r.nextGaussian() * variance[k];
			}
		}
	}
	
	public int processors = Runtime.getRuntime().availableProcessors();
	public ExecutorService executor = Executors.newFixedThreadPool(processors);
	
	public float[] generateNext() {
		
		int randcluster = (int) ((size++) % numClusters);
		if (shuffle) {
			r = new Random();
			randcluster = r.nextInt(numClusters);
		}

		float[] variance = variances.get(randcluster);
		float[] medoid = medoids.get(randcluster);
		float[] dat = new float[dimension];
		
//		int lenDivProcCount = dimension/processors;
//		
//		int i=0;
//		for(;i<processors-1;i++){
//			ParallelGen r = new ParallelGen(dat,medoid,variance,i*lenDivProcCount, (i+1)*lenDivProcCount);
//			executor.submit(r);
//		}
//		
//		ParallelGen r = new ParallelGen(dat,medoid,variance,i*lenDivProcCount,dimension);
//		executor.submit(r);

		for (int k = 0; k < dimension; k++) {
			if (r.nextInt() % (int) (1.0f / sparseness) == 0)
				dat[k] = medoid[k] + (float) r.nextGaussian() * variance[k];
		}
		
		
		if (save) {
			data.add(dat);
			reps.add(randcluster);
		}
		
		return dat;
	}


	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	@Override
	public List<float[]> getMedoids() {
		return medoids;
	}

	@Override
	public List<float[]> getData() {
		return data;
	}

	@Override
	public List<Integer> getLabels() {
		return reps;
	}

	@Override
	public int getDimension() {
		return dimension;
	}

	@Override
	public Iterator<float[]> getIterator() {
		return new Iterator<float[]>(){

			@Override
			public boolean hasNext() {
				return true;
			}

			@Override
			public float[] next() {
				return generateNext();
			}
			
		};

	}

}
package edu.uc.rphash.tests;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.uc.rphash.RPHashStream;
import edu.uc.rphash.Readers.SimpleArrayReader;
import edu.uc.rphash.Readers.StreamObject;

public class GenerateStreamData implements ClusterGenerator {

	private int numClusters;
	private int dimension;
	private Random r;
	private List<float[]> data;
	private List<float[]> medoids;
	private List<float[]> variances;
	private List<Integer> reps;
	private float scaler;
	private boolean shuffle;
	private float sparseness;
	private boolean save;
	private long size;

	public GenerateStreamData(int numClusters, int dimension, float variance,
			boolean save) {
		this.r = new Random();
		this.numClusters = numClusters;
		this.dimension = dimension;
		this.shuffle = true;
		this.medoids = new ArrayList<float[]>();
		this.data = new ArrayList<float[]>();
		this.reps = new ArrayList<Integer>();
		this.scaler = 1f / (variance * (float) Math.sqrt(dimension));// normalize
		this.variances = new ArrayList<float[]>();															// dimension
		this.sparseness = 1.0f;
		this.generateMedoids();
		this.save = save;
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
		this.scaler = 1f / (variance * (float) Math.sqrt(dimension));// normalize
																		// dimension
		this.sparseness = 1.0f;
		this.save = false;
		this.generateMedoids();

	}

	private void generateMedoids() {
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
					variance[k] = scaler * (r.nextFloat() * 2.0f - 1.0f);
				} else {
					medoid[k] = 0;
					variance[k] = 0;
				}

			}
			this.medoids.add(medoid);
			this.variances.add(variance);
		}

	}

	public float[] generateNext() {
		int randcluster = (int) ((size++) % numClusters);
		if (shuffle) {
			r = new Random();
			randcluster = r.nextInt(numClusters);
		}

		float[] variance = variances.get(randcluster);
		float[] medoid = medoids.get(randcluster);
		float[] dat = new float[dimension];
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

	//
	//
	// PipedOutputStream outputStream;
	//
	// public void init() {
	// outputStream = new PipedOutputStream();
	// medoids = new ArrayList<float[]>(numClusters);
	// variances = new ArrayList<float[]>(numClusters);
	// for (int i = 0; i < numClusters; i++) {
	// // gen cluster center
	// float[] medoid = new float[dimension];
	// float[] variance = new float[dimension];
	//
	// for (int k = 0; k < dimension; k++) {
	// if (r.nextInt() % (int) (1.0f / sparseness) == 0) {
	// medoid[k] = r.nextFloat() * 2.0f - 1.0f;
	// variance[k] = scaler * (r.nextFloat() * 2.0f - 1.0f);
	// }
	// }
	// this.variances.add(variance);
	// this.medoids.add(medoid);
	// }
	// return;
	// }
	//
	// public PipedOutputStream getOutputStream() {
	// return outputStream;
	// }
	//
	//
	//
	// volatile long pause = 0;
	// @Override
	// public void run() {
	// int i = 0;
	// DataOutputStream d = new DataOutputStream(outputStream);
	// try {
	// //double sum = 0.0;
	// while (true) {
	//
	// Thread.sleep(pause);
	// int index = r.nextInt(this.numClusters);
	// float[] medoid = medoids.get(index);
	// float[] variance = variances.get(index);
	// for (int k = 0; k < dimension; k++) {
	// float newpt = 0;
	// if (r.nextInt() % (int) (1.0f / sparseness) == 0)
	// newpt = medoid[k] + (float) r.nextGaussian()
	// * variance[k];
	// d.writeFloat(newpt);
	// }
	// if (++i % 1000 == 0) {
	// System.out.println("wrote: "+ i + " vectors");
	// }
	//
	// }
	//
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }
	//
	// public static void main(String[] args) throws InterruptedException,
	// ExecutionException, IOException {
	// ExecutorService executor = Executors.newFixedThreadPool(3);
	//
	// final GenerateStreamData b = new GenerateStreamData(10, 10000,1f);
	// System.out.println("Centroids");
	// for(float[] f:b.medoids){
	// TestUtil.prettyPrint(f);
	// System.out.println();
	// }
	//
	// System.out.println("Variances");
	// for(float[] f:b.variances){
	// TestUtil.prettyPrint(f);
	// System.out.println();
	// }
	//
	// StreamObject obj = new StreamObject(b.outputStream, b.numClusters,
	// b.dimension, executor);
	// executor.submit(b);
	//
	//
	// final RPHashStream rps = new RPHashStream(obj);
	// //executor.execute(rps);
	//
	//
	//
	// while(true)
	// {
	// try {
	// Thread.sleep(10000);
	// } catch (InterruptedException e) {
	// e.printStackTrace();
	// }
	// b.pause = 1000;
	// for(float[] l : rps.getCentroids()){
	// TestUtil.prettyPrint(l);
	// System.out.println();
	// }
	//
	// b.pause = 0;
	// }
	// }


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
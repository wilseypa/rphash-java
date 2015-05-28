package edu.uc.rphash.tests;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.uc.rphash.RPHashStream;
import edu.uc.rphash.Readers.StreamObject;

public class GenerateStreamData extends GenerateData implements Runnable {

	public GenerateStreamData(int numClusters, int dimension) {
		super(numClusters, 0, dimension);
	}

	public GenerateStreamData(int numClusters, int dimension, boolean shuffle) {
		super(numClusters, 0, dimension, shuffle);
	}

	public GenerateStreamData(int numClusters, int dimension, float variance) {
		super(numClusters, 0, dimension, variance);
	}

	public GenerateStreamData(int numClusters, int dimension, float variance,
			boolean shuffle) {
		super(numClusters, 0, dimension, variance, shuffle);
	}

	public GenerateStreamData(int numClusters, int dimension, float variance,
			boolean shuffle, float sparseness) {
		super(numClusters, 0, dimension, variance, shuffle, sparseness);
	}

	public GenerateStreamData(int numClusters, int dimension,
			RandomDistributionFnc genvariate) {
		super(numClusters, 0, dimension);
	}

	public GenerateStreamData(int numClusters, int dimension, File f) {
		super(numClusters, 0, dimension, f);
	}

	public GenerateStreamData(int numClusters, int dimension, File f,
			RandomDistributionFnc genvariate) {
		super(numClusters, 0, dimension, f, genvariate);
	}

	PipedOutputStream outputStream;

	@Override
	public void generateMem() {
		outputStream = new PipedOutputStream();
		medoids = new ArrayList<float[]>(numClusters);
		variances = new ArrayList<float[]>(numClusters);
		for (int i = 0; i < numClusters; i++) {
			// gen cluster center
			float[] medoid = new float[dimension];
			float[] variance = new float[dimension];

			for (int k = 0; k < dimension; k++) {
				if (r.nextInt() % (int) (1.0f / sparseness) == 0) {
					medoid[k] = r.nextFloat() * 2.0f - 1.0f;
					variance[k] = scaler * (r.nextFloat() * 2.0f - 1.0f);
				}
			}
			this.variances.add(variance);
			this.medoids.add(medoid);
		}
		return;
	}

	public PipedOutputStream getOutputStream() {
		return outputStream;
	}

	@Override
	public void generateDisk(File f) {
		System.out.println("Just pipe the data to a file");
		return;
	}

	volatile long pause = 0;
	@Override
	public void run() {
		int i = 0;
		DataOutputStream d = new DataOutputStream(outputStream);
		try {
			//double sum = 0.0;
			while (true) {

				Thread.sleep(pause);		
				int index = r.nextInt(this.numClusters);
				float[] medoid = medoids.get(index);
				float[] variance = variances.get(index);
				for (int k = 0; k < dimension; k++) {
					float newpt = 0;
					if (r.nextInt() % (int) (1.0f / sparseness) == 0)
						newpt = medoid[k] + (float) r.nextGaussian()
								* variance[k];
					d.writeFloat(newpt);
				}
				if (++i % 1000 == 0) {
					System.out.println("wrote: "+ i + " vectors");
				}
				
			}
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws InterruptedException,
			ExecutionException, IOException {
		ExecutorService executor = Executors.newFixedThreadPool(3);
		
		final GenerateStreamData b = new GenerateStreamData(10, 10000);
		for(float[] f:b.medoids){
			TestUtil.prettyPrint(f);
			System.out.println();
		}
		StreamObject obj = new StreamObject(b.outputStream, b.numClusters,
				b.dimension, executor);
		executor.submit(b);

		
		final RPHashStream rps = new RPHashStream(obj);
		executor.execute(rps);
		
		

    	while(true)
		{
			try {
				Thread.sleep(100000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			b.pause = 1000;
			for(float[] l : rps.getCentroids()){
				System.out.println();
				TestUtil.prettyPrint(l);
			}
			b.pause = 0;
		}
	}
}
package edu.uc.rphash.tests;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GenerateMovingStream extends GenerateStreamData {

	public GenerateMovingStream(int numClusters, int dimension, float variance) {
		super(numClusters, dimension, variance);
		avgvariance = variance;
		System.out.println("warning no motion behavior specified");
	}

	private float movementPerVector;

	public GenerateMovingStream(int numClusters, int dimension, float variance,
			float movementPerVector) {
		super(numClusters, dimension, variance);
		this.movementPerVector = movementPerVector;
		avgvariance = variance;
	}

	protected List<float[]> directions;

	@Override
	public void generateMedoids() {
		super.generateMedoids();
		this.directions = new ArrayList<>();
		// generate some random direction vectors for each medoid
		for (int i = 0; i < numClusters; i++) {
			// gen cluster center
			float[] direction = new float[dimension];

			for (int k = 0; k < dimension; k++) {
				direction[k] = r.nextFloat() * 2.0f - 1.0f;
			}
			this.directions.add(direction);
		}

	}

	public void updateMedoid(){
		int randcluster = r.nextInt(numClusters);
		float[] medoid = medoids.get(randcluster);
		float[] direction = directions.get(randcluster);
		
		for (int k = 0; k < dimension; k++) {
			medoid[k] += direction[k] * movementPerVector;
		}
		
	}
	
	
	@Override
	public float[] generateNext() {
		// return super.generateNext();
		// int randcluster = (int) ((size++) % numClusters);
		// if (shuffle) {
		r = new Random();
		int randcluster = r.nextInt(numClusters);
		// }

		float[] medoid = medoids.get(randcluster);
		float[] dat = new float[dimension];
		
		for (int k = 0; k < dimension; k++) {
			//medoid[k] += direction[k] * movementPerVector;
			dat[k] = medoid[k] + (float) r.nextGaussian() * avgvariance;
		}

		if (save) {
			data.add(dat);
			reps.add(randcluster);
		}
		return dat;
	}
	
	 static void print(float [] p){
		 System.out.println(p[0]);
		 System.out.println(p[1]);
		 System.out.println(p[2]);
	}
	
	public static void main(String[] args) {
		GenerateMovingStream gen = new GenerateMovingStream(1, 3, .1f, .075f);
		ArrayList<float[]> a = new ArrayList<>();

		print(gen.medoids.get(0));
		for (int i = 0; i < 2; i++)gen.updateMedoid();
		for (int i = 0; i < 200; i++) {
			if(i%10==0)gen.updateMedoid();
			a.add(gen.generateNext());
		}
		for (int i = 0; i < 4; i++)gen.updateMedoid();
		print(gen.medoids.get(0));
		
		TestUtil.writeFile(new File("/home/lee/Desktop/out1.mat"), a);

		gen = new GenerateMovingStream(1, 3, .1f, .075f);
		a = new ArrayList<>();
		print(gen.medoids.get(0));
		for (int i = 0; i < 2; i++)gen.updateMedoid();
		for (int i = 0; i < 200; i++) {
			if(i%10==0)gen.updateMedoid();
			a.add(gen.generateNext());
		}
		for (int i = 0; i < 4; i++)gen.updateMedoid();
		print(gen.medoids.get(0));
		TestUtil.writeFile(new File("/home/lee/Desktop/out2.mat"), a);

		gen = new GenerateMovingStream(1, 3, .1f, .075f);
		a = new ArrayList<>();
		print(gen.medoids.get(0));
		for (int i = 0; i < 2; i++)gen.updateMedoid();
		for (int i = 0; i < 200; i++) {
			if(i%10==0)gen.updateMedoid();
			a.add(gen.generateNext());
		}
		for (int i = 0; i < 4; i++)gen.updateMedoid();
		print(gen.medoids.get(0));
		TestUtil.writeFile(new File("/home/lee/Desktop/out3.mat"), a);

	}

}

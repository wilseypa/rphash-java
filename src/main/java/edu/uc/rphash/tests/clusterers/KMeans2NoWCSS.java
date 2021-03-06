package edu.uc.rphash.tests.clusterers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import edu.uc.rphash.Centroid;
import edu.uc.rphash.Clusterer;
import edu.uc.rphash.RPHashStream;
import edu.uc.rphash.Readers.RPHashObject;
import edu.uc.rphash.tests.StatTests;
import edu.uc.rphash.tests.generators.GenerateStreamData;

public class KMeans2NoWCSS implements Clusterer {

	class PointND {

		private int dimension; // number of coordinates of a point
		private float[] coordinates; // the coordinates of a point
		private int count; // the coordinates of a point
		private float wcss;

		/**
		 * Create a point centered at the origin of the specific dimension
		 **/
		public PointND(int dimension) {
			this.count = 0;
			this.wcss = 0f;
			this.dimension = dimension;
			coordinates = new float[dimension];
		}

		public PointND(float[] data, int count, float wcss) {
			this.dimension = data.length;
			this.coordinates = data;
			this.count = count;
			this.wcss = wcss;
		}

		/**
		 * Create a new point identical to point p
		 **/
		public PointND(PointND p) {
			this.count = p.count;
			this.wcss = p.wcss;
			this.dimension = p.dimension;
			this.coordinates = new float[dimension];
			for (int i = 0; i < dimension; i++)
				this.coordinates[i] = p.coordinates[i];
		}

		public PointND(Centroid p) {
			this.count = (int) p.getCount();
			this.wcss = 0;//p.getWCSS();
			this.dimension = p.centroid().length;
			this.coordinates = new float[dimension];
			for (int i = 0; i < dimension; i++)
				this.coordinates[i] = p.centroid()[i];
		}
	}

	private int n; // number of instances to classify
	private int d; // number of coordinates of each point
	private int k; // number of clusters
	private PointND[] mu; // coordinate of means mu[j] of each cluster j
	private Vector<PointND>[] w; // holds the points classified into each class
									// w[j]
	private PointND[] sigma; // holds the standard deviation of each class i
	private float[] prior; // holds the prior of each class i
	// private float logLikelihood; // holds the log likelihood of each of the k
	// Gaussians
	private float MDL; // the minimum description length of the model
	private int numIterations = 100;

	private List<Centroid> centroids;
	private PointND[] data;

	public KMeans2NoWCSS(int getk, List<float[]> data) {
		this.data = new PointND[data.size()];
		for (int i = 0; i < data.size(); i++) {
			this.data[i] = new PointND(data.get(i), 1, 0f);
		}
		this.centroids = null;
		init(this.data, getk);
	}

	public KMeans2NoWCSS() {
	}

	/**
	 * Intialize the parameters of the k-means algorithm Randomly assign a point
	 * in x to each mean mu[j]
	 **/
	private void init(PointND[] x, int k) {
		this.n = x.length;
		this.d = x[0].dimension;
		this.k = k;
		this.mu = new PointND[k];
		this.w = new Vector[k];
		this.sigma = new PointND[k];
		this.prior = new float[k];
		
		// randomly assign a point in x to each mean mu[j]
		PointND randomPoint;
		for (int j = 0; j < k; j++) {
			randomPoint = x[(int) (Math.random() * (n - 1))];
			mu[j] = new PointND(randomPoint);
			// each prior and standard deviation are set to zero
			sigma[j] = new PointND(d);
			prior[j] = 0;
		}
	}

	/**
	 * Runs the k-means algorithm with k clusters on the set of instances x Then
	 * find the quality of the model
	 **/
	public void run(PointND[] x, int k, float epsilon) {
		float maxDeltaMeans = epsilon + 1;
		PointND[] oldMeans = new PointND[k];
		// initialize n,k,mu[j]
		init(x, k);
		int iter = 0;
		// iterate until there is no change in mu[j]
		while (maxDeltaMeans > epsilon && iter++ < numIterations) {
			// remember old values of the each mean
			for (int j = 0; j < k; j++) {
				oldMeans[j] = new PointND(mu[j]);

			}

			// classify each instance x[i] to its nearest class
			// first we need to clear the class array since we are reclassifying
			for (int j = 0; j < k; j++) {
				w[j] = new Vector<PointND>(); // could use clear but then have
												// to init...
			}

			for (int i = 0; i < n; i++) {
				classify(x[i]);
			}
			// recompute each mean
			computeMeans();
			// compute the largest change in mu[j]
			maxDeltaMeans = maxDeltaMeans(oldMeans);
		}
		if(iter==numIterations)System.out.println("Max Iterations Reached");
	}

	/**
	 * Classifies the point x to the nearest class
	 **/
	private void classify(PointND x) {
		float dist = 0;
		float smallestDist;
		int nearestClass;

		// compute the distance x is from mean mu[0]
		smallestDist = distance(x.coordinates, mu[0].coordinates);
		nearestClass = 0;

		// compute the distance x is from the other classes
		for (int j = 1; j < k; j++) {
			dist = distance(x.coordinates, mu[j].coordinates);
			if (dist < smallestDist) {
				smallestDist = dist;
				nearestClass = j;
			}
		}
		// classify x into class its nearest class
		w[nearestClass].add(x);
	}

	float distance(float[] x, float[] y) {
		float ret = 0.0f;
		if (x.length != y.length)
			return Float.MAX_VALUE;
		for (int i = 0; i < x.length; i++)
			ret += (x[i] - y[i]) * (x[i] - y[i]);
		return (float) Math.sqrt(ret);
	}

	PointND subtract(PointND px, PointND py) {
		float[] x = px.coordinates;
		float[] y = py.coordinates;

		float[] ret = new float[x.length];
		if (x.length != y.length)
			return null;

		for (int i = 0; i < x.length; i++) {
			ret[i] = (x[i]  - y[i] ) ;
		}

		return new PointND(ret,1, 0f);
	}

	PointND add(PointND px, PointND py) {

		if (px.dimension != py.dimension)
			return null;

		float[] x = px.coordinates;
		float[] y = py.coordinates;

		float[] ret = new float[x.length];

		float total = px.count + py.count;

		for (int i = 0; i < x.length; i++) {
			ret[i] = (x[i] * px.count + y[i] * py.count) / total;
		}
		return new PointND(ret, 1, 0f);
	}

	public float max(float[] coordinates) {
		float value;
		float max = coordinates[0];
		for (int i = 1; i < coordinates.length; i++) {
			value = coordinates[i];
			if (value > max)
				max = value;
		}
		return max;
	}

	/**
	 * Recompute mu[j] as the average of all points classified to the class w[j]
	 **/
	private void computeMeans() {
		int numInstances; // number of instances in each class w[j]
		PointND instance;
		// init the means to zero
		for (int j = 0; j < k; j++)
			mu[j] = new PointND(mu[j].dimension);
		// recompute the means of each cluster
		for (int j = 0; j < k; j++) {
			// recompute the means of each cluster
			numInstances = w[j].size();
			for (int i = 0; i < numInstances; i++) {
				instance = w[j].get(i);
				mu[j] = add(mu[j], instance);
			}
			double wcss = 0.0;
			// recompute the wcss of each cluster
			for (int i = 0; i < numInstances; i++) {
				instance = w[j].get(i);
				for(float f : subtract(mu[j], instance).coordinates){
					wcss += f*f;
				}
			}
			mu[j].wcss = (float) Math.sqrt(wcss)/(float)numInstances;
			mu[j].count = numInstances;
			
		}
	}

	/**
	 * Compute the maximum change over each mean mu[j]
	 **/
	private float maxDeltaMeans(PointND[] oldMeans) {
		float delta;
		oldMeans[0] = subtract(oldMeans[0], mu[0]);

		float maxDelta = max(oldMeans[0].coordinates);
		for (int j = 1; j < k; j++) {
			oldMeans[j] = subtract(oldMeans[j], mu[j]);
			delta = max(oldMeans[j].coordinates);
			if (delta > maxDelta)
				maxDelta = delta;
		}
		return maxDelta;
	}

	public float getMDL() {
		return MDL;
	}

	public List<Centroid> getCentroids() {
		
		
		
		float epsilon = 0.001f;
		if (centroids != null) {
			return centroids;
		}
		
		double minwcss = Double.MAX_VALUE;
		List<Centroid> mincentroids = new ArrayList<>();
		
		for(int j = 0;j<runs;j++){
		
			init(data, k);
			run(data, k, epsilon);
			centroids = new ArrayList<Centroid>(k);
			double twcss = 0.0;
			for (int i = 0; i < k; i++) {
				Centroid c = new Centroid(mu[i].coordinates, 0);
				c.setWCSS(new double[]{mu[i].wcss});
				c.setCount(mu[i].count);
				centroids.add(c);
				twcss+=(mu[i].wcss);
			}
			if(twcss<minwcss){
				minwcss = twcss;
				mincentroids = centroids;
			}
		}

		return mincentroids;
	}

	@Override
	public RPHashObject getParam() {
		return null;
	}

	@Override
	public void setWeights(List<Float> counts) {
		if (data != null) {
			for (int i = 0; i < counts.size() && i < this.data.length; i++) {
				this.data[i].count = counts.get(i).intValue();
			}
		} else
			System.out
					.println("Data does not exist, set data first then weights");
	}

	@Override
	public void setData(List<Centroid> data) {
		this.centroids = null;
		this.data = new PointND[data.size()];
		for (int i = 0; i < data.size(); i++) {
			this.data[i] = new PointND(data.get(i));
		}
	}

	@Override
	public void setRawData(List<float[]> data) {
		this.centroids = null;
		this.data = new PointND[data.size()];
		for (int i = 0; i < data.size(); i++) {
			this.data[i] = new PointND(data.get(i), 1, 0f);
		}
	}

	@Override
	public void setK(int getk) {
		this.k = getk;
	}

	public static void main(String[] args) {
		
		testkm2();
		/*
		int k = 5;
		int d = 100;
		float var = 2.1f;
		int interval = 50;
		Runtime rt = Runtime.getRuntime();

		GenerateStreamData gen1 = new GenerateStreamData(k, d, var, 11331313);
		GenerateStreamData noise = new GenerateStreamData(1, d, var * 10,
				11331313);
		KMeans2 km2 = new KMeans2();
		// HartiganWongKMeans hwkm = new HartiganWongKMeans();

		System.out.printf("\tKMeans\t\t\tNull\t\tReal\n");
		System.out
				.printf("Vecs\tMem(KB)\tTime\tWCSSE\t\tTime\tWCSSE\t\tWCSSE\n");

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

			timestart = System.nanoTime();
			km2.setRawData(justvecsInThisRound);
			km2.setK(k);
			km2.setMultiRun(10);

			List<Centroid> cents = km2.getCentroids();
			long time = System.nanoTime() - timestart;

			rt.gc();
			long usedkB = (rt.totalMemory() - rt.freeMemory()) / 1024;

			double wcsse = StatTests.WCSSECentroidsFloat(cents,
					justvecsInThisRound);
			double realwcsse = StatTests.WCSSE(gen1.medoids,
					justvecsInThisRound);
//			System.out.printf("%d\t%d\t%.4f\t%.1f\t\t", i, usedkB,
//					time / 1000000000f, wcsse);

			// cents = new HartiganWongKMeans(k, vecsAndNoiseInThisRound)
			// .getCentroids();
			// time = System.nanoTime() - timestart;
			// usedkB = (rt.totalMemory() - rt.freeMemory()) / 1024;
			// wcsse = StatTests.WCSSECentroidsFloat(cents,
			// justvecsInThisRound);
			System.out.println(wcsse);
//			System.out.printf("%.4f\t%.1f\t\t%.1f\n", time / 1000000000f,
//					wcsse, realwcsse);
		}*/
	}

	@Override
	public void reset(int randomseed) {
		centroids = null;
	}

	int runs = 1;
	@Override
	public boolean setMultiRun(int runs) {
		this.runs = runs;
		return true;
	
	}

	private static void testkm2() {

		int k = 5;
		int d = 100;
		float var = 2.1f;
		int interval = 1000;
		Runtime rt = Runtime.getRuntime();

		GenerateStreamData gen1 = new GenerateStreamData(k, d, var, 11331313);
		GenerateStreamData noise = new GenerateStreamData(1, d, var * 10,
				11331313);
		KMeans2 km2 = new KMeans2();
		// HartiganWongKMeans hwkm = new HartiganWongKMeans();

		System.out.printf("\tKMeans\t\t\tNull\t\tReal\n");
		System.out
				.printf("Vecs\tMem(KB)\tTime\tWCSSE\t\tTime\tWCSSE\t\tWCSSE\n");

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

			timestart = System.nanoTime();
			km2.setRawData(justvecsInThisRound);
			km2.setK(k);
			km2.setMultiRun(10);

			List<Centroid> cents = km2.getCentroids();
			long time = System.nanoTime() - timestart;

			rt.gc();
			long usedkB = (rt.totalMemory() - rt.freeMemory()) / 1024;

			double wcsse = StatTests.WCSSECentroidsFloat(cents,
					justvecsInThisRound);
			double realwcsse = StatTests.WCSSE(gen1.medoids,
					justvecsInThisRound);
			 System.out.printf("%d\t%d\t%.4f\t%.1f\t\t", i, usedkB,
			 time / 1000000000f, wcsse);

//			 cents = new HartiganWongKMeans(k, vecsAndNoiseInThisRound)
//			 .getCentroids();
//			 time = System.nanoTime() - timestart;
//			 usedkB = (rt.totalMemory() - rt.freeMemory()) / 1024;
//			 wcsse = StatTests.WCSSECentroidsFloat(cents,
//			 justvecsInThisRound);
			 
			 
			System.out.println(realwcsse);
			 //System.out.printf("%.4f\t%.1f\t\t%.1f\n", time / 1000000000f,
			 //wcsse, realwcsse);
		}
	}
}
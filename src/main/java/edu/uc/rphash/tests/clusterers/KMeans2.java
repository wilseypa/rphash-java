package edu.uc.rphash.tests.clusterers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import edu.uc.rphash.Clusterer;
import edu.uc.rphash.RPHashStream;
import edu.uc.rphash.Readers.RPHashObject;
import edu.uc.rphash.tests.StatTests;
import edu.uc.rphash.tests.generators.GenerateStreamData;

public class KMeans2 implements Clusterer {

	class PointND {

		private int dimension; // number of coordinates of a point
		private float[] coordinates; // the coordinates of a point

		/**
		 * Create a point centered at the origin of the specific dimension
		 **/
		public PointND(int dimension) {
			this.dimension = dimension;
			coordinates = new float[dimension];
		}

		public PointND(float[] data) {
			this.dimension = data.length;
			coordinates = data;
		}

		/**
		 * Create a new point identical to point p
		 **/
		public PointND(PointND p) {
			this.dimension = p.dimension;
			this.coordinates = new float[dimension];
			for (int i = 0; i < dimension; i++)
				this.coordinates[i] = p.coordinates[i];
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
	private int numIterations;

	private List<float[]> centroids;
	private PointND[] data;

	public KMeans2(int getk, List<float[]> data) {
		this.data = new PointND[data.size()];
		for (int i = 0; i < data.size(); i++) {
			this.data[i] = new PointND(data.get(i));
		}
		this.centroids = null;
		init(this.data, getk);
	}

	public KMeans2() {
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
		this.numIterations = 0;
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
		// iterate until there is no change in mu[j]
		while (maxDeltaMeans > epsilon) {
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
			numIterations++;
		}
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

	float[] subtract(float[] x, float[] y) {
		float[] ret = new float[x.length];
		if (x.length != y.length)
			return null;
		for (int i = 0; i < x.length; i++)
			ret[i] = x[i] - y[i];
		return ret;
	}

	float[] add(float[] x, float[] y) {
		float[] ret = new float[x.length];
		if (x.length != y.length)
			return null;
		for (int i = 0; i < x.length; i++)
			ret[i] = x[i] + y[i];
		return ret;
	}

	float[] multiply(float[] x, float scalar) {
		float[] ret = new float[x.length];
		for (int i = 0; i < x.length; i++)
			ret[i] = x[i] * scalar;
		return ret;
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
			numInstances = w[j].size();
			for (int i = 0; i < numInstances; i++) {
				instance = w[j].get(i);
				mu[j] = new PointND(
						add(mu[j].coordinates, instance.coordinates));
				// mu[j].add(instance);
			}
			// mu[j].multiply(1.0f / numInstances);
			mu[j] = new PointND(
					multiply(mu[j].coordinates, 1.0f / numInstances));
		}

	}

	/**
	 * Compute the maximum change over each mean mu[j]
	 **/
	private float maxDeltaMeans(PointND[] oldMeans) {
		float delta;
		oldMeans[0] = new PointND(subtract(oldMeans[0].coordinates,
				mu[0].coordinates));
		// oldMeans[0].subtract(mu[0]);

		float maxDelta = max(oldMeans[0].coordinates);
		for (int j = 1; j < k; j++) {
			// oldMeans[j].subtract(mu[j]);
			oldMeans[j] = new PointND(subtract(oldMeans[j].coordinates,
					mu[j].coordinates));
			delta = max(oldMeans[j].coordinates);
			if (delta > maxDelta)
				maxDelta = delta;
		}
		return maxDelta;
	}

	// /**
	// * Compute the standard deviation of the k Gaussians
	// **/
	// private void computeDeviation() {
	// int numInstances; // number of instances in each class w[j]
	// PointND instance;
	// PointND temp;
	//
	// // set the standard deviation to zero
	// for (int j = 0; j < k; j++)
	// sigma[j].setToOrigin();
	//
	// // for each cluster j...
	// for (int j = 0; j < k; j++) {
	// numInstances = w[j].size();
	// for (int i = 0; i < numInstances; i++) {
	// instance = (PointND) (w[j].get(i));
	// temp = new PointND(instance);
	// temp.subtract(mu[j]);
	// temp.pow(2.0f); // (x[i]-mu[j])^2
	// temp.multiply(1.0f / numInstances); // multiply by proba of
	// // having x[i] in cluster j
	// sigma[j].add(temp); // sum i (x[i]-mu[j])^2 * p(x[i])
	// }
	// sigma[j].pow((1.0f / 2f)); // because we want the standard deviation
	// }
	// }
	//
	// /**
	// * Compute the priors of the k Gaussians
	// **/
	// private void computePriors() {
	// float numInstances; // number of instances in each class w[j]
	// for (int j = 0; j < k; j++) {
	// numInstances = w[j].size() * (1.0f);
	// prior[j] = numInstances / n;
	// }
	// }
	//
	// /**
	// * Assume the standard deviations and priors of each cluster have been
	// * computed
	// **/
	// private void computeLogLikelihood(PointND[] x) {
	// float temp1 = 0;
	// float temp2 = 0;
	// PointND variance;
	// float ln2 = (float) Math.log(2);
	// // for each instance x
	// for (int i = 0; i < n; i++) {
	// // for each cluster j
	// temp1 = 0;
	// for (int j = 0; j < k; j++) {
	// temp1 = temp1 + (x[i].normal(mu[j], sigma[j]) * prior[j]);
	// }
	// temp2 = (float) (temp2 + Math.log(temp1) / ln2);
	// }
	// logLikelihood = temp2;
	// }
	//
	// /**
	// * Assume the log likelihood and priors have been computed
	// **/
	// private void computeMDL() {
	// float temp = 0;
	// float numInstances;
	// float ln2 = (float) Math.log(2);
	// for (int j = 0; j < k; j++) {
	// numInstances = w[j].size();
	// for (int i = 0; i < d; i++) {
	// temp = (float) (temp - Math.log(sigma[j].getCoordinate(i)
	// / Math.sqrt(numInstances))
	// / ln2);
	// }
	// }
	// MDL = temp - logLikelihood;
	// }

	public float getMDL() {
		return MDL;
	}

	public List<float[]> getCentroids() {
		float epsilon = 0.01f;
		if (centroids != null)
			return centroids;
		init(data, k);
		run(data, d, epsilon);
		centroids = new ArrayList<float[]>(k);
		for (int i = 0; i < k; i++)
			centroids.add(mu[i].coordinates);

		// compute sum of squares
		double sigtotal = 0.0;
		for (int i = 0; i < sigma.length; i++)
			for (int j = 0; j < sigma[i].dimension; j++)
				sigtotal += sigma[i].coordinates[j];

		return centroids;
	}

	@Override
	public RPHashObject getParam() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setWeights(List<Float> counts) {

	}

	@Override
	public void setData(List<float[]> data) {
		this.data = new PointND[data.size()];
		for (int i = 0; i < data.size(); i++) {
			this.data[i] = new PointND(data.get(i));
		}
		this.centroids = null;

	}

	@Override
	public void setK(int getk) {
		this.k = getk;
	}

	public static void main(String[] args) {
		int k = 10;
		int d = 240;
		float var = 1f;
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
			km2.setData(vecsAndNoiseInThisRound);
			km2.setK(k);

			List<float[]> cents = km2.getCentroids();
			long time = System.nanoTime() - timestart;

			rt.gc();
			long usedkB = (rt.totalMemory() - rt.freeMemory()) / 1024;

			double wcsse = StatTests.WCSSE(cents, justvecsInThisRound);
			double realwcsse = StatTests.WCSSE(gen1.medoids,
					justvecsInThisRound);
			System.out.printf("%d\t%d\t%.4f\t%.1f\t\t", i, usedkB,
					time / 1000000000f, wcsse);

			cents = new HartiganWongKMeans(k, vecsAndNoiseInThisRound)
					.getCentroids();
			time = System.nanoTime() - timestart;
			usedkB = (rt.totalMemory() - rt.freeMemory()) / 1024;
			wcsse = StatTests.WCSSE(cents, justvecsInThisRound);
			System.out.printf("%.4f\t%.1f\t\t%.1f\n", time / 1000000000f,
					wcsse, realwcsse);
		}
	}

}

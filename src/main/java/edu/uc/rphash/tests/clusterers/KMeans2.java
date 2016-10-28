package edu.uc.rphash.tests.clusterers;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import edu.uc.rphash.Centroid;
import edu.uc.rphash.Clusterer;
import edu.uc.rphash.Readers.RPHashObject;
import edu.uc.rphash.tests.StatTests;
import edu.uc.rphash.tests.generators.GenerateStreamData;
import edu.uc.rphash.util.VectorUtil;

public class KMeans2 implements Clusterer {

	private int n; // number of instances to classify
	private int d; // number of coordinates of each point
	private int k; // number of clusters
	private Centroid[] mu; // coordinate of means mu[j] of each cluster j
	private Vector<Centroid>[] w; // holds the points classified into each class
									// w[j]
	private Centroid[] sigma; // holds the standard deviation of each class i
	private float[] prior; // holds the prior of each class i
	// private float logLikelihood; // holds the log likelihood of each of the k
	// Gaussians
	private float MDL; // the minimum description length of the model
	private int numIterations = 200;

	private List<Centroid> centroids;
	private Centroid[] data;
	private int max_failed_runs = 200;

	public KMeans2(int getk, List<float[]> data) {
		this.data = new Centroid[data.size()];
		for (int i = 0; i < data.size(); i++) {
			this.data[i] = new Centroid(data.get(i));
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
	private void init(Centroid[] x, int k) {
		this.n = x.length;
		this.d = x[0].dimensions;
		this.k = k;
		this.mu = new Centroid[k];
		this.w = new Vector[k];
		this.sigma = new Centroid[k];
		this.prior = new float[k];

		Random r = new Random();
		// randomly assign a point in x to each mean mu[j]
		Centroid randomPoint;
		for (int j = 0; j < k; j++) {
			mu[j] = x[r.nextInt(n)];
			sigma[j] = new Centroid(new float[d]);
			prior[j] = 0;
		}
	}

	/**
	 * Runs the k-means algorithm with k clusters on the set of instances x Then
	 * find the quality of the model
	 **/
	public boolean run(Centroid[] x, int k, float epsilon) {
		float maxDeltaMeans = epsilon + 1;
		Centroid[] oldMeans = new Centroid[k];
		// initialize n,k,mu[j]
		init(x, k);
		// iterate until there is no change in mu[j]
		int iter = 0;
		while (maxDeltaMeans > epsilon && iter++ < numIterations) {
			// remember old values of the each mean
			for (int j = 0; j < k; j++) {
				oldMeans[j] = mu[j];

			}

			// classify each instance x[i] to its nearest class
			// first we need to clear the class array since we are reclassifying
			for (int j = 0; j < k; j++) {
				w[j] = new Vector<Centroid>(); // could use clear
			}

			for (int i = 0; i < n; i++) {
				classify(x[i]);
			}
			// recompute each mean, and check for empty clusters
			if (!computeMeansForEachClassW())
				return false;

			// compute the largest change in mu[j]
			maxDeltaMeans = maxDeltaMeans(oldMeans);
		}
		if (iter == numIterations) {
			System.out.println("Max Iterations Reached");
			return false;
		}
		return true;
	}

	/**
	 * Classifies the point x to the nearest class
	 **/
	private void classify(Centroid x) {
		float dist = 0;
		float smallestDist;
		int nearestClass;

		// compute the distance x is from mean mu[0]
		smallestDist = distance(x.centroid, mu[0].centroid);
		nearestClass = 0;

		// compute the distance x is from the other classes
		for (int j = 1; j < k; j++) {
			dist = distance(x.centroid, mu[j].centroid);
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

	Centroid subtract(Centroid px, Centroid py) {
		float[] x = px.centroid;
		float[] y = py.centroid;

		float[] ret = new float[x.length];
		if (x.length != y.length)
			return null;

		for (int i = 0; i < x.length; i++) {
			ret[i] = (x[i] - y[i]);
		}

		return new Centroid(1, ret);
	}

	Centroid add(Centroid px, Centroid py) {

		if (px.dimensions != py.dimensions)
			return null;

		float[] x = px.centroid;
		float[] y = py.centroid;

		float[] ret = new float[x.length];

		float total = px.count + py.count;

		for (int i = 0; i < x.length; i++) {
			ret[i] = (x[i] * px.count + y[i] * py.count) / total;
		}
		return new Centroid(1, ret);
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
	private boolean computeMeansForEachClassW() {
		// init the means to zero
		for (int j = 0; j < k; j++)
			mu[j] = new Centroid(0, new float[mu[j].dimensions]);

		// recompute the means of each cluster
		for (int j = 0; j < k; j++) {
			float[][] cwcss = computemeanAndWCSS(w[j]);
			if (cwcss == null)
				return false;
			mu[j].centroid = cwcss[1];
			mu[j].wcss = cwcss[2];
			mu[j].count = (int) cwcss[0][0];
		}
		return true;
	}

	/**
	 * Compute the maximum change over each mean mu[j]
	 **/
	private float maxDeltaMeans(Centroid[] oldMeans) {
		float delta;
		oldMeans[0] = subtract(oldMeans[0], mu[0]);

		float maxDelta = max(oldMeans[0].centroid);
		for (int j = 1; j < k; j++) {
			oldMeans[j] = subtract(oldMeans[j], mu[j]);
			delta = max(oldMeans[j].centroid);
			if (delta > maxDelta)
				maxDelta = delta;
		}
		return maxDelta;
	}

	public float getMDL() {
		return MDL;
	}

	public List<Centroid> getCentroids() {

		float epsilon = .001f;
		if (centroids != null) {
			return centroids;
		}

		double minwcss = Double.MAX_VALUE;
		List<Centroid> mincentroids = new ArrayList<>();
		int failedruns = 0;
		for (int j = 0; j < runs && failedruns < max_failed_runs;) {

			init(data, k);
			if (run(data, k, epsilon)) {
				centroids = new ArrayList<Centroid>(k);
				double twcss = 0.0;
				for (int i = 0; i < k; i++) {
					Centroid c = new Centroid(mu[i].centroid, 0);
					c.setWCSS(mu[i].wcss);
					c.setCount(mu[i].count);
					centroids.add(c);
				}
				if (twcss < minwcss) {
					minwcss = twcss;
					mincentroids = centroids;
				}
				j++;
			} else {
				failedruns++;
			}
		}
		if (failedruns == max_failed_runs){// try without weighting
			for(Centroid c : data){
				c.setCount(1);
				c.setWCSS(new float[c.dimensions]);
			}
			init(data, k);
			
			if (run(data, k, epsilon)) {
				centroids = new ArrayList<Centroid>(k);
				double twcss = 0.0;
				for (int i = 0; i < k; i++) {
					Centroid c = new Centroid(mu[i].centroid, 0);
					c.setWCSS(mu[i].wcss);
					c.setCount(mu[i].count);
					centroids.add(c);
				}
				if (twcss < minwcss) {
					minwcss = twcss;
					mincentroids = centroids;
				}
			}
			else
			{
				System.out
				.println("Maximum Failed Runs, try dropping epsilon change value in kmeanswcss");
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
		this.data = new Centroid[data.size()];
		for (int i = 0; i < data.size(); i++) {
			this.data[i] = data.get(i);
		}
	}

	@Override
	public void setRawData(List<float[]> data) {
		this.centroids = null;
		this.data = new Centroid[data.size()];
		for (int i = 0; i < data.size(); i++) {
			this.data[i] = new Centroid(data.get(i));
		}
	}

	@Override
	public void setK(int getk) {
		this.k = getk;
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

	public static float[][] merge(float cnt_1, float[] x_1, float[] var_1,
			float cnt_2, float[] x_2, float[] var_2) {
		float cnt_r = cnt_1 + cnt_2;
		float[] x_r = new float[x_1.length];
		float[] var_r = new float[x_1.length];
		for (int i = 0; i < x_1.length; i++) {
			x_r[i] = (cnt_1 * x_1[i] + cnt_2 * x_2[i]) / cnt_r;
			var_r[i] += cnt_1
					* ((x_r[i] - x_1[i]) * (x_r[i] - x_1[i]) + var_1[i])
					+ cnt_2
					* ((x_r[i] - x_2[i]) * (x_r[i] - x_2[i]) + var_2[i]);
			var_r[i] = var_r[i] / cnt_r;
		}

		float[][] ret = new float[3][];
		ret[0] = new float[1];
		ret[0][0] = cnt_r;
		ret[1] = x_r;
		ret[2] = var_r;
		return ret;
	}

	/**
	 * This method computes the mean and wcss of a weighted set of centroids The
	 * first index contains the mean vector of dimension d, the second index
	 * contains the wcss. and the third index contains the new merged count
	 * 
	 * @param c
	 * @return multi attribute array
	 */
	public static float[][] computemeanAndWCSS(List<Centroid> cs) {
		float[][] ret = new float[3][];
		int d = cs.get(0).centroid().length;
		Centroid c1 = cs.get(0);
		Centroid c2 = cs.get(1);
		ret = merge(c1.getCount(), c1.centroid(), new float[d], c2.getCount(),
				c2.centroid(), new float[d]);
		for (int i = 1; i < cs.size(); i++) {
			c2 = cs.get(i);
			ret = merge(ret[0][0], ret[1], ret[2], c2.getCount(),
					c2.centroid(), new float[d]);
		}

		return ret;
	}

	/**
	 * This method computes the mean and wcss of a weighted set of centroids The
	 * first index contains the mean vector of dimension d, the second index
	 * contains the wcss. and the third index contains the new merged count
	 * 
	 * @param c
	 * @return multi attribute array
	 */
	public static float[][] computemeanAndWCSS(Vector<Centroid> cs) {
		if (cs.size() < 2)
			return null;
		float[][] ret = new float[3][];
		int d = cs.get(0).dimensions;
		Centroid c1 = cs.get(0);
		Centroid c2 = cs.get(1);
		ret = merge(c1.count, c1.centroid, new float[d], c2.count, c2.centroid,
				new float[d]);
		for (int i = 1; i < cs.size(); i++) {
			c2 = cs.get(i);
			ret = merge(ret[0][0], ret[1], ret[2], c2.count, c2.centroid,
					new float[d]);
		}

		return ret;
	}
}
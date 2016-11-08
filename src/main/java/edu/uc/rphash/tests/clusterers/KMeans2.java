package edu.uc.rphash.tests.clusterers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import edu.uc.rphash.Centroid;
import edu.uc.rphash.Clusterer;
import edu.uc.rphash.Readers.RPHashObject;
import edu.uc.rphash.util.VectorUtil;

public class KMeans2 implements Clusterer {

	private int n; // number of instances to classify
	private int d; // number of coordinates of each point
	private int k; // number of clusters
	private Centroid[] means; // coordinate of means mu[j] of each cluster j
	private List<Integer>[] clustersOfVectorIndeces; // holds the points classified into each class
	private int numIterations = 100;

	private List<Centroid> centroids;
	private Centroid[] data;
	private int max_failed_runs = 10;

	public KMeans2(int getk, List<float[]> data) {
		this.data = new Centroid[data.size()];
		this.d = this.data[0].dimensions;
		for (int i = 0; i < data.size(); i++) {
			this.data[i] = new Centroid(1,data.get(i));
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
		this.means = new Centroid[k];
		this.clustersOfVectorIndeces = new ArrayList[k];

		List<Integer> initcent = KMeansPlusPlusDecorator.chooseInitialCenters(x, k);
		// randomly assign a point in x to each mean mu[j]		
		for (int j = 0; j < k; j++) {
			means[j] = new Centroid(new float[d]);
			means[j].wcss = new float[d];
			Centroid tmpptr =  x[initcent.get(j)];
			System.arraycopy(tmpptr.centroid, 0, means[j].centroid, 0, d);
			System.arraycopy(tmpptr.wcss, 0, means[j].wcss, 0, d);
			means[j].count = tmpptr.count;
		}
	}
	

	/**
	 * Runs the k-means algorithm with k clusters on the set of instances x Then
	 * find the quality of the model
	 **/
	public boolean run(Centroid[] x, int k, float epsilon) {
		float maxDeltaMeans = Float.MAX_VALUE;
		Centroid[] oldMeans = new Centroid[k];
		for(int i = 0;i<k;i++)
			oldMeans[i] = new Centroid(new float[d]);
		
		// initialize n,k,mu[j]
		init(x, k);
		
		// iterate until there is no change in mu[j]
		int iter = 0;
		
		while (maxDeltaMeans > epsilon && iter++ < numIterations) {
			
			// remember old values of each mean
			for (int j = 0; j < k; j++) 
			{
				System.arraycopy(means[j].centroid, 0, oldMeans[j].centroid, 0, d);
			}

			// classify each instance x[i] to its nearest class
			// first we need to clear the class array since we are reclassifying
			for (int j = 0; j < k; j++) {
				clustersOfVectorIndeces[j] = new ArrayList<Integer>(); // could use clear
			}

			
//			Centroid[] furtherestOut = new Centroid[k];
//			double[] furtherestOutDist = new double[k];
//			int leastidx = 0;
			for (int i = 0; i < n; i++) 
			{
				//classify seems correct
				double d = classify(i);
				
//				if(d>furtherestOutDist[leastidx]){
//					furtherestOutDist[leastidx] = d;
//					furtherestOut[leastidx] = x[i];
//					for(int j = 0;j<k;j++)
//					{
//						if(furtherestOutDist[j]<furtherestOutDist[leastidx]){
//							leastidx = j;
//						}
//					}
//				}
			}

			// recompute each mean, and check for empty clusters
			// note this is not sorted, the idea being that we don't
			// want to pick outliers. however RPHash has filtered 
			// outliers for us, so it's not a major problem
			
			List<Integer> emptyClusters = computeMeansForEachClassW();

			if(emptyClusters.size()>0){
				return false;
			}
//			if(emptyClusters.size()>0) {
				//if empty clusters, replace with highest wcss vectors
//					for(int j = 0;j<emptyClusters.size();j++){
//						means[j].centroid = furtherestOut[j].centroid;
//						means[j].wcss = furtherestOut[j].wcss;
//						means[j].count = furtherestOut[j].getCount();
//						
//					}
//			}
			
//			Arrays.asList(means).forEach(c->System.out.printf((int)c.count+","));
//			System.out.println( ":"+iter);
			
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
	private double classify(int x) {
		double dist = 0;
		double smallestDist;
		int nearestClass;

		// compute the distance x is from mean mu[0]
		smallestDist = distance(data[x].centroid, means[0].centroid);
		nearestClass = 0;

		// compute the distance x is from the other classes
		for (int j = 1; j < k; j++) {
			dist = distance(data[x].centroid, means[j].centroid);
			if (dist <= smallestDist) {
				smallestDist = dist;
				nearestClass = j;
			}
		}
		// classify x into class its nearest class
		clustersOfVectorIndeces[nearestClass].add(x);
		
		return smallestDist;
	}

	float distance(float[] x, float[] y) {
		float ret = 0.0f;
		if (x.length != y.length)
			return Float.MAX_VALUE;
		for (int i = 0; i < x.length; i++)
			ret += (x[i] - y[i]) * (x[i] - y[i]);
		return (float) Math.sqrt(ret);
	}

	float[] subtract(Centroid px, Centroid py) {
		float[] x = px.centroid;
		float[] y = py.centroid;

		float[] ret = new float[x.length];

		for (int i = 0; i < x.length; i++) {
			ret[i] = (x[i]*px.count - y[i]*py.count)/(px.count+py.count);
		}
		
		Centroid c = new Centroid((int)(px.getCount()+py.getCount()), ret);
		return c.centroid;
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
	private List<Integer> computeMeansForEachClassW() {
		List<Integer> ret = new ArrayList<>();


		// recompute the means of each cluster
		for (int j = 0; j < k; j++) 
		{
			CentroidTuple cwcss = computemeanAndWCSS(clustersOfVectorIndeces[j]);
			
			if (cwcss == null) {
				ret.add(j);
			} 
			else 
			{
				means[j].centroid = cwcss.mean;
				means[j].wcss = cwcss.wcss;
				means[j].count = cwcss.count;
			}
		}
		return ret;
	}

	/**
	 * Compute the maximum change over each mean mu[j]
	 **/
	private float maxDeltaMeans(Centroid[] oldMeans) {
		
		float[][] oldmeans =new float[oldMeans.length][];
		float delta;
		oldmeans[0] = subtract(oldMeans[0], means[0]);

		float maxDelta = max(oldmeans[0]);
		
		for (int j = 1; j < k; j++) {
			oldmeans[j] = subtract(oldMeans[j], means[j]);
			delta = max(oldmeans[j]);
			if (delta > maxDelta)
				maxDelta = delta;
		}
		return maxDelta;
	}
	
	class CentroidTuple{
		public CentroidTuple() 
		{
		}
		int count;
		float[] wcss;
		float[] mean;
	}

	public CentroidTuple merge(int cnt_1, float[] x_1, float[] var_1,
			int cnt_2, float[] x_2, float[] var_2) 
	{
		int cnt_r = cnt_1 + cnt_2;
		float[] x_r = new float[d];
		float[] var_r = new float[d];
		
		for (int i = 0; i < d; i++) {
			x_r[i] = (cnt_1 * x_1[i] + cnt_2 * x_2[i]);
			x_r[i] = x_r[i] /(float)cnt_r;
			
			var_r[i] = cnt_1*
					((x_r[i] - x_1[i]) * (x_r[i] - x_1[i]) + var_1[i])
					+ cnt_2 *
					 ((x_r[i] - x_2[i]) * (x_r[i] - x_2[i]) + var_2[i]);
			var_r[i] = var_r[i] / (float)cnt_r;
		}

		CentroidTuple cent = new CentroidTuple();
		cent.count = cnt_r;
		cent.mean = x_r;
		cent.wcss = var_r;
		return cent;
	}

	/**
	 * This method computes the mean and wcss of a weighted set of centroids The
	 * first index contains the new merged count the second index
	 * contains the mean vector of dimension d,  the third index contains the wcss.
	 * @param c
	 * @return multi attribute array
	 */
	public CentroidTuple computemeanAndWCSS(List<Integer> cs) {
		CentroidTuple ret;
		
		if (cs.size() == 0){
			return null;
		}
		
		Centroid c1 = data[cs.get(0)];
		
		if (cs.size() == 1){
			CentroidTuple cent = new CentroidTuple();
			cent.count = (int)c1.count;
			cent.wcss = c1.wcss;
			cent.mean = c1.centroid;
			return cent;
		}

		Centroid c2 = data[cs.get(1)];
		
		ret = merge((int)c1.getCount(), c1.centroid,c1.wcss, (int)c2.getCount(),
				c2.centroid,c2.wcss);
		
		for (int i = 1; i < cs.size(); i++) 
		{
			c2 = data[cs.get(i)];
			ret = merge(ret.count, ret.mean, ret.wcss, (int)c2.getCount(),
					c2.centroid, c2.wcss);
		}
		return ret;
	}
	
	public List<Centroid> getCentroids() {

		float epsilon = .00001f;

		double minwcss = Double.MAX_VALUE;
		List<Centroid> mincentroids = new ArrayList<>();
		int failedruns = 0;
		
		for (int j = 0; j < runs && failedruns < max_failed_runs;) {
			init(data, k);
			if (run(data, k, epsilon)) 
			{
				centroids = new ArrayList<Centroid>(k);
				double twcss = 0.0;
				for (int clusterid = 0; clusterid < k; clusterid++) 
				{
					Centroid c = new Centroid(means[clusterid].centroid, 0);
					c.wcss = means[clusterid].wcss;
					c.count = (long) means[clusterid].count;
					centroids.add(c);
					
					for (int dims = 0; dims < d; dims++)
						twcss += means[clusterid].wcss[dims];
				}
				if (twcss < minwcss) {
					minwcss = twcss;
					mincentroids = centroids;
				}
				j++;
			}
			else 
			{
				failedruns++;
			}
			
		}

//		if (failedruns == max_failed_runs) {// try without weighting
//			for (Centroid c : data) {
//				c.setCount(1);
//				c.setWCSS(new float[c.dimensions]);
//			}
//			init(data, k);
//
//			if (run(data, k, epsilon)) {
//				centroids = new ArrayList<Centroid>(k);
//				double twcss = 0.0;
//				for (int i = 0; i < k; i++) {
//					Centroid c = new Centroid(means[i].centroid, 0);
//					c.setWCSS(means[i].wcss);
//					c.setCount((long) means[i].count);
//					centroids.add(c);
//				}
//				if (twcss < minwcss) {
//					minwcss = twcss;
//					mincentroids = centroids;
//				}
//			} else {
//				System.out
//						.println("Maximum Failed Runs, try dropping epsilon change value in kmeanswcss");
//			}
//		}
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

	int runs = 10;

	@Override
	public boolean setMultiRun(int runs) {
		this.runs = runs;
		return true;
	}
}
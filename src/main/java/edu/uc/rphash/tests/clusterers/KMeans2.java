package edu.uc.rphash.tests.clusterers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import edu.uc.rphash.Centroid;
import edu.uc.rphash.Clusterer;
import edu.uc.rphash.RPHashStream;
import edu.uc.rphash.Readers.RPHashObject;
import edu.uc.rphash.tests.StatTests;
import edu.uc.rphash.tests.generators.GenerateStreamData;
import edu.uc.rphash.util.VectorUtil;

public class KMeans2 implements Clusterer {

	class PointND {

		private int dimension; // number of coordinates of a point
		private float[] coordinates; // the coordinates of a point
		private int count; // the coordinates of a point
		private float[] wcss;

		/**
		 * Create a point centered at the origin of the specific dimension
		 **/
		public PointND(int dimension) {
			this.count = 0;
			this.wcss = new float[dimension];
			this.dimension = dimension;
			coordinates = new float[dimension];
		}

		public PointND(float[] data, int count, float[] wcss) {
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
			this.count = p.getCount().intValue();
			this.wcss = p.getWCSS();
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
	private int numIterations;

	private List<Centroid> centroids;
	private PointND[] data;

	public KMeans2(int getk, List<float[]> data) {
		this.data = new PointND[data.size()];
		for (int i = 0; i < data.size(); i++) {
			this.data[i] = new PointND(data.get(i), 1, new float[data.get(0).length]);
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
		int iter = 0;
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

	PointND subtract(PointND px, PointND py) {
		float[] x = px.coordinates;
		float[] y = py.coordinates;

		float[] ret = new float[x.length];
		if (x.length != y.length)
			return null;

		for (int i = 0; i < x.length; i++) {
			ret[i] = (x[i] - y[i]);
		}

		return new PointND(ret, 1, new float[x.length]);
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
		return new PointND(ret, 1, new float[x.length]);
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
//			double wcss = 0.0;
//			// recompute the wcss of each cluster
//			for (int i = 0; i < numInstances; i++) {
//				instance = w[j].get(i);
//				for (float f : subtract(mu[j], instance).coordinates) {
//					wcss += f * f;
//				}
//			}
//			mu[j].wcss = (float) Math.sqrt(wcss) / (float) numInstances;
			
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

		for (int j = 0; j < runs; j++) {

			init(data, k);
			run(data, k, epsilon);
			centroids = new ArrayList<Centroid>(k);
			double twcss = 0.0;
			for (int i = 0; i < k; i++) {
				Centroid c = new Centroid(mu[i].coordinates, 0);
				c.setWCSS(mu[i].wcss);
				c.setCount(mu[i].count);
				centroids.add(c);
//				twcss += (mu[i].wcss);
			}
			if (twcss < minwcss) {
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
			this.data[i] = new PointND(data.get(i), 1, new float[data.get(0).length]);
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

	//TODO this merge is working
	public static float[][] merge(float cnt_1, float[] x_1, float[] var_1, float cnt_2,
			float[] x_2, float[] var_2) {
	    float cnt_r = cnt_1+cnt_2;
	    float[] x_r = new float[x_1.length];
	    float[] var_r = new float[x_1.length];
	    for(int i = 0;i<x_1.length;i++){
	        x_r[i] = (cnt_1*x_1[i] + cnt_2*x_2[i] ) / cnt_r;
	        var_r[i] +=  cnt_1*( (x_r[i]-x_1[i])*(x_r[i]-x_1[i]) +var_1[i]) 
	        		+  cnt_2*( (x_r[i]-x_2[i])*(x_r[i]-x_2[i]) + var_2[i]);
	        var_r[i] = var_r[i]/cnt_r;
	    }
	    
		float[][] ret = new float[3][];
	    ret[0] = new float[1];
	    ret[0][0] = cnt_r;
	    ret[1] = x_r;
	    ret[2] = var_r;
		return ret;
	}

//	public static float[][] merge(Centroid x_1, Centroid x_2) {
//		
//		int d = x_1.centroid().length;
//		double[] x1 = new double[d];
//		for(int i = 0; i< d;i++)x1[i] = x_1.centroid()[i];
//		double[] x2 = new double[d];
//		for(int i = 0; i< d;i++)x2[i] = x_2.centroid()[i];
//		
//		return merge(x1, x_1.getWCSS() , x_1.getCount(),
//					 x2, x_2.getWCSS()  , x_2.getCount());
//	}

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
		ret = merge(c1.getCount(),c1.centroid(),new float[d],c2.getCount(), c2.centroid(),new float[d]);
		for (int i = 1; i < cs.size(); i++) {
			c2 = cs.get(i);
			ret = merge(ret[0][0],ret[1],ret[2], c2.getCount(),c2.centroid(),new float[d]);
		}

		return ret;
	}

	public static void main(String[] args) {

		
		float[] x_1 = {55.085215700553867f, 57.422460638012225f, 57.572855373777195f, 56.483903938427062f, 58.427244931281827f} ;
		float[] var_1 ={890.10853249058232f, 739.96201001558484f, 825.3038578897914f, 875.91938138515559f, 830.95191282523501f};
		float cnt_1 = 4611;
		float[] x_2 = {28.24013827f,  28.41069007f,  25.01421101f,  18.6368656f,   33.49266143f} ;
		float[] var_2 ={0,0,0,0,0};
		float cnt_2 = 15;
	

		
		float[][] ret = merge(cnt_1, x_1, var_1,  cnt_2,  
				x_2, var_2);
		VectorUtil.prettyPrint(ret[0]);System.out.println();
		VectorUtil.prettyPrint(ret[1]);System.out.println();
		VectorUtil.prettyPrint(ret[2]);System.out.println();
		
		
		for(int k = 0;k<500;k++){
		List<Centroid> cs = new ArrayList<Centroid>();
		List<double[]> csfull = new ArrayList<double[]>();

		Random r = new Random();
		int n = 200;
		int d = 8;
		for (int i = 0; i < n; i++) {
			double[] randvecd = new double[d];
			float[] randvecf = new float[d];

			for (int j = 0; j < d; j++) {
				randvecf[j] = (float)r.nextFloat()*(float)r.nextGaussian()+r.nextInt(20);
				randvecd[j] = randvecf[j];
			}

			int ct = new Random().nextInt(10) + 1;
			Centroid tmp = new Centroid(randvecf, 0);
			tmp.setCount(ct);
			cs.add(tmp);

			// add a bunch of times
			for (int j = 0; j < ct; j++)
				csfull.add(randvecd);
		}
		float wcsstot = 0.0f;
		float[] wcss = KMeans2.computemeanAndWCSS(cs)[2];
		for(int i = 0;i<wcss.length;i++ )wcsstot+=wcss[i];
		System.out.print(wcsstot + "\t");
		wcsstot = 0.0f;
		double[] wcss2 = StatTests.WCSS(csfull);
		for(int i = 0;i<wcss2.length;i++ )wcsstot+=wcss2[i];
		System.out.print(wcsstot + "\n");
		}

	}

	private static void testkm2() {

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
			// System.out.printf("%d\t%d\t%.4f\t%.1f\t\t", i, usedkB,
			// time / 1000000000f, wcsse);

			// cents = new HartiganWongKMeans(k, vecsAndNoiseInThisRound)
			// .getCentroids();
			// time = System.nanoTime() - timestart;
			// usedkB = (rt.totalMemory() - rt.freeMemory()) / 1024;
			// wcsse = StatTests.WCSSECentroidsFloat(cents,
			// justvecsInThisRound);
			System.out.println(wcsse);
			// System.out.printf("%.4f\t%.1f\t\t%.1f\n", time / 1000000000f,
			// wcsse, realwcsse);
		}
	}

}

package edu.uc.rphash.tests.clusterers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D;
import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.ml.clustering.DoublePoint;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.apache.commons.math3.ml.distance.EuclideanDistance;
import org.apache.commons.math3.random.RandomAdaptor;
import org.apache.commons.math3.random.RandomGenerator;
//import org.apache.commons.math3.userguide.ExampleUtils.ExampleFrame; 
import org.apache.commons.math3.util.FastMath;

import edu.uc.rphash.Centroid;
import edu.uc.rphash.Readers.RPHashObject;
import edu.uc.rphash.tests.generators.GenerateData;

import org.apache.commons.math3.ml.distance.ManhattanDistance;


public class KMeansPlusPlus implements edu.uc.rphash.Clusterer {
	public KMeansPlusPlus() {

	}

	public KMeansPlusPlus(List<float[]> data, int k) {
		this.setRawData(data);
		this.setK(k);
	}

	public static Vector2D generateNoiseVector(NormalDistribution distribution) {
		return new Vector2D(distribution.sample(), distribution.sample());
	}

	public static List<DoublePoint> normalize(final List<Vector2D> input,
			double minX, double maxX, double minY, double maxY) {
		double rangeX = maxX - minX;
		double rangeY = maxY - minY;
		List<DoublePoint> points = new ArrayList<DoublePoint>();
		for (Vector2D p : input) {
			double[] arr = p.toArray();
			arr[0] = (arr[0] - minX) / rangeX * 2 - 1;
			arr[1] = (arr[1] - minY) / rangeY * 2 - 1;
			points.add(new DoublePoint(arr));
		}
		return points;
	}

	public static List<Vector2D> makeCircles(int samples, boolean shuffle,
			double noise, double factor, final RandomGenerator random) {
		if (factor < 0 || factor > 1) {
			throw new IllegalArgumentException();
		}

		NormalDistribution dist = new NormalDistribution(random, 0.0, noise,
				1e-9);

		List<Vector2D> points = new ArrayList<Vector2D>();
		double range = 2.0 * FastMath.PI;
		double step = range / (samples / 2.0 + 1);
		for (double angle = 0; angle < range; angle += step) {
			Vector2D outerCircle = new Vector2D(FastMath.cos(angle),
					FastMath.sin(angle));
			Vector2D innerCircle = outerCircle.scalarMultiply(factor);

			points.add(outerCircle.add(generateNoiseVector(dist)));
			points.add(innerCircle.add(generateNoiseVector(dist)));
		}

		if (shuffle) {
			Collections.shuffle(points, new RandomAdaptor(random));
		}

		return points;

	}

	public List<Centroid> getCentroids() { // to be completed

		KMeansPlusPlusClusterer<DoublePoint> km = new KMeansPlusPlusClusterer<DoublePoint>(
				this.k ,50000 );

		List<Centroid> C = new ArrayList<Centroid>(); 
	//	System.out.println(" Entering getCentroids 1");
		
	//	System.out.println("The whole  list for the gen1 is :" + gen1);
		
		List<CentroidCluster<DoublePoint>> clusters = km.cluster(this.gen1);
		
	//	System.out.println(" Entering getCentroids done the clustering ");

		for (Cluster<DoublePoint> c : clusters) // from Class clusterable to
												// centroid
		{
			float[] floatArray = new float[c.getPoints().get(0).getPoint().length];
			for (DoublePoint dp : c.getPoints()) {
				for (int i = 0; i < dp.getPoint().length; i++) {
					floatArray[i] += (float) dp.getPoint()[i];
				}
			}
			float centsize = c.getPoints().size();
			int dim = c.getPoints().get(0).getPoint().length;
			for (int j = 0; j < dim; j++) {
				floatArray[j] = floatArray[j] / centsize;
			}
			C.add(new Centroid(floatArray, 0)); // setting the projection id = 0
		}
		return C;
	}

	// abstract RPHashObject getParam();
	@Override
	public RPHashObject getParam() { // to be completed
		return null;
	}

	// void setWeights(List<Float> counts);

	public void setWeights(List<Float> weights) { // to be completed
		return;
	}

	// void setData(List<float[]> centroids);
	private List<DoublePoint> gen1;
	private int k;

	@Override
	public void setRawData(List<float[]> data) {

		gen1 = new ArrayList<DoublePoint>(); // converting the data generated to
												// DoublePoint
		for (float[] c : data) {
			double[] tmp = new double[c.length];
			for (int i = 0; i < c.length; i++)
				tmp[i] = c[i];// for centroid type c.Centroid[i];
			gen1.add(new DoublePoint(tmp));
			//System.out.println("The Raw data is coverted in setRawData ");
		}
		// to be completed
		/*
		 * this.data = data;
		 * 
		 * this.n = data.get(0).length; this.m = data.size(); this.a = new
		 * double[m * n]; this.c = new double[k * n]; this.nc = new int[k];
		 * this.wss = new double[k];
		 */
		// weights = new Float[m];
		// Collections.shuffle(data);

	}

	@Override
	public void setData(List<Centroid> centroids) {
		ArrayList<float[]> data = new ArrayList<float[]>(centroids.size());
		for (Centroid c : centroids)
			data.add(c.centroid());
		setRawData(data);
		System.out.println("The Raw data is set in setdata ");
	}

	public void setK(int k) { // to be completed
		this.k = k;
	}

	@Override
	public void reset(int randomseed) {

	}

	@Override
	public boolean setMultiRun(int runs) {
		return false;
	}

	// testing the algorithm :

	public static void main(String[] args) {

		// int nSamples = 1500;
		// RandomGenerator rng = new Well19937c(0);
		// List<Vector2D> datasets = makeCircles(nSamples, true, 0.04, 0.5,
		// rng);
		// List<DoublePoint> data = normalize(datasets, -1, 1, -1, 1);

		GenerateData gen = new GenerateData(10, 1000, 8); // the data generator
															// of rhpash

		List<DoublePoint> gen1 = new ArrayList<DoublePoint>(); // converting the
																// data
																// generated to
																// DoublePoint
		for (float[] c : gen.data) {
			double[] tmp = new double[c.length];
			for (int i = 0; i < c.length; i++)
				tmp[i] = c[i];// for centroid type c.Centroid[i];
			gen1.add(new DoublePoint(tmp));

		}

		// System.out.println("The whole  list for the gen1 is :" + gen1);

		List<DoublePoint> data = gen1;

		KMeansPlusPlusClusterer km = new KMeansPlusPlusClusterer<DoublePoint>(3 ,-1 ,new EuclideanDistance());// TODO
																					// code
																					// application
																					// logic
																					// here

		int k = km.getK();
		System.out.println("The Value of k is :" + k);

		List<Cluster> kmc = km.cluster(data);
		int d = kmc.size();
		System.out.println("The size of the list i.e no. of clusters are :" + d);

		// System.out.println(kmc.getClass());

		// System.out.println("The cluster id 1 :" + kmc.get(0));
		// System.out.println("The cluster id 2 :" + kmc.get(1));
		Cluster clkm1 = kmc.get(0);
		Cluster clkm2 = kmc.get(1);
		List<?> clusterkm1 = clkm1.getPoints();
		List<?> clusterkm2 = clkm2.getPoints();
		// System.out.println("The points in clusterKM1:" + clusterkm1);
		// System.out.println("The points in clusterKM2 :" + clusterkm2);

		CentroidDBScan n = new CentroidDBScan(); // though the name is
													// CentroidDBScan it is a
													// general class that has
													// the method to compute the
													// centroid of a cluster.
		System.out.println("The centroid in cluster1:" + n.centroidOf(clkm1));
		System.out.println("The centroid in cluster2:" + n.centroidOf(clkm2));

		List<Clusterable> CentroidsKmpp = new ArrayList<Clusterable>();

		CentroidDBScan iter_obj = new CentroidDBScan(); // creating the list of
														// all centroids from
														// the partions of kmpp
														// .
		for (int i = 0; i < kmc.size(); i++) {

			Clusterable cent = iter_obj.centroidOf(kmc.get(i));
			CentroidsKmpp.add(cent);

		}

		System.out.println("The whole  list of  the centroids are :"
				+ CentroidsKmpp); // output centroids from apache func

		List<Centroid> C = new ArrayList<Centroid>(); // converting to
														// List<Centroid>
														// getCentroids() to
														// match RPHash

		for (Clusterable c : CentroidsKmpp) // from Class clusterable to
											// centroid
		{
			double[] temp = c.getPoint();

			float[] floatArray = new float[temp.length];
			for (int i = 0; i < temp.length; i++) {
				floatArray[i] = (float) temp[i];
			}

			C.add(new Centroid(floatArray, 0)); // setting the projection id = 0

		}

		for (Centroid iter : C) { // output centroids after conversion to RPHash
									// Centroid
			float[] toprint = iter.centroid();
			System.out.println(Arrays.toString(toprint));
		}

	}

}

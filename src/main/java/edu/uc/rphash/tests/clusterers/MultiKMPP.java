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

//import org.apache.commons.math3.ml.distance.ManhattanDistance;
//import org.apache.commons.math3.exception.ConvergenceException;
//import org.apache.commons.math3.exception.MathIllegalArgumentException;
//import org.apache.commons.math3.ml.clustering.evaluation.ClusterEvaluator;
//import org.apache.commons.math3.ml.clustering.evaluation.SumOfClusterVariances;

import org.apache.commons.math3.stat.descriptive.moment.Variance;
import org.apache.commons.math3.ml.distance.DistanceMeasure;





public class MultiKMPP implements edu.uc.rphash.Clusterer {
	public MultiKMPP() {
				
		
	}

	public MultiKMPP(List<float[]> data, int k) {
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
		
		List<Centroid> C = new ArrayList<Centroid>(); 
		
		List<CentroidCluster<DoublePoint>> bestcluster = null;
		double bestVarianceSum = Double.POSITIVE_INFINITY;
		
		for (int i = 1 ; i <= 10 ; i++) {
			
		
			KMeansPlusPlusClusterer<DoublePoint> km = new KMeansPlusPlusClusterer<DoublePoint>(
				this.k ,50000 );
		
	//	System.out.println(" Entering getCentroids 1");		
	//	System.out.println("The whole  list for the gen1 is :" + gen1);
		
		List<CentroidCluster<DoublePoint>> clusters = km.cluster(this.gen1);
		 
		double varianceSum=0.0;	
		
		CentroidDBScan n = new CentroidDBScan();        // to compute the centroid of the kmpp clusters 
				
	//	final Clusterable center = n.centroidOf(clusters.get(0))	;
					
		for (final Cluster<DoublePoint> cluster : clusters) {
            if (!cluster.getPoints().isEmpty()) {

                final Clusterable center = n.centroidOf(cluster);

   //             System.out.println((center));
                
                // compute the distance variance of the current cluster
                final Variance stat = new Variance();
                for (final DoublePoint point : cluster.getPoints()) {
                   
                	stat.increment(distance(point, center));
                }
                varianceSum += stat.getResult();

            }
        }
				
		
		if (varianceSum < bestVarianceSum ){
		   bestVarianceSum = varianceSum;
		   bestcluster = clusters;
//		   System.out.println((bestVarianceSum));
				
		}	
//		     System.out.println((varianceSum));	
		}
	
	//	System.out.println(" Entering getCentroids done the clustering ");

		for (Cluster<DoublePoint> c : bestcluster) // from Class clusterable to
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

	
	
	protected double distance(final Clusterable p1, final Clusterable p2) {
		
		DistanceMeasure measure = new EuclideanDistance();
		
        return measure.compute(p1.getPoint(), p2.getPoint());
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
		//System.out.println("The Raw data is set in setdata ");
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


		GenerateData  gen = new GenerateData(3, 1000, 5); // the data generator of rhpash

		MultiKMPP km = new MultiKMPP (gen.data , 3);
		

		
	//	for (Centroid iter : km.getCentroids()) { // output centroids 
	//		float[] toprint = iter.centroid();
	//		System.out.println(Arrays.toString(toprint));
		
	//	}													
		


	}
}

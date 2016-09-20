package edu.uc.rphash.tests.clusterers;

import java.util.List; 
import java.util.ArrayList; 
import java.util.Collections;  

import org.apache.commons.math3.distribution.NormalDistribution; 
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D; 
import org.apache.commons.math3.ml.clustering.CentroidCluster; 
import org.apache.commons.math3.ml.clustering.Cluster; 
import org.apache.commons.math3.ml.clustering.Clusterable; 
import org.apache.commons.math3.ml.clustering.Clusterer; 
import org.apache.commons.math3.ml.clustering.DoublePoint; 
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer; 
import org.apache.commons.math3.random.RandomAdaptor; 
import org.apache.commons.math3.random.RandomGenerator; 
import org.apache.commons.math3.random.SobolSequenceGenerator; 
import org.apache.commons.math3.random.Well19937c; 
//import org.apache.commons.math3.userguide.ExampleUtils.ExampleFrame; 
import org.apache.commons.math3.util.FastMath; 
import org.apache.commons.math3.util.Pair; 
import org.apache.commons.math3.ml.distance.DistanceMeasure; 
import org.apache.commons.math3.ml.distance.EuclideanDistance;

import edu.uc.rphash.Centroid;
import edu.uc.rphash.Readers.RPHashObject;


public class KMeansPlusPlus  implements edu.uc.rphash.Clusterer{

	    
	      public static Vector2D generateNoiseVector(NormalDistribution distribution) { 
	        return new Vector2D(distribution.sample(), distribution.sample()); 
	    } 
	     
	    public static List<DoublePoint> normalize(final List<Vector2D> input, double minX, double maxX, double minY, double maxY) { 
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
	     
	    
	   public static List<Vector2D> makeCircles(int samples, boolean shuffle, double noise, double factor, final RandomGenerator random) { 
	        if (factor < 0 || factor > 1) { 
	            throw new IllegalArgumentException(); 
	        } 
	         
	        NormalDistribution dist = new NormalDistribution(random, 0.0, noise, 1e-9); 
	 
	        List<Vector2D> points = new ArrayList<Vector2D>(); 
	        double range = 2.0 * FastMath.PI; 
	        double step = range / (samples / 2.0 + 1); 
	        for (double angle = 0; angle < range; angle += step) { 
	            Vector2D outerCircle = new Vector2D(FastMath.cos(angle), FastMath.sin(angle)); 
	            Vector2D innerCircle = outerCircle.scalarMultiply(factor); 
	             
	            points.add(outerCircle.add(generateNoiseVector(dist))); 
	            points.add(innerCircle.add(generateNoiseVector(dist))); 
	        } 
	         
	        if (shuffle) { 
	            Collections.shuffle(points, new RandomAdaptor(random)); 
	        } 
	 
	        return points;           
	        
	   }  
	   
	 //List<float[]> getCentroids();
	   
	   public List<Centroid> getCentroids() {   // to be completed
			return null ;
		}
		
		
//		abstract RPHashObject getParam();    
	   @Override
		public RPHashObject getParam() {      // to be completed    
			return null;
		}
		
		
//		void setWeights(List<Float> counts);
		
		public void setWeights(List<Float> weights) {      // to be completed
			return;
		}

		
		//void setData(List<float[]> centroids);
		
		@Override
		public void setRawData(List<float[]> data) {           // to be completed
		/*  this.data = data;
			
			this.n = data.get(0).length;
			this.m = data.size();
			this.a = new double[m * n];
			this.c = new double[k * n];
			this.nc = new int[k];
			this.wss = new double[k];      */
			// weights = new Float[m];
			// Collections.shuffle(data);

		}
		@Override
		public void setData(List<Centroid> centroids) {
			ArrayList<float[]> data = new ArrayList<float[]>(centroids.size());
			for(Centroid c : centroids)data.add(c.centroid());
			setRawData(data);
		}

		
		
		public void setK(int k) {                         // to be completed
			return;
		}
		
		@Override
		public void reset(int randomseed) {
			
		}
	
// testing the algorithm :
		
	    public static void main(String[] args) {
  
	    int nSamples = 1500;
	   
	    RandomGenerator rng = new Well19937c(0);
	    
	     
	    List<Vector2D> datasets =  makeCircles(nSamples, true, 0.04, 0.5, rng);
	    

	    List<DoublePoint> data = normalize(datasets, -1, 1, -1, 1);
	    
	         
	       KMeansPlusPlusClusterer km = new KMeansPlusPlusClusterer<DoublePoint>(2);// TODO code application logic here
	        
	      
	       int k = km.getK();
	       System.out.println("The Value of k is :" + k );
	   	 
	               
	       List<Cluster> kmc   = km.cluster(data) ;
	        int d = kmc.size();
	        System.out.println("The size of the list is :" + d);
	        
	        System.out.println(kmc.getClass());
	        
	         System.out.println("The cluster id 1 :" + kmc.get(0));
	         System.out.println("The cluster id 2 :" + kmc.get(1));
	          Cluster clkm1 = kmc.get(0);
	          Cluster clkm2 = kmc.get(1);
	          List<?> clusterkm1 = clkm1.getPoints();
	          List<?> clusterkm2 = clkm2.getPoints();
	          System.out.println("The points in clusterKM1:" + clusterkm1);
	          System.out.println("The points in clusterKM2 :" + clusterkm2);
	        
	       
	         CentroidDBScan n = new CentroidDBScan() ;                              // though the name is CentroidDBScan it is a general class that has the method to compute the centroid of a cluster.
	         System.out.println("The centroid in cluster1:" + n.centroidOf(clkm1)); 
	         System.out.println("The centroid in cluster2:" + n.centroidOf(clkm2));  
	    }
	    
	}




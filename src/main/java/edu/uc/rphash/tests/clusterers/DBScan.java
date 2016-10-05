package edu.uc.rphash.tests.clusterers;


import org.apache.commons.math3.ml.clustering.DBSCANClusterer;

import java.util.List; 
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;  
import org.apache.commons.math3.distribution.NormalDistribution; 
import org.apache.commons.math3.geometry.euclidean.twod.Vector2D; 
import org.apache.commons.math3.ml.clustering.CentroidCluster; 
import org.apache.commons.math3.ml.clustering.Cluster; 
import org.apache.commons.math3.ml.clustering.Clusterable; 
import org.apache.commons.math3.ml.clustering.Clusterer; 
import org.apache.commons.math3.ml.clustering.DoublePoint; 
//import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer; 
import org.apache.commons.math3.random.RandomAdaptor; 
import org.apache.commons.math3.random.RandomGenerator; 
//import org.apache.commons.math3.random.SobolSequenceGenerator; 
import org.apache.commons.math3.random.Well19937c; 
//import org.apache.commons.math3.userguide.ExampleUtils.ExampleFrame; 
import org.apache.commons.math3.util.FastMath; 
import org.apache.commons.math3.util.Pair; 
import org.apache.commons.math3.ml.distance.DistanceMeasure; 
import org.apache.commons.math3.ml.distance.EuclideanDistance;

import edu.uc.rphash.Centroid;
import edu.uc.rphash.Readers.RPHashObject;
import edu.uc.rphash.tests.generators.GenerateData;


public class DBScan implements edu.uc.rphash.Clusterer{

    
  /*    public static Vector2D generateNoiseVector(NormalDistribution distribution) { 
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
       
        
   }     */
	
	
	@Override
	public void reset(int randomseed) {					// not needed
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean setMultiRun(int runs) {              // not needed
		// TODO Auto-generated method stub
		return false;
	}
	
	
//	void setWeights(List<Float> counts);
	
	public void setWeights(List<Float> weights) {       // not needed
		return;
	}

	
	
	public void setK(int k) {                           // not needed
		return;
	}
	
	
	
	@Override
	public void setRawData(List<float[]> centroids) {				// to be completed  
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setData(List<Centroid> centroids) {					// to be completed  
		// TODO Auto-generated method stub
		
	}

	//List<float[]> getCentroids();
	   
	   public List<Centroid> getCentroids() {  						 // to be completed
			return null ;
		}
		
		
//		abstract RPHashObject getParam();    
	   @Override
		public RPHashObject getParam() {     						 // to be completed    
			return null;
		}
		
	
	
    public static void main(String[] args) {
      
  //  int nSamples = 2000;
   
  //  RandomGenerator rng = new Well19937c(0);
    
                                                       GenerateData gen = new GenerateData(20,500,5);         // the generator of rhpash
                                                       
                                                      
                                                       List<DoublePoint> gen1 = new ArrayList<DoublePoint>(); 
                                                        for (float[] c:gen.data)
                                                        { 
                                                        	double[] tmp = new double[c.length];
                                                        	for(int i = 0 ;i<c.length;i++)tmp[i]=c[i];// for centroid type c.Centroid[i];
                                                        	gen1.add(new DoublePoint(tmp));
                                                      
                                                        }
                                                       
                                                       
                                                     
                                                     System.out.println("The whole  list for the gen1 is :" + gen1);
     
                                                   List<DoublePoint> data =gen1;
                                                       
     
   // List<Vector2D> datasets =  makeCircles(nSamples, true, 0.04, 0.5, rng);
    
  //  List<DoublePoint> data = normalize(datasets, -1, 1, -1, 1);
    
  //  int size_of_list_for_data = data.size();
    
  //  System.out.println("The size of the list for the data is :" + size_of_list_for_data);
    
  //  System.out.println("The whole  list for the data is :" + data);
    
    
    
    DBSCANClusterer db= new DBSCANClusterer<DoublePoint>(0.1,3);// TODO code application logic here . have to convert this to list of float arrays : List<float[]> getCentroids()
          
    
       double eps = db.getEps();
       int MinPoints = db.getMinPts();
       System.out.println("The Value of Epsilon is :" + eps);
       System.out.println("The Value of Min Points is :" + MinPoints);
 
               
       List<Cluster> abc   = db.cluster(data) ;    // abc is the list of clusters
       
        
        int a = abc.size();
        System.out.println("The size of the list is :" + a);
        
        System.out.println(abc.getClass());
        
         System.out.println("The cluster id 1 :" + abc.get(0));
         System.out.println("The cluster id 2 :" + abc.get(1));
          Cluster cl1 = abc.get(0);
          Cluster cl2 = abc.get(1);
          List<?> cluster1 = cl1.getPoints();
          List<?> cluster2 = cl2.getPoints();
    //      System.out.println("The points in cluster1:" + cluster1);
    //      System.out.println("The points in cluster2 :" + cluster2);
          
          
          CentroidDBScan m = new CentroidDBScan() ;
//          System.out.println("The centroid in cluster1:" + m.centroidOf(cl1)); 
          System.out.println("The centroid in cluster2:" + m.centroidOf(cl2));
          
          System.out.println("The centroid in cluster1:" + m.centroidOf(abc.get(0)));
          
         
          List<DoublePoint>CentroidDBScan1  = new ArrayList<DoublePoint>();
          
          List<Clusterable>Centroids  = new ArrayList<Clusterable>();
          
               
          
          CentroidDBScan n = new CentroidDBScan() ; 
          for (int i=0;i < abc.size();i++ )
          { 
        	         	          	          	  
        	  Clusterable cent =  n.centroidOf(abc.get(i));        	        	  
        	  Centroids.add(cent); 								// convert to List<Centroid> getCentroids();
        	  
        	  
  //       	  CentroidDBScan1.add(new Clusterable (cent));
          }
          
   
          
          
          List<Centroid>C =  new ArrayList<Centroid>(); 
          
//           for (float[] c:)
           { 
           	
           } 
               
    }


    
}

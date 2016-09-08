package edu.uc.rphash.tests.clusterers;


import org.apache.commons.math3.ml.clustering.DBSCANClusterer;

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
//import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer; 
import org.apache.commons.math3.random.RandomAdaptor; 
import org.apache.commons.math3.random.RandomGenerator; 
//import org.apache.commons.math3.random.SobolSequenceGenerator; 
import org.apache.commons.math3.random.Well19937c; 
//import org.apache.commons.math3.userguide.ExampleUtils.ExampleFrame; 
import org.apache.commons.math3.util.FastMath; 
//import org.apache.commons.math3.util.Pair; 



public class DBScan {

    
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
  
    public static void main(String[] args) {
      
    int nSamples = 1500;
   
    RandomGenerator rng = new Well19937c(0);
    
    List<Vector2D> datasets =  makeCircles(nSamples, true, 0.04, 0.5, rng);

    List<DoublePoint> data = normalize(datasets, -1, 1, -1, 1);
    
    DBSCANClusterer db= new DBSCANClusterer<DoublePoint>(0.1,3);// TODO code application logic here
        
       double eps = db.getEps();
       int MinPoints = db.getMinPts();
       System.out.println("The Value of Epsilon is :" + eps);
       System.out.println("The Value of Min Points is :" + MinPoints);
 
               
       List<Cluster> abc   = db.cluster(data) ;
        int a = abc.size();
        System.out.println("The size of the list is :" + a);
        
        System.out.println(abc.getClass());
        
         System.out.println("The cluster id 1 :" + abc.get(0));
         System.out.println("The cluster id 2 :" + abc.get(1));
          Cluster cl1 = abc.get(0);
          Cluster cl2 = abc.get(1);
          List<?> cluster1 = cl1.getPoints();
          List<?> cluster2 = cl2.getPoints();
          System.out.println("The points in cluster1:" + cluster1);
          System.out.println("The points in cluster2 :" + cluster2);
        
        
    }
    
}

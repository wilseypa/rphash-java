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
import org.apache.commons.math3.random.RandomAdaptor; 
import org.apache.commons.math3.random.RandomGenerator; 
import org.apache.commons.math3.random.Well19937c;  
import org.apache.commons.math3.util.FastMath; 
import org.apache.commons.math3.util.Pair; 
import org.apache.commons.math3.ml.distance.DistanceMeasure; 
import org.apache.commons.math3.ml.distance.EuclideanDistance;

import edu.uc.rphash.Centroid;
import edu.uc.rphash.Readers.RPHashObject;
import edu.uc.rphash.tests.generators.GenerateData;

import java.util.Arrays;

public class DBScan implements edu.uc.rphash.Clusterer{

    
  public DBScan() {
	  
  }
  
 /* public DBScan(List<float[]> , double eps , int minPoints) {
	  
	  
	  this.setRawData(data);
	  this.setEps(eps); 
	  this.setminpoints(minPoints);
	   
	  
  }
    */    
  
 public DBScan(List<float[]> data ) {
	   
	  this.setRawData(data); 
	  
  }
	

 public List<Centroid> getCentroids() { // to be completed

	 double eps = 2;
	 int minPoints = 3;
	 
	 DBSCANClusterer<DoublePoint> db = new DBSCANClusterer<DoublePoint>(eps , minPoints );

		List<Centroid> C = new ArrayList<Centroid>(); 
	//	System.out.println(" Entering getCentroids 1");
		
	//	System.out.println("The whole  list for the gen1 is :" + gen1);
		
		List<Cluster<DoublePoint>> clusters = db.cluster(this.gen1);
		
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


	public void setWeights(List<Float> weights) { // to be completed
		return;
	}

	
	private List<DoublePoint> gen1;
	private int minPoints;
	private double eps ;
	
	
	
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
		return;
	}

/*	public void setminpoints(int k) { // to be completed
		this.k = k;
	}
	
	public void seteps(int k) { // to be completed
		this.k = k;
	}*/
	
	
	
	@Override
	public void reset(int randomseed) {

	}

	@Override
	public boolean setMultiRun(int runs) {
		return false;
	}
	
	
	
	
	
    public static void main(String[] args) {
      
  
    	GenerateData  gen = new GenerateData(20,500,5); // the data generator of rhpash
    	

		DBScan db = new DBScan (gen.data );
		System.out.println("number of centroids  = "+ (db.getCentroids()).size());
		
		
		for (Centroid iter : db.getCentroids()) { // output centroids 
			float[] toprint = iter.centroid();
			System.out.println("333333333333333");
			
			System.out.println(Arrays.toString(toprint));
		
		}													
		
		
    
}
}


//xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx please ignore this xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx



//xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx please ignore this xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx



//xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx please ignore this xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx



//xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx please ignore this xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx


/*package edu.uc.rphash.tests.clusterers;


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

import java.util.Arrays;
public class DBScan implements edu.uc.rphash.Clusterer{

    
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
    
    
    
    DBSCANClusterer db= new DBSCANClusterer<DoublePoint>(0.2,4);// TODO code application logic here . have to convert this to list of float arrays : List<float[]> getCentroids()
          
    
       double eps = db.getEps();
       int MinPoints = db.getMinPts();
       System.out.println("The Value of Epsilon is :" + eps);
       System.out.println("The Value of Min Points is :" + MinPoints);
 
               
       List<Cluster> abc   = db.cluster(data) ;    // abc is the list of clusters
       
        
        int a = abc.size();
        System.out.println("The size of the list is :" + a);
        
   //     System.out.println(abc.getClass());
        
   //      System.out.println("The cluster id 1 :" + abc.get(0));
   //      System.out.println("The cluster id 2 :" + abc.get(1));
          Cluster cl1 = abc.get(0);
          Cluster cl2 = abc.get(1);
          List<?> cluster1 = cl1.getPoints();
          List<?> cluster2 = cl2.getPoints();
    //      System.out.println("The points in cluster1:" + cluster1);
    //      System.out.println("The points in cluster2 :" + cluster2);
          
          
          CentroidDBScan m = new CentroidDBScan() ;
//          System.out.println("The centroid in cluster1:" + m.centroidOf(cl1)); 
          System.out.println("The centroid in cluster1:" + m.centroidOf(abc.get(0)));
          System.out.println("The centroid in cluster2:" + m.centroidOf(cl2));
      
          
                   
         
          List<DoublePoint>CentroidDBScan1  = new ArrayList<DoublePoint>();
          
          List<Clusterable>Centroids  = new ArrayList<Clusterable>();
          
               
          
          CentroidDBScan n = new CentroidDBScan() ; 
          for (int i=0;i < abc.size();i++ )
          { 
        	         	          	          	  
        	  Clusterable cent =  n.centroidOf(abc.get(i));        	        	  
        	  Centroids.add(cent); 								// convert to List<Centroid> getCentroids();
        	  
  
          }
                  
          
          System.out.println("The whole  list of  the centroids are :" + Centroids); 
         
          List<Centroid>C =  new ArrayList<Centroid>(); 
          
          
           for ( Clusterable c: Centroids )
           { 
           	   double[] temp =	c.getPoint()	;									// from clusterable to centroid
        	  
           	float[] floatArray = new float[temp.length];
           	for (int i = 0 ; i < temp.length; i++)
           	{
           	    floatArray[i] = (float) temp[i];
           	}
           	   
           	   
        	  C.add(new Centroid(floatArray,0)) ;    // setting  the projection id = 0
           
           	
           } 
           
           
 //          System.out.println("The whole  list of  the centroid ids are :" + C); 
           
           for (Centroid k:C) { 
        	  float[] toprint= k.centroid();         
        	  System.out.println(Arrays.toString(toprint)); }
           
               
    }


    
}
*/

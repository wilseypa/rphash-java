package edu.uc.rphash.centroidTracker;

import java.util.List;

import edu.uc.rphash.Centroid;
import edu.uc.rphash.Readers.RPHashObject;
import edu.uc.rphash.frequentItemSet.KHHCentroidCounter;
import edu.uc.rphash.lsh.LSH;

/*
1.	Check the number of previous centroids and current centroids.
2.	Case i. If the previous centroids = current centroids 
Compute a distance matrix ( Euclidean, Cosine ) between the two sets of centroids.
Assign each one to its closest one. 
  Case ii. If the previous centroids > current centroids
	Compute the distance matrix between two sets.
  Case a. find closest one and assign movements. Find 2nd closest ones to them and assign them merged.
  
  Case iii. If Previous centroids < current centroids
Compute the distance matrix between two sets.
  Case a. find closest one and assign movements and declare the remaining as new.
*/

public class trackCentroids implements Runnable {

//	private float[] vec;
//	private float[][] dismtx;

	public trackCentroids(float[] vec, LSH[] lshfuncs) {

	}

    	// This function returns the square of the euclidean distance.	
    	public static float distancesq(float[] x, float[] y) {
    		if (x.length < 1)
    			return Float.MAX_VALUE;
    		if (y.length < 1)
    			return Float.MAX_VALUE;
    		float dist = (x[0] - y[0]) * (x[0] - y[0]);
    		for (int i = 1; i < x.length; i++)
    			dist += ((x[i] - y[i]) * (x[i] - y[i]));
//    		return (float) Math.sqrt(dist);
    		return dist;
    	}
    	
    	// This function returns the cosine dot distance.	
    	public static float dot(float[] t, float[] u) {
    		float s = 0;
    		for (int i = 0; i < t.length; i++) {
    			s += t[i] * u[i];
    		}
    		return s;
    	}
    	
    	/*
    	 computes the distance matrix between the set of centroids.
    	 
    	 
    	 */
    
    	
   	
    	public static float[][] createDistanceMatrix(  List<Centroid> prev , List<Centroid> curr) {
    		
    		float[][] dismtx = new float[prev.size()][curr.size()+3] ;
    		int currcent=-1;
    		int prevcent =-1;
    	
    		for (int i = 0; i < prev.size(); i++) 
    		    {
    			
    			float mindis= distancesq(prev.get(i).centroid() , curr.get(0).centroid());
    			 for (int j = 0; j < curr.size(); j++) {
    				
    				 dismtx[i][j]= distancesq(prev.get(i).centroid() , curr.get(j).centroid());
    				 if (dismtx[i][j]<= mindis) {
    					 mindis = dismtx[i][j];
    					 prevcent=i;
    					 currcent=j;
    				 }
    			 }
    			 dismtx[i][curr.size()+3] = mindis;
    			 dismtx[i][curr.size()+2] = currcent;
    			 dismtx[i][curr.size()+1] = prevcent;
    			 
    			 
    			}
    		
    		return dismtx;
    	}
    	
    	
    	
  public static float[][] createCosineDistanceMatrix(  List<Centroid> prev , List<Centroid> curr) {
    		
    		float[][] dismtx = new float[prev.size()][curr.size()+3] ;
    		int currcent=-1;
    		int prevcent =-1;
    	
    		for (int i = 0; i < prev.size(); i++) 
    		    {
    			
    			float mindis= dot(prev.get(i).centroid() , curr.get(0).centroid());
    			 for (int j = 0; j < curr.size(); j++) {
    				
    				 dismtx[i][j]= dot(prev.get(i).centroid() , curr.get(j).centroid());
    				 if (dismtx[i][j]<= mindis) {
    					 mindis = dismtx[i][j];
    					 prevcent=i;
    					 currcent=j;
    				 }
    			 }
    			 dismtx[i][curr.size()+3] = mindis;
    			 dismtx[i][curr.size()+2] = currcent;
    			 dismtx[i][curr.size()+1] = prevcent;
    			 
    			 
    			}
    		
    		return dismtx;
    	}
    	
    	
  
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
	
	
	
	public static float[][] mappingcents(  List<Centroid> prev , List<Centroid> curr) {
		
		float[][] mapping1 = new float[prev.size()][curr.size()];
		float[][] mapping2 = new float[prev.size()][curr.size()];
		
		float[][] dismtx_euclid=createDistanceMatrix(prev, curr);
		float[][] dismtx_cosine=createCosineDistanceMatrix(prev, curr);
		
		if (prev.size()==curr.size())
		{
			
			mapping1=dismtx_euclid;
			mapping2=dismtx_cosine;
			
		};
		
		if (prev.size()<curr.size())  // new centroids formed and the others moved.
		{
			
			mapping1=dismtx_euclid;
			mapping2=dismtx_cosine;
			
		};
		
		if (prev.size()>curr.size())   // centroids may have merged and formed
		{
			
			mapping1=dismtx_euclid;
			mapping2=dismtx_cosine;
			
		};
		return mapping1;
	
	}
	

}

		



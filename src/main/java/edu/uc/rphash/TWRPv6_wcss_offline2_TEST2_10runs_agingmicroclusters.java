package edu.uc.rphash;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
//import java.util.Arrays;
import java.util.HashMap;
//import java.util.Iterator;
//import java.util.LinkedHashMap;
import java.util.List;
//import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeSet;
import java.util.stream.IntStream;
import java.util.stream.Stream;
import java.util.Collections;

import edu.uc.rphash.Readers.RPHashObject;
import edu.uc.rphash.Readers.SimpleArrayReader;
import edu.uc.rphash.kneefinder.JythonTest;
import edu.uc.rphash.projections.Projector;
import edu.uc.rphash.tests.StatTests;
import edu.uc.rphash.tests.clusterers.Agglomerative3;
import edu.uc.rphash.tests.clusterers.KMeans2;
import edu.uc.rphash.tests.clusterers.Agglomerative3.ClusteringType;
import edu.uc.rphash.tests.generators.GenerateData;
import edu.uc.rphash.util.VectorUtil;
import edu.uc.rphash.aging.ageCentriods;

//import org.apache.commons.collections.map.MultiValueMap;
//import org.apache.commons.collections.map.*;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import java.lang.*;



// this algorithm runs twrp 10 times : (only the random bisection vector varies, the Projection matrix remains same)
// and selects the one which has the best wcss  offline for the 10X candidate centroids.
public class TWRPv6_wcss_offline2_TEST2_10runs_agingmicroclusters implements Clusterer, Runnable {

	boolean znorm = false;
	int [] num_of_clusters_stage1 = new int[12];
	int min_k;
	int max_k;
	
	// convert this to an array of arrays
	private int counter;
	private float[] rngvec;  
	private float[] rngvec2;
	private float[] rngvec3;
	private float[] rngvec4;
	private float[] rngvec5;
	private float[] rngvec6;
	private float[] rngvec7;
	private float[] rngvec8;
	private float[] rngvec9;
	private float[] rngvec10;
	private float[] rngvec11;
	private float[] rngvec12;
	
	private List<Centroid> centroids = null;
	
	private RPHashObject so;

	public TWRPv6_wcss_offline2_TEST2_10runs_agingmicroclusters(RPHashObject so) {
		this.so = so;
	}

	public List<Centroid> getCentroids(RPHashObject so) {
		this.so = so;
		return getCentroids();
	}

	@Override
	public List<Centroid> getCentroids() {
		if (centroids == null)
			run();
		return centroids;
	}
	
	//private Multimap<Long, float[]> WeightsandCents ;
	
	
//	public  Multimap<Long, float[]> getMicroclusterWeightsandCents (RPHashObject so) {
//		this.so = so;
//		return getCentroids();
//	}
	
	
// This function returns the square of the euclidean distance.	
	public static float distancesq(float[] x, float[] y) {
		if (x.length < 1)
			return Float.MAX_VALUE;
		if (y.length < 1)
			return Float.MAX_VALUE;
		float dist = (x[0] - y[0]) * (x[0] - y[0]);
		for (int i = 1; i < x.length; i++)
			dist += ((x[i] - y[i]) * (x[i] - y[i]));
//		return (float) Math.sqrt(dist);
		return dist;
	}

	
// This method finds the smallest of the numbers and returns that index.

  public static int smallest(float[] arr) 
     {        
         // Initialize minimum element 
         float min = arr[0];
         int minindex = 0;
        // System.out.println(" LENGHT : " + arr.length); 
         // Traverse array elements from second and 
         // compare every element with current max   
         for (int i = 1; i < (arr.length); i++) {
        //	 System.out.println("the min value of i : " + i); 
             if (arr[i]< min) {
                 min = arr[i];
                 minindex = i;
             }
         }		
        // System.out.println("the min value is : " + min); 
        // System.out.println("the index for min val is : " + minindex);        
         return minindex; 
     } 

	
	/*
	 * X - set of vectors compute the medoid of a vector set
	 */
	float[] medoid(List<float[]> X) {
		float[] ret = X.get(0);
		for (int i = 1; i < X.size(); i++) {
			for (int j = 0; j < ret.length; j++) {
				ret[j] += X.get(i)[j];
			}
		}
		for (int j = 0; j < ret.length; j++) {
			ret[j] = ret[j] / ((float) X.size());
		}
		return ret;
	}
	
// this updates the map two cents with different weights are merged into one.
	public static float[][] UpdateHashMap(float cnt_1, float[] x_1, float wcss_1,
			float cnt_2, float[] x_2 , float wcss_2) {
		
		float cnt_r = cnt_1 + cnt_2;
		
		float[] x_r = new float[x_1.length];
		
		for (int i = 0; i < x_1.length; i++) {
			x_r[i] = (cnt_1 * x_1[i] + cnt_2 * x_2[i]) / cnt_r;
								
		}

		float wcss = ( ((wcss_1 + distancesq(x_r,x_1)) ) + distancesq(x_r,x_2) );		
	    
		float[][] ret = new float[3][];
		ret[0] = new float[1];
		ret[0][0] = cnt_r;
		ret[1] = x_r;
		ret[2]= new float [1];
		ret[2][0]= wcss;
		return ret;
					
	}
		

	
	public long hashvec2( float[] xt, float[] x,
			HashMap<Long, float[]>  MapOfIDAndCent, HashMap<Long, Long>  MapOfIDAndCount,  int ct, float[] rngvec, HashMap<Long, Float> MapOfIDAndWCSS) {
		long s = 1;                                  //fixes leading 0's bug
		for (int i = 0; i < xt.length; i++) {
//			s <<= 1;
			s = s << 1 ;                             // left shift the bits of s by 1.
			if (xt[i] > rngvec[i])
				s= s+1;
							
			if (MapOfIDAndCent.containsKey(s)) {
				
				float CurrentCount =   MapOfIDAndCount.get(s);
				float CurrentCent [] = MapOfIDAndCent.get(s);
				float CountForIncomingVector = 1;
				float IncomingVector [] = x;
				float currentWcss= MapOfIDAndWCSS.get(s);
				float incomingWcss= 0;
							
				float[][] MergedValues = UpdateHashMap(CurrentCount , CurrentCent, currentWcss, CountForIncomingVector, IncomingVector, incomingWcss );
				
			  	Long UpdatedCount = (long) MergedValues[0][0] ;
			  	
				float[] MergedVector = MergedValues[1] ;
				
				float wcss= MergedValues[2][0];
				
				MapOfIDAndCount.put(s , UpdatedCount);
				
				MapOfIDAndCent.put(s, MergedVector);
				
				MapOfIDAndWCSS.put(s, wcss);				
						
			} 
				
			else {
							
				float[] xlist = x;
				MapOfIDAndCent.put(s, xlist);
				MapOfIDAndCount.put(s, (long)1);
				MapOfIDAndWCSS.put(s, (float)0);
			}
		}
		return s;
	}
		

	/*
	 * x - input vector IDAndCount - ID->count map IDAndCent - ID->centroid
	 * vector map
	 * 
	 * hash the projected vector x and update the hash to centroid and counts
	 * maps
	 */
	void addtocounter(float[] x, Projector p,
			HashMap<Long, float[]> IDAndCent,HashMap<Long, Long> IDandID,int ct, float[] rngvec , HashMap<Long, Float> IDandWCSS) {
		float[] xt = p.project(x);
		
		hashvec2(xt,x,IDAndCent, IDandID, ct,rngvec , IDandWCSS);
	}
	
	
	/*
	 * X - data set k - canonical k in k-means l - clustering sub-space Compute
	 * density mode via iterative deepening hash counting
	 */
	
	public Multimap<Long, float[]>  findDensityModes2(List<float[]> data_in_round) {

	HashMap<Long, float[]> MapOfIDAndCent1 = new HashMap<>();  
	HashMap<Long, Long> MapOfIDAndCount1 = new HashMap<>();
	HashMap<Long, Float> MapOfIDAndWCSS1 = new HashMap<>();
	
	HashMap<Long, float[]> MapOfIDAndCent2 = new HashMap<>();  
	HashMap<Long, Long> MapOfIDAndCount2 = new HashMap<>();
	HashMap<Long, Float> MapOfIDAndWCSS2 = new HashMap<>();
	
	HashMap<Long, float[]> MapOfIDAndCent3 = new HashMap<>();  
	HashMap<Long, Long> MapOfIDAndCount3 = new HashMap<>();
	HashMap<Long, Float> MapOfIDAndWCSS3 = new HashMap<>();
	
	HashMap<Long, float[]> MapOfIDAndCent4 = new HashMap<>();  
	HashMap<Long, Long> MapOfIDAndCount4 = new HashMap<>();
	HashMap<Long, Float> MapOfIDAndWCSS4 = new HashMap<>();
	
	HashMap<Long, float[]> MapOfIDAndCent5 = new HashMap<>();  
	HashMap<Long, Long> MapOfIDAndCount5 = new HashMap<>();
	HashMap<Long, Float> MapOfIDAndWCSS5 = new HashMap<>();
	
	HashMap<Long, float[]> MapOfIDAndCent6 = new HashMap<>();  
	HashMap<Long, Long> MapOfIDAndCount6 = new HashMap<>();
	HashMap<Long, Float> MapOfIDAndWCSS6 = new HashMap<>();
	
	HashMap<Long, float[]> MapOfIDAndCent7 = new HashMap<>();  
	HashMap<Long, Long> MapOfIDAndCount7 = new HashMap<>();
	HashMap<Long, Float> MapOfIDAndWCSS7 = new HashMap<>();
	
	HashMap<Long, float[]> MapOfIDAndCent8 = new HashMap<>();  
	HashMap<Long, Long> MapOfIDAndCount8 = new HashMap<>();
	HashMap<Long, Float> MapOfIDAndWCSS8 = new HashMap<>();
	
	HashMap<Long, float[]> MapOfIDAndCent9 = new HashMap<>();  
	HashMap<Long, Long> MapOfIDAndCount9 = new HashMap<>();
	HashMap<Long, Float> MapOfIDAndWCSS9 = new HashMap<>();
	
	HashMap<Long, float[]> MapOfIDAndCent10 = new HashMap<>();  
	HashMap<Long, Long> MapOfIDAndCount10 = new HashMap<>();
	HashMap<Long, Float> MapOfIDAndWCSS10 = new HashMap<>();
	
	HashMap<Long, float[]> MapOfIDAndCent11 = new HashMap<>();  
	HashMap<Long, Long> MapOfIDAndCount11 = new HashMap<>();
	HashMap<Long, Float> MapOfIDAndWCSS11 = new HashMap<>();
	
	HashMap<Long, float[]> MapOfIDAndCent12 = new HashMap<>();  
	HashMap<Long, Long> MapOfIDAndCount12 = new HashMap<>();
	HashMap<Long, Float> MapOfIDAndWCSS12 = new HashMap<>();
	
	
	// #create projector matrixs
	Projector projector = so.getProjectionType();
	projector.setOrigDim(so.getdim());
	projector.setProjectedDim(so.getDimparameter());
	projector.setRandomSeed(so.getRandomSeed());
//	projector.setRandomSeed(535247432);
	projector.init();
	
	// #create projector matrixs
	Projector projector2 = so.getProjectionType();
	projector2.setOrigDim(so.getdim());
	projector2.setProjectedDim(so.getDimparameter());
	projector2.setRandomSeed(so.getRandomSeed());
//	projector.setRandomSeed(535247432);
	projector2.init();
	
	// #create projector matrixs
	Projector projector3 = so.getProjectionType();
	projector3.setOrigDim(so.getdim());
	projector3.setProjectedDim(so.getDimparameter());
	projector3.setRandomSeed(so.getRandomSeed());
//	projector.setRandomSeed(535247432);
	projector3.init();
	
	int cutoff = so.getCutoff();
	
	int ct = 0;

	{
		
		for (float[] x : data_in_round) 
			
			
			
		{
			addtocounter(x, projector, MapOfIDAndCent1, MapOfIDAndCount1,ct++, rngvec, MapOfIDAndWCSS1);
			addtocounter(x, projector, MapOfIDAndCent2, MapOfIDAndCount2,ct++, rngvec2,MapOfIDAndWCSS2);
			addtocounter(x, projector, MapOfIDAndCent3, MapOfIDAndCount3,ct++, rngvec3,MapOfIDAndWCSS3);
			addtocounter(x, projector, MapOfIDAndCent4, MapOfIDAndCount4,ct++, rngvec4,MapOfIDAndWCSS4);
			
			addtocounter(x, projector2, MapOfIDAndCent5, MapOfIDAndCount5,ct++, rngvec5,MapOfIDAndWCSS5);
			addtocounter(x, projector2, MapOfIDAndCent6, MapOfIDAndCount6,ct++, rngvec6, MapOfIDAndWCSS6);
			addtocounter(x, projector2, MapOfIDAndCent7, MapOfIDAndCount7,ct++, rngvec7,MapOfIDAndWCSS7);
			addtocounter(x, projector2, MapOfIDAndCent8, MapOfIDAndCount8,ct++, rngvec8,MapOfIDAndWCSS8);
			
			addtocounter(x, projector3, MapOfIDAndCent9, MapOfIDAndCount9,ct++, rngvec9,MapOfIDAndWCSS9);
			addtocounter(x, projector3, MapOfIDAndCent10, MapOfIDAndCount10,ct++, rngvec10,MapOfIDAndWCSS10);	
			addtocounter(x, projector3, MapOfIDAndCent11, MapOfIDAndCount11,ct++, rngvec11,MapOfIDAndWCSS11);
			addtocounter(x, projector3, MapOfIDAndCent12, MapOfIDAndCount12,ct++, rngvec12,MapOfIDAndWCSS12);
			
					
		}
	}	
		
	System.out.println("NumberOfMicroClustersBeforePruning = "+ MapOfIDAndCent3.size());
	
	// next we want to prune the tree by parent count comparison
	// follows breadthfirst search
	
	HashMap<Long, Long> denseSetOfIDandCount2_1 = new HashMap<Long, Long>();
	for (Long cur_id : new TreeSet<Long>(MapOfIDAndCount1.keySet())) 
	{
		if (cur_id >so.getk()){
            int cur_count = (int) (MapOfIDAndCount1.get(cur_id).longValue());
            long parent_id = cur_id>>>1;
            int parent_count = (int) (MapOfIDAndCount1.get(parent_id).longValue());
            
            if(cur_count!=0 && parent_count!=0)
            {
	            if(cur_count == parent_count) {
					denseSetOfIDandCount2_1.put(parent_id, 0L);
					
					MapOfIDAndCent1.put(parent_id, new float[]{});
					
					denseSetOfIDandCount2_1.put(cur_id, (long) cur_count);
					
	            }
	            else
	            {
					if(2 * cur_count > parent_count) {
						denseSetOfIDandCount2_1.remove(parent_id);
						
						MapOfIDAndCent1.put(parent_id, new float[]{});
						
						denseSetOfIDandCount2_1.put(cur_id, (long) cur_count);
					}
	            }
            }
		}
	}
		
	
	HashMap<Long, Long> denseSetOfIDandCount2_2 = new HashMap<Long, Long>();
	for (Long cur_id : new TreeSet<Long>(MapOfIDAndCount2.keySet())) 
	{
		if (cur_id >so.getk()){
            int cur_count = (int) (MapOfIDAndCount2.get(cur_id).longValue());
            long parent_id = cur_id>>>1;
            int parent_count = (int) (MapOfIDAndCount2.get(parent_id).longValue());
            
            if(cur_count!=0 && parent_count!=0)
            {
	            if(cur_count == parent_count) {
					denseSetOfIDandCount2_2.put(parent_id, 0L);
					
					MapOfIDAndCent2.put(parent_id, new float[]{});
					
					denseSetOfIDandCount2_2.put(cur_id, (long) cur_count);
					
	            }
	            else
	            {
					if(2 * cur_count > parent_count) {
						denseSetOfIDandCount2_2.remove(parent_id);
						
						MapOfIDAndCent2.put(parent_id, new float[]{});
						
						denseSetOfIDandCount2_2.put(cur_id, (long) cur_count);
					}
	            }
            }
		}
	}
	
	
	HashMap<Long, Long> denseSetOfIDandCount2_3 = new HashMap<Long, Long>();
	for (Long cur_id : new TreeSet<Long>(MapOfIDAndCount3.keySet())) 
	{
		if (cur_id >so.getk()){
            int cur_count = (int) (MapOfIDAndCount3.get(cur_id).longValue());
            long parent_id = cur_id>>>1;
            int parent_count = (int) (MapOfIDAndCount3.get(parent_id).longValue());
            
            if(cur_count!=0 && parent_count!=0)
            {
	            if(cur_count == parent_count) {
					denseSetOfIDandCount2_3.put(parent_id, 0L);
					
					MapOfIDAndCent3.put(parent_id, new float[]{});
					
					denseSetOfIDandCount2_3.put(cur_id, (long) cur_count);
					
	            }
	            else
	            {
					if(2 * cur_count > parent_count) {
						denseSetOfIDandCount2_3.remove(parent_id);

						MapOfIDAndCent3.put(parent_id, new float[]{});

						denseSetOfIDandCount2_3.put(cur_id, (long) cur_count);
					}
	            }
            }
		}
	}	
	
	
	HashMap<Long, Long> denseSetOfIDandCount2_4 = new HashMap<Long, Long>();
	for (Long cur_id : new TreeSet<Long>(MapOfIDAndCount4.keySet())) 
	{
		if (cur_id >so.getk()){
            int cur_count = (int) (MapOfIDAndCount4.get(cur_id).longValue());
            long parent_id = cur_id>>>1;
            int parent_count = (int) (MapOfIDAndCount4.get(parent_id).longValue());
            
            if(cur_count!=0 && parent_count!=0)
            {
	            if(cur_count == parent_count) {
					denseSetOfIDandCount2_4.put(parent_id, 0L);
					
					MapOfIDAndCent4.put(parent_id, new float[]{});
					
					denseSetOfIDandCount2_4.put(cur_id, (long) cur_count);
					
	            }
	            else
	            {
					if(2 * cur_count > parent_count) {
						denseSetOfIDandCount2_4.remove(parent_id);

						MapOfIDAndCent4.put(parent_id, new float[]{});

						denseSetOfIDandCount2_4.put(cur_id, (long) cur_count);
					}
	            }
            }
		}
	}	
	
	
	HashMap<Long, Long> denseSetOfIDandCount2_5 = new HashMap<Long, Long>();
	for (Long cur_id : new TreeSet<Long>(MapOfIDAndCount5.keySet())) 
	{
		if (cur_id >so.getk()){
            int cur_count = (int) (MapOfIDAndCount5.get(cur_id).longValue());
            long parent_id = cur_id>>>1;
            int parent_count = (int) (MapOfIDAndCount5.get(parent_id).longValue());
            
            if(cur_count!=0 && parent_count!=0)
            {
	            if(cur_count == parent_count) {
					denseSetOfIDandCount2_5.put(parent_id, 0L);
					
					MapOfIDAndCent5.put(parent_id, new float[]{});
					
					denseSetOfIDandCount2_5.put(cur_id, (long) cur_count);
					
	            }
	            else
	            {
					if(2 * cur_count > parent_count) {
						denseSetOfIDandCount2_5.remove(parent_id);

						MapOfIDAndCent5.put(parent_id, new float[]{});

						denseSetOfIDandCount2_5.put(cur_id, (long) cur_count);
					}
	            }
            }
		}
	}	
	
	
	
	HashMap<Long, Long> denseSetOfIDandCount2_6 = new HashMap<Long, Long>();
	for (Long cur_id : new TreeSet<Long>(MapOfIDAndCount6.keySet())) 
	{
		if (cur_id >so.getk()){
            int cur_count = (int) (MapOfIDAndCount6.get(cur_id).longValue());
            long parent_id = cur_id>>>1;
            int parent_count = (int) (MapOfIDAndCount6.get(parent_id).longValue());
            
            if(cur_count!=0 && parent_count!=0)
            {
	            if(cur_count == parent_count) {
					denseSetOfIDandCount2_6.put(parent_id, 0L);
					
					MapOfIDAndCent6.put(parent_id, new float[]{});
					
					denseSetOfIDandCount2_6.put(cur_id, (long) cur_count);
					
	            }
	            else
	            {
					if(2 * cur_count > parent_count) {
						denseSetOfIDandCount2_6.remove(parent_id);

						MapOfIDAndCent6.put(parent_id, new float[]{});

						denseSetOfIDandCount2_6.put(cur_id, (long) cur_count);
					}
	            }
            }
		}
	}	
	
	
	HashMap<Long, Long> denseSetOfIDandCount2_7 = new HashMap<Long, Long>();
	for (Long cur_id : new TreeSet<Long>(MapOfIDAndCount7.keySet())) 
	{
		if (cur_id >so.getk()){
            int cur_count = (int) (MapOfIDAndCount7.get(cur_id).longValue());
            long parent_id = cur_id>>>1;
            int parent_count = (int) (MapOfIDAndCount7.get(parent_id).longValue());
            
            if(cur_count!=0 && parent_count!=0)
            {
	            if(cur_count == parent_count) {
					denseSetOfIDandCount2_7.put(parent_id, 0L);
					
					MapOfIDAndCent7.put(parent_id, new float[]{});
					
					denseSetOfIDandCount2_7.put(cur_id, (long) cur_count);
					
	            }
	            else
	            {
					if(2 * cur_count > parent_count) {
						denseSetOfIDandCount2_7.remove(parent_id);

						MapOfIDAndCent7.put(parent_id, new float[]{});

						denseSetOfIDandCount2_7.put(cur_id, (long) cur_count);
					}
	            }
            }
		}
	}	
	
	HashMap<Long, Long> denseSetOfIDandCount2_8 = new HashMap<Long, Long>();
	for (Long cur_id : new TreeSet<Long>(MapOfIDAndCount8.keySet())) 
	{
		if (cur_id >so.getk()){
            int cur_count = (int) (MapOfIDAndCount8.get(cur_id).longValue());
            long parent_id = cur_id>>>1;
            int parent_count = (int) (MapOfIDAndCount8.get(parent_id).longValue());
            
            if(cur_count!=0 && parent_count!=0)
            {
	            if(cur_count == parent_count) {
					denseSetOfIDandCount2_8.put(parent_id, 0L);
					
					MapOfIDAndCent8.put(parent_id, new float[]{});
					
					denseSetOfIDandCount2_8.put(cur_id, (long) cur_count);
					
	            }
	            else
	            {
					if(2 * cur_count > parent_count) {
						denseSetOfIDandCount2_8.remove(parent_id);

						MapOfIDAndCent8.put(parent_id, new float[]{});

						denseSetOfIDandCount2_8.put(cur_id, (long) cur_count);
					}
	            }
            }
		}
	}
	
	
	HashMap<Long, Long> denseSetOfIDandCount2_9 = new HashMap<Long, Long>();
	for (Long cur_id : new TreeSet<Long>(MapOfIDAndCount9.keySet())) 
	{
		if (cur_id >so.getk()){
            int cur_count = (int) (MapOfIDAndCount9.get(cur_id).longValue());
            long parent_id = cur_id>>>1;
            int parent_count = (int) (MapOfIDAndCount9.get(parent_id).longValue());
            
            if(cur_count!=0 && parent_count!=0)
            {
	            if(cur_count == parent_count) {
					denseSetOfIDandCount2_9.put(parent_id, 0L);
					
					MapOfIDAndCent9.put(parent_id, new float[]{});
					
					denseSetOfIDandCount2_9.put(cur_id, (long) cur_count);
					
	            }
	            else
	            {
					if(2 * cur_count > parent_count) {
						denseSetOfIDandCount2_9.remove(parent_id);

						MapOfIDAndCent9.put(parent_id, new float[]{});

						denseSetOfIDandCount2_9.put(cur_id, (long) cur_count);
					}
	            }
            }
		}
	}
	
	HashMap<Long, Long> denseSetOfIDandCount2_10 = new HashMap<Long, Long>();
	for (Long cur_id : new TreeSet<Long>(MapOfIDAndCount10.keySet())) 
	{
		if (cur_id >so.getk()){
            int cur_count = (int) (MapOfIDAndCount10.get(cur_id).longValue());
            long parent_id = cur_id>>>1;
            int parent_count = (int) (MapOfIDAndCount10.get(parent_id).longValue());
            
            if(cur_count!=0 && parent_count!=0)
            {
	            if(cur_count == parent_count) {
					denseSetOfIDandCount2_10.put(parent_id, 0L);
					
					MapOfIDAndCent10.put(parent_id, new float[]{});
					
					denseSetOfIDandCount2_10.put(cur_id, (long) cur_count);
					
	            }
	            else
	            {
					if(2 * cur_count > parent_count) {
						denseSetOfIDandCount2_10.remove(parent_id);

						MapOfIDAndCent10.put(parent_id, new float[]{});

						denseSetOfIDandCount2_10.put(cur_id, (long) cur_count);
					}
	            }
            }
		}
	}
	
	
	HashMap<Long, Long> denseSetOfIDandCount2_11 = new HashMap<Long, Long>();
	for (Long cur_id : new TreeSet<Long>(MapOfIDAndCount11.keySet())) 
	{
		if (cur_id >so.getk()){
            int cur_count = (int) (MapOfIDAndCount11.get(cur_id).longValue());
            long parent_id = cur_id>>>1;
            int parent_count = (int) (MapOfIDAndCount11.get(parent_id).longValue());
            
            if(cur_count!=0 && parent_count!=0)
            {
	            if(cur_count == parent_count) {
					denseSetOfIDandCount2_11.put(parent_id, 0L);
					
					MapOfIDAndCent11.put(parent_id, new float[]{});
					
					denseSetOfIDandCount2_11.put(cur_id, (long) cur_count);
					
	            }
	            else
	            {
					if(2 * cur_count > parent_count) {
						denseSetOfIDandCount2_11.remove(parent_id);

						MapOfIDAndCent11.put(parent_id, new float[]{});

						denseSetOfIDandCount2_11.put(cur_id, (long) cur_count);
					}
	            }
            }
		}
	}
	
	HashMap<Long, Long> denseSetOfIDandCount2_12 = new HashMap<Long, Long>();
	for (Long cur_id : new TreeSet<Long>(MapOfIDAndCount12.keySet())) 
	{
		if (cur_id >so.getk()){
            int cur_count = (int) (MapOfIDAndCount12.get(cur_id).longValue());
            long parent_id = cur_id>>>1;
            int parent_count = (int) (MapOfIDAndCount12.get(parent_id).longValue());
            
            if(cur_count!=0 && parent_count!=0)
            {
	            if(cur_count == parent_count) {
					denseSetOfIDandCount2_12.put(parent_id, 0L);
					
					MapOfIDAndCent12.put(parent_id, new float[]{});
					
					denseSetOfIDandCount2_12.put(cur_id, (long) cur_count);
					
	            }
	            else
	            {
					if(2 * cur_count > parent_count) {
						denseSetOfIDandCount2_12.remove(parent_id);

						MapOfIDAndCent12.put(parent_id, new float[]{});

						denseSetOfIDandCount2_12.put(cur_id, (long) cur_count);
					}
	            }
            }
		}
	}
	
	//remove keys with support less than 1
	Stream<Entry<Long, Long>> stream2_1 = denseSetOfIDandCount2_1.entrySet().stream().filter(p -> p.getValue() > 1);
	List<Long> sortedIDList2_1= new ArrayList<>();
	// sort and limit the list
	stream2_1.sorted(Entry.<Long, Long> comparingByValue().reversed()).limit(cutoff)
			.forEachOrdered(x -> sortedIDList2_1.add(x.getKey()));
		
	
	Stream<Entry<Long, Long>> stream2_2 = denseSetOfIDandCount2_2.entrySet().stream().filter(p -> p.getValue() > 1);
	List<Long> sortedIDList2_2= new ArrayList<>();
	// sort and limit the list
	stream2_2.sorted(Entry.<Long, Long> comparingByValue().reversed()).limit(cutoff)
			.forEachOrdered(x -> sortedIDList2_2.add(x.getKey()));
		
	
	Stream<Entry<Long, Long>> stream2_3 = denseSetOfIDandCount2_3.entrySet().stream().filter(p -> p.getValue() > 1);
	List<Long> sortedIDList2_3= new ArrayList<>();
	// sort and limit the list
	stream2_3.sorted(Entry.<Long, Long> comparingByValue().reversed()).limit(cutoff)
			.forEachOrdered(x -> sortedIDList2_3.add(x.getKey()));
		
	
	Stream<Entry<Long, Long>> stream2_4 = denseSetOfIDandCount2_4.entrySet().stream().filter(p -> p.getValue() > 1);
	List<Long> sortedIDList2_4= new ArrayList<>();
	// sort and limit the list
	stream2_4.sorted(Entry.<Long, Long> comparingByValue().reversed()).limit(cutoff)
			.forEachOrdered(x -> sortedIDList2_4.add(x.getKey()));
	
	Stream<Entry<Long, Long>> stream2_5 = denseSetOfIDandCount2_5.entrySet().stream().filter(p -> p.getValue() > 1);
	List<Long> sortedIDList2_5= new ArrayList<>();
	// sort and limit the list
	stream2_5.sorted(Entry.<Long, Long> comparingByValue().reversed()).limit(cutoff)
			.forEachOrdered(x -> sortedIDList2_5.add(x.getKey()));
	
	
	Stream<Entry<Long, Long>> stream2_6 = denseSetOfIDandCount2_6.entrySet().stream().filter(p -> p.getValue() > 1);
	List<Long> sortedIDList2_6= new ArrayList<>();
	// sort and limit the list
	stream2_6.sorted(Entry.<Long, Long> comparingByValue().reversed()).limit(cutoff)
			.forEachOrdered(x -> sortedIDList2_6.add(x.getKey()));
	
	
	Stream<Entry<Long, Long>> stream2_7 = denseSetOfIDandCount2_7.entrySet().stream().filter(p -> p.getValue() > 1);
	List<Long> sortedIDList2_7= new ArrayList<>();
	// sort and limit the list
	stream2_7.sorted(Entry.<Long, Long> comparingByValue().reversed()).limit(cutoff)
			.forEachOrdered(x -> sortedIDList2_7.add(x.getKey()));
	
	
	Stream<Entry<Long, Long>> stream2_8 = denseSetOfIDandCount2_8.entrySet().stream().filter(p -> p.getValue() > 1);
	List<Long> sortedIDList2_8= new ArrayList<>();
	// sort and limit the list
	stream2_8.sorted(Entry.<Long, Long> comparingByValue().reversed()).limit(cutoff)
			.forEachOrdered(x -> sortedIDList2_8.add(x.getKey()));
	
	
	Stream<Entry<Long, Long>> stream2_9 = denseSetOfIDandCount2_9.entrySet().stream().filter(p -> p.getValue() > 1);
	List<Long> sortedIDList2_9= new ArrayList<>();
	// sort and limit the list
	stream2_9.sorted(Entry.<Long, Long> comparingByValue().reversed()).limit(cutoff)
			.forEachOrdered(x -> sortedIDList2_9.add(x.getKey()));
	
	
	Stream<Entry<Long, Long>> stream2_10 = denseSetOfIDandCount2_10.entrySet().stream().filter(p -> p.getValue() > 1);
	List<Long> sortedIDList2_10= new ArrayList<>();
	// sort and limit the list
	stream2_10.sorted(Entry.<Long, Long> comparingByValue().reversed()).limit(cutoff)
			.forEachOrdered(x -> sortedIDList2_10.add(x.getKey()));
	

	Stream<Entry<Long, Long>> stream2_11 = denseSetOfIDandCount2_11.entrySet().stream().filter(p -> p.getValue() > 1);
	List<Long> sortedIDList2_11= new ArrayList<>();
	// sort and limit the list
	stream2_11.sorted(Entry.<Long, Long> comparingByValue().reversed()).limit(cutoff)
			.forEachOrdered(x -> sortedIDList2_11.add(x.getKey()));

	Stream<Entry<Long, Long>> stream2_12 = denseSetOfIDandCount2_12.entrySet().stream().filter(p -> p.getValue() > 1);
	List<Long> sortedIDList2_12= new ArrayList<>();
	// sort and limit the list
	stream2_12.sorted(Entry.<Long, Long> comparingByValue().reversed()).limit(cutoff)
			.forEachOrdered(x -> sortedIDList2_12.add(x.getKey()));
	
// finding elbows 
	JythonTest elbowcalculator = new JythonTest();
        double sum_jt = 0;
   
     	num_of_clusters_stage1[0]= elbowcalculator.find_elbow(sortedIDList2_1);
     	num_of_clusters_stage1[1]= elbowcalculator.find_elbow(sortedIDList2_2);
     	num_of_clusters_stage1[2]= elbowcalculator.find_elbow(sortedIDList2_3);
     	num_of_clusters_stage1[3]= elbowcalculator.find_elbow(sortedIDList2_4);
     	num_of_clusters_stage1[4]= elbowcalculator.find_elbow(sortedIDList2_5);
     	num_of_clusters_stage1[5]= elbowcalculator.find_elbow(sortedIDList2_6);
     	num_of_clusters_stage1[6]= elbowcalculator.find_elbow(sortedIDList2_7);
     	num_of_clusters_stage1[7]= elbowcalculator.find_elbow(sortedIDList2_8);
     	num_of_clusters_stage1[8]= elbowcalculator.find_elbow(sortedIDList2_9);
     	num_of_clusters_stage1[9]= elbowcalculator.find_elbow(sortedIDList2_10);
     	num_of_clusters_stage1[10]= elbowcalculator.find_elbow(sortedIDList2_11);
     	num_of_clusters_stage1[11]= elbowcalculator.find_elbow(sortedIDList2_12);
     	
        for (int i=0 ; i<12; i++) {
		
		//int num_of_clusters_2= elbowcalculator.find_elbow(counts);
////		System.out.println("\n" + "No. of clusters_stage1 = " +  num_of_clusters_stage1[i]); 
		//System.out.println(       "No. of clusters_by_COUNT = " +  num_of_clusters_2); 
////		System.out.println( "************************************************************" ); 
		sum_jt = sum_jt + num_of_clusters_stage1[i];
        }
////		System.out.println("\n" + "sum of No. of clusters_stage1 = " +  sum_jt);
       
	    double avg_clus_stage1 =   Math.ceil(sum_jt / 12.0) ;
	    System.out.println("\n" + "Average of No. of clusters_stage1 = " + avg_clus_stage1 );
	    System.out.println( "************************************************************" ); 
	    // finding the range of K for offline clustering :
	    min_k = (int) (avg_clus_stage1 - 3) ;
	    
	    max_k = (int) (avg_clus_stage1 + 3)	;
	    
    	if (min_k < 3 ) { min_k = 3 ; max_k = 9; }  	;	
	
	
	float WCSS1  = 0;
	float WCSS2  = 0;
	float WCSS3  = 0;
	float WCSS4  = 0;
	float WCSS5  = 0;
	float WCSS6  = 0;
	float WCSS7  = 0;
	float WCSS8  = 0;
	float WCSS9  = 0;
	float WCSS10 = 0;
	float WCSS11 = 0;
	float WCSS12 = 0;
	
	
	HashMap<Long, Long> denseSetOfIDandCount2 = new HashMap<Long, Long>();
	HashMap<Long, float[]> MapOfIDAndCent = new HashMap<>();  
	HashMap<Long, Long> MapOfIDAndCount = new HashMap<>();
	HashMap<Long, Float> MapOfIDAndWCSS =  new HashMap<>();
	
	
 //* THIS IS THE RUNTIME CALCULATION OF WCSS STATISTICS WHICH IS DONE ONLINE: 
  
	for (Long keys: sortedIDList2_1){ 
		WCSS1 = WCSS1 + MapOfIDAndWCSS1.get(keys);}
		
	for (Long keys: sortedIDList2_2)	
	{  WCSS2 = WCSS2 + MapOfIDAndWCSS2.get(keys);}
	
	for (Long keys: sortedIDList2_3)	
	{  WCSS3 = WCSS3 + MapOfIDAndWCSS3.get(keys);}
	
	for (Long keys: sortedIDList2_4)	
	{  WCSS4 = WCSS4 + MapOfIDAndWCSS4.get(keys);}
	
	for (Long keys: sortedIDList2_5)	
	{  WCSS5 = WCSS5 + MapOfIDAndWCSS5.get(keys);}
	
	for (Long keys: sortedIDList2_6){ 
		WCSS6 = WCSS6 + MapOfIDAndWCSS6.get(keys);}
		
	for (Long keys: sortedIDList2_7)	
	{  WCSS7 = WCSS7 + MapOfIDAndWCSS7.get(keys);}
	
	for (Long keys: sortedIDList2_8)	
	{  WCSS8 = WCSS8 + MapOfIDAndWCSS8.get(keys);}
	
	for (Long keys: sortedIDList2_9)	
	{  WCSS9 = WCSS9 + MapOfIDAndWCSS9.get(keys);}
	
	for (Long keys: sortedIDList2_10)	
	{  WCSS10 = WCSS10 + MapOfIDAndWCSS10.get(keys);}
	
	for (Long keys: sortedIDList2_11)	
	{  WCSS11 = WCSS11 + MapOfIDAndWCSS11.get(keys);}
	
	for (Long keys: sortedIDList2_12)	
	{  WCSS12 = WCSS12 + MapOfIDAndWCSS12.get(keys);}
	
			
	
	System.out.print(" wcss1 = " + WCSS1);
	
	System.out.print(" wcss2 = " + WCSS2);	
	
	System.out.print(" wcss3 = " + WCSS3);
	
	System.out.print(" wcss4 = " + WCSS4);
	
	System.out.print(" wcss5 = " + WCSS5);
	
	System.out.print(" wcss6 = " + WCSS6);
	
	System.out.print(" wcss7 = " + WCSS7);
	
	System.out.print(" wcss8 = " + WCSS8);
	
	System.out.print(" wcss9 = " + WCSS9);
	
	System.out.print(" wcss10 = " + WCSS10);
	
	System.out.print(" wcss11 = " + WCSS11);
	
	System.out.print(" wcss12 = " + WCSS12);
	
//	float arr[] = {WCSS_off_1,WCSS_off_2,WCSS_off_3,WCSS_off_4,WCSS_off_5,WCSS_off_6,WCSS_off_7,WCSS_off_8,WCSS_off_9,WCSS_off_10};
	float arr[] = {WCSS1,WCSS2,WCSS3,WCSS4,WCSS5,WCSS6,WCSS7,WCSS8,WCSS9,WCSS10, WCSS11, WCSS12 };
	
	int index_of_max = smallest(arr); 
	
	if (index_of_max == 0){
		MapOfIDAndCount = MapOfIDAndCount1;
		MapOfIDAndCent = MapOfIDAndCent1;
		MapOfIDAndWCSS = MapOfIDAndWCSS1;
		denseSetOfIDandCount2 = denseSetOfIDandCount2_1;
		System.out.println("winner = tree1");
	}
	if (index_of_max == 1){	
		MapOfIDAndCount = MapOfIDAndCount2;
		MapOfIDAndCent = MapOfIDAndCent2;
		MapOfIDAndWCSS = MapOfIDAndWCSS2;
		denseSetOfIDandCount2 = denseSetOfIDandCount2_2;
		System.out.println("winner = tree2");
	}
	if (index_of_max == 2){
		MapOfIDAndCount = MapOfIDAndCount3;
		MapOfIDAndCent = MapOfIDAndCent3;
		MapOfIDAndWCSS = MapOfIDAndWCSS3;
		denseSetOfIDandCount2 = denseSetOfIDandCount2_3;
		System.out.println("winner = tree3");
	}	
	if (index_of_max == 3) {
		MapOfIDAndCount = MapOfIDAndCount4;
		MapOfIDAndCent = MapOfIDAndCent4;
		MapOfIDAndWCSS = MapOfIDAndWCSS4;
		denseSetOfIDandCount2 = denseSetOfIDandCount2_4;
		System.out.println("winner = tree4");
	}
	
	if (index_of_max == 4) {
		MapOfIDAndCount = MapOfIDAndCount5;
		MapOfIDAndCent = MapOfIDAndCent5;
		MapOfIDAndWCSS = MapOfIDAndWCSS5;
		denseSetOfIDandCount2 = denseSetOfIDandCount2_5;
		System.out.println("winner = tree5");
	}
	if (index_of_max == 5) {
		MapOfIDAndCount = MapOfIDAndCount6;
		MapOfIDAndCent = MapOfIDAndCent6;
		MapOfIDAndWCSS = MapOfIDAndWCSS6;
		denseSetOfIDandCount2 = denseSetOfIDandCount2_6;
		System.out.println("winner = tree6");
		}
	if (index_of_max == 6) {
		MapOfIDAndCount = MapOfIDAndCount7;
		MapOfIDAndCent = MapOfIDAndCent7;
		MapOfIDAndWCSS = MapOfIDAndWCSS7;
		denseSetOfIDandCount2 = denseSetOfIDandCount2_7;
		System.out.println("winner = tree7");
		}
	if (index_of_max == 7) {
		MapOfIDAndCount = MapOfIDAndCount8;
		MapOfIDAndCent = MapOfIDAndCent8;
		MapOfIDAndWCSS = MapOfIDAndWCSS8;
		denseSetOfIDandCount2 = denseSetOfIDandCount2_8;
		System.out.println("winner = tree8");
		}
	if (index_of_max == 8) {
		MapOfIDAndCount = MapOfIDAndCount9;
		MapOfIDAndCent = MapOfIDAndCent9;
		MapOfIDAndWCSS = MapOfIDAndWCSS9;
		denseSetOfIDandCount2 = denseSetOfIDandCount2_9;
		System.out.println("winner = tree9");
		}
	if (index_of_max == 9) {
		MapOfIDAndCount = MapOfIDAndCount10;
		MapOfIDAndCent = MapOfIDAndCent10;
		MapOfIDAndWCSS = MapOfIDAndWCSS10;
		denseSetOfIDandCount2 = denseSetOfIDandCount2_10;
		System.out.println("winner = tree10");
		}
	if (index_of_max == 10) {
		MapOfIDAndCount = MapOfIDAndCount11;
		MapOfIDAndCent = MapOfIDAndCent11;
		MapOfIDAndWCSS = MapOfIDAndWCSS11;
		denseSetOfIDandCount2 = denseSetOfIDandCount2_11;
		System.out.println("winner = tree11");
		}
	if (index_of_max == 11) {
		MapOfIDAndCount = MapOfIDAndCount12;
		MapOfIDAndCent = MapOfIDAndCent12;
		MapOfIDAndWCSS = MapOfIDAndWCSS12;
		denseSetOfIDandCount2 = denseSetOfIDandCount2_12;
		System.out.println("winner = tree12");
		}
	
	
	System.out.println("NumberOfMicroClustersAfterPruning&beforesortingLimit = "+ denseSetOfIDandCount2.size());
	
	//remove keys with support less than 1
	Stream<Entry<Long, Long>> stream2 = denseSetOfIDandCount2.entrySet().stream().filter(p -> p.getValue() > 1);
	
	List<Long> sortedIDList2= new ArrayList<>();
	// sort and limit the list
	stream2.sorted(Entry.<Long, Long> comparingByValue().reversed()).limit(cutoff)
			.forEachOrdered(x -> sortedIDList2.add(x.getKey()));
		
	Multimap<Long, float[]> multimapWeightAndCent = ArrayListMultimap.create();

	for (Long keys: sortedIDList2)
		
		{

		  multimapWeightAndCent.put((Long)(MapOfIDAndCount.get(keys)), (float[]) (MapOfIDAndCent.get(keys)));
		
		}
		

	return multimapWeightAndCent;
	
	
}
	
	
	// this method gets the hashmap of ids and counts for the top n(cutoff) microclusters
//	public static HashMap<Long, Long> getmicroclusterIDandCount
	
	
	
	// this method gets the hashmap of ids and centroids for the top n(cutoff) microclusters
	
//	public static HashMap<Long, float[]>  getmicroclusterIDandCents
	
	
	
	// this method gets the multihashmap of counts and centroids for the top n(cutoff) microclusters
	
	
	public void run() {
		
		
	    List<float[]> data_in_round = new ArrayList<float[]>() ;
	    int count1=0;
	    int count2=0;
	   // List<Centroid> cents_aged = null ;          /// may required to be initialized
	   // List<Centroid> cents_prev_round = null ;  /// may required to be properly initialized
	    
	    boolean flag = true;    // indicates first round if true  else is false
	    Multimap<Long, float[]> WeightAndClusters =    ArrayListMultimap.create()      ;                                  // null;
	    Multimap<Long, float[]> WeightAndClusters_prev = ArrayListMultimap.create()    ;                             // null;
	 //   Multimap<Long, float[]> WeightAndClusters_aged = ArrayListMultimap.create()    ;                            // null;	
	    
	  	List<float[]>centroids_prev = new ArrayList<>();
	  	List<Float> weights_prev =new ArrayList<>();
	  	
	  	List<float[]>centroids2 = new ArrayList<>();
		List<Float> weights2 =new ArrayList<>();
			
	  	List<float[]>centroids3 = new ArrayList<>();
		List<Float> weights3 =new ArrayList<>();
		
		//System.out.println(count2);
		
	    for (float[] x : so.getRawData()) {
			
	    count1 = count1+1;
		//System.out.println(count1);
		//System.out.println(element);	
		//data_in_round.add(so.getRawData().get(count1));
	
	    data_in_round.add(x);
		count1 = count1+1;
		count2 = count2 +1;
		
		//System.out.println(count2);
		
		if (count2 >= 2500) {
	
			System.out.println(count2);

		
		rngvec  = new float[so.getDimparameter()];
		rngvec2 = new float[so.getDimparameter()];
		rngvec3 = new float[so.getDimparameter()];
		rngvec4 = new float[so.getDimparameter()];
		rngvec5 = new float[so.getDimparameter()];	
		rngvec6  = new float[so.getDimparameter()];
		rngvec7 = new float[so.getDimparameter()];
		rngvec8 = new float[so.getDimparameter()];
		rngvec9 = new float[so.getDimparameter()];
		rngvec10 = new float[so.getDimparameter()];
		rngvec11 = new float[so.getDimparameter()];
		rngvec12 = new float[so.getDimparameter()];
		
		counter = 0;
		boolean randVect = so.getRandomVector();
		
	//  Random r = new Random(so.getRandomSeed());
	//	Random r = new Random(3800635955020675334L) ;
		Random r = new Random();
		Random r2 = new Random();
		Random r3 = new Random();
		Random r4 = new Random();
		Random r5 = new Random();
		Random r6 = new Random();
		Random r7 = new Random();
		Random r8 = new Random();
		Random r9 = new Random();
		Random r10 = new Random();
		Random r11 = new Random();
		Random r12 = new Random();
		
		if (randVect==true){
		for (int i = 0; i < so.getDimparameter(); i++)
			rngvec[i] = (float) r.nextGaussian();
		for (int i = 0; i < so.getDimparameter(); i++)
		    rngvec2[i] = (float) r2.nextGaussian();
		for (int i = 0; i < so.getDimparameter(); i++)
		    rngvec3[i] = (float) r3.nextGaussian();
		for (int i = 0; i < so.getDimparameter(); i++)
		    rngvec4[i] = (float) r4.nextGaussian();	
		for (int i = 0; i < so.getDimparameter(); i++)
		    rngvec5[i] = (float) r5.nextGaussian();
		for (int i = 0; i < so.getDimparameter(); i++)
		    rngvec6[i] = (float) r6.nextGaussian();
		for (int i = 0; i < so.getDimparameter(); i++)
		    rngvec7[i] = (float) r7.nextGaussian();
		for (int i = 0; i < so.getDimparameter(); i++)
		    rngvec8[i] = (float) r8.nextGaussian();
		for (int i = 0; i < so.getDimparameter(); i++)
		    rngvec9[i] = (float) r9.nextGaussian();
		for (int i = 0; i < so.getDimparameter(); i++)
		    rngvec10[i] = (float) r10.nextGaussian();
		for (int i = 0; i < so.getDimparameter(); i++)
		    rngvec11[i] = (float) r11.nextGaussian();
		for (int i = 0; i < so.getDimparameter(); i++)
		    rngvec11[i] = (float) r12.nextGaussian();
		
		    } 
		
		else {
			for (int i = 0; i < so.getDimparameter(); i++)
			rngvec[i] = (float) 0;
			
			   }
		
	      WeightAndClusters = findDensityModes2(data_in_round);
		
		if ( flag == true)  {
			WeightAndClusters_prev = WeightAndClusters;
			flag = false ;
		    }
		
	 //	WeightAndClusters_prev=WeightAndClusters_aged;
	 	
	 //	WeightAndClusters_aged.clear();
		
	//  System.out.println("multimap = "+  WeightAndClusters);
		
     //  System.out.println("\tNumberOfMicroClusters_AfterPruning = "+ WeightAndClusters.size());		
	//	 System.out.println("getRandomVector = "+ randVect);
	//	System.out.println("getRandomVector = "+ randVect);
		  
		
		for (Long weights : WeightAndClusters.keys())						
		{			
		weights2.add((float)weights);
		}
		System.out.println("curr_keys     = "+  weights2);
		
		for (Long weight : WeightAndClusters.keySet())	
			
		{
		    centroids2.addAll(WeightAndClusters.get(weight));					
		}	
					
	
		weights_prev.clear();
		
		for (Long weights : WeightAndClusters_prev.keys())	
		
		{
			float temp  = (float) (0.25 * weights); 
			weights_prev.add((float)temp);
		}
		
			System.out.println("kweighted_prev= "+  weights_prev);	
			
			
			centroids_prev.clear();
			for (Long weights : WeightAndClusters_prev.keySet())	
				
			{
			    centroids_prev.addAll(WeightAndClusters_prev.get(weights));						
			}	
					
			
			
			
			for ( float w : weights_prev)	
				
			{
				weights2.add(w);
				}
				
			System.out.println("keys_joined   = "+  weights2);
			
			for (float[] c : centroids_prev)	
				
			{

			    centroids2.add(c);
						
			}
			
		//	System.out.println("merged weights size = "+  weights2.size());	
		//	System.out.println("merged cents size = "+  centroids2.size());	
			
		//	trim the weights2 and centroids2 to fix size :
		//	logic: select the top n weights and its index from weights2 , then select the centroids from those index in centroids2

		//	Collections.sort(weights2, Collections.reverseOrder());
		//	weights2.sort(Comparator.reverseOrder());


			int[] sortedIndices = IntStream.range(0, weights2.size())
	                .boxed().sorted((i, j) ->  weights2.get(j).compareTo( weights2.get(i)) )
	                .mapToInt(ele -> ele).toArray();
			System.out.println("sorted_index= "+ Arrays. toString(sortedIndices));	
			
			// create weights3 and centroid3 and then select the top 60 or cutoff elements.
			
	      int  limit=so.getCutoff() + 10;
	      
			for (int i=0; i<= limit ;i++)
				
			{
				int indx = sortedIndices[i] ;     // check
				
				 Float key_in_indx = weights2.get(indx);         // weights2 is list  of floats
				
				 weights3.add( key_in_indx);
				 
				  float[] cent_in_indx = centroids2.get(indx);
				
				centroids3.add(cent_in_indx);
						
				}
				
			System.out.println("keys_joined   = "+  weights3);
			
			System.out.println("size of weights3   = "+  weights3.size());
			
			System.out.println("size of centroids3   = "+  centroids3.size());
			
			// create a multimap and add the weights 3 and centriods3 and set it as agedmultimap	
			
		// also create the multimap aged from this weights2 and cents2.
			
	   	Multimap<Long, float[]> WeightAndClusters_aged = ArrayListMultimap.create();
			
			WeightAndClusters_aged.clear();
				
				for (int i=0; i< weights3.size(); i++)
							
				{
				WeightAndClusters_aged.put((weights3.get(i).longValue()), (float[]) (centroids3.get(i)));				
				}
				
		
//		Agglomerative3 aggloOffline =  new Agglomerative3(ClusteringType.AVG_LINKAGE,centroids2, so.getk());
//		aggloOffline.setWeights(weights2);
//		this.centroids = aggloOffline.getCentroids();

		KMeans2 aggloOffline2 = new KMeans2();
		aggloOffline2.setRawData(centroids3);
		aggloOffline2.setWeights(weights3);
		
		
		List<Long> elbow_wcss = new ArrayList<>();
		
		for (int k=min_k; k<=max_k ;k++) {
			
		aggloOffline2.setK(k);
		List<Centroid> cent1 = aggloOffline2.getCentroids();
		System.out.printf("%.0f\t",	StatTests.WCSSECentroidsFloat(cent1, data_in_round));
		long tempu = (long) StatTests.WCSSECentroidsFloat(cent1, data_in_round);
		elbow_wcss.add(tempu);
		}
			
		
		// finding elbows 
		JythonTest elbowcalculator = new JythonTest();
	        double sum_jt = 0;
	   
	     	int num_of_clusters_stage2 = min_k + elbowcalculator.find_elbow(elbow_wcss);
	     	
	     	
	     	System.out.println("\n" + "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx "  );
	     	System.out.println("\n" + " No. of clusters_stage_2_Final = " + num_of_clusters_stage2 );
	     	System.out.println("\n" + "xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx "  );
	     	
	     	System.out.println("\n" + "No. of Data Points = " + so.getRawData().size() );

		
	    //  final choice of centroids :		( repetative calculation , please optimize )
	     	aggloOffline2.setK(num_of_clusters_stage2);
	     	
	     	this.centroids = aggloOffline2.getCentroids();  
	     	
	     	count2=0;
	     	data_in_round.clear();
	     	weights2.clear();
	     	centroids2.clear();
	     	
	     	weights3.clear();
	     	centroids3.clear();
	     	
	     	WeightAndClusters.clear();
	     	
	     	WeightAndClusters_prev.clear();
	     	
	     	WeightAndClusters_prev = WeightAndClusters_aged;
 	
		       }   // end of the if loop
	     
	
		} // end of the for loop
	     	
			
	}  // end of run method
	

	public static void main(String[] args) throws FileNotFoundException,
			IOException , InterruptedException {
		

			float avgtime = 0;
	//		System.out.printf("%f\t", f);
				
					//	List<Float[]> data = "/C:/Users/user/Desktop/temp/OutputTwrpCents1" 
				
			//	RPHashObject o = new SimpleArrayReader(gen.data, k);
				
				boolean raw = Boolean.parseBoolean(("raw"));
 			    List<float[]> data = null;
			
				// "C:\Users\sayan\OneDrive - University of Cincinnati\Documents\downloaded\run_results\run_results\3runs\har_k6\1D.txt"
				
				String inputfile = "C:/Users/sayan/OneDrive - University of Cincinnati/Documents/downloaded/run_results/run_results/3runs/har_k6/1D.txt" ;
				System.out.println(inputfile);
				
				data = VectorUtil.readFile( inputfile , raw);

				int dummyk = 8;
				RPHashObject o = new SimpleArrayReader(data, dummyk);
					
				o.setDimparameter(16);
				o.setCutoff(60);
				o.setRandomVector(true);			
								
				TWRPv6_wcss_offline2_TEST2_10runs_agingmicroclusters rphit = new TWRPv6_wcss_offline2_TEST2_10runs_agingmicroclusters(o);
				long startTime = System.nanoTime();
			   // rphit.getCentroids();
			    rphit.run();

//				avgtime += (System.nanoTime() - startTime) / 100000000;
				
		
				System.gc();
			
		
	}

	@Override
	public RPHashObject getParam() {
		return so;
	}

	@Override
	public void setWeights(List<Float> counts) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setData(List<Centroid> centroids) {
		this.centroids = centroids;

	}

	@Override
	public void setRawData(List<float[]> centroids) {
		if (this.centroids == null)
			this.centroids = new ArrayList<>(centroids.size());
		for (float[] f : centroids) {
			this.centroids.add(new Centroid(f, 0));
		}
	}

	@Override
	public void setK(int getk) {
		this.so.setK(getk);
	}

	@Override
	public void reset(int randomseed) {
		centroids = null;
		so.setRandomSeed(randomseed);
	}

	@Override
	public boolean setMultiRun(int runs) {
		return false;
	}
	
	//@Override
	public void setCutoff(int getCutoff) {
		this.so.setCutoff(getCutoff);
	}
	
	//@Override
	public void setRandomVector(boolean getRandomVector) {
		this.so.setRandomVector(getRandomVector);
	}
	
	
}

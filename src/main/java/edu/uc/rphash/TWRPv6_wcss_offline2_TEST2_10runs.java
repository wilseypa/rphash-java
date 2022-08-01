package edu.uc.rphash;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
//import java.util.Arrays;
import java.util.HashMap;
//import java.util.Iterator;
//import java.util.LinkedHashMap;
import java.util.List;
//import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeSet;
import java.util.stream.Stream;

import edu.uc.rphash.Readers.RPHashObject;
import edu.uc.rphash.Readers.SimpleArrayReader;
import edu.uc.rphash.projections.Projector;
import edu.uc.rphash.tests.StatTests;
import edu.uc.rphash.tests.clusterers.Agglomerative3;
import edu.uc.rphash.tests.clusterers.KMeans2;
import edu.uc.rphash.tests.clusterers.Agglomerative3.ClusteringType;
import edu.uc.rphash.tests.generators.GenerateData;
import edu.uc.rphash.util.VectorUtil;

//import org.apache.commons.collections.map.MultiValueMap;
//import org.apache.commons.collections.map.*;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;



// this algorithm runs twrp 10 times : (only the random bisection vector varies, the Projection matrix remains same)
// and selects the one which has the best wcss  offline for the 10X candidate centroids.
public class TWRPv6_wcss_offline2_TEST2_10runs implements Clusterer, Runnable {

	boolean znorm = false;
	
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
	
	private List<Centroid> centroids = null;
	
	private RPHashObject so;

	public TWRPv6_wcss_offline2_TEST2_10runs(RPHashObject so) {
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

	
// This method finds the largest of the numbers and returns that index.

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
		
// this method is used to calculate the offline wcss	
//	UpdateHashMap_offlineWcss( CurrentCent, currentWcss, IncomingVector, incomingWcss );
	
	public static float[][] UpdateHashMap_offlineWcss(float[] x_1, float wcss_1,float[] x_2 ) {
		
		float wcss = wcss_1 + distancesq(x_1,x_2);
	    
	    
		float[][] ret = new float[3][];
		ret[0] = new float[1];
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
		
// this hash is to calculate the wcss
//	 hashvec2_forwcss(xt,x,IDAndCent,rngvec ,IDandWCSS);
	 
	 public long hashvec2_forwcss( float[] xt, float[] x, HashMap<Long, float[]>  MapOfIDAndCent,  float[] rngvec, HashMap<Long, Float>IDandWCSS_offline) {
			long s = 1;                                  //fixes leading 0's bug
			for (int i = 0; i < xt.length; i++) {
				s = s << 1 ;                             // left shift the bits of s by 1.
				if (xt[i] > rngvec[i])
					s= s+1;
								
				if (MapOfIDAndCent.containsKey(s)) {
					
					float CurrentCent [] = MapOfIDAndCent.get(s);
					float IncomingVector [] = x;
					
					
					float currentWcss= 0;
					
					if (IDandWCSS_offline.containsKey(s)) {
						currentWcss= IDandWCSS_offline.get(s);						
					}		
					
		        float[][] MergedValues = UpdateHashMap_offlineWcss( CurrentCent, currentWcss, IncomingVector );
										
					float wcss= MergedValues[2][0];
										
					IDandWCSS_offline.put(s, wcss);												
					
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
	
	// this method is used to compute the offline WCSS to choose the best of the clusters 
	//calcWCSSoffline(x, projector, MapOfIDAndCent1, rngvec, MapOfIDAandWCSS1_offline);
	
	void calcWCSSoffline(float[] x, Projector p, HashMap<Long, float[]> MapOfIDAndCent, float[] rngvec , HashMap<Long, Float> MapOfIDAandWCSS_offline) {
		
		float[] xt = p.project(x);
		
	   hashvec2_forwcss(xt,x,MapOfIDAndCent,rngvec ,MapOfIDAandWCSS_offline);
				
	}
		
	static boolean isPowerOfTwo(long num) {
		return (num & -num) == num;
	}
	
	/*
	 * X - data set k - canonical k in k-means l - clustering sub-space Compute
	 * density mode via iterative deepening hash counting
	 */
	
	public Multimap<Long, float[]>  findDensityModes2() {

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
	
	
	
	// #create projector matrixs
	Projector projector = so.getProjectionType();
	projector.setOrigDim(so.getdim());
	projector.setProjectedDim(so.getDimparameter());
	projector.setRandomSeed(so.getRandomSeed());
//	projector.setRandomSeed(535247432);
	
	projector.init();
	int cutoff = so.getCutoff();
	
	int ct = 0;

	{
		
		for (float[] x : so.getRawData()) 
		{
			addtocounter(x, projector, MapOfIDAndCent1, MapOfIDAndCount1,ct++, rngvec, MapOfIDAndWCSS1);
			addtocounter(x, projector, MapOfIDAndCent2, MapOfIDAndCount2,ct++, rngvec2,MapOfIDAndWCSS2);
			addtocounter(x, projector, MapOfIDAndCent3, MapOfIDAndCount3,ct++, rngvec3,MapOfIDAndWCSS3);
			addtocounter(x, projector, MapOfIDAndCent4, MapOfIDAndCount4,ct++, rngvec4,MapOfIDAndWCSS4);
			addtocounter(x, projector, MapOfIDAndCent5, MapOfIDAndCount5,ct++, rngvec5,MapOfIDAndWCSS5);
			
			addtocounter(x, projector, MapOfIDAndCent6, MapOfIDAndCount6,ct++, rngvec6, MapOfIDAndWCSS6);
			addtocounter(x, projector, MapOfIDAndCent7, MapOfIDAndCount7,ct++, rngvec7,MapOfIDAndWCSS7);
			addtocounter(x, projector, MapOfIDAndCent8, MapOfIDAndCount8,ct++, rngvec8,MapOfIDAndWCSS8);
			addtocounter(x, projector, MapOfIDAndCent9, MapOfIDAndCount9,ct++, rngvec9,MapOfIDAndWCSS9);
			addtocounter(x, projector, MapOfIDAndCent10, MapOfIDAndCount10,ct++, rngvec10,MapOfIDAndWCSS10);
					
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
	
	
	
	
	float WCSS1 = 0;
	float WCSS2 = 0;
	float WCSS3 = 0;
	float WCSS4 = 0;
	float WCSS5 = 0;
	float WCSS6 = 0;
	float WCSS7 = 0;
	float WCSS8 = 0;
	float WCSS9 = 0;
	float WCSS10 = 0;
	
	float WCSS_off_1 = 0;
	float WCSS_off_2 = 0;
	float WCSS_off_3 = 0;
	float WCSS_off_4 = 0;
	float WCSS_off_5 = 0;
	float WCSS_off_6 = 0;
	float WCSS_off_7 = 0;
	float WCSS_off_8 = 0;
	float WCSS_off_9 = 0;
	float WCSS_off_10 = 0;
	
	HashMap<Long, Long> denseSetOfIDandCount2 = new HashMap<Long, Long>();
	HashMap<Long, float[]> MapOfIDAndCent = new HashMap<>();  
	HashMap<Long, Long> MapOfIDAndCount = new HashMap<>();
	HashMap<Long, Float> MapOfIDAndWCSS =  new HashMap<>();
	
	HashMap<Long, Float> MapOfIDAandWCSS_offline_1 =  new HashMap<>();
	HashMap<Long, Float> MapOfIDAandWCSS_offline_2 =  new HashMap<>();
	HashMap<Long, Float> MapOfIDAandWCSS_offline_3 =  new HashMap<>();
	HashMap<Long, Float> MapOfIDAandWCSS_offline_4 =  new HashMap<>();
	HashMap<Long, Float> MapOfIDAandWCSS_offline_5 =  new HashMap<>();
	HashMap<Long, Float> MapOfIDAandWCSS_offline_6 =  new HashMap<>();
	HashMap<Long, Float> MapOfIDAandWCSS_offline_7 =  new HashMap<>();
	HashMap<Long, Float> MapOfIDAandWCSS_offline_8 =  new HashMap<>();
	HashMap<Long, Float> MapOfIDAandWCSS_offline_9 =  new HashMap<>();
	HashMap<Long, Float> MapOfIDAandWCSS_offline_10 =  new HashMap<>();
	
	
	// calculate the real wcss in offline fashion, so for the keys , hash the points into those buckets
	// and calculate the wcss as we know their centroids :
	

		for (float[] x : so.getRawData()) 
		{
			
			calcWCSSoffline(x, projector, MapOfIDAndCent1, rngvec , MapOfIDAandWCSS_offline_1);
			calcWCSSoffline(x, projector, MapOfIDAndCent2, rngvec2, MapOfIDAandWCSS_offline_2);
			calcWCSSoffline(x, projector, MapOfIDAndCent3, rngvec3, MapOfIDAandWCSS_offline_3);
			calcWCSSoffline(x, projector, MapOfIDAndCent4, rngvec4, MapOfIDAandWCSS_offline_4);
			calcWCSSoffline(x, projector, MapOfIDAndCent5, rngvec5, MapOfIDAandWCSS_offline_5);
			
			calcWCSSoffline(x, projector, MapOfIDAndCent6, rngvec6 , MapOfIDAandWCSS_offline_6);
			calcWCSSoffline(x, projector, MapOfIDAndCent7, rngvec7, MapOfIDAandWCSS_offline_7);
			calcWCSSoffline(x, projector, MapOfIDAndCent8, rngvec8, MapOfIDAandWCSS_offline_8);
			calcWCSSoffline(x, projector, MapOfIDAndCent9, rngvec9, MapOfIDAandWCSS_offline_9);
			calcWCSSoffline(x, projector, MapOfIDAndCent10, rngvec10, MapOfIDAandWCSS_offline_10);
		}	
	
	
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
	
//* THIS IS THE RUNTIME CALCULATION OF WCSS STATISTICS WHICH REQUIRES ANOTHER PASS OVER THE DATA: 	
	
		for (Long keys: sortedIDList2_1)
		{  WCSS_off_1  = WCSS_off_1  + MapOfIDAandWCSS_offline_1.get(keys);}
			
		for (Long keys: sortedIDList2_2)	
		{  WCSS_off_2  = WCSS_off_2  + MapOfIDAandWCSS_offline_2.get(keys);}
 		
		for (Long keys: sortedIDList2_3) 	
		{  WCSS_off_3  = WCSS_off_3  + MapOfIDAandWCSS_offline_3.get(keys);}
	
		for (Long keys: sortedIDList2_4)	
		{  WCSS_off_4  = WCSS_off_4  + MapOfIDAandWCSS_offline_4.get(keys);}
		
		for (Long keys: sortedIDList2_5)	
		{  WCSS_off_5  = WCSS_off_5  + MapOfIDAandWCSS_offline_5.get(keys);}
		
		for (Long keys: sortedIDList2_6)	
		{  WCSS_off_6  = WCSS_off_6  + MapOfIDAandWCSS_offline_6.get(keys);}
		
		for (Long keys: sortedIDList2_7)	
		{  WCSS_off_7  = WCSS_off_7  + MapOfIDAandWCSS_offline_7.get(keys);}
		
		for (Long keys: sortedIDList2_8)	
		{  WCSS_off_8  = WCSS_off_8  + MapOfIDAandWCSS_offline_8.get(keys);}
		
		for (Long keys: sortedIDList2_9)	
		{  WCSS_off_9  = WCSS_off_9  + MapOfIDAandWCSS_offline_9.get(keys);}
		
		for (Long keys: sortedIDList2_10)	
		{  WCSS_off_10  = WCSS_off_10  + MapOfIDAandWCSS_offline_10.get(keys);}
		
		
		
	
	System.out.print("wcss1 = " + WCSS1);
	System.out.println("          wcss_ofline_1 = " + WCSS_off_1);
	
	System.out.print("wcss2 = " + WCSS2);	
	System.out.println("          wcss_ofline_2 = " + WCSS_off_2);
	
	System.out.print("wcss3 = " + WCSS3);
	System.out.println("          wcss_ofline_3 = " + WCSS_off_3);
	
	System.out.print("wcss4 = " + WCSS4);
	System.out.println("          wcss_ofline_4 = " + WCSS_off_4);
	
	System.out.print("wcss5 = " + WCSS5);
	System.out.println("          wcss_ofline_5 = " + WCSS_off_5);
	
	System.out.print("wcss6 = " + WCSS6);
	System.out.println("          wcss_ofline_6 = " + WCSS_off_6);
	
	System.out.print("wcss7 = " + WCSS7);
	System.out.println("          wcss_ofline_7 = " + WCSS_off_7);
	
	System.out.print("wcss8 = " + WCSS8);
	System.out.println("          wcss_ofline_8 = " + WCSS_off_8);
	
	System.out.print("wcss9 = " + WCSS9);
	System.out.println("          wcss_ofline_9 = " + WCSS_off_9);
	
	System.out.print("wcss10 = " + WCSS10);
	System.out.println("          wcss_ofline_10 = " + WCSS_off_10);
	
	
	
	float arr[] = {WCSS_off_1,WCSS_off_2,WCSS_off_3,WCSS_off_4,WCSS_off_5,WCSS_off_6,WCSS_off_7,WCSS_off_8,WCSS_off_9,WCSS_off_10};
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
		

	// this is to be taken out . only done for hypothesis testing.
	
	boolean raw = Boolean.parseBoolean(("raw"));
	List<float[]> data = null;
	try {
		data = VectorUtil.readFile("/C:/Users/deysn/Desktop/temp/har/1D.txt", raw);
	} catch (FileNotFoundException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
	
	Multimap<Long, float[]> multimapWeightAndCent1 = ArrayListMultimap.create();
	for (Long keys: sortedIDList2_1)
	{
	  multimapWeightAndCent1.put((Long)(MapOfIDAndCount1.get(keys)), (float[]) (MapOfIDAndCent1.get(keys)));
	}
	
	Multimap<Long, float[]> multimapWeightAndCent2 = ArrayListMultimap.create();
	for (Long keys: sortedIDList2_2)	
	{
	  multimapWeightAndCent2.put((Long)(MapOfIDAndCount2.get(keys)), (float[]) (MapOfIDAndCent2.get(keys)));
	}
	
	Multimap<Long, float[]> multimapWeightAndCent3 = ArrayListMultimap.create();
	for (Long keys: sortedIDList2_3)	
	{
	  multimapWeightAndCent3.put((Long)(MapOfIDAndCount3.get(keys)), (float[]) (MapOfIDAndCent3.get(keys)));
	}
	
	Multimap<Long, float[]> multimapWeightAndCent4 = ArrayListMultimap.create();
	for (Long keys: sortedIDList2_4)	
	{
	  multimapWeightAndCent4.put((Long)(MapOfIDAndCount4.get(keys)), (float[]) (MapOfIDAndCent4.get(keys)));
	}
	
	Multimap<Long, float[]> multimapWeightAndCent5 = ArrayListMultimap.create();
	for (Long keys: sortedIDList2_5)	
	{
	  multimapWeightAndCent5.put((Long)(MapOfIDAndCount5.get(keys)), (float[]) (MapOfIDAndCent5.get(keys)));
	}
	
	Multimap<Long, float[]> multimapWeightAndCent6 = ArrayListMultimap.create();
	for (Long keys: sortedIDList2_6)	
	{
	  multimapWeightAndCent6.put((Long)(MapOfIDAndCount6.get(keys)), (float[]) (MapOfIDAndCent6.get(keys)));
	}
	Multimap<Long, float[]> multimapWeightAndCent7 = ArrayListMultimap.create();
	for (Long keys: sortedIDList2_7)	
	{
	  multimapWeightAndCent7.put((Long)(MapOfIDAndCount7.get(keys)), (float[]) (MapOfIDAndCent7.get(keys)));
	}
	
	Multimap<Long, float[]> multimapWeightAndCent8 = ArrayListMultimap.create();
	for (Long keys: sortedIDList2_8)	
	{
	  multimapWeightAndCent8.put((Long)(MapOfIDAndCount8.get(keys)), (float[]) (MapOfIDAndCent8.get(keys)));
	}
	
	Multimap<Long, float[]> multimapWeightAndCent9 = ArrayListMultimap.create();
	for (Long keys: sortedIDList2_9)	
	{
	  multimapWeightAndCent9.put((Long)(MapOfIDAndCount9.get(keys)), (float[]) (MapOfIDAndCent9.get(keys)));
	}
	
	Multimap<Long, float[]> multimapWeightAndCent10 = ArrayListMultimap.create();
	for (Long keys: sortedIDList2_10)	
	{
	  multimapWeightAndCent10.put((Long)(MapOfIDAndCount10.get(keys)), (float[]) (MapOfIDAndCent10.get(keys)));
	}
	
	
	
	List<float[]>centroids1 = new ArrayList<>();
	List<Float> weights1 =new ArrayList<>();
	for (Long weights : multimapWeightAndCent1.keys())						
	{	
	weights1.add((float)weights);
	}

	for (Long weight : multimapWeightAndCent1.keySet())	
		
	{
	    centroids1.addAll(multimapWeightAndCent1.get(weight));				
	}
	
//	Agglomerative3 aggloOffline =  new Agglomerative3(ClusteringType.AVG_LINKAGE,centroids1, so.getk());
//	aggloOffline.setWeights(weights1);
//	List<Centroid> finalcentroids_1 = aggloOffline.getCentroids();
		
	KMeans2 Offline = new KMeans2();
	Offline.setK(so.getk());
	Offline.setRawData(centroids1);
	Offline.setWeights(weights1);
	List<Centroid> finalcentroids_1 = Offline.getCentroids();

	
	
	List<float[]>centroids2 = new ArrayList<>();
	List<Float> weights2 =new ArrayList<>();
	for (Long weights : multimapWeightAndCent2.keys())						
	{	
	weights2.add((float)weights);
	}

	for (Long weight : multimapWeightAndCent2.keySet())	
		
	{
	    centroids2.addAll(multimapWeightAndCent1.get(weight));				
	}
	
//	Agglomerative3 aggloOffline2 =  new Agglomerative3(ClusteringType.AVG_LINKAGE,centroids2, so.getk());
//	aggloOffline2.setWeights(weights2);
//	List<Centroid> finalcentroids_2 = aggloOffline2.getCentroids();
		
	KMeans2 Offline2 = new KMeans2();
	Offline2.setK(so.getk());
	Offline2.setRawData(centroids2);
	Offline2.setWeights(weights2);
  List<Centroid> finalcentroids_2 = Offline2.getCentroids();

	
	List<float[]>centroids3 = new ArrayList<>();
	List<Float> weights3 =new ArrayList<>();
	for (Long weights : multimapWeightAndCent3.keys())						
	{	
	weights3.add((float)weights);
	}

	for (Long weight : multimapWeightAndCent3.keySet())	
		
	{
	    centroids3.addAll(multimapWeightAndCent3.get(weight));				
	}
	
//	Agglomerative3 aggloOffline3 =  new Agglomerative3(ClusteringType.AVG_LINKAGE,centroids3, so.getk());
//	aggloOffline3.setWeights(weights3);
//	List<Centroid> finalcentroids_3 = aggloOffline3.getCentroids();
		
	KMeans2 Offline3 = new KMeans2();
	Offline3.setK(so.getk());
	Offline3.setRawData(centroids3);
	Offline3.setWeights(weights3);
	List<Centroid> finalcentroids_3 = Offline3.getCentroids();

	List<float[]>centroids4 = new ArrayList<>();
	List<Float> weights4 =new ArrayList<>();
	for (Long weights : multimapWeightAndCent4.keys())						
	{	
	weights4.add((float)weights);
	}

	for (Long weight : multimapWeightAndCent4.keySet())	
		
	{
	    centroids4.addAll(multimapWeightAndCent4.get(weight));				
	}
	
//	Agglomerative3 aggloOffline4 =  new Agglomerative3(ClusteringType.AVG_LINKAGE,centroids4, so.getk());
//	aggloOffline4.setWeights(weights4);
//	List<Centroid> finalcentroids_4 = aggloOffline4.getCentroids();
		
	KMeans2 Offline4 = new KMeans2();
	Offline4.setK(so.getk());
	Offline4.setRawData(centroids4);
	Offline4.setWeights(weights4);
	List<Centroid> finalcentroids_4 = Offline4.getCentroids();	
	

	List<float[]>centroids5 = new ArrayList<>();
	List<Float> weights5 =new ArrayList<>();
	for (Long weights : multimapWeightAndCent5.keys())						
	{	
	weights5.add((float)weights);
	}

	for (Long weight : multimapWeightAndCent5.keySet())	
		
	{
	    centroids5.addAll(multimapWeightAndCent5.get(weight));				
	}
	
//	Agglomerative3 aggloOffline5 =  new Agglomerative3(ClusteringType.AVG_LINKAGE,centroids5, so.getk());
//	aggloOffline5.setWeights(weights5);
//	List<Centroid> finalcentroids_5 = aggloOffline5.getCentroids();
		
	KMeans2 Offline5 = new KMeans2();
	Offline5.setK(so.getk());
	Offline5.setRawData(centroids5);
	Offline5.setWeights(weights5);
	List<Centroid> finalcentroids_5 = Offline5.getCentroids();
	
	
	List<float[]>centroids6 = new ArrayList<>();
	List<Float> weights6 =new ArrayList<>();
	for (Long weights : multimapWeightAndCent6.keys())						
	{	
	weights6.add((float)weights);
	}

	for (Long weight : multimapWeightAndCent6.keySet())	
		
	{
	    centroids6.addAll(multimapWeightAndCent6.get(weight));				
	}
	
//	Agglomerative3 aggloOffline6 =  new Agglomerative3(ClusteringType.AVG_LINKAGE,centroids6, so.getk());
//	aggloOffline6.setWeights(weights6);
//	List<Centroid> finalcentroids_6 = aggloOffline6.getCentroids();
		
	KMeans2 Offline6 = new KMeans2();
	Offline6.setK(so.getk());
	Offline6.setRawData(centroids6);
	Offline6.setWeights(weights6);
	List<Centroid> finalcentroids_6 = Offline6.getCentroids();
	
	List<float[]>centroids7 = new ArrayList<>();
	List<Float> weights7 =new ArrayList<>();
	for (Long weights : multimapWeightAndCent7.keys())						
	{	
	weights7.add((float)weights);
	}

	for (Long weight : multimapWeightAndCent7.keySet())	
		
	{
	    centroids7.addAll(multimapWeightAndCent7.get(weight));				
	}
	
//	Agglomerative3 aggloOffline7 =  new Agglomerative3(ClusteringType.AVG_LINKAGE,centroids7, so.getk());
//	aggloOffline7.setWeights(weights7);
//	List<Centroid> finalcentroids_7 = aggloOffline7.getCentroids();
		
	KMeans2 Offline7 = new KMeans2();
	Offline7.setK(so.getk());
	Offline7.setRawData(centroids7);
	Offline7.setWeights(weights7);
	List<Centroid> finalcentroids_7 = Offline7.getCentroids();

	
	List<float[]>centroids8 = new ArrayList<>();
	List<Float> weights8 =new ArrayList<>();
	for (Long weights : multimapWeightAndCent8.keys())						
	{	
	weights8.add((float)weights);
	}

	for (Long weight : multimapWeightAndCent8.keySet())	
		
	{
	    centroids8.addAll(multimapWeightAndCent8.get(weight));				
	}
	
//	Agglomerative3 aggloOffline8 =  new Agglomerative3(ClusteringType.AVG_LINKAGE,centroids8, so.getk());
//	aggloOffline8.setWeights(weights8);
//	List<Centroid> finalcentroids_8 = aggloOffline8.getCentroids();
		
	KMeans2 Offline8 = new KMeans2();
	Offline8.setK(so.getk());
	Offline8.setRawData(centroids8);
	Offline8.setWeights(weights8);
	List<Centroid> finalcentroids_8 = Offline8.getCentroids();
	
	
	List<float[]>centroids9 = new ArrayList<>();
	List<Float> weights9 =new ArrayList<>();
	for (Long weights : multimapWeightAndCent9.keys())						
	{	
	weights9.add((float)weights);
	}

	for (Long weight : multimapWeightAndCent9.keySet())	
		
	{
	    centroids9.addAll(multimapWeightAndCent9.get(weight));				
	}
	
//	Agglomerative3 aggloOffline9 =  new Agglomerative3(ClusteringType.AVG_LINKAGE,centroids9, so.getk());
//	aggloOffline9.setWeights(weights9);
//	List<Centroid> finalcentroids_9 = aggloOffline9.getCentroids();
		
	KMeans2 Offline9 = new KMeans2();
	Offline9.setK(so.getk());
	Offline9.setRawData(centroids9);
	Offline9.setWeights(weights9);
	List<Centroid> finalcentroids_9 = Offline9.getCentroids();
	
	
	List<float[]>centroids10 = new ArrayList<>();
	List<Float> weights10 =new ArrayList<>();
	for (Long weights : multimapWeightAndCent10.keys())						
	{	
	weights10.add((float)weights);
	}

	for (Long weight : multimapWeightAndCent10.keySet())	
		
	{
	    centroids10.addAll(multimapWeightAndCent10.get(weight));				
	}
	
//	Agglomerative3 aggloOffline10 =  new Agglomerative3(ClusteringType.AVG_LINKAGE,centroids10, so.getk());
//	aggloOffline10.setWeights(weights10);
//	List<Centroid> finalcentroids_10 = aggloOffline10.getCentroids();
		
	KMeans2 Offline10 = new KMeans2();
	Offline10.setK(so.getk());
	Offline10.setRawData(centroids10);
	Offline10.setWeights(weights10);
	List<Centroid> finalcentroids_10 = Offline10.getCentroids();
	
	
	VectorUtil.writeCentroidsToFile(new File("/C:/Users/deysn/Desktop/temp/har/run_results/10runs/OutputTwrpCents_tree1"),finalcentroids_1, false);	

	System.out.printf("%.0f\t",	StatTests.WCSSECentroidsFloat(finalcentroids_1, data));
	
	VectorUtil.writeCentroidsToFile(new File("/C:/Users/deysn/Desktop/temp/har/run_results/10runs/OutputTwrpCents_tree2"),finalcentroids_2, false);	

	System.out.printf("%.0f\t",	StatTests.WCSSECentroidsFloat(finalcentroids_2, data));
	
	VectorUtil.writeCentroidsToFile(new File("/C:/Users/deysn/Desktop/temp/har/run_results/10runs/OutputTwrpCents_tree3"),finalcentroids_3, false);	

	System.out.printf("%.0f\t",	StatTests.WCSSECentroidsFloat(finalcentroids_3, data));
	
	VectorUtil.writeCentroidsToFile(new File("/C:/Users/deysn/Desktop/temp/har/run_results/10runs/OutputTwrpCents_tree4"),finalcentroids_4, false);	

	System.out.printf("%.0f\t",	StatTests.WCSSECentroidsFloat(finalcentroids_4, data));
	
	VectorUtil.writeCentroidsToFile(new File("/C:/Users/deysn/Desktop/temp/har/run_results/10runs/OutputTwrpCents_tree5"),finalcentroids_5, false);	

	System.out.printf("%.0f\t",	StatTests.WCSSECentroidsFloat(finalcentroids_5, data));
	
	VectorUtil.writeCentroidsToFile(new File("/C:/Users/deysn/Desktop/temp/har/run_results/10runs/OutputTwrpCents_tree6"),finalcentroids_6, false);	

	System.out.printf("%.0f\t",	StatTests.WCSSECentroidsFloat(finalcentroids_6, data));
	
	VectorUtil.writeCentroidsToFile(new File("/C:/Users/deysn/Desktop/temp/har/run_results/10runs/OutputTwrpCents_tree7"),finalcentroids_7, false);	

	System.out.printf("%.0f\t",	StatTests.WCSSECentroidsFloat(finalcentroids_7, data));
	
	VectorUtil.writeCentroidsToFile(new File("/C:/Users/deysn/Desktop/temp/har/run_results/10runs/OutputTwrpCents_tree8"),finalcentroids_8, false);	

	System.out.printf("%.0f\t",	StatTests.WCSSECentroidsFloat(finalcentroids_8, data));
	
	VectorUtil.writeCentroidsToFile(new File("/C:/Users/deysn/Desktop/temp/har/run_results/10runs/OutputTwrpCents_tree9"),finalcentroids_9, false);	

	System.out.printf("%.0f\t",	StatTests.WCSSECentroidsFloat(finalcentroids_9, data));
	
	VectorUtil.writeCentroidsToFile(new File("/C:/Users/deysn/Desktop/temp/har/run_results/10runs/OutputTwrpCents_tree10"),finalcentroids_10, false);	

	System.out.printf("%.0f\t",	StatTests.WCSSECentroidsFloat(finalcentroids_10, data));
	

	
	return multimapWeightAndCent;
	
}
	
	public void run() {
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
		    } 
		
		else {
			for (int i = 0; i < so.getDimparameter(); i++)
			rngvec[i] = (float) 0;
			
			   }
		
		Multimap<Long, float[]> WeightAndClusters = findDensityModes2();
		
		List<float[]>centroids2 = new ArrayList<>();
		List<Float> weights2 =new ArrayList<>();
		
		
		System.out.println("\tNumberOfMicroClusters_AfterPruning = "+ WeightAndClusters.size());		
		System.out.println("getRandomVector = "+ randVect);
		

		for (Long weights : WeightAndClusters.keys())						
		{
			
		weights2.add((float)weights);

		}

		
		for (Long weight : WeightAndClusters.keySet())	
			
		{

		    centroids2.addAll(WeightAndClusters.get(weight));
					
		}	
	
		
//		Agglomerative3 aggloOffline =  new Agglomerative3(ClusteringType.AVG_LINKAGE,centroids2, so.getk());
//		aggloOffline.setWeights(weights2);
//		this.centroids = aggloOffline.getCentroids();

		KMeans2 aggloOffline2 = new KMeans2();
		aggloOffline2.setK(so.getk());
		aggloOffline2.setRawData(centroids2);
		aggloOffline2.setWeights(weights2);
		this.centroids = aggloOffline2.getCentroids();
			
	}
	

	public static void main(String[] args) throws FileNotFoundException,
			IOException {

		int k = 10;//6;
		int d = 200;//16;
		int n = 10000;
		float var = 1.5f;
		int count = 1;
	//	System.out.printf("ClusterVar\t");
	//	for (int i = 0; i < count; i++)
	//		System.out.printf("Trial%d\t", i);
	//	System.out.printf("RealWCSS\n");
		
		String Output = "/C:/Users/deysn/Desktop/temp/har/run_results/10runs/OutputTwrpCents_mainfunc_1" ;  

		    float f = var;
			float avgrealwcss = 0;
			float avgtime = 0;
	//		System.out.printf("%f\t", f);
	//			GenerateData gen = new GenerateData(k, n/k, d, f, true, .5f);
				
					// gen.writeCSVToFile(new File("/home/lee/Desktop/reclsh/in.csv"));	
					//	List<Float[]> data = "/C:/Users/user/Desktop/temp/OutputTwrpCents1" 
				
			//	RPHashObject o = new SimpleArrayReader(gen.data, k);
				
				boolean raw = Boolean.parseBoolean(("raw"));
				List<float[]> data = null;
				data = VectorUtil.readFile("/C:/Users/deysn/Desktop/temp/har/1D.txt", raw);
				k = 6;
				RPHashObject o = new SimpleArrayReader(data, 6);
					
				
				o.setDimparameter(16);
				o.setCutoff(60);
				o.setRandomVector(true);
				
//				System.out.println("cutoff = "+ o.getCutoff());
//				System.out.println("get_random_Vector = "+ o.getRandomVector());			
								
				TWRPv6_wcss_offline2_TEST2_10runs rphit = new TWRPv6_wcss_offline2_TEST2_10runs(o);
				long startTime = System.nanoTime();
				List<Centroid> centsr = rphit.getCentroids();

				avgtime += (System.nanoTime() - startTime) / 100000000;
				
//				avgrealwcss += StatTests.WCSSEFloatCentroid(gen.getMedoids(),gen.getData());
				
								
				VectorUtil.writeCentroidsToFile(new File(Output),centsr, false);	

//				System.out.printf("%.0f\t",	StatTests.WCSSECentroidsFloat(centsr, gen.data));
				System.out.printf("%.0f\t",	StatTests.WCSSECentroidsFloat(centsr, data));
				
				System.gc();
			
//			    System.out.printf("%.0f\n", avgrealwcss / count);
			
		
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

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
import edu.uc.rphash.tests.generators.GenerateData;
import edu.uc.rphash.util.VectorUtil;

//import org.apache.commons.collections.map.MultiValueMap;
//import org.apache.commons.collections.map.*;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;



public class TWRPv3 implements Clusterer, Runnable {

	boolean znorm = false;
	
	private int counter;
	private float[] rngvec;
	private float[] rngvec2;
	private float[] rngvec3;
	
	private List<Centroid> centroids = null;
	
	private RPHashObject so;

	public TWRPv3(RPHashObject so) {
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
	
	
	
	// combines two hashmaps of idsandcents
	
	public static HashMap<Long, float[]> mergehmapsidsandcents(
			HashMap<Long,  float[]> partidandcent1,
			HashMap<Long,  float[]> partidandcent2,
			HashMap<Long, Long> partidandcount1,
			HashMap<Long, Long> partidandcount2) 
{
		 // new empty map
		HashMap<Long, float[]> combined = new HashMap<>();  
		combined.putAll( partidandcent1);

		for(Long key :  partidandcent2.keySet()) {
		    if(combined.containsKey(key)) {
		    	
		    	
		    	Long weight1= partidandcount1.get(key);
		    	
		    	float[] cent1=  combined.get(key);
		    	
		    	Long weight2= partidandcount2.get(key);
		    	
		    	float [] cent2= partidandcent2.get(key);
		    	 
		    	float [][] joined = UpdateHashMap(weight1, cent1 ,weight2 , cent2 );
		    	float combinedCount =  joined[0][0];
		    	float [] combinedCent = joined[1];
		    	
		    	
		    	combined.put(key,combinedCent);
		    	
		    }
		    else {
		    	combined.put(key,partidandcent2.get(key));
		    }
		}
							
		return (combined);
		
}
	
	
	
	// combines two hashmaps of idsandcounts
	
	public static HashMap<Long, Long> mergehmapsidsandcounts(HashMap<Long, Long> partidandcount1,
			HashMap<Long, Long> partidandcountt2) 
	{
		
		
		HashMap<Long, Long> combined  = new HashMap<Long, Long> (); // new empty map
		combined.putAll(partidandcount1);
		
		
		
		for(Long key : partidandcountt2.keySet()) {
		    if(combined.containsKey(key)) {
		    	
		 	
		    	long value1 = combined.get(key);
		    	long value2 = partidandcountt2.get(key);
		    	long value3 = value1 + value2;
		  		    	
		    	combined.put(key,value3);	
		    	  	
		    	
		    } 
		    else {
		    	combined.put(key,partidandcountt2.get(key));
		    }
		    
		
		
	}
		return (combined);
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
	
// this updates the map two cents with different weigths are merged into one.
	public static float[][] UpdateHashMap(float cnt_1, float[] x_1, 
			float cnt_2, float[] x_2) {
		
		float cnt_r = cnt_1 + cnt_2;
		
		float[] x_r = new float[x_1.length];
		
		for (int i = 0; i < x_1.length; i++) {
			x_r[i] = (cnt_1 * x_1[i] + cnt_2 * x_2[i]) / cnt_r;
			
		}

		float[][] ret = new float[2][];
		ret[0] = new float[1];
		ret[0][0] = cnt_r;
		ret[1] = x_r;
		return ret;
	}
		

	public long hashvec2( float[] xt, float[] x,
			HashMap<Long, float[]>  MapOfIDAndCent, HashMap<Long, Long>  MapOfIDAndCount,int ct, float[] rngvec) {
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
				
				float[][] MergedValues = UpdateHashMap(CurrentCount , CurrentCent, CountForIncomingVector, IncomingVector );
				
			  	Long UpdatedCount = (long) MergedValues[0][0] ;
			  	
				float[] MergedVector = MergedValues[1] ;
				
				MapOfIDAndCount.put(s , UpdatedCount);
				
				MapOfIDAndCent.put(s, MergedVector);
						
			} 
				
			else {
							
				float[] xlist = x;
				MapOfIDAndCent.put(s, xlist);
				MapOfIDAndCount.put(s, (long)1);
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
			HashMap<Long, float[]> IDAndCent,HashMap<Long, Long> IDandID,int ct, float[] rngvec) {
		float[] xt = p.project(x);
		
		hashvec2(xt,x,IDAndCent, IDandID,ct,rngvec );
	}
	
	void addtocounter(float[] x, Projector p,
			HashMap<Long,float[]> IDAndCent,HashMap<Long, Long> IDandID,int ct,float[] mean,float[] variance, float[] rngvec )
	{
		float[] xt = p.project(StatTests.znormvec(x, mean, variance));

		
		hashvec2(xt,x,IDAndCent, IDandID,ct, rngvec);
	}

	static boolean isPowerOfTwo(long num) {
		return (num & -num) == num;
	}
	

	

	/*
	 * X - data set k - canonical k in k-means l - clustering sub-space Compute
	 * density mode via iterative deepening hash counting
	 */
	
	
	public Multimap<Long, float[]>  findDensityModes2() {
	//public Map<Long, float[]>  findDensityModes2() {
	HashMap<Long, float[]> MapOfIDAndCent1 = new HashMap<>();  
	HashMap<Long, Long> MapOfIDAndCount1 = new HashMap<>();
	
	
	HashMap<Long, float[]> MapOfIDAndCent2 = new HashMap<>();  
	HashMap<Long, Long> MapOfIDAndCount2 = new HashMap<>();
	
	
	HashMap<Long, float[]> MapOfIDAndCent3 = new HashMap<>();  
	HashMap<Long, Long> MapOfIDAndCount3 = new HashMap<>();
	
	HashMap<Long, float[]> MapOfIDAndCent = new HashMap<>();  
	HashMap<Long, Long> MapOfIDAndCount = new HashMap<>();
	
	
	// #create projector matrixs
	Projector projector = so.getProjectionType();
	projector.setOrigDim(so.getdim());
	projector.setProjectedDim(so.getDimparameter());
	projector.setRandomSeed(so.getRandomSeed());
	projector.init();
	int cutoff = so.getCutoff();
	
	int ct = 0;

	{
		
		for (float[] x : so.getRawData()) 
		{
			addtocounter(x, projector, MapOfIDAndCent1, MapOfIDAndCount1,ct++, rngvec);
			addtocounter(x, projector, MapOfIDAndCent2, MapOfIDAndCount2,ct++, rngvec2);
			addtocounter(x, projector, MapOfIDAndCent2, MapOfIDAndCount2,ct++, rngvec3);
			
			
		}
	}
	
	
	MapOfIDAndCount=mergehmapsidsandcounts(MapOfIDAndCount1, MapOfIDAndCount2);
	
	MapOfIDAndCent = mergehmapsidsandcents(MapOfIDAndCent1,MapOfIDAndCent2,MapOfIDAndCount1, MapOfIDAndCount2 );

	MapOfIDAndCent = mergehmapsidsandcents(MapOfIDAndCent,MapOfIDAndCent3,MapOfIDAndCount, MapOfIDAndCount3 );
	
	MapOfIDAndCount=mergehmapsidsandcounts(MapOfIDAndCount, MapOfIDAndCount3);
	
	
	
	
	
		
	System.out.println("NumberOfMicroClustersBeforePruning = "+ MapOfIDAndCent.size());
	
	// next we want to prune the tree by parent count comparison
	// follows breadthfirst search
	
	
	
	
	HashMap<Long, Long> denseSetOfIDandCount2 = new HashMap<Long, Long>();
	for (Long cur_id : new TreeSet<Long>(MapOfIDAndCount.keySet())) 
	{
		if (cur_id >so.getk()){
            int cur_count = (int) (MapOfIDAndCount.get(cur_id).longValue());
            long parent_id = cur_id>>>1;
            int parent_count = (int) (MapOfIDAndCount.get(parent_id).longValue());
            
            if(cur_count!=0 && parent_count!=0)
            {
	            if(cur_count == parent_count) {
					denseSetOfIDandCount2.put(parent_id, 0L);
				//	IDAndCent.put(parent_id, new ArrayList<>());
					
//HashMap<Long, List<float[]>> IDAndCent = new HashMap<>(); and HashMap<Long, float[]> MapOfIDAndCent = new HashMap<Long, float[]>();
					
					MapOfIDAndCent.put(parent_id, new float[]{});
					
		//			MapOfIDAndCount.put(parent_id, new Long (0));
					
					denseSetOfIDandCount2.put(cur_id, (long) cur_count);
					
	            }
	            else
	            {
					if(2 * cur_count > parent_count) {
						denseSetOfIDandCount2.remove(parent_id);
						
					//	IDAndCent.put(parent_id, new ArrayList<>());
						MapOfIDAndCent.put(parent_id, new float[]{});
				//			MapOfIDAndCount.put(parent_id, new Long (0));	
						
						denseSetOfIDandCount2.put(cur_id, (long) cur_count);
					}
	            }
            }
		}
	}
	
	
	System.out.println("NumberOfMicroClustersAfterPruning&beforesortingLimit = "+ denseSetOfIDandCount2.size());
	
	//remove keys with support less than 1
	Stream<Entry<Long, Long>> stream2 = denseSetOfIDandCount2.entrySet().stream().filter(p -> p.getValue() > 1);
	//64 so 6 bits?
	//stream = stream.filter(p -> p.getKey() > 64);
	
//	Stream<Entry<Long, Long>> stream3 = denseSetOfIDandCount2.entrySet().stream().filter(p -> p.getValue() > 1);
//	long counter= stream3.count();
//	System.out.println("NumberOfMicroClustersAfterPruning&limit_the_1s = "+ counter);
	
//	int cutoff= so.getk()*8;
//	if (so.getk()*6 < 210) {   cutoff=210+so.getk();}  else { cutoff = so.getk()*8;}	
//	int cutoff = clustermembers.size()>200+so.getk()?200+so.getk():clustermembers.size();
//	System.out.println("Cutoff = "+ cutoff);
	
	List<Long> sortedIDList2= new ArrayList<>();
	// sort and limit the list
	stream2.sorted(Entry.<Long, Long> comparingByValue().reversed()).limit(cutoff)
			.forEachOrdered(x -> sortedIDList2.add(x.getKey()));
		
//	HashMap<Long, float[]> KeyAndCent = new HashMap<>();
//	HashMap<Long, Long> KeyAndCount = new HashMap<>();
//	Map<Long, float[]> WeightAndCent = new HashMap<>();
//	Map<Long, float[]> WeightAndCent = new LinkedHashMap<>();
	Multimap<Long, float[]> multimapWeightAndCent = ArrayListMultimap.create();

	

//	  
	for (Long keys: sortedIDList2)
		
		{
//		  WeightAndCent.put((Long)(MapOfIDAndCount.get(keys)), (float[]) (MapOfIDAndCent.get(keys)));
		  
		  multimapWeightAndCent.put((Long)(MapOfIDAndCount.get(keys)), (float[]) (MapOfIDAndCent.get(keys)));
			
//		  KeyAndCent.put(keys, MapOfIDAndCent.get(keys));			
//		  KeyAndCount.put(keys, MapOfIDAndCount.get(keys));
				
		}
		
	
		
	return multimapWeightAndCent;
	
}
	
	
	public void run() {
		rngvec = new float[so.getDimparameter()];
		
		
		rngvec2 = new float[so.getDimparameter()];
		
		rngvec3 = new float[so.getDimparameter()];
		
		counter = 0;
		boolean randVect = so.getRandomVector();
		
	//  Random r = new Random(so.getRandomSeed());
	//	Random r = new Random(3800635955020675334L) ;
		Random r = new Random();
		Random r2 = new Random();
		Random r3 = new Random();
		
		if (randVect==true){
		for (int i = 0; i < so.getDimparameter(); i++)
			rngvec[i] = (float) r.nextGaussian();
		
		for (int i = 0; i < so.getDimparameter(); i++)
		    rngvec2[i] = (float) r2.nextGaussian();
		
		for (int i = 0; i < so.getDimparameter(); i++)
		    rngvec3[i] = (float) r3.nextGaussian();
			
		
		    } else {
			for (int i = 0; i < so.getDimparameter(); i++)
			rngvec[i] = (float) 0;
			   }
		
		
		
		
		
	
		Multimap<Long, float[]> WeightAndClusters = findDensityModes2();
		//Map<Long, float[]> WeightAndClusters = findDensityModes2();
		
		
		
		
		List<float[]>centroids2 = new ArrayList<>();
		List<Float> weights2 =new ArrayList<>();
		
		
		System.out.println("NumberOfMicroClusters_AfterPruning = "+ WeightAndClusters.size());		
		System.out.println("getRandomVector = "+ randVect);
		
	//	int k = NumberOfMicroClusters>200+so.getk()?200+so.getk():NumberOfMicroClusters;
		
	// have to prune depending  NumberOfMicroClusters returned.
	//	int i = 1;
	//	int j=1;
	//	for (Long weights : new TreeSet<Long>(WeightAndClusters.keySet()))
		for (Long weights : WeightAndClusters.keys())						
		{
	//		System.out.println("NumberOfTreesetkeys = "+ i);			
	//		String key =weights.toString(); 
	//      System.out.println(weights); 			
		weights2.add((float)weights);
		//	centroids2.add(WeightAndClusters.get(weights));
		//	centroids2.addAll(WeightAndClusters.get(weights));			
	//	i=i+1;
		}
	//	System.out.println("done printing keys for weights");
		
		for (Long weight : WeightAndClusters.keySet())	
			
		{
	//		System.out.println(weight);
	//		System.out.println("NumberOfTreesetkeys = "+ j);
		    centroids2.addAll(WeightAndClusters.get(weight));
					
	//	j=j+1;
		}	
	//	System.out.println("done printing keys for centroids");
		
	//	System.out.println(weights2.size());
	//	System.out.println(centroids2.size());
		
		//System.out.printf("\tvalueofK is ");
		//System.out.println( so.getk());
		
		Agglomerative3 aggloOffline =  new Agglomerative3(centroids2, so.getk());
		
		aggloOffline.setWeights(weights2);
		
		this.centroids = aggloOffline.getCentroids();
			
	}
	

	public static void main(String[] args) throws FileNotFoundException,
			IOException {

		int k = 10;//6;
		int d = 700;//16;
		int n = 10000;
		float var = 1.5f;
		int count = 1;
	//	System.out.printf("ClusterVar\t");
	//	for (int i = 0; i < count; i++)
	//		System.out.printf("Trial%d\t", i);
	//	System.out.printf("RealWCSS\n");
		
		String Output = "/C:/Users/deysn/Desktop/temp/OutputTwrpCents1" ;  

		    float f = var;
			float avgrealwcss = 0;
			float avgtime = 0;
	//		System.out.printf("%f\t", f);
				GenerateData gen = new GenerateData(k, n/k, d, f, true, .5f);
				
				// gen.writeCSVToFile(new File("/home/lee/Desktop/reclsh/in.csv"));
				
			//	List<Float[]> data = "/C:/Users/user/Desktop/temp/OutputTwrpCents1" 
						
				RPHashObject o = new SimpleArrayReader(gen.data, k);
						
				o.setDimparameter(20);
	
				o.setCutoff(100);
				o.setRandomVector(true);
				
//				System.out.println("cutoff = "+ o.getCutoff());
//				System.out.println("get_random_Vector = "+ o.getRandomVector());			
								
				TWRPv2 rphit = new TWRPv2(o);
				long startTime = System.nanoTime();
				List<Centroid> centsr = rphit.getCentroids();

				avgtime += (System.nanoTime() - startTime) / 100000000;
				
				avgrealwcss += StatTests.WCSSEFloatCentroid(gen.getMedoids(),
						gen.getData());
				
				VectorUtil.writeCentroidsToFile(new File(Output),centsr, false);	

				System.out.printf("%.0f\t",	StatTests.WCSSECentroidsFloat(centsr, gen.data));
				System.gc();
			
			    System.out.printf("%.0f\n", avgrealwcss / count);
			
		
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

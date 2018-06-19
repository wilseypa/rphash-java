package edu.uc.rphash;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeSet;
import java.util.stream.Stream;
import java.util.Map;


import edu.uc.rphash.Readers.RPHashObject;
import edu.uc.rphash.Readers.SimpleArrayReader;
import edu.uc.rphash.projections.Projector;
import edu.uc.rphash.tests.StatTests;
import edu.uc.rphash.tests.clusterers.Agglomerative3;
import edu.uc.rphash.tests.generators.GenerateData;
import edu.uc.rphash.util.VectorUtil;



public class TWRPv2 implements Clusterer, Runnable {

	boolean znorm = false;
	
	
	private int counter;
	private float[] rngvec;
	private List<Centroid> centroids = null;
	
	
	private RPHashObject so;

	public TWRPv2(RPHashObject so) {
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
	
	
	
	

	//float[] rngvec; the range vector is moot if incoming data has been normalized
	//post normalization it should all be zero centered, with variance 1

	/*
	 * super simple hash algorithm, reminiscient of pstable lsh
	 */
	// xt is the projected vector and x is the original vector , rngvec is the randomly generated vector of projected dim.
	
	public long hashvec(float[] xt, float[] x,
			HashMap<Long, List<float[]>> IDAndCent, HashMap<Long, List<Integer>> IDAndLabel,int ct) {
		long s = 1;                                  //fixes leading 0's bug
		for (int i = 0; i < xt.length; i++) {
//			s <<= 1;
			s = s << 1 ;                             // left shift the bits of s by 1.
			if (xt[i] > rngvec[i])
//				s +=  1;
				s= s+1;
			
			if (IDAndCent.containsKey(s)) {
				IDAndLabel.get(s).add(ct);
				IDAndCent.get(s).add(x);
			} else {
				List<float[]> xlist = new ArrayList<>();
				xlist.add(x);
				IDAndCent.put(s, xlist);
				List<Integer> idlist = new ArrayList<>();
				idlist.add(ct);
				IDAndLabel.put(s, idlist);
			}
		}
		return s;
	}
	
	public long hashvec2(float[] xt, float[] x,
			HashMap<Long, float[]>  MapOfIDAndCent, HashMap<Long, Long>  MapOfIDAndCount,int ct) {
		long s = 1;                                  //fixes leading 0's bug
		for (int i = 0; i < xt.length; i++) {
//			s <<= 1;
			s = s << 1 ;                             // left shift the bits of s by 1.
			if (xt[i] > rngvec[i])
//				s +=  1;
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
			HashMap<Long, List<float[]>> IDAndCent,HashMap<Long, List<Integer>> IDandID,int ct) {
		float[] xt = p.project(x);
		
//		counter++;    
//		for(int i = 0;i<xt.length;i++){
//			float delta = xt[i]-rngvec[i];
//			rngvec[i] += delta/(float)counter;
//		}
		
		hashvec(xt,x,IDAndCent, IDandID,ct);
	}
	
	void addtocounter(float[] x, Projector p,
			HashMap<Long, List<float[]>> IDAndCent,HashMap<Long, List<Integer>> IDandID,int ct,float[] mean,float[] variance)
	{
		float[] xt = p.project(StatTests.znormvec(x, mean, variance));
		
//		counter++;    
//		for(int i = 0;i<xt.length;i++){
//			float delta = xt[i]-rngvec[i];
//			rngvec[i] += delta/(float)counter;
//		}
		
		hashvec(xt,x,IDAndCent, IDandID,ct);
	}

	static boolean isPowerOfTwo(long num) {
		return (num & -num) == num;
	}

	/*
	 * X - data set k - canonical k in k-means l - clustering sub-space Compute
	 * density mode via iterative deepening hash counting
	 */
	
	
	
	public HashMap<Long, float[]>  findDensityModes2() {
	HashMap<Long, List<float[]>> IDAndCent = new HashMap<>();
	HashMap<Long, List<Integer>> IDAndID = new HashMap<>();
	// #create projector matrixs
	Projector projector = so.getProjectionType();
	projector.setOrigDim(so.getdim());
	projector.setProjectedDim(so.getDimparameter());
	projector.setRandomSeed(so.getRandomSeed());
	projector.init();
	
	int ct = 0;
//	if(znorm == true){
//		float[] variance = StatTests.varianceCol(so.getRawData());
//		float[] mean = StatTests.meanCols(so.getRawData());
//		// #process data by adding to the counter
//		for (float[] x : so.getRawData()) 
//		{
//			addtocounter(x, projector, IDAndCent,IDAndID,ct++,mean,variance);
//		}
//	}
//	
//	else
	{
		
		for (float[] x : so.getRawData()) 
		{
			addtocounter(x, projector, IDAndCent, IDAndID,ct++);
		}
	}
	
	
	for (Long name: IDAndCent.keySet()){

        String key =name.toString();
        System.out.println(key );
                  
 //       	String value = IDAndCent.get(name).toString() ;           	
//        	String value1 = Arrays.toString(value.toString());
        	
//            System.out.println(key + " " + value);	
        

} 
	
	for (Long name: IDAndID.keySet()){

//         String key =name.toString();
//         String value = IDAndID.get(name).toString();  
//         System.out.println(key + " " + value);  


}
	
	// we would compress the hashmaps. SetOfIDandCount has the ids and the counts corresponding to that id.
	// we have two hashmaps: 1. IDAndCent and 2. IDAndID. we will use IDAndCent
	
	
	HashMap<Long, Long> MapOfIDAndCount = new HashMap<Long, Long>();
	
	HashMap<Long, float[]> MapOfIDAndCent = new HashMap<Long, float[]>();
	
	for (Long cur_id : new TreeSet<Long>(IDAndCent.keySet())) 
	{
            int cur_count = IDAndCent.get(cur_id).size();
            
            MapOfIDAndCount.put(cur_id, (long) cur_count);   // this has the hashids and counts.
            
            List<float[]> bucketpoints = new ArrayList<>();
            		
          	Iterator<float[]> e  = IDAndCent.get(cur_id).iterator();
          	        	
//          	int i=1;
          	while (e.hasNext()) {
          		
//          		System.out.println(i++);
           
          		bucketpoints.add(e.next())  ; 
          		
          		}
          	
          	 float [] bucketcent;
          	 
          	bucketcent = medoid(bucketpoints);
          	
          	MapOfIDAndCent.put(cur_id, bucketcent);     // this has the hashids and centroids.
          	
//          	System.out.println(cur_id + "    " + cur_count);
          	
 //         	int c = MapOfIDAndCent.get(cur_id).length;
          	
   //       	System.out.println(cur_id + "    " + c);
          	
            
	}
	
//	int NumberOfMicroClustersBeforePruning = MapOfIDAndCent.size() ;
//	System.out.println("NumberOfMicroClustersBeforePruning = "+ NumberOfMicroClustersBeforePruning);
	
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
	
	
	
	
	//remove keys with support less than 1

	
	Stream<Entry<Long, Long>> stream2 = denseSetOfIDandCount2.entrySet().stream().filter(p -> p.getValue() > 1);
	//64 so 6 bits?
	//stream = stream.filter(p -> p.getKey() > 64);


	
	List<Long> sortedIDList2= new ArrayList<>();
	// sort and limit the list
	stream2.sorted(Entry.<Long, Long> comparingByValue().reversed()).limit(so.getk()*4)
			.forEachOrdered(x -> sortedIDList2.add(x.getKey()));
	
	
	
	
	HashMap<Long, float[]> KeyAndCent = new HashMap<>();
	HashMap<Long, Long> KeyAndCount = new HashMap<>();
	HashMap<Long, float[]> WeightAndCent = new HashMap<>();
	
	for (int i =0; i<sortedIDList2.size();i++)
	{
		KeyAndCent.put(sortedIDList2.get(i), MapOfIDAndCent.get(sortedIDList2.get(i)));
		KeyAndCount.put(sortedIDList2.get(i), MapOfIDAndCount.get(sortedIDList2.get(i)));
		
		WeightAndCent.put(MapOfIDAndCount.get(sortedIDList2.get(i)), MapOfIDAndCent.get(sortedIDList2.get(i)));		
		
	}
		
	
	
	return WeightAndCent;
	
	
}
	
	
	public void run() {
		rngvec = new float[so.getDimparameter()];
		counter = 0;
		Random r = new Random(so.getRandomSeed());
		for (int i = 0; i < so.getDimparameter(); i++)
			rngvec[i] = (float) r.nextGaussian();
		
		
		HashMap<Long, float[]> WeightAndClusters = findDensityModes2();
		
		
		List<float[]>centroids2 = new ArrayList<>();
		List<Float> weights2 =new ArrayList<>();
		
		
		int NumberOfMicroClusters = WeightAndClusters.size() ;
		System.out.println("NumberOfMicroClusters = "+ NumberOfMicroClusters);
		
	//	int k = NumberOfMicroClusters>200+so.getk()?200+so.getk():NumberOfMicroClusters;
		
	// have to prune depending  NumberOfMicroClusters returned.
		
		for (Long weights : new TreeSet<Long>(WeightAndClusters.keySet()))
			
		{	
		weights2.add((float)weights);
		centroids2.add(WeightAndClusters.get(weights));
		}
		
			
		//System.out.printf("\tvalueofK is ");
		//System.out.println( so.getk());
		
		Agglomerative3 aggloOffline =  new Agglomerative3(centroids2, so.getk());
		
		//System.out.println(centroids2.size());
		aggloOffline.setWeights(weights2);
		
		this.centroids = aggloOffline.getCentroids();
		
		
	}
	
	
	
	

	public static void main(String[] args) throws FileNotFoundException,
			IOException {

		int k = 5;//6;
		int d = 100;//16;
		int n = 5000;
		float var = 1.5f;
		int count = 1;
	//	System.out.printf("ClusterVar\t");
	//	for (int i = 0; i < count; i++)
	//		System.out.printf("Trial%d\t", i);
	//	System.out.printf("RealWCSS\n");
		
		String Output = "/C:/Users/user/Desktop/temp/OutputTwrpCents" ; 

		    float f = var;
			float avgrealwcss = 0;
			float avgtime = 0;
	//		System.out.printf("%f\t", f);
				GenerateData gen = new GenerateData(k, n/k, d, f, true, .5f);
				// gen.writeCSVToFile(new
				// File("/home/lee/Desktop/reclsh/in.csv"));
				RPHashObject o = new SimpleArrayReader(gen.data, k);
				o.setDimparameter(8);
								
				TWRPv2 rphit = new TWRPv2(o);
				long startTime = System.nanoTime();
				List<Centroid> centsr = rphit.getCentroids();

				avgtime += (System.nanoTime() - startTime) / 100000000;
				
				avgrealwcss += StatTests.WCSSEFloatCentroid(gen.getMedoids(),
						gen.getData());
				
				VectorUtil.writeCentroidsToFile(new File(Output),centsr, false);	

	//			System.out.printf("%.0f\t",
	//					StatTests.WCSSECentroidsFloat(centsr, gen.data));
	//			System.gc();
			
	//		System.out.printf("%.0f\n", avgrealwcss / count);
			
		
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
}

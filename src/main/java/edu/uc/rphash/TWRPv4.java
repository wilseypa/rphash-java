package edu.uc.rphash;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;


public class TWRPv4 implements Clusterer, Runnable {

	boolean znorm = false;
	
	private int counter;
	private List<Centroid> centroids = null;
	private float[] bisectionVector;
	
	private RPHashObject so;

	public TWRPv4(RPHashObject so) {
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
	
// this updates the map two cents with different weigths are merged into one.
	public static float[][] UpdateHashMap(float cnt_1, float[] x_1, 
			float cnt_2, float[] x_2) {
		
		float cnt_r = cnt_1 + cnt_2;

		float[] x_r = new float[x_1.length];

		float[] var_r1 = new float[x_1.length];
		float[] var_r2 = new float[x_1.length];

		double var1=0;
		double var2=0;
		

		for (int i = 0; i < x_1.length; i++) {
			x_r[i] = (cnt_1 * x_1[i] + cnt_2 * x_2[i]) / cnt_r;

			var_r1[i] = ((-x_r[i] + x_1[i]) * (-x_r[i] + x_1[i]))/1000000000;

			var_r2[i] =(((-x_r[i] + x_2[i]) * (-x_r[i] + x_2[i])))/1000000000;
			
		}

		for (int i = 0; i < var_r1.length; i++) {
		var1 = var1 + var_r1[i];

		var2 = var2 + var_r2[i];
							}


	//    System.out.println("wcsse = " + wcsse);


	    float[][] ret = new float[3][];
		ret[0] = new float[1];
		ret[0][0] = cnt_r;
		ret[1] = x_r;
		ret[2]= new float [1];
		return ret;
	}
	
	
	public static float[][] UpdateHashMap_actual(float cnt_1, float[] x_1, 
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
	
	
	public long hashvec2(float[] xt, float[] x,
			HashMap<Long, float[]>  MapOfIDAndCent, HashMap<Long, Long>  MapOfIDAndCount,int ct, float[] bisectionVector) {
		
//		for (int i=0 ; i<bisectionVector.length; i++) 
//		{System.out.println(" bisectionvector in tree building = " + bisectionVector[i]);}	
		
		long s = 1;                                  //fixes leading 0's bug
		for (int i = 0; i < xt.length; i++) {
//			s <<= 1;
			s = s << 1 ;                             // left shift the bits of s by 1.
			if (xt[i] > bisectionVector[i])
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
			HashMap<Long, float[]> IDAndCent,HashMap<Long, Long> IDandID,int ct , float[]bisectionVector) {
		float[] xt = p.project(x);
		

		hashvec2(xt,x,IDAndCent, IDandID,ct,bisectionVector);
	}
	

	static boolean isPowerOfTwo(long num) {
		return (num & -num) == num;
	}

	/*
	 * X - data set k - canonical k in k-means l - clustering sub-space Compute
	 * density mode via iterative deepening hash counting
	 */
	
	public Multimap<Long, float[]>  findDensityModes2() {
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

	
	int countformean=0;
	for (float[] x : so.getRawData()) 
	{
		
		float[] temp = projector.project(x);
		
				for (int i=0 ; i<bisectionVector.length; i++) {
					
//					System.out.println("count = " + countformean);
//					System.out.println(" vectors = " + temp[i]);
					bisectionVector[i]= (countformean * bisectionVector[i] + 1* temp[i])/(countformean+1);
//					System.out.println(" vectors mean = " + bisectionVector[i]);
				
					}
		countformean = countformean+1;
		
	}
	
	
	
//	for (int i=0 ; i<bisectionVector.length; i++) 
//	{System.out.println(" bisection vector after creation = " + bisectionVector[i]);}
	
	
	{
		
		for (float[] x : so.getRawData()) 
		{
			addtocounter(x, projector, MapOfIDAndCent, MapOfIDAndCount,ct++,bisectionVector);
		}
	}
	
	

	
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
					
					MapOfIDAndCent.put(parent_id, new float[]{});
	
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

	Multimap<Long, float[]> multimapWeightAndCent = ArrayListMultimap.create();


	for (Long keys: sortedIDList2)
		
		{

		  
		  multimapWeightAndCent.put((Long)(MapOfIDAndCount.get(keys)), (float[]) (MapOfIDAndCent.get(keys)));
			

		}
		

		
	return multimapWeightAndCent;
	
}
	
	
	public void run() {
		
		bisectionVector = new float[so.getDimparameter()];
		counter = 0;
	
		Multimap<Long, float[]> WeightAndClusters = findDensityModes2();
		
		List<float[]>centroids2 = new ArrayList<>();
		List<Float> weights2 =new ArrayList<>();
		
		System.out.println("NumberOfMicroClusters_AfterPruning = "+ WeightAndClusters.size());		


		for (Long weights : WeightAndClusters.keys())						
		{
				
		weights2.add((float)weights);
	
		}

		
		for (Long weight : WeightAndClusters.keySet())	
			
		{

		    centroids2.addAll(WeightAndClusters.get(weight));
					
		}	

		
		Agglomerative3 aggloOffline =  new Agglomerative3(centroids2, so.getk());
		
		aggloOffline.setWeights(weights2);
		
		this.centroids = aggloOffline.getCentroids();
			
	}
	

	public static void main(String[] args) throws FileNotFoundException,
			IOException {

		int k = 10;//6;
		int d = 500;//16;
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
						
				o.setDimparameter(16);
	
				o.setCutoff(100);
				o.setRandomVector(true);				
//				System.out.println("cutoff = "+ o.getCutoff());
				System.out.println("get_random_Vector = "+ o.getRandomVector());			
								
				TWRPv4 rphit = new TWRPv4(o);
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

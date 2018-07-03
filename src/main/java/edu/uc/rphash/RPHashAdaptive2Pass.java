package edu.uc.rphash;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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


public class RPHashAdaptive2Pass implements Clusterer, Runnable {

	boolean znorm = false;
	
	
	private int counter;
	private float[] rngvec;
	private List<Centroid> centroids = null;
	private RPHashObject so;

	public RPHashAdaptive2Pass(RPHashObject so) {
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
	public List<List<float[]>> findDensityModes() {
		HashMap<Long, List<float[]>> IDAndCent = new HashMap<>();
		HashMap<Long, List<Integer>> IDAndID = new HashMap<>();
		// #create projector matrixs
		Projector projector = so.getProjectionType();
		projector.setOrigDim(so.getdim());
		projector.setProjectedDim(so.getDimparameter());
		projector.setRandomSeed(so.getRandomSeed());
		projector.init();
		
		int ct = 0;
//		if(znorm == true){
//			float[] variance = StatTests.varianceCol(so.getRawData());
//			float[] mean = StatTests.meanCols(so.getRawData());
//			// #process data by adding to the counter
//			for (float[] x : so.getRawData()) 
//			{
//				addtocounter(x, projector, IDAndCent,IDAndID,ct++,mean,variance);
//			}
//		}
//		
//		else
		{
			
			for (float[] x : so.getRawData()) 
			{
				addtocounter(x, projector, IDAndCent, IDAndID,ct++);
			}
		}
		
		
//		for (Long name: IDAndCent.keySet()){
//
//            String key =name.toString();                        
//     //       	String value = IDAndCent.get(name).toString() ;       	
//    //        	String value1 = Arrays.toString(value.toString());            	
//                System.out.println(key )  ;//+ " " + value);	           
//} 
	
		System.out.println("NumberOfMicroClustersBeforePruning = "+ IDAndCent.size());
//		for (Long name: IDAndID.keySet()){
//            String key =name.toString();
//            String value = IDAndID.get(name).toString();  
//            System.out.println(key + " " + value);  
//
//
//}
		
		// next we want to prune the tree by parent count comparison
		// follows breadthfirst search
		HashMap<Long, Long> denseSetOfIDandCount = new HashMap<Long, Long>();
		for (Long cur_id : new TreeSet<Long>(IDAndCent.keySet())) 
		{
			if (cur_id >so.getk()){
	            int cur_count = IDAndCent.get(cur_id).size();
	            long parent_id = cur_id>>>1;
	            int parent_count = IDAndCent.get(parent_id).size();
	            
	            if(cur_count!=0 && parent_count!=0)
	            {
		            if(cur_count == parent_count) {
						denseSetOfIDandCount.put(parent_id, 0L);
						IDAndCent.put(parent_id, new ArrayList<>());
						denseSetOfIDandCount.put(cur_id, (long) cur_count);
		            }
		            else
		            {
						if(2 * cur_count > parent_count) {
							denseSetOfIDandCount.remove(parent_id);
							IDAndCent.put(parent_id, new ArrayList<>());
							denseSetOfIDandCount.put(cur_id, (long) cur_count);
						}
		            }
	            }
			}
		}
		
		//remove keys with support less than 1
		Stream<Entry<Long, Long>> stream = denseSetOfIDandCount.entrySet().stream().filter(p -> p.getValue() > 1);
		//64 so 6 bits?
		//stream = stream.filter(p -> p.getKey() > 64);

		List<Long> sortedIDList= new ArrayList<>();
		// sort and limit the list
		stream.sorted(Entry.<Long, Long> comparingByValue().reversed()).limit(so.getk()*4)
				.forEachOrdered(x -> sortedIDList.add(x.getKey()));
		
		// compute centroids

		HashMap<Long, List<float[]>> estcents = new HashMap<>();
		for (int i =0; i<sortedIDList.size();i++)
		{
			estcents.put(sortedIDList.get(i), IDAndCent.get(sortedIDList.get(i)));
		}
//		System.out.println();
//		for (int i =0; i<sortedIDList.size();i++)
//		{
//			System.out.println(sortedIDList.get(i) + ":"+VectorUtil.longToString(sortedIDList.get(i))+":"+IDAndCent.get(sortedIDList.get(i)).size());
//		}
		
//		System.out.println("NumberOfMicroClusters_AfterPruning = "+ denseSetOfIDandCount.size());
		System.out.println("NumberOfMicroClusters_AfterPruning = "+ estcents.size());	
		
		
		return new ArrayList<>(estcents.values());
	}

	public void run() {
		rngvec = new float[so.getDimparameter()];
		counter = 0;
		Random r = new Random(so.getRandomSeed());
		for (int i = 0; i < so.getDimparameter(); i++)
			rngvec[i] = (float) r.nextGaussian();
		
		List<List<float[]>> clustermembers = findDensityModes();
		List<float[]>centroids = new ArrayList<>();
		
		List<Float> weights =new ArrayList<>();
		int k = clustermembers.size()>200+so.getk()?200+so.getk():clustermembers.size();
		for(int i=0;i<k;i++){
			weights.add(new Float(clustermembers.get(i).size()));
			centroids.add(medoid(clustermembers.get(i)));
		}
		Agglomerative3 aggloOffline =  new Agglomerative3(centroids, so.getk());
//		System.out.println(centroids.size());
		aggloOffline.setWeights(weights);
		this.centroids = aggloOffline.getCentroids();
	}

	public static void main(String[] args) throws FileNotFoundException,
			IOException {

		int k = 6;
		int d = 100;
		int n = 5000;
		float var = 1.5f;//0.5f;
		int count = 1;
	//	System.out.printf("ClusterVar\t");
	//	for (int i = 0; i < count; i++)
	//		System.out.printf("Trial%d\t", i);
	//	System.out.printf("RealWCSS\n");

		for (float f = var; f < 1.51; f += 1.5f) {
			float avgrealwcss = 0;
			float avgtime = 0;
	//		System.out.printf("%f\t", f);
			for (int i = 0; i < count; i++) {
				GenerateData gen = new GenerateData(k, n/k, d, f, true, .5f);
				// gen.writeCSVToFile(new
				// File("/home/lee/Desktop/reclsh/in.csv"));
				RPHashObject o = new SimpleArrayReader(gen.data, k);
				o.setDimparameter(8);
				RPHashAdaptive2Pass rphit = new RPHashAdaptive2Pass(o);
				long startTime = System.nanoTime();
				List<Centroid> centsr = rphit.getCentroids();

				avgtime += (System.nanoTime() - startTime) / 100000000;
				
				avgrealwcss += StatTests.WCSSEFloatCentroid(gen.getMedoids(),
						gen.getData());
				
				
				String Output = "/C:/Users/user/Desktop/temp/OutputTwrpCents" ;   	
				VectorUtil.writeCentroidsToFile(new File(Output),centsr, false);
				
				System.out.printf("%.0f\n\t",StatTests.WCSSECentroidsFloat(centsr, gen.data));
				System.gc();
			}
			System.out.printf("%.0f\n", avgrealwcss / count);
			

			
		}
		
		
		
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

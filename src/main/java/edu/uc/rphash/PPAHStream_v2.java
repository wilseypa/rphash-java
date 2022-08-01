package edu.uc.rphash;

/*
 This class will run the Parameter-free Projected Adaptive Hash Stream Clustering 
 */
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Map.Entry;
import java.util.TreeSet;
import java.util.stream.Stream;

import edu.uc.rphash.Readers.RPHashObject;
import edu.uc.rphash.Readers.SimpleArrayReader;
import edu.uc.rphash.projections.Projector;
import edu.uc.rphash.tests.StatTests;
import edu.uc.rphash.tests.generators.GenerateStreamData;




public class PPAHStream_v2 implements StreamClusterer {


	private float[] rngvec;
	private List<Centroid> centroids = null;
	private RPHashObject so;
	// #create projector matrixs
	Projector projector ;
	int ct=0;
	int pdim = 20;

	public PPAHStream_v2(int k, GenerateStreamData gen, int i) {
		so = new SimpleArrayReader(gen,k);
		projector = so.getProjectionType();
		projector.setOrigDim(so.getdim());
		projector.setProjectedDim(pdim);
		projector.setRandomSeed(so.getRandomSeed());
		projector.init();
		initTablesWith();
	}

	public List<Centroid> getCentroids(RPHashObject so) {
		this.so = so;
		return getCentroids();
	}


	/*
	 * X - set of vectors compute the medoid of a vector set
	 */
	/** Add vector to running Centroid
	 * @param cnt_1,cnt_2
	 * @param x_1
	 */
	public static float[] update_cent(int ct, float[] x, float[] cent){
		for(int i=0;i<x.length;i++){
			cent[i] +=(x[i]-cent[i])/(float)(ct);
		}
		return cent;
	}

	
	/*
	 * super simple hash algorithm, reminiscient of pstable lsh
	 */
	public long hashvec(float[] xt, float[] x) {
		long s = 1;// fixes leading 0's bug
		for (int i = 0; i < xt.length; i++) {
			s <<= 1;
			if (xt[i] > 0)
				s += 1;
			addcent(s,x);
		}
		return s;
	}
	

	/*
	 * ===========================MinCount Sketch=======================
	 */
	public static final long PRIME_MODULUS = (1L << 31) - 1;
	private int depth;
	private int width;
	private int[][] tableS;
	private float[][][] tableCent;
	private long[] hashA;
	
	
	private void initTablesWith() {
		this.width = (int) Math.ceil(2 / .025);
		this.depth = (int) Math.ceil(-Math.log(1 - .97) / Math.log(2));
		this.tableS = new int[depth][width];
		this.tableCent = new float[depth][width][];//we will fill these in as we need them
		this.hashA = new long[depth];//hash offsets
		Random r = new Random();
		for (int i = 0; i < depth; ++i) {
			hashA[i] = r.nextLong();
		}
	}
	
	private int hash(long item, int i) {
		long hash = hashA[i] * item;
		hash += hash >>> 32;
		hash &= PRIME_MODULUS;
		return (int) (hash % width);

	}
	
	private int count(long lshhash) {
		int min = (int) tableS[0][hash(lshhash, 0)];
		for (int i = 1; i < depth; ++i) {
			if (tableS[i][hash(lshhash, i)] < min)
				min = (int) tableS[i][hash(lshhash, i)];
		}
		return min;
	}
	
	private float[] get_cent_sketch(long lshhash) {
		int min = (int) tableS[0][hash(lshhash, 0)];
		int mini = 0;
		int minhtmp = 0;
		for (int i = 1; i < depth; ++i) {
			int htmp = hash(lshhash, i);
			if (tableS[i][hash(lshhash, i)] < min){
				mini = i;
				minhtmp = htmp;
				min = (int) tableS[i][htmp];
			}
		}

		return tableCent[mini][minhtmp];
	}
	
	private void addcent(long lshhash, float[] x){

		int htmp = hash(lshhash, 0);
		int argmini = 0;
		int argminhtmp = htmp;
		
		tableS[0][htmp] += 1;
		int min = (int) tableS[0][htmp];

		for (int i = 1; i < depth; ++i) {
			htmp = hash(lshhash, i);
			tableS[i][htmp] += 1;
			
			if (tableS[i][htmp] < min){
				min = (int) tableS[i][htmp];
				argmini = i;
				argminhtmp = htmp;
			}
		}
		
		if(tableCent[argmini][argminhtmp]==null){
			tableCent[argmini][argminhtmp] = x;
		}
		else{
			update_cent(min,  x, tableCent[argmini][argminhtmp]);
		}
	}
	/*
	 * ===========================MinCount Sketch=======================
	 */
	
	

	/*
	 * x - input vector IDAndCount - ID->count map IDAndCent - ID->centroid
	 * vector map
	 * 
	 * hash the projected vector x and update the hash to centroid and counts
	 * maps
	 */
	void addtocounter(float[] x, Projector p) {
		float[] xt = p.project(x);
		hashvec(xt, x);
	}

	@Override
	public long addVectorOnlineStep(float[] x) {
		addtocounter(x, projector);
		return 0;
	}

	@Override
	public List<Centroid> getCentroidsOfflineStep() {
		
		// next we want to prune the tree by parent count comparison
		// follows breadthfirst search
		HashMap<Long, Long> densityAndID = new HashMap<Long, Long>();
		for (Long cur_id =0l;cur_id<2<<pdim;cur_id++) {

			long cur_count = count(cur_id);
			long parent_id = cur_id >>> 1;
			long parent_count = count(parent_id);

			if (2 * cur_count > parent_count) {
				densityAndID.put(parent_id, 0l);
				densityAndID.put(cur_id,cur_count);
			}
		}
		
		//remove keys with support less than 2
		Stream<Entry<Long, Long>> stream = densityAndID.entrySet().stream().filter(p -> p.getValue() > 1);
		//64 so 6 bits?
		//stream = stream.filter(p -> p.getKey() > 64);

		List<Long> sortedIDList= new ArrayList<>();
		// sort and limit the list
		stream.sorted(Entry.<Long, Long> comparingByValue().reversed()).limit(so.getk()*1)
				.forEachOrdered(x -> sortedIDList.add(x.getKey()));
		
		// compute centroids
		List<Centroid> estcents = new ArrayList<>();
		for (int i = 0; i < sortedIDList.size(); i++) {
			System.out.println(densityAndID.get(sortedIDList.get(i)));
			if(get_cent_sketch(sortedIDList.get(i))!=null)
				estcents.add(new Centroid( get_cent_sketch(sortedIDList.get(i))));
		}

		return estcents;
	}

	@Override
	public void shutdown() {
	}

	@Override
	public int getProcessors() {
		return 0;
	}

	@Override
	public List<Centroid> getCentroids() {
		return null;
	}

	
	public static void main(String[] args) throws Exception {

		int k = 10;
		int d = 100;
		int interval = 1000;
		float var = 1f;

		Runtime rt = Runtime.getRuntime();
		GenerateStreamData gen = new GenerateStreamData(k, d, var, 1133131);

		StreamClusterer rphit = new PPAHStream_v2(k, gen, 1);
		//StreamClusterer rphit = new RPHashStreaming(k, gen, 1);

		ArrayList<float[]> vecsInThisRound = new ArrayList<float[]>();

		System.out.printf("Vecs\tMem(KB)\tTime\tWCSSE\n");
		long timestart = System.nanoTime();
		for (int i = 0; i < interval * 6; i++) {
			vecsInThisRound.add(gen.generateNext());
			if (i % interval == interval - 1) {
				timestart = System.nanoTime();
				for (float[] f : vecsInThisRound) {
					rphit.addVectorOnlineStep(f);
				}

				List<Centroid> cents = rphit.getCentroidsOfflineStep();
				long time = System.nanoTime() - timestart;
				rt.gc();
				long usedkB = (rt.totalMemory() - rt.freeMemory()) / 1024;
				double wcsse = StatTests.WCSSECentroidsFloat(cents, vecsInThisRound); 
				vecsInThisRound = new ArrayList<float[]>();
				System.out.printf("%d\t%d\t%.4f\t%.4f\n", i, usedkB,
						time / 1000000000f, wcsse);
			}
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
		// TODO Auto-generated method stub
		return false;
	}
	
}

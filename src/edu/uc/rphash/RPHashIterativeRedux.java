package edu.uc.rphash;



import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import edu.uc.rphash.Readers.RPHashObject;
import edu.uc.rphash.Readers.RPVector;
import edu.uc.rphash.Readers.SimpleArrayReader;
import edu.uc.rphash.decoders.Decoder;
import edu.uc.rphash.decoders.Leech;
import edu.uc.rphash.frequentItemSet.ItemSet;
import edu.uc.rphash.frequentItemSet.KHHCountMinSketch;
import edu.uc.rphash.frequentItemSet.SimpleFrequentItemSet;
import edu.uc.rphash.lsh.LSH;
import edu.uc.rphash.projections.DBFriendlyProjection;
import edu.uc.rphash.projections.Projector;
import edu.uc.rphash.standardhash.HashAlgorithm;
import edu.uc.rphash.standardhash.MurmurHash;
import edu.uc.rphash.standardhash.NoHash;
import edu.uc.rphash.tests.GenerateData;
import edu.uc.rphash.tests.StatTests;
import edu.uc.rphash.tests.TestUtil;

/**This is the Iterative Redux Version Of RPHash
 * Instead of a constant number of passes over the data as in RPHash
 * the Redux variant does k-passes over the data, finding the max
 * density mode at each pass, up to k density modes. 
 * @author lee
 */
public class RPHashIterativeRedux  implements Clusterer
{
	float variance;
	Random r  = new Random();
	public RPHashObject map() 
	{
		Iterator<float[]> vecs = so.getVectorIterator();
		if (!vecs.hasNext())
			return so;

		long hash;
		int probes = so.getNumProjections();
		int k = (int) (so.getk() * probes);
		
		//initialize our counter
		ItemSet<Long> is = new KHHCountMinSketch<Long>(k);
		// create our LSH Device
		//create same LSH Device as before
		Random r = new Random(so.getRandomSeed());
		LSH[] lshfuncs = new LSH[probes];
		
		Decoder dec = new Leech(variance);
		//Decoder dec = new MultiDecoder(1, innerdec);
		
		
		HashAlgorithm hal = new MurmurHash(so.getHashmod());
		
		//create same projection matrices as before
		for (int i = 0; i < probes; i++) {
			Projector p = new DBFriendlyProjection(so.getdim(),
					dec.getDimensionality(), r.nextLong());
			lshfuncs[i] = new LSH(dec, p, hal);
		}
		// add to frequent itemset the hashed Decoded randomly projected vector
		while (vecs.hasNext()) {
			float[] vec = vecs.next();
			for (int i = 0; i < probes; i++) {
				hash = lshfuncs[i].lshHash(vec);
				is.add(hash);
			}
		}
		so.setPreviousTopID(is.getTop().subList(k-1, k));//just the last one
		return so;

	}
	
	class Tuple implements Comparable<Tuple>{
		public float dist;
		public float[] vec;
		public Tuple(float[] vec2, float distance) {
			this.dist=distance;
			this.vec = vec2;
		}
		@Override
		public int compareTo(Tuple o) {
			if(dist<o.dist)return -1;
			else if(dist>o.dist)return 1;
			return 0;

		}

	}
	
	
	public RPHashObject reduce() 
	{
		Long lastID = so.getPreviousTopID().get(0);
		Centroid centroid = new Centroid(so.getdim(),lastID);
		Iterator<float[]> vecs = so.getVectorIterator();
		float[] vec;
		

		
		//sort and remove top n/k items		
		vecs =  so.getVectorIterator();
		ArrayList<Tuple> pq = new ArrayList<Tuple>();
		while(vecs.hasNext())
		{
			vec = vecs.next();
			pq.add(new Tuple(vec,TestUtil.distance(vec,centroid.centroid())));
		}
		
		Collections.sort(pq);
		float cutoff = pq.get(pq.size()/2).dist;//(so.getk())).dist;
		vecs =  so.getVectorIterator();
		while(vecs.hasNext())
		{
				vec = vecs.next();
				if(TestUtil.distance(vec,centroid.centroid()) < cutoff){
					centroid.updateVec(vec);
					vecs.remove();
				}
		}
		so.addCentroid(centroid.centroid());
		return so;
	}
	
	
	
	
	private List<float[]> centroids=null;
	private RPHashObject so;
	public RPHashIterativeRedux (List<float[]> data,int k){
		variance = StatTests.varianceAll(data);
		so = new SimpleArrayReader(data,k,1,250000);
	}
	public RPHashIterativeRedux (RPHashObject so){
		this.so= so;
	}
	public RPHashIterativeRedux (List<float[]> data,int k,int rseed){
		variance = StatTests.varianceAll(data);
		so = new SimpleArrayReader(data,k,rseed,250000);
	}

	
	public List<float[]> getCentroids(RPHashObject so){
		if(centroids == null)run();
		return centroids;
	}
	
	@Override
	public List<float[]> getCentroids(){
		
		if(centroids == null)run();
		return centroids;
	}
	
	private  void run()
	{
		for(int i = 0;i<so.getk();i++)
		{
			so = map();
			so = reduce();
		}
		centroids = so.getCentroids();
	}

	
	public static void main(String[] args) {

		int k = 10;
		int d = 1000;
		int n = 20000;

		float var = .3f;
		for (float f = var; f < 4.1; f += .2f) {
			for (int i = 0; i < 1; i++) {
				GenerateData gen = new GenerateData(k, n / k, d, f, true, 1f);
				RPHashIterativeRedux rphit = new RPHashIterativeRedux(gen.data(), k);

				long startTime = System.nanoTime();
				rphit.getCentroids();
				long duration = (System.nanoTime() - startTime);
				List<float[]> aligned = TestUtil.alignCentroids(
						rphit.getCentroids(), gen.medoids());
				System.out.println(f + ":" + StatTests.PR(aligned, gen) + ":"
						+ StatTests.SSE(aligned, gen) + ":" + duration
						/ 1000000000f);
				System.gc();
			}
		}

	}

	@Override
	public RPHashObject getParam() {
		return so;
	}

}

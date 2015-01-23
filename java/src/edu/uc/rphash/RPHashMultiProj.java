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
import edu.uc.rphash.decoders.LeechDecoder;
import edu.uc.rphash.frequentItemSet.ItemSet;
import edu.uc.rphash.frequentItemSet.StickyWrapper;
import edu.uc.rphash.lsh.LSH;
import edu.uc.rphash.projections.DBFriendlyProjection;
import edu.uc.rphash.projections.Projector;
import edu.uc.rphash.standardhash.HashAlgorithm;
import edu.uc.rphash.standardhash.NoHash;
import edu.uc.rphash.tests.Agglomerative;
import edu.uc.rphash.tests.GenerateData;
import edu.uc.rphash.tests.Kmeans;
import edu.uc.rphash.tests.StatTests;
import edu.uc.rphash.tests.TestUtil;

/**This is the correlated multi projections approach. In this RPHash variation we try to 
 * incorporate the advantage of multiple random projections in order to combat increasing
 * cluster error rates as the deviation between projected and full data increases. The
 * main idea is similar to the referential RPHash, however the set union is projection id
 * dependent. This will be done in a simplified bitmask addition to the hash code in lieu of
 * an array of sets data structures.
 * @author lee
 *
 */
public class RPHashMultiProj  implements Clusterer{
	float variance;
	public RPHashObject mapP1(RPHashObject so) {
		// create our LSH Machine
		Random r = new Random();
		
		HashAlgorithm hal = new NoHash();// MurmurHash(so.getHashmod());
		Iterator<RPVector> vecs = so.getVectorIterator();
		if(!vecs.hasNext())return so;
		
		int probes = 3;
		LSH[] lshfuncs = new LSH[probes];

		
		for (int i = 0; i < probes ; i++) {
			Projector p = new DBFriendlyProjection(so.getdim(),
					LeechDecoder.Dim,  r.nextInt());
			Decoder dec = new LeechDecoder(variance/.75f);
			lshfuncs[i] = new LSH(dec, p, hal);
		}
		int k =probes*so.getk();
		ItemSet<Long> is = new StickyWrapper<Long>(k, so.getn());
		long hash;
		// add to frequent itemset the hashed Decoded randomly projected vector
		while (vecs.hasNext()) {
			RPVector vec = vecs.next();
			hash = lshfuncs[0].lshHash(vec.data);
		    vec.id.add(hash);
		    is.add(hash);
			for (int j=1; j < probes;j++) {
				hash = lshfuncs[j].lshHash(vec.data);
			    vec.id.add(hash);
			    is.add(hash);
			}
		}
		
		so.setPreviousTopID(is.getTop());

		return so;
	}

	/*
	 * This is the second phase after the top ids have been in the reduce phase aggregated
	 */
	public RPHashObject mapP2(RPHashObject so) {

		Iterator<RPVector> vecs = so.getVectorIterator();
		if(!vecs.hasNext())return so;
		RPVector vec = vecs.next();

		// make a set of k default centroid objects
		ArrayList<Centroid> centroids = new ArrayList<Centroid>();
		for (long id : so.getPreviousTopID())
			centroids.add( new Centroid(so.getdim(),id));
		
		while (vecs.hasNext()) {
			for(Centroid cent: centroids)
			{
				if(!Collections.disjoint(cent.ids,vec.id)){
					cent.updateVec(vec);
				}
			}
			vec = vecs.next();
		}
		
		for (Centroid cent : centroids) {
			so.addCentroid(cent.centroid());
		}
		return so;
	}

	public RPHashObject reduceP2(RPHashObject so) {
		return so;
	}
	private List<float[]> centroids=null;
	private RPHashObject so;
	public RPHashMultiProj(List<float[]> data,int k){
		so = new SimpleArrayReader(data,k,1,250000);
		variance = StatTests.varianceAll(data);
	}
	public RPHashMultiProj(List<float[]> data,int k,int rseed){
		so = new SimpleArrayReader(data,k,rseed,250000);
		variance = StatTests.varianceAll(data);
	}
	
	
	public RPHashMultiProj(RPHashObject so){
		this.so= so;
	}

	
	public List<float[]> getCentroids(RPHashObject so){
		if(centroids == null)run(so);
		return centroids;
	}
	
	@Override
	public List<float[]> getCentroids(){
		
		if(centroids == null)run(so);
		return centroids;
	}
	
	private  void run(RPHashObject so)
	{
		so = mapP1(so);
		so = mapP2(so);
		centroids = new Kmeans(so.getk(),so.getCentroids()).getCentroids(); //( new Agglomerative(so.getk(),so.getCentroids())).getCentroids();
		centroids = so.getCentroids();
	}
	
	
	public static void main(String[] args) {

		int k = 20;
		int d = 5000;
		int n = 10000;
		GenerateData gen = new GenerateData(k, n / k, d,1.f,true);
		RPHashMultiProj rphit = new RPHashMultiProj(gen.data(), k);

		System.out.println(StatTests.averageAll(gen.data()));
		System.out.println(StatTests.varianceAll(gen.data()));
		
		
		long startTime = System.nanoTime();
		rphit.getCentroids();
		long duration = (System.nanoTime() - startTime);
		List<float[]> aligned = TestUtil.alignCentroids(rphit.getCentroids(),
				gen.medoids());
		System.out.println(StatTests.PR(aligned, gen) + ":" + duration
				/ 1000000000f);
		System.out.print("\n");
		System.gc();

	}

}

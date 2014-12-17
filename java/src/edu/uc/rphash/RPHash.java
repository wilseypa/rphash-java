package edu.uc.rphash;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import edu.uc.rphash.Readers.RPHashObject;
import edu.uc.rphash.Readers.RPVector;
import edu.uc.rphash.Readers.SimpleArrayReader;
import edu.uc.rphash.decoders.Decoder;
import edu.uc.rphash.decoders.LeechDecoder;
import edu.uc.rphash.frequentItemSet.ItemSet;
import edu.uc.rphash.frequentItemSet.SimpleFrequentItemSet;
import edu.uc.rphash.frequentItemSet.StickyWrapper;
import edu.uc.rphash.lsh.LSH;
import edu.uc.rphash.projections.DBFriendlyProjection;
import edu.uc.rphash.projections.Projector;
import edu.uc.rphash.standardhash.FNVHash;
import edu.uc.rphash.standardhash.HashAlgorithm;
import edu.uc.rphash.standardhash.MurmurHash;
import edu.uc.rphash.standardhash.NoHash;
import edu.uc.rphash.tests.GenerateData;
import edu.uc.rphash.tests.StatTests;
import edu.uc.rphash.tests.TestUtil;

public class RPHash {

	public RPHashObject mapP1(RPHashObject so) {
		// create our LSH Machine
		Random r = new Random();
		HashAlgorithm hal = new NoHash();// MurmurHash(so.getHashmod());
		Iterator<RPVector> vecs = so.getVectorIterator();
		if(!vecs.hasNext())return so;
		RPVector vec = vecs.next();

		
		
		
		int probes = 1;//(int) (Math.log(so.getdim()) + .5) ;
		LSH[] lshfuncs = new LSH[probes ];
		
		for (int i = 0; i < probes ; i++) {
			Projector p = new DBFriendlyProjection(so.getdim(),
					LeechDecoder.Dim,  r.nextInt());
			//float radius =1.5f;//TestUtil.max(vec.data);
			Decoder dec = new LeechDecoder();
			lshfuncs[i] = new LSH(dec, p, hal);
		}
		
		ItemSet<Long> is = /*new SimpleFrequentItemSet<Long>(so.getk());*/
		new StickyWrapper<Long>(so.getk(), so.getn());

		long hash;
		// add to frequent itemset the hashed Decoded randomly projected vector
		while (vecs.hasNext()) {
			for (int j=0; j < probes;j++) {
				hash = lshfuncs[j].lshHash(vec.data);
				is.add(hash);
			    vec.id.add(hash);
			    
//				hash = lshfuncs[j].lshHashRadius(vec.data,.25f);
//				is.add(hash);
//			    vec.id.add(hash);
			    
			    

			    
			    
			}
			vec = vecs.next();
		}
		
		//for(Long id :is.getCounts())System.out.println(id);
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
				if(!Collections.disjoint(cent.ids,vec.id))cent.updateVec(vec);
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
	public RPHash(List<float[]> data,int k){
		so = new SimpleArrayReader(data,k,1,250000,1);
	}
	public RPHash(List<float[]> data,int k,int rseed){
		so = new SimpleArrayReader(data,k,rseed,250000,1);
	}
	
	
	public RPHash(RPHashObject so){
		this.so= so;
	}

	
	public List<float[]> getCentroids(RPHashObject so){
		if(centroids == null)run(so);
		return centroids;
	}
	public List<float[]> getCentroids(){
		
		if(centroids == null)run(so);
		return centroids;
	}
	
	private  void run(RPHashObject so)
	{
		so = mapP1(so);
		so = mapP2(so);
		centroids = so.getCentroids();
	}
	
	
	public static void main(String[] args) {

		int k = 30;
		int d = 2000;
		int n = 20000;
		GenerateData gen = new GenerateData(k, n / k, d, 1.0f, true, 1.f);
		RPHash rphit = new RPHash(gen.data(), k);

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

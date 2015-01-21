package edu.uc.rphash;

import java.io.File;
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

public class RPHashSimple {
	float variance;
	public RPHashObject map(RPHashObject so) {
		// create our LSH Machine
		Random r = new Random();
		HashAlgorithm hal = new MurmurHash(so.getHashmod());
		Iterator<RPVector> vecs = so.getVectorIterator();
		if(!vecs.hasNext())return so;

		//int probes = 1;//doesnt seem to add any benefit
		
		
		Projector p = new DBFriendlyProjection(so.getdim(),
				LeechDecoder.Dim,  r.nextInt());
		Decoder dec = new LeechDecoder(variance/.75f);
		LSH lshfunc = new LSH(dec, p, hal);
		
		ItemSet<Long> is =new StickyWrapper<Long>(so.getk(), so.getn());// new SimpleFrequentItemSet<Long>(so.getk());//
		long hash;

		// add to frequent itemset the hashed Decoded randomly projected vector
		while (vecs.hasNext()) {
			RPVector vec = vecs.next();
			hash = lshfunc.lshHash(vec.data);
			is.add(hash);
		    vec.id.add(hash);

//			for (int j=1; j < probes;j++) {
//				hash = lshfunc.lshHashRadius(vec.data,variance/10f);
//				is.add(hash);
//			    vec.id.add(hash);
//			}
		}
		
		so.setPreviousTopID(is.getTop());
		//for(Long l : is.getCounts())System.out.printf("%d,",l);System.out.printf("\n,");
		//System.out.println( is.getCounts().toString());
		return so;
	}

	/*
	 * This is the second phase after the top ids have been in the reduce phase aggregated
	 */
	public RPHashObject reduce(RPHashObject so) {

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

	
	private List<float[]> centroids=null;
	private RPHashObject so;
	public RPHashSimple(List<float[]> data,int k){
		variance = StatTests.varianceAll(data);
		so = new SimpleArrayReader(data,k,1,250000);
	}
	public RPHashSimple(List<float[]> data,int k, int times,int rseed){
		variance = StatTests.varianceAll(data);
		so = new SimpleArrayReader(data,k,rseed,250000);
	}
	
	
	public RPHashSimple(RPHashObject so){
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
		so = map(so);
		so = reduce(so);
		centroids = so.getCentroids();
	}
	
	
	public static void main(String[] args) {

		int k = 20;
		int d = 5000;
		int n = 10000;
		GenerateData gen = new GenerateData(k, n / k, d, 1.0f, true, 1f);

		RPHashSimple rphit = new RPHashSimple(gen.data(), k);
		
		long startTime = System.nanoTime();
		rphit.getCentroids();
		long duration = (System.nanoTime() - startTime);
		List<float[]> aligned = TestUtil.alignCentroids(rphit.getCentroids(),
				gen.medoids());
		System.out.println(StatTests.PR(aligned, gen) + ":" + duration
				/ 1000000000f);
		System.gc();
		
		
		

	}
}

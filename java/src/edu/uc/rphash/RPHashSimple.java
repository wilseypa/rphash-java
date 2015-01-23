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
import edu.uc.rphash.tests.Agglomerative;
import edu.uc.rphash.tests.GenerateData;
import edu.uc.rphash.tests.Kmeans;
import edu.uc.rphash.tests.StatTests;
import edu.uc.rphash.tests.TestUtil;

public class RPHashSimple  implements Clusterer{
	float variance;
	public RPHashObject map(RPHashObject so) {
		// create our LSH Machine
		Random r = new Random();
		HashAlgorithm hal = new NoHash();
		Iterator<RPVector> vecs = so.getVectorIterator();
		if(!vecs.hasNext())return so;
		
		Projector p = new DBFriendlyProjection(so.getdim(),
				LeechDecoder.Dim,  r.nextInt());
		Decoder dec = new LeechDecoder(variance/.75f);
		LSH lshfunc = new LSH(dec, p, hal);
		//(int)(Math.log(so.getk())*so.getk()+.5);
		//
		long[] hash;
		int probes = 3;
		int k =so.getk()*probes;
		ItemSet<Long> is = new SimpleFrequentItemSet<Long>(k);
		// add to frequent itemset the hashed Decoded randomly projected vector
		while (vecs.hasNext()) {
			RPVector vec = vecs.next();
		    hash = lshfunc.lshHashRadius(vec.data,probes);
			for (int j=0; j < probes;j++) {
				is.add(hash[j]);
			    vec.id.add(hash[j]);
			}
		}
		so.setPreviousTopID(is.getTop());
		//for(Long l : is.getCounts())System.out.printf("%d,",l);System.out.printf("\n,");
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
	
	@Override
	public List<float[]> getCentroids(){
		
		if(centroids == null)run(so);
		return centroids;
	}
	
	private  void run(RPHashObject so)
	{
		so = map(so);
		so = reduce(so);
		centroids =  ( new Kmeans(so.getk(),so.getCentroids())).getCentroids();
		//centroids = so.getCentroids();
	}
	
	
	public static void main(String[] args) {

		int k = 20;
		int d = 5000;
		int n = 10000;
		for(int i =0;i<5;i++){
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
}

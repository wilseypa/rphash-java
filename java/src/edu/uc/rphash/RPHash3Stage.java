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
import edu.uc.rphash.frequentItemSet.StickyWrapper;
import edu.uc.rphash.lsh.LSH;
import edu.uc.rphash.projections.DBFriendlyProjection;
import edu.uc.rphash.projections.Projector;
import edu.uc.rphash.standardhash.HashAlgorithm;
import edu.uc.rphash.standardhash.MurmurHash;
import edu.uc.rphash.tests.GenerateData;
import edu.uc.rphash.tests.StatTests;
import edu.uc.rphash.tests.TestUtil;

public class RPHash3Stage {


	float variance;
	public RPHashObject mapP1(RPHashObject so) {
		
		// create our LSH Machine
		HashAlgorithm hal = new MurmurHash(so.getHashmod());
		Iterator<RPVector> vecs = so.getVectorIterator();
		if(!vecs.hasNext())return so;
		RPVector vec = vecs.next();

		Decoder dec = new LeechDecoder(variance/.75f);
		Projector[] p = new Projector[1];

		p[0] = new DBFriendlyProjection(so.getdim(),
				dec.getDimensionality(),  so.getRandomSeed());
		
		LSH lsh = new LSH(dec, p, hal, so.getTimes());
		ItemSet<Long> is = new StickyWrapper<Long>(so.getk(), so.getn());
//		int probes = 1; /* (int) (Math.log(so.getn()) + .5); (int) (Math.pow(
//				so.getn(), 0.2671) + .5);*/
		
		// add to frequent itemset the hashed Decoded randomly projected vector
		while (vecs.hasNext()) {
			is.add(lsh.lshHash(vec.data));
//			int j = 0;
//			while (j < probes) {
//				is.add(lsh.lshHashRadius(vec.data));
//				j++;
//			}
			vec=vecs.next();
		}
		so.setPreviousTopID(is.getTop());
		return so;
	}

	/*
	 * This step is temporary for testing, it should be performed during the
	 * reduce phase 1.
	 */
	public RPHashObject mapP2(RPHashObject so) {

		// create our LSH Machine
		HashAlgorithm hal = new MurmurHash(so.getHashmod());
		Iterator<RPVector> vecs = so.getVectorIterator();
		if(!vecs.hasNext())return so;
		RPVector vec = vecs.next();
		// trying to combat variance drifting issues by adjusting the
		// scaling the lattice region radius
		Decoder dec = new LeechDecoder(variance/.75f);
		//Projector p = new Projector[so.getTimes()];
		//for (int i = 0; i < so.getTimes(); i++) {
		Projector p = new DBFriendlyProjection(so.getdim(),
					100/*so.getdim()/so.getk()*/,  so.getRandomSeed());
		//}
		LSH lsh = new LSH(dec, p, hal);
		// make a set of k default centroid objects
		HashMap<Long, Centroid> centroids = new HashMap<Long, Centroid>();
		for (Long id : so.getPreviousTopID())
			centroids.put(id, new Centroid(100,id));
		// start the calculation
		long d;
		//int probes = 1;
		// add to frequent itemset the hashed Decoded randomly projected vector
		while (vecs.hasNext()) 
		{
			//int j = 0;
			Centroid cent = null;
			d = lsh.lshHash(vec.data);
			cent = centroids.get(d);
			
			if (cent != null)
				cent.updateVec(p.project(vec.data));
			
//			while (cent == null && j < probes) {
//				d = lsh.lshHashRadius(vec.data);
//				cent = centroids.get(d);
//				if (cent != null)
//					cent.updateVec(p.project(vec.data));
//				j++;
//			}
			vec =vecs.next();
		}
		for (Long id : centroids.keySet()) {
			so.addCentroid(centroids.get(id).centroid());

		}
		
		
		return so;
	}

	public RPHashObject mapP3(RPHashObject so) {
		ArrayList<Centroid> centroids = new ArrayList<Centroid>();
		Projector[] p = new Projector[1];
		p[0] = new DBFriendlyProjection(so.getdim(), 100,  so.getRandomSeed());
		
		Iterator<RPVector> vecs = so.getVectorIterator();
		for (int i = 0; i < so.getk(); i++){
			Centroid cent = new Centroid(so.getdim(),i);
			centroids.add(cent);
		}
		RPVector vec = vecs.next();
		while (vecs.hasNext()) {

			int nn = TestUtil.findNearestDistance(p[0].project(vec.data),
					so.getCentroids());
				centroids.get(nn).updateVec(vec);
			vec = vecs.next();
		}
		so.setCentroids(new ArrayList<float[]>());
		for (Centroid cent : centroids){
			so.addCentroid(cent.centroid());
		}
		return so;
	}

	private List<float[]> centroids = null;
	private RPHashObject so;

	public RPHash3Stage(List<float[]> data, int k) {
		variance = StatTests.varianceAll(data);
		so = new SimpleArrayReader(data, k, 1, 2500000);
	}

	public RPHash3Stage(RPHashObject so) {
		this.so = so;
	}

	public RPHash3Stage(List<float[]> data, int k, int rseed) {
		variance = StatTests.varianceAll(data);
		so = new SimpleArrayReader(data, k, rseed, 2500000);
	}

	public List<float[]> getCentroids(RPHashObject so) {
		if (centroids == null)
			run(so);
		return centroids;
	}

	public List<float[]> getCentroids() {

		if (centroids == null)
			run(so);
		return centroids;
	}

	private void run(RPHashObject so) {
		so = mapP1(so);
		so = mapP2(so);
		so = mapP3(so);

		centroids = so.getCentroids();
	}

	public static void main(String[] args) {

		int k = 20;
		int d = 5000;
		int n = 10000;
		GenerateData gen = new GenerateData(k, n / k, d, 1.0f, true, 1.f);
		RPHash3Stage rphit = new RPHash3Stage(gen.data(), k);

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

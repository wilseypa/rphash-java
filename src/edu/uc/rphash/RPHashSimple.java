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
import edu.uc.rphash.decoders.E8;
import edu.uc.rphash.decoders.Leech;
import edu.uc.rphash.decoders.MultiDecoder;
import edu.uc.rphash.frequentItemSet.ItemSet;
import edu.uc.rphash.frequentItemSet.KHHCountMinSketch;
import edu.uc.rphash.frequentItemSet.SimpleFrequentItemSet;
import edu.uc.rphash.lsh.LSH;
import edu.uc.rphash.projections.DBFriendlyProjection;
import edu.uc.rphash.projections.Projector;
import edu.uc.rphash.standardhash.*;
import edu.uc.rphash.tests.Agglomerative;
import edu.uc.rphash.tests.GenerateData;
import edu.uc.rphash.tests.Kmeans;
import edu.uc.rphash.tests.StatTests;
import edu.uc.rphash.tests.TestUtil;

public class RPHashSimple implements Clusterer {
	float variance;

	public RPHashObject map(RPHashObject so) {

		// create our LSH Machine
		HashAlgorithm hal = new MurmurHash(Integer.MAX_VALUE);
		Iterator<RPVector> vecs = so.getVectorIterator();
		if (!vecs.hasNext())
			return so;
		

		Decoder dec = so.getDecoderType();
		if(dec==null){
			Decoder inner = new Leech(variance);
			dec = new MultiDecoder( so.getInnerDecoderMultiplier()*inner.getDimensionality(), inner);
		}
		
		Projector p = new DBFriendlyProjection(so.getdim(),
				dec.getDimensionality(), so.getRandomSeed());
		LSH lshfunc = new LSH(dec, p, hal);
		long hash;
		int k = (int) (so.getk());

		ItemSet<Long> is = new KHHCountMinSketch<Long>(k);
		// add to frequent itemset the hashed Decoded randomly projected vector

		while (vecs.hasNext()) {
			RPVector vec = vecs.next();
			hash = lshfunc.lshHash(vec.data);
			is.add(hash);
			vec.id.add(hash);
		}
		so.setPreviousTopID(is.getTop());
		
		
//		for (Long l : is.getCounts())
//			System.out.printf(" %d,", l);
//		System.out.printf("\n,");
		return so;
	}

	/*
	 * This is the second phase after the top ids have been in the reduce phase
	 * aggregated
	 */
	public RPHashObject reduce(RPHashObject so) {

		Iterator<RPVector> vecs = so.getVectorIterator();
		if (!vecs.hasNext())
			return so;
		RPVector vec = vecs.next();
		int blurValue = so.getNumBlur();
		
		HashAlgorithm hal = new MurmurHash(Integer.MAX_VALUE);
		Decoder inner = new Leech(variance);
		Decoder dec = new MultiDecoder( so.getInnerDecoderMultiplier()*inner.getDimensionality(), inner);
		Projector p = new DBFriendlyProjection(so.getdim(),
				dec.getDimensionality(), so.getRandomSeed());
		LSH lshfunc = new LSH(dec, p, hal);
		long hash[];
		
		if(blurValue==0)blurValue = (int) Math.log(so.getdim()/dec.getDimensionality());
		// make a set of k default centroid objects
		ArrayList<Centroid> centroids = new ArrayList<Centroid>();
		for (long id : so.getPreviousTopID())
			centroids.add(new Centroid(so.getdim(), id));

//		ItemSet<Long> is = new KHHCountMinSketch<Long>(so.getk());
		
		while (vecs.hasNext()) {
			hash = lshfunc.lshHashRadius(vec.data,blurValue);
			for (Centroid cent : centroids){
				for(long h:hash){
					if(cent.ids.contains(h)){
//						is.add(h);
						cent.updateVec(vec);
						break;
					}
				}
			}
			vec = vecs.next();
		}

//		for (Long l : is.getCounts())
//			System.out.printf(" %d,", l);
//		System.out.printf("\n,");
		
		for (Centroid cent : centroids) so.addCentroid(cent.centroid());
		
		return so;
	}

	private List<float[]> centroids = null;
	private RPHashObject so;

	public RPHashSimple(List<float[]> data, int k) {
		variance = StatTests.varianceSample(data, .01f);
		so = new SimpleArrayReader(data, k);
	}

	public RPHashSimple(List<float[]> data, int k, int times, int rseed) {
		variance = StatTests.varianceSample(data, .01f);
		so = new SimpleArrayReader(data, k);
	}

	public RPHashSimple(RPHashObject so) {
		this.so = so;
	}

	public List<float[]> getCentroids(RPHashObject so) {
		if (centroids == null)
			run(so);
		return centroids;
	}

	@Override
	public List<float[]> getCentroids() {

		if (centroids == null)
			run(so);
		return centroids;
	}

	private void run(RPHashObject so) {
		
		so = map(so);
		so = reduce(so);
		centroids = so.getCentroids();//new Kmeans(so.getk(),so.getCentroids()).getCentroids();
	}

	public static void main(String[] args) {

		int k = 20;
		int d = 5000;
		int n = 10000;
		float var = 0.001f;
		for(float f = var;f<3.0;f+=.01f){
			for (int i = 0; i < 5; i++) {
				GenerateData gen = new GenerateData(k, n / k, d, f, true, 1f);
	
				RPHashSimple rphit = new RPHashSimple(gen.data(), k);
	
				long startTime = System.nanoTime();
				rphit.getCentroids();
				long duration = (System.nanoTime() - startTime);
				List<float[]> aligned = TestUtil.alignCentroids(
						rphit.getCentroids(), gen.medoids());
				System.out.println(f+":"+StatTests.PR(aligned, gen) + ":" + duration
						/ 1000000000f);
				System.gc();
			}
		}

	}
}

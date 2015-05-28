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

	public RPHashObject map() {

		// create our LSH Machine
		HashAlgorithm hal = new MurmurHash(so.getHashmod());
		Iterator<float[]> vecs = so.getVectorIterator();
		if (!vecs.hasNext())
			return so;
		
		Decoder dec = so.getDecoderType();
		Projector p = new DBFriendlyProjection(so.getdim(),
				dec.getDimensionality(), so.getRandomSeed());
		LSH lshfunc = new LSH(dec, p, hal);
		long hash;
		int k = (int) (so.getk()*Math.log(so.getk()));

		ItemSet<Long> is = new KHHCountMinSketch<Long>(k);
		// add to frequent itemset the hashed Decoded randomly projected vector

		while (vecs.hasNext()) {
			float[] vec = vecs.next();
			hash = lshfunc.lshHash(vec);
			is.add(hash);
			//vec.id.add(hash);
		}
		so.setPreviousTopID(is.getTop());		
//		for(long l: is.getCounts())System.out.print(l+", ");
		return so;
	}

	/*
	 * This is the second phase after the top ids have been in the reduce phase
	 * aggregated
	 */
	public RPHashObject reduce() {

		Iterator<float[]> vecs = so.getVectorIterator();
		if (!vecs.hasNext())
			return so;
		float[] vec = vecs.next();
		int blurValue = so.getNumBlur();
		
		HashAlgorithm hal = new MurmurHash(so.getHashmod());
		Decoder dec = so.getDecoderType();
		
		Projector p = new DBFriendlyProjection(so.getdim(),
				dec.getDimensionality(), so.getRandomSeed());
		LSH lshfunc = new LSH(dec, p, hal);
		long hash[];
		
		ArrayList<Centroid> centroids = new ArrayList<Centroid>();
		for (long id : so.getPreviousTopID())
			centroids.add(new Centroid(so.getdim(), id));

		while (vecs.hasNext()) {
			hash = lshfunc.lshHashRadius(vec,blurValue);
			for (Centroid cent : centroids){
				for(long h:hash){
					if(cent.ids.contains(h)){
						cent.updateVec(vec);
						break;
					}
				}
			}
			vec = vecs.next();
		}

		
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
		variance = StatTests.varianceSample(data, .001f);
		so = new SimpleArrayReader(data, k);

	}

	public RPHashSimple(RPHashObject so) {
		this.so = so;
	}

	public List<float[]> getCentroids(RPHashObject so) {
		this.so=so;
		if (centroids == null)
			run();
		return new Kmeans(so.getk(),centroids).getCentroids();
	}

	@Override
	public List<float[]> getCentroids() {
		if (centroids == null)
			run();
		return new Kmeans(so.getk(),centroids).getCentroids();
	}

	private void run() {
		
		map();
		reduce();
		centroids = so.getCentroids();//new Kmeans(so.getk(),so.getCentroids()).getCentroids();
	}

	public static void main(String[] args) {

		int k = 10;
		int d = 1000;
		int n = 10000;
		float var = .3f;
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

	@Override
	public RPHashObject getParam() {
		return so;
	}
}

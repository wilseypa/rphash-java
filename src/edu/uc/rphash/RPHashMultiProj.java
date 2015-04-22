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
import edu.uc.rphash.frequentItemSet.StickyWrapper;
import edu.uc.rphash.lsh.LSH;
import edu.uc.rphash.projections.DBFriendlyProjection;
import edu.uc.rphash.projections.Projector;
import edu.uc.rphash.standardhash.HashAlgorithm;
import edu.uc.rphash.standardhash.MurmurHash;
import edu.uc.rphash.standardhash.NoHash;
import edu.uc.rphash.tests.Agglomerative;
import edu.uc.rphash.tests.GenerateData;
import edu.uc.rphash.tests.Kmeans;
import edu.uc.rphash.tests.StatTests;
import edu.uc.rphash.tests.TestUtil;

/**
 * This is the correlated multi projections approach. In this RPHash variation
 * we try to incorporate the advantage of multiple random projections in order
 * to combat increasing cluster error rates as the deviation between projected
 * and full data increases. The main idea is similar to the referential RPHash,
 * however the set union is projection id dependent. This will be done in a
 * simplified bitmask addition to the hash code in lieu of an array of sets data
 * structures.
 * 
 * @author lee
 *
 */
public class RPHashMultiProj implements Clusterer {
	float variance;

	public RPHashObject map(RPHashObject so) {
		// create our LSH Machine
		Random r = new Random(so.getRandomSeed());

		HashAlgorithm hal = new MurmurHash(Integer.MAX_VALUE);
		Iterator<RPVector> vecs = so.getVectorIterator();
		if (!vecs.hasNext())
			return so;

		int probes = so.getNumProjections();
		LSH[] lshfuncs = new LSH[probes];
		for (int i = 0; i < probes; i++) {
			Decoder dec = so.getDecoderType();
			if(dec==null){
				Decoder inner = new Leech(variance);
				dec = new MultiDecoder( so.getInnerDecoderMultiplier()*inner.getDimensionality(), inner);
			}
			Projector p = new DBFriendlyProjection(so.getdim(),
					dec.getDimensionality(), r.nextLong());
			lshfuncs[i] = new LSH(dec, p, hal);
		}

		long hash;
		int k = (int) (so.getk()*probes);

		ItemSet<Long> is = new KHHCountMinSketch<Long>(k);
		// add to frequent itemset the hashed Decoded randomly projected vector

		while (vecs.hasNext()) {
			RPVector vec = vecs.next();

			for (int i = 0; i < probes; i++) {
				hash = lshfuncs[i].lshHash(vec.data);
				vec.id.add(hash);
				is.add(hash);
			}

			// hash = lshfunc.lshHash(vec.data);
			// is.add(hash);
			// vec.id.add(hash);

		}
		so.setPreviousTopID(is.getTop());
		 for (Long l : is.getCounts())
		 System.out.printf(" %d,", l);
		 System.out.printf("\n,");
		return so;
	}

	/*
	 * This is the second phase after the top ids have been in the reduce phase
	 * aggregated
	 */
	public RPHashObject reduce(RPHashObject so) {
		Random r = new Random(so.getRandomSeed());

		Iterator<RPVector> vecs = so.getVectorIterator();
		if (!vecs.hasNext())
			return so;
		RPVector vec = vecs.next();
		int blurValue = so.getNumBlur();
		
		HashAlgorithm hal = new MurmurHash(Integer.MAX_VALUE);

		int probes = so.getNumProjections();
		LSH[] lshfuncs = new LSH[probes];
		for (int i = 0; i < probes; i++) {
			Decoder decinner = new Leech(variance);
			Decoder dec = new MultiDecoder(so.getInnerDecoderMultiplier()
					* decinner.getDimensionality(), decinner);
			Projector p = new DBFriendlyProjection(so.getdim(),
					dec.getDimensionality(), r.nextLong());
			lshfuncs[i] = new LSH(dec, p, hal);
			if(blurValue==0)blurValue = (int) Math.log(so.getdim()/dec.getDimensionality());
		}

		long hash[];

		// make a set of k default centroid objects
		ArrayList<Centroid> centroids = new ArrayList<Centroid>();
		for (long id : so.getPreviousTopID())
			centroids.add(new Centroid(so.getdim(), id));

		while (vecs.hasNext()) {
			// iterate over the multiple projections
			for (LSH lshfunc : lshfuncs) {
				hash = lshfunc.lshHashRadius(vec.data, blurValue);
				// iterate over the blurred vectors
				for (Centroid cent : centroids) {
					for (long h : hash) {
						if (cent.ids.contains(h)) {
							cent.updateVec(vec);
							break;
						}
					}
				}
			}
			vec = vecs.next();
		}
		for (Centroid cent : centroids)
			so.addCentroid(cent.centroid());

		return so;
	}

	private List<float[]> centroids = null;
	private RPHashObject so;

	public RPHashMultiProj(List<float[]> data, int k) {
		variance = StatTests.varianceSample(data, .01f);
		so = new SimpleArrayReader(data, k,0,2,4);
	}
	
	public RPHashMultiProj(List<float[]> data, int k,int numProjections) {
		variance = StatTests.varianceSample(data, .01f);
		so = new SimpleArrayReader(data, k,0,1,numProjections);
	}
	
	public RPHashMultiProj(List<float[]> data, int k, int decmult, int numProjections) {
		variance = StatTests.varianceSample(data, .01f);
		so = new SimpleArrayReader(data, k,0,decmult,numProjections);
	}

	public RPHashMultiProj(RPHashObject so) {
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
		centroids = new Kmeans(so.getk(),so.getCentroids()).getCentroids();
	}

	public static void main(String[] args) {
		int k = 20;
		int d = 5000;
		int n = 10000;
		float var = .51f;
		for (float f = var; f < 1.0; f += .01f) {
			for (int i = 0; i < 5; i++) {
				GenerateData gen = new GenerateData(k, n / k, d, f, true, 1f);
				RPHashMultiProj rphit = new RPHashMultiProj(gen.data(), k);

				long startTime = System.nanoTime();
				rphit.getCentroids();
				long duration = (System.nanoTime() - startTime);
				List<float[]> aligned = TestUtil.alignCentroids(
						rphit.getCentroids(), gen.medoids());
				System.out.println(f + ":" + StatTests.PR(aligned, gen) + ":"
						+ duration / 1000000000f);
				System.gc();
			}
		}

	}

}

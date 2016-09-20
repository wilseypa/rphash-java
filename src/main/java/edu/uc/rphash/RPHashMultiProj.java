package edu.uc.rphash;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import edu.uc.rphash.Readers.RPHashObject;
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
import edu.uc.rphash.tests.StatTests;
import edu.uc.rphash.tests.clusterers.Agglomerative;
import edu.uc.rphash.tests.clusterers.Agglomerative2;
import edu.uc.rphash.tests.clusterers.LloydIterativeKmeans;
import edu.uc.rphash.tests.generators.GenerateData;
import edu.uc.rphash.util.VectorUtil;

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
	public RPHashObject map() {
		Iterator<float[]> vecs = so.getVectorIterator();
		if (!vecs.hasNext())
			return so;

		long[] hash;
		int projections = so.getNumProjections();

		int k = (int) (so.getk() * projections) * 5;

		// initialize our counter
		ItemSet<Long> is = new SimpleFrequentItemSet<Long>(k);
		// create our LSH Device
		// create same LSH Device as before
		Random r = new Random(so.getRandomSeed());
		LSH[] lshfuncs = new LSH[projections];
		Decoder dec = so.getDecoderType();
		HashAlgorithm hal = new MurmurHash(so.getHashmod());

		// create same projection matrices as before
		for (int i = 0; i < projections; i++) {
			Projector p = new DBFriendlyProjection(so.getdim(),
					dec.getDimensionality(), r.nextLong());
			
			List<float[]> noise = LSH.genNoiseTable(dec.getDimensionality(),
					so.getNumBlur(), r,
					dec.getErrorRadius() / dec.getDimensionality());
			
			lshfuncs[i] = new LSH(dec, p, hal, noise);
		}

		// add to frequent itemset the hashed Decoded randomly projected vector
		while (vecs.hasNext()) {
			float[] vec = vecs.next();
			// iterate over the multiple projections
			for (LSH lshfunc : lshfuncs) {
				// could do a big parallel projection here
				hash = lshfunc.lshHashRadius(vec, so.getNumBlur());
				for (long hh : hash) {
					is.add(hh);
				}
			}
		}

		so.setPreviousTopID(is.getTop());
		List<Float> countsAsFloats = new ArrayList<Float>();

		for (long ct : is.getCounts())
			countsAsFloats.add((float) ct);
		so.setCounts(countsAsFloats);
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

		// make a set of k default centroid objects
		ArrayList<Centroid> centroids = new ArrayList<Centroid>();
		for (long id : so.getPreviousTopID())
			centroids.add(new Centroid(so.getdim(), id, -1));
		
		
		long[] hash;
		int projections = so.getNumProjections();

		// create our LSH Device
		// create same LSH Device as before
		Random r = new Random(so.getRandomSeed());
		LSH[] lshfuncs = new LSH[projections];
		Decoder dec = so.getDecoderType();
		HashAlgorithm hal = new MurmurHash(so.getHashmod());

		// create same projection matrices as before
		for (int i = 0; i < projections; i++) {
			Projector p = new DBFriendlyProjection(so.getdim(),
					dec.getDimensionality(), r.nextLong());
			List<float[]> noise = LSH.genNoiseTable(dec.getDimensionality(),
					so.getNumBlur(), r,
					dec.getErrorRadius() / dec.getDimensionality());
			lshfuncs[i] = new LSH(dec, p, hal, noise);
		}

		while (vecs.hasNext()) {
			float[] vec = vecs.next();
			// iterate over the multiple projections
			for (LSH lshfunc : lshfuncs) {
				// could do a big parallel projection here
				hash = lshfunc.lshHashRadius(vec, so.getNumBlur());
				for (Centroid cent : centroids) {
					for (long hh : hash) {
						if (cent.ids.contains(hh)) {
							cent.updateVec(vec);
							cent.addID(hh);
						}
					}
				}
			}
		}

//		List<float[]> centvectors = new ArrayList<float[]>();
//		List<Float> centcounts = new ArrayList<Float>();
//
//		for (Centroid cent : centroids) {
//			centvectors.add(cent.centroid());
//			centcounts.add((float) cent.getCount());
//		}
		so.setCentroids(centroids);
//		so.setCounts(centcounts);

		return so;
	}

	private List<Centroid> centroids = null;
	private RPHashObject so;

	public RPHashMultiProj(int k, List<float[]> data) {
		// variance = StatTests.varianceSample(data, .01f);
		// System.out.println(variance);
		so = new SimpleArrayReader(data, k);
		// so.getDecoderType().setVariance(variance);
	}

	public RPHashMultiProj(RPHashObject so) {
		this.so = so;
	}

	public List<Centroid> getCentroids(RPHashObject so) {
		this.so = so;
		if (centroids == null)
			run();
		return centroids;
	}

	@Override
	public List<Centroid> getCentroids() {

		if (centroids == null)
			run();

		return centroids;
	}

	private void run() {

		map();
		reduce();

		Clusterer offlineclusterer = so.getOfflineClusterer();
		offlineclusterer.setWeights(so.getCounts());
		offlineclusterer.setData(so.getCentroids());
		offlineclusterer.setK(so.getk());
		centroids = offlineclusterer.getCentroids();
	}

	public static void main(String[] args) {

		int k = 10;
		int d = 1000;
		int n = 20000;

		float var = .1f;
		for (float f = var; f < 2.1; f += .01f) {
			for (int i = 0; i < 1; i++) {
				GenerateData gen = new GenerateData(k, n / k, d, f, true, 1f);
				RPHashMultiProj rphit = new RPHashMultiProj(k, gen.data());
				long startTime = System.nanoTime();
				rphit.getCentroids();
				long duration = (System.nanoTime() - startTime);
//				List<float[]> aligned = VectorUtil.alignCentroids(
//						rphit.getCentroids(), gen.medoids());
//				System.out.println(f + ":" + StatTests.PR(aligned, gen) + ":"
//						+ StatTests.WCSSE(aligned, gen.getData()) + ":"
//						+ duration / 1000000000f);
				System.gc();
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
	public void setK(int getk) {
//		this.so.setK(getk);

	}

	@Override
	public void setRawData(List<float[]> centroids) {
		if(this.centroids == null) this.centroids = new ArrayList<>(centroids.size());
		for(float[] f: centroids){
			this.centroids.add(new Centroid(f,0));
		}
	}
	
	@Override
	public void reset(int randomseed) {
		centroids = null;
		so.setRandomSeed(randomseed);
	}

}

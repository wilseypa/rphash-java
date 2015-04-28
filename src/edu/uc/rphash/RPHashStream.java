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
import edu.uc.rphash.decoders.MultiDecoder;
import edu.uc.rphash.frequentItemSet.ItemSet;
import edu.uc.rphash.frequentItemSet.KHHCountMinSketch;
import edu.uc.rphash.frequentItemSet.SimpleFrequentItemSet;
import edu.uc.rphash.lsh.LSH;
import edu.uc.rphash.projections.DBFriendlyProjection;
import edu.uc.rphash.projections.Projector;
import edu.uc.rphash.standardhash.HashAlgorithm;
import edu.uc.rphash.standardhash.MurmurHash;
import edu.uc.rphash.tests.GenerateData;
import edu.uc.rphash.tests.Kmeans;
import edu.uc.rphash.tests.StatTests;
import edu.uc.rphash.tests.TestUtil;

public class RPHashStream implements Clusterer {



	float variance;

	public RPHashObject processStream() {
		// add to frequent itemset the hashed Decoded randomly projected vector
		Iterator<float[]> vecs = so.getVectorIterator();
		if (!vecs.hasNext())
			return so;

		Random r = new Random(so.getRandomSeed());
		int projections = so.getNumProjections();
		int k = (int) (so.getk()) * projections;
		long hash[];
		
		//initialize our counter
		KHHCountMinSketch<Centroid> is = new KHHCountMinSketch<>(k);
		
		// create LSH Device
		LSH[] lshfuncs = new LSH[projections];
		Decoder dec = so.getDecoderType();
		if(dec==null){
			Decoder inner = new Leech(variance);
			dec = new MultiDecoder( so.getInnerDecoderMultiplier()*inner.getDimensionality(), inner);
		}
		HashAlgorithm hal = new MurmurHash(so.getHashmod());
		
		//create projection matrices add to LSH Device
		for (int i = 0; i < projections; i++) {
			Projector p = new DBFriendlyProjection(so.getdim(),
					dec.getDimensionality(), r.nextLong());
			lshfuncs[i] = new LSH(dec, p, hal);
		}
		
		int blurValue = so.getNumBlur();
		if (blurValue == 0)
			blurValue = (int) Math.log(so.getdim() / dec.getDimensionality());

		while (vecs.hasNext()) {
			float[] vec = vecs.next();
			for (int i = 0; i < projections; i++) {
				hash = lshfuncs[i].lshHashRadius(vec,blurValue);
				for(long h : hash)
					is.add(new Centroid(h,vec));
			}
		}
		
		for(Centroid ff: is.getTop())
			so.addCentroid(ff.centroid());

		//for(long l: is.getCounts())System.out.print(l+",");
		
		return so;
	}


	private List<float[]> centroids = null;
	private RPHashObject so;

	public RPHashStream(List<float[]> data, int k) {
		variance = StatTests.varianceSample(data, .001f);
		so = new SimpleArrayReader(data, k);
		so.setNumProjections(3);
		so.setNumBlur(5);

	}

	public RPHashStream(List<float[]> data, int k, int times, int rseed) {
		variance = StatTests.varianceSample(data, .001f);
		so = new SimpleArrayReader(data, k);
		so.setNumProjections(3);
		so.setNumBlur(5);

	}

	public RPHashStream(RPHashObject so) {
		this.so = so;
	}

	public List<float[]> getCentroids(RPHashObject so) {
		this.so = so;
		if (centroids == null)
			run();
		return centroids;
	}

	@Override
	public List<float[]> getCentroids() {

		if (centroids == null)
			run();
		return centroids;
	}

	private void run() {
		so = processStream();
		centroids = (new Kmeans(so.getk(), so.getCentroids())).getCentroids();
		//centroids = new Kmeans(so.getk(),((SimpleArrayReader)so).data,24).getCentroids();
	}

	public static void main(String[] args) {

		int k = 10;
		int d = 1000;
		int n = 10000;
		float var = .3f;
		for (float f = var; f < 1.1; f += .1f) {
			for (int i = 0; i < 5; i++) {
				GenerateData gen = new GenerateData(k, n / k, d, f, true, 1f);
				RPHashStream rphit = new RPHashStream(gen.data(), k);

				long startTime = System.nanoTime();
				rphit.getCentroids();
				long duration = (System.nanoTime() - startTime);
				List<float[]> aligned = TestUtil.alignCentroids(
						rphit.getCentroids(), gen.medoids());
				System.out.println(f + ":" + StatTests.PR(aligned, gen) + ":"+StatTests.SSE(aligned, gen)+":"
						+ duration / 1000000000f);
				System.gc();
			}
		}

	}

	@Override
	public RPHashObject getParam() {
		return so;
	}

}

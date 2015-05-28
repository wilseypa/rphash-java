package edu.uc.rphash;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import edu.uc.rphash.Readers.RPHashObject;
import edu.uc.rphash.Readers.SimpleArrayReader;
import edu.uc.rphash.decoders.Decoder;
import edu.uc.rphash.decoders.Leech;
import edu.uc.rphash.decoders.MultiDecoder;
import edu.uc.rphash.decoders.Spherical;
import edu.uc.rphash.frequentItemSet.KHHCountMinSketch;
import edu.uc.rphash.frequentItemSet.KHHHashCounter;
import edu.uc.rphash.lsh.LSH;
import edu.uc.rphash.projections.DBFriendlyProjection;
import edu.uc.rphash.projections.Projector;
import edu.uc.rphash.standardhash.HashAlgorithm;
import edu.uc.rphash.standardhash.MurmurHash;
import edu.uc.rphash.tests.Agglomerative;
import edu.uc.rphash.tests.GenerateData;
import edu.uc.rphash.tests.Kmeans;
import edu.uc.rphash.tests.StatTests;
import edu.uc.rphash.tests.TestUtil;

public class RPHashStream implements Clusterer, Runnable {


	//TODO update variance over time
	float variance;
	KHHCountMinSketch<Centroid> is;
	boolean threadRunning = false;

	public RPHashObject processStream() {
		
		threadRunning = true;
		// add to frequent itemset the hashed Decoded randomly projected vector
		Iterator<float[]> vecs = so.getVectorIterator();
		if (!vecs.hasNext())
			return so;

		Random r = new Random(so.getRandomSeed());
		int projections = so.getNumProjections();
		int k =(int) (so.getk() * projections);
		long hash[];
		
		//initialize our counter
		is = new KHHCountMinSketch<>(k);

		// create LSH Device
		LSH[] lshfuncs = new LSH[projections];
		Decoder dec = so.getDecoderType();
		HashAlgorithm hal = new MurmurHash(so.getHashmod());
		
		//create projection matrices add to LSH Device
		for (int i = 0; i < projections; i++) {
			Projector p = new DBFriendlyProjection(so.getdim(),
					dec.getDimensionality(), r.nextLong());
			lshfuncs[i] = new LSH(dec, p, hal);
		}

		while (vecs.hasNext()) {
			is.add(new Centroid(r.nextLong(),vecs.next()));
			float[] vec = vecs.next();

			for (int i = 0; i < projections; i++) {
				hash = lshfuncs[i].lshHashRadius(vec,so.getNumBlur());
				for(long h : hash)
					is.add(new Centroid(h,vec));			
			}
		}
		
		return so;
	}


	private List<float[]> centroids = null;
	private RPHashObject so;

	public RPHashStream(List<float[]> data, int k) {
		variance = StatTests.varianceSample(data, .01f);
		this.average = StatTests.averageCol(data);
		so = new SimpleArrayReader(data, k);
		so.setDecoderType(new Leech(variance));
		so.getDecoderType().setVariance(variance);
	}
	float[] average;
//	public RPHashStream(List<float[]> data, int k, int times, int rseed) {
//		variance = StatTests.varianceSample(data, .01f);
//		this.average = StatTests.averageCol(data);
//		so = new SimpleArrayReader(data, k);
//		//best sphere is 8,2,64
//		//best leech is  8,2,1
//		so.setNumProjections(1);
//		so.setNumBlur(1);
//		so.setInnerDecoderMultiplier(1);
//
//	}

	public RPHashStream(RPHashObject so) {
		this.so = so;
	}

	public List<float[]> getCentroids(RPHashObject so) {
		this.so = so;
		if (centroids == null)
			run();
		return new Kmeans(so.getk(),centroids).getCentroids();
	}

	@Override
	public List<float[]> getCentroids() {

		if (centroids == null)
			run();
		centroids = new ArrayList<float[]>();
		for(Centroid c : is.getTop())centroids.add(c.centroid());
		return  new Kmeans(so.getk(),centroids).getCentroids();
	}

	public void run() {
		if(!threadRunning)//start processing
			so = processStream();
	}

	public static void main(String[] args) {

		int k = 10;
		int d = 1000;
		int n = 20000;
		float var = .3f;
		for (float f = var; f < 4.1; f += .2f) {
			for (int i = 0; i < 1; i++) {
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

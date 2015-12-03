package edu.uc.rphash;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import edu.uc.rphash.Readers.RPHashObject;
import edu.uc.rphash.Readers.SimpleArrayReader;
import edu.uc.rphash.frequentItemSet.KHHCountMinSketch;
import edu.uc.rphash.frequentItemSet.KHHHashCounter;
import edu.uc.rphash.lsh.LSH;
import edu.uc.rphash.projections.DBFriendlyProjection;
import edu.uc.rphash.projections.Projector;
import edu.uc.rphash.tests.GenerateData;
import edu.uc.rphash.tests.Kmeans;
import edu.uc.rphash.tests.StatTests;
import edu.uc.rphash.tests.TestUtil;

public class RPHashAltStream implements Clusterer, Runnable {

	float variance;
	KHHCountMinSketch<Centroid> is;
	Random r;

	public RPHashObject processStream() {
		// add to frequent itemset the hashed Decoded randomly projected vector
		Iterator<float[]> vecs = so.getVectorIterator();
		if (!vecs.hasNext())
			return so;
		r = new Random();

		int k = so.getk();// (int) (so.getk()) * projections;

		// initialize our counter
		is = new KHHCountMinSketch<>(k);

		while (vecs.hasNext()) {
			float[] vec = vecs.next();
			Centroid c = new Centroid(vec);
			is.add(c);

		}

		return so;
	}

	private List<float[]> centroids = null;
	private RPHashObject so;

	public RPHashAltStream(List<float[]> data, int k) {
		variance = StatTests.varianceSample(data, .01f);
		so = new SimpleArrayReader(data, k);
	}

	public RPHashAltStream(List<float[]> data, int k, int times, int rseed) {
		variance = StatTests.varianceSample(data, .01f);
		so = new SimpleArrayReader(data, k);
	}

	public RPHashAltStream(RPHashObject so) {
		this.so = so;
	}

	public List<float[]> getCentroids(RPHashObject so) {
		this.so = so;
		return getCentroids();
	}

	@Override
	public List<float[]> getCentroids() {

		if (centroids == null)
			run();
		return centroids;
	}

	public void run() {
		so = processStream();
		centroids = new ArrayList<float[]>();
		for (Centroid c : is.getTop())
			centroids.add(c.centroid());
		centroids = new Kmeans(so.getk(), centroids).getCentroids();
	}

	public static void main(String[] args) {

		int k = 10;
		int d = 1000;
		int n = 10000;
		float var = .3f;
		for (float f = var; f < 2.1; f += .1f) {
			for (int i = 0; i < 5; i++) {
				GenerateData gen = new GenerateData(k, n / k, d, f, true, 1f);
				RPHashAltStream rphit = new RPHashAltStream(gen.data(), k);
				long startTime = System.nanoTime();
				rphit.getCentroids();
				long duration = (System.nanoTime() - startTime);
				List<float[]> aligned = TestUtil.alignCentroids(
						rphit.getCentroids(), gen.medoids());
				System.out.println(f + ":" + StatTests.PR(aligned, gen) + ":"
						+ StatTests.WCSSE(aligned, gen.getData()) + ":"
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

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
import edu.uc.rphash.frequentItemSet.ItemSet;
import edu.uc.rphash.frequentItemSet.SimpleFrequentItemSet;
import edu.uc.rphash.lsh.LSH;
import edu.uc.rphash.projections.DBFriendlyProjection;
import edu.uc.rphash.projections.Projector;
import edu.uc.rphash.standardhash.HashAlgorithm;
import edu.uc.rphash.standardhash.MurmurHash;
import edu.uc.rphash.tests.GenerateData;
import edu.uc.rphash.tests.Kmeans;
import edu.uc.rphash.tests.StatTests;

public class RPHashStream implements Clusterer {

	class CentroidStream {
		int value;
		Centroid obj;

		public CentroidStream(int value, Centroid obj) {
			this.value = value;
			this.obj = obj;
		}

		@Override
		public int hashCode() {

			return (int) value;
		}

	}

	float variance;

	public RPHashObject map() {
		// create our LSH Machine
		Random r = new Random();
		HashAlgorithm hal = new MurmurHash((int) Long.MAX_VALUE);
		Iterator<RPVector> vecs = so.getVectorIterator();
		if (!vecs.hasNext())
			return so;

		Projector p = new DBFriendlyProjection(so.getdim(), Leech.Dim,
				r.nextInt());
		Decoder dec = new Leech(variance);
		LSH lshfunc = new LSH(dec, p, hal);
		// var = 1 => 1.6 density, var=1.5 =>1.63,var=2=>4.5

		// List<float[]> sampledata = new ArrayList<float[]>();
		// while (vecs.hasNext()) {
		//
		// RPVector vec = vecs.next();
		//
		// float[] tmp = new float[2];
		//
		// tmp[0]=p.project(vec.data)[r.nextInt(24)];
		// tmp[1]=p.project(vec.data)[r.nextInt(24)];
		// sampledata.add(tmp);
		// }
		// TestUtil.writeFile(new File("samplespro"+String.valueOf(1.5)+".mat"),
		// sampledata);

		// (int)(Math.log(so.getk())*so.getk()+.5);
		//

		long hash[];
		int probes = (int) (Math.log10(so.getn()) + .5);
		int k = (int) (so.getk()) * probes;
		ItemSet<Long> is = new SimpleFrequentItemSet<Long>(k);
		// add to frequent itemset the hashed Decoded randomly projected vector
		while (vecs.hasNext()) {
			RPVector vec = vecs.next();
			hash = lshfunc.lshHashRadius(vec.data, probes);// get all hashes at
															// once

			CentroidStream cent = new CentroidStream((int) hash[0], null);
			is.getTop().contains(cent);

			is.add(new Long(cent.hashCode()));
			vec.id.add(hash[0]);

		}
		so.setPreviousTopID(is.getTop());
		// for(Long l :
		// is.getCounts())System.out.printf("%d,",l);System.out.printf("\n,");
		return so;
	}

	/*
	 * This is the second phase after the top ids have been in the reduce phase
	 * aggregated
	 */
	public RPHashObject reduce() {

		Iterator<RPVector> vecs = so.getVectorIterator();
		if (!vecs.hasNext())
			return so;
		RPVector vec = vecs.next();

		// make a set of k default centroid objects
		ArrayList<Centroid> centroids = new ArrayList<Centroid>();
		for (Long id : so.getPreviousTopID())
			centroids.add(new Centroid(so.getdim(), id.hashCode()));

		while (vecs.hasNext()) {
			for (Centroid cent : centroids)
				if (!Collections.disjoint(cent.ids, vec.id))
					cent.updateVec(vec);
			vec = vecs.next();
		}

		for (Centroid cent : centroids) {
			so.addCentroid(cent.centroid());
		}
		return so;
	}

	private List<float[]> centroids = null;
	private RPHashObject so;

	public RPHashStream(List<float[]> data, int k) {
		variance = StatTests.varianceSample(data, .001f);
		so = new SimpleArrayReader(data, k, 1, 250000);
	}

	public RPHashStream(List<float[]> data, int k, int times, int rseed) {
		variance = StatTests.varianceSample(data, .001f);
		so = new SimpleArrayReader(data, k, rseed, 250000);
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
		so = map();
		so = reduce();
		centroids = (new Kmeans(so.getk(), so.getCentroids())).getCentroids();

	}

	public static void main(String[] args) {

		int k = 20;
		int d = 5000;
		int n = 10000;
		for (int i = 0; i < 5; i++) {
			GenerateData gen = new GenerateData(k, n / k, d, 1.f, true, 1f);

		}

	}

	@Override
	public RPHashObject getParam() {
		return so;
	}

}

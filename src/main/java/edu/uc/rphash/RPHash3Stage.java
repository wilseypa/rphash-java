package edu.uc.rphash;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import edu.uc.rphash.Readers.RPHashObject;
import edu.uc.rphash.Readers.SimpleArrayReader;
import edu.uc.rphash.decoders.Decoder;
import edu.uc.rphash.decoders.Leech;
import edu.uc.rphash.frequentItemSet.ItemSet;
import edu.uc.rphash.frequentItemSet.KHHCountMinSketch;
import edu.uc.rphash.lsh.LSH;
import edu.uc.rphash.projections.DBFriendlyProjection;
import edu.uc.rphash.projections.Projector;
import edu.uc.rphash.standardhash.HashAlgorithm;
import edu.uc.rphash.standardhash.MurmurHash;
import edu.uc.rphash.tests.StatTests;
import edu.uc.rphash.tests.clusterers.LloydIterativeKmeans;
import edu.uc.rphash.tests.generators.GenerateData;
import edu.uc.rphash.util.VectorUtil;

public class RPHash3Stage implements Clusterer {

	RPHashObject so;
	float variance;

	public RPHashObject mapP1() {
		Iterator<float[]> vecs = so.getVectorIterator();
		if (!vecs.hasNext())
			return so;

		long hash;
		int probes = so.getNumProjections();
		int k = (int) (so.getk() * probes);
		
		//initialize our counter
		ItemSet<Long> is = new KHHCountMinSketch<Long>(k);
		// create our LSH Device
		//create same LSH Device as before
		Random r = new Random(so.getRandomSeed());
		LSH[] lshfuncs = new LSH[probes];
		
		Decoder dec = so.getDecoderType();
		//Decoder dec = new MultiDecoder(1, innerdec);
		
		
		HashAlgorithm hal = new MurmurHash(so.getHashmod());
		
		//create same projection matrices as before
		for (int i = 0; i < probes; i++) {
			Projector p = new DBFriendlyProjection(so.getdim(),
					dec.getDimensionality(), r.nextLong());
			List<float[]> noise = LSH.genNoiseTable(dec.getDimensionality(),so.getNumBlur(), r, dec.getErrorRadius()/dec.getDimensionality());
			
			lshfuncs[i] = new LSH(dec, p, hal,noise,so.getNormalize());
		}
		// add to frequent itemset the hashed Decoded randomly projected vector
		while (vecs.hasNext()) {
			float[] vec = vecs.next();
			for (int i = 0; i < probes; i++) {
				hash = lshfuncs[i].lshHash(vec);
				is.add(hash);
			}
		}
		
		so.setPreviousTopID(is.getTop());
		return so;

	}

	/*
	 * This step is temporary for testing, it should be performed during the
	 * reduce phase 1.
	 */
	public RPHashObject mapP2() {
		Iterator<float[]> vecs = so.getVectorIterator();
		if (!vecs.hasNext())
			return so;

		long hash;
		int probes = so.getNumProjections();
//		int k = (int) (so.getk() * probes);
		
		//initialize our counter
//		ItemSet<Long> is = new KHHCountMinSketch<Long>(k);
		// create our LSH Device
		//create same LSH Device as before
		Random r = new Random(so.getRandomSeed());
		LSH[] lshfuncs = new LSH[probes];
		
		Decoder dec = so.getDecoderType();
		//Decoder dec = new MultiDecoder(1, innerdec);
		
		
		HashAlgorithm hal = new MurmurHash(so.getHashmod());
		
		//create same projection matrices as before
		for (int i = 0; i < probes; i++) {
			Projector p = new DBFriendlyProjection(so.getdim(),
					dec.getDimensionality(), r.nextLong());
			List<float[]> noise = LSH.genNoiseTable(dec.getDimensionality(),so.getNumBlur(), r, dec.getErrorRadius()/dec.getDimensionality());
			lshfuncs[i] = new LSH(dec, p, hal,noise,so.getNormalize());
		}

		
		Projector p = new DBFriendlyProjection(so.getdim(),
				100, r.nextLong());
		HashMap<Long, Centroid> centroids = new HashMap<Long, Centroid>();
		
		for (Long id : so.getPreviousTopID())
			centroids.put(id, new Centroid(100, id,-1));
		// start the calculation

		// int probes = 1;
		// add to frequent itemset the hashed Decoded randomly projected vector
		float[] vec = vecs.next();
		while (vecs.hasNext()) {
			// int j = 0;
			for (LSH lshfunc : lshfuncs) {
				Centroid cent = null;
				hash = lshfunc.lshHash(vec);
				cent = centroids.get(hash);
	
				if (cent != null){
					cent.updateVec(p.project(vec));
				}
				
			}
			vec = vecs.next();
			
		}
		for (Long id : centroids.keySet()) {
			so.addCentroid(centroids.get(id));

		}
		
		return so;
	}

	public RPHashObject mapP3() {
		ArrayList<Centroid> centroids = new ArrayList<Centroid>();
		Projector[] p = new Projector[1];
		p[0] = new DBFriendlyProjection(so.getdim(), 100, so.getRandomSeed());

		Iterator<float[]> vecs = so.getVectorIterator();
		for (int i = 0; i < so.getCentroids().size(); i++) {
			Centroid cent = new Centroid(so.getdim(), i,-1);
			centroids.add(cent);
		}
		float[] vec = vecs.next();
		while (vecs.hasNext()) {

			int nn = VectorUtil.findNearestDistance(new Centroid(p[0].project(vec),0),
					so.getCentroids());
			centroids.get(nn).updateVec(vec);
			vec = vecs.next();
		}
		so.setCentroids(centroids);
		return so;
	}

	private List<Centroid> centroids = null;

	public RPHash3Stage(List<float[]> data, int k) {
		variance = StatTests.varianceAll(data);
		so = new SimpleArrayReader(data, k, 1, 25000000);
	}

	public RPHash3Stage(RPHashObject so) {
		this.so = so;
	}

	public RPHash3Stage(List<float[]> data, int k, int rseed) {
		variance = StatTests.varianceAll(data);
		so = new SimpleArrayReader(data, k, rseed, 25000000);
	}

	public List<Centroid> getCentroids(RPHashObject so) {
		if (centroids == null)
			run(so);
		return centroids;
	}

	@Override
	public List<Centroid> getCentroids() {

		if (centroids == null)
			run(so);
		return centroids;
	}

	private void run(RPHashObject so) {
		so = mapP1();
		so = mapP2();
		so = mapP3();

		
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

		float var = .3f;
		for (float f = var; f < 4.1; f += .2f) {
			for (int i = 0; i < 1; i++) {
				GenerateData gen = new GenerateData(k, n / k, d, f, true, 1f);
				RPHash3Stage rphit = new RPHash3Stage(gen.data(), k);

				long startTime = System.nanoTime();
				rphit.getCentroids();
//				long duration = (System.nanoTime() - startTime);
//				List<float[]> aligned = VectorUtil.alignCentroids(
//						rphit.getCentroids(), gen.medoids());
//				System.out.println(f + ":" + StatTests.PR(aligned, gen) + ":"
//						+ StatTests.WCSSE(aligned, gen.data()) + ":" + duration
//						/ 1000000000f);
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
		
	}

	@Override
	public void setData(List<Centroid> centroids) {
		this.centroids = centroids;
		
	}
	@Override
	public void setRawData(List<float[]> centroids) {
		if(this.centroids == null) this.centroids = new ArrayList<>(centroids.size());
		for(float[] f: centroids){
			this.centroids.add(new Centroid(f,0));
		}
	}

	@Override
	public void setK(int getk) {
		this.so.setK(getk);
	}

	@Override
	public void reset(int randomseed) {
		centroids = null;
		so.setRandomSeed(randomseed);
	}
	
	@Override
	public boolean setMultiRun(int runs) {
		return false;
	}
}

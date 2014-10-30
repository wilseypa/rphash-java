package edu.uc.rphash;


import java.util.ArrayList;
import java.util.HashMap;

import edu.uc.rphash.Readers.RPHashObject;
import edu.uc.rphash.decoders.Decoder;
import edu.uc.rphash.decoders.LeechDecoder;
import edu.uc.rphash.frequentItemSet.ItemSet;
import edu.uc.rphash.frequentItemSet.StickyWrapper;
import edu.uc.rphash.lsh.LSH;
import edu.uc.rphash.projections.DBFriendlyProjection;
import edu.uc.rphash.projections.Projector;
import edu.uc.rphash.standardhash.HashAlgorithm;
import edu.uc.rphash.standardhash.MurmurHash;
import edu.uc.rphash.tests.TestUtil;

public class RPHash {

	public static float[] avgProjection(float[] v, Projector[] p, int dim) {
		float[] ravg = new float[dim];
		float sclr = 1f / (float) p.length;
		for (int i = 0; i < dim; i++)
			ravg[i] = 0.0f;
		for (int i = 0; i < p.length; i++) {
			float[] r1 = p[i].project(v);
			for (int j = 0; j < dim; j++)
				ravg[j] += (r1[j] * sclr);
		}

		return ravg;
	}


	public RPHashObject mapP1(RPHashObject so) {
		// create our LSH Machine
		HashAlgorithm hal = new MurmurHash(so.getHashmod());

		float[] vec = so.getNextVector();
		// trying to combat variance drifting issues by adjusting the
		// scaling the lattice region radius
		Decoder dec = new LeechDecoder(TestUtil.max(vec));
		Projector[] p = new Projector[so.getTimes()];
		for (int i = 0; i < so.getTimes(); i++) {
			p[i] = new DBFriendlyProjection(so.getdim(),
					dec.getDimensionality(), (i + 1) * so.getRandomSeed());
		}
		LSH lsh = new LSH(dec, p, hal, so.getTimes());
		ItemSet<Long> is = new StickyWrapper<Long>(so.getk(), so.getn());


		int probes = /*(int) (Math.log(so.getn()) + .5);*/ (int)(Math.pow(so.getn(),0.2671)+.5);
		// add to frequent itemset the hashed Decoded randomly projected vector
		while (vec != null) {
			is.add(lsh.lshHash(vec));
			int j = 0;
			while (j < probes) {
				is.add(lsh.lshHashRadius(vec));
				j++;
			}
			vec = so.getNextVector();
		}
		
		so.setIDs(is.getTop());
		so.setCounts(is.getCounts());
		so.reset();
		return so;
	}

	/*
	 * This step is temporary for testing, it should be performed during the reduce phase 1.
	 */
	public RPHashObject mapP2(RPHashObject so) {
		// create our LSH Machine
		HashAlgorithm hal = new MurmurHash(so.getHashmod());
		float[] vec = so.getNextVector();
		// trying to combat variance drifting issues by adjusting the
		// scaling the lattice region radius
		Decoder dec = new LeechDecoder(TestUtil.max(vec));

		Projector[] p = new Projector[so.getTimes()];
		for (int i = 0; i < so.getTimes(); i++) {
			p[i] = new DBFriendlyProjection(so.getdim(),
					dec.getDimensionality(), (i + 1) * so.getRandomSeed());
		}

		LSH lsh = new LSH(dec, p, hal, so.getTimes());
		// make a set of k default centroid objects
		HashMap<Long, Centroid> centroids = new HashMap<Long, Centroid>();
		for (Long id : so.getIDs())
			centroids.put(id, new Centroid(24));
		// start the calculation
		long d;
		int probes = /*(int) (Math.log(so.getn()) + .5);*/(int)(Math.pow(so.getn(),0.2671)+.5);
		// add to frequent itemset the hashed Decoded randomly projected vector
		while (vec != null) {
			int j = 0;
			Centroid cent = null;

			d = lsh.lshHash(vec);
			cent = centroids.get(d);
			if (cent != null)
				cent.updateVec(p[0].project(vec));	
			while (cent == null && j < probes) {
				d = lsh.lshHashRadius(vec);
				cent = centroids.get(d);
				if (cent != null)
					cent.updateVec(p[0].project(vec));
				j++;
			}
			vec = so.getNextVector();
		}
		for (Long id : centroids.keySet()) {
			so.addCentroid(centroids.get(id).centroid());
		}
		so.reset();
		return so;
	}
	
	public RPHashObject mapP3(RPHashObject so) {
		ArrayList<Centroid> centroids = new ArrayList<Centroid>();
		Projector[] p = new Projector[so.getTimes()];
		for (int i = 0; i < so.getTimes(); i++) {
			p[i] = new DBFriendlyProjection(so.getdim(),
					24, (i + 1) * so.getRandomSeed());
		}
		
		float[] vec = so.getNextVector();
		for(int i=0;i<so.getk();i++)
			centroids.add(new Centroid(so.getdim()));
		
		while (vec != null) {
			int nn = TestUtil.findNearestDistance(p[0].project(vec), so.getCentroids());
			centroids.get(nn).updateVec(vec);
			vec = so.getNextVector();
		}
		
		so.setCentroids(new ArrayList<float[]>());
		for (Centroid cent : centroids) 
			so.addCentroid(cent.centroid());

		return so;
		
	}

	public RPHashObject reduceP2(RPHashObject so) {
		return so;
	}
}

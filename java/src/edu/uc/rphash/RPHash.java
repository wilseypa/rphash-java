package edu.uc.rphash;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.uc.rphash.Readers.RPHashObject;
import edu.uc.rphash.decoders.Decoder;
import edu.uc.rphash.decoders.LeechDecoder;
import edu.uc.rphash.frequentItemSet.ItemSet;
import edu.uc.rphash.frequentItemSet.SimpleFrequentItemSet;
import edu.uc.rphash.frequentItemSet.StickyWrapper;
import edu.uc.rphash.lsh.LSH;
import edu.uc.rphash.projections.DBFriendlyProjection;
import edu.uc.rphash.projections.Projector;
import edu.uc.rphash.standardhash.FNVHash;
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

	// public RPHashObject mapP1(RPHashObject so) {
	// //create our LSH Machine
	// // Projector p = new DBFriendlyProjection(so.getdim(),
	// // dec.getDimensionality(),so.getRandomSeed());
	// float[] vec = so.getNextVector();
	// HashAlgorithm hal = new FNVHash(so.getHashmod());
	// Decoder dec = new LeechDecoder(TestUtil.max(vec));
	// Random r = new Random(so.getRandomSeed());
	// LSH lsh = new LSH(dec,hal);
	//
	// Projector[] p = new Projector[(int)(Math.log(so.getn()) +.5)];
	// for(int i = 0;i<p.length;i++)
	// p[i] = new DBFriendlyProjection(so.getdim(),
	// dec.getDimensionality(),r.nextInt());
	//
	//
	//
	//
	//

	public RPHashObject mapP1(RPHashObject so) {

		// create our LSH Machine
		HashAlgorithm hal = new FNVHash(so.getHashmod());
		float[] vec = so.getNextVector();
		// trying to combat variance drifting issues by adjusting the
		// scaling the lattice region radius
		Decoder dec = new LeechDecoder(TestUtil.max(vec) );
		Projector p = new DBFriendlyProjection(so.getdim(),
				dec.getDimensionality(), so.getRandomSeed());
		LSH lsh = new LSH(dec, p, hal);
		ItemSet<Long> is = new StickyWrapper<Long>(so.getk(), so.getn());

		int probes = (int)(Math.log(so.getn())+.5);//(int)(Math.pow(so.getn(),0.2671)+.5);
		// add to frequent itemset the hashed Decoded randomly projected vector
		while (vec != null) {
			is.add(lsh.lshHash(vec));
//			int j = 0;
//			while (j < probes) {
//				is.add(lsh.lshHashRadius(vec));
//				j++;
//			}
			vec = so.getNextVector();
		}

		so.setIDs(is.getTop());
		so.setCounts(is.getCounts());
		so.reset();
		return so;
	}

	//
	// do{
	// is.add(lsh.lshHashRadius(vec));
	// }while( j++<logattempts);
	// public RPHashObject reduceP1(RPHashObject so) {
	// //sum up top R^24 vector representatives
	//
	// HashAlgorithm hal = new FNVHash(so.getHashmod());
	// float[] vec = so.getNextVector();
	// Decoder dec = new LeechDecoder();
	//
	// Projector p = new DBFriendlyProjection(so.getdim(),
	// dec.getDimensionality(),so.getRandomSeed());
	// LSH lsh = new LSH(dec,p,hal);
	//
	// float logattempts =(float)Math.log(so.getn());
	// int j = 0;
	//
	// HashMap<Long,Centroid> centroids = new HashMap<Long,Centroid>();
	// for(Long id:so.getIDs())
	// centroids.put(id, new Centroid(dec.getDimensionality()) );
	//
	// long d;
	// //add some lattice centers around our point too (radius permutation
	// results in 1/3 collision prob)
	// do{
	// Centroid cent = null;
	// d = lsh.lshHashRadius(vec);
	// cent = centroids.get(d);
	//
	// if(cent!=null){
	// cent.updateVec(p.project(vec));
	// TestUtil.prettyPrint(p.project(vec));
	// }
	//
	// while(cent ==null && j++<logattempts){
	// d = lsh.lshHashRadius(vec);
	// cent = centroids.get(d);
	//
	// if(cent!=null){
	// TestUtil.prettyPrint(p.project(vec));
	// cent.updateVec(p.project(vec));
	// }
	// }
	// vec = so.getNextVector();
	// }while( j++<logattempts);
	//
	// for (Centroid v: centroids.values()){
	// so.addCentroid(v.centroid());
	// }
	// so.reset();
	//
	// return so ;
	// }

	public RPHashObject mapP2(RPHashObject so) {
		// create our LSH Machine
		HashAlgorithm hal = new FNVHash(so.getHashmod());
		float[] vec = so.getNextVector();
		// trying to combat variance drifting issues by adjusting the
		// scaling the lattice region radius
		Decoder dec = new LeechDecoder(TestUtil.max(vec));
		Projector p = new DBFriendlyProjection(so.getdim(),
				dec.getDimensionality(), so.getRandomSeed());
		LSH lsh = new LSH(dec, p, hal);
		// make a set of k default centroid objects
		HashMap<Long, Centroid> centroids = new HashMap<Long, Centroid>();
		for (Long id : so.getIDs())
			centroids.put(id, new Centroid(so.getdim()));
		// start the calculation
		long d;
		int probes = (int)(Math.log(so.getn())+.5);//(int)(Math.pow(so.getn(),0.2671)+.5);
		// add to frequent itemset the hashed Decoded randomly projected vector
		while (vec != null) {
			int j = 0;
			Centroid cent = null;
			
			d = lsh.lshHash(vec);
			cent = centroids.get(d);
			if (cent != null)
				cent.updateVec(vec);
			
			while (cent == null && j < probes){
				d = lsh.lshHashRadius(vec);
				cent = centroids.get(d);
				if (cent != null)
					cent.updateVec(vec);
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

	// public RPHashObject mapP2(RPHashObject so) {
	// //create our LSH Machine
	// HashAlgorithm hal = new FNVHash(so.getHashmod());
	// float[] vec = so.getNextVector();
	//
	// //cheap scaling, full normalization would be better
	// Decoder dec = new LeechDecoder(TestUtil.max(vec)*2);
	//
	// Projector p = new DBFriendlyProjection(so.getdim(),
	// dec.getDimensionality(),so.getRandomSeed());
	// LSH lsh = new LSH(dec,p,hal);
	//
	// //make a set of k default centroid objects
	// HashMap<Long,Centroid> centroids = new HashMap<Long,Centroid>();
	// for(Long id:so.getIDs())
	// {
	// centroids.put(id, new Centroid(so.getdim()) );
	// }
	//
	//
	// //start the calculation
	// float logattempts =(float)Math.log(so.getn());
	// long d;
	// //add to frequent itemset the hashed Decoded randomly projected vector
	// while(vec != null)
	// {
	//
	// //int nearest =
	// TestUtil.findNearestDistance(p.project(vec),so.getCentroids());
	// //centroids.get(nearest).updateVec(vec);
	// // This code had issues with increasing variance beyond 0.5
	// //furthermore, the lattice decoder is searching all lattice vectors for
	// //nearest neighbors, we have a smaller list of NN, making linear scan
	// //more efficient.
	// int j = 0 ;
	// Centroid cent = null;
	// d = lsh.lshHash(vec);
	// cent = centroids.get(d);
	// if(cent!=null)cent.updateVec(vec);
	// while(cent ==null && j<logattempts){
	// d = lsh.lshHashRadius(vec);
	// cent = centroids.get(d);
	// if(cent!=null)cent.updateVec(vec);
	// j++;
	// }
	// vec = so.getNextVector();
	// }
	//
	//
	// for (Centroid cent: centroids.values()){
	// so.addCentroid(cent.centroid());
	// }
	//
	// //
	// // for (Centroid cent: centroids){
	// // so.addCentroid(cent.centroid());
	// // }
	// //so.reset();
	// return so;
	// }

	public RPHashObject reduceP2(RPHashObject so) {
		return so;
	}
}

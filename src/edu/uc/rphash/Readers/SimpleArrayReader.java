package edu.uc.rphash.Readers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import edu.uc.rphash.decoders.Decoder;
import edu.uc.rphash.decoders.E8;
import edu.uc.rphash.decoders.Leech;
import edu.uc.rphash.decoders.MultiDecoder;
import edu.uc.rphash.decoders.PStableDistribution;

public class SimpleArrayReader implements RPHashObject {

	public List<float[]> data;
	// List<List<Float>> Xlist;
	int n;
	int dim;
	int numProjections;
	int decoderMultiplier;
	long randomSeed;
	long hashmod;
	int k;
	int numBlur;
	Decoder dec;

	public void setRandomSeed(long randomSeed) {
		this.randomSeed = randomSeed;
	}



	public void setNumBlur(int numBlur) {
		this.numBlur = numBlur;
	}

	List<float[]> centroids;
	List<Long> topIDs;

	public SimpleArrayReader(List<float[]> X, int k) {
		data = X;
//		data = new LinkedList<RPVector>();
//		for (int i = 0; i < X.size(); i++) {
//			RPVector r = new RPVector();
//			r.data = X.get(i);
//			r.count = 0;
//			r.id = new HashSet<Long>();
//			data.add(r);
//		}
		this.dec = null;
		this.n = X.size();
		this.dim = X.get(0).length;
		this.k = k;
		this.randomSeed = System.currentTimeMillis();
		this.hashmod = Long.MAX_VALUE;
		this.decoderMultiplier = 1;
		this.numProjections = 1;
		this.centroids = new ArrayList<float[]>();
		this.numBlur = 0;
		this.topIDs = new ArrayList<Long>();
		for (int i = 0; i < k; i++)
			topIDs.add((long) 0);
	}

	public SimpleArrayReader(List<float[]> X, int k, int blur) {

		data = X;
//		data = new LinkedList<RPVector>();
//		for (int i = 0; i < X.size(); i++) {
//			RPVector r = new RPVector();
//			r.data = X.get(i);
//			r.count = 0;
//			r.id = new HashSet<Long>();
//			data.add(r);
//		}

		this.n = X.size();
		this.dim = X.get(0).length;
		this.k = k;
		this.randomSeed = System.currentTimeMillis();
		this.hashmod = Long.MAX_VALUE;
		this.dec = null;
		this.decoderMultiplier = 1;
		this.numProjections = 1;
		this.centroids = new ArrayList<float[]>();
		this.numBlur = blur;
		this.topIDs = new ArrayList<Long>();
		for (int i = 0; i < k; i++)
			topIDs.add((long) 0);
	}

	public SimpleArrayReader(List<float[]> X, int k, int blur,
			int decoderMultiplier) {

		data = X;
//		data = new LinkedList<RPVector>();
//		for (int i = 0; i < X.size(); i++) {
//			RPVector r = new RPVector();
//			r.data = X.get(i);
//			r.count = 0;
//			r.id = new HashSet<Long>();
//			data.add(r);
//		}

		this.n = X.size();
		this.dim = X.get(0).length;
		this.k = k;
		this.randomSeed = System.currentTimeMillis();
		this.hashmod = Long.MAX_VALUE;
		this.numProjections = 1;
		this.decoderMultiplier = decoderMultiplier;
		this.centroids = new ArrayList<float[]>();
		this.numBlur = blur;
		this.topIDs = new ArrayList<Long>();
		this.dec = null;
		for (int i = 0; i < k; i++)
			topIDs.add((long) 0);
	}

	/**
	 * This instantiation of an array based RPHashObject is specific to the
	 * multi-projection clusterer
	 * 
	 * @param X
	 * @param k
	 * @param randomSeed
	 * @param hashmod
	 * @param blur
	 * @param decoderMutiplier
	 * @param numProjections
	 */
	public SimpleArrayReader(List<float[]> X, int k, int blur,
			int decoderMutiplier, int numProjections) {

		data = X;
//		data = new LinkedList<RPVector>();
//		for (int i = 0; i < X.size(); i++) {
//			RPVector r = new RPVector();
//			r.data = X.get(i);
//			r.count = 0;
//			r.id = new HashSet<Long>();
//			data.add(r);
//		}

		this.n = X.size();
		this.dim = X.get(0).length;
		this.k = k;
		this.randomSeed = System.currentTimeMillis();
		this.hashmod = Long.MAX_VALUE;
		this.numProjections = numProjections;
		this.decoderMultiplier = 1;
		this.centroids = new ArrayList<float[]>();
		;
		this.numBlur = blur;
		this.topIDs = new ArrayList<Long>();
		this.dec = null;
		for (int i = 0; i < k; i++)
			topIDs.add((long) 0);
	}

	public Iterator<float[]> getVectorIterator() {
		return data.iterator();
	}

	@Override
	public int getk() {
		return k;
	}


	@Override
	public int getdim() {
		return dim;
	}

	public long getHashmod() {
		return hashmod;
	}

	@Override
	public long getRandomSeed() {
		return randomSeed;
	}

	@Override
	public void reset() {

	}

	@Override
	public void addCentroid(float[] v) {
		centroids.add(v);
	}

	@Override
	public void setCentroids(List<float[]> l) {
		centroids = l;

	}

	@Override
	public List<float[]> getCentroids() {
		return centroids;
	}

	@Override
	public int getNumBlur() {
		return numBlur;
	}

	@Override
	public List<Long> getPreviousTopID() {
		return topIDs;
	}

	@Override
	public void setPreviousTopID(List<Long> top) {
		topIDs = top;
	}

	@Override
	public void setNumProjections(int probes) {
		this.numProjections = probes;
	}

	@Override
	public int getNumProjections() {
		return numProjections;
	}

	@Override
	public void setInnerDecoderMultiplier(int multiDim) {
		this.decoderMultiplier = multiDim;
	}

	@Override
	public int getInnerDecoderMultiplier() {
		return decoderMultiplier;
	}

	@Override
	public void setHashMod(long parseLong) {
		hashmod = parseLong;
		
	}



	@Override
	public void setDecoderType(Decoder dec) {
		this.dec = dec;
		
	}
	@Override
	public String toString() {
		String ret = "Decoder:";
		if(dec!=null)ret += dec.getClass().getName();
		ret+=", Blur:"+numBlur;
		ret+=", Projections:"+numProjections;
		ret+=", Outer Decoder Multiplier:"+decoderMultiplier;
		return ret;
	}



	@Override
	public Decoder getDecoderType() {
		return dec;
	}

}

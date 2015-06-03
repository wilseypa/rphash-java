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
	final int n;
	final int dim;
	int numProjections;
	int decoderMultiplier;
	long randomSeed;
	long hashmod;
	final int k;
	int numBlur;
	Decoder dec;
	 
	 
	 final static int DEFAULT_NUM_PROJECTIONS = 6;
	 final static int DEFAULT_NUM_BLUR = 2;
	 final static int DEFAULT_NUM_RANDOM_SEED = 0;
	 final static int DEFAULT_NUM_DECODER_MULTIPLIER = 1;
	 final static long DEFAULT_HASH_MODULUS = Long.MAX_VALUE;
	 final static Decoder DEFAULT_INNER_DECODER = new Leech();


	public void setRandomSeed(long randomSeed) {
		this.randomSeed = randomSeed;
	}



	public void setNumBlur(int numBlur) {
		this.numBlur = numBlur;
	}

	List<float[]> centroids;
	List<Long> topIDs;

	public SimpleArrayReader(List<float[]> X, int k) {

		this.randomSeed = DEFAULT_NUM_RANDOM_SEED;
		this.hashmod = DEFAULT_HASH_MODULUS;
		this.decoderMultiplier = DEFAULT_NUM_DECODER_MULTIPLIER;
		this.dec = new MultiDecoder(this.decoderMultiplier,DEFAULT_INNER_DECODER);
		this.numProjections = DEFAULT_NUM_PROJECTIONS;
		this.numBlur = DEFAULT_NUM_BLUR;
		
		
		data = X;
		this.n = X.size();
		this.dim = X.get(0).length;
		this.k = k;
		this.centroids = new ArrayList<float[]>();
		this.topIDs = new ArrayList<Long>();
		for (int i = 0; i < k; i++)
			topIDs.add((long) 0);
	}

	public SimpleArrayReader(List<float[]> X, int k, int blur) {

		this.randomSeed = DEFAULT_NUM_RANDOM_SEED;
		this.hashmod = DEFAULT_HASH_MODULUS;
		this.decoderMultiplier = DEFAULT_NUM_DECODER_MULTIPLIER;
		this.dec = new MultiDecoder(this.decoderMultiplier,DEFAULT_INNER_DECODER);
		this.numProjections = DEFAULT_NUM_PROJECTIONS;
		this.numBlur = blur;
		
		
		data = X;
		this.n = X.size();
		this.dim = X.get(0).length;
		this.k = k;
		this.centroids = new ArrayList<float[]>();
		this.topIDs = new ArrayList<Long>();
		for (int i = 0; i < k; i++)
			topIDs.add((long) 0);
	}

	public SimpleArrayReader(List<float[]> X, int k, int blur,
			int decoderMultiplier) {

		this.randomSeed = DEFAULT_NUM_RANDOM_SEED;
		this.hashmod = DEFAULT_HASH_MODULUS;
		this.dec = new MultiDecoder(this.decoderMultiplier,DEFAULT_INNER_DECODER);
		this.numProjections = DEFAULT_NUM_PROJECTIONS;
		this.numBlur = blur;
		this.decoderMultiplier = decoderMultiplier;
		
		
		data = X;
		this.n = X.size();
		this.dim = X.get(0).length;
		this.k = k;
		this.centroids = new ArrayList<float[]>();
		this.topIDs = new ArrayList<Long>();
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
			int decoderMultiplier, int numProjections) {
		this.randomSeed = DEFAULT_NUM_RANDOM_SEED;
		this.hashmod = DEFAULT_HASH_MODULUS;
		this.dec = new MultiDecoder(this.decoderMultiplier,DEFAULT_INNER_DECODER);
		this.numProjections = numProjections;
		this.numBlur = blur;
		this.decoderMultiplier = decoderMultiplier;
		
		
		data = X;
		this.n = X.size();
		this.dim = X.get(0).length;
		this.k = k;
		this.centroids = new ArrayList<float[]>();
		this.topIDs = new ArrayList<Long>();
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

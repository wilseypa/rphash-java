package edu.uc.rphash.Readers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import edu.uc.rphash.Clusterer;
import edu.uc.rphash.decoders.Decoder;
import edu.uc.rphash.decoders.MultiDecoder;
import edu.uc.rphash.tests.StatTests;
import edu.uc.rphash.tests.generators.ClusterGenerator;

public class SimpleArrayReader implements RPHashObject {

	List<float[]> data;
	Integer dim = null;
	int numProjections;
	int decoderMultiplier;
	long randomSeed;
	long hashmod;
	final int k;
	int numBlur;
	Decoder dec;
	float decayrate;
	List<float[]> centroids;
	List<Long> topIDs;
	boolean parallel = true;
	private int dimparameter;
	List<Float> counts;
	private Clusterer clusterer;

	public void setRandomSeed(long randomSeed) {
		this.randomSeed = randomSeed;
	}

	public void setNumBlur(int numBlur) {
		this.numBlur = numBlur;
	}

	public SimpleArrayReader(ClusterGenerator gen,int k) {

		this.dim = gen.getDimension();
		this.randomSeed = DEFAULT_NUM_RANDOM_SEED;
		this.hashmod = DEFAULT_HASH_MODULUS;
		this.decoderMultiplier = DEFAULT_NUM_DECODER_MULTIPLIER;
		this.dec = new MultiDecoder(this.decoderMultiplier*DEFAULT_INNER_DECODER.getDimensionality(),DEFAULT_INNER_DECODER);
		this.numProjections = DEFAULT_NUM_PROJECTIONS;
		this.numBlur = DEFAULT_NUM_BLUR;
		this.k = k;
//		this.n = 0;
		this.centroids = new ArrayList<float[]>();
		this.topIDs = new ArrayList<Long>();
		this.data = gen.getData();
		this.decayrate = 0;
		this.dimparameter = DEFAULT_DIM_PARAMETER;
		this.clusterer = DEFAULT_OFFLINE_CLUSTERER;
	}
	
	
	
	public SimpleArrayReader(List<float[]> X, int k) {

		this.randomSeed = DEFAULT_NUM_RANDOM_SEED;
		this.hashmod = DEFAULT_HASH_MODULUS;
		this.decoderMultiplier = DEFAULT_NUM_DECODER_MULTIPLIER;
		this.dec = new MultiDecoder(this.decoderMultiplier*DEFAULT_INNER_DECODER.getDimensionality(),DEFAULT_INNER_DECODER);
		this.numProjections = DEFAULT_NUM_PROJECTIONS;
		this.numBlur = DEFAULT_NUM_BLUR;
		this.data = X;
		if(data!=null)
			this.dim = data.get(0).length;
		else 
			this.dim = null;
		this.k = k;
		this.centroids = new ArrayList<float[]>();
		this.topIDs = new ArrayList<Long>();
		this.decayrate = 0;
		this.dimparameter = DEFAULT_DIM_PARAMETER;
		this.clusterer = DEFAULT_OFFLINE_CLUSTERER;
//		for (int i = 0; i < k; i++)
//			topIDs.add((long) 0);
	}

	public SimpleArrayReader(List<float[]> X, int k, int blur) {

		this.randomSeed = DEFAULT_NUM_RANDOM_SEED;
		this.hashmod = DEFAULT_HASH_MODULUS;
		this.decoderMultiplier = DEFAULT_NUM_DECODER_MULTIPLIER;
		this.dec = new MultiDecoder(this.decoderMultiplier*DEFAULT_INNER_DECODER.getDimensionality(),DEFAULT_INNER_DECODER);
		this.numProjections = DEFAULT_NUM_PROJECTIONS;
		this.numBlur = blur;
		data = X;
//		this.n = X.size();
		if(data!=null)
			this.dim = data.get(0).length;
		else 
			this.dim = null;
		this.k = k;
		this.centroids = new ArrayList<float[]>();
		this.topIDs = new ArrayList<Long>();
		for (int i = 0; i < k; i++)
			topIDs.add((long) 0);
		this.decayrate = 0;
		this.dimparameter = DEFAULT_DIM_PARAMETER;
		this.clusterer = DEFAULT_OFFLINE_CLUSTERER;
	}

	public SimpleArrayReader(List<float[]> X, int k, int blur,
			int decoderMultiplier) {

		this.randomSeed = DEFAULT_NUM_RANDOM_SEED;
		this.hashmod = DEFAULT_HASH_MODULUS;
		this.dec = new MultiDecoder(this.decoderMultiplier*DEFAULT_INNER_DECODER.getDimensionality(),DEFAULT_INNER_DECODER);
		this.numProjections = DEFAULT_NUM_PROJECTIONS;
		this.numBlur = blur;
		this.decoderMultiplier = decoderMultiplier;
		this.decayrate = 0;
		this.dimparameter = DEFAULT_DIM_PARAMETER;
		
		data = X;
		if(data!=null)
			this.dim = data.get(0).length;
		else 
			this.dim = null;
		this.k = k;
		this.centroids = new ArrayList<float[]>();
		this.topIDs = new ArrayList<Long>();
		for (int i = 0; i < k; i++)
			topIDs.add((long) 0);
		this.clusterer = DEFAULT_OFFLINE_CLUSTERER;
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
		this.dec = new MultiDecoder(this.decoderMultiplier*DEFAULT_INNER_DECODER.getDimensionality(),DEFAULT_INNER_DECODER);
		this.numProjections = numProjections;
		this.numBlur = blur;
		this.decoderMultiplier = decoderMultiplier;
		data = X;
		if(data!=null)
			this.dim = data.get(0).length;
		else 
			this.dim = null;
		this.k = k;
		this.centroids = new ArrayList<float[]>();
		this.topIDs = new ArrayList<Long>();
		for (int i = 0; i < k; i++)
			topIDs.add((long) 0);
		this.decayrate = 0;
		this.dimparameter = DEFAULT_DIM_PARAMETER;
		this.clusterer = DEFAULT_OFFLINE_CLUSTERER;
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
		if(this.dim==null)
			this.dim = data.get(0).length;
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
		System.out.println("cannot reset a data stream");
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
		ret += ", Offline Clusterer:" + clusterer.getClass().getName();
		return ret;
	}

	@Override
	public void setVariance(List<float[]> data) {
		dec.setVariance(StatTests.varianceSample(data, .01f));
	}

	@Override
	public Decoder getDecoderType() {
		return dec;
	}

	@Override
	public void setDecayRate(float parseFloat) {
		this.decayrate = parseFloat;
	}

	@Override
	public float getDecayRate() {
		return this.decayrate;
	}

	@Override
	public void setParallel(boolean parseBoolean) {
		this.parallel = parseBoolean;
		
	}

	@Override
	public boolean getParallel() {
		return parallel;
	}
	
	@Override
	public void setDimparameter(int parseInt) {
		this.dimparameter = parseInt;
		
	}

	@Override
	public int getDimparameter() {

		return this.dimparameter;
	}

	@Override
	public void setCounts(List<Float> counts) {

		this.counts = counts;
	}

	@Override
	public List<Float> getCounts() {
		return counts;
	}

	@Override
	public void setOfflineClusterer(Clusterer agglomerative3) {
		this.clusterer = agglomerative3;
	}

	@Override
	public Clusterer getOfflineClusterer() {
		return this.clusterer;
	}
}

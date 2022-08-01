package edu.uc.rphash.Readers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import edu.uc.rphash.Centroid;
import edu.uc.rphash.Clusterer;
import edu.uc.rphash.decoders.Decoder;
import edu.uc.rphash.decoders.MultiDecoder;
import edu.uc.rphash.projections.Projector;
import edu.uc.rphash.tests.StatTests;
import edu.uc.rphash.tests.generators.ClusterGenerator;

public class SimpleArrayReader implements RPHashObject {

	List<float[]> data;
	Integer dim = null;
	int numProjections;
	int decoderMultiplier;
	long randomSeed;
	long hashmod;
	int k;
	int numBlur;
	Decoder dec;
	float decayrate;
	List<Centroid> centroids;
	List<Long> topIDs;
	boolean parallel = true;
	private int dimparameter;
	List<Float> counts;
	private Clusterer clusterer;
	private boolean normalize = false;
	private Projector projector;
	
	boolean RandomVector = false;
	int Cutoff;
	
	

	public void setRandomSeed(long randomSeed) {
		this.randomSeed = randomSeed;
	}

	public void setNumBlur(int numBlur) {
		this.numBlur = numBlur;
	}

	public SimpleArrayReader(ClusterGenerator gen,int k) {

		this.dim = gen.getDimension();
		this.randomSeed = new Random().nextLong();
		this.hashmod = DEFAULT_HASH_MODULUS;
		this.decoderMultiplier = DEFAULT_NUM_DECODER_MULTIPLIER;
		if(decoderMultiplier>1)
			this.dec = new MultiDecoder(this.decoderMultiplier*DEFAULT_INNER_DECODER.getDimensionality(),DEFAULT_INNER_DECODER);
		else
			this.dec = DEFAULT_INNER_DECODER;
		this.numProjections = DEFAULT_NUM_PROJECTIONS;
		this.numBlur = DEFAULT_NUM_BLUR;
		this.k = k;
//		this.n = 0;
		this.centroids = new ArrayList<Centroid>();
		this.topIDs = new ArrayList<Long>();
		this.data = gen.getData();
		this.decayrate = 0;
		this.dimparameter = DEFAULT_DIM_PARAMETER;
		this.clusterer = DEFAULT_OFFLINE_CLUSTERER;
		this.projector = DEFAULT_PROJECTOR;
	}
	
	
	
	public SimpleArrayReader(List<float[]> X, int k) {

		this.randomSeed = new Random().nextLong();
		this.hashmod = DEFAULT_HASH_MODULUS;
		this.decoderMultiplier = DEFAULT_NUM_DECODER_MULTIPLIER;
		if(this.decoderMultiplier>1)
			this.dec = new MultiDecoder(this.decoderMultiplier*DEFAULT_INNER_DECODER.getDimensionality(),DEFAULT_INNER_DECODER);
		else
			this.dec = DEFAULT_INNER_DECODER;
		this.numProjections = DEFAULT_NUM_PROJECTIONS;
		this.numBlur = DEFAULT_NUM_BLUR;
		this.data = X;
		if(data!=null)
			this.dim = data.get(0).length;
		else 
			this.dim = null;
		this.k = k;
		this.centroids = new ArrayList<Centroid>();
		this.topIDs = new ArrayList<Long>();
		this.decayrate = 0;
		this.dimparameter = DEFAULT_DIM_PARAMETER;
		this.clusterer = DEFAULT_OFFLINE_CLUSTERER;
		this.projector = DEFAULT_PROJECTOR;
//		for (int i = 0; i < k; i++)
//			topIDs.add((long) 0);
	}

	public SimpleArrayReader(List<float[]> X) {

		this.randomSeed = new Random().nextLong();
		this.hashmod = DEFAULT_HASH_MODULUS;
		this.decoderMultiplier = DEFAULT_NUM_DECODER_MULTIPLIER;
		if(this.decoderMultiplier>1)
			this.dec = new MultiDecoder(this.decoderMultiplier*DEFAULT_INNER_DECODER.getDimensionality(),DEFAULT_INNER_DECODER);
		else
			this.dec = DEFAULT_INNER_DECODER;
		this.numProjections = DEFAULT_NUM_PROJECTIONS;
		this.numBlur = DEFAULT_NUM_BLUR;
		this.data = X;
		if(data!=null)
			this.dim = data.get(0).length;
		else 
			this.dim = null;
		// this.k = k;
		this.centroids = new ArrayList<Centroid>();
		this.topIDs = new ArrayList<Long>();
		this.decayrate = 0;
		this.dimparameter = DEFAULT_DIM_PARAMETER;
		this.clusterer = DEFAULT_OFFLINE_CLUSTERER;
		this.projector = DEFAULT_PROJECTOR;
//		for (int i = 0; i < k; i++)
//			topIDs.add((long) 0);
	}	
	
	
	
	
	
//	public SimpleArrayReader(List<float[]> X, int k, int blur) {
//
//		this.randomSeed = DEFAULT_NUM_RANDOM_SEED;
//		this.hashmod = DEFAULT_HASH_MODULUS;
//		this.decoderMultiplier = DEFAULT_NUM_DECODER_MULTIPLIER;
//		this.dec = new MultiDecoder(this.decoderMultiplier*DEFAULT_INNER_DECODER.getDimensionality(),DEFAULT_INNER_DECODER);
//		this.numProjections = DEFAULT_NUM_PROJECTIONS;
//		this.numBlur = blur;
//		this.data = X;
////		this.n = X.size();
//		if(this.data!=null)
//			this.dim = data.get(0).length;
//		else 
//			this.dim = null;
//		this.k = k;
//		this.centroids = new ArrayList<Centroid>();
//		this.topIDs = new ArrayList<Long>();
//		for (int i = 0; i < k; i++)
//			topIDs.add((long) 0);
//		this.decayrate = 0;
//		this.dimparameter = DEFAULT_DIM_PARAMETER;
//		this.clusterer = DEFAULT_OFFLINE_CLUSTERER;
//	}

	public SimpleArrayReader(List<float[]> X, int k, int blur,
			int decoderMultiplier) {

		this.randomSeed = new Random().nextLong();
		this.hashmod = DEFAULT_HASH_MODULUS;
		this.dec = new MultiDecoder(this.decoderMultiplier*DEFAULT_INNER_DECODER.getDimensionality(),DEFAULT_INNER_DECODER);
		this.numProjections = DEFAULT_NUM_PROJECTIONS;
		this.numBlur = blur;
		this.decoderMultiplier = decoderMultiplier;
		this.decayrate = 0;
		this.dimparameter = DEFAULT_DIM_PARAMETER;
		
		this.data = X;
		if(this.data!=null)
			this.dim = this.data.get(0).length;
		else 
			this.dim = null;
		this.k = k;
		this.centroids = new ArrayList<Centroid>();
		this.topIDs = new ArrayList<Long>();
		for (int i = 0; i < k; i++)
			topIDs.add((long) 0);
		this.clusterer = DEFAULT_OFFLINE_CLUSTERER;
		this.projector = DEFAULT_PROJECTOR;
	}

	/**
	 * This instantiation of an array based RPHashObject is specific to the
	 * multi-projection clusterer
	 * 
	 * @param X
	 * @param t
	 * @param randomSeed
	 * @param hashmod
	 * @param blur
	 * @param decoderMutiplier
	 * @param numProjections
	 */
//	public SimpleArrayReader(List<float[]> X, int k, int blur,
//			int decoderMultiplier, int numProjections) {
//
//		this.randomSeed = DEFAULT_NUM_RANDOM_SEED;
//		this.hashmod = DEFAULT_HASH_MODULUS;
//		this.dec = new MultiDecoder(this.decoderMultiplier*DEFAULT_INNER_DECODER.getDimensionality(),DEFAULT_INNER_DECODER);
//		this.numProjections = numProjections;
//		this.numBlur = blur;
//		this.decoderMultiplier = decoderMultiplier;
//		data = X;
//		if(data!=null)
//			this.dim = data.get(0).length;
//		else 
//			this.dim = null;
//		this.k = k;
//		this.centroids = new ArrayList<Centroid>();
//		this.topIDs = new ArrayList<Long>();
//		for (int i = 0; i < k; i++)
//			topIDs.add((long) 0);
//		this.decayrate = 0;
//		this.dimparameter = DEFAULT_DIM_PARAMETER;
//		this.clusterer = DEFAULT_OFFLINE_CLUSTERER;
//	}

	public SimpleArrayReader() {
		this.randomSeed = new Random().nextLong();
		this.hashmod = DEFAULT_HASH_MODULUS;
		this.decoderMultiplier = DEFAULT_NUM_DECODER_MULTIPLIER;
		this.dec = new MultiDecoder(this.decoderMultiplier*DEFAULT_INNER_DECODER.getDimensionality(),DEFAULT_INNER_DECODER);
		this.numProjections = DEFAULT_NUM_PROJECTIONS;
		this.numBlur = DEFAULT_NUM_BLUR;
		this.centroids = new ArrayList<Centroid>();
		this.topIDs = new ArrayList<Long>();
		this.decayrate = 0;
		this.dimparameter = DEFAULT_DIM_PARAMETER;
		this.clusterer = DEFAULT_OFFLINE_CLUSTERER;
		this.projector = DEFAULT_PROJECTOR;
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
	public void addCentroid(Centroid v) {
		centroids.add(v);
	}

	@Override
	public void setCentroids(List<Centroid> l) {
		centroids = l;
	}

	@Override
	public List<Centroid> getCentroids() {
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
		if(dec!=null)ret += dec.getClass().getSimpleName();
		ret+=", Blur:"+numBlur;
		ret+=", Projections:"+numProjections;
		ret+=", Outer Decoder Multiplier:"+decoderMultiplier;
		ret += ", Offline Clusterer:";
		ret += clusterer==null?"none":clusterer.getClass().getSimpleName();
		return ret;
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

	public List<float[]> getRawData() {
		return data;
	}

	@Override
	public void setK(int getk) {
		this.k = getk;
		for (int i = 0; i < this.k; i++)
			topIDs.add((long) 0);
	}

	@Override
	public void setRawData(List<float[]> c) {
		this.data = c;
	}

	@Override
	public void addRawData(float[] centroid) {
		if(data==null)data=new ArrayList<>();
		data.add(centroid);
	}

	@Override
	public void setNormalize(boolean parseBoolean) {
		this.normalize = parseBoolean;		
	}
	
	public boolean getNormalize() {
		return this.normalize;		
	}

	@Override
	public void setProjectionType(Projector dbFriendlyProjection) {
		this.projector = dbFriendlyProjection;
	}
	@Override
	public Projector getProjectionType(){
		return this.projector;
	}
	
	
	
	@Override
	public void setCutoff(int parseInt) {
		this.Cutoff = parseInt;
		
	}

	@Override
	public int getCutoff() {

		return this.Cutoff;
	}
	
	
	
	@Override
	public void setRandomVector(boolean parseBoolean) {
		this.RandomVector = parseBoolean;		
	}
	public boolean getRandomVector() {
		return this.RandomVector;		
	}
	
	
	
}

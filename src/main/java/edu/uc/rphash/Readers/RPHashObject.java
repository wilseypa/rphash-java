package edu.uc.rphash.Readers;

import java.util.Iterator;
import java.util.List;

import edu.uc.rphash.Clusterer;
import edu.uc.rphash.decoders.Decoder;
import edu.uc.rphash.decoders.E8;
import edu.uc.rphash.decoders.Golay;
import edu.uc.rphash.decoders.Leech;
import edu.uc.rphash.decoders.MultiDecoder;
import edu.uc.rphash.decoders.PsdLSH;
import edu.uc.rphash.decoders.Spherical;
import edu.uc.rphash.tests.clusterers.Agglomerative;
import edu.uc.rphash.tests.clusterers.Agglomerative3;
import edu.uc.rphash.tests.clusterers.KMeans2;
import edu.uc.rphash.tests.clusterers.Kmeans;

public interface RPHashObject {
	 final static int DEFAULT_NUM_PROJECTIONS = 2;
	 public final static int DEFAULT_NUM_BLUR = 1;
	 final static long DEFAULT_NUM_RANDOM_SEED = 38006359550206753L;
	 final static int DEFAULT_NUM_DECODER_MULTIPLIER = 1;
	 final static long DEFAULT_HASH_MODULUS = Long.MAX_VALUE;
	 final static Decoder DEFAULT_INNER_DECODER = new Spherical(24,6,1);//new MultiDecoder(24, new E8(1f));//new Golay();//new Spherical(64,2,1);//new Leech(3);//new PsdLSH();//
	 final static int DEFAULT_DIM_PARAMETER = DEFAULT_INNER_DECODER.getDimensionality();

	 final static Clusterer DEFAULT_OFFLINE_CLUSTERER = new KMeans2();

	 
	 
	int getk();
	int getdim();
	long getRandomSeed();
	long getHashmod();
	int getNumBlur();
	
	Iterator<float[]> getVectorIterator();
	List<float[]> getCentroids( );
	List<Float> getCounts( );
	void setCounts(List<Float> counts);
	
	List<Long> getPreviousTopID();
	void setPreviousTopID(List<Long> i);
	
	void addCentroid(float[] v);
	void setCentroids(List<float[]> l);
	
	void reset();//TODO rename to resetDataStream
	int getNumProjections();
	void setNumProjections(int probes);
	void setInnerDecoderMultiplier(int multiDim);
	int getInnerDecoderMultiplier();
	void setNumBlur(int parseInt);
	void setRandomSeed(long parseLong);
	void setHashMod(long parseLong);
	void setDecoderType(Decoder dec);
	Decoder getDecoderType();
	String toString();
	void setVariance(List<float[]> data);
	void setDecayRate(float parseFloat);
	float getDecayRate();
	void setParallel(boolean parseBoolean);
	boolean getParallel();
	void setDimparameter(int parseInt);
	int getDimparameter();
	void setOfflineClusterer(Clusterer agglomerative3);
	Clusterer getOfflineClusterer();
	List<float[]> getData();

}

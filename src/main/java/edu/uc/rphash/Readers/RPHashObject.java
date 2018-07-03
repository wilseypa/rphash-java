package edu.uc.rphash.Readers;

import java.util.Iterator;
import java.util.List;

import edu.uc.rphash.Centroid;
import edu.uc.rphash.Clusterer;
import edu.uc.rphash.decoders.Decoder;
import edu.uc.rphash.decoders.DepthProbingLSH;
import edu.uc.rphash.decoders.E8;

import edu.uc.rphash.decoders.Leech;
import edu.uc.rphash.decoders.MultiDecoder;

import edu.uc.rphash.decoders.Spherical;
import edu.uc.rphash.projections.DBFriendlyProjection;
import edu.uc.rphash.projections.GaussianProjection;
import edu.uc.rphash.projections.Projector;

import edu.uc.rphash.tests.clusterers.Agglomerative3;
import edu.uc.rphash.tests.clusterers.Agglomerative3.ClusteringType;
import edu.uc.rphash.tests.clusterers.KMeans2;
import edu.uc.rphash.tests.clusterers.KMeans2NoWCSS;
import edu.uc.rphash.tests.clusterers.KMeansPlusPlus;

import edu.uc.rphash.tests.clusterers.MultiKMPP;
import edu.uc.rphash.tests.clusterers.DBScan;

public interface RPHashObject {
	final static int DEFAULT_NUM_PROJECTIONS = 1;
	public final static int DEFAULT_NUM_BLUR = 1;
	final static long DEFAULT_NUM_RANDOM_SEED = 3800635955020675334L;
	final static int DEFAULT_NUM_DECODER_MULTIPLIER = 1;
	final static long DEFAULT_HASH_MODULUS = Long.MAX_VALUE;
	final static Decoder DEFAULT_INNER_DECODER = new Spherical(32,4,1);//new DepthProbingLSH(24);//new Leech();//new Spherical(16,2,2);//new MultiDecoder(24, new E8(1f));//new Golay();//new Spherical(64,2,1);//new Leech(3);//new PsdLSH();//
	final static int DEFAULT_DIM_PARAMETER = DEFAULT_INNER_DECODER.getDimensionality();

	final static Clusterer DEFAULT_OFFLINE_CLUSTERER = new KMeansPlusPlus();//new Agglomerative3(ClusteringType.AVG_LINKAGE);

	
	//final static Clusterer DEFAULT_OFFLINE_CLUSTERER = new Agglomerative3(ClusteringType.AVG_LINKAGE);
	
	//final static Clusterer DEFAULT_OFFLINE_CLUSTERER = new MultiKMPP();

	final static Projector DEFAULT_PROJECTOR = new DBFriendlyProjection(); 
	//final static Projector DEFAULT_PROJECTOR = new GaussianProjection(); 
	
	int getdim();
	
	Iterator<float[]> getVectorIterator();
	List<float[]> getRawData();
	void setRawData(List<float[]> c);
	void addRawData(float[] centroid);
	
	List<Float> getCounts( );
	void setCounts(List<Float> counts);
	
	List<Long> getPreviousTopID();
	void setPreviousTopID(List<Long> i);
	
	void addCentroid(Centroid v);
	void setCentroids(List<Centroid> l);
	List<Centroid> getCentroids( );

	
	int getNumProjections();
	void setNumProjections(int probes);
	
	void setInnerDecoderMultiplier(int multiDim);
	int getInnerDecoderMultiplier();
	
	void setNumBlur(int parseInt);
	int getNumBlur();
	
	void setRandomSeed(long parseLong);
	long getRandomSeed();
	
	void setHashMod(long parseLong);
	long getHashmod();
	
	void setDecoderType(Decoder dec);
	Decoder getDecoderType();
	
	void setDecayRate(float parseFloat);
	float getDecayRate();
	
	void setParallel(boolean parseBoolean);
	boolean getParallel();
	
	void setDimparameter(int parseInt);
	int getDimparameter();
	
//	void setOfflineClusterer(Clusterer agglomerative3);   
//	Clusterer getOfflineClusterer();
	
	
	void setOfflineClusterer(Clusterer clusterer );   
	Clusterer getOfflineClusterer();
	
	
	int getk();
	void setK(int getk);
	
	String toString();
	void reset();//TODO rename to resetDataStream

	void setNormalize(boolean parseBoolean);
	boolean getNormalize();

	void setProjectionType(Projector dbFriendlyProjection);

	Projector getProjectionType();

}

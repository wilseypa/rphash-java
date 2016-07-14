package edu.uc.rphash.Readers;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.zip.GZIPInputStream;

import edu.uc.rphash.Clusterer;
import edu.uc.rphash.decoders.Decoder;
import edu.uc.rphash.decoders.MultiDecoder;
import edu.uc.rphash.tests.StatTests;

public class StreamObject implements RPHashObject, Iterator<float[]> {
	public List<float[]> data;
	int numProjections;
	int decoderMultiplier;
	long randomSeed;
	int numBlur;
	String f;
	InputStream elements;
	int k;
	int dim;
	int randomseed;
	long hashmod;
	List<float[]> centroids;
	List<Long> topIDs;
	int multiDim;
	Decoder dec;
	float decayrate=0;
	boolean parallel = true;

	ExecutorService executor;
	InputStream inputStream;
	boolean raw;

	BufferedReader assin;
	DataInputStream binin;

	// input format
	// per line
	// top ids list (integers)
	// --num of clusters ( == k)
	// --num of data( == n)
	// --num dimensions
	// --input random seed;
	public StreamObject(PipedInputStream istream, int k, int dim,
			ExecutorService executor) throws IOException {
		this.executor = executor;

		this.dim = dim;
		this.randomSeed = DEFAULT_NUM_RANDOM_SEED;
		this.hashmod = DEFAULT_HASH_MODULUS;
		this.decoderMultiplier = DEFAULT_NUM_DECODER_MULTIPLIER;
		this.dec = new MultiDecoder(this.decoderMultiplier
				* DEFAULT_INNER_DECODER.getDimensionality(),
				DEFAULT_INNER_DECODER);
		this.numProjections = DEFAULT_NUM_PROJECTIONS;
		this.numBlur = DEFAULT_NUM_BLUR;
		this.k = k;
		this.data = null;
		this.centroids = new ArrayList<float[]>();
		this.topIDs = new ArrayList<Long>();
		this.dimparameter = DEFAULT_DIM_PARAMETER;
		this.clusterer = DEFAULT_OFFLINE_CLUSTERER;
	}

	boolean filereader = false;
	private int dimparameter;
	private List<Float> counts;
	private Clusterer clusterer;

	public StreamObject(String f, int k, boolean raw) throws IOException {
		this.f = f;

		filereader = true;
		// if (this.f.endsWith("gz"))
		// inputStream = new BufferedReader(new InputStreamReader(
		// new GZIPInputStream(new FileInputStream(this.f))));
		// else
		// inputStream = new BufferedReader(new InputStreamReader(
		// new FileInputStream(this.f)));
		// read the n and m dimension header
		this.raw = raw;

		if (this.f.endsWith("gz"))
			inputStream = new GZIPInputStream(new FileInputStream(this.f));
		else
			inputStream = new FileInputStream(this.f);

		if (!raw) {
			assin = new BufferedReader(new InputStreamReader(inputStream));
			 Integer.parseInt(assin.readLine());
			dim = Integer.parseInt(assin.readLine());
		} else {
			binin = new DataInputStream(new BufferedInputStream(inputStream));
			binin.readInt();
			dim = binin.readInt();
		}
		this.randomSeed = DEFAULT_NUM_RANDOM_SEED;
		this.hashmod = DEFAULT_HASH_MODULUS;
		this.decoderMultiplier = DEFAULT_NUM_DECODER_MULTIPLIER;
		this.dec = new MultiDecoder(this.decoderMultiplier
				* DEFAULT_INNER_DECODER.getDimensionality(),
				DEFAULT_INNER_DECODER);
		this.numProjections = DEFAULT_NUM_PROJECTIONS;
		this.numBlur = DEFAULT_NUM_BLUR;
		this.k = k;
		this.data = null;
		this.centroids = new ArrayList<float[]>();
		this.topIDs = new ArrayList<Long>();
		this.dimparameter = DEFAULT_DIM_PARAMETER;
		this.clusterer = DEFAULT_OFFLINE_CLUSTERER;
		// dec = new MultiDecoder(
		// getInnerDecoderMultiplier()*inner.getDimensionality(), inner);
	}

	@Override
	public void reset() {

		this.centroids = null;
		try {
			if (filereader) {
				inputStream.close();
				if (this.f.endsWith("gz"))
					inputStream = new GZIPInputStream(new FileInputStream(
							this.f));
				else
					inputStream = new FileInputStream(this.f);

				if (!raw) {
					assin = new BufferedReader(new InputStreamReader(
							inputStream));
					Integer.parseInt(assin.readLine());
					dim = Integer.parseInt(assin.readLine());
				} else {
					binin = new DataInputStream(new BufferedInputStream(
							inputStream));
					binin.readInt();
					dim = binin.readInt();

				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
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
	public Iterator<float[]> getVectorIterator() {
		return this;
	}

	@Override
	public List<float[]> getCentroids() {
		return centroids;
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
		hashmod = (int) parseLong;
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
	public void setDecoderType(Decoder dec) {
		this.dec = dec;
	}

	@Override
	public String toString() {
		String ret = "Decoder:";
		if (dec != null)
			ret += dec.getClass().getName();
		ret += ", Blur:" + numBlur;
		ret += ", Projections:" + numProjections;
		ret += ", Outer Decoder Multiplier:" + decoderMultiplier;
		ret += ", Offline Clusterer:" + clusterer.getClass().getName();
		return ret;
	}

	@Override
	public Decoder getDecoderType() {
		return dec;
	}

	@Override
	public void setNumBlur(int parseInt) {
		this.numBlur = parseInt;
	}

	@Override
	public void setRandomSeed(long parseLong) {
		randomSeed = parseLong;
	}

	@Override
	public boolean hasNext() {
		try {
			return inputStream.available() > 0;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	@Override
	public float[] next() {
		float[] readFloat = new float[dim];
		try {

			if (!raw) {
				for (int i = 0; i < dim; i++)
					readFloat[i] = Float.parseFloat(assin.readLine());
			} else {
				for (int i = 0; i < dim; i++)
					readFloat[i] = binin.readFloat();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return readFloat;
	}

	@Override
	public void setVariance(List<float[]> data) {
		dec.setVariance(StatTests.varianceSample(data, .01f));
	}

	public void setDecayRate(float parseFloat) {
		this.decayrate = parseFloat;
	}
	
	public float getDecayRate(){
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

	@Override
	public List<float[]> getData() {
		return this.data;
	}
}

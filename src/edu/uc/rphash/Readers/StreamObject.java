package edu.uc.rphash.Readers;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import edu.uc.rphash.decoders.Decoder;
import edu.uc.rphash.decoders.Leech;
import edu.uc.rphash.decoders.MultiDecoder;
import edu.uc.rphash.decoders.Spherical;

public class StreamObject implements RPHashObject, Iterator<float[]> {
	public List<float[]> data;
	// List<List<Float>> Xlist;

	int numProjections;
	int decoderMultiplier;
	long randomSeed;

	int numBlur;

	InputStream elements;
	int k;
	int n;
	int dim;
	int randomseed;
	int hashmod;
	List<float[]> centroids;
	List<Long> topIDs;
	int multiDim;
	Decoder dec;

	ExecutorService executor;
	final PipedInputStream inputStream;

	// input format
	// per line
	// top ids list (integers)
	// --num of clusters ( == k)
	// --num of data( == n)
	// --num dimensions
	// --input random seed;
	public StreamObject(PipedOutputStream istream, int k, int dim,
			 ExecutorService executor)
			throws IOException {

		inputStream = new PipedInputStream(istream);
		this.executor = executor;

		this.k = k;
		this.dim = dim;
		this.decoderMultiplier = 3;
		//Decoder inner = new Leech();
		//this.dec = new MultiDecoder(decoderMultiplier, inner);
		this.numProjections = 3;
		this.hashmod = Integer.MAX_VALUE;
		this.randomseed = 0;
		this.numBlur = 1;

		this.centroids = new ArrayList<float[]>();
		this.topIDs = new ArrayList<Long>();
		dec = new Spherical(64, 6, 4);
		//dec = new MultiDecoder( getInnerDecoderMultiplier()*inner.getDimensionality(), inner);


	}


	@Override
	public void reset() {

		this.centroids = null;
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
		return true;
	}

	@Override
	public float[] next() {
		final DataInputStream d = new DataInputStream(inputStream);
		float[] readFloat;
		// Read data with timeout
		Callable<float[]> readTask = new Callable<float[]>() {
			@Override
			public float[] call() {
				float[] vec = new float[dim];
				try {
					for (int i = 0; i < dim; i++) {
						vec[i] = d.readFloat();
					}
					return vec;
				} catch (IOException e) {
					return null;
				}
			}
		};

		Future<float[]> future = executor.submit(readTask);
		try {
			readFloat = future.get(5000, TimeUnit.MILLISECONDS);
			if (readFloat != null) {
				return readFloat;
			}
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}


}

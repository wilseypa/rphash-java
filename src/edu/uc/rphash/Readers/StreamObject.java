package edu.uc.rphash.Readers;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import edu.uc.rphash.RPHashStream;
import edu.uc.rphash.decoders.Decoder;
import edu.uc.rphash.decoders.Leech;
import edu.uc.rphash.decoders.MultiDecoder;
import edu.uc.rphash.tests.TestUtil;

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

	// input format
	// per line
	// top ids list (integers)
	// --num of clusters ( == k)
	// --num of data( == n)
	// --num dimensions
	// --input random seed;
	StreamObject(InputStream elements, int k, int dim) {

		this.elements = elements;
		this.k = k;
		this.dim = dim;
		Decoder inner = new Leech();
		this.decoderMultiplier = 3;
		this.numProjections = 3;
		this.hashmod = Integer.MAX_VALUE;
		this.randomseed = 0;
		this.dec = new MultiDecoder(decoderMultiplier, inner);
		this.numBlur = 0;

		this.centroids = new ArrayList<float[]>();
		this.topIDs = new ArrayList<Long>();
		try {
			System.out.println(Integer.parseInt(spacetoken()));// for real
																// streams these
																// wont be here
			System.out.println(Integer.parseInt(spacetoken()));// for real
																// streams these
																// wont be here
			// k = Integer.parseInt(spacetoken());
			// n = Integer.parseInt(spacetoken());
			// dim = Integer.parseInt(spacetoken());
			// randomseed = Integer.parseInt(spacetoken());
			// hashmod = Integer.parseInt(spacetoken());
			// times = Integer.parseInt(spacetoken());
		} catch (IOException e) {
			System.err.println("Couldn't Read Datastream");
		} catch (NumberFormatException pe) {
			System.err.println("Couldn't Parse Stream Number Format Error ");
		}
		dec = null;

	}

	/**
	 * Read as much data as the program wants, and as is available.
	 * 
	 * @return
	 * @throws IOException
	 */
	char blockingRead() throws IOException {
		int b = elements.read();

		while (b == -1) {
			// wait for new input
			try {
				Thread.sleep(1000);// waiting for new data
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			System.out.println("ended stream" + String.valueOf(b));
			b = elements.read();

		}
		return (char) b;

	}

	/**
	 * read until a space, for constant streams
	 * 
	 * @return
	 * @throws IOException
	 */
	String spacetoken() throws IOException {
		StringBuilder sb = new StringBuilder();
		char b = blockingRead();

		while (b != '\n' && b != ' ') {
			sb.append(b);
			b = blockingRead();
		}
		return sb.toString();
	}

	String getNext() throws IOException {
		return spacetoken();
	}

	float getNextFloat() {
		try {
			return Float.parseFloat(spacetoken());
		} catch (IOException e) {
			System.err.println("Couldn't Read Datastream");
		} catch (NumberFormatException pe) {
			System.err.println("Couldn't Parse Stream Number Format Error ");
		}
		return 0.0f;
	}

	int vectorct = 0;

	public synchronized float[] getNextVector() {
		float[] data = new float[dim];
		int i = 0;
		try {
			while (i < dim)
				data[i++] = Float.parseFloat(spacetoken());
		} catch (IOException e) {
			System.err.println("Couldn't Read Datastream");
		} catch (NumberFormatException pe) {
			System.err.println("Couldn't Parse Stream Number Format Error ");
		}
		return data;
	}

	@Override
	public void reset() {

		this.centroids = null;
		// try {
		// elements.reset();
		// } catch (IOException ioe) {
		// ioe.printStackTrace();
		// }
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
		return getNextVector();
	}

	public static void main(String[] args) throws FileNotFoundException,
			InterruptedException {
		InputStream in = new FileInputStream(new File(args[2]));
		System.out.println("opening stream");
		StreamObject o = new StreamObject(in, Integer.parseInt(args[0]),
				Integer.parseInt(args[1]));
		System.out.println("creating RPHash Object");
		RPHashStream strm = new RPHashStream(o);
		Thread procThread = new Thread(strm);


		class GenLoop implements Runnable {
			@Override
			public synchronized void run() {
				while (true) {
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					System.out.println("writing...");
					try {
						procThread.wait();
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					TestUtil.writeFile(
							new File("/home/lee/Desktop/streamedoutput.mat"),
							strm.getCentroids());
					this.notify();
				}
			}

		}
		
		Thread looper = new Thread(new GenLoop());
		procThread.start();
		looper.start();


	}

}

package edu.uc.rphash;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import edu.uc.rphash.Readers.RPHashObject;
import edu.uc.rphash.Readers.SimpleArrayReader;
import edu.uc.rphash.decoders.Decoder;
import edu.uc.rphash.decoders.Leech;
import edu.uc.rphash.decoders.MultiDecoder;
import edu.uc.rphash.decoders.Spherical;
import edu.uc.rphash.frequentItemSet.KHHCentroidCounter;
import edu.uc.rphash.frequentItemSet.KHHCountMinSketch;
//import edu.uc.rphash.frequentItemSet.KHHCountMinSketch.Tuple;
import edu.uc.rphash.lsh.LSH;
import edu.uc.rphash.projections.DBFriendlyProjection;
import edu.uc.rphash.projections.Projector;
import edu.uc.rphash.standardhash.HashAlgorithm;
import edu.uc.rphash.standardhash.MurmurHash;
import edu.uc.rphash.standardhash.NoHash;
import edu.uc.rphash.tests.GenerateData;
import edu.uc.rphash.tests.Kmeans;
import edu.uc.rphash.tests.StatTests;
import edu.uc.rphash.tests.TestUtil;

public class RPHashStream implements StreamClusterer {
	private float variance;
	public KHHCentroidCounter is;
	private LSH[] lshfuncs;
	private StatTests vartracker;
	private List<float[]> centroids = null;
	private RPHashObject so;

	@Override
	public synchronized int addVector(float[] vec) {
		long hash[];
		Centroid c = new Centroid(vec);
		
		
		float tmpvar = vartracker.updateVarianceSample(vec);
		if(variance!=tmpvar){
			for (LSH lshfunc : lshfuncs) 
				lshfunc.updateDecoderVariance(tmpvar);
			variance= tmpvar;
		}
		
		for (LSH lshfunc : lshfuncs) 
		{
			hash = lshfunc.lshHashRadiusNo2Hash(vec, so.getNumBlur());
			for (long h : hash)
				c.addID(h);
		}
		is.add(c);
		return (int) is.count;
	}

	public void init() {

		Random r = new Random(so.getRandomSeed());
		this.vartracker = new StatTests(.01);
		int projections = so.getNumProjections();
		int k = (int) (so.getk() * projections);

		// initialize our counter
		is = new KHHCentroidCounter(k);

		// create LSH Device
		lshfuncs = new LSH[projections];
		Decoder dec = so.getDecoderType();
		HashAlgorithm hal = new MurmurHash(so.getHashmod());

		// create projection matrices add to LSH Device
		for (int i = 0; i < projections; i++) {
			Projector p = new DBFriendlyProjection(so.getdim(),
					dec.getDimensionality(), r.nextLong());
			lshfuncs[i] = new LSH(dec, p, hal);
		}
	}

	public RPHashStream(List<float[]> data, int k) {

		variance = StatTests.varianceSample(data, .01f);
		so = new SimpleArrayReader(data, k);
		init();
		// so.setDecoderType(new Spherical(32,3,4));
		// so.setDecoderType(new Leech(variance));
		// so.getDecoderType().setVariance(variance);
	}

	public RPHashStream(RPHashObject so) {
		this.so = so;
		init();
	}

	public List<float[]> getCentroids(RPHashObject so) {
		this.so = so;
		init();
		if (centroids == null)
			run();
		centroids = new ArrayList<float[]>();
		for (Centroid c : is.getTop())
			centroids.add(c.centroid());
		return new Kmeans(so.getk(), centroids, is.getCounts()).getCentroids();
	}

	@Override
	public List<float[]> getCentroids() 
	{
		if (centroids == null){
			run();
			centroids = new ArrayList<float[]>();
			for (int i = 0; i < is.getTop().size(); i++)
				centroids.add(is.getTop().get(i).centroid());
			centroids = new Kmeans(so.getk(), centroids, is.getCounts()).getCentroids();
		}
		return centroids;
	}
	
	ArrayList<Long> counts;
	public List<float[]> getCentroidsOnline() 
	{
		
		if(centroids == null ){
			centroids = new ArrayList<float[]>();
			counts = new ArrayList<Long>();
		}
		
		for (int i = 0; i < is.getTop().size(); i++)
		{
			centroids.add(is.getTop().get(i).centroid());
		}
		//TODO reincorporate counts
		centroids = new Kmeans(so.getk(), centroids).getCentroids();
		
		return centroids;
	}

	public void run() {
		// add to frequent itemset the hashed Decoded randomly projected vector
		Iterator<float[]> vecs = so.getVectorIterator();
		while (vecs.hasNext()) {
			addVector(vecs.next());
		}
	}

	public List<Long> getTopIdSizes() {
		return is.getCounts();
	}

	public static void main(String[] args) {

		int k = 10;
		int d = 100;
		int n = 10000;
		float var = 1.1f;
		for (float f = var; f < 4.3; f += .2f) {
			for (int i = 0; i < 1; i++) {
				GenerateData gen = new GenerateData(k, n / k, d, f, true, 1f);
				// StreamingKmeans rphit = new StreamingKmeans(gen.data(), k);
				RPHashStream rphit = new RPHashStream(gen.data(), k);
				long startTime = System.nanoTime();
				rphit.getCentroids();
				long duration = (System.nanoTime() - startTime);
				List<float[]> aligned = TestUtil.alignCentroids(
						rphit.getCentroids(), gen.medoids());
				System.out.println(f + ":" + StatTests.PR(aligned, gen) + ":"
						+ StatTests.WCSSD(gen.medoids(), gen) + ":"
						+ StatTests.WCSSD(aligned, gen) + ":" + duration
						/ 1000000000f);
				System.gc();
			}
		}
	}

	@Override
	public RPHashObject getParam() {
		return so;
	}

}

package edu.uc.rphash;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import edu.uc.rphash.Readers.RPHashObject;
import edu.uc.rphash.Readers.SimpleArrayReader;
import edu.uc.rphash.decoders.Decoder;
import edu.uc.rphash.frequentItemSet.KHHCentroidCounterPush;
import edu.uc.rphash.knee.LpointKnee;
import edu.uc.rphash.lsh.LSH;
import edu.uc.rphash.projections.DBFriendlyProjection;
import edu.uc.rphash.projections.Projector;
import edu.uc.rphash.standardhash.HashAlgorithm;
import edu.uc.rphash.standardhash.MurmurHash;
import edu.uc.rphash.tests.StatTests;
import edu.uc.rphash.tests.clusterers.LloydIterativeKmeans;
import edu.uc.rphash.tests.generators.ClusterGenerator;

/**This is an adaptation of RPHash Streaming with support for 
 * automatic knee finding and time based cluster decay.
 * @author lee
 *
 */
public class RPHashStreamingAK implements StreamClusterer {

	public KHHCentroidCounterPush is;
	private LSH[] lshfuncs;
	private StatTests vartracker;
	private List<Centroid> centroids = null;
	private RPHashObject so;


	@Override
	public synchronized long addVectorOnlineStep(float[] vec) {

		if(!lshfuncs[0].lshDecoder.selfScaling()){
			this.vartracker.updateVarianceSampleVec(vec);
			vec = this.vartracker.scaleVector(vec);
		}
		
		
		Centroid c = new Centroid(vec,-1);
		int ret = -1;
		
		for (LSH lshfunc : lshfuncs) {
			if (so.getNumBlur() != 1) {
				long[] hash = lshfunc
						.lshHashRadius(vec, so.getNumBlur());
				for (long h : hash) {
					c.addID(h);
					is.addLong(h, 1);
				}
			} else {
				long hash = lshfunc.lshHash(vec);
				c.addID(hash);
				is.addLong(hash, 1);
			}
		}
		ret = is.addAndUpdate(c);

		return ret;
	}

	public void init() {
		Random r = new Random(so.getRandomSeed());
		this.vartracker = new StatTests(.01f);
		int projections = so.getNumProjections();

		// initialize our counter
		float decayrate = so.getDecayRate();// 1f;// bottom number is window
											// size
		is = new KHHCentroidCounterPush(decayrate,new LpointKnee());
		// create LSH Device
		lshfuncs = new LSH[projections];
		Decoder dec = so.getDecoderType();
		HashAlgorithm hal = new MurmurHash(so.getHashmod());
		// create projection matrices add to LSH Device
		for (int i = 0; i < projections; i++) {
			Projector p = new DBFriendlyProjection(so.getdim(),
					dec.getDimensionality(), r.nextLong());
			List<float[]> noise = LSH.genNoiseTable(dec.getDimensionality(),
					so.getNumBlur(), r, dec.getErrorRadius()
							/ dec.getDimensionality());
			lshfuncs[i] = new LSH(dec, p, hal, noise);
		}
	}

	public RPHashStreamingAK(ClusterGenerator c) {
		so = new SimpleArrayReader(c,0);
		init();
	}

	public RPHashStreamingAK(RPHashObject so) {
		this.so = so;
		init();
	}



	@Override
	public List<Centroid> getCentroids() {
		if (centroids == null) {
			init();
			run();
			getCentroidsOfflineStep();
		}
		return centroids;
	}

	public List<Centroid> getCentroidsOfflineStep() {
		
		centroids = is.getTop();
		
		
//		centroids = new ArrayList<Centroid>();
//		List<Float> counts = is.getCounts();
//
//		for (int i = 0; i < cents.size(); i++) {
//			centroids.add(cents.get(i).centroid());
//		}

		Clusterer offlineclusterer = so.getOfflineClusterer();
		offlineclusterer.setWeights(so.getCounts());
		offlineclusterer.setData(so.getCentroids());
		offlineclusterer.setK(so.getk());
		centroids = offlineclusterer.getCentroids();

		return centroids;
	}

	public void run() {
		// add to frequent itemset the hashed Decoded randomly projected
		// vector
		Iterator<float[]> vecs = so.getVectorIterator();
		while (vecs.hasNext()) {
				addVectorOnlineStep(vecs.next());
		}
	}

	public List<Float> getTopIdSizes() {
		return is.getCounts();
	}

	@Override
	public RPHashObject getParam() {
		return this.so;
	}

	@Override
	public void setWeights(List<Float> counts) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setRawData(List<float[]> data) {
//		this.data = data;
	}
	
	@Override
	public void setData(List<Centroid> centroids) {
		ArrayList<float[]> data = new ArrayList<float[]>(centroids.size());
		for(Centroid c : centroids)data.add(c.centroid());
		setRawData(data);	
	}

	@Override
	public void setK(int getk) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void reset(int randomseed) {
		centroids = null;
		so.setRandomSeed(randomseed);
	}

	@Override
	public boolean setMultiRun(int runs) {
		return false;
	}

}

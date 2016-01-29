package edu.uc.rphash;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import edu.uc.rphash.Readers.RPHashObject;
import edu.uc.rphash.Readers.SimpleArrayReader;
import edu.uc.rphash.decoders.Decoder;
import edu.uc.rphash.frequentItemSet.ItemSet;
import edu.uc.rphash.frequentItemSet.KHHCountMinSketch;
import edu.uc.rphash.frequentItemSet.SimpleFrequentItemSet;
import edu.uc.rphash.lsh.LSH;
import edu.uc.rphash.projections.DBFriendlyProjection;
import edu.uc.rphash.projections.Projector;
import edu.uc.rphash.standardhash.HashAlgorithm;
import edu.uc.rphash.standardhash.MurmurHash;
import edu.uc.rphash.standardhash.NoHash;
import edu.uc.rphash.tests.StatTests;
import edu.uc.rphash.tests.clusterers.Kmeans;
import edu.uc.rphash.tests.generators.GenerateData;
import edu.uc.rphash.tests.generators.GenerateStreamData;
import edu.uc.rphash.util.VectorUtil;

public class RPHashSimple implements Clusterer {
//	float variance;

	public RPHashObject map() {

		// create our LSH Machine
		HashAlgorithm hal = new NoHash(so.getHashmod());
		Iterator<float[]> vecs = so.getVectorIterator();
		if (!vecs.hasNext())
			return so;
		
		Decoder dec = so.getDecoderType();

		Projector p = new DBFriendlyProjection(so.getdim(),
				dec.getDimensionality(), so.getRandomSeed());
		//no noise to start with
		List<float[]> noise = LSH.genNoiseTable(dec.getDimensionality(),0, new Random(), dec.getErrorRadius()/(dec.getDimensionality()*dec.getDimensionality()));
		
		LSH lshfunc = new LSH(dec, p, hal,noise);
		long hash;
		int logk = (int) (.5+Math.log(so.getk())/Math.log(2));//log k and round to integer
		int k = so.getk()*logk;

		ItemSet<Long> is = new SimpleFrequentItemSet<Long>(k);
		// add to frequent itemset the hashed Decoded randomly projected vector

		while (vecs.hasNext()) {
			float[] vec = vecs.next();
			hash = lshfunc.lshHash(vec);
			is.add(hash);
			//vec.id.add(hash);
		}
		so.setPreviousTopID(is.getTop());	
		return so;
	}

	/*
	 * This is the second phase after the top ids have been in the reduce phase
	 * aggregated
	 */
	public RPHashObject reduce() {

		Iterator<float[]> vecs = so.getVectorIterator();
		if (!vecs.hasNext())
			return so;
		float[] vec = vecs.next();
		
		HashAlgorithm hal = new NoHash(so.getHashmod());
		Decoder dec = so.getDecoderType();
		
		Projector p = new DBFriendlyProjection(so.getdim(),
				dec.getDimensionality(), so.getRandomSeed());
		List<float[]> noise = LSH.genNoiseTable(so.getdim(),1, new Random(), dec.getErrorRadius()/(dec.getDimensionality()*dec.getDimensionality()));
		LSH lshfunc = new LSH(dec, p, hal,noise);
		long hash[];
		
		ArrayList<Centroid> centroids = new ArrayList<Centroid>();
		List<Float> counts = new ArrayList<Float>(centroids.size());
		HashMap<Long,Integer> countid = new HashMap<Long,Integer>(counts.size());
		int i = 0;
		for (long id : so.getPreviousTopID()){
			centroids.add(new Centroid(so.getdim(), id));
			counts.add(0f);
			countid.put(id, i++);
		}
		
		while (vecs.hasNext()) {	
			hash = lshfunc.lshHashRadius(vec,noise);
			for (Centroid cent : centroids){
				for(long h:hash){
					if(cent.ids.contains(h)){
						cent.updateVec(vec);
						int tmp = countid.get(cent.id);
						counts.set(tmp,counts.get(tmp)+1);
						break;
					}
				}
			}
			vec = vecs.next();
		}
		
		for (Centroid c: centroids) so.addCentroid(c.centroid());
		so.setCentroids(new Kmeans(so.getk(),so.getCentroids(),counts).getCentroids());
		return so;
	}

	private List<float[]> centroids = null;
	private RPHashObject so;

	public RPHashSimple(List<float[]> data, int k) {
//		variance = StatTests.varianceSample(data, .01f);
		so = new SimpleArrayReader(data, k);

	}

	public RPHashSimple(List<float[]> data, int k, int times, int rseed) {
//		variance = StatTests.varianceSample(data, .001f);
		so = new SimpleArrayReader(data, k);

	}

	public RPHashSimple(RPHashObject so) {
		this.so = so;
	}

	public List<float[]> getCentroids(RPHashObject so) {
		this.so=so;
		if (centroids == null)
			run();
		return centroids;
	}

	@Override
	public List<float[]> getCentroids() {
		if (centroids == null)
			run();
		
		return centroids;
	}

	private void run() {
		map();
		reduce();
		centroids = so.getCentroids();
	}

	public static void main(String[] args) {

		int k = 10;
		int d = 1000;
		int n = 10000;
		float var = 5.5f;
		for(float f = var;f<2*var;f+=.01f){
			for (int i = 0; i < 5; i++) {
				GenerateStreamData gen = new GenerateStreamData(k, d, var, 11331313,true);
				ArrayList<float[]> t = new ArrayList<float[]>();
				
				for(int j =0;j<n;j++){
					t.add(gen.generateNext());
				}
				
				RPHashSimple rphit = new RPHashSimple(t, k);
				long startTime = System.nanoTime();
				rphit.getCentroids();
				long duration = (System.nanoTime() - startTime);
				List<float[]> aligned = VectorUtil.alignCentroids(
						rphit.getCentroids(), gen.getMedoids());
				System.out.println(f+":"+StatTests.PR(aligned, gen.getLabels(),t) + ":" + duration
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

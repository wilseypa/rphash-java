package edu.uc.rphash;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import edu.uc.rphash.Readers.RPHashObject;
import edu.uc.rphash.Readers.SimpleArrayReader;
import edu.uc.rphash.decoders.Decoder;
import edu.uc.rphash.frequentItemSet.ItemSet;
import edu.uc.rphash.frequentItemSet.SimpleFrequentItemSet;
import edu.uc.rphash.lsh.LSH;
import edu.uc.rphash.projections.DBFriendlyProjection;
import edu.uc.rphash.projections.Projector;
import edu.uc.rphash.standardhash.HashAlgorithm;
import edu.uc.rphash.standardhash.NoHash;
import edu.uc.rphash.tests.StatTests;
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
		List<float[]> noise = LSH.genNoiseTable(dec.getDimensionality(),so.getNumBlur(), new Random(), dec.getErrorRadius()/(dec.getDimensionality()*dec.getDimensionality()));
		
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
		
		List<Float> countsAsFloats = new ArrayList<Float>();
		for(long ct: is.getCounts())countsAsFloats.add((float) ct);
		so.setCounts(countsAsFloats);
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
		List<float[]> noise = LSH.genNoiseTable(so.getdim(),so.getNumBlur(), new Random(), dec.getErrorRadius()/(dec.getDimensionality()*dec.getDimensionality()));
		LSH lshfunc = new LSH(dec, p, hal,noise);
		long hash[];
		
		List<Centroid> centroids = new ArrayList<Centroid>();

		for (long id : so.getPreviousTopID()){
			centroids.add(new Centroid(so.getdim(), id,-1));

		}
		
		while (vecs.hasNext()) {	
			hash = lshfunc.lshHashRadius(vec,noise);
			for (Centroid cent : centroids){
				for(long h:hash){
					if(cent.ids.contains(h)){
						cent.updateVec(vec);
					}
				}
			}
			vec = vecs.next();
		}

		
		for (Centroid c: centroids) so.addCentroid(c);
		
		
		Clusterer offlineclusterer = so.getOfflineClusterer();
		offlineclusterer.setWeights(so.getCounts());
		offlineclusterer.setData(so.getCentroids());
		offlineclusterer.setK(so.getk());
		this.centroids = offlineclusterer.getCentroids();
		so.setCentroids(centroids);
		
		
		return so;
	}

	private List<Centroid> centroids = null;
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

	public List<Centroid> getCentroids(RPHashObject so) {
		this.so=so;
		if (centroids == null)
			run();
		return centroids;
	}

	@Override
	public List<Centroid> getCentroids() {
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
		float var = 3.5f;
		for(float f = var;f<2*var;f+=.01f){
			for (int i = 0; i < 5; i++) {
				GenerateStreamData gen = new GenerateStreamData(k, d, var, 11331313,true);
				GenerateStreamData noise = new GenerateStreamData(1, d, var*10, 11331313);
				ArrayList<float[]> t = new ArrayList<float[]>();
				
				for(int j =0;j<n;j++){
					t.add(gen.generateNext());
					t.add(noise.generateNext());
				}
				
				RPHashSimple rphit = new RPHashSimple(t, k);
				long startTime = System.nanoTime();
				rphit.getCentroids();
				long duration = (System.nanoTime() - startTime);
				
//				List<float[]> aligned =  VectorUtil.alignCentroids(
//						rphit.getCentroids(), gen.getMedoids());
				
				ArrayList<float[]> tNoiseRemoved = new ArrayList<float[]>();
				for(int b =0;b<t.size();b++){
					if(b%2==0)tNoiseRemoved.add(t.get(b));
				}
					
				
				
				
//				System.out.println(f+":"+StatTests.PR(aligned, gen.getLabels(),tNoiseRemoved) + ":" + duration
//						/ 1000000000f);
				System.gc();
			}
		}

	}

	@Override
	public RPHashObject getParam() {
		return so;
	}

	@Override
	public void setWeights(List<Float> counts) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setData(List<Centroid> centroids) {
		this.centroids = centroids;
		
	}
	@Override
	public void setRawData(List<float[]> centroids) {
		if(this.centroids == null) this.centroids = new ArrayList<>(centroids.size());
		for(float[] f: centroids){
			this.centroids.add(new Centroid(f,0));
		}
	}

	@Override
	public void setK(int getk) {
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

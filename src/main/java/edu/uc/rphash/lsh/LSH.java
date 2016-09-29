package edu.uc.rphash.lsh;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import edu.uc.rphash.decoders.Decoder;
import edu.uc.rphash.decoders.Leech;
import edu.uc.rphash.projections.DBFriendlyProjection;
import edu.uc.rphash.projections.Projector;
import edu.uc.rphash.standardhash.HashAlgorithm;
import edu.uc.rphash.standardhash.MurmurHash;
import edu.uc.rphash.util.SamplingVarianceTracker;

public class LSH {
	public Projector projectionMatrix;
	HashAlgorithm standardHashAlgorithm;
	public Decoder lshDecoder;
	SamplingVarianceTracker vtrack;
//	int times;
	Random rand;
	float radius;
	float distance = 0.0f;

	List<float[]> noise;

	public LSH(Decoder dec, Projector p, HashAlgorithm hal, List<float[]> noise) {
		this.projectionMatrix = p;// new Projector[1];
		// this.projectionMatrices[0] = p;
		this.standardHashAlgorithm = hal;
		this.lshDecoder = dec;
//		this.times = 1;
		rand = new Random();
		radius = dec.getErrorRadius() / dec.getDimensionality();
		this.noise = noise;
		if(!dec.selfScaling())vtrack = new SamplingVarianceTracker();
	}
	
	public LSH(int dim, long randseed) {
		this.projectionMatrix = new DBFriendlyProjection(dim, 24, randseed);
		this.standardHashAlgorithm = new MurmurHash(100000);
		this.lshDecoder = new Leech();
		rand = new Random();
		this.noise = new ArrayList<>();//size=0 no noise
	}

	/*
	 * Decode full n length vector. Concatenate codes and run universal
	 * hash(fnv,elf, murmur) on whole vector decoding.
	 */
	public long lshHash(float[] r) {
		if(vtrack!=null){
			float newvar = vtrack.updateVarianceSample(r);
			if(newvar!=lshDecoder.getVariance())lshDecoder.setVariance(newvar);
		}
		
		long l = standardHashAlgorithm.hash(lshDecoder.decode(projectionMatrix
				.project(r)));
		return l;

	}
	
	public long lshHashAlreadyProjected(float[] r) {
		if(vtrack!=null){
			float newvar = vtrack.updateVarianceSample(r);
			if(newvar!=lshDecoder.getVariance())lshDecoder.setVariance(newvar);
		}
		long l = standardHashAlgorithm.hash(lshDecoder.decode(r));
		return l;
	}

	public float distance() {
		return distance;
	}

//	public void updateDecoderVariance(float variance) {
//		lshDecoder.setVariance(variance);
//	}

	// public long[] lshHashRadius(float[] r,float radius,int times){
	//
	//
	// float[] pr_r = p[0].project(r);
	//
	// int[] b = new int[pr_r.length/3];
	//
	// long[] ret = new long[times];
	// ret[0] = hal.hash(dec.decode(pr_r));
	// //long minret = ret;
	// //float mindist = dec.getDistance();
	// float[] rtmp = new float[pr_r.length];
	// for(int j =1;j<times;j++)
	// {
	// System.arraycopy(pr_r, 0, rtmp, 0, pr_r.length);
	// for(int k =0;k<pr_r.length/3;k++)b[k] = rand.nextInt(pr_r.length);
	// int k =0;
	// for(;k<pr_r.length/6;k++)
	// rtmp[b[k]]= rtmp[b[k]] + (radius/(float)j);
	// for(;k<pr_r.length/3;k++)
	// rtmp[b[k]]= rtmp[b[k]] - (radius/(float)j);
	// ret[j] = hal.hash(dec.decode(rtmp));
	// }
	// // distance = mindist;
	// return ret;
	// }

	public static List<float[]> genNoiseTable(int len, int times, Random rand,
			float radius) {
		ArrayList<float[]> noise = new ArrayList<float[]>();
		for (int j = 0; j < times; j++) {
			float[] tmp = new float[len];
			for (int k = 0; k < len; k++)
				tmp[k] = (float) rand.nextGaussian() * (radius);
			noise.add(tmp);
		}
		
		//add non-noised vector
		float[] tmp = new float[len];
		for (int k = 0; k < len; k++)tmp[k] = (float)1f;
		noise.add(tmp);
		
		return noise;
	}

	/**
	 * forgo 2ndary hash function
	 * 
	 * @param r
	 * @param times
	 * @return
	 */
	public long[] lshHashRadius(float[] r, int times) {


		
		float[] pr_r = projectionMatrix.project(r);
		if(vtrack!=null){
			float newvar = vtrack.updateVarianceSample(pr_r);
			if(newvar!=lshDecoder.getVariance())lshDecoder.setVariance(newvar);
		}
		
		long nonoise = standardHashAlgorithm.hash(lshDecoder.decode(pr_r));
		
		long[] returnHashes = new long[times];
		// if(times>1){
		returnHashes[0] = nonoise;
//		System.arraycopy(nonoise, 0, returnHashes, 0, nonoise.length);
		// add some blurring probes in addition to the unnoised decoding
		float[] noisedProjectedVector = new float[pr_r.length];
		float[] noiseVec;


		for (int j = 1; j < times; j++) {
			System.arraycopy(pr_r, 0, noisedProjectedVector, 0, pr_r.length);
			noiseVec = noise.get(j);
			for (int k = 0; k < pr_r.length; k++) {
				noisedProjectedVector[k] = noisedProjectedVector[k]
						+ noiseVec[k];
			}
//			nonoise = standardHashAlgorithm.hash(lshDecoder.decode(noisedProjectedVector));
			returnHashes[j] = standardHashAlgorithm.hash(lshDecoder.decode(noisedProjectedVector));
//			System.arraycopy(nonoise, 0, returnHashes, j * nonoise.length,
//					nonoise.length);
		}

		return returnHashes;
	}

	public long lshMinHashRadius(float[] r, float radius, int times) {

		float[] pr_r = projectionMatrix.project(r);
		if(vtrack!=null){
			float newvar = vtrack.updateVarianceSample(pr_r);
			if(newvar!=lshDecoder.getVariance())lshDecoder.setVariance(newvar);
		}
		long ret = standardHashAlgorithm.hash(lshDecoder.decode(pr_r));
		long minret = ret;
		float mindist = lshDecoder.getDistance();
		float[] rtmp = new float[pr_r.length];
		for (int j = 0; j < times; j++) {
			System.arraycopy(pr_r, 0, rtmp, 0, pr_r.length);
			for (int k = 0; k < pr_r.length; k++)
				rtmp[k] = rtmp[k] + (float) rand.nextGaussian() * (radius);
			ret = standardHashAlgorithm.hash(lshDecoder.decode(rtmp));
			if (lshDecoder.getDistance() < mindist) {
				minret = standardHashAlgorithm.hash(lshDecoder.decode(rtmp));
				mindist = lshDecoder.getDistance();
			}
		}
		return minret;
	}

	public long lshMinHashRadius(float[] r, int times) {

		return lshMinHashRadius(r, radius, times);
	}

	public long[] lshHashRadius(float[] vec,List<float[]> noise) {
		long[] ret = new long[noise.size()];
		
		float[] pr_r = projectionMatrix.project(vec);

		if(vtrack!=null){
			float newvar = vtrack.updateVarianceSample(pr_r);
			if(newvar!=lshDecoder.getVariance())lshDecoder.setVariance(newvar);
		}
		
		float[] veccopy = new float[pr_r.length];

		for(int i = 0; i< noise.size();i++)
		{
			System.arraycopy(pr_r, 0, veccopy, 0, pr_r.length);
			for(int j = 0;j<pr_r.length;j++)veccopy[j]+=noise.get(i)[j];
			ret[i] = lshHashAlreadyProjected(veccopy);
		}
		return ret;
	}

	// Query adaptive (c,r)-NN similar to query adaptive lsh ipdps-
	// TODO finish above
	// public long[] lshHashRadius(float[] r,float radius,int times){
	//
	// float[] pr_r = p[0].project(r);
	// long[] dectmp = dec.decode(pr_r);
	// long[] ret = new long[times*dectmp.length];
	// for(int i = 0 ; i< dectmp.length;i++)ret[i]=dectmp[i];
	// //long minret = ret;
	// //float mindist = dec.getDistance();
	//
	// float[] rtmp = new float[pr_r.length];
	// for(int j =1;j<times;j++)
	// {
	// System.arraycopy(pr_r, 0, rtmp, 0, pr_r.length);
	// for(int k =0;k<pr_r.length;k++)rtmp[k]=
	// rtmp[k]+(float)rand.nextGaussian()*(radius/2f);
	//
	// dectmp = dec.decode(rtmp);
	// for(int i = 0 ; i< dectmp.length;i++)ret[j*dectmp.length+i]=dectmp[i];
	//
	// }
	// return ret;
	// }

	// public long[] lshHashRadius(float[] r,int times)
	// {
	// return lshHashRadius(r,radius,times) ;
	// }
}

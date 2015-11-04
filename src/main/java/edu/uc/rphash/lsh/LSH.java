package edu.uc.rphash.lsh;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import edu.uc.rphash.decoders.Decoder;
import edu.uc.rphash.projections.Projector;
import edu.uc.rphash.standardhash.HashAlgorithm;
import edu.uc.rphash.tests.TestUtil;

public class LSH {
	Projector[] p;
	HashAlgorithm hal;
	Decoder dec;
	int times;
	Random rand;
	float radius;
	float distance = 0.0f;

	List<float[]> noise;

	public LSH(Decoder dec, Projector p, HashAlgorithm hal, List<float[]> noise) {
		this.p = new Projector[1];
		this.p[0] = p;
		this.hal = hal;
		this.dec = dec;
		this.times = 1;
		rand = new Random();
		radius = dec.getErrorRadius() / dec.getDimensionality();
		this.noise = noise;
	}

	/*
	 * Decode full n length vector. Concatenate codes and run universal
	 * hash(fnv,elf, murmur) on whole vector decoding.
	 */
	public long lshHash(float[] r) {

		return hal.hash(dec.decode(p[0].project(r)));

	}

	public float distance() {
		return distance;
	}

	public void updateDecoderVariance(float variance) {
		dec.setVariance(variance);
	}

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
		return noise;
	}

	/**
	 * forgo 2ndary hash function
	 * 
	 * @param r
	 * @param times
	 * @return
	 */
	public long[] lshHashRadiusNo2Hash(float[] r, int times) {

		float[] pr_r = p[0].project(r);
		long[] nonoise = dec.decode(pr_r);
		long[] ret = new long[times * nonoise.length];

		System.arraycopy(nonoise, 0, ret, 0, nonoise.length);

		// add some blurring probes in addition to the unnoised decoding
		float[] rtmp = new float[pr_r.length];
		float[] tmp;
		for (int j = 1; j < times; j++) {
			System.arraycopy(pr_r, 0, rtmp, 0, pr_r.length);

			tmp = noise.get(j - 1);
			for (int k = 0; k < pr_r.length; k++) {
				rtmp[k] = rtmp[k] + tmp[k];
			}

			nonoise = dec.decode(rtmp);
			System.arraycopy(nonoise, 0, ret, j * nonoise.length,
					nonoise.length);
		}
		return ret;
	}

	public long lshMinHashRadius(float[] r, float radius, int times) {

		float[] pr_r = p[0].project(r);
		long ret = hal.hash(dec.decode(pr_r));
		long minret = ret;
		float mindist = dec.getDistance();
		float[] rtmp = new float[pr_r.length];
		for (int j = 1; j < times; j++) {
			System.arraycopy(pr_r, 0, rtmp, 0, pr_r.length);
			for (int k = 0; k < pr_r.length; k++)
				rtmp[k] = rtmp[k] + (float) rand.nextGaussian() * (radius);
			ret = hal.hash(dec.decode(rtmp));
			if (dec.getDistance() < mindist) {
				minret = hal.hash(dec.decode(rtmp));
				mindist = dec.getDistance();
			}
		}
		return ret;
	}

	public long lshMinHashRadius(float[] r, int times) {

		return lshMinHashRadius(r, radius, times);
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

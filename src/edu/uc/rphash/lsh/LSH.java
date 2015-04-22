package edu.uc.rphash.lsh;

import java.util.Arrays;
import java.util.Random;

import edu.uc.rphash.decoders.Decoder;
import edu.uc.rphash.projections.Projector;
import edu.uc.rphash.standardhash.HashAlgorithm;
import edu.uc.rphash.tests.TestUtil;

public class LSH 
{
	Projector[] p;
	HashAlgorithm hal;
	Decoder dec;
	int times;
	Random rand ;
	float radius;
	float distance = 0.0f;
	public LSH(Decoder dec,Projector[] p, HashAlgorithm hal,int times)
	{
		this.p = p;
		this.hal = hal;
		this.dec = dec;
		this.times = times;
		rand = new Random();
		radius = dec.getErrorRadius();
	}

	public LSH(Decoder dec, Projector p, HashAlgorithm hal) {
		this.p = new Projector[1];
		this.p[0] = p;
		this.hal = hal;
		this.dec = dec;
		this.times = 1;
		rand = new Random();
		radius = dec.getErrorRadius()/dec.getDimensionality();
	}

	/*
	 * Decode full n length vector. Concatenate codes and run universal hash(fnv,elf, murmur) on whole vector decoding.
	 */
	public long lshHash(float[] r)
	{	 
	  
	  return hal.hash(dec.decode(p[0].project(r)));
	  
	}
	
	public float distance(){
		return distance;
	}
	
//	public long lshHash(float[] r,float[] perm){	 
//	  float[] permvec = new float[r.length];	
//	  for(int i =0;i<r.length;i++)permvec[i] = perm[i]+r[i];
//	  return lshHash(permvec) ;
//	}

	public long[] lshHashRadius(float[] r,float radius,int times){

	     float[] pr_r = p[0].project(r);
	     long[] ret = new long[times];
	     ret[0] = hal.hash(dec.decode(pr_r));
	     //long minret = ret;
	     //float mindist = dec.getDistance();
	     float[] rtmp = new float[pr_r.length];
		 for(int j =1;j<times;j++)
		 {
	    	 System.arraycopy(pr_r, 0, rtmp, 0, pr_r.length);
			 for(int k =0;k<pr_r.length;k++)rtmp[k]= rtmp[k]+ (float)rand.nextGaussian()*(radius/dec.getDimensionality());
			 ret[j] = hal.hash(dec.decode(rtmp));
//			 if(dec.getDistance()<mindist){
//				 minret = hal.hash(dec.decode(rtmp));
//				 mindist = dec.getDistance();
//			 }
	     }
//		 distance = mindist;
		 return ret; 
		}

	public long[] lshHashRadius(float[] r,int times)
	{
		  return lshHashRadius(r,radius,times) ;
	}
}

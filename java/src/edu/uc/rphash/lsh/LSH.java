package edu.uc.rphash.lsh;

import java.util.Random;

import edu.uc.rphash.decoders.Decoder;
import edu.uc.rphash.projections.Projector;
import edu.uc.rphash.standardhash.HashAlgorithm;

public class LSH 
{
	Projector[] p;
	HashAlgorithm hal;
	Decoder dec;
	int times;
	Random rand ;
	float radius;
	public LSH(Decoder dec,Projector[] p, HashAlgorithm hal,int times)
	{
		this.p = p;
		this.hal = hal;
		this.dec = dec;
		this.times = times;
		rand = new Random();
		radius = dec.getErrorRadius();
	}

	/*
	 * Decode full n length vector. Concatenate codes and run universal hash(fnv,elf, murmur) on whole vector decoding.
	 */
	public long lshHash(float[] r){	 
	     int k=0;
	     long ret = 0;
	     do{
	         float[] r1 = p[k].project(r);	         
	         ret =  hal.hash(dec.decode(r1)) ^ ret;
	         k++;
	     }while(k<times);
	  return ret ;
	}
	

	
	public long lshHash(float[] r,float[] perm){	 
	  float[] permvec = new float[r.length];	
	  for(int i =0;i<r.length;i++)permvec[i] = perm[i]+r[i];
	  return lshHash(permvec) ;
	}

	public long lshHashRadius(float[] r,float radius){
	     int k = 0;
	     long ret = 0;
		 do{
		     float[] r1 = p[k].project(r);	
		     for(int i =0;i<dec.getDimensionality();i++)
		    	 r1[i] += (float)rand.nextGaussian()*radius;
		     ret =  hal.hash(dec.decode(r1)) ^ ret;
	         k++;
	     }while(k<times);
	     
		 return ret; 
		}

	public long lshHashRadius(float[] r){
		  return lshHashRadius(r,radius) ;
	}

}

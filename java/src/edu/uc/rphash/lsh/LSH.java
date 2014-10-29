package edu.uc.rphash.lsh;

import java.util.Random;

import edu.uc.rphash.decoders.Decoder;
import edu.uc.rphash.projections.Projector;
import edu.uc.rphash.standardhash.HashAlgorithm;

public class LSH 
{
	Projector p;
	HashAlgorithm hal;
	Decoder dec;
	int times;
	Random rand ;
	public LSH(Decoder dec,Projector p, HashAlgorithm hal)
	{
		this.p = p;
		this.hal = hal;
		this.dec = dec;
		this.times = 1;
		rand = new Random();
	}

	/*
	 * Decode full n length vector. Concatenate codes and run universal hash(fnv,elf, murmur) on whole vector decoding.
	 */
	public long lshHash(float[] r){	 
	     int k=0;
	     long ret = 0;
	     do{
	         float[] r1 = p.project(r);	         
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
		 float[] permvec = new float[dec.getDimensionality()];
	     //long ret = 0;
	     float[] r1 = p.project(r);	
	     for(int i =0;i<dec.getDimensionality();i++)
	    	 permvec[i] = (float)rand.nextGaussian()*radius + r1[i];
	     
	     
		 return hal.hash(dec.decode(permvec)); 
		}
	
	public long lshHashRadius(float[] r){
		  return lshHashRadius(r,dec.getErrorRadius()) ;
	}

}

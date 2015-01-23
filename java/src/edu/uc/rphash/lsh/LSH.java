package edu.uc.rphash.lsh;

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
	public long lshHash(float[] r){	 
//	     int k=0;
//	     long ret = 0;
//	     do{
//	         float[] r1 = p[k].project(r);	  
//	         System.out.println(radius);
//	         TestUtil.prettyPrint(r1);
//	         ret =  hal.hash(dec.decode(r1)) ^ ret;
//	         k++;
//	     }while(k<times);
	  return hal.hash(dec.decode(p[0].project(r)));
	}
	

	
	public long lshHash(float[] r,float[] perm){	 
	  float[] permvec = new float[r.length];	
	  for(int i =0;i<r.length;i++)permvec[i] = perm[i]+r[i];
	  return lshHash(permvec) ;
	}

	public long[] lshHashRadius(float[] r,float radius,int times){
	     int k = 0;
	     long[] ret = new long[times];
	     float[] r1 = p[k].project(r);	
	     float[] r2 = new float[dec.getDimensionality()];

	     ret[0] =  hal.hash(dec.decode(r1)) ;

		 for(int j =1;j<times;j++){
		     for(int i =0;i<dec.getDimensionality();i++){
		    	 r2[i] = r1[i]+(float)rand.nextGaussian()*radius;
		     }
		     ret[j] =  hal.hash(dec.decode(r2)) ;
		     
	     }
		 return ret; 
		}

	public long[] lshHashRadius(float[] r,int times)
	{
		  return lshHashRadius(r,radius,times) ;
	}

}

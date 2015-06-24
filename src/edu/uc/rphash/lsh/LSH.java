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
		radius = dec.getErrorRadius()/dec.getDimensionality();
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
	
	public void updateDecoderVariance(float variance){
		dec.setVariance(variance);
	}

	
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
			 for(int k =0;k<pr_r.length;k++)rtmp[k]= rtmp[k]+ (float)rand.nextGaussian()*(radius/(float)j);
			 ret[j] = hal.hash(dec.decode(rtmp));
			 for(int k =0;k<pr_r.length;k++)rtmp[k]= rtmp[k]+ (float)rand.nextGaussian()*(radius*(float)j);
			 ret[j] = hal.hash(dec.decode(rtmp));

	     }
//		 distance = mindist;
		 return ret; 
		}
	
	public long[] lshHashRadiusNo2Hash(float[] r,int times){

	     float[] pr_r = p[0].project(r);
	     
	     long[] nonoise = dec.decode(pr_r);
	     long[] ret = new long[times*nonoise.length];
	     for(int k =0;k<nonoise.length;k++)ret[k]=nonoise[k];
	     
	     //long minret = ret;
	     //float mindist = dec.getDistance();
	     float[] rtmp = new float[pr_r.length];
		 for(int j =1;j<times;j++)
		 {
	    	 System.arraycopy(pr_r, 0, rtmp, 0, pr_r.length);
			 for(int k =0;k<pr_r.length;k++)rtmp[k]= rtmp[k]+ (float)rand.nextGaussian()*(radius);
			 nonoise = dec.decode(rtmp);
			 for(int k =0;k<nonoise.length;k++)ret[j*nonoise.length+k]=nonoise[k];
	     }
//		 distance = mindist;
		 return ret; 
		}
	
	public long lshMinHashRadius(float[] r,float radius,int times){

	     float[] pr_r = p[0].project(r);
	     long ret = hal.hash(dec.decode(pr_r));
	     long minret = ret;
	     float mindist = dec.getDistance();
	     float[] rtmp = new float[pr_r.length];
		 for(int j =1;j<times;j++)
		 {
	    	 System.arraycopy(pr_r, 0, rtmp, 0, pr_r.length);
			 for(int k =0;k<pr_r.length;k++)rtmp[k]= rtmp[k]+ (float)rand.nextGaussian()*(radius);
			 ret = hal.hash(dec.decode(rtmp));
			 if(dec.getDistance()<mindist){
				 minret = hal.hash(dec.decode(rtmp));
				 mindist = dec.getDistance();
			 }
	     }
		 return ret; 
		}
	
	public long lshMinHashRadius(float[] r,int times){

		 return lshMinHashRadius(r,radius,times); 
		}

//	public long[] lshHashRadius(float[] r,float radius,int times){
//
//	     float[] pr_r = p[0].project(r);
//	     long[] dectmp = dec.decode(pr_r);
//	     long[] ret = new long[times*dectmp.length];
//	     for(int i = 0 ; i< dectmp.length;i++)ret[i]=dectmp[i];
//	     //long minret = ret;
//	     //float mindist = dec.getDistance();
//	     
//	     float[] rtmp = new float[pr_r.length];
//		 for(int j =1;j<times;j++)
//		 {
//	    	 System.arraycopy(pr_r, 0, rtmp, 0, pr_r.length);
//			 for(int k =0;k<pr_r.length;k++)rtmp[k]= rtmp[k]+(float)rand.nextGaussian()*(radius/2f);
//			 
//			 dectmp = dec.decode(rtmp);
//			 for(int i = 0 ; i< dectmp.length;i++)ret[j*dectmp.length+i]=dectmp[i];
//			 
//	     }
//		 return ret; 
//		}

	public long[] lshHashRadius(float[] r,int times)
	{
		  return lshHashRadius(r,radius,times) ;
	}
}

package uc.edu.rphash.lsh;

import uc.edu.rphash.decoders.Decoder;
import uc.edu.rphash.projections.Projector;
import uc.edu.rphash.standardhash.HashAlgorithm;

public class LSH 
{
	Projector p;
	HashAlgorithm hal;
	Decoder dec;
	int times;
	public LSH(Projector p, HashAlgorithm hal, int tableLength, int times)
	{
		this.p = p;
		this.hal = hal;
		this.times = times;
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


    //sometimes the RP throws stuff out of the lattice
    //check min/max are near the interval [-1,1]
//    int d = 1;
//    float sump = r1[0];
//    float summ = r1[0];
//    float avg = 0.0;
//    for(;d<24;d++){
//        if(summ>r1[d])summ =r1[d] ;
//        if(sump<r1[d])sump =r1[d] ;
//        avg+=r1[d];
//    }
//    printf("proj %f, %f, %f\n",summ,avg/24.0,sump)//;

//    d = 1;
//    sump = r[0];
//    summ = r[0];
//    avg = 0.0;
//    for(;d<len;d++){
//        if(summ>r[d])summ =r[d] ;
//        if(sump<r[d])sump =r[d] ;
//        avg+=r[d];
//    }
//    printf("norm %f, %f, %f\n",summ,avg/len,sump);


	
	

}

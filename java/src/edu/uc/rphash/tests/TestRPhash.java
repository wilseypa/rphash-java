package edu.uc.rphash.tests;


import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.streaminer.stream.frequency.BaseFrequency;
import org.streaminer.stream.frequency.FrequencyException;
import org.streaminer.stream.frequency.LossyCounting;
import org.streaminer.stream.frequency.RealCounting;
import org.streaminer.stream.frequency.StickySampling;
import org.streaminer.stream.frequency.util.CountEntry;

import edu.uc.rphash.RPHash3Stage;
import edu.uc.rphash.RPHashIterativeRedux;
import edu.uc.rphash.Readers.RPHashObject;
import edu.uc.rphash.Readers.SimpleArrayReader;
import edu.uc.rphash.decoders.Decoder;
import edu.uc.rphash.decoders.LeechDecoder;
import edu.uc.rphash.projections.FJLTProjection;
import edu.uc.rphash.projections.GaussianProjection;
import edu.uc.rphash.projections.Projector;


//import edu.uc.rphash.frequentItemSet.KarpFrequentItemSet;
//import edu.uc.rphash.frequentItemSet.SimpleFrequentItemSet;

public class TestRPhash {
	
	
	static void testRPHash(int k, int n,int d,float variance,int projdim){
		
		GenerateData gen = new GenerateData(k,n/k,d,variance,true,.1f);
		
		System.out.print(k+":"+n+":"+d+":"+variance+":"+projdim+"\t");
		System.out.print(StatTests.PR(gen.medoids(),gen)+":\t");
		
		
		long startTime = System.nanoTime();
		List<float[]> M = ( new Kmeans(k,gen.data(),projdim)).getCentroids();
		long duration = (System.nanoTime() - startTime);

		List<float[]> aligned = TestUtil.alignCentroids(M,gen.medoids());
		System.out.print(StatTests.PR(aligned,gen)+":"+duration/1000000000f);
		System.out.print("\t");
		System.gc();
//		for(int i = 0 ; i< k;i++)
//			System.out.println(i+":"+TestUtil.distance(aligned.get(i), gen.medoids().get(i)));

		startTime = System.nanoTime();
		RPHashObject so = new SimpleArrayReader(gen.data(),k,1,250000,1);
		RPHash3Stage clusterer = new RPHash3Stage();
		so = clusterer.mapP1(so);
		so = clusterer.mapP2(so);
		so = clusterer.mapP3(so);
		duration = (System.nanoTime() - startTime);
		
		aligned  = TestUtil.alignCentroids(so.getCentroids(),gen.medoids());
		System.out.print(StatTests.PR(aligned,gen)+":"+duration/1000000000f);
////		for(int i = 0 ; i< k;i++)
////			System.out.println(i+":"+TestUtil.distance(aligned.get(i), gen.medoids().get(i)));
		System.out.print("\n");
		System.gc();
		
		
		
//		startTime = System.nanoTime();
//		SimpleArrayReader sar = new SimpleArrayReader(gen.data(),k,1,2500,1);
//		RPHashIterativeRedux rphit = new RPHashIterativeRedux();
//		for(int i = 0;i<k;i++)
//		{
//			sar = rphit.map(sar);
//			sar = rphit.reduce(sar);
//		}
//		duration = (System.nanoTime() - startTime);
//		aligned  = TestUtil.alignCentroids(so.getCentroids(),gen.medoids());
//		System.out.print(StatTests.PR(aligned,gen)+":"+duration/1000000000f);
//		System.out.print("\n");
//		System.gc();
		
		
		
		
		
		
		
	}
	static void clusterPerformanceTests()
	{
		int k = 30;
		int n = 1000;
		int d = 100;
		float v = .3f;
		int projdim = 24;
		
//		System.out.println("-------varying variance-------");
//		GenerateData gen = new GenerateData(k,n/k,d,2f);
//		System.out.println(StatTests.PR(gen.medoids(),gen));
//		gen = new GenerateData(k,n/k,d,4f);
//		System.out.println(StatTests.PR(gen.medoids(),gen));
//		gen = new GenerateData(k,n/k,d,6f);
//		System.out.println(StatTests.PR(gen.medoids(),gen));
//		gen = new GenerateData(k,n/k,d,8f);
//		System.out.println(StatTests.PR(gen.medoids(),gen));
//		gen = new GenerateData(k,n/k,d,10.0f);
//		System.out.println(StatTests.PR(gen.medoids(),gen));

//		System.out.println("-------varying dim-------");
//		for(int i = 24 ;i<100;i+=20){
//			testRPHash(k,n,d,1.0f,i);
//			//testRPHash(k,n,d,i/100f);
//			//testRPHash(k,n,d,i/100f);
//		}
		
		System.out.println("-------varying variance-------");
		for(int i = 10 ;i<200;i+=10){
			testRPHash(k,n,d,i/100f,24);
			testRPHash(k,n,d,i/100f,24);
			testRPHash(k,n,d,i/100f,24);
		}

//		System.out.println("-------varying k-------");
//		for(int i = 0 ;i<100;i+=2){
//			testRPHash(k+i,n,d,v,projdim);
//			testRPHash(k+i,n,d,v,projdim);
//			testRPHash(k+i,n,d,v,projdim);
//		}
//		System.out.println("-------varying n-------");
//		for(int i = 0 ;i<50;i+=2){
//			testRPHash(k,n+i*10000,d,v,projdim);
//			testRPHash(k,n+i*10000,d,v,projdim);
//			testRPHash(k,n+i*10000,d,v,projdim);
//		}
//		System.out.println("-------varying d-------");
//		for(int i = 5 ;i<31;i++){
//			testRPHash(k,n,d+i*500,v,projdim);
//			testRPHash(k,n,d+i*500,v,projdim);
//			testRPHash(k,n,d+i*500,v,projdim);
//		}
		
		
	}

	public static void main(String[] args){
		clusterPerformanceTests();
	   
	}
	

}

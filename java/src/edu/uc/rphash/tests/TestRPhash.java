package edu.uc.rphash.tests;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.streaminer.stream.frequency.BaseFrequency;
import org.streaminer.stream.frequency.FrequencyException;
import org.streaminer.stream.frequency.LossyCounting;
import org.streaminer.stream.frequency.RealCounting;
import org.streaminer.stream.frequency.SpaceSaving;
import org.streaminer.stream.frequency.StickySampling;
import org.streaminer.stream.frequency.util.CountEntry;

import edu.uc.rphash.RPHash;
import edu.uc.rphash.Readers.RPHashObject;
import edu.uc.rphash.Readers.SimpleArrayReader;
import edu.uc.rphash.decoders.Decoder;
import edu.uc.rphash.decoders.LeechDecoder;
import edu.uc.rphash.frequentItemSet.ItemSet;
import edu.uc.rphash.frequentItemSet.KarpFrequentItemSet;
import edu.uc.rphash.frequentItemSet.SimpleFrequentItemSet;

public class TestRPhash {
	
	static void testLeechDec(){

		
		
		Decoder leech = new LeechDecoder();
		double[] f = {-0.98796955, -1.1529434 ,  1.4390883 ,  1.40046597,  0.40183179,
				-0.56885575, -0.81956525,  1.25615557, -1.29526976, -0.62859484,
				-0.7260114 ,  1.19387512,  0.74441283, -0.31003198,  1.16529063,
				0.03210929,  0.88011717,  0.98265615,  1.93322648, -0.05865583,
				-0.56355944, -0.67748379,  0.03904684, -1.0102314};



			double[] u = {0.65126908,  0.10690608,  1.16313656,  0.22987196, -1.43084181,
			0.2755519 ,  0.46149737,  1.62229512,  1.84176411,  1.14668634,
			-0.57105783, -0.25542529, -0.30482256,  0.38044721,  1.03321804,
			-0.19284389,  1.07302753, -0.50554365,  1.09262201, -0.17338258,
			-1.12363241, -0.98586661, -0.01722098,  2.4740535};

			double[] c ={-1.25952848,  0.90974636,  1.18518797,  0.71649243,  0.00428519,
					0.40136433, -0.44116449,  0.78036808,  1.22932536, -0.4739356 ,
					1.26962219,  0.73379495, -0.37507624,  0.6359808 ,  0.04275665,
					-0.06981256, -2.22652298,  2.10441221,  1.13049073, -0.1140077 ,
					-0.88809368, -1.08038432,  0.73727081,  1.02316672};
			double[] k={-0.06629867,  1.64363637, -0.27086515, -0.37690182, -0.20278382,
					0.84133612, -0.78164611,  0.5310594 ,  0.25187642,  0.56032285,
					-0.43311799,  0.34899539,  1.61118461,  0.82464746, -1.91355652,
					0.48868273, -0.69186852, -0.07240643,  0.16149872,  0.28575778,
					0.13803191, -0.18731954, -0.5343032 ,  0.67212346};
			double[] y={-2.46778603,  1.02194656, -1.21799259,  0.27824392, -0.57911542,
					0.22832422, -1.75776838, -0.09309783,  0.75097076,  0.15962876,
					0.5119343 ,  0.37938917,  0.01796803,  1.03030119, -2.64303921,
					0.32328967,  1.37198716, -0.50753097,  0.47852208,  0.10388366,
					-0.74706363, -0.66855493, -0.35686416,  0.7092663};
			double[] o = {-0.84282073,  0.59923037,  0.73899297,  1.22811334, -0.36589193,
					-0.73147463,  0.31780028,  0.99248704, -0.41232863, -0.34636915,
					0.17348888, -0.93814914, -0.05100204,  1.1133043 , -0.48937103,
					0.28450671,  0.20654879, -2.08840144, -1.72441501, -0.66277794,
					-0.72239422, -0.44093551,  1.02989744,  1.28223695};

			System.out.println(leech.decode(cnv(f)));
			System.out.println(leech.decode(cnv(u)));
			System.out.println(leech.decode(cnv(c)));
			System.out.println(leech.decode(cnv(k)));
			System.out.println(leech.decode(cnv(y)));
			System.out.println(leech.decode(cnv(o)));
		
	}
	static public List<CountEntry<Long>> test(ArrayList<Integer> ar, BaseFrequency<Long> counter,int k)
	{
        long startTime = System.nanoTime();
		for(int i =0;i<ar.size();i++)
			try {
				counter.add((long)ar.get(i));
			} catch (FrequencyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        System.out.print(counter.getClass().getName()+":\t"+ String.valueOf(System.nanoTime() - startTime));
		return counter.peek(k);
	}
	
//	static public List<Long> test(ArrayList<Integer> ar, ItemSet<Long> counter,int k)
//	{
//        long startTime = System.nanoTime();
//		for(int i =0;i<ar.size();i++)
//				counter.add((long)ar.get(i));
//
//        System.out.println(ar.getClass().getName()+":\t" + String.valueOf(System.nanoTime() - startTime));
//		return counter.getTop();
//	}
	/*
	 * Sticky Counting seems to be the best
	 */
	static public void testFrequentItems(){
		Random r = new Random();
		KarpFrequentItemSet<Integer> karp = new KarpFrequentItemSet<Integer>((float)(1./500.0));
		SimpleFrequentItemSet<Integer> smpl = new SimpleFrequentItemSet<Integer>(20);
		int testsize = 10_000_000;
		int sets = 6;
		int numsetsperpartition = 10;
		int startval = 1000;
		
		/**START Make Test Data**/
		ArrayList<Integer> ar = new ArrayList<Integer>(testsize);
		for(int w = 0;w<sets;w++){
			for(int i =numsetsperpartition*w;i<numsetsperpartition*(w+1);i++)
			{
				for(int j = 0 ;j<startval;j++)ar.add(i);
			}
			startval/=2;
		}
		for(int i = ar.size();i<testsize;i++)ar.add(r.nextInt(testsize));
		Collections.shuffle(ar, r);
		/**END Make Test Data**/
		int k = 20;
		LossyCounting<Long> lcounter = new LossyCounting<Long>(.1);
		StickySampling<Long> scounter = new StickySampling<Long>(.01,.01,.001);
        //SpaceSaving<Integer> sscounter = new SpaceSaving<Integer>(1000, .01, .1);
        RealCounting<Long> rcounter = new RealCounting<Long>();
		
        final Runtime rt = Runtime.getRuntime();
        for (int i = 0; i < 3; i++) rt.gc();
        long startSize = rt.totalMemory()-rt.freeMemory();
        List<CountEntry<Long>> ll = test(ar, lcounter,k);
        for (int i = 0; i < 3; i++) rt.gc();
        System.out.println("\t" +
           String.valueOf(rt.totalMemory()-rt.freeMemory()-startSize));

        for (int i = 0; i < 3; i++) rt.gc();
        startSize = rt.totalMemory()-rt.freeMemory();
        List<CountEntry<Long>> sl = test(ar, scounter,k);//fastest and pretty good results
        for (int i = 0; i < 3; i++) rt.gc();//2nd smallest memory footprint, asymptotic growth may be important
        System.out.println("\t" +
           String.valueOf(rt.totalMemory()-rt.freeMemory()-startSize));
        
        for (int i = 0; i < 3; i++) rt.gc();
        startSize = rt.totalMemory()-rt.freeMemory();
        List<CountEntry<Long>> rl = test(ar, rcounter,k);
        for (int i = 0; i < 3; i++) rt.gc();
        System.out.println("\t" +
           String.valueOf(rt.totalMemory()-rt.freeMemory()-startSize));
        
        
//		for(int i =0;i<testsize;i++)
//		{
//			smpl.add(ar.get(i));
//			rcounter.add(ar.get(i),1);
//			karp.add(ar.get(i));
//			lcounter.add(ar.get(i),1);
//			scounter.add(ar.get(i),1);
//			sscounter.add(ar.get(i),1);
//		}
        
//		ArrayList<Integer> kl = karp.getTop();//but karp us pretty terrible
//		ArrayList<Integer> sml = smpl.getTop();//takes roughly 5x longer
//		List<CountEntry<Integer>> ll = lcounter.peek(kl.size());
//		List<CountEntry<Integer>> sl = scounter.peek(kl.size());
//		List<CountEntry<Integer>> ssl = sscounter.peek(kl.size());
//		List<CountEntry<Integer>> rl = rcounter.peek(kl.size());
		
		System.out.println("Real \t Smpl \t karp \t lossy \t stcky \t ssave");
		for(int i = 0;i<k;i++)
			try{
			System.out.println(//sml.get(i).intValue()+"\t" + 
					rl.get(i).getItem()+"\t" + "\t \t"+
					//kl.get(i).intValue()+"\t"+
					ll.get(i).getItem()+"\t"+
					sl.get(i).getItem()+"\t\t"+
					" ");}//ssl.get(i).getItem()) ;}
			catch(Exception E){
						;
					}
	}
	
	static float distance(float[] x,float[] y)
	{
		float dist = 0f ;
		for(int i = 0 ;i< x.length; i++)dist += (x[i]-y[i])*(x[i]-y[i]);
		return dist;
	}
	
	
	static int findNearestDistance(float[] x,List <float[]> DB)
	{
		Iterator<float[]> it = DB.iterator();
		float mindist = distance(x,it.next());
		float tmp;
		int minindex = 0;
		int index = 0;
		while(it.hasNext()){
			tmp = distance(x,it.next());
			index++;
			if(tmp < mindist){
				mindist = tmp;
				minindex = index;
			}

		}
		return minindex;
	}
	
	
	
	static void testRPHash(){
		GenerateData gen = new GenerateData(5,5000,24);

		for(float[] vec : gen.medoids())
		{
		for(int i = 0 ; i < vec.length;i++)
			System.out.printf("%.2f ",vec[i]);
			System.out.println();
		}
		
		System.out.print(  "------------------------------------");
		System.out.print(  "------------------------------------");
		System.out.print(  "------------------------------------");
		System.out.println("------------------------------------");
		RPHashObject so = new SimpleArrayReader(gen.data(),5,1,250000);
		RPHash clusterer = new RPHash();
		so = clusterer.mapP1(so);
		so = clusterer.mapP2(so);
		float [] centroid = so.getNextCentroid();
		while(centroid!=null){
			for(int i = 0 ; i < centroid.length;i++)
				System.out.printf("%.2f ",centroid[i]);
			int minindex = findNearestDistance(centroid,gen.medoids());
			System.out.printf("\t| %d %.4f \n",minindex,distance(centroid,gen.medoids().get(minindex)));

			findNearestDistance(centroid,gen.medoids());
			centroid = so.getNextCentroid();
		}

	

//		
//		for(float[] vec : gen.medoids())
//		{
//			for(float v : vec)
//				System.out.print(v+" ");
//			System.out.println();
//		}
//		
//		GenerateData gd = new GenerateData(5,10,10);
//		for(float[] v:gd.data()){
//			for(float i : v)System.out.print(i+" ");
//			System.out.println();
//		}
//		for(float[] v:gd.medoids()){
//			for(float i : v)System.out.print(i+" ");
//			System.out.println();
//		}
//		File file = new File("/home/lee/Desktop/M.mat");
//		gd = new GenerateData(5,10,10,file);
		
	}
	
	public static void main(String[] args){
		//testFrequentItems();
		testRPHash();
			
	}
	
	public static float[] cnv(double[] fff){
	float[] ret = new float[fff.length];
	for(int i = 0 ;i<fff.length;i++)ret[i] = (float)fff[i];
	return ret;
	}

}

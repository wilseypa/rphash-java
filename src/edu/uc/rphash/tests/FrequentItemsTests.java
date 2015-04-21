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

public class FrequentItemsTests {
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
//		KarpFrequentItemSet<Integer> karp = new KarpFrequentItemSet<Integer>((float)(1./500.0));
//		SimpleFrequentItemSet<Integer> smpl = new SimpleFrequentItemSet<Integer>(20);
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
	public static void main(String[] args){
		testFrequentItems();
	}
	

}

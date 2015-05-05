package edu.uc.rphash.frequentItemSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

import org.streaminer.stream.frequency.CountMinSketch;
import org.streaminer.stream.frequency.CountMinSketchAlt;
import org.streaminer.stream.frequency.FrequencyException;
import org.streaminer.stream.frequency.StickySampling;

import edu.uc.rphash.Centroid;

public class KHHCountMinSketch<E> implements ItemSet<E> {

//	ArrayList<E> randomshit = new ArrayList<>();
//	Random rand = new Random();
	
	private class Tuple implements Comparable<E>{
		public E item;
		public long count;
		public Tuple(E e, long count) {
			this.item = e;
			this.count = count;
		}
		@SuppressWarnings("unchecked")//private class so always a tuple
		@Override
		public int compareTo(Object o) {
			return  (int) (this.count-((Tuple)o).count);

		}
		@Override
		public boolean equals(Object obj) 
		{
			return item.hashCode() == ((Tuple)obj).item.hashCode();
		}
		@Override
		public int hashCode() {
			return item.hashCode();
		}	
		
	}
//	StickySampling<E> scounter;
	CountMinSketchAlt<Integer> scounter;
//	CountMinSketch<E> scounter;
	PriorityQueue<Tuple> p;
	int k;
	boolean pqfull = false;
	HashMap<Integer,Tuple> items;

	public KHHCountMinSketch(int k)
	{
		this.k=k;
		scounter = new CountMinSketchAlt<>(.00001,.995,(int)System.currentTimeMillis());
		p = new PriorityQueue<Tuple>();
		items = new HashMap<>();
	}
	
	
	public KHHCountMinSketch(double epsOfTotalCount,	double confidence,
	int seed,int k)
	{
		this.k=k;
		scounter = new CountMinSketchAlt<>(.00001,.995,(int)System.currentTimeMillis());
		p = new PriorityQueue<Tuple>();
	}
	
	@Override
	public List<E> getTop() {
		ArrayList<E> ret = new ArrayList<>();
		Iterator<Tuple> it =  p.iterator();
		while(it.hasNext()){
			ret.add(it.next().item);
		}
		
		return ret;
	}
	
	@Override
	public List<Long> getCounts() 
	{
		ArrayList<Long> ret = new ArrayList<>();
		Iterator<Tuple> it =  p.iterator();
		while(it.hasNext()){
			ret.add(it.next().count);
		}
		Collections.sort(ret);
		return ret;
	}
	@Override
	public Object getBaseClass() {
		return scounter;
	}

	
	
	@Override
	public boolean add(E e){
		try {
			scounter.add(e.hashCode());
		} catch (FrequencyException e1) {
			e1.printStackTrace();
		}
		
		long count = scounter.estimateCount(e.hashCode());
		Tuple t = items.get(e.hashCode());
		
		if(t!=null){//update current in list item
			t.count++;
			if(e instanceof Centroid)((Centroid)t.item).updateVec(((Centroid)t.item).centroid());
		}
		else{
			Tuple newt = new Tuple(e,count);
			if(!pqfull){
				items.put( e.hashCode(),newt);
				p.add(newt);
				pqfull = (p.size()==this.k);
			}
			else{
				
				if(count>p.peek().count){
					items.remove(p.poll().item.hashCode());
					items.put( e.hashCode(),newt);
					p.add(newt);
				}
			}
		}
		return false;
	}

	
	public static void main(String[] t) throws FrequencyException{
		Random r = new Random();
		KHHCountMinSketch<Integer> khh = new KHHCountMinSketch<>(50);
		//CountMinSketchAlt<Integer> scounter = new CountMinSketchAlt<>(.00001,.995,101223);
		long ts = System.currentTimeMillis();
		for(long i = 1 ; i< 10000000;i++){
			
			khh.add( r.nextInt((int)i)/100);
			//scounter.add( r.nextInt((int)i)/100);
		}
		System.out.println( System.currentTimeMillis()-ts);
		System.out.println(khh.getTop());
		System.out.println(khh.getCounts());
		//for(int i = 1;i<100;i++)System.out.print(scounter.estimateCount(i)+", ");
		
	}


}

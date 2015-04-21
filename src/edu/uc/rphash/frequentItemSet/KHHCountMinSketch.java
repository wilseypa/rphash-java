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
		public boolean equals(Object obj) {
			return item.equals(((Tuple)obj).item);
		}
		@Override
		public int hashCode() {
			return item.hashCode();
		}	
		
	}
//	StickySampling<E> scounter;
	CountMinSketchAlt<E> scounter;
//	CountMinSketch<E> scounter;
	PriorityQueue<Tuple> p;
	int k;
	boolean pqfull = false;
	HashSet<Tuple> items;

	public KHHCountMinSketch(int k)
	{
		this.k=k;

		scounter = new CountMinSketchAlt<E>(.0001,.99,101223);
		p = new PriorityQueue<Tuple>();
		items = new HashSet<>();
	}
	
	
	public KHHCountMinSketch(double epsOfTotalCount,	double confidence,
	int seed,int k)
	{
		this.k=k;
		scounter = new CountMinSketchAlt<E>(.00001,.995,101223);
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
		return ret;
	}
	@Override
	public Object getBaseClass() {
		return scounter;
	}

	@Override
	public boolean add(E e){
		try {
			scounter.add(e);
		} catch (FrequencyException e1) {
			e1.printStackTrace();
		}
		
		
		long count = scounter.estimateCount(e);
		Tuple t = new Tuple(e,count);
		
		if(items.contains(t)){//update current in list item
			items.remove(t);
			p.remove(t);
			t = new Tuple(t.item,count+1);
			items.add(t);
			p.add(t);
		}
			
		else{
			if(!pqfull)
			{
				items.add(t);
				p.add(t);
				pqfull = (p.size()==this.k);
				
			}
			
			else{
				if(count>p.peek().count){
					items.remove(p.poll());
					items.add(t);
					p.add(t);
				}
			}
		}
		return false;
	}

	
	public static void main(String[] t) throws FrequencyException{
		Random r = new Random();
		KHHCountMinSketch<Long> khh = new KHHCountMinSketch<Long>(10);
		CountMinSketchAlt<Long> scounter = new CountMinSketchAlt<Long>(.0001,.99,101223);
		for(long i = 1 ; i< 100000;i++){
			
			khh.add((long) r.nextInt((int)i)/100);
			scounter.add((long) r.nextInt((int)i)/100);
		}

		System.out.println(khh.getTop());
		System.out.println(khh.getCounts());
		for(long i = 1;i<100;i++)System.out.print(scounter.estimateCount(i)+", ");
		
	}


}

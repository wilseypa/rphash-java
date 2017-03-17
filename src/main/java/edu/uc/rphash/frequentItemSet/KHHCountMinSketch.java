package edu.uc.rphash.frequentItemSet;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

import edu.uc.rphash.Centroid;

public class KHHCountMinSketch<E> implements ItemSet<E>,Countable {
	public static final long PRIME_MODULUS = (1L << 31) - 1;
	private int depth;
	private int width;
	private long[][] table;
	private long[] hashA;
	private long size;
	
	PriorityQueue<E> p;
	int k;
	//int origk;
	HashMap<Long, E> items;
	HashMap<Long,Long> countlist;

	public KHHCountMinSketch(int m) {
		//this.origk = k;
		
		this.k = (int) (m * Math.log(m));
		double epsOfTotalCount = .00001;
		double confidence = .99;
		int seed = (int) System.currentTimeMillis();
		
		countlist = new HashMap<>();
		
		Comparator<E> cmp = new Comparator<E>() {
			@Override
			public int compare(E n1, E n2) {
				long cn1 = countlist.get((long)n1.hashCode());//count(n1.hashCode());
				long cn2 = countlist.get((long)n2.hashCode());////count(n2.hashCode());
				if (cn1 > cn2)
					return +1;
				else if (cn1 < cn2)
					return -1;
				return 0;
			}
		};
		p = new PriorityQueue<E>(cmp);
		items = new HashMap<>();
		this.width = (int) Math.ceil(2 / epsOfTotalCount);
		this.depth = (int) Math.ceil(-Math.log(1 - confidence) / Math.log(2));
		initTablesWith(depth, width, seed);
	}

	private void initTablesWith(int depth, int width, int seed) {
		this.table = new long[depth][width];
		this.hashA = new long[depth];
		Random r = new Random(seed);
		for (int i = 0; i < depth; ++i) {
			hashA[i] = r.nextInt(Integer.MAX_VALUE);
		}
	}

	@Override
	public Object getBaseClass() {
		return this;
	}


	public boolean add(E e) {
		long count = addLong(e.hashCode(), 1);
		if(e instanceof Centroid){
			Centroid c = (Centroid) e;
			E probed =  items.remove(c.id);
//			for(Long h : c.ids){
//				if(probed!=null){
//					break;}
//				probed = items.remove(h);
//				}
			if(probed==null){
				countlist.put( c.id, count);
				p.add(e);
				items.put((long) c.id, e);
			} else // remove the key and put it back
			{
				
				p.remove(probed);
				//((Centroid)probed).updateVec(c.centroid());
				//((Centroid)probed).ids.addAll(c.ids);
				items.put(((Centroid)probed).id,  probed);
				countlist.put( ((Centroid)probed).id, count);
				p.add( probed);
			}	
		}else
		{
			if (!items.containsKey((long)e.hashCode()))
			{
				countlist.put((long)e.hashCode(), count);
				p.add(e);
				items.put((long)e.hashCode(), e);
			}else{
				p.remove(e);
				items.put((long)e.hashCode(), e);
				countlist.put((long)e.hashCode(), count);
				p.add(e);
			}
				
			
		}
		
		
		if (p.size() > this.k) {
			items.remove(p.poll());
		}
		return false;
	}

	private int hash(long item, int i) {
		long hash = hashA[i] * item;
		hash += hash >> 32;
		hash &= PRIME_MODULUS;
		return ((int) hash) % width;
	}

//	private long count(long item) {
//		int min = (int) table[0][hash(item, 0)];
//		for (int i = 1; i < depth; ++i) {
//			if (table[i][hash(item, i)] < min)
//				min = (int) table[i][hash(item, i)];
//		}
//		return min;
//	}

	private long addLong(long item, long count) {
		table[0][hash(item, 0)] += count;
		int min = (int) table[0][hash(item, 0)];
		for (int i = 1; i < depth; ++i) {
			table[i][hash(item, i)] += count;
			if (table[i][hash(item, i)] < min)
				min = (int) table[i][hash(item, i)];
		}
		size += count;
		return min;
	}
	
	
	public float count(long item) {
		int min = (int) table[0][hash(item, 0)];
		for (int i = 1; i < depth; ++i) {
			if (table[i][hash(item, i)] < min)
				min = (int) table[i][hash(item, i)];
		}
		return min;
	}
	
	
	
	List<E> topcent = null;
	List<Long> counts = null;
	@Override
	public List<E> getTop() {
		if (this.topcent != null)
			return this.topcent;
		
		this.topcent = new ArrayList<>();
		this.counts = new ArrayList<>();

		while (!p.isEmpty()) {
			E tmp = p.poll();
			topcent.add(tmp);
			counts.add(countlist.get((long)tmp.hashCode()));//count(tmp.hashCode()));
		}
		
//		topcent = topcent.subList(k - origk, k);
//		counts = counts.subList(k - origk, k);
		return topcent;
	}

	@Override
	public List<Long> getCounts() {
		if (this.counts != null)
			return counts;
		getTop();
		return counts;
	}
	
	public static void main(String[] t) {
		Random r = new Random();
		KHHCountMinSketch<Integer> khh = new KHHCountMinSketch<>(100);
		long ts = System.currentTimeMillis();

		for (long i = 1; i < 5000000; i++) {
			khh.add(r.nextInt((int) i) / 100);
		}

		System.out.println(System.currentTimeMillis() - ts);
		System.out.println(khh.getTop());
		System.out.println(khh.getCounts());
	}

	@Override
	public boolean add(Long e) {
		addLong(e,1);
		return true;
	}
}

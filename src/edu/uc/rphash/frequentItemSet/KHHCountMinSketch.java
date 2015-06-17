package edu.uc.rphash.frequentItemSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

import edu.uc.rphash.Centroid;

public class KHHCountMinSketch<E> implements ItemSet<E> {
	public static final long PRIME_MODULUS = (1L << 31) - 1;
	private int depth;
	private int width;
	private long[][] table;
	private long[] hashA;
	private long size;
	
	PriorityQueue<E> p;
	int k;
	int origk;
	HashMap<Integer, E> items;
	HashMap<Integer,Long> countlist;

	public KHHCountMinSketch(int k) {
		this.origk = k;
		this.k = (int) (k * Math.log(k));

		double epsOfTotalCount = .00001;
		double confidence = .99;
		int seed = (int) System.currentTimeMillis();
		
		countlist = new HashMap<>();
		Comparator<E> cmp = new Comparator<E>() {
			@Override
			public int compare(E n1, E n2) {
				long cn1 = countlist.get(n1.hashCode());//count(n1.hashCode());
				long cn2 = countlist.get(n2.hashCode());////count(n2.hashCode());
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

	List<E> topcent = null;
	List<Long> counts = null;
	@Override
	public List<E> getTop() {
		if (this.topcent != null)
			return this.topcent;
		
		this.topcent = new ArrayList<>();
		this.counts = new ArrayList<>();
		
		while (p.size() > 0) {
			E tmp = p.remove();
			topcent.add(tmp);
			counts.add(countlist.get(tmp.hashCode()));//count(tmp.hashCode()));
		}
		
		topcent = topcent.subList(k - origk, k);
		counts = counts.subList(k - origk, k);
		return topcent;
	}

	@Override
	public List<Long> getCounts() {
		if (this.counts != null)
			return counts;
		
		getTop();
		
		return counts;
	}

	@Override
	public Object getBaseClass() {
		return this;
	}

	@Override
	public boolean add(E e) {
		if (!items.containsKey(e.hashCode())) {
			long count = addLong(e.hashCode(), 1);
			countlist.put(e.hashCode(), count);
			p.add(e);
			items.put(e.hashCode(), e);
		} else // remove the key and put it back
		{
			p.remove(e);
			if (e instanceof Centroid) {
				((Centroid) e).updateVec(((Centroid) e).centroid());
				((Centroid) e).addID(((Centroid) e).id);
			}
			long count = addLong(e.hashCode(), 1);
			items.put(e.hashCode(), e);
			countlist.put(e.hashCode(), count);
			
			p.add(e);
		}
		if (p.size() > k) {
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

	private long count(long item) {
		int min = (int) table[0][hash(item, 0)];
		for (int i = 1; i < depth; ++i) {
			if (table[i][hash(item, i)] < min)
				min = (int) table[i][hash(item, i)];
		}
		return min;
	}

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
}

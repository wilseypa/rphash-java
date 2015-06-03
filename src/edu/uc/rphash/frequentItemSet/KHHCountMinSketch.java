package edu.uc.rphash.frequentItemSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

import edu.uc.rphash.Centroid;


public class KHHCountMinSketch<E> implements ItemSet<E> {

	private class Tuple implements Comparable<E> {
		public E item;
		public long count;

		public Tuple(E e, long count) {
			this.item = e;
			this.count = count;
		}

		@SuppressWarnings("unchecked")
		// private class so always a tuple
		@Override
		public int compareTo(Object o) {
			return (int) (this.count - ((Tuple) o).count);
//	min sort		return (int) ( ((Tuple) o).count-this.count );
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean equals(Object obj) {
			return item.hashCode() == ((Tuple) obj).item.hashCode();
		}

		@Override
		public int hashCode() {
			return item.hashCode();
		}

	}

	public static final long PRIME_MODULUS = (1L << 31) - 1;
	private int depth;
	private int width;
	private long[][] table;
	private long[] hashA;
	private long size;
	PriorityQueue<Tuple> p;
	int k;
	int origk;
	boolean pqfull = false;
	HashMap<Integer, Tuple> items;

	public KHHCountMinSketch(int k) {
		this.origk = k;
		this.k = (int) (k*Math.log(k));
	
		double epsOfTotalCount = .00001;
		double confidence = .99;
		int seed = (int) System.currentTimeMillis();
		p = new PriorityQueue<Tuple>();
		items = new HashMap<>();
		this.width = (int) Math.ceil(2 / epsOfTotalCount);
		this.depth = (int) Math.ceil(-Math.log(1 - confidence) / Math.log(2));
		initTablesWith(depth, width, seed);
	}
	
	public KHHCountMinSketch(double epsOfTotalCount, double confidence, int seed,
			int k) {
		this.origk = k;
		this.k = (int) (k*Math.log(k));
		p = new PriorityQueue<Tuple>();
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

	ArrayList<E> topcent = null;
	ArrayList<Long> counts =null;
	@Override
	public List<E> getTop() {
		if(this.topcent!=null) return this.topcent;
		this.topcent = new ArrayList<>();
		this.counts = new ArrayList<>();
				
		while (!p.isEmpty()) {
			Tuple tmp = p.poll();
			this.topcent.add(tmp.item);
			//System.out.println(tmp.count);
			this.counts.add(tmp.count);
		}

		return topcent;
	}

	
	@Override
	public List<Long> getCounts() {
		if(this.counts != null) return this.counts;
		getTop();
		return this.counts;
	}

	@Override
	public Object getBaseClass() {
		return this;// scounter;
	}

	@Override
	public boolean add(E e) {

		long count = addLong(e.hashCode(), 1);
		Tuple t = items.get(e.hashCode());

		if (t != null) {// update current in list item
			t.count++;
			if (e instanceof Centroid)
			{
				((Centroid) t.item).updateVec(((Centroid) e).centroid());
				((Centroid) t.item).addID(((Centroid) e).id);
			}
		} else {
			Tuple newt = new Tuple(e, count);
			if (!pqfull) {
				items.put(e.hashCode(), newt);
				p.add(newt);
				pqfull = (p.size() == this.k);
			} else {

				if (p.peek()!=null && count > p.peek().count) {
					items.remove(p.poll().item.hashCode());
					items.put(e.hashCode(), newt);
					p.add(newt);
				}
			}
		}
		return false;
	}

	public static void main(String[] t) {
		Random r = new Random();
		KHHCountMinSketch<Integer> khh = new KHHCountMinSketch<>(10);
		// CountMinSketchAlt<Integer> scounter = new
		// CountMinSketchAlt<>(.00001,.995,101223);
		long ts = System.currentTimeMillis();
		for (long i = 1; i < 10000000; i++) {
			khh.add(r.nextInt((int) i) / 1000);
			// scounter.add( r.nextInt((int)i)/100);
		}

		System.out.println(System.currentTimeMillis() - ts);
		System.out.println(khh.getTop());
		System.out.println(khh.getCounts());
		// for(int i =
		// 1;i<100;i++)System.out.print(scounter.estimateCount(i)+", ");

	}

	private int hash(long item, int i) {
		long hash = hashA[i] * item;
		hash += hash >> 32;
		hash &= PRIME_MODULUS;
		return ((int) hash) % width;
	}

	private int addLong(long item, long count) {
		table[0][hash((Long) item, 0)] += count;
		int min = (int) table[0][hash((Long) item, 0)];
		for (int i = 1; i < depth; ++i) {

			table[i][hash((Long) item, i)] += count;
			if (table[i][hash((Long) item, i)] < min)
				min = (int) table[i][hash((Long) item, i)];
		}
		size += count;
		return min;
	}


}

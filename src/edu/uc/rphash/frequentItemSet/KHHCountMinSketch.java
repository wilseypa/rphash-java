package edu.uc.rphash.frequentItemSet;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

import edu.uc.rphash.Centroid;

public class KHHCountMinSketch<E> implements ItemSet<E> {

	// /**Tuple of counts and items
	// * @author lee
	// *Note: this class has a natural ordering that is inconsistent with
	// equals.
	// */
	// private class Tuple implements Comparable<E> {
	// public E item;
	// public int count;
	//
	// public Tuple(E e, int count) {
	// this.item = e;
	// this.count = count;
	// }
	//
	// @SuppressWarnings("unchecked")
	// // private class so always a tuple
	// @Override
	// public int compareTo(Object o) {
	// Tuple t = ((Tuple) o);
	// if(this.count>t.count)return +1;
	// else if(this.count<t.count) return -1;
	// return 0;
	// }
	//
	//
	//
	// }

	public static final long PRIME_MODULUS = (1L << 31) - 1;
	private int depth;
	private int width;
	private long[][] table;
	private long[] hashA;
	private long size;
	PriorityQueue<E> p;
	int k;
	int origk;
	boolean pqfull = false;
	HashMap<Integer, E> items;

	public KHHCountMinSketch(int k) {
		this.origk = k;
		this.k = (int) (k * Math.log(k));

		double epsOfTotalCount = .00001;
		double confidence = .99;
		int seed = (int) System.currentTimeMillis();

		Comparator<E> cmp = new Comparator<E>() {
			@Override
			public int compare(E n1, E n2) {
				long cn1 = count(n1.hashCode());
				long cn2 = count(n2.hashCode());
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

	public KHHCountMinSketch(double epsOfTotalCount, double confidence,
			int seed, int k) {
		this.origk = k;
		this.k = (int) (k * Math.log(k));
		Comparator<E> cmp = new Comparator<E>() {
			@Override
			public int compare(E n1, E n2) {
				long cn1 = count(n1.hashCode());
				long cn2 = count(n2.hashCode());
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

	ArrayList<E> topcent = null;
	ArrayList<Long> counts = null;

	@Override
	public List<E> getTop() {
		if (this.topcent != null)
			return this.topcent.subList(k-origk, k);
		this.topcent = new ArrayList<>();
		this.counts = new ArrayList<>();

		while (p.size() > 0) {
			E tmp = p.remove();
			this.topcent.add(tmp);
			this.counts.add((long) count(tmp.hashCode()));
		}

		return topcent.subList(k-origk, k);
	}

	@Override
	public List<Long> getCounts() {
		if (this.counts != null)
			return this.counts.subList(k-origk, k);
		getTop();
		return this.counts.subList(k-origk, k);
	}

	@Override
	public Object getBaseClass() {
		return this;// scounter;
	}

	@Override
	public boolean add(E e) {

		// Long count = count(e.hashCode());

		// <<<<<<< HEAD
		// if (t != null) {// update current in list item
		// t.count++;
		// if (e instanceof Centroid)
		// {
		// ((Centroid) t.item).updateVec(((Centroid) e).centroid());
		// ((Centroid) t.item).addID(((Centroid) e).id);
		// }
		// } else {
		// Tuple newt = new Tuple(e, count);
		// if (!pqfull) {
		// items.put(e.hashCode(), newt);
		// p.add(newt);
		// pqfull = (p.size() == this.k);
		// } else {
		// =======
		if (!items.containsKey(e)) {
			addLong(e.hashCode(), 1);
			p.add(e);
			items.put(e.hashCode(), e);
			// >>>>>>> branch 'master' of https://github.com/wilseypa/rphash.git

		} else // remove the key an put it back
		{
			p.remove(e);
			addLong(e.hashCode(), 1);
			p.add(e);

		}

		if (p.size() > k) {
			items.remove(p.poll());
		}

		// if (e instanceof Centroid) {
		// Centroid c = (Centroid) e;
		// count = addLong(c.id, 1);
		// for (Long l : c.ids) {
		// t = items.get(l);
		// count = addLong(l, 1);
		// if (t != null) {
		// break;
		// }
		// }
		// } else {// normal stuff
		// count = (long) addLong(e.hashCode(), 1);
		// t = items.get(e.hashCode());
		// }
		//
		// if (t != null) {// update current in list item
		// if (e instanceof Centroid)
		// ((Centroid) t.item).updateVec(((Centroid) e).centroid());
		// t.count += 1;
		// } else {
		// // count = 1L;
		// Tuple newt = new Tuple(e, count.intValue());
		// if (!pqfull) {
		// items.put(e.hashCode(), newt);
		// p.add(newt);
		// pqfull = (p.size() == this.k);
		// } else {
		// if (p.peek() != null && count > p.peek().count) {
		// items.remove(p.poll().item.hashCode());
		// items.put(e.hashCode(), newt);
		// p.add(newt);
		// }
		// }
		// }
		return false;
	}

	// much slower, former does better book keeping
	// @Override
	// public boolean add(E e) {
	// long count = addLong(e.hashCode(), 1);
	//
	// if(itemMap.containsKey(e.hashCode())){//faster to check first before
	// removing
	// E item = itemMap.get(e.hashCode());
	// if(e instanceof Centroid && item instanceof Centroid)
	// ((Centroid)item).updateVec(((Centroid)e).centroid());
	// p.remove(new Tuple(e.hashCode(),count));
	// p.add(new Tuple(e.hashCode(),count));
	// return true;
	// }
	// //new object
	// p.add(new Tuple(e.hashCode(),count));
	// itemMap.put(e.hashCode(),e);
	// //System.out.println(itemMap);
	// if (itemMap.size() > this.k)
	// itemMap.remove(p.poll().itemhash);
	//
	// return true;
	// }

	public static void main(String[] t) {
		Random r = new Random();
		KHHCountMinSketch<Integer> khh = new KHHCountMinSketch<>(10);
		// CountMinSketchAlt<Integer> scounter = new
		// CountMinSketchAlt<>(.00001,.995,101223);
		long ts = System.currentTimeMillis();

		for (long i = 1; i < 5000000; i++) {
			khh.add(r.nextInt((int) i) / 100);
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

}

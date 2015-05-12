package edu.uc.rphash.frequentItemSet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

import edu.uc.rphash.Centroid;
import edu.uc.rphash.decoders.Golay;
import edu.uc.rphash.projections.DBFriendlyProjection;
import edu.uc.rphash.projections.Projector;

@SuppressWarnings("rawtypes")
public class KHHHashCounter implements ItemSet {
	private class Tuple implements Comparable {
		public Centroid item;
		public long count;

		public Tuple(Centroid e, long count) {
			this.item = e;
			this.count = count;
		}

		// private class so always a tuple
		@Override
		public int compareTo(Object o) {
			return (int) (this.count - ((Tuple) o).count);

		}

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
	private int bits;
	private int seed;
	private long[][] table;
	private int[][] hashA;
	private long size;
	PriorityQueue<Tuple> p;
	int k;
	int origk;
	int dim;
	boolean pqfull = false;
	HashMap<Integer, Tuple> items;
	Projector[] proj;

	public KHHHashCounter(int k, int dim) {

		this.origk = k;
		this.k = (int) (k * Math.log(k));

		double epsOfTotalCount = .00001;
		double confidence = .995;
		this.seed = (int) System.currentTimeMillis();
		p = new PriorityQueue<Tuple>();
		items = new HashMap<>();
		this.dim = dim;
		this.width = (int) Math.ceil(2 / epsOfTotalCount);
		this.depth = (int) Math.ceil(-Math.log(1 - confidence) / Math.log(2));
		this.bits = 24;
		initTablesWith();
	}

	private void initTablesWith() {
		this.table = new long[depth][width];
		this.hashA = new int[depth][bits];
		Random r = new Random(seed);
		for (int i = 0; i < depth; ++i) {
			for (int j = 0; j < bits; ++j) {
				hashA[i][j] = j;// r.nextInt(24);
			}

		}

		proj = new Projector[depth];
		for (int i = 0; i < depth; i++) {
			proj[i] = new DBFriendlyProjection(dim, 24, r.nextLong());

		}
	}

	public KHHHashCounter(double epsOfTotalCount, double confidence, int seed,
			int k, int bits, int dim) {
		this.origk = k;
		this.k = (int) (k * Math.log(k));
		p = new PriorityQueue<Tuple>();
		items = new HashMap<>();
		this.width = (int) Math.ceil(2 / epsOfTotalCount);
		this.depth = (int) Math.ceil(-Math.log(1 - confidence) / Math.log(2));
		this.dim = dim;
		this.seed = (int) System.currentTimeMillis();
		initTablesWith();
	}

	ArrayList<Centroid> topcent = null;
	ArrayList<Long> counts = null;

	@Override
	public List<Centroid> getTop() {
		if (this.topcent != null)
			return this.topcent;
		this.topcent = new ArrayList<>();
		this.counts = new ArrayList<>();

		while (!p.isEmpty()) {
			Tuple tmp = p.poll();
			this.topcent.add(tmp.item);
			this.counts.add(tmp.count);
		}

		return topcent.subList(topcent.size() - origk, topcent.size());
	}

	@Override
	public List<Long> getCounts() {
		if (this.counts != null)
			return this.counts;
		getTop();
		return this.counts;
	}

	@Override
	public Object getBaseClass() {
		return this;// scounter;
	}

	public boolean add(Centroid e) {
		long count = addVector(e.centroid(), 1);
		Tuple t = items.get(e);

		if (t != null) {// update current in list item
			t.count++;
			((Centroid) t.item).updateVec(((Centroid) t.item).centroid());
		} else {
			Tuple newt = new Tuple(e, count);
			if (!pqfull) {
				items.put(e.hashCode(), newt);
				p.add(newt);
				pqfull = (p.size() == this.k);
			} else {

				if (count > p.peek().count) {
					items.remove(p.poll().item.hashCode());
					items.put(e.hashCode(), newt);
					p.add(newt);
				}
			}
		}

		return false;
	}

	//
	// public static void main(String[] t) {
	// Random r = new Random();
	// KHHHashCounter<Integer> khh = new KHHHashCounter<>(50);
	// // CountMinSketchAlt<Integer> scounter = new
	// // CountMinSketchAlt<>(.00001,.995,101223);
	// long ts = System.currentTimeMillis();
	// for (long i = 1; i < 10000000; i++) {
	// khh.add(r.nextInt((int) i) / 100);
	// // scounter.add( r.nextInt((int)i)/100);
	// }
	//
	// System.out.println(System.currentTimeMillis() - ts);
	// System.out.println(khh.getTop());
	// System.out.println(khh.getCounts());
	// // for(int i =
	// // 1;i<100;i++)System.out.print(scounter.estimateCount(i)+", ");
	//
	// }

	// int gt = 0;
	// int lt = 0;
	private int hash(float[] item, int i) {
		int hash = 0;
		float[] itemtmp = proj[i].project(item);
		for (int j = 0; j < bits; j++) {
			if (itemtmp[hashA[i][j]] < 0/* average[hashA[i][j]] */) {
				hash += 1;
				// lt++;
			} else {
				// gt++;
			}
			hash <<= 1;
		}
		return Golay.decode(hash);
		// return ((int) hash) % width;
	}

	// add test for radial distance to use spherical lsh

	private int addVector(float[] item, long count) {
		table[0][hash(item, 0)] += count;
		int min = (int) table[0][hash(item, 0)];
		for (int i = 1; i < depth; ++i) {
			table[i][hash(item, i)] += count;
			if (table[i][hash(item, i)] > min)
				min = (int) table[i][hash(item, i)];
		}
		size += count;
		return min;
	}

	@Override
	public boolean add(Object e) {
		System.out.println("sadfasd");
		return false;
	}

}

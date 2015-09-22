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
import edu.uc.rphash.tests.TestUtil;

/**
 * K Heavy-Hitters Centroid Counter maintains a list of the top k log k most
 * frequent centroid ids and a cmsketch of all vector id counts.
 */
// TODO
/*
 * @author lee
 */
public class KHHCentroidCounter {
	public static final long PRIME_MODULUS = (1L << 31) - 1;
	private int depth;
	private int width;
	private short[][] table;
	private long[] hashA;
	public long count;

	PriorityQueue<Centroid> priorityQueue;
	int k;
	int origk;
	HashMap<Long, Centroid> frequentItems;
	HashMap<Long, Long> countlist;

	public KHHCentroidCounter(int k) {
		this.origk = k;
		this.k = (int) (k * Math.log(k)) * 4;
		double epsOfTotalCount = .00001;
		double confidence = .99;
		int seed = (int) System.currentTimeMillis();
		count = 0;
		countlist = new HashMap<>();

		Comparator<Centroid> cmp = new Comparator<Centroid>() {
			@Override
			public int compare(Centroid n1, Centroid n2) {
				long cn1 = countlist.get(n1.id);// count(n1.id);
				long cn2 = countlist.get(n2.id);// count(n2.id);
				if (cn1 > cn2)
					return +1;
				else if (cn1 < cn2)
					return -1;
				return 0;
			}
		};
		priorityQueue = new PriorityQueue<Centroid>(cmp);
		frequentItems = new HashMap<>();
		this.width = (int) Math.ceil(2 / epsOfTotalCount);
		this.depth = (int) Math.ceil(-Math.log(1 - confidence) / Math.log(2));
		initTablesWith(depth, width, seed);
	}

	private void initTablesWith(int depth, int width, int seed) {
		this.table = new short[depth][width];
		this.hashA = new long[depth];
		Random r = new Random(seed);
		for (int i = 0; i < depth; ++i) {
			hashA[i] = r.nextLong();
		}
	}

	public void add(Centroid c) {
		this.count++;
		long count = addLong(c.id, 1);
		Centroid probed = frequentItems.remove(c.id);
		//search for blurred and projected versions if
		//representative id is not in the frequentItems lists
		for (Long h : c.ids) {
			if (probed != null) {
				break;
			}
			probed = frequentItems.remove(h);
		}

		if (probed == null) {//add new centroid to the tree and frequent item list
			countlist.put(c.id, count);
			frequentItems.put(c.id, c);
			priorityQueue.add(c);
		} else//update centroids if it was in the tree and put it back
		{
			
			//java hash clobbers original value on update
			priorityQueue.remove(probed);
			probed.updateVec(c.centroid());
			probed.ids.addAll(c.ids);
			frequentItems.put(probed.id, probed);
			// Long oldcount = countlist.remove(probed.id);
			countlist.put(probed.id, count + 1);
			priorityQueue.add(probed);
		}

		//shrink if needed
		if (priorityQueue.size() > this.k) {
			Centroid removed = priorityQueue.poll();
			frequentItems.remove(removed.id);
			countlist.remove(removed.id);
		}
	}

	private int hash(long item, int i) {
		long hash = hashA[i] * item;
		hash += hash >>> 32;
		hash &= PRIME_MODULUS;
		return (int) (hash % width);
	}

	/**
	 * add item hashed to a long value to count min sketch table add long comes
	 * from streaminer documentation
	 * 
	 * @param item
	 * @param count
	 * @return size of min count bucket
	 */
	private long addLong(long item, long count) {
		table[0][hash(item, 0)] += count;
		int min = (int) table[0][hash(item, 0)];
		for (int i = 1; i < depth; ++i) {
			table[i][hash(item, i)] += count;
			if (table[i][hash(item, i)] < min)
				min = (int) table[i][hash(item, i)];
		}

		return min;
	}

	private long count(long item) {
		int min = (int) table[0][hash(item, 0)];
		for (int i = 1; i < depth; ++i) {
			if (table[i][hash(item, i)] < min)
				min = (int) table[i][hash(item, i)];
		}
		return min;
	}

	List<Centroid> topcent = null;
	List<Long> counts = null;

	public List<Centroid> getTop() {
		if (this.topcent != null)
			return topcent;
		// return this.topcent;

		this.topcent = new ArrayList<>();
		this.counts = new ArrayList<>();
		while (!priorityQueue.isEmpty()) {
			Centroid tmp = priorityQueue.poll();
			topcent.add(tmp);
			counts.add(count(tmp.id));// count(tmp.id));
		}

		// topcent = topcent.subList(k-origk, k);
		// counts = counts.subList(k-origk, k);

		return topcent;
	}

	public List<Long> getCounts() {
		if (this.counts != null)
			return counts;
		getTop();
		return counts;
	}
}

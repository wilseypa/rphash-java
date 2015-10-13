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
	private float[][] tableF;
	private short[][] tableS;
	private int[][] decaytable;
	private long[] hashA;
	public long count;

	PriorityQueue<Centroid> priorityQueue;
	int k;
	int origk;
	HashMap<Long, Centroid> frequentItems;
	HashMap<Long, Float> countlist;
	Float decayRate;

	public KHHCentroidCounter(int k) {
		this.origk = k;
		this.k = (int) (k * Math.log(k)) * 4;
		double epsOfTotalCount = .00001;
		double confidence = .99;
		int seed = (int) System.currentTimeMillis();
		this.decayRate = null;
		count = 0;
		countlist = new HashMap<>();

		Comparator<Centroid> cmp = new Comparator<Centroid>() {
			@Override
			public int compare(Centroid n1, Centroid n2) {
				float cn1 = countlist.get(n1.id);// count(n1.id);
				float cn2 = countlist.get(n2.id);// count(n2.id);
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
	
	
	public KHHCentroidCounter(int k,float decayRate) {
		this.origk = k;
		this.k = (int) (k * Math.log(k)) * 4;
		this.decayRate = decayRate;
		double epsOfTotalCount = .00001;
		double confidence = .99;
		int seed = (int) System.currentTimeMillis();
		count = 0;
		countlist = new HashMap<>();

		Comparator<Centroid> cmp = new Comparator<Centroid>() {
			@Override
			public int compare(Centroid n1, Centroid n2) {
				float cn1 = countlist.get(n1.id);// count(n1.id);
				float cn2 = countlist.get(n2.id);// count(n2.id);
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
		if(decayRate!=null){
			this.tableF = new float[depth][width];
			this.decaytable = new int[depth][width];
		}
		else
			this.tableS = new short[depth][width];
		this.hashA = new long[depth];
		Random r = new Random(seed);
		for (int i = 0; i < depth; ++i) {
			hashA[i] = r.nextLong();
		}
	}

	public void add(Centroid c) {
		this.count++;
		float count = addLong(c.id, 1);
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
	
	private float decayOnInsert(float prev_val,int prevt){
		return 1+(float) (prev_val*Math.pow((1-decayRate),(float)(this.count-prevt)));
	}

	/**
	 * add item hashed to a long value to count min sketch table add long comes
	 * from streaminer documentation
	 * 
	 * @param item
	 * @param count
	 * @return size of min count bucket
	 */
	private float addLong(long item, long count) {
		
		float min;
		if(decayRate!=null)
		{
			int htmp = hash(item, 0);
			int oldtime = decaytable[0][htmp];
			tableF[0][htmp] = decayOnInsert(tableF[0][htmp],oldtime);
			decaytable[0][htmp] = (int) count;
			
			min = tableF[0][htmp];
			
			for (int i = 1; i < depth; ++i) {
				htmp = hash(item, i);
				oldtime = decaytable[i][htmp];
				tableF[i][htmp] = decayOnInsert(tableF[i][htmp],oldtime);
				decaytable[i][htmp] = (int) count;
				if (tableF[i][hash(item, i)] < min)
					min =  tableF[i][hash(item, i)];
			}
		}
		else{
			tableS[0][hash(item, 0)] += count;
			min = (int) tableS[0][hash(item, 0)];
			for (int i = 1; i < depth; ++i) {
				tableS[i][hash(item, i)] += count;
				if (tableS[i][hash(item, i)] < min)
					min = (int) tableS[i][hash(item, i)];
			}
			
		}
		return min;
	}

	
	
	
	private float count(long item) {
		float min;
		if(decayRate!=null)
		{
			int htmp = hash(item, 0);
			int oldtime = decaytable[0][htmp];
			min = decayOnInsert(tableF[0][htmp],oldtime);
			for (int i = 1; i < depth; ++i) {
				htmp = hash(item, i);
				oldtime = decaytable[i][htmp];
				min = decayOnInsert(tableF[i][htmp],oldtime);
				if (tableF[i][hash(item, i)] < min)
					min =  tableF[i][hash(item, i)];
			}
			
		}
		else{
			min = (int) tableS[0][hash(item, 0)];
			for (int i = 1; i < depth; ++i) {
				if (tableS[i][hash(item, i)] < min)
					min = (int) tableS[i][hash(item, i)];
			}
			
		}
		
		return min;
	}

	List<Centroid> topcent = null;
	List<Float> counts = null;

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

	public List<Float> getCounts() {
		if (this.counts != null)
			return counts;
		getTop();
		return counts;
	}
}

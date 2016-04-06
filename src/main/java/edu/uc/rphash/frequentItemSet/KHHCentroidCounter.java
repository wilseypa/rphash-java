package edu.uc.rphash.frequentItemSet;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.PriorityBlockingQueue;

import edu.uc.rphash.Centroid;

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

	PriorityBlockingQueue<Centroid> priorityQueue;
	int k;
	int origk;
	ConcurrentHashMap<Long, Centroid> frequentItems;
	ConcurrentHashMap<Long, Float> countlist;
	Float decayRate;

	Comparator<Centroid> cmp = new Comparator<Centroid>() {
		@Override
		public int compare(Centroid n1, Centroid n2) {
			float cn1 = countlist.get(n1.id);// count(n1.id);
			float cn2 = countlist.get(n2.id);// count(n2.id);
			int counts = (int)(cn2-cn1);
			if (counts!=0)
				return counts;
			if(n1.id!=n2.id){
				return 1;
			}else{
				return 0;
			}
			}
	};
	
	public KHHCentroidCounter(int k) {
		this.origk = k;
		this.k = (int) (k * Math.log(k));
		double epsOfTotalCount = .00001;
		double confidence = .99;
		int seed = (int) System.currentTimeMillis();
		this.decayRate = null;
		count = 0;
		countlist = new ConcurrentHashMap<>();
		priorityQueue = new PriorityBlockingQueue<Centroid>(this.k + 1, cmp);
		frequentItems = new ConcurrentHashMap<>();
		this.width = (int) Math.ceil(2 / epsOfTotalCount);
		this.depth = (int) Math.ceil(-Math.log(1 - confidence) / Math.log(2));
		initTablesWith(depth, width, seed);
	}

	public KHHCentroidCounter(int k, float decayRate) {
		this.origk = k;
		this.k = (int) (k * Math.log(k));
		this.decayRate = decayRate;
		double epsOfTotalCount = .00001;
		double confidence = .99;
		int seed = (int) System.currentTimeMillis();
		count = 0;
		countlist = new ConcurrentHashMap<>();

		priorityQueue = new PriorityBlockingQueue<Centroid>(this.k + 1, cmp);
		frequentItems = new ConcurrentHashMap<>();
		this.width = (int) Math.ceil(2 / epsOfTotalCount);
		this.depth = (int) Math.ceil(-Math.log(1 - confidence) / Math.log(2));
		initTablesWith(depth, width, seed);

	}

	private void initTablesWith(int depth, int width, int seed) {
		if (decayRate != null) {
			this.tableF = new float[depth][width];
			this.decaytable = new int[depth][width];
		} else{
			this.tableS = new short[depth][width];
		}
		this.hashA = new long[depth];
		Random r = new Random(seed);
		for (int i = 0; i < depth; ++i) {
			hashA[i] = r.nextLong();
		}
	}

	public void add(Centroid c) {
		this.count++;
		float count = addLong(c.id,1);

		synchronized (frequentItems) {
			Centroid probed = frequentItems.remove(c.id);
			// search for blurred and projected versions if
			// representative id is not in the frequentItems lists
			for (Long h : c.ids) {
				if (probed != null) {
					break;
				}
				probed = frequentItems.remove(h);
			}

			synchronized (priorityQueue) {
				if (probed == null) {// add new centroid to the tree and
					countlist.put(c.id, count);
					frequentItems.put(c.id, c);
					priorityQueue.add(c);
				} else// update centroids if it was in the tree and put it back
				{
					// java hash clobbers original value on update
					priorityQueue.remove(probed);
					probed.updateVec(c.centroid());
					probed.ids.addAll(c.ids);
					frequentItems.put(probed.id, probed);
					countlist.put(probed.id, count);
					priorityQueue.add(probed);
				}
				// shrink if needed
				if (priorityQueue.size() > this.k*10) {
					Centroid removed = priorityQueue.poll();
					frequentItems.remove(removed.id);
					countlist.remove(removed.id);
				}
			}
		}
	}

	private int hash(long item, int i) {
		long hash = hashA[i] * item;
		hash += hash >>> 32;
		hash &= PRIME_MODULUS;
		return (int) (hash % width);

	}

	private float decayOnInsert(float prev_val, int prevt) {
		return (float) (prev_val * Math.pow((1 - decayRate),
				(float) (this.count - prevt)));
	}

	/**
	 * add item hashed to a long value to count min sketch table add long comes
	 * from streaminer documentation
	 * 
	 * @param item
	 * @param count
	 * @return size of min count bucket
	 */
	public float addLong(long item, long count) {

		float min;
		if (decayRate != null) {
			int htmp = hash(item, 0);
			int oldtime = decaytable[0][htmp];
			tableF[0][htmp] = 1 + decayOnInsert(tableF[0][htmp], oldtime);
			decaytable[0][htmp] = (int) this.count;

			min = tableF[0][htmp];

			for (int i = 1; i < depth; ++i) {
				htmp = hash(item, i);
				oldtime = decaytable[i][htmp];
				tableF[i][htmp] = 1 + decayOnInsert(tableF[i][htmp], oldtime);
				decaytable[i][htmp] = (int) this.count;
				if (tableF[i][htmp] < min)
					min = tableF[i][htmp];
			}
		} else {
			int htmp = hash(item, 0);
			tableS[0][htmp] += count;
			min = (int) tableS[0][htmp];
			for (int i = 1; i < depth; ++i) {
				htmp = hash(item, i);
				tableS[i][htmp] += count;
				if (tableS[i][htmp] < min)
					min = (int) tableS[i][htmp];
			}
		}
		return min;
	}

	private float count(long item) {
		float min;
		if (decayRate != null) {
			int htmp = hash(item, 0);
			int oldtime = decaytable[0][htmp];
			min = decayOnInsert(tableF[0][htmp], oldtime);
			for (int i = 1; i < depth; ++i) {
				htmp = hash(item, i);
				oldtime = decaytable[i][htmp];
				min = decayOnInsert(tableF[i][htmp], oldtime);
				if (tableF[i][hash(item, i)] < min)
					min = tableF[i][hash(item, i)];
			}
		} else {
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
		
		this.topcent = new ArrayList<>();
		this.counts = new ArrayList<>();
		int count = 0;
		synchronized (priorityQueue) {
			while (!priorityQueue.isEmpty() && count<origk) {
				Centroid tmp = priorityQueue.poll();
				topcent.add(tmp);
				counts.add(countlist.get(tmp.id));
				count++;
			}
		}
		return topcent;
	}

	public List<Float> getCounts() {
		if(topcent==null)getTop();
		return counts;
	}
	

}

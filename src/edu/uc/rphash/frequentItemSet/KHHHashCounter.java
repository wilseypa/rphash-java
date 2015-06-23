package edu.uc.rphash.frequentItemSet;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Random;

import edu.uc.rphash.Centroid;
import edu.uc.rphash.decoders.Decoder;
import edu.uc.rphash.decoders.Golay;
import edu.uc.rphash.decoders.Leech;
import edu.uc.rphash.projections.DBFriendlyProjection;
import edu.uc.rphash.projections.Projector;

@SuppressWarnings("rawtypes")
/**Tuple of counts and items
* @author lee
 *Note: this class has a natural ordering that is inconsistent with
 equals.
*/
public class KHHHashCounter {

	public static final long PRIME_MODULUS = (1L << 31) - 1;
	private int depth;
	private int width;
	private long[][] table;
	private long[] hashA;
	PriorityQueue<Long> p;
	int k;
	int origk;
	boolean pqfull = false;
	HashMap<Integer, Centroid> items;

	public KHHHashCounter(int k) {
		this.origk = k;
		this.k = (int) (k * Math.log(k));

		double epsOfTotalCount = .00001;
		double confidence = .99;
		int seed = (int) System.currentTimeMillis();

		Comparator<Long> cmp = new Comparator<Long>() {
			@Override
			public int compare(Long n1, Long n2) {
				long cn1 = count(n1.hashCode());
				long cn2 = count(n2.hashCode());
				if (cn1 > cn2)
					return +1;
				else if (cn1 < cn2)
					return -1;
				return 0;
			}
		};

		p = new PriorityQueue<Long>(cmp);
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

	ArrayList<Centroid> topcent = null;
	ArrayList<Long> counts = null;

	public List<Centroid> getTop() {
		if (this.topcent != null)
			return topcent;//this.topcent.subList(k-origk, k);
		
		this.topcent = new ArrayList<>();
		this.counts = new ArrayList<>();

		while (p.size() > 0) {
			Long tmp = p.remove();
			this.topcent.add(items.get(tmp));
			this.counts.add((long) count(tmp.hashCode()));
		}

		return topcent;//topcent.subList(k-origk, k);
	}


	public List<Long> getCounts() {
		if (this.counts != null)
			return this.counts;//.subList(k-origk, k);
		getTop();
		return counts;//counts.subList(k-origk, k);
	}


	public Object getBaseClass() {
		return this;// scounter;
	}

	public boolean add(Centroid e) {
		if (!items.containsKey(e.hashCode())) {
			addLong(e.hashCode(), 1);
			p.add((long) e.hashCode());
			items.put(e.hashCode(), e);
//			System.out.println("adding");
		} else // remove the key an put it back
		{
//			System.out.println("updating");
			p.remove(e);
			addLong(e.hashCode(), 1);
			p.add((long) e.hashCode());
			items.get(e.hashCode()).updateVec(e.centroid());
		}

		if (p.size() > k) {
			items.remove(p.poll());
//			System.out.println("removing");
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
		return min;
	}

	public static void main(String[] t) {
		Random r = new Random();
		
		KHHHashCounter khh = new KHHHashCounter(20);

		long ts = System.currentTimeMillis();

		for (long i = 1; i < 5000; i++) {
			khh.add(new Centroid(0,r.nextInt((int) i) ));
		}

//		System.out.println(System.currentTimeMillis() - ts);
		for(Centroid c: khh.getTop())
			System.out.println(c);
		
		System.out.println(khh.getCounts());


	}
	
	
}

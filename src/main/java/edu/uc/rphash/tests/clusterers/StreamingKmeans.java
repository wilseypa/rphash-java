package edu.uc.rphash.tests.clusterers;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import edu.uc.rphash.Centroid;
import edu.uc.rphash.Clusterer;
import edu.uc.rphash.RPHashSimple;
import edu.uc.rphash.RPHashStream;
import edu.uc.rphash.StreamClusterer;
import edu.uc.rphash.Readers.RPHashObject;
import edu.uc.rphash.Readers.SimpleArrayReader;
import edu.uc.rphash.tests.StatTests;
import edu.uc.rphash.tests.generators.ClusterGenerator;
import edu.uc.rphash.tests.generators.GenerateStreamData;
import edu.uc.rphash.tests.kmeanspp.Cluster;
import edu.uc.rphash.util.VectorUtil;

/**
 * An implementation of streaming K Means algorithm. It is based on a the
 * following paper:
 *
 * <ul>
 * <li>Braverman, V., Meyerson, A., Ostrovsky, R., Roytman, A., Shindler, M.,
 * and Tagiku, B. Streaming k-means on Well-Clusterable Data. In Proceedings of
 * SODA. 2011, 26-40. Available online <a
 * href="www.cs.ucla.edu/~shindler/StreamingKMeans_soda11.pdfSimilar">here</a></li>
 * </ul>
 *
 * </p>
 */



//TODO wrapper around streamers to do the replication step for multiple runs, basically a meta class
// wrapper that call add vector for each run->streamclusterer , getOfflineStep then reports wcss and
// the min is returned

public class StreamingKmeans implements StreamClusterer {
	public boolean parallel = false;
	public ExecutorService executor;
	private RPHashObject so;
	int processors;
	List<float[]> data;
	List<float[]> centroids;
	
	int runs = 1;

	public class CentroidCluster {

		/**
		 * The centroid of this {@link Cluster}. This is the only data
		 * representation stored for the {@link Cluster}.
		 */
		private float[] centroid;

		/**
		 * The set of data point id's that assigned to this {@link Cluster}.
		 * TODO: Consider replacing with a TIntSet after merging the
		 * graphical-update branch which depends on Trove.
		 */
		private BitSet assignments;

		/**
		 * Creates a new {@link CentroidCluster} that takes ownership of
		 * {@code emptyVector} as the centroid for this {@link Cluster}.
		 * {@code emptyVector} should have length equal to the length of vectors
		 * that will be assigned to this {@link Cluster} and should be dense if
		 * a large number of vectors, or any dense vectors, are expected to be
		 * assigned to this {@link Cluster}.
		 */
		public CentroidCluster(float[] emptyVector) {
			centroid = emptyVector;
			assignments = new BitSet();
		}

		public float[] add(float[] vector1, float[] vector2) {
			int length = vector2.length;
			for (int i = 0; i < length; ++i) {
				float value = vector2[i] + vector1[i];
				vector1[i] = value / 2.0f;
			}
			return vector1;
		}

		public void addVector(float[] vector, int id) {
			add(centroid, vector);
			if (id >= 0)
				assignments.set(id);
		}

		public float cosineSimilarity(float[] a, float[] b) {
			float dotProduct = 0.0f;
			float aMagnitude = 0.0f;
			float bMagnitude = 0.0f;
			for (int i = 0; i < b.length; i++) {
				float aValue = a[i];
				float bValue = b[i];
				aMagnitude += aValue * aValue;
				bMagnitude += bValue * bValue;
				dotProduct += aValue * bValue;
			}
			aMagnitude = (float) Math.sqrt(aMagnitude);
			bMagnitude = (float) Math.sqrt(bMagnitude);
			return (aMagnitude == 0 || bMagnitude == 0) ? 0 : dotProduct
					/ (aMagnitude * bMagnitude);
		}

		public float compareWithVector(float[] vector) {
			return cosineSimilarity(centroid, vector);
		}

		public float[] centroid() {
			// TODO Figure out how to return this as an immutable vector.
			return centroid;
		}

		/**
		 * Returns an empty list, as no spare data point values are maintained.
		 */
		public List<float[]> dataPointValues() {
			return new ArrayList<float[]>();
		}

		public BitSet dataPointIds() {
			return assignments;
		}

		public void merge(CentroidCluster other) {
			add(centroid, other.centroid());
			for (float[] otherDataPoint : other.dataPointValues())
				add(centroid, otherDataPoint);

			for (int i = other.dataPointIds().nextSetBit(0); i >= 0; i = other
					.dataPointIds().nextSetBit(i + 1))
				assignments.set(i);
		}

		public int size() {
			return assignments.size();
		}
	}


	public StreamingKmeans(RPHashObject streamObject) {

		if (parallel)
			this.processors = Runtime.getRuntime().availableProcessors();
		else
			this.processors = 1;
		executor = Executors.newFixedThreadPool(processors);

		this.data = streamObject.getRawData();

		this.so = streamObject;
		centroids = null;
		// Create initial data structures.
		facilities = Collections
				.synchronizedList(new ArrayList<CentroidCluster>());
		idCounter = new AtomicInteger(1);
		firstKPoints = new ArrayList<float[]>(streamObject.getk());

		this.alpha = DEFAULT_ALPHA;
		this.cofl = DEFAULT_COFL;
		this.kofl = DEFAULT_KOFL;

		// Save the constants.
		this.numClusters = streamObject.getk();

		this.beta = 2 * alpha * alpha * cofl + 2 * alpha;
		this.gamma = Math.max(4 * alpha * alpha * alpha * cofl * cofl + 2
				* alpha * alpha * cofl, beta * kofl + 1);
		this.logNumPoints = 30;

		// Precompute the thresholds, which are constants as well.
		costThreshold = gamma;
		facilityThreshold = (1 + logNumPoints) * numClusters;

		LCost = 1;
		facilityCost = LCost / (numClusters * (1 + logNumPoints));
		totalCost = 0;
	}
	
	
	public StreamingKmeans(int k, GenerateStreamData c, int processors) {
		so = new SimpleArrayReader(c, k);
		
		if (processors>1){
			so.setParallel(true);
			this.parallel = true;
			this.processors = processors;
		}
		else{
			this.processors = 1;
			so.setParallel(false);
			this.parallel = false;
		}
		executor = Executors.newFixedThreadPool(this.processors);
		centroids = null;
		// Create initial data structures.
		facilities = Collections
				.synchronizedList(new ArrayList<CentroidCluster>());
		idCounter = new AtomicInteger(1);
		firstKPoints = new ArrayList<float[]>(so.getk());

		this.alpha = DEFAULT_ALPHA;
		this.cofl = DEFAULT_COFL;
		this.kofl = DEFAULT_KOFL;

		// Save the constants.
		this.numClusters = so.getk();

		this.beta = 2 * alpha * alpha * cofl + 2 * alpha;
		this.gamma = Math.max(4 * alpha * alpha * alpha * cofl * cofl + 2
				* alpha * alpha * cofl, beta * kofl + 1);
		this.logNumPoints = 30;

		// Precompute the thresholds, which are constants as well.
		costThreshold = gamma;
		facilityThreshold = (1 + logNumPoints) * numClusters;

		LCost = 1;
		facilityCost = LCost / (numClusters * (1 + logNumPoints));
		totalCost = 0;
	}


	public void run() {
		for (float[] x : data)
			this.addVectorOnlineStep(x);
	}

	/**
	 * Returns "StreamingKMeans"
	 */
	public String toString() {
		return "StreamingKMeans";
	}

	@Override
	public List<Centroid> getCentroids() {

		if (centroids == null)
			run();
		List<float[]> ret = new ArrayList<>();
		for (CentroidCluster c1 : getClusters())
			ret.add(c1.centroid());

		//our default is agglomerative, but kmpp is more akin to skm.
		so.setOfflineClusterer(new KMeansPlusPlus());
		so.getOfflineClusterer().setRawData(ret);
		so.getOfflineClusterer().setK(numClusters);
		return so.getOfflineClusterer().getCentroids();
		// return new HartiganWongKMeans(numClusters, ret).getCentroids();

	}

	@Override
	public RPHashObject getParam() {

		return so;
	}

	/**
	 * The default number of clusters.
	 */
	public static final int DEFAULT_NUM_CLUSTERS = 10;

	/**
	 * The default number of clusters.
	 */
	public static final int DEFAULT_NUM_POINTS = 2500000;

	/**
	 * The default alpha value.
	 */
	public static final float DEFAULT_ALPHA = 2.0f;

	public static final float DEFAULT_COFL = 2.0f;
	public static final float DEFAULT_KOFL = 2.0f;

	/**
	 * The default beta value.
	 */
	public static final float DEFAULT_BETA = 216.25f;

	/**
	 * The default gamma value.
	 */
	public static final float DEFAULT_GAMMA = 169f / 4.0f;

	/**
	 * The alpha constant.
	 */
	private final float alpha;

	private final float cofl;

	private final float kofl;

	/**
	 * The beta constant.
	 */
	private final float beta;

	/**
	 * The gamma constant.
	 */
	private final float gamma;

	/**
	 * A list of the first K data points. After the first K data points have
	 * been observed, this list is set to {@code null} and never re-used.
	 */
	public List<float[]> firstKPoints;

	/**
	 * The scaled clustering cost.
	 */
	private float LCost;

	/**
	 * The cost of creating a new cluster.
	 */
	private float facilityCost;

	/**
	 * The total clustering cost.
	 */
	private float totalCost;

	/**
	 * An estimate of the number of data points that will be observed. This is
	 * used for evaluating the clustering cost and number of clusters.
	 */
	private final float logNumPoints;

	/**
	 * The number of clusters desired.
	 */
	private final int numClusters;

	/**
	 * The set of clusters.
	 */
	// private CopyOnWriteArrayList<CentroidCluster> facilities;
	List<CentroidCluster> facilities;
	/**
	 * A counter for generating item identifiers.
	 */
	private final AtomicInteger idCounter;

	/**
	 * The maximum total clustering cost, based on the constant values.
	 */
	public final float costThreshold;

	/**
	 * The maximum number of clusters, based on the constant values.
	 */
	private final float facilityThreshold;

	public class StreamingkmThread implements Runnable {

		long id;
		float[] value;

		public StreamingkmThread(float[] value) {
			this.value = value;
		}

		@Override
		public void run() {
			// Get the id of the new data point.
			int id = idCounter.getAndAdd(1);

			// Try to assign the data point to a cluster. If assigning the data
			// point causes either the cost threshold or number of clusters
			// threshold to be surpassed, try reclustering the centroids and
			// sampled data points, then cluster the data point again. This may
			// take several iterations since the creation of new clusters is
			// done at random.
			if (addDataPoint(value, id) < 0) {
				// Reassign the centroid of each cluster.
				List<CentroidCluster> clusters = facilities;
				facilities = Collections
						.synchronizedList(new ArrayList<CentroidCluster>());
				LCost *= beta;
				facilityCost = LCost / (numClusters * (1 + logNumPoints));

				// When reassigning each centroid, copy over the id of assigned
				// data points to the new cluster.
				for (CentroidCluster cluster : clusters) {
					int assignment = addDataPoint(cluster.centroid(), 0);
					//synchronized (facilities) {
						CentroidCluster newCluster = facilities.get(assignment);
						newCluster.dataPointIds().or(cluster.dataPointIds());
					//}
				}
			}
			this.id = id;

		}

	}

	public long addVectorOnlineStep(float[] value) {
		
//		 if (this.parallel) {
//			 StreamingkmThread r = new StreamingkmThread(value);
//			 executor.execute(r);
//			 return -1;
//		 }

		// Get the id of the new data point.
		int id = idCounter.getAndAdd(1);

		// Try to assign the data point to a cluster. If assigning the data
		// point causes either the cost threshold or number of clusters
		// threshold to be surpassed, try reclustering the centroids and
		// sampled data points, then cluster the data point again. This may
		// take several iterations since the creation of new clusters is
		// done at random.
		if (addDataPoint(value, id) < 0) {
			// Reassign the centroid of each cluster.
			List<CentroidCluster> clusters = facilities;
			facilities = Collections
					.synchronizedList(new ArrayList<CentroidCluster>());
			LCost *= beta;
			facilityCost = LCost / (numClusters * (1 + logNumPoints));

			// When reassigning each centroid, copy over the id of assigned
			// data points to the new cluster.
			for (CentroidCluster cluster : clusters) {
				int assignment = addDataPoint(cluster.centroid(), 0);
				synchronized (facilities) {
					CentroidCluster newCluster = facilities.get(assignment);

					newCluster.dataPointIds().or(cluster.dataPointIds());
				}
			}
		}
		return id;
	}

	/**
	 * Assigns {@code value} to a cluster, or makes a new cluster with
	 * {@code value} as the centroid, and returns the id of the cluster.
	 * {@code -1} is returned if the data point cannot be assigned without
	 * violating either the facility threshold or the cost threshold.
	 *
	 * @param value
	 *            The value to cluster
	 * @param id
	 *            The unique identifier for {@code id}
	 */
	private int addDataPoint(float[] value, int id) {
		// Find the cluster that is closest to value.
		float bestCost = Float.MAX_VALUE;
		int bestClusterId = 0;
		CentroidCluster bestCluster = null;
		int i = 0;
		Iterator<CentroidCluster> it = facilities.iterator();
		while (it.hasNext()) {
			CentroidCluster cluster ;

			cluster = it.next();
			float cost = cluster.compareWithVector(value);
			// Reverse the scale so that a high similarity corresponds to a
			// low cost and a low similarity corresponds to a high cost, but
			// is still from a 0 to 1 range.
			cost = -1 * cost + 1;
			if (cost < bestCost) {
				bestCost = cost;
				bestCluster = cluster;
				bestClusterId = i;
			}
			++i;
		}
		
		// Determine whether or not a new facility, or cluster, should be
		// generated for this data point. This based on the total cost of
		// serving this data point and the cost of creating a new
		// facility.
		float makeFacilityProb = Math.min(bestCost / facilityCost, 1);
		boolean makeFacility = facilities.size() == 0
				|| Math.random() < makeFacilityProb || facilities.size()<this.so.getk();

		if (makeFacility) {

			CentroidCluster newCluster = new CentroidCluster(value);
			newCluster.addVector(value, (id > 0) ? id : -1);
			
			facilities.add(newCluster);
			
			bestClusterId = facilities.size() - 1;
		} else {
			bestCluster.addVector(value, (id > 0) ? id : -1);
			totalCost += bestCost;
		}

		if (id != 0) {
			if (totalCost > gamma * LCost)
				return -1;
			if (facilities.size() >= facilityThreshold)
				return -2;
		}

		return bestClusterId;
	}

	public CentroidCluster getCluster(int clusterIndex) {
		if (facilities.size() <= clusterIndex || clusterIndex < 0)
			throw new ArrayIndexOutOfBoundsException();
		return facilities.get(clusterIndex);
	}

	public List<CentroidCluster> getClusters() {
		return facilities;
	}

	public synchronized int size() {
		return facilities.size();
	}

	@Override
	public List<Centroid> getCentroidsOfflineStep() {
		
		if (this.parallel) {
			executor.shutdown();
			try {
				executor.awaitTermination(10, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			executor = Executors.newFixedThreadPool(getProcessors());
		}
		
		List<Centroid> ret = new ArrayList<>();

		synchronized (facilities) {
			Iterator<CentroidCluster> it = facilities.iterator();
			while (it.hasNext()) {
				CentroidCluster c1 = it.next();

				ret.add(new Centroid(c1.centroid(), 0));
			}
		}
		Clusterer clusterer = so.getOfflineClusterer();
		clusterer.setData(ret);
		clusterer.setK(numClusters);
		
		
		
		return clusterer.getCentroids();

	}

	public static void main(String[] args) throws Exception {

		int k = 20;
		int d = 10000;
		int interval = 100000;
		float var = 1f;

		int processors = Runtime.getRuntime().availableProcessors();
		
		if(args.length==0){
			System.out.println("USAGE: java -jar RPHash.jar [skm,rphash] \n\tOptional Arguments: #threads k n_interval dim");
			System.exit(2);
		}
		
		boolean rphashflag = true;
		if(String.valueOf(args[0]).equalsIgnoreCase("skm"))rphashflag = false;
		
		if (args.length > 1)
			processors = Integer.parseInt(args[1]);
		if (args.length > 2)
			k = Integer.parseInt(args[2]);
		if (args.length > 3)
			interval = Integer.parseInt(args[3]);
		if (args.length > 4)
			d = Integer.parseInt(args[4]);

		Runtime rt = Runtime.getRuntime();
		GenerateStreamData gen = new GenerateStreamData(k, d, var, 1133131,rt.availableProcessors());
		
		StreamClusterer rphit;
		if(rphashflag){
			rphit = new RPHashStream(k,gen,processors);
			System.out
			.printf("Running Streaming RPHash on %d processors, d=%d,k=%d,n=%d,var=%.0f\n",
					rphit.getProcessors(), d, k, interval, var);
		}
		else{
			rphit = new StreamingKmeans(k,gen,processors);
			System.out
			.printf("Running Streaming Kmeans on %d processors, d=%d,k=%d,n=%d,var=%.0f\n",
					rphit.getProcessors(), d, k, interval, var);
		}
		
		ArrayList<float[]> vecsInThisRound = new ArrayList<float[]>();
		
		System.out.printf("Vecs\tMem(KB)\tTime\tWCSSE\n");
		long timestart = System.nanoTime();
		for (int i = 0; i < interval*10; i++) {
			vecsInThisRound.add(gen.generateNext());
			if (i % interval == interval - 1) {

				timestart = System.nanoTime();
				for (float[] f : vecsInThisRound){
					rphit.addVectorOnlineStep(f);
				}
				
				List<Centroid> cents = rphit.getCentroidsOfflineStep();
				long time = System.nanoTime() - timestart;
				rt.gc();
				long usedkB = (rt.totalMemory() - rt.freeMemory()) / 1024;
				double wcsse = StatTests.WCSSECentroidsFloat(cents, vecsInThisRound);
				vecsInThisRound = new ArrayList<float[]>();
				System.out.printf("%d\t%d\t%.4f\t%.4f\n", i, usedkB,
						time / 1000000000f, wcsse);
			}
		}
	}

	@Override
	public int getProcessors() {
		return processors;
	}

	@Override
	public void setWeights(List<Float> counts) {
	}

	@Override
	public void setRawData(List<float[]> data) {
		this.data = data;
	}

	@Override
	public void setData(List<Centroid> centroids) {
		ArrayList<float[]> data = new ArrayList<float[]>(centroids.size());
		for (Centroid c : centroids)
			data.add(c.centroid());
		setRawData(data);
	}

	@Override
	public void setK(int getk) {
	}

	@Override
	public void shutdown() {
		if (so.getParallel()) {
			executor.shutdown();
			try {
				executor.awaitTermination(10, TimeUnit.SECONDS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			executor = Executors.newFixedThreadPool(getProcessors());
		}

	}
	
	@Override
	public void reset(int randomseed) {
		centroids = null;
	}
	
	@Override
	public boolean setMultiRun(int runs) {
		return true;
	}

}

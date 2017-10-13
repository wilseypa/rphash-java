package edu.uc.rphash;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeSet;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import edu.uc.rphash.Readers.RPHashObject;
import edu.uc.rphash.Readers.SimpleArrayReader;
import edu.uc.rphash.projections.Projector;
import edu.uc.rphash.tests.StatTests;
import edu.uc.rphash.tests.clusterers.Agglomerative3;
import edu.uc.rphash.tests.generators.GenerateData;

public class RPHashAdaptive2PassParallel implements Clusterer, Runnable {

	boolean znorm = true;

	private float[] rngvec;
	private List<Centroid> centroids = null;
	private RPHashObject so;
	int threads = 1;

	public RPHashAdaptive2PassParallel(RPHashObject so) {
		this.threads = 4;
		this.so = so;
	}

	public RPHashAdaptive2PassParallel(List<float[]> data, int k, int processors) {
		this.threads = processors;
		so = new SimpleArrayReader(data, k);
	}

	public List<Centroid> getCentroids(RPHashObject so) {
		this.so = so;
		return getCentroids();
	}

	@Override
	public List<Centroid> getCentroids() {
		if (centroids == null)
			run();
		return centroids;
	}

	/*
	 * X - set of vectors compute the medoid of a vector set
	 */
	float[] medoid(List<float[]> X) {
		float[] ret = X.get(0);
		for (int i = 1; i < X.size(); i++) {
			for (int j = 0; j < ret.length; j++) {
				ret[j] += X.get(i)[j];
			}
		}
		for (int j = 0; j < ret.length; j++) {
			ret[j] = ret[j] / ((float) X.size());
		}
		return ret;
	}

	// float[] rngvec; the range vector is moot if incoming data has been
	// normalized
	// post normalization it should all be zero centered, with variance 1

	/*
	 * super simple hash algorithm, reminiscient of pstable lsh
	 */
	public long hashvec(float[] xt, float[] x,
			Map<Long, ArrayList<float[]>> IDAndCent,
			Map<Long, ArrayList<Integer>> IDAndLabel, int ct) {
		long s = 1;// fixes leading 0's bug
		for (int i = 0; i < xt.length; i++) {
			s <<= 1;
			if (xt[i] > rngvec[i])
				s += 1;
			if (IDAndCent.containsKey(s)) {
				if (IDAndLabel.get(s) != null)
					IDAndLabel.get(s).add(ct);
				if (IDAndCent.get(s) != null)
					IDAndCent.get(s).add(x);
			} else {
				ArrayList<float[]> xlist = new ArrayList<>();
				xlist.add(x);
				IDAndCent.put(s, xlist);
				ArrayList<Integer> idlist = new ArrayList<>();
				idlist.add(ct);
				IDAndLabel.put(s, idlist);
			}
		}
		return s;
	}

	/*
	 * x - input vector IDAndCount - ID->count map IDAndCent - ID->centroid
	 * vector map
	 * 
	 * hash the projected vector x and update the hash to centroid and counts
	 * maps
	 */
	void addtocounter(float[] x, Projector p,
			Map<Long, ArrayList<float[]>> IDAndCent,
			Map<Long, ArrayList<Integer>> IDandID, int ct) {
		float[] xt = p.project(x);

		hashvec(xt, x, IDAndCent, IDandID, ct);
	}

	/*
	 * X - data set k - canonical k in k-means l - clustering sub-space Compute
	 * density mode via iterative deepening hash counting
	 */
	public Collection<ArrayList<float[]>> findDensityModes()
			throws InterruptedException, ExecutionException {

		// #create projector matrixs
		Projector projector = so.getProjectionType();
		projector.setOrigDim(so.getdim());
		projector.setProjectedDim(so.getDimparameter());
		projector.setRandomSeed(so.getRandomSeed());
		projector.init();

		// int ct = 1;

		List<float[]> dat = so.getRawData();

		AtomicInteger ct = new AtomicInteger(0);

		ExecutorService executor = Executors.newFixedThreadPool(this.threads);

		int chunksize = dat.size() / this.threads;

		ArrayList<Future<Map<Long, ArrayList<float[]>>>> gather = new ArrayList<>(this.threads);

		for (int i = 0; i < this.threads; i++) {
			int chunk = chunksize* i;
			gather.add(executor.submit(new Callable<Map<Long, ArrayList<float[]>>>() {
				public Map<Long, ArrayList<float[]>> call() {
					Map<Long, ArrayList<float[]>> IDAndCent = new HashMap<>();
					Map<Long, ArrayList<Integer>> IDAndID = new HashMap<>();
					for (int j = chunk; j < chunksize + chunk && j < dat.size(); j++) {
						addtocounter(dat.get(j), projector, IDAndCent, IDAndID,
								ct.incrementAndGet());
					}
					return IDAndCent ;//new Object[] { IDAndCent, IDAndID };
				}
			}));
		}

		List<Map<Long, ArrayList<float[]>>> gatheredCent = new ArrayList<>(this.threads);
//		List<Map<Long, List>> gatheredID = new ArrayList<>(this.threads);
		
//		executor.awaitTermination(10,TimeUnit.SECONDS);
		for (Future<Map<Long, ArrayList<float[]>>> f : gather) {
			Map<Long, ArrayList<float[]>> o = f.get();
			gatheredCent.add(o);
//			gatheredID.add((Map<Long, List>) o[1]);
		}

		executor.shutdown();
		Map<Long, List> IDAndCent = gatheredCent
				.stream()
				.parallel()
				.map(Map::entrySet)
				.flatMap(Collection::stream)
				.collect(
						Collectors.toConcurrentMap(Map.Entry::getKey,
								Map.Entry::getValue,
							    (old, latest)->{
							        old.addAll(latest);
							        return old;
							    }
								));

		// next we want to prune the tree by parent count comparison
		// follows breadthfirst search
		HashMap<Long, Long> denseSetOfIDandCount = new HashMap<Long, Long>();
		for (Long cur_id : new TreeSet<Long>(IDAndCent.keySet())) {
			if (cur_id > so.getk()) {
				int cur_count = IDAndCent.get(cur_id).size();
				long parent_id = cur_id >>> 1;
				int parent_count = IDAndCent.get(parent_id).size();

				if (cur_count != 0 && parent_count != 0) {
					if (cur_count == parent_count) {
						denseSetOfIDandCount.put(parent_id, 0L);
						IDAndCent.put(parent_id, new ArrayList<>());
						denseSetOfIDandCount.put(cur_id, (long) cur_count);
					} else {
						if (2 * cur_count > parent_count) {
							denseSetOfIDandCount.remove(parent_id);
							IDAndCent.put(parent_id, new ArrayList<>());
							denseSetOfIDandCount.put(cur_id, (long) cur_count);
						}
					}
				}
			}
		}

		// remove keys with support less than 1
		Stream<Entry<Long, Long>> stream = denseSetOfIDandCount.entrySet()
				.parallelStream().filter(p -> p.getValue() > 1);
		// 64 so 6 bits?
		// stream = stream.filter(p -> p.getKey() > 64);

		List<Long> sortedIDList = new ArrayList<>();
		// sort and limit the list
		stream.sorted(Entry.<Long, Long> comparingByValue().reversed())
				.limit(so.getk() * 4).parallel()
				.forEachOrdered(x -> sortedIDList.add(x.getKey()));

		// compute centroids

		HashMap<Long, ArrayList<float[]>> estcents = new HashMap<>();
		for (int i = 0; i < sortedIDList.size(); i++) 
		{
			estcents.put(sortedIDList.get(i),
					new ArrayList(IDAndCent.get(sortedIDList.get(i))));
		}

		return estcents.values();
	}

	public void run() {
		rngvec = new float[so.getDimparameter()];
		Random r = new Random(so.getRandomSeed());
		for (int i = 0; i < so.getDimparameter(); i++)
			rngvec[i] = (float) r.nextGaussian();

		Collection<ArrayList<float[]>> clustermembers;
		try {
			clustermembers = findDensityModes();

			List<float[]> centroids = new ArrayList<>();

			List<Float> weights = new ArrayList<>();
			int k = clustermembers.size() > 200 + so.getk() ? 200 + so.getk()
					: clustermembers.size();

			for (List<float[]> cl : clustermembers) {
				weights.add(new Float(cl.size()));
				centroids.add(medoid(cl));
			}

			Agglomerative3 aggloOffline = new Agglomerative3(centroids,
					so.getk());
			aggloOffline.setWeights(weights);
			this.centroids = aggloOffline.getCentroids();
		} catch (InterruptedException | ExecutionException e) {

			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws FileNotFoundException,
			IOException {

		int k = 10;
		int d = 1000;
		int n = 10000;
		float var = 0.1f;
		int count = 10;
		System.out.printf("ClusterVar\t");
		for (int i = 0; i < count; i++)
			System.out.printf("Trial%d\t", i);
		System.out.printf("RealWCSS\n");

		for (float f = var; f < 5.01; f += .05f) {
			float avgrealwcss = 0;
			float avgtime = 0;
			System.out.printf("%f\t", f);
			for (int i = 0; i < count; i++) {
				GenerateData gen = new GenerateData(k, n / k, d, f, true, .5f);
				// gen.writeCSVToFile(new
				// File("/home/lee/Desktop/reclsh/in.csv"));
				RPHashObject o = new SimpleArrayReader(gen.data, k);
				o.setDimparameter(32);
				
				RPHashAdaptive2PassParallel rphit = new RPHashAdaptive2PassParallel(
						o);
				long startTime = System.nanoTime();
				List<Centroid> centsr = rphit.getCentroids();

				avgtime += (System.nanoTime() - startTime) / 100000000;

				avgrealwcss += StatTests.WCSSEFloatCentroid(gen.getMedoids(),
						gen.getData());

				System.out.printf("%.0f\t",
						StatTests.WCSSECentroidsFloat(centsr, gen.data));
				System.gc();
			}
			System.out.printf("%.0f\n", avgrealwcss / count);
		}
	}

	@Override
	public RPHashObject getParam() {
		return so;
	}

	@Override
	public void setWeights(List<Float> counts) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setData(List<Centroid> centroids) {
		this.centroids = centroids;

	}

	@Override
	public void setRawData(List<float[]> centroids) {
		if (this.centroids == null)
			this.centroids = new ArrayList<>(centroids.size());
		for (float[] f : centroids) {
			this.centroids.add(new Centroid(f, 0));
		}
	}

	@Override
	public void setK(int getk) {
		this.so.setK(getk);
	}

	@Override
	public void reset(int randomseed) {
		centroids = null;
		so.setRandomSeed(randomseed);
	}

	@Override
	public boolean setMultiRun(int runs) {
		return false;
	}
}

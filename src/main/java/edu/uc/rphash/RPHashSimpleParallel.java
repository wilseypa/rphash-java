package edu.uc.rphash;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import edu.uc.rphash.Readers.RPHashObject;
import edu.uc.rphash.Readers.SimpleArrayReader;
import edu.uc.rphash.decoders.Decoder;
import edu.uc.rphash.decoders.Spherical;
import edu.uc.rphash.frequentItemSet.ItemSet;
import edu.uc.rphash.frequentItemSet.SimpleFrequentItemSet;
import edu.uc.rphash.lsh.LSH;
//import edu.uc.rphash.projections.DBFriendlyProjection;
import edu.uc.rphash.projections.Projector;
import edu.uc.rphash.standardhash.HashAlgorithm;
import edu.uc.rphash.standardhash.NoHash;
import edu.uc.rphash.tests.clusterers.KMeans2;
import edu.uc.rphash.tests.generators.GenerateData;
import edu.uc.rphash.util.VectorUtil;

public class RPHashSimpleParallel implements Clusterer {
	// float variance;

	public ItemSet<Long> is;

	List<Long> labels;
	HashMap<Long, Long> labelmap;

	public static void mapfunc(float[] vec, LSH lshfunc, ItemSet<Long> is) {

		long hash = lshfunc.lshHash(vec);
		is.add(hash);
	}

	public RPHashObject map() {
		

		// create our LSH Machine
		HashAlgorithm hal = new NoHash(so.getHashmod());
		Iterator<float[]> vecs = so.getVectorIterator();
		if (!vecs.hasNext())
			return so;

		int logk = (int) (.5 + Math.log(so.getk()) / Math.log(2));// log k and
																	// round to
																	// integer
		int k = so.getk() * logk;
		is = new SimpleFrequentItemSet<Long>(k);
		Decoder dec = so.getDecoderType();
		dec.setCounter(is);

		Projector p = so.getProjectionType();
		p.setOrigDim(so.getdim());
		p.setProjectedDim(dec.getDimensionality());
		p.setRandomSeed(so.getRandomSeed());
		p.init();
		// no noise to start with
		List<float[]> noise = LSH.genNoiseTable(
				dec.getDimensionality(),
				so.getNumBlur(),
				new Random(),
				dec.getErrorRadius()
						/ (dec.getDimensionality() * dec.getDimensionality()));

		LSH lshfunc = new LSH(dec, p, hal, noise, so.getNormalize());

		// add to frequent itemset the hashed Decoded randomly projected vector

	
		List<float[]> dat = so.getRawData();

		ExecutorService executor = Executors.newFixedThreadPool(this.threads);

		int chunksize = dat.size() / this.threads;

		ArrayList<Future<ArrayList<Centroid>>> gather = new ArrayList<>(this.threads);

		for (int i = 0; i < this.threads; i++) {
			int chunk = chunksize* i;
			gather.add(executor.submit(new Callable< ArrayList<Centroid>>() {
				public ArrayList<Centroid> call() {
					
					ArrayList<Centroid> centroids = new ArrayList<Centroid>();
					for (int j = chunk; j < chunksize + chunk && j < dat.size(); j++) {
						mapfunc(dat.get(j), lshfunc, is);
					}
					return centroids;
				}
			}));
		}
		
		
		for (Future<ArrayList<Centroid>> f : gather){
			try {
				f.get();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		executor.shutdown();

		List<Long> topids = is.getTop();
		so.setPreviousTopID(topids);

		List<Long> topsizes = is.getCounts();

		List<Float> countsAsFloats = new ArrayList<Float>();
		for (long ct : topsizes)
			countsAsFloats.add((float) ct);
		so.setCounts(countsAsFloats);
		return so;
	}

	public static void redFunc(float[] vec, LSH lshfunc, List<float[]> noise,
			List<Long> labels, List<Centroid> centroids) {
		long[] hash = lshfunc.lshHashRadius(vec, noise);
		labels.add(-1l);
		// radius probe around the vector
		for (Centroid cent : centroids) {
			for (long h : hash) {
				if (cent.ids.contains(h)) {
					cent.updateVec(vec);
					labels.set(labels.size() - 1, cent.id);
				}
			}
		}
	}

	/*
	 * This is the second phase after the top ids have been in the reduce phase
	 * aggregated
	 */
	public RPHashObject reduce() {

		Iterator<float[]> vecs = so.getVectorIterator();
		if (!vecs.hasNext())
			return so;
		float[] vec = vecs.next();

		HashAlgorithm hal = new NoHash(so.getHashmod());
		Decoder dec = so.getDecoderType();

		Projector p = so.getProjectionType();
		p.setOrigDim(so.getdim());
		p.setProjectedDim(dec.getDimensionality());
		p.setRandomSeed(so.getRandomSeed());
		p.init();

		List<float[]> noise = LSH.genNoiseTable(
				so.getdim(),
				so.getNumBlur(),
				new Random(so.getRandomSeed()),
				(float) (dec.getErrorRadius())
						/ (float) (dec.getDimensionality() * dec
								.getDimensionality()));

		LSH lshfunc = new LSH(dec, p, hal, noise, so.getNormalize());
		List<Centroid> centroids = new ArrayList<Centroid>();

		for (long id : so.getPreviousTopID()) {
			centroids.add(new Centroid(so.getdim(), id, -1));
		}

		this.labels = new ArrayList<>();


		while (vecs.hasNext()) {
			redFunc(vecs.next(), lshfunc, noise, labels, centroids);
		}
		
		
		List<float[]> dat = so.getRawData();

		ExecutorService executor = Executors.newFixedThreadPool(this.threads);

		int chunksize = dat.size() / this.threads;

		ArrayList<Future<ArrayList<Centroid>>> gather = new ArrayList<>(this.threads);

		for (int i = 0; i < this.threads; i++) {
			int chunk = chunksize* i;
			gather.add(executor.submit(new Callable< ArrayList<Centroid>>() {
				public ArrayList<Centroid> call() {
					ArrayList<Centroid> centroids = new ArrayList<Centroid>();
					for (int j = chunk; j < chunksize + chunk && j < dat.size(); j++) {
						redFunc(dat.get(j), lshfunc, noise, labels, centroids);
					}
					return centroids;
				}
			}));
		}
		
		
		
		for (Future<ArrayList<Centroid>> f : gather) {
			ArrayList<Centroid> o;
			try {
				o = f.get();
				centroids.addAll(o);
			} catch (InterruptedException e) {

				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}

		}

		executor.shutdown();
		

		Clusterer offlineclusterer = so.getOfflineClusterer();
		offlineclusterer.setData(centroids);
		offlineclusterer.setWeights(so.getCounts());
		offlineclusterer.setK(so.getk());
		this.centroids = offlineclusterer.getCentroids();
		this.labelmap = VectorUtil.generateIDMap(centroids, this.centroids);
		so.setCentroids(centroids);
		return so;
	}

	// 271458
	// 264779.7

	public List<Long> getLabels() {
		for (int i = 0; i < labels.size(); i++) {
			if (labelmap.containsKey(labels.get(i))) {
				labels.set(i, labelmap.get(labels.get(i)));
			} else {
				labels.set(i, -1l);
			}
		}
		return this.labels;
	}

	private List<Centroid> centroids = null;
	private RPHashObject so;

	public RPHashSimpleParallel(List<float[]> data, int k) {
		so = new SimpleArrayReader(data, k);
	}

	int threads = 1;

	public RPHashSimpleParallel(List<float[]> data, int k, int processors) {
		// System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism",String.valueOf(processors));
		threads = processors;
		so = new SimpleArrayReader(data, k);
		so.setParallel(true);
	}

	public RPHashSimpleParallel(List<float[]> data, int k, int times, int rseed) {
		so = new SimpleArrayReader(data, k);
	}

	public RPHashSimpleParallel(RPHashObject so) {
		this.so = so;
	}

	public List<Centroid> getCentroids(RPHashObject so) {
		this.so = so;
		if (centroids == null)
			run();
		return centroids;
	}

	@Override
	public List<Centroid> getCentroids() {
		if (centroids == null)
			run();

		return centroids;
	}

	private void run() {
		map();
		reduce();
		this.centroids = so.getCentroids();
	}

	public static void main(String[] args) {
		int k = 10;
		int d = 1000;
		int n = 10000;
		float var = 1f;
		int count = 5;
		System.out.printf("Decoder: %s\n", "Sphere");
		System.out.printf("ClusterVar\t");
		for (int i = 0; i < count; i++)
			System.out.printf("Trial%d\t", i);
		System.out.printf("RealWCSS\n");

		for (float f = var; f < 3.01; f += .05f) {
			float avgrealwcss = 0;
			float avgtime = 0;
			System.out.printf("%f\t", f);
			for (int i = 0; i < count; i++) {
				GenerateData gen = new GenerateData(k, n / k, d, f, true, 1f);
				RPHashObject o = new SimpleArrayReader(gen.data, k);
				RPHashSimpleParallel rphit = new RPHashSimpleParallel(o);
				rphit.threads = 4;
				o.setDecoderType(new Spherical(32, 4, 1));
				// o.setDimparameter(31);
				o.setOfflineClusterer(new KMeans2());
				long startTime = System.nanoTime();
				List<Centroid> centsr = rphit.getCentroids();
				avgtime += (System.nanoTime() - startTime) / 100000000;

				// avgrealwcss += StatTests.WCSSEFloatCentroid(gen.getMedoids(),
				// gen.getData());

				// System.out.printf("%.0f\t",
				// StatTests.WCSSECentroidsFloat(centsr, gen.data));
				// System.gc();

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
		// TODO Auto-generated method stub

	}

	@Override
	public void reset(int randomseed) {
		centroids = null;
		so.setRandomSeed(randomseed);
	}

	@Override
	public boolean setMultiRun(int runs) {
		return true;
	}
}

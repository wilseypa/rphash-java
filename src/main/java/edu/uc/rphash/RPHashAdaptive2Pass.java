package edu.uc.rphash;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.apache.commons.lang3.ArrayUtils;

import edu.uc.rphash.Readers.RPHashObject;
import edu.uc.rphash.Readers.SimpleArrayReader;
import edu.uc.rphash.projections.DBFriendlyProjection;
import edu.uc.rphash.projections.Projector;
import edu.uc.rphash.tests.StatTests;
import edu.uc.rphash.tests.clusterers.Agglomerative3;
import edu.uc.rphash.tests.generators.GenerateData;
import edu.uc.rphash.util.VectorUtil;

public class RPHashAdaptive2Pass implements Clusterer, Runnable {

	private List<Centroid> centroids = null;
	private RPHashObject so;

	public RPHashAdaptive2Pass(RPHashObject so) {
		this.so = so;
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
			ret[j] = ret[j] / ((float)X.size());
		}
		return ret;
	}

	/*
	 * super simple hash algorithm, reminiscient of pstable lsh
	 */
	public long hashvec(float[] x) {
		long s = 0;
		for (int i = 0; i < x.length; i++) {
			s <<= 1;
			if (x[i] > 0)
				s += 1;
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
			HashMap<Long, List<float[]>> IDAndCent, int l) {
		float[] xt = p.project(x);
		long s = hashvec(xt);

		for (int i = 0; i < l; i++) {
			long partialhash = s >>> i;
			if (IDAndCent.containsKey(partialhash)) {
				IDAndCent.get(partialhash).add(x);
			} else {
				List<float[]> xlist = new ArrayList<float[]>();
				xlist.add(x);
				IDAndCent.put(partialhash, xlist);
			}
		}
	}
	

	/*
	 * X - data set k - canonical k in k-means l - clustering sub-space Compute
	 * density mode via iterative deepening hash counting
	 */
	public ArrayList<float[]> findDensityModes() {
		//HashMap<Long, Long> IDAndCount = new HashMap<>();
		HashMap<Long, List<float[]>> IDAndCent = new HashMap<>();

		// #create projector matrixs
		DBFriendlyProjection projector = new DBFriendlyProjection(so.getdim(),
				so.getDimparameter());
		
		//VectorUtil.simpleSave(projector.M,"/home/lee/Desktop/reclsh/M");
		//VectorUtil.simpleSave(projector.P,"/home/lee/Desktop/reclsh/P");

		// #process data by adding to the counter
		for (float[] x : so.getRawData()) {
			addtocounter(x, projector, IDAndCent,
					so.getDimparameter());
		}

		// next we want to prune the tree by parent count comparison
		// follows breadthfirst search
		HashMap<Long, Long> denseSetOfIDandCount = new HashMap<Long, Long>();
		for (Long h : new TreeSet<Long>(IDAndCent.keySet())) {
			if (h > 1 && 2 * IDAndCent.get(h).size() > IDAndCent.get(h >>> 1).size()) {
				denseSetOfIDandCount.put(h, new Long(IDAndCent.get(h).size()));
				// remove parent of denser child
				denseSetOfIDandCount.remove(h >>> 1);
			}
		}

		ArrayList<float[]> estcents = new ArrayList<float[]>();
		denseSetOfIDandCount
				.entrySet()
				.stream()
				.sorted(Map.Entry.<Long, Long> comparingByValue().reversed())
				.limit(so.getk())
				.forEachOrdered(
						x -> estcents.add(medoid(IDAndCent.get(x.getKey()))));
		//estcents.stream().forEach(x->VectorUtil.prettyPrint(x));
		return estcents;
	}

	public void run() {
		List<float[]> rawcent = findDensityModes();
		centroids = new ArrayList<>();
		for (float[] f : rawcent)
			centroids.add(new Centroid(f, 1));
		 //centroids = new Agglomerative3(rawcent, so.getk()).getCentroids();
	}

	public static void main(String[] args) throws FileNotFoundException, IOException {

		int k = 10;
		int d = 1000;
		int n = 10000;
		float var = 1.f;
		for (float f = var; f < 1.01; f += .1f) {
			for (int i = 0; i < 1; i++) {
				GenerateData gen = new GenerateData(k, n / k, d, f, true, 1f);
				//gen.writeCSVToFile(new File("/home/lee/Desktop/reclsh/in.csv"));
				RPHashObject o = new SimpleArrayReader(gen.data, k);
				o.setDimparameter(32);
				RPHashAdaptive2Pass rphit = new RPHashAdaptive2Pass(o);
				long startTime = System.nanoTime();
				List<Centroid> centsr = rphit.getCentroids();
				long duration = (System.nanoTime() - startTime);
				double realwcss = StatTests.WCSSEFloatCentroid(
						gen.getMedoids(), gen.getData());
				System.out.printf("%f : %f : %f : %f\n", f,
						StatTests.WCSSECentroidsFloat(centsr, gen.data),
						realwcss, duration / 1000000000.0);
				System.gc();
			}
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
		return false;
	}
}

package edu.uc.rphash.tests;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class GenerateData implements ClusterGenerator {
	final RandomDistributionFnc genfnc;
	int numClusters;
	int numVectorsPerCluster;
	int dimension;
	Random r;
	List<float[]> data;
	List<float[]> medoids;
	List<float[]> variances;
	List<Integer> reps;
	float scaler;
	boolean shuffle;
	float sparseness;
	private boolean noised = true;

	public GenerateData(int numClusters, int numVectorsPerCluster, int dimension) {
		r = new Random();
		this.numClusters = numClusters;
		this.numVectorsPerCluster = numVectorsPerCluster;
		this.dimension = dimension;
		this.shuffle = true;
		this.medoids = null;
		this.data = null;
		this.reps = null;
		this.scaler = 1.0f / (float) Math.sqrt(dimension);;//(float) Math.sqrt(dimension);// normalize dimension
		this.sparseness = 1.0f;
		this.genfnc = new RandomDistributionFnc() {
			@Override
			public float genVariate() {
				return (float) r.nextGaussian();
			}
		};
		generateMem();
	}

	public GenerateData(int numClusters, int numVectorsPerCluster,
			int dimension, boolean shuffle) {
		r = new Random();
		this.numClusters = numClusters;
		this.numVectorsPerCluster = numVectorsPerCluster;
		this.dimension = dimension;
		this.medoids = null;
		this.data = null;
		this.reps = null;
		this.scaler = 1.0f / (float) Math.sqrt(dimension);;//(float) Math.sqrt(dimension);// normalize dimension
		this.shuffle = shuffle;
		this.sparseness = 1.0f;
		this.genfnc = new RandomDistributionFnc() {
			@Override
			public float genVariate() {
				return (float) r.nextGaussian();
			}
		};
		generateMem();
	}

	public GenerateData(int numClusters, int numVectorsPerCluster,
			int dimension, float variance) {
		r = new Random();
		this.numClusters = numClusters;
		this.numVectorsPerCluster = numVectorsPerCluster;
		this.dimension = dimension;
		this.medoids = null;
		this.data = null;
		this.reps = null;
		this.shuffle = true;
		this.sparseness = 1.0f;
		if (variance > 0)
			this.scaler = variance;// normalize dimension
		else
			this.scaler = 1.0f ;

		this.genfnc = new RandomDistributionFnc() {
			@Override
			public float genVariate() {
				return (float) r.nextGaussian();
			}
		};
		generateMem();
	}

	public GenerateData(int numClusters, int numVectorsPerCluster,
			int dimension, float variance, boolean shuffle) {
		r = new Random();
		this.numClusters = numClusters;
		this.numVectorsPerCluster = numVectorsPerCluster;
		this.dimension = dimension;
		this.medoids = null;
		this.data = null;
		this.reps = null;
		this.shuffle = shuffle;
		this.sparseness = 1.0f;
		if (variance > 0)
			this.scaler = variance;// normalize dimension
		else
			this.scaler = 1.0f ;

		this.genfnc = new RandomDistributionFnc() {
			@Override
			public float genVariate() {
				return (float) r.nextGaussian();
			}
		};

		generateMem();
	}

	public GenerateData(int numClusters, int numVectorsPerCluster,
			int dimension, float variance, boolean shuffle, float sparseness) {
		r = new Random();
		this.numClusters = numClusters;
		this.numVectorsPerCluster = numVectorsPerCluster;
		this.dimension = dimension;
		this.medoids = null;
		this.data = null;
		this.reps = null;
		this.shuffle = shuffle;
		this.sparseness = sparseness;
		if (variance > 0)
			this.scaler = variance;// /(float)Math.sqrt(dimension);//normalize
									// dimension
		else
			this.scaler = 1.0f ;

		this.genfnc = new RandomDistributionFnc() {
			@Override
			public float genVariate() {
				return (float) r.nextGaussian();
			}
		};

		generateMem();
	}

	public GenerateData(int numClusters, int numVectorsPerCluster,
			int dimension, RandomDistributionFnc genvariate) {
		r = new Random();
		this.numClusters = numClusters;
		this.numVectorsPerCluster = numVectorsPerCluster;
		this.dimension = dimension;
		this.genfnc = genvariate;
		this.scaler = 1.0f ;// normalize dimension
		this.medoids = null;
		this.data = null;
		this.shuffle = true;
		this.sparseness = 1.0f;
		generateMem();
	}

	public GenerateData(int numClusters, int numVectorsPerCluster,
			int dimension, File f) {
		r = new Random();
		this.numClusters = numClusters;
		this.numVectorsPerCluster = numVectorsPerCluster;
		this.dimension = dimension;
		this.scaler = 1.0f;// normalize dimension
		this.medoids = new ArrayList<float[]>();
		this.data = new ArrayList<float[]>();
		this.shuffle = true;
		this.sparseness = 1.0f;
		this.genfnc = new RandomDistributionFnc() {
			@Override
			public float genVariate() {
				return (float) r.nextGaussian();
			}
		};
		generateDisk(f);
	}

	public GenerateData(int numClusters, int numVectorsPerCluster,
			int dimension, File f, RandomDistributionFnc genvariate) {
		r = new Random();
		this.numClusters = numClusters;
		this.numVectorsPerCluster = numVectorsPerCluster;
		this.dimension = dimension;
		this.scaler = 1.0f ;// normalize dimension
		this.genfnc = genvariate;
		this.shuffle = true;
		this.sparseness = 1.0f;
		generateDisk(f);
	}

	private void permute() {

		reps = new ArrayList<Integer>(data.size());
		ArrayList<float[]> newData = new ArrayList<float[]>(data.size());
		for (int i = 0; i < data.size(); i++)
			reps.add(i);

		Collections.shuffle(reps, r);

		for (int i = 0; i < reps.size(); i++) {
			newData.add(data.get(reps.get(i)));
			reps.set(i,
					(int) ((float) reps.get(i) / (float) numVectorsPerCluster));
		}
		data = newData;
	}

	void normalize() {
		for (int i = 0; i < dimension; i++) {
			float sum = 0.0f;
			for (float[] f : data) {
				sum += Math.abs(f[i]);
			}
			sum /= (float) data.size();
			for (float[] f : data) {
				f[i] /= (sum);
			}
			for (float[] f : medoids) {
				f[i] /= (sum);
			}
		}
	}

	public void generateMem() {
		this.data = new ArrayList<float[]>();// new
												// float[numClusters*numVectorsPerCluster][dimension];
		this.medoids = new ArrayList<float[]>();// new
												// float[numClusters][dimension];

		// float
		// float maxval = 0.0f;
		for (int i = 0; i < numClusters; i++) {
			// gen cluster center
			float[] medoid = new float[dimension];
			float[] variances = new float[dimension];

			for (int k = 0; k < dimension; k++) {
				if (r.nextInt() % (int) (1.0f / sparseness) == 0) {
					medoid[k] = r.nextFloat() * 2.0f - 1.0f;
					variances[k] = scaler * r.nextFloat();
				}

			}
			this.medoids.add(medoid);
			// gen data
			for (int j = 0; j < numVectorsPerCluster; j++) {
				float[] dat = new float[dimension];
				for (int k = 0; k < dimension; k++) {
					if (r.nextInt() % (int) (1.0f / sparseness) == 0)
						dat[k] = (float) (medoid[k] + genfnc.genVariate()*variances[k]);
				}
				this.data.add(dat);
			}
		}

		// normalize();
		if (this.shuffle) {
			permute();
		}

	}

	public int getClusterID(int vecIdx) {
		return reps.get(vecIdx);
	}

	public void writeToFile(File f) {
		try {
			BufferedWriter bf = new BufferedWriter(new FileWriter(f));
			int l = 0;
			for (int i = 0; i < numClusters; i++) {
				float[] medoid = medoids.get(i);
				for (int k = 0; k < dimension; k++) {
					bf.write(String.valueOf(medoid[k]));
					bf.write(' ');
				}
				bf.write('\n');
				// gen data
				for (int j = 0; j < numVectorsPerCluster; j++) {
					float[] vec = data.get(l);
					for (int k = 0; k < dimension; k++) {
						bf.write(String.valueOf(vec[k]));
						bf.write(' ');
					}
					bf.write('\n');
					l++;
				}
				bf.flush();

			}
			bf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void generateDisk(File f) {
		try {
			BufferedWriter bf = new BufferedWriter(new FileWriter(f));

			bf.write(String.valueOf(numClusters * numVectorsPerCluster));
			bf.write('\n');
			bf.write(String.valueOf(dimension));
			bf.write('\n');

			float[][] medoids = new float[numClusters][dimension];
			float[][] variances = new float[numClusters][dimension];
			for (int i = 0; i < numClusters; i++) {
				// gen cluster center
				for (int k = 0; k < dimension; k++) {
					medoids[i][k] = r.nextFloat() * 2.0f - 1.0f;
					variances[i][k] = scaler * (r.nextFloat());

				}
			}
			// gen data
			for (int j = 0; j < numVectorsPerCluster*numClusters; j++) {
				int clusteridx = r.nextInt(numClusters);
				for (int k = 0; k < dimension; k++) {
					bf.write(String.valueOf(medoids[clusteridx][k] * r.nextGaussian()*variances[clusteridx][k]));
					bf.write('\n');
				}
			}
			bf.flush();

			bf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public List<float[]> data() {
		if (data == null)
			generateMem();
		return data;
	}

	public List<float[]> medoids() {
		if (medoids == null)
			generateMem();
		return medoids;
	}

	@Override
	public List<float[]> getMedoids() {
		return medoids;
	}

	@Override
	public List<float[]> getData() {

		return data;
	}

	@Override
	public List<Integer> getLabels() {
		// TODO Auto-generated method stub
		return reps;
	}

	@Override
	public int getDimension() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Iterator<float[]> getIterator() {
		return data.iterator();
	}

	public static void main(String[] args) {
		new GenerateData(10, 10000, 200, new File(args[0]));

	}

}

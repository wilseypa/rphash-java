package edu.uc.rphash.tests;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import edu.uc.rphash.Clusterer;

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
		this.scaler = 0.75f / (float) Math.sqrt(dimension);
		;// (float) Math.sqrt(dimension);// normalize dimension
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
		this.scaler = .75f / (float) Math.sqrt(dimension);
		;// (float) Math.sqrt(dimension);// normalize dimension
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
			this.scaler = .750f;

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
			this.scaler = 0.750f;

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
			this.scaler = .750f;

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
		this.scaler = .75f;// normalize dimension
		this.medoids = null;
		this.data = null;
		this.shuffle = true;
		this.sparseness = 1.0f;
		generateMem();
	}

	public GenerateData(int numClusters, int numVectorsPerCluster,
			int dimension, float variance, boolean shuffle, float sparseness,
			File f,File lblFile,boolean raw) {
		r = new Random();
		this.numClusters = numClusters;
		this.numVectorsPerCluster = numVectorsPerCluster;
		this.dimension = dimension;
		this.scaler = variance;// normalize dimension
		this.medoids = new ArrayList<float[]>();
		this.data = new ArrayList<float[]>();
		this.shuffle =shuffle;
		this.sparseness = sparseness;
		this.genfnc = new RandomDistributionFnc() {
			@Override
			public float genVariate() {
				return (float) r.nextGaussian();
			}
		};
		if(!raw)
			generateDisk(f,lblFile);
		else
			generateDiskRaw(f,lblFile);
	}

	public GenerateData(int numClusters, int numVectorsPerCluster,
			int dimension, File f,File lblFile, RandomDistributionFnc genvariate,boolean raw) {
		r = new Random();
		this.numClusters = numClusters;
		this.numVectorsPerCluster = numVectorsPerCluster;
		this.dimension = dimension;
		this.scaler = .75f;// normalize dimension
		this.genfnc = genvariate;
		this.shuffle = true;
		this.sparseness = 1.0f;
		if(!raw)
			generateDisk(f,lblFile);
		else
			generateDiskRaw(f,lblFile);
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
						dat[k] = (float) (medoid[k] + genfnc.genVariate()
								* variances[k]);
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
	
	
	private void generateDiskRaw(File f,File lbl) {
		try {
			DataOutputStream bf = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(f)));
			DataOutputStream bflbl = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(lbl)));
			
			bflbl.writeInt(numClusters * numVectorsPerCluster);
			bflbl.writeInt(1);

			bf.writeInt(numClusters * numVectorsPerCluster);
			bf.writeInt(dimension);

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
			for (int j = 0; j < numVectorsPerCluster * numClusters; j++) {
				int clusteridx = r.nextInt(numClusters);
				bflbl.writeInt(clusteridx);
				bflbl.write('\n');
				for (int k = 0; k < dimension; k++) {
					if(r.nextFloat()<this.sparseness){
						
						bf.writeFloat( (float) (medoids[clusteridx][k]
							* r.nextGaussian() * variances[clusteridx][k]));
					}
					else{
						bf.writeFloat(0.0f);
					}
				}
			}
			bf.flush();
			bf.close();
			
			bflbl.flush();
			bflbl.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void generateDisk(File f,File lbl) {
		try {
			BufferedWriter bf = new BufferedWriter(new FileWriter(f));
			BufferedWriter bflbl = new BufferedWriter(new FileWriter(lbl));
			
			bflbl.write(String.valueOf(numClusters * numVectorsPerCluster));
			bflbl.write('\n');
			bflbl.write(String.valueOf(1));
			bflbl.write('\n');
			
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
			for (int j = 0; j < numVectorsPerCluster * numClusters; j++) {
				int clusteridx = r.nextInt(numClusters);
				bflbl.write(String.valueOf(clusteridx));
				bflbl.write('\n');
				for (int k = 0; k < dimension; k++) {
					if(r.nextFloat()<this.sparseness){
						
						bf.write(String.valueOf((float)(medoids[clusteridx][k]
							* r.nextGaussian() * variances[clusteridx][k])));
					}
					else{
						bf.write(String.valueOf(0.0f));
					}
					bf.write('\n');
				}
			}
			bf.flush();
			bf.close();
			
			bflbl.flush();
			bflbl.close();
			
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

	public static Map<String, String> argsUI(String[] args,
			List<String> truncatedArgs) {

		Map<String, String> cmdMap = new HashMap<String, String>();
		for (String s : args) {
			String[] cmd = s.split("=");
			if (cmd.length > 1)
				cmdMap.put(cmd[0].toLowerCase(), cmd[1].toLowerCase());
			else
				truncatedArgs.add(s);
		}
		
		args = new String[truncatedArgs.size()];
		for (int i = 0; i < truncatedArgs.size(); i++)
			args[i] = truncatedArgs.get(i);
		
		return cmdMap;
	}

	public static void main(String[] args) throws NumberFormatException,
			IOException, InterruptedException {

		if( args.length < 0) {
			System.out
					.print(""
							+ "Usage: genData outputfile "
							+ "[numDimensions=1000 numClusters=10 "
							+ "numVectors=20000 variance=1.0 sparseness=1.0 "
							+ "shuffled=true]");
			System.exit(0);
		}

		List<String> truncatedArgs = new ArrayList<String>();
		Map<String, String> taggedArgs = argsUI(args, truncatedArgs);

		int k = 10;
		int d = 1000;
		int n = 20000;
		float var = 1f;
		float sparseness = 1f;
		boolean shuffle = true;
		boolean raw = false;
		
		if(taggedArgs.containsKey("numdimensions"))d = Integer.parseInt(taggedArgs.get("numdimensions"));
		if(taggedArgs.containsKey("numclusters"))k = Integer.parseInt(taggedArgs.get("numclusters"));
		if(taggedArgs.containsKey("numvectors"))n = Integer.parseInt(taggedArgs.get("numvectors"));
		if(taggedArgs.containsKey("variance"))var = Float.parseFloat(taggedArgs.get("variance"));
		if(taggedArgs.containsKey("sparseness"))sparseness = Float.parseFloat(taggedArgs.get("sparseness"));
		if(taggedArgs.containsKey("shuffled"))shuffle = Boolean.parseBoolean(taggedArgs.get("shuffled"));
		if(taggedArgs.containsKey("raw"))raw = Boolean.parseBoolean(taggedArgs.get("raw"));
		
		File outputFile = new File(args[0]+"_"+k+"x"+d+"x"+n+".mat");
		File lblFile = new File(args[0]+"_"+k+"x"+d+"x"+n+".lbl");

		System.out.printf("k=%d, n=%d, d=%d, var=%f, sparseness=%f %s > %s",k,n,
				d, var, sparseness, shuffle ? "shuffled" : "", 
				outputFile.getAbsolutePath()+"\n");

		
		GenerateData gen = new GenerateData(k, n/k , d, var, shuffle,
				sparseness,outputFile,lblFile,raw);


		
	}
	
	

}

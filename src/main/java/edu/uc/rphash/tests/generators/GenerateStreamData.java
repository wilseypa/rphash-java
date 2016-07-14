package edu.uc.rphash.tests.generators;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GenerateStreamData implements ClusterGenerator {

	protected int numClusters;
	protected int dimension;
	protected Random r;
	protected List<float[]> data;
	public List<float[]> medoids;
	protected List<float[]> variances;
	protected List<Integer> reps;
	protected float variance_scaler;
	protected boolean shuffle;
	protected float sparseness;
	protected boolean save;
	protected long size;
	protected float avgvariance;

	public GenerateStreamData(int numClusters, int dimension, float variance, long randomseed,
			boolean save) {
		this.r = new Random();
		this.numClusters = numClusters;
		this.dimension = dimension;
		this.shuffle = true;
		this.medoids = new ArrayList<float[]>();
		this.data = new ArrayList<float[]>();
		this.reps = new ArrayList<Integer>();
		this.avgvariance = variance;
		this.variance_scaler = (variance / (float) Math.sqrt(dimension));// normalize
		this.variances = new ArrayList<float[]>();															// dimension
		this.sparseness = 1.0f;
		this.generateMedoids();
		this.save = save;
	}

	public GenerateStreamData(int numClusters, int dimension, float variance, long randomseed) {
		this.r = new Random(randomseed);
		this.numClusters = numClusters;
		this.dimension = dimension;
		this.shuffle = true;
		this.medoids = new ArrayList<float[]>();
		
		this.data = new ArrayList<float[]>();
		this.reps = new ArrayList<Integer>();
		this.variances = new ArrayList<float[]>();
		this.variance_scaler = (variance / (float) Math.sqrt(dimension));// normalize
																		// dimension
		this.sparseness = 1.0f;
		this.save = false;
		this.generateMedoids();
	
	}
	public GenerateStreamData(int numClusters, int dimension, float variance) {
		this.r = new Random();
		this.numClusters = numClusters;
		this.dimension = dimension;
		this.shuffle = true;
		this.medoids = new ArrayList<float[]>();
		
		this.data = new ArrayList<float[]>();
		this.reps = new ArrayList<Integer>();
		this.variances = new ArrayList<float[]>();
		this.variance_scaler = (variance / (float) Math.sqrt(dimension));// normalize
																		// dimension
		this.sparseness = 1.0f;
		this.save = false;
		this.generateMedoids();

	}

	public void generateMedoids() {
		if (save) {
			reps = new ArrayList<>();
			data = new ArrayList<>();
		}
		size = 0;
		for (int i = 0; i < numClusters; i++) {
			// gen cluster center
			float[] medoid = new float[dimension];
			float[] variance = new float[dimension];

			for (int k = 0; k < dimension; k++) {
				if (r.nextInt() % (int) (1.0f / sparseness) == 0) {
					medoid[k] = r.nextFloat() * 2.0f - 1.0f;
					variance[k] = variance_scaler * (r.nextFloat());
				} else {
					medoid[k] = 0;
					variance[k] = 0;
				}

			}
			this.medoids.add(medoid);
			this.variances.add(variance);
		}

	}

	
	
//	private class ParallelGen implements Runnable {
//
//	
//		float[] dat;
//		float[] medoid;
//		float[] variance;
//		int end;
//		int start;
//		
//		public ParallelGen(float[] dat,float[] medoid,float[] variance,int start, int end) {
//			this.dat = dat;
//			this.medoid = medoid;
//			this.variance = variance;
//			this.start = start;
//			this.end = end;
//		}
//
//		@Override
//		public void run() {
//			for (int k = start; k < end; k++) {
//				if (r.nextInt() % (int) (1.0f / sparseness) == 0)
//					dat[k] = medoid[k] + (float) r.nextGaussian() * variance[k];
//			}
//		}
//	}
	
	public int processors = Runtime.getRuntime().availableProcessors();
	public ExecutorService executor = Executors.newFixedThreadPool(processors);
	
	public int lastlabel;
	
	public float[] generateNext() {
		
		int randcluster = (int) ((size++) % numClusters);
		if (shuffle) {
			r = new Random();
			randcluster = r.nextInt(numClusters);
		}
		lastlabel = randcluster;
		
		float[] variance = variances.get(randcluster);
		float[] medoid = medoids.get(randcluster);
		float[] dat = new float[dimension];
		
//		int lenDivProcCount = dimension/processors;
//		
//		int i=0;
//		for(;i<processors-1;i++){
//			ParallelGen r = new ParallelGen(dat,medoid,variance,i*lenDivProcCount, (i+1)*lenDivProcCount);
//			executor.submit(r);
//		}
//		
//		ParallelGen r = new ParallelGen(dat,medoid,variance,i*lenDivProcCount,dimension);
//		executor.submit(r);

		for (int k = 0; k < dimension; k++) {
			if (r.nextInt() % (int) (1.0f / sparseness) == 0)
				dat[k] = medoid[k] + (float) r.nextGaussian() * variance[k];
		}
		
		
		if (save) {
			data.add(dat);
			reps.add(randcluster);
		}
		
		return dat;
	}


	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
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
		return reps;
	}

	@Override
	public int getDimension() {
		return dimension;
	}

	@Override
	public Iterator<float[]> getIterator() {
		return new Iterator<float[]>(){

			@Override
			public boolean hasNext() {
				return true;
			}

			@Override
			public float[] next() {
				return generateNext();
			}
			
		};

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
		int d = 500;
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

		
		GenerateStreamData gen = new GenerateStreamData(k , d, var, 1012012013,raw);
		
		gen.writeToFile(outputFile,lblFile,30000);;
	}

	private void writeToFile(File datafile,File lblfile,int numofvecs) {

			try {
				BufferedWriter lbl = new BufferedWriter(new FileWriter(lblfile));
				BufferedWriter dat = new BufferedWriter(new FileWriter(datafile));
				lbl.write(String.valueOf(numofvecs));
				lbl.write('\n');
				lbl.write(String.valueOf(1));
				lbl.write('\n');
				
				dat.write(String.valueOf(numofvecs));
				dat.write('\n');
				dat.write(String.valueOf(dimension));
				dat.write('\n');
				
				
				for (int i = 0; i < numofvecs; i++) {
					float[] v = this.generateNext();
					lbl.write(String.valueOf(this.lastlabel));
					lbl.write('\n');
					lbl.flush();
					for(float vi : v){
						dat.write(String.valueOf(vi));
						dat.write('\n');
					}
					dat.flush();
				}
				dat.close();
				lbl.close();
			} catch (IOException e) {
				e.printStackTrace();

		}
		
	}
	

}
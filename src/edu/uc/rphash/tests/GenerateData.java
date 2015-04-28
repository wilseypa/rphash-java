package edu.uc.rphash.tests;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class GenerateData 
{
	RandomDistributionFnc genfnc;
	int numClusters;
	int numVectorsPerCluster;
	int dimension;
	Random r;
	List<float[]>  data;
	List<float[]> medoids;
	List<Integer> reps;
	float scaler;
	boolean shuffle;
	float sparseness;
	
	public GenerateData(int numClusters, int numVectorsPerCluster, int dimension){
		r = new Random();
		this.numClusters =numClusters;
		this.numVectorsPerCluster=numVectorsPerCluster;
		this.dimension=dimension;
		this.shuffle = true;
		this.medoids = null;
		this.data = null;
		this.reps = null;
		this.scaler = (float)Math.sqrt(dimension);//normalize dimension
		this.sparseness = 1.0f;
		this.genfnc = new RandomDistributionFnc(){
			@Override
			public float genVariate() {
				return (float)r.nextGaussian();
			}
		};
		generateMem();
	}
	
	
	public GenerateData(int numClusters, int numVectorsPerCluster, int dimension,boolean shuffle){
		r = new Random();
		this.numClusters =numClusters;
		this.numVectorsPerCluster=numVectorsPerCluster;
		this.dimension=dimension;
		this.medoids = null;
		this.data = null;
		this.reps = null;
		this.scaler = (float)Math.sqrt(dimension);//normalize dimension
		this.shuffle = shuffle;
		this.sparseness = 1.0f;
		this.genfnc = new RandomDistributionFnc(){
			@Override
			public float genVariate() {
				return (float)r.nextGaussian();
			}
		};
		generateMem();
	}
	
	public GenerateData(int numClusters, int numVectorsPerCluster, int dimension,float variance){
		r = new Random();
		this.numClusters =numClusters;
		this.numVectorsPerCluster=numVectorsPerCluster;
		this.dimension=dimension;
		this.medoids = null;
		this.data = null;
		this.reps = null;
		this.shuffle = true;
		this.sparseness = 1.0f;
		if(variance>0)
			this.scaler = variance;//normalize dimension
		else
			this.scaler = 1.0f/(float)Math.sqrt(dimension);
		
		this.genfnc = new RandomDistributionFnc(){
			@Override
			public float genVariate() {
				return (float)r.nextGaussian();
			}
		};
		generateMem();
	}
	public GenerateData(int numClusters, int numVectorsPerCluster, int dimension,float variance,boolean shuffle){
		r = new Random();
		this.numClusters =numClusters;
		this.numVectorsPerCluster=numVectorsPerCluster;
		this.dimension=dimension;
		this.medoids = null;
		this.data = null;
		this.reps = null;
		this.shuffle = shuffle;
		this.sparseness = 1.0f;
		if(variance>0)
			this.scaler = variance;//normalize dimension
		else
			this.scaler = 1.0f/(float)Math.sqrt(dimension);
		
		this.genfnc = new RandomDistributionFnc(){
			@Override
			public float genVariate() {
				return (float)r.nextGaussian();
			}
		};
		
		generateMem();
	}
	
	public GenerateData(int numClusters, int numVectorsPerCluster, int dimension,float variance,boolean shuffle, float sparseness){
		r = new Random();
		this.numClusters =numClusters;
		this.numVectorsPerCluster=numVectorsPerCluster;
		this.dimension=dimension;
		this.medoids = null;
		this.data = null;
		this.reps = null;
		this.shuffle = shuffle;
		this.sparseness = sparseness;
		if(variance>0)
			this.scaler = variance;///(float)Math.sqrt(dimension);//normalize dimension
		else
			this.scaler = 1.0f/(float)Math.sqrt(dimension);
		
		this.genfnc = new RandomDistributionFnc(){
			@Override
			public float genVariate() {
				return (float)r.nextGaussian();
			}
		};
		
		generateMem();
	}
	
	public GenerateData(int numClusters, int numVectorsPerCluster, int dimension, RandomDistributionFnc genvariate)
	{
		r = new Random();
		this.numClusters =numClusters;
		this.numVectorsPerCluster=numVectorsPerCluster;
		this.dimension=dimension;
		this.genfnc = genvariate;
		this.scaler =  1.0f/(float)Math.sqrt(dimension);//normalize dimension
		this.medoids = null;
		this.data = null;
		this.shuffle = true;
		this.sparseness = 1.0f;
		generateMem();
	}
	public GenerateData(int numClusters, int numVectorsPerCluster, int dimension, File f)
	{
		r = new Random();
		this.numClusters =numClusters;
		this.numVectorsPerCluster=numVectorsPerCluster;
		this.dimension=dimension;
		this.scaler =  1.0f/(float)Math.sqrt(dimension);//normalize dimension
		this.medoids = new ArrayList<float[]>();
		this.data = new ArrayList<float[]>();
		this.shuffle = true;
		this.sparseness = 1.0f;
		this.genfnc = new RandomDistributionFnc(){
			@Override
			public float genVariate() {
				return (float)r.nextGaussian();
			}
		};
		generateDisk(f);
	}
	public GenerateData(int numClusters, int numVectorsPerCluster, int dimension, File f, RandomDistributionFnc genvariate){
		r = new Random();
		this.numClusters =numClusters;
		this.numVectorsPerCluster=numVectorsPerCluster;
		this.dimension=dimension;
		this.scaler =  1.0f/(float)Math.sqrt(dimension);//normalize dimension
		this.genfnc = genvariate;
		this.shuffle = true;
		this.sparseness = 1.0f;
		generateDisk(f);
	}
	
	private void permute(){
		
		reps = new ArrayList<Integer>(data.size());
		ArrayList<float[]> newData = new ArrayList<float[]>(data.size());
		for(int i = 0; i<data.size();i++)reps.add(i);
		
		Collections.shuffle(reps, r);
		
		for(int i = 0; i<reps.size();i++){
			newData.add(data.get(reps.get(i)));
			reps.set(i,(int)((float)reps.get(i)/(float)numVectorsPerCluster));
		}
		data = newData;
	}
	
	void normalize()
	{
		for(int i =0;i<dimension;i++){
			float sum = 0.0f;
			for(float[] f : data)
			{
				sum+= Math.abs(f[i]);
			}
			sum/=(float)data.size();
			for(float[] f : data)
			{
				f[i]/=(sum);
			}
			for(float[] f :medoids)
			{
				f[i]/=(sum);
			}
		}
	}
	
	private void generateMem()
	{
		this.data = new ArrayList<float[]>();//new float[numClusters*numVectorsPerCluster][dimension];
		this.medoids = new ArrayList<float[]>();//new float[numClusters][dimension];
		
		//float 
//		float maxval = 0.0f;
		for(int i=0;i<numClusters;i++){
			//gen cluster center
			float[] medoid = new float[dimension];
			float[] variances = new float[dimension];
			
			for(int k=0;k<dimension;k++)
			{
				if(r.nextInt()%(int)(1.0f/sparseness)==0){
					medoid[k] = r.nextFloat()*2.0f -1.0f;
					variances[k] = scaler*(r.nextFloat()*2.0f -1.0f);
				}
			
			}
			this.medoids.add(medoid);
			//gen data
			for(int j=0;j<numVectorsPerCluster;j++){
				float[] dat = new float[dimension];
				for(int k=0;k<dimension;k++)
				{
					if(r.nextInt()%(int)(1.0f/sparseness)==0)
						dat[k] = medoid[k]+(float)r.nextGaussian()*variances[k];
				}
				this.data.add(dat);
			}
		}

		//normalize();
		if(this.shuffle){
			permute();
		}
		
	}
	
	public int getClusterID(int vecIdx){
		if(shuffle)
		return reps.get(vecIdx);
		else return vecIdx;
	}
	
	public void writeToFile(File f){
		try{
			BufferedWriter bf = new BufferedWriter(new FileWriter(f));
			int l = 0;
			for(int i=0;i<numClusters;i++){
				float[] medoid = medoids.get(i);
				for(int k=0;k<dimension;k++)
				{
					bf.write(String.valueOf(medoid[k]));
					bf.write(' ');
				}
				bf.write('\n');
				//gen data
				for(int j=0;j<numVectorsPerCluster;j++){
					float[] vec = data.get(l);
					for(int k=0;k<dimension;k++){
						bf.write(String.valueOf(vec[k]));
						bf.write(' ');
					}
					bf.write('\n');
					l++;
				}
				bf.flush();
				
			}
			bf.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	

	
	private void generateDisk(File f)
	{
		try{
			BufferedWriter bf = new BufferedWriter(new FileWriter(f));

			float scaler = (float)Math.sqrt(dimension);//normalize dimension
			for(int i=0;i<numClusters;i++){
				//gen cluster center
				float[] medoid = new float[dimension];
				float[] variances = new float[dimension];
				
				for(int k=0;k<dimension;k++)
				{
					medoid[k] = r.nextFloat()*2.0f -1.0f;
					variances[k] = scaler*(r.nextFloat()*2.0f -1.0f);
					bf.write(String.valueOf(medoid[k]));
					bf.write(' ');
				}
				bf.write('\n');
				//gen data
				for(int j=0;j<numVectorsPerCluster;j++){
					for(int k=0;k<dimension;k++){
						bf.write(String.valueOf(medoid[k]+(float)r.nextGaussian()*variances[k]));
						bf.write(' ');
					}
					bf.write('\n');
				}
				bf.flush();
				
			}
			bf.close();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	public List<float[]> data(){
		if(data==null)
			generateMem();
		return data;
	}
	
	public List<float[]> medoids(){
		if(medoids==null)
			generateMem();
		return medoids;
	}
	
}

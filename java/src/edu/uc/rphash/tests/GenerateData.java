package edu.uc.rphash.tests;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
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
	
	public GenerateData(int numClusters, int numVectorsPerCluster, int dimension){
		r = new Random();
		this.numClusters =numClusters;
		this.numVectorsPerCluster=numVectorsPerCluster;
		this.dimension=dimension;
		this.medoids = null;
		this.data = null;
		
		genfnc = new RandomDistributionFnc(){
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
		genfnc = genvariate;
		this.medoids = null;
		this.data = null;
		
		generateMem();
	}
	public GenerateData(int numClusters, int numVectorsPerCluster, int dimension, File f)
	{
		r = new Random();
		this.numClusters =numClusters;
		this.numVectorsPerCluster=numVectorsPerCluster;
		this.dimension=dimension;
		this.medoids = new ArrayList<float[]>();
		this.data = new ArrayList<float[]>();
		genfnc = new RandomDistributionFnc(){
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
		genfnc = genvariate;
		generateDisk(f);
	}
	
	private void generateMem()
	{
		this.data = new ArrayList<float[]>();//new float[numClusters*numVectorsPerCluster][dimension];
		this.medoids = new ArrayList<float[]>();//new float[numClusters][dimension];
		float scaler = (float)Math.sqrt(dimension);//normalize dimension
		for(int i=0;i<numClusters;i++){
			//gen cluster center
			float[] medoid = new float[dimension];
			for(int k=0;k<dimension;k++)
			{
				medoid[k] = r.nextFloat()*2.0f -1.0f;
			}
			this.medoids.add(medoid);
			//gen data
			for(int j=0;j<numVectorsPerCluster;j++){
				float[] dat = new float[dimension];
				for(int k=0;k<dimension;k++){
					dat[k] = medoid[k]+(float)r.nextGaussian()/scaler;
				}
				this.data.add(dat);
			}
			
		}
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
				for(int k=0;k<dimension;k++)
				{
					medoid[k] = r.nextFloat()*2.0f -1.0f;
					bf.write(String.valueOf(medoid[k]));
					bf.write(' ');
				}
				bf.write('\n');
				//gen data
				for(int j=0;j<numVectorsPerCluster;j++){
					for(int k=0;k<dimension;k++){
						bf.write(String.valueOf(medoid[k]+(float)r.nextGaussian()/scaler));
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

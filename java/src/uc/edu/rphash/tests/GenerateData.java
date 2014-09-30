package uc.edu.rphash.tests;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class GenerateData 
{
	RandomDistributionFnc genfnc;
	int numClusters;
	int numVectorsPerCluster;
	int dimension;
	Random r;
	float[][] data;
	float[][] medoids;
	
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
		this.medoids = new float[0][0];
		this.data = new float[0][0];
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
		this.data = new float[numClusters*numVectorsPerCluster][dimension];
		this.medoids = new float[numClusters][dimension];
		int l = 0;
		float scaler = (float)Math.sqrt(dimension);//normalize dimension
		for(int i=0;i<numClusters;i++){
			//gen cluster center
			
			for(int k=0;k<dimension;k++)
			{
				medoids[i][k] = r.nextFloat()*2.0f -1.0f;
			}
			//gen data
			for(int j=0;j<numVectorsPerCluster;j++){
				for(int k=0;k<dimension;k++){
					data[l][k] = medoids[i][k]+(float)r.nextGaussian()/scaler;
				}
				l++;
			}
			
		}
	}
	
	public void writeToFile(File f){
		try{
			BufferedWriter bf = new BufferedWriter(new FileWriter(f));
			int l = 0;
			for(int i=0;i<numClusters;i++){
				for(int k=0;k<dimension;k++)
				{
					bf.write(String.valueOf(medoids[i][k]));
					bf.write(' ');
				}
				bf.write('\n');
				//gen data
				for(int j=0;j<numVectorsPerCluster;j++){
					for(int k=0;k<dimension;k++){
						bf.write(String.valueOf(data[l][k]));
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
	
	public float[][] data(){
		
		if(data==null)
			generateMem();
		return data;
	}
	
	public float[][] medoids(){
		if(medoids==null)
			generateMem();
		return medoids;
	}
	
}

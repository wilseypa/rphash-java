package edu.uc.rphash.tests.clusterers;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.uc.rphash.Clusterer;
import edu.uc.rphash.Readers.RPHashObject;
import edu.uc.rphash.tests.generators.GenerateData;

/*	Adaptive Mean Shift (AMS) Algorithm
 * 
 * 
 * 	Mean Shift algorithm based on methods described by Fukunaga and Hostetler
 * 		'Estimation of the Gradient of a Density Function, with Applications 
 * 			in Pattern Recognition' (
 * 
 * 		http://ieeexplore.ieee.org/stamp/stamp.jsp?arnumber=1055330
 * 
 * 
 * 
 * 	Additional Kernel and Optimizations described by Cheng
 * 		'Mean Shift, Mode Seeking, and Clustering' (1995)
 * 
 * 		http://dl.acm.org/citation.cfm?id=628711
 * 
 * 
 * 
 * 	Adaptive Mean Shift algorithm based on ...
 * 		
 * 
 */


//TODO: Add labels to points for centroids
//TODO: Compute ARI (R)
//TODO: Remove testing options
//TODO: Merge branch to Master
//TODO: Add KNN / KNN-LSH / KD-TREE


public class AdaptiveMeanShift implements Clusterer {

	List<float[]> data;			//global data storage
	List<float[]> centroids;	//global centroid storage
	

	//Parameters
	double h =1;			// bandwidth
	
	int kernelMode = 1;		// mode (0:uniform; 1:gaussian) 
	
	int windowMode = 0;		// Determine how to perform the Adaptive Window
							// 		0 - No adaptivity; Basic Mean Shift
							// 		1 - Balloon Estimator
							// 		2 - Sample Point Estimator (TODO)

	int knnAlg = 0; 		//Determine what KNN algorithm to use
							//		0 - KNN EUC (TODO)
							//		1 - KNN LSH (TODO)
							//		2 - KD-TREE (TODO)
	
	int n = 5; 				//Number of KNN points for adaptive window
	
	
	
	
	
	static int maxiters = 10000;			//Max iterations before breaking search for convergence
	float convergeValue = (float) 0.00001;	//maximum change in each dimension to 'converge'
	float blurPercent = (float) 2;				//Amount to blur centroids to group similar Floats
	
	//TEST Parameters:
	boolean debug = false;							//Control Debug Output
	boolean minimalOutput = true;					//Print the minimal final output (pretty print)
	boolean printCentroids = false;					//Print out centroids (not pretty)
	Set<String> cent = new HashSet<String>();		//Storage for grouping the clusters
	double knntime = 0;
	double calcTime = 0;
	double algTime = 0;
	boolean testRun = false;
		
	public void setMode(int mode){ this.kernelMode = mode; }
	
	public void setH(double h) { this.h = h; }
	
	public void setWinMode(int winMode){ this.windowMode = winMode;	}

	public void setN(int n){ this.n = n; }

	public List<float[]> getData() { return data; }

	public void setData(List<float[]> data){ this.data = data; }
	
	
	public AdaptiveMeanShift(){ 
		this.centroids = new ArrayList<float[]>(); 
	}

	
	public AdaptiveMeanShift(int h, int windowMode, int kernelMode, int n, List<float[]> data){
		this.h = h;
		this.windowMode = windowMode;
		this.kernelMode = kernelMode;
		this.n = n;
		this.data = data;
		this.centroids = new ArrayList<float[]>();
	}

	
	public float calcMode(float curWindow, float workingData){
		float mPoint = 0;
		float kern = 0;
		float c = (float) (1.0/Math.pow(h,2));
		
		//Mode 0 is uniform
		if (kernelMode == 0){
			mPoint = workingData;
		}
		
		//Mode 1 is Gaussian
		else if (kernelMode == 1){
			kern = (float) Math.exp(-c * Math.pow(workingData - curWindow, 2));
			mPoint = (float) kern * (workingData - curWindow);
		}

		return mPoint;
	}
	
	
	public void adaptH(List<float[]> data, int curPoint){
		if(windowMode == 0){
			return; //No adaptivity
		}
		else if(windowMode == 1){

			long startTime = System.currentTimeMillis();
			
			h = findKNN(data,curPoint,n);

			long endTime = System.currentTimeMillis();
			
			knntime += endTime - startTime;		
			
			return; //Balloon estimator
		}
		else if(windowMode == 2){
			return; //KNN sample point estimator
		}
	}
	
	public float findKNN(List<float[]> data, int curPoint, int n){
		//Find the nth instance to get h
		List<Float> dataSet = new ArrayList<Float>();
		float[] curData = data.get(curPoint);
		float curMax = 10000; //TODO: fix logic to set this value
		
		for(int i = 0; i < data.size(); i++){
			float euc = 0;
			if(i != curPoint){
				

				long startTime = System.currentTimeMillis();
				for(int y = 0; y < data.get(i).length; y++){
					//Calculate the euc distance per axis
					euc = (float) (euc + (Math.pow(curData[y] - data.get(i)[y],2)));
				}
				
				//Take square root of total axis distances
				euc = (float) Math.sqrt(euc);

				
				if(euc < curMax){
					dataSet.add(euc);
				}
				if(dataSet.size() > n){
					int maxI = 0;
					float maxV = 0;
					
					for(int t = 0; t < dataSet.size(); t++){
						if(dataSet.get(t) > maxV){
							maxI = t;
							maxV = dataSet.get(t);
						}
					}
					curMax = maxV;
					dataSet.remove(maxI);
					
				}
				long endTime = System.currentTimeMillis();
				
				calcTime += endTime - startTime;
			}			
		}
		
		//System.out.println(dataSet);
		int maxI = 0;
		float maxV = 0;
		for(int t = 0; t < dataSet.size(); t++){
			if(dataSet.get(t) > maxV){
				maxI = t;
				maxV = dataSet.get(t);
			}
		}
		return dataSet.get(maxI);
	}
	
	
	public void cluster(List<float[]> data){

		//Loop through each row to test each point
		for(int i = 0; i < data.size(); i++){
			
			float[] curWindow = new float[data.get(0).length];
			float[] bufWindow = new float[data.get(0).length];
			double euc = 0;
			boolean converge = false;
			int m = 0;
			int winCount = 0;
			
			for(int t = 0; t < data.get(0).length; t++){
				curWindow = data.get(i).clone();
			}

			adaptH(data, i);
			
			// Check for convergence, or we've hit max iterations before convergence
			while((!converge) && (m < maxiters)){			
				m++;
				bufWindow = curWindow.clone();
				
				for(int t = 0; t < data.get(0).length; t++){
					curWindow[t] = (float) 0;
				}
				
				// Loop through each point in the data set
				for(int x = 0; x < data.size(); x++){
					euc = 0;
					
					for(int y = 0; y < data.get(x).length; y++){
						//Calculate the euc distance per axis
						euc = euc + (Math.pow(bufWindow[y] - data.get(x)[y],2));
					}
					
					//Take square root of total axis distances
					euc = Math.sqrt(euc);

					// Is the test point in the window?
					if(euc <= h){
						winCount++;

						//Traverse each dimension
						for(int n = 0; n < data.get(x).length; n++){
							curWindow[n] = curWindow[n] + calcMode(bufWindow[n], data.get(x)[n]);
						}
					}
				}
				
				if(winCount > 0){
					//If we used Mode 0 or ..., take the average
					boolean convergeTest = true;
					
					for(int y = 0; y < curWindow.length; y++){
						if(curWindow[y] >= convergeValue)
							convergeTest = false;
					}
					
					if(kernelMode == 0){
						for(int y = 0; y < curWindow.length; y++){
							curWindow[y] = curWindow[y] / winCount;
						}
					}
					if(kernelMode >= 1){
						for(int y = 0; y < curWindow.length; y++){
							curWindow[y] = curWindow[y] / winCount;
							curWindow[y] = bufWindow[y] + curWindow[y];
							printDebug("New Window: " + curWindow[y]);
						}
						printDebug("_______________________________________");
					}
					//Check for convergence
					if(Arrays.equals(curWindow,bufWindow) || convergeTest){
						boolean add = true;
						if(centroids.indexOf(curWindow) >= 0){
							add = false;
						}
						add = checkAllCentroids(curWindow);
						
						if(add){
							String str = "";
							for(int j = 0; j < curWindow.length; j++){str += Float.toString(curWindow[j]) + ",";}
							cent.add(str + "\n");
							centroids.add(curWindow.clone());
						}
						
						converge = true;
					}		
					bufWindow = curWindow.clone();
				}

				//Reset values for next iteration
				euc = 0;
				m = 0;
				winCount = 0;	
			}	
		}
	}
	
	
	public boolean checkAllCentroids(float[] window){
		float[] centroid;
		for(int i = 0; i < centroids.size(); i++){
			centroid = centroids.get(i);
			double percentDiff = 0;
			
			for(int z = 0; z < centroid.length; z++){
				percentDiff = percentDiff + Math.abs(1-(centroid[z] / window[z]));
			}
			
			percentDiff = percentDiff / centroid.length;

			if(percentDiff < blurPercent){
				return false;
			}
			
		}
		return true;
	}

	
	void run(int genClusters, int genRowsPerCluster, int genColumns){		

		if(this.data == null){
			GenerateData gen = new GenerateData(genClusters,genRowsPerCluster, genColumns);
			this.data = gen.data;
		}
		
		long startTime = System.currentTimeMillis();
		cluster(this.data);
		
		long endTime = System.currentTimeMillis();
		algTime = (endTime - startTime)/1000.0;
	}
	
	
	public void printDebug(String s){
		if(debug)
			System.out.println(s);
	}

	
	public void runTests(int max_genClusters, int max_genRowsPerCluster, int max_genColumns,
			int incr_genClusters, int incr_genRowsPerCluster, int incr_genColumns){
		try{
			PrintWriter output = new PrintWriter("testOutput.csv","UTF-8");
			
			GenerateData genData;
			
			output.println("g_Clusters,g_Rows,g_Columns,h,kMode,wMode,n,count,completionTime");
			
			for(int genClusters = 2; genClusters <= max_genClusters; genClusters = genClusters + incr_genClusters){
				System.out.println(genClusters);
				for(int genRowsPerCluster = 2; genRowsPerCluster <= max_genRowsPerCluster; genRowsPerCluster = genRowsPerCluster + incr_genRowsPerCluster){
					for(int genColumns = 1; genColumns <= max_genColumns; genColumns = genColumns + incr_genColumns){
						for(int i = 0; i < 1; i++){
							genData = new GenerateData(genClusters,genRowsPerCluster,genColumns);
							for(int kMode = 0; kMode < 2; kMode++){
								for(int wMode = 0; wMode < 2; wMode++){
									if(wMode == 0){
										for(double band = 0.25; band < 1.5; band = band + 0.25){
											AdaptiveMeanShift ams = new AdaptiveMeanShift();
											ams.setMode(kMode);
											ams.setN(n);
											ams.setH(band);
											ams.setWinMode(wMode);
											ams.data = genData.data;
											
											ams.run(genClusters,genRowsPerCluster,genColumns);
													
											output.print(genClusters + "," + genRowsPerCluster + "," + genColumns + ",");
											output.print(ams.h + "," + ams.kernelMode + "," + ams.windowMode + ",");
											output.println(ams.n + "," + ams.cent.size() + "," + ams.algTime);
										}
									}
									else{
										for(int n = 2; n <= 10 && n <= genClusters * genRowsPerCluster; n = n+2){
											AdaptiveMeanShift ams = new AdaptiveMeanShift();
											ams.setMode(kMode);
											ams.setN(n);
											ams.setWinMode(wMode);
											ams.data = genData.data;
											
											ams.run(genClusters,genRowsPerCluster,genColumns);
														
											output.print(genClusters + "," + genRowsPerCluster + "," + genColumns + ",");
											output.print(ams.h + "," + ams.kernelMode + "," + ams.windowMode + ",");
											output.println(ams.n + "," + ams.cent.size() + "," + ams.algTime);	
																		
										}
									}
								}
							}
						}
					}
				}
			}
			
			output.close();
		}
		catch(Exception e){
			System.out.println("Exception: " + e.toString());
		}
	}
	
	
	public static void main(String[] args){
		int genClusters = 3;
		int genRowsPerCluster = 10;
		int genColumns = 100;
		
		AdaptiveMeanShift ams = new AdaptiveMeanShift();
		
		if(ams.testRun){
			ams.runTests(genClusters, genRowsPerCluster, genColumns, 1,100,1000);
		}
		else{
			ams.run(genClusters,genRowsPerCluster,genColumns);
			if(ams.printCentroids){
				System.out.println("Printing Centroids:");
					
				for(int i = 0; i < ams.centroids.size(); i ++){
					System.out.println("Centroid " + i);
					for(int t = 0; t < ams.centroids.get(0).length; t++)
						System.out.println("\t" + ams.centroids.get(i)[t]);
				}
			}
			if(ams.minimalOutput){
				
				System.out.println("\n\nh: " + ams.h);
				System.out.println("Kernel Mode: " + ams.kernelMode);
				System.out.println("Window Mode: " + ams.windowMode);
				System.out.println("n (KNN): " + ams.n + "\n");
				System.out.println("Number of Clusters: " + ams.cent.size() + "\n");
				System.out.println(ams.cent.toString().replaceAll(", ", " "));
				System.out.println("\nAMS Clustering completed in: " + ams.algTime + " seconds\n");				
			}
		}
		System.out.println("\n\nDone!");
	}
	

	@Override
	public List<float[]> getCentroids() {
		return centroids;
	}

	@Override
	public RPHashObject getParam() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setK(int getk) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setWeights(List<Float> counts) {
		// TODO Auto-generated method stub
		
	}

}
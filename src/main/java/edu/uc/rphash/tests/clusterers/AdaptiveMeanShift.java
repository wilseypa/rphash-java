package edu.uc.rphash.tests.clusterers;

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

public class AdaptiveMeanShift implements Clusterer {

	List<float[]> data;			//global data storage
	List<float[]> centroids;	//global centroid storage
	

	//Parameters
	double h =1;// bandwidth
	
	int kernelMode = 1;		// mode (0:uniform; 1:gaussian) 
	
	int windowMode = 0;		// Determine how to perform the Adaptive Window
							// 		0 - No adaptivity; Basic Mean Shift
							// 		1 - Balloon Estimator (TODO)
							// 		2 - KNN (use N for number of points) (TODO)

	int n = 5; 				//Number of KNN points for adaptive window
	
	
	//TEST Parameters:
	boolean debug = false;							//Control Debug Output
	static int maxiters = 10000;					//Max iterations before breaking search for convergence
	boolean minimalOutput = true;					//Print the minimal final output (pretty print)
	boolean printCentroids = false;					//Print out centroids (not pretty)
	Set<String> cent = new HashSet<String>();		//Storage for grouping the clusters
	float convergeValue = (float) 0.00001;			//maximum change in each dimension to 'converge'
	float blurPercent = (float) 0.5;				//Amount to blur centroids to group similar Floats
	
		
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

		//Mode 0 is uniform -> each point weighed equally
		if (kernelMode == 0){
			//#In uniform, add the coordinates to a cumulative buffer
			mPoint = workingData;
		}
		
		//Mode 1 is Gaussian -> NEEDS VALIDATION
		else if (kernelMode == 1){
			
			float c = (float) (1.0/Math.pow(h,2));
			
			kern = (float) Math.exp(-c * Math.pow(workingData - curWindow, 2));
			mPoint = (float) kern * (workingData - curWindow);
		}

		//Mode 2 is Epanechnikov -> TODO
		else if (kernelMode == 2){
			//	m_point = 1-(pow(test_points[x_2][var_n]/h,2))
		}
		return mPoint;
	}
	
	
	public void adaptH(List<float[]> data, int curPoint){
		if(windowMode == 0){
			return; //No adaptivity
		}
		else if(windowMode == 1){
			return; //Balloon estimator
		}
		else if(windowMode == 2){
			return; //KNN sample point estimator
		}
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
			
			// Check for convergence, or we've hit max iterations before convergence
			while((!converge) && (m < maxiters)){
			
				adaptH(data, i);
				
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
					printDebug("Converge? " + convergeTest);
					
					if(kernelMode == 0){
						for(int y = 0; y < curWindow.length; y++){
							curWindow[y] = curWindow[y] / winCount;
						}
					}
					if(kernelMode == 1){
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
				printDebug("\t\t" + centroid[z]);
				printDebug("\t\t" + window[z]);
				percentDiff = percentDiff + Math.abs(1-(centroid[z] / window[z]));
				printDebug("\t\tPD: " + Math.abs(1-(centroid[z]/window[z])));
			}
			
			percentDiff = percentDiff / centroid.length;

			printDebug("\tPercentDiff = " + percentDiff);
			if(percentDiff < blurPercent){
				printDebug("Returning false, found centroid");
				return false;
			}
			
		}
		return true;
	}
	
	
	public void reducedCentroids(){
		System.out.println("h: " + h);
		System.out.println("Kernel Mode: " + kernelMode);
		System.out.println("Window Mode:" + windowMode);
		
		//Reduce the centroids
		
		Set<String> s = new HashSet<String>();
		for(int i = 0; i < centroids.size(); i++){
			s.add(centroids.get(i).toString());
		}
		
		System.out.println(s.toString());
		
	}

	
	void run(){		
		GenerateData gen = new GenerateData(15,100,100);
		List<float[]> data = gen.data;
		
		long startTime = System.currentTimeMillis();
		cluster(data);
		
		long endTime = System.currentTimeMillis();
		
		System.out.println("AMS Clustering completed in: " + (endTime - startTime)/1000.0 + " seconds\n");
	}
	
	
	public void printDebug(String s){
		if(debug)
			System.out.println(s);
	}

	
	public static void main(String[] args){
		AdaptiveMeanShift ams = new AdaptiveMeanShift();
		ams.run();
				
		if(ams.printCentroids){
			System.out.println("Printing Centroids:");
				
			for(int i = 0; i < ams.centroids.size(); i ++){
				System.out.println("Centroid " + i);
				for(int t = 0; t < ams.centroids.get(0).length; t++)
					System.out.println("\t" + ams.centroids.get(i)[t]);
			}
		}
		
		if(ams.minimalOutput){
			System.out.println("h: " + ams.h);
			System.out.println("Kernel Mode: " + ams.kernelMode);
			System.out.println("Window Mode: " + ams.windowMode +"\n");
			System.out.println("Number of Clusters: " + ams.cent.size() + "\n");
			System.out.println(ams.cent.toString().replaceAll(", ", " "));
			
		}
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
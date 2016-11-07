package edu.uc.rphash.tests.clusterers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.uc.rphash.Centroid;
import edu.uc.rphash.Clusterer;
import edu.uc.rphash.Readers.RPHashObject;
import edu.uc.rphash.Readers.SimpleArrayReader;
import edu.uc.rphash.kdtree.KDTreeNN;
import edu.uc.rphash.kdtree.naiveNN;
import edu.uc.rphash.lsh.LSHkNN;
import edu.uc.rphash.tests.generators.GenerateData;
import edu.uc.rphash.util.VectorUtil;

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
//TODO: add weights to centroid merging -> rphash (cardinality)
//TODO: windowMode -> Sample Point Estimator

final class cStore{
	public int count;
	public float[] centroid;
	public float wcsse = 0;
	public Centroid cent;
	
	public void addPoint(float[] point){		
		this.count++;
		this.wcsse += VectorUtil.distance(point, centroid);
		this.cent.setCount(this.count);
		this.cent.setWCSS(this.wcsse);
	}
	
	public cStore(float[] centroid){
		this.count = 0;
		this.cent = new Centroid(centroid,0);
		this.centroid = centroid;
		this.wcsse = 0;
	}

	public cStore(float[] window, float[] point) {
		// TODO Auto-generated constructor stub
		this.count = 0;
		this.cent = new Centroid(window,0);
		this.centroid = window;
		this.wcsse = VectorUtil.distance(point, window);
	}
	
}


public class AdaptiveMeanShift implements Clusterer {

	List<float[]> data;			//global data storage
	List<Centroid> centroids;	//global centroid storage
	private RPHashObject so;
	private List<cStore> cs;

	//Parameters
	double h = 1;			// bandwidth
	
	int kernelMode = 0;		// mode (0:uniform; 1:gaussian) 
	
	int windowMode = 1;		// Determine how to perform the Adaptive Window
							// 		0 - No adaptivity; Basic Mean Shift
							// 		1 - Balloon Estimator
							// 		2 - Sample Point Estimator (TODO)

	int knnAlg = 2; 		//Determine what KNN algorithm to use
							//		0 - kNN Naive
							//		1 - kNN LSH
							//		2 - KD-TREE kNN
	
	int k = 5; 				//Number of KNN points for adaptive window
	
	Clusterer weightClusters = null;
	
	
	static int maxiters = 10000;				//Max iterations before breaking search for convergence
	float convergeValue = (float) 0.00001;		//maximum change in each dimension to 'converge'
	float blurPercent = (float) 2;				//Amount to blur centroids to group similar Floats
	
	//TEST Parameters:
	boolean debug = false;							//Control Debug Output
	boolean minimalOutput = true;					//Print the minimal final output (pretty print)
	boolean printCentroids = true;					//Print out centroids (not pretty)
	Set<String> cent = new HashSet<String>();		//Storage for grouping the clusters
		
	public void setMode(int mode){ this.kernelMode = mode; }
	
	public void setH(double h) { this.h = h; }
	
	public void setWinMode(int winMode){ this.windowMode = winMode;	}

	public List<float[]> getData() { return data; }

	public void setRawData(List<float[]> data){ this.data = data; }
	
	
	public AdaptiveMeanShift(){ 
		this.centroids = new ArrayList<Centroid>(); 
		this.cs = new ArrayList<cStore>();
	}
	
	public AdaptiveMeanShift(int k, List<float[]> data){
		this.k = k;
		this.data = data;
		this.centroids = new ArrayList<Centroid>();
	}
	
	public AdaptiveMeanShift(int h, int windowMode, int kernelMode, int k, List<float[]> data){
		this.h = h;
		this.windowMode = windowMode;
		this.kernelMode = kernelMode;
		this.k = k;
		this.data = data;
		this.centroids = new ArrayList<Centroid>();
		this.cs = new ArrayList<cStore>();
	}
	
	public AdaptiveMeanShift(int h, int windowMode, int kernelMode, int k, List<float[]> data, Clusterer c){
		this.h = h;
		this.weightClusters = c;
		this.windowMode = windowMode;
		this.kernelMode = kernelMode;
		this.k = k;
		this.data = data;
		this.centroids = new ArrayList<Centroid>();
		this.cs = new ArrayList<cStore>();
	}
	
	public float calcMode(float curWindow, float workingData){
		float mPoint = 0;
		float kern = 0;
		
		if (kernelMode == 0)	//Uniform
			mPoint = workingData;
		else if (kernelMode == 1){	//Gaussian
			float c = (float) (1.0/Math.pow(h,2));
			kern = (float) Math.exp(-c * Math.pow(workingData - curWindow, 2));
			mPoint = (float) kern * (workingData - curWindow);
		}

		return mPoint;
	}
	
	public void adaptH(List<float[]> data, int curPoint, LSHkNN knnHandle, KDTreeNN kdHandle, naiveNN naiveHandle){
		if(windowMode == 0)	//No adaptivity
			return; 
		else if(windowMode == 1){	//Balloon
			if(knnAlg == 0){
				h = Math.sqrt(naiveHandle.getNNEuc(k, data.get(curPoint)));
				printDebug("naiveH: " + h);
			}
			if(knnAlg == 1){
				List<float[]> retData = knnHandle.knn(k, data.get(curPoint));
				h = VectorUtil.distance(retData.get(retData.size() - 1),data.get(curPoint));
				printDebug("LSHH: " + h);
			}
			if(knnAlg == 2){
				h = Math.sqrt(kdHandle.treeNNEuc(k, data.get(curPoint)));
				printDebug("KDH: " + h + "\n");
			}

			return; 
		}
		else if(windowMode == 2){	//KNN sample point estimator
			return; 
		}
	}
	
	
	
	public void cluster(List<float[]> data){
		LSHkNN knnHandle = null;
		KDTreeNN kdHandle = null;
		naiveNN naiveHandle = null;
		if(windowMode == 1){
			if(knnAlg == 0){
				naiveHandle = new naiveNN(data);
			}
			if(knnAlg == 1){
				knnHandle = new LSHkNN(data.get(0).length,5);
				knnHandle.createDB(data);
			}
			if(knnAlg == 2){
				kdHandle = new KDTreeNN();
				kdHandle.createTree(data);
			}
		}

		
		for(int i = 0; i < data.size(); i++){
			
			float[] curWindow = new float[data.get(0).length];
			float[] bufWindow = new float[data.get(0).length];
			boolean converge = false;
			int m = 0;
			int winCount = 0;
			
			for(int t = 0; t < data.get(0).length; t++){
				curWindow = data.get(i).clone();
			}

			adaptH(data, i, knnHandle, kdHandle, naiveHandle);
			
			while((!converge) && (m < maxiters)){			
				m++;
				bufWindow = curWindow.clone();
				
				for(int t = 0; t < data.get(0).length; t++){
					curWindow[t] = (float) 0;
				}
				
				for(int x = 0; x < data.size(); x++){

					if(VectorUtil.distance(bufWindow, data.get(x)) <= h){
						winCount++;

						for(int n = 0; n < data.get(x).length; n++){
							curWindow[n] = curWindow[n] + calcMode(bufWindow[n], data.get(x)[n]);
						}
					}
				}
				
				if(winCount > 0){
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
						add = checkAllCentroids(curWindow, data.get(i));
						
						if(add){
							String str = "";
							for(int j = 0; j < curWindow.length; j++){str += Float.toString(curWindow[j]) + ",";}
							cent.add(str + "\n");
						}
						
						converge = true;
					}		
					bufWindow = curWindow.clone();
				}

				m = 0;
				winCount = 0;	
			}	
		}
		
		for(cStore cen: cs){
			Centroid it = new Centroid(cen.centroid, 0);
			it.setCount(cen.count);
			it.setWCSS(cen.wcsse);
			centroids.add(it);
			
		}
	}
	
	
	public boolean checkAllCentroids(float[] window, float[] point){
		float[] centroid;
		for(cStore cz : cs){
			centroid = cz.centroid;
			double percentDiff = 0;
			
			for(int z = 0; z < centroid.length; z++){
				percentDiff = percentDiff + Math.abs(1-(centroid[z] / window[z]));
			}
			
			percentDiff = percentDiff / centroid.length;

			if(percentDiff < blurPercent){
				cz.addPoint(point);
				return false;
			}
			
		}
		
		cs.add(new cStore(window, point));
		return true;
	}

	
	void run(){		
		if(this.weightClusters != null){
			//this.weightClusters.setData(this.data);
		}
		
		cluster(this.data);
	}
	
	
	public void printDebug(String s){
		if(debug)
			System.out.println(s);
	}
	
	
	public static void main(String[] args){
		int genClusters = 3;
		int genRowsPerCluster =100;
		int genColumns = 100;
		
		AdaptiveMeanShift ams = new AdaptiveMeanShift();
		
		if(ams.data == null){
			GenerateData gen = new GenerateData(genClusters,genRowsPerCluster, genColumns);
			ams.data = gen.data;
		}
		
		ams.run();
		if(ams.printCentroids){
			System.out.println("Centroid Count: " + ams.centroids.size());
			for(Centroid c: ams.centroids){
				System.out.println("WCSS = " + c.getWCSS());
				System.out.print("Cent = ");
				for(int z = 0; z < c.centroid().length; z++)
					System.out.print(c.centroid()[z] + ",");
				System.out.println("\n\n");
			}
		}
		if(ams.minimalOutput){
			System.out.println("\n\nh: " + ams.h);
			System.out.println("Kernel Mode: " + ams.kernelMode);
			System.out.println("Window Mode: " + ams.windowMode);
			System.out.println("k (KNN): " + ams.k + "\n");
			System.out.println("Number of Clusters: " + ams.cent.size() + "\n");
			System.out.println(ams.cent.toString().replaceAll(", ", " "));				
		}
		
		System.out.println("\n\nDone!");
	}
	

	@Override
	public List<Centroid> getCentroids() {
		if(this.centroids.size() == 0)
			run();		
		return this.centroids;
	}

	@Override
	public RPHashObject getParam() {
		so = new SimpleArrayReader(this.data, k);
		return so;
	}

	@Override
	public void setK(int getk) {
		this.k = getk;
	}

	@Override
	public void setWeights(List<Float> counts) {
		// TODO Auto-generated method stub
		if(data != null) {
			
		}
		
		
	}
	
	@Override
	public void setData(List<Centroid> centroids) {
		ArrayList<float[]> data = new ArrayList<float[]>(centroids.size());
		for(Centroid c : centroids)data.add(c.centroid());
		setRawData(data);
	}

	@Override
	public void reset(int randomseed) {
		// TODO Auto-generated method stub
		this.centroids = null;
		
	}

	@Override
	public boolean setMultiRun(int runs) {
		// Return true to ignore multi-run (deterministic)
		return true;
	}

}
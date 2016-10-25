package edu.uc.rphash.kdtree;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.TreeSet;

import edu.uc.rphash.lsh.LSHkNN;
import edu.uc.rphash.tests.generators.GenerateData;
import edu.uc.rphash.util.VectorUtil;

public class naiveNN {
	List<Float> KNNSet = new ArrayList<Float>();
	List<float[]> points = null;
	float curMax = 10000;
	int k = 3;
	boolean isDebug = false;
	
	public void debug(String s){
		if(isDebug)
			System.out.println(s);
	}
	
	public naiveNN(List<float[]> points){
		//TODO: Constructor (may not need anything)
		
		this.points = points;
	}
	
	public float getNN(int k, float[] p){
		curMax = 10000;
		
		for(int i = 0; i < this.points.size(); i++){
			if(p != points.get(i))
				addToKNNBuffer(p ,points.get(i));
		}
		
		
		return curMax;
	}
	
	public float addToKNNBuffer(float[] startPoint, float[] entryPoint){
		float euc = 0;
		
		debug("Adding to KNN Buffer; current size: " + KNNSet.size());
		
		for(int y = 0; y < entryPoint.length; y++){
			euc = (float) (euc + (Math.pow(entryPoint[y] - startPoint[y],2)));
		}
		
		if(euc < curMax){
			KNNSet.add(euc);
		}
		else
			return 0;
		
		if(KNNSet.size() > k){
			int maxI = 0;
			float maxV = 0;
			
			for(int t = 0; t < KNNSet.size(); t++){
				if(KNNSet.get(t) > maxV){
					maxI = t;
					maxV = KNNSet.get(t);
				}
			}
			curMax = maxV;
			
			KNNSet.remove(maxI);
		}
		return euc;
	}
	
	
	public static void main(String[] args){
		GenerateData g = new GenerateData(20,100,100);
		naiveNN search = new naiveNN(g.data);
		KDTreeNN tree = new KDTreeNN();
		LSHkNN querier = new LSHkNN(g.data.get(0).length, 2);
		
		tree.createTree(g.data);
		querier.createDB(g.data);
		
		float[] p = querier.knn(10, g.data.get(1)).get(1);
		float euc = 0;
		for(int i = 0; i < p.length; i++){
			euc = (float) (euc + (Math.pow(p[i] - g.data.get(1)[i],2)));
		}
		System.out.println("LSH Output: " + euc);
		
		System.out.println("KD Output: " + tree.treeNN(2, g.data.get(1)));
		
		System.out.println("Naive Output: " + search.getNN(2, g.data.get(1)));
		System.out.println("Completed");
		return;
	}
	
	
}


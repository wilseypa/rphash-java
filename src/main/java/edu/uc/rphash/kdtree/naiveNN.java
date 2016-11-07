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
	List<float[]> KNNSet = new ArrayList<float[]>();
	List<float[]> points = null;
	float curMax = 10000;
	int k = 3;
	boolean isDebug = false;
	
	public void debug(String s){
		if(isDebug)
			System.out.println(s);
	}
	
	public naiveNN(List<float[]> points){
		this.points = points;
	}
	
	public List<float[]> getNN(int k, float[] p){
		curMax = 10000;
		KNNSet.clear();
		
		this.k = k;
		
		for(int i = 0; i < this.points.size(); i++){
			addToKNNBuffer(p ,points.get(i));
		}
		
		return KNNSet;
	}
	
	public float getNNEuc(int k, float[] p){
		curMax = 10000;
		KNNSet.clear();
		
		this.k = k;
		
		for(int i = 0; i < this.points.size(); i++){
			addToKNNBuffer(p ,points.get(i));
		}
		
		return curMax;
	}
	
	public float addToKNNBuffer(float[] startPoint, float[] entryPoint){
		float euc = 0;
		
		debug("Adding to KNN Buffer; current size: " + KNNSet.size());
		
		euc = (float) VectorUtil.distance(startPoint, entryPoint);;
		
		if(euc < curMax){
			KNNSet.add(entryPoint);
		}
		else
			return 0;
		
		if(KNNSet.size() > k){
			int maxI = 0;
			float maxV = 0;
			
			for(int t = 0; t < KNNSet.size(); t++){
				if(VectorUtil.distance(startPoint, KNNSet.get(t)) > maxV){
					maxI = t;
					maxV = VectorUtil.distance(startPoint, KNNSet.get(t));
				}
			}
			curMax = maxV;
			
			KNNSet.remove(maxI);
		}
		return euc;
	}
	
	
	public static void main(String[] args){
		GenerateData g = new GenerateData(3,100,100);
		naiveNN search = new naiveNN(g.data);
		KDTreeNN tree = new KDTreeNN();
		LSHkNN querier = new LSHkNN(g.data.get(0).length, 10);
		
		tree.createTree(g.data);
		querier.createDB(g.data);
		
		System.out.println("Data: ");
		VectorUtil.prettyPrint(g.data.get(1));
		
		System.out.println("\nNaive: " + search.getNNEuc(4, g.data.get(1)));
		VectorUtil.prettyPrint(search.getNN(4, g.data.get(1)));
		
		System.out.println("\nKDTree: " + tree.treeNNEuc(4, g.data.get(1)));
		VectorUtil.prettyPrint(tree.treeNN(4, g.data.get(1)));
		
		List<float[]> output = querier.knn(4, g.data.get(1));
		System.out.println("\nLSHKnn: " + VectorUtil.distance(output.get(output.size() - 1),g.data.get(1)));
		VectorUtil.prettyPrint(querier.knn(4, g.data.get(1)));
		System.out.println("\nLSHKnn[2]: " + VectorUtil.distance(output.get(3),g.data.get(1)));
		System.out.println("\nLSHKnn[2]: " + VectorUtil.distance(output.get(2),g.data.get(1)));
		System.out.println("\nLSHKnn[1]: " + VectorUtil.distance(output.get(1),g.data.get(1)));
		System.out.println("\nLSHKnn[0]: " + VectorUtil.distance(output.get(0),g.data.get(1)));
		
		
		return;
	}
	
	
}


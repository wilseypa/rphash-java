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

final class treeElem{
	treeElem left;			//Left branch of tree
	treeElem right;			//Right branch of tree
	treeElem parent;
	float[] point = null;		//Point value of leaf
	int splitDim;				//Dimension of the axis split
	
	public treeElem(){}
}
	
final class sortTree{
	TreeSet<float[]> points;
	int splitDim;
	
	class fComp implements Comparator<float[]>{
		public int compare(float[] f1, float[] f2){
					
			if(f1[splitDim]-f2[splitDim] < 0){
				return -1;
			}
			if(f1[splitDim]-f2[splitDim] == 0){
				return 0;
			}
			if(f1[splitDim]-f2[splitDim] > 0){
				return 1;
			}
			return 0;
		}
	}
	
	public sortTree(int splitDim){
		points = new TreeSet<float[]>(new fComp());
		this.splitDim = splitDim;
	}
	
	public void createTree(List<float[]> points){		
		for(int i = 0; i < points.size(); i++){
			this.points.add(points.get(i));
		}
		return;
	}
	
	public float[] get(int i){
		int z = 0;
		for(float[] pt: this.points){
			if(z == i - 1 || i == 0)
				return pt;
			z++;
		}
		return null;
	}
}


public class KDTreeNN {
	List<Float> KNNSet = new ArrayList<Float>();
	float curMax = 10000;
	int k = 3;
	int countLeaves = 0;
	treeElem head = null;
	int kWorst = 0;
	boolean isDebug = false;
	
	public void debug(String s){
		if(isDebug)
			System.out.println(s);
	}
	
	public KDTreeNN(){
		//TODO: Constructor (may not need anything)
	}

	
	public float[] getMedian(int dimension, List<float[]> points){
		//TODO: get median of point set by sorting and finding median values
		debug("Dim: " + dimension);
		
		sortTree tempTree = new sortTree(dimension);
		tempTree.createTree(points);
			
		debug("Created new tree for: " + dimension);
		debug("Treesize: " + tempTree.points.size());
		
		float[] retValue = tempTree.get((int) (tempTree.points.size()+1) / 2);
		
		debug("retValue = " + retValue[0] + "\t" + retValue[1]);
		return retValue;
		
	}
	
	public void addToTree(List<float[]> points, treeElem current, int axis){
		List<float[]> leftPoints = new ArrayList<float[]>();
		List<float[]> rightPoints = new ArrayList<float[]>();
		
		current.splitDim = axis % points.get(0).length;
		debug("len: " + points.get(0).length);
		debug("\n\nAxis: " + current.splitDim);
		
		current.point = getMedian(current.splitDim, points);
		
		for(int i = 0; i < points.size(); i++){
			if(points.get(i)[current.splitDim] - current.point[current.splitDim] < 0) {
				//debug("Left");
				leftPoints.add(points.get(i));
			}
			if(points.get(i)[current.splitDim] - current.point[current.splitDim] > 0) {
				//debug("right");
				rightPoints.add(points.get(i));
			}
			if(points.get(i)[current.splitDim] - current.point[current.splitDim] == 0) {
				current.point = points.get(i);
				countLeaves++;
			}
		}
		
		if(leftPoints.size() >= 1){
			treeElem left = new treeElem();
			current.left = left;
			left.parent = current;
			addToTree(leftPoints,left, current.splitDim + 1);
		}
		if(rightPoints.size() >= 1){
			treeElem right = new treeElem();
			current.right = right;
			right.parent = current;
			addToTree(rightPoints, right, current.splitDim + 1);
		}
	}
	
	public treeElem findPath(float[] point)
	{
		treeElem current = head;
		Boolean found = false;
		int depth = 0;
		
		while(!found){
			printPoint(current);
			if(current.point == point){
				debug("\nFound Point!");
				debug("Depth: " + depth);
				found = true;
				return current;
			}
			else if(point[current.splitDim] < current.point[current.splitDim]){
				debug("L");
				if(current.left == null){
						debug("found optimal left");
						return current;
				}
				current = current.left;
				depth ++;
			}
			else if(point[current.splitDim] >= current.point[current.splitDim]){
				debug("R");
				if(current.right == null){
					debug("found optimal left");
					return current;
				}
				current = current.right;
				depth ++;
			}
			else{
				System.out.print("\nCan't find point...");
			}
			
		}
		
		return null;
		
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
	
	
	public void tSubtree(float[] point, treeElem current){
		debug("\n\nT'ing...");
		printPoint(current);
		
		if(current.point != null){
			//Found leaf;
			debug("Leaf");
			addToKNNBuffer(point, current.point);
		}
		if(current.right != null)
			tSubtree(point, current.right);

		debug("T Right Completed");
			
		
		if(current.left != null)
			tSubtree(point,current.left);
		debug("T Left Completed");
			
		
		
	}
	
	
	public void traverseSubtree(float[] point, treeElem current){

		debug("\n\nTraversing...");
		printPoint(current);
		
		if(current.point != null){
			//Found leaf;
			debug("Leaf");
			addToKNNBuffer(point, current.point);
		}
		if(point[current.splitDim] < current.point[current.splitDim]){
			//Traverse right subtree
			debug("Traverse Right");
			if(current.right != null)
				tSubtree(point, current.right);

			debug("Traverse Right Completed");
			
		}
		if(point[current.splitDim] >= current.point[current.splitDim]){
			//Traverse left subtree
			debug("Traverse Left");
			if(current.left != null)
				tSubtree(point,current.left);
			debug("Traverse Left Completed");
			
		}
	}
	
	
	
	public void printPoint(treeElem a){
		debug("\n***********************");
		debug("Value: " + a.point[0] + ", " + a.point[1]);
		debug("Parent: " + a.parent);
		debug("Left: " + a.left);
		debug("Right: " + a.right);
		debug("Dim: " + a.splitDim);
		debug("***********************\n");
	}
	
	public treeElem searchTree(float[] point){
		debug("Searching Tree for: " + point.toString());
		
		treeElem d = findPath(point);
		printPoint(d);
		
		return d;	
		
	}
	
	
	public void createTree(List<float[]> points){
		head = new treeElem();
		
		addToTree(points, head, 0);
		debug("Leaves: " + countLeaves);
		return;
	}
	
	
	public float treeNN(int k, float[] p){
		curMax = 10000;
		KNNSet.clear();
		//TODO: Return nearest neighbors
		this.k = k;
		treeElem d = searchTree(p);
		//d = d.parent;
		
		while(boundsCheck(d, p)){
			printPoint(d);
			
			traverseSubtree(p,d);
			
			
			
			if(d.parent == null)
				return curMax;
			d = d.parent;
		}
		
		return curMax;
	}
	
	public boolean boundsCheck(treeElem current, float[] point){
		
		float bound = (float) (Math.pow(current.point[current.splitDim] - point[current.splitDim],2));
		debug("Bound Check:  " + bound + " < " + curMax);
		if(bound < curMax)
			return true;
		
		return false;
	}
	
	
	public static void main(String[] args){
		GenerateData g = new GenerateData(20,100,100);
		KDTreeNN tree = new KDTreeNN();
		
		ArrayList<float[]> testPoints = new ArrayList<float[]>();
		testPoints.add(new float[] {(float) 1.0, (float) 2.0});
		testPoints.add(new float[] {(float) 3.0, (float) 5.0});
		testPoints.add(new float[] {(float) 8.0, (float) 8.0});
		testPoints.add(new float[] {(float) 9.0, (float) 1.0});
		testPoints.add(new float[] {(float) 2.0, (float) 3.0});
		testPoints.add(new float[] {(float) 0.0, (float) 7.0});
		testPoints.add(new float[] {(float) 9.1, (float) 4.0});
		testPoints.add(new float[] {(float) 4.0, (float) 10.0});
		
		
		tree.createTree(g.data);
		//tree.createTree(testPoints);
		System.out.println("Created tree...");
		
		//System.out.println("Traversing subtree for: " + g.data.get(1).toString());
		//tree.traverseSubtree(g.data.get(1),tree.head);
		//System.out.println("Output: " + tree.treeNN(2, new float[] {(float) 5.0, (float) 6.0}));
		System.out.println("Output: " + tree.treeNN(2, g.data.get(1)));
		LSHkNN querier = new LSHkNN(g.data.get(0).length, 2);
		querier.createDB(g.data);
		
		float[] p = querier.knn(10, g.data.get(1)).get(1);
		float euc = 0;
		for(int i = 0; i < p.length; i++){
			euc = (float) (euc + (Math.pow(p[i] - g.data.get(1)[i],2)));
		}
		System.out.println("LSH: " + euc);
		System.out.println("Completed");
		return;
	}
	
	
}


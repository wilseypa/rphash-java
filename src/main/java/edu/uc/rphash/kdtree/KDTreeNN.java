package edu.uc.rphash.kdtree;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import edu.uc.rphash.lsh.LSHkNN;
import edu.uc.rphash.tests.generators.GenerateData;
import edu.uc.rphash.util.VectorUtil;

final class treeElem{
	treeElem left;				//Left branch of tree
	treeElem right;				//Right branch of tree
	treeElem parent;			//Hold the parent for traversal up
	float[] point = null;		//Point value of leaf
	int splitDim;				//Dimension of the axis split
}

	
final class sortTree{
	TreeSet<float[]> points;
	int splitDim;
	
	class fComp implements Comparator<float[]>{
		public int compare(float[] f1, float[] f2){
			if(f1[splitDim]-f2[splitDim] < 0)
				return -1;
			if(f1[splitDim]-f2[splitDim] == 0)
				return 0;
			if(f1[splitDim]-f2[splitDim] > 0)
				return 1;
			return 0;
		}
	}
	
	public sortTree(int splitDim, List<float[]> points){
		this.points = new TreeSet<float[]>(new fComp());
		this.splitDim = splitDim;		
		this.points.addAll(points);
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
	List<float[]> KNNSet = new ArrayList<float[]>();
	float curMax = 10000;
	int k = 3;
	treeElem head = null;
	boolean isDebug = false;
	
	public void debug(String s){
		if(isDebug)
			System.out.println(s);
	}
	
	public float[] getMedian(int dimension, List<float[]> points){
		sortTree tempTree = new sortTree(dimension,points);
		
		return tempTree.get((int) (tempTree.points.size()+1) / 2);
	}
	
	public void addToTree(List<float[]> points, treeElem current, int axis){
		List<float[]> leftPoints = new ArrayList<float[]>();
		List<float[]> rightPoints = new ArrayList<float[]>();
		
		current.splitDim = axis % points.get(0).length;
		current.point = getMedian(current.splitDim, points);
		
		for(int i = 0; i < points.size(); i++){
			if(points.get(i)[current.splitDim] - current.point[current.splitDim] < 0)
				leftPoints.add(points.get(i));
			if(points.get(i)[current.splitDim] - current.point[current.splitDim] > 0) 
				rightPoints.add(points.get(i));
			if(points.get(i)[current.splitDim] - current.point[current.splitDim] == 0) {
				current.point = points.get(i);
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
				if(current.left == null)
						return current;
				current = current.left;
				depth ++;
			}
			else if(point[current.splitDim] >= current.point[current.splitDim]){
				if(current.right == null)
					return current;
				current = current.right;
				depth ++;
			}			
		}		
		return null;
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
	
	
	public void tSubtree(float[] point, treeElem current){
		printPoint(current);
		
		if(current.point != null){
			addToKNNBuffer(point, current.point);
		}
		if(current.right != null)
			tSubtree(point, current.right);
		
		if(current.left != null)
			tSubtree(point,current.left);
	}
	
	
	public void traverseSubtree(float[] point, treeElem current){
		printPoint(current);
		
		if(current.point != null){
			addToKNNBuffer(point, current.point);
		}
		if(point[current.splitDim] < current.point[current.splitDim]){
			//Traverse right subtree
			if(current.right != null)
				tSubtree(point, current.right);			
		}
		if(point[current.splitDim] >= current.point[current.splitDim]){
			if(current.left != null)
				tSubtree(point,current.left);
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
		treeElem d = findPath(point);
		printPoint(d);
		
		return d;	
		
	}
	
	
	public void createTree(List<float[]> points){
		head = new treeElem();
		
		addToTree(points, head, 0);
		return;
	}
	
	
	public List<float[]> treeNN(int k, float[] p){
		curMax = 10000;
		KNNSet.clear();

		this.k = k;
		treeElem d = searchTree(p);
		
		while(boundsCheck(d, p)){
			printPoint(d);
			
			traverseSubtree(p,d);
					
			if(d.parent == null)
				return KNNSet;
			d = d.parent;
		}		
		return KNNSet;	
	}
	
	public float treeNNEuc(int k, float[] p){
		curMax = 10000;
		KNNSet.clear();
		
		this.k = k;
		treeElem d = searchTree(p);
		
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
		if(bound < curMax)
			return true;
		
		return false;
	}
	
	public static void main(String[] args){
		GenerateData g = new GenerateData(20,100,100);
		KDTreeNN tree = new KDTreeNN();		
		
		tree.createTree(g.data);
		
		List<float[]> testOutput = tree.treeNN(2, g.data.get(1));
		VectorUtil.prettyPrint(testOutput);
		
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


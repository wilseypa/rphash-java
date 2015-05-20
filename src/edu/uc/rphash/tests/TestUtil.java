package edu.uc.rphash.tests;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import edu.uc.rphash.projections.Projector;

public class TestUtil {
	
	/**Return the euclidean distance between two vectors x and y
	 * Returns infinite if vectors are misaligned
	 * @param x
	 * @param y
	 * @return
	 */
	public static float distance(float[] x,float[] y)
	{
		if(x.length<1)return Float.MAX_VALUE;
		if(y.length<1)return Float.MAX_VALUE;
		float dist = (x[0]-y[0])*(x[0]-y[0]) ;
		for(int i = 1 ;i< x.length; i++)dist += ((x[i]-y[i])*(x[i]-y[i]));
		return (float)Math.sqrt(dist);
	}
	/** Resturns the euclidean distance between a vector region {i-k} of x with a vector region {j-k} of y
	 * @param y second vector
	 * @param i start indece of x
	 * @param j start indece of y
	 * @param k compare length
	 * @return
	 */
	public static float distance(float[] x,float[] y,int i,int j,int k)
	{
		if(x.length<1)return Float.MAX_VALUE;
		if(y.length<1)return Float.MAX_VALUE;

		float dist = (x[i+0]-y[j+0])*(x[i+0]-y[j+0]) ;
		for(int ii = 1 ;ii< k; ii++)
			dist += ((x[i+ii]-y[j+ii])*(x[i+ii]-y[j+ii]));
		
		
		return (float)Math.sqrt(dist);
	}
	
	/**Linear search for x's nearest neighbor in DB
	 * @param x
	 * @param DB
	 * @return
	 */
	public static int findNearestDistance(float[] x,List <float[]> DB)
	{
		float mindist = distance(x,DB.get(0));
		int minindex = 0;
		float tmp;
		for(int i=1;i<DB.size();i++){
			tmp = distance(x,DB.get(i));
			if(tmp <= mindist){
				mindist = tmp;
				minindex = i;
			}
		}
		return minindex;
	}
	
	/**Linear search for x's nearest neighbor in DB
	 * @param x
	 * @param DB
	 * @return
	 */
	public static int findNearestDistance(float[] x,List <float[]> DB,float[] dist)
	{
		float mindist = distance(x,DB.get(0));
		int minindex = 0;
		float tmp;
		for(int i=1;i<DB.size();i++){
			tmp = distance(x,DB.get(i));
			if(tmp <= mindist){
				mindist = tmp;
				minindex = i;
			}
		}
		dist[0] = mindist;
		return minindex;
	}
	
	
	
	
	
	public static int findNearestDistance(float[] x,List <float[]> DB, HashSet<Integer> taken)
	{
		float mindist = Float.MAX_VALUE;//distance(x,DB.get(0));
		int minindex = 0;
		float tmp;

		for(int i=0;i<DB.size();i++){
			if(!taken.contains(i)){
			tmp = distance(x,DB.get(i));
			if(tmp <= mindist){
				mindist = tmp;
				minindex = i;
			}}
		}
		return minindex;
	}
	
	/**Print a matrix, compress if the output is too big
	 * @param mat
	 */
	public static void prettyPrint(float[][] mat){
		ArrayList<float[]> tmp = new ArrayList<float[]>();
		for(float[] m : mat)tmp.add(m);
		prettyPrint(tmp);
	}
	
	/**Print a matrix, compress if the output is too big
	 * @param mat
	 */
	public static void prettyPrint(List<float[]> mat){
		int m = mat.size();
		int n = mat.get(0).length;
		boolean curtailm = m>10;
		if(curtailm){
			for(int i=0;i<4;i++){
				prettyPrint(mat.get(i));
			}
			for(int j = 0;j<n/2;j++)System.out.print("\t");
			System.out.print(" ...\n");
			for(int i=mat.size()-4;i<mat.size();i++){
				prettyPrint(mat.get(i));
			}
		}else{
			for(int i=0;i<mat.size();i++){
				prettyPrint(mat.get(i));
			}
		}
	}
	
	

	/**Print a vector, compress if the output is too big
	 * @param mat
	 */
	public static void prettyPrint(Integer[] mat){
		int n = mat.length;
		boolean curtailm = n>10;
		if(curtailm){
			for(int i=0;i<4;i++){
				if(mat[i]>0)System.out.printf(" ");
				System.out.printf("%.4f ",mat[i]);
			}
			System.out.print("\t ... \t");
			for(int i=mat.length-4;i<mat.length;i++){
				if(mat[i]>0)System.out.printf(" ");
				System.out.printf("%.4f ",mat[i]);
			}
		}else{
			for(int i=0;i<mat.length;i++){
				if(mat[i]>0)System.out.printf(" ");
				System.out.printf("%.4f ",mat[i]);
			}
		}
		System.out.printf("\n");
	}
	
	/**Print a vector, compress if the output is too big
	 * @param mat
	 */
	public static void prettyPrint(float[] mat){
		int n = mat.length;

		boolean curtailm = n>10;
		if(curtailm){
			for(int i=0;i<4;i++){
				if(mat[i]>0)System.out.printf(" ");
				System.out.printf("%.4f ",mat[i]);
			}
			System.out.print("\t ... \t");
			for(int i=mat.length-4;i<mat.length;i++){
				if(mat[i]>0)System.out.printf(" ");
				System.out.printf("%.4f ",mat[i]);
			}
		}else{
			for(int i=0;i<mat.length;i++){
				if(mat[i]>0)System.out.printf(" ");
				System.out.printf("%.4f ",mat[i]);
			}
		}
	}
	
	void pp(
			long ret,
			int ct,
			int grsize)
	{
		int i,j;//,err;
		for(i=0;i<ct;i++)
		{
			for(j=0;j<grsize;j++)
			{
				System.out.printf("%li",ret&1);
				//err +=ret&1;
				ret=ret>>>1;

			}
			System.out.printf(" ");
		}
		//if(err%2) printf("error \n");else
		System.out.printf("\n");
	}
	
	/** Find the best labeling map from a set of known centroids
	 * to a set of experimental centroids. greedy method.
	 * @param estCentroids
	 * @param realCentroids
	 * @return
	 */
	public static List<float[]> alignCentroids(List<float[]> estCentroids, List<float[]> realCentroids)
	{
		List<float[]> aligned = new ArrayList<float[]>(realCentroids.size());
		for(int i = 0 ; i< realCentroids.size();i++)aligned.add(new float[0]);
		
		for(float[] estCentroid:estCentroids)
		{
			int index = TestUtil.findNearestDistance(estCentroid,realCentroids);
			aligned.set(index, estCentroid);
		}
		return aligned;
	}
	
	/**write a simple matrix format of 
	 * row[newline]
	 * col[newline]
	 * data_1_1[newline]
	 * ...[newline]
	 * data_||row||_||col||
	 * @param data - list of float arrays
	 * @param output - file
	 */
	public static void writeFile(File output,List<float[]> data){
		BufferedWriter out = null;
		try {
			out = new BufferedWriter(new FileWriter(output));
			out.write(String.valueOf(data.size())+"\n");
			out.write(String.valueOf(data.get(0).length)+"\n");
			for(float[] vector:data){
				for(float v:vector)out.write(String.valueOf(v)+"\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		if(out!=null)
			try {
				out.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
	
	public static float max(float[] l){
		float mx = l[0];
		for(int i = 1; i<l.length;i++){
			if(l[i]>mx){
				mx=l[i];
			}
		}
		return mx;
	}

	public static double max(List l){
		double mx = (double)l.get(0);
		for(int i = 1; i<l.size();i++){
			if((double)l.get(i) > mx){
				mx=(double)l.get(i);
			}
		}
		return mx;
	}
	
	
	public static float min(float[] l){
		float mx = l[0];
		for(int i = 1; i<l.length;i++){
			if(l[i]<mx){
				mx=l[i];
			}
		}
		return mx;
	}
	/**Walk along a byte outputting the bits bigendian
	 * @param b
	 * @return
	 */
	public static String b2s(byte b){
		String s = "";
		for(int i =0;i<8;i++){
			s+= Integer.valueOf(b)&1;
			b>>>=1;
		}
		return s;
	}
	
	
	/**Read a simple matrix format of 
	 * row[newline]
	 * col[newline]
	 * data_1_1[newline]
	 * ...[newline]
	 * data_||row||_||col||
	 * @param input
	 * @return
	 */
	public static List<float[]> readFile(File input){
		BufferedReader in = null;
		List<float[]> M = null;
		try {
			in = new BufferedReader(new FileReader(input));
			int m = Integer.parseInt(in.readLine());
			int n = Integer.parseInt(in.readLine());
			M = new ArrayList<float[]>(m);
			for(int i =0;i<m;i++){
				float[] vec = new float[n];
				for(int j =0;j<n;j++)
					vec[j] = Float.parseFloat(in.readLine());
				M.add(vec);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		if(in!=null){
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return M;
	}
	
	/**Print a byte array 8 bits at a time
	 * @param b
	 */
	public static void prettyPrint(byte[] b){
		for(int i =0;i<b.length;i++){
			System.out.print(b2s(b[i])+",");
		}
		System.out.println();
	}
	
	public static void prettyPrint(long b){
		
		byte chnk = (byte) (b&0xFF);
		for(int i =0;i<8;i++){
			System.out.print(b2s(chnk)+",");
			b>>>=8;
			chnk = (byte) (b&0xFF);
		}
		System.out.println();
	}
	
	public static void prettyPrint(char[] b){
		for(int i =0;i<b.length;i++){
			System.out.print(b2s((byte)b[i])+",");
		}
		System.out.println();
		
	}
	

	
	/** Print a diagonal distance matrix of all (n-1)(n-2)/2 distances
	 * @param b
	 * @param d
	 */
	public static void printDistanceMatrix (float[] b,int d){
		
		int s = d/b.length;
		for(int i = 0; i < s;i++)
		{
			for(int j = 0; j < s;j++)
			{
				System.out.printf("%.3f",TestUtil.distance(b,b,i*d,j*d,d));
				System.out.print("\t");
			}
			System.out.println();
		}

		
	}
	
	/**
	 * Get the average projection of a vector under multiple matrix projections
	 * @param v
	 * @param p
	 * @param dim
	 * @return
	 */
	public static float[] avgProjection(float[] v, Projector[] p, int dim) {
		float[] ravg = new float[dim];
		float sclr = 1f / (float) p.length;
		for (int i = 0; i < dim; i++)
			ravg[i] = 0.0f;
		for (int i = 0; i < p.length; i++) {
			float[] r1 = p[i].project(v);
			for (int j = 0; j < dim; j++)
				ravg[j] += (r1[j] * sclr);
		}

		return ravg;
	}
	
	public static float[] normalize(float[] x){
		float length =0;
		for(int i = 0; i<x.length;i++)length+=(x[i]*x[i]);
		length = (float) Math.sqrt(length);
		for(int i = 0; i<x.length;i++)x[i]/=length;
		return x;
	}
	public static double distance(double[] x, double[] y) {
		if(x.length<1)return Double.MAX_VALUE;
		if(y.length<1)return Double.MAX_VALUE;
		double dist = (x[0]-y[0])*(x[0]-y[0]) ;
		for(int i = 1 ;i< x.length; i++)dist += ((x[i]-y[i])*(x[i]-y[i]));
		return Math.sqrt(dist);
	
	}
	public static float[] scale(float[] x, float variance) {
		for(int i = 0; i<x.length;i++)x[i]/=variance;
		return x;
	}
	

}

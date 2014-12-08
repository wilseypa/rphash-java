package edu.uc.rphash.tests;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TestUtil {
	static float distance(float[] x,float[] y)
	{
		if(x.length<1)return Float.MAX_VALUE;
		if(y.length<1)return Float.MAX_VALUE;
		float dist = (x[0]-y[0])*(x[0]-y[0]) ;
		for(int i = 1 ;i< x.length; i++)dist += ((x[i]-y[i])*(x[i]-y[i]));
		return dist;
	}
	
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
	
	public static void prettyPrint(float[][] mat){
		ArrayList<float[]> tmp = new ArrayList<float[]>();
		for(float[] m : mat)tmp.add(m);
		prettyPrint(tmp);
	}
	
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
		System.out.printf("\n");
	}
	
	
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
	
	public static void prettyPrint(byte[] b){
		for(int i =0;i<b.length;i++){
			System.out.print(b2s(b[i])+",");
		}
		System.out.println();
		
	}
	

}

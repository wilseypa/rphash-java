package edu.uc.rphash.tests;

import java.util.List;

import edu.uc.rphash.Clusterer;
import edu.uc.rphash.Readers.RPHashObject;
import edu.uc.rphash.Readers.SimpleArrayReader;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Random;

/**
 * @author lee
 * learns mle model with T topics from words x docs counts data
 */
/*
 * All private methods are so because they for speed reasons do not employ any
 * form of checking for numerical stability. In the case of mle matrices this is
 * acceptable as probability matrices are never negative and the dimensions of 
 * the matrices do not change.
 * for a unit test of mle, mle will be used to produce the NMF of a matrix, functionality
 * and correctness can be confirmed by finding the product to be equal to the input 
 * matrix
 */
public class MLE2 implements Clusterer {


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		float[][] F = {{1f, 0f, 0f, 2f, 0f},{1f, 1f, 1f, 0f, 0f}, {0f, 0f, 1f, 1f , 1f},{2f, 1f, 1f, 0f, 1f},{1f, 0f, 0f, 2f, 0f},{1f, 1f, 1f, 0f, 0f}, {0f, 0f, 1f, 1f , 1f},{2f, 1f, 1f, 0f, 1f}};
		for(float[] ff: F){
			System.out.println();
			for(float f: ff){
				System.out.print(f + " ");
			}
		}System.out.println();
		
		
		MLE2 mlobj = new MLE2(F,4,.00001f);

		printmat(normalize(F));
		printmat(mlobj.wt);printmat(mlobj.td);
		printmat( multiply(mlobj.wt,mlobj.td));

	}
	
	
	int W;//words, rows
	int D;//documents, columns
	int T;// topics or latent classes

	public float[][] td;
	public float[][] wt;
	float[][] counts;
	
	public MLE2(float[][] counts, int T,float epsilon)
	{
		this.counts = counts;
		W=counts.length;
		D = counts[0].length;
		this.T = T;
		mle(epsilon);
	}
	
	// use if you want wt initialized to some specific value
	public void mle( float epsilon)
	{
		float tot = sum(counts);
		td = normalize(ones(T,D));
		wt = normalize(rand(W,T));
		
		
		float[] E = sum1D(logDotProduct(counts,multiply(wt,td)));
		float F = sum(E)/tot;
		float F_new ;
		float rel_ch;
		

		do
		{
			// Expectation Step
			// td = norm(td .* ( wt' * ( counts ./ (wt * td) ) ));
			td = normalize(dotProduct(td,(multiply(transpose(wt),dotDivide(counts,multiply(wt,td))))));
			
			//maximization step
			//wt = normalize( wt .* ( ( counts ./ ( wt * td + eps ) ) * td' ))
			wt = normalize(dotProduct(wt,multiply(dotDivide(counts,multiply(wt,td)),transpose(td))));
			
			//calculate log-likelihood
		/* 
		 *   ___       ___
		 *   \		     \
		 *   /__	     /__     n(d,w) log P(d,w)
		 *  d c D   w c W
		*/
			E = sum1D(logDotProduct(counts,multiply(wt,td)));
			F_new = sum(E)/tot;
			
			//calculate iteration's relative change to determine convergence
			rel_ch = Math.abs((F_new - F))/ Math.abs(F);
			F= F_new;
			
			System.out.println(rel_ch);
			
		}while(rel_ch>epsilon);
		
	}
	

	//testing status - works
	//gets the pairwise products of two matrices
	//no dimension checking
	private static float[][] dotProduct(float[][] mat1, float[][] mat2)
	{
		float[][] rtrn = new float [mat1.length ][mat1[0].length];
		
		for(int i = 0;i<mat1.length;i++){
			for(int j = 0;j<mat1[0].length;j++)rtrn[i][j] = mat1[i][j]*mat2[i][j];
		}
		return rtrn;
	}
	
	//testing status - works
	//gets the pairwise division of two matrices
	//no dimension checking
	private static float[][] dotDivide(float[][] mat1, float[][] mat2)
	{
		float[][] rtrn = new float [mat1.length ][mat1[0].length];
		
		for(int i = 0;i<mat1.length;i++){
			for(int j = 0;j<mat1[0].length;j++)rtrn[i][j] = mat1[i][j]/(mat2[i][j]+Float.MIN_VALUE);
		}
		return rtrn;
	}
	
	
	//testing status - works
	//find the pairwise product of mat1 and log(mat2)
	//no dimension checking
	private static float[][] logDotProduct(float[][] mat1, float[][] mat2)
	{
		float[][] rtrn = new float [mat1.length ][mat1[0].length];
		
		for(int i = 0;i<mat1.length;i++){
			for(int j = 0;j<mat1[0].length;j++)
				rtrn[i][j] = mat1[i][j]*(float)Math.log(mat2[i][j] + Float.MIN_VALUE);
		}
		return rtrn;
	}
	
	//testing status - works
	//create a random matrix to with which we will initialize mle
	//ding, he, zha, simon suggest using in svd reduced matrices to initialize this matrix
	//farahat and chen suggest initializing with svd center's
	//hoffman suggests multiple attempts at mle with differenent random initializations
	public static float[][] rand(int w,int t)
	{
		Random r= new Random(8589934591L);// a mersenne prime
		float[][] rand = new float[w][t];
		for(int i = 0;i<w;i++){
			for(int j = 0;j<t;j++)rand[i][j]=r.nextFloat();
		}
		return rand;
	}
	
	//testing status - works
	//create a matrix of all ones of the specified dimension
	//future improvements will provide a normalized matrix of ones function
	public static float[][] ones(int w,int t)
	{
		float[][] ones = new float[w][t];
		for(int i = 0;i<w;i++){
			for(int j = 0;j<t;j++)ones[i][j]=1;
		}
		return ones;
	}
	
	
	//testing status - works
	//give the sum of all the elements of a matrix
	public static float sum(float[][] A)
	{
		float sum =0;
		for(float[] ff:A){
			for(float f:ff) sum+=f;
		}
		return sum;
	}
	
	//testing status - works
	//give the sum of all the elements of a vector
	public static float sum(float[]A)
	{
		float sum =0;
			for(float f:A) sum+=f;
		return sum;
	}
	
	//testing status - works
	//give the column vector sum of all the elements of a matrix
	public static float[] sum1D(float[][] A)
	{
		float[] sum = new float[A[0].length];
		Arrays.fill(sum, 0f);
		for(float[] ff:A)
		{
			for(int i = 0;i<ff.length;i++)
				sum[i] += ff[i];
		}
		return sum;
	}
	
	
//	testing status - works
	//beware of caching problems, this is a classic bad array access loop
	// lee carraher will fix it when it becomes a problem
	public static float[][] normalize(float[][] A)
	{
		for(int i = 0; i<A[0].length;i++)
		{
			float sum = 0;
			for(int j = 0; j<A.length;j++)
			{
				sum+=A[j][i];
			}
			for(int j = 0; j<A.length;j++)
			{
				if(sum==0)
					A[j][i]=0;
				else
					A[j][i] /=sum;
			}
		}
		return A;
	}
	
	//testing status - works
	//naive multiply for testing, to be replaced with straussen or winograd's
	// this will be a dense multiply (rand(T,K) * ones(K,D))
	//no dimension checking
	private static float[][] multiply(float[][] in1, float[][] in2){
		float[][] rtrn = new float[in1.length][in2[0].length];		
		for(int i = 0; i<in1.length;i++)
		{
			for(int j = 0; j<in2[0].length;j++)
			{
				rtrn[i][j] = 0f;
				for(int k=0; k<in2.length;k++)
				{
					rtrn[i][j]+=in1[i][k]*in2[k][j];	
				}	
			}
		}
		return rtrn;
	}

	//testing status - works
	//simple method to transpose an array
	public static float[][] transpose(float[][] in)
	{
		float[][] rtrn = new float[in[0].length][in.length];
		
		for(int i = 0; i<in.length;i++)
		{
			for(int j=0; j<rtrn.length;j++)
			{
				rtrn[j][i]=in[i][j];
			}
		}
		
		return rtrn;
	}
	
	public static void printmat(float[][] D){
		for(float[] dd: D){
			System.out.println();
			for(float d: dd){
				System.out.print(d + " ");
			}
		}
		System.out.println();
	}
	


	@Override
	public List<float[]> getCentroids() {
		
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RPHashObject getParam() {
		return new SimpleArrayReader(null, 0);
	}

}

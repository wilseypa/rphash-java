package edu.uc.rphash.projections;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class DBFriendlyProjection implements Projector {
	int RAND_MAX = 2147483647;
	int[][] M;// minus
	int[][] P;// plus
	int n;
	int t;
	Random rand;

	public DBFriendlyProjection(int n, int t) {
		this.n = n;
		this.t = t;
		rand = new Random();
		GenRandom();
	}

	public DBFriendlyProjection(int n, int t, long randomseed) {
		this.n = n;
		this.t = t;
		rand = new Random(randomseed);
		GenRandom();
	}

	/*
	 * from Achlioptas 01 and JL -THm r_ij = sqr(3/m)*| +1 Pr =1/6 | 0 Pr=2/3 |
	 * - 1 Pr =1/6
	 * 
	 * Naive method O(n), faster select and bookkeeping should be O((5/12 )n),
	 * still linear
	 */
	void GenRandom() {
		M = new int[t][];
		P = new int[t][];
		int r = 0;
		for (int i = 0; i < t; i++) {
													   //approx size
			List<Integer> orderedM = new ArrayList<Integer>(n / 6);
			List<Integer> orderedP = new ArrayList<Integer>(n / 6);
			for (int j = 0; j < n; j++) {
				r = rand.nextInt(6);
				if (r == 0)
					orderedM.add(j);
				if( r == 1)
					orderedP.add(j);
			}
			
			Collections.sort(orderedM);

			M[i] = new int[orderedM.size()];
			int j = 0;
			for (Integer in : orderedM)
				M[i][j++] = in;
			
			P[i] = new int[orderedP.size()];
			j = 0;
			Collections.sort(orderedP);
			for (Integer in : orderedP)
				P[i][j++] = in;
			
		}

	}

	@Override
	public float[] project(float[] v) {
		return projectN(v, P, M, n, t);
	}

	// v: the input vector
	// P: the size [t x n/6] set of vector indices that should be positive
	// +sqrt(3/t)
	// M: the size [t x n/6] set of vector indices that should incur negative
	// -sqrt(3/t)
	// n: original dimension
	// t: target/projected dimension
	static float[] projectN(float[] v, int[][] P, int[][] M, int n, int t) {
		float[] r = new float[t];
		float sum;
		float scale = (float) Math.sqrt(3.0f / ((float) t));
		int[] tmp;

		for (int i = 0; i < t; i++) {
			sum = 0.0f;
			
			for (int col :  M[i]) sum -= v[col] ;
			
			for(int col :  P[i]) sum += v[col] ;
			
			r[i] = sum* scale;
		}
		return r;
	}

	// WiP - bitwalking
	// do{
	// b= 0;
	// r = rand.nextInt();//2^32 we need 1/6 or roughly 3 bits per => 10 selects
	// per for faster generation
	// while(r>6){
	// if((r&0x7)<6)
	// { b++;
	// if( (r&0x7) ==0)M[i].add(j);
	// }
	// r>>=3;
	// }
	// System.out.println(b);
	// j+=b;
	// }while(j<n);

	// System.out.println(TestUtil.max(r)+":"+TestUtil.max(v));
	// System.out.println(TestUtil.min(r)+":"+TestUtil.min(v));

	// /*
	// * from Achlioptas with book keeping
	// * cost of bookkeeping is n to create, then 5/12n to check
	// * extra. but is RAND expensive in comparison
	// * Assume we will collide with constant probability
	// * Maths:
	// * prob of collision in 1/3 is 1/12, add penalty
	// * log(3/2)
	// * is it repeated intersection 1/3,1/12,1/48 converges to ...
	// * expriments:
	// * bookkeeper lengths
	// * numerical results peg log(3/2) , how many require some brushing up on
	// * series and continuous UBE
	// */
	// float GenRandomBook(int n,int m,int M[]){
	//
	// int l,i,r,j,b=(int)((float)n/(float)6);
	// float randn = (float) (1.0f/(Math.sqrt(n))) ;//variance scaled back a
	// little
	//
	// int[] bookkeeper = new int [n];
	// M = new int[2*b];
	//
	// //reset bookkeeper
	// for(l=0;l < n; l++ )bookkeeper[l]=t+1;
	// j=0;
	// for(i=0;i<t;i++)
	// {
	// for(l=0;l < b; l++ )
	// {
	// do{r =rand.nextInt()%n;}
	// while(bookkeeper[r]==l );
	// bookkeeper[r]=l;
	// M[j++] = r;
	// }
	// for(;l < 2*b; l++ )
	// {
	// do{ r =rand.nextInt()%n;}
	// while(bookkeeper[r]==l );
	// bookkeeper[r]=l;
	// M[j++] = r;
	// }
	// }
	//
	//
	// return randn;
	// }
	//
	// //project a vector using a bookkeeper matrix
	// static float[] projectBook(float[] v, int[] M, float randn, int n,int t){
	// int i,j;
	// float[] r = new float[t];
	// float sum;
	// for(i=0;i<t;i++)
	// {
	// sum = 0.0f;
	// for(j=0;j < n; j++ ){
	// if(M[i*n+j]!=0)//they are mostly 0 so worth checking
	// sum+=v[i]*(M[i*n+j]*randn);
	// }
	// r[i] = sum;
	// }
	// return r;
	// }

}

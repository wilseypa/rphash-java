package edu.uc.rphash.projections;

import java.util.Random;



public class DBFriendlyProjection implements Projector {
	int RAND_MAX= 2147483647;
	float[] M;
	int n;
	int t;
	Random rand;
	public DBFriendlyProjection(int n,int t)
	{
		this.n = n;
		this.t = t;
		rand = new Random();
		M =  GenRandom(n, t);
	}
	
	public DBFriendlyProjection(int n,int t,int randomseed)
	{
		this.n = n;
		this.t = t;
		rand = new Random(randomseed);
		M =  GenRandom(n, t);
	}
	
	float quicksqrt(float b)
	{
	    float x = 1.1f;
	    char i =0;

	    for(;i<16;i++){
	        x = (x+(b/x))/2.0f;
	    }
	    return x;
	}


	/*
	 * from Achlioptas 01 and JL -THm
	 * r_ij = sqr(3/m)*| +1    Pr =1/6
	 *                       |    0    Pr=2/3
	 *                       |  - 1   Pr =1/6
	 *
	 *                      Naive method O(n), faster select and bookkeeping
	 *                      should be O((5/12 )n), still linear
	 */
	float[] GenRandom(int m,int n){
	  float[] M = new float[n*m];
	  int i =0;
	  float scale = (float)Math.sqrt(3.0f/(m));
	  int r = 0;
	  for(i=0;i<m*n;i++)
	  {
        r = rand.nextInt(6);
        M[i] = 0.0f;
        if(r==0)M[i] = scale;
        if(r==5)M[i] = -scale;
        
	   }
	  return M;
	}
	
	@Override
	public float[] project(float[] v) {
		return projectN(v,M,n,t);
	}
	
	
	static float[] projectN(float[] v, float[] M, int n,int t){
		  int i,j;
		  float[] r = new float[t];
		  float sum;
		  for(i=0;i<t;i++)
		  {
		      sum = 0.0f;
		      for(j=0;j < n; j++ )
		          sum+=v[i]*M[i*n+j];
		      r[i] = sum;
		  }
		  
//		  System.out.println(TestUtil.max(r)+":"+TestUtil.max(v));
//		  System.out.println(TestUtil.min(r)+":"+TestUtil.min(v));
		  return r;
		}



	/*
	 * from Achlioptas with book keeping
	 *                      cost of bookkeeping is n to create, then 5/12n to check
	 *                      extra. but is RAND expensive in comparison
	 *                       Assume we will collide with constant probability
	 *        Maths:
	 *        prob of collision in 1/3 is 1/12, add penalty
	 *        log(3/2)
	 *         is it repeated intersection 1/3,1/12,1/48 converges to ...
	 *        expriments:
	 *        bookkeeper lengths
	 *        numerical results peg log(3/2) , how many require some brushing up on
	 *        series and continuous UBE
	 */
	float GenRandomBook(int n,int m,int M[]){

	    int l,i,r,j,b=(int)((float)n/(float)6);
	    float randn = 1.0f/(quicksqrt(((float)n))) ;//variance scaled back a little

	    int[] bookkeeper = new int [n];
	    M = new int[2*b];

	    //reset bookkeeper
	    for(l=0;l < n; l++ )bookkeeper[l]=t+1;
	    j=0;
	    for(i=0;i<t;i++)
	    {
	        for(l=0;l < b; l++ )
	        {
	            do{r =rand.nextInt()%n;}
	            while(bookkeeper[r]==l );
	            bookkeeper[r]=l;
	            M[j++] = r;
	        }
	        for(;l < 2*b; l++ )
	        {
	          do{ r =rand.nextInt()%n;}
	          while(bookkeeper[r]==l );
	          bookkeeper[r]=l;
	          M[j++] = r;
	        }
	    }


	    return randn;
	    }
	
	//project a vector using a bookkeeper matrix
	static float[] projectBook(float[] v, int[] M, float randn, int n,int t){
		  int i,j;
		  float[] r = new float[t];
		  float sum;
		  for(i=0;i<t;i++)
		  {
		      sum = 0.0f;
		      for(j=0;j < n; j++ ){
		    	  if(M[i*n+j]!=0)//they are mostly 0 so worth checking
		    		  sum+=v[i]*(M[i*n+j]*randn);
		      }
		      r[i] = sum;
		  }
		  return r;
		}
	
	
}

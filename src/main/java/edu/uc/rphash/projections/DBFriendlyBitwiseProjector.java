package edu.uc.rphash.projections;

import java.util.Random;

public class DBFriendlyBitwiseProjector implements Projector {

	int n;
	int t;
	float randn;
	Random rand;
	public DBFriendlyBitwiseProjector(	int n,	int t) {
		this.n = n;
		this.t = t;
		randn = quicksqrt(n);
		rand = new Random();
		
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
	
	//random project a vector, without a random matrix
	float[] project(float[] v,float randn, int n,int t){
		  int i,j;//b=(int)((float)n/(float)6);
		  float[] r = new float[t];
		  float sum = 0.0f;
		  randn = 1.0f/quicksqrt(n);

		  int b = 0;
		  for(i=0;i<t;i++)
		  {
		      for(j=0;j<n;j+=30)
		      {
		    	  //put a 1 in front so we see leading 0s
		          b = (1<<31)+rand.nextInt() ;
		          while(b>1)
		          {
		              if((b&1) == 1)
		                  sum+=randn*v[j];
		              else
		                  sum-=randn*v[j];
		              b>>=1;//next index
		          }
		      }
		      r[i] = sum;
		  }
		  return r;
		}
	
	@Override
	public float[] project(float[] t) {
		// TODO Auto-generated method stub
		return null;
	}

}

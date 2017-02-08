package edu.uc.rphash.projections;

import java.util.Random;

import edu.uc.rphash.util.VectorUtil;

/**Work In Progress to implement the Fat Johnson Lindenstrauss transform
 * under the Walsh-Hadamard vector matrix transform
 * @author lee
 *
 */
public class FJLTProjection implements Projector {
	
	int n;
	int k;
	int d;
	float [] D;
	float [] P;
	Random r ;
	public FJLTProjection( int d,int k,int n){
		r = new Random(n);
		this.n = n;
		this.k = k;
		this.d = d;
		
		float eps = (float) Math.sqrt(Math.log(n) / k);
		
		D=  generated(d);
		P = generatep(n, k, d, eps, 2);
	}
	
	
	
	void cblas_sgemv(int t, int n,float alpha,float[] M, float[] v,int startpoint,float[] result,int startoutput){
		  int i,j;
		  float sum;
		  for(i=0;i<t;i++)
		  {
		      sum = 0.0f;
		      for(j=0;j < n; j++ )
		          sum+=v[j+startpoint]*M[i*n+j];
		      result[startoutput+i] = sum*alpha;//scaled
		  }
		}




	/*
	 * Generates P matrix
	 *
	 * INPUT: size of distribution (k,d)
	 * 		  epsilon e
	 * 		  embedding type p
	 * 		  number of points n
	 * OUTPUT: P matrix
	 *
	 * Algorithm: P_ij = N(0,1/q) with probability q
	 * 				   = 0 elsewhere
	 *
	 * q = min( ( e^p-2 * (log n)^p /d ), 1 )
	 * p belongs to {1,2}
	 */
	float[] generatep(int n, int k, int d, float e, int p) {

		float [] data = new float[k * d ];
		//memset(data, 0, k * d * sizeof(float)); java defaults to 0.0f

		float q = (float) ((Math.pow(e, p - 2) * Math.pow(Math.log(n), p)) / (float)d);
		q = q < 1f ? q : 1f;

		float []rdata = new float[k * d ];

		//randn_mv(data, k, d, 0, 1 / q);
	    inv_randn(data, k, d, 0f, 1f/(float)q);
		randu(rdata, k, d);


		for (int i = 0; i < k; i++) {
			for (int j = 0; j < d; j++) {
				data[i * d + j] *= (rdata[i * d + j] < q?0f:1f);
			}
		}



		return data;

	}

	/*
	 *  Normal Distribution
	 *
	 *  INPUT: Pointer to hold the distribution data
	 *  	   Size of Distribution (m,n)
	 *  	   mean mu, variance var
	 *  OUTPUT: Matrix filled with normal distribution
	 *
	 *  Algorithm: Uses Moro's Inverse CND distribution to
	 *  generate an arbitrary normal distribution with
	 *  mean mu and variance var
	 */
	void inv_randn(float []data, int m, int n, float mu, float var){
		
		float sd = (float)Math.sqrt(var);
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) 
				data[i * n + j] = mu + sd *(float)moroinv_cnd(r.nextFloat());
		}
	}

/*
*  Uniform Distribution
*
*  INPUT: Pointer to hold the distribution data
*  	   Size of Distribution (m,n)
*  OUTPUT: Matrix filled with uniform distribution
*
*  Algorithm: Nothing Special
*/
void randu(float []data, int m, int n) {

	for (int i = 0; i < m; i++) {
		for (int j = 0; j < n; j++) {
			data[i * n + j] = r.nextFloat() ;
		}
	}

}


	/*
	 * Generates diagnol matrix D
	 *
	 * INPUT: size of matrix d
	 * OUTPUT: Matrix D
	 *
	 * Algorithm: D_ii = {-1,1} with probability 0.5
	 *
	 * Returned as a vector with length d to avoid extra space
	 * seems to be correct
	 */
	float[] generated(int d) {
		float [] data = new float[d ];
		int l,j;
		for (int i = 0; i < d;) 
		{
			l = r.nextInt();
			for(j=0; j<32 &&  i<d ;j++,i++){
				data[i] = (( l&1)==1) ? 1f:-1f;
				l>>>=1;
			}
		}
		return data;
	}
	
	 static float moroinv_cnd(float P){
		 float a1 = 2.50662823884f;
		 float a2 = -18.61500062529f;
		 float a3 = 41.39119773534f;
		 float a4 = -25.44106049637f;
		 float b1 = -8.4735109309f;
		 float b2 = 23.08336743743f;
		 float b3 = -21.06224101826f;
		 float b4 = 3.13082909833f;
		 float c1 = 0.337475482272615f;
		 float c2 = 0.976169019091719f;
		 float c3 = 0.160797971491821f;
		 float c4 = 2.76438810333863E-02f;
		 float c5 = 3.8405729373609E-03f;
		 float c6 = 3.951896511919E-04f;
		 float c7 = 3.21767881768E-05f;
		 float c8 = 2.888167364E-07f;
		 float c9 = 3.960315187E-07f;
		    float y, z;

		    if(P <= 0 || P >= 1.0){
		        //printf("MoroInvCND(): bad parameter %f\n", P);
		        //Caused by numerical instability of rand        
		        P = 0.9999f;
		    }

		    y = P - 0.5f;
		    if(Math.abs(y) < 0.42){
		        z = y * y;
		        z = y * (((a4 * z + a3) * z + a2) * z + a1) / ((((b4 * z + b3) * z + b2) * z + b1) * z + 1);
		    }else{
		        if(y > 0)
		            z = (float)Math.log(-Math.log(1.0 - P));
		        else
		            z = (float)Math.log(-Math.log(P));

		        z = c1 + z * (c2 + z * (c3 + z * (c4 + z * (c5 + z * (c6 + z * (c7 + z * (c8 + z * c9)))))));
		        if(y < 0) z = -z;
		    }
		    return z;
		}

	
	
	float[] FJLT(float [] input) {
	
		float [] result = new float[n * k];
		// default is 0.0f memset(result, 0, k * n * sizeof(float));
		//float sqrtd = 1 / sqrt(d);

		/*
		 * Process each point at once i.e each column of data
		 */
		int curr = 0;
		for(curr=0; curr < n; curr++) {
			int startpoint = curr*d;
			//float * point = data[startpoint];
			int startoutput = k*curr;
			//float * output = result + k * curr;

			int a, b, c;
			for (a = 0; a < d; a++){
	            //data[a+startpoint] *= D[a];
				input[a+startpoint] *= D[a];
	        }

			/*
			 * Do Fast Walsh transform on the point
			 */
			int l2 = (int) (Math.log(d)/Math.log(2));
			for (a = 0; a < l2; a++) {
				for (b = 0; b < (1 << l2); b += (1 << (a + 1))) {
					for (c = 0; c < (1 << a); c++) {
						float temp = input[startpoint+b + c];
						input[startpoint+b + c] += input[startpoint+b + c + (1 << a)];
						input[startpoint+b + c + (1 << a)] = temp -input[startpoint+b + c + (1 << a)];
//						float temp = data[startpoint+b + c];
//						data[startpoint+b + c] += data[startpoint+b + c + (1 << a)];
//						data[startpoint+b + c + (1 << a)] = temp -data[startpoint+b + c + (1 << a)];
					}
				}
			}

			/*
			 * Multiply with P
			 */
			//matrix vector multiplication
			// output <- alpha*A*x + y                   
			//                  m  n   alpha    a  aidx   x   xidx       beta
		    //cblas_sgemv( k, d, 1.0f/(float)d, P,data,startpoint, result,startoutput);
			cblas_sgemv( k, d, 1.0f/(float)d, P,input,startpoint, result,startoutput);
		}
		return result;
	}
	
	
	



	@Override
	public float[] project(float [] input) {

		float [] result = new float[k];


			int a, b, c;
			for (a = 0; a < d; a++){
				input[a] *= D[a];
	        }

			/*
			 * Do Fast Walsh transform on the point
			 */
			int l2 = (int) (Math.log(d)/Math.log(2));
			for (a = 0; a < l2; a++) {
				for (b = 0; b < (1 << l2); b += (1 << (a + 1))) {
					for (c = 0; c < (1 << a); c++) {
						float temp = input[b + c];
						input[b + c] += input[b + c + (1 << a)];
						input[b + c + (1 << a)] = temp -input[b + c + (1 << a)];
					}
				}
			}

			/*
			 * Multiply with P
			 */
			//matrix vector multiplication
			// output <- alpha*A*x + y                   
			//                  m  n   alpha    a  aidx   x   xidx       beta
		    //cblas_sgemv( k, d, 1.0f/(float)d, P,data,startpoint, result,startoutput);
			cblas_sgemv( k, d, 1.0f/(float)d, P,input,0, result,0);
		return result;
	}


	public static void  testALL(){
		//float[] data = new float[d*s];
//		for(int i = 0; i < s;i++){
//			for (int j=0;j<d;j++)data[i*d+j] = r.nextFloat();}
		int d = 10;
		int n = 12;
		int t = 3;
		
		
		float[] data= {
								1.f,0.f,2.f,7.f,4.f,0.f,8.f,3.f,2.f,1.f,
								4.f,0.f,8.f,3.f,2.f,1.f,4.f,2.f,4.f,5.f,
								3.f,2.f,1.f,1.f,4.f,2.f,4.f,5.f,1.f,0.f,
								2.f,7.f,4.f,0.f,8.f,4.f,0.f,8.f,3.f,2.f,
								0.f,8.f,3.f,0.f,8.f,3.f,2.f,1.f,4.f,2.f,
								4.f,5.f,2.f,1.f,4.f,1.f,0.f,2.f,7.f,4.f,
								1.f,3.f,2.f,1.f,4.f,0.f,8.f,3.f,2.f,1.f,
								4.f,2.f,4.f,5.f,0.f,2.f,7.f,4.f,0.f,8.f,
								1.f,0.f,2.f,7.f,0.f,8.f,3.f,2.f,1.f,4.f,
								2.f,4.f,4.f,0.f,8.f,3.f,2.f,1.f,4.f,5.f,
								4.f,0.f,8.f,3.f,2.f,1.f,4.f,0.f,8.f,3.f,
								2.f,1.f,4.f,2.f,4.f,5.f,1.f,0.f,2.f,7.f
								};
		
		
		float[][] dataVec= {
				{	1.f,0.f,2.f,7.f,4.f,0.f,8.f,3.f,2.f,1.f},
								{	4.f,0.f,8.f,3.f,2.f,1.f,4.f,2.f,4.f,5.f},
								{	3.f,2.f,1.f,1.f,4.f,2.f,4.f,5.f,1.f,0.f},
								{	2.f,7.f,4.f,0.f,8.f,4.f,0.f,8.f,3.f,2.f},
								{	0.f,8.f,3.f,0.f,8.f,3.f,2.f,1.f,4.f,2.f},
								{	4.f,5.f,2.f,1.f,4.f,1.f,0.f,2.f,7.f,4.f},
								{	1.f,3.f,2.f,1.f,4.f,0.f,8.f,3.f,2.f,1.f},
								{	4.f,2.f,4.f,5.f,0.f,2.f,7.f,4.f,0.f,8.f},
								{	1.f,0.f,2.f,7.f,0.f,8.f,3.f,2.f,1.f,4.f},
								{2.f,4.f,4.f,0.f,8.f,3.f,2.f,1.f,4.f,5.f},
								{4.f,0.f,8.f,3.f,2.f,1.f,4.f,0.f,8.f,3.f},
								{	2.f,1.f,4.f,2.f,4.f,5.f,1.f,0.f,2.f,7.f}
								};

		
		
		VectorUtil.printDistanceMatrix(data, d);
		FJLTProjection f = new FJLTProjection(n, t, d);
		float[] result = f.FJLT(data);
		VectorUtil.printDistanceMatrix(result, t);
		//make some distance matrices

		
		for(int i = 0; i < n;i++)
		{
			for(int j = 0; j < t;j++)
			{
				System.out.printf("%.3f",result[i*t+j]);
				System.out.print("\t");
			}
			float[] result2 = f.project(dataVec[i]);
			for(int j = 0; j < t;j++)
			{
				System.out.printf("%.3f",result2[j]);
				System.out.print("\t");
			}
			
			System.out.println();
			System.out.println();
		}
		
	}
	
	
	@Override
	public void setOrigDim(int n) {
		this.n = n;
	}

	@Override
	public void setProjectedDim(int t) {
		this.k = t;
	}

	@Override
	public void setRandomSeed(long l) {
		this.r =new Random(l);
		
	}

	@Override
	public void init() {
		float eps = (float) Math.sqrt(Math.log(n) / k);
		D=  generated(d);
		P = generatep(n, k, d, eps, 2);
	}
	
	
	

//	
//	public static void main(String[] args){
//		//int d = 1000;
//		int s = 10;
//		int t = 24;
//		int q = 16;
//		int k =2;
//		int d = 10;
//		float[] data = {1.f,0.f,2.f,7.f,4.f,0.f,8.f,3.f,2.f,1.f,4.f,0.f,8.f,3.f,2.f,1.f,4.f,2.f,4.f,5.f};
//		
//	   // inv_randn(data, k, d, 0f, 1f/(float)q);
//		//TestUtil.prettyPrint(data);
//		 testALL();
//		
//		//randu(rdata, k, d);
//		
//	}

}

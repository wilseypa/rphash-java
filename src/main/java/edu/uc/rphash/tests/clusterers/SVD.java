package edu.uc.rphash.tests.clusterers;

import java.util.ArrayList;
import java.util.Collections;

public class SVD {
	/**
	 * returns U in a. normaly U is nr*nr, but if nr>nc only the first nc
	 * columns are returned (nice, saves memory). The columns of U have
	 * arbitrary sign, also the columns corresponding to near-zero singular
	 * values can vary wildly from other implementations.
	 *This function is adapted from the c coded method from Numerical Recipes in C
	 */

	private float[][] A;
	SVDMatrix svdmat;

	public SVD(float[][] A)
	{
		this.A=A;
	}

	public void compute()
	{

		
		
		float[] D = new float[A[0].length];
		float[][] V = new float[A[0].length][A[0].length];

		svdmat = new SVDMatrix(A,D,V,A.length < A[0].length);

		svd(svdmat.getU(),svdmat.getD(),svdmat.getV());
	}

	public float[][] getU(){
		return svdmat.getV();	
	}

	public float[][] getD()
	{
		return padV(svdmat.getD());
	}

	public float[][] getVT(){
		return transpose(svdmat.getV());	
	}

	public void svd(float[][] a, float[] w, float[][] v) {
		int i, its, j, jj, k, l = 0, nm = 0;
		boolean flag;
		int m = a.length;
		int n = a[0].length;
		float c, f, h, s, x, y, z;
		float anorm = 0.f, g = 0.f, scale = 0.f;
		float[] rv1 = new float[n];

		for (i = 0; i < n; i++) {
			l = i + 1;
			rv1[i] = scale * g;
			g = s = scale = 0.f;
			if (i < m) {
				for (k = i; k < m; k++)
					scale += abs(a[k][i]);
				if (scale != 0.0) {
					for (k = i; k < m; k++) {
						a[k][i] /= scale;
						s += a[k][i] * a[k][i];
					}
					f = a[i][i];
					g = -SIGN((float)Math.sqrt(s), f);
					h = f * g - s;
					a[i][i] = f - g;
					// if (i!=(n-1)) { // CHECK
					for (j = l; j < n; j++) {
						for (s = 0, k = i; k < m; k++)
							s += a[k][i] * a[k][j];
						f = s / h;
						for (k = i; k < m; k++)
							a[k][j] += f * a[k][i];
					}
					// }
					for (k = i; k < m; k++)
						a[k][i] *= scale;
				}
			}
			w[i] = scale * g;
			g = s = scale = 0.0f;
			if (i < m && i != n - 1) { //
				for (k = l; k < n; k++)
					scale += abs(a[i][k]);
				if (scale != 0.) {
					for (k = l; k < n; k++) { //
						a[i][k] /= scale;
						s += a[i][k] * a[i][k];
					}
					f = a[i][l];
					g = -SIGN((float)Math.sqrt(s), f);
					h = f * g - s;
					a[i][l] = f - g;
					for (k = l; k < n; k++)
						rv1[k] = a[i][k] / h;
					if (i != m - 1) { //
						for (j = l; j < m; j++) { //
							for (s = 0, k = l; k < n; k++)
								s += a[j][k] * a[i][k];
							for (k = l; k < n; k++)
								a[j][k] += s * rv1[k];
						}
					}
					for (k = l; k < n; k++)
						a[i][k] *= scale;
				}
			} // i<m && i!=n-1
			anorm = Math.max(anorm, (abs(w[i]) + abs(rv1[i])));
		} // i
		for (i = n - 1; i >= 0; --i) {
			if (i < n - 1) { //
				if (g != 0.) {
					for (j = l; j < n; j++)
						v[j][i] = (a[i][j] / a[i][l]) / g;
					for (j = l; j < n; j++) {
						for (s = 0, k = l; k < n; k++)
							s += a[i][k] * v[k][j];
						for (k = l; k < n; k++)
							v[k][j] += s * v[k][i];
					}
				}
				for (j = l; j < n; j++)
					//
					v[i][j] = v[j][i] = 0.0f;
			}
			v[i][i] = 1.0f;
			g = rv1[i];
			l = i;
		}
		// for (i=IMIN(m,n);i>=1;i--) { // !
		// for (i = n-1; i>=0; --i) {
		for (i = Math.min(m - 1, n - 1); i >= 0; --i) {
			l = i + 1;
			g = w[i];
			if (i < n - 1) //
				for (j = l; j < n; j++)
					//
					a[i][j] = 0.0f;
			if (g != 0.) {
				g = 1.f / g;
				if (i != n - 1) {
					for (j = l; j < n; j++) {
						for (s = 0, k = l; k < m; k++)
							s += a[k][i] * a[k][j];
						f = (s / a[i][i]) * g;
						for (k = i; k < m; k++)
							a[k][j] += f * a[k][i];
					}
				}
				for (j = i; j < m; j++)
					a[j][i] *= g;
			} else {
				for (j = i; j < m; j++)
					a[j][i] = 0.0f;
			}
			a[i][i] += 1.0;
		}
		for (k = n - 1; k >= 0; --k) {
			for (its = 1; its <= 30; ++its) {
				flag = true;
				for (l = k; l >= 0; --l) {
					nm = l - 1;
					if ((abs(rv1[l]) + anorm) == anorm) {
						flag = false;
						break;
					}
					if ((abs(w[nm]) + anorm) == anorm)
						break;
				}
				if (flag) {
					c = 0.0f;
					s = 1.0f;
					for (i = l; i <= k; i++) { //
						f = s * rv1[i];
						rv1[i] = c * rv1[i];
						if ((abs(f) + anorm) == anorm)
							break;
						g = w[i];
						h = pythag(f, g);
						w[i] = h;
						h = 1.0f / h;
						c = g * h;
						s = -f * h;
						for (j = 0; j < m; j++) {
							y = a[j][nm];
							z = a[j][i];
							a[j][nm] = y * c + z * s;
							a[j][i] = z * c - y * s;
						}
					}
				} // flag
				z = w[k];
				if (l == k) {
					if (z < 0.) {
						w[k] = -z;
						for (j = 0; j < n; j++)
							v[j][k] = -v[j][k];
					}
					break;
				} // l==k
				x = w[l];
				nm = k - 1;
				y = w[nm];
				g = rv1[nm];
				h = rv1[k];
				f = ((y - z) * (y + z) + (g - h) * (g + h)) / (2 * h * y);
				g = pythag(f, 1.0f);
				f = ((x - z) * (x + z) + h * ((y / (f + SIGN(g, f))) - h)) / x;
				c = s = 1.0f;
				for (j = l; j <= nm; j++) {
					i = j + 1;
					g = rv1[i];
					y = w[i];
					h = s * g;
					g = c * g;
					z = pythag(f, h);
					rv1[j] = z;
					c = f / z;
					s = h / z;
					f = x * c + g * s;
					g = g * c - x * s;
					h = y * s;
					y *= c;
					for (jj = 0; jj < n; jj++) {
						x = v[jj][j];
						z = v[jj][i];
						v[jj][j] = x * c + z * s;
						v[jj][i] = z * c - x * s;
					}
					z = pythag(f, h);
					w[j] = z;
					if (z != 0.0) {
						z = 1.0f / z;
						c = f * z;
						s = h * z;
					}
					f = c * g + s * y;
					x = c * y - s * g;
					for (jj = 0; jj < m; ++jj) {
						y = a[jj][j];
						z = a[jj][i];
						a[jj][j] = y * c + z * s;
						a[jj][i] = z * c - y * s;
					}
				} // j<nm
				rv1[l] = 0.0f;
				rv1[k] = f;
				w[k] = x;
			} // its
		} // k
		// free rv1
	} // svd

	//absolute value
	static final float abs(float a) {
		return (a < 0.) ? -a : a;
	}

	//normal
	static final float pythag(float a, float b) {
		return (float)Math.sqrt((a * a + b * b));
	}

	//applies the sign of b to the absolute value of a
	static final float SIGN(float a, float b) {
		return ((b) >= 0. ? abs(a) : -abs(a));
	}

	//creates a diagonal matrix by padding the vector(v) with zeros
	public static float[][] padV(float[] v)
	{
		float rtrn[][] = new float[v.length][v.length];
		for(int i =0;i<v.length;i++)
		{
			for(int j = 0; j< v.length;j++)
			{		
				if(i==j)
					rtrn[i][j]=v[i];
				else 
					rtrn[i][j]=0F;
			}			
		}
		return rtrn;
	}

	// test it
	public static void main(String[] args) {


			float[][] u = {{1,2,3,4},{5,7,11,1},{2,5,6,3}};
			SVD svd = new SVD(u);

			System.out.print("A=");output(u);			
			svd.compute();
			System.out.print("U=");output(svd.getU());
			System.out.print("D=");output(svd.getD());
			System.out.print("V'=");output(svd.getVT());

			//multiply and see if we get the original matrix
			output(multiply(multiply(transpose(svd.getVT()),svd.getD()),transpose(svd.getU())));

	}

	//output matrix	
	public static void output(float[][] d){
		for(float dd[]:d)
		{
			System.out.print("\n\t");
			for(float ddd:dd)
				System.out.print((float)ddd + " ");
		}		
		System.out.println();
	}

	//naive multiply for testing
	public static float[][] multiply(float[][] in1, float[][] in2){
		float[][] rtrn = new float[in1.length][in2[0].length];		
		for(int i = 0; i<in1.length;i++)
		{
			for(int j = 0; j<in2[0].length;j++)
			{
				rtrn[i][j] = 0F;
				for(int k=0; k<in2.length;k++)
				{
					rtrn[i][j]+=in1[i][k]*in2[k][j];	
				}	
			}
		}
		return rtrn;
	}
	
	//dense transpose, a sparse method would be faster
	public static float[][] transpose(float[][] in){
		float[][] out = new float[in[0].length][in.length];
		for(int i = 0;i<in.length;i++){
			for(int j = 0;j<in[0].length;j++){
				 out[j][i]=in[i][j];
			}
		}
		return out;
	}
	//this functions sorts the singular value matrix by descending row, and subsequently the other two matrices corresponding columns
		public static class SVDMatrix{
			ArrayList <SVDValuePairs> svdpairs;
			float[] D;
			float[][] U;
			float[][]V;
			boolean sorted;

			
			public  SVDMatrix(float[][] u, float[] d, float[][]v, boolean transpose)
			{
				transpose = false;
				sorted = false;
				
			/*	if(transpose){	
					D=d;
					U=transpose(v);
					V=u;
				}else
				{
			*/		D=d;
					U=u;
					V=v;
			//	}
			}
			
			public void sortSingularValues()
			{
				svdpairs = new ArrayList<SVDValuePairs>(D.length);
				
				for(int i = 0; i< D.length;i++){
					float urow[] = new float[U[0].length];
					float vrow[] = new float[V[0].length];
					
					for(int j = 0; j <V[0].length;j++){
						vrow[j] = V[j][i];
					}
					
					for(int j = 0; j <U[0].length;j++){
						urow[j] = U[j][i];
					}
					svdpairs.add(new SVDValuePairs(D[i],urow,vrow));
				}
				Collections.sort(svdpairs);
				sorted = true;
			}
			
			private void fillMatrices(){
				if(!sorted)sortSingularValues();	
				
				D = new float[svdpairs.size()];

				int i = 0;
				
				U = new float[svdpairs.get(0).vrows.length][svdpairs.size()];
				V = new float[svdpairs.size()][svdpairs.size()];
				
				for(SVDValuePairs p:svdpairs){
					D[i]=p.singularValue;
					
					for(int j = 0;j < p.vrows.length;j++){
						V[j][i] = p.vrows[j];
					}
					for(int j = 0;j < p.urows.length;j++){
						U[j][i] = p.urows[j];
					}
					
					
					i++;
				}
			}
			
			
			public float[] getD(){
				if(D==null)fillMatrices();
				return D;
			}
			public float[][] getU(){
				if(U==null)fillMatrices();
				return U;
			}
			public float[][] getV(){
				if(V==null)fillMatrices();
				return V;
			}


			private class SVDValuePairs implements Comparable<SVDValuePairs>{
				float singularValue;
				float[] urows;
				float[] vrows;
				
				public SVDValuePairs(float singularValue, float[] urows,float[] vrows){
					this.singularValue = singularValue;
					this.vrows = vrows;
					this.urows = urows;
				}
				
				public int compareTo(SVDValuePairs o) {
					if(o instanceof SVDValuePairs)
					return 0;
					if(((SVDValuePairs)o).singularValue == this.singularValue)return 0;
					if(((SVDValuePairs)o).singularValue > this.singularValue)return 1;
					return -1;
				}
			}
		}
}

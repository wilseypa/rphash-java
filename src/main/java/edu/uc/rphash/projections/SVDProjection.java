package edu.uc.rphash.projections;

import java.util.List;
import java.util.Random;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;

import edu.uc.rphash.tests.generators.GenerateData;
import edu.uc.rphash.util.VectorUtil;

public class SVDProjection implements Projector{

	int n;
	int t;
	Random rand;
	float[][] proj;
	List<float[]> data;
	RealMatrix projectionmatrix = null;
	
	public SVDProjection(List<float[]> data){
		this.data = data;
	}
	
	
	@Override
	public float[] project(float[] t) {
		if(projectionmatrix==null)runSVD();
		double[] dt = new double[t.length];
		for(int i = 0;i<t.length;i++)dt[i] = t[i];
		double[] proj = projectionmatrix.preMultiply(dt);
		float[] ret = new float[proj.length];
		for(int i = 0;i<proj.length;i++)ret[i] = (float) proj[i];
		return ret;
	}

	@Override
	public void setOrigDim(int n) {
		this.n = n;
	}

	@Override
	public void setProjectedDim(int t) {
		this.t = t;
	}

	@Override
	public void setRandomSeed(long l) {
		this.rand =new Random(l);
		
	}

	@Override
	public void init() {
		runSVD();
	}


	private void runSVD() {
		int n = this.data.size();
		int m = this.data.get(0).length;
		double[][] X = new double[n][m];
		
		for(int i =0;i<n;i++){
			float[] tmp = this.data.get(i);
			for(int j =0;j<m;j++){
				X[i][j] =tmp[j];
			}
		}
		Array2DRowRealMatrix mat = new Array2DRowRealMatrix(X);
		SingularValueDecomposition decomp = new SingularValueDecomposition(mat);
		double[] singularvalues = decomp.getSingularValues();
		

		this.projectionmatrix = decomp.getV().getSubMatrix(0, this.n-1, 0, this.t-1);
		
		for(int i = 0;i<this.t;i++){
			//System.out.println(singularvalues[i]);
			double[] tmp = this.projectionmatrix.getRow(i);
			for(int j=0;j<tmp.length;j++){
				tmp[j]=tmp[j]/singularvalues[i];
				
			}
			this.projectionmatrix.setRow(i, tmp);
		}
	}
	
	public static void main(String[] args){
		
		
		for(int i =1000;i<100000;i*=1.1){
		GenerateData gen = new GenerateData(5, 1_000, i, .5f, true, 1f);
			Projector proj;
			//proj = new SVDProjection(gen.data());
			proj = new DBFriendlyProjection();
			//proj = new GaussianProjection();
			//proj = new NoProjection();
			//proj = new FJLTProjection(gen.data.size());
			proj.setOrigDim(gen.getDimension());
			proj.setProjectedDim(32);
			proj.setRandomSeed(1l);
			proj.init();
			long time = System.currentTimeMillis();
			for(float[] t: gen.data){
				proj.project(t);
				//for(float f:proj.project(t))
					//System.out.print(f+",");
				//System.out.println();
			}
			
			System.out.println(i+"\t"+(System.currentTimeMillis()-time)/1000.0);
		
		}
		
		
	}

	
}

package edu.uc.rphash.tests.clusterers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import edu.uc.rphash.Clusterer;
import edu.uc.rphash.Readers.RPHashObject;


import edu.uc.rphash.tests.generators.GenerateStreamData;

/**
 * This code is distributed under the GNU LGPL license. Author: Original
 * FORTRAN77 version by John Hartigan, Manchek Wong. C++ version by John
 * Burkardt.
 *
 * @author Java version by Lee Carraher
 * 
 *         Reference: John Hartigan, Manchek Wong, Algorithm AS 136: A K-Means
 *         Clustering Algorithm, Applied Statistics, Volume 28, Number 1, 1979,
 *         pages 100-108.
 */
public class HartiganWongKMeans implements Clusterer {

	private int k;
	private final int iter;
	private int n;
	private int m;
	private double[] a;
	private double[] c;

//	public Float[] weights = null;
	public double[] wss;
	public int[] ic1;
	public int[] nc;
	private List<float[]> cents;
	private List<float[]> data;
	// public double[] vars;

	public HartiganWongKMeans() {
		this.iter = 50;
	}

	public HartiganWongKMeans(int k, List<float[]> data) {
		this.k = k;
		this.iter = 50;
		this.n = data.get(0).length;
		this.m = data.size();
		this.data = data;
		// this.vars = new double[k];

		// initialize cluster count and within cluster sum
		this.a = new double[m * n];
		this.c = new double[k * n];
		this.ic1 = new int[m];
		this.nc = new int[k];
		this.wss = new double[k];
		// weights = new Float[m];
		// Collections.shuffle(data);

		for (int i = 0; i < m; i++) {
			// weights[i] = 1f;
			for (int j = 0; j < n; j++) {
				this.a[i + (j) * m] = data.get(i)[j];
			}
		}
	}

	/**
	 * Perform Hartigan and Wong Kmeans clustering
	 * 
	 * @param a
	 *            double[M*N], the points.
	 * @param m
	 *            int M, the number of points.
	 * @param n
	 *            int N, the number of spatial dimensions.
	 * @param c
	 *            double[K][N], the cluster centers.
	 * @param k
	 *            int K, the number of clusters.
	 * @param ic1
	 *            int[M], the cluster to which each point is assigned.
	 * @param nc
	 *            int[k] the number of points in each cluster.
	 * @param iter
	 *            the maximum number of iterations allowed
	 * @param wss
	 *            double[k], the within-cluster sum of squares of each cluster.
	 *            Errors: 0: no error was detected. 1: at least one cluster is
	 *            empty after the initial assignment. A better set of initial
	 *            clusters is needed 2: the allowed maximum number off
	 *            iterations was exceeded. 3: K is less than or equal to 1, or
	 *            greater than or equal to M.
	 */
	public int kmns() {
		
		// choose initial cluster centers
		// needs to happen before duplications
		// so we dont end up with an initial
		// centroid set of all the same thing
		
		this.m = data.size();
		this.a = new double[m*n];
		this.ic1 = new int[m];
		for (int i = 0; i < m; i++) {
			for (int j = 0; j < n; j++) {
				this.a[i + (j) * m] = data.get(i)[j];
			}
		}
//	}
		c = new double[k * n];
		for (int i = 0; i < k; i++) {
			for (int j = 0; j < n; j++) {
				c[i + j * k] = this.a[i + (j) * m];
			}
		}
		
		//convert to col X row matrix (fortran...)
//		if(weights==null){
			//no weights

//		else
//		{
//			//poor mans weights 10 is the resolution
//			float weightsum = 0;
//			for (int i = 0; i < m; i++)weightsum+=weights[i];
//			for(int i = 0; i < m; i++)
//			{
//				for(int j = 0; j < 10*(weights[i]/weightsum); j++)
//				{
//					data.add(data.get(i));
//				}
//			}
//			
//			for (int i = 0; i < m; i++) {
//				for (int j = 0; j < n; j++) {
//					this.a[i + (j) * m] = this.data.get(i)[j];
//				}
//			}
//			
//		}
		
		

		double aa;
		double da;
		double db;
		double dc;
		double[] dt = new double[2];
		int indx;
		double temp;

		int ifault = 0;

		if (k <= 1 || m <= k) {
			ifault = 3;
			return ifault;
		}
		int[] ic2 = new int[m];
		double[] an1 = new double[k];
		double[] an2 = new double[k];
		int[] ncp = new int[k];
		double[] d = new double[m];
		int[] itran = new int[k];
		int[] live = new int[k];

		//
		// For each point I, find its two closest centers, IC1(I) and
		// IC2(I). Assign the point to IC1(I).
		//
		for (int i = 0; i < m; i++) {
			ic1[i] = 1;
			ic2[i] = 2;

			for (int il = 0; il < 2; il++) {
				dt[il] = 0.0;
				for (int j = 0; j < n; j++) {
					da = a[i + j * m] - c[il + j * k];
					dt[il] = dt[il] + da * da;
				}
			}

			if (dt[1] < dt[0]) {
				ic1[i] = 2;
				ic2[i] = 1;
				temp = dt[0];
				dt[0] = dt[1];
				dt[1] = temp;
			}

			for (int l = 3; l <= k; l++) {
				db = 0.0;
				for (int j = 0; j < n; j++) {
					dc = a[i + j * m] - c[l - 1 + j * k];
					db = db + dc * dc;
				}

				if (db < dt[1]) {
					if (dt[0] <= db) {
						dt[1] = db;
						ic2[i] = l;
					} else {
						dt[1] = dt[0];
						ic2[i] = ic1[i];
						dt[0] = db;
						ic1[i] = l;
					}
				}
			}
		}
		//
		// Update cluster centers to be the average of points contained within
		// them.
		//
		for (int l = 0; l < k; l++) {
			nc[l] = 0;
			for (int j = 0; j < n; j++) {
				c[l + j * k] = 0.0;
			}
		}

		for (int i = 0; i < m; i++) {
			int l = ic1[i];
			int currentCount = nc[l - 1];
			nc[l - 1] = nc[l - 1] + 1;// weights[i].intValue();//+ 1;
			for (int j = 0; j < n; j++) {
				c[l - 1 + j * k] = c[l - 1 + j * k] * currentCount
						+ a[i + j * m];// *weights[i].intValue();
			}
		}
		//
		// Check to see if there is any empty cluster at this stage.
		//
		ifault = 1;

		for (int l = 0; l < k; l++) {
			if (nc[l] == 0) {
				ifault = 1;
				return ifault;
			}
		}

		ifault = 0;

		// normalize by the count
		for (int l = 0; l < k; l++) {
			aa = (double) (nc[l]);

			for (int j = 0; j < n; j++) {
				c[l + j * k] = c[l + j * k] / aa;
			}
			//
			// Initialize AN1, AN2, ITRAN and NCP.
			//
			// AN1(L) = NC(L) / (NC(L) - 1)
			// AN2(L) = NC(L) / (NC(L) + 1)
			// ITRAN(L) = 1 if cluster L is updated in the quick-transfer stage,
			// = 0 otherwise
			//
			// In the optimal-transfer stage, NCP(L) stores the step at which
			// cluster L is last updated.
			//
			// In the quick-transfer stage, NCP(L) stores the step at which
			// cluster L is last updated plus M.
			//
			an2[l] = aa / (aa + 1.0);

			if (1.0 < aa) {
				an1[l] = aa / (aa - 1.0);
			} else {
				an1[l] = Double.MAX_VALUE;
			}
			itran[l] = 1;
			ncp[l] = -1;
		}

		indx = 0;
		ifault = 2;

		for (int ij = 0; ij < iter; ij++) {
			//
			// In this stage, there is only one pass through the data. Each
			// point is re-allocated, if necessary, to the cluster that will
			// induce the maximum reduction in within-cluster sum of squares.
			//
			indx = optra(ic2, an1, an2, ncp, d, itran, live, indx);
			//
			// Stop if no transfer took place in the last M optimal transfer
			// steps.
			//
			if (indx == m) {
				ifault = 0;
				break;
			}
			//
			// Each point is tested in turn to see if it should be re-allocated
			// to the cluster to which it is most likely to be transferred,
			// IC2(I), from its present cluster, IC1(I). Loop through the
			// data until no further change is to take place.
			//
			indx = qtran(ic2, an1, an2, ncp, d, itran, indx);
			//
			// If there are only two clusters, there is no need to re-enter the
			// optimal transfer stage.
			//
			if (k == 2) {
				ifault = 0;
				break;
			}
			//
			// NCP has to be set to 0 before entering OPTRA.
			//
			for (int l = 0; l < k; l++) {
				ncp[l] = 0;
			}

		}
		//
		// If the maximum number of iterations was taken without convergence,
		// IFAULT is 2 now. This may indicate unforeseen looping.
		//
		if (ifault == 2) {
			System.out.println("KMNS - Warning!\n"
					+ "  Maximum number of iterations reached\n"
					+ "  without convergence.");
		}
		//
		// Compute the within-cluster sum of squares for each cluster.
		//

		// initialize wcss
		for (int l = 0; l < k; l++) {
			wss[l] = 0.0;
			for (int j = 0; j < n; j++) {
				c[l + j * k] = 0.0;
			}
		}

		// sum wcss over all vectors
		for (int i = 0; i < m; i++) {
			int ii = ic1[i] - 1;
			for (int j = 0; j < n; j++) {
				c[ii + j * k] = c[ii + j * k] + a[i + j * m];
			}
		}

		// iterate over dimensions
		for (int j = 0; j < n; j++) {
			// compute cluster centroids
			for (int l = 0; l < k; l++) {
				c[l + j * k] = c[l + j * k] / (double) (nc[l]);
			}
			// compute wss from cluster deltas
			for (int i = 0; i < m; i++) {
				int ii = ic1[i] - 1;
				da = a[i + j * m] - c[ii + j * k];
				wss[ii] = wss[ii] + da * da;
			}
		}

		// for ( int i = 0; i < k; i++ )
		// this.vars[i] = Math.sqrt(wss[i])/(double)nc[i];

		return ifault;
	}

	/**
	 * @param a
	 *            double[M][N], the points.
	 * @param m
	 *            int M, the number of points.
	 * @param n
	 *            int N, the number of spatial dimensions.
	 * @param c
	 *            double[K][N], the cluster centers.
	 * @param k
	 *            int K, the number of clusters.
	 * @param ic1
	 *            int[M], the cluster to which each point is assigned.
	 * @param ic2
	 *            int used to store the cluster which each point is most likely
	 *            to be transferred to at each step.
	 * @param nc
	 *            int[k] the number of points in each cluster.
	 * @param an1
	 *            double[k]
	 * @param an2
	 *            double[k]
	 * @param ncp
	 *            int[k]
	 * @param d
	 *            double[m]
	 * @param itran
	 *            int[k]
	 * @param live
	 *            int[k]
	 * @return indx - the number of steps since a transfer took place.
	 */
	int optra(int[] ic2, double[] an1, double[] an2, int[] ncp, double[] d,
			int[] itran, int[] live, int indx) {
		double al1;
		double al2;
		double alt;
		double alw;
		double da;
		double db;
		double dc;
		double dd;
		double de;
		double df;
		int l1;
		int l2;
		int ll;
		double r2;
		double rr;
		//
		// If cluster L is updated in the last quick-transfer stage, it
		// belongs to the live set throughout this stage. Otherwise, at
		// each step, it is not in the live set if it has not been updated
		// in the last M optimal transfer steps.
		//
		for (int l = 0; l < k; l++) {
			if (itran[l] == 1) {
				live[l] = m + 1;
			}
		}

		for (int i = 1; i <= m; i++) {
			indx = indx + 1;
			l1 = ic1[i - 1];
			l2 = ic2[i - 1];
			ll = l2;
			//
			// If point I is the only member of cluster L1, no transfer.
			//
			if (1 < nc[l1 - 1]) {
				//
				// If L1 has not yet been updated in this stage, no need to
				// re-compute D(I).
				//
				if (ncp[l1 - 1] != 0) {
					de = 0.0;
					for (int j = 0; j < n; j++) {
						df = a[i - 1 + j * m] - c[l1 - 1 + j * k];
						de = de + df * df;
					}
					d[i - 1] = de * an1[l1 - 1];
				}
				//
				// Find the cluster with minimum R2.
				//
				da = 0.0;
				for (int j = 0; j < n; j++) {
					db = a[i - 1 + j * m] - c[l2 - 1 + j * k];
					da = da + db * db;
				}
				r2 = da * an2[l2 - 1];

				for (int l = 1; l <= k; l++) {
					//
					// If LIVE(L1) <= I, then L1 is not in the live set. If this
					// is
					// true, we only need to consider clusters that are in the
					// live set
					// for possible transfer of point I. Otherwise, we need to
					// consider
					// all possible clusters.
					//
					if ((i < live[l1 - 1] || i < live[l2 - 1]) && l != l1
							&& l != ll) {
						rr = r2 / an2[l - 1];

						dc = 0.0;
						for (int j = 0; j < n; j++) {
							dd = a[i - 1 + j * m] - c[l - 1 + j * k];
							dc = dc + dd * dd;
						}

						if (dc < rr) {
							r2 = dc * an2[l - 1];
							l2 = l;
						}
					}
				}
				//
				// If no transfer is necessary, L2 is the new IC2(I).
				//
				if (d[i - 1] <= r2) {
					ic2[i - 1] = l2;
				}
				//
				// Update cluster centers, LIVE, NCP, AN1 and AN2 for clusters
				// L1 and
				// L2, and update IC1(I) and IC2(I).
				//
				else {
					indx = 0;
					live[l1 - 1] = m + i;
					live[l2 - 1] = m + i;
					ncp[l1 - 1] = i;
					ncp[l2 - 1] = i;
					al1 = (double) (nc[l1 - 1]);
					alw = al1 - 1.0;
					al2 = (double) (nc[l2 - 1]);
					alt = al2 + 1.0;
					for (int j = 0; j < n; j++) {
						c[l1 - 1 + j * k] = (c[l1 - 1 + j * k] * al1 - a[i - 1
								+ j * m])
								/ alw;
						c[l2 - 1 + j * k] = (c[l2 - 1 + j * k] * al2 + a[i - 1
								+ j * m])
								/ alt;
					}
					nc[l1 - 1] = nc[l1 - 1] - 1;// weights[i-1].intValue(); //-
												// 1;
					nc[l2 - 1] = nc[l2 - 1] + 1;// weights[i-1].intValue(); //+
												// 1;
					an2[l1 - 1] = alw / al1;
					if (1.0 < alw) {
						an1[l1 - 1] = alw / (alw - 1.0);
					} else {
						an1[l1 - 1] = Double.MAX_VALUE;
					}
					an1[l2 - 1] = alt / al2;
					an2[l2 - 1] = alt / (alt + 1.0);
					ic1[i - 1] = l2;
					ic2[i - 1] = l1;
				}
			}

			if (indx == m) {
				return indx;
			}
		}
		//
		// ITRAN(L) = 0 before entering QTRAN. Also, LIVE(L) has to be
		// decreased by M before re-entering OPTRA.
		//
		for (int l = 0; l < k; l++) {
			itran[l] = 0;
			live[l] = live[l] - m;
		}

		return indx;
	}

	/**
	 * QTRAN carries out the quick transfer stage. This is the quick transfer
	 * stage. IC1(I) is the cluster which point I belongs to. IC2(I) is the
	 * cluster which point I is most likely to be transferred to. For each point
	 * I, IC1(I) and IC2(I) are switched, if necessary, to reduce within-cluster
	 * sum of squares. The cluster centers are updated after each step.
	 * 
	 * @param ic1
	 *            int[M], the cluster to which each point is assigned.
	 * @param ic2
	 *            int used to store the cluster which each point is most likely
	 *            to be transferred to at each step.
	 * @param nc
	 *            int[k] the number of points in each cluster.
	 * @param an1
	 *            double[k]
	 * @param an2
	 *            double[k]
	 * @param ncp
	 *            int[k]
	 * @param d
	 *            double[m]
	 * @param itran
	 *            int[k]
	 * @returns indx the number of steps since a transfer took place.
	 */
	int qtran(int[] ic2, double[] an1, double[] an2, int[] ncp, double[] d,
			int[] itran, int indx) {
		double al1;
		double al2;
		double alt;
		double alw;
		double da;
		double db;
		double dd;
		double de;
		int icoun;
		int istep;
		int l1;
		int l2;
		double r2;
		//
		// In the optimal transfer stage, NCP(L) indicates the step at which
		// cluster L is last updated. In the quick transfer stage, NCP(L)
		// is equal to the step at which cluster L is last updated plus M.
		//
		icoun = 0;
		istep = 0;

		for (;;) {
			for (int i = 0; i < m; i++) {
				icoun = icoun + 1;
				istep = istep + 1;
				l1 = ic1[i];
				l2 = ic2[i];
				//
				// If point I is the only member of cluster L1, no transfer.
				//
				if (1 < nc[l1 - 1]) {
					//
					// If NCP(L1) < ISTEP, no need to re-compute distance from
					// point I to
					// cluster L1. Note that if cluster L1 is last updated
					// exactly M
					// steps ago, we still need to compute the distance from
					// point I to
					// cluster L1.
					//
					if (istep <= ncp[l1 - 1]) {
						da = 0.0;
						for (int j = 0; j < n; j++) {
							db = a[i + j * m] - c[l1 - 1 + j * k];
							da = da + db * db;
						}
						d[i] = da * an1[l1 - 1];
					}
					//
					// If NCP(L1) <= ISTEP and NCP(L2) <= ISTEP, there will be
					// no transfer of
					// point I at this step.
					//
					if (istep < ncp[l1 - 1] || istep < ncp[l2 - 1]) {
						r2 = d[i] / an2[l2 - 1];

						dd = 0.0;
						for (int j = 1; j <= n; j++) {
							de = a[i + (j - 1) * m] - c[l2 - 1 + (j - 1) * k];
							dd = dd + de * de;
						}
						//
						// Update cluster centers, NCP, NC, ITRAN, AN1 and AN2
						// for clusters
						// L1 and L2. Also update IC1(I) and IC2(I). Note that
						// if any
						// updating occurs in this stage, INDX is set back to 0.
						//
						if (dd < r2) {
							icoun = 0;
							indx = 0;
							itran[l1 - 1] = 1;
							itran[l2 - 1] = 1;
							ncp[l1 - 1] = istep + m;
							ncp[l2 - 1] = istep + m;
							al1 = (double) (nc[l1 - 1]);
							alw = al1 - 1.0;
							al2 = (double) (nc[l2 - 1]);
							alt = al2 + 1.0;
							for (int j = 1; j <= n; j++) {
								c[l1 - 1 + (j - 1) * k] = (c[l1 - 1 + (j - 1)
										* k]
										* al1 - a[i + (j - 1) * m])
										/ alw;
								c[l2 - 1 + (j - 1) * k] = (c[l2 - 1 + (j - 1)
										* k]
										* al2 + a[i + (j - 1) * m])
										/ alt;
							}
							nc[l1 - 1] = nc[l1 - 1] - 1;// weights[i].intValue();//-
														// 1;
							nc[l2 - 1] = nc[l2 - 1] + 1;// weights[i].intValue();//+
														// 1;
							an2[l1 - 1] = alw / al1;
							if (1.0 < alw) {
								an1[l1 - 1] = alw / (alw - 1.0);
							} else {
								an1[l1 - 1] = Double.MAX_VALUE;
							}
							an1[l2 - 1] = alt / al2;
							an2[l2 - 1] = alt / (alt + 1.0);
							ic1[i] = l2;
							ic2[i] = l1;
						}
					}
				}
				//
				// If no re-allocation took place in the last M steps, return.
				//
				if (icoun == m) {
					return indx;
				}
			}
		}
	}

	

	
	int iterations = 10;
	@Override
	public List<float[]> getCentroids() {
		if (cents != null)
			return cents;
		//find minimum of kmeans iterations
		double mint_wcss = Double.MAX_VALUE;
		
		int origm = m;
		double[] cmin = new double[k * n];
		for(int i =0;i<iterations;)
		{
			
			this.m = origm;

			Collections.shuffle(data);
			int err = kmns();
			
			double t_wcss = 0.0;
			if(err==0){
				for(int j =0;j<this.k;j++)
				{
					t_wcss += wss[j];
				}
				if(t_wcss<mint_wcss){
					System.arraycopy(this.c, 0, cmin, 0, cmin.length);
					mint_wcss = t_wcss;
				}
				i++;
			}else{
				System.out.println("error occured: "+ err);
			}
			// remove duplicated weight objects
//			this.data = this.data.subList(0, m);
		}
		
		cents = new ArrayList<>();
		for (int i = 0; i < k; i++) {
			float[] nv = new float[n];
			for (int j = 0; j < n; j++) {
				nv[j] = (float) cmin[i + (j) * k];
			}
			cents.add(nv);
		}

		return cents;
	}

	@Override
	public RPHashObject getParam() {
		return null;
	}

	@Override
	public void setWeights(List<Float> weights) {
//		this.weights = weights.toArray(new Float[0]);
	}

	@Override
	public void setData(List<float[]> data) {
		this.data = data;
		
		this.n = data.get(0).length;
		this.m = data.size();
		this.a = new double[m * n];
		this.c = new double[k * n];
		this.nc = new int[k];
		this.wss = new double[k];
		// weights = new Float[m];
		// Collections.shuffle(data);

	}

	@Override
	public void setK(int k) {
		this.c = new double[k * n];
		this.k = k;		this.nc = new int[k];
		this.wss = new double[k];
	}
	
	public static void main(String[] args) {
		int k = 3;
		int n = 5000;
		int d = 10;
		GenerateStreamData gen1 = new GenerateStreamData(k, d, 1f,
				System.currentTimeMillis());

		ArrayList<float[]> data = new ArrayList<>();
		for (int i = 0; i < n; i++) {
			data.add(gen1.generateNext());
		}

		for (float[] vec : gen1.medoids) {
			for (float f : vec)
				System.out.printf("%.3f,", f);
			System.out.printf("\n");
		}

		double min = Double.MAX_VALUE;
		
		for (int i = 0; i < 1000; i++) {
			Collections.shuffle(data);
			HartiganWongKMeans clu = runtests(data, k);
			double nc_sum = 0;
			double wss_sum = 0.0;

			for (int j = 0; j < k; j++) {
				nc_sum = nc_sum + clu.nc[j];
				wss_sum = wss_sum + clu.wss[j];
			}
			double tmp = wss_sum;

			if (tmp < min) {
				System.out.println(i + "\t" + tmp + "\t" + min + "\t");
				min = tmp;
				for (int j = 0; j < k; j++) {
					float[] ff = clu.getCentroids().get(j);
					for (float f : ff)
						System.out.printf("%.3f,", f);
					System.out.printf(" @%.5f \n", clu.wss[j]);
				}
			}
		}
	}

	public static HartiganWongKMeans runtests(ArrayList<float[]> data, int k) {

		HartiganWongKMeans clu = new HartiganWongKMeans(k, data);
		ArrayList<Float> weights = new ArrayList<Float>();
		for (int i = 0; i < data.size(); i++)
			weights.add((float) 1);
		clu.setWeights(weights);
		if (clu.kmns() != 0)
			System.out.println("breaks sometimes!");



		return clu;

	}

}
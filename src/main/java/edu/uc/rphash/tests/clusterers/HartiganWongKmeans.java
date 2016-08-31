package edu.uc.rphash.tests.clusterers;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.uc.rphash.Clusterer;
import edu.uc.rphash.Readers.RPHashObject;
import edu.uc.rphash.tests.generators.GenerateData;
import edu.uc.rphash.tests.generators.GenerateStreamData;
import edu.uc.rphash.util.VectorUtil;

/**
 * This code is distributed under the GNU LGPL license. Author: Original
 * FORTRAN77 version by John Hartigan, Manchek Wong. C++ version by John
 * Burkardt.
 *
 * @author Java version by Lee Carraher Reference: John Hartigan, Manchek Wong,
 *         Algorithm AS 136: A K-Means Clustering Algorithm, Applied Statistics,
 *         Volume 28, Number 1, 1979, pages 100-108.
 */
public class HartiganWongKmeans implements Clusterer

{
	
	HartiganWongKmeans(){}
	
	/**
	 * Perform Hartigan and Wong Kmeans clustering
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
	 * @param nc
	 *            int[k] the number of points in each cluster.
	 * @param iter the maximum number of iterations allowed
	 * @param wss double[k], the within-cluster sum of squares of each cluster.
	 * Errors:
	 *  0: no error was detected.
  	 *  1: at least one cluster is empty after the initial assignment.  A better set of initial clusters is needed
  	 *  2: the allowed maximum number off iterations was exceeded.
  	 *  3: K is less than or equal to 1, or greater than or equal to M.
	 */
	void kmns(double a[], int m, int n, double c[], int k, int ic1[], int nc[],
			int iter, double wss[]) {
		double aa;
		double[] an1;
		double[] an2;
		double[] d;
		double da;
		double db;
		double dc;
		double[] dt = new double[2];
		int i;
		int ic2[];
		int ii;
		int ij;
		int il;
		Integer indx;
		int itran[];
		int j;
		int l;
		int live[];
		int ncp[];
		double temp;

		if (k <= 1 || m <= k) {
			return;
		}
		ic2 = new int[m];
		an1 = new double[k];
		an2 = new double[k];
		ncp = new int[k];
		d = new double[m];
		itran = new int[k];
		live = new int[k];
		//
		// For each point I, find its two closest centers, IC1(I) and
		// IC2(I). Assign the point to IC1(I).
		//
		for (i = 1; i <= m; i++) {
			ic1[i - 1] = 1;
			ic2[i - 1] = 2;

			for (il = 1; il <= 2; il++) {
				dt[il - 1] = 0.0;
				for (j = 1; j <= n; j++) {
					da = a[i - 1 + (j - 1) * m] - c[il - 1 + (j - 1) * k];
					dt[il - 1] = dt[il - 1] + da * da;
				}
			}

			if (dt[1] < dt[0]) {
				ic1[i - 1] = 2;
				ic2[i - 1] = 1;
				temp = dt[0];
				dt[0] = dt[1];
				dt[1] = temp;
			}

			for (l = 3; l <= k; l++) {
				db = 0.0;
				for (j = 1; j <= n; j++) {
					dc = a[i - 1 + (j - 1) * m] - c[l - 1 + (j - 1) * k];
					db = db + dc * dc;
				}

				if (db < dt[1]) {
					if (dt[0] <= db) {
						dt[1] = db;
						ic2[i - 1] = l;
					} else {
						dt[1] = dt[0];
						ic2[i - 1] = ic1[i - 1];
						dt[0] = db;
						ic1[i - 1] = l;
					}
				}
			}
		}
		//
		// Update cluster centers to be the average of points contained within
		// them.
		//
		for (l = 1; l <= k; l++) {
			nc[l - 1] = 0;
			for (j = 1; j <= n; j++) {
				c[l - 1 + (j - 1) * k] = 0.0;
			}
		}

		for (i = 1; i <= m; i++) {
			l = ic1[i - 1];
			nc[l - 1] = nc[l - 1] + 1;
			for (j = 1; j <= n; j++) {
				c[l - 1 + (j - 1) * k] = c[l - 1 + (j - 1) * k]
						+ a[i - 1 + (j - 1) * m];
			}
		}
		//
		// Check to see if there is any empty cluster at this stage.
		//

		for (l = 1; l <= k; l++) {
			if (nc[l - 1] == 0) {

				return;
			}

		}

		for (l = 1; l <= k; l++) {
			aa = (double) (nc[l - 1]);

			for (j = 1; j <= n; j++) {
				c[l - 1 + (j - 1) * k] = c[l - 1 + (j - 1) * k] / aa;
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
			an2[l - 1] = aa / (aa + 1.0);

			if (1.0 < aa) {
				an1[l - 1] = aa / (aa - 1.0);
			} else {
				an1[l - 1] = Double.MAX_VALUE;
			}
			itran[l - 1] = 1;
			ncp[l - 1] = -1;
		}

		indx = 0;

		for (ij = 1; ij <= iter; ij++) {
			//
			// In this stage, there is only one pass through the data. Each
			// point is re-allocated, if necessary, to the cluster that will
			// induce the maximum reduction in within-cluster sum of squares.
			//
			optra(a, m, n, c, k, ic1, ic2, nc, an1, an2, ncp, d, itran, live,
					indx);
			//
			// Stop if no transfer took place in the last M optimal transfer
			// steps.
			//
			if (indx == m) {

				break;
			}
			//
			// Each point is tested in turn to see if it should be re-allocated
			// to the cluster to which it is most likely to be transferred,
			// IC2(I), from its present cluster, IC1(I). Loop through the
			// data until no further change is to take place.
			//
			qtran(a, m, n, c, k, ic1, ic2, nc, an1, an2, ncp, d, itran, indx);
			//
			// If there are only two clusters, there is no need to re-enter the
			// optimal transfer stage.
			//
			if (k == 2) {
				break;
			}
			//
			// NCP has to be set to 0 before entering OPTRA.
			//
			for (l = 1; l <= k; l++) {
				ncp[l - 1] = 0;
			}

		}
		//
		// If the maximum number of iterations was taken without convergence,
		// IFAULT is 2 now. This may indicate unforeseen looping.
		//
		// if ( ifault == 2 )
		// {
		// System.out.println(
		// "KMNS - Warning!\n  Maximum number of iterations reached\n  without convergence");
		// }
		//
		// Compute the within-cluster sum of squares for each cluster.
		//
		for (l = 1; l <= k; l++) {
			wss[l - 1] = 0.0;
			for (j = 1; j <= n; j++) {
				c[l - 1 + (j - 1) * k] = 0.0;
			}
		}

		for (i = 1; i <= m; i++) {
			ii = ic1[i - 1];
			for (j = 1; j <= n; j++) {
				c[ii - 1 + (j - 1) * k] = c[ii - 1 + (j - 1) * k]
						+ a[i - 1 + (j - 1) * m];
			}
		}

		for (j = 1; j <= n; j++) {
			for (l = 1; l <= k; l++) {
				c[l - 1 + (j - 1) * k] = c[l - 1 + (j - 1) * k]
						/ (double) (nc[l - 1]);
			}
			for (i = 1; i <= m; i++) {
				ii = ic1[i - 1];
				da = a[i - 1 + (j - 1) * m] - c[ii - 1 + (j - 1) * k];
				wss[ii - 1] = wss[ii - 1] + da * da;
			}
		}

		return;
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
	int optra(double a[], int m, int n, double c[], int k, int ic1[],
			int ic2[], int nc[], double an1[], double an2[], int ncp[],
			double d[], int itran[], int live[], int indx)
	{
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
		int i;
		int j;
		int l;
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
		for (l = 1; l <= k; l++) {
			if (itran[l - 1] == 1) {
				live[l - 1] = m + 1;
			}
		}

		for (i = 1; i <= m; i++) {
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
					for (j = 1; j <= n; j++) {
						df = a[i - 1 + (j - 1) * m] - c[l1 - 1 + (j - 1) * k];
						de = de + df * df;
					}
					d[i - 1] = de * an1[l1 - 1];
				}
				//
				// Find the cluster with minimum R2.
				//
				da = 0.0;
				for (j = 1; j <= n; j++) {
					db = a[i - 1 + (j - 1) * m] - c[l2 - 1 + (j - 1) * k];
					da = da + db * db;
				}
				r2 = da * an2[l2 - 1];

				for (l = 1; l <= k; l++) {
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
						for (j = 1; j <= n; j++) {
							dd = a[i - 1 + (j - 1) * m]
									- c[l - 1 + (j - 1) * k];
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
					for (j = 1; j <= n; j++) {
						c[l1 - 1 + (j - 1) * k] = (c[l1 - 1 + (j - 1) * k]
								* al1 - a[i - 1 + (j - 1) * m])
								/ alw;
						c[l2 - 1 + (j - 1) * k] = (c[l2 - 1 + (j - 1) * k]
								* al2 + a[i - 1 + (j - 1) * m])
								/ alt;
					}
					nc[l1 - 1] = nc[l1 - 1] - 1;
					nc[l2 - 1] = nc[l2 - 1] + 1;
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
		for (l = 1; l <= k; l++) {
			itran[l - 1] = 0;
			live[l - 1] = live[l - 1] - m;
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
	 * @returns indx the number of steps since a transfer took place.
	 */
	int qtran(double a[], int m, int n, double c[], int k, int ic1[],
			int ic2[], int nc[], double an1[], double an2[], int ncp[],
			double d[], int itran[], int indx) {
		double al1;
		double al2;
		double alt;
		double alw;
		double da;
		double db;
		double dd;
		double de;
		int i;
		int icoun;
		int istep;
		int j;
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
			for (i = 1; i <= m; i++) {
				icoun = icoun + 1;
				istep = istep + 1;
				l1 = ic1[i - 1];
				l2 = ic2[i - 1];
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
						for (j = 1; j <= n; j++) {
							db = a[i - 1 + (j - 1) * m]
									- c[l1 - 1 + (j - 1) * k];
							da = da + db * db;
						}
						d[i - 1] = da * an1[l1 - 1];
					}
					//
					// If NCP(L1) <= ISTEP and NCP(L2) <= ISTEP, there will be
					// no transfer of
					// point I at this step.
					//
					if (istep < ncp[l1 - 1] || istep < ncp[l2 - 1]) {
						r2 = d[i - 1] / an2[l2 - 1];

						dd = 0.0;
						for (j = 1; j <= n; j++) {
							de = a[i - 1 + (j - 1) * m]
									- c[l2 - 1 + (j - 1) * k];
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
							for (j = 1; j <= n; j++) {
								c[l1 - 1 + (j - 1) * k] = (c[l1 - 1 + (j - 1)
										* k]
										* al1 - a[i - 1 + (j - 1) * m])
										/ alw;
								c[l2 - 1 + (j - 1) * k] = (c[l2 - 1 + (j - 1)
										* k]
										* al2 + a[i - 1 + (j - 1) * m])
										/ alt;
							}
							nc[l1 - 1] = nc[l1 - 1] - 1;
							nc[l2 - 1] = nc[l2 - 1] + 1;
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

	@Override
	public List<float[]> getCentroids() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public RPHashObject getParam() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setWeights(List<Float> counts) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setData(List<float[]> centroids) {
		// TODO Auto-generated method stub

	}

	@Override
	public void setK(int getk) {
		// TODO Auto-generated method stub

	}

	public static void main(String[] args){

		double[] a;
		double[] c;
		int[] ic1;

		int k = 4;

		int n = 10000;
		int d = 4;
		GenerateData gen1 = new GenerateData(k,n,d);
		n = n*k;
		
		List<float[]> data = gen1.data();
		for(float[] vec : gen1.getMedoids()){
			for(float f : vec)
				System.out.printf("%.3f,",f);
			System.out.printf("\n");
		}
		System.out.printf("--------------------------\n");


		
		int[] nc;
		int nc_sum;
		double[] wss;
		double wss_sum;

		a = new double[n * d];
		c = new double[k * d];
		ic1 = new int[n];
		nc = new int[k];
		wss = new double[k];

//		System.out
//				.println("TEST01\n  Test the KMNS algorithm,\n Applied Statistics Algorithm #136.\n");

		double[][] amat = new double[n][d];
		for (int i = 0; i<data.size();i++) {
			for (int j = 0; j < d; j++) {
				amat[i][j] = data.get(i)[j];
			}
		}
		
		for (int i = 0; i<n;i++) {
			for (int j = 0; j < d; j++) {
				a[i  + (j) * n] = amat[i][j];
			}
		}
		

		//
		// Initialize the cluster centers.
		// Here, we arbitrarily make the first K data points cluster centers.
		//
		for (int i = 1; i <= k; i++) {
			for (int j = 1; j <= d; j++) {
				c[i - 1 + (j - 1) * k] = a[i - 1 + (j - 1) * n];
			}
		}

		int iter = 100;
		//
		// Compute the clusters.
		//
		new HartiganWongKmeans().kmns(a, n, d, c, k, ic1, nc, iter, wss);

		
		for (int i = 1; i <= k; i++) {
			for (int j = 1; j <= d; j++)
				System.out.printf("%.3f,",c[i - 1 + (j - 1) * k]);
			System.out.printf("\n");
		}

		
		
//		System.out.println("Cluster  Population  Energy");
//
//		nc_sum = 0;
//		wss_sum = 0.0;
//
//		for (int i = 1; i <= k; i++) {
//			System.out.println(i + "  " + nc[i - 1] + "  " + wss[i - 1]);
//			nc_sum = nc_sum + nc[i - 1];
//			wss_sum = wss_sum + wss[i - 1];
//		}
//
//		System.out.println("Total" + nc_sum + "  " + wss_sum);

	}

}
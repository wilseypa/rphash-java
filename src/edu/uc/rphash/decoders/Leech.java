package edu.uc.rphash.decoders;

import java.util.HashSet;
import java.util.Random;

import edu.uc.rphash.tests.GenerateData;
import edu.uc.rphash.tests.TestUtil;

/*Author: Lee Carraher
 #Institution: University of Cincinnati, Computer Science Dept.


 # this is a nearest lattice point decoder based on the hexacode based decoder of
 #Amrani, Be'ery IEEE Trans. on Comm. '96, with initial construction
 #from  Amrani, Be'ery,Vardy, Sun,Tilborg IEEE Info Thry'94

 # the goal is to rewrite this algorithm in efficient C for cuda
 # and eventual use as a Hashing Function
 # for use in a Cuda Parallel Locality Hash Based Clustering algorithm
 # additional implementation may include MPI/Cuda, and
 #anonymous offline data clustering


 #-------------QAM Stuff ----------------------
 # use a curtailed QAM for all positive signals
 #  7 A000 B000 A110 B110
 #  5 B101 A010 B010 A101
 #  3 A111 B111 A001 B001
 #  1 B011 A100 B100 A011
 #  0   1         3       5      7
 # still gets rotated    \ 4 /
 #                               1 \/ 3
 #                         /\
 #                           / 2 \

 Bs           100
 55    55    51  55   77   37   77   77    33  77   33   73
 010 010 001 010 011 000 011 011 111 011 111 100
 7.0,3.0,   3.0,3.0,   7.0,7.0   ,   3.0,3.0,   7.0,7.0,    7.0,7.0,    3.0,7.0,   7.0,7.0,    5.0,5.0   ,    5.0,1.0,   5.0,5.0,   5.0,5.0



 # leech decoder uses a rotated Z2 lattice, so to find leading cosets
 # just find the nearest point in 64QAM, A,B ; odd, even| to the rotated
 # input vector
 # rotate using the standard 2d rotation transform
 #                      [cos x -sin x ]
 #                  R = [sin x  cos x ]    cos(pi/4) = sin(pi/4)=1/sqrt(2)
 # for faster C implementation use these binary fp constants
 # 1/sqrt(2) = cc3b7f669ea0e63f ieee fp little endian
 #           = 3fe6a09e667f3bcc ieee fp big endian
 #           = 0.7071067811865475244008
 #
 #v' = v * R
 # integer lattice
 #
 #  4 A000 B000 A110 B110 | A000 B000 A110 B110
 #  3 B101 A010 B010 A101 | B101 A010 B010 A101
 #  2 A111 B111 A001 B001 | A111 B111 A001 B001
 #  1 B011 A100 B100 A011 | B011 A100 B100 A011
 #    --------------------|---------------------
 # -1 A000 B000 A110 B110 | A000 B000 A110 B110
 # -2 B101 A010 B010 A101 | B101 A010 B010 A101
 # -3 A111 B111v A001 B001 | A111 B111 A001 B001
 # -4 B011 A100 B100 A011 | B011 A100 B100 A011
 #even pts {000,110,111,001}
 #odd  pts {010,101,100,011}
 */

/*
 * this thing converges really quickly this is more than enough for fp

 inline float quicksqrt(float b)
 {
 float x = 1.1;
 unsigned char i =0;

 for(;i<16;i++){
 x = (x+(b/x))/2.0;
 }

 return x;
 }*/

public class Leech implements Decoder {

	public static int Dim = 24;
	float radius;
	float distance = -1f;
	/*
	 * an integer symbol encoding of an H6 encoder. 0 1 2 3 = 0 1 w w' indexes
	 * of the array result in the H6 encoding of a 3 integer symbol character
	 * equivalent word. eg: H6CodeWords[0][1][2] = [0,3,2]resulting in the
	 * codeword : 0 1 w 0 w' w
	 */
	static char[][][][] H6CodeWords = { {// 0 0 1 w -w
			{ { 0, 0, 0 }, { 1, 1, 1 }, { 2, 2, 2 }, { 3, 3, 3 } },// 0
					{ { 1, 2, 3 }, { 0, 3, 2 }, { 3, 0, 1 }, { 2, 1, 0 } },// 1
					{ { 2, 3, 1 }, { 3, 2, 0 }, { 0, 1, 3 }, { 1, 0, 2 } },// w
					{ { 3, 1, 2 }, { 2, 0, 3 }, { 1, 3, 0 }, { 0, 2, 1 } } },// -w
			{// 1 0 1 w -w
			{ { 1, 3, 2 }, { 0, 2, 3 }, { 3, 1, 0 }, { 2, 0, 1 } },// 0
					{ { 0, 1, 1 }, { 1, 0, 0 }, { 2, 3, 3 }, { 3, 2, 2 } },// 1
					{ { 3, 0, 3 }, { 2, 1, 2 }, { 1, 2, 1 }, { 0, 3, 0 } },// w
					{ { 2, 2, 0 }, { 3, 3, 1 }, { 0, 0, 2 }, { 1, 1, 3 } } // -w
			}, {// w 0 1 w -w
			{ { 2, 1, 3 }, { 3, 0, 2 }, { 0, 3, 1 }, { 1, 2, 0 } },// 0
					{ { 3, 3, 0 }, { 2, 2, 1 }, { 1, 1, 2 }, { 0, 0, 3 } },// 1
					{ { 0, 2, 2 }, { 1, 3, 3 }, { 2, 0, 0 }, { 3, 1, 1 } },// w
					{ { 1, 0, 1 }, { 0, 1, 0 }, { 3, 2, 3 }, { 2, 3, 2 } } // -w
			}, {// -w 0 1 w -w
			{ { 3, 2, 1 }, { 2, 3, 0 }, { 1, 0, 3 }, { 0, 1, 2 } },// 0
					{ { 2, 0, 2 }, { 3, 1, 3 }, { 0, 2, 0 }, { 1, 3, 1 } },// 1
					{ { 1, 1, 0 }, { 0, 0, 1 }, { 3, 3, 2 }, { 2, 2, 3 } },// w
					{ { 0, 3, 3 }, { 1, 2, 2 }, { 2, 1, 1 }, { 3, 0, 0 } } // -w
			} };

	static char[][][][] H6CodeWordsRev = { {
			// #w 0 1 w -w
			{ { 0, 0, 0 }, { 3, 2, 1 }, { 1, 3, 2 }, { 2, 1, 3 } }, // 0
			{ { 2, 3, 1 }, { 1, 1, 0 }, { 3, 0, 3 }, { 0, 2, 2 } },// 1
			{ { 3, 1, 2 }, { 0, 3, 3 }, { 2, 2, 0 }, { 1, 0, 1 } }, // w
			{ { 1, 2, 3 }, { 2, 0, 2 }, { 0, 1, 1 }, { 3, 3, 0 } } // -w
			}, {// w 0 1 w -w
			{ { 1, 1, 1 }, { 2, 3, 0 }, { 0, 2, 3 }, { 3, 0, 2 } },// 0
					{ { 3, 2, 0 }, { 0, 0, 1 }, { 2, 1, 2 }, { 1, 3, 3 } },// 1
					{ { 2, 0, 3 }, { 1, 2, 2 }, { 3, 3, 1 }, { 0, 1, 0 } },// w
					{ { 0, 3, 2 }, { 3, 1, 3 }, { 1, 0, 0 }, { 2, 2, 1 } } // -w
			}, {// w 0 1 w -w
			{ { 2, 2, 2 }, { 1, 0, 3 }, { 3, 1, 0 }, { 0, 3, 1 } },// 0
					{ { 0, 1, 3 }, { 3, 3, 2 }, { 1, 2, 1 }, { 2, 0, 0 } },// 1
					{ { 1, 3, 0 }, { 2, 1, 1 }, { 0, 0, 2 }, { 3, 2, 3 } },// w
					{ { 3, 0, 1 }, { 0, 2, 0 }, { 2, 3, 3 }, { 1, 1, 2 } } // -w
			}, {// -w 0 1 w -w
			{ { 3, 3, 3 }, { 0, 1, 2 }, { 2, 0, 1 }, { 1, 2, 0 } },// 0
					{ { 1, 0, 2 }, { 2, 2, 3 }, { 0, 3, 0 }, { 3, 1, 1 } },// 1
					{ { 0, 2, 1 }, { 3, 0, 0 }, { 1, 1, 3 }, { 2, 3, 2 } },// w
					{ { 2, 1, 0 }, { 1, 3, 1 }, { 3, 2, 2 }, { 0, 0, 3 } } // -w
			} };

	/*
	 * #define APT 1 #define BPT 3 #define CPT 5 #define DPT 7
	 */

	// shaping -.75, -.25,+.25,+.75
	// the unit scaled points of 16QAM centered at the origin.
	// along with their golay code + parity bit representations
	// 000, 110 , 001, 111
	float[][] evenAPts;
	// 010 100 011 101
	float[][] oddAPts;
	// 000, 110 , 001, 111
	float[][] evenBPts;
	// 010 100 011 101
	float[][] oddBPts;
	// generated by the inverse cumulative density of the normal function
	// remap based on (x*sigma)+mu
	// float APT = -1.1503493805157545f;//-.75f;
	// float BPT = -0.3186393636454069f;//-.25f;
	// float CPT = 0.3186393636454069f;//.25f;
	// float DPT = 1.15034938051575455f;//.75f;
	float APT = -.75f;
	float BPT = -.25f;
	float CPT = .25f;
	float DPT = .75f;

	public Leech() {
		this.scaler = 1.0f;
		float[][] evenAPts = { { APT, DPT }, { CPT, DPT }, { CPT, BPT },
				{ APT, BPT } };
		float[][] oddAPts = { { BPT, CPT }, { BPT, APT }, { DPT, APT },
				{ DPT, CPT } };
		float[][] evenBPts = { { BPT, DPT }, { DPT, DPT }, { DPT, BPT },
				{ BPT, BPT } };
		float[][] oddBPts = { { CPT, CPT }, { CPT, APT }, { APT, APT },
				{ APT, CPT } };
		this.evenAPts = evenAPts;
		this.oddAPts = oddAPts;
		this.evenBPts = evenBPts;
		this.oddBPts = oddBPts;
		radius = DPT + CPT;

	}

	private void RotateLattice90() {
		// col1
		float r1 = 0.70710678f;
		float r2 = 0.70710678f;
		// col2
		float r3 = -0.70710678f;
		float r4 = 0.70710678f;

		for (int i = 0; i < 4; i++) {
			float tmp = r1 * evenAPts[i][0] + r2 * evenAPts[i][1];
			evenAPts[i][1] = r3 * evenAPts[i][0] + r4 * evenAPts[i][1];
			evenAPts[i][0] = tmp;
		}
		for (int i = 0; i < 4; i++) {
			float tmp = r1 * oddAPts[i][0] + r2 * oddAPts[i][1];
			oddAPts[i][1] = r3 * oddAPts[i][0] + r4 * oddAPts[i][1];
			oddAPts[i][0] = tmp;
		}
		for (int i = 0; i < 4; i++) {
			float tmp = r1 * evenBPts[i][0] + r2 * evenBPts[i][1];
			evenBPts[i][1] = r3 * evenBPts[i][0] + r4 * evenBPts[i][1];
			evenBPts[i][0] = tmp;
		}
		for (int i = 0; i < 4; i++) {
			float tmp = r1 * oddBPts[i][0] + r2 * oddBPts[i][1];
			oddBPts[i][1] = r3 * oddBPts[i][0] + r4 * oddBPts[i][1];
			oddBPts[i][0] = tmp;
		}

	}

	public float scaler;

	public Leech(float scaler) {
		this.scaler = scaler;

		APT = (float) (this.APT * scaler);
		BPT = (float) (this.BPT * scaler);
		CPT = (float) (this.CPT * scaler);
		DPT = (float) (this.DPT * scaler);
		float[][] evenAPts = { { APT, DPT }, { CPT, DPT }, { CPT, BPT },
				{ APT, BPT } };
		float[][] oddAPts = { { BPT, CPT }, { BPT, APT }, { DPT, APT },
				{ DPT, CPT } };
		float[][] evenBPts = { { BPT, DPT }, { DPT, DPT }, { DPT, BPT },
				{ BPT, BPT } };
		float[][] oddBPts = { { CPT, CPT }, { CPT, APT }, { APT, APT },
				{ APT, CPT } };
		this.evenAPts = evenAPts;
		this.oddAPts = oddAPts;
		this.evenBPts = evenBPts;
		this.oddBPts = oddBPts;
		radius = DPT + CPT;
	}
	
	public void setScaler(float scaler){
		this.scaler = scaler;
		APT = (float) (this.APT * scaler);
		BPT = (float) (this.BPT * scaler);
		CPT = (float) (this.CPT * scaler);
		DPT = (float) (this.DPT * scaler);
		float[][] evenAPts = { { APT, DPT }, { CPT, DPT }, { CPT, BPT },
				{ APT, BPT } };
		float[][] oddAPts = { { BPT, CPT }, { BPT, APT }, { DPT, APT },
				{ DPT, CPT } };
		float[][] evenBPts = { { BPT, DPT }, { DPT, DPT }, { DPT, BPT },
				{ BPT, BPT } };
		float[][] oddBPts = { { CPT, CPT }, { CPT, APT }, { APT, APT },
				{ APT, CPT } };
		this.evenAPts = evenAPts;
		this.oddAPts = oddAPts;
		this.evenBPts = evenBPts;
		this.oddBPts = oddBPts;
		radius = DPT + CPT;
		
	}

	/*
	 * WARNING: not true euclidean distance compute the distance between two 24
	 * dimensional vectors. The square-root is omitted because the algorithm
	 * only needs to know which is closer d(cp, pt.) or d(cp',pt) , for which
	 * sqrt(d(cp, pt.)) and sqrt(d(cp', pt.)) inequality holds for positive
	 * distances(this is why we keep the squares).
	 */
	// #define golay
	float distance(float pt[], float cp[], int startat) {
		return (cp[0] - pt[startat]) * (cp[0] - pt[startat])
				+ (cp[1] - pt[startat + 1]) * (cp[1] - pt[startat + 1]);
	}

	// float[] convertToCoords(long c)
	// {
	//
	// float[] point = new float[24];
	// float axCoords[] = {APT,CPT, BPT,DPT,BPT,DPT,CPT,APT };
	// float ayCoords[] = {DPT,BPT,CPT,APT,APT,CPT,DPT,BPT};
	// float bxCoords[] = {BPT,DPT,CPT,APT,CPT,APT,DPT,BPT};
	// float byCoords[] = {DPT,BPT, CPT,APT,APT,CPT,DPT,BPT};
	//
	// int parity = (int)(c&0xfff);//seperate these parts
	//
	// //compute A/B point from parity
	// int u = parity;
	// int Bpoint = 0;
	// while(u>0)
	// {
	// if((u &1)== 1)Bpoint++;
	// u = (u>>>1);
	// }
	//
	// //this may verywell break this function
	// //c=(c&0xffffff000)>>12;
	//
	// int i;
	// int pt = 0;
	// if((Bpoint &1)==0)
	// {
	// for(i=0;i<12;i++){
	// pt = (int) (((c&1)<<2)+(c&2)+(parity&1));
	// point[i*2]= bxCoords[pt];
	// point[i*2+1]=byCoords[pt];
	// c = c>>>2;
	// parity = parity>>>1;
	// }
	// }
	// else{
	// for(i=0;i<12;i++){
	// pt = (int) (((c&1)<<2)+(c&2)+(parity&1)) ;
	// point[i*2]= axCoords[pt];
	// point[i*2+1]=ayCoords[pt];
	// c = c>>>2;
	// parity = parity>>>1;
	// }
	// }
	// return point;
	// }

	/*
	 * this function returns all of the pertinent information from the decoder
	 * such as minimum distances, nearest coset leader quadrant, and alternative
	 * k-parity distances
	 * 
	 * these maps are separated into the quadrants of a Cartesian plane now we
	 * gotta order these properly
	 * 
	 * another simple fix is that the quadrants of QAM be abstractly defined,
	 * and the -,+ of order pairs be used to tile the generalized 16bit qam,
	 * besides this has to be done anyway so we can get out the real number
	 * coordinates in the end
	 */
	int QAM(float[] r, float[][] evenPts, // [4][2],
			float[][] oddPts, // [4][2],
			float[][] dijs,// [12][4],
			float[][] dijks,// [12][4],
			char[][] kparities// [12][4])
	) {
		// void QAM(float *r, float *evenPts,float *oddPts,float *dijs,float
		// *dijks,int *kparities){
		// the closest even-type Z2 lattice point is used as the
		// coset representatives for all points, not currently used
		// quadrant = [0 for k in range(12)]

		char i = 0;
		// int ret = 0;
		for (; i < 12; i++) {

			// ret <<=2;
			// if(r[i*2] <0f) {
			// r[i*2] = r[i*2]+2f ;
			// ret+=2;
			// }
			// if(r[i*2+1]<0f) {
			// r[i*2+1]= r[i*2+1]+2f ;
			// ret+=1;
			// }

			float dist000 = distance(r, evenPts[0], i * 2);
			float dist110 = distance(r, evenPts[1], i * 2);
			float dist001 = distance(r, evenPts[2], i * 2);
			float dist111 = distance(r, evenPts[3], i * 2);

			// TODO dist111 is always least.. why
			// System.out.printf("%.3f,%.3f,%.3f,%.3f\t\t\t",dist000,dist110,dist001,dist111);
			if (dist000 < dist001) {
				dijs[i][0] = dist000;
				dijks[i][0] = dist001;
				kparities[i][0] = 0;
			} else {
				dijs[i][0] = dist001;
				dijks[i][0] = dist000;
				kparities[i][0] = 1;
			}
			if (dist110 < dist111) {
				dijs[i][3] = dist110;
				dijks[i][3] = dist111;
				kparities[i][3] = 0;
			} else {
				dijs[i][3] = dist111;
				dijks[i][3] = dist110;
				kparities[i][3] = 1;
			}
			// quadrant[i] = 0

			// min over odds
			float dist010 = distance(r, oddPts[0], i * 2);
			float dist100 = distance(r, oddPts[1], i * 2);
			float dist011 = distance(r, oddPts[2], i * 2);
			float dist101 = distance(r, oddPts[3], i * 2);
			// System.out.printf("\t\t%.3f,%.3f,%.3f,%.3f\n",dist010,dist100,dist011,dist101);

			if (dist010 < dist011) {
				dijs[i][1] = dist010;
				dijks[i][1] = dist011;
				kparities[i][1] = 0;
			} else {
				dijs[i][1] = dist011;
				dijks[i][1] = dist010;
				kparities[i][1] = 1;
			}
			if (dist100 < dist101) {
				dijs[i][2] = dist100;
				dijks[i][2] = dist101;
				kparities[i][2] = 0;
			} else {
				dijs[i][2] = dist101;
				dijks[i][2] = dist100;
				kparities[i][2] = 1;
			}
		}

		return 0;// ret;
	}

	/*
	 * computes the Z2 block confidence of the concatenated points projections
	 * onto GF4 characters
	 */
	void blockConf(float[][] dijs,// [12][4],
			float[][] muEs,// [6][4],
			float[][] muOs,// [6][4],
			char[][][] prefRepE,// [6][4][4],
			char[][][] prefRepO// [6][4][4])
	) {

		// each two symbols is taken as a single character in GF4
		char i = 0;
		for (; i < 6; i++) {

			// 0000 1111
			float s = dijs[2 * i][0] + dijs[2 * i + 1][0];
			float t = dijs[2 * i][3] + dijs[2 * i + 1][3];
			if (s < t) {
				muEs[i][0] = s;
				prefRepE[i][0][0] = 0;
				prefRepE[i][0][1] = 0;
				prefRepE[i][0][2] = 0;
				prefRepE[i][0][3] = 0;

			} else {
				muEs[i][0] = t;
				// prefRepE[i][0] = 15;//[1,1,1,1]
				prefRepE[i][0][0] = 1;
				prefRepE[i][0][1] = 1;
				prefRepE[i][0][2] = 1;
				prefRepE[i][0][3] = 1;
			}

			// 0011 1100 0 3 3 0
			s = dijs[2 * i][0] + dijs[2 * i + 1][3];
			t = dijs[2 * i][3] + dijs[2 * i + 1][0];
			if (s < t) {
				muEs[i][1] = s;
				// prefRepE[i][1] = 3;//[0,0,1,1]
				prefRepE[i][1][0] = 0;
				prefRepE[i][1][1] = 0;
				prefRepE[i][1][2] = 1;
				prefRepE[i][1][3] = 1;
			} else {
				muEs[i][1] = t;
				// prefRepE[i][1] = 12;//[1,1,0,0]
				prefRepE[i][1][0] = 1;
				prefRepE[i][1][1] = 1;
				prefRepE[i][1][2] = 0;
				prefRepE[i][1][3] = 0;
			}

			// 1010 0101
			s = dijs[2 * i][2] + dijs[2 * i + 1][2];
			t = dijs[2 * i][1] + dijs[2 * i + 1][1];
			if (s < t) {
				muEs[i][2] = s;
				// prefRepE[i][2] = 10;//[1,0,1,0]
				prefRepE[i][2][0] = 1;
				prefRepE[i][2][1] = 0;
				prefRepE[i][2][2] = 1;
				prefRepE[i][2][3] = 0;
			} else {
				muEs[i][2] = t;
				// prefRepE[i][2] = 5;//[0,1,0,1]
				prefRepE[i][2][0] = 0;
				prefRepE[i][2][1] = 1;
				prefRepE[i][2][2] = 0;
				prefRepE[i][2][3] = 1;
			}
			// 0110 1001
			s = dijs[2 * i][1] + dijs[2 * i + 1][2];
			t = dijs[2 * i][2] + dijs[2 * i + 1][1];
			if (s < t) {
				muEs[i][3] = s;
				// prefRepE[i][3] =6;// [0,1,1,0]
				prefRepE[i][3][0] = 0;
				prefRepE[i][3][1] = 1;
				prefRepE[i][3][2] = 1;
				prefRepE[i][3][3] = 0;
			} else {
				muEs[i][3] = t;
				// prefRepE[i][3] = 9;//[1,0,0,1]
				prefRepE[i][3][0] = 1;
				prefRepE[i][3][1] = 0;
				prefRepE[i][3][2] = 0;
				prefRepE[i][3][3] = 1;
			}

			// 1000 0111
			s = dijs[2 * i][2] + dijs[2 * i + 1][0];
			t = dijs[2 * i][1] + dijs[2 * i + 1][3];
			if (s < t) {
				muOs[i][0] = s;
				// prefRepO[i][0] = 8;//[1,0,0,0]
				prefRepO[i][0][0] = 1;
				prefRepO[i][0][1] = 0;
				prefRepO[i][0][2] = 0;
				prefRepO[i][0][3] = 0;
			} else {
				muOs[i][0] = t;
				// prefRepO[i][0] = 7;//[0,1,1,1]
				prefRepO[i][0][0] = 0;
				prefRepO[i][0][1] = 1;
				prefRepO[i][0][2] = 1;
				prefRepO[i][0][3] = 1;
			}
			// 0100 1011
			s = dijs[2 * i][1] + dijs[2 * i + 1][0];
			t = dijs[2 * i][2] + dijs[2 * i + 1][3];
			if (s < t) {
				muOs[i][1] = s;
				// prefRepO[i][1] = 4;//[0,1,0,0]
				prefRepO[i][1][0] = 0;
				prefRepO[i][1][1] = 1;
				prefRepO[i][1][2] = 0;
				prefRepO[i][1][3] = 0;
			} else {
				muOs[i][1] = t;
				// prefRepO[i][1] = 11;//[1,0,1,1]
				prefRepO[i][1][0] = 1;
				prefRepO[i][1][1] = 0;
				prefRepO[i][1][2] = 1;
				prefRepO[i][1][3] = 1;
			}

			// 0010 1101
			s = dijs[2 * i][0] + dijs[2 * i + 1][2];
			t = dijs[2 * i][3] + dijs[2 * i + 1][1];
			if (s < t) {
				muOs[i][2] = s;
				// prefRepO[i][2] =2;// [0,0,1,0]
				prefRepO[i][2][0] = 0;
				prefRepO[i][2][1] = 0;
				prefRepO[i][2][2] = 1;
				prefRepO[i][2][3] = 0;
			} else {
				muOs[i][2] = t;
				// prefRepO[i][2] = 13;//[1,1,0,1]
				prefRepO[i][2][0] = 1;
				prefRepO[i][2][1] = 1;
				prefRepO[i][2][2] = 0;
				prefRepO[i][2][3] = 1;
			}

			// 0001 1110
			s = dijs[2 * i][0] + dijs[2 * i + 1][1];
			t = dijs[2 * i][3] + dijs[2 * i + 1][2];
			if (s < t) {
				muOs[i][3] = s;
				// prefRepO[i][3] = 1;//[0,0,0,1]
				prefRepO[i][3][0] = 0;
				prefRepO[i][3][1] = 0;
				prefRepO[i][3][2] = 0;
				prefRepO[i][3][3] = 1;
			} else {
				muOs[i][3] = t;
				// prefRepO[i][3] = 14;//[1,1,1,0]
				prefRepO[i][3][0] = 1;
				prefRepO[i][3][1] = 1;
				prefRepO[i][3][2] = 1;
				prefRepO[i][3][3] = 0;
			}
		}
	}

	/*
	 * here we are looking for the least character in the H6 hexacode word
	 * returns the hexacode word and the wt, for using in locating the least
	 * reliable symbol
	 */
	void constructHexWord(float[][] mus,// [6][4],
			char[] chars,// [6],
			float[] charwts// [6])
	) {

		char i = 0;
		for (; i < 6; i++) {
			char leastChar = 0;
			float leastwt = mus[i][0];
			if (mus[i][1] < leastwt) {
				leastwt = mus[i][1];
				leastChar = 1;
			}
			if (mus[i][2] < leastwt) {
				leastwt = mus[i][2];
				leastChar = 2;
			}
			if (mus[i][3] < leastwt) {
				leastwt = mus[i][3];
				leastChar = 3;
			}
			chars[i] = leastChar;
			charwts[i] = leastwt;
		}

	}

	/*
	 * this is the minimization over the hexacode function using the 2nd
	 * algorithm of amrani and be'ery ieee may '96
	 */
	float minH6(char[] y,// [6],
			float[] charwts,// [6],
			float[][] mus)// [6][4])
	{

		// locate least reliable
		float leastreliablewt = charwts[0];
		char leastreliablechar = 0;

		if (charwts[1] > leastreliablewt) {
			leastreliablewt = charwts[1];
			leastreliablechar = 1;
		}
		if (charwts[2] > leastreliablewt) {
			leastreliablewt = charwts[2];
			leastreliablechar = 2;
		}

		// minimize over the 8 candidate Hexacode words
		float minCodeWt = 1000.0f;
		char j = 0;
		// unsigned char min = 0;
		float m_dist;

		char[] leastcan = { 0, 0, 0, 0, 0, 0 };
		// build candidate list
		char[] cand = { 0, 0, 0, 0, 0, 0 };

		// try combinations of substitutions for the least reliable
		char i;
		for (i = 0; i < 4; i++) {
			y[leastreliablechar] = i;
			cand[0] = y[0];
			cand[1] = y[1];
			cand[2] = y[2];
			cand[3] = H6CodeWords[y[0]][y[1]][y[2]][0];
			cand[4] = H6CodeWords[y[0]][y[1]][y[2]][1];
			cand[5] = H6CodeWords[y[0]][y[1]][y[2]][2];

			m_dist = 0.0f;
			for (j = 0; j < 6; j++)
				m_dist += mus[j][cand[j]];
			if (m_dist < minCodeWt) {
				minCodeWt = m_dist;
				for (j = 0; j < 6; j++)
					leastcan[j] = cand[j];
			}
		}

		// y2
		// locate the least reliable symbol in each
		leastreliablewt = charwts[3];
		leastreliablechar = 3;
		if (charwts[4] > leastreliablewt) {
			leastreliablewt = charwts[4];
			leastreliablechar = 4;
		}
		if (charwts[5] > leastreliablewt) {
			leastreliablewt = charwts[5];
			leastreliablechar = 5;
		}

		for (; i < 8; i++) {
			y[leastreliablechar] = (char) (i - 4);
			cand[0] = H6CodeWordsRev[y[3]][y[4]][y[5]][0];
			cand[1] = H6CodeWordsRev[y[3]][y[4]][y[5]][1];
			cand[2] = H6CodeWordsRev[y[3]][y[4]][y[5]][2];
			cand[3] = y[3];
			cand[4] = y[4];
			cand[5] = y[5];

			m_dist = 0.0f;
			for (j = 0; j < 6; j++)
				m_dist += mus[j][cand[j]];

			if (m_dist < minCodeWt) {
				minCodeWt = m_dist;
				for (j = 0; j < 6; j++)
					leastcan[j] = cand[j];
			}
		}
		for (j = 0; j < 6; j++)
			y[j] = leastcan[j];

		return minCodeWt;
	}

	/*
	 * here we are resolving the h-parity. which requires that the overall least
	 * significant bit parities equal the bit parities of each projected GF4
	 * block. aka column parity must equal 1st row parity
	 */
	float hparity(float weight, char[] hexword,// [6],
			char[][][] prefReps,// [6][4][4],
			float[][] dijs,// [12][4],
			int subSetParity, char[] codeword) {

		char parity = 0;
		char i;
		;

		for (i = 0; i < 6; i++) {
			// create the golay codeword from the hexacode representation
			codeword[i << 2] = prefReps[i][hexword[i]][0];
			codeword[(i << 2) + 1] = prefReps[i][hexword[i]][1];
			codeword[(i << 2) + 2] = prefReps[i][hexword[i]][2];
			codeword[(i << 2) + 3] = prefReps[i][hexword[i]][3];

			parity = (char) (parity ^ prefReps[i][hexword[i]][0]);// this should
																	// be the
																	// highest
																	// order bit
		}

		// System.out.println("---------------Q------------------");
		if ((parity & 1) == subSetParity) {
			return weight;
		}

		float leastwt = 100000.0f;
		char least = 0;
		float deltaX;
		char idx1, idx2;
		// walk along the codeword again
		for (i = 0; i < 6; i++) {

			idx1 = (char) ((codeword[i << 2] << 1) + codeword[(i << 2) + 1]);
			idx2 = (char) ((codeword[(i << 2) + 2] << 1) + codeword[(i << 2) + 3]);
			// compute cost of complementing the hexacode representation ^3 of
			// bits
			// select minimal cost complement.
			deltaX = (dijs[i << 1][idx1 ^ 3] + dijs[(i << 1) + 1][idx2 ^ 3])
					- (dijs[i << 1][idx1] + dijs[(i << 1) + 1][idx2]);

			if (deltaX < leastwt) {
				leastwt = deltaX;
				least = (char) (i << 2);
			}
		}

		weight = weight + leastwt;

		codeword[least] = (char) (codeword[least] ^ 1);
		codeword[least + 1] = (char) (codeword[least + 1] ^ 1);
		codeword[least + 2] = (char) (codeword[least + 2] ^ 1);
		codeword[least + 3] = (char) (codeword[least + 3] ^ 1);

		/*
		 * for(i=0;i<6;i++){ printf("%i%i%i%i ",
		 * codeword[i*4],codeword[i*4+1],codeword[i*4+2],codeword[i*4+3]); }
		 * printf(": [%i , %f]\n" , least,leastwt);
		 */

		return weight;
	}

	float kparity(float weight, char[] codeword, char ABtype,
			char[] codeParity, float[][] dijks,// [12][4],
			float[][] dijs,// [12][4],
			char[][] kparities// [12][4])
	) {
		/*
		 * this last parity check assures that all A or B points have even/odd
		 * parity
		 */
		char parity = 0;
		char i = 0;
		float least = 100000;
		float dif;
		char argLeast = 0;

		for (; i < 12; i++) {
			char n = (char) ((codeword[i << 1] << 1) + codeword[(i << 1) + 1]);
			parity = (char) ((char) parity ^ kparities[i][n]);
			codeParity[i] = kparities[i][n];
			dif = dijks[i][n] - dijs[i][n];
			if (dif <= least) {
				least = dif;
				argLeast = i;
			}
		}

		if (parity == ABtype) {
			return weight;
		}
		codeParity[argLeast] ^= 1;
		return weight + least;
	}

	long[] convertbin(char[] cw, char[] cp, byte[] quant) {
		// unsigned long leastCodeword;
		// unsigned char* leastCodeword = malloc(24*sizeof(unsigned char));
		// unsigned char leastCodeword[24];

		long[] retOpt;

			retOpt = new long[1];
			retOpt[0]=0;
		    for(int i =0;i<24;i++){
		    	retOpt[0]+=cw[i];
		    	retOpt[0]<<=1;

		    }
		    for(int i =0;i<12;i++){
		    	retOpt[0]+=cp[i];
		    	retOpt[0]<<=1;
		    }
		    if(quant!=null)//use remaining 64-36 = 8 bits for a quantization vector
		    for(int i =0;i<8;i++){
		    	retOpt[0]^=quant[i];
		    }
		    
//		retOpt[0] = (byte) (cw[0] + (cw[1] << 1) + (cw[2] << 2) + (cw[3] << 3)
//				+ (cw[4] << 4) + (cw[5] << 5) + (cw[6] << 6) + (cw[7] << 7));
//		retOpt[1] = (byte) (cw[8] + (cw[9] << 1) + (cw[10] << 2)
//				+ (cw[11] << 3) + (cw[12] << 4) + (cw[13] << 5) + (cw[14] << 6) + (cw[15] << 7));
//		retOpt[2] = (byte) (cw[16] + (cw[17] << 1) + (cw[18] << 2)
//				+ (cw[19] << 3) + (cw[20] << 4) + (cw[21] << 5) + (cw[22] << 6) + (cw[23] << 7));
//		retOpt[3] = (byte) (cp[0] + (cp[1] << 1) + (cp[2] << 2) + (cp[3] << 3)
//				+ (cp[4] << 4) + (cp[5] << 5) + (cp[6] << 6) + (cp[7] << 7));
//		retOpt[4] = (byte) (cp[8] + (cp[9] << 1) + (cp[10] << 2) + (cp[11] << 3));

		return retOpt;
	}

	@Override
	public int getDimensionality() {
		return 24;
	}

	@Override
	public float getErrorRadius() {
		return (float) Math.sqrt(2 * (DPT + CPT));
	}

	/**
	 * Generate the initial quantization vector for the lattice. ie
	 * <1,0,1,1,1,0,1,1> is a valid E8 vector as is <8,0,-8,4,1,0,1,1>, there is
	 * simply a quantization disparity. to fix this we simply solve the unscaled
	 * version equation cX = 1X
	 * 
	 * @return
	 */
	public byte[] generateQuantizationVector(float[] r) {

		// TestUtil.prettyPrint(r);
		// System.out.println();
		byte[] ret = new byte[r.length];
		// 4.0 -> 0.0 mean,
		for (int i = 0; i < r.length; i++) {
			if (r[i] > 0) {
				byte l = (byte) (r[i] / radius);
				ret[i] = (byte) ((l + 1) / 2);
				r[i] -= ret[i] * 2 * (radius);
			} else {
				byte l = (byte) (-r[i] / radius);
				ret[i] = (byte) (-(l + 1) / 2);
				r[i] -= ret[i] * 2 * (radius);
			}
		}
		// TestUtil.prettyPrint(r);
		// System.out.println();
		// for(int g = 0;g<24;g++)System.out.printf("%d,",ret[g]);
		// System.out.println();
		return ret;
	}

	// static int [] winners = new int[4];

	// -369372827648 010 010 010 010, 001 001 001 001,010 010 010 010 inner A
	// near 0 mean
	// -241-> 001 001 001 001,001 001 001 001,001 001 001 001, inner4 A near 0
	// mean
	// -1448472832 001 001 001 001,010 010 010 010 ,010 010 010 010 inner 4 A
	// near 0 mean
	// -370810028017 010 010 010 010, 010 010 010 010, 001 001 001 001 inner 4 A
	// near 0 mean
	// unsigned char* decode(float r[12][2], float *distance){
	// unsigned long long decodeLeech(float *r,float *distance)
	public long[] decode(float[] r) {
		//r = TestUtil.normalize(r);
		// #####################QAM Dijks ###################
		float[][] dijs = new float[12][4];
		float[][] dijks = new float[12][4];
		// there is a set for each quarter decoder, and the A/B_ij odd/even
		char[][] kparities = new char[12][4];

		byte[] append = generateQuantizationVector(r);
		QAM(r, evenAPts, oddAPts, dijs, dijks, kparities);

		// #####################Block Confidences ###################
		// 0 1 w W
		float[][] muEs = new float[6][4];
		float[][] muOs = new float[6][4];
		char[][][] prefRepE = new char[6][4][4];
		char[][][] prefRepO = new char[6][4][4];

		blockConf(dijs, muEs, muOs, prefRepE, prefRepO); // just run through
															// both as its
															// faster, but could
															// conserve array
															// allocation

		// #####################Construct Hexacode Word ###################
		char[] y = new char[6];

		float[] charwts = new float[6];
		constructHexWord(muEs, y, charwts);

		// #####################Minimize over the Hexacode ###################
		float weight = minH6(y, charwts, muEs);

		// ****chars = y = hexword *****
		char[] cw = new char[24];
		char[] cp = new char[12];

		// int winner = 0;
		float leastweight;
		long[] retOpt;

		weight = hparity(weight, y, prefRepE, dijs, 0, cw);// byref

		weight = kparity(weight, cw, (char) 0, cp, dijks, dijs, kparities);

		// set as least
		leastweight = weight;
		retOpt = convertbin(cw, cp, append);

		// ----------------A Odd Quarter Lattice Decoder----------------
		constructHexWord(muOs, y, charwts);
		;
		weight = minH6(y, charwts, muOs);
		weight = hparity(weight, y, prefRepO, dijs, (char) 1, cw);// byref

		weight = kparity(weight, cw, (char) 0, cp, dijks, dijs, kparities);

		if (weight < leastweight) {
			leastweight = weight;
			retOpt = convertbin(cw, cp, append);
		}

		// ----------------H_24 Half Lattice Decoder for B
		// points----------------
		QAM(r, evenBPts, oddBPts, dijs, dijks, kparities);
		blockConf(dijs, muEs, muOs, prefRepE, prefRepO);

		// ----------------B Even Quarter Lattice Decoder----------------
		constructHexWord(muEs, y, charwts);
		weight = minH6(y, charwts, muEs);
		weight = hparity(weight, y, prefRepE, dijs, 0, cw);// byref

		weight = kparity(weight, cw, (char) 1, cp, dijks, dijs, kparities);

		if (weight < leastweight) {
			leastweight = weight;
			retOpt = convertbin(cw, cp, append);
		}

		// ----------------B Odd Quarter Lattice Decoder----------------
		constructHexWord(muOs, y, charwts);
		weight = minH6(y, charwts, muOs);
		weight = hparity(weight, y, prefRepO, dijs, 1, cw);// byref
		weight = kparity(weight, cw, (char) 1, cp, dijks, dijs, kparities);

		if (weight < leastweight) {
			leastweight = weight;
			retOpt = convertbin(cw, cp, append);
		}
		this.distance = leastweight;
		return retOpt;// leastCodeword;
	}

	public static float[] cnv(double[] fff) {
		float[] ret = new float[fff.length];
		for (int i = 0; i < fff.length; i++)
			ret[i] = (float) fff[i];
		return ret;
	}

	public float[] encode(long codeword) {
		return null;
	}

	@Override
	public float getDistance() {
		return this.distance;
	}

	public static void main(String[] args) {
		Random r = new Random();
		int d = 24;
		Leech sp = new Leech();
		for (int i = 0; i < 100; i++) {
			int ct = 0;
			float distavg = 0.0f;
			for (int j = 0; j < 10000; j++) {
				float p1[] = new float[d];
				float p2[] = new float[d];
				for (int k = 0; k < d; k++) {
					p1[k] = r.nextFloat() * 2 - 1;
					p2[k] = (float) (p1[k] + r.nextGaussian()
							* ((float) i / 100f));
				}
				distavg+=TestUtil.distance(p1,p2);
				long[] hp1 = sp.decode(p1);
				long[] hp2 = sp.decode(p2);
				if(hp1[0]==hp2[0])ct++;
			}
			System.out.println(distavg / 10000f + "\t" + (float) ct / 10000f);
		}
	}
	// public static void main(String[] args)
	// {
	// Decoder leech = new LeechDecoder(2f);
	//
	// HashSet<Long> h = new HashSet<Long>();
	// int k = 0;
	// float[] f = new float[24];
	// byte[] b;
	// long t;
	// int j;
	// Random r = new Random();
	// //winners[0]=0;winners[1]=0;winners[2]=0;winners[3]=0;
	// //even decoder subset selection and 4096 (codewords of golay code) * 4096
	// (2^12 parity combinations)
	// for(long i = 0;i<10000000;i++)
	// {
	// //System.out.println("------------------------------------------------------------");
	// for(j=0;j<24;j++){
	// f[j] = r.nextFloat()*2f-1f;
	// }
	// b= leech.decode(f);
	//
	//
	// for(j=0;j<24;j++){
	// f[j] = r.nextFloat()*2f-1f;
	// }
	// t = 0;
	// for(j = 0;j<5;j++){
	// t = (t<<8)+b[j];
	//
	// }
	// h.add(t);
	// k++;
	// if(k%100000==0){
	// h.size();
	// }
	// // System.out.printf("%d:{%f,%f,%f,%f}\n",
	// // h.size(),
	// // (float)winners[0]/k,
	// // (float)winners[1]/k,
	// // (float)winners[2]/k,
	// // (float)winners[3]/k);
	// // }
	//
	// // TestUtil.prettyPrint(leech.decode(f));
	// }
	//
	// }

	@Override
	public void setVariance(Float parameterObject) {
		scaler = parameterObject;
		radius = (DPT + CPT) * scaler;
		APT = (float) (this.APT * scaler);
		BPT = (float) (this.BPT * scaler);
		CPT = (float) (this.CPT * scaler);
		DPT = (float) (this.DPT * scaler);

		float[][] evenAPts = { { APT, DPT }, { CPT, DPT }, { CPT, BPT },
				{ APT, BPT } };
		float[][] oddAPts = { { BPT, CPT }, { BPT, APT }, { DPT, APT },
				{ DPT, CPT } };
		float[][] evenBPts = { { BPT, DPT }, { DPT, DPT }, { DPT, BPT },
				{ BPT, BPT } };
		float[][] oddBPts = { { CPT, CPT }, { CPT, APT }, { APT, APT },
				{ APT, CPT } };
		this.evenAPts = evenAPts;
		this.oddAPts = oddAPts;
		this.evenBPts = evenBPts;
		this.oddBPts = oddBPts;
		
	}
}

/***********************************************************************
 	hadoop-gpu
	Authors: Koichi Shirahata, Hitoshi Sato, Satoshi Matsuoka

This software is licensed under Apache License, Version 2.0 (the  "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
-------------------------------------------------------------------------
File: cpu-kmeans2D.cc
 - Kmeans with 2D input data on CPU.
Version: 0.20.1.2
Modified for n dimension vectors
Authors: Lee Carraher
 ***********************************************************************/

#include "stdint.h"

#include "Pipes.hh"
#include "TemplateFactory.hh"
#include "StringUtils.hh"

#include <iostream>
#include <cstdlib>
#include <cmath>
#include <time.h>
#include <sys/time.h>
#include <fstream>

#define DEBUG

//using namespace std;

int deviceID = 0;

#ifndef DEBUG
class KmeansMap: public HadoopPipes::Mapper {
public:
	KmeansMap(HadoopPipes::TaskContext& context){}
#endif
	// pos : coordinate
	// cent : id of nearest cluster
	class data {
	public:
		float* v;
		int len;
		int cent;
		void print(){
			for(int i=0;i<len-1;i++)printf("%.3f, ",v[i]);
			printf("%.3f\n",v[len-1]);
		}
	};

	double gettime() {
		struct timeval tv;
		gettimeofday(&tv,NULL);
		return tv.tv_sec+tv.tv_usec * 1e-6;
	}

	//zero init
	void init_int(int *data, int num) {
		for(int i = 0; i < num; i++) {
			data[i] = 0;
		}
	}
	void init_float(float *data, int num) {
		for(int i = 0; i < num; i++) {
			data[i] = 0.0;
		}
	}

	// quick sort by d->cent
	void myqsort(data *d, int start, int end)
	{
		int i = start;
		int j = end;
		float base = (d[start].cent + d[end].cent) / 2;
		while(1) {
			while (d[i].cent < base) i++;
			while (d[j].cent > base) j--;
			if (i >= j) break;
			data temp = d[i];
			d[i] = d[j];
			d[j] = temp;
			i++;
			j--;
		}
		if (start < i-1) myqsort(d, start, i-1);
		if (end > j+1)  myqsort(d, j+1, end);
	}
	//unchecked
	float mysqrt(data a, data b) {
		//int i =0;
		float sum =0.0;
		for(int i=0;i<a.len;i++)
		{

			sum+=((a.v[i]-b.v[i])*(a.v[i]-b.v[i]));

		}

		return std::sqrt(sum);
	}

	//data object assignment
	//calculate new centroid for each data(plot)
	void assign_data(
			data *centroids, data *data, int num_of_data, int num_of_cluster) {


		for(int i = 0; i < num_of_data; i++) {


			int center = 0;
			float dmin = mysqrt(centroids[0], data[i]);


			for(int j = 1; j < num_of_cluster; j++) {
				float dist = mysqrt(centroids[j], data[i]);
				if(dist < dmin)
				{
					dmin = dist;
					center = j;
				}
			}
			data[i].cent = center;
		}
	}

	//counts the nunber of data objects contained by each cluster
	void count_data_in_cluster(
			data *d, int *ndata, int num_of_data, int num_of_cluster) {
		int i;
		for(i = 0; i < num_of_data; i++) {
			ndata[d[i].cent]++;
		}
		//this may not be needed...
		for(i = 1; i < num_of_cluster; i++) {
			ndata[i] += ndata[i-1];
		}
	}

	//K centroids recalculation
	void centroids_recalc(
			data *newcent, data *d, int *ndata, int num_of_data, int num_of_cluster) {
		int len =   d[0].len;
		int i, j,k;
		//init centroid sums
		for(i = 0; i < num_of_cluster; i++) {
			for(j = 0; j < d[i].len; j++)
				newcent[i].v[i] = 0.0;
		}

		for(j = 0; j < ndata[0]; j++) {
			for(i = 0;i<len;i++)
				newcent[0].v[i] += d[j].v[i];
			//    newcent[0].x += d[j].x;
			//    newcent[0].y+= d[j].y;
		}

		for(i = 0;i<len;i++) newcent[0].v[i]/=static_cast<float>(ndata[0]);
		//newcent[0].x /= static_cast<float>(ndata[0]);
		//newcent[0].y /= static_cast<float>(ndata[0]);



		for(i = 1; i < num_of_cluster; i++) {
			for(j = ndata[i-1]; j < ndata[i]; j++) {
				for(k=0;k<len;k++)
					newcent[i].v[k] += d[j].v[k];
				//newcent[i].x += d[j].x;
				//newcent[i].y += d[j].y;
			}
			float n = static_cast<float>(ndata[i]-ndata[i-1]);

			for(k=0;k<len;k++)newcent[i].v[k] /= n;
			//newcent[i].x /= n;
			//newcent[i].y /= n;
		}
	}
	/*
  bool datacmp(data *a, data *b, int num) {
    for(int i = 0; i < num; i++) {
      if(a[i].x != b[i].x || a[i].y != b[i].y) {
	return false;
      }
    }
    return true;
  }
	 */
	bool datacmp(data *a, data *b, int num) {
		for( int i = 0; i < num; i++) {
			if( mysqrt(a[i], b[i]) > 1 ) {
				return false;
			}
		}
		return true;
	}
#ifdef DEBUG
	void map(char * context) {
#else
		void map(HadoopPipes::MapContext& context) {
#endif
			// input format
			// --num of clusters ( == k)
			// --num of data( == n)
			// --initial centers for all clusters;
			// --input rows;

			double t[10];
			t[0] = gettime();



			std::vector<std::string> elements = HadoopUtils::splitString(
#ifdef DEBUG
					context, " ");
#else
			context.getInputValue(), " ");
#endif




			t[1] = gettime();

			const int k = HadoopUtils::toInt(elements[0]);
			const int n = HadoopUtils::toInt(elements[1]);
			const int len = HadoopUtils::toInt(elements[2]);
			const int randomseed = HadoopUtils::toInt(elements[3]);
			srand(randomseed);
			// c[] : pos of cluster
			// d[] : data
			// ndata[] : num of data for each cluster
			data c[2][k];
			data d[n];
			int ndata[k];
			int i, cur, next, iter,b;

			//structure is the first
			//initialize
			for(i = 0; i < k; i++) {

				c[0][i].len = len;
				c[0][i].v = (float*)malloc(sizeof(float)*len);
				c[1][i].v = (float*)malloc(sizeof(float)*len);
				c[1][i].len = len;
				for(b = 0; b < len; b++)
					c[0][i].v[b] = HadoopUtils::toFloat(elements[len*i+b+4]);
				//c[0][i].v[0] = HadoopUtils::toFloat(elements[2*i+4]);
				//c[0][i].v[1] = HadoopUtils::toFloat(elements[2*i+5]);
			}



			for(i = 0; i < n; i++) {

				d[i].v = (float*)malloc(sizeof(float)*len);
				d[i].len = len;
				for(b = 0; b < len; b++)
					d[i].v[b] = HadoopUtils::toFloat(elements[len*i+len*k+b+4]);

				//d[i].v[0] = HadoopUtils::toFloat(elements[2*i+2*k+4]);
				//d[i].v[1] = HadoopUtils::toFloat(elements[2*i+2*k+5]);

			}

			t[2] = gettime();


			/*#ifdef DEBUG
    for(i = 0; i < 2+k+n; i++) {
      std::cout << elements[i] << ' ';
    }
    std::cout << '\n';
    for(i = 0; i < k; i++) {
      std::cout << c[0][i].x << " " << c[0][i].y << "\t";
    }
    std::cout << '\n';
    for(i = 0; i < n; i++)
      std::cout << d[i].x << " " << d[i].y << " ";
    std::cout << '\n';
#endif*/

			t[3] = gettime();

			// buffer id
			cur = 0;
			next = 1;

			//    for(int j = 0; j < 10; j++) {
			iter = 0;
			do {
				iter++;
				init_int(ndata, k);

				//data object assignment
				assign_data(c[cur], d, n, k);

				t[4] = gettime();

				/*#ifdef DEBUG
      for(i = 0; i < n; i++)
	std::cout << d[i].cent << " ";
      std::cout << '\n';
#endif*/

				//rearranges all data objects
				//and counts the nunber of data objects contained by each cluster
				myqsort(d, 0, n-1);
				count_data_in_cluster(d, ndata, n, k);

				t[5] = gettime();


				/*#ifdef DEBUG
      for(i = 0; i < k; i++)
	std::cout << ndata[i] << " ";
      std::cout << '\n';
#endif*/

				//K centroids recalculation
				centroids_recalc(c[next], d, ndata, n, k);

				t[6] = gettime();

				/*#ifdef DEBUG
      for(i = 0; i < k; i++)
	std::cout << c[next][i].x << " " << c[next][i].y << " ";
      std::cout << "\n\n";
#endif*/

				cur = 1 - cur;
				next = 1 - next;

			} while(datacmp(c[cur], c[next], k) == false && iter < 100);
			//      }

			std::cout << iter << " iterations\n";

			//emit
			//key : cluster id
			//value : cluster centroid position
			for(i = 0; i < k; i++) {



				std::string s = "";
				for(int j = 0;j<len-1;j++)s+=HadoopUtils::toString((int)c[cur][i].v[j]) + '\t';
				s+=HadoopUtils::toString((int)c[cur][i].v[len-1]);
				printf("%s\n",&s[0]);

#ifndef DEBUG
				context.emit(context.getInputKey() + '\t' + HadoopUtils::toString(i),s);
				//HadoopUtils::toString((float)c[cur][i].v[0]) + '\t'
				//    		   + HadoopUtils::toString((float)c[cur][i].v[1]);
#endif
			}

			t[7] = gettime();

			std::cout << "Run on CPU" << '\n';
			std::cout << "iter : " << iter << '\n';
			for(i = 0; i < 7; i++) {
				std::cout << t[i+1] - t[i] << '\n';
			}
			std::cout <<  t[7] - t[0] << '\n';
			std::cout << '\n';

		}
#ifndef DEBUG
	};
#endif


	class KmeansReduce: public HadoopPipes::Reducer {
	public:
		KmeansReduce(HadoopPipes::TaskContext& context){}
		void reduce(HadoopPipes::ReduceContext& context) {
			while(context.nextValue()) {
				context.emit(context.getInputKey(), context.getInputValue());
			}
		}
	};

	int main(int argc, char *argv[]) {
		if(argc > 1) {
			deviceID = atoi(argv[1]);
			std::cout << "deviceID: " << '\n';
		}
#ifdef DEBUG
		std::string line;
		std::ifstream myfile("data.dat");
		if (myfile.is_open())
		{
			std::getline (myfile,line) ;
			myfile.close();
		}
		map(&line[0]);
		return 1;

#else
		return HadoopPipes::runTask(HadoopPipes::TemplateFactory<KmeansMap,
				KmeansReduce>());
#endif



	}


#include "stdint.h"

#include "Pipes.hh"
#include "TemplateFactory.hh"
#include "StringUtils.hh"

#include <iostream>
#include <cstdlib>
#include <cmath>

#include <time.h>
#include <sys/time.h>
//#include "../RPHash.cpp"
#include "../LSHDecoder.cpp"
#include "../leechArrayDecoder.c"

//#define DEBUG

//using namespace std;

int deviceID = 0;


#ifndef DEBUG
class RPHashPhase1Map: public HadoopPipes::Mapper {
public:
	RPHashPhase1Map(HadoopPipes::TaskContext& context){}
#endif

	void getTopBucketHashes(long* buckets, int k,  int hashMod, long* topBuckets, int* topBucketCounts )
	{
		//the list of highest hit count buckets
		int i,j = 0;
		//show tops
		for(j=0;j<k;j++)
		{
			int maxind = 0;
			int maxval = 0;
			for(i=0;i<hashMod;i++)
			{
				if(maxval<buckets[i])
				{
					maxval = buckets[i];
					maxind=i;
				}
			}
			topBuckets[j] = maxind;
			topBucketCounts[j] = maxval;
			buckets[maxind] = 0;
		}
	}


#ifndef DEBUG
	void map(HadoopPipes::MapContext& context) {
#else
		char* map(char * context) {
#endif
			// input format
			//per line
			//top ids list (integers)
			// --num of clusters ( == k)
			// --num of data( == n)
			// --num dimensions
			// --input random seed;



			Quantizer  q= Quantizer(decodeLeech, 24);
			initLSH(q);

			std::vector<std::string> elements = HadoopUtils::splitString(
#ifdef DEBUG
					context, " ");
#else
			context.getInputValue(), " ");
#endif

			const int k = HadoopUtils::toInt(elements[0]);
			const int n = HadoopUtils::toInt(elements[1]);
			const int dim = HadoopUtils::toInt(elements[2]);
			const int randomseed = HadoopUtils::toInt(elements[3]);
			srand(randomseed);

			int logn =1;
			while((1<<logn++) < n  );// poor mans log(numPoints)
			int hashMod = n*logn;

			long i,j;


			long topBuckets[k];
			int topBucketCounts[k];
			for(i=0;i<k;i++)
			{
				topBuckets[i] = 0;
				topBucketCounts[i] = 0;
			}

			//size of the buckets
			long* buckets = (long*)malloc(sizeof(long)*hashMod);

			for(i=0;i<hashMod;i++)
			{
				buckets[i] = 0;
			}
			float * dist;
			float* M = GenRandomN(q.dimensionality,dim,n);
			float* data = (float*)malloc(sizeof(float)*dim);
			for (i =0;i<n;i++){
				long start = i*dim+2;
				for (j =0;j<dim;j++)
				{
					data[j] = HadoopUtils::toFloat(elements[start+j]);
				}
				long hash = lshHash(data,dim, 1, hashMod,M, dist);
				buckets[hash]++;
			}

			for(i=0;i<k;i++)
			{
				topBuckets[i] = 0;
				topBucketCounts[i] = 0;
			}

			free(M);
			getTopBucketHashes(buckets,k, hashMod, topBuckets, topBucketCounts);
			free(buckets);


			for(i = 0; i < k; i++)
			{
#ifndef DEBUG
				context.emit( HadoopUtils::toString(topBuckets[i]),
				HadoopUtils::toString(topBucketCounts[i]) );
#else
				return HadoopUtils::toString(topBuckets[i])+
								HadoopUtils::toString(topBucketCounts[i]);
#endif
			}

		}
#ifndef DEBUG
	};
#endif

#ifndef DEBUG
	class RPHashPhase1Reduce: public HadoopPipes::Reducer {
	public:
		RPHashPhase1Reduce(HadoopPipes::TaskContext& context){}
		void reduce(HadoopPipes::ReduceContext& context) {
#else
			char* reduce(char* context) {
#endif

				long count = 0;
#ifndef DEBUG
				//--- get all tuples with the same key, and count their numbers ---
				while ( context.nextValue() )
				{
					count += HadoopUtils::toInt( context.getInputValue() );
				//--- emit (id, count) ---
					context.emit(context.getInputKey(), HadoopUtils::toString( count ) );
				}
#else
				return context ;
#endif

			}
#ifndef DEBUG
		};
#endif


int main(int argc, char *argv[]) {
	if(argc > 1) {
		deviceID = atoi(argv[1]);
		std::cout << "deviceID: " << '\n';
	}
	bool jobsuccess = false;
	jobsuccess &= HadoopPipes::runTask(HadoopPipes::TemplateFactory<RPHashPhase1Map,RPHashPhase1Reduce>());
	return jobsuccess;
}


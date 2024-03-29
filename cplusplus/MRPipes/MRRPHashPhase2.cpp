
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

int deviceID = 0;


#ifndef DEBUG
		class RPHashPhase2Map: public HadoopPipes::Mapper {
		public:
			RPHashPhase2Map(HadoopPipes::TaskContext& context){}
			void map(HadoopPipes::MapContext& context) {
#else
				void map(char* context) {
#endif
					// input format
					// --num of clusters ( == k)
					// --num of data( == n)
					// --num dimensions
					// --input random seed;

					Quantizer  q= Quantizer(decodeLeech, 24);
					initLSH(q);

					std::vector<std::string> elements = HadoopUtils::splitString(
#ifndef DEBUG
							context.getInputValue(), " ");
#else
					context, " ");
#endif

					const int k = HadoopUtils::toInt(elements[0]);
					const int n = HadoopUtils::toInt(elements[1]);
					const int dim = HadoopUtils::toInt(elements[2]);
					const int randomseed = HadoopUtils::toInt(elements[3]);
					srand(randomseed);

					int i,l,j=0;
					int numProbes = 1;

					while((1<<numProbes++) < n  );// poor mans log(numPoints)
					numProbes++;
					//birthday paradox hash universe
					int hashMod = n*numProbes;

					//numProbes*=10;

					//float* vec;
					float* curCentroid;
					long hash;
					int hashCollisionIdx;
					float* M;
					float divisor,*dist;
					long topBuckets[k];
					int topBucketCounts[k];
					float* centroids = (float*)malloc(sizeof(float)*k*dim);
					for(j=0;j<numProbes;j++)//multiple probes
					{
						M = GenRandomN(q.dimensionality,dim,n);
						for(i=0;i<n;i++)//iterate over vectors
						{

							float* data = (float*)malloc(sizeof(float)*dim);
							for (i =0;i<n;i++){
								long start = i*dim+2;
								for (j =0;j<dim;j++)
								{
									data[j] = HadoopUtils::toFloat(elements[start+j]);
								}
								long hash = lshHash(data,dim, 1, hashMod,M, dist);
								hashCollisionIdx = -1;
								for(l=0;l<k;l++)
								{
									//if hash is one of the top hashes...
									if(topBuckets[l]==hash)
									{
										hashCollisionIdx = l;
										l = k;//hash found, break!
									}
								}
								//update centroids if collision is found
								if(hashCollisionIdx>-1)
								{
									topBucketCounts[hashCollisionIdx]++;

									if(topBucketCounts[hashCollisionIdx]==1)
										memcpy(&centroids[hashCollisionIdx*dim],&data,dim);
									else
										for(l=0;l<dim;l++)centroids[hashCollisionIdx*dim+l]+=data[l];
								}
							}
							free(M);
						}
						for(i=0;i<k;i++)
						{

							curCentroid = &(centroids[i*dim]);
							divisor = 1.0;
							if( topBucketCounts[i]>0)
								divisor = 1.0/(float)topBucketCounts[i];
							for(j=0;j<dim;j++)
								centroids[i*dim+j] *= divisor;


						}

#ifndef DEBUG
					};

					class RPHashPhase2Reduce: public HadoopPipes::Reducer {
					public:
						RPHashPhase2Reduce(HadoopPipes::TaskContext& context){}
						void reduce(HadoopPipes::ReduceContext& context){
#else
						void reduce(char* context){
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

								//	  int length, int dim, long* bucket1,
								//	                          int* bucketCounts1, float* bucketCentroids1, long* bucket2,
								//	                          int* bucketCounts2, float* bucketCentroids2,  long* topBucket ,
								//	                          int* topBucketCounts,float* topBucketCentroids)
								//	      //this method merges buckets 4*k go in 2*k come out
									      int i,j = 0;
								//fill top bucket with bucket1's data (parallel implementation mayh choose
								//computer on which this data resides to start with)
/*
	      for(k=0;k<length;k++)
	      {
	          topBucket[k] = bucket1[k];
	          topBucketCounts[k] = bucketCounts1[k];
	          memcpy(&topBucketCentroids[k],&bucketCentroids1[k],dim);
	      }


	      for(i=0;i<length;i++){
	          printVecF(&bucketCentroids1[i],dim);
	      }

	      //iterate over bucket2 , merge same buckets
	      for(k=0;k<length;k++)
	      {
	           for(i=0;i<length;i++)
	          {
	              if(topBucket[k] == bucket2[i])
	              {//recompute centroid bias to the support ratio
	                  for(j=0;j<dim;j++)
	                  {
	                      topBucketCentroids[k*dim+j]= (bucketCentroids2[i*dim+j]*   bucketCounts2[i]
	                                              +   topBucketCentroids[k*dim+j]* topBucketCounts[k])
	                                                                      /2.0 ;
	                  }
	                  topBucketCounts[k] += bucketCounts2[k];
	                  bucketCounts2[k] = 0;
	              }
	          }
	      }



//	              ^
//	      * note these| have to be 2 steps, otherwise merged
//	      * clusters  | could be skipped, if they are replaced before merging
//	                  v
	      //iterate over bucket2 again
	      for(k=0;k<length;k++)
	      {
	          //skip if already merged
	          if(bucketCounts2[k]!=0)
	          {
	              //find min in topBuckets, try to merge
	              int minind = 0;
	              int minval = bucketCounts2[k];
	              for(i=0;i<length;i++)
	              {
	                  if(minval>topBucketCounts[i])
	                  {
	                      minval = topBucketCounts[i];
	                      minind=i;
	                  }
	              }
	              //if greater than min bucket, replace the min
	              if(bucketCounts2[k]>minval){

	                  for(j=0;j<dim;j++)
	                  {//recompute centroid bias to the support ratio
	                      topBucketCentroids[minind*dim+j]= (bucketCentroids2[k*dim+j]*   bucketCounts2[k]
	                                              +   topBucketCentroids[minind*dim+j]* topBucketCounts[minind])
	                                                                      /2.0 ;
	                  }
	                  topBucket[minind] = bucket2[k];
	                  topBucketCounts[minind] = bucketCounts2[k];
	              }
	          }
*/


	      //emit topBuckets, topBucketCounts , topBucketCentroids byref



int main(int argc, char *argv[]) {
	if(argc > 1) {
		deviceID = atoi(argv[1]);
		std::cout << "deviceID: " << '\n';
	}

	return HadoopPipes::runTask(HadoopPipes::TemplateFactory<RPHashPhase2Map,RPHashPhase2Reduce>());
}

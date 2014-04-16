#include "LSHDecoder.c"
#include "leechArrayDecoder.c"
#include "testUtils.c"
#include "IOUtils.c"

#include <stdlib.h>
#include <sys/time.h>

//off is better?
//#define AVG_CLUSTER


//#define AVG_CLUSTER
/*
 * to avoid name conflict, test distance is
 * a euclidean distance function for arbitrary
 * length @len vectors r and q. furthermore
 * unlike@distance() in the main class, it
 * takes the square root.
 */
float testDist(float* r , float * q,int len ){
  float dist = 0;
  int i;
  for (i=0;i<len;i++)
  {
      //printf("%.1f,",(float)r[i]);
      dist += ((float)r[i]- (float)q[i]) *((float)r[i]- (float)q[i])  ;
  }
  //printf("\n");
return quicksqrt(dist);
}


void getTopBucketHashes(int* buckets, int cutoff,  int hashMod, int* topBuckets )
{
  //the list of highest hit count buckets
    int i,k = 0;
    //show tops
    for(k=0;k<cutoff;k++)
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
        topBuckets[k] = maxind;
        buckets[maxind] = 0;
    }
}

float* dist;
void rpHashPhase1(float* ret, int numPoints, int dim, Quantizer* q, int hashMod, int cutoff,int* topBuckets){
    int d = q->dimensionality;
    int i=0;
    //size of the buckets
    int* buckets = malloc(sizeof(int)*hashMod);

    //pass by reference container
    float* M = GenRandomN(d,dim,numPoints);

    
    for(i=0;i<numPoints;i++)
    {  
        long hash = lshHash(&ret[i*dim],dim, 1, hashMod,M, &dist);
        //printVecF(&ret[i*dim],dim);
        buckets[hash]++;
    }
    
    free(M);
    getTopBucketHashes(buckets,cutoff, hashMod, topBuckets);
    free(buckets);
}

void rpHashPhase2(float* ret, int numPoints, int dim, Quantizer* q, int hashMod, 
                          int titularK, int cutoff,int* topBuckets, float* centroids)
{
    
    int d = q->dimensionality;
    int i,k,j=0;
    int numProbes = 0;
    while((1<<numProbes++) < numPoints  );// poor mans log(numPoints)
    numProbes++;
    //numProbes*=10;
    
    float* bucketsAvgs = malloc(sizeof(float)*cutoff*dim);
    int * bucketsAvgAccums = malloc(sizeof(int)*cutoff);
    for(i=0;i<cutoff;i++)bucketsAvgAccums[i]=0;


    float* vec;
    float* curCentroid;
    long hash;
    int hashCollisionIdx;
    float* M;
    float divisor;
    
    for(j=0;j<numProbes;j++)//multiple probes
    {
      M = GenRandomN(d,dim,numPoints);
      for(i=0;i<numPoints;i++)//iterate over vectors
      {
           vec = &(ret[i*dim]); 
           hash = lshHash(vec,dim, 1, hashMod,M, &dist);
           //check if hash collides with a top bucket
           hashCollisionIdx = -1;
           for(k=0;k<cutoff;k++)
           {
               //if hash is one of the top hashes...
               if(topBuckets[k]==hash)
               {
                   hashCollisionIdx = k;
                   k = cutoff;//hash found!
               }
            }
           //update centroids if collision is found
           if(hashCollisionIdx>-1)
           { 
             curCentroid = &bucketsAvgs[hashCollisionIdx*dim];
             bucketsAvgAccums[hashCollisionIdx]++;
             
             //printf("%i,%i,%i\n", bucketsAvgAccums[hashCollisionIdx],hashCollisionIdx,hash);

             if(bucketsAvgAccums[hashCollisionIdx]==1)
                 //initialize this centroid
                 for(k=0;k<dim;k++)curCentroid[k]=vec[k]; 
             else
                 //just sum up all the vectors in this buckets
                 for(k=0;k<dim;k++)curCentroid[k]+=vec[k];
           }    
      }
      
      free(M);
    }
    

    
    
    for(i=0;i<cutoff;i++) 
    {    
        curCentroid = &bucketsAvgs[i*dim];
        divisor = 1.0;
        if( bucketsAvgAccums[i]>0)
            divisor = 1.0/(float)bucketsAvgAccums[i];
        for(j=0;j<dim;j++)
            curCentroid[j] *= divisor;
    }
    
    getTopBucketHashes(bucketsAvgAccums, titularK, cutoff, topBuckets);
    
    
    
    for(k=0;k<titularK;k++) 
    {
        //printVecF(&bucketsAvgs[topBuckets[k]*dim],dim);

        for(j = 0;j<dim;j++)centroids[k*dim+j]=bucketsAvgs[topBuckets[k]*dim+j];
    }
    //printVecF(centroids,dim);



    
    
    //free(distances);
}


//more memory conservative 2x time complexity
//main idea is to probe twice, first round maintain counts for all buckets
//second round only store and compute the top k bucket averages
//the complexity is still linear, while memory is O(k*m) instead of O(k*m *||lambda||)
void rpHash2(float* data, int numPoints, int dim, Quantizer* q, int hashMod, int k,float* centroids)
{

    int cutoff = 4*k;
    int* topBuckets = malloc(sizeof(int)*cutoff);//k or 2*k according to Edo Liberty
    rpHashPhase1(data, numPoints, dim, q, hashMod, cutoff, topBuckets);
    rpHashPhase2(data, numPoints, dim, q, hashMod, k , cutoff, topBuckets,centroids);


    //<---------- this is the second phase---------->
    //<---------- for parallel, this would be the broadcast to all nodes, mapper step ------------->

}








/*
 * This function projects points until relatively large buckets of points begin to emerge.
 * Serving as an attempt to prove the hypothesis that these points are the approx.
 * density modes of the later discovered clusters.
 * max number of tests, l is the target bucket size before emitting to the parallel system
 * dim is the vector dimensionality
 */
void rpHash(float* ret, int numPoints, int dim, Quantizer* q, int hashMod, int cutoff,float* centroids)
{
  int d = q->dimensionality;
  int i,k,j=0;

  int numProbes = 0;
  while((1<<numProbes++) < numPoints  );// poor mans log(numPoints)
  //while(numProbes*numProbes<hashMod)numProbes++;<-- Bday Paradox, way to high!
  numProbes++;
  /*
    <---------------THIS IS THE MAPPER--------------->
   */

  //size of the buckets
  int* buckets = malloc(sizeof(int)*hashMod);
  for(i=0;i<hashMod;i++) buckets[i]=0;
  //bucket centroid
  float* bucketsAvgs = malloc(sizeof(float)*hashMod*dim);
  for(i=0;i<hashMod*dim;i++) bucketsAvgs[i]=0.0f;
  //sum of decoding distances
  float* distances = malloc(sizeof(float)*hashMod);
  //pass by reference container
  float distance;



  long l = time(NULL);
  for(j=0;j<numProbes;j++){

    float* M = GenRandomN(d,dim,numPoints);
    for(i=0;i<numPoints;i++)
    {
        int hash = (int) lshHash(&ret[i*dim],dim, 1, hashMod,M, &distance);

        //accumulate hits to this bucket
        buckets[hash]++;
        distances[hash]+=distance;

        //compute a moving average for current bucket
        //m_{i+1} = (m_{i}+v)/(n+1)
        for(k=0;k<dim;k++)
          bucketsAvgs[hash*dim+k]=(bucketsAvgs[hash*dim+k]*((float)buckets[hash])+ret[i*dim+k])/(((float)buckets[hash])+1.0);
    }
    free(M);
  }

  //printf("%u\n",time(NULL)-l);
  int *centroidIdx = malloc(sizeof(int)*cutoff);

  //initialize the top cutoff buckets
  for(i=0;i<cutoff;i++)
      centroidIdx[i] = i;

  //find the top k
  for(i=cutoff;i<hashMod;i++)
  {
    int argleast = 0;
    int sizeofCandidate = buckets[i];
/*
  <---------------THIS IS THE REDUCER--------------->
 */


    for(k=0;k<cutoff;k++)
    {
        //use XOR'd lattice ID as general distance measure
        //find overlaps in the list k
        //can compute as max distance to cluster's average distance                    vvvvv though it may still be gaussian bound apriori, simulate to check.
        if(testDist(&bucketsAvgs[i*dim], &bucketsAvgs[centroidIdx[k]*dim],dim)<quicksqrt(3.0))
          {
            //weighted average nearby buckets
            int s = buckets[centroidIdx[k]] + buckets[i];
#ifdef AVG_CLUSTER
            for(j=0;j<dim;j++)
            {
                bucketsAvgs[centroidIdx[k]*dim+j] = ( (buckets[centroidIdx[k]])*bucketsAvgs[centroidIdx[k]*dim+j]
                                                                                           + (buckets[i]) * bucketsAvgs[i*dim+j]  )/((float)s);
            }
#endif
            buckets[centroidIdx[k]]=s;
            buckets[i] =0;
            k=cutoff;//break
        }else{
          //checks for biggest buckets
          if(  sizeofCandidate >  buckets[centroidIdx[k]] )
            {
            //then the kth bucket can be replaced
            //but we need to also keep looking if
            //something is even less
            sizeofCandidate = buckets[centroidIdx[k]];

            //this is the vetor that is getting replaced
            argleast = k;
          }
        }
      }

      //after the least is found it should be in argleast
      if( buckets[i] >buckets[centroidIdx[argleast]] )centroidIdx[argleast] = i;
  }

  //load return vector of centroids
  //float * centroids = malloc(sizeof(float)*dim*k);

  for(k=0;k<cutoff;k++)
  {
    for(j = 0;j<dim;j++)centroids[k*dim+j]=bucketsAvgs[dim*centroidIdx[k] + j];

    //printf("%i:%i\n",centroidIdx[k],buckets[centroidIdx[k]]);
  }

  free(buckets);
  free(distances);
  free(centroidIdx);
  free(bucketsAvgs);

}



void clusterFile(const char* infile , const char* clusterFile,int numClusters, int hashMod)
{
    long numVectors;
    long dim;
    Quantizer * q= initializeQuantizer(decodeLeech, 24);
    initLSH(q);

    // somewhere to put stuff
    int nu,i,j;
    // could use this to compute average distance and cluster affinity
    float* ret= mat(infile,&numVectors,&dim);

    //printf("Clustering %ix%i\n",numVectors,dim);

    struct timeval tv;
    struct timezone tz;
    gettimeofday(&tv, &tz);
    double start1 =tv.tv_sec+((float)tv.tv_usec)/1000000.0;

    float* centroids = malloc(sizeof(float)*numClusters*dim);

    rpHash (ret, numVectors, dim, q, hashMod, numClusters, centroids);
    //rpHash2(ret, numVectors, dim, q, hashMod, numClusters, centroids);
    gettimeofday(&tv, &tz);
    double end1 = tv.tv_sec+((float)tv.tv_usec)/1000000.0;
    printf("%f ", end1-start1);

    if(clusterFile==NULL){
      write("out.mat", numClusters, dim, centroids);
    }

    free(ret);
    //free(centroids);
}


int main(int argc, char* argv[])
{


  srand((unsigned int)1532711);
  //countUnique();
//  testRatios(100000, 60, 24);
//  testRatios(100000, 60, 240);
//  testRatios(100000, 60, 2400);
//return;
//countUnique();

    
  int hashMod = 10000;
  char* centsFile = NULL;
  int clusters = 8;
 //dont generate data just run
  if(argc==1){
      int partitionSize = 2000;
      int dim = 1000;
      float* centroids = malloc(sizeof(float)*clusters*dim);
      //rpHash2(ret, numVectors, dim, q, hashMod, numClusters,centroids);
      //big_bucket_Search(partitionSize,clusters,dimensions,hashMod);
      return 0;
  }

  srand((unsigned int)time(0));

  //cluster a file and compare to another file with cluster centers
  if(argc>3) centsFile = argv[3];

  //cluster a file
  if(argc>4) hashMod=atoi(argv[4]);
  
  
  clusterFile(argv[1] ,centsFile, atoi(argv[2]), hashMod);

  return 0;
}



#include <stdlib.h>
#include <stdio.h>
/*
 * Generate a 'good enough' gaussian random variate.
 * based on central limit thm , this is used if better than
 * achipolis projection is needed
 */
double randn() {
  int i;
  float s = 0.0;
  for(i = 0;i<6; i++)s+=((float)rand())/RAND_MAX;
  return s - 3.0;

}
/*printLargestK(DATA,K,LEN)
 *Print the top @K items from a @LEN length list : @DATA .
 */
void printTopK(int* buckets,int cutoff,int hashMod){
    int i,k = 0;
    int* storeOld = malloc(sizeof(int)*cutoff);
    int* storeOldIdx = malloc(sizeof(int)*cutoff);
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
        printf("%i:%i, ",maxind,maxval);
        storeOld[k]=maxval;
        storeOldIdx[k]=maxind;
        buckets[maxind] = 0;
    }printf("\n");

    //restore previous olds
    for(k=0;k<cutoff;k++){
        buckets[storeOldIdx[k]] =storeOld[k];
    }



}

/*
 *print @size values of an integer vector @v
 */
inline void printVecI(int* v,int size){
    int i = 0;

    for(;i<size-1;i++)
        printf("%i,",v[i]);
    printf("%i\n",v[i]);
}

/*
 *print @size values of an float vector @v
 */
inline void printVecF(float* v,int size){
int i = 0;
for(;i<size-1;i++)
    printf("%.2f,",v[i]);
printf("%.2f\n",v[i]);

}


/*
 * print the binary expansion of a long integer. output ct
 * sets of grsize bits. For golay and hexacode set grsize
 * to 4 bits, for full leech decoding with parity, set grsize
 * to 3 bits
 */
static void print(unsigned long ret,int ct,int grsize){
    int i,j;//,err;
    for(i=0;i<ct;i++)
    {
        for(j=0;j<grsize;j++)
        {
            printf("%lu",ret&1);
            //err +=ret&1;
            ret=ret>>1;

        }
        printf(" ");
    }
    //if(err%2) printf("error \n");else
      printf("\n");
}

int weight(unsigned long u){
  int ret = 0;
  while(u>0)
   {
      if((u&1) == 1)ret++;
      u = (u>>1);
  }
  return ret;
}





/*
 * Create a random float vector @r of length @len and
 * @sparseness
 */
void genRandomVector(int len,float sparesness,float* r)
{
  while(len>0){
      if(((float)rand())/((float)RAND_MAX)< sparesness)
          r[len-1]=2.0*((float)rand())/RAND_MAX-1.0;
      else r[len-1]=0.0;
      len--;
  }

}




/*
 * Lets prove we are not accidentally finding clusters.
 */
void shuffle(float* M,int dim,int len)
{
  int j,i = 0;
  for(;i<len;i++)
  {
      int r = rand()/RAND_MAX;
      for(j = 0;j<dim;j++)
      {
          float temp = M[r*dim+j];
          M[r*dim+j] = M[i*dim+j];
          M[i*dim+j] = temp;

         // xor swap
        /*
          M[r*dim+j] ^= M[i*dim+j] ;
          M[i*dim+j] ^= M[r*dim+j];
          M[r*dim+j] ^= M[i*dim+j] ;
          */
      }

  }

}

/*
 * Create a scaled histogram for the counts data
 * inx and the indexed data iny. b is the number of
 * buckets, and len is the length of iny and inx.
 */

float histogram(int b, int len, float* inx,float* iny)
{
  int i,j;
  float max = -RAND_MAX;
  float min = RAND_MAX;//big number

  int* cts = malloc(sizeof(int)*b);
  float * out =malloc(sizeof(float)*b);

  //initialize output
  for (i=0;i<b; i++)
   {
      cts[i]=0;
      out[i]=0;
   }


  for (i=0;i<len; i++)
   {
      if(inx[i]>max)max =(float) inx[i];
      if(inx[i]<min)min =(float) inx[i];
   }
  float wsize = ((float)max-min) /((float) b);

  for (i=0;i<len; i++)
  {
      j = 0;
      while(j+1<b && inx[i] > min+wsize*j)j++;
      out[j] += (float)iny[i];
      cts[j]++;
  }

  //scale averages
  for (i=0;i<b; i++)
      if(cts[i]!=0)out[i]=out[i]/((float)cts[i]);
  printVecF(out,b);
  for (i=0;i<b; i++)printf("%.0f, ",wsize*i);printf("\n");
  return wsize;
}


int listSearchAndAdd(unsigned long query, unsigned long* list,int len)
{
  list[len]=query;
  while(--len)
      if(list[len]==query)
        return len;

  return len;
}



/*
 * Return the index of the nearest neighbor to vector v
 */
int NN(float* v, float*M,int dim,int len)
{

  int i,j = 0;
  float dist = 0.0f;
  int argmin = 0;
  float min = 10000.0f;
  float* q;
  for(;j<len;j++)
    {
      dist =0.0f;
      q = &M[j*dim];
      for (i=0;i<dim;i++)
      {
          dist += (v[i]- q[i]) *(v[i]- q[i])  ;
      }
      if(dist<min){
          min = dist;
          argmin = j;
      }

  }
  return argmin;
}


/*
 * generate random centers
 */
float * generateRandomCenters(int d,int clu)
{
  int i;
  float *clusterCenters =(float*) malloc(sizeof(float)*d*clu);
  for (i=0;i<clu;i++){
      genRandomVector(d,1.0,&clusterCenters[i*d]);
  }

  return clusterCenters;
}



/*
 * Very optimized Gaussian random cluster generator
 */
float* generateGaussianClusters(int part,int d,int clu,float* clusterCenters){//, float *clusterCenters){//, float *ret){

    float *ret = (float*)malloc(sizeof(float*)*d*part*clu);

    int i,j,k;

    for (i=0;i<clu;i++){
        //must be positive and near 1, around .1 , higher numbers results in data out of range ~(-1:1)
        float variance = .1;//*((float)rand())/RAND_MAX;
        float* ct = &clusterCenters[i*d];
        //these are the partitions
        for(j = 0;j<part;j++)
          {
            //these are the individual vectors
            for( k=0;k<d;k++)
              {
                float pts = (randn()*4.0);
                ret[i*part*d +j*d+k]= (pts*variance+ ct[k]);
            }

        }
    }

    return ret;
}


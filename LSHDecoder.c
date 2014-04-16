
// VV not working VV
//#include "MurmurHash3.cpp"
//^^                   ^^
#define  RAND_MAX 2147483647

#include <stdlib.h>
#include <stdio.h>


typedef struct quantizer_t {
        int dimensionality;//could contain other data like entropy, nominal coding gain, density
        long (* decode)();
} Quantizer;

Quantizer q;




/*
 * Generate a 'good enough' gaussian random variate.
 * based on central limit thm , this is used if better than
 * achipolis projection is needed
 */
double sampleNormal() {
  int i;
  float s = 0.0;
  for(i = 0;i<6; i++)s+=((float)rand())/RAND_MAX;
  return s - 3.0;

}

inline float quicksqrt(float b)
{
    float x = 1.1;
    unsigned char i =0;

    for(;i<16;i++){
        x = (x+(b/x))/2.0;
    }

    return x;
}

/*
 * print the binary expansion of a long integer. output ct
 * sets of grsize bits. For golay and hexacode set grsize
 * to 4 bits, for full leech decoding with parity, set grsize
 * to 3 bits
 */
static void print2(unsigned long ret,int ct,int grsize){
    int i,j;
    for(i=0;i<ct;i++)
    {
        for(j=0;j<grsize;j++)
        {
            printf("%d",ret&1);
            ret=ret>>1;
        }

    } printf("\n");
}

void initLSH(Quantizer* quanti)
{
  //srand((unsigned int)12412471);
  q = *quanti;
}


//TODO create two seperate projections. One is the db good N(0,1) projection
//          the other is our fast random 1/3 projection

void project(float* v, float* r,int* M,float randn, int n,int t){
  int i,j;//b=(int)((float)n/(float)6);
  float sum;
  randn = 1.0/quicksqrt(n);

  unsigned int b = 0L;
  for(i=0;i<t;i++)
  {
   /*

      sum = 0.0;
      /* pre M matrix
      for(j=0;j < b; j++ )
          sum+=v[M[i*b*2+j]]*randn;

      for(;j < 2*b; j++ )
          sum-=v[M[i*b*2+j]]*randn;
      */
/*
      //1/3, 2/3, 1/3
      for(j=0;j<n;j++)
           {
              b = rand()%6;
              if(b==0)sum+=randn;
              if(b==1)sum-=randn;


           }
*/
      //1/2, 1/2

      for(j=0;j<n;j+=31)
      {
          b = (1<<31)^rand()%RAND_MAX ;

          while(b>1)
          {
              if(b&1)
                  sum+=randn;
              else
                  sum-=randn;
              b>>=1;
          }
          //if(rand()%RAND_MAX){
          //    sum+=randn;
          //}
          //else{
          //    sum-=randn;
             //printf("%f\n",randn);
          //}
      }
      r[i] = sum;
  }
}

float* GenRandomN(int m,int n,int size){
  float* M = malloc(m*n*sizeof(float));
  int i =0;
  float scale = (1.0/quicksqrt((float)n));
  int r = 0;
  for(i=0;i<m*n;i++)
    {
        r = rand()%6;
        M[i] = 0.0;
        if(r%6==0)M[i] = scale;
        if(r%6==1)M[i] = -scale; 
    }
    
  //size throws things way to far from the lattice^^^interval, n seems better
  // proof this is  in the rnd proj method book

  return M;
}

void projectN(float* v, float* r,float* M, int n,int t){
  int i,j,b=(int)((float)n/(float)6);
  float sum;
  for(i=0;i<t;i++)
  {
      sum = 0.0;
      for(j=0;j < n; j++ )
          sum+=v[i]*M[i*n+j];

      r[i] = sum;
  }
}

/*
 * from Achlioptas 01 and JL -THm
 * r_ij = sqr(n)*| +1    Pr =1/6
 *                      |    0    Pr=2/3
 *                      |  - 1   Pr =1/6
 *
 *                      Naive method O(n), faster select and bookkeeping
 *                      should be O((5/12 )n), still linear, but 2x faster
 *                      cost of bookkeeping is n to create, then 5/12n to check
 *                      extra. but is RAND expensive in comparison
 *                       Assume we will collide with constant probability
 *        Maths:
 *        prob of collision in 1/3 is 1/12, add penalty
 *        log(3/2)
 *         is it repeated intersection 1/3,1/12,1/48 converges to ...
 *        expriments:
 *        bookkeeper lengths
 *        numerical results peg log(3/2) , how may require some brushing up on
 *        series and continuous UBE
 */


float GenRandom(int n,int m,int *M){

    int l,i,r,j,b=(int)((float)n/(float)6);
    float sum;
    float randn = 1.0/(quicksqrt(((float)m)*3.0)) ;//variance scaled back a little

    unsigned char* bookkeeper = malloc(sizeof(unsigned char)*n);
    M = malloc(2*b*sizeof(float));

    //reset bookkeeper
    for(l=0;l < n; l++ )bookkeeper[l]=q.dimensionality+1;


    j=0;
    for(i=0;i<q.dimensionality;i++)
    {

        sum = 0.0;
        for(l=0;l < b; l++ )
        {
            do{r =rand()%n;}
            while(bookkeeper[r]==l );
            bookkeeper[r]=l;

            M[j++] = r;
        }

        for(;l < 2*b; l++ )
        {
          do{ r =rand()%n;}
          while(bookkeeper[r]==l );

          bookkeeper[r]=l;
          M[j++] = r;
        }
    }
    free(bookkeeper);

    
    return randn;
    }

const unsigned long fnvHash (unsigned  long key, int tablelength )
{


      unsigned char* bytes = (unsigned char*)(&key);
      unsigned long hash = 2166136261U;
      hash = (16777619U * hash) ^ bytes[0];
      hash = (16777619U * hash) ^ bytes[1];
      hash = (16777619U * hash) ^ bytes[2];
      hash = (16777619U * hash) ^ bytes[3];
      hash = (16777619U * hash) ^ bytes[4];
      hash = (16777619U * hash) ^ bytes[5];
      hash = (16777619U * hash) ^ bytes[6];
      hash = (16777619U * hash) ^ bytes[7];
      return hash %tablelength;
}

const unsigned long fnvHashStr(unsigned char* data, int len,int tablelength)
{
   unsigned long hash= 2166136261U;
   int i =0;
    for ( i=0; i < len; i++) {
        hash = (16777619U * hash) ^ (unsigned char)(data[i]);
    }
    return hash%tablelength;
}




//unsigned long ELFHash(const unsigned char *key,int tablesize)
unsigned long ELFHash(unsigned long key,int tablesize)
{
  unsigned long h = 0;
  while(key){

    h = (h << 4) + (key&0xFF);
    key>>=8;
    unsigned long g = h & 0xF0000000L;
    if (g) h ^= g >> 24;
    h &= ~g;
  }
  return h%tablesize;
}

/*
 * Decode full n length vector. Concatenate codes and run universal hash(fnv,elf, murmur) on whole vector decoding.
 */
unsigned long lshHash(float *r, int len, int times, long tableLength,float* R, float *distance){
    *distance = 0;//reset distance

    //unsigned char rn;
    //int b=(int)((float)len/(float)6);

    if(len==q.dimensionality)return fnvHash(q.decode(r,distance), tableLength);


     float * r1 =malloc(q.dimensionality*sizeof(float));
     //float randn = 1.0/quicksqrt((float)len);
     int k=0;
     unsigned long ret = 0L;

     do{

         projectN(r, r1,R, len,q.dimensionality);//r1 if this is on
         ret =  q.decode(r1,distance);



           //sometimes the RP throws stuff out of the lattice
           //check min/max are near the interval [-1,1]
//           int d = 1;
//           float sump = r1[0];
//           float summ = r1[0];
//           float avg = 0.0;
//           for(;d<24;d++){
//               if(summ>r1[d])summ =r1[d] ;
//               if(sump<r1[d])sump =r1[d] ;
//               avg+=r1[d];
//           }
//           printf("proj %f, %f, %f\n",summ,avg/24.0,sump)//;

//           d = 1;
//           sump = r[0];
//           summ = r[0];
//           avg = 0.0;
//           for(;d<len;d++){
//               if(summ>r[d])summ =r[d] ;
//               if(sump<r[d])sump =r[d] ;
//               avg+=r[d];
//           }
//           printf("norm %f, %f, %f\n",summ,avg/len,sump);

         k++;
     }while(k<times);


  free(r1);

  return fnvHash(ret, tableLength) ;
}



Quantizer * initializeQuantizer( long(* decode)(float*,float*),int dim)
{
    Quantizer *q=(Quantizer *)malloc(sizeof(Quantizer));
    q->dimensionality=dim;
    q->decode = decode;
    return q;
}



#include <stdio.h>
float* mat(const char * infile,long * m,long *n){
     long length = 0;
     FILE    *fptr;
     /* Open the file */
     if ((fptr = fopen(infile,"r")) == (char)0) {
          fprintf(stderr,"Unable to open data file\n");
          printf("%s",infile);
          exit(0);
     }

     char* str = malloc(sizeof(char)*64);
     fgets(str,64,fptr);
     sscanf(str,"%i",m);
     fgets(str,64,fptr);
     sscanf(str,"%i",n);

     float* data = malloc(sizeof(float)*(int)(*m)*(int)(*n));
     /* Read as many points as we can */

     while (fgets(str,64,fptr) !=NULL) {
          if(length>((int)(*m)*(int)(*n))){
               printf("Malformed Data File\n");
               return;
          }
          sscanf(str,"%f",&data[length++]);

     }

     fclose(fptr);
     free(str);
     //printf("read %u \n",length,m,n);

     return data;

}


int write(const char * outfile,int m,int n, float* data){

     int length = 0;
     float d;
     FILE    *fptr;
     /* Open the file */
     if ((fptr = fopen(outfile,"wt")) == NULL) {
          printf("Unable to open data file\n");
          printf("%s",outfile);
          return 1;
     }

        fprintf (fptr, "%i\n",m);
        fprintf (fptr, "%i\n",n);
        int i;
        for(i=0;i<m*n;i++)fprintf (fptr, "%f\n",data[i]);
        fclose (fptr);
        return 0;



}

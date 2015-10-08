'''
    This program reads matrix files with name in "argv[1]#.mat" format
    and puts them in a randomly shuffled order into a matrix file as
    and stores the original labeling map.
'''

import sys
import random 

basename = sys.argv[1]
numClusters = int(sys.argv[2])
outputfile = sys.argv[3]

def readVectorIterator(f):
    x = int(f.readline())
    y = int(f.readline())
    for i in xrange(x):
        vec = [0.0]*y
        for j in xrange(y):
            vec[j] = float(f.readline())
        yield vec
    f.close()

#read dimensions, assume all files are equal length
f=file(basename+"1.mat",'r')
x = int(f.readline())
y = int(f.readline())


#write new dimensions of mixed file
outfiledat = file(outputfile,"w")
outfiledat.write(str(x*numClusters)+'\n')
outfiledat.write(str(y)+'\n')

outfilelbl = file("lbl_"+outputfile,"w")
outfilelbl.write(str(x*numClusters)+'\n')
outfilelbl.write(str(1)+'\n')

#load up the file iterators
fileiterators = []
for i in range(numClusters):
    f=file(basename+str(i)+".mat",'r')
    fileiterators.append(readVectorIterator(f))

#create and shuffled a clusterlabel index
vecidx = [c for c in range(numClusters) for d in range(x)]
random.shuffle(vecidx)

for vecid in vecidx:
    vec = fileiterators[vecid].next()
    for i in xrange(y):
        outfiledat.write(str(float(vec[i]))+'\n')
    outfilelbl.write(str(vecid)+'\n')

outfiledat.close()
outfilelbl.close()




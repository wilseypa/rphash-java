'''
Created on Jan 9, 2014

@author: lee
'''

def readMatFile(name):
    f = file(name,"r")
    x = int(f.readline())
    y = int(f.readline())
    M = [[0.0]*y for a in xrange(x)]
    
    for i in xrange(len(M)):
        for j in xrange(len(M[i])):
            M[i][j] = float(f.readline())
    f.close()
    return M

def writeMatFile(M,name):
    f = file(name,"w")
    f.write(str(len(M))+'\n')
    f.write(str(len(M[0]))+'\n')
    for i in xrange(len(M)):
        for j in xrange(len(M[i])):
            f.write(str(M[i][j])+'\n')
    f.close()
    return name

def readVectorIterator(f):
    x = int(f.readline())
    y = int(f.readline())
    for i in xrange(x):
        vec = [0.0]*y
        for j in xrange(y):
            vec[j] = float(f.readline())
        yield vec
    f.close()


#import random
#k = 20
#n = 1000
#d = 500
#M = []
#for i in range(k):#
#	cent = [random.random()*2.0-1.0 for j in range(d)]
#	for l in range(n/k):
#		b = []
#		for j in range(d):
#			b.append(cent[j]+random.gauss(0.0,1.0))
#		M.append(b)
#writeMatFile(M,"outputs.mat")

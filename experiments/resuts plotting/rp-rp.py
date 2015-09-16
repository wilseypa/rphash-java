from pylab import *
from pyIOUtils import *
sparsity = .1
def roundspecial(X):
    for i in xrange(len(X)):
        if X[i]>.1:X[i]=0.0
    return X

def genData(n,m,k):
    #cent = []
    #data = []
    #cent = rand(k,m)
    #for i in xrange(n):
    #    data.append(randn(m)*.1+cent[i%k])
    #sparsify
    #for i in xrange(n):
    #    r = roundspecial(rand(m))
    #    data[i]*=r
    return array(readMatFile("allmimicsigdata.mat"))#data)
def euclidean(x,y):
    return sum((x-y)**2)**.5

def nn(x,A):
    mini = euclidean(x,A[0])
    minarg = 0
    for n in xrange(1,len(A)):
        if euclidean(x,A[n])<mini:
            mini = euclidean(x,A[n])
            minarg = n
    return minarg

from scipy.spatial import cKDTree
def countInt(A,B):
    t = cKDTree(B)
    ct = 0;
    for i in xrange(len(A)):
        if t.query(A[i],5)[1].tolist().__contains__(i):
            ct+=1
        #if nn(A[i],B) == i:ct+=1
    return ct

def run(n,m,k,l):
    n = 38
    m=256
    k = 1
    data = genData(n,m,k)
    R = randn(m,l)
    pData = dot(data,R)
    invR = linalg.pinv(R)
    #invR = randn(l,m)
    pinvData = dot(pData,invR) 
    return countInt(pinvData,data)

print "k=10,l=24,n=5000, avgs=10"
for i in range(1,256,4):
    print str(i),
    for j in range(10):
        print run(38,256,1,i)/38.,
    print ""



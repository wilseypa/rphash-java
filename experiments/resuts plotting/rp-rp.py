from pylab import *
sparsity = .1
def roundspecial(X):
    for i in xrange(len(X)):
        if X[i]>.1:X[i]=0.0
    return X

def genData(n,m,k):
    cent = []
    data = []
    cent = rand(k,m)
    for i in xrange(n):
        data.append(randn(m)*.1+cent[i%k])
    #sparsify
    for i in xrange(n):
        r = roundspecial(rand(m))
        data[i]*=r
    return array(data)
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
        if t.query(A[i],5)[1] == i:ct+=1
        #if nn(A[i],B) == i:ct+=1
    return ct

def run(n,m,k,l):
    data = genData(n,m,k)
    R = randn(m,l)
    pData = dot(data,R)
    invR = linalg.pinv(R)
    #invR = randn(l,m)
    pinvData = dot(pData,invR) 
    return countInt(pinvData,data)

print "k=10,l=24,n=5000, avgs=10"
for i in range(70,100):
    print str(i+24),
    for j in range(10):
        print run(5000,i+24,10,24)/5000.,
    print ""



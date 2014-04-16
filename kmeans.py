from random import shuffle,randint,random ,gauss
from copy import deepcopy

def divide(X,Y,split):
    '''
        Divide a dataset into training and testing under @split ratio
        @return: trainX,trainY,testX,testY
    '''
    n = len(X)
    mix=range(0,n)
    shuffle(mix)
      
    size = int(split*n)
      
    trainX = [0]*size
    trainY = [0]*size
    testX =  [0]*(n-size)
    testY =  [0]*(n-size)
      
    for i in range(0,size):
        trainX[i] = X[mix[i]]
        trainY[i] = Y[mix[i]]
      
    for i in range(size,n):
        testX[i-size] = X[mix[i]]
        testY[i-size] = Y[mix[i]]
    return [trainX,trainY,testX,testY]


CLUSTER_DISCREPANCY = 1.0 # makes visually acceptable clusters

def getDataPoints(part,d,clu):
    ret = [[] for j in xrange(part*clu)]
    clusterCenters = []
    #simulate different descriptor weightings
    varsDim = [random()*CLUSTER_DISCREPANCY for i in range(d)]
    for i in range(clu):
        #variance =CLUSTER_DISCREPANCY *(d**.5)
        means = [(random()*2.)-1.0 for b in range(d)] 
        clusterCenters.append(means)
        
        for j in range(part):
            p = [0]*d
            for k in xrange(d):
                p[k]= (gauss(0,varsDim[k])+means[k])
            ret[i*part+j]=p
    #from mean_shift import drawPts
    #drawPts(clusterCenters,ret)
    
    
    return ret,clusterCenters 


def dist(X,Y):
    '''
        Euclidean for now
    '''
    d = 0.0
    for i in xrange(len(X)):
        d=d+(X[i]-Y[i])*(X[i]-Y[i])
    return d
    
def least(q,D,fnc=dist):
    '''
        Find the index of the element in dataset @D that minimizes the function
        @fnc on query  @q
        Return the ArgLeast(fnc(q,D))
    '''
    l = fnc(q,D[0])
    lp = 0

    for i in xrange(1,len(D)):
        tmp=fnc(q,D[i])
        if tmp<l:
            lp = i
            l = tmp
    return lp
    


def assignClusters(A,means,clusters):
    '''
        assign to clusters
    '''
    swaps = 0
    newclusters = [list() for i in xrange(len(means))]
    for i in xrange(len(clusters)):
        for j in xrange(len(clusters[i])):
            arglst = least(A[clusters[i][j]],means)
            newclusters[arglst].append(clusters[i][j])
            swaps += int(arglst != i)
    return newclusters,swaps


def kmeansUpdate(A,clusters,dim):
    '''
        update means
    '''
    means  = []
    for cluster in clusters:
        mean=[0.0 for k in xrange(dim)]
        l = len(cluster)
        if l ==0:l=1
        for point in cluster:
            for d in xrange(dim):
                mean[d] = mean[d]+A[point][d] 
        for d in xrange(dim):mean[d] = mean[d]/float(l)
        means.append(mean)
    return means

def kmeans(A,k,dim,maxiters = 1000):
    #some data storage structures
    R = range(len(A))
    shuffle(R)
    clusters = []
    
    part = len(R)/k
    for i in xrange(k):
        clusters.append( R[i*part:(i+1)*(part)])
    means = kmeansUpdate(A,clusters,dim)
    clusters,swaps = assignClusters(A,means,clusters)
    while swaps>2 and maxiters>0:
        maxiters-=1
        means = kmeansUpdate(A,clusters,dim)
        clusters,swaps = assignClusters(A,means,clusters)
        #print "swaps = ",swaps
    return means, clusters



    


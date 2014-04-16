from kmeans import *
from random import shuffle,randint,random ,gauss
from copy import deepcopy
    
def classifier(centroids,data):
    '''
        Apply nearest centroid classifier to data
        returns nearest centroid idx and distance
    '''
    ret = [0]*len(data)
    retvals = [0.0]*len(data)
    for i in xrange(len(data)):
        mindist = dist(data[i],centroids[0])
        argmindist = 0
        for j in xrange(1,len(centroids)):
            if dist(data[i],centroids[j]) < mindist:
                mindist = dist(data[i],centroids[j])
                argmindist = j
        ret[i] = argmindist
        retvals[i] = mindist
    return ret,retvals



def findlabels(Y,tildaY,k,labels):
    '''
        find which cluster has the most of a particular label
        return a mapping from cluster id->label
    '''
    counts = [ [0]*labels for i in range(k)]
    ret = {}
    for i in xrange(len(Y)):
        counts[tildaY[i]][Y[i]]+=1
    for i in xrange(k):
        clu = counts[i]
        mxlbl = clu[0]
        argmx = 0
        for ct in xrange(1,labels):
            if clu[ct]>mxlbl:
                mxlbl=clu[ct]
                argmx = ct
        ret[i] = argmx
   # print counts
    return ret
  
 
def getLabelAccuracy(means,testX,testY):
    '''
    Compute the Precision Recall For labeled data
    '''
    ret,retvals = classifier(means,testX)
    labelMap = findlabels(testY,ret,k,k)
    #print labelMap
    ctequal = 0
    #accumulate correct matches
    for i in range(len(testY)):
        ctequal += (testY[i]==labelMap[ret[i]])
    #print "Classification Accuracy:",
    return float(ctequal)/float(len(testY))
            
            
if __name__ == '__main__':
    pass
    #from numpy import array
    import sys
    k = 10
    dim = 100
    part = 500
    if len(sys.argv)>1:dim = int(sys.argv[1])
    if len(sys.argv)>2:part = int(sys.argv[2])
    if len(sys.argv)>3:k  = int(sys.argv[3])

    
    from pyIOUtils import *
    import os
    
    print "running kmeans on: ",
    print part*k,dim,k
    
    rphashPR = []
    kmeansPR = []
    dimlist = []

    import time
    av = 10
    h=dim
    for part in xrange(100,5000,500):
        
        
        rpAvg = []
        kmAvg = []
        for j in xrange(av):
            X , cntrs = getDataPoints(part,h,k)
            Y = [i/part for i in range(part*k) ]
        
            [trainX,trainY,testX,testY]=divide(X,Y,.50)
        
            #rphash
            writeMatFile(trainX, "X.mat")
            #start = time.time()
            #sys.exit(0);
            os.system("./a.out X.mat " + str(k))
            #rpAvg.append(time.time() - start)
            means = readMatFile("out.mat")
            rpAvg.append(getLabelAccuracy(means,testX,testY))
            
            '''
            start = time.time()

            #standard kmeans
            means,clusters = kmeans(trainX,k,h)

            print time.time()-start

            kmAvg.append(getLabelAccuracy(means,testX,testY))
            '''
            del(X,Y,trainX,trainY,testX,testY)
            
        
        rphashPR.append(sum(rpAvg)/float(av))
        kmeansPR.append(sum(kmAvg)/float(av))
        dimlist.append(h)
        
        mrp = sum(rpAvg)/float(av)
        mkm = sum(kmAvg)/float(av)
        print ""
        print part*k,mrp ,mkm,sum([x*x for x in rpAvg] )/av-mrp*mrp, sum([x*x for x in kmAvg] )/av-mkm*mkm
    
    print dimlist
    print rphashPR
    print kmeansPR
        
    #assign centroid labels based on max labels from the training set
   
        



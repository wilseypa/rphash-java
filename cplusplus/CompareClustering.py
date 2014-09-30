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


def getLabelAccuracy(means,testX,testY,noiseLabels):
    '''
    Compute the Precision Recall For labeled data
    '''
    ret,retvals = classifier(means,testX)
    k = len(means)
    labelMap = findlabels(testY,ret,k,k)
    #print labelMap
    ctequal = 0
    ct = 0
    #accumulate correct matches
    for i in range(len(testY)):
        if not noiseLabels.__contains__(testY[i]):#only count non-noise points
            ctequal += (testY[i]==labelMap[ret[i]])
            ct+=1
    #print "Classification Accuracy:",
    return float(ctequal)/float(ct)


if __name__ == '__main__':
    pass
    #from numpy import array
    import sys
    k = 10
    dim = 5000
    part = 10000
    if len(sys.argv)>1:dim = int(sys.argv[1])
    if len(sys.argv)>2:part = int(sys.argv[2])
    if len(sys.argv)>3:k  = int(sys.argv[3])


    from pyIOUtils import *
    import os
    noise =0#1 # add uniform random noise
    k+=noise
    if len(sys.argv) > 4:
        print "output Mat and exit mode: ",
        print part*k,dim,k
        X , cntrs = getDataPoints(part,dim,k,amtNoise = noise)
        Y = [i/part for i in range(part*(k)) ]
        [trainX,trainY,testX,testY]=divide(X,Y,.50)
        #rphash
        writeMatFile(trainX, "X.mat")
        writeMatFile(cntrs, "centers.mat")
        colorlst = ['r','g','b','y','k']
        '''
        from pylab import *
        for i in range(k):
            print k,colorlst[i]
            plot(cntrs[i][0],cntrs[i][1],'o'+colorlst[i])
            for j in range(part):
                plot(X[i*part+j][0],X[i*part+j][1],'x'+colorlst[i])
        show()
        '''

        sys.exit()


    print "running clustering on: ",
    print part,dim,k

    rp2AllhashPR = []
    rp2hashPR = []
    rp1hashPR = []


    kmeansPR = []
    dimlist = []

    import time
    av = 10
    h=dim
    for part in xrange(100,5000,250):


        rp2AllAvg = []
        rp2Avg = []
        rp1Avg = []
        kmAvg = []
        for j in xrange(av):
            X , cntrs = getDataPoints(part,h,k,amtNoise = noise)
            Y = [i/part for i in range(part*(k)) ]
            [trainX,trainY,testX,testY]=divide(X,Y,.50)

            #rphash
            writeMatFile(trainX, "X.mat")
            #start = time.time()
            #sys.exit(0);
            #os.system("./rp2All.out X.mat " + str(k))
            #means = readMatFile("out.mat")
            #rp2AllAvg.append(getLabelAccuracy(means,testX,testY,[k]))


            os.system("./a.out X.mat " + str(k))
            means = readMatFile("out.mat")
            rp2Avg.append(getLabelAccuracy(means,testX,testY,[k]))


            os.system("./rp1.out X.mat " + str(k))
            means = readMatFile("out.mat")
            rp1Avg.append(getLabelAccuracy(means,testX,testY,[k]))


            start = time.time()

            #standard kmeans
            means,clusters = kmeans(trainX,k,h)

            print time.time()-start

            kmAvg.append(getLabelAccuracy(means,testX,testY,[k]))

            del(X,Y,trainX,trainY,testX,testY)

        rp2AllhashPR.append(sum(rp2AllAvg)/float(av))
        rp2hashPR.append(sum(rp2AllAvg)/float(av))
        rp1hashPR.append(sum(rp2AllAvg)/float(av))
        kmeansPR.append(sum(kmAvg)/float(av))
        dimlist.append(h)

        mrp2All = sum(rp2AllAvg)/float(av)
        mrp2 = sum(rp2Avg)/float(av)
        mrp1 = sum(rp1Avg)/float(av)
        mkm = sum(kmAvg)/float(av)

        print ""


        print part*k,mrp2All,mrp2 ,mrp1, mkm#mkm,sum([x*x for x in rpAvg] )/av-mrp*mrp, sum([x*x for x in kmAvg] )/av-mkm*mkm

    print dimlist
    #print rphashPR
    #print kmeansPR

    #assign centroid labels based on max labels from the training set





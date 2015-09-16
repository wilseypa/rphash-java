from pylab import *
from pyIOUtils import *
from kmeans import *

data = array(readMatFile("alldata.mat"))
allsmallsets = set()
l = 256
for i in range(20):
    members = kmeans(data,2,l)[1]
    smallset = members[1] 
    if len(members[0])<len(members[1]):
        smallset = members[0]
    for j in smallset:
        allsmallsets.add(j)
print allsmallsets

m=256
for l in range(1,256,3):
    allsmallsetscomp = set()
    avg = 0.0
    for t in range(20):
        R = randn(m,l)
        pdata = dot(data,R)*(1./float(m))**.5
        members = kmeans(pdata,2,l)[1]
        smallset = members[1] 
        if len(members[0])<len(members[1]):
            smallset = members[0]
        for j in smallset:
            allsmallsetscomp.add(j)
        uall = float(len(allsmallsetscomp.union(allsmallsets)))
        iall = float(len(allsmallsetscomp.intersection(allsmallsets)))
        avg+=iall/uall
    print l,avg/20.

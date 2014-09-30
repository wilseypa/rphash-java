from pylab import *
from time import time

centroids = 5
dim = 24
num = 10000
lines = 10

cents = randn(centroids,dim)
data = []
for c in cents:
    data.append(c)
for i in range(num):
    data.append(cents[i%centroids]+randn(len(cents[0]))*(1/(24)**.5))
f = file("data.dat",'wb')
for p in range(lines):
    f.write(str(centroids)+" "+str(num/lines)+" "+str(dim)+" "+str(int(time())))
    for b in range(num/lines):
        for m in range(dim):
            f.write(" "+str(round(data[b+p*(num/lines)][m],7)))
    f.write("\n")

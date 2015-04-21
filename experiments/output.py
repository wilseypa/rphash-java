
from pylab import var, plot, show
import pyIOUtils
from math import *

a = pyIOUtils.readMatFile("outx.mat")
from sklearn.preprocessing import normalize
aa = normalize(a)
pyIOUtils.writeMatFile(aa,"scaledX.mat")
aa = normalize(a,axis=0)
pyIOUtils.writeMatFile(aa,"scaledX.mat")
Agglom = pyIOUtils.readMatFile("outputfile.mat.aggl")
Simple = pyIOUtils.readMatFile("outputfile.mat.smpl")

y = []
x = []
for i in range(len(aa[0])):
    for j in range(0,len(aa),2):
		y.append(aa[j+1][i])
for i in range(len(aa[0])):
	for j in range(0,len(aa),2):
		x.append(aa[j][i])

pylab.plot(x,y,'.')

a = .25
b = -.25
c = .75
d = -.75
pylab.plot([a,a,a,a,b,b,b,b,c,c,c,c,d,d,d,d],[a,b,c,d,a,b,c,d,a,b,c,d,a,b,c,d],'*')
pylab.plot(x,y,'.')
show()


aggl = pyIOUtils.readMatFile("outputfile.mat.aggl")
mrrp = pyIOUtils.readMatFile("outputfile.mat.mprp")
for i in range(len(aggl)):
    pylab.subplot(4,4,i)
    x1 = mrrp[i]
    x2 = aggl[3]
    pylab.title(str(sum([(x1[j]-x2[j])**2 for j in range(len(x2))])))
    pylab.plot(x1)
    pylab.plot(x2)
show()
for i in range(len(aggl)):
    minst,midx = 100,0
    for k in range(len(aggl)):
        dis = sum([(aggl[i][j]-mrrp[k][j])**2 for j in range(len(x2))])
        if dis < minst:
            minst = dis
            midx=i
    print midx


def invBoxMuller(x,mu,sigma):
	y = (x-mu)/sigma
	return erfc(-y/sqrt(2.))-1.


mx = mean(x)
sx = sqrt(var(x))
my = mean(x)
sy = sqrt(var(x))

ux = [invBoxMuller(xx,mx,sx) for xx in x]
uy = [invBoxMuller(yy,my,sy) for yy in y]
plot(ux,uy,'.')
show()
erfc?


from numpy import dot,array
from numpy.random import randn
from random import randrange
def genRandom(n,t):
    scaler = (3.0/t)**.5
    m = [ [0.0]*n for i in range(t)]
    for i in range(t):
        for j in range(n):
            r = randrange(0,6)
            if r==0:m[i][j] = scaler
            if r==1:m[i][j] = -scaler
    return array(m)

def project(x,M):
   return dot(M,x)

def dist(x,y):
	return sum([(x[i]-y[i])*(x[i]-y[i]) for i in range(len(x))])**.5





n = 5000
t = 100
trials = 10

M = genRandom(n,t)

sums = 0.0
for i in range(trials):
	v = randn(n)
	mv = project(v,M)

	u = randn(n)
	mu = project(u,M)

	dp = dist(mu,mv)
	df = dist(u,v)
	sums+= (  (1.0-dp/df) * (1.0-dp/df )  )**.5
	print (  (1.0-dp/df) * (1.0-dp/df )  )**.5,
print ""
print "average distance variation: " + str(sums/float(trials))

from math import sqrt 

def gramm(X):
    return X
    V = X
    k = len(X[0])          # number of columns. 
    n = len(X)             # number of rows.
 
    for j in range(k):
       for i in range(j):
          D = sum([V[p][i]*V[p][j] for p in range(n)])
          for p in range(n): 
            V[p][j] -= (D * V[p][i])
       # Normalize column V[j]
       invnorm = 1.0 / sqrt(sum([(V[p][j])**2 for p in range(n)]))
       for p in range(n):
           V[p][j] *= invnorm
    return V
    
def distAll(i,D):                                         
    ret = [0]*len(D)
    q = D[i]
    #reuse i
    for i in xrange(len(D)):
        ret[i] = sum([(q[j]-D[i][j])**2 for j in xrange(len(q))])
    return ret   

    
def computeMoment(i,D):
    q = D[i]
    n = len(D)
    d = len(q)
    
    shift = [0.0]*d
    
    for i in xrange(n):
        dist = (sum([(q[j]-D[i][j])**2 for j in xrange(len(q))])**.5)
        if dist !=0.0:
            for j in xrange(d):
                shift[j] += ( (dist))
        
    return [exp(-s) for s in shift ]






def shift(D):
    n = len(D)
    d = len(D[0])
    
    M = [[0.0]*d for i in xrange(n)] 
    
    #compute moments
    for i in xrange(n):
        M[i] = computeMoment(i,D)
        
    #apply moments
    for i in xrange(len(D)):
        D[i]=D[i]*M[i]
    return D
    
    
from numpy import *

def pr(n,m): return sum(eye(m) - dot(random.randn(m,n),random.randn(n,m))/float(m*m))
'''

avgs = []
for i in range(100):
    avgs.append(sum([abs(pr(10000,i))/100 for j in range(100) ]))

#(s*orth(R)'+a)*orth(R) = s+(a*orth(R))
def check(a,s,P):
    P = array(gramm(P))
    a = array(a)
    s = array(s)
    return abs(sum((dot(dot(s,P.T) + a,P)) - (s+dot(a,P))))#<.00000001
'''
def generatePts(n,l):
     a = random.randn(1,n)
     s = random.randn(1,l)
     P = random.randn(n,l)
     return check(a,s,P)



def randp(m,n,fnc, params):
    return array([[ fnc(params) for j in xrange(m)] for i in xrange(n)])

n=50
d = 4

import pylab
'''
L = []
for i in range(10)  :  
    r =  random.randint(2,100)
    l =  random.randint(1,r)
    L.append(generatePts(r,l))
print sum(L)/float(len(L))



clusters = 5
AllRPts = []
for i in range(clusters):
    clPts = random.randn(n,d)
    variance = ([random.random()]*d)
    means = random.randn(d)*([4]*d)
    AllRPts.extend( clPts*variance+means)

D = array(AllRPts)

steps = len(D)/clusters
for j in range(6):
    print j

    pylab.subplot(3,3,j+1)
    
    for i in range(clusters):
        pylab.plot(D[i*steps:(i+1)*steps,0],D[i*steps:(i+1)*steps,1],'o')
    
    
    shift(D)
    
pylab.show()
'''

import brewer2mpl
from mpl_toolkits.mplot3d import Axes3D


def display_graph(fileName) :
	pylab.savefig(fileName, bbox_inches='tight')
	#plt.show()


#fig = pylab.figure(1)
#ax = Axes3D(fig)
bmap = brewer2mpl.get_map('Set1', 'qualitative', 4)

colors = bmap.mpl_colors

markers=["x", "o","s","^"]
frame = pylab.gca()
#generate some random pts
for i in range(4):
    rPts = random.randn(n,d)
    variance = ([.3])
    means = random.randn(d)*([1]*d)
    rPts = rPts*variance+means
    
    #ax.scatter(rPts[:,0],rPts[:,1],rPts[:,2],marker = markers[i],color=colors[i])
    fig = pylab.figure(2)
    fig.subplots_adjust(left=None, bottom=None, right=None, top=None, wspace=.02, hspace=.02)
    for j in range(9):
        frame = pylab.gca()
        frame.axes.get_xaxis().set_ticks([])
        frame.axes.get_yaxis().set_ticks([])
        prs = random.randn(d,2)#/array([[1./2. , 1./2. ] for i in range(d)])
        pylab.subplot(3,3,j+1)
        K = gramm(dot(rPts,prs))
        #print var(K[:,1]) 
        pylab.scatter(K[:,0],K[:,1],marker = markers[i],color=colors[i])
    

#pylab.show()
display_graph("multiproj.pdf")
#test is orth hurts ditribution
#for i in range(10):
#    K = gramm(random.randn(200,2))
#    pylab.plot(K[:,0],K[:,1],'o')




def online_freq(X,k,theta = .49):
    K = {}
    theta = 1.0/theta
    for i in xrange(len(X)):
        if K.has_key(X[i]):
            K[X[i]]=K[X[i]]+1
        else:
            K[X[i]]=1
        if len(K) > theta:
            for a in K.keys():
                K[a]= K[a]-1
                if K[a] == 0: del(K[a])
    return K

def exhaustive_count(X):
    G = {}
    for i in X:
        if G.has_key(i): 
            G[i]=G[i]+1
        else : 
            G[i]=1
    return G

def freq_items(G,X,k):
    ret = []
    for g in G:
        ret.append((X.count(g),g))
    ret.sort(reverse=True)
    ret = [(r[1],r[0]) for r in ret]
    return ret[:k]



def findmisses(X,Y):
    ct = 0
    for i in X:
        for j in Y:
            if j[0]==i[0]:ct+=1

    return len(X)-ct

from random import gammavariate,gauss,shuffle,random
tests = 2

from scipy.optimize import curve_fit
from pylab import plot,var,axes,show,errorbar,polyval,log

def runtest(ax,tK=10,objs=10000):
    ctRange = [tK,130]
    g = []
    #errs = []
    data = sorted([random() for i in range(objs)])
    #data = sorted([gammavariate(1.1,3.8) for i in range(objs)])
    for i in range(ctRange[0],ctRange[1]):
        x = [int(xx*(objs/10.0)) for xx in data]
        #print "Exhaustive List: "
        freq_e = exhaustive_count(x)
        X = freq_items(freq_e,x,tK)
        #print "Karp Max Sets: "
        freq_k = online_freq(x,i)
        Y =  freq_items(freq_k,x,tK)
        misses = float(findmisses(X,Y))/float(tests+1)
        g.append(misses/tK)
        #errs.append([misses])

    for t in range(tests):
        data = sorted([gauss(0.0,1.0) for i in range(objs)])
        #data = sorted([gammavariate(1.1,3.8) for j in range(1000)])
        for i in range(ctRange[0],ctRange[1]):
            x = [int(xx*100) for xx in data]
            #print "Exhaustive List: "
            freq_e = exhaustive_count(x)
            X = freq_items(freq_e,x,tK)
            #print "Karp Max Sets: "
            freq_k = online_freq(x,i)
            Y =  freq_items(freq_k,x,tK)
            misses = float(findmisses(X,Y))/float(tests+1)
            g[i-tK]+=misses/tK
            #errs[i-5].append(misses)

    #varying nodes
    #def logDistFunc(x, a, b):
    #     return a*log(x) +b
    #errs = map(var,errs)

    x = range(ctRange[0],ctRange[1])
    #popt, pcov = curve_fit(logDistFunc, x,g)
    #fits = polyval(popt,x)
    #print fits
    ax.plot(x,g,label="Karp FI: k="+str(tK))
    
    #ax.plot(x,fits)


ax = axes()
for i in range(1,4):runtest(ax,i*4,)
ax.set_title("Karp Frequent Itemset Item Miss Rate")
ax.set_xlabel("Memory Usage")
ax.set_ylabel("Missed Frequent Items")
ax.legend(loc="lower left")
#ax.errorbar(x, g, yerr=errs, fmt='-',label="Karp FI")
#from xkcdify import *
#XKCDify(ax)
show()




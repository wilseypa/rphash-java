from pylab import *


def fitAndPlot(x,y,title, degree = 1 , color='r'):
    rm = polyfit(x,y,degree)
    rpyp = polyval(rm,x)
    plot(x, y,color+'o',label=title)
    ttlAndEqn = title +":y="+str(rm)[1:6]#+" + "+str(rm)[1:6]+"x"
    degree-=1
    while degree>0:
        ttlAndEqn = ttlAndEqn + ""
    plot(x, rpyp, color+'--',label=ttlAndEqn)

f = open("results","r")
rp2AllTimes = []
rp2Times = []
rp1Times = []
kmTimes = []

rp2AllPR = []
rp2PR = []
rp1PR = []
kmPR = []

s = f.readline()
print s
s = f.readline()
print s
s = f.readline()
ct = 0
while not s == "":
    if ct<10:
        t = s.split(" ")
        rp2AllTimes.append(float(t[0]))
        rp2Times.append(float(t[1]))
        rp1Times.append(float(t[2]))
        kmTimes.append(float(t[3]))
        ct += 1
    else:
        t = s.split(" ")
        rp2AllPR.append(float(t[1]))
        rp2PR.append(float(t[2]))
        rp1PR.append(float(t[3]))
        kmPR.append(float(t[4]))
        ct = 0
    s = f.readline()

print sum(rp2AllPR),sum(rp2PR),sum(rp1PR),sum(kmPR)
x = [i for i in range(len(rp2AllPR))]
fitAndPlot(x,rp2AllPR,title="RP2 All",degree = 0 , color = 'r')
fitAndPlot(x,rp2PR,title="RP2 2K",degree = 0, color = 'g')
fitAndPlot(x,rp1PR,title="RP1",degree = 0, color = 'b')
fitAndPlot(x,kmPR,title="KM",degree = 0, color = 'y')
#plot(rp2AllPR,label="RP2 All")
#plot(rp2PR,label="RP2 2K")
#plot(rp1PR,label="RP1")
#plot(kmPR,label="KM")
legend(loc="lower right")

figure()

times = []
for i in range(20):times.extend([i]*10)
x = times
fitAndPlot(x,rp2AllTimes,title="RP2 All", color = 'r')
fitAndPlot(x,rp2Times,title="RP2 2K", color = 'g')
fitAndPlot(x,rp1Times,title="RP1", color = 'b')
fitAndPlot(x,kmTimes,title="KM", color = 'y')


#plot(times, rp2AllTimes,'o',label="RP2 All")
#plot(times, rp2Times,'o',label="RP2 2K")
#plot(times, rp1Times,'o',label="RP1")
#plot(times, kmTimes,'o',label="KM")


#(rm,rb) = pylab.polyfit(x,rx,1)
#(km,kb) = pylab.polyfit(x,kx,1)

#rp1yp = pylab.polyval([rm,rb],x)
#kmyp = pylab.polyval([km,kb],x)

#avgrx = [sum(rx)/len(rx)]*len(x)
#avgkx = [sum(kx)/len(kx)]*len(x)


# intersection at x=34.21399 y=0.3473621
#pylab.plot(x, rpyp, 'b--',label="RP Hash Mean: y = "+str(rb)[1:6]+" - "+str(rm)[2:7]+"x")
#pylab.plot(x, kmyp,'g--',label="K-Means Mean: y = "+str(kb)[1:6]+" - "+str(km)[2:7]+"x")
#pylab.xlabel('Number of Clusters',size=30)
#pylab.ylabel('PR Performance',size=30)


legend(loc="upper left")
show()
    

    

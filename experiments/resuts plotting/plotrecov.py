from pylab import *
from matplotlib import rc
import seaborn as sns
def fitAndPlot(x,y,title, degree = 1 , color='r'):
    rm = polyfit(x,y,degree)
    rpyp = polyval(rm,x)
    plot(x, y,color+'o',label=title)
    ttlAndEqn = title +":y="+str(rm)[1:6]#+" + "+str(rm)[1:6]+"x"
    degree-=1
    #while degree>0:
    #    ttlAndEqn = ttlAndEqn + ""
    plot(x, rpyp, color+'--',label=ttlAndEqn)




## for Palatino and other serif fonts use:
#rc('font',**{'family':'serif','serif':['Palatino']})
rc('text', usetex=True)


f = file("k10l24n5000.csv",'r')
avgs = []
variance = []
ct = []#array(range(24,118))
s = f.readline()
s = f.readline()
allpts = []
while(not s==""):
    ct.append(float(s.split(',')[0]))
    allpts.append([float(n) for n in s.split(',')[1:-2]])
    variance.append(float(s.split(',')[-1]))
    #avgs.append(float(s.split('\t')[-2]))
    s = f.readline()
avgs = [sum(alp)/10.0 for alp in allpts]
from scipy import optimize
#powerlaw = lambda x, amp, index: amp * (x**index)
fitfunc = lambda p, x: p[0] + p[1]/(x**p[2]+x**p[3]) 
#fitfunc = lambda p, x:  p[2]/x + p[3]/x**2 + p[1]/x**3+ p[0]/x**4
errfunc = lambda p, x, y, err: (y - fitfunc(p, x)) / err

allpt = array(allpts[:55])
yerr = array(variance[:55])
ydata = array(avgs[:55])
xdata = array(ct[:55])
logy = ydata#log10(ydata)
logx = xdata#log10(xdata)
yerr = yerr / ydata
rc('text', usetex=True)
pinit = [1.,   1.0,  1.2,1.0,1.0,1.0]
out = optimize.leastsq(errfunc, pinit,args=(logx, logy, yerr), full_output=1)
pfinal = out[0]
print pfinal
index = pfinal[1]

#sns.regplot(xdata,fitfunc(out[0],xdata),'g-',label="power law fit")

#for i in range(60):
#     print variance(ct[i])
#    plot([ct[i] for j in range(10)],allpts[i],'gx')
xlabel("Difference in number of Dimensions (d)",fontsize=16)
ylabel("$P(NN((x \cdot R)\cdot \hat{R}^{-1})=x)$",fontsize=16)
title("Probability of Re-associating a \n Projected Vector",fontsize=16)
fitAndPlot(array(ct),array(avgs),"avgs",4)
#errorbar(xdata, ydata, yerr=(yerr), fmt='g.',label="avg w/ err (10 runs)")
#grid()
#legend()
show()


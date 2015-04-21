from PlotUtils import *
import pandas as pd
data = pd.read_csv('resultsdataVar.csv')
xaxis = 'var'
columns = ['KMeans Time','SimpleTime','mproj rand Time','mproj avg Time']
labels = ['RP-KMeans','Streaming RPHash','Multi RPHash(rep)','Multi RPHash(avg)']
ytitle = "Time ($s$)"
xtitle = "$\sigma^2$"
title = "Processing Time For Varying Cluster Variance"
filename = "varVarTime.pdf"
printincolors(data,xaxis,columns,labels,xtitle,ytitle,title,filename,1)

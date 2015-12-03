from PlotUtils import *
import pandas as pd
data = pd.read_csv('resultsdataVar.csv')
xaxis = 'var'
columns = ['KMeans','Simple','mproj rand','mproj avg']
labels = ['RP-KMeans','Streaming RPHash','Multi RPHash(rep)','Multi RPHash(avg)']
ytitle = "Precision Recall"
xtitle = "$\sigma^2$"
title = "Precision Recall on Varying Cluster Variance"
filename = "varVar.pdf"
printincolors(data,xaxis,columns,labels,xtitle,ytitle,title,filename,0)

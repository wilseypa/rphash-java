from PlotUtils import *
import pandas as pd
data = pd.read_csv('resultsdatad.csv')
xaxis = 'd'
columns = ['KMeans','Simple','mproj rand','mproj avg']
labels = ['RP-KMeans','Streaming RPHash','Multi RPHash(rep)','Multi RPHash(avg)']
ytitle = "Precision Recall"
xtitle = "$d$"
title = "Precision Recall on Varying Embedding Dimension"
filename = "varD.pdf"
printincolors(data,xaxis,columns,labels,xtitle,ytitle,title,filename,0)

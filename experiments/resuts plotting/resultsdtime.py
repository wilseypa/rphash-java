from PlotUtils import *
import pandas as pd
data = pd.read_csv('resultsdatad.csv')
xaxis = 'd'
columns = ['KMeans Time','SimpleTime','mproj rand Time','mproj avg Time']
labels = ['RP-KMeans','Streaming RPHash','Multi RPHash(rep)','Multi RPHash(avg)']
ytitle = "Time ($s$)"
xtitle = "$\sigma^2$"
title = "Processing Time For Varying Embedding Dimension"
filename = "varDTime.pdf"
printincolors(data,xaxis,columns,labels,xtitle,ytitle,title,filename,1)

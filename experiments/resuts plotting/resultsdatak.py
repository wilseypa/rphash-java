from PlotUtils import *
import pandas as pd
data = pd.read_csv('resultsdatak.csv')
xaxis = 'k'
columns = ['KMeans','Simple','mproj rand','mproj avg']
labels = ['RP-KMeans','Streaming RPHash','Multi RPHash(rep)','Multi RPHash(avg)']
ytitle = "Precision Recall"
xtitle = "$k$"
title = "Precision Recall on Varying Number of Clusters"
filename = "varK.pdf"
printincolors(data,xaxis,columns,labels,xtitle,ytitle,title,filename,0)

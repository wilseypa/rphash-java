from PlotUtils import *
import pandas as pd
data = pd.read_csv('resultsdatan.csv')
xaxis = 'n'
columns = ['KMeans','Simple','mproj rand','mproj avg']
labels = ['RP-KMeans','Streaming RPHash','Multi RPHash(rep)','Multi RPHash(avg)']
ytitle = "Precision Recall"
xtitle = "$n$"
title = "Precision Recall on Varying Number of Vectors"
filename = "varN.pdf"
printincolors(data,xaxis,columns,labels,xtitle,ytitle,title,filename,0)

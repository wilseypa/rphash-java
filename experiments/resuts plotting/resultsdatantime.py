from PlotUtils import *
import pandas as pd
data = pd.read_csv('resultsdatan.csv')
xaxis = "n"
columns = ['KMeans Time','SimpleTime','mproj rand Time','mproj avg Time']
labels = ['RP-KMeans','Streaming RPHash','Multi RPHash(rep)','Multi RPHash(avg)']
ytitle = "Time ($s$)"
xtitle = "$n$"
title = "Processing Time For Varying Number of Vectors"
filename = "varNTime.pdf"
printincolors(data,xaxis,columns,labels,xtitle,ytitle,title,filename,1)

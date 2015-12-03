from PlotUtils import *
import pandas as pd
data = pd.read_csv('resultsdatak.csv')
xaxis = 'k'
columns = ['KMeans Time','SimpleTime','mproj rand Time','mproj avg Time']
labels = ['RP-KMeans','Streaming RPHash','Multi RPHash(rep)','Multi RPHash(avg)']
ytitle = "Time ($s$)"
xtitle = "$k$"
title = "Processing Time For Varying Number of Clusters"
filename = "varkTime.pdf"
printincolors(data,xaxis,columns,labels,xtitle,ytitle,title,filename,1)

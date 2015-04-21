from PlotUtils import *
import pandas as pd
import matplotlib.pyplot as plt
data = pd.read_csv('pkmeansdata.csv')
plt.xlim(0.0,2.1)
xaxis = 'Variance'
columns = ['Dim']
labels = ['']
ytitle = "RP Subspace Dimension"
xtitle = "Variance"
title = "RP Dimension vs 90% Precision Recall for Projected KMeans"
filename = "pkmeans.pdf"
printincolors(data,xaxis,columns,labels,xtitle,ytitle,title,filename,3)

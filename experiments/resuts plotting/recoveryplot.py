from PlotUtils import *
import pandas as pd
data = pd.read_csv('k10l24n5000.csv')
xaxis = 'dim'
columns = ['tavg']
labels = [' ']
ytitle = "$P(NN((x \cdot R)\cdot \hat{R}^{-1})=x)$"
xtitle = "Difference in number of Dimensions (d)"
title = "Probability of Re-associating a \n Projected Vector"
filename = "recovery.pdf"
printincolors(data,xaxis,columns,labels,xtitle,ytitle,title,filename,0)

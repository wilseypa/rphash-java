import numpy as np
import seaborn as sns
import matplotlib as mpl
import matplotlib.pyplot as plt
import brewer2mpl
linestyle = ['_', '-', '--', ':']
def display_graph(fileName) :
	plt.savefig(fileName, bbox_inches='tight')
	#plt.show()
markers=["x", "o","s","^"]
def printincolors(data,xaxis,columns,labels,xtitle,ytitle,title,filename,orderPoly):
	bmap = brewer2mpl.get_map('Set1', 'qualitative', 4)
	colors = bmap.mpl_colors
	for i in range(len(columns)):
		if orderPoly==0:
			sns.regplot(xaxis,columns[i],data,logistic=True,color=colors[i],label=labels[i],marker = markers[i])
		else:
			sns.regplot(xaxis,columns[i],data,order=orderPoly , color=colors[i],label=labels[i],marker = markers[i])
	plt.ylabel(ytitle,fontsize="large")
	plt.xlabel(xtitle,fontsize="large")
	plt.legend()
	plt.title(title,fontsize="large")
	display_graph(filename)


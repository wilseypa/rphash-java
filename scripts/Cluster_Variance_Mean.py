import sys
import csv
import numpy as np
from collections import defaultdict
import matplotlib.pyplot as plt

d = defaultdict(list)
values_unsorted = defaultdict(list)
var_sorted = defaultdict(list)

fileName = sys.argv[1]

with open(fileName, 'rb') as filein:  # Open the input file
    rows = csv.reader(filein, quoting=csv.QUOTE_NONNUMERIC)  # Retrieve rows from the input file
    for r in rows:
        d[r[-1]].append(r[:-1])  # Store the rows in a dictionary d, where clusters are keys and lists of vectors are values

    for k, v in d.items():  # Loop through each cluster (key) and list of vectors (value)
        for column in zip(*v):  # Convert lists of vectors to lists of columns and loop through each column
            variance = np.var(column)
            mean = np.average(column)
            values_unsorted[k].append((variance, mean))  # Store in a dictionary, where each cluster is a key and the list of (variance, mean) of each column is its value

colCount = len(values_unsorted[1])  # Get the number of columns (of any cluster)


# Sort the variance of each cluster. Store in a dictionary where each cluster is a key and the list [list of sorted variance, list of corresponding means] is its value
for k, v in values_unsorted.items():
    var_list = []
    mean_list = []
    for i in range(colCount):
        var_list.append(v[i][0])  # Extract variances (of each cluster) and store them in a list 'var_list'
    sorted_var_list = sorted(var_list, reverse=True)  # Sort 'var_list'
    mean_list_index = sorted(range(len(var_list)), key=lambda n: var_list[n], reverse=True)  # Retrieve indices of means correcsponding to sorted variances
    for j in mean_list_index:
        mean_list.append(v[j][1])  # Retrieve means (of each cluster), correcsponding to sorted variances
    var_sorted[k].append((sorted_var_list, mean_list))  # Store in a dictionary, where each cluster is a key and the list [list of sorted variance, list of corresponding means] is its value


# Initialise plotting parameters
ax1 = []
lbel1 = []
ax2 = []
lbel2 = []
lbel = []
lbels = []

# Loop through each cluster to plot its variance and mean
for cluster, values in var_sorted.items():
    i = int(cluster) - 1
    ax1.append(i)
    lbel1.append(i)
    ax2.append(i)
    lbel2.append(i)
    lbel.append(i)
    lbels.append(i)
    ax1[i] = plt.subplot(2, 2, i)
    plt.title('Variance and Mean of Cluster '+ str(i+1) + ' for %s' % fileName)

    # Plot the variance
    lbel1[i] = ax1[i].plot(values[0][0], 'bx', label='Variance')
    ax1[i].set_ylabel('Variance')

    # Plot the mean
    ax2[i] = ax1[i].twinx()
    ax2[i].set_ylabel('Mean')
    lbel2[i] = ax2[i].plot(values[0][1], 'rx', label='Mean')

    # Display legends
    lbel[i] = lbel1[i] + lbel2[i]
    lbels[i] = [l.get_label() for l in lbel[i]]
    plt.legend(lbel[i], lbels[i], loc='best')

plt.show()

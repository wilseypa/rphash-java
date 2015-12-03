import sys
import csv
import numpy as np
import matplotlib.pyplot as plt

y = []
fileName = sys.argv[1]

with open(fileName, 'rb') as filein:
    reader = csv.reader(filein, quoting=csv.QUOTE_NONNUMERIC)
    for column in zip(*reader):
        var = np.var(column)
        mean = np.average(column)
        y.append((var,mean))

sortedData = sorted(y, reverse=True)
vars = [sortedData[i][0] for i in range(len(sortedData))]
means = [sortedData[i][1] for i in range(len(sortedData))]

fig, ax1 = plt.subplots()
plt.title('Variance and Mean for DataSet %s' % fileName)

# plot the variance
lbel1 = ax1.plot(vars, 'bx', label='Variance')
ax1.set_ylabel('Variance')
# plot the mean
ax2=ax1.twinx()
ax2.set_ylabel('Mean')
lbel2 = ax2.plot(means, 'rx', label='Mean')
# display the legend
lbel = lbel1 + lbel2
lbels = [l.get_label() for l in lbel]
plt.legend(lbel, lbels, loc='best')
plt.show()

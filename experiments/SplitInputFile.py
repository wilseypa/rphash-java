import numpy as np
import sys

mainInputFile = sys.argv[1]
arr = np.loadtxt(open(mainInputFile, 'rb'))

horizon = input('Enter the stream duration: ')
numberOfHorizons = int(arr[0])//horizon
points = horizon * int(arr[1])
index = 2

for h in range(1, numberOfHorizons+1):
    with open('SplitInputFile ' + str(h) + '.txt', 'w') as splitFile:
        splitFile.write(str(horizon) + '\n' + str(int(arr[1])) + '\n')
        for i in range(points):
            splitFile.write(str(arr[index]) + '\n')
            index += 1

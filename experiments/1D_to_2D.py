import numpy as np

arr = np.loadtxt(open('1D.txt', 'rb'))
index = 2

fc = open('2D.csv', 'w')
for j in range(1, int(arr[1])+1):
    fc.write('Attr' + str(j) + ',')
fc.write('\n')

for i in range(1, int(arr[0])+1):
    for j in range(1, int(arr[1])+1):
        fc.write(str(int(arr[index])) + ',')
        index += 1
    fc.write('\n')

fc.close()

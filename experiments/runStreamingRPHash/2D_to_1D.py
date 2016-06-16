import numpy as np
import sys

inputFile = sys.argv[1]
arr = np.loadtxt(open(inputFile, 'rb'), dtype = str, delimiter =',')  # Converts the 2D csv file into an ndarray
arr_size = np.shape(arr)     # Computes the size of the array

# Open the 2D csv file, store its contents as a single string and then create the column for input to RPHash
fr = open(inputFile, 'r')
str_arr = fr.read()
oneD_col = str_arr.replace(',', '\n')
fr.close()

# Create the input file for RPHash
fc = open('1D.txt', 'w')
fc.write(str(arr_size[0]) + '\n' + str(arr_size[1]) + '\n')
fc.write(oneD_col)
fc.close()

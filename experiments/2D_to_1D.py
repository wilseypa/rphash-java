import numpy as np

arr = np.loadtxt(open('2D.csv', 'rb'), dtype = str, delimiter =',')  # Converts the 2D csv file into an ndarray
arr_size = np.shape(arr)     # Computes the size of the array

# Open the 2D csv file, store its contents as a single string and then create the column for input to RPHash
fr = open('2D.csv', 'r')
str_arr = fr.read()
oneD_col = str_arr.replace(',', '\n')

# Create the input file for RPHash
fc = open('1D.txt', 'w')
fc.write(str(arr_size[0]) + '\n' + str(arr_size[1]) + '\n')
fc.write(oneD_col)
fc.close()

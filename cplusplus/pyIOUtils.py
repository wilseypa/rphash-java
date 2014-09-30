'''
Created on Jan 9, 2014

@author: lee
'''

def readMatFile(name):
    f = file(name,"r")
    x = int(f.readline())
    y = int(f.readline())
    M = [[0.0]*y for a in xrange(x)]
    
    for i in xrange(len(M)):
        for j in xrange(len(M[i])):
            M[i][j] = float(f.readline())
    f.close()
    return M

def writeMatFile(M,name):
    f = file(name,"w")
    f.write(str(len(M))+'\n')
    f.write(str(len(M[0]))+'\n')
    for i in xrange(len(M)):
        for j in xrange(len(M[i])):
            f.write(str(M[i][j])+'\n')
    f.close()
    return True

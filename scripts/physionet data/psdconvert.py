from pylab import *
from glob import glob
import pandas as pd
from pyIOUtils import *

files = glob("samples/*.out")
alldigests = []
for f in files:
    print f+":"
    o = pd.read_csv(f)
    digest = []
    for col in ["'RESP'","'ABP'","'ECG II'","'PLETH'"]:
        print "\t"+col
        digest.extend( psd(o[col],1024,detrend=matplotlib.pylab.detrend_linear)[0][0:64])
    alldigests.append(digest)
writeMatFile(alldigests,"alldata.mat")

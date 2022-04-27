#import JythonTest3

import sys

class PyScript:
    def __init__(self,txt):
        city = []
        for i in range(0,len(sys.argv)):
            city.append(str(sys.argv[i]))
            print(city)
#        jObj = JavaProg()
#        jObj.getData(city)
        print("Done")
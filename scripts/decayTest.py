#import matplotlib.pyplot as plt
import numpy as np
import random 
import pylab

'''
So let's keep this simple.  We want to develop a lightweight aging computation for our
min-count sketch counters.  Effectively we want an IIR filter looking something like
  value = newValue*decayRate + value*(1-decayRate) where decayRate < 1.0


Here i will build two decay functions, the first, fullDecay,
will update the counts every update cycle.  the second, lightDecay will update counts only
with each increment to the counter.

the goal for the ligthDecay is to have the same value as the fullDecay at each important
measurement cycle (recorded by the variable measurementCycle.  we will plot the %error at
the end of each measurement cycle.  to test, we will setup an array of n counters and
randomly increment one of the counters each step of the measurement cycle.

'''
def checkOrder(A,B):
    '''
        check the order of the  sequences relative to the correct ordering A, and our approximate ordering B
    '''

    tA = []
    for ait in range(len(A)):
        tA.append((A[ait],ait))
    tB = []
    for bit in range(len(B)):
        tB.append((B[bit],bit))

    s = [l for (k,l) in sorted(tA)]
    t = [l for (k,l) in sorted(tB)]
    return int(s==t)




measurementCycle = 100
decayRate = .995
numCounters = 1
pointsper = 10

# because i don't know python, we'll do this the ugly way
decayArray=[decayRate  for i in range(measurementCycle)]
for i in reversed(range(measurementCycle-1)) :
    decayArray[i]=decayArray[i+1]*decayRate 

#print decayArray

# any initial values for the counters will work, using an enumerate for simplicity
countersFull = [[0.0 for i in range(measurementCycle*pointsper) ] for i in range(numCounters)]
countersLight = [1.0 for i in range(numCounters)]
percentError = [0.0 for i in range(numCounters)]

errorInOrder = [0.0]*(500*20)

#print countersFull
#print countersLight

#test for sets of the measurement cycle
for i in range(pointsper) :
    # age all the counters
    for k in range(numCounters) :
        countersLight[k] = countersLight[k]*decayArray[0]
    for j in range (1,measurementCycle) :
        # pick a counter to update
        updateCounter = random.randint(0,numCounters-1)
#        print 'Updating counter: ' + str(updateCounter)
        # age all full counters array
        for k in range(numCounters) :
            countersFull[k][i*measurementCycle+j]  = countersFull[k][i*measurementCycle+j-1]  * decayRate
        # add increment to specific counter
        
        countersFull[updateCounter][i*measurementCycle+j] = countersFull[updateCounter][i*measurementCycle+j-1] + (1 * decayRate)
        countersLight[updateCounter] = countersLight[updateCounter] + (1 * decayArray[j])
    
    #print 'Last counter touched: ' + str(updateCounter)


pylab.plot(countersFull[0])
pylab.show()

'''
    for i in range(numCounters) :
        percentError[i] = countersLight[i]/countersFull[i] 
    maxError = 0.0
    maxIndex = 0
    for i in range(numCounters) :
        if percentError[i] > maxError :
            maxError = percentError[i]
            maxIndex = i
    print 'Index of Max Error: ' + str(maxIndex) + ' Full Count: ' + str(countersFull[maxIndex]) + ' Light Count: ' + str(countersLight[maxIndex])
'''
print checkOrder(countersFull,countersLight)
    
#pylab.plot(percentError, label="Error")
#pylab.plot(countersFull, label='Full Count', marker='o', markevery=10)
#pylab.plot(countersLight, label='Light Count', marker='x', markevery=5)
#pylab.legend()
#pylab.show()
    
    

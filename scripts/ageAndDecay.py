import matplotlib.pyplot as plt
from numpy import array
'''
So we have a sparse array of mostly empty counters, and a few frequent 
counters for which we want to compute a time decaying population. 
Currently the idea is to regard the counters as super additive 
(in regard to when the next batch decay everything process is to take place). 
This is a pretty decent scheme, but the batch process is computationally
 prohibitive, and the accuracy isnt great either.
 
the basic idea is to decay every hash bucket of the min-count sketch
structure. Because this is computationally intensive, and highly redundant
for majority of empty min-count sketch cells, we instead suggest a
batch decay process, intersparsed with per step additive process.
because we are only interested in order, and not exact value, if we
can figure out a function that adds in such a way to maintain order
then we can claim success.Qualititatively we want to have something
like a pingponging between the exact
'''


from math import exp

def batchDecayCalc(prev,decayrate,mod):
    '''
        this function runs at each cluster interval as a batch 
        computation step, it is very computationally intensive
        use the actual decay function for all data
    '''
    
    return  prev * (1.0-decayrate)**mod #prev - decayrate*((mod-lastupdate)/mod)

def approxAgeCalc(prev,decayrate,mod,i):
    '''
        this command is run when a bucket happens to get hit with a 
        hash collision
        calculate the value within a batch, assuming some decay will
        occur are the end of this sequence.
    '''
    i = i%mod #we are considered with what i is within the range
    return prev + (1-(1-decayrate*mod)**(i/float(mod))) #*(1.0+decayrate)**((mod-i)/(mod))
#prev+(1+(decayrate*float(mod-i)))#prev*( (decayrate*float(mod-i))) #this should follow inverse exp curve

def updateDecayCalc(prev,decayrate):
    '''
        this is the standard memoryless decay and update function
        incrementally decay all data evenly. this function serves as our objective
        alternatively : return (prev - (prev*decayrate*(1.0/float(mod))))
    '''
    return prev * (1.0-decayrate)




def checkOrder(A,B,i):
    '''
        check the order of the  sequences relative to the correct ordering A, and our approximate ordering B
    '''
    tA = []
    for ait in range(len(A)):
        tA.append((A[ait][i],ait))
    tB = []
    for bit in range(len(B)):
        tB.append((B[bit][i],bit))

    s = [l for (k,l) in sorted(tA)]
    t = [l for (k,l) in sorted(tB)]
    return int(s==t)

def plotData(approxSequence,exactSequence,order,n):#approxAgeDecay,approxAgeDecayLower,approxAgeDecayUpper,exactDecay,exactLower,exactUpper,order,n):
    '''
        plot the approx decay graph, the exact decay graph.
        and the correct or not graph (0 incorrect, 1 correct), in a 
        multigraph window.
    '''
    #draw the graphs as 3 plots in the same figure
    plt.subplot(3, 1, 1)
    for seq in approxSequence:
        plt.plot(range(n),seq)
    plt.subplot(3, 1, 2)
    for seq in exactSequence:
        plt.plot(range(n),seq)
    plt.subplot(3, 1, 3)
    plt.plot(range(len(order)),order)
    plt.show()

def run(mod,plotdata=False):
    '''
        mod: the stream cluster interval
        n: number of data points to simulate
        arrivalrate: the speed at which we should generate hits to a bucket
        nseq: number of sequences to consider
        decayrate: the decay rate of a bucket
        Compute the decay rates and randomly add hash hits to the representative buckets
    '''
    n = 100000
    nseq = 4
    decayrate = .90/float(mod)
    arrivalrate = .01
    order = []#the order between the decays lists
    from random import random, randrange
    approxSequences = []
    exactSequences = []
    #lastupdateSequence = []
    for i in range(nseq):#generate our sequence arrays
        approxSequences.append([0.0]*n)
        exactSequences.append([0.0]*n)
        #lastupdateSequence.append([0]*n)

    for i in range(1,n):#iterate over the data
        
        for j in range(nseq):#iterate over the number of sequences to consider
            approxSequences[j][i] = approxSequences[j][i-1]#we have to propagate the count forward
            exactSequences[j][i] = updateDecayCalc(exactSequences[j][i-1],decayrate)#,mod)


            if i%mod==0:# this is when we are allowed to run our batch decay function
                approxSequences[j][i] = batchDecayCalc(approxSequences[j][i-1],decayrate,mod)
                #just check after the batch processes
                #order.append( checkOrder(approxSequences,exactSequences,i))
                

        if random() < arrivalrate:#randomly add some hits to the buckets
            r = randrange(0,nseq)
            #this is the only time we are allowed to run our aging calculation
            approxSequences[r][i]=approxAgeCalc(approxSequences[r][i-1],decayrate,mod,i)+1
            exactSequences[r][i]+=1
        order.append( checkOrder(approxSequences,exactSequences,i))


    #store correctness of order after each data point
    if plotdata: plotData(approxSequences,exactSequences,order,n)

    return sum(order)/float(len(order))


def runmany():
    '''
         update the batch decay range for i in 1:1000
         plot the results with the batch decay as the x axis, and
         accuracy as the y axis
    '''
    #uncomment below to generate a per interval profile of your function
    print "interval accuracy"
    p = []
    for i in range(1,500,2):
        p.append(run(i))
        print i,p[-1]
    plt.plot(range(1,500,1),p,'o')
    plt.show()

t=10000
print "we are correct about order: " + str(run(t,True)*100) +"% of the time with interval at: " +str(t)
runmany()


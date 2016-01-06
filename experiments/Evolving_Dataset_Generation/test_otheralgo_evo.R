library(stream)
library(clValid)
library(mclust)
library(fpc)
library(streamMOA)

d <- readline("Enter the number of dimensions: ")
d <- as.numeric(d)

## Start other algorithms
stream <- DSD_ReadCSV('Users/user/Documents/datatset200_1wl_scaled_30000pts.csv', class = d+1, sep=",", header=FALSE )

dstream <- DSC_DStream(gridsize=1, Cm=1.5)
#sample <- DSC_TwoStage(micro = DSC_Sample(k = 100, biased = TRUE), macro = DSC_DBSCAN(eps = .05))
#window <- DSC_TwoStage(micro = DSC_Window(horizon = 100, lambda = 0.01), macro = DSC_DBSCAN(eps = .05))
dbstream <- DSC_DBSTREAM(r=10, lambda=0.1)
denstream <- DSC_DenStream(epsilon = 0.9, lambda = 0.01, recluster = TRUE, k=NULL)
#cltree <- DSC_TwoStage(micro = DSC_ClusTree(maxHeight = 8), macro=DSC_Reachability(epsilon = .15))

#algorithms <- list(DStream = dstream, ReservoirSampling = sample, SlidingWindow = window ,DBStream = dbstream , DenStream = denstream, ClusTree = cltree)
algorithms <- list(DStream = dstream,DBStream = dbstream , DenStream = denstream)

n <- 30000   # Total number of points
horizon <- 1000   # Stream duration
reset_stream(stream)


evaluation <- lapply(algorithms, FUN = function(a) {
  reset_stream(stream)
  print(a)
  print(evaluate_cluster(a, stream, measure = c("numMicro", "numMacro", "cRand", "purity", "SSQ"), type = "macro", assign = "micro", n = n, horizon = horizon))
})

Position <- evaluation[[1]][,"points"]

sink(file = "C:/Users/user/Documents/out.txt")

ARI <- sapply(evaluation, FUN=function(x) x[,"cRand"])
print('ARI:', quote = FALSE)
print(ARI)

Purity <- sapply(evaluation, FUN=function(x) x[,"purity"])
print('Purity:', quote = FALSE)
print(Purity)

WCSSE <- sapply(evaluation, FUN=function(x) x[,"SSQ"])
print('WCSSE:', quote = FALSE)
print(WCSSE)

sink()
## End other algorithms
# To get the memory usage and runtime of four other stream clustering algorithms

library(stream)
library(streamMOA)

stream <- DSD_ReadCSV('dataset.csv', k = 6, d = 561, class = 562, sep = ",")

dstream <- DSC_TwoStage(micro = DSC_DStream(gridsize = 0.8, lambda = 0.01), macro = DSC_Kmeans(k = 6, weighted = TRUE, nstart = 25, algorithm = "Hartigan-Wong"))
dbstream <- DSC_TwoStage(micro = DSC_DBSTREAM(r = 0.6, lambda = 0.01), macro = DSC_Kmeans(k = 6, weighted = TRUE, nstart = 25, algorithm = "Hartigan-Wong"))
sample <- DSC_TwoStage(micro = DSC_Sample(k = 100, biased = TRUE), macro = DSC_Kmeans(k = 6, weighted = TRUE, nstart = 25, algorithm = "Hartigan-Wong"))
window <- DSC_TwoStage(micro = DSC_Window(horizon = 100, lambda = 0.01), macro = DSC_Kmeans(k = 6, weighted = TRUE, nstart = 25, algorithm = "Hartigan-Wong"))

algorithms <- list(DStream = dstream, DBStream = dbstream, ReservoirSampling = sample, SlidingWindow = window)

n <- readline("Enter the number of points to be extracted from the stream: ")
n <- as.numeric(n)
horizon <- 500
hn <- n/horizon

for(a in algorithms){
  print(a)
  reset_stream(stream)
  pos <- 1
  for(i in 1:hn){
    print(pos)
    reset_stream(stream, pos = pos)
    print(gc(reset = TRUE))
    print(system.time(update(a, stream, n = 500)))
    print(gc())
    pos <- pos+horizon
  }
}

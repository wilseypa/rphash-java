# To compare seven other stream clustering algorithms

library(stream)
library(streamMOA)

stream <- DSD_ReadCSV('dataset.csv', k = 6, d = 561, class = 562, sep = ",")

dstream <- DSC_TwoStage(micro = DSC_DStream(gridsize = 0.8, lambda = 0.01), macro = DSC_Kmeans(k = 6, weighted = TRUE, nstart = 25, algorithm = "Hartigan-Wong"))
dbstream <- DSC_TwoStage(micro = DSC_DBSTREAM(r = 0.6, lambda = 0.01), macro = DSC_Kmeans(k = 6, weighted = TRUE, nstart = 25, algorithm = "Hartigan-Wong"))
sample <- DSC_TwoStage(micro = DSC_Sample(k = 100, biased = TRUE), macro = DSC_Kmeans(k = 6, weighted = TRUE, nstart = 25, algorithm = "Hartigan-Wong"))
window <- DSC_TwoStage(micro = DSC_Window(horizon = 100, lambda = 0.01), macro = DSC_Kmeans(k = 6, weighted = TRUE, nstart = 25, algorithm = "Hartigan-Wong"))
clstream <- DSC_TwoStage(micro = DSC_CluStream(m = 100), macro = DSC_Kmeans(k = 6, weighted = TRUE, nstart = 25, algorithm = "Hartigan-Wong"))
cltree <- DSC_TwoStage(micro = DSC_ClusTree(maxHeight = 8), macro = DSC_Kmeans(k = 6, weighted = TRUE, nstart = 25, algorithm = "Hartigan-Wong"))
denstream <- DSC_TwoStage(micro = DSC_DenStream(epsilon = 0.7, lambda = 0.01), macro = DSC_Kmeans(k = 6, weighted = TRUE, nstart = 25, algorithm = "Hartigan-Wong"))

algorithms <- list(DStream = dstream, DBStream = dbstream, ReservoirSampling = sample, SlidingWindow = window, CluStream = clstream, ClusTree = cltree, DenStream = denstream)

n <- readline("Enter the number of points to be extracted from the stream: ")
n <- as.numeric(n)
horizon <- 250
reset_stream(stream)

evaluation <- lapply(algorithms, FUN = function(a) {
  reset_stream(stream)
  print(a)
  print(evaluate_cluster(a, stream, measure = c("numMicro", "numMacro", "cRand", "purity", "dunn", "silhouette", "SSQ", "vi"), type = "macro", assign = "micro", n = n, horizon = horizon))
})

Position <- evaluation[[1]][,"points"]

ARI <- sapply(evaluation, FUN=function(x) x[,"cRand"])
print('ARI:', quote = FALSE)
print(ARI)
matplot(Position, ARI, type= "l", lwd = 2)
matplot(Position, ARI, type= "l", lwd = 2)
legend("bottomleft", legend = names(evaluation), col = 1:6, lty = 1:6, bty = "n", lwd = 2)
boxplot(ARI, las = 2)

Purity <- sapply(evaluation, FUN=function(x) x[,"purity"])
print('Purity:', quote = FALSE)
print(Purity)
matplot(Position, Purity, type= "l", lwd = 2)
matplot(Position, Purity, type= "l", lwd = 2)
legend("bottomleft", legend = names(evaluation), col = 1:6, lty = 1:6, bty = "n", lwd = 2)
boxplot(Purity, las = 2)

Dunn <- sapply(evaluation, FUN=function(x) x[,"dunn"])
print('Dunn Index:', quote = FALSE)
print(Dunn)
matplot(Position, Dunn, type= "l", lwd = 2)
matplot(Position, Dunn, type= "l", lwd = 2)
legend("bottomleft", legend = names(evaluation), col = 1:6, lty = 1:6, bty = "n", lwd = 2)
boxplot(Dunn, las = 2)

Silhouette <- sapply(evaluation, FUN=function(x) x[,"silhouette"])
print('Silhouette Width:', quote = FALSE)
print(Silhouette)
matplot(Position, Silhouette, type= "l", lwd = 2)
matplot(Position, Silhouette, type= "l", lwd = 2)
legend("bottomleft", legend = names(evaluation), col = 1:6, lty = 1:6, bty = "n", lwd = 2)
boxplot(Silhouette, las = 2)

WCSSE <- sapply(evaluation, FUN=function(x) x[,"SSQ"])
print('WCSSE:', quote = FALSE)
print(WCSSE)
matplot(Position, WCSSE, type= "l", lwd = 2)
matplot(Position, WCSSE, type= "l", lwd = 2)
legend("bottomleft", legend = names(evaluation), col = 1:6, lty = 1:6, bty = "n", lwd = 2)
boxplot(WCSSE, las = 2)

VI <- sapply(evaluation, FUN=function(x) x[,"vi"])
print('VI:', quote = FALSE)
print(VI)
matplot(Position, VI, type= "l", lwd = 2)
matplot(Position, VI, type= "l", lwd = 2)
legend("bottomleft", legend = names(evaluation), col = 1:6, lty = 1:6, bty = "n", lwd = 2)
legend("bottomleft", legend = names(evaluation), col = 1:6, lty = 1:6, bty = "o", lwd = 2)
boxplot(VI, las = 2)

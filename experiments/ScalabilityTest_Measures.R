library(stream)
library(clValid)
library(mclust)
library(fpc)

d <- readline("Enter the number of dimensions: ")
d <- as.numeric(d)

## Start other algorithms
stream <- DSD_ReadCSV('dataset.csv', k = 10, d = d, class = d+1)

dstream <- DSC_TwoStage(micro = DSC_DStream(gridsize = 0.8, lambda = 0.01), macro = DSC_Kmeans(k = 10, weighted = TRUE, nstart = 25, algorithm = "Hartigan-Wong"))
sample <- DSC_TwoStage(micro = DSC_Sample(k = 100, biased = TRUE), macro = DSC_Kmeans(k = 10, weighted = TRUE, nstart = 25, algorithm = "Hartigan-Wong"))
window <- DSC_TwoStage(micro = DSC_Window(horizon = 100, lambda = 0.01), macro = DSC_Kmeans(k = 10, weighted = TRUE, nstart = 25, algorithm = "Hartigan-Wong"))

algorithms <- list(DStream = dstream, ReservoirSampling = sample, SlidingWindow = window)

n <- 20000   # Total number of points
horizon <- 1000   # Stream duration
reset_stream(stream)


evaluation <- lapply(algorithms, FUN = function(a) {
  reset_stream(stream)
  print(a)
  print(evaluate_cluster(a, stream, measure = c("numMicro", "numMacro", "cRand", "purity", "silhouette", "SSQ"), type = "macro", assign = "micro", n = n, horizon = horizon))
})

Position <- evaluation[[1]][,"points"]

sink(file = "out.txt")

ARI <- sapply(evaluation, FUN=function(x) x[,"cRand"])
print('ARI:', quote = FALSE)
print(ARI)

Purity <- sapply(evaluation, FUN=function(x) x[,"purity"])
print('Purity:', quote = FALSE)
print(Purity)

Silhouette <- sapply(evaluation, FUN=function(x) x[,"silhouette"])
print('Silhouette Width:', quote = FALSE)
print(Silhouette)

WCSSE <- sapply(evaluation, FUN=function(x) x[,"SSQ"])
print('WCSSE:', quote = FALSE)
print(WCSSE)

sink()
## End other algorithms

## Start Streaming RPHash and Streaming K-Means
h <- 20   # Number of horizons

funcPurity <- function(clusterLabels, groundTruthLabels) {
  sum(apply(table(groundTruthLabels, clusterLabels), 2, max)) / length(clusterLabels)
}

rp <- read.csv('RPHashLabels.csv')
km <- read.csv('SKMLabels.csv')
data <- read.csv('dataset.csv', header = FALSE)
data.features <- data
data.features[,ncol(data.features)] <- NULL

ARI_rp <- vector()
purity_rp <- vector()
silWidth_rp <- vector()
wcss_rp <- vector()

ARI_km <- vector()
purity_km <- vector()
silWidth_km <- vector()
wcss_km <- vector()

silWidth_base <- vector()
wcss_base <- vector()

p <- 1
q <- 1000

for(i in 1:h){
  gt <- data[c(p:q),ncol(data)]
  ARI_rp[i] <- adjustedRandIndex(rp[,i], gt)
  ARI_km[i] <- adjustedRandIndex(km[,i], gt)
  purity_rp[i] <- funcPurity(rp[,i], gt)
  purity_km[i] <- funcPurity(km[,i], gt)
  dataframe <- data.features[c(p:q),]
  d <- dist(dataframe, method="euclidean")
  p <- p+1000
  q <- q+1000
  sil_rp <- silhouette(rp[,i], d)
  silWidth_rp[i] <- summary(sil_rp)$si.summary['Mean']
  sil_km <- silhouette(km[,i], d)
  silWidth_km[i] <- summary(sil_km)$si.summary['Mean']
  sil_base <- silhouette(gt, d)
  silWidth_base[i] <- summary(sil_base)$si.summary['Mean']
  validations_rp <- cluster.stats(d, rp[,i], alt.clustering = gt)
  wcss_rp[i] <- validations_rp$within.cluster.ss
  validations_km <- cluster.stats(d, km[,i], alt.clustering = gt)
  wcss_km[i] <- validations_km$within.cluster.ss
  validations_base <- cluster.stats(d, gt)
  wcss_base[i] <- validations_base$within.cluster.ss
}

sink(file = "out.txt", append = TRUE)

writeLines('\n')
print("Streaming RPHash Output:")
writeLines('\n')
print("ARI")
print(t(t(ARI_rp)))
print("Purity")
print(t(t(purity_rp)))
print("Silhouette Width")
print(t(t(silWidth_rp)))
print("WCSSE")
print(t(t(wcss_rp)))

writeLines('\n')
print("Streaming K-Means Output:")
writeLines('\n')
print("ARI")
print(t(t(ARI_km)))
print("Purity")
print(t(t(purity_km)))
print("Silhouette Width")
print(t(t(silWidth_km)))
print("WCSSE")
print(t(t(wcss_km)))

writeLines('\n')
print("Baseline values of Silhouette and WCSSE:")
writeLines('\n')
print("Silhouette Width")
print(t(t(silWidth_base)))
print("WCSSE")
print(t(t(wcss_base)))

sink()
## End Streaming RPHash and Streaming K-Means

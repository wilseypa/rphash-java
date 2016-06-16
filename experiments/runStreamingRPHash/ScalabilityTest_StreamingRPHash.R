library(clValid)
library(mclust)
library(fpc)

## Start Streaming RPHash
h <- 20   # Number of horizons

funcPurity <- function(clusterLabels, groundTruthLabels) {
  sum(apply(table(groundTruthLabels, clusterLabels), 2, max)) / length(clusterLabels)
}

rp <- read.csv('RPHashLabels.csv', header = FALSE)
data <- read.csv('dataset.csv', header = FALSE)
data.features <- data
data.features[,ncol(data.features)] <- NULL

ARI_rp <- vector()
purity_rp <- vector()
silWidth_rp <- vector()
wcss_rp <- vector()

p <- 1
q <- 1000

for(i in 1:h){
  gt <- data[c(p:q),ncol(data)]
  ARI_rp[i] <- adjustedRandIndex(rp[,i], gt)
  purity_rp[i] <- funcPurity(rp[,i], gt)
  dataframe <- data.features[c(p:q),]
  d <- dist(dataframe, method="euclidean")
  p <- p+1000
  q <- q+1000
  sil_rp <- silhouette(rp[,i], d)
  silWidth_rp[i] <- summary(sil_rp)$si.summary['Mean']
  validations_rp <- cluster.stats(d, rp[,i], alt.clustering = gt)
  wcss_rp[i] <- validations_rp$within.cluster.ss
}

sink(file = "StreamingRPHashOutput.txt")

print("Streaming RPHash Output:")
writeLines('\n')
print("ARI")
print(t(t(ARI_rp)))
writeLines('\n')
print("Purity")
print(t(t(purity_rp)))
writeLines('\n')
print("Silhouette Width")
print(t(t(silWidth_rp)))
writeLines('\n')
print("WCSSE")
print(t(t(wcss_rp)))

sink()
## End Streaming RPHash

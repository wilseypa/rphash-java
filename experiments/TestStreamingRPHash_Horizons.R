# To see the performance of Streaming RPHash with horizon = 500

library(clValid)
library(mclust)
library(caret)
library(fpc)

h <- readline("Enter the number of horizons: ")
h <- as.numeric(h)

funcPurity <- function(clusterLabels, groundTruthLabels) {
  sum(apply(table(groundTruthLabels, clusterLabels), 2, max)) / length(clusterLabels)
}

gt <- read.csv('GTLabels.csv')
rp <- read.csv('RPHash.csv')
data <- read.csv('dataset.csv')
data.features <- data
data.features$Class <- NULL

ARI <- vector()
purity <- vector()
dunnIndex <- vector()
silWidth <- vector()
VI <- vector()

p <- 1
q <- 500

for(i in 1:h){
  ARI[i] <- adjustedRandIndex(rp[,i], gt[,i])
  purity[i] <- funcPurity(rp[,i], gt[,i])
  dataframe <- data.features[c(p:q),]
  d <- dist(dataframe, method="euclidean")
  p <- p+500
  q <- q+500
  dunnIndex[i] <- dunn(d, rp[,i])
  sil <- silhouette(rp[,i], d)
  silWidth[i] <- summary(sil)$si.summary['Mean']
  validations <- cluster.stats(d, rp[,i], alt.clustering = gt[,i])
  VI[i] <- validations$vi
}
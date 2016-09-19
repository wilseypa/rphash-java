require(mclust)

data <- read.csv('dataset.csv', header = TRUE)
data.features <- data
data.features[,ncol(data.features)] <- NULL

funcPurity <- function(clusterLabels, groundTruthLabels) {
  sum(apply(table(groundTruthLabels, clusterLabels), 2, max)) / length(clusterLabels)
}

k <- readline("Enter the number of clusters: ")
k <- as.numeric(k)
maxNstart <- readline("Enter the maximum value of nstart: ")
maxNstart <- as.numeric(maxNstart)
numRuns <- 6
avgARI <- vector()
sdARI <- vector()
avgPurity <- vector()
sdPurity <- vector()
avgWCSS <- vector()
sdWCSS <- vector()

for (i in 1:maxNstart) {
  ariEachRun <- vector()
  purityEachRun <- vector()
  wcssEachRun <- vector()
  for (j in 1:numRuns) {
    kmOut <- kmeans(data.features, k, iter.max = 10000, nstart = i)
    kmOut.labels <- kmOut$cluster
    ariEachRun[j] <- adjustedRandIndex(kmOut.labels, data[,ncol(data)])
    purityEachRun[j] <- funcPurity(kmOut.labels, data[,ncol(data)])
    wcssEachRun[j] <- kmOut$tot.withinss
  }
  avgARI[i] <- mean(ariEachRun)
  sdARI[i] <- sd(ariEachRun)
  avgPurity[i] <- mean(purityEachRun)
  sdPurity[i] <- sd(purityEachRun)
  avgWCSS[i] <- mean(wcssEachRun)
  sdWCSS[i] <- sd(wcssEachRun)
}

mat <- paste0(1:maxNstart, ",", avgARI, ",", avgPurity, ",", avgWCSS, ",", 
              sdARI, ",", sdPurity, ",", sdWCSS)
write.table(mat, file = 'nstartTestResults.csv', append = TRUE, quote = FALSE, 
            sep = ",", row.names = FALSE, col.names = FALSE)
require(clValid)
require(mclust)
require(fpc)

data <- read.csv("/home/anindya/testFolder/dataset.csv", header = TRUE)
gt <- data[,ncol(data)]
data.features <- data
data.features[, ncol(data.features)] <- NULL
d <- dist(data.features, method = "euclidean")

funcPurity <- function(clusterLabels, groundTruthLabels) {
  sum(apply(table(groundTruthLabels, clusterLabels), 2, max)) / length(clusterLabels)
}

n <- 6

ari <- vector()
purity <- vector()
silWidth <- vector()
wcsse <- vector()
runtimes <- vector()
memory <- vector()

files <- list.files(path = "/home/anindya/testFolder/", pattern = "*.csv", full.names = TRUE)
lapply(files, function(i) {
  f <- read.csv(i, header = FALSE)
  if (ncol(f) == n) {
    for (j in 1:n) {
      ari[j] <- adjustedRandIndex(f[,j], gt)
      purity[j] <- funcPurity(f[,j], gt)
      if (length(unique(f[,j])) != 1) {
        sil <- silhouette(f[,j], d)
        silWidth[j] <- summary(sil)$si.summary['Mean']
        validations <- cluster.stats(d, f[,j])
        wcsse[j] <- validations$within.cluster.ss
      }
      else {
        sink(file = "Message.txt")
        print(paste0(i, "Config detected only one cluster at run ", j), quote = FALSE)
        sink()
      }
    }
    ari_mean <- mean(ari)
    purity_mean <- mean(purity)
    ari_sd <- sd(ari)
    purity_sd <- sd(purity)
    silWidth_mean <- mean(silWidth)
    silWidth_sd <- sd(silWidth)
    wcsse_mean <- mean(wcsse)
    wcsse_sd <- sd(wcsse)
    row1 <- paste0(i, ",", ari_mean, ",", ari_sd, ",", purity_mean, ",", purity_sd, ",", silWidth_mean, ",", silWidth_sd,
                     ",", wcsse_mean, ",", wcsse_sd)
    write.table(row1, file = 'TestMeasures.csv', append = TRUE, quote = FALSE, sep = ",",
                row.names = FALSE, col.names = FALSE)
  }
  if (ncol(f) == 18) {
    for (k in 1:n) {
      runtimes[k] <- f[1, 3*k-2]
      memory[k] <- f[1, 3*k-1]
    }
    runtimes_mean <- mean(runtimes)
    memory_mean <- mean(memory)
    row2 <- paste0(i, ",", runtimes_mean, ",", memory_mean)
    write.table(row2, file = 'TimeMemory.csv', append = TRUE, quote = FALSE, sep = ",",
                row.names = FALSE, col.names = FALSE)
  }
})
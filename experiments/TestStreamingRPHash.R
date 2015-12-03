# To compare Streaming RPHash performance under various configurations.

library(clValid)
library(mclust)
library(caret)
library(fpc)

K <- readline("Enter the number of clusters: ")
K <- as.numeric(K)

funcPurity <- function(clusterLabels, groundTruthLabels) {
  sum(apply(table(groundTruthLabels, clusterLabels), 2, max)) / length(clusterLabels)
}

data <- read.csv('dataset.csv')
groundTruthLabels <- data$Class
data.features <- data
data.features$Class <- NULL

n <- 20

rphash_Strm <- read.csv('RPHash_Strm_Outputs.csv')
print("Streaming RPHash output:", quote = FALSE)

t_rpStrm <- rphash_Strm$Runtime[1:n]
ARI_rpStrm <- vector()
purity <- vector()
accuracy_rpStrm <- vector()
kappa_rpStrm <- vector()

for(i in 1:n){
  ARI_rpStrm[i] <- adjustedRandIndex(rphash_Strm[,i], data$Class)
  clusterLabels <- rphash_Strm[,i]
  purity[i] <- funcPurity(clusterLabels, groundTruthLabels)
  rpStrm_table <- table(rphash_Strm[,i], data$Class)
  accuracy_rpStrm[i] <- confusionMatrix(rpStrm_table)$overall['Accuracy']
  kappa_rpStrm[i] <- confusionMatrix(rpStrm_table)$overall['Kappa']
}

a_rpStrm_time <- mean(t_rpStrm)
a_purity <- mean(purity)
a_rpStrm_ARI <- mean(ARI_rpStrm)
a_rpStrm_accuracy <- mean(accuracy_rpStrm)
a_rpStrm_kappa <- mean(kappa_rpStrm)

s_rpStrm_time <- sd(t_rpStrm)
s_rpStrm_ARI <- sd(ARI_rpStrm)
s_rpStrm_accuracy <- sd(accuracy_rpStrm)
s_rpStrm_kappa <- sd(kappa_rpStrm)

error_rpStrm_time <- qt(0.975, df=n-1)*s_rpStrm_time/sqrt(n)
error_rpStrm_ARI <- qt(0.975, df=n-1)*s_rpStrm_ARI/sqrt(n)
error_rpStrm_accuracy <- qt(0.975, df=n-1)*s_rpStrm_accuracy/sqrt(n)
error_rpStrm_kappa <- qt(0.975, df=n-1)*s_rpStrm_kappa/sqrt(n)

left_rpStrm_time <- a_rpStrm_time - error_rpStrm_time
right_rpStrm_time <- a_rpStrm_time + error_rpStrm_time
print(paste0('Streaming RPHash average runtime with 95% CI: ', a_rpStrm_time, ', ', '(', left_rpStrm_time, ', ', right_rpStrm_time, ')'), quote = FALSE)

left_rpStrm_ARI <- a_rpStrm_ARI - error_rpStrm_ARI
right_rpStrm_ARI <- a_rpStrm_ARI + error_rpStrm_ARI
print(paste0('Streaming RPHash average Adjusted Rand Index with 95% CI :', a_rpStrm_ARI, ', ', '(', left_rpStrm_ARI, ', ', right_rpStrm_ARI, ')'), quote = FALSE)

print(paste0('Streaming RPHash average Purity :', a_purity), quote = FALSE)

left_rpStrm_accuracy <- a_rpStrm_accuracy - error_rpStrm_accuracy
right_rpStrm_accuracy <- a_rpStrm_accuracy + error_rpStrm_accuracy
print(paste0('Streaming RPHash average overall accuracy with 95% CI :', a_rpStrm_accuracy, ', ', '(', left_rpStrm_accuracy, ', ', right_rpStrm_accuracy, ')'), quote = FALSE)

left_rpStrm_kappa <- a_rpStrm_kappa - error_rpStrm_kappa
right_rpStrm_kappa <- a_rpStrm_kappa + error_rpStrm_kappa
print(paste0('Streaming RPHash Kappa Statistic with 95% CI : ', a_rpStrm_kappa, ', ', '(', left_rpStrm_kappa, ', ', right_rpStrm_kappa, ')'), quote = FALSE)

RPHash_Labels <- read.csv('RPHash_Strm_Outputs.csv')
d <- dist(data.features, method="euclidean")

dunnIndex <- vector()
silWidth <- vector()
wcss <- vector()
VI <- vector()

for(i in 1:n){
  dunnIndex[i] <- dunn(d, RPHash_Labels[,i])
  sil <- silhouette(RPHash_Labels[,i], d)
  silWidth[i] <- summary(sil)$si.summary['Mean']
  clusterLabels <- RPHash_Labels[,i]
  validations <- cluster.stats(d, clusterLabels, alt.clustering = groundTruthLabels)
  wcss[i] <- validations$within.cluster.ss
  VI[i] <- validations$vi
}

a_dunnIndex <- mean(dunnIndex)
a_silWidth <- mean(silWidth)
a_wcss <- mean(wcss)
a_VI <- mean(VI)

print(paste0('Streaming RPHash average Dunn Index :', a_dunnIndex), quote = FALSE)
print(paste0('Streaming RPHash average Silhouette Width :', a_silWidth), quote = FALSE)
print(paste0('Streaming RPHash average Within Clusters Sum of Squares :', a_wcss), quote = FALSE)
print(paste0('Streaming RPHash average Variation of Information :', a_VI), quote = FALSE)

library(clValid)
library(mclust)
library(caret)
library(fpc)

K <- readline("Enter the number of clusters: ")
K <- as.numeric(K)

data <- read.csv('C:/Users/user/Desktop/DataComm/iad/iad_scaled.csv', header=FALSE) 
d<-ncol(data)
d_minusone<- (d-1)
data.features <- data[,1:d_minusone]
gt <- data[,d]

funcPurity <- function(clusterLabels, groundTruthLabels) {
  sum(apply(table(groundTruthLabels, clusterLabels), 2, max)) / length(clusterLabels)
}
n <- 6

t_km <- vector()
ARI_km <- vector()
accuracy_km <- vector()
kappa_km <- vector()
purity_km <- vector()
dunnIndex_km <- vector()
silWidth_km <- vector()
wcss_km <- vector()
VI_km <- vector()

dunnIndex_base <- vector()
silWidth_base <- vector()
wcss_base <- vector()
VI_base <- vector()

# mem_km_reset<- vector()
# mem_km <- vector()

dis <- dist(data.features, method="euclidean")

sink(file = "C:/Users/user/Desktop/DataComm/iad/outiad_scaled.txt")

for(i in 1:n){
  t_km[i] <- system.time(km_out <- kmeans(data.features, K, nstart = 25))['user.self']
  print(gc(reset = TRUE))
  print(gc())
  ARI_km[i] <- adjustedRandIndex(km_out$cluster, gt)
  km_table <- table(km_out$cluster, gt)
  accuracy_km[i] <- confusionMatrix(km_table)$overall['Accuracy']
  kappa_km[i] <- confusionMatrix(km_table)$overall['Kappa']
  purity_km[i] <- funcPurity(km_out$cluster, gt)
  
  
  dunnIndex_km[i] <- dunn(dis, km_out$cluster)
  sil <- silhouette(km_out$cluster, dis)
  silWidth_km[i] <- summary(sil)$si.summary['Mean']
  validations <- cluster.stats(dis, km_out$cluster, alt.clustering = gt)
  wcss_km[i] <- validations$within.cluster.ss
  VI_km[i] <- validations$vi
  
  dunnIndex_base[i] <- dunn(dis, gt)
  sil_base <- silhouette(gt, dis)
  silWidth_base[i] <- summary(sil_base)$si.summary['Mean'] 
  validations_base <- cluster.stats(dis,gt)
  wcss_base[i] <- validations_base$within.cluster.ss
  VI_base[i] <- validations_base$vi
  
#   mem_km_reset[i]<-gc(reset = TRUE)
#   mem_km[i]<-gc()
}

a_km_time <- mean(t_km)
a_km_ARI <- mean(ARI_km)
a_km_accuracy <- mean(accuracy_km)
a_km_kappa <- mean(kappa_km)
a_km_purity <- mean(purity_km)
a_km_dunnIndex <- mean(dunnIndex_km)
a_km_silWidth <- mean(silWidth_km)
a_km_wcss <- mean(wcss_km)
a_km_VI <- mean(VI_km)

a_base_dunnIndex <- mean(dunnIndex_base)
a_base_silWidth <- mean(silWidth_base)
a_base_wcss <- mean(wcss_base)
a_base_VI <- mean(VI_base)

# a_km_mem_reset <- mean(mem_km_reset)
# a_km_mem <- mean(mem_km)




print("K-means output:", quote = FALSE)
print(paste0('K-means average runtime : ', a_km_time), quote = FALSE)
print(paste0('K-means average Adjusted Rand Index  :', a_km_ARI), quote = FALSE)
print(paste0('K-means average overall accuracy  :', a_km_accuracy), quote = FALSE)
print(paste0('K-means Kappa Statistic: ', a_km_kappa), quote = FALSE)
print(paste0('K-means average purity :', a_km_purity), quote = FALSE)
print(paste0('K-means average dunn index  :', a_km_dunnIndex), quote = FALSE)
print(paste0('K-means average BASE dunn index  :', a_base_dunnIndex), quote = FALSE)
print(paste0('K-means average sill Width  :', a_km_silWidth), quote = FALSE)
print(paste0('K-means average BASE sill Width  :', a_base_silWidth), quote = FALSE)
print(paste0('K-means average WCSSE  :', a_km_wcss), quote = FALSE)
print(paste0('K-means average BASE WCSSE  :', a_base_wcss), quote = FALSE)
print(paste0('K-means average VI  :', a_km_VI), quote = FALSE)
print(paste0('K-means average BASE VI  :', a_base_VI), quote = FALSE)

#print(paste0('K-means average MEM RESET  :', a_km_mem_reset), quote = FALSE)
#print(paste0('K-means average MEM ----  :', a_km_mem), quote = FALSE)


print("Single linkage output:", quote = FALSE)
t_single <- system.time(hc_single <- hclust(dist(data.features), method = "single"))['user.self']
print(paste0('Single linkage runtime: ', t_single), quote = FALSE)
print(gc(reset = TRUE))
print(gc())

tree_single <- cutree(hc_single, k = K)
single_table <- table(tree_single, gt)

ARI_single <- adjustedRandIndex(tree_single, gt)
print(paste0('Single linkage Adjusted Rand Index: ', ARI_single), quote = FALSE)
accuracy_single <- confusionMatrix(single_table)$overall['Accuracy']
print(paste0('Single linkage overall accuracy: ', accuracy_single), quote = FALSE)
kappa_single <- confusionMatrix(single_table)$overall['Kappa']
print(paste0('Single linkage Kappa Statistic: ', kappa_single), quote = FALSE)

purity_single <- funcPurity(tree_single, gt)
dunnIndex_single <- dunn(dis, tree_single)
sil_single <- silhouette(tree_single, dis)
silWidth_single <- summary(sil_single)$si.summary['Mean']
validations_single <- cluster.stats(dis, tree_single, alt.clustering = gt)
wcss_single <- validations_single$within.cluster.ss
VI_single <- validations_single$vi

print(paste0('Single linkage purity: ', purity_single), quote = FALSE)
print(paste0('Single linkage DUNN Index: ', dunnIndex_single), quote = FALSE)
print(paste0('Single linkage Sil Width: ', silWidth_single), quote = FALSE)
print(paste0('Single linkage WCSSE: ', wcss_single), quote = FALSE)
print(paste0('Single linkage VI : ', VI_single), quote = FALSE)


print("Complete linkage output:", quote = FALSE)
t_complete <- system.time(hc_complete <- hclust(dist(data.features), method = "complete"))['user.self']
print(paste0('Complete linkage runtime: ', t_complete), quote = FALSE)
print(gc(reset = TRUE))
print(gc())

tree_complete <- cutree(hc_complete, k = K)
complete_table <- table(tree_complete, gt)

ARI_complete <- adjustedRandIndex(tree_complete, gt)
print(paste0('Complete linkage Adjusted Rand Index: ', ARI_complete), quote = FALSE)
accuracy_complete <- confusionMatrix(complete_table)$overall['Accuracy']
print(paste0('Complete linkage overall accuracy: ', accuracy_complete), quote = FALSE)
kappa_complete <- confusionMatrix(complete_table)$overall['Kappa']
print(paste0('Complete linkage Kappa Statistic: ', kappa_complete), quote = FALSE)

purity_complete <- funcPurity(tree_complete, gt)
dunnIndex_complete <- dunn(dis, tree_complete)
sil_complete <- silhouette(tree_complete, dis)
silWidth_complete <- summary(sil_complete)$si.summary['Mean']
validations_complete <- cluster.stats(dis, tree_complete, alt.clustering = gt)
wcss_complete <- validations_complete$within.cluster.ss
VI_complete <- validations_complete$vi

print(paste0('complete linkage purity: ', purity_complete), quote = FALSE)
print(paste0('complete linkage DUNN Index: ', dunnIndex_complete), quote = FALSE)
print(paste0('complete linkage Sil Width: ', silWidth_complete), quote = FALSE)
print(paste0('complete linkage WCSSE: ', wcss_complete), quote = FALSE)
print(paste0('complete linkage VI : ', VI_complete), quote = FALSE)


print("Average linkage output:", quote = FALSE)
t_average <- system.time(hc_average <- hclust(dist(data.features), method = "average"))['user.self']
print(paste0('Average linkage runtime: ', t_average), quote = FALSE)
print(gc(reset = TRUE))
print(gc())

tree_average <- cutree(hc_average, k = K)
average_table <- table(tree_average, gt)

ARI_average <- adjustedRandIndex(tree_average, gt)
print(paste0('Average linkage Adjusted Rand Index: ', ARI_average), quote = FALSE)

accuracy_average <- confusionMatrix(average_table)$overall['Accuracy']
print(paste0('Average linkage overall accuracy: ', accuracy_average), quote = FALSE)

kappa_average <- confusionMatrix(average_table)$overall['Kappa']
print(paste0('Average linkage Kappa Statistic: ', kappa_average), quote = FALSE)



purity_average <- funcPurity(tree_average, gt)
dunnIndex_average <- dunn(dis, tree_average)
sil_average <- silhouette(tree_average, dis)
silWidth_average <- summary(sil_average)$si.summary['Mean']
validations_average <- cluster.stats(dis, tree_average, alt.clustering = gt)
wcss_average <- validations_average$within.cluster.ss
VI_average <- validations_average$vi

print(paste0('average linkage purity: ', purity_average), quote = FALSE)
print(paste0('average linkage DUNN Index: ', dunnIndex_average), quote = FALSE)
print(paste0('average linkage Sil Width: ', silWidth_average), quote = FALSE)
print(paste0('average linkage WCSSE: ', wcss_average), quote = FALSE)
print(paste0('average linkage VI : ', VI_average), quote = FALSE)



print("Ward output:", quote = FALSE)
t_ward <- system.time(hc_ward <- hclust(dist(data.features), method = "ward.D2"))['user.self']
print(paste0('Ward\'s runtime: ', t_ward), quote = FALSE)
print(gc(reset = TRUE))
print(gc())

tree_ward <- cutree(hc_ward, k = K)
ward_table <- table(tree_ward, gt)

ARI_ward <- adjustedRandIndex(tree_ward, gt)
print(paste0('Ward\'s Adjusted Rand Index: ', ARI_ward), quote = FALSE)

accuracy_ward <- confusionMatrix(ward_table)$overall['Accuracy']
print(paste0('Ward\'s overall accuracy: ', accuracy_ward), quote = FALSE)

kappa_ward <- confusionMatrix(ward_table)$overall['Kappa']
print(paste0('Ward\'s Kappa Statistic: ', kappa_ward), quote = FALSE)

purity_ward <- funcPurity(tree_ward, gt)
dunnIndex_ward <- dunn(dis, tree_ward)
sil_ward <- silhouette(tree_ward, dis)
silWidth_ward <- summary(sil_ward)$si.summary['Mean']
validations_ward <- cluster.stats(dis, tree_ward, alt.clustering = gt)
wcss_ward <- validations_ward$within.cluster.ss
VI_ward <- validations_ward$vi

print(paste0('ward purity: ', purity_ward), quote = FALSE)
print(paste0('ward DUNN Index: ', dunnIndex_ward), quote = FALSE)
print(paste0('ward Sil Width: ', silWidth_ward), quote = FALSE)
print(paste0('ward WCSSE: ', wcss_ward), quote = FALSE)
print(paste0('ward VI : ', VI_ward), quote = FALSE)


print("SOTA output:", quote = FALSE)
t_sota <- system.time(sotaCl <- sota(as.matrix(data.features), K-1))['user.self']
print(paste0('SOTA runtime: ', t_sota), quote = FALSE)
print(gc(reset = TRUE))
print(gc())

ARI_sota <- adjustedRandIndex(sotaCl$clust, gt)
print(paste0('SOTA Adjusted Rand Index: ', ARI_sota), quote = FALSE)

sota_table <- table(sotaCl$clust, gt)

accuracy_sota <- confusionMatrix(sota_table)$overall['Accuracy']
print(paste0('SOTA overall accuracy: ', accuracy_sota), quote = FALSE)

kappa_sota <- confusionMatrix(sota_table)$overall['Kappa']
print(paste0('SOTA Kappa Statistic: ', kappa_sota), quote = FALSE)

purity_sota <- funcPurity(sotaCl$clust, gt)
dunnIndex_sota <- dunn(dis, sotaCl$clust)
sil_sota <- silhouette(sotaCl$clust, dis)
silWidth_sota <- summary(sil_sota)$si.summary['Mean']
validations_sota <- cluster.stats(dis, sotaCl$clust, alt.clustering = gt)
wcss_sota <- validations_sota$within.cluster.ss
VI_sota <- validations_sota$vi

print(paste0('sota purity: ', purity_sota), quote = FALSE)
print(paste0('sota DUNN Index: ', dunnIndex_sota), quote = FALSE)
print(paste0('sota Sil Width: ', silWidth_sota), quote = FALSE)
print(paste0('sota WCSSE: ', wcss_sota), quote = FALSE)
print(paste0('sota VI : ', VI_sota), quote = FALSE)


sink()


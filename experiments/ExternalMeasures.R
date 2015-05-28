library(clValid)
library(mclust)
library(caret)

K <- readline("Enter the number of clusters: ")
K <- as.numeric(K)

data <- read.csv('dataset.csv')
data.features <- data
data.features$Class <- NULL

n <- 20

print("K-means output:", quote = FALSE)

t_km <- vector()
ARI_km <- vector()
accuracy_km <- vector()
kappa_km <- vector()

for(i in 1:n){
  t_km[i] <- system.time(km_out <- kmeans(data.features, K, nstart = 25))['user.self']
  ARI_km[i] <- adjustedRandIndex(km_out$cluster, data$Class)
  km_table <- table(km_out$cluster, data$Class)
  accuracy_km[i] <- confusionMatrix(km_table)$overall['Accuracy']
  kappa_km[i] <- confusionMatrix(km_table)$overall['Kappa']
}

a_km_time <- mean(t_km)
a_km_ARI <- mean(ARI_km)
a_km_accuracy <- mean(accuracy_km)
a_km_kappa <- mean(kappa_km)

s_km_time <- sd(t_km)
s_km_ARI <- sd(ARI_km)
s_km_accuracy <- sd(accuracy_km)
s_km_kappa <- sd(kappa_km)

error_km_time <- qt(0.975, df=n-1)*s_km_time/sqrt(n)
error_km_ARI <- qt(0.975, df=n-1)*s_km_ARI/sqrt(n)
error_km_accuracy <- qt(0.975, df=n-1)*s_km_accuracy/sqrt(n)
error_km_kappa <- qt(0.975, df=n-1)*s_km_kappa/sqrt(n)

left_km_time <- a_km_time - error_km_time
right_km_time <- a_km_time + error_km_time
print(paste0('K-means average runtime with 95% CI: ', a_km_time, ', ', '(', left_km_time, ', ', right_km_time, ')'), quote = FALSE)

left_km_ARI <- a_km_ARI - error_km_ARI
right_km_ARI <- a_km_ARI + error_km_ARI
print(paste0('K-means average Adjusted Rand Index with 95% CI :', a_km_ARI, ', ', '(', left_km_ARI, ', ', right_km_ARI, ')'), quote = FALSE)

left_km_accuracy <- a_km_accuracy - error_km_accuracy
right_km_accuracy <- a_km_accuracy + error_km_accuracy
print(paste0('K-means average overall accuracy with 95% CI :', a_km_accuracy, ', ', '(', left_km_accuracy, ', ', right_km_accuracy, ')'), quote = FALSE)

left_km_kappa <- a_km_kappa - error_km_kappa
right_km_kappa <- a_km_kappa + error_km_kappa
print(paste0('K-means Kappa Statistic with 95% CI : ', a_km_kappa, ', ', '(', left_km_kappa, ', ', right_km_kappa, ')'), quote = FALSE)


print("Single linkage output:", quote = FALSE)
t_single <- system.time(hc_single <- hclust(dist(data.features), method = "single"))['user.self']
print(paste0('Single linkage runtime: ', t_single), quote = FALSE)

tree_single <- cutree(hc_single, k = K)
single_table <- table(tree_single, data$Class)

ARI_single <- adjustedRandIndex(tree_single, data$Class)
print(paste0('Single linkage Adjusted Rand Index: ', ARI_single), quote = FALSE)

accuracy_single <- confusionMatrix(single_table)$overall['Accuracy']
print(paste0('Single linkage overall accuracy: ', accuracy_single), quote = FALSE)

kappa_single <- confusionMatrix(single_table)$overall['Kappa']
print(paste0('Single linkage Kappa Statistic: ', kappa_single), quote = FALSE)


print("Complete linkage output:", quote = FALSE)
t_complete <- system.time(hc_complete <- hclust(dist(data.features), method = "complete"))['user.self']
print(paste0('Complete linkage runtime: ', t_complete), quote = FALSE)

tree_complete <- cutree(hc_complete, k = K)
complete_table <- table(tree_complete, data$Class)

ARI_complete <- adjustedRandIndex(tree_complete, data$Class)
print(paste0('Complete linkage Adjusted Rand Index: ', ARI_complete), quote = FALSE)

accuracy_complete <- confusionMatrix(complete_table)$overall['Accuracy']
print(paste0('Complete linkage overall accuracy: ', accuracy_complete), quote = FALSE)

kappa_complete <- confusionMatrix(complete_table)$overall['Kappa']
print(paste0('Complete linkage Kappa Statistic: ', kappa_complete), quote = FALSE)


print("Average linkage output:", quote = FALSE)
t_average <- system.time(hc_average <- hclust(dist(data.features), method = "average"))['user.self']
print(paste0('Average linkage runtime: ', t_average), quote = FALSE)

tree_average <- cutree(hc_average, k = K)
average_table <- table(tree_average, data$Class)

ARI_average <- adjustedRandIndex(tree_average, data$Class)
print(paste0('Average linkage Adjusted Rand Index: ', ARI_average), quote = FALSE)

accuracy_average <- confusionMatrix(average_table)$overall['Accuracy']
print(paste0('Average linkage overall accuracy: ', accuracy_average), quote = FALSE)

kappa_average <- confusionMatrix(average_table)$overall['Kappa']
print(paste0('Average linkage Kappa Statistic: ', kappa_average), quote = FALSE)


print("Ward output:", quote = FALSE)
t_ward <- system.time(hc_ward <- hclust(dist(data.features), method = "ward.D2"))['user.self']
print(paste0('Ward\'s runtime: ', t_ward), quote = FALSE)

tree_ward <- cutree(hc_ward, k = K)
ward_table <- table(tree_ward, data$Class)

ARI_ward <- adjustedRandIndex(tree_ward, data$Class)
print(paste0('Ward\'s Adjusted Rand Index: ', ARI_ward), quote = FALSE)

accuracy_ward <- confusionMatrix(ward_table)$overall['Accuracy']
print(paste0('Ward\'s overall accuracy: ', accuracy_ward), quote = FALSE)

kappa_ward <- confusionMatrix(ward_table)$overall['Kappa']
print(paste0('Ward\'s Kappa Statistic: ', kappa_ward), quote = FALSE)


print("SOTA output:", quote = FALSE)
t_sota <- system.time(sotaCl <- sota(as.matrix(data.features), K-1))['user.self']
print(paste0('SOTA runtime: ', t_sota), quote = FALSE)

ARI_sota <- adjustedRandIndex(sotaCl$clust, data$Class)
print(paste0('SOTA Adjusted Rand Index: ', ARI_sota), quote = FALSE)

sota_table <- table(sotaCl$clust, data$Class)

accuracy_sota <- confusionMatrix(sota_table)$overall['Accuracy']
print(paste0('SOTA overall accuracy: ', accuracy_sota), quote = FALSE)

kappa_sota <- confusionMatrix(sota_table)$overall['Kappa']
print(paste0('SOTA Kappa Statistic: ', kappa_sota), quote = FALSE)


print("EM output:", quote = FALSE)
t_em <- system.time(model <- Mclust(data.features, G = K))['user.self']
print(paste0('EM runtime: ', t_em), quote = FALSE)

ARI_em <- adjustedRandIndex(model$classification, data$Class)
print(paste0('EM Adjusted Rand Index: ', ARI_em), quote = FALSE)

em_table <- table(model$classification, data$Class)

accuracy_em <- confusionMatrix(em_table)$overall['Accuracy']
print(paste0('EM overall accuracy: ', accuracy_em), quote = FALSE)

kappa_em <- confusionMatrix(em_table)$overall['Kappa']
print(paste0('EM Kappa Statistic: ', kappa_em), quote = FALSE)


rphash_Strm <- read.csv('RPHash_Strm_Outputs.csv')
print("Streaming RPHash output:", quote = FALSE)

t_rpStrm <- rphash_Strm$Runtime
ARI_rpStrm <- vector()
accuracy_rpStrm <- vector()
kappa_rpStrm <- vector()

for(i in 1:n){
  ARI_rpStrm[i] <- adjustedRandIndex(rphash_Strm[,i], data$Class)
  rpStrm_table <- table(rphash_Strm[,i], data$Class)
  accuracy_rpStrm[i] <- confusionMatrix(rpStrm_table)$overall['Accuracy']
  kappa_rpStrm[i] <- confusionMatrix(rpStrm_table)$overall['Kappa']
}

a_rpStrm_time <- mean(t_rpStrm)
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

left_rpStrm_accuracy <- a_rpStrm_accuracy - error_rpStrm_accuracy
right_rpStrm_accuracy <- a_rpStrm_accuracy + error_rpStrm_accuracy
print(paste0('Streaming RPHash average overall accuracy with 95% CI :', a_rpStrm_accuracy, ', ', '(', left_rpStrm_accuracy, ', ', right_rpStrm_accuracy, ')'), quote = FALSE)

left_rpStrm_kappa <- a_rpStrm_kappa - error_rpStrm_kappa
right_rpStrm_kappa <- a_rpStrm_kappa + error_rpStrm_kappa
print(paste0('Streaming RPHash Kappa Statistic with 95% CI : ', a_rpStrm_kappa, ', ', '(', left_rpStrm_kappa, ', ', right_rpStrm_kappa, ')'), quote = FALSE)



rphash_2Pass <- read.csv('RPHash_2Pass_Outputs.csv')
print("2-Pass RPHash output:", quote = FALSE)

t_rp2Pass <- rphash_2Pass$Runtime
ARI_rp2Pass <- vector()
accuracy_rp2Pass <- vector()
kappa_rp2Pass <- vector()

for(i in 1:n){
  ARI_rp2Pass[i] <- adjustedRandIndex(rphash_2Pass[,i], data$Class)
  rp2Pass_table <- table(rphash_2Pass[,i], data$Class)
  accuracy_rp2Pass[i] <- confusionMatrix(rp2Pass_table)$overall['Accuracy']
  kappa_rp2Pass[i] <- confusionMatrix(rp2Pass_table)$overall['Kappa']
}

a_rp2Pass_time <- mean(t_rp2Pass)
a_rp2Pass_ARI <- mean(ARI_rp2Pass)
a_rp2Pass_accuracy <- mean(accuracy_rp2Pass)
a_rp2Pass_kappa <- mean(kappa_rp2Pass)

s_rp2Pass_time <- sd(t_rp2Pass)
s_rp2Pass_ARI <- sd(ARI_rp2Pass)
s_rp2Pass_accuracy <- sd(accuracy_rp2Pass)
s_rp2Pass_kappa <- sd(kappa_rp2Pass)

error_rp2Pass_time <- qt(0.975, df=n-1)*s_rp2Pass_time/sqrt(n)
error_rp2Pass_ARI <- qt(0.975, df=n-1)*s_rp2Pass_ARI/sqrt(n)
error_rp2Pass_accuracy <- qt(0.975, df=n-1)*s_rp2Pass_accuracy/sqrt(n)
error_rp2Pass_kappa <- qt(0.975, df=n-1)*s_rp2Pass_kappa/sqrt(n)

left_rp2Pass_time <- a_rp2Pass_time - error_rp2Pass_time
right_rp2Pass_time <- a_rp2Pass_time + error_rp2Pass_time
print(paste0('2-Pass RPHash average runtime with 95% CI: ', a_rp2Pass_time, ', ', '(', left_rp2Pass_time, ', ', right_rp2Pass_time, ')'), quote = FALSE)

left_rp2Pass_ARI <- a_rp2Pass_ARI - error_rp2Pass_ARI
right_rp2Pass_ARI <- a_rp2Pass_ARI + error_rp2Pass_ARI
print(paste0('2-Pass RPHash average Adjusted Rand Index with 95% CI :', a_rp2Pass_ARI, ', ', '(', left_rp2Pass_ARI, ', ', right_rp2Pass_ARI, ')'), quote = FALSE)

left_rp2Pass_accuracy <- a_rp2Pass_accuracy - error_rp2Pass_accuracy
right_rp2Pass_accuracy <- a_rp2Pass_accuracy + error_rp2Pass_accuracy
print(paste0('2-Pass RPHash average overall accuracy with 95% CI :', a_rp2Pass_accuracy, ', ', '(', left_rp2Pass_accuracy, ', ', right_rp2Pass_accuracy, ')'), quote = FALSE)

left_rp2Pass_kappa <- a_rp2Pass_kappa - error_rp2Pass_kappa
right_rp2Pass_kappa <- a_rp2Pass_kappa + error_rp2Pass_kappa
print(paste0('2-Pass RPHash Kappa Statistic with 95% CI : ', a_rp2Pass_kappa, ', ', '(', left_rp2Pass_kappa, ', ', right_rp2Pass_kappa, ')'), quote = FALSE)

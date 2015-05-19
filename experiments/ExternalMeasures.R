library(caret)
library(clValid)
library(mclust)

K <- readline("Enter the number of clusters: ")
K <- as.numeric(K)

data <- read.csv('dataset.csv')
data.features <- data
data.features$Class <- NULL

n <- 20

print("K-means output:", quote = FALSE)

t_km <- vector()
accuracy_km <- vector()
kappa_km <- vector()

for(i in 1:n){
  t_km[i] <- system.time(km_out <- kmeans(data.features, K, nstart = 25))['user.self']
  km_table <- table(km_out$cluster, data$Class)
  accuracy_km[i] <- confusionMatrix(km_table)$overall['Accuracy']
  kappa_km[i] <- confusionMatrix(km_table)$overall['Kappa']
}

a_km_time <- mean(t_km)
a_km_accuracy <- mean(accuracy_km)
a_km_kappa <- mean(kappa_km)

s_km_time <- sd(t_km)
s_km_accuracy <- sd(accuracy_km)
s_km_kappa <- sd(kappa_km)

error_km_time <- qt(0.975, df=n-1)*s_km_time/sqrt(n)
error_km_accuracy <- qt(0.975, df=n-1)*s_km_accuracy/sqrt(n)
error_km_kappa <- qt(0.975, df=n-1)*s_km_kappa/sqrt(n)

left_km_time <- a_km_time - error_km_time
right_km_time <- a_km_time + error_km_time
print(paste0('K-means average runtime with 95% CI: ', a_km_time, ', ', '(', left_km_time, ', ', right_km_time, ')'), quote = FALSE)

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

accuracy_single <- confusionMatrix(single_table)$overall['Accuracy']
print(paste0('Single linkage overall accuracy: ', accuracy_single), quote = FALSE)

kappa_single <- confusionMatrix(single_table)$overall['Kappa']
print(paste0('Single linkage Kappa Statistic: ', kappa_single), quote = FALSE)


print("Complete linkage output:", quote = FALSE)
t_complete <- system.time(hc_complete <- hclust(dist(data.features), method = "complete"))['user.self']
print(paste0('Complete linkage runtime: ', t_complete), quote = FALSE)

tree_complete <- cutree(hc_complete, k = K)
complete_table <- table(tree_complete, data$Class)

accuracy_complete <- confusionMatrix(complete_table)$overall['Accuracy']
print(paste0('Complete linkage overall accuracy: ', accuracy_complete), quote = FALSE)

kappa_complete <- confusionMatrix(complete_table)$overall['Kappa']
print(paste0('Complete linkage Kappa Statistic: ', kappa_complete), quote = FALSE)


print("Average linkage output:", quote = FALSE)
t_average <- system.time(hc_average <- hclust(dist(data.features), method = "average"))['user.self']
print(paste0('Average linkage runtime: ', t_average), quote = FALSE)

tree_average <- cutree(hc_average, k = K)
average_table <- table(tree_average, data$Class)

accuracy_average <- confusionMatrix(average_table)$overall['Accuracy']
print(paste0('Average linkage overall accuracy: ', accuracy_average), quote = FALSE)

kappa_average <- confusionMatrix(average_table)$overall['Kappa']
print(paste0('Average linkage Kappa Statistic: ', kappa_average), quote = FALSE)


print("Ward output:", quote = FALSE)
t_ward <- system.time(hc_ward <- hclust(dist(data.features), method = "ward.D2"))['user.self']
print(paste0('Ward\'s runtime: ', t_ward), quote = FALSE)

tree_ward <- cutree(hc_ward, k = K)
ward_table <- table(tree_ward, data$Class)

accuracy_ward <- confusionMatrix(ward_table)$overall['Accuracy']
print(paste0('Ward\'s overall accuracy: ', accuracy_ward), quote = FALSE)

kappa_ward <- confusionMatrix(ward_table)$overall['Kappa']
print(paste0('Ward\'s Kappa Statistic: ', kappa_ward), quote = FALSE)


print("SOTA output:", quote = FALSE)
t_sota <- system.time(sotaCl <- sota(as.matrix(data.features), K-1))['user.self']
print(paste0('SOTA runtime: ', t_sota), quote = FALSE)

sota_table <- table(sotaCl$clust, data$Class)

accuracy_sota <- confusionMatrix(sota_table)$overall['Accuracy']
print(paste0('SOTA overall accuracy: ', accuracy_sota), quote = FALSE)

kappa_sota <- confusionMatrix(sota_table)$overall['Kappa']
print(paste0('SOTA Kappa Statistic: ', kappa_sota), quote = FALSE)


print("EM output:", quote = FALSE)
t_em <- system.time(model <- Mclust(data.features, G = K))['user.self']
print(paste0('EM runtime: ', t_em), quote = FALSE)

em_table <- table(model$classification, data$Class)

accuracy_em <- confusionMatrix(em_table)$overall['Accuracy']
print(paste0('EM overall accuracy: ', accuracy_em), quote = FALSE)

kappa_em <- confusionMatrix(em_table)$overall['Kappa']
print(paste0('EM Kappa Statistic: ', kappa_em), quote = FALSE)


rphash <- read.csv('RPHash Outputs.csv')
print("RPHash output:", quote = FALSE)

t_rp <- rphash$Runtime
accuracy_rp <- vector()
kappa_rp <- vector()

for(i in 1:n){
  rp_table <- table(rphash[,i], data$Class)
  accuracy_rp[i] <- confusionMatrix(rp_table)$overall['Accuracy']
  kappa_rp[i] <- confusionMatrix(rp_table)$overall['Kappa']
}

a_rp_time <- mean(t_rp)
a_rp_accuracy <- mean(accuracy_rp)
a_rp_kappa <- mean(kappa_rp)

s_rp_time <- sd(t_rp)
s_rp_accuracy <- sd(accuracy_rp)
s_rp_kappa <- sd(kappa_rp)

error_rp_time <- qt(0.975, df=n-1)*s_rp_time/sqrt(n)
error_rp_accuracy <- qt(0.975, df=n-1)*s_rp_accuracy/sqrt(n)
error_rp_kappa <- qt(0.975, df=n-1)*s_rp_kappa/sqrt(n)

left_rp_time <- a_rp_time - error_rp_time
right_rp_time <- a_rp_time + error_rp_time
print(paste0('RPHash average runtime with 95% CI: ', a_rp_time, ', ', '(', left_rp_time, ', ', right_rp_time, ')'), quote = FALSE)

left_rp_accuracy <- a_rp_accuracy - error_rp_accuracy
right_rp_accuracy <- a_rp_accuracy + error_rp_accuracy
print(paste0('RPHash average overall accuracy with 95% CI :', a_rp_accuracy, ', ', '(', left_rp_accuracy, ', ', right_rp_accuracy, ')'), quote = FALSE)

left_rp_kappa <- a_rp_kappa - error_rp_kappa
right_rp_kappa <- a_rp_kappa + error_rp_kappa
print(paste0('RPHash Kappa Statistic with 95% CI : ', a_rp_kappa, ', ', '(', left_rp_kappa, ', ', right_rp_kappa, ')'), quote = FALSE)

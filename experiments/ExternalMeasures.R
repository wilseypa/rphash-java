library(clValid)
library(mclust)

K <- readline("Enter the number of clusters: ")
K <- as.numeric(K)

data <- read.csv('C:/Users/Anindya/Desktop/inR_caret.csv')
data.features <- data
data.features$Class <- NULL

n <- 20

print("K-means output:", quote = FALSE)

t_km <- vector()
ARI_km <- vector()

for(i in 1:n){
  t_km[i] <- system.time(km_out <- kmeans(data.features, K, nstart = 25))['user.self']
  ARI_km[i] <- adjustedRandIndex(km_out$cluster, data$Class)
}

a_km_time <- mean(t_km)
a_km_ARI <- mean(ARI_km)

s_km_time <- sd(t_km)
s_km_ARI <- sd(ARI_km)

error_km_time <- qt(0.975, df=n-1)*s_km_time/sqrt(n)
error_km_ARI <- qt(0.975, df=n-1)*s_km_ARI/sqrt(n)

left_km_time <- a_km_time - error_km_time
right_km_time <- a_km_time + error_km_time
print(paste0('K-means average runtime with 95% CI: ', a_km_time, ', ', '(', left_km_time, ', ', right_km_time, ')'), quote = FALSE)

left_km_ARI <- a_km_ARI - error_km_ARI
right_km_ARI <- a_km_ARI + error_km_ARI
print(paste0('K-means average Adjusted Rand Index with 95% CI :', a_km_ARI, ', ', '(', left_km_ARI, ', ', right_km_ARI, ')'), quote = FALSE)


print("Single linkage output:", quote = FALSE)
t_single <- system.time(hc_single <- hclust(dist(data.features), method = "single"))['user.self']
print(paste0('Single linkage runtime: ', t_single), quote = FALSE)

tree_single <- cutree(hc_single, k = K)
ARI_single <- adjustedRandIndex(tree_single, data$Class)
print(paste0('Single linkage Adjusted Rand Index: ', ARI_single), quote = FALSE)


print("Complete linkage output:", quote = FALSE)
t_complete <- system.time(hc_complete <- hclust(dist(data.features), method = "complete"))['user.self']
print(paste0('Complete linkage runtime: ', t_complete), quote = FALSE)

tree_complete <- cutree(hc_complete, k = K)
ARI_complete <- adjustedRandIndex(tree_complete, data$Class)
print(paste0('Complete linkage Adjusted Rand Index: ', ARI_complete), quote = FALSE)


print("Average linkage output:", quote = FALSE)
t_average <- system.time(hc_average <- hclust(dist(data.features), method = "average"))['user.self']
print(paste0('Average linkage runtime: ', t_average), quote = FALSE)

tree_average <- cutree(hc_average, k = K)
ARI_average <- adjustedRandIndex(tree_average, data$Class)
print(paste0('Average linkage Adjusted Rand Index: ', ARI_average), quote = FALSE)


print("Ward output:", quote = FALSE)
t_ward <- system.time(hc_ward <- hclust(dist(data.features), method = "ward.D2"))['user.self']
print(paste0('Ward\'s runtime: ', t_ward), quote = FALSE)

tree_ward <- cutree(hc_ward, k = K)
ARI_ward <- adjustedRandIndex(tree_ward, data$Class)
print(paste0('Ward\'s Adjusted Rand Index: ', ARI_ward), quote = FALSE)


print("SOTA output:", quote = FALSE)
t_sota <- system.time(sotaCl <- sota(as.matrix(data.features), K-1))['user.self']
print(paste0('SOTA runtime: ', t_sota), quote = FALSE)

ARI_sota <- adjustedRandIndex(sotaCl$clust, data$Class)
print(paste0('SOTA Adjusted Rand Index: ', ARI_sota), quote = FALSE)


print("EM output:", quote = FALSE)
t_em <- system.time(model <- Mclust(data.features, G = K))['user.self']
print(paste0('EM runtime: ', t_em), quote = FALSE)

ARI_em <- adjustedRandIndex(model$classification, data$Class)
print(paste0('EM Adjusted Rand Index: ', ARI_em), quote = FALSE)


rphash <- read.csv('C:/Users/Anindya/Desktop/RPHash Outputs.csv')
print("RPHash output:", quote = FALSE)

t_rp <- rphash$Runtime
ARI_rp <- vector()

for(i in 1:n){
  ARI_rp[i] <- adjustedRandIndex(rphash[,i], data$Class)
}

a_rp_time <- mean(t_rp)
a_rp_ARI <- mean(ARI_rp)

s_rp_time <- sd(t_rp)
s_rp_ARI <- sd(ARI_rp)

error_rp_time <- qt(0.975, df=n-1)*s_rp_time/sqrt(n)
error_rp_ARI <- qt(0.975, df=n-1)*s_rp_ARI/sqrt(n)

left_rp_time <- a_rp_time - error_rp_time
right_rp_time <- a_rp_time + error_rp_time
print(paste0('RPHash average runtime with 95% CI: ', a_rp_time, ', ', '(', left_rp_time, ', ', right_rp_time, ')'), quote = FALSE)

left_rp_ARI <- a_rp_ARI - error_rp_ARI
right_rp_ARI <- a_rp_ARI + error_rp_ARI
print(paste0('RPHash average Adjusted Rand Index with 95% CI :', a_rp_ARI, ', ', '(', left_rp_ARI, ', ', right_rp_ARI, ')'), quote = FALSE)

myDSD_Gaussians <- function(k=2, d=2, mu, sigma, p, separation=0.2, 
                            noise = 0, noise_range) { 
  
  # if p isn't defined, we give all the clusters equal probability
  if (missing(p)) {
    p <- rep(1/k, k)
  }
  
  # for each d, random value between 0 and 1
  # we create a matrix of d columns and k rows
  if (missing(mu)) {
    mu <- matrix(runif(d*k, min=0.2, max=0.8), ncol=d)
    
    if(separation>0) {
      i <- 100L
      while(any(dist(mu)<separation)){
        mu <- matrix(runif(d*k, min=0.2, max=0.8), ncol=d)
        i <- i - 1L
        if(i<1L) stop("Unable to find centers with sufficient separation!")
      }
    }
    
  } else {
    mu <- as.matrix(mu)
  }
  
  # covariance matrix
  if (missing(sigma)) {
    sigma <- replicate(k,clusterGeneration::genPositiveDefMat("eigen", rangeVar=c(0.001,0.01), lambdaLow= 0.002, ratioLambda=4, dim=d)$Sigma, simplify=F)
  }
  
  # noise
  if (noise == 0) noise_range <- NA
  else {
    if (missing(noise_range)) noise_range <- matrix(c(0,1), 
                                                    ncol=2, nrow=d, byrow=TRUE)
    else if (ncol(noise_range) != 2 || nrow(noise_range) != d) {
      stop("noise_range is not correctly specified!")	
    }
  }
  
  # error checking
  if (length(p) != k)
    stop("size of probability vector, p, must equal k")
  
  if (d < 0)
    stop("invalid number of dimensions")
  
  if (ncol(mu) != d || nrow(mu) != k)
    stop("invalid size of mu matrix")
  
  ## TODO: error checking on sigma
  # list of length k
  # d x d matrix in the list
  
  l <- list(description = "Mixture of Gaussians",
            k = k,
            d = d,
            mu = mu,
            sigma = sigma,
            p = p,
            noise = noise,
            noise_range = noise_range)
  class(l) <- c("myDSD_Gaussians","DSD_R", "DSD_data.frame", "DSD")
  l
}

require(parallel)
get_myPoints.myDSD_Gaussians <- function(x, n=1, 
                                         outofpoints=c("stop", "warn", "ignore"), 
                                         cluster = FALSE, class = FALSE, ...) {

  
  clusterOrder <- sample(x=c(1:x$k), 
                         size=n, 
                         replace=TRUE, 
                         prob=x$p)
  
  data <- t(as.data.frame(mclapply(clusterOrder, FUN = function(i) MASS::mvrnorm(1, mu=x$mu[i,], Sigma=x$sigma[[i]]), mc.cores = 12)))			
  rownames(data) <- NULL

  ## Replace some points by random noise
  ## TODO: [0,1]^d might not be a good choice. Some clusters can have
  ## points outside this range!
  if(x$noise) {
    repl <- runif(n)<x$noise 
    if(sum(repl)>0) {
      data[repl,] <- t(replicate(sum(repl),runif(x$d, 
                                                 min=x$noise_range[,1],
                                                 max=x$noise_range[,2])))
      clusterOrder[repl] <- sample(x=c(1:x$k), size = sum(repl), replace = TRUE)
    }
  }

  data <- as.data.frame(data)
  if(cluster) attr(data, "cluster") <- clusterOrder
  if(class) data <- cbind(data, class = clusterOrder)
  
  data
}

d <- readline("Enter the number of dimensions: ")
d <- as.numeric(d)

noise <- readline("Enter the noise percentage: ")
noise <- as.numeric(noise)

stream <- myDSD_Gaussians(k = 10, d = d, noise = noise)

dataPoints <- get_myPoints.myDSD_Gaussians(stream, n = 20004, cluster = TRUE)
dataPoints$Class <- attr(dataPoints, 'cluster')
write.csv(dataPoints, file = paste('NoisyData_', noise, '.csv', sep=""), row.names = FALSE)

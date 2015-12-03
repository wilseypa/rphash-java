### simulate the clustering of a splitting cluster
c1 <- MGC_Linear(dim = 2, keyframelist = list(
  keyframe(time = 1, dens = 20, center = c(0,0), param = 10),
  keyframe(time = 50, dens = 10, center = c(50,50), param = 10),
  keyframe(time = 100,dens = 10, center = c(50,100),param = 10)
))
### Note: Second cluster appearch at time=50
c2 <- MGC_Linear(dim = 2, keyframelist = list(
  keyframe(time = 50, dens = 10, center = c(50,50), param = 10),
  keyframe(time = 100,dens = 10, center = c(100,50),param = 10)
))
stream <- DSD_MG(dim = 2, c1, c2)
stream
dbstream <- DSC_DBSTREAM(r=10, lambda=0.1)
## Not run:
purity <- animate_cluster(dbstream, stream, n=3500, type="both",
                          xlim=c(-10,120), ylim=c(-10,120), evaluationMeasure="purity", horizon=100)
## End(Not run)
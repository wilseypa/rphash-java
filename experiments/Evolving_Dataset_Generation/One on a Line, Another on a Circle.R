### Example of several MGC_Functions
stream <- DSD_MG(dim = 2)
### block-shaped cluster moving from bottom-left to top-right increasing size
c1 <- MGC_Function(
  density = function(t){100},
  parameter = function(t){1*t},
  center = function(t) c(t,t),
  shape = MGC_Shape_Block
)
add_cluster(stream,c1)
### cluster moving in a circle (default shape is Gaussian)
c2 <- MGC_Function(
  density = function(t){25},
  parameter = function(t){5},
  center= function(t) c(sin(t/10)*50+50, cos(t/10)*50+50)
)
add_cluster(stream,c2)
## Not run:
animate_data(stream,15000,xlim=c(-20,120),ylim=c(-20,120), horizon=100)
## End(Not run)
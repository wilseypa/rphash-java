### two fixed and a moving cluster
stream <- DSD_MG(dim = 2,
                 MGC_Static(dens=1, par=.1, center=c(0,0)),
                 MGC_Static(dens=1, par=.1, center=c(1,1)),
                 MGC_Linear(dim=2,list(
                   keyframe(time = 0, dens=1, par=.1, center=c(0,0)),
                   keyframe(time = 1000, dens=1, par=.1, center=c(1,1)),
                   keyframe(time = 2000, dens=1, par=.1, center=c(0,0), reset=TRUE)
                 )))
noise <- MGC_Noise(dens=.1, range=rbind(c(-.2,1.2),c(-.2,1.2)))
add_cluster(stream, noise)
## Not run:
animate_data(stream, n=2000*7.1, xlim=c(-.2,1.2), ylim=c(-.2,1.2), horiz=200)
## End(Not run)
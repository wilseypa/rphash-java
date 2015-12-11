stream <- DSD_MG(dimension=2,
                 MGC_Random(den = 100, center=c(1, 0), par=.1, rand=.5),
                 MGC_Random(den = 100, center=c(2, 0), par=.4, shape=MGC_Shape_Block, rand=.5)
)
## Not run:
animate_data(stream, 2500, xlim=c(0,3), ylim=c(-2,2), horizon=100)
## End(Not run)
cutEqual <- function(x, n, include.lowest = TRUE, ...) {
    stopifnot(require(lattice))
    cut(x, co.intervals(x, n, 0)[c(1, (n+1):(n*2))], 
        include.lowest = include.lowest, ...)
}

set.seed(12345)
x <- rnorm(50)
table(cutEqual(x, 5))
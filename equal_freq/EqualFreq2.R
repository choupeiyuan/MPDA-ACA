EqualFreq2 <- function(x,n){
    nx <- length(x)
    nrepl <- floor(nx/n)
    nplus <- sample(1:n,nx - nrepl*n)
    nrep <- rep(nrepl,n)
    nrep[nplus] <- nrepl+1
    x[order(x)] <- rep(seq.int(n),nrep)
    x
}

x <- rpois(50,5)
y <- EqualFreq2(x,3)
table(y)
split(x,y)
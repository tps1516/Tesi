pack1 <- require(MASS)
pack2 <- require(sandwich)
pack3 <- require(base)
pack4 <- require(zoo)
pack5 <- require(strucchange)
pack6 <- require(lmtest)
pack7 <- require(urca)
pack8 <- require(vars)
pack8 <- require(Runiversal)
data(Canada)
s<-Canada
varNuova <- VARselect(s, lag.max = 2, type= "none", season = NULL, exogen = NULL)
orderp <- as.numeric(varNuova$selection[1])
l <- VAR(s, p=orderp, type="none", ic = "AIC")
coefficients<-list(e = as.numeric(l$varresult$e$coefficients),prod = as.numeric(l$varresult$prod$coefficients), rw=as.numeric(l$varresult$rw$coefficients), u=as.numeric(l$varresult$U$coefficients))

e_dataframe = data.frame(l$varresult$e$coefficients)
prod_dataframe = data.frame(l$varresult$prod$coefficients)
rw_dataframe = data.frame(l$varresult$rw$coefficients)
u_dataframe = data.frame(l$varresult$U$coefficients)

e_nomifeature <- dimnames(e_dataframe)
rw_nomifeature <- dimnames(rw_dataframe)
prod_nomifeature <- dimnames(prod_dataframe)
u_nomifeature <- dimnames(u_dataframe)

e_nomifeature <- e_nomifeature[[1]]
prod_nomifeature <- prod_nomifeature[[1]]
rw_nomifeature <- rw_nomifeature[[1]]
u_nomifeature <- u_nomifeature[[1]]

nomifeature = list ( e = e_nomifeature, prod = prod_nomifeature, rw = rw_nomifeature, u = u_nomifeature)

s2 <- predict (l,n.ahead=10, ci=0.95)
q <- list(f=as.numeric(s2$fcst$1s[1,1]))



        code.addRCode("dimnames(s) <- list(c('1g','2g','3g','4g','5g'),c('e','2s'))");
        code.addRCode("s[1,1] <- 11");
        code.addRCode("s[2,1] <- 21");
        code.addRCode(" s[2,2] <- 22");
        code.addRCode("s[1,2] <- 12");
        code.addRCode("s[3,1] <- 31");
        code.addRCode("s[3,2] <- 32");
        code.addRCode("s[3,1] <- 41");
        code.addRCode("s[3,2] <- 42");
        code.addRCode("s[4,1] <- 41");
        code.addRCode("s[4,2] <- 42");
        code.addRCode("s[5,1] <- 51");
        code.addRCode("s[5,2] <- 52");   
        


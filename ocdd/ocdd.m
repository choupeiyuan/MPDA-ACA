function [ a,aa ] = ocdd( DD,CC )
%UNTITLED Summary of this function goes here
%   DD is the colum of discrete attribute, CC is the continuous. Output a is
%   the discretized colum of CC, aa is the corresponding cutting value of CC.
    D=unique(DD);K=length(D);n=length(DD);
    A=unique(CC);R=length(A);
    Q=zeros(K,R);
    for i=1:n
        k=sum(D<=DD(i));r=sum(A<=CC(i));
        Q(k,r)=Q(k,r)+1;
    end
    Q=Q/n;
    Pk=sum(Q,2);Pr=sum(Q,1);
    con=sum(Pk.*log(Pk));u=1;F=-con;
    while(F>0)
        par=zeros(R,R);
        par(1,1)=1;ff=zeros(1,R);
        ff(1)=-Pr(1)*log(Pr(1))+u*sum(Q((Q(:,1)~=0),1).*log(Q((Q(:,1)~=0),1)));
        for i=2:R
            ppr=sum(Pr(1:i));
            pkr=sum(Q(:,(1:i)),2);
            ss=-ppr*log(ppr)+u*sum(pkr(pkr~=0).*log(pkr(pkr~=0)));
            par(i,1)=i;
            for j=2:i
                ppr=sum(Pr(j:i));
                pkr=sum(Q(:,(j:i)),2);
                ss1=-ppr*log(ppr)+u*sum(pkr(pkr~=0).*log(pkr(pkr~=0)));
                if(ff(j-1)+ss1>ss)
                    ss=ff(j-1)+ss1;
                    par(i,:)=par(j-1,:);
                    par(i,sum(par(i,:)~=0)+1)=i;
                end
            end
            ff(i)=ss;
        end
        F=ff(R)-con;
        ge=sum(par(R,:)~=0);
        pppr=sum(Pr(1:par(R,1)));
        for i=2:ge
            pppr(i)=sum(Pr(par(R,i-1)+1:par(R,i)));
        end
        qqq=sum(pppr.*log(pppr));
        u=(con+qqq)*u/(con+qqq+F);
    end
    ge=sum(par(R,:)~=0);
    aa=A(par(R,1:ge));
    a=CC;
    for i=1:length(a)
        a(i)=sum(a(i)>aa)+1;
    end
end

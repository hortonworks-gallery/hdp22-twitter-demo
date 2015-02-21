drop table if exists securities;

create table securities (
 symbol varchar(50),
 name   varchar(100),
 sector varchar(50),
 subindustry varchar(100),
 address varchar(50),
 cik     varchar(20),
 tweet_threshold bigint,
 CONSTRAINT pk PRIMARY KEY (symbol)
);

drop table if exists alerts;

create table alerts (
 alertid bigint,
 symbol  varchar(50),
 alerttime varchar(50),
 tweetvolume bigint
 CONSTRAINT pk PRIMARY KEY(alertid)
);

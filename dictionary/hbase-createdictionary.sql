drop table if exists dictionary;

create table dictionary (
 word varchar(100),
 polarity   varchar(15),
 CONSTRAINT pk PRIMARY KEY (word)
);


drop table tweets_text_partition;

create table tweets_text_partition(
userId string
, displayname string
, hashtag string
, tweet string
, created string
, longitude string
, latitude string
, language string
, fulltext string
)
row format delimited fields terminated by '|'
location '/tweets/staging';

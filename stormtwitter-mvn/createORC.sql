drop table tweets_orc_partition_single;

create external table tweets_orc_partition_single(
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
row format serde 'org.apache.hadoop.hive.ql.io.orc.OrcSerde'
stored as orc;

set hive.exec.dynamic.partition.mode=nonstrict;
INSERT OVERWRITE TABLE tweets_orc_partition_single SELECT * FROM tweets_text_partition;


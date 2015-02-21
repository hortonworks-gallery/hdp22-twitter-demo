echo "Creating Hbase table with thresholds"
/root/phoenix-4.1.0-bin/hadoop2/bin/psql.py  localhost:2181:/hbase /root/hdp22-twitter-demo/fetchSecuritiesList/hbase-createstockthresholds.sql /root/hdp22-twitter-demo/fetchSecuritiesList/securities.csv


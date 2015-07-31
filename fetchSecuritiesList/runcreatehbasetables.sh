echo "Creating Hbase table with thresholds"
/usr/hdp/current/phoenix-client/bin/psql.py localhost:2181:/hbase-unsecure /root/hdp22-twitter-demo/fetchSecuritiesList/hbase-createstockthresholds.sql /root/hdp22-twitter-demo/fetchSecuritiesList/securities.csv


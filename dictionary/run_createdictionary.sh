echo "Creating Hbase table with dictionary"
/usr/hdp/current/phoenix-client/bin/psql.py  localhost:2181:/hbase-unsecure /root/hdp22-twitter-demo/dictionary/hbase-createdictionary.sql /root/hdp22-twitter-demo/dictionary/dictionary.csv


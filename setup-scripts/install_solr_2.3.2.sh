sed -i "s/<arr name=\"format\">/<arr name=\"format\">\n        <str>EEE MMM d HH:mm:ss Z yyyy<\/str>/" /opt/lucidworks-hdpsearch/solr/server/solr/configsets/data_driven_schema_configs/conf/solrconfig.xml

sudo -u hdfs hadoop fs -mkdir /user/solr
sudo -u hdfs hadoop fs -chown solr /user/solr

/bin/cp -f /root/hdp22-twitter-demo/default.json /opt/lucidworks-hdpsearch/solr/server/solr-webapp/webapp/banana/app/dashboards/

chown -R solr:solr /opt/lucidworks-hdpsearch/solr
sudo -u solr /opt/lucidworks-hdpsearch/solr/bin/solr start -c -z localhost:2181

sleep 5

sudo -u solr /opt/lucidworks-hdpsearch/solr/bin/solr create -c tweets \
   -d data_driven_schema_configs \
   -s 1 \
   -rf 1

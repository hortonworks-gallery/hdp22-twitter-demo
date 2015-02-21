adduser solr
echo solr | passwd solr --stdin
mkdir /opt/solr

su -l hdfs -c "hadoop fs -mkdir -p /user/solr"
su -l hdfs -c "hadoop fs -chown solr /user/solr"

cd /opt/solr
wget  http://www.interior-dsgn.com/apache/lucene/solr/4.10.2/solr-4.10.2.tgz
tar xzf solr-4.10.2.tgz
ln -s solr-4.10.2 latest
rm -rf solr-*.tgz
cp -r /opt/solr/latest/example /opt/solr/latest/hdp
rm -rf /opt/solr/latest/hdp/example* /opt/solr/latest/hdp/multicore /opt/solr/latest/hdp/solr

cp -r /opt/solr/latest/example/example-schemaless/solr /opt/solr/latest/hdp/solr
mv /opt/solr/latest/hdp/solr/collection1 /opt/solr/latest/hdp/solr/tweets
cp -r /opt/solr/latest/example/solr/collection1/conf/admin-*.html /opt/solr/latest/hdp/solr/tweets/conf
rm -rf /opt/solr/latest/hdp/solr/tweets/core.properties

chown -R solr:solr /opt/solr

mv /usr/lib/storm/lib/httpclient-4.1.1.jar /usr/lib/storm/lib/httpclient-4.1.1.jar.bak
mv /usr/lib/storm/lib/httpcore-4.1.jar /usr/lib/storm/lib/httpcore-4.1.jar.bak

mv /opt/solr/latest/hdp/solr/tweets/conf/solrconfig.xml /opt/solr/latest/hdp/solr/tweets/conf/solrconfig.xml.bak
cp ~/hdp22-twitter-demo/solrconfig.xml /opt/solr/latest/hdp/solr/tweets/conf/


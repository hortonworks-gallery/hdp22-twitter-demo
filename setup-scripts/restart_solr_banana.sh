echo "Rebuilding banana webapp..."
cd /opt/banana/latest/src/app/dashboards
/bin/cp -f ~/hdp22-twitter-demo/default.json .

rm -f /opt/banana/latest/build/*
cd /opt/banana/latest
ant
cp -f /opt/banana/latest/build/banana*.war /opt/solr/latest/hdp/webapps/banana.war
cp -f /opt/banana/latest/jetty-contexts/banana-context.xml /opt/solr/latest/hdp/contexts/

echo "Resetting Solr...."
ps -ef | grep sol[r] | awk '{print $2}' | sudo xargs kill
rm  -f /opt/solr/latest/hdp/solr/tweets/core.properties 
sudo -u hdfs hadoop fs -rm -r /user/solr/tweets
sudo -u hdfs hadoop fs -mkdir /user/solr/tweets
sudo -u hdfs hadoop fs -chmod -R 777 /user/solr
nohup java -jar /opt/solr/latest/hdp/start.jar -Djetty.home=/opt/solr/latest/hdp -Dsolr.solr.home=/opt/solr/latest/hdp/solr &> /root/hdp22-twitter-demo/logs/solr.out &
sleep 30
curl "http://localhost:8983/solr/admin/cores?action=CREATE&name=tweets&instanceDir=/opt/solr/latest/hdp/solr/tweets"


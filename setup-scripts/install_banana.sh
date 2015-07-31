mkdir /opt/banana
chown solr:solr /opt/banana
cd /opt/banana
#wget  https://github.com/LucidWorks/banana/archive/banana-1.4.tar.gz
#tar xzf banana-1.4.tar.gz
git clone https://github.com/LucidWorks/banana.git
mv banana latest
sed -i 's/localhost/sandbox.hortonworks.com/g' /opt/banana/latest/src/config.js
sed -i 's/logstash_logs/tweets/g' /opt/banana/latest/src/config.js
cd /opt/banana/latest/src/app/dashboards
/bin/cp -f ~/hdp22-twitter-demo/default.json .

#build banana webapp
cd /opt/banana/latest
mkdir build
yum -y install ant
ant
/bin/cp -f /opt/banana/latest/build/banana*.war /opt/solr/latest/hdp/webapps/banana.war
/bin/cp -f /opt/banana/latest/jetty-contexts/banana-context.xml /opt/solr/latest/hdp/contexts/

#start solr
echo "Starting Solr..."
nohup java -jar /opt/solr/latest/hdp/start.jar -Djetty.home=/opt/solr/latest/hdp -Dsolr.solr.home=/opt/solr/latest/hdp/solr &> /root/hdp22-twitter-demo/logs/solr.out &
echo "Waiting for Solr to start..."
sleep 10
curl "http://localhost:8983/solr/admin/cores?action=CREATE&name=tweets&instanceDir=/opt/solr/latest/hdp/solr/tweets"


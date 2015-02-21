set -e 
KAFKA_HOME=/usr/hdp/2.2.0.0-2041/kafka

rpmdb --rebuilddb
/root/start_ambari.sh
echo "Starting HBase..."
curl -u admin:admin -i -H 'X-Requested-By: ambari' -X PUT -d '{"RequestInfo": {"context" :"Start HBASE via REST"}, "Body": {"ServiceInfo": {"state": "STARTED"}}}' http://localhost:8080/api/v1/clusters/Sandbox/services/HBASE
echo "Starting Kafka..."
curl -u admin:admin -i -H 'X-Requested-By: ambari' -X PUT -d '{"RequestInfo": {"context" :"Start KAFKA via REST"}, "Body": {"ServiceInfo": {"state": "STARTED"}}}' http://localhost:8080/api/v1/clusters/Sandbox/services/KAFKA
echo "Starting Storm..."
curl -u admin:admin -i -H 'X-Requested-By: ambari' -X PUT -d '{"RequestInfo": {"context" :"Start STORM via REST"}, "Body": {"ServiceInfo": {"state": "STARTED"}}}' http://localhost:8080/api/v1/clusters/Sandbox/services/STORM

TOPICS=`$KAFKA_HOME/bin/kafka-topics.sh --zookeeper sandbox.hortonworks.com:2181 --list | wc -l`
if [ $TOPICS == 0 ]
then
	echo "No Kafka topics found...creating..."
	$KAFKA_HOME/bin/kafka-topics.sh --create --zookeeper sandbox.hortonworks.com:2181 --replication-factor 1 --partitions 1 --topic twitter_events	
fi

mkdir /root/hdp22-twitter-demo/logs
find /root/hdp22-twitter-demo -iname '*.sh' | xargs chmod +x
echo "Installing mvn..."
/root/hdp22-twitter-demo/setup-scripts/install_mvn.sh > /root/hdp22-twitter-demo/logs/install_mvn.log
echo "Installing Solr..."
/root/hdp22-twitter-demo/setup-scripts/install_solr.sh > /root/hdp22-twitter-demo/logs/install_solr.log
echo "Installing Banana..."
/root/hdp22-twitter-demo/setup-scripts/install_banana.sh > /root/hdp22-twitter-demo/logs/install_banana.log
echo "Installing Phoenix"
/root/hdp22-twitter-demo/setup-scripts/install_phoenix.sh > /root/hdp22-twitter-demo/logs/install_phoenix.log

echo "Setup complete. Logs available under /root/hdp22-twitter-demo/logs"



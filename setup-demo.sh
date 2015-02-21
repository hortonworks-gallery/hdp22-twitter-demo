set -e 
rpmdb --rebuilddb
/root/start_ambari.sh
echo "Starting HBase..."
curl -u admin:admin -i -H 'X-Requested-By: ambari' -X PUT -d '{"RequestInfo": {"context" :"Start HBASE via REST"}, "Body": {"ServiceInfo": {"state": "STARTED"}}}' http://localhost:8080/api/v1/clusters/Sandbox/services/HBASE
echo "Starting Kafka..."
curl -u admin:admin -i -H 'X-Requested-By: ambari' -X PUT -d '{"RequestInfo": {"context" :"Start KAFKA via REST"}, "Body": {"ServiceInfo": {"state": "STARTED"}}}' http://localhost:8080/api/v1/clusters/Sandbox/services/KAFKA
echo "Starting Storm..."
curl -u admin:admin -i -H 'X-Requested-By: ambari' -X PUT -d '{"RequestInfo": {"context" :"Start STORM via REST"}, "Body": {"ServiceInfo": {"state": "STARTED"}}}' http://localhost:8080/api/v1/clusters/Sandbox/services/STORM

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



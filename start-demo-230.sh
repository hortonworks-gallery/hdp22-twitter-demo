source ambari_util.sh

AMBARI_STARTED=`ps -ef | grep AmbariServe[r] | wc -l`
if [ $AMBARI_STARTED -eq 0 ]
then
	echo 'Starting Ambari'
	if [ -f /root/start_ambari.sh ]
	then
		/root/start_ambari.sh
	else
		ambari-server start
		ambari-agent start
	fi
	sleep 5
fi

service ranger-admin start

echo '*** Starting Storm....'
startWait STORM

echo '*** Starting HBase....'
startWait HBASE

echo '*** Starting kafka....'
startWait KAFKA

/root/hdp22-twitter-demo/setup-scripts/restart_solr_banana.sh
cd ~/hdp22-twitter-demo/stormtwitter-mvn
if [ ! -f target/storm-streaming-1.0-SNAPSHOT.jar ]
then
	echo "First compile detected, running mvn purge local"
	mvn dependency:purge-local-repository
else
	echo "Uber jar found, running regular mvn compile"	
fi	
./runtopology.sh


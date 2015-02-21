if [ `ps -ef |  grep kafka.Kafk[a] | wc -l` -gt 0 ]
then
        echo "Detected Kafka is already running"
else
        echo "Kafka is down...restarting"
        nohup /opt/kafka/latest/bin/kafka-server-start.sh /opt/kafka/latest/config/server.properties &
        sleep 5
fi

/root/hdp22-twitter-demo/setup-scripts/restart_solr_banana.sh
cd ~/hdp22-twitter-demo/stormtwitter-mvn
./runtopology.sh

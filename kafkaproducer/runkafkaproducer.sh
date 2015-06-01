#Usage:
#./runkafkaproducer.sh - regular mode
#./runkafkaproducer.sh debug - this avoids streaming full JSON doc for ease of debugging
 
MODE=$1
KAFKA_HOME=/usr/hdp/current/kafka-broker/
yum install -y ntp
if [ "$MODE" == "" ]
then
	MODE="nondebug"
fi

if [ `ps -ef |  grep kafka.Kafk[a] | wc -l` -gt 0 ]
then
	echo "Detected Kafka is already running"
else
	echo "Kafka is down...restarting"
	nohup /opt/kafka/latest/bin/kafka-server-start.sh /opt/kafka/latest/config/server.properties &
	sleep 5
fi
	
TOPICS=`$KAFKA_HOME/bin/kafka-topics.sh --zookeeper sandbox.hortonworks.com:2181 --list | wc -l`
if [ $TOPICS == 0 ]
then
	echo "No Kafka topics found...creating..."
	$KAFKA_HOME/bin/kafka-topics.sh --create --zookeeper sandbox.hortonworks.com:2181 --replication-factor 1 --partitions 1 --topic twitter_events	
fi

echo "Compiling jar..."
cd /root/hdp22-twitter-demo/kafkaproducer
rm -f producertest.jar
rm -rf classes
mkdir classes
export CLASSPATH=$KAFKA_HOME/libs/\*:./\*
javac  -d classes *.java
jar -cvf producertest.jar -C classes/ .

echo "Fixing system time..."
service ntpd stop
ntpdate pool.ntp.org
service ntpd start

java  example.producer.TestProducer $MODE


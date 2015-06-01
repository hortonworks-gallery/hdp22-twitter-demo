KAFKA_HOME=/usr/hdp/current/kafka-broker/
MODE=$1
if [ "$MODE" == "" ]
then
        MODE="nondebug"
fi

export CLASSPATH=$KAFKA_HOME/libs/\*:./\*
java  example.producer.TestProducer $MODE


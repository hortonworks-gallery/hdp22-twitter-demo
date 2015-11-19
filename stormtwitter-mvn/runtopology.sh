#Usage:
#./runtopology.sh - runs in regular mode: run topology on cluster without cleanup
#./runtopology.sh runOnCluster clean - run topology on cluster after recreating HDFS, hive, HBase, Solr artifacts
#./runtopology.sh runLocally clean - runs topology locally after cleaning up HDFS/Hivea

mvn clean install


#Possible values are "runLocally" to run topology locally or "runOnCluster" anything else to run on cluster
STORMMODE=$1
if [ "$STORMMODE" == "" ]
then
        STORMMODE="runOnCluster"
fi

#Possible value are "clean" to clean Hive tables and HDFS or "skipclean" to skip cleaning
HDFSMODE=$2

if [ "$HDFSMODE" == "" ]
then
        HDFSMODE="skipclean"
fi


#echo "Killing kafka-hdfs-topology...."
#storm kill kafka-hdfs-topology

#echo "Sleeping for 20 sec..."
#sleep 20

if [ "$HDFSMODE" == "clean" ]
then
	/root/hdp22-twitter-demo/reset-demo.sh
fi
echo "Starting toplogy ..."
host=$(hostname -f)
storm jar ./target/storm-streaming-1.0-SNAPSHOT.jar hellostorm.GNstorm $STORMMODE $host



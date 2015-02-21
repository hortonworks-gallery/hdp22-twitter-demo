package hellostorm;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.StormSubmitter;
import backtype.storm.cluster.StormClusterState;
import backtype.storm.topology.TopologyBuilder;
import backtype.storm.spout.RawMultiScheme;
import storm.kafka.ZkHosts;
import storm.kafka.KafkaSpout;
import storm.kafka.SpoutConfig;
import storm.kafka.BrokerHosts;

import backtype.storm.spout.SchemeAsMultiScheme;
import org.apache.storm.hdfs.bolt.HdfsBolt;
import org.apache.storm.hdfs.bolt.format.DefaultFileNameFormat;
import org.apache.storm.hdfs.bolt.format.DelimitedRecordFormat;
import org.apache.storm.hdfs.bolt.format.FileNameFormat;
import org.apache.storm.hdfs.bolt.format.RecordFormat;
import org.apache.storm.hdfs.bolt.rotation.FileRotationPolicy;
import org.apache.storm.hdfs.bolt.rotation.FileSizeRotationPolicy;
import org.apache.storm.hdfs.bolt.rotation.FileSizeRotationPolicy.Units;
import org.apache.storm.hdfs.bolt.sync.CountSyncPolicy;
import org.apache.storm.hdfs.bolt.sync.SyncPolicy;
import org.apache.storm.hdfs.common.rotation.MoveFileAction;

//import com.hortonworks.streaming.impl.bolts.hdfs.FileTimeRotationPolicy;
//import com.hortonworks.streaming.impl.bolts.hive.HiveTablePartitionAction;
import hellostorm.HiveTablePartitionAction;
import hellostorm.TwitterScheme;
import hellostorm.TwitterRuleBolt;
//import hellostorm.FileTimeRotationPolicy;

public class GNstorm {

	public static void main(String[] args) throws Exception{
		String argument = args[0];
		Config config = new Config();
		config.setDebug(true);
		config.put(Config.TOPOLOGY_MAX_SPOUT_PENDING, 1);
		//set the number of workers
		config.setNumWorkers(2);
		
		TopologyBuilder builder = new TopologyBuilder();

		//Setup Kafka spout
		BrokerHosts hosts = new ZkHosts("sandbox.hortonworks.com:2181");
		String topic = "twitter_events";
		String zkRoot = "";
		String consumerGroupId = "group1";
		SpoutConfig spoutConfig = new SpoutConfig(hosts, topic, zkRoot, consumerGroupId);
		spoutConfig.scheme = new RawMultiScheme();
		spoutConfig.scheme = new SchemeAsMultiScheme(new TwitterScheme());
		KafkaSpout kafkaSpout = new KafkaSpout(spoutConfig);
		builder.setSpout("KafkaSpout", kafkaSpout);		


		//Setup HDFS bolt
		String rootPath = "/tweets";
		String prefix = "tweets";
		String fsUrl = "hdfs://sandbox.hortonworks.com:8020";
		String sourceMetastoreUrl = "thrift://sandbox.hortonworks.com:9083";
		String hiveStagingTableName = "tweets_text_partition";
		String databaseName = "default";
		Float rotationTimeInMinutes = Float.valueOf("60");
		
		//this has to match the delimiter in the sql script that creates the Hive table
		RecordFormat format = new DelimitedRecordFormat().withFieldDelimiter("|");

		//Synchronize data buffer with the filesystem every 1000 tuples
		SyncPolicy syncPolicy = new CountSyncPolicy(5);

		// Rotate data files when they reach five MB
		FileRotationPolicy rotationPolicy = new FileSizeRotationPolicy(5.0f, Units.MB);
		
		//Rotate every X minutes
		//FileTimeRotationPolicy rotationPolicy = new FileTimeRotationPolicy(rotationTimeInMinutes, FileTimeRotationPolicy.Units.MINUTES);
		
		//Hive Partition Action
		HiveTablePartitionAction hivePartitionAction = new HiveTablePartitionAction(sourceMetastoreUrl, hiveStagingTableName, databaseName, fsUrl);
		
		//MoveFileAction moveFileAction = new MoveFileAction().toDestination(rootPath + "/working");
		
		FileNameFormat fileNameFormat = new DefaultFileNameFormat()
				.withPath(rootPath + "/staging")
				.withPrefix(prefix);

		// Instantiate the HdfsBolt
		HdfsBolt hdfsBolt = new HdfsBolt()
				 .withFsUrl(fsUrl)
		         .withFileNameFormat(fileNameFormat)
		         .withRecordFormat(format)
		         .withRotationPolicy(rotationPolicy)
		         .withSyncPolicy(syncPolicy)
		         .addRotationAction(hivePartitionAction);
		
		//hdfsbolt.thread.count this determines how many HDFS entries are created
		int hdfsBoltCount = 4;

		//defind the topology grouping
		builder.setBolt("HDFSBolt", hdfsBolt, hdfsBoltCount).shuffleGrouping("KafkaSpout");

        	builder.setBolt("FileWriterbolt", new FileWriterBolt()).shuffleGrouping("KafkaSpout");
        
       		builder.setBolt("TwitterRuleBolt", new TwitterRuleBolt()).allGrouping("KafkaSpout");

		builder.setBolt("SolrBolt", new SolrBolt()).shuffleGrouping("KafkaSpout"); 
	
		if (argument.equalsIgnoreCase("runLocally")){
			System.out.println("Running topology locally...");
			LocalCluster cluster = new LocalCluster();
			cluster.submitTopology("Test Storm", config, builder.createTopology());
		}
		else {
			System.out.println("Running topology on cluster...");
			StormSubmitter.submitTopology("Twittertopology", config, builder.createTopology()); 
		}
		
	}
	
}

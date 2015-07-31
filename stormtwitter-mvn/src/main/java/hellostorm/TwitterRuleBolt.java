package hellostorm;

import java.sql.Timestamp;
import java.util.concurrent.*;
import java.util.concurrent.atomic.*;
import java.util.Properties;
import java.util.TimeZone;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;

import java.util.Map;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Driver;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Date;
import java.text.SimpleDateFormat;

import org.apache.phoenix.jdbc.PhoenixDriver;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrInputDocument;


public class TwitterRuleBolt implements IRichBolt {
	
	private static final long serialVersionUID = 6816706717943197642L;
	private static final int MONITOR_INTERVAL_SECS = 20;

	//default threshold value in case threshold not available in Hbase table for a stock
	private static final int DEFAULT_ALERT_THRESHOLD = 5;
		
	private OutputCollector collector;
	//private TruckEventRuleEngine ruleEngine;
	//private static final Driver phoenixDriver = null;
	private static Driver phoenixDriver = new PhoenixDriver();
	Connection conn = null;
	
	private ConcurrentMap<String, AtomicInteger> twitterEvents = new ConcurrentHashMap();
	

	public TwitterRuleBolt() {
        Timer timer = new Timer();  //At this line a new Thread will be created
        timer.schedule(new RemindTask(twitterEvents), 0, MONITOR_INTERVAL_SECS *1000); //delay in milliseconds
	}

	
	@Override
	public void prepare(Map stormConf, TopologyContext context,
			OutputCollector collector) {
		this.collector = collector;

	}

	//For each Tweet that is read, check threshold of the stock and apply 
	@Override
	public void execute(Tuple input) {
		String userId = input.getString(0);
		String displayname = input.getString(1);
		String hashtag_all = input.getString(2);
		String tweet = input.getString(3);		
		String created = input.getString(4);
		String longitude = input.getString(5);
		String latitude = input.getString(6);
		String language = input.getString(7);		
		String fullTweet = input.getString(8);
		
							
		if (hashtag_all.length() == 0){
			System.out.println("Skipping tweet...unable to find hashtag from it:" + tweet);
                        collector.ack(input);
			return;
		}
		
		String hashtags[] = hashtag_all.split(" ");
		
		for (String hashtag : hashtags){
				
			System.out.println("RuleBolt received event displayname: " + displayname + " hashtag: " + hashtag + " tweet: " + tweet );
			//double latitude = input.getDoubleByField("latitude");
			//long correlationId = input.getLongByField("correlationId");
			//int truckId = input.getIntegerByField("truckId");
			
			//save event to our Map of events and retrive how many times its been mentioned in tweets tweeted
	   		 twitterEvents.putIfAbsent(hashtag, new AtomicInteger(0));
	   		 int numTimesStockTweeted = twitterEvents.get(hashtag).incrementAndGet();
	
			//query HBase table for threshold for the stock symbol that was tweeted about
			int threshold = findThresholdForStock(hashtag);
			//int threshold = DEFAULT_ALERT_THRESHOLD;
	               
	   		 System.out.println("\n\n\n\n\n\n\nStock: " + hashtag + " now has count: " + numTimesStockTweeted + ", threshold = " + threshold + " structure: " + twitterEvents + "\n\n\n\n\n\n\n");
	
			//check if this event takes the tweet volume for this stock above threshold
			if (numTimesStockTweeted > threshold) {
				int unixTime = (int) (System.currentTimeMillis() / 1000L);
				String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
				String upsert = String.format("upsert into alerts values(%d, '%s', '%s', %d)",unixTime, hashtag, timeStamp, numTimesStockTweeted);
				System.out.println("ALERT!!! Stock: " + hashtag + " exceeded limit: " + threshold + " as it has count: " + numTimesStockTweeted + " on: " + timeStamp);
						
				runHbaseUpsert(upsert);
									
				createSolrAlert(userId, created,hashtag);
			}
			
		}
		    		
		collector.ack(input);
					
	}

	private void runHbaseUpsert(String upsert){
	
			try {	
				conn.createStatement().executeUpdate(upsert);
				conn.commit();
			}
			catch(Exception e)
			{
				e.printStackTrace();
				throw new RuntimeException(e);
			}			
			finally
			{
				try
				{
					if(conn != null)
						conn.close();
				}
				catch(Exception e)
				{
					throw new RuntimeException(e);
				}
				
			}		
	}
	
	
	//query HBase table for threshold for the stock symbol that was tweeted about
	private int findThresholdForStock(String hashtag){

		int threshold = DEFAULT_ALERT_THRESHOLD;
	
		try {
			conn = phoenixDriver.connect("jdbc:phoenix:localhost:2181:/hbase-unsecure",new Properties());	

			ResultSet rst = conn.createStatement().executeQuery("select tweet_threshold from securities where lower(symbol)='"+hashtag+"'");
		    				
		    
			while (rst.next()) {
				threshold = rst.getInt(1);
				System.out.println("Found threshold in Hbase: " + threshold + " for: " + hashtag);
			}

			if (threshold == 0){
				System.out.println("Unable to find threshold in HBase for: " + hashtag + ". Setting to default: " + DEFAULT_ALERT_THRESHOLD);
				threshold = DEFAULT_ALERT_THRESHOLD;			
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
			throw new RuntimeException(e);

		}
		finally
		{
			try
			{
				if(conn != null)
					conn.close();
			}
			catch(Exception e)
			{
				throw new RuntimeException(e);
			}
				
		}
		
		return threshold;
	
	}

	private static void createSolrAlert(String userId, String created, String hashtag){

		Date  d=new Date(created);
		SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
		created = formatter.format(d);	
	
		HttpSolrServer server  = new HttpSolrServer("http://sandbox.hortonworks.com:8983/solr/tweets");
		//create a tweet doc
		SolrInputDocument doc = new SolrInputDocument();

        	doc.addField("id", userId + ":" + hashtag + "-" + created);
        	doc.addField("doctype_s", "alert");
        	doc.addField("createdAt_dt", created);
		doc.addField("tag_s", hashtag);	
		
		try{
			server.add(doc);
			System.out.println("SolrBolt: successfully added alert to Solr server" + hashtag);
		} catch (Exception e) {
			e.printStackTrace();
			//collector.fail(input);
		}
	
	
	
	}
	
	@Override
	public void cleanup() {
		//ruleEngine.cleanUpResources();
		


	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
	}

	@Override
	public Map<String, Object> getComponentConfiguration() {
		return null;
	}
}

   class RemindTask extends TimerTask {
		private ConcurrentMap<String, AtomicInteger> twitterEvents;
		
		public RemindTask(ConcurrentMap<String, AtomicInteger> twitterEvents){
			this.twitterEvents = twitterEvents;
		}
        @Override
        public void run() {
            twitterEvents = new ConcurrentHashMap();
            System.out.println("Timer completed! Resetting counts to 0. Struct: " + twitterEvents);
            
        }
    }



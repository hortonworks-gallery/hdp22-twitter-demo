package hellostorm;

import java.io.File;
import java.io.FileWriter;
import java.util.Map;
import java.util.Arrays;
import java.util.Properties;
import java.util.TimeZone;

import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Tuple;
import backtype.storm.task.OutputCollector;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Driver;
import java.sql.SQLException;

import org.apache.phoenix.jdbc.PhoenixDriver;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrInputDocument;

import java.util.Date;
import java.text.BreakIterator;
import java.text.SimpleDateFormat;

public class SolrBolt extends BaseBasicBolt {

	private SolrServer server = null;

	private static Driver phoenixDriver = new PhoenixDriver();
	Connection conn = null;

	@Override
	public void prepare(Map stormConf, TopologyContext context) {
	
		server = new HttpSolrServer("http://sandbox.hortonworks.com:8983/solr/tweets");
		try{
			conn = phoenixDriver.connect("jdbc:phoenix:localhost:2181:/hbase-unsecure",new Properties());
		}catch (SQLException e){
			System.err.println("error when connection to phoenix" + e.toString());
		}

	}

	@Override
	public void execute(Tuple input, BasicOutputCollector collector) {
 
 		String userId = input.getString(0);
		String displayname = input.getString(1);
		String hashtag = input.getString(2);
		String tweet = input.getString(3);		
		String created = input.getString(4);
		String longitude = input.getString(5);
		String latitude = input.getString(6);
		String language = input.getString(7);		
		String fullTweet = input.getString(8);


		Date  d=new Date(created);
		//SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z+9HOUR'");
		//SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z-4HOUR'");
		SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
		created = formatter.format(d);

		//create a tweet doc
		SolrInputDocument doc = new SolrInputDocument();

	        doc.addField("id", userId + ":" + tweet);
        	doc.addField("doctype_s", "tweet");
       		doc.addField("createdAt_dt", created);
        	doc.addField("text_t", tweet);
            doc.addField("tag_ss", Arrays.asList(hashtag.split(" ")));
        	doc.addField("language_s", language);
        	doc.addField("screenName_s", displayname);
        	doc.addField("source_s", "");
        	doc.addField("location_s", "");
        	doc.addField("polarity_s", getPolarity(tweet));
        	doc.addField("polarityCount_i", 1);

        	
		
		try{
			server.add(doc);
			System.out.println("SolrBolt: successfully added tweet to Solr server");
		} catch (Exception e) {
			e.printStackTrace();
			//collector.fail(input);
		}


	}

	private String getPolarity(String tweet){

		String polarity = "neutral";
		tweet = tweet.toLowerCase();
		BreakIterator wi = BreakIterator.getWordInstance();
		StringBuffer strBuffer = new StringBuffer();
		wi.setText(tweet);
		int widx = 0;
		while(wi.next() != BreakIterator.DONE) {
			String word = tweet.substring(widx, wi.current());
			widx = wi.current();
			if(Character.isLetterOrDigit(word.charAt(0))) {
				word = word.replaceAll("'", "\\\\'");
				strBuffer.append("word = '" +  word + "' OR ");
			}
		}


		try {
			String query = "select case when positive > negative then 'positive' when negative > positive then 'negative' else 'neutral' end as polarity from (select coalesce(sum(case when polarity='positive' then 1 end),0) as positive, coalesce(sum(case when polarity='negative' then 1 end),0) as negative from dictionary where (" + strBuffer.substring(0, strBuffer.length()-4) + ")) t1";
			System.out.println(query);
			ResultSet rst = conn.createStatement().executeQuery(query);

			while (rst.next()) {
				polarity = rst.getString(1);
			}

		}
		catch(Exception e){
			e.printStackTrace();
			//ERROR SELECTION POLARITY IGNOR
			System.err.println("Error selection polarity, erro will be ignored and polarity will be considered 'neutral'. error: " + e.toString());
		}

		return polarity;
	}

	
    @Override
	public void cleanup() {
		// TODO Auto-generated method stub
		super.cleanup();
		try{
			conn.close();
		}catch (SQLException e){
			System.err.println("Error when closing phoenix connection " +  e.toString());
		}
		conn = null;
	}

	@Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        // no-op
    }

}


package hellostorm;

import java.io.File;
import java.io.FileWriter;
import java.util.Map;

import backtype.storm.topology.BasicOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.base.BaseBasicBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Tuple;
import backtype.storm.task.OutputCollector;

import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrInputDocument;

import java.util.Date;
import java.text.SimpleDateFormat;

public class SolrBolt extends BaseBasicBolt {

	private SolrServer server = null;

	@Override
	public void prepare(Map stormConf, TopologyContext context) {
	
		server = new HttpSolrServer("http://sandbox.hortonworks.com:8983/solr/tweets");
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
		SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss'Z'");
		created = formatter.format(d);

		//create a hashtag doc
		SolrInputDocument hashtagDoc = new SolrInputDocument();
       		 hashtagDoc.addField("id", userId + ":" + "hashtag");
        	hashtagDoc.addField("doctype_s", "hashtag");
        	hashtagDoc.addField("createdAt_dt", created);
        	hashtagDoc.addField("tag_s", hashtag);		


		try{
			server.add(hashtagDoc);	
			System.out.println("SolrBolt: successfully added hastag to Solr server");
				
		} catch (Exception e) {
			e.printStackTrace();			
		}		

		//create a tweet doc
		SolrInputDocument doc = new SolrInputDocument();

	        doc.addField("id", userId + ":" + "tweet");
        	doc.addField("doctype_s", "tweet");
       		 doc.addField("createdAt_dt", created);
        	doc.addField("text_t", tweet);
        	doc.addField("language_s", language);
        	doc.addField("screenName_s", displayname);
        	doc.addField("source_s", "");
        	doc.addField("location_s", "");
		
		try{
			server.add(doc);
			System.out.println("SolrBolt: successfully added tweet to Solr server");
		} catch (Exception e) {
			e.printStackTrace();
			//collector.fail(input);
		}


	}

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        // no-op
    }

}


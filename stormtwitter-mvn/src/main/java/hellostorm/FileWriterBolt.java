package hellostorm;

import java.io.File;
import java.io.FileWriter;
import java.util.Map;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.IRichBolt;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.tuple.Tuple;

public class FileWriterBolt implements IRichBolt{

	private OutputCollector collector;
	private FileWriter out;
	
	@Override
	public void prepare(Map stormConf, TopologyContext context,
			OutputCollector collector) {
		this.collector = collector;
	
		try
		{
			out = new FileWriter(new File("/tmp/Tweets.xls"));
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void execute(Tuple input) {
		try
		{
			//write out the tweets in tab delimited format to local disk
			String str = input.getString(1) + "\t" +input.getString(2) + "\t" +input.getString(3) + "\n";
			out.write(str);
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
		
		collector.ack(input);
	}

	@Override
	public void cleanup() {
		System.out.println("exiting topology...");
		try
		{
			out.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}

	@Override
	public void declareOutputFields(OutputFieldsDeclarer declarer) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Map<String, Object> getComponentConfiguration() {
		// TODO Auto-generated method stub
		return null;
	}

}

package example.producer;

import java.util.*;
import java.io.*;
import kafka.javaapi.producer.Producer;
import kafka.producer.KeyedMessage;
import kafka.producer.ProducerConfig;
import twitter4j.*;
 
import java.io.BufferedReader;
import java.io.IOException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TestProducer {


	//max number of hashtags allowed by Twitter streaming APIs filter
	private static int MAX_NUM_TWITTER_HASHTAG = 400;
	
	//Location of CSV file from where to read stocks
	private static String CSV_LOCATION = "/root/hdp22-twitter-demo/fetchSecuritiesList/securities.csv";
	
	//Delimitor used in CSV file
	private static String FILE_DELIMITOR = ",";
	
	public static String[] fetchHashtags() {
	
		String csvFileToRead = CSV_LOCATION;
  		BufferedReader br = null;
  		String line = "";
  		String splitBy = FILE_DELIMITOR;
  		String[] hashtags = null;
  		int count = 0;
  		List<String> stockSymbolList = new ArrayList<String>();

  		try {

			System.out.println("Reading stock symbols to send to Twitter from " + CSV_LOCATION);
   			br = new BufferedReader(new FileReader(csvFileToRead));
  			while ((line = br.readLine()) != null && count < MAX_NUM_TWITTER_HASHTAG) {

    			// split on comma(',')
    			String[] splitLine = line.split(splitBy);

				//The first column in the CSV is the stock symbol
				String hashtag = splitLine[0];
				
    			// adding hashtag objects to a list
    			stockSymbolList.add(hashtag);
    			
    			System.out.println("Adding to hashtag filters: " + hashtag);
    			
    			count++;
			}

			System.out.println("Read " + count + " hashtags to send to Twitter");
			hashtags = stockSymbolList.toArray(new String[stockSymbolList.size()]);
	
		
		} catch (FileNotFoundException e) {
			System.out.println("No file found under " + CSV_LOCATION + ". Producing stream of all Tweets. ");
   			//e.printStackTrace();
  		} catch (IOException e) {
   			e.printStackTrace();
  		} finally {
   			if (br != null) {
    			try {
     				br.close();
    				} catch (IOException e) {
     						e.printStackTrace();
    				}
   			}
  		}
  		
  		return hashtags;	
 	}
	
	
	public static String matchRegex(String url, String regex) {
		String result = "";
		Matcher matcher = Pattern.compile(regex).matcher(url);
		if (matcher.find()) {			
			result = matcher.group(1);
			result = result.trim();
		}

		return result;
	}

    /*private static void getConnections(long id) throws TwitterException {
    	Twitter twitter = new TwitterFactory().getInstance();

    	long lCursor = -1;
    	IDs friendsIDs = twitter.getFriendsIDs(id, lCursor);
    	System.out.println(twitter.showUser(id).getName());
    	System.out.println("==========================");
   		do
   		{
   	   		for (long i : friendsIDs.getIDs())
       		{
           		System.out.println("follower ID #" + i);
           		System.out.println("follower name: " + twitter.showUser(i).getScreenName());
       		}
    	}while(friendsIDs.hasNext());
	}*/
		
    public static void main(String[] args) throws TwitterException {
    	final String mode=args[0];

    	System.out.println("Mode is " + mode);
	String[] hashtags = fetchHashtags();
	
        Properties props = new Properties();
        props.put("metadata.broker.list", "sandbox.hortonworks.com:9092");
        props.put("serializer.class", "kafka.serializer.StringEncoder");
        props.put("partitioner.class", "example.producer.SimplePartitioner");
        props.put("request.required.acks", "1");
	props.put("retry.backoff.ms", "150");
	props.put("message.send.max.retries","10");
	props.put("topic.metadata.refresh.interval.ms","0");
 
        ProducerConfig config = new ProducerConfig(props);
 
        final Producer<String, String> producer = new Producer<String, String>(config);


	//Listener that is invokes every time a tweet containing one of the hashtags is created
     	StatusListener listener = new StatusListener(){
        	public void onStatus(Status status) {
				//String userName = status.getUser().getName().replace("\n", "").replace("\r", "").replace("|","").replace(",","");
				String userId = Long.toString(status.getUser().getId());
				String displayname = status.getUser().getScreenName().replace("\n", "").replace("\r", "").replace("|","").replace(",","");
				String tweet = status.getText().replace("\n", "").replace("\r", "").replace("|","").replace(",","");
				Date created = status.getCreatedAt();
				String fullTweet = status.toString().replace("\n", "").replace("\r", "").replace("|","");
				String language = status.getLang();	 

				String inReplyToScreenName = status.getInReplyToScreenName();
				double longitude = 0; 
				double latitude = 0; 
				if (status.getGeoLocation() != null){
					longitude = status.getGeoLocation().getLongitude();
					latitude = status.getGeoLocation().getLatitude();
				}
								
				
				System.out.format("TwitterKafkaProducer: userid: %s displayname: %s inreplyto: %s date: %s long/lat %f / %f tweet: %s %s\n", userId, displayname, inReplyToScreenName, created.toString(), longitude, latitude, tweet, (mode.equalsIgnoreCase("debug")) ? fullTweet : "");


				KeyedMessage<String, String> data = new KeyedMessage<String, String>("twitter_events",userId+"||"+displayname+"||"+tweet+"||"+created+"||"+longitude+"||"+latitude+"||"+language+"||"+fullTweet);
				producer.send(data);
        	}
        	
        	public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {}
        	public void onTrackLimitationNotice(int numberOfLimitedStatuses) {}
        	public void onException(Exception ex) {
            		ex.printStackTrace();
       		 }
 		public void onStallWarning(StallWarning warning) {
			System.out.println("Got stall warning:" + warning);
		}
 		public void onScrubGeo(long userId, long upToStatusId) {
			System.out.println("Got scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
		}
    	};



        final TwitterStream twitterStream = new TwitterStreamFactory().getInstance();
        twitterStream.addListener(listener);

	//create a query to filter the tweets by hashtag and geolocation
	FilterQuery tweetFilterQuery = new FilterQuery();
	//tweetFilterQuery.locations(new double[][]{new double[]{-122.75,36.8}, new double[]{-121.75,37.8}});

	//if hashtag list found send it over
	if (hashtags != null && hashtags.length > 0){
		tweetFilterQuery.track(hashtags);
		twitterStream.filter(tweetFilterQuery);
	}
	//otherwise just read TwitterStream
	else
        	// sample() method internally creates a thread which manipulates TwitterStream and calls these adequate listener methods continuously.
        	twitterStream.sample();

	Runtime.getRuntime().addShutdownHook(new Thread() {
    	public void run() { 
		System.out.println("Shutting down producer...");
  		producer.close();
		//twitterStream.close();
		}
 	});


    }
}

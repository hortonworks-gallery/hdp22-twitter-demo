package hellostorm;

import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Date;
import java.text.SimpleDateFormat;
import backtype.storm.spout.Scheme;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Values;

import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class TwitterScheme implements Scheme{

	private static final long serialVersionUID = -2990121166902741545L;

	private static String findSearch(String tweet){
		String hashtag = "";
		//look for $hashtags or #hashtags
		Matcher matcher = Pattern.compile("((\\$|\\#)[\\p{L}]+)").matcher(tweet);
		while (matcher.find())
			hashtag += matcher.group(1).toLowerCase().trim() + " ";
		return hashtag;
	}

	@Override
	public List<Object> deserialize(byte[] bytes) {
		try {
			String twitterEvent = new String(bytes, "UTF-8");
			System.out.println("Scheme input:" + twitterEvent);

			//delimiter should respect how Kafka producer is submitting events
			String[] pieces = twitterEvent.split("\\|\\|");

			String userId  = pieces[0];
			String displayname = pieces[1];
            String tweet = pieces[2];
            String created = pieces[3];
			String longitude = pieces[4];
			String latitude = pieces[5];
			String language = pieces[6];
			String fulltext = pieces[7];
			String hashtag = findSearch(tweet);
			/*Timestamp created = new Timestamp(System.currentTimeMillis());
			if (pieces[2] != null){
				Date  ss1=new Date(pieces[2]);
				SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				String formats = formatter.format(ss1);
				created = Timestamp.valueOf(formats);
			}*/

			//String longitude = pieces[3];
			//String latitude = pieces[4];

			//System.out.println("Creating a Scheme with userId[" + userId + "], tweet[" + tweet + "], created["+created+"]");
			//return new Values(userId, tweet, created, longitude, latitude);
			System.out.println("Creating a Scheme with userId[" + userId + "], tweet[" + tweet + "]");
			return new Values(userId, displayname, hashtag, tweet, created, longitude, latitude, language, fulltext);

		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}

	}

	@Override
	public Fields getOutputFields() {
		return new Fields("userId", "displayname", "hashtag", "tweet", "created", "longitude", "latidude", "language", "fulltext");

	}


}

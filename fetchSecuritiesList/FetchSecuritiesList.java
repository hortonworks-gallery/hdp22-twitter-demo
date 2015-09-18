package example.producer;

import java.util.*;
import java.io.*;

import java.net.MalformedURLException;
import java.net.URL;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import example.producer.Security;

public class FetchSecuritiesList {

	//max number of hashtags allowed by Twitter streaming APIs filter
	private static int MAX_NUM_TWITTER_HASHTAG = 400;
	
	//default twitter volume threshold level above which to generate alerts
	private static String DEFUALT_ALERT_THRESHOLD = "5";

	//Prefix to add to stock symbol for search. For financial tweets this is $, otherwise its # 
	private static String TWITTER_SEARCH_PREFIX = "$";	
		
	private static String FILE_DELIMITOR = ",";

	public static List<Security> populateSecuritiesList(String wikipediaUrl){

	List<Security> securities = new ArrayList<Security>();	
	int count = 0;	
 	try {
            URL link = new URL(wikipediaUrl);
            BufferedReader in = new BufferedReader(new InputStreamReader(link.openStream()));
            String line; 
            boolean startSeen = false;
            int fieldNum = 0;
            Security security = null;
 
            while ((line = in.readLine()) != null) {
                // Process each line.
                if (line.contains("500 Component Stocks"))
                	startSeen = true;
                if (startSeen) {	
                
                	if (line.contains("</table>"))	
                		startSeen = false;
                	if (line.contains("<tr>"))	{
                		fieldNum=0;
                		security = new Security();
                	}
            		
                	if (line.contains("<td>")){
                		fieldNum++;
                		String data = matchRegex(line, ">([^<>]+)<.*td>");
                		if (data.length() > 0){
 	               			switch(fieldNum){
                				case 1:
                					security.setTickerSymbol(TWITTER_SEARCH_PREFIX + data);
                				case 2:
                					security.setSecurity(data);       
                				case 3:
                					security.setSecFiling(data);     
                				case 4:
                					security.setSector(data); 
                				case 5:
                					security.setSubindustry(data); 
                				case 6:
                					security.setAddress(data); 
                				case 8:
                					security.setCik(data);                 				                				                				
                				break;
                			}
                		}
                	}
                		
                	if (line.contains("</tr>"))	{
                		fieldNum=0;
                		if (security != null && security.getTickerSymbol() != null){
                			securities.add(security);
                			count++;
                			//System.out.println("Found security symbol: " + security.getTickerSymbol() + " name: " + security.getSecurity());
                		}
                	}                		
                							
                	
                }
            }
            in.close(); 
 
        } catch (MalformedURLException me) {
            System.out.println(me); 
 
        } catch (IOException ioe) {
            System.out.println(ioe);
        }
	
		return securities;
	}


	private static String matchRegex(String url, String regex) {
		String result = "";
		Matcher matcher = Pattern.compile(regex).matcher(url);
		if (matcher.find()) {			
			result = matcher.group(1);
			result = result.trim();
		}

		return result;
	}
	
	private static String cleanString(String s){
		s = s.replace(FILE_DELIMITOR,"").replace("&amp;","&");
		return s;
	}
	
    public static void main(String[] args)  {
		String url = "https://en.wikipedia.org/wiki/List_of_S%26P_500_companies";	
		String fileName = "securities.csv";
		
		System.out.println("Reading securities from: " + url);			
		List<Security> securities = populateSecuritiesList(url);
		
		int count = 0;
		BufferedWriter writer = null;
		try
		{
    		writer = new BufferedWriter( new FileWriter(fileName));
	   		for (Security security : securities){
	   			if (count < MAX_NUM_TWITTER_HASHTAG){
					writer.write (cleanString(security.getTickerSymbol()) + FILE_DELIMITOR);			
					writer.write (cleanString(security.getSecurity()) + FILE_DELIMITOR);
					writer.write (cleanString(security.getSector()) + FILE_DELIMITOR);
					writer.write (cleanString(security.getSubindustry()) + FILE_DELIMITOR);
					writer.write (cleanString(security.getAddress()) + FILE_DELIMITOR);
					writer.write (cleanString(security.getCik()) + FILE_DELIMITOR);
					writer.write (DEFUALT_ALERT_THRESHOLD);	   		
				
					writer.write ("\n");						
					count++;				
				}	
			}		  
 			writer.close();
			System.out.println("Generated data for "+ count + " securities to " + fileName);
		}
		catch ( IOException e)
		{
			System.out.println(e);
		}					        		
	}      
	
}	

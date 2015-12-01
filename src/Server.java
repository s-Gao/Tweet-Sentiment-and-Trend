import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import javax.servlet.http.HttpServlet;
import javax.websocket.OnMessage;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.OnOpen;
import javax.websocket.OnClose;

import org.apache.http.client.ClientProtocolException;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import twitter4j.FilterQuery;
import twitter4j.GeoLocation;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;

@ServerEndpoint("/map")
public class Server extends HttpServlet{
	
	
	
	static String keywordForTrans = "all";
	static String[] sports = {"sports","Soccer","Football","Basketball","NBA","NFL","NHL","MLB","NCAA","FIBA","FIFA","UEFA","EPL","LIGUE 1","SERIE A","La Liga","Bundesliga","Tennis","Volleyball","Baseball","Skating"};
    static String[] food = {"food", "restaurant", "pizza", "burger", "noodle", "fries","cupcake", "breakfast", "brunch"};
    static String[] news = {"news","nytimes","Xinhua","新华社","共同通信社","kyodo","YNA","KCNA","ИТАР-ТАСС","Reuters","AFP","press","CNA","РИА Новости","DPA","Agencia EFE"};	
    static String[] keywordAll = concatAll(sports,food,news);
	
	
	private static DBManager dbmn = new DBManager();
	
	
	private static Session currentSession;
	//private Session currentSession;
	private TwitterStream twitterStream;
	
	@OnOpen
	public void open(Session session){
			currentSession = session;
			dbmn.connect();
			
			SQSWrapper sqsWrapper = new SQSWrapper();
			
			
						ConfigurationBuilder cb = new ConfigurationBuilder();
	         cb.setDebugEnabled(true)
	           .setOAuthConsumerKey("")
	           .setOAuthConsumerSecret("")
	           .setOAuthAccessToken("")
	           .setOAuthAccessTokenSecret("");

	        twitterStream = new TwitterStreamFactory(cb.build()).getInstance();
	        
	        StatusListener listener = new StatusListener() {
	        	
	            @Override
	            public void onStatus(Status status) {
	            	
	                GeoLocation location = status.getGeoLocation();
	                String text = status.getText();
                    
	                double latitude = 0;
	                double longitude = 0;
	                
	                
	                
	                
	                
	                if(location!=null){
	                	try { 
	                		
	                		
	                		
	                		latitude = location.getLatitude();
	                        longitude = location.getLongitude();
	            	        Gson gson = new GsonBuilder().create();
	            	        RealTimeTweetInfo singlePoint = new RealTimeTweetInfo(latitude,longitude);
	            	        
	            	      //store the current tweet into the database
	    	                
	    	    			java.sql.Timestamp date = new java.sql.Timestamp(status.getCreatedAt().getTime()); 
	    	    			try {
	    						dbmn.storeTweet(status.getId(), status.getUser().getScreenName(), latitude, longitude, date, text);
	    					} catch (ClassNotFoundException e1) {
	    						// TODO Auto-generated catch block
	    						e1.printStackTrace();
	    					} catch (InstantiationException e1) {
	    						// TODO Auto-generated catch block
	    						e1.printStackTrace();
	    					} catch (IllegalAccessException e1) {
	    						// TODO Auto-generated catch block
	    						e1.printStackTrace();
	    					} catch (SQLException e1) {
	    						// TODO Auto-generated catch block
	    						e1.printStackTrace();
	    					}
	    	    			//end of storing data into database
	            	        
	            	        
	            	        
	            	        
	            	        if(keywordForTrans=="all"||!containKeyword(text,keywordAll)){
	            	        	String jsonPoint = gson.toJson(singlePoint);
		            	        currentSession.getBasicRemote().sendText(jsonPoint);
	            	        }
	            	        
	            	        
	            	        else{
	            	        	RealTimeTweetInfoForSenti singlePointForSenti = new RealTimeTweetInfoForSenti(latitude,longitude);
	            	        	if(containKeyword(text,news)){
	            	        		singlePointForSenti.SetKeyword("news");
	                            }
	                            else if (containKeyword(text,sports)){
	                            	singlePointForSenti.SetKeyword("sports");
	                            }
	                            else{
	                            	singlePointForSenti.SetKeyword("food");
	                            }
	            	        	singlePointForSenti.SetText(text);
	            	        	
	            	        	String jsonPoint = gson.toJson(singlePointForSenti);
	            	        	
	            	        	
	            	        	SQSWrapper.sendMessage(jsonPoint);
	            	        	
	            	        	
	            	        	
	            	        	

	                            
	                            
	            	        }//end of the situation of containing a keyword
	            	        
	            	        
	            		} catch (Exception e) {
	            			e.printStackTrace();
	            		}
	                	
	                }
	            }
	            
	            @Override
	            public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
	                //System.out.println("Got a status deletion notice id:" + statusDeletionNotice.getStatusId());
	            }
	            @Override
	            public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
	                //System.out.println("Got track limitation notice:" + numberOfLimitedStatuses);
	            }
	            @Override
	            public void onScrubGeo(long userId, long upToStatusId) {
	                System.out.println("Got scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
	            }
	            @Override
	            public void onStallWarning(StallWarning warning) {
	                System.out.println("Got stall warning:" + warning);
	            }
	            @Override
	            public void onException(Exception ex) {
	                ex.printStackTrace();
	            }
	        };
	        
	        twitterStream.addListener(listener);
	        FilterQuery filter = new FilterQuery();
	        double[][] locations = {{-180,-90},{180,90}}; 
	        filter.locations(locations);
	        //filter.track(keywordAll);
	        twitterStream.filter(filter);
		
	}
	
	
	
	
	
	
	
	
	
	
	
	
	@OnMessage
    public void echoTextMessage(Session session, String keyword) {
		currentSession = session;
		keywordForTrans = keyword;
		//System.out.println(keywordForTrans);
    }
	
	@OnClose
    public void close(Session session){
    	//currentSession = session;
    	twitterStream.shutdown();
    	
    	//SQSWrapper.deleteQueue();
    }
	
	
	
	
	
	public static String[] concatAll(String[] sports, String[] food, String[] news) {
    	ArrayList<String> res = new ArrayList<String>();
    	for(int i=0; i < sports.length; i++){
			res.add(sports[i]);
		}
		for(int i=0; i < food.length; i++){
			res.add(food[i]);
		}
		for(int i=0; i < news.length; i++){
			res.add(news[i]);
		}		
		String[] arr = new String[res.size()];
		for(int i=0; i<arr.length; i++){
			arr[i] = res.get(i);
		}
		return arr;
    }
    	
    public static boolean containKeyword(String text, String[] allKeywords) {
    	boolean res = false;
		for (String str : allKeywords){
			if(text.toLowerCase().contains(str.toLowerCase())){
				return true;
			}
		}
		return res;
    }
    public static String sqlStatement(String TABLE_NAME, long tweetID,String userName,double lat,double lon,java.sql.Timestamp date,String text, String keyword,double score){
    	 return "INSERT INTO "
			     + TABLE_NAME
				 + "(tweetID,userName,Latitude,Longtitude,tweetTimeStamp,text,keyword,score)"
				 + " VALUES ('" + tweetID + "','" + userName + "','" + lat
				 + "','" + lon + "','" + date + "','" + text + "','" + keyword + "','" + score + "')";
    }
	
    public static void send(String data){
    	
    	try {
			Server.currentSession.getBasicRemote().sendText(data);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
	
	

}//end of class Server


class RealTimeTweetInfo {
	double lat;
	double lon;
	String kw;
	double senti;
	
	public RealTimeTweetInfo(double lati, double longti){
		this.lat = lati;
		this.lon = longti;
		this.kw  = "all";
		this.senti = 0.0;
	}
	void SetKeyword(String keyword){
		this.kw = keyword;
	}
	void SetScore(double score){
		this.senti = score;
	}
	
}

class RealTimeTweetInfoForSenti{
	double lat;
	double lon;
	String kw;
	double senti;
	String text;
	
	public RealTimeTweetInfoForSenti(double lati, double longti){
		this.lat = lati;
		this.lon = longti;
		this.kw  = "all";
		this.senti = 0.0;
		this.text=null;
	}
	void SetKeyword(String keyword){
		this.kw = keyword;
	}
	void SetScore(double score){
		this.senti = score;
	}
	void SetText(String text){
		this.text=text;
	}
}

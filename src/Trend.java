import twitter4j.Location;
import twitter4j.ResponseList;
import twitter4j.Trends;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
/**
 * Servlet implementation class Trend
 */
@WebServlet("/Trend")
public class Trend extends HttpServlet {
	
	private static final long serialVersionUID = 1L;
	// twitter API
	private static final String myConsumerKey = "";
	private static final String myConsumerSecret = "";
	private static final String myAccessToken = "";
	private static final String myTokenSecret = "";
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public Trend() {
        super();
        // TODO Auto-generated constructor stub
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		// TODO Auto-generated method stub
		HttpSession session = request.getSession(false);
		if (session != null) {
		    session.invalidate();
		}
		
		response.setContentType("text/plain");
		
        String text = request.getParameter("text");
        System.out.println();
        System.out.println(text);

		try {
			ConfigurationBuilder cb = new ConfigurationBuilder();
			cb.setDebugEnabled(true)
			  .setOAuthConsumerKey(myConsumerKey)
			  .setOAuthConsumerSecret(myConsumerSecret)
			  .setOAuthAccessToken(myAccessToken)
			  .setOAuthAccessTokenSecret(myTokenSecret);
		
	        TwitterFactory tf = new TwitterFactory(cb.build());
	        Twitter twitter = tf.getInstance();	

	        Integer idTrendLocation = getTrendLocationId(text, twitter);

	        if (idTrendLocation == null) {
		        System.out.println("Trend Location Not Found");
		        //System.exit(0);
		        response.getWriter().write("Trend Location Not Found");
		        return;
	        }

	        Trends trends = twitter.getPlaceTrends(idTrendLocation);
	        
	        PrintWriter out = response.getWriter(); 
	        for (int i = 0; i < trends.getTrends().length; i++) {
	        	System.out.println(i + 1);
	        	System.out.println(trends.getTrends()[i].getName());
	        	String number = i + 1 + ":";
	        	out.write(number);
	        	out.write(trends.getTrends()[i].getName());
	        	out.write("<br>");
	        }

	    } catch (TwitterException te) {
	        te.printStackTrace();
	        System.out.println("Failed to get trends: " + te.getMessage());
	        System.exit(-1);
	        response.getWriter().write(te.toString());
	    }
		return;
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		

	}
	
	 private static Integer getTrendLocationId(String locationName, Twitter twitter) {

		    int idTrendLocation = 0;

		    try {

		        ResponseList<Location> locations;
		        locations = twitter.getAvailableTrends();

		        for (Location location : locations) {
		        if (location.getName().toLowerCase().equals(locationName.toLowerCase())) {
		            idTrendLocation = location.getWoeid();
		            break;
		        }
		        }

		        if (idTrendLocation > 0) {
		        return idTrendLocation;
		        }

		        return null;

		    } catch (TwitterException te) {
		        te.printStackTrace();
		        System.out.println("Failed to get trends: " + te.getMessage());
		        return null;
		    }

		    }

}

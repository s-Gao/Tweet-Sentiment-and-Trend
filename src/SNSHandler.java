import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.Signature;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Scanner;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.ConfirmSubscriptionRequest;
import com.amazonaws.services.sns.model.ConfirmSubscriptionResult;
import com.google.gson.*;
/**
 * Servlet implementation class SNSHandler
 */

public class SNSHandler extends HttpServlet {
	

	private static final long serialVersionUID = 1L;
	


	
	private class SNSMessage {
		private String type;
		private String messageId;
		private String token;
		private String topicArn;
		private String message;
		private String subscribeURL;
		private String subject;
		private String timestamp;
		private String signatureVersion;
		private String signature;
		private String signingCertURL;
		
		public String getType(){
			return this.type;
		}
		public String getMessageId(){
			return this.messageId;
		}
		public String getToken(){
			return this.token;
		}
		public String getTopicArn(){
			return this.topicArn;
		}
		public String getMessage(){
			return this.message;
		}
		public String getSubscribeURL(){
			return this.subscribeURL;
		}
		public String getSubject(){
			return this.subject;
		}
		public String getTimestamp(){
			return this.timestamp;
		}
		public String getSignatureVersion(){
			return this.signatureVersion;
		}
		public String getSignature(){
			return this.signature;
		}
		public String getSigningCertURL(){
			return this.signingCertURL;
		}
	}
       
    /**
     * @see HttpServlet#HttpServlet()
     */
    public SNSHandler() {
        super();
        // TODO Auto-generated constructor stub
        
    }

	/**
	 * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
	 */
	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
	}

	/**
	 * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
	 */
	 //@SuppressWarnings("resource")
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// TODO Auto-generated method stub
		//Get the message type header.
		 		
				String messagetype = request.getHeader("x-amz-sns-message-type");
				//If message doesn't have the message type header, don't process it.
				if (messagetype == null){
					System.out.println("No message type");
					return;
				}
					

		    // Parse the JSON message in the message body
		    // and hydrate a Message object with its contents 
		    // so that we have easy access to the name/value pairs 
		    // from the JSON message.
		   
			Scanner scan = new Scanner(request.getInputStream());
		    StringBuilder builder = new StringBuilder();
		    while (scan.hasNextLine()) {
		      builder.append(scan.nextLine());
		    }
		    	Gson gson = new Gson();
		    	
		    	System.out.println(builder.toString());
		    	
				 String messageStr= builder.toString();
				 JsonObject messageJson = gson.fromJson(messageStr, JsonObject.class);
		        System.out.println(messageJson.get("Type").getAsString());
		    // The signature is based on SignatureVersion 1. 
		    // If the sig version is something other than 1, 
		    // throw an exception.
		    /*if (msg.getSignatureVersion().equals("1")) {
				  // Check the signature and throw an exception if the signature verification fails.
				  if (isMessageSignatureValid(msg))
						System.out.println(">>Signature verification succeeded");
				  else {
						System.out.println(">>Signature verification failed");
						throw new SecurityException("Signature verification failed.");
				  }
		    }
		    else {
					System.out.println(">>Unexpected signature version. Unable to verify signature.");
		      throw new SecurityException("Unexpected signature version. Unable to verify signature.");
		    }
			*/
		    // Process the message based on type.
				if (messageJson.get("Type").getAsString().equals("Notification")) {
					
					String dataContent = messageJson.get("Message").getAsString();
					System.out.println(dataContent);
					Server.send(dataContent);
					//TODO: Do something with the Message and Subject.
					//Just log the subject (if it exists) and the message.
					/*
					String logMsgAndSubject = ">>Notification received from topic " + msg.getTopicArn();
					if (msg.getSubject() != null)
						logMsgAndSubject += " Subject: " + msg.getSubject();
					logMsgAndSubject += " Message: " + msg.getMessage();
					System.out.println(logMsgAndSubject);
					*/
				}
		    else if (messageJson.get("Type").getAsString().equals("SubscriptionConfirmation"))
				{
		       //TODO: You should make sure that this subscription is from the topic you expect. Compare topicARN to your list of topics 
		       //that you want to enable to add this endpoint as a subscription.
		        	
		       //Confirm the subscription by going to the subscribeURL location 
		       //and capture the return value (XML message body as a string)
		       Scanner sc = new Scanner(new URL(messageJson.get("SubscribeURL").getAsString()).openStream());
		       StringBuilder sb = new StringBuilder();
		       while (sc.hasNextLine()) {
		         sb.append(sc.nextLine());
		       }
		       System.out.println(">>Subscription confirmation (" + messageJson.get("SubscribeURL").getAsString() +") Return value: " + sb.toString());
		       //TODO: Process the return value to ensure the endpoint is subscribed.
		       SNSWrapper.confirmSubscribe(messageJson.get("TopicArn").getAsString(),messageJson.get("Token").getAsString());
		       
				}
		    else if (messageJson.get("Type").getAsString().equals("UnsubscribeConfirmation")) {
		      //TODO: Handle UnsubscribeConfirmation message. 
		      //For example, take action if unsubscribing should not have occurred.
		      //You can read the SubscribeURL from this message and 
		      //re-subscribe the endpoint.
		      System.out.println(">>Unsubscribe confirmation: " + messageJson.get("Message").getAsString());
				}
		    else {
		      //TODO: Handle unknown message type.
		      System.out.println(">>Unknown message type.");
		    }
				System.out.println(">>Done processing message: " + messageJson.get("MessageId").getAsString());
		
		
	}
	
	private static boolean isMessageSignatureValid(SNSMessage msg) {
        try {
          URL url = new URL(msg.getSigningCertURL());
          InputStream inStream = url.openStream();
          CertificateFactory cf = CertificateFactory.getInstance("X.509");
          X509Certificate cert = (X509Certificate)cf.generateCertificate(inStream);
          inStream.close();

          Signature sig = Signature.getInstance("SHA1withRSA");
          sig.initVerify(cert.getPublicKey());
          sig.update(getMessageBytesToSign(msg));
          return sig.verify(Base64.decodeBase64(msg.getSignature()));
        }
        catch (Exception e) {
       	  throw new SecurityException("Verify method failed.", e);
        }
   }
	
	private static byte [] getMessageBytesToSign (SNSMessage msg) {
		byte [] bytesToSign = null;
		if (msg.getType().equals("Notification"))
			bytesToSign = buildNotificationStringToSign(msg).getBytes();
		else if (msg.getType().equals("SubscriptionConfirmation") || msg.getType().equals("UnsubscribeConfirmation"))
			bytesToSign = buildSubscriptionStringToSign(msg).getBytes();
		return bytesToSign;
	}
	
	 //Build the string to sign for Notification messages.
	 public static String buildNotificationStringToSign( SNSMessage msg) {
	   String stringToSign = null;
		 
  	 //Build the string to sign from the values in the message.
	   //Name and values separated by newline characters
  	 //The name value pairs are sorted by name 
	   //in byte sort order.
	   stringToSign = "Message\n";
	   stringToSign += msg.getMessage() + "\n";
	   stringToSign += "MessageId\n";
	   stringToSign += msg.getMessageId() + "\n";
	   if (msg.getSubject() != null) {
	     stringToSign += "Subject\n";
		   stringToSign += msg.getSubject() + "\n";
	   }
	   stringToSign += "Timestamp\n";
	   stringToSign += msg.getTimestamp() + "\n";
	   stringToSign += "TopicArn\n";
	   stringToSign += msg.getTopicArn() + "\n";
	   stringToSign += "Type\n";
	   stringToSign += msg.getType() + "\n";
	   return stringToSign;
	 }

	 //Build the string to sign for SubscriptionConfirmation 
	 //and UnsubscribeConfirmation messages.
	 public static String buildSubscriptionStringToSign(SNSMessage msg) {
		 String stringToSign = null;
		 //Build the string to sign from the values in the message.
		 //Name and values separated by newline characters
		 //The name value pairs are sorted by name 
		 //in byte sort order.
		 stringToSign = "Message\n";
		 stringToSign += msg.getMessage() + "\n";
		 stringToSign += "MessageId\n";
		 stringToSign += msg.getMessageId() + "\n";
		 stringToSign += "SubscribeURL\n";
		 stringToSign += msg.getSubscribeURL() + "\n";
		 stringToSign += "Timestamp\n";
		 stringToSign += msg.getTimestamp() + "\n";
		 stringToSign += "Token\n";
		 stringToSign += msg.getToken() + "\n";
		 stringToSign += "TopicArn\n";
		 stringToSign += msg.getTopicArn() + "\n";
		 stringToSign += "Type\n";
		 stringToSign += msg.getType() + "\n";
		 return stringToSign;
	 }

}

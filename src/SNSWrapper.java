import com.amazonaws.services.sns.AmazonSNS;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.auth.ClasspathPropertiesFileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sns.model.ConfirmSubscriptionRequest;
import com.amazonaws.services.sns.model.ConfirmSubscriptionResult;
import com.amazonaws.services.sns.model.CreateTopicRequest;
import com.amazonaws.services.sns.model.CreateTopicResult;
import com.amazonaws.services.sns.model.SubscribeRequest;
import com.amazonaws.services.sns.model.PublishRequest;
import com.amazonaws.services.sns.model.PublishResult;
import com.amazonaws.services.sns.model.DeleteTopicRequest;

public class SNSWrapper {
	
	private static AmazonSNSClient sns;
	private static String topicArn="";
	
	private static void createSNS(){
		//create a new SNS client and set endpoint
		sns = new AmazonSNSClient(new ClasspathPropertiesFileCredentialsProvider());		                           
		sns.setRegion(Region.getRegion(Regions.US_EAST_1));

		//create a new SNS topic
		CreateTopicRequest createTopicRequest = new CreateTopicRequest("");
		
	}
	public static void addSNS(String message){
		if(sns==null){
			createSNS();
		}
		//publish to an SNS topic
		String msg = message;
		PublishRequest publishRequest = new PublishRequest(topicArn, msg);
		PublishResult publishResult = sns.publish(publishRequest);
		//print MessageId of message published to SNS topic
		System.out.println("SNS MessageId - " + publishResult.getMessageId());
	}
	public static void subscribeSNS(){
		if(sns==null){
			createSNS();
		}
		//subscribe to an SNS topic
		SubscribeRequest subRequest = new SubscribeRequest(topicArn, "http", "http:///TwittTrend/SNSHandler");
		
		try{
			sns.subscribe(subRequest);
			
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		//get request id for SubscribeRequest from SNS metadata
		System.out.println("SubscribeRequest - " + sns.getCachedResponseMetadata(subRequest));
		System.out.println("Check your email and confirm subscription.");
	}
	
	public static void confirmSubscribe(String topicArn,String token){
		if(sns==null){
			createSNS();
		}
		try{
		   ConfirmSubscriptionRequest confirmSubscriptionRequest = new ConfirmSubscriptionRequest()
			.withTopicArn(topicArn)
			.withToken(token);
	       ConfirmSubscriptionResult resutlt = sns.confirmSubscription(confirmSubscriptionRequest);
	       System.out.println("subscribed to " + resutlt.getSubscriptionArn());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
	}
	


}

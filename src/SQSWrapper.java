import java.util.List;
import java.util.Map.Entry;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.profile.ProfileCredentialsProvider;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.CreateQueueRequest;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.DeleteQueueRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.amazonaws.services.sqs.model.SendMessageRequest;

public class SQSWrapper {

		private static AmazonSQS sqs;
		private static String myQueueUrl;
		
		
		
		private static void createSQS(){
			AWSCredentials credentials = null;
	        try {
	            credentials = new ProfileCredentialsProvider().getCredentials();
	        } catch (Exception e) {
	            throw new AmazonClientException(
	                    "Cannot load the credentials from the credential profiles file. " +
	                    "Please make sure that your credentials file is at the correct " +
	                    "location (~/.aws/credentials), and is in valid format.",
	                    e);
	        }

	        sqs = new AmazonSQSClient(credentials);
	        Region usEast2 = Region.getRegion(Regions.US_EAST_1);
	        sqs.setRegion(usEast2);

	        try {
	            // Create a queue
	            String queueName="";
	            CreateQueueRequest createQueueRequest = new CreateQueueRequest(queueName);
	            myQueueUrl = sqs.createQueue(createQueueRequest).getQueueUrl();

	            // List queues
	            System.out.println("Listing all queues in your account.\n");
	            for (String queueUrl : sqs.listQueues().getQueueUrls()) {
	                System.out.println("  QueueUrl: " + queueUrl);
	            }
	            System.out.println();
	        }
	        catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		public static void sendMessage(String msg){
			// Send a message
			
			if (sqs == null || myQueueUrl.isEmpty()) {
				
				createSQS();
			}
	        sqs.sendMessage(new SendMessageRequest(myQueueUrl, msg));
		}
		
		public static Message receiveMessage(){
			if (sqs == null || myQueueUrl.isEmpty()) {
				createSQS();
			}
			
				ReceiveMessageRequest request = new ReceiveMessageRequest(myQueueUrl);
	            request.setMaxNumberOfMessages(1);
	            ReceiveMessageResult result = sqs.receiveMessage(request);
	    		
	    		if(result != null && result.getMessages().size() > 0){
	    			return result.getMessages().get(0);			
	    		}
	    		
	    		return null;
			
            
		}
		
		
		public static void deleteMessage(Message message){
				DeleteMessageRequest deleteMessageRequest = new DeleteMessageRequest(myQueueUrl, message.getReceiptHandle());			
				sqs.deleteMessage(deleteMessageRequest);
		}
		
		public static void deleteQueue(){
			try{
	            sqs.deleteQueue(new DeleteQueueRequest(myQueueUrl));
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
    
}

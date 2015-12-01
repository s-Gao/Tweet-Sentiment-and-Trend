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

import java.io.IOException;

import com.google.gson.*;
public class Worker {
	
	public static void main(String[] args){
		
		
		
		System.out.println("try subscribe");
        SNSWrapper.subscribeSNS();
        
        
        
		while(true){
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			//System.out.println("worker working");
			Message msg = SQSWrapper.receiveMessage();
			if(msg!=null){
				String str = new Gson().toJson(msg);
				Gson gson = new Gson();
				JsonObject jsonObject =gson.fromJson(str, JsonObject.class);
				
				//System.out.println(jsonObject.toString());
				String body = jsonObject.get("body").getAsString();
				JsonObject jsonObject1 = gson.fromJson(body,JsonObject.class);
				String text = jsonObject1.get("text").getAsString();
				String lat = jsonObject1.get("lat").getAsString();
				String lon = jsonObject1.get("lon").getAsString();
				String kw = jsonObject1.get("kw").getAsString();
				
				double score =0.0;
				APIService apiService = APIService.getInstanceWithKey("18390c237a26dce724bdb409f8d73235aa49f5b1");
				String s=null;
				
					try {
						s = apiService.getSentiment(text);
					} 
					catch (Exception e) {
						e.printStackTrace();
					}
					JsonObject jsonObjectSenti = new Gson().fromJson(s, JsonObject.class);
	                JsonObject jsonObjectSentiDoc = null;
	                if(jsonObjectSenti.get("docSentiment")!=null){
	                	jsonObjectSentiDoc = jsonObjectSenti.get("docSentiment").getAsJsonObject();
	                    if (jsonObjectSentiDoc.get("score")!=null){
	                    		score = jsonObjectSentiDoc.get("score").getAsDouble();
	                    		
	                    }
	                    
	                }
                
	                	
	                	SNSInfo snsInfo = new SNSInfo(lon,lat,score,kw);
	                	Gson snsGson = new Gson();
	                	String snsString = snsGson.toJson(snsInfo,SNSInfo.class);
	                	
	                	SNSWrapper.addSNS(snsString);
	                	
	                	
	                	
	                	
	                	
	                	
	                

				SQSWrapper.deleteMessage(msg);
			}
				
		}
	}
	
}
class SNSInfo{
	String lon=null;
	String lat=null;
	double senti=0.0;
	String kw=null;
	
	SNSInfo(String lon,String lat, double score, String kw){
		this.lon=lon;
		this.lat=lat;
		this.senti=score;
		this.kw=kw;
		
	}
}

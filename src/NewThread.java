import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.amazonaws.services.sqs.model.Message;

public class NewThread extends Thread{
	Message msg;
	public NewThread(Message msg){
		this.msg=msg;
	}
	public void run(){
		
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
		APIService apiService = APIService.getInstanceWithKey("");
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

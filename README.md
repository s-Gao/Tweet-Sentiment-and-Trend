# Tweet-Sentiment-and-Trend
This is the web application that could show the sentiment of tweets from all over the world and trends in specific locations.


Specifically, the communication of front-end and back-end is through WebSocket implemented by Java Servlet . 

Firstly, the server utilizes the twitter4j API to catch tweets with geo-locations and filters tweets with the keywords we are interested in. These tweets are then stored in Amazon RDS and also pushed into Amazon SQS. Multi-thread workers are implemented to fetch tweets from SQS and analyze the sentiment of tweets with Alchemy API. Once the last step is finished, a notification would be sent to an HTTP endpoint using Amazon SNS. Then these tweets with sentiment information would be sent to the front for presentation. 

Another function of trend in specific places are implemented by using Ajax. First send HTTP request containing the name of the place.  The back-end uses twitter4j API to generate the 10 most popular words and send back. Then the page would be updated to show the trending information.


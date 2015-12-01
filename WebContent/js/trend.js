$(function() {
	// button to start the trend thread
	$("#trendButton").click(function() {
		
		$.get("Trend", {
			text: $("#text").val()
		}, function(response) {
			
			
			document.getElementById("messages").innerHTML=response;
			
			//$("#messages").innerHTML(
			//	response);
			
		});
		
	});

});
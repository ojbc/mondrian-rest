var availableConnections = null;
var currentConnection = null;

$().ready(function() {

  $.ajax({
	    "url"        : "../getConnections",
	    "dataType"   : "json",
	    "contentType": "application/json",
	    "type"       : "GET",
	    "complete"   : function(response) {
        let menu = $("#connectionDropdownMenu");
        availableConnections = response.responseJSON;
        for (var conn in response.responseJSON) {
          menu.append("<button class=\"dropdown-item connection-dropdown-item\" type=\"button\" data-conn=\"" + conn + "\">" + response.responseJSON[conn].Description + "</button>");
        }
        $(".connection-dropdown-item").on("click", function(e) {
          currentConnection = e.target.dataset.conn;
          $("#currentConnectionLabel").text(availableConnections[currentConnection].Description);
        });
	    }
	});

  $("#runQueryButton").on("click", function(e) {
    showWaitPane();
    let request = new Object();
    request.connectionName = currentConnection;
    request.query = $("#mdxTextArea").val();
    request.tidy = new Object();
    request.tidy.enabled = $("#tidyCheckbox")[0].checked;
    $.ajax({
  	    "url"        : "../query",
  	    "dataType"   : "json",
  	    "contentType": "application/json",
  	    "type"       : "POST",
        "data"       : JSON.stringify(request),
  	    "complete"   : function(response) {
          $("#results").text(JSON.stringify(response.responseJSON, null, 2));
          hideWaitPane();
        }
      });
  });

});

showWaitPane = function() {
	let waitPaneDiv = $("#wait-spinner");
	waitPaneDiv.show();
}

hideWaitPane = function() {
	$("#wait-spinner").hide();
}

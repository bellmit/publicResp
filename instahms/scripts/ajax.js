/*
 * Returns an new XMLHttpRequest object, or false if the browser
 * doesn't support it
 */
function newXMLHttpRequest() {

  var xmlreq = false;

  // Create XMLHttpRequest object in non-Microsoft browsers
  if (window.XMLHttpRequest) {
    xmlreq = new XMLHttpRequest();

  } else if (window.ActiveXObject) {

    try {
      // Try to create XMLHttpRequest in later versions
      // of Internet Explorer

      xmlreq = new ActiveXObject("Msxml2.XMLHTTP");

    } catch (e1) {

      // Failed to create required ActiveXObject

      try {
        // Try version supported by older versions
        // of Internet Explorer

        xmlreq = new ActiveXObject("Microsoft.XMLHTTP");

      } catch (e2) {

        // Unable to create an XMLHttpRequest by any means
        xmlreq = false;
      }
    }
  }

return xmlreq;
}

 /*
	* Returns a function that waits for the specified XMLHttpRequest
	* to complete, then passes it XML response to the given handler function.
  * req - The XMLHttpRequest whose state is changing
  * responseXmlHandler - Function to pass the XML response to
  */
 function getReadyStateHandler(req, responseXmlHandler) {

   // Return an anonymous function that listens to the XMLHttpRequest instance
   return function () {

     // If the request's status is "complete"
     if (req.readyState == 4) {

       // Check that we received a successful response from the server
       if (req.status == 200) {

         // Pass the XML payload of the response to the handler function.
         responseXmlHandler(req.responseText);

       } else {

         // An HTTP problem has occurred
         alert("HTTP error "+req.status+": "+req.statusText);
       }
     }
   }
 }

  /*
  * Returns a function that waits for the specified XMLHttpRequest
  * to complete, then passes it XML response to the given handler function.
  * req - The XMLHttpRequest whose state is changing
  * responseXmlHandler - Function to pass the XML response to
  */
function getResponseHandler(reqObject, responseXmlHandler,url) {
	// Return an anonymous function that listens to the XMLHttpRequest instance
	// If the request's status is "complete"
	var domObject="",domDocObj="";
	if (reqObject) {
		reqObject.onreadystatechange=function() {
			if (reqObject.readyState == 4) {
				if ( (reqObject.status == 200) && (reqObject.responseText!=null) && 
						(reqObject.responseXML != null) ) {
					domDocumentObj = reqObject.responseXML.documentElement;
					responseXmlHandler(domDocumentObj,reqObject.responseText);
				} else {
					responseXmlHandler(null,null);
				}
			}
		}

		reqObject.open("POST",url.toString(), true);
		reqObject.send(null);
	}
}

function getResponseHandlerText(reqObject, responseHandler, url) {
    var domObject="",domDocObj="";
    if (reqObject) {
		reqObject.onreadystatechange = function() {
			if (reqObject.readyState == 4) {
				if ( (reqObject.status == 200) && (reqObject.responseText!=null) ) {
					responseHandler(reqObject.responseText);
				} else {
					responseHandler(null);
				}
			}
			// else, ignore the state change.
		}
		reqObject.open("POST",url.toString(), true);
		reqObject.send(null);
	}
}

//New Ajax functionality
Ajax = function () { }
/*
 * Call a remote url via GET using ajax call. 
 * Example usage:
 * Ajax.get("/mydata.do?id=1", function(data, status) { document.form.field.value = data; });
 * 
 * Here we are assigning the value retrieved directly from ajax call to a field. The call is 
 * a async GET call
 */
Ajax.get = function(url, callback) {
	var callbackResponse = function(out) {
		callback(out.responseText, out.status);
	}
	var func_args =	{ 
			success: callbackResponse,
			failure: callbackResponse
	}; 
	return YAHOO.util.Connect.asyncRequest("GET", url, func_args)
}

/*
 * Call a remote url via POST using ajax call. 
 * Example usage:
 * Ajax.get("/mydata.do", "id=1&name=xyz", function(data, status) { document.form.field.value = data; });
 * 
 * Here we are assigning the value retrieved directly from ajax call to a field. The call is 
 * a async POST call
 */
Ajax.post = function(url, params, callback) {
	var callbackResponse = function(out) {
		callback(out.responseText, out.status);
	}
	var func_args = {
			success: callbackResponse, 
			failure: callbackResponse
	}
	return YAHOO.util.Connect.asyncRequest("POST", url, func_args, params);
}



<%@tag pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@attribute name="prefix" required="false" %> <%-- defaults to empty string--%>
<%
	// js. as a prefix identifies the resource strings that are used in javascript
	String stdPrefix = "js.";
	String pagePrefix = ((null != prefix && prefix.trim().length() > 0) ? prefix.trim() : "");

 	// Each page can have its own prefix for the resource string in addition to js.
 	// This is so that not all js resources are loaded all the time.
 	// We just load the ones used by the page.
	String keyPrefix = stdPrefix + ((null != pagePrefix && pagePrefix.trim().length() > 0) ?
					(pagePrefix.trim() + ".") : "");
 	
	java.util.Locale userLocale = new java.util.Locale((String) request.getAttribute("language"));

 	java.util.ResourceBundle rb = java.util.ResourceBundle.getBundle("java.resources.application", userLocale);

 	java.util.Map bundle = new java.util.HashMap();
	for( java.util.Enumeration en = rb.getKeys(); en.hasMoreElements(); ) {
		String key = (String)en.nextElement();
		if( key.startsWith(keyPrefix) ) {
			bundle.put(key, rb.getString(key));
		}
	}

	flexjson.JSONSerializer serializer = new flexjson.JSONSerializer().exclude("class");
	String jsBundle = serializer.deepSerialize(bundle);
%>
<%--The following is a javascript call that loads the key-value pairs into
Insta.i18n. --%>

<script type="text/javascript">
	addResourceBundle("<%=pagePrefix%>", <%=jsBundle%>);
</script>

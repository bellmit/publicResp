<%--
 Can be used inside the JSP like this, if there can be special characters in the string:
	<insta:encodeComponent value="${string}"/>;
--%>
<%@attribute name="value" required="true" %>  <%=java.net.URLEncoder.encode(value, "utf-8")%>
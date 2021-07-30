<%-- 
 Can be used inside the JSP like this, if there can be quote characters in the string:
	var customField1 = <insta:jsString value="${customField1}"/>;


--%><%@attribute name="value" required="true" %> <%= new flexjson.JSONSerializer().deepSerialize(value) %>

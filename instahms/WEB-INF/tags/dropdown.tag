<%@ tag body-content="empty" dynamic-attributes="dynatr" pageEncoding="UTF-8"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<%@ attribute name="username" required="true" %>
<%@ attribute name="elename" required="true" %>
<%@ attribute name="datalist" required="true" type="java.util.List"%>
<%@ attribute name="display_field" required="true" %>
<%@ attribute name="display_value" required="true" %>
<%@ attribute name="selected_value" required="false" %>
<%@ attribute name="default_display_field" required="false" %>
<%@ attribute name="default_display_value" required="false" %>

<select name="${elename}"
<c:forEach items="${dynatr}" var="a">${a.key}="${ifn:cleanHtmlAttribute(a.value)}"</c:forEach> class="dropDown">
	<c:if test="${not empty default_display_field}">
		<option value='${default_display_value}'>${default_display_field}</option>
	</c:if>
	<c:forEach var="data" items="${datalist}">
 		<option value='${data[display_value]}'<c:if test="${data[display_value] eq selected_value}">selected</c:if>>${data[display_field]}</option>
 	</c:forEach>
</select>

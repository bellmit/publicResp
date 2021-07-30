<%@ tag body-content="empty" dynamic-attributes="dynattrs" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib prefix="insta" tagdir="/WEB-INF/tags" %>
<%@ attribute name="fieldName" required="true" %>
<%@ attribute name="type" required="true" %>
<%@ attribute name="displayName" required="false" %>
<!-- Attributes required for checkbox -->
<%@ attribute name="opvalues" required="false" %>
<%@ attribute name="optexts" required="false" %>
<%@ attribute name="selValues" required="false" type="java.lang.String[]" %>
<%-- 
<%@ attribute name="valuecol" required="false" %>
<%@ attribute name="table" required="false" %>
<%@ attribute name="displaycol" required="false" %>
<%@ attribute name="multiple" required="false" %>
<%@ attribute name="orderby" required="false" %>
<%@ attribute name="filtercol" required="false" %>
<%@ attribute name="filtervalue" required="false" %>
 --%>

<c:set var="attrs">
	<c:forEach items="${dynattrs}" var="a">
 	${a.key}="${ifn:cleanHtml(a.value)}"
	</c:forEach>
</c:set>
<c:set var="fieldDisplayName" value="${(null != displayName) ? displayName : ifn:prettyPrint(fieldName)}"/>
<div class="sboField">
	<div class="sboFieldLabel">${fieldDisplayName}:</div>
	<div class="sboFieldInput" style="min-height:21px;height:100%;"> <%-- style is so adjusted that the panel height expands to fit content --%>
	<c:choose>
	<c:when test="${type eq 'a'}"> <%-- autocomplete --%>
		<input type="text" name="${fieldName}" id="${fieldName}" value="${ifn:cleanHtmlAttribute(param[fieldName])}">
		<input type="hidden" name="${fieldName}@op" value="ico" />
		<div id="${fieldName}_container" style = "width:20em"></div>
	</c:when>
	<c:when test="${type eq 'c'}"> <%-- checkbox --%>
		<insta:checkgroup name="${fieldName}" opvalues="${opvalues}" optexts="${optexts}" selValues="${paramValues[fieldName]}"/>
		<input type="hidden" name="${fieldName}@op" value="in" />
	</c:when>
	<c:when test="${type eq 't'}"> <%-- text field --%>
		<input type="text" name="${fieldName}" value="${ifn:cleanHtmlAttribute(param[fieldName])}" />
	</c:when>
	</c:choose>
	</div>
</div>

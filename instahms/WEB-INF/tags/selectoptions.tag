<%@ tag body-content="empty" dynamic-attributes="dynattrs" pageEncoding="UTF-8"%>

<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ attribute name="name" required="true" %>
<%@ attribute name="value" required="true" %>
<%@ attribute name="opvalues" required="true" %>
<%@ attribute name="optexts" required="true" %>
<%@ attribute name="dummyvalue" required="false" %>
<%@ attribute name="dummyvalueId" required="false" %>
<%@ attribute name="styleClass" required="false" %>
<%@ attribute name="disabled" required="false" %>

<%--
	Generates a select tag with its child option tags. The option with the value will be marked as selected.

	Example Usage:
		<insta:selectoptions name="reg_time" value="N" opvalues="Y,N" optexts="Yes,No"/>

    This will produce the following html output
		<select name="reg_time">
			<option value="Y">Yes</option>
			<option value="N" selected="true">No</option>
		</select>
--%>
<c:set var="textArray" value="${fn:split(optexts,',')}"/>
<c:set var="valueArray" value="${fn:split(value,',')}"/>
<c:set var="classSpec">
	<c:choose>
		<c:when test="${not empty styleClass}">class="${styleClass}"</c:when>
		<c:otherwise>class="dropdown"</c:otherwise>
	</c:choose>
</c:set>
<%
	if (dummyvalueId==null) dummyvalueId = "";
%>
<select ${classSpec}
<c:forEach items="${dynattrs}" var="a">
 ${a.key}="${ifn:cleanHtml(a.value)}"
</c:forEach>
<c:if test="${disabled}">disabled</c:if>
name="${name}">
<c:if test="${dummyvalue != null}">
<option value="${dummyvalueId}">${dummyvalue}</option>
</c:if>
<c:forEach items="${fn:split(opvalues,',')}" var="option" varStatus="status">
  <c:choose>
	<c:when test="${ifn:arrayFind(valueArray,option) ne -1}">
		<c:set var="attr" value="selected='true'"/>
	</c:when>
	<c:otherwise>
		<c:set var="attr" value=""/>
	</c:otherwise>
  </c:choose>
  <option value="<c:out value="${option}"/>" ${attr}>${textArray[status.index]}</option>
</c:forEach>
</select>


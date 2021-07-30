<%@ tag body-content="empty" dynamic-attributes="dynattrs" pageEncoding="UTF-8"%>

<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ attribute name="name" required="true" %>
<%@ attribute name="radioValues" required="true" %>
<%@ attribute name="value" required="true" %>
<%@ attribute name="radioText" required="true" %>
<%@ attribute name="radioIds" required="false" %>

<%--
	Generates a radio tag with its list of options. The option with the value will be marked as selected.

	Example Usage:
			<insta:radio name="status" radioValues="y,n" value="Y" radioText="Yes,No" radioIds="Y,N" />

        This will produce the following html output
		<td>
			<input type="radio" name="status" value="y" checked="1" id="Y"/>
			<label>Yes</label>

			<input type="radio" name="status" value="n" id="N"  />
			<label>No</label>
		</td>
--%>

<c:set var="textArray" value="${fn:split(radioText,',')}"/>
<c:set var="idArray" value="${fn:split(radioIds,',')}"/>
<c:set var="valueArray" value="${fn:split(radioValues,',')}" />

<c:forEach items="${valueArray}" var="option" varStatus="status">
 <c:choose>
	<c:when test="${option eq value}">
		<c:set var="attr" value="checked='1'"/>
	</c:when>
	<c:otherwise>
		<c:set var="attr" value=""/>
	</c:otherwise>
  </c:choose>
  <input type="radio" name="${name}" value="${option}" ${attr} id="${idArray[status.index]}"
	<c:forEach items="${dynattrs}" var="a">
 		${a.key}="${ifn:cleanHtmlAttribute(a.value)}"
	</c:forEach>
  />
  <label >${textArray[status.index]}</label>
</c:forEach>


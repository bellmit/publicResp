<%@tag body-content="empty" dynamic-attributes="dynattrs" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>

<%@attribute name="name" required="true" %>
<%@attribute name="id" required="false" %>				<%-- defaults to name if not given --%>
<%@attribute name="value" required="false" %>			<%-- value given as a string --%>
<%@attribute name="valueDate" required="false" type="java.util.Date" %>	<%-- value given as a date object --%>
<%@attribute name="valid" required="false" %>			<%-- future/past --%>
<%@attribute name="extravalidation" required="false"%>	<%-- java function pointer --%>
<%@attribute name="calButton" required="false" %>			<%-- true/false: defaults to false --%>
<%@attribute name="calDblclick" required="false" %>		<%-- true/false: defaults to true --%>
<%@attribute name="btnPos" required="false" %>		<%-- left/right: defaults to right --%>
<%@attribute name="required" required="false" %>		<%-- defaults to false --%>
<%@attribute name="editValue" required="false" %>
<%@attribute name="title" required="false" %>
<%--
  This is a common widget for use across all Insta applications. The basic usage is
  like this:
	&lt;%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %&gt;
	...
  	<insta:datewidget name="fieldname"/>
  See above for additional attributes and what they mean.

  An example is available at pages/Common/ServerDate.jsp
--%>

<fmt:formatDate var="curTimeStr" pattern="HH:mm" value="<%=new java.util.Date()%>"/>
<fmt:formatDate var="curDateStr" pattern="dd-MM-yyyy" value="<%=new java.util.Date()%>"/>

<%-- create a Javascript variable and a request scope object to store the current date+time
  We convert the string to date in javascript because we do not want to use the browser timezone
  which messes up things.
--%>
<script>
	var gServerNow = getDateTime('${curDateStr}','${curTimeStr}');
</script>
<%
	java.util.Date now = new java.util.Date();
	request.setAttribute("serverNow", now);
%>

<c:set var="objId">
	<c:choose>
		<c:when test="${not empty id}">${id}</c:when>
		<c:otherwise>${name}</c:otherwise>
	</c:choose>
</c:set>

<c:set var="objValue">
	<c:choose>
		<c:when test="${value=='today'}"><fmt:formatDate value="${serverNow}" pattern="dd-MM-yyyy"/></c:when>
		<c:when test="${value=='td'}"><fmt:formatDate value="${serverNow}" pattern="dd-MM-yyyy"/></c:when>
		<c:when test="${not empty valueDate}"><fmt:formatDate value="${valueDate}" pattern="dd-MM-yyyy"/></c:when>
		<c:otherwise>${ifn:cleanHtml(value)}</c:otherwise>
	</c:choose>
</c:set>

<c:if test="${empty calDblclick}"><c:set var="calDblclick" value="false"/></c:if>
<c:if test="${empty calButton}"><c:set var="calButton" value="true"/></c:if>
<c:if test="${empty btnPos}"><c:set var="btnPos" value="right"/></c:if>
<c:if test="${(not empty editValue) && (editValue != false)}"><c:set var="readonly" value="readonly"/></c:if>
<c:set var="dblClickEvent">
	<c:if test="${calDblclick}">ondblclick="showCalendar('${objId}')"</c:if>
</c:set>
<input type="text" size="10" name="${name}" id="${objId}" value="${objValue}" title="${not empty title ? title : 'Date is mandatory.'}"
	${readonly}
<c:forEach items="${dynattrs}" var="a">
 ${a.key}="${ifn:cleanHtml(a.value)}"
</c:forEach>
	class="validate-date-in ${required ? 'required' : ''} datefield"
	onchange="doValidateDateField(this,'${valid}');${extravalidation}"
	${dblClickEvent}
/>

<c:if test="${calDblclick || calButton}">
	<c:if test="${calButton}">
		<img src="<%=request.getContextPath()%>/images/calendar.png" height="16" width="16"
		style="vertical-align: text-bottom" onclick="showCalendar('${objId}')"/>
		<c:if test="${required}"><span class="required">*</span></c:if>
	</c:if>
	<span class="yui-skin-sam">
		<%-- we are hiding this container for smoother rendering, we will show it during initialization --%>
		<div id="${objId}_container" class="instadateCalContainer" style="display: none; visibility: hidden">
			<div class="hd"><insta:ltext key="datewidget.calendar.title"/></div>
			<div class="bd">
				<div id="${objId}_cal" class="instadateCalendar"></div>
			</div>
		</div>
	</span>
	<script>
		YAHOO.util.Event.addListener(window, "load", function() {makePopupCalendar('${objId}','${btnPos}')});
	</script>
</c:if>


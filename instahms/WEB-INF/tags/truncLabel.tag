<%@tag body-content="empty" dynamic-attributes="dynattrs" pageEncoding="UTF-8"%>

<%--
	This is like a <label> tag, but it truncates the input value into the given number of
	characters. If there is a truncation happening, it adds a ... to indicate that truncation has
	happened. In addition, it adds a tooltip to the label.

	Example:
  		<insta:truncLabel value="${remarks}" length="10"/>

--%>

<%@taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<%@attribute name="value" required="true" %>
<%@attribute name="length" required="true" type="java.lang.Integer" %>
<c:set var="nowrap" value="white-space: nowrap"/>
<c:set var="foundStyle" value="false"/>
<c:choose>
	<c:when test="${fn:length(value) gt length}">
		<label 
			<c:forEach items="${dynattrs}" var="a"> 
					<c:choose>
						<c:when test="${a.key == 'style'}">
							${ifn:cleanHtmlAttribute(a.key)}="${nowrap}; ${ifn:cleanHtmlAttribute(a.value)}"
							<c:set var="foundStyle" value="true"/>
						</c:when>
						<c:otherwise>${ifn:cleanHtmlAttribute(a.key)}="${ifn:cleanHtmlAttribute(a.value)}"</c:otherwise>
					</c:choose>
				</c:forEach>
				<c:if test="${!foundStyle}">style="white-space: nowrap"</c:if>
			title="${fn:escapeXml(value)}" ><c:out value="${fn:substring(value, 0, length-2)}"/>...
		</label>
	</c:when>
	<c:otherwise>
		<label 	<c:forEach items="${dynattrs}" var="a"> 
					<c:choose>
						<c:when test="${a.key == 'style'}">
							${ifn:cleanHtmlAttribute(a.key)}="${nowrap}; ${ifn:cleanHtmlAttribute(a.value)}"
							<c:set var="foundStyle" value="true"/>
						</c:when>
						<c:otherwise>${ifn:cleanHtmlAttribute(a.key)}="${ifn:cleanHtmlAttribute(a.value)}"</c:otherwise>
					</c:choose>
				</c:forEach>
				<c:if test="${!foundStyle}">style="white-space: nowrap"</c:if> >
			<c:out value="${value}"/>
		</label>
	</c:otherwise>
</c:choose>


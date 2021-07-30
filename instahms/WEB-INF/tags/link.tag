<%@ tag dynamic-attributes="dynattrs" pageEncoding="UTF-8"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>

<%@ attribute name="type" required="true" %>
<%@ attribute name="file" required="false" %>	<%-- relative to the type directory, eg, scripts --%>
<%@ attribute name="path" required="false" %>	<%-- full path, takes priority over file --%>
<%@ attribute name="content" required="false" %>	<%-- for href, the content --%>
<%@ attribute name="styleclass" required="false" %>	<%-- for href, image: the style class, if any --%>
<%@ attribute name="clickevent" required="false" %>	<%-- for href, image: any script for click event --%>
<%@ attribute name="display" required="false" %>	<%-- for href, image: the title attribute --%>

<%-- For cache-buster query param, we are using the software version from application.properties --%>
<c:set var="cb"><fmt:message key='insta.software.version'/></c:set>
<c:set var="stylesDir" value="${pageDirection == 'rtl' ? 'cssrtl' : 'css'}"/>
<c:set var="typeDir">
	<c:choose>
		<c:when test="${type eq 'css' || type eq 'style'}">${stylesDir}</c:when>
		<c:when test="${type eq 'js' || type eq 'script'}">scripts</c:when>
		<c:when test="${type eq 'image' || type eq 'img'}">images</c:when>
		<c:when test="${type eq 'href' || type eq 'hyperlink'}">pages</c:when>
	</c:choose>
</c:set>

<c:set var="fullPath">
	<c:choose>
		<c:when test="${not empty path}">${pageContext.request.contextPath}/${path}</c:when>
		<c:otherwise>${pageContext.request.contextPath}/${typeDir}/${file}</c:otherwise>
	</c:choose>
</c:set>

<c:if test="${not empty styleclass}">
	<c:set var="styleAttr">class="${styleclass}"</c:set>
</c:if>

<c:if test="${not empty clickevent}">
	<c:set var="clickAttr">onclick="${clickevent}"</c:set>
</c:if>

<c:if test="${not empty display}">
	<c:set var="titleAttr">title="${display}"</c:set>
</c:if>

<c:set var="otherAttr">
	<c:forEach items="${dynattrs}" var="a"> ${a.key}="${ifn:cleanHtml(a.value)}"</c:forEach>
</c:set>

<c:choose>
	<c:when test="${type eq 'css' || type eq 'style'}">
		<link type="text/css" rel="stylesheet" href="${fullPath}?${cb}"/>
	</c:when>
	<c:when test="${type eq 'js' || type eq 'script'}">
		<script type="text/javascript" src="${fullPath}?${cb}" charset="UTF-8"></script>
	</c:when>
	<c:when test="${type eq 'image' || type eq 'img'}">
		<img ${clickAttr} ${titleAttr} ${otherAttr} src="${fullPath}?${cb}"/>
	</c:when>
	<c:when test="${type eq 'href' || type eq 'hyperlink'}">
		<a ${clickAttr} ${titleAttr} ${otherAttr} ${styleAttr} href="${fullPath}">${content}</a>
	</c:when>
</c:choose>



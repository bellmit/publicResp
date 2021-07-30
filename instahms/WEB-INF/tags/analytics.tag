<%@ tag dynamic-attributes="dynattrs" pageEncoding="UTF-8"%>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri='http://java.sun.com/jsp/jstl/functions' prefix='fn' %>

<!-- Generic parameters required for setting html tag -->
<%@ attribute name="tagType" required="true" %>
<%@ attribute name="clickevent" required="false" %>

<!-- Parameter required for google event tracking -->
<%@attribute name="category" required="true" %>
<%@attribute name="action" required="true" %>
<%@attribute name="label" required="false" %>
<%@attribute name="value" required="false" %>

<!-- setting the onclick event -->
<c:choose>
    <c:when test="${not empty category && not empty action && not empty clickevent}">
        <c:set var="clickAttr">onclick='eventTracking("${category}","${action}","${label}");${clickevent}'</c:set>
    </c:when>
    <c:otherwise>
        <c:set var="clickAttr">onclick='eventTracking("${category}","${action}","${label}");'</c:set>
    </c:otherwise>
</c:choose>

<!-- setting the rest parameters -->
<c:set var="otherAttr">
    <c:forEach items="${dynattrs}" var="a"> ${a.key}="${ifn:cleanHtml(a.value)}"</c:forEach>
</c:set>

<c:choose>
    <c:when test="${tagType eq 'hyperLink'}">
        <a ${clickAttr} ${otherAttr} > <jsp:doBody/> </a>
    </c:when>
    <c:when test="${tagType eq 'button'}">
        <button ${clickAttr} ${otherAttr} > <jsp:doBody/> </button>
    </c:when>
</c:choose>

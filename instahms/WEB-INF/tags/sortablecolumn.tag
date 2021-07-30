<%@tag pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ attribute name="name" required="true" %>
<%@ attribute name="title" required="true" %>
<%@ attribute name="add_th" required="false" %>
<%@ attribute name="tooltip" required="false"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%--
	Generates a table head cell for a sortable column. For use in dashboards.
	Assumes that the parameters "sortOrder" and "sortReverse" are used to indicate
	the current sorting. Also, the parameters "msg" and "error" are stripped from the
	new URL that is created, assuming these are temporary variables.

	Example Usage:
	<tr>
		<insta:sortablecolumn name="reg_time" title="Registration Date"/>
		<td>Non sortable column</td>
		<th align="right"><insta:sortablecolumn name="sl_no" add_th="false" title="#"/></th>
		...
	</tr>
--%>

<c:set var="columnHeaderHelp">
<insta:ltext key="sortablecolumn.header.title"/>
</c:set>
<c:set var="sortTypeAscending">
<insta:ltext key="sortablecolumn.sorttype.ascending"/>
</c:set>
<c:set var="sortTypeDescending">
<insta:ltext key="sortablecolumn.sorttype.descending"/>
</c:set>
<%--added this flag to append "?" if no sorting query params found--%>
<c:set var="paramsFound" value="false"/>

<%--reqUri has been added with reference to HMS-8373. Need to get the requestUri without the "/" at the beginning.
hence getting the substring from 1 to the length of the request. We need to do this because c:url appends the context
path if it detects a url at the start
  --%>
<c:set var="reqUri" value = "${requestScope['javax.servlet.forward.request_uri']}"/>
<c:set var="reqUri" value="${fn:substring(reqUri,1,fn:length(reqUri))}"/>
<c:url var="urlWithoutSort" value="${reqUri}">
	<%-- add all the request parameters except sort params as parameters to the search URL --%>
	<c:forEach var="p" items="${param}">
		<c:if test="${p.key != 'sortOrder' && p.key != 'sortReverse' && p.key != 'msg' && p.key != 'error'}">
			<c:forEach items="${paramValues[p.key]}" var="value">	<%-- handle multival params --%>
				<c:param name="${p.key}" value="${value}"/>
				<c:set var="paramsFound" value="true"/>
			</c:forEach>
		</c:if>
	</c:forEach>
</c:url>

<c:set var="urlWithoutSort" value="${fn:escapeXml(urlWithoutSort)}"/>

<c:if test="${empty add_th}"><c:set var="add_th" value="true"/></c:if>
<c:if test="${not empty tooltip}">
	<c:set var="columnHeaderHelp" value="${tooltip} (${columnHeaderHelp})"/>
</c:if>
<c:if test="${add_th}">
	<th>
</c:if>
	<c:set var="sortSpec">
		<c:if test="${name == param.sortOrder}">&amp;sortReverse=${not param.sortReverse}</c:if>
	</c:set>

	<a href='/${urlWithoutSort}${paramsFound ? '&' : "?"}sortOrder=${name}${sortSpec}'
        	title="${columnHeaderHelp}">${title}</a>
	<c:if test="${name == param.sortOrder}">
		<img src="${pageContext.request.contextPath}/icons/tablesort${ifn:cleanURL(param.sortReverse) ? 'up' : 'down'}.gif"
			align="absmiddle" style="border: 1px solid; border-color: #AAAAAA; margin-left: 5px"
			title="${ifn:cleanHtmlAttribute(param.sortReverse) ? sortTypeDescending : sortTypeAscending}">
	</c:if>
<c:if test="${add_th}">
	</th>
</c:if>


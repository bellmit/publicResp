<%@ tag body-content="empty" dynamic-attributes="dynatr" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib prefix="insta" tagdir="/WEB-INF/tags" %>
<%@ attribute name="addEditUrl" required="true" %>
<%@ attribute name="displayName" required="true" %>
<%@ attribute name="legends" required="false" %>

		<c:set var="cpath" value="${pageContext.request.contextPath}"/>
		<c:set var="hasResults" value="${empty pagedList.dtoList ? false : true}"/>
		
		<c:url var="url" value="${addEditUrl}.do">
				<c:param name="_method" value="add"/>
		</c:url>

		<div class="screenActions" style="float:left"><a href="<c:out value='${url}' />">Add ${displayName}</a></div>
		<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
		<c:forEach items="${fn:split(legends,',')}" var="legendItem" varStatus="status">
			<c:set var="legendPart" value="${fn:split(legendItem,':')}"/>
			<div class="flag"><img src='${cpath}/images/${legendPart[0]}'></div>
			<div class="flagText">${legendPart[1]}</div>
		</c:forEach>
		</div>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<html>

<head>
	<title>Send Message - Insta HMS</title>
	<meta http-equiv="Content-Type"  content="text/html;charset=iso-8859-1">
	<insta:link type="script" file="ajax.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>
	<insta:link type="js" file="messaging/messaging.js"/>
	<insta:link type="css" file="widgets.css"/>
  <script>
    contextPath = "${pageContext.request.contextPath}";
    publicPath = contextPath + "/ui/";
  </script>	
</head>

<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="providerList" value="${providerList}" />
<c:set var="currentProvider" value="${currentProvider}" />
<c:set var="messageDataList" value="${messageData[currentProvider]}"/>
<c:set var="hasResult" value="${not empty messageDataList}"/>
<c:set var="urlPrefix" value=""/>
<c:set var="searchUrl" value="${cpath}/message/MessageLog.do?_method=list" />
<c:set var="detailsUrl" value="message/MessageLog.do?_method=show" />

<body>
<div class="pageHeader">Send Message - Select Recipients - ${currentProvider}</div>

<form method="GET" action='<c:out value="${searchUrl}"/>' name="searchForm">

<input type="hidden" name="_method" value="list" id="_method">
<input type="hidden" name="_searchMethod" value="list" id="_searchMethod">
<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>
<input type="hidden" name="_hasResult" value="${hasResult}"/>

<!-- new css and new list pattern-->
	<c:if test ="${not empty messageDataList}" >
	<div class="resultList">
		<table class="resultList" cellpadding="0" cellspacing="0" align="center" width="100%" id="resultTable" >
				<tr>
				<c:forEach var="token" items="${tokens}" varStatus="tokenStatus">
					<th>${token}</th>
<!-- 				<th>Message Type</th>
					<th>Mode</th>
					<th>Sent Date</th>
					<th>Last Send Status</th>
					<th>Retry Count</th> -->
				</c:forEach>
				</tr>

				<c:forEach var="recipientData" items="${messageDataList}" varStatus="status">
					<tr class="${status.index == 0 ?'firstRow': ''} ${status.index % 2 == 0? 'even':'odd' }">
				<c:forEach var="token" items="${tokens}" varStatus="tokenStatus">
						<td>${recipientData[token]}</td>
				</c:forEach>
					</tr>
				</c:forEach>
			</table>
		</div>
	</c:if>
	<insta:noresults hasResults="${hasResult}"/>
	</form>
<script>

/*
Toolbar for the page
*/

var toolbar = {};
toolbar.ViewMessage = {
	title: "View Message",
	imageSrc: "icons/View.png",
	href: '${detailsUrl}',
	target: '_blank',
	onclick: null,
	description: "View Message"
};
</script>
</body>
</html>

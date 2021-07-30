<%@page import="com.bob.hms.common.Constants" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
<title>Reports Dashboard - Insta HMS</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<meta http-equiv="refresh" content="30">
<insta:link type="script" file="dashboardColors.js"/>
<insta:link type="js" file="widgets.js"/>
<insta:link type="css" file="widgets.css"/>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<style type="text/css">
	.status_A { background-color: #EAD6BB;}
	.status_F { background-color: #DDDA8A}
	.status_C { background-color: #E0E8E0}
	.status_D { background-color: #ffa07a}
	table.legend {border-collapse : collapse ; margin-left : 6px }
	table.legend td {border : 1px solid grey ;padding 2px 5px }
	table.search td { white-space: nowrap }

	.scrolForContainer .yui-ac-content{
		 max-height:11em;overflow:auto;overflow-x:hidden; /* scrolling */
	    _height:11em; /* ie6 */
	}
</style>
<script>
		var contextPath = '<%=request.getContextPath()%>';
	</script>
</head>
<body class="yui-skin-sam">
<div class="pageHeader">Report Dashboard</div>
<insta:feedback-panel/>
	<div class="resultList">
		<table class="resultList" cellpadding="0" cellspacing="0" id="resultTable">
			<tr>
				<th>Report Name</th>
				<th>Generated Date/Time</th>
				<th>Status</th>
				<th>Actions</th>
			</tr>
			<c:forEach var="report" items="${reportList}">
				<c:set var="flagColor">
					<c:choose>
						<c:when test="${report.status == 'completed'}">green</c:when>
						<c:when test="${report.status == 'queued'}">yellow</c:when>
						<c:when test="${report.status == 'failed'}">red</c:when>
						<c:otherwise>empty</c:otherwise>
					</c:choose>
				</c:set>
				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}">
					<td>${report.file_name}</td>
					<c:choose>
						<c:when test="${report.status == 'completed'}">
							<td>${report.creation_time}</td>
							<td><img class="flag" src="${cpath}/images/${flagColor}_flag.gif"/> ${report.status}</td>
							<td><a href=${cpath}/reportdashboard/download.htm?id=${report.id} target="_blank">View/Download</a></td>
						</c:when>
						<c:when test="${report.status == 'failed'}">
							<td>${report.creation_time}</td>
							<td><img class="flag" src="${cpath}/images/${flagColor}_flag.gif"/> ${report.status}</td>
							<td><a href=${cpath}/reportdashboard/download.htm?id=${report.id} target="_blank">View Log</a></td>
							<!-- <td></td> -->
						</c:when>
						<c:otherwise>
							<td></td>
							<td><img class="flag" src="${cpath}/images/${flagColor}_flag.gif"/> ${report.status}</td>
							<td></td>
						</c:otherwise>
					</c:choose>
				</tr>
			</c:forEach>
		</table>
	</div>

	<div class="legend">
		<div class="flag"><img src='${cpath}/images/green_flag.gif'></div>
		<div class="flagText"> Completed</div>
		<div class="flag"><img src='${cpath}/images/yellow_flag.gif'></div>
		<div class="flagText"> Queued</div>
		<div class="flag"><img src='${cpath}/images/red_flag.gif'></div>		
		<div class="flagText"> Failed</div>
	</div>
</body>

</html>

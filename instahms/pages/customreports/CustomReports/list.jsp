<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<html>
<head>
<title>Custom Reports List - Insta HMS</title>
<insta:link type="css" file="widgets.css" />
<insta:link type="js" file="widgets.js" />
<insta:link type="js" file="hmsvalidation.js" />
<insta:link type="js" file="dashboardsearch.js"/>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<script>
	function validate(){
		deleteRecord=confirm("Do you want to delete");
		if(deleteRecord==true){
			document.forms[0].submit();
			return true;
		}else{
			return false;
		}
	}
	
	var toolbar = {

			Rights: {
				title: "Edit Report Rights",
				imageSrc: "icons/Change.png",
				href: '/customreports/CustomReports.do?method=editReportRights&%params',
				target: '_blank',
				onclick: null,
				description: "View and/or Edit the rights of this report."
			}
	};
	
	function init(){
		createToolbar(toolbar);
	}
</script>
</head>

<body onload="init();">
	<div class="pageHeader">Custom Reports</div>
	<div style="text-align:left"><insta:feedback-panel /></div>

	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

	<div class="resultList">
	<table class="resultList" align="center" width="99%">
		<tr>
			<insta:sortablecolumn name="report_name" title="Name"/>
			<th>Type</th>
			<th>Description</th>
			<th>Run Report</th>
			<c:if test="${roleId == 1}">
				<th>Delete</th>
			</c:if>
		</tr>

		<c:forEach var="report" items="${pagedlist.dtoList}" varStatus="st">
			<tr>
				<td onclick="showToolbar(${st.index}, event, 'resultList',
							{'%params' : '${report.map.report_id}',_savedreport : '${report.map.report_name}',_myreport:'${report.map.report_id}'},
							[${roleId == 1 || roleId == 2 || urlRightsMap.custom_rpt_add == 'A'  }]);"
						onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}" width="20%">${report.map.report_name}</td>
				<td onclick="showToolbar(${st.index}, event, 'resultList',
							{'%params' : '${report.map.report_id}',_savedreport : '${report.map.report_name}',_myreport:'${report.map.report_id}'},
							[${roleId == 1 || roleId == 2 || urlRightsMap.custom_rpt_add == 'A'  }]);"
						onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}">
					<c:choose>
						<c:when test="${report.map.report_type == 'csv'}">CSV</c:when>
						<c:when test="${report.map.report_type == 'srjs'}">Builder</c:when>
						<c:otherwise>PDF</c:otherwise>
					</c:choose>
				</td>
				<td onclick="showToolbar(${st.index}, event, 'resultList',
							{'%params' : '${report.map.report_id}',_savedreport : '${report.map.report_name}',_myreport:'${report.map.report_id}'},
							[${roleId == 1 || roleId == 2 || urlRightsMap.custom_rpt_add == 'A'  }]);"
						onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}" width="60%" title="${report.map.report_desc}" class="label"
						style="white-space: nowrap;overflow: hidden;max-width: 20em">
					<c:out value="${report.map.report_desc}"/>
				</td>


				<td width="5%">
					<c:choose>
						<c:when test="${report.map.num_vars > 0}">
							<a target="_blank" href="./CustomReports.do?method=show&id=${report.map.report_id}">Run</a>
						</c:when>
						<c:otherwise>
							<a target="_blank" href="./CustomReports.do?method=runReport&id=${report.map.report_id}">Run</a>
						</c:otherwise>
					</c:choose>
				</td>

				<c:if test="${roleId == 1}">
					<td width="5%">
						<c:url var="dUrl" value="./CustomReports.do">
							<c:param name="method" value="delete"></c:param>
							<c:param name="id" value="${report.map.report_id}"></c:param>
						</c:url>
						<a href="${dUrl}" onclick="return validate();">Delete</a></td>
					</td>
				</c:if>
			</tr>
		</c:forEach>
	</table>
	</div>

	<div class="screenActions">
		<insta:screenlink screenId="custom_rpt_add" extraParam="?method=add" label="Add Custom Report" />
		&nbsp;|&nbsp;
		<a href="${cpath}/reportdashboard/list.htm" target="_blank">Report Dashboard</a>
	</div>

</body>
</html>


<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
	<title>Current IVF Sessions - Insta HMS</title>
	<meta http-equiv="Content-Type"  content="text/html;charset=iso-8859-1">
	<insta:link type="css" file="hmsNew.css" />
	<insta:link type="js" file="ajax.js" />
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="script" file="dashboardColors.js"/>
	<insta:link type="js" file="ivf/ivfsessions.js" />
	<insta:link type="js" file="dashboardsearch.js"/>
	<jsp:include page="/pages/Common/MrnoPrefix.jsp"/>

	<script>
		var contextPath = '<%=request.getContextPath()%>';
	</script>
</head>
<c:set var="ivfSessionList" value="${pagedlist.dtoList}"/>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="hasResults" value="${not empty ivfSessionList}"/>
<body onload="initivfSessionList();" class="yui-skin-sam">
<div class="pageHeader">Current IVF Sessions</div>
<insta:feedback-panel/>
<form name="ivfSessionListForm" method="get" >
<input type="hidden" name="_method" value="list">
	<input type="hidden" name="_searchMethod" value="list"/>
	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>
	<insta:search form="ivfSessionListForm" optionsId="optionalFilter" closed="${hasResults}" >

	<div class="searchBasicOpts" >
			<div class="sboField">
				<div class="sboFieldLabel">MR No/Patient Name:</div>
				<div class="sboFieldInput">
					<div id="mrnoAutoComplete">
						<input type="text" name="mr_no" id="mrno" value="${ifn:cleanHtmlAttribute(param.mr_no)}" />
						<div id="mrnoContainer" style="width: 300px"></div>
					</div>
				</div>
			</div>
	</div>
	<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
		<table class="searchFormTable" >
			<tr>
				<td>
					<div class="sfLabel">Start Date</div>
						<div class="sfField">
							<div class="sfFieldSub">From:</div>
							<insta:datewidget name="start_date" id="start_date0" value="${paramValues.start_date[0]}"/>
							</div>
						<div class="sfField">
							<div class="sfFieldSub">To:</div>
							<insta:datewidget name="start_date" id="start_date1" value="${paramValues.start_date[1]}"/>
							<%-- NOTE: tell the query-builder to use >= and <= operators for the dates --%>
							<input type="hidden" name="start_date@op" value="ge,le"/>
					</div>
				</td>
				<td class="last">
					<div class="sfLabel">Status</div>
					<div class="sfField">
						<insta:checkgroup name="cycle_status" selValues="${paramValues.cycle_status}"
							opvalues="O,P,D,C" optexts="Ordered,Pre Cycle Completed,Daily Treatment Completed,Cycle Completed"/>
					</div>
				</td>
					<td class="last"></td>
					<td class="last"></td>
				</tr>
			</table>
		</div>
	</insta:search>
	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
	<div class="resultList">
		<table class="resultList" cellpadding="0" cellspacing="0" id="resultTable" onmouseover="hideToolBar('');" >
			<tr onmouseover="hideToolBar();">
				<th>Mr No</th>
				<th>Patient ID</th>
				<th>Patient Name</th>
				<th>Start Date</th>
				<th>Cycle Status</th>
			</tr>
			<c:forEach var="ivf" items="${ivfSessionList}" varStatus="st">
			<fmt:formatDate value="${ivf.start_date}" pattern="dd-MM-yyyy" var="stDate" />

			<c:set var="preCycle" value="${ivf.cycle_status eq 'O'}"/>
			<c:set var="dailyTreatment" value="${ivf.cycle_status eq 'P'}"/>
			<c:set var="cycleCompletion" value="${ivf.cycle_status eq 'D'}"/>
				<tr  class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
				onclick="showToolbar(${st.index}, event, 'resultTable',
							{mr_no: '${ivf.mr_no}', patient_id: '${ivf.patient_id}',ivf_cycle_id: '${ivf.ivf_cycle_id}',
							start_date: '${stDate}'},[${preCycle},${dailyTreatment},${cycleCompletion}]);"
							onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}">
					<td>${ivf.mr_no}</td>
					<td>${ivf.patient_id}</td>
					<td>${ivf.patient_name}</td>
					<td>${stDate}</td>
					<td>
						<c:if test="${ivf.cycle_status == 'O'}">Ordered</c:if>
						<c:if test="${ivf.cycle_status == 'P'}">Pre Cycle Completed</c:if>
						<c:if test="${ivf.cycle_status == 'D'}">Daily Treatment Completed</c:if>
						<c:if test="${ivf.cycle_status == 'C'}">Cycle Completed</c:if>
					</td>
				</tr>
			</c:forEach>
		</table>
		<c:if test="${param._method == 'list'}">
			<insta:noresults hasResults="${hasResults}"/>
		</c:if>
	</div>
</form>
</body>
</html>
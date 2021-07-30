<html>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<c:set var="pkgList" value="${pagedList.dtoList}"/>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="hasResults" value="${not empty pkgList}"/>

<head>
	<title>Patient Packages - Insta HMS</title>
</head>

<body class="yui-skin-sam" onload="init();">

<h1>Patient Packages</h1>

<insta:feedback-panel/>

<form name="patpackageform">
	<input type="hidden" name="_method" value="list"/>
	<input type="hidden" name="_searchMethod" value="list"/>

	<insta:search form="patpackageform" optionsId="optionalFilter" closed="false" >
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

			<div class="sboField">
				<div class="sboFieldLabel">&nbsp;
					<div class="sboFieldInput">
						<input type="checkbox" name="_mr_no" id="_mr_no" onclick="changeStatus()"/>Active Only
					</div>
				</div>
			</div>
		</div>
		<div id="optionalFilter" style="clear: both; display:'block'" >
			<table class="searchFormTable">
				<tr>
					<td>
						<div class="sfLabel">Order date</div>
						<div class="sfField">
							<div class="sfFieldSub">From:</div>
							<insta:datewidget name="presc_date" id="presc_date0"
								value="${paramValues.presc_date[0]}"/>
						</div>
						<div class="sfField">
							<div class="sfFieldSub">To:</div>
							<insta:datewidget name="presc_date" id="presc_date1"
								value="${paramValues.presc_date[1]}"/>
							<input type="hidden" name="presc_date@op" value="ge,le"/>
							<input type="hidden" name="presc_date@cast" value="y"/>
						</div>
					</td>
					<td>
					<div class="sfLabel">Conduction Status</div>
					<div class="sfField">
						<insta:checkgroup name="status" selValues="${paramValues.status}"
						opvalues="P,C" optexts="In Progress,Done"/>
					</div>
					</td>
				</tr>
			</table>
		</div>
 	</insta:search>

<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

<div class="detailList" >

	<table class="detailList" cellspacing="" cellpadding="" id="resultTable" onmouseover="hideToolBar();">
		<tr onmouseover="hideToolBar();">
			<th>#</th>
			<th>Mr No.</th>
			<th>Patient Name</th>
			<th>
				<input type="checkbox" name="checkAllForCompleted"
						onclick="return checkOrUncheckAll(this)"/>
				Package Name
			</th>
			<th>Order Time</th>
			<th>Conduction Status</th>
		</tr>


		<c:forEach var="record" items="${pkgList}" varStatus="st" >

			<c:set var="flagColor">
				<c:choose>
					<c:when test="${record.handed_over == 'Y'}">yellow</c:when>
					<c:when test="${record.status == 'C'}">green</c:when>
					<c:when test="${record.status == 'X'}">red</c:when>
					<c:otherwise>empty</c:otherwise>
				</c:choose>
			</c:set>
			<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
					id="toolbarRow${st.index}"
					onclick="showToolbar(${st.index}, event, 'resultTable',
						{prescription_id: '${record.prescription_id}',patient_id: '${record.patient_id}',mr_no: '${record.mr_no}'},
						[${record.status != 'X' },${ record.handover_to == 'P'  && record.status == 'C' && record.handed_over == 'N' },${ record.handover_to == 'P'  && record.status == 'C' && record.handed_over == 'N' }]);">
				<td>${((pagedList.pageNumber-1) * pagedList.pageSize) + st.index + 1}</td>
				<td>${record.mr_no}</td>
				<td>
					${record.patient_name}</td>
				<td>
					<input type="checkbox" name="_prescription_id" value="${record.prescription_id}" onclick="return checkPackageStatus('${record.status}')" ${(record.handover_to == 'S' && record.package_status != 'C' ) ? '' : 'disabled' } />
					<insta:truncLabel value="${record.package_name}" length="30"/>
				</td>
				<td><fmt:formatDate value="${record.presc_date}" pattern="dd-MM-yyyy HH:mm"/></td>
				<td>
					<img class="flag" src="${cpath}/images/${flagColor}_flag.gif"/>
					<input type="hidden" name="_package_status" value="${record.status}"/>
					${record.status_text }
				</td>
			</tr>
		</c:forEach>
	</table>

	<div style="float: left; margin-top: 10px;display: ${hasResults ? 'block' : 'none'}">
		<button type="button" accesskey="C" onclick="complete()">
		<label><b><u>C</u></b>omplete Packages</label></button>
	</div>

	<div class="legend" >
		<div class="flag"><img src="${cpath}/images/green_flag.gif"></div>
		<div class="flagText">Done</div>
	</div>
</form>

	<script>var cpath = '${cpath}';</script>
	<insta:link type="script" file="dashboardsearch.js"/>
	<jsp:include page="/pages/Common/MrnoPrefix.jsp" />
	<insta:link type="script" file="AdvancedPackages/patient_packages.js"/>

</body>
</html>


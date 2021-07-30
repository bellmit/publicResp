<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page isELIgnored="false" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@page import="com.insta.hms.integration.URLRoute"%>
<c:set var="pagePath" value="<%=URLRoute.REMITTANCE_UPLOAD %>"/>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Updated Remittance List - Insta HMS</title>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>
	<insta:link type="script" file="dashboardColors.js"/>

	<c:set var="cpath" value="${pageContext.request.contextPath}"/>

</head>

<body>

	<c:set var="hasResults" value="${not empty pagedList.dtoList ? 'true' : 'false'}"/>

	<h1>Remittance Log</h1>

	<insta:feedback-panel/>

	<form name="remform" method="GET">

		<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
		<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

		<insta:search-lessoptions form="remform" >
			<div class="searchBasicOpts" >
                <div class="sboField">
                    <div class="sboFieldLabel">File Name:</div>
                    <div class="sboFieldInput">
                        <input type="text" name="file_name" value="${ifn:cleanHtmlAttribute(param.file_name)}" />
                        <input type="hidden" name="file_name@op" value="ico"/>
                    </div>
                </div>
				<div class="sboField">
					<div class="sboFieldLabel">Insurance Comp Name:</div>
					<div class="sboFieldInput">
						<input type="text" name="insurance_co_name" value="${ifn:cleanHtmlAttribute(param.insurance_co_name)}" />
						<input type="hidden" name="insurance_co_name@op" value="ico"/>
					</div>
				</div>
				<div class="sboField" style="height:69">
					<div class="sboFieldLabel">TPA/Sponsor Name:</div>
					<div class="sboFieldInput">
						<input type="text" name="tpa_name" value="${ifn:cleanHtmlAttribute(param.tpa_name)}" />
						<input type="hidden" name="tpa_name@op" value="ico"/>
					</div>
				</div>
				<div class="sboField" style="height: 140px">
					<div class="sboFieldLabel">Status: </div>
					<div class="sboFieldInput">
						<insta:checkgroup name="processing_status" optexts="Scheduled,Processing,Failed,Completed" opvalues="S,P,F,C" selValues="${paramValues.processing_status}"/>
					</div>
				</div>	
			</div>
		</insta:search-lessoptions>

		<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

		<div class="detailList" >
			<table class="detailList" cellspacing="" cellpadding="" id="resultTable" onmouseover="hideToolBar();">
				<tr onmouseover="hideToolBar();">
					<th>#</th>
					<insta:sortablecolumn name="insurance_co_name" title="Insurance Comp Name"/>
					<insta:sortablecolumn name="tpa_name" title="TPA"/>
					<insta:sortablecolumn name="file_name" title="File Name"/>
					<insta:sortablecolumn name="received_date" title="Received Date"/>
					<insta:sortablecolumn name="processing_status" title="Status"/>
					<th>Download</th>
				</tr>
				<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}" id="toolbarRow${st.index}">

						<td>${(pagedList.pageNumber - 1) * pagedList.pageSize + (st.index + 1)}</td>
						<td title="${record.insurance_co_name}"> <insta:truncLabel value="${record.insurance_co_name}" length="25"/> </td>
						<td title="${record.tpa_name}"> <insta:truncLabel value="${record.tpa_name}" length="25"/> </td>
						<td title="${record.file_name}"> <insta:truncLabel value="${record.file_name}" length="55" /> </td>
						<td>${record.received_date}</td>
						<c:choose>
							<c:when test="${record.processing_status == 'S'}">
								<td>Scheduled</td>
								<td></td>
							</c:when>
							<c:when test="${record.processing_status == 'P'}">
								<td>Processing</td>
								<td></td>
							</c:when>
							<c:when test="${record.processing_status == 'F'}">
								<td>Failed</td>
								<td><a href="${cpath}${pagePath}/exporterror.htm?remittanceId=${record.remittance_id}">Error Log</td>
							</c:when>
							<c:when test="${record.processing_status == 'I'}">
								<td>Partially Completed</td>
								<td><a href="${cpath}${pagePath}/exporterror.htm?remittanceId=${record.remittance_id}">Error Log</td>
							</c:when>
							<c:when test="${record.processing_status == 'C'}">
								<td>Completed</td>
								<td></td>
							</c:when>
						<c:otherwise>
							<td></td>
							<td></td>
						</c:otherwise>
						</c:choose>
					</tr>
				</c:forEach>
			</table>

			<c:if test="${empty pagedList.dtoList}">
				<insta:noresults hasResults="${hasResults}"/>
			</c:if>

		</div>
		
		<c:url var="downloadUrl" value="${pagePath}/remittanceDownloadList.htm?sortReverse=true"/>
		

		<div class="screenActions" style="float: left">
			<a href="${downloadUrl}">Remittance Advice Download</a>
		</div>
</form>
</body>
</html>





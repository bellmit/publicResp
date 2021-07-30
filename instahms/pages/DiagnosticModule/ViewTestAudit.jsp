<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<html>

<head>
	<title>View Test Audit - Insta HMS</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="script" file="widgets.js"/>
	<insta:link type="script" file="dashboardColors.js"/>

	<style type="text/css">
		.status_A.type_P { background-color: #EAD6BB }
		.status_C { background-color: #C5D9A3 }
		.status_X { color: grey }

</style>
</head>
<c:set var="old_val" value=""/>
<c:set var="new_val" value=""/>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="hasResults" value="${not empty pagedList.dtoList}"/>
<body>
	<div class="pageHeader">View Test Audit</div>
	<div class="infoPanel" style="display: ${hasResults ? 'block' : 'none'}">
		<div class="img"><img src="${cPath}/images/information.png"/></div>
		<div class="txt">Older Tests will display fewer audit records.</div>
		<div style="clear: both"></div>
	</div>
	<form action="/pages/DiagnosticModule/TestAuditList.do" method="GET">
	<input type="hidden" name="_method" value="viewTestAuditLog">
	<input type="hidden" name="pageNum" id="pageNum" value="${pagedList.pageNumber}"/>
	<input type="hidden" name="testName" value="${ifn:cleanHtmlAttribute(requestScope.testName)}"/>
	<h2> Test Name: ${ifn:cleanHtml(requestScope.testName)}</h2>

	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
	<table  class="dashboard hideemptycells" width="100%" cellspacing="0" cellpadding="0">
		<tr>
			<th>User</th>
			<th>Date &amp; Time</th>
			<th>Description</th>
			<th>Old Value</th>
			<th>New Value</th>
		</tr>
		<c:forEach var="logList" items="${pagedList.dtoList}" varStatus="st">
			<tr>
				<td>${logList.map.username}&nbsp;</td>
				<td><fmt:formatDate value="${logList.map.datetime}" pattern="dd-MM-yyyy HH:mm" />&nbsp;</td>
					<c:choose>
						<c:when test="${logList.map.operation eq 'INSERT' }">
							<c:choose>
								<c:when test="${logList.map.db_table eq 'tests_prescribed'}">
									<c:if test="${logList.map.db_field_name eq 'pres_doctor'}">
										<td>Test Prescribed by ${logList.map.doctor_name}</td>
									</c:if>
									<c:if test="${logList.map.db_field_name eq 'pres_date'}">
										<td>Prescribed on ${logList.map.db_field_new_val}</td>
									</c:if>
									<c:if test="${logList.map.db_field_name eq 'pres_time'}">
										<td>Prescribed time ${logList.map.db_field_new_val}</td>
									</c:if>
								</c:when>
								<c:when test="${logList.map.db_table eq 'tests_conducted'}">
									<c:if test="${logList.map.db_field_name eq 'conducted_by'}">
										<td>Conducted by ${logList.map.doctor_name}</td>
									</c:if>
									<c:if test="${logList.map.db_field_name eq 'conducted_date'}">
										<td>Conducted on ${logList.map.db_field_new_val}</td>
									</c:if>
									<c:if test="${logList.map.db_field_name eq 'conducted_time'}">
										<td>Conducted time ${logList.map.db_field_new_val}</td>
									</c:if>
								</c:when>
								<c:when test="${logList.map.db_table eq 'sample_collection'}">
									<c:if test="${logList.map.db_field_name eq 'sample_sno'}">
										<td>Sample Collected, Id: ${logList.map.db_field_new_val}</td>
									</c:if>
								</c:when>
								<c:when test="${logList.map.db_table eq 'test_details'}">
										<td>Test Details inserted</td>
								</c:when>
								<c:when test="${logList.map.db_table eq 'test_visit_reports'}">
									<td>Test Visit Reports inserted</td>
								</c:when>
								<c:otherwise>
									<td>Record inserted to ${logList.map.db_table}</td>
								</c:otherwise>
							</c:choose>
						</c:when>
						<c:when test="${logList.map.operation eq 'UPDATE' }">
							<c:choose>
								<c:when test="${logList.map.db_table eq 'tests_prescribed'}">
									<c:choose>
											<c:when test="${logList.map.db_field_name eq 'conducted'}">
												<td>Conducted status </td>
											</c:when>
											<c:when test="${logList.map.db_field_name eq 'conducted_by'}">
												<td>Conducted By ${logList.map.doctor_name} </td>
											</c:when>
											<c:when test="${logList.map.db_field_name eq 'remarks'}">
												<td>Remarks updated</td>
											</c:when>
											<c:when test="${logList.map.db_field_name eq 'report_id'}">
												<td>Report generated</td>
											</c:when>
											<c:when test="${logList.map.db_field_name eq 'cancelled_by'}">
												<td>Cancelled by</td>
											</c:when>
											<c:otherwise>
											<td>${logList.map.db_field_name} modified in ${logList.map.db_table}</td>
											</c:otherwise>
										</c:choose>
								</c:when>
								<c:when test="${logList.map.db_table eq 'tests_conducted'}">
									<c:choose>
											<c:when test="${logList.map.db_field_name eq 'conducted_by'}">
												<td>Conduction updated By ${logList.map.doctor_name} </td>
											</c:when>
											<c:when test="${logList.map.db_field_name eq 'conducted_date'}">
												<td>Conduction updated on</td>
											</c:when>
											<c:when test="${logList.map.db_field_name eq 'conducted_time'}">
												<td>Conduction updated time</td>
											</c:when>
											<c:otherwise>
											<td>${logList.map.db_field_name} modified in ${logList.map.db_table}</td>
											</c:otherwise>
										</c:choose>
								</c:when>
								<c:when test="${logList.map.db_table eq 'test_visit_reports'}">
									<c:choose>
											<c:when test="${logList.map.db_field_name eq 'signed_off'}">
												<td>Signoff status </td>
											</c:when>
											<c:otherwise>
											<td>${logList.map.db_field_name} modified in ${logList.map.db_table}</td>
											</c:otherwise>
										</c:choose>
								</c:when>
								<c:otherwise>
								<td>${logList.map.db_field_name} modified in ${logList.map.db_table}</td>
								</c:otherwise>
							</c:choose>
						</c:when>
						<c:otherwise>
							<td>${logList.map.operation}${logList.map.db_field_name}&nbsp;</td>
						</c:otherwise>
					</c:choose>
					<c:choose>
						<c:when test="${(logList.map.db_table eq 'tests_prescribed') and (logList.map.db_field_name eq 'conducted') }">
							<c:if test="${logList.map.db_field_old_val eq 'N'}">
								<c:set var="old_val" value="Prescribed"/>
							</c:if>
							<c:if test="${logList.map.db_field_new_val eq 'N'}">
								<c:set var="new_val" value="Prescribed"/>
							</c:if>
							<c:if test="${logList.map.db_field_old_val eq 'P'}">
								<c:set var="old_val" value="Partial Conducted"/>
							</c:if>
							<c:if test="${logList.map.db_field_new_val eq 'P'}">
								<c:set var="new_val" value="Partial Conducted"/>
							</c:if>
							<c:if test="${logList.map.db_field_old_val eq 'Y'}">
								<c:set var="old_val" value="Conducted"/>
							</c:if>
							<c:if test="${logList.map.db_field_new_val eq 'Y'}">
								<c:set var="new_val" value="Conducted"/>
							</c:if>
							<c:if test="${logList.map.db_field_old_val eq 'Cancel'}">
								<c:set var="old_val" value="Cancelled"/>
							</c:if>
							<c:if test="${logList.map.db_field_new_val eq 'Cancel'}">
								<c:set var="new_val" value="Cancelled"/>
							</c:if>
							<c:if test="${logList.map.db_field_old_val eq 'MA'}">
								<c:set var="old_val" value="Modality Arrived"/>
							</c:if>
							<c:if test="${logList.map.db_field_new_val eq 'MA'}">
								<c:set var="new_val" value="Modality Arrived"/>
							</c:if>
							<c:if test="${logList.map.db_field_old_val eq 'CC'}">
								<c:set var="old_val" value="Conduction Completed"/>
							</c:if>
							<c:if test="${logList.map.db_field_new_val eq 'CC'}">
								<c:set var="new_val" value="Conduction Completed"/>
							</c:if>
							<c:if test="${logList.map.db_field_old_val eq 'TS'}">
								<c:set var="old_val" value="Scheduled for Transcriptionist"/>
							</c:if>
							<c:if test="${logList.map.db_field_new_val eq 'TS'}">
								<c:set var="new_val" value="Scheduled for Transcriptionist"/>
							</c:if>
							<c:if test="${logList.map.db_field_old_val eq 'CR'}">
								<c:set var="old_val" value="Change Required"/>
							</c:if>
							<c:if test="${logList.map.db_field_new_val eq 'CR'}">
								<c:set var="new_val" value="Change Required"/>
							</c:if>
						</c:when>
						<c:when test="${(logList.map.db_table eq 'test_visit_reports') and (logList.map.db_field_name eq 'signed_off') }">
							<c:if test="${logList.map.db_field_old_val eq 'N'}">
								<c:set var="old_val" value="No"/>
							</c:if>
							<c:if test="${logList.map.db_field_new_val eq 'N'}">
								<c:set var="new_val" value="No"/>
							</c:if>
							<c:if test="${logList.map.db_field_old_val eq 'Y'}">
								<c:set var="old_val" value="Yes"/>
							</c:if>
							<c:if test="${logList.map.db_field_new_val eq 'Y'}">
								<c:set var="new_val" value="Yes"/>
							</c:if>
						</c:when>
						<c:otherwise>
						    <c:set var="old_val" value="${logList.map.db_field_old_val}"/>
						    <c:choose>
						    	<c:when test="${(logList.map.operation eq 'INSERT')&&(logList.map.db_table eq 'tests_prescribed')}">
						    		<c:set var="new_val" value=""/>
						    	</c:when>
						    	<c:when test="${(logList.map.operation eq 'INSERT')&&(logList.map.db_table eq 'tests_conducted')}">
						    		<c:set var="new_val" value=""/>
						    	</c:when>
						    	<c:when test="${(logList.map.operation eq 'UPDATE')&&(logList.map.db_table eq 'tests_conducted')}">
						    		<c:set var="new_val" value=""/>
						    	</c:when>
						    	<c:when test="${(logList.map.operation eq 'UPDATE')&&(logList.map.db_table eq 'tests_prescribed')&&(logList.map.db_field_name eq 'report_id') }">
						    		<c:set var="new_val" value=""/>
						    	</c:when>
						    	<c:when test="${(logList.map.operation eq 'INSERT')&&(logList.map.db_table eq 'sample_collection')&&(logList.map.db_field_name eq 'sample_sno')}">
						    		<c:set var="new_val" value=""/>
						    	</c:when>
						    	<c:otherwise>
						    		<c:set var="new_val" value="${logList.map.db_field_new_val}"/>
						    	</c:otherwise>
						    </c:choose>
						</c:otherwise>
				</c:choose>
				<td>${old_val}&nbsp;</td>
				<td>${new_val}&nbsp;</td>
			</tr>
		</c:forEach>
	</table>
	<insta:noresults hasResults="${hasResults}" message="No logs recorded"/>
</body>
</html>


<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<%@page import="com.insta.hms.integration.URLRoute"%>
<c:set var="pagePath" value="<%=URLRoute.SELF_PAY_CLAIM_SUBMISSION %>"/>

<jsp:useBean id="statusDisplay" class="java.util.HashMap"/>
<c:set target="${statusDisplay}" property="O" value="Open"/>
<c:set target="${statusDisplay}" property="S" value="Sent"/>
<c:set target="${statusDisplay}" property="X" value="Rejected"/>
<c:set target="${statusDisplay}" property="C" value="Completed"/>
<c:set target="${statusDisplay}" property="F" value="Failed"/>
<c:set target="${statusDisplay}" property="P" value="Processing"/>
<c:set target="${statusDisplay}" property="N" value="Not Scheduled"/>


<jsp:useBean id="resubmissionDisplay" class="java.util.HashMap"/>
<c:set target="${resubmissionDisplay}" property="Y" value="Yes"/>
<c:set target="${resubmissionDisplay}" property="N" value="No"/>

<jsp:useBean id="patientTypeDisplay" class="java.util.HashMap"/>
<c:set target="${patientTypeDisplay}" property="*" value="All"/>
<c:set target="${patientTypeDisplay}" property="o" value="OP"/>
<c:set target="${patientTypeDisplay}" property="i" value="IP"/>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<script>
	var eClaimModule		= '${preferences.modulesActivatedMap['mod_eclaim']}';
	var haadClaimRights		= '${urlRightsMap.insurance_haad_claim}';
	var accumedClaimRights	= '${urlRightsMap.insurance_accumed_claim}';
	var advPackagesMod	= '${preferences.modulesActivatedMap['mod_adv_packages']}';
	var batchType = '${param.submissionType}';
</script>
<title>Self Pay Submissions List - Insta HMS</title>
<insta:link type="js" file="widgets.js"/>
<insta:link type="css" file="widgets.css"/>
<insta:link type="script" file="dashboardLookup.js"/>
<insta:link type="script" file="billing/claimsCommon.js"/>
<insta:link type="script" file="billing/selfpayclaimlist.js"/>

</head>

<c:set var="claimSubmissionsList" value="${pagedList.dtoList}"/>
<c:set var="hasResults" value="${not empty claimSubmissionsList}"/>
<c:set var="corpInsurance" value='<%=GenericPreferencesDAO.getAllPrefs().get("corporate_insurance")%>'/>

<body onload="init();">

<h1>Self Pay Claim / Person Register Submission</h1>
<insta:feedback-panel/>
<form name="selfpayClaimSubmissionsForm" method="GET">
	<input type="hidden" name="_method" value="list">
	<input type="hidden" name="_searchMethod" value="list"/>

	<insta:find form="selfpayClaimSubmissionsForm" optionsId="optionalFilter" closed="${hasResults}" validateFunction="doSearch()" clearFunction="clearForm">
	<div class="searchBasicOpts">
		<div class="sboField">
			<div class="sboFieldLabel"> Submission Batch ID: </div>
			<div class="sboFieldInput">
				<input type="text" name="batch_id" value="${ifn:cleanHtmlAttribute(param.batch_id)}">
				<input type="hidden" name="selfpay_batch_id@type" value="integer"/>
			</div>
		</div>
<!-- 		<div style="width: 100%"> -->
				<div class="sboFieldLabel">Batch Type:</div>
				<div class="sboFieldInput">
							<input type="radio" name="submissionType" id="SelfPay" value="SP" ${param.submissionType== 'SP' ? 'checked' :''} >Self Pay Claim Batch
							<input type="radio" name="submissionType" id="PersonRegister" value="PR" ${param.submissionType == 'PR' ? 'checked':''}>Person Register Batch
			
		</div>
	</div>

	<div id="optionalFilter" style="clear: both; display : ${hasResults ? 'none' : 'block'}" >
	<table class="searchFormTable">
		<tr>
			<td>
				<div class="sfLabel">Created Date:</div>
				<div class="sfField">
					<div class="sfFieldSub">From:</div>
						<insta:datewidget name="created_at" id="created_at0" value="${paramValues.created_at[0]}"/>
					</div>
					<div class="sfField">
						<div class="sfFieldSub">To:</div>
						<insta:datewidget name="created_at" id="created_at1" value="${paramValues.created_at[1]}"/>
						<input type="hidden" name="created_at@type" value="date"/>
						<input type="hidden" name="created_at@op" value="ge,le"/>
						<input type="hidden" name="created_at@cast" value="y"/>
					</div>
			</td>
			<td>
				<div class="sfLabel">Status:</div>
				<div class="sfField">
					<insta:checkgroup name="status" selValues="${paramValues.status}"
						opvalues="O,S" optexts="Open,Sent"/>
				</div>
			</td>			
			<td>
				<div class="sfLabel">Patient Type:</div>
				<div class="sfField">
					<insta:checkgroup name="visit_type" selValues="${paramValues.visit_type}"
						opvalues="o,i" optexts="OP,IP"/>
				</div>
			</td>
		</tr>
		<tr>
			<td>
				<div class="sfLabel">Submission Date:</div>
				<div class="sfField">
						<div class="sfFieldSub">From:</div>
						<insta:datewidget name="submission_date" id="submission_date0" value="${paramValues.submission_date[0]}"/>
					</div>
					<div class="sfField">
						<div class="sfFieldSub">To:</div>
						<insta:datewidget name="submission_date" id="submission_date1" value="${paramValues.submission_date[1]}"/>
						<input type="hidden" name="submission_date@type" value="date"/>
						<input type="hidden" name="submission_date@op" value="ge,le"/>
						<input type="hidden" name="submission_date@cast" value="y"/>
				</div>
			</td>
			
			
			
			<td>
				<div class="sfLabel">Center/Account Group:</div>
				<div class="sfField">

				<select name="center_or_account_group" id="center_or_account_group" class="dropdown">
					<option value=""> (All) </option>
					<c:forEach items="${accountGrpAndCenterList}" var="acc">
						<option value="${acc.map.id}" ${paramValues.center_or_account_group[0] == acc.map.id ? 'selected':''}>${acc.map.ac_name}-(${acc.map.accounting_company_name})</option>
					</c:forEach>
				</select>
				<input type="hidden" name="center_or_account_group@type" value="text"/>
				</div>
			</td>
		</tr>
	</table>
	</div>
	</insta:find>
</form>
<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

	<div class="resultList">
		<table class="resultList" cellspacing="0" cellpadding="0" align="center" width="100%" id="resultTable"
		onmouseover="hideToolBar('');">
			<tr>
				<insta:sortablecolumn name="selfpay_batch_id" title="Submission Batch ID"/>
				<th>Service Reg No.</th>
				<insta:sortablecolumn name="visit_type" title="Patient Type"/>
				<th>Center/Account Group</th>
				<insta:sortablecolumn name="status" title="Status"/>
				<insta:sortablecolumn name="processing_status" title="Job Status"/>
				<th>Submission Date</th>
				<th>Created Date</th>
			</tr>
			<c:set var="rowSubmissionId" value=""/>
			<c:forEach var="record" items="${claimSubmissionsList}" varStatus="st">
				<c:set var="i" value="${st.index}"/>
					<c:set var="FlagColor">
					<c:choose>
						<c:when test="${record.status eq 'O'}">empty</c:when>
						<c:when test="${record.status eq 'S'}">grey</c:when>
					</c:choose>
					</c:set>
					<c:set var="sentEnable"     value="${record.status eq 'O' && record.processing_status ne 'P'} "/>
					<c:set var="deleteEnable"   value="${record.status eq 'O' && record.processing_status ne 'P'}"/>
					<c:set var="downloadXMLEnable"   value="${record.processing_status eq 'C' }"/>
					<c:set var="generateXMLEnable"   value="${(record.processing_status ne 'C' && record.processing_status ne 'P' && !fn:contains(record.batch_id, 'PR'))||(record.processing_status ne 'P' && record.status ne 'S' && fn:contains(record.batch_id, 'PR'))}"/>
					<c:set var="viewErrorEnable"   value="${record.processing_status eq 'F' }"/>
					<c:set var="uploadEnable"   value="${record.processing_status eq 'C' && record.status ne 'S'}"/>

					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
					onclick="showToolbar(${st.index}, event, 'resultTable',
					{batch_id:'${record.batch_id}',submissionType:'${param.submissionType}'},
					[${generateXMLEnable}, ${sentEnable}, ${deleteEnable}, ${downloadXMLEnable}, ${viewErrorEnable}, ${uploadEnable}])"
					onmouseover="hideToolBar(${st.index})" 	id="toolbarRow${st.index}">
						<td>
							<c:if test="${record.batch_id ne rowSubmissionId}">${record.batch_id}</c:if>
							<c:if test="${record.batch_id eq rowSubmissionId}">
								<i><u> ${record.batch_id} </u></i></c:if>
						</td>
						<td>${(empty record.account_group_service_reg_no || record.account_group_service_reg_no == '') ? record.hospital_center_service_reg_no : record.account_group_service_reg_no}</td>
						<td>${patientTypeDisplay[record.visit_type]}</td>
						<td>${record.account_group_id == 0 ? record.center_name : record.account_group_name}</td>
						
						<td>
							<img src="${cpath}/images/${FlagColor}_flag.gif"/>
							${statusDisplay[record.status]}
						</td>
						<td>
							${statusDisplay[record.processing_status]}
						</td>
						<td><fmt:formatDate value="${record.submission_date}" pattern="dd-MM-yyyy"/></td>
						<td><fmt:formatDate value="${record.created_at}" pattern="dd-MM-yyyy"/></td>
					</tr>
					<c:set var="rowSubmissionId" value="${record.batch_id}"/>
			</c:forEach>
		</table>

		<insta:noresults hasResults="${hasResults}"/>
	</div>

	<div class="screenActions" style="float:left">
		<a href="${cpath}${pagePath}/create.htm">Self Pay Claim / Person Register Submission</a>
	</div>

	<div class="legend" style="display:${hasResults ? 'block' : 'none'}">
		<div class="flag"><img src="${cpath}/images/empty_flag.gif"></img></div>
		<div class="flagText">Open</div>
		<div class="flag"><img src="${cpath}/images/grey_flag.gif"></img></div>
		<div class="flagText">Sent</div>
	</div>
</body>
</html>

<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<jsp:useBean id="statusDisplay" class="java.util.HashMap"/>
<c:set target="${statusDisplay}" property="O" value="Open"/>
<c:set target="${statusDisplay}" property="S" value="Sent"/>
<c:set target="${statusDisplay}" property="X" value="Rejected"/>

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
	var isAccumed		= '${preferences.modulesActivatedMap['mod_accumed']}';
	var haadClaimRights		= '${urlRightsMap.insurance_haad_claim}';
	var accumedClaimRights	= '${urlRightsMap.insurance_accumed_claim}';
	var advPackagesMod	= '${preferences.modulesActivatedMap['mod_adv_packages']}';
</script>
<title>Submissions List - Insta HMS</title>
<insta:link type="js" file="widgets.js"/>
<insta:link type="css" file="widgets.css"/>
<insta:link type="script" file="dashboardsearch.js"/>
<insta:link type="script" file="billing/claimsCommon.js"/>
<insta:link type="script" file="billing/claimsubmissionslist.js"/>

<script>
	var companyList    = [];
	var categoryList   = ${insCategoryList};
	var tpaList        = ${tpaList};
	var planList       = [];
</script>
</head>

<c:set var="claimSubmissionsList" value="${pagedList.dtoList}"/>
<c:set var="hasResults" value="${not empty claimSubmissionsList}"/>
<c:set var="corpInsurance" value='<%=GenericPreferencesDAO.getAllPrefs().get("corporate_insurance")%>'/>

<body onload="init();">

<h1>Claim Submissions List</h1>
<insta:feedback-panel/>
<form name="claimSubmissionsForm" method="GET">
	<input type="hidden" name="_method" value="list">
	<input type="hidden" name="_searchMethod" value="list"/>

	<insta:search form="claimSubmissionsForm" optionsId="optionalFilter" closed="${hasResults}" validateFunction="doSearch()" clearFunction="clearForm">
	<div class="searchBasicOpts">
		<div class="sboField">
			<div class="sboFieldLabel"> Submission Batch ID: </div>
			<div class="sboFieldInput">
				<input type="text" name="submission_batch_id" value="${ifn:cleanHtmlAttribute(param.submission_batch_id)}">
			</div>
		</div>
	</div>

	<div id="optionalFilter" style="clear: both; display : ${hasResults ? 'none' : 'block'}" >
	<table class="searchFormTable">
		<tr>
			<td>
				<div class="sfLabel">Created Date:</div>
				<div class="sfField">
						<div class="sfFieldSub">From:</div>
						<insta:datewidget name="created_date" id="created_date0" value="${paramValues.created_date[0]}"/>
					</div>
					<div class="sfField">
						<div class="sfFieldSub">To:</div>
						<insta:datewidget name="created_date" id="created_date1" value="${paramValues.created_date[1]}"/>
						<input type="hidden" name="created_date@op" value="ge,le"/>
						<input type="hidden" name="created_date@cast" value="y"/>
				</div>
			</td>
			<td>
				<div class="sfLabel">Status:</div>
				<div class="sfField">
					<insta:checkgroup name="status" selValues="${paramValues.status}"
						opvalues="O,S,X" optexts="Open,Sent,Rejected"/>
				</div>
			</td>
			<td>
				<div class="sfLabel">Ins. Company Name:</div>
				<div class="sfField">
					<insta:selectdb displaycol="insurance_co_name" name="insurance_co_id" id="insurance_co_id" value="${param.insurance_co_id}"
					table="insurance_company_master" valuecol="insurance_co_id" dummyvalue="(All)" onchange="onChangeInsuranceCompany()"/>
				</div>
			</td>
			<td>
				<div class="sfLabel">Sponsor Name: </div>
				<div class="sfField">
					<insta:selectdb displaycol="tpa_name" name="tpa_id" id="tpa_id" value="${param.tpa_id}"
					table="tpa_master" valuecol="tpa_id" dummyvalue="(All)" onchange="onChangeTPA()"/>
				</div>
			</td>
			<td>
				<div class="sfLabel">Patient Type:</div>
				<div class="sfField">
					<insta:checkgroup name="patient_type" selValues="${paramValues.patient_type}"
						opvalues="*,o,i" optexts="(OP & IP),only OP,only IP"/>
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
				<div class="sfLabel">Resubmission</div>
				<div class="sfField">
					<insta:checkgroup name="is_resubmission" selValues="${paramValues.is_resubmission}"
						opvalues="Y,N" optexts="Yes,No"/>
				</div>
			</td>			
			<td colspan="2">
				<div class="sfLabel">Network/Plan Type:</div>
				  <div class="sfField">
				  	<c:set var="inscatSelected" value="${fn:join(paramValues.category_id, ' ')}"/>
							<select name="category_id" id="category_id"  multiple="multiple" class="listbox" optionTitle="true"
										style="border-top:1px #999 solid; border-left:1px #999 solid; border-bottom:1px #ccc solid; 
										border-right:1px #ccc solid; width:350px;"
										onchange="onChangeInsuranceCategory()" >
								<option value="(All)">(All)</option>
								<c:forEach items="${inscatname}" var="inscat" >
									<c:set var="selected" value="${fn:contains(inscatSelected,inscat.map.category_id)?'selected':''}"/>
									<option value="${inscat.map.category_id}" ${selected}>${inscat.map.category_name}</option>
								</c:forEach>
									<input type="hidden" name="category_id@type" value="text"/>
							</select>
				</div>
			</td>
			<td>
				<div class="sfLabel">Plan Name:</div>
				<input type="text" name="plan_name" id="plan_name" style="width: 138px;">
				<div id="plan_name_dropdown" class="scrolForContainer"></div>
				<input type="hidden" name="plan_id" id="plan_id">
				<input type="hidden" name="plan_id@type" value="integer"/>
				
			</td>
		</tr>
		<tr>
			<td>
				<div class="sfLabel">Center/Account Group:</div>
				<div class="sfField">

				<input type="text" name="center_or_account_group" id="center_or_account_group" 
							/>
				<div id="center_or_account_group_dropdown" class="scrolForContainer"></div>
				<input type="hidden" name="center_or_account_group_id" id = "center_or_account_group_id"/>
				<input type="hidden" name="center_or_account_group@type" value="text"/>
				</div>
			</td>
			<td>
				<div class="sfLabel">Claim Format</div>
				<div class="sfField">
					<insta:checkgroup name="claim_format" selValues="${paramValues.claim_format}"
						opvalues="XML,XL" optexts="XML,XL"/>
				</div>
			</td>
			<td>
				<div class="sfLabel">Job Status</div>
				<div class="sfField">
					<insta:checkgroup name="processing_status" selValues="${paramValues.processing_status}"
						opvalues="P,F,C,N" optexts="In-Progress,Failed,Success,Not Scheduled"/>
				</div>
			</td>
			<td></td>
			<td></td>
		</tr>
	</table>
	</div>
	</insta:search>
</form>
<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

	<div class="resultList">
		<table class="resultList" cellspacing="0" cellpadding="0" align="center" width="100%" id="resultTable"
		onmouseover="hideToolBar('');">
			<tr>
				<insta:sortablecolumn name="submission_batch_id" title="Submission Batch ID"/>
				<insta:sortablecolumn name="status" title="Status"/>
				<th>Job Type</th>
				<th>Job Status</th>
				<th>Service Reg No.</th>
				<insta:sortablecolumn name="tpa_name" title="Sponsor Name"/>
				<insta:sortablecolumn name="insurance_co_name" title="Ins. Company Name"/>
				<insta:sortablecolumn name="category_name" title="Network/Plan Type"/>
				<insta:sortablecolumn name="plan_name" title="Plan Name"/>
				<insta:sortablecolumn name="patient_type" title="Patient Type"/>
				<th>Center/Account Group</th>
				<th>Claim Format</th>
				<insta:sortablecolumn name="is_resubmission" title="Resubmission"/>
				<th>Submission Date</th>
				<th>Created Date</th>
			</tr>
			<c:set var="rowSubmissionId" value=""/>
			<c:forEach var="record" items="${claimSubmissionsList}" varStatus="st">
				<c:set var="i" value="${st.index}"/>
					<c:set var="flagColor">
					<c:choose>
						<c:when test="${record.status eq 'O'}">empty</c:when>
						<c:when test="${record.status eq 'S'}">grey</c:when>
						<c:when test="${record.status eq 'X'}">red</c:when>
					</c:choose>
					</c:set>

					<c:if test="${mod_eclaim}">
						<c:set var="eClaimEnable" value="${mod_eclaim && record.status ne 'X' && record.claim_format eq 'XML' && record.status eq 'O' && record.processing_status ne 'P'}"/>
						<c:set var="uploadEClaimEnable" value="${mod_eclaim && record.status ne 'X' && record.claim_format eq 'XML' && record.status eq 'O' && record.processing_status eq 'C' }"/>
						<c:set var="testEnable"   value="${mod_eclaim && record.status ne 'X' && record.claim_format eq 'XML' && record.status eq 'O' && record.processing_status ne 'P'}"/>
						<c:set var="eClaimError" value="${mod_eclaim && record.status ne 'X' && record.claim_format eq 'XML' && record.processing_status eq 'F'}"/>
					</c:if>
					<c:set var="sentEnable"     value="${record.status eq 'O'} "/>
					<c:set var="rejectEnable"   value="${record.status eq 'S'}"/>
					<c:set var="deleteEnable"   value="${record.status eq 'O'}"/>

					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
					onclick="showToolbar(${st.index}, event, 'resultTable',
					{submission_batch_id:'${record.submission_batch_id}', processing_type: '${record.processing_type}'},
					[${eClaimEnable},${eClaimEnable},${eClaimEnable}, ${testEnable}
					,${sentEnable}, true, ${rejectEnable},${deleteEnable},${record.status ne 'X'},${record.processing_status eq 'C'},${uploadEClaimEnable},${eClaimError}])"
					onmouseover="hideToolBar(${st.index})" 	id="toolbarRow${st.index}">
						<td>
							<c:if test="${record.submission_batch_id ne rowSubmissionId}">${record.submission_batch_id}</c:if>
							<c:if test="${record.submission_batch_id eq rowSubmissionId}">
								<i><u> ${record.submission_batch_id} </u></i></c:if>
						</td>
						<td>
							<img src="${cpath}/images/${flagColor}_flag.gif"/>
							${statusDisplay[record.status]}
						</td>
						<td>${empty record.processing_type  ? '' : record.processing_type == 'T' ? 'Test' : 'Production'}</td>
						
						<td>${empty record.processing_status || record.processing_status eq 'N' ? 'Not Scheduled' : record.processing_status == 'P' ? 'In-Progress' : record.processing_status == 'F' ? 'Failed' : 'Success'}</td>
						
						<td>${(empty record.account_group_service_reg_no || record.account_group_service_reg_no == '') ? record.hospital_center_service_reg_no : record.account_group_service_reg_no}</td>
						<td><insta:truncLabel value="${empty record.tpa_name ? 'All' : record.tpa_name}" length="15"/></td>
						<td><insta:truncLabel value="${empty record.insurance_co_name ? 'All' :record.insurance_co_name}" length="15"/></td>
						<td><insta:truncLabel value="${empty record.category_name ? 'All' :record.category_name}" length="15"/></td>
						<td><insta:truncLabel value="${empty record.plan_name ? 'All' :record.plan_name}" length="15"/></td>
						<td>${patientTypeDisplay[record.patient_type]}</td>
						<td>${record.account_group == 0 ? record.center_name : record.account_group_name}</td>
						<td>${empty record.claim_format ? 'All' :record.claim_format}</td>
						
						<td>${resubmissionDisplay[record.is_resubmission]}</td>
						<td><fmt:formatDate value="${record.submission_date}" pattern="dd-MM-yyyy"/></td>
						<td><fmt:formatDate value="${record.created_date}" pattern="dd-MM-yyyy"/></td>
					</tr>
					<c:set var="rowSubmissionId" value="${record.submission_batch_id}"/>
			</c:forEach>
		</table>

		<insta:noresults hasResults="${hasResults}"/>
	</div>

	<div class="screenActions" style="float:left">
		<a href="./raiseClaimSubmission.do?_method=add">New Claim Batch Submission</a>
	</div>

	<div class="legend" style="display:${hasResults ? 'block' : 'none'}">
		<div class="flag"><img src="${cpath}/images/empty_flag.gif"></img></div>
		<div class="flagText">Open</div>
		<div class="flag"><img src="${cpath}/images/grey_flag.gif"></img></div>
		<div class="flagText">Sent</div>
		<div class="flag"><img src="${cpath}/images/red_flag.gif"></img></div>
		<div class="flagText">Rejected</div>
	</div>
</body>
</html>

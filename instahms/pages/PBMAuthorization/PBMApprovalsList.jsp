<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@page import="com.insta.hms.stores.StoresDBTablesUtil"%>

<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<jsp:useBean id="pbmStatusDisplay" class="java.util.HashMap"/>
<c:set target="${pbmStatusDisplay}" property="O" value="Open"/>
<c:set target="${pbmStatusDisplay}" property="S" value="Sent"/>
<c:set target="${pbmStatusDisplay}" property="D" value="Denied"/>
<c:set target="${pbmStatusDisplay}" property="R" value="ForResub"/>
<c:set target="${pbmStatusDisplay}" property="C" value="Closed"/>

<jsp:useBean id="approvalStatus" class="java.util.HashMap"/>
<c:set target="${approvalStatus}" property="F" value="Fully Approved"/>
<c:set target="${approvalStatus}" property="P" value="Partially Approved"/>
<c:set target="${approvalStatus}" property="R" value="Fully Rejected"/>

<jsp:useBean id="prescStatus" class="java.util.HashMap"/>
<c:set target="${prescStatus}" property="Y" value="Fully Dispensed"/>
<c:set target="${prescStatus}" property="P" value="Partially Dispensed"/>
<c:set target="${prescStatus}" property="N" value="Not Dispensed"/>

<c:set var="hasPbmFinalizeRights" value="${((actionRightsMap.pbm_prescription_finalize == 'A')||(roleId==1)||(roleId==2))}"/>

<html>
<head>
	<title>PBM Approvals - Insta HMS</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<script type="text/javascript">
		var shafafiyaPBMLive = '${shafafiya_pbm_active}';
		var hasPbmFinalizeRights = '${hasPbmFinalizeRights}';
	</script>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="script" file="widgets.js"/>
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="script" file="billing/claimsCommon.js"/>
	<insta:link type="script" file="PBMAuthorization/pbmApprovals.js"/>
	<insta:link type="script" file="dashboardsearch.js"/>
	<script>
		var companyList    = ${insCompList};
		var categoryList   = ${insCategoryList};
		var tpaList        = ${tpaList};
		var planList       = ${planList};
		var extraDetails   = [];
	</script>
</head>

<c:set var="prescList" value="${pagedList.dtoList}"/>
<c:set var="hasResults" value="${not empty prescList}"/>

<body onload="init();ajaxForPrintUrls();showFilterActive(document.pbmApprovalSearchForm);">
<div id="storecheck" style="display: block;" >
<h1>PBM Approvals List</h1>

<insta:feedback-panel/>
<c:if test="${empty error}">
<form name="pbmApprovalSearchForm" method="GET">
	<input type="hidden" name="_method"  id ="_method" value="getApprovals">
	<input type="hidden" name="_searchMethod" value="getApprovals"/>

	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

	<insta:search form="pbmApprovalSearchForm" optionsId="optionalFilter" closed="${hasResults}" >
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
				<div class="sboFieldLabel">PBM Presc. ID: </div>
				<div class="sboFieldInput">
					<input type="text" name="pbm_presc_id" value="${ifn:cleanHtmlAttribute(param.pbm_presc_id)}">
					<input type="hidden" name="pbm_presc_id@type" value="integer"/>
				</div>
			</div>
			<div class="sboField">
				<div class="sboFieldLabel">PBM Request ID: </div>
				<div class="sboFieldInput">
					<input type="text" name="pbm_request_id" value="${ifn:cleanHtmlAttribute(param.pbm_request_id)}">
				</div>
			</div>
	   </div>
	 	<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
			<table class="searchFormTable">
				<tr>
					<td>
						<div class="sfLabel">Received Date:</div>
						<div class="sfField">
							<div class="sfFieldSub">From: </div>
							<insta:datewidget name="approval_recd_date" id="approval_recd_date0" value="${paramValues.approval_recd_date[0]}"/>
						</div>
						<div class="sfField">
							<div class="sfFieldSub">To: </div>
							<insta:datewidget name="approval_recd_date" id="approval_recd_date1" value="${paramValues.approval_recd_date[1]}"/>
							<input type="hidden" name="approval_recd_date@op" value="ge,le"/>
							<input type="hidden" name="approval_recd_date@cast" value="y"/>
						</div>
						<div class="sfLabel">Finalized:</div>
						<div class="sfField">
							<insta:checkgroup name="pbm_finalized" selValues="${paramValues.pbm_finalized}"
							opvalues="Y,N" optexts="Yes,No"/>
						</div>
					</td>
					<td>
						<div class="sfLabel">Ins. Company Name:</div>
						<div class="sfField">
							<insta:selectdb displaycol="insurance_co_name" name="insurance_co_id" id="insurance_co_id" value="${param.insurance_co_id}"
							table="insurance_company_master" orderby="insurance_co_name"
							valuecol="insurance_co_id" dummyvalue="(All)" onchange="onChangeInsuranceCompany()"/>
						</div>
						<div class="sfLabel">Network/Plan Type:</div>
						<div class="sfField">
							<insta:selectdb displaycol="category_name" name="category_id"
							id="category_id" values="${paramValues.category_id}" orderby="category_name"
							multiple="multiple" class="listbox" optionTitle="true"
							style="border-top:1px #999 solid; border-left:1px #999 solid; border-bottom:1px #ccc solid; border-right:1px #ccc solid;"
							table="insurance_category_master" valuecol="category_id" dummyvalue="(All)" onchange="onChangeInsuranceCategory()"/>
							<input type="hidden" name="category_id@type" value="text"/>
						</div>
					</td>
					<td>
						<div class="sfLabel">TPA Name:</div>
						<div class="sfField">
							<insta:selectdb displaycol="tpa_name" name="tpa_id" id="tpa_id" value="${param.tpa_id}"
							table="tpa_master" orderby="tpa_name"
							valuecol="tpa_id" dummyvalue="(All)" onchange="onChangeTPA()"/>
						</div>
						<div class="sfLabel">Plan Name:</div>
						<div class="sfField">
							<insta:selectdb displaycol="plan_name" name="plan_id" id="plan_id" value="${param.plan_id}"
							table="insurance_plan_main" orderby="plan_name"
							valuecol="plan_id" dummyvalue="(All)"/>
							<input type="hidden" name="plan_id@type" value="integer"/>
						</div>
						<div class="sfLabel">Resubmit:</div>
						<div class="sfField">
							<insta:checkgroup name="is_resubmit" selValues="${paramValues.is_resubmit}"
							opvalues="Y,N" optexts="Yes,No"/>
						</div>
					</td>
					<td>
						<div class="sfLabel">PBM Presc. Status:</div>
						<div class="sfField">
							<insta:checkgroup name="pbm_presc_status" selValues="${paramValues.pbm_presc_status}"
							opvalues="O,S,D,R,C" optexts="Open,Sent,Denied,ForResub,Closed"/>
						</div>
					</td>
					<td>
						<div class="sfLabel">Approval Status</div>
						<div class="sfField">
							<insta:checkgroup name="approval_status" selValues="${paramValues.approval_status}"
							opvalues="F,P,R" optexts="Fully Approved,Partially Approved,Fully Rejected"/>
						</div>
						<div class="sfLabel">Prescription Status</div>
						<div class="sfField">
							<insta:checkgroup name="presc_status" selValues="${paramValues.presc_status}"
							opvalues="N,P,Y" optexts="Not Dispensed,Partially Dispensed,Fully Dispensed"/>
						</div>
					</td>
				</tr>
			</table>
	   </div>
	</insta:search>
 </form>
	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
	<form method="POST" name="pbmNewApprovalsForm" action="./PBMApprovals.do">
		<input type="hidden" name="_method"  id ="_method" value="getPriorAuthResponse">
		From: 
		<insta:datewidget name="_from_date" id="_from_date0" value="${paramValues._from_date[0]}"/>
		To: 
		<insta:datewidget name="_to_date" id="_to_date" value="${paramValues._to_date[0]}"/>
		<input type="hidden" name="_to_date@op" value="ge,le"/>
		<input type="hidden" name="_to_date@cast" value="y"/>
		<button type="button" name="checkNewPBMAuths" value="searchAuth"
				accessKey="R" onclick="return approvalSearch();">
		<img class="flag" src="${cpath}/icons/Refresh.png"/>
		<b><u>R</u></b>efresh
		</button>
		<div class="resultList">
			<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable"">
				<tr onmouseover="hideToolBar();">
					<th>#</th>
					<th title="Prior Authorization File Id Received.">File</th>
					<th title="File Not Downloaded / Downloaded With Received Date and Time."><insta:sortablecolumn name="approval_recd_date" title="Received Date Time" add_th="false"/></th>
					<th>Approval status</th>
					<insta:sortablecolumn name="mr_no" title="MR No"/>
					<insta:sortablecolumn name="patient_id" title="Visit ID"/>
					<th>Patient Name</th>
					<insta:sortablecolumn name="pbm_presc_id" title="PBM Presc. ID"/>
					<th>PBM Presc. Status</th>
					<insta:sortablecolumn name="pbm_request_id" title="Request ID"/>
					<th>Request Date Time</th>
					<th>Drug Count</th>
					<insta:sortablecolumn name="tpa_name" title="TPA/Sponsor"/>
					<insta:sortablecolumn name="category_name" title="Network/Plan Type"/>
					<th>Authorization ID</th>
					<th>Request Type</th>
					<th>Resubmit Type</th>
					<th>Prescription Status</th>
				</tr>
				<c:forEach var="presc" items="${prescList}" varStatus="st">
				<c:set var="i" value="${st.index + 1}"/>
				<c:set var="flagColor">
					<c:choose>
						<c:when test="${presc.approval_status eq 'F'}">empty</c:when>
						<c:when test="${presc.approval_status eq 'P'}">blue</c:when>
						<c:when test="${presc.approval_status eq 'R'}">red</c:when>
					</c:choose>
				</c:set>
				<c:set var="enableUploadTestPriorAuth" value="${empty presc.approval_status && presc.member_id == '1116528'}"/>
				<c:set var="enableSales" value="${presc.pbm_request_type != 'Cancellation'}"/>
				<c:set var="apprvlStatus" value=""/>
				<c:choose>
						<c:when test="${not empty presc.approval_status}">
							<c:set var="apprvlStatus" value="${presc.approval_status}"/>
						</c:when>
						<c:when test="${presc.pbm_presc_status == 'S'}">
							<c:set var="apprvlStatus" value="${presc.pbm_presc_status}"/>
						</c:when>
				</c:choose>

				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'resultTable',
							{visit_id: '${presc.patient_id}', pbm_presc_id: ${presc.pbm_presc_id},
							pbm_request_id: '${presc.pbm_request_id}', approval_status: '${apprvlStatus}',
							approval_comments: '${presc.approval_comments}', consultation_id: ${presc.consultation_id}, row_id: ${st.index} },
							[true, true, ${enableUploadTestPriorAuth}, ${enableSales}, true]);"
						onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}"	>
					<td>
						${(pagedList.pageNumber-1) * pagedList.pageSize + st.index + 1}
						<input type="hidden" name="pid" id="pid${st.index}" value="${presc.pbm_presc_id}"/>
						<input type="hidden" name="prconsid" id="prconsid${st.index}" value="${presc.consultation_id}"/>
						<input type="hidden" name="prid" id="prid${st.index}" value="${presc.pbm_request_id}"/>
					</td>
					<td>
						<c:if test="${not empty presc.file_id && not empty presc.approval_status}">
							<img class="flag" src="${cpath}/icons/received.png"/>
						</c:if>
					</td>
					<td>
						<c:if test="${not empty presc.file_id && empty presc.approval_status}">
							<img class="flag" src="${cpath}/icons/not_received.png"/>
						</c:if>
						<fmt:formatDate pattern="dd-MM-yyyy HH:mm" value="${presc.approval_recd_date}"/>
					</td>
					<td>
						<c:choose>
						<c:when test="${not empty presc.approval_status}">
							<img class="flag" src="${cpath}/images/${flagColor}_flag.gif"/>${approvalStatus[presc.approval_status]}
						</c:when>
						<c:when test="${presc.pbm_presc_status == 'S'}">
							Pending...
						</c:when>
						</c:choose>
					</td>
					<td>${presc.mr_no }</td>
					<td>${presc.patient_id }</td>
					<td><insta:truncLabel value="${presc.patname}" length="15"/></td>
					<td>${presc.pbm_presc_id }</td>
					<td>${pbmStatusDisplay[presc.pbm_presc_status]}</td>
					<td>${presc.pbm_request_id }</td>
					<td><fmt:formatDate pattern="dd-MM-yyyy HH:mm" value="${presc.request_date}"/></td>
					<td>${presc.drug_count }</td>
					<td><insta:truncLabel value="${presc.tpa_name}" length="15"/></td>
					<td><insta:truncLabel value="${presc.category_name}" length="15"/></td>
					<td>${presc.pbm_auth_id_payer}</td>
					<td>${presc.pbm_request_type}</td>
					<td>${presc.resubmit_type}</td>
				   <td>${prescStatus[presc.presc_status]}</td>
				</tr>
				<c:if test="${fn:length(presc.approval_comments) > 0}">
					<c:choose>
						<c:when test="${fn:length(presc.approval_comments) > 200 }">
						<c:set var="truncComments" value="${fn:substring(presc.approval_comments, 0, 200)}..."></c:set>
						</c:when>
						<c:otherwise>
						<c:set var="truncComments" value="${presc.approval_comments}"></c:set>
						</c:otherwise>
					</c:choose>
					<script>
						extraDetails['toolbarRow${st.index}'] = {
							'Comments': <insta:jsString value="${truncComments}"/>
						};
					</script>
				</c:if>
				</c:forEach>
			</table>
			<c:if test="${param._method == 'getApprovals'}">
				<insta:noresults hasResults="${hasResults}"/>
			</c:if>
	    </div>
</form>

<form name="uploadTestingAuthForm" action="PBMApprovals.do" method="POST" enctype="multipart/form-data">
<input type="hidden" id="pbm_presc_id" value=""/>
<input type="hidden" name="pbm_request_id" value=""/>
<input type="hidden" name="uploadDialogId" value=""/>

<div id="uploadDialog" style="visibility:hidden">
	<div id="dialog_h" class="hd" style="cursor: move;">
		<label>Upload Prior-Auth XML file.</label>
	</div>
	<div class="bd">
		<table cellpadding="0" cellspacing="0">
		<tr>
			<td class="forminfo"><input type="file" name="prior_auth_file" id="prior_auth_file"  accept="<insta:ltext key="upload.accept.claim"/>"/></td>
		</tr>
		</table>
		<div style="padding-top:10px;margin-left:5px;width:400px">
			<button type="button" name="ok" id="ok" onclick="uploadSubmit();" accessKey="K">O<b><u>K</u></b></button>
			<button type="button" onclick="uploadCancel();" accessKey="A">C<b><u>A</u></b>ncel</button>
		</div>
	</div>
</div>
</form>

<div class="legend" style="display:${hasResults ? 'block' : 'none'}">
	<div class="flag"><img src="${cpath}/images/empty_flag.gif"></img></div>
	<div class="flagText">Fully Approved</div>
	<div class="flag"><img src="${cpath}/images/blue_flag.gif"></img></div>
	<div class="flagText">Partially Approved</div>
	<div class="flag"><img src="${cpath}/images/red_flag.gif"></img></div>
	<div class="flagText">Fully Rejected</div>
</div>
</c:if>
</body>
</html>

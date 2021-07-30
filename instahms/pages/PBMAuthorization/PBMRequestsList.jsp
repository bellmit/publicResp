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
<c:set target="${approvalStatus}" property="D" value="Fully Denied"/>

<jsp:useBean id="prescStatus" class="java.util.HashMap"/>
<c:set target="${prescStatus}" property="Y" value="Fully Dispensed"/>
<c:set target="${prescStatus}" property="P" value="Partially Dispensed"/>
<c:set target="${prescStatus}" property="N" value="Not Dispensed"/>

<html>
<head>
	<title>PBM Requests - Insta HMS</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="script" file="widgets.js"/>
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="script" file="billing/claimsCommon.js"/>
	<insta:link type="script" file="PBMAuthorization/pbmRequests.js"/>
	<insta:link type="script" file="dashboardsearch.js"/>
	<script>
		var companyList    = ${insCompList};
		var categoryList   = ${insCategoryList};
		var tpaList        = ${tpaList};
		var planList       = ${planList};
	</script>
</head>

<c:set var="prescList" value="${pagedList.dtoList}"/>
<c:set var="hasResults" value="${not empty prescList}"/>

<body onload="init(); showFilterActive(document.pbmRequestSearchForm);">
<div id="storecheck" style="display: block;" >
<h1>PBM Requests List</h1>

<insta:feedback-panel/>
<c:if test="${empty error}">
<form name="pbmRequestSearchForm" method="GET">
	<input type="hidden" name="_method"  id ="_method" value="getRequests">
	<input type="hidden" name="_searchMethod" value="getRequests"/>

	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

	<insta:search form="pbmRequestSearchForm" optionsId="optionalFilter" closed="${hasResults}" >
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
						<div class="sfLabel">Request Date:</div>
						<div class="sfField">
							<div class="sfFieldSub">From: </div>
							<insta:datewidget name="request_date" id="request_date0" value="${paramValues.request_date[0]}"/>
						</div>
						<div class="sfField">
							<div class="sfFieldSub">To: </div>
							<insta:datewidget name="request_date" id="request_date1" value="${paramValues.request_date[1]}"/>
							<input type="hidden" name="request_date@op" value="ge,le"/>
							<input type="hidden" name="request_date@cast" value="y"/>
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
						<div class="sfLabel">Request Type:</div>
						<div class="sfField">
							<insta:checkgroup name="pbm_request_type" selValues="${paramValues.pbm_request_type}"
							opvalues="Authorization,Cancellation" optexts="Authorization,Cancellation"/>
						</div>
					</td>
					<td>
						<div class="sfLabel">Approval Status</div>
						<div class="sfField">
							<insta:checkgroup name="approval_status" selValues="${paramValues.approval_status}"
							opvalues="F,P,R" optexts="Fully Approved,Partially Approved,Fully Rejected"/>
						</div>
					</td>
				</tr>
			</table>
	   </div>
	</insta:search>
 </form>
	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}" />
	<form method="POST">
		<div class="resultList">
			<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable"">
				<tr onmouseover="hideToolBar();">
					<th>#</th>
					<insta:sortablecolumn name="mr_no" title="MR No"/>
					<insta:sortablecolumn name="patient_id" title="Visit ID"/>
					<th>Patient Name</th>
					<th>PBM Presc. Status</th>
					<th>PBM Presc. ID</th>
					<insta:sortablecolumn name="pbm_request_id" title="Request ID"/>
					<th>Request Date Time</th>
					<th>Resubmission</th>
					<th>Resubmit Type</th>
					<th>Drug Count</th>
					<insta:sortablecolumn name="tpa_name" title="TPA/Sponsor"/>
					<insta:sortablecolumn name="category_name" title="Network/Plan Type"/>
					<th>Request Type</th>
					<th>Received Date Time</th>
					<th>Approval status</th>
					<th>Authorization ID</th>
					<th>Prescription Status</th>
				</tr>
				<c:forEach var="presc" items="${prescList}" varStatus="st">
				<c:set var="i" value="${st.index + 1}"/>
				<c:set var="flagColor">
					<c:choose>
						<c:when test="${presc.pbm_presc_status eq 'O'}">empty</c:when>
						<c:when test="${presc.pbm_presc_status eq 'S'}">violet</c:when>
						<c:when test="${presc.pbm_presc_status eq 'D'}">red</c:when>
						<c:when test="${presc.pbm_presc_status eq 'R'}">green</c:when>
						<c:when test="${presc.pbm_presc_status eq 'C'}">grey</c:when>
					</c:choose>
				</c:set>

				<%--
				PBM Cancel menu should not be available in the following cases:
				a) if the status is Open (O)
				b) if the cancellation request has already been sent. This is determined by the request_type
					(stored in pbm_request_approval_details)
				c) if the medicines have already been dispensed and the sale is part of a claim submission batch
					that has been marked as Sent.
				--%>

				<c:set var="requestEnable"  value="${presc.pbm_presc_status eq 'O'} "/>
				<c:set var="resubmitEnable" value="${presc.pbm_presc_status eq 'R' && presc.presc_status eq 'N'}"/>

				<c:set var="cancelEnable">
					<c:choose>
						<c:when test="${presc.pbm_presc_status eq 'O'}">false</c:when>
						<c:when test="${presc.pbm_request_type == 'Cancellation'}">false</c:when>
						<c:when test="${presc.presc_status ne 'N' && presc.claim_status ne 'O'}">false</c:when>
						<c:otherwise>true</c:otherwise>
					</c:choose>
				</c:set>

				<c:set var="cloneEnable"  value="${presc.pbm_request_type eq 'Cancellation'} "/>
				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'resultTable',
							{pbm_presc_id: ${presc.pbm_presc_id}, consultation_id: ${presc.consultation_id} , patient_id: '${presc.patient_id}',
							all_items_returned: ${presc.all_items_returned}, presc_status: '${presc.presc_status}' },
							[true, ${requestEnable}, ${resubmitEnable}, ${cancelEnable}, ${cloneEnable}]);"
						onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}"	>
					<td>${(pagedList.pageNumber-1) * pagedList.pageSize + st.index + 1}</td>
					<td>${presc.mr_no }</td>
					<td>${presc.patient_id }</td>
					<td><insta:truncLabel value="${presc.patname}" length="15"/></td>
					<td><img class="flag" src="${cpath}/images/${flagColor}_flag.gif"/>${pbmStatusDisplay[presc.pbm_presc_status]}</td>
					<td>${presc.pbm_presc_id }</td>
					<td>${presc.pbm_request_id }</td>
					<td><fmt:formatDate pattern="dd-MM-yyyy HH:mm" value="${presc.request_date}"/></td>
					<td>${presc.is_resubmit == 'Y' ? 'Yes' : 'No' }</td>
					<td>${presc.resubmit_type}</td>
					<td>${presc.drug_count }</td>
					<td><insta:truncLabel value="${presc.tpa_name}" length="15"/></td>
					<td><insta:truncLabel value="${presc.category_name}" length="15"/></td>
					<td>${presc.pbm_request_type}</td>
				   <td><fmt:formatDate pattern="dd-MM-yyyy HH:mm" value="${presc.approval_recd_date}"/></td>
				   <td>${approvalStatus[presc.approval_status]}</td>
				   <td>${presc.pbm_auth_id_payer }</td>
				   <td>${prescStatus[presc.presc_status]}</td>
				</tr>
				</c:forEach>
			</table>
			<c:if test="${param._method == 'getRequests'}">
				<insta:noresults hasResults="${hasResults}"/>
			</c:if>
	    </div>
</form>

<div class="legend" style="display:${hasResults ? 'block' : 'none'}">
	<div class="flag"><img src="${cpath}/images/empty_flag.gif"></img></div>
	<div class="flagText">Open</div>
	<div class="flag"><img src="${cpath}/images/violet_flag.gif"></img></div>
	<div class="flagText">Sent</div>
	<div class="flag"><img src="${cpath}/images/red_flag.gif"></img></div>
	<div class="flagText">Denied</div>
	<div class="flag"><img src="${cpath}/images/green_flag.gif"></img></div>
	<div class="flagText">ForResub.</div>
	<div class="flag"><img src="${cpath}/images/grey_flag.gif"></img></div>
	<div class="flagText">Closed</div>
</div>
</c:if>
</body>
</html>

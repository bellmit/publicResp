<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib uri="/WEB-INF/esapi.tld" prefix="esapi" %>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<jsp:useBean id="preauthStatusDisplay" class="java.util.HashMap"/>
<c:set target="${preauthStatusDisplay}" property="O" value="Open"/>
<c:set target="${preauthStatusDisplay}" property="S" value="Sent"/>
<c:set target="${preauthStatusDisplay}" property="D" value="Denied"/>
<c:set target="${preauthStatusDisplay}" property="R" value="ForResub"/>
<c:set target="${preauthStatusDisplay}" property="C" value="Closed"/>
<c:set target="${preauthStatusDisplay}" property="X" value="Cancelled"/>

<jsp:useBean id="approvalStatus" class="java.util.HashMap"/>
<c:set target="${approvalStatus}" property="F" value="Fully Approved"/>
<c:set target="${approvalStatus}" property="P" value="Partially Approved"/>
<c:set target="${approvalStatus}" property="R" value="Fully Rejected"/>
<c:set target="${approvalStatus}" property="X" value="Cancelled"/>

<html>
<head>
	<title>Prior Auth Approvals - Insta HMS</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="script" file="widgets.js"/>
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="script" file="billing/claimsCommon.js"/>
	<insta:link type="script" file="EAuthorization/eAuthApprovals.js"/>
	<insta:link type="script" file="dashboardsearch.js"/>
	<script>
		var companyList    = ${insCompList};
		var categoryList   = ${insCategoryList};
		var tpaList        = ${tpaList};
	</script>
</head>

<c:set var="preauthList" value="${pagedList.dtoList}"/>
<c:set var="hasResults" value="${not empty preauthList}"/>

<body onload="init(); showFilterActive(document.preauthApprovalSearchForm);">
<div id="storecheck" style="display: block;" >
<h1>Prior Auth Approvals List</h1>

<insta:feedback-panel/>
<c:if test="${not empty errorMsg}">
	<div style="margin-bottom:20px; padding:10px 0 10px 10px; background-color:#FFC;" class="brB brT brL brR" id="msgDiv">
		<div class="fltR" style="margin:-8px 0px 0 26px; width:17px;"> <img src="${cpath}/images/fileclose.png" onclick="document.getElementById('msgDiv').style.display='none';"/></div>
		<c:if test="${not empty errorMsg}">
			<div class="fltL" style="width: 25px; margin:-1px 0 0 3px;"> <img src="${cpath}/images/error.png" /></div>
			<div class="fltL"  style="margin:0px 0 0 5px ; width:865px;"> <esapi:encodeForHTML> ${errorMsg} </esapi:encodeForHTML></div>
		</c:if>
		<div class="clrboth"></div>
	</div>
</c:if>
<c:if test="${empty errorMsg}">
<form name="preauthApprovalSearchForm" method="GET">
	<input type="hidden" name="_method"  id ="_method" value="getApprovals">
	<input type="hidden" name="_searchMethod" value="getApprovals"/>

	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

	<insta:search form="preauthApprovalSearchForm" optionsId="optionalFilter" closed="${hasResults}" >
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
				<div class="sboFieldLabel">Prior Auth Presc. ID: </div>
				<div class="sboFieldInput">
					<input type="text" name="preauth_presc_id" value="${ifn:cleanHtmlAttribute(param.preauth_presc_id)}">
					<input type="hidden" name="preauth_presc_id@type" value="integer"/>
				</div>
			</div>
			<div class="sboField">
				<div class="sboFieldLabel">Prior Auth Request ID: </div>
				<div class="sboFieldInput">
					<input type="text" name="preauth_request_id" value="${ifn:cleanHtmlAttribute(param.preauth_request_id)}">
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
							table="insurance_category_master" valuecol="category_id" dummyvalue="(All)" onchange="onChangeOfInsuranceCategory()"/>
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
							<select id="plan_id" name="plan_id" class="dropdown">
								<option selected="selected" value="">(All)</option>
							</select>
							<input type="hidden" name="plan_id@type" value="integer"/>
						</div>
						<div class="sfLabel">Resubmit:</div>
						<div class="sfField">
							<insta:checkgroup name="is_resubmit" selValues="${paramValues.is_resubmit}"
							opvalues="Y,N" optexts="Yes,No"/>
						</div>
					</td>
					<td>
						<div class="sfLabel">Prior Auth Status:</div>
						<div class="sfField">
							<insta:checkgroup name="preauth_status" selValues="${paramValues.preauth_status}"
							opvalues="O,S,D,R,C,X" optexts="Open,Sent,Denied,ForResub,Closed,Cancelled"/>
						</div>
					</td>
					<td>
						<div class="sfLabel">Approval Status</div>
						<div class="sfField">
							<insta:checkgroup name="approval_status" selValues="${paramValues.approval_status}"
							opvalues="F,P,R,X" optexts="Fully Approved,Partially Approved,Fully Rejected,Cancelled"/>
						</div>
					</td>
				</tr>
			</table>
	   </div>
	</insta:search>
 </form>
	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
	<form method="POST" name="preauthNewApprovalsForm" action="./EAuthApproval.do">
		<input type="hidden" name="_method"  id ="_method" value="getEAuthorizationApproval">
		<button type="button" name="checkNewEAuths" value="searchAuth"
				accessKey="R" onclick="return approvalSearch();">
		<img class="flag" src="${cpath}/icons/Refresh.png"/>
		<b><u>R</u></b>efresh
		</button>
		<div class="resultList">
			<table class="resultList dialog_displayColumns" cellspacing="0" cellpadding="0" id="resultTable"">
				<tr onmouseover="hideToolBar();">
					<th>#</th>
					<th title="Prior Authorization File Id Received.">File</th>
					<th title="File Not Downloaded / Downloaded With Received Date and Time.">Received Date Time</th>
					<th>Approval status</th>
					<insta:sortablecolumn name="mr_no" title="MR No"/>
					<insta:sortablecolumn name="patient_id" title="Visit ID"/>
					<th>Patient Name</th>
					<insta:sortablecolumn name="preauth_presc_id" title="Prior Auth Presc. ID"/>
					<th>Prior Auth Status</th>
					<insta:sortablecolumn name="preauth_request_id" title="Request ID"/>
					<th>Request Date Time</th>
					<insta:sortablecolumn name="primary_insurance_co_name" title="Pri. Insurance Company"/>
					<insta:sortablecolumn name="primary_tpa_name" title="Pri. TPA/Sponsor"/>
					<insta:sortablecolumn name="primary_category_name" title="Pri. Network/Plan Type"/>
					<insta:sortablecolumn name="secondary_insurance_co_name" title="Sec. Insurance Company"/>
					<insta:sortablecolumn name="secondary_tpa_name" title="Sec. TPA/Sponsor"/>
					<insta:sortablecolumn name="secondary_category_name" title="Sec. Network/Plan Type"/>
					<th>Authorization ID</th>
					<th>Request Type</th>
					<th>Resubmit Type</th>
				</tr>
				<c:forEach var="presc" items="${preauthList}" varStatus="st">
				<c:set var="i" value="${st.index + 1}"/>
				<c:set var="flagColor">
					<c:choose>
						<c:when test="${presc.approval_status eq 'F'}">empty</c:when>
						<c:when test="${presc.approval_status eq 'P'}">blue</c:when>
						<c:when test="${presc.approval_status eq 'R'}">red</c:when>
						<c:when test="${presc.approval_status eq 'X'}">grey</c:when>
					</c:choose>
				</c:set>
				<c:set var="enableDownload" value="${not empty presc.file_id && presc.approval_status != 'X'}"/>

				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'resultTable',
							{visit_id: '${presc.patient_id}', preauth_presc_id: ${presc.preauth_presc_id}, priority: '${presc.priority}',
    						insurance_co_id: '${presc.priority == 1 ? presc.primary_insurance_co_id : presc.secondary_insurance_co_id}',
							preauth_request_id: '${presc.preauth_request_id}', row_id: ${st.index} },
							[true, ${enableDownload}, true]);"
						onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}"	>
					<td>
						${(pagedList.pageNumber-1) * pagedList.pageSize + st.index + 1}
						<input type="hidden" name="pid" id="pid${st.index}" value="${presc.preauth_presc_id}"/>
						<input type="hidden" name="prid" id="prid${st.index}" value="${presc.preauth_request_id}"/>
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
							<img class="flag" src="${cpath}/images/${flagColor}_flag.gif"/>
						</c:when>
						<c:when test="${presc.preauth_status == 'S'}">
							Pending...
						</c:when>
						</c:choose>
					</td>
					<td>${presc.mr_no }</td>
					<td>${presc.patient_id }</td>
					<td><insta:truncLabel value="${presc.patname}" length="15"/></td>
					<td>${presc.preauth_presc_id }</td>
					<td>${preauthStatusDisplay[presc.preauth_status]}</td>
					<td>${presc.preauth_request_id }</td>
					<td><fmt:formatDate pattern="dd-MM-yyyy HH:mm" value="${presc.request_date}"/></td>
					<td><insta:truncLabel value="${presc.priority == 1 ? presc.primary_insurance_co_name : ''}" length="20"/></td>
				  	<td><insta:truncLabel value="${presc.priority == 1 ? presc.primary_tpa_name : ''}" length="20"/></td>
					<td><insta:truncLabel value="${presc.priority == 1 ? presc.primary_category_name : ''}" length="20"/></td>
					<td><insta:truncLabel value="${presc.priority == 2 ? presc.secondary_insurance_co_name : ''}" length="20"/></td>
				   	<td><insta:truncLabel value="${presc.priority == 2 ? presc.secondary_tpa_name : ''}" length="20"/></td>
					<td><insta:truncLabel value="${presc.priority == 2 ? presc.secondary_category_name : ''}" length="20"/></td>
					<td>${presc.preauth_id_payer}</td>
					<td>${presc.preauth_request_type}</td>
					<td>${presc.resubmit_type}</td>
				</tr>
				</c:forEach>
			</table>
			<c:if test="${param._method == 'getApprovals'}">
				<insta:noresults hasResults="${hasResults}"/>
			</c:if>
	    </div>
</form>

<div class="legend" style="display:${hasResults ? 'block' : 'none'}">
	<div class="flag"><img src="${cpath}/images/empty_flag.gif"></img></div>
	<div class="flagText">Fully Approved</div>
	<div class="flag"><img src="${cpath}/images/blue_flag.gif"></img></div>
	<div class="flagText">Partially Approved</div>
	<div class="flag"><img src="${cpath}/images/red_flag.gif"></img></div>
	<div class="flagText">Fully Rejected</div>
	<div class="flag"><img src="${cpath}/images/grey_flag.gif"></img></div>
	<div class="flagText">Cancelled</div>
</div>
</c:if>
</body>
</html>

<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<jsp:useBean id="closureStatus" class="java.util.HashMap"/>
<c:set target="${closureStatus}" property="F" value="Fully Received"/>
<c:set target="${closureStatus}" property="D" value="Denial Accepted"/>
<c:set target="${closureStatus}" property="W" value="Written Off"/>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<script>
	var	eClaimModule = '${preferences.modulesActivatedMap['mod_eclaim']}';
	var updateMRDRights = '${urlRightsMap.update_mrd}';
	var visitEMRRights = '${urlRightsMap.visit_emr_screen}';
</script>
<title>Claim Reconciliation - Insta HMS</title>
<insta:link type="js" file="widgets.js"/>
<insta:link type="css" file="widgets.css"/>
<insta:link type="script" file="dashboardsearch.js"/>
<insta:link type="script" file="billing/claimsCommon.js"/>
<insta:link type="script" file="billing/claimreconciliation.js"/>

<script>
	var companyList    = [];
	var categoryList   = ${insCategoryList};
	var tpaList        = ${tpaList};
	var planList       = [];
	var extraDetails   = [];
</script>
</head>

<c:set var="claimReconciliationList" value="${pagedList.dtoList}"/>
<c:set var="hasResults" value="${not empty claimReconciliationList}"/>
<c:set var="corpInsurance" value='<%=GenericPreferencesDAO.getAllPrefs().get("corporate_insurance")%>'/>

<body onload="init();">

<h1>Claim Reconciliation</h1>
<insta:feedback-panel/>

<form name="claimReconciliationForm" method="GET">
	<input type="hidden" name="_method" value="list">
	<input type="hidden" name="_searchMethod" value="list"/>

	<insta:search form="claimReconciliationForm" optionsId="optionalFilter" closed="${hasResults}" clearFunction="clearForm">
	<div class="searchBasicOpts">
		<div class="sboField">
			<div class="sboFieldLabel"> Submission Batch ID: </div>
			<div class="sboFieldInput">
				<input type="text" name="submission_batch_id" value="${ifn:cleanHtmlAttribute(param.submission_batch_id)}">
			</div>
		</div>
		<div class="sboField">
			<div class="sboFieldLabel"> Claim ID: </div>
			<div class="sboFieldInput">
				<input type="text" name="claim_id" value="${ifn:cleanHtmlAttribute(param.claim_id)}">
			</div>
		</div>
		<div class="sboField">
			<div class="sboFieldLabel">MR No/Patient Name:</div>
			<div class="sboFieldInput">
				<div id="mrnoAutoComplete">
					<input type="text" name="mr_no" id="mrno" value="${ifn:cleanHtmlAttribute(param.mr_no)}" />
					<input type="hidden" name="mr_no@op" value="ilike" />
					<div id="mrnoContainer"></div>
				</div>
			</div>
		</div>
	</div>

	<div id="optionalFilter" style="clear: both; display : ${hasResults ? 'none' : 'block'}" >
	<table class="searchFormTable">
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
				<div class="sfLabel">Ins. Company Name:</div>
				<div class="sfField">
					<insta:selectdb displaycol="insurance_co_name" name="insurance_co_id" id="insurance_co_id" value="${param.insurance_co_id}"
					table="insurance_company_master" valuecol="insurance_co_id" dummyvalue="(All)" onchange="onChangeInsuranceCompany()"/>
				</div>
				<%--
				<c:if test="${corpInsurance ne 'Y'}">
				 --%>
				<div class="sfLabel">Network/Plan Type:</div>
				<div class="sfField">
					<c:set var="inscatSelected" value="${fn:join(paramValues.category_id, ' ')}"/>
							<select name="category_id" id="category_id"  multiple="multiple" class="listbox" optionTitle="true"
										style="border-top:1px #999 solid; border-left:1px #999 solid; border-bottom:1px #ccc solid; border-right:1px #ccc solid;"
										onchange="onChangeInsuranceCategory()">
								<option value="">(All)</option>
								<c:forEach items="${inscatName}" var="inscat" >
									<c:set var="selected" value="${fn:contains(inscatSelected,inscat.map.category_id)?'selected':''}"/>
									<option value="${inscat.map.category_id}" ${selected}>${inscat.map.category_name}</option>
								</c:forEach>
									<input type="hidden" name="category_id@type" value="text"/>
							</select>
				</div>
				<%--
				</c:if>
				 --%>
				<div class="sfLabel">Denial Code Type:</div>
				<div class="sfField">
					<insta:selectdb name="denial_code_type" values="${paramValues.denial_code_type}" table="insurance_denial_code_types"
					valuecol="type" displaycol="type" dummyvalue="(All)"
					multiple="multiple" class="listbox" optionTitle="true"
					style="border-top:1px #999 solid; border-left:1px #999 solid; border-bottom:1px #ccc solid; border-right:1px #ccc solid;"/>
				</div>
			</td>
			<td>
				<div class="sfLabel">Sponsor Name:</div>
				<%--
				<c:choose>
				<c:when test="${corpInsurance eq 'Y'}">
				<div class="sfLabel">Sponsor Name:</div>
				</c:when>
				<c:otherwise>
				<div class="sfLabel">TPA Name:</div>
				</c:otherwise>
				</c:choose>
				--%>
				<div class="sfField">
					<insta:selectdb displaycol="tpa_name" name="tpa_id" id="tpa_id" value="${param.tpa_id}"
					table="tpa_master" valuecol="tpa_id" dummyvalue="(All)" onchange="onChangeTPA()"/>
				</div>
				<div class="sfLabel">Plan Name:</div>
				<div class="sfField">
					<input type="text" name="plan_name" id="plan_name" style="width: 138px;">
					<div id="plan_name_dropdown" class="scrolForContainer"></div>
					<input type="hidden" name="plan_id" id="plan_id">
					<input type="hidden" name="plan_id@type" value="integer"/>
				</div>
				<div class="sfLabel">Resubmission</div>
				<div class="sfField">
					<insta:checkgroup name="is_resubmission" selValues="${paramValues.is_resubmission}"
						opvalues="Y,N" optexts="Yes,No"/>
				</div>
			</td>
			<td>
				<div class="sfLabel">Status:</div>
				<div class="sfField">
					<insta:checkgroup name="claim_status" selValues="${paramValues.claim_status}"
						opvalues="Open,Batched,Sent,Denied,ForResub.,Closed" optexts="Open,Batched,Sent,Denied,For Resubmission,Closed"/>
				</div>
				<div class="sfLabel">Claim Due &lt;= :</div>
				<div class="sfField">
					<input type="text" name="claim_due" name="claim_due0" value="${paramValues.claim_due[0]}">
					<input type="hidden" name="claim_due@op" value="le,ge"/>
					<input type="hidden" name="claim_due@type" value="numeric"/>
				</div>
				<div class="sfLabel">Claim Due &gt;= :</div>
				<div class="sfField">
					<input type="text" name="claim_due" name="claim_due1" value="${paramValues.claim_due[1]}">
					<input type="hidden" name="claim_due@type" value="numeric"/>
				</div>
			</td>
		</tr>
	</table>
	</div>
	</insta:search>
</form>
<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}" showTooltipButton="true"/>
<form method="POST" name="claimResubmissionForm" action="./claimReconciliation.do">
	<input type="hidden" name="_method" value=""/>
	<div class="resultList">
		<table class="detailList dialog_displayColumns" cellspacing="0" cellpadding="0" align="center" width="100%" id="resultTable"
		onmouseover="hideToolBar('');">
			<tr>
				<th>#</th>
				<th><input type="checkbox" name="_allclaims" onclick="checkAllClaims();"/></th>
				<insta:sortablecolumn name="claim_id" title="Claim ID"/>
				<th>Cons. Visit</th>
				<insta:sortablecolumn name="status" title="Status"/>
				<insta:sortablecolumn name="submission_batch_id" title="Batch ID"/>
				<insta:sortablecolumn name="tpa_name" title="Sponsor Name"/>
				<%--
				<c:choose>
					<c:when test="${corpInsurance eq 'Y'}">
						<insta:sortablecolumn name="tpa_name" title="Sponsor Name"/>
					</c:when>
					<c:otherwise>
						<insta:sortablecolumn name="tpa_name" title="TPA Name"/>
					</c:otherwise>
				</c:choose>
				 --%>
				<insta:sortablecolumn name="insurance_co_name" title="Ins. Company Name"/>
				<%--
				<c:if test="${corpInsurance ne 'Y'}">
				 --%>
				<insta:sortablecolumn name="category_name" title="Network/Plan Type"/>
				<%--
				</c:if>
				 --%>
				<insta:sortablecolumn name="plan_name" title="Plan Name"/>
				<th>Claim Due</th>
				<th>Patient Due</th>
				<insta:sortablecolumn name="resubmission_count" title="Resub. Count"/>
				<insta:sortablecolumn name="submission_date" title="Last Sub. Date"/>
			</tr>
			<c:forEach var="record" items="${claimReconciliationList}" varStatus="st">
				<c:set var="i" value="${st.index}"/>
					<c:set var="flagColor">
					<c:choose>
						<c:when test="${record.claim_status eq 'Open'}">yellow</c:when>
						<c:when test="${record.claim_status eq 'Batched'}">blue</c:when>
						<c:when test="${record.claim_status eq 'Sent'}">violet</c:when>
						<c:when test="${record.claim_status eq 'Denied'}">red</c:when>
						<c:when test="${record.claim_status eq 'ForResub.'}">green</c:when>
						<c:when test="${record.claim_status eq 'Closed'}">grey</c:when>
					</c:choose>
					</c:set>
					<c:set var="billsEnable" value ="true"/>
					<c:set var="codificationEnable" value="true"/>
					<c:set var="visitEmrEnable" value="true"/>
					<c:set var="attachmentEnable" value ="${record.claim_status eq 'Denied' || record.claim_status eq 'ForResub.'}"/>
					<c:set var="addToResubmitEnable" value ="${record.claim_status eq 'ForResub.' && record.resubmission_count >= 0}"/>
					<c:set var="addToSubmitEnable" value ="${empty record.submission_batch_id && record.claim_status eq 'Open' && record.resubmission_count == 0}"/>

					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
					onclick="showToolbar(${st.index}, event, 'resultTable',
					{claim_id:'${record.claim_id}', patient_id: '${record.claim_for_visit}',visit_id: '${record.claim_for_visit}',
					 claim_id:'${record.claim_id}'},
					[${billsEnable},${codificationEnable},${visitEmrEnable},${attachmentEnable},${addToResubmitEnable},${addToSubmitEnable}])"
					onmouseover="hideToolBar(${st.index})" 	id="toolbarRow${st.index}">
						<td>${(pagedList.pageNumber-1) * pagedList.pageSize + st.index + 1}</td>
						<td>
							<c:choose>
								<c:when test="${record.claim_status eq 'Denied'}">
									<input type="checkbox" name="claim_id" value="${record.claim_id}"/>
								</c:when>
								<c:otherwise>
									<input type="checkbox" name="claim_id" value="" disabled/>
								</c:otherwise>
							</c:choose>
						</td>
						<td>${record.claim_id}</td>
						<input type="hidden" name="claim_ids" value=${record.claim_id}>
						<td>${record.claim_for_visit}</td>
						<td>${record.claim_status}
							<img src="${cpath}/images/${flagColor}_flag.gif"/>
							
						</td>
						<td>${record.submission_batch_id}</td>
						<td><insta:truncLabel value="${record.tpa_name}" length="15"/></td>
						<td><insta:truncLabel value="${record.insurance_co_name}" length="15"/></td>
						<td><insta:truncLabel value="${empty record.category_name ? 'All' :record.category_name}" length="15"/></td>
						<td><insta:truncLabel value="${empty record.plan_name ? 'All' :record.plan_name}" length="15"/></td>
						<td>${record.claim_due}</td>
						<td>${record.patient_due}</td>
						<td>${record.resubmission_count}</td>
						<input type="hidden" name="resubmission_count" value=${record.resubmission_count}>
						<input type="hidden" name="max_resubmission_count" value=${record.max_resubmission_count}>
						<td>${record.submission_date}</td>
					</tr>
					<script>
					extraDetails['toolbarRow${st.index}'] = {
						'MR No': '${record.mr_no}',
						'Patient Id': '${record.claim_for_visit}',
						'Name':  <insta:jsString value="${record.patient_full_name}"/>
					};
				</script>
			</c:forEach>
		</table>

		<insta:noresults hasResults="${hasResults}"/>
	</div>

	<br/>
	<fieldset class="fieldSetBorder">
	<table class="formtable" align="center">
		<tr>
			<td class="formlabel">Resubmission type:</td>
			<td class="forminfo">
				<c:choose>
                    <c:when test="${healthAuthority == 'DHA'}">
                		<insta:selectoptions name="_resubmission_type" id="_resubmission_type" value="" dummyvalue="-- Select --"
                		    optexts="correction,internal complaint,legacy,reconciliation" opvalues="correction,internal complaint,legacy,reconciliation"/>
                	</c:when>
                	<c:otherwise>
                	    <insta:selectoptions name="_resubmission_type" id="_resubmission_type" value="" dummyvalue="-- Select --"
                		    optexts="correction,internal complaint,legacy" opvalues="correction,internal complaint,legacy"/>
                	</c:otherwise>
                </c:choose>
			</td>
			<td class="formlabel">Comments:</td>
			<td class="forminfo">
				<textarea name="_comments" id="_comments" title="Comments for resubmission" rows="2" cols="60"></textarea>
			</td>
		</tr>
	</table>
	</fieldset>

	<fieldset class="fieldSetBorder">
	<table class="formtable" align="center">
		<tr>
			<td class="formlabel">Closure type:</td>
			<td class="forminfo">
				<c:choose>
					<c:when test="${!((actionRightsMap.allow_denial_acceptance == 'A')||(roleId==1)||(roleId==2))}">
						<insta:selectoptions name="_closure_type" id="_closure_type"
							dummyvalue="-- Select --" optexts="Fully Received,Denial Accepted,Write Off/Credit Note"
							opvalues="F,D,W" value="" onchange="disableOrEnableReconClaimRejReason();" disabled="true"/>
					</c:when>
					<c:otherwise>
						<insta:selectoptions name="_closure_type" id="_closure_type"
							dummyvalue="-- Select --" optexts="Fully Received,Denial Accepted,Write Off/Credit Note"
							opvalues="F,D,W" value="" onchange="disableOrEnableReconClaimRejReason();"/>
					</c:otherwise>
				</c:choose>
			</td>
			<td class="formlabel">Closure Remarks:</td>
			<td class="forminfo">
				<textarea name="_closure_remarks" id="_closure_remarks" title="Remarks for claim closure" rows="2" cols="60"></textarea>
			</td>
		</tr>
		<tr>
			<td class="formlabel">Rejection Reason:</td>
			<td>
			<insta:selectdb name="_claim_rejection_reasons_drpdn" id="_claim_rejection_reasons_drpdn"
											onchange="" value=""
											table="rejection_reason_categories" style="width:137px;"
											dummyvalue="--Select--" valuecol="rejection_reason_category_id"
											displaycol="rejection_reason_category_name"
											orderby="rejection_reason_category_name" disabled="true"/>
			</td>
		</tr>
	</table>
	</fieldset>

	<table class="screenActions">
		<tr>
			<td>
				<button type="button" accesskey="M" name="resubmitClaims" class="button" onclick="return validateResubmission();">
				<label><b><u>M</u></b>ark for Resubmission</label></button>&nbsp;
				<label>|</label>
				<button type="button" accesskey="C" name="closeClaims" class="button" onclick="return validateClaimClosure();">
				<label><b><u>C</u></b>laim Closure</label></button>&nbsp;
				<label>|</label>
				<a href="./claimSubmissionsList.do?_method=list&is_resubmission=Y&sortOrder=created_date&sortReverse=true">Resubmissions</a>
				<label>|</label>
				<a href="javascript:void(0);" onclick="clearForm(document.claimResubmissionForm);">Clear</a>
			</td>
		 </tr>
	</table>

	<div class="legend" style="display:${hasResults ? 'block' : 'none'}">
		<div class="flag"><img src="${cpath}/images/yellow_flag.gif"></img></div>
		<div class="flagText">Open</div>
		<div class="flag"><img src="${cpath}/images/blue_flag.gif"></img></div>
		<div class="flagText">Batched</div>
		<div class="flag"><img src="${cpath}/images/violet_flag.gif"/></div>
		<div class="flagText">Sent</div>
		<div class="flag"><img src="${cpath}/images/red_flag.gif"/></div>
		<div class="flagText">Denied</div>
		<div class="flag"><img src="${cpath}/images/green_flag.gif"></img></div>
		<div class="flagText">ForResub.</div>
		<div class="flag"><img src="${cpath}/images/grey_flag.gif"></img></div>
		<div class="flagText">Closed</div>
	</div>
</body>
</html>

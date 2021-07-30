<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />

<html>
<head>
	<title>Claim Submission</title>
	<meta http-equiv="Content-Type"  content="text/html;charset=iso-8859-1">
	<insta:link type="js" file="ajax.js" />
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="script" file="hmsvalidation.js" />
	<insta:link type="js" file="dashboardsearch.js"/>
	<insta:link type="script" file="reports/std_report_builder.js" />
	<insta:link type="script" file="billing/claimsCommon.js"/>
	<insta:link type="script" file="billing/claimsubmission.js"/>
	
<script>
	var companyList    = ${insCompList};
	var tpaList        = ${tpaList};
</script>
</head>

<body class="yui-skin-sam">
<form name="submissionform" method="POST" action="raiseClaimSubmission.do">
	<input type="hidden" name="_method" value="createSubmission">
	<div class="pageHeader">Claim Batch Submission</div>
	<insta:feedback-panel/>
	<div class="helpPanel">
		<table>
			<tr>
				<td valign="top" style="width: 30px"><img src="${cpath}/images/information.png"/></td>
				<td style="padding-bottom: 5px">
					    Claims Submission :<br/>
						All bills (Open, Finalized), which are part of claim are eligible for claim submission.<br/>
						If submission has any open bills please finalize them before generating e-claim.<br/>
						If not finalized, e-claim processing cannot be done with open bills.<br/>
						In this case if the open bills have to be excluded from submission for generating e-claim then,<br/>
						delete the submission and create a new submission.
				</td>
			</tr>
			<tr>
				<td valign="top"><img src="${cpath}/images/information.png"/></td>
				<td>    Claims Resubmission :<br/>
						If claims have to be resubmitted, please check Claim Resubmission check box.<br/>
						Only those claims which are marked for resubmission are choosen for resubmission.<br/>
				</td>
			</tr>
		</table>
	</div>

	<fieldset class="fieldSetBorder">
		<legend class="fieldSetLabel">Submission Criteria</legend>


	<table class="searchFormTable" style="border-top:none">
		<tr>
			<td style="border-right:none;">
				<div class="sfLabel">Ins. Company Name:</div>
				<div class="sfField">
					<insta:selectdb displaycol="insurance_co_name" name="insurance_co_id" id="insurance_co_id" value="${param.insurance_co_id}"
					table="insurance_company_master" valuecol="insurance_co_id" dummyvalue="(All)" onchange="onInsuranceCompanyChange()"
					orderby="insurance_co_name"/>
				</div>
			</td>
			<td style="border-right:none;">
				<div class="sfLabel">TPA Name:</div>
				<div class="sfField">
					<insta:selectdb displaycol="tpa_name" name="tpa_id" id="tpa_id" value="${param.tpa_id}"
					table="tpa_master" valuecol="tpa_id" dummyvalue="(All)" onchange="onTPAChange()"
					orderby="tpa_name"/>
				</div>
			</td>
			<td style="border-right:none;" colspan="2">
				<div class="sfLabel">Network/Plan Type:</div>
				<div class="sfField">
					<select id="category_id" name="category_id"  multiple="multiple" class="listbox" optionTitle="true"
						style="border-top:1px #999 solid; border-left:1px #999 solid; border-bottom:1px #ccc solid; 
						border-right:1px #ccc solid; width:300px"
						onchange="onInsuranceCategoryChange()">
						<option selected="selected" value="">(All)</option>
					</select>
				</div>
			<td style="border-right:none;">
				<div class="sfLabel">Plan Name:</div>
				<div class="sfField">
					<select id="plan_id" name="plan_id" class="dropdown">
						<option selected="selected" value="">(All)</option>
					</select>
					<input type="hidden" name="plan_id@type" value="integer"/>
				</div>
			</td>
		</tr>
		<tr>
			<td style="border-right:none;">
				<div class="sfLabel">Center/Account Group:</div>
				<div class="sfLabel" style="border-bottom: none;">

				<select name="center_or_account_group" id="center_or_account_group" class="dropdown">
					<c:forEach items="${accountGrpAndCenterList}" var="acc">
						<option value="${acc.map.id}" ${param.center_or_account_group == acc.map.id ? 'selected':''}>${acc.map.ac_name}-(${acc.map.accounting_company_name})</option>
					</c:forEach>
				</select>

					<input type="hidden" name="center_or_account_group@type" value="text"/>
				</div>
			</td>
			<td style="border-right:none;">
				<div class="sfLabel">Patient Type:</div>
				<div class="sfField">
					<insta:checkgroup name="visit_type" selValues="${paramValues.visit_type}"
						opvalues="i,o" optexts="IP,OP"  />
				</div>
			</td>
			<td style="border-right:none;">
				<div class="sfLabel">Registration Date:</div>
				<div class="sfField" >
					<div class="sfFieldSub">From:</div>
					<insta:datewidget name="_reg_date" id="reg_date0" value="${paramValues.reg_date[0]}" onchange = "defaultRegTimeFrom()"/>
					<!-- <input type="hidden" name="reg_date@op" value="ge,le"/>
					<input type="hidden" name="reg_date@type" value="date"/>
					 -->
					<input type="text" name="_reg_time" id="reg_time0" style="width: 39px" value="${paramValues.reg_time[0]}"/>
					<!-- <input type="hidden" name="reg_time@op" value="ge,le"/>
					<input type="hidden" name="reg_time@type" value="time"/> -->
					<input type="hidden" name="reg_date_time@op" value="ge,le"/>
					<input type="hidden" name="reg_date_time@type" value="timestamp"/>
				</div>
				
				<div class="sfField">
					<div class="sfFieldSub">To:</div>
					<insta:datewidget name="_reg_date" id="reg_date1" value="${paramValues.reg_date[1]}" onchange = "defaultRegTimeTo()"/>
					<input type="text" name="_reg_time" id="reg_time1" style="width: 39px" value="${paramValues.reg_time[1]}" />
				</div>
			</td>
			<td style="border-right:none;">
				<div class="sfLabel">Last Bill Finalized Date:</div>
				<div class="sfField">
					<div class="sfFieldSub">From:</div> 
					<insta:datewidget name="_last_bill_finalized_date" id="last_bill_finalized_date0" value="${paramValues.last_bill_finalized_date[0]}" onchange = "defaultLastFinalizedTimeFrom()"/>
					<input type="text" name="_last_bill_finalized_time" id="last_bill_finalized_time0" style="width: 39px" value="${paramValues.last_bill_finalized_time[0]}" />
					<input type="hidden" name="last_bill_finalized_date@op" value="ge,le"/>
					<input type="hidden" name="last_bill_finalized_date@type" value="timestamp"/>
				</div>
				<div class="sfField">
					<div class="sfFieldSub">To:</div>
					<insta:datewidget name="_last_bill_finalized_date" id="last_bill_finalized_date1" value="${paramValues.last_bill_finalized_date[1]}" onchange = "defaultLastFinalizedTimeTo()" />
					<input type="text" name="_last_bill_finalized_time" id="last_bill_finalized_time1" style="width: 39px" value="${paramValues.last_bill_finalized_time[1]}" />
				</div>
			</td>
			<td style="border-right:none;">
				<div class="sfLabel">First Bill Open Date:</div>
				<div class="sfField">
					<div class="sfFieldSub">From:</div>
					<insta:datewidget name="_first_bill_open_date" id="first_bill_open_date0" value="${paramValues.first_bill_open_date[0]}" onchange = "defaultFirstBillOpenTimeFrom()"/>
					<input type="text" name="_first_bill_open_time" id="first_bill_open_time0" style="width: 39px" value="${paramValues.first_bill_open_time[0]}" />
					<input type="hidden" name="first_bill_open_date@op" value="ge,le"/>
					<input type="hidden" name="first_bill_open_date@type" value="timestamp"/>
				</div>
				<div class="sfField">
					<div class="sfFieldSub">To:</div>
					<insta:datewidget name="_first_bill_open_date" id="first_bill_open_date1" value="${paramValues.first_bill_open_date[1]}" onchange = "defaultFirstBillOpenTimeTo()"/>
					<input type="text" name="_first_bill_open_time" id="first_bill_open_time1" style="width: 39px" value="${paramValues.first_bill_open_time[1]}" />
				</div>
			</td>
		</tr>
		<tr>
			<td style="border-right:none;">
				<div class="sfLabel">Codification Status:</div>
				<div class="sfField">
					<insta:checkgroup name="codification_status" selValues="${paramValues.codification_status}"
						opvalues="P,C,R,V" optexts="In-Progress,Completed,Completed Needs Verification,Verified and Closed"/>
				</div>
			</td>
			<td style="border-right:none;">
				<div class="sfLabel">Bill Status:</div>
				<div class="sfField">
					<insta:checkgroup name="bill_status" selValues="${paramValues.bill_status}"
						opvalues="A,F" optexts="Open,Finalized"/>
				</div>
			</td>
			<td style="border-right:none;">
				<div class="sfLabel">Discharge Date:</div>
				<div class="sfField">
					<div class="sfFieldSub">From:</div>
					<insta:datewidget name="_discharge_date" id="discharge_date0" value="${paramValues.discharge_date[0]}" onchange = "defaultDischargeTimeFrom()"/>
					<!-- <input type="hidden" name="discharge_date@op" value="ge,le"/>
					<input type="hidden" name="discharge_date@type" value="date"/> -->
					
					<input type="text" name="_discharge_time" id="discharge_time0" style="width: 39px" value="${paramValues.discharge_time[0]}"/>
<!-- 					<input type="hidden" name="discharge_time@op" value="ge,le"/> -->
<!-- 					<input type="hidden" name="discharge_time@type" value="time"/> -->
					<input type="hidden" name="discharge_date_time@op" value="ge,le"/>
					<input type="hidden" name="discharge_date_time@type" value="timestamp"/>
				</div>
				<div class="sfField">
					<div class="sfFieldSub">To:</div>
					<insta:datewidget name="_discharge_date" id="discharge_date1" value="${paramValues.discharge_date[1]}" onchange = "defaultDischargeTimeTo()"/>
					<input type="text" name="_discharge_time" id="discharge_time1" style="width: 39px" value="${paramValues.discharge_time[1]}"/>
				</div>
			</td>
			<td style="border-right:none;">
				<div class="sfLabel" style="border-bottom: none;">
					<input type="checkbox" name="is_resubmission" id="is_resubmission"
					 "${not empty param.is_resubmission &&  param.is_resubmission == 'Y'? 'checked':''}"/>
					 <label for="is_resubmission"> Claim Resubmission </label>
				</div>
			</td>
			<td style="border-right:none;">
				<div class="sfLabel" style="border-bottom: none;">
					<input type="checkbox" name="is_external_pbm" id="is_external_pbm"
					 <label for="is_resubmission"> Ignore external PBM </label>
				</div>
			</td>
		</tr>
	</table>

	<table class="screenActions">
		<tr>
			<td>
				<button type="button" accesskey="C" name="submitClaims" class="button" onclick="return validateSubmit();">
				<label><b><u>C</u></b>reate Batch Submission</label></button>&nbsp;
				<label>|</label>
				<a href="./claimSubmissionsList.do?_method=list&sortOrder=created_date&sortReverse=true&status=O">Claim Submissions List</a>
				<label>|</label>
				<a href="javascript:void(0)" onclick="clearForm(submissionform);initSearch();">Reset</a>
			</td>
		 </tr>
	</table>

</form>
</body>
</html>

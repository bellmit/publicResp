<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@page import="com.insta.hms.integration.URLRoute"%>
<c:set var="pagePath" value="<%=URLRoute.SELF_PAY_CLAIM_SUBMISSION %>"/>
<c:set var="cpath" value="${pageContext.request.contextPath}" />

<html>
<head>
	<title><insta:ltext key="ui.label.self.pay.claim.batch.submission"/></title>
	<meta http-equiv="Content-Type"  content="text/html;charset=iso-8859-1">
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="script" file="hmsvalidation.js" />
	<insta:link type="js" file="dashboardsearch.js"/>
	<insta:link type="script" file="billing/claimsCommon.js"/>
	
	<insta:link type="script" file="billing/claimsubmission.js"/>
</head>

<body onload="enableFields();">
<script>

	function validateSelfPaySubmit() {
		var regFromDate = document.getElementById('reg_date0');
		var regToDate = document.getElementById('reg_date1');

		if (trim(document.getElementById('reg_date0').value) == ''
				|| trim(document.getElementById('reg_date1').value) == '') {
			alert("Enter both date fields");
			return false;
		}
		if (!doValidateDateField(document.getElementById('reg_date0'))) {
			return false;
		}
		if (!doValidateDateField(document.getElementById('reg_date1'))) {
			return false;
		}
		document.submissionform.submit();
	}
	function onChangeSubmissionType() {
		if(document.getElementById("PersonRegister").checked){
			document.getElementById("ignoreopenbills").style.display = 'none';
		}
		else{
			document.getElementById("ignoreopenbills").style.display = '';
			}
		
	}
	function enableFields() {
		var val = document.getElementById("visit_type").value;
		var x = document.getElementById("regdate");
		var y = document.getElementById("disdate");
		if (val == 'i') {
			x.style.display = "none";
			y.style.display = "block";
		} else {
			y.style.display = "none";
			x.style.display = "block";
		}
	}
</script>
<form name="submissionform" method="POST" action="createSubmission.htm">
	<input type="hidden" name="_method" value="create">
	<div class="pageHeader"><insta:ltext key="ui.label.self.pay.claim.batch.submission"/></div>
	<insta:feedback-panel/>
	<div class="helpPanel">
		<table>
			<tr>
				<td valign="top" style="width: 30px"><img src="${cpath}/images/information.png"/></td>
				<td style="padding-bottom: 5px">
					    Self Pay Claims Submission :<br/>
						All bills (Open, Finalized), which are part of claim are eligible for selfpay claim submission.<br/>
						If submission has any open bills please finalize them before generating selfpay claim.<br/>
						If not finalized, selfpay-claim processing cannot be done with open bills.<br/>
						In this case if the open bills have to be excluded from submission for generating e-claim then,<br/>
						delete the submission and create a new submission.
				</td>
			</tr>
		</table>
	</div>

	<fieldset class="fieldSetBorder">
		<legend class="fieldSetLabel"><insta:ltext key="ui.label.filter.criteria"/></legend>


	<table class="searchFormTable" style="border-top:none">
		<tr>
			<td >
						<div style="width: 100%">
							<input type="radio" name="submission_type" id="SelfPay" value="SP" onclick="onChangeSubmissionType();" checked="checked">Self Pay Claim Batch
							<input type="radio" name="submission_type" id="PersonRegister" value="PR" onclick="onChangeSubmissionType();">Person Register Batch
			
						</div>
			</td>
		</tr>
		<tr>
			<td style="border-right:none;">
				<div class="sfLabel">Center/Account Group:</div>
				<div class="sfLabel" style="border-bottom: none;">

				<select name="center_or_account_group" id="center_or_account_group" class="dropdown">
					<c:forEach items="${accountGrpAndCenterList}" var="acc">
						<option value="${acc.id}" ${param.center_or_account_group == acc.id ? 'selected':''}>${acc.ac_name}-(${acc.accounting_company_name})</option>
					</c:forEach>
				</select>

					<input type="hidden" name="center_or_account_group@type" value="text"/>
				</div>
			</td>
	<%-- 		<td id="visit_classification_type" style="display:none;">
				<div class="sboField" style="height: 140px; width: 100%;">
				<div class="sfLabel"><insta:ltext key="billing.billlist.list.visitclassificationtype"/>:</div>
				<div class="sboFieldInput">
				<insta:checkgroup name="visit_classification_type"
							optexts="Medical Tourist,Selfpay,ProformaPayer"
							opvalues="MT,S,PP" selValues="${param.visit_classification_type}" />
					
				</div>
				</div>
			</td> --%>
			<td id="codestatus" style="display:'';">
				<div class="sboField" style="height: 140px; width: 100%;">
					<div class="sfLabel"><insta:ltext key="js.search.patient.visit.toolbar.option.codification"/> 
					<insta:ltext key="registration.optoipconversion.details.status"/>
					:</div>
					<div class="sboFieldInput">
						<insta:checkgroup name="codification_status"
							optexts="In-Progress,Completed,Completed Needs Verification,Verified and Closed"
							opvalues="P,C,R,V" selValues="${param.codification_status}" />
					</div>
				</div>
			</td>
			<td id="dept" style="display:'';">
			<div class="sboField" style="height: 140px; width: 100%;">
					<div class="sfLabel"><insta:ltext key="search.patient.visit.department"/>:</div>
					<div>
					<insta:selectdb name="dept_id" table="department" id="dept_id"
							displaycol="dept_name" valuecol="dept_id" dummyvalue="All" dummyvalueid="" filtered="false" value="" multiple="multiple"/>
					</div>
			</div>
			</td>
		</tr>
		<tr>
			<td style="border-right:none;">
				<div class="sfLabel"><insta:ltext key="billing.billlist.list.patienttype"/>:</div>
				<div class="sfField">
					<select id="visit_type" name="visit_type" class="dropDown" onchange="enableFields()">
						<option  value='i' ${param.visit_type eq 'i' ?'selected':''}>IP</option>
						<option  value='o' ${param.visit_type eq 'o' ?'selected':''}>OP</option>
				 	</select>
				</div>
			</td>

			<td style="border-right:none;">
				<div class="sfLabel" id="regdate"><insta:ltext key="ui.label.registration.date"/>:</div>
				<div class="sfLabel" id="disdate"><insta:ltext key="search.patient.visit.discharge.date"/>:</div>
				<div class="sfField" >
					<div class="sfFieldSub"><insta:ltext key="patient.diag.status.visitdate.from"/>:</div>
					<insta:datewidget name="_reg_date" id="reg_date0" value="${paramValues.reg_date[0]}" onchange = "defaultRegTimeFrom()"/>
					<input type="text" name="_reg_time" id="reg_time0" style="width: 39px" value="${paramValues.reg_time[0]}" />
					<input type="hidden" name="reg_time@type" value="time"/>
					<input type="hidden" name="reg_date_time@op" value="ge,le"/>
					<input type="hidden" name="reg_date_time@type" value="timestamp"/>
				</div>				
				<div class="sfField">
					<div class="sfFieldSub"><insta:ltext key="patient.diag.status.visitdate.to"/>:</div>
					<insta:datewidget name="_reg_date" id="reg_date1" value="${paramValues.reg_date[1]}" onchange = "defaultRegTimeTo()"/>
					<input type="text" name="_reg_time" id="reg_time1" style="width: 39px" value="${paramValues.reg_time[1]}" />
				</div>
			</td>
			<td id="ignoreopenbills" style="display:'';">
			<div class="sfLabel" id="disdate"><insta:ltext key="ui.label.additional.filters"/>:</div>
				<div class="sfField">
					<input type="checkbox" name="ignore_open_bills"/>
					<insta:ltext key="ui.label.ignore.visits.with.open.bills"/>
				</div>
			</td>
		</tr>
	</table>

	<table class="screenActions">
		<tr>
			<td>
				<button type="button" accesskey="C" name="submitClaims" class="button" onclick="return validateSelfPaySubmit();">
				<label><b><u>C</u></b>reate Submission Batch</label></button>&nbsp;
				<label>|</label>
				<a href="./list.htm?status=O&submissionType=SP"><insta:ltext key="ui.label.self.pay.claim.submission.list"/></a>
				<label>|</label>
				<a href="javascript:void(0)" onclick="clearForm(submissionform);initSearch();"><insta:ltext key="registration.patient.common.screenlink.reset"/></a>
			</td>
		 </tr>
	</table>

</form>
</body>
</html>

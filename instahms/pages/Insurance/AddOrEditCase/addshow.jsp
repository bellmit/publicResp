<%@page import="com.bob.hms.common.Constants" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<jsp:useBean id="billstatusDisplay" class="java.util.HashMap"/>
<c:set target="${billstatusDisplay}" property="A" value="Open"/>
<c:set target="${billstatusDisplay}" property="F" value="Finalized"/>
<c:set target="${billstatusDisplay}" property="C" value="Closed"/>

<jsp:useBean id="claimStatusDisplay" class="java.util.HashMap"/>
<c:set target="${claimStatusDisplay}" property="O" value="Open"/>
<c:set target="${claimStatusDisplay}" property="R" value="Received"/>
<c:set target="${claimStatusDisplay}" property="S" value="Sent"/>

<html>
<head>
<title>Insurance Patients List - Insta HMS</title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">

<insta:link type="script" file="hmsvalidation.js"/>
<insta:link type="script" file="Insurance/insurance.js"/>
<insta:link type="script" file="dashboardColors.js"/>
<insta:link type="script" file="ajax.js"/>
<insta:link type="js" file="widgets.js"/>
<insta:link type="css" file="widgets.css"/>

<jsp:include page="/pages/Common/MrnoPrefix.jsp"/>
	<script>
		var contextPath = '<%=request.getContextPath()%>';
		var theForm = document.addNewCase;
		var isBillOpen = '${isBillOpen}';
	</script>
</head>
<body onload="init();">
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<h1 ${whichScreen == 'AddNewCase' ? 'style="float: left"' : '' }>Add/Edit Case</h1>
<c:if test="${whichScreen == 'AddNewCase'}">
	<c:url var="searchUrl" value="AddOrEditCase.do" />
	<insta:patientsearch searchType="mrNo" fieldName="mr_no" searchUrl="${searchUrl}" buttonLabel="Find"
		searchMethod="add" />
</c:if>

<insta:feedback-panel/>
<div >
	<c:choose>
		<c:when test="${not empty insDetails.patient_id}">
			<insta:patientdetails  visitid="${insDetails.patient_id}" />
		</c:when>
		<c:otherwise>
			<c:choose>
				<c:when test="${not empty visitId}">
					<insta:patientdetails  visitid="${visitId}" />
				</c:when>
				<c:otherwise>
					<insta:patientgeneraldetails  mrno="${mr_no}" />
				</c:otherwise>
			</c:choose>
		</c:otherwise>
	</c:choose>
</div>

<form name="addNewCase" method="POST" action="/Insurance/AddOrEditCase.do" onsubmit="getInsDetails(); return false;">
<input type="hidden" name="method" id="_method" value="addshow">
<input type="hidden" name="insurance_id" id="insurance_id" value="${insDetails.insurance_id}" />
<input type="hidden" name="mr_no" id="mr_no" value="${ifn:cleanHtmlAttribute(mr_no)}"/>
<input type="hidden" name="visit_id" id="visit_id" value="${insDetails.patient_id}" />
<input type="hidden"  id="paramVisitId" value="${ifn:cleanHtmlAttribute(visitId)}">
<input type="hidden" name="whichScreen" id="whichScreen" value="${ifn:cleanHtmlAttribute(whichScreen)}" />
<input type="hidden" name="caseStatus" id="caseStatus" value="${insDetails.status}" />
<input type="hidden"  id="casePresent" value="${casePresent}" />
<input type="hidden" id="connectCaseTo" name="connectCaseTo" value="M" />
<input type="hidden" name="finalized_date" id="finalized_date" value="">

<c:set var="totalBillAmount" value="0"/>
<c:set var="totalPatientAmount" value="0"/>
<c:set var="totalClaimAmount" value="0"/>
<c:set var="totalSponsorReceipts" value="0"/>
<c:set var="corpInsurance" value='<%=GenericPreferencesDAO.getAllPrefs().get("corporate_insurance")%>'/>
<c:choose>
<c:when test="${not empty insDetails.patient_id && not empty insDetails.insurance_id}">
<c:choose>
	<c:when test="${not empty insuredBills}">
		<div class="resultList" style="margin: 10px 0px 5px 0px;">
			<table class="detailList dialog_displayColumns" cellspacing="0" cellpadding="0" id="billsTable" border="0" width="100%">
				<tr>
					<th style="width: 30px">Bill No</th>
					<th style="width: 30px">Status</th>
					<th style="width: 30px">Bill Amt</th>
					<th style="width: 30px">Patient Amt</th>
					<th style="width: 30px">Claim Amt</th>
					<th style="width: 30px">Sponsor Receipts</th>
					<th style="width: 30px">Claim Status</th>
					<th style="width: 40px">Approval Amt</th>
				</tr>
					<c:set var="billURL" value="${cpath}/billing/BillAction.do?_method=getCreditBillingCollectScreen"/>
					<c:set var="totalApprovalAmt" value=""/>
					<c:forEach items="${insuredBills}" var="b">
						<tr>
							<td>
								<c:choose>
									<c:when test="${urlRightsMap['credit_bill_collection'] == 'A'}">
										<a target="#" href="${billURL}&billNo=${b.map.bill_no}">
											<b>${b.map.bill_no}</b>
										</a>
									</c:when>
									<c:otherwise>
										<b>${b.map.bill_no}</b>
									</c:otherwise>
								</c:choose>
							</td>
							<td>${billstatusDisplay[b.map.status]}</td>
							<td>${b.map.total_amount}</td>
							<td>${b.map.total_amount - b.map.total_claim}</td>
							<td>${b.map.total_claim}</td>
							<td>${b.map.primary_total_sponsor_receipts}</td>
							<td>${claimStatusDisplay[b.map.primary_claim_status]}</td>
							<td>
								${b.map.approval_amount}
								<c:set var="totalApprovalAmt"
									value="${not empty b.map.approval_amount ? (not empty totalApprovalAmt ?  (totalApprovalAmt + b.map.approval_amount) : (0 + b.map.approval_amount)) : ''}"/>

								<c:set var="totalBillAmount" value="${totalBillAmount + b.map.total_amount}"/>
								<c:set var="totalPatientAmount" value="${totalPatientAmount + (b.map.total_amount - b.map.total_claim)}"/>
								<c:set var="totalClaimAmount" value="${totalClaimAmount + b.map.total_claim}"/>
								<c:set var="totalSponsorReceipts" value="${totalSponsorReceipts + b.map.primary_total_sponsor_receipts}"/>
							</td>
						</tr>
					</c:forEach>
				</table>
			</fieldset>
			<fieldset class="fieldSetBorder">
			  <legend class="fieldSetLabel">Claim Totals</legend>
				<table width="640" align="right" class="infotable">
			   		<tr>
			   			<td></td>
						<td></td>
						<td class="formlabel">Total Bill Amt:</td>
						<td class="forminfo" align="right">
							<label id="lblBillsAmount">${totalBillAmount}</label>
						</td>

						<td class="formlabel">Total Patient Amt:</td>
						<td class="forminfo" align="right">
							<label id="lblBillsPatientAmt">${totalPatientAmount}</label>
						</td>

						<td class="formlabel">Total Claim Amt:</td>
						<td class="forminfo" align="right">
							<label id="totalClaimAmount">${totalClaimAmount}</label>
						</td>
				   	</tr>

					<tr>
						<td></td>
						<td></td>
						<td></td>
						<td></td>
						<td></td>
						<td></td>

						<td class="formlabel">Total Sponsor Receipts:</td>
						<td class="forminfo" align="right">
							<label id="lblSponsorReceipts">${totalSponsorReceipts}</label>
						</td>
				   	</tr>
				   	<tr>
						<td></td>
						<td></td>
						<td></td>
						<td></td>
						<td class="formlabel">Total Approval Amt:</td>
						<td class="forminfo" align="right">
							<label id="totalApprovalAmtLbl">${totalApprovalAmt}</label>
						</td>

						<td class="formlabel">Total Sponsor Due:</td>
						<td class="forminfo" align="right">
							<label id="lblSponsorDue">${totalClaimAmount - totalSponsorReceipts}</label>
						</td>
				   	</tr>
				 </table>
			</fieldset>
		</div>
	</c:when>
	<c:otherwise>
		<div>
			<fieldset class="fieldSetBorder" ><legend class="fieldSetLabel">Bill Details</legend>
				<table class="formTable" width="100%" border="0">
					<tr>
						<td>
							<div style="width: 951px; height: 35px; border: 1px solid #E0E0E0; border-top: none; background-color:#FFC">
								<div style="float: left; width: 25px; margin-top: 10px; margin-left: 3px;">
									<img src="${cpath}/images/alert.png"/>
								</div>
								<div style="float: left; margin-top: 10px">
									No Insured Bills
								</div>
							</div>
						</td>
					</tr>
				</table>
			</fieldset>
		</div>
	</c:otherwise>
	</c:choose>
</c:when>
<c:otherwise>
	<c:choose>
		<c:when test="${not empty isCaseConnected && isCaseConnected == 'Y'}">
			<div>
				<table class="formTable" width="100%" border="0">
					<tr>
						<td>
							<div style="width: 951px; height: 35px; border: 1px solid #E0E0E0; border-top: none; background-color:#FFC">
								<div style="float: left; width: 25px; margin-top: 10px; margin-left: 3px;">
									<img src="${cpath}/images/alert.png"/>
								</div>
								<div style="float: left; margin-top: 10px">
									Visit is connected with case &nbsp;&nbsp;&nbsp;&nbsp;

									<insta:screenlink screenId="ins_dashboard"
									extraParam="?_method=list&method=list
										&_searchMethod=list&mr_no=${mr_no}&mr_no%40op=ilike"
										label="View Case List"/>
								</div>
							</div>
						</td>
					</tr>
				</table>
			</div>
		</c:when>
		<c:otherwise>
			<c:if test="${not empty mr_no}">
				<div>
					<table class="formTable" width="100%" border="0">
						<tr>
							<td>
								<div style="width: 951px; height: 35px; border: 1px solid #E0E0E0; border-top: none; background-color:#FFC">
									<div style="float: left; width: 25px; margin-top: 10px; margin-left: 3px;">
										<img src="${cpath}/images/alert.png"/>
									</div>
									<div style="float: left; margin-top: 10px">
										Visit is not connected to TPA &nbsp;&nbsp;&nbsp;&nbsp;

										<insta:screenlink screenId="ins_newcase"
										extraParam="?_method=connectcase&method=connectcase
											&_searchMethod=connectcase&mr_no=${mr_no}&mr_no%40op=ilike"
											label="View Patient Pending Cases"/>
									</div>
								</div>
							</td>
						</tr>
					</table>
				</div>
			</c:if>
		</c:otherwise>
	</c:choose>
</c:otherwise>
</c:choose>


<div>
	<fieldset class="fieldSetBorder" ><legend class="fieldSetLabel">Case Details</legend>
		<table class="formTable" width="100%" border="0">
			<tr>
				<td class="formlabel">Sponsor:</td>
				<%-- 
				<c:choose>
						<c:when test="${corpInsurance eq 'Y'}">
							<td class="formlabel">Sponsor:</td>
						</c:when>
						<c:otherwise>
							<td class="formlabel">TPA:</td>
						</c:otherwise>
				</c:choose>
				--%>
				<td >
					<c:if test="${whichScreen == 'AddNewCase'}">
						<c:choose>
							<c:when test="${not empty allowedTpas}">
								<select name="tpa_id" id="tpa_id" class="dropdown">
									<option value="">--Select--</option>
									<c:forEach var="allowedTpa" items="${allowedTpas}">
										<option value="${allowedTpa.map.tpa_id}">${allowedTpa.map.tpa_name}</option>
									</c:forEach>
								</select>
							</c:when>
							<c:otherwise>
								<insta:selectdb name="tpa_id" table="tpa_master" valuecol="tpa_id"
									displaycol="tpa_name" filtered="true" value="${default_tpa}" dummyvalue="--Select--" />
							</c:otherwise>
						</c:choose>
					</c:if>
					<c:if test="${whichScreen != 'AddNewCase' && not empty insDetails.tpa_id}">
						<input type="hidden" name="tpa_id" id="tpa_id" value="${insDetails.tpa_id}">
						<select name="tpa_id_disabled" disabled="disabled" class="dropdown">
							<option value="${insDetails.tpa_id}" selected="selected" >${insDetails.tpa_name}</option>
						</select>
					</c:if>
				</td>
				<td class="formlabel">Case No:</td>
				<td class="forminfo">${whichScreen == 'AddOrEditDashboard' ? insDetails.insurance_id : ''}</td>
				<td class="formlabel">Open Date:</td>
				<td ><fmt:formatDate value="${insDetails.case_added_date}" pattern="dd-MM-yyyy"/></td>
			</tr>

			<tr>
				<td class="formlabel">Policy No:</td>
				<td class="forminfo"><input type="text" name="policy_no" id="policy_no" maxlength="50" value="${insDetails.policy_no}" /></td>
				<td class="formlabel">Insurance No:</td>
				<td class="forminfo"><input type="text" name="insurance_no" id="insurance_no" maxlength="50" value="${insDetails.insurance_no}" /></td>
				<td class="formlabel">Policy Holder:</td>
				<td class="forminfo"><input type="text" name="policy_holder_name" id="policy_holder_name"  maxlength="100" value="${insDetails.policy_holder_name}"/></td>
			</tr>

			<tr>
				<td class="formlabel">Patient Relationship:</td>
				<td class="forminfo"><input type="text" name="patient_relationship" id="patient_relationship" maxlength="50" value="${insDetails.patient_relationship}"/></td>
				<td class="formlabel">Case Status:</td>
				<td class="forminfo">
					<insta:selectdb name="status" table="insurance_status" valuecol="status_id" displaycol="status_name" filtered="false" value="${insDetails.status}" />
				</td>
				<td class="formlabel">Case Finalized Date:</td>
				<td class="forminfo">
					<div style="float: left;">
						<c:set var="finalizedTime"><fmt:formatDate value="${insDetails.finalized_date}" pattern="HH:mm"/></c:set>
						<insta:datewidget name="finalizedDate" id="finalizedDate" calButton="true" valueDate="${insDetails.finalized_date}"/>
					</div>
					<div style="float: left;">
						<input type="text" size="5" class="number" id="finalizedTime" name="finalizedTime"
						   value="${finalizedTime}" tabindex="15"  onfocus="makeblank(this);" onblur="setTime(this)" />
					</div>
				</td>
			</tr>
			<tr>
				<td class="formlabel">Case Status Reason:</td>
				<td class="forminfo"><input type="text" name="status_reason" id="status_reason" size="75" maxlength="100" value="${insDetails.status_reason}" /></td>
				<td class="formlabel" >Remarks:</td>
				<td ><textarea name="remarks" id="remarks" >${insDetails.remarks}</textarea></td>
			</tr>
			<tr>
				<td class="formlabel">Diagnosis:</td>
				<td class="forminfo">
					<input type="text" name="diagnosis" id="diagnosis" size="75" maxlength="100" value="${insDetails.diagnosis}" />
				</td>
				<td class="formlabel">Estimate Amount:</td>
				<td class="forminfo">${insDetails.estimate_amt}</td>
			</tr>
			<tr>
				<td></td>
			</tr>

		</table>
	</fieldset>
</div>

<div class="screenActions">
	<button type="button" name="save" id="save" accesskey="S" onclick="return funValidateAndSubmit();"><b><u>S</u></b>ave</button> |
	<a href="javascript:void(0)" onclick="funAddOrEditCancel();">Case List</a>
	<c:if test="${not empty insDetails.insurance_id && whichScreen != 'AddNewCase'}">
		<c:if test="${not empty insDetails.estimate_amt}">
			| <a href="../pages/insurance/EstimateAction.do?method=getQuickEstimationScreen&insuranceID=${insDetails.insurance_id}&moduleId=mod_insurance">Edit Estimate</a>
		</c:if>
		<c:if test="${empty insDetails.estimate_amt}">
			| <a href="../pages/registration/QuickEstimate.do?_method=getQuickEstimateScreen&mr_no=${ifn:cleanURL(param.mr_no)}">New Estimate</a>
		</c:if>
		<c:if test="${insDetails.preauth_doc_id != 0}">
			| <a onclick="return funCheckForPreauthForm(this,'${insDetails.tpa_pdf_form}');" href="PreAuthorizationForms.do?_method=searchPatientGeneralDocuments&mr_no=${insDetails.mr_no}&patient_id=${insDetails.patient_id}&insurance_id=${insDetails.insurance_id}">Edit Preauth</a>
		</c:if>
		<c:if test="${insDetails.preauth_doc_id == 0}">
			| <a onclick="return funCheckForPreauthForm(this,'${insDetails.tpa_pdf_form}');" href="PreAuthorizationForms.do?_method=add&mr_no=${insDetails.mr_no}&template_id=${insDetails.tpa_pdf_form}&format=doc_pdf_form_templates&insurance_id=${insDetails.insurance_id}&patient_id=${insDetails.patient_id}">New Preauth</a>
		</c:if>
		<c:if test="${not empty insDetails.claim_id}">
			| <a onclick="return funCheckForCaseConnected(this,'${insDetails.insurance_id}','${insDetails.claim_template_id}','${insDetails.default_claim_template}');" href="InsuranceClaim.do?_method=show&insurance_id=${insDetails.insurance_id}">Edit Claim</a>
		</c:if>
		<c:if test="${empty insDetails.claim_id}">
			| <a onclick="return funCheckForCaseConnected(this,'${insDetails.insurance_id}','${insDetails.claim_template_id}','${insDetails.default_claim_template}');" href="InsuranceClaim.do?_method=show&insurance_id=${insDetails.insurance_id}">New Claim</a>
		</c:if>
		| <a href="InsuranceGenericDocuments.do?_method=searchPatientGeneralDocuments&mr_no=${insDetails.mr_no}&patient_id=${insDetails.patient_id}&insurance_id=${insDetails.insurance_id}">Documents</a>
		| <a href="SendToTpa.do?_method=show&mr_no=${insDetails.mr_no}&visit_id=${insDetails.patient_id}&insurance_id=${insDetails.insurance_id}">Send</a>
		<c:choose>
			<c:when test="${not empty insDetails.patient_id}">
				| <a href="UploadReceivedDocs.do?_method=add&visit_id=${insDetails.patient_id}&insurance_id=${insDetails.insurance_id}&mr_no=${insDetails.mr_no}">Upload</a>
			</c:when>
			<c:otherwise>
				| <a href="UploadReceivedDocs.do?_method=add&mr_no=${insDetails.mr_no}&insurance_id=${insDetails.insurance_id}">Upload</a>
			</c:otherwise>
		</c:choose>
		| <a href="InsuranceHistory.do?_method=show&mr_no=${insDetails.mr_no}&visit_id=${insDetails.patient_id}&insurance_id=${insDetails.insurance_id}">History</a>
		| <a href="${cpath}/Insurance/AddOrEditCase.do?_method=show">Add New Case</a>
	</c:if>
</div>
</form>
</body>

</html>

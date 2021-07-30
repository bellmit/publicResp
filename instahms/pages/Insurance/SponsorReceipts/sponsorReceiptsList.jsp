<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page isELIgnored="false" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>

<c:set var="dummyValue">
<insta:ltext key="insurance.patientapprovallist.patientapprovals.selectAll"/>
</c:set>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Sponsor Receipts - Insta HMS</title>
<insta:js-bundle prefix="billing.salucro"/>
<insta:link type="css" file="widgets.css"/>
<insta:link type="js" file="widgets.js"/>
<insta:link type="script" file="hmsvalidation.js"/>
<insta:link type="js" file="dashboardsearch.js"/>
<insta:link type="script" file="dashboardColors.js"/>
<insta:link type="script" file="billPaymentCommon.js"/>
<insta:link type="script" file="sponsorReceipt.js"/>
<insta:link type="js" file="instaautocomplete.js" />
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="visibilityStatus" value="${visibilityStatus}"/>
<c:set var="no_of_credit_debit_card_digits" value='<%=GenericPreferencesDAO.getAllPrefs().get("no_of_credit_debit_card_digits") %>'/>

<script type="text/javascript">
var no_of_credit_debit_card_digits = ${empty no_of_credit_debit_card_digits ? 0 : no_of_credit_debit_card_digits};
function initSponsorReceipt() {
	initMrNoAutoComplete(cpath, 'mrno', 'mrnoContainer', 'active');
	toggle_visibility(${visibilityStatus});
	document.getElementById('paymentType').disabled = true;
	document.getElementById('addPaymentMode').disabled = true;
}
function validateCollectionCenterData(){
	var valid = true;
	valid= valid && validateAllNumerics();
	valid = valid && validateCounter();
	if(valid){
		for(i=0;i<${fn:length(pagedList.dtoList)};i++){
			if(document.getElementById("recdAmt"+i).value != ''){
				if (!validateAmount(document.getElementById("recdAmt"+i), "Enter valid amount")){
					document.getElementById("recdAmt"+i).focus();
					return false;
				}
				if(parseFloat(document.getElementById("recdAmt"+i).value) > parseFloat(document.getElementById("amount_due"+i).value)){
					alert("Recd. Amount should be less than or equal to corresponding Amount Due.");
					var index="recdAmt"+i;
					document.getElementById("recdAmt"+i).focus();
					return false;
				}
			}
		}
		// Collection center should not empty while update.  totPayingAmt0
		if(document.SponsorReceiptsForm.totPayingAmt0.value == ''){
			alert("Pay Amount should not empty.");
			document.SponsorReceiptsForm.totPayingAmt0.focus();
			return false;
		} else {
			// to varify the Total Amount and Recd. Amount should be equal.
			var total=0;
			for(i=0;i<${fn:length(pagedList.dtoList)};i++){
				if(document.getElementById("recdAmt"+i).value != ''){
					total= parseFloat(total)+ parseFloat(document.getElementById("recdAmt"+i).value);
				}
			}
			if(total.toFixed(2) != parseFloat(document.SponsorReceiptsForm.totPayingAmt0.value)){
				alert("Pay Amount and sum of Recd. Amount should match.");
				document.SponsorReceiptsForm.totPayingAmt0.focus();
				return false;
			}
		}
		document.SponsorReceiptsForm.submit();
	}
	return valid;
}
</script>
	<insta:js-bundle prefix="laboratory.radiology.billpaymentcommon"/>
</head>

<body onload="initSponsorReceipt();">

	<h1>Sponsor Receipts- Unpaid Bills</h1>
	<insta:feedback-panel/>
	<form action="SponsorReceipts.do" method="GET" name="sponsorReceiptStatusForm" autocomplete="off">
		<input type="hidden" name="_method"value="list"/>
		<table class="formtable">
			<tr>
				<td>
					<fieldset class="fieldSetBorder">
					<legend class="fieldSetLabel">Unpaid Bills</legend>
						<table>
							<tr>
								<td class="formlabel">Primary Center:</td>
								<td class="forminfo">
								<select name="primary_center_id" id="primary_center_id" class="dropdown">
									<option value="">${dummyValue}</option>
									<c:forEach items="${centers}" var="center">
										<option value="${center.map.center_id}"${param.primary_center_id == center.map.center_id ? 'selected' : ''}>
										${center.map.center_name}</option>
									</c:forEach>
							 	</select>
							 	<input type="hidden" name="primary_center_id@type" value="int"/>
							 	<input type="hidden" name="primary_center_id@cast" value="y"/>
								</td>
							</tr>
							<tr>
								<td class="formlabel">Sponsor:</td>
								<td>
									<insta:selectdb id="sponsor_id" name="sponsor_id" table="tpa_master" valuecol="tpa_id"
									displaycol="tpa_name"  value="${param.sponsor_id}" dummyvalue="---Select---"
									orderby="tpa_name"/>
									<td><span class="star" style="margin-left: -8px;">*</span></td>
								</td>
								<td class="formlabel">From:</td>
								<td>
								<insta:datewidget name="open_date" id="open_date0"
										value="${paramValues.open_date[0]}" onchange="doValidateDateField(this,'past');"/>
								</td><td></td><td></td><td></td><td></td><td></td><td></td><td></td>
							</tr>
							<tr>
								<td class="formlabel" style="padding-right: 7px;">MR No:</td>
								<td>
									<div id="mrNoAutocomplete">
										<input type="text" name="mr_no" id="mrno" size="10" value="${ifn:cleanHtmlAttribute(param.mr_no)}" style="margin-top: -10px;"/>
										<div id="mrnoContainer"></div>
									</div>
								</td>
								<td></td>
								<td class="formlabel">To:</td>
								<td>
								<insta:datewidget name="open_date" id="open_date1"
										value="${paramValues.open_date[1]}" onchange="doValidateDateField(this,'past');"/>
									<input type="hidden" name="open_date@op" value="ge,le"/>
								</td><td></td><td></td><td></td><td></td><td></td><td></td><td></td>
							</tr>
							<tr>
								
								<td>
									<button type="button" id="getDetails" name="getDetails" accesskey="G" value="Get Details" onclick="return validate();">
									<label><u><b>G</b></u>et Details</label>
									</button>
								</td>
								<td></td>
								<td></td>
								<td></td>
							</tr>
						</table>
					</fieldset>
				</td>
			</tr>
	   </table>
	</form>
	<form name="SponsorReceiptsForm" action="SponsorReceipts.do" method="POST" id="SponsorReceiptsForm" autocomplete="off">
		<input type="hidden" name="_method" value="update"/>
		<input type="hidden" name="_searchMethod" value="update"/>
		<input type="hidden" name="sponsor_id" value="${ifn:cleanHtmlAttribute(param.sponsor_id)}"/>
		<input type="hidden" name="primary_center_id" value="${ifn:cleanHtmlAttribute(param.primary_center_id)}"/>
		<dl class="accordion" style="margin-bottom: 10px;">
			<dt>
				<span><insta:ltext key="billing.patientbill.details.payments" /></span>
				<div class="clrboth"></div>
			</dt>
			<dd id="payDD" class="'open'}">
				<div class="bd">

					<c:set var="paymentSelValue" value="SR" />

					<c:set var="bnow" value="false" />
					<c:set var="insp" value="true" />
					<c:set var="prisnp" value="true" />
					<c:set var="secsnp" value="false" />
					<c:set var="primarySponsor" value="${patient.primary_sponsor_id}"/>
					<c:set var="secondarySponsor" value="${patient.secondary_sponsor_id}"/>

					<insta:billPaymentDetails formName="SponsorReceiptsForm"
						isBillNowPayment="${bnow}" defaultPaymentType="${paymentSelValue}"
						isInsuredPayment="${insp}" isPrimarySponsorPayment="${prisnp}"
						isSecondarySponsorPayment="${secsnp}" 
						primarySponsor="${primarySponsor}" secondarySponsor="${secondarySponsor}" />
				</div>
			</dd>
		</dl>
		<div class="resultList">
			<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
				<tr onmouseover="hideToolBar();">
					<th>MR No</th>
					<th>Patient Name</th>
					<th>Consolidated Bill No</th>
					<th>Amount Due</th>
					<th>Recd. Amount</th>
				</tr>
				<c:forEach var="record" items="${pagedList.dtoList}" varStatus="st">
					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						 id="toolbarRow${st.index}">
						<td>${record.mr_no}<input type="hidden" name="mr_nos" id="mr_nos${st.index}" value="${record.mr_no}"/></td>
						<td>${record.patient_name}</td>
						<td>${record.consolidated_bill_no}<input type="hidden" name="consolidated_bill_no" id="consolidated_bill_no${st.index}" value="${record.consolidated_bill_no}"/></td>
						<td>${record.insurance_claim_amt - record.sponsor_received_amt}
							<input type="hidden" name="amount_due" id="amount_due${st.index}" value="${record.insurance_claim_amt - record.sponsor_received_amt}"/>
						</td>
						<td><input type="text" name="recdAmt" id="recdAmt${st.index}" value=""></td>
					</tr>
				</c:forEach>
			</table>
		</div>
	
		<c:url var="url" value="SponsorReceipts.do">
			<c:param name="_method" value="list"></c:param>
		</c:url>
	
		<table class="screenActions">
			<tr>
				<td><button type="button" value="Update" accesskey="U" name="updateDetails" onclick="return validateCollectionCenterData();">
				<label><u><b>U</b></u>pdate</label></button></td>
				<td>&nbsp;|&nbsp;</td><td><a title="Cancel" href="<c:out value='${url}'/>">Clear</a>&nbsp;</td>
				<td><insta:screenlink screenId="dialysis_order" addPipe="true" label="Dialysis Order"
							extraParam="?_method=showDialysisOrder"/></td>
			</tr>
		</table>
	</form>
</body>
</html>

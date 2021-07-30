<%@ taglib uri="/WEB-INF/struts-html.tld"  prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld"  prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld"  prefix="logic" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>


<c:set var="bill" value="${billDetails.bill}"/>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="billPrintRights" value="${urlRightsMap.bill_print}"/>
<c:set var="isInsuranceBill" value="${bill.is_tpa && (bill.restrictionType == 'N' || bill.restrictionType == 'P')}"/>
<c:set var="points_redemption_rate" value="${not empty genPrefs.points_redemption_rate ? genPrefs.points_redemption_rate : 0}"/>
<c:set var="all">
	<insta:ltext key="billing.patientbill.details.all"/>
</c:set>
<c:set var="all2">
	<insta:ltext key="billing.patientbill.details.all.in.brackets"/>
</c:set>
<c:set var="all1">
	<insta:ltext key="billing.patientbill.details.included"/>,
	<insta:ltext key="billing.patientbill.details.excluded"/>
</c:set>

<%-- bill type display --%>
<c:set var="billTypeDisplay">
	<c:choose>
		<c:when test="${bill.billType == 'P' && bill.restrictionType == 'P'}"><insta:ltext key="billing.patientbill.details.billnow_ph"/></c:when>
		<c:when test="${bill.billType == 'C' && bill.restrictionType == 'P'}"><insta:ltext key="billing.patientbill.details.billlater_ph"/></c:when>
		<c:when test="${bill.billType == 'P'}"><insta:ltext key="billing.patientbill.details.billnow"/></c:when>
		<c:when test="${bill.billType == 'C'}"><insta:ltext key="billing.patientbill.details.billlater"/></c:when>
		<c:otherwise><insta:ltext key="billing.patientbill.details.other"/></c:otherwise>
	</c:choose>
</c:set>

<c:if test="${multiVisitBill == 'Y'}">
	<c:set var="billTypeDisplay" value="${billTypeDisplay}-Pkg"/>
</c:if>

<c:if test="${bill.billType == 'C' && bill.isPrimaryBill == 'N'}">
	<c:set var="billTypeDisplay" value="Sec. ${billTypeDisplay}"/>
</c:if>

<%@page import="flexjson.JSONSerializer"%>
<html>
<head>
	<title><insta:ltext key="billing.patientbill.details.billtitle"/> ${ifn:cleanHtml(bill.billNo)} - <insta:ltext key="billing.patientbill.details.instahms"/></title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<meta name="i18nSupport" content="true"/>
	
	<insta:link type="script" file="billing/creditNote.js"/>
	
	<script type="text/javascript">
		var templateList = <%= request.getAttribute("templateListJSON") %>;
		var origBillStatus = '${ifn:cleanJavaScript(bill.status)}';
		var billType = '${ifn:cleanJavaScript(bill.billType)}';
		var existingReceipts = ${empty existingReceipts ? 0 : existingReceipts};
		var existingSponsorReceipts = ${empty existingSponsorReceipts ? 0 : existingSponsorReceipts};
		var existingRecdAmount = ${empty existingRecdAmount ? 0 : existingRecdAmount};
		var sponsorBillNo = '${ifn:cleanJavaScript(bill.sponsorBillNo)}';
		var priSponsorType = '${patient.sponsor_type}';
		var secSponsorType = '${patient.sec_sponsor_type}';
		var priSponsorId = '${patient.primary_sponsor_id}';
		var secSponsorId = '${patient.secondary_sponsor_id}';
		var priTpaName = <insta:jsString value="${patient.tpa_name}"/>;
		var secTpaName = <insta:jsString value="${patient.sec_tpa_name}"/>;
	</script>
	
</head>
<body onload="ajaxForPrintUrls();" class="yui-skin-sam">
	<table>
		<tr>
			<td width="100%"><h1><insta:ltext key="js.billing.billlist.creditnote"/></h1></td>
		</tr>
	</table>
	
	<div><insta:feedback-panel/></div>	
	
	<c:if test="${bill.visitType == 'r'}">
		<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel"><insta:ltext key="billing.patientbill.details.customerdetails"/></legend>
			<table class="formtable">
				<tr>
					<td><insta:ltext key="billing.patientbill.details.customername"/>:</td>
					<td class="forminfo">${retailCustomer.customer_name}</td>
					<td><insta:ltext key="billing.patientbill.details.sponsor"/>:</td>
					<td class="forminfo">${retailCustomer.sponsor_name}</td>
				</tr>
			</table>
		</fieldset>
	</c:if>

	<c:if test="${bill.visitType == 't'}">
		<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel"><insta:ltext key="billing.patientbill.details.incomingtestdetails"/></legend>
			<table class="formtable">
				<tr>
					<td><insta:ltext key="billing.patientbill.details.hospitalname"/>:</td>
					<td class="forminfo">${incomingCustomer.map.hospital_name}</td>
					<td><insta:ltext key="ui.label.patient.name"/>:</td>
					<td class="forminfo">${incomingCustomer.map.patient_name}</td>
					<td><insta:ltext key="billing.patientbill.details.age.or.gender"/>:</td>
					<td class="forminfo">${incomingCustomer.map.age_text}${fn:toLowerCase(incomingCustomer.map.age_unit)} / ${incomingCustomer.map.gender}</td>
				</tr>
				<c:if test="${bill.billRatePlanName != 'GENERAL' }">
				<tr>
					<td><insta:ltext key="billing.patientbill.details.rateplan"/>:</td>
					<td class="forminfo">${ifn:cleanHtml(bill.billRatePlanName)}</td>
				</tr>
				</c:if>
			</table>
		</fieldset>
	</c:if>
	
	<div class="">
		<c:if test="${not (bill.visitType == 'r' || bill.visitType == 't')}">
			<insta:patientdetails patient="${patient}" />
		</c:if>
	</div>
	
	<form name="mainform" method="post" action="CreditNoteAction.do" autocomplete="off">
		<input type="hidden" name="billNo" value="${ifn:cleanHtmlAttribute(bill.billNo)}">
		<c:choose>
			<c:when test="${bill.visitType == 't'}">
				<input type="hidden" name="visitId" id="visitId" value="${incomingCustomer.map.incoming_visit_id}">
			</c:when>
			<c:otherwise>
				<input type="hidden" name="visitId" id="visitId" value="${patient.patient_id}">
			</c:otherwise>
		</c:choose>	
		<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel"><insta:ltext key="billing.patientbill.details.billdetails"/></legend>
			<table class="formtable">
				<%-- Information row: Bill no (type), Open date (by), Billing Bed Type --%>
				<tr>
					<c:choose>
						<c:when test="${bill.totalClaim < 0}">
							<td class="formlabel"><insta:ltext key="js.billing.billlist.sponsorcreditnote"></insta:ltext>:</td>
							<td class="forminfo">(${sponsorType} Sponsor)</td>
						</c:when>
						<c:otherwise>
							<td class="formlabel"><insta:ltext key="js.billing.billlist.patientcreditnote"></insta:ltext></td>
							<td class="forminfo"></td>
						</c:otherwise>
					</c:choose>
					<td class="formlabel"><insta:ltext key="js.billing.billlist.creditnotereason"/>:</td>
					<td class="forminfo">
						${ifn:cleanHtmlAttribute(bill.creditNoteReasons)}
					</td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="billing.patientbill.details.creditnoteno"/>:</td>
					<td class="forminfo">${ifn:cleanHtml(bill.billNo)} 
						<input type="hidden" name="billType" value="${ifn:cleanHtmlAttribute(bill.billType)}"/>
					</td>
	
					<td class="formlabel"><insta:ltext key="billing.patientbill.details.opendate.by.in.brackets"/>:</td>
					<td class="forminfo">
						<c:set var="opendate"><fmt:formatDate value="${bill.openDate}" pattern="dd-MM-yyyy"/></c:set>
						<c:set var="opentime"><fmt:formatDate value="${bill.openDate}" pattern="HH:mm"/></c:set>
						<fmt:formatDate value="${bill.openDate}" pattern="dd-MM-yyyy"/>
						<fmt:formatDate value="${bill.openDate}" pattern="HH:mm"/> (${ifn:cleanHtml(bill.openedBy)})
					</td>	
				</tr>
				<%-- Status row: Status --%>
				<tr>
					<td class="formlabel"><insta:ltext key="billing.patientbill.details.billstatus"/>:</td>
					<td class="forminfo">
						<b><insta:ltext key="billing.patientbill.details.closed"/></b>
					</td>
					<td class="formlabel"><insta:ltext key="billing.patientbill.details.remarks"/>:</td>
					<td class="forminfo">
						${ifn:cleanHtmlAttribute(bill.billRemarks)}
					</td>
				</tr>
			</table>
		</fieldset>
		<table>
			<tr>
				<td width="80%">
					<table class="formtable">
						<tr>
							<td class="formlabel"><insta:ltext key="billing.patientbill.details.filter"/>&nbsp;<insta:ltext key="billing.patientbill.details.service"/>&nbsp;<insta:ltext key="billing.patientbill.details.groups"/>: </td>
							<td>
								<insta:selectdb name="filterServiceGroup" table="service_groups" valuecol="service_group_id"
								displaycol="service_group_name" orderby="display_order" dummyvalue="${all2}" dummyvalueId=""
								onchange="onChangeFilter(this);"/>
							</td>
			
							<td class="formlabel"><insta:ltext key="billing.patientbill.details.filter"/>&nbsp;<insta:ltext key="billing.patientbill.details.charge"/>&nbsp;<insta:ltext key="billing.patientbill.details.heads"/>:</td>
							<td>
								<insta:selectdb name="filterChargeHead" table="chargehead_constants" valuecol="chargehead_id"
								displaycol="chargehead_name" filtered="false" orderby="display_order" dummyvalue="${all2}" dummyvalueId=""
								onchange="onChangeFilter(this);"/>
							</td>
			
							<td class="formlabel">&nbsp;</td>
							<td class="forminfo">&nbsp;</td>
						</tr>
					</table>
				</td>
				<td width="20%">
					<table id="dynaPkgFilterTable" class="formtable">
					<tr>
						<c:if test="${hasDynaPackage}">
							<td class="formlabel"><insta:ltext key="billing.patientbill.details.package"/>:</td>
							<td class="forminfo">
								<insta:selectoptions name="filterPackage" optexts="${all1}"
								opvalues="Included,Excluded" dummyvalue="${all}" value="" onchange="onChangeFilter(this);"/>
							</td>
						</c:if>
					</tr>
					</table>
				</td>
			</tr>
		</table>
		<div class="resultList" style="margin: 10px 0px 5px 0px;">
			<table class="detailList dialog_displayColumns" cellspacing="0" cellpadding="0" id="chargesTable" border="0" width="100%">
				<tr bgcolor="#8FBC8F" id="chRow0">
					<th style="width: 80px"><insta:ltext key="billing.patientbill.details.date"/></th>
					<th style="width: 30px"><insta:ltext key="billing.patientbill.details.ord"/>#</th>
					<th><insta:ltext key="billing.patientbill.details.head"/></th>
					<th style="width: 60px" title="${ifn:cleanHtmlAttribute(code)}"><insta:ltext key="billing.patientbill.details.code"/></th>
					<th style="width: 130px"><insta:ltext key="billing.patientbill.details.description"/></th>
					<th><insta:ltext key="billing.patientbill.details.details"/></th>
					<th class="number"><insta:ltext key="billing.patientbill.details.rate"/></th>
					<th class="number"><insta:ltext key="billing.patientbill.details.qty"/></th>
					<th class="number" style="width: 10px"></th>
					<th class="number"><insta:ltext key="billing.patientbill.details.amt"/></th>
					<th class="number"></th>
					<c:if test="${isInsuranceBill}">
						<c:choose>
							<c:when test="${multiPlanExists}">
								<th class="number" title="${priclaimamt}"><insta:ltext key="billing.patientbill.details.prisponsor"/></th>
								<th class="number" title="${secclaimamt}"><insta:ltext key="billing.patientbill.details.secsponsor"/></th>
								<th class="number" title="${patientamt}"><insta:ltext key="billing.patientbill.details.patient"/></th>
								<th><insta:ltext key="billing.patientbill.details.pripriorauthno"/></th>
								<th><insta:ltext key="billing.patientbill.details.secpriorauthno"/></th>
							</c:when>
							<c:otherwise>
									<th class="number" title="${sponseramt}"><insta:ltext key="billing.patientbill.details.sponsor"/></th>
									<th class="number" title="${patientamt}"><insta:ltext key="billing.patientbill.details.patient"/></th>
									<th><insta:ltext key="billing.patientbill.details.priorauthno"/></th>
							</c:otherwise>
						</c:choose>	
					</c:if>
				</tr>
				<c:set var="rounded" value="N"/>
				<c:set var="totalClaimAmount" value="0"/>
				<c:set var="numCharges" value="${fn:length(billDetails.charges)}"/>
				
				<%-- we add one hidden row with a null charge for use as a template to clone from --%>
			<c:forEach begin="1" end="${numCharges+1}" var="i" varStatus="loop">
				<c:set var="charge" value="${billDetails.charges[i-1]}"/>
				<c:set var="flagColor">
					<c:choose>
						<c:when test="${charge.status == 'X'}"><insta:ltext key="billing.patientbill.details.red"/></c:when>
						<c:when test="${charge.chargeExcluded == 'Y'}"><insta:ltext key="billing.patientbill.details.blue"/></c:when>
						<c:when test="${charge.chargeExcluded == 'P'}"><insta:ltext key="billing.patientbill.details.blue"/></c:when>
						<c:when test="${hasRewardPointsEligibility && charge.eligible_to_redeem_points == 'Y'}"><insta:ltext key="billing.patientbill.details.green"/></c:when>
						<c:otherwise><insta:ltext key="billing.patientbill.details.empty"/></c:otherwise>
					</c:choose>
				</c:set>
	
				<c:if test="${empty charge}">
					<c:set var="style" value='style="display:none"'/>
				</c:if>
	
				<tr ${style}>
					<td>
						<img src="${cpath}/images/${flagColor}_flag.gif"/>
						<label>
							<fmt:formatDate value="${charge.postedDate}" pattern="dd-MM-yyyy"/>
						</label>
					</td>
					<td>
						<label>${charge.orderNumber}</label>
					</td>
					
					<td>
						<label title="${charge.chargeHeadName}">${charge.chargeHeadName}</label>
						<input type="hidden" name="chargeHeadName" value='${charge.chargeHeadName}'>
						<input type="hidden" name="chargeGroupName" value='${charge.chargeGroupName}'>
						<input type="hidden" name="chargeGroupId" value='${charge.chargeGroup}'>
						<input type="hidden" name="chargeHeadId" value='${charge.chargeHead}'>
						<input type="hidden" name="chargeId" value='${charge.chargeId}' >
						<input type="hidden" name="chargeRef" value='${charge.chargeRef}' >
						<input type="hidden" name="departmentId" value='${charge.actDepartmentId}'>
						<input type="hidden" name="hasActivity" value='${charge.hasActivity}'>
						<input type="hidden" name="payeeDocId" value='${charge.payeeDoctorId}'>
						<input type="hidden" name="docPaymentId" value='${charge.docPaymentId}'>
						<input type="hidden" name="activityConducted" value='${charge.activityConducted}'>
						<input type="hidden" name="prescDocId" value='${charge.prescribingDrId}'>
						<input type="hidden" name="actItemCode" value='${charge.actItemCode}'>
						<input type="hidden" name="edited" value='false'>
						<input type="hidden" name="remarks" value="${charge.actRemarks}"/>
						<input type="hidden" name="userRemarks" value="${charge.userRemarks}"/>
						<input type="hidden" name="itemRemarks" value="${charge.itemRemarks}"/>
						<input type="hidden" name="allowDiscount" value='${charge.allowDiscount}' >
						<input type="hidden" name="allowRateVariation" value='${charge.allowRateVariation}' >
						<input type="hidden" name="service_sub_group_id" value='${charge.serviceSubGroupId}' >
						<input type="hidden" name="service_group_id" value='${charge.serviceGroupId}' >
						<input type="hidden" name="serviceGroupName" value='${charge.serviceGroupName}' >
						<input type="hidden" name="serviceSubGroupName" value='${charge.serviceSubGroupName}' >
						<input type="hidden" name="conducting_doc_mandatory" value='${charge.conducting_doc_mandatory}' >
						<input type="hidden" name="delCharge" value="${charge.status=='X'?'true':'false'}"	/>
						<input type="hidden" name="chargeExcluded" value="${charge.chargeExcluded}"/>
						<input type="hidden" name="packageFinalized" value="${charge.packageFinalized}"/>
						<input type="hidden" name="consultation_type_id" value='${charge.consultation_type_id}' >
						<input type="hidden" name="op_id" value='${charge.op_id}' >
						<input type="hidden" name="from_date" value='${charge.from_date}' />
						<input type="hidden" name="to_date" value='${charge.to_date}' />
						<input type="hidden" name="insuranceCategoryId" value='${charge.insuranceCategoryId}' >
						<input type="hidden" name="pseudoPatientAmt" value='${charge.amount - charge.insuranceClaimAmount}' >
						<input type="hidden" name="firstOfCategory" value='${charge.firstOfCategory}' >
						<input type="hidden" name="allowRateIncrease" value="${charge.allowRateIncrease }"/>
						<input type="hidden" name="allowRateDecrease" value="${charge.allowRateDecrease }"/>
						<input type="hidden" name="eligible_to_redeem_points" value='${charge.eligible_to_redeem_points}' >
						<input type="hidden" name="redemption_cap_percent" value='${charge.redemption_cap_percent}' >
						<input type="hidden" name="redeemed_points" value='${charge.redeemed_points}' >
						<input type="hidden" name="max_redeemable_points" value='0' >
						<input type="hidden" name="amount" value='${charge.amount}'>
						<input type="hidden" name="discount" value='${charge.discount}'>
						<input type="hidden" name="isClaimLocked" value='${charge.isClaimLocked}'>
					</td>

					<td>
						<label>${charge.actRatePlanItemCode}</label>
					</td>
					<td>
						<div style="width:130px; white-space:nowrap; overflow:hidden; text-overflow:ellipsis;" title="${charge.actDescription}">
							${charge.actDescription}
						</div>
					</td>
					<td>
						<c:choose>
							<c:when test="${charge.chargeHead == 'PHMED' || charge.chargeHead == 'PHRET' || charge.chargeHead == 'PHCMED' || charge.chargeHead == 'PHCRET'}">
								<c:set var="remLen" value="${fn:length(charge.actRemarks)}"/>
								<c:set var="remarks" value="${fn:substring(charge.actRemarks,4,remLen)}"/>
								<a target="#" title="${charge.actRemarks}"
								 href="${cpath}/pages/stores/MedicineSalesPrint.do?method=getSalesPrint&printerId=0&duplicate=true&saleId=${fn:substring(charge.actRemarks,4,remLen)}">
									<c:out value="${fn:substring(charge.actRemarks,0,15)}"/>
								</a>
							</c:when>
							<c:when test="${charge.chargeHead eq 'INVITE'}">
								<c:set var="remLen" value="${fn:length(charge.actRemarks)}"/>
								<c:set var="remarks" value="${fn:substring(charge.actRemarks,4,remLen)}"/>
								<a target="#" title="${charge.actRemarks}"
								 href="${cpath}/DirectReport.do?report=StoreStockPatientIssues&issNo=${fn:substring(charge.actRemarks,4,remLen)}">
									<c:out value="${fn:substring(charge.actRemarks,0,15)}"/>
								</a>
							</c:when>
							<c:otherwise>
								<c:set var="remarks" value=""/>
								<insta:truncLabel value="${charge.actRemarks}" length="16"/>
							</c:otherwise>
						</c:choose>
					</td>
					<td class="number">
						<label>${charge.actRate}</label>
					</td>
	
					<td class="number">
						<label>${charge.actQuantity}</label>
					</td>
	
					<td>
						<label>${charge.actUnit}</label>
					</td>
					<td class="number">
						<label>${charge.amount}</label>
					</td>
					<td class="number" style="display:none">
						<%-- <label>${charge.amount}</label> --%>
						<input type="hidden" name="amt" value='${charge.amount}' />
						<input type="hidden" name="amount_included" value="${charge.amount_included}"/>
						<input type="hidden" name="serviceChrgApplicable" value='${charge.serviceChrgApplicable}' />
					</td>
					<c:if test="${isInsuranceBill}">
						<td class="number">
							<c:choose>
								<c:when test="${charge.chargeHead == 'PHCRET' || charge.chargeHead == 'PHRET'}">
									<label>${ifn:afmt(0)}</label>
								</c:when>
								<c:otherwise>
									<label>${ifn:afmt(charge.claimAmounts[0])}</label>
								</c:otherwise>
							</c:choose>
							<input type="hidden" name="insClaimAmt" value='${charge.claimAmounts[0]}' />
							<input type="hidden" name="priInsClaimAmt" value='${charge.claimAmounts[0]}' />
							<input type="hidden" name="priIncludeInClaim" value='${charge.includeInClaimCalc[0]}' />
							<input type="hidden" name="insClaimable" value="${charge.insurancePayable}"/>
							<input type="hidden" name="insClaimTaxable" value="${charge.insuranceClaimTaxable}"/>
						</td>
						<c:if test="${multiPlanExists}">
							<td class="number">
								<c:choose>
									<c:when test="${charge.chargeHead == 'PHCRET' || charge.chargeHead == 'PHRET'}">
										<label>${ifn:afmt(0)}</label>
									</c:when>
									<c:otherwise>
										<label>${ifn:afmt(charge.claimAmounts[1])}</label>
									</c:otherwise>
								</c:choose>
								<input type="hidden" name="secInsClaimAmt" value='${charge.claimAmounts[1]}' />
								<input type="hidden" name="secIncludeInClaim" value='${charge.includeInClaimCalc[1]}' />
								<input type="hidden" name="insClaimable" value="${charge.insurancePayable}"/>
								<input type="hidden" name="insClaimTaxable" value="${charge.insuranceClaimTaxable}"/>
							</td>
						</c:if>
	
						<td class="number">
							<c:choose>
								<c:when test="${charge.chargeHead == 'PHCRET' || charge.chargeHead == 'PHRET'}">
									<label>${ifn:afmt(0)}</label>
								</c:when>
								<c:when test="${charge.chargeHead == 'PHCMED' || charge.chargeHead == 'PHMED'}">
									<label>${(charge.amount + charge.returnAmt) - charge.claimAmounts[0] - charge.claimAmounts[1]}</label>
								</c:when>
								<c:otherwise>
									<label>${charge.amount - charge.claimAmounts[0] - charge.claimAmounts[1]}</label>
								</c:otherwise>
							</c:choose>
							<input type="hidden" name="returnAmt" value='${charge.returnAmt}' />
							<input type="hidden" name="returnInsuranceClaimAmt" value='${charge.returnInsuranceClaimAmt}' />
							<input type="hidden" name="insDeductionAmt" value='${charge.amount - charge.claimAmounts[0] - charge.claimAmounts[1]}' />
						</td>
						<td>
							<label>${charge.preAuthIds[0]}</label>
							<input type="hidden" name="preAuthId" value='${charge.preAuthIds[0]}' >
							<input type="hidden" name="preAuthModeId" value='${charge.preAuthModeIds[0]}' >
						</td>
						<c:if test="${multiPlanExists}">
							<td>
								<label>${charge.preAuthIds[1]}</label>
								<input type="hidden" name="secPreAuthId" value='${charge.preAuthIds[1]}' >
								<input type="hidden" name="secPreAuthModeId" value='${charge.preAuthModeIds[1]}' >
							</td>
						</c:if>
					</c:if>
					
				</tr>
			</c:forEach>
			</table>
		</div>
		
		<fieldset class="fieldSetBorder">
	  	<legend class="fieldSetLabel"><insta:ltext key="billing.patientbill.details.totals"/></legend>
		<table align="right" class="infotable">
			<tr style="display:none" id ="filterRow">
				<td class="formlabel">&nbsp;</td>
				<td class="forminfo">&nbsp;</td>
				
				<c:if test="${availableDeposits > 0 || hasRewardPointsEligibility}">
					<td class="formlabel">&nbsp;</td>
					<td class="forminfo">&nbsp;</td>
				</c:if>
	
				<c:if test="${isInsuranceBill}">
					<td class="formlabel">&nbsp;</td>
					<td class="forminfo">&nbsp;</td>
				</c:if>
	
				<td class="formlabel"><insta:ltext key="billing.patientbill.details.filteredamount"/>:</td>
				<td class="forminfo">
					<label id="lblFilteredAmount">0.00</label>
				</td>
	   		</tr>
			
			<tr>
				<td class="formlabel"><insta:ltext key="billing.patientbill.details.billedamount"/>:</td>
				<td class="forminfo">
					<label id="lblTotBilled">${bill.totalAmount}</label>
				</td>
		 	</tr>
	 		<tr>
				<c:choose>
					<c:when test="${isInsuranceBill || isOtherHospitalSponsorBill}">
						<td class="formlabel"><insta:ltext key="billing.patientbill.details.sponsorcreditamount"/>:</td>
						<td class="forminfo">
							<label id="lblTotInsAmt">${bill.totalClaim}</label>
						</td>
					</c:when>
					<c:otherwise>
						<td class="formlabel"><insta:ltext key="billing.patientbill.details.patientcreditamount"/>:</td>
						<td class="forminfo">
							<label id="lblPatientAmount">${bill.totalAmount - bill.totalClaim}</label>
						</td>
					</c:otherwise>
				</c:choose>
			</tr>
		</table>
		</fieldset>
		
		<%-- Determine what are the available templates to use for printing. --%>
		<c:choose>
			<c:when test="${bill.billType =='C'}">
				<c:set var="billPrintDefault" value="${genPrefs.billLaterPrintDefault}"/>
			</c:when>
			<c:otherwise>
				<c:set var="billPrintDefault" value="${genPrefs.billNowPrintDefault}"/>
			</c:otherwise>
		</c:choose>
		
		<c:if test="${not empty bill}">
			<table  id="txx">
				<tr>
					<td align="left" style="width:520px"></td>
					<td align="right">
							<c:if test="${bill.billType != 'M' && bill.billType != 'R' && (billPrintRights == 'A' || roleId == 1 || roleId == 2)}">
					 			<%-- We need a print button along with the print type and printer --%>
								<select name="printBill" id="printSelect" class="dropdown" onchange="loadTemplates(this)">
									<c:forEach var="template" items="${availableTemplates}">
										<option value="${fn:escapeXml(template.map.template_id)}"
										${(template.map.template_id == billPrintDefault) ? 'selected' : ''}>
											<c:out value="${template.map.template_name}"/>
										</option>
									</c:forEach>
								</select>
	
								<c:if test="${bill.billType == 'C' || isInsuranceBill}">
									<insta:selectdb name="printType" table="printer_definition"
										valuecol="printer_id"  displaycol="printer_definition_name" orderby="printer_definition_name"
										value="${genPrefs.default_printer_for_bill_later}"/>
								</c:if>
								<c:if test="${bill.billType == 'P' && !isInsuranceBill}">
									<insta:selectdb name="printType" table="printer_definition"
										valuecol="printer_id"  displaycol="printer_definition_name" orderby="printer_definition_name"
										value="${genPrefs.default_printer_for_bill_now}"/>
								</c:if>
								<c:if test="${not disablePrint}">
									<button type="button" id="printButton" accessKey="P"
										onclick="return billPrint('${billPrintDefault}','${genPrefs.userNameInBillPrint}')">
										<b><insta:ltext key="billing.patientbill.details.p"/></b><insta:ltext key="billing.patientbill.details.rint"/></button>
										<button type="button" id="downloadButton" accessKey="D" onclick="submitForm();" disabled>
										<b><insta:ltext key="billing.patientbill.details.d"/></b><insta:ltext key="billing.patientbill.details.ownload"/></button>
								</c:if>
							</c:if>
					</td>
				</tr>
			</table>
		</c:if>
	</form>
	
	<form name="billCSVdownloadForm" action="BillAction.do">
		<input type="hidden" name="billNo" value=""/>
		<input type="hidden" name="_method" value="downloadCSVFile"/>
		<input type="hidden" name="template_id" value=""/>
		<input type="hidden" name="printerId" value=""/>
	</form>
	
	
	<script>
		var multiPlanExists = ${not empty multiPlanExists ? multiPlanExists : false};
		var points_redemption_rate = ${empty points_redemption_rate ? 0 : points_redemption_rate};
	</script>

</body>
</html>
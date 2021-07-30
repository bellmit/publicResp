<%@ taglib uri="/WEB-INF/struts-html.tld"  prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld"  prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld"  prefix="logic" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<c:set var="bill" value="${billDetails.bill}"/>
<c:set var="points_redemption_rate" value="${not empty genPrefs.points_redemption_rate ? genPrefs.points_redemption_rate : 0}"/>
<c:set var="consolidated_credit_note" value="${consolidated_credit_note}"/>
<c:set var="current_month_bill" value="${current_month_bill}"/>
<c:set var="existingReceipts" value="${bill.totalReceipts}"/>
<c:set var="existingRecdAmount" value="${bill.claimRecdAmount}"/>
<c:set var="totalAmount" value="${bill.totalAmount}" />
<c:set var="existingSponsorReceipts" value="${bill.totalPrimarySponsorReceipts + bill.totalSecondarySponsorReceipts}"/>
<c:set var="availableDeposits" value='${depositDetails.map.total_deposits -	depositDetails.map.total_set_offs + depositDetails.map.deposit_set_off}'/>
<c:set var="ipDeposits" value='${ipDepositDetails.map.total_ip_deposits - ipDepositDetails.map.total_ip_set_offs + depositDetails.map.ip_deposit_set_off}'/>
<c:set var="availableRewardPoints"
	value='${rewardPointDetails.map.total_points_earned - rewardPointDetails.map.total_points_redeemed - rewardPointDetails.map.total_open_points_redeemed + rewardPointDetails.map.points_redeemed}'/>
<c:set var="availableRewardPointsAmount" value="${availableRewardPoints * points_redemption_rate}"/>
<c:set var="userIsallowedtoEditOpenDate" value="${actionRightsMap.allow_edit_bill_open_date == 'A' || roleId == 1 || roleId ==2}"/>

<c:set var="dischargeType">
	<c:choose>
		<c:when test="${bill.billType == 'P'}">none</c:when>
		<c:when test="${bill.visitType == 'i' && preferences.modulesActivatedMap['mod_adt'] eq 'Y'}">adt</c:when>
		<c:when test="${bill.isPrimaryBill == 'N'}">none</c:when>
		<c:otherwise>bill</c:otherwise>
	</c:choose>
</c:set>
<c:set var="billPrintRights" value="${urlRightsMap.bill_print}"/>

<jsp:useBean id="statusDisplay" class="java.util.HashMap"/>
<c:set target="${statusDisplay}" property="A" value="Open"/>
<c:set target="${statusDisplay}" property="F" value="Finalized"/>
<c:set target="${statusDisplay}" property="C" value="Closed"/>
<c:set target="${statusDisplay}" property="X" value="Cancelled"/>

<jsp:useBean id="paymentStatusDisplay" class="java.util.HashMap"/>
<c:set target="${paymentStatusDisplay}" property="U" value="Unpaid"/>
<c:set target="${paymentStatusDisplay}" property="P" value="Paid"/>

<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<jsp:useBean id="currentDate" class="java.util.Date"/>

<%@page import="flexjson.JSONSerializer"%>
<html>
<head>
	<title><insta:ltext key="billing.patientbill.details.billtitle"/> ${ifn:cleanHtml(bill.billNo)} - <insta:ltext key="billing.patientbill.details.instahms"/></title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<meta name="i18nSupport" content="true"/>

	<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
	<c:set var="bill_cancellation_requires_approval" value='<%=GenericPreferencesDAO.getAllPrefs().get("bill_cancellation_requires_approval")%>'/>

	<insta:link type="script" file="ajax.js"/>
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="script" file="consolidatedVisitBilling.js"/>
	<insta:link type="script" file="billingDynaPkg.js"/>
	<insta:link type="script" file="billingPerdiem.js"/>
	<insta:link type="script" file="billPaymentCommon.js"/>
	<insta:link type="script" file="Insurance/insuranceCalculation.js"/>
	<insta:link type="script" file="widgets.js"/>
	<insta:link type="script" file="orderdialog.js" />
	<insta:link type="script" file="ordertable.js" />
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="dashboardsearch.js" />
	<insta:link type="js" file="doctorConsultations.js" />
	<insta:link type="js" file="billing/billing_discount_rule.js" />

	<%-- send directBilling as empty if we have rights for billing anything, otherwise, send it as Y
	so that we only get items which are set to directBilling=Y --%>
	<c:set var="directBilling"
		value="${(actionRightsMap.addtobill_charges != 'N' || roleId == 1 || roleId ==2) ? '' : 'Y'}"/>

	<c:set var="isInsuranceBill" value="${bill.is_tpa && (bill.restrictionType == 'N' || bill.restrictionType == 'P')}"/>
	<c:set var="isNonInsuredBillOfInsPatient" value="${patient.primary_sponsor_id != null && patient.primary_sponsor_id != '' && patient.org_id != bill.billRatePlanId }" />

	<c:set var="version"><bean:message key='insta.software.version'/></c:set>

	<c:set var="orderItemsUrl" value="${cpath}/master/orderItems.do?method=getOrderableItems"/>
	<c:set var="orderItemsUrl" value="${orderItemsUrl}&${version}&${sesHospitalId}&mts=${masterTimeStamp}"/>
	<c:set var="orderItemsUrl" value="${orderItemsUrl}&filter=&orderable="/>
	<c:set var="orderItemsUrl" value="${orderItemsUrl}&directBilling=${directBilling}&operationOrderApplicable=${genPrefs.operationApplicableFor}"/>
	<c:set var="orderItemsUrl" value="${orderItemsUrl}&orgId=${patient.org_id}&visitType=${patient.visit_type}"/>
	<c:set var="orderItemsUrl" value="${orderItemsUrl}&bedType=${patient.bill_bed_type}"/>
	<c:set var="orderItemsUrl" value="${orderItemsUrl}&center_id=${patient.center_id}&tpa_id=${patient.primary_sponsor_id}"/>
	<script src="${orderItemsUrl}"></script>

	<style type="text/css">
		table.infotable td.forminfo { width: 40px; text-align: right }
		table.infotable td.formlabel { width: 110px; text-align: right }
		select.filterActive { color: blue; }
		.scrollForContainer .yui-ac-content{
			 overflow:auto;overflow-x:auto;width:400px;  /* scrolling */
		}
		select.billLabel {color: red;}
	</style>
	<script type="text/javascript">
		<%-- global variables relating to this bill --%>
		var showChargesAllRatePlan = 'A'; // hardcoded to 'A', because, in bill screen, item amount is a must see column
		var origBillStatus = '${ifn:cleanJavaScript(bill.status)}';
		var billType = '${ifn:cleanJavaScript(bill.billType)}';
		var visitType = '${ifn:cleanJavaScript(bill.visitType)}';
		var isPrimaryBill = '${ifn:cleanJavaScript(bill.isPrimaryBill)}';
		var billRatePlanId = '${ifn:cleanJavaScript(bill.billRatePlanId)}';
		var points_redemption_rate = ${empty points_redemption_rate ? 0 : points_redemption_rate};
		var existingReceipts = ${empty existingReceipts ? 0 : existingReceipts};
		var existingRecdAmount = ${empty existingRecdAmount ? 0 : existingRecdAmount};
		var totalBilledAmount = ${empty totalAmount ? 0 : totalAmount};
		var existingSponsorReceipts = ${empty existingSponsorReceipts ? 0 : existingSponsorReceipts};
		var availableDeposits = ${empty availableDeposits ? 0 : availableDeposits};
		var ipDeposits = ${empty ipDeposits ? 0 : ipDeposits};
		var availableRewardPoints = ${empty availableRewardPoints ? 0 : availableRewardPoints};
		var availableRewardPointsAmount = ${empty availableRewardPointsAmount ? 0 : availableRewardPointsAmount};
		var billRewardPoints = ${empty bill ? 0 : bill.rewardPointsRedeemed};
		var openDate = '<fmt:formatDate value="${bill.openDate}" pattern="dd-MM-yyyy"/>';
		var openTime = '<fmt:formatDate value="${bill.openDate}" pattern="HH:mm"/>';
		var finalizedDate = '<fmt:formatDate value="${bill.finalizedDate}" pattern="dd-MM-yyyy"/>';
		var finalizedTime = '<fmt:formatDate value="${bill.finalizedDate}" pattern="HH:mm"/>';
		var curDate = '<fmt:formatDate pattern="dd-MM-yyyy" value="${currentDate}"/>';
		var curTime = '<fmt:formatDate pattern="HH:mm" value="${currentDate}"/>';
		var gServerNow = new Date(<%= (new java.util.Date()).getTime() %>);
		var gPrintReceiptPrinterType = '${ifn:cleanJavaScript(param.printerType)}';
		var roleId = '${ifn:cleanJavaScript(roleId)}';
		var addToBillRights = '${actionRightsMap.addtobill_charges}';
		var allowDiscount = '${actionRightsMap.allow_discount}';
		var allowRateChange = '${actionRightsMap.allow_ratechange}';
		var allowRateIncr = '${actionRightsMap.allow_rateincrease}';
		var allowRateDcr = '${actionRightsMap.allow_ratedecrease}';
		var allowDynaPkgIncExc = '${actionRightsMap.allow_dyna_package_include_exclude}';
		var allowBackDate = '${actionRightsMap.allow_backdate}';
		var allowBackDateBillActivities = "${roleId == 1 || roleId == 2 ? 'A' :actionRightsMap.allow_back_date_bill_activities}";
		var editBillRights = '${actionRightsMap.edit_bill}';
		var cancelBillRights = '${actionRightsMap.cancel_bill}';
		var cancelElementsInBillRights = '${actionRightsMap.cancel_elements_in_bill}';
		var allowRefundRights = '${actionRightsMap.allow_refund}';
		var customTemplate = '${ifn:cleanJavaScript(param.customTemplate)}';
		var userNameInBillPrint = '${genPrefs.userNameInBillPrint}';
		var claimServiceTaxPer = '${genPrefs.serviceTaxOnClaimAmount}';
		var patientDept = '${patient.dept_id}';
		var patientOrgId = '${patient.org_id}';
		var billOrgId  = '${bill.billRatePlanId}';
		var patientCategoryId = '${patient.patient_category_id}';
		var patientBedType = <insta:jsString value="${patient.bill_bed_type}"/>;
		var otherUnpaidBills = <%= request.getAttribute("otherUnpaidBillsJSON") %> ;
		var dischargeStatus = '${patient.discharge_flag}';
		var dischargeDate = '${patient.discharge_date}';
		var dischargeType = '${dischargeType}';
		var prescribingDocName = '';
		var prescribingDoctor = '';
		var fixedOtCharges = '${genPrefs.fixedOtCharges}';
		gPrescDocRequired = '${genPrefs.prescribingDoctorRequired}';
		var gOnePrescDocForOP = '${genPrefs.op_one_presc_doc}';
		<c:if test="${genPrefs.default_prescribe_doctor == 'Y'}">
			prescribingDocName = <insta:jsString value="${patient.doctor_name}"/>;
			prescribingDoctor = '${patient.doctor}';
		</c:if>
		var consultingDocName = <insta:jsString value="${patient.doctor_name}"/>;
		var patientType = '${patient.visit_type}';
		var regDate = '<fmt:formatDate value="${patient.reg_date}" pattern="dd-MM-yyyy"/>';
		var regTime = '<fmt:formatDate value="${patient.reg_time}" pattern="HH:mm"/>';
		var adtmodule = '${preferences.modulesActivatedMap['mod_adt']}';
		var mod_adv_packages = '${preferences.modulesActivatedMap['mod_adv_packages']}';
		var sponsorBillorReceipt = '${ifn:cleanJavaScript(sponsor_bill_receipt)}';
		var sponsorBillNo = '${ifn:cleanJavaScript(bill.sponsorBillNo)}';
		var restrictionType = '${ifn:cleanJavaScript(bill.restrictionType)}';
		var pendingEquipmentFinalization = '${pendingEquipmentFinalization}';
		var pendingBedFinalization = '${pendingBedFinalization}';
		mealTimingsRequired = true;
		equipTimingsRequired = true;
		var forceSubGroupSelection = '${genPrefs.forceSubGroupSelection}';
		var serviceGroupsJSON = <%= request.getAttribute("serviceGroupsJSON") %> ;
		var servicesSubGroupsJSON = <%= request.getAttribute("servicesSubGroupsJSON") %> ;
		var doctorConsultationTypes = <%= request.getAttribute("doctorConsultationTypes") %> ;
		var allDoctorConsultationTypes = <%= request.getAttribute("allDoctorConsultationTypes") %> ;
		var allServiceSubgroupsList = <%= request.getAttribute("allServiceSubgroupsList") %> ;
		var regPref = <%= request.getAttribute("regPrefJSON") %> ;
		var doctorsList =  <%= request.getAttribute("doctorsJSON") %> ;
		var billPrintDefault = (billType == 'C') ? '${genPrefs.billLaterPrintDefault}' : '${genPrefs.billNowPrintDefault}';

		var serviceChargePer = '${genPrefs.serviceChargePercent}';
		var dynaPkgProcessedBefore = '${dynaPkgProcessedBefore}';
		var dynaPackageProcessed = '${ifn:cleanJavaScript(bill.dynaPkgProcessed)}';
		var existingDynaPackageId = '${bill.dynaPkgId}';
		var allowBillInsurance = '${allowBillInsurance}';
		var isTpa       = ${empty bill ? false : bill.is_tpa};
		var billNowTpa  = billType == 'P' && isTpa && (restrictionType == 'N' || restrictionType == 'P');
		var tpaBill     = isTpa && (restrictionType == 'N' || restrictionType == 'P');
		var visitPrimaryApprovalAmount = 0;
		var visitSecondaryApprovalAmount = 0;
		var visitApprovalAmount = 0;
		var otherBillsApprovalTotal = 0;
		var planId = ${empty patient.plan_id ? 0 : patient.plan_id};
		<c:if test="${isInsuranceBill}">
			visitPrimaryApprovalAmount = ${empty patient.primary_insurance_approval ? 0 : patient.primary_insurance_approval};
			visitSecondaryApprovalAmount = ${empty patient.secondary_insurance_approval ? 0 : patient.secondary_insurance_approval};
			visitApprovalAmount = visitPrimaryApprovalAmount + visitSecondaryApprovalAmount;
			otherBillsApprovalTotal = ${empty bill ? 0 : billsApprovalTotal - bill.primaryApprovalAmount - bill.secondaryApprovalAmount};
		</c:if>
		var allOrdersJSON = null;

		var planBean = <%= request.getAttribute("planBeanJSON") %> ;
		var drgBean = <%= request.getAttribute("drgBeanJSON") %> ;

		var priSponsorType = '${patient.sponsor_type}';
		var secSponsorType = '${patient.sec_sponsor_type}';
		var priSponsorId = '${patient.primary_sponsor_id}';
		var secSponsorId = '${patient.secondary_sponsor_id}';
		var priTpaName = <insta:jsString value="${patient.tpa_name}"/>;
		var secTpaName = <insta:jsString value="${patient.sec_tpa_name}"/>;
		var opType = '${patient.op_type}';
		var visitTpaBills = '${visitTpaBills}';
		var firstTpaBill = '${ifn:cleanJavaScript(firstTpaBill.billNo)}';

		var billLabelMasterJson = <%= new JSONSerializer().serialize(request.getAttribute("billLabelMasterMap"))%>;
		var billLabelId = '${bill.billLabelId}';
		var billRemarks = <insta:jsString value="${bill.billRemarks}"/>;
		var multiVisitBill = '${multiVisitBill}';
		var perdiemPlanBean = <%= request.getAttribute("perdiemPlanBeanJSON") %> ;

		var priPlanExists = '${bill.primaryPlanId != null &&  bill.primaryPlanId != '' }';
		var secPlanExists = '${bill.secondaryPlanId != null && bill.secondaryPlanId != ''}';

		var patientWriteOff = '${ifn:cleanJavaScript(bill.patientWriteOff)}';
		var sponsorWriteOff = '${ifn:cleanJavaScript(bill.sponsorWriteOff)}';
		var billCancelRequiresApproval = '${bill_cancellation_requires_approval}';
		var cancellationApprovalStatus = '${ifn:cleanJavaScript(bill.cancellationApprovalStatus)}';
		var screenid = '';
		var ipDepositExists = '${ipDepositExists}';
		var sponserType=  '<%= request.getAttribute("sponserType") %>' ;
		var discountPlansJSON = ${discountPlansJSON};
		var isCustomFieldsExist = '${isCustomFieldsExist}';
		var is_Baby = '${is_Baby}';
		var visitAdjExists = '${visitAdjExists}';
		var visitDiscountPlanId = ${visitDiscountPlanId};

	</script>
	<insta:js-bundle prefix="billing.salucro"/>
	<insta:js-bundle prefix="billing.billlist"/>
	<insta:js-bundle prefix="billing.dynapackage"/>
	<insta:js-bundle prefix="billing.billingprediem"/>
	<insta:js-bundle prefix="laboratory.radiology.billpaymentcommon"/>
	<insta:js-bundle prefix="order.common"/>
	<insta:js-bundle prefix="registration.patient"/>
</head>

<body onload="init(),ajaxForPrintUrls();" class="yui-skin-sam">
<c:set var="primaryclaimstatus">
<insta:ltext key="billing.patientbill.details.open"/>,
<insta:ltext key="billing.patientbill.details.sent"/>,
<insta:ltext key="billing.patientbill.details.closed"/>
</c:set>
<c:set var="paymentstatus">
<insta:ltext key="billing.patientbill.details.unpaid"/>,
<insta:ltext key="billing.patientbill.details.paid"/>
</c:set>
<c:set var="filterpackage">
<insta:ltext key="billing.patientbill.details.included"/>,
<insta:ltext key="billing.patientbill.details.excluded"/>
</c:set>
<c:set var="editbillremittance">
<insta:ltext key="billing.patientbill.details.editbillremittance"/>
</c:set>
<c:set var="editpharmacyitemamount">
<insta:ltext key="billing.patientbill.details.editpharmacyitemamount"/>
</c:set>
<c:set var="editclaimtitle">
<insta:ltext key="billing.patientbill.details.editclaim"/>
</c:set>
<c:set var="editpatientinsurancetitle">
<insta:ltext key="billing.patientbill.details.editpatientinsurance"/>
</c:set>
<c:set var="addpatientinsurancetitle">
<insta:ltext key="billing.patientbill.details.addpatientinsurance"/>
</c:set>
<c:set var="changeprimarytitle">
<insta:ltext key="billing.patientbill.details.changeprimary"/>
</c:set>
<c:set var="changerateplantype">
<insta:ltext key="billing.patientbill.details.changerateplan.bedtype"/>
</c:set>
<c:set var="tobillater">
<insta:ltext key="billing.patientbill.details.tobilllater"/>
</c:set>
<c:set var="disconnectins">
<insta:ltext key="billing.patientbill.details.disconnectinsurance"/>
</c:set>
<c:set var="connectins">
<insta:ltext key="billing.patientbill.details.connectinsurance"/>
</c:set>
<c:set var="priclaimamt">
<insta:ltext key="billing.patientbill.details.prisponsor.or.claimamount"/>
</c:set>
<c:set var="secclaimamt">
<insta:ltext key="billing.patientbill.details.secsponsor.or.claimamount"/>
</c:set>
<c:set var="sponserclaimamt">
<insta:ltext key="billing.patientbill.details.sponsor.or.claimamount"/>
</c:set>
<c:set var="patientamt">
<insta:ltext key="billing.patientbill.details.patientamount"/>
</c:set>
<c:set var="code">
<insta:ltext key="billing.editpharmacyitemamounts.items.code"/>
</c:set>
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
<c:set var="addbutton">
<insta:ltext key="billing.patientbill.details.add"/>
</c:set>
<c:set var="applybutton">
<insta:ltext key="billing.patientbill.details.apply"/>
</c:set>
<c:set var="dummyvalue">
	<insta:ltext key="selectdb.dummy.value"/>
</c:set>
<table width="100%">
		<tr>
			<td width="100%">
				<c:choose>
					<c:when test="${(consolidated_cn_label)}">
						<h1><insta:ltext key="js.billing.billlist.creditnote"/></h1>
					</c:when>
					<c:otherwise>
						<h1><insta:ltext key="billing.patientbill.details.patientbill"/></h1>
					</c:otherwise>
				</c:choose>
			</td>
		</tr>
</table>
<div><insta:feedback-panel/></div>

<c:if test="${bill.visitType == 'r'}">
	<fieldset class="fieldSetBorder">
		<legend class="fieldSetLabel"><insta:ltext key="billing.patientbill.details.customerdetails"/></legend>
		<table class="formtable" cellpadding="0" cellspacing="0" width="100%" >
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
		<table class="formtable" cellpadding="0" cellspacing="0" width="100%" >
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

<form name="mainform" method="post" action="BillAction.do" autocomplete="off">
<input type="hidden" name="_method" value="saveBillDetails">
<input type="hidden" name="billNo" value="${ifn:cleanHtmlAttribute(bill.billNo)}">
<input type="hidden" name="buttonAction" value="save">
<input type="hidden" name="billDiscountAuth" id="billDiscountAuth" value="${bill.billDiscountAuth}"/>
<input type="hidden" name="billDiscountCategory" id="billDiscountCategory" value="${bill.billDiscountCategory}"/>
<input type="hidden" name="billOpenedDate" value="<fmt:formatDate value="${bill.openDate}" pattern="dd-MM-yyyy"/>">
<input type="hidden" name="billOpenedTime" value="<fmt:formatDate value="${bill.openDate}" pattern="HH:mm"/>">
<input type="hidden" name="modTime" value="${bill.modTime.getTime()}"/>
<input type="hidden" name="paymentForceClose" value="N"/>
<input type="hidden" name="claimForceClose" value="N"/>
<input type="hidden" name="secondarySponsorExists" value="${not empty patient.secondary_sponsor_id ? 'Y' :'N'}"/>
<input type="hidden" name="mrNo" value="${patient.mr_no}">
<input type="hidden" name="mrno" id="mrno" value="${patient.mr_no}">
<c:choose>
	<c:when test="${bill.visitType == 't'}">
		<input type="hidden" name="visitId" id="visitId" value="${incomingCustomer.map.incoming_visit_id}">
	</c:when>
	<c:otherwise>
		<input type="hidden" name="visitId" id="visitId" value="${patient.patient_id}">
	</c:otherwise>
</c:choose>

<input type="hidden" name="organizationId" id="organizationId" value="${patient.org_id}">
<input type="hidden" name="bedType" id="bedType" value="${patient.bill_bed_type}">
<input type="hidden" name="doctorId" id="doctorId" value ="${patient.doctor}"/>
<input type="hidden" name="referralDocId" id="referralDocId" value ="${patient.reference_docto_id}"/>
<input type="hidden" name="originalDepositSetOff" id="originalDepositSetOff" value='${depositDetails.map.deposit_set_off}'/>
<input type="hidden" name="originalPointsRedeemed" id="originalPointsRedeemed" value='${rewardPointDetails.map.points_redeemed}'/>
<input type="hidden" name="existing_per_diem_code" id="existing_per_diem_code" value ="${patient.per_diem_code}"/>
<input type="hidden" name="billStatus" id="billStatus" value="${ifn:cleanHtmlAttribute(bill.status)}" />
<input type="hidden" name="paymentStatus" value="${ifn:cleanHtmlAttribute(bill.paymentStatus)}" />
<input type="hidden" name="opendate" value="<fmt:formatDate value="${bill.openDate}" pattern="dd-MM-yyyy"/>">
<input type="hidden" name="opentime" value="<fmt:formatDate value="${bill.openDate}" pattern="HH:mm"/>">
<input type="hidden" name="screenId" value="view_consolidated_bill">
<%-- bill type display --%>

<c:set var="isOtherHospitalSponsorBill"
	value="${not empty bill.sponsorBillNo && bill.billType eq 'C' && bill.visitType == 't' && bill.restrictionType == 'N'}"/>

<c:set var="extraColumns" value="${isInsuranceBill ? 2 : 0}"/>
<c:if test="${((actionRightsMap.cancel_elements_in_bill == 'A')||(roleId==1)||(roleId==2))}">
	<c:set var="extraColumns" value="${extraColumns + 1}"/>
</c:if>


<c:set var="isPhBillNowReturns" value="${bill.billType == 'P' && bill.restrictionType == 'P' && bill.totalAmount < 0}"/>
<c:set var="hasRewardPointsEligibility" value="${(!isInsuranceBill && (availableRewardPoints > 0 || (not empty rewardPointDetails.map.points_redeemed && rewardPointDetails.map.points_redeemed != 0)) && !isPhBillNowReturns)}" />

<c:set var="remarks" value=""/>

<table width="100%">
<tr>
	<td width="80%">
		<table class="formtable" cellpadding="0" cellspacing="0" width="100%" border="0">
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

				<td class="formlabel"><insta:ltext key="billing.patientbill.details.hide"/>&nbsp;<insta:ltext key="billing.patientbill.details.cancelled"/>:</td>
				<td class="forminfo">
					<input type="checkbox" name="showCancelled" onclick="onChangeFilter();"/>
				</td>
			</tr>
		</table>
	</td>
	
</tr>
</table>
<div class="resultList" style="margin: 10px 0px 5px 0px;">
	<table class="detailList dialog_displayColumns" cellspacing="0" cellpadding="0" id="chargesTable" border="0" width="100%">
		<tr bgcolor="#8FBC8F" id="chRow0">
			<th><input type="checkbox" onclick="selectAllForDiscounts()" id="discountAll"/></th>
			<th style="width: 80px"><insta:ltext key="billing.patientbill.details.date"/></th>
			<th style="width: 30px"><insta:ltext key="billing.patientbill.details.ord"/>#</th>
			<th><insta:ltext key="billing.patientbill.details.head"/></th>
			<th style="width: 60px" title="${ifn:cleanHtmlAttribute(code)}"><insta:ltext key="billing.patientbill.details.code"/></th>
			<th style="width: 130px"><insta:ltext key="billing.patientbill.details.description"/></th>
			<th><insta:ltext key="billing.patientbill.details.details"/></th>
			<th class="number"><insta:ltext key="billing.patientbill.details.rate"/></th>
			<th class="number"><insta:ltext key="billing.patientbill.details.qty"/></th>
			<th class="number" style="width: 10px"></th>
			<th class="number"><insta:ltext key="billing.patientbill.details.disc"/></th>
			<th class="number"><insta:ltext key="billing.patientbill.details.amt"/></th>
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
			<c:if test="${((actionRightsMap.cancel_elements_in_bill == 'A')||(roleId==1)||(roleId==2))}">
				<th style="width:24px"></th>		<%-- trash icon --%>
			</c:if>
			<th style="width:24px"></th>		<%-- edit icon --%>
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
				<td><input type="checkbox" name="discountCheck"/></td>
				<td>
					<img src="${cpath}/images/${flagColor}_flag.gif"/>
					<label>
						<fmt:formatDate value="${charge.postedDate}" pattern="dd-MM-yyyy"/>
					</label>
					<input type="hidden" name="packageId" value="" />
					<input type="hidden" name="postedDate"
						value="<fmt:formatDate value="${charge.postedDate}" pattern="dd-MM-yyyy"/>" />
					<input type="hidden" name="postedTime"
						value="<fmt:formatDate value="${charge.postedDate}" pattern="HH:mm"/>" />
				</td>
				<td><label>${charge.orderNumber}</label>
					<input type="hidden" name="orderNumber" value='${charge.orderNumber}'>
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
                    <input type="hidden" name="dynaPackageExcluded" value="${charge.dynaPackageExcluded}"/>
				</td>

				<td>
					<label>${charge.actRatePlanItemCode}</label>
					<input type="hidden" name="actRatePlanItemCode" value='${charge.actRatePlanItemCode}'>
					<input type="hidden" name="codeType" value='${charge.codeType}'>
				</td>

				<td>
					<div style="width:130px; white-space:nowrap; overflow:hidden; text-overflow:ellipsis;" title="${charge.actDescription}">
						${charge.actDescription}
					</div>
					<input type="hidden" name="description"
						value="${fn:escapeXml(charge.actDescription)}" >
					<input type="hidden" name="descriptionId"
						value="${charge.actDescriptionId}" >
				</td>

				<td>
					<c:set var="remarks" value=""/>
					<insta:truncLabel value="${charge.actRemarks}" length="16"/>
				</td>

				<td class="number">
					<label>${charge.actRate}</label>
					<input type="hidden" name="rate" value="${charge.actRate}"/>
					<input type="hidden" name="originalRate" value='${charge.originalRate}' />
					<input type="hidden" name="savedRate" value="${charge.actRate}"/>
				</td>

				<td class="number">
					<label>${charge.actQuantity}</label>
					<input type="hidden" name="qty" value="${charge.actQuantity}"/>
					<input type="hidden" name="returnQty" value="${charge.returnQty}"/>
					<input type="hidden" name="qty_included" value="${charge.qty_included}"/>
				</td>

				<td>
					<label>${charge.actUnit}</label>
					<input type="hidden" name="units" value="${charge.actUnit}"/>
				</td>

				<!-- discounts title hidden field value are setted here -->
				<td class="number">
					<label>${charge.discount}</label>
					<input type="hidden" name="disc" value="${charge.discount}"/>
					<input type="hidden" name="oldDisc" value="${charge.discount}"/>
					<input type="hidden" name="overall_discount_auth_name"
						value="${charge.overall_discount_auth_name}"/>
					<input type="hidden" name="old_overall_discount_auth"
						value="${charge.overall_discount_auth}"/>
					<input type="hidden" name="overall_discount_auth"
						value="${charge.overall_discount_auth}"/>
                    <input type="hidden" name="isSystemDiscount"
                           value="${charge.isSystemDiscount}"/>
                    <input type="hidden" name="overall_discount_amt"
						value="${charge.overall_discount_amt}"/>
			       <input type="hidden" name="discount_auth_dr_name"
						value="${charge.discount_auth_dr_name}"/>
					<input type="hidden" name="discount_auth_dr"
						value="${charge.discount_auth_dr}"/>
					<input type="hidden" name="dr_discount_amt"
						value="${charge.dr_discount_amt}"/>

					<input type="hidden" name="discount_auth_pres_dr_name"
						value="${charge.discount_auth_pres_dr_name}"/>
					<input type="hidden" name="discount_auth_pres_dr"
						value="${charge.discount_auth_pres_dr}"/>
					<input type="hidden" name="pres_dr_discount_amt"
						value="${charge.pres_dr_discount_amt}"/>

					<input type="hidden" name="discount_auth_ref_name"
						value="${charge.discount_auth_ref_name}"/>
					<input type="hidden" name="discount_auth_ref"
						value="${charge.discount_auth_ref}"/>
					<input type="hidden" name="ref_discount_amt"
						value="${charge.ref_discount_amt}"/>

					<input type="hidden" name="discount_auth_hosp_name"
						value="${charge.discount_auth_hosp_name}"/>
					<input type="hidden" name="discount_auth_hosp"
						value="${charge.discount_auth_hosp}"/>
					<input type="hidden" name="hosp_discount_amt"
						value="${charge.hosp_discount_amt}"/>
				</td>
				<td class="number">
					<label>${charge.amount}</label>
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
						<input type="hidden" name="priInsClaimTaxAmt" value='0' />
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
							<input type="hidden" name="secInsClaimTaxAmt" value='0' />
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

				<c:if test="${!isInsuranceBill}">
					<input type="hidden" name="returnAmt" value='${charge.returnAmt}' />
				</c:if>

				<c:if test="${((actionRightsMap.cancel_elements_in_bill == 'A')||(roleId==1)||(roleId==2))}">
					<td style="text-align: center">
						<c:if test="${(!consolidated_credit_note)}">
							<c:choose>
								<c:when test="${current_month_bill}">
									<c:choose>
										<c:when test="${charge.status == 'X'}">
											<a href="javascript:Un-Cancel Item" onclick="return cancelCharge(this);" title='<insta:ltext key="billing.patientbill.details.undocancel"/>'>
												<img src="${cpath}/icons/undo_delete.gif" class="imgDelete button"/>
											</a>
										</c:when>
										<c:otherwise>
												<a href="javascript:Cancel Item" onclick="return cancelCharge(this);" title='<insta:ltext key="billing.patientbill.details.cancelitem"/>'>
													<img src="${cpath}/icons/delete.gif" class="imgDelete button"/>
												</a>
										</c:otherwise>
									</c:choose>
								</c:when>
								<c:otherwise>
									<c:if test="${(bill.status == 'A' || bill.status == 'F' )}">
										<c:choose>
											<c:when test="${charge.status == 'X'}">
												<a href="javascript:Un-Cancel Item" onclick="return cancelCharge(this);" title='<insta:ltext key="billing.patientbill.details.undocancel"/>'>
													<img src="${cpath}/icons/undo_delete.gif" class="imgDelete button"/>
												</a>
											</c:when>
											<c:otherwise>
													<a href="javascript:Cancel Item" onclick="return cancelCharge(this);" title='<insta:ltext key="billing.patientbill.details.cancelitem"/>'>
														<img src="${cpath}/icons/delete.gif" class="imgDelete button"/>
													</a>
											</c:otherwise>
										</c:choose>
									</c:if>
								</c:otherwise>
							</c:choose>
						</c:if>
					</td>
				</c:if>

				<td style="text-align: center">
				<c:if test="${(!consolidated_credit_note)}">
					<c:choose>
						<c:when test="${current_month_bill}">
							<c:choose>
								<c:when test="${charge.chargeHead == 'ROF'}">
									<img src="${cpath}/icons/Edit1.png" class="button" />
								</c:when>
								<c:otherwise>
									<a href="javascript:Edit" onclick="return showEditDialog(this,'${charge.chargeHead}','${remarks}');" title='<insta:ltext key="billing.patientbill.details.edititemdetails"/>'>
									<img src="${cpath}/icons/Edit.png" class="button" />
									</a>
								</c:otherwise>
							</c:choose>
						</c:when>
						<c:otherwise>
							<c:if test="${(bill.status == 'A' || bill.status == 'F' )}">
								<c:choose>
									<c:when test="${charge.chargeHead == 'ROF'}">
										<img src="${cpath}/icons/Edit1.png" class="button" />
									</c:when>
									<c:otherwise>
										<a href="javascript:Edit" onclick="return showEditDialog(this,'${charge.chargeHead}','${remarks}');" title='<insta:ltext key="billing.patientbill.details.edititemdetails"/>'>
										<img src="${cpath}/icons/Edit.png" class="button" />
										</a>
									</c:otherwise>
								</c:choose>
							</c:if>
						</c:otherwise>
					</c:choose>
				</c:if>
					
				</td>
	
			</tr>

			<c:if test="${charge.status != 'X'}">
				<c:set var="totalNetAmount" value="${totalNetAmount + charge.amount}"/>
				<c:set var="totalDiscount" value="${totalDiscount + charge.discount}"/>
				<c:if test="${isInsuranceBill}">
					<c:set var="totalClaimAmount" value="${totalClaimAmount + charge.insuranceClaimAmount}"/>
				</c:if>
			</c:if>

			<c:if test="${charge.chargeHead == 'ROF'}">
				<c:set var="rounded" value="Y"/>
			</c:if>

		</c:forEach>

		<c:set var="totalClaimAmount" value="${totalClaimAmount - billDeduction}"/>
		<c:if test="${totalClaimAmount < 0}">
			<c:set var="totalClaimAmount" value="0"/>
		</c:if>
		<c:if test="${isOtherHospitalSponsorBill}">
			<c:set var="totalClaimAmount" value="${totalNetAmount}"/>
		</c:if>

		<%-- one row for apply discounts and add button --%>
		<c:set var="discButtonNeeded" value="${ (bill.status != 'C') && (bill.status != 'X') &&
				((actionRightsMap.allow_discount == 'A')||(roleId==1)||(roleId==2)) }"/>
		<c:set var="discCategoryBtnNeeded" value="${bill.status != 'C' && bill.status != 'X'}"/>
		<c:set var="canChangeDiscountPlan" value="${!bill.is_tpa || visitDiscountPlanId == 0 || bill.billDiscountCategory == 0}"/>
		<c:set var="disableDiscCategory" value="${canChangeDiscountPlan && actionRightsMap.allow_discount_plans_in_bill != 'N' && (bill.status != 'C' && bill.status != 'X' && bill.status != 'F') && (bill.billDiscountAuth == 0 || bill.billDiscountAuth == '')?'':'disabled'}"/>
		<input  type="hidden"  id="disableDiscCategory1" value="${actionRightsMap.allow_discount_plans_in_bill != 'N'}">
	</table>
</div>

<c:set var="width" value="640"/>
<c:if test="${(availableDeposits > 0 || hasRewardPointsEligibility) && isInsuranceBill}">
	<c:set var="width" value="800"/>
</c:if>

<fieldset class="fieldSetBorder">
  <legend class="fieldSetLabel"><insta:ltext key="billing.patientbill.details.totals"/></legend>
	<table width="${width}" align="right" class="infotable">
   		<tr style="display:none" id ="filterRow">

			<td class="formlabel"><insta:ltext key="billing.patientbill.details.filterednetamt"/>:</td>
			<td class="forminfo">
				<label id="lblFilteredNetAmt">0.00</label>
			</td>

			<c:if test="${availableDeposits > 0 || hasRewardPointsEligibility}">

				<td class="formlabel">&nbsp;</td>
				<td class="forminfo">&nbsp;</td>
			</c:if>

			<c:if test="${isInsuranceBill}">
				<td class="formlabel">&nbsp;</td>
				<td class="forminfo">&nbsp;</td>
			</c:if>

			<td class="formlabel"><insta:ltext key="billing.patientbill.details.filtereddiscounts"/>:</td>
			<td class="forminfo">
				<label id="lblFilteredDisc">0.00</label>
			</td>

			<td class="formlabel"><insta:ltext key="billing.patientbill.details.filteredamount"/>:</td>
			<td class="forminfo">
				<label id="lblFilteredAmount">0.00</label>
			</td>
	   	</tr>

		<tr>

			<td class="formlabel"><insta:ltext key="billing.patientbill.details.billedamount"/>:</td>
			<td class="forminfo">
				<label id="lblTotBilled">0.00</label>
			</td>

			<c:if test="${availableDeposits > 0 || hasRewardPointsEligibility}">
				<td class="formlabel">&nbsp;</td>
				<td class="forminfo">&nbsp;</td>
			</c:if>

			<c:if test="${isInsuranceBill}">
				<td class="formlabel">&nbsp;</td>
				<td class="forminfo">&nbsp;</td>
			</c:if>

			<td class="formlabel"><insta:ltext key="billing.patientbill.details.discounts"/>:</td>
			<td class="forminfo">
				<label id="lblTotDisc">0.00</label>
			</td>

			<td class="formlabel"><insta:ltext key="billing.patientbill.details.netamount"/>:</td>
			<td class="forminfo">
				<label id="lblTotAmt">0.00</label>
			</td>
	 	</tr>

 		<tr>

			<td class="formlabel"><insta:ltext key="billing.patientbill.details.patientamount"/>:</td>
			<td class="forminfo">
				<label id="lblPatientAmount">0.00</label>
			</td>
			<!--
			<c:if test="${isInsuranceBill}">
				<td class="formlabel">Patient Deduction:</td>
				<td class="forminfo">
					<label id="lblPatientDeduction">0.00</label>
				</td>
			</c:if>
			 -->
			<c:if test="${isInsuranceBill}">
				<td class="formlabel"></td>
				<td class="forminfo"></td>
			</c:if>

			<td class="formlabel">
				<c:if test="${not empty bill}">
					<a href="ReceiptList.do?_method=getReceipts&bill_no=${ifn:cleanURL(bill.billNo)}&payment_type=R&payment_type=F">
				</c:if>
					<insta:ltext key="billing.patientbill.details.patient"/>&nbsp;<insta:ltext key="billing.patientbill.details.payments"/>:
				<c:if test="${not empty bill}">
					</a>
				</c:if>
			</td>
			<td class="forminfo">
				<label id="lblExistingReceipts">0.00</label>
			</td>

			<c:if test="${availableDeposits > 0 || hasRewardPointsEligibility}">
			<c:choose>
				<c:when test="${availableDeposits > 0 && hasRewardPointsEligibility}">
					<td class="formlabel" style="width:140px;"><insta:ltext key="billing.patientbill.details.deposit"/> + <insta:ltext key="billing.patientbill.details.pointsamt"/>:</td>
					<td class="forminfo" style="width:80px;">
						<label id="lblDepositsSetOff">0.00 </label>&nbsp;+&nbsp;<label id="lblRewardPointsAmt"> 0.00</label>
					</td>
				</c:when>
				<c:when test="${availableDeposits > 0}">
					<td class="formlabel"><insta:ltext key="billing.patientbill.details.depositssetoff"/>:</td>
					<td class="forminfo">
						<label id="lblDepositsSetOff">0.00</label>
					</td>
				</c:when>
				<c:when test="${hasRewardPointsEligibility}">
					<td class="formlabel"><insta:ltext key="billing.patientbill.details.pointsamt"/>:</td>
					<td class="forminfo">
						<label id="lblRewardPointsAmt">0.00</label>
					</td>
					</c:when>
				<c:otherwise>
				</c:otherwise>
			</c:choose>
			</c:if>

			<c:choose>
				<c:when test="${isInsuranceBill}">
					<c:set var="patDueAmt" value="${bill.totalAmount - bill.totalClaim - bill.totalReceipts}"/>
					<c:set var="spoDueAmt" value="${bill.totalClaim - bill.totalPrimarySponsorReceipts - bill.totalSecondarySponsorReceipts}"/>

				</c:when>
				<c:otherwise>
					<c:set var="patDueAmt" value="${bill.totalAmount - bill.totalReceipts}"/>
					<c:set var="spoDueAmt" value="0.00"/>
				</c:otherwise>
			</c:choose>
			<td class="formlabel">
				<c:choose>
					<c:when test="${bill.patientWriteOff == 'M' && (patDueAmt > 0.00 || patDueAmt < 0.00)}">
						<b><span style="color:red">*</span></b><insta:ltext key="billing.patientbill.details.patientdue"/>:
					</c:when>
					<c:otherwise>
						<insta:ltext key="billing.patientbill.details.patientdue"/>:
					</c:otherwise>
				</c:choose>
			</td>
			<td class="forminfo">
				<label id="lblPatientDue">0.00</label>
			</td>
		</tr>

		<c:if test="${isInsuranceBill || isOtherHospitalSponsorBill}">
			<tr>

				<td class="formlabel"><insta:ltext key="billing.patientbill.details.sponsor"/>&nbsp;<insta:ltext key="billing.patientbill.details.amount"/>:</td>
				<td class="forminfo">
					<label id="lblTotInsAmt">0.00</label>
				</td>

				<td class="formlabel"><insta:ltext key="billing.patientbill.details.sponsor"/>&nbsp;<insta:ltext key="billing.patientbill.details.remittances"/>:</td>
				<td class="forminfo">
					<label id="lblSponsorRecdAmount">0.00</label>
				</td>

				<td class="formlabel">
				<a href="ReceiptList.do?_method=getReceipts&bill_no=${ifn:cleanURL(bill.billNo)}&payment_type=S"><insta:ltext key="billing.patientbill.details.sponsor"/>&nbsp;<insta:ltext key="billing.patientbill.details.payments"/>:</a>
				</td>
				<td class="forminfo">
					<label id="lblSponsorReceipts">0.00</label>
				</td>

				<c:if test="${availableDeposits > 0}">
					<td class="formlabel">&nbsp;</td>
					<td class="forminfo">&nbsp;</td>
				</c:if>

				<td class="formlabel">
					<c:choose>
						<c:when test="${bill.sponsorWriteOff == 'M' && spoDueAmt > 0.00}">
							<b><span style="color:red">*</span></b><insta:ltext key="billing.patientbill.details.sponsordue"/>:
						</c:when>
						<c:otherwise>
							<insta:ltext key="billing.patientbill.details.sponsordue"/>:
						</c:otherwise>
					</c:choose>

				</td>
				<td class="forminfo">
					<label id="lblSponsorDue">0.00</label>
				</td>
			</tr>
		</c:if>
		<c:if test="${(bill.patientWriteOff == 'A' &&  (patDueAmt > 0.00 || patDueAmt < 0.00)) || (bill.sponsorWriteOff == 'A' && (spoDueAmt > 0.00 || spoDueAmt < 0.00))}" >
			<tr>
				<c:if test="${bill.patientWriteOff == 'A' &&  (patDueAmt > 0.00 || patDueAmt < 0.00)}">
					<td class="formlabel"><insta:ltext key="billing.patientbill.details.writeOffamt"/>:</td>
					<td class="forminfo">
						<label id="lblWrittenOffAmt">0.00</label>
					</td>
				</c:if>
				<c:if test="${bill.sponsorWriteOff == 'A' && (spoDueAmt > 0.00 || spoDueAmt < 0.00)}">
					<td class="formlabel"><insta:ltext key="billing.patientbill.details.spnrWriteOffAmt"/>:</td>
					<td class="forminfo">
						<label id="lblSpnrWrittenOffAmt">0.00</label>
					</td>
				</c:if>
			</tr>
		</c:if>

		<!-- <c:if test="${bill.patientWriteOff == 'M'}">
			<tr>
				<td class="formlabel"><insta:ltext key="billing.patientbill.details.markedForWriteOff"/>:</td>
				<td class="forminfo">
					<insta:ltext key="billing.patientbill.details.markedForWriteOffYes"/>
				</td>
				<td>
					<img class="imgHelpText" src="${cpath}/images/help.png"
						title="Marked For Patient Writeoff" style="float: left"/>
				</td>
			</tr>
		</c:if> -->
	</table>
</fieldset>


<%-- Determine what are the available templates to use for printing. --%>

<c:if test="${not empty bill}">
	<table cellpadding="0" cellspacing="0"  border="0" width="100%">
		<tr>
			<td align="left">
				<c:if test="${(!consolidated_credit_note)}">
					<c:choose>
						<c:when test="${current_month_bill}">
							<button type="button" id="saveButton" accessKey="S" onclick="return doSave();"><b><u><insta:ltext key="billing.patientbill.details.s"/></u></b><insta:ltext key="billing.patientbill.details.ave"/></button>
						</c:when>
						<c:otherwise>
							<c:if test="${(bill.status == 'A' || bill.status == 'F' )}">
							<button type="button" id="saveButton" accessKey="S" onclick="return doSave();"><b><u><insta:ltext key="billing.patientbill.details.s"/></u></b><insta:ltext key="billing.patientbill.details.ave"/></button>
							</c:if>
						</c:otherwise>
					</c:choose>
				</c:if>
				<insta:screenlink target="_blank" screenId="dialysis_bill_audit_log"
							extraParam="?_method=getAuditLogDetails&bill_no=${bill.billNo}&al_table=bill_audit_view&mr_no=${patient.mr_no}"
								label="Audit Log" addPipe="${(!consolidated_credit_note)}"/>
			</td>
		</tr>
	</table>
</c:if>
	<div id="showAlertRemarksDialog" style="display: none">
		<div class="bd">
		<fieldset class="fieldSetBorder">
			<table width="100%" >
				<tr>
					<td style="width: 17px"><insta:ltext key="billing.patientbill.details.label"/>:</td>
					<td><label id="alertLabelName"></label></td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
				</tr>
				<tr>
					<td style="width: 18px"><insta:ltext key="billing.patientbill.details.remarks"/>:</td>
					<td><label id="alertLabelRemarks"></label></td>
					<td>&nbsp;</td>
				</tr>
			</table>
		</fieldset>
		</div>
	</div>
</form>

<form name="editform">
<input type="hidden" id="editRowId" value=""/>
<input type="hidden" name="eDeductionAmt" id="eDeduction" value=""/>
<input type="hidden" name="eInsCalcReq" id="eInsCalcReq" value=""/>
<div id="editChargeDialog" style="display:none">
	<div class="bd">
		<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel"><insta:ltext key="billing.patientbill.details.edititemchargedetails"/></legend>
			<table class="formtable">
				<tr>
					<td class="formlabel"><insta:ltext key="billing.patientbill.details.chargegroup"/>:</td>
					<td class="forminfo"><label id="eChargeGroup"></label></td>
					<td class="formlabel"><insta:ltext key="billing.patientbill.details.chargehead"/>:</td>
					<td class="forminfo"><label id="eChargeHead"></label></td>
					<td class="formlabel"><insta:ltext key="billing.patientbill.details.description"/>:</td>
					<td class="forminfo" colspan="3"><label id="eDescription"></label></td>
				</tr>

				<tr>
					<td colspan="6">
					<div id="itemDetails" style="margin-bottom:10px;">
						<dl class="accordion">
						  <dt>
					    	<div style="float:left; margin-right: 5px; display:block;" id="itemDetailsImg">
								<img width="16" height="16" src="<%=request.getContextPath()%>/images/arrow_down.png">
							</div>
							<div style="float:left"><insta:ltext key="billing.patientbill.details.otheritemdetails"/></div>
						    <div class="clrboth"></div>
						  </dt>
						  <dd id="itemDetailsDD">
							<table class="formtable" width="100px">
								<tr>
							    	<td class="formlabel"><insta:ltext key="billing.patientbill.details.servicegroup"/>:</td>
							    	<td class="forminfo"><label id="eserviceGroup"></td>
							    	<td class="formlabel"><insta:ltext key="billing.patientbill.details.service"/>&nbsp;<insta:ltext key="billing.patientbill.details.sub"/>&nbsp;<insta:ltext key="billing.patientbill.details.group"/>:</td>
							    	<td class="forminfo"><label id="eserviceSubGroup"></td>
							    	<td class="formlabel"></td>
							    	<td class="forminfo">&nbsp;</td>
							    	<td class="formlabel"></td>
							    	<td class="forminfo">&nbsp;</td>
				    			</tr>
				    			<tr>
				    				<td class="formlabel"><insta:ltext key="billing.patientbill.details.itemdescription"/>:</td>
									<td class="forminfo" colspan="3"><label id="eItemDescription"></label></td>
				    			</tr>
				    			<tr>
									<td class="formlabel"><insta:ltext key="billing.patientbill.details.itemremarks"/>:</td>
									<td colspan="3"><input type="text" style="width:416px" name="eItemRemarks" onchange="setEdited()"></td>
								</tr>
							</table>
						</dd>
						</dl>
					</div>
					</td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="billing.patientbill.details.date"/> & <insta:ltext key="billing.patientbill.details.time"/>:</td>
					<td>
						<fmt:formatDate var="posteddate" pattern="dd-MM-yyyy" value="${currentDate}"/>
						<fmt:formatDate var="postedtime" pattern="HH:mm" value="${currentDate}"/>

						<insta:datewidget name="ePostedDate" value="${posteddate}" valid="past" btnPos="left" onchange="setEdited()"/>
						<input type="text" size="4" name="ePostedTime" value="${postedtime}" class="timefield" onchange="setEdited()"/>
					</td>
					<td class="formlabel"><insta:ltext key="billing.patientbill.details.rateplancode"/>:</td>
					<td>
						<input type="text" name="eCode" maxlength="50" onchange="setEdited()"/>
					</td>
				</tr>

				<tr>
					<td class="formlabel"><insta:ltext key="billing.patientbill.details.details"/>:</td>
					<td colspan="3"><input type="text" style="width:428px" name="eRemarks" onchange="setEdited()"></td>
				</tr>

				<tr>
					<td class="formlabel"><insta:ltext key="billing.patientbill.details.remarks"/>:</td>
					<td colspan="3"><input type="text" style="width:428px" name="eUserRemarks" onchange="setEdited()"></td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="billing.patientbill.details.rate"/>:</td>
					<td>
						<input type="text" name="eRate" onchange="chkRateVariation();recalcEditChargeAmount();setEdited();setInsCalcReq();"/>
					</td>
					<td class="formlabel"><insta:ltext key="billing.patientbill.details.qty"/>:</td>
					<td>
						<input type="text" name="eQty" onchange="recalcEditChargeAmount();setEdited();setInsCalcReq();" readonly="1"/>
					</td>
					<td class="formlabel"><insta:ltext key="billing.patientbill.details.amount"/>:</td>
					<td>
						<input type="text" name="eAmt" readonly="1"/>
					</td>
				</tr>
				<tr>
					<!-- <td class="formlabel"><insta:ltext key="billing.patientbill.details.discountauth"/>:</td>
					<td valign="top">
						<div id="disoverall_wrapper" class="autoComplete">
							<input type="text" name="overallDiscByName" id="overallDiscByName"
								size="21" maxlength="50" onchange="setEdited()"/>
				    		<div id="overallDiscByName_dropdown"></div>
			    		</div>
			    		<input type="hidden" name="overallDiscBy" id="overallDiscBy"/>
					</td>
					<td class="formlabel"><insta:ltext key="billing.patientbill.details.discount"/>:</td>
					<td>
						<input type="text" name="overallDiscRs" id="overallDiscRs"
								onchange="setDiscEdited(this);recalcEditChargeAmount();setEdited();setInsCalcReq();"/>
					</td> -->
					<c:if test="${isInsuranceBill}">
						<td class="formlabel">
							<c:choose>
								<c:when test="${multiPlanExists}">
									<insta:ltext key="billing.patientbill.details.priclaim"/>:
								</c:when>
								<c:otherwise>
									<insta:ltext key="billing.patientbill.details.claim"/>:
								</c:otherwise>
							</c:choose>
						</td>
						<td>
							<input type="text" name="eClaimAmt" onchange="setDeductionAmt('P');setEdited();setInsCalcReq();"/>
							<input type="hidden" name="eClaimLocked"/>
							<input type="hidden" name="ePriIncludeInClaimCalc"/>
						</td>
					</c:if>
				</tr>
				<c:if test="${multiPlanExists}">
					<tr>
						<c:if test="${isInsuranceBill}">
							<td class="formlabel"><insta:ltext key="billing.patientbill.details.secclaim"/>:</td>
							<td>
								<input type="text" name="eSecClaimAmt" onchange="setDeductionAmt('S');setEdited();setInsCalcReq();"/>
								<input type="hidden" name="eSecIncludeInClaimCalc"/>
							</td>
						</c:if>
					</tr>
				</c:if>
			</table>
		</fieldset>
		<table>
			<tr>
				<td><input type="button" onclick="onEditSubmit(false)" value="OK" /></td>
				<td><input type="button" onclick="onEditCancel()" value="Cancel"/></td>
				<td><input type="button" onclick="showNextOrPrevCharge('prev')" name="prevBtn" value="<<Prev"/></td>
				<td><input type="button" onclick="showNextOrPrevCharge('next')" name="nextBtn" value="Next>>"/></td>
			</tr>
		</table>
	</div>
</div>
</form>


<table width="100%"><tr>
	<c:if test="${not empty bill}">
	<c:if test="${(bill.patientWriteOff == 'M' && (patDueAmt > 0.00 || patDueAmt < 0.00) )|| (bill.sponsorWriteOff == 'M' && (spoDueAmt > 0.00 || spoDueAmt < 0.00))}">
		<td width="50%" style="float:left" align="left">
			<div class="legend" style="float:left">
			<div class="flag"><b><span style="color:red">*&nbsp;&nbsp;</span></b></div>
			<div class="flagText">Marked For Write Off</div>
			</div>
		</td>
	</c:if>
	</c:if>
	<td width="50%">
		<div class="legend">
		<div class="flag"><img src='${cpath}/images/red_flag.gif'></div>
		<div class="flagText"><insta:ltext key="billing.patientbill.details.cancelled"/></div>
		<c:if test="${hasRewardPointsEligibility}">
			<div class="flag"><img src='${cpath}/images/green_flag.gif'></div>
			<div class="flagText"><insta:ltext key="billing.patientbill.details.eligibletoredeem"/></div>
		</c:if>
		</div>
	</td>
	</tr>
</table>

<script>
	var jModulesActivated = <%= request.getAttribute("modulesActivatedJSON") %>;
	var jChargeHeads = <%= request.getAttribute("chargeHeadsJSON") %>;
	var jChargeGroups = <%= request.getAttribute("chargeGroupsJSON") %>;
	var pendingtests = '${pendingtest}';
	var jDiscountAuthorizers = <%= request.getAttribute("discountAuthorizersJSON") %>;
	var jdiscountCategories = <%= request.getAttribute("discountCategoriesJSON") %>;
	var jProcedureNameList = <%= request.getAttribute("procedureNameList") %>;
	var jDynaPkgDetailsList = <%= request.getAttribute("dynaPkgDetailsJSON") %>;
	var jDynaPkgNameList = <%= request.getAttribute("dynaPkgNameJSON") %>;
	var jPerdiemCodeDetailsList = <%= request.getAttribute("perdiemCodeDetailsJSON") %>;
	var jPolicyNameList = <%= request.getAttribute("policyCharges") %>;
	var planList = <%= request.getAttribute("planList") %>;
	var jDoctors = doctorsList;
	var jPaymentModes = <%= request.getAttribute("paymentModesJSON") %>;
	var jForeignCurrencyList = <%= request.getAttribute("foreignCurrencyListJSON") %>;
	var currencyRowCount = !empty(jForeignCurrencyList) ? 1 : 0;
	var paymentRowsUncloned = 2;
	var paymentRows = 7 + currencyRowCount;//commisssion Amount,commission Percentage added as extra row
	var printURL = '${pop_up}';
	totAmtPaise = ${totalNetAmount*100};
	var hasDynaPackage = billType != 'P' && (visitType == 'i' || visitType == 'o') && jDynaPkgNameList != null && jDynaPkgNameList.length > 0;
	<c:if test="${not empty anaeTypesJSON}">
		var anaeTypes = ${anaeTypesJSON};
	</c:if>
	var useDRG = '${patient.use_drg}';
	var usePerdiem = '${patient.use_perdiem}';
	var isPhBillNowReturns = (billType == 'P' && restrictionType == 'P' && totAmtPaise < 0);
	var hasRewardPointsEligibility = (!isTpa && (availableRewardPoints > 0 || billRewardPoints != 0) && (!isPhBillNowReturns));
	var templateList = <%= request.getAttribute("templateListJSON") %>;
	var multiPlanExists = ${not empty multiPlanExists ? multiPlanExists : false};
	var disableDiscCategory = ${!canChangeDiscountPlan};
	var discountPlanAllowRt = '${actionRightsMap.allow_discount_plans_in_bill}';
</script>
</body>
</html>


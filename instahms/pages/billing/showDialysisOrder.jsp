<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@page import="org.apache.struts.Globals" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />

<c:set var="URl" value="${(actionId eq 'dialysis_order') ? 'dialysis_order' : '' }"/>
<c:set var="tooth_numbering_system" value='<%=GenericPreferencesDAO.getAllPrefs().get("tooth_numbering_system") %>'/>
<c:choose>
	<c:when test="${not empty param.mrNo}">
		  <c:set var="mrNo" value="${param.mrNo}" />
	</c:when>
	<c:otherwise>
	<!--  	<c:if test="${not empty param.mrNoR}">
		  <c:set var="mrNo" value="${param.mrNoR}" />
		</c:if>
		<c:if test="${not empty param.mrNoF}">
		  <c:set var="mrNo" value="${param.mrNoF}" />
		</c:if> -->
	</c:otherwise>
</c:choose>
<c:set var="dummyValue">
<insta:ltext key="billing.dialysisorders.rateplan.dummyvalue"/>
</c:set>
<html>
<head>
	<jsp:useBean id="currentDate" class="java.util.Date"/>
	<title><insta:ltext key="billing.dialysisorders.dialysisorder"/></title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
		<meta name="i18nSupport" content="true"/>

	<c:set var="version"><fmt:message key='insta.software.version'/></c:set>
	<c:set var="orderItemsUrl" value="${cpath}/master/orderItems.do?method=getOrderableItems"/>
	<c:set var="orderItemsUrl" value="${orderItemsUrl}&${version}&${sesHospitalId}&mts=${masterTimeStamp}"/>
	<c:set var="orderItemsUrl" value="${orderItemsUrl}&filter=&orderable=Y&directBilling=&operationOrderApplicable=${genPrefs.operationApplicableFor}"/>
	<c:if test="${preferences.modulesActivatedMap['mod_adv_packages'] eq 'Y'}">
		<c:set var="orderItemsUrl" value="${orderItemsUrl}&isMultiVisitPackage=true"/>
	</c:if>
	<c:set var="no_of_credit_debit_card_digits" value='<%=GenericPreferencesDAO.getAllPrefs().get("no_of_credit_debit_card_digits") %>'/>
	<c:set var="orderItemsUrlWithoutOrgTpa" value="${orderItemsUrl}"/>
	<script src="${orderItemsUrl}&orgId=ORG0001&visitType=o&center_id=${centerId}&tpa_id="></script>

	<insta:link type="script" file="ajax.js"/>
	<insta:link type="script" file="date_go.js"/>
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="script" file="widgets.js"/>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="script" file="billPaymentCommon.js"/>
	<insta:link type="js" file="orders/test_additional_details.js"/>
	<insta:link type="js" file="orders/orders.js" />
	<insta:link type="js" file="orderdialog.js" />
	<insta:link type="js" file="ordertable.js" />

	<insta:link type="script" file="dialysisOrder.js"/>

	<style type="text/css">
		td.forminfo { font-weight: bold; }
		form { padding: 0px; margin: 0px; }
		table.detailFormTable { font-family:Verdana,Arial,sans-serif; font-size:9pt; border-collapse: collapse;}
		table.detailFormTable td.label { padding: 0px 2px 0px 2px; overflow: hidden; }
		.stwMain { margin: 5px 7px }
		tr.deleted {background-color: #F2DCDC; color: gray; }
		tr.deleted input {background-color: #F2DCDC; color: gray;}
		tr.newRow {background-color: #E9F2C2; }
	</style>
	<script type="text/javascript">
		var cur_Date = '<fmt:formatDate pattern="dd-MM-yyyy" value="${currentDate}"/>';
		var cur_Time = '<fmt:formatDate pattern="HH:mm" value="${currentDate}"/>';
        var dischargedAsDead =<%= request.getAttribute("dischargedAsDead") %>;
		var deathDate = <%= request.getAttribute("deathDate") %>;
        var deathTime = <%= request.getAttribute("deathTime") %>;
		var doctorsList = ${doctorsList};
		var allowBackDate = '${actionRightsMap.allow_backdate}';
		var allowBackDateBillActivities = "${roleId == 1 || roleId == 2 ? 'A' :actionRightsMap.allow_back_date_bill_activities}";
		var fixedOtCharges = '${genPrefs.fixedOtCharges}';
		var forceSubGroupSelection = '${genPrefs.forceSubGroupSelection}';
		var anaeTypes = ${anaeTypesJSON};
		var regPref = <%= request.getAttribute("regPrefJSON") %> ;
		var sesHospitalId = '${ifn:cleanJavaScript(sesHospitalId)}';
		var masterTimestamp = '${masterTimestamp}';
		var multiPlanExists = false;
		gPrescDocRequired = '${genPrefs.prescribingDoctorRequired}';
		var screenid = '${screenId}';
		var equipTimingsRequired = false;
		// var allOrdersJSON = new Array();
		var mod_adv_packages = '${preferences.modulesActivatedMap['mod_adv_packages']}';
		var editOrders = true;
		var insured = false;
		var serviceGroupsJSON = ${serviceGroupsJSON};
		var servicesSubGroupsJSON = ${servicesSubGroupsJSON};
		var tooth_numbering_system = '${tooth_numbering_system}';
		var mainVisitId = '${mainVisitId}';
		var prevBillsReceiptsTotal = '${prevBillsReceiptsTotal}';
		var visitID = '${visitDet.map.patient_id}';
		var userCenterId = '${centerId}';
		var isMultiCenter = <%= request.getAttribute("isMultiCenter") %>
		var allOrdersJSON = <%= request.getAttribute("allOrdersJSON") %>;
		var ordrClaimAmtsMapJSON = ${ordrClaimAmtsMapJSON};
		var no_of_credit_debit_card_digits = ${empty no_of_credit_debit_card_digits ? 0 : no_of_credit_debit_card_digits};
	</script>

	<insta:js-bundle prefix="registration.patient"/>
	<insta:js-bundle prefix="billing.depositlist"/>
	<insta:js-bundle prefix="laboratory.radiology.billpaymentcommon"/>
	<insta:js-bundle prefix="order.common"/>
	<insta:js-bundle prefix="billing.salucro"/>
	<insta:js-bundle prefix="common.order"/>
	<insta:js-bundle prefix="billing.dialysisorders"/>
</head>

<c:set var="cpath" value="${pageContext.request.contextPath}" />

<body class="yui-skin-sam" onload="initDialysisOrder();ajaxForPrintUrls();">

<h1 style="float: left"><insta:ltext key="billing.dialysisorders.dialysisorder"/></h1>

<insta:patientsearch searchType="mrNo" searchUrl="DialysisOrder.do"  buttonLabel="Find"
	 searchMethod="showDialysisOrder" fieldName="mrNo"/>

<insta:AddOrderDialog visitType="${screenId eq 'ip_registration' ? 'i' : 'o'}"/>
<jsp:include page="/pages/orderEditCommon.jsp"/>

<insta:feedback-panel/>
<insta:patientgeneraldetails mrno="${mrNo}" />

<form name="mainform" id="mainform" method="POST" action="DialysisOrder.do">
	<fmt:formatDate var="currentdate" pattern="dd-MM-yyyy" value="${currentDate }"/>
	<fmt:formatDate var="currenttime" pattern="HH:mm" value="${currentDate}"/>
	<fieldset class="fieldSetBorder">
		<legend class="fieldSetLabel">Other Details</legend>
		<table class="patientdetails" cellpadding="0" cellspacing="0" width="100%">
	    	<tr>
	      		<td class="formlabel"><insta:ltext key="billing.dialysisorders.visitdatetime"/>:</td>
	      		<td>
	      			<c:set var="visitDt" value="${currentdate}"/>
	      			<c:set var="visitTm" value="${currenttime}"/>
	      			<c:choose>
	      				<c:when test="${mrNo != null && visitDet != null}">
	      					<fmt:formatDate var="reg_date" pattern="dd-MM-yyyy" value="${visitDet.map.reg_date }"/>
							<fmt:formatDate var="reg_time" pattern="HH:mm" value="${visitDet.map.reg_time}"/>
	      					<c:set var="visitDt" value="${reg_date}"/>
	      					<c:set var="visitTm" value="${reg_time}"/>
	      					<c:set var="visitId" value="${visitDet.map.patient_id}"/>
	      				</c:when>
	      				<c:when test="${mrNo != null}">
	      					<c:set var="visitDt" value="${currentdate}"/>
	      					<c:set var="visitTm" value="${currenttime}"/>
	      					<c:set var="visitId" value=""/>
	      				</c:when>
	      				<c:otherwise>
	      					<c:set var="visitDt" value=""/>
	      					<c:set var="visitTm" value=""/>
	      					<c:set var="visitId" value=""/>
	      				</c:otherwise>
	      			</c:choose>
					<insta:datewidget name="visitDate" id="visitDate" value="${visitDt}" btnPos="left"/>
					<input type="text" size="4" id="visitTime" name="visitTime" value="${visitTm}"
						 class="timefield" />
				</td>
	      		<td class="formlabel"><insta:ltext key="billing.dialysisorders.rateplan"/>:</td>
	      		<td>
	      			<select name="organization_details" id="organization_details" class="dropdown" onchange="onChangeRatePlan();">
						<option value="">${dummyValue}</option>
						<c:forEach items="${ratePlanList}" var="orgNames">
							<c:choose>
								<c:when test="${(lastVisitOrgId == orgNames.org_id) || (mainVisitBean.map.org_id == orgNames.org_id)}">
									<option value="${orgNames.org_id}" selected>${orgNames.org_name}</option>
								</c:when>
								<c:otherwise>
									<option value="${orgNames.org_id}">${orgNames.org_name}</option>
								</c:otherwise>
							</c:choose>
						</c:forEach>
					</select>
					<span class="star">*</span>
		 	  	</td>
	      		<td class="formlabel"></td>
	      		<td></td>
	    	</tr>
	   </table>
	</fieldset>

	<input type="hidden" name="_method" value="saveDialysisOrders">
	<input type="hidden" name="mr_no" id="mr_no" value="${ifn:cleanHtmlAttribute(mrNo)}"/>
	<input type="hidden" name="visitId" id="visitId" value="${ifn:cleanHtmlAttribute(visitId)}"/>
	<input type="hidden" name="billNo"  value="${billBean.map.bill_no}"/>

	<input type="hidden" name="appointmentId" value="${ifn:cleanHtmlAttribute(appointmentId)}"/>

	<input type="hidden" name="category" value="${ifn:cleanHtmlAttribute(category)}"/>
	<input type="hidden" name="scheduleName" value="${scheduleName}" />
	<input type="hidden" name="presDocId" value="${presDocId}" />

	<dl class="accordion" style="margin-bottom: 10px;">
		<dt>
			<span><insta:ltext key="billing.dialysisorders.details.patientapprovals"/></span>
			<div class="clrboth"></div>
		</dt>
		<dd id="payDD">
			<div class="bd"><br>
				<table class="approvalsfilters" cellpadding="0" cellspacing="0" width="100%">
			    	<tr>
			      		<td class="formlabel">Filter:
							<insta:selectoptions name="filterstatus" id="filterstatus" opvalues="A"
								optexts="Active" value="A"  dummyvalue="-- (All) --" dummyvalueId=""
								onchange="onChangeFilter(this);"/>
						</td>
			      		<td class="formlabel">Approvals By Sponsor:
			      			<insta:selectdb name="tpa_id" id="tpa_id" table="tpa_master" valuecol="tpa_id" displaycol="tpa_name"  filtered="false"
									value="" orderby="tpa_name" dummyvalue="-- (All) --"
									onchange="onChangeFilter(this);"/>
			      		<td>
				 	  	</td>
			      		<td class="formlabel">For period :
			      			<insta:selectoptions name="filterperiod" id="filterperiod" opvalues="C"
								optexts="Current Month" value="C"  dummyvalue="-- (All) --" dummyvalueId=""
								onchange="onChangeFilter(this);"/>
			      		</td>
			    	</tr>
			   </table>

			   <table class="detailList dialog_displayColumns" cellspacing="0" cellpadding="0" id="patientApprovals" border="0" style="margin-top: 8px">
			   		<tr>
			   			<th><insta:ltext key="billing.dialysisorders.approvals.sponsor"/></th>
			   			<th><insta:ltext key="billing.dialysisorders.approvals.approvalid"/></th>
			   			<th><insta:ltext key="billing.dialysisorders.approvals.validityend"/></th>
			   			<th><insta:ltext key="billing.dialysisorders.approvals.itemorservgrp"/></th>
			   			<th><insta:ltext key="billing.dialysisorders.approvals.limittype"/></th>
			   			<th><insta:ltext key="billing.dialysisorders.approvals.approvedlimit"/></th>
			   			<th><insta:ltext key="billing.dialysisorders.approvals.usedqtyamt"/></th>
			   			<th><insta:ltext key="billing.dialysisorders.approvals.copay"/></th>
			   		</tr>
			   		<c:set var="numapprovals" value="${fn:length(approvals)}"/>
			   		<input type="hidden" name="numapprovals" id="numapprovals"value="${fn:length(approvals)}"/>
					<c:forEach begin="1" end="${numapprovals+1}" var="i" varStatus="loop">
						<c:set var="approval" value="${approvals[i-1].map}"/>
						<c:if test="${empty approval}">
							<c:set var="style" value='style="display:none"'/>
						</c:if>
						<tr ${style}>
							<td>
								<c:choose>
									<c:when test="${approval.approval_status == 'Y'}">
										<img src="${cpath}/images/empty_flag.gif">
									</c:when>
									<c:otherwise>
										<img src="${cpath}/images/yellow_flag.gif">
									</c:otherwise>
								</c:choose>
								${approval.tpa_name}
							</td>
							<input type="hidden" name="status" id="status"  value="${approval.status}"/>
							<input type="hidden" name="sponsor_id" id="sponsor_id"  value="${approval.sponsor_id}"/>
							<input type="hidden" name="period" id="period"  value="${approval.period}"/>
				   			<td>${approval.approval_no}</td>
				   			<td>${approval.validity_end}</td>
				   			<td><insta:truncLabel value="${approval.applicable_to_name}" length="25"/></td>
				   			<td>${approval.limit_type_name}</td>
				   			<td>${approval.limit_value}</td>
				   			<td>
				   				<c:choose>
				   					<c:when test="${approval.limit_type=='Q'}">
				   						${approval.limit_value ge approval.used_qty  ? approval.used_qty : approval.limit_value}
				   					</c:when>
				   					<c:otherwise>
				   						${approval.used_amt}
				   					</c:otherwise>
				   				</c:choose>
				   			</td>
				   			<td>${approval.copay_value} ${approval.copay_type=='P' ? '%' : ''}</td>
						</tr>
					</c:forEach>
			   		<tr>
			   			<td></td>
			   			<td></td>
			   			<td></td>
			   			<td></td>
			   			<td></td>
			   			<td></td>
			   			<td></td>
			   			<td></td>
			   		</tr>
			   </table>
			</div>
		</dd>
	</dl>
	<div class="resultList" >
		<insta:OrderTable  onlyNew="true" orderList="${allOrders}" instanceId="0" />
	</div>

	<table id="innerDocVisitForPack"></table>
	<table id="innerCondDocForPack"></table>

	<fieldset class="fieldSetBorder">
		<legend class="fieldSetLabel"><insta:ltext key="billing.patientbill.details.totals"/></legend>
		<table align="right" class="infotable">
			<tr id ="filterRow">
				<c:set var="billopendate" value="${billBean.map.open_date}"/>
				<td class="formlabel">Amount : </td>
				<td class="forminfo">
					<label id="lblTotAmt">${billBean.map.total_amount}</label>
				</td>
				<td class="formlabel">&nbsp;</td>
				<td class="forminfo">&nbsp;</td>
				<td class="formlabel">Sponsor Due : </td>
				<td class="forminfo">
					<label id="lblTotSponAmt">${billBean.map.total_claim - billBean.map.primary_total_sponsor_receipts}</label>
				</td>
				<td class="formlabel">&nbsp;</td>
				<td class="forminfo">&nbsp;</td>
				<td class="formlabel">Patient Due : </td>
				<td class="forminfo">
					<label id="lblTotPatAmt">${billBean.map.total_amount -
						billBean.map.total_claim - billBean.map.total_receipts}</label>
				</td>
			</tr>
			<tr>
				<td class="formlabel">New Amount : </td>
				<td class="forminfo">
					<label id="lblNewAmt">0.00</label>
				</td>
				<td class="formlabel">&nbsp;</td>
				<td class="forminfo">&nbsp;</td>
				<td class="formlabel">New Sponsor Amt : </td>
				<td class="forminfo">
					<label id="lblNewSponAmt">0.00</label>
				</td>
				<td class="formlabel">&nbsp;</td>
				<td class="forminfo">&nbsp;</td>
				<td class="formlabel">New Patient Amt : </td>
				<td class="forminfo">
					<label id="lblNewPatAmt">0.00</label>
				</td>
			</tr>
			<tr>
				<td class="formlabel"></td>
				<td class="forminfo">
				</td>
				<td class="formlabel">&nbsp;</td>
				<td class="forminfo">&nbsp;</td>
				<td class="formlabel"></td>
				<td class="forminfo">
				</td>
				<td class="formlabel">&nbsp;</td>
				<td class="forminfo">&nbsp;</td>
				<td class="formlabel">Net Patient Due : </td>
				<td class="forminfo">
					<label id="lblNetPatDue">${billBean.map.total_amount -
						billBean.map.total_claim - billBean.map.total_receipts}</label>
				</td>
			</tr>
		</table>
	</fieldset>

	<dl class="accordion" style="margin-bottom: 10px;">
		<dt>
			<span><insta:ltext key="billing.dialysisorders.details.previousunpaidbills"/></span>
			<div class="clrboth"></div>
		</dt>
		<dd id="payDD">
			<div class="bd"><br>
				<c:set var="totpatdue" value="0"/>
				<table class="detailList dialog_displayColumns" cellspacing="0" cellpadding="0" id="patientApprovals" border="0" style="margin-top: 8px">
			   		<tr>
			   			<th><insta:ltext key="billing.dialysisorders.approvals.visitid"/></th>
			   			<th><insta:ltext key="billing.dialysisorders.approvals.visitdate"/></th>
			   			<th><insta:ltext key="billing.dialysisorders.approvals.billamt"/></th>
			   			<th><insta:ltext key="billing.dialysisorders.approvals.patamt"/></th>
			   			<th><insta:ltext key="billing.dialysisorders.approvals.patpaid"/></th>
			   			<th><insta:ltext key="billing.dialysisorders.approvals.creditnoteamount"/></th>
			   			<th><insta:ltext key="billing.dialysisorders.approvals.patdue"/></th>
			   		</tr>
			   		<c:set var="numunpaidbills" value="${fn:length(unpaidbills)}"/>
			   		<input type="hidden" name="numunpaidbills" id="numunpaidbills" value="${fn:length(unpaidbills)}"/>
					<c:forEach begin="1" end="${numunpaidbills+1}" var="i" varStatus="loop">
						<c:set var="unpaidbill" value="${unpaidbills[i-1].map}"/>
						<c:set var="style" value='style=""'/>
						<c:if test="${empty unpaidbill}">
							<c:set var="style" value='style="display:none"'/>
						</c:if>
						<tr ${style}>
							<td>${unpaidbill.visit_id}</td>
				   			<td><fmt:formatDate pattern="dd-MM-yyyy" value="${unpaidbill.reg_date}"/></td>
				   			<td>${unpaidbill.total_amount}</td>
				   			<td>${unpaidbill.total_amount - unpaidbill.total_claim}</td>
				   			<td>${unpaidbill.total_receipts}</td>
				   			<td>${unpaidbill.total_credits}</td>
				   			<td>${unpaidbill.total_amount - unpaidbill.total_claim - unpaidbill.total_receipts + unpaidbill.total_credits}</td>
				   			<c:set var="totpatAmt" value="${totpatAmt+(unpaidbill.total_amount - unpaidbill.total_claim)}"/>
				   			<c:set var="totpatdue" value="${totpatdue+(unpaidbill.total_amount - unpaidbill.total_claim - unpaidbill.total_receipts + unpaidbill.total_credits)}"/>
						</tr>
					</c:forEach>

			   	</table>
			   	<fieldset class="fieldSetBorder">
					<legend class="fieldSetLabel"><insta:ltext key="billing.patientbill.details.totals"/></legend>
						<table align="right" class="infotable">
							<tr id ="filterRow">
							<%-- <td class="formlabel">Tot.Amount : </td>
								<td class="forminfo">
									<label id="lblFilteredNetAmt">0.00</label>
								</td>
								<td class="formlabel">&nbsp;</td>
								<td class="forminfo">&nbsp;</td>
								<td class="formlabel">Tot. Pat. Paid: </td>
								<td class="forminfo">
									<label id="lblFilteredNetAmt">0.00</label>
								</td> --%>
								<td class="formlabel">&nbsp;</td>
								<td class="forminfo">&nbsp;</td>
								<td class="formlabel">Patient Due : </td>
								<td class="forminfo">
									<label id="lblPrevBillsPatDue">${totpatdue}</label>
								</td>
								<td class="forminfo">&nbsp;</td>
								<td class="formlabel">Gross Patient Due : </td>
								<td class="forminfo">
									<label id="lblGrosPatDue" title="newly ordered items pat. amount+previous bills pat. due">${grossPatientDue}</label>
								</td>
								<input type="hidden" id="h_prevBillsTotPatAmt" name="h_prevBillsTotPatAmt" value="${totpatAmt}"/>
							</tr>
						</table>
				</fieldset>
			</div>
		</dd>
	</dl>

	<%-- payments section --%>
	<dl class="accordion" style="margin-bottom: 10px;">
		<dt>
			<span><insta:ltext key="billing.patientbill.details.payments"/></span>
			<div class="clrboth"></div>
		</dt>
		<dd id="payDD">
			<div class="bd">

			<c:set var="paymentSelValue" value=""/>
			<c:set var="primarySponsor" value="${not empty visitDet ? visitDet.map.primary_sponsor_id : null}"/>
			<c:set var="secondarySponsor" value="${not empty visitDet ? visitDet.map.secondary_sponsor_id : null}"/>
		
			<insta:billPaymentDetails formName="mainform"  isBillNowPayment="${false}" defaultPaymentType="${paymentSelValue}"
			 	isInsuredPayment="${true}" isPrimarySponsorPayment="${false}" isSecondarySponsorPayment="${false}"
			 	primarySponsor="${primarySponsor}" secondarySponsor="${secondarySponsor}" />
			</div>
		</dd>
	</dl>

	<table cellpadding="0" cellspacing="0"  border="0" width="100%">
		<tr>
			<td>
				<button type="button" id="saveButton" accessKey="S"
					onclick="validateSave();"><b><u>S</u></b>ave</button>
				<c:if test="${not empty visitDet}">
				<insta:screenlink screenId="reg_general" addPipe="true" label="Edit Patient Details"
							extraParam="?_method=show&mrno=${mrNo}&regType=regd&mr_no=${mrNo}"/>
				<insta:screenlink screenId="consolidated_sponsor_receipts" addPipe="true" label="Sponsor Receipts"
							extraParam="?_method=list&sortOrder=bill_no&sortReverse=false&status=A"/>
				<insta:screenlink screenId="dialysis_prescriptions" addPipe="true" label="Dialysis Prescriptions"
							extraParam="?_method=add&mr_no=${mrNo}"/>
				<c:if test="${not empty preDialysisBean}">
					<c:set var="visit_center" value="${not empty visitDet ? visitDet.map.center_id : ''}"/>
					<insta:screenlink screenId="dialysis_pre_sessions" addPipe="true" label="Pre Dialysis"
							extraParam="?_method=add&prescriptionId=${preDialysisBean.map.order_id}&visit_center=${visit_center}&mr_no=${mrNo}"/>
				</c:if>
				</c:if>
				<c:if test="${not empty consoBillBean && not empty consoBillBean.map.consolidated_bill_no}">
					<insta:screenlink screenId="consolidated_bill" addPipe="true" label="View Consolidated Bill"
							extraParam="?_method=show&consolidated_bill_no=${consoBillBean.map.consolidated_bill_no}&mr_no=${mrNo}"/>
				</c:if>
			</td>
		</tr>
	</table>
	<c:set var="showPayments" value="true" />
	<div class="legend" style="display:${not empty approvals ? 'block' : 'none'}">
		<div class="flag"><img src="${cpath}/images/empty_flag.gif"></img></div>
		<div class="flagText">Approved</div>
		<div class="flag"><img src="${cpath}/images/yellow_flag.gif"/></div>
		<div class="flagText">Pending Approval</div>
	</div>
</form>

<form name="cancelOptionsForm">
	<div id="cancelOptionsDialog" style="visibility: hidden; display:none">
		<div class="bd">
			<fieldset class="fieldSetBorder">
				<legend class="fieldSetLabel"><insta:ltext key="orders.commonmodule.show.cancellationtype"/></legend>
				<table class="formtable" width="100%">
					<tr>
						<td style="width: 360px">
							<label for="cancelOrder1"><insta:ltext key="orders.commonmodule.show.cancelitemandcharges.toberefunded.in.brackets"/></label>
						</td>
						<td style="width: 30px">
							<input id="cancelOrder1" type="radio" name="cancelOrderType" value="IC">
						</td>
					</tr>
					<tr>
						<td style="width: 360px">
							<label for="cancelOrder2"><insta:ltext key="orders.commonmodule.show.cancelitemonly.norefund.in.brackets"/></label>
						</td>
						<td style="width: 30px">
							<input id="cancelOrder2" type="radio" name="cancelOrderType" value="I">
						</td>
					</tr>
				</table>
			</fieldset>
			<table>
				<tr>
					<td><input type="button" name="cancelOrderOk" value="OK" /></td>
					<td><input type="button" name="cancelOrderCancel" value="Cancel"/></td>
				</tr>
			</table>
		</div>
	</div>
</form>

<form name="oPcancelOptionsForm">
	<div id="oPcancelOptionsDialog" style="visibility: hidden; display:none">
		<div class="bd">
			<fieldset class="fieldSetBorder">
				<legend class="fieldSetLabel"><insta:ltext key="orders.commonmodule.show.cancellationtype"/></legend>
				<table class="formtable" width="100%">
					<tr>
						<td style="width: 360px">
							<label for="oPcancelOrder1"><insta:ltext key="orders.commonmodule.show.cancelitemandcharges.toberefunded.in.brackets"/></label>
						</td>
						<td style="width: 30px">
							<input id="oPcancelOrder1" type="radio" name="oPcancelOrderType" value="IC">
						</td>
					</tr>
					<tr>
						<td style="width: 360px">
							<label for="oPcancelOrder2"><insta:ltext key="orders.commonmodule.show.cancelitemonly.norefund.in.brackets"/></label>
						</td>
						<td style="width: 30px">
							<input id="oPcancelOrder2" type="radio" name="oPcancelOrderType" value="I">
						</td>
					</tr>
				</table>
			</fieldset>
			<table>
				<tr>
					<td><input type="button" name="oPcancelOrderOk" value="OK" /></td>
					<td><input type="button" name="oPcancelOrderCancel" value="Cancel"/></td>
				</tr>
			</table>
		</div>
	</div>
</form>
<script>
	var regDate="${visitDt}";
	var regTime="${visitTm}";
	var billOpenDate="${billopendate}";
	var mrno = '${ifn:cleanJavaScript(mrNo)}';
	var	eClaimModule = '${preferences.modulesActivatedMap['mod_eclaim']}';
	var doctorConsultationTypes = ${doctorConsultationTypes};
	var allDoctorConsultationTypes = ${allDoctorConsultationTypes} ;
	var mainConsultations = ${consultationsAcrVisits};
	var gServerNow = new Date(<%= (new java.util.Date()).getTime() %>);
</script>
</body>
</html>
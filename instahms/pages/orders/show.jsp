<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="tooth_numbering_system" value='<%=GenericPreferencesDAO.getAllPrefs().get("tooth_numbering_system") %>'/>
<c:set var="mod_adv_packages" value="${preferences.modulesActivatedMap.mod_adv_packages}"/>
<c:set var="ip_credit_limit_rule" value='<%=GenericPreferencesDAO.getAllPrefs().get("ip_credit_limit_rule") %>' />

<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<%-- TODO
  *
	* Validate doctor + consultation type: Anaesthetists, OT Doctors only should be allowed
	*   for selecting anaesthetst fees/surgeon fees.
	*
	* Initiation of dialysis should set partial completion status.
	*
--%>

<c:set var="title">
	<c:choose>
		<c:when test="${filter eq 'Laboratory'}"><insta:ltext key="orders.commonmodule.show.laboratorytestsorders"/></c:when>
		<c:when test="${filter eq 'Radiology'}"><insta:ltext key="orders.commonmodule.show.radiologytestsorders"/></c:when>
		<c:when test="${filter eq 'Service'}"><insta:ltext key="orders.commonmodule.show.servicesorders"/></c:when>
		<c:when test="${filter eq 'Equipment'}"><insta:ltext key="orders.commonmodule.show.equipmentorders"/></c:when>
		<c:otherwise><insta:ltext key="orders.commonmodule.show.orders"/></c:otherwise>
	</c:choose>
</c:set>

<c:set var="numOperations" value="${fn:length(operations)}"/>

<c:set var="filterValue">
	<c:choose>
		<c:when test="${filter eq 'Laboratory' || filter eq 'Radiology'}" >${filter},Order Sets</c:when>
		<c:otherwise>${filter}</c:otherwise>
		</c:choose>
</c:set>

<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>${title}</title>
	<insta:link type="js" file="ajax.js" />
	<insta:link type="js" file="hmsvalidation.js" />
	<insta:link type="js" file="orderdialog.js" />
	<insta:link type="js" file="ordertable.js" />
	<insta:link type="js" file="orders/orders.js" />
	<insta:link type="js" file="orders/OrderItemsCache.js" />
	<insta:link type="js" file="doctorConsultations.js" />
	<insta:link type="js" file="orders/test_additional_details.js"/>

	<c:set var="version"><fmt:message key='insta.software.version'/></c:set>
	<c:set var="orderItemsUrl" value="${cpath}/master/orderItems.do?method=getOrderableItems"/>
	<c:set var="orderItemsUrl" value="${orderItemsUrl}&${version}&${sesHospitalId}&mts=${masterTimeStamp}"/>
	<c:set var="orderItemsUrl" value="${orderItemsUrl}&filter=${filterValue}&orderable=Y"/>
	<c:set var="orderItemsUrl" value="${orderItemsUrl}&directBilling=&operationOrderApplicable=${genPrefs.operationApplicableFor}"/>
	<c:set var="orderItemsUrl" value="${orderItemsUrl}&orgId=${patient.org_id}&visitType=${patient.visit_type}"/>
	<c:set var="orderItemsUrl" value="${orderItemsUrl}&center_id=${patient.center_id}&tpa_id=${patient.primary_sponsor_id}&dept_id=${patient.dept_id}&gender_applicability=${patient.patient_gender}&order_controls_applicable=Y"/>
	<c:set var="orderItemsUrl" value="${orderItemsUrl}&planId=${patient.plan_id == 0 ? '' : patient.plan_id}"/>
	<c:if test="${mod_adv_packages == 'Y'}">
		<c:set var="orderItemsUrl" value="${orderItemsUrl}&isMultiVisitPackage=true"/>
	</c:if>

	<c:set var="orderItemsForOperationUrl" value="${orderItemsUrl}&operationApplicable=Y&scriptvar=rateplanwiseoperationitems"/>
	<insta:js-bundle prefix="order.common"/>
	<script src="${orderItemsUrl}"></script>
	<script src="${orderItemsForOperationUrl}"></script>

	<style type="text/css">
		.yui-skin-sam .yui-dt-col-priority { padding: 2px 10px;}

		.yui-ac {
			width: 15em;
			padding-bottom: 2em;
		}

		.scrolForContainer .yui-ac-content{
			 max-height:11em;overflow:auto;overflow-x:hidden; /* scrolling */
		    _height:11em; /* ie6 */
		}

		table#operationtable tr.added td {
			background-color:#EAFADC;
		}

	</style>

	<c:set var="allowCancle" value="${roleId == 1 || roleId == 2 ? 'A' : actionRightsMap.cancel_elements_in_bill}"/>
	<c:set var="showCharges" value="${roleId == 1 || roleId == 2 ? 'A' : actionRightsMap.view_all_rates}"/>
	<c:if test="${showCharges == 'N' && not empty patient && patient.org_id == 'ORG0001'}">
		<c:set var="showCharges" value="A"/>
	</c:if>

	<script type="text/javascript">
		var departmentId = '${patient.dept_id}';
		var genderApplicability = '${patient.patient_gender}';
		mealTimingsRequired = true;
		equipTimingsRequired = true;
		var cpath = '<%=request.getContextPath()%>';
		var filter = '${filter}';
		numOperations = ${numOperations};
		var allowBackdate = "${roleId == 1 || roleId == 2 ? 'A' : actionRightsMap.allow_backdate}";
		var allowBackDateBillActivities = "${roleId == 1 || roleId == 2 ? 'A' : actionRightsMap.allow_back_date_bill_activities}";
		<c:if test="${not empty patient}">
			var doctorsList = ${doctorsJSON};
			var gTestPrescriptions = ${testPrescriptionsJSON};
			var gServicePrescriptions = ${servicePrescriptionsJSON};
			var gDietPrescriptions = ${dietPrescriptionsJSON};
			var gConsultationPrescriptions = ${consultationPrescriptionsJSON};
			var gOperationPrescriptions = ${operationPrescriptionsJSON};
			var regdate = '<fmt:formatDate value="${patient.reg_date}" pattern="dd-MM-yyyy"/>';
			var regtime = '<fmt:formatDate value="${patient.reg_time}" pattern="HH:mm"/>';
			var patientType = '${patient.visit_type}';
			var patientStatus = '${patient.visit_status}';
			var billDetails = ${billsJSON};
			var totalCreditAmount = 0,billAmount = 0;
			var patientDocId = '${patient.doc_id}';
			prescribingDocName = '';
			prescribingDoctor = '';
			var fixedOtCharges = '${genPrefs.fixedOtCharges}';
			gPrescDocRequired = '${genPrefs.prescribingDoctorRequired}';
			var gOnePrescDocForOP = '${genPrefs.op_one_presc_doc}';
			<c:if test="${genPrefs.default_prescribe_doctor == 'Y'}">
				prescribingDocName = <insta:jsString value="${patient.doctor_name}"/>;
				prescribingDoctor = '${patient.doctor}';
			</c:if>
			var consultingDocName = <insta:jsString value="${patient.doctor_name}"/>;
			tpaId = '${patient.primary_sponsor_id}';
			planId = '${patient.plan_id}';
			var serviceGroupsJSON = ${serviceGroupsJSON};
			var servicesSubGroupsJSON = ${servicesSubGroupsJSON};
			var doctorConsultationTypes = ${doctorConsultationTypes};
			var allDoctorConsultationTypes = ${allDoctorConsultationTypes} ;
			var regPref = ${regPrefJSON};
			var anaeTypes = ${anaeTypesJSON};
			var allOrdersJSON = ${allOrdersJSON};
			var mainConsultations = ${consultationsAcrVisits};
			var gFrom_pending_prescriptions = '${from_pending_prescriptions}';
		</c:if>
		mealTimingsRequired = true;
		equipTimingsRequired = true;
		var forceSubGroupSelection = '${genPrefs.forceSubGroupSelection}';
		var mrno = '${patient.mr_no}';
		var eClaimModule = '${preferences.modulesActivatedMap['mod_eclaim']}';
		var editOrders = true;
		var newBillAllowed = ${actionRightsMap.new_bill_for_order_screen == 'A' || roleId == '1' || roleId == '2'};
		var insured = ${not empty patient.primary_sponsor_id};
		var tooth_numbering_system = '${tooth_numbering_system}';
		var gToothNumberRequired = true;
		var mod_adv_packages = '${mod_adv_packages}';
		var planList = <%= request.getAttribute("planList") %>;
		var multiPlanExists = ${not empty multiPlanExists ? multiPlanExists : false};
		var screenid = '';
		var sesHospitalId = '${ifn:cleanJavaScript(sesHospitalId)}';
		var masterTimeStamp = '${masterTimeStamp}';
		var operationApplicableFor = '${genPrefs.operationApplicableFor}';
		var orgId = '${patient.org_id}';
		var visitType = '${patient.visit_type}';
		var centerId = '${patient.center_id}';
		var priSponsorId = '${patient.primary_sponsor_id}';
		var ip_credit_limit_rule = <insta:jsString value="${ip_credit_limit_rule}"/>;
		var ipCreditLimitAmount = ${not empty patient.ip_credit_limit_amount ? patient.ip_credit_limit_amount : 0};
		var visitTotalPatientDue = ${visitTotalPatientDue}
	</script>
	<insta:js-bundle prefix="order.common"/>
	<insta:js-bundle prefix="common.order"/>
	<insta:js-bundle prefix="registration.patient"/>
</head>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="okBtn">
  <insta:ltext key="patient.registration.button.save"/>
</c:set>
<c:set var="cancelBtn">
	<insta:ltext key="patient.registration.button.cancel"/>
</c:set>
<body onload="init(); ajaxForPrintUrls();">

<h1 style="float: left">${title}</h1>

<insta:patientsearch searchType="visit" searchUrl="order.do" buttonLabel="Find"
	searchMethod="getOrders" fieldName="patient_id" activeOnly="true"/>

<insta:feedback-panel/>
<insta:patientdetails patient="${patient}" showClinicalInfo="true"/>
<fieldset class="fieldSetBorder" style="display: ${not empty babyOrMother ? 'block' : 'none'}">
	<legend class="fieldSetLabel">Other Details</legend>
	<table class="patientdetails"  border="0" width="100%">
		<c:forEach var="baby" items="${babyOrMother}">
			<tr>
				<td class="formlabel">
					<b>
						<c:if test="${baby.ISBABY !='Y' && baby.ISBABY !='N'}"><insta:ltext key="orders.commonmodule.show.babydetails"/></c:if>
						<c:if test="${baby.ISBABY =='Y' || baby.ISBABY =='N'}"><insta:ltext key="orders.commonmodule.show.motherdetails"/></c:if>
					</b>
				</td>
				<td ></td>
				<td class="formlabel"></td>
				<td></td>
				<td class="formlabel"></td>
				<td></td>
			</tr>
			<tr>
				<td class="formlabel"><insta:ltext key="ui.label.mrno"/>: </td>
				<td class="forminfo">
					<c:if test="${baby.ISBABY !='Y' && baby.ISBABY !='N'}">${baby.MRNO2}</c:if>
					<c:if test="${baby.ISBABY =='Y' || baby.ISBABY =='N'}">${baby.MRNO1}</c:if>
				</td>
				<td class="formlabel"><insta:ltext key="orders.commonmodule.show.name"/>:</td>
				<td class="forminfo">${baby.PATIENT_NAME} ${baby.MIDDLE_NAME}</td>
				<td class="formlabel"></td>
				<td></td>
			</tr>
		</c:forEach>
	</table>
</fieldset>

<form method="POST" name="mainform" action="order.do?_method=update" enctype="multipart/form-data" autocomplete="off">
<input type="hidden" name="_method" id="method" value="update"/>
<input type="hidden" name="print" id="print" value="save"/>
<input type="hidden" name="printerId" id="printerId" value=""/>
<input type="hidden" name="amount" id="amount" readonly="readonly" size="5" value="0"/>
<input type="hidden" name="fromOTScreen" value="${ifn:cleanHtmlAttribute(fromOTScreen)}"/>
<input type="hidden" name="operation_details_id" value="${ifn:cleanHtmlAttribute(operation_details_id)}"/>

<script>
	bedType = <insta:jsString value="${patient.bill_bed_type}"/>;
	orgId = '${patient.org_id}';
	patientType = '${patient.visit_type}';
</script>
<input type="hidden" name="mrno" id="mrno" value='${patient.mr_no}' />
<input type="hidden" name="orgid" id="orgid" value='${patient.org_id}' />
<input type="hidden" name="doctor" id="doctor" value='${patient.doctor}' />
<input type="hidden" name="patientid" id="patientid" value='${patient.patient_id}' />
<input type="hidden" name="visitType" id="visitType" value='${patient.visit_type}'/>
<input type="hidden" name="newPatientAmt" id="newPatientAmt" value=""/>

<input type="hidden" id="dialogId" value=""/>
<c:set var="primaryBill" value=""/>
<c:forEach var="bill" items="${bills}">
	<c:if test="${not empty bill.is_primary_bill && bill.is_primary_bill == 'Y'}">
		<c:set var="primaryBill" value="${bill.bill_no}"/>
	</c:if>
</c:forEach>

<c:set var="selectedBillNo" value="${empty param.billNo && not empty primaryBill ? primaryBill : param.billNo}"/>

<fieldset class="fieldSetBorder">
	<legend class="fieldSetLabel"><insta:ltext key="orders.commonmodule.show.billdetails"/></legend>
	<table class="infotable">
		<tr>
			<td class="formlabel" style="width: 68px"><insta:ltext key="orders.commonmodule.show.billno"/>:</td>
			<td style="width: 150px">
				<select name="billNo" onchange="return onChangeBill()" class="dropdown" style="width:200px;">
					<c:if test="${(fn:length(bills) > 1) || (fn:length(bills) == 0)}">
						<option value="">-- Select --</option>
					</c:if>
					<c:forEach var="bill" items="${bills}">
						<option value="${bill.bill_no}" <c:if test="${bill.bill_no eq selectedBillNo }"><insta:ltext key="orders.commonmodule.show.selected"/></c:if> >
							<c:choose>
								<c:when test="${bill.is_tpa}">
									${bill.bill_no} ${bill.bill_type == 'C' ? ' (Bill Later Insurance)': ' (Bill Now Insurance)'}
								</c:when>
								<c:otherwise>
									${bill.bill_no} ${bill.bill_type == 'C' ? ' (Bill Later)': ' (Bill Now)'}
								</c:otherwise>
							</c:choose>
						</option>
					</c:forEach>
					<c:if test="${actionRightsMap.new_bill_for_order_screen == 'A' || roleId == '1' || roleId == '2'}">
						<option value="new"><insta:ltext key="orders.commonmodule.show.newbill"/></option>
						<c:set var="allowConnectTPA" value="${(not empty patient.primary_sponsor_id && empty patient.secondary_sponsor_id) || multiPlanExisits}"/>
						<c:if test="${allowConnectTPA && allowBillNowInsurance && patient.use_drg == 'N'
									 && patient.use_perdiem == 'N' }">
							<option value="newInsurance"><insta:ltext key="orders.commonmodule.show.newbill.insurance.in.brackets"/></option>
						</c:if>
					</c:if>
				</select>
			</td>
			<td class="formlabel" style="width: 116px"><insta:ltext key="orders.commonmodule.show.billamount"/>:</td>
			<td class="forminfo" style="width: 110px" id="billAmt"></td>
			<td class="formlabel" style="width: 114px"><insta:ltext key="orders.commonmodule.show.credits"/>:</td>
			<td class="forminfo"  style="width: 110px"id="creditsAmt"></td>
			<c:if test="${showCharges == 'A'}">
				<td class="formlabel" style="width: 114px"><insta:ltext key="orders.commonmodule.show.newamount"/>:</td>
				<td class="forminfo" id="totalNewAmount" style="width: 60px"></td>
			</c:if>
			</tr>
			<c:if test="${(not empty patient.primary_sponsor_id) && (showCharges == 'A')}">
				<tr id="patientAmounts">
					<td></td>
					<td></td>
					<td class="formlabel" style="width: 116px"><insta:ltext key="orders.commonmodule.show.patientamount"/>:</td>
					<td class="forminfo" style="width: 110px" id="billPatientAmt"></td>
					<td></td>
					<td></td>
					<td class="formlabel" style="width: 114px"><insta:ltext key="orders.commonmodule.show.patientnewamount"/>:</td>
					<td class="forminfo" id="totalNewPatientAmt" style="width: 60px"></td>
				</tr>
			</c:if>
	</table>
</fieldset>

<div class="resultList">
	<insta:OrderTable orderList="${allOrders}" operItems="false" instanceId="0"
	insured="${not empty patient.primary_sponsor_id}"
	allowAdd="${patient.visit_status == 'A' && hasOpenUnpaidPriInsCreditBillForPerdiem}"
	allowCancle="${allowCancle eq 'A'}"
	test_info_view="true"/>
</div>

<c:if test="${filter ne 'Service'}">
	<jsp:include page="/pages/orders/TestAdditionalDetailsInclude.jsp" >
		<jsp:param name="patient_id" value="${patient.patient_id}"/>
		<jsp:param name="allowAdd" value="${patient.visit_status == 'A' && hasOpenUnpaidPriInsCreditBillForPerdiem}"/>
		<jsp:param name="filter" value="${filter}"/>
	</jsp:include>
</c:if>

<div id="surgeriesHeading" style="display: ${numOperations > 0 ? 'block' : 'none'}">
	<br/>
	<h2><insta:ltext key="orders.commonmodule.show.surgeries"/></h2>

	<table id="operationtable" width="100%">

		<c:forEach begin="0" end="${numOperations}" var="i" varStatus="loop">
			<c:set var="operation" value="${operations[i].map}"/>
			<c:set var="presc_id" value="${operation.id}"/>
			<c:set var="operAnaesthesiDetails" value="${operAnaesthesiaTypeOrdersMap[presc_id]}"/>
			<c:set var="advancedOperAnaesthesiDetails" value="${advotanaesthesiatypeordersmap[presc_id]}"/>
			<tr id="op_${operation.id}" style="${empty operation ? 'display:none' : ''}">
				<td>
					<fieldset class="fieldSetBorder" width="100%" id="">
						<legend class="fieldSetLabel">
						<insta:ltext key="orders.commonmodule.show.ord"/>#:
						<c:choose>
							<c:when test="${operation.status != 'X'}">
								<a target="blank" href="order.do?_method=printOrder&patientId=${patient.patient_id}&orderid=${operation.common_order_id}">
									${operation.common_order_id}</a>
							</c:when>
							<c:otherwise>
								<label>${operation.common_order_id}</label>
							</c:otherwise>
						</c:choose>
						&nbsp;&nbsp;&nbsp;
						<insta:truncLabel value="${operation.name}" length="32"/></legend>
						<table class="infotable" width="100%">	<%-- header of operation --%>
							<tr>
								<td class="formlabel">
									<insta:ltext key="orders.commonmodule.show.operationtheatre"/>:
									<input type="hidden" name="multi_visit_package_bill_no" value="">
									<input type="hidden" name="package_id" value="">
									<input type="hidden" name="multi_visit_package" value="">
									<input type="hidden" name="item_id" value="${operation.operation_id }"/>
									<input type="hidden" name="type" value="Operation"/>
									<input type="hidden" name="operationId" value="${operation.id}"/>
									<input type="hidden" name="operAmount" value="0"/>
									<input type="hidden" name="operPatientAmt" value="0"/>
									<input type="hidden" name="new" value="N" />
									<input type="hidden" name="cancelled" value=""/>
									<input type="hidden" name="edited" value=""/>
									<input type="hidden" name="existingtype" value="${empty operation ? '' : 'Operation'}"/>
									<input type="hidden" name="prescribedId" value="${operation.id}"/>
									<input type="hidden" name="bill_status" value="${operation.bill_status}"/>
									<input type="hidden" name="status" value="${operation.status}"/>
									<input type="hidden" name="pres_date" value="<fmt:formatDate value="${operation.prescribed_date }" pattern="dd-MM-yyyy"/>"/>
									<fmt:formatDate value="${operation.start_datetime}" var="fromDate" pattern="dd-MM-yyyy"/>
									<fmt:formatDate value="${operation.start_datetime}" var="fromTime" pattern="HH:mm"/>
									<fmt:formatDate value="${operation.end_datetime}" var="toDate" pattern="dd-MM-yyyy"/>
									<fmt:formatDate value="${operation.end_datetime}" var="toTime" pattern="HH:mm"/>
									<input type="hidden" name="fromDate" value="${fromDate}"/>
									<input type="hidden" name="fromTime" value="${fromTime}"/>
									<input type="hidden" name="toDate" value="${toDate}"/>
									<input type="hidden" name="toTime" value="${toTime}"/>
									<input type="hidden" name="presDocId" value="${operation.consultant_doctor}"/>
									<input type="hidden" name="presDocName" value="${operation.doctor_name}"/>
									<input type="hidden" name="remarks" value="${operation.remarks}"/>
									<input type="hidden" name="newFinStatus" value="${operation.finalization_status}"/>
									<input type="hidden" name="finStatus" value="${operation.finalization_status}"/>
									<input type="hidden" name="urgent" value=""/>
									<input type="hidden" name="quantity" value=""/>
									<c:forEach items="${operAnaesthesiDetails}" var="operAnaesthesiaType">
										<input type="hidden" name="anaesthesia_type_${i}" value="${operAnaesthesiaType.map.anesthesia_type_id}">
										<input type="hidden" name="anaes_start_datetime_${i}" value="<fmt:formatDate value="${operAnaesthesiaType.map.anaes_start_datetime}" pattern="dd-MM-yyyy HH:mm"/>">
										<input type="hidden" name="anaes_end_datetime_${i}" value="<fmt:formatDate value="${operAnaesthesiaType.map.anaes_end_datetime}" pattern="dd-MM-yyyy HH:mm"/>">
										<input type="hidden" name="op_edit_charge_id_${i}" value="${operAnaesthesiaType.map.charge_id}">
										<input type="hidden" name="surgery_anesthesia_details_id_${i}" value="${operAnaesthesiaType.map.surgery_anesthesia_details_id}">
									</c:forEach>
									<c:forEach items="${advancedOperAnaesthesiDetails}" var="advOperAnaesthesiaType">
										<input type="hidden" name="operation_anae_detail_id_${i}" value="${advOperAnaesthesiaType.map.operation_anae_detail_id}">
									</c:forEach>
								</td>
								<td class="forminfo">${operation.theatre}</td>

								<td class="formlabel"><insta:ltext key="orders.commonmodule.show.start"/>:</td>
								<td class="forminfo">
									<fmt:formatDate value="${operation.start_datetime}" pattern="dd-MM-yyyy HH:mm"/>
								</td>

								<td class="formlabel"><insta:ltext key="orders.commonmodule.show.end"/>:</td>
								<td class="forminfo">
									<fmt:formatDate value="${operation.end_datetime}" pattern="dd-MM-yyyy HH:mm"/>
								</td>
								<td></td>
								<td></td>

								<td style="text-align: right">
									<c:set var="cancelEnabled" value="${allowCancle == 'A' &&
									(empty operation || operation.status == 'N' || operation.status == 'U' )}"/>
									<c:choose>
										<c:when test="${cancelEnabled}">
											<a href="javascript:void(0)" onclick="cancelOperation(this, '${instanceId}');"
												title='<insta:ltext key="orders.commonmodule.show.cancelorder"/>' >
												<img src="${cpath}/icons/delete.gif" class="button" />
											</a>
										</c:when>
										<c:otherwise>
											<img class="imgDelete" src="${cpath}/icons/delete_disabled.gif" />
										</c:otherwise>
									</c:choose>
								</td>
								<c:if test="${!onlyNew}">
									<td>
										<c:set var="editEnabled" value="${(operation.status=='N' || operation.status == 'U')}"/>
										<c:choose>
											<c:when test="${editEnabled}">
												<a href="javascript:void(0)" name="btnEditCharges" id="btnEditCharges${i}"
													onclick="showOpEditDialog(this, '${instanceId}');"
													title='<insta:ltext key="orders.commonmodule.show.edititemdetails"/>' >
													<img src="${cpath}/icons/Edit.png" class="button" />
												</a>
											</c:when>
											<c:otherwise>
												<img src="${cpath}/icons/Edit1.png"/>
											</c:otherwise>
										</c:choose>
									</td>
								</c:if>
							</tr>

							<tr>
								<td class="formlabel"><insta:ltext key="orders.commonmodule.show.surgeon"/>:</td>
								<td class="forminfo">${operation.surgeon_name}</td>

								<td class="formlabel"><insta:ltext key="orders.commonmodule.show.status"/>:</td>
								<td class="forminfo">${operation.status eq 'X' ? 'Cancelled' : operation.status eq 'C' ? 'Completed' : operation.status == 'U' ? 'N/A' : ''}</td>

								<td class="formlabel"><insta:ltext key="orders.commonmodule.show.remarks"/>:</td>
								<td class="forminfo"><insta:truncLabel value="${operation.remarks}" length="50"/></td>

								<td class="formlabel"><insta:ltext key="orders.commonmodule.show.totalnewitemsamount"/>:</td>
								<td class="forminfo"></td>

							</tr>

							<tr>
								<td class="formlabel"><insta:ltext key="orders.commonmodule.show.secondaryoperations"/>:</td>
								<td class="forminfo">${operation.secondary_operations}</td>
							</tr>

						</table>

						<%-- Operation related other services/charges --%>
						<div class="resultList" style="width:940px">
							<insta:OrderTable orderList="${operOrdersMap[operation.id]}"
							operItems="true" instanceId="${operation.id}" insured="${not empty patient.primary_sponsor_id}"
							itemId="${operation.operation_id}" allowAdd="${operation.status != 'X' }" allowCancle="${allowCancle eq 'A' }"/>
						</div>
					</fieldset>
				</td>
			</tr>
		</c:forEach>
	</table>
</div>


<table id="innerDocVisitForPack"></table>
<table id="innerCondDocForPack"></table>
</form>

<jsp:include page="/pages/orderEditCommon.jsp"/>

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
					<td><input type="button" name="cancelOrderOk" value="${okBtn}" /></td>
					<td><input type="button" name="cancelOrderCancel" value="${cancelBtn}"/></td>
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
					<td><input type="button" name="oPcancelOrderOk" value="${okBtn}" /></td>
					<td><input type="button" name="oPcancelOrderCancel" value="${cancelBtn}"/></td>
				</tr>
			</table>
		</div>
	</div>
</form>

<form name="oPeditForm">
	<div id="oPeditDialog" style="visibility: none; display:none">
		<div class="bd">
			<fieldset class="fieldSetBorder">
				<legend class="fieldSetLabel"><insta:ltext key="orders.commonmodule.show.edititemdetails"/></legend>
				<table class="formtable" width="100%">
					<tr>
						<td class="formlabel"><insta:ltext key="orders.commonmodule.show.prescribedby"/>:</td>
						<td class="yui-skin-sam">
							<div>
								<input type="text" name="eOpPrescribedBy" id="eOpPrescribedBy" style="width: 11.5em"  />
								<div id="eOpPrescribedByContainer"></div>
							</div>
							<input type="hidden" name="ePresDocId"/>
						</td>
						<td class="formlabel"><insta:ltext key="orders.commonmodule.show.remarks"/>:</td>
						<td><input type="text" name="eRemarks" id="eRemarks"/></td>
					</tr>
					<tr>
						<td class="formlabel"><insta:ltext key="orders.commonmodule.show.finalized"/>:</td>
						<td><input type="checkbox" name="eCompleted"/></td>
						<td></td>
						<td></td>
					</tr>
					<tr>
						<td class="formlabel"><insta:ltext key="orders.commonmodule.show.start"/>:</td>
						<td>
							<insta:datewidget name="eOpFromDate" btnPos="left"/>
							<input type="text" name="eOpFromTime" id="eFromTime" class="timefield" maxlength="5" />
						</td>

						<td class="formlabel"><insta:ltext key="orders.commonmodule.show.end"/>:</td>
						<td>
							<insta:datewidget name="eOpToDate" btnPos="left"/>
							<input type="text" name="eOpToTime" id="eToTime" class="timefield" maxlength="5"/>
						</td>
					</tr>
				</table>
			</fieldset>
			<div style="height:10px;">&nbsp;</div>
			<fieldset id="opEditAnaesDetFieldset">
				<legend class="fieldSetLabel"><insta:ltext key="common.addorder.label.anesthesia.details"/></legend>
				<table  width="100%" id="operAnaesthesiaDetTable">
				</table>
			</fieldset>
			<table>
				<tr>
					<td><input type="button" value="${okBtn}" onclick="saveOpEdit()"></td>
					<td><input type="button" value="${cancelBtn}" onclick="cancelOpEdit()"/></td>
				</tr>
			</table>
		</div>
	</div>
</form>


<table style="float: left" class="screenActions">
	<tr>
		<td>
			<button type="submit" name="save" id="save" accesskey="S" class="button" onclick="onSubmit(this);">
			<label><u><b><insta:ltext key="orders.commonmodule.show.s"/></b></u><insta:ltext key="orders.commonmodule.show.ave"/></label></button>&nbsp;
			<button type="button" name="saveNPrint" id="saveNPrint" accesskey="P" onclick="onSubmit(this);" disabled><insta:ltext key="orders.commonmodule.show.save"/> &amp; <b><u><insta:ltext key="orders.commonmodule.show.p"/></u></b><insta:ltext key="orders.commonmodule.show.rint"/></button>
			<%-- hyper links for each bill --%>
			<c:if test="${urlRightsMap.credit_bill_collection == 'A' || roleId == 1 || roleId == 2}">
				<c:forEach var="bill" items="${bills}">
				| <insta:screenlink screenId="credit_bill_collection" extraParam="?_method=getCreditBillingCollectScreen&billNo=${bill.bill_no}"
					label="Bill ${bill.bill_no}"/>
				</c:forEach>
			</c:if>
			<c:if test="${genPrefs.sampleFlowRequired == 'Y' && patient.patient_id != null && filter ne 'Service' && filter ne 'Radiology' && fromOTScreen != 'Y'}">
			<c:choose>
				<c:when test="${urlRightsMap.lab_pending_samples_search == 'A' }">
					<c:set var="collectSampleUrl" value="PendingSamplesSearch"/>
				</c:when>
				<c:otherwise>
					<c:set var="collectSampleUrl" value="PendingSamples"/>
				</c:otherwise>
			</c:choose>
				|<a href="${pageContext.request.contextPath}/Laboratory/${collectSampleUrl }.do?_method=getSampleCollectionScreen&visitid=${patient.patient_id}&title=Collect Sample&default_sample_status=C"
				target="_blank"><insta:ltext key="orders.commonmodule.show.collectsample"/></a>
			</c:if>
			<c:if test="${patient.patient_gender eq 'F' && patient.visit_type eq 'i' && patient.salutation ne 'Baby' && empty filter }" >
				| <a href="${cpath}/pages/ipservices/Ipservices.do?_method=getNewBorn&mrno=${patient.mr_no}&patientid=${patient.patient_id}"><insta:ltext key="orders.commonmodule.show.childbirth"/></a>
			</c:if>
			<c:if test="${fromOTScreen == 'Y'}">
				| <a href="${cpath}/otservices/OtManagement.do?_method=getOtManagementScreen&operation_details_id=${ifn:cleanURL(operation_details_id)}&visit_id=${patient.patient_id}">
				<insta:ltext key="orders.commonmodule.show.otmanagement"/>
				</a>
			</c:if>
		</td>
	</tr>
	<tr><td><table id="quantitytable"></table></td></tr>
</table>
<div style="float: right; margin-top:10px">
	<insta:selectdb name="printId" table="printer_definition" valuecol="printer_id"
	displaycol="printer_definition_name"  value="${showPrinter}" id="printId"/>

</div>
<form name="cancelReconductionTestForm">
<div style="display:none" id="cancleReconductTestDialog">
<div class="bd">
	<fieldset class="fieldSetBorder">
		<table class="formtabel">
			<tr>
				<td colspan="2" class="formlabel">
					<insta:ltext key="orders.commonmodule.show.reconductiontestischosenforcancellation"/></br><insta:ltext key="orders.commonmodule.show.doyouwanttocancel"/>
				</td>
			</tr>
		</table>
		<input type="button" value="${okBtn}" name="cancleReconductionTestOK">
		<input type="button" value="${cancelBtn}" name="cancleReconductionTestCancle"/>
	</fieldset>
</div>
</div>
</form>
<div style="clear: both"></div>
<div class="legend">
	<div class="flag"><img src='${cpath}/images/green_flag.gif'></div>
	<div class="flagText"><insta:ltext key="orders.commonmodule.show.completed"/></div>
	<div class="flag"><img src='${cpath}/images/yellow_flag.gif'></div>
	<div class="flagText"><insta:ltext key="orders.commonmodule.show.partiallycompleted"/></div>
	<div class="flag"><img src='${cpath}/images/blue_flag.gif'></div>
	<div class="flagText"><insta:ltext key="orders.commonmodule.show.samplecollected"/></div>
	<div class="flag"><img src='${cpath}/images/red_flag.gif'></div>

	<div class="flagText"><insta:ltext key="orders.commonmodule.show.cancelled"/></div>
	<div class="flag"><img src='${cpath}/images/brown_flag.gif'></div>
	<div class="flagText"><insta:ltext key="orders.commonmodule.show.reconducted.reopened"/></div>

</div>
<insta:AddOrderDialog visitType="${patient.visit_type}"/>

</body>
</html>


<%@page import="com.insta.hms.common.Encoder"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>

<c:set var="cpath" value="${pageContext.request.contextPath}" scope="request"/>
<c:set var="genericPrefs" value='<%= GenericPreferencesDAO.getAllPrefs() %>'/>
<c:set var="billPrintRights" value="${urlRightsMap.bill_print}"/>
<c:set var="permissibleDiscountPercenatge" value ='${permissibleDiscountPercenatge}'/>
<fmt:formatDate pattern="HH:mm" value="<%=new java.util.Date()%>" var="current_time"/>

<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDTO"%>
<%@page import="com.insta.hms.stores.SupplierMasterDAO"%>
<%@page import="com.insta.hms.master.dentalsupplier.DentalSupplierMasterDAO"%>
<%@page import="com.insta.hms.master.dentalsupplieritemratemaster.DentalSupplierItemRateMasterDAO"%>
<%@page import="com.insta.hms.common.ConversionUtils"%>

<html>
<head>
	<script>
		var showChargesAllRatePlan = ('${roleId}' == '1' || '${roleId}' == '2') ? 'A' : '${actionRightsMap.view_all_rates}';
		var discPart = ('${roleId}' == '1' || '${roleId}' == '2') ? 'A' : '${actionRightsMap.allow_discount}';
		var tooth_numbering_system = '${genericPrefs.map.tooth_numbering_system}';
		var use_store_items = '${genericPrefs.map.prescription_uses_stores}';
		var dental_chart_pref = '${genericPrefs.map.dental_chart}';
		var prescriptions_by_generics = '${ifn:cleanJavaScript(prescriptions_by_generics_js)}';
		var useGenerics = (use_store_items == 'Y' && prescriptions_by_generics == 'Y');
		var medDosages = <%= request.getAttribute("medDosages") %>;
		var itemFormList = <%= request.getAttribute("itemFormList") %>;
		var user = JSON.parse('${ifn:cleanJavaScript(user)}');
		var gItems = ${items};
		var gMax_centers_inc_default = ${genericPrefs.map.max_centers_inc_default};
		var pediac_tooth_details_json = ${pediac_tooth_details_json};
		var adult_tooth_image_details_json = ${adult_tooth_image_details_json};
		var doctorsJSON = <%= request.getAttribute("doctors") %>;
		var shadesListJSON = <%= request.getAttribute("shadesList") %>;
		var latestVisitCenterId = '${latest_visit_centerid_js}';
		var openAndFinalizedUnPaidBillsList = <%= request.getAttribute("openAndFinalizedUnPaidBillsJSON") %>;
		var jDiscountAuthorizers = <%= request.getAttribute("discountAuthorizersJSON") %>;
		var ItemSupplierList = <%= new flexjson.JSONSerializer().exclude("class").serialize(ConversionUtils.listBeanToListMap(
    		DentalSupplierItemRateMasterDAO.getItemSupplierList())) %>;
        var screenId = '${screenId}';
        var userId = '${ifn:cleanJavaScript(userId)}'
        var roleId = <%= Encoder.cleanJavaScript((String) request.getAttribute("roleId")) %>;
		var no_of_credit_debit_card_digits = ${empty genericPrefs.map.no_of_credit_debit_card_digits ? 0 : genericPrefs.map.no_of_credit_debit_card_digits};
		var orgId = '${orgId}' ;
		var userPermissibleDiscount = ${permissibleDiscountPercenatge};
		var ipDepositSetOff = 0;
		var generalDepositSetOff = 0;
		var availableDeposits = 0;
		var ipDeposits = 0;
		var availableRewardPoints = 0;
		var availableRewardPointsAmount = 0;
		var billRewardPoints = 0;
		var isMvvPackage  = 0;
		var visitType = '';
		var hasRewardPointsEligibility = false;
		var income_tax_cash_limit_applicability = '${genericPrefs.map.income_tax_cash_limit_applicability}';
		var cashTransactionLimitAmt =  ${empty cashTransactionLimit ? 0 : cashTransactionLimit};

	</script>
	<title>Dental Consultation - Insta HMS</title>
	<insta:js-bundle prefix="billing.salucro"/>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="script" file="outpatient/dentalchart.js"/>
	<insta:link type="script" file="outpatient/dentalconsultation.js"/>
	<insta:link type="script" file="billPaymentCommon.js"/>
	<insta:link file="SpryAssets/SpryCollapsiblePanel.js" type="js"/>
	<insta:link path="scripts/SpryAssets/SpryCollapsiblePanel.css" type="css" />
	<style>
		.yui-ac {
			padding-bottom: 20px;
		}
		.scrolForContainer .yui-ac-content{
			 max-height:11em;overflow:auto;overflow-x:auto; /* scrolling */
		}
		#condDoctorTable td {
			padding: 5px 0px 2px 5px;
		}
	</style>
	<insta:js-bundle prefix="laboratory.radiology.billpaymentcommon"/>
	<insta:js-bundle prefix="billing.billlist"/>
	
</head>

<jsp:useBean id="statusDisplay" class="java.util.HashMap"/>
<c:set target="${statusDisplay}" property="A" value="Open"/>
<c:set target="${statusDisplay}" property="F" value="Finalized"/>
<c:set target="${statusDisplay}" property="C" value="Closed"/>
<c:set target="${statusDisplay}" property="X" value="Cancelled"/>
<c:set target="${statusDisplay}" property="N" value="Patient No Show"/>

<jsp:useBean id="paymentStatusDisplay" class="java.util.HashMap"/>
<c:set target="${paymentStatusDisplay}" property="U" value="Unpaid"/>
<c:set target="${paymentStatusDisplay}" property="P" value="Paid"/>

<body onload="${empty param.emptyScreen ? 'init()' : ''};ajaxForPrintUrls();filterPaymentModes();" autocomplete="off">
	<h1 style="float: left">Dental Consultation</h1>
	<c:url var="searchUrl" value="/DentalConsultation/Consultation.do" />
	<insta:patientsearch searchType="mrNo" searchUrl="${searchUrl}" buttonLabel="Find" searchMethod="show"
		fieldName="mr_no" showStatusField="true"/>

	<insta:feedback-panel/>
	<insta:patientgeneraldetails mrno="${param.mr_no}" showClinicalInfo="true"/>
	<input type="hidden" name="bed_type" id="bed_type" value="GENERAL"/>
	<input type="hidden" name="org_id" id="org_id" value="${orgId}"/>
	<c:choose>
		<c:when test="${not empty param.emptyScreen}">
		</c:when>
		<c:otherwise>
			<form name="consultationForm" action="Consultation.do" method="POST" autocomplete="off">
				<input type="hidden" name="_method" value="update"/>
				<input type="hidden" name="isPrint" id="print" value="false"/>
				<input type="hidden" name="mr_no" id="mr_no" value="${patient.mr_no}"/>
				<input type="hidden" name="org_id_rate" id="org_id_rate" value="${orgId}"/>
				<c:if test="${genericPrefs.map.dental_chart == 'Y'}">
					<jsp:include page="DentalChartInclude.jsp">
						<jsp:param name="mr_no" value="${param.mr_no}"/>
					</jsp:include>
				</c:if>
				<div class="resultList">
					<fieldset class="fieldSetBorder">
						<legend class="fieldSetLabel">Management</legend>
						<table class="detailList dialog_displayColumns" cellspacing="0" cellpadding="0" id="itemsTable" border="0" width="100%">
							<tr>
								<th>Type</th>
								<th>Name</th>
								<th>Form</th>
								<th>Strength</th>
								<th>Admin Strength</th>
								<th>Details</th>
								<th>Route</th>
								<th>Remarks</th>
								<th>Qty</th>
								<th style="text-align: right">Pkg. Price</th>
								<th style="text-align: right">Unit Price</th>
								<th style="width: 16px;"></th>
								<th style="width: 16px"></th>
							</tr>
							<c:set var="numPrescriptions" value="${fn:length(prescriptions)}"/>
							<c:forEach begin="1" end="${numPrescriptions+1}" var="i" varStatus="loop">
								<c:set var="prescription" value="${prescriptions[i-1].map}"/>
								<%-- treat the partially prescribed items as fully prescribed items. --%>
								<c:set var="item_issued" value="${not empty prescription.issued ? prescription.issued : 'P'}"/>
								<c:set var="item_issued" value="${prescription.issued == 'PA' or prescription.issued == 'O' ? 'O' : prescription.issued}"/>
								<c:set var="flagColor">
									<c:choose>
										<c:when test="${item_issued == 'O'}">grey</c:when>
										<c:otherwise>empty</c:otherwise>
									</c:choose>
								</c:set>
								<c:if test="${empty prescription}">
									<c:set var="style" value='style="display:none"'/>
								</c:if>
								<tr ${style}>
									<td>
										<img src="${cpath}/images/${flagColor}_flag.gif"/>
										<label>${prescription.item_type}</label>
										<input type="hidden" name="item_prescribed_id" value="${prescription.item_prescribed_id}"/>
										<input type="hidden" name="itemType" value="${prescription.item_type}"/>
										<c:choose>
											<c:when test="${prescription.item_type == 'Medicine' && empty prescription.item_id}">
												<input type="hidden" name="item_name" value="<c:out value='${prescription.generic_name}'/>"/>
												<input type="hidden" name="item_id" value="${prescription.generic_code}"/>
											</c:when>
											<c:otherwise>
												<input type="hidden" name="item_name" value="<c:out value='${prescription.item_name}'/>"/>
												<input type="hidden" name="item_id" value="${prescription.item_id}"/>
											</c:otherwise>
										</c:choose>
										<input type="hidden" name="admin_strength" value="${prescription.admin_strength}"/>
										<input type="hidden" name="granular_units" value="${prescription.granular_units}"/>
										<input type="hidden" name="strength" value="${prescription.strength}"/>
										<input type="hidden" name="frequency" value="${ifn:cleanHtmlAttribute(prescription.frequency)}"/>
										<input type="hidden" name="duration" value="${prescription.duration}"/>
										<input type="hidden" name="duration_units" value="${prescription.duration_units}"/>
										<input type="hidden" name="medicine_quantity" value="${prescription.medicine_quantity}"/>
										<input type="hidden" name="item_remarks" value="${prescription.medicine_remarks}"/>
										<input type="hidden" name="item_master" value="${prescription.master}"/>
										<input type="hidden" name="ispackage" id="ispackage" value="${prescription.ispackage}"/>
										<input type="hidden" name="delItem" id="delItem" value="false" />
										<input type="hidden" name="issued" value="${item_issued}"/>
										<input type="hidden" name="generic_code" value="${prescription.generic_code}"/>
										<input type="hidden" name="generic_name" value="${prescription.generic_name}"/>
										<input type="hidden" name="edited" value='false'/>
										<input type="hidden" name="pkg_size" value=""/>
										<input type="hidden" name="pkg_price" value=""/>
										<input type="hidden" name="item_pkg_price" value=""/>
										<input type="hidden" name="item_unit_price" value=""/>
										<input type="hidden" name="route_id" value="${prescription.route_id}"/>
										<input type="hidden" name="route_name" value="${prescription.route_name}"/>
										<input type="hidden" name="cons_uom_id" value="${prescription.cons_uom_id}"/>
										<input type="hidden" name="consumption_uom" value="${prescription.consumption_uom}"/>
										<input type="hidden" name="qty_in_stock" value=""/>
										<input type="hidden" name="item_form_id" value="${prescription.item_form_id == 0 ? '' : prescription.item_form_id}"/>
										<input type="hidden" name="item_strength" value="${prescription.item_strength}"/>
										<input type="hidden" name="item_strength_units" value="${prescription.item_strength_units}"/>
										<input type="hidden" name="presc_by_generics" value="${(prescription.item_type == 'Medicine' && empty prescription.item_id) ? 'Y' : 'N'}" />
									</td>
									<td>
										<c:choose>
											<c:when test="${prescription.item_type == 'Medicine' && empty prescription.item_id}">
												<insta:truncLabel value="${prescription.generic_name}" length="20"/>
											</c:when>
											<c:otherwise>
												<insta:truncLabel value="${prescription.item_name}" length="20"/>
											</c:otherwise>
										</c:choose>
									</td>
									<td>
										<insta:truncLabel value="${prescription.item_form_name}" length="15"/>
									</td>
									<td>
										<insta:truncLabel value="${prescription.item_strength} ${prescription.unit_name}" length="15"/>
									</td>
									<td>
										<insta:truncLabel value="${prescription.admin_strength}" length="15"/>
									</td>
									<td>
										<c:if test="${prescription.item_type == 'Medicine' && (not empty prescription.frequency or not empty prescription.duration)}">
													<insta:truncLabel value="${prescription.frequency} / ${prescription.duration} ${prescription.duration_units}" length="20"/>
										</c:if>
									</td>
									<td>
										<label>
											<c:if test="${prescription.item_type == 'Medicine'}">
												${prescription.route_name}
											</c:if>
										</label>
									</td>
									<td>
										<insta:truncLabel value="${prescription.medicine_remarks}" length="30"/>
									</td>
									<td>
										<label>
											<c:if test="${prescription.item_type == 'Medicine'}">
												${prescription.medicine_quantity}
											</c:if>
										</label>
									</td>
										<!-- hide the unit price and pkg price when prescriptions done by generics -->
									<td class="number"><label></label></td>
									<td class="number"><label></label></td>
									<td style="text-align: center">
										<c:choose>
											<c:when test="${item_issued == 'Y'}">
												<img src="${cpath}/icons/delete_disabled.gif"" class="imgDelete button" />
											</c:when>
											<c:otherwise>
												<a href="javascript:Cancel Item" onclick="return cancelItem(this);" title="Cancel Item" >
													<img src="${cpath}/icons/delete.gif" class="imgDelete button" />
												</a>
											</c:otherwise>
										</c:choose>
									</td>
									<td style="text-align: center">
										<a name="_editAnchor" href="javascript:Edit" onclick="return showEditItemDialog(this);"
											title="Edit One Time Prescription Details">
											<img src="${cpath}/icons/Edit.png" class="button" />
										</a>
									</td>
								</tr>
							</c:forEach>
						</table>
						<table class="addButton" style="height: 25px;">
							<tr>

								<td class="number" style="text-align: right">
									Pkg. Price Total: <b><label id="estimatedPkgTotal"></label></b>
									&nbsp;&nbsp;Unit Price Total: <b><label id="estimatedUnitTotal"></label></b>
								</td>
								<td style="width: 16px;text-align: right">
									<button type="button" name="btnAddItem" id="btnAddItem" title="Add One Time Prescription (Alt_Shift_+)"
										onclick="showAddItemDialog(this); return false;" ${madd_disable ? 'disabled' : ''}
										accesskey="+" class="imgButton">
										<c:choose>
											<c:when test="${madd_disable}"><img src="${cpath}/icons/Add1.png"/></c:when>
											<c:otherwise><img src="${cpath}/icons/Add.png"/></c:otherwise>
										</c:choose>
									</button>
								</td>
							</tr>
						</table>
					</fieldset>
				</div>
				<fieldset class="fieldSetBorder">
					<legend class="fieldSetLabel">Filter On</legend>
					<table width="100%">
						<tr>
							<td>Status: </td>
							<td><select class="dropdown" name="flt_treatment_status" id="flt_treatment_status" onchange="filterResults(this);">
									<option value="">-- All --</option>
									<option value="P">Planned</option>
									<option value="I">In Progress</option>
									<option value="C">Completed</option>
									<option value="X">Cancelled</option>
									<option value="N">Patient No Show</option>
								</select></td>
							<td>for Tooth: </td>
							<td><select class="dropdown" name="flt_tooth_number" id="flt_tooth_number" onchange="filterResults(this);">
								</select></td>
							<td>
							<td>and for Duration:</td>
							<td><select class="dropdown" name="flt_duration" id="flt_duration" onchange="filterResults(this);">
									<option value="">-- Any --</option>
									<option value="3">Last 3 months onwards</option>
									<option value="6">Last 6 months onwards</option>
									<option value="12">Last Year onwards</option>
								</select>
							</td>
							<td>
								From : <insta:datewidget name="flt_from_date" id="flt_from_date" extravalidation="filterResults(this)"/>
								To: <insta:datewidget name="flt_to_date" id="flt_to_date" extravalidation="filterResults(this);"/>
							</td>
						</tr>
					</table>
				</fieldset>

				<fieldset class="fieldSetBorder">
					<legend class="fieldSetLabel">Treatment Details</legend>
					<table id="condDoctorTable" style="margin-bottom: 10px; margin-top:10px">
						<tr>
							<td >Conducting Doctor: </td>
							<td>
								<div id="apply_cond_doctor_for_selected_ac" style="width: 138px;">
									<input type="text" id="apply_cond_doctor_for_selected" value=""/>
									<div id="apply_cond_doctor_for_selected_container" class="scrolForContainer" style="width: 350px"></div>
								</div>
								<input type="hidden" id="apply_cond_doctor_id_for_selected" value=""/>
							</td>
							<td>
								<input type="button" id="apply_cond_doctor" value="Apply" onclick="return applyCondDoctor()"/>
							</td>
						</tr>
					</table>
					<div class="resultList">
						<table class="detailList dialog_displayColumns" cellspacing="0" cellpadding="0" id="trtmtDetails" border="0" width="100%">
							<tr>
								<th><input type="checkbox" onclick="selectAllTreatForOrderAndFinalize()" id="orderAndFinalizeAll" autocompelte="off"/></th>
								<th>T. No.</th>
								<th>Service</th>
								<th>Status</th>
								<th>Pln By</th>
								<th>Pln Date</th>
								<th>Start Date</th>
								<th><input type="checkbox" onclick="return checkOrUncheckAll('cond_doctor_chk', this)" id="cond_doctor_chk_all" autocomplete="off"/>Cond. By</th>
								<th>Compl Date</th>
								<th>Bill No</th>
								<th>Rate</th>
								<th>Pln qty</th>
								<th>Disc</th>
								<th>Amt</th>
								<th>Comments</th>
								<th>Task Status</th>
								<th style="width: 16px"></th>
								<th style="width: 16px"></th>
								<th style="width: 16px"></th>
							</tr>
							<c:set var="numTreatments" value="${fn:length(treatments)}"/>
							<c:forEach begin="1" end="${numTreatments+1}" var="i" varStatus="loop">
								<c:set var="treatment" value="${treatments[i-1].map}"/>
								<c:if test="${empty treatment}">
									<c:set var="tr_style" value='style="display:none"'/>
								</c:if>
								<tr ${tr_style}>
									<td>
										<c:set var="disabled" value=""/>
										<c:choose>
											<c:when test="${not empty treatment.service_prescribed_id}">
												<c:set var="disabled" value="disabled"/>
											</c:when>
											<c:when test="${treatment.treatment_status == 'C' || treatment.treatment_status == 'X'}">
												<c:set var="disabled" value="disabled"/>
											</c:when>
										</c:choose>
										<input type="checkbox" name="orderAndFinalizeCheck" value="${treatment.treatment_id}" ${disabled} onclick="enableDisableSave(this, '');" autocompelte="off"/>
										<input type="hidden" name="order_this_service" id="order_this_service" value=""/>
										<input type="hidden" name="planned_by_doctor" id="planned_by_doctor" value=""/>
										<input type="hidden" name="order_treatment_id" id="order_treatment_id" value=""/>
										<input type="hidden" name="order_qty" id="order_qty" value=""/>
										<input type="hidden" name="tooth_number" id="tooth_number" value=""/>
										<input type="hidden" name="conducting_doctor" id="conducting_doctor" value=""/>
										<input type="hidden" name="overall_discount_auth_name" id="overall_discount_auth_name" value=""/>
										<input type="hidden" name="overall_discount_auth" id="overall_discount_auth" value=""/>
										<input type="hidden" name="overall_discount_amt" id="overall_discount_amt" value=""/>
									</td>
									<td>
										<fmt:formatDate pattern="dd-MM-yyyy HH:mm" var="planned_date" value="${treatment.planned_date}"/>
										<fmt:formatDate pattern="dd-MM-yyyy HH:mm" var="completed_date" value="${treatment.completed_date}"/>
										<fmt:formatDate pattern="dd-MM-yyyy HH:mm" var="creation_date" value="${treatment.creation_date}"/>
										<fmt:formatDate pattern="dd-MM-yyyy HH:mm" var="start_date" value="${treatment.start_date}"/>

										<c:set var="tooth_number"
												value="${empty treatment.tooth_unv_number ? treatment.tooth_fdi_number : treatment.tooth_unv_number}"/>
										<insta:truncLabel value="${tooth_number}" length="10"/>
										<input type="hidden" name="h_treatment_id" value="${treatment.treatment_id}"/>
										<input type="hidden" name="h_tooth_unv_number" value="${treatment.tooth_unv_number}"/>
										<input type="hidden" name="h_tooth_fdi_number" value="${treatment.tooth_fdi_number}"/>
										<input type="hidden" name="h_planned_date" value="${planned_date}"/>
										<input type="hidden" name="h_service_id" value="${treatment.service_id}"/>
										<input type="hidden" name="h_service_idx" value="${i-1}" />
										<input type="hidden" name="h_doc_speciality_id" value="${treatment.doc_speciality_id}"/>
										<input type="hidden" name="h_treatment_status" value="${treatment.treatment_status}"/>
										<input type="hidden" name="h_service_charge" value="${treatment.charge}"/>
										<input type="hidden" name="h_service_discount" value="${treatment.discount}">
										<input type="hidden" name="h_planned_by" value="${treatment.planned_by}"/>
										<input type="hidden" name="h_planned_by_name" value="${treatment.planned_by_name}">
										<input type="hidden" name="h_completed_date" value="${completed_date}"/>
										<input type="hidden" name="h_completed_by" value="${treatment.completed_by}"/>
										<input type="hidden" name="h_completed_by_name" value="${treatment.completed_by_name}">
										<input type="hidden" name="h_comments" value="${treatment.comments}">
										<input type="hidden" name="h_service_name" value="${treatment.service_name}"/>
										<input type="hidden" name="h_service_group_name" value="${treatment.service_group_name}"/>
										<input type="hidden" name="h_service_presc_id" value="${treatment.service_prescribed_id}"/>
										<input type="hidden" name="h_tooth_num_required" value="${treatment.tooth_num_required}"/>
										<input type="hidden" name="h_creation_date" value="${creation_date}"/>
										<input type="hidden" name="h_start_date" value="${start_date}"/>
										<input type="hidden" name="h_qty" value="${treatment.qty}"/>
										<input type="hidden" name="h_planned_qty" value="${treatment.planned_qty}"/>
										<input type="hidden" name="h_conducting_doc_mandatory" value="${ifn:cleanHtmlAttribute(treatment.conducting_doc_mandatory)}"/>
										<input type="hidden" name="ht_edited" id="ht_edited" value="false"/>
										<input type="hidden" name="ht_delete" id="h_delete" value="false"/>
										<input type="hidden" name="h_den_supplies_flag" id="h_den_supplies_flag" value="oldDen">
										<input type="hidden" name="h_order_index" value="">
										<div></div>
									</td>
									<td><insta:truncLabel value="${treatment.service_name} (${treatment.service_group_name})" length="20"/></td>
									<td>
										<label>
											<c:choose>
												<c:when test="${treatment.treatment_status == 'P'}">
													Planned
												</c:when>
												<c:when test="${treatment.treatment_status == 'I'}">
													In Progress
												</c:when>
												<c:when test="${treatment.treatment_status == 'C'}">
													Completed
												</c:when>
												<c:when test="${treatment.treatment_status == 'X'}">
													Cancelled
												</c:when>
												<c:when test="${treatment.treatment_status == 'N'}">
													Patient No Show
												</c:when>
											</c:choose>
										</label>
									</td>
									<td><insta:truncLabel value="${treatment.planned_by_name}" length="10"/></td>
									<td><label>${planned_date}</label></td>
									<td><label>${start_date}</label></td>
									<td>
										<input type="checkbox" name="cond_doctor_chk" autocomplete="off"/>
										<insta:truncLabel value="${treatment.completed_by_name}" length="10"/>
									</td>
									<td><label>${completed_date}</label></td>
									<td>
										<c:if test="${not empty treatment.service_prescribed_id && not empty treatment.bill_no}">
											<insta:screenlink screenId="credit_bill_collection" extraParam="?_method=getCreditBillingCollectScreen&billNo=${treatment.bill_no}"
												label="Bill ${treatment.bill_no}" target="_blank"/>
										</c:if>
									</td>
									<c:set var="flag" value="false"/>
									<c:if test="${not empty treatment.service_prescribed_id}">
										<c:set var="flag" value="true"/>
									</c:if>
									<td class="number"><label>${flag ? treatment.rate : treatment.charge}</label>
										<input type="hidden" name="rate" value="${flag ? treatment.rate : treatment.charge}"/>
									</td>
									<td>
										<label>${treatment.planned_qty}</label>
										<input type="hidden" name="qty" value="${flag ? treatment.ordered_qty : treatment.qty}"/>
									</td>
									<td class="number"><label>${flag ? treatment.disc : (treatment.discount*treatment.qty)}</label>
										<input type="hidden" name="disc" value="${flag ? treatment.disc : (treatment.discount*treatment.qty)}"/>
									</td>
									<td class="number"><label>${flag ? treatment.amt : (treatment.charge - treatment.discount)*treatment.qty}</label>
										<input type="hidden" name="amt" value="${flag ? treatment.amt : (treatment.charge - treatment.discount)*treatment.qty}"/>
									</td>
									<td><insta:truncLabel value="${treatment.comments}" length="20"/></td>
									<td>
										<c:set var="hasTasks" value="false"/>
										<c:set var="compl_count" value="0"/>
										<c:set var="noOfTasks" value="${fn:length(service_sub_tasks[treatment.treatment_id])}"/>
										<c:forEach var="task" items="${service_sub_tasks[treatment.treatment_id]}">
											<c:if test="${not empty task.map.sub_task_id}">
												<c:set var="hasTasks" value="true"/>
												<c:set var="compl_count" value="${task.map.status == 'C' || task.map.status == 'NR' ? compl_count+1 : compl_count}"/>
											</c:if>
										</c:forEach>
										<c:set var="allSubTasksStatus">
											<c:choose>
												<c:when test="${empty treatment || hasTasks}">
														<c:choose >
															<c:when test="${compl_count == noOfTasks}">
																Completed
															</c:when>
															<c:when test="${compl_count > 0 && compl_count < noOfTasks}">
																Partial
															</c:when>
															<c:when test="${compl_count == 0}">
																Not Completed
															</c:when>
														</c:choose>
												</c:when>
												<c:otherwise>
													None
												</c:otherwise>
											</c:choose>
										</c:set>
										<label>${allSubTasksStatus}</label>
										<input type="hidden" name="allSubTasksStatus" value="${allSubTasksStatus}"/>
									</td>
									<td style="text-align: center">
										<c:choose>
											<c:when test="${not empty treatment.service_prescribed_id}">
												<img src="${cpath}/icons/delete_disabled.gif"" class="imgDelete button" />
											</c:when>
											<c:otherwise>
												<a href="javascript:Cancel Item" onclick="return cancelTrtmtItem(this);" title="Cancel Item" >
													<img src="${cpath}/icons/delete.gif" class="imgDelete button" />
												</a>
											</c:otherwise>
										</c:choose>
									</td>
									<td style="text-align: center">
										<input type="hidden" name="delPayment" id="delPayment" value="false" />
										<a name="_trtmtEditAnchor" href="javascript:Edit" onclick="return showEditTrtmtItemDialog(this);"
											title="Edit Treatment Details">
											<img src="${cpath}/icons/Edit.png" class="button" />
										</a>
									</td>

									<td style="text-align: center">
										<a style="display: ${hasTasks || empty treatment ? 'block' : 'none'}" name="_taskEditAnchor" href="javascript:Edit" onclick="return showSubTaskDialog(this);"
													title="Edit Sub Task Details">
											<img src="${cpath}/icons/Choose.png" class="button" />
										</a>
										<img src="${cpath}/icons/Choose1.png" class="button" name="disabledTaskIcon" style="display: ${hasTasks || empty treatment ? 'none' : 'block'}"/>
									</td>
								</tr>
							</c:forEach>
							<%-- one row for apply discounts and add button --%>
								<c:set var="discButtonNeeded" value="${(actionRightsMap.allow_discount == 'A')||(roleId==1)||(roleId==2)}"/>
						</table>
						<table class="addButton" style="height: 25px;">
							<tr>
								<td>
									<%-- we need the row to be present for getNumCharges calculation to be correct,
									even if the buttons are not being shown. --%>
									<table class="footerTable">
										<tr>
											<c:if test="${discButtonNeeded}">
												<td>
													Discount on selected items:
													<select class="dropdown" name="itemDiscType" style="width: 70px">
														<option value='P'>Percent</option>
														<option value='R'>Amount</option>
													</select>
													<input type="text" name="itemDiscPer" id="itemDiscPer" value="0" size="3"
													onchange="return validateDiscPer();" style="text-align:right;width:60px" />

												</td>
												<td>Discount Auth:</td>
												<td>
													<select name="discountAuthName" id="discountAuthName" class="dropdown" >
														<option value="">-- Select --</option>
														<c:forEach var="discAuth" items="${discountAuthorizers}">
															<option value="${discAuth.map.disc_auth_id}">${discAuth.map.disc_auth_name}</option>
														</c:forEach>
													</select>
													<input type="hidden" name="discAuth" id="discAuth" value=""/>
												</td>
											     <td>
													<input type="button" value="Apply" name="itemDiscPerApply"
															title="Replace existing discount with new value" onclick="onApplyItemDiscPer();">
												</td>
											</c:if>
										</tr>
									</table>
								</td>
								<td style="width: 16px;text-align: right">
									<button type="button" name="btnTrtmtAddItem" id="btnTrtmtAddItem" title="Add Treatment"
										onclick="showAddTrtmtItemDialog(this); return false;"
										class="imgButton"><img src="${cpath}/icons/Add.png"></button>
								</td>
							</tr>
						</table>
					</div>
				</fieldset>

				<table id="serviceSubTasksTab" style="display: none">
					<c:forEach begin="1" end="${numTreatments}" var="i" varStatus="loop">
						<c:set var="treatment" value="${treatments[i-1].map}"/>
						<c:forEach var="task" items="${service_sub_tasks[treatment.treatment_id]}">
							<tr>
								<td>
									<fmt:formatDate var="st_completion_date" value="${task.map.completion_time}" pattern="dd-MM-yyyy"/>
									<fmt:formatDate var="st_completion_time" value="${task.map.completion_time}" pattern="HH:mm"/>
									<input type="hidden" name="task_presc_id_${i-1}" value="${empty task.map.task_presc_id ? '_' : task.map.task_presc_id}"/>
									<input type="hidden" name="sub_task_id_${i-1}" value="${empty task.map.sub_task_id ? '_' : task.map.sub_task_id}"/>
									<input type="hidden" name="st_treatment_id_${i-1}" value="${task.map.treatment_id}"/>
									<input type="hidden" name="st_service_id_${i-1}" value="${task.map.service_id}"/>
									<input type="hidden" name="st_username_${i-1}" value="${task.map.doctor_name}"/>
									<input type="hidden" name="st_completion_date_${i-1}" value="${st_completion_date}"/>
									<input type="hidden" name="st_completion_time_${i-1}" value="${st_completion_time}"/>
									<input type="hidden" name="st_status_${i-1}" value="${task.map.status}"/>
									<input type="hidden" name="sub_task_name_${i-1}" value="${task.map.desc_short}"/>
									<input type="hidden" name="task_completed_by_${i-1}" value="${task.map.completed_by}">
								</td>
							</tr>
						</c:forEach>
					</c:forEach>
				</table>
				<div style="margin-top: 10px; float: left">
					<input type="button" value="Save" name="save" onclick="return onSave(false);">
					<input type="button" value="Save & Print" name="saveAndPrint" onclick="return onSave(true)">
					<input type="button" value="Print Quotation" name="printQuotation" onclick="printTreatmentQuotation();"/>
					<input type="button" value="Order & Finalize Bill" name="orderAndFinalizeBills" onclick="orderServicesAndFinalizeBills();"/>
					<insta:screenlink screenId="patient_dental_supplies" extraParam="?_method=show&mr_no=${patient.mr_no}"
						label="Dental Supplies" addPipe="true"/>
					<insta:screenlink screenId="patient_progress" extraParam="?_method=show&mr_no=${patient.mr_no}" addPipe="true"
						label="Patient Progress Notes"/>
					<c:if test="${not empty latestVisit && patient.visit_type == 'o'}">
						<insta:screenlink screenId="new_op_order" addPipe="true" label="Order"
							extraParam="/index.htm#/filter/default/patient/${ifn:encodeUriComponent(patient.mr_no)}/order/visit/${ifn:encodeUriComponent(latestVisit.map.patient_id)}?retain_route_params=true"/>
					</c:if>
					<c:if test="${not empty latestVisit && patient.visit_type == 'i'}">
						<insta:screenlink screenId="new_ip_order" addPipe="true" label="Order"
							extraParam="/index.htm#/filter/default/patient/${ifn:encodeUriComponent(patient.mr_no)}/order/visit/${ifn:encodeUriComponent(latestVisit.map.patient_id)}?retain_route_params=true"/>
					</c:if>
					<c:if test="${not empty latestConsultation}">
						| <a href="${cpath}/outpatient/OpPrescribeAction.do?_method=list&consultation_id=${latestConsultation.map.consultation_id}"
								title="Consultation and Management" >Consultation and Management</a>
					</c:if>
				</div>
				<div style="margin-top: 10px; float: right; ">
					<c:set var="templateValues" value="BUILTIN_HTML,BUILTIN_TEXT"/>
					<c:set var="templateTexts" value="Built-in Default HTML template, Built-in Default Text template"/>
					<c:if test="${not empty printTemplate}">
						<c:forEach var="temp" items="${printTemplate}">
							<c:set var="templateValues" value="${templateValues},${temp.template_name}"/>
							<c:set var="templateTexts" value="${templateTexts},${temp.template_name}"/>
						</c:forEach>
					</c:if>
					<insta:selectoptions name="printTemplate" id="templateList" opvalues="${templateValues}"
														optexts="${templateTexts}" value="${templateName}"/>
					<insta:selectdb name="printerId" table="printer_definition" class="dropdown"
										valuecol="printer_id"  displaycol="printer_definition_name"
										value="${printerDef}"/>
				</div>
				<c:if test="${fn:length(openAndFinalizedUnPaidBillsList) > 0}">
					<div class="resultList" style="padding-top: 10px">
						<div id="CollapsiblePanel" class="CollapsiblePanel">
							<div class=" title CollapsiblePanelTab"  style=" border-left:none;" >
								<div class="fltL " style="width: 230px; margin:5px 0px 0px 10px;"><b><i>Pending Payment Bills</i></b></div>
								<div class="fltR txtRT" style="width: 25px; margin:-10px 10px 0px 680px;">
									<img src="${cpath}/images/down.png" />
								</div>
								<div class="clrboth"></div>
							</div>
							<div style="width: 100%">
							<table class="resultList dialog_displayColumns" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
								<tr onmouseover="hideToolBar();">
									<th style="text-align: right">#</th>
									<insta:sortablecolumn name="h_bill_no" title="Bill No"/>
									<insta:sortablecolumn name="h_open_date" title="Open Date"/>
									<insta:sortablecolumn name="h_finalized_date" title="Finalized Date"/>
									<insta:sortablecolumn name="h_status" title="Status"/>
									<th align="right">Bill Amt</th>
									<th align="right">Pat Amt</th>
									<th align="right">Disc.</th>
									<insta:sortablecolumn name="h_payment_status" title="Pmt Status" tooltip="Payment Status"/>
									<th align="right" title="Total Paid Amount">Tot Paid</th>
									<th align="right" title="Total Due Amount">Tot Due</th>
									<th>
										<input type="hidden" id="billNo" name="billNo" value=""/>
										<input type="hidden" id="billType" name="billType" value=""/>
										<input type="hidden" id=totalAmt name="totalAmt" value=""/>
										<input type="hidden" id="totalRec" name="totalRec" value=""/>
										<input type="hidden" id="totalCla" name="totalCla" value=""/>
										<input type="hidden" id="totalDis" name="totalDis" value=""/>
										<input type="hidden" id="totalDue" name="totalDue" value=""/>
										<input type="hidden" id="visitId" name="visitId" value=""/>
									</th>
								</tr>
	                             <c:forEach var="bill" items="${openAndFinalizedUnPaidBillsList}" varStatus="st">
									<tr id="billTr" class="">
										<td>${st.index + 1}</td>
										<td <c:if test="${not empty bill.map.remarks}">class="remarkIndicator" title="<c:out value='${bill.map.remarks}'/>"</c:if>>
											<insta:screenlink screenId="credit_bill_collection" extraParam="?_method=getCreditBillingCollectScreen&billNo=${bill.map.bill_no}"
													label="Bill ${bill.map.bill_no}" target="_blank"/>
										</td>
										<td><fmt:formatDate value="${bill.map.open_date}" pattern="dd-MM-yyyy HH:mm"/></td>
										<td><fmt:formatDate value="${bill.map.finalized_date}" pattern="dd-MM-yyyy HH:mm"/></td>
										<td><label title="${bill.map.status}">${statusDisplay[bill.map.status]}</label></td>
										<td style="text-align: right;">${bill.map.total_amount}</td>
										<td style="text-align: right;">${bill.map.total_amount - bill.map.primary_total_claim - bill.map.secondary_total_claim}</td>
										<td style="text-align: right;">${bill.map.total_discount}</td>
										<td>${paymentStatusDisplay[bill.map.payment_status]}</td>
										<c:set var="totalPaidAmt" value ="${bill.map.total_receipts}"/>
										<td style="text-align: right;">${totalPaidAmt}</td>
										<td style="text-align: right;">${bill.map.total_amount - totalPaidAmt - bill.map.points_redeemed_amt}</td>
										<td><input type="button" id="initiate_payment" name="initiate_payment" value="Initiate Payment"
											autocomplete="off" onclick="return initiatePaymentForBill(this, '${bill.map.bill_no}');" ${(not empty billingcounterId) ? '' : 'disabled'}/>
										</td>
									</tr>
								</c:forEach>
							</table>
							</div>
						</div>
						<script type="text/javascript">
							var CollapsiblePanel = new Spry.Widget.CollapsiblePanel("CollapsiblePanel", {contentIsOpen:false});
						</script>
					</div>
				</c:if>
				<div id="addPaymentsDiv" style="display: none; padding-top: 10px;">
					<dl class="accordion" style="margin-bottom: 10px;">
						<dt>
							<span id="paySec"></span>
							<div class="clrboth"></div>
						</dt>
						<dd id="payDD" class="">
							<div class="bd" id="paymentTag">
								<insta:billPaymentDetails formName="consultationForm" isPaymentTypeByJs="${paymentTypeByJs}" />
							</div>
						</dd>
					</dl>
					<table class="formtable" width="100%">
						<tr>
							<td id="recLink">
								<button type="button" name="paySave" accesskey="P" onclick="return payAndPrintDetails();"><b><u>P</u></b>ay & Print</button>
								<button type="button" name="PayClose" accesskey="C" onclick="return canclePayment();"><b><u>C</u></b>lose</button>
							</td>
							<td>
							<c:if test="${billPrintRights == 'A' || roleId == 1 || roleId == 2}">
								<div style="margin-top: 10px; float: right; ">
									<select name="printBill" id="printSelect" class="dropdown"
											onchange="loadTemplates(this)">
										<c:forEach var="template" items="${availableTemplates}">
											<option value="${fn:escapeXml(template.map.template_id)}"
											${(template.map.template_id == billPrintDefault) ? 'selected' : ''}>
												<c:out value="${template.map.template_name}"/>
											</option>
										</c:forEach>
									</select>
									<insta:selectdb name="printType" table="printer_definition" valuecol="printer_id"  displaycol="printer_definition_name"
										value="${genericPrefs.map.default_printer_for_bill_now}"/>
								</div>
							</c:if>
							</td>
						</tr>
					</table>
				</div>
				<div id="addItemDialog" style="display: none">
					<div class="bd">
						<div id="addItemDialogFields">
							<fieldset class="fieldSetBorder">
								<legend class="fieldSetLabel">Add</legend>
								<fieldset >
									<legend class="fieldSetLabel">Item Type</legend>
									<table>
										<tr>
											<td ><input type="radio" name="d_itemType" value="Medicine" checked onclick="onItemChange()"> Medicine</td>
											<td ><input type="radio" name="d_itemType" value="Inv." onclick="onItemChange()"> Investigation</td>
											<td ><input type="radio" name="d_itemType" value="Service" onclick="onItemChange()"> Service</td>
										</tr>
									</table>
								</fieldset>
								<fieldset class="fieldSetBorder">
									<legend class="fieldSetLabel">Item Details</legend>
									<table class="formtable">
										<tr>
											<td class="formlabel">Item: </td>
											<td colspan="5">
												<div id="itemAutocomplete" style="padding-bottom: 20px; width: 443px">
													<input type="text" id="d_itemName" name="d_itemName" >
													<div id="itemContainer" style="width: 550px" class="scrolForContainer"></div>
													<input type="hidden" name="d_item_master" id="d_item_master" value=""/>
													<input type="hidden" name="d_generic_name" id="d_generic_name" value=""/>
													<input type="hidden" name="d_generic_code" id="d_generic_code" value=""/>
													<input type="hidden" name="d_package_size" id="d_package_size" value=""/>
													<input type="hidden" name="d_price" id="d_price" value=""/>
													<input type="hidden" name="d_item_id" id="d_item_id" value=""/>
													<input type="hidden" name="d_qty_in_stock" id="d_qty_in_stock" value=""/>
													<input type="hidden" name="d_ispackage" id="d_ispackage" value=""/>
													<input type="hidden" name="d_granular_units" id="d_granular_units" value=""/>
												</div>
											</td>
										</tr>
										<tr >
											<td class="formlabel">Hospital Item: </td>
											<td ><b><label id="d_itemMasterType" style="width: 20px"></label></b>
											</td>
											<td class="formlabel">Generic: </td>
											<td colspan="3"><a id="genericNameAnchor_dialog" style="display: none"></a></td>
										</tr>
										<tr>
											<td class="formlabel">Pkg. Size: </td>
											<td><b><label id="d_pkg_size_label"></label></b></td>
											<td class="formlabel">Price: </td>
											<td><b><label id="d_price_label"></label></b></td>
											<td class="formlabel"></td>
											<td></td>
										</tr>
										<tr id="d_itemFormRow">
											<td class="formlabel">Item Form: </td>
											<td><insta:selectdb name="d_item_form_id" id="d_item_form_id" table="item_form_master"
												displaycol="item_form_name" valuecol="item_form_id" filtercol="status" filtervalue="A" orderby="item_form_name"
												dummyvalue="-- Select --" dummyvalueId="" onchange="setGranularUnit(event, 'd');"/>
											</td>
											<td class="formlabel">Strength</td>
											<td>
												<div style="width: 138px">
													<input type="text" name="d_item_strength" id="d_item_strength" value="" style="width: 60px">
													<insta:selectdb name="d_item_strength_units" id="d_item_strength_units" table="strength_units" displaycol="unit_name" valuecol="unit_id"
														dummyvalue="Select --" style="width: 60px"/>
												</div>
											</td>
											<td class="formlabel"></td>
											<td></td>
										</tr>
									</table>
								</fieldset>
								<fieldset class="fieldSetBorder">
									<legend class="fieldSetLabel">Management</legend>
									<table class="formtable">
										<tr>
											<td class="formlabel">Admin Strength: </td>
											<td>
												<input type="text" name="d_admin_strength" id="d_admin_strength" value="" maxlength="98"/>
											</td>
											<td></td>
											<td></td>
											<td class="formlabel">Route: </td>
											<td>
												<c:choose>
													<c:when test="${prescriptions_by_generics}">
														<insta:selectdb name="d_medicine_route" id="d_medicine_route" table="medicine_route" valuecol="route_id" dummyvalue="-- Select --"
															displaycol="route_name" />
													</c:when>
													<c:otherwise >
														<select id="d_medicine_route" class="dropdown">
															<option value="">-- Select --</option>
														</select>
													</c:otherwise>
												</c:choose>
											</td>
										</tr>
										<tr>
											<td class="formlabel">Number Of Units (Dosage): </td>
											<td colspan="5">
												<input type="text" name="d_strength" id="d_strength" value="" />
												<input type="text" name="d_consumption_uom" id="d_consumption_uom" value="" onchange="modifyUOMLabel(this, 'd')"/> Granular Units
											</td>
										</tr>
										<tr>
											<td class="formlabel">Duration: </td>
											<td>
												<input type="text" name="d_duration" id="d_duration" value="" onkeypress="return enterNumOnlyzeroToNine(event);" onchange="return calcQty('d');"/>
											</td>
											<td colspan="2" >
												<div style="width: 190px">
													<input type="radio" name="d_duration_units" value="D" onchange="return calcQty('d');">Days
													<input type="radio" name="d_duration_units" value="W" onchange="return calcQty('d');">Weeks
													<input type="radio" name="d_duration_units" value="M" onchange="return calcQty('d');">Months
												</div>
											</td>
											<td class="formlabel">Frequency: </td>
											<td>
												<div id="frequencyAutoComplete" style="padding-bottom: 20px; width: 138px;">
													<input type="text" name="d_frequency" id="d_frequency" maxlength="150">
													<div id="frequencyContainer"></div>
													<input type="hidden" name="d_per_day_qty" id="d_per_day_qty" value=""/>
												</div>
											</td>
										</tr>
										<tr>
											<td class="formlabel">Total Qty: </td>
											<td><input type="text" name="d_qty" id="d_qty" value="" maxlength="5" onkeypress="return enterNumOnlyzeroToNine(event);"/></td>
											<td colspan="2"><label id="d_consumption_uom_label"></label></td>
											<td class="formlabel"></td>
											<td></td>
										</tr>
										<tr>
											<td class="formlabel" >Remarks: </td>
											<td colspan="5"><input type="text" name="d_remarks" id="d_remarks" value="" style="width: 443px"></td>
										</tr>

									</table>
							</fieldset>
						</div>
						<table style="margin-top: 10">
							<tr>
								<td>
									<button type="button" name="Add" id="Add" accesskey="A" >
										<b><u>A</u></b>dd
									</button>
									<input type="button" name="Close" value="Close" id="Close"/>
								</td>
							</tr>
						</table>
					</div>
				</div>

				<div id="editItemDialog" style="display: none">
					<input type="hidden" name="editRowId" id="editRowId" value=""/>
					<div class="bd">
						<fieldset class="fieldSetBorder">
							<legend class="fieldSetLabel">Edit Item</legend>
							<fieldset class="fieldSetBorder">
								<legend class="fieldSetLabel">Item Details</legend>
								<table class="formtable">
									<tr>
										<td class="formlabel">Type: </td>
										<td>
											<label id="ed_itemTypeLabel" style="font-weight: bold"></label>
											<input type="hidden" id="ed_itemType" name="ed_itemType" value=""/>
										</td>
									</tr>
									<tr>
										<td class="formlabel">Item: </td>
										<td colspan="5">
											<label id="ed_itemNameLabel" style="font-weight: bold"></label>
											<input type="hidden" id="ed_itemName" name="ed_itemName" value="">
											<input type="hidden" id="ed_item_master" name="ed_item_master" value=""/>
											<input type="hidden" id="ed_ispackage" name="ed_ispackage" value=""/>
											<input type="hidden" name="ed_package_size" id="ed_package_size" value=""/>
											<input type="hidden" name="ed_price" id="ed_price" value=""/>
											<input type="hidden" name="ed_item_id" id="ed_item_id" value=""/>
											<input type="hidden" name="ed_granular_units" id="ed_granular_units" value=""/>
										</td>
									</tr>
									<tr>
										<td class="formlabel">Hospital Item: </td>
										<td ><b><label id="ed_itemMasterType"></label></b></td>
										<td class="formlabel">Generic: </td>
										<td colspan="3"><a id="genericNameAnchor_editdialog" style="display:none"></a></td>
									</tr>
									<tr>
										<td class="formlabel">Pkg. Size: </td>
										<td><b><label id="ed_pkg_size_label"></label></b></td>
										<td class="formlabel">Price: </td>
										<td><b><label id="ed_price_label"></label></b></td>
										<td class="formlabel"></td>
										<td></td>
									</tr>
									<tr id="ed_itemFormRow">
										<td class="formlabel">Item Form: </td>
										<td><insta:selectdb name="ed_item_form_id" id="ed_item_form_id" table="item_form_master"
											displaycol="item_form_name" valuecol="item_form_id" filtercol="status" filtervalue="A" orderby="item_form_name"
											dummyvalue="-- Select --" dummyvalueId="" onchange="setGranularUnit(event, 'ed');"/>
										</td>
										<td class="formlabel">Strength</td>
										<td>
											<div style="width: 138px">
												<input type="text" name="ed_item_strength" id="ed_item_strength" value="" style="width: 60px">
												<insta:selectdb name="ed_item_strength_units" id="ed_item_strength_units" table="strength_units" displaycol="unit_name" valuecol="unit_id"
													dummyvalue="Select --" style="width: 60px" onchange="setEdited();"/>
											</div>
										</td>
										<td class="formlabel"></td>
										<td></td>
									</tr>
								</table>
							</fieldset>
							<fieldset class="fieldSetBorder">
								<legend class="fieldSetLabel">Management</legend>
								<table class="formtable">
									<tr>
										<td class="formlabel">Admin Strength: </td>
										<td>
											<input type="text" name="ed_admin_strength" id="ed_admin_strength" value="" onchange="setEdited();" maxlength="98"/>
										</td>
										<td></td>
										<td></td>
										<td class="formlabel">Route: </td>
										<td><b><label id="ed_medicine_route"></label></b></td>
									</tr>
									<tr>
										<td class="formlabel">Number Of Units (Dosage): </td>
										<td colspan="5">
											<input type="text" name="ed_strength" id="ed_strength" value="" onchange="setEdited();"/>
											<input type="text" name="ed_consumption_uom" id="ed_consumption_uom" value="" onchange="modifyUOMLabel(this, 'ed');setEdited();"/> Granular Units
										</td>
									</tr>
									<tr>
										<td class="formlabel">Duration: </td>
										<td><input type="text" name="ed_duration" id="ed_duration" value="" onkeypress="return enterNumOnlyzeroToNine(event);" onchange="setEdited(); return calcQty('ed');" /></td>
										<td colspan="2">
											<div style="width: 190px">
												<input type="radio" name="ed_duration_units" value="D" onchange="setEdited();return calcQty('ed');">Days
												<input type="radio" name="ed_duration_units" value="W" onchange="setEdited();return calcQty('ed');">Weeks
												<input type="radio" name="ed_duration_units" value="M" onchange="setEdited();return calcQty('ed');">Months
											</div>
										</td>
										<td class="formlabel">Frequency: </td>
										<td>
											<div id="ed_frequencyAutoComplete" style="padding-bottom: 20px; width: 138px;">
												<input type="text" name="ed_frequecy" id="ed_frequency" value="" maxlengthR*="150" />
												<input type="hidden" name="ed_frequency_hidden" id="d_frequency_hidden" value=""/>
												<input type="hidden" name="ed_per_day_qty" id="d_per_day_qty" value=""/>
												<div id="ed_frequencyContainer"></div>
											</div>
										</td>
									</tr>
									<tr>
										<td class="formlabel">Total Qty: </td>
										<td><input type="text" name="ed_qty" id="ed_qty" value="" maxlength="5" onkeypress="return enterNumOnlyzeroToNine(event);" onchange="setEdited();"/></td>
										<td colspan="2">
											<label id="ed_consumption_uom_label"></label>
										</td>
										<td class="formlabel"></td>
										<td></td>
									</tr>
									<tr>
										<td class="formlabel">Remarks: </td>
										<td colspan="5"><input type="text" name="ed_remarks" id="ed_remarks" value="" style="width: 443px;" onchange="setEdited();"></td>
									</tr>
								</table>
								<table style="margin-top: 10">
									<tr>
										<td>
											<input type="button" id="editOk" name="editok" value="Ok">
											<input type="button" id="editCancel" name="cancel" value="Cancel" />
											<input type="button" id="editPrevious" name="previous" value="<<Previous" />
											<input type="button" id="editNext" name="next" value="Next>>"/>
										</td>
									</tr>
								</table>
							</fieldset>
						</fieldset>
					</div>
				</div>

				<div id="addTrtmtDialog" style="display: none">
					<div class="bd">
						<div id="addTrtmtDialogFields">
							<fieldset class="fieldSetBorder">
									<legend class="fieldSetLabel">Add Treatment Details</legend>
									<table class="formtable">
										<tr>
											<td class="formlabel">Service: </td>
											<td colspan="3">
												<div id="serviceAutocomplete" style="padding-bottom: 20px; width: 443px">
													<input type="text" id="d_serviceName" name="d_serviceName" >
													<div id="serviceNameContainer" style="width: 500px" class="scrolForContainer"></div>
												</div>
											</td>
										</tr>
										<tr>
											<td class="formlabel">Tooth Number: </td>
											<td>
												<input type="hidden" name="d_service_id" id="d_service_id" value=""/>
												<input type="hidden" name="d_doc_speciality" id="d_doc_speciality" value=""/>
												<input type="hidden" name="d_service_charge" id="d_service_charge" value=""/>
												<input type="hidden" name="d_service_discount" id="d_service_discount" value=""/>
												<input type="hidden" name="d_service_group_name" id="d_service_group_name" value=""/>
												<input type="hidden" name="d_tooth_num_required" id="d_tooth_num_required" value=""/>
												<input type="hidden" name="d_tooth_number" id="d_tooth_number" value=""/>
												<input type="hidden" name="d_center_id" id="d_center_id" value="${centerId}"/>
												<div id="dToothNumberDiv" style="width: 200px; float: left;"></div>
												<div class="multiInfoEditBtn" style="float: left;margin-left: 10px" id="dToothNumBtnDiv">
													<a href="javascript:void(0);" onclick="return showToothNumberDialog('add', this);"
														title="Select Tooth Numbers">
														<img src="${cpath}/icons/Edit.png" class="button"/>
													</a>
												</div>
												<div id="dToothNumDsblBtnDiv" style="float: left; display: none">
													<img src="${cpath}/icons/Edit1.png" class="button"/>
												</div>
											</td>
											<td class="formlabel">Treatment Status</td>
											<td>
												<select class="dropdown" name="d_treatment_status" id="d_treatment_status" onchange="addStatusChange(this);">
													<option value="P">Planned</option>
													<option value="I">In Progress</option>
													<option value="C">Completed</option>
													<option value="X">Cancelled</option>
													<option value="N">Patient No Show</option>
												</select>
											</td>
										</tr>
										<tr>
											<td class="formlabel">Planned for Date: </td>
											<td>
												<div style="display: inline;">
													<insta:datewidget name="d_planned_date" id="d_planned_date" value="today"/>
													<input type="text" name="d_planned_time" id="d_planned_time" style="width: 50px" value="${current_time}"/>
												</div>
											</td>
											<td class="formlabel">Planned By: </td>
											<td>
												<div id="plnDoctorAC">
													<input type="text" name="d_pln_doctor" id="d_pln_doctor" />
													<input type="hidden" name="d_planned_by" id="d_planned_by" value=""/>
													<div id="plnDoctorContainer" class="scrolForContainer" style="right: 0px; width: 300px;"></div>
												</div>
											</td>
										</tr>
										<tr>
											<td class="formlabel">Creation Date: </td>
											<td>
												<div style="display: inline;">
													<insta:datewidget name="d_creation_date" id="d_creation_date" value="today"/>
													<input type="text" name="d_creation_time" id="d_creation_time" style="width: 50px" value="${current_time}"/>
												</div>
											</td>
											<td class="formlabel">Planned Qty: </td>
											<td><input type="text" name="d_planned_qty" id="d_planned_qty" value="1" onkeypress="return enterNumOnlyzeroToNine(event)"/></td>
										</tr>
										<tr>
											<td class="formlabel">Start Date: </td>
											<td>
												<div style="display: inline;">
													<insta:datewidget name="d_start_date" id="d_start_date"/>
													<input type="text" name="d_start_time" id="d_start_time" style="width: 50px" value=""/>
												</div>
											</td>
											<td class="formlabel">Conducted By: </td>
											<td>
												<div id="condDoctorAC">
													<input type="text" name="d_cond_doctor" id="d_cond_doctor" />
													<input type="hidden" name="d_completed_by" id="d_completed_by" value=""/>
													<div id="condDoctorContainer" class="scrolForContainer" style="right: 0px; width: 300px;"></div>
												</div>
											</td>
										</tr>
										<tr>
											<td class="formlabel">Completion Date: </td>
											<td>
												<div style="display: inline;">
													<insta:datewidget name="d_completed_date" id="d_completed_date" extravalidation="setStartDate(this, 'd');"/>
													<input type="text" name="d_completed_time" id="d_completed_time" style="width: 50px" value=""/>
												</div>
											</td>
										</tr>
										<tr>
											<td class="formlabel">Comments: </td>
											<td colspan="3"><textarea id="d_comments" name="d_comments" cols="50" rows="2"></textarea></td>
										</tr>
									</table>
									<div id="addDentalSuppliesDialog"  style="display:none">
										<fieldset class="fieldSetBorder">
											<legend class="fieldSetLabel" >Supplies List</legend>
											<table class="dataTable" width="100%" cellspacing="0" cellpadding="0" id="addDentalSuppliestabel">
												<tr>
													<th style="width: 40%;">Item</th>
													<th style="width: 30%;">Supplier</th>
													<th style="width: 30%;">Shade</th>
													<th style="width: 10%;">Item Qty</th>
												</tr>

											</table>
										</fieldset>
									</div>
									<table style="margin-top: 10">
										<tr>
											<td>
												<button type="button" name="trtmtAdd" id="trtmtAdd">Add</button>
												<input type="button" name="trtmtClose" value="Close" id="trtmtClose"/>
											</td>
										</tr>
									</table>
							</fieldset>
						</div>
					</div>
				</div>
				<div id="editTrtmtItemDialog" style="display: none">
					<input type="hidden" name="editTrtmtRowId" id="editTrtmtRowId" value=""/>
					<div class="bd">
						<fieldset class="fieldSetBorder">
							<legend class="fieldSetLabel">Edit Treatment</legend>
							<table class="formtable">
								<tr>
									<td class="formlabel">Service: </td>
									<td colspan="3">
										<label id="ed_serviceNameLabel" style="font-weight: bold"></label>
									</td>
								</tr>
								<tr>
									<td class="formlabel">Tooth Number: </td>
									<td>
										<input type="hidden" name="ed_service_prescribed_id" id="ed_service_prescribed_id" value=""/>
										<input type="hidden" name="ed_doc_speciality" id="ed_doc_speciality" value=""/>
										<input type="hidden" name="ed_tooth_num_required" id="ed_tooth_num_required" value=""/>
										<input type="hidden" name="ed_service_charge" id="ed_service_charge" value=""/>
										<input type="hidden" name="ed_service_discount" id="ed_service_discount" value=""/>
										<input type="hidden" name="ed_tooth_number" id="ed_tooth_number" value=""/>
										<input type="hidden" name="ed_center_id" id="ed_center_id" value="${centerId}"/>
										<div id="edToothNumberDiv" style="width: 200px; float: left"></div>
										<div class="multiInfoEditBtn" style="float: left" id="edToothNumBtnDiv">
											<a href="javascript:void(0);" onclick="return showToothNumberDialog('edit', this);"
												title="Select Tooth Numbers">
												<img src="${cpath}/icons/Edit.png" class="button"/>
											</a>
										</div>
										<div id="edToothNumDsblBtnDiv" style="float: left; display: none">
											<img src="${cpath}/icons/Edit1.png" class="button"/>
										</div>
									</td>
									<td class="formlabel">Treatment Status: </td>
									<td >
										<select class="dropdown" name="ed_treatment_status" id="ed_treatment_status" onchange="trtmtFieldEdited();trtmtStatusChange(this);">
											<option value="P">Planned</option>
											<option value="I">In Progress</option>
											<option value="C">Completed</option>
											<option value="X">Cancelled</option>
											<option value="N">Patient No Show</option>
										</select>
									</td>
								</tr>
								<tr>
									<td class="formlabel">Planned for Date: </td>
									<td>
										<div style="display: inline;">
											<insta:datewidget name="ed_planned_date" id="ed_planned_date" extravalidation="trtmtFieldEdited()"/>
											<input type="text" name="ed_planned_time" id="ed_planned_time" style="width: 50px" value="" onchange="trtmtFieldEdited();"/>
										</div>
									<td class="formlabel">Planned By: </td>
									<td>
										<div id="ed_plnDoctorAC">
											<input type="text" name="ed_pln_doctor" id="ed_pln_doctor" />
											<input type="hidden" name="ed_planned_by" id="ed_planned_by" value=""/>
											<div id="ed_plnDoctorContainer" class="scrolForContainer" style="right: 0px; width: 300px;"></div>
										</div>
									</td>
								</tr>
								<tr>
									<td class="formlabel">Creation Date: </td>
									<td>
										<div style="display: inline;">
											<insta:datewidget name="ed_creation_date" id="ed_creation_date" extravalidation="trtmtFieldEdited()"/>
											<input type="text" name="ed_creation_time" id="ed_creation_time" style="width: 50px" value="" onchange="trtmtFieldEdited();"/>
										</div>
									</td>
									<td class="formlabel">Planned Qty: </td>
									<td><input type="text" name="ed_planned_qty" id="ed_planned_qty" value="" onchange="trtmtFieldEdited();" onkeypress="return enterNumOnlyzeroToNine(event)"/></td>
								</tr>
								<tr>
									<td class="formlabel">Start Date: </td>
									<td>
										<div style="display: inline;">
											<insta:datewidget name="ed_start_date" id="ed_start_date"/>
											<input type="text" name="ed_start_time" id="ed_start_time" style="width: 50px" value="" />
										</div>
									</td>

									<td class="formlabel">Conducted By: </td>
									<td>
										<div id="ed_condDoctorAC">
											<input type="text" name="ed_cond_doctor" id="ed_cond_doctor" />
											<input type="hidden" name="ed_completed_by" id="ed_completed_by" value=""/>
											<div id="ed_condDoctorContainer" class="scrolForContainer" style="right: 0px; width: 300px;"></div>
										</div>
									</td>
								</tr>
								<tr>
									<td class="formlabel">Completion Date: </td>
									<td>
										<div style="display: inline;">
											<insta:datewidget name="ed_completed_date" id="ed_completed_date" extravalidation="setStartDate(this, 'ed');trtmtFieldEdited()"/>
											<input type="text" name="ed_completed_time" id="ed_completed_time" style="width: 50px" value="" onchange="trtmtFieldEdited();"/>
										</div>
									</td>
								</tr>
								<tr>
									<td class="formlabel">Comments: </td>
									<td colspan="3"><textarea id="ed_comments" name="ed_comments" cols="50" rows="2" onchange="trtmtFieldEdited();"></textarea></td>
								</tr>
							</table>
							<div id="editDentalSuppliesDialog"  style="display:none">
										<fieldset class="fieldSetBorder">
											<legend class="fieldSetLabel" >Edit Supplies List</legend>
											<table class="dataTable" width="100%" cellspacing="0" cellpadding="0" id="editDentalSuppliestabel">
												<tr>
													<th style="width: 40%;">Item</th>
													<th style="width: 30%;">Supplier</th>
													<th style="width: 30%;">Shade</th>
													<th style="width: 10%;">Item Qty</th>
												</tr>

											</table>
										</fieldset>
									</div>
							<table style="margin-top: 10">
								<tr>
									<td>
										<input type="button" id="editTrtmtOk" name="editok" value="Ok">
										<input type="button" id="editTrtmtCancel" name="cancel" value="Cancel" />
										<input type="button" id="editTrtmtPrevious" name="previous" value="<<Previous" />
										<input type="button" id="editTrtmtNext" name="next" value="Next>>"/>
									</td>
								</tr>
							</table>
						</fieldset>
					</div>
				</div>
				<div id="toothNumDialog" style="display: none">
					<div class="bd">
						<input type="hidden" name="dialog_type" id="dialog_type" value=""/>
						<fieldset class="fieldSetBorder">
							<legend class="fieldSetLabel">Tooth Numbers(${genericPrefs.map.tooth_numbering_system == 'U' ? 'UNV' : 'FDI'})</legend>
							<table style="width:600px">
								<tr>
									<td colspan="10" style="border-bottom: 1px solid">Pediatric: </td>
								</tr>
								<tr>
									<c:forEach items="${pediac_tooth_numbers}" var="entry" varStatus="st">
										<c:if test="${st.index%10 == 0}">
											</tr><tr>
										</c:if>
										<td style="width: 50px">
											<input type="checkbox" name="d_chk_tooth_number" 
												value="${ifn:cleanHtmlAttribute(entry)}"/> ${ifn:cleanHtml(entry)}
										</td>
									</c:forEach>
								</tr>
							</table>
							<table style="width:600px">
								<tr>
									<td colspan="18" style="border-bottom: 1px solid">Adult: </td>
								</tr>
								<c:set var="toothSize" value="${adult_tooth_numbers.size() / 2}"/>
								<tr>
									<c:forEach items="${adult_tooth_numbers}" var="entry" varStatus="st">
										<c:if test="${st.index%toothSize == 0}">
											</tr><tr>
										</c:if>
										<td style="width: 50px">
											<input type="checkbox" name="d_chk_tooth_number" 
												value="${ifn:cleanHtmlAttribute(entry)}"/> ${ifn:cleanHtml(entry)}
										</td>
									</c:forEach>
								</tr>
							</table>
							<table style="margin-top: 10px">
								<tr>
									<td><input type="button" name="toothNumDialog_ok" id="toothNumDialog_ok" value="Ok"></td>
									<td><input type="button" name="toothNumDialog_close" id="toothNumDialog_close" value="Close"></td>
								</tr>
							</table>
						</fieldset>
					</div>
				</div>
				<div id="genericNameDisplayDialog"  style="visibility:hidden">
					<div class="bd">
						<fieldset class="fieldSetBorder">
							<legend class="fieldSetLabel">Generic Name Details</legend>
							<table border="0" class="formtable">
								<tr height="10px"></tr>
								<tr>
									<td class="formlabel">Generic&nbsp;Name: </td>
									<td class="forminfo" style="width:8em"><b><label id="gen_generic_name"></label></b>
									</td>
								</tr>
								<tr>
									<td class="formlabel">Classification: </td>
									<td class="forminfo" style="width:25em"><b><label id="classification_name"></label>	</td>
								</tr>
								<tr>
									<td class="formlabel">Sub-Classification:</td>
										<td class="forminfo" style="width:25em"><b><label id="sub_classification_name"></label>	</td>
								</tr>
								<tr>
									<td class="formlabel">Standard Adult Dose:</td>
										<td class="forminfo" style="width:25em"><b><label id="standard_adult_dose"></label>	</td>
								</tr>
								<tr>
									<td class="formlabel">Criticality:</td>
										<td class="forminfo" style="width:25em"><b><label id="criticality"></label>	</td>
								</tr>

								<tr height="10px"></tr>
							</table>
							<table>
								<tr>
							    	<td><input type="button" id="genericNameCloseBtn" value="Close"></td>
								</tr>
							</table>
						</fieldset>
					</div>
				</div>
				<div id="serviceSubTaskDiv"  style="visibility:hidden">
					<div class="bd">
						<fieldset class="fieldSetBorder">
							<legend class="fieldSetLabel">Edit Sub Tasks</legend>
							<table style="margin-top: 10px">
								<tr>
									<td>Service Name: </td>
									<td>
										<label id="serviceNameForSubTasks"></label>
										<input type="hidden" id="service_row_id" value=""/>
									</td>
								</tr>
							</table>
								<legend class="fieldSetLabel" style="margin-top: 10px">Sub Tasks</legend>
								<table border="0" class="detailList" id="subTasksListInDialog" style="margin-top: 8px">
									<tr>
										<th>Name</th>
										<th>Status</th>
										<th>By</th>
										<th>Date & Time</th>
									</tr>
									<tr style="display: none">
										<td></td>
										<td>
											<input type="hidden" name="taskPrescId" value=""/>
											<input type="hidden" name="taskName" value=""/>
											<input type="hidden" name="taskId" value=""/>
											<select name="taskStatus" class="dropdown" onchange="return enableTaskFields(this);">
												<option value="NC">Not Completed</option>
												<option value="NR">Not Required</option>
												<option value="C">Completed</option>
											</select>
										</td>
										<td>
										</td>
										<td >

										</td>
									</tr>
								</table>
							<table style="margin-top: 10px">
								<tr>
									<td>
										<input type="button" id="task_Ok" value="Ok"/>
										<input type="button" id="task_Cancel" value="Cancel">
									</td>
								</tr>
							</table>
						</fieldset>
					</div>
				</div>
			</form>
		</c:otherwise>
	</c:choose>
</body>
</html>
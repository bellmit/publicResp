<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ page import="org.apache.struts.Globals" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ page import="com.insta.hms.stores.StoresDBTablesUtil" %>
<%@ page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO" %>

<c:set var="genPrefs" value="<%= GenericPreferencesDAO.getGenericPreferences() %>" />
<c:set var="prefVat" value="${genPrefs.showVAT}" scope="request"/>
<c:set var="applySupplierTaxRules" value="${genPrefs.apply_supplier_tax_rules}" scope="request"/>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="prefMaxCP" value="${genPrefs.pharmacyValidateCostPrice}"/>
<c:set var="prefQtyType" value="${genPrefs.qtyDefaultToIssueUnit}"/>
<c:set var="prefDecimalQty" value="${genPrefs.allowdecimalsforqty}"/>
<c:set var="prefCed" value="${genPrefs.showCED}" scope="request"/>
<c:set var="prefbarcode" value="${genPrefs.barcodeForItem}" scope="request"/>
<c:set var="pharmacygrpid" value="<%= StoresDBTablesUtil.getPharmacyGrpId() %>" scope="request"/>
<c:set var="defaultValue" value="${(genPrefs.decimalDigits == 3) ? '0.000' : '0.00'}"/>
<c:set var="POValidationReq" value="${genPrefs.poToBeValidated eq 'Y' }"/>
<c:set var="validatePORts" value="${urlRightsMap.stores_validate_po}"/>
<c:set var="approvePORts" value="${urlRightsMap.stores_po_approval}"/>
<c:set var="po" value="${pobean.map}"/>	<%-- convenience --%>
<c:set var="validationAllowed" value="${(empty pobean.map.po_no || (not empty pobean.map.po_no && (po.status eq 'A' || po.status eq 'AA')) || (po.status eq 'O' || po.status eq 'AO')) && POValidationReq && (validatePORts eq 'A' || roleId == 1 || roleId == 2 )}"/>
<c:set var="poInUserApprovalLimit" value="${(not empty userpoApprovalLimit && not empty pobean.map.po_no ? userpoApprovalLimit ge po.po_total : true)}"/>
<c:set var="approvalAllowed" value="${(POValidationReq  ? (po.status eq 'V' || po.status eq 'AV') : (empty pobean.map.po_no || (not empty pobean.map.po_no && (po.status eq 'A' || po.status eq 'AA')) || (po.status eq 'O' || po.status eq 'AO')) ) && (approvePORts eq 'A' || roleId == 1 || roleId == 2 ) }"/>
<c:set var="approvalAllowed" value="${approvalAllowed && poInUserApprovalLimit}"/>
<c:set var="taxLabel" value="${genPrefs.procurement_tax_label}" scope="request"/>
<c:set var="prefRejRemarks" value="${genPrefs.forceRemarksForPoItemReject}"/>
<c:set var="existingpostatus" value="${not empty pobean.map.po_no ? pobean.map.status : 'O'}"/>
<c:set var="amendment" value="${screen_id eq 'stores_amend_po' }"/>
<c:set var="round_off" value="${genPrefs.pharmaAutoRoundOff}"/>
<c:set var="autoOrManulaRoundoff" value="${genPrefs.poroundoff }"/>
<c:set var="applyCpValidationForPo" value="${genPrefs.applyCpValidationForPo}"/>


<!-- new po if validate is allow V if not A if not both O the same thing repeats after approval with AV,AA,AO -->
<c:set var="nextPOstatus" value="${validationAllowed ?( (not empty pobean.map.po_no && (po.status eq 'A' || po.status eq 'AA' || po.status eq 'AO')) ? 'AV' : 'V') : approvalAllowed ? ((not empty pobean.map.po_no && (po.status eq 'A' || po.status eq 'AA' || po.status eq 'AO' || po.status eq 'AV')) ? 'AA' : 'A') : ((not empty pobean.map.po_no && po.status eq 'A') ? 'AO' : 'O')}"/>
<c:set var="viewMode" value="${(not empty pobean.map.po_no && grn_exists) || ((not empty pobean.map.po_no) and (pobean.map.status eq 'A' || pobean.map.status eq 'AO' || pobean.map.status eq 'AV' || pobean.map.status eq 'AA' || pobean.map.status eq 'X' || pobean.map.status eq 'C' || pobean.map.status eq 'FC')) }"/>
<c:if test="${amendment }">
<!-- no view mode in amending phase -->
<c:set var="viewMode" value="${not amendment ||  pobean.map.status eq 'FC'}"/>
</c:if>
<c:set var="title">
	<c:choose>
		<c:when test="${amendment}"><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.amendpurchaseorder"/></c:when>
		<c:when test="${viewMode && (grn_count == 0) || pobean.map.status eq 'FC' || pobean.map.status eq 'C'|| pobean.map.status eq 'X'}"><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.viewpurchaseorder"/></c:when>
		<c:when test="${not empty pobean.map.po_no}"><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.editpurchaseorder"/></c:when>
		<c:otherwise><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.raisepurchaseorder"/></c:otherwise>
	</c:choose>
</c:set>
<html>
<head>
<c:set var="strictPO" value="${genPrefs.seWithPO == 'Y' &&
	actionRightsMap.direct_stock_entry == 'N' && roleId ne 1 && roleId ne 2}"/>

	<title>${title}</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<meta name="i18nSupport" content="true"/>
	<insta:link type="script" file="ajax.js"/>
	<insta:link type="script" file="date_go.js"/>
	<insta:link type="script" file="hmsvalidation.js"/>
    <insta:link type="js" file="stores/po.js"/>
	<insta:link type="js" file="stores/purchasedetails.js"/>
	<insta:link type="js" file="stores/registered_supplier_settings_validations.js"/>
	<insta:link type="js" file="stores/storeshelper.js"/>


<script>
	var comingFromSavePO = false;
	var popurl = '${pageContext.request.contextPath}';
	var validateCostPrice='${prefMaxCP}';
	var cpath = '${cpath}';
	var editMaxCP = '${actionRightsMap.change_max_costprice}';
	var qtyDecimal = '${prefDecimalQty}';
	var prefCED = '${prefCed}';
	var fromsr = '${fromSR}';
	var prefVAT = '${prefVat}';
	var prefBarCode = '${prefbarcode}';
	var onlyView = ${viewMode};
	var poNo = '${pobean.map.po_no}';
	var tcsApplicable = '${pobean.map.tcs_applicable}';
	var centerId = '${centerId}';
	var poStoreId = '${pobean.map.store_id}';
	var gItemMasterTimestamp = '${medicine_timestamp}';
 	var allowBackDate = '${actionRightsMap.allow_backdate}';
	var jAdditionalItems = undefined;
	var gRoleId = '${roleId}';
	<c:if test="${not empty additionalItemsJSON}">
		jAdditionalItems = ${additionalItemsJSON};
		var jAdditionalQtys = ${additionalQtysJSON};
	</c:if>
	var initSupplierId = '${ifn:cleanJavaScript(param.supplier_id)}';
	var initStoreId = '${ifn:cleanJavaScript(param.store_id)}';
	var applySupplierTaxRules = '${applySupplierTaxRules}';

	function chkBeforeunload () {
		if (!comingFromSavePO) {
 			var numRows = getNumItems();
 			if (numRows > 0) {
 				return '';
 			}
 		}
	}

	var nextPOstatus = '${nextPOstatus}';
	var taxLabel = '${taxLabel}';
	var prefRejRemarks = '${prefRejRemarks}';
	var userpoApprovalLimit = 0;
	<c:if test="${ not empty userpoApprovalLimit}">
		userpoApprovalLimit = ${userpoApprovalLimit};
	</c:if>
	var existingPOstatus = '${existingpostatus}';
	var amendment = '${amendment}';
	var poStoredVattype = '${po.vat_type}';
	var subgroupNamesList = JSON.parse('${ifn:cleanJavaScript(subGroupListJSON)}'); 
	var groupListJSON = JSON.parse('${ifn:cleanJavaScript(groupListJSON)}');
	var autoOrManulaRoundoff = '${autoOrManulaRoundoff}';
	var allowTaxEditRights ='${actionRightsMap.allow_tax_subgroup_edit}';
	var applyCpValidationForPo ='${applyCpValidationForPo}';
	
</script>

<style>
.scrolForContainer .yui-ac-content{
	 max-height:18em;overflow:auto;overflow-x:auto; /* scrolling */
    _height:18em; max-width:35em; width:35em;/* ie6 */
}

.selectedRow {background-color: #C7E782;}
</style>
<insta:js-bundle prefix="stores.procurement"/>
</head>

<c:set var="cststatus">
<insta:ltext key="storemgmt.stockapproval.list.${taxLabel}"/>,<insta:ltext key="storemgmt.stockapproval.list.cst.${taxLabel}"/><c:if test="${applySupplierTaxRules == 't'}">,<insta:ltext key="storemgmt.stockapproval.list.not.applicable.text"/></c:if>
</c:set>
<c:set var="cstvalues">
<insta:ltext key="storemgmt.stockapproval.list.${taxLabel}"/>,<insta:ltext key="storemgmt.stockapproval.list.cst.${taxLabel}"/><c:if test="${applySupplierTaxRules == 't' }">,<insta:ltext key="storemgmt.stockapproval.list.not.applicable"/></c:if>
</c:set>
<c:set var="vatstatus">
<insta:ltext key="storemgmt.stockapproval.list.${taxLabel}"/>
</c:set>
<c:set var="cancelText">
<insta:ltext key="storemgmt.stockapproval.list.cancelText"/>
</c:set>
<c:set var="poList">
<insta:ltext key="storemgmt.stockapproval.list.polist"/>
</c:set>

<body onload="init();" class="yui-skin-sam"  onbeforeunload="chkBeforeunload();">
<c:set var="status">
<c:choose>
<c:when test="${po.status == 'O'}">
	<insta:ltext key="storeprocurement.raisepurchaseorder.podetails.open"/>,
</c:when>
<c:when test="${po.status == 'V'}">
	<insta:ltext key="storeprocurement.raisepurchaseorder.podetails.validated"/>,
</c:when>
<c:when test="${po.status == 'A'}">
	<insta:ltext key="storeprocurement.raisepurchaseorder.podetails.approved"/>,
</c:when>
<c:when test="${po.status == 'AO'}">
	<insta:ltext key="storeprocurement.polist.list.amended.open"/>,
</c:when>
<c:when test="${po.status == 'AV'}">
	<insta:ltext key="storeprocurement.polist.list.amended.validated"/>,
</c:when>
<c:when test="${po.status == 'AA'}">
	<insta:ltext key="storeprocurement.polist.list.amended.approved"/>,
</c:when>
</c:choose>
<insta:ltext key="storeprocurement.raisepurchaseorder.podetails.forceclosed"/>

</c:set>

<c:set var="statusvalues">
<c:if test="${po.status == 'O' || po.status == 'V' || po.status == 'A' || po.status == 'AO' || po.status == 'AV' || po.status == 'AA'}">
${po.status},</c:if>FC
 


</c:set>
<c:set var="uomstatus">
<insta:ltext key="storeprocurement.raisepurchaseorder.podetails.packageuom"/>,
<insta:ltext key="storeprocurement.raisepurchaseorder.podetails.unituom"/>
</c:set>
<c:set var="discountstatus">
<insta:ltext key="storeprocurement.raisepurchaseorder.podetails.percent"/>,
<insta:ltext key="storeprocurement.raisepurchaseorder.podetails.amount"/>
</c:set>
<c:set var="selecttemplate">
<insta:ltext key="storeprocurement.raisepurchaseorder.podetails.selecttemplate"/>
</c:set>
<c:set var="mrpstatus">
<insta:ltext key="storeprocurement.stockentry.invoicedetails.mrpbased.with.bonus"/>,
<insta:ltext key="storeprocurement.stockentry.invoicedetails.mrpbased.without.bonus"/>,
<insta:ltext key="storeprocurement.stockentry.invoicedetails.cpbased.with.bonus"/>,
<insta:ltext key="storeprocurement.stockentry.invoicedetails.cpbased.without.bonus"/>
</c:set>
<c:set var="dummyvalue">
<insta:ltext key="selectdb.dummy.value"/>
</c:set>
<c:set var="savePath" value="${amendment ? 'amendpo' : 'poscreen' }"/>
<form name="poForm" method="post" enctype="multipart/form-data" action="${savePath }.do?_method=${grn_count > 0 && viewMode ? 'insertRemarks' : 'savePO' }" >

<input type="hidden" id="po_no" name="po_no" value="${pobean.map.po_no }"/>
<input type="hidden" name="_printAfterSave" value=""/>
<input type="hidden" id="grn_count" name="grn_count" value="${ifn:cleanHtmlAttribute(grn_count)}"/>

<c:choose>
	<c:when test="${grn_count > 0 && viewMode }">
		<input type="hidden" id="_method" name="_method" value="insertRemarks"/>
	</c:when>
	<c:otherwise>
		<input type="hidden" id="_method" name="_method" value="savePO"/>
	</c:otherwise>
</c:choose>
<h1>${title}</h1>

<div><insta:feedback-panel/></div>
 

<div align="center">
	<fieldset class="fieldSetBorder" >
		<legend class="fieldSetLabel"><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.podetails"/></legend>
		<table class="formtable" cellpadding="0" cellspacing="0" border="0" width="100%">
		<c:choose>
			<c:when test="${not empty po.po_no}">
				<tr>
					<td class="formlabel"><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.pono"/>:</td>
					<td class="forminfo">${po.po_no}</td>
					<td class="formlabel"><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.user"/>:</td>
					<td class="forminfo">${po.user_id}</td>
					<td class="formlabel"><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.status"/>:</td>
					<td ><insta:selectoptions name="status_fld" value="${po.status}" opvalues="${statusvalues}" optexts="${status}"/>
						<input type="hidden" name="status" value="${po.status}"/>
					</td>
				</tr>
			</c:when>
			<c:otherwise>
				<input type="hidden" name="status" value="O"/>
			</c:otherwise>
		</c:choose>
			<tr>
				<td class="formlabel"><span class="prestar">*</span><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.store"/>:</td>
				<td>
					<insta:userstores username="${userid}" elename="store_id" onlySuperStores="Y"  onchange="return onChangeStore();"
						val="${po.store_id}"/>
				</td>
				<td class="formlabel"><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.department"/>:</td>
				<td class="forminfo">
					<insta:selectdb name="dept_id" value="${po.dept_id}" table="department" orderby="dept_name"
					valuecol="dept_id" displaycol="dept_name" dummyvalue="${dummyvalue}"/>
				</td>

				<td class="formlabel"><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.quantityuom"/>:</td>
				<td>
					<insta:selectoptions name="po_qty_unit" opvalues="P,I" optexts="${uomstatus}"
					value="${empty po.po_no ? (prefQtyType == 'Y' ? 'I' : 'P') : po.po_qty_unit}"
					onChange="return onChangeQtySelection();"/>
				</td>
			</tr>
			<tr>
				<td class="formlabel"><span class="prestar">*</span><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.supplier"/>:</td>
				<td valign="top">
					<div id="supplier_container">
						<input type="text" name="supplier_name" onblur="resetTaxTypeValues(this);" value="${po.supplier_name}" />
						<div id="supplier_dropdown" class="scrolForContainer"></div>
						<input type="hidden" name="supplier_id" value="${po.supplier_id}"/>
					</div>
				</td>
				<td class="formlabel"><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.supplieraddress"/>:</td>
				<td class="forminfo" ><label id="suppAddId"></label></td>

				<td class="formlabel"><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.allotedto"/>:</td>
		
				<td ><insta:selectoptions name="po_alloted_to" value="${po.po_alloted_to}" opvalues="${store_users}" optexts="${store_users}" dummyvalue="${dummyvalue}"/>
					<input type="hidden" name="po_alloted_to" value="${po.po_alloted_to}"/>
				</td>
			</tr>

			<tr>
				<td class="formlabel"><span class="prestar">*</span><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.podate"/>:</td>
				<fmt:formatDate  var="poDate" pattern="dd-MM-yyyy" value="${po.po_date}"/>
				<td><insta:datewidget name="po_date" value="${empty poDate ? 'today' : poDate}" extravalidation="validpastDate();"/></td>
				<td class="formlabel"><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.enquiryno"/>:</td>
				<td><input type="text" name="enq_no" value="${po.enq_no}" maxlength="30"></td>
				<td class="formlabel"><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.enquirydate"/>:</td>
				<fmt:formatDate  var="enqDate" pattern="dd-MM-yyyy" value="${po.enq_date}"/>
				<td ><insta:datewidget name="enq_date" valid="past" value="${enqDate}" btnPos="left"/></td>
			</tr>

			<tr>
				<td class="formlabel"><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.quotationno"/>:</td>
				<td><input type="text" name="qut_no" value="${po.qut_no}" maxlength="30"></td>
				<td class="formlabel"><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.quotationdate"/>:</td>
				<fmt:formatDate  var="qutDate" pattern="dd-MM-yyyy" value="${po.qut_date}"/>
				<td><insta:datewidget name="qut_date" valid="past" value="${qutDate}"/></td>
				<td class="formlabel"><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.reference"/>:</td>
				<td><input type="text" name="reference" id="reference" maxlength="100" value="${po.reference}"></td>
			</tr>

			<tr>
				<td class="formlabel"><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.creditperiod"/>:</td>
				<td>
					<input type="text" name="credit_period" value="${po.credit_period}" maxlength="6"
					onkeypress="return enterNumAndDot(event);">
					<input type="hidden" name="po_vat_type" value="${po.vat_type}"/>
					<input type="hidden" name="po_vat_rate" value="${po.vat_rate}"/>
				</td>
					<td class="formlabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.purposeofpurchase"/>:</td>
					<td><input type="text" name="purpose_of_purchase" id="purpose_of_purchase" value="${po.purpose_of_purchase}" maxlength="70"/></td>
					
					<td class="formlabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.uploadquotationcopy"/>:</td>
					<td colspan="2">
							<input type="file" name="quotationAttachment" ${(po.status eq null || po.status eq 'O' || po.status eq 'V' || po.status eq 'AO' || po.status eq 'AV') ? '' : 'disabled'} accept="<insta:ltext key="upload.accept.image"/>,<insta:ltext key="upload.accept.document"/>"/>
							<c:if test="${(not empty po.quotation_file_name)}">
								<c:url var="quoUrl" value="/pages/stores/poscreen.do">
									<c:param name="_method" value="getUploadedQuotationCopy"/>
									<c:param name="purordNo" value="${po.po_no}"/>
								</c:url>
								<a href="${quoUrl}" target="blank"><insta:ltext key="storeprocurement.stockentry.invoicedetails.view"/></a>
							</c:if>
							<c:if test="${(not empty po.quotation_file_name)}">
								<insta:ltext key="storeprocurement.stockentry.invoicedetails.delete"/>:
								<input type="checkbox" name="deleteUploadedQuotation" value="Y" ${(po.status eq 'O' || po.status eq 'V' || po.status eq 'AO' || po.status eq 'AV') ? '' : 'disabled'} onclick="onClickDeleteQuotation()"/>
							</c:if>
						</td>
			</tr>

			<tr>
				<td class="formlabel"><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.expecteddeliverydate"/>:</td>
				<fmt:formatDate var="deliveryDate" pattern="dd-MM-yyyy" value="${po.delivery_date}"/>
				<td><insta:datewidget name="delivery_date" value="${deliveryDate}" btnPos="left"/></td>

				<td class="formlabel"><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.remarks"/>:</td>
				<td class="forminfo" colspan="3">
					<input type="text" name="remarks" style="width: 35em;" value="${po.remarks}" maxlength="4000"  
					<c:if test="${grn_count > 0 && (po.status == 'FC')}"> disabled</c:if> />
				</td>
			</tr>
			<tr style="display:none;">
				<td class="formlabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.cform"/>:</td>
				<td><input type="checkbox" name="c_form" value ="Y" onclick="validateCformTaxrate(this)" disabled 
					<c:if test="${po.c_form eq true}"><insta:ltext key="storeprocurement.stockentry.invoicedetails.checked"/></c:if>	style="margin: 0px; padding: 0px" />
				</td>
				<td class="formlabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.form8H"/>:</td>
				<td><input type="checkbox" name="form_8h" value ="Y" onclick="resetForm8hValues(this)"  
				${po.form_8h eq true or (not empty po  and not empty po.status)   ? 'disabled' : ''}
						<c:if test="${po.form_8h eq true}"><insta:ltext key="storeprocurement.stockentry.invoicedetails.checked"/></c:if> style="margin: 0px; padding: 0px" />
				</td>
			</tr>
			<tr style="display:none;">
				<td class="formlabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.taxtype"/>:</td>
				<td>
					<insta:selectoptions name="main_vat_type" opvalues="${cstvalues}" optexts="${cststatus}"
					value="${empty po.po_no ? (taxLabel == 'V' ? 'VAT' : 'GST') : po.vat_type}"
					onchange="onChangeTaxName();" class="dropdown"/>
				</td>
					<td class="formlabel" style="display:none" name = "cstRateH"><insta:ltext key="storeprocurement.stockentry.invoicedetails.cstrate"/>:</td>
					<td  style="display:none" name = "cstRateH">
						<input type="text" name="main_cst_rate"
						value="${not empty po.vat_rate  ? po.vat_rate : 0}"
						onkeypress="return enterNumAndDot(event);" readonly onchange="makeingDec(this.value,this);onChangeCST()">%
					</td>
			</tr>
			
			<c:if test="${not empty po  and not empty po.status and( (po.status eq 'A' && not viewMode) || po.status eq 'AO' || po.status eq 'AV' || po.status eq 'AA' || po.status eq 'C' || po.status eq 'X' || po.status eq 'FC') }">
				<tr>
					<c:choose>
						<c:when test="${amendment}">
							<td class="formlabel"><span class="prestar">*</span><insta:ltext key="storeprocurement.stockentry.invoicedetails.amend.reason"/>:</td>
							<td>
								<input type="text" name="amended_reason" style="width: 35em;" maxlength="4000" value="${po.amended_reason }"/>
							</td>
						</c:when>
						<c:otherwise>	
							<c:if test="${ not empty po.amended_reason}">	
								<td class="formlabel"><span class="prestar">*</span><insta:ltext key="storeprocurement.stockentry.invoicedetails.amend.reason"/>:</td>				
								<td class="forminfo" colspan="5"><p style="width: 65em;word-wrap: break-word;">${po.amended_reason }</p></td>  			  					
							</c:if>
						</c:otherwise>
					</c:choose>
				</tr>
			</c:if>
		</table>
	</fieldset>
</div>

<table class="detailList dialog_displayColumns" width="100%" cellspacing="0" cellpadding="0" id="medtabel">
	<tr>
		<th><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.item"/></th>
		<th><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.code"/></th>
		<th style="text-align: right"><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.pkg"/></th>
		<th><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.pkgtype"/></th>
		<th style="text-align: right"><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.mrp"/></th>
		<th style="text-align: right"><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.adjmrp"/></th>
		<th style="text-align: right"><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.rate"/></th>
		<th style="text-align: right"><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.qty"/></th>
		<th style="text-align: right"><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.bonus"/></th>
		<th><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.uom"/></th>
		<th style="text-align: right"><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.tax"/></th>
		<th style="text-align:right;"><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.taxtype"/></th>
		<th style="text-align: right"><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.disc.per"/></th>
		<th style="text-align: right"><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.disc"/></th>
		<th style="text-align: right"><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.vatamt"/></th>
		<th style="text-align: right;"><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.total"/></th>
		<th></th>
		<th></th>
	</tr>

	<c:set var="totalDiscount" value="0"/>
	<c:set var="totalAmount" value="0"/>
	<c:set var="totalVat" value="0"/>
	<c:set var="numItems" value="${fn:length(poItems)}"/>
	
	<%--  Ideally we should have handled the Stock Reorder items also within this loop, but since
	  we need a confirmation for adding to existing PO items, we have to do the additions in
		javascript. --%>
	<c:forEach begin="1" end="${numItems+1}" var="i" varStatus="loop">
		<c:set var="item" value="${poItems[i-1]}"/>
		<c:set var="attribs">
			<c:choose>
      		<c:when test="${empty item}">style="display:none"</c:when>
				<c:otherwise>id="itmRow${i-1}"</c:otherwise>
			</c:choose>
		</c:set>

		<c:set var="flagColor">
				<c:choose>
					<c:when test="${item.status == 'R'}"><insta:ltext key="billing.patientbill.details.red"/></c:when>
					<c:when test="${item.status == 'A' || item.status == 'AA'}"><insta:ltext key="storeprocurement.itempurchasedetails.list.green"/></c:when>
					<c:otherwise><insta:ltext key="billing.patientbill.details.empty"/></c:otherwise>
				</c:choose>
			</c:set>

		<tr ${attribs}>
			<td class="forminfo" style="width:25em;padding-left: 0.5em;white-space:normal;" valign="middle">
				<%-- display values are different from stored values in case qty unit is package --%>
				<img src="${cpath}/images/${flagColor}_flag.gif"/>
				<label>${item.medicine_name}</label>
				<input type="hidden" name="medicine_id" value="${item.medicine_id}">
				<input type="hidden" name="medicine_name" value="${item.medicine_name}">
				<input type="hidden" name="item_barcode_id" value="${item.item_barcode_id}">
			   	<input type="hidden" name="cost_price" value="${item.cost_price}">
			   	<input type="hidden" name="cost_price_display">
			    <input type="hidden" name="mrp" value="${item.mrp}">
			    <input type="hidden" name="mrp_display">
			    <input type="hidden" name="adj_mrp" value="${item.adj_mrp}">
				<input type="hidden" name="adj_mrp_display">
				<input type="hidden" name="vat_rate" value="${item.vat_rate}" />
				<input type="hidden" name="vat_type" value="${item.vat_type}">
			    <input type="hidden" name="vat" value="${item.vat}">
			   	<input type="hidden" name="qty_req" value="${item.qty_req}" >
				<input type="hidden" name="qty_req_display">
				<input type="hidden" name="bonus_qty_req" value="${item.bonus_qty_req}" >
				<input type="hidden" name="bonus_qty_req_display">
				<input type="hidden" name="discount" value="${item.discount}" >
				<input type="hidden" name="discount_per" value="${item.discount_per}" >
				<input type="hidden" name="item_ced_per" value="${item.item_ced_per}">
				<input type="hidden" name="item_ced" value="${item.item_ced}">
				<input type="hidden" name="med_total" value="${item.med_total}">
				<input type="hidden" name="po_pkg_size" value="${item.po_pkg_size}">
				<input type="hidden" name="package_type" value="${item.package_type}">
				<input type="hidden" name="package_uom" value="${item.package_uom}">
				<input type="hidden" name="issue_units" value="${item.issue_units}">
				<input type="hidden" name="uom_display">
				<input type="hidden" name="billable" value="${item.billable}">
				<input type="hidden" name="item_order" value="${item.item_order}">
				<input type="hidden" name="_deleted" value="false">
				<input type="hidden" name="cst_rate" value="${item.vat_rate}"/>
				<input type="hidden" name="status_ar" value="${item.status}" />
				<input type="hidden" name="item_remarks" value="${item.item_remarks}"/>
				<input type="hidden" name="min_rate" value="${item.min_rate}"/>
				<input type="hidden" name="max_cost_price" value="${item.max_cost_price}">
				<input type="hidden" name="discounted_min_rate" value="${item.discounted_min_rate}"/>
				<input type="hidden" name="min_rate_suppliers" value="${item.min_rate_suppliers}"/>
				<input type="hidden" name="margin" value=${item.margin}/>
				<input type="hidden" name="margin_type" value=${item.margin_type}>
				<input type="hidden"   name="supplier_rate_validation" value="false" />
				<input type="hidden"   name="supplier_rate_val" value="" />
				<input type="hidden" name="tax_rate" value="${item.vat_rate}" />
				<input type="hidden" name="master_vat_rate" value="${item.master_vat_rate}" />
				<input type="hidden" name="master_vat_type" value="${item.master_vat_type}" />
				<c:set var="_taxindex" value="0"/>
				<c:forEach items="${groupList}" var="group">
					<c:set var="_taxindex" value="${_taxindex+1}"/>
					<c:set var="tax_exist" value="false"/>
						<c:if test="${fn:length(po_tax_details) gt 0}">
							<c:forEach items="${po_tax_details}" var="potax">
								<c:if test="${group.item_group_id == potax.map.item_group_id && item.medicine_id eq potax.map.medicine_id }">
									<c:set var="tax_exist" value="true"/>
									<input type="hidden" name="taxname${group.item_group_id}" value="${potax.map.item_group_name}" />
									<input type="hidden" name="taxrate${group.item_group_id}" value="${potax.map.tax_rate}" />
									<input type="hidden" name="taxamount${group.item_group_id}" value="${potax.map.tax_amt}" />
									<input type="hidden" name="taxsubgroupid${group.item_group_id}" value="${potax.map.item_subgroup_id}" />
									<input type="hidden" name="oldtaxsubgroupid${group.item_group_id}" value="${potax.map.item_subgroup_id}" />
								</c:if>
							</c:forEach>
						</c:if>
						<c:if test="${tax_exist eq 'false'}">
							<input type="hidden" name="taxname${group.item_group_id}" value="" />
							<input type="hidden" name="taxrate${group.item_group_id}" value="" />
							<input type="hidden" name="taxamount${group.item_group_id}" value="" />
							<input type="hidden" name="taxsubgroupid${group.item_group_id}" value="" />
							<input type="hidden" name="oldtaxsubgroupid${group.item_group_id}" value="" />
						</c:if>
				</c:forEach>
				<input type="hidden" name="item_code" value="${item.item_code}" />
			</td>

			<%-- values are set in javascript using the hidden field values as common code, so that
			Package/Unit quantity can be handled correctly in one place --%>
			<td></td>	<%-- Item code --%>
			<td style="text-align: right;"></td>	<%-- pkg size --%>
			<td></td> <%-- pkg type --%>
			<td style="text-align:right;"></td>		<%-- mrp --%>

			<td style="text-align: right;"></td> <%-- adj_mrp --%>
			
			<td style="text-align: right;"></td>	<%-- cost_price --%>
			<td style="text-align: right;"></td>	<%-- qty_req --%>
			<td style="text-align: right;"></td>	<%-- bonus_qty_req --%>
			<td></td>		<%-- UOM: could be issue or package, depending on qtySelection --%>

			<td style="text-align: right;"></td>	<%-- vat_rate --%>
			<td style="text-align: right;"></td>	<%-- vat_type --%>
			<td style="text-align: right;"></td>	<%-- discount per --%>
			<td style="text-align: right;"></td>	<%-- discount --%>
			<td style="text-align: right;"></td>	<%-- vat --%>
			<td style="text-align: right;"></td>	<%-- med_total --%>

			<td>
				<label>
					<img class="imgDelete" src="${cpath}/icons/delete.gif"
					onclick="deleteItem(this)" style="cursor:pointer" />
				</label>
			</td>
			<td>
				<label>
					<img class="button" name="editicon" onclick="openEditDialogBox(this)"
					src="${cpath }/icons/Edit.png">
				</label>
			</td>
		</tr>
		<c:set var="totalAmount" value="${totalAmount + item.med_total}"/>
		<c:set var="totalDiscount" value="${totalDiscount + item.discount}"/>
		<c:set var="totalVat" value="${totalVat + item.vat}"/>
	</c:forEach>
</table>

<table class="addButton">
	<tr>
		<td align="right">
			<button type="button" name="plusItem" id="plusItem" title='<insta:ltext key="storeprocurement.raisepurchaseorder.podetails.addnewitem"/>'
				onclick="openAddDialog(); return false;" accesskey="+"
				class="imgButton" >
				<c:choose>
					<c:when test="${not viewMode}"><img src="${cpath }/icons/Add.png"/></c:when>
					<c:otherwise><img src="${cpath }/icons/Add1.png"/></c:otherwise>
				</c:choose>
			</button>
		</td>
	</tr>
</table>

<fieldset class="fieldSetBorder">
	<legend class="fieldSetLabel"><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.totals"/></legend>
	<table align="right" class="infotable" width="100%">
		<tr>
			<c:set var="_taxindex" value="0"/>
			<c:forEach items="${groupList}" var="group">
				<c:set var="_taxindex" value="${_taxindex+1}"/>
				<td class="formlabel"><label id="taxnamelabel_${group.item_group_id}">${group.item_group_name}</label>:</td>
				<td class="forminfo"><label id="taxamtlabel_${group.item_group_id}">0</label></td>
				<c:if test="${_taxindex%3 == 0}">
					</tr>
					<tr>
				</c:if>
			</c:forEach>
			<td class="formlabel" style="width:12em;"><label><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.totaltax"/></label>:</td>
			<td class="forminfo"><label id="lblTotalTaxes">${totalVat}</label></td>
		</tr>
		<tr>
			<td class="formlabel"><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.itemdiscounts"/>:</td>
			<td class="forminfo"><label id="lblItemDiscounts">${ifn:afmt(totalDiscount)}</label></td>

			<td class="formlabel"><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.totalitemamt"/>:</td>
			<td class="forminfo"><label id="lblItemTotal">${ifn:afmt(totalAmount)}</label></td>
		</tr>

		<tr>
			<td class="formlabel"><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.podiscounttype"/>:</td>
			<td>
				<insta:selectoptions name="discount_type" opvalues="P,A" optexts="${discountstatus}"
					value="${po.discount_type}" onchange="onChangeDiscountType()"/>
			</td>

			<td class="formlabel"><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.podiscountvalue"/>:</td>
			<td>
				<input type="text" name="discount_val"
					value="${po.discount_type == 'P' ? po.discount_per : po.discount}"
					onkeypress="return enterNumAndDot(event);" onChange="return onChangeDiscountVal()"/>
				<input type="hidden" name="po_discount_per" value="${po.discount_per}"/>
				<input type="hidden" name="po_discount" value="${po.discount}"/>
			</td>
			<td class="formlabel"><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.podiscountamt"/>:</td>
			<td class="forminfo"><label id="lblPODiscount">${po.discount}</label></td>
		</tr>
		<tr style="display:none;" id ="tcsRow">
			<td class="formlabel"><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.tcs.type"/>:</td>
			<td>
				<insta:selectoptions name="tcs_type" opvalues="P,A" optexts="${discountstatus}"
					value="${po.tcs_type}" onchange="onChangeTcsType()"/>
			</td>

			<td class="formlabel"><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.tcs.value"/>:</td>
			<td>
				<input type="text" name="tcs_value"
					value="${po.tcs_type == 'P' ? po.tcs_per : po.tcs_amount}"
					onkeypress="return enterNumAndDot(event);" onChange="return onChangeTcsVal()"/>
				<input type="hidden" name="po_tcs_per" value="${po.tcs_per}"/>
				<input type="hidden" name="po_tcs_amount" value="${po.tcs_amount}"/>
			</td>
			<td class="formlabel"><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.tcs.amount"/>:</td>
			<td class="forminfo"><label id="lblPOTcsAmount">${po.tcs_amount}</label></td>
		</tr>
		<tr>
			<td class="formlabel"><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.roundoff"/>:</td>
			<c:choose>
			<c:when test="${autoOrManulaRoundoff == 'M' }">
				<td><input type="text" name="round_off" value="${empty po.round_off ? '0' : po.round_off}"
					onchange="onChangeRoundOff();" onkeypress="return enterNumAndDotAndMinus(event);"/>
				</td>
				<input type="hidden" name="round_off_flag" value="${po.round_off_flag}"/>
				<input type="hidden" name="round_off_val" value=""/>
			</c:when>
			<c:otherwise>
				<td>
					<input type="checkbox" name="round_off_val" id="round_off_val" onclick="onChangeRoundOff();" 
						<c:choose>
							<c:when test="${not empty pobean.map.po_no}">${po.round_off_flag == 'Y' ? 'checked' : ''}</c:when>
							<c:otherwise>${round_off == 'Y' ? 'checked' : ''}</c:otherwise>
						</c:choose>
					/>
					<input type="hidden" name="round_off_flag" value="${po.round_off_flag}"/>
					<input type="hidden" name="round_off" value="${po.round_off}"/>
				</td>
			</c:otherwise>
			</c:choose>
			<td class="formlabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.transportcharges"/>:</td>
			<td>
				<input type="text" name="transportation_charges" value="${not empty pobean.map.transportation_charges ? pobean.map.transportation_charges : defaultValue }" onkeypress="return enterNumAndDot(event);" onchange="onChangeTransportationChargeVal();return makeingDec(this.value,this)" maxlength="13"/>
			</td>
			<td class="formlabel"><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.pototal"/>:</td>
			<td class="forminfo">
				<label id="lblGrandTotal">${po.po_total}</label>
				<input type="hidden" name="po_total" value="${po.po_total}"/>
			</td>
		</tr>
	</table>
</fieldset>


<table>
	<tr>
		<td><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.suppliertermsandconditions"/>:
			<insta:selectdb name="supplierTermTemplates" table="ph_payment_terms"
			valuecol="template_code" displaycol="template_name" dummyvalue="${selecttemplate}"
			filtercol="status,is_delivery_instruction" filtervalue="A,N"
			onChange="onSupplierTermTemplatesChange()" />
		</td>
	</tr>
	<tr>
		<td >
			<textarea rows="4" onfocus ="this.rows=8" cols="80" name="supplier_terms">${po.supplier_terms}</textarea>
		</td>
	</tr>
</table>

<table>
	<tr>
		<td class=""><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.hospitaltermsandconditions"/>: </td>
	</tr>
	<tr>
		<td >
			<textarea rows="4" onfocus ="this.rows=8" cols="80" name="hospital_terms">${not empty po.hospital_terms ? po.hospital_terms : hospitalterms}</textarea>
		</td>
	</tr>
</table>

<table>
	<tr>
		<td class=""><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.deliveryinstructions"/>:
			<insta:selectdb name="deliveryInstructionTemplates" table="ph_payment_terms"
			valuecol="template_code" displaycol="template_name" dummyvalue="${selecttemplate}"
			filtercol="status,is_delivery_instruction" filtervalue="A,Y"
			onChange="onDeliveryInstructionTemplatesChange()" />
		</td>
	</tr>
	<tr>
		<td >
			<textarea rows="4" onfocus ="this.rows=8" cols="80" name="delivery_instructions">${po.delivery_instructions}</textarea>
		</td>
	</tr>
</table>

<%-- New PO can be saved or save+printed. Existing PO can be viewed (no save/save&print, only print)
	or edited (no print, only save/save&print) --%>
<c:if test="${not viewMode}">
	<div class="screenActions" style="float: left">
		<button type="button" name="btnSavePo" onclick="onlySavePO();" accessKey='S'>
			<b><u><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.s"/></u></b><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.ave"/>
		</button>
		<c:if test="${(validationAllowed || approvalAllowed)}">
		<button type="button" name="btnSavendsetstatusPo" onclick="saveAndSetStatusPO('${nextPOstatus}');" accessKey=${nextPOstatus eq 'A' || nextPOstatus eq 'AA' ? 'P' : 'L'}>
			<insta:ltext key="storeprocurement.raisepurchaseorder.podetails.save"/> &amp; <c:choose><c:when test="${nextPOstatus eq 'V' || nextPOstatus eq 'AV' }"><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.Va"/><b><u><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.l"/></u></b><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.idate"/></c:when><c:when test="${nextPOstatus eq 'A' || nextPOstatus eq 'AA' }"><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.a"/><b><u><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.p"/></u></b><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.pprove"/></c:when></c:choose>
		</button>
		</c:if>
		<button type="button" name="btnSaveAndPrintPo" onclick="saveAndPrintPO();" accessKey='R'>
			<insta:ltext key="storeprocurement.raisepurchaseorder.podetails.save"/> &amp; <insta:ltext key="storeprocurement.raisepurchaseorder.podetails.p"/><b><u><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.r"/></u></b><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.int"/>
		</button>
		<insta:screenlink addPipe="true" screenId="stores_view_po" label="PO List" extraParam="?_method=getPOs&sortOrder=po_date&sortReverse=true&status=A&status=O&status=AO&status=AA"/>
		<c:if test="${not empty pobean.map.po_no}">
			<insta:screenlink target="_blank" screenId="po_audit_log" extraParam="?_method=getAuditLogDetails&po_no=${pobean.map.po_no}&al_table=store_po_main_audit_log" label="Audit Log" addPipe="true"/>
		</c:if>		
	</div>
</c:if>
<c:if test="${viewMode}">
	 <div class="screenActions" style="float: left">
	 	<%-- Partial po edited remarks and saved --%>
	 	<c:if test="${grn_count > 0 && (po.status != 'FC' && po.status != 'C' && po.status != 'X')}">
	 		<button type="button" name="btnSavePartialPO" onclick="submitPartialPOSave();" accessKey='S'>
			<b><u><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.s"/></u></b><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.ave"/>
		</button>	
		</c:if>
		<insta:screenlink addPipe="true" screenId="stores_view_po" label="PO List" extraParam="?_method=getPOs&sortOrder=po_date&sortReverse=true&status=A&status=O&status=AO&status=AA"/>	
		<c:if test="${not empty pobean.map.po_no}">
			<insta:screenlink target="_blank" screenId="po_audit_log" extraParam="?_method=getAuditLogDetails&po_no=${pobean.map.po_no}&al_table=store_po_main_audit_log" label="Audit Log" addPipe="true"/>
		</c:if>
	</div> 
</c:if>

<div class="screenActions" style="float: right">
	<select name="template_name" class="dropdown">
		<option value="BUILTIN_HTML" ${default_po_print_template eq 'BUILTIN_HTML'? 'selected': ''} ><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.builtindefaulttemplate"/></option>
		<option value="BUILTIN_TEXT" ${default_po_print_template eq 'BUILTIN_TEXT'? 'selected': ''}><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.builtintexttemplate"/></option>
		<c:forEach var="t" items="${templates}">
			<option value="${t.map.template_name}" ${default_po_print_template eq t.map.template_name? 'selected': ''}>${t.map.template_name}</option>
		</c:forEach>
	</select>

	<insta:selectdb name="printType" table="printer_definition" valuecol="printer_id"
		displaycol="printer_definition_name"  orderby="printer_definition_name" value="${printPref.map.printer_id}"/>

	<c:if test="${viewMode}">
		<button type="button" onclick="printPO();" accessKey='P'>
			<b><u><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.p"/></u></b><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.rint"/>
		</button>
	</c:if>	
</div>

</form>
<form name="dlgForm" id="dlgForm">
<div id="detaildialog" style="visibility:hidden">
<div class="bd">
	<fieldset class="fieldSetBorder">
		<legend class="fieldSetLabel"><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.itemdetails"/></legend>
		<table class="formtable" cellpadding="0" cellspacing="0" border="0" width="100%">
			<c:if test="${prefbarcode eq 'Y'}">
				<tr>
					<td class="formLabel"><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.itembarcode"/>:</td>
					<td>
						<input type="text" name="item_barcode_id" onchange="onChangeBarcode(this.value);" >
					</td>
				</tr>
			</c:if>

			<tr>
				<td class="formLabel"><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.item"/>:</td>
				<td>
					<div id="item_wrapper" style="width: 20em; padding-bottom:2em; ">
						<input type="text" name="medicine_name" style="width: 20em" maxlength="100"/>
						<div id="item_dropdown" class="scrolForContainer"></div>
					</div>
					<input type="hidden" name="medicine_id" value="" >
					<input type="hidden" name="po_pkg_size" value="" >
					<input type="hidden" name="package_type" value="" >
					<input type="hidden" name="package_uom" value="" >
					<input type="hidden" name="issue_units" value="" >
					<input type="hidden" name="billable" value="" >
					<input type="hidden" name="vat_rate" id="vat_rate" value="" >
					<input type="hidden" name="vat" id="vat" value="" >
					<input type="hidden" name="tax_rate" id="tax_rate" value="" >
					<input type="hidden" name="item_code" value=""/>
					<input type="hidden" name="store_id_hid" id="store_id_hid" value=""/>
 					<input type="hidden" name="supplier_code_hid" id="supplier_code_hid" value=""/>
 					<input type="hidden" name="store_package_uom" value=""/>
					<input type="hidden" name="min_rate">
					<input type="hidden" name="discounted_min_rate">
					<input type="hidden" name="min_rate_suppliers">
					<input type="hidden" name="margin">
					<input type="hidden" name="margin_type">
					<input type="hidden" name="max_cost_price">
				</td>
				<td colspan="2">
					<a href="#" onclick="onPurchaseDetails(); return false;" id="itemPur"><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.purchasedetails"/></a>
				</td>
			</tr>
			
			<tr>
				<td class="formlabel"><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.manufacturer"/>:</td>
				<td class="forminfo" style="width:10em" valign="middle"><b><label id="lblManf"></label></b></td>
				<td class="formlabel">${ifn:cleanHtml(storeName)} <insta:ltext key="storeprocurement.raisepurchaseorder.podetails.stock"/>:</td>
				<td class="forminfo" style="width:10em" valign="middle"><b><label id="lblStoreStock"></label></b></td>
				<td class="formlabel" ><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.allstoresstock"/>:</td>
				<td class="forminfo" style="width:10em" valign="middle"><b><label id="lblTotalStock"></label></b></td>
			</tr>
			
			<tr>
				<td class="formlabel"><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.pkgsize"/>:</td>
				<td class="forminfo" style="width:10em" valign="middle"><b><label id="lblPkgSize"></label></b></td>
				<td class="formlabel"><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.packageuom"/>:</td>
				<td class="forminfo" style="width:10em" valign="middle"><b><label id="lblPkgUom"></label></b></td>
				<td class="formlabel"><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.unituom"/>:</td>
				<td class="forminfo" style="width:10em" valign="middle"><b><label id="lblUnitUom"></label></b></td>
			</tr>

			<tr>
				<td class="formLabel"><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.mrp"/>:</td>
				<td>
					<input type="text" name="mrp_display" onkeypress="return enterNumAndDot(event);"
						onChange="onChangeMrp();" maxlength="13">
				</td>
				<td class="formLabel"><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.adjmrp"/>:</td>
				<td>
					<input type="text" name="adj_mrp" id="adj_mrp" value="" readonly/>
				</td>
				<td class="formLabel"><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.rate"/>:</td>
				<td>
					<input type="text" onkeypress="return enterNumAndDot(event);"  name="cost_price_display"
						onChange="onChangeCostPrice();" maxlength="13"/>
						<input type="hidden"   name="supplier_rate_validation" value="false" />
						<input type="hidden"   name="supplier_rate_val" value="" />
				</td>
			</tr>

			<tr>
				<td class="formLabel"><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.qty"/>:</td>
				<td>
					<input type="text" name="qty_req_display" maxlength="8"
					onkeypress="return onKeyPressAddQty(event);" onChange="onChangeQty();"/>
				</td>

				<td class="formLabel"><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.bonus"/>:</td>
				<td>
					<input type="text" name="bonus_qty_req_display" maxlength="8"
					onkeypress="return onKeyPressAddQty(event);" onChange="onChangeBonusQty();"/>
				</td>
			</tr>

			<tr>
				<td class="formLabel"><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.discount.percentage.in.brackets"/></td>
				<td>
					<input type="text" name="discount_per" onkeypress="return enterNumAndDot(event);"
					onChange="onChangeDiscountPer();"/>
				</td>
				<td class="formLabel"><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.discount.amt.in.brackets"/></td>
				<td>
					<input name="discount" type="text" onkeypress="return enterNumAndDot(event);"
					onChange="onChangeDiscountAmt();"/>
				</td>
			</tr>

			<c:if test="${prefCed eq 'Y'}">
				<tr style="display:none;">
					<td class="formLabel" title='<insta:ltext key="storeprocurement.raisepurchaseorder.podetails.centralexciseduty"/>'><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.ced.percentage.in.brackets"/></td>
					<td>
						<input type="text" name="item_ced_per" onkeypress="return enterNumAndDot(event);"
						onChange="onChangeCedPer();" value="0" />
					</td>

					<td class="formLabel" title='<insta:ltext key="storeprocurement.raisepurchaseorder.podetails.centralexciseduty"/>'><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.ced.amt.in.brackets"/></td>
					<td>
						<input type="text" name="item_ced" onkeypress="return enterNumAndDot(event);"
						onChange="onChangeCedAmt();" value="0"/>
					</td>
				</tr>
			</c:if>

			<c:if test="${nextPOstatus eq 'A' || nextPOstatus eq 'AA'}">
				<tr>
					<td class="formLabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.item.status"/>:</td>
					<td><insta:selectoptions name="status" value="" dummyvalue="--Select--" dummyvalueId="" opvalues="A,R" optexts="Approve,Reject"/></td>
					<td class="formLabel"><insta:ltext key="storeprocurement.approve.rejectpo.addshow.remarks"/>:</td>
					<td><input type="text" name="item_remarks"  /></td>
					<td></td>
				</tr>
			</c:if>
		</table>
		
		<c:set var="_taxindex" value="0"/>
		<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel">Tax Details</legend>
			<table class="formtable">
				<tr>
					<td class="formLabel"><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.taxtype"/>:</td>
					<td class="formInfo">
						<select name="vat_type" class="dropdown" onChange="onChangeTaxBasis(this);">
							<option value="MB"><insta:ltext key="storeprocurement.stockentry.invoicedetails.mrpbased.with.bonus"/></option>
							<option value="M"><insta:ltext key="storeprocurement.stockentry.invoicedetails.mrpbased.without.bonus"/></option>
							<option value="CB"><insta:ltext key="storeprocurement.stockentry.invoicedetails.cpbased.with.bonus"/></option>
							<option value="C"><insta:ltext key="storeprocurement.stockentry.invoicedetails.cpbased.without.bonus"/></option>
						</select>
					</td>
					<td class="formLabel"><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.taxrate"/>:</td>
					<td class="formInfo"><label id="lblvat_rate">0</label></td>
					<td class="formLabel"><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.vatamt"/>:</td>
					<td class="formInfo"><label id="lblvat_amt">0</label></td>
				</tr>
				<tr style="display:none;">
					<td class="formLabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.cstrate.percentage.in.brackets"/>:</td>
					<td>
						<input type="text" name="cst_rate" onkeypress="return enterNumAndDot(event);" onchange="makeingDec(this.value,this);onChangeCST()">
					</td>
				</tr>
				<tr>
					<c:forEach items="${groupList}" var="group"> 
						<input type="hidden" name="taxname${group.item_group_id}" id="taxname_${group.item_group_id}" value="${group.item_group_name}">
						<input type="hidden" name="taxrate${group.item_group_id}" id="taxrate_${group.item_group_id}" value="0">
						<input type="hidden" name="taxamount${group.item_group_id}" id="taxamount_${group.item_group_id}" value="0">
						<input type="hidden" name="taxsubgroupid${group.item_group_id}" id="taxsubgroupid_${group.item_group_id}" value="0">
						<input type="hidden" name="oldtaxsubgroupid${group.item_group_id}" id="oldtaxsubgroupid_${group.item_group_id}" value="0">
					</c:forEach>
				</tr>
				<tr id="add_tax_groups"></tr>
			</table>
		</fieldset>
		<c:if test="${prefCed ne 'Y'}">
			<input type="hidden" name="item_ced_per" value="0"/>
			<input type="hidden" name="item_ced" value="0"/>
		</c:if>
	</fieldset>

	<table width="100%">
		<tr>
			<td>
				<button type="button" accesskey="A"
					onclick="onDialogSave();"><label><u><b><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.a"/></b></u><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.dd"/></label></button>
			</td>
			<td>
				<input type="button" value="${cancelText}" onclick="onDialogCancel();" />
			</td>
			<td>
				<button type="button" id="prevDialog" accesskey="V" onclick="onDialogPrevNext(false);" disabled><label> &lt;&lt; <insta:ltext key="storeprocurement.raisepurchaseorder.podetails.pre"/><u><b><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.v"/></b></u><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.ious"/></label></button>
			</td>
			<td>
				<button type="button" id="nextDialog" accesskey="N" onclick="onDialogPrevNext(true);" disabled><label> <u><b><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.n"/></b></u><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.ext"/> &gt;&gt;</label></button>
			</td>
			<td align="right" width="50%">
				<c:if test="${urlRightsMap['mas_medicines'] == 'A'}">
					<button type="button" onclick="openMasterScreen();"><label><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.newitem"/></label></button>
				</c:if>
			</td>
		</tr>
	</table>
</div>
</div>
</form>
<div style="clear: both"></div>
<div class="legend">
	<div class="flag"><img src='${cpath}/images/green_flag.gif'></div>
	<div class="flagText"><insta:ltext key="storemgmt.stockapproval.list.approval.flag"/></div>
	<div class="flag"><img src='${cpath}/images/red_flag.gif'></div>
	<div class="flagText"><insta:ltext key="storemgmt.stockapproval.list.reject.flag"/></div>

</div>
<jsp:include page="/pages/stores/PurchaseDetails.jsp"/>
<script>
var podate = '${poDate}';
</script>
</body>

</html>

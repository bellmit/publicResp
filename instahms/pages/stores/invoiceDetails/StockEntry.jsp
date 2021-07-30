<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ page import="com.insta.hms.stores.StoresDBTablesUtil" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ page import="com.insta.hms.common.Encoder" %>

<%--
This screen works in the following modes:
1. Stock entry without PO (provided strictPO is not set):
 a. Initial Entry: manual additions
 b. Edit GRN: manual additions/increments allowed
2. Stock entry with PO.
 a. Initial Entry: populate PO items automatically
 b. Edit GRN: only manual additions/increments allowed

Note that PO can be selected in this screen, which should be same behaviour as if user
clicked Stock Entry in the PO dashboard. In both cases, it is considered initial stock entry
(ie, not Edit GRN). Edit GRN mode is invoked before coming here. GRN cannot be selected here.

This screen works in the javascript object list mode, where the base list of items in
the grid is represented by a javascript object array. The objects are then transformed
into row-hidden values (for form submission), row-labels and dialog form fields as
and when required. The object list is created from DB stored values or from newly entered
user values. Thus, to edit a GRN we don't loop through the items in the JSP since we
anyway need the js object array, we use that and populate the grid at load time.

TODO:
 * Uniformly handle edit GRN non-editable fields as labels instead of disabling.
 * Barcode print as getPrintUrl paradigm.
 * PO needs VAT/CST, CST Rate, Cess and Other charges so that PO total can be matched against
   GRN total. (Later?)
--%>
<html>

<head>

<c:set var="strictPO" value="${genPrefs.se_with_po == 'Y' &&
	actionRightsMap.direct_stock_entry == 'N' && roleId ne 1 && roleId ne 2}"/>
<c:set var="noItemAddPO" value="${genPrefs.se_with_po == 'N' && roleId ne 1 && roleId ne 2}"/>

<c:set var="prefExpItemProc" value="${genPrefs.expired_items_procurement}"/>
<c:set var="prefProcExpireDays" value="${genPrefs.procurement_expiry_days}"/>
<c:set var="prefStockEntryStatus" value="${genPrefs.stock_entry_agnst_do}"/>
<c:set var="userCenterId" value="${userCenterId}"/>

<c:set var="prefMaxCP" value="${genPrefs.validate_cost_price}"/>
<c:set var="prefQtyType" value="${genPrefs.qty_default_to_issue_unit}"/>
<c:set var="prefDecimalQty" value="${genPrefs.allow_decimals_for_qty}" scope="request"/>
<c:set var="prefCed" value="${genPrefs.show_central_excise_duty}" scope="request"/>
<c:set var="prefVat" value="${genPrefs.vat_applicable}" scope="request"/>
<c:set var="prefCess" value="${genPrefs.cess_applicable}" scope="request"/>
<c:set var="prefbarcode" value="${genPrefs.barcode_for_item}" scope="request"/>
<c:set var="taxLabel" value="${genPrefs.procurement_tax_label}" scope="request"/>

<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="defaultValue" value="${(prefDecimalDigits == 3) ? '0.000' : '0.00'}"/>
<c:set var="allowSave" value="${(empty inv.map.status) || inv.map.status == 'O'}"/>
<c:set var="max_centers_inc_default" value='<%=GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default")%>'/>
<c:set var="applySupplierTaxRules" value="${genPrefs.apply_supplier_tax_rules}" scope="request"/>
<c:set var="applyStrictPoControls" value="${actionRightsMap.apply_strict_po_controls == 'A'}"/>
<c:set var="allowFinalizeCloseGrn" value="${actionRightsMap.allow_finalize_close_grn}"/>
<c:set var="title">
	<c:choose>
		<c:when test="${not allowSave}"><insta:ltext key="storeprocurement.stockentry.invoicedetails.editgrn"/> ${ifn:cleanHtml(grn)}</c:when>
		<c:when test="${editGRN}"><insta:ltext key="storeprocurement.stockentry.invoicedetails.stockentry.editgrn"/> ${ifn:cleanHtml(grn)})</c:when>
		<c:otherwise><insta:ltext key="storeprocurement.stockentry.invoicedetails.stockentry"/></c:otherwise>
	</c:choose>
</c:set>
<c:set var="doAllowStatus">
	<c:choose>
		<c:when test="${fn:toLowerCase(prefStockEntryStatus) eq 'y' && userCenterId != 0}">true</c:when>
		<c:otherwise>false</c:otherwise>
	</c:choose>
</c:set>
<c:set var="doSchemaAllowStatus">
	<c:choose>
		<c:when test="${fn:toLowerCase(prefStockEntryStatus) eq 'y'}">true</c:when>
		<c:otherwise>false</c:otherwise>
	</c:choose>
</c:set>
<title>${title} - <insta:ltext key="storeprocurement.stockentry.invoicedetails.instahms"/></title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<meta name="i18nSupport" content="true"/>
<insta:link type="script" file="ajax.js"/>
<insta:link type="script" file="hmsvalidation.js"/>
<insta:link type="js" file="stores/stockentry.js"/>
<insta:link type="js" file="stores/purchasedetails.js"/>
<insta:link type="script" file="stores/storescommon.js" />
<insta:link type="js" file="stores/registered_supplier_settings_validations.js"/>
<insta:link type="js" file="stores/storeshelper.js"/>

<insta:link type="js" file="widgets.js"/>
<insta:link type="css" file="widgets.css"/>

<script type="text/javascript">
	var gDefaultTaxRate = '${prefVatRate}';
	var gDefaultVatType = '${prefVatType}';
	if ('${prefVat}' == 'N') taxRate = 0;
	var popurl = '${pageContext.request.contextPath}';
	var editGRN = ${editGRN};
	var poNo = '${ifn:cleanJavaScript(ponum)}';
	var poStoreId = '${ifn:cleanJavaScript(poStoreId)}';
	var strictPO = ${strictPO};
	var noItemAddPO = ${noItemAddPO};
	var applyStrictPoControls = ${applyStrictPoControls};;
	var grnNo = '${ifn:cleanJavaScript(grn)}';
	var applyItemLevelCST = '${apply_item_level_cst}';
	var centerId = '${centerId}';
	var invoiceAccGroup = '${invoiceAccGroup}';
	var identifierSeq = '${ifn:cleanJavaScript(seqCount)}';
	var validateCostPrice='${prefMaxCP}';
	var editMaxCP = '${actionRightsMap.change_max_costprice}';
	var qtyDecimal = '${prefDecimalQty}';
	var grnpo_no='${inv.map.po_no}';
	var deptId = '${pharmacyStoreId}';
	var prefCED = '${prefCed}';
	var allowBackDate = '${actionRightsMap.allow_backdate}';
	var prefVAT = '${prefVat}';
	var prefCESS = '${prefCess}';
	var prefBarCode = '${prefbarcode}';
	var sucmsg = '${ifn:cleanJavaScript(param.msg)}';
	var sucflag = '${ifn:cleanJavaScript(param.flag)}';
	var cpath = '${cpath}';
	var packageUOMs = ${packageUOMList};
	var invSupId = '${inv.map.supplier_id}';
	var tcsApplicable = '${inv.map.tcs_applicable}';
	var invSupName = "${inv.map.supplier_name}";
	var gServerNow = new Date(<%= (new java.util.Date()).getTime() %>);
	<c:if test="${not empty grnItemsJSON}">var grnItemsList = ${grnItemsJSON};</c:if>
	<c:if test="${not empty grnItemBatchesJSON}">var grnItemBatches = ${grnItemBatchesJSON};</c:if>
	<c:if test="${not empty suppInvoicesJSON}">var invoicesOfSupp = ${suppInvoicesJSON};</c:if>
	
	var origInvoiceNo = <insta:jsString value="${inv.map.invoice_no}"/>
	var origInvoiceDate =<insta:jsString value ="${inv.map.invoice_date}"/>
	var gRoleId = '${roleId}';
	var gItemMasterTimestamp = '${master_timestamp}';
	var allowSave = ${allowSave};
	var taxLabel = '${taxLabel}';
	var procExpireDays = '${prefProcExpireDays}';
	var prefExpItemProc = '${prefExpItemProc}';
	var doAllowStatus =  eval('${doAllowStatus}');
	var doSchemaAllowStatus = eval('${doSchemaAllowStatus}');
	var applySupplierTaxRules = '${applySupplierTaxRules}';
	var storesListJSON = JSON.parse('${ifn:cleanJavaScript(storesListJSON)}'); 
	var subgroupNamesList = JSON.parse('${ifn:cleanJavaScript(subGroupListJSON)}'); 
	var groupListJSON = JSON.parse('${ifn:cleanJavaScript(groupListJSON)}');
	<c:if test="${not empty grn_tax_details}">var grnTaxJSON = JSON.parse('${ifn:cleanJavaScript(grn_tax_details)}');</c:if>
	var allowTaxEditRights ='${actionRightsMap.allow_tax_subgroup_edit}';
	var fin_yr_start_month = '<%=GenericPreferencesDAO.getAllPrefs().get("fin_year_start_month")%>';
	var fin_yr_end_month = '<%=GenericPreferencesDAO.getAllPrefs().get("fin_year_end_month")%>';
</script>

<style type="text/css">
input.size2 {width:30px;}
table.infotable td.forminfo { text-align: right }
table.infotable td.formlabel { text-align: right }
.scrolForContainer .yui-ac-content{
	 max-height:18em;overflow:auto;overflow-x:auto; /* scrolling */
    _height:18em; max-width:35em; width:35em;/* ie6 */
}
</style>
<insta:js-bundle prefix="stores.procurement"/>
</head>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<body onload="init();" class="yui-skin-sam">
<c:set var="dummyvalue">
	<insta:ltext key="selectdb.dummy.value"/>
</c:set>
<c:set var="approvalstatus">
 <insta:ltext key="storemgmt.stockapproval.list.approved"/>,
<insta:ltext key="storemgmt.stockapproval.list.unapproved"/>
</c:set>
<c:set var="amountstatus">
 <insta:ltext key="storemgmt.stockapproval.list.percent"/>,
<insta:ltext key="storemgmt.stockapproval.list.amount"/>
</c:set>
<c:set var="uomstatus">
 <insta:ltext key="storemgmt.stockapproval.list.packageuom"/>,
<insta:ltext key="storemgmt.stockapproval.list.unituom"/>
</c:set>
<c:set var="cststatus">
<insta:ltext key="storemgmt.stockapproval.list.${taxLabel}"/>,<insta:ltext key="storemgmt.stockapproval.list.cst.${taxLabel}"/><c:if test="${applySupplierTaxRules == 'true' }">,<insta:ltext key="storemgmt.stockapproval.list.not.applicable.text"/></c:if>
</c:set>
<c:set var="cstvalues">
<insta:ltext key="storemgmt.stockapproval.list.${taxLabel}"/>,<insta:ltext key="storemgmt.stockapproval.list.cst.${taxLabel}"/><c:if test="${applySupplierTaxRules == 'true' }">,<insta:ltext key="storemgmt.stockapproval.list.not.applicable"/></c:if>
</c:set>
<c:set var="vatstatus">
<insta:ltext key="storemgmt.stockapproval.list.${taxLabel}"/>
</c:set>
<c:set var="addnewitem">
<insta:ltext key="storemgmt.stockapproval.list.addnewitem"/>
</c:set>
<c:set var="mrpstatus">
<insta:ltext key="storeprocurement.stockentry.invoicedetails.mrpbased.with.bonus"/>,
<insta:ltext key="storeprocurement.stockentry.invoicedetails.mrpbased.without.bonus"/>,
<insta:ltext key="storeprocurement.stockentry.invoicedetails.cpbased.with.bonus"/>,
<insta:ltext key="storeprocurement.stockentry.invoicedetails.cpbased.without.bonus"/>
</c:set>
<c:set var="centralexciseduty">
 <insta:ltext key="storeprocurement.stockentry.invoicedetails.centralexciseduty"/>
</c:set>
<div id="checkDefaultStore" style="display: block;">
<form name="directstockform" method="POST" enctype="multipart/form-data"
		action="stockentry.do?_method=insertInvoiceStock">
	<input type="hidden" name="countSeq" id="countSeq" value=""/>
	<input type="hidden" name="originalst" id="originalst" value=""/>
	<input type="hidden" name="grn_no" value="${ifn:cleanHtmlAttribute(grn)}"/>
	<input type="hidden" name="_printAfterSave" value="N"/>
	<input type="hidden" name="doAllowStatus" value="${doAllowStatus}"/>
	<input type="hidden" name="doSchemaAllowStatus" value="${ifn:cleanHtmlAttribute(doSchemaAllowStatus)}"/>
    <input type="hidden" id="grn_count" name="grn_count" value="<%= Encoder.cleanHtmlAttribute((String)request.getAttribute("grn_count")) %>"/>
	<input type="hidden" id="grnPrintTemplateHid" name="grnPrintTemplate">
	<h1>${title}</h1>

	<insta:feedback-panel/>

	<fieldset class="fieldSetBorder" >
		<c:choose>
			<c:when test="${doAllowStatus || doSchemaAllowStatus}">
				<legend class="fieldSetLabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.dodetails"/></legend>
			</c:when>
			<c:otherwise>
				<legend class="fieldSetLabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.invoicedetails"/></legend>
			</c:otherwise>
		</c:choose>
		<table class="formtable" cellpadding="0" cellspacing="0" border="0" width="100%">
			<tr>
				<td class="formlabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.store"/>: </td>
				<td>
					<c:choose>
						<c:when test="${editGRN}">
							<b><insta:getStoreName store_id="${inv.map.store_id}"/></b>
							 <input type="hidden" name="store_id" id="store" value="${inv.map.store_id}">
						</c:when>
						<c:when test="${multiStoreAccess eq 'A' || roleId eq 1 || roleId eq 2}">
							<insta:userstores username="${userid}" elename="store_id" onchange="return onChangeStore();"
								val="${pharmacyStoreId}"/>
						</c:when>
						<c:otherwise>
							<b><insta:getStoreName store_id="${pharmacyStoreId}"/></b>
							<input type="hidden" name="store_id" id="store" value="${pharmacyStoreId}">
						</c:otherwise>
					</c:choose>
				</td>

				<td class="formlabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.ponumber"/>:</td>
				<td class="forminfo">
					<c:choose>
						<c:when test="${editGRN}">
							${inv.map.po_no } <input type="hidden" name="po_no" id="po_no" value="${inv.map.po_no }"/>
						</c:when>
						<c:otherwise>
							<select name="po_no" id="po_no" class="dropdown" onchange="onChangePO();">
								<option>${dummyvalue}</option>
							</select>
						</c:otherwise>
					</c:choose>
				</td>

				<td class="formlabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.quantityuom"/>:</td>
				<td>
					<insta:selectoptions name="grn_qty_unit" opvalues="P,I" optexts="${uomstatus}"
						value="${editGRN ? inv.map.grn_qty_unit : (prefQtyType == 'Y' ? 'I' : 'P')}"
						onChange="return onChangeQtyUOM();"/>
				</td>
			</tr>

			<tr>
				<td class="formlabel"><span class="prestar">*</span><insta:ltext key="storeprocurement.stockentry.invoicedetails.supplier"/>:</td>
				<td valign="top">
					<div id="supplier_wrapper" style="width: 15em;">
						<input type="text" name="supplierName" id="supplierName" style="width: 11.25em" onblur="resetTaxTypeValues(this);"
							value="${inv.map.supplier_name }"/>
						<div id="suppliername_dropdown" class="scrolForContainer"></div>
						<input type="hidden" name="supplier_id" value="${inv.map.supplier_id}"/>
					</div>
				</td>
				<td class="formlabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.supplieraddress"/>:</td>
				<td colspan="4" class="forminfo">
					<insta:truncLabel id="suppAddId" name="suppAddId" value="${inv.map.supplier_address}" length="60"/>
				</td>
			</tr>

			<tr>
				<td class="formlabel"><span class="prestar">*</span>
				<c:choose>
					<c:when test="${doAllowStatus || doSchemaAllowStatus}"><insta:ltext key="storeprocurement.stockentry.invoicedetails.dono"/>:</c:when>
					<c:otherwise><insta:ltext key="storeprocurement.stockentry.invoicedetails.invoiceno"/>:</c:otherwise>
				</c:choose></td>
				<td>
					<input type="text" name="invoice_no" maxlength="99" value="${inv.map.invoice_no }"
						onchange="return checkDuplicateInvoice(this);">
				</td>

				<td class="formlabel"><span class="prestar">*</span>
				<c:choose>
					<c:when test="${doAllowStatus || doSchemaAllowStatus}"><insta:ltext key="storeprocurement.stockentry.invoicedetails.dodate"/>:</c:when>
					<c:otherwise><insta:ltext key="storeprocurement.stockentry.invoicedetails.invoicedate"/>:</c:otherwise>
				</c:choose></td>
				<td>
					<insta:datewidget name="invoice_date" valid="past" value="${not empty inv.map.invoice_date ? inv.map.invoice_date : 'today'}" btnPos="left"
						extravalidation="onChangeInvDate();"/>
				</td>

				<td class="formlabel"><span class="prestar">*</span><insta:ltext key="storeprocurement.stockentry.invoicedetails.duedate"/>:</td>
			 	<td>
					<insta:datewidget name="due_date"
						value="${not empty inv.map.due_date ? inv.map.due_date : 'today'}" btnPos="left"/>
			 	</td>
			</tr>

			<tr>
			 	<td class="formlabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.status"/>: </td>
			 	<c:if test="${(empty inv.map.status) || inv.map.status == 'O'}">
				 	<td class="forminfo">
				 		<insta:ltext key="storeprocurement.stockentry.invoicedetails.status.open"/>
				 	</td>
				 </c:if>
				 <c:if test="${inv.map.status == 'F'}">
				 	<td class="forminfo">
				 		<insta:ltext key="storeprocurement.stockentry.invoicedetails.status.finalize"/>
				 	</td>
				 </c:if>
				 <c:if test="${inv.map.status == 'C'}">
				 	<td class="forminfo">
				 		<insta:ltext key="storeprocurement.stockentry.invoicedetails.status.close"/>
				 	</td>
			 	</c:if>
			 	<input type="hidden" name="status" value="${empty inv.map.status ? 'O' : inv.map.status}"/>
			 	<%-- 
				<td>
					<insta:selectoptions name="status" value="${empty inv.map.status ? 'F' : inv.map.status}"
					 	opvalues="O,F,C" optexts="Open,Finalized,Closed" style="width:8em;"/>
				</td>
				--%>
				<td class="formlabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.stocktype"/>:</td>
				<td class="forminfo">
					<c:choose>
					 <c:when test="${empty grn}">
						<select  name="consignment_stock" class="dropdown">
							<option value="false"><insta:ltext key="storeprocurement.stockentry.invoicedetails.normalstock"/></option>
							<option value="true"><insta:ltext key="storeprocurement.stockentry.invoicedetails.consignmentstock"/></option>
						</select>
					 </c:when>
					 <c:otherwise>
					 	<c:choose>
					 		<c:when test="${inv.map.stock_type == 't'}">
					 			<insta:ltext key="storeprocurement.stockentry.invoicedetails.consignmentstock"/>
					 		</c:when>
					 		<c:otherwise>
					 			<insta:ltext key="storeprocurement.stockentry.invoicedetails.normalstock"/>
					 		</c:otherwise>
					 	</c:choose>
					 	<input type="hidden" name="consignment_stock" value="${inv.map.stock_type}">
					 </c:otherwise>
					 </c:choose>
				</td>
				<c:choose>
					<c:when test="${doAllowStatus}">
						<td class="formlabel" style="display:none"><insta:ltext key="storeprocurement.stockentry.invoicedetails.cashpurchase"/>:</td>
						<td style="display:none"><input type="checkbox" name="cash_purchase" value="Y"
							<c:if test="${inv.map.cash_purchase eq 'Y'}"><insta:ltext key="storeprocurement.stockentry.invoicedetails.checked"/></c:if>	style="margin: 0px; padding: 0px" />
						</td>
					</c:when>
					<c:otherwise>
						<td class="formlabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.cashpurchase"/>:</td>
							<td><input type="checkbox" name="cash_purchase" value="Y"
							<c:if test="${inv.map.cash_purchase eq 'Y'}"><insta:ltext key="storeprocurement.stockentry.invoicedetails.checked"/></c:if>	style="margin: 0px; padding: 0px" />
						</td>
					</c:otherwise>
				</c:choose>
				
			</tr>

			<tr>
				<td class="formlabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.poreference"/>:</td>
				<td >
					<input type="text" name="po_reference" value="${inv.map.po_reference}" size="50" maxlength="200">
				</td>

				<c:choose>
					<c:when test="${doAllowStatus}">
						<td class="formlabel" style="display:none"><insta:ltext key="storeprocurement.stockentry.invoicedetails.itemdiscounttype"/>:</td>
						<td style="display:none">
							<insta:selectoptions name="item_discount_type" value="P" opvalues="P,A" optexts="${amountstatus}" />
						</td>
						
						<td class="formlabel" style="display:none"><insta:ltext key="storeprocurement.stockentry.invoicedetails.itemdiscountvalue"/>:</td>
						<td style="display:none">
							<input type="text" name="itemdiscount" id="itemdiscount" value="${defaultValue }"
								onkeypress="return enterNumAndDot(event);" onchange="return makeingDec(this.value,this)"/>
						</td>
						
						<td class="formlabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.remarks"/>:</td>
							<td>
								<input type="text" name="remarks" id="remarks" value="${inv.map.remarks}" maxlength="3999"/>
								<input type="hidden" name="tax_name" value="VAT"/>
								<input type="hidden" name="cess_tax_rate" value="0"/>
								<input type="hidden" name="cess_tax_amt" value="0"/>
							</td>
							
							<td class="formlabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.uploaddocopy"/>:</td>
							<td colspan="2">
								<input type="file" name="invoiceAttachment" accept="<insta:ltext key="upload.accept.image"/>,<insta:ltext key="upload.accept.document"/>"/>
								<c:if test="${editGRN && (not empty inv.map.invoice_file_name)}">
									<c:url var="invUrl" value="/stores/stockentry.do">
										<c:param name="_method" value="getUploadedInvoiceCopy"/>
										<c:param name="suppinvid" value="${inv.map.supplier_invoice_id}"/>
									</c:url>
									<a href="${invUrl}" target="blank"><insta:ltext key="storeprocurement.stockentry.invoicedetails.view"/></a>
								</c:if>
								<c:if test="${editGRN && (not empty inv.map.invoice_file_name)}">
									<insta:ltext key="storeprocurement.stockentry.invoicedetails.delete"/>:
									<input type="checkbox" name="deleteUploadedInvoice" value="Y" onclick="onClickDeleteInvoice()"/>
								</c:if>
							</td>
					</c:when>
					<c:otherwise>
						<td class="formlabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.itemdiscounttype"/>:</td>
						<td>
							<insta:selectoptions name="item_discount_type" value="P" opvalues="P,A" optexts="${amountstatus}" />
						</td>
		
						<td class="formlabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.itemdiscountvalue"/>:</td>
						<td>
							<input type="text" name="itemdiscount" id="itemdiscount" value="${defaultValue }"
								onkeypress="return enterNumAndDot(event);" onchange="return makeingDec(this.value,this)"/>
						</td>
					</c:otherwise>
				</c:choose>
			</tr>

			<tr style="display:none;">
				<c:choose>
					<c:when test="${doAllowStatus}">
						<td class="formlabel" style="display:none"><insta:ltext key="storeprocurement.stockentry.invoicedetails.taxtype"/>:</td>
						<td style="display:none">
							<insta:selectoptions name="tax_name" opvalues="${cstvalues}" optexts="${cststatus}"
							value="${empty grn ? 'VAT' : inv.map.tax_name}"
							onchange="onChangeTaxName();" class="dropdown"/>
						</td>
	
						<td class="formlabel" style="display:none"><insta:ltext key="storeprocurement.stockentry.invoicedetails.cstrate"/>:</td>
						<td style="display:none">
							<input type="text" name="main_cst_rate"
							value="${not empty inv.map.cst_rate ? inv.map.cst_rate : 0}"
							onkeypress="return enterNumAndDot(event);" readonly onchange="makeingDec(this.value,this);onChangeCST()">%
						</td>
	
						<c:choose>
							<c:when test="${prefCess eq 'Y'}">
								<td class="formlabel" style="display:none"><insta:ltext key="storeprocurement.stockentry.invoicedetails.cessrate"/>:</td>
								<td style="display:none">
									<input type="text" name="cess_tax_rate"
									value="${not empty inv.map.cess_tax_rate ? inv.map.cess_tax_rate : 0}"
									onkeypress="return enterNumAndDot(event);"
									onchange="makeingDec(this.value,this),onChangeCessRate()"/>(%)
					 				<input type="hidden" name="cess_tax_amt" value="0"/>
								</td>
							</c:when>
							<c:otherwise>
								<td style="display:none">
					 				<input type="hidden" name="cess_tax_rate" value="0"/>
					 				<input type="hidden" name="cess_tax_amt" value="0"/>
					 			</td>
							</c:otherwise>
						</c:choose>
					</c:when>
					<c:otherwise>
						<td class="formlabel" ><insta:ltext key="storeprocurement.stockentry.invoicedetails.taxtype"/>:</td>
						<td >
							<insta:selectoptions name="tax_name" opvalues="${cstvalues}" optexts="${cststatus}"
							value="${empty grn ? 'VAT' : inv.map.tax_name}"
							onchange="onChangeTaxName();" class="dropdown"/>
						</td>
	
						<td class="formlabel"style="display:none" name="cstRateH"><insta:ltext key="storeprocurement.stockentry.invoicedetails.cstrate"/>:</td>
						<td style="display:none" name="cstRateH">
							<input type="text" name="main_cst_rate"
							value="${not empty inv.map.cst_rate ? inv.map.cst_rate : 0}"
							onkeypress="return enterNumAndDot(event);" readonly onchange="makeingDec(this.value,this);onChangeCST()">%
						</td>
	
						<c:choose>
							<c:when test="${prefCess eq 'Y'}">
								<td class="formlabel" ><insta:ltext key="storeprocurement.stockentry.invoicedetails.cessrate"/>:</td>
								<td >
									<input type="text" name="cess_tax_rate"
									value="${not empty inv.map.cess_tax_rate ? inv.map.cess_tax_rate : 0}"
									onkeypress="return enterNumAndDot(event);"
									onchange="makeingDec(this.value,this),onChangeCessRate()"/>%
					 				<input type="hidden" name="cess_tax_amt" value="0"/>
								</td>
							</c:when>
							<c:otherwise>
								<td>
					 				<input type="hidden" name="cess_tax_rate" value="0"/>
					 				<input type="hidden" name="cess_tax_amt" value="0"/>
					 			</td>
							</c:otherwise>
						</c:choose>
					</c:otherwise>
				</c:choose>				
			</tr>


			<tr>
				<c:choose>
					<c:when test="${doAllowStatus}">
						<td class="formlabel" style="display:none"><insta:ltext key="storeprocurement.stockentry.invoicedetails.remarks"/>:</td>
						<td style="display:none">
							<input type="text" name="remarks" id="remarks" value="${inv.map.remarks}" maxlength="3999"/>
							<input type="hidden" name="tax_name" value="VAT"/>
							<input type="hidden" name="cess_tax_rate" value="0"/>
							<input type="hidden" name="cess_tax_amt" value="0"/>
						</td>
						
						<td class="formlabel" style="display:none"><insta:ltext key="storeprocurement.stockentry.invoicedetails.paymentremarks"/>:</td>
						<td style="display:none">
							<input type="text" name="payment_remarks" id="payment_remarks" value="${inv.map.payment_remarks}" maxlength="100"/>
						</td>
						
						<td class="formlabel" style="display:none"><insta:ltext key="storeprocurement.stockentry.invoicedetails.uploaddocopy"/>:</td>
						<td colspan="2" style="display:none">
							<input type="file" name="invoiceAttachment" accept="<insta:ltext key="upload.accept.image"/>,<insta:ltext key="upload.accept.document"/>"/>
							<c:if test="${editGRN && (not empty inv.map.invoice_file_name)}">
								<c:url var="invUrl" value="/stores/stockentry.do">
									<c:param name="_method" value="getUploadedInvoiceCopy"/>
									<c:param name="suppinvid" value="${inv.map.supplier_invoice_id}"/>
								</c:url>
								<a href="${invUrl}" target="blank"><insta:ltext key="storeprocurement.stockentry.invoicedetails.view"/></a>
							</c:if>
							<c:if test="${editGRN && (not empty inv.map.invoice_file_name)}">
								<insta:ltext key="storeprocurement.stockentry.invoicedetails.delete"/>:
								<input type="checkbox" name="deleteUploadedInvoice" value="Y" onclick="onClickDeleteInvoice()"/>
							</c:if>
						</td>
					</c:when>
					<c:otherwise>
						<td class="formlabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.remarks"/>:</td>
						<td>
							<input type="text" name="remarks" id="remarks" value="${inv.map.remarks}" maxlength="3999"/>
							<input type="hidden" name="tax_name" value="VAT"/>
							<input type="hidden" name="cess_tax_rate" value="0"/>
							<input type="hidden" name="cess_tax_amt" value="0"/>
						</td>
				
						<td class="formlabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.paymentremarks"/>:</td>
						<td>
							<input type="text" name="payment_remarks" id="payment_remarks" value="${inv.map.payment_remarks}" maxlength="100"/>
						</td>
						<c:choose>
							<c:when test="${doSchemaAllowStatus}">
								<td class="formlabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.uploaddocopy"/>:</td>
							</c:when>
							<c:otherwise>
								<td class="formlabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.uploadinvoicecopy"/>:</td>
							</c:otherwise>
						</c:choose>
						<td colspan="2">
							<input type="file" name="invoiceAttachment" accept="<insta:ltext key="upload.accept.image"/>,<insta:ltext key="upload.accept.document"/>"/>
							<c:if test="${editGRN && (not empty inv.map.invoice_file_name)}">
								<c:url var="invUrl" value="/stores/stockentry.do">
									<c:param name="_method" value="getUploadedInvoiceCopy"/>
									<c:param name="suppinvid" value="${inv.map.supplier_invoice_id}"/>
								</c:url>
								<a href="${invUrl}" target="blank"><insta:ltext key="storeprocurement.stockentry.invoicedetails.view"/></a>
							</c:if>
							<c:if test="${editGRN && (not empty inv.map.invoice_file_name)}">
								<insta:ltext key="storeprocurement.stockentry.invoicedetails.delete"/>:
								<input type="checkbox" name="deleteUploadedInvoice" value="Y" onclick="onClickDeleteInvoice()"/>
							</c:if>
						</td>
					</c:otherwise>
				</c:choose>
			</tr>
			<tr style="display:none;">
				<td class="formlabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.cform"/>:</td>
					<td><input type="checkbox" name="c_form" value ="Y" onclick="validateCformTaxrate(this)" disabled
						<c:if test="${inv.map.c_form eq true}"><insta:ltext key="storeprocurement.stockentry.invoicedetails.checked"/></c:if>	style="margin: 0px; padding: 0px" />
				</td>
				<td class="formlabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.form8H"/>:</td>
					<td><input type="checkbox" name="form_8h" value ="Y" onclick="validate8HformTaxrate(this)" 
						${inv.map.form_8h eq true or (not empty inv  and not empty inv.map.status)   ? 'disabled' : ''}
							<c:if test="${inv.map.form_8h eq true}"><insta:ltext key="storeprocurement.stockentry.invoicedetails.checked"/></c:if> style="margin: 0px; padding: 0px" />
				</td>
			</tr>
			<tr>
				<td class="formlabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.purposeofpurchase"/>:</td>
				<td><input type="text" name="purpose_of_purchase" id="purpose_of_purchase" value="${inv.map.purpose_of_purchase}" maxlength="70"/></td>
			</tr>
		</table>
	</fieldset>
	
	<fieldset class="fieldSetBorder" >
		<legend class="fieldSetLabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.consignmentdetails"/></legend>
			<table class="formtable" cellpadding="0" cellspacing="0" border="0" width="100%">
				<tr>
				<td class="formlabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.companyname"/>:</td>
					<td><input type="text" name="company_name" id="company_name" value="${inv.map.company_name}" maxlength="100"/></td>
				
				<td class="formlabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.meansoftransport"/>:</td>
					<td><input type="text" name="means_of_transport" id="means_of_transport" value="${inv.map.means_of_transport}" maxlength="50"/></td>
				
				<td class="formlabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.consignmentno"/>:</td>
					<td><input type="text" name="consignment_no" id="consignment_no" value="${inv.map.consignment_no}" maxlength="50"/></td>
				
			</tr>
			<tr>
				<td class="formlabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.consignmentdate"/>:</td>
				<td>
					<insta:datewidget name="consignment_date"  value="${not empty inv.map.consignment_date ? inv.map.consignment_date : ''}" btnPos="left"/>
				</td>
			</tr>
		</table>
	</fieldset>

	<table class="detailList dialog_displayColumns" width="100%" cellspacing="0" cellpadding="0" id="medtabel">
		<tr>
			<th title='<insta:ltext key="storeprocurement.stockentry.invoicedetails.itemname"/>'><insta:ltext key="storeprocurement.stockentry.invoicedetails.item"/></th>
			<th><insta:ltext key="storeprocurement.stockentry.invoicedetails.code"/></th>
			<th><insta:ltext key="storeprocurement.stockentry.invoicedetails.b.slash.s.no"/></th>
			<th><insta:ltext key="storeprocurement.stockentry.invoicedetails.exp"/></th>
			<th style="text-align:right;"><insta:ltext key="storeprocurement.stockentry.invoicedetails.pkg"/></th>
			<c:if test="${!doAllowStatus}">
					<th style="text-align:right;"><insta:ltext key="storeprocurement.stockentry.invoicedetails.mrp"/></th>
					<th style="text-align:right;"><insta:ltext key="storeprocurement.stockentry.invoicedetails.rate"/></th>
			</c:if>
			<th style="text-align:right;"><insta:ltext key="storeprocurement.stockentry.invoicedetails.qty"/></th>
			<th style="text-align:right;"><insta:ltext key="storeprocurement.stockentry.invoicedetails.bonus"/></th>
			<th style="text-align:right;"><insta:ltext key="storeprocurement.stockentry.invoicedetails.uom"/></th>
			<c:if test="${!doAllowStatus}">
				<th style="text-align:right;"><insta:ltext key="storeprocurement.stockentry.invoicedetails.tax"/></th>
				<th style="text-align:right;"><insta:ltext key="storeprocurement.stockentry.invoicedetails.taxbasis"/></th>
			</c:if>
			<c:if test="${!doAllowStatus}">
				<th style="text-align:right;"><insta:ltext key="storeprocurement.stockentry.invoicedetails.disc.per"/></th>
				<th style="text-align:right;"><insta:ltext key="storeprocurement.stockentry.invoicedetails.disc"/></th>
				<th style="text-align:right;"><insta:ltext key="storeprocurement.stockentry.invoicedetails.scheme.disc"/></th>
				<th style="text-align:right;" ><insta:ltext key="storeprocurement.stockentry.invoicedetails.taxamt"/></th>
				<th style="text-align:right;"><insta:ltext key="storeprocurement.stockentry.invoicedetails.amt"/></th>
			</c:if>
			<c:if test="${allowSave}">
				<th style="width: 10px;"></th>
				<th style="width: 10px;"></th>
			</c:if>
		</tr>

		<tr style="display:none">
			<td class="forminfo" style="width:25em;padding-left: 0.5em;white-space:normal;" valign="middle">
				<label></label>
				<input type="hidden" name="medicine_name" value="">
				<input type="hidden" name="medicine_id" value="">
				<input type="hidden" name="batch_no" value="" >
				<input type="hidden" name="exp_dt" value="" >
				<input type="hidden" name="tax_type"value="">
				<input type="hidden" name="newbatch" value="">
				<input type="hidden" name="grn_pkg_size" value="">
				<input type="hidden" name="grn_package_uom" value="">
				<input type="hidden" name="mrp" value="" >
				<input type="hidden" name="cost_price" value=""  >
				<input type="hidden" name="adj_mrp" value="" >
				<input type="hidden" name="billed_qty" value="" >
				<input type="hidden" name="po_billed_qty" value="" >
				<input type="hidden" name="bonus_qty" value=""  >
				<input type="hidden" name="po_bonus_qty" value=""  >
				<input type="hidden" name="tax_rate" value="" >
				<input type="hidden" name="tax" value="">
				<input type="hidden" name="bonus_tax" value="">
				<input type="hidden" name="item_ced_per" value="">
				<input type="hidden" name="item_ced" value="">
				<input type="hidden" name="discount" value="" >
				<input type="hidden" name="discount_per" value="">
				<input type="hidden" name="scheme_discount" value="" >
				<input type="hidden" name="scheme_discount_per" value="">
				<input type="hidden" name="asset_approved" value="">
				<input type="hidden" name="med_total" value="">
				<input type="hidden" name="grnmed" value="">
				<input type="hidden" name="pomed" value="">
				<input type="hidden" name="package_uom" value=""/>
				<input type="hidden" name="issue_units" value=""/>
				<input type="hidden" name="issue_base_unit" value=""/>
				<input type="hidden" name="item_batch_id"/>
				<input type="hidden" name="candelete" />
				<input type="hidden" name="cst_rate"/>
				<input type="hidden" name="supplier_rate_validation" value="false" />
				<input type="hidden" name="max_cost_price" value="" />
				<input type="hidden" name="item_max_cost_price" value="" />
				<input type="hidden" name="code_type" value="" />
				<input type="hidden" name="item_code" value="" />
				<c:set var="_taxindex" value="0"/>
				<c:forEach items="${groupList}" var="group">
					<c:set var="_taxindex" value="${_taxindex+1}"/>
						<input type="hidden" name="taxname${group.item_group_id}" value="" />
						<input type="hidden" name="taxrate${group.item_group_id}" value="" />
						<input type="hidden" name="taxamount${group.item_group_id}" value="" />
						<input type="hidden" name="taxsubgroupid${group.item_group_id}" value="" />
						<input type="hidden" name="oldtaxsubgroupid${group.item_group_id}" value="" />
				</c:forEach>
			</td>
			<td>
				<label></label>
			</td>
			<td>
				<label></label>
			</td>
			<td><label></label></td>
			<td style="text-align:right;"><label></label></td>
			<c:if test="${!doAllowStatus}">
				<td style="text-align:right;"><label></label></td>
				<td style="text-align:right;"><label></label></td>
			</c:if>
			<td style="text-align:right;"><label></label></td>
			<td style="text-align:right;"><label></label></td>
			<td style="text-align:right;"><label></label></td>
			<c:if test="${!doAllowStatus}">
				<td style="text-align:right;"><label></label></td>
				<td style="text-align:right;"><label></label></td>
			</c:if>
			<c:if test="${!doAllowStatus}">
				<td style="text-align:right;"><label></label>
				</td>
				<td style="text-align:right;"><label></label>
				</td>
				<td style="text-align:right;"><label></label></td>
				<td style="text-align:right;"><label></label></td>
				<td style="text-align:right;"><label></label></td>
			</c:if>
			<c:if test="${allowSave}">
				<td>
					<label>
						<img class="imgDelete" src="${cpath}/icons/delete.gif"
						onclick="onDeleteRow(this)" style="cursor:pointer" />
					</label>
				</td>

				<td>
					<label>
						<img class="button" name="editicon" onclick="onEditRow(this)"
						src="${cpath }/icons/Edit.png">
					</label>
				</td>
			</c:if>

		</tr>
	</table>

	<table class="addButton">
		<tr>
			<td align="right">
				<c:if test="${allowSave && !doSchemaAllowStatus}">
					<button type="button" name="plusItem" id="plusItem" title='<insta:ltext key="storeprocurement.stockentry.invoicedetails.addnewitem"/>'
						onclick="openAddDialog(); return false;" accesskey="+"
			 			class="imgButton" ><img src="${cpath}/icons/Add.png"></button>
				</c:if>
			</td>
		</tr>
	</table>

	<c:choose>
		<c:when test="${doAllowStatus}">
			<fieldset class="fieldSetBorder" style="display:none;">
				<legend class="fieldSetLabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.totals"/></legend>
				<table class="compactform" width="100%">
					<tr>
						<td class="formlabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.totalitemdiscount"/>:</td>
						<td class="forminfo">
							<label id="lblTotalDisc">${defaultValue}</label>
						</td>
						<td class="formlabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.totalitemschemediscount"/>:</td>
						<td class="forminfo">
							<label id="lblTotalschemeDisc">${defaultValue}</label>
						</td>
		
					  <td class="formlabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.totalitemamount"/>:</td>
						<td class="forminfo">
							<label id="lblTotalItemAmt">${defaultValue}</label>
						</td>
		
		
					</tr>
		
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
					  	<td class="formlabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.taxamount"/>:</td>
						<td class="forminfo">
							<label id="lblTotalVat">${defaultValue}</label>
						</td>
					  </tr>
					  <tr>
						<td class="formlabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.inv.discounttype"/>:</td>
						<td>
							<insta:selectoptions name="inv_discount_type" class="dropdown" style="width: 7em"
							opvalues="P,A" optexts="${amountstatus}" value="${empty grn ? 'P' : inv.map.discount_type}"
							onchange="onChangeInvDiscType();"/>
						</td>
		
						<td class="formlabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.inv.discountvalue"/>:</td>
						<td>
							<input type="text" name="inv_discount_val"
							value="${empty grn ? defaultValue : inv.map.discount_type eq 'P' ? inv.map.discount_per : inv.map.discount}"
							onkeypress="return enterNumAndDot(event);" onchange="onChangeInvDiscVal()" />
						</td>
						<td class="formlabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.transportcharges"/>:</td>
						<td>
							<input type="text" name="transportation_charges" value="${not empty inv.map.transportation_charges ? inv.map.transportation_charges : defaultValue }" onkeypress="return enterNumAndDot(event);" onchange="onChangeOtherCharges();return makeingDec(this.value,this)" maxlength="13"/>
						</td>
					  <td class="formlabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.inv.discountamount"/>:</td>
						<td class="forminfo">
							<label id="lblInvDisc">${defaultValue}</label>
							<input type="hidden" name="inv_discount" value="${inv.map.discount}"/>
							<input type="hidden" name="inv_discount_per" value="${inv.map.discount_per}"/>
						</td>
		
					</tr>
					<tr style="display:none;" id ="tcsRow">
						<td class="formlabel"><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.tcs.type"/>:</td>
						<td>
							<insta:selectoptions name="tcs_type" opvalues="P,A" optexts="${amountstatus}" style="width: 7em"
								value="${empty grn ? 'P' : inv.map.tcs_type}" onchange="onChangeTcsType()"/>
						</td>

						<td class="formlabel"><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.tcs.value"/>:</td>
						<td>
							<input type="text" name="tcs_value"
								value="${empty grn ? defaultValue : inv.map.tcs_type eq 'P' ? inv.map.tcs_per : inv.map.tcs_amount}"
								onkeypress="return enterNumAndDot(event);" onChange="return onChangeTcsVal()"/>
							<input type="hidden" name="inv_tcs_per" value="${inv.map.tcs_per}"/>
							<input type="hidden" name="inv_tcs_amount" value="${inv.map.tcs_amount}"/>
						</td>
						<td class="formlabel"><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.tcs.amount"/>:</td>
						<td class="forminfo"><label id="lblGrnTcsAmount">${inv.map.tcs_amount}</label></td>
					</tr>
		
					<tr>
						<td class="formlabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.othercharges"/>:</td>
						<td>
							<input type="text" name="other_charges" value="${not empty inv.map.other_charges ? inv.map.other_charges : defaultValue }" onkeypress="return enterNumAndDot(event);" onchange="onChangeOtherCharges();return makeingDec(this.value,this)"/>
						</td>
		
						<td class="formlabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.roundoff"/>:</td>
						<td>
							<input type="text" name="round_off" value="${not empty inv.map.round_off ? inv.map.round_off : defaultValue }" onkeypress="return enterNumAndDotAndMinus(event);" onchange="onChangeRoundOff();return makeingDec(this.value,this)"/>
						</td>
		
						<td class="formlabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.debitamount"/>:</td>
						<td>
							<input type="text" name="debit_amt"
							value="${not empty inv.map.debit_amt ? inv.map.debit_amt : defaultValue}"
							onkeypress="return enterNumAndDot(event);"
							onchange="return makeingDec(this.value,this)"/>
						</td>
		
					  <td class="formlabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.invoicetotal"/>:</td>
					  <td class="forminfo"><label id="lblGrandTotal">${defaultValue}</label></td>
					</tr>
				</table>
			</fieldset>
		</c:when>
		<c:otherwise>
			<fieldset class="fieldSetBorder">
				<legend class="fieldSetLabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.totals"/></legend>
				<table class="compactform" width="100%">
					<tr>
						<td class="formlabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.totalitemdiscount"/>:</td>
						<td class="forminfo">
							<label id="lblTotalDisc">${defaultValue}</label>
						</td>
						<td class="formlabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.totalitemschemediscount"/>:</td>
						<td class="forminfo">
							<label id="lblTotalschemeDisc">${defaultValue}</label>
						</td>
		
					  <td class="formlabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.totalitemamount"/>:</td>
						<td class="forminfo">
							<label id="lblTotalItemAmt">${defaultValue}</label>
						</td>
		
		
					</tr>
		
					<tr>
						<c:set var="_taxindex" value="0"/>
						<c:forEach items="${groupList}" var="group">
							<c:set var="_taxindex" value="${_taxindex+1}"/>
							<td class="formlabel"><label id="taxnamelabel_${group.item_group_id}">${group.item_group_name}</label>:</td>
							<td class="forminfo"><label id="taxamtlabel_${group.item_group_id}">0</label></td>
							<c:if test="${_taxindex%4 == 0}">
								</tr>
								<tr>
							</c:if>
						</c:forEach>
					  	<td class="formlabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.taxamount"/>:</td>
						<td class="forminfo">
							<label id="lblTotalVat">${defaultValue}</label>
						</td>
						
					  </tr>
					  <tr>
						<td class="formlabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.inv.discounttype"/>:</td>
						<td>
							<insta:selectoptions name="inv_discount_type" class="dropdown" style="width: 7em"
							opvalues="P,A" optexts="${amountstatus}" value="${empty grn ? 'P' : inv.map.discount_type}"
							onchange="onChangeInvDiscType();"/>
						</td>
		
						<td class="formlabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.inv.discountvalue"/>:</td>
						<td>
							<input type="text" name="inv_discount_val"
							value="${empty grn ? defaultValue : inv.map.discount_type eq 'P' ? inv.map.discount_per : inv.map.discount}"
							onkeypress="return enterNumAndDot(event);" onchange="onChangeInvDiscVal()" />
						</td>
						<td class="formlabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.transportcharges"/>:</td>
						<td>
							<input type="text" name="transportation_charges" value="${not empty inv.map.transportation_charges ? inv.map.transportation_charges : defaultValue }" onkeypress="return enterNumAndDot(event);" onchange="onChangeOtherCharges();return makeingDec(this.value,this)" maxlength="13"/>
						</td>
					  <td class="formlabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.inv.discountamount"/>:</td>
						<td class="forminfo">
							<label id="lblInvDisc">${defaultValue}</label>
							<input type="hidden" name="inv_discount" value="${inv.map.discount}"/>
							<input type="hidden" name="inv_discount_per" value="${inv.map.discount_per}"/>
						</td>
		
					</tr>
					<tr style="display:none;" id ="tcsRow">
						<td class="formlabel"><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.tcs.type"/>:</td>
						<td>
							<insta:selectoptions name="tcs_type" opvalues="P,A" optexts="${amountstatus}" style="width: 7em"
								value="${empty grn ? 'P' : inv.map.tcs_type}" onchange="onChangeTcsType()"/>
						</td>

						<td class="formlabel"><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.tcs.value"/>:</td>
						<td>
							<input type="text" name="tcs_value"
								value="${empty grn ? defaultValue : inv.map.tcs_type eq 'P' ? inv.map.tcs_per : inv.map.tcs_amount}"
								onkeypress="return enterNumAndDot(event);" onChange="return onChangeTcsVal()"/>
							<input type="hidden" name="inv_tcs_per" value="${inv.map.tcs_per}"/>
							<input type="hidden" name="inv_tcs_amount" value="${inv.map.tcs_amount}"/>
							<input type="hidden" name="po_inv_amt" value=""/>
						</td>
						<td class="formlabel"><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.tcs.amount"/>:</td>
						<td class="forminfo"><label id="lblGrnTcsAmount">${inv.map.tcs_amount}</label></td>
					</tr>
		
					<tr>
						<td class="formlabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.othercharges"/>:</td>
						<td>
							<input type="text" name="other_charges" value="${not empty inv.map.other_charges ? inv.map.other_charges : defaultValue }" onkeypress="return enterNumAndDot(event);" onchange="onChangeOtherCharges();return makeingDec(this.value,this)"/>
						</td>
		
						<td class="formlabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.roundoff"/>:</td>
						<td>
							<input type="text" name="round_off" value="${not empty inv.map.round_off ? inv.map.round_off : defaultValue }" onkeypress="return enterNumAndDotAndMinus(event);" onchange="onChangeRoundOff();return makeingDec(this.value,this)"/>
						</td>
		
						<td class="formlabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.debitamount"/>:</td>
						<td>
							<input type="text" name="debit_amt"
							value="${not empty inv.map.debit_amt ? inv.map.debit_amt : defaultValue}"
							onkeypress="return enterNumAndDot(event);"
							onchange="return makeingDec(this.value,this)"/>
						</td>
		
					  <td class="formlabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.invoicetotal"/>:</td>
					  <td class="forminfo"><label id="lblGrandTotal">${defaultValue}</label></td>
					</tr>
				</table>
			</fieldset>
		</c:otherwise>
	</c:choose>

	<dl class="accordion" style="margin-bottom: 10px;">
		<dt>
			<span><insta:ltext key="storeprocurement.stockentry.invoicedetails.stockentrypreferences"/></span>
			<div class="clrboth"></div>
		</dt>
		<dd >
			<div class="bd">
				<table class="formtable" width="100%">
	  			   <insta:storepreferences addBox="false" showtype="SE" vatPref="${prefVat}"/>
	  			</table>
	  	    </div>
	    </dd>
	</dl>

	<div class="screenActions" style="float: left;">
		<c:choose><c:when test="${allowSave}">
			<button type="button" name="saveStk" accesskey="S" style="display: inline;"
			class="button" onclick="return savestock();"><label> <u><b><insta:ltext key="storeprocurement.stockentry.invoicedetails.s"/></b></u><insta:ltext key="storeprocurement.stockentry.invoicedetails.ave"/></label></button>
			<c:if test="${allowFinalizeCloseGrn != 'N'}">
			<button type="button" name="saveAndFinalizeStk" accesskey="F" style="display: inline;"
				class="button" onclick="return saveAndFinalize();"><label> <insta:ltext key="storeprocurement.stockentry.invoicedetails.save"/> &amp; <u><b><insta:ltext key="storeprocurement.stockentry.invoicedetails.f"/></u></b><insta:ltext key="storeprocurement.stockentry.invoicedetails.inalize"/></label></button>
			</c:if>
			<button type="button" name="saveAndPrintStk" accesskey="R" style="display: inline;"
				class="button" onclick="return saveAndPrint();"><label> <insta:ltext key="storeprocurement.stockentry.invoicedetails.save"/> &amp; <insta:ltext key="storeprocurement.stockentry.invoicedetails.p"/><u><b><insta:ltext key="storeprocurement.stockentry.invoicedetails.r"/></u></b><insta:ltext key="storeprocurement.stockentry.invoicedetails.int"/></label></button>	
		</c:when><c:otherwise>
			<c:if test="${urlRightsMap.reopen_grn == 'A' || roleId eq 1 || roleId eq 2}">
			<button type="button" name="saveStk" accesskey="R" style="display: inline;"
				class="button" onclick="return reopen();"><label><u><b><insta:ltext key="storeprocurement.stockentry.invoicedetails.capsr"/></u></b><insta:ltext key="storeprocurement.stockentry.invoicedetails.eopengrn"/></label></button>
			</c:if>
			<c:if test="${inv.map.status == 'F' && allowFinalizeCloseGrn != 'N'}">
				<button type="button" name="closeStk" accesskey="C" style="display: inline;"
					class="button" onclick="return closeGRN();"><label><u><b><insta:ltext key="storeprocurement.stockentry.invoicedetails.c"/></u></b><insta:ltext key="storeprocurement.stockentry.invoicedetails.losegrn"/></label></button>
			</c:if>
		</c:otherwise></c:choose>
		<input type="hidden" name="saveAction" value="save"/>
		<a href="" target="_blank" id="polink"></a>
	</div>
	<div class="screenActions" style="float: right;">
		<select name="grn_print_template" id="grn_print_template" class="dropdown" onchange="onChangeGrnTemplate(this.value)">
			<option value="BUILTIN_HTML" >Built-in Default HTML template</option>
			<c:forEach var="grn_print_template" items="${grnPrintTemplates}" varStatus="st">
				<option value="${grn_print_template.template_name}">${grn_print_template.template_name}</option>
			</c:forEach>
		</select>
		
		<insta:selectdb name="printType" table="printer_definition"
				valuecol="printer_id"  displaycol="printer_definition_name"
				value="${printPref.map.printer_id}"/>
		<c:if test="${not allowSave}">
			<button type="button" id="print" name="print" style="display: inline;"
				class="button" onclick="return grnPrint();"><label><insta:ltext key="storeprocurement.stockentry.invoicedetails.print"/></label></button>
		</c:if>
	</div>
</form>

<form name="detailForm" onsubmit="javascript:void(0); return false;" id="dlgForm">
	<div id="addEditDialog" style="visibility:hidden; display:none">
		<div class="bd" style="text-align:center;">
			<fieldset class="fieldSetBorder" style="width: 98%;text-align:center;">
				<legend class="fieldSetLabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.itemdetails"/></legend>
				<table  class="formtable" cellpadding="0" cellspacing="0" border="0" width="100%">
					<tr>
						<c:choose>
							<c:when test="${prefbarcode eq 'Y'}">
								<td class="formLabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.itembarcode"/>:</td>
								<td>
									<input type="text" name="item_barcode_id" onchange="getItemBarCodeDetails(this.value);" >
								</td>
								<td class="formLabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.item"/>:</td>
								<td colspan="2">
									<div id="item_wrapper" style="width: 20em; padding-bottom:2em; ">
										<input type="text" name="medicine_name" id="medicine_name" style="width: 20em"	maxlength="100"/>
										<div id="item_dropdown" class="scrolForContainer"></div>
									</div>
								</td>
								<c:if test="${!doAllowStatus}">
									<td>
										<a href="#" onclick="onClickPurchaseDetails(); return false;" id="itemPur"><insta:ltext key="storeprocurement.stockentry.invoicedetails.purchasedetails"/></a>
									</td>
								</c:if>
							</c:when>
							<c:otherwise>
								<td class="formLabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.item"/>:</td>
								<td colspan="3">
									<div id="item_wrapper" style="width: ${iwidth}; padding-bottom:2em; ">
										<input type="text" name="medicine_name" style="width: ${iwidth}"
										maxlength="100"/>
										<div id="item_dropdown" class="scrolForContainer"></div>
									</div>
								</td>
								<c:if test="${!doAllowStatus}">
									<td colspan="2">
										<a href="#" onclick="onClickPurchaseDetails(); return false;" id="itemPur"><insta:ltext key="storeprocurement.stockentry.invoicedetails.purchasedetails"/></a>
									</td>
								</c:if>
							</c:otherwise>
						</c:choose>
					</tr>

						<tr>
							<td class="formLabel"> <insta:ltext key="storeprocurement.stockentry.invoicedetails.batch.serialno"/>.:
								<input type="hidden" name="medicine_id" value="" >
						    	<input type="hidden" name="tax_rate" value="0"/>
								<input type="hidden" name="tax" value="0"/>
								<input type="hidden" name="bonus_tax" value="0"/>
								<input type="hidden" name="store_id_hid" id="store_id_hid" value=""/>
 								<input type="hidden" name="supplier_code_hid" id="supplier_code_hid" value=""/>
								<input type="hidden" name="store_package_uom" value=""/>
						    <c:if test="${prefCed ne 'Y'}">
						    	<input type="hidden" name="item_ced_per" value="0"/>
								<input type="hidden" name="item_ced" value="0"/>
						    </c:if>
							</td>
							<td class="formContent" valign="top">
								<div id="identifier_wrapper" style="width: 12em; padding-bottom:0.2em; ">
									<input type="text" name="batch_no" style="width: 12em" maxlength="50"  />
									<div id="identifier_dropdown" class="scrolForContainer"></div>
								</div>
						 		<input type="hidden" name="currentIdentifier" id="currentIdentifier" value="">
						 		<input type="hidden" name="item_batch_id"/>
							</td>

							<td class="formLabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.expiry.mm_yy.in.brackets"/>:</td>
							<td class="formContent">
						 		<input type="text" name="exp_dt_mon" class="size2" maxlength="2" onchange="chkMon();"
								onkeypress="return enterNumOnlyzeroToNine(event);"  /> -
						 		<input type="text" name="exp_dt_year" class="size2" maxlength="2"
								onkeypress="return enterNumOnlyzeroToNine(event);" onchange="chkYear();" />
							</td>
							<td class="formLabel"> &nbsp;</td>
							<td class="formContent">&nbsp;</td>
						</tr>
						<tr>
							<c:choose>
								<c:when test="${doAllowStatus}">
									<td class="formLabel" style="display:none;"><insta:ltext key="storeprocurement.stockentry.invoicedetails.mrp"/>:</td>
									<td class="formContent" style="display:none;">
										<input type="text" name="mrp_display" onkeypress="return enterNumAndDot(event);"
										onchange="onChangeMrp();" maxlength="13" />
									</td>
		
									<td class="formLabel" style="display:none;"><insta:ltext key="storeprocurement.stockentry.invoicedetails.adj.mrp"/>:</td>
								 	<td style="display:none;">
								 		<input type="text" name="adj_mrp_display" maxlength="13" disabled="disabled" />
								 		<input type="hidden" name="adj_mrp"/>
								 	</td>
		
									<td class="formLabel" style="display:none;"><insta:ltext key="storeprocurement.stockentry.invoicedetails.rate"/>:</td>
									<td class="formContent" style="display:none;">
										<input type="text" name="cost_price_display" maxlength="13"
										onkeypress="return enterNumAndDot(event);"
										onchange="onChangeCostPrice();" />
										<input type="hidden" name="cost_price" />
										<input type="hidden" name="supplier_rate_validation" value="false" />
										<input type="hidden" name="max_cost_price" value="" />
										<input type="hidden" name="item_max_cost_price" value="" />
									</td>
								</c:when>
								<c:otherwise>
									<td class="formLabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.mrp"/>:</td>
									<td class="formContent">
										<input type="text" name="mrp_display" onkeypress="return enterNumAndDot(event);"
										onchange="onChangeMrp();" maxlength="13" />
										<input type="hidden" name="po_mrp"/>
									</td>
		
									<td class="formLabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.adj.mrp"/>:</td>
								 	<td>
								 		<input type="text" name="adj_mrp_display" maxlength="13" disabled="disabled" />
								 		<input type="hidden" name="adj_mrp"/>
								 	</td>
									
		
									<td class="formLabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.rate"/>:</td>
									<td class="formContent">
										<input type="text" name="cost_price_display" maxlength="13"
										onkeypress="return enterNumAndDot(event);"
										onchange="onChangeCostPrice();" />
										<input type="hidden" name="cost_price" />
										<input type="hidden" name="supplier_rate_validation" value="false" />
										<input type="hidden" name="max_cost_price" value="" />
										<input type="hidden" name="item_max_cost_price" value="" />
									</td>
								</c:otherwise>
							</c:choose>
						</tr>

						<tr>
							<td class="formLabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.quantity"/>:</td>
							<td>
								<input type="text" name="billed_qty_display" maxlength="8"
								onkeypress="return onKeyPressAddQty(event);"
								onchange="onChangeBilledQty();" />
								<input type="hidden" name="billed_qty"/>
								<input type="hidden" name="po_billed_qty"/>
								<input type="hidden" name="pomed"/>
							</td>

							<td class="formLabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.bonus"/>:</td>
							<td><input type="text" name="bonus_qty_display"
								maxlength="8" value="0" onChange="onChangeBonusQty();" />
								<input type="hidden" name="bonus_qty"/>
								<input type="hidden" name="po_bonus_qty"/>
							</td>

							<td colspan="2" class="forminfo"><label id="UOMDesc"></label></td>

						</tr>
						<c:choose>
							<c:when test="${prefCed eq 'Y'}">
								<c:choose>
									<c:when test="${doAllowStatus}">
										<tr id="ceds" style="display:none;">
											<td class="formLabel" title="${centralexcerciseduty}"><insta:ltext key="storeprocurement.stockentry.invoicedetails.ced.percentage.in.brackets"/>:</td>
											<td>
												<input type="text" name="item_ced_per" onkeypress="return enterNumAndDot(event);"
												onchange="onChangeCedPer();" value="0" />
											</td>
		
											<td class="formLabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.ced"/>:</td>
											<td>
												<input type="text" name="item_ced" onkeypress="return enterNumAndDot(event);"
												onchange="onChangeCedAmt();" value="0"  />
											</td>
		
											<td class="formLabel">
											</td>
											<td>
											</td>
										</tr>
									</c:when>
									<c:otherwise>
										<tr id="ceds" style="display:none;">
											<td class="formLabel" title="${centralexcerciseduty}"><insta:ltext key="storeprocurement.stockentry.invoicedetails.ced.percentage.in.brackets"/>:</td>
											<td>
												<input type="text" name="item_ced_per" onkeypress="return enterNumAndDot(event);"
												onchange="onChangeCedPer();" value="0" />
											</td>
		
											<td class="formLabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.ced"/>:</td>
											<td>
												<input type="text" name="item_ced" onkeypress="return enterNumAndDot(event);"
												onchange="onChangeCedAmt();" value="0"  />
											</td>
		
											<td class="formLabel">
											</td>
											<td>
											</td>
										</tr>
									</c:otherwise>
								</c:choose>
								
							</c:when>
						</c:choose>
						<c:choose>
							<c:when test="${doAllowStatus}">
								<tr style="display:none;">
									<td class="formLabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.discount.percentage.in.brackets"/>:</td>
									<td>
										<input type="text" name="discount_per" class="num"  onkeypress="return enterNumAndDot(event);"
										onChange="onChangeDiscountPer();" value="0" />
									</td>
		
									<td class="formLabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.discount"/>:</td>
									<td>
										<input type="text" name="discount" onkeypress="return enterNumAndDot(event);"
										onchange="onChangeDiscountAmt();" value="0" />
									</td>
		
									<td class="formLabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.amt"/>:</td>
									<td>
										<input type="text" name="med_total" disabled="disabled"  />
									</td>
								</tr>
							</c:when>
							<c:otherwise>
								<tr>
									<td class="formLabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.discount.percentage.in.brackets"/>:</td>
									<td>
										<input type="text" name="discount_per" class="num"  onkeypress="return enterNumAndDot(event);"
										onChange="onChangeDiscountPer();" value="0" />
									</td>
		
									<td class="formLabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.discount"/>:</td>
									<td>
										<input type="text" name="discount" onkeypress="return enterNumAndDot(event);"
										onchange="onChangeDiscountAmt();" value="0" />
									</td>
		
									<td class="formLabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.amt"/>:</td>
									<td>
										<input type="text" name="med_total" disabled="disabled"  />
									</td>
								</tr>
							</c:otherwise>
						</c:choose>
						
						<c:choose>
							<c:when test="${doAllowStatus}">
								<tr style="display:none;">
									<td class="formLabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.scheme.discount.percentage.in.brackets"/>:</td>
									<td>
										<input type="text" name="scheme_discount_per" class="num"  onkeypress="return enterNumAndDot(event);"
										onChange="onChangeSchemeDiscountPer();" value="0" />
									</td>
		
		
									<td class="formLabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.scheme.discount"/>:</td>
									<td>
										<input type="text" name="scheme_discount" onkeypress="return enterNumAndDot(event);"
										onchange="onChangeSchemeDiscountAmt();" value="0" />
									</td>
								</tr>
							</c:when>
							<c:otherwise>
								<tr>
									<td class="formLabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.scheme.discount.percentage.in.brackets"/>:</td>
									<td>
										<input type="text" name="scheme_discount_per" class="num"  onkeypress="return enterNumAndDot(event);"
										onChange="onChangeSchemeDiscountPer();" value="0" />
									</td>
		
		
									<td class="formLabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.scheme.discount"/>:</td>
									<td>
										<input type="text" name="scheme_discount" onkeypress="return enterNumAndDot(event);"
										onchange="onChangeSchemeDiscountAmt();" value="0" />
									</td>
								</tr>
							</c:otherwise>
						</c:choose>
						
						<tr>
							<td class="formLabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.pkgsize"/>:</td>
							<td>
								<label id="lblGrnPkgSize"></label>
								<input type="hidden" name="grn_pkg_size"/>
								<input type="hidden"   name="code_type" value="" />
								<input type="hidden"   name="item_code" value="" />
							</td>
						</tr>
						
						<c:choose>
							<c:when test="${doAllowStatus}">
								<tr style="display:none;">
									<td class="formLabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.cstrate.percentage.in.brackets"/>:</td>
									<td>
										<input type="text" name="cst_rate"
										onkeypress="return enterNumAndDot(event);" onchange="makeingDec(this.value,this);onChangeCST()">
									</td>
								</tr>
							</c:when>
							<c:otherwise>
								<tr style="display:none;">
									<td class="formLabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.cstrate.percentage.in.brackets"/>:</td>
									<td>
										<input type="text" name="cst_rate"
										onkeypress="return enterNumAndDot(event);" onchange="makeingDec(this.value,this);onChangeCST()">
									</td>
								</tr>
							</c:otherwise>
						</c:choose>
					</table>
					<c:set var="_taxindex" value="0"/>
					<fieldset class="fieldSetBorder">
						<legend class="fieldSetLabel" style="text-align:left;">Tax Details</legend>
						<table class="formtable">
							<tr>
								<td class="formLabel"><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.taxtype"/>:</td>
								<td class="formInfo">
									<select name="tax_type" class="dropdown" onChange="onChangeTaxType(this);">
										<option value="MB"><insta:ltext key="storeprocurement.stockentry.invoicedetails.mrpbased.with.bonus"/></option>
										<option value="M"><insta:ltext key="storeprocurement.stockentry.invoicedetails.mrpbased.without.bonus"/></option>
										<option value="CB"><insta:ltext key="storeprocurement.stockentry.invoicedetails.cpbased.with.bonus"/></option>
										<option value="C"><insta:ltext key="storeprocurement.stockentry.invoicedetails.cpbased.without.bonus"/></option>
									</select>
								</td>
								<td class="formLabel"><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.taxrate"/>:</td>
								<td class="formInfo">
									<label id="lblvat_rate">0</label>
								</td>
								<td class="formLabel"><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.vatamt"/>:</td>
								<td class="formInfo">
									<label id="lblvat_amt">0</label>
								</td>
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
					
			</fieldset>

			<table width="100%"><tr><td>
			<div style="float: left">
				<button type="button" id="Add" name="Add" accesskey="A"  style="display: inline;" class="button" onclick="onDialogSave();" ><label> <u><b><insta:ltext key="storeprocurement.stockentry.invoicedetails.a"/></b></u><insta:ltext key="storeprocurement.stockentry.invoicedetails.dd"/></label></button>
				<input type="button" name="CancelBut" id="CancelBut" value="Cancel" onclick="handleDetailDialogCancel();" />
				<button type="button" id="prevDialog" name="prevDialog" accesskey="P"  style="display: inline;" class="button" onclick="onNextPrev(this);"  disabled="disabled"><label> << <u><b><insta:ltext key="storeprocurement.stockentry.invoicedetails.p"/></b></u><insta:ltext key="storeprocurement.stockentry.invoicedetails.revious"/></label></button>
				<button type="button" id="nextDialog" name="nextDialog" accesskey="N"  style="display: inline;" class="button" onclick="onNextPrev(this);"  disabled="disabled"><label> <u><b><insta:ltext key="storeprocurement.stockentry.invoicedetails.n"/></b></u><insta:ltext key="storeprocurement.stockentry.invoicedetails.ext"/> >></label></button>
			</div>

			<c:if test="${urlRightsMap['mas_medicines'] == 'A' && (not strictPO)}">
				<div style="float: right">
					<button type="button" onclick="openMasterScreen();"><label><insta:ltext key="storeprocurement.stockentry.invoicedetails.newitem"/></label></button>
				</div>
			</c:if>
</td></tr></table>
		</div>
	</div>
</form>

<jsp:include page="/pages/stores/PurchaseDetails.jsp"/>

<%-- End of dialog forms --%>

</body>
</html>

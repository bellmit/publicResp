<%@page import="com.bob.hms.common.RequestContext"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<%@ page
	import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<%@ page import="com.insta.hms.stores.StoresDBTablesUtil"%>
<html>
<head>
	<title><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.supplierreturns.debit.in.brackets.instahms"/></title>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<c:set var="prefQtyType"
	value="<%= GenericPreferencesDAO.getGenericPreferences().getQtyDefaultToIssueUnit() %>" />
<c:set var="prefDecimalQty"
	value="<%= GenericPreferencesDAO.getGenericPreferences().getAllowdecimalsforqty() %>" />
<c:set var="prefretAgtSupp"
	value="<%= GenericPreferencesDAO.getGenericPreferences().getReturnAgainstSpecificSupplier() %>" />
<c:set var="prefCed"
	value="<%= GenericPreferencesDAO.getGenericPreferences().getShowCED() %>" />
<c:set var="prefVat"
	value="<%= GenericPreferencesDAO.getGenericPreferences().getShowVAT() %>" />
<c:set var="defaultValue"
	value="${(prefDecimalDigits == 3) ? '0.000' : '0.00'}" />
<c:set var="prefbarcode"
	value="<%= GenericPreferencesDAO.getGenericPreferences().getBarcodeForItem() %>" />
<c:set var="max_centers_inc_default" value='<%=GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default")%>'/>
<c:set var="taxLabel" value="${genPrefs.procurement_tax_label}" scope="request"/>
<c:set var="isReturnAgainstGrn" value='<%=GenericPreferencesDAO.getAllPrefs().get("is_return_against_grnno")%>' />


<insta:link type="js" file="ajax.js" />
<insta:link type="script" file="hmsvalidation.js" />
<insta:link type="js" file="stores/supplierdebits.js" />
<insta:link type="js" file="stores/storeshelper.js"/>
<c:set var="typeOfSale" value="" />
<insta:js-bundle prefix="stores.supplier"/>
<script>
	 var qtyDecimal = '${prefDecimalQty}';
	 var retAgtSupp = '${prefretAgtSupp}';
	 var selsupp = <insta:jsString value='${debit.map.supplier_name}'/>;
	 var popurl = '${pageContext.request.contextPath}';
	 var jAllSuppliers = <%= request.getAttribute("supp") %>;
	 var fromedit = '${fromedit}';
	 var groupMedDetails = ${groupMedDetails};
	 var groupDeptId  = '${ifn:cleanJavaScript(groupDeptId)}';
	 var debitReturnType = '${debit.map.return_type}';
	 var gRoleId ='${roleId}';
	 var deptId = '${ifn:cleanJavaScript(dept_id)}';
	 var centerId = <%= RequestContext.getCenterId() %>;
	 var jAllSuppliers1 = <%= StoresDBTablesUtil.getTableDataInJSON(StoresDBTablesUtil.GETSUPPLIERS)%>;
	 <c:if test="${max_centers_inc_default>1}">
	  var jCenterSuppliers = ${listAllcentersforDebit};
	</c:if>
	 var prefCed = '${prefCed}' ;
	 var prefVAT = '${prefVat}';
	 var prefBarCode = '${prefbarcode}';
	 var sesHospitalId = '${ifn:cleanJavaScript(sesHospitalId)}';
	 var taxLabel = '${taxLabel}';
	 var defaultDiscType = '${debit.map.discount_type}';
	 var defaultStatus = '${debit.map.status}';
	 var multiStoreAccess = '${multiStoreAccess}';
	 var paramStore = '${param.store}';
	 var phStore = '${store}';
	 var isReturnAgainstGrn = <%=GenericPreferencesDAO.getAllPrefs().get("is_return_against_grnno")%>;
	 var subgroupNamesList = JSON.parse('${ifn:cleanJavaScript(subGroupListJSON)}'); 
	 var groupListJSON = JSON.parse('${ifn:cleanJavaScript(groupListJSON)}');
</script>

<style>
	.scrolForContainer .yui-ac-content{
	 max-height:18em;overflow:auto;overflow-x:auto; /* scrolling */
    _height:18em; max-width:35em; width:35em;/* ie6 */
    }
</style>

</head>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="debitList" value="${list}" />
<c:set var="hasResults" value="${not empty debitList}" />
<c:choose>
	<c:when test="${not empty groupStoreId}">
		<c:set var="defStoreVal" value="${groupStoreId}" />
	</c:when>
	<c:otherwise>
		<c:set var="defStoreVal" value="${dept_id}" />
	</c:otherwise>
</c:choose>
<c:set var="apply">
<insta:ltext key="storeprocurement.supplierreturns.replacementlist.list.apply"/>
</c:set>
<body onload="init();" class="yui-skin-sam">
<c:set var="taxlabel">
	<insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.${taxLabel}"/>
</c:set>
<c:set var="csttaxlabel">
	<insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.cst.${taxLabel}"/>
</c:set>
<div id="storecheck" style="display: block;">
<form method="POST" action="StoresSupplierReturns.do"
	name="supplierdebitform"><input type="hidden" name="debitNo"
	id="debitNo" value="${ifn:cleanHtmlAttribute(debitNo)}" /> <input type="hidden" id="dialogId"
	value="" />
<h1><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.supplierreturn.debit.in.brackets"/></h1>
<insta:feedback-panel/>
<c:choose>
	<c:when test="${not empty debitNo}">
		<div style="padding-bottom:1em;"><b><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.note"/> :</b> <insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.qtyselectedin"/>
		${qtyselection eq 'P' ? 'Package UOM' : 'Unit UOM' }</div>
		<input type="hidden" name="qty_unit" value="${ifn:cleanHtmlAttribute(qtyselection)}">
	</c:when>
	<c:otherwise>
		<div style="padding-bottom:1em;"
			title="If you change selection ,existing row(s) in the Grid are deleted">
		<insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.qtyin"/>: <input type="radio" name="qty_unit" id="issue_units"
			onclick="clearGrid()"
			<c:if test="${prefQtyType eq 'Y'}" ><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.checked"/></c:if> value="I"><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.unit"/>
		<insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.uom"/> <input type="radio" name="qty_unit" id="pkg_units"
			onclick="clearGrid()"
			<c:if test="${prefQtyType eq 'N'}" ><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.checked"/> </c:if> value="P"><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.package"/>
		<insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.uom"/></div>
	</c:otherwise>
</c:choose>

<c:if test="${fromedit 	ne 'Y'}">
	<fieldset class="fieldSetBorder">
			<table style="margin-left: 4px;" width="99%">
			<tr>
				<td valign="top">		
					<table border="0">
						<tr>
							<td valign="top" style="padding-top: 3px" id="patientType"><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.returnagainst"/></td>
							<td>
								<table cellspacing="0" cellpadding="0">
									<c:if test="${not isReturnAgainstGrn}">
									<tr>
										<td>
											<input type="radio" name="returnAgainst" accesskey="W" id="return_without_grn"
												value="withoutGrnReturn" checked="checked" onclick="changeReturnType();">
											<label for="returnType_without_grn_no"><b><u><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.w"/></u></b><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.ithoutgrnno"/></label>
										</td>
										<td></td>
									</tr>
									</c:if>
									<tr>
										<td>
											<input type="radio" name="returnAgainst" accesskey="G" id="return_against_grn"
												value="grnReturn" onclick="changeReturnType();">
												<label for="returnType_grn_no"><b><u><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.g"/></u></b><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.rnno"/></label>
										</td>
										<td>
											<input type="text" name="grn_no" size="8" onchange="getGrnDetails()"
												style="width: 16em"/>
										</td>
										<td><input type="hidden" name="grnReturnH" id="grnReturnH" value =""></td>
									</tr>
								</table>
							</td>	
						</tr>
					</table>
				</td>
			</tr>				
					
			</table>	
	</fieldset>	
</c:if>

<fieldset class="fieldSetBorder"><legend
	class="fieldSetLabel"><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.debitnotedetails"/></legend>
<table class="formtable" cellpadding="0" cellspacing="0" width="100%"
	border="0">
	<tr>
		<c:if test="${fromedit eq 'Y'}">
			<td class="formlabel"><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.debitnote"/>:</td>
			<td style="width: 30em;">${ifn:cleanHtml(debitNo)}</h1></td>
		</c:if>
		<td class="formlabel"><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.supplier"/>:</td>
		<td valign="top">
		<div id="supplier_wrapper" style="width: 16em; padding-bottom:0.2em">
		<input type="text" name="supplierName" id="supplierName"
			style="width: 16em;" value="${debit.map.supplier_name}"
			<c:if test="${fromedit eq 'Y' }"><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.readonly"/></c:if>>
		<div id="suppliername_dropdown" class="scrolForContainer"></div>
		</div>
		<input type = "hidden" name ="supplier_name" value="${debit.map.supplier_name}"/>
		<input type = "hidden" name ="supplier_code" value="${debit.map.supplier_code}"/>
		</td>
		<td class="formlabel"><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.store"/>:</td>
		<c:choose>
			<c:when
				test="${(multiStoreAccess eq 'A' || roleId eq 1 || roleId eq 2 )}">
				<td><insta:userstores username="${userid}" elename="store"
					id="store" onchange="changeStore();" val="${param.store}" /></td>
			</c:when>
			<c:otherwise>
				<td><b><insta:getStoreName store_id="${param.store eq null? store : param.store}" /></b>
				<input type="hidden" name="store" id="store"
					value="${param.store eq null? store : param.store}" /></td>
			</c:otherwise>
		</c:choose>

	</tr>
	<tr>
		<td class="formlabel"><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.supplieraddress"/>:</td>
		<td class="forminfo" colspan="5"><label id="suppAddId">${supplier_address}</label></td>
	</tr>
	<tr>
		<td class="formlabel"><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.returntype"/>:</td>
		<td style="width: 22em;"><insta:selectoptions name="returnType"
			value="" opvalues="D,E,O" class="dropdown"
			optexts="Damage,Expiry,Others" onchange="chgType(this.value)"
			style="width:8em;" /> <input type="text" name="othersreason"
			id="othersreason"
			value=<c:if test="${debit.map.return_type eq 'O'}" >${debit.map.other_reason }</c:if>>
		</td>
		<td class="formlabel"><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.remarks"/>:</td>
		<td><input type="text" name="remarks" id="remarks"
			maxlength="99" value="${debit.map.remarks }" size="25"></td>
		<td class="formlabel"><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.gatepass"/> :</td>
		<td><input type="checkbox" name="gatepass" id="gatepass"></td>

	</tr>
</table>
</fieldset>
	<fieldset class="fieldSetBorder" style="display: none">
	<legend class="fieldSetLabel"><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.vatorcst.${taxLabel}"/></legend>
	<table class="formtable" cellpadding="0" cellspacing="0" width="50%" border="0">
		<tr>
			<td align="center" style="width: 20em" id="tax_td" ><input type="radio"
				name="vatORcst" id="vat" value="${taxlabel}" checked
				onclick="enableTaxDefaults();" />${taxlabel} <input type="radio"
				name="vatORcst" id="cst" value="${csttaxlabel }" onclick="enableTaxDefaults();" />${csttaxlabel }
			</td>
			<td id="cstRateId"><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.cstrate.${taxLabel}"/>:<input class="number" type="text"
				name="cstrate" id="cstrate" readOnly
				onkeypress="return enterNumAndDot(event);"
				onchange="updateTaxRates();" /></td>
			<td style="display:none" id="taxNAlbl"><label>Not Applicable</label></td>
			
		</tr>
	</table>
</fieldset>
<fieldset class="fieldSetBorder" >
		<legend class="fieldSetLabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.consignmentdetails"/></legend>
			<table class="formtable" cellpadding="0" cellspacing="0" border="0" width="100%">
				<tr>
				<td class="formlabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.companyname"/>:</td>
					<td><input type="text" name="company_name" id="company_name" value="${debit.map.company_name}" maxlength="100"/></td>
				
				<td class="formlabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.meansoftransport"/>:</td>
					<td><input type="text" name="means_of_transport" id="means_of_transport" value="${debit.map.means_of_transport}" maxlength="50"/></td>
				
				<td class="formlabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.consignmentno"/>:</td>
					<td><input type="text" name="consignment_no" id="consignment_no" value="${debit.map.consignment_no}" maxlength="50"/></td>
			</tr>
			<tr>
				<td class="formlabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.consignmentdate"/>:</td>
				<td>
					<insta:datewidget name="consignment_date"  value="${not empty debit.map.consignment_date_fmt ? debit.map.consignment_date_fmt : ''}" btnPos="left"/>
				</td>
			</tr>
		</table>
	</fieldset>
<fieldset class="fieldSetBorder"><legend
	class="fieldSetLabel"><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.itemlist"/></legend>
	<div class="resultList" style="margin: 10px 0px 5px 0px;">
<table class="detailList dialog_displayColumns" width="100%" cellspacing="0" cellpadding="0" id="medList" border="0">
	<tr>

		<th><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.item"/></th>
		<th><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.code"/></th>
		<th><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.pkg"/></th>
		<th><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.b.or.si.no"/></th>
		<th><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.exp"/></th>
		<th style="text-align:right" title="MRP"><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.mrp"/></th>
		<th style="text-align:right" title="Qty. to be returned"><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.retbilledqty"/></th>
		<th style="text-align:right" title="Qty. to be returned"><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.retbonusqty"/></th>
		<th style="text-align:left" title="Package/Unit UOM"><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.uom"/></th>
		<th style="text-align:right" title="Rate"><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.rate"/></th>
		<th style="text-align:right" title="Discount"><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.disc"/></th>
		<th style="text-align:right" title="Discount"><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.schemedisc"/></th>
		<th style="text-align:right" title="User-entered Revised Rate"><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.revrate"/></th>
		<th style="text-align:right" title="Revised Discount)"><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.revdisc"/></th>
		<th style="text-align:right" title="Discount"><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.revschemedisc"/></th>
		<th style="text-align:right" id="taxId"><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.revvat"/> (%)</th>
		<th style="text-align:right" id="taxIdAmt"><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.revvatamt"/></th>
		<th style="text-align:right" title="Calculated Amt"><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.amt"/></th>
		<th style="text-align:right" title="Calculated Revised Amt"><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.rev.amt"/></th>
		<th></th>
		<th></th>
	</tr>
	<c:set var="i" value="1" />
	<c:set var="totalDiscount" value="0" />
	<c:set var="totalRevDiscount" value="0" />
	<c:set var="totalAmount" value="0" />
	<c:set var="totalVat" value="0" />

	<c:choose>
		<c:when test="${not empty debitList }">
			<c:forEach items="${debitList}" var="med" varStatus="status">
				<c:set var="flagColor"
					value='${fn:startsWith(med.map.tax_type,"C") ? "yellow" : ""}' />
				<tr>
					<td class="forminfo" style="width:10em;padding-left: 0.5em;"
						valign="middle"><c:choose>
						<c:when
							test="${fn:startsWith(med.map.tax_type,'C') }">
							<img class="flag" src="${cpath}/images/${flagColor}_flag.gif" />
							<label id="medName${i}">${med.map.medicine_name }</label>
						</c:when>
						<c:otherwise>
							<label id="medName${i}">${med.map.medicine_name }</label>
						</c:otherwise>
					</c:choose> <input type="hidden" name="hmedId" id='hmedId${i}'
						value="${med.map.medicine_id }"> <input type="hidden"
						name="hmedName" id='hmedName${i}'
						value="${med.map.medicine_name }"> <input type="hidden"
						name="hadjmrp" id='hadjmrp${i}'
						value="${qtyselection eq 'P' ? med.map.adj_mrp : ifn:afmt(med.map.adj_mrp/med.map.issue_base_unit)}">
					<input type="hidden" name="htaxrate" id='htaxrate${i}'
						value="${med.map.tax_rate }"> <input type="hidden" name="hrevtaxrate"
					id='hrevtaxrate${i}' value="${med.map.tax_rate }" /><input type="hidden"
						name="hcedamt" id='hcedamt${i}' value="${med.map.item_ced_amt }">
					<input type="hidden" name="htaxtype" id='htaxtype${i}'
						value="${med.map.tax_type }"> <input type="hidden"
						name="hitemidentification" id='hitemidentification${i}'
						value="${med.map.identification }"> <input type="hidden"
						name="hitembarcode" id="hitembarcode${i}"
						value="${med.map.item_barcode_id }"></td>
						<td ><input type="hidden"
							name="hitemcode" id='hitemcode${i}'
						value="${med.map.item_code }"><label
						id="itemcodelabel${i }">
						<c:set var="_taxindex" value="0"/>
						<c:forEach items="${groupList}" var="group">
							<c:set var="_taxindex" value="${_taxindex+1}"/>
							<c:set var="tax_exist" value="false"/>
								<c:if test="${fn:length(debit_tax_details) gt 0}">
									<c:forEach items="${debit_tax_details}" var="debittax">
										<c:if test="${group.item_group_id == debittax.map.item_group_id && med.map.medicine_id eq debittax.map.medicine_id && med.map.item_batch_id eq debittax.map.item_batch_id }">
											<c:set var="tax_exist" value="true"/>
											<input type="hidden" name="taxname${group.item_group_id}" id="${i}taxname${group.item_group_id}" value="${debittax.map.item_group_name}" />
											<input type="hidden" name="taxrate${group.item_group_id}" id="${i}taxrate${group.item_group_id}" value="${debittax.map.tax_rate}" />
											<input type="hidden" name="taxamount${group.item_group_id}" id="${i}taxamount${group.item_group_id}" value="${debittax.map.tax_amt}" />
											<input type="hidden" name="taxsubgroupid${group.item_group_id}" id="${i}taxsubgroupid${group.item_group_id}" value="${debittax.map.item_subgroup_id}" />
										</c:if>
									</c:forEach>
								</c:if>
								<c:if test="${tax_exist eq 'false'}">
									<input type="hidden" name="taxname${group.item_group_id}" id="${i}taxname${group.item_group_id}" value="0" />
									<input type="hidden" name="taxrate${group.item_group_id}" id="${i}taxrate${group.item_group_id}" value="0" />
									<input type="hidden" name="taxamount${group.item_group_id}" id="${i}taxamount${group.item_group_id}" value="0" />
									<input type="hidden" name="taxsubgroupid${group.item_group_id}" id="${i}taxsubgroupid${group.item_group_id}" value="0" />
								</c:if>
						</c:forEach>
						${med.map.item_code}
					</label></td>
					<td style="padding-left: 0.5em;"><input type="hidden"
						name="hpkgsz" id='hpkgsz${i}'
						value="${ifn:afmt(med.map.issue_base_unit) }"><label
						id="hpkgszlabel${i }">${ifn:afmt(med.map.issue_base_unit)
					}</label></td>
					<td style="padding-left: 0.5em;">
						<input type="hidden" name="hbatchno" id='hbatchno${i}' value="${med.map.batch_no }">
						<input type="hidden" name="item_batch_id" id='item_batch_id${i}' value="${med.map.item_batch_id }">
						<label id="hbatchnolabel${i }">${med.map.batch_no}</label>
					</td>
					<td style="padding-left: 0.5em;"><input type="hidden"
						name="hexpdt" id='hexpdt${i }' value="${med.map.exp_dt}" /><label
						id="hexpdtlabel${i }"><fmt:formatDate
						value="${med.map.exp_dt}" pattern="MM-yy" /></label></td>
					<td style="text-align:right;"><input type="hidden" name="hmrp"
						id='hmrp${i }'
						value="${qtyselection eq 'P' ? med.map.mrp : ifn:afmt(med.map.mrp/med.map.issue_base_unit)}"><label
						id="hmrplabel${i }">${qtyselection eq 'P' ? med.map.mrp :
					ifn:afmt(med.map.mrp/med.map.issue_base_unit)}</label></td>
					<td align="right" style="text-align:right;">
						<input type="hidden" class="number" readonly name="hactqty" id='hactqty${i}' value="">
						<c:set var="ret_qty" value="${qtyselection eq 'P' ? ifn:afmt((0-med.map.billed_qty)/med.map.issue_base_unit) : ifn:afmt(0-med.map.billed_qty) }"/>
						<c:if test="${prefDecimalQty != 'Y'}">
							<fmt:formatNumber pattern="#" value="${ret_qty}" var="ret_qty"/> <!-- remove decimals for qty if exists -->
						</c:if>
						<label id='hretqtylabel${i}'>${ret_qty}</label>
						<input type="hidden" name="hretqty" id='hretqty${i}' value="${ret_qty}" />
					</td>
					<td align="right" style="text-align:right;">
						<input type="hidden" class="number" readonly name="hactbonusqty" id='hactbonusqty${i}' value="">
						<c:set var="ret_bonus_qty" value="${qtyselection eq 'P' ? ifn:afmt((0-med.map.bonus_qty)/med.map.issue_base_unit) : ifn:afmt(0-med.map.bonus_qty) }"/>
						<c:if test="${prefDecimalQty != 'Y'}">
							<fmt:formatNumber pattern="#" value="${ret_bonus_qty}" var="ret_bonus_qty"/> <!-- remove decimals for qty if exists -->
						</c:if>
						<label id='hretbonusqtylabel${i}'>${ret_bonus_qty}</label>
						<input type="hidden" name="hretbonusqty" id='hretbonusqty${i}' value="${ret_bonus_qty}" />
					</td>
					<td align="left"><label id="itemUnits${i}">${med.map.user_uom}</label><input type="hidden" name="pkg_uom" id="pkg_uom${i}" value="${med.map.user_uom}">
					</td>
					<td align="right" style="text-align:right"><label
						id='hratelabel${i}'>${qtyselection eq 'P' ?
					ifn:afmt(med.map.orig_debit_rate) :
					ifn:afmt(med.map.orig_debit_rate/med.map.issue_base_unit)}</label> <input
						type="hidden" name="hrate" id='hrate${i}'
						value="${qtyselection eq 'P' ? ifn:afmt(med.map.orig_debit_rate ) : 
						ifn:afmt(med.map.orig_debit_rate/med.map.issue_base_unit) }" />

						<input
						type="hidden" name="hgrnrate" id='hgrnrate${i}'
						value="${qtyselection eq 'P' ? ifn:afmt(med.map.orig_debit_rate ) : 
						ifn:afmt(med.map.orig_debit_rate/med.map.issue_base_unit) }" /></td>
						
					<td align="right" style="text-align:right">
						<c:set var="numerIsZero"
							value="${ifn:round(med.map.orig_discount, 'ROUND_CEILING', '0')== 0}" />
						<c:set var="denomIsZero"
							value="${ifn:round(med.map.billed_qty * med.map.orig_debit_rate, 'ROUND_CEILING', '0')== 0}" />

						<c:choose>
							<c:when test="${numerIsZero || denomIsZero}">
								<c:set var="discPer" value="0" />
							</c:when>
							<c:otherwise>
								<c:set var="discPer" value="${ifn:afmt((med.map.orig_discount * 100) / (med.map.billed_qty * med.map.orig_debit_rate/med.map.issue_base_unit))}" />
							</c:otherwise>
						</c:choose>
						<input	type="hidden" name="hdiscper" id='hdiscper${i}'
							value="${discPer}" />
						<label id='hdiscamtlabel${i}' style="text-align:right">
							${0- med.map.orig_discount }
						</label>
						<input type="hidden" name="hdiscamt"
							id='hdiscamt${i}' value="${0 - med.map.orig_discount }" />
					</td>



					<td>
						<c:set var="numerIsZero"
							value="${ifn:round(med.map.orig_scheme_discount, 'ROUND_CEILING', '0')== 0}" />
						<c:set var="denomIsZero"
							value="${ifn:round(med.map.billed_qty * med.map.orig_debit_rate, 'ROUND_CEILING', '0')== 0}" />

						<c:choose>
							<c:when test="${numerIsZero || denomIsZero}">
								<c:set var="schdiscPer" value="0" />
							</c:when>
							<c:otherwise>
								<c:set var="schdiscPer" value="${ifn:afmt((med.map.orig_scheme_discount * 100) / (med.map.billed_qty * med.map.orig_debit_rate/med.map.issue_base_unit))}" />
							</c:otherwise>
						</c:choose>
						<input	type="hidden" name="hschemediscper" id='hschemediscper${i}'
							value="${schdiscPer}" />
						<label id='hschemediscamtlabel${i}' style="text-align:right">
							${0- med.map.orig_scheme_discount }
						</label>
						<input type="hidden" name="hschemediscamt"
							id='hschemediscamt${i}' value="${0 - med.map.orig_scheme_discount }" />
					</td>


					<td align="right" style="text-align:right"><label
						id='hrevratelabel${i}'>${qtyselection eq 'P' ?
					ifn:afmt(med.map.cost_price) :
					ifn:afmt(med.map.cost_price/med.map.issue_base_unit)}</label> <input
						type="hidden" name="hrevrate" id='hrevrate${i}'
						value="${qtyselection eq 'P' ? ifn:afmt(med.map.cost_price) : ifn:afmt(med.map.cost_price/med.map.issue_base_unit)}" /></td>
					<td align="right" style="text-align:right"></label>
					<c:set var="numerIsZero"
							value="${ifn:round(med.map.discount, 'ROUND_CEILING', '0')== 0}" />
					<c:set var="denomIsZero"
							value="${ifn:round(med.map.billed_qty * med.map.cost_price, 'ROUND_CEILING', '0')== 0}" />
					<c:choose>
							<c:when test="${numerIsZero || denomIsZero || finalDenomIsZero}">
								<c:set var="discPer" value="0" />
							</c:when>
							<c:otherwise>
								<c:set var="discPer" value="${ifn:afmt((med.map.discount * 100) / (med.map.billed_qty * med.map.cost_price/med.map.issue_base_unit ))}" />
							</c:otherwise>
					</c:choose>
					<input
						type="hidden" name="hrevdiscper" id='hrevdiscper${i}'
						value="${discPer}" />

						<label
						id='hrevdisclabel${i}'>${0 - med.map.discount }</label> <input
						type="hidden" name="hrevdisc" id='hrevdisc${i}'
						value="${0 - med.map.discount }" /> <input type="hidden"
						name="hvatper" id='hvatper${i}' value="${med.map.tax_rate }" /> <input
						type="hidden" class="number" readonly name="vatperqty"
						id='vatperqty${i}' value="${0-med.map.orig_tax}"> <input
						type="hidden" class="number" readonly name="revvatperqty"
						id='revvatperqty${i}' value="${0-med.map.debit_tax}"> <input
						type="hidden" class="number" readonly name="cedperqty"
						id='vatperqty${i}' value="${med.map.orig_ced}"> <input
						type="hidden" class="number" readonly name="revcedperqty"
						id='revvatperqty${i}' value="${med.map.item_ced}"> <input
						type="hidden" class="number" readonly name="hrevvat"
						id='hrevvat${i}' value="${0-med.map.debit_tax}"> <input
						type="hidden" name="hvat" id='hvat${i}'
						value="${0-med.map.orig_tax }" /> <input type="hidden"
						name="hced" id='hced${i}' value="${med.map.orig_ced }" /> <!-- <input type="hidden" class="number" readonly name="hrevvat" id='hrevvat${i}'  value="${med.map.item_ced_per}" >  -->
					</td>

					<td align="right" style="text-align:right"></label>
					<c:set var="numerIsZero"
							value="${ifn:round(med.map.scheme_discount, 'ROUND_CEILING', '0') == 0}" />
					<c:set var="denomIsZero"
							value="${ifn:round(med.map.billed_qty * med.map.cost_price, 'ROUND_CEILING', '0') == 0}" />
					<c:choose>
							<c:when test="${numerIsZero || denomIsZero || finalDenomIsZero}">
								<c:set var="schemediscPer" value="0" />
							</c:when>
							<c:otherwise>
								<c:set var="schemediscPer" value="${ifn:afmt((med.map.scheme_discount * 100) / (med.map.billed_qty * med.map.cost_price/med.map.issue_base_unit ))}" />
							</c:otherwise>
					</c:choose>
					<input
						type="hidden" name="hrevschemediscper" id='hrevschemediscper${i}'
						value="${schemediscPer}" />
						<label
						id='hrevschemedisclabel${i}'>${0 - med.map.scheme_discount }</label> <input
						type="hidden" name="hrevschemedisc" id='hrevschemedisc${i}'
						value="${0 - med.map.scheme_discount }" />
					</td>
					<td align="right" style="text-align:right"><label
						id='htaxratelabel${i}'>${med.map.tax_rate }</label></td>
					<td align="right" style="text-align:right"><label
						id='htaxamtlabel${i}'>${ifn:afmt(0-med.map.debit_tax)}</label></td>
					<td style="text-align:right;"><c:set var="tamount"
						value="${ qtyselection eq 'P' ? ifn:afmt((med.map.orig_debit_rate * (0 - med.map.billed_qty/med.map.issue_base_unit)) + med.map.orig_tax + med.map.orig_ced- (med.map.orig_discount+med.map.orig_scheme_discount)) : ifn:afmt(((med.map.orig_debit_rate/med.map.issue_base_unit) * (0 - med.map.billed_qty))+ med.map.orig_tax + med.map.item_ced - (med.map.orig_discount+med.map.orig_discount))}" />
					<label id='hamtlabel${i}' style="text-align:right">${(med.map.orig_debit_rate
					ne 0.00) ? (qtyselection eq 'P' ? ifn:afmt((med.map.orig_debit_rate
					* (0 - med.map.billed_qty/med.map.issue_base_unit)) + (0
					-med.map.orig_tax) + ((0 - med.map.item_ced_amt) * (med.map.billed_qty/med.map.issue_base_unit)) - (0 -
					(med.map.orig_discount+med.map.orig_scheme_discount))) :
					ifn:afmt(((med.map.orig_debit_rate/med.map.issue_base_unit) * (0 -
					med.map.billed_qty)) + (0 -med.map.orig_tax) + (
					(0 - med.map.item_ced_amt) * (med.map.billed_qty)) - (0 - (med.map.orig_discount+med.map.orig_scheme_discount)) )) :
					ifn:afmt(tamount)}</label> <input type="hidden" name="hamt" id='hamt${i}'
						value="${(med.map.orig_debit_rate
					ne 0.00) ? (qtyselection eq 'P' ? ifn:afmt((med.map.orig_debit_rate
					* (0 - med.map.billed_qty/med.map.issue_base_unit)) + (0
					-med.map.orig_tax) + ((0 - med.map.item_ced_amt) * (med.map.billed_qty/med.map.issue_base_unit)) - (0 -
					(med.map.orig_discount+med.map.orig_scheme_discount))) :
					ifn:afmt(((med.map.orig_debit_rate/med.map.issue_base_unit) * (0 -
					med.map.billed_qty)) + (0 -med.map.orig_tax) + (
					(0 - med.map.item_ced_amt) * (med.map.billed_qty)) - (0 - (med.map.orig_discount+med.map.orig_scheme_discount)) )) :
					ifn:afmt(tamount)}" />
					</td>
					<td style="text-align:right;"><c:set var="tRecAmount"
						value="${ qtyselection eq 'P' ? ifn:afmt(med.map.cost_price * ((0-med.map.billed_qty/med.map.issue_base_unit) + (med.map.debit_tax) + (med.map.discount+med.map.scheme_discount) )- (med.map.item_ced )) : ifn:afmt((med.map.cost_price/med.map.issue_base_unit) * (0-med.map.billed_qty) + (med.map.debit_tax) + (med.map.item_ced ))}" />
					<label id='hRecdAmtlabel${i}' style="text-align:right">${(med.map.cost_price
					ne 0.00) ? (qtyselection eq 'P' ? ifn:afmt((med.map.cost_price * (0
					- med.map.billed_qty/med.map.issue_base_unit)) + (0 -
					med.map.debit_tax) + ((0 - med.map.item_ced_amt) * (med.map.billed_qty/med.map.issue_base_unit)) - (0 -
					(med.map.discount+med.map.scheme_discount))) : ifn:afmt(((med.map.cost_price /
					med.map.issue_base_unit) * (0 - med.map.billed_qty)) + (0
					-med.map.debit_tax) + ((0 - med.map.item_ced_amt) * (med.map.billed_qty)) - (0 -
					(med.map.discount+med.map.scheme_discount)))) : ifn:afmt(tRecamount)}</label> <input type="hidden"
						name="hRecdAmt" id='hRecdAmt${i}'
						value="${(med.map.cost_price
					ne 0.00) ? (qtyselection eq 'P' ? ifn:afmt((med.map.cost_price * (0
					- med.map.billed_qty/med.map.issue_base_unit)) + (0 -
					med.map.debit_tax) + ((0 - med.map.item_ced_amt) * (med.map.billed_qty/med.map.issue_base_unit)) - (0 -
					(med.map.discount+med.map.scheme_discount))) : ifn:afmt(((med.map.cost_price /
					med.map.issue_base_unit) * (0 - med.map.billed_qty)) + (0
					-med.map.debit_tax) + ((0 - med.map.item_ced_amt) * (med.map.billed_qty)) - (0 -
					(med.map.discount+med.map.scheme_discount)))) : ifn:afmt(tRecamount)}" />
					<input type="hidden" name="hindentno" id='hindentno${i}' value="">
					<input type="hidden" name="hrejQty" id='hrejQty${i}' value="">
					<input type="hidden" name="hTranType" id='hTranType${i}' value="">
					</td>
					<td><label id="itemCheck1"></label> <input type="hidden"
						name="hdeleted" id="hdeleted${i}" value="false" /></td>
					<td><label id="editIcon1"> <img class="button"
						name="editButton" id="editButton${i}"
						<c:choose>
						   <c:when test="${debit.map.status eq 'O' }"> src="../icons/Edit.png"  onclick="getItemGroupDialog(${i}); return false;"</c:when>
						   <c:otherwise>src="../icons/Edit1.png"</c:otherwise></c:choose> /></label>
					</td>

				</tr>
				<c:set var="i" value="${i+1}" />
				<c:set var="totalVat"
					value="${totalVat + (0 - med.map.orig_tax) + (qtyselection eq 'P' ? 
					 (0 - med.map.item_ced_amt) * (med.map.billed_qty/med.map.issue_base_unit) 
					 : (0 - med.map.item_ced_amt) * (med.map.billed_qty) )}" />
				<c:set var="totalRevVat"
					value="${totalRevVat + (0 - med.map.debit_tax) + (qtyselection eq 'P' ? 
					 (0 - med.map.item_ced_amt) * (med.map.billed_qty/med.map.issue_base_unit) 
					 : (0 - med.map.item_ced_amt) * (med.map.billed_qty) )}" />
				<c:set var="totalAmount"
					value="${totalAmount + ((med.map.orig_debit_rate ne 0.00) ? 
					(qtyselection eq 'P' ? 
					ifn:afmt((med.map.orig_debit_rate * (0 - med.map.billed_qty/med.map.issue_base_unit)) + 
					(0 - med.map.orig_tax) + ((0 - med.map.item_ced_amt) * (med.map.billed_qty/med.map.issue_base_unit)) - 
					(0 - (med.map.orig_discount+med.map.orig_scheme_discount))) : 
					ifn:afmt(((med.map.orig_debit_rate / med.map.issue_base_unit) * (0 - med.map.billed_qty))   +
					 (0 - med.map.orig_tax) + ((0 - med.map.item_ced_amt) * (med.map.billed_qty )) - (0 - (med.map.orig_discount+med.map.orig_scheme_discount)))) : ifn:afmt(tamount))}" />
				<c:set var="totalRecAmount"
					value="${totalRecAmount + ((med.map.cost_price ne 0.00) ? (qtyselection eq 'P' ? ifn:afmt((med.map.cost_price * (0 - med.map.billed_qty/med.map.issue_base_unit))  + 
					(0 - med.map.debit_tax)  + ((0 - med.map.item_ced_amt) * (med.map.billed_qty/med.map.issue_base_unit)) -
					 (0 - (med.map.discount+med.map.scheme_discount))) : ifn:afmt(((med.map.cost_price/med.map.issue_base_unit)  * 
					 (0 - med.map.billed_qty))  + (0 - med.map.debit_tax) + ((0 - med.map.item_ced_amt) * (med.map.billed_qty)) - (0 - (med.map.discount+med.map.scheme_discount)))) : ifn:afmt(tRecAmount))}" />
				<c:set var="totalDiscount"
					value="${totalDiscount + (0 - med.map.orig_discount)}" />
				<c:set var="totalRevDiscount"
					value="${totalRevDiscount + (0 - med.map.discount)}" />
					<c:set var="totalSchemeDiscount"
					value="${totalSchemeDiscount + (0 - med.map.orig_scheme_discount)}" />
				<c:set var="totalRevSchemeDiscount"
					value="${totalRevSchemeDiscount + (0 - med.map.scheme_discount)}" />
			</c:forEach>
		</c:when>
		<c:otherwise>
			<tr id="tableRow1" style="display: none;">
				<td class="forminfo" style="width:10em;padding-left: 0.5em;"
					valign="middle"><label id="medName1"></label> <input
					type="hidden" name="hmedId" id='hmedId1' value=""> <input
					type="hidden" name="hmedName" id='hmedName1' value=""> <input
					type="hidden" name="hadjmrp" id='hadjmrp1' value=""> <input
					type="hidden" name="htaxtype" id='htaxtype1' value=""> <input
					type="hidden" name="hitemidentification" id='hitemidentification1'
					value=""> <input type="hidden" name="hitembarcode"
					id="hitembarcode1" value="">
						<c:set var="_taxindex" value="0"/>
						<c:forEach items="${groupList}" var="group">
							<c:set var="_taxindex" value="${_taxindex+1}"/>
							<c:set var="tax_exist" value="false"/>
								<c:if test="${fn:length(debit_tax_details) gt 0}">
									<c:forEach items="${debit_tax_details}" var="potax">
										<c:if test="${group.item_group_id == potax.map.item_group_id && item.medicine_id eq potax.map.medicine_id }">
											<c:set var="tax_exist" value="true"/>
											<input type="hidden" name="taxname${group.item_group_id}" id="1taxname${group.item_group_id}" value="${potax.map.item_group_name}" />
											<input type="hidden" name="taxrate${group.item_group_id}" id="1taxrate${group.item_group_id}" value="${potax.map.tax_rate}" />
											<input type="hidden" name="taxamount${group.item_group_id}" id="1taxamount${group.item_group_id}" value="${potax.map.tax_amt}" />
											<input type="hidden" name="taxsubgroupid${group.item_group_id}" id="1taxsubgroupid${group.item_group_id}" value="${potax.map.item_subgroup_id}" />
										</c:if>
									</c:forEach>
								</c:if>
								<c:if test="${tax_exist eq 'false'}">
									<input type="hidden" name="taxname${group.item_group_id}" id="1taxname${group.item_group_id}" value="0" />
									<input type="hidden" name="taxrate${group.item_group_id}" id="1taxrate${group.item_group_id}" value="0" />
									<input type="hidden" name="taxamount${group.item_group_id}" id="1taxamount${group.item_group_id}" value="0" />
									<input type="hidden" name="taxsubgroupid${group.item_group_id}" id="1taxsubgroupid${group.item_group_id}" value="0" />
								</c:if>
						</c:forEach>
					</td>
					<td><label
					id="itemcodelabel1"></label></td>
				<td style="padding-left: 0.5em;"><input type="hidden"
					name="hpkgsz" id='hpkgsz1' value=""><label
					id="hpkgszlabel1"></label></td>
				<td style="padding-left: 0.5em;">
					<input type="hidden" name="hbatchno" id='hbatchno1' value="">
					<input type="hidden" name="item_batch_id" id='item_batch_id1' value="">
					<label id="hbatchnolabel1"></label>
				</td>
				<td style="padding-left: 0.5em;"><input type="hidden"
					name="hexpdt" id='hexpdt1' value="" /><label id="hexpdtlabel1"></label></td>
				<td style="text-align:right;"><input type="hidden" name="hmrp"
					id='hmrp1' value=""><label id="hmrplabel1"></label></td>
				<td align="right" style="text-align:right"><input
					type="hidden" class="number" readonly name="hactqty" id='hactqty1'
					value=""><label id='hretqtylabel1'></label> <input
					type="hidden" name="hretqty" id='hretqty1' value="" /></td>
					<td align="right" style="text-align:right">
					<label id='hretbonusqtylabel1'></label>
					<input
					type="hidden" class="number" readonly name="hactbonusqty" id='hactbonusqty1'
					value="">
					 <input	type="hidden" name="hretbonusqty" id='hretbonusqty1' value="" /></td>
				<td><label id="itemUnits1"></label><input type="hidden" name="pkg_uom" id="pkg_uom1"></td>
				<td align="right" style="text-align:right"><label
					id='hratelabel1'></label> <input type="hidden" name="hrate"
					id='hrate1' value="" />
					<input type="hidden" name="hgrnrate"
					id='hgrnrate1' value="" /></td>
				<td align="right" style="text-align:right;width:3em;"><input
					type="hidden" name="hdiscper" id='hdiscper1' value="0" /> <label
					id='hdiscamtlabel1' style="text-align:right"></label><input
					type="hidden" name="hdiscamt" id='hdiscamt1' value="" /></td>
				<td align="right" style="text-align:right;width:3em;"><input
					type="hidden" name="hschemediscper" id='hschemediscper1' value="0" /> <label
					id='hschemediscamtlabel1' style="text-align:right"></label>
					<input
					type="hidden" name="hschemediscamt" id='hschemediscamt1' value="" />
					</td>
				<td align="right" style="text-align:right"><label
					id='hrevratelabel1'></label> <input type="hidden" name="hrevrate"
					id='hrevrate1' value="" /></td>
				<td align="right" style="text-align:right"></label> <label
					id='hrevdisclabel1'></label> <input type="hidden" name="hrevdisc"
					id='hrevdisc1' value="" /> <input type="hidden" name="hrevdiscper"
					id='hrevdiscper1' value="" /> <input type="hidden" name="htaxrate"
					id='htaxrate1' value="" /> <input type="hidden" name="hrevtaxrate"
					id='hrevtaxrate1' value="" />
					<input type="hidden" name="hcedamt"
					id='hcedamt1' value="" /> <input type="hidden" class="number"
					readonly name="vatperqty" id='vatperqty1' value=""> <input
					type="hidden" class="number" readonly name="revvatperqty"
					id='revvatperqty1' value=""> <input type="hidden"
					class="number" readonly name="cedperqty" id='cedperqty1' value="">
				<input type="hidden" class="number" readonly name="revcedperqty"
					id='revcedperqty1' value=""> <input type="hidden"
					class="number" readonly name="hrevvat" id='hrevvat1' value="">
				<input type="hidden" class="number" readonly name="hvat" id='hvat1'
					value=""> <input type="hidden" class="number" readonly
					name="hrevced" id='hrevced1' value=""> <input
					type="hidden" class="number" readonly name="hced" id='hced1'
					value=""></td>
				<td align="right" style="text-align:right"></label> <label
					id='hrevschemedisclabel1'></label> <input type="hidden" name="hrevschemedisc"
					id='hrevschemedisc1' value="" /> <input type="hidden" name="hrevschemediscper"
					id='hrevschemediscper1' value="" />
					<td align="right" style="text-align:right"><label
						id='htaxratelabel1'></label></td>
					<td align="right" style="text-align:right"><label
						id='htaxamtlabel1'></label></td>
				<td id="amtcell1" align="right" style="text-align:right"><label
					id='hamtlabel1' style="text-align:right"></label> <input
					type="hidden" name="hamt" id='hamt1' value="" /></td>
				<td id="recdAmtcell1" align="right" style="text-align:right"><label
					id='hRecdAmtlabel1' style="text-align:right"></label> <input
					type="hidden" name="hRecdAmt" id='hRecdAmt1' value="" /> <input
					type="hidden" name="hTranType" id='hTranType1' value="" /></td>
				<td><label id="itemCheck1"></label> <input type="hidden"
					name="hdeleted" id="hdeleted1" value="false" /></td>

				<td><label id="editIcon1"></label></td>
			</tr>
		</c:otherwise>
	</c:choose>
	<!--
				<tr>
				<td colspan="7" style="text-align:right">Totals:</td>
			     <td style="width:4em;text-align:right">
			        <label id="totDisclabel" style="font-weight:bolder;">${ifn:afmt(totalDiscount)}</label>
				   <input type="hidden" name="totDisc" id="totDisc" class="number" style="font-weight:bolder;"
							value="${ifn:afmt(totalDiscount)}"/>
			     </td>
			     <td>&nbsp;</td>
			     <td style="width:4em;text-align:right">
			        <label id="totRevDisclabel" style="font-weight:bolder;">${ifn:afmt(totalRevDiscount)}</label>
				   <input type="hidden" name="totRevDisc" id="totRevDisc" class="number" style="font-weight:bolder;"
							value="${ifn:afmt(totalRevDiscount)}"/>
				   <input type="hidden" name="totVAT" id="totVAT" class="number" style="font-weight:bolder;"
							value="${ifn:afmt(totalVat)}"/>
					<input type="hidden" name="totRevVAT" id="totRevVAT" class="number" style="font-weight:bolder;"
							value="${ifn:afmt(totalRevVat)}"/>
					<input type="hidden" name="totCED" id="totCED" class="number" style="font-weight:bolder;"
							value="${ifn:afmt(totalCed)}"/>
					<input type="hidden" name="totRevCED" id="totRevCED" class="number" style="font-weight:bolder;"
							value="${ifn:afmt(totalRevCed)}"/>
			     </td>
			    <c:if test="${prefVat eq 'Y'}" > <td>&nbsp;</td><td>&nbsp;</td> </c:if>
			     <td style="width:4em;text-align:right">
			      <label id="totAmountlabel" style="font-weight:bolder;">${ifn:afmt(totalAmount)}</label>
				   <input type="hidden" name="totAmount" id="totAmount" class="number" style="font-weight:bolder;"
							value="${ifn:afmt(totalAmount)}"/>
			     </td>
			     <td style="width:4em;text-align:right">
			      <label id="totRecAmountlabel" style="font-weight:bolder;">${ifn:afmt(totalRecAmount)}</label>
				   <input type="hidden" name="totRecAmount" id="totRecAmount" class="number" style="font-weight:bolder;"
							value="${ifn:afmt(totalRecAmount)}"/>
			     </td>
			     <td colspan="3">&nbsp;</td>
			     </tr>
-->
</table>
</div>
<jsp:include page="/pages/stores/PurchaseDetails.jsp"/>
<jsp:include page="/pages/stores/StoresSupplierReturns/AddItemDialog.jsp"/>

<table class="addButton">
	<tr>
		<td>
		<div class="flag"><img src='${cpath}/images/yellow_flag.gif'></div>
		<div class="flagText"><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.costpricebasedtax"/></div>
		</td>
		<td align="right">
		<button name="plusItem" id="plusItem" class="imgButton"
			onclick="openAddDialog(); return false;" accesskey="+"
			title="Add New Item "><c:choose>
			<c:when test="${empty debitList}">
				<img src="../icons/Add.png"/>
			</c:when>
			<c:otherwise>
				<img src="../icons/Add1.png"/>
			</c:otherwise>
		</c:choose></button>
		</td>
	</tr>
</table>

</fieldset>
<fieldset class="fieldSetBorder"><legend
	class="fieldSetLabel"><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.grandtotals"/></legend>
<table align="right" class="infotable" width="600px">
	<tr>
		<td class="formlabel"><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.totaldiscount"/> :</td>
		<td class="forminfo" style="text-align:right;"><label
			id="totDisclabel" style="font-weight:bolder;">${ifn:afmt(totalDiscount)}</label>
		<input type="hidden" name="totDisc" id="totDisc" class="number"
			style="font-weight:bolder;" value="${ifn:afmt(totalDiscount)}" /></td>
		<td class="formlabel"><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.totalschemediscount"/> :</td>
		<td class="forminfo" style="text-align:right;"><label
			id="totSchemeDisclabel" style="font-weight:bolder;">${ifn:afmt(totalSchemeDiscount)}</label>
		<input type="hidden" name="totSchemeDisc" id="totSchemeDisc" class="number"
			style="font-weight:bolder;" value="${ifn:afmt(totalSchemeDiscount)}" /></td>

		<input type="hidden" name="totVAT" id="totVAT" class="number"
			style="font-weight:bolder;" value="${ifn:afmt(totalVat)}" /> <input
			type="hidden" name="totRevVAT" id="totRevVAT" class="number"
			style="font-weight:bolder;" value="${ifn:afmt(totalRevVat)}" /> <input
			type="hidden" name="totCED" id="totCED" class="number"
			style="font-weight:bolder;" value="${ifn:afmt(totalCed)}" /> <input
			type="hidden" name="totRevCED" id="totRevCED" class="number"
			style="font-weight:bolder;" value="${ifn:afmt(totalRevCed)}" /></td>
		<td class="formlabel"><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.totalamount"/> :</td>
		<td class="forminfo" style="text-align:right;"><label id="totAmountlabel" style="font-weight:bolder;">${ifn:afmt(totalAmount)}</label>
		<input type="hidden" name="totAmount" id="totAmount" class="number"
			style="font-weight:bolder;" value="${ifn:afmt(totalAmount)}" /></td>

	</tr>
	<tr>
		<c:set var="_taxindex" value="0"/>
		<c:forEach items="${groupList}" var="group">
			<c:set var="_taxindex" value="${_taxindex+1}"/>
			<td class="formlabel"><label id="taxnamelabel_${group.item_group_id}">${group.item_group_name}</label>:</td>
			<td class="forminfo" style="text-align:right;"><label id="taxamtlabel_${group.item_group_id}" style="font-weight:bolder;">0</label></td>
			<c:if test="${_taxindex%3 == 0}">
				</tr>
				<tr>
			</c:if>
		</c:forEach>
		<td class="formlabel" style="width:12em;"><label><insta:ltext key="storeprocurement.raisepurchaseorder.podetails.totaltax"/></label>:</td>
		<td class="forminfo" style="text-align:right;"><label id="lblTotalTaxes" style="font-weight:bolder;">${totalVat}</label></td>
	</tr>
	<tr>
		<td class="formlabel"><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.totalrevdiscount"/> :</td>
			<td class="forminfo" style="text-align:right;"><label
				id="totRevDisclabel" style="font-weight:bolder;">${ifn:afmt(totalRevDiscount)}</label>
				<input type="hidden" name="totRevDisc" id="totRevDisc" class="number"
			style="font-weight:bolder;" value="${ifn:afmt(totalRevDiscount)}" /></td>
			<td class="formlabel"><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.totalrevschemediscount"/> :</td>
			<td class="forminfo" style="text-align:right;"><label
				id="totRevSchemeDisclabel" style="font-weight:bolder;">${ifn:afmt(totalRevSchemeDiscount)}</label>
			<input type="hidden" name="totRevSchemeDisc" id="totRevSchemeDisc" class="number"
				style="font-weight:bolder;" value="${ifn:afmt(totalRevSchemeDiscount)}" /></td>
		</td>
		<td class="formlabel"><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.totalrevamount"/> :</td>
		<td class="forminfo" style="text-align:right;"><label
			id="totRecAmountlabel" style="font-weight:bolder;">${ifn:afmt(totalRecAmount)}</label>
		<input type="hidden" name="totRecAmount" id="totRecAmount"
			class="number" style="font-weight:bolder;"
			value="${ifn:afmt(totalRecAmount)}" /></td>

	</tr>
</table>
</fieldset>


<table>
	<tr style="display:none;">
		<td ><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.itemleveldiscount"/>:</td>
		<td><input type="text" name="itemleveldisc" readOnly
			id="itemleveldisc" class="number"
			onkeypress="return enterNumAndDot(event);"
			onchange="validateMaxPercent(this.value, this);return makeingDec(this.value,this);"
			value="0" />%</td>
		<td><input type="button" name="itemlvl" id="itemlvl"
			value="${apply}" class="button" onclick="return itemLevelDiscount();"></td>
	</tr>
</table>
<fieldset class="fieldSetBorder"><legend
	class="fieldSetLabel"><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.debitnoteamounts"/></legend>
<table class="formtable">
	<tr>
		<td class="formlabel"><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.status"/>:</td>
		<c:choose>
			<c:when test="${debit.map.status == 'C' && urlRightsMap.reopen_grn == 'N' && roleId ne 1 && roleId ne 2 }">
				<td><insta:selectoptions name="status" id="status"
					value="${debit.map.status}" opvalues="C" optexts="Closed"
					style="width: 13em" /></td>
			</c:when>
			<c:otherwise>
				<td><insta:selectoptions name="status" id="status"
					value="${debit.map.status}" opvalues="O,C" optexts="Open,Closed"
					style="width: 13em" /></td>
			</c:otherwise>
		</c:choose>
		<td></td>
		<td></td>
	</tr>
	<tr>
		<td class="formlabel"><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.otherdiscounts"/>:</td>
		<td><input type="text" name="discount" id="discount"
			style="width: 5em" value="${ifn:afmt(debit.map.discamt )}"
			class="vtax" onkeypress="return enterNumAndDot(event);"
			onchange="validateOtherDiscount(); makeingDec(this.value,this); onchangeDiscType();calculateNetPayble()"
			<c:if test="${debit.map.status == 'C' }"><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.readonly"/></c:if> />&nbsp;<insta:selectoptions
			name="discType" value="${debit.map.discount_type}" opvalues="P,A"
			optexts="Percent,Amount"
			onchange="validateOtherDiscount(); onchangeDiscType()"
			style="width: 7em" /></td>
		<td class="formlabel"><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.discountamt"/>:</td>
		<td><input type="text" style="width:6em;" name="otherdisc"
			id="otherdisc" value="${debit.map.discount }" readonly class="vtax" /><input
			type="text" style="width:6em;" name="otherrevdisc" id="otherrevdisc"
			value="${debit.map.discount }" readonly class="vtax" /></td>
	</tr>
	<tr>
		<td class="formlabel"><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.othercharges"/>:</td>
		<td><input type="text" name="otherCharges" id="otherCharges"
			class="vtax" style="width: 12em" value="${debit.map.other_charges }"
			onkeypress="return enterNumAndDot(event);"
			onchange="return makeingDec(this.value,this),calculateNetPayble()"
			<c:if test="${debit.map.status == 'C' }" ><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.readonly"/> </c:if> /></td>
		<td class="formlabel"><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.description"/>:</td>
		<td><input type="text" name="otherDescription"
			id="otherDescription" value="${debit.map.other_charges_remarks }"
			maxlength="99"
			<c:if test="${debit.map.status == 'C' }" ><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.readonly"/> </c:if> /></td>
	</tr>
	<tr>
		<td class="formlabel"><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.roundoffamt"/>:</td>
		<td><input type="text" name="roundAmt" id="roundAmt" class="vtax"
			value="${debit.map.round_off }"
			onkeypress="return enterNumAndDotAndMinus(event);"
			onchange="return makeingRoundoff(this.value,this, decDigits),calculateNetPayble()"
			<c:if test="${debit.map.status == 'C' }" ><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.readonly"/></c:if> /></td>
		<td></td>
		<td></td>
	</tr>
	<tr>
		<td class="formlabel"><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.netamtdebited"/>:</td>
		<td><input type="text" name="netAmtPayble" id="netAmtPayble"
			class="amt" style="font-weight:bolder;" readonly /></td>
		<td class="formlabel"><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.reviseddebitamt"/>:</td>
		<td><input type="text" name="recDebitAmt" id="recDebitAmt"
			class="amt"
			<c:if test="${debit.map.status == 'C' }" ><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.readonly"/> </c:if>
			value="${debit.map.received_debit_amt}" style="font-weight:bolder;"
			onkeypress="return enterNumAndDot(event);"
			onchange="makeingDec(this.value,this);" /></td>
	</tr>
	<c:if test="${fromedit == 'Y'}">
		<script>
	 		calculateNetPayble();
	 	</script>
	</c:if>
</table>
</fieldset>
<div class="screenActions">
<button type="button" name="saveStk" id="saveStk" accesskey="U" value=""
	class="button" onclick="return savestock();"><b><u><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.u"/></u></b><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.pdate"/>
<insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.debitnote"/></button>
<a
	href="${pageContext.request.contextPath}/stores/StoresSupplierReturns.do?_method=getSupplierReturnDebits&sortOrder=debit_note_no&sortReverse=true"><insta:ltext key="storeprocurement.supplierreturn.debit.notedetails.backtodashboard"/></a></div>
</form>

<script>

</script>
</body>
</html>

<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
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
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="prefMaxCP" value="${genPrefs.pharmacyValidateCostPrice}"/>
<c:set var="prefQtyType" value="${genPrefs.qtyDefaultToIssueUnit}"/>
<c:set var="prefDecimalQty" value="${genPrefs.allowdecimalsforqty}"/>
<c:set var="wo" value="${wobean.map}"/>	<%-- convenience --%>
<c:set var="approveWORts" value="${(roleId eq 1 || roleId eq 2 ) ? 'A' : actionRightsMap.allow_wo_approve}"/>
<c:set var="existStatus" value="${wo.status}" />

<c:set var="title">
	<c:if test="${op_mode == 'edit'}">
		<insta:ltext key="resourcemanagement.workorder.raisewo.wodetails.editworkorder"/>
	</c:if>
	<c:if test="${op_mode == 'add'}">
		<insta:ltext key="resourcemanagement.workorder.raisewo.wodetails.raiseworkorder"/>
	</c:if>
	<c:if test="${op_mode == 'view'}">
		<insta:ltext key="resourcemanagement.workorder.raisewo.wodetails.viewworkorder"/>
	</c:if>
</c:set>
<c:set var="status">
	<insta:ltext key="resourcemanagement.workorder.raisewo.wodetails.open"/>,
	<insta:ltext key="resourcemanagement.workorder.raisewo.wodetails.approved"/>,
	<insta:ltext key="resourcemanagement.workorder.raisewo.wodetails.rejected"/>,
	<insta:ltext key="resourcemanagement.workorder.raisewo.wodetails.forceclosed"/>
</c:set>
<c:set var="statusvalues">
	O,A,R,FC
</c:set>
<c:set var="cancelText">
	<insta:ltext key="resourcemanagement.workorder.raisewo.wodetails.cancel"/>
</c:set>
<c:set var="taxLabel" value="${genPrefs.procurement_tax_label}" scope="request"/>
<html>
<head>
	<title>${title}</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<meta name="i18nSupport" content="true"/>
	<insta:link type="script" file="ajax.js"/>
	<insta:link type="script" file="date_go.js"/>
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="js" file="maintcont/wo.js"/>
	<script>
		var popurl = '${pageContext.request.contextPath}';
		var centerId = '${centerId}';
		var qtyDecimal = '${prefDecimalQty}';
		var jAllSuppliers = <%= StoresDBTablesUtil.getTableDataInJSON(StoresDBTablesUtil.GETSUPPLIERS) %>;
	 	var jCenterSuppliers = ${listAllcentersforAPo};
	 	var itemList = <%= StoresDBTablesUtil.getTableDataInJSON(StoresDBTablesUtil.GET_ALL_WO_ITEMS) %>;
	 	var initSupplierId = '${ifn:cleanJavaScript(param.supplier_id)}';
	 	var woNo = '${wobean.map.wo_no}';
	 	var approveWORts = '${approveWORts}';
	 	var prefVAT = '${prefVat}';
	</script>
	<insta:js-bundle prefix="resourcemanagement.workorder"/>
	<style>
		.scrolForContainer .yui-ac-content{
			 max-height:18em;overflow:auto;overflow-x:auto; /* scrolling */
		    _height:18em; max-width:35em; width:35em;/* ie6 */
		}
		
		.selectedRow {background-color: #C7E782;}
	</style>
</head>
<body onload="init();" class="yui-skin-sam">
<form name="woForm" method="post" action="workorder.do?_method=saveWO" autocomplete="off">
	<input type="hidden" id="wo_no" name="wo_no" value="${wo.wo_no }"/>
	<input type="hidden" name="_printAfterSave" value=""/>
	<h1>${title}</h1>
	
	<div><insta:feedback-panel/></div>
	
	<div align="left">
		<fieldset class="fieldSetBorder" >
			<legend class="fieldSetLabel"><insta:ltext key="resourcemanagement.workorder.raisewo.wodetails.wodetails"/></legend>
			<table class="formtable" cellpadding="0" cellspacing="0" border="0" width="100%">
				<tr>
					<c:if test="${!empty wo.wo_no}">
						<td class="formlabel">
							<insta:ltext key="resourcemanagement.workorder.list.wono"/>:
						</td>
						<td>
							<div class="formlabel"><b>${wo.wo_no}</b></div>
						</td>
					</c:if>
				</tr>
				<tr>
					<td class="formlabel"><span class="prestar">*</span><insta:ltext key="resourcemanagement.workorder.raisewo.wodetails.supplier"/>:</td>
					<td valign="top">
						<div id="supplier_container">
							<input type="text" name="supplier_name" value="${wo.supplier_name}"/>
							<div id="supplier_dropdown" class="scrolForContainer"></div>
							<input type="hidden" name="supplier_id" value="${wo.supplier_id}"/>
						</div>
					</td>
					<td class="formlabel"><insta:ltext key="resourcemanagement.workorder.raisewo.wodetails.supplieraddress"/>:</td>
					<td class="forminfo" colspan="1"><label id="suppAddId"></label></td>
					<td class="formlabel"><insta:ltext key="resourcemanagement.workorder.raisewo.wodetails.expecteddate"/>:</td>
					<fmt:formatDate  var="expected_received_date_fmt" pattern="dd-MM-yyyy" value="${wo.expected_received_date}"/>
					<td><insta:datewidget name="expected_received_date" valid="future" value="${empty expected_received_date_fmt? 'today':expected_received_date_fmt}"/></td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext key="resourcemanagement.workorder.raisewo.wodetails.remarks"/>:</td>
					<td class="forminfo" colspan="3">
						<textarea name="remarks" style="width: 30em;" maxlength="4000">${wo.remarks}</textarea>
					</td>
					<td class="formlabel"><insta:ltext key="resourcemanagement.workorder.raisewo.wodetails.status"/>:</td>
					<c:if test="${op_mode eq 'add'}">
						<td ><insta:selectoptions name="status_fld" id="status_fld" value="${existStatus}" opvalues="O" optexts="Open" disabled='true'/>
							<input type="hidden" name="status" value="O"/>
						</td>
					</c:if>
					<c:if test="${op_mode eq 'edit'}">
						<c:choose>
							<c:when test="${existStatus eq 'A'}">
								<td ><insta:selectoptions name="status_fld" id="status_fld" value="${existStatus}" opvalues="A" optexts="Approved" disabled="true"/>
									<input type="hidden" name="status" value="${existStatus}"/>
								</td>
							</c:when>
							<c:when test="${existStatus eq 'O' && approveWORts eq 'A'}">
								<td ><insta:selectoptions name="status_fld" id="status_fld" value="${existStatus}" opvalues="${statusvalues}" optexts="${status}"/>
									<input type="hidden" name="status" value="${existStatus}"/>
								</td>
							</c:when>
							<c:when test="${existStatus eq 'O'}">
								<td ><insta:selectoptions name="status_fld" id="status_fld" value="${existStatus}" opvalues="O,FC" optexts="Open, Force Closed"/>
									<input type="hidden" name="status" value="${existStatus}"/>
								</td>
							</c:when>
							<c:when test="${existStatus eq 'FC'}">
								<td ><insta:selectoptions name="status_fld" id="status_fld" value="${existStatus}" opvalues="FC" optexts="Force Closed" disabled="true"/>
									<input type="hidden" name="status" value="${existStatus}"/>
								</td>
							</c:when>
							<c:when test="${existStatus eq 'C'}">
								<td ><insta:selectoptions name="status_fld" id="status_fld" value="${existStatus}" opvalues="C" optexts="Closed" disabled="true"/>
									<input type="hidden" name="status" value="${existStatus}"/>
								</td>
							</c:when>
							<c:when test="${existStatus eq 'X'}">
								<td ><insta:selectoptions name="status_fld" id="status_fld" value="${existStatus}" opvalues="X" optexts="Cancelled" disabled="true"/>
									<input type="hidden" name="status" value="${existStatus}"/>
								</td>
							</c:when>
							<c:when test="${existStatus eq 'R'}">
								<td ><insta:selectoptions name="status_fld" id="status_fld" value="${existStatus}" opvalues="R" optexts="Rejected" disabled="true"/>
									<input type="hidden" name="status" value="${existStatus}"/>
								</td>
							</c:when>
						</c:choose>
					</c:if>
				</tr>
			</table>
		</fieldset>
	</div>
	<div align="left">
		<fieldset class="fieldSetBorder" >
			<legend class="fieldSetLabel"><insta:ltext key="resourcemanagement.workorder.raisewo.wodetails.workorderitemdetails"/></legend>
			<table class="detailList dialog_displayColumns" width="100%" cellspacing="0" cellpadding="0" id="itemtabel">
				<tr>
					<th style="text-align: left"><insta:ltext key="resourcemanagement.workorder.raisewo.wodetails.item"/></th>
					<th style="text-align: left"><insta:ltext key="resourcemanagement.workorder.raisewo.wodetails.qty"/></th>
					<th style="text-align: left"><insta:ltext key="resourcemanagement.workorder.raisewo.wodetails.rate"/></th>
					<c:if test="${prefVat eq 'Y'}">
						<th style="text-align: left"><insta:ltext key="resourcemanagement.workorder.raisewo.wodetails.taxper"/></th>
						<th style="text-align: left"><insta:ltext key="resourcemanagement.workorder.raisewo.wodetails.tax"/></th>
					</c:if>
					<th style="text-align: left"><insta:ltext key="resourcemanagement.workorder.raisewo.wodetails.grid.discount"/></th>
					<th style="text-align: left"><insta:ltext key="resourcemanagement.workorder.raisewo.wodetails.description"/></th>
					<th style="text-align: left"><insta:ltext key="resourcemanagement.workorder.raisewo.wodetails.itemtotal"/></th>
					<th></th>
					<th></th>
				</tr>
				<c:set var="totalservicetax" value="0"/>
				<c:set var="totalAmount" value="0"/>
				<c:set var="totalDiscount" value="0"/>
				<c:set var="totalVat" value="0"/>
				<c:set var="tax" value="0"/>
				<c:set var="totalcess" value="0"/>
				<c:set var="totalitemamount" value="0"/>
				<c:set var="numItems" value="${fn:length(woItems)}"/>
				<c:forEach begin="1" end="${numItems+1}" var="i" varStatus="loop">
					<c:set var="item" value="${woItems[i-1].map}"/>
					<c:set var="attribs">
						<c:choose>
			      		<c:when test="${empty item}">style="display:none"</c:when>
							<c:otherwise>id="itmRow${i-1}"</c:otherwise>
						</c:choose>
					</c:set>
					<c:set var="flagColor">
						<c:choose>
							<c:when test="${item.status == 'R'}"><insta:ltext key="resourcemanagement.workorder.raisewo.wodetails.red"/></c:when>
							<c:when test="${item.status == 'A' }"><insta:ltext key="resourcemanagement.workorder.raisewo.wodetails.green"/></c:when>
							<c:otherwise><insta:ltext key="billing.patientbill.details.empty"/></c:otherwise>
						</c:choose>
					</c:set>
					
					<tr ${attribs}>
						<td class="forminfo" style="width:25em;padding-left: 0.5em;white-space:normal;" valign="middle">
							<%-- display values are different from stored values in case qty unit is package --%>
							<img src="${cpath}/images/${flagColor}_flag.gif"/>
							<label style="text-align: center;">${item.wo_item_name}</label>
							<input type="hidden" name="wo_item_id" value="${item.wo_item_id}">
							<input type="hidden" name="wo_item_name" value="${item.wo_item_name}">
							<input type="hidden" name="qty" value="${item.qty}">
						   	<input type="hidden" name="qty_display">
						    <input type="hidden" name="rate" value="${item.rate}">
						    <input type="hidden" name="rate_display">
						    <input type="hidden" name="discount_per" value="">
						    <input type="hidden" name="discount" value="${item.discount}">
						    <c:if test="${prefVat eq 'Y'}">
						    	<input type="hidden" name="vat_rate" value="${item.vat_rate}">
						    	<input type="hidden" name="item_tax" value="${item.item_tax}">
						    </c:if>
							<input type="hidden" name="description" value="${item.description}" />
							<input type="hidden" name="amount" value="${item.amount}">
						    <input type="hidden" name="total" value="">
							<input type="hidden" name="dbvalue" value="${item.dbvalue}">
							<input type="hidden" name="status_ar" value="${item.status}" />
							<input type="hidden" name="_deleted" value="false"/>
						</td>
			
						<%-- values are set in javascript using the hidden field values as common code, so that
						Package/Unit quantity can be handled correctly in one place --%>
						<td style="text-align: left;"></td>	<%-- qty --%>
						<td style="text-align: left;"></td>	<%-- rate --%>
						<c:if test="${prefVat eq 'Y'}">
							<td style="text-align: left;"></td>	<%-- vat --%>
							<td style="text-align: left;"></td>	<%-- tax --%>
						</c:if>
						<td style="text-align: left;"></td>	<%-- discount --%>
						<td style="text-align: left;width:40px;"></td>	<%-- description --%>
						<td style="text-align: left;"></td>	<%-- total --%>
						<td style="width:20px;">
							<label>
								<img class="imgDelete" src="${cpath}/icons/delete.gif"
								onclick="deleteItem(this)" style="cursor:pointer;float:right;" />
							</label>
						</td>
						<td style="width:20px;">
							<label>
								<img class="button" name="editicon" onclick="openEditDialogBox(this)"
								src="${cpath }/icons/Edit.png" style="cursor:pointer;float:right;" >
							</label>
						</td>
					</tr>
					<c:set var="totalAmount" value="${totalAmount + item.amount}"/>
					<c:set var="totalAmountwithDiscount" value="${totalAmountwithDiscount + ((item.qty*item.rate)-item.discount)}"/>
					<c:set var="totalDiscount" value="${totalDiscount + item.discount}"/>
					<c:set var="totalVat" value="${totalVat + item.item_tax}"/>
					<c:set var="tax" value="${wo.total_tax}"/>
				</c:forEach>
				
				<c:set var="totalservicetax" value="${(totalAmountwithDiscount * wo.wo_service_tax)/100}"/>
				<c:set var="totalcess" value="${((totalservicetax+totalVat) * wo.wo_cess_rate)/100}"/>
				<c:set var="totalitemamount" value="${(totalAmount + totalcess + totalservicetax )}"/>
			</table>
			<table class="addButton">
				<tr>
					<td align="right">
						<button type="button" name="plusItem" id="plusItem" title='<insta:ltext key="resourcemanagement.workorder.raisewo.wodetails.addnewitem"/>'
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
		</fieldset>
		<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel"><insta:ltext key="resourcemanagement.workorder.raisewo.wodetails.workordertax"/></legend>
			<table align="left" class="infotable" width="100%">
				<tr>
					<c:choose>
						<c:when test="${taxLabel eq 'V'}">
							<td class="formlabel"><insta:ltext key="resourcemanagement.workorder.raisewo.wodetails.${taxLabel}.servietax"/>:</td>
							<td class="forminfo" >
								<input type="text" name="wo_service_tax" id="servicetax" value="${wo.wo_service_tax}" maxlength="6" onkeypress="return enterNumAndDot(event);" onchange="onChangeServiceTax();"/>
							</td>	
						</c:when>
						<c:otherwise>
							<input type="hidden" name="wo_service_tax" id="servicetax" value="0"/>
						</c:otherwise>
					</c:choose>
					<td class="formlabel"><insta:ltext key="resourcemanagement.workorder.raisewo.wodetails.cess"/>:</td>
					<td class="forminfo" >
						<input type="text" name="wo_cess_rate" id="cess" value="${wo.wo_cess_rate}" maxlength="6" onkeypress="return enterNumAndDot(event);" onchange="onChangeCESS();"/>
					</td>
				</tr>
			</table>
		</fieldset>
		<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel"><insta:ltext key="resourcemanagement.workorder.raisewo.wodetails.total"/></legend>
			<table align="left" class="infotable" width="100%">
				<tr>
					<td colspan="2">&nbsp;</td>
					<c:if test="${prefVat eq 'Y'}">	
						<td class="formlabel" style="text-align:right;"><insta:ltext key="resourcemanagement.workorder.raisewo.wodetails.totalvat.${taxLabel }"/>:</td>
						<td class="forminfo" style="text-align:left;"><label id="lblTotalVat">${ifn:afmt(totalVat)}</label></td>
					</c:if>
					<c:choose>
						<c:when test="${taxLabel eq 'V'}">
							<td class="formlabel" style="text-align:right;"><insta:ltext key="resourcemanagement.workorder.raisewo.wodetails.totalservicetax"/>:</td>
							<td class="forminfo" style="text-align:left;"><label id="lblServiceTaxes">${ifn:afmt(totalservicetax)}</label></td>
						</c:when>
						<c:otherwise>
							<td class="formlabel" style="text-align:right;display:none;"><insta:ltext key="resourcemanagement.workorder.raisewo.wodetails.totalservicetax"/>:</td>
							<td class="forminfo" style="text-align:left;display:none;"><label id="lblServiceTaxes">${ifn:afmt(totalservicetax)}</label></td>
						</c:otherwise>
					</c:choose>
					
					<td class="formlabel" style="text-align:right;"><insta:ltext key="resourcemanagement.workorder.raisewo.wodetails.totaltax"/>:</td>
					<td class="forminfo" style="text-align:left;"><label id="lblTotalTaxes">${ifn:afmt(tax)}</label>
						<input type="hidden" name="total_tax" value="${wo.total_tax}"/>
					</td>
				</tr>
		
				<tr>
					<td class="formlabel" style="text-align:right;"><insta:ltext key="resourcemanagement.workorder.raisewo.wodetails.totalitemamount"/>:</td>
					<td class="forminfo" style="text-align:left;"><label id="lblItemTotal">${ifn:afmt(totalAmount)}</label>
						<input type="hidden" name="total_amount" value="${wo.total_amount}"/>
					</td>
					<td class="formlabel" style="text-align:right;"><insta:ltext key="resourcemanagement.workorder.raisewo.wodetails.totaldiscount"/>:</td>
					<td class="forminfo" style="text-align:left;"><label id="lblDiscount">${ifn:afmt(totalDiscount)}</label>
						<input type="hidden" name="total_discount" value="${wo.total_discount}"/>
					</td>
					
					<td class="formlabel" style="text-align:right;"><insta:ltext key="resourcemanagement.workorder.raisewo.wodetails.totalcess"/>:</td>
					<td class="forminfo" style="text-align:left;"><label id="lblCessTaxes">${ifn:afmt(totalcess)}</label></td>
					
					<td class="formlabel" style="text-align:right;"><insta:ltext key="resourcemanagement.workorder.raisewo.wodetails.totalwoamount"/>:</td>
					<td class="forminfo" style="text-align:left;"><label id="lblWOTotal">${ifn:afmt(totalitemamount)}</label></td>
				</tr>
		
			</table>
		</fieldset>
	</div>
	<c:if test="${existStatus eq 'O' || empty existStatus}">
		<div class="screenActions" style="float: left">
			<button type="button" name="btnSaveWo" onclick="onlySaveWO();" accessKey='S'>
				<b><u><insta:ltext key="resourcemanagement.workorder.raisewo.wodetails.s"/></u></b><insta:ltext key="resourcemanagement.workorder.raisewo.wodetails.ave"/>
			</button>
			<c:if test="${approveWORts eq 'A'}">
			<button type="button" name="btnSavendsetstatusWo" onclick="saveAndSetStatusWO('A');" accessKey= 'P'>
				<insta:ltext key="resourcemanagement.workorder.raisewo.wodetails.save"/> &amp; <insta:ltext key="resourcemanagement.workorder.raisewo.wodetails.a"/><b><u><insta:ltext key="resourcemanagement.workorder.raisewo.wodetails.p"/></u></b><insta:ltext key="resourcemanagement.workorder.raisewo.wodetails.pprove"/>
			</button>
			</c:if>
			<button type="button" name="btnSaveAndPrintWo" onclick="saveAndPrintWO();" accessKey='R'>
				<insta:ltext key="resourcemanagement.workorder.raisewo.wodetails.save"/> &amp; <insta:ltext key="resourcemanagement.workorder.raisewo.wodetails.p"/><b><u><insta:ltext key="resourcemanagement.workorder.raisewo.wodetails.r"/></u></b><insta:ltext key="resourcemanagement.workorder.raisewo.wodetails.int"/>
			</button>
				| <a href="${cpath}/resourcemanagement/workorder.do?_method=getWOs&status=A&status=O&sortOrder=wo_no&sortReverse=false"><insta:ltext key="resourcemanagement.workorder.list.wolist"/></a>
		</div>
	</c:if>
	<c:if test="${existStatus eq 'A' || existStatus eq 'R' || existStatus eq 'X' || existStatus eq 'C' || existStatus eq 'FC'}">
		<button type="button" name="btnSaveAndPrintWo" onclick="printWO();" accessKey='P'>
				<b><u><insta:ltext key="resourcemanagement.workorder.raisewo.wodetails.P"/></u></b><insta:ltext key="resourcemanagement.workorder.raisewo.wodetails.rint"/>
		</button>
				| <a href="${cpath}/resourcemanagement/workorder.do?_method=getWOs&status=A&status=O&sortOrder=wo_no&sortReverse=false"><insta:ltext key="resourcemanagement.workorder.list.wolist"/></a>	
	</c:if>	
</form>

<form name="dlgForm">
	<div id="detaildialog" style="visibility:hidden">
		<div class="bd">
			<fieldset class="fieldSetBorder" style="width: 98%;text-align:center;">
				<legend class="fieldSetLabel"><insta:ltext key="resourcemanagement.workorder.raisewo.wodetails.additem"/></legend>
				<table class="formtable" cellpadding="0" cellspacing="0" border="0" width="100%">
					<tr>
						<td class="formLabel"><insta:ltext key="resourcemanagement.workorder.raisewo.wodetails.item"/>:</td>
						<td colspan="2" >
							<div id="item_wrapper" style="width: 20em; padding-bottom:2em; ">
								<input type="text" name="wo_item_name" id="wo_item_name" style="width: 20em" maxlength="4000"/>
								<div id="item_dropdown" class="scrolForContainer"></div>
							</div>
							<input type="hidden" name="wo_item_id" value="" />
							<!-- <input type="hidden" name="discount" value="" />  -->
							
						</td>
					</tr>
		
					<tr>
						<td class="formLabel"><insta:ltext key="resourcemanagement.workorder.raisewo.wodetails.rate"/>:</td>
						<td>
							<input type="text" onkeypress="return enterNumAndDot(event);"  name="rate_display"
								onChange="onChangeCostPrice();" maxlength="13"/>
						</td>
						<td class="formLabel"><insta:ltext key="resourcemanagement.workorder.raisewo.wodetails.qty"/>:</td>
						<td>
							<input type="text" name="qty_display" onkeypress="return onKeyPressAddQty(event);" onChange="onChangeQty();"/>
						</td>
					</tr>
					<tr>
						<td class="formLabel"><insta:ltext key="resourcemanagement.workorder.raisewo.wodetails.discount"/>:</td>
						<td>
							<input type="text" name="discount_per" onkeypress="return enterNumAndDot(event);"
							onChange="onChangeDiscountAmt();"/>
						</td>
						<td class="formLabel"><insta:ltext key="resourcemanagement.workorder.raisewo.wodetails.discountAmt"/>:</td>
						<td>
							<input type="text" name="discount" onkeypress="return enterNumAndDot(event);"
							onChange="onChangeDiscountPer(this);"/>
						</td>
					</tr>
					<tr>	
						<c:if test="${prefVat eq 'Y'}">
							<td class="formLabel"><insta:ltext key="resourcemanagement.workorder.raisewo.wodetails.taxper"/>:</td>
							<td>
								<input name="vat_rate" type="text" onkeypress="return enterNumAndDot(event);"
								onChange="onChangeTaxAmt();"/>
							</td>
						</c:if>
					</tr>
		
					<tr>
						<c:if test="${prefVat eq 'Y'}">
							<%-- <td class="formLabel"><insta:ltext key="resourcemanagement.workorder.raisewo.wodetails.tax"/>:</td> --%>
							<input name="item_tax" type="hidden" onkeypress="return enterNumAndDot(event);" disabled="disabled"/>
						</c:if>
						
						<!-- <td>
							<input name="description" type="text"/>
						</td> -->
						<td class="formLabel"><insta:ltext key="resourcemanagement.workorder.raisewo.wodetails.item.status"/>:</td>
						<td><insta:selectoptions name="item_status" value="" dummyvalue="--Select--" dummyvalueId="" opvalues="A,R" optexts="Approve,Reject"/></td>
						
					</tr>
					<tr>
						<td class="formLabel"><insta:ltext key="resourcemanagement.workorder.raisewo.wodetails.description"/>:</td>
						<td class="forminfo" colspan="5">
							<textarea name="description" style="width: 30em;" maxlength="4000"></textarea>
						</td>
						<td></td>
					</tr>
				</table>
			</fieldset>
		
			<table width="100%">
				<tr>
					<td style="text-align:left;">
						<button type="button" accesskey="A"
							onclick="onDialogSave();"><label><u><b><insta:ltext key="resourcemanagement.workorder.raisewo.wodetails.a"/></b></u><insta:ltext key="resourcemanagement.workorder.raisewo.wodetails.dd"/></label></button>&nbsp;
						<input type="button" value="${cancelText}" onclick="onDialogCancel();" />
					</td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
					<td style="text-align:right;">
						<button type="button" id="prevDialog" accesskey="V" onclick="onDialogPrevNext(false);" disabled><label>&lt;&lt;<insta:ltext key="resourcemanagement.workorder.raisewo.wodetails.pre"/><u><b><insta:ltext key="resourcemanagement.workorder.raisewo.wodetails.v"/></b></u><insta:ltext key="resourcemanagement.workorder.raisewo.wodetails.ious"/></label></button>
						&nbsp;
						<button type="button" id="nextDialog" accesskey="N" onclick="onDialogPrevNext(true);" disabled><label> <u><b><insta:ltext key="resourcemanagement.workorder.raisewo.wodetails.n"/></b></u><insta:ltext key="resourcemanagement.workorder.raisewo.wodetails.ext"/> &gt;&gt;</label></button>
					</td>
				</tr>
			</table>
		</div>
	</div>
</form>
<div style="clear: both"></div>
<div class="legend">
	<div class="flag"><img src='${cpath}/images/green_flag.gif'></div>
	<div class="flagText"><insta:ltext key="resourcemanagement.workorder.raisewo.wodetails.approval.flag"/></div>
	<div class="flag"><img src='${cpath}/images/red_flag.gif'></div>
	<div class="flagText"><insta:ltext key="resourcemanagement.workorder.raisewo.wodetails.reject.flag"/></div>

</div>
</body>
</html>
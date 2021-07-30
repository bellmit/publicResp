<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<%@ page import="com.insta.hms.stores.StoresDBTablesUtil"%>
<%@ page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO" %>

<html>
<head>
	<title><insta:ltext key="storeprocurement.supplierreplacement.list.title"/></title>

	<insta:link type="js" file="ajax.js" />
	<insta:link type="js" file="date_go.js" />
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="js" file="stores/supplierreturn.js" />
	<insta:link type="js" file="stores/storescommon.js" />
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="css" file="widgets.css"/>
<style type="text/css">
	 	.scrolForContainer .yui-ac-content{
		 max-height:18em;overflow:auto;overflow-x:auto; /* scrolling */
	    _height:18em; max-width:35em; width:35em;/* ie6 */
		}
</style>

	<c:set var="typeOfSale" value=""/>
	<c:set var="prefQtyType" value="<%= GenericPreferencesDAO.getGenericPreferences().getQtyDefaultToIssueUnit() %>"/>
	<c:set var="prefDecimalQty" value="<%= GenericPreferencesDAO.getGenericPreferences().getAllowdecimalsforqty() %>"/>
	<c:set var="prefretAgtSupp" value="<%= GenericPreferencesDAO.getGenericPreferences().getReturnAgainstSpecificSupplier() %>"/>
	<c:set var="defaultValue" value="${(prefDecimalDigits == 3) ? '0.000' : '0.00'}"/>
	<c:set var="prefbarcode" value="<%= GenericPreferencesDAO.getGenericPreferences().getBarcodeForItem() %>" />
	<c:choose>
	<c:when test="${not empty groupStoreId}">
		<c:set var="defStoreVal" value="${groupStoreId}"/>
	</c:when>
	<c:otherwise>
		<c:set var="defStoreVal" value="${dept_id}"/>
	</c:otherwise>
</c:choose>

	<script>
		var groupMedDetails = ${groupMedDetails};
		var groupDeptId  = '${ifn:cleanJavaScript(groupDeptId)}';
		var popurl = '${pageContext.request.contextPath}';
		var jAllSuppliers = <%= StoresDBTablesUtil.getTableDataInJSON(StoresDBTablesUtil.GETSUPPLIERS)%>;
		var jCenterSuppliers = ${listAllcentersforDebit};
		var deptId = '${ifn:cleanJavaScript(dept_id)}';
		var gRoleId = '${roleId}';
		var qtyDecimal = '${prefDecimalQty}';
		var retAgtSupp = '${prefretAgtSupp}';
		var selsupp = '${ifn:cleanJavaScript(param.supplier_name)}';
		var prefBarCode = '${prefbarcode}';
		var sesHospitalId = '${ifn:cleanJavaScript(sesHospitalId)}';
		var centerId = '${centerId}';
	</script>

<style>
	.scrolForContainer .yui-ac-content{
	 max-height:18em;overflow:auto;overflow-x:auto; /* scrolling */
    _height:18em; max-width:35em; width:35em;/* ie6 */
</style>
</head>
<c:set var="supplier_return">
<insta:ltext key="storeprocurement.supplierreturns.replacementlist.list.return"/>
</c:set>
<body onload="init();" class="yui-skin-sam">
<c:set var="returntype">
<insta:ltext key="storeprocurement.supplierreplacement.list.damage"/>,
<insta:ltext key="storeprocurement.supplierreplacement.list.expiry"/>,
<insta:ltext key="storeprocurement.supplierreplacement.list.non_moving"/>,
<insta:ltext key="storeprocurement.supplierreplacement.list.others"/>
</c:set>
<div id="storecheck" style="display: block;" >
 <form method="POST" action="StoresSupplierReturnslist.do" name="supplierreturnsform">
 <input type="hidden" name="_method" value="makeSupplierReturns"/>
 <input type="hidden" id="dialogId" value=""/>
 <h1><insta:ltext key="storeprocurement.supplierreplacement.list.supplierreturns"/></h1>

<div style="padding-bottom:.5em;" title="If you change selection ,existing row(s) in the Grid will be deleted">
	<insta:ltext key="storeprocurement.supplierreplacement.list.qtyin"/>: <input type="radio" name="qty_unit" id="issue_units"  onclick="clearGrid()" <c:if test="${prefQtyType eq 'Y'}" ><insta:ltext key="storeprocurement.supplierreplacement.list.checked"/> </c:if> value="I"><insta:ltext key="storeprocurement.supplierreplacement.list.unituom"/>
 		<input type="radio" name="qty_unit" id="pkg_units" onclick="clearGrid()" <c:if test="${prefQtyType eq 'N'}" ><insta:ltext key="storeprocurement.supplierreplacement.list.checked"/> </c:if>  value="P"><insta:ltext key="storeprocurement.supplierreplacement.list.packageuom"/>
</div>
	<div id="itemDialog" style="visibility:hidden">
		<div class="bd">
		    <fieldset class="fieldSetBorder"><legend class="fieldSetLabel"><insta:ltext key="storeprocurement.supplierreplacement.list.additem"/></legend>
			<table cellpadding="0" cellspacing="0" width="100%">
					<tr>
						<c:if test="${prefbarcode eq 'Y'}">
							<td ><insta:ltext key="storeprocurement.supplierreplacement.list.itembarcode"/> </td>
						</c:if>
						<td><insta:ltext key="storeprocurement.supplierreplacement.list.item"/></td>
						<td><insta:ltext key="storeprocurement.supplierreplacement.list.batch.or.serial.no"/></td>
						<td><insta:ltext key="storeprocurement.supplierreplacement.list.expdate"/></td>
						<td><insta:ltext key="storeprocurement.supplierreplacement.list.mrp"/></td>
						<td><insta:ltext key="storeprocurement.supplierreplacement.list.currentstock"/></td>
						<td><insta:ltext key="storeprocurement.supplierreplacement.list.returnqty"/></td>
					</tr>
					<tr>
					    <c:choose>
						<c:when test="${prefbarcode eq 'Y'}">
							<td ><input type="text" name="barCodeId" id="barCodeId" onchange="getItemBarCodeDetails(this.value);" tabindex="4"></td>
					    </c:when>
					    <c:otherwise>
					    	<input type="hidden" name="barCodeId" id="barCodeId" >
					    </c:otherwise>
					    </c:choose>
						<td valign="top">
							<div id="medicine_wrapper" style="width: 14em; padding-bottom:0.2em">
								<input type="text" name="medicine" id="medicine"  style="width: 14em;" tabindex="5">
								<div id="medicine_dropdown"  class="scrolForContainer"></div>
							</div>
						</td>
						<td>
							<select name="batch" id="batch" onchange="displayMedicineDetails();" class="dropdown" tabindex="6">
								<option value=""><insta:ltext key="storeprocurement.supplierreplacement.list.select"/></option>
							</select>
						</td>
						<td class="forminfo" style="width:8em"><b><label id="expdate"></label></b></td>
						<td class="forminfo" style="width:8em"><b><label id="mrp"></label></b></td>
						<td class="forminfo" style="width:8em"><b><label id="currentstock"></label></b></td>
						<td>
							<input type="text" name="returnQty" id="returnQty" class="number" size="4"  onkeypress="return onKeyPressAddQty(event);"
							onchange="return makeingDecValidate(this.value,this.id),onchangeQty();" tabindex="6" >
							<input type="hidden" id="dialognum" value=""/>
							<input type="hidden" id="issueunits"/>
							<label id="item_unit" > </label>
						</td>
					</tr>
				</table>
				</fieldset>
				<table>
					<tr><td>&nbsp;</td></tr>
					<tr>
						<td>
							<button type="button" id="Save" name="Save" accesskey="A"  style="display: inline;" class="button" onclick="onAddMedicine();"tabindex="6"><label><b><insta:ltext key="storeprocurement.supplierreplacement.list.save"/></b></label></button>
							<button type="button" id="Cancel" name="Cancel" accesskey="A"  style="display: inline;" class="button" onclick="handleCancel();"tabindex="7"><label><b><insta:ltext key="storeprocurement.supplierreplacement.list.cancel"/></b></label></button>
						</td>
					</tr>
			</table>
		</div>
	</div>
	    <div style="width:650px">
           <fieldset class="fieldSetBorder"><legend class="fieldSetLabel"><insta:ltext key="storeprocurement.supplierreplacement.list.returndetails"/></legend>
				<table class="formtable" >
					<tr>
						<td class="formLabel"><insta:ltext key="storeprocurement.supplierreplacement.list.supplier"/>:</td>
						<td valign="top">
						<div id="supplier_wrapper" style="width: 15em;">
							<input type="text" name="supplierName" id="supplierName"  class="field"  value="${ifn:cleanHtmlAttribute(param.supplier_name )}">
							<div id="suppliername_dropdown" class="scrolForContainer"></div>
						</div>
					</td>
					<td class="formLabel"><insta:ltext key="storeprocurement.supplierreplacement.list.store"/>:</td>
					<c:choose>
						<c:when test="${(multiStoreAccess eq 'A' || roleId eq 1 || roleId eq 2 )}">
						<td>
							 <insta:userstores username="${userid}" elename="store" id="store" onchange="changeStore();" val="${defStoreVal}"/>
							 </td>
						</c:when>
					<c:otherwise>
                        <td><b>${ifn:cleanHtml(dept_name)}</b>
						<input type = "hidden" name="store" id="store" value="${not empty groupDeptId ? groupDeptId : dept_id}" />
						</td>
					</c:otherwise>
					</c:choose>
					</tr>
					<tr><td class="formlabel"><insta:ltext key="storeprocurement.supplierreplacement.list.supplieraddress"/>:</td><td class="forminfo" colspan="5"><label id="suppAddId"></label></td></tr>
					<tr>
					    <td class="formLabel"><insta:ltext key="storeprocurement.supplierreplacement.list.returntype"/>:</td>
					    <td><insta:selectoptions name="returnType" value="" opvalues="D,E,N,O"
					    	class="dropdown" optexts="${returntype}"/><span class="star">*</span></td>
						<td class="formLabel"><insta:ltext key="storeprocurement.supplierreplacement.list.remarks"/>:</td>
						<td>
						<input type="text" name="remarks" id="remarks" maxlength="500"  onblur="upperCase(remarks)" size="60">
						</td>
						<td>
						<input type="checkbox" name="gatepass" id="gatepass" ><insta:ltext key="storeprocurement.supplierreplacement.list.gatepass"/>
						</td>
					</tr>

				</table>
			</fieldset>
			</div>
			<fieldset class="fieldSetBorder"><legend class="fieldSetLabel"><insta:ltext key="storeprocurement.supplierreplacement.list.itemlist"/></legend>
			<table class="detailList" id="medList" >
				<tr>

					<th><insta:ltext key="storeprocurement.supplierreplacement.list.itemname"/></th>
					<th><insta:ltext key="storeprocurement.supplierreplacement.list.manf"/></th>
					<th><insta:ltext key="storeprocurement.supplierreplacement.list.batch.or.serial.no"/></th>
					<th><insta:ltext key="storeprocurement.supplierreplacement.list.mrp"/></th>
					<th><insta:ltext key="storeprocurement.supplierreplacement.list.expdate"/></th>
					<th><insta:ltext key="storeprocurement.supplierreplacement.list.stockqty"/></th>
					<th><insta:ltext key="storeprocurement.supplierreplacement.list.returnqty"/></th>
					<th><insta:ltext key="storeprocurement.supplierreplacement.list.uom"/></th>
					<th></th>
					<th></th>
				   <tr id="tableRow1">
					    <td "style=width:10em;padding-left:0.5em;">
					    	<label id ="medlabel1"></label>
					    	<input type="hidden" name="hmedId" id="hmedId1" value="">
					    	<input type="hidden" name="hmedName" id="hmedName1" value="">
					    	<input type="hidden" name="hitemidentification" id="hitemidentification1" value="">
					    	<input type="hidden" name="hitembarcode" id="hitembarcode1" value="">
					    </td>
					    <td "style=width:10em;padding-left:0.5em;">
					    	<label id ="manflabel1"></label>
					    </td>
					    <td "style=width:10em;padding-left:0.5em;">
					    	<label id ="batchlabel1"></label>
					    	<input type="hidden" name="itemBatchId" id="itemBatchId1" value="">
					    </td>
					    <td "style=width:10em;padding-left:0.5em;">
					    	<label id ="mrplabel1"></label>
					    	<input type="hidden" name="hpkgsz" id="hpkgsz1" value="">
					    </td>
					    <td "style=width:10em;padding-left:0.5em;">
					    	<label id ="expdtlabel1"></label>
					    	<input type="hidden" name="hexpdt" id="hexpdt1" value="">
					    </td>
					    <td "style=width:10em;padding-left:0.5em;">
					    	<label id ="currentstklabel1"></label>
					    	<input type="hidden" name="hactqty" id="hactqty1" value="">
					    </td>
					    <td "style=width:10em;padding-left:0.5em;">
					    	<label id ="qtylabel1"></label>
					    	<input type="hidden" name="hretqty" id="hretqty1" value="">
					    	<input type="hidden" name="hindentno" id="hindentno1" value="">
					    </td>
					     <td "style=width:10em;padding-left:0.5em;">
					    	<label id ="issueUnitsLabel1"></label>
					    </td>
					    <td "style=width:10em;padding-left:0.5em;">
					    	<label id="itemCheck1"></label>
					    	<input type="hidden" name="hdeleted" id="hdeleted1"  value="false">
					    </td>
					    <td "style=width:10em;padding-left:0.5em;">
						    <button name="addBut" id="addBut1" class="imgButton" accesskey="+" onclick="getItemGroupDialog(1); return false;" title='<insta:ltext key="storeprocurement.supplierreplacement.list.addnewitem"/>'>
						    	<img class="button" name="add" id="add1"
											style="cursor:pointer;"    src="../icons/Add.png"/>
							</button>
						</td>
				   </tr>

			 </table>
			 </fieldset>

	<div class="screenActions">
            <input type="button" name="saveStk" value="${supplier_return}" class="button" onclick="return savestock();">
            <a href="${pageContext.request.contextPath}/stores/StoresSupplierReturns.do?_method=getSupplierReturns&sortOrder=return_no&sortReverse=true"><insta:ltext key="storeprocurement.supplierreplacement.list.backtodashboard"/></a>
     </div>
	</form>
	</div>
</body>
</html>

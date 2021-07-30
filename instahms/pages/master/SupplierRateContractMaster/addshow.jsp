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
<c:set var="sscbean" value="${sscbean.map}"/>	<%-- convenience --%>
<c:set var="supplierNameList" value="${supplierRateContractNameList}"/>	<%-- convenience --%>
<c:set var="max_centers" value='<%= GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default") %>' scope="request"/>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<c:set var="title">
<c:choose>
<c:when test="${mode == 'additem' }">
		<insta:ltext key="master.supplierratecontract.raisecontract.add.rate"/>
</c:when>
<c:otherwise>
	<c:if test="${op_mode == 'add'}">
		<insta:ltext key="master.supplierratecontract.raisecontract.addcontract"/>
	</c:if>
	<c:if test="${op_mode == 'edit'}">
		<insta:ltext key="master.supplierratecontract.raisecontract.editcontract"/>
	</c:if>
</c:otherwise>
</c:choose>
</c:set>
<c:set var="status">
	<insta:ltext key="master.supplierratecontract.raisecontract.status.active"/>,
	<insta:ltext key="master.supplierratecontract.raisecontract.status.inactive"/>
</c:set>
<c:set var="statusvalues">
	A,I
</c:set>
<c:set var="existStatus" value="${sscbean.status}" />
<html>
<head>
	<title>${title}</title>
	<script>
		var centerId = '${centerId}';
		var jAllSuppliers = ${listAllcentersforAPo};
	 	var supplier_rate_contract_id = '${supplier_rate_contract_id}';
	 	var supplierContractNameList = ${supplierNameList};
	 	var opMode = '${op_mode}';
	 	var mode = '${mode}';
	</script>
	<insta:js-bundle prefix="master.supplierratecontract"/>
	<insta:link type="script" file="ajax.js"/>
	<insta:link type="script" file="date_go.js"/>
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="js" file="masters/SupplierRateContracts.js"/>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<meta name="i18nSupport" content="true"/>

<%-- all items in master, using variable jItemNames. this is not used when strict PO. --%>
<script src="${cpath}/pages/stores/getItemMaster.do?ts=${master_timestamp}&hosp=${ifn:cleanURL(sesHospitalId)}"></script>
<insta:js-bundle prefix="stores.procurement"/>
<insta:js-bundle prefix="stores.suppliercontracts.itemrate"/>
</head>

<body onload="init();" class="yui-skin-sam">
	<form name="supplierRateContractForm" method="post" enctype="multipart/form-data" action="SupplierRateContractMaster.do?_method=saveSupplierRateContract" autocomplete="off">
	<input type="hidden" id="supplier_rate_contract_id" name="supplier_rate_contract_id" value="${param.supplier_rate_contract_id }"/>
		<h1>${title}</h1>
		<div><insta:feedback-panel/></div>
		<div align="left">
		<fieldset class="fieldSetBorder" >
			<legend class="fieldSetLabel"><insta:ltext key="master.supplierratecontract.raisecontract.supplierratecontract"/></legend>
				<table class="formtable">
						<tr>
							<td class="formlabel"><insta:ltext key="master.supplierratecontract.raisecontract.contractname"/><span class="prestar">*</span>:</td>
							<td valign="top" style="width:150px;">
								<input type="hidden" id="supplier_contract_name_hid" name="supplier_contract_name_hid" value="${sscbean.supplier_rate_contract_name}"/>
								<input type="text" name="supplier_contract_name" id="supplier_contract_name" value="${sscbean.supplier_rate_contract_name}" ${mode == 'additem'? 'disabled' : '' }/>
							</td>
							
							<td class="formlabel"><insta:ltext key="master.supplierratecontract.raisecontract.supplier"/><span class="prestar">*</span>:</td>
							<td valign="top" style="width:200px;">
								<div id="supplier_container">
									<c:choose>
										<c:when test="${op_mode == 'edit'}">
											<input type="text" name="supplier_name" id="supplier_name" value="${sscbean.supplier_name}" ${mode == 'additem'? 'disabled' : '' } />	
										</c:when>
										<c:otherwise>
											<input type="text" name="supplier_name" id="supplier_name" value="${sscbean.supplier_name}" ${mode == 'additem'? 'disabled' : '' } />
										</c:otherwise>
									</c:choose>
									<div id="supplier_dropdown" class="scrolForContainer" style="width:250px;"></div>
									<input type="hidden" name="supplier_id" id="supplier_id" value="${supplier_code}"/>
								</div>
							</td>
							
							<td class="formlabel"><insta:ltext key="master.supplierratecontract.raisecontract.supplieraddress"/>:</td>
							<td class="forminfo" colspan="1"><label id="suppAddId"></label></td>
						</tr>
						<tr>
							<td class="formlabel"><insta:ltext key="master.supplierratecontract.raisecontract.vstart"/><span class="prestar">*</span>:</td>
							<fmt:formatDate  var="validity_start_date_fmt" pattern="dd-MM-yyyy" value="${sscbean.validity_start_date}"/>
							<td><insta:datewidget name="validity_start_date" id="validity_start_date" value="${empty validity_start_date_fmt? 'today':validity_start_date_fmt}"  /></td>
							
							<td class="formlabel"><insta:ltext key="master.supplierratecontract.raisecontract.vend"/><span class="prestar">*</span>:</td>
							<fmt:formatDate  var="validity_end_date_fmt" pattern="dd-MM-yyyy" value="${sscbean.validity_end}"/>
							<td><insta:datewidget name="validity_end_date"  id="validity_end_date" value="${empty validity_end_date_fmt? 'today':validity_end_date_fmt}" /></td>
							
							<td class="formlabel"><insta:ltext key="master.supplierratecontract.raisecontract.status"/>:</td>
							<td ><insta:selectoptions name="status" id="status" value="${existStatus}" opvalues="${statusvalues}" optexts="${status}"  /></td>
						</tr>
						<c:if test="${mode != 'additem'}">
						<tr>
							<td class="formlabel"><insta:ltext key="master.supplierratecontract.raisecontract.export"/>:</td>
							<td>
								<c:url var="downloadUrl" value="SupplierRateContractMaster.do">
									<c:param name="_method" value="exportMaster"/>
									<c:param name="supplier_rate_contract_id" value="${param.supplier_rate_contract_id }"/>
								</c:url>
								<a href="${downloadUrl}" target="blank"><insta:ltext key="master.supplierratecontract.raisecontract.download"/></a>
							</td>
							<td class="formlabel"><insta:ltext key="master.supplierratecontract.raisecontract.import"/>:</td>
							<td colspan="3">
								<input type="file" name="uploadFile" id="uploadFile" accept="<insta:ltext key="upload.accept.master"/>"/>
							</td>
						</tr>
						</c:if>
				</table>
		</fieldset>
		<c:if test="${mode == 'additem'}">
			<table class="detailList dialog_displayColumns" width="100%" cellspacing="0" cellpadding="0" id="medtabel">
				<tr>
					<th title='<insta:ltext key="storeprocurement.stockentry.invoicedetails.itemname"/>'><insta:ltext key="storeprocurement.stockentry.invoicedetails.item"/></th>
					<th><insta:ltext key="storeprocurement.stockentry.invoicedetails.item.pkg.size"/></th>
					<th><insta:ltext key="storeprocurement.stockentry.invoicedetails.mrp"/></th>
					<th><insta:ltext key="storeprocurement.stockentry.invoicedetails.rate"/></th>
					<th><insta:ltext key="storeprocurement.stockentry.invoicedetails.disc"/></th>
					<th><insta:ltext key="ui.label.margin"/></th>
					<th><insta:ltext key="ui.label.margin.type"/></th>
					<th>&nbsp;</th>
					<th>&nbsp;</th>
				</tr>
		
				<tr style="display:none">
					<td class="forminfo" style="width:25em;padding-left: 0.5em;white-space:normal;" valign="middle">
						<label></label>
						<input type="hidden" name="medicine_name" value="">
						<input type="hidden" name="medicine_id" value="">
						<input type="hidden" name="issue_base_unit" value="">
						<input type="hidden" name="mrp" value="" >
						<input type="hidden" name="supplier_rate" value=""  >
						<input type="hidden" name="discount" value="" >
						<input type="hidden" name="margin" value="" >
						<input type="hidden" name="margin_type" value="" >
					</td>
					<td>
						<label></label>
					</td>
					<td>
						<label></label>
					</td>
					<td>
						<label></label>
					</td>
					<td><label></label></td>
					<td><label></label></td>
					<td><label></label></td>
					<td>
						<label>
							<img class="button" name="editicon" onclick="onEditRow(this)"
							src="${cpath }/icons/Edit.png">
						</label>
					</td>
						<td>
							<label>
								<img class="imgDelete" src="${cpath}/icons/delete.gif"
								onclick="onDeleteRow(this)" style="cursor:pointer" />
							</label>
						</td>
		
				</tr>
			</table>
		
			<table class="addButton">
				<tr>
					<td align="right">
						
						<button type="button" name="plusItem" id="plusItem" title='<insta:ltext key="storeprocurement.stockentry.invoicedetails.addnewitem"/>'
							onclick="openAddDialog(); return false;" accessKey="+"
				 			class="imgButton" ><img src="${cpath}/icons/Add.png"></button>
				 			
					</td>
					
				</tr>
			</table>
		</c:if>
		</div>
		<button type="button" name="save" onclick="saveSupplierRateContract();" accessKey='S'><b><u><insta:ltext key="master.supplierratecontract.raisecontract.s"/></u></b><insta:ltext key="master.supplierratecontract.raisecontract.ave"/></button>
		<c:if test="${op_mode == 'edit'}">
			<insta:screenlink screenId="mas_supplier_rate_contracts" addPipe="true" label="Add Supplier Rate Contract"
				extraParam="?_method=addshow" title="Add Supplier Rate Contract"/>
		</c:if>
		<insta:screenlink screenId="mas_supplier_rate_contracts" addPipe="true" label="Supplier Rate Contract List"
				extraParam="?_method=list&status=A" title="Supplier Rate Contract List"/>
		<c:if test="${max_centers > 1 && op_mode != 'add'}">
			<insta:screenlink screenId="mas_supplier_rate_contracts_center_ass" addPipe="true" label="Supplier Rate Contract Center Applicability"
				extraParam="?_method=getScreen&supplier_rate_contract_id=${param.supplier_rate_contract_id}"
				title="Supplier Rate Contract Center Applicability"/>
		</c:if>
		<c:if test="${mode != 'additem' && sscbean.status == 'A'}">
			| <a href="${cpath}/pages/master/SupplierRateContractMaster.do?_method=addScreen&supplier_rate_contract_id=${param.supplier_rate_contract_id}&supplier_code=${param.supplier_code}&status=${param.status}" target="" title="Add Supplier Item Rates" onclick="">Add Supplier Item Rates</a>
			
		</c:if>
		<c:if test="${mode == 'additem' && sscbean.status == 'A'}">
		| <a href="${cpath}/pages/master/SupplierContractMaster/SupplierContractItemRates.do?_method=Redirectlist&supplier_rate_contract_id=${param.supplier_rate_contract_id}&supplier_code=${param.supplier_code}&status=${param.status}" target="" title="Add Supplier Item Rates" onclick="">Edit Supplier Item Rates</a>
	
		</c:if>
	</form>
	
<form name="detailForm" onsubmit="javascript:void(0); return false;">
	<div id="addEditDialog" style="visibility:hidden; display:none">
		<div class="bd" style="text-align:center;">
			<fieldset class="fieldSetBorder" style="width: 98%;text-align:center;">
				<legend class="fieldSetLabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.itemdetails"/></legend>
				<table  class="formtable" cellpadding="0" cellspacing="0" border="0" width="100%">
					<tr>
						<td class="formLabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.item"/>:</td>
						<td valign="top" style="width:200px;">
							<div id="item_wrapper">
								<input type="text" name="medicine_name" id="medicine_name" />
								<div id="item_dropdown" class="scrolForContainer" style="width:250px;"></div>
							</div>
						</td>
						<td class="formLabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.item.pkg.size"/>:</td>
						<td class="forminfo" colspan="1"><label id="itemPkgSize"></label>
							<input type="hidden" name="issue_base_unit" id="issue_base_unit" value=""/></td>
					</tr>
					<tr>
						<td class="formLabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.mrp"/>:</td>
						<td><input type="text" name="mrp" id="mrp" value="" class="numeric"  onkeypress="return enterNumAndDot(event),onKeyPressAddQty();" onblur="setMrpValue()"/></td>
						<td class="formLabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.rate"/>:</td>
						<td><input type="text" name="supplier_rate" id="supplier_rate" value="" class="numeric" onkeypress="return enterNumAndDot(event),onKeyPressAddQty(event)" onblur="setRateValue()"/>
							<input type="hidden" name="medicine_id" id="medicine_id" value=""/></td>
					</tr>
					<tr>
						<td class="formLabel"><insta:ltext key="storeprocurement.stockentry.invoicedetails.discount"/>:</td>
						<td><input type="text" name="discount" id="discount" value="" onkeypress="return enterNumAndDot(event),onKeyPressAddQty(event);" class="numeric" onblur="setDiscountValue()"/></td>
					</tr>
					<tr>
						<td class="formLabel"><insta:ltext key="ui.label.margin"/>:</td>
						<td>
							<input type="text" name="margin" id="margin" value="" class="numeric" onkeypress="return enterNumAndDot(event)"/>
						</td>
						<td class="formLabel"><insta:ltext key="ui.label.margin.type"/>:</td>
						<td>
							<select name="margin_type" id="margin_type">
								<option value="">--Select--</option>
								<option value="P">Percent</option>
								<option value="A">Amount</option>
							</select>
						</td>
					</tr>
				</table>
				<table width="100%"><tr><td>
					<div style="float: left">
						<button type="button" id="Add" name="Add"  onclick="onDialogSave();" accessKey="A" ><u><b><insta:ltext key="storeprocurement.stockentry.invoicedetails.a"/></b></u><insta:ltext key="storeprocurement.stockentry.invoicedetails.dd"/></button>
						<button type="button" id="prevDialog" name="prevDialog"  onclick="onNextPrev(this);"  disabled="disabled" accessKey="P" ><< <u><b><insta:ltext key="storeprocurement.stockentry.invoicedetails.p"/></b></u><insta:ltext key="storeprocurement.stockentry.invoicedetails.revious"/></button>
						<button type="button" id="nextDialog" name="nextDialog"  onclick="onNextPrev(this);"  disabled="disabled" accessKey="N" ><u><b><insta:ltext key="storeprocurement.stockentry.invoicedetails.n"/></b></u><insta:ltext key="storeprocurement.stockentry.invoicedetails.ext"/> >></button>
						
					</div>
				</td></tr></table>
				
			</fieldset>			
	</div>
</div>
</form>
<script>
	if ( mode == 'additem' ){
		document.getElementById('validity_start_date').disabled = true;
		document.getElementById('validity_end_date').disabled = true;
		document.supplierRateContractForm.status.disabled = true;
	}
	var DecimalDigits = ${prefDecimalDigits};
</script>
</body>

</html>
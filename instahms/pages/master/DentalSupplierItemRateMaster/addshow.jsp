<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<html>
<head>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Add/Edit Dental Supplier Item Rate Master - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>

<script>
var dentalSupplireItemRateList = <%= request.getAttribute("dentalSupplireItemRateList") %>;
function validate() {

	var supplierName = document.getElementById('supplier_name');
	var itemName = document.getElementById('item_name');
	var unitRate = document.getElementById('unit_rate');

	if (empty(itemName.value)) {
		alert('Please select item name');
		itemName.focus();
		return false;
	}

	if (empty(supplierName.value)) {
		alert('Please select supplier name');
		supplierName.focus();
		return false;
	}

	if (empty(unitRate.value)) {
		alert('Please enter unit rate');
		unitRate.focus();
		return false;
	}

	if(isNaN(unitRate.value)) {
		alert("not a number");
		unitRate.value = parseFloat(0).toFixed(decDigits);
		return false;
	}

	if(!isDuplicate()) {
		alert("association of item name and supplier name is already exists.");
		return false;
	}

	return true;
}


function isDuplicate() {
	var method = document.dentalSupplierRateMaster._method.value;
	var sItemId = document.getElementById('item_name').value;
	var sSupplierId = document.getElementById('supplier_name').value;
	var sItemSupplierRateId = document.getElementById('item_supplier_rate_id').value;
	var itemId;
	var supplierId;
	var itemSupplierRateId;
	var item;

	if(method == 'add') {
		for(var i=0;i<dentalSupplireItemRateList.length;i++) {
			item = dentalSupplireItemRateList[i];
			itemId = item.item_id;
			supplierId = item.supplier_id;

			if(sItemId == itemId && sSupplierId == supplierId) {
				return false;
			}
		}
	} else {
		for(var i=0;i<dentalSupplireItemRateList.length;i++) {
			item = dentalSupplireItemRateList[i];
			itemId = item.item_id;
			supplierId = item.supplier_id;
			itemSupplierRateId = item.item_supplier_rate_id;

			if(sItemSupplierRateId != itemSupplierRateId && sItemId == itemId && sSupplierId == supplierId) {
				return false;
			}
		}
	}
	return true;
}

</script>

</head>
<body>

<form action="DentalSupplierRateMaster.do" method="POST" name="dentalSupplierRateMaster">
	<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}">
	<input type="hidden" name="item_supplier_rate_id" id="item_supplier_rate_id" value="${bean.map.item_supplier_rate_id}"/>

	<h1>${param._method == 'add' ? 'Add' : 'Edit'} Dental Supplier Items Rate</h1>
	<insta:feedback-panel/>
	<fieldset class="fieldsetborder">
		<legend class="fieldsetlabel">Denatl Supplier Item Rate Details</legend>

		<table class="formtable">
			<tr>
				<td class="formlabel">Item Name:</td>
				<td>
					<insta:selectdb id="item_name" name="item_name" value="${bean.map.item_id}"
						table="dental_supplies_master" class="dropdown"   dummyvalue="-- Select --"
						valuecol="item_id"  displaycol="item_name"  filtercol="status" filterval="A" filtered="true" orderby="item_name"/><span class="star">*</span>
					<input type="hidden" name="c_item_id" id="c_item_id" value="${bean.map.item_id}">
				</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
			</tr>
			<tr>
				<td class="formlabel">Supplier Name:</td>
				<td>
					<insta:selectdb id="supplier_name" name="supplier_name" value="${bean.map.supplier_id}"
						table="dental_supplier_master" class="dropdown"   dummyvalue="-- Select --"
						valuecol="supplier_id"  displaycol="supplier_name" filtered="true" filtercol="status" filterval="A" orderby="supplier_name"/><span class="star">*</span>
					<input type="hidden" name="c_supplier_id" id="c_supplier_id" value="${bean.map.supplier_id}">
				</td>
			</tr>
			<tr>
				<td class="formlabel">Status:</td>
				<td><insta:selectoptions name="status" value="${bean.map.status}" opvalues="A,I" optexts="Active,Inactive" /></td>
			</tr>
			<tr>
				<td class="formlabel">Unit Rate:</td>
				<td>
					<input type="text" name="unit_rate" id="unit_rate" value="${bean.map.unit_rate}"  onkeypress="return enterNumAndDot(event);" onchange="return formatAmountObj(this)"><span class="star">*</span>
				</td>
			</tr>
			<tr>
				<td class="formlabel">Vat Percentage(%):</td>
				<td>
					<input type="text" name="vat_perc" id="vat_perc" value="${bean.map.vat_perc}"  onkeypress="return enterNumAndDot(event);" onchange="return formatAmountObj(this, true)">
				</td>
			</tr>
		</table>

	</fieldset>

	<div class="screenActions">
		<button type="submit" accesskey="S" onclick="return validate();"><b><u>S</u></b>ave</button>
		<c:if test="${param._method=='show'}">| <a href="DentalSupplierRateMaster.do?_method=add" >Add Dental Supplier Item Rate</a></c:if>
		| <a href="DentalSupplierRateMaster.do?_method=list&sortOrder=item_name&sortReverse=false&item_rate_status=A">Supplier Item Rate List</a>
	</div>
</form>

</body>
</html>

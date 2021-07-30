<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<html>
<head>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Add/Edit Dental Supplier Master - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>

<script>
	var chkSupplierName = ${ifn:convertListToJson(suppliersList)};
	var supplierItemsRateList = ${ifn:convertListToJson(supplierItemsRateList)};
	var backupName = '';



function validate() {

	var supplierName = document.getElementById('supplier_name').value.trim();
	if (empty(supplierName)) {
		alert('Please enter supplier name');
		document.getElementById('supplier_name').focus();
		return false;
	}

	var address = document.dentalSupplierMaster.supplier_address;

	if(!empty(address.value) && address.value.length > 500) {
		alert("address can't be more than 500 characters.")
		address.focus();
		return false;
	}

	var supplierPh1 = document.getElementById('supplier_phone1').value;
	var supplierPh2 = document.getElementById('supplier_phone2').value;
	var contactPersonPh = document.getElementById('contact_person_mobile_number').value;

	if(!empty(supplierPh1) && !validatePhoneNo(supplierPh1)) {
		document.getElementById('supplier_phone1').focus();
		return false;
	}
	if(!empty(supplierPh2) && !validatePhoneNo(supplierPh2)) {
		document.getElementById('supplier_phone2').focus();
		return false;
	}

	if(!empty(contactPersonPh) && !validatePhoneNo(contactPersonPh)){
		document.getElementById('contact_person_mobile_number').focus();
		return false;
	}

	if (!checkDuplicate()) return false;

	return true;
}

function checkDuplicate(){
	var newSupplierName = trimAll(document.dentalSupplierMaster.supplier_name.value);

	for(var i=0;i<chkSupplierName.length;i++){
		item = chkSupplierName[i];
		if (newSupplierName == item.supplier_name){
			alert(document.dentalSupplierMaster.supplier_name.value+" already exists pls enter other name...");
	    	document.dentalSupplierMaster.supplier_name.value='';
	    	document.dentalSupplierMaster.supplier_name.focus();
	    	return false;
		}
	}
	return true;
}

</script>

</head>
<body >

<form action="create.htm" method="POST" name="dentalSupplierMaster">

	<h1>Add Dental Supplier</h1>
	<insta:feedback-panel/>
	<fieldset class="fieldsetborder">
		<legend class="fieldsetlabel">Denatl Supplier Details</legend>

		<table class="formtable">
			<tr>
				<td class="formlabel">Supplier Name:</td>
				<td>
					<input type="text" name="supplier_name" id="supplier_name"  maxlength="100"><span class="star">*</span>
				</td>
				<td class="formlabel">Status:</td>
				<td><insta:selectoptions name="status" id="status" value="A" opvalues="A,I" optexts="Active,Inactive" /></td>
				<td class="formlabel">Supplier Address:</td>
				<td>
					<textarea name="supplier_address" cols="20" rows="2"></textarea>
				</td>
			</tr>
			<tr>
				<td class="formlabel">Supplier Phone1:</td>
				<td>
					<input type="text" name="supplier_phone1" id="supplier_phone1" maxlength="20">
				</td>
				<td class="formlabel">Supplier Phone2:</td>
				<td>
					<input type="text" name="supplier_phone2" id="supplier_phone2"  maxlength="20">
				</td>
				<td class="formlabel">Supplier Fax:</td>
				<td>
					<input type="text" name="supplier_fax" id="supplier_fax"  maxlength="20">
				</td>
			</tr>
			<tr>
				<td class="formlabel">Supplier Mail:</td>
				<td>
					<input type="text" name="supplier_mailid" id="supplier_mailid"  maxlength="100">
				</td>
				<td class="formlabel">Supplier Website:</td>
				<td>
					<input type="text" name="supplier_website" id="supplier_website"  maxlength="100">
				</td>
				<td class="formlabel">Contact Person Name:</td>
				<td>
					<input type="text" name="contact_person_name" id="contact_person_name"  maxlength="100">
				</td>
			</tr>
			<tr>
				<td class="formlabel">Contact Person Mobile:</td>
				<td>
					<input type="text" name="contact_person_mobile_number" id="contact_person_mobile_number"  maxlength="100">
				</td>
				<td class="formlabel">Contact Person Mail:</td>
				<td>
					<input type="text" name="contact_person_mailid" id="contact_person_mailid" maxlength="100">
				</td>
			</tr>
		</table>

	</fieldset>

	<div class="screenActions">
		<button type="submit" accesskey="S" onclick="return validate();"><b><u>S</u></b>ave</button>
		| <a href="list.htm?sortOrder=supplier_name&sortReverse=false&status=A">Supplier List</a>
	</div>
</form>

</body>
</html>

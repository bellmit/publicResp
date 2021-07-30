<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Add/Edit Dental Supplies - Insta HMS</title>
	<script>
	var chkItemNames = ${ifn:convertListToJson(itemNames)};
	var supplierItemsRateList = ${ifn:convertListToJson(supplierItemsRateList)};
	var backupName = '';

	function validate() {
		document.getElementById('item_name').value = document.getElementById('item_name').value.trim();
		if (document.getElementById('item_name').value == '') {
			alert('Please enter item name');
			document.getElementById('item_name').focus();
			return false;
		}
		
		if (!checkDuplicate()) return false;

		return true;
	}
	function checkDuplicate() {
		var newItemName = trimAll(document.dentalSuppliesMaster.item_name.value);
		for(var i=0;i<chkItemNames.length;i++){
			item = chkItemNames[i];
			if (newItemName == item.item_name){
				alert(document.dentalSuppliesMaster.item_name.value+" already exists pls enter other name...");
		    	document.dentalSuppliesMaster.item_name.value='';
		    	document.dentalSuppliesMaster.item_name.focus();
		    	return false;
			}
		}
		return true;
	}
	</script>
	<insta:link type="script" file="hmsvalidation.js"/>
</head>
<body class="yui-skin-sam">
	<h1>Add Dental Supplies </h1>
	<insta:feedback-panel/>
	<form action="create.htm" method="POST" name="dentalSuppliesMaster">
		<fieldset class="fieldSetBorder">
		<legend class="fieldSetLabel">Dental Supplies Details</legend>
			<table class="formtable">
				<tr>
					<td class="formlabel">Item Name: </td>
					<td><input type="text" name="item_name" id="item_name" class="required" title="Item Name is mandatory."><span class="star">*</span></td>
					<td>&nbsp</td>
					<td>&nbsp</td>
					<td>&nbsp</td>
				</tr>
				<tr>
					<td class="formlabel">Status: </td>
					<td><Select name="status" id="status" class="dropdown validate-not-empty" title="Status is mandatory.">
							<option value="">-- Select --</option>
							<option value="A" selected>Active</option>
							<option value="I" >Inactive</option>
						</Select><span class="star">*</span>
					</td>
					<td>&nbsp</td>
					<td>&nbsp</td>
					<td>&nbsp</td>
				</tr>
			</table>
		</fieldset>
		<table style="margin-top: 10px">
				<tr>
					<td>
						<input type="submit" name="Save" value="Save" onclick="return validate();"/>
						| <a href="list.htm?sortOrder=item_name&sortReverse=false&status=A">Back To DashBoard</a>
					</td>
				</tr>
			</table>
	</form>
</body>
</html>
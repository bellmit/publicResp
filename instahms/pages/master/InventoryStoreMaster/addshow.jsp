<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Add Store - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>
	<script>
		var backupName = '';

		function keepBackUp(){
			if(document.forms[0]._method.value == 'update'){
					backupName = document.forms[0].store_name.value;
			}
		}

		function doClose() {
			window.location.href = "${cpath}/master/InventoryStoreMaster.do?_method=list";
		}
		function focus(){
			document.forms[0].store_name.focus();
		}
	</script>
</head>
<body onload="focus(); keepBackUp();">

<form action="InventoryStoreMaster.do" method="POST">
	<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}">
	<c:if test="${param._method == 'show'}">
		<input type="hidden" name="store_id" value="${bean.map.store_id}"/>
	</c:if>

	<h1>${param._method == 'add' ? 'Add' : 'Edit'} Inventory Store</h1>
	<insta:feedback-panel/>

	<fieldset class="fieldSetBorder">
	<table class="formtable" >
		<tr>
			<td class="formlabel">Store:</td>
			<td>
				 <input type="text" name="store_name" value="${bean.map.store_name}" onblur="capWords(store_name)" class="required validate-length" length="100" title="Name is required and max length of name can be 100" />
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
			</td>
		</tr>

		<tr>
			<td class="formlabel">Account Group:</td>
			<td>
				<insta:selectdb name="account_group" value="${bean.map.account_group}" table="account_group_master"
						valuecol="account_group_id" displaycol="account_group_name" />
			</td>
		</tr>

		<tr>
			<td class="formlabel">Status</td>
			<td><insta:selectoptions name="status" value="${bean.map.status}" opvalues="A,I" optexts="Active,Inactive" /></td>
		</tr>

	</table>
	</fieldset>

	<table class="screenActions">
		<tr>
			<td><input type="submit" value="Save"/></td>
			<td>|</td>
			<td><a href="javascript:void(0)" onclick="doClose();">Stores List</a></td>
		</tr>
	</table>

</form>

</body>
</html>

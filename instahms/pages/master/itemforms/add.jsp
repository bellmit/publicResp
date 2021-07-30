<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@page import="com.insta.hms.master.URLRoute"%>
<html>
<head>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="pagePath" value="<%=URLRoute.ITEM_FORM_PATH %>"/>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Add Item Form - Insta HMS</title>
<script>
	function validate() {
		var itemForm = document.ItemForm.item_form_name.value;
		if (itemForm == '') {
			alert("Please enter an Item Form Name.");
			document.ItemForm.item_form_name.focus();
			return false;
		}
		return true;
	}
</script>


</head>
<body >

<form action="create.htm" method="POST" name="ItemForm" >
	<h1>Add Item Form</h1>
	<insta:feedback-panel/>
	<fieldset class="fieldsetborder">

		<table class="formtable">
			<tr>
				<td class="formlabel">Item Form Name:</td>
				<td>
					<input type="text" name="item_form_name" id="item_form_name" value="${bean.map.item_form_name}" />
				</td>
				<td class="formlabel">&nbsp;</td>
				<td>&nbsp;</td>
				<td class="formlabel">&nbsp;</td>
				<td>&nbsp;</td>
			</tr>
			<tr>
				<td class="formlabel">Granular Units Applicable:</td>
				<td><insta:selectoptions name="granular_units" value="${bean.map.granular_units}" opvalues="Y,N" optexts="Yes,No" /></td>
				<td class="formlabel">&nbsp;</td>
				<td>&nbsp;</td>
				<td class="formlabel">&nbsp;</td>
				<td>&nbsp;</td>
			</tr>
			<tr>
				<td class="formlabel">Status:</td>
				<td><insta:selectoptions name="status" value="${bean.map.status}" opvalues="A,I" optexts="Active,Inactive" /></td>
				<td class="formlabel">&nbsp;</td>
				<td>&nbsp;</td>
				<td class="formlabel">&nbsp;</td>
				<td>&nbsp;</td>
			</tr>
		</table>

	</fieldset>

	<div class="screenActions">
		<button type="submit" accesskey="S" onclick="return validate();"><b><u>S</u></b>ave</button>
		<c:url var="dashboardUrl" value="${pagePath}/list.htm">
			<c:param name="sortOrder" value="item_form_name"/>
			<c:param name="sortReverse" value="false"/>
		</c:url>
		| <a href="${dashboardUrl}" title="Item Forms List">Item Forms List</a>
	</div>
</form>

</body>
</html>

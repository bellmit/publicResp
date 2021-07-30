<%@page import="com.insta.hms.master.URLRoute"%>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="pagepath" value="<%= URLRoute.ICD_CODES_PATH %>" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Add Codes - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>
<script>
	function focus(){
		document.forms[0].code.focus();
	}

	function validateInput(){
		var code = document.getElementById('code');
		var code_desc = document.getElementById('code_desc');
		var code_type = document.getElementById('code_type');
		if(empty(code.value) || code.value.length > 100){
			alert("Code is required and max length of Code can be 100");
			code.focus();
			return false;
		}
		if(empty(code_desc.value)){
			alert("Code Description is required");
			code_desc.focus();
			return false;
		}
		if(code_type.selectedIndex == 0){
			alert("Please Select Code Type");
			code_type.focus();
			return false;
		}
		return true;
	}
</script>
</head>

<body onload="focus();">
<form action="create.htm" method="POST">

	<div class="pageHeader">Add Codes </div>

	<insta:feedback-panel/>

	<fieldset class="fieldSetBorder">
	<table class="formtable">
		<tr>
			<td class="formlabel">Code:</td>
			<td>
				<input type="text" name="code"  value="${bean.code}" id="code"
					maxlength="100"  />
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
			</td>
		</tr>
		<tr>
			<td class="formlabel">Code Description:</td>
			<td colspan="5">
				<input type="text" name="code_desc" style="width: 100%" value="${bean.code_desc}" id="code_desc"  size="60" />
			</td>
		</tr>
		<tr>
			<td class="formlabel">Code Type:</td>
			<td>
					<insta:selectdb name="code_type" value="${bean.code_type}" table="mrd_supported_code_types" id="code_type"
					valuecol="code_type" displaycol="code_type" dummyvalue="..Select.." dummyvalueId=""/>
			</td>
		</tr>
		<tr>
			<td class="formlabel">Status</td>
			<td><insta:selectoptions name="status" value="${bean.status}" opvalues="A,I" optexts="Active,Inactive" /></td>
		</tr>

	</table>
	</fieldset>

	<div class="screenActions">
		<button type="submit" accesskey="S" onclick="return validateInput();"><b><u>S</u></b>ave</button>
		|
		<a href="${cpath}${pagepath}.htm?sortOrder=code&sortReverse=false&status=A">Codes List</a>
	</div>

</form>

</body>
</html>

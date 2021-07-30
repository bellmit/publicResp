<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page import="com.insta.hms.master.URLRoute" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>

<html>
<head>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="pagePath" value="<%=URLRoute.BILL_LABEL_PATH %>"/>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Add/Edit Bill Label - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>

<script>
	var backupName = '';
	var namesList = ${ifn:convertListToJson(namesList)};

	function keepBackUp(){
		if(document.forms[0]._method.value == 'update'){
				backupName = document.forms[0].bill_label_name.value;
		}
	}

	function doClose() {
		window.location.href = "${cpath}/${pagePath}/list.htm?_method=list&sortOrder=bill_label_name&sortReverse=false&status=A";
	}

	function checkDuplicates() {
		document.getElementById('bill_label_name').value = trim(document.getElementById('bill_label_name').value);
		var status = document.getElementById('status').value;
		var method = document.getElementById('method').value;
		var name = document.getElementById('bill_label_name').value;
		var id = document.billLabelForm.bill_label_id.value;
		if (name == '') {
			alert("Please enter label name.");
			document.getElementById('bill_label_name').focus();
			return false;
		}
		if (id == 0 && status == 'I' && method == 'update') {
			alert(name+" can not be inactivated");
			return false;
		}
			for (var i = 0; i<namesList.length; i++) {
				var dbName = namesList[i].bill_label_name;
				var dbId = namesList[i].bill_label_id;
				if (id != dbId) {
					if (name == dbName) {
						alert("Bill label name already exists, Please enter another name.");
						document.getElementById('bill_label_name').focus();
						return false;
					}
				}
			}
		return true;
	}

</script>

</head>
<body onload="keepBackUp();">
<c:set var="actionUrl" value="${cpath}/${pagePath}/create.htm"/>
<form action="${actionUrl}" method="POST" name="billLabelForm">
	<input type="hidden" name="_method" id="method" value="create">
	<input type="hidden" name="bill_label_id" value="${bean.bill_label_id}"/>

	<h1>Add Bill Label</h1>
	<insta:feedback-panel/>
	<fieldset class="fieldsetborder">

		<table class="formtable">
			<tr>
				<td class="formlabel">Bill Label Name:</td>
				<td>
					<input type="text" name="bill_label_name" id="bill_label_name" value="${bean.bill_label_name}" length="100" maxlength="50"
						title="Name is required and max length of name can be 50"  />
				</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
			</tr>
			<tr>
				<td class="formlabel">Highlight:</td>
				<td><insta:selectoptions name="highlight" value="${bean.highlight}" opvalues="Y,N" optexts="Yes,No" /></td>
			</tr>
			<tr>
				<td class="formlabel">Alert:</td>
				<td><insta:selectoptions name="alert" value="${bean.alert}" opvalues="N,Y" optexts="No,Yes" /></td>
			</tr>
			<tr>
				<td class="formlabel">Remarks Required:</td>
				<td><insta:selectoptions name="remarks_reqd" value="${bean.remarks_reqd}" opvalues="Y,N" optexts="Yes,No" /></td>
			</tr>

			<tr>
				<td class="formlabel">Status:</td>
				<td><insta:selectoptions name="status" id="status" value="${bean.status}" opvalues="A,I" optexts="Active,Inactive" /></td>
			</tr>
		</table>

	</fieldset>

	<div class="screenActions">
		<button type="submit" accesskey="S" onclick="return checkDuplicates();"><b><u>S</u></b>ave</button>
		| <a href="javascript:void(0)" onclick="doClose();">Bill Label List</a>
	</div>
</form>

</body>
</html>

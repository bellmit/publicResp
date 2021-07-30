<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<meta HTTP-EQUIV="Pragma" content="no-cache">
<meta HTTP-EQUIV="Expires" content="-1">
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<title>Print Template (Mode Select) - Insta HMS</title>
<script>
	function  validateForm() {
		var mode = document.printTemplate.templateMode;
		var modeSelected = false;
		for (var i=0;i<mode.length;i++) {
			if (mode[i].checked) {
				modeSelected = true;
				break;
			}
		}
		if (!modeSelected) {
			alert("Select Template Mode");
			return false;
		}
		if (document.printTemplate.template_type.value == '') {
			alert("Please select the Template Type");
			document.printTemplate.template_type.focus();
			return false;
		}
		return true;
	}

	function reloadIfBackBtnPressed() {
		var e=document.getElementById("refreshed");
		if (e.value=="no")
			e.value="yes";
		else {
			e.value="no";
			window.location.reload();
		}
	}
</script>
</head>
<body onload="reloadIfBackBtnPressed()">

<h1>Print Template</h1>
<form name="printTemplate" method="POST" action="CommonPrintTemplates.do">
	<input type="hidden" name="_method" value="add">
	<input type="hidden" id="refreshed" value="no">
	<fieldset class="fieldSetBorder">
		<table class="formtable">
			<tr>
				<td class="formLabel">Select Template Mode:</td>
				<td style="white-space:nowrap"><input type="radio" name="templateMode" id="textTemplate" value="T">Text Mode:
												<input type="radio" name="templateMode" id="htmlTemplate" value="H">HTML Mode:
				</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
			</tr>
			<tr>
				<td class="formlabel">Template Type: </td>
				<td>
					<select class="dropdown" name="template_type">
						<option value="">-- Select --</option>
						<c:forEach items="${template_types}" var="template_type">
							<option value="${template_type.type}">${template_type.type}</option>
						</c:forEach>
					</select>
				</td>
			</tr>
		</table>
	</fieldset>

	<table class="screenActions">
		<tr>
			<td><input type="submit" name="next"value="Next" id="next" onclick="return validateForm();"></td>
			<td>&nbsp;|&nbsp;</td>
			<td><a href="${cpath}/master/CommonPrintTemplates.do?_method=list">Print Template List</td>
		</tr>
	</table>
</form>
</body>
</html>
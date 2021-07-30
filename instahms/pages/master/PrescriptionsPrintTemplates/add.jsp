<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Consultation Print Template (Mode Select) - Insta HMS</title>
<script>
	function  validateForm() {
		var mode = document.PrescriptionPrintTemplate.templateMode;
		for (var i=0;i<mode.length;i++) {
			if (mode[i].checked) {
				return true;
			}
		}
		alert("Select Template Mode");
		return false;
	}

	function closeTemplate() {
		document.PrescriptionPrintTemplate.method.value = 'list';
		document.PrescriptionPrintTemplate.submit();
	}
</script>
</head>
<body>

<h1>Consultation Print Template</h1>
<form name="PrescriptionPrintTemplate" method="POST" action="PrescriptionsPrintTemplates.do">
	<input type="hidden" name="method" value="add">
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
		</table>
	</fieldset>

	<table class="screenActions">
		<tr>
			<td><input type="submit" name="next"value="Next" id="next" onclick="return validateForm();"></td>
			<td>&nbsp;|&nbsp;</td>
			<td><a href="javascript:void(0)" onclick="closeTemplate();">Consulation Print List</td>
		</tr>
	</table>

</body>
</html>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@taglib  uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>Receipt/Refund Print Template - Insta HMS</title>
<insta:link type="js" file="tiny_mce/tiny_mce.js"/>
<insta:link type="js" file="editor.js" />

<script>

function validateForm(){
	var mode = document.forms[0].templateMode;
	for (i=0;i<mode.length;i++){
		if(mode[i].checked){
			document.forms[0].template_mode.value=mode[i].value;
			return true;
		}
	}
	alert("Select template mode");
	return false;
}

function funclose(){
	document.forms[0].method.value='list';
	document.forms[0].submit();
}

</script>

</head>

<body>
<div class="pageHeader">Receipt/Refund Print Template</div>
<span align="center" class="error">${ifn:cleanHtml(error)}</span>
<span align="center"><b>${ifn:cleanHtml(msg)}</b></span>

<form method="POST" action="ReceiptRefundPrintTemplate.do">
	<input type="hidden" name="method" value="add"/>
	<input type="hidden" name="template_mode" value=""/>
	<div>
	<fieldset class="fieldSetBorder">
		<table class="formtable">
		<tr>
			<td class="formlabel">Select Template Mode :</td>
			<td style="white-space:nowrap"><input type="radio" name="templateMode" id="textTemplate"  value="T" />Text Mode
				<input type="radio" name="templateMode" id="htmlTemplate"  value="H" />HTML Mode
			</td>
			<td>&nbsp;</td>
			<td>&nbsp;</td>
			<td>&nbsp;</td>
			<td>&nbsp;</td>
		</tr>
		</table>
	</fieldset>
		<table class="screenActions">
		<tr>
			<td><input type="submit" name="next" value="Next" id="next" onclick="return validateForm()";/></td>
			<td>&nbsp;|&nbsp;<a href="javascript:void(0)"onclick="funclose();">Receipt/Refund Print List</a></td>
		</tr>
		</table>

	</div>
</form>

</body>
</html>


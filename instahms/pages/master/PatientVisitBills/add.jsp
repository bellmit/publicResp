<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@taglib  uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>


<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
	<title>Patient Visit Bills Print Template - Insta HMS</title>

	<insta:link type="js" file="tiny_mce/tiny_mce.js"/>
	<insta:link type="js" file="editor.js" />

	<script>

	function validateForm(){
		var mode = document.mainform.templateMode;
		for (i=0;i<mode.length;i++){
			if(mode[i].checked){
				return true;
			}
		}
		alert("Select template mode");
		return false;
	}

	function closeTemplate(){
		document.mainform.method.value='list';
		document.mainform.submit();
	}
	</script>
</head>

<body>
	<h1>Patient Visit Bills Print Template</h1>

	<form method="POST"  name="mainform" action="PatientVisitBillsPrintTemplate.do">

		<input type="hidden" name="method" value="add"/>

		<fieldset class="fieldSetBorder">
			<table class="formtable">
				<tr>
					<td class="formlabel">Select Template Mode :</td>
					<td style="white-space:nowrap"><input type="radio" name="templateMode" id="textTemplate"  value="T" /> Text Mode
						<input type="radio" name="templateMode" id="htmlTemplate"  value="H" />HTML Mode
					</td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
					</tr>
			</table>
		</fieldset>
		<table class="screenActions">
			<tr>				<td><input type="submit" name="next" value="Next" id="next" onclick="return validateForm()";/></td>
				<td>&nbsp;|&nbsp;</td>
				<td><a href="javascript:void(0)" onclick="closeTemplate();">Patient Visit Bills Print List</a></td>
			</tr>
		</table>
	</form>

</body>
</html>


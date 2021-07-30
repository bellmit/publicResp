<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Consumption UOM - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>
	<script>

		function doClose() {
			window.location.href = "${cpath}/master/consumptionuom.htm?sortOrder=consumption_uom" +
						"&sortReverse=false";
		}
		function focus() {
			document.consumptionuomform.consumption_uom.focus();
		}

	</script>
</head>
<body>
<h1>Add Consumption UOM </h1>
<form action="create.htm"  name="consumptionuomform" method="POST">
	<insta:feedback-panel/>
	<fieldset class="fieldSetBorder">
	<table class="formtable" >
		<tr>
			<td class="formlabel">Consumption UOM:</td>
			<td>
				<input type="text" name="consumption_uom" value="${bean.consumption_uom}" onblur="capWords(consumption_uom)"
						class="required validate-length" length="30"
						title="Consumption UOM is required and max length of name can be 30" />
			</td>
			<td class="formlabel">Status:</td>
			<td>
				<insta:selectoptions name="status" value="${bean.status}" opvalues="A,I" optexts="Active,Inactive"/>
			</td>
			<td>&nbsp;</td>
			<td>&nbsp;</td>
		</tr>
	</table>
	</fieldset>

	<div class="screenActions">
		<button type="submit" accesskey="S"><b><u>S</u></b>ave</button>
		|<a href="javascript:void(0)" onclick="doClose();">Consumption UOM List</a>
	</div>

</form>
</body>
</html>

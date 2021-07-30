<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Store Item Control Type- Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>
	<script>

		function doClose() {
			window.location.href = "${cpath}/master/storeitemcontroltypes.htm?sortOrder=control_type_name" +
						"&sortReverse=false";
		}
		function focus() {
			document.controltypemasterform.control_type_name.focus();
		}

	</script>
</head>
<body>
<h1>Add Control Type </h1>
<form action="create.htm"  name="controltypemasterform" method="POST">
	<insta:feedback-panel/>
	<fieldset class="fieldSetBorder">
	<table class="formtable" >
		<tr>
			<td class="formlabel">Control Type Name:</td>
				<td>
					<input type="text" name="control_type_name" value="${bean.control_type_name}" onblur="capWords(control_type_name)"
						class="required validate-length" length="100"
						title="Name is required and max length of name can be 100" />
				</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
		</tr>
	</table>
	</fieldset>

	<div class="screenActions">
		<button type="submit" accesskey="S"><b><u>S</u></b>ave</button>
		|<a href="javascript:void(0)" onclick="doClose();">Control Type List</a>
	</div>

</form>
</body>
</html>

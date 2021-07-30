<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Title - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>
	<script>
		function doClose() {
			window.location.href = "${cpath}/master/SalutationMaster.do?_method=list&sortOrder=salutation&sortReverse=false&status=A";
		}
	</script>

</head>
<body>

<form action="SalutationMaster.do" method="POST">
	<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}">
	<c:if test="${param._method == 'show'}">
		<input type="hidden" name="salutation_id" value="${bean.map.salutation_id}"/>
	</c:if>

	<h1>${param._method == 'add' ? 'Add' : 'Edit'} Salutation</h1>

	<insta:feedback-panel/>

	<fieldset class="fieldsetborder">

		<table class="formtable">
			<tr>
				<td class="formlabel">Salutation:</td>
				<td>
					<input type="text" name="salutation" value="${bean.map.salutation}" onblur="capWords(salutation)" class="required validate-length" length="100" title="Name is required and max length of name can be 100" />
				</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
			</tr>

			<tr>
				<td class="formlabel">Status:</td>
				<td><insta:selectoptions name="status" value="${bean.map.status}" opvalues="A,I" optexts="Active,Inactive" /></td>
			</tr>

			<tr>
				<td class="formlabel">Gender:</td>
				<td><insta:selectoptions name="gender" value="${bean.map.gender}" opvalues="N,M,F" optexts="...Select ...,Male,Female" /></td>
			</tr>

		</table>

	</fieldset>

		<table class="screenActions">
		<tr>
			<td><button type="submit" accesskey="S"><b><u>S</u></b>ave</button></td>
			<td>&nbsp;|&nbsp;</td>
			<td><a href="javascript:void(0)" onclick="window.location.href='${cpath}/master/SalutationMaster.do?_method=add'">Add</a></td>
			<td>&nbsp;|&nbsp;</td>
			<td><a href="javascript:void(0)" onclick="doClose();">Salutation List</a></td>
		</tr>
	</table>

</form>

</body>
</html>

<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title> ${custom_type eq 'V' ? 'Visit' : 'Patient'} Custom List ${custom_list}- Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>

<c:if test="${not empty custom_list && not empty custom_type}">
	<c:choose>
		<c:when test="${custom_type eq 'P'}">
			<c:set var="urlpath"> CustomMaster${custom_list}.do</c:set>
		</c:when>
		<c:otherwise>
			<c:set var="urlpath"> VisitCustomMaster${custom_list}.do</c:set>
		</c:otherwise>
	</c:choose>
</c:if>

<script>
	function doClose() {
		window.location.href = "${cpath}/master/${urlpath}?_method=list&sortOrder=custom_value&sortReverse=false&status=A";
	}
	function focus(){
		document.forms[0].custom_value.focus();
	}
	function trimValues() {
		document.forms[0].custom_value.value = trim(document.forms[0].custom_value.value);
		return true;
	}
</script>

</head>

<body onload="focus()">

<form action="${urlpath}" method="POST">
	<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}">
	<c:if test="${param._method == 'show'}">
		<input type="hidden" name="old_custom_value" value='<c:out value="${bean.map.custom_value}"/>'/>
	</c:if>

	<h1>${param._method == 'add' ? 'Add' : 'Edit'} ${custom_type eq 'V' ? 'Visit' : 'Patient'} Custom List ${custom_list}</h1>
	<insta:feedback-panel/>
	<fieldset class="fieldsetborder">

		<table class="formtable">
			<tr>
				<td class="formlabel">Custom Value:</td>
				<td>
					<input type="text" name="custom_value" value='<c:out value="${bean.map.custom_value}"/>'
						onblur="capWords(custom_value)" class="required validate-length"
						length="50" title="Name is required and max length of name can be 50" />
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
		</table>

	</fieldset>

	<table class="screenActions">
		<tr>
			<td><button type="submit" accesskey="S" onclick="return trimValues();"><b><u>S</u></b>ave</button></td>
			<td>&nbsp;|&nbsp;</td>
			<td><a href="javascript:void(0)" onclick="window.location.href='${cpath}/master/${urlpath}?_method=add'">Add</a></td>
			<td>&nbsp;|&nbsp;</td>
			<td><a href="javascript:void(0)" onclick="doClose();">${custom_type eq 'V' ? 'Visit' : 'Patient'} Custom List ${custom_list}</a></td>
		</tr>
	</table>
</form>

</body>
</html>

<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Edit Purpose Master - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>

</head>
<body>

<c:set var="actionUrl" value="${cpath}/master/fpVerificationPurpose/update.htm?purpose_id=${bean.purpose_id}"/>
<form action="${actionUrl}" method="POST">
		<input type="hidden" name="purpose_id" value="${bean.purpose_id}"/>

	<div class="pageHeader">Edit Purpose</div>
	<insta:feedback-panel/>
	<fieldset class="fieldsetborder">

		<table class="formtable">

			<tr>
				<td class="formlabel">Purpose:</td>
				<td>
					<input type="text" name="purpose" value="${bean.purpose}" onblur="capWords(purpose);" class="required validate-length" length="50" title="Purpose is required and max length of purpose can be 50" />
					<span class="star">*</span>
				</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
			</tr>

			<tr>
				<td class="formlabel">Status</td>
				<td><insta:selectoptions name="status" value="${bean.status}" opvalues="A,I" optexts="Active,Inactive" /></td>
			</tr>

		</table>

	</fieldset>

		<table class="screenActions">
		<tr>
			<td><button type="submit" accesskey="S" ><b><u>S</u></b>ave</button></td>
			<td>&nbsp;|&nbsp;</td>
			<td><a href="${cpath}/master/fpVerificationPurpose/add.htm">Add</a>
			<td>&nbsp;|&nbsp;</td>
			<td><a href="${cpath}/master/fpVerificationPurpose/list.htm">Purpose List</a></td>
		</tr>
	</table>

</form>

</body>
</html>

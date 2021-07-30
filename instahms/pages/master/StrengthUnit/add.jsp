<%@page import="com.insta.hms.master.URLRoute"%>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<html>
<head>
	<c:set var="cpath" value="${pageContext.request.contextPath}" />
	<c:set var="pagepath" value="<%= URLRoute.STRENGTH_UNIT_MASTER %>" />
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>

	<title>Add Strength Unit Master - Insta HMS</title>
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="js" file="/masters/strengthUnit_add.js" />

	<script>
		var chkStrengthUnit = ${ifn:convertListToJson(strengthUnitsList)};

	</script>

</head>
<body>

	<form action="create.htm" method="POST" name="strengthUnitMaster">

		<h1>Add Strength Unit </h1>
		<insta:feedback-panel/>
		<fieldset class="fieldsetborder">
			<legend class="fieldsetlabel">Strength Unit Details</legend>

			<table class="formtable">
				<tr>
					<td class="formlabel">Unit Name :</td>
					<td>
						<input type="text" name="unit_name" id="unit_name" value="" maxlength="10" class="required" title="Unit Name is mandatory."><span class="star">*</span>
					</td>
					<td/>
					<td/>
					<td/>
				</tr>
				<tr>
					<td class="formlabel">Status:</td>
					<td><insta:selectoptions name="status" id="status" value="" opvalues="A,I" optexts="Active,Inactive" /></td>
					<td/>
					<td/>
					<td/>
				</tr>
			</table>
		</fieldset>

		<div class="screenActions">
			<button type="submit" accesskey="S" onclick="return validate();"><b><u>S</u></b>ave</button>
			| <a href="list.htm?sortOrder=unit_id&sortReverse=false&status=A">Strength Unit List</a>
		</div>
	</form>

</body>
</html>

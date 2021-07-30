<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="itemname" value="${param._type}"/>

<html>
<head>
	<title>Add Prescriptions Medicine Master - Insta HMS</title>

<script>

	function doValidation() {
		var medicineName = document.getElementById('medicine_name');
		if (medicineName.value == null || medicineName.value == '') {
			alert("Enter item name ");
			medicineName.focus();
			return false;
		}

		return true;
	}
</script>

</head>

<body>
	<h1>${param._method == 'add' ? 'Add' : 'Edit'} Medicine</h1>
	<insta:feedback-panel />
	<form action="PrescriptionsMaster.do" method="POST">
	<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}"/>
	<input type="hidden" name="dbName" value="${bean.map['medicine_name']}" />
	<fieldset class="fieldSetBorder">
		<table class="formtable" align="center">
			<tr>
				<td class="formlabel">Medicine Name : </td>
				<td><input type="text" name="medicine_name" id="medicine_name"
						maxlength="50" class="required" title="Medicine Name is mandatory." value="${bean.map['medicine_name']}"/>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
			</tr>
			<tr>
				<td class="formlabel">Item Form: </td>
				<td><insta:selectdb name="item_form_id" id="item_form_id" table="item_form_master" value="${bean.map.item_form_id}"
					displaycol="item_form_name" valuecol="item_form_id" filtercol="status" filtervalue="A" orderby="item_form_name"
					dummyvalue="-- Select --" dummyvalueId=""/></td>
			</tr>
			<tr>
				<td class="formlabel">Strength: </td>
				<td><input type="text" name="item_strength" id="item_strength" maxlength="50" value="${bean.map.item_strength}"></td>
			</tr>
			<tr>
				<td class="formlabel">Strength Units: </td>
				<td><insta:selectdb name="item_strength_units" id="item_strength_units" table="strength_units" displaycol="unit_name" valuecol="unit_id"
					dummyvalue="-- Select --" value="${bean.map.item_strength_units}"/>
				</td>
			</tr>
			<tr>
				<td class="formlabel">Route: </td>
				<td><select name="route_of_admin" id="route_of_admin" size="5" multiple="multiple">
						<c:forEach var="route" items="${medicineRouteList}">
							<c:set var="selected" value=""/>
							<c:forTokens delims="," items="${bean.map.route_of_admin}" var="routeId" >
								<c:if test="${route.map.route_id == routeId}">
									<c:set var="selected" value="selected"/>
								</c:if>
							</c:forTokens>
							<option value="${route.map.route_id}" ${selected} title="${route.map.route_name}">${route.map.route_name}</option>
						</c:forEach>
					</select>
				</td>
			</tr>

		</table>
		</fieldset>
		<div class="screenActions">

			<c:url var="dashboardUrl" value="PrescriptionsMaster.do">
				<c:param name="_method" value="list"/>
			</c:url>
				<button type="submit" accesskey="S" onclick="return doValidation();"><b><u>S</u></b>ave</button>
					|
				<a href="javascript:void(0)" onclick="return gotoLocation('${dashboardUrl}');">Medicines List</a>
		</div>
	</form>
</body>
</html>
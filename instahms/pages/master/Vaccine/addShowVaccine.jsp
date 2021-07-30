<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<html>
<head>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>Add/Edit Vaccine - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js" />

<script>
	var vaccineList =
<%=request.getAttribute("vaccineList")%>
	var backupName = '';
	var actionMethod = '${ifn:cleanJavaScript(param._method)}';
	var dosage = '${bean.map.single_dose}';
	var existingStatus = '${bean.map.status}';

	function keepBackUp() {
		if (document.forms[0]._method.value == 'update') {
			backupName = document.forms[0].vaccine_name.value;
		}
	}

	function doClose() {
		window.location.href = "${cpath}/master/vaccineMaster.do?_method=list&sortOrder=display_order&sortReverse=false&status=A";
	}

	function checkduplicate() {
		var newVaccineName = trimAll(document.forms[0].vaccine_name.value);
		var hiddenVaccineId = document.forms[0].vaccine_id.value;
		for (var i = 0; i < vaccineList.length; i++) {
			item = vaccineList[i];
			if (hiddenVaccineId != item.VACCINE_ID) {
				var actualVaccineName = item.VACCINE_NAME;
				if (newVaccineName.toLowerCase() == actualVaccineName
						.toLowerCase()) {
					alert(document.forms[0].vaccine_name.value
							+ " already exists please enter other name");
					document.forms[0].vaccine_name.value = '';
					document.forms[0].vaccine_name.focus();
					return false;
				}
			}
		}

		if (actionMethod != 'add') {
			var radioList = document.getElementsByName('single_dose');
			var status = document.getElementById('status').value;
			for (var i = 0; i < radioList.length; i++) {
				if (radioList[i].checked) {
					if (dosage != radioList[i].value) {
						alert('You can not modify single dose to multiple dose or multiple dose to single.');
						return false;
					}
				}
			}

			if (existingStatus == 'I' && status == 'A') {
				alert('You can not change status from inactive to active state.');
				return false;
			}
		}

	}
</script>

</head>
<body onload="keepBackUp();">

	<form action="vaccineMaster.do" method="POST" name="vaccineMaster">
		<input type="hidden" name="_method"
			value="${param._method == 'add' ? 'create' : 'update'}"> <input
			type="hidden" name="vaccine_id" value="${bean.map.vaccine_id}" />

		<h1>${param._method == 'add' ? 'Add' : 'Edit'}Vaccine</h1>
		<insta:feedback-panel />
		<fieldset class="fieldsetborder">

			<table class="formtable">
				<tr>
					<td class="formlabel">Vaccine Name:</td>
					<td><input type="text" name="vaccine_name"
						value="${bean.map.vaccine_name}" class="required validate-length"
						length="500"
						title="Name is required and max length of name can be 500" /></td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
				</tr>
				<tr>
					<td colspan="2" align="right"><input type="radio"
						name="single_dose" value='Y'
						${(bean.map.single_dose eq 'Y' || empty bean.map.single_dose) ? 'checked' : '' } />
						Single Dose <input type="radio" name="single_dose" value='N'
						${bean.map.single_dose eq 'N' ? 'checked' : ''} /> Multiple Doses
					</td>
				</tr>
				<tr>
					<td class="formlabel">Display Order:</td>
					<td><input type="text" class="number" name="display_order"
						id="display_order"
						onkeypress="return enterNumOnlyzeroToNine(event)"
						value="${bean.map.display_order}" /></td>
				</tr>
				<tr>
					<td class="formlabel">Status:</td>
					<td><insta:selectoptions name="status" id="status"
							value="${bean.map.status}" opvalues="A,I"
							optexts="Active,Inactive" /></td>
				</tr>
				<tr>
					<td class="formlabel"><insta:ltext
							key="vaccine.master.vaccineaddedit.vaccination.category" /></td>
					<td><select name="selectedCategories" multiple class="listbox"
						style="width: 230px">
							<c:forEach items="${vaccineCategories}" var="option">
								<c:set var="value" value="${option.get('vaccine_category_id')}" />
								<c:choose>
									<c:when test="${ifn:arrayFind(mappedCategories,value) ne -1}">
										<c:set var="attr" value="selected='true'" />
									</c:when>
									<c:otherwise>
										<c:set var="attr" value="" />
									</c:otherwise>
								</c:choose>
								<option value='<c:out value="${value}"/>' ${attr}>
									${option.get("vaccine_category_name")}</option>
							</c:forEach>
					</select></td>
				</tr>
			</table>

		</fieldset>

		<div class="screenActions">
			<c:url var="listURL" value="vaccineMaster.do">
				<c:param name="_method" value="showDossageDetails" />
				<c:param name="vaccine_id" value="${bean.map.vaccine_id}" />
				<c:param name="status" value="A" />
			</c:url>
			<button type="submit" accesskey="S" onclick="return checkduplicate()">
				<b><u>S</u></b>ave
			</button>
			<c:if test="${param._method=='showVaccineDetails'}">
			| <a href="vaccineMaster.do?_method=add">Add</a>
			| <a href="${listURL}">Dose Details</a>
			</c:if>
			| <a href="javascript:void(0)" onclick="doClose();">Vaccine List</a>
		</div>
	</form>

</body>
</html>

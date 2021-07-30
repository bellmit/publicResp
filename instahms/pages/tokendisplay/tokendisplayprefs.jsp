<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Token Display System Pre-requisites  - Insta HMS</title>
	<insta:link type="js" file="hmsvalidation.js"/>
	<script>
		var avlFlds = null;
		var selFlds = null;

		function swapOptions(obj, i, j) {
			var o = obj.options;
			var i_selected = o[i].selected;
			var j_selected = o[j].selected;
			var temp = new Option(o[i].text, o[i].value, o[i].title, o[i].defaultSelected, o[i].selected);
			temp.setAttribute("title", o[i].title);
			var temp2 = new Option(o[j].text, o[j].value, o[j].title, o[j].defaultSelected, o[j].selected);
			temp2.setAttribute("title", o[j].title);

			o[i] = temp2;
			o[j] = temp;
			o[i].selected = j_selected;
			o[j].selected = i_selected;
		}

		function moveOptionUp(obj) {
			if (!hasOptions(obj)) {
				return;
			}
			for (i = 0; i < obj.options.length; i++) {
				if (obj.options[i].selected) {
					if (i != 0 && !obj.options[i - 1].selected) {
						swapOptions(obj, i, i - 1);
						obj.options[i - 1].selected = true;
					}
				}
			}
		}

		function moveOptionDown(obj) {
			if (!hasOptions(obj)) {
				return;
			}
			for (i = obj.options.length - 1; i >= 0; i--) {
				if (obj.options[i].selected) {
					if (i != (obj.options.length - 1) && !obj.options[i + 1].selected) {
						swapOptions(obj, i, i + 1);
						obj.options[i + 1].selected = true;
					}
				}
			}
		}

		function sortSelect(obj) {
			var o = new Array();
			if (!hasOptions(obj)) {
				return;
			}
			for (var i = 0; i < obj.options.length; i++) {
				o[o.length] = new Option(obj.options[i].text, obj.options[i].value, obj.options[i].defaultSelected, obj.options[i].selected);
				(o[i]).title = obj.options[i].title;
				(o[i]).value = obj.options[i].value;

			}
			if (o.length == 0) {
				return;
			}
			o = o.sort(function(val1, val2) {
				if ((val1.text + "") < (val2.text + "")) {
					return - 1;
				}
				if ((val1.text + "") > (val2.text + "")) {
					return 1;
				}
				return 0;
			});

			for (var i = 0; i < o.length; i++) {
				obj.options[i] = new Option(o[i].text, o[i].defaultSelected, o[i].selected);
				obj.options[i].title = o[i].title;
				obj.options[i].value = o[i].value;
			}
		}

		function hasOptions(obj) {
			if (obj != null && obj.options != null) {
				return true;
			}
			return false;
		}

		function moveSelectedOptions(from, to, sort) {
			if (!hasOptions(from)) {
				return;
			}
			for (var i = 0; i < from.options.length; i++) {
				var o = from.options[i];
				if (o.selected) {
					if (!hasOptions(to)) {
						var index = 0;
					} else {
						var index = to.options.length;
					}
					to.options[index] = new Option(o.text, o.value, o.title, false, false);
					to.options[index].setAttribute("title", o.title);
				}
			}
			// Delete the selected options from  the available list.
			for (var i = (from.options.length - 1); i >= 0; i--) {
				var o = from.options[i];
				if (o.selected) {
					from.options[i] = null;
				}
			}
			//********If needed, the fields in the list can be sorted after addition or deletion.******
			if(from.id=='avlbl_doctor_name'){
				sortSelect(from);
			}else if(to.id=='avlbl_doctor_name'){
				sortSelect(to);
			}
			from.selectedIndex = -1;
			to.selectedIndex = -1;
		}

		function createListElements(from, to) {
			avlFlds = document.getElementById(from);
			selFlds = document.getElementById(to);
		}

		function addListFields() {
			createListElements('avlbl_doctor_name', 'selected_doctors');
			moveSelectedOptions(avlFlds, selFlds, 'from');
		}

		function removeListFields() {
			createListElements('avlbl_doctor_name', 'selected_doctors');
			moveSelectedOptions(selFlds, avlFlds);
		}

		function doSave() {
			if (document.prefsform.selected_doctors.length == 0) {
				alert("Please select doctors to display");
				return false
			}
			if (document.getElementById('interval').value == '' || document.getElementById('interval').value == 0) {
				alert("interval is mandatory and it should be greater than Zero.");
				document.getElementById('interval').focus();
				return false;
			}
			if (document.getElementById('no_of_records').value == '' ||
					document.getElementById('no_of_records').value == 0) {
				alert("No. of tokens per page is mandatory. And it should be greater than Zero.");
				document.getElementById('no_of_records').focus();
				return false;
			}
			for (var i=0; i<document.prefsform.selected_doctors.length; i++) {
				document.prefsform.selected_doctors.options[i].selected = true;
			}

			document.prefsform.submit();
		}

	</script>
</head>
<body>
	<h1>Consultation List Preferences</h1>
	<form action="ConsultationTokenDisplayAction.do" name="prefsform">
		<input type="hidden" name="_method" value="setPrefs"/>
		<input type="hidden" name="doctorNo" value="0"/>
		<input type="hidden" name="pageNumber" value="1"/>
		<fieldset class="fieldSetBorder">
			<table align="center" width="342" style="padding-right:5; padding-left:10px;border-width:0px; margin:0px;">
				<tr>
					<td align="center" style="padding-right: 4pt; border-width:0px; margin:0px; width:134px;">
						Available Doctors
						<br />
						<br />
						<select name="avlbl_doctor_name" id="avlbl_doctor_name" style="width:12em;padding-left:5; color:#666666;"
							multiple="true" size="15" onDblClick="moveSelectedOptions(this,this.form.selected_doctors);">
							<c:forEach items="${doctors}" var="doctor">
								<option value="${doctor.map.doctor_id}">${doctor.map.doctor_name}</option>
							</c:forEach>
						</select>
					</td>
					<td valign="top" align="left" style="padding-right:0;">
						<br />
						<br />
						<input type="button" name="addLstFldsButton" value=">" onclick="addListFields();"/>

					</td>
					<td valign="top" align="center" style="width:134px;padding-left:4pt;">
						Selected Doctors
						<br />
						<br />
						<select  size="15" style="width:12em;padding-left:5; color:#666666;" multiple id="selected_doctors" name="selected_doctors" onDblClick="moveSelectedOptions(this,this.form.avlbl_doctor_name);">
						</select>
					</td>
					<td>
						<div align="center">
							<button type="button" style="border-width:thin;border-style:none; background-color:#FFFFFF;" onclick="moveOptionUp(selected_doctors);"> <img src="${cpath}/icons/std_up.png" width=10 height=8/>  </button>
							<br />
							<br />
							<button type="button" style="border-width:thin;border-style:none; background-color:#FFFFFF;" onclick="moveOptionDown(selected_doctors);"><img src="${cpath}/icons/std_down.png" width=10 height=8/> </button>
							<br />
							<br />
							<br /><br />
							<br /><br />
							<br /><br />
							<br /><br />
							<br/><br/>
						</div>
					</td>
				</tr>
			</table>
			<table class="formtable" width="100%">
				<tr>
					<td class="formlabel">Refresh Interval (in sec): </td>
					<td><input type="text" name="interval" id="interval" onkeypress="return enterNumOnlyzeroToNine(event)"></td>
					<td class="formlabel"></td>
					<td></td>
					<td class="formlabel"></td>
					<td></td>
				</tr>
				<tr>
					<td class="formlabel">Consultations per page: </td>
					<td><input type="text" name="no_of_records" id="no_of_records" onkeypress="return enterNumOnlyzeroToNine(event)"></td>
					<td class="formlabel"></td>
					<td></td>
					<td class="formlabel"></td>
					<td></td>
				</tr>
				<tr>
					<td class="formlabel">Display Patient Name: </td>
					<td><input type="checkbox" name="display_patient_name" value="Y"/></td>
				</tr>
			</table>
			<table style="margin-top: 10px">
				<tr>
					<td>
						<input type="button" name="Save" value="Display List" onclick="doSave()"/>
					</td>
				</tr>
			</table>
		</fieldset>
	</form>
</body>
</html>

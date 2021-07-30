<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<jsp:useBean id="currentDate" class="java.util.Date"/>
<fmt:formatDate var="curDateVal" value="${currentDate}" pattern="dd-MM-yyyy"/>
<fmt:formatDate var="curTimeVal" value="${currentDate}" pattern="HH:mm"/>
<fmt:formatDate var="conductedDt" value="${equipmentCondDetails.map.conducted_on}" pattern="dd-MM-yyyy"/>
<fmt:formatDate var="conductedTm" value="${equipmentCondDetails.map.conducted_on}" pattern="HH:mm"/>
<c:set var="newRecord" value="${empty equipmentCondDetails }"/>
<html>
<head>
	<title>${newRecord ?"Record New " : "Edit " } <insta:ltext key="laboratory.equipmentqualitytest.addedit.title"/></title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<script>
		function validateSave() {
			var complete = document.getElementById('test_record_complete');
			var testValue = document.getElementsByName('test_value');
			var flag = false;
			var j = 0;
			if (testValue != null && complete!= null && complete.checked) {
				for(var i=0;i<testValue.length;i++) {
					if (document.getElementById('test_value'+i).value != '') {
						flag = true;
						break;
					} else {
						flag = false;
					}
				}
				if (!flag) {
					alert('No Results Entered');
					document.getElementById('test_value'+j).focus();
					return false;;
				}
			}
			return true;
		}
	</script>
</head>
<body>

	<div class="pageHeader">${newRecord ?"Record New " : "Edit " }<insta:ltext key="laboratory.equipmentqualitytest.addedit.qualitydata"/> </div>
	<insta:feedback-panel/>
	<form name="equipmentConductedForm" method="POST" action="${category == 'DEP_LAB' ? 'Lab' : 'Rad' }EquipmentQAAction.do">
	<input type="hidden" name="_method" value="${newRecord ? 'recordQuality' : 'editQuality'}"/>
	<input type="hidden" name="equipment_conducted_id" value="${ifn:cleanHtmlAttribute(param.eqConductedId)}"/>

		<div>
			<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel"><insta:ltext key="laboratory.equipmentqualitytest.addedit.equipmentdetails"/></legend>
				<table class="formtable">
					<tr>
						<td class="formlabel"><insta:ltext key="laboratory.equipmentqualitytest.addedit.equipmentname"/>:</td>
						<td class="forminfo">${equipmentDetails.map.equipment_name }</td>
					</tr>
				</table>
			</fieldset>
		</div>

		<div>
			<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel"><insta:ltext key="laboratory.equipmentqualitytest.addedit.qualitytest"/></legend>
				<table class="formtable">
					<tr>
						<td class="formlabel"><insta:ltext key="laboratory.equipmentqualitytest.addedit.conductedby"/>:</td>
						<td>
							<insta:selectdb name="conducted_by" table="u_user" valuecol="emp_username"
								displaycol="emp_username" style="width: 11em"
								value="${equipmentCondDetails.map.conducted_by}" class="noClass" filtered="true"
								filtercol="emp_status,hosp_user" filtervalue="A,Y" orderby="emp_username"/>
						</td>
						<td class="formlabel"><insta:ltext key="laboratory.equipmentqualitytest.addedit.conductedon"/>:</td>
						<td>
							<insta:datewidget name="conducted_on_dt" id="conducted_on_dt"
								value="${newRecord ? curDateVal : conductedDt }" btnPos="left" />
					   		<input type="text" id="conducted_on_tm" name="conducted_on_tm" class="number"
					   			value="${newRecord ? curTimeVal : conductedTm }" onblur="setTime(this)"/>
						</td>
					</tr>
					<tr>
						<td class="formlabel"><insta:ltext key="laboratory.equipmentqualitytest.addedit.sampleinfo"/>:</td>
						<td>
							<input type="text" name="sample_info" value="${equipmentCondDetails.map.sample_info }"/>
						</td>
					</tr>
				</table>
			</fieldset>
		</div>

		<div>
			<div>
			<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel"><insta:ltext key="laboratory.equipmentqualitytest.addedit.testresults"/></legend>
				<table class="detailList">
					<tr>
						<th><insta:ltext key="laboratory.equipmentqualitytest.addedit.resultname"/></th>
						<th><insta:ltext key="laboratory.equipmentqualitytest.addedit.value"/></th>
						<th><insta:ltext key="laboratory.equipmentqualitytest.addedit.units"/></th>
					</tr>
					<c:forEach items="${equipmentResultDetails}" var="result" varStatus="status">
					<c:set var="i" value="${status.index}"/>
						<tr>
							<td>${result.map.resultlabel }</td>
							<td>
								<input type="hidden" name="equipment_id" value="${result.map.eq_id }"/>
								<input type="hidden" name="resultlabel_id" value="${result.map.resultlabel_id }"/>
								<input type="text" name="test_value" value="${newRecord ? ''  : result.map.test_value }" id="test_value${i}"/>
							</td>
							<td>${result.map.units }</td>
						</tr>
					</c:forEach>
					<tr>
						<td colspan="2">
							<insta:ltext key="laboratory.equipmentqualitytest.addedit.remarks"/>:<input type="text" name="remarks" maxlength="1000" value="${equipmentCondDetails.map.remarks }"/>
						</td>
						<td>
							<insta:ltext key="laboratory.equipmentqualitytest.addedit.complete"/>:<input type="checkbox"
								name="test_record_complete" id="test_record_complete"
								value="Y" ${equipmentCondDetails.map.test_record_complete == 'Y' ? 'checked disabled' : '' }/>
						</td>
					</tr>
				</table>
			</fieldset>
		</div>

		<div>
			<button type="submit"  name="save" id="Save" style="button" accesskey="S" onclick="return validateSave();"
				styleClass="button" ${equipmentCondDetails.map.test_record_complete == 'Y' ? 'disabled' : '' }
				><b><u><insta:ltext key="laboratory.equipmentqualitytest.addedit.s"/></u></b><insta:ltext key="laboratory.equipmentqualitytest.addedit.ave"/></button>
			<insta:screenlink addPipe="true" screenId="${category == 'DEP_LAB' ? 'lab_equipment_qa':'rad_equipment_qa' }" label="Equipments Quality Data list"
				extraParam="?_method=list"/>
		</div>
	</form>
</body>
</html>
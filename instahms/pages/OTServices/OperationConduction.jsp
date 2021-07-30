<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"
	isELIgnored="false"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cPath" value="${pageContext.request.contextPath}"/>

<html>
<head>
<title>Surgery/Procedure Conduction</title>
<script>
	function validateForm(addReport) {
		document.opeconductform.addReport.value = addReport;
		document.opeconductform.submit();
		return true;
	}
	var gServerNow = new Date(<%= (new java.util.Date()).getTime() %>);
</script>
<insta:link type="css" file="hmsNew.css" />
<insta:link type="script" file="hmsvalidation.js" />
<insta:link type="script" file="ipservices/Operations.js" />
<style>
      .autocomplete {
          width:8em; /* set width here */
          padding-bottom:1em;
      }

      .scrolForContainer .yui-ac-content{
         max-height:11em;overflow:auto;overflow-x:hidden; /* scrolling */
          _height:11em; /* ie6 */
      }
</style>
</head>
<body >
<h1>Conduct Surgery/Procedure</h1>
<insta:feedback-panel />
<insta:patientdetails visitid="${param.visitId}" showClinicalInfo="true"/>
<form method="POST" action="${cPath}/otservices/EditOperation.do?method=conductOperation&patient_id=${ifn:cleanURL(param.visitId)}" name="opeconductform">
	<input type="hidden" name="_method" id="_method" value="conductOperation" />

	<input type="hidden" name="hdate" id="hdate"/>
	<input type="hidden" name="htime" id="htime"/>
	<input type="hidden" name="addReport" value="false"/>
	<input type="hidden" name="mr_no" id="mr_no" value="${patient.mr_no}"/>
	<input type="hidden" name="prescribed_id" id="prescribed_id" value="${operation.map.prescribed_id}"/>
	<input type="hidden" name="operation_name" id="operation_name" value="${operation.map.op_id}"/>
	<fieldset class="fieldSetBorder" >
		<legend class="fieldSetLabel">${operation.map.operation_name}</legend>
		<table class="formtable" width="100%">
			<tr>
				<td class="formlabel">Primary&nbsp;Surgeon/Doctor:</td>
				<td valign="middle" class="forminfo">
						<c:forEach items="${doctors_list}" var="operationSurgeon">
							<c:if test="${operationSurgeon.OT_DOCTOR_FLAG eq 'Y'}">
							<label> ${operationSurgeon.DOCTOR_ID == operation.map.surgeon_id ?operationSurgeon.DOCTOR_NAME:''} </label>
				            <input type="hidden" name="surgeonid" id="surgeonid" value="" >
							</c:if>
						</c:forEach>
				</td>
				<td class="formlabel">Primary&nbsp;Anaesthetist:</td>
				<td valign="middle" class="forminfo">
						<c:forEach items="${doctors_list}" var="operationAnae">
							<c:if test="${operationAnae.DEPT_NAME eq 'DEP0002'}">
							<label> ${operationAnae.DOCTOR_ID == operation.map.anaesthetist_id ?operationAnae.DOCTOR_NAME :''} </label>
                                        <input type="hidden" name="anaestesistid" id="anaestesistid" value="" >
							</c:if>
						</c:forEach>
				</td>
				<td class="formlabel">Theatre/Room:</td>
				<td valign="middle" class="forminfo">${operation.map.theatre_name}
					<input type="hidden" name="theatre_id" id="theatre_id" value="${operation.map.theatre_id}"/>
				</td>
			</tr>
			<tr>
				<td class="formlabel">Theatre/Room Hrly:</td>
				<td class="yui-skin-sam" valign="top">
				     <input type="checkbox" name="hrly" value="H" id="hrly" ${operation.map.hrly} disabled/>
				</td>
				<td class="formlabel">Start Date & Time:</td>
				<td class="forminfo">${operation.map.startdate} ${operation.map.st_time}
					<input type="hidden" id="startdate" name="startdate" value="${operation.map.startdate}" />
					<input type="hidden" name="operation_time" id="operation_time" value="${operation.map.st_time}" />
				</td>
				<td class="formlabel">End Date & Time:</td>
				<td class="forminfo">${operation.map.enddate} ${operation.map.end_time}
					<input type="hidden" name="enddate" id="enddate" value="${operation.map.enddate}" />
					<input type="hidden" name="expected_end_time" id="expected_end_time" value="${operation.map.end_time}" />
				</td>
			</tr>
			<tr>
				<td class="formlabel">Completed:</td>
				<td>
					<input type="checkbox" name="completed" id="completed" value="C"
						<c:if test="${operation.map.status eq 'C' }">checked disabled</c:if> />
					<input type="hidden" name="hidden_completed" value="${operation.map.status}"/>
				</td>
				<td class="formlabel">Remarks:</td>
				<td><input type="text" name="remarks" id="remarks" value="${operation.map.remarks }"/></td>
			</tr>
			<tr>
				<td colspan="2" style="display: ${preferences.modulesActivatedMap['mod_consumables_flow'] == 'Y' && not empty has_consumables ? 'table-cell' : 'none'}">
					<insta:screenlink screenId="ot_consumables" label="Edit OT Consumbles" extraParam="?_method=getModifyOtConsumablesScreen&operation_id=${operation.map.op_id}&operation_name=${operation.map.operation_name}&prescribedId=${operation.map.prescribed_id}&patient_id=${param.visitId}" />
				</td>
			</tr>
		</table>
	</fieldset>

	<c:url
		var="URL" value="PendingOperations.do">
		<c:param value="pendingList" name="_method" />
		<c:param name="pageNum" value="${param.pageNum}" />
	</c:url>
	<div class="screenActions">
		<input type="button" name="conduct" value="Conduct" onclick="return validateForm(false);"/>
		<input type="button" name="conductAndReport" value="Conduct & Report" onclick="return validateForm(true);">
		| <a href='<c:out value="${URL}"/>'>Pending Surgery/Procedure List</a>
	</div>
</form>
</body>
</html>

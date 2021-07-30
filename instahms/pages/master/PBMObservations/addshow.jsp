<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>${param._method != 'add' ? 'Edit' : 'Add'} PBM Observation Master - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>

	<script>
		var backupName = '';

		function keepBackUp(){
			if(document.pbmObservationForm._method.value == 'update'){
				backupName = document.pbmObservationForm.observation_name.value;
			}
		}

		function doClose() {
			window.location.href = "${cpath}/master/PBMObservationsMaster.do?_method=list&sortOrder=observation_name" +
							"&sortReverse=false&status=A";
		}

		function validate() {
			if (trim(document.getElementById('observation_type').value)=="") {
				alert("Code Type is required");
				document.getElementById('observation_type').focus();
				return false;
			}

			document.pbmObservationForm.submit();
			return true;
		}

	</script>

</head>
<body onload="keepBackUp(); document.getElementById('observation_name').focus();">
<c:choose>
    <c:when test="${param._method != 'add'}">
         <h1>Edit PBM Observation</h1>
    </c:when>
    <c:otherwise>
         <h1>Add PBM Observation</h1>
    </c:otherwise>
</c:choose>

<form action="PBMObservationsMaster.do" name="pbmObservationForm"  method="POST" >
	<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}">
	<c:if test="${param._method == 'show'}">
		<input type="hidden" name="id" value="${bean.map.id}"/>
	</c:if>

	<insta:feedback-panel/>

	<fieldset class="fieldSetBorder">
	<table class="formtable" >
		<tr>
			<td class="formlabel">Observation Name:</td>
			<td>
				<input type="text" name="observation_name" id="observation_name" value="${bean.map.observation_name}"
					class="required validate-length" length="100" title="Observation Name is required and max length of name can be 100" />
			</td>
			<td>Patient Medicine Presc. Value Column:</td>
			<td>
				<select name="patient_med_presc_value_column" id="patient_med_presc_value_column" class="dropdown">
					<c:forEach items="${patMedCols}" var="pmc">
					<option value="${pmc.column_name}"
						${bean.map.patient_med_presc_value_column == pmc.column_name ? 'selected' : ''}>${pmc.column_name}</option>
					</c:forEach>
				</select>
			</td>
			<td>Patient Medicine Presc. Units Column:</td>
			<td>
				<select name="patient_med_presc_units_column" id="patient_med_presc_units_column" class="dropdown">
					<option value="">-- No Units --</option>
					<c:forEach items="${patMedCols}" var="pmc">
					<option value="${pmc.column_name}"
						${bean.map.patient_med_presc_units_column == pmc.column_name ? 'selected' : ''}>${pmc.column_name}</option>
					</c:forEach>
				</select>
			</td>
		</tr>
		<tr>
			<td class="formlabel">Code Type:</td>
			<td>
				<insta:selectdb name="observation_type" value="${bean.map.observation_type}"
					table="mrd_supported_codes" id="observation_type"
					filtercol="code_category" filtervalue="Observations"
					valuecol="code_type" displaycol="code_type" dummyvalue="-- Select --" dummyvalueId=""/>
			</td>
			<td class="formlabel">Code:</td>
			<td>
				<input type="text" name="code"  value="${bean.map.code}" id="code" maxlength="50"
				 class="required validate-length" title="Code is required and max length of name can be 50"/>
			</td>
		</tr>
		<tr>
			<td class="formlabel">Required:</td>
			<td><insta:selectoptions name="required" value="${bean.map.required}" opvalues="Y,N" optexts="Yes,No" /></td>
			<td class="formlabel">Status</td>
			<td><insta:selectoptions name="status" value="${bean.map.status}" opvalues="A,I" optexts="Active,Inactive" /></td>
		</tr>
	</table>
	</fieldset>

	<div class="screenActions">
		<button type="submit" accesskey="S" onclick="return validate();"><b><u>S</u></b>ave</button>
		|
		<c:if test="${param._method != 'add'}">
		<a href="javascript:void(0);" onclick="window.location.href='${cpath}/master/PBMObservationsMaster.do?_method=add'">Add</a>
		|
		</c:if>
		<a href="javascript:void(0)" onclick="doClose();">PBM Observations List</a>
	</div>

</form>

</body>
</html>

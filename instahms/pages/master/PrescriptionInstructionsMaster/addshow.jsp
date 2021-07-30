<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<html>
<head>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Add/Edit Prescription Instructions Master - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>
<insta:link type="js" file="/masters/presInstructionsMaster.js" />

<script>
	var chkPrescriptionInstructions = <%= request.getAttribute("prescriptionInstructionsList") %>;

	var backupName = '';

	function keepBackUp(){
		if(document.prescriptionInstructionsMaster._method.value == 'update'){
				backupName = document.prescriptionInstructionsMaster.instruction_desc.value;
		}
	}

</script>

</head>
<body onload="keepBackUp();">

<form action="PrescriptionInstructionsMaster.do" method="POST" name="prescriptionInstructionsMaster">
	<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}">
	<input type="hidden" name="instruction_id" id="instruction_id" value="${bean.map.instruction_id}"/>

	<h1>${param._method == 'add' ? 'Add' : 'Edit'} Prescription Instructions </h1>
	<insta:feedback-panel/>
	<fieldset class="fieldsetborder">
		<legend class="fieldsetlabel">Prescription Instructions Details</legend>

		<table class="formtable">
			<tr>
				<td class="formlabel">Prescription Instructions :</td>
				<td colspan="3">
					<input type="text" name="instruction_desc" id="instruction_desc" value="<c:out value="${bean.map.instruction_desc}" />"
						style = "width : 500px" maxlength="500" class="required" title="Prescription Instructions Name is mandatory."><span class="star">*</span>
				</td>
			</tr>
		</table>
	</fieldset>

	<div class="screenActions">
		<button type="submit" accesskey="S" onclick="return validate();"><b><u>S</u></b>ave</button>
		<c:if test="${param._method=='show'}">| <a href="PrescriptionInstructionsMaster.do?_method=add" >Add</a></c:if>
		| <a href="PrescriptionInstructionsMaster.do?_method=list&sortOrder=instruction_id&sortReverse=false">Prescription Instructions List</a>
	</div>
</form>

</body>
</html>

<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Add DRG Codes - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>
<script>
	var backupName = '';
	var hcpcsDefault = 0;
	
	function defaultHcpcsPortionValue(){
		if(document.drgForm._method.value == 'create'){
			document.drgForm.hcpcs_portion_per.value = hcpcsDefault.toFixed(2);
		}
	}
	function keepBackUp(){
		if(document.drgForm._method.value == 'update'){
				backupName = document.drgForm.drg_code.value;
		}
	}

	function doClose() {
		window.location.href = "${cpath}/master/DRGCodesMaster.do?_method=list&sortOrder=drg_code&sortReverse=false" +
							"&status=A";
	}

	function focus(){
		document.drgForm.drg_code.focus();
	}

	function validateDRGForm() {
		document.drgForm.relative_weight.value =
			trim(document.drgForm.relative_weight.value) == '' ? 0 : trim(document.drgForm.relative_weight.value);
		if (!validateDecimal(document.drgForm.relative_weight, "Relative weight must be a valid number"))
				return false;
		document.drgForm.hcpcs_portion_per.value =
			trim(document.drgForm.hcpcs_portion_per.value) == '' ? 0 : trim(document.drgForm.hcpcs_portion_per.value);
		if (!validateDecimal(document.drgForm.hcpcs_portion_per, "HCPCS Portion % must be a valid number"))
				return false;
		if (document.drgForm.hcpcs_portion_per.value > 100){
			alert("HCPCS Portion % must be less than or equal to 100");
			document.drgForm.hcpcs_portion_per.focus();
			return false;
		}
		return true;
	}

	<c:if test="${param._method != 'add'}">
      Insta.masterData=${DRGCodesList};
    </c:if>
    
    var itemGroupList = ${itemGroupListJson};
	var itemSubGroupList = ${itemSubGroupListJson};

</script>
</head>

<body onload="focus(); keepBackUp(); defaultHcpcsPortionValue();itemsubgroupinit();">

<c:choose>
    <c:when test="${param._method !='add'}">
         <h1 style="float:left">Edit DRG Code</h1>
         <c:url var ="searchUrl" value="DRGCodesMaster.do"/>
         <insta:findbykey keys="drg_description,drg_code" fieldName="drg_code" method="show" url="${searchUrl}"/>
    </c:when>
    <c:otherwise>
         <h1>Add DRG Code</h1>
    </c:otherwise>
</c:choose>

<insta:feedback-panel/>

<form action="DRGCodesMaster.do" name="drgForm" method="POST">
	<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}">
	<input type="hidden" name="old_drg_code"  value="${bean.map.drg_code}"/>

	<fieldset class="fieldSetBorder">
	<table class="formtable">
		<tr>
			<td class="formlabel">DRG Code:</td>
			<td>
				<input type="text" name="drg_code"  value="${bean.map.drg_code}" class="required validate-length"
					length="15" title="DRG Code is required and max length of drg code can be 15" ${param._method == 'add' ? '' : 'readonly'}/>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
			</td>
		</tr>
		<tr>
			<td class="formlabel">DRG Code Description:</td>
			<td colspan="5">
				<input type="text" name="drg_description" style="width: 100%" value="${bean.map.drg_description}"
					class="required" size="60" />
			</td>
		</tr>
		<tr>
			<td class="formlabel">Patient Type:</td>
			<td><insta:selectoptions name="patient_type" value="${bean.map.patient_type}" opvalues="I,O" optexts="IP,OP" /></td>
		</tr>
		<tr>
			<td class="formlabel">Relative Weight:</td>
			<td>
				<input type="text" name="relative_weight" value="${bean.map.relative_weight}" class="number"/>
			</td>
		</tr>		
		<tr>
			<td class="formlabel">Status</td>
			<td><insta:selectoptions name="status" value="${bean.map.status}" opvalues="A,I" optexts="Active,Inactive" /></td>
		</tr>
		<tr>
			<td class="formlabel">HCPCS Portion %:</td>
			<td>
				<input type="text" name="hcpcs_portion_per" value="${bean.map.hcpcs_portion_per}" class="number"/>
				
			</td>
		</tr>
	</table>
	</fieldset>
<insta:taxations/>
	<div class="screenActions">
		<button type="submit" accesskey="S" onclick="return validateDRGForm();"><b><u>S</u></b>ave</button>
		|
		<c:if test="${param._method != 'add'}">
			<a href="javascript:void(0)" onclick="window.location.href='${cpath}/master/DRGCodesMaster.do?_method=add'">Add</a>
		|
		</c:if>
		<a href="javascript:void(0)" onclick="doClose();">DRG Codes List</a>
	</div>

</form>

</body>
</html>

<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<html>
<head>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Add/Edit Vaccinations Types Master - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>

<script>
	var vaccinationTypesAndIds = <%= request.getAttribute("vaccinationTypesAndIds") %>
	var backupName = '';
	<c:if test="${param._method != 'add'}">
   	 Insta.masterData=${vaccinationTypesAndIds};
  	</c:if>

	function keepBackUp(){
		if(document.VaccinationTypesMasterSearchForm._method.value == 'update'){
				backupName = document.VaccinationTypesMasterSearchForm.vaccination_type.value;
		}
	}

	function doClose() {
		window.location.href = "${cpath}/master/ClinicalVaccinationsMaster.do?_method=list&sortOrder=vaccination_type&sortReverse=false&status=A";
	}

	function validateForm() {
		var vaccinationType = document.VaccinationTypesMasterSearchForm.vaccination_type.value;
		if (empty(vaccinationType)) {
			alert("Vaccination Type is required");
			document.getElementById('vaccination_type').focus();
			return false;
		}
		if(!checkDuplicate())
			return false;

		return true;
	}

	function checkDuplicate(){
		var newVaccinationType = trimAll(document.VaccinationTypesMasterSearchForm.vaccination_type.value);
		var vaccinationId = document.VaccinationTypesMasterSearchForm.vaccination_type_id.value;
		if(document.VaccinationTypesMasterSearchForm._method.value != 'update'){
			for(var i=0;i<vaccinationTypesAndIds.length;i++){
				item = vaccinationTypesAndIds[i];
				if(newVaccinationType == item.vaccination_type){
					alert(document.VaccinationTypesMasterSearchForm.vaccination_type.value+" already exists pls enter other name...");
			    	document.VaccinationTypesMasterSearchForm.vaccination_type.value='';
			    	document.VaccinationTypesMasterSearchForm.vaccination_type.focus();
			    	return false;
				}

			}
		}
	 	if(document.VaccinationTypesMasterSearchForm._method.value == 'update'){
	  		if (backupName != newVaccinationType){
				for(var i=0;i<vaccinationTypesAndIds.length;i++){
					item = vaccinationTypesAndIds[i];
					if(newVaccinationType == item.vaccination_type){
						alert(document.VaccinationTypesMasterSearchForm.vaccination_type.value+" already exists pls enter other name");
				    	document.VaccinationTypesMasterSearchForm.vaccination_type.focus();
				    	return false;
	  				}
	  			}
	 		}
	 	}
	 	return true;
	}//end of function

</script>

</head>
<body onload="keepBackUp();">

<c:choose>
     <c:when test="${param._method != 'add'}">
        <h1 style="float:left">Edit Vaccination Type</h1>
	    <c:url var="searchUrl" value="/master/ClinicalVaccinationsMaster.do"/>
	    <insta:findbykey keys="vaccination_type,vaccination_type_id" method="show" fieldName="vaccination_type_id" url="${searchUrl}" />
     </c:when>
     <c:otherwise>
        <h1>Add Vaccination Type</h1>
     </c:otherwise>
</c:choose>


<form action="ClinicalVaccinationsMaster.do" method="POST" name="VaccinationTypesMasterSearchForm">
	<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}">
	<input type="hidden" name=vaccination_type_id value="${bean.map.vaccination_type_id}"/>
	<insta:feedback-panel/>
	<fieldset class="fieldsetborder">

		<table class="formtable">
			<tr>
				<td class="formlabel">Vaccination Type:</td>
				<td>
					<input type="text" name="vaccination_type" id="vaccination_type" style="width: 155px" value="${bean.map.vaccination_type}" maxlength="200" />
				</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
			</tr>
			<tr>
				<td class="formlabel">Frequency In Months:</td>
				<td><input type="text" name="frequency_in_months" id="frequency_in_months" value="${bean.map.frequency_in_months}" onkeypress="return enterNumOnly(event)" maxlength="3" ></td>
			</tr>
			<tr>
				<td class="formlabel">Status:</td>
				<td>
					<insta:selectoptions name="status" value="${bean.map.status}" opvalues="A,I" optexts="Active,Inactive" />
				</td>
			</tr>
		</table>

	</fieldset>

	<div class="screenActions">
		<button type="submit" accesskey="S" onclick="return validateForm()"><b><u>S</u></b>ave</button>
		<c:if test="${param._method=='show'}">| <a href="ClinicalVaccinationsMaster.do?_method=add" >Add</a></c:if>
		| <a href="javascript:void(0)" onclick="doClose();">Vaccination Type List</a>
	</div>
</form>

</body>
</html>

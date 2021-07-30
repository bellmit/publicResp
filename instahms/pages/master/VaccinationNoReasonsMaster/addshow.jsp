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
	var noReasonNamesAndIds = <%= request.getAttribute("noReasonNamesAndIds") %>
	var backupName = '';
	<c:if test="${param._method != 'add'}">
   	 Insta.masterData=${noReasonNamesAndIds};
  	</c:if>

	function keepBackUp(){
		if(document.VaccinationNoReasonsMasterSearchForm._method.value == 'update'){
			backupName = document.VaccinationNoReasonsMasterSearchForm.reason_name.value;
		}
	}

	function doClose() {
		window.location.href = "${cpath}/master/ClinicalVaccinationNoReasonsMaster.do?_method=list&sortOrder=reason_name&sortReverse=false&status=A";
	}

	function validateForm() {
		var reasonName = document.VaccinationNoReasonsMasterSearchForm.reason_name.value;
		if (empty(reasonName)) {
			alert("Reason Name is required");
			document.getElementById('reason_name').focus();
			return false;
		}
		if(!checkDuplicate())
			return false;

		return true;
	}

	function checkDuplicate(){
		var newReasonName = trimAll(document.VaccinationNoReasonsMasterSearchForm.reason_name.value);
		var reasonId = document.VaccinationNoReasonsMasterSearchForm.reason_id.value;
		if(document.VaccinationNoReasonsMasterSearchForm._method.value != 'update'){
			for(var i=0;i<noReasonNamesAndIds.length;i++){
				item = noReasonNamesAndIds[i];
				if(newReasonName == item.reason_name){
					alert(document.VaccinationNoReasonsMasterSearchForm.reason_name.value+" already exists pls enter other name...");
			    	document.VaccinationNoReasonsMasterSearchForm.reason_name.value='';
			    	document.VaccinationNoReasonsMasterSearchForm.reason_name.focus();
			    	return false;
				}

			}
		}
	 	if(document.VaccinationNoReasonsMasterSearchForm._method.value == 'update'){
	  		if (backupName != newReasonName){
				for(var i=0;i<noReasonNamesAndIds.length;i++){
					item = noReasonNamesAndIds[i];
					if(newReasonName == item.reason_name){
						alert(document.VaccinationNoReasonsMasterSearchForm.reason_name.value+" already exists pls enter other name");
				    	document.VaccinationNoReasonsMasterSearchForm.reason_name.focus();
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
        <h1 style="float:left">Edit Vaccination No Reason</h1>
	    <c:url var="searchUrl" value="/master/ClinicalVaccinationNoReasonsMaster.do"/>
	    <insta:findbykey keys="reason_name,reason_id" method="show" fieldName="reason_id" url="${searchUrl}" />
     </c:when>
     <c:otherwise>
        <h1>Add Vaccination No Reason</h1>
     </c:otherwise>
</c:choose>


<form action="ClinicalVaccinationNoReasonsMaster.do" method="POST" name="VaccinationNoReasonsMasterSearchForm">
	<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}">
	<input type="hidden" name=reason_id value="${bean.map.reason_id}"/>

	<insta:feedback-panel/>
	<fieldset class="fieldsetborder">

		<table class="formtable">
			<tr>
				<td class="formlabel">Reason Name:</td>
				<td>
					<input type="text" name="reason_name" id="reason_name" style="width: 155px" value="${bean.map.reason_name}" maxlength="500"/>
				</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
			</tr>
			<tr>
				<td class="formlabel">Status:</td>
				<td><insta:selectoptions name="status" value="${bean.map.status}" opvalues="A,I" optexts="Active,Inactive" /></td>
			</tr>
		</table>

	</fieldset>

	<div class="screenActions">
		<button type="submit" accesskey="S" onclick="return validateForm()"><b><u>S</u></b>ave</button>
		<c:if test="${param._method=='show'}">| <a href="ClinicalVaccinationNoReasonsMaster.do?_method=add" >Add</a></c:if>
		| <a href="javascript:void(0)" onclick="doClose();">Vaccination No Reasons List</a>
	</div>
</form>

</body>
</html>

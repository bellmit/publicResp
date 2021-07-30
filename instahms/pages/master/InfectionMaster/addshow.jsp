<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<html>
<head>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Add/Edit Infection Types Master - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>

<script>
	var infectionTypesAndIds = <%= request.getAttribute("infectionTypesAndIds") %>
	var backupName = '';
	<c:if test="${param._method != 'add'}">
   	 Insta.masterData=${infectionTypesAndIds};
  	</c:if>

	function keepBackUp(){
		if(document.InfectionTypesMasterSearchForm._method.value == 'update'){
				backupName = document.InfectionTypesMasterSearchForm.infection_type.value;
		}
	}

	function doClose() {
		window.location.href = "${cpath}/master/ClinicalInfectionMaster.do?_method=list&sortOrder=infection_type&sortReverse=false&status=A";
	}

	function validateForm() {
		var infectionType = document.InfectionTypesMasterSearchForm.infection_type.value;
		if (empty(infectionType)) {
			alert("Infection Type is required");
			document.getElementById('infection_type').focus();
			return false;
		}
		if(!checkDuplicate())
			return false;

		return true;
	}

	function checkDuplicate(){
		var newInfectionType = trimAll(document.InfectionTypesMasterSearchForm.infection_type.value);
		var infectionId = document.InfectionTypesMasterSearchForm.infection_type_id.value;
		if(document.InfectionTypesMasterSearchForm._method.value != 'update'){
			for(var i=0;i<infectionTypesAndIds.length;i++){
				item = infectionTypesAndIds[i];
				if(newInfectionType == item.infection_type){
					alert(document.InfectionTypesMasterSearchForm.infection_type.value+" already exists pls enter other name...");
			    	document.InfectionTypesMasterSearchForm.infection_type.value='';
			    	document.InfectionTypesMasterSearchForm.infection_type.focus();
			    	return false;
				}

			}
		}
	 	if(document.InfectionTypesMasterSearchForm._method.value == 'update'){
	  		if (backupName != newInfectionType){
				for(var i=0;i<infectionTypesAndIds.length;i++){
					item = infectionTypesAndIds[i];
					if(newInfectionType == item.infection_type){
						alert(document.InfectionTypesMasterSearchForm.infection_type.value+" already exists pls enter other name");
				    	document.InfectionTypesMasterSearchForm.infection_type.focus();
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
        <h1 style="float:left">Edit Infection Type</h1>
	    <c:url var="searchUrl" value="/master/ClinicalInfectionMaster.do"/>
	    <insta:findbykey keys="infection_type,infection_type_id" method="show" fieldName="infection_type_id" url="${searchUrl}" />
     </c:when>
     <c:otherwise>
        <h1>Add Infection Type</h1>
     </c:otherwise>
</c:choose>


<form action="ClinicalInfectionMaster.do" method="POST" name="InfectionTypesMasterSearchForm">
	<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}">
	<input type="hidden" name=infection_type_id value="${bean.map.infection_type_id}"/>
	<insta:feedback-panel/>
	<fieldset class="fieldsetborder">
		<table class="formtable">
			<tr>
				<td class="formlabel">Infection Type:</td>
				<td>
					<input type="text" name="infection_type" id="infection_type" style="width: 155px" value="${bean.map.infection_type}" maxlength="200"/>
				</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
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
			<c:if test="${param._method=='show'}">| <a href="ClinicalInfectionMaster.do?_method=add" >Add</a></c:if>
		| <a href="javascript:void(0)" onclick="doClose();">Infection Type List</a>
	</div>
</form>

</body>
</html>

<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<html>
<head>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Add/Edit Infection Site Master - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>

<script>
	var infectionSitesAndIds = <%= request.getAttribute("infectionSitesAndIds") %>
	var backupName = '';
	<c:if test="${param._method != 'add'}">
   	 Insta.masterData=${infectionSitesAndIds};
  	</c:if>

	function keepBackUp(){
		if(document.InfectionSitesMasterSearchForm._method.value == 'update'){
				backupName = document.InfectionSitesMasterSearchForm.infection_site_name.value;
		}
	}

	function doClose() {
		window.location.href = "${cpath}/master/ClinicalInfectionSiteMaster.do?_method=list&sortOrder=infection_site_name&sortReverse=false&status=A";
	}

	function validateForm() {
		var infectionSite = document.InfectionSitesMasterSearchForm.infection_site_name.value;
		if (empty(infectionSite)) {
			alert("Infection Site is required");
			document.getElementById('infection_site_name').focus();
			return false;
		}
		if(!checkDuplicate())
			return false;

		return true;
	}

	function checkDuplicate(){
		var newInfectionSite = trimAll(document.InfectionSitesMasterSearchForm.infection_site_name.value);
		var infectionId = document.InfectionSitesMasterSearchForm.infection_site_id.value;
		if(document.InfectionSitesMasterSearchForm._method.value != 'update'){
			for(var i=0;i<infectionSitesAndIds.length;i++){
				item = infectionSitesAndIds[i];
				if(newInfectionSite == item.infection_site_name){
					alert(document.InfectionSitesMasterSearchForm.infection_site_name.value+" already exists pls enter other name...");
			    	document.InfectionSitesMasterSearchForm.infection_site_name.value='';
			    	document.InfectionSitesMasterSearchForm.infection_site_name.focus();
			    	return false;
				}

			}
		}
	 	if(document.InfectionSitesMasterSearchForm._method.value == 'update'){
	  		if (backupName != newInfectionSite){
				for(var i=0;i<infectionSitesAndIds.length;i++){
					item = infectionSitesAndIds[i];
					if(newInfectionSite == item.infection_site_name){
						alert(document.InfectionSitesMasterSearchForm.infection_site_name.value+" already exists pls enter other name");
				    	document.InfectionSitesMasterSearchForm.infection_site_name.focus();
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
        <h1 style="float:left">Edit Infection Site</h1>
	    <c:url var="searchUrl" value="/master/ClinicalInfectionSiteMaster.do"/>
	    <insta:findbykey keys="infection_site_name,infection_site_id" method="show" fieldName="infection_site_id" url="${searchUrl}" />
     </c:when>
     <c:otherwise>
        <h1>Add Infection Site</h1>
     </c:otherwise>
</c:choose>


<form action="ClinicalInfectionSiteMaster.do" method="POST" name="InfectionSitesMasterSearchForm">
	<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}">
	<input type="hidden" name=infection_site_id value="${bean.map.infection_site_id}"/>
	<insta:feedback-panel/>
	<fieldset class="fieldsetborder">
		<table class="formtable">
			<tr>
				<td class="formlabel">Infection Site:</td>
				<td>
					<input type="text" name="infection_site_name" id="infection_site_name" style="width: 155px" value="${bean.map.infection_site_name}" maxlength="500"/>
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
			<c:if test="${param._method=='show'}">| <a href="ClinicalInfectionSiteMaster.do?_method=add" >Add</a></c:if>
		| <a href="javascript:void(0)" onclick="doClose();">Infection Site List</a>
	</div>
</form>

</body>
</html>

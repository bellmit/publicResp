<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title><insta:ltext key="patient.hospitalroles.addshow.pagetitle"/></title>
<insta:link type="script" file="hmsvalidation.js"/>
<script>
	var chkHospitalRoleName = <%= request.getAttribute("hospitalRolesList") %>;
	var backupName = '';

	function keepBackUp(){
		if(document.AddHospitalRolesMasterForm._method.value == 'update'){
				backupName = document.AddHospitalRolesMasterForm.hosp_role_name.value;
		}
	}

	function validate() {

		var hospRoleName = document.getElementById('hosp_role_name').value.trim();
		if (empty(hospRoleName)) {
			alert('Please enter hospital role name.');
			document.getElementById('hosp_role_name').focus();
			return false;
		}

		if (!checkDuplicate()) return false;

		return true;
	}

	function checkDuplicate() {

		var newHospRoleName = trimAll(document.AddHospitalRolesMasterForm.hosp_role_name.value);

		if(document.AddHospitalRolesMasterForm._method.value != 'update'){
			for(var i=0;i<chkHospitalRoleName.length;i++){
				item = chkHospitalRoleName[i];
				if (newHospRoleName == item.HOSP_ROLE_NAME){
					alert(document.AddHospitalRolesMasterForm.hosp_role_name.value+" already exists pls enter other name...");
			    	document.AddHospitalRolesMasterForm.hosp_role_name.value='';
			    	document.AddHospitalRolesMasterForm.hosp_role_name.focus();
			    	return false;
				}
			}
		}

		if(document.AddHospitalRolesMasterForm._method.value == 'update'){
		  		if (backupName != newHospRoleName){
					for(var i=0;i<chkHospitalRoleName.length;i++){
						item = chkHospitalRoleName[i];
						if(newHospRoleName == item.HOSP_ROLE_NAME){
							alert(document.AddHospitalRolesMasterForm.hosp_role_name.value+" already exists pls enter other name.");
					    	document.AddHospitalRolesMasterForm.hosp_role_name.focus();
					    	return false;
		  				}
		  			}
		 		}
		 	}
		return true;
	}
</script>
</head>
<body onload="keepBackUp();">
	<form action="HospitalRolesMaster.do" method="POST" name="AddHospitalRolesMasterForm">
	<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}">
	<input type="hidden" name="hosp_role_id" id="hosp_role_id" value="${bean.map.hosp_role_id}"/>
	<h1>
	<c:choose>
		<c:when test="${param._method == 'add'}"><insta:ltext key="patient.hospitalroles.addshow.title.add"/></c:when>
		<c:otherwise><insta:ltext key="patient.hospitalroles.addshow.title.edit"/></c:otherwise>
	</c:choose>
		<insta:ltext key="patient.hospitalroles.addshow.title.hospitalrole"/>
	</h1>
	<insta:feedback-panel/>
	<fieldset class="fieldsetborder">
		<legend class="fieldsetlabel"><insta:ltext key="patient.hospitalroles.addshow.hospitalrolesdetails"/></legend>

		<table class="formtable">
			<tr>
				<td class="formlabel"><insta:ltext key="patient.hospitalroles.addshow.hospitalrolename"/>:</td>
				<td>
					<input type="text" name="hosp_role_name" id="hosp_role_name" value="${bean.map.hosp_role_name}" maxlength="100" class="required" title="Hospital Role Name is mandatory."><span class="star">*</span>
				</td>
				<td/>
				<td/>
				<td/>
			</tr>
			<tr>
				<td class="formlabel"><insta:ltext key="patient.hospitalroles.addshow.status"/>:</td>
				<td><insta:selectoptions name="status" id="status" value="${bean.map.status}" opvalues="A,I" optexts="Active,Inactive" /></td>
				<td/>
				<td/>
				<td/>
			</tr>
		</table>
	</fieldset>
	<div class="screenActions">
		<button type="submit" accesskey="S" onclick="return validate();"><b><u><insta:ltext key="patient.hospitalroles.addshow.s"/></u></b><insta:ltext key="patient.hospitalroles.addshow.ave"/></button>
		<c:if test="${param._method=='show'}">| <a href="HospitalRolesMaster.do?_method=add" ><insta:ltext key="patient.hospitalroles.addshow.title.add"/></a></c:if>
		| <a href="HospitalRolesMaster.do?_method=list&sortOrder=hosp_role_id&sortReverse=false&status=A"><insta:ltext key="patient.hospitalroles.addshow.hospitalroleslist"/></a>
	</div>
	</form>

</body>
</html>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<html>
<head>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Add/Edit Discharge Types Master - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>

<script>
	var dischargeTypes = <%= request.getAttribute("dischargeTypes") %>;
	var backupName = '';

	function keepBackUp(){
		if(document.dischargeTypeMaster._method.value == 'update'){
			backupName = document.dischargeTypeMaster.discharge_type.value;
		}
	}


function validate() {
	var dischargeType = document.getElementById('discharge_type').value.trim();
	if (empty(dischargeType)) {
		alert('Please enter discharge type');
		document.getElementById('discharge_type').focus();
		return false;
	}

	if (!checkDuplicate()) return false;

	return true;
}

function checkDuplicate(){
	var newDischargeType = trimAll(document.dischargeTypeMaster.discharge_type.value);

	if(document.dischargeTypeMaster._method.value != 'update'){
		for(var i=0;i<dischargeTypes.length;i++){
			item = dischargeTypes[i];
			if (newDischargeType == item.DISCHARGE_TYPE){
				alert(document.dischargeTypeMaster.discharge_type.value+" already exists pls enter other name...");
		    	document.dischargeTypeMaster.discharge_type.value='';
		    	document.dischargeTypeMaster.discharge_type.focus();
		    	return false;
			}
		}
	}

	if(document.dischargeTypeMaster._method.value == 'update'){
	  		if (backupName != newDischargeType){
				for(var i=0;i<dischargeTypes.length;i++){
					item = dischargeTypes[i];
					if(newDischargeType == item.DISCHARGE_TYPE){
						alert(document.dischargeTypeMaster.discharge_type.value+" already exists pls enter other name");
				    	document.dischargeTypeMaster.discharge_type.focus();
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

<form action="DischargeTypeMaster.do" method="POST" name="dischargeTypeMaster">
	<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}">
	<input type="hidden" name="discharge_type_id" id="discharge_type_id" value='<c:out value="${bean.map.discharge_type_id}"/>'/>
	<c:set var="curr_discharge_type_id" value='<c:out value="${bean.map.discharge_type_id}"/>'/>

	<jsp:useBean id="oldValuesMap" class="java.util.HashMap"/>
		<c:set target="${oldValuesMap}" property="1" value= "Normal" />
		<c:set target="${oldValuesMap}" property="2" value= "Absconded" />
		<c:set target="${oldValuesMap}" property="3" value= "Death" />
		<c:set target="${oldValuesMap}" property="4" value= "DAMA" />
		<c:set target="${oldValuesMap}" property="5" value= "Referred To" />
		<c:set target="${oldValuesMap}" property="6" value= "Admission Cancelled" />
	<c:set var="curr_discharge_type_id" value="${ifn:toString(bean.map.discharge_type_id)}"/>
	<h1>${param._method == 'add' ? 'Add' : 'Edit'} Discharge Types</h1>
	<insta:feedback-panel/>
	<fieldset class="fieldsetborder">
		<legend class="fieldsetlabel">Discharge Type Details</legend>

		<table class="formtable">
			<tr>
				<td class="formlabel">Discharge Type:</td>
				<td>
					<input type="text" name="discharge_type" id="discharge_type" value='<c:out value="${bean.map.discharge_type}"/>'
					maxlength="100" ${(not empty bean.map.discharge_type_id && oldValuesMap[curr_discharge_type_id] == bean.map.discharge_type) ? 'disabled' : ''}>
					<span class="star">*</span>
				</td>
				<td class="formlabel">Status:</td>
				<td><insta:selectoptions name="status" id="status" value="${bean.map.status}" opvalues="A,I" optexts="Active,Inactive" /></td>
			</tr>
		</table>

	</fieldset>

	<div class="screenActions">
		<button type="submit" accesskey="S" onclick="return validate();"><b><u>S</u></b>ave</button>
		<c:if test="${param._method=='show'}">| <a href="DischargeTypeMaster.do?_method=add" >Add</a></c:if>
		| <a href="DischargeTypeMaster.do?_method=list&sortOrder=discharge_type&sortReverse=false&status=A">Discharge Types List</a>
	</div>
</form>

</body>
</html>

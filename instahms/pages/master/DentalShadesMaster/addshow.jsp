<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<html>
<head>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Add/Edit Dental Shades Master - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>

<script>
	var chkShadeName = <%= request.getAttribute("dentalShadesList") %>;
	var backupName = '';

	function keepBackUp(){
		if(document.dentalShadesMaster._method.value == 'update'){
			backupName = document.dentalShadesMaster.shade_name.value;
		}
	}


function validate() {
	var shadeName = document.getElementById('shade_name').value.trim();
	if (empty(shadeName)) {
		alert('Please enter shade name');
		document.getElementById('shade_name').focus();
		return false;
	}

	if (!checkDuplicate()) return false;

	return true;
}

function checkDuplicate(){
	var newShadeName = trimAll(document.dentalShadesMaster.shade_name.value);

	if(document.dentalShadesMaster._method.value != 'update'){
		for(var i=0;i<chkShadeName.length;i++){
			item = chkShadeName[i];
			if (newShadeName == item.SHADE_NAME){
				alert(document.dentalShadesMaster.shade_name.value+" already exists pls enter other name...");
		    	document.dentalShadesMaster.shade_name.value='';
		    	document.dentalShadesMaster.shade_name.focus();
		    	return false;
			}
		}
	}

	if(document.dentalShadesMaster._method.value == 'update'){
	  		if (backupName != newShadeName){
				for(var i=0;i<chkShadeName.length;i++){
					item = chkShadeName[i];
					if(newShadeName == item.SHADE_NAME){
						alert(document.dentalShadesMaster.shade_name.value+" already exists pls enter other name");
				    	document.dentalShadesMaster.shade_name.focus();
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

<form action="DentalShadesMaster.do" method="POST" name="dentalShadesMaster">
	<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}">
	<input type="hidden" name="shade_id" id="shade_id" value="${bean.map.shade_id}"/>

	<h1>${param._method == 'add' ? 'Add' : 'Edit'} Dental Sahdes</h1>
	<insta:feedback-panel/>
	<fieldset class="fieldsetborder">
		<legend class="fieldsetlabel">Denatl Shades Details</legend>

		<table class="formtable">
			<tr>
				<td class="formlabel">Shade Name:</td>
				<td>
					<input type="text" name="shade_name" id="shade_name" value="${bean.map.shade_name}" maxlength="50"><span class="star">*</span>
				</td>
				<td class="formlabel">Status:</td>
				<td><insta:selectoptions name="status" id="status" value="${bean.map.status}" opvalues="A,I" optexts="Active,Inactive" /></td>
				<td class="formlabel">Shade Code:</td>
				<td>
					<input type="text" name="shade_code" id="shade_code" value="${bean.map.shade_code}" maxlength="50">
				</td>
			</tr>
		</table>

	</fieldset>

	<div class="screenActions">
		<button type="submit" accesskey="S" onclick="return validate();"><b><u>S</u></b>ave</button>
		<c:if test="${param._method=='show'}">| <a href="DentalShadesMaster.do?_method=add" >Add</a></c:if>
		| <a href="DentalShadesMaster.do?_method=list&sortOrder=shade_name&sortReverse=false&status=A">Dental Shades List</a>
	</div>
</form>

</body>
</html>

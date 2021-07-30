<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="max_centers_inc_default" value='<%=GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default")%>'/>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>

<html>
<head>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Add/Edit Generic Resources Master - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>

<script>
	var chkResourceName = <%= request.getAttribute("genericResourceNames") %>
	var backupName = '';

	function keepBackUp(){
	if(document.GenericResourcesMaster._method.value == 'update'){
			backupName = document.GenericResourcesMaster.generic_resource_name.value;
	}
}

	function changeCheckboxValues() {
		var schedule = document.GenericResourcesMaster.schedule;
		if (schedule.checked)
			document.GenericResourcesMaster.overbook_limit.disabled = false;
			else
				document.GenericResourcesMaster.overbook_limit.disabled = true;
				document.GenericResourcesMaster.overbook_limit.value = 0;
	}


function validate() {

	document.getElementById('generic_resource_name').value = document.getElementById('generic_resource_name').value.trim();
	if (empty(document.getElementById('generic_resource_name').value)) {
		alert('Please enter generic resource name');
		document.getElementById('generic_resource_name').focus();
		return false;
	}

	if (empty(document.getElementById('generic_resource_type_id').value)) {
		alert('Please enter generic resource type');
		document.getElementById('generic_resource_type_id').focus();
		return false;
	}

	var center = document.getElementById('center_id');
	if (center && empty(center.value)) {
		alert("Please select the center");
		center.focus();
		return false;
	}
	var overbook = document.forms[0].overbook_limit.value;
	
	if(overbook.length > 10){
		alert("Please enter only 10 digits number for overbook Limit");
		document.forms[0].overbook_limit.focus();
		return false;
	}
	if (!checkDuplicate()) return false;

	return true;
}
function checkDuplicate(){
	var newResourceName = trimAll(document.GenericResourcesMaster.generic_resource_name.value);

	if(document.GenericResourcesMaster._method.value != 'update'){
		for(var i=0;i<chkResourceName.length;i++){
			item = chkResourceName[i];
			if (newResourceName == item.GENERIC_RESOURCE_NAME){
				alert(document.GenericResourcesMaster.generic_resource_name.value+" already exists pls enter other name...");
		    	document.GenericResourcesMaster.generic_resource_name.value='';
		    	document.GenericResourcesMaster.generic_resource_name.focus();
		    	return false;
			}
		}
	}

	if(document.GenericResourcesMaster._method.value == 'update'){
  		if (backupName != newResourceName){
			for(var i=0;i<chkResourceName.length;i++){
				item = chkResourceName[i];
				if(newResourceName == item.GENERIC_RESOURCE_NAME){
					alert(document.GenericResourcesMaster.generic_resource_name.value+" already exists pls enter other name");
			    	document.GenericResourcesMaster.generic_resource_name.focus();
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

<form action="GenericResourceMaster.do" method="POST" name="GenericResourcesMaster">
	<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}">
	<input type="hidden" name="generic_resource_id" id="generic_resource_id" value="${bean.map.generic_resource_id}"/>

	<h1>${param._method == 'add' ? 'Add' : 'Edit'} Generic Resources</h1>
	<insta:feedback-panel/>
	<fieldset class="fieldsetborder">
		<legend>Generic Resource Details</legend>

		<table class="formtable">
			<tr>
				<td class="formlabel">Resource Name:</td>
				<td class="forminput">
					<input type="text" name="generic_resource_name" id="generic_resource_name" value="${bean.map.generic_resource_name}">
				</td>
				<td class="formlabel">Generic Resource Type:</td>
				<td>
					<insta:selectdb name="generic_resource_type_id" id="generic_resource_type_id" value="${bean.map.generic_resource_type_id}"
											table="generic_resource_type" valuecol="generic_resource_type_id" displaycol="resource_type_desc" dummyvalue="--Select--"
											orderby="resource_type_desc"/>
				</td>
				<td class="formlabel">Status:</td>
				<td><insta:selectoptions name="status" value="${bean.map.status}" opvalues="A,I" optexts="Active,Inactive" /></td>
			</tr>
			<tr>
				<td class="formlabel">Schedulable:</td>
				<td><input type="checkbox" name="schedule" ${bean.map.schedule ? 'checked' : ''} onclick="changeCheckboxValues()"/></td>
				<td class="formlabel">Overbook Limit:</td>
				<c:set var="overbookDisable" value="${bean.map.schedule ? '' : 'disabled'}"/>
				<td><input type="text" name="overbook_limit" value="${overbookDisable == 'disabled'? 0 : bean.map.overbook_limit}" ${overbookDisable} 
				onkeypress="return enterNumOnlyzeroToNine(event)" />
				<img class="imgHelpText" title=" Zero - overbook not allowed.
 Empty - infinite.
 Specific number - that many overbooking allowed." src="${cpath}/images/help.png" style="float:right;"></td>
				
				<c:choose>
					<c:when test="${max_centers_inc_default == 1}">
						<input type="hidden" name="center_id" id="center_id" value="0"/>
					</c:when>
					<c:otherwise>
						<td class="formlabel">Center: </td>
						<td class="forminfo">
							<c:choose>
								<c:when test="${param._method == 'add'}">
									<select class="dropdown" name="center_id" id="center_id">
										<option value="">-- Select --</option>
										<c:forEach items="${centers}" var="center">
											<option value="${center.map.center_id}">${center.map.center_name}</option>
										</c:forEach>
									</select>
								</c:when>
								<c:otherwise>
									<input type="hidden" name="center_id" id="center_id" value="${bean.map.center_id}"/>
									${bean.map.center_name}
								</c:otherwise>
							</c:choose>
						</td>
					</c:otherwise>
				</c:choose>
			</tr>
		</table>

	</fieldset>

	<div class="screenActions">
		<button type="submit" accesskey="S" onclick="return validate();"><b><u>S</u></b>ave</button>
		<c:if test="${param._method=='show'}">| <a href="GenericResourceMaster.do?_method=add" >Add</a></c:if>
		| <a href="GenericResourceMaster.do?_method=list&sortOrder=generic_resource_name&sortReverse=false&grm.status=A">Generic Resources List</a>
	</div>
</form>

</body>
</html>

<%@page import="com.insta.hms.master.URLRoute"%>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="pagePath" value="<%=URLRoute.INCOMING_HOSPITALS %>"/>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Add Incoming Hospital - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>
	<script>
		var chkOccupationList = ${ifn:convertListToJson(HospitalList)};
		var hiddenOccupationId = '${bean.hospital_id}';
		function doClose() {
			window.location.href = "${cpath}/${pagePath}/list.htm?sortOrder=hospital_name&sortReverse=false&status=A";
		}
		function checkduplicate(){
			var newoccupationName = trimAll(document.InComingHospitalForm.hospital_name.value);
			for(var i=0;i<chkOccupationList.length;i++){
				item = chkOccupationList[i];
				if (item != undefined){
					if(hiddenOccupationId!=item.hospital_id){
				   		var actualOccupationName = item.hospital_name;
				    	if (newoccupationName.toLowerCase() == actualOccupationName.toLowerCase()) {
				    	alert(document.InComingHospitalForm.hospital_name.value+" already exists pls enter other name");
				    	document.InComingHospitalForm.hospital_name.value='';
				    	document.InComingHospitalForm.hospital_name.focus();
				    	return false;
				    }
			     }
			}
		 }
      }
      function focus(){
      	document.InComingHospitalForm.hospital_name.focus();
      }

      var backupName = '';

		function validateSave(){
			document.getElementById("hospital_name").value=document.getElementById("hospital_name").value.trim();
			return true;
		}
	</script>
</head>
<body onload="focus();">
 <h1>Add Hospital</h1>
     
<c:set var="actionUrl" value="create.htm"/>
<form action="${actionUrl}" name="InComingHospitalForm" method="POST">

	<insta:feedback-panel/>

	<fieldset class="fieldSetBorder">
	<table class="formtable">
		<tr>
			<td class="formlabel">Hospital Name:</td>
			<td>
				<input type="text" id ="hospital_name" name="hospital_name" value="${bean.hospital_name}" onblur="capWords(hospital_name);checkduplicate();" class="required validate-length" length="100" title="Name is required and max length of name can be 100" />
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
			</td>
		</tr>

		<tr>
			<td class="formlabel">Default Rate Plan:</td>
			<td>			
			<select name="default_rate_plan_id" id="default_rate_plan_id" class="dropdown">
				<option value="">--Select--</option>
					<c:forEach items="${ratePlanDetails}" var="ratePlans">
						<option value="${ratePlans.org_id}">${ratePlans.org_name}</option>
					</c:forEach>
			</select>
			</td>
		</tr>

		<tr>
			<td class="formlabel">Status:</td>
			<td><insta:selectoptions name="status" value="${bean.status}" opvalues="A,I" optexts="Active,Inactive" /></td>
		</tr>
        <tr>
		    <td class="formlabel">Email Id:</td>
		    <td>
		        <input type="text" name="email_id" value="${bean.email_id}" class="validate-email" title="Email is not correct"/>
		    </td>
		</tr>


	</table>
	</fieldset>

	<div class="screenActions">
		<button type="submit" accesskey="S" onclick="validateSave();"><b><u>S</u></b>ave</button>
		|
		<a href="javascript:void(0)" onclick="doClose();">Hospital List</a>
	</div>

</form>

</body>
</html>

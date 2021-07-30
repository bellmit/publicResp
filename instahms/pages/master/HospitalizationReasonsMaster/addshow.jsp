<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<html>
<head>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Add/Edit Hospitalization Reason Master - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>

<script>
	var reasonNamesAndIds = <%= request.getAttribute("reasonNamesAndIds") %>
	var backupName = '';
	<c:if test="${param._method != 'add'}">
    	Insta.masterData=${reasonNamesAndIds};
  	</c:if>

	function keepBackUp(){
		if(document.HospitalizationReasonsMasterSearchForm._method.value == 'update'){
				backupName = document.HospitalizationReasonsMasterSearchForm.reason.value;
		}
	}

	function doClose() {
		window.location.href = "${cpath}/master/HospitalizationReasonsMaster.do?_method=list&sortOrder=reason&sortReverse=false&status=A";
	}

	function validateForm() {
		var reason = document.HospitalizationReasonsMasterSearchForm.reason.value;
		if (empty(reason)) {
			alert("Reason Name is required");
			document.getElementById('reason').focus();
			return false;
		}
		if(!checkDuplicate())
			return false;

		return true;
	}

	function checkDuplicate(){
		var newReasonName = trimAll(document.HospitalizationReasonsMasterSearchForm.reason.value);
		var reasonId = document.HospitalizationReasonsMasterSearchForm.reason_id.value;
		if(document.HospitalizationReasonsMasterSearchForm._method.value != 'update'){
			for(var i=0;i<reasonNamesAndIds.length;i++){
				item = reasonNamesAndIds[i];
				if(newReasonName == item.reason){
					alert(document.HospitalizationReasonsMasterSearchForm.reason.value+" already exists pls enter other name...");
			    	document.HospitalizationReasonsMasterSearchForm.reason.value='';
			    	document.HospitalizationReasonsMasterSearchForm.reason.focus();
			    	return false;
				}

			}
		}
	 	if(document.forms[0]._method.value == 'update'){
	  		if (backupName != newReasonName){
				for(var i=0;i<reasonNamesAndIds.length;i++){
					item = reasonNamesAndIds[i];
					if(newReasonName == item.reason){
						alert(document.forms[0].reason.value+" already exists pls enter other name");
				    	document.HospitalizationReasonsMasterSearchForm.reason.focus();
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
        <h1 style="float:left">Edit Reasons</h1>
	    <c:url var="searchUrl" value="/master/HospitalizationReasonsMaster.do"/>
	    <insta:findbykey keys="reason,reason_id" method="show" fieldName="reason_id" url="${searchUrl}" />
     </c:when>
     <c:otherwise>
        <h1>Add Reasons</h1>
     </c:otherwise>
</c:choose>


<form action="HospitalizationReasonsMaster.do" method="POST" name="HospitalizationReasonsMasterSearchForm">
	<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}">
	<input type="hidden" name="reason_id" value="${bean.map.reason_id}"/>

	<insta:feedback-panel/>
	<fieldset class="fieldsetborder">

		<table class="formtable">
			<tr>
				<td class="formlabel">Reason Name:</td>
				<td>
					<input type="text" name="reason" id="reason" style="width: 155px" value="${bean.map.reason}" maxlength="200"/>
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
		<c:if test="${param._method=='show'}">| <a href="HospitalizationReasonsMaster.do?_method=add" >Add</a></c:if>
		| <a href="javascript:void(0)" onclick="doClose();">Reason List</a>
	</div>
</form>

</body>
</html>

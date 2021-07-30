<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<html>
<head>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Add/Edit Feedback Forms - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>

<script>
	var chkFormName = <%= request.getAttribute("feedbackFormsList") %>;
	var backupName = '';

	function keepBackUp(){
		if(document.feedbackFormSearchForm._method.value == 'update'){
			backupName = document.feedbackFormSearchForm.form_name.value;
		}
	}


function validate() {
	var formName = document.getElementById('form_name').value.trim();
	var formTitle = document.feedbackFormSearchForm.form_title.value.trim();
	if (empty(formName)) {
		alert('Please enter Form Name');
		document.getElementById('form_name').focus();
		return false;
	}

	if(empty(formTitle)) {
		alert('Please enter Form Title');
		document.feedbackFormSearchForm.form_title.focus();
		return false;
	}

	if (!checkDuplicate()) return false;

	if(!imposeMaxLength(document.feedbackFormSearchForm.form_title,'Title')) return false;

	if(!imposeMaxLength(document.feedbackFormSearchForm.form_footer,'Footer')) return false;

	return true;
}

function checkDuplicate(){
	var newFormName = trimAll(document.feedbackFormSearchForm.form_name.value);

	if(document.feedbackFormSearchForm._method.value != 'update'){
		for(var i=0;i<chkFormName.length;i++){
			item = chkFormName[i];
			if (newFormName == item.FORM_NAME){
				alert(document.feedbackFormSearchForm.form_name.value+" already exists pls enter other name...");
		    	document.feedbackFormSearchForm.form_name.value='';
		    	document.feedbackFormSearchForm.form_name.focus();
		    	return false;
			}
		}
	}

	if(document.feedbackFormSearchForm._method.value == 'update'){
	  		if (backupName != newFormName){
				for(var i=0;i<chkFormName.length;i++){
					item = chkFormName[i];
					if(newFormName == item.FORM_NAME){
						alert(document.feedbackFormSearchForm.form_name.value+" already exists pls enter other name");
				    	document.feedbackFormSearchForm.form_name.focus();
				    	return false;
	  				}
	  			}
	 		}
	 	}
		return true;
	}

	function imposeMaxLength(obj,text){
		var objDesc = obj.value;
		var newLines = objDesc.split("\n").length;
		var length = objDesc.length + newLines;
		var fixedLen = (text == 'Title') ? 500 : 2000;
		if (length > fixedLen) {
			alert(text+" can not be more than" +fixedLen +" characters");
			obj.focus();
			return false;
		}
		return true;
	}

</script>

</head>
<body onload="keepBackUp();">

<form name="feedbackFormSearchForm" action="SurveyFeedbackForms.do" method="POST">
	<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}">
	<input type="hidden" name="form_id" id="form_id" value="${bean.map.form_id}"/>

	<h1>${param._method == 'add' ? 'Add' : 'Edit'} Feedback Form</h1>
	<insta:feedback-panel/>
	<fieldset class="fieldsetborder">
		<legend class="fieldsetlabel">Feedback Form Details</legend>

		<table class="formtable">
			<tr>
				<td class="formlabel">Form Name:</td>
				<td colspan="3" title="${bean.map.form_name}">
					<input type="text" name="form_name" id="form_name" value="${bean.map.form_name}" maxlength="100"><span class="star">*</span>
				</td>
			</tr>
			<tr>
				<td class="formlabel">Form Status:</td>
				<td colspan="3"><insta:selectoptions name="form_status" id="form_status" value="${bean.map.form_status}" opvalues="A,I" optexts="Active,Inactive" /></td>
			</tr>
			<tr>
				<td class="formlabel">Form Title:</td>
				<td colspan="3">
					<textarea cols="60" rows="4" name="form_title"><c:out value="${bean.map.form_title}"/></textarea><span class="star">*</span>
				</td>
			</tr>
			<tr>
				<td class="formlabel">Form Footer:</td>
				<td colspan="3">
					<textarea cols="60" rows="4" name="form_footer"><c:out value="${bean.map.form_footer}"/></textarea>
				</td>
			</tr>
		</table>

	</fieldset>

	<div class="screenActions">
		<button type="submit" accesskey="S" onclick="return validate();"><b><u>S</u></b>ave</button>
		<c:choose>
			<c:when test="${not empty bean.map.form_id}">
		 		<a href="SurveyFeedbackForms.do?_method=editForm&form_id=${bean.map.form_id}">Feedback Form Design</a>
		 	</c:when>
		 	<c:otherwise>
		 		 <a href="SurveyFeedbackForms.do?_method=getFeedbackFormsList&sortOrder=form_name&sortReverse=false&form_status=A">Feedback Forms List</a>
		 	</c:otherwise>
		 </c:choose>
	</div>
</form>

</body>
</html>

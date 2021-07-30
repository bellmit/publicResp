<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<html>
<head>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Add/Edit Survey Question Category - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>

<script>
	var chkCategoryName = <%= request.getAttribute("questionCategoryList") %>;
	var backupName = '';

	function keepBackUp(){
		if(document.categoryMasterForm._method.value == 'update'){
			backupName = document.categoryMasterForm.category.value;
		}
	}


function validate() {
	var category = document.getElementById('category').value.trim();
	if (empty(category)) {
		alert('Please enter category');
		document.getElementById('category').focus();
		return false;
	}

	if (!checkDuplicate()) return false;

	if(!imposeMaxLength(document.categoryMasterForm.category_desc)) return false;

	return true;
}

function checkDuplicate(){
	var newCategoryName = trimAll(document.categoryMasterForm.category.value);

	if(document.categoryMasterForm._method.value != 'update'){
		for(var i=0;i<chkCategoryName.length;i++){
			item = chkCategoryName[i];
			if (newCategoryName == item.CATEGORY){
				alert(document.categoryMasterForm.category.value+" already exists pls enter other name...");
		    	document.categoryMasterForm.category.value='';
		    	document.categoryMasterForm.category.focus();
		    	return false;
			}
		}
	}

	if(document.categoryMasterForm._method.value == 'update'){
	  		if (backupName != newCategoryName){
				for(var i=0;i<chkCategoryName.length;i++){
					item = chkCategoryName[i];
					if(newCategoryName == item.CATEGORY){
						alert(document.categoryMasterForm.category.value+" already exists pls enter other name");
				    	document.categoryMasterForm.category.focus();
				    	return false;
	  				}
	  			}
	 		}
	 	}
		return true;
	}

	function imposeMaxLength(obj){
		var categoryDesc = document.categoryMasterForm.category_desc.value;
		var newLines = categoryDesc.split("\n").length;
		var length = categoryDesc.length + newLines;
		if (length > 500) {
			alert("descripton can not be more than 500 characters");
			return false;
		}
		return true;
	}

</script>

</head>
<body onload="keepBackUp();">

<form action="SurveyQuestionCategoryMaster.do" method="POST" name="categoryMasterForm">
	<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}">
	<input type="hidden" name="category_id" id="category_id" value="${bean.map.category_id}"/>

	<h1>${param._method == 'add' ? 'Add' : 'Edit'} Survey Question Category</h1>
	<insta:feedback-panel/>
	<fieldset class="fieldsetborder">
		<legend class="fieldsetlabel">Question Category Details</legend>

		<table class="formtable">
			<tr>
				<td class="formlabel">Category:</td>
				<td colspan="3">
					<input type="text" name="category" id="category" value="${bean.map.category}" maxlength="300"><span class="star">*</span>
				</td>
			</tr>
			<tr>
				<td class="formlabel">Status:</td>
				<td colspan="3"><insta:selectoptions name="status" id="status" value="${bean.map.status}" opvalues="A,I" optexts="Active,Inactive" /></td>
			</tr>
			<tr>
				<td class="formlabel">Category Description:</td>
				<td colspan="3">
					<textarea cols="60" rows="4" name="category_desc"><c:out value="${bean.map.category_desc}"/></textarea>
				</td>
			</tr>
		</table>

	</fieldset>

	<div class="screenActions">
		<button type="submit" accesskey="S" onclick="return validate();"><b><u>S</u></b>ave</button>
		<c:if test="${param._method=='show'}">| <a href="SurveyQuestionCategoryMaster.do?_method=add" >Add</a></c:if>
		| <a href="SurveyQuestionCategoryMaster.do?_method=getCategoryList&sortOrder=category&sortReverse=false&status=A">Question Category List</a>
	</div>
</form>

</body>
</html>

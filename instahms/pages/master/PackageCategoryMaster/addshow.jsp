<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<html>
<head>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Add/Edit Package Category Master - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>
<insta:link type="js" file="/masters/packageCategory.js" />

<script>
	var chkpackageCategory = <%= request.getAttribute("packageCategoryList") %>;
	var backupName = '';

	function keepBackUp(){
		if(document.packageCategoryMaster._method.value == 'update'){
				backupName = document.packageCategoryMaster.package_category.value;
		}
	}

</script>

</head>
<body onload="keepBackUp();">

<form action="PackageCategoryMaster.do" method="POST" name="packageCategoryMaster">
	<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}">
	<input type="hidden" name="package_category_id" id="package_category_id" value="${bean.map.package_category_id}"/>

	<h1>${param._method == 'add' ? 'Add' : 'Edit'} Package Category </h1>
	<insta:feedback-panel/>
	<fieldset class="fieldsetborder">
		<legend class="fieldsetlabel">Package Category Details</legend>

		<table class="formtable">
			<tr>
				<td class="formlabel">Package Category :</td>
				<td>
					<input type="text" name="package_category" id="package_category" value="<c:out value="${bean.map.package_category}" />" <c:if test="${bean.map.package_category_id == -1}">disabled</c:if> maxlength="100" class="required" title="Package Category Name is mandatory." /><span class="star">*</span>
				</td>
				<td/>
				<td/>
				<td/>
			</tr>
			<tr>
				<td class="formlabel">Status:</td>
				<td><insta:selectoptions name="status" id="status" value="${bean.map.status}" opvalues="A,I" optexts="Active,Inactive" disabled="${bean.map.package_category_id < 0 }" /></td>
				<td/>
				<td/>
				<td/>
			</tr>
		</table>
	</fieldset>

	<div class="screenActions">
		<button type="submit" accesskey="S" onclick="return validate();"><b><u>S</u></b>ave</button>
		<c:if test="${param._method=='show'}">| <a href="PackageCategoryMaster.do?_method=add" >Add</a></c:if>
		| <a href="PackageCategoryMaster.do?_method=list&sortOrder=package_category_id&sortReverse=false&status=A">Package Category List</a>
	</div>
</form>

</body>
</html>

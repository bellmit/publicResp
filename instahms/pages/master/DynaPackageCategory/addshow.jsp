<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Add/Edit Dyna Package Category - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>
<script>

	function doClose() {
		window.location.href = "${cpath}/master/DynaPackageCategoryMaster.do?_method=list&sortOrder=dyna_pkg_cat_name&sortReverse=false";
	}

	function focus(){
		document.dynaPkgCatForm.dyna_pkg_cat_name.focus();
	}

	function validateDelete() {
		document.dynaPkgCatForm._method.value = "delete";
		document.dynaPkgCatForm.submit();
		return true;
	}

	function validateSubmit() {
		var methodVal = "${param._method == 'add' ? 'create' : 'update'}";
		document.dynaPkgCatForm._method.value = methodVal;
		document.dynaPkgCatForm.submit();
		return true;
	}

	<c:if test="${param._method != 'add'}">
      Insta.masterData=${DynaPkgCategoryNamesList};
    </c:if>

</script>
</head>

<body onload="focus();">

<c:choose>
    <c:when test="${param._method !='add'}">
         <h1 style="float:left">Edit Dyna Package Category</h1>
         <c:url var ="searchUrl" value="DynaPackageCategoryMaster.do"/>
         <insta:findbykey keys="dyna_pkg_cat_name,dyna_pkg_cat_id" fieldName="dyna_pkg_cat_id" method="show" url="${searchUrl}"/>
    </c:when>
    <c:otherwise>
         <h1>Add Dyna Package Category</h1>
    </c:otherwise>
</c:choose>

<insta:feedback-panel/>

<form action="DynaPackageCategoryMaster.do" name="dynaPkgCatForm" method="POST">
	<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}">
	<input type="hidden" name="dyna_pkg_cat_id"  value="${bean.map.dyna_pkg_cat_id}"/>

	<fieldset class="fieldSetBorder">
	<table class="formtable">
		<tr>
			<td class="formlabel">Category Name:</td>
			<td>
				<input type="text" name="dyna_pkg_cat_name"  value="${bean.map.dyna_pkg_cat_name}" class="required validate-length"
				  maxlength="150" title="Category name is required and max length can be 150" />
			</td>
			<td class="formlabel">Limit Type:</td>
			<td>
				<c:choose>
					<c:when test="${param._method == 'add'}">
						<insta:selectoptions name="limit_type" opvalues="A,Q,U" value="${bean.map.limit_type}"
							optexts="Amount,Quantity,Unlimited"/>
					</c:when>
					<c:otherwise>
						<b>${bean.map.limit_type eq 'A' ? 'Amount' : ( bean.map.limit_type eq 'Q' ? 'Quantity' : 'Unlimited' )}</b>
						<input type="hidden" name="limit_type"  value="${bean.map.limit_type}"/>
					</c:otherwise>
				</c:choose>
			</td>
			<td>&nbsp;</td>
			<td>&nbsp;</td>
		</tr>
		<tr>
			<td class="formlabel">Category Description:</td>
			<td colspan="2">
				<textarea rows="2" cols="38" name="dyna_pkg_cat_desc">${bean.map.dyna_pkg_cat_desc}</textarea>
			</td>
		</tr>
	</table>
	</fieldset>

	<div class="screenActions">
		<button type="button" accesskey="S" onclick="return validateSubmit();"><b><u>S</u></b>ave</button>
		<c:if test="${param._method != 'add'}">
		|
		<button type="button" accesskey="D" onclick="return validateDelete();"><b><u>D</u></b>elete</button>
		</c:if>
		|
		<c:if test="${param._method != 'add'}">
			<a href="javascript:void(0)" onclick="window.location.href='${cpath}/master/DynaPackageCategoryMaster.do?_method=add'">Add</a>
		|
		</c:if>
		<a href="javascript:void(0)" onclick="doClose();">Dyna Package Category List</a>
	</div>

</form>

</body>
</html>

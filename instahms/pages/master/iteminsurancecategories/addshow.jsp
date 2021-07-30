<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Item Insurance Category Master- Insta HMS</title>
	<insta:link type="script" file="hmsvalidation.js"/>
	<c:set var="cpath" value="${pageContext.request.contextPath}"/>

	<script>
		function doCancel(){
			window.location.href="${cpath}/Insurance/ItemInsuranceCategoryMaster.do?_method=list&system_category=N";
		}
		function validateForm(){
			if(document.insuranceItemCatMasterForm.insurance_category_name.value==""||document.insuranceItemCatMasterForm.insurance_category_name.value==null)
			{
				alert("Please enter the Item Insurance Category name");
				document.insuranceItemCatMasterForm.insurance_category_name.focus();
				return false;
			}
			return true;
		}

		<c:if test="${param._method != 'add'}">
	      Insta.masterData=${InsuranceCategoriesLists};
        </c:if>

	</script>
</head>

<body>
<c:choose>
    <c:when test="${param._method !='add'}">
         <h1 style="float:left">Edit Item Insurance Category Details</h1>
         <c:url var ="searchUrl" value="/Insurance/ItemInsuranceCategoryMaster.do"/>
         <insta:findbykey keys="insurance_category_name,insurance_category_id" fieldName="insurance_category_id" method="show" url="${searchUrl}"/>
    </c:when>
    <c:otherwise>
         <h1>Add Item Insurance Category Details</h1>
    </c:otherwise>
</c:choose>


	<insta:feedback-panel/>

	<form onsubmit="return validateForm();" name="insuranceItemCatMasterForm" action="${cpath}/Insurance/ItemInsuranceCategoryMaster.do" method="POST" >

		<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}">
		<input type="hidden" name="insurance_category_id" value="${bean.map.insurance_category_id}">


		<fieldset class="fieldSetBorder" ><legend class="fieldSetLabel">Item Insurance Category Details</legend>

			<table class="formtable">
				<tr>
					<td class="formlabel">Item Insurance Category Name:</td>
					<td><input type="text" name="insurance_category_name" value="${bean.map.insurance_category_name}"/></td>
					<td class="formlabel">Insurance Payable:</td>
					<td><insta:selectoptions name="insurance_payable" value="${bean.map.insurance_payable}" opvalues="Y,N" optexts="Yes,No"/></td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
				</tr>
			</table>

		</fieldset>

		<div class="screenActions">
			<button type="submit" accesskey="S"><b><u>S</u></b>ave</button>
			|
			<c:if test="${param._method != 'add'}">
			<a href="javascript:void(0)" onclick="window.location.href='${cpath}/Insurance/ItemInsuranceCategoryMaster.do?_method=add'">Add</a>
			|
			</c:if>
			<a href="#" onclick="doCancel();">Item Insurance Category List</a>
		</div>
	</form>
</body>
</html>


<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Item Insurance Category Master- Insta HMS</title>
	<insta:link type="script" file="hmsvalidation.js"/>
	<c:set var="cpath" value="${pageContext.request.contextPath}"/>

	<script>
		function validateForm(){
			if(document.insuranceItemCatMasterForm.insurance_category_name.value==""||document.insuranceItemCatMasterForm.insurance_category_name.value==null)
			{
				alert("Please enter the Item Insurance Category name");
				document.insuranceItemCatMasterForm.insurance_category_name.focus();
				return false;
			}
			return true;
		}

	   Insta.masterData=${ifn:convertListToJson(InsuranceCategoriesLists)};
	</script>
</head>

<body>
	 <h1 style="float:left">Edit Item Insurance Category Details</h1>
     <c:url var ="searchUrl" value="show.htm"/>
     <insta:findbykey keys="insurance_category_name,insurance_category_id" fieldName="insurance_category_id" method="show" url="${searchUrl}"/>

	<insta:feedback-panel/>

	<form onsubmit="return validateForm();" name="insuranceItemCatMasterForm" action="update.htm" method="POST" >
		<input type="hidden" name="insurance_category_id" value="${bean.insurance_category_id}">
		<fieldset class="fieldSetBorder" ><legend class="fieldSetLabel">Item Insurance Category Details</legend>
			<table class="formtable">
				<tr>
					<td class="formlabel">Item Insurance Category Name:</td>
					<td><input type="text" name="insurance_category_name" value="${bean.insurance_category_name}"/></td>
					<td class="formlabel">Insurance Payable:</td>
					<td><insta:selectoptions name="insurance_payable" value="${bean.insurance_payable}" opvalues="Y,N" optexts="Yes,No"/></td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
				</tr>
			</table>
		</fieldset>

		<div class="screenActions">
			<button type="submit" accesskey="S"><b><u>S</u></b>ave</button> | 
			<a href="${cpath}/master/iteminsurancecategories/add.htm" >Add</a> |
			<a href="${cpath}/master/iteminsurancecategories/list.htm" >Item Insurance Category List</a>
		</div>
	</form>
</body>
</html>


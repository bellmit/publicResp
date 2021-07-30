<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>${param._method == 'add' ? 'Add' : 'Update'} Pharmacy Retail Sponsors - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>
	<script>
		var backupName = '';

		function keepBackUp(){
			if(document.pharmacyRetailSponsorForm._method.value == 'update'){
					backupName = document.pharmacyRetailSponsorForm.sponsor_name.value;
			}
		}

		function doClose() {
			window.location.href = "${cpath}/master/PharmacyRetailSponsor.do?_method=list&sortOrder=sponsor_name" +
							"&sortReverse=false&status=A";
		}
		function focus(){
			document.pharmacyRetailSponsorForm.sponsor_name.focus();
		}

		<c:if test="${param._method != 'add'}">
		     Insta.masterData=${retailSponsorsLists};
		</c:if>
	</script>
</head>
<body onload="focus(); keepBackUp();">
<c:choose>
    <c:when test="${param._method != 'add'}">
         <h1 style="float:left">Edit Pharmacy Retail Sponsor</h1>
         <c:url var="searchUrl" value="/master/PharmacyRetailSponsor.do"/>
         <insta:findbykey keys="sponsor_name,sponsor_id" fieldName="sponsor_id" method="show" url="${searchUrl}"/>
    </c:when>
    <c:otherwise>
         <h1>Add Pharmacy Retail Sponsor</h1>
    </c:otherwise>
</c:choose>
<form action="PharmacyRetailSponsor.do" name="pharmacyRetailSponsorForm" method="POST">
	<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}">
	<c:if test="${param._method == 'show'}">
		<input type="hidden" name="sponsor_id" value="${bean.map.sponsor_id}"/>
	</c:if>

	<insta:feedback-panel/>

	<fieldset class="fieldSetBorder">
	<table class="formtable" align="center">
		<tr>
			<td class="formlabel">Sponsor:</td>
			<td>
				 <input type="text" name="sponsor_name" value="${bean.map.sponsor_name}"  class="required validate-length" length="50" title="Name is required and max length of name can be 50" />
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
			</td>
		</tr>

		<tr>
			<td class="formlabel">Status</td>
			<td><insta:selectoptions name="status" value="${bean.map.status}" opvalues="A,I" optexts="Active,Inactive" /></td>
		</tr>

	</table>
	</fieldset>

	<div class="screenActions">
		<button type="submit" accesskey="S"><b><u>S</u></b>ave</button>
		 	|
		<c:if test="${param._method != 'add'}">
			<a href="${cpath}/master/PharmacyRetailSponsor.do?_method=add">Add</a>
			|
		</c:if>
		<a href="javascript:void(0)" onclick="doClose();">Sponsor List</a>
	</div>

</form>

</body>
</html>

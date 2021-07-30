<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />

<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
		<title>Allergy Master - Insta HMS</title>
		<insta:link type="script" file="hmsvalidation.js"/>
		<script>
			 <c:if test="${param._method != 'add'}">
			    Insta.masterData=${allergiesList};
			  </c:if>
		</script>
	</head>
	<body>
		<c:choose>
			<c:when test="${param._method !='add'}">
				<h1 style="float:left">Edit Allergy</h1>
				 <c:url var="searchUrl" value="/IVF/master/AllergyMaster.do"/>
	    		<insta:findbykey keys="allergy_name,allergy_id" method="show" fieldName="allergy_id" url="${searchUrl}" />
			</c:when>
			<c:otherwise>
				<h1>Add Allergy</h1>
			</c:otherwise>
		</c:choose>

	<form action="AllergyMaster.do" name="editAllergyForm" method="POST">
	<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}">
	<c:if test="${param._method == 'show'}">
		<input type="hidden" name="allergy_id" value="${bean.map.allergy_id}"/>
	</c:if>
	<insta:feedback-panel/>

	<fieldset class="fieldsetborder"><legend class="fieldSetLabel">Allergy Details</legend>
	<table class="formtable">
		<tr>
			<td class="formlabel">Allergy Name:</td>
			<td>
				<input type="text" name="allergy_name"  value="${bean.map.allergy_name}" />
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
			</td>
		</tr>
		<tr>
			<td class="formlabel">Status:</td>
			<td><insta:selectoptions name="status" value="${bean.map.status}" opvalues="A,I" optexts="Active,Inactive" /></td>
		</tr>
	</table>
	</fieldset>

	<table class="screenActions">
		<tr>
			<td><button type="submit" accesskey="S"><b><u>S</u></b>ave</button>
			</td>
			<c:if test="${param._method != 'add' }">
				<td>&nbsp;|&nbsp;</td>
				<td><a href="${cpath}/IVF/master/AllergyMaster.do?_method=add">Add</a></td>
			</c:if>
			<td>&nbsp;|&nbsp;</td>
			<td><a href="${cpath}/IVF/master/AllergyMaster.do?_method=list&sortOrder=allergy_name&sortReverse=false&status=A";">Allergy List</a></td>
		</tr>
	</table>

</form>
</body>
</html>

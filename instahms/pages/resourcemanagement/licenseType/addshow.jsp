<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>License Type - Insta HMS</title>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<insta:link type="js" file="hmsvalidation.js"/>
<insta:link type="js" file="ajax.js" />

<script>
var licenseTypeList = <%= request.getAttribute("avllicense") %>;
var hiddenId = '${bean.license_type_id}';
function validate(){
	if(document.forms[0].license_type.value==""){
		alert("Name is required");
		document.forms[0].license_type.focus();
		return false;
	}
	var newLicensName = trimAll(document.forms[0].license_type.value);
		for(i=0;i<licenseTypeList.length;i++){
			item = licenseTypeList[i];
			if(hiddenId!=item.LICENSE_TYPE_ID){
				if(newLicensName==item.LICENSE_TYPE){
					alert("Name already exists");
					document.forms[0].license_type.focus();
					return false;
				}
			}
		}
	document.forms[0].submit();

}
function doClose() {
		window.location.href = "${cpath}/resourcemanagement/licenseType.do?_method=list&status=A&sortOrder=license_type&sortReverse=false";
	}
</script>

</head>
<body>

<form action="licenseType.do" method="POST" >
	<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}">
	<c:if test="${param._method == 'show'}">
		<input type="hidden" name="license_type_id" value="${bean.license_type_id}"/>
	</c:if>

	<h1>${param._method == 'add' ? 'Add' : 'Edit'} License Type</h1>

	<insta:feedback-panel/>

	<fieldset class="fieldsetborder">

		<table class="formtable">
			<tr>
				<td class="formlabel">Name:</td>
				<td>
					<input type="text" name="license_type" value="${bean.license_type}" onblur="capWords(license_type);"
							class="required validate-length" length="100" title="Name is required and max length of name can be 100" />
				</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
			</tr>

			<tr>
				<td class="formlabel">Status:</td>
				<td><insta:selectoptions name="status" value="${bean.status}" opvalues="A,I" optexts="Active,Inactive" /></td>
			</tr>
		</table>

	</fieldset>

	<table class="screenActions">
		<tr>
			<td><button type="submit" accesskey="S" onclick="return validate()"><b><u>S</u></b>ave</button></td>
			<c:if test="${param._method=='show'}">
			<td>&nbsp;|&nbsp;</td>
			<td><a href="javascript:void(0)" onclick="window.location.href='${cpath}/resourcemanagement/licenseType.do?_method=add'">Add</a></td>
			</c:if>
			<td>&nbsp;|&nbsp;</td>
			<td><a href="javascript:void(0)" onclick="doClose();">License Type</a></td>
		</tr>
	</table>

</form>

</body>
</html>

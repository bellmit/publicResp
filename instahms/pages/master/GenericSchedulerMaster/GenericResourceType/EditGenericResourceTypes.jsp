<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page isELIgnored="false" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<c:set var="max_centers_inc_default" value='<%=GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default")%>'/>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>Generic Resources List - Insta HMS</title>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>

	<c:set var="cpath" value="${pageContext.request.contextPath}"/>

	<style type="text/css">
		.status_InActive{background-color: #E4C89C}
	</style>
</head>

<body>

<form action="GenericResourceType.do" method="POST" name="GenericResourcesType">
	<input type="hidden" name="_method" value="update"}">
	<input type="hidden" name="generic_resource_type_id" id="generic_resource_type_id" value="${bean.map.generic_resource_type_id}"/>

	<h1>${param._method == 'add' ? 'Add' : 'Edit'} Generic Resource Types</h1>
	<insta:feedback-panel/>
	<fieldset class="fieldsetborder">
		<legend>Generic Resource Type Details</legend>

		<table class="formtable">
			<tr>
				<td class="formlabel">Generic Resource Type:</td>
				<td class="forminput">
					<input type="text" name="resource_type_desc" id="resource_type_desc" value="${bean.map.resource_type_desc}"/>
				</td>
				<td class="formlabel">Status:</td>
				<td><insta:selectoptions name="status" value="${bean.map.status}" opvalues="A,I" optexts="Active,Inactive" /></td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
			</tr>
		</table>
	</fieldset>

	<div class="screenActions">
		<button type="submit" accesskey="S"><b><u>S</u></b>ave</button>
		 <a href="GenericResourceType.do?_method=list&sortOrder=resource_type_desc&sortReverse=false&status=A">Generic Resource Types List</a>
	</div>
</form>

</body>
</html>
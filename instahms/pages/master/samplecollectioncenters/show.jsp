<%@page import="com.insta.hms.master.URLRoute"%>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="pagePath" value="<%=URLRoute.SAMPLE_COLLECTION_CENTER%>"/>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Sample Collection Centers - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<c:set var="max_centers_inc_default" value='<%=GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default")%>'/>
	<script>
		var sampleCentersList = ${ifn:convertListToJson(sampleCenters)};
		var hiddenDeptId = '${bean.collection_center_id}';
		var max_centers_inc_default = ${max_centers_inc_default};
		
		Insta.masterData=${ifn:convertListToJson(sampleCenters)};
	</script>
</head>
<body>
<h1 style="float:left">Edit Sample Collection Center</h1>
<c:url var="searchUrl" value="/master/samplecollectioncenters/show.htm"/>
<insta:findbykey keys="collection_center,collection_center_id" method="show" fieldName="collection_center_id" url="${searchUrl}" />
<form action="update.htm" method="POST">
	<input type="hidden" name="collection_center_id" value="${bean.collection_center_id}"/>
	<insta:feedback-panel/>

	<fieldset class="fieldsetborder">
	<table class="formtable">
		<tr>
			<td class="formlabel">Sample Collection Center:</td>
			<td>
				<input type="text" name="collection_center"  value="${bean.collection_center}"
					class="required validate-length" maxlength="50" title="Name is required and max length of name can be 50" />
				<td>&nbsp;</td>
				<td>&nbsp;</td>
			</td>
		</tr>
		<tr>
			<c:choose>
				<c:when test="${max_centers_inc_default == 1}">
					<input type="hidden" name="center_id" id="center_id" value="0"/>
				</c:when>
				<c:otherwise>
					<td class="formlabel">Center: </td>
					<td class="forminfo">
						<input type="hidden" name="center_id" id="center_id" value="${bean.center_id}"/>
						<insta:getCenterName center_id="${bean.center_id}"/>
					</td>
				</c:otherwise>
			</c:choose>
		</tr>
		<tr>
			<td class="formlabel">Status:</td>
			<td><insta:selectoptions name="status" value="${bean.status}" opvalues="A,I" optexts="Active,Inactive" /></td>
		</tr>
	</table>
	</fieldset>

	<table class="screenActions">
		<tr>
			<td><button type="submit" accesskey="S"><b><u>S</u></b>ave</button>
			</td>
				<td>&nbsp;|&nbsp;</td>
				<td><a href="${cpath}${pagePath}/add.htm">Add</a></td>
			<td>&nbsp;|&nbsp;</td>
			<td><a href="${cpath}${pagePath}/list.htm?sortOrder=collection_center&sortReverse=false&status=A">
				Sample Collection Centers
			</a></td>
		</tr>
	</table>

</form>
</body>
</html>

<%@page import="com.insta.hms.master.URLRoute"%>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
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
	</script>
</head>
<body>
<h1>Add Sample Collection Center</h1>
<c:url var="searchUrl" value="/master/samplecollectioncenters/add.htm"/>

<form action="create.htm" method="POST">
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
					<select class="dropdown" name="center_id" id="center_id" >
						<c:forEach items="${centers}" var="center">
							<c:if test="${center.center_id != 0}">
								<option value="${center.center_id}">${center.center_name}</option>
							</c:if>
						</c:forEach>
					</select>
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
			<td><a href="${cpath}${pagePath}/list.htm?sortOrder=collection_center&sortReverse=false&status=A">
				Sample Collection Centers
			</a></td>
		</tr>
	</table>

</form>
</body>
</html>

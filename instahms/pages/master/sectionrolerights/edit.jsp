<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@page import="com.insta.hms.master.URLRoute" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="sys_stn_pagePath" value="<%=URLRoute.SYS_GEN_SEC_PATH %>"/>
<c:set var="stn_pagePath" value="<%=URLRoute.SECTIONS_PATH %>"/>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
		<title>Section Role Rights Edit - Insta HMS</title>
		<script>
			var avlbl_options = 'available_roles';
			var selected_options = 'selected_roles';
			function onSubmit() {
				var markers = document.getElementById('selected_roles');
				for (var i=0; i<markers.options.length; i++) {
					markers.options[i].selected = true;
				}
			}
		</script>
		<insta:link type="script" file="shiftelements.js"/>
	</head>
	<body>
		<h1>Edit Section Role Rights</h1>
		<form action="sectionrolerights.do?_method=save" method="POST" autocomplete="off" name="rolerights" enctype="multipart/form-data">
			<input type="hidden" name="section_id" value="${section.map.section_id}"/>
			<table>
				<tr>
					<td class="formlabel">Section Name :</td>
					<td class="forminfo">${section.map.section_id > 0 ? section.map.section_title : section.map.section_name}</td>
				</tr>
			</table>
			<table width="342" style="padding-right:5; padding-left:10px;border-width:0px; margin:10px 0px 10px 0px;">
				<tr>
					<td align="center" style="padding-right: 4pt; border-width:0px; margin:0px; width:134px;">
						Available Roles
						<br />
						<select name="available_roles" id="available_roles" style="width:15em;padding-left:5; color:#666666;"
							multiple="multiple" size="15" onDblClick="moveSelectedOptions(this,this.form.selected_roles);">
							<c:forEach items="${available_roles}" var="a_role">
								<c:set var="roleNotSelected" value="true"/>
								<c:forEach items="${selected_roles}" var="s_role">
									<c:if test="${a_role.getRoleId() == s_role.map.role_id}">
										<c:set var="roleNotSelected" value="false"/>
									</c:if>
								</c:forEach>
								<c:if test="${roleNotSelected && a_role.getStatus() == 'A'}">
									<option value="${a_role.getRoleId()}" title="${a_role.getName()}">${a_role.getName()}</option>
								</c:if>
							</c:forEach>
						</select>
					</td>
					<td valign="top" align="left" style="padding-right:0;">
						<br />
						<br />
						<input type="button" name="addLstFldsButton" value=">" onclick="addListFields();"/>
						<br/>
						<br/>
						<input type="button" name="addLstFldsButton" value="&lt;" onclick="removeListFields();"/>
					</td>
					<td valign="top" align="center" style="width:134px;padding-left:4pt;">
						Selected Roles
						<br />
						<select  size="15" style="width:15em;padding-left:5; color:#666666;" multiple="multiple" id="selected_roles" name="selected_roles" onDblClick="moveSelectedOptions(this,this.form.available_roles);">
							<c:forEach items="${selected_roles}" var="s_role">
								<option value="${s_role.map.role_id}" title="${s_role.map.role_name}">${s_role.map.role_name}</option>
							</c:forEach>
						</select>
					</td>
				</tr>
			</table>
			<div >
				<input type="submit" name="save" value="Save" onclick="onSubmit();"/>
				<c:url value="SectionFieldsMaster.do" var="listUrl">
					<c:param name="_method" value="list"/>
					<c:param name="section_id" value="${section.map.section_id}"/>
				</c:url>
				<c:url value="SectionFieldsMaster.do" var="addUrl">
					<c:param name="_method" value="add"/>
					<c:param name="section_id" value="${section.map.section_id}"/>
				</c:url>
				<c:if test="${section.map.section_id > 0}">
					| <a href="<c:out value='${listUrl}' />">Fields List</a>
					| <a href="<c:out value='${addUrl}' />">Add New Field</a>
					| <a href="<c:out value='${cpath}/master/sections/list.htm?&sortOrder=section_title&sortReverse=false&status=A' />">Section List</a>
				</c:if>
				<c:if test="${section.map.section_id < 0 }">
					| <a href="${cpath}/${sys_stn_pagePath}/list.htm">System Generated sections</a>
				</c:if>
			</div>
		</form>
	</body>
</html>
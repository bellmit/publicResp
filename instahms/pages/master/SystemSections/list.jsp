<%@page import="com.insta.hms.master.URLRoute" %>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<title>System Generated Sections List - Insta HMS</title>
	<c:set var="pagePath" value="<%=URLRoute.SYS_GEN_SEC_PATH %>"/>
	<script>
		var toolbar = {
			Edit : {
				title: "Edit",
				imageSrc: "icons/Edit.png",
				href: '${pagePath}/show.htm?',
				description: "Edit Center Details"
			},
			RoleRights : {
				title: "View/Edit Role Rights",
				imageSrc: "icons/Edit.png",
				href: 'master/sectionrolerights.do?_method=edit',
				onclick: null,
				description: "View/Edit Section Role Rights",
				show : ${urlRightsMap.mas_section_role_rights == 'A'}
			}
		}
		function init() {
			createToolbar(toolbar);
		}
	</script>
</head>
<body onload="init()">
	<h1>System Generated Sections List</h1>
	<c:set var="opFollowupFormRights" value="${preferences.modulesActivatedMap['mod_develop'] eq 'Y'}"/>
	<insta:feedback-panel/>
	<div class="resultList" >
		<table width="100%" class="dataTable" cellspacing="0" cellpadding="0" width="100%" id="resultTable">
			<tr>
				<th>#</th>
				<th>Section Name</th>
				<th>Data Mandatory</th>
				<th title="OP Consultation">OP</th>
				<th>IP Record</th>
				<th title="Surgery/Operation Theatre Management">Surgery</th>
				<th title="Services">Services</th>
				<th title="Triage">Triage</th>
				<th title="Initial Assessment">Assessment</th>
				<th title="Generic Form">Generic Form</th>
				<c:if test="${opFollowupFormRights}">
					<th title="OP Follow Up Form">OP Follow Up Form</th>
				</c:if>
			</tr>
			<c:forEach items="${sections}" var="bean" varStatus="st">
				<c:set var="is_role_edit_requird" value="${bean.section_id == -1 || bean.section_id == -6 ||
					bean.section_id == -4 || bean.section_id == -3}" />
				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'resultTable',
							{section_id:'${bean.section_id}'},[true, ${!is_role_edit_requird}]);" id="toolbarRow${st.index}">
					<td>${(pagedList.pageNumber-1) * pagedList.pageSize + st.index + 1}</td>
					<td>${bean.section_name}</td>
					<td>${bean.section_mandatory ? 'Yes' : 'No'}</td>
					<td>
						${bean.op == 'Y' ? "Yes" : "No"}
					</td>
					<td>
						${bean.ip == 'Y' ? "Yes" : "No"}
					</td>
					<td>
						${bean.surgery == 'Y' ? "Yes" : "No"}
					</td>
					<td>
						${bean.service == 'Y' ? "Yes" : "No"}
					</td>
					<td>
						${bean.triage == 'Y' ? "Yes" : "No"}
					</td>
					<td>
						${bean.initial_assessment == 'Y' ? "Yes" : "No"}
					</td>
					<td>
						${bean.generic_form == 'Y' ? "Yes" : "No"}
					</td>
					<c:if test="${opFollowupFormRights}">
						<td>
							${bean.op_follow_up_consult_form == 'Y' ? "Yes" : "No"}
						</td>
					</c:if>	
				</tr>
			</c:forEach>
		</table>
	</div>
</body>
</html>

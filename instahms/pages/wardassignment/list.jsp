<%@page import="com.insta.hms.core.patient.URLRoute"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" scope="request"/>
<c:set var="pagepath" value="<%= URLRoute.EDIT_WARD %>" />
<jsp:useBean id="currentDate" class="java.util.Date"/>
<html>

<head>
	<title>Nurse/Staff Ward Assignment - Insta HMS</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<insta:link type="js" file="shareLoginDialogCommon.js" />
	<insta:link type="css" file="widgets.css" />
	<insta:link type="script" file="widgets.js" />
	<insta:link type="script" file="ajax.js" />
	<insta:link type="script" file="dashboardColors.js" />
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="script" file="wardactivities/userward/userwarddashboard.js"/>
	<script>
	</script>
	<insta:js-bundle prefix="registration.patient"/>
	<script type="text/javascript">
		var userNameList = ${ifn:convertListToJson(allUserList)};
		var roleNameList = ${ifn:convertListToJson(allRoleList)};
		var pagepath = '${pagepath}';
		
	</script>
</head>
<body onload="init();">
<c:set var="hasResults" value="${not empty pagedList.userdetails}"/>
	<h1>Nurse/Staff Ward Assignment</h1>
	<insta:feedback-panel/>
	<c:set var="userwardassignList" value="${pagedList.userdetails.dtoList}"/>
	<c:set var="pageList" value="${pagedList.userdetails}"/>
	<form name="userDetailsSearchForm" method="GET">
	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>
	<insta:search-lessoptions form="userDetailsSearchForm" >
		<div class="searchbasicOpts">
			<div class="sboField">
				<div class="sboFieldLabel">User Role</div>
				<div class="sboFieldInput">
					<div id="userAutoComplete" style="padding-bottom: 20px">
						<input type="text" name="role_name" id="role_name" value="${ifn:cleanHtmlAttribute(param.role_name)}"/>
						<input type="hidden" name="role_id" id="role_id" value=""/>
						<div id="roleContainer"></div>
					</div>
				</div>
			</div>
		</div>
		<div class="searchbasicOpts">
			<div class="sboField">
				<div class="sboFieldLabel"> User Name</div>
				<div class="sboFieldInput">
					<div id="userAutoComplete" style="padding-bottom: 20px">
						<input type="text" name="emp_username" id="emp_username" value="${ifn:cleanHtmlAttribute(param.emp_username)}"/>
						<input type="hidden" name="user_name" id="user_name" value=""/>
						<div id="usernameContainer"></div>
					</div>
				</div>
			</div>
		</div>
	</insta:search-lessoptions>
	</form>
	<insta:paginate curPage="${pageList.pageNumber}" numPages="${pageList.numPages}" totalRecords="${pageList.totalRecords}"/>
	<div class="resultList">
		<table class="detailList" cellspacing="0" cellpadding="0" style="margin-top: 5px" id="itemsTable" border="0" width="100%">
			<tr >
				<th>#</th>
				<insta:sortablecolumn name="role_name" title="Role name"/>		
				<insta:sortablecolumn name="emp_username" title="User Name"/>
			</tr>
			<c:set var="editNurseWardEnabled" value="${urlRightsMap.addedit_ward_assignment ne 'N' }" />
			<c:forEach var="userWardAssign" items="${userwardassignList}" varStatus="st">
			<c:set var="userList" value="${userWardAssign}"/>
				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
				onclick="showToolbar(${st.index}, event, 'resultTable',
						{roleId: '${userList.role_id}',roleName:'${userList.role_name}',empusername: '${userList.emp_username}'},[${editNurseWardEnabled}])"
						onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}">
					<td>${(pageList.pageNumber-1) *  pageList.pageSize + st.index + 1}</td>
					<td>${userList.role_name}</td>
					<td>${userList.emp_username}</td>
				</tr>
			</c:forEach>
		</table>
	</div>
</body>
</html>

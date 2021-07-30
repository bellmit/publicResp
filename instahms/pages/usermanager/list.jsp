<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>

<head>

	<title>Roles and Users - Insta HMS</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">

	<insta:link type="css" file="widgets.css" />
	<insta:link type="css" file="usermanager/usermanagerStyle.css"/>
	<insta:link type="script" file="widgets.js" />
	<insta:link type="script" file="hmsvalidation.js" />
	<insta:link type="js" file="usermanager/userDashBoard.js" />
	<insta:link type="js" file="dashboardsearch.js"/>
	
	<script>
		//var cpath = '${cpath}';
		//var operations = ${operations};
		var extraDetails = [];

		function init() {
			initTooltip('resultTable', extraDetails);
		}

	</script>

</head>

<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="userList" value="${pagedList.dtoList}" />
<c:set var="results" value="${not empty userList}"/>

<body class="yui-skin-sam" onload="autoCompleteForRole();showFilterActive(${results}, document.roleForm);init();ajaxForPrintUrls();">

	<h1>Roles and Users</h1>

	<insta:feedback-panel/>

	<html:form action="/pages/usermanager/UserDashBoard.do" method="GET">


		<input type="hidden" name="_method" value="list">
		<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
		<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>
		<input type="hidden" name="_searchMethod" value="list"/>


		<insta:search form="roleForm" optionsId="optionalFilter" closed="${results}" >
			<table class="formtable" style="height: 5em">
				<tr>
					<td valign="top">Application Role Name:<br><div id="roleName_wrapper" style="width: 15em;">
							<html:text property="roleName" styleId="roleName" />
						<div id="roleName_dropdown"></div></td>
					<td valign="top">User Name:<br><div id="userName_wrapper" style="width: 15em;">
							<html:text property="userName" styleId="userName" />
						<div id="userName_dropdown"></div></td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
				</tr>
			</table>

			<div id="optionalFilter" style="clear: both; display: ${results ? 'none' : 'block'}" >
				<table class="searchFormTable">
					<tr>
						<td>
							<div class="sfLabel">User Status:</div>
							<div class="sfField">
								<html:checkbox property="active">Active</html:checkbox><br />
								<html:checkbox property="inActive">Inactive</html:checkbox><br />
							</div>
						</td>
						<c:choose>
						<c:when test="${genPrefs.map.max_centers_inc_default > 1 && userCenterId == 0}">
							<td class="last">
								<div class="sfLabel">Center:</div>
								<div class="sfField">
									<insta:selectdb name="userCenter" table="hospital_center_master" style="width: 200px;"
								     valuecol="center_id" displaycol="center_name" dummyvalue="-- Select --" value="${param.userCenter}"/>
								</div>
							</td>
						</c:when>
						</c:choose>
						<c:if test="${userCollectionCenter == -1}">
						<td>
							<div class="sfLabel">Collection Center:</div>
							<div class="sfField">

									<c:choose>
										<c:when test="${genPrefs.map.max_centers_inc_default > 1}">
											<c:choose>
												<c:when test="${userCenterId == 0}">
													<insta:selectdb name="CollectionCenter" table="sample_collection_centers" style="width: 200px;"
													valuecol="collection_center_id" displaycol="collection_center" dummyvalue="--Select--" value="${param.CollectionCenter}"/>
												</c:when>
												<c:otherwise>
													<select name="CollectionCenter" id="CollectionCenter" class="dropdown">
														<option value="">--Select--</option>
														<option value="-1" ${param.CollectionCenter == -1?'selected':''}>${defautlCollectionCenter}</option>
														<c:forEach items="${collectionCenters}" var="col_Centers">
															<option value="${col_Centers.map.collection_center_id}" ${col_Centers.map.collection_center_id == param.CollectionCenter?'selected':''}>
																${col_Centers.map.collection_center}
															</option>
														</c:forEach>
													</select>
												</c:otherwise>
											</c:choose>
										</c:when>
										<c:otherwise>
											<insta:selectdb name="CollectionCenter" table="sample_collection_centers" style="width: 200px;"
													valuecol="collection_center_id" displaycol="collection_center" dummyvalue="--Select--" value="${param.CollectionCenter}"/>
										</c:otherwise>
									</c:choose>
							</div>
						</td>
						</c:if>
						<td>
						<div class="sfLabel">Hospital Role:</div>
						<div class="sfField">
							<insta:selectdb name="hospital_roles_master" table="hospital_roles_master" style="width: 200px;"
								     valuecol="hosp_role_id" displaycol="hosp_role_name" dummyvalue="-- Select --" value="${param.hospital_roles_master}"/>
						</div>
						</td>
					</tr>
				</table>
			</div>
		</insta:search>

		<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

		<div class="resultList">
			<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');" style="empty-cells: show">
				<tr onmouseover="hideToolBar();">
					<th>#</th>
					<insta:sortablecolumn name="rolename" title="Application Role Name"/>
					<insta:sortablecolumn name="username" title="User Name"/>
					<th >Hospital Role</th>
					<c:if test="${genPrefs.map.max_centers_inc_default > 1 && userCenterId == 0}">
						<insta:sortablecolumn name="center_name" title="Center Name"/>
					</c:if>
					<th >Last Login</th>
					<th >Total Login Count</th>
				</tr>

				<c:url value="RoleAction.do" var="UrlNewRole">
					<c:param name="method" value="getRoleScreen" />
					<c:param name="creatingNewRole" value="yes" />
				</c:url>
				<c:url value="UserAction.do" var="UrlNewUser">
					<c:param name="method" value="getUserScreen" />
				</c:url>
				<c:forEach var="userRole" items="${userList}" varStatus="st">
					<c:choose>
						<c:when test="${userRole.map.emp_status eq 'I'}">
							<c:set var="flagColor" value="grey_flag"/>
						</c:when>
						<c:otherwise>
							<c:set var="flagColor" value="empty_flag"/>
						</c:otherwise>
					</c:choose>

					<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'resultTable',
							{roleId: '${userRole.map.role_id}', roleName: '${userRole.map.role_name}',
							userName:'${userRole.map.emp_username}', emp_username:'${userRole.map.emp_username}'});"
						onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}">
						<td>${(pagedList.pageNumber-1) *  pagedList.pageSize + st.index + 1}</td>
						<td >
							${userRole.map.role_name}
						</td>
						<td >
							<c:if test="${userRole.map.emp_username ne null}">
								<c:if test="${userRole.map.emp_status eq 'I'}"><img src='${cpath}/images/grey_flag.gif'></c:if>
								<c:if test="${userRole.map.emp_status eq 'A'}"><img src='${cpath}/images/empty_flag.gif'></c:if>
								${userRole.map.emp_username}
							</c:if>
						</td>
						<td>
							${userRole.map.hosp_roles}
						</td>
						<c:choose>
							<c:when test="${genPrefs.map.max_centers_inc_default > 1 && userCenterId == 0}">
								<td>${userRole.map.center_name}</td>
							</c:when>
						</c:choose>
						<td><fmt:formatDate value="${userRole.map.last_login}" pattern="dd-MM-yyyy  HH:mm"/></td>
						<c:choose>
							<c:when test="${userRole.map.total_login > 0}">
								<td >${userRole.map.total_login}</td>
							</c:when>
							<c:otherwise>
								<td ></td>
							</c:otherwise>
						</c:choose>
					</tr>
					<script>

					extraDetails['toolbarRow${st.index}'] = {
						'User Name':<insta:jsString value="${userRole.map.emp_username}"/>,
						'Application Role' :<insta:jsString value="${userRole.map.role_name}"/>,
						'Hospital Roles' : <insta:jsString value="${userRole.map.hosp_roles}"/>,
						};
				</script>

				</c:forEach>
				<insta:noresults hasResults="${results}"/>
			</table>
		</div>

		<div class="screenActions" style="float:left">
			<a href="${UrlNewRole}">Create New Role</a>
				|
			<a href="${UrlNewUser}">Create New User</a>
		</div>

		<div class="legend" style="display: ${results?'block':'none'}" >
			<div class="flag"><img src="${cpath}/images/grey_flag.gif"/></div>
			<div class="flagText">Inactive Users</div>
		</div>

		<script>
			var roleNameList = <%= request.getAttribute("allRoleNames") %>;
			var userNameList = <%= request.getAttribute("allUserNames") %>;
		</script>

	</html:form>
</body>
</html>

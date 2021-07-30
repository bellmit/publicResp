<%@page import="com.insta.hms.core.patient.URLRoute"%>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" scope="request"/>
<c:set var="pagepath" value="<%= URLRoute.USER_WARD %>" />
<jsp:useBean id="currentDate" class="java.util.Date"/>
<html>

<head>
	<title>Edit User Ward Assignment - Insta HMS</title>
	<insta:link type="js" file="shareLoginDialogCommon.js" />
	<insta:link type="css" file="widgets.css" />
	<insta:link type="script" file="widgets.js" />
	<insta:link type="script" file="ajax.js" />
	<insta:link type="script" file="dashboardColors.js" />
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="script" file="wardactivities/userward/userwarddashboard.js"/>
	<insta:js-bundle prefix="registration.patient"/>
	<script type="text/javascript">
		var wards = ${ifn:convertListToJson(wardsJSON)};
		var loggedInUserId = '${ifn:cleanJavaScript(userId)}';
		var curDate = '<fmt:formatDate pattern="dd-MM-yyyy HH:mm" value="${currentDate}"/>';
		function setWardId() {
			document.getElementById('a_ward_id').value = document.getElementById('ward_id').value;
		}
	</script>
</head>
<body onload="nurseInit();">
	<c:set var="hasResults" value="${not empty userWardlist}"/>
	<h1>Edit User Ward Assignment</h1>
	<insta:feedback-panel/>
	<form name="nurseWardForm" method="POST" action="update.htm">
	<c:set var="empRoleName" value="${ifn:cleanHtmlAttribute(param.roleName)}"/>
	<c:set var="empUserDeatils" value="${userWardlist}"/>
	<input type="hidden" name="empusername" id="empusername" value="${empusername}"/>
	<input type="hidden" name="roleId" id="roleId" value="${empRoleId}"/>
	<input type="hidden" name="roleName" id="roleName" value="${empRoleName}"/>
	
		<fieldset class="fieldSetBorder">
		<legend class="fieldSetLabel">Add User Ward</legend>
		<div style="height: 30px;">
			<table>
				<tr>
					<td class="formlabel">Ward Name:</td>
						<td class="forminfo">
							<select name="ward_id" id="ward_id" class="dropdown" onchange="setWardId();">
							<option value=""> --Select-- </option>
							<c:forEach items="${wards }" var="ward">
								<option value="${ward.map.ward_no }" >
									${ward.map.ward_name }
								</option>
							</c:forEach>
							<input type="hidden" name="a_ward_id" id="a_ward_id" value=""/>
						</select>
					</td>
					<td>&nbsp;&nbsp;</td>
					<td style="width: 100px">
						<button type="button" name="btnAddItem" id="btnAddItem" title="Assign Ward To User (Alt_Shift_+)"
						onclick="return addWardToGrid(this);" 
						accesskey="+" class="imgButton"><img src="${cpath}/icons/Add.png"></button>
					</td>
				</tr>
			</table>
		</div>
		<div class="resultList">
		<table class="detailList " cellspacing="0" cellpadding="0" style="margin-top: 5px" id="itemsTable" border="0" width="100%">
				<tr>
					<th>#</th>
					<th>Role Name </th>			
					<th>User Name </th>
					<th>Ward </th>
					<th>Added By</th>
					<th>Date Added</th>
					<th>Action </th>
				</tr>
				<c:set var="numRecords" value="${fn:length(empUserDeatils)}"/>
				<c:forEach begin="1" end="${numRecords+1}" var="i" varStatus="loop">
					<c:set var="record" value="${empUserDeatils[i-1].map}"/>
					<c:if test="${empty record}">
						<c:set var="tr_style" value='style="display:none"'/>
					</c:if>
					<fmt:formatDate pattern="dd-MM-yyyy HH:mm" value="${record.mod_time}" var="datetime"/>
					<tr ${tr_style}>
						<td>${i}
							<input type="hidden" name="h_role_id" id="h_role_id" value="${record.role_id}"/>
							<input type="hidden" name="h_ward_id"  id="h_ward_id" value="${record.ward_id}"/>
							<input type="hidden" name="h_emp_username" id="h_emp_username" value="${record.emp_username}"/>
							<input type="hidden" name="h_ward_name" id="h_ward_name" value="${record.ward_name}"/>
							<input type="hidden" name="h_mode_time" id="h_mode_time" value="${datetime}"/>
							<input type="hidden" name="h_isadded" id="h_isadded" value="false"/>
							<input type="hidden" name="h_delItem" id="h_delItem" value="false"/>
						</td>
						<td>${record.role_name}</td>
						<td>${record.emp_username}</td>
						<td>${record.ward_name}</td>
						<td>${record.username}</td>
						<td>${datetime}</td>
						<td>
							<a href="javascript:Cancel Item" onclick="return cancelItem(this);" title="Cancel care doctor" >
								<img src="${cpath}/icons/delete.gif" class="imgDelete button" />
							</a>
					</td>
				</tr>
			</c:forEach>
	</table>
	</div>
	</fieldset>
	<div>
		<button type="button" accesskey="S" name="save" id="save" onclick="return saveUserWard();"><b><u>S</u></b>ave</button>
		| <a href="${cpath}${pagepath}/list.htm?sortOrder=role_name&sortOrder=emp_username">Nurse/Staff Ward Assignment</a>
	</div>
	</form>
	
</body>
</html>

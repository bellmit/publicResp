<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>

<html>
<head>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="max_centers_inc_default" value='<%=GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default")%>'/>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>${param._method == 'add' ? 'Add' : 'Edit'} Rule - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>
<insta:link type="js" file="widgets.js"/>
<insta:link type="css" file="widgets.css"/>
<insta:link type="script" file="ajax.js"/>
<insta:link type="js" file="dashboardsearch.js"/>
<insta:link type="script" file="/EMRAccessRight/emrAccessRight.js"/>

<script>
var max_centers_inc_default = ${max_centers_inc_default};
var centerId = ${centerId};
//var consultationDocRulesList = ${consultationDocRulesList};
var consultationDocRulesMap = ${consultationDocRulesMap};
var consultationRuleDetailsMap = ${consultationRuleDetailsMap};


</script>

</head>
<body onload="init();">

<form action="EMRAccessRight.do" method="POST" >
	<input type="hidden" name="_method" id="_method" value="${param._method == 'add' ? 'create' : 'update'}">
	<h1>${param._method == 'add' ? 'Add' : 'Edit'} Rule</h1>
	<insta:feedback-panel/>
	<c:if test="${param.rule_type == 'DOC'}">
	<input type="hidden" id="doc_type_id" name="doc_type_id" value="${bean.map.doc_type_id}"/>
	</c:if>
	<input type="hidden" id="rule_type" name="rule_type" value="${param.rule_type == 'DOC' ? 'DOC' : 'ROLE'}"/>
	<input type="hidden" id="rule_id" name="rule_id" value="${emrbean.map.rule_id}"/>
	<c:set var="selectPrompt">
		<insta:ltext key="selectdb.dummy.value"/>
	</c:set>
	<c:set var="longertext">
 		<insta:ltext key="generalmasters.documenttype.accessrightdoctype.accessright.owndocumentsToopTip"/>
 		</c:set>

	<fieldset class="fieldsetborder">
	<c:if test="${param.rule_type == 'DOC'}">
		<legend class="fieldSetLabel"><insta:ltext key="generalmasters.documenttype.accessrightdoctype.documenttypedetails"/></legend>
		<table class="formtable">
			<tr>
				<td class="formlabel">Document Type:</td><td>&nbsp;${bean.map.doc_type_name}</td>
				<td class="formlabel">Status:</td><td>&nbsp;${bean.map.status}</td>
				<td class="formlabel">EMR Symbol:</td><td>&nbsp;${bean.map.prefix}
				</td>
			</tr>
			<c:if test="${bean.map.doc_type_id == 'SYS_CONSULT'}">
			<tr>
			<td class="formlabel"><insta:ltext key="generalmasters.documenttype.accessrightdoctype.accessright.seldocumentssubtype"/>:</td>
			<td>
			<div class="sfField">
			<c:if test="${param._method == 'add'}">
			<insta:selectdb name="doc_sub_type" id="docSubType" table="department" valuecol="dept_id" displaycol="dept_name" orderby="dept_name"
					dummyvalue="${selectPrompt}" values="${paramValues.dept_id}" />
			</c:if>
			<c:if test="${param._method != 'add'}">
			<c:set var="selectedItem" value="${emrbean.map.doc_sub_type}"></c:set>
				<select name="doc_sub_type" id="doc_sub_type" onchange="getDocRuleDetails(this)">
				<option value=""><insta:ltext key="selectdb.dummy.value"/></option>
				  <c:forEach var="item" items="${deptList}" >
				  <option value="${item.map.dept_id}" ${selectedItem == item.map.dept_id ? 'selected="selected"' : ''}>${item.map.dept_name}</option>
				  </c:forEach>
				</select>
				<input type="hidden" id="doc_subtype" name="doc_subtype" value="${emrbean.map.doc_sub_type}"/>
			</c:if>
			</div>
			</td>
			</tr>
			</c:if>
			<c:if test="${bean.map.doc_type_id != 'SYS_CONSULT'}">
				<input type="hidden" id="doc_sub_type" name="doc_sub_type" value=""/>
			</c:if>
		</table>
	</c:if>
	<c:if test="${param.rule_type == 'ROLE'}">
		<legend class="fieldSetLabel"><insta:ltext key="generalmasters.documenttype.accessrightdoctype.roletypedetails"/></legend>
		<table class="formtable">
			<tr>
				<td class="formlabel">Role Name:</td><td>&nbsp;${rolebean.map.role_name}</td>
				<td>&nbsp;<input type="hidden" id="role_id" name="role_id" value="${rolebean.map.role_id}"/></td><td>&nbsp;</td>
				<td>&nbsp;</td><td>&nbsp;</td>
			</tr>
		</table>
	</c:if>
	</fieldset>
	<fieldset class="fieldsetborder">
	<c:if test="${param.rule_type == 'DOC'}">
		<legend class="fieldSetLabel"><insta:ltext key="generalmasters.documenttype.accessrightdoctype.accessibleby"/></legend>
		<table class="formtable">
			<tr>
				<td><div id="allUsr">
					<input type="radio" value="3" ${emrbean.map.user_access == '3'? 'checked': ''} checked name="user_access" id="allUsers"
						onclick="displayUserAndDocDetails(allUsers)">
					<label for="allUsers"><insta:ltext key="generalmasters.documenttype.accessrightdoctype.accessright.anyusers"/></label>
				</div></td>
				<td>&nbsp;</td>
				<td><div id="authOnly">
					<input type="radio" value="2" ${emrbean.map.user_access == '2'? 'checked': ''} name="user_access" id="authorOnly"
						onclick="displayUserAndDocDetails(authorOnly)">
					<label for="authorOnly"><insta:ltext key="generalmasters.documenttype.accessrightdoctype.accessright.authoronly"/></label>
				</div></td>
				<td>&nbsp;</td>
				<td><div id="selUsr">
					<input type="radio" value="1" ${(emrbean.map.user_access == '1' || emrbean.map.user_access == '0')? 'checked': ''} name="user_access" id="selUserOnly"
						onclick="displayUserAndDocDetails(selUserOnly)">
					<label for="selUserOnly"><insta:ltext key="generalmasters.documenttype.accessrightdoctype.accessright.selectuser"/></label>
				</div></td>
			</tr>
			<tr><td>&nbsp;</td></tr>
		</table>
		</c:if>
		<c:if test="${param.rule_type == 'ROLE'}">
		<legend class="fieldSetLabel"><insta:ltext key="generalmasters.documenttype.accessrightdoctype.accessibledocuments"/></legend>
		<table class="formtable">
			<tr>
				<td><div id="allDoc">
					<input type="radio" value="3" ${emrbean.map.doc_access == '3'? 'checked': ''} checked name="doc_access" id="allDocuments"
						onclick="displayUserAndDocDetails(allDocuments)">
					<label for="allDocuments"><insta:ltext key="generalmasters.documenttype.accessrightdoctype.accessright.anydocument"/></label>
				</div></td>
				<td>&nbsp;</td>
				<td><div id="ownDoc">
					<input type="radio" value="2" ${emrbean.map.doc_access == '2'? 'checked': ''} name="doc_access" id="ownerOnly"
						onclick="displayUserAndDocDetails(ownerOnly)">
					<label for="ownerOnly"><insta:ltext key="generalmasters.documenttype.accessrightdoctype.accessright.owndocuments"/></label>
					<img class="imgHelpText" title=<insta:jsString value="${longertext}"/> src="${cpath}/images/help.png"/>
				</div></td>
				<td>&nbsp;</td>
				<td><div id="selDoc">
					<input type="radio" value="1" ${(emrbean.map.doc_access == '1' || emrbean.map.doc_access == '0')? 'checked': ''} name="doc_access" id="selDocOnly"
						onclick="displayUserAndDocDetails(selDocOnly)">
					<label for="selDocOnly"><insta:ltext key="generalmasters.documenttype.accessrightdoctype.accessright.seldocuments"/></label>
				</div></td>
			</tr>
			<tr><td>&nbsp;</td></tr>
		</table>
		</c:if>
		<table>
		<tr>
		<td>
		<c:if test="${param.rule_type == 'DOC'}">
			<div  id="centUsrDisplay" style="display: none; height:260px; width:270px;">
			<fieldset class="fieldsetborder" style="height:250px;">
			<legend class="fieldSetLabel"><insta:ltext key="generalmasters.documenttype.accessrightdoctype.userbelongingto"/></legend>
			</c:if>
			<c:if test="${param.rule_type == 'ROLE'}">
			<div  id="centUsrDisplay" style="display: none; height:260px; width:303px;">
			<fieldset class="fieldsetborder" style="height:250px;">
			<legend class="fieldSetLabel"><insta:ltext key="generalmasters.documenttype.accessrightdoctype.documentbelongingto"/></legend>
			</c:if>
			<table class="formtable">
			<c:choose>
				<c:when test="${max_centers_inc_default > 1}">
					<tr>
					<td><div id="allCent">
						<input type="radio" value="3" ${emrbean.map.center_access == '3'? 'checked': ''} checked name="center_access" id="allCenters"
							onclick="displayCenterDetails(allCenters)">
						<label for="allCenters"><insta:ltext key="generalmasters.documenttype.accessrightdoctype.accessright.anycenteruser"/></label>
					</div></td>
					</tr>
					<tr>
					<td><div id="sameCent">
						<input type="radio" value="2" ${emrbean.map.center_access == '2'? 'checked': ''} name="center_access" id="sameCenterOnly"
							onclick="displayCenterDetails(sameCenterOnly)">
						<label for="sameCenterOnly"><insta:ltext key="generalmasters.documenttype.accessrightdoctype.accessright.samecenteruser"/></label>
					</div></td>
					</tr>
					<tr>
					<td><div id="selCent">
						<input type="radio" value="1" ${emrbean.map.center_access == '1'? 'checked': ''} name="center_access" id="selCentersOnly"
							onclick="displayCenterDetails(selCentersOnly)">
						<label for="selCentersOnly"><insta:ltext key="generalmasters.documenttype.accessrightdoctype.accessright.selectcenteruser"/></label>
					</div></td>
					</tr>
				</c:when>
				<c:when test="${max_centers_inc_default == 1}">
					<tr>
						<td><div id="allCent">
							<input type="radio" value="3" ${emrbean.map.center_access == '3'? 'checked': ''} checked name="center_access" id="allCenters"
								onclick="displayCenterDetails(allCenters)">
							<label for="allCenters"><insta:ltext key="generalmasters.documenttype.accessrightdoctype.accessright.anycenteruser"/></label>
						</div></td>
					</tr>
				</c:when>
			</c:choose>
				<tr id="centerboxDisplay"  style="display: none">
				<td>
					<div class="sfLabel"><insta:ltext key="generalmasters.documenttype.accessrightdoctype.accessright.center"/></div>
					<div class="sfField">
						<c:if test="${param._method == 'add'}">
						<insta:selectdb name="center_id" id="center_id" table="hospital_center_master" valuecol="center_id" displaycol="center_name" orderby="center_name"
							values="${paramValues['center_id']}"  multiple="true" size="8" class="listbox" style="height: 100px; width:180px;"/>
						<input type="hidden" name="center_id@type" value="text"/>
						<input type="hidden" name="center_id@op" value="in"/>
						</c:if>
						<c:if test="${param._method != 'add'}">
							<select name="center_id" id="center_id" multiple="multiple" size="8" class="listbox" style="height: 100px; width:180px;">
							  <c:forEach var="centerItem" items="${centList}" >
							  <c:set var="selected" value=""/>
							  <c:forEach var="centerEmrItem" items="${emrDetailsList}" >
							   <c:if test="${(centerEmrItem.map.entity_type == 'C') && (centerEmrItem.map.entity_id eq centerItem.map.center_id)}">
							   <c:set var="selected" value="selected"/>
							   </c:if>
							   </c:forEach>
							  <option value="${centerItem.map.center_id}" ${selected} title="${centerItem.map.center_name}">${centerItem.map.center_name}</option>
							  </c:forEach>
							</select>
						</c:if>
					</div>
				</td>
				</tr>
			</table>
			</fieldset>
			</div>
		</td>
		<td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
		<td>
		<c:if test="${param.rule_type == 'DOC'}">
		<div id="deptUsrDisplay" style="display: none; height:260px; width:270px;">
		<fieldset class="fieldsetborder" style="height:250px;" >
			<legend class="fieldSetLabel"><insta:ltext key="generalmasters.documenttype.accessrightdoctype.userbelongingto"/></legend>
		</c:if>
		<c:if test="${param.rule_type == 'ROLE'}">
		<div id="deptUsrDisplay" style="display: none; height:260px; width:303px;">
		<fieldset class="fieldsetborder" style="height:250px;" >
			<legend class="fieldSetLabel"><insta:ltext key="generalmasters.documenttype.accessrightdoctype.documentbelongingto"/></legend>
		</c:if>
		<table class="formtable">
				<tr>
				<td><div id="allDept">
					<input type="radio" value="3" ${emrbean.map.dept_access == '3'? 'checked': ''} checked name="dept_access" id="allDepartments"
						onclick="displayDepartmentDetails(allDepartments)">
					<label for="alldepartments"><insta:ltext key="generalmasters.documenttype.accessrightdoctype.accessright.anydepartmentuser"/></label>
				</div></td>
				</tr>
				<tr>
				<td><div id="sameDept">
					<input type="radio" value="2" ${emrbean.map.dept_access == '2'? 'checked': ''} name="dept_access" id="sameDepartmentOnly"
						onclick="displayDepartmentDetails(sameDepartmentOnly)">
					<label for="samedepartmentOnly"><insta:ltext key="generalmasters.documenttype.accessrightdoctype.accessright.samedepartmentuser"/></label>
				</div></td>
				</tr>
				<tr>
				<td><div id="selDept">
					<input type="radio" value="1" ${emrbean.map.dept_access == '1'? 'checked': ''} name="dept_access" id="selDepartmentsOnly"
						onclick="displayDepartmentDetails(selDepartmentsOnly)">
					<label for="selDepartmentsOnly"><insta:ltext key="generalmasters.documenttype.accessrightdoctype.accessright.selectdepartmentuser"/></label>
				</div></td>
				</tr>
			<tr id="deptboxDisplay" style="display: none" >
			<td>
				<div class="sfLabel"><insta:ltext key="generalmasters.documenttype.accessrightdoctype.accessright.department"/></div>
				<div class="sfField">
					<c:if test="${param._method == 'add'}">
					<insta:selectdb name="dept_id" id="dept_id" table="department" valuecol="dept_id" displaycol="dept_name" orderby="dept_name"
						values="${paramValues['dept_id']}" multiple="true" size="8" class="listbox" style="height: 100px; width:180px;"/>
					<input type="hidden" name="dept_id@type" value="text"/>
					<input type="hidden" name="dept_id@op" value="in"/>
					</c:if>
					<c:if test="${param._method != 'add'}">
						<select name="dept_id" id="dept_id" multiple="multiple" size="8" class="listbox" style="height: 100px; width:180px;">
						  <c:forEach var="deptItem" items="${deptList}" >
						  <c:set var="selected" value=""/>
						  <c:forEach var="deptEmrItem" items="${emrDetailsList}" >
						   <c:if test="${(deptEmrItem.map.entity_type == 'D') && (deptEmrItem.map.entity_id eq deptItem.map.dept_id)}">
						   <c:set var="selected" value="selected"/>
						   </c:if>
						   </c:forEach>
						  <option value="${deptItem.map.dept_id}" ${selected} title="${deptItem.map.dept_name}">${deptItem.map.dept_name}</option>
						  </c:forEach>
						</select>
					</c:if>
				</div>
			</td>
		</tr>
		</table>
	</fieldset>
	</div>
	</td>
	<c:if test="${param.rule_type == 'DOC'}">
	<td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
	<td>
			<div id="roleUsrDisplay" style="display: none; height:260px; width:369px;">
			<fieldset class="fieldsetborder" style="height:250px;">
			<legend class="fieldSetLabel"><insta:ltext key="generalmasters.documenttype.accessrightdoctype.userbelongingto"/></legend>
			<table class="formtable">
					<tr>
					<td><div id="allRol">
						<input type="radio" value="3" ${emrbean.map.role_access == '3'? 'checked': ''} checked name="role_access" id="allroles"
							onclick="displayRoleDetails(allroles)">
						<label for="allroles"><insta:ltext key="generalmasters.documenttype.accessrightdoctype.accessright.anyroleuser"/></label>
					</div></td>
					</tr>
					<tr>
					<td><div id="sameRol">
						<input type="radio" value="2" ${emrbean.map.role_access == '2'? 'checked': ''} name="role_access" id="sameRoleOnly"
							onclick="displayRoleDetails(sameRoleOnly)">
						<label for="sameRoleOnly"><insta:ltext key="generalmasters.documenttype.accessrightdoctype.accessright.sameroleuser"/></label>
					</div></td>
					</tr>
					<tr>
					<td><div id="selRol">
						<input type="radio" value="1" ${emrbean.map.role_access == '1'? 'checked': ''} name="role_access" id="selRoleOnly"
							onclick="displayRoleDetails(selRoleOnly)">
						<label for="selRoleOnly"><insta:ltext key="generalmasters.documenttype.accessrightdoctype.accessright.selectroleuser"/></label>
					</div></td>
					<td><div id="selusers">
						<input type="radio" value="4" ${(emrbean.map.role_access == '4' || emrbean.map.role_access == '0')? 'checked': ''} name="role_access" id="selUsersOnly"
							onclick="displayRoleDetails(selUsersOnly)">
						<label for="selUsersOnly"><insta:ltext key="generalmasters.documenttype.accessrightdoctype.accessright.selectuser"/></label>
					</div></td>
					</tr>
				<tr>
				<td id="roleboxDisplay" style="display: none">
					<div class="sfLabel"><insta:ltext key="generalmasters.documenttype.accessrightdoctype.accessright.roles"/></div>
					<div class="sfField">
					<c:if test="${param._method == 'add'}">
						<insta:selectdb name="role_id" id="role_id" table="emr_access_u_role_view" valuecol="role_id" displaycol="role_name" filtercol="role_status" orderby="role_name"
							values="${paramValues['role_id']}" multiple="true" size="8" class="listbox" style="height: 100px; width:165px;"/>
						<input type="hidden" name="role_id@type" value="text"/>
						<input type="hidden" name="role_id@op" value="in"/>
						</c:if>
					<c:if test="${param._method != 'add'}">
						<select name="role_id" id="role_id" multiple="multiple" size="8" class="listbox" style="height: 100px; width:165px;">
						  <c:forEach var="roleItem" items="${roleList}" >
							  <c:if test="${roleItem.map.role_id != '1' && roleItem.map.role_id != '2'}">
									  <c:set var="selected" value=""/>
									  <c:forEach var="roleEmrItem" items="${emrDetailsList}" >
										   <c:if test="${(roleEmrItem.map.entity_type == 'R') && (roleEmrItem.map.entity_id eq roleItem.map.role_id)}">
										   		<c:set var="selected" value="selected"/>
										   </c:if>
									   </c:forEach>
									  <option value="${roleItem.map.role_id}" ${selected} title="${roleItem.map.role_name}">${roleItem.map.role_name}</option>
								</c:if>
						  </c:forEach>
						</select>
					</c:if>
					</div>
				</td>
				<td>&nbsp;</td>
				<td id="userboxDisplay" style="display: none">
					<div class="sfLabel"><insta:ltext key="generalmasters.documenttype.accessrightdoctype.accessright.users"/></div>
					<div class="sfField">
					<c:if test="${param._method == 'add'}">
						<insta:selectdb name="emp_username" id="emp_username" table="emr_access_u_user_view" valuecol="emp_username" displaycol="emp_username" filtercol="emp_status" orderby="emp_username"
							values="${paramValues['emp_username']}" multiple="true" size="8" class="listbox" style="height: 100px; width:165px;"/>
						<input type="hidden" name="emp_username@type" value="text"/>
						<input type="hidden" name="emp_username@op" value="in"/>
					</c:if>
					<c:if test="${param._method != 'add'}">
						<select name="emp_username" id="emp_username" multiple="multiple" size="8" class="listbox" style="height: 100px; width:165px;">
						  <c:forEach var="userItem" items="${userList}" >
						  <c:if test="${userItem.map.role_id != '1' && userItem.map.role_id != '2'}">
							  <c:set var="selected" value=""/>
							  <c:forEach var="userEmrItem" items="${emrDetailsList}" >
								   <c:if test="${(userEmrItem.map.entity_type == 'U') && (userEmrItem.map.entity_id eq userItem.map.emp_username)}">
								   	<c:set var="selected" value="selected"/>
								   </c:if>
							   </c:forEach>
							  <option value="${userItem.map.emp_username}" ${selected} title="${userItem.map.emp_username}">${userItem.map.emp_username}</option>
							</c:if>
						 </c:forEach>
						</select>
					</c:if>
					</div>
				</td>
				</tr>
			</table>
			</fieldset>
			</div>
		</td>
		</c:if>
		<c:if test="${param.rule_type == 'ROLE'}">
		<td>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</td>
		<td>
			<div id="docTypeDisplay" style="display: none; height:260px; width:303px;">
			<fieldset class="fieldsetborder" style="height:250px;">
			<legend class="fieldSetLabel"><insta:ltext key="generalmasters.documenttype.accessrightdoctype.documentoftype"/></legend>
			<table class="formtable">
					<tr>
					<td><div id="seldocs">
						<input type="checkbox" value="4" ${(emrbean.map.role_access == '4' )? 'checked': ''}  name="role_access" id="selDocsOnly"
							onclick="displayRoleDetails(selDocsOnly)">
						<label for="selDocsOnly"><insta:ltext key="generalmasters.documenttype.accessrightdoctype.accessright.seldocumentstype"/></label>
					</div></td>
					<input type="hidden" name="role_access" id="role_access" value="0"/>
					</tr>
				<tr>
				<td id="docboxDisplay" style="display: none">
					<div class="sfLabel"><insta:ltext key="generalmasters.documenttype.accessrightdoctype.accessright.documents"/></div>
					<div class="sfField">
					<c:if test="${param._method == 'add'}">
						<insta:selectdb name="doc_type_id" id="doc_type_id" table="doc_type" valuecol="doc_type_id" displaycol="doc_type_name" orderby="doc_type_name"
							values="${paramValues['doc_type_id']}" multiple="true" size="8" class="listbox" style="height: 158px; width:180px;"/>
						<input type="hidden" name="doc_type_id@type" value="text"/>
						<input type="hidden" name="doc_type_id@op" value="in"/>
					</c:if>
					<c:if test="${param._method != 'add'}">
						<select name="doc_type_id" id="doc_type_id" multiple="multiple" size="8" class="listbox" style="height: 158px; width:180px;">
						  <c:forEach var="docItem" items="${documentList}" >
						  <c:set var="selected" value=""/>
						  <c:forEach var="docEmrItem" items="${emrDetailsList}" >
						   <c:if test="${(docEmrItem.map.entity_type == 'T') && (docEmrItem.map.entity_id eq docItem.map.doc_type_id)}">
						   <c:set var="selected" value="selected"/>
						   </c:if>
						   </c:forEach>
						  <option value="${docItem.map.doc_type_id}" ${selected} title="${docItem.map.doc_type_name}">${docItem.map.doc_type_name}</option>
						  </c:forEach>
						</select>
					</c:if>
					</div>
				</td>
				</tr>
			</table>
			</fieldset>
			</div>
		</td>
		</c:if>
		</tr>
		</table>
		</fieldset>

	<table class="screenActions">
	<tr>
		<td><insta:accessbutton buttonkey="generalmasters.documenttype.accessrightdoctype.accessright.save" type="submit" onclick="return validate();"/></td>
		<!--<c:if test="${param.rule_type == 'DOC'}">
		<td>&nbsp;|&nbsp;</td>
		<td>
			<c:url var="url" value="EMRAccessRight.do">
				<c:param name="_method" value="list"/>
				<c:param name="doc_type_id" value="${bean.map.doc_type_id}"/>
			</c:url>
			<a href="<c:out value='${url}' />"">Access Rule List</a>
		</td>
		</c:if>
		--><td>&nbsp;|&nbsp;</td>
		<td><a href="${cpath}/master/documenttypes/list.htm?status=A&sortReverse=false&sortOrder=system_type">Document Type List</a></td>
		<td>&nbsp;|&nbsp;</td>
		<td><a href="${cpath}/pages/usermanager/UserDashBoard.do?_method=list&filterClosed=true&sortOrder=rolename&sortReverse=true&hospital=on&active=on">User/Role List</a></td>
	</tr>
	</table>

</form>

</body>
</html>

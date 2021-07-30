<%@page import="com.insta.hms.master.URLRoute"%>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="pagePath" value="<%=URLRoute.CENTER_GROUP%>"/>
<c:set var="max_centers" value='<%= GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default") %>' scope="request"/>

<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
		<title>Add/Edit ${entityName} Center Applicability - Insta HMS</title>
		<insta:link type="script" file="master/CenterGroup/center_group_applicability.js"/>
		<insta:link type="script" file="master/CenterAssociation/center_applicability.js"/>
		<script>
			function doClose() {
				window.location.href = "${cpath}/${pagePath}/list.htm?sortOrder=center_group_name&sortReverse=false&status=A";
			}
			var max_centers = ${max_centers};
			var centersJSON = ${max_centers} > 1 ? ${ifn:convertListToJson(centers_json)}: null;
			var citiesJSON = ${ifn:convertListToJson(cities_json)};
			var theForm = "center_group_association_form";
			var userCenterGroups = ${ifn:convertListToJson(userGroupsList)};
			var primaryKeyName = "center_group_id";
		</script>
		<style>
			table.add_center_table td {
				padding: 5px 0px 2px 5px;
			}
		</style>
	</head>
	<body onload="init();">
		<h1>Add Center Group Applicability </h1>
		<insta:feedback-panel/>
		<c:set var="actionUrl" value="create.htm"/>
		<form action="${actionUrl}" name="center_group_association_form" method="POST">
		<table class="formtable">
			<tr>
				<td class="formlabel">Center Group Name:</td>
				<td>
					<input type="text" id ="center_group_name" name="center_group_name" value="${bean.center_group_name}"   maxlength="120"/><span class="star">*</span>
				</td>

				<td class="formlabel">Status:</td>
				<td><insta:selectoptions name="status" value="${bean.status}" opvalues="A,I" optexts="Active,Inactive" /></td>
			</tr>
		</table>
			<input type="hidden" name="_method" value="updateCenterGroupAssociation"/>
			<input type="hidden" name="entity_id_column_name" value="center_group_id"/>
			<input type="hidden" name="entity_id" value="${bean.center_group_id }"/>
			<fieldset class="fieldSetBorder" style="display: ${max_centers > 1 ? 'block' : 'none'}">
				<legend class="fieldSetLabel">Centers</legend>
				<c:set var="app_centers_count" value="${fn:length(applicable_centers)}"/>
				<c:set var="app_for_all_centers" value="${app_centers_count > 0 && applicable_centers[0].center_id == 0 ? true : applicable_centers[0].center_id eq null}"/>
				<table style="margin-top: 10px" class="detailList dialog_displayColumns" cellspacing="0" cellpadding="0"
					id="centers_table" border="0" width="100%">
					<tr>
						<th>State</th>
						<th>City</th>
						<th>Center</th>
						<th style="width: 16px"></th>
					</tr>
			 		<c:forEach begin="1" end="${app_for_all_centers ? 1 : app_centers_count+1}" var="i" varStatus="loop">
						<c:set var="app_center" value="${applicable_centers[(app_for_all_centers ? 1 : i-1)]}"/>
						<tr style="display: ${empty app_center ? 'none' : 'table-row'}">
							<td>
								<input type="hidden" name="center_group_assoc_id" value=""/>
							    <input type="hidden" name="center_group_id" value="${app_center.center_group_id}"/>
								<input type="hidden" name="center_id" value="${app_center.center_id}"/>
								<input type="hidden" name="center_name" value="${app_center.center_name}"/>
								<input type="hidden" name="state_name" value="${app_center.state_name}"/>
								<input type="hidden" name="city_name" value="${app_center.city_name}"/>
								<input type="hidden" name="cntr_delete" id="cntr_delete" value="false" />
								<label>${app_center.state_name}</label>
							</td>
							<td>${app_center.city_name}</td>
							<td>${app_center.center_name}</td>
							<td style="text-align: center">
								<a href="javascript:Cancel Center" onclick="return cancelCenterDetails(this);" title="Cancel Center" >
									<img src="${cpath}/icons/delete.gif" class="imgDelete button" />
								</a>
							</td>
						</tr>
					</c:forEach>
				</table>
				<table class="addButton" style="height: 25px;">
					<tr>
						<td style="width: 16px;text-align: right">
							<button type="button" name="btnAddCenter" id="btnAddCenter" title="Add Center (Alt_Shift_+)"
								onclick="showAddCenterDialog(this); return false;"
								accesskey="+" class="imgButton">
									<img src="${cpath}/icons/Add.png" id="centerAddIconEnabled"/>
							</button>
						</td>
					</tr>
				</table>
			</fieldset>
			<table style="margin-top: 10px">
				<tr>
					<td>
						<button type="submit" accesskey="S" onclick="return saveForm('center_group_association_form');"><b><u>S</u></b>ave</button>
						| <a href="javascript:void(0)" onclick="doClose();">Center Group List</a>
					 </td>
				</tr>
			</table>
			<div id="addCenterDialog" style="display: none">
				<div class="bd" style="width: 430px;" >
					<fieldset class="fieldSetBorder">
						<legend class="fieldSetLabel">Add Center</legend>
						<table class="add_center_table">
							<tr>
								<td >State: </td>
								<td><insta:selectdb name="d_state" id="d_state" table="state_master" valuecol="state_id" displaycol="state_name"
										filtered="true" dummyvalue="-- Select --" dummyvalueid="" onchange="return populateCities();" orderby="state_name"/></td>
								<td rowspan="2" valign="middle">
									<div style="float: left; padding-top: 5px; margin-left: 10px">(OR) Show All Centers</div>
									<div style="float: left">
										<input type="checkbox" id="show_all_centers_chkbox" onclick="showAllCenters(this)"/>
									</div>
								</td>
							</tr>
							<tr>
								<td >City: </td>
								<td><select name="d_city" id="d_city" class="dropdown">
										<option value="">-- Select --</option>
									</select>
								</td>
							</tr>
							<tr>
								<td colspan="3">
									<div style="float: right">
										<input type="button" id="d_search_centers" value="Search" />
									</div>

									<div style="float: right; padding-top: 5px" id="paginationDiv">
									</div>
								</td>
							</tr>
						</table>
						<table id="avlbl_centers_table" class="detailList" style="margin-top: 10px">
							<tr>
								<th style="padding-top: 0px;padding-bottom: 0px">
									<input type="checkbox" id="d_checkAllCenters" onclick="return checkOrUncheckAll('d_center_chkbox', this)"/>
								</th>
								<th>State</th>
								<th>City</th>
								<th>Center</th>
							</tr>
							<tr style="display: none">
								<td>
									<input type="checkbox" name="d_center_chkbox" value=""/>
									<input type="hidden" name="d_state_name" value=""/>
									<input type="hidden" name="d_city_name" value=""/>
									<input type="hidden" name="d_center_name" value=""/>
								</td>
								<td></td>
								<td></td>
								<td></td>
							</tr>
						</table>
					</fieldset>
					<table style="margin-top: 10px">
						<tr>
							<td>
								<input type="button" id="d_center_ok" value="Ok"/>
								<input type="button" id="d_center_cancel" value="Cancel"/>
							</td>
						</tr>
					</table>
				</div>
			</div>
			<div id="editCenterDialog" style="display: none">
				<div class="bd">
					<fieldset class="fieldSetBorder">
						<legend class="fieldSetLabel">Edit Center</legend>
						<input type="hidden" id="center_edit_row_id" value=""/>
						<table class="formtable">
							<tr>
								<td class="formlabel">Center: </td>
								<td class="forminfo"><label id="ed_center_label"></label></td>
							</tr>
							<tr>
								<td class="formlabel">State: </td>
								<td class="forminfo"><label id="ed_state_label"></label></td>
							</tr>
							<tr>
								<td class="formlabel">City: </td>
								<td class="forminfo"><label id="ed_city_label"></label></td>
							</tr>
						</table>
					</fieldset>
					<table style="margin-top: 10px">
						<tr>
							<td>
								<input type="button" id="edit_center_ok" value="Ok"/>
								<input type="button" id="edit_center_cancel" value="Cancel"/>
								<input type="button" id="edit_center_previous" value="&lt;&lt;Previous" />
								<input type="button" id="edit_center_next" value="Next&gt;&gt;"/>
							</td>
						</tr>
					</table>
				</div>
			</div>
			
		</form>
	</body>
</html>

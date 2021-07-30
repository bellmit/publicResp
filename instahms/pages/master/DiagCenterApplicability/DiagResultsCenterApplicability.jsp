<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="max_centers" value='<%= GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default") %>' scope="request"/>

<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
		<title>Add/Edit Diagnostic Results Center Applicability - Insta HMS</title>
		<insta:link type="script" file="master/DiagResultCentersApplicability/diagresultscenterapplicability.js"/>
		<script type="text/javascript">
			var max_centers = ${max_centers};
			var centersJSON = ${max_centers} > 1 ? ${centers_json} : null;
			var citiesJSON = ${cities_json};
			var applicable_centers_JSON = ${applicable_centers_json};
			var resultsJSON = ${results_json};
			var testExpressions = ${expression_JSON};
		</script>
		<style type="text/css">
			table.add_center_table td {
				padding: 5px 0px 2px 5px;
			}
		</style>
	</head>
	<body onload="init();">
		<h1>Diagnostic Results Center Applicability</h1>
		<insta:feedback-panel/>
		<form action="diagresultcenterapplicability.do" name="diag_center_association_form" method="POST">
		<input type="hidden" name="_method" value="update"/>
		<input type="hidden" name="test_id" id="test_id" value="${testDeatils.map.test_id }"/>
		<input type="hidden" name="orgId" id="orgId" value="${testDeatils.map.org_id }"/>
		<input type="hidden" name="resultLabelIdSaved" id="resultLabelIdSaved" value="${ifn:cleanHtmlAttribute(param.resultlabel_id)}"/>
		
		<fieldset class="fieldSetBorder"><legend class="fieldSetLabel">Test Details</legend>
			<table class="formtable">
				<tr>
					<td class="formlabel" style="width:70px;">Test Name:</td>
					<td class="forminfo" style="width : 300px">${testDeatils.map.test_name }</td>
					<td class="formlabel">Department:</td>
					<td class="forminfo">${testDeatils.map.ddept_name }</td>
				</tr>
			</table>
		</fieldset>
		<table class="formtable" cellpadding="0" cellspacing="0" width="100%" border="0">
			<tr>
				<td class="formlabel">Filter Result Labels:</td>
				<td align="left" colspan="2">
					<select name="resultlabel_id" id="resultlabel_id" style="width: 200px;" class="dropdown" onchange="hideresultApplicability();filterTableByLabel();getNumRows();changeDropDown();">
						<option value="">---Select---</option>
						<c:forEach items="${dynaResultLblList}" var="resultLblBean">
							<option value="${resultLblBean.map.resultlabel_id}" >${resultLblBean.map.resultlabel}${not empty resultLblBean.map.method_name ? ' (' : ''}${not empty resultLblBean.map.method_name ? resultLblBean.map.method_name : '' }${not empty resultLblBean.map.method_name ? ')' : ''}</option>
						</c:forEach>
					</select>
				</td>
			</tr>
		</table>	
			
			<fieldset class="fieldSetBorder" style="display: ${max_centers > 1 ? 'block' : 'none'}">
			<legend class="fieldSetLabel">Centers</legend>
				<c:set var="app_centers_count" value="${fn:length(applicable_centers)}"/>
				<div id="hideandShow">
				<div>
					<div style="float: left"><input type="radio" name="applicable_for_centers" id="applicableall" value="all"  onchange="allowAddingCenter(this.value);hideresultApplicability();changeGridStatus();"/></div>
					<div style="padding-top: 5px; float: left">Result associated with all centers</div>
				</div>
				<div style="clear: both"></div>
				<div style="margin-top: 10px; margin-left: 100px">OR</div>
				<div style="margin-top: 10px">
					<div style="float: left"><input type="radio" name="applicable_for_centers" id="applicablefew" value="few"  onchange="allowAddingCenter(this.value);hideresultApplicability();"/></div>
					<div style="padding-top: 5px; float: left">Result applicable only for following centers</div>
				</div>
				<div style="clear: both"></div>
					</div>
				<c:set var="loggedInCenter" value="${loggedInCenter}"/>
				<table style="margin-top: 10px" class="detailList dialog_displayColumns" cellspacing="0" cellpadding="0"
					id="centers_table" border="0" width="100%">
					<tr>
						<th>State</th>
						<th>City</th>
						<th>Center</th>
						<th>Status</th>
						<th style="width: 16px"></th>
						<th style="width: 16px"></th>
					</tr>
			 		<c:forEach begin="1" end="${app_for_all_centers ? 1 : app_centers_count+1}" var="i" varStatus="loop">
						<c:set var="app_center" value="${applicable_centers[(app_for_all_centers ? 1 : i-1)].map}"/>
						<tr style="display: ${empty app_center ? 'none' : 'table-row'}">
							<td>
								<input type="hidden" name="result_center_id" id="result_center_id" value="${app_center.result_center_id}"/>
								<input type="hidden" name="center_id" id ="center_id" value="${app_center.center_id}"/>
								<input type="hidden" name="center_name" id="center_name" value="${app_center.center_name}"/>
								<input type="hidden" name="state_name" id="state_name" value="${app_center.state_name}"/>
								<input type="hidden" name="city_name"  id="city_name" value="${app_center.city_name}"/>
								<input type="hidden" name="center_status" id="center_status" value="${app_center.status}"/>
								<input type="hidden" name="cntr_delete" id="cntr_delete" value="false" />
								<input type="hidden" name="cntr_edited" id="cntr_edited" value="false"/>
								<input type="hidden" name="resultlabel_id_grid" id="resultlabel_id_grid" value="${ifn:cleanHtmlAttribute(app_center.resultlabel_id) }"/>
								<label>${app_center.state_name}</label>
							</td>
							<td>${app_center.city_name}</td>
							<td>${app_center.center_name}</td>
							<td>${app_center.status == 'A' ? 'Active' : 'Inactive'}</td>
							<td style="text-align: center">
								<a href="javascript:Cancel Center" onclick="return cancelCenter(this);" title="Cancel Center" >
									<img src="${cpath}/icons/delete.gif" id ="imgDelete" name="imgDelete" class="imgDelete button" alt=""/>
								</a>
							</td>
							<td style="text-align: center">
								<a name="_editCenterAnchor" href="javascript:Edit" onclick="return showEditCenterDialog(this);"
									title="Edit Center Details">
									<img src="${cpath}/icons/Edit.png" class="button" alt=""/>
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
									<img src="${cpath}/icons/Add.png" id="centerAddIconEnabled" alt=""/>
									<img src="${cpath}/icons/Add1.png" id="centerAddIconDisabled" alt=""/>
							</button>
						</td>
					</tr>
				</table>
			</fieldset>
			<table style="margin-top: 10px">
				<tr>
					<td>
						<input type="button" name="Save" value="Save"
							onclick="return saveForm();">
							<c:url value="/masters/hosp/diagnostics/addtest.do" var="diagResultsListUrl">
								<c:param name="_method" value="list"/>
								<c:param name="status" value="A"/>
								<c:param name="sortReverse" value="false"/>
								<c:param name="org_id" value="ORG0001"/>
							</c:url>
						 | <a href="${cpath }/master/addeditdiagnostics/show.htm?&testid=${testDeatils.map.test_id }&orgId=${testDeatils.map.org_id }">Test Details
						   </a>
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
							<tr>
								<td class="formlabel">Status: </td>
								<td class="forminfo">
									<select id="ed_center_status" class="dropdown" onchange="setCenterFieldEdited();">
										<option value="A">Active</option>
										<option value="I">Inactive</option>
									</select>
								</td>
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

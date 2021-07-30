<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="max_centers" value='<%= GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default") %>' scope="request"/>
<c:set var="pkg_approval_rights" value="${actionRightsMap.package_approval}"/>

<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
		<title>Add/Edit Package Applicability - Insta HMS</title>
		<insta:link type="script" file="master/PackageApplicability/packageapplicability.js"/>
		<script>
			var max_centers = ${max_centers};
			var centersJSON = ${max_centers} > 1 ? ${centers_json} : null;
			var citiesJSON = ${cities_json};
			var packId = ${pack_bean.map.package_id};
		</script>
		<style>
			table.add_center_table td {
				padding: 5px 0px 2px 5px;
			}
		</style>
	</head>
	<body onload="init();">
		<h1>Package Applicability - ${pack_bean.map.package_name}</h1>
		<insta:feedback-panel/>
		<form action="PackageApplicabilityAction.do" name="package_applicable_form" method="POST">
			<input type="hidden" name="_method" value="update"/>
			<input type="hidden" name="package_id" value="${ifn:cleanHtmlAttribute(param.packId)}"/>
			<input type="hidden" name="multi_visit_package" value="${ifn:cleanHtmlAttribute(param.multi_visit_package)}"/>
			<input type="hidden" name="org_id" value="${ifn:cleanHtmlAttribute(param.org_id)}"/>
			<fieldset class="fieldSetBorder" style="display: ${max_centers > 1 ? 'block' : 'none'}">
				<legend class="fieldSetLabel">Centers</legend>
				<c:set var="app_centers_count" value="${fn:length(applicable_centers)}"/>
				<div >
					<c:set var="app_for_all_centers" value="${app_centers_count > 0 && applicable_centers[0].map.center_id == -1}"/>
					<div style="float: left"><input type="radio" name="applicable_for_centers" value="all" ${app_for_all_centers ? 'checked' : ''} onchange="allowAddingCenter(this.value)"/></div>
					<div style="padding-top: 5px; float: left">Package applicable for all centers</div>
				</div>
				<div style="clear: both"></div>
				<div style="margin-top: 10px; margin-left: 100px">OR</div>
				<div style="margin-top: 10px">
					<div style="float: left"><input type="radio" name="applicable_for_centers" value="few" ${app_for_all_centers ? '' : 'checked'} onchange="allowAddingCenter(this.value)"/></div>
					<div style="padding-top: 5px; float: left">Package applicable only for following centers</div>
				</div>
				<div style="clear: both"></div>
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
								<input type="hidden" name="package_center_id" value="${app_center.package_center_id}"/>
								<input type="hidden" name="center_id" value="${app_center.center_id}"/>
								<input type="hidden" name="center_name" value="${app_center.center_name}"/>
								<input type="hidden" name="state_name" value="${app_center.state_name}"/>
								<input type="hidden" name="city_name" value="${app_center.city_name}"/>
								<input type="hidden" name="center_status" value="${app_center.status}"/>
								<input type="hidden" name="cntr_delete" id="cntr_delete" value="false" />
								<input type="hidden" name="cntr_edited" id="cntr_edited" value="false"/>
								<label>${app_center.state_name}</label>
							</td>
							<td>${app_center.city_name}</td>
							<td>${app_center.center_name}</td>
							<td>${app_center.status == 'A' ? 'Active' : 'Inactive'}</td>
							<td style="text-align: center">
								<a href="javascript:Cancel Center" onclick="return cancelCenter(this);" title="Cancel Center" >
									<img src="${cpath}/icons/delete.gif" class="imgDelete button" />
								</a>
							</td>
							<td style="text-align: center">
								<a name="_editCenterAnchor" href="javascript:Edit" onclick="return showEditCenterDialog(this);"
									title="Edit Center Details">
									<img src="${cpath}/icons/Edit.png" class="button" />
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
									<img src="${cpath}/icons/Add1.png" id="centerAddIconDisabled"/>
							</button>
						</td>
					</tr>
				</table>
			</fieldset>
			<fieldset class="fieldSetBorder" style="margin-top: 20px">
				<legend class="fieldSetLabel">Sponsors</legend>
				<c:set var="sponsors_count" value="${fn:length(applicable_sponsors)}"/>
				<div>
					<c:set var="app_for_all_tpas" value="${ applicable_sponsors[0].map.tpa_id == '-1'}"/>
					<div style="float: left"><input type="radio" name="applicable_for_sponsors" id="app_for_all_sponsor" value="all" ${app_for_all_tpas ? 'checked' : ''} onchange="allowAddingSponsor(this.value);"/></div>
					<div style="padding-top: 5px; float: left">Applicable for All(Sponsor & Non Sponsor)</div>
				</div>
				<div style="clear: both"></div>
				<div style="margin-top: 10px; margin-left: 100px">OR</div>
				<div >
					<c:set var="app_for_none_tpas" value="${ applicable_sponsors[0].map.tpa_id == '0'}"/>
					<div style="float: left"><input type="radio" name="applicable_for_sponsors" id='app_for_none_sponsor' value="none" ${app_for_none_tpas ? 'checked' : ''} onchange="allowAddingSponsor(this.value);"/></div>
					<div style="padding-top: 5px; float: left">Applicable for none of the sponsors</div>
				</div>
				<div style="clear: both"></div>
				<div style="margin-top: 10px; margin-left: 100px">OR</div>
				<div style="margin-top: 10px">
					<div style="float: left"><input type="radio" name="applicable_for_sponsors" id='app_for_few_sponsor' value="few" ${app_for_all_tpas || app_for_none_tpas ? '' : 'checked'} onchange="allowAddingSponsor(this.value);"/></div>
					<div style="padding-top: 5px; float: left">Applicable for the following sponsors</div>
				</div>
				<div style="clear: both"></div>
				<table style="margin-top: 10px" class="detailList dialog_displayColumns" cellspacing="0" cellpadding="0"
					id="sponsors_table" border="0" width="100%">
					<tr>
						<th>Sponsor</th>
						<th>Status</th>
						<th style="width: 16px"></th>
						<th style="width: 16px"></th>
					</tr>
					<c:forEach begin="1" end="${app_for_all_tpas || app_for_none_tpas ? 1 : sponsors_count+1}" var="i" varStatus="loop">
						<c:set var="app_sponsor" value="${applicable_sponsors[(app_for_all_tpas || app_for_none_tpas ? 1 : i-1)].map}"/>
						<tr style="display: ${empty app_sponsor ? 'none' : 'table-row'}">
							<td>
								<input type="hidden" name="tpa_id" value="${app_sponsor.tpa_id}"/>
								<input type="hidden" name="tpa_name" value="${app_sponsor.tpa_name}"/>
								<input type="hidden" name="tpa_status" value="${app_sponsor.status}"/>
								<input type="hidden" name="package_sponsor_id" value="${app_sponsor.package_sponsor_id}"/>
								<input type="hidden" name="tpa_edited" value="false"/>
								<input type="hidden" name="tpa_delete" value="false"/>
								<label>${app_sponsor.tpa_name}</label>
							</td>
							<td>${app_sponsor.status == 'A' ? 'Active' : 'Inactive'}</td>
							<td style="text-align: center">
								<a href="javascript:Cancel Sponsor" onclick="return cancelSponsor(this);" title="Cancel Sponsor" >
									<img src="${cpath}/icons/delete.gif" class="imgDelete button" />
								</a>
							</td>
							<td style="text-align: center">
								<a name="_editSponsorAnchor" href="javascript:Edit" onclick="return showEditSponsorDialog(this);"
									title="Edit Sponsor Details">
									<img src="${cpath}/icons/Edit.png" class="button" />
								</a>
							</td>
						</tr>
					</c:forEach>
				</table>
				<table class="addButton" style="height: 25px;">
					<tr>
						<td style="width: 16px;text-align: right">
							<button type="button" name="btnAddSponsor" id="btnAddSponsor" title="Add Sponsor"
								onclick="showAddSponsorDialog(this); return false;"
								class="imgButton">
								<img src="${cpath}/icons/Add.png" id="sponsorAddIconEnabled"/>
								<img src="${cpath}/icons/Add1.png" id="sponsorAddIconDisabled"/>
							</button>
						</td>
					</tr>
				</table>
			</fieldset>
			<fieldset class="fieldSetBorder" style="margin-top: 20px">
				<legend class="fieldSetLabel">Plans</legend>
				<c:set var="plans_count" value="${fn:length(applicable_plans)}"/>
				<div>
					<c:set var="app_for_all_plans" value="${ applicable_plans[0].map.plan_id == '-1'}"/>
					<div style="float: left"><input type="radio" name="applicable_for_plans" id="app_for_all_plans" value="all" ${app_for_all_plans ? 'checked' : ''} onchange="allowAddingPlan(this.value);"/></div>
					<div style="padding-top: 5px; float: left">Applicable for All(Plans & Cash)</div>
				</div>
				<div style="clear: both"></div>
				<div style="margin-top: 10px; margin-left: 100px">OR</div>
				<div >
					<c:set var="app_for_none_plans" value="${ applicable_plans[0].map.plan_id == '0'}"/>
					<div style="float: left"><input type="radio" name="applicable_for_plans" id="app_for_none_plans" value="none" ${app_for_none_plans ? 'checked' : ''} onchange="allowAddingPlan(this.value);"/></div>
					<div style="padding-top: 5px; float: left">Applicable for none of the Plans(Only cash)</div>
				</div>
				<div style="clear: both"></div>
				<div style="margin-top: 10px; margin-left: 100px">OR</div>
				<div style="margin-top: 10px">
					<div style="float: left"><input type="radio" name="applicable_for_plans" id="app_for_few_plans" value="few" ${app_for_all_plans || app_for_none_plans ? '' : 'checked'} onchange="allowAddingPlan(this.value);"/></div>
					<div style="padding-top: 5px; float: left">Applicable for the following Plans</div>
				</div>
				<div style="clear: both"></div>
				<table style="margin-top: 10px" class="detailList dialog_displayColumns" cellspacing="0" cellpadding="0"
					id="plans_table" border="0" width="100%">
					<tr>
						<th>Plan</th>
						<th>Status</th>
						<th style="width: 16px"></th>
						<th style="width: 16px"></th>
					</tr>
					<c:forEach begin="1" end="${app_for_all_plans || app_for_none_plans ? 1 : plans_count+1}" var="i" varStatus="loop">
						<c:set var="app_plan" value="${applicable_plans[(app_for_all_plans || app_for_none_plans ? 1 : i-1)].map}"/>
						<tr style="display: ${empty app_plan ? 'none' : 'table-row'}">
							<td>
								<input type="hidden" name="plan_id" value="${app_plan.plan_id}"/>
								<input type="hidden" name="plan_name" value="${app_plan.plan_name}"/>
								<input type="hidden" name="plan_status" value="${app_plan.status}"/>
								<input type="hidden" name="package_plan_id" value="${app_plan.package_plan_id}"/>
								<input type="hidden" name="plan_edited" value="false"/>
								<input type="hidden" name="plan_delete" value="false"/>
								<label>${app_plan.plan_name}</label>
							</td>
							<td>${app_plan.status == 'A' ? 'Active' : 'Inactive'}</td>
							<td style="text-align: center">
								<a href="javascript:Cancel Plan" onclick="return cancelPlan(this);" title="Cancel Plan" >
									<img src="${cpath}/icons/delete.gif" class="imgDelete button" />
								</a>
							</td>
							<td style="text-align: center">
								<a name="_editPlanAnchor" href="javascript:Edit" onclick="return showEditPlanDialog(this);"
									title="Edit Plan Details">
									<img src="${cpath}/icons/Edit.png" class="button" />
								</a>
							</td>
						</tr>
					</c:forEach>
				</table>
				<table class="addButton" style="height: 25px;">
					<tr>
						<td style="width: 16px;text-align: right">
							<button type="button" name="btnAddPlan" id="btnAddPlan" title="Add Plan"
								onclick="showAddPlanDialog(this); return false;"
								class="imgButton">
								<img src="${cpath}/icons/Add.png" id="planAddIconEnabled"/>
								<img src="${cpath}/icons/Add1.png" id="planAddIconDisabled"/>
							</button>
						</td>
					</tr>
				</table>
			</fieldset>
			
			<table style="margin-top: 10px">
				<tr>
						<input type="button" name="Save" value="Save"
							${pack_bean.map.type == 'Package' && (roleId != 1 && roleId != 2 && pkg_approval_rights != 'A') && pack_bean.map.approval_status == 'A' ? 'disabled' : ''}
							onclick="return saveForm();">
						<c:if test="${pack_bean.map.type == 'Package'}">
							| <a href="${cpath}/pages/masters/insta/admin/PackagesMasterAction.do?_method=getEditPackageCharges
								&amp;packId=${pack_bean.map.package_id }&amp;org_id=${ifn:cleanURL(param.org_id)}&amp;multi_visit_package=${ifn:cleanURL(param.multi_visit_package)}">Edit Charges</a>
						</c:if>
						| <a href="${cpath}/pages/masters/insta/admin/PackagesMasterAction.do?
							_method=getPackageListScreen&amp;package_active=A&amp;sortReverse=false&approval_status=A">Packages List
							</a>
					</td>
				</tr>
			</table>
			<div id="addCenterDialog" style="display: none">
				<div class="bd">
					<fieldset class="fieldSetBorder">
						<legend class="fieldSetLabel">Add Center</legend>
						<table class="add_center_table">
							<tr>
								<td >State: </td>
								<td><insta:selectdb name="d_state" id="d_state" table="state_master" valuecol="state_id" displaycol="state_name"
										filtered="true" dummyvalue="-- Select --" dummyvalueid="" onchange="return populateCities();"/></td>
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
								<input type="button" id="edit_center_previous" value="<<Previous" />
								<input type="button" id="edit_center_next" value="Next>>"/>
							</td>
						</tr>
					</table>
				</div>
			</div>
			<div id="addPlanDialog" style="display: none">
				<div class="bd">
					<fieldset class="fieldSetBorder">
						<legend class="fieldSetLabel">Add Plan</legend>
						<table class="formtable">
							<tr>
								<td class="formlabel">Plan: </td>
								<td><select name="d_plan" id="d_plan" class="dropdown">
										<option value="">-- Select --</option>
									</select>
								</td>
							</tr>
							<tr>
								<td class="formlabel">Status: </td>
								<td><select id="d_plan_status" class="dropdown">
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
								<input type="button" name="plan_ok" id="plan_ok" value="Ok"/>
								<input type="button" name="plan_cancel" id="plan_cancel" value="Cancel"/>
							</td>
						</tr>
					</table>
				</div>
			</div>
			<div id="editPlanDialog" style="display: none">
				<div class="bd">
					<fieldset class="fieldSetBorder">
						<legend class="fieldSetLabel">Edit Plan</legend>
						<input type="hidden" id="plan_edit_row_id" value=""/>
						<table class="formtable">
							<tr>
								<td class="formlabel">Plan: </td>
								<td class="forminfo">
									<label id="ed_plan_label"></label>
									<input type="hidden" id="ed_plan" value=""/>
								</td>
							</tr>
							<tr>
								<td class="formlabel">Status: </td>
								<td><select id="ed_plan_status" class="dropdown" onchange="setPlanFieldEdited();">
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
								<input type="button" name="plan_edit_ok" id="plan_edit_ok" value="Ok"/>
								<input type="button" name="plan_edit_cancel" id="plan_edit_cancel" value="Cancel"/>
								<input type="button" name="plan_edit_previous" id="plan_edit_previous" value="<<Previous" />
								<input type="button" name="plan_edit_next" id="plan_edit_next" value="Next>>"/>
							</td>
						</tr>
					</table>
				</div>
			</div>
			<div id="addSponsorDialog" style="display: none">
				<div class="bd">
					<fieldset class="fieldSetBorder">
						<legend class="fieldSetLabel">Add Sponsor</legend>
						<table class="formtable">
							<tr>
								<td class="formlabel">Sponsor: </td>
								<td>
									<insta:selectdb name="d_tpa" id="d_tpa" table="tpa_master" displaycol="tpa_name"
										valuecol="tpa_id" filtered="true" dummyvalue="-- Select --" orderby="tpa_name"/>
								</td>
							</tr>
							<tr>
								<td class="formlabel">Status: </td>
								<td><select id="d_tpa_status" class="dropdown">
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
								<input type="button" name="sp_ok" id="sp_ok" value="Ok"/>
								<input type="button" name="sp_cancel" id="sp_cancel" value="Cancel"/>
							</td>
						</tr>
					</table>
				</div>
			</div>
			<div id="editSponsorDialog" style="display: none">
				<div class="bd">
					<fieldset class="fieldSetBorder">
						<legend class="fieldSetLabel">Edit Sponsor</legend>
						<input type="hidden" id="sponsor_edit_row_id" value=""/>
						<table class="formtable">
							<tr>
								<td class="formlabel">Sponsor: </td>
								<td class="forminfo">
									<label id="ed_sponsor_label"></label>
									<input type="hidden" id="ed_sponsor" value=""/>
								</td>
							</tr>
							<tr>
								<td class="formlabel">Status: </td>
								<td><select id="ed_tpa_status" class="dropdown" onchange="setSponsorFieldEdited();">
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
								<input type="button" name="sp_edit_ok" id="sp_edit_ok" value="Ok"/>
								<input type="button" name="sp_edit_cancel" id="sp_edit_cancel" value="Cancel"/>
								<input type="button" name="sp_edit_previous" id="sp_edit_previous" value="<<Previous" />
								<input type="button" name="sp_edit_next" id="sp_edit_next" value="Next>>"/>
							</td>
						</tr>
					</table>
				</div>
			</div>
		</form>
	</body>
</html>

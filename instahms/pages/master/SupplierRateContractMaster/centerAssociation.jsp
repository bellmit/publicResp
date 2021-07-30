<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="max_centers" value='<%= GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default") %>' scope="request"/>

<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
		<title>Add/Edit Supplier Rate Center Applicability - Insta HMS</title>
		<insta:link type="script" file="masters/supplier_rate_contract_center_applicabilty.js"/>
		<insta:link type="script" file="master/CenterAssociation/center_applicability.js"/>
		<script>
			var allotedCenters = ${centerList};
			var max_centers = ${max_centers};
			var centersJSON = ${max_centers} > 1 ? ${centers_json} : null;
			var citiesJSON = ${cities_json};
			var theForm = "center_association_form";
			var primaryKeyName = "discount_plan_center_id";
			var status = '${status}';
		</script>
		<style>
			table.add_center_table td {
				padding: 5px 0px 2px 5px;
			}
		</style>
	</head>
	<body onload="init();">
		<h1>Supplier Rate Center Applicability - ${bean.entity_name}</h1>
		<insta:feedback-panel/>
		<form action="SupplierRateContractMasterCenterAssociation.do" name="center_association_form" method="POST">
			<input type="hidden" name="_method" value="updateAssociation"/>
			<input type="hidden" name="entity_id_column_name" value="supplier_rate_contract_id"/>
			<input type="hidden" name="entity_id" value="${bean.supplier_rate_contract_id }"/>
			<fieldset class="fieldSetBorder" style="display: ${max_centers > 1 ? 'block' : 'none'}">
				<legend class="fieldSetLabel">Centers</legend>
				<c:set var="app_centers_count" value="${fn:length(applicable_centers)}"/>
				<div>  
					<c:set var="app_for_all_centers" value="${app_centers_count > 0 && applicable_centers[0].map.center_id == 0 ? true : applicable_centers[0].map.center_id eq null}"/>
					<div style="float: left"><input type="radio" name="applicable_for_centers" value="all" ${app_for_all_centers ? 'checked' : ''} onchange="allowAddingCenter(this.value)"/></div>
					<div style="padding-top: 5px; float: left">Supplier Rate Contract Available For All Centers</div>
				</div>
				<div style="clear: both"></div>
				<div style="margin-top: 10px; margin-left: 100px">OR</div>
				<div style="margin-top: 10px">
					<div style="float: left"><input type="radio" name="applicable_for_centers" value="few" ${app_for_all_centers ? '' : 'checked'} onchange="allowAddingCenter(this.value)"/></div>
					<div style="padding-top: 5px; float: left">Supplier Rate Contract Available For Following Centers</div>
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
								<input type="hidden" name="supplier_rate_contract_center_id" value="${app_center.supplier_rate_contract_center_id}"/>
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
			<table style="margin-top: 10px">
				<tr>
					<td>
						<button type="button" name="Save" value="Save" accessKey='S'
							onclick="return saveForm('center_association_form');"><b><u><insta:ltext key="master.supplierratecontract.raisecontract.s"/></u></b><insta:ltext key="master.supplierratecontract.raisecontract.ave"/></button>
							<c:url value="/pages/master/SupplierRateContractMaster.do" var="supplierRateContractListUrl">
								<c:param name="_method" value="list"/>
								<c:param name="status" value="A"/>
								<c:param name="sortOrder" value="supplier_rate_contract_name"/>
								<c:param name="sortReverse" value="false"/>
							</c:url>
						| <a href="${supplierRateContractListUrl}" title="Supplier Rate Contract List">Supplier Rate Contract List</a>
					 </td>
				</tr>
			</table>
			<insta:addCenterAssociation/>
			<insta:editCenterAssociation/>			
		</form>
	</body>
</html>

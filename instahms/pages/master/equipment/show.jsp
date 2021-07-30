<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<%@page import="com.insta.hms.master.URLRoute"%>
<c:set var="pagePath" value="<%=URLRoute.EQUIPMENT_MASTER %>"/>
<c:set value="${pageContext.request.contextPath}" var="cpath" />
<c:set var="actionUrl" value="${cpath}/${pagePath}/update.htm"/>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Equipment Details - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js" />
<insta:link type="js" file="masters/addEquipmet.js" />
<insta:link type="js" file="masters/charges_common.js" />
<insta:link type="css" file="widgets.css" />
<insta:link type="script" file="widgets.js" />
<insta:link type="js" file="ajax.js" />
<insta:link type="js" file="masters/orderCodes.js" />

<script>
	var cpath = '${cpath}';
	var neworedit = 'edit';
	var namesList = ${ifn:convertListToJson(equipmentNames)};
    var serviceSubGroupsList = ${ifn:convertListToJson(serviceSubGroupsList)};
    var itemGroupList = ${ifn:convertListToJson(itemGroupListJson)};
	var itemSubGroupList = ${ifn:convertListToJson(itemSubGroupListJson)};
    Insta.masterData=${ifn:convertListToJson(equipmentNames)};
</script>

<script type="text/javascript">
	function validateForm() {
		var isInsuranceCatIdSelected = false;
		var insuranceCatId = document.getElementById('insurance_category_id');
		for (var i=0; i<insuranceCatId.options.length; i++) {
			if (insuranceCatId.options[i].selected) {
				isInsuranceCatIdSelected = true;
			}
		}
		if (isInsuranceCatIdSelected) {
			var validation = validateSubmit();
			if (validation) {
			  return true;
			} else {
			  return false;
			}
		} else {
			alert("Please select at least one insurance category");
			return false;
		}
	}
</script>
</head>
<body onload="initAddShow();itemsubgroupinit();">

<h1 style="float:left">Equipment Details</h1>
<c:url var="searchUrl" value="/master/equipment/show.htm"/>
<insta:findbykey keys="equipment_name,eq_id" fieldName="equip_id" method="" url="${searchUrl}"
 extraParamKeys="org_id" extraParamValues="${bean.map.org_id}"/>


<form name="equipmentform" action="${actionUrl}" method="POST">
	<input type="hidden" name="equip_id" value="${param.equip_id}"/>
	<input type="hidden" name="org_id" value="${param.org_id}" />
	<input type="hidden" id="serviceSubGroup" value="${serviceSubGroup}">
<%-- 	<input type="hidden" id="departmentId" value="${departmentId}"> --%>


	<insta:feedback-panel/>

<fieldset class="fieldSetBorder">
	<table class="formtable">
		<tr>
			<td class="formlabel">Equipment Name:</td>
			<td><input type="text" name="equipment_name" id="equipment_name" maxlength="100" value="${bean.get('equipment_name')}"
				  title="Operation name is required" onchange="return checkDuplicates();"/>
				  <span class="star">*</span>
			</td>
			<td class="formlabel">Department:</td>
			<td>
				<select name="dept_id" id="dept_id" class="dropdown">
					<option value="">--Dept Name--</option>
					<c:forEach items="${departments}" var="department" >
						<option value="${department.get('dept_id')}" ${department.get('dept_id') eq departmentId ? 'selected' : ''}>${department.get('dept_name')}</option>
					</c:forEach>
				</select><span class="star">*</span>
			</td>
			<td class="formlabel">Status:</td>
			<td>
				<select name="status" class="dropdown">
					<option value="A" ${bean.get('status') eq 'A' ? 'selected':''}>Active</option>
					<option value="I" ${bean.get('status') eq 'I' ? 'selected':''}>InActive</option>
				</select>
			</td>
		</tr>

		<tr>
			<td class="formlabel">Service Group:</td>
			<td>
				<select name="service_group_id" id="service_group_id" class="dropdown" onchange="loadServiceSubGroup()">
					<option value="">--Select--</option>
					<c:forEach items="${serviceGroups}" var="service" >
						<option value="${service.get('service_group_id')}" ${service.get('service_group_id') eq groupId ? 'selected' : ''}>${service.get('service_group_name')}</option>
					</c:forEach>
				</select><span class="star">*</span>
			</td>
			<td class="formlabel">Service Sub Group:</td>
			<td>
				<select name="serviceSubGroupId" id="serviceSubGroupId" class="dropdown" onchange="getOrderCode();">
					<option value="">-- Select --</option>
				</select>
				<input type="hidden" name="service_sub_group_id" id="service_sub_group_id" /><span class="star">*</span>
			</td>
			<td class="formlabel">Insurance Category:</td>
			<td>
				<select name="insurance_category_id" id="insurance_category_id" class="listbox" multiple="true">
					<c:forEach items="${insuranceCategory}" var="insurance">
						<option value="${insurance.get('insurance_category_id')}" ${activeInsuranceCategory.contains(insurance.get('insurance_category_id')) ? 'selected' : ''}>${insurance.get('insurance_category_name')}</option>
					</c:forEach>
				</select>
			</td>
		</tr>

		<tr>
			<td class="formlabel">Order Code / Alias:</td>
			<td><input type="text" name="equipment_code" id="equipment_code"
				size="8" maxlength="20" value="${bean.get('equipment_code')}"/></td>
			<td class="formlabel">Billing Group:</td>
			<td>
				<insta:selectdb  name="billing_group_id" id="billing_group_id"  value="${bean.map.billing_group_id}" table="item_groups" valuecol="item_group_id"
					displaycol="item_group_name" dummyvalue="-- Select --" filtercol="item_group_type_id,status" filtervalue="BILLGRP,A"/>
			</td>
		</tr>
		<tr>
			<td class="formlabel">Charge Basis:</td>
			<td>
				<select name="charge_basis" class="dropdown">
					<option value="D" ${bean.get('charge_basis') eq 'D'? 'selected':''}>Daily</option>
					<option value="U" ${bean.get('charge_basis') eq 'U'? 'selected':''}>Units</option>
					<option value="B" ${bean.get('charge_basis') eq 'B'? 'selected':''}>Both</option>
				</select>
			</td>
			<td class="formlabel">Unit Size (Minutes):</td>
			<td>
				<input type="text" name="duration_unit_minutes" id="duration_unit_minutes" class="required"
				onkeypress="return enterNumOnlyzeroToNine(event)"
				value="${bean.get('duration_unit_minutes')}"  title="Unit Size is required"/>
				<span class="star">*</span>
			</td>
		</tr>
			<td class="formlabel">Minimum Duration (units):</td>
			<td><input type="text" name="min_duration" Class="required"
				maxlength="3" onkeypress="return enterNumOnlyzeroToNine(event)" value="${bean.get('min_duration')}"  title="Min Duration is required"/>
				<span class="star">*</span>
			</td>

			<td class="formlabel">Slab 1 Threshold (units):</td>
			<td><input type="text" name="slab_1_threshold" Class="required"
				maxlength="3" onkeypress="return enterNumOnlyzeroToNine(event)" value="${bean.get('slab_1_threshold')}"  title="Slab 1 Threshold is required"/>
				<span class="star">*</span>
			</td>
			<td class="formlabel">Incr. Duration (units):</td>
			<td><input type="text" name="incr_duration" Class="required"
				onkeypress="return enterNumOnlyzeroToNine(event)" maxlength="3" value="${bean.get('incr_duration')}" title="Incr Duration is required"/>
				<span class="star">*</span>
			</td>
		</tr>
		<tr>
			<td class="formlabel">Allow Rate Increase:</td>
			<td>
				<input id="" type="radio" checked="1" value="true" name="allow_rate_increase" ${bean.get('allow_rate_increase') eq true ? 'checked' : '' }/>
					 <label>Yes</label>
				<input id="" type="radio" value="false" name="allow_rate_increase" ${bean.get('allow_rate_increase') eq false ? 'checked' : '' }/>
				     <label>No</label>
			</td>
			<td class="formlabel">Allow Rate Decrease:</td>
			<td>
				<input id="" type="radio" checked="1" value="true" name="allow_rate_decrease" ${bean.get('allow_rate_decrease') eq true ? 'checked' : '' }/>
					 <label>Yes</label>
				<input id="" type="radio" value="false" name="allow_rate_decrease" ${bean.get('allow_rate_decrease') eq false ? 'checked' : '' }/>
				     <label>No</label>
			</td>

		</tr>
	</table>
</fieldset>
<insta:taxations/>
<table class="screenActions">
	<tr>
		<td><button type="button" name="Save" accesskey="S" onclick="validateForm();"><b><u>S</u></b>ave</button></td>
			<td>&nbsp;|&nbsp;</td>
			<td><a href="${cpath}/master/equipment/add.htm?org_id=${bean.get('org_id')}">Add</a></td>
			<td>&nbsp;|&nbsp;</td>
			<td><a href="${cpath}/master/equipment/editcharge.htm?equip_id=${bean.get('eq_id')}&org_id=${bean.get('org_id')}">Equipment Charges</a></td>
		<td>&nbsp;|&nbsp;</td>
		<td><a href="${cpath}/master/equipment.htm?status=A&sortOrder=equipment_name&sortReverse=false&org_id=${bean.get('org_id')}">Equipments List</a></td>
	</tr>
</table>

</form>
</body>
</html>

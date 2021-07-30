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

<c:set value="${pageContext.request.contextPath}" var="cpath" />
<script>
	var cpath = '${cpath}';
	var namesList = ${namesList};
 <c:if test="${param._method != 'add'}">
	Insta.masterData=${equipmentsLists};
 </c:if>
 var serviceSubGroupsList = ${serviceSubGroupsList};
 var itemGroupList = ${itemGroupListJson};
 var itemSubGroupList = ${itemSubGroupListJson};
</script>
</head>
<body onload="initAddShow();itemsubgroupinit();">
<c:choose>
	<c:when test="${param._method != 'add'}">
	  <h1 style="float:left">Equipment Details</h1>
	  <c:url var="searchUrl" value="/master/EquipmentMaster.do"/>
	  <insta:findbykey keys="equipment_name,eq_id" fieldName="equip_id" method="show" url="${searchUrl}"/>
    </c:when>
    <c:otherwise>
       <h1>Equipment Details</h1>
    </c:otherwise>
</c:choose>

<form name="equipmentform" action="EquipmentMaster.do" method="POST">
	<input type="hidden" name="_method" value="${requestScope.method}" />
	<input type="hidden" name="equip_id" value="${ifn:cleanHtmlAttribute(param.equip_id)}"/>
	<input type="hidden" id="serviceSubGroup" value="${bean.map.service_sub_group_id}">


	<insta:feedback-panel/>

<fieldset class="fieldSetBorder">
	<table class="formtable">
		<tr>
			<td class="formlabel">Equipment Name:</td>
			<td><input type="text" name="equipment_name" id="equipment_name" maxlength="100" value="${bean.map.equipment_name}"
				  title="Operation name is required" onchange="return checkDuplicates();"/>
				  <span class="star">*</span>
			</td>
			<td class="formlabel">Department:</td>
			<td>
				<insta:selectdb name="dept_id" table="department" valuecol="dept_id" displaycol="dept_name"
					orderby="dept_name" value="${bean.map.dept_id}" dummyvalue="..Select.." class="dropdown" title="Department name is required"/>
					<span class="star">*</span>
			</td>
			<td class="formlabel">Status:</td>
			<td>
				<insta:selectoptions name="status" value="${bean.map.status}"
						opvalues="A,I" optexts="Active,Inactive"/>
			</td>
		</tr>

		<tr>
			<td class="formlabel">Service Group:</td>
			<td>
				<insta:selectdb id="service_group_id" name="service_group_id" value="${groupId}"
					table="service_groups" class="dropdown"   dummyvalue="-- Select --"
					valuecol="service_group_id"  displaycol="service_group_name" onchange="loadServiceSubGroup()" />
					<span class="star">*</span>
			</td>
			<td class="formlabel">Service Sub Group:</td>
			<td>
				<select name="service_sub_group_id" id="service_sub_group_id" class="dropdown" onchange="getOrderCode();">
					<option value="">-- Select --</option>
				</select>
				<span class="star">*</span>
			</td>
			<td class="formlabel">Insurance Category:</td>
			<td>
			<insta:selectdb  name="insurance_category_id" id="insurance_category_id"  value="${insurance_category_id}" table="item_insurance_categories" valuecol="insurance_category_id" displaycol="insurance_category_name" filtercol="system_category" filtervalue="N"/></td>
		</tr>

		<tr>
			<td class="formlabel">Order Code / Alias:</td>
			<td><input type="text" name="equipment_code" id="equipment_code"
				size="8" maxlength="20" value="${bean.map.equipment_code}"/></td>
		</tr>
		<tr>
			<td class="formlabel">Charge Basis:</td>
			<td>
				<insta:selectoptions name="charge_basis" value="${bean.map.charge_basis}"
						opvalues="D,U,B" optexts="Daily,Units,Both"/>
			</td>
			<td class="formlabel">Unit Size (Minutes):</td>
			<td>
				<input type="text" name="duration_unit_minutes" id="duration_unit_minutes" class="required"
				onkeypress="return enterNumOnlyzeroToNine(event)"
				value="${bean.map.duration_unit_minutes}"  title="Unit Size is required"/>
				<span class="star">*</span>
			</td>
		</tr>
			<td class="formlabel">Minimum Duration (units):</td>
			<td><input type="text" name="min_duration" Class="required"
				maxlength="3" onkeypress="return enterNumOnlyzeroToNine(event)" value="${bean.map.min_duration}"  title="Min Duration is required"/>
				<span class="star">*</span>
			</td>

			<td class="formlabel">Slab 1 Threshold (units):</td>
			<td><input type="text" name="slab_1_threshold" Class="required"
				maxlength="3" onkeypress="return enterNumOnlyzeroToNine(event)" value="${bean.map.slab_1_threshold}"  title="Slab 1 Threshold is required"/>
				<span class="star">*</span>
			</td>
			<td class="formlabel">Incr. Duration (units):</td>
			<td><input type="text" name="incr_duration" Class="required"
				onkeypress="return enterNumOnlyzeroToNine(event)" maxlength="3" value="${bean.map.incr_duration}" title="Incr Duration is required"/>
				<span class="star">*</span>
			</td>
		</tr>
		<tr>
			<td class="formlabel">Allow Rate Increase:</td>
			<td>
				<insta:radio name="allow_rate_increase" radioValues="true,false" radioText="Yes,No"
						value="${empty bean.map.allow_rate_increase ? 'true' : bean.map.allow_rate_increase}" />
			</td>
			<td class="formlabel">Allow Rate Decrease:</td>
			<td>
				<insta:radio name="allow_rate_decrease" radioValues="true,false" radioText="Yes,No"
						value="${empty bean.map.allow_rate_decrease ? 'true' : bean.map.allow_rate_decrease}" />
			</td>

		</tr>
	</table>
</fieldset>
<insta:taxations/>
<table class="screenActions">
	<tr>
		<td><button type="button" name="Save" accesskey="S" onclick="validateSubmit();"><b><u>S</u></b>ave</button></td>
		<c:if test="${param._method=='show'}">
			<td>&nbsp;|&nbsp;</td>
			<td><a href="${cpath}/master/EquipmentMaster.do?_method=add">Add</a></td>
			<td>&nbsp;|&nbsp;</td>
			<td><a href="${cpath}/master/EquipmentMaster.do?_method=showCharges&equip_id=${bean.map.eq_id}&org_id=${bean.map.org_id}">Equipment Charges</a></td>
		</c:if>
		<td>&nbsp;|&nbsp;</td>
		<td><a href="${cpath}/master/EquipmentMaster.do?_method=list&status=A&sortOrder=equipment_name&sortReverse=false">Equipments List</a></td>
	</tr>
</table>

</form>
</body>
</html>

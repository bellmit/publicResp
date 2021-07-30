<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<c:set var="cpath" value="${pageContext.request.contextPath}" />

<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<c:set var="max_centers_inc_default" value='<%= GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default") %>' scope="request"/>

<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Add/Edit Dyna Package Rule - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js"/>
<insta:link type="script" file="master/DynaPackageRules/dynaPkgRules.js"/>
<style type="text/css">
	option.select-hr {text-align: center; border-bottom: 1px dotted #000; }
</style>
<script>

	var beanChargeGrp		= '${bean.map.chargegroup_id}';
	var beanChargeHead	= '${bean.map.chargehead_id}';
	var beanServGrp	 	= '${bean.map.service_group_id}';
	var beanServSubGrp	= '${bean.map.service_sub_group_id}';
	var beanActType		= '${bean.map.activity_type}';
	var beanActId			= '${bean.map.activity_id}';
	var beanActName		= '${activity_name}';

</script>
</head>

<body onload="initRules();">
<h1>${param._method == 'add' ? 'Add' : 'Edit'} Dyna Package Rule</h1>
<insta:feedback-panel/>

<form action="DynaPackageRulesMaster.do" name="dynaPkgRuleForm" method="POST">
	<input type="hidden" name="_method" id="_method" value="${param._method == 'add' ? 'create' : 'update'}">
	<input type="hidden" name="pkg_rule_id"  value="${bean.map.pkg_rule_id}"/>

	<fieldset>
	<table class="formtable" cellpadding="0" cellspacing="0" width="100%" border="0">
		<tr>
			<td class="formlabel">Priority:</td>
			<td>
				<input type="text" name="priority" id="priority" value="${bean.map.priority}" maxlength="9"
					class="required validate-number" title="Priority is required">
			</td>
		</tr>
		<tr>
			<td class="formlabel">Charge Group:</td>
			<td>
				<insta:selectdb id="chargegroup_id" name="chargegroup_id" table="chargegroup_constants"
				valuecol="chargegroup_id" displaycol="chargegroup_name" filtered="false" orderby="chargegroup_name"
				dummyvalueId="*" dummyvalue="(All)" onchange="onChangeChargeGroup();"
				value="${bean.map.chargegroup_id}"/>
			</td>
			<td class="formlabel">Charge Head:</td>
			<td>
				<insta:selectdb id="chargehead_id" name="chargehead_id" table="chargehead_constants"
				valuecol="chargehead_id" displaycol="chargehead_name" filtered="false" orderby="chargehead_name"
				dummyvalueId="*" dummyvalue="(All)"
				value="${bean.map.chargehead_id}"/>
			</td>
			<td class="formlabel">
				<c:if test="${max_centers_inc_default > 1}">
					Center Name:
				</c:if>
			</td>
			<td>
				<c:if test="${max_centers_inc_default > 1}">
					<insta:selectdb  name="center_id"  table="hospital_center_master"
						valuecol="center_id" displaycol="center_name" orderby="center_name"
						dummyvalueId="*" dummyvalue="All"
						value="${bean.map.center_id}" />
				 </c:if>
			</td>
		</tr>
		<tr>
			<td class="formlabel">Service Group: </td>
			<td>
				<insta:selectdb id="service_group_id" name="service_group_id" table="service_groups"
				valuecol="service_group_id" displaycol="service_group_name" filtered="false" orderby="service_group_name"
				dummyvalueId="*" dummyvalue="(All)" onchange="onChangeServiceGroup();"
				value="${bean.map.service_group_id}"/>
			</td>
			<td class="formlabel">Service Sub Group: </td>
			<td>
				<insta:selectdb id="service_sub_group_id" name="service_sub_group_id" table="service_sub_groups"
				valuecol="service_sub_group_id" displaycol="service_sub_group_name" filtered="false" orderby="service_sub_group_name"
				dummyvalueId="*" dummyvalue="(All)"
				value="${bean.map.service_sub_group_id}"/>
			</td>
		</tr>
		<tr>
			<td class="formlabel">Activity type / Item Category: </td>
			<td>
				 <select id="activity_type" name="activity_type" onchange="onChangeActivityType();" class="dropdown">

					<option value="*" ${bean.map.activity_type eq '*' ?'selected':'' }>(All)</option>
					<option value="_ALL_DIET" ${bean.map.activity_type eq '_ALL_DIET' ?'selected':'' }>All Diet</option>
					<option value="_ALL_DOCTORS" ${bean.map.activity_type eq '_ALL_DOCTORS' ?'selected':'' }>All Doctors</option>
					<option value="_ALL_EQUIPMENTS" ${bean.map.activity_type eq '_ALL_EQUIPMENTS' ?'selected':'' }>All Equipments</option>
					<option value="_ALL_OTHER_CHARGES" ${bean.map.activity_type eq '_ALL_OTHER_CHARGES' ?'selected':'' }>All Other Charges</option>
					<option value="_ALL_OPERATIONS" ${bean.map.activity_type eq '_ALL_OPERATIONS' ?'selected':'' }>All Surgeries</option>
					<option value="_ALL_PACKAGES" ${bean.map.activity_type eq '_ALL_PACKAGES' ?'selected':'' }>All Packages</option>
					<option value="_ALL_SERVICES" ${bean.map.activity_type eq '_ALL_SERVICES' ?'selected':'' }>All Services</option>
					<option value="_ALL_LABTESTS" ${bean.map.activity_type eq '_ALL_LABTESTS' ?'selected':'' }>All Lab. Tests</option>
					<option value="_ALL_RADTESTS" ${bean.map.activity_type eq '_ALL_RADTESTS' ?'selected':'' }>All Rad. Tests</option>
					<option value="_ALL_NORMAL_BEDTYPES" ${bean.map.activity_type eq '_ALL_NORMAL_BEDTYPES' ?'selected':'' }>All Normal Bedtypes</option>
					<option value="_ALL_ICU_BEDTYPES" ${bean.map.activity_type eq '_ALL_ICU_BEDTYPES' ?'selected':'' }>All ICU Bedtypes</option>
					<option disabled="disabled">----- Store Item Categories below -----</option>
					<c:forEach var="icat" items="${storeItemCategories}">
						<option value="${fn:escapeXml(icat.map.category_id)}"
						${(icat.map.category_id == bean.map.activity_type) ? 'selected' : ''}>
							<c:out value="${icat.map.category}"/>
						</option>
					</c:forEach>
				</select>
			</td>
			<td class="formlabel">Activity / Item: </td>
			<td colspan="2" valign="top">
				<div id="activityIdAutoComplete" style="display:none;">
					<input type="text" name="activity_id_auto_fld" id="activity_id_auto_fld"
						value="(All)" style="width:25em"/>
					<input type="hidden" name="activity_id" id="activity_id" value="*" />
					<div id="activityIdContainer" style = "width:45em"></div>
				</div>
			</td>
		</tr>
		<tr>
			<td class="formlabel">Pkg Category:</td>
			<td>
				<insta:selectdb id="dyna_pkg_cat_id" name="dyna_pkg_cat_id" table="dyna_package_category"
				valuecol="dyna_pkg_cat_id" displaycol="dyna_pkg_cat_name" value="${bean.map.dyna_pkg_cat_id}"
				filtered="false" orderby="dyna_pkg_cat_name" dummyvalue="-- Select --"/>
			</td>
		</tr>
	</table>
	</fieldset>

	<div class="screenActions">
		<button type="button" accesskey="S" onclick="return validateSubmit();"><b><u>S</u></b>ave</button>
		<c:if test="${param._method != 'add'}">
			<a href="${cpath}/master/DynaPackageRulesMaster.do?_method=add">Add</a>
		|
		</c:if>
		<a href="${cpath}/master/DynaPackageRulesMaster.do?_method=list&sortOrder=priority&sortReverse=false">Dyna Package Rules List</a>
	</div>

</form>

<script>
	var chargeGroupHeadList = ${chargeGroupHeadListJSON};
</script>
</body>
</html>

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
<title>Anaesthesia Type Details - Insta HMS</title>
<insta:link type="script" file="hmsvalidation.js" />
<insta:link type="js" file="masters/charges_common.js" />
<insta:link type="js" file="masters/addAnaesthesiatype.js"/>
<insta:link type="css" file="widgets.css" />
<insta:link type="script" file="widgets.js" />
<insta:link type="js" file="ajax.js" />
<insta:link type="js" file="masters/orderCodes.js" />

<c:set value="${pageContext.request.contextPath}" var="cpath" />
<script>
	var cpath = '${cpath}';
	var namesList = ${namesList};
	var method = '${ifn:cleanJavaScript(param._method)}';
 <c:if test="${param._method != 'add'}">
	Insta.masterData=${anaesthesiaTypeLists};
 </c:if>
 var serviceSubGroupsList = ${serviceSubGroupsList};
 var itemGroupList = ${itemGroupListJson};
 var itemSubGroupList = ${itemSubGroupListJson};
</script>
</head>
<body onload="initAddShow();itemsubgroupinit();">
<c:choose>
	<c:when test="${param._method != 'add'}">
	  <h1 style="float:left">Anaesthesia Type Details</h1>
	  <c:url var="searchUrl" value="/master/AnaesthesiaTypeMaster.do"/>
	  <insta:findbykey keys="anesthesia_type_name,anesthesia_type_id" fieldName="anesthesia_type_id" method="show" url="${searchUrl}"/>
    </c:when>
    <c:otherwise>
       <h1>Anaesthesia Type Details</h1>
    </c:otherwise>
</c:choose>

<form action="AnaesthesiaTypeMaster.do" name="showform" method="GET">		<%-- for rate plan change --%>
	<input type="hidden" name="_method" value="${ifn:cleanHtmlAttribute(param._method)}" />
	<input type="hidden" name="anesthesia_type_id" value="${bean.map.anesthesia_type_id}"/>
	<input type="hidden" name="org_id" value=""/>
	<input type="hidden" name="Referer" value="${empty param.Referer ? header.Referer : param.Referer}"/>
</form>

<form name="anaesthesiaform" action="AnaesthesiaTypeMaster.do" autocomplete="off">
	<input type="hidden" name="_method" value="${requestScope.method}" />
	<input type="hidden" name="anesthesia_type_id" value="${ifn:cleanHtmlAttribute(param.anesthesia_type_id)}"/>
	<input type="hidden" id="serviceSubGroup" value="${bean.map.service_sub_group_id}">


	<insta:feedback-panel/>

<fieldset class="fieldSetBorder">
	<table class="formtable">
		<tr>
			<td colspan="6" style="border-bottom: 1px #e6e6e6 solid"><font style="font-weight: bold">Anaesthesia Type: </font></td>
		</tr>
		<tr>
			<td class="formlabel">Anaesthesia Name:</td>
			<td><input type="text" name="anesthesia_type_name" id="anesthesia_type_name" maxlength="100" value="${bean.map.anesthesia_type_name}"
				  title="Anaesthesia Type name is required" onchange="return checkDuplicates();"/></td>
			<td class="formlabel">Status:</td>
			<td>
				<insta:selectoptions name="status" value="${bean.map.status}"
						opvalues="A,I" optexts="Active,Inactive"/>
			</td>
			<td>&nbsp;</td>
			<td>&nbsp;</td>
		</tr>

		<tr>
			<td class="formlabel">Service Group:</td>
			<td>
				<insta:selectdb id="service_group_id" name="service_group_id" value="${groupId}"
					table="service_groups" class="dropdown"   dummyvalue="-- Select --"
					valuecol="service_group_id"  displaycol="service_group_name" onchange="loadServiceSubGroup()" />
			</td>
			<td class="formlabel">Service Sub Group:</td>
			<td>
				<select name="service_sub_group_id" id="service_sub_group_id" class="dropdown">
					<option value="">-- Select --</option>
				</select>
			</td>
			<td class="formlabel">Insurance Category:</td>
			<td>
				<insta:selectdb  name="insurance_category_id" id="insurance_category_id"  value="${insurance_categories}" table="item_insurance_categories" valuecol="insurance_category_id" displaycol="insurance_category_name" filtercol="system_category" filtervalue="N" multiple="true"/>
			</td>
		</tr>
		<tr>
			<td class="formlabel">Unit Size (minutes):</td>
			<td>
				<input type="text" name="duration_unit_minutes" id="duration_unit_minutes"
					onkeypress="return enterNumOnlyzeroToNine(event)" value="${bean.map.duration_unit_minutes}"/>
			</td>
			<td class="formlabel">Allow Zero Claim Amount:</td>
				<td>
					<insta:selectoptions name="allow_zero_claim_amount" value="${empty bean.map.allow_zero_claim_amount ? 'n' : bean.map.allow_zero_claim_amount}" opvalues="n,i,o,b" optexts="No,IP,OP,Both(IP & OP)"/>
				</td>
			<td class="formlabel">Billing Group:</td>
				<td>
					<insta:selectdb  name="billing_group_id" id="billing_group_id"  value="${bean.map.billing_group_id}" table="item_groups" valuecol="item_group_id"
						displaycol="item_group_name" dummyvalue="-- Select --" filtercol="item_group_type_id,status" filtervalue="BILLGRP,A"/>
				</td>
		</tr>
		<tr>
			<td colspan="6" style="border-bottom: 1px #e6e6e6 solid"><font style="font-weight: bold">Charge Calculation: </font></td>
		</tr>
	</table>
	<table class="formtable">
		<tr>
			<td style="width:700px;">
				<fieldset class="fieldSetBorder" style="width:650px;height:100px;">
					<legend class="fieldSetLabel">
						<input type="radio" name="base_unit_select" id="slab_based_select" onclick="enableDisableFields('slab')">
						Slab Based
					</legend>
					<table class="formtable">
						<tr>
							<td class="formlabel">Minimum Duration (units):</td>
							<td>
								<input type="text" name="show_min_duration"  id="show_min_duration"
								onkeypress="return enterNumOnlyzeroToNine(event)" value="${bean.map.min_duration}" onchange="populateHiddenVar('min_duration',this)"/>
								<input type="hidden" name="min_duration" id="min_duration" value="${bean.map.min_duration}"/>
							</td>
							<td class="formlabel">Slab 1 Threshold (units):</td>
							<td>
								<input type="text" name="show_slab_1_threshold" id ="show_slab_1_threshold"
								onkeypress="return enterNumOnlyzeroToNine(event)" value="${bean.map.slab_1_threshold}" onchange="populateHiddenVar('slab_1_threshold',this)"/>
								<input type="hidden" name="slab_1_threshold" id="slab_1_threshold" value="${bean.map.slab_1_threshold}"/>
							</td>
						</tr>
						<tr>
							<td class="formlabel">Incr Duration (units):</td>
							<td>
								<input type="text" name="show_incr_duration" id="show_incr_duration"
								onkeypress="return enterNumOnlyzeroToNine(event)" value="${bean.map.incr_duration}" onchange="populateHiddenVar('incr_duration',this)"/>
								<input type="hidden" name="incr_duration" id="incr_duration" value="${bean.map.incr_duration}"/>
							</td>
						</tr>
					</table>
				</fieldset>
			</td>
			<td style="width:300px;">
				<fieldset class="fieldSetBorder" style="width:300px;height:100px;">
					<legend class="fieldSetLabel">
						<input type="radio" name="base_unit_select" id="base_unit_select"  onclick="enableDisableFields('base')">
						Base Unit
					</legend>
					<table class="formtable">
						<tr>
							<td class="formlabel">Base Unit:</td>
							<td>
								<input type="text" name="show_base_unit" id="show_base_unit" onchange="populateHiddenVar('base_unit',this)"
								onkeypress="return enterNumOnlyzeroToNine(event)" value="${bean.map.base_unit}" />
								<input type="hidden" name="base_unit" id="base_unit" value="${bean.map.base_unit}"/>
							</td>
							<td class="formlabel">&nbsp;</td>
						</tr>
					</table>
				</fieldset>
			</td>
		</tr>
	</table>
	<insta:taxations/>
</fieldset>

<table class="screenActions">
	<tr>
		<td>
			<button type="button" name="Save" accesskey="S" onclick="validateSubmit();"><b><u>S</u></b>ave</button>
		</td>
		<c:if test="${param._method=='show'}">
			<td>&nbsp;|&nbsp;</td>
			<td><a href="${cpath}/master/AnaesthesiaTypeMaster.do?_method=add">Add</a></td>
			<td>&nbsp;|&nbsp;</td>
			<td><a href="${cpath}/master/AnaesthesiaTypeMaster.do?_method=showCharges&anesthesia_type_id=${bean.map.anesthesia_type_id}&org_id=${bean.map.org_id}">Anaesthesia Charges</a></td>
		</c:if>
		<td>&nbsp;|&nbsp;</td>
		<td>
			<a href="${cpath}/master/AnaesthesiaTypeMaster.do?_method=list&status=A&sortOrder=anesthesia_type_name&sortReverse=false&org_id=${bean.map.org_id}">Anaesthesia Type List</a>
		</td>
	</tr>
</table>

</form>
</body>
</html>

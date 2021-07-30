<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>Surgery/Procedure Definition - Insta HMS</title>
	<insta:link type="js" file="hmsvalidation.js" />
	<insta:link type="js" file="masters/charges_common.js" />
	<insta:link type="js" file="masters/operation.js" />
	<insta:link type="js" file="ajax.js" />
	<insta:link type="js" file="masters/orderCodes.js" />

	<script>
	   <c:if test="${param._method != 'add'}">
	    Insta.masterData=${opLists};
	   </c:if>
	   var serviceSubGroupsList = ${serviceSubGroupsList};
	   var operationNames = ${operationNames};
	   var itemGroupList = ${itemGroupListJson};
	   var itemSubGroupList = ${itemSubGroupListJson};
	   function validateForm() {
		 var isInsuranceCatIdSelected = false;
		 var insuranceCatId = document.getElementById('insurance_category_id');
		 for (var i=0; i<insuranceCatId.options.length; i++) {
		   if (insuranceCatId.options[i].selected) {
			 isInsuranceCatIdSelected = true;
		   }
		 }
		 if (isInsuranceCatIdSelected) {
		   var validation = doSave();
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
	   var operationDefaultDuration = ${operation_default_duration}
	</script>
</head>

<body onload="initAddShow();itemsubgroupinit();" class="yui-skin-sam">
<c:choose>
   <c:when test="${param._method != 'add'}">
     <h1 style="float:left">Surgery/Procedure Definition</h1>
     <c:url var="searchUrl" value="/master/OperationMaster.do"/>
     <insta:findbykey keys="operation_name,op_id" fieldName="op_id" method="show" url="${searchUrl}"
     extraParamKeys="_method" extraParamValues="show"/>
  </c:when>
  <c:otherwise>
      <h1>Surgery/Procedure Definition</h1>
  </c:otherwise>
</c:choose>
<insta:feedback-panel/>


<form action="OperationMaster.do" name="showform" method="GET">		<%-- for rate plan change --%>
	<input type="hidden" name="_method" value="${ifn:cleanHtmlAttribute(param._method)}" />
	<input type="hidden" name="op_id" value="${bean.map.op_id}"/>
	<input type="hidden" name="org_id" value=""/>
	<input type="hidden" name="Referer" value="${empty param.Referer ? header.Referer : param.Referer}"/>
</form>

<form name="inputForm" method="POST" action="OperationMaster.do">
	<input type="hidden" name="_method" value="${ifn:cleanHtmlAttribute(method)}" />
	<input type="hidden" name="op_id" value="${bean.map.op_id}"/>
	<input type="hidden" name="Referer" value="${empty param.Referer ? header.Referer : param.Referer}"/>
	<input type="hidden" id="serviceSubGroup" value="${bean.map.service_sub_group_id}">

<fieldset class="fieldSetBorder" >
	<legend class="fieldSetLabel">Surgery/Procedure Details</legend>
	<table class="formtable">
		<tr>
			<td class="formlabel">Surgery/Procedure Name:</td>
			<td>
				<input type="text" name="operation_name" id="operation_name" value="${bean.map.operation_name}" maxlength="100"
				  class="required" title="Operation name is required"/>

				 <input type="hidden" id="OPID" value="${bean.map.op_id}"/>
			</td>

			<td class="formlabel">Department:</td>
			<td>
				<insta:selectdb name="dept_id" id="dept_id" table="department" valuecol="dept_id" displaycol="dept_name"
					orderby="dept_name" value="${bean.map.dept_id}" dummyvalue="..Select.." />
			</td>
			<td class="formlabel">Billing Group:</td>
			<td>
			<insta:selectdb  name="billing_group_id" id="billing_group_id"  value="${bean.map.billing_group_id}" table="item_groups" valuecol="item_group_id" 
				displaycol="item_group_name" dummyvalue="-- Select --" filtercol="item_group_type_id,status" filtervalue="BILLGRP,A"/></td>
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
				<select name="service_sub_group_id" id="service_sub_group_id" class="dropdown" onchange="getOrderCode();">
					<option value="">-- Select --</option>
				</select>
			</td>
			<td></td>
			<td></td>
		</tr>

		<tr>
			<td class="formlabel">Status:</td>
			<td>
				<insta:selectoptions name="status" value="${bean.map.status}"
						opvalues="A,I" optexts="Active,Inactive"/>
			</td>
			<td class="formlabel">Conduction Required:</td>
			<td>
				<insta:radio name="conduction_applicable" radioValues="true,false" radioText="Yes,No"
						value="${empty bean.map.conduction_applicable ? 'true' : bean.map.conduction_applicable}" />
			</td>
		</tr>

		<tr>
			<td class="formlabel">Order Code / Alias:</td>
			<td>
				<input type="text" name="operation_code" value="${bean.map.operation_code}" maxlength="60" />
			</td>
			<td  style="width:180px" class="formlabel">Surgery/Procedure duration : </td>
			<td>
				<input type="text" name="operation_duration" id="operation_duration" class="number" onkeypress="return enterNumOnlyzeroToNine(event)"
						value="${empty bean.map.operation_duration ? 0 : bean.map.operation_duration}"/><span style="padding-left: 5px;position: relative;top: -1px;">mins</span><span class="star">*</span>
			</td>
		</tr>
		<tr>
			<td class="formlabel">Insurance Category:</td>
			<td>
			<insta:selectdb  name="insurance_category_id" id="insurance_category_id"  value="${insurance_categories}" table="item_insurance_categories" valuecol="insurance_category_id" displaycol="insurance_category_name" filtercol="system_category" filtervalue="N" multiple="true"/></td>
			<td class="formlabel">Prior Auth Required:</td>
	 	 	<td>
	 	 		<insta:selectoptions name="prior_auth_required" opvalues="N,S,A" optexts="Never,SomeTimes,Always"
							value="${bean.map.prior_auth_required}" />
	 	 	</td>
			<td class="formlabel">Theatre/Room:</td>
			<td>
        	  <select name="theatre_id" id="theatre_id" class="listbox" multiple="true">
        	     <c:forEach items="${theatres}" var="theatres">
        		    <option value="${theatres.get('theatre_id')}" ${selectedTheatreList.contains(theatres.get('theatre_id')) ? 'selected' : ''}>${ifn:cleanHtml(theatres.get('theatre_name'))}</option>
        	    </c:forEach>
        	  </select>
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
		<tr>
			<td class="formlabel">Remarks:</td>
			<td class="forminfo">
				<input type="text" name="remarks" id="remarks" value="${bean.map.remarks}" maxlength="500">
			</td>
			<td class="formlabel">Allow Zero Claim Amount:</td>
			<td>
				<insta:selectoptions name="allow_zero_claim_amount" value="${empty bean.map.allow_zero_claim_amount ? 'n' : bean.map.allow_zero_claim_amount}" opvalues="n,i,o,b" optexts="No,IP,OP,Both(IP & OP)"/>
			</td>
		</tr>
		<tr>
			<c:if test="${preferences.modulesActivatedMap.mod_cssd eq 'Y'}">
			<td class="formlabel">Kit:</td>
			<td>
				<insta:selectdb name="kit_id" value="${bean.map.kit_id}"
					table="store_kit_main" valuecol="kit_id" orderby="kit_name"
					displaycol="kit_name" dummyvalue="--Select--" dummyvalueId=""/>
			</td>
			</c:if>
		</tr>
	</table>
</fieldset>
<insta:taxations/>

<table class="screenActions">
	<tr>
		<td>
			<button type="button" name="Save" accesskey="S" onclick="validateForm();"><b><u>S</u></b>ave</button>
		</td>
		<c:if test="${param._method=='show'}">
			<td>&nbsp;|&nbsp;</td>
			<td><a href="${pageContext.request.contextPath}/master/OperationMaster.do?_method=add">Add</a></td>
			<td>&nbsp;|&nbsp;</td>
			<td><a href="${pageContext.request.contextPath}/master/OperationMaster.do?_method=showCharges&op_id=${bean.map.op_id}&org_id=${bean.map.org_id}" >Surgery/Procedure Charges</a></td>
		</c:if>
		<td>&nbsp;|&nbsp;</td>
		<td><a href="${pageContext.request.contextPath}/master/OperationMaster.do?_method=list&status=A&sortOrder=operation_name&sortReverse=false" >Surgeries/Procedures List</a></td>
		<td>&nbsp;<insta:screenlink addPipe="true"
					extraParam="?_method=getAuditLogDetails&op_id=${bean.map.op_id}&operation_name=${ifn:encodeUriComponent(bean.map.operation_name)}&al_table=operation_master_audit_log_view"
					screenId="operations_audit_log" label="Audit Log"/>
		</td>
	</tr>
</table>
</form>

</body>
</html>

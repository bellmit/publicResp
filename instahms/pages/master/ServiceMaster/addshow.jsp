<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>

   <script>
      <c:if test="${param._method != 'add'}" >
        Insta.masterData=${serviceList};
      </c:if>
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
      var serviceDefaultDuration = ${service_default_duration}
	  
   </script>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
	<title>Service Definition - Insta HMS</title>
	<insta:link type="js" file="hmsvalidation.js" />
	<insta:link type="js" file="ajax.js" />
	<insta:link type="js" file="masters/charges_common.js" />
	<insta:link type="js" file="masters/service.js" />
	<insta:link type="js" file="masters/orderCodes.js" />
	<insta:link type="js" file="masters/servicesubtask.js" />
</head>

<body onload="initAddShow();initSubtasks();itemsubgroupinit();" class="yui-skin-sam">
<c:choose>
     <c:when test="${param._method != 'add'}">
       <h1 style="float:left">Service Definition</h1>
       <c:url var="searchUrl" value="/master/ServiceMaster.do"/>
       <insta:findbykey keys="service_name,service_id" fieldName="service_id" method="show" url="${searchUrl}"
       extraParamKeys="_method" extraParamValues="show"/>
    </c:when>
    <c:otherwise>
       <h1>Service Definition</h1>
    </c:otherwise>
</c:choose>
<insta:feedback-panel/>

<form action="ServiceMaster.do" name="showform" method="GET">		<%-- for rate plan change --%>
	<input type="hidden" name="_method" value="${ifn:cleanHtmlAttribute(param._method)}" />
	<input type="hidden" name="service_id" value="${bean.map.service_id}"/>
	<input type="hidden" name="org_id" value=""/>
</form>

<form name="inputForm" method="POST" action="ServiceMaster.do">
<input type="hidden" name="_method" value="${ifn:cleanHtmlAttribute(method)}" />
<input type="hidden" name="service_id" value="${bean.map.service_id}"/>
<input type="hidden" name="Referer" value="${ifn:cleanHtmlAttribute(header.Referer)}"/>
<input type="hidden" id="serviceSubGroup" value="${bean.map.service_sub_group_id}">

<fieldset class="fieldSetBorder">
	<legend class="fieldSetLabel">Service Details </legend>
	<table class="formtable">
		<tr>
			<td class="formlabel">Service Name:</td>
			<td>
				<input type="text" name="service_name" value="${bean.map.service_name}" maxlength="600"
				  class="required" title="${bean.map.service_name }"/><span class="star">*</span>
			</td>

			<td class="formlabel">Service Dept:</td>
			<td>
				<insta:selectdb id="serv_dept_id" name="serv_dept_id" value="${bean.map.serv_dept_id}" table="services_departments"
							valuecol="serv_dept_id"  displaycol="department" usecache="true"
							class="dropdown"   dummyvalue="..Department.."  style="width: 140px;"/>
			</td>
			<td class="formlabel">Conducting Doctor Required:</td>
			<td>
				<insta:selectoptions name="conducting_doc_mandatory" value="${empty bean.map.conducting_doc_mandatory ? 'N' : bean.map.conducting_doc_mandatory}" opvalues="N,O,C" optexts="Not Mandatory,Order and Conduction level,Conduction Only"/>
			</td>
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
			<td class="formlabel">Doctor Speciality:</td>
			<td>
				<insta:selectdb id="doc_speciality_id" name="doc_speciality_id" value="${bean.map.doc_speciality_id}" table="doctor_speciality_master"
					valuecol="speciality_id"  displaycol="display_name" class="dropdown" dummyvalue="---select---" />
			</td>
		</tr>

		<tr>
			<td class="formlabel">Status:</td>
			<td>
				<insta:selectoptions name="status" value="${bean.map.status}"
						opvalues="A,I" optexts="Active,Inactive"/>

			<td class="formlabel">Units:</td>
			<td><input type="text" name="units" value="${bean.map.units}" maxlength="20"></td>
			<td class="formlabel">Service Tax (%):</td>
			<td>
				<input type="text" name="service_tax" class="validate-decimal number"
						value="${empty bean.map.service_tax ? 0 : bean.map.service_tax}"/>
			</td>
		</tr>

		<tr>
			<td class="formlabel">Order Code / Alias:</td>
			<td><input type="text" name="service_code" value="${bean.map.service_code}" maxlength="20"></td>

			<td class="formlabel">Conduction Required:</td>
			<td>
				<insta:radio name="conduction_applicable" radioValues="true,false" radioText="Yes,No"
						value="${empty bean.map.conduction_applicable ? 'true' : bean.map.conduction_applicable}" />
			</td>
			<c:if test="${preferences.modulesActivatedMap['mod_dialysis'] eq 'Y' && preferences.modulesActivatedMap['mod_ivf'] eq 'Y'}">
				<td class="formlabel">Specialization:</td>
				<td>
					<insta:selectoptions name="specialization" opvalues="none,D,I" optexts="-- Select --,Dialysis,IVF"
							value="${bean.map.specialization}" />
				</td>
			</c:if>
			<c:if test="${preferences.modulesActivatedMap['mod_dialysis'] eq 'Y' && preferences.modulesActivatedMap['mod_ivf'] ne 'Y'}">
				<td class="formlabel">Specialization:</td>
				<td>
					<insta:selectoptions name="specialization" opvalues="none,D" optexts="-- Select --,Dialysis"
							value="${bean.map.specialization}" />
				</td>
			</c:if>
			<c:if test="${preferences.modulesActivatedMap['mod_dialysis'] ne 'Y' && preferences.modulesActivatedMap['mod_ivf'] eq 'Y'}">
				<td class="formlabel">Specialization:</td>
				<td>
					<insta:selectoptions name="specialization" opvalues="none,I" optexts="-- Select --,IVF"
							value="${bean.map.specialization}" />
				</td>
			</c:if>
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
	 	 	<td class="formlabel">Tooth Num Required:</td>
	 	 	<td >
	 	 		<insta:radio name="tooth_num_required" id="tooth_num_required" radioValues="Y,N" radioText="Yes,No"
						value="${empty bean.map.tooth_num_required ? 'N' : bean.map.tooth_num_required}" />
				<input type="hidden" name="_haad_code" id="haad_code"  />
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
			<td class="formlabel">Remarks:</td>
	 	 	<td class="forminfo">
	 	 		<input type="text" name="remarks" id="remarks" value="${bean.map.remarks}" maxlength="500">
	 	 	</td>
		</tr>
		<tr>
			<td class="formlabel">Conducting Personnel Role:</td>
			<td>
				<insta:selectdb name="conductingRoleId" id="conductingRoleId" table="hospital_roles_master" multiple="true"
						valuecol="hosp_role_id" displaycol="hosp_role_name" values="${conductingRoleIds}"
						orderby="hosp_role_name"/>
			</td>
			<td class="formlabel">Allow Zero Claim Amount:</td>
			<td>
				<insta:selectoptions name="allow_zero_claim_amount" value="${empty bean.map.allow_zero_claim_amount ? 'n' : bean.map.allow_zero_claim_amount}" opvalues="n,i,o,b" optexts="No,IP,OP,Both(IP & OP)"/>
			</td>
			<td class="formlabel">Billing Group:</td>
			<td>
			<insta:selectdb  name="billing_group_id" id="billing_group_id"  value="${bean.map.billing_group_id}" table="item_groups" valuecol="item_group_id" 
				displaycol="item_group_name" dummyvalue="-- Select --" filtercol="item_group_type_id,status" filtervalue="BILLGRP,A"/></td>
	    </tr>
	    <tr>
			<td class="formlabel">Service Duration:</td>
			<td>
				<input type="text" name="service_duration" id="service_duration" class="number" onkeypress="return enterNumOnlyzeroToNine(event)"
						value="${empty bean.map.service_duration ? 0 : bean.map.service_duration}"/><span style="padding-left: 6px;position: relative;top: -1px;">mins</span><span class="star">*</span>
			</td>
			<td class="formlabel">Quantity Split in Pending Prescription:</td>
			<td>
				<insta:selectoptions name="qty_split_in_pending_presc" value="${empty bean.map.qty_split_in_pending_presc ? 'N' : bean.map.qty_split_in_pending_presc}" opvalues="Y,N" optexts="Enabled,Disabled"/>
			</td>
			<td class="formlabel">Schedulable by:</td>
      <td>
         <insta:selectoptions name="scheduleable_by" value="${bean.map.scheduleable_by}"
      		  opvalues="N,S,A"  optexts="None,HMS Staff,All" />
      </td>
		</tr>
		<tr>
			<c:if test="${not empty hl7Interfaces}"> 
				<td class="formlabel">HL7 Export Interface:</td>
				<td>
				<c:set var="numOfInterface" value="${fn:length(interfaceNames)}"></c:set>
					<select multiple="multiple" name="interface_name">
						<c:forEach items="${hl7Interfaces}" var="interfacemap">
							<c:forEach var="i" begin="1" end="${numOfInterface +1}" varStatus="loop">
									<c:if test="${interfaceNames[i-1].map.interface_name eq  interfacemap.map.interface_name}">
										<c:set var="selected" value="true"/>								
									</c:if>
							</c:forEach>
								<option value="${interfacemap.map.interface_name}" ${selected == 'true' ? 'selected' : ''}>${interfacemap.map.interface_name}</option>
								<c:set var="selected" value=""/>
						</c:forEach>
					</select>
				</td>
				<td class="formlabel">Interface Service Code:</td>
				<td>
					<input type="text" name="hl7_export_code" value="${bean.map.hl7_export_code}">
				</td>
			</c:if>
				
			
				<!-- <td><html:text property="hl7ExportCode" maxlength="50"/></td> -->
			<%-- </c:if> --%>
					<td class="formlabel">Reporting Activity Timing In E-claim:</td>
			<td>
				<insta:radio name="activity_timing_eclaim" radioValues="true,false" radioText="Based On Conduction,Based On Billing"
						value="${bean.map.activity_timing_eclaim}" />
			</td>
		</tr>
		<tr>
			<td class="formlabel">Service Resources:</td>
			<td>
        	  <select name="serv_res_id" id="serv_res_id" class="listbox" multiple="true">
        	     <c:forEach items="${service_resources}" var="serviceResource">
        		    <option value="${serviceResource.get('serv_res_id')}" ${selectedServiceResource.contains(serviceResource.get('serv_res_id')) ? 'selected' : ''}>${ifn:cleanHtml(serviceResource.get('serv_resource_name'))}</option>
        	    </c:forEach>
        	  </select>
			</td>
		</tr>
	</table>
</fieldset>

<div class="resultList">
	<legend class="fieldSetLabel" style="margin-top: 10px">Service Sub tasks</legend>
	<table class="detailList dialog_displayColumns" id="subtaskDetailsTable" cellspacing="0" cellpadding="0" border="0" width="100%" style="margin-top: 8px">
		<tr>	   
			<th>Name</th>
			<th>Description</th>
			<th>Status</th>
			<th>Display Order</th>
			<th></th>
		</tr>		
	<c:set var="numServicetasks" value="${fn:length(servicesubtaskList)}"/>
	<c:forEach begin="1" end="${numServicetasks+1}" var="i" varStatus="loop">
		<c:set var="servicesubtaskdetails" value="${servicesubtaskList[i-1]}"/>
		<c:if test="${empty servicesubtaskdetails}">
			<c:set var="style" value='style="display:none"'/>
		</c:if>
		<tr ${style}>
			<td>
				<img src="${cpath}/images/empty_flag.gif"/>
				<input type="hidden" name="sub_task_id" value="${servicesubtaskdetails.map.sub_task_id}"/>
				<input type="hidden" name="service_id" value="${servicesubtaskdetails.map.service_id}"/>
				<input type="hidden" name="desc_short" value="${servicesubtaskdetails.map.desc_short}"/>
				<input type="hidden" name="desc_long" value="${servicesubtaskdetails.map.desc_long}"/>
				<input type="hidden" name="subtask_status" value="${servicesubtaskdetails.map.status}"/>
			 	<input type="hidden" name="display_order" value="${servicesubtaskdetails.map.display_order}"/> 
				<input type="hidden" name="service_subtask_deleted" id="service_subtask_deleted" value="false" />
				<input type="hidden" name="service_subtask_edited" value="false" />
				<insta:truncLabel value="${servicesubtaskdetails.map.desc_short}" length="20"/>
			</td>
			<td>
				<insta:truncLabel value="${servicesubtaskdetails.map.desc_long}" length="50"/>
			</td>
			<td>
					<c:choose>
						<c:when test="${servicesubtaskdetails.map.status == 'A'}"><label>Active</label></c:when>
						<c:when test="${servicesubtaskdetails.map.status == 'I'}"><label>Inactive</label></c:when>
					</c:choose>
			</td>
			
			 <td>
				<insta:truncLabel value="${servicesubtaskdetails.map.display_order}" length="50"/>
			</td> 

			<td style="width: 16px; text-align: center">
				<a name="subtaskEditAnchor" href="javascript:Edit Subtask Details" onclick="return showEditSubtaskDialog(this);"
					title="Edit Subtask Details">
					<img src="${cpath}/icons/Edit.png" class="button" />
				</a>
			</td>
		</tr>
	</c:forEach>
</table>
<table class="addButton" style="height: 25px;">
	<tr>
		<td style="width: 16px;text-align: right">
			<button type="button" name="btnAddSubtask" id="btnAddSubtask" title="Add New Record" onclick="showAddSubtaskDialog(this);return false;" accesskey="+"
				class="imgButton"><img src="${cpath}/icons/Add.png" /></button>
		</td>
	</tr>
</table>
</div>

<div id="addSubtaskDialog" style="display: none">
	<div class="bd">
		<div id="addSubtaskDialogFields">
			<fieldset class="fieldSetBorder">
				<legend class="fieldSetLabel">Add Service Sub task Details</legend>
					<table class="formtable">
						<tr>
							<td class="formlabel">Name: </td>
							<td>
							<input type="hidden" name="d_sub_task_id" id="d_sub_task_id"/>
								<input type="text" name="d_desc_short" maxlength="100" id="d_desc_short" /> 
							</td>
						</tr>
						<tr>
							<td class="formlabel">Description: </td>
							<td colspan="3">
								<textarea name="d_desc_long" id="d_desc_long" rows="4" cols="50"></textarea>
							</td>
						</tr> 
						<tr>
						<td class="formlabel">Status: </td>
						<td>
							<select class="dropdown" id="d_subtask_status">
								<option value="A">Active</option>
								<option value="I">Inactive</option>
							</select>
						</td> 
						</tr> 
						<tr>
							<td class="formlabel">Display Order: </td>
							<td>
								<input type="text" name="d_display_order" id="d_display_order" /> 
							</td>
						</tr>
					</table>
		</fieldset>
	</div>
	<table style="margin-top: 10">
			<tr>
				<td>
					<button type="button" name="subtaskDetails_add_btn" id="subtaskDetails_add_btn" accessKey="A">
						<b><u>A</u></b>dd
					</button>
					<input type="button" name="subtaskDetails_cancel_btn" value="Close" id="subtaskDetails_cancel_btn"/>
				</td>
			</tr>
		</table>
	</div>
</div>

<div id="editSubtaskDialog" style="display: none">
<input type="hidden" name="editSubtaskRowId" id="editSubtaskRowId" value=""/>
	<div class="bd">
			<fieldset class="fieldSetBorder">
				<legend class="fieldSetLabel">Edit Sub task Details</legend>
					<table class="formtable">
						<tr>
						<td class="formlabel">Name: </td>
						<td>
						<input type="hidden" name="ed_sub_task_id" id="ed_sub_task_id"/>
						<input type="text" name="ed_desc_short" id="ed_desc_short" maxlength="100" onchange="setsubtasksEdited();"/> 
						</td>
						</tr>
						<tr>
						<td class="formlabel">Description: </td>
						<td colspan="3">
							<textarea name="ed_desc_long" id="ed_desc_long" rows="4" cols="50" onchange="setsubtasksEdited();"></textarea>
						</td>
					</tr>
					<tr>
						<td class="formlabel">Status: </td>
						<td>
							<select class="dropdown" id="ed_subtask_status" onchange="setsubtasksEdited();">
								<option value="A">Active</option>
								<option value="I">Inactive</option>
							</select>
						</td>
						</tr> 
						<tr>
							<td class="formlabel">Display Order: </td>
							<td>
								<input type="text" name="ed_display_order" id="ed_display_order" onchange="setsubtasksEdited();"/> 
							</td>
						</tr>
					</table>
				<table style="margin-top: 10">
					<tr>
						<td>
							<input type="button" id="edit_SubtaskDetails_Ok" name="editok" value="Ok">
							<input type="button" id="edit_SubtaskDetails_Cancel" name="cancel" value="Cancel" />
							<input type="button" id="edit_SubtaskDetails_Previous" name="previous" value="<<Previous" />
							<input type="button" id="edit_SubtaskDetails_Next" name="next" value="Next>>"/>
						</td>
					</tr>
				</table>
	</fieldset>
</div>
</div>
<insta:taxations/>
<table class="screenActions">
	<tr>
		<td>
			<button type="button" name="Save" accesskey="S" onclick="validateForm();"><b><u>S</u></b>ave</button>
		</td>
		<c:if test="${param._method=='show'}">
			<td>&nbsp;|&nbsp;</td>
			<td><a href="${pageContext.request.contextPath}/master/ServiceMaster.do?_method=add">Add</a></td>
			<td>&nbsp;|&nbsp;</td>
			<td><a href="${pageContext.request.contextPath}/master/ServiceMaster.do?_method=showCharges&service_id=${bean.map.service_id}&org_id=${bean.map.org_id}">Service Charges</a></td>
		</c:if>
		<td>&nbsp;|&nbsp;</td>
		<td>
			<a href="${pageContext.request.contextPath}/master/ServiceMaster.do?_method=list&status=A&sortOrder=service_name&sortReverse=false">Services List</a>
		</td>
		<td>&nbsp;<insta:screenlink screenId="services_audit_log" extraParam="?_method=getAuditLogDetails&al_table=services_audit_log_view&service_id=${bean.map.service_id}&service_name=${ifn:encodeUriComponent(bean.map.service_name)}" label="Audit Log" addPipe="true"/></td>
	</tr>
</table>
</form>

<script>
	var deptNames = ${departmentsJSON};
	var serviceSubGroupsList = ${serviceSubGroupsList};
</script>
</body>
</html>

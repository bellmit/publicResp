<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"
	isELIgnored="false"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@page import="org.apache.struts.Globals" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@page import="com.insta.hms.master.URLRoute"%>
<c:set var="pagePath" value="<%=URLRoute.ADD_EDIT_DIAG_TEST_DETAILS_PATH %>"/>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="actionUrl" value="${cpath}/${pagePath}/update.htm"/>


<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
<title>Insta HMS</title>
<insta:link type="js" file="hmsvalidation.js" />
<insta:link type="js" file="ajax.js" />
<insta:link type="js" file="masters/addtest.js" />
<insta:link type="js" file="masters/orderCodes.js" />
<insta:link type="js" file="masters/hl7interfacemgmt.js" />

<script src="<%=request.getContextPath()%>/gettests.do?${test_timestamp}&${ifn:cleanURL(sesHospitalId)}&module="></script>

<script type="text/javascript">
	var allTestNames = deptWiseTestsjson;
	var max_centers = '${max_centers}';
	var loggedCenter = '${loggedInCenter}';
	var selectedTestId = '<%=request.getAttribute("testIdforCenter")%>';
	var neworedit = 'edit';
	var itemGroupList = ${ifn:convertListToJson(itemGroupListJson)};
	var itemSubGroupList = ${ifn:convertListToJson(itemSubGroupListJson)};
	function validate() {
		var isInsuranceCatIdSelected = false;
		var insuranceCatId = document.getElementById('insurance_category_id');
		for (var i=0; i<insuranceCatId.options.length; i++) {
			if (insuranceCatId.options[i].selected) {
				isInsuranceCatIdSelected = true;
			}
		}
		if (isInsuranceCatIdSelected) {
			var validation = submitValues();
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

<body onload="init();initHl7Mapping();itemsubgroupinit();" class="yui-skin-sam">

	<h1>Test Definitions</h1>
	<insta:feedback-panel/>
	
	<form action="${actionUrl}" name="addtest" method="post">
		<input type="hidden" name="testId" value="${param.testid}"/>
		<input type="hidden" name="orgId" value="${param.orgId}" />
		<input type="hidden" name="contextPath" value="${pageContext.request.contextPath}" />
		<input type="hidden" id="serviceSubGroup" value="${serviceSubGroup}">
		
		
		<c:url var="EC" value="addtest.do">
		<c:param name="_method" value="editTestCharges" />
	</c:url>

	<fieldset class="fieldSetBorder"><legend class="fieldSetLabel">Test Details </legend>

	<table class="formtable">
		<tr>
			<td class="formlabel">Test Name:</td>
			<td><input type="text" name="test_name" id="test_name" onblur="capWords(test_name)" maxlength="600" value="${ifn:cleanHtml(testDetails.get('test_name'))}"/><span class="star">*</span></td>
			<td class="formlabel">Department:</td>
			<td style="white-space:nowrap">
				<b>${testDetails.get('ddept_name')}</b><input type="hidden" name="ddept_id" value="${testDetails.get('ddept_id')}" />
			</td>
			<td class="formlabel">
				Dependent Test:
			</td>
			<td valign="top">
				<div style="display: block; float: left; width: 138px">
					<input type="text" id="dep_test_name" name="dep_test_name" value="${testDetails.get('dependent_test_name')}"/>
					<div id="deptestContainer" class="scrollingDropDown" style="width: 350px;"></div>
					<input type="hidden" name="dependent_test_id" id="dependent_test_id" value="${testDetails.get('dependent_test_id')}"/>
				</div>
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
				</select>
			</td>
			<td class="formlabel">Service Sub Group:</td>
			<td>
				<select name="serviceSubGroupId" id="serviceSubGroupId" class="dropdown" onchange="getOrderCode();">
					<option value="">-- Select --</option>
				</select>
				<input type="hidden" name="service_sub_group_id" id="service_sub_group_id" />
			</td>
			<td class="formlabel">Order Code / Alias:</td>
			<td><input type="text" name="diag_code" maxlength="50" value="${testDetails.get('diag_code')}"/></td>
		</tr>
		<tr>
			<td class="formlabel">Remarks:</td>
			<td>
				<input type="text" name="remarks" value="${testDetails.get('remarks')}"/>
			</td>
			<td class="formlabel">Status:</td>
			<td>
				<select name="status" class="dropdown">
					<option value="A" ${testDetails.get('status') eq 'A' ? 'selected':''}>Active</option>
					<option value="I" ${testDetails.get('status') eq 'I' ? 'selected':''}>InActive</option>
				</select>
			</td>
			<td class="formlabel">Do not auto-share results:</td>
			<td>
				<input type="checkbox" name="isconfidential" ${testDetails.map.isconfidential eq true ? 'checked' : ''}/>
			</td>
			<%-- <td class="formlabel">STAT:</td>
			<td>
				<input type="checkbox" name="statId" id="statId" ${testDetails.map.stat eq 'Y' ? 'checked' : ''}/>
				<input type="hidden" name="stat" id="stat"/>
			</td> --%>
		</tr>
		<tr>
			<c:if test="${not empty hl7Interfaces}">
				<td class="formlabel">Interface Test Code:</td>
				<td><input type="text" name="hl7_export_code" maxlength="50" value="${testDetails.get('hl7_export_code')}"></td>
			</c:if>
			<td class="formlabel">Prescribable:</td>
			<td>
				<select name="is_prescribable" class="dropdown">
					<option value="true" ${testDetails.get('is_prescribable') ? 'selected':''}>Yes</option>
					<option value="false" ${testDetails.get('is_prescribable') ? '':'selected'}>No</option>
				</select>
			</td>
		</tr>
		<tr>
			<td class="formlabel">Test Duration:</td>
				<td>
					<input type="text" 
					name="test_duration" 
					id="test_duration" 
					class="number" 
					onkeypress="return enterNumOnlyzeroToNine(event)"
					value="${ifn:cleanHtml(testDetails.get('test_duration'))}"/>
					<span style="padding-left: 6px;position: relative;top: -1px;">mins</span>
					<span class="star">*</span>
				</td>
		</tr>
		<tr>
		   <td class="formlabel">Test Equipments:</td>
		   <td>
        	  <select name="eq_id" id="eq_id" class="listbox" multiple="true">
        	     <c:forEach items="${testEquipments}" var="tesEquipment">
        		    <option value="${tesEquipment.get('eq_id')}" ${selectedTestEquipments.contains(tesEquipment.get('eq_id')) ? 'selected' : ''}>${tesEquipment.get('equipment_name')}</option>
        	    </c:forEach>
        	  </select>
           </td>
        </tr>
	</table>
	</fieldset>
	<fieldset class="fieldSetBorder"><legend class="fieldSetLabel">Insurance</legend>
	<table class="formtable">
		<tr>
			<td class="formlabel">Insurance Category:</td>
			<td>
				<select name="insurance_category_id" id="insurance_category_id" class="listbox" multiple="true">
					<c:forEach items="${insuranceCategory}" var="insurance">
						<option value="${insurance.get('insurance_category_id')}" ${activeInsuranceCategory.contains(insurance.get('insurance_category_id')) ? 'selected' : ''}>${insurance.get('insurance_category_name')}</option>
					</c:forEach>
				</select>
			</td>
			<td class="formlabel">Pre Auth Required:</td>
	 	 	<td>
	 	 		<select name="prior_auth_required" class="dropdown" id="prior_auth_required">
	 	 			<option value="N" ${testDetails.get('prior_auth_required') eq 'N' ? 'selected': '' }>Never</option>
					<option value="S" ${testDetails.get('prior_auth_required') eq 'S' ? 'selected': '' }>Sometimes</option>
					<option value="A" ${testDetails.get('prior_auth_required') eq 'A' ? 'selected': '' } >Always</option>
				</select>
	 	 	</td>
	 	 	<td class="formlabel">Allow Zero Claim Amount:</td>
			<td>
				<insta:selectoptions name="allow_zero_claim_amount" value="${empty testDetails.get('allow_zero_claim_amount') ? 'n' : testDetails.get('allow_zero_claim_amount')}" opvalues="n,i,o,b" optexts="No,IP,OP,Both(IP & OP)"/>
			</td>
		</tr>
		<tr>
			<td class="formlabel">Billing Group:</td>
			<td>
				<insta:selectdb  name="billing_group_id" id="billing_group_id"  value="${testDetails.map.billing_group_id}" table="item_groups" valuecol="item_group_id"
					displaycol="item_group_name" dummyvalue="-- Select --" filtercol="item_group_type_id,status" filtervalue="BILLGRP,A"/>
			</td>
		</tr>
	</table>
	</fieldset>
	
	<fieldset class="fieldSetBorder"><legend class="fieldSetLabel">Charges</legend>
		<table class="formtable">
			<tr>
				<td class="formlabel">Allow Rate Increase:</td>
				<td>
					<input id="" type="radio" checked="1" value="true" name="allow_rate_increase" ${ testDetails.get('allow_rate_increase') eq true ? 'checked' : '' }/>
					     <label>Yes</label>
					<input id="" type="radio" value="false" name="allow_rate_increase" ${testDetails.get('allow_rate_increase') eq false ? 'checked' : '' }/>
				     <label>No</label>
			     </td>
			     <td class="formlabel">Allow Rate Decrease:</td>
			     <td>
					<input id="" type="radio" checked="1" value="true" name="allow_rate_decrease" ${ testDetails.get('allow_rate_decrease') eq true ? 'checked' : '' }/>
					     <label>Yes</label>
					<input id="" type="radio" value="false" name="allow_rate_decrease" ${ testDetails.get('allow_rate_decrease') eq false ? 'checked' : '' }/>
				     <label>No</label>
			     </td>
			</tr>
		</table>
	</fieldset>
	<fieldset class="fieldSetBorder"><legend class="fieldSetLabel">Conduction Details</legend>
	<table class="formtable">
		<tr>
			<td class="formlabel">Sample Needed:</td>
			<td>
				<select name="sample_needed" id="sample_needed" onchange="disableSpec()" class="dropdown">
					<option value="" >-- Select --</option>
					<option value="y" ${testDetails.get('sample_needed') eq 'y' ? 'selected'  : '' }>Yes</option>
					<option value="n" ${testDetails.get('sample_needed') eq 'n' ? 'selected'  : '' } >No</option>
				</select><span class="star">*</span>
			</td>
			<td class="formlabel">Sample Type:</td>
			<td>
				<select name="specimen" class="dropdown">
					<option value="" >..Select..</option>
					<c:forEach items="${sampleTypes}" var="sample">
						<option value="${sample.get('sample_type_id')}" ${testDetails.get('sample_type_id') eq sample.get('sample_type_id') ? 'selected' : '' }>${sample.get('sample_type')}</option>
					</c:forEach>
				</select>
			</td>
			<td class="formlabel">Conduction Required:</td>
			<td>
			    <input id="" type="radio"  name="conduction_applicable"   value="true" checked="1" ${testDetails.get('conduction_applicable') eq true ? 'checked' : ''} onchange="onChangeCondReq();"/>
			     <label>Yes</label>
			   <input id="" type="radio" name="conduction_applicable"  value="false" ${testDetails.get('conduction_applicable') eq false ? 'checked' : ''} onchange="onChangeCondReq();"/>
			     <label>No</label>
			</td>
		</tr>
		<tr>
			<td class="formlabel">Results Entry:</td>
			<td>
				<input  type="radio"  name="results_entry_applicable"  value="true" checked="1" ${testDetails.get('results_entry_applicable') eq true ? 'checked' : ''} onchange="onChangeResultEntryAppl()";/>
			     <label>Yes</label>
			   <input  type="radio" name="results_entry_applicable"  value="false"  ${testDetails.get('results_entry_applicable') eq false ? 'checked' : ''} onchange="onChangeResultEntryAppl()";/>
			     <label>No</label>
			</td>
			<td class="formlabel"> Conduction format:</td>
			<td style="white-space:nowrap">
				 <input type="radio"  name="reportGroup" id="reportformat" value="T" onclick="checkformat();" ${testDetails.get('conduction_format') eq 'T'? 'checked' : ''}>
				 <label for="reportformat">Use Template</label>
				 <input type="radio" name="reportGroup" id="reportvalue" value="V" onclick="checkformat();" ${testDetails.get('conduction_format') eq 'V'? 'checked' : ''}>
				 <label for="reportvalue">Use Values</label>
				 <c:if test="${not empty impressions}">
					 <input type="radio" name="reportGroup" id="reportmicro" value="H" onclick="checkformat()" ${testDetails.get('conduction_format') eq 'H'? 'checked' : ''}>
					 <label for="reportvalue">Histopathology</label>
				</c:if>
				<c:if test="${not empty antibiotics}">
					 <input type="radio" name="reportGroup" id="reporthisto" value="M" onclick="checkformat()" ${testDetails.get('conduction_format') eq 'M'? 'checked' : ''}>
					 <label for="reportvalue">Microbiology</label>
				 </c:if>
				 <c:if test="${not empty impressions}">
					 <input type="radio" name="reportGroup" id="reportcyto" value="C" onclick="checkformat()" ${testDetails.get('conduction_format') eq 'C'? 'checked' : ''}>
					 <label for="reportvalue">Cytology</label>
				 </c:if>
			</td>
			<td class="formlabel">Conducting Doctor Required:</td>
			<td>
				<select name="conducting_doc_mandatory" id="conducting_doc_mandatory" class="dropdown">
					<option value="N" ${testDetails.get('conducting_doc_mandatory') eq 'N' ? 'selected' :'' }>Not Mandatory</option>
					<option value="O" ${testDetails.get('conducting_doc_mandatory') eq 'O' ? 'selected' :'' }>Order and Conduction level</option>
					<option value="C" ${testDetails.get('conducting_doc_mandatory') eq 'C' ? 'selected' :'' }>Conduction Only</option>
				</select>
			</td>
		</tr>
	 	 <tr>
	 	 	<td class="formlabel">Sample Instructions:</td>
	 	 	<td>
	 	 		<textarea name="sampleCollectionInstructions" id="sampleCollectionInstructions" onblur="checkLength(this,100,'Sample Instructions')">${testDetails.get('sample_collection_instructions')}</textarea> 
	 	 	</td>
	 	 	<td class="formlabel">Conduction Instructions:</td>
	 	 	<td>
	 	 		<textarea name="conductionInstructions" id="conductionInstructions" onblur="checkLength(this,100,'Conduction Instructions')" >${testDetails.get('conduction_instructions')}</textarea>
	 	 	</td>
			<td class="formlabel">Results Validation:</td>
			<td><textarea name="resultsValidation" id="resultsValidation" disabled="true">${testDetails.get('results_validation')}</textarea></td>
	 	 </tr>
	 	 <tr>
	 	 	<td class="formlabel">Mandate Additional Info:</td>
			<td>
				<select name="mandate_additional_info" id="mandate_additional_info" class="dropdown" onchange="disableTestAddnlInfo();">
					<option value="N" ${testDetails.get('mandate_additional_info') eq 'N' ? 'selected' : ''}>No</option>
					<option value="O" ${testDetails.get('mandate_additional_info') eq 'O' ? 'selected' : ''}>Order level</option>
				</select>
				</td>
			<td class="formlabel">Additional Test Information:</td>
			<td>
				<textarea name="test_additional_info" id="test_additional_info" >${testDetails.get('additional_info_reqts')}</textarea>
			</td>
	 	 	<td class="formlabel">Conducting Personnel Role:</td>
			<td>
				<select name="conductingRoleId" id="conductingRoleId" multiple="multiple" class="listbox">
					<c:forEach items="${hospRolesMasterData}" var="hospRoles">
						<option value="${hospRoles.get('hosp_role_id')}" ${ifn:arrayFind(conductingRoleIds, hospRoles.get('hosp_role_id')) != -1 ? 'selected' : ''}>${hospRoles.get('hosp_role_name')}</option>
					</c:forEach>
				</select>
			</td>
	 	 </tr>
	 	 <tr>
	 	 	<td class="formlabel">Mandate Clinical Info:</td>
			<td>
				<select name="mandate_clinical_info" id="mandate_clinical_info" class="dropdown">
					<option value="N" ${testDetails.get('mandate_clinical_info') eq 'N' ? 'selected' : ''}>No</option>
					<option value="P" ${testDetails.get('mandate_clinical_info') eq 'P' ? 'selected' : ''}>Prescription level</option>
				</select>
				</td>
			<td class="formlabel">Clinical Note:</td>
			<td>
				<textarea name="clinical_justification" id="clinical_justification" >${testDetails.get('clinical_justification')}</textarea>
			</td>
		 </tr>
	 	 <tr>
		 	 <td class="formlabel">Test Result Validity Period:</td>
		 	 <td>
		 	 	<input type="text" name="result_validity_period"  style="width:20%" maxlength="3" onkeypress="return enterNumOnlyzeroToNine(event)" class="numeric" value="${testDetails.get('result_validity_period')}"/>
		 	 	<select name="result_validity_period_units" id="result_validity_period_units" style="width:64%" class="dropdown" >
					<option value="D" ${testDetails.get('result_validity_period_units') eq 'D' ? 'selected' : ''}>Days</option>
					<option value="M" ${testDetails.get('result_validity_period_units') eq 'M' ? 'selected' : ''}>Months</option>
					<option value="Y" ${testDetails.get('result_validity_period_units') eq 'Y' ? 'selected' : ''}>Years</option>
				</select>
		 	 </td>
	 	 </tr>
		 
		</table>
	</fieldset>
	<div class="bd" id="bd2">
	<fieldset class="fieldSetBorder">
		<table class="resultList" id="hl7mappingtable">
			<legend class="fieldSetLabel" id="fieldLabel">HL7 Export Interface</legend>
				<tr class="header">
					<th>Message Type</th>
					<th>Interface Name</th>
					<th></th>
					<th></th>
				</tr>
				<c:set var="numOfRecords" value="${fn:length(hl7mappingRecords)}"/>
				<c:forEach begin="1" end="${numOfRecords+1}" var="i" varStatus="loop">
				<c:set var="hl7mappingRows" value="${hl7mappingRecords[i-1]}"/>
					<c:if test="${empty hl7mappingRows}">
						<c:set var="style" value='style="display:none"'/>
					</c:if>
					<c:set var="messageType">
						<c:choose>
							<c:when test="${hl7mappingRows.get('item_type') == 'TEST'}">Send Order</c:when>
							<c:when test="${hl7mappingRows.get('item_type') == 'TESTRESULT'}">Send Result</c:when>
							<c:when test="${hl7mappingRows.get('item_type') == 'TESTMODIFIED'}">Status Change</c:when>
						</c:choose>
					</c:set>
					<tr ${style}>
						<td>
							<img src="${cpath}/images/empty_flag.gif"/>
							<input type="hidden" name="hl7_mapping_test_id" value="${hl7mappingRows.get('test_id')}"/>
							<input type="hidden" name="item_type" value="${hl7mappingRows.get('item_type')}"/>
							<input type="hidden" name="interface_name" value="${hl7mappingRows.get('interface_name')}"/>
							<input type="hidden" name="hl7_lab_interface_id" value="${hl7mappingRows.get('hl7_lab_interface_id')}"/>
							<input type="hidden" name="hl7_mapping_deleted" id="hl7_mapping_deleted" value="false" />
							<input type="hidden" name="hl7_mapping_edited" value="false" />
							<insta:truncLabel value="${messageType}" length="20"/>
						</td>
						<td>
							<insta:truncLabel value="${hl7mappingRows.get('interface_name')}" length="20"/>
						</td>
						<td style="width: 16px; text-align: center">
							<a href="javascript:Cancel Hl7 Mapping Details" onclick="return cancelHl7MappingDetails(this);" title="Cancel Hl7Mapping" >
								<img src="${cpath}/icons/delete.gif" class="imgDelete button" />
							</a>
						</td>
						<td style="width: 16px; text-align: center">
							<a name="hl7mappingEditAnchor" href="javascript:HL7 Interface Mapping Details" onclick="return showEditHl7MappingDialog(this);"
								title="HL7 Interface Mapping Details">
								<img src="${cpath}/icons/Edit.png" class="button" />
							</a>
						</td>
					</tr>
				</c:forEach>
		</table>
		<table class="addButton" style="height: 25px;">
		<tr>
			<td style="width: 16px;text-align: right">
				<button type="button" name="btnAddHl7mapping" id="btnAddHl7mapping" title="Add New Record" onclick="showAddHl7mappingDialog(this);return false;" accesskey="+"
					class="imgButton"><img src="${cpath}/icons/Add.png" /></button>
			</td>
		</tr>
		</table>
	</fieldset>
	</div>
	<div id="addHl7mappingDialog" style="display: none">
	<div class="bd">
		<div id="addHL7mappingDialogFields">
			<fieldset class="fieldSetBorder">
				<legend class="fieldSetLabel">Add HL7 Export Interface</legend>
					<table class="formtable">
						<tr>
						<td class="formlabel">Message Type: </td>
							<td>
								<select class="dropdown" id="d_item_type" name="d_item_type">
									<option value="">--Select--</option>
									<option value="TEST">Send Order</option>
									<option value="TESTRESULT">Send Result</option>
									<option value="TESTMODIFIED">Status Change</option>
								</select>
							</td>
							<td class="formlabel">Interface Name: </td>
							<td>
								<select name="d_interface_name" id="d_interface_name" class="dropdown">
									<option value="">--Select--</option>
									<c:forEach items="${hl7InterfacesMasterData}" var="inter">
	 	 								<option value="${inter['hl7_lab_interface_id']}">${inter['interface_name']}</option>
									</c:forEach>
								</select>
							</td> 
						</tr>
					</table>
			</fieldset>
		</div>
		<table style="margin-top: 10">
			<tr>
				<td>
					<button type="button" name="hl7mapping_add_btn" id="hl7mapping_add_btn" accessKey="A">
						<b><u>A</u></b>dd
					</button>
					<input type="button" name="hl7mapping_cancel_btn" value="Close" id="hl7mapping_cancel_btn"/>
				</td>
			</tr>
		</table>
	</div>
	</div>
	<div id="editHl7mappingDialog" style="display: none">
<input type="hidden" name="editHl7MappingRowId" id="editHl7MappingRowId" value=""/>
	<div class="bd">
			<fieldset class="fieldSetBorder">
				<legend class="fieldSetLabel">Edit HL7 Export Interface</legend>
					<table class="formtable">
						<tr>
						<td class="formlabel">Message Type: </td>
							<td>
								<select class="dropdown" id="ed_item_type" name="ed_item_type">
									<option value="">--Select--</option>
									<option value="TEST">Send Order</option>
									<option value="TESTRESULT">Send Result</option>
									<option value="TESTMODIFIED">Status Change</option>
								</select>
							</td>
							<td class="formlabel">Interface Name: </td>
							<td>
								<select name="ed_interface_name" id="ed_interface_name" class="dropdown">
									<option value="">--Select--</option>
									<c:forEach items="${hl7InterfacesMasterData}" var="inter">
	 	 								<option value="${inter['hl7_lab_interface_id']}">${inter['interface_name']}</option>
									</c:forEach>
								</select>
							</td> 
						</tr>
					</table>
				<table style="margin-top: 10">
					<tr>
						<td>
							<input type="button" id="edit_Hl7mappingDetails_Ok" name="editok" value="Ok">
							<input type="button" id="edit_Hl7mappingDetails_Cancel" name="cancel" value="Cancel" />
							<input type="button" id="edit_Hl7mappingDetails_Previous" name="previous" value="<<Previous" />
							<input type="button" id="edit_Hl7mappingDetails_Next" name="next" value="Next>>"/> 
						</td>
					</tr>
				</table>
	</fieldset>
</div>
</div>
<div id="formatdiv" style="display:none">
		<fieldset class="fieldSetBorder"><legend class="fieldSetLabel">Format Details</legend>
			<table class="formtable">
				<tr>
					<td class="formlabel" style="white-space:nowrap">Select Format Template:</td>
					<td style="white-space:nowrap">
						<select name="formatName" style="width:50em"
							multiple="true" size="6" >
							<c:forEach var="item" items="${reportformats}">
								<option value="${item.get('testformat_id')}" ${ifn:arrayFind(templates, item.get('testformat_id')) != -1 ? 'selected' : ''}>
								${item.get('format_name')}</option>	
							</c:forEach>
						</select>
						<span class="star">*</span>
					</td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
					<td>&nbsp;</td>
				</tr>
			</table>
		</fieldset>
	</div>
	<c:set var="loggedInCenter" value="${loggedInCenter}"/>
	<c:set var="newIndexFORdummyRow" value="" />
	<div id="reportvalues" style="display:none" class="resultList">
			<table class="detailList dialog_displayColumns" id="formatTable" cellspacing="0" cellpadding="0" border="0" width="100%" style="empty-cells: show;margin-top: 5px">

				<tr class="header">
					<th>Result Name</th>
					<input type="hidden" name="resultlabel0" id="resultlabel0" value=""/>
					<th>Result Short Name</th>
					<input type="hidden" name="resultlabel_short0" id="resultlabel_short0" value=""/>
					<th align="left">Methodology</th>
					<th align="left">Units</th>
					<th align="left">Data Allowed</th>
					<th align="left">Default Value</th>
					<th align="left">Source</th>
					<th align="left">Result Ranges</th>
					<th align="left">Expression for Result</th>
					<th align="left">Observation Code Type</th>
					<th align="left">Result Code</th>
					<th align="left">Display Order</th>
					<c:if test="${not empty hl7Interfaces}">
					<th align="left">HL7 Interface Code</th>
					</c:if>
					<c:if test="${max_centers > 1}">
						<th align="left">Center</th>
					</c:if>	
					<th></th>
					<th></th>
					<th></th>
				</tr>
				<c:forEach items="${testResults}" var="result" varStatus="st">
					<c:set var="rangeExists" value="0" />
					<c:forEach items="${testsRanges}" var="ranges">
						<c:if test="${result.get('resultlabel_id') eq ranges.get('resultlabel_id')}">
							<c:set var="rangeExists" value="1" />
						</c:if>
					</c:forEach>
					<tr id="row${st.index}" style="display: ${result.get('status') == 'I'? 'none' : 'table-row'}">
						<input type="hidden" name="resultlabel_id" id="" value="${result.get('resultlabel_id')}" />
						<input type="hidden" name="units" id="" value="${result.get('units')}" />
						<input type="hidden" name="order" id="" value="${result.get('display_order')}" />
						<input type="hidden" name="resultLabel" id="" value="<c:out value="${result.get('resultlabel')}"/>"/>
						<input type="hidden" name="resultLabelShort" id="" value="${result.get('resultlabel_short')}" />
						<input type="hidden" name="expression" id="" value="<c:out value="${result.get('expr_4_calc_result')}"/>"/>
						<input type="hidden" name="code_type" id="" value="${result.get('code_type')}" />
						<input type="hidden" name="result_code" id="" value="${result.get('result_code')}" />
						<input type="hidden" name="data_allowed" value="${result.get('data_allowed')}"/>
						<input type="hidden" name="source_if_list" value="<c:out value="${result.get('source_if_list')}"/>"/>
						<input type="hidden" name="selectedrow" id="selectedrow${st.index+1}" value="false"/>
						<input type="hidden" name="added" id="added${st.index+1}" value="N"/>
						<input type="hidden" name="methodId"  id="methodId" value="${result.get('method_id')}">
						<input type="hidden" name="methodName"  id="methodName" value="${result.get('method_name')}">
						<input type="hidden" name="prevMethodId" id="prevMethodId" value="${result.get('method_id')}" />
						<input type="hidden" name="defaultValue" id="" value="${result.get('default_value')}" />
						<c:if test="${not empty hl7Interfaces}">
						<input type="hidden" name="hl7_interface" id="" value="${result.get('hl7_export_code')}"/>
						</c:if>

							<td><insta:truncLabel  value="${result.get('resultlabel')}" length="32"/></td>
								<input type="hidden" id="resultlabel_id${st.index}" name="resultlabel" value="${result.get('resultlabel_id')}" />
							<td>${result.get('resultlabel_short')}</td>
							<td>${result.get('method_name')}</td>
							<td>${result.get('units')}</td>
							<td>${result.get('data_allowed') eq 'V'?'Any Value':'List'}</td>
							<td><insta:truncLabel value="${result.get('default_value')}" length="16"/></td>
							<td><insta:truncLabel value="${result.get('source_if_list')}" length="16"/></td>
							<td style="text-align: center">
								<c:if test="${ rangeExists == 1 }">
									<img src="${cpath}/images/check-mark-icon.png" alt="" title="Use 'Add/Edit Result Ranges' to modify result range"/>
								</c:if>
							</td>
							<td><insta:truncLabel value="${result.get('expr_4_calc_result')}" length="25"/></td>
							<td>${result.get('code_type')}</td>
							<td><insta:truncLabel value="${result.get('result_code')}" length="16"/></td>
							<td>${result.get('display_order')}</td>
							<c:if test="${not empty hl7Interfaces}">
							<td>${result.get('hl7_export_code')}</td>
							</c:if>
							<c:if test="${max_centers > 1}">
								<td><insta:truncLabel value="${result.get('centers')}" length="20"/></td>
							</c:if>
							<c:forEach items="${result.get('numcenter')}" var="resCenter" begin="0" end="0" varStatus="loopCounter" >					
							
							<c:set var="noOfCenters" value="${fn:split(result.get('numcenter'), ',')}"/>
							<c:set var="numCharges" value="${fn:length(noOfCenters)}"/>
							<td>
								<c:choose>
								<c:when test="${loggedInCenter == 0}">
									<a name="trashIcon" href="javascript:Cancel Item" onclick="return changeElsColor(${st.index+1}, this);" title="Cancel Item">
									<img src="${cpath}/icons/delete.gif" /></a>
								</c:when>
								<c:otherwise>
									<c:choose>
										<c:when test="${loggedInCenter == resCenter && numCharges lt 2 }">
										<a name="trashIcon" href="javascript:Cancel Item" onclick="return changeElsColor(${st.index+1}, this);" title="Cancel Item">
										<img src="${cpath}/icons/delete.gif" /></a>
										</c:when>
									<c:otherwise>
										<a name="trashIcon" href="javascript:Cancel Item" onclick="" title="Cancel Item">
										<img src="${cpath}/icons/delete_disabled.gif" />
										</c:otherwise>
										</c:choose>
								</c:otherwise>
								</c:choose>
							</td>
							<td>
								<c:choose>
									<c:when test="${loggedInCenter == 0}">
									<img src="${cpath}/icons/Edit.png" onclick="onEdit(this)"/>
									</c:when>
									<c:otherwise>
										<c:choose>
										<c:when test="${loggedInCenter == resCenter && numCharges lt 2 }">
										<img src="${cpath}/icons/Edit.png" onclick="onEdit(this)"/>
										</c:when>
										<c:otherwise>
										<img src="${cpath}/icons/Edit1.png" onclick=""/>
										</c:otherwise>
										</c:choose>
									</c:otherwise>
								</c:choose>
							</td>
							</c:forEach>
							<td></td>
					</tr>
					<c:set var="newIndexFORdummyRow" value="${st.index+1}"/>
				</c:forEach>
					<tr id="" style="display: none">
						<input type="hidden" name="resultlabel_id" id="" value="" />
						<input type="hidden" name="units" id="" value="" />
						<input type="hidden" name="order" id="" value="" />
						<input type="hidden" name="resultLabel" id="" value="" />
						<input type="hidden" name="resultLabelShort" id="" value="" />
						<input type="hidden" name="expression" id="" value="" />
						<input type="hidden" name="code_type" id="" value="" />
						<input type="hidden" name="result_code" id="" value="" />
						<input type="hidden" name="data_allowed" value=""/>
						<input type="hidden" name="source_if_list" value=""/>
						<input type="hidden" name="selectedrow" id="selectedrow0" value="false"/>
						<input type="hidden" name="added" id="added0" value="N"/>
						<input type="hidden" name="methodId"  id="methodId" value="">
						<input type="hidden" name="methodName"  id="methodName" value="">
						<input type="hidden" name="prevMethodId" id="prevMethodId" value="" />
						<input type="hidden" name="defaultValue" id="" value="" />
						<c:if test="${not empty hl7Interfaces}">
						<input type="hidden" name="hl7_interface" id="" value=""/>
						</c:if>

							<td><img src="${cpath}/images/empty_flag.gif"></td>
							<td></td>
								<input type="hidden" id="resultlabel_id" name="resultlabel" value="${result.get('resultlabel_id')}" />
							<td></td>
							<td></td>
							<td></td>
							<td></td>
							<td></td>
							<td></td>
							<td></td>
							<td></td>	
							<td></td>	
							<c:if test="${max_centers > 1}">
								<td></td>
							</c:if>
							<td></td>
							<c:if test="${not empty hl7Interfaces}">
							<td></td>
							</c:if>
							<td>
								<a name="trashIcon" href="javascript:Cancel Item" onclick="return changeElsColor('${newIndexFORdummyRow}', this);" title="Cancel Item">
									<img src="${cpath}/icons/delete.gif" />
								</a>
							</td>
							<td><img src="${cpath}/icons/Edit.png" onclick="onEdit(this)" /></td>

					</tr>
			</table>
			<table class="addButton">
					<tr>
						<td width="1000"></td>
						<td>
							<button type="button" name="addresults" Class="imgButton" Id="addresults" onclick="showDialog(this)" >
								<img name="addButton" src="${cpath}/icons/Add.png" />
							</button>
						</td>
					</tr>
			</table>
		</fieldset>
	</div>
<insta:taxations/>
	<table class="screenAction">
		<tr>
			<td><input type="submit" name="submit" value="Save" Class="button" onclick="return validate();">
			</td>
				<td>&nbsp;|&nbsp;</td>
				<td><a href='<c:out value="${cpath}/master/addeditdiagnostics/add.htm?&orgId=${ifn:cleanURL(param.orgId)}"></c:out>'  title="Add">Add</a></td>
				<td>&nbsp;|&nbsp;</td>
				<td><a href='<c:out value="${cpath}/master/addeditdiagnostics/editcharge.htm?&testid=${ifn:cleanURL(testId)}&orgId=${ifn:cleanURL(param.orgId)}"></c:out>' title="Edit Test Charges">Edit Charges</a></td>
				<td id="showOrHideResultRanges" style="margin-top: 6px;">
					<insta:screenlink screenId="mas_edit_result_ranges" extraParam="?_method=list&test_id=${param.testid }&orgId=${param.orgId }"
					label="Add/Edit Result Ranges" addPipe="true"/>				
				</td>
			<c:if test="${max_centers > 1}" >
			<td>&nbsp;|&nbsp;</td>
				<td id="showOrHideCenterApplicability" style="margin-top: 6px;">
					<insta:screenlink screenId="mas_results_cen_app" extraParam="?_method=getScreen&test_id=${param.testid }&orgId=${param.orgId }"
						label="Add/Edit Results Center Applicability" addPipe="false"/>					
				</td>
				<td>&nbsp;|&nbsp;</td>
			</c:if>
			<c:if test="${max_centers == 1}">
			<td>&nbsp;|&nbsp;</td>
			</c:if>
			<td>
			<a href='<c:out value="${cpath}/master/diagnostics.htm?status=A&sortOrder=test_name&sortReverse=false"></c:out>' title="Diagnostic Tests List">Diagnostic Tests List</a></td>
			
			<td>&nbsp;</td>
			<td id="editTestTAT" style="margin-top: 6px;">
					<insta:screenlink screenId="mas_test_tat_master" extraParam="?_method=getScreen&test_id=${testId}"
						label="Add/Edit Test TAT" addPipe="true"/>					
				</td>
			<td>&nbsp;<insta:screenlink screenId="diagnosticTests_audit_log" extraParam="?_method=getAuditLogDetails&al_table=diagnostics_audit_log_view&test_id=${testId}&test_name=${ifn:encodeUriComponent(testName)}" label="Audit Log" addPipe="true"/></td>
			
		</tr>
	</table>
	<div style="display:none"><table id="tabdisplay"></table></div>
	<script>
		var serviceSubGroupsList = ${ifn:convertListToJson(serviceSubGroupsList)};
		var cpath="${cpath}";
		var jdiagdepartments = ${ifn:convertListToJson(diagdepts)};
		<c:if test="${not empty requestScope.newEdit}">
			var neworedit = '${requestScope.newEdit}';
		</c:if>
		<c:if test="${not empty requestScope.testList}">
			var testLists = ${ifn:convertListToJson(requestScope.testList)};
		</c:if>
		<c:if test="${not empty requestScope.codeTypesJSON}">
			var codeTypesJSON = ${ifn:convertListToJson(codeTypesJSON)};
		</c:if>
		<c:if test="${not empty requestScope.testsRangesJSON}">
			var testsRangesJSON = ${testsRangesJSON};
		</c:if>
	</script>

</form>
<form name="editform">

</form>

<div id="resultRangesDIV" style="display:block;visibility:hidden;">
	<div class="bd" id="bd1">
	<fieldset class="fieldSetBorder">
		<legend class="fieldSetLabel" id="fieldLabel"></legend>
		<table class="formTable">
			<tr>
				<td class="formlabel" >Result Name:</td>
				<td><input type="text"  id="resultlabel" name="resultlabel" maxlength="100"></td>
				<td class="formlabel">Result Short Name:</td>
				<td><input type="text"  id="resultlabel_short" name="resultlabel_short" maxlength="10"></td>
			</tr>
			<tr>
				<td class="formlabel">Methodology:</td>
				<td>
					<select  name="methodology" id="methodology" class="dropdown">
					<option value="" >..Select..</option>
					<c:forEach items="${methodologies}" var="method">
						<option value="${method.get('method_id')}">${method.get('method_name')}</option>
					</c:forEach>
				</select>
				</td>
			</tr>
			<tr>
				<td class="formlabel">Data Allowed:</td>
				<td>
					<input id="dataAllowed_V" type="radio" checked="1" value="V" name="dataAllowed" onclick="showSource()"/>
				     <label>Any Value</label>
				   <input id="dataAllowed_L" type="radio" value="false" name="dataAllowed" onclick="showSource()"/>
				     <label>List</label>
				</td>
				<td class="formlabel">Units:</td>
				<td><input type="text"  id="units" name="units" size="3" maxlength="30"></td>
			</tr>
			<tr>
				<td class="formlabel">Expression for Result </td>
				<td colspan="3"><input type="text" id="expression" name="expression" style="width:440px"/></td>
			</tr>
			<tr style="display:none;" id="sourceTr">
				<td class="formlabel">Source:</td>
				<td>
					<input type="text" name="source" id="source"/>
				</td>
			</tr>
			<tr style="displau: none" id="defaultTxtTr">
				<td class="formlabel">Default Value:</td>
				<td colspan="3">
					<textarea name="dlgDefaultValue" id="dlgDefaultValue" style="width: 440px" rows="5"></textarea>
				</td>
			</tr>
			<tr>
				<td class="formlabel">Observation Code Type:</td>
				<td><select id="code_type" name="code_type" class="dropdown" onchange="getResultCodes();"></select></td>
				<td class="formlabel">Result Code:</td>
				<td valign="top">
				<div>
					<input type="text" id="result_code" name="result_code" style="width:137px;" maxlength="50"/>
					<div id="divContainer" name="divConatainer" width="250px"></div>
				<div>
				</td>
			</tr>
			<tr>
				<td class="formlabel">Display Order:</td>
				<td><input type="text" id="order" maxlength="3"  name="order" size="3"  onkeypress="return enterNumOnlyzeroToNine(event)"/></td>
				<c:if test="${not empty hl7Interfaces}">
				<td class="formlabel">HL7 Interface Code:</td>
				<td><input type="text" id="hl7_interface" name="hl7_interface"></td>
				</c:if>
			</tr>
		</table>
		</fieldset>
		<div>
				<input type="button" value="Add" onclick="addToTable()"> |
				<input type="button" value="Cancel" onclick="handleCancel()">
		</div>

	</div>
	</div>		
</body>
	
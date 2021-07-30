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

<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="max_centers" value='<%= GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default") %>' scope="request"/>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>

<html:html>
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
</script>
</head>

<body onload="init();initHl7Mapping();" class="yui-skin-sam">

<c:if test="${requestScope.newEdit == 'new'}"><h1>Add New Test</h1></c:if>
<c:if test="${requestScope.newEdit == 'edit'}"><h1>Test Definition</h1></c:if>

<insta:feedback-panel/>

<html:form action="/pages/masters/hosp/diagnostics/addtest.do" method="post">

	<input type="hidden" name="<%=org.apache.struts.taglib.html.Constants.TOKEN_KEY%>" value='<fmt:message key="${Globals.TRANSACTION_TOKEN_KEY}"/>'/>
	<html:hidden property="_method" value='${method}' />
	<html:hidden property="addOrEdit" value='${requestScope.newEdit}' />
	<html:hidden property="testId" />
	<html:hidden property="contextPath" value="${pageContext.request.contextPath}" />
	<input type="hidden" id="specimenTypeId" value="${typeOfSpeciman}"/>
	<input type="hidden" name="Referer" value="${ifn:cleanHtmlAttribute(header.Referer)}"/>
	<input type="hidden" name="orgId" value="${ifn:cleanHtmlAttribute(param.orgId)}">
	<input type="hidden" id="serviceSubGroup" value="${serviceSubGroup}">

	<c:url var="EC" value="addtest.do">
		<c:param name="_method" value="editTestCharges" />
	</c:url>

	<fieldset class="fieldSetBorder"><legend class="fieldSetLabel">Test Details</legend>

	<table class="formtable">
		<tr>
			<td class="formlabel">Test Name:</td>
			<td><html:text property="testName"	styleId="testName" onblur="capWords(testName)" maxlength="600" /><span class="star">*</span></td>
			<td class="formlabel">Department:</td>
			<td style="white-space:nowrap">
				<c:if test="${newEdit != 'edit'}">
					<html:select property="ddeptId" styleClass="dropdown">
						<option value="">--Dept Name--</option>
					</html:select><span class="star">*</span>
				</c:if>
				<c:if test="${newEdit eq 'edit'}">
					<b>${ddeptName}</b><html:hidden property="ddeptId" />
				</c:if>
			</td>
			<td class="formlabel">
				Dependent Test:
			</td>
			<td valign="top">
				<div style="display: block; float: left; width: 138px">
					<input type="text" id="dep_test_name" name="dep_test_name" value="${testDetails.map.dependent_test_name }"/>
					<div id="deptestContainer" class="scrollingDropDown" style="width: 350px;"></div>
					<input type="hidden" name="dependent_test_id" id="dependent_test_id" value="${testDetails.map.dependent_test_id }"/>
				</div>
			</td>
		</tr>

		<tr>
			<td class="formlabel">Service Group:</td>
			<td>
				<insta:selectdb id="service_group_id" name="service_group_id" value="${groupId}"
					table="service_groups" class="dropdown"   dummyvalue="-- Select --"
					valuecol="service_group_id"  displaycol="service_group_name" onchange="loadServiceSubGroup()" orderby='service_group_name'/>
			</td>
			<td class="formlabel">Service Sub Group:</td>
			<td>
				<select name="serviceSubGroupId" id="serviceSubGroupId" class="dropdown" onchange="getOrderCode();">
					<option value="">-- Select --</option>
				</select>
			</td>
			<td class="formlabel">Order Code / Alias:</td>
			<td><html:text property="diagCode" maxlength="50"/></td>
		</tr>
		<tr>
			<td class="formlabel">Remarks:</td>
			<td>
				<html:text property="remarks"></html:text>
			</td>
			<td class="formlabel">Status:</td>
			<td>
				<html:select property="testStatus" styleClass="dropdown">
					<html:option value="A">Active</html:option>
					<html:option value="I">InActive</html:option>
				</html:select>
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
				<td><html:text property="hl7ExportCode" maxlength="50"/></td>
			</c:if>
		</tr>
	</table>
	</fieldset>


	<fieldset class="fieldSetBorder"><legend class="fieldSetLabel">Insurance</legend>
	<table class="formtable">
		<tr>
			<td class="formlabel">Insurance Category:</td>
			<td>
				<insta:selectdb  name="insurance_category_id" id="insurance_category_id"  value="${insurance_category_id}" table="item_insurance_categories" valuecol="insurance_category_id" displaycol="insurance_category_name" filtercol="system_category" filtervalue="N"/>
			</td>
			<td class="formlabel">Pre Auth Required:</td>
	 	 	<td>
	 	 		<html:select property="preAuthReq" styleClass="dropdown">
	 	 			<html:option value="N">Never</html:option>
					<html:option value="S">Sometimes</html:option>
					<html:option value="A">Always</html:option>
				</html:select>
	 	 	</td>
		</tr>
	</table>
	</fieldset>

	<fieldset class="fieldSetBorder"><legend class="fieldSetLabel">Charges</legend>
		<table class="formtable">
			<tr>
				<td class="formlabel">Allow Rate Increase:</td>
				<td>
					<input id="" type="radio" checked="1" value="true" name="allow_rate_increase" ${ allRateIncr eq true ? 'checked' : '' }/>
					     <label>Yes</label>
					<input id="" type="radio" value="false" name="allow_rate_increase" ${ allRateIncr eq false ? 'checked' : '' }/>
				     <label>No</label>
			     </td>
			     <td class="formlabel">Allow Rate Decrease:</td>
			     <td>
					<input id="" type="radio" checked="1" value="true" name="allow_rate_decrease" ${ allRateDcr eq true ? 'checked' : '' }/>
					     <label>Yes</label>
					<input id="" type="radio" value="false" name="allow_rate_decrease" ${ allRateDcr eq false ? 'checked' : '' }/>
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
				<html:select property="sampleNeed" styleId="sampleNeed" onchange="disableSpec()" styleClass="dropdown">
					<html:option value="">-- Select --</html:option>
					<html:option value="y">Yes</html:option>
					<html:option value="n">No</html:option>
				</html:select><span class="star">*</span>
			</td>
			<td class="formlabel">Sample Type:</td>
			<td>
				<insta:selectdb  name="specimen" value="" table="sample_type" valuecol="sample_type_id" displaycol="sample_type" filtercol="status" filtervalue="A"
				dummyvalue="..Select.."/>
			</td>
			<td class="formlabel">Conduction Required:</td>
			<td>
			    <input id="" type="radio"  name="conduction_applicable"   value="true" checked="1" ${cApplicable eq true ? 'checked' : ''} onchange="onChangeCondReq();"/>
			     <label>Yes</label>
			   <input id="" type="radio" name="conduction_applicable"  value="false" ${cApplicable eq false ? 'checked' : ''} onchange="onChangeCondReq();"/>
			     <label>No</label>
			</td>
		</tr>
		<tr>
			<td class="formlabel">Results Entry:</td>
			<td>
				<input  type="radio"  name="results_entry_applicable"  value="true" checked="1" ${ResEntryApplicable eq true ? 'checked' : ''} onchange="onChangeResultEntryAppl()";/>
			     <label>Yes</label>
			   <input  type="radio" name="results_entry_applicable"  value="false"  ${ResEntryApplicable eq false ? 'checked' : ''} onchange="onChangeResultEntryAppl()";/>
			     <label>No</label>
			</td>
			<td class="formlabel"> Conduction format:</td>
			<td style="white-space:nowrap">
				 <input type="radio"  name="reportGroup" id="reportformat" value="T" onclick="checkformat();" ${reportGroup eq 'T'? 'checked' : ''}>
				 <label for="reportformat">Use Template</label>
				 <input type="radio" name="reportGroup" id="reportvalue" value="V" onclick="checkformat();" ${reportGroup eq 'V'? 'checked' : ''}>
				 <label for="reportvalue">Use Values</label>
				 <c:if test="${not empty impressions}">
					 <input type="radio" name="reportGroup" id="reportmicro" value="H" onclick="checkformat()" ${reportGroup eq 'H'? 'checked' : ''}>
					 <label for="reportvalue">Histopathology</label>
				</c:if>
				<c:if test="${not empty antibiotics}">
					 <input type="radio" name="reportGroup" id="reporthisto" value="M" onclick="checkformat()" ${reportGroup eq 'M'? 'checked' : ''}>
					 <label for="reportvalue">Microbiology</label>
				 </c:if>
				 <c:if test="${not empty impressions}">
					 <input type="radio" name="reportGroup" id="reportcyto" value="C" onclick="checkformat()" ${reportGroup eq 'C'? 'checked' : ''}>
					 <label for="reportvalue">Cytology</label>
				 </c:if>
			</td>
			<td class="formlabel">Conducting Doctor Required:</td>
			<td>
				<html:select property="conducting_doc_mandatory" styleId="conducting_doc_mandatory" styleClass="dropdown">
					<html:option value="N">Not Mandatory</html:option>
					<html:option value="O">Order and Conduction level</html:option>
					<html:option value="C">Conduction Only</html:option>
				</html:select>
			</td>
		</tr>
	 	 <tr>
	 	 	<td class="formlabel">Sample Instructions:</td>
	 	 	<td>
	 	 		<html:textarea property="sampleCollectionInstructions" styleId="sampleCollectionInstructions" onblur="checkLength(this,100,'Sample Instructions')" />
	 	 	</td>
	 	 	<td class="formlabel">Conduction Instructions:</td>
	 	 	<td>
	 	 		<html:textarea property="conductionInstructions" styleId="conductionInstructions" onblur="checkLength(this,100,'Conduction Instructions')" />
	 	 	</td>
			<td class="formlabel">Results Validation:</td>
			<td><html:textarea property="resultsValidation" styleId="resultsValidation" disabled="true" /></td>
	 	 </tr>
	 	 <tr>
	 	 	<td class="formlabel">Mandate Additional Info:</td>
			<td>
				<html:select property="mandate_additional_info" styleId="mandate_additional_info" styleClass="dropdown" onchange="disableTestAddnlInfo();">
					<html:option value="N">No</html:option>
					<html:option value="O">Order level</html:option>
				</html:select>
				</td>
			<td class="formlabel">Additional Test Information:</td>
			<td>
				<html:textarea property="test_additional_info" styleId="test_additional_info"  />
			</td>
	 	 	<td class="formlabel">Conducting Personnel Role:</td>
			<td>
				<insta:selectdb name="conductingRoleId" id="conductingRoleId" table="hospital_roles_master" multiple="true"
						valuecol="hosp_role_id" displaycol="hosp_role_name" values="${addtest.conductingRoleIds}"
						orderby="hosp_role_name"/>
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
							<c:when test="${hl7mappingRows.map.item_type == 'TEST'}">Send Order</c:when>
							<c:when test="${hl7mappingRows.map.item_type == 'TESTRESULT'}">Send Result</c:when>
							<c:when test="${hl7mappingRows.map.item_type == 'TESTMODIFIED'}">Status Change</c:when>
						</c:choose>
					</c:set>
					<tr ${style}>
						<td>
							<img src="${cpath}/images/empty_flag.gif"/>
							<input type="hidden" name="hl7_mapping_test_id" value="${hl7mappingRows.map.test_id}"/>
							<input type="hidden" name="item_type" value="${hl7mappingRows.map.item_type}"/>
							<input type="hidden" name="interface_name" value="${hl7mappingRows.map.interface_name}"/>
							<input type="hidden" name="hl7_mapping_deleted" id="hl7_mapping_deleted" value="false" />
							<input type="hidden" name="hl7_mapping_edited" value="false" />
							<insta:truncLabel value="${messageType}" length="20"/>
						</td>
						<td>
							<insta:truncLabel value="${hl7mappingRows.map.interface_name}" length="20"/>
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
								<insta:selectdb id="d_interface_name" name="d_interface_name" value=""
									table="hl7_lab_interfaces" class="dropdown" dummyvalue="-- Select --"
									valuecol="interface_name"  displaycol="interface_name"/>
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
								<insta:selectdb id="ed_interface_name" name="ed_interface_name" value=""
									table="hl7_lab_interfaces" class="dropdown" dummyvalue="-- Select --"
									valuecol="interface_name"  displaycol="interface_name"/>
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
						<html:select property="formatName" style="width:50em"
							multiple="true" size="6">
							<c:forEach var="item" items="${reportformats}">
								<html:option value="${item.TESTFORMAT_ID}">${item.FORMAT_NAME} </html:option>
							</c:forEach>
						</html:select>
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
						<c:if test="${result.RESULTLABEL_ID eq ranges.resultlabel_id}">
							<c:set var="rangeExists" value="1" />
						</c:if>
					</c:forEach>
					<tr id="row${st.index}" style="display: ${result.STATUS == 'I'? 'none' : 'table-row'}">
						<input type="hidden" name="resultlabel_id" id="" value="${result.RESULTLABEL_ID}" />
						<input type="hidden" name="units" id="" value="${result.UNITS}" />
						<input type="hidden" name="order" id="" value="${result.DISPLAY_ORDER}" />
						<input type="hidden" name="resultLabel" id="" value="<c:out value="${result.RESULTLABEL}"/>"/>
						<input type="hidden" name="resultLabelShort" id="" value="${result.RESULTLABEL_SHORT}" />
						<input type="hidden" name="expression" id="" value="<c:out value='${result.EXPR_4_CALC_RESULT}'/>"/>
						<input type="hidden" name="code_type" id="" value="${result.CODE_TYPE}" />
						<input type="hidden" name="result_code" id="" value="${result.RESULT_CODE}" />
						<input type="hidden" name="data_allowed" value="${result.DATA_ALLOWED }"/>
						<input type="hidden" name="source_if_list" value="<c:out value="${result.SOURCE_IF_LIST }"/>"/>
						<input type="hidden" name="selectedrow" id="selectedrow${st.index+1}" value="false"/>
						<input type="hidden" name="added" id="added${st.index+1}" value="N"/>
						<input type="hidden" name="methodId"  id="methodId" value="${result.METHOD_ID}">
						<input type="hidden" name="methodName"  id="methodName" value="${result.METHOD_NAME}">
						<input type="hidden" name="prevMethodId" id="prevMethodId" value="${result.METHOD_ID}" />
						<input type="hidden" name="defaultValue" id="" value="${result.DEFAULT_VALUE}" />
						<c:if test="${not empty hl7Interfaces}">
						<input type="hidden" name="hl7_interface" id="" value="${result.HL7_EXPORT_CODE }"/>
						</c:if>

							<td><insta:truncLabel  value="${result.RESULTLABEL}" length="32"/></td>
								<input type="hidden" id="resultlabel_id${st.index}" name="resultlabel" value="${result.RESULTLABEL_ID}" />
							<td>${result.RESULTLABEL_SHORT}</td>
							<td>${result.METHOD_NAME}</td>
							<td>${result.UNITS}</td>
							<td>${result.DATA_ALLOWED eq 'V'?'Any Value':'List'}</td>
							<td><insta:truncLabel value="${result.DEFAULT_VALUE}" length="16"/></td>
							<td><insta:truncLabel value="${result.SOURCE_IF_LIST }" length="16"/></td>
							<td style="text-align: center">
								<c:if test="${ rangeExists == 1 }">
									<img src="${cpath}/images/check-mark-icon.png" alt="" title="Use 'Add/Edit Result Ranges' to modify result range"/>
								</c:if>
							</td>
							<td><insta:truncLabel value="${result.EXPR_4_CALC_RESULT}" length="25"/></td>
							<td>${result.CODE_TYPE}</td>
							<td><insta:truncLabel value="${result.RESULT_CODE}" length="16"/></td>
							<td>${result.DISPLAY_ORDER}</td>
							<c:if test="${not empty hl7Interfaces}">
							<td>${result.HL7_EXPORT_CODE}</td>
							</c:if>
							<c:if test="${max_centers > 1}">
								<td><insta:truncLabel value="${result.CENTERS}" length="20"/></td>
							</c:if>
							<c:forEach items="${result.NUMCENTER}" var="resCenter" begin="0" end="0" varStatus="loopCounter" >					
							
							<c:set var="noOfCenters" value="${fn:split(result.NUMCENTER, ',')}"/>
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
								<input type="hidden" id="resultlabel_id" name="resultlabel" value="${result.RESULTLABEL_ID}" />
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

	<table class="screenAction">
		<tr>
			<td><button type="submit" name="submit" style="button" accesskey="S" Class="button" onclick="return submitValues();">
			<b><u>S</u></b>ave</button>
				<c:if test="${newEdit == 'new'}">
					<html:reset property="reset" style="button" value="Reset"
						styleClass="button"></html:reset>
				</c:if>
			</td>
			<c:if test="${newEdit == 'edit'}">
				<td>&nbsp;|&nbsp;</td>
				<td><a href="javascript:void(0);" onclick="window.location.href='${cpath}/pages/masters/hosp/diagnostics/addtest.do?_method=getAddTest&orgId=${ifn:cleanURL(param.orgId)}'">Add</a></td>
				<td>&nbsp;|&nbsp;</td>
				<td><a href='<c:out value="${EC}&testid=${ifn:cleanURL(testId)}&orgId=${ifn:cleanURL(param.orgId)}"></c:out>' title="Edit Test Charges">Edit Charges</a></td>
			</c:if>
			<c:if test="${requestScope.newEdit == 'edit'}">
				<td id="showOrHideResultRanges" style="margin-top: 6px;">
					<insta:screenlink screenId="mas_edit_result_ranges" extraParam="?_method=list&test_id=${param.testid }&orgId=${param.orgId }"
					label="Add/Edit Result Ranges" addPipe="true"/>				
				</td>
			</c:if>
			<c:if test="${max_centers > 1 && requestScope.newEdit == 'edit'}" >
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
			<a href="javascript:void(0);" onclick="doClose();">Diagnostic Tests List</a></td>
			
			<c:if test="${requestScope.newEdit == 'edit'}">
			<td>&nbsp;</td>
			<td id="editTestTAT" style="margin-top: 6px;">
					<insta:screenlink screenId="mas_test_tat_master" extraParam="?_method=getScreen&test_id=${testId}"
						label="Add/Edit Test TAT" addPipe="true"/>					
				</td>
				</c:if>
			<td>&nbsp;<insta:screenlink screenId="diagnosticTests_audit_log" extraParam="?_method=getAuditLogDetails&al_table=diagnostics_audit_log_view&test_id=${testId}&test_name=${ifn:encodeUriComponent(testName)}" label="Audit Log" addPipe="true"/></td>
			
		</tr>
	</table>
	<div style="display:none"><table id="tabdisplay"></table></div>
	<script>
		var serviceSubGroupsList = ${serviceSubGroupsList};
		var cpath="${cpath}"
		<c:if test="${not empty requestScope.diagdepts}">
			var jdiagdepartments = ${requestScope.diagdepts};
		</c:if>
		<c:if test="${not empty requestScope.newEdit}">
			var neworedit = '${requestScope.newEdit}';
		</c:if>
		<c:if test="${not empty requestScope.testList}">
			var testLists = ${requestScope.testList};
		</c:if>
		<c:if test="${not empty requestScope.codeTypesJSON}">
			var codeTypesJSON = ${codeTypesJSON};
		</c:if>
		<c:if test="${not empty requestScope.testsRangesJSON}">
			var testsRangesJSON = ${testsRangesJSON};
		</c:if>
	</script>

</html:form>
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
				<td><insta:selectdb  name="methodology" id="methodology" value="" table="diag_methodology_master" valuecol="method_id" displaycol="method_name" dummyvalueId="" dummyvalue="---Select---"/></td>
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

</html:html>

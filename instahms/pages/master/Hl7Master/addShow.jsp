<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>
	<c:choose>
		
			<c:when test="${param._method == 'add'}">
				<insta:ltext key="generalmasters.hl7configuration.addShow.title.add"></insta:ltext> 
			</c:when>
			<c:otherwise>
				<insta:ltext key="generalmasters.hl7configuration.addShow.title.edit"></insta:ltext>
			</c:otherwise>
	</c:choose>

</title>
<insta:js-bundle prefix="selectdb.dummy" />
<insta:js-bundle prefix="generalmasters.hl7configuration.addShow"/>
<insta:link type="script" file="hmsvalidation.js"/>
<insta:link type="script" file="master/Hl7Master/hl7.js"/>



<script>

	function doClose() {
		window.location.href = "${cpath}/master/HL7Configuration.do?_method=list&sortOrder=interface_type&sortReverse=false&status=A";
	}
	
	var interfaceNameList = <%= request.getAttribute("interfaceNameList") %>;
	var centersJson = ${centersJson};
	
	function checkduplicate(){
		var newInterfaceName = trimAll(document.forms[0].interface_name.value);
		for(var i=0;interfaceNameList !=null && i<interfaceNameList.length;i++){
			interfaceMap = interfaceNameList[i];
			var oldInterfaceName = interfaceMap.INTERFACE_NAME;
			    if (newInterfaceName.toLowerCase() == oldInterfaceName.toLowerCase()) {
			    	var msg=getString("js.generalmasters.hl7configuration.addShow.DuplicateInterface");
			    	alert(document.forms[0].interface_name.value+" "+msg);
			    	document.forms[0].interface_name.value='';
			    	document.forms[0].interface_name.focus();
			    	return false;
		    }
		 }
	}

</script>

	 	

</head>

<body onload="init()">

<form action="HL7Configuration.do" method="POST" name="hl7Configuration">
	<input type="hidden" name="_method" value="${param._method == 'add' ? 'create' : 'update'}">
	<c:if test="${param._method == 'show'}">
		<input type="hidden" name="interaface_name" value="${bean.map.interface_name}"/>
		<input type="hidden" name="hl7_lab_interface_id" value="${bean.map.hl7_lab_interface_id}"/>
	</c:if>
	<c:set var="send_ins_options_txt">
		<insta:ltext key="generalmasters.hl7configuration.addShow.basicInformation.send.ins.option.yes"/>,
		<insta:ltext key="generalmasters.hl7configuration.addShow.basicInformation.send.ins.option.no" />
	</c:set>
	
	<c:set var="result_parameter_options">
		<insta:ltext key="generalmasters.hl7configuration.addShow.basicInformation.resultparameterresource.option.m"/>,
		<insta:ltext key="generalmasters.hl7configuration.addShow.basicInformation.resultparameterresource.option.h"/>
	</c:set>
	
	<c:set var="doctor_identifiers">
		<insta:ltext key="generalmasters.hl7configuration.addShow.basicInformation.options.i"/>,
		<insta:ltext key="generalmasters.hl7configuration.addShow.basicInformation.options.d"/>
	</c:set>

	<c:set var="pageTitle">
		<c:choose>
			<c:when test="${param._method == 'add'}">
				<insta:ltext key="generalmasters.hl7configuration.addShow.pageTitle.add"></insta:ltext>
			</c:when>
			<c:otherwise>
				<insta:ltext key="generalmasters.hl7configuration.addShow.pageTitle.edit"></insta:ltext>
			</c:otherwise>
		
		</c:choose>
	</c:set>
	<c:set var="sendormOptionsTxt">
		<insta:ltext key="generalmasters.hl7configuration.addShow.basicInformation.sendorm.options.oneperresultlabel"/>,
		<insta:ltext key="generalmasters.hl7configuration.addShow.basicInformation.sendorm.options.onepertest"/>
	</c:set>
	
	<div class="pageHeader">${pageTitle}</div>
	<insta:feedback-panel/>
	<fieldset class="fieldsetborder">
		<legend class="fieldSetLabel" id="fieldLabel"><insta:ltext key="generalmasters.hl7configuration.addShow.basicInformation.title"></insta:ltext></legend>
		<table class="formtable">
			<tr>
				<td class="formlabel"><insta:ltext key="generalmasters.hl7configuration.addShow.basicInformation.InterfaceType"></insta:ltext></td>
				<td>
					 <insta:selectoptions name="interface_type" value="${bean.map.interface_type}" class="dropdown" 
					 											opvalues="LIS,RIS,ADT,CONSULTATION,SERVICE,PRESCRIPTION" 
					 											optexts="LIS,RIS,ADT,CONSULTATION,SERVICE,PRESCRIPTION" dummyvalue="--Select--" dummyvalueId=""/>
				</td>
				<td class="formlabel"><insta:ltext key="generalmasters.hl7configuration.addShow.basicInformation.InterfaceName"></insta:ltext></td>
				<td>
					<c:choose>
						<c:when test="${param._method == 'show'}">
							<input type="text" name="interface_name" id="interface_name" value="${bean.map.interface_name}" onchange="checkduplicate();" class="required" readonly="readonly""/>
							<span class="star">*</span>
						</c:when>
						<c:otherwise>
							<input type="text" name="interface_name" value="${bean.map.interface_name}" onblur="checkduplicate();" class="required" />
							<span class="star">*</span>
						</c:otherwise>
					</c:choose>					
				</td>
			</tr>
			<tr>
				<td class="formlabel"><insta:ltext key="generalmasters.hl7configuration.addShow.basicInformation.Status"></insta:ltext></td>
				<td>
					<insta:selectoptions name="status" value="${bean.map.status}" class="dropdown" opvalues="A,I" optexts="Active,InActive"/>
				</td>
				<td class="formlabel"><insta:ltext key="generalmasters.hl7configuration.addShow.basicInformation.SendingFacility"></insta:ltext></td>
				<td>
					<input type="text" name="sending_facility" value="${bean.map.sending_facility}">
				</td>
				
				
			</tr>
			<tr>
				<td class="formlabel"><insta:ltext key="generalmasters.hl7configuration.addShow.basicInformation.SetCompletedStatus"></insta:ltext></td>
				<td>
					<insta:selectoptions name="set_completed_status" value="${bean.map.set_completed_status}" class="dropdown" opvalues="P,C,S" optexts="Pending,Completed,Signed Off"/>
				</td>
				<td class="formlabel"><insta:ltext key="generalmasters.hl7configuration.addShow.basicInformation.ReportGroupMethod"></insta:ltext></td>
				<td>
					<insta:selectoptions id="report_group_method" name="report_group_method" value="${bean.map.report_group_method}" class="dropdown" opvalues="D,O,OD,N" optexts="Department grouping,Common Order grouping,Order+department grouping,No grouping" onchange="validateFields();"/>					
				</td>
			</tr>
			<tr>
				<td class="formlabel"><insta:ltext key="generalmasters.hl7configuration.addShow.basicInformation.AckType"></insta:ltext></td>
				<td>
					<insta:selectoptions opvalues="B,AA,CA"  name="ack_type" value="${bean.map.ack_type}" optexts="Both, App ACK, Commit ACK"/>
				</td>
				<td class="formlabel"><insta:ltext key="generalmasters.hl7configuration.addShow.basicInformation.SendingApp"></insta:ltext></td>
				<td>
					<input type="text" name="sending_app" value="${bean.map.sending_app}"/>
				</td>
			</tr>
			<tr>
				<td class="formlabel"><insta:ltext key="generalmasters.hl7configuration.addShow.basicInformation.EquipmentCodeRequired"></insta:ltext></td>
				<td>
					<input type="checkbox" name="equipment_code_required" value="Y" ${bean.map.equipment_code_required == 'Y' ? 'checked' : ''}/>
				</td>
				<td class="formlabel"><insta:ltext key="generalmasters.hl7configuration.addShow.basicInformation.ConductingDoctorMandatory"></insta:ltext></td>
				<td>
					<input type="checkbox" name="conducting_doctor_mandatory" value="Y" ${bean.map.conducting_doctor_mandatory == 'Y' ? 'checked' : ''} />
				</td>
			</tr>
			<tr>
				<td class="formlabel"><insta:ltext key="generalmasters.hl7configuration.addShow.basicInformation.AppendDoctorSignature"></insta:ltext></td>
				<td>
					<input type="checkbox" name="append_doctor_signature" value="Y"  ${bean.map.append_doctor_signature == 'Y' ? 'checked' : ''}/>
				</td>
				<td class="formlabel"><insta:ltext key="generalmasters.hl7configuration.addShow.basicInformation.ConsolidateMultipleObx"></insta:ltext></td>
				<td>
					<input type="checkbox" id="consolidate_multiple_obx" name="consolidate_multiple_obx" value="Y" ${bean.map.consolidate_multiple_obx == 'Y' ? 'checked' : ''} onclick="validateAllowAddendumOverrideValue()"/>
				</td>
			</tr>
			<tr>
				<td class="formlabel"><insta:ltext key="generalmasters.hl7configuration.addShow.basicInformation.ResultImportDir"></insta:ltext></td>
				<td>
					<input type="text" name="results_import_dir" value="${bean.map.results_import_dir}"/>
				</td>
				<td class="formlabel"><insta:ltext key="generalmasters.hl7configuration.addShow.basicInformation.sendresultas"></insta:ltext></td>
				<td>
					<insta:selectoptions name="send_result_as" value="${bean.map.send_result_as}" class="dropdown" opvalues="V,R" optexts="Value,Report"/>
				</td>
			</tr>
			<tr>
				<td class="formlabel"><insta:ltext key="generalmasters.hl7configuration.addShow.basicInformation.rcvasattachment"></insta:ltext></td>
				<td>
					<input type="checkbox" name="rcv_supporting_doc_ele" id="rcv_supporting_doc_ele" ${(bean.map.rcv_supporting_doc == 'Y') ? 'checked' : ''} onclick="document.getElementById('rcv_supporting_doc').value = (document.getElementById('rcv_supporting_doc_ele').checked ? 'Y' : 'N');"/>
					<input type="hidden" name="rcv_supporting_doc" id="rcv_supporting_doc" value="${empty bean.map.rcv_supporting_doc ? 'N' : bean.map.rcv_supporting_doc}" />
				</td>
				<td class="formlabel"><insta:ltext key="generalmasters.hl7configuration.addShow.basicInformation.closeclientconnection"></insta:ltext></td>
				<td>
					<insta:selectoptions name="close_client_con" value="${bean.map.close_client_con}" class="dropdown" opvalues="Y,N" optexts="Yes,No"/>
				</td>		
			</tr>
			<tr>
				<td class="formlabel"><insta:ltext key="generalmasters.hl7configuration.addShow.basicInformation.sendorm"/></td>
				<td>
					<insta:selectoptions id="send_orm" name="send_orm" value="${bean.map.send_orm}" class="dropdown" opvalues="R,T" optexts="${sendormOptionsTxt}" onchange="validateFields();"/>
				</td>
				<td class="formlabel"><insta:ltext key="generalmasters.hl7configuration.addShow.basicInformation.resultparametersource"></insta:ltext></td>
				<td>
					<insta:selectoptions id="result_parameter_source" name="result_parameter_source" value="${bean.map.result_parameter_source}" class="dropdown" opvalues="M,H" optexts="${result_parameter_options}"/>
				</td>
				<insta:selectoptions style="visibility:hidden;" id="result_parameter_source_hidden" name="result_parameter_source" value="${bean.map.result_parameter_source}" class="dropdown" opvalues="M,H" optexts="${result_parameter_options}"/>
			</tr>
			<tr>
				<td class="formlabel"><insta:ltext key="generalmasters.hl7configuration.addShow.custom.field1" />(<insta:ltext key="generalmasters.hl7configuration.addShow.accession.number" />)</td>
				<td>
					<select class="dropdown" name="custom_field_1">
						<option value=""><insta:ltext key="selectdb.dummy.value"/></option>
						<option value="OBR.20.1" ${bean.map.custom_field_1 eq 'OBR.20.1' ? 'selected' : ''}>OBR-20.1</option> 
					</select>
				</td>
				<td class="formlabel">
					<insta:ltext key="generalmasters.hl7configuration.addShow.basicInformation.send.insurancesegments"></insta:ltext>
				</td>
				<td>
					<insta:selectoptions opvalues="Y,N" optexts="${send_ins_options_txt}" name="send_insurance_segments" value="${empty bean.map.send_insurance_segments ? 'N' : bean.map.send_insurance_segments}"/>
				</td>
			</tr>
			<tr>
				<td class="formlabel">
					<insta:ltext key="generalmasters.hl7configuration.addShow.basicInformation.doctoridentifier"></insta:ltext>
				</td>
				<td>
					<insta:selectoptions opvalues="I,D" optexts="${doctor_identifiers}" name="doctor_identifier" value="${empty bean.map.doctor_identifier ? 'I' : bean.map.doctor_identifier}"/>
				</td>
				<td class="formlabel">
					<insta:ltext key="generalmasters.hl7configuration.addShow.basicInformation.receivePreliminaryReport"></insta:ltext>
				</td>
				<td>
					<insta:selectoptions opvalues="N,Y" optexts="No,Yes" id="receive_preliminary_report" name="receive_preliminary_report" value="${bean.map.receive_preliminary_report}" onchange="setReceivePriliminaryValue();"/>
					<img class="imgHelpText" title="Receive Priliminary report preference is enabled only if Report Group method is set to 'No Grouping' and Send ORM is set to 'One Per Test'"" src="${cpath}/images/help.png"/>
				</td>
				<insta:selectoptions style="visibility:hidden;" opvalues="N,Y" optexts="No,Yes" id="receive_preliminary_report_hidden" name="receive_preliminary_report" value="${bean.map.receive_preliminary_report}"/>
			</tr>
			<tr>
				<td class="formlabel">
					<insta:ltext key="generalmasters.hl7configuration.addShow.basicInformation.allowAddendumOverride"></insta:ltext>
				</td>
				<td>
					<insta:selectoptions opvalues="N,Y" optexts="No,Yes" id="allow_addendum_override" name="allow_addendum_override" value="${bean.map.allow_addendum_override}" onchange="setAllowAddendumOverrideValue();"/>
					<img class="imgHelpText" title="Allow Multiple Addendum preference is enabled only if 'Consolidate multiple obx' is checked" src="${cpath}/images/help.png"/>
				</td>
				<insta:selectoptions style="visibility:hidden;" opvalues="N,Y" optexts="No,Yes" id="allow_addendum_override_hidden" name="allow_addendum_override" value="${bean.map.allow_addendum_override}"/>
			</tr>
		</table>
	</fieldset>

	<fieldSet class="fieldSetBorder">
		<legend class="fieldSetLabel"><insta:ltext key="generalmasters.hl7configuration.addShow.centerwise.title"></insta:ltext></legend>
		<table class="detailList dialog_displayColumns" id="centerConfList" name="centerConfList" cellspacing="0" cellpadding="0" border="0" width="100%" style="empty-cells: show;margin-top: 5px">
			<tr class="header">
				<th><insta:ltext key="generalmasters.hl7configuration.addShow.centerwise.CenterName"></insta:ltext></th>
				<th><insta:ltext key="generalmasters.hl7configuration.addShow.centerwise.ExportType"></insta:ltext></th>
				<th><insta:ltext key="generalmasters.hl7configuration.addShow.centerwise.ExportDir"></insta:ltext></th>
				<th><insta:ltext key="generalmasters.hl7configuration.addShow.centerwise.ExportIpAddr"></insta:ltext></th>
				<th><insta:ltext key="generalmasters.hl7configuration.addShow.centerwise.ExportPort"></insta:ltext></th>
				<!-- <th><insta:ltext key="generalmasters.hl7configuration.addShow.centerwise.ImportType"></insta:ltext></th>
				<th><insta:ltext key="generalmasters.hl7configuration.addShow.centerwise.ImportDir"></insta:ltext></th>
				<th><insta:ltext key="generalmasters.hl7configuration.addShow.centerwise.ImportIpAddr"></insta:ltext></th>
				<th><insta:ltext key="generalmasters.hl7configuration.addShow.centerwise.ImportPort"></insta:ltext></th> -->
				<th></th>
				<th></th> 
			</tr>
			<c:set var="numCenterInterface" value="${fn:length(centerInterfaces)}"></c:set>
			<c:forEach begin="1" end="${numCenterInterface + 1 }" var="i" varStatus="loop">
				<c:set var="centerInterface" value="${centerInterfaces[i-1].map}"/>
				<c:if test="${empty centerInterface}">
					<c:set var="style" value='style="display:none"'/>
				</c:if>
				<tr ${style}>
					<td><label>${centerInterface.center_name}</label>						
						<input name="center_id" id="center_id" type="hidden" value="${centerInterface.center_id}" />
						<input name="center_name" id="center_name" type="hidden" value="${centerInterface.center_name}" />						
						<input name="export_type" type="hidden" value="${centerInterface.export_type}" />
						<input name="orders_export_dir" type="hidden" value="${centerInterface.orders_export_dir}" />
						<input name="orders_export_ip_addr" type="hidden" value="${centerInterface.orders_export_ip_addr}" />
						<input name="orders_export_port" type="hidden" value="${centerInterface.orders_export_port}" />
						<input name="hl7_center_interface_id" type="hidden" value="${centerInterface.hl7_center_interface_id}"/>
					<%-- 	<input name="import_type" type="hidden" value="${centerInterface.import_type}" />
						<input name="import_dir" type="hidden" value="${centerInterface.import_dir}" />
						<input name="import_ip_addr" type="hidden" value="${centerInterface.import_ip_addr}" />
						<input name="import_port" type="hidden" value="${centerInterface.import_port}" /> --%>
						<input name="inserted" id="inserted" value="false" type="hidden"/>
						<input name="deleted" id="deleted" value="false" type="hidden"/> 
						<input name="edited" id="edited" value="false" type="hidden">
					</td>
					<td>${centerInterface.export_type}</td>
					<td>${centerInterface.orders_export_dir}</td>
					<td>${centerInterface.orders_export_ip_addr}</td>
					<td>${centerInterface.orders_export_port}</td>
				<%-- 	<td>${centerInterface.import_type}</td>
					<td>${centerInterface.import_dir}</td>
					<td>${centerInterface.import_ip_addr}</td>
					<td>${centerInterface.import_port}</td> --%>
					
					<td style="text-align: center; ">
						<a name="trashCanAnchor" href="javascript:;" onclick="return cancelCenterInterface(this);" title='Cancel Configuration'>
							<img src="${cpath}/icons/delete.gif" class="imgDelete button" />
						</a>
					</td>
					<td style="text-align: center">
						<a name="editAnchor" href="javascript:;" onclick="return showEditCenterInterface(this);" title='Edit Configuration'>
							<img src="${cpath}/icons/Edit.png" class="button" />
						</a>
					</td>
				</tr>
			</c:forEach>
		
		</table>
		<table class="addButton">
					<tr>
						<td width="1000"></td>
						<td>
							<button type="button" name="configCenterInterface" Class="imgButton" Id="configCenterInterface" onclick="showDialog(this)" >
								<img name="addButton" src="${cpath}/icons/Add.png" />
							</button>
						</td>
					</tr>
		</table>
	</fieldSet>

		<table class="screenActions">
		<tr>
			<td><button type="submit" accesskey="S" onclick="return checkduplicate()"><b><u>S</u></b>ave</button></td>
			<c:if test="${param._method=='show'}">
			<td>&nbsp;|&nbsp;</td>
			<td><a href="javascript:void(0)" onclick="window.location.href='${cpath}/master/HL7Configuration.do?_method=add'">Add</a>
			</c:if>
			<td>&nbsp;|&nbsp;</td>
			<td><a href="javascript:void(0)" onclick="doClose();">HL7 List</a></td>
		</tr>
	</table>
	
	
	
	<div id="centerConfigDivDialog" style="display:block;visibility:hidden;">
		<div class="bd" id="bd1">
			<div id="addcenterConfigDivDialog">
				<fieldset class="fieldSetBorder">
					<legend class="fieldSetLabel" id="fieldLabel"><insta:ltext key="generalmasters.hl7configuration.addShow.centerwise.centerConfigDivDialog.CenterWiseConfiguration.title"></insta:ltext></legend>
						<br/>
						
						<label class="formlabel"><insta:ltext key="generalmasters.hl7configuration.addShow.centerwise.centerConfigDivDialog.CenterWiseConfiguration.CenterId"></insta:ltext></label>
						<insta:selectdb name="d_center_id" value="" table="hospital_center_master" 
										valuecol="center_id" displaycol="center_name" id="d_center_id"></insta:selectdb>
						<br/>
					<fieldset class="fieldSetBorder">
					<legend class="fieldSetLabel" id="fieldLabel"><insta:ltext key="generalmasters.hl7configuration.addShow.centerwise.centerConfigDivDialog.CenterWiseConfiguration.OutgoingMessage"></insta:ltext></legend>
					<table class="formtable">
						<tr>
							<td class="formlabel" ><insta:ltext key="generalmasters.hl7configuration.addShow.centerwise.centerConfigDivDialog.CenterWiseConfiguration.ExportType"></insta:ltext></td>
							<td>
 								<insta:selectoptions  id="d_export_type" opvalues="N,F,S,D,B"  name="d_export_type" value="" optexts="None, File Based, Socket Based, DB Based, File and Socket Both" onchange="checkExportType()"/> 						
							</td>
							
							<td class="formlabel"><insta:ltext key="generalmasters.hl7configuration.addShow.centerwise.centerConfigDivDialog.CenterWiseConfiguration.ExportDir"></insta:ltext></td>
							<td><input type="text"  id="d_orders_export_dir" name="d_orders_export_dir" ></td>
							
						</tr>
					
						<tr>
							<td class="formlabel"><insta:ltext key="generalmasters.hl7configuration.addShow.centerwise.centerConfigDivDialog.CenterWiseConfiguration.ExportIp"></insta:ltext></td>
							<td><input type="text"  id="d_orders_export_ip_addr" name="d_orders_export_ip_addr" ></td>						
							<td class="formlabel"><insta:ltext key="generalmasters.hl7configuration.addShow.centerwise.centerConfigDivDialog.CenterWiseConfiguration.ExportPort"></insta:ltext></td>
							<td><input type="text"  id="d_orders_export_port" name="d_orders_export_port" ></td>
						</tr>
					</table>
					</fieldset>
					<br/>
					<%-- <fieldset class="fieldSetBorder">
					<legend class="fieldSetLabel" id="fieldLabel"><insta:ltext key="generalmasters.hl7configuration.addShow.centerwise.centerConfigDivDialog.CenterWiseConfiguration.IncommingMessage"></insta:ltext></legend>
						
					<table class="formtable">
						<tr>
							<td class="formlabel" ><insta:ltext key="generalmasters.hl7configuration.addShow.centerwise.centerConfigDivDialog.CenterWiseConfiguration.ImportType"></insta:ltext></td>
							<td>
 								<insta:selectoptions opvalues="N,F,S,B"  name="d_import_type" value="" id="d_import_type" 
													optexts="None, File Based, Socket Based, File and Socket Both" onchange="checkImportType()"/>						
							</td>
							
							<td class="formlabel"><insta:ltext key="generalmasters.hl7configuration.addShow.centerwise.centerConfigDivDialog.CenterWiseConfiguration.ImportDir"></insta:ltext></td>
							<td><input type="text"  id="d_import_dir" name="d_import_dir" ></td>
							
						</tr>
					
						<tr>
							<td class="formlabel" ><insta:ltext key="generalmasters.hl7configuration.addShow.centerwise.centerConfigDivDialog.CenterWiseConfiguration.ImportIp"></insta:ltext></td>
							<td><input type="text"  id="d_import_ip_addr" name="d_import_ip_addr" ></td>						
							<td class="formlabel"><insta:ltext key="generalmasters.hl7configuration.addShow.centerwise.centerConfigDivDialog.CenterWiseConfiguration.ImportPort"></insta:ltext></td>
							<td><input type="text"  id="d_import_port" name="d_import_port" ></td>
						</tr>
					</table>
					
					</fieldset>
				</fieldset> --%>
			</div>
			
			<table style="margin-top: 10">
				<tr>
					<td>
						<button type="button" name="Add" id="Add" accesskey="A">
							<insta:ltext key="generalmasters.hl7configuration.addShow.centerwise.centerConfigDivDialog.Add"></insta:ltext>
						</button>
						<input type="button" name="Close" value='<insta:ltext key="generalmasters.hl7configuration.addShow.centerwise.centerConfigDivDialog.Close"></insta:ltext>' id="Close"/>
					</td>
				</tr>
			</table>
		</div>
	</div>
	<div id="editHl7CenterConfig" style="display: none">
		<input type="hidden" name="editRowId" id="editRowId" value=""/>
		<div class="bd">
			<div id="editDialogFieldsDiv">
				<fieldset class="fieldSetBorder">
					<legend class="fieldSetLabel" id="fieldLabel">Center Wise Configuration</legend>
						<br/>
						
						<label class="formlabel"><insta:ltext key="generalmasters.hl7configuration.addShow.centerwise.centerConfigDivDialog.editHl7CenterConfig.CenterId"></insta:ltext></label>
						<input type="hidden" value="" name="ed_center_id" id="ed_center_id"/>
						<input type="text" value="" name="ed_center_name" id="ed_center_name"/>
						<br/>
						<br/>
					<fieldset class="fieldSetBorder">
					<legend class="fieldSetLabel" id="fieldLabel"><insta:ltext key="generalmasters.hl7configuration.addShow.centerwise.centerConfigDivDialog.editHl7CenterConfig.OutgoingMessage"></insta:ltext></legend>
					<table class="formtable">
						<tr>
							<td class="formlabel" ><insta:ltext key="generalmasters.hl7configuration.addShow.centerwise.centerConfigDivDialog.editHl7CenterConfig.ExportType"></insta:ltext></td>
							<td>
 								<insta:selectoptions  id="ed_export_type" opvalues="N,F,S,D,B"  name="ed_export_type" value="" optexts="None, File Based, Socket Based, DB Based, File and Socket Both" onchange="checkEdExportType()"/>						
							</td>
							
							<td class="formlabel"><insta:ltext key="generalmasters.hl7configuration.addShow.centerwise.centerConfigDivDialog.editHl7CenterConfig.ExportDir"></insta:ltext></td>
							<td><input type="text"  id="ed_orders_export_dir" name="ed_orders_export_dir" ></td>
							
						</tr>
					
						<tr>
							<td class="formlabel" ><insta:ltext key="generalmasters.hl7configuration.addShow.centerwise.centerConfigDivDialog.editHl7CenterConfig.ExportIp"></insta:ltext></td>
							<td><input type="text"  id="ed_orders_export_ip_addr" name="ed_orders_export_ip_addr" ></td>						
							<td class="formlabel"><insta:ltext key="generalmasters.hl7configuration.addShow.centerwise.centerConfigDivDialog.editHl7CenterConfig.ExportPort"></insta:ltext></td>
							<td><input type="text"  id="ed_orders_export_port" name="ed_orders_export_port" ></td>
						</tr>
					</table>
					</fieldset>
					<br/>
					<%-- <fieldset class="fieldSetBorder">
					<legend class="fieldSetLabel" id="fieldLabel"><insta:ltext key="generalmasters.hl7configuration.addShow.centerwise.centerConfigDivDialog.editHl7CenterConfig.IncommingMessage"></insta:ltext></legend>
						
					<table class="formtable">
						<tr>
							<td class="formlabel" ><insta:ltext key="generalmasters.hl7configuration.addShow.centerwise.centerConfigDivDialog.editHl7CenterConfig.ImportType"></insta:ltext></td>
							<td>
								<insta:selectoptions opvalues="N,F,S,B"  name="ed_import_type" value="" id="ed_import_type" 
													optexts="None, File Based, Socket Based, File and Socket Both" onchange="checkEdImportType()"/>						
							</td>
							
							<td class="formlabel"><insta:ltext key="generalmasters.hl7configuration.addShow.centerwise.centerConfigDivDialog.editHl7CenterConfig.ImportDir"></insta:ltext></td>
							<td><input type="text"  id="ed_import_dir" name="ed_import_dir" ></td>
							
						</tr>
					
						<tr>
							<td class="formlabel" ><insta:ltext key="generalmasters.hl7configuration.addShow.centerwise.centerConfigDivDialog.editHl7CenterConfig.ImportIp"></insta:ltext></td>
							<td><input type="text"  id="ed_import_ip_addr" name="ed_import_ip_addr" ></td>						
							<td class="formlabel"><insta:ltext key="generalmasters.hl7configuration.addShow.centerwise.centerConfigDivDialog.editHl7CenterConfig.ImportPort"></insta:ltext></td>
							<td><input type="text"  id="ed_import_port" name="ed_import_port" ></td>
						</tr>
					</table>
					
					</fieldset> --%>
				</fieldset>
			</div>
			<table style="margin-top: 10">
				<tr>
					<td>
						<input type="button" id="OK" name="OK" value='<insta:ltext key="generalmasters.hl7configuration.addShow.centerwise.centerConfigDivDialog.OK"></insta:ltext>'/>
						<input type="button" id="EditCancel" name="EditCancel" value='<insta:ltext key="generalmasters.hl7configuration.addShow.centerwise.centerConfigDivDialog.Cancel"></insta:ltext>' />
						<%-- <input type="button" id="EditPrevious" name="EditPrevious" value='<insta:ltext key="generalmasters.hl7configuration.addShow.centerwise.centerConfigDivDialog.Previous"></insta:ltext>' />
						<input type="button" id="EditNext" name="EditNext" value='<insta:ltext key="generalmasters.hl7configuration.addShow.centerwise.centerConfigDivDialog.Next"></insta:ltext>' /> --%>
					</td>
				</tr>
			</table>
		</div>
	</div>	
</form>


	
	

	
</body>
</html>

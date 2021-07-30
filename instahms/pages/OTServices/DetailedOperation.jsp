<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" scope="request"/>
<c:set var="genericPrefs" value="<%= GenericPreferencesDAO.getAllPrefs().getMap()%>" />
<fmt:formatDate pattern="HH:mm" value="<%=new java.util.Date()%>" var="current_time"/>

<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<html>

<head>
	<title>Surgery/Procedure Details Screen - Insta HMS</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<insta:link type="js" file="OTServices/OperationDetails/OperationDetails.js"/>
		<style type="text/css">
		.deletedRow{
			background-color:#EAEAEA; cursor:pointer;
			border-bottom:1px #666 solid;  border-right:1px #999 solid;
			padding:5px 10px 4px 10px;  color:#707070;
		}
	</style>

	<script type="text/javascript">
		var operationsJson = <%= request.getAttribute("operationsJson") %>;
		var surgeonsJson = <%= request.getAttribute("surgeonsJosn") %>;
		var anaesthetistsJson = <%= request.getAttribute("anaesthetistsJosn") %>;
		var operationDetailsJson = <%= request.getAttribute("opearationDetailsJson") %>;
		var doctorsJson = <%= request.getAttribute("doctorsJSON") %>;
	//	var prescribedId = "${prescribed_id}";
		var addedToBill = "${operationDetails.map.added_to_bill}";
		var gPrescDocRequired = '${genPrefs.prescribingDoctorRequired}';
		var fixedOtCharges = '${genPrefs.fixedOtCharges}';
		var regDate = "${regDate}";
		var regTime = "${regTime}";
	</script>
	<insta:js-bundle prefix="registration.patient"/>
</head>
<body onload="init();">
	<h1>Surgery/Procedure Details Screen</h1>
	 <insta:feedback-panel/>
	 <insta:patientdetails visitid="${param.visitId}" showClinicalInfo="true"/>
	 <div style="height:10px;">&nbsp;</div>
	 <form name="detailedOperatioForm" action="Operation.do" method="POST" autocomplete="off">
	 	<input type="hidden" name="patient_id" value="${empty operationDetails.map.patient_id ? param.visitId : operationDetails.map.patient_id}"/>
	 	<input type="hidden" name="mr_no" value="${empty operationDetails.map.patient_id ? param.mr_no : operationDetails.map.mr_no}"/>
	 	<input type="hidden" name="operation_details_id" value="${operationDetails.map.operation_details_id}"/>
	 	<input type="hidden" name="_method" id="_method" value="saveOperationDetails"/>
	 	<c:set var="addedToBill" value="${operationDetails.map.added_to_bill eq 'Y'}"/>
	 	<c:set var="fixedOtCharges" value="${genPrefs.fixedOtCharges}"/>
	 	<div>
	 		<table>
	 			<tr>
	 				<td style="text-align: left;width: 140px;">Scheduler Appointment:</td>
	 				<c:choose>
	 					<c:when test="${empty operationDetails}">
	 						<td colspan="2">
	 							<select name="appointment_id" id="appointment_id" class="dropdown" style="width:400px;">
	 								<option value="">-- Select --</option>
	 								<c:forEach var="schSurgeryApps" items="${surAppDetails}">
	 									<option value="${schSurgeryApps.map.appointment_id}">${schSurgeryApps.map.patient_app_date_time_name_text}</option>
	 								</c:forEach>
	 							</option>
	 						</td>
							<td>&nbsp;</td>
	 					</c:when>
	 					<c:otherwise>
	 						<td class="forminput">
	 							<c:choose>
	 								<c:when test="${not empty operationDetails.map.appointment_id}">
			 							<label style="font-weight: bold">
			 								${operationDetails.map.patient_app_details_showing_text}
			 							</label>
			 						</c:when>
			 						<c:otherwise>
			 							<label style="font-weight: bold;margin-left: -8px;">
			 								Not Applicable
			 							</label>
			 						</c:otherwise>
		 						</c:choose>
	 						</td>
	 					</c:otherwise>
	 				</c:choose>
	 			</tr>
	 		</table>
	 	</div>
	 	<div style="height:10px;">&nbsp;</div>
	 	<div id="op_details_div" style="display:${empty operationDetails ? 'none' : 'block'}">
			<div class="resultList">
				<table class="detailList dialog_displayColumns" cellspacing="0" cellpadding="0" id="procedureDetailsTable" border="0" width="100%" style="margin-top: 10px;">
					<tr>
						<th>Surgery/Procedure Name</th>
						<th>Surgery/Procedure Type</th>
						<th>Modifier</th>
						<th></th>
						<th></th>
						<th></th>
					</tr>
					<c:set var="numOperations" value="${fn:length(patientOperations)}"/>
					<c:forEach begin="1" end="${numOperations+1}" var="i" varStatus="loop">
						<c:set var="operations" value="${patientOperations[i-1].map}"/>
						<c:set var="proc_style" value=""/>
						<c:if test="${empty operations}">
							<c:set var="proc_style" value='style="display:none"'/>
						</c:if>
						<tr ${proc_style}>
							<td><insta:truncLabel value="${operations.operation_name}" length="80"/></td>
							<td>${operations.oper_priority eq 'P' ? 'Primary' : 'Secondary'}</td>
							<td>
								<label>${operations.modifier}</label>
								<input type="hidden" name="operation_proc_id" id="operation_proc_id" value="${operations.operation_proc_id}"/>
								<input type="hidden" name="operation_id" id="operation_id" value="${operations.op_id}"/>
								<input type="hidden" name="operation_name" id="operation_name" value="${operations.operation_name}"/>
								<input type="hidden" name="oper_priority" id="oper_priority" value="${operations.oper_priority}"/>
								<input type="hidden" name="modifier" id="modifier" value="${operations.modifier}"/>
								<input type="hidden" name="op_row_deleted" id="op_row_deleted" value="N"/>
								<input type="hidden" name="op_is_new_row" id="op_is_new_row" value="${empty operations ? 'Y' : 'N'}"/>
								<input type="hidden" name="prescribed_id" id="prescribed_id" value="${operations.prescribed_id}">
							</td>
							<td align="right">
								<c:if test="${preferences.modulesActivatedMap['mod_consumables_flow'] == 'Y'}">
										<c:choose>
											<c:when test="${empty operations.stock_reduced || !operations.stock_reduced}">
												<insta:screenlink screenId="ot_consumables" extraParam="?_method=getModifyOtConsumablesScreen&operation_id=${operations.operation_id}
													&operation_name=${operations.operation_name}&prescribedId=${operations.main_prescribed_id}&patient_id=${patient.patient_id}
													&operation_details_id=${param.operation_details_id}&operation_type=${operations.oper_priority}"
													label="Edit Surgery/Procedure Consumables"/>
												</a>
											</c:when>
										</c:choose>
								</c:if>
							</td>
							<c:choose>
								<c:when test="${addedToBill}">
									<td style="width: 16px; text-align: center">
										<a name="OperationEditAnchor">
											<img src="${cpath}/icons/Edit1.png" name="editIcon" class="button" />
										</a>
									</td>

									<td style="width: 16px; text-align: center">
										<a>
											<img src="${cpath}/icons/delete_disabled.gif" name="oDeleteIcon" class="imgDelete button" />
										</a>
									</td>
								</c:when>
								<c:otherwise>
									<td style="width: 16px; text-align: center">
										<a name="OperationEditAnchor" href="javascript:Edit Operation" onclick="return openEditOpertaionDialog(this,'${operations.op_id}');"
											title="Edit Operation">
											<img src="${cpath}/icons/Edit.png" name="editIcon" class="button" />
										</a>
									</td>

									<td style="width: 16px; text-align: center">
										<a onclick="return cancelOperations(this,'procedureDetailsTable');" title="Cancel Operations" >
											<img src="${cpath}/icons/delete.gif" name="oDeleteIcon" class="imgDelete button" />
										</a>
									</td>
								</c:otherwise>
							</c:choose>
						</tr>
					</c:forEach>
			 	</table>
			 	<table class="addButton">
					<tr>
						<td>&nbsp;</td>
						<c:choose>
							<c:when test="${addedToBill}">
								<td style="width: 16px; text-align: center">
									<button type="button" name="btnOperationAddItem" id="btnOperationAddItem"class="imgButton" disabled>
										<img src="${cpath}/icons/Add1.png">
									</button>
								</td>
							</c:when>
							<c:otherwise>
								<td style="width: 16px; text-align: center">
									<button type="button" name="btnOperationAddItem" id="btnOperationAddItem" accesskey="O" title="Add New Operation(Alt_Shift_O)"
										onclick="showAddEditOperationDialog(this); return false;"
										class="imgButton"><img src="${cpath}/icons/Add.png"></button>
								</td>
							</c:otherwise>
						</c:choose>
					</tr>
				</table>
				<input type="hidden" name="opDialogId" id="opDialogId" value=""/>
		 	</div>

		 	<div class="resultList">
		 		<c:set var="operationSurgeons" value="${operationResources['surgeons']}"/>
				<table class="detailList dialog_displayColumns" cellspacing="0" cellpadding="0" id="surgeonDetailsTable" border="0" width="100%" style="margin-top: 10px;">
					<tr>
						<th>Surgeon/Doctor</th>
						<th>Resource Type</th>
						<th></th>
						<th></th>
					</tr>
					<c:set var="numSurgeons" value="${fn:length(operationSurgeons)}"/>
					<c:set var="surgeonSpec" value=""/>
					<c:forEach begin="1" end="${numSurgeons+1}" var="i" varStatus="loop">
						<c:set var="surgeons" value="${operationSurgeons[i-1].map}"/>
						<c:set var="surgeon_style" value=""/>
						<c:if test="${empty surgeons}">
							<c:set var="surgeon_style" value='style="display:none"'/>
						</c:if>
						<tr ${surgeon_style}>
							<td>${surgeons.doctor_name}</td>
							<c:choose>
								<c:when test="${surgeons.operation_speciality eq 'SU'}">
									<c:set var="surgeonSpec" value="Surgeon/Doctor"/>
								</c:when>
								<c:when test="${surgeons.operation_speciality eq 'ASU'}">
									<c:set var="surgeonSpec" value="Asst Surgeon/Doctor"/>
								</c:when>
								<c:when test="${surgeons.operation_speciality eq 'COSOPE'}">
									<c:set var="surgeonSpec" value="Co-op. Surgeon"/>
								</c:when>
							</c:choose>
							<td>
								<label>${surgeonSpec}</label>
								<input type="hidden" name="su_operation_team_id" id="su_operation_team_id" value="${surgeons.operation_team_id}">
								<input type="hidden" name="su_resource_id" id="su_resource_id" value="${surgeons.resource_id}"/>
								<input type="hidden" name="su_operation_speciality" id="su_operation_speciality" value="${surgeons.operation_speciality}"/>
								<input type="hidden" name="su_row_deleted" id="su_row_deleted" value="N"/>
								<input type="hidden" name="su_is_new_row" id="su_is_new_row" value="${empty surgeons ? 'Y' : 'N'}"/>
							</td>
							<td style="width:200px;">&nbsp;</td>
							<c:choose>
								<c:when test="${addedToBill}">
									<td style="width: 16px; text-align: center">
										<a>
											<img src="${cpath}/icons/delete_disabled.gif" name="suDeleteIcon" id="suDeleteIcon" class="imgDelete button" />
										</a>
									</td>
								</c:when>
								<c:otherwise>
									<td style="width: 16px; text-align: center">
										<a onclick="return cancelSurgeons(this,'surgeonDetailsTable');" title="Cancel Surgeons" >
											<img src="${cpath}/icons/delete.gif" name="suDeleteIcon" id="suDeleteIcon" class="imgDelete button" />
										</a>
									</td>
								</c:otherwise>
							</c:choose>
						</tr>
					</c:forEach>
			 	</table>
			 	<table class="addButton">
					<tr>
						<td>&nbsp;</td>
						<c:choose>
							<c:when test="${addedToBill}">
								<td style="width: 16px; text-align: center">
									<button type="button" name="btnSurgeonAddItem" id="btnSurgeonAddItem" class="imgButton">
										<img src="${cpath}/icons/Add1.png">
									</button>
								</td>
							</c:when>
							<c:otherwise>
								<td style="width: 16px; text-align: center">
									<button type="button" name="btnSurgeonAddItem" id="btnSurgeonAddItem" accesskey="S" title="Add New Surgeon(Alt_Shift_S)"
										onclick="showAddSurgeonsDialog(); return false;"
										class="imgButton"><img src="${cpath}/icons/Add.png"></button>
								</td>
							</c:otherwise>
						</c:choose>
					</tr>
				</table>
		 	</div>

		 	<div class="resultList">
		 		<c:set var="operationPaediatriciansist" value="${operationResources['paediatricians']}"/>
				<table class="detailList dialog_displayColumns" cellspacing="0" cellpadding="0" id="paediatricianDetailsTable" border="0" width="100%" style="margin-top: 10px;">
					<tr>
						<th>Doctor</th>
						<th>Resource Type</th>
						<th></th>
						<th></th>
					</tr>
					<c:set var="numPaediatricians" value="${fn:length(operationPaediatriciansist)}"/>
					<c:forEach begin="1" end="${numPaediatricians+1}" var="i" varStatus="loop">
						<c:set var="paediatricians" value="${operationPaediatriciansist[i-1].map}"/>
						<c:set var="an_style" value=""/>
						<c:if test="${empty paediatricians}">
							<c:set var="paed_style" value='style="display:none"'/>
						</c:if>
						<tr ${paed_style}>
							<td>${paediatricians.doctor_name}</td>
							<td>
								<label>${paediatricians.operation_speciality eq 'PAED' ? 'Doctor' : ''}</label>
								<input type="hidden" name="paed_operation_team_id" id="paed_operation_team_id" value="${paediatricians.operation_team_id}">
								<input type="hidden" name="paed_resource_id" id="paed_resource_id" value="${paediatricians.resource_id}"/>
								<input type="hidden" name="paed_operation_speciality" id="paed_operation_speciality" value="${paediatricians.operation_speciality}"/>
								<input type="hidden" name="paed_row_deleted" id="paed_row_deleted" value="N"/>
								<input type="hidden" name="paed_is_new_row" id="paed_is_new_row" value="${empty paediatricians ? 'Y' : 'N'}"/>
							</td>
							<td style="width:200px;">&nbsp;</td>
							<c:choose>
								<c:when test="${addedToBill}">
									<td style="width: 16px; text-align: center">
										<a>
											<img src="${cpath}/icons/delete_disabled.gif" name="paedDeleteIcon" id="paedDeleteIcon" class="imgDelete button" />
										</a>
									</td>
								</c:when>
								<c:otherwise>
									<td style="width: 16px; text-align: center">
										<a onclick="return cancelPaediatrician(this,'paediatricianDetailsTable');" title="Cancel Paediatrician" >
											<img src="${cpath}/icons/delete.gif" name="paedDeleteIcon" id="paedDeleteIcon" class="imgDelete button" />
										</a>
									</td>
								</c:otherwise>
							</c:choose>
						</tr>
					</c:forEach>
			 	</table>
			 	<table class="addButton">
					<tr>
						<td>&nbsp;</td>
						<c:choose>
							<c:when test="${addedToBill}">
								<td style="width: 16px; text-align: center">
									<button type="button" name="btnPaediatricianAddItem" id="btnPaediatricianAddItem"  class="imgButton">
										<img src="${cpath}/icons/Add1.png">
									</button>
								</td>
							</c:when>
							<c:otherwise>
								<td style="width: 16px; text-align: center">
									<button type="button" name="btnPaediatricianAddItem" id="btnPaediatricianAddItem" accesskey="A" title="Add New Paediatrician(Alt_Shift_A)"
										onclick="showAddPaediatricianDialog(this); return false;"
										 class="imgButton"><img src="${cpath}/icons/Add.png"></button>
								</td>
							</c:otherwise>
						</c:choose>
					</tr>
				</table>
		 	</div>
		 	<div class="resultList">
		 		<c:set var="operationAnaesthetists" value="${operationResources['anestiatists']}"/>
				<table class="detailList dialog_displayColumns" cellspacing="0" cellpadding="0" id="anaesthetistDetailsTable" border="0" width="100%" style="margin-top: 10px;">
					<tr>
						<th>Anaesthetist</th>
						<th>Resource Type</th>
						<th></th>
						<th></th>
					</tr>
					<c:set var="numAnaesthetists" value="${fn:length(operationAnaesthetists)}"/>
					<c:forEach begin="1" end="${numAnaesthetists+1}" var="i" varStatus="loop">
						<c:set var="anaesthetists" value="${operationAnaesthetists[i-1].map}"/>
						<c:set var="an_style" value=""/>
						<c:if test="${empty anaesthetists}">
							<c:set var="an_style" value='style="display:none"'/>
						</c:if>
						<tr ${an_style}>
							<td>${anaesthetists.doctor_name}</td>
							<td>
								<label>${anaesthetists.operation_speciality eq 'AN' ? 'Anaesthetist' : 'Asst Anaesthetist'}</label>
								<input type="hidden" name="an_operation_team_id" id="an_operation_team_id" value="${anaesthetists.operation_team_id}">
								<input type="hidden" name="an_resource_id" id="an_resource_id" value="${anaesthetists.resource_id}"/>
								<input type="hidden" name="an_operation_speciality" id="an_operation_speciality" value="${anaesthetists.operation_speciality}"/>
								<input type="hidden" name="an_row_deleted" id="an_row_deleted" value="N"/>
								<input type="hidden" name="an_is_new_row" id="an_is_new_row" value="${empty anaesthetists ? 'Y' : 'N'}"/>
							</td>
							<td style="width:200px;">&nbsp;</td>
							<c:choose>
								<c:when test="${addedToBill}">
									<td style="width: 16px; text-align: center">
										<a>
											<img src="${cpath}/icons/delete_disabled.gif" name="anDeleteIcon" id="anDeleteIcon" class="imgDelete button" />
										</a>
									</td>
								</c:when>
								<c:otherwise>
									<td style="width: 16px; text-align: center">
										<a onclick="return cancelAnaesthetists(this,'anaesthetistDetailsTable');" title="Cancel Anaesthetists" >
											<img src="${cpath}/icons/delete.gif" name="anDeleteIcon" id="anDeleteIcon" class="imgDelete button" />
										</a>
									</td>
								</c:otherwise>
							</c:choose>
						</tr>
					</c:forEach>
			 	</table>
			 	<table class="addButton">
					<tr>
						<td>&nbsp;</td>
						<c:choose>
							<c:when test="${addedToBill}">
								<td style="width: 16px; text-align: center">
									<button type="button" name="btnAnaesthetistsAddItem" id="btnAnaesthetistsAddItem"  class="imgButton">
										<img src="${cpath}/icons/Add1.png">
									</button>
								</td>
							</c:when>
							<c:otherwise>
								<td style="width: 16px; text-align: center">
									<button type="button" name="btnAnaesthetistsAddItem" id="btnAnaesthetistsAddItem" accesskey="A" title="Add New Anaesthetists(Alt_Shift_A)"
										onclick="showAddAnaesthetistsDialog(this); return false;"
										 class="imgButton"><img src="${cpath}/icons/Add.png"></button>
								</td>
							</c:otherwise>
						</c:choose>
					</tr>
				</table>
		 	</div>
		 	<div class="resultList">
		 		<c:set var="operationAnaesthesiaTypes" value="${operationResources['anaesthesiaTypes']}"/>
				<table class="detailList dialog_displayColumns" cellspacing="0" cellpadding="0" id="anaesthesiaDetailsTable" border="0" width="100%" style="margin-top: 10px;">
					<tr>
						<th>Anesthesia Type</th>
						<th>From</th>
						<th>To</th>
						<th></th>
						<th></th>
					</tr>
					<c:set var="numAnaesthesiaType" value="${fn:length(operationAnaesthesiaTypes)}"/>
					<c:forEach begin="1" end="${numAnaesthesiaType+1}" var="i" varStatus="loop">
						<c:set var="anaesthesiaTypes" value="${operationAnaesthesiaTypes[i-1].map}"/>
						<c:set var="an_type_style" value=""/>
						<c:if test="${empty anaesthesiaTypes}">
							<c:set var="an_type_style" value='style="display:none"'/>
						</c:if>
						<tr ${an_type_style}>
							<td>${anaesthesiaTypes.anesthesia_type_name}</td>
								<fmt:formatDate pattern="dd-MM-yyyy HH:mm:ss" value="${anaesthesiaTypes.anaes_start_datetime}" var="anFrom"/>
								<fmt:formatDate pattern="dd-MM-yyyy HH:mm:ss" value="${anaesthesiaTypes.anaes_end_datetime}" var="anTo"/>
							<td>
								<label>${anFrom}</label>
								<input type="hidden" name="an_type_surgery_anesthesia_details_id" id="an_type_surgery_anesthesia_details_id" value="${anaesthesiaTypes.operation_anae_detail_id}">
								<input type="hidden" name="an_type_anaesthesia_type_id" id="an_type_anaesthesia_type_id" value="${anaesthesiaTypes.anesthesia_type_id}"/>
								<input type="hidden" name="an_type_anaesthesia_type_from" id="an_type_anaesthesia_type_from" value="${anFrom}"/>
								<input type="hidden" name="an_type_anaesthesia_type_to" id="an_type_anaesthesia_type_to" value="${anTo}"/>
								<input type="hidden" name="an_type_row_deleted" id="an_type_row_deleted" value="N"/>
								<input type="hidden" name="an_type_is_new_row" id="an_type_is_new_row" value="${empty anaesthesiaTypes ? 'Y' : 'N'}"/>
							</td>
							<td>${anTo}</td>
							<td style="width:200px;">&nbsp;</td>
							<c:choose>
								<c:when test="${addedToBill}">
									<td style="width: 16px; text-align: center">
										<a>
											<img src="${cpath}/icons/delete_disabled.gif" name="anTypeDeleteIcon" id="anTypeDeleteIcon" class="imgDelete button" />
										</a>
									</td>
								</c:when>
								<c:otherwise>
									<td style="width: 16px; text-align: center">
										<a onclick="return cancelAnaesthesiaType(this,'anaesthesiaDetailsTable');" title="Cancel AnaesthesiaType" >
											<img src="${cpath}/icons/delete.gif" name="anTypeDeleteIcon" id="anTypeDeleteIcon" class="imgDelete button" />
										</a>
									</td>
								</c:otherwise>
							</c:choose>
						</tr>
					</c:forEach>
			 	</table>
			 	<table class="addButton">
					<tr>
						<td>&nbsp;</td>
						<c:choose>
							<c:when test="${addedToBill}">
								<td style="width: 16px; text-align: center">
									<button type="button" name="btnAnaesthesiaTypesAddItem" id="btnAnaesthesiaTypesAddItem"  class="imgButton">
										<img src="${cpath}/icons/Add1.png">
									</button>
								</td>
							</c:when>
							<c:otherwise>
								<td style="width: 16px; text-align: center">
									<button type="button" name="btnAnaesthsiaTypesAddItem" id="btnAnaesthsiaTypesAddItem" accesskey="A" title="Add New Anaesthesia Type(Alt_Shift_A)"
										onclick="showAddAnaesthesiaTypeDialog(this); return false;"
										 class="imgButton"><img src="${cpath}/icons/Add.png"></button>
								</td>
							</c:otherwise>
						</c:choose>
					</tr>
				</table>
		 	</div>
		 	<div style="height:10px;">&nbsp;</div>
		 	<div>
		 		<fieldset class="fieldSetBorder">
					<legend class="fieldSetLabel">Theater / Room</legend>
			 		<table class="formtable">
			 			<tr>
							<td class="formlabel">Theatre/Room:</td>
							<td style="width:200px;">
								<select name="theatre_id" id="theatre_id" class="dropdown" ${(fixedOtCharges == 'Y' || addedToBill ) ? 'disabled=' : ''}>
									<option value="">-- Select --</option>
									<c:forEach items="${userCenterOTLists}" var="ot">
										<option value="${ot.map.theatre_id}" ${operationDetails.map.theatre_id == ot.map.theatre_id ? 'selected' : ''}>
											${ot.map.theatre_name }
										</option>
									</c:forEach>
								</select>
							</td>
							<td class="formlabel" >Charge Type:</td>
							<td style="width:330px;">
								<select name="charge_type" id="charge_type" class="dropdown" ${(fixedOtCharges == 'Y' || addedToBill) ? 'disabled=' : ''}>
									<option value="">-- Select --</option>
									<option value="H" ${operationDetails.map.charge_type == 'H' ? 'selected' : ''}>Hourly</option>
									<option value="D" ${operationDetails.map.charge_type == 'D' ? 'selected' : ''}>Daily</option>

								</select>
							</td>
						</tr>
					</table>
				</fieldset>
			</div>
		 		<fieldset class="fieldSetBorder">
					<legend class="fieldSetLabel">Other Details</legend>
			 		<table class="formtable">
			 			<tr>
							<td class="formlabel" >Prescribed By:</td>
							<td class="yui-skin-sam" valign="top">
								<div style="width: 138px">
									<input type="text" id="prescribing_doctor" name="prescribing_doctor" value="${operationDetails.map.doctor_name}" ${addedToBill ? 'disabled=' : ''}/>
									<div id="prescribing_doctorAcDropdown" style="width: 250px"></div>
								</div>
								<input type="hidden" name="prescribing_doctorId" id="prescribing_doctorId" value="${operationDetails.map.prescribing_doctor}"/>
							</td>
						</tr>
						<tr>
							<fmt:formatDate pattern="dd-MM-yyyy" value="${operationDetails.map.wheel_in_time}" var="wheelInDate"/>
							<fmt:formatDate pattern="HH:mm" value="${operationDetails.map.wheel_in_time}" var="wheelInTime"/>
							<fmt:formatDate pattern="dd-MM-yyyy" value="${operationDetails.map.wheel_out_time}" var="wheelOutDate"/>
							<fmt:formatDate pattern="HH:mm" value="${operationDetails.map.wheel_out_time}" var="wheelOutTime"/>
							<td class="formlabel">Wheel In Time:</td>
							<td>
								<insta:datewidget name="wheel_in_date" id="wheel_in_date" value="${wheelInDate}"/>
								<input type="text" name="wheel_in_time" id="wheel_in_time" value="${wheelInTime}" onchange="getCompleteTime(this)"
									class="timefield" maxlength="5"/>
							</td>
							<td class="formlabel">Out:</td>
							<td>
								<insta:datewidget name="wheel_out_date" id="wheel_out_date" value="${wheelOutDate}"/>
								<input type="text" name="wheel_out_time" id="wheel_out_time" value="${wheelOutTime}"
									class="timefield" onchange="getCompleteTime(this)" maxlength="5"/>
							</td>
							<td colspan="2" width="50px;">&nbsp;</td>
						</tr>
						<tr>
							<fmt:formatDate pattern="dd-MM-yyyy" value="${operationDetails.map.surgery_start}" var="surgeryStartDate"/>
							<fmt:formatDate pattern="HH:mm" value="${operationDetails.map.surgery_start}" var="surgeryStartTime"/>
							<fmt:formatDate pattern="dd-MM-yyyy" value="${operationDetails.map.surgery_end}" var="surgeryEndDate"/>
							<fmt:formatDate pattern="HH:mm" value="${operationDetails.map.surgery_end}" var="surgeryEndTime"/>

							<td class="formlabel">Surgery / Procedure Start:</td>
							<td>
								<c:choose>
									<c:when test="${fixedOtCharges == 'Y' || addedToBill}">
										<insta:datewidget name="surgery_start_date" id="surgery_start_date" value="${surgeryStartDate}" disabled="disabled"/>
									</c:when>
									<c:otherwise>
										<insta:datewidget name="surgery_start_date" id="surgery_start_date" value="${surgeryStartDate}"/>
									</c:otherwise>
								</c:choose>
								<input type="text" name="surgery_start_time" id="surgery_start_time" value="${surgeryStartTime}"
									class="timefield" onchange="getCompleteTime(this)" maxlength="5" ${(fixedOtCharges == 'Y' || addedToBill) ? 'disabled=' : ''}/>
							</td>
							<td class="formlabel">End:</td>
							<td>
								<c:choose>
									<c:when test="${fixedOtCharges == 'Y' || addedToBill}">
										<insta:datewidget name="surgery_end_date" id="surgery_end_date" value="${surgeryEndDate}" disabled="disabled"/>
									</c:when>
									<c:otherwise>
										<insta:datewidget name="surgery_end_date" id="surgery_end_date" value="${surgeryEndDate}"/>
									</c:otherwise>
								</c:choose>
								<input type="text" name="surgery_end_time" id="surgery_end_time" value="${surgeryEndTime}"  class="timefield"
									onchange="getCompleteTime(this)" maxlength="5" ${(fixedOtCharges == 'Y' || addedToBill) ? 'disabled=' : ''}/>
							</td>
							<td colspan="2" width="50px;">&nbsp;</td>
						</tr>
						<tr>
							<td class="formlabel" >Specimen:</td>
							<td colspan="3">
								<textarea name="specimen" id="specimen" cols="80" rows="2" ><c:out value="${operationDetails.map.specimen}"/></textarea>
							</td>
							<td>&nbsp;</td>
						</tr>
						<tr>
							<td class="formlabel" >Conduction Remarks:</td>
							<td colspan="3">
								<textarea name="conduction_remarks" id="conduction_remarks" cols="80" rows="4"><c:out value="${(operationDetails.map.conduction_remarks)}"/></textarea>
							</td>
							<td>&nbsp;</td>
						</tr>
						<tr>
							<td class="formlabel" >Status:</td>
							<td colspan="3">
								<select name="operation_status" id="operation_status"  class="dropdown" onchange="showHideCancelReason(this)">
									<option value="P" ${(operationDetails.map.operation_status == 'N' || operationDetails.map.operation_status == 'P') ? 'selected' : ''}>In Progress</option>
									<option value="C" ${operationDetails.map.operation_status == 'C' ? 'selected' : ''}>Completed</option>
									<option value="X" ${operationDetails.map.operation_status == 'X' ? 'selected' : ''}>Cancelled</option>
								</select>
							</td>
							<td>&nbsp;</td>
						</tr>
						<tr id="cancelReasonRow" style="${operationDetails.map.operation_status != 'X' ? 'display:none' : ''}">
							<td class="formlabel" >Cancel Reason:</td>
							<td colspan="3">
								<textarea name="cancel_reason" id="cancel_reason" cols="80" rows="4"><c:out value="${(operationDetails.map.cancel_reason)}"/></textarea>
							</td>
							<td>&nbsp;</td>
						</tr>
				 	</table>
				 </fieldset>
		 	</div>
		 </div>
	 	<div style="height:10px;">&nbsp;</div>
	 	<div style="margin-top: 10px">
	 		<c:set var="disabled" value="${(operationDetails.map.operation_status == 'C' || operationDetails.map.operation_status == 'X') ? 'disabled=' : ''}"/>
			<button type="submit" name="save" onclick="return saveOperationDetails();" ${disabled}><b><u>S</u></b>ave</button>
			<insta:screenlink screenId="ope_scheduler" extraParam="?method=getScheduleDetails"
				target="_blank" label="Surgery/Procedure Scheduler" addPipe="true"/>

			<c:if test="${(param.operation_details_id != null && param.operation_details_id != '')}">
				<c:if test="${operationDetails.map.operation_status != 'X'}">
					<insta:screenlink screenId="get_ot_management_screen" extraParam="?_method=getOtManagementScreen&prescription_id=${param.prescribed_id}&visit_id=${param.visitId}&operation_details_id=${param.operation_details_id}"
						label="Surgery/Procedure Management" addPipe="true"/>
					<insta:screenlink screenId="operation_detailed_screen"  extraParam="?_method=getAddToBillScreen&operation_details_id=${param.operation_details_id}&visit_id=${param.visitId}"
						 label="Add To Bill" addPipe="true"/>
				</c:if>
			</c:if>
	 	</div>
	 	<div id="addOperationDialog" style="display: none">
	 		<div class="hd" id="operationdialogheader">Add Surgery/Procedure:</div>
			<div class="bd">
				<fieldset class="fieldSetBorder">
					<legend class="fieldSetLabel">Surgery/Procedure</legend>
					<table class="formtable">
						<tr>
							<td class="formlabel">Surgery / Procedure Priority:</td>
							<td>
								<select name="d_oper_priority" id="d_oper_priority" class="dropdown">
									<option value="">-- Select --</option>
									<option value="P">Primary</option>
									<option value="S">Secondary</option>
								</select>
								<span class="star" style="margin-left: -3px;">&nbsp;*</span>
							</td>
						</tr>
						<tr>
							<td class="formlabel">Surgery/Procedure Name:</td>
							<td>

								<div id="d_operation" class="autoComplete" style="padding-bottom: 10px;width:380px">
									<input type="text" id="d_operation_name" name="d_operation_name" value="" style="width: 380px;"/>
									<div id="d_operation_name_container" style="width:460px;"></div>
									<input type="hidden" id="d_operation_id" value=""/>
								</div>
								<span class="star" style="margin-left: 380px;float: left">&nbsp;*</span>
							</td>
						</tr>
						<tr>
							<td class="formlabel">Modifier:</td>
							<td>
								<input type="text" name="d_modifier" id="d_modifier" maxlength="100" value="">
							</td>
						</tr>
					</table>
					<table style="margin-top: 10">
						<tr>
							<td>
								<button type="button" id="d_operation_add_btn" name="Ok" value="Ok">Ok</button>
								<input type="button" id="d_operation_cancel_btn" name="cancel" value="Cancel"/>
							</td>
						</tr>
					</table>
				</fieldset>
			</div>
		</div>
		<div id="addSurgeonDialog" style="display: none">
	 		<div class="hd" id="surgeondialogheader">Add Surgeon/Doctor:</div>
			<div class="bd">
				<fieldset class="fieldSetBorder">
					<legend class="fieldSetLabel">Surgeon/Doctor</legend>
					<table class="formtable">
						<tr>
							<td class="formlabel">Role:</td>
							<td>
								<select name="d_surgeon_speciality" id="d_surgeon_speciality" class="dropdown">
									<option value="">-- Select --</option>
									<option value="SU">Surgeon/Doctor</option>
									<option value="ASU">Asst Surgeon/Doctor</option>
									<option value="COSOPE">Co-op. Surgeon/Doctor</option>
								</select>
								<span class="star" style="margin-left: -3px;">&nbsp;*</span>
							</td>
						</tr>
						<tr>
							<td class="formlabel">Surgeon/Doctor Name:</td>
							<td>
								<div id="d_surgeon" class="autoComplete" style="padding-bottom: 2em;width:143px">
									<input type="text" id="d_surgeon_name" name="d_surgeon_name" value="" style="width: 11.7em;"/>
									<div id="d_surgeon_name_container"></div>
									<input type="hidden" id="d_surgeon_id" value=""/>
								</div>
								<span class="star" style="margin-left: -4px;">&nbsp;*</span>
							</td>
						</tr>
					</table>
					<table style="margin-top: 10">
						<tr>
							<td>
								<button type="button" id="d_surgeon_add_btn" name="Ok" value="Ok">Ok</button>
								<input type="button" id="d_surgeon_cancel_btn" name="cancel" value="Cancel"/>
							</td>
						</tr>
					</table>
				</fieldset>
			</div>
		</div>
		<div id="addAnaesthetistsDialog" style="display: none">
	 		<div class="hd" id="anaesthetistsdialogheader">Add Anaesthetists:</div>
			<div class="bd">
				<fieldset class="fieldSetBorder">
					<legend class="fieldSetLabel">Anaesthetists</legend>
					<table class="formtable">
						<tr>
							<td class="formlabel">Role:</td>
							<td>
								<select name="d_anaesthetists_speciality" id="d_anaesthetists_speciality" class="dropdown">
									<option value="">-- Select --</option>
									<option value="AN">Anaesthetist</option>
									<option value="ASAN">Asst Anaesthetist</option>
								</select>
								<span class="star" style="margin-left: -3px;">&nbsp;*</span>
							</td>
						</tr>
						<tr>
							<td class="formlabel">Anaesthetists Name:</td>
							<td>
								<div id="d_anaesthetists" class="autoComplete" style="padding-bottom: 1em;width:143px">
									<input type="text" id="d_anaesthetists_name" name="d_anaesthetists_name" value="" style="width: 11.7em;"/>
									<div id="d_anaesthetists_name_container"></div>
									<input type="hidden" id="d_anaesthetists_id" value=""/>
								</div>
								<span class="star" style="margin-left: -4px;">&nbsp;*</span>
							</td>
						</tr>
					</table>
					<table style="margin-top: 10">
						<tr>
							<td>
								<button type="button" id="d_anaesthetists_add_btn" name="Ok" value="Ok">Ok</button>
								<input type="button" id="d_anaesthetists_cancel_btn" name="cancel" value="Cancel"/>
							</td>
						</tr>
					</table>
				</fieldset>
			</div>
		</div>
		<div id="addAnaesthesiaTypesDialog" style="display: none">
	 		<div class="hd" id="anaesthesiatypedialogheader">Add Anaesthesia Type:</div>
			<div class="bd">
				<fieldset class="fieldSetBorder">
					<legend class="fieldSetLabel">Anaesthesia Types</legend>
					<table class="formtable">
						<tr>
							<td class="formlabel">Anaesthesia Type:</td>
							<td>
								<insta:selectdb name="d_anaesthesia_type" id="d_anaesthesia_type" table="anesthesia_type_master" valuecol="anesthesia_type_id"
									displaycol="anesthesia_type_name" orderby="anesthesia_type_name" dummyvalue="-- Select --"/>
								<span class="star" style="margin-left: -3px;">&nbsp;*</span>
							</td>
						</tr>
						<tr>
							<td class="formlabel">From:</td>
							<td>
								<insta:datewidget name="d_anes_start_date" id="d_anes_start_date"  value="today"/>
								<input type="text" name="d_anes_start_time" id="d_anes_start_time" class="timefield" maxlength="5" value="${curTimeStr}"/>
								<span class="star" style="margin-left: -4px;">&nbsp;*</span>
							</td>
						</tr>
						<tr>
							<td class="formlabel">End:</td>
							<td>
								<insta:datewidget name="d_anes_end_date" id="d_anes_end_date"  value="today"/>
								<input type="text" name="d_anes_end_time" id="d_anes_end_time" class="timefield" maxlength="5" value="${curTimeStr}"/>
								<span class="star" style="margin-left: -4px;">&nbsp;*</span>
							</td>
						</tr>
					</table>
					<table style="margin-top: 10">
						<tr>
							<td>
								<button type="button" id="d_anaesthesia_type_add_btn" name="Ok" value="Ok">Ok</button>
								<input type="button" id="d_anaesthesia_type_cancel_btn" name="cancel" value="Cancel"/>
							</td>
						</tr>
					</table>
				</fieldset>
			</div>
		</div>
		<div id="addPaediatricianDialog" style="display: none">
	 		<div class="hd" id="paediatriciandialogheader">Add Doctor:</div>
			<div class="bd">
				<fieldset class="fieldSetBorder">
					<legend class="fieldSetLabel">Doctor</legend>
					<table class="formtable">
						<tr>
							<td class="formlabel">Doctor Name:</td>
							<td>
								<div id="d_paediatrician" class="autoComplete" style="padding-bottom: 1em;width:143px">
									<input type="text" id="d_paediatrician_name" name="d_paediatrician_name" value="" style="width: 11.7em;"/>
									<div id="d_paediatrician_name_container"></div>
									<input type="hidden" id="d_paediatrician_id" value=""/>
								</div>
								<span class="star" style="margin-left: -4px;">&nbsp;*</span>
							</td>
						</tr>
					</table>
					<table style="margin-top: 10">
						<tr>
							<td>
								<button type="button" id="d_paediatrician_add_btn" name="Ok" value="Ok">Ok</button>
								<input type="button" id="d_paediatrician_cancel_btn" name="cancel" value="Cancel"/>
							</td>
						</tr>
					</table>
				</fieldset>
			</div>
		</div>
	 </form>
</body>
</html>
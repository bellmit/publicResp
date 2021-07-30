<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
	<form id ="EditTrtForm" name="EditTrtForm" onsubmit="saveMrdObservations();javascript:void(0); return false;" enctype="multipart/form-data" >
		<div id="trtDialog" style="visibility: hidden; display: none; width:760px;">
			<div class="bd">
			    <c:if test="${preferences.modulesActivatedMap.mod_ceed_integration == 'Y' && (roleId == 1 || roleId == 2 ||  actionRightsMap.view_ceed_response_comments == 'A')}" >
                    <fieldset class="fieldSetBorder" id="ceedcommentsfieldset" style="display: block">
                        <legend class="fieldSetLabel">Code Check Comments</legend>
                        <ul class="numbers" id="ceed_response_comments">
                        </ul>
                    </fieldset>
                </c:if> 
				<fieldset class="fieldSetBorder">
					<legend class="fieldSetLabel">Edit Treatment Code</legend>
					<table class="formtable" id="trtCodes">
						<tr>

							<td class="formlabel">Code Type:</td>
							<td class="forminfo">
								<insta:selectdb name="trtCodeType" id="trtCodeType" table="mrd_supported_codes"
									valuecol="code_type" displaycol="code_type" dummyvalue="..Code Type.."
									dummuvalueId="" filtercol="code_category" filtervalue="Treatment"
									onchange="initTrtCodesAutocomp(trtCode, trtDropDown, this.value)"/>

								<insta:selectdb name="drgCodeType" id="drgCodeType" table="mrd_supported_codes"
									valuecol="code_type" displaycol="code_type" dummyvalue="..Code Type.."
									dummuvalueId="" filtercol="code_category" filtervalue="Drug"
									onchange="initTrtCodesAutocomp(trtCode, trtDropDown, this.value)"/>

							</td>
							<td class="formlabel">Code/Desc:</td>
							<td class="forminfo">
								<div id="trtAuto" style="padding-bottom: 20px">
									<input type="text" name="trtCode" id="trtCode" />
									<div id="trtDropDown" class="scrolForContainer"></div>
								</div>
							</td>

							<td class="forminfo" style="white-space: nowrap;" >
								<label id="trtCodeDesc"/> </label>
							</td>

						</tr>
						<tr id="prAuthRowP" style="display: none">

							<td class="formlabel">Primary Prior Auth No:</td>
							<td class="forminfo">
								<input type="text" name="trtPreAuthIdP" />
							</td>
							<td class="formlabel">Primary Prior Auth Mode:</td>
							<td class="forminfo">
								<insta:selectdb  name="trtPreAuthModeIdP" value="" table="prior_auth_modes"
									valuecol="prior_auth_mode_id" displaycol="prior_auth_mode_name" filtered="false"
									dummyvalue="-- Select --"/>
							</td>
						</tr>
						<tr id="prAuthRowS" style="display: none">
							<td class="formlabel">Secondary Prior Auth No:</td>
							<td class="forminfo">
								<input type="text" name="trtPreAuthIdS" />
							</td>
							<td class="formlabel">Secondary Prior Auth Mode:</td>
							<td class="forminfo">
								<insta:selectdb  name="trtPreAuthModeIdS" value="" table="prior_auth_modes"
									valuecol="prior_auth_mode_id" displaycol="prior_auth_mode_name" filtered="false"
									dummyvalue="-- Select --"/>
							</td>
							<td class="forminfo" >
							</td>
						</tr>
					</table>
				</fieldset>
				<script>
				</script>
					<fieldset class="fieldSetBorder">
						 <legend class="fieldSetLabel">View/Edit Observations</legend>
						 <input type="hidden" name="obsIndex" id="obsIndex" value="1"/>
						  <input type="hidden" name="obsChgId" id="obsChgId" value=""/>
				         <table>
				         	<tr>
				         		<td>
					         		<table class="formtable" id="obsValueTable" style="display:none;">
					         			<tr>
					         				<th width="140px">	Observation Type </th>
					         				<th width="76px"> Observation Code </th>
					         				<th width="62px"> Code/Desc </th>
					         				<th width="140px"> Value </th>
					         				<th width="140px"> Value Type </th>
					         				<th> </th>
					         				<th> </th>
					         				<th> </th>
					         			</tr>
					         			<tr id="obs.1">
					         				<!--Observation Row -->
					         				<td width="140px;">
					         					<insta:selectdb name="obsCodeType.1" id="obsCodeType.1" table="mrd_supported_codes"
													valuecol="code_type" displaycol="code_type" dummyvalue="--Select--"
													dummyvalueId="" filtercol="code_category" filtervalue="Observations"
													onchange="initObsCodesAutocomp('obs', 1);showFileUpload('obs',1);"/>
					         				</td>
					         				<td width="76px">
					         					<div id="obsAuto.1" style="width:70px;">
													<input type="text" name="obsCode.1" id="obsCode.1"    style="width:70px;" value=""/>
												<div id="obsDropDown.1" class="scrolForContainer"></div>
												</div>
					         				</td>
					         				<td width="62px" style="text-align:right">
					         					<label id="obsCodeDesc.1"></label>
					         					<input type="hidden" name="obsMasterCodeDesc.1" id="obsMasterCodeDesc.1" value=""/>
												<img class="imgHelpText" name="obsHelpImg.1" id="obsHelpImg.1" title="Code Description" src="${cpath}/images/help.png" onclick="showCodeDescription('obs', 1);"/>
					         				</td>
					         				<td width="140px">
					         					<input type="text" name="obsValue.1" id="obsValue.1" value=""/>
					         				</td>
					         				<td width="140px">
					         					<input type="text" name="obsValueType.1" id="obsValueType.1" value=""/>
					         					<input type="hidden" name="obsValueEditable.1" id="obsValueEditable.1" value="Y"/>
					         					<input type="hidden" name="obsSponsorId.1" id="obsSponsorId.1" value=""/>
					         					<input type="hidden" name="obsDocumentId.1" id="obsDocumentId.1" value=""/>
					         				</td>
					         				<td  id="obsDel.1" align="right" width="17px;" style="padding-left: 5px; padding-right: 5px; padding-top: 9px; height: 18px; width: 17px;">
					         					&nbsp;
					         				</td>
					         				<td>
					         				    <input type="file" id="uploadFile1" name="uploadFile" style="display:none; disabled" accept="image/png, image/jpeg, application/pdf">
					         				    <button type="button" name="btnDownloadFile" id="btnDownloadFile1"
													title="Download File" onclick="downloadFile(1);" style="display:none;">
													Download
												</button>
					         				</td>
					         			</tr>
					         		</table>
				         		</td>
				         	</tr>
				         	<tr>
				         		<!--Add more Observations Row -->
				         		<td colspan="7">&nbsp;</td>
								<td>
									<button type="button" name="btnAddItem" id="btnAddItem"
										title="Add Observations" onclick="addObservationElements('obs');" class="imgButton">
										<img src="${cpath}/icons/Add.png"/>
									</button>
								</td>
				         	</tr>
				         </table>
					</fieldset>
				<table>
					<tr>
						<td><button type="button" name="edit" id="edit" onclick="editTrtGrid();">Save</button></td>
						<td><input type="button" name="cancel" value="Cancel" onclick="cancelTrtDialog();"/></td>
						<td><button type="button" name="prev" id="prev" onclick="openPreviousTrtDialog();">Prev</button></td>
						<td><input type="button" name="next" value="Next" onclick="openNextTrtDialog()";/></td>
					</tr>
				</table>
			</div>
		</div>
		<div id="infoDialogs" style="visibility: hidden; display: none; width:300px;">
			 <div class="hd" style= "visibility: hidden; display: none; ">Observation info</div>
			<div class="bd">
				<table width="90%">
					<tbody>
					<tr>
						<td>
							<label id="infoDialogsText" style="float: left"></label>
						</td>
						<td valign="top" style="width: 12px;">
						</td>
					</tr>
					</tbody>
				</table>
			</div>
		</div>
	</form>

	<form name="EncounterForm">
		<div id="encDialog" style="visibility: hidden; display: none;">
			<div class="bd">
				<fieldset class="fieldSetBorder">
					<legend class="fieldSetLabel">Edit Encounter Codes</legend>
						<table class="formtable" id="encounter_codes">
							<tr>
								<td class="forminfo">Encounter&nbsp;Type:</td>
								<td class="formlabel" style="width: 60px;text-align: left;">Code/Desc:</td>
								<td class="forminfo">
									<div id="encounterAuto" style="padding-bottom: 20px">
										<input type="text" name="encCode" id="encCode" />
										<div id="encDropDown" class="scrolForContainer"></div>
									</div>
								</td>
								<td class="forminfo"  style="white-space: nowrap; width: 250px">
									<label  id="encTypeCodeDesc"></label>
									<input type="hidden" name="_encTypeCodeDesc" />
								</td>

							</tr>
							<tr>
								<td class="forminfo">Encounter&nbsp;Start:</td>
								<td class="formlabel" style="width: 60px;text-align: left;">Code/Desc:</td>
								<td class="forminfo" >
									<div id="encStartAuto" style="padding-bottom: 20px">
									<input type="text" name="encStartCode" id="encStartCode" />
									<div id="encStartDropDown" class="scrolForContainer"></div>
								</div>
								</td>
								<td class="forminfo"  style="white-space: nowrap;">
									<label id="encStartCodeDesc" style="width: 400px" > </label>
									<input type="hidden" name="_encStartCodeDesc" />
								</td>
								<td class="formlabel" style="width: 130px;">Transfer From:</td>
								<td>
									<div id="transferHospitals_wrapper" style="padding-bottom: 20px">
										<input type="text" name="encStartSource" id="encStartSource">
									<div id="encStartSource_dropdown" style="width: 35em;display: inline;" class="scrolForContainer"></div>
									<input type="hidden" name="_encStartSource" />
									</div>
								</td>
                                <td class="formlabel" style="text-align: right;">Date&Time:</td>
                                <td style="width: 250px;">
                                    <insta:datewidget name="encStartDate" id="encStartDate" valid="past" value="${encounter_start_date}" btnPos="right" editValue="true"/>
                                    <input type="text" name="encStartTime" id="encStartTime" value="${encounter_start_time}" class="timefield" readonly/>
                                </td>
							</tr>
							<tr>
								<td class="forminfo">Encounter&nbsp;End:</td>
								<td class="formlabel" style="width: 60px;text-align: left;">Code/Desc:</td>
								<td class="forminfo">
									<div id="encEndAuto" style="padding-bottom: 20px">
									<input type="text" name="encEndCode" id="encEndCode" />
									<div id="encEndDropDown" class="scrolForContainer"></div>
								</div>
								</td>
								<td class="forminfo"  style="white-space: nowrap;">
									<label id="encEndCodeDesc" style="width: 400px"></label>
									<input type="hidden" name="_encEndCodeDesc" />
								</td>
								<td class="formlabel" style="width: 130px;">Transfer To:</td>
								<td>
									<div id="transferHospitals_wrapper" style="padding-bottom: 20px">
										<input type="text" name="encEndDestination" id="encEndDestination">
									<div id="encEndDestination_dropdown" style="width: 35em;display: inline;" class="scrolForContainer"></div>
									<input type="hidden" name="_encEndDestination" />
									</div>
								</td>
                                <td class="formlabel" style="text-align: right;">Date&Time:</td>
                                <c:if test="${patient.visit_type == 'i'}">
                                    <c:choose>
                                        <c:when test="${patientEncCodes.encounter_end_date != null && patientEncCodes.encounter_end_time != null}">
                                            <td style="width: 250px;">
                                                <insta:datewidget name="encEndDate" id="encEndDate" valid="past" value="${patientEncCodes.encounter_end_date}" btnPos="right" extravalidation="validateEncDuration();"  editValue="true"/>
                                                <input type="text" name="encEndTime" id="encEndTime" value="${patientEncCodes.encounter_end_time}" class="timefield" onchange="validateEncDuration()" readonly/>
                                            </td>
                                        </c:when>
                                        <c:otherwise>
                                            <td>
                                                <div title="${patientEncCodes.encounter_end_date} ${patientEncCodes.encounter_end_time}">${patientEncCodes.encounter_end_date}&nbsp;${patientEncCodes.encounter_end_time}</div>
                                            </td>
                                        </c:otherwise>
                                    </c:choose>
                                </c:if>
                                <c:if test="${patient.visit_type == 'o'}">
                                    <td style="width: 250px;">
                                        <insta:datewidget name="encEndDate" id="encEndDate" value="${patientEncCodes.encounter_end_date}" btnPos="right" extravalidation="validateEncDuration();"/>
                                        <input type="text" name="encEndTime" id="encEndTime" value="${patientEncCodes.encounter_end_time}" class="timefield" onchange="validateEncDuration()"/>
                                    </td>
                                </c:if>
							</tr>
						</table>
				</fieldset>
				<table>
					<tr>
						<td><button type="button" name="edit" id="edit" onclick="editEncGrid();">Save</button></td>
						<td><input type="button" name="cancel" value="Cancel" onclick="cancelEncDialog();"/></td>
					</tr>
				</table>
			</div>
		</div>
	</form>

	<form name="AddOrEditForm" id="AddOrEditForm">
		<div id="addoreditDiagnosisCodeDialog" style="visibility: hidden; display: none;">
			<div class="bd">
				<fieldset class="fieldSetBorder">
					<legend class="fieldSetLabel">Add / Edit Diagnosis Codes </legend>
					<table class="formtable" id="mrd_codes">
						<tr style="display: none">
							<td class="forminfo">
								<insta:selectdb name="codeType" table="mrd_supported_codes" style="width: 80px"
									valuecol="code_type" displaycol="code_type" 
									filtercol="code_category" filtervalue="Diagnosis" 
									value="${healthAuthorityPrefs.diagnosis_code_type}" onchange="clearRowData(this)"/>
							</td>
							<td class="formlabel" style="width:10px;">Code&nbsp;/&nbsp;Desc:	</td>
							<td class="forminfo" style="width: 100px">
								<div id="icdcodedescDiv" style="padding-bottom: 20px">
									<input type="text" name="code_id_desc" id="code_id_desc" value="" style="width: 100px"/>
									<div id="codeIdDesc" class="diagScrolForContainer" style="width: 600px"></div>
								</div>
								<input type="hidden" name="codeId" />
								<input type="hidden" name="masterCodeIdDesc" id="masterCodeIdDesc"/>
								<input type="hidden" name="isYearOfOnsetMandatory" id="isYearOfOnsetMandatory"/>
								<input type="hidden" name="desc" id="desc"/>
								<input type="hidden" name="sentForApproval" id="sentForApproval"/>
								<input type="hidden" name="deleted" id="deleted"/>
								<input type="hidden" name="diag_type_from_db" id="diag_type_from_db" />
								<input type="hidden" name="icd_code_from_db" id="icd_code_from_db"/>
                                <input type="hidden" name="diagcodeortypeedited" id="diagcodeortypeedited"/>
							</td>
							<td class="formlabel" style="padding-left: 110px;text-align:right;">Desc:</td>
							<td style="width: 35px;padding-left:5px;text-align:left;overflow: hidden;" onClick="showDiagnosisToolTip('diagInfo', '', event, this);">
								&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
							</td>
							<td style="padding-left:5px;">
								<a href="javascript:void(0);"  style="" onClick="showDiagnosisToolTip('diagInfo', '', event, this);" >
									<img class="imgHelpText" name="helpImg" title="Diagnosis Description" src="${pageContext.request.contextPath}/images/help.png"/>
								</a>
							</td>
							<c:if test="${patient.visit_type == 'i'}">
								<td class="formlabel" >POA:</td>
								<td style="padding-left:5px"><insta:selectoptions name="diagPOA" id="diagPOA" opvalues="Y,N,U,W,1,OP"
									optexts="Yes,No,Unknown,Clinically undetermined,Unreported/Not used,Outpatient claim" dummyvalue="--Select--" dummyvalueId="" value="" onchange=""/>
								</td>
							</c:if>
							<td class="formlabel" style="padding-left:5px" name="yearOfOnsetLabel">Year&nbsp;of&nbsp;onset:</td>
								<td style="padding-left:5px"><input type="number" name="yearOfOnset" id="yearOfOnset" style="width: 70px" maxlength="4" onchange="numberCheck(this)"/>
							</td>
							<td class="formlabel" style="padding-left:5px">Diag&nbsp;Type:</td>
							<c:if test="${patient.visit_type == 'i'}">
								<td style="padding-left:5px"><insta:selectoptions name="diagType" id="diagType" opvalues="P,S,A"
									optexts="Primary,Secondary,Admitting" dummyvalue="--Select--" dummyvalueId="" value="" onchange="setPrimaryCode(this)"/>
								</td>
							</c:if>
							<c:if test="${patient.visit_type != 'i'}">
								<td style="padding-left:5px"><insta:selectoptions name="diagType" id="diagType" opvalues="P,S,V"
									optexts="Primary,Secondary,Reason For Visit" dummyvalue="--Select--" dummyvalueId="" value="" onchange="setPrimaryCode(this)"/>
								</td>
							</c:if>
							<td>
							
								<button type="button" name="btnDeleteItem" id="btnDeleteItem"
								title="Delete Diagnosis Code" onclick="deleteDiagCode(this.parentNode.parentNode);" class="imgButton">
									<div class ="imgDelete">
										<img style="vertical-align: middle;" src="${pageContext.request.contextPath}/icons/delete.gif">
									</div>
									<div style="display: none;" class="deleteText">
										<img style="vertical-align: middle; transform: scaleX(-1);" src="${pageContext.request.contextPath}/icons/Redo.png">
										<b style="vertical-align: middle;">Undo</b>
									</div>
								</button>
							
								
							</td>
						</tr>
						<tr>
							<td colspan="12" style="padding-left: 70px;"></td>
							<td><button type="button" name="btnAddItem" id="btnAddItem"
								title="Add Diagnosis Code" onclick="addOneMoreCode();" class="imgButton">
									<img src="${pageContext.request.contextPath}/icons/Add.png" style="float: left;">
								</button>
							</td>
						</tr>

					</table>
				</fieldset>
				<table>
					<tr>
						<td>
							<input type="button" name="add" value="Ok" id="add" onclick="addDiagCodesToGrid();"/>
						</td>

						<td>
							<input type="button" name="cancel" value=Cancel onclick="cancelDiagDialog();"/>
						</td>
					</tr>
				</table>
			</div>
		</div>


		<div id="diagInfo" class="toolTip" style="z-index:1000;width:200px;vertical-align:top;padding: 0px 0px 3px;background: none repeat scroll 0 0 #FFFFFF;" ondblclick="hidetoolTip('diagInfo');">
			<div id="tt_close"   style="vertical-align:top;text-align:right;">
				<img src="${cpath}/icons/dialog_close.png" onclick="hidetoolTip('diagInfo')" alt="close" style="padding-left:159px;"/>
			</div>
			<p style="text-align:left;padding-left:4px;padding-right:4px;">
				<b><i><label id="diagInfoHeaderLabel">&nbsp;</label></i></b>&nbsp;&nbsp;
		   		<label id="diagInfoLabel">&nbsp;</label>
		   		<br/>
			</p>
		</div>

		<div id="diagnosisHistoryDiv" style="display: none">
		<div class="bd">
			<fieldset class="fieldSetBorder">
				<legend class="fieldSetLabel">Diagnosis History</legend>
				<table class="formtable" id="diagnosisHistoryTable">
					<tr style="display: none;">
						<td style="border-bottom : 1px solid; font-weight: bold; font-color: dark"></td>
						<td style="border-bottom : 1px solid; font-weight: bold; font-color: dark" ></td>
						<td style="border-bottom : 1px solid; font-weight: bold; font-color: dark"></td>
						<td style="border-bottom : 1px solid; font-weight: bold; font-color: dark" ></td>
					</tr>
					<tr style="display: none;">
						<td class="formlabel">Code: </td>
						<td class="forminfo"><label></label></td>
						<td class="formlabel" style="white-space: nowrap"><label></label></td>
						<td class="forminfo"><label></label></td>
					</tr>
				</table>
				<table style="margin-top: 10px">
					<tr>
						<td><input type="button" name="dhdCloseBtn" id="dhdCloseBtn" value="Close"/></td>
					</tr>
				</table>
			</fieldset>
		</div>
	</div>
	</form>

	<form name="AddOrEditConsulForm">
		<div id="addoreditConsulCodeDialog" style="visibility: hidden; display: none;">
			<div class="bd">
 				<fieldset class="fieldSetBorder">
					<legend class="fieldSetLabel">Edit Consultation Code </legend>
					<table class="formtable" id="consul_codes">

						<tr style="display: none">
							<td class="formlabel">Desc:</td>
							<td class="forminfo"><input type="text" name="dlg_codeDesc" id="dlg_codeDesc" style="width: 250px" /></td>
							<td class="formlabel">Code&nbsp;/&nbsp;Desc:	</td>
							<td class="forminfo">
								<div id="dlg_itemCodeDiv" style="padding-bottom: 20px">
									<input type="text" name="dlg_item_code" id="dlg_item_code" value="" style="width: 80px"/>
									<div id="dlg_itemCode" class="scrolForContainer"/>
								</div>
								<input type="hidden" id="dlg_consulIndex" name="dlg_consulIndex" value="0" />
								<input type="hidden" name="dlg_consultationType"  id="dlg_consultationType"/>
								<input type="hidden" name="dlg_consultationTypeId"  id="dlg_consultationTypeId"/>
								<input type="hidden" name="dlg_masterCode" id="dlg_masterCode"/>
								<input type="hidden" name="dlg_codeType"  id="dlg_codeType" value=""/>
								<input type="hidden" name="dlg_masterCodeDesc" id="dlg_masterCodeDesc"/>
							</td>
							<td class="formlabel">Consultn.&nbsp;Type:</td>
							<td>
								<div id="dlg_consultType_label_div">
									<label id="dlg_consultType_label"></label>
								</div>
								<div id="dlg_consultType_dropdown">
									<select name="consulType_drop" id="consulType_drop" class="dropdown" onblur="return changeTypeId(this);">
									</select>
								</div>
							</td>
						</tr>
					</table>
				</fieldset>

				<fieldset class="fieldSetBorder">
					<legend class="fieldSetLabel">View/Edit Observations</legend>
					<input type="hidden" name="vitalObsIndex" id="vitalObsIndex" value="1"/>
					<input type="hidden" name="vitalObsChgId" id="vitalObsChgId" value=""/>
			        <table>
			         	<tr>
			         		<td>
				         		<table class="formtable" id="vitalObsValueTable" style="display:none;">
				         			<tr>
				         				<th width="140px"> Observation Type </th>
				         				<th width="76px"> Observation Code </th>
				         				<th width="62px"> Code/Desc	</th>
				         				<th width="140px"> Value </th>
				         				<th width="140px"> Value Type </th>
				         				<th> </th>
				         			</tr>
				         			<tr id="vitalObs.1">
				         				<!--Observation Row -->
				         				<td width="140px;">
				         					<insta:selectdb name="vitalObsCodeType.1" id="vitalObsCodeType.1" table="mrd_supported_codes"
												valuecol="code_type" displaycol="code_type" dummyvalue="--Select--"
												dummyvalueId="" filtercol="code_category" filtervalue="Observations"
												onchange="initObsCodesAutocomp('vitalObs', 1);"/>
				         				</td>
				         				<td width="76px">
				         					<div id="vitalObsAuto.1" style="width:70px;">
												<input type="text" name="vitalObsCode.1" id="vitalObsCode.1" style="width:70px;" value=""/>
											<div id="vitalObsDropDown.1" class="scrolForContainer"></div>
											</div>
				         				</td>
				         				<td width="62px" style="text-align:right">
				         					<label id="vitalObsCodeDesc.1"></label>
				         					<input type="hidden" name="vitalObsMasterCodeDesc.1" id="vitalObsMasterCodeDesc.1" value=""/>
											<img class="imgHelpText" name="vitalObsHelpImg.1" id="vitalObsHelpImg.1" title="Code Description" src="${cpath}/images/help.png" onclick="showCodeDescription('vitalObs', 1);"/>
				         				</td>
				         				<td width="140px">
				         					<input type="text" name="vitalObsValue.1" id="vitalObsValue.1" value=""/>
				         				</td>
				         				<td width="140px">
				         					<input type="text" name="vitalObsValueType.1" id="vitalObsValueType.1" value=""/>
				         					<input type="hidden" name="vitalObsValueEditable.1" id="vitalObsValueEditable.1" value="Y"/>
				         				</td>
				         				<td  id="vitalObsDel.1" align="right" width="17px;" style="padding-left: 5px; padding-right: 5px; padding-top: 9px; height: 18px; width: 17px;">
				         					&nbsp;
				         				</td>
				         			</tr>
				         		</table>
			         		</td>
			         	</tr>
			         	<tr>
			         		<!--Add more Observations Row -->
			         		<td colspan="7">&nbsp;</td>
							<td>
								<button type="button" name="btnAddItem" id="btnAddItem"
									title="Add Observations" onclick="addObservationElements('vitalObs');" class="imgButton">
									<img src="${cpath}/icons/Add.png"/>
								</button>
							</td>
			         	</tr>
			         </table>
				</fieldset>

				<table>
					<tr>
						<td>
							<button type="button" name="add" id="add" onclick="addConsulCodesToGrid();">Save</button>
						</td>

						<td><input type="button" name="cancel" value="Close" onclick="cancelConsulDialog();"/></td>
					</tr>
				</table>
			</div>
		</div>
	</form>

	<form name="EditLOINCForm" onsubmit="javascript:void(0); return false;">
		<div id="loincDialog" style="visibility: hidden; display: none;">
			<div class="bd">
				<fieldset class="fieldSetBorder">
					<legend class="fieldSetLabel">Edit LOINC Code</legend>
					<table class="formtable" id="trtCodes">
						<tr>

							<td class="formlabel">Code Type:</td>
							<td class="forminfo">
								<insta:selectdb name="loincCodeType" id="loincCodeType" table="mrd_supported_codes"
									valuecol="code_type" displaycol="code_type" dummyvalue="..Code Type.."
									dummuvalueId="" filtercol="code_category" filtervalue="Observations"
									onchange="initLoincCodesAutocomp(loincCode, loincDropDown, this.value)"/>
							</td>
							<td class="formlabel">Code/Desc:</td>
							<td class="forminfo">
								<div id="loincAuto" style="padding-bottom: 20px">
									<input type="text" name="loincCode" id="loincCode" />
									<div id="loincDropDown" class="scrolForContainer"></div>
								</div>
							</td>

							<td class="forminfo" style="white-space: nowrap;" >
								<label id="loincCodeDesc"/> </label>
							</td>

						</tr>

					</table>
				</fieldset>
				<table>
					<tr>
						<td><button type="button" name="edit" id="edit" onclick="editLoincGrid();">Save</button></td>
						<td><input type="button" name="cancel" value="Cancel" onclick="cancelLoincDialog();"/></td>
						<td><button type="button" name="prev" id="prev" onclick="openPreviousLoincDialog();">Prev</button></td>
						<td><input type="button" name="next" value="Next" onclick="openNextLoincDialog()";/></td>
					</tr>
				</table>
			</div>
		</div>
	</form>

	<form name="DRGCodeForm" onsubmit="javascript:void(0); return false;">
		<div id="drgDialog" style="visibility: hidden; display: none;">
			<div class="bd">
				<fieldset class="fieldSetBorder">
					<legend class="fieldSetLabel">Edit DRG Code</legend>
						<table class="formtable" id="drg_codes">
							<tr>
								<td class="formlabel">DRG Code/Desc:</td>
								<td class="forminfo">
									<div id="drgAuto" style="padding-bottom: 20px">
										<input type="text" name="drgCode" id="drgCode" ${!(drgCode.drg_bill_status == 'A') ? 'disabled' : ''}/>
										<div id="drgDropDown" class="scrolForContainer"></div>
									</div>
								</td>
								<td class="forminfo"  style="white-space: nowrap; width: 250px">
									<label id="drgCodeDesc"></label>
									<input type="hidden" name="_drgCodeDesc" />
								</td>
							</tr>
						</table>
				</fieldset>
				<fieldset class="fieldSetBorder">
					 <legend class="fieldSetLabel">View Observations</legend>
					 <input type="hidden" name="drgObsIndex" id="drgObsIndex" value="1"/>
					 <input type="hidden" name="drgObsChgId" id="drgObsChgId" value=""/>
			         <table>
			         	<tr>
			         		<td>
				         		<table class="formtable" id="drgObsValueTable" style="display:none;">
				         			<tr>
				         				<th width="140px"> Code Type </th>
				         				<th width="76px"> Code </th>
				         				<th width="62px"> Code/Desc </th>
				         				<th width="140px"> Value </th>
				         				<th width="140px"> Value Type </th>
				         				<th> </th>
				         			</tr>
				         			<tr id="drgObs.1">
				         				<!--Observation Row -->
				         			 	<td width="140px;">
				         					<insta:selectdb name="drgObsCodeType" id="drgObsCodeType.1" table="mrd_supported_codes" value=""
												valuecol="code_type" displaycol="code_type" dummyvalue="--Select--"
												dummyvalueId="" filtercol="code_category" filtervalue="Observations"
												onchange="initObsCodesAutocomp('drgObs', 1);"/>
				         				</td>
				         				<td width="76px">
				         					<div id="drgObsAuto.1" style="width:70px;">
												<input type="text" name="drgObsCode" id="drgObsCode.1" style="width:70px;"/>
											<div id="drgObsDropDown.1" class="scrolForContainer"></div>
				         				</td>
				         				<td width="62px" style="text-align:right">
				         					<label id="drgObsCodeDesc.1"></label>
				         					<input type="hidden" name="drgObsMasterCodeDesc.1" id="drgObsMasterCodeDesc.1" value=""/>
											<img class="imgHelpText" name="drgObsHelpImg.1" id="drgObsHelpImg.1" title="Code Description" src="${cpath}/images/help.png" onclick="showCodeDescription('drgObs', 1);"/>
				         				</td>
				         				<td width="140px">
				         					<input type="text" name="drgObsValue" id="drgObsValue.1" value=""/>
				         				</td>
				         				<td width="140px">
				         					<input type="text" name="drgObsValueType" id="drgObsValueType.1" value=""/>
				         					<input type="hidden" name="drgObsValueEditable" id="drgObsValueEditable.1" value="Y"/>
				         				</td>
				         				<td id="drgObsDel.1" align="right" width="17px;" style="padding-left: 5px; padding-right: 5px; padding-top: 9px; height: 18px; width: 17px;">
				         					&nbsp;
				         				</td>
				         			</tr>
				         		</table>
			         		</td>
			         	</tr>
			         	<tr style="display:none;">
			         		<!--Add more Observations Row -->
			         		<td colspan="7">&nbsp;</td>
							<td>
								<button type="button" name="btnAddItem" id="btnAddItem"
									title="Add Observations" onclick="addObservationElements('drgObs');" class="imgButton">
									<img src="${cpath}/icons/Add.png"/>
								</button>
							</td>
			         	</tr>
			         </table>
				</fieldset>
				<table>
					<tr>
						<td><button type="button" name="edit" id="edit" onclick="editDrgDialog();">Save</button></td>
						<td><input type="button" name="cancel" value="Cancel" onclick="cancelDrgDialog();"/></td>
					</tr>
				</table>
			</div>
		</div>
	</form>

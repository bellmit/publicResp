<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<html>
<head>
	<title>Patient Vaccination Information</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="script" file="PatientVaccinations/patientVaccinations.js"/>
	<insta:link type="script" file="moment.min.js"/>

	<style>
		.status_I {
			background-color: #dbe7f6;
		}
		tr.symptomsList:nth-child(even) {
		    background-color: #EEEEEE;
		}
		.symptomsListData {
		    padding: 10px !important;
		}
	</style>
	<script>
		var doctors = ${doctors};
 		var mrNo = '${ifn:cleanJavaScript(mr_no)}';
 		var vaccineSymptomSeverityMappingJSON = '${ifn:convertToDeepSerializedJSON(vaccineSymptomSeverityMapping)}';
 		var symptomsListJSON = '${ifn:convertToDeepSerializedJSON(symptomsList)}';
 		var routeOfAdminList = ${routeOfAdminList};

        window.addEventListener('load', function(...event) {
          $.ajaxSetup({
                  complete: function (xhr) {
                      console.log('XHR Date  is ',new Date(xhr.getResponseHeader('Date')));
                      if (xhr.getResponseHeader("Date")) {
                          window.serverTS = new Date(xhr.getResponseHeader("Date"));
                      } else {
                          window.serverTS = new Date();
                      }
                      window.localTS = new Date();
                  }
              });
        });

	</script>
</head>

	<body onload="init();ajaxForPrintUrls();">
		<h1 style="float: left">Patient Vaccination</h1>

		<insta:patientsearch searchType="visit" searchUrl="VaccinationInfo.do" buttonLabel="Find"
			searchMethod="vaccinationsList" fieldName="patient_id" showStatusField="true"/>

	<insta:feedback-panel/>
	<insta:patientgeneraldetails mrno="${mr_no}" showClinicalInfo="true" fieldSetTitle="Vaccinations Administered"/>

	<form action="VaccinationInfo.do" method="POST" name="vaccinationForm">
		<input type="hidden" name="_method" value="saveVaccinations" />
		<input type="hidden" name="mr_no" value="${ifn:cleanHtmlAttribute(mr_no)}" />
		<input type="hidden" name="visit_id" value="${ifn:cleanHtmlAttribute(param.patient_id)}" />
			<!-- starts HERE -->
				<div class="resultList">
				<fieldset class="fieldSetBorder"><legend class="fieldSetLabel">Patient Vaccinations</legend>
					<table class="detailList dialog_displayColumns" cellspacing="0" cellpadding="0" id="dosageTable" border="0" width="100%" style="empty-cells: show;margin-top: 5px">
						<tr>
							<insta:sortablecolumn name="vaccine_name" title="Vaccine"/>
							<th >Dose</th>
							<insta:sortablecolumn name="ordering_age_units" title="Recm Age" tooltip="Recommended Age"/>
							<th >Due Date</th>
							<th >Status</th>
							<th >Reason</th>
							<th title="Date Administered">Date Admn</th>
							<th title="Administered by">Admn by</th>
							<th >Medicine/Manuf/Batch/Exp Date</th>
							<th>Remarks</th>
							<th>Administration</th>
							<th>Adverse Reaction</th>
						</tr>

						<c:forEach items="${dosageList}" var="dosage">
							<c:set var="vaccinationDetails" value="${patientVaccinationList[dosage.vaccine_dose_id][0]}"></c:set>
							<c:if test="${(dosage.dose_status eq 'I' && not empty vaccinationDetails) || dosage.dose_status eq 'A'}">
								<tr >
									<td><label>${dosage.vaccine_name}</label>
										<c:set var="due_date">
											<fmt:formatDate value="${dosage.due_date}" pattern="dd-MM-yyyy"></fmt:formatDate>
										</c:set>
										<c:set var="expiry_date">
											<fmt:formatDate value="${vaccinationDetails.expiry_date}" pattern="dd-MM-yyyy"></fmt:formatDate>
										</c:set>
										<c:set var="vaccination_date">
											<fmt:formatDate value="${vaccinationDetails.vaccination_datetime}" pattern="dd-MM-yyyy"></fmt:formatDate>
										</c:set>
										<c:set var="vaccination_time">
										    <fmt:formatDate  value="${vaccinationDetails.vaccination_datetime}" pattern="HH:mm"></fmt:formatDate>
										</c:set>
										<input type="hidden" name="pat_vacc_id" value="${vaccinationDetails.pat_vacc_id}"/>
										<input type="hidden" name="vaccine_name" value="${dosage.vaccine_name}" />
										<input type="hidden" name="vaccine_id" value="${dosage.vaccine_id}" />
										<input type="hidden" name="patient_id" value="${vaccinationDetails.patient_id}" />
										<input type="hidden" name="dose_num" value="${dosage.dose_num}"/>
										<input type="hidden" name="due_date" value="${due_date}" />
										<input type="hidden" name="vaccine_dose_id" value="${dosage.vaccine_dose_id}"/>
										<input type="hidden" name="vaccination_date" value="${vaccination_date}"/>
										<input type="hidden" name="vaccination_time" value="${vaccination_time}"/>
										<input type="hidden" name="vacc_by" value="${vaccinationDetails.vacc_by}"/>
										<input type="hidden" name="vacc_doctor_id" value="${vaccinationDetails.vacc_doctor_id}"/>
										<input type="hidden" name="vacc_doctor_name" value="${vaccinationDetails.vacc_doctor_name}" />
										<input type="hidden" name="med_name" value="${vaccinationDetails.med_name}"/>
										<input type="hidden" name="medicine_id" value="${vaccinationDetails.medicine_id}"/>
										<input type="hidden" name="isOutsideHospital" value="${empty vaccinationDetails.medicine_id ? 'Y' : 'N' }" />
										<input type="hidden" name="manufacturer" value="${vaccinationDetails.manufacturer}"/>
										<input type="hidden" name="expiry_date" value="${expiry_date}"/>
										<input type="hidden" name="batch" value="${vaccinationDetails.batch}"/>
										<input type="hidden" name="mod_user" value="${vaccinationDetails.mod_user}"/>
										<input type="hidden" name="vaccination_status" value="${vaccinationDetails.vaccination_status}"/>
										<input type="hidden" name="db_vaccination_status" value="${vaccinationDetails.vaccination_status}"/>
										<input type="hidden" name="reason_for_not" value="${vaccinationDetails.reason_for_not}"/>
										<input type="hidden" name="remarks" value="${vaccinationDetails.remarks}"/>
										<input type="hidden" name="vaccine_category_id" value="${vaccinationDetails.vaccine_category_id}"/>
										<input type="hidden" name="vaccine_category_name" value="${vaccinationDetails.vaccine_category_name}"/>
										<input type="hidden" name="route_of_admin" value="${vaccinationDetails.route_of_admin}"/>
										<input type="hidden" name="route_name" value="${vaccinationDetails.route_name}"/>
										<input type="hidden" name="cons_uom_id" value="${vaccinationDetails.cons_uom_id}"/>
										<input type="hidden" name="site_id" value="${vaccinationDetails.site_id}"/>
										<input type="hidden" name="medicine_quantity" value="${vaccinationDetails.medicine_quantity}"/>
										<input type="hidden" name="isEdited" value="false" />
										<input type="hidden" name="isNew" value="${empty vaccinationDetails.pat_vacc_id ? 'Y' : 'N'}" />
										<input type="hidden" name="adverse_reaction_id" value="${vaccinationDetails.adverse_reaction_id}" />
										<input type="hidden" name="adverse_reaction_monitoring_for_id" value="${vaccinationDetails.adverse_reaction_monitoring_for_id}" />
										<input type="hidden" name="adverse_reaction_onset_id" value="${vaccinationDetails.adverse_reaction_onset_id}" />
										<input type="hidden" name="adverse_reaction_corelation_id" value="${vaccinationDetails.adverse_reaction_corelation_id}" />
										<input type="hidden" name="adverse_reaction_actions_id" value="${vaccinationDetails.adverse_reaction_actions_id}" />
										<input type="hidden" name="adverse_reaction_start_date"
										    value="<fmt:formatDate value= "${vaccinationDetails.adverse_start_date}" pattern="dd-MM-yyyy"/>" />
										<input type="hidden" name="adverse_reaction_start_time"
										    value="<fmt:formatDate value= "${vaccinationDetails.adverse_start_date}" pattern="HH:mm"/>" />
										<input type="hidden" name="adverse_reaction_end_date"
										    value="<fmt:formatDate value= "${vaccinationDetails.adverse_end_date}" pattern="dd-MM-yyyy"/>" />
										<input type="hidden" name="adverse_reaction_end_time"
										    value="<fmt:formatDate value= "${vaccinationDetails.adverse_end_date}" pattern="HH:mm"/>" />
										<input type="hidden" name="adverse_remarks" value="${vaccinationDetails.adverse_remarks}" />
										<c:forEach items="${symptomsList}" var="symptom">
                                            <input type="hidden" name="symptom_name_${symptom.id}" value="" />
                                            <input type="hidden" name="occurrences_${symptom.id}" maxlength="30" value="" />
                                            <input type="hidden" name="severity_of_reaction_${symptom.id}" value="" />
                                            <input type="hidden" name="adverse_symptom_severity_id_${symptom.id}" value="" />
                                            <input type="hidden" name="to_be_deleted_${symptom.id}" value"" />
										</c:forEach>
									</td>
									<td>
										<label>${dosage.dose_num}</label>
									</td>
									<td>
										<label>${dosage.recommended_age}&nbsp;${dosage.recommended_age eq 1 ? (dosage.age_units eq 'W' ? 'Week' : (dosage.age_units eq 'M' ? 'Month' : 'Year')) : (dosage.age_units eq 'W' ? 'Weeks' : (dosage.age_units eq 'M' ? 'Months' : 'Years')) }</label>
									</td>
									<td>
										<label>${due_date}</label>
									</td>
									<td>
										<c:set var="adminsteredLbl">
											${vaccinationDetails.vaccination_status eq "A" ? "Administered" : (vaccinationDetails.vaccination_status eq "N" ? "Not to be Administered" : "")}
										</c:set>
										<insta:truncLabel value="${adminsteredLbl}" length="15"/>
									</td>
									<td>
										<insta:truncLabel value="${vaccinationDetails.reason_for_not}" length="10"/>
									</td>
									<td>
										<label>${vaccination_date}</label>
									</td>
									<td>
										<insta:truncLabel value="${vaccinationDetails.vacc_doctor_name}" length="15"/>

									</td>
									<td>
										<c:if test="${not empty vaccinationDetails.med_name}">
											<insta:truncLabel value="${vaccinationDetails.concatenatedlbl}" length="20" />
										</c:if>
									</td>
									<td>
										<insta:truncLabel value="${vaccinationDetails.remarks}" length="20"/>
									</td>

									<td style="text-align: center">
										<a name="dosage_editAnchor" href="javascript:Edit" onclick="return showEditDosageDialog(this);" title="Edit Dosage Details">
											<img src="${cpath}/icons/Edit.png" class="button" />
										</a>
									</td>
									<td style="text-align: center">
                                    	<a name="dosage_editAnchor"
                                    	    href="javascript:Edit"
                                    	    onclick="return showAdverseReactionDialogue(this);"
                                    	    title="Add Adverse Reaction"
                                    	    style ="${vaccinationDetails.vaccination_status eq 'A' ? 'pointer-events:all' : 'pointer-events: none; opacity: 0.5'}" >
                                    	    <img src="${cpath}/icons/Edit.png" class="button" />
                                    	</a>
                                    </td>
								</tr>
							</c:if>
						</c:forEach>
					</table>
				</fieldset>
			</div>

			<div id="editDosageDialog" style="display: none" cellpadding="4">
				<input type="hidden" name="dosage_editRowId" id="dosage_editRowId" value=""/>
				<div class="bd">
					<div id="editDosageDialogFieldsDiv">
						<fieldset class="fieldSetBorder">
							<legend id="dosageModal" class="fieldSetLabel">Add Dose Details</legend>
							<table class="formtable">
								<tr>
									<td class="formlabel" id="vaccine_name_label">Vaccine: </td>
									<td>
										<label id="d_vaccine_label" style="font-weight: bold"></label>
									</td>
                                    <td class="formlabel" id="vaccine_category_label">Vaccine Category:</td>
                                    <td>
                                      <select id="d_vaccine_category" class="dropdown" style="width:240px" >
                                            <option value="">-- Select --</option>
								      </select>
								    </td>
								</tr>
								<tr>
									<td class="formlabel" id="vaccine_due_date_label">Due Date: </td>
									<td>
										<label id="d_dosage_due_date_label" style="font-weight: bold"></label>
									</td>
									<td class="formlabel" id="vaccine_dose_label">Dose: </td>
									<td>
										<label id="d_vaccine_dose_label"></label>
									</td>
								</tr>
								<tr>
									<td class="formlabel">Vaccination Status: </td>
									<td>
										<insta:selectoptions onchange="enableAndDisableStatus(null);" name="d_vaccination_status" id="d_vaccination_status" dummyvalueId="" dummyvalue="---Select---" value="" opvalues="A,N" optexts="Administered,Not to be Administered"/>
									</td>
									<td colspan="3">
                                        <input type="checkbox" class="defaultCheckbox" name="outsidehospital" id="outsidehospital"
                                            value="O" onChange="onChangeAdministeredType(null);">Administered Outside Hospital</br>
									</td>
								</tr>
								<tr>
									<td id="vaccination_reason_tdid1" valign="middle" class="formlabel">Reason: </td>
									<td id="vaccination_reason_tdid2" rowspan="2" colspan="2" valign="top">
										<textarea  name="d_vaccination_reason" id="d_vaccination_reason" ></textarea>
									</td>
								</tr>
								<tr></tr>
                                <tr>
                                    <td class="formlabel">Medicine Name:</td>
                                    <td valign="top">
                                        <div id="medicine_name_wrapper">
                                            <input type="text" name="d_med_name" id="d_med_name"
                                                class="field" style="width: 200px" />
                                            <div id="medicinename_dropdown" style="width: 186px"></div>
                                        </div> <input type="hidden" name="d_medicine_id" id="d_medicine_id" value=""/>
                                    </td>
                                    <td class="formlabel" id="vaccine_route_label">Route:</td>
                                    <td><select id="d_medicine_route" class="dropdown">
                                            <option value="">-- Select --</option>
                                    </select></td>
                                </tr>
                                <tr>
                                    <td class="formlabel">Manufacturer:</td>
                                    <td>
                                      <input type="text" name="d_manufacturer"
                                        id="d_manufacturer" maxlength="200" style="width: 200px;" />
                                    </td>
                                    <td class="formlabel" id="vaccine_site_label">Site:</td>
                                    <td>
                                        <insta:selectdb name="d_site_id" id="d_site_id"
                                            table="iv_infusionsites" displaycol="site_name" valuecol="id"
                                            filtercol="status" filtervalue="A" orderby="site_name"
                                            dummyvalue="-- Select --" dummyvalueId=""
                                            style="width: 138px;" />
                                    </td>
                                </tr>
                                <tr>
                                    <td class="formlabel">Administered Date: </td>
                                    <td><insta:datewidget name="d_adminstered_date" btnPos="left" value=""/>
                                        <input type="text" name="d_administered_time" maxlength="5" id="d_administered_time" style="width: 40px;" />
                                    </td>
                                    <td class="formlabel" id="vaccine_dosageqty_label">Dosage:</td>
                                    <td>
                                        <input type="text" name="d_dosage_qty" id="d_dosage_qty" style="width: 40px;" />
                                        <insta:selectdb name="d_cons_uom_id" id="d_cons_uom_id"
                                                table="consumption_uom_master" displaycol="consumption_uom"
                                                valuecol="cons_uom_id" filtercol="status" filtervalue="A"
                                                orderby="consumption_uom" dummyvalue=" Select "
                                                dummyvalueId="" style="width: 100px;" />
                                    </td>
                                </tr>
                                <tr>
                                    <td class="formlabel">Administered By:</td>
                                    <td>
                                        <div id="dosage_wrapper"
                                            style="padding-bottom: 20px; width: 200px;"">
                                                <input type="text" name="administered_name"
                                                    id="administered_name" style="width: 200px" maxlength="100" />
                                                <div id="administered_dropdown" class="scrolForContainer"></div>
                                        </div>
                                        <input type="hidden" name="d_administered_by_id"
                                            id="d_administered_by_id" value="" /> <input type="hidden"
                                            name="d_administered_by_name" id="d_administered_by_name"
                                            value="" />
                                    </td>
                                </tr>
                                <tr>
                                    <td class="formlabel">Batch:</td>
                                    <td valign="top">
                                        <div id="batch_name_wrapper" style="width: 186px">
                                            <input type="text" name="d_batch_text" id="d_batch_text"
                                                style="width: 186px" maxlength="100" />
                                            <div id="batch_dropdown" style="width: 186px"></div>
                                        </div>
                                </tr>
                                <tr>
                                    <td class="formlabel">Expiry Date: </td>
                                    <td>
                                        <insta:datewidget name="d_expiry_date" id="d_expiry_date" btnPos="left" value=""/>
                                    </td>
                                </tr>
								<tr>
									<td class="formlabel">Remarks: </td>
									<td colspan="2" rowspan="2">
										<textarea  name="d_remarks" id="d_remarks" ></textarea>
									</td>
								</tr>
							</table>
						</fieldset>
					</div>
					<table style="margin-top: 10">
						<tr>
							<td>
								<input type="button" id="editOk" name="editOk" value="Ok"/>
								<input type="button" id="editCancel" name="editCancel" value="Cancel" />
								<input type="button" id="editPrevious" name="editPrevious" value="<<Previous" />
								<input type="button" id="editNext" name="editNext" value="Next>>" />
							</td>
						</tr>
					</table>
				</div>
			</div>
			<div id="adverseReactionDialog" style="display: none">
			    <input type="hidden" name="dosage_adverseReactionId" id="dosage_adverseReactionId" value=""/>
                <div class="bd">
                    <div id="adverseReactionDialogFieldsDiv">
                        <fieldset class="fieldSetBorder">
                            <legend class="fieldSetLabel">Adverse Reaction Monitoring</legend>
                                <table class="formtable">
                                    <tr>
                                        <td>Adverse Reaction Monitoring For: </td>
                                        <td>
                                            <insta:selectdb name="adverse_reaction_monitoring_for"
                                                id="adverse_reaction_monitoring_for"
                                                table="adverse_reaction_monitoring_for" valuecol="id"
                                                displaycol="monitoring_for" filtered="false"
                                                dummyvalue="-- Select --"
                                                style="width:50%" />
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>Onset of Event: </td>
                                        <td>
                                            <insta:selectdb name="adverse_reaction_onset"
                                                id="adverse_reaction_onset"
                                                table="adverse_reaction_onset" valuecol="id"
                                                displaycol="on_set_desc" filtered="false"
                                                dummyvalue="-- Select --"
                                                style="width:50%" />
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>Event Co-relation to the Administration: </td>
                                        <td>
                                            <insta:selectdb name="adverse_reaction_corelation"
                                                id="adverse_reaction_corelation"
                                                table="adverse_reaction_corelation" valuecol="id"
                                                displaycol="corelation_desc" filtered="false"
                                                dummyvalue="-- Select --"
                                                style="width:50%" />
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>Action taken: </td>
                                        <td>
                                            <insta:selectdb name="adverse_reaction_actions"
                                                id="adverse_reaction_actions"
                                                table="adverse_reaction_actions" valuecol="id"
                                                displaycol="actions_desc" filtered="false"
                                                dummyvalue="-- Select --"
                                                style="width:50%" />
                                        </td>
                                    </tr>
                                    <tr>
                                        <td><span>Start Date & Time</span></td>
                                        <td>
                                            <insta:datewidget name="adverse_start_date_calendar" id="adverse_start_date_id" btnPos="left"/>
                                            <input type="text" name="adverse_start_time" id="adverse_start_time_id" maxlength="5" style="width: 40px" />
                                        </td>
                                    </tr>
                                    <tr>
                                        <td><span>End Date & Time</span></td>
                                        <td>
                                            <insta:datewidget name="adverse_end_date_calendar" id="adverse_end_date_id" btnPos="left"/>
                                            <input type="text" name="adverse_end_time" id="adverse_end_time_id" maxlength="5" style="width: 40px" />
                                        </td>
                                    </tr>
                                    <tr>
                                        <td colspan=3>
                                            <p>Select Symptoms and Severity</p>
                                            <table class="severityTable" style="border: 1px solid">
                                                <tbody style="display: block; height: 150px; overflow-y: scroll">
                                                    <tr style="background-color: #A6A6A6; text-align: left">
                                                        <th class="symptomsListData">Symptoms</th>
                                                        <th class="symptomsListData">No. of times it occurred</th>
                                                        <th class="symptomsListData">Type of Reaction</th>
                                                    </tr>
                                                    <c:forEach items="${symptomsList}"  var="symptoms">
                                                        <tr class="symptomsList">
                                                            <input type="hidden" id="adverse_symptom_severity_id_${symptoms.id}" value="" />
                                                            <td class="symptomsListData">
                                                                <input type="checkbox" id="symptom_name_${symptoms.id}">
                                                                    <label>${symptoms.symptom_name}</label>
                                                                </input>
                                                            </td>
                                                            <td class="symptomsListData">
                                                                <input type="number" id="occurrences_${symptoms.id}" maxlength="30" onkeypress="return enterNumOnlyzeroToNine(event)" />
                                                            </td>
                                                            <td class="symptomsListData">
                                                                <insta:selectdb name="severity_of_reaction"
                                                                    id="severity_of_reaction_${symptoms.id}"
                                                                    table="severity_of_reaction" valuecol="id"
                                                                    displaycol="severity_name" filtered="false"
                                                                    dummyvalue="-- Select --"
                                                                    style="width:90%" />
                                                            </td>
                                                        </tr>
                                                    </c:forEach>
                                                </tbody>
                                            </table>
                                        </td>
                                    </tr>
                                    <tr>
                                        <td>Remarks: </td>
                                        <td>
                                            <textarea  name="adverse_remarks" id="adverse_remarks_id" cols="50" rows="5" ></textarea>
                                        </td>
                                    </tr>
                                </table>
                        </fieldset>
                    </div>
                    <table style="margin-top: 10">
                        <tr>
                            <td>
                                <input type="button" id="adverseReactionOk" name="adverseReactionOk" value="Ok"/>
                                <input type="button" id="adverseReactionCancel" name="adverseReactionOk" value="Cancel" />
                            </td>
                        </tr>
                    </table>
            		</div>
            	</div>
			</div>
			<!-- ends HERE -->
			<div class="screenActions">
				<input type="submit" name="save" value="Save" accessKey="S" onclick="return validate();"/>
				<input type="submit" name="save" value="Save & Print" accessKey="P" onclick="return validate();"/>
			</div>
		</form>
	</body>
</html>

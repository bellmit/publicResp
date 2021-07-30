
<%@taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="consultation_edit_across_doctors" value="${clinicalPrefs.op_consultation_edit_across_doctors}"/>

<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<html>

<head>
	<title><insta:ltext key="patient.outpatientlist.list.oplist.instahms"/></title>
	<meta http-equiv="Content-Type"  content="text/html;charset=iso-8859-1">

	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="js" file="hmsvalidation.js" />
	<jsp:include page="/pages/Common/MrnoPrefix.jsp"/>
	<insta:link type="js" file="dashboardsearch.js"/>
	<insta:link type="js" file ="outpatient/opList.js"/>
	<insta:js-bundle prefix="outpatientlist.patientsconsultation"/>
	<style type="text/css">
		table.search td { white-space: nowrap }
		.revisit {background-color: #E0E8E0}
	</style>

	<script>
		var toolbarOptions = getToolbarBundle("js.outpatientlist.patientsconsultation.toolbar");
		var mod_growth_charts = '${preferences.modulesActivatedMap.mod_growth_charts == "Y"}';
		var con_ceed_status_json = ${consultationCeedStatusMapJson != null ? consultationCeedStatusMapJson : 'null'} ;
		var mod_ceed_enabled = ${preferences.modulesActivatedMap.mod_ceed_integration == 'Y'};
		var searchForm ;
		var toolbar = {}
			toolbar.Prescribe = {
				title : toolbarOptions["consult"]["name"],
				imageSrc: "icons/Order.png",
				href: "outpatient/OpPrescribeAction.do?_method=list"
			};
		toolbar.TeleConsult = {
				title : toolbarOptions["teleconsult"]["name"],
				imageSrc: "icons/Order.png",
				target: "_blank",
				href: "teleconsult"
			};
			toolbar.GrowthCharts = {
				title : toolbarOptions["growthcharts"]["name"],
				imageSrc: "icons/Edit.png",
				href: "/GrowthCharts/GrowthChartsAction.do?method=list",
				show : (mod_growth_charts == 'true')
			};
			toolbar.TriageForm = {
				title :toolbarOptions["triage"]["name"],
				imageSrc: "icons/Edit.png",
				href: "TriageForm/TriageFormAction.do?method=show",
				show: ${urlRightsMap.triage_form=='A'}
			};
			toolbar.optometrist = {
				title : toolbarOptions["optometrist"]["name"],
				imageSrc : "icons/Edit.png",
				href : "opthalmology/OpthalmologyTestsList.do?_method=show",
				show : ${urlRightsMap.optometrist_screen == 'A'}
			};
			toolbar.doctorEyeExamp = {
				title : toolbarOptions["doctoreyeexam"]["name"],
				imageSrc : "icons/Edit.png",
				href : "opthalmology/DoctorEyeExam.do?_method=showDoctorEyeExamScreen",
				show : ${urlRightsMap.doctor_eye_exam == 'A'}
			};
			toolbar.InitialAssessment = {
				title : toolbarOptions["initialassessment"]["name"],
				imageSrc : "icons/Edit.png",
				href : "InitialAssessment/InitialAssessmentAction.do?_method=getInitialAssessmentScreen",
				show : ${urlRightsMap.initial_assessment == 'A'}
			};
			toolbar.ChangeConsultingDoctor = {
				title : toolbarOptions["changedoctor"]["name"],
				imageSrc : "icons/Edit.png",
				href : 'outpatient/ChangeConsultingDoctor.do?_method=getScreen',
				show : ${urlRightsMap.change_consult_doctor == 'A'}
			};
			toolbar.VaccinationInfo = {
				title : toolbarOptions["vaccinationinfo"]["name"],
				imageSrc : 'icons/Edit.png',
				href : '/VaccinationInfo.do?_method=vaccinationsList',
				show : ${preferences.modulesActivatedMap.mod_vaccination == "Y"}
			};
			toolbar.GenDocumentList = {
				title : toolbarOptions["documentlist"]["name"],
				imageSrc : 'icons/Edit.png',
				href : '/pages/GenericDocuments/GenericDocumentsAction.do?_method=searchPatientGeneralDocuments',
				show : ${urlRightsMap.generic_documents_list == 'A'}
			};
			toolbar.AddGenDocument = {
				title : toolbarOptions["adddocument"]["name"],
				imageSrc : "icons/Add.png",
				href: 'pages/GenericDocuments/GenericDocumentsAction.do?_method=addPatientDocument&addDocFor=visit',
				description :toolbarOptions["adddocument"]["description"],
				show : ${urlRightsMap.generic_documents_list == 'A'}

			};
			toolbar.GenFormList = {
				title : toolbarOptions["genformlist"]["name"],
				imageSrc : 'icons/Edit.png',
				href : '/GenericForms/GenericFormsAction.do?_method=list',
				description :toolbarOptions["genformlist"]["description"],
				show : ${urlRightsMap.patient_generic_form_list == 'A'}
			};
			toolbar.AddGenForm = {
				title : toolbarOptions["addgenform"]["name"],
				imageSrc : "icons/Add.png",
				href: '/GenericForms/GenericFormsAction.do?_method=getChooseGenericFormScreen',
				description :toolbarOptions["addgenform"]["description"],
				show : ${urlRightsMap.patient_generic_form_list == 'A'}
			};
			toolbar.EMR = {
				title : toolbarOptions["patientemr"]["name"],
				imageSrc : "icons/View.png",
				href: '/emr/EMRDisplay.do?_method=list',
				description :toolbarOptions["patientemr"]["description"]
			};

		function init(){
 			searchForm=document.OpSearchForm;
 			Insta.initMRNoAcSearch('${cpath}', 'mrno', 'mrnoContainer', 'all', null, null);
 			createToolbar(toolbar);
		}

		function initMrNoAutoComplete() {
			Insta.initPatientAcSearch('${cpath}', 'mrno', 'mrnoContainer', 'all', null, null);
		}

		function clearSearch(){
			searchForm.regDate.value = "";
			searchForm.firstName.value = "";
			searchForm.lastName.value = "";
			searchForm.phone.value = "";
			searchForm.departments.options.selectedIndex = -1;
			searchForm.doctors.options.selectedIndex = -1;
			var status = searchForm.status;
			for (var i=0; i<status.length; i++) {
				if (status[i].checked) status[i].checked = false;
				if (status[i].disabled) status[i].disabled = false;
			}
		}

		function validateSearchForm() {
			if (!doValidateDateField(document.getElementById('visited_date1'))) {
				return false;
			}
			if (!doValidateDateField(document.getElementById('visited_date2'))) {
				return false;
			}
			return true;
		}

		function checkClose() {
			var checkBoxes = document.closeForm._closeVisit;
			var anyChecked = false;
			var disabledCount = 0;
			var totalConsultations = 1;
			var checkedceedundone = [];
			if (checkBoxes.length) {
				totalConsultations = checkBoxes.length;
				for (var i=0; i<checkBoxes.length; i++) {
					if (!checkBoxes[i].disabled && checkBoxes[i].checked) {
						anyChecked = true;
						break;
					}
				}

				for (var i=0; i<checkBoxes.length; i++) {
					if (checkBoxes[i].disabled)
						disabledCount++;
				}

			} else {
				var checkBox = document.closeForm._closeVisit;
				if (!checkBox.disabled && checkBox.checked)
					anyChecked = true;
				if (checkBox.disabled)
					disabledCount++;
			}
			if (!anyChecked) {
				if (disabledCount == totalConsultations) {
					showMessage("js.outpatientlist.patientsconsultation.cons.closed");
					return false;
				}
				showMessage("js.outpatientlist.patientsconsultation.checkvisits.close");
				return false;
			}
			var allcheckboxes = document.getElementsByName("_closeVisit");
			if(mod_ceed_enabled && allcheckboxes.length > 0) {
			   var mrnos = document.getElementsByName("_mrno");
			   var patientnames = document.getElementsByName("_patientfullname");
			   var doctornames = document.getElementsByName("_doctorname");
			   for (var i=0; i<allcheckboxes.length; i++) {
			        var consId = allcheckboxes[i].value;
                    if (!allcheckboxes[i].disabled && allcheckboxes[i].checked) {
                        if(con_ceed_status_json[consId].map.status != 'A') {
                            checkedceedundone.push(mrnos[i].value + ", " + patientnames[i].value + ", " + doctornames[i].value);
                        }
                    }
                } 
                if(checkedceedundone.length > 0) {
                    var confirmmessage = getString("js.outpatientlist.patientsconsultation.ceed.not.consultations") + "\n";
                    for(var i=0; i<checkedceedundone.length;i++) {
                        confirmmessage += ((i+1) +". " + checkedceedundone[i] + "\n");
                    }
                    confirmmessage += getString("js.outpatientlist.patientsconsultation.proceed.closing");
                    var retstatus = confirm(confirmmessage);
                    if(retstatus == false) {
                        return false;
                    }
                }
                
			}
		}




	</script>

</head>

<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="patientList" value="${pagedList.dtoList}"/>
<c:set var="hasResults" value="${not empty patientList}"/>

<body onload="init(); showFilterActive(document.consultationForm);">
<c:set var="mrno">
<insta:ltext key="ui.label.mrno"/>
</c:set>
<c:set var="token">
<insta:ltext key="patient.outpatientlist.list.token"/>
</c:set>
<c:set var="visitTime">
<insta:ltext key="patient.outpatientlist.list.visittime"/>
</c:set>
<c:set var="estConsStartTime">
<insta:ltext key="patient.outpatientlist.list.est.cons.start"/>
</c:set>
<c:set var="actConsStartTime">
<insta:ltext key="patient.outpatientlist.list.act.cons.start"/>
</c:set>
<c:set var="appointemntTime">
<insta:ltext key="patient.outpatientlist.list.appointmenttime"/>
</c:set>
<c:set var="crossreferredby">
<insta:ltext key="patient.outpatientlist.list.crossreferredby"/>
</c:set>
<c:set var="consultdoctor">
<insta:ltext key="patient.outpatientlist.list.consult.doctor"/>
</c:set>
<c:set var="consultationtime">
<insta:ltext key="patient.outpatientlist.list.consultationtime"/>
</c:set>
<c:set var="schedulertime">
<insta:ltext key="patient.outpatientlist.list.schedulertime"/>
</c:set>

<c:set var="consstatus">
<insta:ltext key="patient.outpatientlist.list.new"/>,
<insta:ltext key="patient.outpatientlist.list.prescribed"/>,
<insta:ltext key="patient.outpatientlist.list.closed"/>
</c:set>
<c:set var="prioritystatus">
<insta:ltext key="patient.outpatientlist.list.emergent"/>,
<insta:ltext key="patient.outpatientlist.list.urgent"/>,
<insta:ltext key="patient.outpatientlist.list.nonurgent"/>
</c:set>
<c:set var="triagestatus">
<insta:ltext key="patient.outpatientlist.list.completed"/>,
<insta:ltext key="patient.outpatientlist.list.notcompleted"/>
</c:set>
<c:set var="patstatus">
<insta:ltext key="patient.outpatientlist.list.active"/>,
<insta:ltext key="patient.outpatientlist.list.inactive"/>
</c:set>

<c:set var="visitMode">
	<insta:ltext key="patient.resourcescheduler.todayspatientappointments.in.person"/>,
	<insta:ltext key="patient.resourcescheduler.todayspatientappointments.online"/>,
</c:set>
<h1 ><insta:ltext key="patient.outpatientlist.list.patientsforconsultation"/></h1>
<insta:feedback-panel/>
<form action="${cpath}/outpatient/OpListAction.do" method="GET" name="consultationForm">
	<input type="hidden" name="_method" value="list">
	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>
	<input type="hidden" name="_searchMethod" value="list"/>

	<insta:search form="consultationForm" optionsId="optionalFilter" closed="${hasResults}" validateFunction="validateSearchForm()">
		<div class="searchBasicOpts" >
			<div class="sboField">
				<div class="sboFieldLabel"><insta:ltext key="patient.outpatientlist.list.mrno.or.patientname"/>:</div>
				<div class="sboFieldInput">
					<div id="mrnoAutoComplete" style="padding-bottom: 20px">
						<input type="text" name="mr_no" id="mrno" value="${ifn:cleanHtmlAttribute(param['mr_no'])}" />
						<input type="hidden" name="dc.mr_no@op" value="ilike" />
						<div id="mrnoContainer"></div>
					</div>
				</div>
			</div>
		</div>
		<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'};" >
	    	<table class="searchFormTable">
				<tr>
					<td >
						<div class="sfLabel"><insta:ltext key="patient.outpatientlist.list.scheduleddate"/>:</div>
						<div class="sfField">
							<div class="sfFieldSub"><insta:ltext key="patient.outpatientlist.list.from"/>:</div>
							<insta:datewidget name="visited_date" id="visited_date1" value="${paramValues['visited_date'][0]}"/>
							<input type="hidden" name="visited_date@op" value="ge,le"/>
						</div>
						<div class="sfField" style="display: inline">
							<div class="sfFieldSub"><insta:ltext key="patient.outpatientlist.list.to"/>:</div>
							<insta:datewidget name="visited_date" id="visited_date2" value="${paramValues['visited_date'][1]}"/>
							<input type="hidden" name="visited_date@cast"  value="y"/>
						</div>
						<div class="sfLabel"><insta:ltext key="patient.outpatientlist.list.priority"/>:</div>
						<div class="sfField">
							 <insta:checkgroup name="emergency_category" opvalues="E,U,N" optexts="${prioritystatus}" selValues="${paramValues['emergency_category']}"/>
						</div>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="patient.outpatientlist.list.consultationstatus"/>:</div>
						<div class="sfField">
							 <insta:checkgroup name="status" opvalues="A,P,C" optexts="${consstatus}" selValues="${paramValues['status']}"/>
						</div>
						<div class="sfLabel"><insta:ltext key="patient.outpatientlist.list.triage"/>:</div>
						<div class="sfField">
							<insta:checkgroup name="triage_done" id="triage_done" opvalues="Y,N" optexts="${triagestatus}" selValues="${paramValues['triage_done']}"/>
						</div>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="patient.outpatientlist.list.patientstatus"/>:</div>
						<div class="sfField">
							 <insta:checkgroup name="visit_status" opvalues="A,I" optexts="${patstatus}" selValues="${paramValues['visit_status']}"/>
						</div>
						<div class="sfLabel"><insta:ltext key="ui.label.visit.mode"/></div>
						<div class="sfField">
							<insta:checkgroup name="visit_mode"
								opvalues="I,O"
								optexts="${visitMode}"
								selValues="${paramValues.visit_mode}"/>
						</div>
					</td>
					<td style="display: ${empty doctor_logged_in ? 'table-cell' : 'none'}">
						<div class="sfLabel"><insta:ltext key="patient.outpatientlist.list.department"/>:</div>
						<div class="sfField">
							<insta:selectdb name="dept_id" id="department" table="department" valuecol="dept_id" displaycol="dept_name"
									values="${paramValues['dept_id']}" multiple="true" size="8" class="listbox" optionTitle="true"/>

						</div>
					</td>
					<td class="last" style="display: ${empty doctor_logged_in ? 'table-cell' : 'none'}">
						<div class="sfLabel"><insta:ltext key="patient.outpatientlist.list.doctor"/>:</div>
						<div class="sfField">
							<c:set var="docSelected" value="${fn:join(paramValues.doctor_id, ' ')}"/>
							<select name="doctor_id" id="doctor_id"  multiple="multiple" class="listbox" optionTitle="true"
										style="border-top:1px #999 solid; border-left:1px #999 solid; border-bottom:1px #ccc solid; border-right:1px #ccc solid;">
								<option value="(All)">(All)</option>
								<c:forEach items="${doclist}" var="doctors" >
									<c:set var="selected" value="${fn:contains(docSelected,doctors.map.doctor_id)?'selected':''}"/>
									<option value="${doctors.map.doctor_id}" title="${doctors.map.doctor_name}" ${selected}>${doctors.map.doctor_name}</option>
								</c:forEach>
							</select>
						</div>
					</td>
				</tr>
			</table>
		</div>
	</insta:search>
</form> <%-- end of search form --%>
<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}" />
<c:url var="closeUrl" value="OpListAction.do">
	<c:param name="_method" value="close"/>
	<%-- add all the request parameters except sort params as parameters to the search URL --%>
	<c:forEach var="p" items="${param}">
		<c:forEach items="${paramValues[p.key]}" var="value">	<%-- handle multival params --%>
			<c:param name="${p.key}" value="${value}"/>
		</c:forEach>
	</c:forEach>
</c:url>
<form name="closeForm" action="${closeUrl}" method="POST">
	<div id="resultsDiv" class="resultList">
		<table class="resultList dialog_displayColumns" cellpadding="0" cellspacing="0" align="center" width="100%" id="resultTable">
			<tr>
				<th style="padding-top: 0px;padding-bottom: 0px">
					<input type="checkbox" name="_checkAllForClose" onclick="return checkOrUncheckAll('_closeVisit', this)"/>
				</th>
				<insta:sortablecolumn name="mr_no" title="${mrno}"/>
				<th><insta:ltext key="patient.outpatientlist.list.name"/></th>
				<c:if test="${tokenEnabled == 'true'}">
					<insta:sortablecolumn name="consultation_token" title="${token}"/>
				</c:if>
				<insta:sortablecolumn name="arrival_time" title="${visitTime}" tooltip="${visitTime}"/>
				<insta:sortablecolumn name="visited_date" title="${estConsStartTime}" tooltip="${consultationtime}"/>
				<insta:sortablecolumn name="start_datetime" title="${actConsStartTime}" tooltip="${consultationtime}"/>
				<insta:sortablecolumn name="appointment_time" title="${appointemntTime}" tooltip="${schedulertime}"/>
				<th><insta:ltext key="patient.outpatientlist.list.complaint"/></th>
				<th><insta:ltext key="patient.outpatientlist.list.department"/></th>
				<th><insta:ltext key="patient.outpatientlist.list.nationality"/></th>
				<insta:sortablecolumn name="presc_doctor_name" title="${crossreferredby}"/>
				<insta:sortablecolumn name="doctor_full_name" title="${consultdoctor}"/>
				<th><insta:ltext key="patient.outpatientlist.list.remarks"/></th>
				<c:if test="${urlRightsMap.triage_form == 'A'}">
					<th><insta:ltext key="patient.outpatientlist.list.priority"/></th>
				</c:if>
				<th><insta:ltext key="patient.outpatientlist.list.reports"/></th>
				<c:if test="${urlRightsMap.triage_form == 'A'}">
					<th><insta:ltext key="patient.outpatientlist.list.triagedone"/></th>
				</c:if>
				<th><insta:ltext key="ui.label.visit.mode"/></th>
			</tr>
			<c:forEach var="patientBean" items="${patientList}" varStatus="st">
				<c:set var="patient" value="${patientBean.map}"/>
				<c:choose>
					<c:when test="${patient.bill_type == 'P' and patient.payment_status == 'U'}">
						<c:set var="flagColor" value="red"/>
					</c:when>
					<c:when test="${patient.status == 'P'}">
						<c:set var="flagColor" value="yellow" />
					</c:when>
					<c:when test="${patient.status == 'C'}">
						<c:set var="flagColor" value="grey"/>
					</c:when>
					<c:otherwise>
						<c:set var="flagColor" value="empty"/>
					</c:otherwise>
				</c:choose>
				<c:set var="vital_measurements" value="${urlRightsMap.vital_measurements == 'A'}"/>
				<c:set var="blockUnpaid"
					value="${directBillingPrefs.Doctor.map.block_unpaid == 'Y'
							&& (patient.bill_type == 'P' and patient.payment_status == 'U')}"/> <!-- if blockUnpaid is true then disable the consultation link  -->
				<c:set var="optometrist" value="${!blockUnpaid && (fn:toUpperCase(patient.dept_name) eq 'OPHTHALMOLOGY')}" />
				<c:set var="doctor_eye_exam" value="${optometrist}"/>
				<c:set var="editOrCloseNotAllowed" value="${roleId != 1 && roleId != 2 && (consultation_edit_across_doctors == 'N' && doctor_logged_in != patient.doctor_name)}"/>
				<c:set var="disableConsultLink" value="${blockUnpaid || editOrCloseNotAllowed}"/>
				<c:set var="disableTeleConsultLink" value="${patient.visit_mode eq 'I'  || patient.teleconsult_url == null}"/>
				<c:set var="showvaccinationLink"
					value="${patient.patient_age_in eq 'Y' ? (patient.patient_age le 18) : (patient.patient_age_in eq 'M' || patient.patient_age_in eq 'D')}"/>
				<c:set var="showGrowthChart" value="${patient.patient_age_in eq 'Y' ? (patient.patient_age le 20) : (patient.patient_age_in eq 'M' || patient.patient_age_in eq 'D')}" />
				<c:set var="showemr" value="${(roleId == 1 || roleId == 2 || urlRightsMap.emr_screen_without_mrno_search == 'A')}" />
				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
					onclick="showToolbar(${st.index}, event, 'resultTable',
						{consultation_id: '${patient.consultation_id}',patient_id: '${patient.patient_id}', doctor_id: '${patient.doctor_name}', mr_no: '${patient.mr_no}', teleconsult: '${patient.teleconsult_url}'},
						[!${disableConsultLink}, !${disableTeleConsultLink}, ${showGrowthChart},!${blockUnpaid}, ${optometrist},
							${doctor_eye_exam}, !${blockUnpaid}, true, ${showvaccinationLink},true, true, true, true,${showemr}],'',null,oupPatientSetHrefs);"
					id="toolbarRow${st.index}">
					<td >
						<c:set var="disabled" value=""/>
						<c:set var="checked" value=""/>
						<c:choose>
							<c:when test="${patient.status == 'C'}">
								<c:set var="disabled" value="disabled"/>
								<c:set var="checked" value="checked"/>
							</c:when>
							<c:when test="${editOrCloseNotAllowed}">
								<c:set var="disabled" value="disabled"/>
							</c:when>
						</c:choose>
						<input type="checkbox" name="_closeVisit" value="${patient.consultation_id}" ${disabled} ${checked}/>
						<input type="hidden" name="_mrno" value="${patient.mr_no}">
						<input type="hidden" name="_patientfullname" value="${patient.patient_full_name}">
					    <input type="hidden" name="_doctorname" value="${patient.doctor_full_name}">
					</td>
					<td><img src="${cpath}/images/${flagColor}_flag.gif" /> ${patient.mr_no}</td>
					<c:set var="patientName" value="${patient.patient_full_name}"/>
					<td><insta:truncLabel value="${patientName}" length="25"/></td>
					<c:if test="${tokenEnabled == 'true'}">
						<c:choose>
							<c:when test="${patient.consultation_token eq '0'}"><td></td></c:when>
							<c:otherwise>
								<td>${patient.consultation_token}</td>
							</c:otherwise>
						</c:choose>
					</c:if>
					<td style="text-align: center">
						<fmt:formatDate value="${patient.arrival_time}" pattern="dd-MM-yyyy HH:mm"/>
					</td>
					<td>
						<fmt:formatDate value="${patient.visited_date}" pattern="dd-MM-yyyy HH:mm" var="est_consultation_start"/><!-- consultation date in 24Hr format -->
						<label title="${est_consultation_start}">${fn:split(est_consultation_start, " ")[1]}</label>
					</td>
					<c:choose>
						<c:when test="${!empty patient.start_datetime}">
							<td>
								<fmt:formatDate value="${patient.start_datetime}" pattern="dd-MM-yyyy HH:mm" var="act_consultation_start"/><!-- consultation date in 24Hr format -->
								<label title="${act_consultation_start}">${fn:split(act_consultation_start, " ")[1]}</label>
							</td>
						</c:when>
						<c:otherwise><td></td>
						</c:otherwise>
					</c:choose>
					<td style="text-align: center">
						<c:if test="${patient.appointment_id > 0 }">
							<fmt:formatDate value="${patient.appointment_time}" pattern="dd-MM-yyyy HH:mm" var="appointment_time"/>
							<label title="${appointment_time}">${fn:split(appointment_time, " ")[1]}</label>
						</c:if>
					</td>

					<td><insta:truncLabel value="${patient.complaint}" length="20"/></td>
					<td><insta:truncLabel value="${patient.dept_name}" length="20"/></td>
					<td><insta:truncLabel value="${patient.nationality}" length="40"/></td>
					<td><insta:truncLabel value="${patient.presc_doctor_name}" length="20"/></td>
					<td><insta:truncLabel value="${patient.doctor_full_name}" length="20"/></td>
					<td style="white-space: normal"><insta:truncLabel value="${patient.remarks}" length="10"/></td>
					<c:if test="${urlRightsMap.triage_form == 'A'}">
						<td>${patient.emergency_category}</td>
					</c:if>
					<td>
						<c:set var="lab_rad_reports_count" value="0"/>
						<c:if test="${not empty lab_rad_reports[patient.patient_id]}">
							<c:set var="lab_rad_reports_count" value="${lab_rad_reports[patient.patient_id].map.lab_rad_signed_off_reports}"/>
						</c:if>
						<c:set var="service_reports_count" value="0"/>
						<c:if test="${not empty service_reports[patient.patient_id]}">
							<c:set var="service_reports_count" value="${service_reports[patient.patient_id].map.service_signed_off_reports}"/>
						</c:if>
						${lab_rad_reports_count+service_reports_count}
					</td>
					<c:if test="${urlRightsMap.triage_form == 'A'}">
						<td>${patient.triage_done}</td>
					</c:if>
					<c:choose>
  						<c:when test="${patient.visit_mode == 'O'}">
							<td><insta:ltext key="patient.resourcescheduler.todayspatientappointments.online"/></td>
						</c:when>
						<c:otherwise>
							<td><insta:ltext key="patient.resourcescheduler.todayspatientappointments.in.person"/></td>
						</c:otherwise>
					</c:choose> 
				</tr>
			</c:forEach>
		</table>
		<insta:noresults  hasResults="${hasResults}"/>
	</div>
	<div class="fltL" style="width: 50%; margin-top: 5px; display: ${hasResults?'block':'none'}">
		<button type="submit" name="close" accesskey="C"  class="button"onclick="return checkClose()">
			<b><u><insta:ltext key="patient.outpatientlist.list.c"/></u></b><insta:ltext key="patient.outpatientlist.list.lose"/></button>&nbsp;
	</div>
	<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
		<div class="flag"><img src='${cpath}/images/red_flag.gif'></div>
		<div class="flagText"><insta:ltext key="patient.outpatientlist.list.unpaidbillnowbills"/></div>
		<div class="flag"><img src='${cpath}/images/yellow_flag.gif'></div>
		<div class="flagText"><insta:ltext key="patient.outpatientlist.list.partial"/></div>
		<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
		<div class="flagText"><insta:ltext key="patient.outpatientlist.list.closed"/></div>
	</div>


</form>
</body>
</html>


<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@page import="com.insta.hms.common.Encoder" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>

<head>

<script>
	// required by toolbar, so define it before Dashboard.js is included.
	dietPrescAvailable = '${preferences.modulesActivatedMap.mod_dietary}';
	opOrder = '${urlRightsMap.new_op_order}';
	ipOrder = '${urlRightsMap.new_ip_order}';
	var wardServicesActivated = '${preferences.modulesActivatedMap.mod_wardactivities}';
	var issueRights = '${urlRightsMap.patient_inventory_issue}';
	var dischargeSummary = '${urlRightsMap["discharge_summary"]}';
	var pending_operation_rights = ${urlRightsMap.operations_pending_list == 'A'};
	var patient_indent_rights = ${urlRightsMap.stores_patient_indent_add == 'A'};
	var patient_return_indent_rights = ${urlRightsMap.stores_patient_indent_add_return == 'A'};
	var ip_case_sheet_rights = ${urlRightsMap.patient_summary == 'A'};
	var new_ipemr = ${urlRightsMap.new_ipemr == 'A'};
	var generic_forms_list = '${urlRightsMap["patient_generic_form_list"]}';
	var generic_docs_list = '${urlRightsMap["generic_documents_list"]}';
	var DischargeModuleEnabled ='${preferences.modulesActivatedMap.mod_discharge}';
</script>

<title><insta:ltext key="registration.patient.adt.inpatientlist.instahms"/></title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<meta name="i18nSupport" content="true"/>
<insta:link type="css" file="widgets.css" />
<insta:link type="script" file="widgets.js" />
<insta:link type="script" file="ajax.js" />
<insta:link type="script" file="dashboardColors.js" />
<insta:link type="js" file="dashboardsearch.js"/>
<insta:link type="script" file="ipservices/ipservices.js" />
<insta:link type="script" file="ipservices/DashBoard.js" />
<insta:link type="script" file="hmsvalidation.js" />

</head>
<c:set var="patientList" value="${pagedList.dtoList}" />
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="hasResults" value="${not empty patientList}"/>

<body onload="init();">
<c:set var="mrno">
<insta:ltext key="ui.label.mrno"/>
</c:set>
<c:set var="name">
<insta:ltext key="registration.patient.adt.name"/>
</c:set>
<c:set var="admissiondate">
<insta:ltext key="registration.patient.adt.admissiondate"/>
</c:set>

<c:set var="notdischarged">
	<insta:ltext key="patient.discharge.status.common.notdischarged"/>
</c:set>
<c:set var="dischargeInitiated">
	<insta:ltext key="patient.discharge.status.common.dischargeinitiated"/>
</c:set>
<c:set var="clinicalDischarge" >
	<insta:ltext key="patient.discharge.status.common.clinicaldischarge"/>
</c:set>
<c:set var="financialDischarge">
	<insta:ltext key="patient.discharge.status.common.financialdischarge"/>
</c:set>
<c:set var="physicalDischarge">
	<insta:ltext key="patient.discharge.status.common.physicaldischarge"/>
</c:set>

<jsp:useBean id="dischargeStatusMap" class="java.util.HashMap" scope="request"/>
<c:set target="${dischargeStatusMap}" property="N" value="${notdischarged}"/>
<c:set target="${dischargeStatusMap}" property="I" value="${dischargeInitiated}"/>
<c:set target="${dischargeStatusMap}" property="C" value="${clinicalDischarge}"/>
<c:set target="${dischargeStatusMap}" property="F" value="${financialDischarge}"/>
<c:set target="${dischargeStatusMap}" property="D" value="${physicalDischarge}"/>

<div class="pageHeader"><insta:ltext key="registration.patient.adt.inpatientlist"/></div>
<form action="IpservicesList.do"	method="GET" name="ipdashboardform">

	<input type="hidden" name="_method" value="getIPDashBoard">
	<input type="hidden" name="_searchMethod" value="getIPDashBoard"/>
	<input type="hidden" name="mrno" value="">
	<input type="hidden" name="patid" value="">
	<input type="hidden" name="doctorId" value="">

	<insta:search form="ipdashboardform" optionsId="optionalFilter" closed="${hasResults}">
		<div class="searchBasicOpts" >
			<div class="sboField">
				<div class="sboFieldLabel"><insta:ltext key="registration.patient.adt.mrno.or.patientname"/></div>
				<div class="sboFieldInput">
					<div id="mrnoAutoComplete">
						<input type="text" name="mr_no" id="mrno" value="${ifn:cleanHtmlAttribute(param.mr_no)}" />
						<div id="mrnoContainer"></div>
					</div>
				</div>
			</div>
		</div>

		<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
			<table class="searchFormTable">
				<tr>
					<td>
						<div class="sfLabel"><insta:ltext key="registration.patient.adt.ward"/></div>
						<div class="sfField">
							<select name="ward_no" id="ward_no" class="noClass" multiple="multiple" size="8">
								<c:forEach items="${wardName }" var="ward">
									<c:choose>
										<c:when test="${ifn:arrayFind(paramValues['ward_no'],ward.map.ward_no) ne -1}">
											<c:set var="selAtt" value="selected='true'"/>
										</c:when>
										<c:otherwise>
								               		<c:set var="selAtt" value=""/>
								        	</c:otherwise>
									</c:choose>
									<option value="${ward.map.ward_no }" ${selAtt }>
										${ward.map.ward_name }
									</option>
								</c:forEach>
							</select>
						</div>
					</td>
					<td style="display: ${empty doctor_logged_in ? 'table-cell' : 'none'}">
						<div class="sfLabel"><insta:ltext key="registration.patient.adt.doctor"/></div>
						<div class="sfField">
							<c:set var="docSelected" value="${fn:join(paramValues.doctor_id, ' ')}"/>
							<select name="doctor_id" id="doctor_id"  multiple="multiple" class="listbox" optionTitle="true"
										style="border-top:1px #999 solid; border-left:1px #999 solid; border-bottom:1px #ccc solid; border-right:1px #ccc solid;">
								<option value="(All)">(All)</option>
								<c:forEach items="${doclist}" var="doctors" >
									<c:set var="selected" value="${fn:contains(docSelected,doctors.map.doctor_id)?'selected':''}"/>
									<option value="${doctors.map.doctor_id}" ${selected}>${doctors.map.doctor_name}</option>
								</c:forEach>
							</select>
						</div>
					</td>
					<td>
						<div class="sfLabel"><insta:ltext key="registration.patient.adt.admissiondate"/></div>
						<div class="sfField">
							<div class="sfFieldSub"><insta:ltext key="registration.patient.adt.from"/>:</div>
							<insta:datewidget name="admit_date" valid="past"	id="admit_date0" value="${paramValues.admit_date[0]}" />
						</div>
						<div class="sfField">
							<div class="sfFieldSub"><insta:ltext key="registration.patient.adt.to"/>:</div>
							<insta:datewidget name="admit_date" valid="past"	id="admit_date1" value="${paramValues.admit_date[1]}" />
							<input type="hidden" name="admit_date@op" value="ge,le">
							<input type="hidden" name="admit_date@cast" value="y">
						</div>
					</td>
					<td class="last">
						<div class="sfLabel"><insta:ltext key="patient.discharge.status.common.dischargestatus" />:</div>
						<div class="sfField">
							<insta:checkgroup name="patient_discharge_status" selValues="${paramValues.patient_discharge_status}"
								opvalues="N,I,C,F,D" optexts="${notdischarged},${dischargeInitiated},${clinicalDischarge},${financialDischarge},${physicalDischarge}"/>
						</div>
					</td>
					<td class="last">
						<div class="sfLabel"><insta:ltext key="search.patient.visit.status"/>:</div>
						<div class="sfField">
							<insta:checkgroup name="status" selValues="${paramValues['status']}"
								opvalues="A,I" optexts="Active,Inactive"/>
						</div>
					</td>
				</tr>
			</table>
		</div>
	</insta:search>

	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

	<div class="resultList">
		<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">

			<tr onmouseover="hideToolBar();">
				<th>#</th>
				<insta:sortablecolumn name="mr_no" title="${mrno}"/>
				<insta:sortablecolumn name="patient_name" title="${name}"/>
				<th><insta:ltext key="registration.patient.adt.ward"/></th>
				<th><insta:ltext key="registration.patient.adt.bedno"/></th>
				<insta:sortablecolumn name="admit_date" title="${admissiondate}"/>
				<th><insta:ltext key="registration.patient.adt.admittingdoctor"/></th>
				<th><insta:ltext key="patient.discharge.status.common.dischargestatus"/></th>
			</tr>
	      <c:forEach var="patient" items="${patientList}" varStatus="st">
    	     <c:set var="flagColor">
   	     		<c:choose>
				<c:when test="${patient.credit_bill_exists != null}">
					<c:if test="${patient.credit_bill_exists == 'true'}">
						<c:choose>
							<c:when test="${patient.bill_status_ok == 'true'}">
								<c:if test="${patient.payment_ok == 'true'}"><insta:ltext key="registration.patient.adt.green"/></c:if>
								<c:if test="${patient.payment_ok == 'false'}"><insta:ltext key="registration.patient.adt.empty"/></c:if>
						    </c:when>
						    <c:when test="${patient_bill_status_ok == 'false'}">
								<c:if test="${patient.payment.ok == 'true'}"><insta:ltext key="registration.patient.adt.empty"/></c:if>
								<c:if test="${patient.payment.ok == 'false'}"><insta:ltext key="registration.patient.adt.red"/></c:if>
						    </c:when>
						    <c:otherwise>red</c:otherwise>
						</c:choose>
					</c:if>
					<c:if test="${patient.credit_bill_exists == 'false'}"><insta:ltext key="registration.patient.adt.empty"/></c:if>
				</c:when>
				<c:otherwise><insta:ltext key="registration.patient.adt.empty"/></c:otherwise>
				</c:choose>
			 </c:set>

			 <c:set var="orderEnabled" value="${patient.credit_bill_exists == 'true' && patient.bill_status_ok == 'false'}" />
			 <c:set var="dischargeEnabled" value="${urlRightsMap.pat_discharge ne 'N'}" />
			 <c:set var="dischargeSummaryEnabled" value="${urlRightsMap.new_discharge_summary ne 'N'}" />
			 <c:set var="dischargeMedication" value="${urlRightsMap.discharge_medication ne 'N'}"/>
			 <c:set var="vitalFormsEnabled" value="${urlRightsMap.vital_measurements ne 'N' }" />
			 <c:set var="addcaredoctorEnabled" value="${urlRightsMap.define_care_team ne 'N' && preferences.modulesActivatedMap['mod_wardactivities'] eq 'Y'}" />
			 <c:set var="prescribeDietEnabled" value="${preferences.modulesActivatedMap['mod_dietary'] eq 'Y'}" />
			 <c:set var="params" value="mrno:'${patient.mr_no}', mr_no: '${patient.mr_no}', patientId:'${patient.patient_id}', patient_id:'${patient.patient_id}',visit_id:'${patient.patient_id}',
						patientid:'${patient.patient_id}', visitId:'${patient.patient_id}',
						visit_type:'${patient.visit_type_name}',orgid:'${patient.org_id}',
						gender:'${patient.patient_gender}', dept:'${patient.dept_id}',
						isbaby:'${patient.isbaby}', age:'${patient.patient_age}', vifromDate: 'today'" />
			<c:set var="showemr" value="${preferences.modulesActivatedMap['mod_wardactivities'] eq 'Y' && (roleId == 1 || roleId == 2 || urlRightsMap.emr_screen_without_mrno_search == 'A')}" />

				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
					onclick="setOnClickEvent('${patient.mr_no}', '${patient.patient_id}'), showToolbar(${st.index}, event, 'resultTable', { ${params}},
						[true, true, true, ${prescribeDietEnabled}, true, ${showemr},
						true, true, true, ${orderEnabled}, true, true, ${dischargeEnabled},  ${dischargeMedication}, ${dischargeSummaryEnabled}], null, true );"
					onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}">

					<td>${(pagedList.pageNumber - 1) * pagedList.pageSize + (st.index + 1)}	</td>
					<td><img class="flag" src="${cpath}/images/${flagColor}_flag.gif"/>${patient.mr_no}</td>
					<td><insta:truncLabel value="${patient.patient_name}" length="30"/></td>

					<td>
						<c:if test="${patient.ward_name == ''}">${patient.ward}	</c:if>
						<c:if test="${patient.ward_name != ''}">${patient.ward_name}</c:if></td>
					<td>
						<c:choose>
							<c:when test="${patient.bed_name == 'Allocate Bed'}">
								<c:set var="bedname" value="Not Allocated"></c:set>
							</c:when>
							<c:otherwise>
								<c:set var="bedname" value="${patient.bed_name}"></c:set>
							</c:otherwise>
						</c:choose>
						${bedname}
					</td>
					<td><fmt:formatDate value="${patient.admit_date}" pattern="dd-MM-yyyy HH:mm"/></td>
					<td>${patient.doctor_name}</td>
					<td>${dischargeStatusMap[patient.patient_discharge_status]}</td>
				</tr>
		</c:forEach>
	</table>
	<insta:noresults hasResults="${hasResults}"/>
	</div>
	<div style="margin-top: 10px;float: left">
		<c:if test="${preferences.modulesActivatedMap['mod_wardactivities'] eq 'Y'}">
			<insta:screenlink screenId="ward_activities" label="IP Clinical Management" addPipe="false"
					extraParam="?_method=list" />
		</c:if>
	</div>
	<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
		<div class="flag"><img src='${cpath}/images/empty_flag.gif'></div>
		<div class="flagText"><insta:ltext key="registration.patient.adt.okay"/></div>
		<div class="flag"><img src='${cpath}/images/red_flag.gif'></div>
		<div class="flagText"><insta:ltext key="registration.patient.adt.approvalamtexceeded.or.paymentdue"/></div>
		<div class="flag"><img src='${cpath}/images/green_flag.gif'></div>
		<div class="flagText"><insta:ltext key="registration.patient.adt.okaytodischarge"/></div>
	</div>


</form>
<script>
var wardNames = <%= request.getAttribute("wardsJSON") %>;
var doctorlist = <%= request.getAttribute("doctorlist") %>;
var index = <%= Encoder.cleanJavaScript((String)request.getAttribute("index")) %>;
var patientsawaiting = <%= request.getAttribute("patientsawaiting") %>;
var opencreditbills = <%= request.getAttribute("opencreditbills") %>;
var selectedConsultantIndex = <%= Encoder.cleanJavaScript((String)request.getAttribute("selectedConsultantIndex")) %>;
var dischargeStatuses = <%=request.getAttribute("dischargeStatuses")%>;
var billno = '';
var patientstartdateanddayslist = <%= request.getAttribute("patientstartdateanddayslist") %>;
</script>
</body>
</html>


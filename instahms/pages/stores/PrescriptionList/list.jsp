<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@page import="com.insta.hms.stores.StoresDBTablesUtil"%>
<html>
<head>
	<title><insta:ltext key="salesissues.prescriptionlist.list.title"/></title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<meta name="i18nSupport" content="true"/>
	<insta:js-bundle prefix="sales.issues"/>
	<script>
		var toolbarOptions = getToolbarBundle("js.sales.issues.toolbar");
		var docList = <%= StoresDBTablesUtil.getNamesInJSON(StoresDBTablesUtil.DOCTORS) %>;
		var catArray = '${fn:join(paramValues.med_category_id, ",")}'.split(",");
		var deptId = '${pharmacyStoreId}';
		var gRoleId = '${roleId}';
		var printerId = '${printerId}';
		function checkClose() {
			var checkBoxes = document.closeForm._closePrescription;
			var anyChecked = false;
			if (checkBoxes.length) {
				for (var i=0; i<checkBoxes.length; i++) {
					if (checkBoxes[i].checked) {
						anyChecked = true;
						break;
					}
				}
			} else {
				anyChecked = document.closeForm._closePrescription.checked;
			}
			if (!anyChecked) {
				showMessage("js.sales.issues.prescriptions.close");
				return false;
			}
		}
	</script>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="script" file="widgets.js"/>
	<insta:link type="script" file="hmsvalidation.js"/>
	<insta:link type="script" file="stores/prescriptionlist.js"/>
	<insta:link type="script" file="dashboardsearch.js"/>
</head>
<jsp:useBean id="prescStatus" class="java.util.HashMap">
<c:set target="${prescStatus}" property="O" value="Fully Dispensed"/>
<c:set target="${prescStatus}" property="PA" value="Partially Dispensed"/>
<c:set target="${prescStatus}" property="P" value="Not Dispensed"/>
</jsp:useBean>
<jsp:useBean id="prescType" class="java.util.HashMap">
<c:set target="${prescType}" property="P" value="Prescription"/>
<c:set target="${prescType}" property="DM" value="Discharge Medication"/>
</jsp:useBean>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="prescList" value="${pagedList.dtoList}"/>
<c:set var="hasResults" value="${not empty prescList}"/>
<c:set var="allowIpInactiveVisit" value="${true}"/>
<c:set var="genPrefs" value="<%= GenericPreferencesDAO.getGenericPreferences() %>" />
<c:set var="blockIpVisit" value="${genPrefs.restrictInactiveIpVisit}" />

<body onload="init(); showFilterActive(document.listSearchForm); checkstoreallocation();">
<c:set var="visitstatus">
<insta:ltext key="salesissues.prescriptionlist.list.ip"/>,
<insta:ltext key="salesissues.prescriptionlist.list.op"/>
</c:set>
<c:set var="patstatus">
<insta:ltext key="salesissues.prescriptionlist.list.active"/>,
<insta:ltext key="salesissues.prescriptionlist.list.inactive"/>
</c:set>
<c:set var="status">
<insta:ltext key="salesissues.prescriptionlist.list.notdispensed"/>,
<insta:ltext key="salesissues.prescriptionlist.list.partiallydispensed"/>,
<insta:ltext key="salesissues.prescriptionlist.list.fullydispensed"/>
</c:set>
<c:set var="prescriptiontype">
	<insta:ltext key="salesissues.prescriptionlist.list.presc"/>,
	<insta:ltext key="salesissues.prescriptionlist.list.dischargemedication"/>
</c:set>
<c:set var="mrno">
<insta:ltext key="ui.label.mrno"/>
</c:set>
<c:set var="visitid">
<insta:ltext key="salesissues.prescriptionlist.list.visitid"/>
</c:set>
<c:set var="all">
<insta:ltext key="salesissues.prescriptionlist.list.all"/>
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

<div id="storecheck" style="display: block;" >
<h1><insta:ltext key="salesissues.prescriptionlist.list.prescriptionlist"/></h1>

<insta:feedback-panel/>

<form name="listSearchForm" method="GET">
	<input type="hidden" name="_method"  id = "_method" value="getList">
	<input type="hidden" name="_searchMethod" value="getList"/>

	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>

	<insta:search form="listSearchForm" optionsId="optionalFilter" closed="${hasResults}" >
		<div class="searchBasicOpts" >
		  	<div class="sboField">
				<div class="sboFieldLabel"><insta:ltext key="salesissues.prescriptionlist.list.ward"/></div>
					<div class="sboFieldInput">
						<insta:selectdb name="ward_no" value="${param.ward_no}" dummyvalue ="${all}" table="ward_names"
							valuecol="ward_no" displaycol="ward_name" orderby="ward_name"/>
					</div>
		     </div>
	    </div>
	 	<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
		  	<table class="searchFormTable">
					<tr>
						<td>
							<div class="sfLabel"><insta:ltext key="salesissues.prescriptionlist.list.mrno.or.patientname"/>:</div>
							<div class="sfField">
								<div id="mrnoAutoComplete">
		
									<input type="text" name="mr_no" id="mrno" value="${ifn:cleanHtmlAttribute(param.mr_no)}" />
									<div id="mrnoContainer" style="width: 320px"></div>
								</div>
							</div>
							<div class="sfLabel" style="padding-top: 64px"><insta:ltext key="salesissues.prescriptionlist.list.prescriptiontype"/></div>
							<div class="sfField">
								<insta:checkgroup name="type_of_prescription" selValues="${paramValues.type_of_prescription}"
								opvalues="P,DM" optexts="${prescriptiontype}"/>
							</div>
						</td>
						<td>
							<div class="sfLabel"><insta:ltext key="salesissues.prescriptionlist.list.patienttype"/></div>
							<div class="sfField">
								<insta:checkgroup name="visit_type" selValues="${paramValues.visit_type}"
								opvalues="i,o" optexts="${visitstatus}"/>
						    </div>
						    <div class="sfLabel"><insta:ltext key="patient.discharge.status.common.dischargestatus" />:</div>
							<div class="sfField">
								<insta:checkgroup name="patient_discharge_status" selValues="${paramValues.patient_discharge_status}"
									opvalues="N,I,C,F,D" optexts="${notdischarged},${dischargeInitiated},${clinicalDischarge},${financialDischarge},${physicalDischarge}"/>
							</div>
						</td>
						<td>
							<div class="sfLabel"><insta:ltext key="salesissues.prescriptionlist.list.patientstatus"/></div>
							<div class="sfField">
								<insta:checkgroup name="patstatus" selValues="${paramValues.patstatus}"
								opvalues="A,I" optexts="${patstatus}"/>
							</div>
						</td>
						<td>
							<div class="sfLabel"><insta:ltext key="salesissues.prescriptionlist.list.doctor"/></div>
							<div id="doctorAutoComplete">
								<input type="text" name="doctor"  id="doctor" value="${ifn:cleanHtmlAttribute(param.doctor)}"/>
							    <div id="doc_dropdown" style="width: 300px"></div>
					        </div>
					        <div class="sfLabel" style="margin-top: 20px"><insta:ltext key="salesissues.prescriptionlist.list.consultationdate"/>:</div>
							<div class="sfField">
								<div class="sfFieldSub"><insta:ltext key="salesissues.prescriptionlist.list.from"/>: </div>
								<insta:datewidget name="visited_date" id="visited_date0" value="${paramValues.visited_date[0]}"/>
							</div>
							<div class="sfField">
								<div class="sfFieldSub"><insta:ltext key="salesissues.prescriptionlist.list.to"/>: </div>
								<insta:datewidget name="visited_date" id="visited_date1" value="${paramValues.visited_date[1]}"/>
								<input type="hidden" name="visited_date@op" value="ge,le"/>
								<input type="hidden" name="visited_date@cast" value="y"/>
							</div>

						</td>
						<td class="last">
							<div class="sfLabel"><insta:ltext key="salesissues.prescriptionlist.list.prescriptionstatus"/></div>
							<div class="sfField">
								<insta:checkgroup name="status" selValues="${paramValues.status}"
								opvalues="P,PA,O" optexts="${status}"/>
							</div>
						</td>
					</tr>
			</table>
	   </div>
	</insta:search>
 </form>
	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
	<form method="POST" name="closeForm" action="MedicinePrescList.do">
		<input type="hidden" name="_method" value="closePrescription"/>
		<div class="resultList">
			<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable"">
				<tr onmouseover="hideToolBar();">
					<th style="padding-top: 0px;padding-bottom: 0px">
						<input type="checkbox" name="_checkAllForClose" onclick="return checkOrUncheckAll('_closePrescription', this)"/>
					</th>
					<insta:sortablecolumn name="mr_no" title="${mrno}"/>
					<insta:sortablecolumn name="patient_id" title="${visitid}"/>
					<th><insta:ltext key="ui.label.patient.name"/></th>
					<th><insta:ltext key="salesissues.prescriptionlist.list.ward"/></th>
					<th><insta:ltext key="salesissues.prescriptionlist.list.bed"/></th>
					<th><insta:ltext key="salesissues.prescriptionlist.list.consultingdoctor"/></th>
					<th><insta:ltext key="salesissues.prescriptionlist.list.visiteddate"/></th>
					<th><insta:ltext key="salesissues.prescriptionlist.list.prescriptionstatus"/></th>
					<th><insta:ltext key="salesissues.prescriptionlist.list.prescriptiontype"/></th>
					<th><insta:ltext key="patient.discharge.status.common.dischargestatus"/></th>
				</tr>
				<c:forEach var="presc" items="${prescList}" varStatus="st">
				<c:set var="i" value="${st.index + 1}"/>
				<c:set var="flagColor">
					<c:choose>
						<c:when test="${presc.patstatus eq 'I'}"><insta:ltext key="salesissues.prescriptionlist.list.grey"/></c:when>
						<c:otherwise><insta:ltext key="salesissues.prescriptionlist.list.empty"/></c:otherwise>
					</c:choose>
				</c:set>
				<c:if test="${blockIpVisit =='O' || blockIpVisit =='B'}">
					    <c:set var="allowIpInactiveVisit" value="${not (presc.patstatus == 'I' && presc.visit_type == 'o')}"/>
				</c:if>
				<c:set var="id" value="${presc.visit_type == 'o' ? presc.consultation_id : presc.patient_id}"/>
				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
						onclick="showToolbar(${st.index}, event, 'resultTable',
							{visit_id:'${presc.patient_id }',mr_no:'${presc.mr_no }',patstatus:'${presc.patstatus }',
							consultation_id: '${id}',patient_id:'${presc.patient_id }',printerId:'${printerId}',pbm_presc_id:'${presc.pbm_presc_id}' },
							[${allowIpInactiveVisit}, ${presc.type_of_prescription eq 'P'}, ${presc.type_of_prescription eq 'DM'}]);"
						onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}"	>
						<c:set var="prescriptionID" value="${id}" />
					<td><input type="checkbox" name="_closePrescription" value="${prescriptionID}" /></td>
					<td><img class="flag" src="${cpath}/images/${flagColor}_flag.gif"/>${presc.mr_no }</td>
					<td>${presc.patient_id }</td>
					<td>${presc.patname }</td>
					<td>${presc.ward_name }</td>
				   	<td>${presc.bed_name}</td>
				    <td>${presc.doctor}</td>
				    <td><fmt:formatDate pattern="dd-MM-yyyy HH:mm" value="${presc.visited_date}"/></td>
					<td>${prescStatus[presc.status]}</td>
					<td>${prescType[presc.type_of_prescription]}</td>
					<td>${dischargeStatusMap[presc.patient_discharge_status]}</td>
				</tr>
				</c:forEach>
			</table>
			<c:if test="${param._method == 'getList'}">
				<insta:noresults hasResults="${hasResults}"/>
			</c:if>
	    </div>
	    <div class="fltL" style="width: 50%; margin-top: 5px; display: ${hasResults?'block':'none'}">
			<button type="submit" name="close" accesskey="C"  class="button"onclick="return checkClose()">
				<b><u><insta:ltext key="salesissues.prescriptionlist.list.c"/></u></b><insta:ltext key="salesissues.prescriptionlist.list.lose"/></button>&nbsp;
	</div>
	</form>
	<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
		<div class="flag"><img src='${cpath}/images/empty_flag.gif'></div>
		<div class="flagText"><insta:ltext key="salesissues.prescriptionlist.list.active"/></div>
		<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
		<div class="flagText"><insta:ltext key="salesissues.prescriptionlist.list.inactive"/></div>
	</div>



</body>
</html>
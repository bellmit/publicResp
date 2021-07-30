<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Patient Documents List - Insta HMS</title>
<insta:link type="css" file="widgets.css"/>
<jsp:include page="/pages/Common/MrnoPrefix.jsp"/>
<insta:link type="js" file="widgets.js"/>
<insta:link type="js" file="genericdocuments/patientsdashboard.js"/>
<insta:link type="js" file="dashboardsearch.js"/>

	<style type="text/css">
		table.search td { white-space: nowrap }
	</style>
	<script>
		var cpath = '${cpath}';
		var toolbarArray  = {
			Docs : 		{	title: 'Docs',
							imageSrc: 'icons/View.png',
							href: "pages/GenericDocuments/GenericDocumentsAction.do?_method=searchPatientGeneralDocuments&specialized=N",
							onclick: null,
				  		},
			Docket : 	{	title: 'Docket',
							imageSrc: 'icons/Docket.png',
							href: 'emr/PatientDocket.do?_method=getPatientDocket',
							onclick: null,

				  		}
		};

	</script>


</head>
<c:set var="patientsList" value="${pagedList.dtoList}"/>
<c:set var="results" value="${not empty patientsList}"/>

<body onload="init();showFilterActive(document.searchForm);">
<div class="pageHeader">Patient Dashboard</div>
<span align="center" class="error">${ifn:cleanHtml(msg)}</span>
<form action="GenericDocumentsAction.do" method="GET" name="searchForm" >
	<input type="hidden" name="_method" value="getPatientsDetails"/>
	<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>
	<input type="hidden" name="sortReverse" value="${ifn:cleanHtmlAttribute(param.sortReverse)}"/>
	<input type="hidden" name="_actionId" id="_actionId" value="${empty param._actionId ? _actionId : param._actionId}"/>
	<input type="hidden" name="_searchMethod" value="getPatientsDetails"/>

	<insta:search form="searchForm" optionsId="optionalFilter" closed="${hasResults}" >
		<div class="searchBasicOpts" >
			<div class="sboField">
				<div class="sboFieldLabel">MR No/Patient Name:</div>
				<div class="sboFieldInput">
					<div id="mrnoAutoComplete">
						<input type="text" name="mrno" id="mrno" value="${ifn:cleanHtmlAttribute(param.mr_no)}" />
						<input type="hidden" name="mrno@op" value="ilike" />
						<div id="mrnoContainer"></div>
					</div>
				</div>
			</div>
		</div>

		<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
			<table class="searchFormTable">
				<tr>
					<td>
						<div class="sfLabel">Admission Date</div>
						<div class="sfField" style="white-space: nowrap;">
							<div class="sfFieldSub">From:</div>
							<%-- NOTE: datewidget requires that each date field have a unique id --%>
							<insta:datewidget name="fdate" id="fdate" valid="past" value="${paramValues['pra.reg_date'][0]}"/>
						</div>
						<div class="sfField" style="white-space: nowrap;">
							<div class="sfFieldSub">To:</div>
							<insta:datewidget name="tdate" id="tdate" valid="past" value="${paramValues['pra.reg_date'][1]}"/>
							<input type="hidden" name="reg_date@op" value="ge,le"/>
						</div>
					</td>

					<td>
						<div class="sfLabel">Type</div>
						<div class="sfField">
							<input type="checkbox" name="typeAll" onclick="enablePatientType();" ${not empty param.typeAll?'checked':''}>All<br/>
							<input type="checkbox" name="typeIP" ${not empty param.typeIP?'checked':''} ${not empty param.typeAll?'disabled':''}>IP</br>
							<input type="checkbox" name="typeOP" ${not empty param.typeOP?'checked':''} ${not empty param.typeAll?'disabled':''}>OP</br>
						</div>
						<div class="sfLabel">Status</div>
						<div class="sfField">
							<input type="checkbox" name="statusAll" onclick="enablePatientStatus()" ${not empty param.statusAll?'checked':''}/>All</br>
							<input type="checkbox" name="statusActive" ${not empty param.statusActive?'checked':''} ${not empty param.statusAll?'disabled':''}>Active</br>
							<input type="checkbox" name="statusInactive" ${not empty param.statusInactive?'checked':''} ${not empty param.statusAll?'disabled':''}>Inactive
						</div>
					</td>

					<td>
						<div class="sfLabel">Department</div>
						<div class="sfField">
							<insta:selectdb name="department" id="department" table="department" valuecol="dept_id" displaycol="dept_name"
								values="${paramValues['department']}" multiple="true" size="8" class="listbox" />
						</div>
					</td>

					<td>
						<div class="sfLabel">Doctor</div>
						<div class="sfField">
							<insta:selectdb name="doctor" id="doctor" table="doctors" valuecol="doctor_id" displaycol="doctor_name"
								values="${paramValues['doctor']}" multiple="true"  />
						</div>
					</td>

					<td class="last">&nbsp;</td>
				</tr>
			</table>
		</div>
	</insta:search>
	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
</form>
	<div style="overflow:auto;width: 953;">
		<table class="dashboard" id="resultTable" width="100%">
			<tr onmouseover="hideToolBar('');">
				<insta:sortablecolumn name="mrno" title="MR No"/>
				<th>Patient Name</th>
				<th>Age/Gender</th>
				<th>Mobile No.</th>
				<insta:sortablecolumn name="dept" title="Department"/>
				<insta:sortablecolumn name="doctor" title="Doctor"/>
				<insta:sortablecolumn name="admit_date" title="Admit Date"/>
			</tr>
			<c:forEach items="${patientsList}" var="patient" varStatus="st">
				<c:choose>
					<c:when test="${patient.visit_status == 'A'}">
						<c:set var="flagColor" value="empty"/>
					</c:when>
					<c:when test="${patient.visit_status == 'I'}">
						<c:set var="flagColor" value="grey"/>
					</c:when>
					<c:when test="${empty patient.patient_id}">
						<c:set var="flagColor" value="grey"/>
					</c:when>
				</c:choose>
				<tr class="${st.first?'firstRow':''}" onclick="showToolbar('${st.index}', event, 'resultTable',
							{mr_no: '${patient.mr_no}'}, null)" onmouseover="hideToolBar('${st.index}')" id="toolbarRow${st.index}">
					<td>
						<div style="width: 15px; float: left"><img src="${cpath}/images/${flagColor}_flag.gif"/></div>
						${patient.mr_no}
					</td>
					<td>${patient.salutation} ${patient.patient_name} ${patient.last_name}</td>
					<td>${patient.age} ${patient.agein} / ${patient.patient_gender}</td>
					<td>${patient.patient_phone}</td>
					<td>${patient.visit_status == 'I'?patient.pr_dept_name:patient.dept_name}</td>
					<td>${patient.visit_status == 'I'?patient.pr_doctor_name:patient.doctor_name}</td>
					<fmt:formatDate value="${patient.visit_status == 'I'?patient.pr_reg_date:patient.reg_date}" var="reg_date" pattern="dd-MM-yyyy"/>
					<fmt:formatDate value="${patient.visit_status == 'I'?patient.pr_reg_date:patient.reg_time}" var="reg_time" pattern="HH:mm"/>
					<td>${reg_date} ${reg_time}</td><!-- registration date in 24Hr format -->
				</tr>
			</c:forEach>
			<insta:noresults hasResults="${results}"/>
		</table>
	</div>
	<div class="legend" style="display: ${results?'block':'none'}" >
		<div class="flag"><img src="${cpath}/images/empty_flag.gif"/></div>
		<div class="flagText">Active Patients</div>
		<div class="flag"><img src="${cpath}/images/grey_flag.gif"/></div>
		<div class="flagText">Inactive Patients</div>
		<div class="flag"><img src="${cpath}/images/green_flag.gif"/></div>
		<div class="flagText">Patients without visits</div>
	</div>


</body>
</html>

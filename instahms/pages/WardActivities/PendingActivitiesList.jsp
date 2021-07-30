<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<insta:link type="css" file="widgets.css" />
<insta:link type="js" file="dashboardsearch.js"/>
<insta:link type="js" file="ajax.js"/>
<title>IP Clinical Management-InstaHms</title>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<script>
	var vitals_measurment_rights = ${urlRightsMap.vital_measurements == 'A'};
	var toolbar = {
			PatientActivities : {
									title : "Patient Activities",
									href  : "pages/wardactivities/PatientActivitiesAction.do?_method=list",
									imageSrc : "icons/Order.png",
									description : "View Patient Activities Screen"
			},
			Order : {
						title : "Order",
						href  : "patients/orders",
						imageSrc : "icons/Order.png",
						description : "View Patient Orders Screen"
			}
	};
 function init() {
 		Insta.initMRNoAcSearch('${cpath}', 'mrno', 'mrnoContainer', 'all', null, null);
		createToolbar(toolbar);
	}

</script>
</head>
<body onload="init()">
<c:set var="hasResults" value="${not empty pagedList.dtoList}"/>

<h1>IP Clinical Management</h1>
<insta:feedback-panel/>
<c:set var="patientActivitiesList" value="${pagedList.dtoList}"/>
	<form name="patientPendingActivitiesSearchForm" method="GET">
	<input type="hidden" name="_method" value="list">
	<input type="hidden" name="_searchMethod" value="list">
	<insta:search-lessoptions form="patientPendingActivitiesSearchForm" >
		<div class="searchbasicOpts">
			<div class="sboField">
				<div class="sboFieldLabel"> MR No/Patient Name</div>
				<div class="sboFieldInput">
					<div id="mrnoAutoComplete"  style="padding-bottom: 20px">
						<input type="text" name="pr.mr_no" id="mrno" value="${ifn:cleanHtmlAttribute(param['pr.mr_no'])}">
						<input type="hidden" name="pr.mr_no@op" value="ilike">
						<div id="mrnoContainer"></div>
					</div>
				</div>
			</div>
		</div>
		<div class="serchBasicOpts">
			<div class="sboField">
				<div class="sboFieldLabel"> Ward </div>
				<div class="sboFieldInput">
					<select name="wn.ward_no" id="wn.ward_no" class="dropdown">
						<option value="">(All)</option>
						<c:forEach items="${wards }" var="ward">
							<option value="${ward.map.ward_no }"
								<c:if test="${param['wn.ward_no'] eq ward.map.ward_no}">selected</c:if> >
								${ward.map.ward_name }
							</option>
						</c:forEach>
					</select>
				</div>
			</div>
		</div>
		<div class="sboField">
			<div class="sboFieldLabel">Type</div>
			<div class="sboFieldInput">
				<select id="prescription_type" name="a.prescription_type" class="dropdown" >
						<option value="">(All)</option>
						<option value="M" ${param['a.prescription_type'] == 'M' ? 'selected' : ''}>Medication</option>
						<option value="I" ${param['a.prescription_type'] == 'I' ? 'selected' : ''}>Investigation</option>
						<option value="S" ${param['a.prescription_type'] == 'S' ? 'selected' : ''}>Service</option>
						<option value="D" ${param['a.prescription_type'] == 'D' ? 'selected' : ''}>Consultation</option>
						<option value="O" ${param['a.prescription_type'] == 'O' ? 'selected' : ''}>Others</option>
				</select>
			</div>
		</div>
	</insta:search-lessoptions>
	</form>
	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
	<div class="resultList">
		<table class="resultList" id="resultTable" cellspacing="0" cellpadding="0" style="empty-cells: show">
			<tr>
				<insta:sortablecolumn name="due_date" title="Due Date"/>
				<th>Time</th>
				<th>MR NO</th>
				<th>Visit Id</th>
				<th>Patient Name</th>
				<insta:sortablecolumn name="bed_id" title="Bed"/>
				<th>Doctor</th>
				<insta:sortablecolumn name="prescription_type" title="Type"/>
				<th>Description</th>
			</tr>
			<c:forEach var="pendingActivitiesList" items="${patientActivitiesList}" varStatus="st">
				<c:set var="activity" value="${pendingActivitiesList.map}"/>
				<c:set var="item_name" value=""/>
				<c:set var="prescription_type_name" value=""/>

				<c:choose>
					<c:when test="${activity.prescription_type == 'M'}">
						<c:set var="prescription_type_name" value="Medicine"/>
					</c:when>
					<c:when test="${activity.prescription_type == 'I'}">
						<c:set var="prescription_type_name" value="Inv."/>
					</c:when>
					<c:when test="${activity.prescription_type == 'S'}">
						<c:set var="prescription_type_name" value="Service"/>
					</c:when>
					<c:when test="${activity.prescription_type == 'D'}">
						<c:set var="prescription_type_name" value="Consultation"/>
					</c:when>
					<c:when test="${activity.activity_type == 'G'}">
						<c:set var="prescription_type_name" value="General Activity"/>
					</c:when>
				</c:choose>
				<c:set var="flagColor" value="empty" />
				<c:choose>
					<c:when test="${activity.prescription_type == 'I'}">
						<c:choose>
							<c:when test="${empty activity.order_no}">
								<c:set var="flagColor" value="red" />
							</c:when>
							<c:when test="${not empty activity.order_no && activity.test_conducted == 'N' &&
								activity.sample_status == '0'}">
								<c:set var="flagColor" value="blue" />
							</c:when>
							<c:when test="${not empty activity.order_no && activity.test_conducted == 'N' &&
								activity.sample_status == '1' || activity.sample_status == 'U'}">
								<c:set var="flagColor" value="green" />
							</c:when>
							<c:when test="${not empty activity.order_no && (activity.test_conducted == 'P'
								|| activity.test_conducted == 'C' || activity.test_conducted == 'U')}">
								<c:set var="flagColor" value="empty" />
							</c:when>
						</c:choose>
					</c:when>
					<c:when test="${activity.prescription_type == 'S'}">
						<c:choose>
							<c:when test="${empty activity.order_no}">
								<c:set var="flagColor" value="red" />
							</c:when>
							<c:when test ="${not empty activity.order_no && activity.service_conducted == 'N'}">
								<c:set var="flagColor" value="blue" />
							</c:when>
							<c:otherwise>
								<c:set var="flagColor" value="empty" />
							</c:otherwise>
						</c:choose>
					</c:when>
				</c:choose>
				<tr onclick="showToolbar(${st.index}, event, 'resultTable',
						{visitId: '${activity.patient_id}',patient_id: '${activity.patient_id}',
						 mrno: '${activity.mr_no}', visitType: '${activity.visit_type}'},
						 [${activity.prescription_type != 'M'},true]);"
						onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}">
					<td><fmt:formatDate value="${activity.due_date}" pattern="dd-MM-yyyy"/></td>
					<td><fmt:formatDate value="${activity.due_date}" pattern="HH:mm"/></td>
					<td>${activity.mr_no}</td>
					<td>${activity.patient_id} </td>
					<td>
						<c:set var="fullName" value="${activity.salutation} ${activity.patient_name} ${activity.middle_name} ${activity.last_name}"/>
						<insta:truncLabel value="${fullName}" length="30"/>
					</td>
					<td>${activity.bed_name}</td>
					<td><insta:truncLabel value="${activity.doctor_name}" length="20"/></td>
					<td>${prescription_type_name}</td>
					<td>
						<img src="${cpath}/images/${flagColor}_flag.gif"/>
						<insta:truncLabel value="${activity.item_name}" length="30"/>
					</td>
				</tr>
			</c:forEach>
		</table>
		<div class="legend" >
			<div class="flag"><img src='${cpath}/images/red_flag.gif'></div>
			<div class="flagText">No Order</div>
			<div class="flag"><img src='${cpath}/images/blue_flag.gif'></div>
			<div class="flagText">Not Conducted</div>
			<div class="flag"><img src='${cpath}/images/green_flag.gif'></div>
			<div class="flagText">Sample Collected</div>
		</div>
</body>
</html>
<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@page import="com.insta.hms.common.Encoder" %>
<html>

<head>

<title><insta:ltext key="registration.patient.adt.list.adt.instahms"/></title>
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
<meta name="i18nSupport" content="true"/>
<insta:js-bundle prefix="registration.adt"/>
<script>
	var toolbarOptions = getToolbarBundle("js.registration.adt.toolbar");
	var discharge  ='${urlRightsMap.pat_discharge}';
</script>
<insta:link type="css" file="widgets.css" />
<insta:link type="script" file="widgets.js" />
<insta:link type="script" file="ajax.js" />
<insta:link type="script" file="dashboardColors.js" />
<insta:link type="js" file="dashboardsearch.js"/>
<insta:link type="script" file="ipservices/ipservices.js" />
<insta:link type="script" file="hmsvalidation.js" />
<insta:link type="script" file="ipservices/ADT.js" />
</head>

<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="patientList" value="${pagedList.dtoList}" />
<c:set var="hasResults" value="${not empty patientList}"/>
<body onload="initToolbar();">
<c:set var="mrno">
<insta:ltext key="ui.label.mrno"/>
</c:set>
<c:set var="name">
<insta:ltext key="registration.patient.adt.list.name"/>
</c:set>
<c:set var="admitdate">
<insta:ltext key="registration.patient.adt.list.admissiondate"/>
</c:set>
<div class="pageHeader"><insta:ltext key="registration.patient.adt.list.adt"/></div>
<insta:feedback-panel/>
<form action="adt.do" method="GET" name="ipdashboardform">
	<input type="hidden" name="_method" value="getADTScreen">
	<input type="hidden" name="_searchMethod" value="getADTScreen"/>
	<input type="hidden" name="mrno" value="">
	<input type="hidden" name="patid" value="">
	<input type="hidden" name="doctorId" value="">
	<input type="hidden" name="readyToDischarge" value="">

	<insta:search form="ipdashboardform" optionsId="optionalFilter" closed="${hasResults}">

		<div class="searchBasicOpts" >
			<div class="sboField">
				<div class="sboFieldLabel"><insta:ltext key="registration.patient.adt.list.mrno.or.patientname"/></div>
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
						<div class="sfLabel"><insta:ltext key="registration.patient.adt.list.ward"/></div>
						<div class="sfField">
						<select name="ward_no" id="ward_no" class="noClass" multiple="multiple" size="8">
							<option value="">-- Select --</option>
							<c:forEach items="${wards }" var="ward">
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
					<td>
						<div class="sfLabel"><insta:ltext key="registration.patient.adt.list.doctor"/></div>
						<div class="sfField">
							<insta:selectdb name="doctor_id" table="doctors" valuecol="doctor_id"
										displaycol="doctor_name" size="8" style="width: 11em" multiple="true"
											values="${paramValues.doctor_id}" class="noClass" orderby="doctor_id"/>
						</div>
					</td>
					<td class="last">
						<div class="sfLabel"><insta:ltext key="registration.patient.adt.list.admissiondate"/></div>
						<div class="sfField">
							<div class="sfFieldSub"><insta:ltext key="registration.patient.adt.list.from"/>:</div>
							<insta:datewidget name="admit_date" valid="past"	id="admit_date0" value="${paramValues.admit_date[0]}" />
						</div>
						<div class="sfField">
							<div class="sfFieldSub"><insta:ltext key="registration.patient.adt.list.to"/>:</div>
							<insta:datewidget name="admit_date" valid="past"	id="admit_date1" value="${paramValues.admit_date[1]}" />
							<input type="hidden" name="admit_date@op" value="ge,le">
							<input type="hidden" name="admit_date@cast" value="y">
						</div>
					</td>

				</tr>
			</table>
		</div>
	</insta:search>
	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>

	<c:url var="urlWithoutSort" value="Ipservices.do">
		<c:forEach var="p" items="${param}">
			<c:if test="${p.key != 'sortOrder' && p.key != 'sortReverse'}">
				<c:param name="${p.key}" value="${p.value}" />
			</c:if>
		</c:forEach>
	</c:url>

	<div class="resultList">
		<table class="resultList" cellspacing="0" cellpadding="0" id="resultTable" onmouseover="hideToolBar('');">
			<tr onmouseover="hideToolBar();">
				<th>#</th>
				<insta:sortablecolumn name="mr_no" title="${mrno}" />
				<insta:sortablecolumn name="patient_name" title="${name}" />
				<th><insta:ltext key="registration.patient.adt.list.ward"/></th>
				<th><insta:ltext key="registration.patient.adt.list.bedno"/></th>
				<insta:sortablecolumn name="admit_date" title="${admitdate}" />
				<th><insta:ltext key="registration.patient.adt.list.admittingdoctor"/></th>
				<th><insta:ltext key="registration.patient.adt.list.advancestatus"/></th>
			</tr>

			<c:set var="bedDetailsEnabled" value="${roleId == '1' || roleId == '2' || actionRightsMap['bed_close'] == 'A'}" />

			<c:forEach var="patient" items="${patientList}" varStatus="st">

				<c:set var="flagColor">
					<c:choose>
						<c:when test="${patient.daycare_status == 'Y'}">grey</c:when>
						<c:when test="${patient.parent_id != null && patient.daycare_status != 'Y'}">yellow</c:when>
						<c:otherwise>empty</c:otherwise>
					</c:choose>
				</c:set>

				<c:url var="bedURL" value="Ipservices.do" >
					<c:param name="method" value="getIpBedDetailsScreen"/>
					<c:param name="patientid" value="${patient.patient_id}"/>
					<c:param name="estimated" value="${patient.estimated_days}"/>
					<c:param name="babystatus" value="${patient.isbaby}"/>
				</c:url>

				<c:set var="allocatebedEnable" value="${bedDetailsEnabled && patient.bed_name == 'Allocate Bed'}"/>
				<c:set var="finalizedState" value="${patient.bed_state == 'F'}"/>
				<c:set var="showBystander" value="${!allocatebedEnable && !finalizedState && patient.daycare_status == 'N'}"/>
				<c:if test="${ip_preferences.bystander_availability == 'I'}">
					<c:set var="showBystander" value="${!allocatebedEnable && !finalizedState && patient.is_icu eq 'Y' && patient.daycare_status == 'N'}"/>
				</c:if>

				<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
					onclick="showToolbar(${st.index}, event, 'resultTable',
						{mrno: '${patient.mr_no}', patientid: '${patient.patient_id}', patid: '${patient.patient_id}',
						estimated: '${patient.estimated_days}',babystatus:'${patient.isbaby}',
						doctorId:'${patient.doctor_id}',billStatusOk:'${patient.bill_status_ok}', paymentOk:'${patient.payment_ok}' },
						[${bedDetailsEnabled && !allocatebedEnable }, ${bedDetailsEnabled && (allocatebedEnable || finalizedState) },
						${bedDetailsEnabled && (!allocatebedEnable && !finalizedState)},${bedDetailsEnabled && showBystander},true]);"
					onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}">
					<td>${(pagedList.pageNumber - 1) * pagedList.pageSize + (st.index + 1)}	</td>
					<td><img class="flag" src="${cpath}/images/${flagColor}_flag.gif"/>${patient.mr_no}</td>
					<td <c:if test="${patient.vip_status=='Y'}">class="vipIndicator" title="VIP"</c:if>><insta:truncLabel value="${patient.patient_name}" length="30"/></td>
					<td>
						<c:if test="${patient.ward_name == ''}">${patient.ward}</c:if>
						<c:if test="${patient.ward_name != ''}">${patient.ward_name}</c:if>
					</td>
					<td>${patient.bed_name}</td>
					<td><fmt:formatDate value="${patient.admit_date}" pattern="dd-MM-yyyy HH:mm"/></td>
					<td>${patient.doctor_name}</td>
					<c:if test="${patient.credit_bill_exists == 'true'}">
						<c:choose>
							<c:when test="${patient.bill_status_ok == 'true'}">
									<td><insta:ltext key="registration.patient.adt.list.oktodischarge"/></td>
							</c:when>
							<c:otherwise>
								<c:if test="${patient.payment_ok == 'true'}">
									<td><insta:ltext key="registration.patient.adt.list.okay"/></td>
								</c:if>
								<c:if test="${patient.payment_ok == 'false'}">
									<td align="left"><font color="#FFA07A"><insta:ltext key="registration.patient.adt.list.paymentdue"/></font></td>
								</c:if>
							</c:otherwise>
						</c:choose>
					</c:if>
					<c:if test="${patient.credit_bill_exists == 'false'}">
						<td><insta:ltext key="registration.patient.adt.list.okay"/></td>
					</c:if>
				</tr>
			</c:forEach>
		</table>
		<insta:noresults hasResults="${hasResults}"/>
	</div>
	<div style="margin-top: 10px;float: left">
		<a href="${cpath}/pages/registration/admissionrequest.do?_method=getAdmissionRequestList">
					<insta:ltext key="patient.addeditadmissionrequest.admission.request.list.link"/>
		</a>
	</div>

	<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
		<div class="flag"><img src='${cpath}/images/empty_flag.gif'></div>
		<div class="flagText"><insta:ltext key="registration.patient.adt.list.inpatient"/></div>
		<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
		<div class="flagText"> <insta:ltext key="registration.patient.adt.list.daycareinpatient"/></div>
		<div class="flag"><img src='${cpath}/images/yellow_flag.gif'></div>
		<div class="flagText"><insta:ltext key="registration.patient.adt.list.baby"/></div>
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


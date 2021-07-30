<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib uri="/WEB-INF/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/struts-bean.tld" prefix="bean"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions"  prefix="fn"%>

<c:set var="cpath" value="${pageContext.request.contextPath}"/>

<html>
<head>
	<script type="text/javascript">
	var category = '${ifn:cleanJavaScript(category)}'
	</script>
	<title><insta:ltext key="radiology.arrivals.title"/> <insta:ltext key="radiology.arrivals.instahms"/></title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<insta:link type="css" file="widgets.css" />
	<insta:link type="js" file="diagnostics/unfinishedtests.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>
	<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
	<c:set var="max_centers_inc_default" value='<%=GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default")%>'/>
	<insta:js-bundle prefix="laboratory.radiology.batchconduction"/>
	<insta:js-bundle prefix="laboratory.radiology.pendingtests"/>
	<insta:js-bundle prefix="diagnostics.diagdashboards"/>
	<insta:js-bundle prefix="radiology.arrivals"/>
	<script src="<%=request.getContextPath()%>/gettests.do?${test_timestamp}&${ifn:cleanURL(sesHospitalId)}&module=${ifn:cleanURL(module)}"></script>
	<script>
		var toolbarOptions = getToolbarBundle("js.laboratory.radiology.batchconduction.toolbar");
		var allTestNames = deptWiseTestsjson;
		var outHouses = ${outHouses};
		var inHouses = ${inHouses};
		var cpath = '${cpath}';
		var centerId = ${centerId};
		var max_centers_inc_default = ${max_centers_inc_default};
		var sampleCollectionCenterId = ${sampleCollectionCenterId};

		var cancelUrl = '';
		var patientToolbar = null;

		if (category == "DEP_RAD") {
			baseUrl = 'pages/DiagnosticModule/radiology.do';
		}
		var baseModule = 'Radiology';
		var module = 'Radiology';

		function init() {
			autoCompleteTest();
			autoCompleteOutHouse();
			autoCompleteInHouse();
			initMrNoAutoComplete('${cpath}');
		}

		var form = document.testsForm;

		function doSave(action){
			if (empty(action)) return false;

			var checkBox = document.getElementsByName("completeCheck");

			var count = 0;
			for (var i=0; i<checkBox.length; i++) {
				if (checkBox[i].checked) count++;
			}
			var method = "";
			var msg = "";
			if (action == 'setDoctor') {
				method = "setConductingDoctor";
				msg = "js.radiology.arrivals.setConductingDoctor";
			} else if (action == 'modalityArrived') {
				method = "setModalityArrived";
				msg = "js.radiology.arrivals.modalityArrived";
			} else if (action == 'conductionCompleted') {
				method = "conductionCompleted";
				msg = "js.radiology.arrivals.conductionCompleted";
			}
			if (count==0) {
				showMessage(msg)
				return false;
			} else {
				document.resultsForm.action = cpath +"/Radiology/Arrivals.do?_method="+method;
				document.resultsForm.submit();
			}
		}

		function validateSearchForm() {
			var searchForm = document.testsForm;
			var presFDate = searchForm.pres_date[0].value;
			var presToDate = searchForm.pres_date[1].value;

			if (presFDate != '') {
				if (!doValidateDateField(searchForm.pres_date[0], 'past')) {
					searchForm.pres_date[0].focus();
					return false;
				}
			}
			if (presToDate != '') {
				if (!doValidateDateField(searchForm.pres_date[1], 'past')) {
					searchForm.pres_date[1].focus();
					return false;
				}
			}

			return true;
		}
	</script>
</head>
<body onload="init()">
<c:set var="selecthouse">
 <insta:ltext key="laboratory.pendingtests.list.inhouse"/>,
 <insta:ltext key="laboratory.pendingtests.list.outsource"/>
</c:set>
<c:set var="priority">
 <insta:ltext key="laboratory.signedoffreportslist.report.star"/>,
 <insta:ltext key="laboratory.signedoffreportslist.report.regular"/>
</c:set>
<c:set var="conducted">
	<insta:ltext key="laboratory.testauditlog.search.new.results"/>,
	<insta:ltext key="laboratory.testauditlog.search.new.noresults"/>,
	<insta:ltext key="ui.label.patient.arrived"/>
</c:set>
<c:set var="samplestatus">
 <insta:ltext key="laboratory.pendingtests.list.pending"/>,
 <insta:ltext key="laboratory.pendingtests.list.collected"/>,
  <insta:ltext key="laboratory.pendingtests.list.notrequired"/>
</c:set>
<c:set var="visittype">
 <insta:ltext key="laboratory.pendingtests.list.ip"/>,
 <insta:ltext key="laboratory.pendingtests.list.op"/>,
  <insta:ltext key="laboratory.pendingtests.list.incomingtest"/>
</c:set>
<c:set var="mrno">
 <insta:ltext key="ui.label.mrno"/>
</c:set>
<c:set var="visitid">
 <insta:ltext key="laboratory.pendingtests.list.visitid"/>
</c:set>
<c:set var="presdate">
 <insta:ltext key="laboratory.pendingtests.list.presdate"/>
</c:set>
<c:set var="select">
 <insta:ltext key="selectdb.dummy.value"/>
</c:set>
	<c:set var="testsList" value="${pagedList.dtoList}"/>
	<c:set var="hasResults" value="${not empty testsList}"/>
	<c:set var="hasSampleFlow" value="${diagGenericPref.map.sampleflow_required == 'Y'}" />
	<input type="hidden" name="printType" value="${genPrefs.sampleCollectionPrintType}"/>
	<c:set var="displayToken" value="false"/>


	<jsp:useBean id="recondutableStatusBean" class="java.util.HashSet" scope="request">
	    <%
	    recondutableStatusBean.add("P");
	    recondutableStatusBean.add("C");
	    recondutableStatusBean.add("V");
	    recondutableStatusBean.add("S");
	    recondutableStatusBean.add("MA");
	    recondutableStatusBean.add("CC");
	    recondutableStatusBean.add("TS");
	    recondutableStatusBean.add("CR");
	    %>
	</jsp:useBean>

	<h1 ><insta:ltext key="radiology.arrivals.header"/></h1>
	<c:set var="actionURL" value="${cpath}/Radiology/Arrivals.do"/>
	<c:set var="displayToken" value="${genPrefs.gen_token_for_rad == 'Y'}"/>

	<form name="testsForm" action="${actionURL}">
		<input type="hidden" name="_method" value="getRadiologyArrivalScreen"/>
		<input type="hidden" name="_searchMethod" value="getRadiologyArrivalScreen"/>
		<input type="hidden" name="sortOrder" value="${ifn:cleanHtmlAttribute(param.sortOrder)}"/>

		<insta:search form="testsForm" optionsId="optionalFilter" closed="${hasResults}" validateFunction="validateSearchForm()">
			<div class="searchBasicOpts" >
				<div class="sboField">
					<div class="sboFieldLabel"><insta:ltext key="laboratory.pendingtests.list.mrno.patientname"/></div>
					<div class="sboFieldInput">
						<div id="mrnoAutoComplete">
							<input type="text" name="mr_no" id="mrno" value="${ifn:cleanHtmlAttribute(param.mr_no)}" />
							<div id="mrnoContainer" style="width: 300px"></div>
						</div>
					</div>
				</div>
				<div class="sboField">
					<div class="sboFieldLabel"><insta:ltext key="laboratory.pendingtests.list.labno"/>:</div>
					<div class="sboFieldInput">
						<input type="text" name="labno" size="10" value="${ifn:cleanHtmlAttribute(param.labno)}"/>
					</div>
				</div>
			</div>
			<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
				<table class="searchFormTable">
					<tr>
						<td>
							<div class="sfLabel"><insta:ltext key="laboratory.pendingtests.list.department"/></div>
							<div class="sfField">
								<insta:selectdb name="ddept_id" table="diagnostics_departments" valuecol="ddept_id" displaycol="ddept_name"
									value="${empty param.ddept_id ? userDept : param.ddept_id}"
									dummyvalue="${select}" filtered="true" filtercol="category,status"
									filtervalue="${category},A"/>

							</div>

							<div class="sfLabel"><insta:ltext key="laboratory.pendingtests.list.testname"/>:</div>
							<div class="sfField">
								<div id="test_wrapper">
									<input name="test_name" type="text" id="test_name" 
										value="${ifn:cleanHtmlAttribute(param.test_name)}"/>
									<div id="test_container" style="width: 300px"></div>
								</div>
							</div>

						</td>
						<td>
							<div class="sfLabel"><insta:ltext key="laboratory.pendingtests.list.conductiontype"/></div>
							<div class="sfField">
								<insta:checkgroup name="house_status" selValues="${paramValues.house_status}"
									opvalues="I,O" optexts="${selecthouse}"/>
							</div>
							<div class="sfLabel"><insta:ltext key="laboratory.pendingtests.list.incominghospital"/></div>
							<div class="sfField" style="height: 20px">
								<div id="inhouse_wrapper">
									<input type="text" name="ih_name" id="ih_name" 
										value="${ifn:cleanHtmlAttribute(param.ih_name)}" />
									<div id="inhouse_container"></div>
								</div>
							</div>

							<div class="sfLabel"><insta:ltext key="laboratory.pendingtests.list.outsource"/>:</div>
							<div class="sfField" style="margin-bottom: 20px">
								<div id="outhouse_wrapper">
									<input type="text" name="oh_name" id="oh_name" 
										value="${ifn:cleanHtmlAttribute(param.oh_name)}" />
									<div id="outhouse_container"></div>
								</div>
							</div>

							<div class="sfLabel"><insta:ltext key="laboratory.pendingtests.list.testpriority"/></div>
							<div class="sfField">
								<insta:checkgroup name="priority" selValues="${paramValues.priority}"
								opvalues="S,R" optexts="${priority}"/>
							</div>
						</td>
						<td>
							<div class="sfLabel"><insta:ltext key="laboratory.pendingtests.list.testprescribeddate"/></div>
							<div class="sfField">
								<div class="sfFieldSub"><insta:ltext key="laboratory.pendingtests.list.from"/></div>
									<insta:datewidget name="pres_date" id="pres_date0" valid="past"	value="${paramValues.pres_date[0]}" />
									<input type="hidden" name="pres_date@type" value="date"/>
									<input type="hidden" name="pres_date@op" value="ge,le"/>
									<input type="hidden" name="pres_date@cast" value="y"/>
								</div>
							<div class="sfField">
								<div class="sfFieldSub"><insta:ltext key="laboratory.pendingtests.list.to"/></div>
									<insta:datewidget name="pres_date" id="pres_date1" valid="past"	value="${paramValues.pres_date[1]}" />
							</div>

						</td>

						<td>
							<div class="sfLabel"><insta:ltext key="laboratory.pendingtests.list.conductionstatus"/></div>
           					<div class="sfField">
								<insta:checkgroup name="conducted" selValues="${paramValues.conducted}"
								opvalues="N,NRN,MA" optexts="${conducted}"/>
							</div>

						</td>

						<td class="last">
							<div class="sfLabel"><insta:ltext key="laboratory.pendingtests.list.patienttype"/></div>
							<div class="sfField">
								<insta:checkgroup name="visit_type" opvalues="i,o,t" optexts="${visittype}"
									selValues="${paramValues.visit_type}"/>
							</div>
							<div class="sfLabel"><insta:ltext key="laboratory.pendingtests.list.incomingpatientname"/></div>
							<div class="sfField">
								<input type="text" name="inc_patient_name" 
									value="${ifn:cleanHtmlAttribute(param.inc_patient_name)}"/>
								<input type="hidden" name="inc_patient_name@op" value="ilike"/>
							</div>

							<div class="sfLabel"><insta:ltext key="laboratory.pendingtests.list.incomingpatient.otherinfo"/></div>
							<div class="sfField">
								<input type="text" name="patient_other_info" 
									value="${ifn:cleanHtmlAttribute(param.patient_other_info)}"/>
							</div>
						</td>
					</tr>
				</table>
			</div>
		</insta:search>
	</form>
	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
	<form name="resultsForm"  method="POST">
		<input type="hidden" name="_method" value=""/>
		<input type="hidden" name="visitid" value=""/>
		<input type="hidden" name="visitType" value=""/>
		<input type="hidden" name="reportId" value=""/>
		<input type="hidden" name="prescId" id="prescribed_id" value=""/>
		<div class="resultList">
			<table class="resultList dialog_displayColumns" id="resultTable" cellspacing="0" cellpadding="0" style="empty-cells: show">
				<tr>
					<th><input type="checkbox" onclick="return checkOrUncheckAll('completeCheck',this)" id="completeAll"/></th>
					<c:if test="${!displayToken}">
						<th>#</th>
					</c:if>
					<c:if test="${displayToken}">
						<insta:sortablecolumn name="token_number" title="Token No."/>
					</c:if>
					<insta:sortablecolumn name="mr_no" title="${mrno}"/>
					<insta:sortablecolumn name="patient_id" title="${visitid}"/>
					<th><insta:ltext key="ui.label.patient.name"/></th>
					<th><insta:ltext key="laboratory.pendingtests.list.ordersymbol"/></th>
					<th><insta:ltext key="laboratory.pendingtests.list.testname"/></th>
					<th><insta:ltext key="laboratory.reportsearch.search.conductingdoctor"/></th>
					<th><insta:ltext key="laboratory.pendingtests.list.prescribingdoctor"/></th>
					<insta:sortablecolumn name="pres_date" title="${presdate}"/>
					<c:if test="${islabNoReq eq 'Y'}">
						<th><insta:ltext key="laboratory.pendingtests.list.labno"/></th>
					</c:if>
					<th><insta:ltext key="laboratory.pendingtests.list.remarks"/></th>
				</tr>
				<c:forEach items="${testsList}" var="test" varStatus="st">
					<c:set var="printItem" value="${(test.report_id != 0) and (test.hasData ne 'N')}"/>

					<c:set var="sample_status">
					<c:choose>
						<c:when test="${test.sample_needed == 'n'}">U</c:when>
						<c:otherwise>${test.sflag}</c:otherwise>
					</c:choose>
					</c:set>
					<c:set var="sampleRequired"
							value="${(test.house_status eq 'O' || hasSampleFlow) && sample_status eq '0'}"/>
					<c:set var="selectOutHouse"
							value="${sample_status eq 'U' && test.house_status eq 'O' && (empty test.outhouse_hospital_name)}"/>
					<c:set var="collectionCenterTest"
							value="${max_centers_inc_default > 1 && category eq 'DEP_LAB' && test.house_status eq 'O' && test.outsource_dest_type eq 'C'}"/>
					<c:set var="sampleAssertion" value="${diagGenericPref.map.sample_assertion == 'Y'}" />
					<c:set var="blockAsserted" value="${(sampleAssertion == true && test.sample_collection_status ne 'A') && test.sample_status ne 'U' }"/>
					<c:set var="sampleStatus" value="${test.sample_status eq '0' ? 'false' : 'true'}"/>
					<c:set var="flagColor">
						<c:choose>
							<c:when test="${!test.billPaid}">grey</c:when>
							<c:when test="${test.conducted == 'MA'}">brown</c:when>
							<c:when test="${test.collectSample}">blue</c:when>
							<c:when test="${test.assignOuthouse}">yellow</c:when>
							<c:otherwise>empty</c:otherwise>
						</c:choose>
					</c:set>
					<c:choose>
						<c:when test="${test.billPaid}">
							<c:set var="blockUnpaid" value="false"/>
						</c:when>
						<c:otherwise>
							<c:set var="blockUnpaid" value="${directBillingPrefs[category == 'DEP_LAB' ? 'Laboratory' : 'Radiology'].map.block_unpaid == 'Y'}"/>
						</c:otherwise>
					</c:choose>


					<tr id="toolbarRow${st.index}">
						<td>
							<input type="checkbox" name="completeCheck" id="completeCheck" value="${test.prescribed_id}"
								${(!test.resultEntryApplicable || test.collectSample  || blockAsserted || blockUnpaid  || test.assignOuthouse || collectionCenterTest) ? 'disabled':''}/>
							<input type="hidden" name="test_status" id="test_status" value="${test.conducted}"/>
						</td>
						<c:if test="${!displayToken}">
							<td>${((pagedList.pageNumber-1) * pagedList.pageSize) + st.index + 1}</td>
						</c:if>
						<c:if test="${displayToken}">
							<td>${test.token_number}</td>
						</c:if>
						<td>${test.mr_no}</td>
						<td>${test.pat_id}</td>
						<td><insta:truncLabel value="${test.patient_full_name}" length="30"/></td>
						<td>${test.common_order_id}</td>
						<td>
							<img src="${cpath}/images/${flagColor}_flag.gif"/>
							<c:choose>
							<c:when test="${test.priority=='S'}">
								<b><font color="#444444">
								<insta:truncLabel value="${test.test_name}" length="35"/>
								</font></b>
							</c:when>
							<c:otherwise>
								<insta:truncLabel value="${test.test_name}" length="35"/>
							</c:otherwise>
							</c:choose>
						</td>
						<td>${test.conducting_doctor_name}</td>
						<td>${test.pres_doctor_name }</td>
						<td><fmt:formatDate pattern="dd-MM-yyyy HH:mm" value="${test.pres_date}"/></td>

						<c:if test="${islabNoReq eq 'Y'}">
							<td>${ifn:cleanHtml(test.labno)}</td>
						</c:if>
						<td style="white-space: normal;">
							<insta:truncLabel value="${test.remarks}" length="12"></insta:truncLabel>
						</td>
					</tr>
				</c:forEach>
			</table>
		</div>
		<div id="results" style="white-space: normal">
		</div>
		<div style="padding-top: 7px">
			<label><insta:ltext key="laboratory.reportsearch.search.conductingdoctor"/></label>
			<select name="conducting_doctor" id="conducting_doctor" style="width:12em"
				 class="dropdown" title="Set conducting doctor for all test at once"  }>
				<option value="">${select}</option>
				<c:forEach var="doctor" items="${requestScope.doctors}">
						<option value="${doctor.DOCTOR_ID}">${doctor.DOCTOR_NAME}</option>
					</c:forEach>
			</select>
		</div>
		<div class="screenActions">
			<button type="button" accesskey="D" onclick="doSave('setDoctor')">
			<label><insta:ltext key="radiology.arrivals.set"/>&nbsp;<insta:ltext key="radiology.arrivals.conducting"/>
				<b><u><insta:ltext key="radiology.arrivals.d"/></u></b><insta:ltext key="radiology.arrivals.doctor"/></label></button>&nbsp;|
			<button type="button" id="saveButton" accessKey="P" onclick="return doSave('modalityArrived');">
				<insta:ltext key="ui.label.patient.arrived"/>
			</button>
			<button type="button" id="saveButton" accessKey="C" onclick="return doSave('conductionCompleted');">
				<b><u><insta:ltext key="radiology.arrivals.c"/></u></b><insta:ltext key="radiology.arrivals.condcompleted"/>
			</button>
		</div>
	</form>
	<div class="legend" style="margin-top: 10px;">
		<div class="flag"><img src='${cpath}/images/grey_flag.gif'/></div>
		<div class="flagText"><insta:ltext key="laboratory.pendingtests.list.unpaidbills"/></div>
		<div class="flag"><img src='${cpath}/images/brown_flag.gif'/></div>
		<div class="flagText"><insta:ltext key="ui.label.patient.arrived"/></div>
		<div class="flag"><img src='${cpath}/images/yellow_flag.gif'></div>
		<div class="flagText"><insta:ltext key="laboratory.pendingtests.list.selectouthouse"/></div>
	</div>

</body>
</html>

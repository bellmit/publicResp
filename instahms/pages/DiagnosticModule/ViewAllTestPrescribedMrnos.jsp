<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8"%>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions"  prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="title" value="${category == 'DEP_LAB' ? 'Laboratory' : 'Radiology'} Reports"/>

<html>
<head>
	<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
	<c:set var="max_centers_inc_default" value='<%=GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default")%>'/>
	<insta:js-bundle prefix="laboratory.radiology.batchconduction"/>
	<insta:js-bundle prefix="laboratory.radiology.reportlist"/>
	<insta:js-bundle prefix="diagnostics.diagdashboards"/>
	<script type="text/javascript">
	
	
	var toolbarOptions = getToolbarBundle("js.laboratory.radiology.batchconduction.toolbar");
		var category = '${ifn:cleanJavaScript(category)}';
		var urlRights = <%= new flexjson.JSONSerializer().serialize(session.getAttribute("urlRightsMap")) %>;
		var centerId = ${centerId};
		var max_centers_inc_default = ${max_centers_inc_default};
		var sampleCollectionCenterId = ${sampleCollectionCenterId};
		function validateSearchForm() {
			var presDateFrom = document.getElementById("pres_date0");
			var presDateTo = document.getElementById("pres_date1");
			var mr_no = document.getElementById("mrno").value;
			if (category === 'DEP_LAB') {
				var sample_no = document.getElementById("sample_no").value;
			}
			var common_order_id = document.getElementById("common_order_id").value;

			var presDateReqd = category==='DEP_LAB' ? ((mr_no == undefined || (mr_no!=undefined && mr_no == "")) 
						&& 	(sample_no == undefined || (sample_no != undefined && sample_no == "")) 
						&&	(common_order_id == undefined  || (common_order_id!=undefined && common_order_id == ""))
						&& (presDateFrom.value!=undefined && presDateFrom.value == "")) : 
			((mr_no == undefined || (mr_no!=undefined && mr_no == "")) && (common_order_id == undefined  || (common_order_id!=undefined && common_order_id == "")) && (presDateFrom.value!=undefined && presDateFrom.value == ""));
			
			if (presDateReqd) {
				showMessage("js.laboratory.radiology.reportlist.frompresdaterequired");
				presDateFrom.focus();
				return false;
			}
			// if to date is empty then take today's date
			if (presDateTo.value == "") {
				presDateTo.value = formatDate(new Date(), 'ddmmyyyy', '-');
			}

			if (!doValidateDateField(presDateFrom))
				return false;
			if (!doValidateDateField(presDateTo))
				return false;

			if (parseDateStr(presDateFrom.value) > parseDateStr(presDateTo.value)) {
					showMessage("js.laboratory.radiology.reportlist.signedoff.topresdate");
					presDateFrom.focus();
					return false;
			}

			// Difference of Test Prescribed to and from date in days should not be greater than 31 
			if (daysDiff(parseDateStr(presDateFrom.value), parseDateStr(presDateTo.value)) > 31) {
				showMessage("js.laboratory.radiology.reportlist.presdaterange");
				presDateFrom.focus();
				return false;
			}

			if(!validateOrderId())
				return false;
			var errd=document.getElementById("_exp_rep_ready_date");
			var errt=document.getElementById("_exp_rep_ready_time");
			if(errd!=undefined && errt!=undefined && 
					errd.value!=undefined && errt.value!=undefined){
				if(empty(errd.value) && !empty(errt.value)){
					alert("Please Enter the value for date field in Expected Report Ready Time");
					return false;
				}
				if(!empty(errd.value) && !empty(errt.value)){
					var x=getDatePart(getDateFromField(errd));
					var curDate =getDatePart( new Date());
					if (x < curDate) {
						alert("Date can not be in the Past");
						return false;
					}
					return validateTime(errt);
				}
			}	
			var referName = document.getElementById("_referaldoctorName");
			var referId = document.getElementById("reference_docto_id")
			if(referName!=undefined && referId!=undefined){
				if(empty(referName.value) && !empty(referId.value))
					referId.value = "";
				if(empty(referId.value) && !empty(referName))
					referName.value = "";
			}
			
			return true;
		}
		
	</script>
	<title>${title} - <insta:ltext key="laboratory.reportsearch.search.instahms"/></title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<insta:link type="css" file="widgets.css" />
	<insta:link type="script" file="widgets.js" />
	<insta:link type="js" file="diagnostics/diagdashboards.js" />
	<insta:link type="js" file="dashboardsearch.js"/>
	<insta:link type="js" file="hmsvalidation.js" />
	<insta:link type="js" file="referaldoctorautocomplete.js" />
	<insta:link type="script" file="instadate.js" />
	<script src="<%=request.getContextPath()%>/gettests.do?${test_timestamp}&${ifn:cleanJavaScript(sesHospitalId)}&module=${ifn:cleanJavaScript(module)}"></script>

	<style type="text/css">
		.autocomplete {
			width:33em; /* set width here */
			padding-bottom:0.2em;
		}
	</style>


</head>
<body onload="init();create();" class="yui-skin-sam">
<insta:feedback-panel/>
<c:set var="select">
 <insta:ltext key="selectdb.dummy.value"/>
</c:set>
<c:set var="selecthouse">
 <insta:ltext key="laboratory.pendingtests.list.inhouse"/>,
 <insta:ltext key="laboratory.pendingtests.list.outsource"/>
</c:set>
<c:set var="priority">
 <insta:ltext key="laboratory.signedoffreportslist.report.star"/>,
 <insta:ltext key="laboratory.signedoffreportslist.report.regular"/>
</c:set>
<c:set var="samplestatus">
 <insta:ltext key="laboratory.pendingtests.list.pending"/>,
 <insta:ltext key="laboratory.pendingtests.list.collected"/>,
  <insta:ltext key="laboratory.pendingtests.list.notrequired"/>
</c:set>
<c:set var="conducted">
 <insta:ltext key="laboratory.testauditlog.search.notconducted"/>,
 <insta:ltext key="laboratory.testauditlog.search.inprogress"/>,
  <insta:ltext key="laboratory.testauditlog.search.completed"/>,
  <insta:ltext key="laboratory.testauditlog.search.validated"/>,
  <insta:ltext key="laboratory.testauditlog.search.revisioninprogress"/>,
  <insta:ltext key="laboratory.testauditlog.search.revisioncompleted"/>,
  <insta:ltext key="laboratory.testauditlog.search.revisionvalidated"/>
</c:set>
<c:set var="conductedRad">
 <insta:ltext key="laboratory.testauditlog.search.notconducted"/>,
 <insta:ltext key="ui.label.patient.arrived"/>,
 <insta:ltext key="laboratory.testauditlog.search.conductioncompleted"/>,
 <insta:ltext key="laboratory.testauditlog.search.transcriptionistscheduled"/>,
 <insta:ltext key="laboratory.rad.testauditlog.search.inprogress"/>,
 <insta:ltext key="laboratory.rad.testauditlog.search.completed"/>,
 <insta:ltext key="laboratory.testauditlog.search.validated"/>,
  <insta:ltext key="laboratory.testauditlog.search.changerequired"/>,
 <insta:ltext key="laboratory.testauditlog.search.revisioninprogress"/>,
 <insta:ltext key="laboratory.testauditlog.search.revisioncompleted"/>,
 <insta:ltext key="laboratory.testauditlog.search.revisionvalidated"/>
</c:set>
<c:set var="visittype">
 <insta:ltext key="laboratory.pendingtests.list.ip"/>,
 <insta:ltext key="laboratory.pendingtests.list.op"/>,
  <insta:ltext key="laboratory.pendingtests.list.incomingtest"/>
</c:set>
<c:set var="sponsorType">
 <insta:ltext key="laboratory.pendingtests.list.sponsor"/>,
 <insta:ltext key="laboratory.pendingtests.list.retail"/>
</c:set>
<c:set var="severitystatus">
<insta:ltext key="laboratory.testauditlog.search.allnormal"/>,
<insta:ltext key="laboratory.testauditlog.search.hasabnormalresults"/>,
<insta:ltext key="laboratory.testauditlog.search.hascriticalresults"/>,
<insta:ltext key="laboratory.testauditlog.search.nonvaluebasedtests"/>
</c:set>
<c:set var="mrno">
 <insta:ltext key="ui.label.mrno"/>
</c:set>
<c:set var="grey">
 <insta:ltext key="laboratory.reportsearch.search.grey"/>
</c:set>
<c:set var="blue">
 <insta:ltext key="laboratory.reportsearch.search.blue"/>
</c:set>
<c:set var="green">
 <insta:ltext key="laboratory.reportsearch.search.green"/>
</c:set>
<c:set var="violet">
 <insta:ltext key="laboratory.reportsearch.search.violet"/>
</c:set>
<c:set var="yellow">
 <insta:ltext key="laboratory.reportsearch.search.yellow"/>
</c:set>
<c:set var="darkblue">
 <insta:ltext key="laboratory.reportsearch.search.darkblue"/>
</c:set>
<c:set var="purple">
 <insta:ltext key="laboratory.reportsearch.search.purple"/>
</c:set>
<c:set var="emptycolor">
 <insta:ltext key="laboratory.reportsearch.search.empty"/>
</c:set>
<c:set var="mrno">
 <insta:ltext key="ui.label.mrno"/>
</c:set>

<c:set var="visitid">
 <insta:ltext key="laboratory.reportsearch.search.visitid"/>
</c:set>
<c:set var="headerSponsorType">
	<insta:ltext key="laboratory.reportsearch.search.sponsor_type"/>
</c:set>
<h1>${title}</h1>
<c:set var="visitList" value="${pagedList.dtoList}" />
<c:set var="hasResults" value="${not empty visitList}" />
<c:set var="hasSignOffRights" value="${(roleId le 2) || actionRightsMap['sign_off_lab_reports'] eq 'A'}"/>
<c:set var="hasCancelRights" value="${(roleId le 2) || actionRightsMap.allow_cancel_test == 'A'}"/>

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


<jsp:useBean id="revisionStates" class="java.util.HashSet" scope="request">
    <%
    revisionStates.add("RP");
    revisionStates.add("RC");
    revisionStates.add("RV");
    %>
</jsp:useBean>

<b><c:out value="${param.msg}" /></b>
<b><c:out value="${resultmsg}" /></b>
<c:set var="searchUrl" value="${cpath}/${category == 'DEP_LAB' ? 'Laboratory' : 'Radiology'}/${screenType == 'worklist' ? 'schedules.do' : 'schedulesearch.do'}"/>

<form method="GET" action="${ifn:cleanHtmlAttribute(searchUrl)}" name="diagcenterform">
<input type="hidden" name="_method" value="getScheduleList">
<input type="hidden" name="_searchMethod" value="getScheduleList">

<%-- todo: not showing search filters active title --%>
<insta:search form="diagcenterform" optionsId="optionalFilter" closed="${hasResults}" validateFunction="validateSearchForm()">
	<div class="searchBasicOpts" >
		<div class="sboField">
			<div class="sboFieldLabel"><insta:ltext key="laboratory.reportsearch.search.mrno.patientname"/></div>
			<div class="sboFieldInput">
				<div id="mrnoAutoComplete">
					<input type="text" name="mr_no" id="mrno" value="${ifn:cleanHtmlAttribute(param.mr_no)}" />
					<div id="mrnoContainer" style="width: 300px"></div>
				</div>
			</div>
		</div>
		<c:if test="${category == 'DEP_LAB'}">
			<div class="sboField">
				<div class="sboFieldLabel"><insta:ltext key="laboratory.reportsearch.search.sampleno"/></div>
				<div class="sboFieldInput">
					<input type="text" name="sample_no" id="sample_no" value="${ifn:cleanHtmlAttribute(param.sample_no)}"/>
				</div>
			</div>
		</c:if>
		<div class="sboField">
		<div class="sboFieldLabel"><insta:ltext key="laboratory.reportsearch.search.orderno"/></div>
		<div class="sboFieldInput">
			<input type="text" name="common_order_id" id="common_order_id" value="${ifn:cleanHtmlAttribute(param.common_order_id)}"
				onkeypress="return enterNumOnlyzeroToNine(event)"/>
				<input type="hidden" name="common_order_id@type" value="integer"/>
		</div>
		</div>
		
	</div>

	<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}">
		<table class="searchFormTable">
			<tr>
				<td>
					<div class="sfLabel"><insta:ltext key="laboratory.reportsearch.search.department"/></div>
					<div class="sfField">

						<insta:selectdb name="ddept_id" table="diagnostics_departments" valuecol="ddept_id"
							displaycol="ddept_name" value="${empty param.ddept_id ? userDept : param.ddept_id}"
						    dummyvalue="${select}" filtered="true" filtercol="category,status"
						    filtervalue="${category},A"/>

					</div>
					<div class="sfLabel"><insta:ltext key="laboratory.reportsearch.search.testname"/></div>
					<div class="sfField" style="height: 20px">
						<div id="test_wrapper">
							<input type="text" name="test_name" id="test_name" value="${ifn:cleanHtmlAttribute(param.test_name)}"/>
							<div id="test_container" style="width:500px;"></div>
							<input type="hidden" name="test_name@op" value="ico"/>
						</div>
					</div>
					<div class="sfLabel"><insta:ltext key="laboratory.reportsearch.search.labno"/></div>
					<div class="sfField">
						<input type="text" name="labno"/>
					</div>

					<c:if test="${category == 'DEP_RAD'}">
                    <div class="sfLabel"><insta:ltext key="laboratory.reportsearch.search.prescribedby"/></div>
                    <div class="sfField" style="height: 20px">
                         <div id="referalAutoComplete">
                              <input type="text" name="_doctor_name" id="_doctor_name" value="${ifn:cleanHtmlAttribute(param._doctor_name)}" />
                              <input type="hidden" name="pres_doctor" id="pres_doctor" value="${param.pres_doctor}"/>
                               <div id="prescribedDocContainer"></div>
                         </div>
                    </div>
                    </c:if>

					<c:if test="${category == 'DEP_LAB'}">
						<div class="sfLabel"><insta:ltext key="laboratory.reportsearch.search.originalsampleno"/></div>
							<div class="sfField">
								<input type="text" name="orig_sample_no" id="orig_sample_no" value="${ifn:cleanHtmlAttribute(param.orig_sample_no)}"/>
							</div>
						</div>					
						<div class="sfLabel" style="display: none"><insta:ltext key="laboratory.reportsearch.search.histo.cyto.shortimpression"/></div>
						<div class="sfField" style="display: none">
							<div id="shot_impression_wrapper">
								<input type="text" name="short_impression" id="short_impression" value="${ifn:cleanHtmlAttribute(param.short_impression)}"/>
								<div id="shot_impression_container"></div>
								<input type="hidden" name="short_impression@op" value="ico"/>
							</div>
						</div>
					</c:if>
				</td>
				<td>
					<c:if test="${category == 'DEP_LAB'}">
					<div class="sfLabel"><insta:ltext key="laboratory.reportsearch.search.referredby"/></div>
					<div class="sfField" style="height: 20px">
						<div id="referalAutoComplete">
							<input type="text" name="_referaldoctorName" id="_referaldoctorName" value="${ifn:cleanHtmlAttribute(param._referaldoctorName)}"/>
							<input type="hidden" name="reference_docto_id" id="reference_docto_id" value="${param.reference_docto_id}"/>
							<div id="referalNameContainer"></div>
						</div>
					</div>
					</c:if>

					<c:if test="${category == 'DEP_LAB'}">
                    <div class="sfLabel"><insta:ltext key="laboratory.reportsearch.search.prescribedby"/></div>
                    <div class="sfField" style="height: 20px">
                         <div id="referalAutoComplete">
                              <input type="text" name="_doctor_name" id="_doctor_name" value="${ifn:cleanHtmlAttribute(param._doctor_name)}" />
                              <input type="hidden" name="pres_doctor" id="pres_doctor" value="${param.pres_doctor}"/>
                              <div id="prescribedDocContainer"></div>
                         </div>
                    </div>
                    </c:if>

					<div class="sfLabel"><insta:ltext key="laboratory.reportsearch.search.incominghospital"/></div>
					<div class="sfField" style="height: 20px">
						<div id="inhouse_wrapper">
							<input type="text" name="ih_name" id="ih_name" value="${ifn:cleanHtmlAttribute(param.ih_name)}" />
							<div id="inhouse_container"></div>
						</div>
					</div>

					<div class="sfLabel"><insta:ltext key="laboratory.reportsearch.search.outsource"/></div>
					<div class="sfField">
						<div id="outhouse_wrapper">
							<input type="text" name="oh_name" id="oh_name" value="${ifn:cleanHtmlAttribute(param.oh_name)}" />
							<div id="outhouse_container"></div>
						</div>
					</div>
					
				</td>

				<td>
					<div class="sfLabel"><insta:ltext key="laboratory.reportsearch.search.testprescribeddate"/></div>
					<div class="sfField">
						<div class="sfFieldSub"><insta:ltext key="laboratory.reportsearch.search.from"/></div>
							<insta:datewidget name="pres_date" id="pres_date0" valid="past"	value="${paramValues.pres_date[0]}" />
							<input type="hidden" name="pres_date@type" value="date"/>
							<input type="hidden" name="pres_date@op" value="ge,le"/>
						</div>
					<div class="sfField">
						<div class="sfFieldSub"><insta:ltext key="laboratory.reportsearch.search.to"/></div>
							<insta:datewidget name="pres_date" id="pres_date1" valid="past"	value="${paramValues.pres_date[1]}" />
					</div>

					<div class="sfLabel"><insta:ltext key="laboratory.reportsearch.search.reportdate"/></div>
					<div class="sfField">
						<div class="sfFieldSub"><insta:ltext key="laboratory.reportsearch.search.from"/></div>
							<insta:datewidget name="report_date" id="report_date0" valid="past" value="${paramValues.report_date[0]}" />
							<input type="hidden" name="report_date@type" value="date"/>
							<input type="hidden" name="report_date@op" value="ge,le">
							<input type="hidden" name="report_date@cast" value="y"/>
						</div>
					<div class="sfField">
						<div class="sfFieldSub"><insta:ltext key="laboratory.reportsearch.search.to"/></div>
							<insta:datewidget name="report_date" id="report_date1" valid="past" value="${paramValues.report_date[1]}" />
					</div>
					<c:if test="${sampleCollectionCenterId == -1 && category == 'DEP_LAB'}">
						<div class="sfLabel"><insta:ltext key="laboratory.reportsearch.search.collectioncenter"/></div>
						<div class="sfField">
							<c:choose>
								<c:when test="${max_centers_inc_default > 1 && centerId != 0}">
										<select name="collectionCenterId" id="collectionCenterId" class="dropdown">
												<option value="">${select}</option>
												<option value="-1" ${param.collectionCenterId == -1?'selected':''}>${defautlCollectionCenter}</option>
											<c:forEach items="${collectionCenters}" var="col_Centers">
												<option value="${col_Centers.map.collection_center_id}" ${col_Centers.map.collection_center_id == param.collectionCenterId?'selected':''}>
													${col_Centers.map.collection_center}
												</option>
											</c:forEach>
										</select>
								</c:when>
								<c:otherwise>
									<insta:selectdb id="collectionCenterId"  name="collectionCenterId"
								value="${param.collectionCenterId}" table="sample_collection_centers"
								valuecol="collection_center_id" displaycol="collection_center" dummyvalue="${select}"/>
								</c:otherwise>
							</c:choose>
						</div>
					</c:if>
					<div class="sfLabel"><insta:ltext key="laboratory.reportsearch.search.testpriority"/></div>
					<div class="sfField">
						<insta:checkgroup name="priority" selValues="${paramValues.priority}"
						opvalues="S,R" optexts="${priority}"/>
					</div>
					<div class="sfLabel"><insta:ltext key="laboratory.reportsearch.search.expRepReadyTime"/>:</div>
					<div class="sfField">
					<insta:datewidget name="_exp_rep_ready_date" id="_exp_rep_ready_date" valid="future" value="${param._exp_rep_ready_date}" />
					<input type="text" name="_exp_rep_ready_time" id="_exp_rep_ready_time" class="timefield" 
						value="${ifn:cleanHtmlAttribute(param._exp_rep_ready_time)}" maxlength="5"/>
					<input type="hidden" name="exp_rep_ready_time@type" value="timestamp"/>
					<input type="hidden" name="exp_rep_ready_time@op" value="le"/>
			</div>
				</td>

				<td>
					<div class="sfLabel"><insta:ltext key="laboratory.reportsearch.search.conductionstatus"/></div>
					<c:choose>
					<c:when test="${category == 'DEP_RAD'}">
					<div class="sfField">
						<insta:checkgroup name="conducted" selValues="${paramValues.conducted}"
						opvalues="N,MA,CC,TS,P,C,V,CR,RP,RC,RV" optexts="${conductedRad}"/>
					</div>
					</c:when>
					<c:otherwise>
					<div class="sfField">
						<insta:checkgroup name="conducted" selValues="${paramValues.conducted}"
						opvalues="N,P,C,V,RP,RC,RV" optexts="${conducted}"/>
					</div>
					</c:otherwise>
					</c:choose>
					<c:if test="${category == 'DEP_LAB'}">
						<div class="sfLabel"><insta:ltext key="laboratory.reportsearch.search.samplestatus"/></div>
						<div class="sfField">
							<insta:checkgroup name="sample_status" selValues="${paramValues.sample_status}"
							opvalues="0,1,U" optexts="${samplestatus}"/>
						</div>
					</c:if>
					<div class="sfLabel"> <insta:ltext key="laboratory.reportsearch.search.reportswithtestresults"/></div>
					<div class="sfField">
						<insta:checkgroup name="report_results_severity_status" selValues="${paramValues.report_results_severity_status}"
						opvalues="A,H,C,T" optexts="${severitystatus}"/>
					</div>
				</td>

				<td class="last">
					<div class="sfLabel"><insta:ltext key="laboratory.reportsearch.search.patienttype"/></div>
					<div class="sfField">
						<insta:checkgroup name="visit_type" selValues="${paramValues.visit_type}"
						opvalues="i,o,t" optexts="${visittype}"/>
					</div>
					<div class="sfLabel"><insta:ltext key="laboratory.reportsearch.search.sponsortype"/></div>
					<div class="sfField">
						<insta:checkgroup name="patient_sponsor_type" selValues="${paramValues.patient_sponsor_type}"
						opvalues="S,R" optexts="${sponsorType}"/>
					</div>
					<div class="sfLabel"><insta:ltext key="laboratory.reportsearch.search.incomingpatientname"/></div>
					<div class="sfField">
						<input type="text" name="inc_patient_name" value="${ifn:cleanHtmlAttribute(param.inc_patient_name)}"/>
						<input type="hidden" name="inc_patient_name@op" value="ilike"/>
					</div>

					<div class="sfLabel"><insta:ltext key="laboratory.reportsearch.search.incomingpatient.otherinfo"/></div>
					<div class="sfField">
						<input type="text" name="patient_other_info" value="${ifn:cleanHtmlAttribute(param.patient_other_info)}"/>
					</div>
				</td>
			</tr>
		</table>
	</div>
</insta:search>
</form>

<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}"
	totalRecords="${pagedList.totalRecords}" showRecordCount="true" />

<c:set var="hasSampleFlow" value="${diagGenericPref.map.sampleflow_required == 'Y'}" />

<form name="signOffForm" method='POST' action="?_method=signOffSelectedReports" autocomplete="off">

<div class="resultList">
	<table class="resultList" width="100%" id="dashboardTable" style="empty-cells: show">
		<tr>
			<insta:sortablecolumn name="mr_no" title="${mrno}"/>
			<insta:sortablecolumn name="patient_id" title="${visitid} (${headerSponsorType})"/>
			<th><insta:ltext key="ui.label.patient.name"/></th>
			<th>
				<input type="checkbox" name="checkAllForSignOff"
						onclick="return checkOrUncheckAll('signOff', this)"/>
				<insta:ltext key="laboratory.reportsearch.search.report"/>
			</th>
			<th><insta:ltext key="laboratory.testconduction.list.severity.status"/></th>
			<th>
				<c:choose>
					<c:when test="${islabNoReq == 'Y'}">
						<insta:ltext key="laboratory.reportsearch.search.testname.presdate.labno.order"/></br>
						<label style="font-size: 9px;" title='<insta:ltext key="laboratory.reportsearch.search.conductingdoctor"/>'>(<insta:ltext key="laboratory.reportsearch.search.conductingdoctor"/>)</label>
					</c:when>
					<c:otherwise >
						<insta:ltext key="laboratory.reportsearch.search.testname.presdate.order"/></br>
						<label style="font-size: 9px;" title='<insta:ltext key="laboratory.reportsearch.search.conductingdoctor"/>'>(<insta:ltext key="laboratory.reportsearch.search.conductingdoctor"/>)</label>
					</c:otherwise>
				</c:choose>
			</th>
		</tr>

		<%--Schedules Begins --%>
		<c:set var="rowIndex" value="0"/>

		<c:forEach var="visitBean" items="${visitList}" varStatus="st">
			<c:set var="visitId" value="${visitBean.pat_id}"/>
			<c:set var="visitReports" value="${visitDetails[visitId]}"/>	<%-- is a list of list of tests --%>
			<c:set var="visit" value="${visitReports[0][0].map}"/>	<%-- use first first bean as test --%>

			<c:set var="blockUnpaid" value="${directBillingPrefs[category == 'DEP_LAB' ? 'Laboratory' : 'Radiology'].map.block_unpaid == 'Y' &&
						visit.payment_status eq 'U' && visit.bill_type ne 'C'}"/>
			<c:set var="incomingPatient" value="${category == 'DEP_LAB' && visit.hospital eq 'incoming'}"/>
			<c:set var="isChildCenter" value="${category == 'DEP_LAB' ? (incomingPatient && visit.incoming_source_type eq 'C') : false}"/>
			<c:set var="isLIS" value="${category == 'DEP_LAB' ? (incomingPatient && visit.incoming_source_type eq 'IH') : false}"/>

			<c:set var="collectSampleReq" value="N"/>
			<c:set var="outHouseReq" value="N"/>
			<c:set var="cancleReq" value="N"/>
			<c:if test="${actionRightsMap.cancel_test_any_time == 'A' || roleId == 1 || roleId == 2}">
					<c:set var="cancleReq" value="Y"/>
			</c:if>
			<c:set var="sampleAssertion" value="${diagGenericPref.map.sample_assertion == 'Y'}" />
			<c:forEach var="reportTestList" items="${visitReports}">
				<c:forEach var="testBean" items="${reportTestList}">

					<c:set var="test" value="${testBean.map}"/>
					<c:set var="collectSampleReq" value="${collectSampleReq == 'N' ? ( ((test.house_status eq 'O' || hasSampleFlow) && test.sample_status eq '0') ? 'Y' : 'N' ) : collectSampleReq}"/>
					<c:set var="outHouseReq" value="${outHouseReq == 'N' ? ( ((test.sample_status eq 'U' && test.house_status eq 'O' && (empty test.oh_name)) 
							|| test.sample_status ne 'U' && test.house_status eq 'O' && empty test.oh_name && test.hospital eq 'incoming') ? 'Y' : 'N' ) : outHouseReq}"/>
 					<c:if test="${test.conducted ne 'N'}">
						<c:set var="outHouseReq" value="N"/>
					</c:if>
					<c:set var="cancleReq" value="${cancleReq == 'N' ? (test.conducted == 'N' ? 'Y' : 'N') : cancleReq }"/>
					<c:set var="_blockUnpaid" value="${test.payment_status eq 'U' && test.bill_type ne 'C'}"/>
					<c:set var="blockUnpaid" value="${(blockUnpaid && _blockUnpaid)}"/>
					<c:set var="sampleStatus" value="${test.sample_status eq '0' ? 'false' : 'true'}"/>
				</c:forEach>
			</c:forEach>

			<tr class="${st.first ? 'firstRow' : ''}"
				onclick="showToolbar(${rowIndex}, event, 'dashboardTable',
				{visitid: '${ifn:cleanJavaScript(visitId)}', patient_id: '${ifn:cleanJavaScript(visitId)}', category: '${ifn:cleanJavaScript(category)}', mrno: '${visit.mr_no}', title : 'Collect Sample', bulkWorkSheetPrint: 'N'},
				[!${blockUnpaid} && ${outHouseReq == 'Y'} && ${!isChildCenter}, !${blockUnpaid}, !${blockUnpaid}&&!${incomingPatient }&&${collectSampleReq == 'Y' },
				 ${cancleReq == 'Y' && !isChildCenter && !isLIS}, ${sampleStatus}], 'patient');"
				onmouseover="hideToolBar(${rowIndex})" id="toolbarRow${rowIndex}" >

				<td>${visit.mr_no}</td>
				<td>${ifn:cleanHtml(visit.patient_id)} (${ifn:cleanHtml(visit.patient_sponsor_type)})</td>
				<td>${visit.visit_type == 't' ? visit.inc_patient_name : visit.patient_full_name}</td>
				<td>
					<input type="hidden" name="signOff" value="${report.report_id}" disabled/>
					<input type="hidden" name="reportWithIncompleteTests" value="Y"/>
				</td>
				<td>&nbsp;</td>
				<td>&nbsp;</td>
			</tr>
			<c:set var="rowIndex" value="${rowIndex + 1}"/>
			<c:forEach var="reportTestList" items="${visitReports}">  <%-- list of tests for this report --%>

				<c:set var="report" value="${reportTestList[0].map}"/>	<%-- use first test bean as report --%>
				<c:set var="reportTest" value="${testBean.map}"/>
				<c:set var="printItem" value="${(not empty report.report_id) && (report.report_has_data ne 'N')}"/>
				<c:set var="childTest" value="${category eq 'DEP_LAB' and max_centers_inc_default > 1 and report.hospital eq 'incoming' and report.incoming_source_type eq 'C'}"/>

				<tr onclick="showToolbar(${rowIndex}, event, 'dashboardTable',
					{reportId: '${ifn:cleanJavaScript(report.report_id)}', category: '${ifn:cleanJavaScript(category)}',visitid: '${ifn:cleanJavaScript(report.patient_id)}',
					visit_type: '${ifn:cleanJavaScript(report.visit_type)}',patientid:'${ifn:cleanJavaScript(report.patient_id)}',mrno: '${ifn:cleanJavaScript(visit.mr_no)}',hospital: '${ifn:cleanJavaScript(report.hospital)}'},
					[${!blockUnpaid} && ${childTest ? centerId ne 0 : true},${ifn:contains(recondutableStatusBean,report.conducted)},${printItem}], 'test', true);"
					onmouseover="hideToolBar(${rowIndex})" id="toolbarRow${rowIndex}"	>

					<td class="indent" colspan="3">&nbsp;</td>
					<td class="subResult" valign="top">
						<input type="checkbox" name="signOff" value="${report.report_id}"
							${((empty report.report_id || report.report_id == 0) || (childTest ? centerId eq 0 : false)) ? 'disabled' : ''}/>
						${empty report.report_id || report.report_id == 0 ? '(No Report)' : report.report_name}
					</td>
					<td style="text-align: center">
						<c:choose>
							<c:when test="${report.report_results_severity_status == 'A' }">
								<insta:ltext key="laboratory.reportsearch.search.allnormal"/>
							</c:when>
							<c:when test="${report.report_results_severity_status == 'H' }">
								<insta:ltext key="laboratory.reportsearch.search.abnormalresultsLbl"/>                             
							</c:when>
							<c:when test="${report.report_results_severity_status == 'C' }">
								<insta:ltext key="laboratory.reportsearch.search.criticalresultsLbl"/>                          
							</c:when>
						</c:choose>
					</td>
					<c:set var="signOffApplicable" value="Y"/>
					<td valign="top" class="subResult">
						<c:set var="tatMessage" value=""/>
						<c:set var="textRemarks" value="" />
						<c:forEach var="testBean" items="${reportTestList}" varStatus="status">
							<c:set var="test" value="${testBean.map}"/>
							<c:set var="sampleRequired" value="${(test.house_status eq 'O' || hasSampleFlow) && test.sample_status eq '0'}"/>
							<c:set var="paymentRequired" value="${test.payment_status eq 'U' && test.bill_type ne 'C'}"/>
							<c:set var="selectOutHouse" value="${(test.sample_status eq 'U' && test.house_status eq 'O' && (empty test.oh_name)
										|| test.sample_status ne 'U' && test.house_status eq 'O' && empty test.oh_name && test.hospital eq 'incoming')}"/>
 							<c:if test="${test.conducted ne 'N'}">
								<c:set var="selectOutHouse" value="false"/>
							</c:if>
							<c:set var="signOffApplicable" value="${ ( signOffApplicable == 'N' || test.conducted == 'P' || test.conducted == 'MA' || test.conducted == 'CC' || test.conducted == 'CR' || test.conducted == 'TS' || test.conducted == 'RP' || test.conducted == 'N') ? 'N' : 'Y' }"/>
							<c:set var="isInternalLab" value="${category eq 'DEP_LAB' and test.outsource_dest_type eq 'C'}"/>
							<c:set var="isChildTest" value="${category eq 'DEP_LAB' and max_centers_inc_default > 1 and test.hospital eq 'incoming' and test.incoming_source_type eq 'C'}"/>
							<c:set var="blockAsserted" value="${(sampleAssertion == true && test.sample_collection_status ne 'A') && test.sample_status ne 'U' && !isInternalLab}"/>
							<c:set var="flagColor">
								<c:choose>
									<c:when test="${test.payment_status eq 'U' && test.bill_type ne 'C'}">grey</c:when>
									<c:when test="${sampleRequired}">blue</c:when>
									<c:when test="${test.conducted eq 'C' || test.conducted eq 'U' }">green</c:when>
									<c:when test="${test.conducted eq 'V'}">violet</c:when>
									<c:when test="${selectOutHouse}">yellow</c:when>
									<c:when test="${ifn:contains(revisionStates,test.conducted)}">dark_blue</c:when>
									<c:when test="${sampleAssertion == true && test.sample_receive_status eq 'R' && test.sample_collection_status ne 'A' 
												&& test.sample_status ne 'U' && (empty test.report_id || test.report_id == 0)}">purple</c:when>
									<c:otherwise>empty</c:otherwise>
								</c:choose>
							</c:set>
							<c:set var="editDisabled"
									value="${sampleRequired || blockAsserted || paymentRequired || (not empty test.report_id && test.report_id != 0 ) || selectOutHouse || (isInternalLab ? test.outsource_dest ne centerId : false) || (isChildTest ? centerId eq 0 : false)}"/>
							<input type="checkbox" name="forEdit" value="${test.prescribed_id}"
									${editDisabled ? 'disabled' : ''}/>
							<c:if test="${islabNoReq == 'Y'}">
								<c:set var="labnoText">-(${ifn:cleanHtml(test.labno)})</c:set>
							</c:if>
							<fmt:formatDate value="${test.pres_date}" pattern="dd-MM-yyyy" var="presDate"/>
							<c:set var="sampleNoTxt">
								<c:choose>
									<c:when test="${not empty test.sample_no && category == 'DEP_LAB'}">(${test.sample_no})-</c:when>
								</c:choose>
							</c:set>
							<img src="${cpath}/images/${flagColor}_flag.gif"/>
							<c:choose>
							<c:when test="${test.priority == 'S'}">
							
								<b>${sampleNoTxt}<font color="#444444"><c:out value="${test.test_name}"/>-(${presDate})${labnoText}-[${test.common_order_id}]</font></b>
							</c:when>
							<c:otherwise>
								${sampleNoTxt}<c:out value="${test.test_name}"/>-(${presDate})${labnoText}-[${test.common_order_id}]
							</c:otherwise>
							</c:choose>
							<c:if test="${not empty test.doctor_name}">
								<br/>
								<label style="font-size: 12px; padding-left: 25px">(${test.doctor_name})</label>
							</c:if>
							<br/>
							<c:set var="temp">
							 	${test.test_name}--[<fmt:formatDate pattern="dd-MM-yyyy HH:mm" value='${test.exp_rep_ready_time}'/>]<br/>
							 </c:set>
							<c:set var="tatMessage" value="${status.first ? '' : tatMessage} ${temp}"/>
							<c:set var="tempRemarks">
							 	<br />${test.test_name} -- ${test.remarks}
							 </c:set>
							<c:set var="textRemarks" value="${textRemarks} ${tempRemarks} " />
						</c:forEach>
						<input type="hidden" name="reportWithIncompleteTests" value="${signOffApplicable }"/>
					</td>
					<script>
					extraDetails['toolbarRow${rowIndex}'] = {'Expected Report Ready Time': '${tatMessage}', 'Remarks': '${textRemarks}'};
					</script>
				</tr>
				<c:set var="rowIndex" value="${rowIndex + 1}"/>
			</c:forEach> <%-- End of Report List --%>
		</c:forEach> <%--Schedules Ends --%>
	</table>
</div>
<c:if test="${empty initialScreen}">
	<insta:noresults hasResults="${hasResults}"/>
</c:if>

<div style="margin-top: 7px">
	<label><insta:ltext key="laboratory.reportsearch.search.conductingdoctor"/></label>
	<c:set var="conductingDoctorLength" value="${fn:length(requestScope.doctors)}"></c:set>
	<select name="commonConductingDoctor" id="commonConductingDoctor" style="width:12em"
		 class="dropdown" title="Set conducting doctor for all test at once"  }>
		<option value="">${select}</option>
		<c:forEach var="doctor" items="${requestScope.doctors}">
				<option value="${doctor.DOCTOR_ID}" ${conductingDoctorLength == 1 ? 'selected': ''}>${doctor.DOCTOR_NAME}</option>
			</c:forEach>
	</select>
</div>
<div style="float: left; margin-top: 20px" style="display: ${hasResults ? 'block' : 'none'}">
		<button type="button" accesskey="C" onclick="setConductingDoc()">
		<label><insta:ltext key="laboratory.reportsearch.search.set"/>&nbsp;<b><u><insta:ltext key="laboratory.reportsearch.search.c"/></u></b><insta:ltext key="laboratory.reportsearch.search.onductingdoctor"/></label></button>&nbsp;|
	<c:if test="${hasSignOffRights}">
		<button type="button" accesskey="S" onclick="SignOff('N')">
		<label><b><u><insta:ltext key="laboratory.reportsearch.search.s"/></u></b><insta:ltext key="laboratory.reportsearch.search.ignoffreports"/></label></button>&nbsp;|
		<button type="button" accesskey="O" onclick="SignOff('Y')">
		<label><b><u><insta:ltext key="laboratory.reportsearch.search.s"/></u></b><insta:ltext key="laboratory.reportsearch.search.ignoff.print"/></label></button>&nbsp; |
	</c:if>
	<button type="button" accessKey="P" onclick="PrintAll('N')">
	<label><b><u><insta:ltext key="laboratory.reportsearch.search.p"/></u></b><insta:ltext key="laboratory.reportsearch.search.rintreports"/></label></button>&nbsp;
</div>

<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
	<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
	<div class="flagText"><insta:ltext key="laboratory.reportsearch.search.unpaidbills"/></div>
	<div class="flag"><img src='${cpath}/images/blue_flag.gif'></div>
	<div class="flagText"><insta:ltext key="laboratory.reportsearch.search.collectsample"/></div>
	<div class="flag"><img src='${cpath}/images/purple_flag.gif'></div>
	<div class="flagText"><insta:ltext key="laboratory.reportsearch.search.pendingassertion"/></div>
	<div class="flag"><img src='${cpath}/images/yellow_flag.gif'></div>
	<div class="flagText"><insta:ltext key="laboratory.reportsearch.search.selectouthouse"/></div>
	<div class="flag"><img src='${cpath}/images/green_flag.gif'></div>
	<div class="flagText"><insta:ltext key="laboratory.reportsearch.search.completed"/></div>
	<div class="flag"><img src='${cpath}/images/violet_flag.gif'></div>
	<div class="flagText"><insta:ltext key="laboratory.reportsearch.search.validated"/></div>
	<div class="flag"><img src='${cpath}/images/dark_blue_flag.gif'></div>
	<div class="flagText"><insta:ltext key="laboratory.reportsearch.search.revision"/></div>
</div>
<div style="clear: both"/>
<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
	<div class="flagText"><insta:ltext key="laboratory.reportsearch.search.legend.sponsor"/></div>
	<div class="flagText" style="padding-left: 10px"><insta:ltext key="laboratory.reportsearch.search.legend.retail"/></div>
</div>

<script>
	var allTestNames = deptWiseTestsjson;
	var HistoCytoNames = <%= request.getAttribute("HistoCytoNames") %>;
	var outHouses = ${outHouses};
	var inHouses = ${inHouses};
	var category = '${ifn:cleanJavaScript(category)}';
	var cancelTestRights = '${actionRightsMap.allow_cancel_test}';
	var roleId = '${roleId}';
	var prescribedDocJSON = ${prescribedDoctors};
	var optimizedLabReportPrint = '${diagGenericPref.map.optimized_lab_report_print}';
</script>
</form>
</body>
</html>


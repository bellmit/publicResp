<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>

<c:set var="cpath" value="${pageContext.request.contextPath}" scope="request"/>
<c:set var="genericPrefs" value="<%= GenericPreferencesDAO.getAllPrefs().getMap()%>" />
<c:set var="medicationList" value="${medicationpagedList.dtoList}" />
<c:set var="hasResults" value="${not empty medicationList}"/>

<html>
<head>
	<title>Medication Chart - Insta HMS</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">

	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>
	<insta:link type="js" file="dashboardsearch.js"/>
	<insta:link type="script" file="hmsvalidation.js"/>
	<script>

	function init() {
		autoMedicineItems();
	}

	var itemAutoComp = null;
	function autoMedicineItems() {
		if (!empty(itemAutoComp)) {
			itemAutoComp.destroy();
			itemAutoComp = null;
		}
		var itemType = 'M';

		var orgId = document.getElementById('_org_id').value;
		var ds = new YAHOO.util.XHRDataSource(cpath + '/wardactivities/IPPrescriptions.do');
		ds.scriptQueryAppend = "_method=findItems&searchType=" + itemType + "&org_id=" + orgId ;
		ds.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;

		ds.responseSchema = {
			resultsList : "result",
			fields : [  {key : "item_name"},
						{key : "order_code"},
						{key : "item_id"},
						{key : "qty"},
						{key : "generic_code"},
						{key : "generic_name"},
						{key : "ispkg"},
						{key : "master"},
						{key : "item_type"},
						{key : 'consumption_uom'},
						{key : 'item_form_id'},
						{key : 'item_strength'},
						{key : 'pack_type'}
					 ],
			numMatchFields: 2
		};

		itemAutoComp = new YAHOO.widget.AutoComplete("medicine_name", "itemContainer", ds);
		itemAutoComp.minQueryLength = 1;
		itemAutoComp.animVert = false;
		itemAutoComp.maxResultsDisplayed = 50;
		itemAutoComp.resultTypeList = false;
		var forceSelection = true;
		itemAutoComp.forceSelection = forceSelection;

		itemAutoComp.filterResults = Insta.queryMatchWordStartsWith;
		itemAutoComp.formatResult = function(oResultData, sQuery, sResultMatch) {
			var record = oResultData;
			var highlightedValue = Insta.autoHighlightWordBeginnings.call(this, oResultData, sQuery);

			return highlightedValue;
		}

		return itemAutoComp;
	}
    function printActivities() {
	var patientId = document.getElementById('visit_id').value;
	var printerId = document.getElementById('printerId').value;
	if (printerId == '') {
		alert("Please select the printer settings");
		return false;
	}
	if (patientId == '') {
		alert("Please select the patient Id");
		return false;
	}
	window.open(cpath+"/wardactivities/MedicationChart.do?_method=printMedicationChart" +
					"&patientId=" + patientId + "&printerId=" + printerId);
   }
	</script>
	<style>
		.scrollForContainer .yui-ac-content{
			 max-height:11em;overflow:auto;overflow-x:auto; /* scrolling */
		}
		.yui-ac {
			padding-bottom: 20px;
		}
	</style>
	<insta:js-bundle prefix="registration.patient"/>
</head>

<body onload="init()">
	<h1>Medication Chart</h1>
	<insta:feedback-panel/>
	<c:choose >
	<c:when test="${not empty param['visit_id']}">
		<insta:patientdetails visitid="${param['visit_id']}" showClinicalInfo="true"/>

		<form name="medicationchartform" action="MedicationChart.do" method="GET" autocomplete="off">
			<input type="hidden" name="_org_id" id="_org_id" value="${patient.org_id}"/>
			<input type="hidden" name="visit_id" id="visit_id" value="${patient.patient_id}"/>
			<input type="hidden" name="_method" value="list"/>
			<input type="hidden" name="_searchMethod" value="list"/>
			<input type="hidden" name="sortOrder" value="${param.sortOrder}"/>
			<input type="hidden" name="sortReverse" value="${param.sortReverse}"/>
			<insta:search form="medicationchartform" optionsId="optionalFilter" closed="${hasResults}">
				<div class="searchBasicOpts" >
					<div class="sboField">
						<div class="sboFieldLabel">Medicine (Hospital Items)</div>
						<div class="sboFieldInput">
							<div id="medicineAutoComplete">
								<input type="text" name="medicine_name" id="medicine_name" style="width: 400px" value="${param.medicine_name}"/>
								<input type="hidden" name="item_id" id="item_id"/>
								<div id="itemContainer" class="scrollForContainer" style="width: 400px" />
							</div>
						</div>
					</div>
				</div>

				<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
					<table class="searchFormTable">
						<tr>
							<td>
								<div class="sfLabel">Doctor</div>
								<div class="sfField">
									<insta:selectdb name="doctor_id" table="doctors" valuecol="doctor_id"
												displaycol="doctor_name" size="8" style="width: 11em" multiple="true"
													values="${paramValues['doctor_id']}" orderby="doctor_name" />
								</div>
							</td>
							<td>
								<div class="sfLabel">Prescription Start Date Range</div>
								<div class="sfField">
									<div class="sfFieldSub">From:</div>
									<insta:datewidget name="prescription_date" valid="past" id="prescription_date0" value="${paramValues.prescription_date[0]}" />
								</div>
								<div class="sfField">
									<div class="sfFieldSub">To:</div>
									<insta:datewidget name="prescription_date" valid="past"	id="prescription_date1" value="${paramValues.prescription_date[1]}" />
									<input type="hidden" name="prescription_date@op" value="ge,le">
									<input type="hidden" name="prescription_date@cast" value="y">
								</div>
							</td>
							<td class="last">
								<div class="sfLabel">Medicine Administered Date Range</div>
								<div class="sfField">
									<div class="sfFieldSub">From:</div>
									<insta:datewidget name="completed_date" valid="past" id="completed_date0" value="${paramValues.completed_date[0]}" />
								</div>
								<div class="sfField">
									<div class="sfFieldSub">To:</div>
									<insta:datewidget name="completed_date" valid="past" id="completed_date1" value="${paramValues.completed_date[1]}" />
									<input type="hidden" name="completed_date@op" value="ge,le">
									<input type="hidden" name="completed_date@cast" value="y">
								</div>
							</td>
						</tr>
					</table>
				</div>
			</insta:search>
	</form>
		<insta:paginate curPage="${medicationpagedList.pageNumber}" numPages="${medicationpagedList.numPages}" totalRecords="${medicationpagedList.totalRecords}" />

		<div class="resultList">
			<table class="detailList dialog_displayColumns" cellspacing="0" cellpadding="0" id="medicationChartTable" border="0" width="100%">
				<tr>
					<insta:sortablecolumn name="medicine_name" title="Medicine"/>
					<insta:sortablecolumn name="doctor_name" title="Doctor"/>
					<th>Strength</th>
					<th>Dosage</th>
					<th>Frequency</th>
					<th>Route</th>
					<th>Start Date</th>
					<th>End Date</th>
					<th>Presc Remarks</th>
					<insta:sortablecolumn name="completed_date" title="Adm Date"/>
					<insta:sortablecolumn name="completed_by" title="Adm By"/>
					<th>Activity Remarks</th>
				</tr>
				<c:forEach items="${medicationList}" var="medicationChart" varStatus="loop">
					<tr>
						<td><insta:truncLabel value="${medicationChart.map.medicine_name}" length="20"/></td>
						<td><insta:truncLabel value="${medicationChart.map.doctor_name}" length="15"/></td>
						<td><insta:truncLabel value="${medicationChart.map.med_strength}" length="5"/></td>
						<td><insta:truncLabel value="${medicationChart.map.med_dosage}" length="5"/></td>
						<td>
							<c:choose>
								<c:when test="${medicationChart.map.repeat_interval_units == 'M'}">
									<c:set var="interval_units" value="Minutes"/>
								</c:when>
								<c:when test="${medicationChart.map.repeat_interval_units == 'H'}">
									<c:set var="interval_units" value="Hours"/>
								</c:when>
								<c:when test="${medicationChart.map.repeat_interval_units == 'D'}">
									<c:set var="interval_units" value="Days"/>
								</c:when>
							</c:choose>
						<c:set var="interval" value="${medicationChart.map.repeat_interval} ${interval_units}"/>
						<insta:truncLabel value="${medicationChart.map.freq_type == 'F' ? medicationChart.map.recurrence_name : interval}" length="10"/>
						</td>
						<td><insta:truncLabel value="${medicationChart.map.med_route_name}" length="5"/></td>
						<td><fmt:formatDate pattern="dd-MM-yyyy HH:mm" value="${medicationChart.map.start_datetime}" var="startDate"/>${startDate}</td>
						<td><fmt:formatDate pattern="dd-MM-yyyy HH:mm" value="${medicationChart.map.end_datetime}" var="endDate"/>${endDate}</td>
						<td><insta:truncLabel value="${medicationChart.map.presc_remarks}" length="30"/></td>
						<td><fmt:formatDate pattern="dd-MM-yyyy HH:mm" value="${medicationChart.map.completed_date}" var="completedDate"/>${completedDate}</td>
						<td><insta:truncLabel value="${medicationChart.map.completed_by}" length="10"/></td>
						<td><insta:truncLabel value="${medicationChart.map.activity_remarks}" length="30"/></td>
					</tr>
				</c:forEach>
			</table>
			<insta:noresults hasResults="${hasResults}"/>
		</div>
		<div style="margin-top: 10px">
			<a href="${cpath}/ipemr/index.htm#/filter/default/patient/${ifn:cleanURL(patient.mr_no)}/ipemr/visit/${ifn:cleanURL(patient.patient_id)}?retain_route_params=true"><insta:ltext key="ui.label.rename.ipemr"/> </a>
			<insta:screenlink screenId="activities_list" extraParam="?_method=list&patient_id=${patient.patient_id}"
				label="Patient Ward Activities" addPipe="true"/>
		<div style="float: right;">
			<insta:selectdb name="printerId" id="printerId" table="printer_definition" class="dropdown"
							valuecol="printer_id"  displaycol="printer_definition_name"/>
			<input type="button" name="print" value="Print" onclick="printActivities()"/>
		</div>
	</div>
	</c:when>
	</c:choose>
</body>
</html>
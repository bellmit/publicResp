<%@ taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<c:set var="max_centers_inc_default" value='<%=GenericPreferencesDAO.getAllPrefs().get("max_centers_inc_default")%>'/>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<html>
<head>
	<title>Patient Planned Surgeries / Procedures List - Insta HMS</title>
	<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
	<insta:link type="css" file="widgets.css" />
	<insta:link type="js" file="dashboardsearch.js"/>
	<insta:link type="js" file="ajax.js"/>
	<script>
		var cpath = '${cpath}';
		var operations = ${operations};
		var extraDetails = [];
		var operationToolbar = {
			OTManagement: { title: 'Surgery/Procedure Management', imageSrc: 'icons/Edit.png',
					href: 'otservices/OtManagement.do?_method=getOtManagementScreen',
					show: ${urlRightsMap.conduct_or_add_operation_doc == 'A'}
			}
		};
		

		function init() {
			createToolbar(operationToolbar);
			initMrNoAutoComplete('${cpath}');
			initOperationAutoComplete();
			initTooltip('resultTable', extraDetails);
			document.getElementById("divToolBar_default").style.width="215px"
		}

		function initOperationAutoComplete() {
			var ds = new YAHOO.util.LocalDataSource({result:operations});
		 	ds.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;

			ds.responseSchema = {
				resultsList : "result",
				fields : [  {key : "operation_name"},{key : "op_id"} ]
			};

			var autoComp = new YAHOO.widget.AutoComplete('operation', 'opConatainer', ds);
			autoComp.minQueryLength = 0;
			autoComp.animVert = false;
			autoComp.maxResultsDisplayed = 20;
			autoComp.resultTypeList = false;
			autoComp.forceSelection = false;
		}

	</script>
</head>
<body onload="init();ajaxForPrintUrls();">
<h1>Planned Surgeries / Procedures List</h1>
<insta:feedback-panel/>
<form method="GET"  action="PlannedOperations.do" name="plannedOperationForm">
	<c:set var="operationsList" value="${pagedList.dtoList}"/>
	<c:set var="hasResults" value="${not empty operationsList}"/>
	<input type="hidden" name="_method" value="getPlannedOperationsList"/>
	<input type="hidden" name="_searchMethod" value="getPlannedOperationsList"/>

	<insta:search form="plannedOperationForm" optionsId="optionalFilter" closed="${hasResults}">
		<div class="searchBasicOpts" >
			<div class="sboField">
				<div class="sboFieldLabel">Mr No:</div>
				<div class="sboFieldInput">
					<div id="mrNoAutocomplete">
						<input type="text" name="mr_no" id="mrno" size="10" value="${ifn:cleanHtmlAttribute(param.mr_no)}"/>
						<div id="mrnoContainer"></div>
					</div>
				</div>
			</div>
			<div class="sboField">
			<div class="sboFieldLabel">Operation Theatre</div>
					<div class="sboFieldInput">
						<select class="dropdown" name="theatre_id" id="theatre_id">
							<option value="">-- Select --</option>
							<c:forEach items="${userTheatres}" var="userTheatres">
								<option
									value="${userTheatres.map.theatre_id}"
											 ${param.theatre_id == userTheatres.map.theatre_id ? 'selected' : ''}>${userTheatres.map.theatre_name}</option>
							</c:forEach>
						</select> <input type="hidden" name="patient_name@op" value="ilike">
					</div>
			</div>
			<div class="sboField">
			<div class="sboFieldLabel">Surgery / Procedure</div>
				<div class="sboFieldInput">
				<div id="opAutocomplete">
					<input type="text" name="operation" id="operation" value="${ifn:cleanHtmlAttribute(param.operation)}"/>
					<div id="opConatainer" style="width: 610px;"></div>
				</div>
				<input type="hidden" name="operation@op" value="ilike">
				</div>
			</div>
		</div>

		<div id="optionalFilter" style="clear: both; display: ${hasResults ? 'none' : 'block'}" >
			<table class="searchFormTable">
				<tr>
					<td>
						<div class="sfLabel">Surgery/Procedure Appt. Date</div>
							<div class="sfField">
								<div class="sfFieldSub">From:</div>
								<insta:datewidget name="operation_scheduled_time"  id="operation_scheduled_time0" value="${paramValues.operation_scheduled_time[0]}" />
							</div>
							<div class="sfField">
								<div class="sfFieldSub">To:</div>
								<insta:datewidget name="operation_scheduled_time" id="operation_scheduled_time1" value="${paramValues.operation_scheduled_time[1]}" />
								<input type="hidden" name="operation_scheduled_time@op" value="ge,le">
								<input type="hidden" name="operation_scheduled_time@type" value="date">
								<input type="hidden" name="operation_scheduled_time@cast" value="y">
							</div>
					</td>
					<td>
						<div class="sfLabel">Surgery / Procedure Status</div>

							<div class="sfField">
								<insta:checkgroup name="operation_status" selValues="${paramValues.operation_status}"
									opvalues="P,C,X" optexts="In Progress,Completed,Cancelled"/>
							</div>
					</td>
					<td>
						<div class="sfLabel">Billing Status</div>
							<div class="sfField">
								<insta:checkgroup name="_billing_status" selValues="${paramValues._billing_status}"
									opvalues="B,NB" optexts="Billed,Not Billed"/>
							</div>
					</td>

					<c:choose>
						<c:when test="${max_centers_inc_default > 1 && centerId == 0}">
							<input type="hidden" name="center_id@cast" value="y"/>
							<input type="hidden" name="center_id@type" value="integer"/>
							<td class="last">
								<div class="sfLabel">Center:</div>
								<div class="sfField">
									<select class="dropdown" name="center_id" id="center_id">
										<option value="">-- Select --</option>
										<c:forEach items="${centers}" var="center">
											<option value="${center.map.center_id}"
												${param['center_id'] == center.map.center_id ? 'selected' : ''}>${center.map.center_name}</option>
										</c:forEach>
									</select>
								</div>
							</div>
						</c:when>
						<c:otherwise>
							<input type="hidden" name="center_id@cast" value="y"/>
							<input type="hidden" name="center_id@type" value="integer"/>
							<input type="hidden" name="center_id" value="${centerId}"/>
						</c:otherwise>
					</c:choose>
				</tr>
			</table>
		</div>
	</insta:search>

	<insta:paginate curPage="${pagedList.pageNumber}" numPages="${pagedList.numPages}" totalRecords="${pagedList.totalRecords}"/>
	<div class="resultList">
		<table class="resultList" id="resultTable" cellspacing="0" cellpadding="0" style="empty-cells: show">
			<tr>
				<insta:sortablecolumn name="mr_no" title="MR No."/>
				<insta:sortablecolumn name="patient_id" title="Visit Id"/>
				<th>Patient Name</th>
				<insta:sortablecolumn name="operation_scheduled_time" title="Scheduled Time"/>
				<th>Theatre / Room</th>
				<th>Surgery / Procedure</th>
			</tr>
			<c:forEach items="${operationsList}" var="operation" varStatus="st">
				<c:set var="disableOTMangement" value="${ operation.operation_status != 'X' && not empty operation.patient_id }"/>
				<c:set var="flagColor">
					<c:choose>
						<c:when test="${operation.operation_status == 'C' && operation.added_to_bill eq 'N'}">yellow</c:when>
						<c:when test="${operation.operation_status == 'C' && operation.added_to_bill eq 'Y'}">green</c:when>
						<c:when test="${operation.operation_status == 'X'}">red</c:when>
						<c:otherwise>empty</c:otherwise>
					</c:choose>
				</c:set>

				<tr onclick="showToolbar(${st.index}, event, 'resultTable',
						{operation_details_id: '${operation.operation_details_id}',prescribed_id : '${operation.prescribed_id}', visit_id: '${operation.patient_id}'},
						[${disableOTMangement}]);"
					onmouseover="hideToolBar(${st.index})" id="toolbarRow${st.index}">
					<td><img src="${cpath}/images/${flagColor}_flag.gif"/>${operation.mr_no}</td>
					<td>${operation.patient_id}</td>
					<td><insta:truncLabel value="${operation.patient_name}" length="50"/></td>
					<td><fmt:formatDate pattern="dd-MM-yyyy HH:mm" value="${operation.operation_scheduled_time}"/></td>
					<td><insta:truncLabel value="${operation.theatre_name}" length="30"/></td>
					<td><insta:truncLabel value="${operation.operation}" length="50"/></td>
				</tr>
				<script>
					var primarySurgeons = <insta:jsString value="${operation.primary_surgeon_name}"/>;
						primarySurgeons = truncateText(primarySurgeons,100);
					var conductionRemarks = <insta:jsString value="${operation.conduction_remarks}"/>;
						conductionRemarks = truncateText(conductionRemarks,100);
					var cancelRemarks = <insta:jsString value="${operation.cancel_reason}"/>;
						cancelRemarks = truncateText(cancelRemarks,100);

					extraDetails['toolbarRow${st.index}'] = {
						'Patient Name': <insta:jsString value="${operation.patient_name}"/>,
						'Surgery/Procedure(Pri.)':<insta:jsString value="${operation.operation}"/>,
						'Surgeon/Doctor(Pri.)': primarySurgeons,
						'Theatre/Room': '${operation.theatre_name}',
						'Surgery/Procedure Start' : '<fmt:formatDate value="${operation.surgery_start}" pattern="dd-MM-yyyy HH:mm:SS"/>',
						'Surgery/Procedure End' : '<fmt:formatDate value="${operation.surgery_end}" pattern="dd-MM-yyyy HH:mm:SS"/>',
						'Conduction Remarks' : conductionRemarks,
						<c:if test="${not empty operation.cancel_reason}">
							'Cancellation Remarks' : cancelRemarks,
						</c:if>
					};
				</script>

			</c:forEach>
		</table>
	</div>
	<insta:noresults hasResults="${hasResults}"/>
		<div class="legend" style="display: ${hasResults? 'block' : 'none'}" >
			<div class="flag"><img src='${cpath}/images/red_flag.gif'></div>
			<div class="flagText">Cancelled</div>
			<div class="flag"><img src="${cpath}/images/green_flag.gif"></div>
			<div class="flagText">Completed And Billed</div>
			<div class="flag"><img src='${cpath}/images/yellow_flag.gif'></div>
			<div class="flagText">Completed And Not Billed</div>
		</div>

</form>
</body>
</html>

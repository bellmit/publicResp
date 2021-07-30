<html>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<c:set var="opList" value="${pagedList.dtoList}"/>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="hasResults" value="${not empty opList}"/>

<head>
	<title>Surgery Kit Availability - Insta HMS</title>
</head>

<body class="yui-skin-sam">

<h1>Surgery Kit Availability</h1>

<form name="sform">
	<input type="hidden" name="_method" value="list"/>
	<input type="hidden" name="_searchMethod" value="list"/>

	<insta:search form="sform" optionsId="optionalFilter" closed="false" >
	  <div class="searchBasicOpts" >
		</div>
		<div id="optionalFilter" style="clear: both; display:'block'" >
			<table class="searchFormTable">
				<tr>
					<td>
						<div class="sfLabel">Appointment Date</div>
						<div class="sfField">
							<div class="sfFieldSub">From:</div>
							<insta:datewidget name="appointment_date" id="appointment_date0"
								value="${paramValues.appointment_date[0]}"/>
						</div>
						<div class="sfField">
							<div class="sfFieldSub">To:</div>
							<insta:datewidget name="appointment_date" id="appointment_date1"
								value="${paramValues.appointment_date[1]}"/>
							<input type="hidden" name="appointment_date@op" value="ge,le"/>
							<input type="hidden" name="appointment_date@cast" value="y"/>
						</div>
					</td>
					<td>
					<div class="sfLabel">Issue Status</div>
					<div class="sfField">
						<insta:checkgroup name="issue_status" selValues="${paramValues.issue_status}"
						opvalues="Y,N" optexts="Issued,Not Issued"/>
					</div>
					</td>
				</tr>
			</table>
		</div>
 	</insta:search>
</form>

<div id="storeSelectDiv" style="display: block">
	<label>Show Availability in:</label>
	<select id="storeSelect" class="dropdown"></select>
</div>

<div class="detailList" >

	<table class="detailList" cellspacing="" cellpadding="" id="resultTable" onmouseover="hideToolBar();">
		<tr onmouseover="hideToolBar();">
			<th>#</th>
			<th>Mr No.</th>
			<th>Patient Name</th>
			<th>Surgery/Procedure Name</th>
			<th>OT</th>
			<th>Appointment Time</th>
			<th>Kit Name</th>
			<th style="width:24px"></th>		<%-- for info icon --%>
		</tr>

		<script>var surgeries = [];</script><%-- ideally these should go into retrievable scripts --%>

		<c:forEach var="record" items="${opList}" varStatus="st" >
			<script>
				surgeries[${st.index}] = { op_id: '${record.op_id}', is_sterile_store: 'Y',
					ot_store: ${record.store_id }, kit_id: ${record.kit_id },
					appointment_id: ${record.appointment_id}, issued: '${record.issue_status}',
					disallow_expired: 'checked' }
			</script>
			<tr class="${st.index == 0 ? 'firstRow' : ''} ${st.index % 2 == 0 ? 'even' : 'odd'}"
					id="toolbarRow${st.index}"
					onclick="checkAndShowToolbar(${st.index}, event, 'resultTable', surgeries[${st.index}],
						[${record.transfer_no == 0 }]);">

				<td>${st.index + 1}</td>
				<td>${record.mr_no}</td>
				<td>${record.patient_name}</td>
				<td><insta:truncLabel value="${record.operation_name}" length="30"/></td>
				<td>${record.theatre_name}</td>
				<td><fmt:formatDate value="${record.appointment_time}" pattern="dd-MM-yyyy HH:mm"/></td>
				<td>
					<img class="flag" src="${cpath}/images/empty_flag.gif"/>
					<insta:truncLabel value="${record.issue_status == 'Y' ? '--' : record.kit_name}" length="30"/>
				</td>
				<td>
					<a href="javascript:Info" title="Availability Details">
						<img src="${cpath}/images/information.png" class="button"/>
					</a>
				</td>
			</tr>
		</c:forEach>
	</table>

	<div class="legend">
		<div class="flag"><img src='${cpath}/images/red_flag.gif'></div>
		<div class="flagText">Not Available</div>
		<div class="flag"><img src='${cpath}/images/grey_flag.gif'></div>
		<div class="flagText">Issued</div>
	</div>

	<div id="avDialog" style="visibility: hidden; display:none">
		<div class="bd" style="text-align:center">
			<div id="avDialogDetails" style="max-height: 500px; overflow: auto">
				<b>Availability Details</b>
				<table class="dataTable" width="100%" height="100%"
					cellspacing="0" cellpadding="0" id="avDialogTable">
					<tr>
						<th>Item Name</th>
						<th style="text-align: right">Req Qty</th>
						<th style="text-align: right">Avbl Qty</th>
						<th></th> <%-- flag --%>
					</tr>
					<tr style="display:none">
						<td><label></label></td>
						<td style="text-align: right"></td>
						<td style="text-align: right"></td>
						<td><img src="${cpath}/images/grey_flag.gif"/></td>
					</tr>
				</table>
			</div>

			<div id="avDialogIssued" style="display:none">
				<b>The kit is already issued.</b>
			</div>

			<div style="text-align: left; margin-top: 10px;">
				<input type="button" id="avDialogNextBtn" value="Next"/>
				<input type="button" id="avDialogPrevBtn" value="Prev"/>
				<input type="button" id="avDialogCloseBtn" value="Close"/>
		</div>
		</div>
	</div>

	<%-- good practice to put scripts at the end: page load is faster --%>
	<script>var cpath = '${cpath}';</script>
	<script src="${cpath}/cssd/SurgeryKitAvailability.do?_method=getActiveKitDetailsScript"></script>
	<script src="${cpath}/cssd/SurgeryKitAvailability.do?_method=getActiveKitItemsStockScript"></script>
	<script src="${cpath}/cssd/SurgeryKitAvailability.do?_method=getSterileStoresScript"></script>

	<insta:link type="script" file="dashboardsearch.js"/>
	<insta:link type="script" file="CSSD/kit_availability.js"/>

</body>
</html>


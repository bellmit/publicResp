<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn"%>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
	<insta:link type="js" file="master/ScheduledEmailableReport/ScheduledEmailableReport.js"/>
	<insta:link type="script" file="hmsvalidation.js"/>
	<c:set var="cpath" value="${pageContext.request.contextPath}"/>
	<style>

		#reportNameAC  {
		    width:20em; /* set width here */
		    padding-bottom:2.0em;
		}

		.scrolForContainer .yui-ac-content{
			 max-height:11em;overflow:auto;overflow-x:hidden; /* scrolling */
		    _height:11em; /* ie6 */
		}

		.yui-ac-content li {
			font-family:Verdana;
			font-size:12px;
			color:#666666;
			font-weight:normal;
			margin:0;
			padding:2px 5px;
			cursor:default;
			white-space:nowrap;
		}

		.yui-ac-content ui {
			width:25em;
		}
	</style>
	<script>
		var cpath = '${pageContext.request.contextPath}';
		var favReports = ${favouriteReportsJSON};
		var customReports = ${customReportsJSON};
		var builtinReports = ${builtinReportsJSON};
		var events = ${eventsJSON};
	</script>
</head>

<jsp:useBean id="typeDisplay" class="java.util.HashMap"/>
<c:set target="${typeDisplay}" property="B" value="Builtin"/>
<c:set target="${typeDisplay}" property="F" value="Favourite"/>
<c:set target="${typeDisplay}" property="C" value="Custom"/>

<body class="yui-skin-sam" onload="init();">
	<c:set var="dtoList" value="${configuredReportsList}"/>

	<h1>Configure Emailable Scheduled Report</h1>

	<insta:feedback-panel/>

	<form name="mainForm" method="POST" action="ScheduledEmailableReport.do" autocomplete="off">

		<input type="hidden" name="method" value="create"/>

		<div style="overflow: hidden">
			<table class="dashboard" id="dataTable" cellspacing="0" cellpadding="0" width="100%">
				<tr>
					<th>Delete</th>
					<th style="width:20em;">Name</th>
					<th>Report Type</th>
					<th>Frequency</th>
					<th>Period</th>
					<th>Parameters</th>
					<th>Output Mode</th>
					<th style="width:20em;">Email IDs</th>
					<th style="text-align:center;">#</th>
				</tr>

				<c:set var="index" value="0"/>
				<c:forEach items="${dtoList}" var="report">
					<c:set var="index" value="${index + 1}"/>
					<tr>
						<td><input type="checkbox" name="deleteReport" id="deleteReport${index}" onclick="return checkForDelete(this.checked, 'delete${index}');"></td>
						<c:set var="reportName">
							<c:choose>
								<c:when test="${report.report_type == 'B'}">${builtinReportMap[report.report_id].report_name}</c:when>
								<c:when test="${report.report_type == 'C'}">${customReportMap[report.report_id].report_name}</c:when>
								<c:when test="${report.report_type == 'F'}">${favouriteReportMap[report.report_id].report_name}</c:when>
							</c:choose>
						</c:set>
						<td>
							<div style="width: 15px; float: left" id="flgDiv${index}">
								<img src="${cpath}/images/grey_flag.gif"/>
							</div>
							<label id="_l_ReportName${index}">${ifn:cleanHtml(reportName)}</label>
						</td>
						<td><label id="_l_ReportType${index}">${typeDisplay[report.report_type]}</label></td>
						<td><label id="_l_TriggerEnum${index}">${report.trigger_enum}</label></td>
						<td><label id="_l_SubEvent${index}">${report.subevent eq null || report.subevent eq '' ?'Report Default':report.subevent}</label></td>
						<td><label id="_l_params${index}">${report.params}</label></td>
						<td><label id="_l_OutputMode${index}">${report.output_mode}</label></td>
						<td><label id="_l_emailId${index}">${report.email_id}</label> <input type="hidden" id="_l_isSrxml${index}" value="${report.report_type}"/></td>
						<td>
							<input type="button" name="addOredit" value="#" rowId="${index}"/>
							<input type="hidden" name="doc_id" id="hDocId${index}" value="${report.doc_id}"/>
							<input type="hidden" name="report_type" id="hReportType${index}" value="${report.report_type}"/>
							<input type="hidden" name="report_id" id="hReportId${index}" value="${report.report_id}"/>
							<input type="hidden" name="report_name" id="hReportName${index}" value="${ifn:cleanHtmlAttribute(reportName)}"/>
							<input type="hidden" name="trigger_enum" id="hTriggerEnum${index}" value="${report.trigger_enum}"/>
							<input type="hidden" name="subevent" id="hSubEvent${index}" value="${report.subevent eq null|| report.subevent eq ''?'Report Default':report.subevent}"/>
							<input type="hidden" name="params" id="hParams${index}" value="${report.params}"/>
							<input type="hidden" name="output_mode" id="hOutputMode${index}" value="${report.output_mode}"/>
							<input type="hidden" name="email_id" id="hEmailId${index}" value="${report.email_id}"/>
							<input type="hidden" name="delete" id="delete${index}" value="N"/>
						</td>
					</tr>
				</c:forEach>

				<tr class="status_I">
					<td><input type="checkbox" name="deleteReport" id="deleteReport${index+1}" onclick="return checkForDelete(this.checked, 'delete${index+1}');"></td>
					<td><div style="width: 15px" id="flgDiv${index+1}"><img src="${cpath}/images/yellow_flag.gif"/></div><label id="_l_ReportName${index+1}"></label></td>
					<td><label id="_l_ReportType${index+1}"></label></td>
					<td><label id="_l_TriggerEnum${index+1}"></label></td>
					<td><label id="_l_SubEvent${index+1}"></label></td>
					<td><label id="_l_params${index+1}"></label></td>
					<td><label id="_l_OutputMode${index+1}"></label></td>
					<td><label id="_l_emailId${index+1}"></label></td>
					<td>
						<input type="button" name="addOredit" value="#" rowId="${index+1}"/>
						<input type="hidden" name="doc_id" id="hDocId${index+1}" value=""/>
						<input type="hidden" name="report_type" id="hReportType${index+1}" value=""/>
						<input type="hidden" name="report_id" id="hReportId${index+1}" value=""/>
						<input type="hidden" name="report_name" id="hReportName${index+1}" value=""/>
						<input type="hidden" name="trigger_enum" id="hTriggerEnum${index+1}" value=""/>
						<input type="hidden" name="subevent" id="hSubEvent${index+1}" value=""/>
						<input type="hidden" name="params" id="hParams${index+1}" value=""/>
						<input type="hidden" name="output_mode" id="hOutputMode${index+1}" value=""/>
						<input type="hidden" name="email_id" id="hEmailId${index+1}" value=""/>
						<input type="hidden" name="delete" id="delete${index+1}" value="N"/>
					</td>
				</tr>
			</table>
		</div>
		<div class="screenActions">
			<button type="button" name="save" accesskey="S" onclick="return saveValues();"><b><u>S</u></b>ave</button></div>
	</form>
	<div class="fltR" style="margin-top: 25px">
		<div style="width: 15px; float: left"><img src="${cpath}/images/grey_flag.gif"/></div>
		<div style="width: 150px; float: left">Configured Reports</div>
		<div style="width: 15px; float: left"><img src="${cpath}/images/yellow_flag.gif"/></div>
		<div style="width: 150px; float: left">Newly added Reports</div>
	</div>

	<div id="dialog" style="visibility: hidden; display:none">
		<div class="hd" id="dialogHeader">Add Scheduled Email Report</div>
		<div class="bd">
			<form method="POST" action="" name="dialogForm">
				<input type="hidden" id="invokedFor" name="invokedFor" value=""/>
				<table align="center" class="formtable">
					<tbody>
						<tr>
							<td class="formlabel"><label id="categoryLabel">Type:</label></td>
							<td>
								<select name="report_type" id="report_type" class="dropdown" 
										onchange="populateReports(this.value);populatePeriod();">
									<option	value="F" selected>Favourite Reports</option>
									<option value="B">Built-in Reports</option>
									<option value="C">Custom Reports</option>
								</select>
							</td>
						</tr>
						<tr>
							<td class="formlabel">Report Name:</td>
							<td>
								<input type="hidden" name="report_id" value="">
								<div id="favReportNameDiv">
									<div id="favReportNameWrapper" style="height:22px;valign:center;">
										<input type="text" name="favReportName" id="favReportName" class="field"/>
										<div id="favReportName_dropdown" style="width:25em;"></div>
									</div>
								</div>
								<div id="builtinReportNameDiv">
									<div id="builtinReportNameWrapper" style="height:22px;valign:center;">
										<input type="text" name="builtinReportName" id="builtinReportName" class="field"/>
										<div id="builtinReportName_dropdown" style="width:25em;"></div>
									</div>
								</div>
								<div id="customReportNameDiv">
									<div id="customReportNameWrapper" style="height:22px;valign:center;">
										<input type="text" name="customReportName" id="customReportName" class="field"/>
										<div id="customReportName_dropdown" style="width:25em;"></div>
									</div>
								</div>
								<div id="reportNameEditDiv"></div>
							</td>
						</tr>
						<tr>
							<td class="formlabel">Frequency:</td>
							<td>
								<insta:selectoptions name="frequency" class="dropdown"
									opvalues="Daily,Weekly,Monthly" value="Daily"
									optexts="Daily,Weekly,Monthly" onchange="populatePeriod()" />
							</td>
						</tr>
						<tr>
							<td class="formlabel">For Period:</td>
							<td><select name="subevent" id="subevent" class="dropdown"></select>
							</td>
						</tr>
						<tr>
							<td class="formlabel">Parameters:</td>
							<td><input type="text" name="params"/></td>
						</tr>
						<tr>
							<td class="formlabel">Output Mode:</td>
							<td>
								<select name="outputMode" class="dropDown">
									<option value="pdf">pdf</option>
									<option value="csv">csv</option>
									<option value="text">text</option>
								</select>
							</td>
						</tr>

						<tr>
							<td class="formlabel">Email Ids:</td>
							<td>
								<input type="text" name="email_id" id="email_id" style="width:20em" class="field required"
								onkeypress="return updateRowOnEnter(event);"/>
							</td>
						</tr>
					</tbody>
				</table>
			</form>
		</div>
	</div>
<!-- custom report map: ${customReportMap} -->
<!-- builtin report map: ${builtinReportMap} -->
<!-- fav report map: ${favouriteReportMap} -->
</body>

</html>


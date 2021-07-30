<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<c:set var="max_centers"
    value='<%=GenericPreferencesDAO.getGenericPreferences().getMax_centers_inc_default()%>'
    scope="request" />
<html>
<head>
<title><fmt:message key="js.topnav.menu.regulatory.reports.philippines.ohsrs.report" /> - Insta HMS</title>
<insta:link type="script" file="underscore-min.js" />
<insta:link type="script" file="ohsrsdohgovph.js" />
<script>
	var ohsrsdohgovphRendererMeta = ${renderer_meta};
    var cpath = '${cpath}';
	var reportingYears = ${ifn:convertListToJson(reporting_years)};
</script>
<style>
	.fullpage {
		min-height:calc(100vh - 350px);
	}
	
	.messageRelCentered {
		width: 100%;
		box-sizing:border-box;
		text-align: center;
		padding:25vh 0;
		font-size: 14px;
	}
	.datasplit{
		font-size: 9px;
		color:#4285f4;
	}
	#reportContainer table.simple {
		width: 500px;
	}
	#actionBar {
		margin-top:20px;
	}
	#reportContainer h4 {
		font-size: 14px;
		margin-top:20px;
		margin-bottom:5px;
	}
	#reportContainer table td,
	#reportContainer table th {
		border: 1px solid black;
		font-size: 12px;
		padding: 2px;
	}
	#reportContainer table.table {
		overflow-x:scroll;
	}
	#reportContainer table.table td,
	#reportContainer table.table th {
		width: 100px !important;
		text-align: right;
		box-sizing: border-box;
	}
	#reportContainer table.table th {
	 background-color: #d3d3d3 !important;
	 background-image: none;
	 font-weight:normal;
	 color: #000;
	}
	#reportContainer table.simple td:last-child {
		width: 150px;
		text-align: right;
		box-sizing: border-box;
	}
	.pill {
		padding: 3px;
		line-height:1;
		border-radius: 2px;
		color: #FFFFFF;
		font-weight: bold;
		display:inline-block;
		font-size: 9px;
	}
	.pill.unprocessed {
		background: #7f7f7f;
	}
	.pill.generation_failed,
	.pill.submission_failed,
	.pill.signoff_failed
	{
		background: #ea4335;
	}
	.pill.generated,
	.pill.submitted,
	.pill.signoff_completed
	{
		background: #34a853;
	}
	.error_box {
		border: 2px solid #ea4335;
		font-size: 11px;
		color: #ea4335;
		padding: 10px;
		margin: 5px 0;
		display:inline-block;
	}
</style>
</head>
<body>
<c:if test="${max_centers > 1 and centerId eq 0}">
	<div style="margin-bottom:20px; padding:10px 0 10px 10px; background-color:#FFC;" class="brB brT brL brR" id="msgDiv">
		<div class="fltR" style="margin:-8px 0px 0 26px; width:17px;"> <img src="${cpath}/images/fileclose.png" onclick="document.getElementById('msgDiv').style.display='none';"/></div>
		<div class="clrboth"></div>
		<div class="fltL" style="width: 25px; margin:-1px 0 0 3px;"><img src="${cpath}/images/error.png" /></div>
		<div class="fltL"  style="margin:0px 0 0 5px ; width:865px;"><fmt:message key="ui.notification.consultation.select.center"/></div>
		<div class="clrboth"></div>
	</div>
</c:if>
<c:if test="${not (max_centers > 1 and centerId eq 0)}">
	<div class="pageHeader"><fmt:message key="js.topnav.menu.regulatory.reports.philippines.ohsrs.report" /></div>
	<fieldset class="fieldSetBorder">
		<legend class="fieldSetLabel">Reporting Year</legend>
		<div style="margin:10px;">
			<select id="reportingYear">
				<option value="">--Select--</option>
				<c:forEach items="${reporting_years}" var="year">
				<option value="${year}">${year}</option>
				</c:forEach>
			</select>
		</div>
	</fieldset>
	<div id="loader" class="fullpage messageRelCentered" style="display:none;"></div>
	<div id="reportContainer" class="fullpage"><p class="messageRelCentered">Select a reporting year to view/submit report</p></div>
	<div id="actionBar">
		<button type="button" id="regenerate" style="display:none;">Generate / Regenerate Report</button>
		<button type="button" id="submitToOHSRS" style="display:none;">Submit to OHSRS</button>
		<button type="button" id="signoffOhsrsReport" style="display:none;">Signoff OHSRS Report</button>
	</div>
</c:if>
<script type="text/template" id="simple_template">
<div>
	<h4>{%= schema.label %} <span class="pill {%= data.status %}">{%= data.status.replace("_"," ").toUpperCase() %}</span></h4>
	{% if (data.status.indexOf("_failed") !== -1 && data.status.indexOf("signoff_failed") === -1) { %}<div class="error_box">{%= data.details %}</div>{% } %}
	<table class="simple ">
		{% var dbKeys = _.keys(data["db"]) %}
		{% var csvKeys = _.keys(data["csv"]) %}
		{% _.each(schema.fields, function(field) { %}
		{%  var csvData = (csvKeys.indexOf(field.key) !== -1) ? data["csv"][field.key] : 0; %}
		{%  var dbData = (dbKeys.indexOf(field.key) !== -1) ? data["db"][field.key] : 0; %}
		<tr>
			<td>{%= field.label %}</td>
			<td>
			{% if (["amount", "integer"].indexOf(field.data_type) !== -1<c:if test="${midyearGolive}"> && schema.uploadable === "firstyear" && isFirstYear</c:if>) { %}
			{%= (csvData + dbData) %}<br/><span class="datasplit">HMS: {%= dbData %} | CSV: {%= csvData %}</span></td>
			{% } else { %}
			{%= data[schema.uploadable === "always" ? "csv" : "db"][field.key]%}
			{% } %}
			</td>
		</tr>
		{% }) %}
	</table>
</div>
</script>
<script type="text/template" id="table_template">
<div>
	<h4>{%= schema.label %} <span class="pill {%= data.status %}">{%= data.status.replace("_"," ").toUpperCase() %}</span></h4>
	{% if (data.status.indexOf("_failed") !== -1 && data.status.indexOf("signoff_failed") === -1) { %}<div class="error_box">{%= _.escape(data.details).replace(/\n/g, '<br>') %}</div>{% } %}
	<table class="table">
		{% _.each(schema.fields, function(field) { %}
			<th>{%= field.label %}</th>
		{% }) %}
		</tr>
		{% var dbRows = _.keys(data["db"]) %}
		{% var csvRows = _.keys(data["csv"]) %}
		{% var unionRows = _.union(dbRows, csvRows) %}
		{% _.each(unionRows, function(tableIndex) { %}
		<tr>
		{%   var dbKeys = (dbRows.indexOf(tableIndex) !== -1) ? _.keys(data["db"][tableIndex]) : [] %}
		{%   var csvKeys = (csvRows.indexOf(tableIndex) !== -1) ? _.keys(data["csv"][tableIndex]) : [] %}
		{%   _.each(schema.fields, function(field) { %}
		{%     var csvData = (csvKeys.indexOf(field.key) !== -1) ? data["csv"][tableIndex][field.key] : 0 %}
		{%     var dbData = (dbKeys.indexOf(field.key) !== -1) ? data["db"][tableIndex][field.key] : 0 %}
		{%     var csvString = (csvKeys.indexOf(field.key) !== -1) ? data["csv"][tableIndex][field.key] : "" %}
		{%     var dbString = (dbKeys.indexOf(field.key) !== -1) ? data["db"][tableIndex][field.key] : "" %}
        {%     var otherValue = dbString ? dbString : csvString %}
			<td>
			{% if (["amount", "integer"].indexOf(field.data_type) !== -1<c:if test="${midyearGolive}"> && schema.uploadable === "firstyear" && isFirstYear</c:if>) { %}
			{%= (csvData + dbData) %}<br/><span class="datasplit">H: {%= dbData %}<br/>C: {%= csvData %}</span></td>
			{% } else { %}
			{%= otherValue %}
			{% } %}
			</td>
		{%   }) %}
		</tr>
		{% }) %}
	</table>
</div>
</script>
<script type="text/template" id="signoff_template">
	<fieldset class="fieldSetBorder">
		<legend class="fieldSetLabel">Signoff Report</legend>
		{% if (signoffError) { %}<div class="error_box">{%= _.escape(signoffError).replace(/\n/g, '<br>') %}</div>{% } %}
		<div style="padding:10px 10px 5px 10px;">
			{% if (!signoff) { %}
			<label style="width:100px;display:inline-block;">Reported by</label>
			<input type="text" name="reportedby"/>
			{% } else { %}
			Reported by : {%= signoff.reportedby %}
			{% } %}
		</div>
		<div style="padding:5px 10px;">
			{% if (!signoff) { %}
			<label style="width:100px;display:inline-block;">Designation</label>
			<input type="text" name="designation"/>
			{% } else { %}
			Designation : {%= signoff.designation %}
			{% } %}
		</div>
		<div style="padding:5px 10px;">
			{% if (!signoff) { %}
			<label style="width:100px;display:inline-block;">Section</label>
			<input type="text" name="section"/>
			{% } else { %}
			Section : {%= signoff.section %}
			{% } %}
		</div>
		<div style="padding:5px 10px;">
			{% if (!signoff) { %}
			<label style="width:100px;display:inline-block;">Department</label>
			<input type="text" name="department"/>
			{% } else { %}
			Department : {%= signoff.department %}
			{% } %}
		</div>
		{% if (signoff) { %}
		<div style="padding:5px 10px;">
			Date Reported : {%= signoff.datereported %}
		</div>
		{% } %}
	</fieldset>
</script>
<script type="text/template" id="uploader_template">
	<fieldset class="fieldSetBorder">
		<legend class="fieldSetLabel">Upload data</legend>
		<div style="padding:10px 10px 5px 10px;">
			<label style="width:100px;display:inline-block;">Report Section</label>
			<select id="ohsrsFunctionSelect">
				<option value="">--Select--</option>
				{% _.each(schemas, function(schema, key) { %}
				{%	 if (schema.uploadable === "always" <c:if test="${midyearGolive}">|| (schema.uploadable === "firstyear" && isFirstYear)</c:if>) { %}
				<option value="{%= schema.key %}">{%= schema.label %}</value>
				{%   } %}
				{% }) %}
			</select>
			<a id="downloadCSVLink" href="javascript:void();" target="_blank" style="display:none;">Download CSV Template</a>
		</div>
		<div style="padding:5px 10px;">
			<form method="POST" enctype="multipart/form-data" id="csvUploadForm">
				<label style="width:100px;display:inline-block;">CSV File</label>
				<input type="file" id="csvFile" name="csv_file" disabled="disabled"/>
			</form>
		</div>
		<div style="padding:5px 10px 10px 115px;">
			<button type="button" id="uploadCsv" disabled="disabled">Upload</button>
		</div>
	</fieldset>
</script>
</body>
</html>

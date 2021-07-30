<%@ taglib uri="/WEB-INF/struts-html.tld"  prefix="html" %>
<%@ taglib uri="/WEB-INF/struts-bean.tld"  prefix="bean" %>
<%@ taglib uri="/WEB-INF/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@page contentType="text/html" import="java.util.*" %>

<html>
<head>
<title>Favourite Report Date Range Selection</title>
<insta:link type="css" file="hmsNew.css" />
<insta:link type="css" file="widgets.css" />
<insta:link type="script" file="widgets.js"/>
<insta:link type="script" file="hmsvalidation.js"/>
<insta:link type="script" file="reports/std_report_builder.js" />
<c:set var="cpath" value="${pageContext.request.contextPath}"/>
<script>
	var reportID = '${ifn:cleanJavaScript(reportID)}';
	var NoneDateField = '${NoneDateField}';
	var allowDateChange = '${allowDateChange}';
	var trend = '${trend}';
	function onChangeofDaterange() {
		var dateRange = getDateRange();

		if (dateRange == "defaultReportDate") {
			document.getElementById("_sel").disabled = true;
			document.dateRangeform.fromDate.disabled = true;
			document.dateRangeform.toDate.disabled = true;
		}
		if (dateRange == "useOtherDate") {
			document.getElementById("_sel").disabled = false;
			document.dateRangeform.fromDate.disabled = false;
			document.dateRangeform.toDate.disabled = false;
			document.getElementById("_sel").value="pd";
			setDateRangeforSel();
		}
	}

	function getDateRange() {
		var dateRangeObj = document.getElementsByName('dateRange');
		for (var i=0; i<dateRangeObj.length; i++) {
			if (dateRangeObj[i].checked)
				return dateRangeObj[i].value;
		}
		return null;
	}

	function onSubmit(format) {
		if (format=='chart') {
			document.dateRangeform._method.value = 'getChart';
		} else {
			document.dateRangeform._method.value = 'runFavReport';
		}
		document.dateRangeform._informat.value = format;
		document.dateRangeform._myreport.value = reportID;
		return true;
	}

	function init() {
		var dateRangeObj = document.getElementsByName('dateRange');
		if(NoneDateField == 'true') {
			dateRangeObj[1].disabled = true;
			dateRangeObj[0].checked = true; dateRangeObj[1].checked = false;
		} 
		if(allowDateChange == 'true'){
			dateRangeObj[1].disabled = false;
		} else {
			dateRangeObj[1].disabled = true;
		}
		//disable trend button for other report types
		if(document.getElementById("chart")!= null)
		{
			if (trend == 'trend') {
				document.getElementById("chart").disabled = false;
			} else {
				document.getElementById("chart").disabled = true;
			}
		}
		onChangeofDaterange();
	}
</script>
</head>
<body onload="init();" class="yui-skin-sam">
	<form name="dateRangeform" action="FavouriteReportsDashboard.do"  method="GET">
	<h1>Favourite Report Date Range Selection</h1>
	<input type="hidden" name="_actionId" value="${actionId}" />
	<input type="hidden" name="_method" value="runFavReport">
	<input type="hidden" name=_informat value=""/>
	<input type="hidden" name="_myreport" value="${ifn:cleanHtmlAttribute(reportID)}"/>
	<input type="hidden" name="selDateRange" id="selDateRange" value="td"/>
	<fieldset class="fieldSetBorder"><legend class="fieldSetLabel">Date Range Selection</legend>
	<table style="border-top:medium none;">
		<tr>
			<td>
				<div class="sfField">
					<input type="radio" name="dateRange" value="defaultReportDate" checked onclick="onChangeofDaterange();"/>
					<label for="defaultReportDate">Use Default Report Date</label>
				</div>
			</td>
		</tr>
		<tr>
			<td>
				<div class="sfField">
					<input type="radio" name="dateRange" value="useOtherDate" onclick="onChangeofDaterange();"/>
					<label for="OtherDate">Use Other Date</label>
				</div>
			</td>
			<td style="border-right: medium none;">
				<div class="sfLabel">Date Range </div>
				<div class="sfField">
						<table class="search">
							<tr>
								<td width="100px">
									<select name="_sel" id="_sel" class="dropDown" onChange="setDateRangeforSel();">
										<option value="pd">Yesterday</option>
										<option value="td">Today</option>
										<option value="pm">Previous Month</option>
										<option value="tm">This Month</option>
										<option value="pf">Previous Financial Year</option>
										<option value="tf">This Financial Year</option>
										<option value="cstm">Custom Date</option>
									</select>
								</td>
								<td valign="top"  style="vertical-align: top; text-align:right; border-right: medium none;">
									From:
										<span style="text-align:left;" onclick="selectCustom();">
										<insta:datewidget name="fromDate" btnPos="left"/>
										</span>
									<br/>
									To:
										<span style="text-align:left;" onclick="selectCustom();">
										<insta:datewidget name="toDate" btnPos="left"/>
										</span>
								</td>
							</tr>
						</table>
				</div>
			</td>
		</tr>
	</table>
	</fieldset>
	<table align="left" style="margin-top: 0em;" style="valign:top;">
		<tr>
			<td style="padding-top:5px;">
				<table style="align:left;">
					<tr>
					<td><button type="submit" accesskey="P" onclick="return onSubmit('pdf')" title="PDF Report"><b><u>P</u></b>DF</button></td>
					<td><button type="submit" accesskey="C" onclick="return onSubmit('csv')" title="CSV Report"><b><u>C</u></b>SV</button></td>
					<td><button type="submit" accesskey="T" onclick="return onSubmit('text')" title="Text Report"><b><u>T</u></b>ext</button></td>
					<c:if test="${chartsActivated}">
						<td><button type="submit" accesskey="V" onclick="return onSubmit('chart')" id="chart" title="Graphical Report"><b><u>C</u></b>hart</button></td>
					</c:if>
					<td>&nbsp;|&nbsp;</td>
					<td><a href="${cpath}/reportdashboard/list.htm" target="_blank">Report Dashboard</a></td>
					<td>&nbsp;</td>
					</tr>
				</table>
			</td>
		</tr>
	</table>
	</form>
</body>
</html>

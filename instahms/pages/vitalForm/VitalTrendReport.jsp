<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>

<head>
	<title>Patient Vital Trend Report - Insta HMS</title>
	<meta http-equiv="Content-Type"  content="text/html;charset=iso-8859-1">
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>

	<script>
		function deselectAll(){
			document.forms[0].all.checked = false;
		}
		function selectVitalvalues(){
			var disable = document.forms[0].all.checked;
			var vitalValuesLen = document.forms[0].vitalValues.length;
			for (i=vitalValuesLen-1;i>=0;i--){
				document.forms[0].vitalValues[i].selected = disable;
			}
		}
		function getView() {
			document.forms[0].method.value = 'getView';
			document.forms[0].target = '';
			return true;
		}
		function getChart() {
			document.forms[0].method.value = 'getChart';
			document.forms[0].target = '_blank';
			return true;
		}
	</script>

	</head>

	<body onload="selectVitalvalues();">
		<div class="pageHeader">Patient Vital Trend Report </div>
		<insta:patientgeneraldetails  mrno="${param.mrno}" showClinicalInfo="true"/>
		<form action="VitalTrendReport.do" method="GET">
			<input type="hidden" name="method" value=""/>
			<input type="hidden" name="mrno" value="${ifn:cleanHtmlAttribute(param.mrno)}"/>

			<table align="center" class="search">
				<tr>
					<th>Vital Result Lables</th>
					<th>Dates</th>
				</tr>
				<tr>
					<td>
						<table>
							<tr>
								<td valign="top">
									<input type="checkbox" name="all" value="all" onclick="selectVitalvalues();" checked>All
								</td>
								<td>
									<select  name="vitalValues"  size = "5" onclick="deselectAll()" multiple>
										<c:forEach items="${vitalLabels}" var="vitalValue">
											<option value="${vitalValue.map.param_label}">${vitalValue.map.param_label}</option>
										</c:forEach>
									</select>
								</td>
							</tr>
						</table>
					</td>
					<td>
						<table>
							<tr>
								<td colspan="2">Select a date range (or select From and To dates manually)</td>
							</tr>
							<tr>
								<td valign="top">
									<input type="radio" id="p7" name="_sel" onclick="setDateRangePreviousNDays(fromDate, toDate, 7)">
									<label for="p7">Last 7 days</label>
									<br/>

								 	<input type="radio" id="p30" name="_sel" onclick="setDateRangePreviousNDays(fromDate, toDate, 30);">
									<label for="p30">Last 30 days</label>
									<br/>

									<input type="radio" id="p90" name="_sel" onclick="setDateRangePreviousNDays(fromDate, toDate, 90);">
									<label for="p90">Last 60 days</label>
									<br/>

									<input type="radio" id="p180" name="_sel" onclick="setDateRangePreviousNDays(fromDate, toDate, 180);">
									<label for="p180">Last 180 days</label>
									<br/>

									<input type="radio" id="py" name="_sel" onclick="setDateRangePreviousNDays(fromDate, toDate, 365);">
									<label for="py">Last 1 Year</label>
									<br/>
								</td>

								<td valign="top" style="padding-left: 2em">
									<table>
										<tr>
											<td align="right">From:</td>
											<td><insta:datewidget name="fromDate"/></td>
										</tr>
										<tr>
											<td align="right">To:</td>
											<td><insta:datewidget name="toDate"/></td>
										</tr>
									</table>
								</td>
							</tr>
						</table>
					</td>
				</tr>
			</table>

			<table align="center" style="margin-top: 1em;">
				<tr><td align="center">
					<input type="submit"  value="View" onclick="return getView();"/>
					<input type="submit"  value="Chart" onclick="return getChart();"/>
					</td>
				</tr>
			</table>


		</form>
	</body>
</html>

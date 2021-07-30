<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO" %>
<c:set var="maxCenters" value='<%= GenericPreferencesDAO.getPrefsBean().get("max_centers_inc_default") %>' scope="session"/>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<title>IP Stats Report - Insta HMS</title>

	<script>


		var fromDate, toDate,category ;

		function onInit() {
			fromDate = document.inputform.fromDate;
			toDate = document.inputform.toDate;
			document.getElementById("tyr").checked = true;
			setDateRangeYear(fromDate, toDate);
		}

			function validateForm(format){

					if (validateFromToDate(document.inputform.fromDate,document.inputform.toDate)) {
						if ( format == 'csv') {
							document.forms[0].method.value = 'getCsv';
							return true;
						}else {
							document.forms[0].method.value = 'getReport';
							return true;

						}
					}
				return false;
		}

	</script>
</head>
	<body onload="onInit();">
		<div class="pageHeader">Department wise(yearly) IP Statistics Report</div>
		<form name="inputform" method="GET" target="_blank">
			<input type="hidden" name="method" value="getReport">

			<div class="tipText">

			</div>

			<table align="center">
				<tr>
					<td  align="left">
						<input type="radio" id="pfy"
						name="_sel" onclick="setDateRangePreviousYear(fromDate, toDate)">
						<label for="pfy">Previous Year</label>
						<br/>

						<input type="radio" id="tyr" name="_sel" onclick="setDateRangeYear(fromDate, toDate)">
						<label for="tfy">This Year</label>
						<br/>
					</td>
					<td valign="top" style="padding-left: 2em; vertical-align: top">
						<table style="white-space:nowrap">
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
				<c:choose>
					<c:when test="${centerId == 0 && ((!empty maxCenters)&& maxCenters>1)}">
						<tr>
							<td align="right"><br />Center:&nbsp;</td>
							<td align="left">
								<br/>
								&nbsp;&nbsp;&nbsp;<insta:selectdb name="center" id="center" table="hospital_center_master" valuecol="center_id" displaycol="center_name" value="${centerId}"/>
							</td>
						</tr>
					</c:when>
					<c:otherwise>
							<input type="hidden"  name="center"  id="center" value="${centerId}"/>
					</c:otherwise>
				</c:choose>
			</table>

			<table align="center" style="margin-top: 1em">
				<tr>
					<td>
						<insta:selectoptions name="printerType" value="pdf" opvalues="pdf,text" optexts="PDF,TEXT" style="width: 5em" />
						<button type="submit" accesskey="G"
						onclick="return validateForm()"><b><u>G</u></b>enerate Report</button>
					</td>
					<td>
						<button type="submit" id="exportButton" accesskey="E" onclick="return validateForm('csv')">
						<b><u>E</u></b>xport to CSV</button>
					</td>
				</tr>
			</table>

		</form>
	</body>
</html>


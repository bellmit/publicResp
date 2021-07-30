<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO" %>
<c:set var="maxCenters" value='<%= GenericPreferencesDAO.getPrefsBean().get("max_centers_inc_default") %>' scope="session"/>
<c:set var="cpath" value="${pageContext.request.contextPath}" />

<head>
<title>Doctor Wise Prescribe vs Sold Report - Insta HMS</title>

</head>

<html>
	<body>
		<div class="pageHeader">Doctor Wise Prescribed vs Sold Report</div>
		<form name="inputform" method="GET" target="_blank" >
			<input type="hidden" name="report" value="DoctorWisePrescribeAndPurchaseReport">
			<input type="hidden" name="method" value=getReport />
			<div class="tipText">
				This report lists prescribe and sale by doctor wise in the given time period.

			</div>

			<table align="center">
				<tr>
					<td colspan="2">Select a date range for the report</td>
				</tr>
				<tr>
					<td valign="top">
						<input type="radio" id="today" name="selectDate" checked
								onclick="setDateRangeToday(document.inputform.fromDate, document.inputform.toDate)">
						<label for="today">Today</label>
						<br/>

						<input type="radio" id="week" name="selectDate"
								onclick="setDateRangeWeek(document.inputform.fromDate, document.inputform.toDate)">
						<label for="week">This week</label>
						<br/>

						<input type="radio" id="month" name="selectDate"
								onclick="setDateRangeMonth(document.inputform.fromDate, document.inputform.toDate)">
						<label for="month">This month</label>
						<br/>

						<input type="radio" id="year" name="selectDate"
								onclick="setDateRangeYear(document.inputform.fromDate, document.inputform.toDate)">
						<label for="year">This year</label>
						<br/>
					</td>

					<td valign="top" style="padding-left: 2em">
						<table>
							<tr>
								<td align="right">From:</td>
								<td><insta:datewidget name="fromDate" value="today"/></td>
							</tr>
							<tr>
								<td align="right">To:</td>
								<td><insta:datewidget name="toDate" value="today"/></td>
							</tr>
						</table>
					</td>
				</tr>
				<tr>
					<td colspan="2" align="center"><br/>Group by:&nbsp;&nbsp;
					<insta:selectdb name="docId" value="" table="doctors"
						valuecol="doctor_id" displaycol="doctor_name" filtered="false" dummyvalue="--select doctor--" orderby="doctor_name"/>
					</td>
				</tr>
				<c:choose>
					<c:when test="${centerId == 0 && ((!empty maxCenters)&& maxCenters>1)}">
						<tr>
							<td  colspan="2" align="center"><br/>Center:&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
								<insta:selectdb name="center" id="center" table="hospital_center_master" valuecol="center_id" displaycol="center_name" value="${centerId}"/>
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
					</td>
					<td>
						<input type="submit" value="Generate Report"
							onclick="return validateFromToDate(document.inputform.fromDate, document.inputform.toDate)">
					</td>
			</table>

		</form>
	</body>
</html>

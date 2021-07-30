<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO" %>
<c:set var="maxCenters" value='<%= GenericPreferencesDAO.getPrefsBean().get("max_centers_inc_default") %>' scope="session"/>
<c:set var="cpath" value="${pageContext.request.contextPath}" />

<head>
	<title>Tests Prescription Ordered Report - Insta HMS</title>

</head>

<html>
	<body>
		<div class="pageHeader">Doctor Wise Tests Prescription Ordered Report</div>
		<form name="inputform" method="GET" target="_blank"	action="TestsPrescriptionOrderedReport.do">
			<input type="hidden" name="report" value="TestsPrescriptionOrderedReport"/>
			<input type="hidden" name="method" value="getReport" />
			<div class="tipText">
				This report lists test prescriptions ordered by doctor wise in the given time period.
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
					<td colspan="2" style="padding-top: 8px">Select a Doctor for the report</td>
				</tr>
				<tr>
					<td style="padding-left: 4px">Doctor Name:</td>
					<td>
                    	<insta:selectdb  name="doctorId" value="" table="doctors" valuecol="doctor_id"
                    			displaycol="doctor_name"   dummyvalue="----(All)-----" orderby="doctor_name" />
                    	<span class="star">*</span></td>
                    </td>
				</tr>
				<c:choose>
					<c:when test="${centerId == 0 && ((!empty maxCenters)&& maxCenters>1)}">
						<tr>
							<td style="padding-left: 4px"><br />Center:&nbsp;</td>
							<td >
								<br />
								<insta:selectdb name="center" id="center" table="hospital_center_master" valuecol="center_id" displaycol="center_name" />
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
							<button type="submit" accesskey="G"
								onclick="return validateFromToDate(document.inputform.fromDate, document.inputform.toDate);">
								<b><u>G</u></b>enerate Report</button>
						</td>
				  </tr>
			</table>

		</form>
	</body>
</html>


<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

<html>
	<head>
		<insta:link type="css" file="widget.css"/>
		<insta:link type="script" file="widget.js"/>
		<insta:link type="script" file="hmsvalidation.js"/>

		<script type="text/javascript">
			var fromDate, toDate;
			function init() {
					fromDate = document.getElementById("fromDate");
					toDate = document.getElementById("toDate");
					setDateRangeToday(fromDate, toDate);
			}
		</script>
	</head>
	<body onload="init()">
		<form action="${pageContext.request.contextPath}/pages/Reports/dietary/DietaryReport.do" target="_blank">
			<input type="hidden" name="method" value="undeliveredMealsReport">
			<table align="center" >
				<tr>
					<td class="pageHeader" colspan="4">Undelivered Meals Report </td>
				</tr>
				<tr>
					<td colspan="2">
						<div class="tiptext">
							This report gives detailed list of all meals which are need to be delivered between the given dates.
							The report is filtered on ward names.
							The report displays MR no, Patient Visit Id, Patient Name,Bed Name,Meal Timing and Meal Name.
						</div>
					</td>
				</tr>
				<tr>
					<td colspan="2" style="padding-left: 6em;">Select a date range (or select From and To dates manually)</td>
				</tr>
				<tr>
					<td valign="top" style="padding-left: 6em;">
						<input checked type="radio" id="td" name="date" onclick="setDateRangeToday(fromDate, toDate)">
						<label for="td">Today</label>
						<br/>

						<input  type="radio" id="tm" name="date" onclick="setDateRangeTomorrow(fromDate, toDate)">
						<label for="tm">Tomorrow</label>
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
				<tr><td></td></tr>
				<tr>
					<td colspan="2" style="padding-left: 6em;">
						Select ward name :

						<insta:selectdb name="wardnameArray" dummyvalue="All" table="ward_names" valuecol="ward_no"
							displaycol="ward_name" orderby="ward_name" filtered="false"/>
					</td>
				</tr>
				<tr>
					<td colspan="2" style="padding-left: 6em;">
						Select meal name :
						<insta:selectdb name="mealnameArray" dummyvalue="All" table="diet_master" valuecol="meal_name"
							displaycol="meal_name" orderby="meal_name"  filtered="false"/>
					</td>
				</tr>
				<tr><td>&nbsp;&nbsp;&nbsp;&nbsp;</td></tr>
				<tr align="center">
					<td colspan="2">
						<input type="submit" name="GetReport" value="Generate Report">
					</td>
				</tr>
			</table>
		</form>
	</body>
</html>
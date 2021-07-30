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
					setDateRangeYesterday(fromDate, toDate);
			}

		</script>
	</head>
	<body onload="init()">
		<form action="${pageContext.request.contextPath}/report/DietaryReport.do" target="_blank">
			<input type="hidden" name="method" value="getReport">
			<table align="center">
				<tr>
					<td class="pageHeader" colspan="4">	Dietary Revenue Report </td>
				</tr>
				<tr>
					<td>
						<div class="tiptext">
							This report gives detailed list of all revenues posted for meals rendered between the given dates.
							The report is filtered on ward names.
							The report displays MR no, Patient Visit Id, Patient Name,Bed Name,Meal Name,Meal Timing and Amount.
						</div>
					</td>
				</tr>

				<tr align="center">
					<td>
					<jsp:include page="/pages/Common/DateRangeSelector.jsp">
						<jsp:param name="skipWeek" value="Y"/>
					</jsp:include>
					</td>
				</tr>
				<tr><td ></td></tr>
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
					<td>
						<input type="submit" name="GetReport" value="Generate Report">
					</td>
				</tr>
			</table>
		</form>
	</body>
</html>
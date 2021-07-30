<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO" %>
<c:set var="maxCenters" value='<%= GenericPreferencesDAO.getPrefsBean().get("max_centers_inc_default") %>' scope="session"/>

<html>
	<head>
		<insta:link type="css" file="widget.css"/>
		<insta:link type="script" file="widgets.js"/>
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
			<input type="hidden" name="method" value="toBeDeliveredMealsReport">
			<table align="center" >
				<tr>
					<td class="pageHeader" colspan="4">To Be Delivered Meals Report </td>
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

						<select name="wardnameArray" id="wardnameArray" class="dropdown">
							<option value="">...Ward...</option>
							<c:forEach items="${wards }" var="ward">
								<option value="${ward.map.ward_no }">${ward.map.ward_name }</option>
							</c:forEach>
						</select>
					</td>
				</tr>
				<tr>
					<td colspan="2" style="padding-left: 6em;">
						Select meal name :
						<insta:selectdb name="mealnameArray" dummyvalue="All" table="diet_master" valuecol="meal_name"
							displaycol="meal_name" orderby="meal_name"  filtered="false"/>
					</td>
				</tr>
				<c:choose>
					<c:when test="${centerId == 0 && ((!empty maxCenters)&& maxCenters>1)}">
						<tr>
							<td colspan="2" style="padding-left: 11em;"><br />Center&nbsp;:&nbsp;
								<insta:selectdb name="center" id="center" table="hospital_center_master" valuecol="center_id" displaycol="center_name" value="${centerId}"/>
							</td>
						</tr>
					</c:when>
					<c:otherwise>
							<input type="hidden"  name="center"  id="center" value="${centerId}"/>
					</c:otherwise>
				</c:choose>
				<tr><td>&nbsp;&nbsp;&nbsp;&nbsp;</td></tr>
				<tr align="center">
					<td colspan="2">
						<button type="submit" name="GetReport" accesskey="G"><b><u>G</u></b>enerate Report</button>
					</td>
				</tr>
			</table>
		</form>
	</body>
</html>
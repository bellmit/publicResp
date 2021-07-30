<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@ page import="java.util.*,java.text.*" %>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<%@ page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO" %>
<c:set var="maxCenters" value='<%= GenericPreferencesDAO.getPrefsBean().get("max_centers_inc_default") %>' scope="session"/>

<head>
	<title>Financial Dashboard - Insta HMS</title>
	<script>
		var fromDate, toDate;

		function onInit() {
			setSelDateRange();
			fromDate = document.inputform.fromDate;
			toDate = document.inputform.toDate;
		}

		function onSubmit(option) {
			document.inputform.format.value = option;
			if (option == 'pdf')
				document.inputform.target = "_blank";
			else
				document.inputform.target = "";
			return validateFromToDate(fromDate, toDate);
		}

		function doHelp(a) {
			var helpWin=window.open(a.href, 'Help on Consolidated Financial Dashboard',
					'height=700,width=800,resizable=yes,scrollbars=yes,status=no');
			helpWin.focus();
			return false;
		}
	</script>
</head>

<html>
<body onload="onInit()">
<div class="pageHeader">Consolidated Financial Dashboard</div>
<form name="inputform" method="GET"
	action="${cpath}/billing/CFDReport.do"><input type="hidden"
	name="method" value="dashboardReport"> <input type="hidden"
	name="format" value="screen">

<div class="tipText">This report/dashboard gives you multiple
tabular summaries of various key financial parameters, including counts,
revenues and collections within the given period.<br />
</div>

<table align="center">
	<jsp:include page="/pages/Common/DateRangeSelector.jsp">
		<jsp:param name="addTable" value="N" />
		<jsp:param name="skipWeek" value="Y" />
	</jsp:include>
	<tr>
		<td></td>
	</tr>
	<tr>
		<td align="right">Revenue By: <br />
		</td>
		<td><select name="dateField">
			<option value="finalized_date">Finalized Date</option>
			<option value="posted_date">Charge Posted Date</option>
		</select> <br />
		</td>
	</tr>
	<tr>
		<td align="right">Include Account Groups:</td>
		<td>
			<insta:selectdb name="accountGroup" table="account_group_master" valuecol="account_group_id" displaycol="account_group_name" dummyvalue="(All)" dummyvalueId="0"/>
		</td>
	</tr>
	<c:choose>
	<c:when test="${centerId == 0 && ((!empty maxCenters)&& maxCenters>1)}">
		<tr>
			<td align="right">Center:</td>
			<td>
				<insta:selectdb name="centerFilter" id="centerFilter" table="hospital_center_master" valuecol="center_id" displaycol="center_name"
				 value="${centerId}"/>
			</td>
		</tr>
	</c:when>
	<c:otherwise>
			<input type="hidden"  name="centerFilter" id="centerFilter"  value="${centerId}"/>
	</c:otherwise>
	</c:choose>
	<tr>
		<td align="right">
			PDF Font size: 
		</td>
		<td>
			<select name="baseFontSize" style="width:4em">
				<option>5pt</option>
				<option>6pt</option>
				<option>7pt</option>
				<option>8pt</option>
				<option>9pt</option>
				<option selected>10pt</option>
				<option>11pt</option>
				<option>12pt</option>
				<option>14pt</option>
				<option>16pt</option>
				<option>18pt</option>
				<option>20pt</option>
			</select>
		</td>
	</tr>
</table>

<table align="center" style="margin-top: 1em">
	<tr>
		<td><button type="submit" accesskey="V"
			onclick="return onSubmit('screen')"><b><u>V</u></b>iew</button></td>
		<td><button type="submit" accesskey="P"
			onclick="return onSubmit('pdf')"><b><u>P</u></b>rint</button></td>
		<td><a
			href="${cpath}/billing/RevenueReportPopup.do?method=getHelpPage"
			onclick="return doHelp(this)">Help</a></td>
	</tr>
</table>

</form>
</body>
</html>



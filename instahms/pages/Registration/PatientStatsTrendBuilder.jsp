<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO"%>
<c:set var="maxCenters" value='<%= GenericPreferencesDAO.getPrefsBean().get("max_centers_inc_default") %>' scope="session"/>

<c:set var="cpath" value="${pageContext.request.contextPath}" />

<head>
	<title>Patient Stats Trends - Insta HMS</title>
	<script>
		var centerId = ${centerId};
		function onInit() {
			document.getElementById('tfy').checked = true;
			setDateRangeFinancialYear(document.inputform.fromDate, document.inputform.toDate);
			document.inputform.trendPeriod.selectedIndex = 2;
		}
		function onSubmit(option) {
			var centerDisplayName="";
			for(i =0; i<document.forms[0].centerFilter.length;i++){
				if(document.forms[0].centerFilter.options[i].selected){
					centerDisplayName = document.forms[0].centerFilter.options[i].text;
				}
			}
			document.getElementById("centerName").value = centerDisplayName;
			setCenterClause();
			document.inputform.format.value = option;
			var sel = document.inputform.groupBy;
			document.inputform.groupByName.value = sel.options[sel.selectedIndex].text;
			if (option == 'pdf')
				document.inputform.target = "_blank";
			return validateFromToDate(document.inputform.fromDate, document.inputform.toDate);
		}

		function setCenterClause() {
			var selectedCenter = document.getElementById("centerFilter").value;
			if ( selectedCenter == 0 ) {
				document.getElementById("centerClause").value = ( " AND center_id = "+selectedCenter );
			} else {
				document.getElementById("centerClause").value = ( " AND center_id = "+selectedCenter );
			}
		}
	</script>
</head>

<html>
	<body onload="onInit()">
		<div class="pageHeader">Patient Stats Trend Report</div>
		<form name="inputform" method="GET" action="${cpath}/registration/PatientStatsTrend.do">
			<input type="hidden" name="method" value="getReport">
			<input type="hidden" name="format" value="screen">
			<input type="hidden" name="groupByName" value="">
			<input type="hidden" name="centerName" id="centerName" value="" />
			<input type="hidden" name="center_id" id="center_id" value="${centerId }"/>
			<input type="hidden" name="centerClause" id="centerClause" value=""/>

			<div class="tipText">
				This report gives you a trend of the patient count between two dates, organized
				by a grouping of your choice.
			</div>

			<jsp:include page="/pages/Common/DateRangeSelector.jsp">
				<jsp:param name="skipDay" value="Y"/>
				<jsp:param name="skipWeek" value="Y"/>
			</jsp:include>

			<table align="center">
				<tr>
					<td align="right">Trend Type:</td>
					<td>
						<select name="trendPeriod">
							<option value="day" selected>Daily</option>
							<option value="week">Weekly</option>
							<option value="month">Monthly</option>
						</select>
					</td>
				</tr>

				<tr>
					<td align="right">Group By:</td>
					<td>
						<select name="groupBy">
							<option value="visit_type_name">Patient Type</option>
							<option value="dept_name">Department</option>
							<option value="unit_name">Unit</option>
							<option value="doctor_name">Doctor</option>
							<option value="referer">Referer</option>
							<option value="ward_name">Ward</option>
							<option value="tpa_name">Sponsor</option>
							<option value="org_name">Rate Plan</option>
							<option value="area">Area</option>
						</select>
					</td>
				</tr>
				<c:choose>
					<c:when test="${centerId == 0 && ((!empty maxCenters)&& maxCenters>1)}">
					<tr>
						<td align="right">Center Name:</td>
						<td>
							<insta:selectdb name="centerFilter" id="centerFilter" table="hospital_center_master"
							valuecol="center_id" displaycol="center_name" value="${centerId}" />
						</td>
					</tr>
					</c:when>
					<c:otherwise>
						<input type="hidden"  name="centerFilter" id="centerFilter"  value="${centerId}"/>
					</c:otherwise>
				</c:choose>
			</table>

			<table align="center" style="margin-top: 1em">
				<tr>
						<td>
							<button type="submit" accesskey="V" onclick="return onSubmit('screen')"><b><u>V</u></b>iew</button>
						</td>
						<td>
							<button type="submit" accesskey="P" onclick="return onSubmit('pdf')"><b><u>P</u></b>rint</button>
						</td>
				  </tr>
			</table>

		</form>
	</body>
</html>



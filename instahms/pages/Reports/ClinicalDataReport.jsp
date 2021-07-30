<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<%@ page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO" %>
<c:set var="maxCenters" value='<%= GenericPreferencesDAO.getPrefsBean().get("max_centers_inc_default") %>' scope="session"/>

<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>
<head>
<title>Clinical Outcomes Report- Insta HMS</title>
<jsp:useBean id="currentDate" class="java.util.Date"/>
<c:set var="valueDate">
	<fmt:formatDate pattern="dd-MM-yyyy  HH:mm" value="${currentDate}"/>
</c:set>
<script>
	function onInit() {
		document.getElementById('pd').checked = true;
		setDateRangeYesterday(document.inputform.fromDate,document.inputform.toDate);
	}

	function validateForm(format){
		document.inputform.format.value=format;
		if (validateFromToDate(document.inputform.fromDate, document.inputform.toDate)) {
				return true;
		}
		return false;
	}
</script>
</head>

<body onload="onInit();">
	<div class="pageHeader">Clinical Outcomes Report</div>
	<form name="inputform" method="GET" target="_blank">
		<input type="hidden" name="_method" value="getReport">
		<input type="hidden" name="format" value="pdf">
		<input type="hidden"  name="currDateTime"  id="currDateTime" value="${valueDate}"/>

		<div class="helpPanel">
			This report shows a summary of various clinical data analyses.
		</div>

		<jsp:include page="/pages/Common/DateRangeSelector.jsp">
			<jsp:param name="skipWeek" value="Y"/>
		</jsp:include>
		<br/><br/>
		<c:choose>
			<c:when test="${centerId == 0 && ((!empty maxCenters)&& maxCenters>1)}">
				<table>
					<tr>
						<td align="right">Center:</td>
						<td>
							<insta:selectdb name="centerId" id="centerId" table="hospital_center_master" valuecol="center_id" displaycol="center_name" />
						</td>
					</tr>
				</table>
			</c:when>
			<c:otherwise>
					<input type="hidden"  name="centerId"  id="centerId" value="${centerId}"/>
			</c:otherwise>
		</c:choose>
		<br/>
		<div style="margin-top: 10px">
			<button type="submit" onclick="return validateForm('screen')">View</button>
			<button type="submit" onclick="return validateForm('pdf')">Print</button>
		</div>
	</form>
</body>
</html>


<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ page import="com.insta.hms.master.GenericPreferences.GenericPreferencesDAO" %>
<c:set var="maxCenters" value='<%= GenericPreferencesDAO.getPrefsBean().get("max_centers_inc_default") %>' scope="session"/>
<c:set var="cpath" value="${pageContext.request.contextPath}" />
<html>

<head>
	<title> Internal Trans. Revenue Report - Insta HMS</title>
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="script" file="widgets.js"/>
	<script>
		function init() {
			document.getElementById('pd').checked = true;
			setDateRangeYesterday(document.inputform.fromDate, document.inputform.toDate);
		}
		function validateFields() {
			if(!validateFromToDate(document.inputform.fromDate, document.inputform.toDate)) return false;
			return true;
		}
	</script>
</head>
<body onload="init();">
		<div class="pageHeader">Internal Transfer Revenue Report</div>
		<form name="inputform" method="GET" target="_blank">
			<input type="hidden" name="method" value="getReport"/>
			<div class="tipText">
				This report shows all charge items posted to a finalized Hospital bill,
				which have the account group different from the corresponding bill's account group.
			</div>
			<table align="center" style="margin-top: 1em">
			<tr>
				<td>
				<jsp:include page="/pages/Common/DateRangeSelector.jsp">
					<jsp:param name="addTable" value="N"/>
					<jsp:param name="skipWeek" value="Y"/>
				</jsp:include>
				</td>
			</tr>
			<c:choose>
				<c:when test="${centerId == 0 && ((!empty maxCenters)&& maxCenters>1)}">
					<tr>
						<td align="left"><br />Center:&nbsp;
							<insta:selectdb name="center" id="center" table="hospital_center_master" valuecol="center_id" displaycol="center_name" value="${centerId}"/>
						</td>
					</tr>
				</c:when>
				<c:otherwise>
						<input type="hidden"  name="center"  id="center" value="${centerId}"/>
				</c:otherwise>
			</c:choose>
			</table>
			<table align="center" style="margin-top: 2em">
				<tr >
					<td>
						<insta:selectoptions name="printerType" value="pdf" opvalues="pdf,text" optexts="PDF,TEXT" style="width: 5em" />
						<button type="submit" accesskey="G"
							onclick="return validateFields();"><b><u>G</u></b>enerate Report</button>
					</td>
			  </tr>
			</table>

		</form>
	</body>
</html>

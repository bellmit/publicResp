<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>
<%@taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<html>

<head>
	<title>Patient Vital Trend Report - Insta HMS</title>
	<meta http-equiv="Content-Type"  content="text/html;charset=iso-8859-1">
	<insta:link type="css" file="widgets.css"/>
	<insta:link type="js" file="widgets.js"/>

	<script>

		function getChart() {
			document.forms[0].method.value = 'getChart';
			document.forms[0].target = '_blank';
			return true;
		}
	</script>

	</head>

	<body>
		<div class="pageHeader">Vital Trend Report </div>
		<insta:patientgeneraldetails  mrno="${param.mrno}"/>
		<form action="VitalTrendReport.do" method="GET">
		<input type="hidden" name="method" value=""/>
		<c:forEach items="${paramValues.vitalValues}" var="vital">
			<input type="hidden" name="vitalValues" value="${vital}"/>
		</c:forEach>
		<input type="hidden" name="mrno" value="${ifn:cleanHtmlAttribute(param.mrno)}"/>
		<input type="hidden" name="fromDate" value="${ifn:cleanHtmlAttribute(param.fromDate)}"/>
		<input type="hidden" name="toDate" value="${ifn:cleanHtmlAttribute(param.toDate)}"/>
			<c:if test="${empty vitalLabels}" ><p class="info"><b>No Data Found</b></p> </c:if>
			<c:if test="${not empty vitalLabels}" >
				<table align="center" class="dashboard" >
					<tr>	<th></th>
						<c:forEach items="${vitalDates}" var="date">
							<th><fmt:formatDate value="${date}" pattern="dd-MM-yy HH:mm"/></th>
						</c:forEach>
					</tr>
					<c:forEach items="${vitalLabels}" var="label" >
						<tr>
							<th>${ifn:cleanHtml(label)}</th>
								<c:forEach items="${vitalDates}" var="date">
										<td>${vitalValuesMap[label][date].map["param_value"] } ${testValuesMap[label][date].map["param_uom"]}</td>
								</c:forEach>
						</tr>
					</c:forEach>
				</table>


				<table align="center" style="margin-top: 1em;">
					<tr><td align="center"><input type="submit"  value="Chart" onclick="return getChart();"/></td></tr>
				</table>
			</c:if>


		</form>
	</body>
</html>
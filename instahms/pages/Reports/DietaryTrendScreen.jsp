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
			function onSubmit(option) {
				document.forms[0].format.value = option;
				if (option == 'pdf'){
					document.forms[0].target = "_blank";
				}else {
					document.forms[0].target = "";
				}
				return validateFromToDate(document.getElementById("fromDate"), document.getElementById("toDate"));
			}
		</script>
	</head>
	<body onload="init()">
		<form action="${pageContext.request.contextPath}/pages/Reports/dietary/DietaryTrendReport.do" >
			<input type="hidden" name="method" value="getTrendReport">
			<input type="hidden" name="format" value="screen">
			<table align="center">
				<tr>
					<td class="pageHeader" colspan="4">	Dietary Trend Report </td>
				</tr>
				<tr>
					<td>
						<div class="tipText">
								This report shows a trend, of the meal count and revenue between the given date range.
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
			</table>

			<table align="center" style="margin-top: 1em">
				<tr>
						<td>
							<input type="submit" value="View" onclick="return onSubmit('screen')">
						</td>
						<td>
							<input type="submit" value="Print" onclick="return onSubmit('pdf')">
						</td>
				  </tr>
			</table>
		</form>
	</body>
</html>
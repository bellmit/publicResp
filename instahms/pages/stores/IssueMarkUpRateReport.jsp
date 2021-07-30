<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>

<c:set var="cpath" value="${pageContext.request.contextPath}" />

<head>
<title>Stores Markup Rates - Insta HMS</title>

</head>

<html>
	<body onload="setDateRangeYesterday(document.forms[0].fromDate, document.forms[0].toDate);">
		<div class="pageHeader">Stores Markup Rates Report</div>
		<form name="inputform" method="GET" target="_blank"	>
			<input type="hidden" name="report" value="storeendreport">
			<input type="hidden" name="method" value="getReport">

			<div class="tipText">
				This report lists issue markup rates.

			</div>



			<table align="center" style="margin-top: 1em">
				<tr>
					<td>
						<insta:selectoptions name="printerType" value="pdf" opvalues="pdf,text" optexts="PDF,TEXT" style="width: 5em" />
					</td>
					<td>
						<button type="submit" accesskey="G"
							onclick="return validateFromToDate(document.forms[0].fromDate, document.forms[0].toDate)">
							   <b><u>G</u></b>enerate Report</button>
					</td>
			</table>

		</form>
	</body>
</html>


<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>

<c:set var="cpath" value="${pageContext.request.contextPath}" />
<head>
<title>Stock Movement Report - Insta HMS</title>



</head>
<html>
	<body onload="setDateRangeYesterday(fromDate, toDate);">
		<div class="pageHeader">Stock Movement Report</div>
		<form name="inputform" method="GET" target="_blank">
			<input type="hidden" name="report" value="PharmacyStockMovement">
			<input type="hidden" name="method" value="getReport">

			<div class="tipText">
				This report lists the total quantity of each medicine that come into or gone out of each
				store, with a brief description of how (eg, via Purchases or Issues or sales etc.).
				The report is grouped by Stores.
			</div>

			<table align="center">
				<tr>
					<td colspan="2">
						<br/>
						<jsp:include page="/pages/Common/DateRangeSelector.jsp">
							<jsp:param name="addTable" value="N" />
							<jsp:param name="skipWeek" value="Y" />
						</jsp:include>
					</td>
				</tr>
			</table>

			<table align="center">
				<br></br>
			    <tr>
			    	<td> Store Name: </td><td></td>
					<td><insta:selectdb id="store_id" name="store_id" table="stores"  value="${pharmacyStoreId}" valuecol="dept_id" displaycol="dept_name" /></td>
				</tr>
			</table>

			<table align="center" style="margin-top: 1em">
				<tr>
					<td>
						<insta:selectoptions name="printerType" value="pdf" opvalues="pdf,text" optexts="PDF,TEXT" style="width: 5em" />
					</td>
					<td>
						<input type="submit" value="Generate Report"
							onclick="return validateFromToDate(document.inputform.fromDate, document.inputform.toDate)">
					</td>
				</td>
			</table>

		</form>
	</body>
</html>


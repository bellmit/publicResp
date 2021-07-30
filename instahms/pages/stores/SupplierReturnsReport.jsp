<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@page import="com.insta.hms.stores.PharmacymasterDAO"%>
<c:set var="cpath" value="${pageContext.request.contextPath}" />

<head>
<title>Supplier Returns Report - Insta HMS</title>



	<script>
		function onSubmit(method) {
			var valid = validateFromToDate(document.inputform.fromDate, document.inputform.toDate);
			if (!valid)
				return false;
			document.inputform.method.value = method;
			document.inputform.submit();
		}
	</script>
</head>

<html>
	<body onload="setDateRangeYesterday(fromDate, toDate);">
		<div class="pageHeader">Supplier Returns Report</div>
		<form name="inputform" method="GET" target="_blank">
			<input type="hidden" name="method" value="getReport">

			<div class="tipText">
				This report lists  Supplier wise Returns in the given time period.

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

				<tr>
					<td colspan="2" style="padding-top: 8px"><br/>Select a supplier for the report</td>
				</tr>
				<tr>
					<td style="padding-left: 4px">Supplier Name:</td>
					<td>
						<select name="supplier_id">
							<option value="">(All)</option>
							<c:forEach var="supplier" items="<%= PharmacymasterDAO.getAllSuppliers() %>">
								<option value="${supplier.SUPPLIER_CODE}">${supplier.SUPPLIER_NAME}</option>
							</c:forEach>
						</select>
					</td>
				</tr>
			</table>

		 <table align="center" style="margin-top: 1em">
				<tr>
					<td>
						<insta:selectoptions name="printerType" value="pdf" opvalues="pdf,text" optexts="PDF,TEXT" style="width: 5em" />
					</td>
					<td>
						<button type="button" accesskey="G"
							onclick="return onSubmit('getReport')"><b><u>G</u></b>enerate Report</button>
					</td>
					<td>
						<button type="button" accesskey="E" onclick="onSubmit('getCsv')"><b><u>E</u></b>xport to CSV</button>
					</td>
					</tr>
			</table>


		</form>
	</body>
</html>


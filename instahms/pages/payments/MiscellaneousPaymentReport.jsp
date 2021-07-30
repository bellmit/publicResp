<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>

<c:set var="cpath" value="${pageContext.request.contextPath}" />

<head>
	<title>Miscellaneous Payments Report - Insta HMS</title>
	<script>
		function onInit() {
			document.getElementById('pd').checked = true;
			setDateRangeYesterday(document.inputform.fromDate, document.inputform.toDate);
		}

		function onSubmit(option) {
			document.inputform.format.value = option;
			if (option == 'pdf') {
				if ( document.inputform.printerType.value == 'text' )
					document.inputform.method.value = 'getText';
				else
					document.inputform.method.value = 'printMiscellaneousPayments';
				document.inputform.target = "_blank";
			}
			else
				document.inputform.target = "";
			return validateFromToDate(fromDate, toDate);
		}
	</script>
</head>

<html>
	<body onload="onInit()">
		<div class="pageHeader">Miscellaneous Payments Report</div>
		<form name="inputform" method="GET" action="${cpath}/payment/PaymentReports.do">
			<input type="hidden" name="method" value="printMiscellaneousPayments">
			<input type="hidden" name="methodPdf" value="printMiscellaneousPayments">
			<input type="hidden" name="format" value="screen">
			<input type="hidden" name="docFilter" value="all">
			<c:set var="payee" value="${requestScope.payeeList}"/>

			<div class="tipText">
				This report gives you a tabular summary of the Doctor Payments between two dates.
			</div>

			<jsp:include page="/pages/Common/DateRangeSelector.jsp">
				<jsp:param name="skipWeek" value="Y"/>
			</jsp:include>

			<table align="center">
				<tr>
					<td>Payee Name:</td>
					<td>
						<table id="docTable" align="center">
							<tr>
								<td>
									<select name="payeeId">
										<option value="All">All Payees</option>
										<c:forEach var="p" items="${payee}">
											<option value="${p.PAYEE_NAME}">${p.PAYEE_NAME}</option>
										</c:forEach>
									</select>
								</td>
							</tr>
						</table>
					</td>
				</tr>
			</table>

			<table align="center" style="margin-top: 1em">
				<tr>
					<td>
						<insta:selectoptions name="printerType" value="pdf" opvalues="pdf,text" optexts="PDF,TEXT" style="width: 5em" />
						<button type="submit" accesskey="P" onclick="return onSubmit('pdf')"><b><u>P</u></b>rint</button>
					</td>
				</tr>
			</table>

		</form>
	</body>
</html>



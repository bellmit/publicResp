<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta" %>

<c:set var="cpath" value="${pageContext.request.contextPath}" />

<head>
	<title>Payments Before Insurance - Insta HMS</title>
	<insta:link type="script" file="/payments/paymentinsurance.js"/>
</head>

<html>
	<body onload="getPayeeType();init()">
		<div class="pageHeader">Payments Before Insurance Amount Received Report</div>
		<form name="inputform" method="GET" action="${cpath}/payment/PaymentsToInsurance.do"/>
			<input type="hidden" name="method" value="getPaymentToInsuranceReport">
			<input type="hidden" name="methodPdf" value="getPaymentToInsuranceReport">
			<input type="hidden" name="format" value="screen">
			<input type="hidden" name="docFilter" value="all">
			<input type="hidden" id="payeeTypeValues" name="payeeTypeValues" value="">
			<c:set var="payee" value="${requestScope.payeeList}"/>

			<div class="tipText">
				This report gives the conducting doctor / prescribing doctor / referral payments which were made during the selected date range, before receiving the insurance claim amount from TPA.
			</div>

			<jsp:include page="/pages/Common/DateRangeSelector.jsp">
				<jsp:param name="skipWeek" value="Y"/>
			</jsp:include>

			<table align="center">
				<tr>
					<td>Payment Type:</td>
					<td>
						<table id="paymentTable" align="center">
							<tr>
								<td>
									<input type="checkbox" name="payeeType" id="docPayee" value="'D'"  checked="checked"
									onclick="getPayeeType();"/>
									<label>Conducting Doctor </label>
									<input type="checkbox" name="payeeType" id="refPayee" value="'R','F'"  checked="checked"
									onclick="getPayeeType();"/>
									<label>Referral Doctor</label>
									<input type="checkbox" name="payeeType" id="presPayee" value="'P'"  checked="checked"
									onclick="getPayeeType();"/>
									<label>Prescribing Doctor</label>
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



<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Ref Payments - Insta HMS</title>

	<style type="text/css">
		.status_InActive{background-color: #E4C89C}
	</style>
</head>
<body>
	<form method="POST" action="ReferralPaymentPreferences.do">
		<input type="hidden" name="method" value=""/>
		<div class="pageHeader">Referral Payment Preferences </div>
			<table class="dashboard" align="center">
					<tr>
						<th>Charge Group Name</th>
						<th>Percent / Amount</th>
						<th>Amount</th>
						<th>Status</th>
					</tr>
					<c:forEach var="ref" items="${chargeGroup}" >
					<tr>
						<c:url var="Eurl" value="ReferralPaymentPreferences.do">
							<c:param name="method" value="show"/>
							<c:param name="chargegroup_id" value="${ref.CHARGEGROUP_ID}"/>
							</c:url>
							<c:if test="${ref.CHARGEGROUP_ID!='ITE'}" >
							<td><a href="${Eurl}">${ref.CHARGEGROUP_NAME}</a>
							</td>

						<td>${ref.PAY == 'Y'?'Percent':'Amount'}</td>
						<td>${ref.AMOUNT}</td>
						<td>${ref.STATUS}</td>
							</c:if>
					</tr>
					</c:forEach>
		</table>
</form>
</body>
</html>

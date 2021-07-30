<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib tagdir="/WEB-INF/tags" prefix="insta"%>
<%@page contentType="text/html" isELIgnored="false" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>

<html>
	<head>
		<title><insta:ltext key="billing.changebillprimary.confirmation.title"/></title>
		<script>
			function handleCancelAction() {
				var opener = window.opener;
				if (opener) {
					opener.handleSubPageCancelActionNewFlow(window);
				}
			}
		</script>
		<c:if test="${isNewUX == 'Y' || isNewUXSuccess == 'Y'}">
			<script>
				function newUXFlowButtonClickHandler() {
					const opener = window.opener;
					if (opener) {
						opener.handleSubPageConfirmActionNewFlow(window);
					}
				}
			</script>
		</c:if>
		<c:if test="${isNewUXSuccess == 'Y'}">
			<script>
				newUXFlowButtonClickHandler();
			</script>
		</c:if>
		<meta http-equiv="Content-type" content="text/html;charset=iso-8859-1" />
		<meta name="i18nSupport" content="true"/>
	</head>

	<body>
	<c:set var="primaryText">
		<insta:ltext key="billing.changebillprimary.confirmation.primary"/>
	</c:set>
	<c:set var="secondaryText">
		<insta:ltext key="billing.changebillprimary.confirmation.secondary"/>
	</c:set>
	<c:set var="okButton">
		<insta:ltext key="billing.changebilltype.confirmation.ok"/>
	</c:set>
	<c:set var="cancelButton">
		<insta:ltext key="billing.changebilltype.confirmation.cancel"/>
	</c:set>
		<insta:feedback-panel/>

		<c:choose><c:when test="${empty error}">
			<form method="POST" action="ChangeBillPrimary.do">
				<input type="hidden" name="_method" value="changePrimaryBill"/>
				<input type="hidden" name="billNo" value="${ifn:cleanHtmlAttribute(param.billNo)}" />
				<input type="hidden" name="primary" value="${bill.is_primary_bill == 'Y' ? 'N' : 'Y'}" />
				<input type="hidden" name="isNewUX" value="${ifn:cleanHtmlAttribute(param.isNewUX)}" />
				<table align="center" style="margin-top: 2em">
					<tr>
						<td>
							<insta:ltext key="billing.changebillprimary.confirmation.confirmationtemplate"/> ${ifn:cleanHtml(param.billNo)} <insta:ltext key="billing.changebillprimary.confirmation.from"/>
							${bill.is_primary_bill == 'Y'? primaryText : secondaryText} <insta:ltext key="billing.changebillprimary.confirmation.to"/>
							<b>${bill.is_primary_bill =='Y'? secondaryText : primaryText}?</b>
						</td>
					</tr>
				</table>
				<table align="center">
					<tr>
						<td><input type="submit" value="${okButton}"></td>
						<td><input type="button" value="${cancelButton}" onclick="window.history.back(); handleCancelAction();"/></td>
					</tr>
				</table>
			</form>
		</c:when><c:otherwise>
			<table align="center">
				<tr>
					<td><input type="button" value="${cancelButton}" onclick="window.history.back(); handleCancelAction();"/></td>
				</tr>
			</table>
		</c:otherwise></c:choose>

	</body>
</html>


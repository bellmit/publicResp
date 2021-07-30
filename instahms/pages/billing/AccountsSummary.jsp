<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="/WEB-INF/instafn.tld" prefix="ifn" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>

<html>
<head>
	<title><insta:ltext key="billing.accountingexportsummary.receipt.claims.title"/></title>
	<style type="text/css">
		.account {width: 15em;}
	</style>
</head>

<jsp:useBean id="entityDisplay" class="java.util.HashMap"/>
<c:set target="${entityDisplay}" property="receipts" value="Receipts and Claims"/>
<c:set target="${entityDisplay}" property="bills" value="Bills"/>
<c:set target="${entityDisplay}" property="purchases" value="Purchases"/>
<c:set target="${entityDisplay}" property="returns" value="Returns"/>
<c:set target="${entityDisplay}" property="payments" value="Payments"/>
<c:set target="${entityDisplay}" property="tobepaidamt" value="Payments (Journal)"/>


<body>
	<div class="pageHeader"><insta:ltext key="billing.accountingexportsummary.receipt.claims.accountingexportsummary"/></div>

	<c:forEach items="receipts,bills,purchases,returns,payments,tobepaidamt" var="e">
		<fieldset class="fieldSetBorder">
			<legend class="fieldSetLabel">${entityDisplay[e]}:</legend>
			<table cellspacing="0" cellpadding="0">
				<tr>
					<td valign="top"><h4><insta:ltext key="billing.accountingexportsummary.receipt.claims.debit"/></h4>
						<table  class="dataTable" cellspacing="0" cellpadding="0">
							<tr>
								<th class="account"><insta:ltext key="billing.accountingexportsummary.receipt.claims.account"/></th>
								<th><insta:ltext key="billing.accountingexportsummary.receipt.claims.amount"/></th>
							</tr>

							<c:set var="total" value="0.00"/>
							<c:forEach items="${debitSummary[e]}" var="acc">
								<tr>
									<td>${acc.key}</td>
									<td align="right">${acc.value}</td>
								</tr>
								<c:set var="total" value="${total + acc.value}"/>
							</c:forEach>

							<tr>
								<td><b><insta:ltext key="billing.accountingexportsummary.receipt.claims.total"/></b></td>
								<td align="right"><b><c:out value="${total}"/></b></td>
							</tr>
						</table>
					</td>
					<td>&nbsp</td>
					<td valign="top"><h4><insta:ltext key="billing.accountingexportsummary.receipt.claims.credit"/></h4>
						<table  class="dataTable" cellspacing="0" cellpadding="0">
							<tr>
								<th class="account"><insta:ltext key="billing.accountingexportsummary.receipt.claims.account"/></th>
								<th><insta:ltext key="billing.accountingexportsummary.receipt.claims.amount"/></th>
							</tr>

							<c:set var="total" value="0.00"/>
							<c:forEach items="${creditSummary[e]}" var="acc">
								<tr>
									<td>${acc.key}</td>
									<td align="right">${acc.value}</td>
								</tr>
								<c:set var="total" value="${total + acc.value}"/>
							</c:forEach>

							<tr>
								<td><b><insta:ltext key="billing.accountingexportsummary.receipt.claims.total"/></b></td>
								<td align="right"><b><c:out value="${total}"/></b></td>
							</tr>
						</table>
					</td>
				</tr>
			</table>
		</fieldset>
	</c:forEach>

	<table class="screenActions">
		<tr>
			<td>
				<form method="GET">
					<input type="hidden" name="_method" value="getVouchers"/>
					<input type="hidden" name="format" value="tallyxml"/>
					<input type="hidden" name="fromDate" value="${ifn:cleanHtmlAttribute(param.fromDate)}"/>
					<input type="hidden" name="fromTime" value="${ifn:cleanHtmlAttribute(param.fromTime)}"/>
					<input type="hidden" name="toDate" value="${ifn:cleanHtmlAttribute(param.toDate)}"/>
					<input type="hidden" name="toTime" value="${ifn:cleanHtmlAttribute(param.toTime)}"/>
					<input type="hidden" name="exportFor" value="${ifn:cleanHtmlAttribute(param.exportFor)}"/>
					<input type="hidden" name="voucherDate" value="${ifn:cleanHtmlAttribute(param.voucherDate)}"/>
					<input type="hidden" name="useVoucherDate" value="${ifn:cleanHtmlAttribute(param.useVoucherDate)}"/>
					<input type="hidden" name="voucherFromDate" value="${ifn:cleanHtmlAttribute(param.voucherFromDate)}"/>
					<input type="hidden" name="voucherToDate" value="${ifn:cleanHtmlAttribute(param.voucherToDate)}"/>

					<c:forEach var="exportItem" items="${paramValues.exportItems}">
						<input type="hidden" name="exportItems" value="${exportItem}"/>
					</c:forEach>
					<button type="submit" name="tallyXMLExport" class="button" accesskey="X"/>
						<b><u><insta:ltext key="billing.accountingexportsummary.receipt.claims.x"/></u></b><insta:ltext key="billing.accountingexportsummary.receipt.claims.mlexport"/></button>&nbsp;
				</form>
			</td>

			<td>
				<form method="GET">
					<input type="hidden" name="_method" value="getVouchers"/>
					<input type="hidden" name="format" value="details"/>
					<input type="hidden" name="fromDate" value="${ifn:cleanHtmlAttribute(param.fromDate)}"/>
					<input type="hidden" name="fromTime" value="${ifn:cleanHtmlAttribute(param.fromTime)}"/>
					<input type="hidden" name="toDate" value="${ifn:cleanHtmlAttribute(param.toDate)}"/>
					<input type="hidden" name="toTime" value="${ifn:cleanHtmlAttribute(param.toTime)}"/>
					<input type="hidden" name="exportFor" value="${ifn:cleanHtmlAttribute(param.exportFor)}"/>
					<input type="hidden" name="voucherDate" value="${ifn:cleanHtmlAttribute(param.voucherDate)}"/>
					<input type="hidden" name="useVoucherDate" value="${ifn:cleanHtmlAttribute(param.useVoucherDate)}"/>
					<input type="hidden" name="voucherFromDate" value="${ifn:cleanHtmlAttribute(param.voucherFromDate)}"/>
					<input type="hidden" name="voucherToDate" value="${ifn:cleanHtmlAttribute(param.voucherToDate)}"/>

					<c:forEach var="exportItem" items="${paramValues.exportItems}">
						<input type="hidden" name="exportItems" value="${exportItem}"/>
					</c:forEach>
					<button type="submit" name="viewDetails" class="button" accesskey="D"/>
						<insta:ltext key="billing.accountingexportsummary.receipt.claims.view"/> <b><u><insta:ltext key="billing.accountingexportsummary.receipt.claims.d"/></u></b><insta:ltext key="billing.accountingexportsummary.receipt.claims.etails"/></button>&nbsp;
				</form>
			</td>

			<td>
				<form method="GET">
					<input type="hidden" name="_method" value="getScreen"/>
					<button type="submit" name="selectPeriod" class="button" accesskey="P"/>
						<insta:ltext key="billing.accountingexportsummary.receipt.claims.selected"/> <b><u><insta:ltext key="billing.accountingexportsummary.receipt.claims.p"/></u></b><insta:ltext key="billing.accountingexportsummary.receipt.claims.eriod"/></button>&nbsp;
				</form>
			</td>
		</tr>
	</table>

</body>

</html>


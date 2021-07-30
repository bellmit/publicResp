<%@page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ taglib tagdir="/WEB-INF/tags" prefix="insta" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
<title>Insta HMS</title>
<insta:link type="js" file="master/accounting/accountingprefs.js"/>
<c:set var="cpath" value="${pageContext.request.contextPath}"/>

</head>
<body>
	<h1>Accounting Voucher Types</h1>

	<form action="VoucherTypes.do" method="POST">

	<input type="hidden" name="method" value="update"/>
		<fieldset class="fieldSetBorder">
			<table class="formtable">
				<tr><td class="formlabel">Receipt Voucher Type: </td>
					<td ><input type="text" class="field" name="receipt_vtype" id="receipt_vtype" value="${vouchertypes.receipt_vtype}">
					<img class="imgHelpText" src="${cpath}/images/help.png" title="Receipt Voucher Type"/></td>

					<td class="formlabel" >Refund Voucher Type: </td>
					<td ><input type="text" class="field" name="refund_vtype" id="refund_vtype" value="${vouchertypes.refund_vtype}">
					<img class="imgHelpText" src="${cpath}/images/help.png" title="Refund Voucher Type"/></td>

					<td class="formlabel">Bill Voucher Type: </td>
					<td ><input type="text" class="field" name="bill_vtype" id="bill_vtype" value="${vouchertypes.bill_vtype}">
					<img class="imgHelpText" src="${cpath}/images/help.png" title="Bill Voucher Type"/></td>
				</tr>
				<tr><td class="formlabel">Pharmacy Bill Voucher Type: </td>
					<td ><input type="text" class="field" name="pharmacy_bill_vtype" id="pharmacy_bill_vtype" value="${vouchertypes.pharmacy_bill_vtype}">
					<img class="imgHelpText" src="${cpath}/images/help.png" title="Pharmacy Bill Voucher Type"/></td>
					<td class="formlabel">Pharmacy Return Voucher Type: </td>
					<td ><input type="text" class="field" name="pharmacy_return_vtype" id="pharmacy_return_vtype" value="${vouchertypes.pharmacy_return_vtype}">
					<img class="imgHelpText" src="${cpath}/images/help.png" title="Pharmacy Return Voucher Type"/></td>
					<td class="formlabel">Payment Posting Voucher Type: </td>
					<td ><input type="text" class="field" name="payment_vtype" id="payment_vtype" value="${vouchertypes.payment_vtype}">
					<img class="imgHelpText" src="${cpath}/images/help.png" title="Payment Posting Voucher Type"/></td>
				</tr>
				<tr><td class="formlabel" >Payment Voucher Voucher Type: </td>
					<td ><input type="text" class="field" name="payment_voucher_vtype" id="payment_voucher_vtype" value="${vouchertypes.payment_voucher_vtype}">
					<img class="imgHelpText" src="${cpath}/images/help.png" title="Payment Voucher Voucher Type"/></td>
					<td class="formlabel" >Purchase Voucher Type: </td>
					<td ><input type="text" class="field" name="purchase_vtype" id="purchase_vtype" value="${vouchertypes.purchase_vtype}">
					<img class="imgHelpText" src="${cpath}/images/help.png" title="Purchase Voucher Type"/></td>
					<td class="formlabel" >Purchase Return Voucher Type: </td>
					<td><input type="text" class="field" name="purchase_return_vtype" id="purchase_return_vtype" value="${vouchertypes.purchase_return_vtype}">
					<img class="imgHelpText" src="${cpath}/images/help.png" title="Purchase Return Voucher Type"/></td>
				</tr>
				<tr><td class="formlabel">Transfers Voucher Type: </td>
					<td ><input type="text" class="field" name="transfers_vtype" id="transfers_vtype" value="${vouchertypes.transfers_vtype}">
					<img class="imgHelpText" src="${cpath}/images/help.png" title="Transfers Voucher Type"/></td>
				</tr>
			</table>
		</fieldset>
		<div class="screenActions" >
			<button type="submit" name="save" accesskey="S">
			<b><u>S</u></b>ave</button>
		</div>
	</form>
</body>
</html>
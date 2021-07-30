<!-- [#setting datetime_format ="dd-MM-yyyy"]-->
<html>
<body>
[#escape x as x?html]
		<table align="center" width="100%">
			<tr>
				<td align="center"><b>Supplier Payment Voucher Details</b></td>
			</tr>
		</table>
		[#assign payeeName=""]
		[#assign postedDate=""]
		[#list supplierVoucher as s]
			[#assign payeeName	= s.supplier_name]
			[#assign postedDate = s.posted_date]
			[#assign remarks = s.remarks]
		[/#list]
		<table width="100%">
			<tr>
				<td>Voucher No:  ${voucherNo}</td>
				<td align="right">Date: ${postedDate}</td>
			</tr>
			<tr>
				<td>Pay to: ${payeeName}</td>
			</tr>
		</table>
		<table width="100%" cellpadding="0" cellspacing="0">
			<tr class="border-above-below">
				<th>Invoice/Debit_Note No</th>
				<th>invioce/Debit_Note Date</th>
				<th align="right">Amount</th>
			</tr>
			[#assign TotalAmount=0]
			[#list supplierVoucher as supplier]
			<tr>
				<td>${supplier.invoice_no}</td>
				<td>${supplier.inv_deb_date}</td>
				<td align="right">${supplier.amount}</td>
			</tr>
			[#assign TotalAmount = supplier.totalamount]
			[/#list]
			</table>
			<table width="100%" style="margin-top:1em" cellspacing="3" cellpadding="0">
			<tr>
				<td align="right">Total Amount : ${TotalAmount}</td>
			</tr>
		</table>
		<table width="100%" cellspacing="3" cellpadding="3">
			<tr>
				<td>Amount in words : ${voucherTotalAmount}</td>
			</tr>
		</table>
		<table width="100%" cellspacing="3" cellpadding="3">
			<tr>
				<td>Remarks: ${remarks!}</td>
			</tr>
		</table>
		<table width="100%" cellspacing="3" cellpadding="0">
			<tr>
				<td>Payment Mode: ${paymentMode}</td>
				<td>Card Type: ${cardType!}</td>
				<td>Bank Name: ${bankName!}</td>
				<td>Reference No: ${referenceNo!}</td>
			</tr>
		</table>
		<table width="100%" cellspacing="10" cellpadding="0">
			<tr>
				<td align="center">Prepared by</td>
				<td align="center">Authorized by</td>
				<td align="center">Receiver's Signature</td>
			</tr>
		</table>
[/#escape]
	</body>
</html>

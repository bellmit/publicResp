

<div>
<!--[#setting datetime_format="dd-MM-yyyy"]-->
<html>
	<body>
<!--[#escape x as x?html] -->
		<table align="center" width="100%">
			<tr>
				<td align="center">
				<!--[#if printType == "summary"]
						[#if voucherType == 'R'] -->
							<b>Receipt Voucher</b>
				<!--	[#else] -->
							<b>Payment Voucher</b>
				<!--	[/#if]
					[#else]
						[#if voucherType == 'R'] -->
								<b>Receipt Voucher Details</b>
				<!--		[#elseif paymentType == 'S'] -->
								<b>Supplier Payment Voucher Details</b>
				<!--		[#elseif paymentType == 'C'] -->
								<b>Miscellaneous Payment Voucher Details </b>
				<!--	[#else] -->
								<b>Payment Voucher Details</b>
				<!--	[/#if]
					[/#if] -->
				</td>
			</tr>
		</table>
<!--	[#assign payeeName=""]
		[#assign postedDate=""]
		[#assign remarks=""]
		[#list voucherDetails as v]
			[#assign payeeName = v.name]
			[#assign postedDate = v.date]
			[#assign remarks = v.remarks]
		[/#list] -->
			<table width="100%">
				<tr>
					<td>Voucher No:  ${voucherNo}</td>
					<td align="right">Date: ${postedDate}</td>
				</tr>
				<tr>
					<td>Pay to: ${payeeName}</td>
				</tr>
			</table>
<!--For all Summary Voucher Prints  -->
	<!--	[#if printType == "summary"]  -->
				<table width="100%">
					<tr class="border-above-below">
						<th>Category</th>
						<th align="right">Count</th>
						<th align="right">Amount</th>
					</tr>
			<!--	[#assign GrandTotal=0]
					[#assign TotalAmount=0]
					[#assign serviceTax=0]
					[#assign tds=0]
					[#list voucherDetails as voucher] -->
					<tr>
						<td>${voucher.category!}</td>
						<td align="right">${voucher.count}</td>
						<td align="right">${voucher.amount}</td>
					</tr>
			<!--	[#assign TotalAmount = voucher.totalamount]
					[#assign serviceTax = voucher.tax_amount]
					[#assign tds = voucher.tds_amount]
					[/#list]  -->
					</table>
					<table width="100%" style="margin-top:1em" cellspacing="3" cellpadding="0">
					<tr>
						<td>Tax Amount : ${serviceTax}</td>
						<td>TDS Amount : ${tds}</td>
					<!--[#if voucherType == 'R'] -->
						<td align="right">Total Amount : ${TotalAmount - tds}</td>
					<!--[#else] -->
						<td align="right">Total Amount : ${TotalAmount}</td>
					<!--[/#if] -->
					</tr>
				</table>
<!--For all Details Voucher Prints -->
		<!--[#else] -->
	<!--For Detail Vocher Print AND Reversal Voucher Type  -->
			<!--[#if voucherType == 'R'] -->
						<table width="100%">
							<tr class="border-above-below">
								<th>Voucher Date</th>
								<th>Description</th>
								<th>Category</th>
								<th>Amount</th>
							</tr>
					<!--	[#assign GrandTotal=0]
							[#assign TotalAmount=0]
							[#assign serviceTax=0]
							[#assign tds=0]
							[#list voucherDetails as voucher] -->
							<tr>
								<td>${voucher.date!}</td>
								<td>${voucher.description!}</td>
								<td>${voucher.category!}</td>
								<td>${voucher.amount}</td>
							</tr>
					<!--	[#assign GrandTotal = GrandTotal + voucher.amount]
							[#assign TotalAmount = voucher.voucher_amount]
							[#assign serviceTax = voucher.tax_amount]
							[#assign tds = voucher.tds_amount]
							[/#list] -->
							</table>
							<table width="100%" style="margin-top:1em" cellspacing="3" cellpadding="0">
							<tr class="border-above-below">
								<td colspan="3">Bill Summary</td>
							</tr>
							<tr>
								<td width="70%"></td>
								<td align="right">Grand total </td>
								<td align="right">${GrandTotal}</td>
							</tr>
							<tr>
								<td width="70%"></td>
								<td align="right">(+)Service Tax</td>
								<td align="right">${serviceTax}</td>
							</tr>
							<tr>
								<td width="70%"></td>
								<td align="right">(-) TDS </td>
								<td align="right">${tds}</td>
							</tr>
							<tr>
								<td width="70%"></td>
								<td align="right">Total Amount</td>
								<td align="right">${TotalAmount + tds}</td>
							</tr>
						</table>
	<!--For Detail Voucher Print AND Supplier Payment Voucher -->
			<!--	[#elseif paymentType == 'S'] -->

						<table width="100%" cellpadding="0" cellspacing="0">
							<tr class="border-above-below">
								<th>Invoice/Debit_Note No</th>
								<th>invioce/Debit_Note Date</th>
								<th align="right">Amount</th>
							</tr>
					<!--	[#assign TotalAmount=0]
							[#list voucherDetails as supplier] -->
							<tr>
								<td>${supplier.invoice_no}</td>
								<td>${supplier.inv_deb_date}</td>
								<td align="right">${supplier.amount}</td>
							</tr>
					<!--	[#assign TotalAmount = supplier.totalamount]
							[/#list] -->
							</table>
							<table width="100%" style="margin-top:1em" cellspacing="3" cellpadding="0">
							<tr>
								<td align="right">Total Amount : ${TotalAmount}</td>
							</tr>
						</table>
	<!--For Detail Voucher Print AND Miscellaneous Payment Voucher -->

				<!--[#elseif paymentType == 'C']  -->

						<table width="100%">
							<tr class="border-above-below">
								<th>Posted Date</th>
								<th>Description</th>
								<th>Category</th>
								<th align="right">Amount</th>
							</tr>
						<!--[#assign GrandTotal=0]
							[#assign TotalAmount=0]
							[#assign serviceTax=0]
							[#assign tds=0]
							[#list voucherDetails as voucher]  -->
							<tr>
								<td>${voucher.date}</td>
								<td>${voucher.description!}</td>
								<td>${voucher.category!}</td>
								<td align="right">${voucher.amount}</td>
							</tr>
						<!--[#assign TotalAmount = voucher.totalamount]
							[#assign serviceTax = voucher.tax_amount]
							[#assign tds = voucher.tds_amount]
							[/#list] -->
							</table>
							<table width="100%" style="margin-top:1em" cellspacing="3" cellpadding="0">
							<tr>
								<td>Tax Amount : ${serviceTax}</td>
								<td>TDS Amount : ${tds}</td>
								<td align="right">Total Amount : ${TotalAmount}</td>
							</tr>
						</table>
	<!--For Detail Voucher Print AND (Doctor or Referral or Prescribing Doctor or Other referral or Out House) Payment Voucher -->
			<!--[#else] -->
						<table width="100%">
							<tr class="border-above-below">
								<th>Bill Date</th>
								<th>Bill No</th>
								<th>MR No</th>
								<th>Patient Name</th>
								<th>Charge Head</th>
							<!--[#if paymentType != 'D'] -->
									<th>Description</th>
							<!--[/#if] -->
								<th>Amount</th>
							</tr>
						<!--[#assign GrandTotal=0]
							[#assign TotalAmount=0]
							[#assign serviceTax=0]
							[#assign tds=0]
							[#list voucherDetails as voucher] -->
							<tr>
								<td>${voucher.conducted_date!}</td>
								<td>${voucher.bill_no}</td>
								<td>${voucher.mr_no}</td>
								<td>${voucher.full_name}</td>
								<td>${voucher.chargehead_name}</td>
							<!--[#if paymentType != 'D'] -->
									<td>${voucher.description}</td>
							<!--[/#if] -->
								<td>${voucher.amount}</td>
							</tr>
					<!--	[#assign GrandTotal = GrandTotal + voucher.amount]
							[#assign TotalAmount = voucher.voucher_amount]
							[#assign serviceTax = voucher.tax_amount]
							[#assign tds = voucher.tds_amount]
							[/#list] -->
							</table>
							<table width="100%" cellspacing="0" cellpadding="0">
								<tr>
									<td>Discount Summary </td>
								</tr>
								<tr class="border-above-below">
									<th>Bill No</th>
									<th>Discount Authorizer</th>
									<th align="right">Discount Amount</th>
								</tr>
						<!--[#list voucherDetails as discount]
							[#assign remarks = discount.remarks!]
								[#if discount.overall_discount_auth_name??] -->
									<tr>
										<td>${discount.bill_no}</td>
										<td>${discount.overall_discount_auth_name!}</td>
										<td align="right">${discount.overall_discount_amt}</td>
									</tr>
						<!--	[/#if]
								[#if discount.discount_auth_dr_name?? ] -->
									<tr>
										<td>${discount.bill_no}</td>
										<td>${discount.discount_auth_dr_name!}</td>
										<td align="right">${discount.dr_discount_amt}</td>
									</tr>
						<!--	[/#if]
								[#if discount.discount_auth_pres_dr_name??] -->
									<tr>
										<td>${discount.bill_no}</td>
										<td>${discount.discount_auth_pres_dr_name!}</td>
										<td align="right">${discount.pres_dr_discount_amt}</td>
									</tr>
						<!--	[/#if]
								[#if discount.discount_auth_ref_name??] -->
									<tr>
										<td>${discount.bill_no}</td>
										<td>${discount.discount_auth_ref_name!}</td>
										<td align="right">${discount.ref_discount_amt}</td>
									</tr>
						<!--	[/#if]
								[#if discount.discount_auth_hosp_name??] -->
									<tr>
										<td>${discount.bill_no}</td>
										<td>${discount.discount_auth_hosp_name!}</td>
										<td align="right">${discount.hosp_discount_amt}</td>
									</tr>
						<!--[/#if]
							[/#list] -->
							</table>
							<table width="100%" cellspacing="3" cellpadding="3">
							<tr>
								<td>Remarks: ${remarks!}</td>
							</tr>
							</table>
							<table width="100%" style="margin-top:1em" cellspacing="3" cellpadding="3">
							<tr class="border-above-below">
								<td colspan="3">Bill Summary</td>
							</tr>
							<tr>
								<td width="70%"></td>
								<td align="right">Grand total </td>
								<td align="right">${GrandTotal}</td>
							</tr>
							<tr>
								<td width="70%"></td>
								<td align="right">(+)Service Tax</td>
								<td align="right">${serviceTax}</td>
							</tr>
							<tr>
								<td width="70%"></td>
								<td align="right">(-) TDS </td>
								<td align="right">${tds}</td>
							</tr>
							<tr>
								<td width="70%"></td>
								<td align="right">Total Amount</td>
								<td align="right">${TotalAmount}</td>
							</tr>
						</table>
	<!--		[/#if]
			[/#if] -->

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
					<td>Payment Mode: ${paymentMode!}</td>
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
</div>

<div>
<!--
	[#setting time_format="HH:mm"]
	[#setting date_format="dd-MM-yyyy"]
	[#setting datetime_format="dd-MM-yyyy HH:mm"]

	[#assign previousSaleId = '']
-->
[#escape x as x?html]
	<table width="100%" border="0">
		<tr>
			<td>Tin: ${sale.tin_no!""}</td>
    	<td align="right">DL No: ${sale.dl_no!""}</td>
		</tr>
	</table>

[/#escape]

[#assign title]
  [#if duplicate]Duplicate [/#if]
	[#if bill.billType=="C"]Credit [#else]Cash [/#if]
  [#if sale.type=="R"]Refund[#else]Bill[/#if]
[/#assign]

<div align="center">
	<b><u>${title}</u></b>
</div>

<br/>

[#escape x as x?html]
	[#if bill.visitType == "r"]
		<table width="100%" cellspacing="0" cellpadding="0" border="0">
			<tr>
				<td valign="top">
					<table>
						<tr>
							<td class="label">Patient: </td>
							<td class="info">${customer.customer_name}</td>
						</tr>
						<tr>
							<td class="label">Bill No:</td>
							<td class="info">${sale.sale_id}</td>
						</tr>
						<tr>
							<td></td>
							<td></td>
						</tr>
					</table>
				</td>

				<td>
					<table align="right">
						<tr>
							<td class="label">Doctor: </td>
							<td class="info">${doctorName!""}</td>
						</tr>
						<tr>
							<td class="label">Date:</td>
							<td class="info">${sale.sale_date}</td>
						</tr>
						<tr>
						<!--	[#if bill.billType=="C"] -->
							<td class="label">Hosp. Bill No:</td>
							<td class="info">${bill.billNo}</td>
						<!--	[#else] -->
							<td></td>
							<td></td>
						<!--	[/#if] -->
						</tr>
					</table>
				</td>
			</tr>
		</table>
	[#else]
		<table width="100%" cellspacing="0" cellpadding="0" border="0">
			<tr>
				<td valign="top">
					<table>
						<tr>
							<td class="label">Patient: </td>
							<td class="info">${patient.full_name}</td>
						</tr>
						<tr>
							<td class="label">Bill No:</td>
							<td class="info">${sale.sale_id}</td>
						</tr>
						<tr>
							<td class="label">MR No: </td>
							<td class="info">${patient.mr_no}
								[#if patient.org_name != "GENERAL"](${patient.org_name})[/#if]
							</td>
						</tr>
					</table>
				</td>

				<td>
					<table align="right">
						<tr>
							<td class="label">Doctor:</td>
							<td class="info">${doctorName!""}</td>
						</tr>
						<tr>
							<td class="label">Date:</td>
							<td class="info">${sale.sale_date}</td>
						</tr>

						<tr>
							<!--	[#if bill.billType=="C"] -->
								<td class="label">Hosp. Bill No:</td>
								<td class="info">${bill.billNo}</td>
							<!--	[#else] -->
								<td></td>
								<td></td>
							<!--	[/#if] -->
						</tr>
					</table>
				</td>
			</tr>
		</table>
	[/#if]
[/#escape]

<br/>

<table style="border-collapse: collapse;border-top: 1px solid silver;border-bottom: 1px solid silver;" width="100%" border="0">
	<tr>
		<th align="left" style="width: 3%;border-bottom: 1px solid silver;padding: 1px 1px 1px 0px;">#</th>
		<!--	[#if hasDiscounts] -->
			<th align="left" style="width: 40%; border-bottom: 1px solid silver;padding: 1px 1px 1px 0px;">Particulars</th>
		<!--	[#else] -->
			<th align="left" style="width: 48%; border-bottom: 1px solid silver;padding: 1px 1px 1px 0px;">Particulars</th>
		<!--	[/#if] -->
		<th align="left" style="width: 7%;border-bottom: 1px solid silver;padding: 1px 1px 1px 0px;">Qty</th>
		<th align="left" style="width: 8%;border-bottom: 1px solid silver;padding: 1px 1px 1px 0px;">Mfr</th>
		<th align="left" style="width: 8%;border-bottom: 1px solid silver;padding: 1px 1px 1px 0px;">Batch</th>
		<th align="left" style="width: 8%;border-bottom: 1px solid silver;padding: 1px 1px 1px 0px;">Expiry</th>
		<th align="right" style="width: 8%;border-bottom: 1px solid silver;padding: 1px 1px 1px 0px;">Rate</th>
		<!--	[#if hasDiscounts] -->
			<th align="left" style="width: 8%;border-bottom: 1px solid silver;padding: 1px 1px 1px 0px;">Disc</th>
		<!--	[/#if] -->
		<th align="right" style="width: 10%;border-bottom: 1px solid silver;padding: 1px 1px 1px 0px;">Amount</th>
	</tr>

	<!--
		[#if hasDiscounts]
		[#assign span = 8]
	[#else]
		[#assign span = 7]
	[/#if]
	-->

	<!--
	[#assign total=0]
	[#assign bill_discount = sale.discount!0]
	[#assign roundoff=sale.round_off!0]
	[#assign depositSetOff=bill.depositSetOff!0]
	[#assign rewardPointsRedeemedAmount=bill.rewardPointsRedeemedAmount!0]
	[#assign sno=1]

	[#list items as s]-->
			<tr>
				<td style="padding: 1px 1px 1px 0px;">${sno?string("#")}</td>
				<td style="padding: 1px 1px 1px 0px;">${s.medicine_short_name?html}</td>
				<td style="padding: 1px 1px 1px 0px;">${s.quantity?string("##.00")}</td>
				<td style="padding: 1px 1px 1px 0px;">${s.manf_mnemonic?html}</td>
				<td style="padding: 1px 1px 1px 0px;">${s.batch_no?html}</td>
				<td style="padding: 1px 1px 1px 0px;">[#if s.expiry_date?has_content]${s.expiry_date?string("MMM-yyyy")}[/#if]</td>
				<td align="right" style="padding: 1px 1px 1px 0px;">${s.rate}</td>
				<!--	[#if hasDiscounts] -->
					<td style="padding: 1px 1px 1px 0px;"><!--	[#if (s.discount_per!0) != 0] -->${s.discount_per}% <!--[/#if ] --></td>
				<!--	[/#if] -->
				<td align="right" style="padding: 1px 1px 1px 0px;">${(s.amount)}</td>
			</tr>
		<!--
		[#assign total=(total+s.amount)]
		[#assign sno=sno+1]
	[/#list]  -->


<!--	[#if (bill_discount != 0)] -->
		<tr>
			<!--	[#if (sale.discount_per != 0)] -->
				<td colspan="3" align="right">Bill Discount @${sale.discount_per?string("##.00")}% :</td>
			<!--	[#else] -->
				<td colspan="3" align="right">Bill Discount :</td>
			<!--	[/#if] -->
			<td colspan="4"><b>${bill_discount} - ${billDiscountWords}</b> </td>
		</tr>
		<!--	[#assign total=(total - bill_discount)]
	[/#if]
	[#if roundoff !=0.00] -->
		<tr>
			<td colspan="${span}" align="right">Round Off</td>
			<td align="right">${roundoff}</td>
		</tr>
		<!--	[#assign total=(total + roundoff)]
	[/#if] -->
	<!--[#if depositSetOff !=0.00 && bill.billType!="C"] -->
		<tr>
			<td colspan="${span}" align="right">Deposit Set Off</td>
			<td align="right">${depositSetOff}</td>
		</tr>
		<!--	[#assign total=(total - depositSetOff)]
	[/#if] -->
	<!--[#if bill.rewardPointsRedeemed > 0 && bill.billType!="C"] -->
		<tr>
			<td colspan="${span}" align="right">Points Redeemed Amt</td>
			<td align="right">${rewardPointsRedeemedAmount}</td>
		</tr>
		<!--	[#assign total=(total - rewardPointsRedeemedAmount)]
	[/#if] -->

	<tr>
		<td colspan="${span+1}">
			<table cellpadding="0" celspacing="0" width="100%" border="0">
			<tbody>
	  		 <!--[#if total !=0.00] -->
	  			<!-- [#if bill.billType =="C"] -->
	  			<tr> <td> <b>(Added to Bill)</b> </td></tr>
	  			<!-- [#else] -->
					  <!--[#if receipt?has_content || refund?has_content] -->
						<tr>
							<td>Payment Mode:
								<table>
									<tr>
										<td>Mode</td>
										<td>Amount</td>
										<td>Card Type</td>
										<td>Bank</td>
										<td>Ref no.</td>
										<td>Currency</td>
										<td>Foreign Curr.</td>
									</tr>
									<!--[#if receipt?has_content] -->
										<!--[#list receipt as recpt] -->
										<tr>
											<td>${recpt.paymentMode}</td>
											<td>${recpt.amount}</td>
											<td>${recpt.cardType!}</td>
											<td>${recpt.bankName!}</td>
											<td>${recpt.referenceNo!}</td>
											<td>${recpt.currency!}</td>
											<td>${recpt.currencyAmt!}</td>
										</tr>
										<!-- [/#list] -->
									<!-- [/#if] -->
									<!--[#if refund?has_content] -->
										<!--[#list refund as refnd] -->
										<tr>
											<td>${refnd.paymentMode}</td>
											<td>${refnd.amount}</td>
											<td>${refnd.cardType!}</td>
											<td>${refnd.bankName!}</td>
											<td>${refnd.referenceNo!}</td>
											<td>${refnd.currency!}</td>
											<td>${refnd.currencyAmt!}</td>
										</tr>
										<!-- [/#list] -->
									<!-- [/#if] -->
								</table>
							</td>
						</tr>
					<!-- [/#if] -->
			<!-- [/#if] -->
				<tr>
					<td align="right">Total: <b>${netAmount} - ${netAmountWords}</b></td>
				</tr>
		  	<!-- [/#if] -->

			<!--	[#list vatDetails?keys as rate] -->
				<tr>
				[#if "${taxLabel.procurement_tax_label}" == "V"]
					<td>VAT @${rate}%: ${vatDetails[rate]}</td>
					<td></td>
				[#else]
					<td>GST @${rate}%: ${vatDetails[rate]}</td>
					<td></td>
				[/#if]
				</tr>
			<!--	[/#list] -->
			</tbody>
			</table>
		</td>
	</tr>

	<!--
	[#assign sno=1]
	[#assign subtotal=0]
    [#list returns as r]
    [#assign currentSaleId = r.sale_id]  -->
   <!--	[#if previousSaleId != currentSaleId] [#assign sno=1] [#assign subtotal=0] -->
			<tr height="2px;"><td colspan="${span}">&nbsp;</td></tr>
   			<tr >
   				<td colspan="${span}">Return Bill : ${r.sale_id}  dated  ${r.sale_date}</td>
   			</tr>

   <!--	[/#if] -->
		    <tr>
				<td style="padding: 1px 1px 1px 0px;">${sno?string("#")}</td>
				<td style="padding: 1px 1px 1px 0px;">${r.medicine_short_name?html}</td>
				<td style="padding: 1px 1px 1px 0px;">${r.quantity?string("##.00")}</td>
				<td style="padding: 1px 1px 1px 0px;">${r.manf_mnemonic?html}</td>
				<td style="padding: 1px 1px 1px 0px;">${r.batch_no?html}</td>
				<td style="padding: 1px 1px 1px 0px;">[#if r.expiry_date?has_content]${r.expiry_date?string("MMM-yyyy")}[/#if]</td>
				<td align="right" style="padding: 1px 1px 1px 0px;">${r.rate}</td>
				<!--	[#if hasDiscounts] -->
					<td style="padding: 1px 1px 1px 0px;"><!--	[#if (r.discount_per!0) != 0] -->${r.discount_per}% <!--[/#if ] --></td>
				<!--	[/#if] -->
				<td align="right" style="padding: 1px 1px 1px 0px;">${(r.amount)}</td>
			</tr>
	<!--
	[#assign subtotal=(subtotal+r.amount)]
		[#assign total=(total+r.amount)]
		[#assign previousSaleId = r.sale_id]
	[#if r.rcount == sno]
			[#if (r.bill_discount != 0)] -->
		<tr>
			<!--	[#if (r.bill_discount_per != 0)] -->
				<td colspan="3" align="right">Bill Discount @${r.bill_discount_per}% :</td>
			<!--	[#else] -->
				<td colspan="3" align="right">Bill Discount :</td>
			<!--	[/#if] -->
			<td colspan="4">${r.bill_discount} - ${billDiscountWords}</td>
		</tr>
		<!--	[#assign subtotal=(subtotal - r.bill_discount)]
			[#assign total=(total - r.bill_discount)]
	[/#if]
	[#if r.round_off !=0.00] -->
		<tr>
			<td colspan="${span}" align="right">Round Off</td>
			<td align="right">${r.round_off}</td>
		</tr>
		<!--	[#assign subtotal=(subtotal + r.round_off)]
				[#assign total=(total + r.round_off)]
	[/#if] -->
		<tr>
			<td colspan="${span}" align="right">Sub Total :</td>
			<td align="right">${subtotal}</td>
		</tr>

	<!--[/#if]
[#assign sno=sno+1]

	[/#list] -->
</table>

<br/>
<!-- [#if retvatDetails?has_content] -->
<table>
[#if "${taxLabel.procurement_tax_label}" == "V"]
	<tr><td>Total VAT for above Returns </td></tr>
	<!--	[#list retvatDetails?keys as rate] -->
				<tr><td>VAT @${rate}%: ${retvatDetails[rate]}</td></tr>

		<!--  [/#list] -->
[#else]
	<tr><td>Total GST for above Returns </td></tr>
	<!--	[#list retvatDetails?keys as rate] -->
				<tr><td>GST @${rate}%: ${retvatDetails[rate]}</td></tr>

		<!--  [/#list] -->
[/#if]
</table>
<!-- [/#if] -->
<!-- [#if returns?has_content] -->
     <div align="right"> Net Amount : ${total}</div>
     <div align="right"> Net Amount in Words: ${netAmountWords}</div>
<!-- [/#if] -->
[#escape x as x?html]
  <p>${sale.user_display_name}</p>
[/#escape]

</div>


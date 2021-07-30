<!--
	[#setting time_format="HH:mm"]
	[#setting date_format="dd-MM-yyyy"]
	[#setting datetime_format="dd-MM-yyyy HH:mm"]
	[#assign empty={}]
-->
[#escape x as x?html]
<div align="center">
		<b><u>SPONSOR RECEIPT</u></b>
</div>
<br/>
<div class="patientHeader" style="margin-bottom: 1em">
	<table width="100%" cellspacing="0" cellpadding="0">
		<tr>
			<td>
				<table>
					[#if sponsorBill.sponsor_type == "S"]
						<tr>
							<td>TPA / Sponsor Name:	</td>
							<td>${tpa.tpa_name!}</td>
						</tr>
						<tr>
							<td>Address:</td>
							<td>${tpa.address!}</td>
						</tr>
						<tr>
							<td></td>
							<td></td>
						</tr>
						<tr>
							<td></td>
							<td></td>
						</tr>
					[#else]
						<tr>
							<td>Hospital Name:</td>
							<td>${hospital.hospital_name!}</td>
						</tr>
						<tr>
							<td>Address:</td>
							<td></td>
						</tr>
						<tr>
							<td></td>
							<td></td>
						</tr>
						<tr>
							<td></td>
							<td></td>
						</tr>
					[/#if]
				</table>
			</td>
			<td>
				<table>
					<tr>
						<td>Sponsor Claim No:</td>
						<td>${sponsorReceipt.sponsor_bill_no}</td>
					</tr>
					<tr>
						<td>Sponsor Receipt No:</td>
						<td>${sponsorReceipt.sponsor_receipt_no}</td>
					</tr>
					<tr>
						<td>Receipt Date:</td>
						<td>${sponsorReceipt.display_date}</td>
					</tr>
					<tr>
						<td></td><td></td>
					</tr>
				</table>
			</td>
		</tr>
	</table>
</div>
<br/>
<div>
<table cellpadding="0" celspacing="0" width="100%" border="0">
		<tr>
			<td>
				<table>
				<tr>
					<td>
						Received Amount: <b>${currencySymbol} ${sponsorReceipt.amount}</b>
					</td>
					<td>TDS Amount Received: ${currencySymbol} ${sponsorReceipt.tds_amt}</td>
					<td>
						Towards: [#if "${sponsorReceipt.recpt_type}" =="A"] Advance
						[#else]	 Settlement	[/#if]
					</td>
					</tr>
				</table>
			</td>
		</tr>

		<tr>
			<td>
				<table>
				<tr>
					<td align="left"> Payment Mode:	<b> ${sponsorReceipt.payment_mode}	</b> </td>
					[#if (sponsorReceipt.card_type??) && (sponsorReceipt.card_type !="")]
					<td>Card Type: <b>${sponsorReceipt.card_type!""}</b></td>
					[#else]
					<td></td>
					[/#if]
				</tr>

				<tr>
					[#if sponsorReceipt.bank_name?? && sponsorReceipt.bank_name !=""]
					<td>Bank:${sponsorReceipt.bank_name!""}</td>
					[#else]
					<td></td>
					[/#if]
					[#if sponsorReceipt.reference_no?? && sponsorReceipt.reference_no !=""]
					<td>Reference:${sponsorReceipt.reference_no!""}</td>
					[#else]
					<td></td>
					[/#if]
				</tr>
				</table>
			</td>
		</tr>
		<tr>
			<td><br/></td>
		</tr>
		<tr>
			<td>Against Claim Bill No: <b>${sponsorReceipt.sponsor_bill_no}</b> consisting of following bills:</td>
		</tr>
		<tr>
			<td>
				<table>
					[#list billList as bl]
					<tr>
					<td>Bill No: </td><td>${bl.bill_no}</td>
					<td>Amount: </td><td>${bl.total_claim}</td>
					</tr>
					[/#list]
				</table>
			</td>
		</tr>
		<tr>
			<td><br/></td>
		</tr>
		<tr>
			<td>
				Net amount received against this bill: ${currencySymbol} ${sponsorReceipt.amount}
			</td>
		</tr>
		<tr>
			<td>Received with thanks: ${netPayments} Only.</td>
		</tr>
		<tr>
			<td></td><td>Signature</td>
		</tr>
		<tr>
			<td></td><td>(${sponsorReceipt.username})</td>
		</tr>
</table>
</div>
[/#escape]

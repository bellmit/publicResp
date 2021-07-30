<!--
	[#setting time_format="HH:mm"]
	[#setting date_format="dd-MM-yyyy"]
	[#setting datetime_format="dd-MM-yyyy HH:mm"]
	[#assign empty={}]
-->
[#escape x as x?html]
<div align="center">
		<b><u>SPONSOR CONSOLIDATED BILL</u></b>
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
						<td>Sponsor Bill No:</td>
						<td>${sponsorBill.sponsor_bill_no}</td>
					</tr>
					<tr>
						<td>Claim Date:</td>
						<td>${sponsorBill.claim_date}</td>
					</tr>
					<tr>
						<td>Claim Amount:</td>
						<td>${sponsorBill.claim_amt}</td>
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
			<td>Bill No</td>
			<td>Mr No</td>
			<td>Visit Id</td>
			<td>Patient Name</td>
			<td>Bill Finalized Date</td>
			<td>Claim Amount</td>
		</tr>
		[#list billList as bl]
		<tr>
			<td>${bl.bill_no}</td>
			<td>${bl.mr_no!}</td>
			<td>${bl.visit_id}</td>
			<td>${bl.patient_name}</td>
			<td>${bl.finalized_date!}</td>
			<td>${bl.total_claim}</td>
		</tr>
		[/#list]
	</table>

	<table>
		<tr>
			<td></td><td>Prepared By:</td>
		</tr>
		<tr>
			<td></td><td>(${sponsorBill.userid})</td>
		</tr>
</table>
</div>
[/#escape]

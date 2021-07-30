<!-- [#setting datetime_format="dd-MM-yyyy"] -->

<!-- [#escape x as x?html] -->
<div align="center">
	<b><u>${depositPrintName}</u></b>
</div>
 <!-- [#list depositsList as dl] -->
[#assign pkgName = dl.package_name!""]
[#assign depositFor = dl.deposit_avalibility!""]
[#if pkgName != "" && depositFor != ""]
[#assign depositFor = " (" + depositFor + ")"]
[/#if]
	<div class="patientHeader" style="margin-bottom: 1em">
		<table width="100%"  >
			<tr>
				<td>MR No: ${dl.mr_no}</td>
				<td align="right">Name: ${dl.patient_name}</td>
			</tr>
			<tr>
				<td>Age/Gender:
					<!-- [#if dl.patient_age_in == 'Y'] -->
						${dl.patient_age} Years / ${dl.patient_gender}
					<!-- [#elseif dl.patient_age_in == 'M'] -->
						${dl.patient_age} Months / ${dl.patient_gender}
					<!-- [#else] -->
						${dl.patient_age} Days / ${dl.patient_gender}
					<!-- [/#if] -->
				</td>
				<td align="right">Contact No: ${patient_phone!}</td>
			</tr>
			<tr>
				<td>Receipt No: ${dl.deposit_no}</td>
				<td align="right">Deposit Date: ${dl.deposit_date}</td>
			</tr>
		</table>
	</div>
	<table width="100%">
		<tr>
			<td>Received Amount: <b>${currencySymbol}${dl.amount}</b></td>
			<td>Towards: <!-- [#if dl.deposit_type == "R"] --> Deposit <!-- [#else] --> Refund <!-- [/#if] --></td>
		</tr>
		<tr>
			<td colspan="2">Against: <b>${dl.mr_no}</b></td>
		</tr>
		<tr>
			<td>Mode of Payment: ${dl.payment_mode_name}</td>
			<!-- [#if (dl.card_type??) && (dl.card_type !="")] -->
			<td>Card Type: <b>${dl.card_type!""}</b></td>
			<!-- [#else] -->
			<td></td>
			<!-- [/#if] -->
		</tr>
		<tr>
			<!-- [#if (dl.bank_name??) && (dl.bank_name !="")] -->
			<td>Bank : ${dl.bank_name!?html}</td>
			<!-- [#else] -->
			<td></td>
			<!-- [/#if] -->
			<!-- [#if (dl.reference_no??) && (dl.reference_no !="")] -->
			<td>Reference No: ${dl.reference_no!}</td>
			<!-- [#else] -->
			<td></td>
			<!-- [/#if] -->
		</tr>
		<tr>
			<td>Net Amount received against this Receipt / Refund :${currencySymbol}${dl.amount}</td>
		</tr>
		<tr>
			<td>Deposit for: ${pkgName}${depositFor}</td>
			<td>Deposit Payee Name: ${dl.deposit_payer_name}</td>
		</tr>
		<tr>
			<td colspan="2">Received with thanks : ${AmountinWords}</td>
		</tr>
	</table>
	<table width="100%">
		<tr>
			<td align="right">Signature</td>
		</tr>
		<tr>
			<td align="right">( ${dl.username} )</td>
		</tr>
	</table>
<!-- [/#list] -->
<!-- [/#escape] -->

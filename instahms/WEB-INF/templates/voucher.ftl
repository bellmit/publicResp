<tr class="start">
	<td>${VOUCHER.formattedDate}</td>
	<td width="250"></td>
	<td>${VOUCHER.voucherType}</td>
	<td>${VOUCHER.voucherNumber}</td>
	<td></td>
	<td></td>
	<td></td>
</tr>
[#list VOUCHER.ledgerList as ledgers]
	<tr>
		<td></td>
		<td width="250">${ledgers.LEDGERNAME}</td>
		<td></td>
		<td></td>
		<td>
			[#if ledgers.billAllocations?has_content]
				${ledgers.billAllocations.NAME!}
			[/#if]
		</td>
		[#if ledgers.ISDEEMEDPOSITIVE]
			<td style="text-align: right">${ledgers.negativeAMOUNT}</td>
			<td></td>
		[#else]
			<td></td>
			<td style="text-align: right">${ledgers.AMOUNT}</td>
		[/#if]
		[#if ledgers.costCenters?has_content]
			[#list ledgers.costCenters as centerDetails]
				</tr>
				<tr>
					<td></td>
					<td style="text-align: right">Cost Center: ${centerDetails.NAME!}</td>
					<td></td>
					<td></td>
					<td></td>
					[#if ledgers.ISDEEMEDPOSITIVE]
						<td style="text-align: right">${centerDetails.negativeAMOUNT}</td>
						<td></td>
					[#else]
						<td></td>
						<td style="text-align: right">${centerDetails.AMOUNT}</td>
					[/#if]
			[/#list]
		[/#if]
	</tr>
[/#list]
<tr>
	<td></td>
	<td class="narration" colspan="4"><font style="color: #333;">${VOUCHER.narration}</span></td>
</tr>

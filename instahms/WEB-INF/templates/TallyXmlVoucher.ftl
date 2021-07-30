[#escape x as x?xml]
<TALLYMESSAGE xmlns:UDF="TallyUDF">
	<VOUCHER ACTION="${VOUCHER.action}" REMOTEID="${VOUCHER.remoteId}" VCHTYPE="${VOUCHER.voucherType}">
		<DATE>${VOUCHER.date?string("d MMM, yyyy")}</DATE>
		<formattedDATE>${VOUCHER.formattedDate}</formattedDATE>
		<GUID>${VOUCHER.guid}</GUID>
		<NARRATION>${VOUCHER.narration}</NARRATION>
		<VOUCHERTYPENAME>${VOUCHER.voucherTypeName}</VOUCHERTYPENAME>
		<VOUCHERNUMBER>${VOUCHER.voucherNumber}</VOUCHERNUMBER>
		<EFFECTIVEDATE>${VOUCHER.effectiveDate}</EFFECTIVEDATE>
		[#if (VOUCHER.voucherType != 'Receipt') && (VOUCHER.voucherType != 'Purchase')]
			[#list VOUCHER.ledgerList as ledgers]
				[#if ledgers.ISDEEMEDPOSITIVE]
					<ALLLEDGERENTRIES.LIST>
						<LEDGERNAME>${ledgers.LEDGERNAME}</LEDGERNAME>
						<LEDGERTYPENAME>${ledgers.LEDGERTYPENAME}</LEDGERTYPENAME>
						<ISDEEMEDPOSITIVE>Yes</ISDEEMEDPOSITIVE>
						<AMOUNT>${ledgers.AMOUNT}</AMOUNT>
						<negativeAMOUNT>${ledgers.negativeAMOUNT}</negativeAMOUNT>
						[#if ledgers.billAllocations?has_content]
							<BILLALLOCATIONS.LIST>
								<NAME>${ledgers.billAllocations.NAME!}</NAME>
								<BILLTYPE>${ledgers.billAllocations.BILLTYPE!}</BILLTYPE>
								<AMOUNT>${ledgers.billAllocations.AMOUNT!}</AMOUNT>
					       </BILLALLOCATIONS.LIST>
						[/#if]
						[#if ledgers.costCenters?has_content]
							<CATEGORYALLOCATIONS.LIST>
								<CATEGORY>Primary Cost Category</CATEGORY>
								[#list ledgers.costCenters as center]
									<COSTCENTREALLOCATIONS.LIST>
										<NAME>${center.NAME!}</NAME>
										<AMOUNT>${center.AMOUNT}</AMOUNT>
									</COSTCENTREALLOCATIONS.LIST>
								[/#list]
							</CATEGORYALLOCATIONS.LIST>
						[/#if]
					</ALLLEDGERENTRIES.LIST>
				[/#if]
			[/#list]
			[#list VOUCHER.ledgerList as ledgers]
				[#if !ledgers.ISDEEMEDPOSITIVE]
					<ALLLEDGERENTRIES.LIST>
						<LEDGERNAME>${ledgers.LEDGERNAME}</LEDGERNAME>
						<LEDGERTYPENAME>${ledgers.LEDGERTYPENAME}</LEDGERTYPENAME>
						<ISDEEMEDPOSITIVE>No</ISDEEMEDPOSITIVE>
						<AMOUNT>${ledgers.AMOUNT}</AMOUNT>
						<negativeAMOUNT>${ledgers.negativeAMOUNT}</negativeAMOUNT>
						[#if ledgers.billAllocations?has_content]
							<BILLALLOCATIONS.LIST>
								<NAME>${ledgers.billAllocations.NAME!}</NAME>
								<BILLTYPE>${ledgers.billAllocations.BILLTYPE!}</BILLTYPE>
								<AMOUNT>${ledgers.billAllocations.AMOUNT!}</AMOUNT>
					       </BILLALLOCATIONS.LIST>
						[/#if]
						[#if ledgers.costCenters?has_content]
							<CATEGORYALLOCATIONS.LIST>
								<CATEGORY>Primary Cost Category</CATEGORY>
								[#list ledgers.costCenters as center]
									<COSTCENTREALLOCATIONS.LIST>
										<NAME>${center.NAME!}</NAME>
										<AMOUNT>${center.AMOUNT}</AMOUNT>
									</COSTCENTREALLOCATIONS.LIST>
								[/#list]
							</CATEGORYALLOCATIONS.LIST>
						[/#if]
					</ALLLEDGERENTRIES.LIST>
				[/#if]
			[/#list]
		[#else]
			[#list VOUCHER.ledgerList as ledgers]
				[#if !ledgers.ISDEEMEDPOSITIVE]
					<ALLLEDGERENTRIES.LIST>
						<LEDGERNAME>${ledgers.LEDGERNAME}</LEDGERNAME>
						<LEDGERTYPENAME>${ledgers.LEDGERTYPENAME}</LEDGERTYPENAME>
						<ISDEEMEDPOSITIVE>No</ISDEEMEDPOSITIVE>
						<AMOUNT>${ledgers.AMOUNT}</AMOUNT>
						<negativeAMOUNT>${ledgers.negativeAMOUNT}</negativeAMOUNT>
						[#if ledgers.billAllocations?has_content]
							<BILLALLOCATIONS.LIST>
								<NAME>${ledgers.billAllocations.NAME!}</NAME>
								<BILLTYPE>${ledgers.billAllocations.BILLTYPE!}</BILLTYPE>
								<AMOUNT>${ledgers.billAllocations.AMOUNT!}</AMOUNT>
					       </BILLALLOCATIONS.LIST>
						[/#if]
						[#if ledgers.costCenters?has_content]
							<CATEGORYALLOCATIONS.LIST>
								<CATEGORY>Primary Cost Category</CATEGORY>
								[#list ledgers.costCenters as center]
									<COSTCENTREALLOCATIONS.LIST>
										<NAME>${center.NAME!}</NAME>
										<AMOUNT>${center.AMOUNT}</AMOUNT>
									</COSTCENTREALLOCATIONS.LIST>
								[/#list]
							</CATEGORYALLOCATIONS.LIST>
						[/#if]
					</ALLLEDGERENTRIES.LIST>
				[/#if]
			[/#list]
			[#list VOUCHER.ledgerList as ledgers]
				[#if ledgers.ISDEEMEDPOSITIVE]
					<ALLLEDGERENTRIES.LIST>
						<LEDGERNAME>${ledgers.LEDGERNAME}</LEDGERNAME>
						<LEDGERTYPENAME>${ledgers.LEDGERTYPENAME}</LEDGERTYPENAME>
						<ISDEEMEDPOSITIVE>Yes</ISDEEMEDPOSITIVE>
						<AMOUNT>${ledgers.AMOUNT}</AMOUNT>
						<negativeAMOUNT>${ledgers.negativeAMOUNT}</negativeAMOUNT>
						[#if ledgers.billAllocations?has_content]
							<BILLALLOCATIONS.LIST>
								<NAME>${ledgers.billAllocations.NAME!}</NAME>
								<BILLTYPE>${ledgers.billAllocations.BILLTYPE!}</BILLTYPE>
								<AMOUNT>${ledgers.billAllocations.AMOUNT!}</AMOUNT>
					       </BILLALLOCATIONS.LIST>
						[/#if]
						[#if ledgers.costCenters?has_content]
							<CATEGORYALLOCATIONS.LIST>
								<CATEGORY>Primary Cost Category</CATEGORY>
								[#list ledgers.costCenters as center]
									<COSTCENTREALLOCATIONS.LIST>
										<NAME>${center.NAME!}</NAME>
										<AMOUNT>${center.AMOUNT}</AMOUNT>
									</COSTCENTREALLOCATIONS.LIST>
								[/#list]
							</CATEGORYALLOCATIONS.LIST>
						[/#if]
					</ALLLEDGERENTRIES.LIST>
				[/#if]
			[/#list]
		[/#if]
	</VOUCHER>
</TALLYMESSAGE>
[/#escape]

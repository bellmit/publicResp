[#setting date_format="dd-MM-yyyy"]
[#setting datetime_format="dd-MM-yyyy HH:mm"]

<div align="center">
	[#if payType == "refund"]
		<b><u>Refund Voucher</u></b>
	[#elseif payType=="receipt"]
		<b><u>Receipt</u></b>
	[/#if]
</div>
<br/>
[#assign totAdvanceAmt=0]
[#escape x as x?html]
	<table cellspacing="0" cellpadding="0" width="100%">
		<tr>
			<td>Name:</td>
			<td>${cust.customer_name!}</td>
			<td>Visit ID:</td>
			<td>${cust.customer_id!}</td>
		</tr>
		<tr>
			<td>Phone No:</td>
			<td>${cust.phone_no!}</td>
			<td>Doctor Name:</td>
		    <td>${doctor!}</td>
		</tr>
	    [#list billDetails.receipts as receipt]
		    [#if receipt.receiptNo == receiptNo]
				<tr>
					 <td>ReceiptNo:</td>
					 <td>${receipt.receiptNo}</td>
					 <td>Receipt Date:</td>
					 <td>${receipt.receiptDate}</td>
				</tr>
				<tr>
				     [#if printMode=='Y']
					<td colspan="4">
						<hr style="margin-down: 20px" />
					</td>
					[/#if]
				</tr>
				<tr>
					<td width="30%">&nbsp;</td>
					<td width="30%">&nbsp;</td>
				</tr>
				<tr>
					<td>Payed Amount:</td>
					<td>
						<b>${receipt.amount}</b>
					</td>
					[#if receipt.receiptType == 'A']
						<td>Towards:</td>
						<td>
							<b>Advance</b>
						</td>
				  	[/#if]
					[#if  receipt.receiptType == 'S']
						<td>Towards:</td>
						<td>
						   <b>Settlement</b>
						</td>
					[/#if]
				</tr>
				<tr>
					<td>Payment Mode:</td>
					<td> <b>${receipt.paymentMode} [#if receipt.cardType?has_content](${receipt.cardType!})[/#if]</b></td>
				    <td width="30%">Against Bill:</td>
				    <td>  <b>${receipt.billNo}</b> </td>
				  </tr>
		  	[/#if]
		  	[#if receipt.counterType == 'P']
            	[#assign totAdvanceAmt=(totAdvanceAmt+receipt.amount)]
            [/#if]
		[/#list]
	   	[#if payType == 'receipt']

		[/#if]
    	 [#assign totRefundAmt=0]
	 	 [#list billDetails.refunds as refund]
			[#if refund.receiptNo == receiptNo]
			<tr>
				<td>ReceiptNo:</td>
				<td>${refund.receiptNo}</td>
				<td>Receipt Date:</td>
				<td>${refund.receiptDate}</td>
			</tr>
			<tr>
			    [#if printMode=='Y']
				<td colspan="4">
				<hr style="margin-down: 20px" />
				</td>
				[/#if]
			</tr>
			<tr>
				<td width="30%">&nbsp;</td>
				<td width="30%">&nbsp;</td>
			</tr>
			<tr>
				<td>Amount:</td>
				<td><b>${refund.amount}</b></td>
		    </tr>
     		<tr>
     			<td>Payment Mode:</td>
				<td> <b>${refund.paymentMode} [#if refund.cardType?has_content](${refund.cardType!})[/#if]</b></td>
			    <td width="30%">Against Bill:</td>
			    <td>  <b>${refund.billNo}</b> </td>
			</tr>
			[/#if]
			[#if refund.counterType == 'P']
				[#assign totRefundAmt=(totRefundAmt + refund.amount)]
			[/#if]
		[/#list]
        	[#if payType == 'refund']
		    <tr>
		    [#assign totNetDeposit=(totAdvanceAmt + totRefundAmt)]
			 <td colspan="4">Net Refunds in Pharmacy Counter: <b>${totRefundAmt}</b></td>
		</tr>
		<tr>
			<td colspan="4" align="right"><b> Patient Signature</b></td>
		</tr>
	   [/#if]
    </table>
[/#escape]

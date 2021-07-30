<!--
	[#setting time_format="HH:mm"]
	[#setting date_format="dd-MM-yyyy"]
	[#setting datetime_format="dd-MM-yyyy HH:mm"]
-->
<div align="center">
	<b><u>[#if type == "Return"]Stores Item Return Note[/#if]
		[#if type == "Replacement"]Stores Item Replacement Note[/#if]
		[#if type == "Debit"]Stores Return Debit Note[/#if]</u></b>
</div>

<div>
<!-- [#escape x as x?html] -->
 <table width="100%" cellspacing="0" cellpadding="0" border="0"
  style="border-collapse: collapse;1px solid silver;border-bottom: 1px solid silver;">
  	<tbody>
  				<tr>
  					[#if type == "Return"]
  					[#assign label = "Return No:"]
  					[#assign num = "${items[0].return_no}"]
  					[#assign date_label = "Return Date:"]
  					[#assign date =  "${items[0].return_date}"]
  					[/#if]
  					[#if type == "Replacement"]
  					[#assign label = "Replacement No:"]
  					[#assign num = "${items[0].return_no}"]
  					[#assign date_label = "Replacement Date:"]
  					[#assign date =  "${items[0].return_date}"]
  					[/#if]
  					[#if type == "Debit"]
  					[#assign label = "Debit No:"]
  					[#assign num = "${items[0].debit_note_no}"]
  					[#assign date_label = "Debit Date:"]
  					[#assign date =  "${items[0].debit_note_date}"]
  					[/#if]

  					<td width="20%">${label}</td>
	    		    <td width="25%">${num}</td>
	    		    <td width="20%">Supplier: </td>
	    		    <td width="25%">${items[0].supplier_name}</td>
	    		</tr>
	    		<tr>
	    			<td width="20%">Return Type: </td>
	    			<td width="25%">${items[0].return_type}</td>
	    		</tr>
	    		<tr>
	    			<td width="20%">${date_label}</td>
	    		    <td width="25%">${date}</td>
	    		    <td width="20%">From Store: </td>
	    		    <td width="25%">${items[0].dept_name}</td>
	    		</tr>

	    	</tbody>
 		 </table>
<!-- [/#escape] -->
[#escape x as x?html]
<table style="margin-top: 10px" cellspacing="0" cellpadding="0">
<tbody>
			<tr>
				<th >Item Name</th>
				<th >Mfr</th>
				<th >Batch/Serial No</th>
				<th >Expiry Date</th>
				[#if type == "Debit"]
					<th >Billed Qty</th>
					<th>Bonus Qty</th>
				[#else]
					<th >Qty</th>
				[/#if]
				[#if type == "Debit"]
					<th >Rate</th>
					<th >Disc</th>
				[#if "${taxLabel.procurement_tax_label}" == "V"]
					<th >VAT %</th>
					<th >VAT</th>
				[#else]
					<th >GST %</th>
					<th >GST</th>
				[/#if]

					<th >Orig.Amt</th>
					<th >Rev.Amt</th>
				[/#if]
			</tr>
	[#assign tot_amt=0]
	[#assign tot_rec_amt=0]
<!--[#assign sno=1]
	[#list items as s]-->
			<tr>
				<td width="50%">${s.medicine_name}</td>
					<td width="50%">${s.manf_name}</td>
					<td width="30%">${s.batch_no}</td>
					<td width="20%">${s.exp_date!""}</td>
					<td width="20%">${s.qty?string("##.00")}</td>
					[#if type == "Debit"]
					<td width="20%">${s.bonus_qty!?string("##.00")}</td>
					<td width="30%">${s.cost_price}</td>
					<td width="30%">${s.itemdiscount}</td>
					<td width="30%">${s.tax_rate}</td>
					<td width="30%">${s.tax}</td>
					<td width="30%">${s.totamt}</td>
					<td width="30%">${s.totrecamt}</td>
					[#assign tot_amt = tot_amt + s.totamt]
					[#assign tot_rec_amt = tot_rec_amt + s.totrecamt]
				[/#if]
			</tr>
<!--
[#assign sno=sno+1]
	[/#list] -->
	<tr></tr>
	<tr></tr>
	<tr>
		<td width="50%">Total Amount: </td>
		<td>${tot_amt}</td>
	</tr>
	<tr>
		<td width="100%">Total Received Amount:</td>
		<td>${tot_rec_amt}</td>
	</tr>
	</tbody>
	</table>
	[/#escape]
</div>


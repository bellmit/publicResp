
<!--
	[#setting time_format="HH:mm"]
	[#setting date_format="dd-MM-yyyy"]
	[#setting datetime_format="dd-MM-yyyy HH:mm"]
-->
<div align="center">
	<b><u>GATE PASS</u></b>
</div>


<div>
	<b>Following Items are permitted to pass out.</b>
</div>
<div>
<!-- [#escape x as x?html] -->
 <table width="100%" cellspacing="0" cellpadding="0" border="0"
  style="border-collapse: collapse;1px solid silver;border-bottom: 1px solid silver;">
  	<tbody>
  				<tr>
  					[#if type == "Issue"]
  					[#assign label = "Issue No:"]
  					[#assign num = "${items[0].user_issue_no}"]
  					[#assign date_label = "Issue Date:"]
  					[#assign date =  "${items[0].issue_date}"]
  					[/#if]
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
					<td width="20%">Gate Pass No:</td>
					<td width="25%">${items[0].gatepass_id}</td>
	    		</tr>
	    		[#if type != "Issue"]
	    		<tr>
	    			<td width="20%">Supplier: </td>
	    		    <td width="25%">${items[0].supplier_name}</td>
	    			<td width="20%">Return Type: </td>
	    			<td width="25%">${items[0].return_type}</td>
	    		</tr>
	    		[/#if]
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
				<th  >Item Name</th>
				<th >Batch/Serial No</th>
				<th >User</th>
				<th >Qty</th>
				<th>Qty Unit</th>
			</tr>
<!--[#assign sno=1]
	[#list items as s]-->
			<tr>
				<td width="50%">${s.medicine_name}</td>
					<td width="30%">${s.batch_no}</td>
					<td width="20%">${s.user_name}</td>
					<td width="20%">${s.qty?string("##.00")}</td>
					[#if type != "Issue"]
					<td width="20%">Qty in ${s.qty_unit}</td>
					[/#if]
			</tr>
<!--
[#assign sno=sno+1]
	[/#list] -->
	</tbody>
	</table>
	[/#escape]
</div>



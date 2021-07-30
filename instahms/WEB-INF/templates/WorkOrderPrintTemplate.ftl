<!--
	[#setting time_format="HH:mm"]
	[#setting date_format="dd-MM-yyyy"]
	[#setting datetime_format="dd-MM-yyyy HH:mm"]
-->

<div align="center">
	<h1><b><u>Work Order</u></b></h1>
</div>
[#escape x as x?html]

 <table width="100%" cellspacing="0" cellpadding="0" border="0"
  style="border-collapse: collapse;1px solid silver;border-bottom: 1px solid silver;">
  				<tr>
	    		    <td width="20%">Hospital Reg No: </td>
	    		    <td width="25%">${hospital_service_regn_no !""}</td>
	    		    <td width="25%">Hospital Pan No: </td>
	    		    <td width="30%">${hospital_pan !""}</td>
	    		</tr>
	    		<tr>
	    			<td width="20%">Hospital Tin No: </td>
	    		    <td width="25%">${hospital_tin !""}</td>
	    			<td width="20%">WO No: </td>
					<td width="25">${woBean.wo_no}</td>
	    		</tr>
	    		<tr>
	    			<td width="20%">WO Date: </td>
					<td width="25%">${woBean.wo_date?date}</td>
	    			<td width="25%">Expected Delivered Date:</td>
	    		    <td width="30%">${woBean.expected_received_date?date}</td>
	    		</tr>
				<tr>
					<td width="20%" valign="top">Supplier Address:</td>
					<td width="25%">${suppBean.supplier_name}<br/>
					                ${suppBean.supplier_address!""}<br/>
					                ${suppBean.supplier_city!""}<br/>
					                ${suppBean.supplier_state!""}<br/>
					                ${suppBean.supplier_country!""}</td>
					<td width="20%">Remarks:</td>
					<td width="25%">${woBean.remarks!}</td>
	    		</tr>
	    		<tr>
					<td width="20%" valign="top">Service TAX(%):</td>
						<td width="25%">
						 [#if woBean.wo_service_tax??]
							${woBean.wo_service_tax?string("0.##")}
						 [/#if]
						</td>
					<td width="20%" valign="top">CESS(%):</td>
						<td width="25%">
							[#if woBean.wo_cess_rate??]
								${woBean.wo_cess_rate?string("0.##")}
							[/#if]
						</td>
	    		</tr>
 		 </table>
[/#escape]
 <table style="border-collapse: collapse;border-top: 1px solid silver;border-bottom: 1px solid silver;" width="100%" border="0">
			<tr>
				<th align="left">#</th>
				<th align="left" width="20%">Item</th>
				<th align="right" width="10%">Qty</th>
				<th align="right" width="15%">Rate</th>
				<th align="right" width="15%">VAT%</th>
				<th align="right" width="15%">TAX</th>
				<th align="right" width="15%">Discount</th>
				<th align="right" width="15%">Description</th>
				<th align="right" width="15%">Amount</th>
			</tr>
[#assign sno=1]
    [#assign total_qty=0]
    [#assign total_discount=0]
    [#assign total_tax_amt=0]
    [#assign total_amt=0]
    [#assign total_service_tax=0]
    [#assign total_cess_tax=0]
    [#assign total_amt_discount=0]
    [#assign total_vat=0]
	[#list items as s]
[#escape x as x?html]
			<tr>
				<td >${sno?string("#")}</td>
				<td width="10%">${s.wo_item_name}</td>
				<td align="right" >${s.qty}</td>
				<td align="right" >${s.rate}</td>
				[#if s.vat_rate??]
					<td align="right" >${s.vat_rate}</td>
					<td align="right" >${s.item_tax}</td>
				[/#if]
				<td align="right" >${s.discount}</td>
				<td align="right" >${s.description}</td>
				<td align="right" >${s.amount}</td>
			</tr>
[/#escape]
	[#assign total_qty=(total_qty+s.qty)]
	[#if s.vat_rate??]
		[#assign total_tax_amt=(total_tax_amt+s.item_tax)]
	[/#if]
	[#assign total_discount=(total_discount+s.discount)]
	[#if s.vat_rate??]
		[#assign total_amt=(total_amt+((s.qty * s.rate)-s.discount+s.item_tax))]
	[#else]
		[#assign total_amt=(total_amt+((s.qty * s.rate)-s.discount))]
	[/#if]
	[#assign total_amt_discount=(total_amt_discount+((s.qty * s.rate)-s.discount))]
	[#assign sno=sno+1]
	[/#list]
</table>
<table  style="border-collapse: collapse;border-top: 1px solid silver;border-bottom: 1px solid silver;" width="100%" border="0">
			<tr></tr>
		    <tr>
		        <td align="right" colspan="8">Total Item Amount:</td>
		        <td align="right" width="15%">${total_amt}</td>
		    </tr>
		    <tr>
		        <td align="right" colspan="8">Total VAT:</td>
		        <td align="right" width="15%">${total_tax_amt}</td>
		    </tr>
		    [#if woBean.wo_service_tax??]
		     <tr>
		     	<td align="right" colspan="8">Total Service TAX:</td>
		     	[#assign total_service_tax = (total_service_tax +((woBean.wo_service_tax * total_amt_discount)/100) )]
		       	[#assign total_tax_amt = (total_tax_amt + ((woBean.wo_service_tax * total_amt_discount)/100))]
               	<td align="right" width="15%">${((woBean.wo_service_tax * total_amt_discount)/100)}</td>
             </tr>
            [/#if]
             <tr>
		     	<td align="right" colspan="8">Total Discount:</td>
               	<td align="right" width="15%">${total_discount}</td>
             </tr>
             [#if woBean.wo_cess_rate??]
             <tr>
		     	<td align="right" colspan="8">Total CESS:</td>
               	[#assign total_cess_tax = ((woBean.wo_cess_rate * total_tax_amt)/100)?string["0.00"]]
                <td align="right" width="15%">${total_cess_tax}</td>
             </tr>
             [/#if]
		    <tr>
		        <td align="right" colspan="8">Total WO Amount</td>
		        [#assign total_cess_tax = (total_amt+total_service_tax+total_cess_tax?number)]
		        <td align="right" width="15%">${total_cess_tax}</td>
		    </tr>
</table>
 <table  width="100%" border="0">
			 <tr></tr>
   			 <tr>
  			 	<td width="20%">Prepared By:</td>
  			 	<td width="30%">${woBean.raised_by!""}</td>
  			 	<td width="20%">Approved By:</td>
  			 	<td width="30%">${woBean.approved_by!""}</td>
  			</tr>
   </table>

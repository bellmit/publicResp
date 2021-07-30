<!--
	[#setting time_format="HH:mm"]
	[#setting date_format="dd-MM-yyyy"]
	[#setting datetime_format="dd-MM-yyyy HH:mm"]
-->
<div align="center">
	<b><u>Goods Received Note</u></b>
</div>
[#escape x as x?html]
 <table width="100%" cellspacing="0" cellpadding="0" border="0" style="border-collapse: collapse;1px solid silver;border-bottom: 1px solid silver;">
  				<tr>
	    		    <td width="20%">Hospital Reg No: </td>
	    		    <td width="25%">${hospital_service_regn_no!""}</td>
	    		    <td width="20%">Hospital Pan No: </td>
	    		    <td width="25%">${hospital_pan!""}</td>
	    		</tr>
	    		<tr>
	    			<td width="20%">Hospital Tin No: </td>
	    		    <td width="25%">${hospital_tin!""}</td>
	    		    <td width="20%"></td>
	    		    <td width="25%"></td>
	    		</tr>
    			<tr>
					<td width="20%">GRN No: </td>
					<td width="25">${items[0].grn_no}</td>
					<td width="20%">PO No: </td>
					<td width="25%">${items[0].po_no!""}</td>
				</tr>
				<tr>
					<td width="20%">GRN Date:</td>
					<td width="25%">${items[0].grn_date!""}</td>
					[#if doSchemaAllowStatus == "true"]
						<td width="20%">Delivery Order Date:</td>
					[#else]
						<td width="20%">Invoice Date:</td>
					[/#if]
					<td width="25%">${items[0].invoice_date!""}</td>
				</tr>
				<tr>
					[#if doSchemaAllowStatus == "true"]
						<td width="20%">Delivery Order No:</td>
					[#else]
						<td width="20%">Invoice No:</td>
					[/#if]
					<td width="25%">${items[0].invoice_no!""}</td>
					<td width="20%">Reference:</td>
					<td width="25%">${items[0].po_reference!""}</td>
				</tr>
				<tr>
					<td width="20%" valign="top">Supplier Name:</td>
					<td width="25%" >${items[0].supplier_name}<br/>
					                ${items[0].supplier_address!""}<br/>
					                ${items[0].city_name!""}<br/>
					                ${items[0].state_name!""}<br/>
					                ${items[0].country_name!""}</td>
					<td width="20%" valign="top">Supplier Tin No:</td>
					<td width="25%" valign="top">${items[0].supplier_tin!""}</td>
	    		</tr>
	    		<tr>
					<td width="20%">UserName:</td>
					<td width="25%">${items[0].user_name!""}</td>
					<td width="20%">Store Name:</td>
					<td width="25%">${items[0].store_name!""}</td>
				</tr>
 		 </table>
[/#escape]
 <table style="border-collapse: collapse;border-top: 1px solid silver;border-bottom: 1px solid silver;" class="report" width="100%" border="0">
			<tr>
				<th align="left">PkgSize</th>
				<th align="left">Expiry</th>
				<th align="left">Qty</th>
				<th align="left">BQty</th>
				[#if doAllowStatus == "false"]
					<th align="left">MRP</th>
					<th align="left">AdjMRP</th>
					<th align="left">Rate</th>
					<th align="left">TAX%TAXAmt</th>
					<th align="left">Disc</th>
					<th align="left">TT</th>
					<th align="right">Amount</th>
				[/#if]
				
			</tr>
<!--[#assign sno=1]
    [#assign total_billed_qty=0]
    [#assign total_bonus_qty=0]
    [#assign total_discount=0]
    [#assign total_tax_amt=0]
    [#assign total_amt=0]
    [#assign cess_tax_amount=0]
	[#list items as s]-->

[#escape x as x?html]
	         <tr>
	         		<td colspan="2">item/Medicine:</td>
	         		<td colspan="3" align="left">${s.medicine_name}</td>
	         </tr>

	         <tr>
	         		<td colspan="2">Batch No :</td>
	         		<td colspan="2" align="left">${s.batch_no}</td>
	         </tr>
			 <tr>
					<td>${s.grn_pkg_size?string("#")}</td>
					<td>${s.exp_date!""}</td>
					<td>${(s.billed_qty/s.grn_pkg_size)?string("##.00")}</td>
					<td>${(s.bonus_qty/s.grn_pkg_size)?string("##.00")}</td>
					[#if doAllowStatus == "false"]
						<td>${s.mrp}</td>
						<td>${s.adj_mrp}</td>
						<td>${s.cost_price}</td>
						<td>${s.tax_rate?string("##.00")}%${s.tax}</td>
						<td>${s.discount}</td>
						<td>${s.tax_type}</td>
						<td align="right">${s.amt}</td>
					[/#if]
			</tr>
[/#escape]
	<!--
	[#assign total_billed_qty=(total_billed_qty+s.billed_qty/s.grn_pkg_size)]
	[#assign total_bonus_qty=(total_bonus_qty+s.bonus_qty/s.grn_pkg_size)]
	[#assign total_tax_amt=(total_tax_amt+s.tax)]
	[#assign total_discount=(total_discount+s.discount)]
	[#assign total_amt=(total_amt+s.amt)]
	[#assign cess_tax_amount = (cess_tax_amount+s.cess_tax_amount)]
	[#assign sno=sno+1]
	[/#list] -->
</table>
[#if doAllowStatus == "false"]
<table style="width:100%" cellspacing="0" cellpadding="0">
			<!-- [#assign invdiscount = 0] -->

			[#if discount_type == "A"]
			 [#assign invdiscount = discount]
			[#else]
				 [#assign invdiscount = (total_amt + other_charges + transportation_charges + cess_tax_amount)* discount_per/100]
			[/#if]
		  <td style="width: 250px; padding-right: 3px" valign="top">
		     <fieldset class="fieldSetBorder">
			    <legend class="fieldSetLabel"><b><u>Grand Totals:</u></b></legend>
		        <table class="smallformtable">
		    <tr>
					<td width="55%" align="right" ><b>Billed Qty:</b></td>
	                <td width="30%" align="right">${total_billed_qty}</td>
	        </tr>
	        <tr>
	                <td width="25%" align="right" ><b>Bonus Qty:</b></td>
		        	<td width="30%" align="right">${total_bonus_qty}</td>
		    </tr>
		    <tr>
	                <td width="25%" align="right" ><b>Debit Amount:</b></td>
		        	<td width="30%" align="right">${debit_amt}</td>
		    </tr>
		    <tr>
	                <td width="25%" align="right" ><b>Tax Amount:</b></td>
		        	<td width="30%" align="right">${total_tax_amt}</td>
		    </tr>
		    <tr>
	                <td width="25%" align="right" ><b>Discount:</b></td>
		        	<td width="30%" align="right">${total_discount}</td>
		    </tr>
		     <tr>
	                <td width="25%" align="right" ><b>GRN Total Amount:</b></td>
		        	<td width="30%" align="right">${total_amt}</td>
		    </tr>
		    <tr>
		    		<td width="25%" align="right" ><b>Cess Tax Amount:</b></td>
		    		<td width="30%" align="right" >${cess_tax_amount}</td>
		    </tr>
		    <tr>
	                <td width="25%" align="right" ><b>Invoice Other Charges:</b></td>
		        	<td width="30%" align="right">${other_charges}</td>
		    </tr>
		    <tr>
	                <td width="25%" align="right" ><b>Invoice Transportation Charges:</b></td>
		        	<td width="30%" align="right">${transportation_charges}</td>
		    </tr>
		    <tr>
	                <td width="25%" align="right" ><b>Invoice Discount:</b></td>
		        	<td width="30%" align="right">${invdiscount}</td>
		    </tr>
		    <tr>
	                <td width="25%" align="right" ><b>TCS Amount:</b></td>
		        	<td width="30%" align="right">${tcs_amount}</td>
		    </tr>
		    <tr>
	                <td width="25%" align="right" ><b>Invoice Round Off Amt:</b></td>
		        	<td width="30%" align="right">${invoice_rnd_off}</td>
		    </tr>
		    <tr>
	                <td width="25%" align="right" ><b>Invoice Total Amount:</b></td>
		        	<td width="30%" align="right">${total_amt + invoice_rnd_off - invdiscount + cess_tax_amount + other_charges + transportation_charges + tcs_amount}</td>
		    </tr>
		        </table>
			</fieldset>
		</td>
		<td width="10%"></td>
		<td style="width: 314px;" valign="top">
			<fieldset class="fieldSetBorder">
				<legend class="fieldSetLabel"><b><u>Legend:</u></b></legend>
				<table class="smallformtable">
			<tr>
					<td width="2%"><b>TT:</b></td>
	                <td width="15%">Tax Basis</td>
	        </tr>
	        <tr>
	                <td width="2%"><b>MB:</b></td>
		        	<td width="15%" align="left">MRP Based (with bonus)</td>
		    </tr>
		    <tr>
	                <td width="2%"><b>M:</b></td>
		        	<td width="20%" align="left">MRP Based (without bonus)</td>
		    </tr>
		    <tr>
	                <td width="2%"><b>CB:</b></td>
		        	<td width="20%" align="left">Cost price based (with bonus)</td>
		    </tr>
		    <tr>
	                <td width="2%"><b>C:</b></td>
		        	<td width="20%" align="left">Cost price based (without bonus)</td>
		    </tr>
				</table>
		</fieldset>
	</td>

</table>
[/#if]

<!--
	[#setting time_format="HH:mm"]
	[#setting date_format="dd-MM-yyyy"]
	[#setting datetime_format="dd-MM-yyyy HH:mm"]
-->

<div align="center">
	<b><u>Purchase Order</u></b>
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
	    		    <td width="20%">Credit Period:</td>
	    		    <td width="25%">${items[0].credit_period!""}</td>
	    		</tr>
	    		<tr>
	    			<td width="25%">Indent Process No:</td>
	    		    <td width="30%">${indentNo!""}</td>
	    		    <td width="25%">Delivery Date:</td>
	    		    <td width="30%">${items[0].delivery_date!""}</td>
	    		</tr>
	    		<tr>
	    			<td width="25%">Auth/Amnd Dt:</td>
	    		    <td width="30%">${poBean.approved_time!""}</td>
	    		    <td></td>
	    		    <td></td>
	    		</tr>
    			<tr>
					<td width="20%">PO No: </td>
					<td width="25">${items[0].po_no}</td>
					<td width="20%">PO Date: </td>
					<td width="25%">${items[0].po_date}</td>
				</tr>
				<tr>
					<td width="20%">Quotation No:</td>
					<td width="25%">${items[0].qut_no!""}</td>
					<td width="20%">Quotation Date:</td>
					<td width="25%">${items[0].qut_date!""}</td>
				</tr>
				<tr>
					<td width="20%" valign="top">Supplier Address:</td>
					<td width="25%">${items[0].supplier_name}<br/>
					                ${items[0].supplier_address!""}<br/>
					                ${items[0].city_name!""}<br/>
					                ${items[0].state_name!""}<br/>
					                ${items[0].country_name!""}</td>
					<td width="20%">Reference:</td>
					<td width="25%">${items[0].reference!""}</td>
	    		</tr>
	    		<tr>
	    			<td width="20%">Store Name:</td>
					<td width="35%">${items[0].store_name}</td>
					<td width="20%"></td>
					<td width="25%"></td>
	    		</tr>
	    		<tr>
	    			<td width="20%">Dept. Name:</td>
					<td width="35%">${dept_name!""}</td>
					<td width="20%">Remarks:</td>
					<td width="25%">${poBean.remarks!}</td>
	    		</tr>
 		 </table>
[/#escape]
 <table style="border-collapse: collapse;border-top: 1px solid silver;border-bottom: 1px solid silver;" width="100%" border="0">
			<tr>
				<th align="left">#</th>
				<th align="left" width="20%">Item/Med</th>
				<th align="left" width="10%">Unt</th>
				<th align="right" width="10%">Qty</th>
				<th align="right" width="18%">BQty</th>
				<th align="right" width="15%">MRP</th>
				<th align="right" width="15%">AdjMRP</th>
				<th align="right" width="15%">Rate</th>
				<th align="right" width="15%">TAX%</th>
				<th align="right" width="15%">TAX</th>
				<th align="right" width="15%">Disc</th>
				<th align="right" width="15%">Amt</th>
			</tr>
<!--[#assign sno=1]
    [#assign total_qty=0]
    [#assign total_discount=0]
    [#assign total_tax_amt=0]
    [#assign total_amt=0]
	[#list items as s]-->
[#escape x as x?html]
			<tr>
				<td >${sno?string("#")}</td>
				<td width="10%">${s.medicine_name}</td>
				<td >[#if s.issue_units?has_content]${s.issue_units}[/#if]</td>
				<td align="right" >${(s.qty_req/s.po_pkg_size)?string("##.00")}</td>
				<td align="right" >${(s.bonus_qty_req/s.po_pkg_size)?string("##.00")}</td>
				<td align="right" >${s.mrp}</td>
				<td align="right" >${s.adj_mrp}</td>
				<td align="right" >${s.cost_price}</td>
				<td align="right" >${s.vat_rate?string("##.00")}%</td>
				<td align="right" >${s.vat}</td>
				<td align="right" >${s.discount}</td>
				<td align="right" >${s.med_total}</td>
			</tr>
[/#escape]
	<!--
	[#assign total_qty=(total_qty+s.qty_req)]
	[#assign total_tax_amt=(total_tax_amt+s.vat)]
	[#assign total_discount=(total_discount+s.discount)]
	[#assign total_amt=(total_amt+s.med_total+poBean.map.transportation_charges!)]
	[#assign sno=sno+1]
	[/#list] -->
			<tr></tr>
			<tr></tr>
		    <tr>
		        <td align="right" colspan="9">Totals:</td>
                <td align="right" width="15%">${total_tax_amt}</td>
		        <td align="right" width="15%">${total_discount}</td>
		        <td align="right" width="15%">${total_amt}</td>
		    </tr>
		    <tr>
		    	<td align="right" colspan="9">Discount:</td>
		    	<td></td>
		    	<td></td>
		    	<td align="right">${poBean.map.discount}</td>
		    </tr>
		    <tr>
		    	<td align="right" colspan="9">TCS Amt:</td>
		    	<td></td>
		    	<td></td>
		    	<td align="right">${poBean.map.tcs_amount}</td>
		    </tr>
		     <tr>
	                <td align="right" colspan="9">PO Transportation Charges:</td>
	                <td></td>
	                <td></td>
		        	<td align="right">${poBean.map.transportation_charges!}</td>
		    </tr>
		     <tr>
		        <td align="right" colspan="9">Round Off:</td>
		        <td></td>
                <td></td>
		        <td align="right">${poBean.map.round_off}</td>
		    </tr>
		     <tr>
		        <td align="right" colspan="9">Grand Total:</td>
                <td ></td>
		        <td ></td>
		        <td align="right">${total_amt - poBean.map.discount + poBean.map.round_off + poBean.map.tcs_amount}</td>
		    </tr>

</table>
[#escape x as x?html]
	 <table  width="100%" border="0">

	  			<tr>
	  				<td >Supplier Terms And Conditions:</td>
	  				<td>${items[0].supplier_terms!""}</td>
	  			</tr>
	  			<tr>
	  				<td>Hospital Terms And Conditions:</td>
	  				<td>${items[0].hospital_terms!""}</td>
	  			</tr>
	 </table>
 [/#escape]
 <table  width="100%" border="0">

             <tr>
                <td width="30%">User:${items[0].user_id!""} ${items[0].actual_po_date!""}</td>
  			 	<td></td>
  			 	<td></td>
             </tr>
   			 <tr>
  			 	<td width="20%">Prepared By:</td>
  			 	<td width="20%">Verified By:</td>
  			 	<td width="20%">Authorized By:</td>
  			</tr>
   </table>

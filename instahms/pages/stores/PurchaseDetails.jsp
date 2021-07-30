<div id="purchaseDialog" style="visibility: hidden; display:none"  >
	<div class="bd" style="text-align:center;">
		<div id="purchaseDialogExisting" style="max-height: 500px; overflow: auto">
			<b>Previous Purchase Prices</b>
			<table class="dataTable" width="100%" height="100%" 
				cellspacing="0" cellpadding="0" id="purchaseDialogTable">
				<tr>
					<th>Supplier</th>
					<th>Invoice Date</th>
					<th>GRN No.</th>
					<th style="text-align: right">MRP</th>
					<th style="text-align: right">Rate</th>
					<th style="text-align: right">Discount (%)</th>
					<th style="text-align: right">Discount (Amt)</th>
					<th style="text-align: right">Qty</th>
					<th style="text-align: right">Bonus</th>
					<th style="text-align: right">Tax Basis</th>
					<th style="text-align: right">Tax Rate</th>
				</tr>
				<tr style="display:none">
					<td name="supplier_name"></td>
					<td name="invoice_date"></td>
					<td name="grn_no"></td>
					<td name="mrp" class="amount" style="text-align: right"></td>
					<td name="cost_price" class="amount" style="text-align: right"></td>
					<td name="discount_per" class="amount" style="text-align: right"></td>
					<td name="discount" class="amount" style="text-align: right"></td>
					<td name="billed_qty" style="text-align: right"></td>
					<td name="bonus_qty" style="text-align: right"></td>
					<td name="tax_type" style="text-align: right"></td>
					<td name="tax_rate" style="text-align: right"></td>
				</tr>
			</table>
		</div>

		<div style="padding: 50px" id="purchaseDialogNoStock">
			<b>There are no purchase records for this item.</b>
		</div>

		<input type="button" id="purchaseDialogCloseBtn" value="Close" onclick="onPurchaseDialogClose()"/>
	</div>
</div>



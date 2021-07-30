<#--
     *  Hospital Specific MIS report for AJHRC. Not enabled in the app by default.
	 *  Parameters : fromDate, toDate
	 *  Output : Shows the various Hospital Parameters, distributed amongst the 4 Quarters.
 -->

<#function get obj key1 key2='' key3=''>
	<#if !(obj??)><#return 0></#if>
	<#if !(obj[key1]??)><#return 0></#if>

	<#if key2 == ''><#return obj[key1]></#if>
	<#if !(obj[key1][key2]??)><#return 0></#if>

	<#if key3 == ''><#return obj[key1][key2]></#if>
	<#if !(obj[key1][key2][key3]??)><#return 0></#if>

	<#return obj[key1][key2][key3]>
</#function>

<#function getStock closing period store purchases sales xferIn xferOut issues returns>
	<#return closing
			- get(purchases, period, store, '_total')
			- (get(xferIn, period, store) - get(xferOut, period, store))
			+ (get(issues, period, store) - get(returns, period, store))
			+ get(sales, period, store, '_total') >
</#function>

<#macro outputRow title value=0 format='#,##0.00'>
	<tr>
		<td class="labelInfo">${title}</td>
		<td class="number">${value?string(format)}</td>
	</tr>
</#macro>

<#macro outputEmptyRow>
	<tr>
		<td class="labelInfo"></td>
		<td class="number"></td>
	</tr>
</#macro>

<#assign prefquery = "SELECT hospital_name FROM generic_preferences">
<#assign prefMap = queryToDynaBean(prefquery)>
<#assign empty= {}>

<html xmlns="http://www.w3.org/1999/xhtml">
<head>
	<title>Monthly MIS Report - Insta HMS</title>
	<style>
		@page {
			size: A4 landscape;
			margin: 36pt 36pt 36pt 36pt;
		}
	body {
		font-family: Arial, sans-serif;
		font-size: 9pt;
	}

	table.outer {
		empty-cells: show;
		border-collapse: collapse;
		width: 100%;
	}

	table.outer td {
		padding: 2px 10px 2px 4px;
		border: 1px solid black;
		vertical-align: top;
		width: 33.33%;
	}

	table.inner {
		width: 100%;
	}

	table.inner td {
		border: none;
		width: 100%;
	}

	td.number {
		text-align: right;
	}

	td.labelInfo {
		padding:5px 0px 0px 0px;
		text-align: right;
	}

	div.heading {
		font-weight: bold;
		padding:0px 0px 0px 0px;
		text-align: left;
	}
	</style>
</head>

<#setting date_format="dd-MM-yyyy">
<body>
<#escape x as x?html>
<div align="center">
	<span style="font-size: 14pt;font-weight: bold;margin-top:10px;">${prefMap.hospital_name!?upper_case}<br/></span>
	<span style="font-size: 10pt;font-weight: bold;margin-top:10px;">MIS Report</span>
	<p style="margin-top:2px;">(${fromDate} - ${toDate})</p>
</div>

<div align="center">
<table class="outer">

	<tr>
		<td>
			<div class="heading">Patient Counts</div>

			<#assign regCountQuery = "SELECT count(*) as reg_count,
				CASE WHEN patient_category_id = 108 THEN 's' ELSE visit_type END as vtype, revisit
				FROM patient_registration
				WHERE reg_date BETWEEN ? AND ?
				GROUP BY vtype, revisit ">
			<#assign regCountResult = queryToDynaList(regCountQuery, fromDate, toDate)>
			<#assign regCounts = listBeanToMapMapNumeric(regCountResult, 'vtype', 'revisit', 'reg_count')>
			<#assign dischargeQuery = "SELECT sum(discharge) as count
				FROM patient_discharges WHERE discharge_date BETWEEN ? AND ?  ">
			<#assign dischCount = queryToDynaBean(dischargeQuery, fromDate, toDate)>
			<#assign daycareCountQuery = "SELECT count(*) as count
				FROM admission WHERE daycare_status  = 'Y'
				AND admit_date BETWEEN ? AND ? ">
			<#assign daycareCount = queryToDynaBean(daycareCountQuery, fromDate, toDate)>
			<#assign ipCountQuery = "SELECT count(*) as count
				FROM patient_registration WHERE visit_type = 'i' AND status = 'A' ">
			<#assign ipCount = queryToDynaBean(ipCountQuery)>

			<table class="inner">
				<@outputRow title='OP - New' value=get(regCounts,'o','N') format='#'/>
				<@outputRow title='OP - Repeat' value=get(regCounts,'o','Y') format='#'/>
				<@outputRow title='OP - Total' value=get(regCounts,'o','_total') format='#'/>
				<@outputEmptyRow/>
				<@outputRow title='OSP - New' value=get(regCounts,'s','N') format='#'/>
				<@outputRow title='OSP - Repeat' value=get(regCounts,'s','Y') format='#'/>
				<@outputRow title='OSP - Total' value=get(regCounts,'s','_total') format='#'/>
				<@outputEmptyRow/>
				<@outputRow title='IP - Admissions' value=get(regCounts,'i','_total') format='#'/>
				<@outputRow title='IP - Discharges' value=get(dischCount,'count') format='#'/>
				<@outputRow title='IP - Daycare' value=get(daycareCount,'count') format='#'/>
				<@outputRow title='IP - Total (Current)' value=get(ipCount,'count') format='#'/>
				<@outputEmptyRow/>
			</table>
		</td>

		<td>
			<div class="heading">Hospital Revenue</div>

			<#assign revenueQuery = "SELECT sum(amount+discount) AS amount, sum(discount) AS discount
				FROM bill_charge bc JOIN bill b USING (bill_no)
				WHERE bc.status != 'X' AND b.status IN ('F','C')
					AND b.restriction_type != 'P'
					AND date(b.finalized_date) BETWEEN ? AND ? ">
			<#assign rev = queryToDynaBean(revenueQuery, fromDate, toDate)>

			<#assign collQuery = "SELECT sum(amount) as amount, payment_type, payment_mode, recpt_type FROM (
					SELECT sum(r.amount) as amount, CASE WHEN r.tpa_id IS NOT NULL THEN 'S' ELSE receipt_type END AS payment_type,
					CASE WHEN payment_mode IN ('Cash', 'Cheque', 'Credit Card') THEN payment_mode
						ELSE 'Others' END AS payment_mode, CASE WHEN r.is_settlement THEN 'S' ELSE 'A' END AS recpt_type 
					FROM receipts r JOIN bill_receipts br ON (r.receipt_id = br.receipt_no)
						JOIN payment_mode_master pm ON (r.payment_mode_id = pm.mode_id)
						JOIN bill b USING (bill_no)
					WHERE b.restriction_type != 'P'
						AND date(display_date) BETWEEN ? AND ?
						GROUP BY payment_type, payment_mode, recpt_type, bill_type
					) as q
				GROUP BY payment_type, payment_mode, recpt_type" >
			<#assign collResult = queryToDynaList(collQuery, fromDate, toDate)>
			<#assign coll = listBeanToMapMapMapNumeric(collResult,
				'recpt_type', 'payment_type', 'payment_mode', 'amount')>

			<#assign untilTodayReceivableQuery = "SELECT
				sum(total_amount + insurance_deduction - total_receipts - total_claim - deposit_set_off) AS amount_due
				FROM bill WHERE status != 'X' AND status = 'F'
					AND restriction_type != 'P' AND date(finalized_date) IS NOT NULL " >
			<#assign untilTodayReceivable = queryToDynaBean(untilTodayReceivableQuery)>

			<#assign todaysCreditQuery = "SELECT sum(amount+discount) AS amount, bill_type
				FROM bill_charge bc JOIN bill b USING (bill_no)
				WHERE bc.status != 'X' AND b.status IN ('F','C')
					AND b.payment_status = 'U' AND b.restriction_type != 'P'
					AND date(b.finalized_date) = current_date
					GROUP BY bill_type " >
			<#assign todaysCredit = queryToDynaList(todaysCreditQuery)>
			<#assign todays = listBeanToMapNumeric(todaysCredit, 'bill_type', 'amount')>

			<#assign creditQuery = "SELECT sum(amount+discount) AS amount
				FROM bill_charge bc JOIN bill b USING (bill_no)
				WHERE bc.status != 'X' AND b.status IN ('F','C')
					AND b.payment_status = 'U' AND b.restriction_type != 'P' AND b.bill_type ='C'
					AND date(b.finalized_date) BETWEEN ? AND ? " >
			<#assign credit = queryToDynaBean(creditQuery, fromDate, toDate)>

			<table class="inner">
				<@outputRow title='Receivables as on today' value=get(untilTodayReceivable,'amount_due') />
				<@outputEmptyRow/>
				<@outputRow title='Total Business Generated' value=get(rev,'amount') />
				<@outputRow title='Discounts' value=get(rev,'discount') />
				<@outputRow title='Net Business Generated' value=get(rev,'amount') - get(rev,'discount') />
				<@outputEmptyRow/>
				<#list ["Cash", "Cheque", "Credit Card", "Others"] as mode>
					<@outputRow title='Amount Collected - ${mode}' value=get(coll,'_total','_total',mode) />
				</#list>
				<@outputRow title='Total Amount Collected' value=get(coll,'_total','_total','_total') />
				<@outputRow title='Refunds' value=(0-get(coll,'_total','F','_total')) />
				<@outputRow title='Net Amount Collected' value=(get(coll,'_total','_total','_total') + get(coll,'_total','F','_total')) />
				<@outputRow title='Todays Credit sales' value=get(todays,'C') />
				<@outputEmptyRow/>
				<@outputRow title='Credit sales' value=get(credit,'amount') />
				<@outputEmptyRow/>
			</table>
		</td>

		<td>
			<div class="heading">Pharmacy Revenue</div>

			<#assign phQuery = "SELECT sum(total_item_amount - discount + round_off) as amount,
					sum(discount + total_item_discount) as discount, b.bill_type, sm.type,
					COALESCE(payment_mode, 'Others') as payment_mode
				FROM store_sales_main sm
					JOIN bill b USING (bill_no)
					LEFT JOIN bill_receipts r ON (b.bill_type = 'P' AND r.bill_no = b.bill_no)
					LEFT JOIN payment_mode_master pm ON (r.payment_mode_id = pm.mode_id)
				WHERE date(sale_date) BETWEEN ? AND ?
				GROUP BY payment_mode, bill_type, type ">
			<#assign phQueryResult = queryToDynaList(phQuery, fromDate, toDate)>
			<#assign phAmount = listBeanToMapMapMapNumeric(phQueryResult,
				'bill_type', 'type', 'payment_mode', 'amount') >
			<#assign phDisc = listBeanToMapMapMapNumeric(phQueryResult,
				'bill_type', 'type', 'payment_mode', 'discount') >

			<table class="inner">
				<@outputRow title='Total Business Generated' value=get(phAmount,'_total','_total','_total')/>
				<#list ["Cash", "Cheque", "Credit Card", "Others"] as mode>
					<@outputRow title='Amount Collected - ${mode}' value=get(phAmount,'P','_total',mode) />
				</#list>
				<@outputRow title='Amount Collected - Total' value=get(phAmount,'P','_total','_total') />
				<@outputRow title='Discounts' value=get(phDisc,'_total','_total','_total') />
				<@outputRow title='Sales Returns' value=(0-get(phAmount,'_total','R','_total'))/>
				<@outputRow title='Amount Collected - Net' value=(get(phAmount,'C','_total','_total') + get(phAmount,'_total','R','_total'))/>
				<@outputRow title='Total Credit' value=get(phAmount,'C','_total','_total')/>
			</table>
		</td>
	</tr>


	<tr>
		<td>
			<div class="heading">Bill Counts</div>

			<#assign billClosedQuery = "SELECT count(*) as count FROM bill
				WHERE status = 'C' AND date(closed_date) BETWEEN ? AND ? ">
			<#assign billClosedCount = queryToDynaBean(billClosedQuery, fromDate, toDate)>

			<#assign billCancelledQuery = "SELECT count(*) as count FROM bill
				WHERE status = 'X' AND date(open_date) BETWEEN ? AND ? ">
			<#assign billCancelledCount = queryToDynaBean(billCancelledQuery, fromDate, toDate)>

			<#assign billsOpenQuery = "SELECT bill_type, count(*) as count FROM bill
				WHERE status = 'A' GROUP BY bill_type ">
			<#assign billsOpenResults = queryToDynaList(billsOpenQuery) >
			<#assign billsOpen = listBeanToMapBean(billsOpenResults, 'bill_type') >

			<table class="inner">
				<@outputRow title='Bills Closed' value=get(billClosedCount,'count') format='#' />
				<@outputRow title='Bills Cancelled' value=get(billCancelledCount,'count') format='#' />
				<@outputRow title='Bill Later - Open (Current)' value=get(billsOpen,'C','count') format='#' />
				<@outputRow title='Bill Now - Open (Current)' value=get(billsOpen,'P','count') format='#' />
			</table>
		</td>

		<td>
			<div class="heading">Cashflow Types</div>
			<#-- query is in Hospital Revenue section -->
			<table class="inner">
				<@outputRow title='Advances' value=get(coll,'A','_total','_total') />
				<@outputRow title='Settlements' value=get(coll,'S','R','_total') + get(coll,'S','S','_total') />
				<@outputRow title='Refunds' value=get(coll,'_total','F','_total') />
				<@outputRow title='' value=get(coll,'A','_total','_total') + get(coll,'S','R','_total') + get(coll,'S','S','_total') + get(coll,'_total','F','_total') />
				<@outputEmptyRow/>
			</table>
		</td>

		<td>
			<div class="heading">Accounts Expenses</div>

			<#assign purchasesQuery = "SELECT cash_purchase, store_id::text as store_id,
				sum(item_amt + other_charges + round_off + cess_tax_amt - discount)::numeric(10,2) as amount
			  FROM (
					SELECT sum(g.billed_qty/g.grn_pkg_size*g.cost_price - g.discount +g.tax) as item_amt,
						coalesce(i.discount, d.discount, 0) as discount,
						coalesce(i.round_off, -d.round_off, 0) as round_off,
						coalesce(i.other_charges, -d.other_charges, 0) as other_charges,
						coalesce(i.cess_tax_amt, 0) as cess_tax_amt,
						gm.store_id, coalesce(i.cash_purchase, 'N') as cash_purchase
					FROM store_grn_details g
						JOIN store_grn_main gm USING (grn_no)
						LEFT JOIN store_invoice i ON (gm.supplier_invoice_id = i.supplier_invoice_id)
						LEFT JOIN store_debit_note d ON (gm.debit_note_no = d.debit_note_no)
					WHERE date(grn_date) between ? AND ?
					GROUP BY grn_no, i.discount, d.discount, i.round_off, d.round_off, i.other_charges,
						d.other_charges, i.cess_tax_amt, gm.store_id, coalesce(i.cash_purchase, 'N')
					) as q
				GROUP BY store_id, cash_purchase order by store_id, cash_purchase desc ">
			<#assign purchasesResults = queryToDynaList(purchasesQuery, fromDate, toDate)>
			<#assign pur = listBeanToMapMapNumeric(purchasesResults, 'store_id', 'cash_purchase', 'amount')>

			<table class="inner">
				<@outputRow title='Cash Purchase - Stores' value=get(pur, '108', 'Y') />
				<@outputRow title='Cash Purchase - Pharmacy' value=get(pur, '186', 'Y')  />
				<@outputRow title='Cheque Purchase - Stores' value=get(pur, '108', 'N')  />
				<@outputRow title='Cheque Purchase - Pharmacy' value=get(pur, '186', 'N')  />
				<@outputRow title='Others' value=0 />
				<@outputEmptyRow/>
			</table>
		</td>
	</tr>


	<tr>
		<td rowspan="2">
			<div class="heading">Occupancy (Current)</div>

			<#assign occQuery = "SELECT bed_type, count(*)
				FROM bed_names JOIN ip_bed_details ipb using (bed_id)
				WHERE ipb.status != 'X' AND bed_state != 'F'
				GROUP by bed_type">
			<#assign occ = listBeanToMapNumeric(queryToDynaList(occQuery), 'bed_type', 'count')>

			<#assign icuQuery = "SELECT count(*) AS count
				FROM bed_names bn JOIN ip_bed_details ipb ON bn.bed_id = ipb.bed_id
				JOIN bed_types bt ON bt.bed_type_name = bn.bed_type
				WHERE ipb.status != 'X' AND bed_state != 'F' AND is_icu = 'Y' ">
			<#assign icuCount = queryToDynaBean(icuQuery)>

			<table class="inner">
				<@outputRow title='General Ward' value=get(occ, 'GENERAL') format='#'/>
				<@outputRow title='Semi Private' value=get(occ, 'SEMI-PVT') format='#'/>
				<@outputRow title='Private' value=get(occ, 'PRIVATE') format='#'/>
				<@outputRow title='Deluxe' value=get(occ, 'DELUXE') format='#'/>
				<@outputRow title='Super Deluxe' value=get(occ, 'SUPER DELUXE') format='#'/>
				<@outputRow title='Daycare' value=get(occ, 'DAY CARE') format='#'/>
				<@outputRow title='ICU' value=get(icuCount,'count') format='#'/>
			</table>
		</td>


		<#-- Queries for Hospital/Pharmacy Store ids -->
		<#assign purchasesQuery = "SELECT s.dept_id::text AS store_id,
				CASE WHEN gm.debit_note_no IS NOT NULL THEN 'D' ELSE 'P' END AS purchase_type,
				CASE WHEN date(gm.grn_date) > ? THEN 'after' ELSE 'within' END as period,
				sum (g.billed_qty/g.grn_pkg_size*g.cost_price - g.discount + g.tax + g.item_ced) as amount
			FROM store_grn_details g
				JOIN store_grn_main gm USING (grn_no)
				JOIN stores s ON (s.dept_id = gm.store_id)
			WHERE date(gm.grn_date) >= ?
			GROUP BY period, s.dept_id::text, purchase_type" >

		<#assign purchases = listBeanToMapMapMapNumeric(queryToDynaList(purchasesQuery, toDate, fromDate),
			'period', 'store_id', 'purchase_type', 'amount')>

		<#assign salesQuery = "SELECT s.dept_id::text AS store_id, sm.type as sale_type,
				CASE WHEN date(sm.sale_date) > ? THEN 'after' ELSE 'within' END as period,
				sum(sd.quantity*sd.pkg_cp/sd.package_unit) AS amount
			FROM store_sales_details sd
				JOIN store_sales_main sm USING(sale_id)
				JOIN stores s ON (s.dept_id = sm.store_id)
			WHERE date(sm.sale_date) >= ?
			GROUP BY period, s.dept_id::text, sale_type">

		<#assign sales = listBeanToMapMapMapNumeric(queryToDynaList(salesQuery, toDate, fromDate),
			'period', 'store_id','sale_type', 'amount')>

		<#assign issueQuery = "SELECT s.dept_id::text AS store_id,
				CASE WHEN date(im.date_time) > ? THEN 'after' ELSE 'within' END as period,
				sum((ssd.package_cp/ssd.stock_pkg_size)*i.qty) as amount
			FROM stock_issue_details i
				JOIN stock_issue_main im USING (user_issue_no)
				JOIN stores s ON (s.dept_id = im.dept_from)
				JOIN store_stock_details ssd ON
					(im.dept_from = ssd.dept_id AND i.medicine_id = ssd.medicine_id AND i.batch_no = ssd.batch_no)
			WHERE date(im.date_time) >= ?
			GROUP BY period, s.dept_id::text ">

		<#assign issues = listBeanToMapMapNumeric(queryToDynaList(issueQuery, toDate, fromDate),
			'period', 'store_id', 'amount')>

		<#assign returnQuery = "SELECT s.dept_id::text AS store_id,
				CASE WHEN date(im.date_time) > ? THEN 'after' ELSE 'within' END as period,
				sum((ssd.package_cp/ssd.stock_pkg_size)*i.qty) as amount
			FROM store_issue_returns_details i
				JOIN store_issue_returns_main im USING (user_return_no)
				JOIN stores s ON (s.dept_id = im.dept_to)
				JOIN store_stock_details ssd ON
					(im.dept_to = ssd.dept_id AND i.medicine_id = ssd.medicine_id AND i.batch_no = ssd.batch_no)
			WHERE date(im.date_time) >= ?
			GROUP BY period, s.dept_id::text ">

		<#assign returns = listBeanToMapMapNumeric(queryToDynaList(returnQuery, toDate, fromDate),
			'period', 'store_id', 'amount')>

		<#assign xferOutQuery = "SELECT sf.dept_id::text AS store_id,
				CASE WHEN date(tm.date_time) > ? THEN 'after' ELSE 'within' END as period,
				sum((ssd.package_cp/ssd.stock_pkg_size)*t.qty) as amount
			FROM store_transfer_details t
				JOIN store_transfer_main tm USING (transfer_no)
				JOIN stores sf ON (sf.dept_id = tm.store_from)
				JOIN stores st ON (st.dept_id = tm.store_to)
				LEFT JOIN store_stock_details ssd ON
					(tm.store_from = ssd.dept_id AND t.medicine_id = ssd.medicine_id AND t.batch_no = ssd.batch_no)
			WHERE date(tm.date_time) >= ?
				AND sf.dept_id != st.dept_id
			GROUP BY period, sf.dept_id::text">

		<#assign xferOut = listBeanToMapMapNumeric(queryToDynaList(xferOutQuery, toDate, fromDate),
			'period', 'store_id', 'amount')>

		<#assign xferInQuery = "SELECT st.dept_id::text AS store_id,
				CASE WHEN date(tm.date_time) > ? THEN 'after' ELSE 'within' END as period,
				sum((ssd.package_cp/ssd.stock_pkg_size)*t.qty) as amount
			FROM store_transfer_details t
				JOIN store_transfer_main tm USING (transfer_no)
				JOIN stores sf ON (sf.dept_id = tm.store_from)
				JOIN stores st ON (st.dept_id = tm.store_to)
				JOIN store_stock_details ssd ON
					(tm.store_from = ssd.dept_id AND t.medicine_id = ssd.medicine_id AND t.batch_no = ssd.batch_no)
			WHERE date(tm.date_time) >= ?
				AND sf.dept_id != st.dept_id
			GROUP BY period, st.dept_id::text">

		<#assign xferIn = listBeanToMapMapNumeric(queryToDynaList(xferInQuery, toDate, fromDate),
			'period', 'store_id',  'amount')>

		<#assign curStockQuery = "SELECT s.dept_id::text AS store_id,
				sum((package_cp/stock_pkg_size)*qty) as amount
			FROM store_stock_details ssd
				JOIN stores s ON (s.dept_id = ssd.dept_id)
			GROUP BY s.dept_id::text ">

		<#assign curStock = listBeanToMapNumeric(queryToDynaList(curStockQuery),
			'store_id', 'amount')>

		<#-- reverse calculate the closing stock from current stock and movement after toDate,
			and opening stock from closing stock and movement within fromDate and toDate
			Hospital Store id is 108
		-->

		<#assign hospStoreClosingStockStores = getStock(get(curStock, '108'), 'after', '108',
			purchases, sales, xferIn, xferOut, issues, returns) >

		<#assign hospStoreOpeningStockStores = getStock(hospStoreClosingStockStores, 'within', '108',
			purchases, sales, xferIn, xferOut, issues, returns) >

		<td rowspan="2">
			<div class="heading">Stock Summary - Hospital Store</div>
			<table class="inner">
				<@outputRow title='Opening Stock' value=hospStoreOpeningStockStores />
				<@outputRow title='Purchases' value=get(purchases,'within','108','P') />
				<@outputRow title='Transfer In from Pharmacy' value=get(xferIn,'within','183') />
				<@outputRow title='Transfer Out from Hospital Store' value=get(xferOut,'within','108') />
				<@outputEmptyRow/>
				<@outputRow title='Sales Returns' value=(0-get(sales,'within','108','R')) />
				<@outputRow title='Sales' value=get(sales,'within','108','S') />
				<@outputEmptyRow/>
				<@outputRow title='Issual Returns' value=get(returns,'within','108') />
				<@outputRow title='Issuals' value=get(issues,'within','108') />
				<@outputEmptyRow/>
				<@outputRow title='Purchase Returns' value=(0-get(purchases,'within','108','D')) />
				<@outputRow title='Closing Stock' value=hospStoreClosingStockStores />
			</table>
		</td>


		<#-- reverse calculate the closing stock from current stock and movement after toDate,
			and opening stock from closing stock and movement within fromDate and toDate
			Pharmacy General Store id is 186
		-->

		<#assign genPharmaClosingStockPharmacy = getStock(get(curStock, '186'), 'after', '186',
			purchases, sales, xferIn, xferOut, issues, returns) >

		<#assign genPharmaOpeningStockPharmacy = getStock(genPharmaClosingStockPharmacy, 'within', '186',
			purchases, sales, xferIn, xferOut, issues, returns) >

		<td>
			<div class="heading">Pharmacy General Store</div>
			<table class="inner">
				<@outputRow title='Opening Stock' value=genPharmaOpeningStockPharmacy />
				<@outputRow title='Transfer Out from Pharmacy G Store' value=get(xferOut,'within','186') />
				<@outputRow title='Purchases' value=get(purchases,'within','186','P') />
				<@outputRow title='Issuals' value=get(issues,'within','186') />
				<@outputRow title='Purchase Returns' value=(0-get(purchases,'within','186','D')) />
				<@outputRow title='Closing Stock' value=genPharmaClosingStockPharmacy />
				<@outputEmptyRow/>
			</table>
		</td>
	</tr>

	<#-- reverse calculate the closing stock from current stock and movement after toDate,
		and opening stock from closing stock and movement within fromDate and toDate
		Pharmacy Store id is 183
	-->

	<#assign pharmaClosingStockPharmacy = getStock(get(curStock, '183'), 'after', '183',
		purchases, sales, xferIn, xferOut, issues, returns) >

	<#assign pharmaOpeningStockPharmacy = getStock(pharmaClosingStockPharmacy, 'within', '183',
		purchases, sales, xferIn, xferOut, issues, returns) >

	<tr>
		<td>
			<div class="heading">Stock Summary - Pharmacy </div>
			<table class="inner">
				<@outputRow title='Opening Stock' value=pharmaOpeningStockPharmacy />
				<@outputRow title='Transfer In from Hospital Store' value=get(xferIn,'within','108') />
				<@outputRow title='Transfer Out from Pharmacy' value=get(xferOut,'within','183') />
				<@outputEmptyRow/>
				<@outputRow title='Sales Returns' value=(0-get(sales,'within','183','R')) />
				<@outputRow title='Counter Sales' value=get(sales,'within','183','S') />
				<@outputEmptyRow/>
				<@outputRow title='Issuals' value=get(issues,'within','183') />
				<@outputRow title='Issual Returns' value=get(returns,'within','183') />
				<@outputRow title='Returns to Pharmacy G Store' value=(0-get(purchases,'within','186','D')) />
				<@outputRow title='Closing Stock' value=pharmaClosingStockPharmacy />
				<@outputEmptyRow/>
			</table>
		</td>
	</tr>
</table>

</div>
</#escape>
</body>
</html>
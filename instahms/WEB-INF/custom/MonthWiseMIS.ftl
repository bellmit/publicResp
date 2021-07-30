<#--
   * Hospital Specific MIS report: Monthly format used by AJHRC
	 *  Parameters : fromDate, toDate
	 *  Output : Shows the various Hospital Parameters, distributed amongst the 4 Quarters.
 -->
<#function getNumber obj key>
	<#if obj??>
		<#if obj[key]??>
			<#return obj[key]>
		</#if>
	</#if>
	<#return 0>
</#function>

<#macro outputRow title beanMap format='#,##,###'>
	<tr>
		<th>${title}</th>
		<#assign total=0>
		<#list dateRange as month>
			<#assign value=getNumber(beanMap, month)>
			<td>${value?string(format)}</td>
			<#assign total = total+ value>
		</#list>
		<td>${total?string(format)}</td>
	</tr>
</#macro>

<#macro outputHeading title>
	<tr>
		<th colspan="${1 + dateRange?size}" style="font-style: italic">${title}</th>
		<th></th>
	</tr>
</#macro>

<#assign dateRange = getDatesInRange(fromDate, toDate, 'month')>
<#assign empty= {}>

<#-- Registration etc. counts -->
<#assign regCountQuery = "
  SELECT to_char(date_trunc('month', reg_date), 'Mon yyyy') AS month_dt,
	  count(*) as reg_count,
    sum(CASE WHEN visit_type = 'i' THEN 1 ELSE 0 END) as adm_count,
    sum(CASE WHEN revisit = 'Y' THEN 1 ELSE 0 END) as revisit_count
	FROM patient_registration
	WHERE reg_date::DATE BETWEEN ? AND ?
	GROUP BY month_dt
">
<#assign regCountResult = queryToDynaList(regCountQuery, fromDate, toDate)>
<#assign regCounts = listBeanToMapNumeric(regCountResult, 'month_dt', 'reg_count')>
<#assign admCounts = listBeanToMapNumeric(regCountResult, 'month_dt', 'adm_count')>
<#assign revisitCounts = listBeanToMapNumeric(regCountResult, 'month_dt', 'revisit_count')>

<#assign revenueByPatientTypeQuery = "
  SELECT to_char(date_trunc('month', finalized_date), 'Mon yyyy') AS month_dt,
	  sum(CASE WHEN bc.charge_group = 'REG' THEN amount ELSE 0 END) as reg_amount,
	  sum(CASE WHEN visit_type = 'i' THEN amount ELSE 0 END) as ip_amount,
	  sum(CASE WHEN visit_type != 'i' THEN amount ELSE 0 END) as op_amount,
	  sum(amount) as total_amount,
	  sum(discount) as discount
  FROM bill_charge bc
    JOIN bill b USING (bill_no)
	WHERE bc.status != 'X' AND b.status!='X' AND b.status != 'A'
    AND b.finalized_date::DATE BETWEEN ? AND ?
  GROUP BY month_dt
">
<#assign revenueByPatientTypeResult = queryToDynaList(revenueByPatientTypeQuery, fromDate, toDate)>
<#assign regCollection = listBeanToMapNumeric(revenueByPatientTypeResult , 'month_dt', 'reg_amount')>
<#assign ipBilling = listBeanToMapNumeric(revenueByPatientTypeResult , 'month_dt', 'ip_amount')>
<#assign opBilling = listBeanToMapNumeric(revenueByPatientTypeResult , 'month_dt', 'op_amount')>
<#assign totalBilling = listBeanToMapNumeric(revenueByPatientTypeResult , 'month_dt', 'total_amount')>
<#assign discount = listBeanToMapNumeric(revenueByPatientTypeResult , 'month_dt', 'discount')>


<#assign collectionQuery = "
  SELECT to_char(date_trunc('month', display_date), 'Mon yyyy') AS month_dt,
	  sum(amount) AS amount
 FROM bill_receipts
 WHERE display_date::DATE BETWEEN ? AND ?
 GROUP BY month_dt
">
<#assign collectionsResult = queryToDynaList(collectionQuery, fromDate, toDate)>
<#assign collections = listBeanToMapNumeric(collectionsResult, 'month_dt', 'amount')>

<#assign duesQuery = "
  SELECT to_char(date_trunc('month', finalized_date), 'Mon yyyy') AS month_dt,
	  sum ((total_amount - total_claim) - (total_receipts - primary_total_sponsor_receipts)) AS general_due,
	  sum (total_claim - primary_total_sponsor_receipts) AS corp_due
  FROM bill b
  WHERE finalized_date::DATE BETWEEN ? AND ?
  GROUP BY month_dt
">
<#assign duesResult = queryToDynaList(duesQuery, fromDate, toDate)>
<#assign generalDues = listBeanToMapNumeric(duesResult, 'month_dt', 'general_due')>
<#assign corpDues = listBeanToMapNumeric(duesResult, 'month_dt', 'corp_due')>

<#assign billsCancelledQuery = "
  SELECT to_char(date_trunc('month',mod_time), 'Mon yyyy') AS month_dt,
    count(distinct bill_no) AS count
	FROM bill_audit_log
  WHERE mod_time::DATE BETWEEN ? AND ?
	  AND field_name = 'status' AND new_value = 'X'
  GROUP BY month_dt
">
<#assign billsCancelledResult = queryToDynaList(billsCancelledQuery, fromDate, toDate)> 
<#assign billsCancelled = listBeanToMapNumeric(billsCancelledResult, 'month_dt', 'count')>

<#assign purchasesQuery = "
SELECT store_type_name, to_char(date_trunc('month', grn_date::date), 'Mon yyyy') AS month_dt,
  sum((SELECT sum(g.billed_qty/g.grn_pkg_size*g.cost_price - g.discount + g.tax + g.item_ced)
	  FROM store_grn_details g WHERE g.grn_no = gm.grn_no)
  + i.other_charges - i.discount + i.round_off + i.cess_tax_amt)
	AS inv_amount
FROM store_grn_main gm
  JOIN store_invoice i USING (supplier_invoice_id)
  JOIN stores s ON (s.dept_id = gm.store_id)
  JOIN store_type_master stm USING (store_type_id)
WHERE gm.grn_date::DATE BETWEEN ? AND ?
GROUP BY store_type_name, month_dt
">
<#assign purchases = queryToDynaList(purchasesQuery, fromDate, toDate)>
<#assign purchasesGrouped = listBeanToMapMapNumeric(purchases, 'store_type_name', 'month_dt', 'inv_amount')>

<#assign phSaleQuery = "
SELECT to_char(date_trunc('month', sale_date::date), 'Mon yyyy') AS month_dt,
  SUM(total_item_amount - discount + round_off) AS sale_amount
FROM store_sales_main
WHERE sale_date::DATE between ? AND ?
GROUP BY month_dt
">
<#assign phSalesResult = queryToDynaList(phSaleQuery, fromDate, toDate)>
<#assign phSales = listBeanToMapNumeric(phSalesResult, 'month_dt', 'sale_amount')>

<#assign phCollectionQuery = "
SELECT to_char(date_trunc('month', display_date::date), 'Mon yyyy') AS month_dt,
  sum(amount) AS receipts
FROM bill_receipts
  JOIN bill b USING (bill_no)
WHERE b.restriction_type = 'P'
AND display_date::DATE BETWEEN ? AND ?
GROUP BY month_dt
">
<#assign phCollectionResult = queryToDynaList(phCollectionQuery, fromDate, toDate)>
<#assign phCollection = listBeanToMapNumeric(phCollectionResult, 'month_dt', 'receipts')>

<#assign stockTransferQuery = "
SELECT to_char(date_trunc('month', date_time::date), 'Mon yyyy') AS month_dt, store_from::text,
  sum(std.qty * ssd.package_sp/ssd.stock_pkg_size) AS cost_value
FROM store_transfer_details std
  JOIN store_transfer_main stm USING (transfer_no)
	JOIN store_stock_details ssd ON (ssd.medicine_id = std.medicine_id AND std.batch_no = ssd.batch_no
	  AND ssd.dept_id = stm.store_from)
WHERE stm.store_from IN (183, 108)
  AND date_time::DATE between ? AND ?
GROUP BY month_dt, store_from::text
">
<#assign stockTransferResult = queryToDynaList(stockTransferQuery, fromDate, toDate)>
<#assign stockTransfer = listBeanToMapMapNumeric(stockTransferResult, 'store_from', 'month_dt', 'cost_value')>
<#assign stockTransferPharmacy = (stockTransfer['183'])!empty>
<#assign stockTransferHospital = (stockTransfer['108'])!empty>


<#assign stockValueQuery = "
SELECT to_char(date_trunc('month', cm.checkpoint_date::date-1), 'Mon yyyy') AS month_dt, c.store_id::text,
  SUM(c.qty * ssd.package_sp/ssd.stock_pkg_size) AS cost_value
FROM store_checkpoint_details c
  JOIN store_checkpoint_main cm USING (checkpoint_id)
	JOIN store_stock_details ssd ON (ssd.medicine_id = c.medicine_id AND c.batch_no = ssd.batch_no
	  AND ssd.dept_id = c.store_id)
WHERE c.store_id IN (183, 186, 108) AND cm.checkpoint_name like 'Monthly Automatic%'
  AND cm.checkpoint_date::date-1 between ? AND ?
GROUP BY month_dt, c.store_id::text
">
<#assign stockValueResult = queryToDynaList(stockValueQuery, fromDate, toDate)>
<#assign stockValue = listBeanToMapMapNumeric(stockValueResult, 'store_id', 'month_dt', 'cost_value')>
<#assign stockValuePharmacy = (stockValue['183'])!empty>
<#assign stockValuePharmacyGeneral = (stockValue['186'])!empty>
<#assign stockValueHospital = (stockValue['108'])!empty>

<#assign revenueByCategoryQuery = "
SELECT to_char(date_trunc('month', finalized_date), 'Mon yyyy') AS month_dt,
  CASE
    WHEN charge_head='SACOPE' AND act_description_id='OPID1137' THEN 'TMT'
		WHEN charge_head='SERSNP' AND act_description_id='SERV0086' THEN 'ECG'
		WHEN charge_head='SERSNP' AND act_description_id IN ('SERV0043','SERV0044','SERV0045') THEN 'Diet Charges'
		WHEN service_sub_group_id=434 THEN 'X-Ray'
		WHEN service_sub_group_id=431 THEN 'CT'
		WHEN service_sub_group_id=125 THEN 'Radiology Cathlab Procedure'
		WHEN service_sub_group_id=126 THEN 'Radiology Consumables'
		WHEN service_sub_group_id=124 THEN 'Radiology Film Extra'
		WHEN service_sub_group_id=106 THEN 'Mamography'
		WHEN service_sub_group_id=433 THEN 'Doppler'
		WHEN service_sub_group_id=432 THEN 'Ultrasound'
		WHEN service_sub_group_id=437 THEN 'Nuclear Medicine'
		WHEN service_sub_group_id=430 THEN 'MRI'
		WHEN service_sub_group_id IN (17,18) THEN 'Cardiology'
		WHEN service_sub_group_id=31 THEN 'Dialysis'
		WHEN service_sub_group_id=5 THEN 'Physio-Therapy'
		WHEN service_sub_group_id IN (427,428,429) THEN 'Lab'
		WHEN service_sub_group_id=488 THEN 'External Lab Service'
		WHEN service_sub_group_id=16 THEN 'Blood Products'
		ELSE 'Others'
	END AS category,
  sum(amount) as amount
FROM bill_charge bc
  JOIN bill b USING (bill_no)
WHERE bc.status != 'X' AND b.status!='X' AND b.status != 'A'
  AND b.finalized_date::DATE BETWEEN ? AND ?
 GROUP BY month_dt, category
">
<#assign revenueByCategoryResult = queryToDynaList(revenueByCategoryQuery, fromDate, toDate)>
<#assign revenueByCategory = listBeanToMapMapNumeric(revenueByCategoryResult,'category','month_dt','amount')>

<#assign prefquery = "SELECT hospital_name FROM generic_preferences">
<#assign prefMap = queryToDynaBean(prefquery)>

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
	}
	table.report {
		empty-cells: show;
		font-size: 9pt;
		border-collapse: collapse;
		border: 1px solid black;
	}

	table.report th {
		border: 1px solid black;
		padding: 2px 8px 2px 3px;
	}

	table.report td {
		padding: 2px 4px 2px 4px;
		border: 1px solid black;
	}

	table.report td.number {
		text-align: right;
	}

	table.report td.heading {
		font-weight: bold;
	}
		table.report th {text-align: left}
		table.report td {text-align: right}
	</style>
</head>

<#setting date_format="dd-MM-yyyy">
<body>
<#escape x as x?html>
<div align="center">
	<span style="font-size: 14pt;font-weight: bold;margin-top:10px;">${prefMap.hospital_name!?upper_case}<br/></span>
	<span style="font-size: 10pt;font-weight: bold;margin-top:10px;">Month-Wise MIS Report</span>
	<p style="margin-top:2px;">(${fromDate} - ${toDate})</p>
</div>

<div align="center">
<table class="report">
	<tr>
		<th>Module Name</th>
		<#list dateRange as month>
			<th>${month}</th>
		</#list>
		<th>Total</th>
	</tr>

	<@outputHeading title='Patient Module'/>

	<@outputRow title='Registration' beanMap=regCounts format='#'/>
	<@outputRow title='Admission' beanMap=admCounts format='#'/>
	<@outputRow title='Revisit' beanMap=revisitCounts format='#'/>
	<@outputRow title='Reg Collection' beanMap=regCollection />

	<@outputHeading title='Billing Module'/>

	<@outputRow title='IP Billing' beanMap=ipBilling />
	<@outputRow title='OP Billing' beanMap=opBilling />
	<@outputRow title='Total Billing' beanMap=totalBilling />
	<@outputRow title='Collection Amt.' beanMap=collections />
	<@outputRow title='General Outstanding' beanMap=generalDues />
	<@outputRow title='Corporate Outstanding' beanMap=corpDues />
	<@outputRow title='Discounts' beanMap=discount />

	<@outputRow title='Bills Cancelled' beanMap=billsCancelled format='#' />

	<@outputHeading title='Pharmacy Module'/>
	<@outputRow title='Purchases' beanMap=purchasesGrouped.PHARMACY!empty />
	<@outputRow title='Sales' beanMap=phSales />
	<@outputRow title='Collections' beanMap=phCollection />
	<@outputRow title='Dept. Issue' beanMap=stockTransferPharmacy />
	<@outputRow title='Stock stt. (Pharmacy)' beanMap=stockValuePharmacy />
	<@outputRow title='Stock stt. (Pharmacy Gen.)' beanMap=stockValuePharmacyGeneral />

	<@outputHeading title='Hospital Stores'/>
	<@outputRow title='Purchases' beanMap=purchasesGrouped.INVENTORY!empty />
	<@outputRow title='Dept. Issue' beanMap=stockTransferHospital />
	<@outputRow title='Stock stt.' beanMap=stockValueHospital />

	<@outputHeading title='Radiology'/>

	<#list ["X-Ray", "CT", "Radiology Cathlab Procedure", "Radiology Consumables", "Radiology Film Extra",
	    "Mamography", "Doppler", "Ultrasound", "Nuclear Medicine", "MRI", "TMT", "ECG", "Cardiology"]
      as category>
		<@outputRow title=category beanMap=(revenueByCategory[category])!empty />
	</#list>

	<@outputHeading title='Other'/>
	<#list ["Dialysis", "Physio-Therapy", "Lab", "External Lab Service", "Blood Products", "Diet Charges",
		  "Others"]
      as category>
		<@outputRow title=category beanMap=(revenueByCategory[category])!empty />
	</#list>
</table>


</div>
</#escape>
</body>
</html>


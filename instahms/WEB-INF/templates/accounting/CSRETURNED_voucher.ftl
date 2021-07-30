[#-- Avaiable tokens on record bean.

	user_return_no,
	final_amt => total of ((cost_price/grn_pkg_size)*qty - (discount/total_qty*qty)),
	supplier_id,
	con_return_date,
	supplier_name,
	center_code,
	dept_center_code => coalesce(cost_center_code, cost_center_code),
	purchase_store_account_prefix => CASE WHEN tax_name = 'CST' THEN purchases_store_cst_account_prefix ELSE purchases_store_vat_account_prefix END,
	purchases_cat_vat_account_prefix,
	purchases_cat_cst_account_prefix,
	final_tax => total of ((qty * tax)/billed_qty),
	vat_rate => CASE WHEN tax_name = 'CST' THEN cst_rate ELSE tax_rate END,
	consignment_invoice_no,
	tax_name

--]

[#import "/accounting/ledgertypes.ftl" as ledgertypes]

<!-- narration -->
[#assign narration = "Consignment Stock Return Id-Supplier Id: " + record.user_return_no + "-" + (record.supplier_name!)]

<!-- party account -->
[#assign prefix = partynames.supplier_ac_prefix!""]
[#assign suffix = partynames.supplier_ac_suffix!""]
[#assign ac_name = record.supplier_name]
[#if partynames.supplier_individual_accounts == 'N']
	[#assign ac_name = partynames.supplier_ac_name!record.supplier_name]
	[#assign supplier_ac_prefix = ""]
	[#assign supplier_ac_suffix = ""]
[/#if]

[#assign sup_ledgerAccount = "${prefix} ${ac_name} ${suffix}"]


[#assign taxAccName = ""]
[#assign catPrefix = ""]
[#assign storePrefix = record.purchase_store_account_prefix!]
[#if record.tax_name = 'VAT']
	[#assign taxAccName = specialnames.outgoing_vat!"Outgoing Vat"]
	[#assign catPrefix = record.purchases_cat_vat_account_prefix!]
[#elseif record.tax_name = 'CST']
	[#assign taxAccName = specialnames.incoming_cst!"Outgoing CST"]
	[#assign catPrefix = record.purchases_cat_cst_account_prefix!]
[/#if]

[#assign vatSuffix=""]
[#if (record.vat_rate!0) gt 0]
	[#assign vatSuffix = " @" + record.vat_rate + "%"]
[/#if]

[#assign purch_ret_ledgerAccount = storePrefix + catPrefix]
[#if (acprefs.separate_purcharse_acc_for_vat!'N') == 'Y']
	[#assign purch_ret_ledgerAccount = purch_ret_ledgerAccount + vatSuffix]
[/#if]
[#assign tax_ledgerAccount = taxAccName]
[#if (acprefs.separate_acc_for_out_vat!'N') == 'Y']
	[#assign tax_ledgerAccount = tax_ledgerAccount + vatSuffix]
[/#if]

<!-- finding a cost center -->
[#assign ie_cost_center_code = ""]
[#assign real_cost_center_code = ""]
[#if acprefs.cost_center_basis == 'Center Based']
	[#assign ie_cost_center_code = record.center_code!'']
	[#assign real_cost_center_code = record.center_code!'']
[#elseif acprefs.cost_center_basis == 'Dept Based']
	[#assign cost_center_code = record.dept_center_code!'']

	[#assign real_cost_center_code = "None"]
	[#assign ie_cost_center_code = cost_center_code!'None']
[/#if]

<!-- Done, export the voucher -->

[@voucher narration="${narration}" ; level]
	[@ledgerEntry debitOrCredit="C" ledgerType="${ledgertypes.purchases}" ledgerAccount="${purch_ret_ledgerAccount}" amount=record.final_amt
		centerCode="${ie_cost_center_code}" /]
	[@ledgerEntry debitOrCredit="C" ledgerType="${ledgertypes.purchases}" ledgerAccount="${tax_ledgerAccount}" amount=record.final_tax
		centerCode="${ie_cost_center_code}" /]
	[@ledgerEntry debitOrCredit="D" ledgerType="${ledgertypes.cv}" ledgerAccount="${sup_ledgerAccount}" amount=record.final_amt+record.final_tax
		centerCode="${real_cost_center_code}"/]
[/@voucher]
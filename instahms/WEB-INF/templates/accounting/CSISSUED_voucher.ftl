[#-- Avaiable tokens on record bean.

	issue_id,
	supplier_id,
	grn_amt,
	supplier_invoice_id,
	con_invoice_date,
	final_tax => tax amount,
	vat_rate,
	supplier_name,
	purchases_cat_vat_account_prefix,
	purchases_cat_cst_account_prefix,
	invoice_no,
	discount,
	round_off,
	tax_name,
	other_charges,
	po_no,
	po_reference,
	cess_tax_amt,
	center_code,
	dept_center_code,
	consignment_invoice_no,
	purchase_store_account_prefix

--]


[#import "/accounting/ledgertypes.ftl" as ledgertypes]

<!-- narration -->
[#assign narration = "Supplier-invoice-issueId: " + record.supplier_name + "_" + record.invoice_no + "_" + record.issue_id]
[#if record.po_no?has_content || record.po_reference?has_content]
	[#assign narration = narration + " against PO: "]
	[#assign narration = narration + (record.po_no!) + " " + (record.po_reference!)]
[/#if]

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

[#assign issue_ledgerAccount = storePrefix + catPrefix]
[#if (acprefs.separate_purcharse_acc_for_vat!'N') == 'Y']
	[#assign issue_ledgerAccount = issue_ledgerAccount + vatSuffix]
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
	[@ledgerEntry debitOrCredit="D" ledgerType="${ledgertypes.purchases}" ledgerAccount="${issue_ledgerAccount}" amount=record.grn_amt
		centerCode="${ie_cost_center_code}" /]
	[@ledgerEntry debitOrCredit="D" ledgerType="${ledgertypes.purchases}" ledgerAccount="${tax_ledgerAccount}" amount=record.final_tax
		centerCode="${ie_cost_center_code}" /]
	[@ledgerEntry debitOrCredit="C" ledgerType="${ledgertypes.cv}" ledgerAccount="${sup_ledgerAccount}" amount=record.grn_amt+record.final_tax
		centerCode="${real_cost_center_code}"/]
[/@voucher]
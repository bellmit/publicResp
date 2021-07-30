[#-- Avaiable tokens on record bean.

	supplier_id,
	supplier_name,
	debit_note_no,
	debit_note_date,
	remarks,
	amt => total of (billed_qty / grn_pkg_size * cost_price - discount)
	tax_amt => total of tax,
	SUM(item_ced) as ced_tax,
	tax_rate,
	discount_type,
	discount_per,
	discount,
	round_off,
	other_charges,
	tax_name,
	purchase_store_account_prefix => (case when tax_name = 'VAT' then s.purchases_store_vat_account_prefix	else purchases_store_cst_account_prefix end),
	purchases_cat_vat_account_prefix,
	purchases_cat_cst_account_prefix,
	center_code, center_id

--]

[#import "/accounting/ledgertypes.ftl" as ledgertypes]

<!-- narration -->
[#assign narration = "Supplier Returns SupplierId-DebitNo: "+record.supplier_id + "-" +record.debit_note_no]

<!-- party account-->
[#assign prefix = partynames.supplier_ac_prefix!""]
[#assign suffix = partynames.supplier_ac_suffix!""]
[#assign ac_name = record.supplier_name]
[#if partynames.supplier_individual_accounts == 'N']
	[#assign ac_name = partynames.supplier_ac_name!record.supplier_name]
	[#assign supplier_ac_prefix = ""]
	[#assign supplier_ac_suffix = ""]
[/#if]
[#assign sup_ledgerAccount = "${prefix} ${ac_name} ${suffix}"]

<!-- storewise and catwise accounts -->
[#assign storePrefix = record.purchase_store_account_prefix!]
[#assign tax_account = specialnames.outgoing_vat!]
[#assign catPrefix = record.purchases_cat_vat_account_prefix!]
[#if record.tax_name == 'CST']
	[#assign tax_account = specialnames.incoming_cst!]
	[#assign catPrefix = record.purchases_cat_cst_account_prefix!]
[/#if]

[#assign ced_ledgerAccount = storePrefix + (specialnames.outgoing_ced!)]
[#assign rf_ledgerAccount = specialnames.pharma_inv_round_off!"Pharmacy Invoice Round-Off"]
[#assign disc_ledgerAccount = specialnames.pharma_inv_discounts!"Pharmacy Invoice Discount"]
[#assign oc_ledgerAccount = specialnames.pharma_inv_other_charges!"Pharmacy Invoice Other Charges"]

[#assign vatSuffix=""]
[#if (record.tax_rate!0) gt 0]
	[#assign vatSuffix = " @" + record.tax_rate + "%"]
[/#if]

[#assign purch_ledgerAccount = storePrefix + catPrefix]
[#if (acprefs.separate_purcharse_acc_for_vat!'N') == 'Y']
	[#assign purch_ledgerAccount = purch_ledgerAccount + vatSuffix ]
[/#if]

[#assign tax_ledgerAccount = storePrefix + tax_account]
[#if (acprefs.separate_acc_for_out_vat!'N') == 'Y']
	[#assign tax_ledgerAccount = tax_ledgerAccount + vatSuffix ]
[/#if]

[#assign real_cost_center_code = ""]
[#assign ie_cost_center_code = ""]
[#if acprefs.cost_center_basis == 'Center Based']
	[#assign real_cost_center_code = record.center_code!'']
	[#assign ie_cost_center_code = record.center_code!'']
[#elseif acprefs.cost_center_basis == 'Dept Based']
	<!-- departments dont involve for invoices returns-->
	[#assign real_cost_center_code = "None"]
	[#assign ie_cost_center_code = "None"]
[/#if]

<!-- Done, export the voucher -->
[@voucher narration="${narration}" ; level]
	[#if level = "V"]
		[@ledgerEntry debitOrCredit="C" ledgerType="${ledgertypes.purchases}" ledgerAccount="${rf_ledgerAccount}" amount=record.round_off
			centerCode="${ie_cost_center_code}" /]
		[@ledgerEntry debitOrCredit="C" ledgerType="${ledgertypes.purchases}" ledgerAccount="${oc_ledgerAccount}" amount=record.other_charges
			centerCode="${ie_cost_center_code}"/]
		[@ledgerEntry debitOrCredit="D" ledgerType="${ledgertypes.purchases}" ledgerAccount="${disc_ledgerAccount}" amount=record.discount
			centerCode="${ie_cost_center_code}"/]
		[@ledgerEntry debitOrCredit="D" ledgerType="${ledgertypes.cv}" ledgerAccount="${sup_ledgerAccount}"
			amount=record.other_charges+record.round_off-record.discount centerCode="${real_cost_center_code}"/]
	[/#if]
	[@ledgerEntry debitOrCredit="C" ledgerType="${ledgertypes.purchases}" ledgerAccount="${purch_ledgerAccount}" amount=(record.amt*-1)
		centerCode="${ie_cost_center_code}" /]
	[@ledgerEntry debitOrCredit="D" ledgerType="${ledgertypes.purchases}" ledgerAccount="${tax_ledgerAccount}" amount=record.tax_amt
		centerCode="${ie_cost_center_code}" /]
	[@ledgerEntry debitOrCredit="D" ledgerType="${ledgertypes.purchases}" ledgerAccount="${ced_ledgerAccount}" amount=record.ced_tax
		centerCode="${ie_cost_center_code}" /]
	[@ledgerEntry debitOrCredit="D" ledgerType="${ledgertypes.cv}" ledgerAccount="${sup_ledgerAccount}" amount=(record.amt*-1) + record.tax_amt + record.ced_tax
		centerCode="${real_cost_center_code}"/]
[/@voucher]

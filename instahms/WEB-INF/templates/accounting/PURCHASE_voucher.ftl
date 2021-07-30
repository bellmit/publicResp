[#-- Avaiable tokens on record bean.
	grn_date,
	supplier_id,
	invoice_no,
	supplier_name,
	due_date,
	invoice_date,
	po_no,
	po_reference,
	status,
	inv_level_discount,
	round_off,
	other_charges,
	cess_tax_amt,
	tax_name,
	supplier_invoice_id,
	cost_price,
	billed_qty,
	grn_pkg_size,
	grn_amt => (cost_price*billed_qty/grn_pkg_size),
	discount,
	tax,
	ced_tax,
	center_code,
	center_id,
	cst_rate,
	tax_rate,
	purchases_store_cst_account_prefix,
	purchases_store_vat_account_prefix,
	purchases_cat_cst_account_prefix,
	purchases_cat_vat_account_prefix,
	debit_amt,

--]

[#import "/accounting/ledgertypes.ftl" as ledgertypes]

[#assign suffix = ""]
[#assign prefix = ""]

<!-- Narration -->
[#assign pono = record.po_no!]
[#assign poref = record.po_reference!]
[#assign narration = "Supplier-invoice:"]
[#assign narration = narration + " " + record.supplier_name + "_" + record.invoice_no]
[#if pono?has_content || poref?has_content]
	[#assign narration = narration + " against PO: " + pono + " " + poref]
[/#if]

[#assign taxAccName = ""]
[#assign purchAccName = ""]
[#assign storePrefix = ""]
[#assign tax_rate=0]
[#if record.tax_name = 'VAT']
	[#assign taxAccName = specialnames.outgoing_vat!"Outgoing Vat"]
	[#assign purchAccName = record.purchases_cat_vat_account_prefix!]
	[#assign storePrefix = record.purchases_store_vat_account_prefix!]
	[#assign tax_rate=record.tax_rate!0]
[#elseif record.tax_name = 'CST']
	[#assign taxAccName = specialnames.incoming_cst!"Outgoing CST"]
	[#assign purchAccName = record.purchases_cat_cst_account_prefix!]
	[#assign storePrefix = record.purchases_store_cst_account_prefix!]
	[#assign tax_rate=cst_rate!0]
[/#if]

[#assign vatSuffix=""]
[#if (tax_rate!0) gt 0]
	[#assign vatSuffix = " @" + tax_rate + "%"]
[/#if]

<!-- debitOrCredit : C or D -->
[#assign purch_ledgerAccount = (storePrefix!'') + (purchAccName!'') ]
[#if (acprefs.separate_purcharse_acc_for_vat!'N') == 'Y']
	[#assign purch_ledgerAccount = purch_ledgerAccount + vatSuffix ]
[/#if]
[#assign tax_ledgerAccount = (storePrefix!'') + taxAccName]
[#if (acprefs.separate_acc_for_out_vat!'N') == 'Y']
	[#assign tax_ledgerAccount = tax_ledgerAccount + vatSuffix ]
[/#if]
[#assign ced_ledgerAccount = (storePrefix!'') + (specialnames.outgoing_ced!"Outgoing CED")]

<!-- roundoff entry-->
[#assign rf_ledgerAccount = specialnames.pharma_inv_round_off!"Pharmacy Invoice Round-Off"]
<!-- Other Charges entry-->
[#assign oc_ledgerAccount = specialnames.pharma_inv_other_charges!"Pharmacy Invoice Other Charges"]
<!-- cess entry-->
[#assign cess_ledgerAccount = specialnames.pharma_cess!"Pharmacy Cess"]
<!-- discount entry-->
[#assign disc_ledgerAccount = specialnames.pharma_inv_discounts!"Pharmacy Invoice Discount"]
<!-- at the voucher level totalamt needs to be calculated like this, totalAmt+othercharge+cess+roundoff-discount -->

[#assign prefix = partynames.supplier_ac_prefix!""]
[#assign suffix = partynames.supplier_ac_suffix!""]
[#assign ac_name = record.supplier_name]
[#if partynames.supplier_individual_accounts == 'N']
	[#assign ac_name = partynames.supplier_ac_name!record.supplier_name]
	[#assign supplier_ac_prefix = ""]
	[#assign supplier_ac_suffix = ""]
[/#if]

[#assign sup_ledgerAccount = "${prefix} ${ac_name} ${suffix}"]

[#assign real_cost_center_code = ""]
[#assign ie_cost_center_code = ""]
[#if acprefs.cost_center_basis == 'Center Based']
	[#assign real_cost_center_code = record.center_code!'']
	[#assign ie_cost_center_code = record.center_code!'']
[#elseif acprefs.cost_center_basis == 'Dept Based']
	<!-- departments dont involve for invoices -->
	[#assign real_cost_center_code = "None"]
	[#assign ie_cost_center_code = "None"]
[/#if]

<!-- Done, export the voucher -->

[@voucher narration="${narration}" ; level]
	[#if level = "V"]
		[@ledgerEntry debitOrCredit="D" ledgerType="${ledgertypes.purchases}" ledgerAccount="${rf_ledgerAccount}" amount=record.round_off
			centerCode="${ie_cost_center_code}" /]
		[@ledgerEntry debitOrCredit="D" ledgerType="${ledgertypes.purchases}" ledgerAccount="${oc_ledgerAccount}" amount=record.other_charges
			centerCode="${ie_cost_center_code}"/]
		[@ledgerEntry debitOrCredit="D" ledgerType="${ledgertypes.purchases}" ledgerAccount="${cess_ledgerAccount}" amount=record.cess_tax_amt
			centerCode="${ie_cost_center_code}"/]
		[@ledgerEntry debitOrCredit="C" ledgerType="${ledgertypes.purchases}" ledgerAccount="${disc_ledgerAccount}" amount=record.inv_level_discount
			centerCode="${ie_cost_center_code}"/]
		[@ledgerEntry debitOrCredit="C" ledgerType="${ledgertypes.cv}" ledgerAccount="${sup_ledgerAccount}"
			amount=record.other_charges+record.cess_tax_amt+record.round_off-record.inv_level_discount centerCode="${real_cost_center_code}"/]
	[/#if]
	[@ledgerEntry debitOrCredit="D" ledgerType="${ledgertypes.purchases}" ledgerAccount="${purch_ledgerAccount}" amount=record.grn_amt-record.discount
		centerCode="${ie_cost_center_code}" /]
	[@ledgerEntry debitOrCredit="D" ledgerType="${ledgertypes.purchases}" ledgerAccount="${tax_ledgerAccount}" amount=record.tax
		centerCode="${ie_cost_center_code}" /]
	[@ledgerEntry debitOrCredit="D" ledgerType="${ledgertypes.purchases}" ledgerAccount="${ced_ledgerAccount}" amount=record.ced_tax
		centerCode="${ie_cost_center_code}" /]
	[@ledgerEntry debitOrCredit="C" ledgerType="${ledgertypes.cv}" ledgerAccount="${sup_ledgerAccount}" amount=record.grn_amt-record.discount + record.tax + record.ced_tax
		centerCode="${real_cost_center_code}"/]
[/@voucher]

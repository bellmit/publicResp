[#-- Avaiable tokens on record bean.

	bill_no,
	finalized_date,
	voucher_date => finalized date,
	is_tpa,
	primary_sponsor_name,
	secondary_sponsor_name,
	account_head_name => case when sbah.account_head_name is null then bah.account_head_name else sbah.account_head_name end,
	insurance_deduction,
	amount ,
	item_discount,
	primary_total_claim,
	secondary_total_claim,
	total_receipts,
	deposit_set_off => deposit_set_off,
	points_redeemed_amt,
	bill_type,
	visit_type,
	restriction_type,
	patient_name => COALESCE(pd.patient_name, isr.patient_name),
	last_name,
	salutation,
	patient_full_name => get_patient_full_name(salutation, COALESCE(patient_name, patient_name), middle_name, last_name),
	final_tax,
	discount,
	round_off,
	sale_id,
	vat_rate,
	type => (H (or) ISSUE (or) ISSUE_RETURN (or) S (or) R),
	mod_time,
	account_group,
	inter_comp_acc_group,
	inter_comp_account_group_id,
	charge_item_type => (Hospital Item (or) Pharmacy Credit Item (or) Store Issue Credit Item (or) Store Return Credit Item),
	med_category_id,
	sales_cat_vat_account_prefix,
	sales_store_vat_account_prefix,
	center_code,
	op_type,
	dept_center_code,
	visit_center_id,
	mr_no

--]

[#import "/accounting/ledgertypes.ftl" as ledgertypes]

[#assign prefix = ""]
[#assign suffix = ""]
[#assign counter_ledgerAccount = "Counter Receipts"]

[#if record.visit_type == 'i']
	[#assign prefix = acprefs.ip_income_acc_prefix!""]
	[#assign suffix = acprefs.ip_income_acc_suffix!""]
	[#assign counter_ledgerAccount = specialnames.counter_receipts_ip!"Counter Receipts"]
[#elseif record.visit_type == 'o']
	[#assign prefix = acprefs.op_income_acc_prefix!""]
	[#assign suffix = acprefs.op_income_acc_suffix!""]
	[#assign counter_ledgerAccount = specialnames.counter_receipts_op!"Counter Receipts"]
[#else]
	[#assign prefix = acprefs.others_income_acc_prefix!""]
	[#assign suffix = acprefs.others_income_acc_suffix!""]
	[#assign counter_ledgerAccount = specialnames.counter_receipts_others!"Counter Receipts"]
[/#if]

[#assign tpaPrefix = partynames.tpa_ac_prefix!""]
[#assign tpaSuffix = partynames.tpa_ac_suffix!""]
[#assign priTpaAcName = (record.primary_sponsor_name!'')]
[#assign secTpaAcName = (record.secondary_sponsor_name!'')]
[#if partynames.tpa_individual_accounts == 'N']
    [#assign priTpaAcName = "${partynames.tpa_ac_name!(record.primary_sponsor_name!'')}"]
    [#assign secTpaAcName = "${partynames.tpa_ac_name!(record.secondary_sponsor_name!'')}"]
    [#assign tpaPrefix = ""]
    [#assign tpaSuffix = ""]
[/#if]

[#assign priTpa_ledgerAccount= "${tpaPrefix} ${priTpaAcName} ${tpaSuffix}"]
[#assign secTpa_ledgerAccount= "${tpaPrefix} ${secTpaAcName} ${tpaSuffix}"]
[#assign hosp_disc_ledgerAccount = settings.hospital_discount_account!"Hospital Discounts"]
[#assign ph_disc_ledgerAccount = pharma_sales_discounts!"Pharmacy Sales Discount Account"]
[#assign deposit_ledgerAccount = specialnames.patient_deposits!"Deposit Account"]
[#assign points_ledgerAccount = specialnames.patient_points!"Patient Points Account"]
[#assign rf_ledgerAccount = pharm_sales_round_off_ac_name!"Round-Off Account"]

[#assign narration = "Bill No. " + record.bill_no]

[#if record.mr_no?has_content || record.patient_full_name?has_content]
	[#assign narration = narration + " ("]
[/#if]
[#if record.mr_no?has_content]
	[#assign narration = narration + " MR No. "+record.mr_no!]
[/#if]
[#if record.patient_full_name!?has_content]
	[#assign narration = narration + " :" + (record.patient_full_name!)]
[/#if]
[#if record.mr_no?has_content || record.patient_full_name?has_content]
	[#assign narration = narration + " )"]
[/#if]



[#assign vatSuffix = ""]
[#if (record.vat_rate!0) gt 0]
	[#assign vatSuffix = " @" + (record.vat_rate!0) + "%"]
[/#if]

[#assign storePrefix = record.sales_store_vat_account_prefix!'']
[#assign catPrefix = record.sales_cat_vat_account_prefix!'']

[#assign ch_ledgerAccount = ""]
[#assign vat_ledgerAccount = ""]

[#assign hosp_disc_amount = 0]
[#assign ch_amt = 0]
[#assign tax_amt = 0]
[#assign ph_disc_amt = 0]
[#assign hosp_disc_amt = 0]

[#assign ie_cost_center_code = ""]
[#assign real_cost_center_code = ""]
[#if acprefs.cost_center_basis == 'Center Based']
	[#assign ie_cost_center_code = record.center_code!'']
	[#assign real_cost_center_code = record.center_code!'']
[#elseif acprefs.cost_center_basis == 'Dept Based']
	[#assign cost_center_code = record.dept_center_code!'']
	[#if record.visit_type == 't']
		[#assign cost_center_code = depts[acprefs.income_dept_incoming_test].cost_center_code!]
	[#elseif record.visit_type == 'o' && record.op_type == 'O']
		[#assign cost_center_code = depts[acprefs.income_dept_osp].cost_center_code!]
	[/#if]
	[#assign real_cost_center_code = "None"]
	[#if (cost_center_code!'') == '']
		[#assign ie_cost_center_code = "None"]
	[#else]
		[#assign ie_cost_center_code = cost_center_code]
	[/#if]
[/#if]

[#assign total = record.amount] <!-- record.amount is included tax and excluded item discount. -->
[#assign item_discount = record.item_discount!0 ]

[#if record.charge_item_type == 'Pharmacy Credit Item']
	<!-- sales/(sales return) items added to hospital bill -->
	[#if acprefs.income_dept_pharmacy?has_content ]
		[#assign ie_cost_center_code = depts[acprefs.income_dept_pharmacy].cost_center_code!'']
	[/#if]

	[#assign ch_ledgerAccount = prefix + storePrefix + catPrefix + (record.account_head_name!'') + suffix]
	[#assign vat_ledgerAccount = storePrefix + (specialnames.incoming_vat!"Incoming Vat") ]

	[#if (acprefs.single_acc_for_item_and_bill_discounts!'N') == 'Y']
		[#assign ch_amt = ch_amt + item_discount]
		[#assign ph_disc_amt = item_discount]
	[/#if]

	[#if (acprefs.pharmacy_sales_acc_include_vat!'N') == 'Y']
		<!-- amounts including vat -->
		[#assign ch_amt = ch_amt + record.amount ]
	[#else]
		[#assign ch_amt = ch_amt + record.amount - record.final_tax]
		[#assign tax_amt = record.final_tax]

		<!-- separate ledgers for actual amount and tax amount -->
		[#if (acprefs.separate_sales_acc_for_vat!'N') == 'Y']
			[#assign ch_ledgerAccount = ch_ledgerAccount + vatSuffix ]
		[/#if]

		[#if (acprefs.separate_acc_for_in_vat!'N') == 'Y']
			[#assign vat_ledgerAccount = vat_ledgerAccount + vatSuffix]
		[/#if]

	[/#if]

	[#if (record.discount!0) != 0]
		[#assign ph_disc_amt = record.discount]
		[#assign total = record.discount*-1] <!-- sale bill level discounts needs to be subtracted from the total -->
	[/#if]

[#elseif record.charge_item_type == 'Store Issue Credit Item' || record.charge_item_type == 'Store Return Credit Item']
	<!-- issues/(issue return) items added to hospital bill-->
	[#assign ch_ledgerAccount = prefix + storePrefix + catPrefix + (record.account_head_name!'') + suffix ]
	<!-- amounts -->
	<!-- vat wise split not required for issues and returns -->
	[#if (acprefs.single_acc_for_item_and_bill_discounts!'N') == 'Y']
		[#assign ch_amt = ch_amt + item_discount]
	[/#if]
	[#assign ch_amt = ch_amt + record.amount]

[#else]

	<!-- direct charges added to hospital bill -->
	[#if (acprefs.single_acc_for_item_and_bill_discounts!'N') == 'Y']
		<!-- add all the item level discounts to the hospital discounts -->
		[#assign hosp_disc_amt = item_discount]
	[/#if]

	<!-- for hospital discounts amounts will be in negatives, so convert it to positive. -->
	[#if (settings.hospital_discount_account == (record.account_head_name!''))]
		[#assign hosp_disc_amt = record.amount*-1 ]
	[#else]
		[#assign ch_ledgerAccount = prefix + (record.account_head_name!'') + suffix]
		[#if (acprefs.single_acc_for_item_and_bill_discounts!'N') == 'Y']
			[#assign ch_amt = ch_amt + item_discount]
		[/#if]
		[#assign ch_amt = ch_amt + record.amount]
	[/#if]
[/#if]




[@voucher narration="${narration}" ; level]
	[#if level = "V"]
		[@ledgerEntry debitOrCredit="D" ledgerType="${ledgertypes.cv}" ledgerAccount="${deposit_ledgerAccount}" amount=record.deposit_set_off
			centerCode="${real_cost_center_code}" /]
		[@ledgerEntry debitOrCredit="D" ledgerType="${ledgertypes.cv}" ledgerAccount="${points_ledgerAccount}" amount=record.points_redeemed_amt
			centerCode="${real_cost_center_code}"/]
		[#if record.is_tpa]
			[@ledgerEntry debitOrCredit="D" ledgerType="${ledgertypes.cv}" ledgerAccount="${priTpa_ledgerAccount}" amount=record.primary_total_claim
				referenceName="${record.bill_no}, ${record.patient_full_name!}" centerCode="${real_cost_center_code}" /]
			[@ledgerEntry debitOrCredit="D" ledgerType="${ledgertypes.cv}" ledgerAccount="${secTpa_ledgerAccount}" amount=record.secondary_total_claim
				referenceName="${record.bill_no}, ${record.patient_full_name!}" centerCode="${real_cost_center_code}" /]
		[/#if]

		[@ledgerEntry debitOrCredit="D" ledgerType="${ledgertypes.cv}" ledgerAccount="${counter_ledgerAccount}"
			amount=(record.primary_total_claim+record.secondary_total_claim+record.deposit_set_off+record.points_redeemed_amt)*-1
			referenceName="${record.bill_no}, ${record.patient_full_name!}" centerCode="${real_cost_center_code}" /]
	[/#if]

	[@ledgerEntry debitOrCredit="C" ledgerType="${ledgertypes.ie}" ledgerAccount="${ch_ledgerAccount}" amount=ch_amt
		centerCode="${ie_cost_center_code}" /]
	[@ledgerEntry debitOrCredit="C" ledgerType="${ledgertypes.ie}" ledgerAccount="${vat_ledgerAccount}" amount=tax_amt
		centerCode="${ie_cost_center_code}" /]

	[@ledgerEntry debitOrCredit="D" ledgerType="${ledgertypes.ie}" ledgerAccount="${ph_disc_ledgerAccount}" amount=ph_disc_amt
		centerCode="${ie_cost_center_code}"/]
	[@ledgerEntry debitOrCredit="D" ledgerType="${ledgertypes.ie}" ledgerAccount="${hosp_disc_ledgerAccount}" amount=hosp_disc_amt
		centerCode="${ie_cost_center_code}" /]

	[@ledgerEntry debitOrCredit="C" ledgerType="${ledgertypes.ie}" ledgerAccount="${rf_ledgerAccount}" amount=record.round_off /]
	[@ledgerEntry debitOrCredit="D" ledgerType="${ledgertypes.cv}" ledgerAccount="${counter_ledgerAccount}" amount=total+record.round_off
		referenceName="${record.bill_no}, ${record.patient_full_name!}" centerCode="${real_cost_center_code}"/]

[/@voucher]

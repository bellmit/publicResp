[#-- Avaiable tokens on record bean.

	receipt_no,
	payment_reference,
	insurance_co_id,
	tpa_id,
	display_date,
	amount,
	counter,
	bank_name,
	reference_no,
	remarks,
	username,
	mod_time,
	payment_mode_id,
	card_type_id,
	bank_batch_no,
	card_auth_code,
	card_holder_name,
	currency_id,
	exchange_rate,
	exchange_date,
	currency_amt,
	card_expdate,
	card_number,
	counter_no,
	tpa_name,
	insurance_co_name,
	account_group_id,
	spl_account_name,
	bank_required,
	ref_required,
	center_code,
	center_id

--]

[#import "/accounting/ledgertypes.ftl" as ledgertypes]
[#include "/accounting/AccountingMacros.ftl"]

<!-- narration -->
[#assign narration = "Receipt No. " + record.receipt_no + " against Sponsor Remittance "]
[#if (record.ref_required!'') == 'Y' && record.reference_no?has_content]
	[#assign narration = narration + ", Reference No. " + (record.reference_no!)]
[/#if]
[#assign narration = narration + " (Payment Ref. " + (record.payment_reference!) + ")." + " " + (record.remarks!?html)]

<!-- party account -->
[#assign tpaPrefix = partynames.tpa_ac_prefix!""]
[#assign tpaSuffix = partynames.tpa_ac_suffix!""]
[#assign ac_name = (record.tpa_name!'')]
[#if partynames.tpa_individual_accounts == 'N']
    [#assign ac_name = "${partynames.tpa_ac_name!(record.tpa_name!'')}"]
    [#assign tpaPrefix = ""]
    [#assign tpaSuffix = ""]
[/#if]
[#assign party_ledgerAccount = "${tpaPrefix} ${ac_name} ${tpaSuffix}"]

[#assign mode_ledgerAccount = "${pm_mode_spl_account(record, record.spl_account_name!)}"]

<!-- Bank / Cash Ledger Type -->
[#if (record.bank_required!'') == "Y"]
	[#assign payment_ledgerType=ledgertypes.bank]
[#else]
	[#assign payment_ledgerType=ledgertypes.cash]
[/#if]

<!-- finding cost center -->
[#assign real_cost_center_code = '']
[#if acprefs.cost_center_basis == 'Center Based']
	[#assign real_cost_center_code = record.center_code!]
[#elseif acprefs.cost_center_basis == 'Dept Based']
	[#assign real_cost_center_code = 'None']
[/#if]

[@voucher narration="${narration}" ; level]
	[@ledgerEntry debitOrCredit="D" ledgerType="${payment_ledgerType}" ledgerAccount="${mode_ledgerAccount}" amount=record.amount
			centerCode="${real_cost_center_code}" /]
	[@ledgerEntry debitOrCredit="C" ledgerType="${ledgertypes.cv}" ledgerAccount="${party_ledgerAccount}" amount=record.amount
			centerCode="${real_cost_center_code}" /]
[/@voucher]


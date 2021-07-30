[#-- Avaiable tokens on record bean.

	payment_type,
	receipt_no,
	amount,
	display_date,
	counter,
	payment_mode,
	bank_name,
	reference_no,
	username,
	remarks,
	status,
	mr_no,
	salutation,
	patient_name,
	last_name,
	dob,
	patient_gender,
	patient_full_name,
	payment_mode_account,
	bank,
	ref_required,
	bank_required,
	center_code

--]

[#import "/accounting/ledgertypes.ftl" as ledgertypes]
[#include "/accounting/AccountingMacros.ftl"]

[#assign narration = "Patient Deposit Refund No. "]
[#if record.payment_type == 'DR']
	[#assign narration = "Patient Deposit Receipt No. "]
[/#if]

[#assign narration = narration + record.receipt_no ]
[#assign narration = narration + " against MR No. " + record.mr_no + " (" + record.patient_full_name + "). " + (record.remarks!?html)]

<!-- Bank / Cash Ledger Type -->
[#if (record.bank_required!'') == "Y"]
	[#assign payment_ledgerType=ledgertypes.bank]
[#else]
	[#assign payment_ledgerType=ledgertypes.cash]
[/#if]

[#assign deposit_ledgerAccount = specialnames.patient_deposits!"Patient Deposits A/C"]
[#assign mod_ledgerAccount = "${pm_mode_spl_account(record, record.payment_mode_account!)!}"]

<!-- finding cost center -->
[#assign real_cost_center_code = '']
[#if acprefs.cost_center_basis == 'Center Based']
	[#assign real_cost_center_code = record.center_code!]
[#elseif acprefs.cost_center_basis == 'Dept Based']
	[#assign real_cost_center_code = 'None']
[/#if]

[@voucher narration="${narration}" ; level]
	[@ledgerEntry debitOrCredit="D" ledgerType="${payment_ledgerType}" ledgerAccount="${mod_ledgerAccount}" amount=record.amount
			centerCode="${real_cost_center_code}" /]
	[@ledgerEntry debitOrCredit="C" ledgerType="${ledgertypes.cv}" ledgerAccount="${deposit_ledgerAccount}" amount=record.amount
			centerCode="${real_cost_center_code}" /]
[/@voucher]
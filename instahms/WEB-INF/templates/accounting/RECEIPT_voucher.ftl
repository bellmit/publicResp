[#-- Avaiable tokens on record bean.

	bill_receipts.* => all columns of bill_receipts table available for customization,
	voucher_date => display date,
	bill_no,
	bill_type,
	visit_type,
	status => bill status,
	finalized_date,
	is_tpa,
	no_of_receipts,
	payment_mode,
	card_type,
	patient_name => coalesce(patient_name, patient_name, customer_name),
	patient_full_name => get_patient_full_name(salutation, coalesce(patient_name, patient_name, customer_name), middle_name, last_name),
	payment_mode_account,
	last_name,
	salutation,
	tpa_name => (case when sponsor_index='P' then tpa_name else tpa_name end),
	counter_id,
	counter_name,
	bank_batch_no,
	restriction_type,
	bank,
	ref_required,
	bank_required,
	center_code,
	mr_no

--]

[#import "/accounting/ledgertypes.ftl" as ledgertypes]
[#include "/accounting/AccountingMacros.ftl"]

<!-- narration -->
[#assign receiptType = " (Settlement) "]
[#if record.recpt_type == 'A']
	[#assign receiptType = " (Advance) " ]
[/#if]
[#assign refNo = ""]
[#if (record.ref_required!'') == 'Y' && record.reference_no?has_content]
	[#assign refNo = ", Reference No. " + record.reference_no!]
[/#if]
[#assign mrNoLabel = ""]
[#if record.mr_no?has_content]
	[#assign mrNoLabel = "MR No." + record.mr_no + ": "]
[/#if]
[#assign narration = ""]

[#if record.payment_type == "R" || record.payment_type == "S"]
	[#assign narration = "Receipt No. " + record.receipt_no + " " + receiptType + " against bill " + record.bill_no ]
[#else]
	[#assign narration = "Refund No. " + record.receipt_no + " " + " against bill " + record.bill_no ]
[/#if]

[#assign narration = narration + refNo]
[#assign narration = narration + " (" + mrNoLabel + (record.patient_full_name!) + ")."]
[#assign narration = narration + (record.remarks!?html)]

<!-- billAllocation's referenceName-->
[#assign refName = record.counter_name!'']
[#if record.bank_batch_no?has_content]
	[#if refName?has_content]
		[#assign refName = refName + ": "]
	[/#if]
	[#assign refName = refName + record.bank_batch_no!]
[/#if]

[#if acprefs.bill_reference == 'bill_no']
	<!-- override the ref name with bill no and patient name -->
	[#assign refName = record.bill_no + ", " + record.patient_full_name ]
[/#if]

[#assign tds_ledgerAccount = tds_receipts!'TDS A/C (Receipts)' ]
[#assign mode_ledgerAccount = "${pm_mode_spl_account(record, record.payment_mode_account!)!}"]

<!-- Bank / Cash Ledger Type -->
[#if (record.bank_required!'') == "Y"]
[#assign payment_ledgerType=ledgertypes.bank]
[#else]
[#assign payment_ledgerType=ledgertypes.cash]
[/#if]

<!-- counter account -->
[#assign counter_ledgerAccount = ""]
[#if record.restriction_type == 'P']
	[#assign counter_ledgerAccount = specialnames.pharma_receipts!]
[#elseif record.visit_type == 'i']
	[#assign counter_ledgerAccount = specialnames.counter_receipts_ip!]
[#elseif record.visit_type == 'o']
	[#assign counter_ledgerAccount = specialnames.counter_receipts_op!]
[#else]
	[#assign counter_ledgerAccount = specialnames.counter_receipts_others!]
[/#if]

[#assign tpaPrefix = partynames.tpa_ac_prefix!""]
[#assign tpaSuffix = partynames.tpa_ac_suffix!""]
[#assign ac_name = (record.tpa_name!'')]
[#if partynames.tpa_individual_accounts == 'N']
    [#assign ac_name = "${partynames.tpa_ac_name!(record.tpa_name!'')}"]
    [#assign tpaPrefix = ""]
    [#assign tpaSuffix = ""]
[/#if]
[#assign party_ledgerAccount = "${tpaPrefix} ${ac_name} ${tpaSuffix}"]

<!-- finding cost center -->
[#assign real_cost_center_code = '']
[#if acprefs.cost_center_basis == 'Center Based']
	[#assign real_cost_center_code = record.center_code!]
[#elseif acprefs.cost_center_basis == 'Dept Based']
	[#assign real_cost_center_code = 'None']
[/#if]

[@voucher narration="${narration}" ; level]
	[#if record.payment_type == 'R']
		<!-- receipts voucher entries -->
		[@ledgerEntry debitOrCredit="D" ledgerType="${payment_ledgerType}" ledgerAccount="${mode_ledgerAccount}" amount=record.amount
			centerCode="${real_cost_center_code}" /]
		[@ledgerEntry debitOrCredit="C" ledgerType="${ledgertypes.cv}" ledgerAccount="${counter_ledgerAccount}" amount=record.amount
			centerCode="${real_cost_center_code}" referenceName="${refName}" /]

	[#elseif record.payment_type == 'S']
		<!-- sponsor receipts voucher entries -->
		[@ledgerEntry debitOrCredit="D" ledgerType="${payment_ledgerType}" ledgerAccount="${mode_ledgerAccount}" amount=record.amount
			centerCode="${real_cost_center_code}" /]
		[@ledgerEntry debitOrCredit="C" ledgerType="${ledgertypes.cv}" ledgerAccount="${party_ledgerAccount}" amount=record.amount+record.tds_amt
			centerCode="${real_cost_center_code}" /]
		[@ledgerEntry debitOrCredit="D" ledgerType="${ledgertypes.al}" ledgerAccount="${tds_ledgerAccount}" amount=record.tds_amt
			centerCode="${real_cost_center_code}" /]

	[#else]
		<!-- refund voucher entries for refunds amounts will be in negative -->
		[@ledgerEntry debitOrCredit="D" ledgerType="${payment_ledgerType}" ledgerAccount="${mode_ledgerAccount}" amount=record.amount
			centerCode="${real_cost_center_code}" /]
		[@ledgerEntry debitOrCredit="C" ledgerType="${ledgertypes.cv}" ledgerAccount="${counter_ledgerAccount}" amount=record.amount
			centerCode="${real_cost_center_code}" referenceName="${refName}" /]
	[/#if]
[/@voucher]
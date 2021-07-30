[#-- Avaiable tokens on record bean.

	voucher_no,
	amount,
	tds_amount,
	reference_no,
	payee_name,
	payment_type,
	payment_mode,
	card_type,
	name,
	mod_time,
	date,
	voucher_date,
	voucher_category,
	counter,
	bank,
	spl_account_name,
	ref_required,
	bank_required,
	center_code,
	tax_amount,
	round_off
--]

[#import "/accounting/ledgertypes.ftl" as ledgertypes]
[#include "/accounting/AccountingMacros.ftl"]

<!-- Narration -->
[#assign narration = "Payment Returns Voucher No."]
[#assign narration = narration + " " + record.voucher_no]
[#if (record.ref_required!'') == "Y" && record.reference_no?has_content]
	[#assign narration = narration + ", Reference No. " + record.reference_no]
[/#if]

[#if record.payment_type == "C"]
	[#assign narration = narration + " ( Paid to " + record.payee_name + ")"]
[/#if]
[#if record.payment_type == "S"]
	[#assign narration = narration + " ( Paid to " + record.name + ")"]
[/#if]

<!-- Bank / Cash Dr -->
[#if (record.bank_required!'') == "Y"]
	[#assign bc_ledgerType="BANK ACCOUNT"]
[#else]
	[#assign bc_ledgerType="CASH ACCOUNT"]
[/#if]
[#assign bc_ledgerAccount = "${pm_mode_spl_account(record, record.spl_account_name!)}"]

<!-- Party a/c Debit -->
[#assign prefix = ""]
[#assign suffix = ""]
[#assign ac_name=record.name]

[#if record.payment_type == 'D']
[#assign ac_name = record.name]
[#assign prefix = partynames.doctor_op_ac_prefix!""]
[#assign suffix = partynames.doctor_op_ac_suffix!""]
[#if partynames.doctor_individual_accounts == 'N']
	[#assign prefix = ""]
	[#assign suffix = ""]
	[#assign ac_name = partynames.doctor_ac_name!record.name]
[/#if]
[/#if]

[#if record.payment_type=='P']
[#assign ac_name = record.name]
[#assign prefix = partynames.prescribingdoctor_op_ac_prefix!""]
[#assign suffix = partynames.prescribingdoctor_op_ac_suffix!""]
[#if partynames.prescribingdoctor_individual_accounts == 'N']
	[#assign prefix = ""]
	[#assign suffix = ""]
	[#assign ac_name = partynames.prescribingdoctor_ac_name!record.name]
[/#if]
[/#if]

[#if record.payment_type=='R']
[#assign ac_name = record.name]
[#assign prefix = partynames.referral_op_ac_prefix!""]
[#assign suffix = partynames.referral_op_ac_suffix!""]
[#if partynames.referral_individual_accounts == 'N']
	[#assign prefix = ""]
	[#assign suffix = ""]
	[#assign ac_name = partynames.referral_ac_name!record.name]
[/#if]
[/#if]

[#if record.payment_type=='C']
	[#assign prefix = partynames.misc_ac_prefix!""]
	[#assign suffix = partynames.misc_ac_suffix!""]
	[#assign ac_name = record.name]
	[#if partynames.misc_individual_accounts == 'N']
		[#assign ac_name = partynames.misc_ac_name!record.name]
		[#assign prefix = ""]
		[#assign suffix = ""]
	[/#if]
[/#if]

[#if record.payment_type=='O']
	[#assign prefix = partynames.outhouse_ac_prefix!""]
	[#assign suffix = partynames.outhouse_ac_suffix!""]
	[#assign ac_name = record.name]
	[#if partynames.outhouse_individual_accounts == 'N']
		[#assign ac_name = partynames.outhouse_ac_name!record.name]
		[#assign prefix = ""]
		[#assign suffix = ""]
	[/#if]
[/#if]

[#if record.payment_type=='S']
[#assign prefix = partynames.supplier_ac_prefix!""]
[#assign suffix = partynames.supplier_ac_suffix!""]
[#assign ac_name = record.name]
[#if partynames.supplier_individual_accounts == 'N']
	[#assign ac_name = partynames.supplier_ac_name!record.name]
	[#assign supplier_ac_prefix = ""]
	[#assign supplier_ac_suffix = ""]
[/#if]
[/#if]

[#assign payee_ledgerAccount = "${prefix} ${ac_name} ${suffix}"]

<!-- TDS a/c -->
[#assign tds_account = specialnames.tds_payments!"TDS A/C (Payments)"]

<!-- finding cost center -->
[#assign real_cost_center_code = ""]
[#if acprefs.cost_center_basis == 'Center Based']
	[#assign real_cost_center_code = record.center_code!'']
[#elseif acprefs.cost_center_basis == 'Dept Based']
	[#assign real_cost_center_code = 'None']
[/#if]
<!-- Done, export the voucher entry -->

[@voucher narration="${narration}"]
	<!-- Item level entries -->
	[@ledgerEntry debitOrCredit="D" ledgerType="${bc_ledgerType}" ledgerAccount="${bc_ledgerAccount}" amount=record.amount*-1
		centerCode="${real_cost_center_code}" /]
	[@ledgerEntry debitOrCredit="D" ledgerType="${ledgertypes.al}" ledgerAccount="${tds_account}" amount=record.tds_amount*-1
		centerCode="${real_cost_center_code}" /]
	[@ledgerEntry debitOrCredit="C" ledgerType="${ledgertypes.cv}" ledgerAccount="${payee_ledgerAccount}" amount=record.amount*-1+record.tds_amount*-1
		centerCode="${real_cost_center_code}"/]
[/@voucher]

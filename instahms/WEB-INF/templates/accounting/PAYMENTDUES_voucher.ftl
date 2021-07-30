[#-- Avaiable tokens on record bean.

	bill_no,
	amount,
	payee_name,
	payment_type,
	name,
	posted_date,
	voucher_date => posted date,
	payment_id,
	account_group,
	account_head_name,
	center_code,
	dept_center_code,
	visit_type,
	op_type,
	mod_time,
	salutation,
	patient_name,
	middle_name,
	last_name,
	full_name

--]

[#import "/accounting/ledgertypes.ftl" as ledgertypes]

<!-- narration -->
[#assign narration = "Bill No-Patient Name : " + (record.bill_no!'') + '-' + (record.full_name!'')]
[#if record.payment_type == 'C']
	[#assign narration = "Miscellaneous Payment Id: " + record.payment_id + ", Payee-name: " + (record.name!)]
[/#if]

<!-- party and special account names -->
[#assign prefix = ""]
[#assign suffix = ""]
[#assign ac_name=record.name]
[#assign spl_ledgerAcccount = ""]

[#if record.payment_type == 'D']
	[#assign ac_name = record.name]
	[#if partynames.doctor_individual_accounts == 'N']
		[#assign prefix = ""]
		[#assign suffix = ""]
		[#assign ac_name = partynames.doctor_ac_name!record.name]
	[#else]
		[#if record.visit_type == 'i']
			[#assign prefix = partynames.doctor_ip_ac_prefix!""]
			[#assign suffix = partynames.doctor_ip_ac_suffix!""]
		[#else]
			[#assign prefix = partynames.doctor_op_ac_prefix!""]
			[#assign suffix = partynames.doctor_op_ac_suffix!""]
		[/#if]
	[/#if]
	[#assign spl_ledgerAccount = specialnames.exp_doctor_payments!]
[/#if]

[#if record.payment_type=='P']
	[#assign ac_name = record.name]
	[#if partynames.prescribingdoctor_individual_accounts == 'N']
		[#assign prefix = ""]
		[#assign suffix = ""]
		[#assign ac_name = partynames.prescribingdoctor_ac_name!record.name]
	[#else]
		[#if record.visit_type == 'i']
			[#assign prefix = partynames.prescribingdoctor_ip_ac_prefix!""]
			[#assign suffix = partynames.prescribingdoctor_ip_ac_suffix!""]
		[#else]
			[#assign prefix = partynames.prescribingdoctor_op_ac_prefix!""]
			[#assign suffix = partynames.prescribingdoctor_op_ac_suffix!""]
		[/#if]
	[/#if]
	[#assign spl_ledgerAccount = specialnames.exp_prescribing_doctor_payments!]
[/#if]

[#if record.payment_type=='R' || record.payment_type=='F']
	[#assign ac_name = record.name]
	[#if partynames.referral_individual_accounts == 'N']
		[#assign prefix = ""]
		[#assign suffix = ""]
		[#assign ac_name = partynames.referral_ac_name!record.name]
	[#else]
		[#if record.visit_type == 'i']
			[#assign prefix = partynames.referral_ip_ac_prefix!""]
			[#assign suffix = partynames.referral_ip_ac_suffix!""]
		[#else]
			[#assign prefix = partynames.referral_op_ac_prefix!""]
			[#assign suffix = partynames.referral_op_ac_suffix!""]
		[/#if]
	[/#if]
	[#assign spl_ledgerAccount = specialnames.exp_referral_payments!]
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
	[#assign spl_ledgerAccount = specialnames.exp_misc_payments!]
	[#if record.account_head_name?has_content]
		[#assign spl_ledgerAccount = record.account_head_name!]
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
	[#assign spl_ledgerAccount = specialnames.exp_outhouse_payments!]
[/#if]

[#assign party_account = "${prefix} ${ac_name} ${suffix}"]

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


<!--Setup the voucher -->

[@voucher narration="${narration}"]
	<!-- Item level entries -->
	[@ledgerEntry debitOrCredit="C" ledgerType="${ledgertypes.cv}" ledgerAccount="${party_account}" amount=record.amount
		centerCode="${real_cost_center_code}" /]
	[@ledgerEntry debitOrCredit="D" ledgerType="${ledgertypes.ie}" ledgerAccount="${spl_ledgerAccount}" amount=record.amount
		centerCode="${ie_cost_center_code}"/]

[/@voucher]
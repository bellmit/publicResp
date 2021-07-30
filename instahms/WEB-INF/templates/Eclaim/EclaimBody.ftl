[#setting number_format="0.####"]
[#escape x as x?html]
[#assign claim = eclaim.claim][#assign diagnosis = eclaim.diagnosis][#assign charges = eclaim.charges][#assign observationsMap = eclaim.observationsMap][#assign gross = 0][#assign net = 0][#assign patient_share = 0][#assign ninteen_seventy=('01/01/1970 00:00')?datetime('dd/MM/yyyy HH:mm')][#assign endDate=ninteen_seventy]	[#if charges??]	[#list charges as charge][#assign gross = gross + charge.amount!0][#assign net = net + charge.net!0][#if claim.visit_type == "o"][#if endDate == ninteen_seventy][#assign endDate = charge.posted_date?datetime("dd/MM/yyyy HH:mm")][#else][#if charge.posted_date?datetime("dd/MM/yyyy HH:mm") > endDate][#assign endDate = charge.posted_date?datetime("dd/MM/yyyy HH:mm")][/#if][/#if][/#if][/#list] [#assign gross = gross + eclaim.drgAdjustmentAmt] [#assign patient_share = gross - net][/#if]
[#assign accGrpList = accGrpList]
[#assign prior_auth_id = claim.prior_auth_id]
[#assign prescDoctorAsOrderingClinicianingClinician = healthHAADPref]
	<Claim>
		<!-- Claim identification information -->
		<ID>${claim.claim_id!""}</ID>
		<IDPayer>[#if claim.payers_reference_no??]${claim.payers_reference_no}[/#if]</IDPayer>
		<MemberID>${claim.member_id!""}</MemberID>
		<PayerID>${claim.payer_id!""}</PayerID>
		<ProviderID>${claim.provider_id!""}</ProviderID>
		<EmiratesIDNumber>${claim.emirates_id_number!""}</EmiratesIDNumber>
		<Gross>${gross!0}</Gross>
		<PatientShare>${patient_share!0}</PatientShare>
		<Net>${net!0}</Net>

		<!-- Encounter information -->
		<Encounter>
			<FacilityID>${claim.provider_id!""}</FacilityID>
			<Type>${claim.encounter_type!0?int}</Type>
			<PatientID>${claim.patient_id}</PatientID>
			<Start>${claim.start_date!""}</Start>
			[#if claim.visit_type == "i" && claim.end_date??]<End>${claim.end_date!""}</End>
			[#elseif claim.visit_type == "o" && endDate??]<End>${endDate?string("dd/MM/yyyy HH:mm")}</End>[/#if]
			[#if claim.encounter_start_type??]<StartType>${claim.encounter_start_type!0?int}</StartType>[/#if]
			[#if claim.encounter_end_type??]<EndType>${claim.encounter_end_type!0?int}</EndType>[/#if]
			[#if claim.encounter_start_type?? && claim.encounter_start_type == 3 && claim.source_service_regn_no?? && claim.source_service_regn_no != ""]<TransferSource>${claim.source_service_regn_no!""}</TransferSource>[/#if]
			[#if claim.encounter_end_type?? && claim.encounter_end_type == 4 && claim.destination_service_regn_no?? && claim.destination_service_regn_no != ""]<TransferDestination>${claim.destination_service_regn_no!""}</TransferDestination>[/#if]
		</Encounter>

		[#assign isPharamcyAccGrp = false]
		[#list accGrpList as acc]
			[#if acc.account_group_id != 1 && acc.account_group_service_reg_no == claim.provider_id]
				[#assign isPharamcyAccGrp = true]
			[/#if]
		[/#list]

		<!-- Diagnosis information -->
		[#if claim.require_pbm_authorization?? && claim.require_pbm_authorization == "Y" && isPharamcyAccGrp]
			[#assign isDiagCodesSet = false]
			[#list diagnosis as diag]
				[#if diag.sent_for_approval?? && diag.sent_for_approval == true]
				[#assign isDiagCodesSet = true]
				<Diagnosis>
					<Type>${diag.diag_type!""}</Type>
					<Code>${diag.icd_code!""}</Code>
				</Diagnosis>
				[/#if]
			[/#list]
			<!-- if no diag codes with sent_for_approval = true, list all diag codes -->
			[#if isDiagCodesSet = false]
				[#list diagnosis as diag]
				<Diagnosis>
					<Type>${diag.diag_type!""}</Type>
					<Code>${diag.icd_code!""}</Code>
				</Diagnosis>
				[/#list]
			[/#if]
		[#else]
			[#list diagnosis as diag]
				<Diagnosis>
					<Type>${diag.diag_type!""}</Type>
					<Code>${diag.icd_code!""}</Code>
				</Diagnosis>
			[/#list]
		[/#if]

		[#if charges??]
		<!-- Activities information -->
		[#list charges as charge]
			<Activity>
				<ID>${charge.activity_charge_id!""}[#if claim.submission_id_with_correction?? && claim.submission_id_with_correction != ""]-${claim.submission_id_with_correction!""}[#else][/#if]</ID>
				[#if charge.is_drg_group == 'Y'] [#if claim.end_date??]
				<Start>${claim.end_date!""}</Start> [#elseif endDate??]
			    <Start>${endDate?string("dd/MM/yyyy HH:mm")}</Start>
	     [#else]<Start>${charge.posted_date!""}</Start>[/#if]
		 [#else]<Start>${charge.posted_date!""}</Start>[/#if]

				<Type>[#if charge.act_type?has_content]${charge.act_type?string("#")}[#else]0[/#if]</Type>
				<Code>${charge.item_code!""}</Code>
				[#assign haadQtyConvrReq = true ]
				[#if claim.require_pbm_authorization?? && claim.require_pbm_authorization == "Y" && eclaim_xml_schema?? && eclaim_xml_schema == "HAAD" && haadQtyConvrReq ]
					<Quantity>${(charge.quantity/charge.issue_base_unit)?string("0.00")}</Quantity>
				[#else]
					<Quantity>${charge.quantity?string("##.00")}</Quantity>
				[/#if]

				<Net>${charge.net!0}</Net>
				[#if eclaim_xml_schema?? && eclaim_xml_schema == "DHA"]
				<Clinician>[#if charge.is_perdiem_code='Y' && charge.is_home_care_code =='Y']${charge.ref_doctor_license_number!charge.doctor_license_number}[#else]${charge.doctor_license_number!""}[/#if]</Clinician>
				[#else]
				<OrderingClinician>[#if prescDoctorAsOrderingClinicianingClinician == 'Y' && charge.prescribing_doctor_license_number??  ]${charge.prescribing_doctor_license_number}[#elseif charge.is_perdiem_code='Y' && charge.is_home_care_code=='Y']${charge.ref_doctor_license_number!charge.doctor_license_number}[#else]${charge.doctor_license_number!""}[/#if]</OrderingClinician>
				<Clinician>${charge.conducting_dr_license_number!""}</Clinician>
				[/#if]
			[#if charge.prior_auth_id?? && charge.prior_auth_id != "0" && charge.prior_auth_id != ""]<PriorAuthorizationID>${charge.prior_auth_id!""}</PriorAuthorizationID>
			[#elseif prior_auth_id?? && prior_auth_id != "0" && prior_auth_id != ""]<PriorAuthorizationID>${prior_auth_id!""}</PriorAuthorizationID>[/#if]
				[#if observationsMap?has_content]
					[#list observationsMap?keys as key]
					[#if key == charge.charge_id]
					<!-- Observations information -->
					[#assign observationVal = ""]
					[#list observationsMap[key] as observation]
					[#if observation.type?? && observation.type != "" && observation.code?? && observation.code != ""]
						[#if observation.code == "Non-Standard-Code"]
							[#if !observationVal?contains(observation.value)]
								[#assign observationVal = observationVal+"::"+observation.value]
							<Observation>
									<Type>${observation.type!""}</Type>
									<Code>${observation.code!""}</Code>
									<Value>${observation.value!""}</Value>
									<ValueType>${observation.value_type!""}</ValueType>
								</Observation>
							[/#if]
						[#else]
								<Observation>
									<Type>${observation.type!""}</Type>
									<Code>${observation.code!""}</Code>
									<Value>${observation.value!""}</Value>
									<ValueType>${observation.value_type!""}</ValueType>
								</Observation>
						[/#if]
					[/#if]
					[/#list]
					[/#if]
					[/#list]
				[/#if]
			</Activity>
		[/#list]
		[/#if]

		[#if claim.is_resubmission?? && claim.is_resubmission == "Y"]
		<!-- Resubmission information -->
		<Resubmission>
			<Type>${claim.resubmission_type!}</Type>
		    <Comment>${claim.comments!}</Comment>
			[#if eclaim.attachment??]<Attachment>${eclaim.attachment}</Attachment> [/#if]
		</Resubmission>
		[/#if]
	</Claim>
[/#escape]

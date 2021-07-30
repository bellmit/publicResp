[#setting number_format="0.####"]
[#escape x as x?html]
[#assign eAuthRequestBean = eAuthRequest.eauthbean][#assign diagnosis = eAuthRequest.diagnosis][#assign activities = eAuthRequest.activities][#assign observationsMap = eAuthRequest.observationsMap]
<Authorization>
	<!-- Prior Authorization identification information -->
		<Type>${eAuthRequestBean.preauth_request_type!""}</Type>
		<ID>${eAuthRequestBean.preauth_request_id!""}</ID>
		<IDPayer>[#if eAuthRequestBean.preauth_id_payer??]${eAuthRequestBean.preauth_id_payer}[/#if]</IDPayer>
		<MemberID>${eAuthRequestBean.member_id!""}</MemberID>
		<PayerID>${eAuthRequestBean.payer_id!""}</PayerID>
		<EmiratesIDNumber>${eAuthRequestBean.emirates_id_number!""}</EmiratesIDNumber>
		[#if eAuthRequestBean.date_ordered?? && eAuthRequestBean.date_ordered != ""]
		<DateOrdered>${eAuthRequestBean.date_ordered!""}</DateOrdered>
		[/#if]

		[#if eAuthRequestBean.preauth_request_type != 'Cancellation']
		<!-- Encounter information -->
		<Encounter>
			<FacilityID>${eAuthRequestBean.provider_id!""}</FacilityID>
			<Type>${eAuthRequestBean.encounter_type!0?int}</Type>
			[#if eAuthRequestBean.start_date?? && eAuthRequestBean.end_date??]
			<Start>${eAuthRequestBean.start_date!""}</Start>
			<End>${eAuthRequestBean.end_date!""}</End>[/#if]
		</Encounter>
		[/#if]

		[#if eAuthRequestBean.preauth_request_type != 'Cancellation']
		<!-- Diagnosis information -->
			[#list diagnosis as diag]
				<Diagnosis>
					[#if diag.diagnosis_type == 'P']
						[#assign diagnosisTypeName = 'Principal']
					[#elseif diag.diagnosis_type == 'A']
						[#assign diagnosisTypeName = 'Admitting']
					[#elseif diag.diagnosis_type == 'V']
						[#assign diagnosisTypeName = 'ReasonForVisit']
					[#else]
						[#assign diagnosisTypeName = 'Secondary']
					[/#if]
					<Type>${diagnosisTypeName!""}</Type>
					<Code>${diag.icd_code!""}</Code>
				</Diagnosis>
			[/#list]
		[/#if]

		[#if eAuthRequestBean.preauth_request_type != 'Cancellation' && activities??]
		<!-- Activities information -->
		[#list activities as activity]
		<Activity>
			<ID>${activity.preauth_act_id!""}[#if eAuthRequestBean.resubmit_request_id_with_correction?? && eAuthRequestBean.resubmit_request_id_with_correction != ""]-${eAuthRequestBean.resubmit_request_id_with_correction!""}[#else][/#if]</ID>
			<Start>${activity.activity_prescribed_date!""}</Start>
			<Type>[#if activity.haad_code?has_content]${activity.haad_code?string("#")}[#else]0[/#if]</Type>
			<Code>${activity.act_code!""}</Code>
			<Quantity>[#if activity.act_qty?has_content]${activity.act_qty?string("#.00")}[#else]0[/#if]</Quantity>
			<Net>${activity.amount!0}</Net>
			<Clinician>${eAuthRequestBean.doctor_license_number!""}</Clinician>

			[#if observationsMap?has_content]
				[#list observationsMap?keys as key]
				[#if key == activity.preauth_act_id?string("#")]
				<!-- Observations information -->
				[#list observationsMap[key] as observation]
				[#if observation.obs_type?? && observation.obs_type != "" && observation.code?? && observation.code != ""]
				<Observation>
					<Type>${observation.obs_type!""}</Type>
					<Code>${observation.code!""}</Code>
					<Value>${observation.value!""}</Value>
					<ValueType>${observation.value_type!""}</ValueType>
				</Observation>
				[/#if]
				[/#list]
				[/#if]
				[/#list]
			[/#if]

		</Activity>
		[/#list]
		[/#if]

	   [#if eAuthRequestBean.is_resubmit?? && eAuthRequestBean.is_resubmit == "Y" && eAuthRequestBean.preauth_request_type != 'Cancellation']
		<!-- Resubmission information -->
		<Resubmission>
			<Type>${eAuthRequestBean.resubmit_type!}</Type>
		    <Comment>${eAuthRequestBean.comments!}</Comment>
			[#if eAuthRequest.attachment??]<Attachment>${eAuthRequest.attachment}</Attachment> [/#if]
		</Resubmission>
		[/#if]

</Authorization>
[/#escape]
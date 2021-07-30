[#setting number_format="0.####"]
[#escape x as x?html]
[#assign pbmRequestBean = pbmrequest.pbmRequestBean][#assign diagnosis = pbmrequest.diagnosis][#assign activities = pbmrequest.activities][#assign observationsMap = pbmrequest.observationsMap]
<Authorization>
	<!-- PBM Authorization identification information -->
		<Type>${pbmRequestBean.pbm_request_type!""}</Type>
		<ID>${pbmRequestBean.pbm_request_id!""}</ID>
		<IDPayer>[#if pbmRequestBean.pbm_auth_id_payer??]${pbmRequestBean.pbm_auth_id_payer}[/#if]</IDPayer>
		<MemberID>${pbmRequestBean.member_id!""}</MemberID>
		<PayerID>${pbmRequestBean.payer_id!""}</PayerID>
		<EmiratesIDNumber>${pbmRequestBean.emirates_id_number!""}</EmiratesIDNumber>
		<DateOrdered>${pbmRequestBean.date_ordered!""}</DateOrdered>

		[#if pbmRequestBean.pbm_request_type != 'Cancellation']
		<!-- Encounter information -->
		<Encounter>
			<FacilityID>${pbmRequestBean.provider_id!""}</FacilityID>
			<Type>${pbmRequestBean.encounter_type!0?int}</Type>
		</Encounter>
		[/#if]

		[#if pbmRequestBean.pbm_request_type != 'Cancellation']
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

		[#if pbmRequestBean.pbm_request_type != 'Cancellation' && activities??]
		<!-- Activities information -->
		[#list activities as activity]
		<Activity>
			<ID>${activity.pbm_medicine_pres_id!""}[#if pbmRequestBean.resubmit_request_id_with_correction?? && pbmRequestBean.resubmit_request_id_with_correction != ""]-${pbmRequestBean.resubmit_request_id_with_correction!""}[#else][/#if]</ID>
			<Start>${activity.activity_prescribed_date!""}</Start>
			<Type>[#if activity.haad_code?has_content]${activity.haad_code?string("#")}[#else]0[/#if]</Type>
			<Code>${activity.item_code!""}</Code>
			<Quantity>[#if activity.medicine_quantity?has_content]${activity.medicine_quantity?string("##0.0000")}[#else]0[/#if]</Quantity>
			
			[#if activity.medicine_quantity?has_content]
				[#assign pbmQty = activity.medicine_quantity!0]
			[#else][#assign pbmQty = 0]
			[/#if]
			<Net>${(activity.amount)?string("##0.00")}</Net>
			<Clinician>${pbmRequestBean.doctor_license_number!""}</Clinician>

			[#if observationsMap?has_content]
				[#list observationsMap?keys as key]
				[#if key == activity.item_prescribed_id]
				<!-- Observations information -->
				[#list observationsMap[key] as observation]
				[#if observation.type?? && observation.type != "" && observation.code?? && observation.code != ""]
				<Observation>
					<Type>${observation.type!""}</Type>
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

	   [#if pbmRequestBean.is_resubmit?? && pbmRequestBean.is_resubmit == "Y"]
		<!-- Resubmission information -->
		<Resubmission>
			<Type>${pbmRequestBean.resubmit_type!}</Type>
		    <Comment>${pbmRequestBean.comments!}</Comment>
			[#if pbmrequest.attachment??]<Attachment>${pbmrequest.attachment}</Attachment> [/#if]
		</Resubmission>
		[/#if]

</Authorization>
[/#escape]
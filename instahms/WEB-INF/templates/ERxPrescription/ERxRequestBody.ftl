[#setting number_format="0.####"]
[#escape x as x?html]
[#assign prescription = request.prescription][#assign patient = prescription.patient]
[#assign encounter = prescription.encounter!][#assign diagnosis = prescription.diagnosis!]
[#assign activities = prescription.activities]

<Prescription>
	<ID>${prescription.id!}</ID>
	<Type>${prescription.type!}</Type>
	<PayerID>${prescription.payerId!}</PayerID>
	<Clinician>${prescription.clinician!}</Clinician>

	<!-- Patient information -->
	<Patient>
	  <MemberID>${patient.memberId!}</MemberID>
	  <EmiratesIDNumber>${patient.emiratesIDNumber!}</EmiratesIDNumber>
	  <DateOfBirth>${patient.dateOfBirth!}</DateOfBirth>
	  <Weight>${patient.weight!}</Weight>
	  [#if patient.email?? && patient.email != ""]<Email>${patient.email!}</Email>[/#if]
	</Patient>

	[#if prescription.type! != 'eRxCancellation']
	<!-- Encounter information -->
	<Encounter>
	  <FacilityID>${encounter.facilityID!}</FacilityID>
	  <Type>${encounter.type!}</Type>
	</Encounter>
	[/#if]

	<!-- Diagnosis information -->
	[#list diagnosis as diag]
	<Diagnosis>
		[#if diag.diagnosis_type == 'P']
				[#assign diagnosisTypeName = 'Principal']
			[#elseif diag.diagnosis_type == 'A']
				[#assign diagnosisTypeName = 'Admitting']
			[#else]
				[#assign diagnosisTypeName = 'Secondary']
		[/#if]
			<Type>${diagnosisTypeName!""}</Type>
			<Code>${diag.code!""}</Code>
	</Diagnosis>
	[/#list]

	<!-- Activities information -->
	[#if prescription.type! != 'eRxCancellation' && activities??]
	[#list activities as activity]
	[#assign frequency = activity.frequency][#assign observations = activity.observations]
	<Activity>
	  <ID>${activity.activityID!}</ID>
	  <Start>${activity.activityStart!}</Start>
	  <Type>${activity.activityType!}</Type>
	  <Code>${activity.activityCode!}</Code>
	  <Quantity>${activity.quantity!}</Quantity>
	  <Duration>${activity.duration!}</Duration>
	  <Refills>${activity.refills!}</Refills>
	  <RoutOfAdmin>${activity.routOfAdmin!}</RoutOfAdmin>
	  <Instructions>${activity.instructions!}</Instructions>
	  [#if frequency?? && frequency.valueType?? && frequency.valueType != '']
	  <Frequency>
	    <UnitPerFrequency>${frequency.unit!}</UnitPerFrequency>
	    <FrequencyValue>${frequency.value!}</FrequencyValue>
	    <FrequencyType>${frequency.type!}</FrequencyType>
	  </Frequency>
	  [/#if]
	  [#if observations??]
		[#list observations as observation]
		<!-- Observations information -->
		[#if observation.type?? && observation.type != "" && observation.code?? && observation.code != ""]
		<Observation>
			<Type>${observation.type!""}</Type>
			<Code>${observation.code!""}</Code>
			<Value>${observation.value!""}</Value>
			<ValueType>${observation.valueType!""}</ValueType>
		</Observation>
		[/#if]
		[/#list]
	  [/#if]
	</Activity>
	[/#list]
	[/#if]

</Prescription>
[/#escape]
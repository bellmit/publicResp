[#setting number_format="0.####"]
[#setting datetime_format="yyyy-MM-dd'T'HH:mm:ss"]
[#setting date_format="yyyy-MM-dd'T00:00:00'"]
[#setting time_format="HH:mm:ss"]
[#escape x as x?html]
<encounter>
	<encounterID>${pat.patient_id}</encounterID>
	<hostSystemStatus>ACTIVE</hostSystemStatus>
	<healthSystem>Lifeline</healthSystem>
	<facilityID>MF2222</facilityID>
	<facilityName>Musaffah</facilityName>
	[#if (pat.dept_name!) == '' ]
	<department>#N/A</department>
	[#else]
	<department>${pat.dept_name}</department>
	[/#if]
	[#if pat.visit_type == 'o' ]
	<patientType>OUTPATIENT</patientType>
	[#if (pat.consultation_type!) != ""]
	<consultationType>${pat.consultation_type!}</consultationType>
	<admitDate>${pat.consultation_date!}</admitDate>
	<dischargeDate>${pat.consultation_date!}</dischargeDate>
	[#else]
	<admitDate>${pat.reg_date}</admitDate>
	<dischargeDate>${pat.reg_date}</dischargeDate>
	[/#if]
	[#else]
	<patientType>INPATIENT</patientType>
	<admitDate>${pat.reg_date}</admitDate>
	[#if (pat.discharge_date??)]
	<dischargeDate>${pat.discharge_date!}</dischargeDate>
	[/#if]
	[/#if]
	<billOpenDate>${pat.open_date}</billOpenDate>
	<openedBy>${pat.opened_by}</openedBy>
	<billingBedType>${pat.bed_type}</billingBedType>
	<billNumber>${pat.bill_no}</billNumber>

	<patient>
  	<identification>
  		<salutation>${pat.salutation!}</salutation>
			<firstName>${pat.first_name!}</firstName>
			[#if pat.middle_name! != ""]
			<middleName>${pat.middle_name!}</middleName>
			[/#if]
			<lastName>[#if pat.last_name! != ""]${pat.last_name!}[#else].[/#if]</lastName>
			<governmentIdNumber>${pat.government_identifier!}</governmentIdNumber>
			<governmentIdType>Emirates ID number</governmentIdType>
			<dateOfBirth>${pat.dob!}</dateOfBirth>
			<ageValue>${pat.age!}</ageValue>
			<ageType>[#if pat.agein == 'Y']YEARS[#elseif pat.agein == 'M']MONTHS[#else]DAYS[/#if]</ageType>
			<gender>[#if pat.patient_gender == 'M']MALE[#elseif pat.patient_gender == 'F']FEMALE[#else]OTHER[/#if]</gender>
			<nationality>${pat.nationality!}</nationality>
		</identification>
		<address>
			<line1>[#if pat.patient_address! == ""]Not captured[#else]${pat.patient_address!}[/#if]</line1>
			<line2>[#if pat.patient_area! == ""]Not captured[#else]${pat.patient_area!}[/#if]</line2>
			<city>${pat.city_name!}</city>
			<state>${pat.state_name!}</state>
			<country>${pat.country_name!}</country>
		</address>
		<contact>
			[#if pat.patient_phone! != ""]
			<primaryPhoneNumber>
				<phoneNumber>${pat.patient_phone}</phoneNumber>
			</primaryPhoneNumber>
			[/#if]
			[#if pat.patient_phone2! != ""]
			<secondaryPhoneNumber>
				<phoneNumber>${pat.patient_phone2}</phoneNumber>
			</secondaryPhoneNumber>
			[/#if]
			[#if pat.email_id! != ""]
			<emailAddress>${pat.email_id!}</emailAddress>
			[/#if]
  	</contact>
		<medicalRecordNumber>${pat.mr_no!}</medicalRecordNumber>
		[#if pat.relation! != ""]
		<nextOfKinName>${pat.relation!}</nextOfKinName>
		[#else]
		<nextOfKinName>UNKNOWN</nextOfKinName>
		[/#if]
		[#if pat.next_of_kin_relation! != ""]
		<nextOfKinRelationship>${pat.next_of_kin_relation!}</nextOfKinRelationship>
		[#else]
		<nextOfKinRelationship>UNKNOWN</nextOfKinRelationship>
		[/#if]
		<patientCategory>${pat.category_name!}</patientCategory>
	</patient>
	[#--
 	<guarantor>
		<identification>
			<firstName>${pat.policy_holder_name!}</firstName>
		</identification>
		<relationship>${pat.patient_relationship!}</relationship>
 	</guarantor>
	--]
	[#if (pat.member_id!) != ""]
 	<healthPlan>
		<healthPlanId>${pat.insurance_co_code!}</healthPlanId>
		<coordinationOfBenefitNumber>1</coordinationOfBenefitNumber>
		<insurancePlanName>${pat.plan_name!}</insurancePlanName>
		<insurancePlanType>${pat.plan_type!}</insurancePlanType>
		<insuranceRatePlan>${pat.org_name!}</insuranceRatePlan>
		<insuranceApprovedAmount>${pat.primary_insurance_approval!0}</insuranceApprovedAmount>
		<tpaSponsor>${pat.tpa_name!}</tpaSponsor>
		<insuranceMemberID>${pat.member_id!}</insuranceMemberID>
		[#if (pat.prior_auth_id!) != ""]
		<priorAuthNumber>${pat.prior_auth_id!}</priorAuthNumber>
		[/#if]
		<planEffectiveDate>${pat.policy_validity_start!}</planEffectiveDate>
		<planEndDate>${pat.policy_validity_end!}</planEndDate>
	</healthPlan>
	[#else]
 	<healthPlan>
		<healthPlanId>@SelfPay</healthPlanId>
		<coordinationOfBenefitNumber>1</coordinationOfBenefitNumber>
		<insurancePlanName>0</insurancePlanName>
		<insurancePlanType>0</insurancePlanType>
		<insuranceRatePlan>0</insuranceRatePlan>
		<insuranceApprovedAmount>0</insuranceApprovedAmount>
		<tpaSponsor>0</tpaSponsor>
		<insuranceMemberID>0</insuranceMemberID>
		<planEffectiveDate>1970-01-01T00:00:00</planEffectiveDate>
		<planEndDate>1970-01-01T00:00:00</planEndDate>
 	</healthPlan>
	[/#if]
	<balances>
		<totalCharges currency="AED">${pat.total_amount + pat.total_discount}</totalCharges>
		<totalInsurancePayments currency="AED">${pat.primary_total_sponsor_receipts}</totalInsurancePayments>
		<totalInsuranceAdjustments currency="AED">${pat.total_discount}</totalInsuranceAdjustments>
		[#assign totalInsuranceBalance = (pat.total_claim - pat.primary_total_sponsor_receipts)]
		<totalInsuranceBalance currency="AED">${totalInsuranceBalance}</totalInsuranceBalance>

		<totalPatientPayments currency="AED">${pat.total_receipts}</totalPatientPayments>
		<totalPatientAdjustments currency="AED">0.00</totalPatientAdjustments>
		[#assign totalPatientBalance = pat.total_amount - pat.total_claim - pat.total_receipts]
		<totalPatientBalance currency="AED">${totalPatientBalance}</totalPatientBalance>
		<currentBalance currency="AED">${totalInsuranceBalance + totalPatientBalance}</currentBalance>
		<totalNetExpected currency="AED">${pat.total_amount}</totalNetExpected>
		<patientShare currency="AED">${pat.total_amount - pat.total_claim}</patientShare>
		<insuranceShare currency="AED">${pat.total_claim}</insuranceShare>
		<totalDiscounts currency="AED">${pat.total_discount}</totalDiscounts>
		<billedAmount currency="AED">${pat.total_amount}</billedAmount>
	</balances>
</encounter>
[/#escape]


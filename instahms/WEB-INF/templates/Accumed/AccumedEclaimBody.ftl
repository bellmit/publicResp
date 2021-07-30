[#setting number_format="0.####"]
[#assign actCodeType = {"CPT":"CPT", "Service Code":"Service", "Service":"Service", "Drug":"Medication", "Drug DHA":"Medication",
"HCPCS":"HCPCS","HCPCS DHA":"HCPCS", "Dental":"Dental", "E&M":"E&M", "ICD":"ICD", "IR-DRG":"IR-DRG", "":"Other" }]
[#escape x as x?html]
[#assign claim = eclaim.claim][#assign diagnosis = eclaim.diagnosis][#assign charges = eclaim.charges][#assign observationsMap = eclaim.observationsMap][#assign list = 0][#assign gross = 0][#assign deductible = 0][#assign net = 0][#assign patient_share = 0][#assign ninteen_seventy=('01/01/1970 00:00')?datetime('dd/MM/yyyy HH:mm')][#assign endDate=ninteen_seventy]	[#if charges??]	[#list charges as charge][#assign chargenet = charge.net!0][#assign amount = charge.amount!0][#assign discount = charge.discount!0][#assign list = list + amount + discount][#assign gross = gross + amount][#assign net = net + chargenet][#if claim.visit_type == "o"][#if endDate == ninteen_seventy][#assign endDate = charge.posted_date?datetime("dd/MM/yyyy HH:mm")][#else][#if charge.posted_date?datetime("dd/MM/yyyy HH:mm") > endDate][#assign endDate = charge.posted_date?datetime("dd/MM/yyyy HH:mm")][/#if][/#if][/#if][/#list] [#assign patient_share = gross - net][/#if]
[#assign clinicalData = eclaim.clinicalData]
[#if eclaim.supportedAttachments??]
[#assign supportedAttachments = eclaim.supportedAttachments]
[/#if]
[#assign prior_auth_id = claim.prior_auth_id]
	<Claim>
		<!-- Claim identification information -->
		<ID>${claim.claim_id!""}</ID>
		[#if claim.main_visit_id??]
		<InvoiceNumber>${claim.main_visit_id!""}</InvoiceNumber>
		[/#if]
	        <ReceiverID>${claim.receiver_id!""}</ReceiverID>
	        <PayerID>${claim.payer_id!""}</PayerID>
	        <FacilityID>${claim.provider_id!""}</FacilityID>
	        <List>${list!0}</List>
	        <Gross>${gross!0}</Gross>
	        <Deductible>${deductible!0}</Deductible>
	        <PatientShare>${patient_share!0}</PatientShare>
	        <Net>${net!0}</Net>
	        <ClientBatchID>${claim.submission_batch_id!""}</ClientBatchID>

		<!-- Encounter information -->
		<Encounter>
			<Type>${claim.encounter_type_desc!""}</Type>
			<PatientID>${claim.patient_id}</PatientID>
			<Start>${claim.start_date!""}</Start>
			[#if claim.visit_type == "i" && claim.end_date??]<End>${claim.end_date!""}</End>
			[#elseif claim.visit_type == "o" && endDate??]<End>${endDate?string("dd/MM/yyyy HH:mm")}</End>[/#if]
			[#if claim.encounter_start_type??]<StartType>${claim.encounter_start_type_desc!""}</StartType>[/#if]
			[#if claim.encounter_end_type??]<EndType>${claim.encounter_end_type_desc!""}</EndType>[/#if]
			[#if claim.encounter_start_type == 3 && claim.source_service_regn_no?? && claim.source_service_regn_no != ""]<TransferSource>${claim.source_service_regn_no!""}</TransferSource>[/#if]
			[#if claim.encounter_end_type == 4 && claim.destination_service_regn_no?? && claim.destination_service_regn_no != ""]<TransferDestination>${claim.destination_service_regn_no!""}</TransferDestination>[/#if]
			[#if clinicalData?has_content]
			[#list clinicalData?keys as key]
			[#if key?? && clinicalData[key]??]<${key}>${clinicalData[key]!""}</${key}>[/#if]
			[/#list]
			[/#if]
			[#if claim.established_status??]<PatientEstablished>${claim.established_status?string('TRUE', 'FALSE')}</PatientEstablished>[/#if]
		</Encounter>

		<!-- Diagnosis information -->
		[#list diagnosis as diag]
		<Diagnosis>
			<Type>${diag.diag_type!""}</Type>
			<Code>${diag.icd_code!""}</Code>
			<ICD>${diag.code_type!""}</ICD>
		</Diagnosis>
		[/#list]

		[#if charges??]
		<!-- Activities information -->
		[#list charges as charge]
		<Activity>[#assign chamount = charge.amount!0][#assign chnet = charge.net!0] [#assign chdiscount = charge.discount!0] [#assign list = chamount + chdiscount] [#assign gross = chamount] [#assign copay = chamount - chnet]
		        <ID>${charge.activity_charge_id!""}</ID>
		        [#if charge.is_drg_group=="Y"] [#if claim.end_date??]
				<Start>${claim.end_date!""}</Start> [#elseif endDate??]
			    <Start>${endDate?string("dd/MM/yyyy HH:mm")}</Start>
		 [#else]<Start>${charge.posted_date!""}</Start>[/#if]
		 [#else]<Start>${charge.posted_date!""}</Start>[/#if]
		 		[#if actCodeType[charge.act_type_desc!""]??]
		        <Type>${actCodeType[charge.act_type_desc!""]}</Type>
		  		[/#if]
		        <Code>${charge.item_code!""}</Code>
		        <Quantity>${charge.quantity?string("##.00")}</Quantity>
		        <List>${list}</List>
		        <ProviderBillRef>${charge.bill_no!""}</ProviderBillRef>
		        <Gross>${gross}</Gross>
		        <Deductible>0</Deductible>
		        <CoPay>${copay}</CoPay>
		        <Net>${charge.net!0}</Net>
		        <ClinicianName>${charge.doctor_name!""}</ClinicianName>
		        <ClinicianLicenseID>${charge.doctor_license_number!""}</ClinicianLicenseID>
			[#if charge.prior_auth_id?? && charge.prior_auth_id != "0" && charge.prior_auth_id != ""]<PriorAuthorizationID>${charge.prior_auth_id!""}</PriorAuthorizationID>
			[#elseif prior_auth_id?? && prior_auth_id != "0" && prior_auth_id != ""]<PriorAuthorizationID>${prior_auth_id!""}</PriorAuthorizationID>[/#if]
			<!-- Add Observation Docs Here for every activity -->
			
			
			[#if observationsMap?has_content]
				[#list observationsMap?keys as key]
					[#if key == charge.charge_id]
				<!-- Observations information -->
						[#list observationsMap[key] as observationMap]
							[#if observationMap?has_content]
								[#if observationMap.type?? && observationMap.type != "" && observationMap.code?? && observationMap.code != ""]								
								<Observation>
									<Type>${observationMap.type!""}</Type>
									<Code>${observationMap.code!""}</Code>
									<Value>${observationMap.value!""}</Value>
									<ValueType>${observationMap.value_type!""}</ValueType>
								</Observation>
								[/#if]
							[/#if]
						[/#list]
					[/#if]
				[/#list]
			[/#if]
			<!-- Observations -->


			
		</Activity>
		[/#list]
		[/#if]

		<Patient>
			<Name>${claim.patient_name!}${claim.middle_name!}</Name>
			<Surname>${claim.last_name!}</Surname>
			<PackageName>${claim.package_name!""}</PackageName>
			[#if claim.policy_number?? && claim.policy_number?has_content]
			<PolicyNumber>${claim.policy_number!""}</PolicyNumber>[/#if]
			<MemberID>${claim.member_id!""}</MemberID>
			<EmiratesIDNumber>${claim.emirates_id_number!""}</EmiratesIDNumber>
			<DateOfBirth>${claim.date_of_birth!""}</DateOfBirth>
			<Gender>${claim.patient_gender!}</Gender>
			[#if claim.nationality??]<Nationality>${claim.nationality!""}</Nationality>[/#if]
			[#if claim.policy_validity_start?? && claim.policy_validity_start?has_content]<StartDate>${claim.policy_validity_start!""}</StartDate>
			[#elseif claim.patient_reg_date?? && claim.patient_reg_date?has_content]<StartDate>${claim.patient_reg_date?string("dd/MM/yyyy")}</StartDate>[/#if]
			[#if claim.policy_validity_end?? && claim.policy_validity_end?has_content]<ExpiryDate>${claim.policy_validity_end!""}</ExpiryDate>
			[#elseif claim.patient_reg_date?? && claim.patient_reg_date?has_content]<ExpiryDate>${claim.patient_reg_date?string("dd/MM/yyyy")}</ExpiryDate>[/#if]

			<!-- Patient Insurance Card Photo -->
			[#if eclaim.cardAttachment??]
			<InsuranceCardPhoto>
				<Name>Card Scanned Image.${claim.card_type?lower_case!""}</Name>
				<Type>${claim.card_type!""}</Type>
				<Comment>${claim.card_comment!""}</Comment>
					<Attachment>${eclaim.cardAttachment!""}</Attachment>
			</InsuranceCardPhoto>
			[/#if]

			[#if planCoPayList??]
			<!-- CoPay information -->
				[#list planCoPayList as copay]
	            <CoPay>
	                <Type>${copay.insurance_category_name!""}</Type>
	                <Value>${copay.patient_percent!0}</Value>
	        		[#if copay.patient_amount_cap?? && copay.patient_amount_cap?has_content]
	        		<Ceiling>${copay.patient_amount_cap!0}</Ceiling>[/#if]
	            </CoPay>
	          [/#list]
			[/#if]
        </Patient>

		[#if supportedAttachments?? && supportedAttachments?has_content]
			<!-- Patient Lab reports --> <!-- Patient Radiology reports -->	<!-- Medical reports -->
			<!-- Doctor notes --> <!-- Discharge summary reports -->
			[#list supportedAttachments as attachment]
				[#if attachment.content?? ]
					<Attachment>
						<Name>${attachment.doc_name!""}.[#if (attachment.doc_type!"")?upper_case == 'JPG']jpeg[#else]${(attachment.doc_type!"")?lower_case}[/#if]</Name>
						<Type>[#if (attachment.doc_type!"")?upper_case == 'JPG']JPEG[#else]${(attachment.doc_type!"")?upper_case}[/#if]</Type>
						<Comment>${attachment.doc_comment!""}</Comment>
							<Attachment>${attachment.content!""}</Attachment>
			    	</Attachment>
		    	[/#if]
          [/#list]
		[/#if]

	</Claim>
[/#escape]

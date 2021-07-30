<!--
	[#setting datetime_format="dd-MM-yyyy HH:mm"]
	[#setting date_format="dd-MM-yyyy"]
-->

	<div class="patientHeader" style="margin-bottom: 1em">
	<table width="100%" cellspacing="0" cellpadding="0">
		<tr>
			<td align="center" colspan="4">${generalBean.form_title!}</td>
		</tr>
		<tr>
			<td colspan = "4">
				<div style="border-bottom:1px solid;"> </div>
			</td>
		</tr>
		<tr>
			<td colspan="4">&nbsp;</td>
		</tr>
		<tr>
			<td width="10%">Name:</td>
			<td>${patient.full_name!}</td>
			<td width="12%">MR No:</td>
			<td>${patient.mr_no!}</td>
		</tr>
		<tr>
			<td>Survey Date:</td>
			<td>${generalBean.survey_date!}</td>
			<td>Visit ID:</td>
			<td>${patient.patient_id}</td>
		</tr>
		<tr>
			<td>Age/Gender:</td>
			<td>${patient.age!?string("#")} ${patient.agein!} ${patient.patient_gender!}</td>
			<td>Address:</td>
			<td>${patient.patient_address!}</td>
		</tr>
		<tr>

		</tr>
		<tr>
			<td>Doctor:</td>
			<td>${patient.doctor_name!}</td>
			<td>
				[#if patient.visit_type == "i"]
					Admission Date:
				[#else]
					Registered Date:
				[/#if]
			</td>
			<td>${patient.reg_date!} ${patient.reg_time!}</td>
		</tr>
		<tr>
			<td>Department:</td>
			<td>${patient.dept_name!}</td>
			<!-- [#if patient.visit_type =='i'] -->
				<td>Ward/Bed</td>
				<td>${patient.bill_bed_type!}</td>
			<!-- [/#if] -->
		</tr>
		<tr>
			<td>Rate Plan:</td>
			<td>${patient.org_name!}</td>
			<!-- [#if patient.visit_type =="i"] -->
				<td>[#if (patient.discharge_type!)=="Expiry"] Death Date: [#else] Discharge Date:[/#if]</td>
				<td>${patient.discharge_date!} ${patient.discharge_time!}</td>
			<!-- [/#if] -->
		</tr>
		<!-- [#if (patient.tpa_name??) || (patient.refdoctorname??)] -->
			<tr>
				<td>Sponsor:</td>
				<td>${patient.tpa_name!}</td>
				<td>Referred By:</td>
				<td>${patient.refdoctorname!}</td>
			</tr>
		<!-- [/#if] -->
		<!-- [#if patient.tpa_id??] -->
			<tr>
				<!-- [#if patient.policy_holder_name??] -->
				<td>Policy Holder:</td>
				<td>${patient.policy_holder_name!}</td>
				<!-- [/#if] -->

				<!-- [#if patient.policy_no??] -->
				<td>Policy No:</td>
				<td>${patient.policy_no!}</td>
				<!-- [/#if] -->
			</tr>
			<tr>
				<!-- [#if patient.plan_name??] -->
					<td>Policy Name:</td>
					<td>${patient.plan_name!}</td>
				<!-- [/#if] -->
				<!-- [#if patient.insurance_id??] -->
					<td>Case No:</td>
					<td>${patient.insurance_id?string('#')}</td>
				<!-- [/#if] -->
			</tr>
			<tr>
			<!-- [#if patient.insurance_no??] -->
				<td>Insurance No:</td>
				<td>${patient.insurance_no!}</td>
			<!-- [/#if] -->
			<td>Location:</td>
			<td><td>${patient.cityname!},${patient.statename!}</td></td>
			</tr>
		<!-- [/#if] -->

		<!-- [#if patient.insurance_co_name?? ] -->
			<tr>
				<td>Insurance Comp:</td>
				<td>${patient.insurance_co_name!}</td>
			</tr>
		<!-- [/#if] -->
		<tr>
			<td>Location:</td>
			<td><td>${patient.cityname!},${patient.statename!}</td></td>
		</tr>
	</table>
</div>
<div style="border-bottom:1px solid;"> </div>
<div>&nbsp;</div>
<div style="border-bottom:1px solid;"> </div>
<table width="100%" cellspacing="0" cellpadding="0">
<!-- [#if surveyResponseDetails?has_content] -->
	[#list surveySectionList as sectionDetails]
		<tr>
			<td colspan="2" align="center">${((sectionDetails.map.section_title!) ?html)?replace("\n", "<br/>")}</td>
		</tr>
		<tr>
			<td colspan="2">
				<div style="border-bottom:1px solid;"> </div>
			</td>
		</tr>
		[#list surveyResponseDetails as responseDetails]
			<!-- [#if responseDetails.map.section_id == sectionDetails.map.section_id] -->
					<tr>
						<td width="12%">&nbsp;</td>
						<td>${((responseDetails.map.question_detail!)?html)?replace("\n","<br/>")}</td>
					</tr>
					<tr>
						<td width="12%">Response:</td>
						<!-- [#if responseDetails.map.response_type != 'T'] -->
							<!-- [#if responseDetails.map.response_type = 'Y'] -->
								<!-- [#if responseDetails.map.response_value = 1] -->
									<td>Yes</td>
								<!-- [#elseif responseDetails.map.response_value = 0] -->
									<td>No</td>
								<!-- [/#if] -->
							<!-- [#else] -->
								<td>${responseDetails.map.rating_text!}</td>
							<!-- [/#if] -->
						<!-- [#else] -->
							<td>${((responseDetails.map.response_text!) ?html)?replace("\n", "<br/>")}</td>
						<!--[/#if] -->
					</tr>
					<!-- [#if responseDetails.map.response_type != 'T'] -->
						<tr>
							<td width="12%">Comment:</td>
							<td>${((responseDetails.map.response_text!) ?html)?replace("\n", "<br/>")}</td>
						</tr>
					<!--[/#if] -->
					<tr>
						<td>&nbsp;</td>
					</tr>
			<!--[/#if] -->
		[/#list]
		[#list surveyFormQuestionList as formQuestionDetails]
			<!-- [#if formQuestionDetails.map.section_id == sectionDetails.map.section_id] -->
				<tr>
					<td width="12%">&nbsp;</td>
					<td>${((formQuestionDetails.map.question_detail!)?html)?replace("\n","<br/>")}</td>
				</tr>
				<tr>
					<td width="12%">Response:</td>
					<td>&nbsp;</td>
				</tr>
				<!-- [#if formQuestionDetails.map.response_type != 'T'] -->
					<tr>
						<td width="12%">Comment:</td>
						<td>&nbsp;</td>
					</tr>
				<!--[/#if] -->
				<tr>
					<td>&nbsp;</td>
				</tr>
			<!--[/#if] -->
		[/#list]
		<tr>
			<td colspan="2">
				<div style="border-bottom:1px solid;"> </div>
			</td>
		</tr>
	[/#list]
<!-- [/#if] -->
</table>

[#setting number_format="#"]
<!--
	[#setting time_format="HH:mm"]
	[#setting date_format="dd-MM-yyyy"]
	[#setting datetime_format="dd-MM-yyyy HH:mm"]
-->

<div align="center">
	<b><u>Sample Work Sheet</u></b>
</div>
<br/>
<br/>
<!-- [#assign i = 1] -->
<!-- [#list testDetailsSampleNoGroups as sampleno] -->
<!-- [#list testDetailsGroupMap[sampleno] as patientDetails] -->
<table width="100%" cellspacing="0" cellpadding="0">
		<tr>
			<td></td><td></td><td></td>
			<td style="font-family: IDAutomationHC39M" align="right">*${sampleno!}*</td>
		</tr>
		<tr>
			<td valign='top'><u>Sample No.</u></td>
			<td valign='top'><u>MR No</u></td>
			<td valign='top'><u>Ward/Bed</u></td>
			<td valign='top'><u>Patient Details</u></td>
		</tr>
		<tr>
			<td valign='top' width="25%">${sampleno}</td>
			<td valign='top' width="25%">${(patientDetails.mr_no)!}</td>
			<td valign='top' width="25%">
				<!--	[#if (patientDetails.visit_type!) = 'i'] -->
					[#if (modules_activated.mod_ipservices!'') == 'Y']
						[#if (patientDetails.alloc_bed_name!'')=='' ](Not Allocated)
						[#else]${(patientDetails.alloc_ward_name)!?html}/${(patientDetails.alloc_bed_name)!?html}
						[/#if]
					[#else]
						${(patientDetails.reg_ward_name)!?html}/${(patientDetails.bill_bed_type)!?html}
					[/#if]
				<!-- [#else] -->
						OP
				<!-- [/#if] -->
			</td>
			<td align='left' valign='top' width="25%">${(patientDetails.patient_full_name)!?html}/${(patientDetails.patient_gender)!}/${(patientDetails.age)!}${(patientDetails.agein)!}/${(patientDetails.patient_phone)!}</td>
		</tr>
		<br/>
		<tr>
			<td valign='top'><u>Sample Type</u></td>
			<td></td>
			<td valign='top'><u>Referring Doctor</u></td>
		</tr>
		<tr>
			<td class="info">${(patientDetails.sample_type)!?html}</td>
			<td></td>
			<td class="info">${(patientDetails.referal_doc_name)!?html}</td>
		</tr>
</table>
<br/>
<table width="100%" cellspacing="0" cellpadding="0">
	<tr>
		<td width="60%">
			<table>
				<tr>
					<td class="label">Sample Collection Time:</td>
					<td class="info">${(patientDetails.sample_date)!}</td>
				</tr>
				<tr>
					<td class="label">Sample Collecting User:</td>
					<td class="info">${(patientDetails.user_name)!}</td>
				</tr>
				<tr>
					<td class="label">Sample Transfer Time:</td>
					<td class="info">${(patientDetails.transfer_time)!}</td>
				</tr>
				<tr>
					<td class="label">Sample Transferred By:</td>
					<td class="info">${(patientDetails.transfer_user)!}</td>
				</tr>
				<tr>
					<td valign='top'>Sample Transfer Details:</td>
					<td class="info">${(patientDetails.transfer_other_details)!?html}</td>
				</tr>
			</table>
		</td>
		<td width="40%">
			<table>
				<tr>
					<td class="label">Sample Receive Time:</td>
					<td class="info">${(patientDetails.receipt_time)!}</td>
				</tr>
				<tr>
					<td class="label">Sample Receive By:</td>
					<td class="info">${(patientDetails.receipt_user)!}</td>
				</tr>
				<tr>
					<td class="label">Sample Assertion Time:</td>
					<td class="info">${(patientDetails.assertion_time)!}</td>
				</tr>
				<tr>
					<td class="label"> Sample Receive Details:</td>
					<td class="info">${(patientDetails.receipt_other_details)!?html}</td>
				</tr>
			</table>
		</td>
	</tr>
</table>
<!--[#break] -->
<!-- [/#list] -->
<br/>
<table width="100%">
	<tr>
		<th valign='top'><u>Test Name (Department)</u></th>
		<th valign='top'><u>Result Name</u></th>
	</tr>
<!-- [#list testDetailsGroupMap[sampleno] as testDetails] -->
	<tr>
		<td width="60%">${(testDetails.test_name)!?html} (${(testDetails.ddept_name)!?html})</td>
		<td width="40%">
			<table>
				<!-- [#if testDetails.cflag == 'N' ] -->
					<!-- [#list testResultsTestIdGroup as testid] -->
						<!-- [#if testDetails.test_id == testid ] -->
							<!-- [#list testResultsTestIdGroupMap[testid] as testResults] -->
								<tr>
									<td>${(testResults.resultlabel)!?html}</td>
								</tr>
							<!-- [/#list] -->
						<!-- [/#if] -->
					<!-- [/#list] -->
				<!-- [#else] -->
					<!-- [#list internalLabResultsTestIdGroup as testid] -->
						<!-- [#if testDetails.test_id == testid ] -->
							<!-- [#list internalLabResultsTestIdGroupMap[testid] as internalLabResults] -->
								<tr>
									<td>${(internalLabResults.resultlabel)!?html}</td>
								</tr>
							<!-- [/#list] -->
						<!-- [/#if] -->
					<!-- [/#list] -->				
				<!-- [/#if] -->
			</table>
		</td>
	</tr>
<!-- [/#list] -->
<!-- [#if testDetailsSampleNoGroups?size > i] -->
	<tr>
		<td style="border-top: 1px dotted; padding: 1px 1px 1px 0px;"></td>
		<td style="border-top: 1px dotted; padding: 1px 1px 1px 0px;"></td>
	</tr>
<!-- [/#if] -->
</table>
<!-- [#assign i = i+1] -->
<!-- [/#list] -->


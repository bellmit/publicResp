<!--
Available variables:
	consultation_bean : Bean representing consultation doctor details.
	visitdetails : Map representing patient and visit details.
	diagnosis_details : Bean representing diagnosis details.
	preAuthPrescActivities : all the pre-auth activities information
	preAuthServices: all the pre-auth information Service activities 
	preAuthInv	: all the pre-auth information Test activities
	preAuthPrescRequest: all the pre-auth request information
	preAuthOperation : all the pre-auth information Operation activities
	preAuthDoctor : all the pre-auth information Doctor activities
	totalActAmount : Map representing total of  (Total Amt,Total Discount,Total Discount,
	Total Patient Amt,Total Claim Net Amt,Total Approved Net Amt)
	
-->
[#setting number_format="#"]
[#setting time_format="HH:mm"]
[#setting date_format="dd-MM-yyyy"]

<!-- [#escape x as x?html] -->

<div align="center">
	<b>
	<!-- [#if preAuthPrescActivities?has_content] -->
		<h3 style="margin-top: 10px"><u>PreAuth Prescription Details</u></h3>
	<!-- [/#if] -->
	</b>
</div>
	
	
<div class="patientHeader">
  	<table cellspacing='0' cellpadding='0' width='100%'>
  	
			<tr>
				<td valign='top'>
					<table cellspacing='0' cellpadding='1' width='100%' >
						
							<tr>
								<td align='left' valign='top'>Name:</td>
								<td align='left' valign='top'>${visitdetails.full_name!}</td>
							</tr>
							<tr>
								<td align='left' valign='top'>Age/Gender:</td>
								<td align='left' valign='top'>${visitdetails.age!} ${visitdetails.agein!}/${visitdetails.patient_gender!}</td>
							</tr>
							<tr>
								<td align='left' valign='top'>Address:</td>
								<td align='left' valign='top'>${visitdetails.patient_address!}</td>
							</tr>
							<tr>
								<td align='left' valign='top'>Location:</td>
								<td align='left' valign='top'>${visitdetails.cityname!}, ${visitdetails.statename!}</td>
							</tr>
							<tr>
								<td align='left' valign='top'>Doctor:</td>
								<td align='left' valign='top'>${visitdetails.doctor_name!}</td>
							</tr>
							<tr>
								<td align='left' valign='top'>Department:</td>
								<td align='left' valign='top'>${visitdetails.dept_name!}</td>
							</tr>
							<!-- [#if (visitdetails.org_name!'GENERAL')!='GENERAL'] -->
							<tr>
								<td align='left' valign='top'>Rate Plan:</td>
								<td align='left' valign='top'>
										${visitdetails.org_name}
								</td>
							</tr>
							<!-- [/#if] -->
							<!-- [#if ((visitdetails.tpa_name!'')!='')] -->
							<tr>
								<td align='left' valign='top'>Sponsor:</td>
								<td align='left' valign='top'>${visitdetails.tpa_name!}</td>
							</tr>
							<!-- [/#if] -->
							<!-- [#if ((visitdetails.insurance_co_name!'')!='')] -->
							<tr>
								<td align='left' valign='top'>Insurance Comp:</td>
								<td align='left' valign='top'>${visitdetails.insurance_co_name!}</td>
							</tr>
							<!-- [/#if] -->
							<!-- [#if ((visitdetails.sec_tpa_name!'')!='')] -->
							<tr>
								<td align='left' valign='top'>Sec.Sponsor:</td>
								<td align='left' valign='top'>${visitdetails.sec_tpa_name!}</td>
							</tr>
							<!-- [/#if] -->
							<!-- [#if ((visitdetails.sec_insurance_co_name!'')!='')] -->
							<tr>
								<td align='left' valign='top'>Sec.Insurance Comp:</td>
								<td align='left' valign='top'>${visitdetails.sec_insurance_co_name!}</td>
							</tr>
							<!-- [/#if] -->
							
						<!-- [#if visitdetails.visit_type == 'i']] -->
						<!-- [#if ((visitdetails.referred_to_hosp!'')!='')] -->
							<tr>
								<td align='left' valign='top'>Referred To:</td>
								<td align='left' valign='top'>${visitdetails.referred_to_hosp!}</td>
							</tr>
						<!-- [/#if] -->
						<!-- [/#if] -->
					
				</table>
			</td>
			<td align='right' valign='top' width="45%">
				<table cellspacing='0' cellpadding='1' style='empty-cells:none;width:100%;'>
				
					<!-- [#if ((label!'')!='')] -->
						<tr >
							<td align='left' valign='top'>${label1!}</td>
							<td align='left' valign='top'>${labelvalue1!}</td>
						</tr>
					<!-- [/#if] -->
					<!-- [#if ((label2!'')!='')] -->
						<tr >
							<td align='left' valign='top'>${label2!}</td>
							<td align='left' valign='top'>${labelvalue2!}</td>
						</tr>
					<!-- [/#if] -->

					<tr>
						<td align='left' valign='top'>MR No:</td>
						<td align='left' valign='top'>${visitdetails.mr_no}</td>
					</tr>
					<tr>
						<td align='left' valign='top'>Visit ID:</td>
						<td align='left' valign='top'>${visitdetails.patient_id}</td>
					</tr>
					<tr>
						<td align='left' valign='top'>
							[#if (visitdetails.visit_type)! = 'i']Admission Date:
							[#else]Visit Date:
							[/#if]
						</td>
						<td align='left' valign='top'>${visitdetails.reg_date!} ${visitdetails.reg_time!}</td>
					</tr>
					
					<!--	[#if visitdetails.visit_type = 'i'] -->
						<tr>
							<td align='left' valign='top'>Ward/Bed:</td>
							<td align='left' valign='top'>
							[#if (modules_activated.mod_ipservices!'') == 'Y']
								[#if (visitdetails.alloc_bed_name!'')=='' ](Not Allocated)
								[#else]${(visitdetails.alloc_ward_name)!}/${(visitdetails.alloc_bed_name)!}
								[/#if]
								[#else]
									${(visitdetails.reg_ward_name)!}/${(visitdetails.bill_bed_type)!}
							[/#if]
							</td>
						</tr>
						<!-- [/#if] -->
							<!-- [#if ((visitdetails.refdoctorname!'')!='')] -->
								<tr>
									<td align='left' valign='top' style='width: 40%'>Referred By:</td>
									<td align='left' valign='top' style='width: 60%'>${(visitdetails.refdoctorname)!}</td>
								</tr>
							<!-- [/#if] -->
							
							<!-- [#if consultation_bean?has_content] -->
								<tr>
									<td align='left' valign='top' style='width: 40%'>Consulting Doctor:</td>
									<td align='left' valign='top' style='width: 60%'>${(consultation_bean.doctor_name)!?html}</td>
  									
  								</tr>
							<!-- [/#if] -->
							
							<!-- [#if ((visitdetails.member_id!'')!='')] -->
							<tr>
								<td align='left' valign='top'>Member Id:</td>
								<td align='left' valign='top'>${visitdetails.member_id!}</td>
							</tr>
							<!-- [/#if] -->
							<!-- [#if ((visitdetails.plan_name!'')!='')] -->
							<tr>
								<td align='left' valign='top'>Plan name :</td>
								<td align='left' valign='top'>${visitdetails.plan_name!}</td>
							</tr>
							<!-- [/#if] -->	
							<!-- [#if ((visitdetails.sec_member_id!'')!='')] -->
							<tr>
								<td align='left' valign='top'>Sec.Member Id:</td>
								<td align='left' valign='top'>${visitdetails.sec_member_id!}</td>
							</tr>
							<!-- [/#if] -->
							<!-- [#if ((visitdetails.sec_plan_name!'')!='')] -->
							<tr>
								<td align='left' valign='top'>Sec.Plan name :</td>
								<td align='left' valign='top'>${visitdetails.sec_plan_name!}</td>
							</tr>
							<!-- [/#if] -->	
							
					</table>
				</td>
			</tr>
			
	</table>
  	
</div>

<!-- [#if diagnosis_details?has_content] -->
			<h3 style="margin-top: 10px"><u>Diagnostic Details</u></h3>
			<table cellspacing='0' cellpadding='1' style='margin-top: 5px' width="100%">
				<tbody>
					<tr>
						<th width="15%">Diag. Type</th>
						<th width="15%">Diag. Code</th>
						<th width="70%">Description</th>
					</tr>
<!--				[#list diagnosis_details as diagnosis] -->
						<tr>
							<td valign="top">
								 [#if diagnosis.diag_type == 'P' ]
									Principal
								 [#elseif diagnosis.diag_type == 'A']Admitting
								 [#elseif diagnosis.diag_type == 'V']Reason For Visit
								 [#else]Secondary
								 [/#if]
							</td>
							<td valign="top">${diagnosis.icd_code!?html}</td>
							<td valign="top">${diagnosis.description!?html}</td>
						</tr>
<!--				[/#list] -->
				</tbody>
			</table>			
<!-- [/#if] -->

<!-- [#if preAuthInv?has_content || preAuthServices?has_content || preAuthOperation?has_content || preAuthDoctor?has_content] -->
	<tr><td colspan="7">&nbsp;</td></tr>					
	<table width="100%" cellspacing="0" cellpadding="0">
		<tr class="border-above-below">
			<th width="12%" align="left">Presc Type</th>
			<th width="26%" align="left">Description</th>
			<th width="8%"  align="left">Rate</th>
			<th width="6%"  align="left">Qty</th>
			<th width="12%"  align="right">Disc</th>
			<th width="12%"  align="right">Gross</th>
			<th width="12%"  align="right">Patient</th>
			<th width="12%"  align="right">Net</th>
		</tr>
	
		<!-- [#if preAuthInv?has_content] -->
			<!-- [#assign subTotal=0]  -->
			<tr>
				<td colspan="3"><b>Investigation</b></td>
			</tr>
			<!-- [#list preAuthInv as test] -->
			<tr>
		
				<td width="12%" align="left">${test.prescribed_date?date!}</td>
				<td width="26%" align="left">${test.preauth_act_name!}</td>
				<td width="8%"  align="left">${test.rate?string("#0.00")!}</td>
				<td width="6%"  align="left">${test.act_qty?string("#0.00")!}</td>
				<td width="12%"  align="right">${test.discount?string("#0.00")!}</td>
				<td width="12%"  align="right">${test.amount?string("#0.00")!}</td>
				<td width="12%"  align="right">${test.patient_share?string("#0.00")!}</td>
				<td width="12%"  align="right">${test.claim_net_amount?string("#0.00")!}</td>
		
			</tr>
			<!-- [#assign subTotal=subTotal+test.amount] -->
			<!-- [/#list] -->
			<tr><td colspan="7">&nbsp;</td></tr>
			<tr>
				<td colspan="7" style="white-space:nowrap"  align="right"><b>Sub Total:</b></td>
				<td align="right"><b>${subTotal?string("#0.00")!}</b></td>
			</tr>
		<!-- [/#if] -->
	
	
	<!-- [#if preAuthServices?has_content] -->
	<!-- [#assign subTotal=0]  -->
	<tr>
		<td colspan="3"><b>Service</b></td>
	</tr>
	<!-- [#list preAuthServices as service] -->
	<tr>
		<td width="12%" align="left">${service.prescribed_date?date!}</td>
		<td width="26%" align="left">${service.preauth_act_name!}</td>
		<td width="8%"  align="left">${service.rate?string("#0.00")!}</td>
		<td width="6%"  align="left">${service.act_qty?string("#0.00")!}</td>
		<td width="12%"  align="right">${service.discount?string("#0.00")!}</td>
		<td width="12%"  align="right">${service.amount?string("#0.00")!}</td>
		<td width="12%"  align="right">${service.patient_share?string("#0.00")!}</td>
		<td width="12%"  align="right">${service.claim_net_amount?string("#0.00")!}</td>
		
			
	</tr>
	<!-- [#assign subTotal=subTotal + service.amount] -->
	<!-- [/#list] -->
	<tr><td colspan="7">&nbsp;</td></tr>
	<tr>
		<td colspan="7" style="white-space:nowrap"  align="right"><b>Sub Total:</b></td>
		<td align="right"><b>${subTotal?string("#0.00")!}</b></td>
	</tr>
	<!-- [/#if] -->
	
	<!-- [#if preAuthOperation?has_content] -->
	<!-- [#assign subTotal=0]  -->
	<tr>
		<td colspan="3"><b>Operation</b></td>
	</tr>
	<!-- [#list preAuthOperation as operation] -->
	<tr>
		<td width="12%" align="left">${operation.prescribed_date?date!}</td>
		<td width="26%" align="left">${operation.preauth_act_name!}</td>
		<td width="8%"  align="left">${operation.rate?string("#0.00")!}</td>
		<td width="6%"  align="left">${operation.act_qty?string("#0.00")!}</td>
		<td width="12%"  align="right">${operation.discount?string("#0.00")!}</td>
		<td width="12%"  align="right">${operation.amount?string("#0.00")!}</td>
		<td width="12%"  align="right">${operation.patient_share?string("#0.00")!}</td>
		<td width="12%"  align="right">${operation.claim_net_amount?string("#0.00")!}</td>
		
	</tr>
	<!-- [#assign subTotal=subTotal + operation.amount] -->
	<!-- [/#list] -->
	<tr><td colspan="7">&nbsp;</td></tr>
	<tr>
		<td colspan="7" style="white-space:nowrap"  align="right"><b>Sub Total:</b></td>
		<td align="right"><b>${subTotal?string("#0.00")!}</b></td>
	</tr>
	<!-- [/#if] -->
	
	<!-- [#if preAuthDoctor?has_content] -->
	<!-- [#assign subTotal=0]  -->
	<tr>
		<td colspan="3"><b>Doctor</b></td>
	</tr>
	<!-- [#list preAuthDoctor as consultation] -->
	<tr>
		<td width="12%" align="left">${consultation.prescribed_date?date!}</td>
		<td width="26%" align="left">${consultation.preauth_act_name!}</td>
		<td width="8%"  align="left">${consultation.rate?string("#0.00")!}</td>
		<td width="6%"  align="left">${consultation.act_qty?string("#0.00")!}</td>
		<td width="12%"  align="right">${consultation.discount?string("#0.00")!}</td>
		<td width="12%"  align="right">${consultation.amount?string("#0.00")!}</td>
		<td width="12%"  align="right">${consultation.patient_share?string("#0.00")!}</td>
		<td width="12%"  align="right">${consultation.claim_net_amount?string("#0.00")!}</td>
		
	</tr>
	<!-- [#assign subTotal=subTotal + consultation.amount] -->
	<!-- [/#list] -->
	<tr><td colspan="7">&nbsp;</td></tr>
	<tr>
		<td colspan="7" style="white-space:nowrap"  align="right"><b>Sub Total:</b></td>
		<td align="right"><b>${subTotal?string("#0.00")!}</b></td>
	</tr>
	<!-- [/#if] -->
	
	<tr><td colspan="7">&nbsp;</td></tr>
	
	<tr>
		<td colspan="7" style="white-space:nowrap" class="border-above-below" align="right"><b>Bill Amount:</b></td>
		<td class="border-above-below" align="right"><b>${totalActAmount.totalAmount?string("#0.00")!}</b></td>
	</tr>
	<tr>
		<td colspan="7" align="right"><b>Total Discount:</b></td>
		<td align="right"><b>${totalActAmount.totalDisc?string("#0.00")!}</b></td>
	</tr>
	<tr>
		<td colspan="7" align="right"><b>TotalGross.Amt</b></td>
		<td align="right"><b>${totalActAmount.totalGrossAmt?string("#0.00")!}</b></td>
	</tr>
	
	<tr>
		<td colspan="7" align="right"><b>TotalPatient.Amt:</b></td>
		<td align="right"><b>${totalActAmount.totalPatientAmt?string("#0.00")!}</b></td>
	</tr>
	<tr>
		<td colspan="7" align="right"><b>ClaimNet.Amt:</b></td>
		<td align="right"><b>${totalActAmount.totalClaimNetAmt?string("#0.00")!}</b></td>
	</tr>
	<tr>
		<td colspan="7" align="right"><b>TotalApprovedNet.Amt:</b></td>
		<td align="right"><b>${totalActAmount.totalApprovedClaimAmt?string("#0.00")!}</b></td>
	</tr>
	
</table>
<!-- [/#if] -->
<!-- [/#escape] -->

	






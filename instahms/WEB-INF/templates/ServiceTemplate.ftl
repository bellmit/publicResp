<!-- [#setting number_format ='##'] -->

<h2 style='text_align;center'> Service Report</h2>


<!-- Patient Details  -->
<div class="patientHeader" >
	<table cellspacing='0' cellpadding='2' width='100%'>
	 <tbody>
	<!--[#if visitDetails??]-->
		<tr>
			 <td width='15%'>Patient&nbsp;Name:</td>
			 <td width='50%'>${visitDetails.fullName}</td>
			 <td width='20%'>Report&nbsp;Date:</td>
			 <td width='15%'>${visitDetails.reportDate}</td>
		</tr>
		<tr>
			 <td width='15%'>Age/Gender:</td>
			 <td width='50%'>${visitDetails.ageAndSex}</td>
			 <!--[#if (visitDetails.mrNo)??]-->
			   <td width='20%'>MR&nbsp;No:</td>
			   <td width='15%'>${visitDetails.mrNo}</td>
			<!--[/#if]-->
		</tr>
		<tr>
			<td width='15%'>Doctor:</td>
			<td width='50%'>${reportDoctor}</td>
			<td width='20%'>Visit No:</td>
			<td width='15%'>${visitDetails.visitNo}</td>
		</tr>
	   <!--[#if (singleServiceDate ) && (reportServiceDate !='')]-->
			<tr>
				<td width='15%'>Service Date: </td>
				<td width='50%'>${reportServiceDate}</td>
				<td width='20%'>${idNoLabel}</td>
			</tr>
		<!--[/#if]
		 [/#if]-->
	  </tbody>
	</table>
</div>
<!--Services  -->

<!-- [#list depts as d] -->
	 <h2 style='text-align: center'>${d.deptName?html}</h2>
	 <table valign='top' cellpadding='2' cellspacing='0' border='0' width='100%'>
	 <tbody>
<!-- [#if (d.valueServices??) && (d.valueServicess?size>0) ] -->
			<tr style='border-bottom: 1px solid black;'>
				 <!-- [#if (d.hasRemarks == true)]
				 	[#assign width = {"Service":"30%","Result":"10%","Units":"10%","Reference Range":"30%","Remarks":"20%"}]
				 	[#assign keys = width?keys]
				  [#else]
					[#assign width = {"Service":"40%","Result":"10%","Units":"10%","Reference Range":"40%"}]
					[#assign keys = width?keys]
			 	 [/#if]-->
			 	 <!--[#list keys as head] -->
					<th align='left' width='${width[head]}'>${head}</th>
				 <!--[/#list]-->
			</tr>
       <!--[#list d.valueServices as  service]
            [#assign singleValueService = false]
			[#if service.results?size == 1]
				[#if service.results[0].resultlabel == service.serviceName]
					[#assign singleValueService = true]
				[/#if]
			[/#if]-->
			<tr><td colspan="5">&nbsp;</td></tr>
			<tr><td colspan='5'>
				 <table width='100%' cellspacing='0' cellpadding='0'>
				   <tbody>
				        <!--[#if !singleValueService]
						       <tr><td colspan='2'><b>${service.serviceName}</b></td></tr>
						    [/#if] -->
						<!--
						<!--[#if !(service.serviceDate??)]-->
							<tr>
								<td align='right'>
							<!-- [#if service.serviceDate??] -->
										Service Date: ${service.serviceDate?string.short}
							<!--[/#if] -->
								</td>
							</tr>
						<!--[/#if]-->
					</tbody>
				</table>
			 </td></tr>
			<!--[#list service.results as result]
				[#if (result.report_value??) && (result.report_value != '')] -->
					<tr>
						<td valign='top'>
						   <!--[#if singleValueService ]--><p style="font-weight: bold;"><!--[/#if] -->
						    ${result.resultlabel?html}
						   <!--[#if singleValueService ]--></p><!--[/#if] -->
						 </td>
						<td valign='top'>
							${result.units?html}
						</td>
						<td>
							${result.reference_range?html}
						</td>
						<td  valign='top'>
							${result.comments?html}
						</td>
					</tr>
			<!--
				[/#if]
			[/#list]
			[#if (service.serviceRemarks??) && (service.serviceRemarks != '')] -->
				<tr><td colspan='5'>Note:&nbsp;${service.serviceRemarks?html}</td></tr>
			<!--[/#if]
		 [/#list]-->
		 <!--[/#if] -->
<!--end of the value list -->

<!--
         [#if d.reportservices??]
			[#list d.reportServices as format]
				   <tr><td colspan='5'>
						 <table width='100%' cellspacing='0' cellpadding='0'>
								<tr><td colspan='2'><b>${format.serviceName?html}</b></td></tr>

								<!--[#if !(service.serviceDate??)] -->
									<tr>
										<td align='right'>
									<!--	[#if format.serviceDate??] -->
												Service Date: ${format.serviceDate?string.short}
									<!--	[/#if] -->
										</td>
								   </tr>
							   <!--[/#if]-->
						</table>
				 </td></tr>
				 <tr><td colspan='5'>
					<div style='margin-top: 10px;'>
					  <!--[#if (format.result.patient_report_file??)]-->
							${format.result.patient_report_file}
					   <!--[/#if]-->
					</div>
				</td></tr>
			<!--[/#list]
         [/#if]
         -->
        </tbody>
		</table>
 <!--[/#list] -->
 <!--end of dept list -->

<br/><br/>
<table cellspacing='0' cellpadding='0' width='100%'>
 <tbody>
 <tr>
<!--  [#if (doctors??) && (doctors?size > 0)]
		[#list doctors as doctor] -->
			<td>
			 	${doctor.DOCTOR_NAME}<br/>
			 	${doctor.SPECIALIZATION?html}
			</td>
<!--	[/#list]
  [/#if]-->
</tr>
</tbody>
</table>

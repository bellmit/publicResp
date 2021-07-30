<!--
Available varialbles are
items,
patDetails

	[#setting time_format="HH:mm"]
	[#setting date_format="dd-MM-yyyy"]
	[#setting datetime_format="dd-MM-yyyy HH:mm"]
-->

<h2 style='text-align: center;'>Clinical Lab Results Print</h2>

[#escape x as x?html]
		<!--[#if patient??] -->

	 		<div class="patientHeader" style="margin-bottom: 1em"  width="100%">
		 			<table width="100%">
		 				<tr>
							<th style="text-align:right">
								MR No:
							</th>
							<td  style="text-align:left">
								${patient.mr_no!}
							</td>
							<th style="text-align:right">
								Name:
							</th>
							<td  style="text-align:left">
								${patient.patient_name!} ${patient.middle_name!} ${patient.last_name!}
							</td>
							<th style="text-align:right">
								Age/Gender:
							</th>
							<td  style="text-align:left">
								<!--

									[#if patient.patient_gender == 'M']
										[#assign gender="Male"]
									[#else]
										[#assign gender="Female"]
									[/#if]
								-->
								${patient.age!} ${patient.agein!}/${gender!}
							</td>
		 				</tr>
		 				<tr>
		 					<th style="text-align:right">
		 						Contact No.:
		 					</th>
		 					<td  style="text-align:left">
		 						${patient.patient_phone}
		 					</td>
		 					<th style="text-align:right">
		 						Blood Group:
		 					</th>
		 					<td style="text-align:left">
								${patient.bloodgroup!}
		 					</td>
		 					<th style="text-align:right">
		 						Remarks:
		 					</th>
		 					<td  style="text-align:left">
		 						${patient.remarks}
		 					</td>
		 				</tr>
		 			</table>
	 			</div>
	 	 	<!--[/#if] -->
	 		<table>
	 			<tr>
	 				<th>
	 					Clinical Test Start Date:
	 				</th>
	 				<td>
						${main.values_as_of_date!}
	 				</td>
	 				<th>
	 					Next Due Date:
	 				</th>
	 				<td>
	 					${main.next_due_date!}
	 				</td>
	 			</tr>
	 		</table>

	 		<!--[#if result??]-->
	 		<table width="100%" style="margin-top: 1em;border: 1px solid black;">
	 			<tr>
		 			<th colspan="2" style="border: 1px solid black;">
		 				Result Name
		 			</th>
		 			<th style="border: 1px solid black;">
		 				Range
		 			</th>
		 			<th style="border: 1px solid black;">
		 				Units
		 			</th>
		 			<th style="border: 1px solid black;">
		 				Value
		 			</th>
		 			<th style="border: 1px solid black;">
		 				Remarks
		 			</th>
		 			<th style="border: 1px solid black;">
		 				Value As Of Date
		 			</th>
	 			</tr>

	 				<!-- [#list result as r]-->
	 				<tr style="border: 1px solid black;">
		 				<td colspan="2" style="border: 1px solid black;">
							${r.resultlabel!} ${r.resultlabel_short!}
		 				</td>
		 				<td style="border: 1px solid black;">
		 					${r.reference_range_txt!}
		 				</td>
		 				<td style="border: 1px solid black;">
		 					${r.units!}
		 				</td>
		 				<td style="border: 1px solid black;">
		 					${r.test_value!}
		 				</td>
		 				<td style="border: 1px solid black;">
		 					${r.remarks!}
		 				</td>
		 				<td style="border: 1px solid black;">
		 					${r.value_date!}
		 				</td>
	 				</tr>
	 				<!--[/#list] -->

	 		</table>
 		<!-- [#else]-->

 		<!-- [/#if]-->
[/#escape]

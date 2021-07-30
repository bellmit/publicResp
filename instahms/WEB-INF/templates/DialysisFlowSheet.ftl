<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<title>Dialysis- Insta HMS</title>

	<style type="text/css">
		@page {
		size: A4 landscape;
		margin: 36pt 36pt 36pt 36pt;
	}
		body {
		font-family : Arial, Helvetica;
		font-size : 10pt;
	}
	</style>
	</head>
[#escape x as x?html]
<body>
[#setting number_format="number"]
[#setting datetime_format="dd-MM-yyyy HH:mm"]
<!--
	all patient sessions has the same center then assign it to center name display it in the patient header section
	otherwise display it in each line(along with date).

	[#assign centerName=""]
	[#if flowSheetDetails?has_content]
		[#list flowSheetDetails as result]
			[#if centerName == ""]
				[#assign centerName=result.center_name]
			[#elseif centerName != result.center_name]
				[#assign centerName=""]
				[#break]
			[/#if]
		[/#list]
	[/#if]

-->
<h1>HAEMODIALYSIS FLOWSHEET REPORT</h1>
<table border='1' cellspacing='0'  width='100%'  >
	<tr>
		<td width='60%' colspan="6"></td>
		<td width='40%' colspan="3">
			<table>
				<tr><td>HAEMODIALYSIS FLOWSHEET</td></tr>
				<tr><td>PATIENT NAME: ${patient.salutation!} ${patient.patient_name!} ${patient.last_name!}</td></tr>
				<tr><td>MRNO: ${patient.mr_no}</td></tr>
				<tr><td>AGE &amp; SEX: ${patient.age!?string("#")} ${patient.agein!} / ${patient.patient_gender!}</td></tr>
				<!-- [#if centerName != ''] -->
					<tr><td>DIALYSIS CENTRE: ${centerName}</td></tr>
				<!-- [/#if] -->
			</table>
		</td>
	</tr>
</table>
<table border='1' cellspacing='0' cellpadding='0' width='100%'>
	<tr>
		<td align='center' width='3%' rowspan='2'>#</td>
		<td align="center" rowspan='2'>Date</td>
		<td  colspan='2' align="center">Time in Hrs</td>
		<td colspan="4" align="center">Weight(Kg)</td>
		<td colspan="2" align="center">Pre BP</td>
		<td colspan="2" align="center">Post BP</td>
		<td rowspan='2' align='center'>Reuse</td>
		<td width='15%'>Medications</td>
		<td width="15%">Drugs Administered</td>
		<td >Remarks</td>
	</tr>
	<tr>
		<td >Start</td>
		<td>End</td>
		<td>Pre</td>
		<td>Gain</td>
		<td>Post</td>
		<td>Loss</td>
		<td>Sitting</td>
		<td>Standing</td>
		<td>Sitting</td>
		<td>Standing</td>
		<td></td>
		<td></td>
	</tr>

		[#assign sno = 0]
		[#assign in_real_wt = 0]
		[#assign fin_real_wt = 0]
		[#assign prev_fin_real_wt = 0]
		[#if flowSheetDetails??]

					[#if finalWt??]
						[#assign prev_fin_real_wt = finalWt]
						[#else][#assign prev_fin_real_wt = 0]
					[/#if]

			[#list flowSheetDetails as result]
				<tr>
						[#assign sno = sno+1]
						<td align='center'>${sno}</td>
						<td align='center'>
							${result.start_time!?substring(0,11)}
							<!-- [#if centerName == ''] -->
								${result.center_name}
							<!-- [/#if] -->
						</td>
						<td>${result.start_time!?substring(11)}</td>
						<td>${result.end_time!?substring(11)}</td>
						<td align="center">${result.in_real_wt!'0'}</td>
										[#if result.in_real_wt??]
											[#assign in_real_wt = result.in_real_wt]
											[#else][#assign in_real_wt = 0]
										[/#if]
										[#if result.fin_real_wt??]
											[#assign fin_real_wt = result.fin_real_wt]
											[#else][#assign fin_real_wt = 0]
										[/#if]

										[#if prev_fin_real_wt == -1]
											<td align="center">--</td>
											[#else]<td align="center">${in_real_wt-prev_fin_real_wt}</td>
										[/#if]
										<td align="center">${result.fin_real_wt!'0'}</td>

										<td align="center">${in_real_wt-fin_real_wt}</td>

						<td>${result.in_bp_high_sit!}/${result.in_bp_low_sit!}</td>
						<td>${result.in_bp_high_stand!}/${result.in_bp_low_stand!}</td>
						<td>${result.fin_bp_high_sit!}/${result.fin_bp_low_sit!}</td>
						<td>${result.fin_bp_high_stand!}/${result.fin_bp_low_stand!}</td>
						<td align='center'>${result.dialyzer_repr_count!}</td>
						<td>
							[#list medications as medicines]
								[#if result.order_id == medicines.order_id]
									${medicines.item_name!},
								[/#if]
							[/#list]

						</td>
						<td>
							[#list drugsAdministered as drugs]
								[#if result.order_id == drugs.order_id]
									${drugs.medicine_name!},
								[/#if]
							[/#list]
						</td>
						<td>${result.start_notes!}</td>

						[#assign prev_fin_real_wt = fin_real_wt]

				</tr>
			[/#list]

		[/#if]

</table>
</body>
[/#escape]
</html>

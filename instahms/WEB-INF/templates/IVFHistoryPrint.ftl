<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<title>IVF History- Insta HMS</title>

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

<h1>IVF HISTORY REPORT</h1>
<table border='1' cellspacing='0'  width='100%'  >
	<tr>
		<td width='60%' colspan="6"></td>
		<td width='40%' colspan="3">
			<table>
				<tr><td>PATIENT NAME: ${patient.salutation!} ${patient.patient_name!} ${patient.last_name!}</td></tr>
				<tr><td>MRNO: ${patient.mr_no}</td></tr>
				<tr><td>AGE &amp; SEX: ${patient.age!?string("#")} ${patient.agein!} / ${patient.patient_gender!}</td></tr>
			</table>
		</td>
	</tr>
</table>
<table border='1' cellspacing='0' cellpadding='0' width='100%'>
	<tr>
		<td align='center' width='3%'>#</td>
		<td align="center">Cycle Start Date</td>
		<td align="center">Protocol</td>
		<td align="center">Total dose of GT</td>
		<td align="center">Peak E</td>
		<td align="center">Oocyte Obtained</td>
		<td align="center">No. Fertilized</td>
		<td align="center" colspan="2">Embryos Transfer</td>
		<td align="center" colspan="3">Embryos Frozen</td>
	</tr>
	<tr>
		<td colspan="7"></td>
		<td align="center">No</td>
		<td align="center">Grade</td>
		<td align="center">No</td>
		<td align="center">Stage</td>
		<td align="center">Grade</td>
	</tr>

		[#assign sno = 0]
		[#if ivfhistorydetails??]

			[#list ivfhistorydetails as result]
				<tr>
						[#assign sno = sno+1]
						<td align="center">${sno}</td>
						<td align="center">
							${result.start_date!?substring(0)}
						</td>
						<td align="center">${result.protocol!}</td>
						<td align="center">${result.gndtropin_dose!}</td>
						<td align="center">${result.peak_e!}</td>
						<td align="center">${result.tot_oocyte!}</td>
						<td align="center">${result.fertilization_rate_number!}</td>
						<td colspan="2">
							<table border='0' cellspacing='0' cellpadding='0' width='100%'>
								[#assign etno = 0]
								[#if etList??]
									[#list etList as et]
										[#if et.ivf_cycle_id == result.ivf_cycle_id]
											<tr>
												[#assign etno = etno+1]
												<td align="center">${et.emb_number!}</td>
												<td align="center">${et.emb_grade!}</td>
											</tr>
										[/#if]
									[/#list]
								[/#if]
							</table>
						</td>
						<td colspan="3">
							<table border='0' cellspacing='0' cellpadding='0' width='100%'>
								[#assign efno = 0]
								[#if efList??]

									[#list efList as ef]
										[#if ef.ivf_cycle_id == result.ivf_cycle_id]
											<tr>
												[#assign efno = efno+1]
												<td align="center">${ef.emb_number!}</td>
												<td align="center">${ef.emb_state!}</td>
												<td align="center">${ef.emb_grade!}</td>
											</tr>
										[/#if]
									[/#list]

								[/#if]
							</table>
						</td>
				</tr>
			[/#list]

		[/#if]

</table>
</body>
[/#escape]
</html>

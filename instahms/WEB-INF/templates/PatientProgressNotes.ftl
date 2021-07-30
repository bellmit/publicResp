<!--[#escape x as x?html]-->
	<div>
		<b><u>Patient Progress Notes</u></b>
	</div>
	<div style="padding-bottom: 20px" class="patientHeader">
		<table width="100%" cellspacing="0" cellpadding="0">
			<tr>
				<td>Name:</td>
				<td>${patient.full_name!}</td>
				<td>Age/Gender:</td>
				<td>${patient.age!?string("#")} ${patient.agein!} ${patient.patient_gender!}</td>
			</tr>
			<tr>
				<td>Address:</td>
				<td>${patient.patient_address!}</td>
				<td>MR No:</td>
				<td>${patient.mr_no!}</td>
			</tr>
			<tr>
				<td>Location:</td>
				<td>${patient.cityname!},${patient.statename!}</td>
			</tr>
		</table>
	</div>
<!--[/#escape]-->
	<!--	[#list progressNotesList as notes] -->
	<table width="100%" cellspacing="0" cellpadding="0">
				<tr>
					<td style="padding-bottom: 4px; padding-top: 4px"><b><u>Progress Notes On ${notes.date_time!} By ${notes.doctor_name!} (User: ${notes.username!})</u></b></td>
				</tr>
				<tr>
					<td class="notes">
						${((notes.notes!)?html)?replace("\n","<br/>")}
					</td>
				</tr>
	</table>
	<!--	[/#list] -->

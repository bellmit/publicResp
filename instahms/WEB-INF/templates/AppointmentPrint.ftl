
	<div class="patientHeader">
	  	<table cellspacing='0' cellpadding='0' width='100%'>
		  	<tbody>
				<!-- [#escape x as x?html] -->

				<!-- [#if category == 'DOC'] -->
					<tr>
						<td valign='top'>
							<table cellspacing='0' cellpadding='0' width='100%' border="0">
								<tbody>
									<tr>
										<td colspan="2">
											<hr style="border: 0.5px solid #000000;" />
										</td>
									</tr>
									<tr>
										<td align="center" colspan="2">Doctor Appointment</td>
									</tr>
									<tr>
										<td align="center" colspan="2">${" "}</td>
									</tr>
									<tr>
										<td align="left" valign='top' width="30%">Patient Name:</td>
										<td align='left' valign='top' width="70%">${appointmentDetails[0].patient_name!}</td>
									</tr>
									<tr>
										<td align='left' valign='top'>MR No.:</td>
										<td align='left' valign='top'>${appointmentDetails[0].mr_no!}</td>
									</tr>
									<tr>
										<td align='left' valign='top'>Doctor Name:</td>
										<td align='left' valign='top'>${appointmentDetails[0].booked_resource!}</td>
									</tr>
									<tr>
										<td align='left' valign='top'>Appointment Date:</td>
										<td align='left' valign='top'>${appointmentDetails[0].appointment_date!}</td>
									</tr>
									<tr>
										<td align='left' valign='top'>Appointment Time:</td>
										<td align='left' valign='top'>${appointmentDetails[0].appointment_time!}</td>
									</tr>
									<tr>
										<td align='left' valign='top'>Duration:</td>
										<td align='left' valign='top'>${appointmentDetails[0].duration!}</td>
									</tr>
									<tr>
										<td colspan="2">
											${" "}
										</td>
									</tr>
									<tr>
										<td colspan="2">
											Please arrive 10 minutes before the appointment time.
										</td>
									</tr>
									<tr>
										<td colspan="2">
											<hr style="border: 0.5px solid #000000;" />
										</td>
									</tr>
								</tbody>
							</table>
						</td>
					</tr>
				<!-- [/#if] -->

				<!-- [#if category == 'OPE'] -->

					<tr>
						<td valign='top'>
							<table cellspacing='0' cellpadding='0' width='100%' border="0">
								<tbody>
									<tr>
										<td colspan="2">
											<hr style="border: 0.5px solid #000000;" />
										</td>
									</tr>
									<tr>
										<td align="center" colspan="2">Surgery Appointment</td>
									</tr>
									<tr>
										<td align="center" colspan="2">${" "}</td>
									</tr>
									<tr>
										<td align="left" valign='top' width="30%">Patient Name:</td>
										<td align='left' valign='top' width="70%">${appointmentDetails[0].patient_name!}</td>
									</tr>
									<tr>
										<td align='left' valign='top'>MR No.:</td>
										<td align='left' valign='top'>${appointmentDetails[0].mr_no!}</td>
									</tr>
									<tr>
										<td align='left' valign='top'>Surgery Name:</td>
										<td align='left' valign='top'>${appointmentDetails[0].booked_resource!}</td>
									</tr>
									<tr>
										<td align='left' valign='top'>Appointment Date:</td>
										<td align='left' valign='top'>${appointmentDetails[0].appointment_date!}</td>
									</tr>
									<tr>
										<td align='left' valign='top'>Appointment Time:</td>
										<td align='left' valign='top'>${appointmentDetails[0].appointment_time!}</td>
									</tr>
									<tr>
										<td align='left' valign='top'>Duration:</td>
										<td align='left' valign='top'>${appointmentDetails[0].duration!}</td>
									</tr>
									<tr>
										<td colspan="2">
											${" "}
										</td>
									</tr>
									<tr>
										<td colspan="2">
											Please arrive 10 minutes before the appointment time.
										</td>
									</tr>
									<tr>
										<td colspan="2">
											<hr style="border: 0.5px solid #000000;" />
										</td>
									</tr>
								</tbody>
							</table>
						</td>
				</tr>

			<!-- [/#if] -->

			<!-- [#if category == 'SNP'] -->

					<tr>
						<td valign='top'>
							<table cellspacing='0' cellpadding='0' width='100%' border="0">
								<tbody>
									<tr>
										<td colspan="2">
											<hr style="border: 0.5px solid #000000;" />
										</td>
									</tr>
									<tr>
										<td align="center" colspan="2">Service Appointment</td>
									</tr>
									<tr>
										<td align="center" colspan="2">${" "}</td>
									</tr>
									<tr>
										<td align="left" valign='top' width="30%">Patient Name:</td>
										<td align='left' valign='top' width="70%">${appointmentDetails[0].patient_name!}</td>
									</tr>
									<tr>
										<td align='left' valign='top'>MR No.:</td>
										<td align='left' valign='top'>${appointmentDetails[0].mr_no!}</td>
									</tr>
									<tr>
										<td align='left' valign='top'>Service Name:</td>
										<td align='left' valign='top'>${appointmentDetails[0].booked_resource!}</td>
									</tr>
									<tr>
										<td align='left' valign='top'>Appointment Date:</td>
										<td align='left' valign='top'>${appointmentDetails[0].appointment_date!}</td>
									</tr>
									<tr>
										<td align='left' valign='top'>Appointment Time:</td>
										<td align='left' valign='top'>${appointmentDetails[0].appointment_time!}</td>
									</tr>
									<tr>
										<td align='left' valign='top'>Duration:</td>
										<td align='left' valign='top'>${appointmentDetails[0].duration!}</td>
									</tr>
									<tr>
										<td colspan="2">
											${" "}
										</td>
									</tr>
									<tr>
										<td colspan="2">
											Please arrive 10 minutes before the appointment time.
										</td>
									</tr>
									<tr>
										<td colspan="2">
											<hr style="border: 0.5px solid #000000;" />
										</td>
									</tr>
								</tbody>
							</table>
						</td>
				</tr>

			<!-- [/#if] -->

			<!-- [#if category == 'DIA'] -->

					<tr>
						<td valign='top'>
							<table cellspacing='0' cellpadding='0' width='100%' border="0">
								<tbody>
									<tr>
										<td colspan="2">
											<hr style="border: 0.5px solid #000000;" />
										</td>
									</tr>
									<tr>
										<td align="center" colspan="2">Test Appointment</td>
									</tr>
									<tr>
										<td align="center" colspan="2">${" "}</td>
									</tr>
									<tr>
										<td align="left" valign='top' width="30%">Patient Name:</td>
										<td align='left' valign='top' width="70%">${appointmentDetails[0].patient_name!}</td>
									</tr>
									<tr>
										<td align='left' valign='top'>MR No.:</td>
										<td align='left' valign='top'>${appointmentDetails[0].mr_no!}</td>
									</tr>
									<tr>
										<td align='left' valign='top'>Test Name:</td>
										<td align='left' valign='top'>${appointmentDetails[0].booked_resource!}</td>
									</tr>
									<tr>
										<td align='left' valign='top'>Appointment Date:</td>
										<td align='left' valign='top'>${appointmentDetails[0].appointment_date!}</td>
									</tr>
									<tr>
										<td align='left' valign='top'>Appointment Time:</td>
										<td align='left' valign='top'>${appointmentDetails[0].appointment_time!}</td>
									</tr>
									<tr>
										<td align='left' valign='top'>Duration:</td>
										<td align='left' valign='top'>${appointmentDetails[0].duration!}</td>
									</tr>
									<tr>
										<td colspan="2">
											${" "}
										</td>
									</tr>
									<tr>
										<td colspan="2">
											Please arrive 10 minutes before the appointment time.
										</td>
									</tr>
									<tr>
										<td colspan="2">
											<hr style="border: 0.5px solid #000000;" />
										</td>
									</tr>
								</tbody>
							</table>
						</td>
				</tr>

			<!-- [/#if] -->

			<!-- [/#escape] -->
			</tbody>
		</table>

	</div>
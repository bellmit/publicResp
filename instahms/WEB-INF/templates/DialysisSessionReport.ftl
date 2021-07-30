<div align="center">
	<b><u>Dialysis Report Chart</u></b>
</div>
<div class="patientHeader" style="margin-bottom: 1em">
	<table  width="50%" cellspacing="0" cellpadding="0">
		<tr>
			<td>Name:</td>
			<td><b>${patient.salutation!} ${patient.patient_name!} ${patient.last_name!}</b></td>
		</tr>
		<tr>
			<td>MR No:</td>
			<td><b>${patient.mr_no}</b></td>
		</tr>
		<tr>
			<td>Age/Gender:</td>
			<td><b>${patient.age!?string("#")} ${patient.agein!} / ${patient.patient_gender!}</b></td>
		</tr>
		<tr>
			<td>Virology Status:</td>
			<td><b>${(patient.custom_field1!)?html}</b></td>
		</tr>
		<tr>
			<td>Center Name:</td>
			<td>${SessionDetails.center_name!}</td>
		</tr>
	</table>
</div>
<div>
<table border="1" width="100%" cellspacing="0" cellpadding="0">
	<tr>
		<td>
			<table>
				<tr>
					<td>Machine#</td>
				</tr>
				<tr>
					<td><b>${SessionDetails.machine_name}</b></td>
				</tr>
			</table>
		</td>
		<td>
			<table>
				<tr>
					<td>Station</td>
				</tr>
				<tr>
					<td><b>${SessionDetails.location_name}</b></td>
				</tr>
			</table>
		</td>
		<td>
			<table>
				<tr>
					<td>Allocated Nurse</td>
				</tr>
				<tr>
					<td><b>${SessionDetails.start_attendant!}</b></td>
				</tr>
			</table>
		</td>
		<td>
			<table>
				<tr>
					<td>AK / Re-use#</td>
				</tr>
				<tr>
					<td><b>${SessionDetails.dialyzer_repr_count!}</b></td>
				</tr>
			</table>
		</td>
		<td>
			<table>
				<tr>
					<td>Date</td>
				</tr>
				<tr>
					<td><b>${SessionDetails.start_date!}</b></td>
				</tr>
			</table>
		</td>
		<td>
			<table>
				<tr>
					<td>Prescribed Time</td>
				</tr>
				<tr>
					<td><b>${SessionDetails.est_duration!}</b>(mins)</td>
				</tr>
			</table>
		</td>
		<td>
			<table>
				<tr>
					<td>Start</td>
				</tr>
				<tr>
					<td><b>${SessionDetails.start_time!}</b></td>
				</tr>
			</table>
		</td>
		<td>
			<table>
				<tr>
					<td>End</td>
				</tr>
				<tr>
					<td><b>${SessionDetails.end_time!}</b></td>
				</tr>
			</table>
		</td>
	</tr>
	<tr>
		<td colspan="4">
			<table>
				<tr height="5%"><td></td></tr>
				<!-- [#list prepParamMasterValues as prepMaster] -->
					<tr>
						<td>${prepMaster.prep_param!}</td>
					</tr>
				<!-- [/#list] -->
				[#if (SessionDetails.in_odometer_reading?? )
					|| (SessionDetails.fin_odometer_reading??)]
					<tr>
						<td>Dialysis Odometer Reading Start/End</td>
					</tr>
				[/#if]
			</table>
		</td>
		<td>
			<table>
				<tr height="5%"><td></td></tr>
				<!-- [#list prepParamValues as prepParamVal] -->
					<tr>
						<td><b>${prepParamVal.prep_param_name!}</b></td>
					</tr>
				<!-- [/#list] -->
				[#if (SessionDetails.in_odometer_reading??)
					|| (SessionDetails.fin_odometer_reading?? )]
					<tr>
						<td>[#if SessionDetails.in_odometer_reading?has_content]
								${SessionDetails.in_odometer_reading}
								[#else]--
							[/#if]
							[#if SessionDetails.fin_odometer_reading?has_content]/${SessionDetails.fin_odometer_reading}
								[#else]/--
							[/#if]
						</td>
					</tr>
				[/#if]
			</table>
		</td>
		<td valign="top">
			<table border="0" width="100%">
				<tr>
					<td valign="top">Checked by:</td>
				</tr>
				<tr><td><b>${SessionDetails.dialyzer_check_user2!}</b></td></tr>
			</table>
		</td>
		<td colspan="4" >
			<table border="0" width="100%">
				<tr>
					<td align="center" colspan="4">
						 [#if SessionDetails.anticoagulation == "H"]
						 	Anticoagulation - Heparin
						 [#else]Normal Saline Flushing
						 [/#if]
					</td>
				</tr>
				<tr><td colspan="4"><hr width="30%" /></td></tr>
				[#if SessionDetails.anticoagulation == "H"]
				<tr>
					<td valign="top">
						<table>
							<tr>
								<td>Bolus/ unit</td>
							</tr>
							<tr>
								<td><b>${SessionDetails.heparin_bolus!}</b></td>
							</tr>
						</table>
					</td>
					<td valign="top">
						<table>
							<tr>
								<td>Hourly units</td>
							</tr>
							<tr>
								<td><b>${SessionDetails.heparin_rate!}</b></td>
							</tr>
						</table>
					</td>
				</tr>
				[#else]
				<tr>
					<td valign="top">
						<table>
							<tr>
								<td>Frequency</td>
							</tr>
							<tr>
								<td><b>${SessionDetails.frequency!}</b></td>
							</tr>
						</table>
					</td>
					<td valign="top">
						<table>
							<tr>
								<td>Volume</td>
							</tr>
							<tr>
								<td><b>${SessionDetails.volume!}</b></td>
							</tr>
						</table>
					</td>
				</tr>
				[/#if]
			</table>
		</td>
	</tr>
	<tr>
		<td colspan="4" valign="top">
			<table>
				<tr>
					<td >Temperature:</td>
					<td><b>${SessionDetails.dialysate_temp!}</b></td>
				</tr>
				<tr>
					<td >Conductivity:</td>
					<td><b>${SessionDetails.first_dialysate_cond!}</b></td>
				</tr>
				<tr>
					<td >Dialysate Type:</td>
					<td><b>${SessionDetails.dialysate_type_name!}</b></td>
				</tr>
				<tr>
					<td >Access Type:</td>
					<td><b>${SessionDetails.access_type!}</b></td>
				</tr>
			</table>
		</td>
	</tr>
	<tr>
		<td colspan="8" align="center" style="background-color: gray"><b>PRE DIALYSIS / ASSESSMENT</b></td>
	</tr>
	<tr>
		<td colspan="2" >
			<table>
				<tr>
					<td>Weight Today:</td>
					<td><b>${SessionDetails.in_real_wt!}</b></td>
				</tr>
				<tr>
					<td>Est Dry Weight:</td>
					<td><b>${SessionDetails.target_weight!}</b></td>
				</tr>
				<tr>
					<td>BP (Sitting):</td>
					<td><b>${SessionDetails.in_bp_high_sit!} / ${SessionDetails.in_bp_low_sit!}</b></td>
				</tr>
				<tr>
					<td>Body Temperature:</td>
					<td><b>${SessionDetails.in_temperature!}</b></td>
				</tr>
				<tr>
					<td>UF Target:</td>
					<td><b>${uf_target!}</b></td>
				</tr>
				<tr>
					<td>Bruit / Thrill:</td>
					<td>${SessionDetails.patency_bruit_thrill!}</td>
				</tr>
				<tr>
					<td>Access Infection:</td>
					<td><b>${SessionDetails.access_site_infection!}</b></td>
				</tr>
			</table>
		</td>
		<td colspan="2" valign="top">
			<table>
				<tr>
					<td >Weight gained:</td>
					<td></td>
				</tr>
				<tr>
					<td >Difference:</td>
					<td></td>
				</tr>
				<tr>
					<td >BP (Standing):</td>
					<td><b>${SessionDetails.in_bp_high_stand!} / ${SessionDetails.in_bp_low_stand!}</b></td>
				</tr>
				<tr>
					<td >Pulse:</td>
					<td><b>${SessionDetails.first_pulse_rate!}</b></td>
				</tr>
				<tr>
					<td >Target Weight:</td>
					<td><b>${SessionDetails.target_wt!}</b></td>
				</tr>
				<tr>
					<td >Excess Weight:</td>
					<td><b>${excessWt!}</b></td>
				</tr>
			</table>
		</td>
		<td colspan="2" width="40%" valign="top">
			<table>
				<tr>
					<td>Chest Ausculation - clear</td>
				</tr>
				<tr>
					<td>Peripheral Edema</td>
				</tr>
				<tr>
					<td>Physical/chest pain or disconfort</td>
				</tr>
				<tr>
					<td >Recent Surgery / Injuries / Trauma / Bleeding</td>
				</tr>
				<tr>
					<td>Intra Dialysis Complaints</td>
				</tr>
				<tr>
					<td>Breakfast/lunch/Dinner</td>
				</tr>
				<tr>
					<td >Perm Cath Flow</td>
				</tr>
			</table>
		</td>
		<td colspan="2" valign="top">
			<table>
				<tr>
					<td><b>${SessionDetails.chest_auscultation_clear!}</b></td>
				</tr>
				<tr>
					<td><b>${SessionDetails.peripheral_edema!}</b></td>
				</tr>
				<tr>
					<td><b>${SessionDetails.chest_pain_discomfort!}</b></td>
				</tr>
				<tr>
					<td><b>${SessionDetails.recent_surgery!}</b></td>
				</tr>
				<tr>
					<td><b>${SessionDetails.intradialysis_complaints!}</b></td>
				</tr>
				<tr>
					<td><b>${SessionDetails.breakfast_lunch_dinner!}</b></td>
				</tr>
				<tr>
					<td><b>${SessionDetails.perm_cath_flow!}</b></td>
				</tr>
			</table>
		</td>
	</tr>
	<tr>
		<td colspan="8" >Commenced By:   ${SessionDetails.dialyzer_check_user2!}</td>
	</tr>
	<tr>
		<td colspan="8">
			<table  width="100%" cellspacing="0" cellpadding="0">
				<tr >
					<td>
						<table border="1" width="100%" cellspacing="0" cellpadding="0">
							<tr style="background-color: gray">
								<td><b>Time</b></td>
								<td><b>BP</b></td>
								<td><b>PR</b></td>
								<td><b>VP</b></td>
								<td><b>TMP</b></td>
								<td><b>UFR</b></td>
								<td><b>Total UF</b></td>
								<td><b>BFR</b></td>
								<td><b>COND.</b></td>
								<td><b>Remarks</b></td>
								<td><b>Sign</b></td>
							</tr>
							<!-- [#list IntraDetails as intra] -->
							<tr>
								<td valign="top" style="padding-left: 6px">${intra.obs_time!}</td>
								<td valign="top" style="padding-left: 6px">${intra.bp_high!} / ${intra.bp_low!}</td>
								<td valign="top" style="padding-left: 6px">${intra.pulse_rate!}</td>
								<td valign="top" style="padding-left: 6px">${intra.venous_pressure!}</td>
								<td valign="top" style="padding-left: 6px">${intra.dialysate_temp!}</td>
								<td valign="top" style="padding-left: 6px">${intra.uf_rate!}</td>
								<td valign="top" style="padding-left: 6px">${intra.uf_removed!}</td>
								<td valign="top" style="padding-left: 6px">${intra.blood_pump_rate!}</td>
								<td valign="top" style="padding-left: 6px">${intra.dialysate_cond!}</td>
								<td valign="top" style="padding-left: 6px"></td>
								<td valign="top" style="padding-left: 6px"></td>
							</tr>
							<!--[/#list]-->
						</table>
					</td>
				</tr>
			</table>
		</td>
	</tr>
	<tr>
		<td colspan="8" align="center" style="background-color: gray"><b>POST DIALYSIS / ASSESSMENT</b></td>
	</tr>
	<tr>
		<td colspan="2" valign="top">
			<table>
				<tr>
					<td>Post Weight:</td>
					<td><b>${SessionDetails.fin_real_wt!}</b></td>
				</tr>
				<tr>
					<td>BP (Sitting):</td>
					<td><b>${SessionDetails.fin_bp_high_sit!} / ${SessionDetails.fin_bp_low_sit!}</b></td>
				</tr>
				<tr>
					<td>Symptomatic Hypotension:</td>
					<td></td>
				</tr>
				<tr>
					<td>Prolonged Bleeding at <br/>punctured sites:</td>
					<td>${SessionDetails.prolonged_bleeding_at_sites!}</td>
				</tr>
				<tr>
					<td>Bruit / Thrill (AVF / AVG):</td>
					<td>${SessionDetails.fin_patency_bruit_thrill!}</td>
				</tr>
			</table>
		</td>
		<td colspan="2" valign="top">
			<table>
				<tr>
					<td>Weight Loss:</td>
					<td><b>${wt_loss!}</b></td>
				</tr>
				<tr>
					<td>BP (Standing):</td>
					<td><b>${SessionDetails.fin_bp_high_stand!} / ${SessionDetails.fin_bp_low_stand!}</b></td>
				</tr>
			</table>
		</td>
		<td colspan="4" valign="top">
			<table>
				<tr>
					<td>UF Reading:</td>
					<td>${SessionDetails.last_uf_removed!}</td>
				</tr>
				<tr>
					<td>Body Temperature:</td>
					<td><b>${SessionDetails.fin_temperature!}</b> </td>
				</tr>
				<tr>
					<td>Pulse:</td>
					<td><b>${SessionDetails.last_pulse_rate!}</b> </td>
				</tr>
				<tr>
					<td>Next Dialysis Date:</td>
					<td>${SessionDetails.nxt_dialysis_date!}</td>
				</tr>
				<tr>
					<td>Shift:</td>
					<td>${SessionDetails.shift!}</td>
				</tr>
				<tr>
					<td>Condition/Assessment:</td>
					<td>${SessionDetails.fin_patient_cond!}</td>
				</tr>
			</table>
		</td>
	</tr>
	<tr>
		<td colspan="8" align="center" style="background-color: gray"><b>Administered Drugs</b></td>
	</tr>
	<tr>
		<td colspan="4" valign="top">
			Drugs:
		</td>
		<td colspan="4" valign="top">${drugs!}</td>
	</tr>
	<tr>
		<td colspan="8" align="center" style="background-color: gray"><b>Notes</b></td>
	</tr>
	<tr>
		<td colspan="4" valign="top">
		<table>
			<tr>
			<td colspan="1" width="40%">Completion Notes:</td>
			<td colspan="3">${(SessionDetails.completion_notes!)?html}</td>
			</tr>
		</table>
		</td>
		<td colspan="4" valign="top">
		<table>
			<tr>
			<td colspan="1" width="50%">Notes to be shown in next Pre Dialysis Session:</td>
			<td colspan="3">${(SessionDetails.post_session_notes!)?html}</td>
			</tr>
		</table>
		</td>
	</tr>
	<tr>
		<td colspan="8" align="center" style="background-color: gray"><b>Treatment Chart</b></td>
	</tr>
	<tr>
		<td colspan="8">
			<table border="1" width="100%" cellspacing="0" cellpadding="0">
				<tr style="background-color: gray">
					<td><b>Medicine Name</b></td>
					<td><b>Presc Date</b></td>
					<td><b>Dosage</b></td>
					<td><b>Frequency</b></td>
					<td><b>Route</b></td>
					<td><b>Remarks</b></td>
				</tr>
				<!--[#list treatmentList as treatment] -->
					<tr>
						<td>${(treatment.item_name!)?html}</td>
						<td>${(treatment.prescribed_date!)?html}</td>
						<td>${(treatment.medicine_dosage!)?html}</td>
						<td>${(treatment.display_name!)?html}</td>
						<td>${(treatment.route_name!)?html}</td>
						<td>${(treatment.remarks!)?html}</td>
					</tr>
				<!--[/#list] -->
			</table>
		</td>
	</tr>
	<tr>
		<td colspan="8" align="left" >Concluded By:   ${SessionDetails.end_attendant!}</td>
	</tr>
</table>
</div>

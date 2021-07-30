function getVisitDoctorConsultationDetails(mrno){
	    var ajaxobj1 = newXMLHttpRequest();
		var url = cpath+'/pages/registration/regUtils.do?_method=getPatientDetailsJSON&mrno='+mrno;
		var reqObject = newXMLHttpRequest();
		reqObject.open("POST", url.toString(), true);
		reqObject.send(null);
		reqObject.onreadystatechange = function() {
		  if (reqObject.readyState == 4 && reqObject.status == 200 && reqObject.responseText != null) {
		    return eval('('+reqObject.responseText+')');
		  }
		}
	}
function isRevisitAfterDischarge(doctorId, patientLastIpVisit, patientFollowUpDoctorVisits) {
	if (empty(patientLastIpVisit))
		return false;
	var admitDoctor = patientLastIpVisit.admitting_doctor;
	var dischargeDate = new Date(patientLastIpVisit.discharge_date);
	var dischargeTime = patientLastIpVisit.discharge_time;

	if (doctorId == null)
		return false;

	if (!empty(admitDoctor) &&  admitDoctor == doctorId ) {
		var doctor = findInList(doctorsList, 'doctor_id', admitDoctor);

		if (doctor == null)
			return false;

		var ipDischargeConsValidityDays = doctor.ip_discharge_consultation_validity;
		var ipDischargeConsultationCount = doctor.ip_discharge_consultation_count;

		var revisitCount = 0;
		var mainVisitWithinValidity = false;

		if (patientFollowUpDoctorVisits == null || patientFollowUpDoctorVisits == '') {
			if (daysDiff(dischargeDate, getServerDate()) <= ipDischargeConsValidityDays) {
				return true;
			}
		}else {
			for (var i=0; i<patientFollowUpDoctorVisits.length; i++) {
				var cons = patientFollowUpDoctorVisits[i];
				if (cons.doctor_name != doctorId)
					continue;
				var visitDate = new Date(cons.visited_date);
				if ((daysDiff(dischargeDate , visitDate) > 0) &&
						(daysDiff(visitDate, getServerDate()) <= ipDischargeConsValidityDays)) {
					if (cons.visit_consultation_type == '-4') {
						revisitCount++;
						mainVisitWithinValidity = true;
					}
				}
			}
		}
		return mainVisitWithinValidity && (revisitCount < ipDischargeConsultationCount);
	}
}

/*
 * Check if the doctor consultation is a revisit based on rules.
 */
function isRevisit(doctorId, patientDoctorVisits) {
	if (patientDoctorVisits == null) {
		return false;
	}

	if (doctorId == null)
		return false;

	var doctor = findInList(doctorsList, 'doctor_id', doctorId);

	if (doctor == null)
		return false;

	var validityDays = doctor.op_consultation_validity;
	var maxVisits = doctor.allowed_revisit_count;

	/*
	 * Go through the patientDoctorVisits and look for the following:
	 *  1. There should be an OPDOC (ie, not revisit) consultation within the validity period
	 *  2. Number of ROPDOC within the validity period should be <= maxVisits
	 * If both are true, then, we can default to a revisit. Else, it becomes a regular visit.
	 * Note: patientDoctorVisits has all doctor visits, need to look at only the doctor
	 * which has been selected (ie, orderItemId)
	 */
	var revisitCount = 0;
	var mainVisitWithinValidity = false;
	for (var i=0; i<patientDoctorVisits.length; i++) {
		var cons = patientDoctorVisits[i];
		if (cons.doctor_name != doctorId)
			continue;
		var visitDate = new Date(cons.visited_date);
		if (daysDiff(visitDate, getServerDate()) <= validityDays) {
			if (cons.visit_consultation_type == '-2')
				revisitCount++;
			else if (cons.visit_consultation_type == '-1')
				mainVisitWithinValidity = true;
		}
	}
	return mainVisitWithinValidity && (revisitCount < maxVisits);
}

package com.insta.hms.emr;

import java.sql.SQLException;
import java.text.ParseException;
import java.util.List;

public class EMRService {

	public static List<EMRDoc> getPatientViewEMR(String patientId) throws ParseException, SQLException {
		return new DIAGProviderBOImpl().listPatientViewDocForMrNo(patientId);
	}
}

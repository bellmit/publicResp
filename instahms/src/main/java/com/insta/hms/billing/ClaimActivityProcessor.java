package com.insta.hms.billing;

import java.io.IOException;
import java.sql.SQLException;

public class ClaimActivityProcessor {

	ClaimSubmissionDAO submitdao = new ClaimSubmissionDAO();

	public void process(String submissionBatchId, String isResubmission, String healthAuthority) throws SQLException,IOException{

			submitdao.updateClaimActivityId(submissionBatchId, isResubmission);
			
			submitdao.insertObservationForUnlistedItems(submissionBatchId, healthAuthority);
	}
}
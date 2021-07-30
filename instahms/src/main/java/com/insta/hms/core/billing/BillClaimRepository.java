package com.insta.hms.core.billing;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.insta.hms.common.DatabaseHelper.queryToDynaBean;

@Repository
public class BillClaimRepository extends GenericRepository{

	public BillClaimRepository() {
		super("bill_claim");
	}
	
	private static final String GET_VISIT_CLAIM_ID = " SELECT icl.claim_id "+
			" FROM bill_claim bc "+
			" JOIN bill b ON (bc.bill_no = b.bill_no AND bc.visit_id = b.visit_id) "+
			" JOIN insurance_claim icl on(icl.claim_id = bc.claim_id) "+
			" WHERE bc.visit_id = ? AND bc.plan_id = ? AND b.account_group = ?	AND icl.status = 'O' " +
			" ORDER BY icl.claim_id DESC LIMIT 1 ";

	public String getVisitClaimId(Object[] obj) {
		return DatabaseHelper.getString(GET_VISIT_CLAIM_ID, obj);		
	}
	
	private static final String GET_CLAIM_ID_FROM_BILL_CLAIM = "SELECT claim_id as claim_id"
			+ " FROM bill_claim WHERE visit_id=? AND bill_no=? AND plan_id=? ";

	public String getClaimId(Object[] obj) {
		return DatabaseHelper.getString(GET_CLAIM_ID_FROM_BILL_CLAIM, obj);
	}

	private static final String GET_MRNO_BY_CLAIM_ID = "SELECT mr_no FROM patient_registration pr" +
			" INNER JOIN bill_claim bc ON (pr.patient_id = bc.visit_id)" +
			" WHERE bc.claim_id in (:claimId)";
	public List<BasicDynaBean> getMrNosByClaimId(List<String> claimIds) {
		MapSqlParameterSource parameters = new MapSqlParameterSource();
		parameters.addValue("claimId", claimIds);
		return DatabaseHelper.queryToDynaList(GET_MRNO_BY_CLAIM_ID, parameters);
	}
	
	private static final String FIND_BILLS = " SELECT b.bill_no,b.status,b.bill_type,b.restriction_type,b.primary_claim_status " +
      " FROM bill b " +
      " JOIN bill_claim bclm ON(b.bill_no = bclm.bill_no)"+
      " WHERE bclm.claim_id = ? AND b.status != 'X' AND b.total_amount >= 0 ORDER BY b.bill_no ";
	
	public List<BasicDynaBean> findAllBills(String claim_id) {
    return DatabaseHelper.queryToDynaList(FIND_BILLS, new Object[]{claim_id});
  }
}

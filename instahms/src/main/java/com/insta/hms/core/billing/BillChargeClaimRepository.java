package com.insta.hms.core.billing;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

/**
 * The Class BillChargeClaimRepository.
 */
@Repository
public class BillChargeClaimRepository extends GenericRepository {

	/**
	 * Instantiates a new bill charge claim repository.
	 */
	public BillChargeClaimRepository() {
		super("bill_charge_claim");
	}
	
	/** The update claim recd total from remit. */
  //Updates the claim recd total from uploaded remittance  ignores claims with re-submission
	private final String UPDATE_CLAIM_RECD_TOTAL_FROM_REMIT = " UPDATE bill_charge_claim bcc "
	    + " SET claim_recd_total=irad.payment_amount "
	    + " FROM insurance_remittance_activity_details irad, "
	    + " insurance_claim ic, bill_charge bc "
	    + " WHERE irad.activity_id LIKE 'A-' || bcc.claim_activity_id || '%' "
	    + " AND irad.remittance_id= ? AND bcc.claim_activity_id NOT LIKE 'ACT-%' "
	    + " AND bcc.claim_id = irad.claim_id AND ic.claim_id = irad.claim_id "
	    + " AND ic.resubmission_type IS NULL AND bc.charge_id = bcc.charge_id "
	    + " AND (bc.submission_batch_type IS NULL OR bc.submission_batch_type = 'I') ";

	private final String UPDATE_PKG_CLAIM_RECD_TOTAL_FROM_REMIT = " UPDATE bill_charge_claim bcc "
	    + " SET claim_recd_total = amt FROM (SELECT "
	    + " ((bcc1.insurance_claim_amt*irad.payment_amount)/COALESCE"
	    + " (NullIF((sum(bcc1.insurance_claim_amt) "
	    + " OVER ( partition BY  bc1.package_id)),0 ),1)) AS amt, "
	    + " irad.claim_id, bc1.charge_id "
	    + " FROM insurance_remittance_activity_details irad "
	    + " JOIN bill_charge_claim bcc ON(irad.activity_id LIKE 'A-' || bcc.claim_activity_id || '%' "
	    + " AND irad.claim_id = bcc.claim_id AND bcc.claim_activity_id NOT LIKE 'ACT-%') "
	    + " JOIN insurance_claim ic ON(ic.claim_id = bcc.claim_id AND ic.resubmission_type IS NULL) "
	    + " JOIN bill_charge bc ON(bcc.charge_id = bc.charge_id AND bc.submission_batch_type = 'P')"
	    + " JOIN bill_charge bc1 ON(bc1.bill_no = bc.bill_no AND bc1.package_id = bc.package_id) "
	    + " JOIN bill_charge_claim bcc1 ON(bc1.charge_id = bcc1.charge_id)"
	    + " WHERE irad.remittance_id = ? AND bc1.charge_head != 'PKGPKG') AS foo  "
	    + " WHERE foo.charge_id = bcc.charge_id AND foo.claim_id = bcc.claim_id";
	
	/** The update claim recd total correction. */
  //Updates claim recd total when resubmission type is correction and 
	//denial code is not special denial code
	private final String UPDATE_CLAIM_RECD_TOTAL_CORRECTION = " UPDATE bill_charge_claim bcc "
	    + " SET claim_recd_total=irad.payment_amount "
	    + " FROM insurance_remittance_activity_details irad, insurance_claim ic, "
	    + " bill_charge bc, insurance_denial_codes idc  "
	    + " WHERE  irad.activity_id LIKE 'A-' || bcc.claim_activity_id || '%' "
	    + " AND irad.remittance_id= ? AND bcc.claim_activity_id NOT LIKE 'ACT-%' "
	    + " AND ic.claim_id = irad.claim_id AND ic.resubmission_type = 'correction' "
	    + " AND bcc.claim_id = irad.claim_id AND bc.charge_id = bcc.charge_id "
	    + " AND (bc.submission_batch_type IS NULL OR bc.submission_batch_type = 'I') "
	    + " AND ((idc.denial_code = irad.denial_code AND idc.special_denial_code_on_correction = 'N')"
	    + " OR irad.denial_code IS NULL )" ;

	private final String UPDATE_PKG_CLAIM_RECD_TOTAL_CORRECTION = " UPDATE bill_charge_claim bcc "
	    + " SET claim_recd_total = amt FROM (SELECT "
	    + " ((bcc1.insurance_claim_amt*irad.payment_amount)/COALESCE"
	    + " (NullIF((sum(bcc1.insurance_claim_amt) OVER ( partition BY  bc1.package_id)),0 ),1)) "
	    + " AS amt, irad.claim_id, bc1.charge_id"
	    + " FROM insurance_remittance_activity_details irad "
	    + " JOIN bill_charge_claim bcc ON(irad.activity_id LIKE 'A-' || bcc.claim_activity_id || '%' "
	    + " AND irad.claim_id = bcc.claim_id AND bcc.claim_activity_id NOT LIKE 'ACT-%')"
	    + " JOIN insurance_claim ic ON(ic.claim_id = bcc.claim_id "
	    + "  AND ic.resubmission_type = 'correction')"
	    + " JOIN bill_charge bc ON(bcc.charge_id = bc.charge_id AND bc.submission_batch_type = 'P')"
	    + " JOIN bill_charge bc1 ON(bc1.bill_no = bc.bill_no AND bc1.package_id = bc.package_id) "
	    + " JOIN bill_charge_claim bcc1 ON(bc1.charge_id = bcc1.charge_id)"
	    + " WHERE (irad.denial_code in(SELECT denial_code FROM  insurance_denial_codes idc "
	    + " where idc.special_denial_code_on_correction = 'N') "
	    + " OR irad.denial_code IS NULL) AND irad.remittance_id = ?  "
	    + " AND bc1.charge_head != 'PKGPKG') AS foo "
	    + " WHERE foo.charge_id = bcc.charge_id AND foo.claim_id = bcc.claim_id ";
	
	private final String UPDATE_CLAIM_RECD_TOTAL_CORRECTION_SPECIAL = "UPDATE bill_charge_claim bcc "
	    + " SET claim_recd_total = (bcc.claim_recd_total + irad.payment_amount) "
	    + " FROM insurance_remittance_activity_details irad, insurance_claim ic, "
	    + " bill_charge bc, insurance_denial_codes idc  "
	    + " WHERE  irad.activity_id LIKE 'A-' || bcc.claim_activity_id || '%' "
	    + " AND irad.remittance_id= ? AND bcc.claim_activity_id NOT LIKE 'ACT-%' "
	    + " AND ic.claim_id = irad.claim_id AND ic.resubmission_type = 'correction' "
	    + " AND bcc.claim_id = irad.claim_id AND bc.charge_id = bcc.charge_id "
	    + " AND (bc.submission_batch_type IS NULL OR bc.submission_batch_type = 'I') "
	    + " AND idc.denial_code = irad.denial_code "
	    + " AND idc.special_denial_code_on_correction = 'Y' ";

	private final String UPDATE_PKG_CLAIM_RECD_TOTAL_CORRECTION_SPECIAL = "UPDATE "
	    + " bill_charge_claim bcc set claim_recd_total = (bcc.claim_recd_total + foo.amt) "
	    + " FROM (SELECT ((bcc1.insurance_claim_amt*irad.payment_amount)/COALESCE("
	    + " NullIF((sum(bcc1.insurance_claim_amt) OVER ( partition BY  bc1.package_id)),0 ),1)) "
	    + " AS amt, irad.claim_id, bc1.charge_id "
	    + " FROM insurance_remittance_activity_details irad  "
	    + " JOIN bill_charge_claim bcc ON(irad.activity_id LIKE 'A-' || bcc.claim_activity_id || '%' "
	    + " AND irad.claim_id = bcc.claim_id AND bcc.claim_activity_id NOT LIKE 'ACT-%') "
	    + " JOIN insurance_claim ic ON(ic.claim_id = bcc.claim_id "
	    + "  AND ic.resubmission_type = 'correction')"
	    + " JOIN bill_charge bc ON(bcc.charge_id = bc.charge_id AND bc.submission_batch_type = 'P') "
	    + " JOIN bill_charge bc1 ON(bc1.bill_no = bc.bill_no AND bc1.package_id = bc.package_id) "
	    + " JOIN bill_charge_claim bcc1 ON(bc1.charge_id = bcc1.charge_id) "
	    + " WHERE irad.denial_code in(SELECT denial_code FROM  insurance_denial_codes idc WHERE idc.special_denial_code_on_correction = 'Y') "
      + "  AND irad.remittance_id = ?  AND bc1.charge_head != 'PKGPKG') AS foo "
      + " WHERE foo.charge_id = bcc.charge_id "
	    + "    AND foo.claim_id = bcc.claim_id ";

	/** The update combined claim recd total correction. */
	//Updates the claim recd total for combined items where resubmission type is correction and denial code is not special denial code
	private final String UPDATE_COMBINED_CLAIM_RECD_TOTAL_CORRECTION = "UPDATE bill_charge_claim fbcc set claim_recd_total = "
			+ "(insurance_amt * foo.payment_amount)/total_claim_amt FROM ("
			+ "SELECT sum(bcc.insurance_claim_amt  + bc.return_insurance_claim_amt) OVER (partition BY claim_activity_id) AS total_claim_amt, "
			+ "bcc.charge_id, (bcc.insurance_claim_amt +  bc.return_insurance_claim_amt) as insurance_amt, "
			+ "ic.claim_id, irad.payment_amount FROM bill_charge_claim bcc "
			+ "JOIN bill_charge bc ON(bcc.charge_id = bc.charge_id and bcc.bill_no = bc.bill_no) "
			+ "JOIN insurance_claim ic ON (ic.claim_id = bcc.claim_id AND "
			+ "ic.resubmission_type = 'correction') "
			+ "JOIN insurance_remittance_activity_details irad ON( irad.activity_id LIKE 'A-%' || bcc.claim_activity_id || '%' "
			+ "AND irad.claim_id = bcc.claim_id) "
			+ "WHERE irad.remittance_id= ? AND irad.activity_id LIKE 'A-ACT-%' AND "
			+ "ic.resubmission_type IS NOT NULL "
			+ "AND (irad.denial_code NOT IN ("
			+ "				SELECT idc.denial_code FROM insurance_denial_codes idc WHERE idc.special_denial_code_on_correction = 'Y') "
			+ "				OR irad.denial_code IS NULL"
			+ "				)) AS foo, "
			+ "insurance_remittance_activity_details WHERE "
			+ "(fbcc.charge_id = foo.charge_id  AND fbcc.insurance_claim_amt != 0 AND "
			+ "insurance_remittance_activity_details.activity_id LIKE 'A-' || fbcc.claim_activity_id || '%' "
			+ "AND fbcc.claim_id = foo.claim_id)";
	

	/** The update combined claim recd total correction special. */
	//Updates the claim recd total from uploaded remittance where re-submission type is correction and denial code is special denial code
	private final String UPDATE_COMBINED_CLAIM_RECD_TOTAL_CORRECTION_SPECIAL = "UPDATE bill_charge_claim fbcc set claim_recd_total = "
			+ "claim_recd_total + (insurance_amt * foo.payment_amount)/total_claim_amt FROM ("
			+ "SELECT sum(bcc.insurance_claim_amt  + bc.return_insurance_claim_amt) OVER (partition BY claim_activity_id) AS total_claim_amt, "
			+ "bcc.charge_id, (bcc.insurance_claim_amt +  bc.return_insurance_claim_amt) as insurance_amt, "
			+ "ic.claim_id, irad.payment_amount FROM bill_charge_claim bcc "
			+ "JOIN bill_charge bc ON(bcc.charge_id = bc.charge_id and bcc.bill_no = bc.bill_no) "
			+ "JOIN insurance_claim ic ON (ic.claim_id = bcc.claim_id AND "
			+ "ic.resubmission_type = 'correction') "
			+ "JOIN insurance_remittance_activity_details irad ON( irad.activity_id LIKE 'A-%' || bcc.claim_activity_id || '%' "
			+ "AND irad.claim_id = bcc.claim_id) "
			+ "JOIN insurance_denial_codes idc ON (irad.denial_code = idc.denial_code AND idc.special_denial_code_on_correction = 'Y') "
			+ "WHERE irad.remittance_id= ? AND irad.activity_id LIKE 'A-ACT-%' AND "
			+ "ic.resubmission_type IS NOT NULL ) AS foo, insurance_remittance_activity_details WHERE "
			+ "(fbcc.charge_id = foo.charge_id  AND fbcc.insurance_claim_amt != 0 AND "
			+ "insurance_remittance_activity_details.activity_id LIKE 'A-' || fbcc.claim_activity_id || '%' "
			+ "AND fbcc.claim_id = foo.claim_id)";
	
	/** The update combined claim recd total from remit. */
	//Updates combined hospital charges  ignores claims with re-submission
	private final String UPDATE_COMBINED_CLAIM_RECD_TOTAL_FROM_REMIT = "UPDATE bill_charge_claim fbcc set claim_recd_total = "
			+ "(insurance_amt * foo.payment_amount)/total_claim_amt FROM ("
			+ "SELECT sum(bcc.insurance_claim_amt  + bc.return_insurance_claim_amt) OVER (partition BY claim_activity_id) AS total_claim_amt,"
			+ "bcc.charge_id, (bcc.insurance_claim_amt +  bc.return_insurance_claim_amt) as insurance_amt,"
			+ "ic.claim_id, irad.payment_amount FROM bill_charge_claim bcc "
			+ "JOIN bill_charge bc ON(bcc.charge_id = bc.charge_id and bcc.bill_no = bc.bill_no)"
			+ "JOIN insurance_claim ic ON (ic.claim_id = bcc.claim_id AND ic.resubmission_type is null) "
			+ "JOIN insurance_remittance_activity_details irad ON( irad.activity_id LIKE 'A-%' || bcc.claim_activity_id || '%' "
			+ "AND irad.claim_id = bcc.claim_id) WHERE irad.remittance_id=? AND "
			+ "irad.activity_id LIKE 'A-ACT-%') AS foo, insurance_remittance_activity_details WHERE "
			+ "(fbcc.charge_id = foo.charge_id  AND fbcc.insurance_claim_amt != 0 AND "
			+ "insurance_remittance_activity_details.activity_id LIKE 'A-' || fbcc.claim_activity_id || '%' "
			+ "AND fbcc.claim_id = foo.claim_id)";
	
	/** The update claim recd total from remit internal. */
  //Updates claim recd from remittance when it is of re-submission type internal
	private final String UPDATE_CLAIM_RECD_TOTAL_FROM_REMIT_INTERNAL = " UPDATE "
	    + " bill_charge_claim bcc "
	    + " SET claim_recd_total = (irad.payment_amount + bcc.claim_recd_total) "
	    + " FROM insurance_remittance_activity_details irad, insurance_claim ic, "
	    + " bill_charge bc, insurance_denial_codes idc "
	    + " WHERE  irad.activity_id LIKE 'A-' || bcc.claim_activity_id || '%' "
	    + " AND irad.remittance_id=? AND bcc.claim_activity_id NOT LIKE 'ACT-%' "
	    + " AND bcc.claim_id = irad.claim_id AND bc.charge_id = bcc.charge_id "
	    + " AND (bc.submission_batch_type IS NULL OR bc.submission_batch_type = 'I') "
	    + " AND ic.claim_id = irad.claim_id AND (ic.resubmission_type = 'internal complaint' "
	    + " OR ic.resubmission_type = 'reconciliation') "
	    + " AND (idc.denial_code = irad.denial_code OR irad.denial_code IS NULL) ";

	private final String UPDATE_PKG_CLAIM_RECD_TOTAL_FROM_REMIT_INTERNAL = " UPDATE "
	    + " bill_charge_claim bcc set claim_recd_total = (bcc.claim_recd_total + foo.amt) "
	    + " FROM (SELECT ((bcc1.insurance_claim_amt*irad.payment_amount)/COALESCE("
	    + " NullIF((sum(bcc1.insurance_claim_amt) OVER ( partition BY  bc1.package_id)),0 ),1)) "
	    + " AS amt, irad.claim_id, bc1.charge_id "
	    + " FROM insurance_remittance_activity_details irad "
	    + " JOIN bill_charge_claim bcc ON(irad.activity_id LIKE 'A-' || "
	    + "  bcc.claim_activity_id || '%' AND irad.claim_id = bcc.claim_id "
	    + "  AND bcc.claim_activity_id NOT LIKE 'ACT-%')"
	    + " JOIN insurance_claim ic ON(ic.claim_id = bcc.claim_id "
	    + "  AND (ic.resubmission_type = 'internal complaint' "
	    + "  OR ic.resubmission_type = 'reconciliation')) "
	    + " JOIN bill_charge bc ON(bcc.charge_id = bc.charge_id AND bc.submission_batch_type = 'P') "
	    + " JOIN bill_charge bc1 ON(bc1.bill_no = bc.bill_no AND bc1.package_id = bc.package_id) "
	    + " JOIN bill_charge_claim bcc1 ON(bc1.charge_id = bcc1.charge_id AND bcc1.claim_id = ic.claim_id) "
	    + " WHERE (irad.denial_code in(SELECT denial_code FROM  insurance_denial_codes) "
      + " OR irad.denial_code IS NULL) AND irad.remittance_id = ? AND bc1.charge_head != 'PKGPKG') AS foo "
	    + " WHERE foo.charge_id = bcc.charge_id AND foo.claim_id = bcc.claim_id ";

	/** The update combined claim recd total from remit internal. */
	//updates claim recd for combined items when re-submission type is internal
	private final String UPDATE_COMBINED_CLAIM_RECD_TOTAL_FROM_REMIT_INTERNAL = "UPDATE bill_charge_claim fbcc set claim_recd_total = "
			+ "(claim_recd_total + (insurance_amt * foo.payment_amount)/total_claim_amt) FROM "
			+ "(SELECT sum(bcc.insurance_claim_amt  + bc.return_insurance_claim_amt) OVER (partition BY claim_activity_id) AS total_claim_amt,"
			+ "bcc.charge_id, (bcc.insurance_claim_amt +  bc.return_insurance_claim_amt) as insurance_amt,"
			+ "ic.claim_id, irad.payment_amount FROM bill_charge_claim bcc "
			+ "JOIN bill_charge bc ON(bcc.charge_id = bc.charge_id and bcc.bill_no = bc.bill_no)"
			+ "JOIN insurance_claim ic ON (ic.claim_id = bcc.claim_id AND "
			+ " (ic.resubmission_type = 'internal complaint' OR ic.resubmission_type = 'reconciliation')) "
			+ "JOIN insurance_remittance_activity_details irad ON( irad.activity_id LIKE 'A-%' || bcc.claim_activity_id || '%' "
			+ "AND irad.claim_id = bcc.claim_id) "
			+ "WHERE irad.remittance_id=? AND "
			+ "irad.activity_id LIKE 'A-ACT-%'"
			+ "AND (irad.denial_code NOT IN ("
			+ "				SELECT idc.denial_code FROM insurance_denial_codes idc WHERE idc.special_denial_code_on_correction = 'Y') "
			+ "				OR irad.denial_code IS NULL"
			+ "				)) AS foo, "
			+ "insurance_remittance_activity_details WHERE "
			+ "(fbcc.charge_id = foo.charge_id  AND fbcc.insurance_claim_amt != 0 AND "
			+ "insurance_remittance_activity_details.activity_id LIKE 'A-' || fbcc.claim_activity_id || '%' "
			+ "AND fbcc.claim_id = foo.claim_id)";
	
	//Updates claim recd from remittance when it marked as a recovery
	private final String UPDATE_CLAIM_RECD_TOTAL_FROM_REMIT_RECOVERY = "UPDATE bill_charge_claim SET claim_recd_total = (irad.payment_amount+claim_recd_total) FROM "
			+ "insurance_remittance_activity_details irad, insurance_denial_codes idc, "
			+ "insurance_remittance ir "
			+ "WHERE  irad.activity_id LIKE 'A-' || bill_charge_claim.claim_activity_id || '%' "
			+ "AND irad.remittance_id=? AND bill_charge_claim.claim_activity_id NOT LIKE 'ACT-%' "
			+ "AND bill_charge_claim.claim_id = irad.claim_id "
			+ "AND (idc.denial_code = irad.denial_code OR irad.denial_code IS NULL) "
			+ "AND ir.remittance_id = irad.remittance_id AND ir.is_recovery = 'Y' "
			+ "AND (bc.submission_batch_type IS NULL OR bc.submission_batch_type = 'I') ";
	
	private final String UPDATE_PKG_CLAIM_RECD_TOTAL_FROM_REMIT_RECOVERY = " UPDATE  "
	    + " bill_charge_claim bcc set claim_recd_total = (bcc.claim_recd_total + foo.amt) "
	    + " FROM (SELECT ((bcc1.insurance_claim_amt*irad.payment_amount)/COALESCE("
	    + " NullIF((sum(bcc1.insurance_claim_amt) OVER ( partition BY  bc1.package_id)),0 ),1)) "
	    + " AS amt, irad.claim_id, bc1.charge_id "
	    + " FROM insurance_remittance_activity_details irad "
	    + " JOIN insurance_remittance ir ON(irad.remittance_id = ir.remittance_id) "
	    + " JOIN bill_charge_claim bcc ON(irad.activity_id LIKE 'A-' || "
	    + " bcc.claim_activity_id || '%' AND irad.claim_id = bcc.claim_id "
	    + " AND bcc.claim_activity_id NOT LIKE 'ACT-%') "
	    + " JOIN insurance_claim ic ON(ic.claim_id = bcc.claim_id) "
	    + " JOIN bill_charge bc ON(bcc.charge_id = bc.charge_id AND bc.submission_batch_type = 'P') "
	    + " JOIN bill_charge bc1 ON(bc1.bill_no = bc.bill_no AND bc1.package_id = bc.package_id) "
	    + " JOIN bill_charge_claim bcc1 ON(bc1.charge_id = bcc1.charge_id AND bcc1.claim_id = ic.claim_id) "
	    + " WHERE (irad.denial_code in(SELECT denial_code FROM  insurance_denial_codes) "
	    + " OR irad.denial_code IS NULL) AND irad.remittance_id = ? AND ir.is_recovery = 'Y') AS foo "
	    + " WHERE foo.charge_id = bcc.charge_id AND foo.claim_id = bcc.claim_id ";
			
	//updates claim recd for combined items when it is marked as recovery
	private final String UPDATE_COMBINED_CLAIM_RECD_TOTAL_FROM_REMIT_RECOVERY = "UPDATE bill_charge_claim fbcc SET claim_recd_total = "
			+ "(claim_recd_total + (insurance_amt * foo.payment_amount)/total_claim_amt) FROM "
			+ "(SELECT sum(bcc.insurance_claim_amt  + bc.return_insurance_claim_amt) OVER (partition BY claim_activity_id) AS total_claim_amt,"
			+ "bcc.charge_id, (bcc.insurance_claim_amt +  bc.return_insurance_claim_amt) as insurance_amt,"
			+ "bcc.claim_id, irad.payment_amount FROM bill_charge_claim bcc "
			+ "JOIN bill_charge bc ON(bcc.charge_id = bc.charge_id and bcc.bill_no = bc.bill_no) "
			+ "JOIN insurance_remittance ir ON (ir.remittance_id = ? AND ir.is_recovery = 'Y') "
			+ "JOIN insurance_remittance_activity_details irad ON( irad.activity_id LIKE 'A-%' || bcc.claim_activity_id || '%' "
			+ "AND irad.claim_id = bcc.claim_id) "
			+ "WHERE irad.remittance_id=? AND "
			+ "irad.activity_id LIKE 'A-ACT-%'"
			+ "AND (irad.denial_code NOT IN ("
			+ "				SELECT idc.denial_code FROM insurance_denial_codes idc WHERE idc.special_denial_code_on_correction = 'Y') "
			+ "				OR irad.denial_code IS NULL"
			+ "				)) AS foo, "
			+ "insurance_remittance_activity_details WHERE "
			+ "(fbcc.charge_id = foo.charge_id  AND fbcc.insurance_claim_amt != 0 AND "
			+ "insurance_remittance_activity_details.activity_id LIKE 'A-' || fbcc.claim_activity_id || '%' "
			+ "AND fbcc.claim_id = foo.claim_id)";
	
	//Updates claim recd from remittance when generic preference for aggregate_amt_on_remittance is Y
	private final String UPDATE_CLAIM_RECD_TOTAL_FROM_REMIT_AGGREGATE = "UPDATE bill_charge_claim "
			+ "obcc SET claim_recd_total=  "
			+ "ff.amt from   "
			+ "(select   "
			+ "case when (bc.submission_batch_type is null OR bc.submission_batch_type = 'I')   "
			+ "then   "
			+ "(irad.payment_amount+COALESCE(bcc.claim_recd_total, 0))  "
			+ "else   "
			+ "(((bcc.insurance_claim_amt+bcc.return_insurance_claim_amt)*foo.payment_amount)/COALESCE(NullIF((sum(bcc.insurance_claim_amt+bcc.return_insurance_claim_amt)"
			+ " OVER ( partition BY  bc.charge_ref)),0 ),1)) end as amt,"
			+ " bcc.charge_id FROM bill_charge_claim bcc  "
			+ "left join   "
			+ "insurance_remittance_activity_details irad on (irad.activity_id LIKE 'A-' || bcc"
			+ ".claim_activity_id || '%' AND bcc.claim_id = irad.claim_id)   "
			+ "join bill_charge bc on (bc.charge_id=bcc.charge_id)  "
			+ "left join   "
			+ "(select irad.payment_amount, bill_charge_claim.charge_id FROM "
			+ "insurance_remittance_activity_details irad join bill_charge_claim  "
			+ " on (irad.activity_id LIKE 'A-' || bill_charge_claim.claim_activity_id || '%' )   "
			+ "  where irad.remittance_id=? AND bill_charge_claim.claim_activity_id NOT LIKE "
			+ "'ACT-%'  "
			+ "   and bill_charge_claim.charge_id in (select bc.charge_id from bill_charge_claim bcc join bill_charge bc on (bc.charge_id=bcc.charge_id)   "
			+ "   where bc.submission_batch_type = 'P' and bc.charge_head = 'PKGPKG' and irad.remittance_id = ?)) as foo  "
			+ " on (bc.charge_ref=foo.charge_id OR bc.charge_id=foo.charge_id)   "
			+ " WHERE bcc.claim_activity_id NOT LIKE 'ACT-%' and (irad.remittance_id=? or bc.charge_ref = foo.charge_id))"
			+ " as ff where obcc.charge_id=ff.charge_id ";

			
	//updates claim recd for combined items when generic preference for aggregate_amt_on_remittance is Y
	private final String UPDATE_COMBINED_CLAIM_RECD_TOTAL_FROM_REMIT_AGGREGATE = "UPDATE bill_charge_claim fbcc SET claim_recd_total = "
			+ "(COALESCE(claim_recd_total, 0) + (insurance_amt * foo.payment_amount)/total_claim_amt) FROM "
			+ "(SELECT sum(bcc.insurance_claim_amt  + bc.return_insurance_claim_amt) OVER (partition BY claim_activity_id) AS total_claim_amt,"
			+ "bcc.charge_id, (bcc.insurance_claim_amt +  bc.return_insurance_claim_amt) as insurance_amt,"
			+ "bcc.claim_id, irad.payment_amount FROM bill_charge_claim bcc "
			+ "JOIN bill_charge bc ON(bcc.charge_id = bc.charge_id and bcc.bill_no = bc.bill_no) "
			+ "JOIN insurance_remittance_activity_details irad ON( irad.activity_id LIKE 'A-%' || bcc.claim_activity_id || '%' "
			+ "AND irad.claim_id = bcc.claim_id) "
			+ "WHERE irad.remittance_id=? AND "
			+ "irad.activity_id LIKE 'A-ACT-%') AS foo, insurance_remittance_activity_details WHERE "
			+ "(fbcc.charge_id = foo.charge_id  AND fbcc.insurance_claim_amt != 0 AND "
			+ "insurance_remittance_activity_details.activity_id LIKE 'A-' || fbcc.claim_activity_id || '%' "
			+ "AND fbcc.claim_id = foo.claim_id)";
	
	//Status updates
	//only update where claim and recd amounts are greater than equal and no denial codes
	private static final String UPDATE_CLOSE_HOSPITAL_CLAIM_STATUS_NO_DENIAL = "UPDATE bill_charge_claim bcc "
			+ "SET claim_status = 'C' FROM insurance_remittance_activity_details irad "
			+ "WHERE bcc.claim_id = irad.claim_id AND "
			+ "	((irad.activity_id LIKE 'A-%' || bcc.claim_activity_id || '%' AND "
			+ "	(bcc.insurance_claim_amt <= bcc.claim_recd_total) "
			+ "	AND irad.denial_code IS NULL) OR bcc.insurance_claim_amt = 0) "
			+ " AND bcc.charge_head != 'PKGPKG' AND irad.remittance_id = ? ";
	
	// for items with special denial codes if charge status is ‘D’ then update the denial code
	private static final String UPDATE_DENIED_HOSPITAL_CLAIM_STATUS_SPECIAL = "UPDATE bill_charge_claim bcc "
			+ "SET denial_code = irad.denial_code FROM insurance_remittance_activity_details irad, "
			+ "insurance_denial_codes idc "
			+ "WHERE (bcc.claim_id = irad.claim_id AND "
			+ "irad.activity_id LIKE 'A-%' || bcc.claim_activity_id || '%' AND bcc.claim_status = 'D' "
			+ "AND idc.denial_code = irad.denial_code AND idc.special_denial_code_on_correction = 'Y' "
			+ "AND irad.denial_code IS NOT NULL AND irad.remittance_id = ?)";
	
	// for items with special denial codes if charge status is ‘O’ then update the denial code and change to ‘D’
	private static final String UPDATE_OPEN_HOSPITAL_CLAIM_STATUS_SPECIAL = "UPDATE bill_charge_claim bcc "
			+ "SET denial_code = irad.denial_code, claim_status = 'D' FROM insurance_remittance_activity_details irad, "
			+ "insurance_denial_codes idc "
			+ "WHERE (bcc.claim_id = irad.claim_id AND "
			+ "irad.activity_id LIKE 'A-%' || bcc.claim_activity_id || '%' AND bcc.claim_status = 'O' "
			+ "AND idc.denial_code = irad.denial_code AND idc.special_denial_code_on_correction = 'Y' "
			+ "AND irad.denial_code IS NOT NULL AND irad.remittance_id = ?)";
	
	// if normal denial codes update status to denied and update denial code
	private static final String UPDATE_DENIED_HOSPITAL_CLAIM_STATUS_DENIAL = "UPDATE bill_charge_claim bcc "
			+ "SET denial_code = irad.denial_code, claim_status = 'D' FROM insurance_remittance_activity_details irad, "
			+ "insurance_denial_codes idc "
			+ "WHERE (bcc.claim_id = irad.claim_id AND "
			+ "irad.activity_id LIKE 'A-%' || bcc.claim_activity_id || '%' "
			+ "AND idc.denial_code = irad.denial_code AND idc.special_denial_code_on_correction = 'N' "
			+ "AND irad.denial_code IS NOT NULL AND irad.remittance_id = ?)";

	// if partial remittance received without denial codes then mark as denied
	private final String UPDATE_DENIED_HOSPITAL_CLAIM_STATUS = "UPDATE bill_charge_claim bcc "
			+ "SET claim_status = 'D' FROM insurance_remittance_activity_details irad "
			+ "WHERE bcc.claim_id = irad.claim_id AND "
			+ "irad.activity_id LIKE 'A-%' || bcc.claim_activity_id || '%' "
			+ "AND (bcc.insurance_claim_amt > bcc.claim_recd_total) "
			+ "AND irad.denial_code IS NULL AND irad.remittance_id = ?";
	
	private static final String UPDATE_PACKAGE_CONTENTS_STATUS = "UPDATE bill_charge_claim bcc "
	    + " SET claim_status = claimstatus "
	    + " FROM (SELECT bcc1.charge_id, "
	    + " CASE WHEN bc1.package_id IS NOT NULL AND "
	    + " (sum(bcc1.claim_recd_total) OVER "
	    + " ( partition by bc1.package_id)) >= (sum(bcc1.insurance_claim_amt) "
	    + " OVER ( partition by bc1.package_id)) THEN 'C' ELSE 'D' END AS claimstatus "
	    + " FROM insurance_remittance_activity_details irad "
	    + " JOIN bill_charge_claim bcc ON(bcc.claim_id = irad.claim_id "
	    + "    AND irad.activity_id LIKE 'A-%' || bcc.claim_activity_id || '%') "
	    + " JOIN bill_charge bc ON(bc.charge_id = bcc.charge_id AND bc.submission_batch_type = 'P') "
	    + " JOIN bill_charge bc1 ON(bc1.bill_no = bc.bill_no AND bc1.package_id = bc.package_id) "
	    + " JOIN bill_charge_claim bcc1 ON(bcc1.charge_id = bc1.charge_id) "
	    + " WHERE irad.remittance_id = ?) as foo WHERE bcc.charge_id = foo.charge_id ";
	
	private static final String UPDATE_PACKAGE_MAIN_CHARGE_STATUS_TO_CLOSE = " UPDATE bill_charge_claim bcc "
	    + " SET claim_status = 'C' "
	    + " FROM insurance_remittance_activity_details irad, bill_charge bc "
	    + " WHERE bcc.claim_id = irad.claim_id AND irad.activity_id LIKE "
	    + " 'A-%' || bcc.claim_activity_id || '%' AND irad.remittance_id = ? "
	    + " AND bc.submission_batch_type IS NOT NULL AND bcc.charge_id NOT IN (SELECT bc.charge_ref "
	    + " FROM insurance_remittance_activity_details irad "
	    + " JOIN bill_charge_claim bcc ON(bcc.claim_id = irad.claim_id "
	    + "    AND irad.activity_id LIKE 'A-%' || bcc.claim_activity_id || '%') "
	    + " JOIN bill_charge bc ON(bcc.charge_id = bc.charge_ref AND bc.submission_batch_type IS NOT NULL) "
	    + " JOIN bill_charge_claim bcc1 ON(bcc1.charge_id = bc.charge_id) "
	    + " WHERE bcc1.claim_status = 'D' AND irad.remittance_id = ?) "
	    + " AND bcc.charge_head = 'PKGPKG' ";
	
	private static final String UPDATE_PACKAGE_MAIN_CHARGE_STATUS_TO_DENIED = " UPDATE bill_charge_claim bcc "
	    + " SET claim_status = 'D' "
	    + " FROM insurance_remittance_activity_details irad, bill_charge bc "
	    + " WHERE bcc.claim_id = irad.claim_id AND irad.activity_id LIKE "
	    + " 'A-%' || bcc.claim_activity_id || '%' AND irad.remittance_id = ? "
	    + " AND bc.submission_batch_type IS NOT NULL AND bcc.charge_id = bc.charge_id AND "
	    + " bc.package_id IN (SELECT bc1.package_id "
	    + " FROM insurance_remittance_activity_details irad "
	    + " JOIN bill_charge_claim bcc ON(bcc.claim_id = irad.claim_id "
	    + "  AND irad.activity_id LIKE 'A-%' || bcc.claim_activity_id || '%') "
	    + " JOIN bill_charge bc ON(bcc.charge_id = bc.charge_id AND bc.submission_batch_type IS NOT NULL) "
	    + " JOIN bill_charge bc1 ON(bc1.bill_no = bc.bill_no AND bc1.package_id = bc.package_id) "
	    + " JOIN bill_charge_claim bcc1 ON(bcc1.charge_id = bc.charge_id) "
	    + " WHERE bcc1.claim_status = 'D' AND irad.remittance_id = ?) "
	    + " AND bcc.charge_head = 'PKGPKG' ";
	
	/**
	 * Update charges of items
	 *
	 * @param remittanceId the remittance id
	 */
	public void updateCharges(Integer remittanceId) {
		DatabaseHelper.update(UPDATE_CLAIM_RECD_TOTAL_FROM_REMIT, new Object[]{remittanceId});
		DatabaseHelper.update(UPDATE_PKG_CLAIM_RECD_TOTAL_FROM_REMIT, new Object[]{remittanceId});
		
		DatabaseHelper.update(UPDATE_CLAIM_RECD_TOTAL_CORRECTION, new Object[]{remittanceId});
		DatabaseHelper.update(UPDATE_PKG_CLAIM_RECD_TOTAL_CORRECTION, new Object[]{remittanceId});
		
		DatabaseHelper.update(UPDATE_CLAIM_RECD_TOTAL_CORRECTION_SPECIAL, new Object[]{remittanceId});
		DatabaseHelper.update(UPDATE_PKG_CLAIM_RECD_TOTAL_CORRECTION_SPECIAL, new Object[]{remittanceId});
		
		DatabaseHelper.update(UPDATE_CLAIM_RECD_TOTAL_FROM_REMIT_INTERNAL, new Object[]{remittanceId});
		DatabaseHelper.update(UPDATE_PKG_CLAIM_RECD_TOTAL_FROM_REMIT_INTERNAL, new Object[]{remittanceId});
		
		DatabaseHelper.update(UPDATE_COMBINED_CLAIM_RECD_TOTAL_FROM_REMIT, new Object[]{remittanceId});
		DatabaseHelper.update(UPDATE_COMBINED_CLAIM_RECD_TOTAL_CORRECTION, new Object[]{remittanceId});
		DatabaseHelper.update(UPDATE_COMBINED_CLAIM_RECD_TOTAL_CORRECTION_SPECIAL, new Object[]{remittanceId});
		DatabaseHelper.update(UPDATE_COMBINED_CLAIM_RECD_TOTAL_FROM_REMIT_INTERNAL, new Object[]{remittanceId});
	}
	
	/**
	 * Update status of hospital items to closed or denied status (by considering special denial codes as well)
	 *
	 * @param remittanceId the remittance id of the file being processed
	 */
	public void updateStatus(Integer remittanceId) {

		//order of status update queries is important 
		DatabaseHelper.update(UPDATE_CLOSE_HOSPITAL_CLAIM_STATUS_NO_DENIAL, new Object[]{remittanceId});
		DatabaseHelper.update(UPDATE_DENIED_HOSPITAL_CLAIM_STATUS_SPECIAL, new Object[]{remittanceId});
		DatabaseHelper.update(UPDATE_OPEN_HOSPITAL_CLAIM_STATUS_SPECIAL, new Object[]{remittanceId});
		DatabaseHelper.update(UPDATE_DENIED_HOSPITAL_CLAIM_STATUS_DENIAL, new Object[]{remittanceId});
		DatabaseHelper.update(UPDATE_DENIED_HOSPITAL_CLAIM_STATUS, new Object[]{remittanceId});
		
		DatabaseHelper.update(UPDATE_PACKAGE_CONTENTS_STATUS, new Object[]{remittanceId});
		//DatabaseHelper.update(UPDATE_PACKAGE_MAIN_CHARGE_STATUS_TO_DENIED, new Object[]{remittanceId, remittanceId});
		//DatabaseHelper.update(UPDATE_PACKAGE_MAIN_CHARGE_STATUS_TO_CLOSE, new Object[]{remittanceId, remittanceId});
	}
	
	private static final String IS_BILL_CHARGE_CLAIM_EXISTS = "SELECT * FROM bill_charge_claim "
	    + " WHERE bill_no = ? AND charge_id=? AND claim_id=? ";

	public boolean isBillChargeClaimExists(Object[] obj) {
		BasicDynaBean bean = DatabaseHelper.queryToDynaBean(IS_BILL_CHARGE_CLAIM_EXISTS, obj);
		return bean != null && !bean.getMap().isEmpty();

	}

	private static final String GET_SPONSOR_ID_FROM_BILL_CLAIM = "SELECT sponsor_id FROM bill_claim WHERE visit_id = ? AND "
			+ " bill_no = ? AND plan_id = ? ";

	public String getSponsorIdFrombillClaim(Object[] obj) {
		return DatabaseHelper.getString(GET_SPONSOR_ID_FROM_BILL_CLAIM, obj);
	}

	
	/**
	 * Update charges if its a recovery remittance
	 *
	 * @param remittanceId the remittance id
	 */
	public void updateRecoveryCharges(Integer remittanceId) {
		
		DatabaseHelper.update(UPDATE_CLAIM_RECD_TOTAL_FROM_REMIT_RECOVERY, new Object[]{remittanceId});
		DatabaseHelper.update(UPDATE_PKG_CLAIM_RECD_TOTAL_FROM_REMIT_RECOVERY, new Object[]{remittanceId});
		DatabaseHelper.update(UPDATE_COMBINED_CLAIM_RECD_TOTAL_FROM_REMIT_RECOVERY, new Object[]{remittanceId, remittanceId});
	}

	/**
	 * Update remit charges based on the generic preference : aggregate_amt_on_remittance
	 *
	 * @param remittanceId the remittance id
	 */
	public void updateAggRemitCharges(Integer remittanceId) {
		DatabaseHelper.update(UPDATE_CLAIM_RECD_TOTAL_FROM_REMIT_AGGREGATE, new Object[]{remittanceId,remittanceId,remittanceId});
		DatabaseHelper.update(UPDATE_COMBINED_CLAIM_RECD_TOTAL_FROM_REMIT_AGGREGATE, new Object[]{remittanceId});
	}
	
	public static final String CANCEL_BILL_CHARGE_CLAIM = "UPDATE bill_charge_claim "+
			" SET insurance_claim_amt=0 WHERE charge_id=? ";
	
	public int cancelBillClaimCharge(String chargeId){
		return DatabaseHelper.update(CANCEL_BILL_CHARGE_CLAIM, chargeId);
	}

	private static final String UPDATE_BILL_CHARGE_CLAIM_AMT_TO_ZERO = "UPDATE bill_charge_claim bcc SET insurance_claim_amt = 0 , tax_amt = 0 WHERE bcc.bill_no = ? ";
	public Boolean updateBillChargeClaimsAmtToZero(String billNo) {
		// TODO Auto-generated method stub
		return DatabaseHelper.update(UPDATE_BILL_CHARGE_CLAIM_AMT_TO_ZERO, new Object[]{billNo}) >= 0;
	}

	public static final String INCLUDE_BILL_CHARGES_IN_CLAIM_CALC = "UPDATE bill_charge_claim bcc set include_in_claim_calc = true "+
			" FROM bill b WHERE b.bill_no = bcc.bill_no AND b.visit_id=?  ";
	
	public Boolean includeBillChargesInClaimCalc(String visitId,
			String billStatus) {
		// TODO Auto-generated method stub
		String query = billStatus.equals("open") ? INCLUDE_BILL_CHARGES_IN_CLAIM_CALC.concat(" AND b.status = 'A' ") : INCLUDE_BILL_CHARGES_IN_CLAIM_CALC;
		return DatabaseHelper.update(query, new Object[]{visitId}) >= 0;
	}

  public int[] cancelBillClaimCharge(List<String> chargeIdsList) {
    List<Object[]> values = new ArrayList<>();
    for (String chargeId : chargeIdsList) {
      values.add(new Object[] { chargeId });
    }
    return DatabaseHelper.batchUpdate(CANCEL_BILL_CHARGE_CLAIM, values);
  }
  
  /** The Constant CANCEL_DRG_ITEMS_CLAIM. */
  private static final String CANCEL_DRG_ITEMS_CLAIM = "UPDATE bill_charge_claim SET "
      + " insurance_claim_amt = 0.00, tax_amt = 0.00"
      + " WHERE charge_head in('BPDRG','ADJDRG','OUTDRG','APDRG','MARDRG') AND bill_no=? ";

  /**
   * Cancel DRG items claims.
   *
   * @param billNo String
   */
  public void cancelDRGItemsClaims(String billNo) {
    DatabaseHelper.update(CANCEL_DRG_ITEMS_CLAIM, billNo);
  }
  
  /** The Constant INCLUDE_ITEMS_IN_INSURANCE_CALC. */
  private static final String INCLUDE_ITEMS_IN_INSURANCE_CALC = "UPDATE bill_charge_claim SET "
      + " include_in_claim_calc = true WHERE bill_no = ? "
      + " AND charge_head NOT IN('PHCMED','PHMED','PHRET','PHCRET','INVRET')";

  /**
   * Include items in insurance calculation.
   *
   * @param billNo String
   */
  public void includeItemsInInsCalc(String billNo) {
    DatabaseHelper.update(INCLUDE_ITEMS_IN_INSURANCE_CALC, billNo);
  }
  
  /** The Constant UPDATE_DRG_BILL_HOSPITAL_ITEMS. */
  private static final String UPDATE_DRG_BILL_HOSPITAL_ITEMS = "UPDATE bill_charge_claim SET "
      + " insurance_claim_amt = 0, tax_amt = 0.00, include_in_claim_calc = false "
      + " WHERE charge_head not in('BPDRG','OUTDRG','APDRG') AND bill_no = ? ";

  /**
   * Sets the items sponsor amount.
   *
   * @param billNo String
   * @return the boolean
   */
  public Boolean setItemsSponsorAmount(String billNo) {
    return DatabaseHelper.update(UPDATE_DRG_BILL_HOSPITAL_ITEMS, billNo) > 0;
  }
  
  /** The Constant CANCEL_DRG_ITEMS_OUTLIER_AMOUNT_CLAIM. */
  private static final String CANCEL_DRG_ITEMS_OUTLIER_AMOUNT_CLAIM = "UPDATE bill_charge_claim " 
      + " SET insurance_claim_amt = 0.00, tax_amt = 0.00 "
      + " WHERE charge_head ='OUTDRG' AND bill_no=? ";

  /**
   * Cancel DRG outlier amount entry.
   *
   * @param billNo String
   * @return the boolean
   */
  public Boolean cancelDRGOutlierAmountEntry(String billNo) {
    return DatabaseHelper.update(CANCEL_DRG_ITEMS_OUTLIER_AMOUNT_CLAIM, billNo) > 0;
  }
  
  /** The Constant CANCEL_ADD_ON_PAYMENT_DRG_ITEMS_CLAIM. */
  private static final String CANCEL_ADD_ON_PAYMENT_DRG_ITEMS_CLAIM = "UPDATE bill_charge_claim SET"
      + " insurance_claim_amt = 0.00, tax_amt = 0.00 "
      + " WHERE charge_head ='APDRG' AND bill_no=? ";

  /**
   * Cancel add on payment DRG items.
   *
   * @param billNo String
   * @return the boolean
   */
  public Boolean cancelAddOnPaymentDRGItems(String billNo) {
    return DatabaseHelper.update(CANCEL_ADD_ON_PAYMENT_DRG_ITEMS_CLAIM, billNo) > 0;
  }


  private static final String GET_CLAIM_ID_FROM_BILL_CLAIM = "SELECT claim_id FROM bill_claim WHERE visit_id=? AND bill_no=? AND plan_id=? ";
  
  public String getClaimId(int planId, String billNo, String visitId){
    return DatabaseHelper.getString(GET_CLAIM_ID_FROM_BILL_CLAIM, visitId, billNo, planId);
  }
  
  //charge_id,sale_item_id, quantity, claim_amount, activity_vat, activity_vat_percent
  
  private static final String GET_COMBINED_ACTIVITIES = " SELECT "
      + " bcc.charge_id, 0 as sale_item_id, bc.act_quantity as quantity, "
      + " bcc.insurance_claim_amt,bcc.tax_amt, COALESCE(bct.tax_rate,0.00) as tax_rate "
      + " FROM bill_charge_claim bcc "
      + " JOIN bill_charge bc ON(bcc.charge_id = bc.charge_id) "
      + " LEFT JOIN bill_charge_tax bct ON(bct.charge_id = bc.charge_id) "
      + " WHERE bcc.claim_activity_id = ? AND bcc.claim_id = ? ";
  
  private static final String GET_ACTIVITIES_FOR_INTERNAL_COMPLAINT = " SELECT "
      + " bcc.charge_id, 0 as sale_item_id, bc.act_quantity as quantity, "
      + " (bcc.insurance_claim_amt - bcc.claim_recd_total) as insurance_claim_amt,"
      + " bcc.tax_amt, COALESCE(bct.tax_rate,0.00) as tax_rate "
      + " FROM bill_charge_claim bcc "
      + " JOIN bill_charge bc ON(bcc.charge_id = bc.charge_id) "
      + " LEFT JOIN bill_charge_tax bct ON(bct.charge_id = bc.charge_id) "
      + " WHERE bcc.claim_activity_id = ? AND bcc.claim_id = ? AND bcc.closure_type != 'D'";

  public List<BasicDynaBean> getCombinedActivities(String claimId, String claimActivityId, Boolean isInternalComplaint) {
    String query = isInternalComplaint ? GET_ACTIVITIES_FOR_INTERNAL_COMPLAINT : GET_COMBINED_ACTIVITIES;
    
    return DatabaseHelper.queryToDynaList(query, new Object[]{claimActivityId, claimId});
  } 

  private static final String UPDATE_CLAIM_AMT_AND_EXCLUSION_BASED_ON_PREAUTH =
      "UPDATE bill_charge_claim bccl "
          + "SET insurance_claim_amt = foo.claim_net_approved_amount, include_in_claim_calc = foo.include_in_claim_calc "
          + " FROM (" //
          + "   SELECT CASE WHEN pip.insurance_co IS NOT NULL AND ppa.status= 'A' AND pip.insurance_co = pp.preauth_payer_id"
          + "        THEN ppa.claim_net_approved_amount ELSE 0.00 END AS claim_net_approved_amount, "
          + "   bcc.claim_id,bc.charge_id," //
          + "   CASE WHEN ppa.status='D' AND ppa.claim_net_approved_amount =0 " //
          + "        THEN false " //
          + "        ELSE true " //
          + "   END AS include_in_claim_calc " //
          + " FROM bill_charge_claim bcc " //
          + "     JOIN bill_charge bc ON (bc.charge_id = bcc.charge_id AND bc.preauth_act_id IS NOT NULL) " //
          + "     JOIN bill b ON (b.bill_no = bcc.bill_no) " //
          + "     LEFT JOIN patient_insurance_plans pip ON (pip.patient_id = b.visit_id AND priority = 1) "
          + "     LEFT JOIN preauth_prescription_activities ppa ON (bc.preauth_act_id = ppa.preauth_act_id) "
          + "     LEFT JOIN preauth_prescription pp ON (pp.preauth_presc_id = ppa.preauth_presc_id) "
          + "   WHERE b.visit_id=? AND b.status='A' AND bc.status='A'" //
          + " ) as foo " //
          + " WHERE foo.claim_id = bccl.claim_id AND foo.charge_id=bccl.charge_id AND foo.claim_id = bccl.claim_id;";//

  public Integer setClaimAmountAndExclusionBasedOnPreAuth(String visitId) {
    return DatabaseHelper.update(UPDATE_CLAIM_AMT_AND_EXCLUSION_BASED_ON_PREAUTH, visitId);
  }

  private static final String UPDATE_CLAIM_AMT_BASED_ON_PREAUTH_APPROVAL_AMOUNT =
      "UPDATE bill_charge_claim bccl " //
        + "SET insurance_claim_amt = ?, "
        + "prior_auth_id = ?, "
        + "prior_auth_mode_id = ? " //
        + " FROM (" //
        + "   SELECT bcc.claim_id,bc.charge_id " //
        + "     FROM bill_charge_claim bcc "
        + "      JOIN bill_charge bc ON (bc.charge_id = bcc.charge_id) "
        + "      JOIN bill b ON (b.bill_no = bcc.bill_no) "
        + "      JOIN patient_insurance_plans pip ON (pip.patient_id = b.visit_id " //
        + "          AND priority = 1) " //
        + "      JOIN preauth_prescription_activities ppa ON (bc.preauth_act_id = ppa.preauth_act_id " //
        + "          AND ppa.added_to_bill = 'Y') "//
        + "      JOIN preauth_prescription pp ON (pip.insurance_co = pp.preauth_payer_id "//
        + "          AND pp.preauth_presc_id = ppa.preauth_presc_id) " //
        + "     WHERE " //
        + "      bc.preauth_act_id = ? " //
        + "      AND b.status = 'A' AND bc.status = 'A') AS foo " //
        + "  WHERE " //
        + "    foo.charge_id = bccl.charge_id " //
        + "    AND foo.claim_id = bccl.claim_id;";

  public Integer setPriorAuthApprovalAmountAsClaimAmount(int preauthActId, Object approvedAmount, String priorAuthId, Integer priorAuthModeId) {
    return DatabaseHelper.update(UPDATE_CLAIM_AMT_BASED_ON_PREAUTH_APPROVAL_AMOUNT, approvedAmount, priorAuthId, priorAuthModeId, (Object)preauthActId);
  }

}

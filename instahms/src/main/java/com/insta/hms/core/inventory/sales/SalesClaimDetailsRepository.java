package com.insta.hms.core.inventory.sales;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * The Class SalesClaimDetailsRepository.
 */
@Repository
public class SalesClaimDetailsRepository extends GenericRepository {

	/**
	 * Instantiates a new sales claim details repository.
	 */
	public SalesClaimDetailsRepository() {
		super("sales_claim_details");
	}

	// Updates the claim recd total from uploaded remittance ignores claims with
	// re-submission
	public final String UPDATE_PHAR_CLAIM_RECD_TOTAL_FROM_REMIT = "UPDATE sales_claim_details SET claim_recd = irad.payment_amount FROM "
			+ "insurance_remittance_activity_details irad, insurance_claim ic "
			+ "WHERE irad.activity_id LIKE 'P-' || sales_claim_details.claim_activity_id || '%' "
			+ "AND irad.remittance_id=? AND irad.activity_id NOT LIKE 'P-ACT-%'"
			+ "AND ic.claim_id = irad.claim_id AND ic.resubmission_type IS NULL AND sales_claim_details.claim_id = irad.claim_id";

	// Updates claim recd total when resubmission type is correction and denial
	// code is not special denial code
	public final String UPDATE_PHAR_CLAIM_RECD_CORRECTION = "UPDATE sales_claim_details SET claim_recd = irad.payment_amount FROM "
			+ "insurance_remittance_activity_details irad, insurance_claim ic, insurance_denial_codes idc	"
			+ "WHERE  irad.activity_id LIKE 'P-' || sales_claim_details.claim_activity_id || '%' "
			+ "AND irad.remittance_id=? AND irad.activity_id NOT LIKE 'P-ACT-%'"
			+ "AND ic.claim_id = irad.claim_id AND ic.resubmission_type ='correction' AND sales_claim_details.claim_id = irad.claim_id "
			+ "AND (irad.denial_code IS NULL "
			+ "		OR (idc.denial_code = irad.denial_code AND idc.special_denial_code_on_correction = 'N'))";

	// Updates claim recd total when resubmission type is correction and denial
	// code is a special denial code
	public final String UPDATE_PHAR_CLAIM_RECD_CORRECTION_SPECIAL = "UPDATE sales_claim_details SET claim_recd = "
			+ "(irad.payment_amount + claim_recd) "
			+ "FROM insurance_remittance_activity_details irad, insurance_claim ic, insurance_denial_codes idc	"
			+ "WHERE  irad.activity_id LIKE 'P-' || sales_claim_details.claim_activity_id || '%' "
			+ "AND irad.remittance_id=? AND irad.activity_id NOT LIKE 'P-ACT-%'"
			+ "AND ic.claim_id = irad.claim_id AND ic.resubmission_type ='correction' "
			+ "AND idc.denial_code = irad.denial_code "
			+ "AND sales_claim_details.claim_id = irad.claim_id "
			+ "AND idc.special_denial_code_on_correction = 'Y'";

	// Updates the claim recd for combined items where resubmission type is
	// correction and denial code is not special denial code
	public final String UPDATE_COMBINED_PHAR_CLAIM_RECD_CORRECTION = "UPDATE sales_claim_details fscd set claim_recd = "
			+ "(insurance_claim_amt * foo.payment_amount)/total_claim_amt FROM ("
			+ "SELECT claim_activity_id, sum(scd.insurance_claim_amt ) OVER (partition BY claim_activity_id) AS total_claim_amt,"
			+ "scd.claim_id, irad.payment_amount FROM sales_claim_details scd "
			+ "JOIN insurance_remittance_activity_details irad ON(irad.activity_id "
			+ "LIKE 'P-' || scd.claim_activity_id || '%' AND irad.claim_id = scd.claim_id) "
			+ "JOIN insurance_claim ic ON (ic.claim_id = scd.claim_id AND "
			+ "ic.resubmission_type = 'correction') "
			+ "WHERE irad.activity_id LIKE 'P-ACT-%' AND irad.remittance_id=?"
			+ "AND (irad.denial_code NOT IN ("
			+ "				SELECT idc.denial_code FROM insurance_denial_codes idc WHERE idc.special_denial_code_on_correction = 'Y') "
			+ "				OR irad.denial_code IS NULL"
			+ "							)"
			+ ") AS foo, "
			+ "insurance_remittance_activity_details WHERE (fscd.insurance_claim_amt != 0 "
			+ "AND insurance_remittance_activity_details.activity_id LIKE 'P-' || fscd.claim_activity_id || '%' AND "
			+ "fscd.claim_id = foo.claim_id AND fscd.claim_activity_id LIKE 'ACT-%' AND foo.claim_activity_id = fscd.claim_activity_id )";

	// Updates the claim recd for combined items where resubmission type is
	// correction and denial code is a special denial code
	public final String UPDATE_COMBINED_PHAR_CLAIM_RECD_CORRECTION_SPECIAL = "UPDATE sales_claim_details fscd set claim_recd = "
			+ "claim_recd + (insurance_claim_amt * foo.payment_amount)/total_claim_amt FROM ("
			+ "SELECT claim_activity_id, sum(scd.insurance_claim_amt ) OVER (partition BY claim_activity_id) AS total_claim_amt,"
			+ "scd.claim_id, irad.payment_amount FROM sales_claim_details scd "
			+ "JOIN insurance_remittance_activity_details irad "
			+ "ON(irad.activity_id LIKE 'P-' || scd.claim_activity_id || '%' AND irad.claim_id = scd.claim_id) "
			+ "JOIN insurance_claim ic ON (ic.claim_id = scd.claim_id AND "
			+ "ic.resubmission_type = 'correction') "
			+ "JOIN insurance_denial_codes idc ON (irad.denial_code = idc.denial_code AND idc.special_denial_code_on_correction = 'Y') "
			+ "WHERE irad.activity_id LIKE 'P-ACT-%' AND irad.remittance_id=?) AS foo, "
			+ "insurance_remittance_activity_details WHERE (fscd.insurance_claim_amt != 0 "
			+ "AND insurance_remittance_activity_details.activity_id LIKE 'P-' || fscd.claim_activity_id || '%' AND "
			+ "fscd.claim_id = foo.claim_id AND fscd.claim_activity_id LIKE 'ACT-%') AND foo.claim_activity_id = fscd.claim_activity_id";

	/** The update combined phar claim recd total from remit. */
	// Updates claim_received for combined items by splitting the received amt
	// from remittance over combined items, ignores if resubmission
	public final String UPDATE_COMBINED_PHAR_CLAIM_RECD_TOTAL_FROM_REMIT = "UPDATE sales_claim_details fscd set claim_recd = "
			+ "(fscd.insurance_claim_amt * foo.payment_amount)/foo.total_claim_amt FROM ("
			+ "SELECT claim_activity_id,sum(scd.insurance_claim_amt ) OVER (partition BY claim_activity_id) AS total_claim_amt,"
			+ "scd.claim_id, irad.payment_amount FROM sales_claim_details scd "
			+ "JOIN insurance_remittance_activity_details irad "
			+ "ON(irad.activity_id LIKE 'P-' || scd.claim_activity_id || '%' AND irad.claim_id = scd.claim_id) "
			+ "JOIN insurance_claim ic ON (ic.claim_id = scd.claim_id AND ic.resubmission_type is null) "
			+ "WHERE irad.activity_id LIKE 'P-ACT-%' AND irad.remittance_id=?) AS foo, "
			+ "insurance_remittance_activity_details WHERE (fscd.insurance_claim_amt != 0 "
			+ "AND insurance_remittance_activity_details.activity_id LIKE 'P-' || fscd.claim_activity_id || '%' AND "
			+ "foo.claim_activity_id = fscd.claim_activity_id AND "
			+ "fscd.claim_id = foo.claim_id AND fscd.claim_activity_id LIKE 'ACT-%')";

	// Updates claim_recd from remittance when resubmission is of type internal
	public final String UPDATE_PHAR_CLAIM_RECD_TOTAL_FROM_REMIT_INTERNAL = "UPDATE sales_claim_details SET claim_recd=(claim_recd + irad.payment_amount) FROM "
			+ "insurance_remittance_activity_details irad, insurance_denial_codes idc, insurance_claim ic "
			+ "WHERE irad.activity_id LIKE 'P-' || sales_claim_details.claim_activity_id || '%' "
			+ "AND irad.remittance_id=? AND irad.activity_id NOT LIKE 'P-ACT-%' "
			+ "AND sales_claim_details.claim_id = irad.claim_id "
			+ "AND ic.claim_id = irad.claim_id AND ( ic.resubmission_type = 'internal complaint' OR ic.resubmission_type = 'reconciliation')"
			+ "AND (idc.denial_code = irad.denial_code OR irad.denial_code IS NULL)";

	// Updates claim_recd for pharmacy from remittance when resubmission type is
	// internal
	public final String UPDATE_COMBINED_PHAR_CLAIM_RECD_TOTAL_FROM_REMIT_INTERNAL = "UPDATE sales_claim_details fscd set claim_recd = "
			+ "(claim_recd + (insurance_claim_amt * foo.payment_amount)/total_claim_amt) FROM ("
			+ "SELECT claim_activity_id,sum(scd.insurance_claim_amt ) OVER (partition BY scd.claim_activity_id) AS total_claim_amt,"
			+ "		scd.claim_id, irad.payment_amount FROM sales_claim_details scd "
			+ "JOIN insurance_remittance_activity_details irad "
			+ "		ON(irad.activity_id LIKE 'P-' || scd.claim_activity_id AND irad.claim_id = scd.claim_id) "
			+ "JOIN insurance_claim ic ON (ic.claim_id = scd.claim_id "
			+ "		AND	(ic.resubmission_type = 'internal complaint' OR ic.resubmission_type = 'reconciliation')) "
			+ "WHERE irad.activity_id LIKE 'P-ACT-%' AND irad.remittance_id=? AND irad.claim_id = ic.claim_id "
			+ "AND (irad.denial_code NOT IN ("
			+ "				SELECT idc.denial_code FROM insurance_denial_codes idc WHERE idc.special_denial_code_on_correction = 'Y') "
			+ "				OR irad.denial_code IS NULL"
			+ "				)) AS foo, "
			+ "insurance_remittance_activity_details WHERE (fscd.insurance_claim_amt != 0 "
			+ "AND foo.claim_activity_id = fscd.claim_activity_id AND fscd.claim_id = foo.claim_id AND fscd.claim_activity_id LIKE 'ACT-%')";

	// Updates claim_recd from remittance when its a recovery
	public final String UPDATE_PHAR_CLAIM_RECD_TOTAL_FROM_REMIT_RECOVERY = "UPDATE sales_claim_details SET claim_recd=(claim_recd + irad.payment_amount) FROM "
			+ "insurance_remittance_activity_details irad, insurance_denial_codes idc, insurance_remittance ir "
			+ "WHERE irad.activity_id LIKE 'P-' || sales_claim_details.claim_activity_id || '%' "
			+ "AND irad.remittance_id=? AND irad.activity_id NOT LIKE 'P-ACT-%'"
			+ "AND (idc.denial_code = irad.denial_code OR irad.denial_code IS NULL) "
			+ "AND sales_claim_details.claim_id = irad.claim_id "
			+ "AND ir.remittance_id = irad.remittance_id AND ir.is_recovery = 'Y'";

	// Updates claim_recd for pharmacy from remittance when its a recovery
	public final String UPDATE_COMBINED_PHAR_CLAIM_RECD_TOTAL_FROM_REMIT_RECOVERY = "UPDATE sales_claim_details fscd set claim_recd = "
			+ "(claim_recd + (insurance_claim_amt * foo.payment_amount)/total_claim_amt) FROM ("
			+ "SELECT claim_activity_id,sum(scd.insurance_claim_amt ) OVER (partition BY scd.claim_activity_id) AS total_claim_amt,"
			+ "scd.claim_id, irad.payment_amount FROM sales_claim_details scd "
			+ "JOIN insurance_remittance_activity_details irad "
			+ "ON(irad.activity_id LIKE 'P-' || scd.claim_activity_id || '%' AND irad.claim_id = scd.claim_id) "
			+ "JOIN insurance_remittance ir ON (ir.remittance_id = ? AND ir.is_recovery = 'Y') "
			+ "WHERE irad.activity_id LIKE 'P-ACT-%' AND irad.remittance_id=? "
			+ "AND (irad.denial_code NOT IN ("
			+ "				SELECT idc.denial_code FROM insurance_denial_codes idc WHERE idc.special_denial_code_on_correction = 'Y') "
			+ "				OR irad.denial_code IS NULL"
			+ "				)) AS foo, "
			+ "insurance_remittance_activity_details "
			+ "WHERE (fscd.insurance_claim_amt != 0 "
			+ "AND insurance_remittance_activity_details.activity_id LIKE 'P-' || fscd.claim_activity_id || '%' "
			+ "AND fscd.claim_id = foo.claim_id AND fscd.claim_activity_id LIKE 'ACT-%' AND foo.claim_activity_id = fscd.claim_activity_id)";
	
	//Copies the charges updated in bill_charge_table for consistency. Must be executed after all sales_claim_details item charges are updated.
	public final String UPDATE_BILL_CHARGES_FROM_PHAR = "UPDATE bill_charge_claim bcc "
			+ "SET claim_recd_total = claim_recd_total_amt FROM "
			+ "( "
			+ "	SELECT SUM(scd.claim_recd) as claim_recd_total_amt, ssm.charge_id, scd.claim_id"
			+ "	FROM sales_claim_details scd "
			+ "	JOIN insurance_remittance_details ird ON (ird.remittance_id = ? AND ird.claim_id = scd.claim_id) "
			+ "	JOIN store_sales_details ssd ON (scd.sale_item_id = ssd.sale_item_id) "
			+ "	JOIN store_sales_main ssm ON (ssm.sale_id = ssd.sale_id) "
			+ "	GROUP BY ssm.charge_id, scd.claim_id"
			+ ") as foo WHERE foo.charge_id = bcc.charge_id and  foo.claim_id = bcc.claim_id ";
	
	//Sets the status updated in bill_charge_table for consistency. Must be executed after all sales_claim_details item charges and statuses are updated.
	//Sets denied status
	public final String UPDATE_BILL_STATUS_DENIED_FROM_PHAR = "UPDATE bill_charge_claim bcc SET claim_status = 'D' FROM sales_claim_details scd, "
			+ "("
			+ "	SELECT ssm2.charge_id, scd2.claim_id "
			+ "			FROM   insurance_remittance_details ird2 "
			+ "			JOIN   sales_claim_details scd2 ON(ird2.claim_id = scd2.claim_id)"
			+ "			JOIN   store_sales_details ssd2 ON(ssd2.sale_item_id = scd2.sale_item_id)"
			+ "			JOIN   store_sales_main ssm2 ON(ssm2.sale_id = ssd2.sale_id)"
			+ "	WHERE ird2.remittance_id = ? AND scd2.claim_status = 'D'"
			+ ") as foo "
			+ "WHERE foo.claim_id = bcc.claim_id AND foo.charge_id = bcc.charge_id";
	
	//Sets the status updated in bill_charge_table for consistency. Must be executed after all sales_claim_details item charges and statuses are updated.
	//Sets closed status
	
	public final String UPDATE_BILL_STATUS_CLOSED_FROM_PHAR = "UPDATE bill_charge_claim bcc "
			+ "SET    claim_status = 'C' "
			+ "FROM   insurance_remittance_details ird "
			+ "JOIN   insurance_remittance_activity_details irad ON (irad.claim_id = ird.claim_id ) "
			+ "JOIN   sales_claim_details scd ON( ird.claim_id = scd.claim_id ) "
			+ "JOIN   store_sales_details ssd ON(ssd.sale_item_id = scd.sale_item_id) "
			+ "JOIN   store_sales_main ssm ON(ssm.sale_id = ssd.sale_id)"
			+ "WHERE  bcc.charge_id NOT IN ("
			+ "				SELECT ssm2.charge_id "
			+ "				FROM   insurance_remittance_details ird2 "
			+ "				JOIN   sales_claim_details scd2 ON(ird2.claim_id = scd2.claim_id)"
			+ "				JOIN   store_sales_details ssd2 ON(ssd2.sale_item_id = scd2.sale_item_id)"
			+ "				JOIN   store_sales_main ssm2 ON(ssm2.sale_id = ssd2.sale_id)"
			+ "				WHERE  ird2.claim_id = scd2.claim_id "
			+ "						AND ird2.remittance_id = ? "
			+ "						AND scd2.insurance_claim_amt != 0 "
			+ "						AND scd2.claim_status = 'D' "
			+ "				)"
			+ "	AND ird.claim_id = bcc.claim_id AND ssm.charge_id = bcc.charge_id"
			+ "	AND ird.remittance_id = ? AND irad.activity_id LIKE 'P-' || scd.claim_activity_id";


	//Updates claim recd from remittance when generic preference for aggregate_amt_on_remittance is Y
	public final String UPDATE_PHAR_CLAIM_RECD_TOTAL_FROM_REMIT_AGGREGATE = "UPDATE sales_claim_details SET claim_recd=(claim_recd + irad.payment_amount) FROM "
			+ "insurance_remittance_activity_details irad "
			+ "WHERE irad.activity_id LIKE 'P-' || sales_claim_details.claim_activity_id || '%' "
			+ "AND irad.remittance_id=? AND irad.activity_id NOT LIKE 'P-ACT-%'"
			+ "AND sales_claim_details.claim_id = irad.claim_id ";

	//updates claim recd for combined items when generic preference for aggregate_amt_on_remittance is Y
	public final String UPDATE_COMBINED_PHAR_CLAIM_RECD_TOTAL_FROM_REMIT_AGGREGATE = "UPDATE sales_claim_details fscd set claim_recd = "
			+ "	(claim_recd + (insurance_claim_amt * foo.payment_amount)/total_claim_amt) FROM "
			+ "	("
			+ "		SELECT claim_activity_id,sum(scd.insurance_claim_amt ) OVER (partition BY scd.claim_activity_id) AS total_claim_amt,"
			+ "				scd.claim_id, irad.payment_amount FROM sales_claim_details scd "
			+ "		JOIN insurance_remittance_activity_details irad "
			+ "				ON(irad.activity_id LIKE 'P-' || scd.claim_activity_id || '%' AND irad.claim_id = scd.claim_id) "
			+ "		WHERE irad.activity_id LIKE 'P-ACT-%' AND irad.remittance_id=? "
			+ "	) AS foo, "
			+ "	insurance_remittance_activity_details "
			+ "	WHERE (fscd.insurance_claim_amt != 0 "
			+ "	AND foo.claim_activity_id = fscd.claim_activity_id AND fscd.claim_id = foo.claim_id AND fscd.claim_activity_id LIKE 'ACT-%')";
	
	
	//Status updates
	//only update where claim and recd amounts are greater than equal and no denial codes
	private static final String UPDATE_CLOSE_PHAR_CLAIM_STATUS_NO_DENIAL = "UPDATE sales_claim_details scd "
            + "SET claim_status = 'C' FROM insurance_remittance_activity_details irad "
            + "WHERE scd.claim_id = irad.claim_id AND "
            + " ((irad.activity_id LIKE 'P-%' || scd.claim_activity_id || '%' AND "
            + " (scd.insurance_claim_amt <= scd.claim_recd) "
            + " AND irad.denial_code IS NULL) OR scd.insurance_claim_amt = 0) AND irad.remittance_id = ? ";
	
	// for items with special denial codes if charge status is ‘D’ then update the denial code
	private static final String UPDATE_DENIED_PHAR_CLAIM_STATUS_SPECIAL = "UPDATE sales_claim_details scd "
			+ "SET denial_code = irad.denial_code FROM insurance_remittance_activity_details irad, "
			+ "insurance_denial_codes idc "
			+ "WHERE (scd.claim_id = irad.claim_id AND "
			+ "irad.activity_id LIKE 'P-%' || scd.claim_activity_id || '%' AND scd.claim_status = 'D' "
			+ "AND idc.denial_code = irad.denial_code AND idc.special_denial_code_on_correction = 'Y' "
			+ "AND irad.denial_code IS NOT NULL AND irad.remittance_id = ?)";
	
	// for items with special denial codes if charge status is ‘O’ then update the denial code and change to ‘D’
	private static final String UPDATE_OPEN_PHAR_CLAIM_STATUS_SPECIAL = "UPDATE sales_claim_details scd "
			+ "SET denial_code = irad.denial_code, claim_status = 'D' FROM insurance_remittance_activity_details irad, "
			+ "insurance_denial_codes idc "
			+ "WHERE (scd.claim_id = irad.claim_id AND "
			+ "irad.activity_id LIKE 'P-%' || scd.claim_activity_id || '%' AND scd.claim_status = 'O' "
			+ "AND idc.denial_code = irad.denial_code AND idc.special_denial_code_on_correction = 'Y' "
			+ "AND irad.denial_code IS NOT NULL AND irad.remittance_id = ?)";
	
	// if normal denial codes update status to denied and update denial code
	private static final String UPDATE_DENIED_PHAR_CLAIM_STATUS_DENIAL = "UPDATE sales_claim_details scd "
			+ "SET denial_code = irad.denial_code, claim_status = 'D' FROM insurance_remittance_activity_details irad, "
			+ "insurance_denial_codes idc "
			+ "WHERE (scd.claim_id = irad.claim_id AND "
			+ "irad.activity_id LIKE 'P-%' || scd.claim_activity_id || '%' "
			+ "AND idc.denial_code = irad.denial_code AND idc.special_denial_code_on_correction = 'N' "
			+ "AND irad.denial_code IS NOT NULL AND irad.remittance_id = ?)";
	
	// if partial remittance received without denial codes then mark as denied
	private final String UPDATE_DENIED_PHAR_CLAIM_STATUS = "UPDATE sales_claim_details scd "
			+ "SET claim_status = 'D' FROM insurance_remittance_activity_details irad "
			+ "WHERE scd.claim_id = irad.claim_id AND "
			+ "irad.activity_id LIKE 'P-%' || scd.claim_activity_id || '%' AND "
			+ "(scd.insurance_claim_amt > scd.claim_recd) "
			+ "AND irad.denial_code IS NULL AND irad.remittance_id = ?";
	

	/**
	 * Update charges of pharmacy items
	 *
	 * @param remittanceId
	 *            the remittance id
	 */
	public void updateCharges(Integer remittanceId) {
		DatabaseHelper.update(UPDATE_PHAR_CLAIM_RECD_TOTAL_FROM_REMIT, new Object[]{remittanceId});
		DatabaseHelper.update(UPDATE_PHAR_CLAIM_RECD_CORRECTION, new Object[]{remittanceId});
		DatabaseHelper.update(UPDATE_PHAR_CLAIM_RECD_CORRECTION_SPECIAL, new Object[]{remittanceId});
		DatabaseHelper.update(UPDATE_COMBINED_PHAR_CLAIM_RECD_CORRECTION, new Object[]{remittanceId});
		DatabaseHelper.update(UPDATE_COMBINED_PHAR_CLAIM_RECD_CORRECTION_SPECIAL, new Object[]{remittanceId});
		DatabaseHelper.update(UPDATE_COMBINED_PHAR_CLAIM_RECD_TOTAL_FROM_REMIT, new Object[]{remittanceId});
		DatabaseHelper.update(UPDATE_PHAR_CLAIM_RECD_TOTAL_FROM_REMIT_INTERNAL, new Object[]{remittanceId});
		DatabaseHelper.update(UPDATE_COMBINED_PHAR_CLAIM_RECD_TOTAL_FROM_REMIT_INTERNAL, new Object[]{remittanceId});

		//copies charge updates from sales_claim_details to bill_charge_claim for consistency
		DatabaseHelper.update(UPDATE_BILL_CHARGES_FROM_PHAR, new Object[]{remittanceId});
	}

	/**
	 * Update status of pharmacy items to closed or denied (checks special denial codes as well)
	 *
	 * @param remittanceId
	 *            the remittance id
	 */
	public void updateStatus(Integer remittanceId) {
		//order of status updates is important and must be conserved
		DatabaseHelper.update(UPDATE_CLOSE_PHAR_CLAIM_STATUS_NO_DENIAL, new Object[]{remittanceId});
		DatabaseHelper.update(UPDATE_DENIED_PHAR_CLAIM_STATUS_SPECIAL, new Object[]{remittanceId});
		DatabaseHelper.update(UPDATE_OPEN_PHAR_CLAIM_STATUS_SPECIAL, new Object[]{remittanceId});
		DatabaseHelper.update(UPDATE_DENIED_PHAR_CLAIM_STATUS_DENIAL, new Object[]{remittanceId});
		DatabaseHelper.update(UPDATE_DENIED_PHAR_CLAIM_STATUS, new Object[]{remittanceId});

		//copies the updated statuses into bill_charge_claim for consistency and enabling claim closure using only bill_charge_claim status entries
		DatabaseHelper.update(UPDATE_BILL_STATUS_DENIED_FROM_PHAR, new Object[]{remittanceId});
		DatabaseHelper.update(UPDATE_BILL_STATUS_CLOSED_FROM_PHAR, new Object[]{remittanceId, remittanceId});
	}

	/**
	 * Update recovery charges.
	 *
	 * @param remittanceId the remittance id
	 */
	public void updateRecoveryCharges(Integer remittanceId) {

		DatabaseHelper.update(UPDATE_PHAR_CLAIM_RECD_TOTAL_FROM_REMIT_RECOVERY, new Object[]{remittanceId});
		DatabaseHelper.update(UPDATE_COMBINED_PHAR_CLAIM_RECD_TOTAL_FROM_REMIT_RECOVERY, new Object[]{remittanceId, remittanceId});

		//copies charge updates from sales_claim_details to bill_charge_claim for consistency
		DatabaseHelper.update(UPDATE_BILL_CHARGES_FROM_PHAR, new Object[]{remittanceId});
	}

	/**
	 * Update remit charges based on the generic preference : aggregate_amt_on_remittance
	 *
	 * @param remittanceId the remittance id
	 */
	public void updateAggRemitCharges(Integer remittanceId) {
		DatabaseHelper.update(UPDATE_PHAR_CLAIM_RECD_TOTAL_FROM_REMIT_AGGREGATE, new Object[]{remittanceId});
		DatabaseHelper.update(UPDATE_COMBINED_PHAR_CLAIM_RECD_TOTAL_FROM_REMIT_AGGREGATE, new Object[]{remittanceId});

		//copies charge updates from sales_claim_details to bill_charge_claim for consistency
		DatabaseHelper.update(UPDATE_BILL_CHARGES_FROM_PHAR, new Object[]{remittanceId});	
	}
	
	private static final String DELETE_SALES_CLAIM_DETAILS = " DELETE FROM sales_claim_details scd  "+ 
	    " WHERE scd.sale_item_id in(SELECT sale_item_id FROM store_sales_details ssd where ssd.sale_id = ?) ";

	public Boolean deletesalesClaimDetails(String saleId) {
		// TODO Auto-generated method stub
		return DatabaseHelper.delete(DELETE_SALES_CLAIM_DETAILS, new Object[]{saleId}) >= 0;
	}

	private static final String GET_INSCLAIMAMT_FROM_SALESCLAIMDETAILS = "SELECT SUM(scd.insurance_claim_amt) " +
			" FROM store_sales_main ssm "+
			" JOIN store_sales_details ssd ON(ssd.sale_id = ssm.sale_id) "+
			" JOIN sales_claim_details scd ON(scd.sale_item_id = ssd.sale_item_id) "+
			" WHERE ssm.charge_id = ?  AND scd.claim_id = ? ";
	
	public BigDecimal getInsuranceClaimAmtFromSalesClaimDetails(
			String chargeId, String claimId) {
		// TODO Auto-generated method stub
		return DatabaseHelper.getBigDecimal(GET_INSCLAIMAMT_FROM_SALESCLAIMDETAILS, new Object[]{chargeId, claimId});
	}
	
	private static final String GET_INSCLAIMTAXAMT_FROM_SALESCLAIMDETAILS = "SELECT SUM(scd.tax_amt) " +
			" FROM store_sales_main ssm "+
			" JOIN store_sales_details ssd ON(ssd.sale_id = ssm.sale_id) "+
			" JOIN sales_claim_details scd ON(scd.sale_item_id = ssd.sale_item_id) "+
			" WHERE ssm.charge_id = ?  AND scd.claim_id = ? ";

	public BigDecimal getInsuranceClaimTaxAmtFromSalesClaimDetails(
			String chargeId, String claimId) {
		// TODO Auto-generated method stub
		return DatabaseHelper.getBigDecimal(GET_INSCLAIMTAXAMT_FROM_SALESCLAIMDETAILS, new Object[]{chargeId, claimId});
	}

	private static final String GET_SALES_CLAIM_TAX_DETAILS = " SELECT ssm.charge_id,sctd.claim_id,sctd.item_subgroup_id AS tax_sub_group_id, " +
			" MAX(sctd.tax_rate) AS tax_rate, " +
			" CASE WHEN ssm.type='R' THEN -sum(sctd.tax_amt) ELSE sum(sctd.tax_amt) END AS sponsor_tax_amount, 0 AS charge_tax_id "+
			" FROM bill b  " +
			" JOIN store_sales_main ssm ON (ssm.bill_no = b.bill_no) " +
			" JOIN bill_charge bc ON (bc.charge_id = ssm.charge_id AND bc.bill_no = b.bill_no) " +
			" JOIN store_sales_details ssd ON (ssd.sale_id = ssm.sale_id)  " +
			" JOIN sales_claim_details scd ON (scd.sale_item_id = ssd.sale_item_id) " +
			" JOIN sales_claim_tax_details sctd ON (sctd.sale_item_id = scd.sale_item_id AND sctd.claim_id = scd.claim_id) " +
			" WHERE b.visit_id = ? AND b.status = 'A' " +
			" GROUP BY ssm.charge_id,ssm.sale_id,sctd.claim_id,sctd.item_subgroup_id,ssm.type ";
	
	public List<BasicDynaBean> getSalesClaimTaxDetails(String visitId) {
		// TODO Auto-generated method stub
		return DatabaseHelper.queryToDynaList(GET_SALES_CLAIM_TAX_DETAILS, new Object[]{visitId});
	}
	
  /** The Constant UPDATE_DRG_BILL_PHARMACY_ITEMS. */
  private static final String UPDATE_DRG_BILL_PHARMACY_ITEMS = "UPDATE sales_claim_details scl SET"
      + " insurance_claim_amt = 0, ref_insurance_claim_amount=0, tax_amt = 0.00, include_in_claim_calc = false "
      + " FROM store_sales_details ssd "
      + " JOIN store_sales_main ssm ON(ssd.sale_id = ssm.sale_id) "
      + " JOIN bill_charge bc on(bc.charge_id = ssm.charge_id) "
      + " WHERE scl.sale_item_id = ssd.sale_item_id AND bc.bill_no = ?";

  /**
   * Sets the items sponsor amount.
   *
   * @param billNo String
   * @return the boolean
   */
  public Boolean setItemsSponsorAmount(String billNo) {
    return DatabaseHelper.update(UPDATE_DRG_BILL_PHARMACY_ITEMS, billNo) > 0;
  }

	/**
	 * Get total tax amount for sale item
	 * */
	private String GET_TAX_AMT = "select sum(tax_amt) as tax_amt from sales_claim_details "
	    + " where sale_item_id = ?";
	
	/**
   * Get total tax amount for sale item
   * 
   * @param sale Item ID
   * return BasicDynaBean
   * 
   * */
  public BasicDynaBean getTaxAmt(Integer saleItemId) {
     return DatabaseHelper.queryToDynaBean(GET_TAX_AMT, new Object []{saleItemId});
  }
  
  /**
	 * Get total tax amount for sale item
	 * */
  private String GET_TAX_INSURANCE_AMT = "select sum(tax_amt) as tax_amt, "
  		+ " sum(insurance_claim_amt) as insurance_claim_amt "
  		+ " from sales_claim_details where sale_item_id = ?";
  
  /**
   * Get total tax amount for sale item
   * 
   * @param sale Item ID
   * return BasicDynaBean
   * 
   * */
  public BasicDynaBean getTaxAmtInsuranceAmt(Integer saleItemId) {
	     return DatabaseHelper.queryToDynaBean(GET_TAX_INSURANCE_AMT, new Object []{saleItemId});
  }
  
  private static final String GET_COMBINED_PHARMACY_ACTIVITIES = " SELECT "
      + " ssm.charge_id, scd.sale_item_id, "
      + " ssd.quantity, scd.insurance_claim_amt, "
      + " scd.tax_amt, COALESCE(sstd.tax_rate,0.00) as tax_rate "
      + " FROM sales_claim_details scd "
      + " JOIN store_sales_details ssd ON(scd.sale_item_id = ssd.sale_item_id) "
      + " JOIN store_sales_main ssm ON(ssm.sale_id = ssd.sale_id) "
      + " LEFT JOIN store_sales_tax_details sstd ON(sstd.sale_item_id = ssd.sale_item_id) "
      + " WHERE scd.claim_activity_id = ? AND scd.claim_id = ?";
  
  private static final String GET_ACTIVITIES_FOR_INTERNAL_COMPLAINT = " SELECT "
      + " ssm.charge_id, scd.sale_item_id, "
      + " ssd.quantity, (scd.insurance_claim_amt - claim_recd) as insurance_claim_amt, "
      + " scd.tax_amt, COALESCE(sstd.tax_rate,0.00) as tax_rate "
      + " FROM sales_claim_details scd "
      + " JOIN store_sales_details ssd ON(scd.sale_item_id = ssd.sale_item_id) "
      + " JOIN store_sales_main ssm ON(ssm.sale_id = ssd.sale_id) "
      + " LEFT JOIN store_sales_tax_details sstd ON(sstd.sale_item_id = ssd.sale_item_id) "
      + " WHERE scd.claim_activity_id = ? AND scd.claim_id = ?";

  public List<BasicDynaBean> getCombinedActivities(String claimId, String claimActivityId, Boolean isInternalComplaint) {
    String query = isInternalComplaint ? GET_ACTIVITIES_FOR_INTERNAL_COMPLAINT : GET_COMBINED_PHARMACY_ACTIVITIES;
    
    return DatabaseHelper.queryToDynaList(query, new Object[]{claimActivityId, claimId});

	}
  
  private static final String UPDATE_CLAIMID_IN_SALES_CLAIM_DETAILS = " UPDATE "
      + " sales_claim_details scd SET claim_id = ?, sponsor_id = ? "
      + " FROM  store_sales_details ssd "
      + " JOIN  store_sales_main ssm ON(ssm.sale_id = ssd.sale_id) "
      + " WHERE scd.sale_item_id = ssd.sale_item_id AND "
      + " ssm.bill_no = ? AND scd.claim_id = ? ";

  public Boolean updateSalesClaimOnEditIns(String billNo, String sponsorId,
      String oldClaimId, String newClaimId) {
    return DatabaseHelper.update(UPDATE_CLAIMID_IN_SALES_CLAIM_DETAILS, 
        new Object[]{newClaimId, sponsorId, billNo, oldClaimId}) >= 0;
  }
  
}
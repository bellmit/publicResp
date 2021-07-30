package com.insta.hms.core.billing;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

@Repository
public class BillReceiptRepository extends GenericRepository{

	public BillReceiptRepository() {
		super("bill_receipts");	
	}

	/*
	 * Receipt number generation methods: this is based on a set of preferences
	 * on how the type controls the prefix and sequence to use.
	 */
	private static final String RECEIPT_SEQ_PREFS = " SELECT pattern_id FROM hosp_receipt_seq_prefs "
			+ "  WHERE (bill_type = ? or bill_type ='*') AND "
			+ "  (visit_type = ? or visit_type = '*') AND "
			+ "  (restriction_type = ? or restriction_type = '*') AND "
			+ "  (payment_type = ?) AND "
			+ "  (center_id = ? OR center_id = 0) order by center_id desc, priority limit 1;" ;
	
	public String getNextReceiptNo(String billType, String visitType,
			String restrictionType, String paymentType, int centerId) {	
		return DatabaseHelper.getNextPatternId(DatabaseHelper.getString(RECEIPT_SEQ_PREFS,
				new Object[]{billType,visitType,restrictionType,paymentType,centerId}));

	}	
	
}

package com.insta.hms.core.billing;

import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

@Repository
public class BillChargeTransactionRepository extends GenericRepository {

	public BillChargeTransactionRepository() {
		super("bill_charge_transaction");
	}

}

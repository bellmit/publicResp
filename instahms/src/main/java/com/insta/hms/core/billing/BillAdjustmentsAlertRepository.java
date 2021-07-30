package com.insta.hms.core.billing;

import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

@Repository
public class BillAdjustmentsAlertRepository extends GenericRepository{

	public BillAdjustmentsAlertRepository() {
		super("bill_adjustment_alerts");
	}
	
}

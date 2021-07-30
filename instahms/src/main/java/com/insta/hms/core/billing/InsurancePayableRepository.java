package com.insta.hms.core.billing;

import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

@Repository
public class InsurancePayableRepository extends GenericRepository{

	public InsurancePayableRepository() {
		super("insurance_payable_bill_charges_view");
	}
	
}

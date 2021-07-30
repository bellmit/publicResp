package com.insta.hms.core.billing;

import org.springframework.stereotype.Repository;

import com.insta.hms.common.GenericRepository;

@Repository
public class PatientDepositsRepository extends GenericRepository{

	public PatientDepositsRepository() {
		super("receipt_usage_view");
	}
}

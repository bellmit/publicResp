package com.insta.hms.core.billing;

import com.insta.hms.common.annotations.LazyAutowired;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PatientDepositsService {
	
	@LazyAutowired
	PatientDepositsRepository patientDepositRepo;
	
	/**
	 * Find by key.
	 *
	 * @param receiptno the receipt no
	 * @return the basic dyna bean
	 */
	public BasicDynaBean findByKey(String depositNo) {
		return patientDepositRepo.findByKey("deposit_no",depositNo);
	}
	
	public List<BasicDynaBean> listAll(List<String> columns, String filterBy,
			Object filterValue) {
		return patientDepositRepo.listAll(columns, filterBy, filterValue);
	}

  public Boolean isDepositIdValid(String depositNo) {
    return patientDepositRepo.exist("deposit_no", depositNo);
  }

}

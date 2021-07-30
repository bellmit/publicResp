package com.insta.hms.mdm.transferhospitals;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

@Repository
public class TransferHospitalsRepository extends MasterRepository<String> {

  public TransferHospitalsRepository() {
    super("transfer_hospitals", "transfer_hospital_id", "transfer_hospital_name");
  }

}

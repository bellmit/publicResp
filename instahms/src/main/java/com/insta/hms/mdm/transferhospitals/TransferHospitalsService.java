package com.insta.hms.mdm.transferhospitals;

import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransferHospitalsService extends MasterService {

  public TransferHospitalsService(TransferHospitalsRepository transferHospitalsRepository,
      TransferHospitalsValidator transferHospitalsValidator) {
    super(transferHospitalsRepository, transferHospitalsValidator);
  }

  public List<BasicDynaBean> listAllActive() {
    return ((TransferHospitalsRepository) this.getRepository()).listAll(null, "status", "A",
        "transfer_hospital_name");
  }

}

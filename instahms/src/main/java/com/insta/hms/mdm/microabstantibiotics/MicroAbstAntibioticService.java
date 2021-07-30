package com.insta.hms.mdm.microabstantibiotics;

import com.insta.hms.mdm.MasterService;

import org.springframework.stereotype.Service;

/** @author anil. 
 * */
@Service
public class MicroAbstAntibioticService extends MasterService {

  public MicroAbstAntibioticService(
      MicroAbstAntibioticRepository microAbstAntibioticRepository, 
      MicroAbstAntibioticValidator microAbstAntibioticValidator) {
    super(microAbstAntibioticRepository, microAbstAntibioticValidator);
    // TODO Auto-generated constructor stub
  }
}

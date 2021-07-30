package com.insta.hms.mdm.contracttypes;

import com.insta.hms.mdm.MasterService;

import org.springframework.stereotype.Service;

@Service
public class ContractTypeService extends MasterService {

  public ContractTypeService(ContractTypeRepository contractTypeRepository,
      ContractTypeValidator contractTypeValidator) {
    super(contractTypeRepository, contractTypeValidator);
  }

}

package com.insta.hms.mdm.contracttypes;


import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public class ContractTypeRepository extends MasterRepository<BigDecimal> {

  public ContractTypeRepository() {
    super("contract_type_master", "contract_type_id", "contract_type");
  }

}

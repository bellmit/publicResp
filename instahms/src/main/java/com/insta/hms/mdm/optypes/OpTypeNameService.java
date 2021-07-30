package com.insta.hms.mdm.optypes;

import com.insta.hms.mdm.MasterService;

import org.springframework.stereotype.Service;

/** @author sonam. 
 * 
 **/
@Service
public class OpTypeNameService extends MasterService {

  public OpTypeNameService(OpTypeNameRepository opTypeNameRepository, 
      OpTypeNameValidation opTypeNameValidation) {
    super(opTypeNameRepository, opTypeNameValidation);
  }
}

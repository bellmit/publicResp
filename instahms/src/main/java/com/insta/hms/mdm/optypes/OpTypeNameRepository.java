package com.insta.hms.mdm.optypes;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

/** @author sonam. 
 **/
@Repository
public class OpTypeNameRepository extends MasterRepository<String> {

  public OpTypeNameRepository() {
    super("op_type_names", "op_type", "op_type_name");
  }
}

package com.insta.hms.mdm.microabstantibiotics;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

/** @author anil.
 * */
@Repository
public class MicroAbstAntibioticRepository extends MasterRepository<String> {

  //todo , composite key
  public MicroAbstAntibioticRepository() {
    super("micro_abst_antibiotic_master", "antibiotic_id");
    // TODO Auto-generated constructor stub
  }
}

package com.insta.hms.mdm.edcmachines;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

@Repository
public class EdcMachinesRepository extends MasterRepository<Integer> {

  public EdcMachinesRepository() {
    super("edc_machine_master", "edc_id");
  }
}

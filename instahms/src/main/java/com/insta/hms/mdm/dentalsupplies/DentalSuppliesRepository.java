package com.insta.hms.mdm.dentalsupplies;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

@Repository
public class DentalSuppliesRepository extends MasterRepository<Integer> {

  public DentalSuppliesRepository() {
    super("dental_supplies_master", "item_id", "item_name");
  }

}

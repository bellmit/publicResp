package com.insta.hms.mdm.billlabel;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

@Repository
public class BillLabelRepository extends MasterRepository<Integer> {

  public BillLabelRepository() {
    super("bill_label_master", "bill_label_id", "bill_label_name");
  }
}

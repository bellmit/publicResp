package com.insta.hms.mdm.rejectionreasons;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

@Repository
public class RejectionReasonRepository extends MasterRepository<Integer> {

  public RejectionReasonRepository() {
    super("rejection_reason_categories", "rejection_reason_category_id",
        "rejection_reason_category_name");
  }

}

package com.insta.hms.mdm.caserate;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

@Repository("caseRateDetailRepository")
public class CaseRateDetailRepository extends MasterRepository<Integer> {
  public CaseRateDetailRepository() {
    super("case_rate_detail", "case_rate_detail_id");
  }
}

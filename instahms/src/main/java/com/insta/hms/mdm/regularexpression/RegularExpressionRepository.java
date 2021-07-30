package com.insta.hms.mdm.regularexpression;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;


@Repository
public class RegularExpressionRepository extends MasterRepository<Integer> {
  public RegularExpressionRepository() {
    super("regexp_pattern_master", "pattern_id", "pattern_name",
        new String[] { "pattern_id", "pattern_name", "regexp_pattern" });
  }
}

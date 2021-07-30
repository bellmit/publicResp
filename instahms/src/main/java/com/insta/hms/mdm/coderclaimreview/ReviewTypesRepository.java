package com.insta.hms.mdm.coderclaimreview;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

@Repository
public class ReviewTypesRepository extends MasterRepository<Integer> {

  public ReviewTypesRepository() {
    super("review_types", "review_type_id", "review_type_id");
  }

  private static final String GENERATE_NEXT_SEQUENCE_QUERY = " SELECT nextval(?)";

  @Override
  public Object getNextId() {
    return DatabaseHelper.getInteger(GENERATE_NEXT_SEQUENCE_QUERY,
        "review_types_id_seq");
    // using sequence created by postgres for SERIAL
  }
}

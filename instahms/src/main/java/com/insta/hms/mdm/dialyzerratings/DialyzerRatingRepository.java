package com.insta.hms.mdm.dialyzerratings;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

@Repository
public class DialyzerRatingRepository extends MasterRepository<Integer> {

  public DialyzerRatingRepository() {
    super("dialyzer_ratings", "dialyzer_rating_id", "dialyzer_rating");
  }

}
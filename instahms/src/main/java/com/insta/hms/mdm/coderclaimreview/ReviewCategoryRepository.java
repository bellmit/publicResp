package com.insta.hms.mdm.coderclaimreview;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

@Repository
public class ReviewCategoryRepository extends MasterRepository<Integer> {

  public ReviewCategoryRepository() {
    super("review_categories", "category_id", "category_name",
        new String[] { "category_id", "category_name", "status", "category_type" });
  }

}

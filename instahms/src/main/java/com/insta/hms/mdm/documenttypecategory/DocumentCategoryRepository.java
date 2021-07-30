package com.insta.hms.mdm.documenttypecategory;

import com.insta.hms.mdm.MasterRepository;
import org.springframework.stereotype.Repository;

@Repository
public class DocumentCategoryRepository extends MasterRepository<String> {

  public DocumentCategoryRepository() {
    super("doc_category_master", "doc_category_id", "doc_category_name");
  }
}

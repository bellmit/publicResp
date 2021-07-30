package com.insta.hms.mdm.codesets;

import com.insta.hms.common.GenericRepository;
import org.springframework.stereotype.Repository;

@Repository
public class CodeSystemCategoriesRepository extends GenericRepository {
  
  public CodeSystemCategoriesRepository() {
    super("code_system_categories");
  }
}

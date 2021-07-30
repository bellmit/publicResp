package com.insta.hms.integration.regulatory.ohsrsdohgovph;

import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class OhsrsdohgovphSurgeryLookupDataRepository extends GenericRepository {

  public static final String COLUMN_OPERATIONCODE = "operationcode";
  public static final String COLUMN_DESCRIPTION = "description";

  public OhsrsdohgovphSurgeryLookupDataRepository() {
    super("ohsrsdohgovph_surgery_lookup_data");
  }

}

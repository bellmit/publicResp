package com.insta.hms.integration.regulatory.ohsrsdohgovph;

import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class OhsrsdohgovphIcdLookupDataRepository extends GenericRepository {

  public static final String COLUMN_ICD10CODE = "icd10code";
  public static final String COLUMN_ICD10DESC = "icd10desc";
  public static final String COLUMN_ICD10CAT = "icd10cat";

  public OhsrsdohgovphIcdLookupDataRepository() {
    super("ohsrsdohgovph_icd_lookup_data");
  }

}

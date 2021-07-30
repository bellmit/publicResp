package com.insta.hms.mdm.diagnostics;

import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class DiagnosticsTestInsuranceCategoryRepository extends GenericRepository {
  public DiagnosticsTestInsuranceCategoryRepository() {
    super("diagnostic_test_insurance_category_mapping");
  }

  @Override
  public List<BasicDynaBean> findByCriteria(Map<String, Object> filterMap) {
    return super.findByCriteria(filterMap);
  }
}

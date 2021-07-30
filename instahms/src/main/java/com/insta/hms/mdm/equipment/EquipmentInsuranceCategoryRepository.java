package com.insta.hms.mdm.equipment;

import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class EquipmentInsuranceCategoryRepository extends GenericRepository {

  public EquipmentInsuranceCategoryRepository() {
    super("equipment_insurance_category_mapping");
  }

  @Override
  public List<BasicDynaBean> findByCriteria(Map<String, Object> filterMap) {
    return super.findByCriteria(filterMap);
  }

}

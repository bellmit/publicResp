package com.insta.hms.mdm.section.fields;

import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

/**
 * The Class FieldsRepository.
 *
 * @author krishnat
 */
@Repository
public class FieldsRepository extends MasterRepository<Integer> {

  /**
   * Instantiates a new fields repository.
   */
  public FieldsRepository() {
    super("section_field_desc", "field_id");
  }

  /**
   * Gets the bean.
   *
   * @param fieldId the field id
   * @return the bean
   */
  public BasicDynaBean getBean(int fieldId) {
    Map<String, Object> filterMap = new HashMap<String, Object>();
    filterMap.put("field_id", fieldId);
    return findByKey(filterMap);
  }
}

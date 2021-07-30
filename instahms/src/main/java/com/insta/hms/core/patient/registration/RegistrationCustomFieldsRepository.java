package com.insta.hms.core.patient.registration;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The Class RegistrationCustomFieldsRepository.
 */
@Repository
public class RegistrationCustomFieldsRepository extends MasterRepository<Integer> {

  /**
   * Instantiates a new registration custom fields repository.
   */
  public RegistrationCustomFieldsRepository() {
    super("reg_custom_fields", "field_id", "name");
  }

  /** The Constant OP_REG_CUSTOM_FIELDS_AND_VALUES. */
  private static final String OP_REG_CUSTOM_FIELDS_AND_VALUES = 
      "select rcf.applicable_to,rcf.name,rcf.label,rcf.validation,"
      + "rcf.display_type,rcf.type,rcf.display_order, "
      + "string_agg(clv.custom_value,'^^') as values, "
      + "CASE WHEN rcf.show_group = 'M' THEN 'primary' ELSE 'secondary' END as show_group, "
      + "CASE WHEN rcf.mandatory IN ('A',?) THEN 'Y' ELSE 'N'  END as mandatory "
      + "from reg_custom_fields as rcf "
      + "LEFT JOIN custom_list_values_view as clv "
      + "on clv.field_name = rcf.name and clv.status = 'A' # "
      + "group by rcf.field_id "
      + "order by rcf.applicable_to, show_group, rcf.display_order ";
  
  private static final String ACTIVE_FILTER = "where rcf.label != ''  and rcf.status = 'A' ";

  /**
   * Gets the active reg custom fields and values.
   *
   * @return the active reg custom fields and values
   */
  public List<BasicDynaBean> getActiveRegCustomFieldsAndValues() {
    String query = OP_REG_CUSTOM_FIELDS_AND_VALUES;
    query = query.replace("#", ACTIVE_FILTER);
    return DatabaseHelper.queryToDynaList(query, 'O');
  }

  /**
   * Gets the reg custom fields and values.
   *
   * @return the reg custom fields and values
   */
  public List<BasicDynaBean> getRegCustomFieldsAndValues(String type) {
    String query = OP_REG_CUSTOM_FIELDS_AND_VALUES;
    query = query.replace("#", "");
    return DatabaseHelper.queryToDynaList(query,type);
  }

}

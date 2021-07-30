package com.insta.hms.mdm.section.fields;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The Class OptionsRepository.
 *
 * @author krishnat
 */
@Repository
public class OptionsRepository extends MasterRepository<Integer> {

  /**
   * Instantiates a new options repository.
   */
  public OptionsRepository() {
    super("section_field_options", "option_id");
  }

  /**
   * Gets the field options.
   *
   * @param fieldId the field id
   * @return the field options
   */
  public List<BasicDynaBean> getFieldOptions(int fieldId) {
    String query =
        " SELECT sfd.field_id, sfd.section_id, sfd.display_order as field_display_order, "
            + " sfd.field_name, field_type, sfd.allow_others, sfd.allow_normal, sfd.normal_text, "
            + " sfd.no_of_lines, sfd.status as field_status, sfd.observation_type, "
            + " sfd.observation_code, sfd.file_content, "
            + " sfd.markers, sfd.is_mandatory, sfd.use_in_presenting_complaint, "
            + " sfd.phrase_category_id as field_phrase_category_id, "
            + " sfd.pattern_id as field_pattern_id, sfd.default_to_current_datetime, "
            + "   case when (sfd.field_type = 'text' or sfd.field_type = 'wide text') "
            + "            then -2 when sfd.field_type='date' "
            + " then -3 when sfd.field_type = 'datetime' "
            + "            then -4 else sfo.option_id end as option_id, "
            + " sfo.display_order as option_display_order, sfo.option_value, "
            + " sfo.status as option_status, "
            + " sfo.value_code, sfo.phrase_category_id as option_phrase_category_id, "
            + " sfo.pattern_id as option_pattern_id "
            + " FROM section_field_desc sfd "
            + " LEFT JOIN section_field_options sfo using (field_id) where field_id=?";
    return DatabaseHelper.queryToDynaList(query, new Object[] {fieldId});
  }
}

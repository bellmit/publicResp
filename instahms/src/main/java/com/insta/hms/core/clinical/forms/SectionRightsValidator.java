package com.insta.hms.core.clinical.forms;

import com.insta.hms.common.DatabaseHelper;
import org.springframework.stereotype.Component;

/**
 * The Class SectionRightsValidator.
 *
 * @author teja
 */
@Component
public class SectionRightsValidator {

  /**
   * Validate.
   *
   * @param roleId the role id
   * @param sectionId the section id
   * @return true, if successful
   */
  public boolean validate(Integer roleId, Integer sectionId) {
    String query =
        "Select section_role_id From insta_section_rights Where section_id=" + sectionId.toString()
            + " And role_id=" + roleId.toString();
    return (DatabaseHelper.queryToDynaBean(query) != null);
  }
}

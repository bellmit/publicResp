package com.insta.hms.mdm.systemgeneratedsections;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.mdm.MasterRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The Class SystemGeneratedSectionsRepository.
 */
@Repository
public class SystemGeneratedSectionsRepository extends MasterRepository<Integer> {

  /**
   * Instantiates a new system generated sections repository.
   */
  public SystemGeneratedSectionsRepository() {
    super("system_generated_sections", "section_id", "section_name");
  }

  /** The system generated sections. */
  private static String sysgensections = "select section_id, section_name, "
      + "section_mandatory,op,ip,surgery,service, "
      + "triage,initial_assessment,generic_form, field_phrase_category_id, " 
       + " op_follow_up_consult_form from system_generated_sections";

  /**
   * Gets the sectionsdata.
   *
   * @return the sectionsdata
   */
  public List<BasicDynaBean> getSectionsdata() {
    return DatabaseHelper.queryToDynaList(sysgensections);
  }
}

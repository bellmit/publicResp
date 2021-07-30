package com.insta.hms.mdm.dischargesummarytemplates;

import com.insta.hms.common.DatabaseHelper;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * The Class DischargeSummaryTemplateRepository.
 *
 * @author anup vishwas
 */

@Repository
public class DischargeSummaryTemplateRepository {

  /** The Constant LIST_ALL_ACTIVE_TEMPLATE. */
  private static final String LIST_ALL_ACTIVE_TEMPLATE = " SELECT "
      + " id as format_id, title, format, caption"
      + " FROM all_patient_reports_view WHERE status='A'";

  /**
   * Gets the all active templates.
   *
   * @return the all active templates
   */
  public List<BasicDynaBean> getAllActiveTemplates() {

    return DatabaseHelper.queryToDynaList(LIST_ALL_ACTIVE_TEMPLATE);
  }

}

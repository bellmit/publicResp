package com.insta.hms.mdm.dischargesummarytemplates;

import com.insta.hms.common.annotations.LazyAutowired;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * The Class DischargeSummaryTemplateService.
 *
 * @author anup vishwas
 */

@Service
public class DischargeSummaryTemplateService {

  /** The discharge summary template repository. */
  @LazyAutowired
  private DischargeSummaryTemplateRepository dischargeSummaryTemplateRepository;
  
  /** The discharge format repo. */
  @LazyAutowired
  private DischargeFormatRepository dischargeFormatRepo;

  /**
   * Gets the all active templates.
   *
   * @return the all active templates
   */
  public List<BasicDynaBean> getAllActiveTemplates() {

    return dischargeSummaryTemplateRepository.getAllActiveTemplates();
  }

  /**
   * Gets the discharge format.
   *
   * @param formatId the format id
   * @return the discharge format
   */
  public BasicDynaBean getDischargeFormat(String formatId) {

    return dischargeFormatRepo.findByKey("format_id", formatId);
  }
}

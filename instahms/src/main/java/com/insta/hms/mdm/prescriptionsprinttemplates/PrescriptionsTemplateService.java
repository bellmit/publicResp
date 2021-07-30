package com.insta.hms.mdm.prescriptionsprinttemplates;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * Prescriptions Template Service.
 * 
 * @author yashwant
 */
@Service
public class PrescriptionsTemplateService extends MasterService {

  /** The pres template repository. */
  @LazyAutowired
  private PrescriptionsTemplateRepository presTemplateRepository;

  /**
   * Instantiates a new prescriptions template service.
   *
   * @param presTemplateRepository the pres template repository
   * @param presTemplateValidator the pres template validator
   */
  public PrescriptionsTemplateService(PrescriptionsTemplateRepository presTemplateRepository,
      PrescriptionsTemplateValidator presTemplateValidator) {
    super(presTemplateRepository, presTemplateValidator);
  }

  /**
   * Gets the template content.
   *
   * @param templateName the template name
   * @return the template content
   */
  public BasicDynaBean getTemplateContent(String templateName) {
    Map<String, Object> map = new HashMap<>();
    map.put("template_name", templateName);
    return presTemplateRepository.findByKey(map);
  }
}

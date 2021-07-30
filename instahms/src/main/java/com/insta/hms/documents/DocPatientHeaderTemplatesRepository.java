package com.insta.hms.documents;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.AppInit;
import com.insta.hms.common.GenericRepository;
import com.insta.hms.master.PatientHeaderTemplate.PatientHeaderTemplate;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.io.FileInputStream;
import java.io.IOException;

// TODO: Auto-generated Javadoc
/**
 * The Class DocPatientHeaderTemplatesRepository.
 */
@Repository
public class DocPatientHeaderTemplatesRepository extends GenericRepository {

  /**
   * Instantiates a new doc patient header templates repository.
   */
  public DocPatientHeaderTemplatesRepository() {
    super("doc_patient_header_templates");
  }

  /**
   * Gets the patient header.
   *
   * @param templateId the template id
   * @param type the type
   * @return the patient header
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public String getPatientHeader(Integer templateId, String type) throws IOException {
    String templateContent = "";
    if (templateId == null) {
      return templateContent;
    }
    if (templateId == 0) {
      PatientHeaderTemplate phTemplate = null;
      for (PatientHeaderTemplate template : PatientHeaderTemplate.values()) {
        if (template.getType().equalsIgnoreCase(type)) {
          phTemplate = template;
          break;
        }
      }
      String ftlPath = AppInit.getServletContext().getRealPath("/WEB-INF/templates/PatientHeaders");
      if (phTemplate != null) {
        FileInputStream stream = new FileInputStream(
            ftlPath + "/" + phTemplate.getFtlName() + ".ftl");
        templateContent = new String(DataBaseUtil.readInputStream(stream));
      }
    } else {
      BasicDynaBean bean = findByKey("template_id", templateId);
      templateContent = (String) bean.get("template_content");
    }

    return templateContent;
  }

}

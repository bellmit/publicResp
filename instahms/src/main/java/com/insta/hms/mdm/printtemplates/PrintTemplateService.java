package com.insta.hms.mdm.printtemplates;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.mdm.MasterService;

import org.springframework.stereotype.Service;

// TODO: Auto-generated Javadoc
/**
 * Print Template Service.
 * @author anup vishwas
 */
@Service
public class PrintTemplateService extends MasterService {

  /** The print template repository. */
  @LazyAutowired private PrintTemplateRepository printTemplateRepository;

  /**
   * Instantiates a new prints the template service.
   *
   * @param printTemplateRepository the print template repository
   * @param printTemplateValidator the print template validator
   */
  public PrintTemplateService(
      PrintTemplateRepository printTemplateRepository,
      PrintTemplateValidator printTemplateValidator) {
    super(printTemplateRepository, printTemplateValidator);
  }

  /**
   * get customized template.
   *
   * @param template print template
   * @return String
   */
  public String getCustomizedTemplate(PrintTemplate template) {
    return (String)
        printTemplateRepository
            .findByKey("template_type", template.getType())
            .get("print_template_content");
  }

  /**
   * Gets the patient header template id.
   *
   * @param template the template
   * @return the patient header template id
   */
  public Integer getPatientHeaderTemplateId(PrintTemplate template) {
    Integer pheaderTemplateId = null;

    if (template.type.equals(PrintTemplate.Lab.type)
        || template.type.equals(PrintTemplate.Rad.type)
        || template.type.equals(PrintTemplate.Ser.type)
        || template.type.equals(PrintTemplate.Patient_Ward_Activities.type)
        || template.type.equals(PrintTemplate.TreatmentSheet.type)
        || template.type.equals(PrintTemplate.ClinicalInfo.type)
        || template.type.equals(PrintTemplate.Triage.type)
        || template.type.equals(PrintTemplate.Initial_Assessment.type)
        || template.type.equals(PrintTemplate.WebLab.type)
        || template.type.equals(PrintTemplate.WebRad.type)
        || template.type.equals(PrintTemplate.Medication_Chart.type)
        || template.type.equals(PrintTemplate.APILAB.type)
        || template.type.equals(PrintTemplate.APIRAD.type)
        || template.type.equals(PrintTemplate.Vital_Measurements.type)
        || template.type.equals(PrintTemplate.Discharge_Medication.type)) {
      pheaderTemplateId =
        (Integer) printTemplateRepository.findByKey("template_type",
            template.getType()).get("pheader_template_id");
    }
    return pheaderTemplateId;
  }
}

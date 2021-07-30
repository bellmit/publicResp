package com.insta.hms.mdm.prescriptionslabelprinttemplates;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

/**
 * Prescriptions Label Print Template Repository.
 */
@Repository
public class PrescriptionsLabelPrintTemplateRepository extends MasterRepository<String> {

  public PrescriptionsLabelPrintTemplateRepository() {
    super("prescription_label_print_template", "template_name");
  }
}

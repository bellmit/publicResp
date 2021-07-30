package com.insta.hms.mdm.prescriptionsprinttemplates;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

/**
 * Prescriptions Template Repository.
 * @author yashwant
 */
@Repository
public class PrescriptionsTemplateRepository extends MasterRepository<String> {

  public PrescriptionsTemplateRepository() {
    super("prescription_print_template", "template_name");
  }
}

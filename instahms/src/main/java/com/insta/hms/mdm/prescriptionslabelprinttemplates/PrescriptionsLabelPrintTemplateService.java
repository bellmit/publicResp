package com.insta.hms.mdm.prescriptionslabelprinttemplates;

import com.insta.hms.mdm.MasterService;

import org.springframework.stereotype.Service;

/**
 * Prescription Label Print template.
 * @author yashwant
 */
@Service
public class PrescriptionsLabelPrintTemplateService extends MasterService {

  public PrescriptionsLabelPrintTemplateService(
      PrescriptionsLabelPrintTemplateRepository presLabelPrintTemplateRepository,
      PrescriptionsLabelPrintTemplateValidator presLabelPrintTemplateValidator) {
    super(presLabelPrintTemplateRepository, presLabelPrintTemplateValidator);
  }
}

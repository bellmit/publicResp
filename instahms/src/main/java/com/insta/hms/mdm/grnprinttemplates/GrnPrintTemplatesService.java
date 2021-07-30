package com.insta.hms.mdm.grnprinttemplates;

import com.insta.hms.mdm.MasterService;

import org.springframework.stereotype.Service;

/** The Class GrnPrintTemplatesService. */
@Service
public class GrnPrintTemplatesService extends MasterService {

  /**
   * Instantiates a new grn print templates service.
   *
   * @param repo the repo
   * @param validator the validator
   */
  public GrnPrintTemplatesService(
      GrnPrintTemplatesRepository repo, GrnPrintTemplatesValidator validator) {
    super(repo, validator);
  }

  /**
   * Delete Grn template.
   *
   * @param grnTemplateId the grn template id
   * @return true, if successful
   */
  public boolean deleteGrnTemplate(int grnTemplateId) {
    return getRepository().delete("template_id", grnTemplateId) > 0;
  }
}

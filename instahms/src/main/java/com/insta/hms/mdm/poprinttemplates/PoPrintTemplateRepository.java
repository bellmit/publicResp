package com.insta.hms.mdm.poprinttemplates;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

@Repository
public class PoPrintTemplateRepository extends MasterRepository<String> {

  public PoPrintTemplateRepository() {
    super("po_print_template", "template_name");
  }
}

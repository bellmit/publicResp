package com.insta.hms.mdm.printtemplates;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

/**
 * Printer Template Repository.
 * @author anup vishwas
*/
@Repository
public class PrintTemplateRepository extends MasterRepository<Integer> {

  public PrintTemplateRepository() {
    super("print_templates", "template_type");
  }
}

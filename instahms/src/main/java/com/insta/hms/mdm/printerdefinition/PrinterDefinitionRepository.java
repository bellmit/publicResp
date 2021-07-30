package com.insta.hms.mdm.printerdefinition;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

/**
 * Print definition Repository.
 */
@Repository
public class PrinterDefinitionRepository extends MasterRepository<Integer> {
  /** Print definition Repository Constructor. */
  public PrinterDefinitionRepository() {
    super(
        "printer_definition",
        "printer_id",
        "printer_definition_name",
        new String[] {"printer_id", "printer_definition_name", "print_mode"});
  }
}

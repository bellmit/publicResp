package com.insta.hms.mdm.printersettings;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

/**
 * Printer Settings Repository.
 */
@Repository
public class PrinterSettingRepository extends MasterRepository<String> {

  public PrinterSettingRepository() {
    super("printer_definition", "printer_definition_name", "printer_id");
  }
}

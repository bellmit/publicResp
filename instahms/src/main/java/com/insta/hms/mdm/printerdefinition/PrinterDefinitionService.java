package com.insta.hms.mdm.printerdefinition;

import com.insta.hms.mdm.MasterService;

import org.springframework.stereotype.Service;

/**
 * Printer Definition Service.
 */
@Service
public class PrinterDefinitionService extends MasterService {

  public PrinterDefinitionService(
      PrinterDefinitionRepository printerDefinitionRepository,
      PrinterDefinitionValidator printerDefinitionValidator) {
    super(printerDefinitionRepository, printerDefinitionValidator);
  }
}

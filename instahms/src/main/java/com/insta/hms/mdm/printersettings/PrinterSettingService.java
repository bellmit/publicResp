package com.insta.hms.mdm.printersettings;

import com.insta.hms.mdm.MasterService;

import org.springframework.stereotype.Service;

/**
 * Printer Setting Service.
 */
@Service
public class PrinterSettingService extends MasterService {

  public PrinterSettingService(
      PrinterSettingRepository printerSettingRepository,
      PrinterSettingValidator printerSettingValidator) {
    super(printerSettingRepository, printerSettingValidator);
  }
}

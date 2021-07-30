package com.insta.hms.mdm.edcmachines;

import com.insta.hms.mdm.MasterService;

import org.springframework.stereotype.Service;

@Service
public class EdcMachinesService extends MasterService {

  public EdcMachinesService(
      EdcMachinesRepository edcMachinesRepository, EdcMachinesValidator edcMachinesValidator) {
    super(edcMachinesRepository, edcMachinesValidator);
  }
}

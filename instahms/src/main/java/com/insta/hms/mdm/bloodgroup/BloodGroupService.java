package com.insta.hms.mdm.bloodgroup;

import com.insta.hms.mdm.MasterService;

import org.springframework.stereotype.Service;

@Service
public class BloodGroupService extends MasterService {

  public BloodGroupService(BloodGroupRepository repository, BloodGroupValidator validatior) {
    super(repository, validatior);
  }

}

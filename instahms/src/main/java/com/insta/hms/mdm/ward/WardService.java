package com.insta.hms.mdm.ward;

import com.insta.hms.mdm.MasterService;

import org.springframework.stereotype.Service;

@Service
public class WardService extends MasterService {

  public WardService(WardRepository repo, WardValidator validator) {
    super(repo, validator);
  }
}
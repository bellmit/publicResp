package com.insta.hms.mdm.bank;

import com.insta.hms.mdm.MasterService;

import org.springframework.stereotype.Service;

@Service
public class BankService extends MasterService {
  
  public BankService(BankRepository repository, BankValidator validator) {
    super(repository, validator);
  }
}

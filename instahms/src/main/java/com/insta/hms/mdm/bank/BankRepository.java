package com.insta.hms.mdm.bank;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

@Repository
public class BankRepository extends MasterRepository<Integer> {
  
  public BankRepository() {
    super("bank_master", "bank_id", "bank_name");
  }

}

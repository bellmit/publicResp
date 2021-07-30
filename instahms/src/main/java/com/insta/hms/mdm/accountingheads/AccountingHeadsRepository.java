package com.insta.hms.mdm.accountingheads;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

@Repository
public class AccountingHeadsRepository extends MasterRepository<Integer> {

  public AccountingHeadsRepository() {
    super("bill_account_heads", "account_head_id", "account_head_name");
  }
}

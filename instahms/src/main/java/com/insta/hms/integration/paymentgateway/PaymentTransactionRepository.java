package com.insta.hms.integration.paymentgateway;

import com.insta.hms.common.GenericRepository;

import org.springframework.stereotype.Repository;

@Repository
public class PaymentTransactionRepository extends GenericRepository {

  public PaymentTransactionRepository() {
    super("payment_transactions");
  }

}

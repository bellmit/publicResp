package com.insta.hms.mdm.paymentcategories;

import com.insta.hms.mdm.MasterService;

import org.springframework.stereotype.Service;

@Service
public class PaymentCategoryService extends MasterService {

  public PaymentCategoryService(PaymentCategoryRepository paymentCategoryRepository,
      PaymentCategoryValidator paymentCategoryValidator) {
    super(paymentCategoryRepository, paymentCategoryValidator);
  }
}
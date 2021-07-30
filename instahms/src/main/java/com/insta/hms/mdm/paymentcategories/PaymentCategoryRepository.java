package com.insta.hms.mdm.paymentcategories;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

import java.math.BigDecimal;

@Repository
public class PaymentCategoryRepository extends MasterRepository<BigDecimal> {

  public PaymentCategoryRepository() {
    super("category_type_master", "cat_id", "cat_name");
  }

}

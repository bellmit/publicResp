package com.insta.hms.mdm.paymentterms;

import com.insta.hms.mdm.MasterRepository;

import org.springframework.stereotype.Repository;

/**
 * The Class PaymentTermsRepository.
 *
 * @author ashokkumar
 */

@Repository
public class PaymentTermsRepository extends MasterRepository<String> {

  /**
   * Instantiates a new payment terms repository.
   */
  public PaymentTermsRepository() {
    super("ph_payment_terms", "template_code", "template_name");
  }

}

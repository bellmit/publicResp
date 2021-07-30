package com.insta.hms.mdm.paymentterms;

import com.insta.hms.mdm.MasterService;

import org.springframework.stereotype.Service;

/**
 * The Class PaymentTermsService.
 *
 * @author ashokkumar
 */

@Service
public class PaymentTermsService extends MasterService {

  /**
   * Instantiates a new payment terms service.
   *
   * @param repository
   *          the repository
   * @param validator
   *          the validator
   */
  public PaymentTermsService(PaymentTermsRepository repository, PaymentTermsValidator validator) {
    super(repository, validator);
  }

}

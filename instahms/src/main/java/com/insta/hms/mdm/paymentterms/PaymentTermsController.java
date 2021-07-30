package com.insta.hms.mdm.paymentterms;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterController;
import com.insta.hms.mdm.MasterResponseRouter;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class PaymentTermsController.
 *
 * @author ashokkumar
 */

@Controller
@RequestMapping(URLRoute.PAYMENT_TERMS_MASTER_PATH)
public class PaymentTermsController extends MasterController {

  /** The loger. */
  static Logger loger = LoggerFactory.getLogger(PaymentTermsController.class);

  /**
   * Instantiates a new payment terms controller.
   *
   * @param paymentTermsService
   *          the payment terms service
   */
  public PaymentTermsController(PaymentTermsService paymentTermsService) {
    super(paymentTermsService, MasterResponseRouter.PAYMENT_TERMS_MASTER_ROUTER);
  }

  /*
   * (non-Javadoc)
   *
   * @see com.insta.hms.mdm.MasterController#getReferenceLists(java.util.Map)
   */
  @Override
  protected Map<String, List<BasicDynaBean>> getReferenceLists(Map params) {
    Map<String, List<BasicDynaBean>> lookupMaps = new HashMap<>();
    lookupMaps.put("paymentTermsList", ((PaymentTermsService) getService()).lookup(false));
    return lookupMaps;
  }
}

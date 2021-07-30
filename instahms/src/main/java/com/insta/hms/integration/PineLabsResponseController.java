package com.insta.hms.integration;

import com.insta.hms.common.BaseRestController;
import com.insta.hms.integration.paymentgateway.GenericPaymentsAggregator;
import com.insta.hms.integration.paymentgateway.PaymentGatewayAggregatorFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;

/**
 * This controller contains endpoint which is exposed to pine labs to get the txn result.
 * This endpoint is bypassed from login action.
 * 
 * @author Utkarsh
 *
 */

@Controller
@RequestMapping(URLRoute.PINE_LABS)
public class PineLabsResponseController extends BaseRestController {

  static Logger log = LoggerFactory.getLogger(PineLabsResponseController.class);

  /**
   * Pine Labs will post transaction details to this end-point.
   */
  @RequestMapping(value = "/transactionresult", method = RequestMethod.POST)
  public void pineLabsTxResponse(HttpServletRequest request) {

    try {
      GenericPaymentsAggregator pineLabsAggregator = new PaymentGatewayAggregatorFactory()
          .getPaymentGatewayAggregatorInstance("PineLabs");
      pineLabsAggregator.processResponse(request.getParameter("ResponseCode"));
    } catch (IllegalAccessException | InstantiationException ex) {
      log.error("", ex);
    }

  }

}

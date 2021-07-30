package com.insta.hms.mdm.paymentmode;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.exception.DuplicateEntityException;
import com.insta.hms.exception.EntityNotFoundException;
import com.insta.hms.integration.InstaIntegrationRepository;
import com.insta.hms.integration.InstaIntegrationService;
import com.insta.hms.mdm.MasterService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.DynaBean;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class PaymentModeService.
 */
@Service
public class PaymentModeService extends MasterService {

  /** The insta integration service. */
  @LazyAutowired
  private InstaIntegrationService instaIntegrationService;

  /** The payment mode repository. */
  @LazyAutowired
  private PaymentModeRepository paymentModeRepository;

  /** The validator. */
  @LazyAutowired
  private PaymentModeValidator validator;

  /** The insta integration repository. */
  @LazyAutowired
  private InstaIntegrationRepository instaIntegrationRepository;

  /** The Constant PAYTMMODEID. */
  private static final int PAYTMMODEID = -2;

  /**
   * Instantiates a new payment mode service.
   *
   * @param paymentModeRepository
   *          the payment mode repository
   * @param paymentModeValidator
   *          the payment mode validator
   */
  public PaymentModeService(PaymentModeRepository paymentModeRepository,
      PaymentModeValidator paymentModeValidator) {
    super(paymentModeRepository, paymentModeValidator);
  }

  /**
   * Gets the adds the edit page data.
   *
   * @param params
   *          the params
   * @return the adds the edit page data
   */
  @SuppressWarnings({ "rawtypes" })
  public Map<String, List<BasicDynaBean>> getAddEditPageData(Map params) {
    Map<String, List<BasicDynaBean>> map = new HashMap<String, List<BasicDynaBean>>();
    if (((String[]) params.get("mode_id")) != null) {
      if (Integer.valueOf(((String[]) params.get("mode_id"))[0]) == PAYTMMODEID) {
        BasicDynaBean additionalDetailsBean = instaIntegrationService
            .getActiveRecord("paytm_withdraw_money");
        map.put("additionalDetailsBean", Collections.singletonList(additionalDetailsBean));
      }
      map.put("paymentModeDetails", paymentModeNamesAndIds());
    }

    return map;
  }

  /**
   * Payment mode names and ids.
   *
   * @return the list
   */
  public List<BasicDynaBean> paymentModeNamesAndIds() {
    return ((PaymentModeRepository) getRepository()).getPaymentModeNamesAndIds();
  }

  /**
   * Gets the payment mode.
   *
   * @param keyColumn
   *          the key column
   * @param identifier
   *          the identifier
   * @return the payment mode
   */
  public BasicDynaBean getPaymentMode(String keyColumn, Integer identifier) {
    return paymentModeRepository.findByKey(keyColumn, identifier);
  }

  /**
   * Update payment mode.
   *
   * @param parameters
   *          the parameters
   * @return true, if successful
   */
  @Transactional(rollbackFor = Exception.class)
  public boolean updatePaymentMode(Map<String, String[]> parameters) {

    boolean success = false;
    BasicDynaBean paymentmodeBean = paymentModeRepository.getBean();
    List<String> errors = new ArrayList<String>();
    ConversionUtils.copyToDynaBean(parameters, paymentmodeBean, errors);

    validator.validateUpdate(paymentmodeBean);
    Integer key = Integer.valueOf(((String[]) parameters.get("mode_id"))[0]);
    Map<String, Integer> keys = new HashMap<String, Integer>();
    keys.put("mode_id", key);
    String paymentmodeName = (String) paymentmodeBean.get("payment_mode");
    String modeId = parameters.get("mode_id")[0];
    if (errors.isEmpty()) {
      DynaBean exists = paymentModeRepository.findByKey("payment_mode",
          paymentmodeBean.get("payment_mode"));
      if (exists != null && !key.equals(exists.get("mode_id"))) {
        throw new DuplicateEntityException(new String[] { "Payment Mode", paymentmodeName });
      } else {
        success = paymentModeRepository.update(paymentmodeBean, keys) > 0;
      }
    } else {
      throw new EntityNotFoundException(new String[] { "Payment mode", "id", modeId });
    }

    return success;
  }
}
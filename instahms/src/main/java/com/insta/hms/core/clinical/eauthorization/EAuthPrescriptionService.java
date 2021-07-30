package com.insta.hms.core.clinical.eauthorization;

import com.insta.hms.common.annotations.LazyAutowired;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * The Class EAuthPrescriptionService.
 */
@Service
public class EAuthPrescriptionService {

  /** The e auth prescription repository. */
  @LazyAutowired
  private EAuthPrescriptionRepository eauthPrescriptionRepository;
  
  private static Logger logger = LoggerFactory.getLogger(EAuthPrescriptionService.class);

  /**
   * Gets the mr nos by E auth presc id.
   *
   * @param preAuthPrescIds
   *          the pre auth presc ids
   * @return the mr nos by E auth presc id
   */
  public List<String> getMrNosByEAuthPrescId(List<String> preAuthPrescIds) {

    List<Integer> prescIdList = new ArrayList<>();
    for (String prescId : preAuthPrescIds) {
      prescIdList.add(Integer.valueOf(prescId));
    }

    List<BasicDynaBean> mrNoBeanList = eauthPrescriptionRepository
        .getMrNosByEAuthPrescId(prescIdList);
    List<String> mrNoList = new ArrayList<>();
    for (BasicDynaBean mrNoBean : mrNoBeanList) {
      if (StringUtils.isNotBlank((String) mrNoBean.get("mr_no"))) {
        mrNoList.add((String) mrNoBean.get("mr_no"));
      }
    }
    return mrNoList;
  }

  /**
   * Check if the given preauthId is valid.
   * @param preAuthId pre authorization id
   * @return true or false
   */
  public Boolean isPreAuthPrescIdValid(String preAuthId) {
    Integer integerPreAuthId = null;
    try {
      integerPreAuthId = Integer.parseInt(preAuthId);
    } catch (NumberFormatException exception) {
      logger.error("Unable to parse:" + preAuthId, exception);
      return false;
    }
    return eauthPrescriptionRepository.exist("preauth_presc_id", integerPreAuthId);
  }
}

package com.insta.hms.integration.easyrewards;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.session.SessionService;

import org.springframework.stereotype.Service;

/**
 * The Class EasyRewardService.
 */
@Service
public class EasyRewardService {

  /** The session service. */
  @LazyAutowired
  private SessionService sessionService;

  @LazyAutowired
  private EasyRewardUtil easyRewardUtil;

  /**
   * The method is for Coupon Redemption.
   * @param easyRewardRequest input coming from UI
   * @return EasyRewardResponse will be returned to UI
   * @throws Exception the exception
   */
  public EasyRewardResponse couponRedemption(EasyRewardRequest easyRewardRequest) throws Exception {

    if (easyRewardRequest != null) {
      Integer centerId = RequestContext.getCenterId() != null ? RequestContext.getCenterId() : 0;
      easyRewardRequest.setCenterId(Integer.toString(centerId));
      String userName = sessionService.getSessionAttributes().get("userId").toString();
      easyRewardRequest.setUserName(userName);

      return easyRewardUtil.getCouponRedemption(easyRewardRequest);
    }
    return null;
  }

}

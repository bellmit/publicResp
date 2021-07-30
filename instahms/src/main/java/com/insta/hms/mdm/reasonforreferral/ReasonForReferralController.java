package com.insta.hms.mdm.reasonforreferral;

import com.insta.hms.master.URLRoute;
import com.insta.hms.mdm.MasterRestController;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(URLRoute.REASON_FOR_REFERRAL_MASTER)
public class ReasonForReferralController extends MasterRestController {

  public ReasonForReferralController(
      ReasonForReferralService reasonForReferralService) {
    super(reasonForReferralService);
  }

  /**
   * Find reason for referral based on filters.
   *
   * @param params
   *          filter map
   * @return map of reason for referrals list
   */
  @GetMapping(value = URLRoute.FIND_BY_FILTER)
  public ResponseEntity<Map<String, Object>> findByFilters(
      @RequestParam Map<String, String> params) {
    Map<String, Object> responseBody = new HashMap<>();
    responseBody.putAll((
        (ReasonForReferralService) getService()).findByFilters(params));

    return new ResponseEntity<>(responseBody, HttpStatus.OK);
  }

  /**
   * Gets the index page.
   *
   * @return the index page
   */
  @GetMapping(URLRoute.MASTER_INDEX_URL)
  public ModelAndView getIndexPage() {
    //return new ModelAndView(URLRoute.HOSPITAL_ADMIN_INDEX_PAGE);
    return renderMasterUi("Master","hospitalAdminMasters");
  }

}

/**
 * 
 */

package com.insta.hms.core.patient.followupdetails;

import com.bob.hms.common.DateUtil;
import com.insta.hms.common.DateHelper;
import com.insta.hms.core.clinical.consultation.ValidationUtils;
import com.insta.hms.exception.ValidationErrorMap;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Component;

/**
 * The Class FollowUpValidator.
 *
 * @author anup vishwas
 */

@Component
public class FollowUpValidator {

  /**
   * Validatefollow up insert.
   *
   * @param followupbean
   *          the followupbean
   * @param consultationBean
   *          the consultation bean
   * @param errMap
   *          the err map
   * @return true, if successful
   */
  public boolean validatefollowUpInsert(BasicDynaBean followupbean, BasicDynaBean consultationBean,
      ValidationErrorMap errMap) {
    boolean valid = true;
    if (followupbean.get("followup_doctor_id") == null
        || followupbean.get("followup_doctor_id").equals("")) {
      errMap.addError("followup_doctor_id", "exception.consultation.followup.doctor.id.notnull");
      valid = false;
    }

    if (followupbean.get("followup_date") == null || followupbean.get("followup_date").equals("")) {
      errMap.addError("followup_date", "exception.consultation.followup.date.notnull");
      valid = false;
    } else {
      String visitDate = DateUtil.formatDate((java.util.Date) consultationBean.get("visited_date"));
      String followupDate = DateUtil.formatDate((java.util.Date) followupbean.get("followup_date"));
      if ((DateHelper.parseDate(visitDate).compareTo(DateHelper.parseDate(followupDate))) > 0) {
        errMap.addError("followup_date", "exception.consultation.followup.date.notvalid");
        valid = false;
      }
    }

    return valid;
  }

  /**
   * Validatefollow up update.
   *
   * @param followupbean
   *          the followupbean
   * @param consultationBean
   *          the consultation bean
   * @param errMap
   *          the err map
   * @return true, if successful
   */
  public boolean validatefollowUpUpdate(BasicDynaBean followupbean, BasicDynaBean consultationBean,
      ValidationErrorMap errMap) {
    boolean valid = true;
    if (followupbean.get("followup_doctor_id") == null
        || followupbean.get("followup_doctor_id").equals("")) {
      errMap.addError("followup_doctor_id", "exception.consultation.followup.doctor.id.notnull");
      valid = false;
    }
    valid = validateFollowUpId((String) followupbean.get("followup_id"), errMap);
    if (followupbean.get("followup_date") == null || followupbean.get("followup_date").equals("")) {
      errMap.addError("followup_date", "exception.consultation.followup.date.notnull");
      valid = false;
    } else {
      String visitDate = DateUtil.formatDate((java.util.Date) consultationBean.get("visited_date"));
      String followupDate = DateUtil.formatDate((java.util.Date) followupbean.get("followup_date"));
      if ((DateHelper.parseDate(visitDate).compareTo(DateHelper.parseDate(followupDate))) > 0) {
        errMap.addError("followup_date", "exception.consultation.followup.date.notvalid");
        valid = false;
      }
    }

    return valid;
  }

  /**
   * Validate follow up id.
   *
   * @param followUpId
   *          the follow up id
   * @param errMap
   *          the err map
   * @return true, if successful
   */
  public boolean validateFollowUpId(String followUpId, ValidationErrorMap errMap) {
    boolean valid = true;
    if (followUpId == null || followUpId.equals("")) {
      errMap.addError("followup_id", " FollowUp id is null");
      valid = false;
    }
    if (!ValidationUtils.isKeyValid("follow_up_details", followUpId, "followup_id")) {
      errMap.addError("followup_id", "exception.consultation.followup.detail.id.notvalid");
      valid = false;
    }
    return valid;
  }
}

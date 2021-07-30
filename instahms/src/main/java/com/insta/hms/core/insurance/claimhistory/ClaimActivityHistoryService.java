package com.insta.hms.core.insurance.claimhistory;

import com.bob.hms.common.DateUtil;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.billing.BillChargeClaimService;
import com.insta.hms.core.billing.BillChargeService;
import com.insta.hms.core.insurance.ClaimSubmissionActivity;
import com.insta.hms.core.insurance.ClaimSubmissionClaim;
import com.insta.hms.core.inventory.sales.SalesClaimDetailsService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

// TODO: Auto-generated Javadoc
/**
 * The Class ClaimActivityHistoryService.
 */
@Service
public class ClaimActivityHistoryService {

  /** The claim act history repo. */
  @LazyAutowired
  private ClaimActivityHistoryRepository claimActHistoryRepo;
  
  @LazyAutowired
  private BillChargeClaimService billChargeClaimService;
  
  @LazyAutowired
  private SalesClaimDetailsService salesClaimDetailsService;

  /**
   * Gets the claim activity history beans to insert.
   *
   * @param claim the claim
   * @param claimSubmissionHistId the claim submission hist id
   * @param userName the user name
   * @param isInternalComplaint the is internal complaint
   * @return the claim activity history beans to insert
   * @throws ParseException the parse exception
   */
  public List<BasicDynaBean> getClaimActivityHistoryBeansToInsert(ClaimSubmissionClaim claim,
      Integer claimSubmissionHistId, String userName, 
      Boolean isInternalComplaint) throws ParseException {
    List<ClaimSubmissionActivity> activities = claim.getActivities();
    List<BasicDynaBean> claimActivityHistoryBeans = new ArrayList<>();
    for (ClaimSubmissionActivity activity : activities) {
      String activityId = activity.getActivityID();

      String[] actArray = activityId.split("-");
      
      Boolean isCombinedActivity = false;

      String chargeId = null;
      Integer saleItemId = 0;
      String claimActivityId = null;
      
      if (activityId.startsWith("A")) {
        if (activityId.startsWith("A-ACT")) {
          chargeId = actArray[2];
          isCombinedActivity = true;
          claimActivityId = actArray[1].concat("-").concat(actArray[2]);
        } else {
          chargeId = actArray[1];
          claimActivityId = actArray[1];
        }
      } else if (activityId.startsWith("P")) {

        if (activityId.startsWith("P-ACT")) {
          chargeId = actArray[2];
          saleItemId = Integer.parseInt(actArray[3]);
          isCombinedActivity = true;
          claimActivityId = actArray[1].concat("-").concat(actArray[2])
              .concat("-").concat(actArray[3]);
        } else {
          chargeId = actArray[1];
          saleItemId = Integer.parseInt(actArray[2]);
          claimActivityId = actArray[1].concat("-").concat(actArray[2]);
        }
      }
      
      String claimId = claim.getClaimID();
      
      if (isCombinedActivity) {
        List<BasicDynaBean> combinedActivities = getCombinedActivities(claimId, activityId, 
            claimActivityId, isInternalComplaint);
        if (null != combinedActivities && !combinedActivities.isEmpty()) {
          for (BasicDynaBean combActBean : combinedActivities) {
            BasicDynaBean actBean = claimActHistoryRepo.getBean();
            setCombinedActBean(actBean, activity, combActBean, claimSubmissionHistId,
                claimId, claimActivityId, activityId, userName);
            claimActivityHistoryBeans.add(actBean);
          }
        }
      } else {
        BasicDynaBean actBean = claimActHistoryRepo.getBean();
        setActBean(actBean, activity, claimSubmissionHistId, claimId, claimActivityId, userName, 
            chargeId,saleItemId, activityId);
        claimActivityHistoryBeans.add(actBean);
      }
    }
    return claimActivityHistoryBeans;
  }

  /**
   * Sets the act bean.
   *
   * @param actBean the act bean
   * @param activity the activity
   * @param claimSubmissionHistId the claim submission hist id
   * @param claimActivityId the claim activity id
   * @param userName the user name
   * @param chargeId the charge id
   * @param saleItemId the sale item id
   * @param activityId the activity id
   * @throws ParseException the parse exception
   */
  private void setActBean(BasicDynaBean actBean, ClaimSubmissionActivity activity,
      Integer claimSubmissionHistId, String claimId, String claimActivityId, 
      String userName, String chargeId, Integer saleItemId, String activityId) 
          throws ParseException {
    actBean.set("claim_submission_hist_id", claimSubmissionHistId);
    actBean.set("claim_id", claimId);
    actBean.set("activity_id", activity.getActivityID());
    actBean.set("claim_activity_id", claimActivityId);
    String chargeType = activityId.startsWith("A") ? "Hospital" : "Pharmacy";
    actBean.set("charge_type", chargeType);
    actBean.set("activity_type", activity.getType());
    actBean.set("activity_code", activity.getCode());
    actBean.set("clinician", activity.getClinician());
    actBean.set("activity_start", DateUtil.stringToTimestamp(activity.getStart()));
    actBean.set("ordering_clinician", activity.getOrderingClinician());
    actBean.set("created_at", DateUtil.getCurrentTimestamp());
    actBean.set("created_by", userName);
    actBean.set("charge_id", chargeId);
    actBean.set("sale_item_id", saleItemId);
    actBean.set("quantity", activity.getQuantity());
    actBean.set("claim_amount", activity.getNet());
    actBean.set("activity_vat", activity.getVat());
    actBean.set("activity_vat_percent", activity.getVatPercent());

  }

  /**
   * Sets the combined act bean.
   *
   * @param actBean the act bean
   * @param activity the activity
   * @param combActBean the comb act bean
   * @param claimSubmissionHistId the claim submission hist id
   * @param claimActivityId the claim activity id
   * @param activityId the activity id
   * @param userName the user name
   * @throws ParseException the parse exception
   */
  private void setCombinedActBean(BasicDynaBean actBean, ClaimSubmissionActivity activity,
      BasicDynaBean combActBean, Integer claimSubmissionHistId, 
      String claimId, String claimActivityId,
      String activityId, String userName) throws ParseException {
    actBean.set("claim_submission_hist_id", claimSubmissionHistId);
    actBean.set("claim_id", claimId);
    actBean.set("activity_id", activity.getActivityID());
    actBean.set("claim_activity_id", claimActivityId);
    String chargeType = activityId.startsWith("A") ? "Hospital" : "Pharmacy";
    actBean.set("charge_type", chargeType);
    actBean.set("activity_type", activity.getType());
    actBean.set("activity_code", activity.getCode());
    actBean.set("clinician", activity.getClinician());
    actBean.set("activity_start", DateUtil.stringToTimestamp(activity.getStart()));
    actBean.set("ordering_clinician", activity.getOrderingClinician());
    actBean.set("created_at", DateUtil.getCurrentTimestamp());
    actBean.set("created_by", userName);
    actBean.set("charge_id", (String) combActBean.get("charge_id"));
    actBean.set("sale_item_id", (Integer) combActBean.get("sale_item_id"));
    actBean.set("quantity", (BigDecimal) combActBean.get("quantity"));
    actBean.set("claim_amount", (BigDecimal) combActBean.get("insurance_claim_amt"));
    actBean.set("activity_vat", (BigDecimal) combActBean.get("tax_amt"));
    actBean.set("activity_vat_percent", combActBean.get("tax_rate"));

  }

  /**
   * Gets the combined activities.
   *
   * @param activityId the activity id
   * @param claimActivityId the claim activity id
   * @param isInternalComplaint the is internal complaint
   * @return the combined activities
   */
  private List<BasicDynaBean> getCombinedActivities(String claimId, String activityId,
      String claimActivityId, Boolean isInternalComplaint) {
    List<BasicDynaBean> combinedActivities = new ArrayList<>();
    
    if (activityId.startsWith("A")) {
      combinedActivities = billChargeClaimService.getCombinedActivities(claimId, claimActivityId,
          isInternalComplaint);
    } else if (activityId.startsWith("P")) {
      combinedActivities = salesClaimDetailsService.getCombinedActivities(claimId, claimActivityId,
          isInternalComplaint);
    }
    return combinedActivities;
  }

}

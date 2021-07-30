package com.insta.hms.core.insurance.claimhistory;

import com.bob.hms.common.DateUtil;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.core.insurance.ClaimSubmissionClaim;
import com.insta.hms.core.insurance.ClaimSubmissionHeader;
import com.insta.hms.core.insurance.ClaimSubmissionResubmission;
import com.insta.hms.core.insurance.ClaimSubmissionService;
import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class ClaimSubmissionHistoryService.
 */
@Service
public class ClaimSubmissionHistoryService {

  /** The claim sub history repo. */
  @LazyAutowired
  private ClaimSubmissionHistoryRepository claimSubHistoryRepo;

  /** The claim activity history service. */
  @LazyAutowired
  private ClaimActivityHistoryRepository claimActivityHistoryService;

  /** The claim submission service. */
  @LazyAutowired
  private ClaimSubmissionService claimSubmissionService;

  /** The claim activity hisotory service. */
  @LazyAutowired
  private ClaimActivityHistoryService claimActivityHisotoryService;

  /** The claim activity history repo. */
  @LazyAutowired
  private ClaimActivityHistoryRepository claimActivityHistoryRepo;

  /** The session service. */
  @LazyAutowired
  private SessionService sessionService;

  /**
   * Insert claim submission history.
   *
   * @param submissionBatchID the submission batch ID
   * @param header the header
   * @param claims the claims
   * @throws ParseException the parse exception
   */
  public void insertClaimSubmissionHistory(String submissionBatchID, ClaimSubmissionHeader header,
      List<ClaimSubmissionClaim> claims) throws ParseException {
    List<BasicDynaBean> claimList = claimSubmissionService
        .getClaimsInSubmissionBatch(submissionBatchID);
    
    List<BasicDynaBean> claimSubmissionCountList = claimSubmissionService
        .getClaimSubmissionCountList(submissionBatchID);
    
    Map<String, BasicDynaBean> claimMap = ConversionUtils.listBeanToMapBean(claimList, "claim_id");
    
    Map<String, BasicDynaBean> claimSubmissionCountMap = ConversionUtils
        .listBeanToMapBean(claimSubmissionCountList, "claim_id");

    List<BasicDynaBean> claimSubHistoryList = new ArrayList<>();
    List<BasicDynaBean> claimActivityHistoryList = new ArrayList<>();

    for (ClaimSubmissionClaim claim : claims) {

      BasicDynaBean bean = claimSubHistoryRepo.getBean();
      String claimId = claim.getClaimID();
      BasicDynaBean claimBean = claimMap.get(claimId);
      Integer claimSubmissionHistId = claimSubHistoryRepo.getNextSequence();
      bean.set("claim_submission_hist_id", claimSubmissionHistId);
      bean.set("submission_batch_id", submissionBatchID);
      bean.set("claim_id", claimId);
      bean.set("transaction_date", DateUtil.stringToTimestamp(header.getTransactionDate()));
      bean.set("insurance_co_id", claimBean.get("insurance_co"));
      if (StringUtils.isNotBlank(header.getSenderID())) {
        bean.set("sender_id", header.getSenderID());
      } else {
        bean.set("sender_id", header.getProviderID());
      }
      if (StringUtils.isNotBlank(header.getReceiverID())) {
        bean.set("receiver_id", header.getReceiverID());
      } else {
        bean.set("receiver_id", claim.getReceiverID());
      }
      bean.set("tpa_id", claimBean.get("sponsor_id"));
      bean.set("member_id", claim.getMemberId());
      bean.set("total_amount", claim.getGross());
      bean.set("total_claim_amount", claim.getNet());
      bean.set("total_patient_amount", claim.getPatientShare());
      bean.set("total_claim_tax", claim.getVat());
      ClaimSubmissionResubmission resub = claim.getResubmission();
      Boolean isInternalComplaint = false;
      if (null != resub) {
        bean.set("resubmission_type", resub.getType());
        if (resub.getType().equals("internal complaint")
            || resub.getType().equals("reconciliation")) {
          isInternalComplaint = true;
        }
        bean.set("resubmission_remarks", resub.getComment());
      }

      Map<String, Object> sessionAttributes = sessionService.getSessionAttributes();
      final String userName = (String) sessionAttributes.get("userId");

      bean.set("created_by", userName);
      bean.set("created_at", DateUtil.getCurrentTimestamp());
      
      BasicDynaBean claimSubmissionCountBean = claimSubmissionCountMap.get(claimId);
      bean.set("submission_number", (Integer)claimSubmissionCountBean.get("submissioncount"));

      claimSubHistoryList.add(bean);

      List<BasicDynaBean> claimActivityHistoryBeans = claimActivityHisotoryService
          .getClaimActivityHistoryBeansToInsert(claim, claimSubmissionHistId, 
              userName, isInternalComplaint);

      claimActivityHistoryList.addAll(claimActivityHistoryBeans);
    }

    claimSubHistoryRepo.batchInsert(claimSubHistoryList);
    claimActivityHistoryRepo.batchInsert(claimActivityHistoryList);
  }

  public Boolean updateClaimReceivedAmount(Integer remittanceId) {
    return claimSubHistoryRepo.updateClaimReceivedAmount(remittanceId);
  }

  public Boolean updateClaimRecoveryAmount(Integer remittanceId) {
    return claimSubHistoryRepo.updateClaimRecoveryAmount(remittanceId);
  }

  public Boolean updateCombinedActivityReceivedAmount(int remitId) {
    return claimSubHistoryRepo.updateCombinedActivityReceivedAmount(remitId);
  }

  public Boolean updateCombinedActivityRecoveryAmount(int remitId) {
    return claimSubHistoryRepo.updateCombinedActivityRecoveryAmount(remitId);
  }

  public Boolean updateActivityStatusForHospital(int remitId) {
    return claimSubHistoryRepo.updateActivityStatusForHospital(remitId);
  }

  public Boolean updateActivityStatusForPharmacy(int remitId) {
    return claimSubHistoryRepo.updateActivityStatusForPharmacy(remitId);
  }

  public List<BasicDynaBean> getClaimActivityHistory(String claimId, String chargeId, 
      Integer saleItemId) {
    return claimSubHistoryRepo.getClaimActivityHistory(claimId, chargeId, saleItemId);
  }

  public Boolean updateRecievedAmtAndRemittanceId(int remitId) {
    return claimSubHistoryRepo.updateRecievedAmtAndRemittanceId(remitId);
  }

  public Boolean updateRecoveryAmtAndRemittanceId(int remitId) {
    return claimSubHistoryRepo.updateRecoveryAmtAndRemittanceId(remitId);
  }

  public Map<String, Object> getBillLevelClaimHistory(String fromSubmissionDate,
      String toSubmissionDate) throws ParseException {
    return claimSubHistoryRepo.getBillLevelClaimHistory(DateUtil.parseDate(fromSubmissionDate),
        DateUtil.parseDate(toSubmissionDate));
  }

}

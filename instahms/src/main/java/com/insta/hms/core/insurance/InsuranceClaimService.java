package com.insta.hms.core.insurance;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.core.billing.BillClaimService;
import com.insta.hms.core.billing.BillService;
import com.insta.hms.core.patient.registration.RegistrationService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class InsuranceClaimService.
 */
@Service
public class InsuranceClaimService {

  @LazyAutowired
  private InsuranceClaimRepository insuranceClaimRepo;

  @LazyAutowired
  private GenericPreferencesService genPrefService;

  @LazyAutowired
  private BillService billService;

  @LazyAutowired
  private RegistrationService regService;

  @LazyAutowired
  private BillClaimService billClaimService;

  Logger logger = LoggerFactory.getLogger(InsuranceClaimService.class);

  /**
   * Gets the next prefixed id.
   *
   * @param centerId the center id
   * @param accGrpId the acc grp id
   * @return the next prefixed id
   */
  public String getNextPrefixedId(int centerId, Integer accGrpId) {
    return insuranceClaimRepo.getNextPrefixedId(centerId, accGrpId);
  }

  /**
   * Gets the bean.
   *
   * @return the bean
   */
  public BasicDynaBean getBean() {
    return insuranceClaimRepo.getBean();
  }

  /**
   * Insert.
   *
   * @param claimbean the claimbean
   */
  public void insert(BasicDynaBean claimbean) {
    insuranceClaimRepo.insert(claimbean);
  }

  /**
   * Gets the claim by id.
   *
   * @param claimId the claim id
   * @return the claim by id
   */
  public BasicDynaBean getClaimById(String claimId) {
    // TODO Auto-generated method stub
    return insuranceClaimRepo.getClaimById(claimId);
  }

  /**
   * Find by key.
   *
   * @param key      the key
   * @param keyValue the key value
   * @return the basic dyna bean
   */
  public BasicDynaBean findByKey(String key, String keyValue) {
    // TODO Auto-generated method stub
    return insuranceClaimRepo.findByKey(key, keyValue);
  }

  /**
   * Update.
   *
   * @param insClaimBean the ins claim bean
   * @param insClaimKeys the ins claim keys
   * @return the boolean
   */
  public Boolean update(BasicDynaBean insClaimBean, Map<String, Object> insClaimKeys) {
    // TODO Auto-generated method stub
    return insuranceClaimRepo.update(insClaimBean, insClaimKeys) >= 0;
  }

  /**
   * Checks if is claim id exist.
   *
   * @param claimId the claim id
   * @return the boolean
   */
  public Boolean isClaimIdExist(String claimId) {
    return insuranceClaimRepo.exist("claim_id", claimId);
  }

  /**
   * Find all charges.
   *
   * @param claimId           the claim id
   * @param billNo            the bill no
   * @param isResubmission    the is resubmission
   * @param checkDrgOrPerDiem the check drg or per diem
   * @return the list
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> findAllCharges(String claimId, String billNo, Boolean isResubmission,
      Boolean checkDrgOrPerDiem) throws SQLException {
    return insuranceClaimRepo.findAllCharges(billNo, claimId, isResubmission, checkDrgOrPerDiem);
  }

  /**
   * Gets the attachment.
   *
   * @param claimId the claim id
   * @return the attachment
   */
  public BasicDynaBean getAttachment(String claimId) {
    return insuranceClaimRepo.getAttachment(claimId);
  }

  /**
   * Find all charges for XML.
   *
   * @param claimId           the claim id
   * @param ignoreExternalPbm the ignore external pbm
   * @param isResubmission    the is resubmission
   * @return the list
   * @throws SQLException the SQL exception
   */
  public List<BasicDynaBean> findAllChargesForXML(String claimId, Boolean ignoreExternalPbm,
      Boolean isResubmission, boolean isAccumed) throws SQLException {
    Map<String, Object> keyMap = new HashMap<String, Object>();
    keyMap.put("claim_id", claimId);
    BasicDynaBean billClaimBean = billClaimService.findByKey(keyMap);
    if (billClaimBean != null) {
      BasicDynaBean visitbean = regService.findByKey((String) billClaimBean.get("visit_id"));
      boolean hasDRG = visitbean != null && visitbean.get("use_drg").equals("Y");
      boolean hasPerdiem = visitbean != null && visitbean.get("use_perdiem").equals("Y");
      boolean checkDRGorPRD = (hasDRG || hasPerdiem);
      return insuranceClaimRepo.findAllChargesForXML(claimId, ignoreExternalPbm, isResubmission,
          checkDRGorPRD, isAccumed);
    }
    return new ArrayList<>();
  }

  /**
   * Find all claim observations.
   *
   * @param claimId         the claim id
   * @param sponsorId       the sponsor id
   * @param healthAuthority the health authority
   * @return the list
   */
  public List<BasicDynaBean> findAllClaimObservations(String claimId, String sponsorId,
      String healthAuthority) {

    return insuranceClaimRepo.findAllClaimObservations(claimId, sponsorId, healthAuthority);
  }

}

package com.insta.hms.documents;

import com.bob.hms.common.NumberToWordFormat;
import com.insta.hms.billing.EditInsuranceHelper;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.genericdocuments.GenericDocumentsFields;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class InsuranceDocumentStore extends AbstractDocumentStore {

  public InsuranceDocumentStore() {
    super("insurance", true);
    // TODO Auto-generated constructor stub
  }

  public static enum KEYS {
    insurance_id
  }

  @LazyAutowired
  private InsuranceDocsRepository insdocrepo;
  @LazyAutowired
  private InsuranceCaseRepository insuranceCaseRepository;
  @LazyAutowired
  private GenericDocumentsUtil genericDocumentsUtil;

  @Override
  public Map<String, Object> getKeys() {
    Map<String, Object> keyValues = new HashMap<>();
    for (KEYS key : KEYS.values()) {
      keyValues.put(key.name(), null);
    }

    return keyValues;
  }

  @Override
  public boolean postCreate(int docid, Map requestParams, List errors) {
    BasicDynaBean insdocbean = insdocrepo.getBean();

    ConversionUtils.copyToDynaBean(requestParams, insdocbean, errors);

    if (errors.isEmpty()) {
      insdocbean.set("doc_id", docid);
      return insdocrepo.insert(insdocbean) > 0;
    } else {
      requestParams.put("error", "Incorrectly formatted details supplied..");
    }
    return false;
  }

  @Override
  public boolean postUpdate(int docid, Map requestParams, List errors) {

    Map<String, Object> keys = new HashMap<>();
    keys.put("doc_id", docid);

    BasicDynaBean insdocbean = insdocrepo.getBean();
    ConversionUtils.copyToDynaBean(requestParams, insdocbean, errors);

    if (errors.isEmpty()) {
      insdocbean.set("doc_id", docid);
      if (insdocrepo.update(insdocbean, keys) > 0) {
        return true;
      }
      return false;

    } else {
      requestParams.put("error", "Incorrectly formatted details supplied..");
    }
    return false;
  }

  @Override
  public boolean postDelete(Object docId, String format) {
    return insdocrepo.delete("doc_id", docId) > 0;
  }

  @Override
  public List<BasicDynaBean> getDocumentsList(String key, Object valueCol, Boolean specialized,
      String specializedDocType) {
    return null;
  }

  @Override
  public List<BasicDynaBean> searchDocuments(Map listingParams, Map extraParams,
      Boolean specialized) {

    return InsuranceCaseRepository.getInsuranceDocs(listingParams, extraParams, specialized);
  }

  @Override
  public void copyReplaceableFields(Map to, Map keyParams, boolean underscore) throws SQLException {

    if (to == null) {
      return;
    }

    int insuranceId = Integer.parseInt((String) keyParams.get("insurance_id"));
    BasicDynaBean caseBean = insuranceCaseRepository.getCaseDetails(insuranceId);
    // copy all the case details
    GenericDocumentsFields.convertAndCopy(caseBean.getMap(), to, underscore);

    // copy all patient details
    String mrNo = (String) caseBean.get("mr_no");
    String patientId = (String) caseBean.get("patient_id");

    genericDocumentsUtil.copyPatientDetails(to, mrNo, patientId, underscore);

    List<BasicDynaBean> insuredBills = new ArrayList<>();

    BigDecimal billClaimAmt = BigDecimal.ZERO;
    BigDecimal billAmt = BigDecimal.ZERO;
    BigDecimal claimRecdAmt = BigDecimal.ZERO;
    BigDecimal approvedAmt = BigDecimal.ZERO;

    if (patientId != null && !patientId.equals("")) {
      // Get all insured bills (Open/Finalized/Closed) for the
      // patient(Main/Followup)
      insuredBills = new EditInsuranceHelper().getMainAndFollowUpVisitTPABills(patientId,
          "all_bills");
    }

    if (insuredBills != null && insuredBills.size() > 0) {
      for (BasicDynaBean bill : insuredBills) {
        billClaimAmt = bill.get("total_claim") != null
            ? billClaimAmt.add((BigDecimal) bill.get("total_claim"))
            : billClaimAmt;
        billAmt = bill.get("total_amount") != null
            ? billAmt.add((BigDecimal) bill.get("total_amount"))
            : billAmt;
        claimRecdAmt = bill.get("claim_recd_amount") != null
            ? claimRecdAmt.add((BigDecimal) bill.get("claim_recd_amount"))
            : claimRecdAmt;
        approvedAmt = bill.get("approval_amount") != null
            ? approvedAmt.add((BigDecimal) bill.get("approval_amount"))
            : approvedAmt;
      }
    }

    String underScoreStr = (underscore ? "_" : "");

    to.put(underScoreStr + "bill_claim_amt_words",
        NumberToWordFormat.wordFormat().toRupeesPaise(billClaimAmt));
    to.put(underScoreStr + "bill_amt_words",
        NumberToWordFormat.wordFormat().toRupeesPaise(billAmt));
    to.put(underScoreStr + "claim_recd_amt_words",
        NumberToWordFormat.wordFormat().toRupeesPaise(claimRecdAmt));
    to.put(underScoreStr + "approved_amt_words",
        NumberToWordFormat.wordFormat().toRupeesPaise(approvedAmt));

    String caseStatus = caseBean.get("status").toString();

    if (caseStatus.equals("A")) {
      to.put(underScoreStr + "case_status", "Approved");
    } else if (caseStatus.equals("F")) {
      to.put(underScoreStr + "case_status", "Finalized");
    } else if (caseStatus.equals("C")) {
      to.put(underScoreStr + "case_status", "Closed");
    } else if (caseStatus.equals("D")) {
      to.put(underScoreStr + "case_status", "Denied");
    } else {
      to.put(underScoreStr + "case_status", "Preauth");
    }

    if (caseBean.get("remarks") != null) {
      to.put(underScoreStr + "case_remarks", caseBean.get("remarks").toString());
    }
  }

  @Override
  public void copyDocumentDetails(int docid, Map to) {
    BasicDynaBean documentDetails = insdocrepo.findByKey("doc_id", docid);
    to.putAll(documentDetails.getMap());
  }

  @Override
  public Map<String, Object> getDocKeyParams(int docid) {
    BasicDynaBean documentDetails = insdocrepo.findByKey("doc_id", docid);
    HashMap hashMap = new HashMap();
    hashMap.put("insurance_id", documentDetails.get("insurance_id").toString());
    hashMap.put("doc_name", documentDetails.get("doc_name").toString());
    return hashMap;
  }

  @Override
  public int getCenterId(Map requestParams) {
    // TODO Auto-generated method stub
    return 0;
  }

}

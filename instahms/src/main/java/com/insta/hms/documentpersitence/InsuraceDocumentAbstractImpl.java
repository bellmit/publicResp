package com.insta.hms.documentpersitence;

import com.bob.hms.common.NumberToWordFormat;
import com.insta.hms.billing.EditInsuranceHelper;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.genericdocuments.GenericDocumentsFields;
import com.insta.hms.insurance.InsuranceDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author krishna.t
 *
 */
public class InsuraceDocumentAbstractImpl extends AbstractDocumentPersistence {

  public static enum KEYS {
    insurance_id
  }

  public GenericDAO insdocdao = new GenericDAO("insurance_docs");

  @Override
  public Map<String, Object> getKeys() {
    Map<String, Object> keyValues = new HashMap<>();
    for (KEYS key : KEYS.values()) {
      keyValues.put(key.name(), null);
    }

    return keyValues;
  }

  @Override
  public boolean otherTxWhileCreate(Connection con, int docid, Map requestParams, List errors)
      throws SQLException, IOException {
    BasicDynaBean insdocbean = insdocdao.getBean();

    ConversionUtils.copyToDynaBean(requestParams, insdocbean, errors);

    if (errors.isEmpty()) {
      insdocbean.set("doc_id", docid);
      return insdocdao.insert(con, insdocbean);
    } else {
      requestParams.put("error", "Incorrectly formatted details supplied..");
    }
    return false;
  }

  @Override
  public boolean otherTxWhileUpdate(Connection con, int docid, Map requestParams, List errors)
      throws SQLException, IOException {

    Map<String, Object> keys = new HashMap<>();
    keys.put("doc_id", docid);

    BasicDynaBean insdocbean = insdocdao.getBean();
    ConversionUtils.copyToDynaBean(requestParams, insdocbean, errors);

    if (errors.isEmpty()) {
      insdocbean.set("doc_id", docid);
      if (insdocdao.update(con, insdocbean.getMap(), keys) > 0) {
        return true;
      }
      return false;

    } else {
      requestParams.put("error", "Incorrectly formatted details supplied..");
    }
    return false;
  }

  @Override
  public boolean otherTxWhileDelete(Connection con, Object docId, String format)
      throws SQLException {
    return insdocdao.delete(con, "doc_id", docId);
  }

  @Override
  public List<BasicDynaBean> getDocumentsList(String key, Object valueCol, Boolean specialized,
      String specializedDocType) throws SQLException, IOException {
    return null;
  }

  @Override
  public PagedList searchDocuments(Map listingParams, Map extraParams, Boolean specialized,
      String specializedDocType) throws SQLException, IOException {

    return InsuranceDAO.getInsuranceDocs(listingParams, extraParams, specialized);
  }

  @Override
  public void copyReplaceableFields(Map to, Map keyParams, boolean underscore) throws SQLException {

    if (to == null) {
      return;
    }

    int insuranceId = Integer.parseInt((String) keyParams.get("insurance_id"));
    BasicDynaBean caseBean = new InsuranceDAO().getCaseDetails(insuranceId);
    // copy all the case details
    GenericDocumentsFields.convertAndCopy(caseBean.getMap(), to, underscore);

    // copy all patient details
    String mrNo = (String) caseBean.get("mr_no");
    String patientId = (String) caseBean.get("patient_id");

    GenericDocumentsFields.copyPatientDetails(to, mrNo, patientId, underscore);

    List<BasicDynaBean> insuredBills = new ArrayList<>();

    BigDecimal billClaimAmt = BigDecimal.ZERO;
    BigDecimal billAmt = BigDecimal.ZERO;
    BigDecimal claimRecdAmt = BigDecimal.ZERO;
    BigDecimal approvedAmt = BigDecimal.ZERO;

    if (patientId != null && !patientId.equals("")) {
      // Get all insured bills (Open/Finalized/Closed) for the patient(Main/Followup)
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
  public void copyDocumentDetails(int docid, Map to) throws SQLException {
    BasicDynaBean documentDetails = insdocdao.findByKey("doc_id", docid);
    to.putAll(documentDetails.getMap());
  }

  @Override
  public Map<String, Object> getDocKeyParams(int docid) throws SQLException {
    BasicDynaBean documentDetails = insdocdao.findByKey("doc_id", docid);
    HashMap hashMap = new HashMap();
    hashMap.put("insurance_id", documentDetails.get("insurance_id").toString());
    hashMap.put("doc_name", documentDetails.get("doc_name").toString());
    return hashMap;
  }

  @Override
  public int getCenterId(Map requestParams) throws SQLException {
    // TODO Auto-generated method stub
    return 0;
  }
}

package com.insta.hms.documentpersitence;

import com.bob.hms.common.DateUtil;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.genericdocuments.GenericDocumentsFields;
import com.insta.hms.insurance.InsuranceDAO;

import org.apache.commons.beanutils.BasicDynaBean;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NationalCardDocumentAbstractImpl extends AbstractDocumentPersistence {

  GenericDAO nationalSponsorDAO = new GenericDAO("patient_national_sponsor_details");
  GenericDAO nationalDocDAO = new GenericDAO("national_sponsor_docs_details");
  public static boolean isSecondaryCard = false;

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.DocumentPersitence.AbstractDocumentPersistence#copyDocumentDetails(int,
   * java.util.Map)
   */
  @Override
  public void copyDocumentDetails(int docid, Map to) throws SQLException {
    if (to == null) {
      return;
    }
    BasicDynaBean docDetailsBean = nationalDocDAO.findByKey("doc_id", docid);
    to.putAll(docDetailsBean.getMap());
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.insta.hms.DocumentPersitence.AbstractDocumentPersistence#copyReplaceableFields(java.util.
   * Map, java.util.Map, boolean)
   */
  @Override
  public void copyReplaceableFields(Map fields, Map keyParams, boolean underscore)
      throws SQLException {
    if (fields == null) {
      return;
    }
    String patientId = (String) keyParams.get("patient_id");
    String mrno = (String) keyParams.get("mr_no");
    GenericDocumentsFields.copyPatientDetails(fields, mrno, patientId, underscore);

  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.DocumentPersitence.AbstractDocumentPersistence#getDocKeyParams(int)
   */
  @Override
  public Map<String, Object> getDocKeyParams(int docid) throws SQLException {
    BasicDynaBean docDetailsBean = nationalDocDAO.findByKey("doc_id", docid);
    HashMap hashMap = new HashMap();
    hashMap.putAll(docDetailsBean.getMap());
    return hashMap;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.insta.hms.DocumentPersitence.AbstractDocumentPersistence#getDocumentsList(java.lang.String,
   * java.lang.Object, java.lang.Boolean, java.lang.String)
   */
  @Override
  public List<BasicDynaBean> getDocumentsList(String key, Object valueCol, Boolean specialized,
      String specializedDocType) throws SQLException, IOException {
    if (key.equalsIgnoreCase("mr_no")) {
      return InsuranceDAO.getAllPatientNationalCardDocuments((String) valueCol);
    } else if (key.equalsIgnoreCase("patient_id")) {
      return InsuranceDAO.getAllVisitNationalCardDocuments((String) valueCol);
    }

    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.DocumentPersitence.AbstractDocumentPersistence#getKeys()
   */
  @Override
  public Map<String, Object> getKeys() {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.DocumentPersitence.AbstractDocumentPersistence#otherTxWhileCreate(java.sql.
   * Connection, int, java.util.Map, java.util.List)
   */
  @Override
  public boolean otherTxWhileCreate(Connection con, int docid, Map requestParams, List errors)
      throws SQLException, IOException {
    Boolean success = false;
    BasicDynaBean corpDocBean = nationalDocDAO.getBean();
    ConversionUtils.copyToDynaBean(requestParams, corpDocBean, errors);
    GenericDAO patGenDocDao = new GenericDAO("patient_general_docs");
    if (errors.isEmpty()) {
      Integer patientNationalSponsorId = (Integer) corpDocBean.get("patient_national_sponsor_id");
      if (patientNationalSponsorId == null || patientNationalSponsorId == 0) {
        String patientId = ((String[]) requestParams.get("patient_id"))[0];
        BasicDynaBean regBean = new VisitDetailsDAO().findByKey(con, "patient_id", patientId);
        Integer priNationalId = (Integer) regBean.get("patient_national_sponsor_id");
        String priSponsorId = (String) regBean.get("primary_sponsor_id");

        if (priSponsorId != null && priNationalId != null && !priSponsorId.equals("")
            && priNationalId != 0) {
          patientNationalSponsorId = (Integer) regBean.get("patient_national_sponsor_id");
        } else {
          patientNationalSponsorId = (Integer) regBean.get("secondary_patient_national_sponsor_id");
        }

        if (isSecondaryCard) {
          patientNationalSponsorId = (Integer) regBean.get("secondary_patient_national_sponsor_id");
          isSecondaryCard = false;// reset the secondary card flag.
        }

        BasicDynaBean patGenDoc = patGenDocDao.getBean();
        patGenDoc.set("mr_no", regBean.get("mr_no"));
        patGenDoc.set("patient_id", regBean.get("patient_id"));
        patGenDoc.set("doc_id", docid);
        patGenDoc.set("doc_name", "Insurance Card");
        patGenDoc.set("doc_date", DateUtil.getCurrentDate());
        patGenDoc.set("username", regBean.get("user_name"));
      }

      if (errors.isEmpty()) {
        corpDocBean.set("doc_id", docid);
        corpDocBean.set("patient_national_sponsor_id", patientNationalSponsorId);
        success = nationalDocDAO.insert(con, corpDocBean);
      }

      return success;
    } else {
      requestParams.put("error", "Incorrectly formatted details supplied..");
    }
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.DocumentPersitence.AbstractDocumentPersistence#otherTxWhileDelete(java.sql.
   * Connection, java.lang.Object, java.lang.String)
   */
  @Override
  public boolean otherTxWhileDelete(Connection con, Object docId, String format)
      throws SQLException, IOException {
    Map columndata = new HashMap();
    columndata.put("doc_id", null);
    return nationalDocDAO.update(con, columndata, "doc_id", docId) > 0;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.DocumentPersitence.AbstractDocumentPersistence#otherTxWhileUpdate(java.sql.
   * Connection, int, java.util.Map, java.util.List)
   */
  @Override
  public boolean otherTxWhileUpdate(Connection con, int docid, Map requestParams, List errors)
      throws SQLException, IOException {
    Map<String, Object> keys = new HashMap<>();
    keys.put("doc_id", docid);
    BasicDynaBean patientplandocbean = nationalDocDAO.getBean();
    ConversionUtils.copyToDynaBean(requestParams, patientplandocbean, errors);
    if (errors.isEmpty()) {
      patientplandocbean.set("doc_id", docid);
      // updates the patient document details like doc_date, doc_name, user etc..
      if (nationalDocDAO.update(con, patientplandocbean.getMap(), keys) > 0) {
        return true;
      }
      return false;

    } else {
      requestParams.put("error", "Incorrectly formatted details supplied..");
    }
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see
   * com.insta.hms.DocumentPersitence.AbstractDocumentPersistence#searchDocuments(java.util.Map,
   * java.util.Map, java.lang.Boolean, java.lang.String)
   */
  @Override
  public PagedList searchDocuments(Map listingParams, Map extraParams, Boolean specialized,
      String specializedDocType) throws SQLException, IOException {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public int getCenterId(Map requestParams) throws SQLException {
    // TODO Auto-generated method stub
    return 0;
  }

}

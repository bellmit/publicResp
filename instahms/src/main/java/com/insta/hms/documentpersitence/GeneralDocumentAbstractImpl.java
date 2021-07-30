package com.insta.hms.documentpersitence;

import com.bob.hms.common.Preferences;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.PagedList;
import com.insta.hms.common.StringUtil;
import com.insta.hms.genericdocuments.GenericDocumentsDAO;
import com.insta.hms.genericdocuments.GenericDocumentsFields;
import com.insta.hms.genericdocuments.PatientDocumentsDAO;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpSession;

/**
 * @author krishna.t
 *
 */
public class GeneralDocumentAbstractImpl extends AbstractDocumentPersistence {
  static Logger log = LoggerFactory.getLogger(GeneralDocumentAbstractImpl.class);

  public static enum KEYS {
    patient_id, mr_no, format
  }

  private GenericDocumentsDAO patientgendocdao = new GenericDocumentsDAO();
  private PatientDocumentsDAO patientdocdao = new PatientDocumentsDAO();

  @Override
  public Map<String, Object> getKeys() {
    Map<String, Object> keyValues = new HashMap<>();
    for (KEYS key : KEYS.values()) {
      keyValues.put(key.name(), null);
    }

    return keyValues;
  }

  @Override
  public int getCenterId(Map requestParams) throws SQLException {
    Object[] docId = (Object[]) requestParams.get("doc_id");

    if (docId != null && docId.length > 0 && !docId[0].equals("")) {
      PatientDocumentsDAO pdao = new PatientDocumentsDAO();
      int docIdInt = -1;
      if ((docId[0]) instanceof String) {
        docIdInt = Integer.parseInt((String) docId[0]);
      } else {
        docIdInt = (Integer) docId[0];
      }
      BasicDynaBean bean = pdao.getPatientDocument(docIdInt);
      if (bean != null) {
        return (Integer) bean.get("center_id");
      }
    }
    Object mrNoObj = requestParams.get("mr_no");
    String[] mrNo = new String[2];
    if (mrNoObj instanceof String) {
      mrNo[0] = (String) mrNoObj;
    } else {
      mrNo = (String[]) mrNoObj;
    }
    String[] patientId = new String[2];
    Object patientIdObj = requestParams.get("patient_id");
    if (patientIdObj instanceof String) {
      patientId[0] = (String) patientIdObj;
    } else {
      patientId = (String[]) patientIdObj;
    }
    int centerId = 0;
    if (patientId != null && patientId.length > 0 && !patientId[0].equals("")) {
      GenericDAO genDAO = new GenericDAO("patient_registration");
      List list = genDAO.findAllByKey("patient_id", patientId[0]);
      if (list != null && list.size() > 0) {
        BasicDynaBean bean = (BasicDynaBean) list.get(0);
        centerId = (Integer) bean.get("center_id");
      } else {
        centerId = RequestContext.getCenterId();
      }
    } else {
      centerId = RequestContext.getCenterId();
    }
    return centerId;
  }

  @Override
  public boolean otherTxWhileCreate(Connection con, int docid, Map requestParams, List errors)
      throws SQLException, IOException {
    BasicDynaBean patientgendocbean = patientgendocdao.getBean();
    ConversionUtils.copyToDynaBean(requestParams, patientgendocbean, errors);
    patientgendocbean.set("created_by", RequestContext.getUserName());
    if (errors.isEmpty()) {
      patientgendocbean.set("doc_id", docid);
      return patientgendocdao.insert(con, patientgendocbean);

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

    BasicDynaBean patientgendocbean = patientgendocdao.getBean();
    ConversionUtils.copyToDynaBean(requestParams, patientgendocbean, errors);

    if (errors.isEmpty()) {
      patientgendocbean.set("doc_id", docid);
      // updates the patient general document details like doc_date, doc_name, user etc..
      if (patientgendocdao.update(con, patientgendocbean.getMap(), keys) > 0) {
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
    return patientgendocdao.delete(con, "doc_id", docId);
  }

  @Override
  public List<BasicDynaBean> getDocumentsList(String key, Object valueCol, Boolean specialized,
      String specializedDocType) throws SQLException, IOException {
    if (key.equalsIgnoreCase("mr_no")) {
      return GenericDocumentsDAO.getAllPatientDocuments((String) valueCol);
    } else if (key.equalsIgnoreCase("patient_id")) {
      return GenericDocumentsDAO.getAllVisitDocuments((String) valueCol);
    }

    return null;
  }

  @Override
  public PagedList searchDocuments(Map listingParams, Map extraParams, Boolean specialized,
      String specializedDocType) throws SQLException, IOException {
    return GenericDocumentsDAO.searchPatientGeneralDocuments(listingParams, extraParams,
        specialized);
  }

  @Override
  public void copyReplaceableFields(Map to, Map keyParams, boolean underscore) throws Exception {
    if (to == null || keyParams == null || keyParams.isEmpty()) {
      return;
    }
    String patientId = (String) keyParams.get("patient_id");
    String mrNo = (String) keyParams.get("mr_no");
    GenericDocumentsFields.copyPatientDetails(to, mrNo, patientId, underscore);
    if (patientId != null && !patientId.isEmpty()) {
      GenericDocumentsFields.copyPatientDiagAndCptCodeDetails(to, patientId, underscore);
    }
    // Call all the method to used for section data
    String format = (String) keyParams.get("format");
    if (format != null
        && (format.equals("doc_pdf_form_templates") || format.equals("doc_rtf_templates"))) {
      BasicDynaBean modBean = new GenericDAO("modules_activated").findByKey("module_id",
          "mod_eclaim");
      if (modBean != null && modBean.get("activation_status") != null
          && modBean.get("activation_status").equals("Y") 
          && patientId != null && !patientId.isEmpty()) {
        GenericDocumentsFields.copyPatientSectionDetailMap(to, patientId, underscore);
      }
    }
    //to get the User Id and Temp User Name
    String tempUserName = null;
    String userName = null;
    String userId = (String) keyParams.get("userId");
    GenericDAO userDao = new GenericDAO("u_user");
    BasicDynaBean userBean = userDao.findByKey("emp_username", userId);
    if ((null != format  && "doc_pdf_form_templates".equals(format))
        && null != userBean && mrNo != null && !StringUtil.isNullOrEmpty(mrNo)) {
      tempUserName = (String) userBean.get("temp_username");
      userName = (String) userBean.get("emp_username");
      GenericDocumentsFields.copyUserName(to, tempUserName, userName, underscore);
    }
  }

  @Override
  public void copyDocumentDetails(int docid, Map to) throws SQLException {
    BasicDynaBean documentDetails = patientgendocdao.findByKey("doc_id", docid);
    to.putAll(documentDetails.getMap());
  }

  @Override
  public Map<String, Object> getDocKeyParams(int docid) throws SQLException {
    BasicDynaBean documentDetailsBean = patientgendocdao.findByKey("doc_id", docid);
    BasicDynaBean patientDocBean = patientdocdao.getBean();
    patientdocdao.loadByteaRecords(patientDocBean, "doc_id", docid);
    String format = patientDocBean.get("doc_format").toString();
    String mrNo = (String) documentDetailsBean.get("mr_no");
    String patientId = (String) documentDetailsBean.get("patient_id");
    String userId = (String) documentDetailsBean.get("username");
    HashMap hashmap = new HashMap();
    hashmap.put("mr_no", mrNo);
    hashmap.put("patient_id", patientId);
    hashmap.put("format", format);
    hashmap.put("userId", userId);
    return hashmap;
  }
}

package com.insta.hms.documents;

import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.patient.registration.PatientRegistrationRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class GeneralDocumentStore.
 */
@Component
public class GeneralDocumentStore extends AbstractDocumentStore {

  /**
   * Instantiates a new general document store.
   */
  public GeneralDocumentStore() {
    super("gen_docs", false);
  }

  /**
   * The Enum KEYS.
   */
  public static enum KEYS {
    /** The patient id. */
    patient_id, /** The mr no. */
    mr_no
  }

  /** The patientgendocrepo. */
  @LazyAutowired
  private PatientGeneralDocsRepository patientgendocrepo;

  /** The patregrepo. */
  @LazyAutowired
  private PatientRegistrationRepository patregrepo;

  /** The generic documents util. */
  @LazyAutowired
  private GenericDocumentsUtil genericDocumentsUtil;

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.documents.AbstractDocumentStore#getKeys()
   */
  @Override
  public Map<String, Object> getKeys() {
    Map<String, Object> keyValues = new HashMap<>();
    for (KEYS key : KEYS.values()) {
      keyValues.put(key.name(), null);
    }

    return keyValues;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.documents.AbstractDocumentStore#getCenterId(java.util.Map)
   */
  @Override
  public int getCenterId(Map requestParams) {
    Object[] docId = (Object[]) requestParams.get("doc_id");

    if (docId != null && docId.length > 0 && !docId[0].equals("")) {
      int docIdInt = -1;
      if ((docId[0]) instanceof String) {
        docIdInt = Integer.parseInt((String) docId[0]);
      } else {
        docIdInt = (Integer) docId[0];
      }
      BasicDynaBean bean = PatientDocumentRepository.getPatientDocument(docIdInt);
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
      List list = patregrepo.listAll(null, "patient_id", patientId[0]);
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

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.documents.AbstractDocumentStore#postCreate(int, java.util.Map,
   * java.util.List)
   */
  @SuppressWarnings({ "unchecked", "rawtypes" })
  @Override
  public boolean postCreate(int docid, Map requestParams, List errors) throws IOException {

    BasicDynaBean patientGeneralDocsBean = null;
    if (requestParams.get("mr_no") != null) {
      try {
        Integer oldDocId = Integer.parseInt((String) ((Map<String, Object[]>) requestParams)
            .get("document_id")[0]);
        Map map = new HashMap();
        map.put("doc_id", oldDocId);
        patientGeneralDocsBean = patientgendocrepo.getBean();
        patientGeneralDocsBean.set("doc_id", docid);
        if (errors.isEmpty()) {
          patientGeneralDocsBean.set("doc_id", docid);
          return 0 < patientgendocrepo.update(patientGeneralDocsBean, map);
        }
      } catch (Exception exc) {
        patientGeneralDocsBean = patientgendocrepo.getBean();
        patientGeneralDocsBean
            .set("mr_no", ((Map<String, Object[]>) requestParams).get("mr_no")[0]);
        patientGeneralDocsBean.set("doc_name",
            ((Map<String, Object[]>) requestParams).get("doc_name")[0]);
        patientGeneralDocsBean.set("patient_id",
            ((Map<String, Object[]>) requestParams).get("patient_id")[0]);
        patientGeneralDocsBean.set("doc_id", docid);
        patientGeneralDocsBean.set("doc_date", DateUtil.getCurrentDate());
        patientGeneralDocsBean.set("username",
            (String) sessionService.getSessionAttributes().get("userId"));
        if (errors.isEmpty()) {
          return 0 < patientgendocrepo.insert(patientGeneralDocsBean);
        }
      }
    }
    requestParams.put("error", "Incorrectly formatted details supplied..");
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.documents.AbstractDocumentStore#postUpdate(int, java.util.Map,
   * java.util.List)
   */
  @Override
  public boolean postUpdate(int docid, Map requestParams, List errors) throws IOException {

    Map<String, Object> keys = new HashMap<>();
    keys.put("doc_id", docid);

    BasicDynaBean patientgendocbean = patientgendocrepo.getBean();
    ConversionUtils.copyToDynaBean(requestParams, patientgendocbean, errors);

    if (errors.isEmpty()) {
      patientgendocbean.set("doc_id", docid);
      // updates the patient general document details like doc_date,
      // doc_name, user etc..
      if (patientgendocrepo.update(patientgendocbean, keys) > 0) {
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
   * @see com.insta.hms.documents.AbstractDocumentStore#postDelete(java.lang.Object,
   * java.lang.String)
   */
  @Override
  public boolean postDelete(Object docId, String format) {
    return 0 < patientgendocrepo.delete("doc_id", docId);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.documents.AbstractDocumentStore#getDocumentsList(java.lang.String,
   * java.lang.Object, java.lang.Boolean, java.lang.String)
   */
  @Override
  public List<BasicDynaBean> getDocumentsList(String key, Object valueCol, Boolean specialized,
      String specializedDocType) throws IOException {
    if (key.equalsIgnoreCase("mr_no")) {
      return PatientGeneralDocsRepository.getAllPatientDocuments((String) valueCol);
    } else if (key.equalsIgnoreCase("patient_id")) {
      return PatientGeneralDocsRepository.getAllVisitDocuments((String) valueCol);
    }

    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.documents.AbstractDocumentStore#searchDocuments(java.util.Map,
   * java.util.Map, java.lang.Boolean)
   */
  @Override
  public List searchDocuments(Map listingParams, Map extraParams, Boolean specialized)
      throws IOException {
    return null; // TODO Migrate the below function when needed
    // PatientGeneralDocsRepository.searchPatientGeneralDocuments(listingParams, extraParams,
    // specialized);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.documents.AbstractDocumentStore#copyReplaceableFields(java.util.Map,
   * java.util.Map, boolean)
   */
  @Override
  public void copyReplaceableFields(Map to, Map keyParams, boolean underscore) {
    if (to == null || keyParams == null || keyParams.isEmpty()) {
      return;
    }
    String patientId = (String) keyParams.get("patient_id");
    String mrNo = (String) keyParams.get("mr_no");
    genericDocumentsUtil.copyPatientDetails(to, mrNo, patientId, underscore);
    if (patientId != null && !patientId.isEmpty()) {
      genericDocumentsUtil.copyPatientDiagAndCptCodeDetails(to, patientId, underscore);
    }
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.documents.AbstractDocumentStore#copyDocumentDetails(int, java.util.Map)
   */
  @Override
  public void copyDocumentDetails(int docid, Map to) {
    BasicDynaBean documentDetails = patientgendocrepo.findByKey("doc_id", docid);
    to.putAll(documentDetails.getMap());
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.documents.AbstractDocumentStore#getDocKeyParams(int)
   */
  @Override
  public Map<String, Object> getDocKeyParams(int docid) {
    BasicDynaBean documentdetailsbean = patientgendocrepo.findByKey("doc_id", docid);
    String mrNo = (String) documentdetailsbean.get("mr_no");
    String patientId = (String) documentdetailsbean.get("patient_id");
    HashMap hashMap = new HashMap();
    hashMap.put("mr_no", mrNo);
    hashMap.put("patient_id", patientId);
    return hashMap;
  }

}

package com.insta.hms.core.patient.registration;

import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.documents.AbstractDocumentStore;
import com.insta.hms.documents.GenericDocumentsUtil;
import com.insta.hms.documents.HvfTemplateFieldsRepository;
import com.insta.hms.documents.PatientDocumentRepository;
import com.insta.hms.documents.PatientHvfDocValuesRepository;
import com.insta.hms.documents.RichTextTemplateRepository;
import com.insta.hms.documents.RtfTemplateRepository;
import com.insta.hms.genericdocuments.CommonHelper;

import freemarker.template.TemplateException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class MLCDocumentsStore.
 */
@Component
public class MLCDocumentsStore extends AbstractDocumentStore {

  /** The patient doc repo. */
  @LazyAutowired
  private PatientDocumentRepository patientDocRepo;

  /** The hvf template fields repo. */
  @LazyAutowired
  private HvfTemplateFieldsRepository hvfTemplateFieldsRepo;

  /** The rich template repo. */
  @LazyAutowired
  private RichTextTemplateRepository richTemplateRepo;

  /** The rtf template repo. */
  @LazyAutowired
  private RtfTemplateRepository rtfTemplateRepo;

  /** The generic doc util. */
  @LazyAutowired
  private GenericDocumentsUtil genericDocUtil;

  /** The registration service. */
  @LazyAutowired
  private RegistrationService registrationService;

  /** The patient hvf repo. */
  @LazyAutowired
  private PatientHvfDocValuesRepository patientHvfRepo;

  /**
   * Instantiates a new MLC documents store.
   */
  public MLCDocumentsStore() {
    super("mlc", true);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.documents.AbstractDocumentStore#create(java.util.Map)
   */
  // The params object is a Map<String, Object>.
  @Transactional(rollbackFor = Exception.class)
  @Override
  public Map<String, Object> create(Map params) throws IOException {
    boolean patientdocvaluesflag = true;
    int docId = 0;
    int valueId = 0;
    String[] tempIdAndFormat = ((String) params.get("mlc_template_id")).split(",");
    Integer templateId = Integer.parseInt(tempIdAndFormat[0]);
    String format = tempIdAndFormat[1];

    String mrNo = (String) params.get("mr_no");
    String visitId = (String) params.get("patient_id");
    InputStream rtfStream = null;
    String contentType = null;

    Integer pheaderTemplateId = null;

    // update doc_id to patient_registration
    docId = patientDocRepo.getNextSequence();

    String docContent = null;
    if (format.equals("doc_hvf_templates")) {
      List<BasicDynaBean> hvfList = hvfTemplateFieldsRepo
          .listAll(null, "template_id", templateId);

      if (!hvfList.isEmpty()) {

        List<BasicDynaBean> insertList = new ArrayList<BasicDynaBean>();

        for (BasicDynaBean bean : hvfList) {
          BasicDynaBean hvfdocvaluesbean = patientHvfRepo.getBean();
          valueId = patientHvfRepo.getNextSequence();

          hvfdocvaluesbean.set("value_id", valueId);
          hvfdocvaluesbean.set("doc_id", docId);
          hvfdocvaluesbean.set("field_id", bean.get("field_id"));
          hvfdocvaluesbean.set("field_value", bean.get("default_value"));

          insertList.add(hvfdocvaluesbean);
        }
        // inserts hvf field values.
        // patientdocvaluesflag = true;
        int[] returnArr = patientHvfRepo.batchInsert(insertList);
        for (int returnValue : returnArr) {
          patientdocvaluesflag = patientdocvaluesflag && (returnValue > 0);
        }
      }

    } else if (format.equals("doc_rich_templates")) {
      BasicDynaBean richTemplateBean = richTemplateRepo.findByKey("template_id", templateId);
      pheaderTemplateId = (Integer) richTemplateBean.get("pheader_template_id");

      Map<String, String> fields = new HashMap<String, String>();
      genericDocUtil.copyStandardFields(fields, false);
      genericDocUtil.copyPatientDetails(fields, mrNo, visitId, false);

      String templateContent = (String) richTemplateBean.get("template_content");
      docContent = CommonHelper
          .replaceTags(templateContent, fields, false);
      try {
        docContent = CommonHelper.addRichTextTitle(docContent,
            (String) richTemplateBean.get("title"),
            (Integer) richTemplateBean.get("pheader_template_id"), visitId, mrNo);
      } catch (TemplateException exc) {
        throw new IOException(exc);
      }

    } else if ("doc_rtf_templates".equalsIgnoreCase(format)) {
      BasicDynaBean rtfbean = rtfTemplateRepo.findByKey("template_id", templateId);

      Map<String, String> fields = new HashMap<String, String>();
      genericDocUtil.copyStandardFields(fields, false);
      genericDocUtil.copyPatientDetails(fields, mrNo, visitId, false);
      ByteArrayOutputStream outstream = new ByteArrayOutputStream();
      String templateContentType = (String) rtfbean.get("content_type");
      boolean isRtf = false;
      if (templateContentType.equals("application/rtf") 
          || templateContentType.equals("text/rtf")) {
        isRtf = true;
      }
      CommonHelper.replaceTags((InputStream) rtfbean
          .get("template_content"), outstream, fields,
          isRtf);
      rtfStream = new ByteArrayInputStream(outstream.toByteArray());
      contentType = (String) rtfbean.get("content_type");
    }

    // inserts into the patient_documents
    BasicDynaBean patientdocbean = patientDocRepo.getBean();
    patientdocbean.set("doc_id", docId);
    patientdocbean.set("template_id", templateId);
    patientdocbean.set("doc_format", format);
    patientdocbean.set("doc_content_text", docContent);
    patientdocbean.set("doc_content_bytea", rtfStream);
    patientdocbean.set("content_type", contentType);
    patientdocbean.set("doc_type", "4");
    patientdocbean.set("pheader_template_id", pheaderTemplateId);

    boolean patientdocflag = patientDocRepo.insert(patientdocbean) > 0;
    boolean postCreate = postCreate(docId, params, null);
    Map<String, Object> resultMap = new HashMap<String, Object>();
    resultMap.put("status", postCreate && patientdocflag && patientdocvaluesflag);
    return resultMap;
  }

  /**
   * (non-Javadoc)
   * 
   * @see com.insta.hms.documents.AbstractDocumentStore#postCreate(int, java.util.Map,
   * java.util.List)
   */
  
  @SuppressWarnings("rawtypes")
  public boolean postCreate(int docid, Map params, List errors) {
    String visitId = (String) params.get("patient_id");
    String userName = (String) params.get("username");

    BasicDynaBean patientRegbean = registrationService.getBean();
    patientRegbean.set("doc_id", docid);
    patientRegbean.set("user_name", userName);

    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put("patient_id", visitId);

    boolean regUpdate = false;
    regUpdate = registrationService.update(patientRegbean, keys) > 0;

    return regUpdate;
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
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.documents.AbstractDocumentStore#searchDocuments(java.util.Map,
   * java.util.Map, java.lang.Boolean)
   */
  @Override
  public List<BasicDynaBean> searchDocuments(Map listingParams, 
      Map extraParams, Boolean specialized)
      throws IOException, ParseException {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.documents.AbstractDocumentStore#copyReplaceableFields(java.util.Map,
   * java.util.Map, boolean)
   */
  @Override
  public void copyReplaceableFields(Map fields, 
      Map keyParams, boolean underscore) {
    // TODO Auto-generated method stub

  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.documents.AbstractDocumentStore#getDocKeyParams(int)
   */
  @Override
  public Map<String, Object> getDocKeyParams(int docid) {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.documents.AbstractDocumentStore#copyDocumentDetails(int, java.util.Map)
   */
  @Override
  public void copyDocumentDetails(int docid, Map to) {
    // TODO Auto-generated method stub

  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.documents.AbstractDocumentStore#postUpdate(int, java.util.Map,
   * java.util.List)
   */
  @Override
  public boolean postUpdate(int docid, Map requestParams, List errors) throws IOException {
    // TODO Auto-generated method stub
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.documents.AbstractDocumentStore#postDelete(java.lang.Object,
   * java.lang.String)
   */
  @Override
  public boolean postDelete(Object docId, String format) throws IOException {
    // TODO Auto-generated method stub
    return false;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.documents.AbstractDocumentStore#getKeys()
   */
  @Override
  public Map<String, Object> getKeys() {
    // TODO Auto-generated method stub
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.documents.AbstractDocumentStore#getCenterId(java.util.Map)
   */
  @Override
  public int getCenterId(Map requestParams) {
    // TODO Auto-generated method stub
    return 0;
  }

}

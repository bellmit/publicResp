package com.insta.hms.documents;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.core.patient.PatientDetailsRepository;
import com.insta.hms.genericdocuments.CommonHelper;
import com.lowagie.text.DocumentException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.sql.Date;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class RegistrationDocumentStore.
 */
@Component
public class RegistrationDocumentStore extends AbstractDocumentStore {

  /** The rch text temp repo. */
  @LazyAutowired
  private RichTextTemplateRepository rchTextTempRepo;

  /** The pdftemplaterepo. */
  @LazyAutowired
  private PdfFormTemplateRepository pdftemplaterepo;

  /** The generic documents util. */
  @LazyAutowired
  private GenericDocumentsUtil genericDocumentsUtil;

  /** The regcardrepo. */
  @LazyAutowired
  private PatientRegCardsRepository regcardrepo;

  /**
   * Instantiates a new registration document store.
   */
  public RegistrationDocumentStore() {
    super("reg", true);
    // TODO Auto-generated constructor stub
  }

  /**
   * The Enum keys.
   */
  public static enum KEYS {
    /** The patient id. */
    patient_id
  }

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
   * @see com.insta.hms.documents.AbstractDocumentStore#postCreate(int, java.util.Map,
   * java.util.List)
   */
  @Override
  public boolean postCreate(int docid, Map requestParams, List errors) throws IOException {
    BasicDynaBean regcarddocbean = regcardrepo.getBean();
    ConversionUtils.copyToDynaBean(requestParams, regcarddocbean, errors);
    if (errors.isEmpty()) {
      regcarddocbean.set("doc_id", docid);
      return 0 < regcardrepo.insert(regcarddocbean);

    } else {
      requestParams.put("error", "Incorrectly formatted details supplied..");
    }
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

    BasicDynaBean regcarddocbean = regcardrepo.getBean();
    ConversionUtils.copyToDynaBean(requestParams, regcarddocbean, errors);

    if (errors.isEmpty()) {
      regcarddocbean.set("doc_id", docid);
      // updates the patient general document details like doc_date,
      // doc_name, user etc..
      if (regcardrepo.update(regcarddocbean, keys) > 0) {
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
    return true;
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
    return null;
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.documents.AbstractDocumentStore#searchDocuments(java.util.Map,
   * java.util.Map, java.lang.Boolean)
   */
  @Override
  public List<BasicDynaBean> searchDocuments(Map listingParams, Map extraParams,
      Boolean specialized) throws IOException {

    return PatientDetailsRepository.getRegistrationDocs(listingParams, extraParams, specialized);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.documents.AbstractDocumentStore#copyReplaceableFields(java.util.Map,
   * java.util.Map, boolean)
   */
  @Override
  public void copyReplaceableFields(Map to, Map keyParams, boolean underscore) {
    if (to == null) {
      return;
    }
    String patientId = (String) keyParams.get("patient_id");
    genericDocumentsUtil.copyPatientDetails(to, null, patientId, underscore);
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.documents.AbstractDocumentStore#copyDocumentDetails(int, java.util.Map)
   */
  @Override
  public void copyDocumentDetails(int docid, Map to) {
    if (to == null) {
      return;
    }

    BasicDynaBean documentdetailsbean = regcardrepo.findByKey("doc_id", docid);
    // copies document details like doc date, username, and visit id.
    to.putAll(documentdetailsbean.getMap());
  }

  /*
   * (non-Javadoc)
   * 
   * @see com.insta.hms.documents.AbstractDocumentStore#getDocKeyParams(int)
   */
  @Override
  public Map<String, Object> getDocKeyParams(int docid) {
    BasicDynaBean documentdetailsbean = regcardrepo.findByKey("doc_id", docid);
    HashMap hashMap = new HashMap();
    hashMap.put("patient_id", documentdetailsbean.get("patient_id"));
    return hashMap;
  }

  /**
   * Auto generate reg documents.
   *
   * @param patientId
   *          the patient id
   * @param mrNo
   *          the mr no
   * @param visitType
   *          the visit type
   * @param user
   *          the user
   * @return true, if successful
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws DocumentException
   *           the document exception
   */
  public boolean autoGenerateRegDocuments(String patientId, String mrNo, String visitType,
      String user) throws IOException, DocumentException {

    Map<String, String> fields = new HashMap<>();

    String keyCol = visitType.equals("opreg") ? "auto_gen_op" : "auto_gen_ip";
    Map<String, Object> filterMap = new HashMap<>();
    filterMap.put(keyCol, "Y");
    filterMap.put("status", "A");

    List<BasicDynaBean> autoGenPDFDocList = pdftemplaterepo
        .listAll(Arrays.asList(new String[] { "template_id", "template_name" }), filterMap, null);
    List<BasicDynaBean> autoGenRichTxtDocList = rchTextTempRepo.listAll(Arrays.asList(
        new String[] { "template_id", "template_name", "template_content", "pheader_template_id" }),
        filterMap, null);

    Map<String, List<BasicDynaBean>> templatesMap = new HashMap<>();
    templatesMap.put("doc_pdf_form_templates", autoGenPDFDocList);
    templatesMap.put("doc_rich_templates", autoGenRichTxtDocList);

    for (Map.Entry<String, List<BasicDynaBean>> autoGenDocEntry : templatesMap.entrySet()) {

      String docFormat = autoGenDocEntry.getKey();
      List<BasicDynaBean> autoGenDocList = autoGenDocEntry.getValue();
      for (BasicDynaBean tempBean : autoGenDocList) {

        int templateId = (Integer) tempBean.get("template_id");

        Map<String, Object[]> resMap = new HashMap<>();
        String docContent = "";

        if (!docFormat.equals("doc_pdf_form_templates")) {
          genericDocumentsUtil.copyPatientDetails(fields, null, patientId, false);
          docContent = (String) tempBean.get("template_content");
          docContent = CommonHelper.replaceTags(docContent, fields, true);
          resMap.put("pheader_template_id",
              new Object[] { (Integer) tempBean.get("pheader_template_id") });
        }

        resMap.put("format", new String[] { docFormat });
        resMap.put("template_id", new Object[] { templateId });
        resMap.put("doc_type", new String[] { "SYS_RG" });
        resMap.put("doc_content_text", new Object[] { docContent });
        resMap.put("mr_no", new String[] { mrNo });
        resMap.put("patient_id", new String[] { patientId });
        String docName = (String) tempBean.get("template_name");
        resMap.put("doc_name", new String[] { docName });
        Date docDate = (new java.sql.Date((new java.util.Date()).getTime()));
        resMap.put("doc_date", new Object[] { docDate });
        resMap.put("username", new Object[] { user });

        boolean success = (Boolean) create(resMap).get("status");
        if (!success) {
          return false;
        }
      }
    }

    return true;
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

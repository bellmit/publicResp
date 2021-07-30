package com.insta.hms.core.patient.registration;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.annotations.LazyAutowired;
import com.insta.hms.documents.PatientDocumentService;
import com.insta.hms.documents.PatientHvfDocValuesServices;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class MLCDocumentsService.
 */
@Service
public class MLCDocumentsService {

  /** The registration service. */
  @LazyAutowired
  private RegistrationService registrationService;

  /** The patient document service. */
  @LazyAutowired
  private PatientDocumentService patientDocumentService;

  /** The hvf doc service. */
  @LazyAutowired
  PatientHvfDocValuesServices hvfDocService;
  // TODO: Chetan Change this to corresponding repository. ------------

  /** The Constant GET_TEMPLATE_MLC_PAT_LIST. */
  private static final String GET_TEMPLATE_MLC_PAT_LIST = 
      " select pr.mr_no, pr.patient_id, pd.doc_id, pd.doc_format,"
      + " pd.template_id,dv.doc_type, dv.specialized "
      + " from doc_all_templates_view dv "
      + " join patient_documents pd on pd.template_id = dv.template_id"
      + " and dv.doc_format = pd.doc_format "
      + " join patient_registration  pr on (pr.doc_id = pd.doc_id) "
      + " where pr.mlc_status = 'Y' AND pr.patient_id=?";

  /**
   * List patient MLC template.
   *
   * @param patientId
   *          the patient id
   * @return the list
   */
  public List<BasicDynaBean> listPatientMLCTemplate(String patientId) {
    return DatabaseHelper.queryToDynaList(GET_TEMPLATE_MLC_PAT_LIST, patientId);
  }

  /** The Constant ALL_TEMPLATE_FIELDS. */
  // this into genericdocumentstemplete repository
  public static final String ALL_TEMPLATE_FIELDS = 
      "select doc_type_id, doc_type_name, dept_name, foo.template_id, format, "
      + "template_name, status ";

  /** The Constant ALL_TEMPLATE_TABLES. */
  public static final String ALL_TEMPLATE_TABLES = " FROM (select dt.doc_type_id ,"
      + " dt.doc_type_name, "
      + " hvf.template_id,'doc_hvf_templates' as format, hvf.template_name,"
      + " hvf.status, hvf.dept_name, hvf.specialized "
      + " FROM doc_type dt JOIN doc_hvf_templates as hvf ON dt.doc_type_id=hvf.doc_type "
      + " union "
      + " select dt.doc_type_id, dt.doc_type_name, rich.template_id,'doc_rich_templates'"
      + " as format, rich.template_name,"
      + " rich.status, rich.dept_name, rich.specialized  FROM doc_type dt JOIN doc_rich_templates"
      + " as rich ON dt.doc_type_id=rich.doc_type "
      + " union "
      + " select dt.doc_type_id, dt.doc_type_name, pdf.template_id,'doc_pdf_form_templates'"
      + " as format, pdf.template_name, "
      + " pdf.status, pdf.dept_name, pdf.specialized FROM doc_type dt JOIN  doc_pdf_form_templates"
      + " as pdf ON dt.doc_type_id=pdf.doc_type "
      + " union "
      + " select dt.doc_type_id, dt.doc_type_name, rtf.template_id ,'doc_rtf_templates'"
      + " as format, rtf.template_name, "
      + " rtf.status,rtf.dept_name, rtf.specialized FROM doc_type dt JOIN doc_rtf_templates"
      + " as rtf on dt.doc_type_id=rtf.doc_type) as foo";

  /** The Constant GET_TEMPLATES. */
  public static final String GET_TEMPLATES = ALL_TEMPLATE_FIELDS + ALL_TEMPLATE_TABLES
      + " WHERE  doc_type_id = ?  AND specialized=? AND status = ? ";

  /**
   * Gets the templates.
   *
   * @param specialized
   *          the specialized
   * @param specializedDocType
   *          the specialized doc type
   * @param status
   *          the status
   * @return the templates
   */
  public List<BasicDynaBean> getTemplates(Boolean specialized, String specializedDocType,
      String status) {
    return DatabaseHelper.queryToDynaList(GET_TEMPLATES, new Object[] { specializedDocType,
        specialized, status });
  }

  /**
   * Delete MLC details.
   *
   * @param visitId
   *          the visit id
   * @param userName
   *          the user name
   * @return true, if successful
   */
  // ----------------
  public boolean deleteMLCDetails(String visitId, String userName) {
    BasicDynaBean visitBean = registrationService.findByKey(visitId);
    Integer mlcDocId = null;
    if (visitBean != null) {
      mlcDocId = (Integer) visitBean.get("doc_id");
    }
    BasicDynaBean docFormatBean = patientDocumentService.getDocFormatFromDocId(mlcDocId);
    String docFormat = null;
    if (docFormatBean != null) {
      docFormat = (String) docFormatBean.get("doc_format");
    }
    BasicDynaBean patientBean = registrationService.getBean();
    patientBean.set("mlc_status", "N");
    patientBean.set("doc_id", null);
    patientBean.set("user_name", userName);
    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put("patient_id", visitId);
    boolean deleteSuccess = true;
    deleteSuccess = registrationService.update(patientBean, keys) > 0;

    if (deleteSuccess) {
      deleteSuccess = patientDocumentService.delete(mlcDocId) > 0;
    }
    if (deleteSuccess && ("doc_hvf_templates".equalsIgnoreCase(docFormat))) {
      deleteSuccess = hvfDocService.delete("doc_id", mlcDocId) > 0;
    }

    return deleteSuccess;
  }

}

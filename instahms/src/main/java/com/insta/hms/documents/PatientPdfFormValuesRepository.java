package com.insta.hms.documents;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class PatientPdfFormValuesRepository.
 */
@Repository
public class PatientPdfFormValuesRepository extends GenericRepository {

  /**
   * Instantiates a new patient pdf form values repository.
   */
  public PatientPdfFormValuesRepository() {
    super("patient_pdf_form_doc_values");
    // TODO Auto-generated constructor stub
  }

  /**
   * Insert PDF form field values.
   *
   * @param params the params
   * @param docId the doc id
   * @param username the username
   * @return true, if successful
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public boolean insertPDFFormFieldValues(Map params, Object docId, String username)
      throws IOException {

    Map allFields = ConversionUtils.flatten(params);
    allFields.remove("mr_no");
    allFields.remove("patient_id");
    allFields.remove("pat_name");
    allFields.remove("dept_name");
    allFields.remove("method");
    allFields.remove("doc_date");
    allFields.remove("template_id");
    allFields.remove("doc_name");
    allFields.remove("format");
    allFields.remove("doc_id");

    BasicDynaBean insertBean = super.getBean();
    List<BasicDynaBean> beans = new ArrayList<>();
    for (Map.Entry e : (Collection<Map.Entry>) allFields.entrySet()) {

      if (((String) e.getKey()).startsWith("_")) {
        // _ names are supposed to be temp variables and skipped.
        continue;
      }
      insertBean.set("doc_id", docId);
      insertBean.set("field_name", e.getKey());
      insertBean.set("field_value", e.getValue());
      insertBean.set("username", username);

      beans.add(insertBean);
    }
    int[] results = batchInsert(beans);
    return true;
  }

  /** The Constant UPDATE_PDF_FORM_FIELD_VALUES. */
  private static final String UPDATE_PDF_FORM_FIELD_VALUES = "update patient_pdf_form_doc_values "
      + "set field_value=? where doc_id=? and field_name=?";

  /** The Constant GET_PDF_FORM_FIELD_VALUES. */
  private static final String GET_PDF_FORM_FIELD_VALUES = "select field_name from"
      + " patient_pdf_form_doc_values where doc_id = ?";

  /**
   * Update PDF form field values.
   *
   * @param params the params
   * @param docId the doc id
   * @return true, if successful
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public boolean updatePDFFormFieldValues(Map params, Object docId) throws IOException {

    Map allFields = ConversionUtils.flatten(params);
    allFields.remove("mr_no");
    allFields.remove("patient_id");
    allFields.remove("pat_name");
    allFields.remove("dept_name");
    allFields.remove("method");
    allFields.remove("doc_date");
    allFields.remove("template_id");
    allFields.remove("doc_name");
    allFields.remove("format");
    allFields.remove("doc_id");

    Integer templateId = (params.get("template_id") != null
        && !((String[]) params.get("template_id"))[0].equals(""))
            ? Integer.parseInt(((String[]) params.get("template_id"))[0])
            : null;
    List<BasicDynaBean> imageTemplateFieldvalues = PatientPdfDocImagesRepository
        .getPDFTemplateImageValues(templateId);
    String userName = params.get("user_name") == null ? ""
        : ((String[]) params.get("user_name"))[0];
    userName = userName == null ? "" : userName;

    if (imageTemplateFieldvalues != null && imageTemplateFieldvalues.size() > 0) {
      for (int i = 0; i < imageTemplateFieldvalues.size(); i++) {
        allFields.remove("field_id" + "_" + i);
        allFields.remove("field_input" + "_" + i);
        allFields.remove("device_ip" + "_" + i);
        allFields.remove("device_info" + "_" + i);
        allFields.remove("fieldImgText" + "_" + i);
      }
    }

    boolean exists = false;
    Map<String, Boolean> savedFields = new HashMap<>();
    BasicDynaBean insertBean = super.getBean();
    List<BasicDynaBean> insertList = new ArrayList<>();
    BasicDynaBean updateBeaan = super.getBean();
    List<BasicDynaBean> updateList = new ArrayList<>();
    Map<String, Object> updateKeys = new HashMap<>();
    List<BasicDynaBean> fieldList = DatabaseHelper.queryToDynaList(GET_PDF_FORM_FIELD_VALUES,
        new Object[] { docId });
    if (fieldList.size() > 0) {
      for (int i = 0; i < fieldList.size(); i++) {
        BasicDynaBean bean = fieldList.get(i);
        savedFields.put((String) bean.get("field_name"), false);
      }
      for (Map.Entry e : (Collection<Map.Entry>) allFields.entrySet()) {
        if (((String) e.getKey()).startsWith("_")) {
          // _ names are supposed to be temp variables and skipped.
          continue;
        }
        for (int i = 0; i < fieldList.size(); i++) {
          BasicDynaBean bean = fieldList.get(i);

          if (bean.get("field_name").equals(e.getKey())) {
            updateBeaan.set("field_value", e.getValue());
            updateKeys.put("doc_id", docId);
            updateKeys.put("field_name", e.getKey());
            updateList.add(updateBeaan);
            savedFields.put((String) bean.get("field_name"), true); // updated
            // fields
            exists = true;
            break;
          }
        }
        if (!exists) {
          insertBean.set("doc_id", docId);
          insertBean.set("field_name", e.getKey());
          insertBean.set("field_value", e.getValue());
          insertBean.set("username", userName);
          insertList.add(insertBean);
        }
        exists = false;
      }
      // updating checkbox fields with no, which were saved earlier
      // transaction, and want to uncheck the values now.
      for (Map.Entry<String, Boolean> e : savedFields.entrySet()) {
        if (!e.getValue()) {
          updateBeaan.set("field_value", "");
          updateKeys.put("doc_id", docId);
          updateKeys.put("field_name", e.getKey());
          updateList.add(updateBeaan);
        }
      }

    } else {
      for (Map.Entry e : (Collection<Map.Entry>) allFields.entrySet()) {
        if (((String) e.getKey()).startsWith("_")) {
          // _ names are supposed to be temp variables and skipped.
          continue;
        }
        insertBean.set("doc_id", docId);
        insertBean.set("field_name", e.getKey());
        insertBean.set("field_value", e.getValue());
        insertBean.set("username", userName);
        insertList.add(insertBean);
      }
    }
    int[] updateResults = batchUpdate(updateList, updateKeys);
    int[] insertResults = batchInsert(insertList);

    return true;
  }

}

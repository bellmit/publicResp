package com.insta.hms.documents;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class PatientPdfDocImagesRepository.
 */
@Repository
public class PatientPdfDocImagesRepository extends GenericRepository {

  /**
   * Instantiates a new patient pdf doc images repository.
   */
  public PatientPdfDocImagesRepository() {
    super("patient_pdf_doc_images");
    // TODO Auto-generated constructor stub
  }

  /** The Constant PDF_TEMPLATE_IMAGE_VALUES. */
  private static final String PDF_TEMPLATE_IMAGE_VALUES = " SELECT dht.template_name, "
      + "dhtf.field_name, dhtf.display_name, dhtf.field_input, NULL AS field_image_content_type, "
      + " dhtf.field_id, NULL AS doc_image_id, NULL AS device_ip, NULL AS device_info, "
      + " NULL AS capture_time " + " FROM doc_pdf_template_ext_fields dhtf "
      + " LEFT OUTER JOIN doc_pdf_form_templates dht on dht.template_id=dhtf.template_id "
      + " WHERE dht.template_id=? ";

  /**
   * Gets the PDF template image values.
   *
   * @param templateId the template id
   * @return the PDF template image values
   */
  public static List<BasicDynaBean> getPDFTemplateImageValues(int templateId) {
    return DatabaseHelper.queryToDynaList(PDF_TEMPLATE_IMAGE_VALUES, new Object[] { templateId });
  }

  /** The Constant PDF_IMAGE_VALUES. */
  private static final String PDF_IMAGE_VALUES = " SELECT dht.template_name,dhtf.field_name, "
      + "dhtf.display_name, dhtf.field_input, pdhi.field_image_content_type, "
      + " dhtf.field_id, pdhi.doc_image_id, pdhi.device_ip, pdhi.device_info,"
      + " pdhi.capture_time " + " FROM patient_pdf_doc_images pdhi"
      + " LEFT OUTER JOIN patient_documents phd on phd.doc_id = pdhi.doc_id"
      + " LEFT OUTER JOIN doc_pdf_form_templates dht on dht.template_id=phd.template_id"
      + " LEFT OUTER JOIN doc_pdf_template_ext_fields dhtf on dhtf.field_id=pdhi.field_id"
      + " WHERE phd.doc_id=? ";

  /**
   * Gets the PDF doc image values.
   *
   * @param docId the doc id
   * @return the PDF doc image values
   */
  public static List<BasicDynaBean> getPDFDocImageValues(int docId) {
    return DatabaseHelper.queryToDynaList(PDF_IMAGE_VALUES, new Object[] { docId });
  }

  /**
   * Update PDF doc image values.
   *
   * @param list the list
   * @param docId the doc id
   * @return true, if successful
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public boolean updatePDFDocImageValues(List<Map<String, BasicDynaBean>> list, Object docId)
      throws IOException {
    int count = 0;
    Map<String, Object> keys = new HashMap<>();
    keys.put("doc_id", docId);
    for (Map<String, BasicDynaBean> map : list) {
      for (Map.Entry<String, BasicDynaBean> entry : (Collection<Map
          .Entry<String, BasicDynaBean>>) map.entrySet()) {
        BasicDynaBean pdfdocimagevaluesbean = entry.getValue();
        if (entry.getKey().equals("insert")) {
          pdfdocimagevaluesbean.set("doc_id", docId);
          if (insert(pdfdocimagevaluesbean) > 0) {
            count++;
          }

        } else if (entry.getKey().equals("update")) {
          keys.put("field_id", pdfdocimagevaluesbean.get("field_id"));
          count += update(pdfdocimagevaluesbean, keys);

        }
      }
    }
    return list.size() == count;
  }

}

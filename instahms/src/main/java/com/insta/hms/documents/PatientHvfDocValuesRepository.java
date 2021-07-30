package com.insta.hms.documents;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;
import com.insta.hms.common.annotations.LazyAutowired;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class PatientHvfDocValuesRepository.
 */
@Repository
public class PatientHvfDocValuesRepository extends GenericRepository {

  /**
   * Instantiates a new patient hvf doc values repository.
   */
  public PatientHvfDocValuesRepository() {
    super("patient_hvf_doc_values");
    // TODO Auto-generated constructor stub
  }

  /** The hvfdocimagevaluesrepo. */
  @LazyAutowired
  private PatientHvfDocImagesRepository hvfdocimagevaluesrepo;

  /**
   * Insert all.
   *
   * @param records the records
   * @return true, if successful
   */
  public boolean insertAll(List<BasicDynaBean> records) {
    int[] result = batchInsert(records);
    return result.length == records.size();
  }

  /**
   * Update HVF doc values.
   *
   * @param list the list
   * @param docId the doc id
   * @return true, if successful
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public boolean updateHVFDocValues(List<Map<String, BasicDynaBean>> list, Object docId)
      throws IOException {
    int count = 0;
    Map<String, Object> keys = new HashMap<>();
    keys.put("doc_id", docId);
    for (Map<String, BasicDynaBean> map : list) {
      for (Map.Entry<String, BasicDynaBean> entry : (Collection<Map
          .Entry<String, BasicDynaBean>>) map
          .entrySet()) {
        BasicDynaBean hvfdocvaluesbean = entry.getValue();
        if (entry.getKey().equals("insert")) {
          hvfdocvaluesbean.set("doc_id", docId);
          hvfdocvaluesbean.set("value_id", getNextSequence());
          if (insert(hvfdocvaluesbean) > 0) {
            count++;
          }

        } else if (entry.getKey().equals("update")) {
          keys.put("value_id", hvfdocvaluesbean.get("value_id"));
          count += update(hvfdocvaluesbean, keys);

        } else if (entry.getKey().equals("delete")) {
          if (delete("value_id", hvfdocvaluesbean.get("value_id")) > 0) {
            count++;
          }

        } else if (entry.getKey().equals("deleteField")) {
          count++;
          LinkedHashMap fkeys = new LinkedHashMap();
          fkeys.put("field_id", hvfdocvaluesbean.get("field_id"));
          fkeys.put("doc_id", hvfdocvaluesbean.get("doc_id"));
          delete(fkeys);
        }
      }
    }
    return list.size() == count;
  }

  /**
   * Update HVF doc image values.
   *
   * @param list the list
   * @param docId the doc id
   * @return true, if successful
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public boolean updateHVFDocImageValues(List<Map<String, BasicDynaBean>> list, Object docId)
      throws IOException {
    int count = 0;

    Map<String, Object> keys = new HashMap<>();
    keys.put("doc_id", docId);
    for (Map<String, BasicDynaBean> map : list) {
      for (Map.Entry<String, BasicDynaBean> entry : (Collection<Map.Entry<String,
          BasicDynaBean>>) map
          .entrySet()) {
        BasicDynaBean hvfdocimagevaluesbean = entry.getValue();
        if (entry.getKey().equals("insert")) {
          hvfdocimagevaluesbean.set("doc_id", docId);
          if (hvfdocimagevaluesrepo.insert(hvfdocimagevaluesbean) > 0) {
            count++;
          }

        } else if (entry.getKey().equals("update")) {
          keys.put("field_id", hvfdocimagevaluesbean.get("field_id"));
          count += hvfdocimagevaluesrepo.update(hvfdocimagevaluesbean, keys);

        } else if (entry.getKey().equals("deleteField")) {
          count++;
          LinkedHashMap fkeys = new LinkedHashMap();
          fkeys.put("field_id", hvfdocimagevaluesbean.get("field_id"));
          fkeys.put("doc_id", hvfdocimagevaluesbean.get("doc_id"));
          hvfdocimagevaluesrepo.delete(fkeys);
        }
      }
    }
    return list.size() == count;
  }

  /** The Constant HVF_REPORT_IMAGES_QUERY. */
  private static final String HVF_REPORT_IMAGES_QUERY = " SELECT dht.template_name, dht.title,"
      + " dhtf.field_name, pdhi.field_image_content_type,"
      + " dhtf.field_id, pdhi.doc_image_id, pdhi.device_ip, pdhi.device_info, "
      + " pdhi.capture_time, dht.print_template_name " + " FROM patient_documents phd "
      + " LEFT OUTER JOIN patient_hvf_doc_images pdhi on (phd.doc_id=pdhi.doc_id) "
      + " LEFT OUTER JOIN doc_hvf_templates dht on dht.template_id=phd.template_id "
      + " LEFT OUTER JOIN doc_hvf_template_fields dhtf on dhtf.field_id=pdhi.field_id "
      + " WHERE phd.doc_id=? ";

  /**
   * Gets the HVF doc image values.
   *
   * @param docId the doc id
   * @param allFields the all fields
   * @return the HVF doc image values
   */
  public static List<BasicDynaBean> getHVFDocImageValues(int docId, boolean allFields) {
    String fieldsValues = HVF_REPORT_IMAGES_QUERY;
    if (!allFields) {
      fieldsValues = fieldsValues + " AND print_column='Y' ";
    }
    fieldsValues += "ORDER BY dhtf.display_order";
    return DatabaseHelper.queryToDynaList(fieldsValues, new Object[] { docId });
  }

  /** The Constant HVF_REPORT_QUERY. */
  private static final String HVF_REPORT_QUERY = " SELECT dht.template_name, dht.title, "
      + "dhtf.field_name, dhtf.field_id, phdv.field_value,dht.print_template_name "
      + " FROM patient_documents phd "
      + " LEFT OUTER JOIN patient_hvf_doc_values phdv on phd.doc_id=phdv.doc_id "
      + " LEFT OUTER JOIN doc_hvf_templates dht on dht.template_id=phd.template_id "
      + " LEFT OUTER JOIN doc_hvf_template_fields  dhtf on dhtf.field_id=phdv.field_id "
      + " WHERE phd.doc_id=? and phdv.field_value is not null and phdv.field_value!=''";

  /** The Constant PRINTABLE_COLUMNS. */
  private static final String PRINTABLE_COLUMNS = " AND print_column='Y'"
      + " AND coalesce(field_value, '')!=''";

  /**
   * Gets the HVF doc values.
   *
   * @param docId the doc id
   * @param allFields the all fields
   * @return the HVF doc values
   */
  public static List<BasicDynaBean> getHVFDocValues(int docId, boolean allFields) {
    String fieldsValues = HVF_REPORT_QUERY;
    if (!allFields) {
      fieldsValues = fieldsValues + PRINTABLE_COLUMNS;
    }
    fieldsValues += " ORDER BY dhtf.display_order";
    return DatabaseHelper.queryToDynaList(fieldsValues, new Object[] { docId });
  }

  /** The Constant GET_PRINT_TEMPLATE_NAME. */
  private static final String GET_PRINT_TEMPLATE_NAME = "SELECT dht.print_template_name "
      + " FROM patient_documents pd "
      + " JOIN doc_hvf_templates dht ON dht.template_id = pd.template_id WHERE pd.doc_id = ?";

  /**
   * Gets the prints the template name.
   *
   * @param docId the doc id
   * @return the prints the template name
   */
  public static String getPrintTemplateName(int docId) {
    String templateName = GET_PRINT_TEMPLATE_NAME;
    return DatabaseHelper.getString(GET_PRINT_TEMPLATE_NAME, docId);
  }

  /** The Constant GET_HVF_PRINT_TEMPLATE_NAME. */
  private static final String GET_HVF_PRINT_TEMPLATE_NAME = "SELECT dht.template_name "
      + " FROM patient_documents pd "
      + " JOIN doc_hvf_templates dht ON dht.template_id = pd.template_id WHERE pd.doc_id = ?";

  /** The Constant GET_RICH_TEXT_PRINT_TEMPLATE_NAME. */
  private static final String GET_RICH_TEXT_PRINT_TEMPLATE_NAME = "SELECT drt.template_name "
      + " FROM patient_documents pd "
      + " JOIN doc_rich_templates drt ON drt.template_id = pd.template_id "
      + " WHERE pd.doc_id = ?";

  /**
   * Gets the template name.
   *
   * @param docId the doc id
   * @param documentFormat the document format
   * @return the template name
   */
  public static String getTemplateName(int docId, String documentFormat) {
    String templateName = null;
    if (documentFormat != null && documentFormat.equals("doc_hvf_templates")) {
      templateName = GET_HVF_PRINT_TEMPLATE_NAME;
    } else {
      templateName = GET_RICH_TEXT_PRINT_TEMPLATE_NAME;
    }
    return DatabaseHelper.getString(templateName, docId);
  }

}

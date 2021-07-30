package com.insta.hms.genericdocuments;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;
import org.apache.commons.beanutils.BasicDynaBean;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class PatientHVFDocValuesDAO.
 *
 * @author krishna.t
 */
public class PatientHVFDocValuesDAO extends GenericDAO {

  /**
   * Instantiates a new patient HVF doc values DAO.
   */
  public PatientHVFDocValuesDAO() {
    super("patient_hvf_doc_values");
  }

  /**
   * Update HVF doc values.
   *
   * @param con the con
   * @param list the list
   * @param docId the doc id
   * @return true, if successful
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public boolean updateHVFDocValues(Connection con, List<Map<String, BasicDynaBean>> list,
      Object docId) throws SQLException, IOException {
    int count = 0;
    Map<String, Object> keys = new HashMap<>();
    keys.put("doc_id", docId);
    for (Map<String, BasicDynaBean> map : list) {
      for (Map.Entry<String, BasicDynaBean> entry 
          : (Collection<Map.Entry<String, BasicDynaBean>>) map
          .entrySet()) {
        BasicDynaBean hvfdocvaluesbean = entry.getValue();
        if (entry.getKey().equals("insert")) {
          hvfdocvaluesbean.set("doc_id", docId);
          hvfdocvaluesbean.set("value_id", getNextSequence());
          if (insert(con, hvfdocvaluesbean)) {
            count++;
          }
        } else if (entry.getKey().equals("update")) {
          keys.put("value_id", hvfdocvaluesbean.get("value_id"));
          count += update(con, hvfdocvaluesbean.getMap(), keys);

        } else if (entry.getKey().equals("delete")) {
          if (delete(con, "value_id", hvfdocvaluesbean.get("value_id"))) {
            count++;
          }
        } else if (entry.getKey().equals("deleteField")) {
          count++;
          LinkedHashMap<String, Object> fkeys = new LinkedHashMap();
          fkeys.put("field_id", hvfdocvaluesbean.get("field_id"));
          fkeys.put("doc_id", hvfdocvaluesbean.get("doc_id"));
          delete(con, fkeys);
        }
      }
    }
    return list.size() == count;
  }

  /**
   * Update HVF doc image values.
   *
   * @param con the con
   * @param list the list
   * @param docId the doc id
   * @return true, if successful
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public boolean updateHVFDocImageValues(Connection con, List<Map<String, BasicDynaBean>> list,
      Object docId) throws SQLException, IOException {
    int count = 0;
    GenericDAO hvfdocimagevaluesdao = new GenericDAO("patient_hvf_doc_images");
    Map<String, Object> keys = new HashMap<>();
    keys.put("doc_id", docId);
    for (Map<String, BasicDynaBean> map : list) {
      for (Map.Entry<String, BasicDynaBean> entry 
          : (Collection<Map.Entry<String, BasicDynaBean>>) map
          .entrySet()) {
        BasicDynaBean hvfdocimagevaluesbean = entry.getValue();
        if (entry.getKey().equals("insert")) {
          hvfdocimagevaluesbean.set("doc_id", docId);
          if (hvfdocimagevaluesdao.insert(con, hvfdocimagevaluesbean)) {
            count++;
          }
        } else if (entry.getKey().equals("update")) {
          keys.put("field_id", hvfdocimagevaluesbean.get("field_id"));
          count += hvfdocimagevaluesdao.update(con, hvfdocimagevaluesbean.getMap(), keys);

        } else if (entry.getKey().equals("deleteField")) {
          count++;
          LinkedHashMap<String, Object> fkeys = new LinkedHashMap();
          fkeys.put("field_id", hvfdocimagevaluesbean.get("field_id"));
          fkeys.put("doc_id", hvfdocimagevaluesbean.get("doc_id"));
          hvfdocimagevaluesdao.delete(con, fkeys);
        }
      }
    }
    return list.size() == count;
  }

  /** The Constant HVF_REPORT_QUERY. */
  private static final String HVF_REPORT_QUERY =
      " SELECT dht.template_name, dht.title, dhtf.field_name, dhtf.field_id,"
          + " phdv.field_value,dht.print_template_name " + " FROM patient_documents phd "
          + " LEFT OUTER JOIN patient_hvf_doc_values phdv on phd.doc_id=phdv.doc_id "
          + " LEFT OUTER JOIN doc_hvf_templates dht on dht.template_id=phd.template_id "
          + " LEFT OUTER JOIN doc_hvf_template_fields  dhtf on dhtf.field_id=phdv.field_id "
          + " WHERE phd.doc_id = ? and phdv.field_value is not null and phdv.field_value!=''";

  /** The Constant GET_PRINT_TEMPLATE_NAME. */
  private static final String GET_PRINT_TEMPLATE_NAME = "SELECT dht.print_template_name "
      + " FROM patient_documents pd JOIN doc_hvf_templates dht "
      + " ON dht.template_id = pd.template_id WHERE pd.doc_id = ?";

  /** The Constant PRINTABLE_COLUMNS. */
  private static final String PRINTABLE_COLUMNS =
      " AND print_column='Y' AND coalesce(field_value, '')!=''";

  /**
   * Gets the HVF doc values.
   *
   * @param docId the doc id
   * @param allFields the all fields
   * @return the HVF doc values
   * @throws SQLException the SQL exception
   */
  @SuppressWarnings("unchecked")
  public static List<BasicDynaBean> getHVFDocValues(int docId, boolean allFields)
      throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      String fieldsValues = HVF_REPORT_QUERY;
      if (!allFields) {
        fieldsValues = fieldsValues + PRINTABLE_COLUMNS;
      }
      fieldsValues += " ORDER BY dhtf.display_order";
      ps = con.prepareStatement(fieldsValues);
      int index = 1; // Sonarqube lists a bug if 1 is used instead of a variable
      ps.setInt(index, docId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant HVF_REPORT_IMAGES_QUERY. */
  private static final String HVF_REPORT_IMAGES_QUERY =
      " SELECT dht.template_name, dht.title, dhtf.field_name, pdhi.field_image_content_type,"
          + " dhtf.field_id, pdhi.doc_image_id, pdhi.device_ip, pdhi.device_info, "
          + " pdhi.capture_time, dht.print_template_name " + " FROM patient_documents phd "
          + " LEFT OUTER JOIN patient_hvf_doc_images pdhi on (phd.doc_id=pdhi.doc_id) "
          + " LEFT OUTER JOIN doc_hvf_templates dht on dht.template_id=phd.template_id "
          + " LEFT OUTER JOIN doc_hvf_template_fields dhtf on dhtf.field_id=pdhi.field_id "
          + " WHERE phd.doc_id = ? ";

  /**
   * Gets the HVF doc image values.
   *
   * @param docId the doc id
   * @param allFields the all fields
   * @return the HVF doc image values
   * @throws SQLException the SQL exception
   */
  @SuppressWarnings("unchecked")
  public static List<BasicDynaBean> getHVFDocImageValues(int docId, boolean allFields)
      throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      String fieldsValues = HVF_REPORT_IMAGES_QUERY;
      if (!allFields) {
        fieldsValues = fieldsValues + " AND print_column='Y' ";
      }
      fieldsValues += "ORDER BY dhtf.display_order";
      ps = con.prepareStatement(fieldsValues);
      int index = 1; // Sonarqube lists a bug if 1 is used instead of a variable
      ps.setInt(index, docId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Gets the prints the template name.
   *
   * @param docId the doc id
   * @return the prints the template name
   * @throws SQLException the SQL exception
   */
  public static String getPrintTemplateName(int docId) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      String templateName = GET_PRINT_TEMPLATE_NAME;
      ps = con.prepareStatement(templateName);
      ps.setInt(1, docId);
      return DataBaseUtil.getStringValueFromDb(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_HVF_PRINT_TEMPLATE_NAME. */
  private static final String GET_HVF_PRINT_TEMPLATE_NAME = "SELECT dht.template_name "
      + " FROM patient_documents pd "
      + " JOIN doc_hvf_templates dht ON dht.template_id = pd.template_id " + " WHERE pd.doc_id = ?";

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
   * @throws SQLException the SQL exception
   */
  public static String getTemplateName(int docId, String documentFormat) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      String templateName = null;
      if (documentFormat != null && documentFormat.equals("doc_hvf_templates")) {
        templateName = GET_HVF_PRINT_TEMPLATE_NAME;
      } else {
        templateName = GET_RICH_TEXT_PRINT_TEMPLATE_NAME;
      }
      ps = con.prepareStatement(templateName);
      ps.setInt(1, docId);
      return DataBaseUtil.getStringValueFromDb(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }
}

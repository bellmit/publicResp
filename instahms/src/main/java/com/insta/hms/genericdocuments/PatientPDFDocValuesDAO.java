/**
 *
 */

package com.insta.hms.genericdocuments;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;
import org.apache.commons.beanutils.BasicDynaBean;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class PatientPDFDocValuesDAO.
 *
 * @author lakshmi.p
 */
public class PatientPDFDocValuesDAO extends GenericDAO {

  /**
   * Instantiates a new patient PDF doc values DAO.
   */
  public PatientPDFDocValuesDAO() {
    super("patient_pdf_doc_images");
  }

  /**
   * Update PDF doc image values.
   *
   * @param con the con
   * @param list the list
   * @param docId the doc id
   * @return true, if successful
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public boolean updatePDFDocImageValues(Connection con, List<Map<String, BasicDynaBean>> list,
      Object docId) throws SQLException, IOException {
    int count = 0;
    GenericDAO pdfdocimagevaluesdao = new GenericDAO("patient_pdf_doc_images");
    Map<String, Object> keys = new HashMap<String, Object>();
    keys.put("doc_id", docId);
    for (Map<String, BasicDynaBean> map : list) {
      for (Map.Entry<String, BasicDynaBean> entry 
          : (Collection<Map.Entry<String, BasicDynaBean>>) map
          .entrySet()) {
        BasicDynaBean pdfdocimagevaluesbean = entry.getValue();
        if (entry.getKey().equals("insert")) {
          pdfdocimagevaluesbean.set("doc_id", docId);
          if (pdfdocimagevaluesdao.insert(con, pdfdocimagevaluesbean)) {
            count++;
          }

        } else if (entry.getKey().equals("update")) {
          keys.put("field_id", pdfdocimagevaluesbean.get("field_id"));
          count += pdfdocimagevaluesdao.update(con, pdfdocimagevaluesbean.getMap(), keys);

        }
      }
    }
    return list.size() == count;
  }

  /** The Constant PDF_IMAGE_VALUES. */
  private static final String PDF_IMAGE_VALUES =
      " SELECT dht.template_name,dhtf.field_name, dhtf.display_name, dhtf.field_input,"
          + " pdhi.field_image_content_type, "
          + " dhtf.field_id, pdhi.doc_image_id, pdhi.device_ip, pdhi.device_info,"
          + " pdhi.capture_time "
          + " FROM patient_pdf_doc_images pdhi"
          + " LEFT OUTER JOIN patient_documents phd on phd.doc_id = pdhi.doc_id"
          + " LEFT OUTER JOIN doc_pdf_form_templates dht on dht.template_id=phd.template_id"
          + " LEFT OUTER JOIN doc_pdf_template_ext_fields dhtf on dhtf.field_id=pdhi.field_id"
          + " WHERE phd.doc_id=? ";

  /**
   * Gets the PDF doc image values.
   *
   * @param docId the doc id
   * @return the PDF doc image values
   * @throws SQLException the SQL exception
   */
  @SuppressWarnings("unchecked")
  public static List<BasicDynaBean> getPDFDocImageValues(int docId) throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      String fieldsValues = PDF_IMAGE_VALUES;
      ps = con.prepareStatement(fieldsValues);
      ps.setInt(1, docId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant PDF_TEMPLATE_IMAGE_VALUES. */
  private static final String PDF_TEMPLATE_IMAGE_VALUES =
      " SELECT dht.template_name, dhtf.field_name, dhtf.display_name, dhtf.field_input,"
          + " NULL AS field_image_content_type, "
          + " dhtf.field_id, NULL AS doc_image_id, NULL AS device_ip, NULL AS device_info, "
          + " NULL AS capture_time "
          + " FROM doc_pdf_template_ext_fields dhtf "
          + " LEFT OUTER JOIN doc_pdf_form_templates dht on dht.template_id=dhtf.template_id "
          + " WHERE dht.template_id=? ";

  /**
   * Gets the PDF template image values.
   *
   * @param templateId the template id
   * @return the PDF template image values
   * @throws SQLException the SQL exception
   */
  @SuppressWarnings("unchecked")
  public static List<BasicDynaBean> getPDFTemplateImageValues(int templateId)
      throws SQLException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      String fieldsValues = PDF_TEMPLATE_IMAGE_VALUES;
      ps = con.prepareStatement(fieldsValues);
      ps.setInt(1, templateId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant PDF_DOC_IMAGE. */
  private static final String PDF_DOC_IMAGE =
      " SELECT field_image  FROM patient_pdf_doc_images WHERE doc_image_id=?";

  /**
   * Gets the PDF doc image.
   *
   * @param docImageId the doc image id
   * @return the PDF doc image
   * @throws SQLException the SQL exception
   */
  public static InputStream getPDFDocImage(int docImageId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    ResultSet rs = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(PDF_DOC_IMAGE);
      ps.setInt(1, docImageId);
      rs = ps.executeQuery();
      if (rs.next()) {
        return rs.getBinaryStream(1);
      } else {
        return null;
      }
    } finally {
      DataBaseUtil.closeConnections(con, ps, rs);
    }
  }
}

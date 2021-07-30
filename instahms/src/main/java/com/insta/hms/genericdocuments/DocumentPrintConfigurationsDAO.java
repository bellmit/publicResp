package com.insta.hms.genericdocuments;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.RequestContext;
import com.insta.hms.common.GenericDAO;
import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

// TODO: Auto-generated Javadoc
/**
 * The Class DocumentPrintConfigurationsDAO.
 */
public class DocumentPrintConfigurationsDAO extends GenericDAO {

  /**
   * Instantiates a new document print configurations DAO.
   */
  public DocumentPrintConfigurationsDAO() {
    super("doc_print_configuration");
  }

  /**
   * Gets the document print configaration.
   *
   * @param docType the doc type
   * @return the document print configaration
   * @throws SQLException the SQL exception
   */
  public static BasicDynaBean getDocumentPrintConfigaration(String docType) throws SQLException {
    return getDocumentPrintConfigaration(docType, RequestContext.getCenterId());
  }

  /** The Constant GET_DOCUMENT_PRINT_CONFIGURATION. */
  private static final String GET_DOCUMENT_PRINT_CONFIGURATION =
      "SELECT * FROM doc_print_configuration WHERE document_type = ? AND center_id = ?";

  /**
   * Gets the document print configaration.
   *
   * @param docType the doc type
   * @param centerId the center id
   * @return the document print configaration
   * @throws SQLException the SQL exception
   */
  public static BasicDynaBean getDocumentPrintConfigaration(String docType, Integer centerId)
      throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    BasicDynaBean bean = null;
    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(GET_DOCUMENT_PRINT_CONFIGURATION);
      ps.setString(1, docType);
      bean = getApplicableConfiguration(ps, centerId, 2);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
    return bean;
  }

  // These methods are at the time, workaround for the fact that we need to combine the hpm and pd
  // fields into
  // one bean for "print" function to work correctly. Once all the prints are moved to this model,
  // we will not
  // need these functions, we will get the pd and hpm separately

  /**
   * Gets the all print preferences.
   *
   * @param docType the doc type
   * @return the all print preferences
   * @throws SQLException the SQL exception
   */
  public static BasicDynaBean getAllPrintPreferences(String docType) throws SQLException {
    return getAllPrintPreferences(docType, RequestContext.getCenterId(), null);
  }

  /**
   * Gets the all print preferences.
   *
   * @param docType the doc type
   * @param printerId the printer id
   * @return the all print preferences
   * @throws SQLException the SQL exception
   */
  public static BasicDynaBean getAllPrintPreferences(String docType, Integer printerId)
      throws SQLException {
    return getAllPrintPreferences(docType, RequestContext.getCenterId(), printerId);
  }

  /** The Constant GET_DEFAULT_PRINTER_PREFS. */
  private static final String GET_DEFAULT_PRINTER_PREFS =
      "SELECT pd.*, hpm.* FROM doc_print_configuration dpc, printer_definition pd,"
          + " hosp_print_master hpm "
          + " WHERE dpc.printer_settings = pd.printer_id AND dpc.page_settings = hpm.print_type"
          + " AND dpc.center_id = hpm.center_id "
          + " AND dpc.document_type = ? AND dpc.center_id = ?"
          + " AND dpc.printer_settings = pd.printer_id";

  /** The Constant GET_PRINTER_SPECIFIC_PREFS. */
  private static final String GET_PRINTER_SPECIFIC_PREFS =
      "SELECT pd.*, hpm.* FROM doc_print_configuration dpc "
          + " LEFT JOIN hosp_print_master hpm on dpc.page_settings = hpm.print_type"
          + " AND dpc.document_type = ? AND dpc.center_id = hpm.center_id "
          + " LEFT JOIN printer_definition pd on pd.printer_id = ? " + " WHERE hpm.center_id = ?";

  /**
   * Gets the all print preferences.
   *
   * @param docType the doc type
   * @param centerId the center id
   * @param printerId the printer id
   * @return the all print preferences
   * @throws SQLException the SQL exception
   */
  public static BasicDynaBean getAllPrintPreferences(String docType, Integer centerId,
      Integer printerId) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    BasicDynaBean bean = null;
    int centerIndex = 2;
    String query = GET_DEFAULT_PRINTER_PREFS;

    if (null != printerId) {
      query = GET_PRINTER_SPECIFIC_PREFS;
      centerIndex++;
    }

    try {
      con = DataBaseUtil.getConnection();
      ps = con.prepareStatement(query);
      ps.setString(1, docType);
      if (null != printerId) {
        ps.setInt(2, printerId);
      }
      bean = getApplicableConfiguration(ps, centerId, centerIndex);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }

    return bean;
  }

  /**
   * Gets the center specific configuration.
   *
   * @param ps the ps
   * @param centerId the center id
   * @param fieldIndex the field index
   * @return the center specific configuration
   * @throws SQLException the SQL exception
   */
  private static BasicDynaBean getCenterSpecificConfiguration(PreparedStatement ps,
      Integer centerId, int fieldIndex) throws SQLException {
    if (null == centerId) {
      centerId = 0;
    }
    ps.setInt(fieldIndex, centerId);
    BasicDynaBean bean = DataBaseUtil.queryToDynaBean(ps);
    return bean;
  }

  // TODO : change this to set the parameters dynamically, without reference to a specific field
  // index

  /**
   * Gets the applicable configuration.
   *
   * @param ps the ps
   * @param centerId the center id
   * @param fieldIndex the field index
   * @return the applicable configuration
   * @throws SQLException the SQL exception
   */
  private static BasicDynaBean getApplicableConfiguration(PreparedStatement ps, Integer centerId,
      int fieldIndex) throws SQLException {
    BasicDynaBean bean = null;
    bean = getCenterSpecificConfiguration(ps, centerId, fieldIndex);
    if (null == bean) {
      bean = getCenterSpecificConfiguration(ps, 0, fieldIndex);
    }
    return bean;
  }

  /**
   * Gets the discharge summary configuration.
   *
   * @return the discharge summary configuration
   * @throws SQLException the SQL exception
   */
  public static BasicDynaBean getDischargeSummaryConfiguration() throws SQLException {
    return DocumentPrintConfigurationsDAO.getDocumentPrintConfigaration("discharge");
  }

  /**
   * Gets the discharge summary preferences.
   *
   * @param printerId the printer id
   * @return the discharge summary preferences
   * @throws SQLException the SQL exception
   */
  public static BasicDynaBean getDischargeSummaryPreferences(Integer printerId)
      throws SQLException {
    return getAllPrintPreferences("discharge", printerId);
  }

  /**
   * Gets the registration print configuration.
   *
   * @param templateName the template name
   * @return the registration print configuration
   * @throws SQLException the SQL exception
   */
  public static BasicDynaBean getRegistrationPrintConfiguration(String templateName)
      throws SQLException {
    return DocumentPrintConfigurationsDAO.getDocumentPrintConfigaration("reg_"
        + (templateName == null ? "" : templateName));
  }

  /**
   * Gets the registration print configuration.
   *
   * @param templateName the template name
   * @param printerId the printer id
   * @return the registration print configuration
   * @throws SQLException the SQL exception
   */
  public static BasicDynaBean getRegistrationPrintConfiguration(String templateName,
      Integer printerId) throws SQLException {
    return getAllPrintPreferences("reg_" + (templateName == null ? "" : templateName), printerId);
  }

  /**
   * Gets the registration print preferences.
   *
   * @param templateName the template name
   * @param printerId the printer id
   * @return the registration print preferences
   * @throws SQLException the SQL exception
   */
  public static BasicDynaBean getRegistrationPrintPreferences(String templateName,
      Integer printerId) throws SQLException {
    return getAllPrintPreferences("reg_" + (templateName == null ? "" : templateName), printerId);
  }

  /**
   * Gets the prescription print configuration.
   *
   * @param templateName the template name
   * @return the prescription print configuration
   * @throws SQLException the SQL exception
   */
  public static BasicDynaBean getPrescriptionPrintConfiguration(String templateName)
      throws SQLException {
    return DocumentPrintConfigurationsDAO.getDocumentPrintConfigaration("prescription_"
        + (templateName == null ? "" : templateName));
  }

  /**
   * Gets the prescription print preferences.
   *
   * @param templateName the template name
   * @param printerId the printer id
   * @return the prescription print preferences
   * @throws SQLException the SQL exception
   */
  public static BasicDynaBean getPrescriptionPrintPreferences(String templateName,
      Integer printerId) throws SQLException {
    return getAllPrintPreferences("prescription_" + (templateName == null ? "" : templateName),
        printerId);
  }

}

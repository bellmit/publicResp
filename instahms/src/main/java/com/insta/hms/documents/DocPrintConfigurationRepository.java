package com.insta.hms.documents;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

import java.sql.SQLException;

// TODO: Auto-generated Javadoc
/**
 * The Class DocPrintConfigurationRepository.
 */
@Repository
public class DocPrintConfigurationRepository extends GenericRepository {

  /**
   * Instantiates a new doc print configuration repository.
   */
  public DocPrintConfigurationRepository() {
    super("doc_print_configuration");
    // TODO Auto-generated constructor stub
  }

  /**
   * Gets the registration print configuration.
   *
   * @param templateName the template name
   * @return the registration print configuration
   */
  public static BasicDynaBean getRegistrationPrintConfiguration(String templateName) {
    // RC TODO : Why DocPrintConfigurationRepository. ?
    return getDocumentPrintConfigaration("reg_" + (templateName == null ? "" : templateName));
  }
  
  /**
   * Gets the registration print configuration.
   *
   * @param templateName the template name
   * @param printerId the printer id
   * @return the registration print configuration
   */
  public static BasicDynaBean getRegistrationPrintConfiguration(String templateName,
      Integer printerId) {
    return getAllPrintPreferences("reg_" + (templateName == null ? "" : templateName), printerId);
  }


  /**
   * Gets the document print configaration.
   *
   * @param docType the doc type
   * @return the document print configaration
   */
  public static BasicDynaBean getDocumentPrintConfigaration(String docType) {
    return getDocumentPrintConfigaration(docType, RequestContext.getCenterId());
  }

  /** The Constant GET_DOCUMENT_PRINT_CONFIGURATION. */
  private static final String GET_DOCUMENT_PRINT_CONFIGURATION = "SELECT * FROM "
      + "doc_print_configuration WHERE document_type = ? AND center_id = ?";

  /**
   * Gets the document print configaration.
   *
   * @param docType the doc type
   * @param centerId the center id
   * @return the document print configaration
   */
  public static BasicDynaBean getDocumentPrintConfigaration(String docType, Integer centerId) {
    return getApplicableConfiguration(GET_DOCUMENT_PRINT_CONFIGURATION, docType, centerId, 0);
  }

  /**
   * Gets the center specific configuration.
   *
   * @param query the query
   * @param param the param
   * @param centerId the center id
   * @param printerId the printer id
   * @return the center specific configuration
   */
  private static BasicDynaBean getCenterSpecificConfiguration(String query, String param,
      Integer centerId, Integer printerId) {
    if (null == centerId) {
      centerId = 0;
    }
    if (query.equalsIgnoreCase(GET_DEFAULT_PRINTER_PREFS)
        || query.equalsIgnoreCase(GET_DOCUMENT_PRINT_CONFIGURATION)) {
      return DatabaseHelper.queryToDynaBean(query, new Object[] { param, centerId });
    } else if (query.equalsIgnoreCase(GET_PRINTER_SPECIFIC_PREFS)) {
      return DatabaseHelper.queryToDynaBean(query, new Object[] { param, printerId, centerId });
    }
    return null;
  }
  
  /**
   * Gets the center specific configuration.
   *
   * @param query the query
   * @param param the param
   * @param centerId the center id
   * @return the center specific configuration
   * @throws SQLException the SQL exception
   */
  private static BasicDynaBean getCenterSpecificConfiguration(String query, String param,
      Integer centerId) throws SQLException {
    if (null == centerId) {
      centerId = 0;
    }
    return DatabaseHelper.queryToDynaBean(query, new Object[] { param, centerId });
  }

  /**
   * Gets the prescription print preferences.
   *
   * @param templateName the template name
   * @return the prescription print preferences
   */
  public static BasicDynaBean getPrescriptionPrintPreferences(String templateName) {
    return getDocumentPrintConfigaration(
        "prescription_" + (templateName == null ? "" : templateName));
  }

  /**
   * Gets the prescription print preferences.
   *
   * @param templateName the template name
   * @param printerId the printer id
   * @return the prescription print preferences
   */
  public static BasicDynaBean getPrescriptionPrintPreferences(String templateName,
      Integer printerId) {
    return getAllPrintPreferences("prescription_" + (templateName == null ? "" : templateName),
        printerId);
  }

  /**
   * Gets the applicable configuration.
   *
   * @param query the query
   * @param docType the doc type
   * @param centerId the center id
   * @param printerId the printer id
   * @return the applicable configuration
   */
  private static BasicDynaBean getApplicableConfiguration(String query, String docType,
      Integer centerId, int printerId) {
    BasicDynaBean bean = null;
    bean = getCenterSpecificConfiguration(query, docType, centerId, printerId);
    if (null == bean) {
      bean = getCenterSpecificConfiguration(query, docType, 0, printerId);
    }
    return bean;
  }

  /** The Constant GET_DEFAULT_PRINTER_PREFS. */
  private static final String GET_DEFAULT_PRINTER_PREFS = "SELECT pd.*, hpm.* FROM "
      + "doc_print_configuration dpc, printer_definition pd, hosp_print_master hpm "
      + " WHERE dpc.printer_settings = pd.printer_id AND dpc.page_settings = hpm.print_type "
      + "AND dpc.center_id = hpm.center_id "
      + " AND dpc.document_type = ? AND dpc.center_id = ? "
      + "AND dpc.printer_settings = pd.printer_id";

  /** The Constant GET_PRINTER_SPECIFIC_PREFS. */
  private static final String GET_PRINTER_SPECIFIC_PREFS = "SELECT pd.*, hpm.* FROM "
      + "doc_print_configuration dpc "
      + " LEFT JOIN hosp_print_master hpm on dpc.page_settings = hpm.print_type AND "
      + "dpc.document_type = ? AND dpc.center_id = hpm.center_id "
      + " LEFT JOIN printer_definition pd on pd.printer_id = ?  WHERE hpm.center_id = ?";


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
   * Gets the discharge summary configuration.
   *
   * @return the discharge summary configuration
   * @throws SQLException the SQL exception
   */
  public static BasicDynaBean getDischargeSummaryConfiguration() throws SQLException {
    return getDocumentPrintConfigaration("discharge");
  }

  // public static BasicDynaBean getPrescriptionPrintPreferences(String
  // templateName,Integer printerId) throws SQLException {
  // return getAllPrintPreferences("prescription_"+(templateName == null ? ""
  // : templateName), printerId);
  // }

  /**
   * Gets the all print preferences.
   *
   * @param docType the doc type
   * @param printerId the printer id
   * @return the all print preferences
   */
  public static BasicDynaBean getAllPrintPreferences(String docType, Integer printerId) {
    return getAllPrintPreferences(docType, RequestContext.getCenterId(), printerId);
  }

  /**
   * Gets the all print preferences.
   *
   * @param docType the doc type
   * @param centerId the center id
   * @param printerId the printer id
   * @return the all print preferences
   */
  public static BasicDynaBean getAllPrintPreferences(String docType, Integer centerId,
      Integer printerId) {
    BasicDynaBean bean = null;
    String query = GET_DEFAULT_PRINTER_PREFS;
    if (centerId == null) {
      centerId = 0;
    }
    if (null != printerId) {
      query = GET_PRINTER_SPECIFIC_PREFS;
    }
    if (query.equals(GET_DEFAULT_PRINTER_PREFS)) {
      bean = DatabaseHelper.queryToDynaBean(query, new Object[] { docType, centerId });
      if (bean == null) {
        bean = DatabaseHelper.queryToDynaBean(query, new Object[] { docType, 0 });
      }
    } else {
      bean = DatabaseHelper.queryToDynaBean(query, new Object[] { docType, printerId, centerId });
      if (bean == null) {
        // possible, if there is no printer configuration for particular center
        bean = DatabaseHelper.queryToDynaBean(query, new Object[] { docType, printerId, 0 });
      } 
    }
    return bean;
  }
}

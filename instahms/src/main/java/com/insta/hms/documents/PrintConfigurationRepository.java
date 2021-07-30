package com.insta.hms.documents;

import com.bob.hms.common.RequestContext;
import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.common.GenericRepository;

import org.apache.commons.beanutils.BasicDynaBean;
import org.springframework.stereotype.Repository;

// TODO: Auto-generated Javadoc
/**
 * The Class PrintConfigurationRepository.
 */
@Repository
public class PrintConfigurationRepository extends GenericRepository {

  /**
   * Instantiates a new prints the configuration repository.
   */
  public PrintConfigurationRepository() {
    super("hosp_print_master");
  }

  /** The Constant PRINT_TYPE_PATIENT. */
  public static final String PRINT_TYPE_PATIENT = "Discharge";
  
  /** The Constant PRINT_TYPE_DIAG. */
  public static final String PRINT_TYPE_DIAG = "Lab";
  
  /** The Constant PRINT_TYPE_DIAG_RAD. */
  public static final String PRINT_TYPE_DIAG_RAD = "Rad";
  
  /** The Constant PRINT_TYPE_BILL. */
  public static final String PRINT_TYPE_BILL = "Bill";
  
  /** The Constant PRINT_TYPE_PHARMACY. */
  public static final String PRINT_TYPE_PHARMACY = "Pharmacy";
  
  /** The Constant PRINT_TYPE_DISCHARGE. */
  public static final String PRINT_TYPE_DISCHARGE = "Discharge";
  
  /** The Constant PRINT_TYPE_SERVICE. */
  public static final String PRINT_TYPE_SERVICE = "Service";
  
  /** The Constant PRINT_TYPE_INSURENCE. */
  public static final String PRINT_TYPE_INSURENCE = "Insurance";
  
  /** The Constant PRINT_TYPE_STORE. */
  public static final String PRINT_TYPE_STORE = "Store";
  
  /** The Constant PRINT_TYPE_APPOINTMENT. */
  public static final String PRINT_TYPE_APPOINTMENT = "Appointment";
  
  /** The Constant PRINT_TYPE_PATIENT_SURVEY_RESPONSE. */
  public static final String PRINT_TYPE_PATIENT_SURVEY_RESPONSE = "SurveyResponse";
  
  /** The Constant PRINT_TYPE_SAMPLE_COLLECTION. */
  public static final String PRINT_TYPE_SAMPLE_COLLECTION = "Sample";
  
  /** The Constant PRINT_TYPE_PRESCRIPTION_LABEL. */
  public static final String PRINT_TYPE_PRESCRIPTION_LABEL = "PrescLabel";
  
  /** The Constant PRINT_TYPE_SAMPLE_WORK_SHEET. */
  public static final String PRINT_TYPE_SAMPLE_WORK_SHEET = "SampleWorkSheet";
  
  /** The Constant PRINT_TYPE_WEB_DIAG. */
  public static final String PRINT_TYPE_WEB_DIAG = "Web Diag";

  /*
   * Returns the complete print settings using the default printer defined for the given print type.
   * Print type can be one of PRINT_TYPE_XXX constants defined above. The return value includes:
   * Page options: page width, height etc (as in printer_definition) Header/footer settings:
   * header1, header2 etc (as in hosp_print_master)
   */

  /** The print page options. */
  private static String PRINT_PAGE_OPTIONS = "SELECT pd.*,hpm.* FROM printer_definition pd "
      + " LEFT JOIN hosp_print_master hpm USING(printer_id) ";

  /**
   * Gets the center page options.
   *
   * @param printType the print type
   * @param centerId the center id
   * @return the center page options
   */
  public static BasicDynaBean getCenterPageOptions(String printType, int centerId) {

    String printTypeQuery = PRINT_PAGE_OPTIONS + " where print_type= ? and center_id=?";
    BasicDynaBean bean = DatabaseHelper.queryToDynaBean(printTypeQuery,
        new Object[] { printType, centerId });
    if (bean != null) {
      // found center specific print configurations.
      return bean;
    } else {
      // not found center specific print configuration so return the super
      // center print configuration.
      if (centerId != 0) {
        bean = DatabaseHelper.queryToDynaBean(printTypeQuery, new Object[] { printType, 0 });
        return bean;
      }
    }
    return null;
  }

  /**
   * Gets the patient default print prefs.
   *
   * @return the patient default print prefs
   */
  public static BasicDynaBean getPatientDefaultPrintPrefs() {
    return getPageOptions(PRINT_TYPE_PATIENT);
  }
  
  /**
   * Gets the patient default print prefs.
   *
   * @param centerId the center id
   * @return the patient default print prefs
   */
  public static BasicDynaBean getPatientDefaultPrintPrefs(int centerId) {
    return getCenterPageOptions(PRINT_TYPE_PATIENT, centerId);
  }

  /** The print page definitions. */
  private static String PRINT_PAGE_DEFINITIONS = "SELECT pd.*,hpm.* FROM printer_definition pd "
      + " LEFT JOIN hosp_print_master hpm on pd.printer_id=? where  print_type=? and center_id=?";

  /**
   * Gets the page options.
   *
   * @param printType the print type
   * @param printId the print id
   * @param centerId the center id
   * @return the page options
   */
  public static BasicDynaBean getPageOptions(String printType, int printId, int centerId) {

    if (printId == 0) {
      return getCenterPageOptions(printType, centerId);
    }
    BasicDynaBean bean = DatabaseHelper.queryToDynaBean(PRINT_PAGE_DEFINITIONS,
        new Object[] { printId, printType, centerId });
    if (bean != null) {
      // found center specific print configurations.
      return bean;
    } else {
      // not found center specific print configuration so return the super
      // center print configuration.
      if (centerId != 0) {
        return DatabaseHelper.queryToDynaBean(PRINT_PAGE_DEFINITIONS,
            new Object[] { printId, printType, 0 });
      }
    }
    return null;

  }
  
  /**
   * Gets the page options.
   *
   * @param printType the print type
   * @return the page options
   */
  public static BasicDynaBean getPageOptions(String printType) {
    return getCenterPageOptions(printType, RequestContext.getCenterId());
  }


}

package com.insta.hms.outpatient;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.core.clinical.allergies.AllergiesConstants;
import com.insta.hms.core.clinical.allergies.AllergiesValidator;
import com.insta.hms.exception.ValidationErrorMap;
import com.insta.hms.mdm.allergy.AllergenMasterRepository;
import com.insta.hms.mdm.stores.genericnames.GenericNamesService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The Class AllergiesDAO.
 */
public class AllergiesDAO extends GenericDAO {

  /** The log. */
  static Logger log = LoggerFactory.getLogger(AllergiesDAO.class);

  static AllergenMasterRepository allergenMasterRepository = ApplicationContextProvider.getBean(
      AllergenMasterRepository.class);

  static AllergiesValidator allergiesValidator = ApplicationContextProvider.getBean(
      AllergiesValidator.class);

  static GenericNamesService genericNamesService = ApplicationContextProvider.getBean(
      GenericNamesService.class);

  /**
   * Instantiates a new allergies DAO.
   */
  public AllergiesDAO() {
    super("patient_allergies");
  }

  /** The Constant GET_PATIENT_ALLERGIES. */
  private static final String GET_PATIENT_ALLERGIES = "SELECT pa.allergy_id, pa.reaction,"
      + " COALESCE(am.allergen_description,gn.generic_name) as allergy,"
      + " case when pa.allergy_type_id is null then 'N' else atm.allergy_type_code END"
      + " as allergy_type, atm.allergy_type_name, pa.allergen_code_id, gn.generic_code,"
      + " pa.allergy_type_id,"
      + " pa.onset_date, pa.severity, pa.status,"
      + " psd.mr_no, psd.section_item_id, psd.patient_id, psd.item_type"
      + " FROM patient_allergies pa "
      + " JOIN patient_section_details psd ON (pa.section_detail_id = psd.section_detail_id) "
      + " LEFT JOIN allergen_master am ON (pa.allergen_code_id=am.allergen_code_id)"
      + " LEFT JOIN allergy_type_master atm ON (atm.allergy_type_id = pa.allergy_type_id)"
      + " LEFT JOIN generic_name gn ON (pa.allergen_code_id = gn.allergen_code_id)"
      + " WHERE psd.mr_no = ? AND psd.section_id = -2 AND psd.section_status = 'A' "
      + " ORDER BY pa.status, atm.allergy_type_code";

  /**
   * Gets the active allergies for patient.
   *
   * @param mrNo the mr no
   * @return the active allergies for patient
   * @throws SQLException the SQL exception
   */
  public static List getActiveAllergiesForPatient(String mrNo) throws SQLException {

    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(GET_PATIENT_ALLERGIES);
      ps.setString(1, mrNo);

      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  private static final String GET_ALL_ACTIVE_ALLERGIES = "SELECT  pa.severity, pa.reaction, "
      + " pa.onset_date, pa.status, pa.allergy_id, atm.allergy_type_name, "
      + " COALESCE(am.allergen_description,gn.generic_name) as allergy,"
      + " case when pa.allergy_type_id is null then 'N' else atm.allergy_type_code END"
      + " as allergy_type, pa.allergen_code_id, gn.generic_code,"
      + " pa.allergy_type_id ,"
      + " psd.mr_no, psd.section_item_id, psd.patient_id, psd.finalized, psd.finalized_user, "
      + " usr.temp_username FROM patient_allergies pa"
      + " JOIN patient_section_details psd ON (pa.section_detail_id=psd.section_detail_id) "
      + " JOIN patient_section_forms psf ON (psf.section_detail_id=psd.section_detail_id) "
      + " LEFT JOIN allergen_master am ON (pa.allergen_code_id=am.allergen_code_id)"
      + " LEFT JOIN allergy_type_master atm ON (atm.allergy_type_id = pa.allergy_type_id)"
      + " LEFT JOIN generic_name gn ON (pa.allergen_code_id = gn.allergen_code_id)"
      + " LEFT JOIN u_user usr on psd.finalized_user = usr.emp_username "
      + " WHERE psd.mr_no=? AND psd.patient_id=? "
      + " AND coalesce(psd.section_item_id, 0)=? AND coalesce(psd.generic_form_id, 0)=?"
      + " AND psd.item_type=? AND section_id=-2 AND pa.status='A' AND psf.form_id=?"
      + " ORDER BY status, atm.allergy_type_code";

  /**
   * Gets the allergies.
   *
   * @param mrNo the mr no
   * @param patientId the patient id
   * @param itemId the item id
   * @param genericFormId the generic form id
   * @param formId the form id
   * @param itemType the item type
   * @return the allergies
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static List getAllergies(String mrNo, String patientId, int itemId, int genericFormId,
      int formId, String itemType) throws SQLException {

    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(GET_ALL_ACTIVE_ALLERGIES);
      ps.setString(1, mrNo);
      ps.setString(2, patientId);
      ps.setInt(3, itemId);
      ps.setInt(4, genericFormId);
      ps.setString(5, itemType);
      ps.setInt(6, formId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Gets the active allergies.
   *
   * @param mrNo the mr no
   * @param patientId the patient id
   * @param itemId the item id
   * @param genericFormId the generic form id
   * @param formId the form id
   * @param itemType the item type
   * @return the all active allergies
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   */
  public static List getAllActiveAllergies(String mrNo, String patientId, int itemId,
      int genericFormId, int formId, String itemType) throws SQLException, IOException {
    Connection con = DataBaseUtil.getConnection();
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(GET_ALL_ACTIVE_ALLERGIES);
      ps.setString(1, mrNo);
      ps.setString(2, patientId);
      ps.setInt(3, itemId);
      ps.setInt(4, genericFormId);
      ps.setString(5, itemType);
      ps.setInt(6, formId);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Update allergies.
   *
   * @param con the con
   * @param allergyId the allergy id
   * @param sectionDetailId the section detail id
   * @param allergyTypeIdStr the allergy type id string
   * @param allergenCodeId the allergen code id
   * @param genericCodeId the generic code id
   * @param allergy the allergy
   * @param reaction the reaction
   * @param onsetDate the onset date
   * @param severity the severity
   * @param status the status
   * @param delete the delete
   * @param edited the edited
   * @param userName the user name
   * @param isInsert the is insert
   * @return true, if successful
   * @throws SQLException the SQL exception
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws ParseException the parse exception
   */
  public boolean updateAllergies(Connection con, String allergyId, int sectionDetailId,
      String allergyTypeIdStr, String allergenCodeId, String genericCodeId, String allergy,
      String reaction, String onsetDate, String severity, String status, boolean delete,
      boolean edited, String userName, boolean isInsert) throws SQLException, IOException {

    ValidationErrorMap errMap = new ValidationErrorMap();

    if (allergyId.equals("_")) {
      if (delete) {
        return true; // no need to insert or delete
      } else {
        BasicDynaBean allergyBean = getBean();
        allergyBean.set("allergy_id", getNextSequence());
        allergyBean.set("section_detail_id", sectionDetailId);
        allergyBean.set("reaction", reaction);
        allergyBean.set("onset_date", onsetDate);
        allergyBean.set("severity", severity);
        allergyBean.set("status", status);
        allergyBean.set("username", userName);
        allergyBean.set("mod_time", DateUtil.getCurrentTimestamp());

        if (!StringUtils.isEmpty(allergyTypeIdStr)) {
          Integer allergyTypeId = Integer.parseInt(allergyTypeIdStr);
          allergyBean.set("allergy_type_id", allergyTypeId);
          setAllergenCodeIdForAllergy(userName, allergy, genericCodeId, allergyBean,
              allergenCodeId);
        } else {
          allergyBean.set("allergen_code_id", null);
          allergyBean.set("allergy_type_id", null);
        }

        if (allergiesValidator.validateAllergyInsert(allergyBean, errMap)) {
          if (!insert(con, allergyBean)) {
            return false;
          }

        }
      }
    } else {
      if (delete) {
        if (AllergiesDAO.updateUserName(con, Integer.parseInt(allergyId), userName) && !delete(con,
            "allergy_id", new Integer(allergyId))) {
          return false;
        }
      } else if (edited && !StringUtils.isEmpty(allergyId)) {
        BasicDynaBean allergyBean = getBean();
        allergyBean.set("allergy_id", Integer.parseInt(allergyId));
        allergyBean.set("section_detail_id", sectionDetailId);
        allergyBean.set("reaction", reaction);
        allergyBean.set("onset_date", onsetDate);
        allergyBean.set("severity", severity);
        allergyBean.set("status", status);
        allergyBean.set("username", userName);
        allergyBean.set("mod_time", DateUtil.getCurrentTimestamp());

        if (!StringUtils.isEmpty(allergyTypeIdStr)) {
          Integer allergyTypeId = Integer.parseInt(allergyTypeIdStr);
          allergyBean.set("allergy_type_id", allergyTypeId);
          setAllergenCodeIdForAllergy(userName, allergy, genericCodeId, allergyBean,
              allergenCodeId);
        } else {
          allergyBean.set("allergen_code_id", null);
          allergyBean.set("allergy_type_id", null);
        }

        HashMap<String, Integer> keys = new HashMap<>();
        keys.put("allergy_id", new Integer(allergyId));
        if (allergiesValidator.validateAllergyUpdate(allergyBean, errMap)) {
          if (update(con, allergyBean.getMap(), keys) == 0) {
            return false;
          }
        }
      }
    }
    return true;
  }

  private void setAllergenCodeIdForAllergy(String userName, String allergy,
      String genericCodeId, BasicDynaBean allergyBean, String allergenCodeId) {
    Integer allergyTypeId = (Integer) allergyBean.get(AllergiesConstants.ALLERGY_TYPE_ID);

    if (allergyTypeId == AllergiesConstants.MEDICINE_ALLERGY_TYPE_ID) {
      // To map the respective allergen code id with the selected generic name
      fetchAllergenCodeIdForGenericMedicine(genericCodeId, allergyBean);
    } else {
      if (StringUtils.isEmpty(allergenCodeId)) {
        // To auto create an entry in allergen master
        int newAllergenCodeId = autoCreateAllergenEntry(userName, allergy, allergyTypeId);
        allergyBean.set(AllergiesConstants.ALLERGEN_CODE_ID, newAllergenCodeId);
      } else {
        allergyBean.set(AllergiesConstants.ALLERGEN_CODE_ID, Integer.parseInt(allergenCodeId));
      }
    }
  }

  private int autoCreateAllergenEntry(String userName, String allergyString,
      int allergyBeanTypeId) {
    BasicDynaBean existingAllergenEntryBean = allergenMasterRepository.existingAllergenEntry(
        allergyString, allergyBeanTypeId);
    if (existingAllergenEntryBean != null) {
      return (int) existingAllergenEntryBean.get(AllergiesConstants.ALLERGEN_CODE_ID);
    }
    BasicDynaBean allergenMasterBean = allergenMasterRepository.getBean();
    allergenMasterBean.set(AllergiesConstants.ALLERGEN_CODE_ID, allergenMasterRepository
        .getNextId());
    allergenMasterBean.set(AllergiesConstants.ALLERGY_TYPE_ID, allergyBeanTypeId);
    allergenMasterBean.set("allergen_description", allergyString);
    allergenMasterBean.set("created_by", userName);
    allergenMasterBean.set("mod_user", userName);
    allergenMasterRepository.insert(allergenMasterBean);

    return (int) allergenMasterBean.get(AllergiesConstants.ALLERGEN_CODE_ID);
  }

  private void fetchAllergenCodeIdForGenericMedicine(Object genericCode,
      BasicDynaBean allergyBean) {
    if (genericCode instanceof String) {
      Map<String, Object> params = new HashMap<String, Object>();
      params.put("generic_code", (String) genericCode);
      BasicDynaBean genericNamesBean = genericNamesService.findByPk(params);
      if (genericNamesBean != null) {
        allergyBean.set(AllergiesConstants.ALLERGEN_CODE_ID, genericNamesBean.get(
            AllergiesConstants.ALLERGEN_CODE_ID));
      }
    }
  }

  /** The Constant UPDATE_USERNAME. */
  private static final String UPDATE_USERNAME = "UPDATE patient_allergies"
      + " SET username = ? WHERE allergy_id = ?";

  /**
   * Update user name.
   *
   * @param con the con
   * @param allergyId the allergy id
   * @param userName the user name
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public static boolean updateUserName(Connection con, int allergyId, String userName)
      throws SQLException {
    PreparedStatement ps = null;
    try {
      ps = con.prepareStatement(UPDATE_USERNAME);
      ps.setString(1, userName);
      ps.setInt(2, allergyId);
      return ps.executeUpdate() > 0;
    } finally {
      if (ps != null) {
        ps.close();
      }
    }
  }
}

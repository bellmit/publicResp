package com.insta.hms.vaccinationsinfo;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.GenericDAO;

import com.insta.hms.common.GenericRepository;
import org.apache.commons.beanutils.BasicDynaBean;

import java.sql.Array;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// TODO: Auto-generated Javadoc
/**
 * The Class VaccinationsInfoDao.
 */
public class VaccinationsInfoDao {

  /** The Constant DOSAGE_MASTER_LIST_QUERY. */
  private static final String DOSAGE_MASTER_LIST_QUERY = "SELECT * FROM ("
      + " SELECT '1992-1-12'::date as due_date, vm.vaccine_id, vm.vaccine_name,"
      + " vm.single_dose, vm.display_order, vm.status AS vaccine_status,  "
      + " vdm.vaccine_dose_id, vdm.dose_num, vdm.recommended_age, vdm.age_units, "
      + " vdm.notification_lead_time_days, vdm.status AS dose_status,"
      + " CASE WHEN vdm.age_units = 'W' THEN recommended_age * 7 "
      + " WHEN vdm.age_units = 'M' THEN recommended_age * 30.43 "
      + " ELSE recommended_age * 365.25 END AS ordering_age_units "
      + " FROM vaccine_dose_master vdm"
      + " JOIN vaccine_master vm ON(vdm.vaccine_id = vm.vaccine_id)) "
      + " AS foo ORDER BY #sortColumn# #sortOrder#";

  /** The Constant DOSAGE_MASTER_LIST_QUERY_FOR_PRINT. */
  private static final String DOSAGE_MASTER_LIST_QUERY_FOR_PRINT = "SELECT "
      + " * FROM ("
      + " SELECT '1992-1-12'::date as due_date, vm.vaccine_id, vm.vaccine_name,"
      + " vm.single_dose, vm.display_order,vdm.vaccine_dose_id, vdm.dose_num, "
      + " vdm.recommended_age, vdm.age_units, "
      + " vdm.notification_lead_time_days, vdm.status AS dose_status,"
      + " CASE WHEN vdm.age_units = 'W' THEN recommended_age * 7 "
      + " WHEN vdm.age_units = 'M' THEN recommended_age * 30.43 "
      + " ELSE recommended_age * 365.25 END AS ordering_age_units, "
      + " CASE WHEN vdm.age_units = 'W' THEN 'Week' WHEN vdm.age_units = 'M' "
      + " THEN 'Month' ELSE 'Year' END AS screen_age_units "
      + " FROM vaccine_dose_master vdm"
      + " JOIN vaccine_master vm ON(vdm.vaccine_id = vm.vaccine_id)) "
      + " AS foo ORDER BY ordering_age_units";

  /** The Constant PATIENT_VACCINATIONS_LIST. */
  private static final String PATIENT_VACCINATIONS_LIST = "SELECT "
      + " coalesce(d.doctor_name, pv.vacc_doctor_id, '') AS vacc_doctor_name, "
      + " pv.pat_vacc_id, pv.mr_no, pv.vaccine_dose_id, "
      + " pv.vaccination_datetime, pv.vacc_by, pv.patient_id, " 
      + " pv.medicine_id,pv.vaccine_category_id,vc.vaccine_category_name, "
      + "pv.route_of_admin,mr.route_name,pv.site_id,pv.cons_uom_id, "
      + " pv.medicine_quantity,pv.vacc_doctor_id, pv.med_name, pv.manufacturer, "
      + " pv.expiry_date, pv.batch, pv.mod_user, pv.mod_time, "
      + " pv.vaccination_status, pv.reason_for_not, pv.remarks,"
      + " pv.med_name || '/' || pv.manufacturer || '/' || pv.batch || '/' || "
      + " (case when pv.expiry_date is not null then "
      + " (to_char(pv.expiry_date, 'dd-MM-yyyy')) else '' end) AS concatenatedlbl,"
      + " pv.adverse_reaction_id,"
      + " ad.adverse_reaction_monitoring_for_id, ad.adverse_reaction_onset_id,"
      + " ad.adverse_reaction_corelation_id, ad.adverse_reaction_actions_id,"
      + " ad.adverse_start_date, ad.adverse_end_date, ad.adverse_remarks "
      + " FROM patient_vaccination pv"
      + " LEFT JOIN doctors d ON (pv.vacc_doctor_id = d.doctor_id)"
      + " LEFT JOIN adverse_reaction_for_vaccination ad "
      + " ON pv.adverse_reaction_id = ad.adverse_reaction_id"
      + " LEFT JOIN medicine_route mr ON (mr.route_id=pv.route_of_admin) "
      + " LEFT JOIN vaccine_category_master vc "
      + " ON (vc.vaccine_category_id = pv.vaccine_category_id)"
      + " WHERE mr_no = ?";

  /** The Constant PATIENT_VACCINATIONS_LIST_FOR_PRINT. */
  private static final String PATIENT_VACCINATIONS_LIST_FOR_PRINT = "SELECT "
      + " coalesce(d.doctor_name, pv.vacc_doctor_id, '') AS vacc_doctor_name, "
      + " pv.pat_vacc_id, pv.mr_no, pv.vaccine_dose_id, pv.vaccination_datetime, pv.vacc_by, "
      + " pv.vacc_doctor_id, pv.med_name, pv.manufacturer, "
      + " pv.expiry_date, pv.batch, pv.mod_user, pv.mod_time, pv.reason_for_not, pv.remarks,"
      + " pv.med_name || '/' || pv.manufacturer || '/' || pv.batch || '/' || "
      + " (case when pv.expiry_date is not null then "
      + " (to_char(pv.expiry_date, 'dd-MM-yyyy')) else '' end) AS concatenatedlbl, "
      + " CASE WHEN pv.vaccination_status = 'A' THEN 'Administered' "
      + " WHEN pv.vaccination_status = 'N' THEN 'Not to be Administered' "
      + " END AS vaccination_status "
      + " FROM patient_vaccination pv"
      + " LEFT JOIN doctors d ON (pv.vacc_doctor_id = d.doctor_id) "
      + " WHERE mr_no = ?";

  /** The Constant GET_PATIENT_AGE. */
  private static final String GET_PATIENT_AGE = "SELECT "
      + " coalesce(dateofbirth, expected_dob) AS patient_age "
      + " FROM patient_details WHERE mr_no = ?";

  /** The Constant GET_PATIENT_DETAILS. */
  private static final String GET_PATIENT_DETAILS = "Select "
      + " patient_name,dateofbirth,patient_phone,"
      + " CASE WHEN current_date - COALESCE(dateofbirth, expected_dob) < 31 "
      + " THEN (floor(current_date - COALESCE(dateofbirth, expected_dob)))::integer "
      + " WHEN current_date - COALESCE(dateofbirth, expected_dob) < 730 "
      + " THEN (floor((current_date - COALESCE(dateofbirth, expected_dob))/30.43))::integer "
      + " ELSE (floor((current_date - COALESCE(dateofbirth, expected_dob))/365.25))::integer "
      + " END AS age, "
      + " case when patient_gender = 'M' then 'Male' else 'Female' end as patient_gender "
      + " from patient_details where mr_no = ?";

  /** Max Vaccination Date. **/
  private static final String RECENT_VACCINATION_ADMINISTERED = "SELECT "
      + " vaccination_datetime::date administered_date,"
      + " mod_time as updated_datetime,"
      + " mod_user as updated_by FROM patient_vaccination"
      + " WHERE mr_no = ? order by vaccination_datetime desc limit 1";

  /**
   * Gets the patient details.
   *
   * @param mrNo the mr no
   * @return the patient details
   * @throws SQLException the SQL exception
   */
  public Map getPatientDetails(String mrNo) throws SQLException {
    if (mrNo == null || mrNo.equals("")) {
      return Collections.EMPTY_MAP;
    }

    Connection con = null;
    PreparedStatement pstmt = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      pstmt = con.prepareStatement(GET_PATIENT_DETAILS);
      pstmt.setString(1, mrNo);
      BasicDynaBean bean = DataBaseUtil.queryToDynaBean(pstmt);
      return bean.getMap();
    } finally {
      DataBaseUtil.closeConnections(con, pstmt);
    }
  }

  /**
   * Gets the patient DOB.
   *
   * @param mrno the mrno
   * @return the patient DOB
   * @throws SQLException the SQL exception
   */
  public java.sql.Date getPatientDOB(String mrno) throws SQLException {
    Connection con = null;
    PreparedStatement pstmt = null;

    try {
      con = DataBaseUtil.getReadOnlyConnection();
      pstmt = con.prepareStatement(GET_PATIENT_AGE);
      pstmt.setString(1, mrno);
      return DataBaseUtil.getDateValueFromDb(pstmt);
    } finally {
      DataBaseUtil.closeConnections(con, pstmt);
    }

  }

  /**
   * Gets the dosage master list.
   *
   * @param mrNo the mr no
   * @return the dosage master list
   * @throws SQLException the SQL exception
   */
  public List getDosageMasterList(String mrNo, String sortColumn, boolean sortReverse)
      throws SQLException {

    Connection con = null;
    PreparedStatement pstmt = null;

    if (mrNo == null || mrNo.equals("")) {
      return Collections.EMPTY_LIST;
    }

    try {
      con = DataBaseUtil.getReadOnlyConnection();
      pstmt = con.prepareStatement(GET_PATIENT_AGE);
      pstmt.setString(1, mrNo);
      BasicDynaBean bean = DataBaseUtil.queryToDynaBean(pstmt);
      java.sql.Date patientDob = (java.sql.Date) bean.get("patient_age");
      List<Map> dosageMapList = new ArrayList<>();
      String query = DOSAGE_MASTER_LIST_QUERY;
      if (sortColumn != null && sortColumn.equals("vaccine_name")) {
        sortColumn = "lower(vaccine_name)";
      }
      query = sortColumn != null ? query.replace("#sortColumn#", sortColumn)
          : query.replace("#sortColumn#", "ordering_age_units");
      String sortOrderFilter = "#sortOrder#";
      query = sortReverse ? query.replace(sortOrderFilter, "DESC")
          : query.replace(sortOrderFilter, "");
      pstmt =
          con.prepareStatement(query);
      List<BasicDynaBean> dosageBeansList = DataBaseUtil.queryToDynaList(pstmt);
      for (int i = 0; i < dosageBeansList.size(); i++) {
        BasicDynaBean dosageBean = dosageBeansList.get(i);
        /*
         * if (((String)dosageBean.get("dose_status")).equalsIgnoreCase("I") &&
         * dosageBean.get("pat_vacc_id") == null) {
         * 
         * dosageBeansList.remove(i); continue; }
         */

        dosageBean.set("due_date", getDueDate(patientDob, dosageBean));
        dosageMapList.add(dosageBean.getMap());

      }

      return dosageMapList;
    } finally {
      DataBaseUtil.closeConnections(con, pstmt);
    }
  }

  /**
   * Gets the dosage master list for print.
   *
   * @param mrNo the mr no
   * @return the dosage master list for print
   * @throws SQLException the SQL exception
   */
  public List getDosageMasterListForPrint(String mrNo) throws SQLException {

    Connection con = null;
    PreparedStatement pstmt = null;

    if (mrNo == null || mrNo.equals("")) {
      return Collections.EMPTY_LIST;
    }

    try {
      con = DataBaseUtil.getReadOnlyConnection();
      pstmt = con.prepareStatement(GET_PATIENT_AGE);
      pstmt.setString(1, mrNo);
      BasicDynaBean bean = DataBaseUtil.queryToDynaBean(pstmt);
      java.sql.Date patientDob = (java.sql.Date) bean.get("patient_age");
      List<Map> dosageMapList = new ArrayList<Map>();

      pstmt = con.prepareStatement(DOSAGE_MASTER_LIST_QUERY_FOR_PRINT);
      List<BasicDynaBean> dosageBeansList = DataBaseUtil.queryToDynaList(pstmt);
      for (int i = 0; i < dosageBeansList.size(); i++) {
        BasicDynaBean dosageBean = dosageBeansList.get(i);
        /*
         * if (((String)dosageBean.get("dose_status")).equalsIgnoreCase("I") &&
         * dosageBean.get("pat_vacc_id") == null) {
         * 
         * dosageBeansList.remove(i); continue; }
         */

        dosageBean.set("due_date", getDueDate(patientDob, dosageBean));
        dosageMapList.add(dosageBean.getMap());

      }

      return dosageMapList;
    } finally {
      DataBaseUtil.closeConnections(con, pstmt);
    }
  }

  /**
   * Gets the all patient vaccination list.
   *
   * @param mrno the mrno
   * @return the all patient vaccination list
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getAllPatientVaccinationList(String mrno) throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;

    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(PATIENT_VACCINATIONS_LIST);
      ps.setString(1, mrno);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Gets the all patient vaccination list for print.
   *
   * @param mrno the mrno
   * @return the all patient vaccination list for print
   * @throws SQLException the SQL exception
   */
  public static List<BasicDynaBean> getAllPatientVaccinationListForPrint(String mrno)
      throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;

    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(PATIENT_VACCINATIONS_LIST_FOR_PRINT);
      ps.setString(1, mrno);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /** The Constant GET_ALL_ACTIVE_DOCTORS. */
  private static final String GET_ALL_ACTIVE_DOCTORS = "SELECT "
      + " doctor_name, doctor_id  "
      + " FROM doctors "
      + " WHERE status='A' order by doctor_id";

  /**
   * Gets the all active doctors.
   *
   * @return the all active doctors
   * @throws SQLException the SQL exception
   */
  public static List getAllActiveDoctors() throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;

    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(GET_ALL_ACTIVE_DOCTORS);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }

  /**
   * Gets the due date.
   *
   * @param patientDob the patient dob
   * @param dosageBean the dosage bean
   * @return the due date
   */
  public java.sql.Date getDueDate(java.sql.Date patientDob, BasicDynaBean dosageBean) {
    Calendar calendar = Calendar.getInstance();

    Integer recommendedAge = (Integer) dosageBean.get("recommended_age");
    String ageUnits = (String) dosageBean.get("age_units");
    calendar.setTime(patientDob);
    int years = calendar.get(Calendar.YEAR);
    int months = calendar.get(Calendar.MONTH);
    int days = calendar.get(Calendar.DATE);

    if (ageUnits.equalsIgnoreCase("W")) {
      int recommendedDays = recommendedAge * 7;
      days = days + recommendedDays;
    } else if (ageUnits.equalsIgnoreCase("M")) {
      months = months + recommendedAge;
    } else {
      years = years + recommendedAge;
    }
    calendar.set(Calendar.DATE, days);
    calendar.set(Calendar.MONTH, months);
    calendar.set(calendar.YEAR, years);
    java.sql.Date date = new java.sql.Date(calendar.getTimeInMillis());

    return date;
  }

  /** The Constant DUE_VACCINATIONS_QUERY. */
  private static final String DUE_VACCINATIONS_QUERY = "SELECT vm.vaccine_id, vm.vaccine_name, "
      + " vm.single_dose, vm.display_order, vm.status AS vaccine_status,"
      + " vdm.vaccine_dose_id, vdm.dose_num, vdm.recommended_age, vdm.age_units,"
      + " vdm.notification_lead_time_days, vdm.status AS dose_status "
      + " FROM vaccine_dose_master vdm "
      + " JOIN vaccine_master vm ON(vdm.vaccine_id = vm.vaccine_id) " + " WHERE vdm.status = 'A'";

  /**
   * Gets the patient vaccination info.
   *
   * @param mrNo the mr no
   * @return the patient vaccination info
   * @throws SQLException the SQL exception
   * @throws ParseException the parse exception
   */
  public StringBuilder getPatientVaccinationInfo(String mrNo) throws SQLException, ParseException {
    StringBuilder msgBuilder = new StringBuilder();
    Connection con = null;
    PreparedStatement pstmt = null;

    java.sql.Date patientDob = getPatientDOB(mrNo);
    java.sql.Date currentDate = new java.sql.Date(new java.util.Date().getTime());
    int idx = 0;
    List columns = Arrays.asList(new String[] { "mr_no", "vaccine_dose_id", "vaccination_status" });
    Map<String, Object> identifiers = new HashMap<String, Object>();
    Calendar calendar = Calendar.getInstance();

    SimpleDateFormat formater = new SimpleDateFormat("dd-MM-yyyy");
    GenericDAO patientVaccinationDao = new GenericDAO("patient_vaccination");
    String doseNumber = "";

    try {
      con = DataBaseUtil.getReadOnlyConnection();
      pstmt = con.prepareStatement(DUE_VACCINATIONS_QUERY);
      List<BasicDynaBean> dosageBeansList = DataBaseUtil.queryToDynaList(pstmt);
      for (int i = 0; i < dosageBeansList.size(); i++) {
        BasicDynaBean bean = dosageBeansList.get(i);
        identifiers.put("mr_no", mrNo);
        identifiers
            .put("vaccine_dose_id", Integer.parseInt(bean.get("vaccine_dose_id").toString()));
        BasicDynaBean patientBean = patientVaccinationDao.findByKey(columns, identifiers);
        if (patientBean != null
            && patientBean.get("vaccination_status") != null
            && (((String) patientBean.get("vaccination_status")).equalsIgnoreCase("A") 
                || ((String) patientBean
                .get("vaccination_status")).equalsIgnoreCase("N"))) {
          continue;
        }

        java.sql.Date dueDate = getDueDate(patientDob, bean);
        calendar.setTime(dueDate);
        int daysPart = calendar.get(Calendar.DATE);
        daysPart = daysPart - (Integer) bean.get("notification_lead_time_days");
        calendar.set(Calendar.DATE, daysPart);
        java.sql.Date notificationDate = new java.sql.Date(calendar.getTimeInMillis());
        int days = dueDate.compareTo(currentDate);

        java.util.Date showDate = new java.util.Date(dueDate.getTime());
        String showFormatDate = formater.format(showDate);

        int notificationDays = notificationDate.compareTo(currentDate);

        if (days >= 0) {
          if (notificationDays <= 0) {
            doseNumber = bean.get("single_dose").toString().equalsIgnoreCase("Y") ? "" : " dose "
                + bean.get("dose_num").toString();
            msgBuilder.append(bean.get("vaccine_name") + " " + doseNumber + " is due on "
                + showFormatDate);
            if (i + 1 != dosageBeansList.size()) {
              msgBuilder.append(", ");
            }
          }
        } else if (days < 0) {
          doseNumber = bean.get("single_dose").toString().equalsIgnoreCase("Y") ? "" : " dose "
              + bean.get("dose_num").toString();
          msgBuilder.append(bean.get("vaccine_name") + " " + doseNumber + " is overdue on "
              + showFormatDate);
          if (i + 1 != dosageBeansList.size()) {
            msgBuilder.append(", ");
          }
        }

        idx++;
        if (idx % 1 == 0) {
          msgBuilder.append("<br/>");
        }
      }
    } finally {
      DataBaseUtil.closeConnections(con, pstmt);
    }

    return msgBuilder;
  }

  /**
   * Gets the patient vaccine due date.
   *
   * @param mrNo the mr no
   * @param dosageId the dosage id
   * @return the patient vaccine due date
   * @throws SQLException the SQL exception
   */
  public String getPatientVaccineDueDate(String mrNo, int dosageId) throws SQLException {

    GenericDAO dosageDao = new GenericDAO("vaccine_dose_master");
    SimpleDateFormat formater = new SimpleDateFormat("dd-MM-yyyy");

    BasicDynaBean dosageBean = dosageDao.findByKey("vaccine_dose_id", dosageId);
    java.sql.Date patientDob = getPatientDOB(mrNo);
    java.sql.Date dueDate = getDueDate(patientDob, dosageBean);
    java.util.Date showDate = new java.util.Date(dueDate.getTime());
    String showFormatDate = formater.format(showDate);

    return showFormatDate;
  }

  public BasicDynaBean getRecentVaccineAdministration(String mrNo) throws SQLException {
    return DataBaseUtil.queryToDynaBean(RECENT_VACCINATION_ADMINISTERED, new Object[] {mrNo});
  }

  private static final String VACCINE_SYMPTOM_SEVERITY_MAPPING = "SELECT ssm.* "
      + "FROM adverse_reaction_symptom_severity_mapping ssm "
      + "LEFT JOIN adverse_reaction_for_vaccination ad "
      + "ON ad.adverse_reaction_id = ssm.adverse_reaction_for_vaccination_id "
      + "WHERE adverse_reaction_for_vaccination_id = ANY (? ::integer[])";

  /**
   * This function getsVaccineSymptomSeverity.
   * @param adverseReactionIds lsit of all adverseReactionId
   * @return list of all vaccineSymptomSeverity
   * @throws SQLException exception
   */
  public List<BasicDynaBean> getVaccineSymptomSeverity(List<Integer> adverseReactionIds)
      throws SQLException {
    Connection con = null;
    PreparedStatement ps = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();
      ps = con.prepareStatement(VACCINE_SYMPTOM_SEVERITY_MAPPING);
      Array adverseIds = con.createArrayOf("INTEGER", adverseReactionIds.toArray());
      ps.setArray(1, adverseIds);
      return DataBaseUtil.queryToDynaList(ps);
    } finally {
      DataBaseUtil.closeConnections(con, ps);
    }
  }
}

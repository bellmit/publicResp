package com.insta.hms.messaging.hl7.providers;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.parser.Parser;

import com.insta.hms.common.DatabaseHelper;
import com.insta.hms.messaging.providers.QueryDataProvider;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * The Class HL7ADTProvider.
 * 
 * @author yashwant
 */
public abstract class HL7ADTProvider extends QueryDataProvider {

  /** The Constant logger. */
  private static final Logger logger = LoggerFactory.getLogger(HL7ADTProvider.class);

  /**
   * Instantiates a new HL 7 ADT provider.
   *
   * @param provider
   *          the provider
   */
  public HL7ADTProvider(String provider) {
    super(provider);
  }

  /** The sql adt08. */
  private static final String SQL_ADT_04_08 = "SELECT  pd.mr_no, pr.patient_id, pr.visit_type,"
      + " pd.patient_gender, pd.last_name, COALESCE(pd.middle_name, '') as middle_name,"
      + " pd.patient_name, sm.salutation, patient_address,"
      + " to_char(coalesce(pd.dateofbirth,pd.expected_dob), 'YYYYMMDD') as expected_dob,"
      + " gim.identifier_type, pd.government_identifier, c.city_name, pd.patient_phone,"
      + " pd.email_id, ctry.country_name, ctry.country_code, nid.country_name as nationality_name,"
      + " nid.country_code as nationality_code, stm.state_id, stm.state_name, cond.doctor_id,"
      + " cond.doctor_name, cond.doctor_license_number" + " FROM patient_details pd"
      + " LEFT JOIN patient_registration pr using(mr_no)"
      + " LEFT JOIN salutation_master sm ON (pd.salutation = sm.salutation_id)"
      + " LEFT JOIN govt_identifier_master gim ON (gim.identifier_id = pd.identifier_id)"
      + " LEFT JOIN city c ON(c.city_id = pd.patient_city)"
      + " LEFT JOIN country_master ctry ON (ctry.country_id = pd.country)"
      + " LEFT JOIN country_master nid ON (nid.country_id = pd.nationality_id)"
      + " LEFT JOIN state_master stm ON (stm.state_id = pd.patient_state)"
      + " LEFT JOIN doctors cond ON(cond.doctor_id = pr.doctor)";

  /**
   * Gets the patient dyna list.
   *
   * @param patientId
   *          the patient id
   * @param mrNo
   *          the mr no
   * @return the patient dyna list
   */
  protected BasicDynaBean getPatientDynaList(String patientId, String mrNo) {
    StringBuilder sb = new StringBuilder(SQL_ADT_04_08);
    if (!(patientId == null || patientId.isEmpty())) {
      Object[] filter = new Object[] { patientId };
      sb.append(" WHERE patient_id=?");
      return DatabaseHelper.queryToDynaBean(sb.toString(), filter);
    } else if (!(mrNo == null || mrNo.isEmpty())) {
      Object[] filter = new Object[] { mrNo };
      sb.append(" WHERE mr_no=? limit 1");
      return DatabaseHelper.queryToDynaBean(sb.toString(), filter);
    } else {
      return null;
    }

  }

  /** The Constant insuranceQuery. */
  private static final String insuranceQuery = "SELECT od.org_name, picm.insurance_co_name,"
      + " ptpa.tpa_name, pipm.interface_code AS plan_interface_code, pipm.plan_id, "
      + " picam.category_name, pipm.plan_name , pipm.plan_notes, "
      + " picm.interface_code AS ins_co_interface_code, picm.insurance_co_id,"
      + " pipm.plan_exclusions, ppip.priority, (case when modact.activation_status = 'Y'"
      + " then ppd.member_id else ins.policy_no end) as member_id, "
      + " (case when modact.activation_status = 'Y' then ppd.policy_number"
      + " else ins.insurance_no end) as policy_number,"
      + " to_char((case when modact.activation_status = 'Y' then ppd.policy_validity_start"
      + " else ins.policy_validity_start end), 'YYYYMMDD') as policy_validity_start,"
      + " to_char((case when modact.activation_status = 'Y' then ppd.policy_validity_end"
      + " else ins.policy_validity_end end), 'YYYYMMDD') as policy_validity_end,"
      + " (case when modact.activation_status = 'Y' then ppd.policy_holder_name "
      + " else ins.policy_holder_name end) as policy_holder_name,"
      + " (case when modact.activation_status = 'Y' then ppd.patient_relationship"
      + " else ins.patient_relationship end) as patient_relationship,"
      + " picm.insurance_co_address, picm.insurance_co_city, picm.insurance_co_state,"
      + " picm.insurance_co_country, picm.insurance_co_phone, ppip.plan_limit, pr.patient_id"
      + " FROM  patient_registration pr"
      + " LEFT JOIN organization_details od ON(pr.org_id = od.org_id)"
      + " LEFT JOIN insurance_case ins ON (pr.insurance_id = ins.insurance_id)"
      + " LEFT JOIN patient_insurance_plans ppip ON(ppip.patient_id = pr.patient_id"
      + " AND ppip.priority = 1)"
      + " LEFT JOIN insurance_plan_main pipm ON(pipm.plan_id = ppip.plan_id)"
      + " LEFT JOIN patient_policy_details ppd ON (ppd.mr_no = ppip.mr_no and ppd.status = 'A'"
      + " AND ppip.patient_policy_id = ppd.patient_policy_id and ppip.plan_id = ppd.plan_id)"
      + " LEFT JOIN insurance_company_master picm ON(picm.insurance_co_id = pipm.insurance_co_id)"
      + " LEFT JOIN tpa_master ptpa ON (ptpa.tpa_id = ppip.sponsor_id)"
      + " LEFT JOIN insurance_category_master picam ON (picam.category_id=pipm.category_id)"
      + " LEFT JOIN modules_activated modact ON (modact.module_id = 'mod_adv_ins')"
      + " WHERE pr.patient_id = ?";

  /**
   * Gets the visit insurance details.
   *
   * @param visitId
   *          the visit id
   * @return the visit insurance details
   */
  protected List<BasicDynaBean> getVisitInsuranceDetails(String visitId) {
    Object[] filter = new Object[] { visitId };
    return DatabaseHelper.queryToDynaList(insuranceQuery, filter);
  }

  /** The diag query. */
  private static String diagQuery = "SELECT mrd.icd_code as diagnosis_code, "
      + " mrd.description as diagnosis" + " FROM mrd_diagnosis mrd" + " WHERE mrd.visit_id = ? "
      + " AND mrd.diag_type IS NOT NULL" + " AND mrd.diag_type != ''" + " ORDER BY diag_type";

  /**
   * Gets the visit diag details.
   *
   * @param visitId
   *          the visit id
   * @return the visit diag details
   */
  protected List<BasicDynaBean> getVisitDiagDetails(String visitId) {
    Object[] filter = new Object[] { visitId };
    return DatabaseHelper.queryToDynaList(diagQuery, filter);
  }

  /** The allergy query. */
  private static String allergyQuery = "SELECT "
      + " CASE "
      + "   WHEN atm.allergy_type_code = 'M' THEN 'Medicine'::text "
      + "   WHEN atm.allergy_type_code = 'F' THEN 'Food'::text "
      + "   WHEN atm.allergy_type_code = 'O' THEN 'Others' "
      + "   WHEN atm.allergy_type_code IS NULL THEN 'No Known Allergies'::text "
      + " END as allergy_type, "
      + "   COALESCE(am.allergen_description,gn.generic_name) as allergy, "
      + "   reaction, "
      + "   severity "
      + " FROM patient_section_details psd "
      + " JOIN patient_allergies pa "
      + " USING(section_detail_id) "
      + " LEFT JOIN allergy_type_master atm "
      + " ON atm.allergy_type_id = pa.allergy_type_id "
      + " LEFT JOIN allergen_master am "
      + " ON am.allergen_code_id = pa.allergen_code_id "
      + " LEFT JOIN generic_name gn ON (gn.allergen_code_id = pa.allergen_code_id)"
      + " WHERE patient_id = ?";

  /**
   * Gets the visit allergy details.
   *
   * @param visitId
   *          the visit id
   * @return the visit allergy details
   */
  protected List<BasicDynaBean> getVisitAllergyDetails(String visitId) {
    Object[] filter = new Object[] { visitId };
    return DatabaseHelper.queryToDynaList(allergyQuery, filter);
  }

  /**
   * Gets the current time stamp.
   *
   * @return the current time stamp
   */
  protected String getCurrentTimeStamp() {
    return new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
  }

  /**
   * Gets the parsed message.
   *
   * @param message
   *          the message
   * @return the parsed message
   * @throws HL7Exception
   *           the HL 7 exception
   */
  protected String getParsedMessage(Message message) throws HL7Exception {
    HapiContext context = new DefaultHapiContext();
    Parser parser = context.getPipeParser();
    String encodedMessage = parser.encode(message);
    logger.debug(encodedMessage);
    return encodedMessage;
  }
}

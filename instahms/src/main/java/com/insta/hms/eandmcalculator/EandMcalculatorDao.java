package com.insta.hms.eandmcalculator;

import com.bob.hms.common.DataBaseUtil;
import com.insta.hms.common.AppInit;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.emr.EMRDoc;
import com.insta.hms.emr.EMRInterface;
import com.insta.hms.genericdocuments.GenericDocumentsFields;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.outpatient.DoctorConsultationDAO;
import com.lowagie.text.DocumentException;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import org.apache.commons.beanutils.BasicDynaBean;

import java.io.IOException;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPathExpressionException;

// TODO: Auto-generated Javadoc
/**
 * The Class EandMcalculatorDao.
 */
public class EandMcalculatorDao {

  /** The query params map. */
  private static LinkedHashMap<String, LinkedList<String>> queryParamsMap =
      new LinkedHashMap<String, LinkedList<String>>();

  /** The field types. */
  private static LinkedList<String> fieldTypes = new LinkedList<String>();

  /** The history names. */
  private static LinkedList<String> historyNames = new LinkedList<String>();

  /** The form types. */
  private static LinkedList<LinkedList<String>> formTypes = new LinkedList<LinkedList<String>>();

  /** The eandm code map. */
  private static HashMap<String, HashMap<String, HashMap<String, ArrayList<Integer>>>> emcodeMap =
      new HashMap<String, HashMap<String, HashMap<String, ArrayList<Integer>>>>();

  /** The eandm code map for exam and MDM. */
  private static HashMap<String, HashMap<String, HashMap<String, ArrayList<Integer>>>> mdmMap =
      new HashMap<String, HashMap<String, HashMap<String, ArrayList<Integer>>>>();

  static {

    fieldTypes.add("checkbox");
    fieldTypes.add("wide text, text");
    fieldTypes.add("dropdown");

    LinkedList<String> list1 = new LinkedList<String>();
    list1.add("History of Present Illness");
    list1.add("HPI");
    formTypes.add(list1);

    LinkedList<String> list2 = new LinkedList<String>();
    list2.add("Review Of Systems");
    list2.add("ROS");
    formTypes.add(list2);

    LinkedList<String> list3 = new LinkedList<String>();
    list3.add("Personal, Family & Social History");
    list3.add("PFSH");
    formTypes.add(list3);

    LinkedList<String> list4 = new LinkedList<String>();
    list4.add("Physical Examination");
    list4.add("PE");
    formTypes.add(list4);

    historyNames.add("PFSH");
    historyNames.add("ROS");
    historyNames.add("HPI");

    ArrayList<Integer> hpiLevelFor1to3 = new ArrayList<Integer>();
    hpiLevelFor1to3.add(99202);
    hpiLevelFor1to3.add(99201);

    ArrayList<Integer> hpiLevelFor4orMore = new ArrayList<Integer>();
    hpiLevelFor4orMore.add(99205);
    hpiLevelFor4orMore.add(99204);
    hpiLevelFor4orMore.add(99203);

    HashMap<String, ArrayList<Integer>> hpiEmlevelsForNew =
        new HashMap<String, ArrayList<Integer>>();
    hpiEmlevelsForNew.put("1 to 3", hpiLevelFor1to3);
    hpiEmlevelsForNew.put("4 or More", hpiLevelFor4orMore);

    ArrayList<Integer> rosLevelFor10OrMore = new ArrayList<Integer>();
    rosLevelFor10OrMore.add(99205);
    rosLevelFor10OrMore.add(99204);

    ArrayList<Integer> rosLevelFor2To9 = new ArrayList<Integer>();
    rosLevelFor2To9.add(99203);

    ArrayList<Integer> rosLevelFor1 = new ArrayList<Integer>();
    rosLevelFor1.add(99202);

    ArrayList<Integer> rosLevelForNoneed = new ArrayList<Integer>();
    rosLevelForNoneed.add(99201);

    HashMap<String, ArrayList<Integer>> rosEmlevelsForNew =
        new HashMap<String, ArrayList<Integer>>();
    rosEmlevelsForNew.put("10 or more", rosLevelFor10OrMore);
    rosEmlevelsForNew.put("2 to 9", rosLevelFor2To9);
    rosEmlevelsForNew.put("1", rosLevelFor1);
    rosEmlevelsForNew.put("No Need", rosLevelForNoneed);

    ArrayList<Integer> pfshLevelFor3 = new ArrayList<Integer>();
    pfshLevelFor3.add(99205);
    pfshLevelFor3.add(99204);

    ArrayList<Integer> pfshLevelFor1 = new ArrayList<Integer>();
    pfshLevelFor1.add(99203);

    ArrayList<Integer> pfshLevelForNoneed = new ArrayList<Integer>();
    pfshLevelForNoneed.add(99202);
    pfshLevelForNoneed.add(99201);

    HashMap<String, ArrayList<Integer>> pfshEmlevelsForNew =
        new HashMap<String, ArrayList<Integer>>();
    pfshEmlevelsForNew.put("3", pfshLevelFor3);
    pfshEmlevelsForNew.put("1", pfshLevelFor1);
    pfshEmlevelsForNew.put("No Need", pfshLevelForNoneed);

    HashMap<String, HashMap<String, ArrayList<Integer>>> historyMapForNew =
        new HashMap<String, HashMap<String, ArrayList<Integer>>>();
    historyMapForNew.put("HPI", hpiEmlevelsForNew);
    historyMapForNew.put("ROS", rosEmlevelsForNew);
    historyMapForNew.put("PFSH", pfshEmlevelsForNew);

    emcodeMap.put("N", historyMapForNew);

    ArrayList<Integer> examLevelFor1 = new ArrayList<Integer>();
    examLevelFor1.add(99201);

    ArrayList<Integer> examLevelFor2to4 = new ArrayList<Integer>();
    examLevelFor2to4.add(99202);

    ArrayList<Integer> examLevelFor5to7 = new ArrayList<Integer>();
    examLevelFor5to7.add(99203);

    ArrayList<Integer> examLevelFor8OrMore = new ArrayList<Integer>();
    examLevelFor8OrMore.add(99205);
    examLevelFor8OrMore.add(99204);

    HashMap<String, ArrayList<Integer>> examEmlevelsForNew =
        new HashMap<String, ArrayList<Integer>>();
    examEmlevelsForNew.put("8 or more", examLevelFor8OrMore);
    examEmlevelsForNew.put("5 to 7", examLevelFor5to7);
    examEmlevelsForNew.put("2 to 4", examLevelFor2to4);
    examEmlevelsForNew.put("1", examLevelFor1);

    ArrayList<Integer> mdmLevelForHigh = new ArrayList<Integer>();
    mdmLevelForHigh.add(99205);
    ArrayList<Integer> mdmLevelForModerate = new ArrayList<Integer>();
    mdmLevelForModerate.add(99204);
    ArrayList<Integer> mdmLevelForLow = new ArrayList<Integer>();
    mdmLevelForLow.add(99203);
    ArrayList<Integer> mdmLevelForStraight = new ArrayList<Integer>();
    mdmLevelForStraight.add(99202);
    mdmLevelForStraight.add(99201);

    HashMap<String, ArrayList<Integer>> mdmEmlevelsForNew =
        new HashMap<String, ArrayList<Integer>>();
    mdmEmlevelsForNew.put("High", mdmLevelForHigh);
    mdmEmlevelsForNew.put("Moderate", mdmLevelForModerate);
    mdmEmlevelsForNew.put("Low", mdmLevelForLow);
    mdmEmlevelsForNew.put("Straight Forward", mdmLevelForStraight);

    HashMap<String, HashMap<String, ArrayList<Integer>>> eandmlevelsForExamAndMdmN =
        new HashMap<String, HashMap<String, ArrayList<Integer>>>();
    eandmlevelsForExamAndMdmN.put("EXAM", examEmlevelsForNew);
    eandmlevelsForExamAndMdmN.put("MDM", mdmEmlevelsForNew);

    mdmMap.put("N", eandmlevelsForExamAndMdmN);

    /* For ESTABLISHED */

    ArrayList<Integer> hpiLevelFor1to3E = new ArrayList<Integer>();
    hpiLevelFor1to3E.add(99213);
    hpiLevelFor1to3E.add(99212);
    // hpiLevelFor1to3E.add(99211);

    ArrayList<Integer> hpiLevelFor4orMoreE = new ArrayList<Integer>();
    hpiLevelFor4orMoreE.add(99215);
    hpiLevelFor4orMoreE.add(99214);

    HashMap<String, ArrayList<Integer>> hpiEmlevelsForEstablished =
        new HashMap<String, ArrayList<Integer>>();
    hpiEmlevelsForEstablished.put("1 to 3", hpiLevelFor1to3E);
    hpiEmlevelsForEstablished.put("4 or More", hpiLevelFor4orMoreE);

    ArrayList<Integer> rosLevelFor10OrMoreE = new ArrayList<Integer>();
    rosLevelFor10OrMoreE.add(99215);

    ArrayList<Integer> rosLevelFor2To9E = new ArrayList<Integer>();
    rosLevelFor2To9E.add(99214);

    ArrayList<Integer> rosLevelFor1E = new ArrayList<Integer>();
    rosLevelFor1E.add(99213);

    ArrayList<Integer> rosLevelForNoneedE = new ArrayList<Integer>();
    rosLevelForNoneedE.add(99212);

    HashMap<String, ArrayList<Integer>> rosEmlevelsForEstablished =
        new HashMap<String, ArrayList<Integer>>();
    rosEmlevelsForEstablished.put("10 or more", rosLevelFor10OrMoreE);
    rosEmlevelsForEstablished.put("2 to 9", rosLevelFor2To9E);
    rosEmlevelsForEstablished.put("1", rosLevelFor1E);
    rosEmlevelsForEstablished.put("No Need", rosLevelForNoneedE);

    ArrayList<Integer> pfshLevelFor2to3E = new ArrayList<Integer>();
    pfshLevelFor2to3E.add(99215);

    ArrayList<Integer> pfshLevelFor1E = new ArrayList<Integer>();
    pfshLevelFor1E.add(99214);

    ArrayList<Integer> pfshLevelForNoneedE = new ArrayList<Integer>();
    pfshLevelForNoneedE.add(99213);
    pfshLevelForNoneedE.add(99212);

    HashMap<String, ArrayList<Integer>> pfshEmlevelsForEstablished =
        new HashMap<String, ArrayList<Integer>>();
    pfshEmlevelsForEstablished.put("2 to 3", pfshLevelFor2to3E);
    pfshEmlevelsForEstablished.put("1", pfshLevelFor1E);
    pfshEmlevelsForEstablished.put("No Need", pfshLevelForNoneedE);

    HashMap<String, HashMap<String, ArrayList<Integer>>> historyMapForEstablished =
        new HashMap<String, HashMap<String, ArrayList<Integer>>>();
    historyMapForEstablished.put("HPI", hpiEmlevelsForEstablished);
    historyMapForEstablished.put("ROS", rosEmlevelsForEstablished);
    historyMapForEstablished.put("PFSH", pfshEmlevelsForEstablished);

    emcodeMap.put("E", historyMapForEstablished);

    ArrayList<Integer> examLevelFor8OrMoreE = new ArrayList<Integer>();
    examLevelFor8OrMoreE.add(99215);

    ArrayList<Integer> examLevelFor5to7E = new ArrayList<Integer>();
    examLevelFor5to7E.add(99214);

    ArrayList<Integer> examLevelFor2to4E = new ArrayList<Integer>();
    examLevelFor2to4E.add(99213);

    ArrayList<Integer> examLevelFor1E = new ArrayList<Integer>();
    examLevelFor1E.add(99212);

    HashMap<String, ArrayList<Integer>> examEmlevelsForEstablished =
        new HashMap<String, ArrayList<Integer>>();
    examEmlevelsForEstablished.put("8 or more", examLevelFor8OrMoreE);
    examEmlevelsForEstablished.put("5 to 7", examLevelFor5to7E);
    examEmlevelsForEstablished.put("2 to 4", examLevelFor2to4E);
    examEmlevelsForEstablished.put("1", examLevelFor1E);

    ArrayList<Integer> mdmLevelForHighE = new ArrayList<Integer>();
    mdmLevelForHighE.add(99215);
    ArrayList<Integer> mdmLevelForModerateE = new ArrayList<Integer>();
    mdmLevelForModerateE.add(99214);
    ArrayList<Integer> mdmLevelForLowE = new ArrayList<Integer>();
    mdmLevelForLowE.add(99213);
    ArrayList<Integer> mdmLevelForStraightE = new ArrayList<Integer>();
    mdmLevelForStraightE.add(99212);

    HashMap<String, ArrayList<Integer>> mdmEmlevelsForEstablished =
        new HashMap<String, ArrayList<Integer>>();
    mdmEmlevelsForEstablished.put("High", mdmLevelForHighE);
    mdmEmlevelsForEstablished.put("Moderate", mdmLevelForModerateE);
    mdmEmlevelsForEstablished.put("Low", mdmLevelForLowE);
    mdmEmlevelsForEstablished.put("Straight Forward", mdmLevelForStraightE);

    HashMap<String, HashMap<String, ArrayList<Integer>>> eandmlevelsForExamAndMdmE =
        new HashMap<String, HashMap<String, ArrayList<Integer>>>();
    eandmlevelsForExamAndMdmE.put("EXAM", examEmlevelsForEstablished);
    eandmlevelsForExamAndMdmE.put("MDM", mdmEmlevelsForEstablished);

    mdmMap.put("E", eandmlevelsForExamAndMdmE);

    /* For EMERGENCY */

    ArrayList<Integer> hpiLevelFor1to3Em = new ArrayList<Integer>();
    hpiLevelFor1to3Em.add(99283);
    hpiLevelFor1to3Em.add(99282);
    hpiLevelFor1to3Em.add(99281);

    ArrayList<Integer> hpiLevelFor4orMoreEm = new ArrayList<Integer>();
    hpiLevelFor4orMoreEm.add(99285);
    hpiLevelFor4orMoreEm.add(99284);

    HashMap<String, ArrayList<Integer>> hpiEmlevelsForEmergency =
        new HashMap<String, ArrayList<Integer>>();
    hpiEmlevelsForEmergency.put("1 to 3", hpiLevelFor1to3Em);
    hpiEmlevelsForEmergency.put("4 or More", hpiLevelFor4orMoreEm);

    ArrayList<Integer> rosLevelFor10OrMoreEm = new ArrayList<Integer>();
    rosLevelFor10OrMoreEm.add(99285);

    ArrayList<Integer> rosLevelFor2To9Em = new ArrayList<Integer>();
    rosLevelFor2To9Em.add(99284);

    ArrayList<Integer> rosLevelFor1Em = new ArrayList<Integer>();
    rosLevelFor1Em.add(99283);
    rosLevelFor1Em.add(99282);

    ArrayList<Integer> rosLevelForNoneedEm = new ArrayList<Integer>();
    rosLevelForNoneedEm.add(99281);

    HashMap<String, ArrayList<Integer>> rosEmlevelsForEmergency =
        new HashMap<String, ArrayList<Integer>>();
    rosEmlevelsForEmergency.put("10 or more", rosLevelFor10OrMoreEm);
    rosEmlevelsForEmergency.put("2 to 9", rosLevelFor2To9Em);
    rosEmlevelsForEmergency.put("1", rosLevelFor1Em);
    rosEmlevelsForEmergency.put("No Need", rosLevelForNoneedEm);

    ArrayList<Integer> pfshLevelFor2to3Em = new ArrayList<Integer>();
    pfshLevelFor2to3Em.add(99285);

    ArrayList<Integer> pfshLevelFor1Em = new ArrayList<Integer>();
    pfshLevelFor1Em.add(99284);

    ArrayList<Integer> pfshLevelForNoneedEm = new ArrayList<Integer>();
    pfshLevelForNoneedEm.add(99283);
    pfshLevelForNoneedEm.add(99282);
    pfshLevelForNoneedEm.add(99281);

    HashMap<String, ArrayList<Integer>> pfshEmlevelsForEmergency =
        new HashMap<String, ArrayList<Integer>>();
    pfshEmlevelsForEmergency.put("2 to 3", pfshLevelFor2to3Em);
    pfshEmlevelsForEmergency.put("1", pfshLevelFor1Em);
    pfshEmlevelsForEmergency.put("No Need", pfshLevelForNoneedEm);

    HashMap<String, HashMap<String, ArrayList<Integer>>> historyMapForEmergency =
        new HashMap<String, HashMap<String, ArrayList<Integer>>>();
    historyMapForEmergency.put("HPI", hpiEmlevelsForEmergency);
    historyMapForEmergency.put("ROS", rosEmlevelsForEmergency);
    historyMapForEmergency.put("PFSH", pfshEmlevelsForEmergency);

    emcodeMap.put("M", historyMapForEmergency);

    ArrayList<Integer> examLevelFor8OrMoreEm = new ArrayList<Integer>();
    examLevelFor8OrMoreEm.add(99285);

    ArrayList<Integer> examLevelFor5to7Em = new ArrayList<Integer>();
    examLevelFor5to7Em.add(99284);

    ArrayList<Integer> examLevelFor2to4Em = new ArrayList<Integer>();
    examLevelFor2to4Em.add(99283);
    examLevelFor2to4Em.add(99282);

    ArrayList<Integer> examLevelFor1Em = new ArrayList<Integer>();
    examLevelFor1Em.add(99281);

    HashMap<String, ArrayList<Integer>> examEmlevelsForEmergency =
        new HashMap<String, ArrayList<Integer>>();
    examEmlevelsForEmergency.put("8 or more", examLevelFor8OrMoreEm);
    examEmlevelsForEmergency.put("5 to 7", examLevelFor5to7Em);
    examEmlevelsForEmergency.put("2 to 4", examLevelFor2to4Em);
    examEmlevelsForEmergency.put("1", examLevelFor1Em);

    ArrayList<Integer> mdmLevelForHighEm = new ArrayList<Integer>();
    mdmLevelForHighEm.add(99285);
    ArrayList<Integer> mdmLevelForModerateEm = new ArrayList<Integer>();
    mdmLevelForModerateEm.add(99284);
    mdmLevelForModerateEm.add(99283);
    ArrayList<Integer> mdmLevelForLowEm = new ArrayList<Integer>();
    mdmLevelForLowEm.add(99282);
    ArrayList<Integer> mdmLevelForStraightEm = new ArrayList<Integer>();
    mdmLevelForStraightEm.add(99281);

    HashMap<String, ArrayList<Integer>> mdmEmlevelsForEmergency =
        new HashMap<String, ArrayList<Integer>>();
    mdmEmlevelsForEmergency.put("High", mdmLevelForHighEm);
    mdmEmlevelsForEmergency.put("Moderate", mdmLevelForModerateEm);
    mdmEmlevelsForEmergency.put("Low", mdmLevelForLowEm);
    mdmEmlevelsForEmergency.put("Straight Forward", mdmLevelForStraightEm);

    HashMap<String, HashMap<String, ArrayList<Integer>>> eandmlevelsForExamAndMdmEm =
        new HashMap<String, HashMap<String, ArrayList<Integer>>>();
    eandmlevelsForExamAndMdmEm.put("EXAM", examEmlevelsForEmergency);
    eandmlevelsForExamAndMdmEm.put("MDM", mdmEmlevelsForEmergency);

    mdmMap.put("M", eandmlevelsForExamAndMdmEm);

  }

  /** The count. */
  private static String COUNT =
      " SELECT SUM(CASE WHEN EXISTS " + "(SELECT field_id FROM patient_section_fields pfv "
          + " LEFT JOIN patient_section_options po using (field_detail_id) "
          + " LEFT JOIN patient_section_image_details img "
          + " on (pfv.field_detail_id=img.field_detail_id) "
          + " JOIN patient_section_details fdet USING (section_detail_id) "
          + " JOIN section_master sm ON (sm.section_id=fdet.section_id) "
          + " WHERE (sm.section_title ilike ? OR sm.section_title ilike ?) AND "
          + " pfv.field_id=pfd.field_id and fdet.section_item_id=? " + " AND fdet.item_type='CONS' "
          + " AND (case when field_type in ('text', 'wide text') "
          + " then coalesce(pfv.field_remarks, '') != '' "
          + " when field_type in ('dropdown', 'checkbox') then coalesce(po.available, 'N') = 'Y' "
          + " when field_type = 'date' then pfv.date is not null "
          + " when field_type = 'datetime' then pfv.date_time is not null "
          + " when field_type = 'image' then img.marker_id is not null end) = true" + " ) "
          + " then 1 else 0 end) " + " FROM section_field_desc pfd";

  /**
   * Gets the count map.
   *
   * @param consultationId
   *          the consultation id
   * @param mrNo
   *          the mr no
   * @return the count map
   * @throws SQLException
   *           the SQL exception
   */
  public Map<String, Integer> getCountMap(Integer consultationId, String mrNo) throws SQLException {

    Map<String, Integer> countMap = new HashMap<String, Integer>();
    PreparedStatement pstmt = null;
    Connection con = null;
    try {
      con = DataBaseUtil.getReadOnlyConnection();

      for (List<String> list : formTypes) {

        pstmt = con.prepareStatement(COUNT);
        int count = 1;
        pstmt.setString(count++, list.get(0));
        pstmt.setString(count++, list.get(1));
        pstmt.setInt(count++, consultationId);
        countMap.put(list.get(1), DataBaseUtil.getIntValueFromDb(pstmt));
        DataBaseUtil.closeConnections(null, pstmt);
      }
    } finally {
      DataBaseUtil.closeConnections(con, pstmt);
    }

    return countMap;
  }

  /**
   * Gets the keys for E mcode.
   *
   * @param countType
   *          the count type
   * @param value
   *          the value
   * @param visitType
   *          the visit type
   * @return the keys for E mcode
   */
  public static String getKeysForEMcode(String countType, int value, String visitType) {

    if (countType.equals("HPI")) {
      if (value == 0) {
        return "No Need";
      } else if (value >= 1 && value <= 3) {
        return "1 to 3";
      } else if (value >= 4) {
        return "4 or More";
      }
    }

    if (countType.equals("ROS")) {
      if (value == 0) {
        return "No Need";
      } else if (value == 1) {
        return "1";
      } else if (value >= 2 && value <= 9) {
        return "2 to 9";
      } else if (value >= 9) {
        return "10 or more";
      }
    }

    if (countType.equals("PFSH") && visitType.equals("N")) {
      if (value == 0) {
        return "No Need";
      } else if (value == 1) {
        return "1";
      } else if (value == 3) {
        return "3";
      }
    } else if (countType.equals("PFSH") && (visitType.equals("E") || visitType.equals("Z"))) {
      if (value == 0) {
        return "No Need";
      } else if (value == 1) {
        return "1";
      } else if (value >= 2 && value <= 3) {
        return "2 to 3";
      }
    }

    if (countType.equals("EXAM")) {
      if (value == 1) {
        return "1";
      } else if (value >= 2 && value <= 4) {
        return "2 to 4";
      } else if (value >= 5 && value <= 7) {
        return "5 to 7";
      } else if (value >= 8) {
        return "8 or more";
      }
    }

    return null;
  }

  /**
   * Gets the e mlevel code.
   *
   * @param visitType
   *          the visit type
   * @param parametersMap
   *          the parameters map
   * @return the e mlevel code
   */
  public static Integer getEMlevelCode(String visitType, HashMap<String, Object> parametersMap) {
    HashMap<String, HashMap<String, ArrayList<Integer>>> eandmlevelsForExamAndMdm = null;
    HashMap<String, ArrayList<Integer>> tempMap = null;
    ArrayList<Integer> tempList = null;
    Integer mdmValue = null;
    Integer tempMdmValue = null;
    Object key = null;
    if (visitType != null && !visitType.equals("")) {
      eandmlevelsForExamAndMdm = mdmMap.get(visitType);
      tempMap = eandmlevelsForExamAndMdm.get("MDM");
      key = parametersMap.get("MDM");
      tempList = tempMap.get(key);
      mdmValue = tempList.get(0);

      tempMap = eandmlevelsForExamAndMdm.get("EXAM");
      key = parametersMap.get("EXAM");
      key = getKeysForEMcode("EXAM", (Integer) key, visitType);
      if (key != null) {
        tempList = tempMap.get(key);
        for (Integer value : tempList) {
          if (value <= mdmValue) {
            mdmValue = value;
            break;
          }
        }
      }

      eandmlevelsForExamAndMdm = emcodeMap.get(visitType);
      for (String historyName : historyNames) {
        tempMap = eandmlevelsForExamAndMdm.get(historyName);
        key = parametersMap.get(historyName);
        key = getKeysForEMcode(historyName, (Integer) key, visitType);
        tempList = tempMap.get(key);
        if (tempList != null) {
          for (Integer value : tempList) {
            if (value <= mdmValue) { // iterate tempList if <= we will stop that iteration
              mdmValue = value;
              break;
            }
          }
        }
      }

    }

    return mdmValue;
  }

  /** The Constant GET_FORM. */
  public static final String GET_FORM = "SELECT * FROM section_master WHERE section_title ilike ?";

  /**
   * Checks if is form exist.
   *
   * @param formId
   *          the form id
   * @return the basic dyna bean
   * @throws SQLException
   *           the SQL exception
   */
  public BasicDynaBean isFormExist(String formId) throws SQLException {
    Connection con = null;
    PreparedStatement pstmt = null;
    BasicDynaBean bean = null;

    try {
      con = DataBaseUtil.getReadOnlyConnection();
      pstmt = con.prepareStatement(GET_FORM);
      pstmt.setString(1, formId);

      return DataBaseUtil.queryToDynaBean(pstmt);
    } finally {
      DataBaseUtil.closeConnections(con, pstmt);
    }
  }

  /** The Constant GET_EAMDM_RESULTS. */
  private static final String GET_EAMDM_RESULTS = "SELECT d.doctor_name, dc.patient_id, dc.mr_no,"
      + " dc.consultation_id, cec.user_name, dc.visited_date,pr.reg_date "
      + " FROM consultation_em_calculation cec "
      + " JOIN doctor_consultation dc ON (cec.consultation_id = dc.consultation_id)"
      + " JOIN doctors d ON (d.doctor_id = dc.doctor_name)"
      + " JOIN patient_registration pr ON(dc.patient_id = pr.patient_id)" + " WHERE ";

  /**
   * Gets the eand mcalc EMR docs.
   *
   * @param patientId
   *          the patient id
   * @param mrNo
   *          the mr no
   * @param allVisitsDocuments
   *          the all visits documents
   * @return the eand mcalc EMR docs
   * @throws SQLException
   *           the SQL exception
   * @throws ParseException
   *           the parse exception
   */
  public static List<EMRDoc> getEandMcalcEMRDocs(String patientId, String mrNo,
      Boolean allVisitsDocuments) throws SQLException, ParseException {

    List<BasicDynaBean> list = null;
    if (allVisitsDocuments) {
      list = (List<BasicDynaBean>) DataBaseUtil.queryToDynaList(GET_EAMDM_RESULTS + " dc.mr_no=? ",
          mrNo);
    } else {
      list = (List<BasicDynaBean>) DataBaseUtil
          .queryToDynaList(GET_EAMDM_RESULTS + " dc.patient_id=?", patientId);
    }

    List<EMRDoc> emrDocs = new ArrayList<EMRDoc>();
    BasicDynaBean printpref =
        PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PATIENT);
    int printerId = (Integer) printpref.get("printer_id");
    for (BasicDynaBean b : list) {
      EMRDoc emrDoc = new EMRDoc();

      emrDoc.setPrinterId(printerId);
      String doctor = (String) b.get("doctor_name");
      int consultId = (Integer) b.get("consultation_id");
      emrDoc.setDocid("consultation_id" + consultId);
      emrDoc.setDate((Timestamp) b.get("visited_date"));
      emrDoc.setType("SYS_CONSULT");
      emrDoc.setPdfSupported(true);
      emrDoc.setAuthorized(true);
      emrDoc.setContentType("application/pdf");
      emrDoc.setUpdatedBy((String) b.get("user_name"));
      emrDoc.setDoctor(doctor);
      emrDoc.setTitle("EandM Calculator - " + doctor);
      emrDoc.setDisplayUrl("/eandmcalculator.do?_method=getPrint" + "&consultationId=" + consultId
          + "&printerId=" + printerId);
      emrDoc.setProvider(EMRInterface.Provider.EandMcalculatorResultsProvider);
      emrDoc.setVisitid((String) b.get("patient_id"));
      emrDoc.setVisitDate((java.util.Date) b.get("reg_date"));

      emrDocs.add(emrDoc);
    }
    return emrDocs;
  }

  /**
   * Return bytes.
   *
   * @param docID
   *          the doc ID
   * @param printerID
   *          the printer ID
   * @return the byte[]
   * @throws SQLException
   *           the SQL exception
   * @throws IOException
   *           Signals that an I/O exception has occurred.
   * @throws TemplateException
   *           the template exception
   * @throws DocumentException
   *           the document exception
   * @throws XPathExpressionException
   *           the x path expression exception
   */
  public static byte[] returnBytes(String docID, int printerID) throws SQLException, IOException,
      TemplateException, DocumentException, XPathExpressionException {

    BasicDynaBean consultBean = DoctorConsultationDAO.getConsultDetails(Integer.parseInt(docID));
    Map patientDetails = new HashMap();
    GenericDocumentsFields.copyPatientDetails(patientDetails, null,
        (String) consultBean.get("patient_id"), false);
    Map eandmDetailsMap =
        new GenericDAO("consultation_em_calculation").findByKey("consultation_id", docID).getMap();
    Map ftlParamMap = new HashMap();
    ftlParamMap.put("visitdetails", patientDetails);
    ftlParamMap.put("consultation_bean", consultBean);
    ftlParamMap.put("eandmDetailsMap", eandmDetailsMap);

    Template template = AppInit.getFmConfig().getTemplate("EMcodeCalculationPrint.ftl");
    HtmlConverter htmlConverter = new HtmlConverter();

    StringWriter writer = new StringWriter();
    template.process(ftlParamMap, writer);
    String textContent = writer.toString();

    BasicDynaBean printPref =
        PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PATIENT, printerID);

    return htmlConverter.getPdfBytes(textContent, "EM COde Calculation Print", printPref, false,
        true, true, true, false);

  }

}

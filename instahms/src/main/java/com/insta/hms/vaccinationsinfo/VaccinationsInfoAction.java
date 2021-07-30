package com.insta.hms.vaccinationsinfo;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.ftl.FtlReportGenerator;
import com.insta.hms.integration.configuration.InterfaceEventMappingService;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.master.PrintTemplates.PrintTemplate;
import com.insta.hms.master.PrintTemplates.PrintTemplatesDAO;
import com.insta.hms.mdm.consumptionuom.ConsumptionUOMService;
import com.insta.hms.mdm.medicineroute.MedicineRouteService;
import flexjson.JSONSerializer;
import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.lang3.StringUtils;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.actions.DispatchAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// TODO: Auto-generated Javadoc
/**
 * The Class VaccinationsInfoAction.
 */
public class VaccinationsInfoAction extends DispatchAction {

  /** The dao. */
  VaccinationsInfoDao dao = new VaccinationsInfoDao();

  private static final GenericDAO patientVaccinationDao =
      new GenericDAO("patient_vaccination");
  private static final GenericDAO adverseReactionForVaccinationDAO =
      new GenericDAO("adverse_reaction_for_vaccination");
  private static final GenericDAO adverseReactionSymptomSeverityMappingDAO =
      new GenericDAO("adverse_reaction_symptom_severity_mapping");
  private static final GenericDAO adverseReactionSymptomsListDAO =
      new GenericDAO("adverse_reaction_symptoms_list");

  private static final Logger logger = LoggerFactory.getLogger(VaccinationsInfoAction.class);
  static MedicineRouteService medicineRouteService = ApplicationContextProvider.getBean(
      MedicineRouteService.class);

  static ConsumptionUOMService consumptionUOMService = ApplicationContextProvider.getBean(
      ConsumptionUOMService.class);

  private static final InterfaceEventMappingService interfaceEventMappingService =
      ApplicationContextProvider.getBean(InterfaceEventMappingService.class);


  /**
   * Vaccinations list.
   *
   * @param mapping the mapping
   * @param forml the forml
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws SQLException the SQL exception
   */
  @IgnoreConfidentialFilters
  public ActionForward vaccinationsList(ActionMapping mapping, ActionForm forml,
      HttpServletRequest request, HttpServletResponse response) throws SQLException {

    String patientId = request.getParameter("patient_id");
    String mrNo = VisitDetailsDAO.getMrno(patientId);
    Map patmap = null;
    if (mrNo != null && !mrNo.equals("")) {
      patmap = PatientDetailsDAO.getPatientGeneralDetailsMap(mrNo);
      if (patmap == null) {
        FlashScope flash = FlashScope.getScope(request);
        flash.put("error", mrNo + " doesn't exists.");
        ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
        redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
        return redirect;
      }
    }
    String sortColumn = request.getParameter("sortOrder");
    boolean sortReverse = Boolean.parseBoolean(request.getParameter("sortReverse"));
    List<BasicDynaBean> patientVaccinationsList = dao.getAllPatientVaccinationList(mrNo);
    request.setAttribute("dosageList", dao.getDosageMasterList(mrNo, sortColumn, sortReverse));
    request.setAttribute("patientVaccinationList", ConversionUtils.listBeanToMapListMap(
        patientVaccinationsList, "vaccine_dose_id"));
    List<Integer> adverseReactionIds = new ArrayList<>();
    if (!patientVaccinationsList.isEmpty()) {
      patientVaccinationsList.forEach(
          (vaccineList) -> adverseReactionIds
              .add((Integer) vaccineList.get("adverse_reaction_id")));
      request.setAttribute("vaccineSymptomSeverityMapping",
          ConversionUtils.listBeanToMapListMap(
              dao.getVaccineSymptomSeverity(adverseReactionIds),
              "adverse_reaction_for_vaccination_id"));
    }
    request.setAttribute("symptomsList",
        ConversionUtils.copyListDynaBeansToMap(adverseReactionSymptomsListDAO.listAll()));
    JSONSerializer js = new JSONSerializer().exclude("class");
    String doctors = js.deepSerialize(
        ConversionUtils.copyListDynaBeansToMap(dao.getAllActiveDoctors()));
    request.setAttribute("doctors", doctors);
    request.setAttribute("routeOfAdminList", js.deepSerialize(ConversionUtils.listBeanToListMap(
        medicineRouteService.listAll())));
    request.setAttribute("patientMap", patmap);
    request.setAttribute("mr_no", mrNo);
    return mapping.findForward("vaccinationList");

  }

  /**
   * Prints the vaccinations list.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws Exception the exception
   */
  @SuppressWarnings("unchecked")
  public ActionForward printVaccinationsList(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws Exception {

    String mrNo = request.getParameter("mr_no");
    Map<String, Object> ftlParams = new HashMap();
    List<Map> dosageList = dao.getDosageMasterListForPrint(mrNo);
    Map patientVaccinationList = ConversionUtils.listBeanToMapListMap(
        dao.getAllPatientVaccinationListForPrint(mrNo), "vaccine_dose_id");
    Map patientDetails = dao.getPatientDetails(mrNo);

    List<Map> ftlParamsList = new ArrayList();
    for (int i = 0; i < dosageList.size(); i++) {
      Map dosage = dosageList.get(i);

      Map ftlParam = new HashMap();
      ftlParam.put("vaccine_name", dosage.get("vaccine_name"));
      ftlParam.put("due_date", dosage.get("due_date"));
      ftlParam.put("dose_num", dosage.get("dose_num"));
      ftlParam.put("recommended_age", dosage.get("recommended_age"));
      ftlParam.put("age_units", dosage.get("screen_age_units"));

      Integer vaccineDoseId = (Integer) dosage.get("vaccine_dose_id");
      List tempList = (List) patientVaccinationList.get(vaccineDoseId);
      Map vaccinationDetails = null;

      if (tempList != null) {
        vaccinationDetails = (Map) tempList.get(0);
      }

      if (vaccinationDetails != null) {

        ftlParam.put("expiry_date", vaccinationDetails.get("expiry_date"));
        ftlParam.put("vaccination_datetime", vaccinationDetails.get("vaccination_datetime"));
        ftlParam.put("vaccination_status", vaccinationDetails.get("vaccination_status"));
        ftlParam.put("reason_for_not", vaccinationDetails.get("reason_for_not"));
        ftlParam.put("vacc_doctor_name", vaccinationDetails.get("vacc_doctor_name"));
        ftlParam.put("concatenatedlbl", vaccinationDetails.get("concatenatedlbl"));
        ftlParam.put("remarks", vaccinationDetails.get("remarks"));
        ftlParam.put("med_name", vaccinationDetails.get("med_name"));
        ftlParam.put("manufacturer", vaccinationDetails.get("manufacturer"));
        ftlParam.put("batch", vaccinationDetails.get("batch"));
        ftlParam.put("expiry_date", vaccinationDetails.get("expiry_date"));
      }
      if ((dosage.get("dose_status").equals("I") && vaccinationDetails != null)
          || dosage.get("dose_status").equals("A")) {
        ftlParamsList.add(ftlParam);
      }
    }

    Map patientVisitDetails = (Map) request.getAttribute("patient");
    if (patientVisitDetails == null) {
      patientVisitDetails = PatientDetailsDAO.getPatientGeneralDetailsMap(mrNo);
    }

    ftlParams.put("mr_no", mrNo);
    ftlParams.put("vaccinations", ftlParamsList);
    ftlParams.put("patient_details", patientVisitDetails);
    FtlReportGenerator ftlGen = null;
    PrintTemplate template = PrintTemplate.Vaccination;
    String templateContent = new PrintTemplatesDAO().getCustomizedTemplate(template);
    StringWriter writer = new StringWriter();
    if (templateContent == null || templateContent.equals("")) {
      ftlGen = new FtlReportGenerator(template.getFtlName());
    } else {
      StringReader reader = new StringReader(templateContent);
      ftlGen = new FtlReportGenerator(template.getFtlName(), reader);
    }
    ftlGen.setReportParams(ftlParams);
    ftlGen.process(writer);
    StringBuilder html = new StringBuilder(writer.toString());
    writer.close();

    String outString = html.toString();
    BasicDynaBean printPrefs = null;
    Boolean isDuplicate = false;
    HtmlConverter hc = new HtmlConverter();
    Integer centerId = (Integer) request.getSession(false).getAttribute("centerId");

    if (centerId == null || centerId.equals("")) {
      centerId = 0;
    }

    printPrefs = PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PATIENT);
    String requestedMode = (String) printPrefs.get("print_mode");
    OutputStream os = response.getOutputStream();

    if (requestedMode.equals("P")) {
      response.setContentType("application/pdf");
      boolean repeatPatientHeader = ((String) printPrefs.get("repeat_patient_info"))
          .equalsIgnoreCase("Y");

      hc.writePdf(os, outString, "Vaccination Report", printPrefs, false, repeatPatientHeader,
          true, true, true, isDuplicate, centerId);
      os.close();
      return null;

    } else {

      String textReport = new String(hc.getText(outString, "Vaccination Report", printPrefs, true,
          true, centerId));
      request.setAttribute("textReport", textReport);
      request.setAttribute("textColumns", printPrefs.get("text_mode_column"));
      request.setAttribute("printerType", "DMP");
      return mapping.findForward("textPrintApplet");
    }

  }

  /**
   * Save vaccinations.
   *
   * @param mapping the mapping
   * @param form the form
   * @param request the request
   * @param response the response
   * @return the action forward
   * @throws Exception the exception
   */
  public ActionForward saveVaccinations(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws Exception {

    Connection con = null;
    boolean success = true;
    Map paramMap = request.getParameterMap();
    Map duplicateMap = new HashMap();
    duplicateMap.putAll(paramMap);
    String[] isNew = request.getParameterValues("isNew");
    String[] vaccinationStatus = request.getParameterValues("vaccination_status");
    String[] dbVaccinationStatus = request.getParameterValues("db_vaccination_status");
    String[] isEdited = request.getParameterValues("isEdited");
    String[] isAdministeredOutsideHospital = request.getParameterValues("isOutsideHospital");
    HashMap<String, Integer> keys = new HashMap<String, Integer>();
    String mrNo = request.getParameter("mr_no");
    String formLevelPatientId = request.getParameter("visit_id");

    ActionRedirect redirect = new ActionRedirect(mapping.findForward("showRedirect"));
    redirect.addParameter("patient_id", formLevelPatientId);
    FlashScope flash = FlashScope.getScope(request);
    java.sql.Timestamp modTime = new java.sql.Timestamp(new java.util.Date().getTime());
    String userName = (String) request.getSession(false).getAttribute("userId");
    List<BasicDynaBean> symptomsList = adverseReactionSymptomsListDAO.listAll();

    if (request.getParameter("save").equals("Save & Print")) {

      List<String> printURLs = new ArrayList<String>();
      StringBuilder url = new StringBuilder(
          "VaccinationInfo.do?_method=printVaccinationsList&mr_no=" + mrNo);
      printURLs.add(url.toString());
      request.getSession(false).setAttribute("printURLs", printURLs);
    }

    String[] adverseStartDate = request.getParameterValues("adverse_reaction_start_date");
    String[] adverseStartTime = request.getParameterValues("adverse_reaction_start_time");
    String[] adverseEndDate = request.getParameterValues("adverse_reaction_end_date");
    String[] adverseEndTime = request.getParameterValues("adverse_reaction_end_time");
    String[] administeredDateArray = request.getParameterValues("vaccination_date");
    String[] administeredTimeArray = request.getParameterValues("vaccination_time");
    
    List<Integer> newlyAddedVaccineList = new ArrayList<>();
    List<Integer> updatedVaccineList = new ArrayList<>();

    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      if (isNew != null) {
        for (int i = 0; i < isNew.length; i++) {
          if (vaccinationStatus[i] != null) {
            BasicDynaBean bean = patientVaccinationDao.getBean();
            BasicDynaBean adverseReactionMonitoringBean =
                adverseReactionForVaccinationDAO.getBean();
            ConversionUtils.copyIndexToDynaBean(duplicateMap, i, bean, null, false, null);
            ConversionUtils.copyIndexToDynaBean(duplicateMap, i, adverseReactionMonitoringBean);

            bean.set("mod_time", modTime);
            String startDate = adverseStartDate[i];
            String startTime = adverseStartTime[i];
            Timestamp startDateTime = null;
            if (!StringUtils.isEmpty(startDate) && !StringUtils.isEmpty(startTime)) {
              startDateTime = DateUtil.parseTimestamp(startDate, startTime);
            }
            adverseReactionMonitoringBean.set("adverse_start_date", startDateTime);

            String endDate = adverseEndDate[i];
            String endTime = adverseEndTime[i];
            Timestamp endDateTime = null;
            if (!StringUtils.isEmpty(endDate) && !StringUtils.isEmpty(endTime)) {
              endDateTime = DateUtil.parseTimestamp(endDate, endTime);
            }
            adverseReactionMonitoringBean.set("adverse_end_date", endDateTime);
            
            BasicDynaBean adverseReactionSymptomMappingBean =
                adverseReactionSymptomSeverityMappingDAO.getBean();
            ConversionUtils.copyIndexToDynaBean(duplicateMap, i, adverseReactionSymptomMappingBean);
            bean.set("mod_user", userName);
            
            boolean isAdverseReactionNew = bean.get("adverse_reaction_id") == null || !(
                (Integer) bean.get("adverse_reaction_id") > 0);

            String administeredDate = administeredDateArray[i];
            String administeredTime = administeredTimeArray[i];
            Timestamp vaccinationDatetime = null;
            if (!StringUtils.isEmpty(administeredDate) && !StringUtils.isEmpty(administeredTime)) {
              vaccinationDatetime = DateUtil.parseTimestamp(administeredDate, administeredTime);
            }
            bean.set("vaccination_datetime", vaccinationDatetime);
            int patientVaccinationId;
            boolean isVaccinatedOutsideHospital =
                isAdministeredOutsideHospital[i].equalsIgnoreCase("Y");
            if (isNew[i].equalsIgnoreCase("Y") && !vaccinationStatus[i].equals("")) {
              patientVaccinationId = patientVaccinationDao.getNextSequence();
              bean.set("pat_vacc_id", patientVaccinationId);
              bean.set("mr_no", mrNo);
              bean.set("patient_id", isVaccinatedOutsideHospital ? null : formLevelPatientId);
              int adverseReactionId =
                  saveAdverseReaction(bean, adverseReactionMonitoringBean,
                      paramMap, i, symptomsList,true);
              bean.set("adverse_reaction_id", adverseReactionId);

              success &= patientVaccinationDao.insert(con, bean);
              if (!isVaccinatedOutsideHospital) {
                newlyAddedVaccineList.add(patientVaccinationId);
              }
            } else if (!dbVaccinationStatus[i].equals("") && vaccinationStatus[i].equals("")) {
              patientVaccinationId = (Integer) bean.get("pat_vacc_id");
              success &= patientVaccinationDao.delete(con, "pat_vacc_id", patientVaccinationId);
            } else if (isNew[i].equals("N") && !vaccinationStatus[i].equals("")) {
              patientVaccinationId = (Integer) bean.get("pat_vacc_id");
              keys.put("pat_vacc_id", patientVaccinationId);
              int adverseReactionId =
                  saveAdverseReaction(bean, adverseReactionMonitoringBean, paramMap, i,
                      symptomsList ,isAdverseReactionNew);
              bean.set("adverse_reaction_id", adverseReactionId);
              success &= patientVaccinationDao.update(con, bean.getMap(), keys) > 0;
              boolean isVaccinationEdited = Boolean.parseBoolean(isEdited[i]);
              if (isVaccinationEdited && !isVaccinatedOutsideHospital) {
                updatedVaccineList.add(patientVaccinationId);
              }
            }
          }
        }
      }
      if (!newlyAddedVaccineList.isEmpty()) {
        interfaceEventMappingService.vaccinationAddOrEditEvent(formLevelPatientId,
            newlyAddedVaccineList, true);
      }
      
      if (!updatedVaccineList.isEmpty()) {
        interfaceEventMappingService.vaccinationAddOrEditEvent(formLevelPatientId,
            updatedVaccineList, false);
      }
    } finally {
      DataBaseUtil.commitClose(con, success);
    }
    if (success) {
      flash.success("Patient Vaccination details saved successfully..");
    } else {
      flash.error("Failed to save vaccination details..");
    }

    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

    return redirect;
  }

  /**
   * This function saves AdverseReaction.
   * @param patientVaccinationBean patientVaccinationBean
   * @param adverseReactionMonitoringBean adverseReactionForVaccination Mapping
   * @param isInsert if data is new or to update existing
   * @return adverseReactionId
   * @throws SQLException Exception
   */
  private int saveAdverseReaction(
      BasicDynaBean patientVaccinationBean,
      BasicDynaBean adverseReactionMonitoringBean,
      Map parameterMap,
      int index,
      List<BasicDynaBean> symptomList,
      boolean isInsert)
      throws SQLException {
    if (patientVaccinationBean != null) {
      boolean success = true;
      Connection con = null;
      try {
        con = DataBaseUtil.getConnection();
        con.setAutoCommit(false);
        int adverseReactionId;
        if (isInsert) {
          adverseReactionId = adverseReactionForVaccinationDAO.getNextSequence();
          adverseReactionMonitoringBean.set("adverse_reaction_id", adverseReactionId);
          success = adverseReactionForVaccinationDAO.insert(con, adverseReactionMonitoringBean);
          saveSymptomSeverityMapping(adverseReactionId, parameterMap, index, symptomList);
          return  adverseReactionId;
        } else {
          HashMap<String, Integer> adverserReactionKeys = new HashMap<>();
          adverseReactionId = (Integer) patientVaccinationBean.get("adverse_reaction_id");
          adverserReactionKeys.put("adverse_reaction_id", adverseReactionId);
          success = adverseReactionForVaccinationDAO
              .update(con, adverseReactionMonitoringBean.getMap(), adverserReactionKeys) > 0;
          saveSymptomSeverityMapping(adverseReactionId, parameterMap, index, symptomList);
          return  adverseReactionId;
        }
      } catch (Exception ex) {
        logger.error("Error while adding/updating adverse reaction",ex);
      } finally {
        DataBaseUtil.commitClose(con, success);
      }
    }
    return  0;
  }

  private void saveSymptomSeverityMapping(int adverseReactionId, Map paramMap, int index,
      List<BasicDynaBean> symptomsList) throws SQLException {

    Connection con = null;
    boolean success = true;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      for (BasicDynaBean symptom : symptomsList) {
        int symptomId = (Integer) symptom.get("id");
        String symptomNameId = "symptom_name_" + symptom.get("id");
        String uncheckedSymptomId = "to_be_deleted_" + symptomId;
        if (paramMap.containsKey(symptomNameId)) {
          String symptomName =  ((String[])paramMap.get(symptomNameId))[index];
          boolean isSymptomUnchecked = Boolean.parseBoolean(
              ((String[])paramMap.get(uncheckedSymptomId))[index]);
          if (!StringUtils.isEmpty(symptomName)) {

            String occurrencesId = "occurrences_" + symptomId;
            String severityReactionId = "severity_of_reaction_" + symptomId;
            String severityReactionValue = ((String[])paramMap.get(severityReactionId))[index];
            int severityReaction = 0;
            if (!StringUtils.isEmpty(severityReactionValue)) {
              severityReaction = Integer.parseInt(severityReactionValue);
            }

            String adverseReactionSymptomSeverityId =
                ((String[])paramMap.get("adverse_symptom_severity_id_" + symptomId))[index];
            int adverseSymptomSeverityId = 0;
            if (!StringUtils.isEmpty(adverseReactionSymptomSeverityId)) {
              adverseSymptomSeverityId = Integer.parseInt(adverseReactionSymptomSeverityId);
            }

            String numberOfOccurrences = ((String[])paramMap.get(occurrencesId))[index];
            int noOfOccurrences = 0;
            if (!StringUtils.isEmpty(numberOfOccurrences)) {
              noOfOccurrences = Integer.parseInt(((String[])paramMap.get(occurrencesId))[index]);
            }
            BasicDynaBean adverseReactionSymptomMappingBean =
                adverseReactionSymptomSeverityMappingDAO.getBean();
            adverseReactionSymptomMappingBean.set("adverse_reaction_for_vaccination_id",
                adverseReactionId);
            adverseReactionSymptomMappingBean.set("severity_of_reaction_id", severityReaction == 0
                ? null
                : severityReaction);
            adverseReactionSymptomMappingBean.set("adverse_reaction_symptoms_list_id", symptomId);
            adverseReactionSymptomMappingBean.set("number_of_occurrences", noOfOccurrences == 0
                ? null
                : noOfOccurrences);
            if (adverseSymptomSeverityId == 0) {
              adverseSymptomSeverityId = adverseReactionSymptomSeverityMappingDAO.getNextSequence();
              adverseReactionSymptomMappingBean
                  .set("adverse_symptom_severity_id", adverseSymptomSeverityId);
              success = adverseReactionSymptomSeverityMappingDAO
                  .insert(con, adverseReactionSymptomMappingBean);
            } else if (isSymptomUnchecked) {
              success = adverseReactionSymptomSeverityMappingDAO
                  .delete(con, "adverse_symptom_severity_id", adverseSymptomSeverityId);
            } else {
              HashMap<String, Integer> key = new HashMap<>();
              key.put("adverse_symptom_severity_id", adverseSymptomSeverityId);
              success = adverseReactionSymptomSeverityMappingDAO.update(con,
                  adverseReactionSymptomMappingBean.getMap(), key) > 0;
            }
          }
        }
      }
    } catch (Exception ex) {
      logger.error("Error while saving adverseReactionSymptomSeverityMapping ", ex);
    } finally {
      DataBaseUtil.commitClose(con, success);
    }


  }

}

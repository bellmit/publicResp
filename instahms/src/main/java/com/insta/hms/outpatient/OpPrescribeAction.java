package com.insta.hms.outpatient;

import static com.insta.hms.jobs.common.QuartzJobHelper.buildJob;

import com.bob.hms.adminmasters.organization.OrgMasterDao;
import com.bob.hms.common.AutoIncrementId;
import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.Preferences;
import com.bob.hms.common.RequestContext;
import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.Registration.PatientInsurancePlanDAO;
import com.insta.hms.Registration.VisitDetailsDAO;
import com.insta.hms.batchjob.builders.PrescriptionPHRJob;
import com.insta.hms.ceed.CeedDAO;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.ConversionUtils.LISTING;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.JobSchedulingService;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.common.preferences.clinicalpreferences.ClinicalPreferencesService;
import com.insta.hms.eauthorization.EAuthPrescriptionActivitiesDAO;
import com.insta.hms.eauthorization.EAuthPrescriptionDAO;
import com.insta.hms.emr.DIAGProviderBOImpl;
import com.insta.hms.emr.EMRDoc;
import com.insta.hms.emr.OperationProviderBOImpl;
import com.insta.hms.emr.ServiceProviderBOImpl;
import com.insta.hms.erxprescription.ERxPrescriptionDAO;
import com.insta.hms.genericdocuments.DocumentPrintConfigurationsDAO;
import com.insta.hms.instaforms.AbstractInstaForms;
import com.insta.hms.instaforms.ConsultationForms;
import com.insta.hms.instaforms.PatientSectionDetailsDAO;
import com.insta.hms.jobs.JobService;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.ConsultationFavourites.ConsultationFavouritesDAO;
import com.insta.hms.master.ConsultationTypes.ConsultationTypesDAO;
import com.insta.hms.master.DiagnosisCodeFavourites.MRDCodesMasterDAO;
import com.insta.hms.master.FormComponents.FormComponentsDAO;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.HealthAuthorityPreferences.HealthAuthorityPreferencesDAO;
import com.insta.hms.master.ImageMarkers.ImageMarkerDAO;
import com.insta.hms.master.MedicineRoute.MedicineRouteDAO;
import com.insta.hms.master.PerDiemCodes.PerDiemCodesDAO;
import com.insta.hms.master.PhraseSuggestionsMaster.PhraseSuggestionsMasterDAO;
import com.insta.hms.master.PrescriptionsMaster.PrescriptionsMasterDAO;
import com.insta.hms.master.PrescriptionsPrintTemplates.PrescriptionsTemplateDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.master.RecurrenceDailyMaster.RecurrenceDailyMasterDAO;
import com.insta.hms.master.RegistrationPreferences.RegistrationPreferencesDAO;
import com.insta.hms.master.RegistrationPreferences.RegistrationPreferencesDTO;
import com.insta.hms.master.RegularExpression.RegularExpressionDAO;
import com.insta.hms.master.Sections.SectionsDAO;
import com.insta.hms.master.TpaMaster.TpaMasterDAO;
import com.insta.hms.master.outpatient.SystemGeneratedSections;
import com.insta.hms.master.outpatient.SystemGeneratedSectionsDAO;
import com.insta.hms.master.sectionrolerights.SectionRoleRightsDAO;
import com.insta.hms.medicalrecorddepartment.MRDDiagnosisDAO;
import com.insta.hms.medicalrecorddepartment.MRDUpdateScreenDAO;
import com.insta.hms.messaging.MessageUtil;
import com.insta.hms.pbmauthorization.PBMPrescriptionsDAO;
import com.insta.hms.resourcescheduler.ResourceDAO;
import com.insta.hms.stores.GenericDTO;
import com.insta.hms.stores.GenericMasterDAO;
import com.insta.hms.stores.MedicineStockDAO;
import com.insta.hms.stores.PharmacymasterDAO;
import com.insta.hms.usermanager.User;
import com.insta.hms.usermanager.UserDAO;
import com.insta.hms.vaccinationsinfo.VaccinationsInfoDao;
import com.insta.hms.vitalForm.VisitVitalsDAO;
import com.insta.hms.vitalForm.genericVitalFormDAO;
import com.insta.hms.vitalparameter.VitalMasterDAO;
import com.insta.hms.wardactivities.PatientActivitiesDAO;
import com.lowagie.text.DocumentException;

import flexjson.JSONSerializer;

import freemarker.template.TemplateException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.http.HttpHeaders;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;

/**
 * The Class OpPrescribeAction.
 */
public class OpPrescribeAction extends BaseAction {

  /** The log. */
  static Logger log = LoggerFactory.getLogger(OpPrescribeAction.class);

  /** The e auth act dao. */
  static EAuthPrescriptionActivitiesDAO eAuthActDao = new EAuthPrescriptionActivitiesDAO();

  /** The e auth presc DAO. */
  static EAuthPrescriptionDAO eAuthPrescDAO = new EAuthPrescriptionDAO();

  /** The pat med presc DAO. */
  static GenericDAO patMedPrescDAO = new GenericDAO("patient_medicine_prescriptions");

  /** The s dao. */
  static GenericDAO sDao = new GenericDAO("patient_service_prescriptions");

  /** The t dao. */
  static GenericDAO tDao = new GenericDAO("patient_test_prescriptions");

  /** The nh dao. */
  static GenericDAO nhDao = new GenericDAO("patient_other_prescriptions"); // non hospital items

  /** The ot dao. */
  // prescriptions.
  static GenericDAO otDao = new GenericDAO("patient_operation_prescriptions");

  /** The scomplaint dao. */
  static SecondaryComplaintDAO scomplaintDao = new SecondaryComplaintDAO();

  /** The form comp dao. */
  static FormComponentsDAO formCompDao = new FormComponentsDAO();

  /** The phy form desc. */
  static SectionsDAO phyFormDesc = new SectionsDAO();

  /** The progress notesdao. */
  static GenericDAO progressNotesdao = new GenericDAO("progress_notes");

  /** The mm dao. */
  static PrescriptionsMasterDAO mmDao = new PrescriptionsMasterDAO();

  /** The pbm presc DAO. */
  static PBMPrescriptionsDAO pbmPrescDAO = new PBMPrescriptionsDAO();

  /** The patient org DAO. */
  static GenericDAO patientOrgDAO = new GenericDAO("patient_registration");

  /** The consult dao. */
  static DoctorConsultationDAO consultDao = new DoctorConsultationDAO();

  /** The follow up dao. */
  static GenericDAO followUpDao = new GenericDAO("follow_up_details");

  /** The med dosage dao. */
  static GenericDAO medDosageDao = new GenericDAO("medicine_dosage_master");

  /** The pres instruction dao. */
  static GenericDAO presInstructionDao = new GenericDAO("presc_instr_master");

  /** The consult field values dao. */
  static ConsultationFieldValuesDAO consultFieldValuesDao = new ConsultationFieldValuesDAO();

  /** The doctor DAO. */
  static GenericDAO doctorDAO = new GenericDAO("patient_consultation_prescriptions"); // cross

  /** The consult image dao. */
  // consultations
  static DoctorConsultImagesDAO consultImageDao = new DoctorConsultImagesDAO();

  /** The gdao. */
  static genericVitalFormDAO gdao = new genericVitalFormDAO("vital_parameter_master");

  /** The activity dao. */
  static PatientActivitiesDAO activityDao = new PatientActivitiesDAO();

  /** The rdm DAO. */
  static RecurrenceDailyMasterDAO rdmDAO = new RecurrenceDailyMasterDAO();

  /** The visit DAO. */
  static VisitDetailsDAO visitDAO = new VisitDetailsDAO();

  /** The pat det dao. */
  static PatientDetailsDAO patDetDao = new PatientDetailsDAO();

  /** The aller dao. */
  static AllergiesDAO allerDao = new AllergiesDAO();

  /** The heal maint dao. */
  static HealthMaintenanceDAO healMaintDao = new HealthMaintenanceDAO();

  /** The pre anaes dao. */
  static PreAnaesthestheticDAO preAnaesDao = new PreAnaesthestheticDAO();

  /** The user dao. */
  static UserDAO userDao = new UserDAO();

  /** The crown statuses DAO. */
  static CrownStatusesDAO crownStatusesDAO = new CrownStatusesDAO();

  /** The root statuses DAO. */
  static RootStatusesDAO rootStatusesDAO = new RootStatusesDAO();

  /** The sur matrl DAO. */
  static SurfaceMaterialDAO surMatrlDAO = new SurfaceMaterialDAO();

  /** The s units DAO. */
  static GenericDAO sUnitsDAO = new GenericDAO("strength_units");

  /** The route DAO. */
  static MedicineRouteDAO routeDAO = new MedicineRouteDAO();

  /** The erxdao. */
  static ERxPrescriptionDAO erxdao = new ERxPrescriptionDAO();

  /** The vm DAO. */
  static VitalMasterDAO vmDAO = new VitalMasterDAO();

  /** The vv DAO. */
  static VisitVitalsDAO vvDAO = new VisitVitalsDAO();

  /** The pipdao. */
  static PatientInsurancePlanDAO pipdao = new PatientInsurancePlanDAO();

  /**
   * List.
   *
   * @param mapping the mapping
   * @param form    the form
   * @param req     the req
   * @param res     the res
   * @return the action forward
   * @throws SQLException             the SQL exception
   * @throws ParseException           the parse exception
   * @throws IOException              Signals that an I/O exception has occurred.
   * @throws DocumentException        the document exception
   * @throws TemplateException        the template exception
   * @throws TransformerException     the transformer exception
   * @throws XPathExpressionException the x path expression exception
   * @throws Exception                the exception
   */

  public ActionForward list(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws SQLException, ParseException, IOException, DocumentException,
      TemplateException, TransformerException, XPathExpressionException, Exception {
    /*
     * Action to list the prescriptions for the consultation Parameters: consultation_id
     */

    String consIdStr = req.getParameter("consultation_id");
    String mrNo = req.getParameter("mr_no");
    int consId = Integer.parseInt(consIdStr);
    if (null == mrNo || mrNo.equals("")) {
      BasicDynaBean consultBean = consultDao.findByKey("consultation_id", consId);
      mrNo = (String) consultBean.get("mr_no");
    }
    Map modulesActivatedMap = ((Preferences) RequestContext.getSession()
        .getAttribute("preferences")).getModulesActivatedMap();
    String modNewconsStatus = (String) modulesActivatedMap.get("mod_newcons");
    if (null != modNewconsStatus && modNewconsStatus.equals("Y")) {
      BasicDynaBean patientDetailsBean = patDetDao.findByKey("mr_no", mrNo);
      mrNo = (patientDetailsBean.get("original_mr_no") != null
          && !patientDetailsBean.get("original_mr_no").equals(""))
              ? patientDetailsBean.get("original_mr_no").toString()
              : patientDetailsBean.get("mr_no").toString();
      ActionRedirect redirect = new ActionRedirect(
          "/consultation/index.htm#/filter/default/patient/"
              + URLEncoder.encode(mrNo, "UTF-8").replaceAll("\\+", "%20")
              + "/consultation/" + consId + "?retain_route_params=true");
      return redirect;
    }

    log.debug("consultation load start " + new java.util.Date());
    BasicDynaBean consultBean = consultDao.findConsultationExt(consId);
    String visitType = (String) consultBean.get("visit_type");
    // patient has been converted as IP in another browser tab. refer bug 53016
    if (visitType.equals("i")) {
      FlashScope flash = FlashScope.getScope(req);
      flash.error("Patient '" + mrNo
          + "' has been converted to an IP patient. Cannot record an OP consultation.");
      ActionRedirect redirect = new ActionRedirect(mapping.findForward("opListRedirect"));
      redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
      return redirect;
    }

    BasicDynaBean genericPrefs = GenericPreferencesDAO.getAllPrefs();
    req.setAttribute("genericPrefs", genericPrefs.getMap());

    BasicDynaBean clinicalPrefs = ApplicationContextProvider.getApplicationContext()
        .getBean(ClinicalPreferencesService.class).getClinicalPreferences();
    req.setAttribute("clinicalPrefs", clinicalPrefs.getMap());
    String consultationEditAcrossDoctors = visitType.equals("o")
        ? (String) clinicalPrefs.get("op_consultation_edit_across_doctors")
        : (String) genericPrefs.get("ip_consultation_edit_across_doctors");
    consultationEditAcrossDoctors = consultationEditAcrossDoctors == null ? "Y"
        : consultationEditAcrossDoctors;
    Integer roleId = (Integer) req.getSession(false).getAttribute("roleId");
    String consultingDoctorId = (String) consultBean.get("doctor_name");
    consultingDoctorId = consultingDoctorId == null ? "" : consultingDoctorId;
    User user = userDao.getUser((String) req.getSession(false).getAttribute("userid"));
    String loggedInDoctor = user == null ? "" : user.getDoctorId();
    loggedInDoctor = loggedInDoctor == null ? "" : loggedInDoctor;
    if (roleId != 1 && roleId != 2 && (consultationEditAcrossDoctors.equals("N")
        && !consultingDoctorId.equals(loggedInDoctor))) {
      ((HttpServletResponse) res).sendRedirect(req.getContextPath() + "/AccessControlForward.do");
      return null;
    }

    String patientId = (String) consultBean.get("patient_id");
    java.util.Map patient = com.insta.hms.Registration.VisitDetailsDAO
        .getPatientVisitDetailsMap(patientId);

    List<BasicDynaBean> planListBean = pipdao.getPlanDetails(patientId);
    BasicDynaBean patientPlan = null;
    if (planListBean != null && !planListBean.isEmpty()) {
      patientPlan = planListBean.get(0);
    }

    String prescriptionUsesStores = (String) genericPrefs.get("prescription_uses_stores");
    log.debug("Prescription Query Time before : " + new java.util.Date());
    req.setAttribute("prescriptions", PrescriptionsMasterDAO.getAllPrescriptionsWithoutCharges(
        consId, (String) consultBean.get("patient_id"), prescriptionUsesStores, patient));
    log.debug("Prescription Query Time after : " + new java.util.Date());
    Map filterMap = new HashMap<>();
    filterMap.put("status", "A");
    filterMap.put("medication_type", "M");
    req.setAttribute("frequencies", rdmDAO.listAll(null, filterMap, null));

    JSONSerializer js = new JSONSerializer().exclude("class");
    req.setAttribute("routes_list_json", js
        .serialize(ConversionUtils.copyListDynaBeansToMap(routeDAO.listAll(null, "status", "A"))));

    String orgId = (String) consultBean.get("org_id");
    String tpaId = (String) patient.get("primary_sponsor_id");
    tpaId = tpaId == null || tpaId.equals("") ? "-1" : tpaId;

    BasicDynaBean orgDetails = new OrgMasterDao().findByKey("org_id", orgId);
    req.setAttribute("orgDetails", orgDetails);

    req.setAttribute("planList", js.serialize(ConversionUtils.listBeanToListMap(planListBean)));

    boolean multiPlanExists = null != planListBean && planListBean.size() == 2;
    req.setAttribute("multiPlanExists", multiPlanExists);

    boolean modEclaimPreauth = (Boolean) req.getSession(false).getAttribute("mod_eclaim_preauth");
    String tpaRequiresPreAuth = "N";
    String tpaEauthMode = "M";

    /*
     * E-PreAuth is required only when mod_eclaim_preauth module is enabled and TPA has Prior
     * Authorization mode Online, for OP/IP/OSP patients
     */
    if (modEclaimPreauth) {
      BasicDynaBean tpaBean = new TpaMasterDAO().findByKey("tpa_id", tpaId);
      if (tpaBean != null) {
        if (((String) tpaBean.get("pre_auth_mode")).equals("M")
            || ((String) tpaBean.get("pre_auth_mode")).equals("O")) {
          tpaRequiresPreAuth = "Y";
        }
        if (((String) tpaBean.get("pre_auth_mode")).equals("O")) {
          tpaEauthMode = "O";
        }
      }
    }
    req.setAttribute("TPArequiresPreAuth", tpaRequiresPreAuth);
    req.setAttribute("TPAEAuthMode", tpaEauthMode);

    req.setAttribute("patient", patient);
    // send an empty map and handle it in jsp
    req.setAttribute("patientPlan", (null != patientPlan) ? patientPlan.getMap() : new HashMap());
    Calendar cal = Calendar.getInstance(getLocale(req));
    if (consultBean.get("start_datetime") == null) {
      consultBean.set("start_datetime", new java.sql.Timestamp(cal.getTimeInMillis()));
    }

    req.setAttribute("consultation_bean", consultBean.getMap());
    req.setAttribute("antenatal_bean_doctor_id", patient.get("doctor"));
    req.setAttribute("antenatal_bean_doctor_name", patient.get("doctor_name"));

    String toothNumberingSystem = (String) genericPrefs.get("tooth_numbering_system");
    ToothImageDetails adultToothImageDetails = DentalChartHelperDAO.getToothImageDetails(true);
    ToothImageDetails pediacToothImageDetails = DentalChartHelperDAO.getToothImageDetails(false);

    req.setAttribute("adult_tooth_numbers",
        DentalChartHelperDAO.getToothNumbers(toothNumberingSystem, adultToothImageDetails));
    req.setAttribute("pediac_tooth_numbers",
        DentalChartHelperDAO.getToothNumbers(toothNumberingSystem, pediacToothImageDetails));

    Timestamp closingTime = (Timestamp) consultBean.get("consultation_complete_time");
    long noOfSecTillDate = 0;
    if (closingTime != null) {
      long diff = ((new Timestamp(new java.util.Date().getTime())).getTime()
          - closingTime.getTime());
      noOfSecTillDate = diff / (1000);
    }
    req.setAttribute("no_of_sec_till_date", noOfSecTillDate);
    req.setAttribute("followup_bean", followUpDao.findByKey("patient_id", patientId));
    req.setAttribute("visitType", (String) consultBean.get("visit_type"));

    List itemFormList = new GenericDAO("item_form_master").listAll();
    req.setAttribute("itemFormList",
        js.serialize(ConversionUtils.copyListDynaBeansToMap(itemFormList)));
    Integer patientCenterId = (Integer) patient.get("center_id");
    String prescByGenerics = HealthAuthorityPreferencesDAO
        .getHealthAuthorityPreferences(CenterMasterDAO.getHealthAuthorityForCenter(patientCenterId))
        .getPrescriptions_by_generics();
    req.setAttribute("prescriptions_by_generics",
        prescriptionUsesStores.equals("Y") && prescByGenerics.equals("Y"));
    String healthAuthority = CenterMasterDAO.getHealthAuthorityForCenter(patientCenterId);
    String defaultDiagnosisCodeType = HealthAuthorityPreferencesDAO
        .getHealthAuthorityPreferences(healthAuthority).getDiagnosis_code_type();
    req.setAttribute("defaultDiagnosisCodeType", defaultDiagnosisCodeType);

    List<String> favcodesList = new ArrayList<String>();
    favcodesList = MRDCodesMasterDAO.getDoctorFavouriteCodesList(consultingDoctorId,
        defaultDiagnosisCodeType);
    req.setAttribute("favcodesList", favcodesList);
    List medDosages = medDosageDao.listAll();
    req.setAttribute("medDosages",
        js.serialize(ConversionUtils.copyListDynaBeansToMap(medDosages)));
    List presInstructions = presInstructionDao.listAll();
    req.setAttribute("presInstructions",
        js.serialize(ConversionUtils.copyListDynaBeansToMap(presInstructions)));

    String userName = (String) req.getSession(false).getAttribute("userid");
    String noteTakerPreferences = userDao.getPrescriptionNoteTaker(userName);
    req.setAttribute("prescriptionNoteTakerPreferences", noteTakerPreferences);
    req.setAttribute("diagnosis_details", MRDDiagnosisDAO.getAllDiagnosisDetails(patientId));

    req.setAttribute("printTemplate", PrescriptionsTemplateDAO.getTemplateNames());
    req.setAttribute("validate_diagnosis_codification",
        GenericPreferencesDAO.getAllPrefs().get("validate_diagnosis_codification"));

    AbstractInstaForms formDAO = new ConsultationForms();
    BasicDynaBean opform = formDAO.getComponents(req.getParameterMap());
    req.setAttribute("form", opform);
    req.setAttribute("insta_form_json", js.serialize(opform.getMap()));
    req.setAttribute("group_patient_sections",
        formCompDao.findByKey("id", opform.get("form_id")).get("group_patient_sections"));
    req.setAttribute("section_rights", new SectionRoleRightsDAO()
        .getAllSectionsRights((Integer) req.getSession().getAttribute("roleId")));

    List<BasicDynaBean> sectionsDefList = new SectionsDAO()
        .getSections((String) opform.get("sections"));
    req.setAttribute("sectionsDefMap",
        ConversionUtils.listBeanToMapBean(sectionsDefList, "section_id"));

    req.setAttribute("insta_sections", sectionsDefList);
    req.setAttribute("insta_sections_json",
        js.serialize(ConversionUtils.copyListDynaBeansToMap(sectionsDefList)));

    Boolean triageSummary = false;

    for (String comp : ((String) opform.get("sections")).split(",")) {
      Integer sectionId = Integer.parseInt(comp);
      if (sectionId == SystemGeneratedSections.TriageSummary.getSectionId()) {
        triageSummary = true;
      } else if (sectionId == SystemGeneratedSections.ConsultationNotes.getSectionId()) {
        if (noteTakerPreferences != null && noteTakerPreferences.equals("Y")) {
          List<String> imageColumn = new ArrayList<String>();
          imageColumn.add("consultation_id");
          imageColumn.add("content_type");
          imageColumn.add("datetime");
          imageColumn.add("image_id");

          List consImageList = consultImageDao.listAll(imageColumn, "consultation_id", consId);
          req.setAttribute("imageList", consImageList);
        } else {
          int doctorTemplateId = (Integer) consultBean.get("doctor_template_id");
          List consultTemplateFields = DoctorConsultationDAO
              .getConsultationTemplate(doctorTemplateId);
          req.setAttribute("consultFields", consultTemplateFields);
          req.setAttribute("consultFieldValues",
              ConsultationFieldValuesDAO.getConsultationFieldsValues(consId, true, false));
        }
      } else if (sectionId == SystemGeneratedSections.HealthMaintenance.getSectionId()) {
        req.setAttribute("healthMaints",
            formDAO.getHealthMaintenanceRecords((String) consultBean.get("mr_no"), patientId,
                consId, 0, (Integer) opform.get("form_id")));
      } else if (sectionId == SystemGeneratedSections.Antenatal.getSectionId()) {
        req.setAttribute("antenatalinfo",
            formDAO.getAntenatalRecords((String) consultBean.get("mr_no"), patientId, consId, 0,
                (Integer) opform.get("form_id")));

      } else if (sectionId == SystemGeneratedSections.PreAnaesthestheticCheckup.getSectionId()) {
        req.setAttribute("preAnaesthestheticList",
            formDAO.getPreAnaestestheticRecords((String) consultBean.get("mr_no"), patientId,
                consId, 0, (Integer) opform.get("form_id")));

      } else if (sectionId == SystemGeneratedSections.PregnancyHistory.getSectionId()) {
        req.setAttribute("pregnancyhistories",
            formDAO.getPregnancyHistories((String) consultBean.get("mr_no"), patientId, consId, 0,
                (Integer) opform.get("form_id")));
        req.setAttribute("pregnancyhistoriesBean",
            ConversionUtils
                .listBeanToListMap(formDAO.getObstetricrecords((String) consultBean.get("mr_no"),
                    patientId, consId, 0, (Integer) opform.get("form_id"))));

      } else if (sectionId == SystemGeneratedSections.Allergies.getSectionId()) {
        req.setAttribute("allergies", formDAO.getAllergies((String) consultBean.get("mr_no"),
            patientId, consId, 0, (Integer) opform.get("form_id")));

      } else if (sectionId == SystemGeneratedSections.Vitals.getSectionId()) {
        req.setAttribute("referenceList", genericVitalFormDAO.getReferenceRange(patient));
        req.setAttribute("prefColorCodes", GenericPreferencesDAO.getAllPrefs());
        req.setAttribute("all_fields", vmDAO.getActiveVitalParams("O"));
        List readingList = vvDAO.getVitals((String) consultBean.get("patient_id"), null, null, "V",
            patient);
        req.setAttribute("vital_readings", readingList);
        // vital reading exists is defined using above list, anyhow no date filters applied.
        req.setAttribute("vital_reading_exists", !readingList.isEmpty());
        req.setAttribute("latest_vital_reading_json",
            js.deepSerialize(ConversionUtils.copyListDynaBeansToMap(vvDAO.getLatestVitals(
                (String) consultBean.get("mr_no"), (String) consultBean.get("patient_id")))));
        req.setAttribute("height_weight_params",
            js.deepSerialize(ConversionUtils.copyListDynaBeansToMap(vvDAO.getHeightAndWeight(
                (String) consultBean.get("mr_no"), (String) consultBean.get("patient_id")))));
        req.setAttribute("paramType", "V");
      } else if (sectionId == SystemGeneratedSections.Complaint.getSectionId()) {
        req.setAttribute("secondary_complaints", scomplaintDao.getSecondaryComplaints(patientId));
      }
    }

    // markers of fields from all sections.
    List<BasicDynaBean> imageMarkers = new ImageMarkerDAO()
        .getMarkers((String) opform.get("sections"));
    req.setAttribute("sectionsImageMarkers",
        ConversionUtils.listBeanToMapListBean(imageMarkers, "section_id"));

    PatientSectionDetailsDAO psd = new PatientSectionDetailsDAO();
    req.setAttribute("section_finalize_status",
        ConversionUtils.listBeanToMapMap(
            psd.getSections(mrNo, patientId, consId, 0, (Integer) opform.get("form_id")),
            "section_id"));
    log.debug("before triage " + new java.util.Date());
    if (triageSummary) {
      String printerIdStr = req.getParameter("showPrinter");
      BasicDynaBean prefs = null;
      int printerId = 0;
      if ((printerIdStr != null) && !printerIdStr.equals("")) {
        printerId = Integer.parseInt(printerIdStr);
      }
      prefs = PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_PATIENT,
          printerId);

      req.setAttribute("triage_summary",
          new String(new OPPrescriptionFtlHelper().getTriageAndClinicalInfoFtlReport(consId,
              OPPrescriptionFtlHelper.ReturnType.HTML, prefs, null, userName, true, patient)));
    }
    log.debug("after triage " + new java.util.Date());
    req.setAttribute("allDoctorConsultationTypes",
        js.serialize(ConversionUtils.copyListDynaBeansToMap(new ConsultationTypesDAO().listAll())));

    List systemGeneratedSections = ConversionUtils
        .listBeanToListMap(new SystemGeneratedSectionsDAO().listAll());
    req.setAttribute("sys_generated_forms", js.deepSerialize(systemGeneratedSections));
    req.setAttribute("sys_generated_section", systemGeneratedSections);
    String templateName = req.getParameter("templateName");
    if (templateName == null) {
      req.setAttribute("printPresc",
          GenericPreferencesDAO.getAllPrefs().get("default_prescription_print_template"));
    } else {
      req.setAttribute("printPresc", templateName);
    }

    String showPrinter = req.getParameter("showPrinter");
    if (showPrinter == null) {
      Integer printerId = (Integer) DocumentPrintConfigurationsDAO
          .getPrescriptionPrintConfiguration(
              templateName = (templateName == null
                  ? (String) GenericPreferencesDAO.getAllPrefs()
                      .get("default_prescription_print_template")
                  : templateName))
          .get("printer_settings");
      // show default printer.
      req.setAttribute("showPrinter", printerId);
    } else {
      req.setAttribute("showPrinter", showPrinter);
    }

    String printType = req.getParameter("printType");
    if (printType == null) {
      req.setAttribute("printType", "case");
    } else {
      req.setAttribute("printType", printType);
    }

    req.setAttribute("jsonConsultationIds", js.serialize(ConversionUtils.copyListDynaBeansToMap(
        OutPatientDAO.getConsultaionIds((String) patient.get("mr_no"), consId))));

    RegistrationPreferencesDTO regPref = RegistrationPreferencesDAO.getRegistrationPreferences();
    req.setAttribute("regPrefs", regPref);
    List perdiemCodesList = null;
    if (regPref.getAllow_drg_perdiem() != null && regPref.getAllow_drg_perdiem().equals("Y")) {
      perdiemCodesList = PerDiemCodesDAO.getPerDiemCodes();
    }
    req.setAttribute("perdiemCodesList", ConversionUtils.listBeanToListMap(perdiemCodesList));
    req.setAttribute("perdiemCodesListJSON",
        js.serialize(ConversionUtils.copyListDynaBeansToMap(perdiemCodesList)));

    req.setAttribute("consErxBean", erxdao.getLatestConsErxBean(consId));
    int pbmPrescId = erxdao.getErxConsPBMId(consId);
    req.setAttribute("erxBean", erxdao.getConsErxDetails(pbmPrescId));

    java.util.List<BasicDynaBean> phraseSuggestionsByDept = PhraseSuggestionsMasterDAO
        .getPhraseSuggestionsByDeptDynaList((String) patient.get("dept_id"));
    req.setAttribute("phrase_suggestions_by_dept_json", js.deepSerialize(ConversionUtils
        .listBeanToMapListMap(phraseSuggestionsByDept, "phrase_suggestions_category_id")));

    req.setAttribute("strength_units_json", js
        .serialize(ConversionUtils.copyListDynaBeansToMap(sUnitsDAO.listAll(null, "status", "A"))));

    String modCeedIntegrationEnabled = (String) modulesActivatedMap.get("mod_ceed_integration");
    String modVaccination = (String) modulesActivatedMap.get("mod_vaccination");
    if (modVaccination != null && modVaccination.equals("Y")) {
      StringBuilder builder = new VaccinationsInfoDao()
          .getPatientVaccinationInfo((String) patient.get("mr_no"));
      if (builder.length() > 0) {
        req.setAttribute("vaccinationsInfo", builder.toString());
      }
    }

    Map ceedResponse = null;
    Boolean ceedstatus = null;
    BasicDynaBean ceedbean = null;
    if (modCeedIntegrationEnabled != null && modCeedIntegrationEnabled.equals("Y")) {
      ceedResponse = ConversionUtils.listBeanToMapListMap(CeedDAO.getResponseDetails(consId),
          "activity_id");
      ceedbean = CeedDAO.checkIfCeedCheckDone(consId);
      ceedstatus = ceedbean != null;
      log.debug(
          "CEED Response Time: " + (ceedbean == null ? "" : ceedbean.get("response_datetime")));
    }

    req.setAttribute("ceedResponseMap", ceedResponse);
    req.setAttribute("ceedResponseMapJson", js.deepSerialize(ceedResponse));
    req.setAttribute("ceedstatus", js.deepSerialize(ceedstatus));
    req.setAttribute("ceedbean", ceedbean);

    HashMap<Integer, String> regExpPatternMap = RegularExpressionDAO
        .getRegPatternWithExpression("E");
    req.setAttribute("regExpMapDesc", RegularExpressionDAO.getRegPatternWithExpression("D"));
    req.setAttribute("regExpPatternMap", js.serialize(regExpPatternMap));

    log.debug("consultation load end " + new java.util.Date());

    return mapping.findForward("OpPrescribeList");
  }

  /**
   * Gets the reports.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the reports
   * @throws IOException      Signals that an I/O exception has occurred.
   * @throws ServletException the servlet exception
   * @throws Exception        the exception
   */
  public ActionForward getReports(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException, Exception {

    response.setContentType("text/plain");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    String patientId = request.getParameter("patient_id");
    Map<String, List<EMRDoc>> map = new LinkedHashMap<String, List<EMRDoc>>();
    map.put("lab_reports", new DIAGProviderBOImpl().listDocumentsByVisit(patientId));
    map.put("service_reports", new ServiceProviderBOImpl().listDocumentsByVisit(patientId));
    map.put("ot_reports", new OperationProviderBOImpl().listDocumentsByVisit(patientId));

    JSONSerializer js = new JSONSerializer().exclude("class");
    js.deepSerialize(map, response.getWriter());
    response.flushBuffer();
    return null;
  }

  /**
   * Gets the primary plan.
   *
   * @param patientId the patient id
   * @return the primary plan
   * @throws SQLException the SQL exception
   */
  private BasicDynaBean getPrimaryPlan(String patientId) throws SQLException {

    // Patient plan / tpa information should come from patient insurance plan rather than patient
    // visit table
    List<BasicDynaBean> patientPlans = pipdao.getPlanDetails(patientId);
    BasicDynaBean patientPlan = null;
    if (null != patientPlans && patientPlans.size() > 0) {
      patientPlan = patientPlans.get(0); // primary plan
    }
    return patientPlan;
  }

  /**
   * Update.
   *
   * @param mapping the mapping
   * @param form    the form
   * @param req     the req
   * @param res     the res
   * @return the action forward
   * @throws IOException    Signals that an I/O exception has occurred.
   * @throws SQLException   the SQL exception
   * @throws ParseException the parse exception
   * @throws Exception      the exception
   */

  public ActionForward update(ActionMapping mapping, ActionForm form, HttpServletRequest req,
      HttpServletResponse res) throws IOException, SQLException, ParseException, Exception {

    /*
     * POST: update the list of prescriptions with new ones, including addition of prescriptions
     * Parameters: consultation_id [op_medicine_pres_id, medicine_name, dosage ...]
     * [op_services_pres_id, service_name, remarks ...]
     */
    HttpSession session = req.getSession(false);

    String close = req.getParameter("closeConsultation");
    Boolean closeConsultation = close != null;
    String consIdStr = req.getParameter("consultation_id");
    String userName = (String) session.getAttribute("userid");
    int consId = Integer.parseInt(consIdStr);
    Map params = req.getParameterMap();
    ArrayList errors = new ArrayList();

    String perdiemCheck = req.getParameter("perdiem_check");
    String usePerdiem = req.getParameter("use_perdiem");
    String perdiemCode = req.getParameter("per_diem_code");

    BasicDynaBean genericPrefs = GenericPreferencesDAO.getAllPrefs();
    BasicDynaBean consRecord = consultDao.findConsultationExt(consId);
    String mrno = (String) consRecord.get("mr_no");
    // patient has been converted as IP in another browser tab. refer bug 53016
    if (consRecord.get("visit_type").equals("i")) {
      FlashScope flash = FlashScope.getScope(req);
      flash.error("Patient '" + mrno
          + "' has been converted to an IP patient. Cannot record an OP consultation.");
      ActionRedirect redirect = new ActionRedirect(mapping.findForward("opListRedirect"));
      redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
      return redirect;
    }
    String patientId = (String) consRecord.get("patient_id");

    String error = null;
    Connection con = null;
    boolean allSuccess = false;
    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);

      txn: {
        if (!insertImages(consId, req.getParameterValues("pastedPhoto"), con)) {
          break txn;
        }

        if (!deleteImages(req.getParameterValues("deleteImage"), con)) {
          break txn;
        }

        if (!insertOrUpdateFollowupDetails(con, patientId, params, consRecord)) {
          break txn;
        }

        String customField11 = req.getParameter("custom_field11");
        String customField12 = req.getParameter("custom_field12");
        String customField13 = req.getParameter("custom_field13");
        if (!PatientDetailsDAO.updatePatientConsulationInfo(con,
            req.getParameter("patient_consultation_info"), customField11, customField12,
            customField13, patientId)) {
          break txn;
        }

        BasicDynaBean consultBean = consultDao.getBean();
        ConversionUtils.copyToDynaBean(params, consultBean, errors);

        if (closeConsultation) {
          consultBean.set("status", "C");
          consultBean.set("consultation_complete_time",
              new java.sql.Timestamp((new java.util.Date()).getTime()));
        } else {
          consultBean.set("status", "P");
        }

        consultBean.set("username", req.getSession(false).getAttribute("userid"));
        consultBean.set("prescription_notes", req.getParameter("prescription_notes"));
        if (req.getParameter("consultation_start_date") != null
            && !req.getParameter("consultation_start_date").equals("")) {
          consultBean.set("start_datetime",
              DateUtil.parseTimestamp(req.getParameter("consultation_start_date"),
                  req.getParameter("consultation_start_time")));
        }
        if (req.getParameter("consultation_end_date") != null
            && !req.getParameter("consultation_end_date").equals("")) {
          consultBean.set("end_datetime",
              DateUtil.parseTimestamp(req.getParameter("consultation_end_date"),
                  req.getParameter("consultation_end_time")));
        }

        consultBean.set("consultation_mod_time", DateUtil.getCurrentTimestamp());
        if (1 != consultDao.update(con, consultBean.getMap(), "consultation_id",
            consultBean.get("consultation_id"))) {
          break txn;
        }

        if (closeConsultation) {
          if (!ResourceDAO.updateAppointments(con, (Integer) consultBean.get("consultation_id"),
              "DOC")) {
            break txn;
          }
        }

        error = new ConsultationForms().save(con, params);
        if (error != null) {
          break txn;
        }

        if (!new MRDUpdateScreenDAO().updatePresentingComplaint(con, consId, mrno, patientId)) {
          break txn;
        }

        if (perdiemCheck != null && usePerdiem != null && usePerdiem.equals("Y")) {
          HashMap<String, String> perdiemMap = new HashMap<String, String>();
          perdiemMap.put("use_perdiem", usePerdiem);
          perdiemMap.put("per_diem_code", perdiemCode);

          if (!(visitDAO.update(con, perdiemMap, "patient_id", patientId) > 0)) {
            break txn;
          }
        }
        if (req.getParameter("consultationStatus") != null
            && req.getParameter("consultationStatus").equals("I")) {
          BasicDynaBean docConsultBean = consultDao.findByKey("consultation_id",
              consultBean.get("consultation_id"));
          if (!docConsultBean.get("status").equals("A")) {
            break txn;
          }
        }
        allSuccess = true;
      }

    } finally {
      DataBaseUtil.commitClose(con, allSuccess);
    }

    FlashScope flash = FlashScope.getScope(req);
    if (!errors.isEmpty()) {
      flash.put("error", "Some values had invalid format");
    } else if (!allSuccess) {
      flash.put("error", error == null ? "Transaction failed" : error);
    } else {
      if (MessageUtil.allowMessageNotification(req, "general_message_send")) {
        BasicDynaBean messageTypeBean = new GenericDAO("message_types").findByKey("message_type_id",
            "email_phr_prescription");
        if (messageTypeBean != null && messageTypeBean.get("status").equals("A")) {
          // phr practo drive
          String path = RequestContext.getRequest().getServletContext().getRealPath("");
          schedulePrescriptionPHR("Consultation_id_" + consIdStr, path);
        }
      }
      Boolean isPrint = new Boolean(req.getParameter("isPrint"));
      if (isPrint) {
        ActionRedirect printRedirect = new ActionRedirect(mapping.findForward("printConsultation"));
        printRedirect.addParameter("consultation_id", consId);
        printRedirect.addParameter("allFields", "N");
        printRedirect.addParameter("printerId", req.getParameter("printerId"));
        printRedirect.addParameter("templateName", req.getParameter("printTemplate"));

        List<String> printURLs = new ArrayList<String>();
        printURLs.add(req.getContextPath() + printRedirect.getPath());
        req.getSession(false).setAttribute("printURLs", printURLs);
      }

      flash.put("success", "Prescription saved successfully..");
    }

    String sendErxRequest = req.getParameter("sendErxRequest");
    String ceedcheck = req.getParameter("ceedcheck");

    if (sendErxRequest != null && sendErxRequest.equals("Y")) {
      ActionRedirect redirect = new ActionRedirect(mapping.findForward("sendErxRequestRedirect"));
      redirect.addParameter("consultation_id", consId);
      redirect.addParameter("visit_id", patientId);
      redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
      return redirect;
    } else if (ceedcheck != null && ceedcheck.equals("Y")) { // redirect for sending ceed request
      ActionRedirect redirect = new ActionRedirect(mapping.findForward("sendCeedRequestRedirect"));
      redirect.addParameter("consultation_id", consId);
      redirect.addParameter("visit_id", patientId);
      return redirect;
    } else {
      ActionRedirect redirect = new ActionRedirect(mapping.findForward("OpPrescribeListRedirect"));
      redirect.addParameter("consultation_id", consId);
      redirect.addParameter("showPrinter", req.getParameter("printerId"));
      redirect.addParameter("templateName", req.getParameter("printTemplate"));
      redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
      return redirect;
    }

  }

  /**
   * Insert or update followup details.
   *
   * @param con        the con
   * @param patientId  the patient id
   * @param params     the params
   * @param consRecord the cons record
   * @return true, if successful
   * @throws SQLException   the SQL exception
   * @throws IOException    Signals that an I/O exception has occurred.
   * @throws ParseException the parse exception
   */
  private boolean insertOrUpdateFollowupDetails(Connection con, String patientId, Map params,
      BasicDynaBean consRecord) throws SQLException, IOException, ParseException {
    boolean flag = false;
    txn: {
      BasicDynaBean followuprecord = followUpDao.findByKey("patient_id", patientId);
      String followUpDate = ConversionUtils.getParamValue(params, "followup_date", "");
      if (followUpDate.equals("")) {
        if (followuprecord != null && !followUpDao.delete(con, "patient_id", patientId)) {
          break txn;
        }
      } else {
        BasicDynaBean followupbean = followUpDao.getBean();
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        followupbean.set("followup_date", new java.sql.Date(sdf.parse(followUpDate).getTime()));
        followupbean.set("followup_doctor_id", consRecord.get("doctor_name"));
        if (followuprecord == null) {
          String followUpId = AutoIncrementId.getSequenceId("follow_up_details_seq",
              "follow_up_details");
          followupbean.set("followup_id", followUpId);
          followupbean.set("patient_id", consRecord.get("patient_id"));
          if (!followUpDao.insert(con, followupbean)) {
            break txn;
          }
        } else {
          if (1 != followUpDao.update(con, followupbean.getMap(), "patient_id", patientId)) {
            break txn;
          }
        }
      }
      flag = true;
    }
    return flag;
  }

  /**
   * Gets the parameter.
   *
   * @param params the params
   * @param key    the key
   * @param idx    the idx
   * @return the parameter
   */
  public String getParameter(Map params, String key, int idx) {
    return ((String[]) params.get(key))[idx];
  }

  /**
   * Reopen consultation.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the action forward
   * @throws ServletException the servlet exception
   * @throws IOException      Signals that an I/O exception has occurred.
   * @throws SQLException     the SQL exception
   */
  public ActionForward reopenConsultation(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException, SQLException {
    String consultationId = request.getParameter("consultation_id");
    ActionRedirect redirect = new ActionRedirect(mapping.findForward("OpPrescribeListRedirect"));
    FlashScope flash = FlashScope.getScope(request);
    if (DoctorConsultationDAO.reopenConsultation(Integer.parseInt(consultationId))) {
      flash.put("info", "Consultation Reopened Successfully.. "
          + "Please revisit E&M code after editing and closing consultation.");
    } else {
      flash.put("error", "Failed to reopen the consultation..");
    }
    redirect.addParameter("consultation_id", consultationId);
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
    return redirect;

  }

  /**
   * File upload.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the action forward
   * @throws IOException         Signals that an I/O exception has occurred.
   * @throws FileUploadException the file upload exception
   * @throws SQLException        the SQL exception
   */
  public ActionForward fileUpload(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws IOException, FileUploadException, SQLException {
    Map params = getParameterMap(request);
    GenericDAO dao = new GenericDAO("doctor_consult_images");
    BasicDynaBean bean = dao.getBean();
    List errorFields = new ArrayList();
    ConversionUtils.copyToDynaBean(params, bean, errorFields);
    Connection con = DataBaseUtil.getConnection();

    String msg = "";
    int imageIdentifier = 0;
    try {
      if (errorFields.isEmpty()) {
        imageIdentifier = dao.getNextSequence();
        bean.set("image_id", imageIdentifier);
        if (dao.insert(con, bean)) {
          msg = "success";
        } else {
          msg = "Failed to insert the image";
        }
      } else {
        msg = "Conversion failed";
      }
    } finally {
      if (!msg.equals("success")) {
        imageIdentifier = 0;
      }
      DataBaseUtil.closeConnections(con, null);
    }
    response.setContentType("text/plain");
    response.getWriter().write(imageIdentifier + "");
    response.flushBuffer();
    return null;
  }

  /**
   * Paste image.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the action forward
   * @throws IOException         Signals that an I/O exception has occurred.
   * @throws FileUploadException the file upload exception
   * @throws SQLException        the SQL exception
   */
  public ActionForward pasteImage(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws IOException, FileUploadException, SQLException {
    Map params = getParameterMap(request);
    BasicDynaBean bean = consultImageDao.getBean();
    List errorFields = new ArrayList();
    ConversionUtils.copyToDynaBean(params, bean, errorFields);
    Connection con = DataBaseUtil.getConnection();

    boolean success = false;
    int imageIdentifier = 0;
    try {
      if (errorFields.isEmpty()) {
        imageIdentifier = consultImageDao.getNextSequence();
        bean.set("image_id", imageIdentifier);
        bean.set("datetime", DateUtil.getCurrentTimestamp());
        bean.set("content_type", "image/jpeg");
        success = consultImageDao.insert(con, bean);
      }
    } finally {
      if (!success) {
        imageIdentifier = 0;
      }
      DataBaseUtil.closeConnections(con, null);
    }
    response.setContentType("text/plain");
    response.getWriter().write(imageIdentifier + "");
    response.flushBuffer();
    return null;
  }

  /**
   * View image.
   *
   * @param mappin   the mappin
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the action forward
   * @throws IOException  Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   */
  @IgnoreConfidentialFilters
  public ActionForward viewImage(ActionMapping mappin, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws IOException, SQLException {
    BasicDynaBean bean = consultImageDao.getBean();
    consultImageDao.loadByteaRecords(bean, "image_id",
        Integer.parseInt(request.getParameter("image_id")));
    OutputStream os = response.getOutputStream();
    response.setContentType((String) bean.get("content_type"));
    os.write(DataBaseUtil.readInputStream((InputStream) bean.get("image")));
    os.flush();
    os.close();
    return null;
  }

  /**
   * Insert images.
   *
   * @param consultationId the consultation id
   * @param imageIds       the image ids
   * @param con            the con
   * @return true, if successful
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  public boolean insertImages(Integer consultationId, String[] imageIds, Connection con)
      throws SQLException, IOException {
    if (imageIds == null) {
      return true;
    }
    int count = 0;

    Map<String, Integer> columndata = new HashMap<>();
    columndata.put("consultation_id", consultationId);
    for (String imageId : imageIds) {
      if (consultImageDao.update(con, columndata, "image_id", Integer.parseInt(imageId)) == 1) {
        count++;
      }
    }
    return imageIds.length == count;
  }

  /**
   * Delete images.
   *
   * @param images the images
   * @param con    the con
   * @return true, if successful
   * @throws SQLException the SQL exception
   */
  public boolean deleteImages(String[] images, Connection con) throws SQLException {
    if (images == null) {
      return true;
    }

    int count = 0;
    for (String imageId : images) {
      if (consultImageDao.delete(con, "image_id", Integer.parseInt(imageId))) {
        count++;
      }
    }
    return images.length == count;
  }

  /**
   * Find items.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the action forward
   * @throws IOException  Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   */
  @IgnoreConfidentialFilters
  public ActionForward findItems(ActionMapping mapping, ActionForm form, HttpServletRequest request,
      HttpServletResponse response) throws IOException, SQLException {

    response.setContentType("text/plain");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    String patientHealthAutority = request.getParameter("p_health_authority");
    if (patientHealthAutority == null || patientHealthAutority.equals(null)) {
      patientHealthAutority = "";
    }

    String centerIdStr = request.getParameter("center_id");
    String centerId = "0";
    if (centerIdStr != null && !centerIdStr.equals("")) {
      centerId = centerIdStr;
    }

    String tpaIdStr = request.getParameter("tpa_id");
    String tpaId = "-1";
    if (tpaIdStr != null && !tpaIdStr.equals("")) {
      tpaId = tpaIdStr;
    } else {
      tpaId = "0";
    }
    String query = request.getParameter("query");
    String searchType = request.getParameter("searchType");
    String orgId = request.getParameter("org_id");
    String deptId = request.getParameter("dept_id");
    if (searchType == null) {
      searchType = "";
    }

    Boolean nonDentalServices = new Boolean(request.getParameter("non_dental_services"));
    BasicDynaBean genericPrefs = GenericPreferencesDAO.getAllPrefs();
    String useStoreItems = (String) genericPrefs.get("prescription_uses_stores");
    Boolean isStanding = new Boolean(request.getParameter("isStanding"));
    boolean forceUnUseOfGenerics = request.getParameter("forceUnUseOfGenerics") != null
        ? new Boolean(request.getParameter("forceUnUseOfGenerics"))
        : false;
    List list = PrescriptionsMasterDAO.getAllItems(orgId, searchType, query, useStoreItems,
        isStanding, nonDentalServices, tpaId, centerId, forceUnUseOfGenerics, patientHealthAutority,
        deptId);
    HashMap retVal = new HashMap();
    retVal.put("result", ConversionUtils.copyListDynaBeansToMap(list));
    JSONSerializer js = new JSONSerializer().exclude("class");
    js.deepSerialize(retVal, response.getWriter());
    response.flushBuffer();

    return null;
  }

  /**
   * Find items for favourites.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the action forward
   * @throws IOException  Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   */
  @IgnoreConfidentialFilters
  public ActionForward findItemsForFavourites(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {

    response.setContentType("text/plain");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");

    String query = request.getParameter("query");
    String searchType = request.getParameter("searchType");
    int centerId = Integer.parseInt(request.getParameter("center_id"));
    BasicDynaBean genericPrefs = GenericPreferencesDAO.getAllPrefs();
    String useStoreItems = (String) genericPrefs.get("prescription_uses_stores");

    if (searchType == null) {
      searchType = "";
    }

    List list = PrescriptionsMasterDAO.getAllItemsForFavourites(searchType, query, useStoreItems,
        centerId);
    HashMap retVal = new HashMap();
    retVal.put("result", ConversionUtils.copyListDynaBeansToMap(list));
    JSONSerializer js = new JSONSerializer().exclude("class");
    js.deepSerialize(retVal, response.getWriter());
    response.flushBuffer();

    return null;
  }

  /**
   * Gets the item rate details.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the item rate details
   * @throws ServletException the servlet exception
   * @throws IOException      Signals that an I/O exception has occurred.
   * @throws SQLException     the SQL exception
   */
  @IgnoreConfidentialFilters
  public ActionForward getItemRateDetails(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException, SQLException {
    response.setContentType("text/plain");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");

    String bedType = request.getParameter("bed_type");
    bedType = bedType.equals("") ? "GENERAL" : bedType;
    BasicDynaBean genericPrefs = GenericPreferencesDAO.getAllPrefs();
    String useStoreItems = (String) genericPrefs.get("prescription_uses_stores");
    String planIdStr = request.getParameter("planId");
    String planIdStr1 = request.getParameter("planId1");// for secondary Sponsor
    int planId = 0;
    if (planIdStr != null && !planIdStr.equals("")) {
      planId = Integer.parseInt(planIdStr);
    }
    int planId1 = 0;
    if (planIdStr1 != null && !planIdStr1.equals("")) {
      planId1 = Integer.parseInt(planIdStr1);
    }
    BasicDynaBean bean = null;
    String itemType = request.getParameter("item_type");
    String orgId = request.getParameter("org_id");
    String itemId = request.getParameter("item_id");
    String itemName = request.getParameter("item_name");
    Boolean ispkg = new Boolean(request.getParameter("is_package"));
    if (itemType.equals("Medicine") && !useStoreItems.equals("Y")) {
      // for non hospital medicines charges will not be available.
    } else {
      bean = PrescriptionsMasterDAO.getItemRateDetails(planId, orgId, bedType, itemType, itemId,
          ispkg);
      if (bean != null
          && "N".equals(bean.get("category_payable") != null ? bean.get("category_payable") : "")
          && planId1 != 0) {
        bean = PrescriptionsMasterDAO.getItemRateDetails(planId1, orgId, bedType, itemType, itemId,
            ispkg);
      }
    }

    Map map = new HashMap();
    if (bean != null) {
      map.putAll(bean.getMap());
    }
    if (itemType.equals("Medicine")) {
      BasicDynaBean routesBean = PharmacymasterDAO
          .getRoutesOfAdministrations(useStoreItems.equals("Y") ? itemId : itemName, useStoreItems);
      if (routesBean != null) {
        map.putAll(routesBean.getMap());
      }
    }

    JSONSerializer js = new JSONSerializer().exclude("class");
    js.serialize(map, response.getWriter());
    response.flushBuffer();

    return null;
  }

  /**
   * Gets the routes of administrations.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the routes of administrations
   * @throws IOException  Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   */
  @IgnoreConfidentialFilters
  public ActionForward getRoutesOfAdministrations(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
    response.setContentType("text/plain");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");

    JSONSerializer js = new JSONSerializer().exclude("class");
    String itemId = request.getParameter("item_id");
    String itemName = request.getParameter("item_name");
    BasicDynaBean genericPrefs = GenericPreferencesDAO.getAllPrefs();
    String useStoreItems = (String) genericPrefs.get("prescription_uses_stores");
    BasicDynaBean routesBean = PharmacymasterDAO
        .getRoutesOfAdministrations(useStoreItems.equals("Y") ? itemId : itemName, useStoreItems);

    js.serialize(routesBean == null ? null : routesBean.getMap(), response.getWriter());
    response.flushBuffer();

    return null;
  }

  /**
   * Gets the diagnosis history.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the diagnosis history
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  public ActionForward getDiagnosisHistory(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
    response.setContentType("text/plain");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");

    int consultationId = Integer.parseInt(request.getParameter("consultation_id"));
    BasicDynaBean consultBean = consultDao.findConsultationExt(consultationId);
    String mrNo = (String) consultBean.get("mr_no");
    String doctorId = (String) consultBean.get("doctor_name");
    String visitType = request.getParameter("visit_type");
    List list = MRDDiagnosisDAO.getDiagnosisHistory(mrNo, consultationId, doctorId, visitType);
    JSONSerializer js = new JSONSerializer();

    response.getWriter().write(js.serialize(ConversionUtils.copyListDynaBeansToMap(list)));
    response.flushBuffer();

    return null;
  }

  /**
   * Gets the vitals history.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the vitals history
   * @throws IOException  Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   */
  public ActionForward getVitalsHistory(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
    response.setContentType("text/plain");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");

    int consultationId = Integer.parseInt(request.getParameter("consultation_id"));
    BasicDynaBean consultBean = consultDao.findConsultationExt(consultationId);
    String mrNo = (String) consultBean.get("mr_no");
    String doctorId = (String) consultBean.get("doctor_name");
    String visitType = request.getParameter("visit_type");
    List list = genericVitalFormDAO.getVitalsHistory(mrNo, consultationId, doctorId, visitType);

    JSONSerializer js = new JSONSerializer();
    response.getWriter().write(js.serialize(ConversionUtils.copyListDynaBeansToMap(list)));
    return null;
  }

  /**
   * Gets the prescriptions history.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the prescriptions history
   * @throws IOException  Signals that an I/O exception has occurred.
   * @throws SQLException the SQL exception
   */
  public ActionForward getPrescriptionsHistory(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws IOException, SQLException {
    response.setContentType("text/plain");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");

    int consultationId = Integer.parseInt(request.getParameter("consultation_id"));
    BasicDynaBean consultBean = consultDao.findConsultationExt(consultationId);
    String mrNo = (String) consultBean.get("mr_no");
    String doctorId = (String) consultBean.get("doctor_name");
    String visitType = request.getParameter("visit_type");

    log.debug("Prescription History Query Time before : " + System.currentTimeMillis() / 1000);
    List list = PrescriptionsMasterDAO.getAllPrescriptionsHistory(mrNo, visitType, doctorId,
        consultationId);
    log.debug("Prescription History Query Time after : " + System.currentTimeMillis() / 1000);

    JSONSerializer js = new JSONSerializer();
    response.getWriter().write(js.serialize(ConversionUtils.copyListDynaBeansToMap(list)));

    return null;
  }

  /**
   * Gets the consultation notes history.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the consultation notes history
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  public ActionForward getConsultationNotesHistory(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
    response.setContentType("text/plain");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");

    int consultationId = Integer.parseInt(request.getParameter("consultation_id"));
    BasicDynaBean consultBean = consultDao.findConsultationExt(consultationId);
    String mrNo = (String) consultBean.get("mr_no");
    String doctorId = (String) consultBean.get("doctor_name");
    String visitType = request.getParameter("visit_type");
    List list = ConsultationFieldValuesDAO.getConsultationFieldsHistory(mrNo, visitType, doctorId,
        consultationId);

    JSONSerializer js = new JSONSerializer();
    response.getWriter().write(js.serialize(ConversionUtils.copyListDynaBeansToMap(list)));

    return null;
  }

  /**
   * Gets the images history.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the images history
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  public ActionForward getImagesHistory(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {
    response.setContentType("text/plain");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");

    int consultationId = Integer.parseInt(request.getParameter("consultation_id"));
    BasicDynaBean consultBean = consultDao.findConsultationExt(consultationId);
    String mrNo = (String) consultBean.get("mr_no");
    String doctorId = (String) consultBean.get("doctor_name");
    String visitType = request.getParameter("visit_type");
    List list = DoctorConsultImagesDAO.getImagesHistory(mrNo, visitType, doctorId, consultationId);

    JSONSerializer js = new JSONSerializer();
    response.getWriter().write(js.serialize(ConversionUtils.copyListDynaBeansToMap(list)));

    return null;
  }

  /**
   * Gets the generic JSON.
   *
   * @param am  the am
   * @param af  the af
   * @param req the req
   * @param res the res
   * @return the generic JSON
   * @throws Exception the exception
   */
  public ActionForward getGenericJSON(ActionMapping am, ActionForm af, HttpServletRequest req,
      HttpServletResponse res) throws Exception {
    String genericId = req.getParameter("generic_code");
    if ((genericId == null) || genericId.equals("")) {
      log.error("getGenericJSON: Generic code is required");
      return null;
    }
    Map map = new HashMap();
    GenericDTO genDetails = GenericMasterDAO.getSelectedGenDetails(genericId);
    map.put("gmaster_name", genDetails.getGmaster_name());
    map.put("genCode", genDetails.getGenCode());
    map.put("status", genDetails.getStatus());
    map.put("operation", genDetails.getOperation());
    map.put("classification_id", genDetails.getClassification_id());
    map.put("sub_classification_id", genDetails.getSub_classification_id());
    map.put("standard_adult_dose", genDetails.getStandard_adult_dose());
    map.put("criticality", genDetails.getCriticality());
    map.put("classificationName", genDetails.getClassificationName());
    map.put("sub_ClassificationName", genDetails.getSub_classificationName());

    JSONSerializer js = new JSONSerializer().exclude("class");
    String genericJSON = js.serialize(map);
    res.setContentType("text/plain");
    res.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    res.getWriter().write(genericJSON);
    res.flushBuffer();
    return null;
  }

  /**
   * Gets the patient previous prescriptions.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the patient previous prescriptions
   * @throws SQLException   the SQL exception
   * @throws IOException    Signals that an I/O exception has occurred.
   * @throws ParseException the parse exception
   */
  public ActionForward getPatientPreviousPrescriptions(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws SQLException, IOException, ParseException {
    String consultaionIdStr = request.getParameter("consultaion_id");
    int consId = Integer.parseInt(consultaionIdStr);
    String mrNo = request.getParameter("mr_no");
    String doctorId = request.getParameter("doctor_id");
    Map<LISTING, Object> listingParams = ConversionUtils
        .getListingParameter(request.getParameterMap());

    // current visit id, this is because we get the items which are applicable to the current visit
    // rateplan and sponsor
    String patientId = request.getParameter("patient_id");
    BasicDynaBean patientPlan = getPrimaryPlan(patientId);
    String patientHealthAutority = request.getParameter("p_health_authority");
    if (patientHealthAutority == null) {
      patientHealthAutority = "";
    }

    List<Map> list = ConversionUtils
        .copyListDynaBeansToMap(OutPatientDAO.getPatientPreviousPrescriptionsList(mrNo, consId,
            doctorId, patientId, patientHealthAutority));

    Map<String, java.util.Map<String, Object>> prescChargeMap = new HashMap<>();
    BasicDynaBean rateBean = null;
    List<Integer> medList = new ArrayList<>();
    for (int i = 0; i < list.size(); i++) {
      Map map = (Map) list.get(i);
      if (map.get("item_type").toString().equalsIgnoreCase("Medicine")
          && !(Boolean) map.get("non_hosp_medicine")) {
        if (map.get("item_id") == null || map.get("item_id").toString().equals("")) {
          prescChargeMap.put(map.get("op_medicine_pres_id") == null ? null
              : map.get("op_medicine_pres_id").toString(), null);
        } else {
          prescChargeMap.put(map.get("op_medicine_pres_id").toString(), map);
          if (!medList.contains((Integer) map.get("item_id"))) {
            medList.add((Integer) map.get("item_id"));
          }
        }
      }
    }
    if (!medList.isEmpty()) {
      int centerId = RequestContext.getCenterId();
      String healthAuthority = CenterMasterDAO.getHealthAuthorityForCenter(centerId);
      int planId = patientPlan == null ? 0 : (Integer) patientPlan.get("plan_id");
      Connection con = null;
      try {
        con = DataBaseUtil.getConnection();
        List<BasicDynaBean> beans = new MedicineStockDAO(con)
            .getAllStoreMedicineStockWithPatAmtsInDept(medList, planId, "o", true, 0,
                healthAuthority, true);
        Map<Integer, Map> uniqueMedicines = new HashMap<Integer, Map>();
        for (Map.Entry<String, Map<String, Object>> m : prescChargeMap.entrySet()) {
          if (m.getKey() == null) {
            continue;
          }
          Integer medId = (Integer) m.getValue().get("item_id");

          if (uniqueMedicines.containsKey(medId)) {
            // replacing the map with the rate map.
            prescChargeMap.put(m.getKey(), (Map) uniqueMedicines.get(medId));

          } else {
            BasicDynaBean medBean = null;
            for (BasicDynaBean b : beans) {
              Integer medicineId = (Integer) b.get("medicine_id");
              if (medId.intValue() == medicineId.intValue()) {
                medBean = b;
                break;
              }
            }
            prescChargeMap.put(m.getKey(), medBean == null ? null : medBean.getMap());
          }
        }
      } finally {
        DataBaseUtil.closeConnections(con, null);
      }
    }
    Map<String, Object> jsMap = new HashMap<String, Object>();
    jsMap.put("list", list);
    jsMap.put("chargeMap", prescChargeMap);
    jsMap.put("docdetailsMap", OutPatientDAO.getDoctorConsultationMap(consId));

    response.setContentType("text/plain");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");

    JSONSerializer js = new JSONSerializer().exclude("class");

    js.deepSerialize(jsMap, response.getWriter());
    return null;
  }

  /**
   * Find doctor favourite items.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the action forward
   * @throws SQLException the SQL exception
   * @throws IOException  Signals that an I/O exception has occurred.
   */
  @IgnoreConfidentialFilters
  public ActionForward findDoctorFavouriteItems(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws SQLException, IOException {

    response.setContentType("text/plain");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");

    String patientHealthAutority = request.getParameter("p_health_authority");
    if (patientHealthAutority == null) {
      patientHealthAutority = "";
    }
    String centerIdStr = request.getParameter("center_id");
    String centerId = "0";
    if (centerIdStr != null && !centerIdStr.equals("")) {
      centerId = centerIdStr;
    }

    String tpaIdStr = request.getParameter("tpa_id");
    String tpaId = "-1";
    if (tpaIdStr != null && !tpaIdStr.equals("")) {
      tpaId = tpaIdStr;
    } else {
      tpaId = "0";
    }

    String searchType = request.getParameter("searchType");
    if (searchType == null) {
      searchType = "";
    }
    BasicDynaBean genericPrefs = GenericPreferencesDAO.getAllPrefs();
    String useStoreItems = (String) genericPrefs.get("prescription_uses_stores");
    boolean forceUnUseOfGenerics = request.getParameter("forceUnUseOfGenerics") != null
        ? new Boolean(request.getParameter("forceUnUseOfGenerics"))
        : false;
    String query = request.getParameter("query");
    String orgId = request.getParameter("org_id");
    String consultDoctorId = request.getParameter("consult_doctor_id");
    Boolean nonDentalServices = new Boolean(request.getParameter("non_dental_services"));
    Boolean nonHospMedicine = (request.getParameter("non_hosp_medicine") != null
        && !request.getParameter("non_hosp_medicine").equals("")
            ? new Boolean(request.getParameter("non_hosp_medicine"))
            : false);
    List list = PrescriptionsMasterDAO.getDoctorFavouriteItems(orgId, searchType, query,
        useStoreItems, consultDoctorId, nonDentalServices, tpaId, centerId, forceUnUseOfGenerics,
        patientHealthAutority, nonHospMedicine);
    HashMap retVal = new HashMap();
    retVal.put("result", ConversionUtils.copyListDynaBeansToMap(list));

    JSONSerializer js = new JSONSerializer().exclude("class");
    js.deepSerialize(retVal, response.getWriter());
    response.flushBuffer();

    return null;
  }

  /**
   * Schedule prescription PHR.
   *
   * @param uniString the uni string
   * @param path      the path
   */
  private void schedulePrescriptionPHR(String uniString, String path) {
    String params = uniString + ";" + RequestContext.getCenterId();
    Map<String, Object> jobData = new HashMap<>();
    jobData.put("params", params);
    jobData.put("path", path);
    jobData.put("schema", RequestContext.getSchema());
    JobService jobService = JobSchedulingService.getJobService();
    jobService.scheduleImmediate(buildJob(uniString, PrescriptionPHRJob.class, jobData));

  }

  /**
   * Gets the prescription favourites.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the prescription favourites
   * @throws ServletException the servlet exception
   * @throws IOException      Signals that an I/O exception has occurred.
   * @throws SQLException     the SQL exception
   */
  public ActionForward getPrescriptionFavourites(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException, SQLException {
    String consultationId = request.getParameter("consultation_id");
    Integer consId = 0;
    JSONSerializer js = new JSONSerializer().exclude(".class");
    Map result = new HashMap();

    if (consultationId == null || consultationId.equals("")) {
      js.deepSerialize(result, response.getWriter());
      return null;
    } else {
      consId = Integer.parseInt(consultationId);
    }
    BasicDynaBean consultBean = consultDao.findConsultationExt(consId);
    String patientId = (String) consultBean.get("patient_id");
    java.util.Map patient = com.insta.hms.Registration.VisitDetailsDAO
        .getPatientVisitDetailsMap(patientId);

    String tpaId = (String) patient.get("primary_sponsor_id");
    tpaId = tpaId == null || tpaId.equals("") ? "-1" : tpaId;

    String bedType = "";
    String allocBedType = (String) patient.get("alloc_bed_type");
    if (allocBedType == null || allocBedType.equals("")) {
      bedType = (String) patient.get("bill_bed_type");
    } else {
      bedType = allocBedType;
    }
    bedType = bedType.equals("") ? "GENERAL" : bedType;
    BasicDynaBean patientPlan = getPrimaryPlan(patientId);
    BasicDynaBean genericPrefs = GenericPreferencesDAO.getAllPrefs();
    String prescriptionUsesStores = (String) genericPrefs.get("prescription_uses_stores");
    String orgId = (String) consultBean.get("org_id");
    int planId = patientPlan == null ? 0 : (Integer) patientPlan.get("plan_id");
    Integer patientCenterId = (Integer) patient.get("center_id");
    String patientFavHealthAutority = CenterMasterDAO.getHealthAuthorityForCenter(patientCenterId);
    Integer pageNo = Integer.parseInt(request.getParameter("page_no"));
    List<BasicDynaBean> doctorFavourites = ConsultationFavouritesDAO.getAllFavourites(
        (String) consultBean.get("doctor_name"), prescriptionUsesStores, orgId, tpaId,
        patientCenterId, patientFavHealthAutority, bedType, planId, pageNo);

    request.setAttribute("doctor_favourites", doctorFavourites);

    Map favCharges = new HashMap();
    for (BasicDynaBean favourite : doctorFavourites) {
      String itemId = (String) favourite.get("item_id");
      Integer favouriteId = (Integer) favourite.get("favourite_id");
      Boolean nonHospMedicine = (Boolean) favourite.get("non_hosp_medicine");

      String itemType = (String) favourite.get("item_type");
      if (itemType.equals("Medicine") && !nonHospMedicine
          && (itemId == null || itemId.equals(""))) {
        continue;
      }
      if ((itemType.equals("Medicine") && !nonHospMedicine) || itemType.equals("Inv.")
          || itemType.equals("Service") || itemType.equals("Operation")
          || itemType.equals("Doctor")) {
        favCharges.put(itemType + "_" + nonHospMedicine + "_" + favouriteId, favourite.getMap());
      }
    }

    result.put("doctor_favourites", ConversionUtils.copyListDynaBeansToMap(doctorFavourites));
    result.put("fav_charges", favCharges);
    result.put("prev", pageNo != 0);
    result.put("page_no", pageNo);
    result.put("next", doctorFavourites.size() == 20);

    response.setContentType("text/plain");
    response.setHeader(HttpHeaders.CACHE_CONTROL, "no-cache");
    js.deepSerialize(result, response.getWriter());
    response.flushBuffer();

    return null;

  }
}

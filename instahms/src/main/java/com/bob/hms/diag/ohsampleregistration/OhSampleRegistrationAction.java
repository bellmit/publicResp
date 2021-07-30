
package com.bob.hms.diag.ohsampleregistration;

import com.bob.hms.common.DataBaseUtil;
import com.bob.hms.common.DateUtil;
import com.bob.hms.common.RequestContext;
import com.bob.hms.diag.incomingsamplependingbills.IncomingSamplePendingBillDAO;
import com.insta.hms.Registration.PatientDetailsDAO;
import com.insta.hms.billing.Bill;
import com.insta.hms.billing.BillBO;
import com.insta.hms.billing.BillDAO;
import com.insta.hms.billing.BillDetails;
import com.insta.hms.billing.ChargeBO;
import com.insta.hms.billing.ChargeDAO;
import com.insta.hms.billing.ChargeDTO;
import com.insta.hms.billing.Receipt;
import com.insta.hms.billing.paymentdetails.AbstractPaymentDetails;
import com.insta.hms.common.AppInit;
import com.insta.hms.common.ApplicationContextProvider;
import com.insta.hms.common.BaseAction;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.FlashScope;
import com.insta.hms.common.GenericDAO;
import com.insta.hms.common.HtmlConverter;
import com.insta.hms.common.PhoneNumberUtil;
import com.insta.hms.common.StringUtil;
import com.insta.hms.common.annotations.IgnoreConfidentialFilters;
import com.insta.hms.core.billing.AllocationService;
import com.insta.hms.core.fa.AccountingJobScheduler;
import com.insta.hms.diagnosticmodule.common.BatchSampleIdGenerator;
import com.insta.hms.diagnosticmodule.common.PrefixSampleIdGenerator;
import com.insta.hms.diagnosticmodule.common.SampleCollectionDAO;
import com.insta.hms.diagnosticmodule.internallab.AutomaticSampleRegistration;
import com.insta.hms.diagnosticmodule.internallab.InternalLab;
import com.insta.hms.diagnosticmodule.laboratory.LaboratoryDAO;
import com.insta.hms.diagnosticmodule.laboratory.PendingSamplesDAO;
import com.insta.hms.documentpersitence.AbstractDocumentPersistence;
import com.insta.hms.master.CardType.CardTypeMasterDAO;
import com.insta.hms.master.CenterMaster.CenterMasterDAO;
import com.insta.hms.master.DiagnosticDepartmentMaster.DiagnosticDepartmentMasterDAO;
import com.insta.hms.master.DiscountAuthorizerMaster.DiscountAuthorizerMasterAction;
import com.insta.hms.master.GenericPreferences.GenericPreferencesDAO;
import com.insta.hms.master.GovtIdentifierMaster.GovtIdentifierMasterDAO;
import com.insta.hms.master.InComingHospitals.InComingHospitalDAO;
import com.insta.hms.master.PaymentModes.PaymentModeMasterDAO;
import com.insta.hms.master.PrintConfigurationMaster.PrintConfigurationsDAO;
import com.insta.hms.master.PrintTemplates.PrintTemplate;
import com.insta.hms.master.PrintTemplates.PrintTemplatesDAO;
import com.insta.hms.master.ReferalDoctor.ReferalDoctorDAO;
import com.insta.hms.master.RegistrationPreferences.RegistrationPreferencesDAO;
import com.insta.hms.master.SalutationMaster.SalutationMasterDAO;
import com.lowagie.text.DocumentException;

import flexjson.JSONSerializer;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import net.sf.jasperreports.engine.JRException;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.upload.FormFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPathExpressionException;


/**
 * The Class OhSampleRegistrationAction.
 */
public class OhSampleRegistrationAction extends BaseAction {

  /** The logger. */
  private static Logger logger = LoggerFactory.getLogger(OhSampleRegistrationAction.class);

  /** The center master DAO. */
  private static CenterMasterDAO centerMasterDAO = new CenterMasterDAO();
  
  /** The allocation service. */
  private final AllocationService allocationService = (AllocationService)ApplicationContextProvider
      .getApplicationContext().getBean("allocationService");

  private static PaymentModeMasterDAO paymentModeMasterDao = new PaymentModeMasterDAO();
  
  final AccountingJobScheduler accountingJobScheduler = ApplicationContextProvider
      .getBean(AccountingJobScheduler.class);

  private static GenericDAO packageDao = new GenericDAO("packages");

  /**
   * Gets the sample registration form.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the sample registration form
   * @throws SQLException Sql exception
   */
  @IgnoreConfidentialFilters
  public ActionForward getSampleRegistrationForm(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws SQLException {

    JSONSerializer js = new JSONSerializer().exclude("class");
    int centerId = (Integer) request.getSession(false).getAttribute("centerId");

    request.setAttribute("ddepteDetails",
        DiagnosticDepartmentMasterDAO.getAllDiagnosticDepartments());

    BasicDynaBean printPref = PrintConfigurationsDAO
        .getPrintMode(PrintConfigurationsDAO.PRINT_TYPE_BILL);
    request.setAttribute("pref", printPref);
    request.setAttribute("incomingHospitalJSON",
        js.serialize(ConversionUtils.copyListDynaBeansToMap(
            new GenericDAO("incoming_hospitals").listAll(null, "status", "A", "hospital_name"))));
    request.setAttribute("referalDoctorsJSON",
        js.serialize(ReferalDoctorDAO.getReferencedoctors()));
    request.setAttribute("salutationQueryJson",
        js.serialize(ConversionUtils.listBeanToListMap(SalutationMasterDAO.getSalutationIdName())));
    request.setAttribute("genPrefs", GenericPreferencesDAO.getGenericPreferences());
    request.setAttribute("regPrefJSON",
        js.serialize(RegistrationPreferencesDAO.getRegistrationPreferences()));
    OhSampleRegistrationDAO dao = new OhSampleRegistrationDAO();
    request.setAttribute("salutaions", dao.getSalutions());
    BasicDynaBean mst = new GenericDAO("master_timestamp").getRecord();
    request.setAttribute("masterTimeStamp", mst.get("master_count"));
    request.setAttribute("specimenType", js.serialize(PendingSamplesDAO.getSampleType()));
    BasicDynaBean diagGenericPref = GenericPreferencesDAO.getdiagGenericPref();
    String doAutoGenerateSammpleIdStr = (String) diagGenericPref.get("autogenerate_sampleid");
    request.setAttribute("isautogenerateSampleIdReq", doAutoGenerateSammpleIdStr);
    request.setAttribute("category", mapping.getProperty("category"));
    request.setAttribute("outhousesAgainstTests",
        PendingSamplesDAO.getOuthousesAgainstTestId(centerId, true, null));
    request.setAttribute("outhousesAgainstTestsJson",
        js.deepSerialize(PendingSamplesDAO.getOuthousesAgainstTestId(centerId, true, null)));
    List<BasicDynaBean> discAuths = DiscountAuthorizerMasterAction.getDiscountAuthorizers(centerId);
    request.setAttribute("discountAuthorizersJSON",
        js.serialize(ConversionUtils.listBeanToListMap(discAuths)));
    request.setAttribute("discountAuthorizers", discAuths);
    request.setAttribute("doctors",
        js.serialize(ConversionUtils.listBeanToListMap(new LaboratoryDAO()
            .getDoctorDepartmentsDynaList(mapping.getProperty("category"), centerId))));
    request.setAttribute("getAllCreditTypes", js.serialize(ConversionUtils
        .copyListDynaBeansToMap(new CardTypeMasterDAO().listAll(null, "status", "A", null))));
    request.setAttribute("screenId", mapping.getProperty("action_id"));
    String countryCode = centerMasterDAO.getCountryCode(RequestContext.getCenterId());
    if (StringUtil.isNullOrEmpty(countryCode)) {
      countryCode = centerMasterDAO.getCountryCode(0);
    }
    request.setAttribute("defaultCountryCode", countryCode);
    request.setAttribute("countryList", PhoneNumberUtil.getAllCountries());
    List govtIdentifier = new GovtIdentifierMasterDAO().listAll(null, "status", "A");
    request.setAttribute("govtIdentifierTypes", govtIdentifier);
    request.setAttribute("govtIdentifierTypesJSON",
        js.serialize(ConversionUtils.copyListDynaBeansToMap(govtIdentifier)));
    request.setAttribute("regPref", RegistrationPreferencesDAO.getRegistrationPreferences());
    request.setAttribute("cashTransactionLimit",paymentModeMasterDao.getCashLimit());
    return mapping.findForward("getSampleForm");
  }

  /**
   * Save prescriptions.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the action forward
   * @throws Exception the exception
   */
  @IgnoreConfidentialFilters
  @SuppressWarnings("static-access")
  public ActionForward savePrescriptions(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws Exception {

    OhSampleRegistrationBO ohBo = new OhSampleRegistrationBO();
    SampleRegistrationForm srform = (SampleRegistrationForm) form;
    Map<String, String[]> map = request.getParameterMap();
    String salutation = request.getParameter("salutation");
    String pname = salutation.concat(request.getParameter("patientname"));
    String phoneNo = request.getParameter("phone_no");
    Integer govtIdType = 0;
    if (null != request.getParameter("identifier_id")
        && !request.getParameter("identifier_id").equals("")) {
      govtIdType = Integer.parseInt(request.getParameter("identifier_id"));
    }
    String govtId = request.getParameter("government_identifier");
    String phoneNoCountryCode = request.getParameter("phone_no_country_code");
    String ageStr = request.getParameter("age");
    String genderValue = request.getParameter("gender");
    String orginalLabId = request.getParameter("labId");
    String orginalLabName = request.getParameter("orginalLabName");
    String[] sampleId = map.get("sampleId");
    String[] sampleTypeId = map.get("sampleTypeId");
    String[] testId = map.get("testId");
    String[] chargeStr = map.get("charge");
    String billType = request.getParameter("billType");
    HttpSession session = request.getSession(false);
    String userName = (String) session.getAttribute("userid");
    String referralDocId = request.getParameter("referralDocId");
    String referralDocName = request.getParameter("referralDoctorName");
    String category = request.getParameter("category");
    String isAutoGenerateSampleReq = request.getParameter("sampleAutoGeneratedIdReq");
    String printerId = request.getParameter("printer");
    String[] amtStr = map.get("amt");
    String[] discStr = map.get("disc");
    String discountAuth = request.getParameter("discountAuthName");
    String[] origSampleNums = map.get("orig_sample_no");
    String[] testINpackage = map.get("testINpackage");
    String[] isPackage = map.get("isPackage");
    String[] houseType = map.get("houseType");
    String[] outhouseId = map.get("outhouseId");
    String[] packageRefs = map.get("package_ref");
    String[] conductingDoctorIds = map.get("conducting_doctor_id");
    String[] outsourceDestType = map.get("outsourceDestType");
    int discAuth = discountAuth.equals("") ? -1 : Integer.parseInt(discountAuth);
    int centerId = (Integer) request.getSession(false).getAttribute("centerId");
    String patOtherInfo = request.getParameter("patient_other_info");
    String dateOfBirth = request.getParameter("dateOfBirth");
    String ageIn = request.getParameter("ageIn");
    BasicDynaBean diagGenericPref = GenericPreferencesDAO.getdiagGenericPref();
    String noGeneration = (String) diagGenericPref.get("sample_no_generation");
    String[] batchBased = new String[testId.length];
    String[] billRatePlan = map.get("bill_rate_plan_id");
    StringBuilder batchBasedNo = new StringBuilder();
    Date dob = null;
    if (!dateOfBirth.trim().equals("")) {
      dob = Date.valueOf(dateOfBirth);
      ageStr = "";
    } // only dob .. if dob is not there then save age.
    String sampleNos = "";
    String sampleDates = "";
    String sampleTypes = "";
    String needSamplePrint = "N";

    String phoneNumber = map.get("phone_no")[0];
    String defaultCode = centerMasterDAO.getCountryCode(RequestContext.getCenterId());
    List<String> splitCountryCodeAndText = PhoneNumberUtil.getCountryCodeAndNationalPart(
        phoneNumber, null);
    if (splitCountryCodeAndText != null && !splitCountryCodeAndText.isEmpty()
        && !splitCountryCodeAndText.get(0).isEmpty()) {
      phoneNo = "+" + splitCountryCodeAndText.get(0) + splitCountryCodeAndText.get(1);
      phoneNoCountryCode = "+" + splitCountryCodeAndText.get(0);
    } else if (defaultCode != null) {
      if (phoneNo != null && !phoneNo.equals("") && !phoneNumber.startsWith("+")) {
        phoneNo =  "+" + defaultCode + phoneNumber;
        phoneNoCountryCode = defaultCode;
      }
    }

    boolean status = true;
    Connection con = null;
    PreparedStatement ps = null;
    String billNo = null;
    LaboratoryDAO labDao = new LaboratoryDAO();

    ArrayList<BasicDynaBean> incomingMainList = new ArrayList<>();
    ArrayList<BasicDynaBean> incomingList = new ArrayList<>();
    ArrayList<BasicDynaBean> testPrescribedList = new ArrayList<>();
    ArrayList<BasicDynaBean> sampleBeanList = new ArrayList<>();
    ArrayList<BasicDynaBean> bills = new ArrayList<>();
    ArrayList<BasicDynaBean> billCharges = new ArrayList<>();
    ArrayList<BasicDynaBean> packagePrescribedList = new ArrayList<>();
    ArrayList<BasicDynaBean> billActivityCharges = new ArrayList<>();
    ArrayList<BasicDynaBean> outhouseSampleBeanList = new ArrayList<>();
    Map<String, InternalLab> incomingSamplesRegMap = new HashMap<>();

    labDao.removeSpaceFromArray(outhouseId);
    ActionRedirect redirect = new ActionRedirect(mapping.findForward("redirectForm"));
    FlashScope flash = FlashScope.getScope(request);

    try {
      con = DataBaseUtil.getConnection();
      con.setAutoCommit(false);
      HashMap<String, String> visitMap = new HashMap<>();
      HashMap<String, String> labNOMap = new HashMap<>();
      Map<Object, Integer> sampleNotoSampleID = new HashMap<>();
      HashMap<String, String> sampNotoTypemap = new HashMap<>();
      HashMap<String, String> sampleNoToOrigSmplNoMap = new HashMap<>();
      HashMap<String, String> outSourceDestIdMap = new HashMap<>();
      Map<String, String> outSourceChainMap = new HashMap<>();

      outer: do {
        boolean isBatchBased = isAutoGenerateSampleReq.equals("Y") && noGeneration.equals("B");

        // Checking duplicate sample no for manual entry .
        if (isAutoGenerateSampleReq.equals("N") && category.equals("DEP_LAB")) {
          status &= ohBo.isSampleIdsExists(testId, isPackage, sampleId);
        }

        if (!"".equals(orginalLabId) || orginalLabId != null) {
          InComingHospitalDAO dao = new InComingHospitalDAO();
          BasicDynaBean incomingLabBean = null;
          BasicDynaBean exists = dao.findByKey("hospital_name", orginalLabName);
          if (exists == null) {
            incomingLabBean = dao.checkOrCreateIncomingHospital(con, orginalLabName);
            status &= incomingLabBean != null;
          }
        }

        if (!status) {
          break outer;
        }

        if (!"".equals(referralDocId) || referralDocId != null) {
          status &= ohBo.insertReferalDoctor(con, referralDocName);
        }

        if (!status) {
          break outer;
        }

        // used when the tests are mapped with multiple hops and multiple tests collected as same
        // sample type
        String[] outSourceChain = new String[testId.length];
        if (category.equals("DEP_LAB")) {
          outSourceChainMap.putAll(ohBo.createOutSourceChainMap(centerId, testId));
          if (outSourceChainMap != null) {
            for (int i = 0; i < testId.length; i++) {
              String chainkey = testId[i] + "-" + outhouseId[i];
              outSourceChain[i] = outSourceChainMap.get(chainkey);
            }
          }
        }

        if (isAutoGenerateSampleReq.equals("N") && category.equals("DEP_LAB")) {
          sampNotoTypemap.putAll(ohBo.createSampleTypeMap(testId, sampleId, sampleTypeId));
          sampleNoToOrigSmplNoMap
              .putAll(ohBo.createOrigSampleNoMap(testId, sampleId, origSampleNums));
          outSourceDestIdMap.putAll(ohBo.createOutSourceDestMap(testId, sampleId, outhouseId));
        }

        for (int i = 0; i < testId.length; i++) {

          if (!visitMap.containsKey(pname)) {
            ohBo.createVisitMap(pname, visitMap);
            labNOMap.putAll(ohBo.createLabNo(category, pname));

            bills.addAll(ohBo.createBillListBean(con, billType, centerId, pname, userName,
                billRatePlan, discAuth, visitMap));

            incomingMainList.addAll(ohBo.createIncomingSampleBeanMain(pname, orginalLabId, phoneNo,
                phoneNoCountryCode, govtIdType, govtId, ageStr, patOtherInfo, genderValue,
                referralDocId, category, centerId, visitMap, bills, null, null, ageIn, dob));
          }

        }

        if (isAutoGenerateSampleReq.equals("Y") && noGeneration.equals("P")
            && category.equals("DEP_LAB") && testId != null) {
          new PrefixSampleIdGenerator().generatePrefixBasedSampleId(con, testId, isPackage,
              origSampleNums, sampleId, sampleTypeId, centerId, outSourceChain, outhouseId,
              sampNotoTypemap, sampleNoToOrigSmplNoMap, outSourceDestIdMap);

        } else if (isAutoGenerateSampleReq.equals("Y") && noGeneration.equals("B")
            && category.equals("DEP_LAB") && testId != null) {
          sampleId = new String[testId.length];
          batchBased = new String[testId.length];
          new BatchSampleIdGenerator().generateBatchBasedSampleId(con, testId, isPackage, sampleId,
              sampleTypeId, batchBased, batchBasedNo, origSampleNums, outhouseId, sampNotoTypemap,
              sampleNoToOrigSmplNoMap, outSourceDestIdMap);
        }

        if (testId != null) {

          Set<String> sampleSet = null;
          if (category.equals("DEP_LAB")) {
            if (isBatchBased) {
              sampleSet = ohBo.getSampleSet(batchBased);
            } else {
              sampleSet = ohBo.getSampleSet(sampleId);
            }

            sampleBeanList.addAll(ohBo.createSampleBean(sampleSet, sampleTypeId, pname,
                isBatchBased, batchBasedNo.toString(), outhouseId, visitMap, sampNotoTypemap,
                sampleNotoSampleID, sampleNoToOrigSmplNoMap, outSourceDestIdMap, null, false,
                null));
            // use to set the value for printparams fields
            Iterator sampleIt = null;
            sampleIt = null != sampleSet ? sampleSet.iterator() : sampleIt;
            String sampleNo;
            boolean isNumset = false;
            BasicDynaBean sampletypeBean = null;
            int index = 0;
            while (sampleIt.hasNext()) {
              sampleNo = (String) sampleIt.next();
              if (sampleNo.isEmpty()) {
                continue;// possible for Lab test with sample_needed = No
              }

              if (!sampleTypeId[index].equals("")) {
                sampletypeBean = new GenericDAO("sample_type").findByKey("sample_type_id",
                    Integer.parseInt(sampleTypeId[index]));
              }

              if (isBatchBased && !isNumset) {
                sampleNos = sampleNos + "'" + batchBasedNo + "'" + ',';
                isNumset = true;
              } else {
                sampleNos = sampleNos + "'" + sampleNo + "'" + ',';
              }
              if (sampletypeBean != null) {
                sampleTypes = sampleTypes + "'" + sampletypeBean.get("sample_type") + "'" + ",";
              }

              sampleDates = sampleDates + "'"
                  + DataBaseUtil.timeStampFormatter.format(DateUtil.getCurrentTimestamp()) + "'"
                  + ",";
              needSamplePrint = "Y";
              index++;
            }
          }

          int nextPresId = 0;
          String chargeId = "";
          int packageActivityIndex = 0;
          String[] testsRowIndexes = map.get("rowIndex");

          for (int i = 0; i < testId.length; i++) {

            Boolean isPackages = isPackage[i].equalsIgnoreCase("y");

            BasicDynaBean testMasterDetails = null;
            BasicDynaBean packMasterDetails = null;

            String labOrigSampleNo = null;
            String labSampleId = null;
            String labhouseType = null;
            String labBatchBased = null;
            // generate presc id or pkg presc id before setting the value into list for considering
            // the row of the temp package
            nextPresId = ohBo.getPrescriptionId(isPackages);

            // for radiology incoming some fields are not there as per lab incoming for the tests
            // so considering those fields as zero if the is radiology tests.
            if (category.equals("DEP_LAB")) {
              labOrigSampleNo = origSampleNums[i];
              labSampleId = sampleId[i];
              labhouseType = houseType[i];
              labBatchBased = batchBased[i];
            }

            if (!isPackages) {

              incomingList.addAll(ohBo.createIncommingSamplebean(testId[i], pname, category,
                  labOrigSampleNo, visitMap, nextPresId));

              testPrescribedList.addAll(ohBo.createTestPrescribedBean(con, testId[i],
                  ((null != outhouseId) ? outhouseId[i] : null), pname, userName, packageRefs[i],
                  category, labSampleId, isBatchBased, labhouseType, centerId, labBatchBased,
                  visitMap, labNOMap, sampleNotoSampleID, nextPresId, packageActivityIndex));
              testMasterDetails = new GenericDAO("diagnostics").findByKey(con, "test_id",
                  testId[i]);
              if (testINpackage[i].equalsIgnoreCase("n")) {
                chargeId = new ChargeDAO(con).getNextChargeId();
                billCharges.addAll(ohBo.createBillChargeBean(con, testId[i], pname, isPackage[i],
                    testMasterDetails, category, chargeStr[i], packMasterDetails, amtStr[i],
                    discStr[i], userName, discountAuth,
                    (conductingDoctorIds != null ? conductingDoctorIds[i] : null), bills,
                    chargeId));
              }

              billActivityCharges
                  .addAll(ohBo.createBillActivityBean(chargeId, isPackage[i], null, nextPresId,
                      category, (conductingDoctorIds != null ? conductingDoctorIds[i] : null)));
            }
            if (isPackages) {
              packagePrescribedList.addAll(ohBo.createPackagePrescribedBean(con, pname, testId[i],
                  userName, visitMap, packMasterDetails, nextPresId));
              packMasterDetails = packageDao.findByKey(con, "package_id",
                  Integer.parseInt(testId[i]));

              if (testINpackage[i].equalsIgnoreCase("n")) {
                chargeId = new ChargeDAO(con).getNextChargeId();
                billCharges.addAll(ohBo.createBillChargeBean(con, testId[i], pname, isPackage[i],
                    testMasterDetails, category, chargeStr[i], packMasterDetails, amtStr[i],
                    discStr[i], userName, discountAuth,
                    (conductingDoctorIds != null ? conductingDoctorIds[i] : null), bills,
                    chargeId));
              }

              billActivityCharges
                  .addAll(ohBo.createBillActivityBean(chargeId, isPackage[i], nextPresId, null,
                      category, (conductingDoctorIds != null ? conductingDoctorIds[i] : null)));
            }

            if (category.equals("DEP_LAB")) {
              if (houseType[i].equalsIgnoreCase("O")
                  && outsourceDestType[i].equalsIgnoreCase("O")) {
                outhouseSampleBeanList.addAll(ohBo.createOudHouseSampleBean(testId[i], nextPresId,
                    pname, outhouseId[i], category, sampleId[i], visitMap));

              } else if (houseType[i].equalsIgnoreCase("O")
                  && outsourceDestType[i].equalsIgnoreCase("C")) {

                ohBo.createAutomaticSampleRegMap(Integer.parseInt(outhouseId[i]), testId[i],
                    nextPresId, sampleId[i], sampleTypeId[i], pname, visitMap,
                    incomingSamplesRegMap);
              }
            }
          }

          if (!incomingMainList.isEmpty()) {
            status &= new GenericDAO("incoming_sample_registration").insertAll(con,
                incomingMainList);
          }
          if (!incomingList.isEmpty()) {
            status &= new GenericDAO("incoming_sample_registration_details").insertAll(con,
                incomingList);
          }
          if (!sampleBeanList.isEmpty()) {
            for (int i = 0; i < sampleBeanList.size(); i++) {
              BasicDynaBean samplecollBean = sampleBeanList.get(i);
              status &= new GenericDAO("sample_collection").insert(con, samplecollBean);
            }
          }
          if (!testPrescribedList.isEmpty()) {
            for (int i = 0; i < testPrescribedList.size(); i++) {
              BasicDynaBean testPrescBean = testPrescribedList.get(i);
              // this in needed only from application(not from api), and called for each bean
              String error = insertTestDocuments(con, testsRowIndexes[i], packageActivityIndex,
                  srform, userName, category, testPrescBean);
              status &= (error == null);
              status &= new GenericDAO("tests_prescribed").insert(con, testPrescBean);
            }
          }
          if (!packagePrescribedList.isEmpty()) {
            status &= new GenericDAO("package_prescribed").insertAll(con, packagePrescribedList);
          }
          if (!bills.isEmpty()) {
            status &= new GenericDAO("bill").insertAll(con, bills);
          }

          if (!billCharges.isEmpty()) {
            status &= new GenericDAO("bill_charge").insertAll(con, billCharges);
          }

          if (!billActivityCharges.isEmpty()) {
            status &= new GenericDAO("bill_activity_charge").insertAll(con, billActivityCharges);
          }

          if (!outhouseSampleBeanList.isEmpty()) {
            status &= new GenericDAO("outsource_sample_details").insertAll(con,
                outhouseSampleBeanList);
          }

          AutomaticSampleRegistration incomingSampleReg = new AutomaticSampleRegistration();
          if (incomingSamplesRegMap != null) {
            Set<String> keys = incomingSamplesRegMap.keySet();
            Iterator it = keys.iterator();
            while (it.hasNext()) {
              String key = (String) it.next();
              String[] splitParts = key.split(",");
              status &= incomingSampleReg.sampleRegistrationInInternalLab(con,
                  incomingSamplesRegMap.get(key), splitParts[0], diagGenericPref);
              if (!status) {
                break;
              }
            }
          }

          for (int i = 0; i < testPrescribedList.size(); i++) {
            ChargeBO chargeBo = new ChargeBO();
            String chargeID = LaboratoryDAO.getOhTestChargeId(con,
                (Integer) testPrescribedList.get(i).get("prescribed_id"), ChargeDTO.CH_DIAG_LAB);
            status &= chargeBo.updateOhPayment(con, chargeID, centerId);
          }

          Map printParamMap = new HashMap();
          if (category.equals("DEP_LAB")) {
            if (sampleNos != null && !sampleNos.equals("")) {
              sampleNos = sampleNos.substring(0, sampleNos.length() - 1);
              sampleDates = sampleDates.substring(0, sampleDates.length() - 1);
              if (!sampleTypes.equals("")) {
                sampleTypes = sampleTypes.substring(0, sampleTypes.length() - 1);
              }
            }
            printParamMap.put("sampleNo", sampleNos);
            printParamMap.put("sampleDates", sampleDates);
            printParamMap.put("sampleTypes", sampleTypes);
          }

          printParamMap.put("printerTypeStr", printerId);
          printParamMap.put("incomingVisitId", visitMap.get(pname));
          printParamMap.put("needPrint", needSamplePrint);
          printParamMap.put("category", category);
          printParamMap.put("sampleBardCodeTemplate",
              request.getParameter("sampleBardCodeTemplate"));

          Map<String, String[]> requestParams = request.getParameterMap();
          AbstractPaymentDetails ipdImpl = AbstractPaymentDetails
              .getReceiptImpl(AbstractPaymentDetails.INCOMING_PAYMENT);
          List<Receipt> receiptList = ipdImpl.processReceiptParams(requestParams);
          billNo = (String) bills.get(0).get("bill_no");
          BillDAO billDAO = new BillDAO(con);
          Bill bill = billDAO.getBill(billNo);

          if (bill.getBillType().equals("P") && receiptList != null && (!receiptList.isEmpty())) {
            status &= ipdImpl.createReceipts(con, receiptList, bill, bill.getVisitType(),
                bill.getStatus());
            if (status) {

              printParamMap.put("billNo", billNo);
              if (bill.getStatus().equals(Bill.BILL_STATUS_CLOSED)
                  || bill.getStatus().equals(Bill.BILL_STATUS_FINALIZED)) {
                accountingJobScheduler.scheduleAccountingForBill(bill.getVisitId(), billNo);
              }
              int receiptCount = 0;
              for (Receipt receipt : receiptList) {
                if (receipt.getPaymentType().equals("R")
                    && bill.getBillType().equals(Bill.BILL_TYPE_PREPAID)) {
                  receiptCount++;
                }
              }
              
              printParamMap.put("BILLPRINT", (receiptCount == 1) ? "Y" : "N");

              List<String> printURLs = ipdImpl.generatePrintReceiptUrls(receiptList, printParamMap);
              request.getSession(false).setAttribute("printURLs", printURLs);
            }
            // Update the bill total amount.
            allocationService.updateBillTotal(bill.getBillNo());
            
            // Call the allocation method
            allocationService.allocate(billNo, centerId);
          } else if (bill.getBillType().equals("C")) {

            List<String> printURLs = ipdImpl.generatePrintReceiptUrls(null, printParamMap);
            request.getSession(false).setAttribute("printURLs", printURLs);
          }
          
        }
      } while (false);
    } catch (DuplicateSampleIdException exception) {
      status = false;
      flash.error(exception.getMessage());
      redirect.addParameter(FlashScope.FLASH_KEY, flash.key());
      return redirect;
    } catch (Exception exception) {
      status = false;
      throw exception;
    } finally {
      if (status) {
        con.commit();
      } else {
        con.rollback();
      }
      DataBaseUtil.closeConnections(con, ps);
    }

    if (status) {
      flash.success("Samples are registered Successfully..");
    } else {
      flash.error("Failed to register samples..");
    }

    redirect.addParameter("category", category);
    redirect.addParameter(FlashScope.FLASH_KEY, flash.key());

    return redirect;
  }

  /**
   * Insert test documents.
   *
   * @param con                  the con
   * @param rowIndex             the row index
   * @param packageActivityIndex the package activity index
   * @param orderForm            the order form
   * @param userName             the user name
   * @param testCategory         the test category
   * @param testPrescBean        the test presc bean
   * @return the string
   * @throws Exception the exception
   */
  @IgnoreConfidentialFilters
  private String insertTestDocuments(Connection con, String rowIndex, int packageActivityIndex,
      SampleRegistrationForm orderForm, String userName, String testCategory,
      BasicDynaBean testPrescBean) throws Exception {

    String[] clinicalNotesAr = orderForm.getAd_clinical_notes();
    String[] rowIdsAr = orderForm.getAd_main_row_id();
    List uploadedDocAr = orderForm.getAd_test_file_upload();
    Boolean[] notesEdited = orderForm.getAd_notes_entered();
    int[] activityIndexAr = orderForm.getAd_package_activity_index();
    Boolean[] deleteDocument = orderForm.getAd_test_doc_delete();
    AbstractDocumentPersistence persistenceAPI = AbstractDocumentPersistence
        .getInstance("lab_test_doc", true);

    for (int i = 0; i < rowIdsAr.length; i++) {
      if (rowIdsAr[i].equals(rowIndex) && packageActivityIndex == activityIndexAr[i]) {
        if (notesEdited[i]) {
          testPrescBean.set("clinical_notes", clinicalNotesAr[i]);
        }

        if (deleteDocument[i]) {
          continue; // marked for delete so ignore it.
        }
        FormFile file = (FormFile) uploadedDocAr.get(i);
        if (file != null && file.getFileSize() > 0) {
          Map<String, Object[]> newparamMap = new HashMap<>();
          ConversionUtils.copyStringToMap(newparamMap, "username", userName);
          ConversionUtils.copyObjectToMap(newparamMap, "doc_content_bytea", file.getInputStream());
          ConversionUtils.copyObjectToMap(newparamMap, "content_type", file.getContentType());
          ConversionUtils.copyObjectToMap(newparamMap, "fileName", file.getFileName());
          ConversionUtils.copyStringToMap(newparamMap, "doc_date",
              DateUtil.formatDate(new java.util.Date()));

          // insert the document in two cases.
          // 1) user attached a new document for the existing ordered item.
          // 2) user attached a new document for freshly added ordered item.
          ConversionUtils.copyStringToMap(newparamMap, "doc_type",
              testCategory.equals("DEP_LAB") ? "SYS_LR" : "SYS_RR");
          ConversionUtils.copyStringToMap(newparamMap, "prescribed_id",
              String.valueOf(testPrescBean.get("prescribed_id")));
          ConversionUtils.copyStringToMap(newparamMap, "doc_name", "Test Clinical Document");
          ConversionUtils.copyStringToMap(newparamMap, "doc_format", "doc_fileupload");
          ConversionUtils.copyStringToMap(newparamMap, "format", "doc_fileupload");

          boolean success = persistenceAPI.create(newparamMap, con);
          String error = success ? null : (String) newparamMap.get("error")[0];
          newparamMap.clear();

          if (!success) {
            return error;
          }
        }

      }
    }

    return null;
  }

  /**
   * Generate sample collection report.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the action forward
   * @throws JRException    the JR exception
   * @throws IOException    Signals that an I/O exception has occurred.
   * @throws SQLException   the SQL exception
   * @throws ParseException the parse exception
   * @throws Exception      the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward generateSampleCollectionReport(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response)
      throws JRException, IOException, SQLException, ParseException, Exception {

    Template template = null;
    String mrNo = null;
    String patientId = null;

    HashMap map = new HashMap();
    map.put("visitId", request.getParameter("visitid"));
    map.put("sampleNo", request.getParameter("sampleNo"));
    map.put("sampleType", request.getParameter("sampleTypes"));
    String sampleNos = request.getParameter("sampleNo");
    String visitId = request.getParameter("visitid");

    List<BasicDynaBean> sampleDetails = new ArrayList<>();
    sampleDetails = new SampleCollectionDAO().getSampleCollectionPaperPrintDetails(visitId,
        sampleNos);
    if (!sampleDetails.isEmpty()) {
      mrNo = (String) sampleDetails.get(0).get("mr_no");
      patientId = (String) sampleDetails.get(0).get("patient_id");

      if (mrNo == null) {
        map.put("patient", PatientDetailsDAO.getIncomingPatientDetails(patientId));
      } else {
        map.put("patient",
            com.insta.hms.Registration.PatientDetailsDAO.getPatientGeneralDetailsBean(mrNo));
      }
    }
    map.put("sampleDetails", sampleDetails);

    Configuration cfg = AppInit.getFmConfig();
    OutputStream os = null;
    HtmlConverter hc = new HtmlConverter();
    int printerId = 0;
    String printerIdStr = request.getParameter("printType");
    if ((printerIdStr != null) && !printerIdStr.equals("")) {
      printerId = Integer.parseInt(printerIdStr);
    }
    BasicDynaBean printPrefs = PrintConfigurationsDAO
        .getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_SAMPLE_COLLECTION, printerId);

    String printMode = "P";
    if (printPrefs.get("print_mode") != null) {
      printMode = (String) printPrefs.get("print_mode");
    }

    StringWriter writer = new StringWriter();
    PrintTemplatesDAO printtemplatedao = new PrintTemplatesDAO();
    PrintTemplate temp = PrintTemplate.Sample;
    String templateContent = null;
    templateContent = printtemplatedao.getCustomizedTemplate(temp);
    if (templateContent == null || templateContent.equals("")) {
      template = cfg.getTemplate(PrintTemplate.Sample.getFtlName() + ".ftl");
    } else {
      StringReader reader = new StringReader(templateContent);
      template = new Template("SampleCollectionPaperPrint.ftl", reader, AppInit.getFmConfig());
    }

    template.process(map, writer);
    if (printMode.equals("P")) {
      os = response.getOutputStream();
      response.setContentType("application/pdf");
      boolean repeatPatientHeader = ((String) printPrefs.get("repeat_patient_info"))
          .equalsIgnoreCase("Y");
      hc.writePdf(os, writer.toString(), "SampleCollectionPaperPrint", printPrefs, false,
          repeatPatientHeader, true, true, true, false);
      os.close();
      return null;
    } else {
      String textReport = new String(
          hc.getText(writer.toString(), "SampleCollectionPaperPrint", printPrefs, true, true));
      request.setAttribute("textReport", textReport);
      request.setAttribute("textColumns", printPrefs.get("text_mode_column"));
      request.setAttribute("printerType", "DMP");
      return mapping.findForward("textPrintApplet");
    }
  }

  /**
   * Gets the incoming sample bill print.
   *
   * @param am  the am
   * @param af  the af
   * @param req the req
   * @param res the res
   * @return the incoming sample bill print
   * @throws SQLException             the SQL exception
   * @throws IOException              Signals that an I/O exception has occurred.
   * @throws ParseException           the parse exception
   * @throws TemplateException        the template exception
   * @throws DocumentException        the document exception
   * @throws XPathExpressionException the x path expression exception
   * @throws TransformerException     the transformer exception
   */
  @IgnoreConfidentialFilters
  public ActionForward getIncomingSampleBillPrint(ActionMapping am, ActionForm af,
      HttpServletRequest req, HttpServletResponse res)
      throws SQLException, IOException, ParseException, TemplateException, DocumentException,
      XPathExpressionException, TransformerException {

    HashMap paramMap = new HashMap();
    String receiptNo = req.getParameter("receiptNo");
    paramMap.put("receiptNo", receiptNo);
    String billNo = req.getParameter("billNo");
    paramMap.put("billNo", billNo);
    String incomingVisitId = req.getParameter("incomingVisitId");
    paramMap.put("incomingVisitId", incomingVisitId);
    String paymentType = req.getParameter("paymentType");
    paramMap.put("paymentType", paymentType);
    paramMap.put("patientDetails",
        IncomingSamplePendingBillDAO.getIncomingPatientDetails(incomingVisitId));

    String printerIdStr = req.getParameter("printerId");
    int printerId = 1;
    if (printerIdStr != null && !printerIdStr.equals("")) {
      printerId = Integer.parseInt(printerIdStr);
    }

    BasicDynaBean pref = null;
    pref = PrintConfigurationsDAO.getPageOptions(PrintConfigurationsDAO.PRINT_TYPE_DIAG, printerId);
    paramMap.put("printMaster", pref);
    paramMap.put("printerType", req.getParameter("printerId"));
    paramMap.put("printMode", pref.get("print_mode"));
    BillBO bo = new BillBO();
    BillDetails billDetails = bo.getBillDetails(billNo);
    paramMap.put("billDetails", billDetails);

    // Bill Or Receipt Template for Incoming Sample Payment
    Template template = null;
    String category = req.getParameter("category");
    if (req.getParameter("BILLPRINT").equals("Y")) {
      paramMap.put("sampleDetails",
          IncomingSamplePendingBillDAO.getSampleDetailsList(billNo, category));
      List packageDetails = IncomingSamplePendingBillDAO.getPackageDetails(billNo);
      paramMap.put("packageDetails", packageDetails);

      String[] packageIds = new String[packageDetails.size()];

      for (int i = 0; i < packageDetails.size(); i++) {
        BasicDynaBean bean = (BasicDynaBean) packageDetails.get(i);
        packageIds[i] = (String) bean.get("pack_id");
      }
      paramMap.put("testInPackDetails",
          IncomingSamplePendingBillDAO.getTestDetailsInPackage(incomingVisitId, packageIds));

      template = AppInit.getFmConfig().getTemplate("IncomingSampleBill.ftl");
    } else {

      ActionRedirect redirect = new ActionRedirect(
          "billprint.do?_method=receiptRefundPrintTemplate");
      redirect.addParameter("type", paymentType);
      redirect.addParameter("receiptNo", receiptNo);
      redirect.addParameter("printerType", printerId);
      redirect.addParameter("printTemplate", "");
      return redirect;

    }
    StringWriter writer = new StringWriter();
    template.process(paramMap, writer);
    boolean isFinalized = !billDetails.getBill().getStatus().equals("A");
    HtmlConverter hc = new HtmlConverter();
    String templateContent = writer.toString();
    if (pref.get("print_mode").equals("P")) {
      // pdf mode
      OutputStream os = res.getOutputStream();
      res.setContentType("application/pdf");
      hc.writePdf(os, templateContent, "Incoming Sample Bill", pref, false, false, true, true,
          isFinalized, false);
      os.close();
      return null;
    } else {
      // text mode
      String textReport = new String(
          hc.getText(templateContent, "Incoming Sample Bill", pref, true, true));
      req.setAttribute("textReport", textReport);
      req.setAttribute("textColumns", pref.get("text_mode_column"));
      req.setAttribute("printerType", "DMP");
      return am.findForward("textPrintApplet");
    }
  }

  /**
   * Gets the test listin package.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the test listin package
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws Exception   the exception
   */
  @IgnoreConfidentialFilters
  public ActionForward getTestListinPackage(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws Exception {

    String packageId = request.getParameter("packageId");
    int centerId = (Integer) request.getSession(false).getAttribute("centerId");
    String orgId = request.getParameter("orgId");

    List<BasicDynaBean> includedTestList = new ArrayList<>();
    List<BasicDynaBean> testList = new OhSampleRegistrationDAO()
        .getTestsinPackage(Integer.parseInt(packageId), centerId);

    for (BasicDynaBean test : testList) {
      String testId = (String) test.get("test_id");
      Map<String, Object> keys = new HashMap<>();
      keys.put("test_id", testId);
      keys.put("org_id", orgId);
      BasicDynaBean testOrgBean = new GenericDAO("test_org_details").findByKey(keys);
      if ((Boolean) testOrgBean.get("applicable")) {
        includedTestList.add(test);
      }
    }

    response.setContentType("application/x-json");
    response.setHeader("Cache-Control", "no-cache");
    response.getWriter()
        .write(new JSONSerializer().serialize(ConversionUtils.listBeanToListMap(includedTestList)));
    response.flushBuffer();
    return null;
  }

  /**
   * Gets the default bill rate plan.
   *
   * @param mapping  the mapping
   * @param form     the form
   * @param request  the request
   * @param response the response
   * @return the default bill rate plan
   * @throws IOException Signals that an I/O exception has occurred.
   * @throws Exception   the exception
   */
  public ActionForward getDefaultBillRatePlan(ActionMapping mapping, ActionForm form,
      HttpServletRequest request, HttpServletResponse response) throws Exception {
    String incHosp = request.getParameter("selectedHosp");
    response.setContentType("application/x-json");
    response.setHeader("Cache-Control", "no-cache");
    JSONSerializer js = new JSONSerializer();
    response.getWriter().write(js.deepSerialize(
        ConversionUtils.listBeanToListMap(OhSampleRegistrationDAO.getRatePlanNames(incHosp))));
    response.flushBuffer();

    return null;
  }

}

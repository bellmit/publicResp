package testng.com.insta.hms.core.insurance;

import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.core.billing.BillChargeClaimService;
import com.insta.hms.core.billing.BillChargeService;
import com.insta.hms.core.billing.BillChargeTaxService;
import com.insta.hms.core.billing.BillService;
import com.insta.hms.core.billing.ChangeItemRateService;
import com.insta.hms.core.insurance.EditInsuranceService;
import com.insta.hms.core.insurance.SponsorService;
import com.insta.hms.core.inventory.sales.SalesService;
import com.insta.hms.core.patient.registration.PatientInsurancePlanDetailsRepository;
import com.insta.hms.core.patient.registration.PatientInsurancePlanDetailsService;
import com.insta.hms.core.patient.registration.PatientInsurancePlansRepository;
import com.insta.hms.core.patient.registration.PatientInsurancePlansService;
import com.insta.hms.core.patient.registration.PatientInsurancePolicyDetailsRepository;
import com.insta.hms.core.patient.registration.PatientInsurancePolicyDetailsService;
import com.insta.hms.core.patient.registration.PatientRegistrationRepository;
import com.insta.hms.core.patient.registration.PatientRegistrationService;
import com.insta.hms.documents.PlanDocsDetailsService;
import com.insta.hms.mdm.centerpreferences.CenterPreferencesRepository;
import com.insta.hms.mdm.centerpreferences.CenterPreferencesService;
import com.insta.hms.mdm.insuranceplans.InsurancePlanService;
import testng.utils.TestRepoInit;

import org.apache.commons.beanutils.BasicDynaBean;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.springframework.web.multipart.MultipartFile;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Test
@ContextConfiguration(locations = { "classpath:spring/test-spring-config.xml" })
public class EditInsuranceServiceTest extends AbstractTransactionalTestNGSpringContextTests{

  @InjectMocks
  private EditInsuranceService editInsuranceService;
  
  @Mock
  private PatientInsurancePlansService patientInsPlansService;
  
  @Mock
  private PatientInsurancePlanDetailsService patientInsurancePlanDetailsService;
  
  @Mock
  private PlanDocsDetailsService planDocsDetailService;
  
  @Mock
  private SessionService sessionService;
  
  @Mock
  private PatientRegistrationService patientRegistrationService;
  
  @Mock
  private PatientInsurancePolicyDetailsService patientPolicyDetailsService;
  
  @Mock
  private InsurancePlanService insurancePlanService;
  
  @Mock
  private ChangeItemRateService changeItemRateService;
  
  @Mock
  private CenterPreferencesService centerPrefService;
  
  @Mock
  private BillService billService;
  
  @Mock
  private BillChargeService billChargeService;
  
  @Mock
  private BillChargeTaxService billChargeTaxService;
  
  @Mock
  private BillChargeClaimService billChargeClaimService;
  
  @Mock
  private SponsorService sponsorService;
  
  @Mock
  private SalesService salesService;
  
  @Spy
  private PatientRegistrationRepository patientRegistrationRepo;
  
  @Spy
  private PatientInsurancePolicyDetailsRepository  patientInsurancePolicyDetailsRepo;
  
  @Spy
  private PatientInsurancePlansRepository patientInsurancePlanRepo;
  
  @Spy
  private PatientInsurancePlanDetailsRepository patientInsurancePlanDetailRepo;
  
  @Spy
  private CenterPreferencesRepository centerPrefRepo;
  
  private Map<String,Object> dbDataMap = null;
  
  @BeforeMethod
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
    TestRepoInit testRepo = new TestRepoInit();
    testRepo.insert("organization_details");
    testRepo.insert("diagnostics");
    testRepo.insert("test_org_details");
    testRepo.insert("diagnostic_charges");
    testRepo.insert("patient_registration");
    testRepo.insert("bill");
    testRepo.insert("bill_charge");
    testRepo.insert("patient_policy_details");
    //testRepo.insert("patient_insurance_plans");
    dbDataMap = testRepo.initializeRepo(); 
    logger.info("Before every Add/Edit Insurance");
  }
  
  public void updateInsuranceDetailsTestAddInsurance(){
    Map<String, String[]> params = getInsuranceParamsMap();
    Map<String, Map> paramsMap = new HashMap<>();
    Map<String, Object> resultMap = new HashMap<String, Object>();
    Map<String, MultipartFile> fileParams = new HashMap<String, MultipartFile>();
    
    paramsMap.put("params", params);
    paramsMap.put("file_params", fileParams);
    Map<String, Object> keys = new HashMap<>();
    String visitId = params.get("visitId")[0];
    keys.put("patient_id", visitId);
    List<BasicDynaBean> existingVisitInsList = new ArrayList<>();
    Mockito.when(patientInsPlansService.listAll(keys)).thenReturn(existingVisitInsList);
    
    List<Map> visitList = (List) dbDataMap.get("patient_registration");
    //logger.info("patient_registration : "+visitList);
    BasicDynaBean visitBean = patientRegistrationRepo.getBean();
    for (Map visit : visitList) {
      ConversionUtils.copyToDynaBean(visit, visitBean);
    }
    
    Mockito.when(patientRegistrationService.findByKey("patient_id", visitId)).thenReturn(visitBean);
    
    BasicDynaBean policyBean = patientInsurancePolicyDetailsRepo.getBean();
    Mockito.when(patientPolicyDetailsService.getBean()).thenReturn(policyBean);
    
    List<Map> patientPolicyList = (List) dbDataMap.get("patient_policy_details");
    BasicDynaBean patientPolicyBean = patientInsurancePolicyDetailsRepo.getBean();
    for (Map visit : patientPolicyList) {
      ConversionUtils.copyToDynaBean(visit, patientPolicyBean);
    }
    
    Mockito.when(patientPolicyDetailsService.getBean()).thenReturn(policyBean);
    
    Mockito.when(patientPolicyDetailsService.findByKey(Mockito.anyMap())).thenReturn(patientPolicyBean);
    
    BasicDynaBean patientInsurancePlanBean = patientInsurancePlanRepo.getBean();
    Mockito.when(patientInsPlansService.getBean()).thenReturn(patientInsurancePlanBean);
    
    patientInsurancePlanBean.set("patient_insurance_plans_id", 5);
    
    Mockito.when(patientInsPlansService.findByKey(Mockito.anyMap())).thenReturn(patientInsurancePlanBean);
    
    BasicDynaBean patientInsurancePlanDetailBean = patientInsurancePlanDetailRepo.getBean();
    
    Mockito.when(patientInsurancePlanDetailsService.getBean()).thenReturn(patientInsurancePlanDetailBean);
    
    Mockito.when(changeItemRateService.getMultiVisitPackageTPABills(Mockito.anyString(), 
        Mockito.anyString())).thenReturn(new ArrayList<BasicDynaBean>());
    
    Mockito.when(insurancePlanService.findByKey(Mockito.anyInt())).thenReturn(null);
    
    Mockito.when(changeItemRateService.getMultiVisitPackageCashBills(Mockito.anyString(), 
        Mockito.anyString())).thenReturn(null);
    
    BasicDynaBean centerPrefBean = centerPrefRepo.getBean();
    centerPrefBean.set("pref_rate_plan_for_non_insured_bill", "ORG0001");
    Mockito.when(centerPrefService.getCenterPreferences(Mockito.anyInt())).thenReturn(centerPrefBean);
    
    Mockito.when(billService.getNonInsuranceOpenBills(Mockito.anyString())).thenReturn(new ArrayList<BasicDynaBean>());
    
    Mockito.when(billChargeService.getVisitBillCharges(Mockito.anyString())).thenReturn(new ArrayList<BasicDynaBean>());
    
    resultMap = editInsuranceService.updateInsuranceDetails(paramsMap);
  }

  private BasicDynaBean getVisitBean() {
    BasicDynaBean visitBean = (BasicDynaBean)dbDataMap.get("patient_registration");
    return visitBean;
  }

  private Map<String, String[]> getInsuranceParamsMap() {
    // TODO Auto-generated method stub
    Map<String, String[]> paramsMap = new HashMap<String, String[]>();
   
    paramsMap.put("primary_sponsor_id", new String[]{"TPAID0001"});
    paramsMap.put("secondary_sponsor_id", new String[]{"ICM00001"});
    paramsMap.put("primary_plan_id", new String[]{"1"});
    paramsMap.put("secondary_plan_id", new String[]{"2"});
    paramsMap.put("organization", new String[]{"ORG0001"});
    paramsMap.put("insEdited", new String[]{"true"});
    paramsMap.put("visitLimitsChanged", new String[]{"Y"});
    paramsMap.put("primary_policy_validity_start", new String[]{"2018-01-01"});
    
    
    
    paramsMap.put("visitId", new String[]{"OP00001"});
    paramsMap.put("visitType", new String[]{"o"});
    
    paramsMap.put("primary_sponsor_id", new String[]{"TPAID0001"});
    paramsMap.put("secondary_sponsor_id", new String[]{"TPAID0002"});
    
    paramsMap.put("primary_insurance_co", new String[]{"ICM00001"});
    paramsMap.put("secondary_insurance_co", new String[]{"ICM00002"});
    
    paramsMap.put("primary_plan_type", new String[]{"1"});
    paramsMap.put("secondary_plan_type", new String[]{"2"});
    
    paramsMap.put("primary_plan_id", new String[]{"1"});
    paramsMap.put("secondary_plan_id", new String[]{"2"});
    
    paramsMap.put("primary_patient_insurance_plans_id", new String[]{""});
    paramsMap.put("secondary_patient_insurance_plans_id", new String[]{""});
    
    paramsMap.put("organization", new String[]{"ORG0001"});
    paramsMap.put("insEdited", new String[]{"true"});
    paramsMap.put("visitLimitsChanged", new String[]{"Y"});
    paramsMap.put("primary_policy_validity_start", new String[]{"2018-01-01"});
    paramsMap.put("primary_policy_validity_end", new String[]{"2018-12-30"});
    paramsMap.put("primary_member_id", new String[]{"askd23e"});
    paramsMap.put("primary_policy_number", new String[]{"2323"});
    
    paramsMap.put("primary_policy_holder_name", new String[]{"shilpa"});
    paramsMap.put("primary_patient_relationship", new String[]{"self"});
    paramsMap.put("primary_policy_id", new String[]{""});
    
    paramsMap.put("secondary_policy_validity_start", new String[]{"2018-01-01"});
    paramsMap.put("secondary_policy_validity_end", new String[]{"2018-12-30"});
    paramsMap.put("secondary_member_id", new String[]{"askd2rr3e"});
    paramsMap.put("secondary_policy_number", new String[]{"23243"});
    paramsMap.put("secondary_policy_holder_name", new String[]{"shilpa"});
    paramsMap.put("secondary_patient_relationship", new String[]{"self"});
    paramsMap.put("secondary_policy_id", new String[]{""});
    paramsMap.put("primary_use_drg", new String[]{"N"});
    paramsMap.put("secondary_use_drg", new String[]{"N"});
    
    paramsMap.put("primary_use_perdiem", new String[]{"N"});
    paramsMap.put("secondary_use_perdiem", new String[]{"N"});
    
    paramsMap.put("primary_limits_include_followUps", new String[]{"N"});
    paramsMap.put("secondary_limits_include_followUps", new String[]{"N"});
    
    paramsMap.put("primary_prior_auth_id", new String[]{""});
    paramsMap.put("secondary_prior_auth_id", new String[]{""});
    
    paramsMap.put("primary_prior_auth_mode_id", new String[]{""});
    paramsMap.put("secondary_prior_auth_mode_id", new String[]{""});
    
    paramsMap.put("primary_plan_limit", new String[]{"5000"});
    paramsMap.put("primary_visit_limit", new String[]{"1000"});
    
    paramsMap.put("primary_visit_deductible", new String[]{"100"});
    paramsMap.put("primary_visit_copay", new String[]{"10"});
    paramsMap.put("primary_max_copay", new String[]{"50"});
    paramsMap.put("primary_perday_limit", new String[]{"0"});
    paramsMap.put("primary_plan_utilization", new String[]{"4000"});
    
    paramsMap.put("P_cat_name", new String[]{"General","Laboratory","Service"});
    paramsMap.put("P_cat_id", new String[]{"-1","3","9"});
    paramsMap.put("P_sponser_limit", new String[]{"300","200","400"});
    paramsMap.put("P_cat_deductible", new String[]{"10","15","20"});
    paramsMap.put("P_item_deductible", new String[]{"0","0","0"});
    paramsMap.put("P_copay_percent", new String[]{"10","10","10"});
    paramsMap.put("P_max_copay", new String[]{"50","60","70"});
    
    paramsMap.put("S_cat_name", new String[]{"General","Laboratory","Service"});
    paramsMap.put("S_cat_id", new String[]{"-1","3","9"});
    paramsMap.put("S_sponser_limit", new String[]{"250","150","75"});
    paramsMap.put("S_cat_deductible", new String[]{"5","10","12"});
    paramsMap.put("S_item_deductible", new String[]{"0","0","0"});
    paramsMap.put("S_copay_percent", new String[]{"5","5","5"});
    paramsMap.put("S_max_copay", new String[]{"5","6","7"});
    
    paramsMap.put("primary_sponsor_cardfileLocationI", new String[]{""});
    paramsMap.put("primary_insurance_doc_name", new String[]{""});
    paramsMap.put("primary_insurance_format", new String[]{""});
    paramsMap.put("primary_insurance_doc_type", new String[]{""});
    paramsMap.put("primary_insurance_doc_date", new String[]{""});
    
    paramsMap.put("secondary_sponsor_cardfileLocationI", new String[]{""});
    paramsMap.put("secondary_insurance_doc_name", new String[]{""});
    paramsMap.put("secondary_insurance_format", new String[]{""});
    paramsMap.put("secondary_insurance_doc_type", new String[]{""});
    paramsMap.put("secondary_insurance_doc_date", new String[]{""});
    
    paramsMap.put("bills_to_change_sponsor_amounts", new String[]{"open_bills"});
  
    return paramsMap;
  }
}

package testng.com.insta.hms.mdm.insuranceplans;

import static org.testng.Assert.assertEquals;

import com.insta.hms.common.DynaBeanBuilder;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.mdm.insurancecategory.InsuranceCategoryService;
import com.insta.hms.mdm.insurancecompanies.InsuranceCompanyService;
import com.insta.hms.mdm.insuranceitemcategories.InsuranceItemCategoryService;
import com.insta.hms.mdm.insuranceplandetails.InsurancePlanDetailsRepository;
import com.insta.hms.mdm.insuranceplans.InsurancePlanRepository;
import com.insta.hms.mdm.insuranceplans.InsurancePlanService;
import com.insta.hms.mdm.tpas.TpaService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import testng.utils.TestRepoInit;

@Test
@ContextConfiguration(locations = { "classpath:spring/test-spring-config.xml" })
public class InsurancePlanServiceTest extends AbstractTransactionalTestNGSpringContextTests {

  private Logger logger = LoggerFactory.getLogger(InsurancePlanServiceTest.class);

  @Mock
  private InsuranceItemCategoryService insuranceItemCategoryService;

  @Mock
  private TpaService tpaService;

  @Mock
  private SessionService sessionService;

  @Mock
  private GenericPreferencesService genericPreferencesService;

  @Mock
  private InsuranceCategoryService insuranceCategoryService;

  @Mock
  private InsuranceCompanyService insuranceCompanyService;

  @Spy
  private InsurancePlanRepository insurancePlanRepository;

  @Spy
  private InsurancePlanDetailsRepository insurancePlanDetailsRepository;

  @InjectMocks
  private InsurancePlanService insurancePlanService;

  private Map<String, Object> dbDataMap = null;

  @BeforeMethod
  public void initMocks() {
    MockitoAnnotations.initMocks(this);
    TestRepoInit testRepo = new TestRepoInit();
    testRepo.insert("insurance_company_master");
    testRepo.insert("tpa_master");
    testRepo.insert("insurance_company_tpa_master");
    testRepo.insert("item_insurance_categories");
    testRepo.insert("patient_category_master");
    testRepo.insert("center_preferences");
    testRepo.insert("insurance_category_center_master");
    testRepo.insert("insurance_category_master");
    testRepo.insert("insurance_plan_main");
    testRepo.insert("insurance_plan_details");
    testRepo.insert("patient_insurance_plan_details");
    testRepo.insert("insurance_company_category_mapping");
    testRepo.insert("discount_plan_main");
    testRepo.insert("organization_details");
    testRepo.insert("case_rate_main");
    testRepo.insert("case_rate_detail");
    dbDataMap = testRepo.initializeRepo();
    logger.info("Before every Plan Master Test");
  }

  @SuppressWarnings("unchecked")
  public void mockedDependentServices() {
    Mockito.when(insuranceItemCategoryService.getItemInsuranceCategory())
        .thenReturn(new ArrayList<BasicDynaBean>());
    Mockito.when(tpaService.getTpasNamesAndIds()).thenReturn(new ArrayList<BasicDynaBean>());
    DynaBeanBuilder beanBuilder = new DynaBeanBuilder();
    beanBuilder.add("max_centers_inc_default", Integer.class);
    BasicDynaBean genericPrefBean = beanBuilder.build();
    genericPrefBean.set("max_centers_inc_default", 1);
    Mockito.when(genericPreferencesService.getAllPreferences()).thenReturn(genericPrefBean);
    Mockito.when(insuranceCategoryService.getInsuranceCategoryActiveList(Mockito.anyInt()))
        .thenReturn(new ArrayList<BasicDynaBean>());
    Mockito.when(tpaService.getCompanyTpaList()).thenReturn(new ArrayList<BasicDynaBean>());
    Mockito.when(insuranceCompanyService.getInsuranceCompaniesNamesAndIds())
        .thenReturn(new ArrayList<BasicDynaBean>());
  }

  public Map<String, Object> getSessionServiceAttributes() {
    Map<String, Object> sessionAttributes = new HashMap<String, Object>();
    sessionAttributes.put("userId", "Anand");
    sessionAttributes.put("centerId", 1);
    return sessionAttributes;
  }

  @Test
  private void listAllOpActivePlans() {
    logger.info("TestCase 1A: List all op applicable active insurance plan");
    Map<String, String> filterMap = new HashMap<String, String>();
    filterMap.put("status", "A");
    filterMap.put("op_applicable", "Y");
    List<BasicDynaBean> activeOpApplicablePlans = insurancePlanService.listAll(filterMap);
    List<Map> insurancePlanList = (List) dbDataMap.get("insurance_plan_main");
    int csvPlanSize = 0;
    int dbPlanSize = 0;
    for (Map<String, Object> plan : insurancePlanList) {
      if (plan.get("status").equals(filterMap.get("status"))
          && plan.get("op_applicable").equals(filterMap.get("op_applicable"))) {
        csvPlanSize++;
        for (BasicDynaBean activeOpAppPlan : activeOpApplicablePlans) {
          if (activeOpAppPlan.get("status").equals(filterMap.get("status"))
              && activeOpAppPlan.get("op_applicable").equals(filterMap.get("op_applicable"))) {
            if (Integer
                .valueOf((String) plan.get("plan_id")) == (int) activeOpAppPlan.get("plan_id")) {
              dbPlanSize++;
              break;
            }
          }
        }
      }
    }
    Assert.assertEquals(dbPlanSize, csvPlanSize);
  }

  @Test
  private void listAllOpInactivePlans() {
    logger.info("TestCase 1B: List all op applicable inactive insurance plan");
    Map<String, String> filterMap = new HashMap<String, String>();
    filterMap.put("status", "I");
    filterMap.put("op_applicable", "Y");
    List<BasicDynaBean> inActiveOpApplicablePlans = insurancePlanService.listAll(filterMap);
    List<Map> insurancePlanList = (List) dbDataMap.get("insurance_plan_main");
    int csvPlanSize = 0;
    int dbPlanSize = 0;
    for (Map<String, Object> plan : insurancePlanList) {
      if (plan.get("status").equals(filterMap.get("status"))
          && plan.get("op_applicable").equals(filterMap.get("op_applicable"))) {
        csvPlanSize++;
        for (BasicDynaBean inActiveOpAppPlan : inActiveOpApplicablePlans) {
          if (inActiveOpAppPlan.get("status").equals(filterMap.get("status"))
              && inActiveOpAppPlan.get("op_applicable").equals(filterMap.get("op_applicable"))) {
            if (Integer
                .valueOf((String) plan.get("plan_id")) == (int) inActiveOpAppPlan.get("plan_id")) {
              dbPlanSize++;
              break;
            }
          }
        }
      }
    }
    Assert.assertEquals(dbPlanSize, csvPlanSize);
  }

  @Test
  private void listAllIpActivePlans() {
    logger.info("TestCase 2A: List all ip applicable active insurance plan");
    Map<String, String> filterMap = new HashMap<String, String>();
    filterMap.put("status", "A");
    filterMap.put("ip_applicable", "Y");
    List<BasicDynaBean> activeIpApplicablePlans = insurancePlanService.listAll(filterMap);
    List<Map> insurancePlanList = (List) dbDataMap.get("insurance_plan_main");
    int csvPlanSize = 0;
    int dbPlanSize = 0;
    for (Map<String, Object> plan : insurancePlanList) {
      if (plan.get("status").equals(filterMap.get("status"))
          && plan.get("op_applicable").equals(filterMap.get("op_applicable"))) {
        csvPlanSize++;
        for (BasicDynaBean activeIpAppPlan : activeIpApplicablePlans) {
          if (activeIpAppPlan.get("status").equals(filterMap.get("status"))
              && activeIpAppPlan.get("op_applicable").equals(filterMap.get("op_applicable"))) {
            if (Integer
                .valueOf((String) plan.get("plan_id")) == (int) activeIpAppPlan.get("plan_id")) {
              dbPlanSize++;
              break;
            }
          }
        }
      }
    }
    Assert.assertEquals(dbPlanSize, csvPlanSize);
  }

  @Test
  private void listAllIpInactivePlans() {
    logger.info("TestCase 2B: List all ip applicable inactive insurance plan");
    Map<String, String> filterMap = new HashMap<String, String>();
    filterMap.put("status", "I");
    filterMap.put("ip_applicable", "Y");
    List<BasicDynaBean> inActiveIpApplicablePlans = insurancePlanService.listAll(filterMap);
    List<Map> insurancePlanList = (List) dbDataMap.get("insurance_plan_main");
    int csvPlanSize = 0;
    int dbPlanSize = 0;
    for (Map<String, Object> plan : insurancePlanList) {
      if (plan.get("status").equals(filterMap.get("status"))
          && plan.get("op_applicable").equals(filterMap.get("op_applicable"))) {
        csvPlanSize++;
        for (BasicDynaBean inActiveIpAppPlan : inActiveIpApplicablePlans) {
          if (inActiveIpAppPlan.get("status").equals(filterMap.get("status"))
              && inActiveIpAppPlan.get("op_applicable").equals(filterMap.get("op_applicable"))) {
            if (Integer
                .valueOf((String) plan.get("plan_id")) == (int) inActiveIpAppPlan.get("plan_id")) {
              dbPlanSize++;
              break;
            }
          }
        }
      }
    }
    Assert.assertEquals(dbPlanSize, csvPlanSize);
  }

  @Test
  private void getMappedPlansWithNullKey() {
    logger.info("TestCase 3A: Test case for getMappedPlans method with Null Key");
    List<BasicDynaBean> mappedPlan = insurancePlanService.getMappedPlans(null, null);
    Assert.assertEquals(mappedPlan.size(), 0);
  }

  @Test
  private void getMappedPlansWithEmptyKey() {
    logger.info("TestCase 3B: Test case for getMappedPlans method with Null and empty Key");
    List<BasicDynaBean> mappedPlan = insurancePlanService.getMappedPlans(null, "");
    Assert.assertEquals(mappedPlan.size(), 0);
  }

  @Test
  private void getMappedPlans() {
    logger.info("TestCase 3C: Test case for getMappedPlans method");
    Integer categoryId = 133;
    String tpaId = "TPAID0100";
    List<BasicDynaBean> mappedPlan = insurancePlanService.getMappedPlans(categoryId, tpaId);
    Assert.assertEquals(mappedPlan.size(), 1);
  }

  @Test
  private void getPlanDefaultRatePlanWithNullKey() {
    logger.info("TestCase 4A: Test case for getPlanDefaultRatePlan method with Null Key");
    List<BasicDynaBean> planDefaultRatePlans = insurancePlanService.getPlanDefaultRatePlan(null);
    Assert.assertEquals(planDefaultRatePlans.size(), 0);
  }

  @Test
  private void getPlanDefaultRatePlan() {
    logger.info("TestCase 4B: Test case for getPlanDefaultRatePlan method");
    List<BasicDynaBean> planDefaultRatePlans = insurancePlanService.getPlanDefaultRatePlan(1);
    Assert.assertEquals(planDefaultRatePlans.size(), 1);
  }

  @Test
  private void getPlanNamesForSponsorWithNullKey() {
    logger.info(
        "TestCase 5: Getting Plan Names for a Sponsor with category id and center id as null");
    List<BasicDynaBean> plansForSponsor = insurancePlanService.getPlanNamesForSponsor("TPAID0091",
        null, null);
    Assert.assertEquals(plansForSponsor.size(), 2);
  }

  @Test
  private void getPlanNamesForSponsor() {
    logger.info("TestCase 5: Getting Plan Names for a Sponsor");
    List<BasicDynaBean> plansForSponsor = insurancePlanService.getPlanNamesForSponsor("TPAID0091",
        1, 0);
    Assert.assertEquals(plansForSponsor.size(), 2);
  }

  @Test
  private void getChargeAmtForPlanOpApplicable() {
    logger.info("TestCase 6A: Test case for getChargeAmtForPlan method for op");
    BasicDynaBean planDetails = insurancePlanService.getChargeAmtForPlan(1, 7, "o");
    Assert.assertNotNull(planDetails);
  }

  @Test
  private void getChargeAmtForPlanIpApplicable() {
    logger.info("TestCase 6B: Test case for getChargeAmtForPlan method for ip");
    BasicDynaBean planDetails = insurancePlanService.getChargeAmtForPlan(1, 7, "i");
    Assert.assertNotNull(planDetails);
  }

  @Test
  private void findByKey() {
    logger.info("TestCase 7: Find the Plan by using plan id as filter key ");
    List<Map> insurancePlanList = (List) dbDataMap.get("insurance_plan_main");
    for (Map<String, Object> map : insurancePlanList) {
      int planId = Integer.parseInt(map.get("plan_id").toString());
      BasicDynaBean planBean = insurancePlanService.findByKey(planId);
      Assert.assertNotNull(planBean);
    }

  }

  @Test
  private void findByKeys() {
    logger.info("TestCase 8: Find the Plan by using filter as map ");
    List<Map> insurancePlanList = (List) dbDataMap.get("insurance_plan_main");
    for (Map<String, Object> map : insurancePlanList) {
      int planId = Integer.parseInt(map.get("plan_id").toString());
      Map<String, Object> planKey = new HashMap<>();
      planKey.put("plan_id", planId);
      BasicDynaBean planBean = insurancePlanService.findByKey(planKey);
      Assert.assertNotNull(planBean);
    }

  }

  @Test
  private void getAddShowPageDataWithNullKey() {
    logger.info("TestCase 9A: addShow page data fetch with plan id null");
    mockedDependentServices();
    Map<String, String[]> paramsMap = new HashMap<>();
    Map<String, List<BasicDynaBean>> addEditReference = insurancePlanService
        .getAddShowPageData(paramsMap);
    Assert.assertNotNull(addEditReference.get("insuranceCompaniesLists"));
  }

  @Test
  private void getAddShowPageData() {
    logger.info("TestCase 9B: addShow page data fetch");
    mockedDependentServices();
    Map<String, String[]> paramsMap = new HashMap<String, String[]>();
    paramsMap.put("plan_id", new String[] { "1" });
    Map<String, List<BasicDynaBean>> addEditReference = insurancePlanService
        .getAddShowPageData(paramsMap);
    Assert.assertNotNull(addEditReference.get("insuPayableList"));
    Assert.assertNotNull(addEditReference.get("tpaMasterLists"));
    Assert.assertNotNull(addEditReference.get("categoryLists"));
    Assert.assertNotNull(addEditReference.get("defaultDiscountPlanList"));
  }

  @Test
  public void toBean() {
    logger.info("TestCase 10: toBean Method Unit Test");
    Map<String, String[]> requestParams = new HashMap<>();
    requestParams.put("plan_id", new String[] { "1" });
    requestParams.put("plan_name", new String[] { "Insurance Plan" });
    requestParams.put("category_id", new String[] { "133" });
    requestParams.put("insurance_co_id", new String[] { "ICM00002" });
    Mockito.when(sessionService.getSessionAttributes()).thenReturn(getSessionServiceAttributes());
    BasicDynaBean planBean = insurancePlanService.toBean(requestParams, null);
    Assert.assertNotNull(planBean);
  }

  @Test
  public void checkIsGeneralCategoryExistsForRegChargesForTrue() {
    logger.info("TestCase 11A: check general Category exists for Registration Charges with true");
    Boolean isGeneralCatExist = insurancePlanService
        .checkIsGeneralCategoryExistsForRegCharges("OP121788", 1, "o");
    Assert.assertTrue(isGeneralCatExist);
  }

  @Test
  public void checkIsGeneralCategoryExistsForRegChargesForFalse() {
    logger.info("TestCase 11B: check general Category exists for Registration Charges with false");
    Boolean isGeneralCatExist = insurancePlanService
        .checkIsGeneralCategoryExistsForRegCharges("OP121789", 1, "i");
    Assert.assertFalse(isGeneralCatExist);
  }

  @Test
  public void getInsuCatMappedToInsuComp() {
    logger.info("TestCase 12: get Insurance Category mapped to Insurance Company");
    List<BasicDynaBean> plansForSponsor = insurancePlanService
        .getInsuCatMappedToInsuComp("ICM00002");
    Assert.assertEquals(20, plansForSponsor.size());
  }

  @Test
  public void getPlansByCategoryTest() {
    Map<String, Object> res = this.insurancePlanService.getPlansByCategory(133, "ICM00002");
    List<Map<String, Object>> mapList = (List) res.get("planList");
    assertEquals(mapList.get(0).get("plan_id"), 1);
  }
}

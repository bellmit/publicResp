package testng.com.insta.hms.core.clinical.order.master;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insta.hms.common.ConversionUtils;
import com.insta.hms.common.DynaBeanBuilder;
import com.insta.hms.common.InstaLinkedMultiValueMap;
import com.insta.hms.common.preferences.genericpreferences.GenericPreferencesService;
import com.insta.hms.common.session.SessionService;
import com.insta.hms.core.billing.BillChargeRepository;
import com.insta.hms.core.billing.BillChargeTaxService;
import com.insta.hms.core.billing.BillRepository;
import com.insta.hms.core.billing.BillService;
import com.insta.hms.core.clinical.order.doctoritems.DoctorOrderItemService;
import com.insta.hms.core.clinical.order.master.OrderBillSelection;
import com.insta.hms.core.clinical.order.master.OrderRepository;
import com.insta.hms.core.clinical.order.master.OrderService;
import com.insta.hms.core.clinical.order.operationitems.OperationOrderItemService;
import com.insta.hms.core.clinical.order.packageitems.MultiVisitPackageService;
import com.insta.hms.core.clinical.order.packageitems.PackageOrderItemService;
import com.insta.hms.core.patient.PatientDetailsService;
import com.insta.hms.core.patient.registration.PatientInsurancePlansService;
import com.insta.hms.core.patient.registration.RegistrationService;
import com.insta.hms.integration.priorauth.PriorAuthorizationService;
import com.insta.hms.mdm.anaesthesia.AnaesthesiaTypeService;
import com.insta.hms.mdm.centerpreferences.CenterPreferencesService;
import com.insta.hms.mdm.centers.CenterService;
import com.insta.hms.mdm.chargeheads.ChargeHeadsService;
import com.insta.hms.mdm.consultationtypes.ConsultationTypesService;
import com.insta.hms.mdm.discountplans.DiscountPlanService;
import com.insta.hms.mdm.hospitalcenters.HospitalCenterService;
import com.insta.hms.mdm.packages.PackagesService;
import com.insta.hms.mdm.servicegroup.ServiceGroupService;
import com.insta.hms.mdm.servicesubgroup.ServiceSubGroupService;

import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.io.FileUtils;
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

import testng.utils.MockSessionAttributes;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Test
@ContextConfiguration(locations = { "classpath:spring/test-spring-config.xml" })
public class OrderOrderableItemTest extends AbstractTransactionalTestNGSpringContextTests{

	private static final String FILENAME = "OrderableItem.json";

	@Spy
	@InjectMocks
	private OrderService orderService;
	
	@Mock
	private ServiceGroupService serviceGroupService;
	
	@Mock
	private ServiceSubGroupService serviceSubGroupService;
	
	@Mock
	private AnaesthesiaTypeService anesthesiaTypeService;

	@Mock
	private ConsultationTypesService consultationTypesService;
	
	@Mock
	private ChargeHeadsService chargeHeadsService;

	@Mock
	private PriorAuthorizationService priorAuthService;
	
	@Mock
	private CenterPreferencesService centerPrefService;
	
	@Mock
	private SessionService sessionService;

	@Mock
	private CenterService centerService;

	@Mock
	private GenericPreferencesService genericPreferencesService;

	@Mock
	private  MultiVisitPackageService multiVisitPackageService;

	@Mock
	private PackagesService packagesService;

	@Mock
	private OrderRepository orderRepository;

	@Mock
	private DoctorOrderItemService doctorOrderService;

	@Mock
	private OrderBillSelection orderBillSelection;

	@Mock
	private PackageOrderItemService packageOrderItemService;

	@Mock
	private BillService billService;

	@Mock
	private PatientInsurancePlansService patientInsurancePlansService;

	@Mock
	private DiscountPlanService discountPlanService;

	@Mock
	private RegistrationService registrationService;

	@Mock
	private PatientDetailsService patientdetailsService;

	@Mock
	private HospitalCenterService hospitalCenterService;

	@Mock
	private BillChargeTaxService billChargeTaxService;

	@Mock
	private OperationOrderItemService operationOrderService;

	@Spy
	private BillChargeRepository billChargeRepository;

	@Spy BillRepository billRepository;

	private Map<String, Object> mockData;
	
	@BeforeMethod
	public void initMocks() throws IOException {
		MockitoAnnotations.initMocks(this);
		ClassLoader classLoader = getClass().getClassLoader();
		String jsonString = FileUtils.readFileToString(new File(classLoader.getResource(FILENAME)
		    .getFile()), "UTF-8");
		ObjectMapper mapper = new ObjectMapper();
		this.mockData = mapper.readValue(jsonString,
		  new TypeReference<Map<String, Object>>() {
		  });
	}
	  
	@Test
	public void getBasicOrderInfo(){
		List list = new ArrayList();
		DynaBeanBuilder builder = new DynaBeanBuilder();
		builder.add("key", Integer.class);
		BasicDynaBean bean = builder.build();
		Mockito.when(sessionService.getSessionAttributes()).thenReturn(new MockSessionAttributes().getMockSessionAttributes());
		Mockito.when(centerPrefService.getCenterPreferences(Mockito.anyInt())).thenReturn(bean);
		Mockito.when(priorAuthService.listAll()).thenReturn(list);
		Mockito.when(serviceSubGroupService.listOrderActiveRecord()).thenReturn(list);
		Mockito.when(anesthesiaTypeService.listAll(null, "status", "A", null)).thenReturn(list);
		Mockito.when(consultationTypesService.getConsultationTypes("o")).thenReturn(list);
		Mockito.when(chargeHeadsService.getOtDoctorChargeHeads()).thenReturn(list);
		Map<String, Object> result = orderService.getBasicOrderInfo("o");
		List serviceGroups = (List) result.get("service_groups");
		Assert.assertEquals(serviceGroups.size(),0);
	}

	 @Test
	 public void getConsultationTypes() {
	   List list = new ArrayList<>();
	   Mockito.when(sessionService.getSessionAttributes()).thenReturn(new MockSessionAttributes().getMockSessionAttributes());
	   DynaBeanBuilder builder = new DynaBeanBuilder();
	   builder.add("health_authority");
	   builder.add("operation_apllicable_for");
	   BasicDynaBean bean = builder.build();
	   bean = Mockito.spy(bean);
	   Mockito.when(centerService.findByPk(Mockito.anyMap())).thenReturn(bean);
	   Mockito.when(bean.get("health_authority")).thenReturn("Default");
	   Mockito.when(genericPreferencesService.getAllPreferences()).thenReturn(bean);
	   Mockito.when(bean.get("operation_apllicable_for")).thenReturn("b");
	   ArrayList<String> orgIds = new ArrayList<>();
	   orgIds.add("ORG0001");
	   Mockito.when(consultationTypesService.getConsultationTypes("o", orgIds,
         "Default")).thenReturn(list);
	   Mockito.when(consultationTypesService.getConsultationTypes("o", "ot",
		orgIds, "Default")).thenReturn(list);
	   Mockito.when(consultationTypesService.getConsultationTypes("i", "ot",
		orgIds, "Default")).thenReturn(list);
	   Mockito.when(consultationTypesService.getConsultationTypes("i",orgIds,
		"Default")).thenReturn(list);
	   List check1 = orderService.getConsultationTypes(orgIds, "o", "N");
	   List check2 = orderService.getConsultationTypes(orgIds, "o", "Y");
	   List check3 = orderService.getConsultationTypes(orgIds, "i", "Y");
	   List check4 = orderService.getConsultationTypes(orgIds, "i", "N");
	   List check5 = orderService.getConsultationTypes(orgIds, "p", "N");

	   Mockito.when(bean.get("operation_apllicable_for")).thenReturn("p");

	   List check6 = orderService.getConsultationTypes(orgIds, "p", "Y");

	   Assert.assertEquals(check1.size(), 0);
	   Assert.assertEquals(check2.size(), 0);
	   Assert.assertEquals(check3.size(), 0);
	   Assert.assertEquals(check4.size(), 0);
	   Assert.assertEquals(check5.size(), 0);
	   Assert.assertEquals(check6.size(), 0);
	 }


	@Test
	public void getPackageComponents() {
		DynaBeanBuilder builder = new DynaBeanBuilder();
		builder.add("pref_rate_plan_for_non_insured_bill", String.class);
		BasicDynaBean centerPrefBean = builder.build();
		centerPrefBean.set("pref_rate_plan_for_non_insured_bill", "ORG0001");
		List list = new ArrayList();
		Mockito.when(multiVisitPackageService.
			getOrderedPackageItems(Mockito.anyString())).
			thenReturn(list);
		Mockito.when(packagesService.getPackageComponents(Mockito.anyInt())).
			thenReturn(list);
		Mockito.when(centerPrefService.getCenterPreferences(Mockito.anyInt())).
			thenReturn(centerPrefBean);
		List result = orderService.getPackageComponentDetails(119);
		Assert.assertEquals(result.size(), 0);	
	}

	@Test
	public void getOrderPackageDetails() {
		List list = new ArrayList();
		Mockito.when(packagesService.getPackageComponentDetails(Mockito.anyInt())).
			thenReturn(list);
		List result = packagesService.getPackageComponentDetails(119);
		Assert.assertEquals(result.size(), 0);
	}

	@Test
	public void getOrderableItem() {
		Map<String,List<String>> params = (Map<String, List<String>>) this.mockData.
			get("orderable_item_params");
		DynaBeanBuilder builder = new DynaBeanBuilder();
		builder.add("max_centers_inc_default");
		BasicDynaBean bean = builder.build();
		bean = Mockito.spy(bean);
		List<BasicDynaBean> orderableItems = new ArrayList<>();
		List<LinkedHashMap<String,String>> orderItems= (List<LinkedHashMap<String, String>>) this.mockData.
				get("orderable_item_result");
		for (LinkedHashMap<String,String> lm : orderItems) {
			DynaBeanBuilder build = new DynaBeanBuilder();
			build.add("status");
			build.add("entity");
			build.add("service_group_id");
			build.add("service_sub_group_id");
			build.add("orderable");
			build.add("entity_id");
			BasicDynaBean dyBean = build.build("orderable_item");
			ConversionUtils.copyToDynaBean(lm, dyBean, new ArrayList(),false);
			orderableItems.add(dyBean);
		}
		Mockito.when(genericPreferencesService.getPreferences()).thenReturn(bean);
		Mockito.when(bean.get("max_centers_inc_default")).thenReturn(2);
		Mockito.when(orderRepository.getOrderableItems(Mockito.anyMap(),Mockito.any(String[].class),
				Mockito.anyList())).thenReturn(orderableItems);
		Mockito.when(doctorOrderService.getDoctorPackageItemDetail()).thenReturn(bean);
		List result = orderService.getOrderableItem(new InstaLinkedMultiValueMap<String,String>(params));
		Assert.assertEquals(result.size(), 1);
	}

	/*@Test
	public void getItemChargeEstimate() throws ParseException, SQLException {
	  Map<String,Object> charges = (Map<String, Object>) this.mockData.
        get("get_charges");
	  List<BasicDynaBean> list = new ArrayList<>();
	  BasicDynaBean chargeListBean = billChargeRepository.getBean();
	  BasicDynaBean billBean = billRepository.getBean();
	  billBean.set("is_tpa", true);
	  ConversionUtils.copyJsonToDynaBean(charges, chargeListBean, list, false);
	  List<BasicDynaBean> chargeList = new ArrayList<>();
	  chargeList.add(chargeListBean);
	  Map<String,Object> params = (Map<String, Object>) this.mockData.
	      get("item_charge_params");
	  Map<String,Object> billRatePlan = (Map<String, Object>) this.mockData.
        get("bill_rate_plan");
	  Map<String,Object> estimateAmt1 = (Map<String, Object>) this.mockData.
        get("estimate_amt_map1");

	  for(String el : estimateAmt1.keySet()) {
	    Map<String,Object> childMap = (Map<String,Object>) estimateAmt1.get(el);
	    childMap.put("insurance_claim_amt",
	        new BigDecimal((Double)childMap.get("insurance_claim_amt")));
	    childMap.put("tax_amt",
          new BigDecimal((Double)childMap.get("tax_amt")));
	  }
	  Map<String,Object> estimateAmt2 = (Map<String, Object>) this.mockData.
        get("estimate_amt_map2");

	  Map<Integer,Object> estimateAmtMap1 = new LinkedHashMap<>();
	  estimateAmtMap1.put(22, estimateAmt1);
	  estimateAmtMap1.put(-2, estimateAmt2);

	  Map<String,Object> estimateAmtMap = new LinkedHashMap<>();
	  estimateAmtMap.put("estimate_amount",estimateAmtMap1);

	  DynaBeanBuilder builder = new DynaBeanBuilder();
	  builder.add("is_tpa",Boolean.class);
	  builder.add("discount_plan_id");
	  builder.add("discount_category_id");
	  builder.add("visit_id");
	  builder.add("mr_no");
	  BasicDynaBean bean = builder.build();
	  bean.set("is_tpa", true);
	  bean = Mockito.spy(bean);

	  Mockito.when(billService.findByKey("bill_no", "BC18000062")).thenReturn(bean);
	  Mockito.when(orderBillSelection.billSelectionRatePlan(Mockito.anyMap())).thenReturn(billRatePlan);
	  Mockito.when(sessionService.getSessionAttributes()).
	    thenReturn(new MockSessionAttributes().getMockSessionAttributes());
	  Mockito.when(packageOrderItemService.getCharges(Mockito.anyMap())).thenReturn(chargeList);
	  Mockito.when(patientInsurancePlansService.getVisitPrimaryPlan(Mockito.anyString())).
	    thenReturn(bean);
	  Mockito.when(discountPlanService.listAllDiscountPlanDetails(
	      Mockito.anyList(),Mockito.anyString(),Mockito.any(),Mockito.anyString())).
	    thenReturn(list);
	  Mockito.when(discountPlanService.isItemCategoryPayable(Mockito.anyInt(),
	      Mockito.anyString(), Mockito.anyInt(), Mockito.anyBoolean())).thenReturn(true);
	  Mockito.when(registrationService.findByKey(Mockito.anyString())).thenReturn(bean);
	  Mockito.when(patientdetailsService.findByKey(Mockito.anyString())).thenReturn(bean);
	  Mockito.when(hospitalCenterService.findByKey(Mockito.anyInt())).thenReturn(bean);
	  Mockito.doNothing().when(billChargeTaxService).setTaxAmounts(Mockito.any(BasicDynaBean.class),
        Mockito.any(BasicDynaBean.class),Mockito.any(BasicDynaBean.class),
        Mockito.any(BasicDynaBean.class),Mockito.any(BasicDynaBean.class),
        Mockito.any(BasicDynaBean.class));
	Mockito.when(billService.getBill(Mockito.anyString())).thenReturn(billBean);
	Mockito.when(billService.estimateAmount(Mockito.anyMap())).thenReturn(estimateAmtMap);
	List<Map<String,Object>> expandEditOperationList = new ArrayList<>();
	Mockito.when(operationOrderService.expandEditOperationMap(Mockito.anyMap(),
	  Mockito.anyList(),Mockito.anyString(),Mockito.anyMap())).
	thenReturn(expandEditOperationList);

	Map<String,Object> result = orderService.getItemChargeEstimate(params);
	Assert.assertNotNull(result);

	Map<String,Object> editItemIndex = ( Map<String,Object>)
	  params.get("newly_added_ordered_item");
	editItemIndex.put("editedItemIndex",1);

	Map<String,Object> result1 = orderService.getItemChargeEstimate(params);
	Assert.assertNotNull(result1);

	editItemIndex.put("type", "Operation");

	Map<String,Object> result2 = orderService.getItemChargeEstimate(params);
	Assert.assertNotNull(result2);

	editItemIndex.put("edited", "Y");

	Map<String,Object> result3 = orderService.getItemChargeEstimate(params);
	Assert.assertNotNull(result3);

	editItemIndex.remove("editedItemIndex");
	editItemIndex.put("edited", "N");

	Map<String,Object> result4 = orderService.getItemChargeEstimate(params);
	Assert.assertNotNull(result4);

	}*/

}
